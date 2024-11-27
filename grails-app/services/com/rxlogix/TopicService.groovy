package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.config.*
import com.rxlogix.mapping.MedDraSOC
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.util.AttachmentableUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyResultSetExtension
import groovy.sql.OutParameter
import groovy.sql.Sql
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import oracle.jdbc.OracleTypes
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.io.IOUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

@Transactional
class TopicService {
    def CRUDService
    def activityService
    def userService
    def grailsApplication
    def reportIntegrationService
    def dataSource
    def grailsLinkGenerator
    def dataSource_pva
    def attachmentableService

    def changeAssignedToUser(topicId, newUser) {
        def topic = Topic.get(topicId)
        def oldUser = topic.assignedTo
        topic.assignedTo = newUser
        def details = "Assigned To changed from '${oldUser?.fullName}' to '${newUser?.fullName}' for topic '${topic.name}'"
        def attr = [oldUser: oldUser?.fullName, newUser: newUser?.fullName, topic: topic.name]
        activityService.createActivityForTopic(topic, '', details, ActivityType.findByValue(ActivityTypeValue.AssignedToChange),
                newUser, userService.getUser(), attr)
        CRUDService.update(topic)
        topic
    }

    def changePriority(topicId, priorityValue, justification) {
        Topic topic = Topic.get(topicId)
        def oldPriority = topic.priority
        def newPriority = Priority.findByValue(priorityValue)
        topic.priority = newPriority
        def details = "Priority changed from '${oldPriority?.displayName}' to '${newPriority?.displayName}' for topic '${topic.name}'"
        def attr = [oldPriority: oldPriority?.displayName, newPriority: newPriority?.displayName, topic: topic.name]
        activityService.createActivityForTopic(topic, justification, details, ActivityType.findByValue(ActivityTypeValue.PriorityChange),
                topic.assignedTo, userService.getUser(), attr)
        CRUDService.update(topic)
    }

    def changeWorkflowState(topicId, newState, newDisposition, justification) {
        Topic topic = Topic.get(topicId)
        def responseMap = [:]

        responseMap.topic = topic

        def oldStateObj = topic.workflowState
        def newStateObj = PVSState.findByValue(newState)

        responseMap.oldState = oldStateObj
        responseMap.newState = newStateObj

        def toBeNotified = false

        def workflowRule = WorkflowRule.findAllByIncomeStateAndTargetStateAndIsDeleted(oldStateObj, newStateObj, false)
        if (workflowRule?.notify) {
            toBeNotified = true
        }

        topic.workflowState = newStateObj
        def attrs = [oldWorkflowState:oldStateObj, newWorkflowState: newStateObj, topic: topic.name]
        def details = "Workflow State changed from '$oldStateObj.displayName' to '$newStateObj.displayName'"
        if (newDisposition) {
            def oldDispositionObj = topic.disposition
            def newDispositionObj = Disposition.findByValue(newDisposition)
            if (oldDispositionObj != newDispositionObj) {
                toBeNotified = true
                topic.disposition = newDispositionObj
                attrs << [oldDisposition : oldDispositionObj.displayName, newDisposition: newDispositionObj.displayName]
                details << " and disposition changed from '$oldDispositionObj.displayName' to '$newDispositionObj.displayName'"
            }
        }
        details <<  " for topic '${topic.name}'"
        activityService.createActivityForTopic(topic, justification, details,
                ActivityType.findByValue(ActivityTypeValue.WorkflowStateChange), topic.assignedTo, userService.getUser(), attrs)
        CRUDService.update(topic)
        responseMap.toBeNotified = toBeNotified
        responseMap
    }

    @Transactional
    ValidatedSignal migrateTopicToSignal(Topic topic){
        log.info("Migration Started.")
        ValidatedSignal validatedSignal = new ValidatedSignal(
                name: topic.name, products: topic.products, disposition: topic.disposition, assignedTo: topic.assignedTo,
                startDate: topic.startDate,endDate: topic.endDate,priority: topic.priority, strategy: topic.strategy,initialDataSource: topic.initialDataSource,
                assignmentType: 'USER')
        topic.singleCaseAlerts?.each { validatedSignal.addToSingleCaseAlerts(it) }
        topic.aggregateAlerts?.each { validatedSignal.addToAggregateAlerts(it) }
        topic.adhocAlerts?.each { validatedSignal.addToAdhocAlerts(it) }
        topic.activities?.each { validatedSignal.addToActivities(it) }
        topic.actions?.each { validatedSignal.addToActions(it) }
        topic.comments?.each { validatedSignal.addToComments(it) }
        topic.meetings?.each { validatedSignal.addToMeetings(it) }
        validatedSignal.singleCaseAlerts?.each { topic.removeFromSingleCaseAlerts(it) }
        validatedSignal.aggregateAlerts?.each { topic.removeFromAggregateAlerts(it) }
        validatedSignal.adhocAlerts?.each { topic.removeFromAdhocAlerts(it) }
        validatedSignal.activities?.each { topic.removeFromActivities(it) }
        validatedSignal.actions?.each { topic.removeFromActions(it) }
        validatedSignal.comments?.each { topic.removeFromComments(it) }
        validatedSignal.meetings?.each { topic.removeFromMeetings(it) }
        ArrayList<MedicalConcepts> medicalConcepts = []
        validatedSignal.singleCaseAlerts?.each {SingleCaseAlert singleCaseAlert->
            medicalConcepts = singleCaseAlert.topicConcepts
            medicalConcepts?.each {
                singleCaseAlert.addToSignalConcepts(it)
                singleCaseAlert.removeFromTopicConcepts(it)
            }
        }
        validatedSignal.aggregateAlerts?.each { AggregateCaseAlert aggregateCaseAlert->
            medicalConcepts = aggregateCaseAlert.topicConcepts
            medicalConcepts?.each {
                aggregateCaseAlert.addToSignalConcepts(it)
                aggregateCaseAlert.removeFromTopicConcepts(it)
            }
        }
        validatedSignal.adhocAlerts?.each { AdHocAlert adHocAlert->
            medicalConcepts = adHocAlert.topicConcepts
            medicalConcepts?.each {
                adHocAlert.addToSignalConcepts(it)
                adHocAlert.removeFromTopicConcepts(it)
            }
        }
        validatedSignal = CRUDService.save(validatedSignal)
        migrateAttachments(topic, validatedSignal)
        log.info("Migration Completed.")
        topic.delete(flush: true)
        validatedSignal
    }

    /**
     * Method to migrate the attachments from topic to signal.
     * @param topic
     * @param validatedSignal
     * @return
     */
    def migrateAttachments(Topic topic, ValidatedSignal validatedSignal) {
        InputStream input = null
        OutputStream os = null
        try {
            String contentType
            List<AttachmentLink> attachmentLinkList = AttachmentLink.findAllByReferenceId(topic?.id)
            Set<Attachment> attachmentList
            Attachment newAttachment
            Long oldAttachmentId
            CommonsMultipartFile multipartFile

            FileItem fileItem
            attachmentLinkList.eachWithIndex { AttachmentLink entry, int i ->
                attachmentList = entry.attachments
                attachmentList.each { Attachment attachment ->
                    oldAttachmentId = attachment.id
                    File file = AttachmentableUtil.getFile(grailsApplication.config, attachment)
                    contentType = URLConnection.guessContentTypeFromName(file.getName())
                    fileItem = new DiskFileItem("attachments", contentType, false, file.getName(), (int) file.length(), file.getParentFile());
                    input = new FileInputStream(file);
                    os = fileItem.getOutputStream();
                    IOUtils.copy(input, os);
                    multipartFile = new CommonsMultipartFile(fileItem)
                    newAttachment = attachmentableService.doAddAttachmentForTopicMigration(userService.getUser(), validatedSignal, multipartFile)
                    saveAttachmentDescription(oldAttachmentId, newAttachment)
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage())
        } finally {
            try {
                input?.close()
                os?.close()
            } catch(Throwable t) {
                log.error(t.getMessage())
            }
        }
    }

    void saveAttachmentDescription(Long oldAttachmentId, Attachment newAttachment) {
        AttachmentDescription oldAttachmentDescription = AttachmentDescription.findByAttachment(Attachment.load(oldAttachmentId))
        AttachmentDescription attachmentDescription
        if (oldAttachmentDescription) {
            attachmentDescription = new AttachmentDescription()
            attachmentDescription.attachment = newAttachment
            attachmentDescription.description = oldAttachmentDescription.description
            attachmentDescription.dateCreated = oldAttachmentDescription.dateCreated
            attachmentDescription.createdBy = oldAttachmentDescription.createdBy
            attachmentDescription.save(failOnError: true)
        }
    }

    String generateSignalDetailPageUrl(ValidatedSignal validatedSignal){
        grailsLinkGenerator.link(controller: 'validatedSignal', action: 'topicMigrated', id: validatedSignal.id, params: [isTopicMigrated: true], absolute: true)
    }

    List<Topic> getTopicFromAlertObj(alert, alertType) {
        def inCriteria = 'singleCaseAlerts'
        if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
            inCriteria = 'aggregateAlerts'
        } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
            inCriteria = 'adhocAlerts'
        }
        List<Topic> topicList = []
        List<Topic> topics = Topic.findAll()
        topics?.each { Topic topic ->
            def alerts = topic."$inCriteria"
            if (alerts.contains(alert)) {
                topicList.add(topic)
            }
        }
        topicList
    }

    def scheduleChartReport(signalName, caseData, chartName) {
        def user = userService.getUser()
        try {
            def url = Holders.config.pvreports.url
            def reportTemplates = ["severity": "PVS - Distribution By Seriousness Over Time", "ageGroup": "PVS - Distribution By Age Group Over Time",
                                   "country" : "PVS - Distribution By Country Over Time", "gender" : "PVS - Distribution By Gender Over Time",
                                   "onset" : "PVS - Distribution By Time to Onset",
                                   "quantitativeReviewOverTime" : "SIGNAL_REVIEW_TEMPLATE_QUANTITATIVE"]

            def path = Holders.config.pvreports.api.scheduleReportApi
            def query = [userName: user.username,
                         reportName: signalName,
                         template : reportTemplates.get(chartName),  //Put the template from actual data.
                         caseNumbers: caseData]
            reportIntegrationService.postData(url, path, query, Method.POST)
        } catch (Exception e) {
            e.printStackTrace()
            [status: 500]
        }
    }

    def getChartData(id) {
        try {
            def url = Holders.config.pvreports.url
            def path = "/reports/reportsApi/getChart"
            def query = [id: id]
            reportIntegrationService.postData(url, path, query, Method.POST)
        } catch (Exception e) {
            e.printStackTrace()
            [status: 500]
        }
    }

    def getReportExecutionStatus(id) {
        try {
            def url = Holders.config.pvreports.url
            def path = "/reports/reportsApi/getStatus"
            def query = [id: id]
            return postData(url, path, query, Method.POST)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    private Map postData(String baseUrl, String path, def query, method = Method.POST) {
        def ret = [:]
        def http = new HTTPBuilder(baseUrl)
        // perform a POST request, expecting JSON response
        http.request(method, ContentType.JSON) {
            uri.path = path
            uri.query = query
            // response handlers
            response.success = { resp, reader -> ret = [status: resp.status, result: reader] }
            response.failure = { resp -> ret = [status: resp.status] }
        }
        return ret
    }

    def getTopicsFromAlertObj(alert, alertType) {
        def inCriteria = 'singleCaseAlerts'
        if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
            inCriteria = 'aggregateAlerts'
        } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
            inCriteria = 'adhocAlerts'
        }
        def topicList = Topic.createCriteria().list {
            "$inCriteria"{
                eq('id', alert.id)
            }
        }
        topicList
    }

    @Transactional
    def changeGroup(topic, selectedGroups) {

        def oldGroups = topic.sharedGroups
        def oldGroupNames = ''
        if (oldGroups) {
            oldGroups.each {
                oldGroupNames = oldGroupNames + it.name + ","
            }
        }

        Sql sqlDeletion = new Sql(dataSource)

        def deletionQuery = "delete from TOPIC_GROUP where TOPIC_ID="+topic.id

        try {
            sqlDeletion.execute(deletionQuery);
            sqlDeletion.commit()
        } catch(Exception ex) {
            sqlDeletion.rollback()
        } finally {
            sqlDeletion.close()
        }
        def newGroupNames = ""
        Sql sqlInsertion = new Sql(dataSource)
        try {
            selectedGroups.split(',').each {
                def groupId = Long.parseLong(it)
                def groupObj = Group.findById(groupId, [null: null])
                if (groupObj) {
                    newGroupNames = newGroupNames + groupObj.name + ","

                }
                def sqlstr = """INSERT INTO TOPIC_GROUP VALUES ($topic.id, $groupId)"""
                sqlInsertion.execute(sqlstr);
                topic.assignmentType = "GROUP"
                topic.save(flush: true)

            }
        }catch(Exception ex) {
            sqlInsertion.rollback()
        } finally {
            sqlInsertion.commit()
            sqlDeletion.close()
        }
        return newGroupNames
    }

    def heatMapData(Topic topic) {
        def socs = (topic.aggregateAlerts).collect { it.soc }.unique().sort()
        def prodNames = (topic.aggregateAlerts).collect { it.productName }.unique().sort()
        def data = []

        socs.eachWithIndex { soc, i ->
            def rowData = []
            prodNames.eachWithIndex { prodName, j ->
                def sumOfCumSpont = topic.aggregateAlerts.findAll { it.productName == prodName }.inject(0, {
                    Integer memo, AggregateCaseAlert next ->
                        memo = memo + next.cumSponCount
                        memo

                })

                def sumOfTotalEv = 0

                rowData.push(sumOfCumSpont + sumOfTotalEv)
            }

            data.push(rowData)
        }

        socs = MedDraSOC.withTransaction {
            MedDraSOC.findAllByLangId(1).unique().collect { it.name }.sort()
        }
        prodNames = prodNames

        /* TODO We are making some fake data for the demo */
        def years = ['Jan-2017', 'Feb-2017', 'Mar-2017', 'Apr-2017', 'May-2017', 'Jun-2017', 'Jul-2017']

        def random = new Random()
        data = []

        years.eachWithIndex { iv, j ->
            socs.eachWithIndex { String entry, int i ->
                def ran = random.nextInt(200)
                if (ran)
                    data.add([j, i, ran])
            }
        }

        data = [[0, 0, 9], [0, 1, 16], [0, 2, 18], [0, 3, 19], [0, 4, 18], [0, 5, 13], [0, 6, 4], [0, 7, 10], [0, 8, 19], [0, 9, 12], [0, 10, 44], [0, 11, 62], [0, 12, 12], [0, 13, 72], [0, 14, 8], [0, 15, 15], [0, 16, 43], [0, 17, 18], [0, 18, 14], [0, 19, 4], [0, 20, 14], [0, 21, 64], [0, 22, 3], [0, 23, 35], [0, 24, 25], [0, 25, 52], [1, 0, 25], [1, 1, 2], [1, 2, 50], [1, 3, 27], [1, 4, 52], [1, 5, 9], [1, 6, 13], [1, 7, 18], [1, 8, 18], [1, 9, 8], [1, 10, 1], [1, 11, 17], [1, 12, 14], [1, 13, 30], [1, 14, 20], [1, 15, 34], [1, 16, 14], [1, 17, 16], [1, 18, 12], [1, 19, 44], [1, 20, 22], [1, 21, 4], [1, 22, 32], [1, 23, 69], [1, 24, 18], [1, 25, 12], [2, 0, 34], [2, 1, 15], [2, 2, 44], [2, 3, 14], [2, 4, 38], [2, 5, 19], [2, 6, 18], [2, 7, 12], [2, 8, 22], [2, 9, 37], [2, 10, 22], [2, 11, 31], [2, 12, 13], [2, 13, 23], [2, 14, 70], [2, 15, 12], [2, 16, 24], [2, 17, 36], [2, 18, 32], [2, 19, 70], [2, 20, 17], [2, 21, 17], [2, 22, 14], [2, 23, 36], [2, 24, 8], [2, 25, 50], [3, 0, 10], [3, 1, 39], [3, 2, 37], [3, 3, 41], [3, 4, 23], [3, 5, 16], [3, 6, 17], [3, 7, 18], [3, 8, 19], [3, 9, 6], [3, 10, 24], [3, 11, 17], [3, 12, 26], [3, 13, 6], [3, 14, 32], [3, 15, 26], [3, 16, 15], [3, 17, 7], [3, 18, 11], [3, 19, 40], [3, 20, 62], [3, 21, 32], [3, 22, 25], [3, 23, 7], [3, 24, 34], [3, 25, 18], [4, 0, 24], [4, 1, 38], [4, 2, 7], [4, 3, 38], [4, 4, 14], [4, 5, 7], [4, 6, 4], [4, 7, 42], [4, 8, 11], [4, 9, 30], [4, 10, 16], [4, 11, 49], [4, 12, 27], [4, 13, 11], [4, 14, 19], [4, 15, 41], [4, 16, 24], [4, 17, 74], [4, 18, 29], [4, 19, 60], [4, 20, 13], [4, 21, 15], [4, 22, 25], [4, 23, 25], [4, 24, 10], [4, 25, 14], [5, 0, 18], [5, 1, 17], [5, 2, 37], [5, 3, 17], [5, 4, 8], [5, 5, 12], [5, 6, 55], [5, 7, 33], [5, 8, 19], [5, 9, 31], [5, 10, 34], [5, 11, 6], [5, 12, 22], [5, 13, 17], [5, 14, 18], [5, 15, 42], [5, 16, 8], [5, 17, 14], [5, 18, 34], [5, 19, 7], [5, 20, 12], [5, 21, 5], [5, 22, 8], [5, 23, 18], [5, 24, 21], [5, 25, 10], [6, 0, 36], [6, 1, 35], [6, 2, 25], [6, 3, 7], [6, 4, 52], [6, 5, 36], [6, 6, 60], [6, 7, 73], [6, 8, 29], [6, 9, 12], [6, 10, 28], [6, 11, 3], [6, 12, 9], [6, 13, 4], [6, 14, 10], [6, 15, 37], [6, 16, 43], [6, 17, 8], [6, 18, 4], [6, 19, 10], [6, 20, 6], [6, 21, 7], [6, 22, 5], [6, 23, 14], [6, 24, 38], [6, 25, 50]]

        [socs: socs as JSON, years: years as JSON, data: data as JSON]
    }

    def insertSingleAndAggregateCases(Topic topic) {
        def scaList = []
        def aggList = []

        topic.aggregateAlerts.each { agg ->
            def map = [:]
            map.put("caseNumber", null)
            map.put("verNumber", agg.productId)
            map.put("type", "PRODUCT_ID")
            aggList.add(map)
        }

        topic.singleCaseAlerts.each { sca ->
            def map = [:]
            map.put("caseNumber", sca.caseNumber)
            map.put("verNumber", sca.caseVersion)
            map.put("type", "CASE_ID")
            scaList.add(map)
        }

        def sql = new Sql(dataSource_pva)
        deletePreviousDataFromTable(sql)

        //populate data in table

        callSqlBatchStatement(aggList, sql)
        callSqlBatchStatement(scaList, sql)
        sql.close()

        def dataMap = callAssessmentProc()
        dataMap

    }

    def deletePreviousDataFromTable(sql) {
        sql.execute("delete from signal_report_cases")
        sql.execute("COMMIT")
    }

    def callSqlBatchStatement(def list, sql) {
        def size = list.size()
        sql.withBatch(size, "insert into signal_report_cases(case_id, case_num, version_num, type) values (:val0, :val1, :val2, :val3)".toString(), { preparedStatement ->
            list.each {
                preparedStatement.addBatch(val0: null, val1: it.caseNumber, val2: it.verNumber, val3: it.type)
            }
        })
    }

    def callAssessmentProc() {
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            public int getType() {
                return OracleTypes.CURSOR
            }
        }

        def sql = new Sql(dataSource_pva)
        def map = [:]
        def procedure = "call P_SIGNAL_REPORTS(?,?,?,?,?)"

        sql.call("{${procedure}}", [null, null, null, null, CURSOR_PARAMETER]) { result ->
            result.eachRow() { GroovyResultSetExtension resultRow ->
                def rowList = []
                def tempMap = [:]
                def header = resultRow.getProperty("FULL_GROUP")
                def val = resultRow.getProperty("VAL")
                def percent = resultRow.getProperty("PERCENT")

                tempMap.put(val, percent)
                rowList.add(tempMap)
                if (!map[header]) {
                    map.put(header, rowList)
                } else {
                    List tempList = map[header]
                    tempList = tempList + rowList
                    map.put(header, tempList)

                }
            }
        }
        sql.close()
        map
    }


    def detachTopic(alert, alertType, String justification) {
        def details = ""
        def activityType
        try {
            def existingTopics = getTopicsFromAlertObj(alert, alertType)
            existingTopics.each { Topic topic ->
                if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
                    topic.removeFromSingleCaseAlerts(alert)
                    details = "Case '${alert.caseNumber}' has been dissociated from '${topic.name}'"
                    activityType = ActivityType.findByValue(ActivityTypeValue.CaseDissociated)
                } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
                    topic.removeFromAggregateAlerts(alert)
                    details = "PEC '${alert.productName}'-'${alert.pt}' has been dissociated from '${topic.name}'"
                    activityType = ActivityType.findByValue(ActivityTypeValue.PECDissociated)
                } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
                    topic.removeFromAdhocAlerts(alert)
                    details = "AdHoc '${alert.name}' has been dissociated from '${topic.name}'"
                    activityType = ActivityType.findByValue(ActivityTypeValue.AdhocAlertDissociated)
                }

                if (details) {
                    details += " with justification - '$justification'"
                }
                def attr = [alert: alert.name, alertType: alertType, signal: topic.name]
                activityService.createActivityForTopic(topic, '', details, activityType,
                        topic.assignedTo, userService.getUser(), attr)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

}
