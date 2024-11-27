package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.config.*
import com.rxlogix.controllers.SignalController
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.SignalAssessmentDateRangeEnum
import com.rxlogix.signal.*
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSetExtension
import groovy.sql.OutParameter
import groovy.sql.Sql
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import oracle.jdbc.OracleTypes
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.Cookie
import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class TopicController implements SignalController {

    def CRUDService
    def productEventHistoryService
    def topicService
    def workflowService
    def alertDocumentService
    def alertAttributesService
    def activityService
    def userService
    def attachmentableService
    def dynamicReportService
    def specialPEService
    def validatedSignalService
    def dataSource_pva
    def emailService
    def messageSource
    def aggregateCaseAlertService
    def singleCaseAlertService
    def validatedSignalChartService
    def productBasedSecurityService

    def index() {
        redirect(controller: "validatedSignal", action: "index")
    }

    def list() {
        List<String> allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(user)
        def topics = Topic.list()?.findAll {
            def productNameList = it.getProductNameList()
            if(!(productNameList instanceof String) && allowedProductsToUser?.intersect(productNameList).size() > 0){
                return true
            } else {
                return false
            }
        }
        topics = topics?.collect {
            it.toDto()
        }
        respond topics, [formats: ['json']]
    }

    def addAlertToTopic() {

        try {
            def topicNames = params.topicName.split(",")
            def updateFlag = true
            topicNames.each {
                Topic topic = Topic.findByName(it)
                if (!topic) {
                    topic = createTopicWithDefaultValues()
                    topic.name = it
                    updateFlag = false
                }
                if (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    addAggregateToTopic(topic)
                } else if (params.appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    addSingleToTopic(topic)
                } else {
                    addAdhocToTopic(topic)
                }

                if (updateFlag) {
                    CRUDService.update(topic)
                } else {
                    CRUDService.save(topic)
                }
            }

            request.withFormat {
                form {
                    flash.message = message(code: 'default.created.message',
                            args: ['Topic', params.topicName])
                    redirect(controller: "validatedSignal", action: "index")
                }
                '*' { respond topic, [status: CREATED] }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            def msg = [message: "Topic Creation Failed."]
            respond msg, [status: BAD_REQUEST]
        }
    }


    private void addAdhocToTopic(Topic topic) {
        AdHocAlert adhoc = AdHocAlert.findById(Long.parseLong(params.alertId))
        adhoc = addMedicalConceptsToAlert(adhoc)
        topic.addToAdhocAlerts(adhoc)
    }

    private void addSingleToTopic(Topic topic) throws IllegalArgumentException {
        SingleCaseAlert single = SingleCaseAlert.findById(Long.parseLong(params.alertId))
        single = addMedicalConceptsToAlert(single)
        topic.addToSingleCaseAlerts(single)
    }


    private void addAggregateToTopic(Topic topic) {
        AggregateCaseAlert agg = AggregateCaseAlert.findById(Long.parseLong(params.alertId))
        agg = addMedicalConceptsToAlert(agg)
        topic.addToAggregateAlerts(agg)
    }

    void validateParams(def params, boolean updateFlag) throws IllegalArgumentException {
        if (!params.medicalConcepts) {
            throw new IllegalArgumentException("Medical Concepts is required.")
        }
        if (!params.startDate) {
            throw new IllegalArgumentException("Start Date is required.")
        }
        if (!params.endDate) {
            throw new IllegalArgumentException("End Date is required.")
        }
        if (!params.product && !updateFlag) {
            throw new IllegalArgumentException("Product is required.")
        }
    }

    def addBatchAlertToTopic() {
        if (!params.topicName) {
            throw new IllegalArgumentException("Topic name is required.")
        }
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: 'default.created.message', args: ['Topic', params.topicName]))
        Topic topic = Topic.findByName(params.topicName)
        boolean updateFlag = true
        if (!topic) {
            updateFlag = false
        }
        JsonSlurper jsonSlurper = new JsonSlurper()
        validateParams(params, updateFlag)
        def alertListObj = jsonSlurper.parseText(params.alertArray)
        if (!topic) {
            topic = createTopicWithDefaultValues()
            topic.name = params.topicName
        }
        String alertId
        def alert
        def productName
        def alertName
        if (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            alertListObj.each {
                params.alertId = it.alertId
                alert = AggregateCaseAlert.findById(it.alertId)
                def executedConfigId = alert.executedAlertConfiguration.id
                def executedConfig = aggregateCaseAlertService.getExecConfigurationById(executedConfigId)
                alertName = alert.name
                addAggregateToTopic(topic)
                activityService.createActivity(executedConfig, ActivityType.findByValue(ActivityTypeValue.TopicAdded),
                        userService.getUser(), "PEC " + "Added to topic " + params.topicName, null,
                        ['For Aggregate Alert'], alert.productName, alert.pt, alert.assignedTo, null)
            }
        } else if (params.appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            alertListObj.each {
                params.alertId = it.alertId
                alert = SingleCaseAlert.findById(it.alertId)
                def executedConfigId = alert.executedAlertConfiguration.id
                def executedConfig = singleCaseAlertService.getExecConfigurationById(executedConfigId)
                productName = alert.productFamily
                alertName = alert.name
                addSingleToTopic(topic)
                activityService.createActivity(executedConfig, ActivityType.findByValue(ActivityTypeValue.TopicAdded),
                        userService.getUser(), "Case " + alert.caseNumber + " added to topic " + params.topicName, null,
                        ['For Case Number': alert.caseNumber], productName, alert.getAttr('masterPrefTermAll_7'), userService.getUser(), alert.caseNumber)
            }
        } else {
            alertListObj.each {
                params.alertId = it.alertId
                alert = AdHocAlert.findById(it.alertId)
                addAdhocToTopic(topic)
                activityService.create(alert, ActivityType.findByValue(ActivityTypeValue.TopicAdded),
                        userService.getUser(), "Added to topic " + params.topicName,
                        null, [product: (alert.productSelection),
                               event  : (alert.eventSelection)])
            }
        }

        try {
            if (updateFlag) {
                CRUDService.update(topic)
            } else {
                CRUDService.save(topic)
            }
        } catch (Exception ex) {
            log.error('Exception occurred', ex)
            responseDTO.status = false
            responseDTO.message = "Topic Creation Failed."
        }
        render(responseDTO as JSON)
    }

    def addMedicalConceptsToAlert(alert) {
        def medicalConceptsList = params.medicalConcepts.split(",")
        medicalConceptsList.each {
            MedicalConcepts medicalConcepts = MedicalConcepts.findById(it)
            alert.addToTopicConcepts(medicalConcepts)
        }
        alert
    }

    def exportReport() {
        def topics
        if(params.idList) {
            def idList = params.idList.split(',')
            idList = idList.collect{Long.parseLong(it)}
            topics = Topic.withCriteria{ 'in'('id', idList) }.collect { it.toDto() }
        }else if(params.idList == ''){
            topics = []
        }else{
            topics = Topic.list().collect { it.toDto() }
        }
        def reportFile = dynamicReportService.createtopicsReport(new JRMapCollectionDataSource(topics), params)
        renderReportOutputType(reportFile, "TopicListing")
    }

    def createTopicWithDefaultValues() {
        Topic topic = new Topic()
        topic.disposition = Disposition.findByValue(grailsApplication.config.pvsignal.disposition.default.value)
        topic.assignedTo = userService.getUser()
        topic.workflowState = PVSState.findByValue(grailsApplication.config.pvsignal.workflow.default.value)
        topic.priority = Priority.findByValue(grailsApplication.config.pvsignal.priority.signal.default.value)
        topic.startDate = new Date()
        topic.endDate = new Date()
        topic.initialDataSource = params.initialDataSource
        topic.products = params.product
        topic.createdBy =userService.getUser().getUsername()
        topic.modifiedBy =userService.getUser().getUsername()

        topic
    }

    def create() {
        def topic = new Topic()
        def initialDataSource = alertAttributesService.get('initialDataSource')
        [topic: topic, initialDataSource: initialDataSource]
    }

    def save() {

        Boolean isError = false
        Topic topic = new Topic()
        topic = createTopicObj(topic)
        try {
            Boolean isTopicExists = Topic.countByName(params.name) as Boolean

            def isProductOrFamilyOrIngredient = allowedDictionarySelection(topic)

            if (!isProductOrFamilyOrIngredient) {
                flash.error = message(code: "app.label.product.family.error.message")
                def initialDataSource = alertAttributesService.get('initialDataSource')
                render(view: "create", model: [topic: topic, initialDataSource: initialDataSource])
                return
            }

            if (!isTopicExists) {
                CRUDService.save(topic)
                def alertLink = createHref("topic", "details", ["id": topic.id])
                emailService.sendNotificationEmail(['toAddress': [topic.assignedTo.email],
                                                    "inboxType": "Topic Creation",
                                                    'title'    : message(code: "email.topic.create.title"),
                                                    'map'      : ["map": ['Topic Name '       : topic.name,
                                                                          "Product Name"      : topic.getProductNameList(),
                                                                          "Workflow State"    : topic.workflowState?.displayName,
                                                                          "Assigned User"     : topic.assignedTo.fullName],
                                                                  "emailMessage": message(code: "email.topic.create.message"),
                                                                  "screenName"  : "Topic",
                                                                  "alertLink"   : alertLink]
                ])

            } else {
                isError = true
                flash.error = "Topic already exists with same name."
            }
        } catch (Throwable ex) {
            isError = true
            log.error(ex.getMessage())
        }
        if (isError) {
            def initialDataSource = alertAttributesService.get('initialDataSource')
            render(view: "create", model: [topic: topic,  initialDataSource: initialDataSource])
        } else {
            redirect(controller: 'validatedSignal', action: 'index')
        }
    }

    def edit(Long topicId) {
        Topic topic = Topic.get(topicId)
        def initialDataSource = alertAttributesService.get('initialDataSource')
        render(view: "edit", model: [topic: topic, initialDataSource: initialDataSource])
    }

    def update() {
        boolean isError = false
        Boolean isTopicExists = false
        Long topicId = params.topicId as Long
        Topic topic = Topic.get(topicId)
        boolean isTopicNameChanged = (params.name == topic.name) ? false : true
        topic = createTopicObj(topic)
        def isProductOrFamilyOrIngredient = allowedDictionarySelection(topic)

        if (!isProductOrFamilyOrIngredient) {
            flash.error = message(code: "app.label.product.family.error.message")
            def initialDataSource = alertAttributesService.get('initialDataSource')
            render(view: "edit", model: [topic: topic, initialDataSource: initialDataSource])
            return
        }
        try {
            if (isTopicNameChanged) {
                isTopicExists = Topic.countByName(params.name) as Boolean
            }
            if (!isTopicExists) {
                CRUDService.update(topic)
            } else {
                isError = true
                flash.error = "Topic already exists with same name."
            }
        } catch (Throwable ex) {
            isError = true
            log.error(ex.getMessage())
        }
        if (isError) {
            def initialDataSource = alertAttributesService.get('initialDataSource')
            render(view: "edit", model: [topic: topic, initialDataSource: initialDataSource])
        } else {
            redirect(controller: 'validatedSignal', action: 'index')
        }
    }

    private Topic createTopicObj(Topic topic) {
        topic.name = params.name
        topic.products = params.productNames
        topic.initialDataSource = params.initialDataSource
        def startDate = params.startDate[0]
        def endDate = params.endDate[0]
        topic.startDate =
                DateUtil.stringToDate(startDate, "MM/dd/yyyy", grailsApplication.config.server.timezone)
        topic.endDate =
                DateUtil.stringToDate(endDate, "MM/dd/yyyy", grailsApplication.config.server.timezone)

        if (params.priority != null && params.priority != '' && params.priority != 'null') {
            topic.priority = Priority.get(Long.parseLong(params.priority))
        } else {
            topic.priority = Priority.findByValue(grailsApplication.config.pvsignal.priority.signal.default.value)
        }
        if (params.topicStrategy != null && params.topicStrategy != 'null') {
            topic.strategy = SignalStrategy.get(Long.parseLong(params.topicStrategy))
        }

        if (params.assignedTo != null && params.assignedTo != '' && params.assignedTo != 'null') {
            topic.assignedTo = User.get(Long.parseLong(params.assignedTo))
        } else {
            topic.assignedTo = userService.getUser()
        }
        topic.workflowState = PVSState.findByValue(grailsApplication.config.pvsignal.workflow.default.value)
        topic.disposition = Disposition.findByValue(grailsApplication.config.pvsignal.disposition.default.value)
        topic.initialDataSource = params.initialDataSource
        topic.products = params.productSelection
        topic
    }

    def details() {

        def topicid = params.id
        def topic = Topic.findById(topicid)

        def emergingSafetyList = EmergingIssue.list()
        def safetyIssueList = []

        List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.TOPIC)

        if (emergingSafetyList) {
            safetyIssueList = validatedSignalService.getEmergingSafetyIssueList(emergingSafetyList)
        }
        def caseCount = topic?.singleCaseAlerts.size()
        def pecCountArgus = 0


        def emergingIssuesList = []
        def specialPEList = []
        def aggAlerts = topic?.aggregateAlerts
        aggAlerts?.each {
            if (safetyIssueList.contains(it.pt)) {
                emergingIssuesList.add(it.pt)
            }
            def isSpecialPE = specialPEService.isSpecialPE(it.productName, it.pt)
            if (isSpecialPE) {
                specialPEList.add(it.productName + " - " + it.pt)
            }
            if (it.alertConfiguration.selectedDatasource == Constants.DataSource.PVA) {
                pecCountArgus = pecCountArgus + 1
            }
        }
        def conceptsMap = createConceptsMap(topic)
        def scaConfigCount = getAlertConfigCount(topic, Constants.AlertConfigType.SINGLE_CASE_ALERT)
        def aggConfigCount = getAlertConfigCount(topic, Constants.AlertConfigType.SINGLE_CASE_ALERT)
        def allCaseCount = getAllAlertCount(topic, Constants.AlertConfigType.SINGLE_CASE_ALERT, SingleCaseAlert)
        def allPECount = getAllAlertCount(topic, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, AggregateCaseAlert)

        def chartCount = SignalChart.countByTopic(topic)
        List userList = User.list().sort { it.fullName?.toLowerCase() }.collect {
            [id: it.id, fullName: it.fullName]
        }

        def assessmentDetails = topicService.insertSingleAndAggregateCases(topic)
        Map heatMap = topicService.heatMapData(topic)
        List<String> datasources = Holders.config.pvsignal.supported.datasource.call()

        def heatMapModel = [socs: heatMap.socs, years: heatMap.years, data: heatMap.data]
        render(view: "details", model: [topic         : topic, emergingIssues: emergingIssuesList, scaConfigCount: scaConfigCount, datasources: datasources*.toUpperCase(),
                                        aggConfigCount: aggConfigCount, allCaseCount: allCaseCount, allPECount: allPECount, chartCount: chartCount,
                                        caseCount: caseCount, pecCountArgus: pecCountArgus, actionConfigList: actionConfigList,
                                        conceptsMap   : conceptsMap, specialPEList: specialPEList, userList: userList, heatMap: heatMapModel, assessmentDetails: assessmentDetails]
        )

    }

    def createConceptsMap(Topic topic) {
        def medicalConceptsMap = [singleCaseAlerts: [:], aggregateAlerts: [:], adhocAlerts: [:]]

        ['singleCaseAlerts', 'aggregateAlerts', 'adhocAlerts'].each { alertType ->
            String[] conceptArray = topic."$alertType".signalConcepts.name.flatten()
            conceptArray.each { name ->
                if (medicalConceptsMap[alertType][name] == null)
                    medicalConceptsMap[alertType][name] = 1
                else
                    (medicalConceptsMap[alertType][name])++
            }
        }

        // Turn the map 90 degree
        def concepts = [:]
        medicalConceptsMap.each { key, Map<String, String> values ->
            values.each { name, cnt ->
                if (concepts[name] == null) {
                    def tmp = [:]
                    tmp.put(key, cnt)
                    concepts[name] = tmp
                } else
                    concepts[name][key] = cnt
            }
        }
        concepts
    }

    private getAlertConfigCount(topic, type) {
        def scaCount = Configuration.findAllByType(type)?.size()
        scaCount
    }

    private getAllAlertCount(topic, type, domain) {
        def configurations = Configuration.findAllByType(type)
        def countList = []
        configurations.each {
            def alert = domain.findAllByAlertConfiguration(it)
            countList.add(alert)
        }
        countList = countList.flatten()
        countList.size()
    }

    def singleCaseAlertList() {
        Topic topic = Topic.get(Long.parseLong(params.id))
        def result = topic.singleCaseAlerts
        respond result.collect {
            it.composeAlert("UTC")
        }, [formats: ['json']]
    }

    def aggregateCaseAlertList() {
        def topic = Topic.get(Long.parseLong(params.id))
        def result = topic.aggregateAlerts

        respond result.collect {
            def isSpecialPE = null
            def trend = null
            if (it instanceof EvdasAlert) {
                isSpecialPE = specialPEService.isSpecialPE(it.substance, it.pt)
                trend = productEventHistoryService.getProductEventHistoryTrend(it.substance, it.pt)
            } else {
                isSpecialPE = specialPEService.isSpecialPE(it.productName, it.pt)
                trend = productEventHistoryService.getProductEventHistoryTrend(it.productName, it.pt)
            }
            it.toDto("UTC", isSpecialPE, trend)
        }, [formats: ['json']]
    }

    def adHocAlertList(Long id) {
        Topic topic = Topic.get(id)
        Set<AdHocAlert> result = topic.adhocAlerts
        Map responseMap = [:]

        respond result.collect {
            responseMap = it.details()
            def topics = topicService.getTopicFromAlertObj(it, Constants.AlertConfigType.AD_HOC_ALERT)
            responseMap.signalsAndTopics = topics?.collect { it.name + "(T)" }?.join(",")
            responseMap
        }, [formats: ['json']]
    }

    def changeAssignedTo(Long id) {
        def topicId = id
        def newUserId = Long.parseLong(params.newValue)

        if (topicId && newUserId) {
            def newUser = User.findById(newUserId)

            try {
                Topic topic = topicService.changeAssignedToUser(topicId, newUser)
                sendAssignedToEmail([newUser.email], topic)
                render(contentType: "application/json", status: OK.value()) {
                    [success: 'true', newValue: newUser.fullName, newId: newUser.id]
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            }
            return
        }
        render(status: BAD_REQUEST)
    }

    private void sendAssignedToEmail(List recipientList, Topic topic) {
        if (recipientList) {
            def alertLink = createHref("topic", "details", ["id": topic.id])
            emailService.sendNotificationEmail([
                    'toAddress': recipientList,
                    "inboxType": "Assigned To Change",
                    'title'    : message(code: "email.topic.assignedTo.change.title"),
                    'map'      : ["map"         : ['Topic Name'     : topic.name,
                                                   'Product Name'   : topic.getProductNameList(),
                                                   'Workflow State' : topic.workflowState?.displayName,
                                                   'Assigned To'    : topic.assignedTo.fullName],
                                  "emailMessage": message(code: "email.topic.assignedTo.change.message"),
                                  "screenName"  :"Topic",
                                  "alertLink"   : alertLink
                    ]
            ])
        }
    }

    def changeGroup(Long id) {
        def topicId = id
        def selectedGroups = params.selectedGroups

        def newGroups = ''
        if (topicId && selectedGroups) {
            try {
                Topic topic = Topic.get(id);
                newGroups = topicService.changeGroup(topic, selectedGroups)
                List recipientList = topic.sharedGroups.collect { it.members.collect { it.email } }.flatten()
                sendAssignedToEmail(recipientList, topic)
            } catch (Exception ex) {
                log.error('Error occurred while changing group', ex)
                render(status: BAD_REQUEST)
                return
            }
        }
        render(contentType: "application/json", status: OK.value()) {
            [success: 'true', newValue: newGroups]
        }
    }

    def changePriority() {
        def topicId = Long.parseLong(params.id)
        def newPriority = params.newValue
        def justification = params.justification

        if (topicId && newPriority) {

            try {
                topicService.changePriority(topicId, newPriority, justification)
                render(contentType: "application/json", status: OK.value()) {
                    [success: 'true', newValue: newPriority]
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            }
            return
        }
        render(status: BAD_REQUEST)
    }

    def changeWorkflowState() {
        Boolean isTopicMigrated = false
        def topicId = Long.parseLong(params.id)
        def topic = Topic.findById(topicId)

        def newState = params.newValue
        def justification = params.justification
        String newDisposition = params.extra_value
        String url = null
        if (topicId && newState) {
            try {
                def response = topicService.changeWorkflowState(topicId, newState, newDisposition, justification)
                if (newDisposition) {
                    def newDispositionObj = Disposition.findByValue(newDisposition)
                    if (newDispositionObj.validatedConfirmed) {
                        ValidatedSignal validatedSignal = topicService.migrateTopicToSignal(topic)
                        validatedSignalService.saveHistoryForSignal(validatedSignal)
                        activityService.createActivityForSignal(validatedSignal, params.justification, "${validatedSignal.name} has been migrated to Signal successfully ", ActivityType.findByValue(ActivityTypeValue.TopicMigrated),
                                validatedSignal.assignedTo, userService.getUser(), null)
                        url = topicService.generateSignalDetailPageUrl(validatedSignal)
                        isTopicMigrated = true
                        if(response.toBeNotified) {
                            def alertLink = createHref("validatedSignal", "details", ["id": topic.id])
                            emailService.sendNotificationEmail(['toAddress': [validatedSignal?.assignedTo?.email],
                                                                "inboxType": messageSource.getMessage("email.workflow.change.title", null, Locale.default),
                                                                'title'    : message(code: "email.topic.workflow.change.title"),
                                                                'map'      : ["map"         : ['Signal Name '      : validatedSignal.name,
                                                                                               "Product Name"      : validatedSignal.getProductNameList(),
                                                                                               "Priority "         : validatedSignal.priority.displayName,
                                                                                               "Old Workflow State": response.oldState,
                                                                                               "New Workflow State": response.newState,
                                                                                               "Assigned User"     : validatedSignal.assignedTo.fullName],
                                                                              "emailMessage": message(code: "email.topic.workflow.change.signalMigration.message", args: [validatedSignal.name]),
                                                                              "screenName"  : "Signal",
                                                                              "alertLink"   : alertLink]
                            ])
                        }
                    }
                }
                else if (response.toBeNotified) {
                    def alertLink = createHref("topic", "details", ["id": topic.id])
                    emailService.sendNotificationEmail(['toAddress': [topic?.assignedTo?.email],
                                                        "inboxType": messageSource.getMessage("email.workflow.change.title", null, Locale.default),
                                                        'title'    : message(code: "email.topic.workflow.change.title"),
                                                        'map'      : ["map": ['Topic Name '       : topic.name,
                                                                              "Product Name"      : topic.getProductNameList(),
                                                                              "Priority "         : topic.priority.displayName,
                                                                              "Old Workflow State": response.oldState,
                                                                              "New Workflow State": response.newState,
                                                                              "Assigned User"     : topic.assignedTo.fullName],
                                                                      "emailMessage": message(code: "email.topic.workflow.change.message",args: [topic.name]),
                                                                      "screenName"  :"Topic",
                                                                      "alertLink"   : alertLink]
                    ])
                }
                render([success: 'true', newValue: newState, isTopicMigrated: isTopicMigrated, url: url] as JSON)
            } catch (Exception ex) {
                render([success: 'false', message: 'Error occurred while changing workflow state'] as JSON)
            }
            return
        }
        render(status: BAD_REQUEST)
    }

    def convertTopicAlertstoSignal(Topic topic) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        def signalObj = jsonSlurper.parseText(params.signalList)
        ValidatedSignal validatedSignal
        def updateFlag = true
        signalObj.each {
            validatedSignal = ValidatedSignal.findByName(it.signalName)
            if (!validatedSignal) {
                validatedSignal = validatedSignalService.createSignalWithDefaultValues()
                validatedSignal.name = it.signalName
                updateFlag = false
            }
        }

        topic.aggregateAlerts.each {
            validatedSignal.addToAggregateAlerts(it)
        }

        topic.singleCaseAlerts.each {
            validatedSignal.addToSingleCaseAlerts(it)
        }

        topic.adhocAlerts.each {
            validatedSignal.addToAdhocAlerts(it)
        }
        try {
            if (updateFlag) {
                CRUDService.update(validatedSignal)
            } else {
                CRUDService.save(validatedSignal)
            }
        } catch (Exception ex) {
            log.info("Unable to Transfer alerts from TOPIC to SIGNAL")
        }
        topic

    }

    def getPriorities() {
        def justificationObjList = Justification.list()
        def justificationList = []
        def topicId = Long.parseLong(params.id)
        Topic topic = Topic.get(topicId)

        justificationObjList.each {
            if (it.getAttr("topicPriority") == "on") {
                justificationList.add(it)
            }
        }
        def map = [currentValue: topic.priority.displayName, availableValues: (workflowService.priorities()), justification: justificationList]

        render(map as JSON)
    }

    def upload(final Long id) {

        Topic topic = Topic.findById(id)
        def fileName = params?.attachments.filename
        if (fileName instanceof String) {
            fileName = [fileName]
        }

        User currentUser = userService.getUser()
        attachmentableService.attachUploadFileTo(currentUser, fileName, topic, request)
        String fileDescription = params.description
        if (fileDescription) {
            List<Attachment> attachments = topic.getAttachments()
            //Get the Last/Latest attachment of this instance
            if (attachments) {
                attachments.each { attachment ->
                    AttachmentDescription attachmentDescription = new AttachmentDescription()
                    attachmentDescription.attachment = attachment
                    attachmentDescription.createdBy = userService.getUser().username
                    attachmentDescription.description = fileDescription
                    attachmentDescription.save()
                }
            }
        }

        if (fileName) {
            def attr = [fileName: fileName, topic: topic.name]
            activityService.createActivityForTopic(topic, '', "Attachment '" + fileName + "' is added", ActivityType.findByValue(ActivityTypeValue.AttachmentAdded), topic.assignedTo,
                    userService.getUser(), attr)
        }
        redirect(action: "details", params: [id: id])
    }

    def deleteAttachment() {
        def attachment = Attachment.findById(params?.attachmentId)
        if (attachment) {
            def fileName = attachment.name + "." + attachment.ext
            def topic = Topic.findById(params?.alertId)
            if (AttachmentDescription.findByAttachment(attachment)) {
                AttachmentDescription.findByAttachment(attachment).delete()
            }
            def result = attachmentableService.removeAttachment(Long.parseLong(params.attachmentId))
            def attr = [fileName: fileName, topic: topic.name]
            activityService.createActivityForTopic(topic, '', "Attachment '" + fileName + "' is removed", ActivityType.findByValue(ActivityTypeValue.AttachmentRemoved), topic.assignedTo,
                    userService.getUser(), attr)
        }
        redirect(action: "details", params: [id: params.id])
    }

    def listStrageties() {
        def strategies = SignalStrategy.list().collect { it.toDto() }
        respond strategies, [formats: ['json']]
    }

    def searchStrategyProducts() {
        def strategyId = Long.parseLong(params.id)
        def strategyProducts = SignalStrategy.get(strategyId)?.getProductNameList()
        JSONObject strategyProductsObj = new JSONObject()
        strategyProductsObj.productNames = strategyProducts
        render strategyProductsObj as JSON
    }

    def generateSignalReports() {
        def reportType = params.reportType
        Topic topic = Topic.get(params.topicId)
        if (reportType == Constants.SignalReportTypes.PEBER) {
            def user = userService.getUser()

            def summary = ""

            topic.comments.each {
                summary = summary + it.comments
            }

            def reportData = [
                    'dateDetected'            : DateUtil.toDateString(topic.dateCreated, user.preference.getTimeZone()),
                    'status'                  : topic.workflowState.value == 'Closed Signal' ? 'Closed' : 'Ongoing',
                    'dateClosed'              : topic.workflowState.value == 'Closed Signal' ? topic.lastUpdated : '-',
                    'signalSource'            : topic.initialDataSource ?: '-',
                    'signalSummary'           : summary.toString(),
                    'methodOfSignalEvaluation': 'Review meta analysis and available data',
                    'actionTaken'             : topic.actions ? topic.actions*.details.join(",") : '-'
            ]

            def aggAlertList = topic.aggregateAlerts

            def reportDataList = []

            def filledPt = []
            if (aggAlertList) {
                aggAlertList.each {
                    if (!filledPt.contains(it.pt)) {
                        def data = ["signalTerm": it.pt]
                        data = data + reportData
                        reportDataList.add(data)
                        filledPt.add(it.pt)
                    }
                }
            } else {
                def data = ["signalTerm": "No event Added"]
                data = data + reportData
                reportDataList.add(data)
            }

            params.reportingInterval = DateUtil.toDateString(topic.startDate, user.preference.getTimeZone()) +
                    " to " + DateUtil.toDateString(topic.endDate, user.preference.getTimeZone())

            def reportFile = dynamicReportService.createPeberSignalReport(new JRMapCollectionDataSource(reportDataList), params, false)
            response.addCookie(new Cookie((params.action).toLowerCase(), ""))
            renderReportOutputType(reportFile)

        }
    }

    def generateSignalAssessmentReport() {
        Topic topic = Topic.findById(Long.parseLong(params.topicId))
        def assessmentData = insertSingleAndAggregateCases(topic)
        def reportFile = dynamicReportService.createAssessmentReport(assessmentData, params)
        response.addCookie(new Cookie((params.action).toLowerCase(), ""))
        renderReportOutputType(reportFile)
    }

    def generateSignalSummaryReport() {

        Topic topic = Topic.findById(Long.parseLong(params.topicId))

        def topicListingData = []

        topic.aggregateAlerts?.each {
            def ebgmTrend = productEventHistoryService.getProductEventHistoryTrend(it.productName, it.pt)
            topicListingData.add([
                    "peName"           : it.productName + "-" + it.pt, "newCount": it.newSponCount, "totalCount": it.cumSponCount,
                    "newFatal"         : it.newFatalCount, "totalFatal": it.cumFatalCount, "sdr": "Yes",
                    "dme"              : "No", "ime": "Yes", "trend": ebgmTrend, "listed": "No",
                    "serious"          : "Yes", "children": "No", "pragenent": "No", "elderly": "No",
                    "specialMonitoring": "No", "priority": it.priority.displayName, "prrTrend": ebgmTrend, "ebgmTrend": ebgmTrend
            ])
        }

        def signalSummaryData = [
                'status'         : topic.workflowState.displayName,
                "disposition"    : topic.disposition.displayName,
                "noOfPec"        : topic.aggregateAlerts?.size(),
                "noOfCases"      : topic.singleCaseAlerts?.size(),
                "noOfAdhocAlerts": topic.adhocAlerts?.size(),
                "sdr"            : "Yes", "trend": "Positive", "dme": "Yes",
                "priority"       : topic.priority.displayName,
                "product"        : topic.products

        ]
        def assessmentData = insertSingleAndAggregateCases(topic)
        def reportFile = dynamicReportService.createSignalSummaryReport(new JRMapCollectionDataSource(topicListingData),
                signalSummaryData, assessmentData, params)
        response.addCookie(new Cookie((params.action).toLowerCase(), ""))
        renderReportOutputType(reportFile)
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
                return OracleTypes.CURSOR;
            }
        };
        def sql = new Sql(dataSource_pva)
        def map = [:]
        def procedure = "call P_SIGNAL_REPORTS(?)"

        sql.call("{${procedure}}", [CURSOR_PARAMETER]) { result ->
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

    /**
     * Send the output type (PDF/Excel/Word) to the browser which will save it to the user's local file system
     * @param reportFile
     * @return
     */
    private renderReportOutputType(File reportFile, String name = "TopicSummaryReport") {
        String reportName = name + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    def initiateChartDataGeneration() {

        def topic = Topic.findById(Long.parseLong(params.topicId))
        def signalChartName = params.chartName
        def singleCaseAlertList = topic.singleCaseAlerts
        def caseData = []
        singleCaseAlertList?.each { sca ->
            caseData.add(sca.caseNumber + "-" + sca.caseVersion)
        }

        if (caseData.size() > 0) {
            caseData = caseData.join(",")

            def response = topicService.scheduleChartReport(topic.name, caseData, signalChartName)
            if (response.status == 200) {
                sendResponse(200, response.result)
            } else {
                def errorMessage = message(code: "app.label.chart.error")
                sendResponse(500, errorMessage)
            }
        } else {
            def errorMessage = message(code: "app.label.case.data.error", args: ['Topic'])
            sendResponse(500, errorMessage)
        }
    }

    def getReportExecutionStatus() {
        def response = topicService.getReportExecutionStatus(params.id)
        if (response.result) {
            sendResponse(200, response.result)
        } else {
            sendResponse(500, response.status)
        }
    }

    def getChartData() {
        def response = topicService.getReportExecutionStatus(params.executedId)
        log.info("The response.status is :" + response.status)
        if (response.status == 200) {
            def reportStatus = response.result.executionStatus
            log.info("The reportStatus is :" + reportStatus)
            if (reportStatus == "COMPLETED") {
                def chartResponse = topicService.getChartData(params.executedId)
                log.info("The chartResponse.status is :" + chartResponse.status)
                if (chartResponse.status == 200) {
                    def topic = Topic.get(params.topicId)
                    def signalChart
                    def oldChart = SignalChart.findByChartNameAndTopic(params.chartName, topic)
                    if (!oldChart) {
                        signalChart = new SignalChart([
                                chartName: params.chartName,
                                execId   : params.executedId,
                                chartData: new JsonBuilder(chartResponse.result).toPrettyString(),
                                topic    : topic
                        ])
                        signalChart.save()
                    } else {
                        oldChart.chartData = new JsonBuilder(chartResponse.result).toPrettyString()
                        oldChart.save()
                    }

                    sendResponse(200, chartResponse.result)
                } else {
                    sendResponse(500, message(code: "app.label.chart.error"))
                }
            } else {
                sendResponse(500, message(code: "app.label.chart.error"))
            }
        } else {
            sendResponse(500, message(code: "app.label.chart.error"))
        }
    }

    def getCurrentChartData() {
        def topic = Topic.get(params.topicId)
        def chartName = params.chartName
        def signalChart = SignalChart.findByChartNameAndTopic(chartName, topic)
        def chartResult = []
        if (signalChart) {
            chartResult = [chartData: JSON.parse(signalChart.chartData), chartName: chartName]
            render chartResult as JSON
        } else {
            chartResult = [chartData: null, errorMessage: message(code: "app.label.chart.error"), chartName: chartName]
        }
        render chartResult as JSON
    }

    //Method to prepare the response.
    private sendResponse(stat, msg) {
        response.status = stat
        render(contentType: "application/json") {
            responseText = msg
            status = stat
        }
    }

    def showTrendAnalysis() { [] }

    def showProbabilityAnalysis() { [] }

    def handleIllegalArgumentException(IllegalArgumentException e) {
        render(new ResponseDTO(status: false, message: e.message) as JSON)
    }

    def fetchTopicNames(Long alertId, String caseNumber, String applicationName) {
        List<Topic> tl = Topic.createCriteria().list {
            if (applicationName == "Single Case Alert") {
                'singleCaseAlerts' {
                    eq('caseNumber', caseNumber)
                }
            }
            if (applicationName == "Aggregate Case Alert") {
                'aggregateAlerts' {
                    eq('id', alertId)
                }
            }
            if (applicationName == "Ad-Hoc Alert") {
                'adhocAlerts' {
                    eq('id', alertId)
                }
            }
        } as List<Topic>
        tl = Topic.list() - tl
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy")
        List topicData = tl.collect {
            [id: it.id, name: it.name, startDate: sdf.format(it.startDate), endDate: sdf.format(it.endDate), products: it.products]
        }
        render([topicData: topicData] as JSON)
    }

    def partnersList() {
        def partnetList = grailsApplication.config.partnersList
        def topicId = params.topicId
        def topic = Topic.findById(topicId)
        def product = topic.getProductNameList()

        def pList = []

        partnetList.each {
            if (product.contains(it.productName)) {
                pList.add(it)
            }
        }
        render pList as JSON
    }

    def graphReport(String dataSource, Topic topic) {
        def dateRange = params.dateRange as SignalAssessmentDateRangeEnum
        Integer groupingCode = dateRange.groupingCode

        List productSelection = JSON.parse(params.productSelection).collect { k, v -> v*.id }.flatten().unique()
        List eventSelection = JSON.parse(params.eventSelection).collect { k, v -> v*.id }.flatten().unique()
        String caseList
        String timeZone = userService.user.preference.timeZone

        switch (dateRange) {
            case SignalAssessmentDateRangeEnum.LAST_3_MONTH:
                dateRange = RelativeDateConverter.lastXMonths(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_6_MONTH:
                dateRange = RelativeDateConverter.lastXMonths(null, 6, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_3_YEAR:
                dateRange = RelativeDateConverter.lastXYears(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_5_YEAR:
                dateRange = RelativeDateConverter.lastXYears(null, 5, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break

            case SignalAssessmentDateRangeEnum.SIGNAL_DATA:
                caseList = validatedSignalChartService.mapCaseNumberFormatForProc(topic.singleCaseAlerts*.caseNumber as List<String>)
                dateRange = validatedSignalChartService.fetchDateRangeFromCaseAlerts(topic.singleCaseAlerts as List<SingleCaseAlert>)
                break
            case SignalAssessmentDateRangeEnum.LAST_1_YEAR:
            default:
                dateRange = RelativeDateConverter.lastXYears(null, 1, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
        }

        Map result = [:]
        result.put('age-grp-over-time-chart', validatedSignalChartService.fetchDataForDistributionByAgeOverTime(dateRange, productSelection, eventSelection, groupingCode, caseList))
        result.put('seriousness-over-time-chart', validatedSignalChartService.fetchDataForDistributionBySeriousnessOverTime(dateRange, productSelection, eventSelection, groupingCode, caseList))
        result.put('country-over-time-chart', validatedSignalChartService.fetchDataForDistributionByCountryOverTime(dateRange, productSelection, eventSelection, groupingCode, caseList))
        result.put('gender-over-time-chart', validatedSignalChartService.fetchDataForDistributionByGenderOverTime(dateRange, productSelection, eventSelection, groupingCode, caseList))
        result.put('outcome-over-time-chart', validatedSignalChartService.fetchDataForDistributionByOutcome(dateRange, productSelection, eventSelection, groupingCode, caseList))
        result.put('seriousness-count-pie-chart', validatedSignalChartService.fetchDataForDistributionBySourceOverTime(dateRange, productSelection, eventSelection, groupingCode, caseList))
        result.put('systemOrganClass', validatedSignalChartService.fetchDataForDistributionBySystemOrganClass(dateRange, productSelection, eventSelection, groupingCode, caseList))
        render result as JSON
    }


    def aggregateCaseAlertProductAndEventList() {
        def topic = Topic.get(Long.parseLong(params.id))
        def result = topic.aggregateAlerts

        Map aggList = ['productList': new HashSet(), 'eventList': new HashSet()]

        result.each {
            aggList['productList'] << ["name": it.productName, "id": it.productId]
            aggList['eventList'] << ["name": it.pt, "id": it.ptCode]
        }

        render aggList as JSON
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchSignalsList() {
        try {
            def alertType = params.alertType
            def alertId = params.alertId
            def alert
            def signalsMap = [signalsBoolean: false]
            if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
                alert = AggregateCaseAlert.findById(alertId.toInteger())
            } else if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
                alert = SingleCaseAlert.findById(alertId.toInteger())
            } else if (Constants.AlertConfigType.EVDAS_ALERT == alertType) {
                alert = EvdasAlert.findById(alertId.toInteger())
            } else {
                alert = AdHocAlert.findById(alertId.toInteger())
            }
            def existingSignals = topicService.getTopicsFromAlertObj(alert, alertType)
            if (existingSignals.size() > 1) {
                signalsMap.signalsBoolean = true

            }
            render signalsMap as JSON
        } catch (Exception ex) {
            ex.printStackTrace()
            render(status: BAD_REQUEST)
        }
    }
}
