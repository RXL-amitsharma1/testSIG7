package com.rxlogix

import com.rxlogix.config.Activity
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.ArchivedLiteratureAlert
import com.rxlogix.config.Configuration
import com.rxlogix.config.EmbaseLiteratureAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureActivity
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.dto.AlertTagDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.helper.AlertTagHelper
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.GlobalArticle
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.signal.PvsAlertTag
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.GroovyResultSetExtension
import groovy.sql.OutParameter
import groovy.sql.Sql
import oracle.jdbc.OracleTypes
import org.hibernate.SQLQuery
import org.hibernate.Session
import com.rxlogix.dto.CategoryDTO
import org.joda.time.DateTime
import org.springframework.transaction.TransactionDefinition

import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import org.springframework.transaction.annotation.Propagation

@Transactional
class PvsAlertTagService implements EventPublisher {
    def aggregateCaseAlertService
    def CRUDService
    def signalDataSourceService
    def sessionFactory
    def alertService
    def sqlGenerationService
    def activityService
    def userService
    def literatureActivityService
    def businessConfigurationService
    def caseHistoryService
    def productEventHistoryService
    def literatureHistoryService
    def cacheService
    def pvsGlobalTagService
    def dataSource
    def dataSource_pva
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def createActivity(ExecutedConfiguration executedConfiguration,
                       ActivityType type,
                       User loggedInUser,
                       String details,
                       String justification,
                       def attrs,
                       String product,
                       String event,
                       User assignedToUser,
                       String caseNumber, Group assignToGroup = null, String guestAttendeeEmail = null, String privateUserName = null) {

        if (executedConfiguration) {
            Activity activity = new Activity(
                    type: type,
                    details: details,
                    timestamp: DateTime.now(),
                    justification: justification,
                    attributes: (attrs as JSON).toString(),
                    suspectProduct: product,
                    eventName: event,
                    caseNumber: caseNumber,
                    privateUserName: privateUserName
            )
            if (loggedInUser) {
                activity.performedBy = loggedInUser
            }
            if (assignedToUser) {
                activity.assignedTo = assignedToUser
            } else if (assignToGroup)
                activity.assignedToGroup = assignToGroup
            else {
                activity.guestAttendeeEmail = guestAttendeeEmail
            }
            return activity
        }
    }


    def saveAlertTag(AlertTagDTO alertTag, User assignedToUser = null, Group assignedToGroup = null) {
        PvsAlertTag tag = new PvsAlertTag()
        if (tag.domain.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND)) {
            tag.domain = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        } else if (tag.domain.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND)) {
            tag.domain = Constants.AlertConfigType.SINGLE_CASE_ALERT
        }
        def activity
        String userName
        Map activityMap = [:]
        PvsAlertTag.withTransaction {
            tag = populateTagObject(alertTag, tag)
            String alertText = alertTag.alertLevel ? "(A)" : ""
            String privateUser = (tag.privateUser) ? "(P)" : ""
            String description = tag.subTagText ? "Category Added " + tag.tagText + privateUser + alertText + " and Sub-Category(ies) Added " + tag.subTagText : "Category Added " + tag.tagText + privateUser+ alertText
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.CategoryAdded)
            ActivityType activityTypeUpdated = ActivityType.findByValue(ActivityTypeValue.CategoryUpdated)
            User performedBy = User.findByUsername(tag.createdBy) ?: User.findByUsername("System")
            userName = performedBy.username
            String privateUserName = tag?.privateUser
            switch (alertTag.domain) {
                case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                    def singleCaseAlert

                    singleCaseAlert = SingleCaseAlert.get(tag.alertId) ?: ArchivedSingleCaseAlert.get(tag.alertId)

                    if (alertTag.isActivity) {
                        activity = createActivity(singleCaseAlert.executedAlertConfiguration, activityType,
                                performedBy, description, null,
                                ['For Case Number': singleCaseAlert.caseNumber], singleCaseAlert.productName, singleCaseAlert.pt, assignedToUser, singleCaseAlert.caseNumber,
                                assignedToGroup, null, privateUserName)

                        activityMap.put(singleCaseAlert.executedAlertConfiguration.id, activity)

                    }
                    break
                case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                    def aggregateCaseAlert
                    aggregateCaseAlert = AggregateCaseAlert.get(tag.alertId) ?: ArchivedAggregateCaseAlert.get(tag.alertId)
                    if (alertTag.isActivity) {
                        activity = createActivity(aggregateCaseAlert.executedAlertConfiguration, activityType,
                                performedBy, description,
                                null, ['For Aggregate Alert'], aggregateCaseAlert.productName, aggregateCaseAlert.pt
                                , assignedToUser, null, assignedToGroup, null, privateUserName)

                        activityMap.put(aggregateCaseAlert.executedAlertConfiguration.id, activity)
                    }else if (alertTag.isEdit) {
                        alertText = alertTag.alertLevel ? "(A)" : ""
                        description = tag.subTagText ? "Category Updated " + tag.tagText + privateUser + alertText+ " and Sub-Category(ies) Added " + tag.subTagText : "Category Updated " + tag.tagText + privateUser + alertText
                        activity = createActivity(aggregateCaseAlert.executedAlertConfiguration, activityTypeUpdated,
                                performedBy, description,
                                null, ['For Aggregate Alert'], aggregateCaseAlert.productName, aggregateCaseAlert.pt
                                , assignedToUser, null, assignedToGroup, null, privateUserName)

                        activityMap.put(aggregateCaseAlert.executedAlertConfiguration.id, activity)
                    }
                    break
                case Constants.AlertType.LITERATURE_ALERT:
                    def literatureAlert
                    literatureAlert = LiteratureAlert.get(tag.alertId) ?: ArchivedLiteratureAlert.get(tag.alertId)
                    if (alertTag.isActivity) {
                        activity = new LiteratureActivity(type: activityType, details: description, timestamp: DateTime.now(), justification: null,
                                productName: literatureAlert.productSelection, eventName: literatureAlert.eventSelection, articleId: literatureAlert.articleId, searchString: literatureAlert.searchString, privateUserName: privateUserName,
                                performedBy: performedBy, assignedTo: assignedToUser, assignedToGroup: assignedToGroup, guestAttendeeEmail: null
                        )

                        activityMap.put(literatureAlert.exLitSearchConfig.id, activity)
                    }
                    break
                case Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND:
                    break
                case Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND:
                    break
            }
            tag.save()
        }
        return [activityMap: activityMap, alertTagMap: [alertSpecific: true, type: 'I', id: tag.alertId, tagId: tag.id],performedBy: userName]
    }

    def deleteAlertTag(List alertTagsList, AlertTagDTO alertTag, User assignedToUser, Group assignedToGroup, String defaultDomain) {
        boolean allSubTagDeleted = true
        Long deleteTagCount = 0
        boolean isUpdated = false
        alertTagsList.each{ it ->
            if(it.tagText == alertTag.tagText){
                deleteTagCount = deleteTagCount+1
                if(it.dmlType!= Constants.DMLType.DELETE ){
                    allSubTagDeleted = false
                }
                if(it.dmlType == Constants.DMLType.INSERT){
                    isUpdated = true
                }
            }
        }
        PvsAlertTag tag = PvsAlertTag.findByTagTextAndSubTagTextAndAlertId(alertTag.tagText, alertTag.subTagText, alertTag.alertId as Long)
        List<PvsAlertTag> tagList = PvsAlertTag.findAllByTagTextAndAlertId(alertTag.tagText, alertTag.alertId as Long)
        boolean existExtraSubTag = false
        if(tag) {
            ActivityType activityType
            String description
            tagList.each{
                if(it?.subTagText!=tag?.subTagText){
                    existExtraSubTag = true
                }
            }
            if((existExtraSubTag && !(deleteTagCount == tagList.size() && allSubTagDeleted)) || isUpdated){
                description = tag?.subTagText ? "Category Updated " + tag?.tagText + " and Sub-Category(ies) Removed " + tag?.subTagText : "Category Updated " + tag?.tagText
                activityType = ActivityType.findByValue(ActivityTypeValue.CategoryUpdated)
            }else{
                description = tag?.subTagText ? "Category Removed " + tag?.tagText + " and Sub-Category(ies) Removed " + tag?.subTagText : "Category Removed " + tag?.tagText
                activityType = ActivityType.findByValue(ActivityTypeValue.CategoryRemoved)
            }
            User performedBy = User.findByUsername(alertTag.modifiedBy) ?: User.findByUsername("System")
            String privateUserName = tag?.privateUser
            def activity
            Map activityMap =[:]
            String userName = performedBy.username
            switch (tag?.domain) {
                case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                    def singleCaseAlert
                    singleCaseAlert = SingleCaseAlert.get(tag.alertId) ?: ArchivedSingleCaseAlert.get(tag.alertId)
                    if (alertTag.isActivity) {
                        activity = createActivity(singleCaseAlert.executedAlertConfiguration, activityType,
                                performedBy, description, null,
                                ['For Case Number': singleCaseAlert.caseNumber], singleCaseAlert.productName, singleCaseAlert.pt, assignedToUser, singleCaseAlert.caseNumber,
                                assignedToGroup, null, privateUserName)
                        activityMap.put(singleCaseAlert.executedAlertConfiguration.id, activity)
                    }
                    break
                case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                    def aggregateCaseAlert

                    aggregateCaseAlert = AggregateCaseAlert.get(tag.alertId) ?: ArchivedAggregateCaseAlert.get(tag.alertId)
                    if (alertTag.isActivity) {
                        activity = createActivity(aggregateCaseAlert.executedAlertConfiguration, activityType,
                                performedBy, description,
                                null, ['For Aggregate Alert'], aggregateCaseAlert.productName, aggregateCaseAlert.pt
                                , assignedToUser, null, assignedToGroup, null, privateUserName)

                        activityMap.put(aggregateCaseAlert.executedAlertConfiguration.id, activity)
                    }
                    break
                case Constants.AlertType.LITERATURE_ALERT:
                    def literatureAlert
                    literatureAlert = LiteratureAlert.get(tag.alertId) ?: ArchivedLiteratureAlert.get(tag.alertId)
                    if (alertTag.isActivity) {
                        activity = new LiteratureActivity(type: activityType, details: description, timestamp: DateTime.now(), justification: null,
                                productName: literatureAlert.productSelection, eventName: literatureAlert.eventSelection, articleId: literatureAlert.articleId, searchString: literatureAlert.searchString,
                                privateUserName: privateUserName, performedBy: performedBy, assignedTo: assignedToUser, assignedToGroup: assignedToGroup, guestAttendeeEmail: null
                        )

                        activityMap.put(literatureAlert.exLitSearchConfig.id, activity)
                    }
                    break
            }
            String privateUser = alertTag.privateUser ? User.get(alertTag.privateUser as Long)?.username : null
            List<PvsAlertTag> tags = PvsAlertTag.findAllByTagTextAndSubTagTextAndAlertIdAndPrivateUserAndDomain(alertTag.tagText, alertTag.subTagText, alertTag.alertId as Long, privateUser, defaultDomain)
            List<Long> deleteTagsId = []
            tags.each { aTag ->
                deleteTagsId.add(aTag.id)
            }
            return [activityMap: activityMap, alertTagMap: [alertSpecific: true, deleteTagsIds: deleteTagsId],performedBy: userName]
        }
    }

    void updateAlertTag(AlertTagDTO alertTag) {
        PvsAlertTag tag = PvsAlertTag.findByTagTextAndSubTagTextAndAlertId(alertTag.tagText, alertTag.subTagText , alertTag.alertId as Long)
        if(tag) {
            tag = populateTagObject(alertTag, tag)
            tag.save(flush: true)
        }
    }

    Long fetchSingleCaseAlertId(String tagId) {
        Long caseId = tagId.split('-')[1].toLong()
        Long caseSeriesId = tagId.split('-')[0].toLong()
        SingleCaseAlert singleCaseAlert = SingleCaseAlert.createCriteria().get {
            eq('caseId', caseId)
            'executedAlertConfiguration' {
                eq('pvrCaseSeriesId', caseSeriesId)
            }
        }
        return singleCaseAlert?.id
    }

    List<Map> getAllAlertSpecificTags(List<Long> alertIdList, String alertType) {
        List<Map> tags = []
        String userName = userService.getCurrentUserName()
        List<PvsAlertTag> pvsAlertTagList = []
        alertIdList.collate(Constants.AggregateAlertFields.BATCH_SIZE).each { batchIds ->
            List<PvsAlertTag> batchResult = PvsAlertTag.createCriteria().list {
                'eq'('domain', alertType)
                'or' {
                    batchIds.collate(1000).each {
                        'in'('alertId', it)
                    }
                }
            } as List<PvsAlertTag>
            pvsAlertTagList.addAll(batchResult)
            sessionFactory.currentSession.clear()
        }
        Map <StringBuilder , Integer> tagsAdded = [:]
        StringBuilder sBuilder = new StringBuilder()
        pvsAlertTagList.each { pvsAlertTag ->
            sBuilder.setLength(0)
            if (isAddTags(pvsAlertTag, userName, tags)) {
                sBuilder.append(pvsAlertTag.tagText).append('-').append(pvsAlertTag.subTagText).append('-').append(pvsAlertTag.alertId)
                if(!tagsAdded.containsKey(sBuilder.toString())) {
                    Integer index = tags.findIndexOf { it.alertId == pvsAlertTag.alertId && it.tagText == pvsAlertTag.tagText && pvsAlertTag.privateUser==it.privateUserName}
                    if (index != -1 && pvsAlertTag.subTagText != null) {
                        String subTags = tags.get(index).subTagText
                        tags.get(index).subTagText = subTags + ';' + pvsAlertTag.subTagText
                    } else {
                        tags.add(['tagText'        : pvsAlertTag.tagText, 'subTagText': pvsAlertTag.subTagText, 'privateUser': pvsAlertTag.privateUser ? '(P)' : '',
                                  'priority'       : pvsAlertTag.priority, 'tagType': '(A)', 'alertId': pvsAlertTag.alertId,
                                  'privateUserName': pvsAlertTag.privateUser])
                    }
                    tagsAdded.put(sBuilder.toString(), 1)

                }
            }
        }
        return tags
    }

    PvsAlertTag populateTagObject(AlertTagDTO alertTag, PvsAlertTag tag) {
        tag.tagId = alertTag.tagId?.toLong()
        tag.subTagId = alertTag.subTagId?.toLong()
        tag.tagText = alertTag.tagText
        tag.subTagText = alertTag.subTagText
        tag.alertId = alertTag.alertId?.toLong()
        tag.domain = alertTag.domain
        tag.createdBy = alertTag.createdBy
        tag.modifiedBy = alertTag.modifiedBy
        tag.createdAt = DateUtil.parseDate(alertTag.createdAt, DateUtil.DATEPICKER_UTC_FORMAT)
        tag.modifiedAt = DateUtil.parseDate(alertTag.modifiedAt, DateUtil.DATEPICKER_UTC_FORMAT)
        tag.privateUser = alertTag.privateUser ? User.get(alertTag.privateUser as Long)?.username : null
        tag.martId = alertTag.martId
        tag.isMasterCategory = alertTag.isMasterCategory
        tag.priority = alertTag.priority
        tag.autoTagged = alertTag.autoTagged
        tag.isRetained = alertTag.retained
        tag.execConfigId = alertTag.execConfigId
        return tag
    }

    void importSingleAlertTags(boolean isArchived = null) {
        String sql_statement = SignalQueryHelper.fetchAlertTags(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        Map existingTags = [:]
        StringBuilder sBuilder = new StringBuilder()
        sqlQuery.list().each { row ->
            sBuilder.append(row[0]).append('-').append(row[1]).append('-').append(row[2])
            existingTags.put(sBuilder.toString(), 1)
            sBuilder.setLength(0)
        }
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            int getType() {
                return OracleTypes.CURSOR
            }
        }
        def domain = isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        String statement = sqlGenerationService.getFetchGttStatement(Holders.config.category.singleCase.alertSpecific)
        List pvsAlertTagList = []
        sql.execute(statement)
        StringBuilder tagSubTagPair = new StringBuilder()
        sql.call("{call pkg_category.P_CAT_FACT_FETCH(?)}", [CURSOR_PARAMETER]) { result ->
            result.eachRow() { GroovyResultSetExtension resultRow ->
                Long execConfigId = resultRow.getProperty("ud_Number_1") ? resultRow.getProperty("ud_Number_1") : null
                if(execConfigId) {
                    List<Long> alertIdList = domain.createCriteria().list {
                        projections {
                            property("id")
                        }
                        eq("executedAlertConfiguration.id", execConfigId)
                        'executedAlertConfiguration' {
                            eq("pvrCaseSeriesId", resultRow.getProperty("fact_grp_col_2") as Long)
                        }
                        eq("caseId", resultRow.getProperty("fact_grp_col_3") as Long)

                    } as List<Long>
                    alertIdList.each { Long alertId ->
                        tagSubTagPair.append(resultRow.getProperty("cat_nm")).append("-").append(resultRow.getProperty("sub_cat_nm")).append('-').append(alertId)
                        if (alertId && !existingTags.containsKey(tagSubTagPair.toString())) {
                            PvsAlertTag pvsAlertTag = new PvsAlertTag()
                            pvsAlertTag.tagText = resultRow.getProperty("cat_nm")
                            pvsAlertTag.subTagText = resultRow.getProperty("sub_cat_nm")
                            pvsAlertTag.tagId = resultRow.getProperty("cat_id") as Long
                            pvsAlertTag.subTagId = resultRow.getProperty("sub_cat_id") as Long
                            pvsAlertTag.domain = Constants.AlertConfigType.SINGLE_CASE_ALERT
                            pvsAlertTag.createdAt = new Date()
                            pvsAlertTag.createdBy = resultRow.getProperty("created_by")
                            pvsAlertTag.modifiedAt = new Date()
                            pvsAlertTag.modifiedBy = resultRow.getProperty("updated_by")
                            Long privateUserId = resultRow.getProperty("private_user_id") as Long
                            if (privateUserId)
                                pvsAlertTag.privateUser = User.findById(privateUserId)?.username
                            else
                                pvsAlertTag.privateUser = null
                            pvsAlertTag.martId = resultRow.getProperty("id") as Long
                            pvsAlertTag.priority = resultRow.getProperty("priority") as Integer
                            pvsAlertTag.isMasterCategory = false
                            pvsAlertTag.alertId = alertId
                            pvsAlertTag.autoTagged = (resultRow.getProperty("is_auto_tagged") as Integer) > 0 ? true : false
                            pvsAlertTag.isRetained = (resultRow.getProperty("is_Retained") as Integer) > 0 ? true : false
                            pvsAlertTag.execConfigId = resultRow.getProperty("ud_number_1") as Long
                            pvsAlertTagList.add(pvsAlertTag)
                        }
                        tagSubTagPair.setLength(0)
                    }
                }
            }
        }
        List<Map> tagAlertMap = batchPersistAlertTags(pvsAlertTagList)
        session = sessionFactory.currentSession
        String insertValidatedQuery
        if (isArchived)
            insertValidatedQuery = "INSERT INTO ARCHIVED_SCA_TAGS(PVS_ALERT_TAG_ID,SINGLE_ALERT_ID) VALUES(?,?)"
        else
            insertValidatedQuery = "INSERT INTO SINGLE_CASE_ALERT_TAGS(PVS_ALERT_TAG_ID,SINGLE_ALERT_ID) VALUES(?,?)"
        alertService.batchPersistForMapping(session, tagAlertMap, insertValidatedQuery)

    }

    List<Map> batchPersistAlertTags(List<PvsAlertTag> tagsList) {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        List<Map> tagCaseMap = []
        PvsAlertTag.withTransaction {
            List batch = []
            tagsList.eachWithIndex { def pvsAlertTag, Integer index ->
                batch += pvsAlertTag
                pvsAlertTag.save(validate: false)
                tagCaseMap.add([col1: pvsAlertTag.id.toString(), col2: pvsAlertTag.alertId.toString()])
                if (index && index.mod(batchSize) == 0) {
                    Session session = sessionFactory.currentSession
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }
        return tagCaseMap
    }

    def saveCaseHistory(def singleCaseAlert , String createdBy , String modifiedBy , List<AlertTagDTO> alertTagsList) {
        Long existingHistoryId
        Map caseHistoryMap = businessConfigurationService.getCaseHistoryMap(singleCaseAlert, '-')
        caseHistoryMap.followUpNumber = caseHistoryMap.followUpNumber ?: Constants.Commons.ZERO
        //followUpNumber is null , Added for PVS-68670
        Map tagsMap = fetchTagsForHistory(singleCaseAlert , alertTagsList)
        caseHistoryMap['tagsUpdated'] = tagsMap.tagsUpdated.toString()
        caseHistoryMap['tagName'] = tagsMap.tagsUpdated.toString()
        caseHistoryMap['subTagName'] = tagsMap.subTagsUpdated.toString()
        Long configId = caseHistoryMap.configId
        String caseNumber = caseHistoryMap.caseNumber
        CaseHistory existingCaseHistory = caseHistoryService.getLatestCaseHistory(caseNumber, configId)
        CaseHistory caseHistory = new CaseHistory(caseHistoryMap)
        if (existingCaseHistory) {
            if (caseHistoryMap.change != Constants.Commons.BLANK_STRING) {
                caseHistory.properties = existingCaseHistory.properties
                caseHistory.configId = configId
                caseHistory.singleAlertId = caseHistoryMap.singleAlertId
                caseHistory.execConfigId = caseHistoryMap.execConfigId
                caseHistory.tagName = caseHistoryMap.tagName
                caseHistory.subTagName = caseHistoryMap.subTagName
                if (caseHistoryMap.followUpNumber || caseHistoryMap.followUpNumber == 0) {
                    caseHistory.followUpNumber = caseHistoryMap.followUpNumber
                }
            }
            existingHistoryId = existingCaseHistory.id
        }
        caseHistory.currentDisposition = singleCaseAlert.disposition
        caseHistory.tagsUpdated = caseHistoryMap.tagsUpdated

        caseHistory.justification = caseHistoryMap.justification
        caseHistory.change = Constants.HistoryType.ALERT_TAGS
        caseHistory.caseVersion = singleCaseAlert.caseVersion
        caseHistory.isLatest = true
        caseHistory.setLastUpdated(new Date())
        caseHistory.createdBy = User.findByUsername(createdBy)?.username?: User.findByUsername("System")?.username
        caseHistory.modifiedBy = modifiedBy ?: "System"
        if (!caseHistory.save(failOnError: true, flush: true)) {
            log.error("errors:" + caseHistory.errors)
            throw new ValidationException("Validation Exception", caseHistory.errors)
        }
        existingHistoryId
    }

    void saveProductEventHistory(aggregateCaseAlert , String createdBy , String modifiedBy , List<AlertTagDTO> alertTagsList) {
        Map peHistoryMap = [
                "justification"   : '',
                "change"          : Constants.HistoryType.ALERT_TAGS
        ]
        peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(aggregateCaseAlert,peHistoryMap)
        peHistoryMap.createdBy = User.findByUsername(createdBy)?.fullName ?: User.findByUsername("System")?.fullName
        peHistoryMap.modifiedBy = User.findByUsername(modifiedBy)?.fullName ?: User.findByUsername("System")?.fullName
        Map tagsMap = fetchTagsForHistory(aggregateCaseAlert , alertTagsList)
        peHistoryMap['tagName'] = tagsMap.tagsUpdated.toString()
        peHistoryMap['subTagName'] = tagsMap.subTagsUpdated.toString()
        peHistoryMap['tagsUpdated'] = tagsMap.tagsUpdated.toString()
        List peHistoryList = [peHistoryMap]
        productEventHistoryService.batchPersistHistory(peHistoryList)
    }

    List checkIfActivityPossible(List <AlertTagDTO> tagsList) {
        tagsList.each{tag->
            if(tag.dmlType == "I") {
                tagsList.each{
                    if(tag.tagText == it.tagText && tag.subTagText == it.subTagText && it.dmlType == "D") {
                        tag.isActivity = false
                        it.isActivity = false
                        tag.isEdit = true
                    }else if(tag.tagText == it.tagText && tag.subTagText == it.subTagText && (tag.alertLevel!=it.alertLevel||tag.privateUser!=it.privateUser)){
                        tag.isActivity = false
                        it.isActivity = false
                        tag.isEdit = true
                    }
                }
            }
        }

        return tagsList
    }

    void saveliteratureHistory(def literatureAlert , String createdBy , String modifiedBy , List<AlertTagDTO> alertTagsList) {
        Map literatureHistoryMap = literatureHistoryService.getLiteratureHistoryMap(null , literatureAlert , Constants.HistoryType.ALERT_TAGS , "")
        Map tagsMap = fetchTagsForHistory(literatureAlert , alertTagsList)
        String change = literatureHistoryMap.change
        LiteratureHistory existingLiteratureHistory = literatureHistoryService.getLatestLiteratureHistory(literatureHistoryMap.articleId, literatureHistoryMap.litExecConfigId)
        LiteratureHistory literatureHistory = new LiteratureHistory(literatureHistoryMap)
        if (existingLiteratureHistory) {
            literatureHistory.tagName =
            literatureHistory.change = change
            existingLiteratureHistory.isLatest = false
            if (!existingLiteratureHistory.save(failOnError: true, flush: true)) {
                log.error("errors:" + existingLiteratureHistory.errors)
                throw new ValidationException("Validation Exception", existingLiteratureHistory.errors)
            }

        }
        literatureHistory.justification = ""
        literatureHistory.isLatest = true
        literatureHistory.setLastUpdated(new Date())
        literatureHistory.createdBy = User.findByUsername(createdBy)?.username ?: User.findByUsername("System")?.username
        literatureHistory.modifiedBy = User.findByUsername(modifiedBy)?.username ?: User.findByUsername("System")?.username
        literatureHistory.tagName = tagsMap.tagsUpdated.toString()
        literatureHistory.subTagName = tagsMap.subTagsUpdated.toString()
        literatureHistory.tagsUpdated = tagsMap.tagsUpdated.toString()
        if (!literatureHistory.save(failOnError: true, flush: true)) {
            log.error("errors:" + literatureHistory.errors)
            throw new ValidationException("Validation Exception", literatureHistory.errors)
        }
    }

    Map fetchTagsForHistory(def alert , List alertTagsList) {
        Set tagList = []
        Set subTagList = []
        List tagSubtagPair = []
        alert?.pvsAlertTag?.each { tagName ->
            if(!tagSubtagPair.contains(tagName.tagText + '-' + tagName.subTagText)) {
                if (tagName.privateUser) {
                    tagList.add(["name": tagName.tagText, "type": Constants.Commons.PRIVATE_TAG_ALERT, "privateUser": tagName.privateUser] as JSON)
                    tagName.subTagText ? subTagList.add(["name": tagName.subTagText, "type": Constants.Commons.PRIVATE_TAG_ALERT, "privateUser": tagName.privateUser] as JSON) : null
                } else {
                    tagList.add(["name": tagName.tagText, "type": Constants.Commons.CASE_SERIES_TAG] as JSON)
                    tagName.subTagText ? subTagList.add(["name": tagName.subTagText, "type": Constants.Commons.CASE_SERIES_TAG] as JSON) : null
                }
                tagSubtagPair.add(tagName.tagText + '-' + tagName.subTagText)
            }
        }
        tagSubtagPair = []
        alert?.globalIdentity?.pvsGlobalTag.each { tagName ->
            if(!tagSubtagPair.contains(tagName.tagText + '-' + tagName.subTagText)) {
                if (tagName.privateUser) {
                    tagList.add(["name": tagName.tagText, "type": Constants.Commons.PRIVATE_TAG_GLOBAL, "privateUser": tagName.privateUser] as JSON)
                    tagName.subTagText ? subTagList.add(["name": tagName.subTagText, "type": Constants.Commons.PRIVATE_TAG_GLOBAL, "privateUser": tagName.privateUser] as JSON) : null
                } else {
                    tagList.add(["name": tagName.tagText, "type": Constants.Commons.GLOBAL_TAG] as JSON)
                    tagName.subTagText ? subTagList.add(["name": tagName.subTagText, "type": Constants.Commons.GLOBAL_TAG] as JSON) : null
                }
            }
            tagSubtagPair.add(tagName.tagText + '-' + tagName.subTagText)
        }
        Set tagsUpdated = []
        Set subTagsUpdated = []
        List updatedTagSubTagPair = []
        alertTagsList.each{alertTag->
            if(alertTag.alertLevel){
                if (alertTag.alertId == alert.id.toString() && !updatedTagSubTagPair.contains(alertTag.tagText + '-' + alertTag.subTagText + '-' + alertTag.alertLevel)) {
                    String stateOfCRUD = alertTag.dmlType == "D" ? "Deleted: " : ""
                    String privateUserName = alertTag.privateUser ? User.get(alertTag.privateUser as Long)?.username : null
                    if (privateUserName) {
                        tagsUpdated.add(["name": stateOfCRUD + alertTag.tagText, "type": Constants.Commons.PRIVATE_TAG_ALERT, "privateUser": privateUserName] as JSON)
                        alertTag.subTagText ? subTagsUpdated.add(["name": stateOfCRUD + alertTag.subTagText, "type": Constants.Commons.PRIVATE_TAG_ALERT, "privateUser": privateUserName] as JSON) : null
                    } else {
                        tagsUpdated.add(["name": stateOfCRUD + alertTag.tagText, "type": Constants.Commons.CASE_SERIES_TAG] as JSON)
                        alertTag.subTagText ? subTagsUpdated.add(["name": stateOfCRUD + alertTag.subTagText, "type": Constants.Commons.CASE_SERIES_TAG] as JSON) : null
                    }
                    // Pair of added tags and subtags to remove duplicates
                    updatedTagSubTagPair.add(alertTag.tagText + '-' + alertTag.subTagText + '-' + alertTag.alertLevel)
                }
            }
            else {
                if (alertTag.alertId == alert.id.toString() && !updatedTagSubTagPair.contains(alertTag.tagText + '-' + alertTag.subTagText + '-' + alertTag.alertLevel)) {                    String stateOfCRUD = alertTag.dmlType == "D" ? "Deleted: " : ""
                    String privateUserName = alertTag.privateUser ? User.get(alertTag.privateUser as Long)?.username : null
                    if (privateUserName) {
                        tagsUpdated.add(["name": stateOfCRUD + alertTag.tagText, "type": Constants.Commons.PRIVATE_TAG_GLOBAL, "privateUser": privateUserName] as JSON)
                        alertTag.subTagText ? subTagsUpdated.add(["name": stateOfCRUD + alertTag.subTagText, "type": Constants.Commons.PRIVATE_TAG_GLOBAL, "privateUser": privateUserName] as JSON) : null
                    } else {
                        tagsUpdated.add(["name": stateOfCRUD + alertTag.tagText, "type": Constants.Commons.GLOBAL_TAG] as JSON)
                        alertTag.subTagText ? subTagsUpdated.add(["name": stateOfCRUD + alertTag.subTagText, "type": Constants.Commons.GLOBAL_TAG] as JSON) : null
                    }
                    // Pair of added tags and subtags to remove duplicates
                    updatedTagSubTagPair.add(alertTag.tagText + '-' + alertTag.subTagText + '-' + alertTag.alertLevel)
                }
            }
        }
        tagList.unique{
            JSON.parse(it.toString())
        }
        return [tagList : tagList , subTagList : subTagList , tagsUpdated : tagsUpdated, subTagsUpdated:subTagsUpdated]
    }

    ResponseDTO persistCategories(AlertTagHelper listAlertTags, ResponseDTO responseDTO) {
        try {
            List<AlertTagDTO> alertTagsList = listAlertTags.alertTags
            List<AlertTagDTO> tagsUpdated = []
            alertTagsList = checkIfActivityPossible(alertTagsList)
            Set alertId = []
            String defaultDomain = alertTagsList[0].domain
            Long startTime2 = System.currentTimeMillis()
            Boolean isAggArchived = false
            Boolean isEmbaseAlert = false
            AggregateCaseAlert.withTransaction([propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW]) {
                ExecutorService executor = Executors.newFixedThreadPool(20)
                List<Future<?>> futures = new ArrayList<>()
                alertTagsList.each { alertTag ->

                    def domain
                    switch (alertTag.domain) {
                        case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                            domain = SingleCaseAlert
                            break
                        case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                            domain = AggregateCaseAlert
                            break
                        case Constants.AlertType.LITERATURE_ALERT:
                            domain = LiteratureAlert
                            break
                    }
                    def alert = domain?.get(alertTag.alertId)
                    if (domain == AggregateCaseAlert && !alert) {
                        alert = ArchivedAggregateCaseAlert.get(alertTag.alertId)
                        isAggArchived = true
                    }
                    if (domain == LiteratureAlert && !alert) {
                        alert = EmbaseLiteratureAlert.get(alertTag.alertId)
                        isEmbaseAlert = true
                    }
                    User user = alert?.assignedTo
                    Group assignedToGroup = alert?.assignedToGroup
                    Future<?> future = executor.submit({ ->
                        Boolean alertTagAdd = true
                       def dataMap
                        switch (alertTag.dmlType) {
                            case Constants.DMLType.INSERT:
                                dataMap = alertTag.alertLevel == true ? saveAlertTag(alertTag, user, assignedToGroup) : pvsGlobalTagService.saveGlobalTag(alertTag ,user, assignedToGroup)
                                break
                            case Constants.DMLType.DELETE:
                                dataMap = alertTag.alertLevel == true ? deleteAlertTag(alertTagsList,alertTag, user, assignedToGroup,defaultDomain) : pvsGlobalTagService.deleteGlobalTag(alertTagsList,alertTag, user, assignedToGroup)
                                break
                            case Constants.DMLType.UPDATE:
                                alertTag.alertLevel == true ? updateAlertTag(alertTag) : pvsGlobalTagService.updateGlobalTag(alertTag)
                                alertTagAdd = false
                                break
                        }
                alertId.add(alertTag.alertId as Long)
                        [alertId: alertTag.alertId, tagsUpdated: alertTagAdd ? alertTag : [], activityMap: dataMap?.activityMap, alertTagIdMap: dataMap?.alertTagMap, performedBy: dataMap?.performedBy]
                    } as Callable)
                    futures.add(future)
                }
                executor.shutdown()
                def catDataMap = []
                    for (Future<?> future : futures) {
                        catDataMap.add(future.get())
                    }

                Set s1 =  catDataMap['activityMap']*.keySet().flatten() as Set
                Set performedBySet = catDataMap['performedBy'] as Set
                performedBySet.remove(null)
                s1.remove(null)
                def domain = alertTagsList[0].domain == "Literature Alert" ? ExecutedLiteratureConfiguration : ExecutedConfiguration
                int performedBySetSize = performedBySet.size()
                User currentUser = User.findByUsername(performedBySetSize > 1 ? performedBySet[performedBySetSize - 1] : performedBySet[0])
                ActivityType addedType = ActivityType.findByValue(ActivityTypeValue.CategoryAdded)
                ActivityType catRemoved = ActivityType.findByValue(ActivityTypeValue.CategoryRemoved)
                ActivityType catUpdated = ActivityType.findByValue(ActivityTypeValue.CategoryUpdated)
                ActivityType type
                if (s1.size() == 1 && s1[0]) {
                    def exConfig = domain.get(s1[0])
                    catDataMap['activityMap']*.values().each {
                        Set activities = []
                        if (it && it!=[]) {
                            if (!activities.contains(it[0])) {
                                def activity = it[0]
                                if (activity.type.toString() == 'CategoryAdded') {
                                    type = addedType
                                } else if (activity.type.toString() == 'CategoryRemoved') {
                                    type = catRemoved
                                } else if (activity.type.toString() == 'CategoryUpdated') {
                                    type = catUpdated
                                }
                                activity.performedBy = currentUser
                                activity.type = type
                                alertTagsList[0].domain == "Literature Alert" ? exConfig.addToLiteratureActivity(activity) : exConfig.addToActivities(activity)
                                activity.save(flush:true) //Added this for maintaining order in activities tab
                                activities.add(activity)
                            }
                        }
                    }
                    exConfig.save(validate: false)
                } else if (s1.size() > 1) {
                    //
                    catDataMap['activityMap'].each { //each within each prob
                        if (it && it!=[]){
                            it.each { key,value ->
                                def exConfig = domain.get(key)
                                def activity = value
                                if (activity.type.toString() == 'CategoryAdded') {
                                    type = addedType
                                } else if (activity.type.toString() == 'CategoryRemoved') {
                                    type = catRemoved
                                } else if (activity.type.toString() == 'CategoryUpdated') {
                                    type = catUpdated
                                }
                                activity.performedBy = currentUser
                                activity.type = type
                                alertTagsList[0].domain == "Literature Alert" ? exConfig.addToLiteratureActivity(activity) : exConfig.addToActivities(activity)
                                activity.save(flush:true) //Added this for maintaining order in activities tab
                                exConfig.save(validate: false)
                            }
                        }
                    }
                    //
                }
                String alertTableMapTable
                String globalTagMapTable
                String alertIdCol
                String globalIdCol
                switch (defaultDomain){
                    case "Single Case Alert":
                        alertTableMapTable = 'SINGLE_CASE_ALERT_TAGS'
                        globalTagMapTable = 'SINGLE_GLOBAL_TAGS'
                        alertIdCol = 'SINGLE_ALERT_ID'
                        globalIdCol = 'GLOBAL_CASE_ID'
                        break
                    case "Aggregate Case Alert":
                        alertTableMapTable = 'AGG_CASE_ALERT_TAGS'
                        if (isAggArchived)
                            alertTableMapTable = 'ARCHIVED_AGG_CASE_ALERT_TAGS'
                        globalTagMapTable = 'AGG_GLOBAL_TAGS'
                        alertIdCol = 'AGG_ALERT_ID'
                        globalIdCol = 'GLOBAL_PRODUCT_EVENT_ID'
                        break
                    case "Literature Alert":
                        if(isEmbaseAlert) {
                            alertTableMapTable = 'EMBASE_LITERATURE_CASE_ALERT_TAGS'
                            alertIdCol = 'EMBASE_LITERATURE_ALERT_ID'
                        } else {
                            alertTableMapTable = 'LITERATURE_CASE_ALERT_TAGS'
                            alertIdCol = 'LITERATURE_ALERT_ID'
                        }
                        globalTagMapTable = 'LITERAURE_GLOBAL_TAGS'
                        globalIdCol = 'GLOBAL_ARTICLE_ID'
                        break
                    case "Single Case Alert on Demand":
                        alertTableMapTable = 'SINGLE_DEMAND_ALERT_TAGS'
                        globalTagMapTable = 'SINGLE_GLOBAL_TAGS'
                        alertIdCol = 'SINGLE_ALERT_ID'
                        globalIdCol = 'GLOBAL_CASE_ID'
                        break
                    case "Aggregate Case Alert on Demand":
                        alertTableMapTable = 'AGG_DEMAND_ALERT_TAGS'
                        globalTagMapTable = 'AGG_GLOBAL_TAGS'
                        alertIdCol = 'AGG_ALERT_ID'
                        globalIdCol = 'GLOBAL_PRODUCT_EVENT_ID'
                        break
                }

                Boolean runInsertQuery = false
                Boolean runAlertDeleteQuery = false
                Boolean runDeleteQuery = false
                String alertQuery = ''
                String globalQuery = ''
                List<Long> deleteTagIds = []
                List<Long> deleteGlobalTagIds = []

                catDataMap['alertTagIdMap'].each {
                    if (it && it.type == 'I') {
                        if (it.alertSpecific) {
                            alertQuery += "($it.id, $it.tagId), "
                        } else {
                            globalQuery += "($it.globalId, $it.globalTagId, CURRENT_TIMESTAMP), "
                        }
                        runInsertQuery = true
                    } else if (it) {
                        if (it.alertSpecific) {
                            runAlertDeleteQuery = true
                            deleteTagIds += it.deleteTagsIds
                        } else {
                            runDeleteQuery = true
                            deleteGlobalTagIds += it.deleteTagsIds
                        }
                    }
                }

                Sql sql = new Sql(dataSource)

                if (runInsertQuery) {
                    if (alertQuery) {
                        alertQuery = alertQuery.substring(0, alertQuery.length() - 2) // Remove trailing comma and space
                        String finalAlertQuery = "INSERT INTO $alertTableMapTable ($alertIdCol, PVS_ALERT_TAG_ID) VALUES $alertQuery"
                        sql.execute(finalAlertQuery)
                    }

                    if (globalQuery) {
                        globalQuery = globalQuery.substring(0, globalQuery.length() - 2) // Remove trailing comma and space
                        String finalGlobalQuery = "INSERT INTO $globalTagMapTable ($globalIdCol, PVS_GLOBAL_TAG_ID, CREATION_DATE) VALUES $globalQuery"
                        sql.execute(finalGlobalQuery)
                    }
                }

                if (runAlertDeleteQuery) {
                    String deleteMappingQuery = 'DELETE FROM ' + alertTableMapTable + ' WHERE  '
                    String deleteQuery = 'DELETE FROM PVS_ALERT_TAG WHERE  '
                    deleteTagIds.collate(999).each {
                        String listAsString = it.join(',')
                        deleteMappingQuery = deleteMappingQuery + ' PVS_ALERT_TAG_ID IN (' + listAsString + ') OR'
                        deleteQuery = deleteQuery + 'ID IN (' + listAsString + ') OR'
                    }
                    sql.execute(deleteMappingQuery.substring(0, deleteMappingQuery.length() - 2))
                    sql.execute(deleteQuery.substring(0, deleteQuery.length() - 2))
                }

                if (runDeleteQuery) {
                    String deleteMappingQuery = 'DELETE FROM ' + globalTagMapTable + ' WHERE  '
                    String deleteQuery = 'DELETE FROM PVS_GLOBAL_TAG WHERE  '
                    deleteGlobalTagIds.collate(999).each {
                        String listAsString = it.join(',')
                        deleteMappingQuery = deleteMappingQuery + ' PVS_GLOBAL_TAG_ID IN (' + listAsString + ') OR'
                        deleteQuery = deleteQuery + 'ID IN (' + listAsString + ') OR'
                    }
                    log.info(deleteMappingQuery.substring(0, deleteMappingQuery.length() - 2))
                    log.info(deleteQuery.substring(0, deleteQuery.length() - 2))
                    sql.execute(deleteMappingQuery.substring(0, deleteMappingQuery.length() - 2))
                    sql.execute(deleteQuery.substring(0, deleteQuery.length() - 2))
                }

                tagsUpdated = catDataMap['tagsUpdated']
                sql.close()


            }
            Long endTime2 = System.currentTimeMillis()
            log.info('time taken to save  cat in PVS:' + (endTime2 - startTime2))

            Long startTime = System.currentTimeMillis()

            saveHistory(alertTagsList[0], alertId, tagsUpdated.flatten(), defaultDomain)
            cacheService.prepareCommonTagCache()
            Long endTime = System.currentTimeMillis()
            log.info('time taken to save history for cat:' + (endTime - startTime))
        } catch (Exception e) {
            log.error("Categories Persisting Failed ", e)
            responseDTO.status = false
        }
        responseDTO
    }

    private void saveHistory(AlertTagDTO alertTag, alertId, List<AlertTagDTO> tagsUpdated, String defaultDomain = null) {

        switch (alertTag.domain) {
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                def domain = SingleCaseAlert.get(alertTag?.alertId) ? SingleCaseAlert : ArchivedSingleCaseAlert
                List<Long> existingHistoryIds = []
                log.info('save History called')
                domain.withTransaction {
                    ExecutorService executor = Executors.newFixedThreadPool(20)
                    List<Future<?>> futures = new ArrayList<>()
                    alertId.each {

                        Future<?> future = executor.submit({ ->
                            Long existingHistoryId
                            String idx = it
                            SingleCaseAlert.withNewSession {
                                def singleCaseAlert = domain.findById(idx?.toLong())
                                log.info(' domain: ' + defaultDomain + ', tagDomain: '+ alertTag.domain)
                                if (singleCaseAlert && defaultDomain == alertTag.domain) {
                                    log.info('Single Case Alert: '+ singleCaseAlert?.id )
                                    existingHistoryId = saveCaseHistory(singleCaseAlert, tagsUpdated.size() > 0 ? tagsUpdated[0].modifiedBy : alertTag.modifiedBy, tagsUpdated.size() > 0 ? tagsUpdated[0].modifiedBy : alertTag.modifiedBy, tagsUpdated)
                                    notify 'propagate.categories.versions.published', [caseId: singleCaseAlert?.caseId?.toString(), caseVersion: singleCaseAlert.caseVersion]
                                } else if (defaultDomain != alertTag.domain) {
                                    SingleOnDemandAlert alert = SingleOnDemandAlert.get(idx?.toLong())
                                    log.info('Single on demand: '+ alert?.id)

                                    notify 'propagate.categories.versions.published', [caseId: alert?.caseId?.toString(), caseVersion: alert?.caseVersion]
                                }
                            }

                            existingHistoryId
                        } as Callable)
                        futures.add(future)
                    }
                    executor.shutdown()

                    for (Future<?> future : futures) {
                        existingHistoryIds.add(future.get())
                    }

                    existingHistoryIds.unique()
                    existingHistoryIds.remove(null)
                    if (existingHistoryIds) {
                        CaseHistory.executeUpdate("UPDATE CaseHistory SET IS_LATEST=false WHERE IS_LATEST=true AND ID IN (:IdList) ", [IdList: existingHistoryIds])
                    }
                }
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                def domain = AggregateCaseAlert.get(alertTag?.alertId) ? AggregateCaseAlert : ArchivedAggregateCaseAlert
                domain.withTransaction {
                    ExecutorService executor = Executors.newFixedThreadPool(20)
                    List<Future<?>> futures = new ArrayList<>()
                    alertId.each {
                        Future<?> future = executor.submit({ ->
                            Long existingHistoryId
                            String idx = it
                            SingleCaseAlert.withNewSession{
                                def aggregateCaseAlert = domain.get(idx)
                                if (aggregateCaseAlert) {
                                    saveProductEventHistory(aggregateCaseAlert, tagsUpdated.size() > 0 ? tagsUpdated[0].modifiedBy : alertTag.modifiedBy, tagsUpdated.size() > 0 ? tagsUpdated[0].modifiedBy : alertTag.modifiedBy, tagsUpdated)
                                }
                            }

                            existingHistoryId } as Callable)

                    }
                    executor.shutdown()
                    for (Future<?> future : futures) {
                        future.get()
                    }
                }
                break
            case Constants.AlertType.LITERATURE_ALERT:
                def domain = LiteratureAlert.get(alertTag?.alertId) ? LiteratureAlert : ArchivedLiteratureAlert
                domain.withTransaction {
                    ExecutorService executor = Executors.newFixedThreadPool(20)
                    List<Future<?>> futures = new ArrayList<>()
                    alertId.each {
                        Future<?> future = executor.submit({ ->
                            Long existingHistoryId
                            String idx = it
                            SingleCaseAlert.withNewSession{
                                def literatureAlert = domain.get(idx)
                        if (literatureAlert)
                            saveliteratureHistory(literatureAlert, tagsUpdated.size() > 0 ? tagsUpdated[0].modifiedBy : alertTag.modifiedBy, tagsUpdated.size() > 0 ? tagsUpdated[0].modifiedBy : alertTag.modifiedBy, tagsUpdated)

                            }

                            existingHistoryId } as Callable)
                    }
                    executor.shutdown()
                        for (Future<?> future : futures) {
                            future.get()
                        }
                }
                break
            case Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND:
                def domain = SingleOnDemandAlert
                log.info('Sync single on demand category method called ')
                domain.withTransaction {
                    ExecutorService executor = Executors.newFixedThreadPool(20)
                    List<Future<?>> futures = new ArrayList<>()
                    alertId.each {
                        Future<?> future = executor.submit({ ->
                            String idx = it
                            SingleOnDemandAlert.withNewSession {
                                SingleOnDemandAlert alert = SingleOnDemandAlert.get(idx?.toLong())
                                log.info('Single on demand: ' + alert?.id)
                                if (alert) {
                                    notify 'propagate.categories.versions.published', [caseId: alert?.caseId?.toString(), caseVersion: alert?.caseVersion]
                                }
                            }
                        } as Callable)
                        futures.add(future)
                    }
                    executor.shutdown()
                    for (Future<?> future : futures) {
                        future.get()
                    }
                }
                break
        }
    }

    void populateAlertTagsForVersions(List<CategoryDTO> alertCategories,Long exConfigId) {
        log.info("Alert Categories Sync Started")
        List<Map> categories = alertCategories.collect{ it ->
            [alertId:  it.alertId, factGrpCol3:it.factGrpCol3, factGrpCol4:it.factGrpCol4]
        }.unique{it.alertId}
        List <CategoryDTO> newCategories = []
        List <com.rxlogix.CategoryDTO> newCategoriesMart = []
        categories.each{
          Integer version = it.factGrpCol4 as Integer
          Long caseId = it.factGrpCol3 as Long
            def singleCaseAlert =  SingleCaseAlert.get(it.alertId) ?: ArchivedSingleCaseAlert.get(it.alertId) ?: SingleOnDemandAlert.get(it.alertId)
            Configuration configuration = singleCaseAlert.alertConfiguration
            List<ExecutedConfiguration> executedConfigurations = ExecutedConfiguration.findAllByConfigId(configuration?.id)
            List  archivedSingleCaseAlertList =  ArchivedSingleCaseAlert.createCriteria().list{
                'in'('executedAlertConfiguration' , executedConfigurations)
                ge('caseVersion' , version)
                ne('executedAlertConfiguration' , singleCaseAlert.executedAlertConfiguration)
                eq('caseId' , caseId)
            }

            for (CategoryDTO category in alertCategories){
                if(category.alertId == singleCaseAlert.id.toString()){
                    archivedSingleCaseAlertList.each { def archivedAlert->
                        CategoryDTO categoryDTO = category.clone()
                        categoryDTO.alertId = archivedAlert.id
                        categoryDTO.factGrpCol2 = archivedAlert.executedAlertConfiguration.pvrCaseSeriesId
                        categoryDTO.factGrpCol3 = archivedAlert.caseId
                        categoryDTO.factGrpCol4 = archivedAlert.caseVersion
                        newCategories.add(categoryDTO)
                        newCategoriesMart.add(fetchMartDto(categoryDTO))
                    }
                }
            }
        }
        CategoryUtil.saveCategories(newCategoriesMart)
        cacheService.prepareCommonTagCache()
        savePvsAlertTag(newCategories,exConfigId)
        log.info("Alert Categories Sync Ended")
    }

    void savePvsAlertTag(List<CategoryDTO> alertCategories,Long exConfigId) {
        alertCategories.each { alertTag ->
            try {
                saveAlertTagVersions(alertTag,exConfigId)
            } catch(Exception e) {
                log.error(e.getMessage(), e)
            }
        }
    }

    void saveAlertTagVersions(CategoryDTO tagDTO,Long exConfigId) {
        try{
            PvsAlertTag.withTransaction {
                PvsAlertTag pvsAlertTag = new PvsAlertTag()
                pvsAlertTag.tagId = tagDTO.catId
                pvsAlertTag.subTagId = tagDTO.subCatId
                pvsAlertTag.tagText = tagDTO.catName
                pvsAlertTag.subTagText = tagDTO.subCatName
                pvsAlertTag.alertId = tagDTO.alertId?.toLong()
                pvsAlertTag.domain = Constants.AlertConfigType.SINGLE_CASE_ALERT
                pvsAlertTag.createdBy = tagDTO.createdBy
                pvsAlertTag.modifiedBy = tagDTO.updatedBy
                pvsAlertTag.createdAt = DateUtil.parseDate(tagDTO.createdDate, DateUtil.DATEPICKER_UTC_FORMAT)
                pvsAlertTag.modifiedAt = DateUtil.parseDate(tagDTO.updatedDate, DateUtil.DATEPICKER_UTC_FORMAT)
                pvsAlertTag.privateUser = tagDTO.privateUserId ? User.get(tagDTO.privateUserId)?.username : null
                pvsAlertTag.martId = tagDTO.martId
                pvsAlertTag.priority = tagDTO.priority
                pvsAlertTag.execConfigId=exConfigId
                def singleCaseAlert
                singleCaseAlert = SingleCaseAlert.get(pvsAlertTag.alertId) ?: ArchivedSingleCaseAlert.get(pvsAlertTag.alertId)
                switch (tagDTO.dmlType) {
                    case Constants.DMLType.INSERT:
                        singleCaseAlert.addToPvsAlertTag(pvsAlertTag)
                        break
                    case Constants.DMLType.DELETE:
                        List<PvsAlertTag> tags = PvsAlertTag.findAllByTagTextAndSubTagTextAndAlertIdAndPrivateUser(pvsAlertTag.tagText, pvsAlertTag.subTagText, pvsAlertTag.alertId as Long, pvsAlertTag.privateUser)
                        tags.each { aTag ->
                            singleCaseAlert.removeFromPvsAlertTag(aTag)
                            aTag?.delete(flush: true)
                        }
                        break
                    case Constants.DMLType.UPDATE:
                        List<PvsAlertTag> tags = PvsAlertTag.findAllByTagTextAndSubTagTextAndAlertId(pvsAlertTag.tagText, pvsAlertTag.subTagText, pvsAlertTag.alertId as Long)
                        tags.each { updateTag ->
                            updateTag.createdBy = pvsAlertTag.createdBy
                            updateTag.modifiedBy = pvsAlertTag.modifiedBy
                            updateTag.createdAt = pvsAlertTag.createdAt
                            updateTag.modifiedAt = pvsAlertTag.modifiedAt
                            updateTag.privateUser = pvsAlertTag.privateUser
                            updateTag.isMasterCategory = pvsAlertTag.isMasterCategory
                            updateTag.priority = pvsAlertTag.priority
                            updateTag.execConfigId=exConfigId
                            updateTag.save(flush: true)
                        }
                        break
                }
            }
        } catch(Throwable th){
            throw th
        }
    }

    def fetchMartDto(CategoryDTO categoryDTO) {
        com.rxlogix.CategoryDTO category = new com.rxlogix.CategoryDTO()
        category.setCatId(categoryDTO.catId as Integer)
        category.setCatName(categoryDTO.catName)
        category.setSubCatId(categoryDTO.subCatId as Integer)
        category.setSubCatName(categoryDTO.subCatName)
        category.setFactGrpId(categoryDTO.factGrpId)
        category.setDmlType(categoryDTO.dmlType)
        category.setDataSource(categoryDTO.dataSource)
        category.setModule(categoryDTO.module)
        category.setCreatedBy(categoryDTO.createdBy)
        category.setFactGrpCol1(categoryDTO.factGrpCol1)
        category.setPriority(categoryDTO.priority)
        category.setFactGrpCol2(categoryDTO.factGrpCol2)
        category.setFactGrpCol3(categoryDTO.factGrpCol3)
        category.setFactGrpCol4(categoryDTO.factGrpCol4)
        category.setUpdatedBy(categoryDTO.updatedBy)
        category.setCreatedDate(categoryDTO.createdDate)
        category.setUpdatedDate(categoryDTO.updatedDate)
        category.setPrivateUserId(categoryDTO.privateUserId as Integer)
        return category
    }

    boolean isAddTags(PvsAlertTag pvsAlertTag, String userName, List<Map> tags){
        ((pvsAlertTag.privateUser && userName == pvsAlertTag.privateUser) || !pvsAlertTag.privateUser) && !tags.any {
            it.alertId == pvsAlertTag.alertId && it.tagText == pvsAlertTag.tagText && it.subTagText?.split(";")?.contains(pvsAlertTag.subTagText) && it.privateUserName == pvsAlertTag.privateUser
        }
    }

    PvsAlertTag fetchPvsAlertTagObject(String tagName, Long tagId, Long alertId, String domain, String subTagName = null,
                                       Long subTagId = null, Boolean autoTagged=false, Boolean isRetained = false, Long execConfigid = 0) {
        PvsAlertTag pvsAlertTag = new PvsAlertTag()
        pvsAlertTag.tagId = tagId
        pvsAlertTag.subTagText = subTagName
        pvsAlertTag.subTagId = subTagId
        pvsAlertTag.priority = 9999
        pvsAlertTag.tagText = tagName
        pvsAlertTag.alertId = alertId
        pvsAlertTag.domain = domain
        pvsAlertTag.createdAt = new Date()
        pvsAlertTag.modifiedAt = new Date()
        pvsAlertTag.autoTagged = autoTagged
        pvsAlertTag.isRetained = isRetained
        pvsAlertTag.execConfigId = execConfigid
        return pvsAlertTag

    }
}
