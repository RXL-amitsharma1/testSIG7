package com.rxlogix

import org.hibernate.Transaction
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.ArchivedLiteratureAlert
import com.rxlogix.config.LiteratureActivity
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.dto.AlertTagDTO
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.util.DateUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyResultSetExtension
import groovy.sql.OutParameter
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import oracle.jdbc.OracleTypes
import org.hibernate.SQLQuery
import org.hibernate.Session
import com.rxlogix.user.User
import com.rxlogix.util.SignalQueryHelper
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime

import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Transactional
class PvsGlobalTagService {
    def sessionFactory
    def signalDataSourceService
    def alertService
    def sqlGenerationService
    def singleCaseAlertService
    def activityService
    def userService
    def literatureActivityService
    def aggregateCaseAlertService
    def pvsAlertTagService
    ReportIntegrationService reportIntegrationService
    def signalExecutorService
    def dataSource
    def dataSource_pva
    def dataObjectService

    def saveGlobalTag(AlertTagDTO globalTag, User assignedToUser = null, Group assignedToGroup = null) {
        String previousDomain = globalTag.domain
        updateDomainForOnDemand(globalTag)
        PvsGlobalTag tag = new PvsGlobalTag()
        def activity
        String userName
        Map activityMap =[:]
        PvsGlobalTag.withTransaction {
            tag = populateTagObject(globalTag, tag)
            String alertText = globalTag.alertLevel ? "(A)" : ""
            String privateUser = (tag.privateUser) ? "(P)" : ""
            String description = tag.subTagText ? "Category Added " + tag.tagText + privateUser + alertText + " and Sub-Category(ies) Added " + tag.subTagText : "Category Added " + tag.tagText + privateUser + alertText
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.CategoryAdded)
            ActivityType activityTypeUpdated = ActivityType.findByValue(ActivityTypeValue.CategoryUpdated)
            User performedBy = User.findByUsername(tag.createdBy) ?: User.findByUsername("System")
            userName = performedBy.username
            String privateUserName = tag?.privateUser
            switch (globalTag.domain) {
                case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                    if (globalTag.alertId && globalTag.isActivity && previousDomain != Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND) {
                        def singleCaseAlert
                        singleCaseAlert = SingleCaseAlert.get(globalTag.alertId as Long) ?: ArchivedSingleCaseAlert.get(globalTag.alertId as Long)
                        activity = pvsAlertTagService.createActivity(singleCaseAlert.executedAlertConfiguration, activityType,
                                performedBy, description, null,
                                ['For Case Number': singleCaseAlert.caseNumber], singleCaseAlert.productName, singleCaseAlert.pt, assignedToUser, singleCaseAlert.caseNumber,
                                assignedToGroup, null, privateUserName)

                        activityMap.put(singleCaseAlert.executedAlertConfiguration.id, activity)
                    }
                    break
                case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                    if (globalTag.alertId && globalTag.isActivity) {
                        def aggregateCaseAlert
                        aggregateCaseAlert = AggregateCaseAlert.get(globalTag.alertId as Long) ?: ArchivedAggregateCaseAlert.get(globalTag.alertId as Long)
                        if (aggregateCaseAlert && previousDomain != Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND) {
                            activity = pvsAlertTagService.createActivity(aggregateCaseAlert.executedAlertConfiguration, activityType,
                                    performedBy, description,
                                    null, ['For Aggregate Alert'], aggregateCaseAlert.productName, aggregateCaseAlert.pt
                                    , assignedToUser, null, assignedToGroup, null, privateUserName)

                            activityMap.put(aggregateCaseAlert.executedAlertConfiguration.id, activity)
                        }
                    } else if(globalTag.alertId && globalTag.isEdit){
                        def aggregateCaseAlert
                        aggregateCaseAlert = AggregateCaseAlert.get(globalTag.alertId as Long) ?: ArchivedAggregateCaseAlert.get(globalTag.alertId as Long)
                        description = tag.subTagText ? "Category Updated " + tag.tagText + privateUser + alertText+ " and Sub-Category(ies) Added " + tag.subTagText : "Category Updated " + tag.tagText + privateUser + alertText
                        if (aggregateCaseAlert && previousDomain != Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND) {
                            activity = pvsAlertTagService.createActivity(aggregateCaseAlert.executedAlertConfiguration, activityTypeUpdated,
                                    performedBy, description,
                                    null, ['For Aggregate Alert'], aggregateCaseAlert.productName, aggregateCaseAlert.pt
                                    , assignedToUser, null, assignedToGroup, null, privateUserName)

                            activityMap.put(aggregateCaseAlert.executedAlertConfiguration.id, activity)
                        }
                    }
                    break
                case Constants.AlertType.LITERATURE_ALERT:
                    if (globalTag.alertId && globalTag.isActivity) {
                        def literatureAlert
                        literatureAlert = LiteratureAlert.get(globalTag.alertId as Long) ?: ArchivedLiteratureAlert.get(globalTag.alertId as Long)
                        activity = new LiteratureActivity(type: activityType, details: description, timestamp: DateTime.now(), justification: null,
                                productName: literatureAlert.productSelection, eventName: literatureAlert.eventSelection, articleId: literatureAlert.articleId, searchString: literatureAlert.searchString, privateUserName: privateUserName,
                                performedBy: performedBy, assignedTo: assignedToUser, assignedToGroup: assignedToGroup, guestAttendeeEmail: null
                        )

                        activityMap.put(literatureAlert.exLitSearchConfig.id, activity)
                    }

                    break
            }
            tag.save(flush:true)
        }
        return [activityMap:  activityMap, alertTagMap:[type:'I',globalId:tag.globalId,globalTagId:tag.id], performedBy: userName]
    }

    def deleteGlobalTag(List alertTagsList, AlertTagDTO globalTag, User assignedToUser, Group assignedToGroup) {
        boolean allSubTagDeleted = true
        boolean isUpdated = false
        Long deleteTagCount = 0
        alertTagsList.each{ it ->
            if(it.tagText == globalTag.tagText){
                deleteTagCount = deleteTagCount+1
                if(it.dmlType!= Constants.DMLType.DELETE ){
                    allSubTagDeleted = false
                }
                if(it.dmlType == Constants.DMLType.INSERT){
                    isUpdated = true
                }
            }
        }
        String previousDomain = globalTag.domain
        updateDomainForOnDemand(globalTag)
        Long globalId = fetchGlobalId(globalTag.domain, globalTag.globalId, globalTag.prodHirearchyId, globalTag.eventHirearchyId)
        PvsGlobalTag tag = PvsGlobalTag.findByTagTextAndSubTagTextAndGlobalId(globalTag.tagText, globalTag.subTagText, globalId)
        List<PvsGlobalTag> tagList = PvsGlobalTag.findAllByTagTextAndGlobalId(globalTag.tagText,globalId)
        boolean existExtraSubTag = false
        if(tag) {
            ActivityType activityType
            String description
            tagList.each {
                if (it?.subTagText != tag?.subTagText) {
                    existExtraSubTag = true
                }
            }


            if ((existExtraSubTag && !(deleteTagCount == tagList.size() && allSubTagDeleted)) || isUpdated) {
                description = tag?.subTagText ? "Category Updated " + tag?.tagText + " and Sub-Category(ies) Removed " + tag?.subTagText : "Category Updated " + tag?.tagText
                activityType = ActivityType.findByValue(ActivityTypeValue.CategoryUpdated)
            } else {
                description = tag?.subTagText ? "Category Removed " + tag?.tagText + " and Sub-Category(ies) Removed " + tag?.subTagText : "Category Removed " + tag?.tagText
                activityType = ActivityType.findByValue(ActivityTypeValue.CategoryRemoved)
            }
            User performedBy = User.findByUsername(globalTag.modifiedBy) ?: User.findByUsername("System")
            def activity
            Map activityMap = [:]
            String userName = performedBy.username
            String privateUserName = tag?.privateUser
            def domain
            switch (globalTag.domain) {
                case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                    if (globalTag.alertId && globalTag.isActivity) {
                        def singleCaseAlert
                        singleCaseAlert = SingleCaseAlert.get(globalTag.alertId as Long) ?: ArchivedSingleCaseAlert.get(globalTag.alertId as Long)
                        if (singleCaseAlert && previousDomain != Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND) {
                            activity =  pvsAlertTagService.createActivity(singleCaseAlert.executedAlertConfiguration, activityType,
                                    performedBy, description, null,
                                    ['For Case Number': singleCaseAlert.caseNumber], singleCaseAlert.productName, singleCaseAlert.pt, assignedToUser, singleCaseAlert.caseNumber,
                                    assignedToGroup, null, privateUserName)
                            activityMap.put(singleCaseAlert.executedAlertConfiguration.id,activity)
                        }
                    }
                    break
                case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:

                    if (globalTag.alertId && globalTag.isActivity) {
                        def aggregateCaseAlert
                        aggregateCaseAlert = AggregateCaseAlert.get(globalTag.alertId as Long) ?: ArchivedAggregateCaseAlert.get(globalTag.alertId as Long)
                        if (aggregateCaseAlert && previousDomain != Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND) {
                            activity =  pvsAlertTagService.createActivity(aggregateCaseAlert.executedAlertConfiguration, activityType,
                                    performedBy, description,
                                    null, ['For Aggregate Alert'], aggregateCaseAlert.productName, aggregateCaseAlert.pt
                                    , assignedToUser, null, assignedToGroup, null, privateUserName)

                            activityMap.put(aggregateCaseAlert.executedAlertConfiguration.id,activity)
                        }

                    }
                    break
                case Constants.AlertType.LITERATURE_ALERT:
                     if (globalTag.alertId && globalTag.isActivity) {
                         def literatureAlert
                         literatureAlert = LiteratureAlert.get(globalTag.alertId as Long) ?: ArchivedLiteratureAlert.get(globalTag.alertId as Long)
                         activity = new LiteratureActivity(type: activityType, details: description, timestamp: DateTime.now(), justification: null,
                                productName: literatureAlert.productSelection, eventName: literatureAlert.eventSelection, articleId: literatureAlert.articleId, searchString: literatureAlert.searchString,
                                privateUserName: privateUserName, performedBy: performedBy, assignedTo: assignedToUser, assignedToGroup: assignedToGroup, guestAttendeeEmail: null
                        )

                        activityMap.put(literatureAlert.exLitSearchConfig.id, activity)
                    }
                    break
            }
            String privateUser = globalTag.privateUser ? User.get(globalTag.privateUser as Long)?.username : null
            List<PvsGlobalTag> tags = PvsGlobalTag.findAllByTagTextAndSubTagTextAndGlobalIdAndPrivateUser(globalTag.tagText, globalTag.subTagText, globalId, privateUser)
            List <Long> deleteTagsId =[]
            tags.each { gTag ->
                deleteTagsId.add(gTag.id)
            }
            return [activityMap:  activityMap, alertTagMap:[deleteTagsIds:deleteTagsId],performedBy: userName]
        }
    }

    void updateGlobalTag(AlertTagDTO globalTag) {
        updateDomainForOnDemand(globalTag)
        Long globalId = fetchGlobalId(globalTag.domain, globalTag.globalId, globalTag.prodHirearchyId, globalTag.eventHirearchyId)
        PvsGlobalTag tag = PvsGlobalTag.findByTagTextAndSubTagTextAndGlobalId(globalTag.tagText, globalTag.subTagText , globalId)
        if (tag) {
            tag = populateTagObject(globalTag, tag)
            tag.save(flush: true)
        }
    }

    void updateDomainForOnDemand(AlertTagDTO globalTag) {
        if (globalTag.domain.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND)) {
            globalTag.domain = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        } else if (globalTag.domain.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND)) {
            globalTag.domain = Constants.AlertConfigType.SINGLE_CASE_ALERT
        }
    }

    Long fetchGlobalId(String domain, String globalId, Integer prodHirearchyId, Integer eventHirearchyId) {
        Long globalAlertId
        switch (domain) {
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                List<String> list = globalId.split('-')
                Long globalCaseId = list[0]?.toLong()
                Integer versionNum = list[1]?.toInteger()
                globalAlertId = GlobalCase.findByCaseIdAndVersionNum(globalCaseId as Long,versionNum as Integer)?.id
                if (!globalAlertId) {
                    GlobalCase globalCase = new GlobalCase()
                    globalCase.caseId = globalCaseId
                    globalCase.versionNum = versionNum
                    globalCase.save(flush: true)
                    globalAlertId = globalCase?.id
                }
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                globalAlertId = GlobalProductEvent.findByProductEventCombAndProductKeyIdAndEventKeyId(globalId, prodHirearchyId as Long, eventHirearchyId as Long)?.id
                if (!globalAlertId) {
                    GlobalProductEvent globalProductEvent = new GlobalProductEvent()
                    globalProductEvent?.productEventComb = globalId
                    globalProductEvent?.productKeyId = prodHirearchyId as Long
                    globalProductEvent?.eventKeyId = eventHirearchyId as Long
                    globalProductEvent.save(flush: true)
                    globalAlertId = globalProductEvent?.id
                }
                break
            case Constants.AlertType.LITERATURE_ALERT:
                globalAlertId = GlobalArticle.findByArticleId(globalId as Long)?.id
                if (!globalAlertId) {
                    GlobalArticle globalArticle = new GlobalArticle()
                    globalArticle.articleId = globalId?.toLong()
                    globalArticle.save(flush: true)
                    globalAlertId = globalArticle?.id
                }
                break

        }
        return globalAlertId
    }

    void batchPersistGlobalCase(List newCaseIds) {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        GlobalCase.withTransaction {
            List batch = []
            newCaseIds.eachWithIndex { String caseId, Integer index ->
                if(!caseId.contains("null")) {
                    List list = caseId.split("-")
                    Long case_id = list[0] as Long
                    Integer versionNum = list[1] as Integer
                    GlobalCase globalCase = new GlobalCase()
                    globalCase.caseId = case_id
                    globalCase.versionNum = versionNum
                    batch += globalCase
                    globalCase.save(validate: false)
                }
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
        log.info("Global Cases are saved")
    }

    List<Map> fetchTagsAndSubtags(Boolean isMasterData = false) {
        Sql sql = null
        List<Map> tags = []
        try {
            sql = new Sql(signalDataSourceService.getDataSource('pva'))
            String statement = isMasterData ? "select * from code_value where code_list_id = ${Holders.config.mart.codeValue.tags.value} and is_master_data = 1" : "select * from code_value where code_list_id = ${Holders.config.mart.codeValue.tags.value}"
            sql.eachRow(statement, []) { row ->
                Map rowData = [id: row.id as Long, text: row.value?.trim(), parentId: row.parent_id as Long]
                tags << rowData
            }
        } catch (Throwable t) {
            log.error("Error on fetching Tags ")
        } finally {
            try {
                sql?.close()
            } catch (Throwable notableToHandle) {
                log.error("Failed to close the Sql", notableToHandle)
            }
            return tags
        }
    }

    List<Map> mapTagSubtags(List<Map> codeValues) {
        List<Map> subTags = fetchSubTagsFromMart(codeValues)
        subTags.each { subTag ->
            Map codeValue = codeValues.find { subTag.parentId == it.id }
            subTag.put('tagText', codeValue?.text)
        }
        return subTags
    }

    CategoryDTO fetchCategoryDto(String dataSource , String tagName, Long tagId, String globalString, Integer tagIndex, String subTagName = null,
                                 Long subTagId = null, Long privateUserId = null, Boolean autoTagged=false, Boolean isRetained=false, Long execConfigId=0,
                                Date createdDate=null, Date updatedDate=null, String createdBy = null, String modifiedBy = null, Integer priority = null) {
        String ptCode = null
        String smqCode = null
        String caseId = null
        if (globalString?.contains('-')) {
            List<String> aggregateStringList = globalString.split('-')
            if(aggregateStringList.size()>2) {
                globalString = aggregateStringList[0]
                ptCode = aggregateStringList[1]
                smqCode = aggregateStringList.size() > 2 ? aggregateStringList[2] : "null"
            }else{
                globalString = aggregateStringList[0]
                caseId = aggregateStringList[1]
            }
        }
        CategoryDTO category = new CategoryDTO()
        category.setSubCatName(subTagName)
        category.setCatId(tagId as Integer)
        category.setCatName(tagName)
        category.setSubCatId(subTagId as Integer)
        category.setFactGrpId(tagIndex)
        category.setDmlType("I")
        category.setDataSource(dataSource)
        category.setModule("PVS")
        category.setCreatedBy(createdBy ?: "System")
        category.setFactGrpCol1("1")
        category.setPriority(priority ?: 9999)
        category.setFactGrpCol2(globalString)
        category.setFactGrpCol3(ptCode?:caseId)
        category.setFactGrpCol4(smqCode)
        category.setUpdatedBy(modifiedBy ?: "System")
        category.setCreatedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( (createdDate?: new Date())))
        category.setUpdatedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( (updatedDate?: new Date())))
        category.setPrivateUserId(privateUserId ? privateUserId as Integer : null)
        category.setIsAutoTagged(autoTagged?1:0)
        category.setIsRetained(isRetained?1:0)
        category.setUdNumber1(execConfigId)
        return category
    }

    PvsGlobalTag fetchPvsGlobalTagObject(String tagName, Long tagId, Long globalId, String domain, String subTagName = null, Long subTagId = null,
                                         Boolean autoTagged = false, Boolean isRetained = false, Long execConfigId = 0, Long privateUserId = null) {
        PvsGlobalTag pvsGlobalTag = new PvsGlobalTag()
        pvsGlobalTag.tagId = tagId
        pvsGlobalTag.subTagText = subTagName
        pvsGlobalTag.subTagId = subTagId
        pvsGlobalTag.priority = 9999
        pvsGlobalTag.tagText = tagName
        pvsGlobalTag.globalId = globalId
        pvsGlobalTag.privateUser = privateUserId ? User.get(privateUserId)?.username : null
        pvsGlobalTag.domain = domain
        pvsGlobalTag.createdAt = new Date()
        pvsGlobalTag.modifiedAt = new Date()
        pvsGlobalTag.autoTagged = autoTagged
        pvsGlobalTag.isRetained = isRetained
        pvsGlobalTag.execConfigId = execConfigId
        return pvsGlobalTag
    }

    List<Map> batchPersistGlobalTags(List<PvsGlobalTag> tagsList) {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        List<Map> tagCaseMap = []
        PvsGlobalTag.withTransaction {
            List batch = []
            tagsList.eachWithIndex { def pvsGlobalTag, Integer index ->
                batch += pvsGlobalTag
                pvsGlobalTag.save(validate: false)
                tagCaseMap.add([col1: pvsGlobalTag.id.toString(), col2: pvsGlobalTag.globalId.toString()])
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

    void batchPersistGlobalProductEvents(List peCombinations) {
        Integer batchSize = Holders.config.signal.batch.size as Integer
            GlobalProductEvent.withTransaction {
                List batch = []
                peCombinations.eachWithIndex { def pec, Integer index ->
                    GlobalProductEvent globalProductEvent = new GlobalProductEvent()
                    globalProductEvent.productEventComb = pec.productEventComb
                    globalProductEvent.productKeyId = pec.productKeyId
                    globalProductEvent.eventKeyId = pec.eventKeyId
                    batch += globalProductEvent
                    globalProductEvent.save(validate: false)
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
            log.info("Global Product Events are saved")
    }

    List<Map> getAllGlobalTags(List<Long> globalIdList, String alertType, String callingScreen = null) {
        CopyOnWriteArraySet<Map> tags = new CopyOnWriteArraySet<Map>()
        String userName = userService.getCurrentUserName()
        List<Map> pvsGlobalTagList = []
        globalIdList.collate(Constants.AggregateAlertFields.BATCH_SIZE).each { batchIds ->
            List<Map> batchResult = PvsGlobalTag.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    groupProperty('globalId', 'globalId')
                    groupProperty('tagText', 'tagText')
                    groupProperty('subTagText', 'subTagText')
                    groupProperty('privateUser', 'privateUser')
                    groupProperty('priority', 'priority')
                }
                'eq'('domain', alertType)
                batchIds.collate(1000).each {
                    'in'('globalId', it)
                }
            } as List<Map>

            pvsGlobalTagList.addAll(batchResult)
            sessionFactory.currentSession.clear()
        }
        println '----------------------------------------------------------------------------------------------------------------------------------'
        println 'pvsGlobalTagList'
        println pvsGlobalTagList
        println '-----------------------------------------------------------------------------------------------------------------------------------'

        ConcurrentHashMap <String , Integer> tagsAdded = new ConcurrentHashMap<String , Integer>()
//        StringBuffer sBuilder = new StringBuffer()
        ExecutorService executorService = Executors.newFixedThreadPool(6)
        List<Future> futureList = pvsGlobalTagList.collect { pvsGlobalTag ->
            executorService.submit({ ->
                StringBuilder sBuilder = new StringBuilder()
                sBuilder.setLength(0)
                if ((pvsGlobalTag.privateUser && userName == pvsGlobalTag.privateUser) || !pvsGlobalTag.privateUser) {

                    sBuilder.append(pvsGlobalTag.tagText).append('-').append(pvsGlobalTag.subTagText).append('-').append(pvsGlobalTag.globalId)
                    String key = sBuilder.toString()
                    if (!tagsAdded.containsKey(key)) {

                        synchronized (tags) {
                            Map matchedMap = tags.find {it.globalId == pvsGlobalTag.globalId && it.tagText == pvsGlobalTag.tagText && pvsGlobalTag.privateUser==it.privateUserName}
                            if (matchedMap && pvsGlobalTag.subTagText != null) {
                                String subTags = matchedMap.subTagText
                                if(subTags && !subTags.contains(pvsGlobalTag.subTagText)){
                                    matchedMap.subTagText = subTags + ';' + pvsGlobalTag.subTagText
                                }
                            } else {
                                tags.add(['tagText'        : pvsGlobalTag.tagText, 'subTagText': pvsGlobalTag.subTagText, 'privateUser': pvsGlobalTag.privateUser ? '(P)' : '',
                                          'priority'       : pvsGlobalTag.priority, 'tagType': '', 'globalId': pvsGlobalTag.globalId,
                                          'privateUserName': pvsGlobalTag.privateUser])
                            }
                            tagsAdded.put(key, 1)
                        }
                    }
                }
            }as Runnable)
        }
        futureList.each {
            it.get()
        }
        List<Map> uniqueTags = new ArrayList<Map>();
        uniqueTags.addAll(tags);
        executorService?.shutdown()
        return uniqueTags
    }

    void batchPersistGlobalArticles(List articlesId) {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        GlobalArticle.withTransaction {
            List batch = []
            articlesId.eachWithIndex { def articleId, Integer index ->
                GlobalArticle globalArticle = new GlobalArticle()
                globalArticle.articleId = articleId
                batch += globalArticle
                globalArticle.save(validate: false)
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
        log.info("Global Articles are saved")
    }

    List<Map> fetchTagsfromMart(List<Map> codeValues) {
        List<Map> tags = codeValues.findAll {
            it.parentId == '0' || it.parentId == null
        }
        return tags

    }

    List<Map> fetchSubTagsFromMart(List<Map> codeValues) {
        List<Map> subTags = codeValues.findAll {
            it.parentId != '0' && it.parentId != null
        }
        return subTags
    }

    PvsGlobalTag populateTagObject(AlertTagDTO globalTag, PvsGlobalTag tag) {
        tag.tagId = globalTag.tagId?.toLong()
        tag.subTagId = globalTag.subTagId?.toLong()
        tag.tagText = globalTag.tagText
        tag.subTagText = globalTag.subTagText
        tag.globalId = fetchGlobalId(globalTag.domain, globalTag.globalId, globalTag.prodHirearchyId, globalTag.eventHirearchyId)
        tag.domain = globalTag.domain
        tag.createdBy = globalTag.createdBy
        tag.modifiedBy = globalTag.modifiedBy
        tag.createdAt = DateUtil.parseDate(globalTag.createdAt, DateUtil.DATEPICKER_UTC_FORMAT)
        tag.modifiedAt = DateUtil.parseDate(globalTag.modifiedAt, DateUtil.DATEPICKER_UTC_FORMAT)
        tag.privateUser = globalTag.privateUser ? User.get(globalTag.privateUser as Long)?.username : null
        tag.martId = globalTag.martId
        tag.isMasterCategory = globalTag.isMasterCategory
        tag.priority = globalTag.priority
        tag.autoTagged = globalTag.autoTagged
        tag.isRetained = globalTag.retained
        tag.execConfigId = globalTag.execConfigId
        return tag
    }

    void migrateGlobalCase() {
        Sql sql = new Sql(signalDataSourceService.getDataSource('pva'))
        String statement = "SELECT CASE_ID,VERSION_NUM FROM CAT_FACT_CASE_G GROUP BY CASE_ID,VERSION_NUM"
        List<String> caseIds = []
        sql.eachRow(statement, []) { row ->
            String caseVersionComb = row.CASE_ID+"-"+row.version_NUM
            if(!caseVersionComb.contains('null'))
                caseIds.add(caseVersionComb)
        }
        sql.close()
        List<GlobalCase> oldCaseIds = []
        caseIds.collate(1000).each { List<String> caseIdList ->
            oldCaseIds += GlobalCase.createCriteria().list {
                or {
                    caseIdList.each {
                        List list = it.split('-')
                        Long caseId = list[0] as Long
                        Integer versionNUm = list[1] as Integer
                        and {
                            'eq'("caseId", caseId)
                            'eq'("versionNum", versionNUm)
                        }

                    }
                }
            }
        }
        List oldCaseIdsMap = []
        oldCaseIds.each {
            oldCaseIdsMap.add(it.caseId+"-"+it.versionNum)
        }
        List<String> newCaseIds = caseIds - oldCaseIdsMap
        batchPersistGlobalCase(newCaseIds)
    }

    void importSingleGlobalTags() {
        String sql_statement = SignalQueryHelper.fetchGlobalTags(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        Map existingTags = [:]
        StringBuilder sBuilder = new StringBuilder()
        sqlQuery.list().each { row ->
            sBuilder.append(row[0]).append('-').append(row[1]).append('-').append(row[2])
            existingTags.put(sBuilder.toString() , 1)
            sBuilder.setLength(0)
        }
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            int getType() {
                return OracleTypes.CURSOR
            }
        }
        String statement = "SELECT * FROM GLOBAL_CASE"
        sqlQuery = session.createSQLQuery(statement)
        Map<String, Long> caseGlobalMap = new HashMap<>()
        StringBuilder sBuilder2 = new StringBuilder()
        sqlQuery.list()?.each { row ->
            sBuilder2.append(row[2]).append('-').append(row[3])
            caseGlobalMap.put(sBuilder2.toString(), row[0] as Long)
            sBuilder2.setLength(0)
        }
        log.info("globalCase")
        log.info("${caseGlobalMap.size()}")
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            statement = sqlGenerationService.getFetchGttStatement(Holders.config.category.singleCase.global)
            List pvsGlobalTagList = []
            sql.execute(statement)
            StringBuilder caseVersion = new StringBuilder()
            StringBuilder tagSubTagPair = new StringBuilder()
            sql.call("{call pkg_category.P_CAT_FACT_FETCH(?)}", [CURSOR_PARAMETER]) { result ->
                result.eachRow() { GroovyResultSetExtension resultRow ->
                    caseVersion.append(resultRow.getProperty("fact_grp_col_2")).append("-").append(resultRow.getProperty("fact_grp_col_3"))
                    Long globalId = caseGlobalMap.get(caseVersion.toString())
                    tagSubTagPair.append(resultRow.getProperty("cat_nm")).append("-").append(resultRow.getProperty("sub_cat_nm")).append('-').append(globalId)
                    if (globalId && !existingTags.containsKey(tagSubTagPair.toString())) {
                        PvsGlobalTag pvsGlobalTag = fetchPvsGlobalTag(resultRow)
                        pvsGlobalTag.domain = Constants.AlertConfigType.SINGLE_CASE_ALERT
                        pvsGlobalTag.globalId = globalId
                        pvsGlobalTagList.add(pvsGlobalTag)
                    }
                    caseVersion.setLength(0)
                    tagSubTagPair.setLength(0)

                }
                log.info("pvsGlobalTag saved : ${pvsGlobalTagList.size()}")
                List<Map> tagCaseMap = batchPersistGlobalTags(pvsGlobalTagList)
                log.info("tagCaseMap")
                log.info("${tagCaseMap.size()}")
                String insertValidatedQuery = "INSERT INTO SINGLE_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_CASE_ID) VALUES(?,?)"
                alertService.batchPersistForMapping(session, tagCaseMap, insertValidatedQuery)
            }
        } catch (Exception ex) {
            log.error(ex.toString())
        } finally {
            if (Objects.nonNull(sql)) {
                sql.close()
            }
        }
    }

    void migrateAggregateGlobalTags() {
        Session session = sessionFactory.currentSession
        String statement = "SELECT DISTINCT ATA.NAME , PRODUCT_ID , PT_CODE , SMQ_CODE , E.SELECTED_DATA_SOURCE FROM AGG_ALERT_TAGS T INNER JOIN AGG_ALERT A ON T.AGG_ALERT_ID = A.ID  INNER JOIN ALERT_TAG ATA ON T.ALERT_TAG_ID = ATA.ID INNER JOIN EX_RCONFIG E ON E.ID = A.EXEC_CONFIGURATION_ID "
        SQLQuery sqlQuery = session.createSQLQuery(statement)
        List<CategoryDTO> categoryDTOList = []
        sqlQuery.list()?.each { row ->
            String peComb = row[3] ? row[1] + "-" + row[2] + "-" + row[3] : row[1] + "-" + row[2] + "-" + "null"
            categoryDTOList.add(fetchCategoryDto(row[4] , row[0], null, peComb, Holders.config.category.aggregateCase.global))
        }
        categoryDTOList.collate(20000).each{
            CategoryUtil.saveCategories(it)
        }
    }

    void importAggregateGlobalTags() {
        String sql_statement = SignalQueryHelper.fetchGlobalTags(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        Map existingTags = [:]
        sqlQuery.list().each { row ->
            existingTags.put(row[0]  + '-' + row[1] +  '-' + row[2] , 1)
        }
        String statement = "SELECT * FROM GLOBAL_PRODUCT_EVENT"
        sqlQuery = session.createSQLQuery(statement)
        Map<String, Long> pEGlobalMap = new HashMap<>()
        sqlQuery.list()?.each { row ->
            pEGlobalMap.put(row[2] as String, row[0] as Long)
        }
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            int getType() {
                return OracleTypes.CURSOR
            }
        }
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            statement = sqlGenerationService.getFetchGttStatement(Holders.config.category.aggregateCase.global)
            List pvsGlobalTagList = []
            sql.execute(statement)
            sql.call("{call pkg_category.P_CAT_FACT_FETCH(?)}", [CURSOR_PARAMETER]) { result ->
                result.eachRow() { GroovyResultSetExtension resultRow ->
                    String productId = resultRow.getProperty("fact_grp_col_2")
                    String eventId = resultRow.getProperty("fact_grp_col_3")
                    String pEComb = resultRow.getProperty("fact_grp_col_4") ? productId + "-" + eventId + "-" + resultRow.getProperty("fact_grp_col_4") : productId + "-" + eventId + "-" + "null"
                    if (pEGlobalMap.get(pEComb)) {
                        String tagSubTagPair = resultRow.getProperty("cat_nm") + "-" + resultRow.getProperty("sub_cat_nm") + '-' + pEGlobalMap.get(pEComb)
                        if (!existingTags.containsKey(tagSubTagPair)) {
                            PvsGlobalTag pvsGlobalTag = fetchPvsGlobalTag(resultRow)
                            pvsGlobalTag.domain = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
                            pvsGlobalTag.globalId = pEGlobalMap.get(pEComb)
                            pvsGlobalTagList.add(pvsGlobalTag)
                        }
                    }
                }
                List<Map> tagPEMap = batchPersistGlobalTags(pvsGlobalTagList)
                String insertValidatedQuery = "INSERT INTO AGG_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_PRODUCT_EVENT_ID) VALUES(?,?)"
                alertService.batchPersistForMapping(session, tagPEMap, insertValidatedQuery)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (Objects.nonNull(sql)) {
                sql.close()
            }
        }
    }

    PvsGlobalTag fetchPvsGlobalTag(GroovyResultSetExtension resultRow) {
        PvsGlobalTag pvsGlobalTag = new PvsGlobalTag()
        pvsGlobalTag.tagText = resultRow.getProperty("cat_nm")
        pvsGlobalTag.subTagText = resultRow.getProperty("sub_cat_nm")
        pvsGlobalTag.tagId = resultRow.getProperty("cat_id") as Long
        pvsGlobalTag.subTagId = resultRow.getProperty("sub_cat_id") as Long
        pvsGlobalTag.createdAt = new Date()
        pvsGlobalTag.createdBy = resultRow.getProperty("created_by")
        pvsGlobalTag.modifiedAt = new Date()
        pvsGlobalTag.modifiedBy = resultRow.getProperty("updated_by")
        Long privateUserId = resultRow.getProperty("private_user_id") as Long
        if (privateUserId)
            pvsGlobalTag.privateUser = User.findById(privateUserId)?.username
        else
            pvsGlobalTag.privateUser = null
        pvsGlobalTag.martId = resultRow.getProperty("id") as Long
        pvsGlobalTag.priority = resultRow.getProperty("priority") as Integer
        pvsGlobalTag.isMasterCategory = false
        pvsGlobalTag.autoTagged = (resultRow.getProperty("is_auto_tagged") as Integer) > 0? true: false
        pvsGlobalTag.isRetained = (resultRow.getProperty("is_retained") as Integer) > 0? true: false
        pvsGlobalTag.execConfigId = resultRow.getProperty("ud_number_1") as Long

        return pvsGlobalTag
    }

    void migrateLiteratureGlobalTags() {
        Session session = sessionFactory.currentSession
        String statement = "SELECT DISTINCT ATA.NAME , ARTICLE_ID FROM LITERATURE_ALERT_TAGS L INNER JOIN LITERATURE_ALERT LA ON L.LITERATURE_ALERT_ID = LA.ID  INNER JOIN ALERT_TAG ATA ON L.ALERT_TAG_ID = ATA.ID "
        SQLQuery sqlQuery = session.createSQLQuery(statement)
        List<CategoryDTO> categoryDTOList = []
        sqlQuery.list()?.each { row ->
            categoryDTOList.add(fetchCategoryDto(Constants.DataSource.PVA , row[0], null, row[1] as String, Holders.config.category.literature.global))
        }
        categoryDTOList.collate(20000).each{
            CategoryUtil.saveCategories(it)
        }
    }

    void importLiteratureGlobalTags(){
        String sql_statement = SignalQueryHelper.fetchGlobalTags(Constants.AlertType.LITERATURE_ALERT)
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        Map existingTags = [:]
        sqlQuery.list().each { row ->
            existingTags.put(row[0]  + '-' + row[1] +  '-' + row[2] , 1)
        }
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            int getType() {
                return OracleTypes.CURSOR
            }
        }
        String statement = "SELECT * FROM GLOBAL_ARTICLE"
        sqlQuery = session.createSQLQuery(statement)
        Map<Long, Long> pEGlobalMap = new HashMap<>()
        sqlQuery.list()?.each { row ->
            pEGlobalMap.put(row[2] as Long, row[0] as Long)
        }
        log.info("globalArticle")
        log.info("${pEGlobalMap.size()}")
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            statement = sqlGenerationService.getFetchGttStatement(Holders.config.category.literature.global)
            List pvsGlobalTagList = []
            sql.execute(statement)
            sql.call("{call pkg_category.P_CAT_FACT_FETCH(?)}", [CURSOR_PARAMETER]) { result ->
                result.eachRow() { GroovyResultSetExtension resultRow ->
                    Long globalId = pEGlobalMap.get(resultRow.getProperty("fact_grp_col_2") as Long)
                    if (globalId) {
                        String tagSubTagPair = resultRow.getProperty("cat_nm") + "-" + resultRow.getProperty("sub_cat_nm") + '-' + globalId
                        if (!existingTags.containsKey(tagSubTagPair)) {
                            PvsGlobalTag pvsGlobalTag = fetchPvsGlobalTag(resultRow)
                            pvsGlobalTag.domain = Constants.AlertType.LITERATURE_ALERT
                            pvsGlobalTag.globalId = globalId
                            pvsGlobalTagList.add(pvsGlobalTag)
                        }
                    }

                }
                log.info("litarauter Global Tags ${pvsGlobalTagList.size()}")
                List<Map> tagArticleMap = batchPersistGlobalTags(pvsGlobalTagList)
                log.info("${tagArticleMap.size()}")
                String insertValidatedQuery = "INSERT INTO LITERAURE_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_ARTICLE_ID,CREATION_DATE) VALUES(?,?,TIMESTAMP)"
                alertService.batchPersistForMapping(session, tagArticleMap, insertValidatedQuery)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (Objects.nonNull(sql)) {
                sql.close()
            }
        }
    }

    void syncGlobalTags(List globalTagList, String dmlType) {
        try {
            globalTagList.each { tag->
                GlobalCase globalCase = GlobalCase.get(tag?.globalId)
                List<GlobalCase> globalCaseList = GlobalCase.findAllByCaseIdAndVersionNumGreaterThan(globalCase?.caseId, globalCase?.versionNum)
                switch (dmlType) {
                    case Constants.DMLType.INSERT:
                        globalCaseList?.each {
                            PvsGlobalTag newGlobalTag = tag?.clone()
                            newGlobalTag.id = null
                            newGlobalTag.globalId = it?.id
                            it.addToPvsGlobalTag(newGlobalTag)
                            newGlobalTag.save(flush: true)
                        }
                        break
                    case Constants.DMLType.DELETE:
                        globalCaseList.each {
                            List<PvsGlobalTag> tags = PvsGlobalTag.findAllByTagTextAndSubTagTextAndGlobalIdAndPrivateUser(tag?.tagText, tag?.subTagText, it?.id, tag?.privateUser)
                            tags?.each { gTag ->
                                GlobalCase.get(gTag?.globalId).removeFromPvsGlobalTag(gTag)
                                gTag?.delete(flush: true)
                            }
                        }
                        break
                    case Constants.DMLType.UPDATE:
                        globalCaseList.each {
                            PvsGlobalTag updateTag = PvsGlobalTag.findByTagTextAndSubTagTextAndGlobalId(tag?.tagText, tag?.subTagText, it?.id)
                            if (updateTag) {
                                updateTag.createdBy = tag?.createdBy
                                updateTag.modifiedBy = tag?.modifiedBy
                                updateTag.createdAt = tag?.createdAt
                                updateTag.modifiedAt = tag?.modifiedAt
                                updateTag.privateUser = tag?.privateUser
                                updateTag.isMasterCategory = tag?.isMasterCategory
                                updateTag.priority = tag?.priority
                                updateTag.save(flush: true)
                            }
                        }
                        break
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
    }

    /*
    This method is responsible for refreshing the Global Categories for given caseId from MART
    if version is null all the global CAT will be refreshed else the categories greater than
    passed version will be updated.
     */

    void refreshGlobalTagsForCaseId(String caseId, Integer caseVersion = null) {

        log.info("Sync Global Categories Started")
        Sql sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
        List<Long> globalIds
        if (caseVersion)
            globalIds = GlobalCase.findAllByCaseIdAndVersionNumGreaterThan(Long.parseLong(caseId), caseVersion)*.id
        else
            globalIds = GlobalCase.findAllByCaseId(Long.parseLong(caseId))*.id

        if (globalIds) {

            String deletePvsGlobalTags = SignalQueryHelper.deleteGlobalTags(globalIds.join(','))
            sql.execute(deletePvsGlobalTags)

            String deletePvsGlobalTagsMapping = SignalQueryHelper.deleteGlobalTagsMapping(globalIds.join(','))
            sql.execute(deletePvsGlobalTagsMapping)
        }

        String grpId = Holders.config.category.singleCase.global
        def url = Holders.config.pvcc.api.url
        def path = Holders.config.pvcc.api.path.get

        String params = 1 + "," + caseId
        try {

            def query = [grpId: grpId, grpParams: params]
            Map result = reportIntegrationService.get(url, path, query)
            Map response = result.data ?: [:]

            List<com.rxlogix.dto.CategoryDTO> dtoList = response.data

            dtoList?.each {

                GlobalCase globalCase = null
                if (caseVersion < (it.factGrpCol3 as Integer) || caseVersion == null) {
                    globalCase = GlobalCase.findByCaseIdAndVersionNum(it.factGrpCol2 as Long, it.factGrpCol3 as Integer)
                    if (!globalCase) {
                        globalCase = new GlobalCase(caseId: it.factGrpCol2 as Long, versionNum: it.factGrpCol3 as Integer)
                        globalCase.save(flush: true)
                    }
                    PvsGlobalTag pvsGlobalTag = fetchPvsGlobalTagObject(it.catName, it.catId, globalCase?.id
                            , Constants.AlertConfigType.SINGLE_CASE_ALERT, it.subCatName, it.subCatId, true, false, 0, it.privateUserId)
                    globalCase.addToPvsGlobalTag(pvsGlobalTag)
                    pvsGlobalTag.save(flush: true)
                }

            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }

        log.info("Sync Global Categories Ended")
    }

    void executeSqlChanges() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now executing tags related sql.")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.delete_agg_global_tags())
                sql.execute(SignalQueryHelper.update_agg_alerts())
                sql.execute(SignalQueryHelper.update_archived_agg_alerts())
            }
            log.info("Successfully Executed Tags related Sql.")
        }
        catch (Exception ex) {
            log.error("##################### Error occurred while updating GLOBAL_PRODUCT_EVENT table. #############", ex)
        } finally {
            sql.close()
        }
    }

    void truncateGlobalTags() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now truncating the tags related table.")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.disable_constraint_agg_table())
                sql.execute(SignalQueryHelper.disable_constraint_archived_agg_table())
                sql.execute(SignalQueryHelper.truncate_global_product_table())
                sql.execute(SignalQueryHelper.enable_constraint_agg_table())
                sql.execute(SignalQueryHelper.enable_constraint_archived_agg_table())
                log.info("Successfully truncated the tags related table.")
            }
        }
        catch (Exception ex) {
            println(ex)
            println("##################### Error occurred while updating GLOBAL_PRODUCT_EVENT table. #############")
        } finally {
            sql.close()
        }

    }

    void mergeAggAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now merging the agg table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.merge_agg_alerts())
            }
            log.info("Successfully merged the agg table")
        } catch (Exception ex) {
            println(ex)
            println("##################### Error occurred while merging agg_alert table. #############")
        } finally {
            sql.close()
        }
    }

    void mergeAggOnDemandAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now merging the agg on demand alert table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.merge_agg_on_demand_alerts())
            }
            log.info("Successfully merged the agg on demand alert table")
        } catch (Exception ex) {
            println(ex)
            println("##################### Error occurred while merging agg on demand alert table. #############")
        } finally {
            sql.close()
        }
    }

    void insertAggGlobalTags() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now inserting into the agg_global_tags table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.insert_agg_alerts())
            }
            log.info("Successfully inserted into the agg_global_tags table")
        } catch (Exception ex) {
            println(ex)
            println("##################### Error occurred while inserted into the agg_global_tags table. #############")
        } finally {
            sql.close()
        }
    }


    void mergeArchivedAggAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now merging the archived agg table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.merge_archived_agg_alerts())
            }
            log.info("Successfully merged the archived agg table")
        } catch (Exception ex) {
            println(ex)
            println("##################### Error occurred while merging ARCHIVED_AGG_ALERT table. #############")
        } finally {
            sql.close()
        }
    }
    void updateMigrationFlag(Sql sql, String tableName){
        try{
            sql.execute("drop synonym "+ tableName)
            sql.call("{call pkg_category.p_drop_table(?)}",[tableName])
            sql.execute("update cat_parameters set param_value='0' where param_key='IS_WEBAPP_MIG_REQUIRED'")
            sql.execute("COMMIT")
            log.info("Successfully updated the migration flag in mart.")
        } catch(Exception ex){
            log.error("Error occured while updating the migration flag with exception", ex)
        }

    }
    void executeCasesSqlChanges() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now executing tags related sql.")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.delete_single_global_tags())
                sql.execute(SignalQueryHelper.update_single_alerts())
                sql.execute(SignalQueryHelper.insert_dummy_values_global_case())
                sql.execute(SignalQueryHelper.update_archived_single_alerts())
            }
            log.info("Successfully Executed Tags related Sql.")
        }
        catch (Exception ex) {
            log.error("##################### Error occurred while updating SINGLE_GLOBAL_TAGS table. #############", ex)
        } finally {
            sql.close()
        }
    }

    void truncateCasesGlobalTags() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now truncating the case tags related table.")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.disable_constraint_single_table())
                sql.execute(SignalQueryHelper.disable_constraint_archived_single_table())
                sql.execute(SignalQueryHelper.truncate_global_case_table())
                sql.execute(SignalQueryHelper.enable_constraint_single_table())
                sql.execute(SignalQueryHelper.insert_dummy_values_global_case())
                sql.execute(SignalQueryHelper.enable_constraint_archived_single_table())
                log.info("Successfully truncated the case tags related table.")
            }
        }
        catch (Exception ex) {
            log.error("##################### Error occurred while updating Single Case tags table. #############",ex)
        } finally {
            sql.close()
        }

    }

    void mergeSingleAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now merging the single table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.merge_single_alerts())
            }
            log.info("Successfully merged the single table")
        } catch (Exception ex) {
            log.error("##################### Error occurred while merging single_case_alert table. #############",ex)
        } finally {
            sql.close()
        }
    }

    void mergeSingleOnDemandAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now merging the single on demand alert table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.merge_single_on_demand_alerts())
            }
            log.info("Successfully merged the single on demand alert table")
        } catch (Exception ex) {
            log.error("##################### Error occurred while merging single on demand alert table. #############",ex)
        } finally {
            sql.close()
        }
    }

    void insertSingleGlobalTags() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now inserting into the single_global_tags table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.insert_single_alerts())
            }
            log.info("Successfully inserted into the single_global_tags table")
        } catch (Exception ex) {
            println(ex)
            println("##################### Error occurred while inserted into the single_global_tags table. #############")
        } finally {
            sql.close()
        }
    }


    void mergeArchivedSingleAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("Now merging the archived single table")
            sql.withTransaction{
                sql.execute(SignalQueryHelper.merge_archived_single_alerts())
            }
            log.info("Successfully merged the archived single table")
        } catch (Exception ex) {
            log.error("##################### Error occurred while merging ARCHIVED_SINGLE_ALERT table. #############",ex)
        } finally {
            sql.close()
        }
    }


    void importAggregateGlobalTagsWithKeyId() {
        Long startTime = System.currentTimeMillis()
        log.info("Executing importAggregateGlobalTagsWithKeyId")
        Sql sql = new Sql(dataSource_pva)
        Integer count =0
        sql.eachRow(SignalQueryHelper.is_migration_required(),[] ){ row ->
            count = row.param_value as Integer
        }
        if (count || Holders.config.signal.cat.count.flag) {
            Session session = sessionFactory.currentSession
            OutParameter CURSOR_PARAMETER = new OutParameter() {
                int getType() {
                    return OracleTypes.CURSOR
                }
            }
            String statement = sqlGenerationService.getFetchGttStatementTab(Holders.config.category.aggregateCase.global)
            sql.execute(statement)

            statement = sqlGenerationService.getInputStatement()
            sql.execute(statement)

            String tableName =""
            sql.call("{call pkg_category_mig_util.p_cat_fact_fetch_tab_exec(?)}", [CURSOR_PARAMETER]) { result ->
                result.eachRow() { GroovyResultSetExtension resultRow ->
                    tableName = resultRow.getProperty("CAT_FACT_FETCH_TAB_NAME")
                }
            }
            if(tableName){
                try{
                    List catData = sql.rows("SELECT * FROM "+tableName)
                    executeSqlChanges()
                    truncateGlobalTags()
                    savePvsGlobalTags(catData)
                    createGPEForAggAlerts()
                    createGPEForArchivedAggAlerts()
                    insertAggGlobalTags()
                    mergeAggAlerts()
                    mergeArchivedAggAlerts()
                    mergeAggOnDemandAlerts()
                    updateMigrationFlag(sql, tableName)
                    dataObjectService.clearPecMap()
                } catch (Exception ex){
                    println(ex)
                } finally {
                    sql.close()
                }
            }
        }
        Long endTime = System.currentTimeMillis()
        log.info('time taken to importAggregateGlobalTagsWithKeyId for cat:' + (endTime - startTime)/1000 +"sec")
    }



    void importSingleGlobalTagsWithKeyId() {
        Long startTime = System.currentTimeMillis()
        log.info("Executing importSingleGlobalTagsWithKeyId")
        Sql sql = new Sql(dataSource_pva)
        Integer count =0
        sql.eachRow(SignalQueryHelper.is_migration_required(),[] ){ row ->
            count = row.param_value as Integer
        }
        if (count || Holders.config.signal.cat.count.flag) {
            OutParameter CURSOR_PARAMETER = new OutParameter() {
                int getType() {
                    return OracleTypes.CURSOR
                }
            }
            String statement = sqlGenerationService.getFetchGttStatementTab(Holders.config.category.singleCase.global)
            sql.execute(statement)

            statement = sqlGenerationService.getInputStatement()
            sql.execute(statement)

            String tableName =""
            sql.call("{call pkg_category_mig_util.p_cat_fact_fetch_tab_exec(?)}", [CURSOR_PARAMETER]) { result ->
                result.eachRow() { GroovyResultSetExtension resultRow ->
                    tableName = resultRow.getProperty("CAT_FACT_FETCH_TAB_NAME")
                }
            }
            if(tableName){
                try{
                    List catData = sql.rows("SELECT * FROM "+tableName)
                    executeCasesSqlChanges()
                    truncateCasesGlobalTags()
                    saveCaseLevelPvsGlobalTags(catData)
                    createGCForSingleAlerts()
                    createGCForArchivedSingleAlerts()
                    insertSingleGlobalTags()
                    mergeSingleAlerts()
                    mergeArchivedSingleAlerts()
                    mergeSingleOnDemandAlerts()
                    updateMigrationFlag(sql, tableName)
                    dataObjectService.clearCaseMap()
                } catch (Exception ex){
                    log.error("Error occured while sycning single alert global categories", ex)
                } finally {
                    sql.close()
                }
            }
        }
        Long endTime = System.currentTimeMillis()
        log.info('time taken to importSingleGlobalTagsWithKeyId for cat:' + (endTime - startTime)/1000 +"sec")
    }

    void savePvsGlobalTags(List catData){
        Long startTime = System.currentTimeMillis()
        Session session = sessionFactory.openSession()
        Transaction tx = session.beginTransaction()
        catData.eachWithIndex { it, counter ->
            String productId = it.fact_grp_col_2
            String eventId = it.fact_grp_col_3
            String productEventComb = it.fact_grp_col_4 ? productId + "-" + eventId + "-" + it.fact_grp_col_4 : productId + "-" + eventId + "-" + "null"
            Long productKeyId = it.fact_grp_col_5 as Long
            Long eventKeyId = it.fact_grp_col_6 as Long
            Long pecId = dataObjectService.getPecMap(productEventComb + "-" + it.fact_grp_col_5 + "-" + it.fact_grp_col_6)
            if(!pecId){
                GlobalProductEvent globalProductEvent = new GlobalProductEvent()
                globalProductEvent.productEventComb = productEventComb
                globalProductEvent.productKeyId=productKeyId
                globalProductEvent.eventKeyId=eventKeyId
                session.save(globalProductEvent)
                dataObjectService.setPecMap(productEventComb + "-" + it.fact_grp_col_5 + "-" + it.fact_grp_col_6, globalProductEvent.id)
            }
            PvsGlobalTag pvsGlobalTag = new PvsGlobalTag()
            pvsGlobalTag.tagText = it.cat_nm
            pvsGlobalTag.subTagText = it.sub_cat_nm
            pvsGlobalTag.tagId = it.cat_id as Long
            pvsGlobalTag.subTagId = it.sub_cat_id as Long
            pvsGlobalTag.createdAt = new Date()
            pvsGlobalTag.createdBy = it.created_by
            pvsGlobalTag.modifiedAt = new Date()
            pvsGlobalTag.modifiedBy = it.updated_by
            Long privateUserId = it.private_user_id as Long
            if (privateUserId)
                pvsGlobalTag.privateUser = User.findById(privateUserId)?.username
            else
                pvsGlobalTag.privateUser = null
            pvsGlobalTag.martId = it.id as Long
            pvsGlobalTag.priority = it.priority as Integer
            pvsGlobalTag.isMasterCategory = false
            pvsGlobalTag.autoTagged = (it.is_auto_tagged as Integer) > 0? true: false
            pvsGlobalTag.isRetained = (it.is_retained as Integer) > 0? true: false
            pvsGlobalTag.execConfigId = it.ud_number_1 as Long
            pvsGlobalTag.globalId = dataObjectService.getPecMap(productEventComb + "-" + it.fact_grp_col_5 + "-" + it.fact_grp_col_6)
            pvsGlobalTag.domain = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
            session.save(pvsGlobalTag)
            if(counter.mod(1000)==0) {
                //clear session and save records after every 1000 records
                session.flush();
                session.clear();
            }
        }
        tx.commit()
        session.close()
        Long endTime = System.currentTimeMillis()
        log.info('time taken to save savePvsGlobalTags for cat:' + (endTime - startTime)/1000 +"sec")

    }
    void saveCaseLevelPvsGlobalTags(List catData){
        Long startTime = System.currentTimeMillis()
        Session session = sessionFactory.openSession()
        Transaction tx = session.beginTransaction()
        catData.eachWithIndex { it, counter ->
            Long caseId = it.fact_grp_col_2 as Long
            Integer versionNum = it.fact_grp_col_3 as Integer
            String caseComb = caseId + "-" + versionNum
            Long caseCombId = dataObjectService.getCaseMap(caseComb)
            if(!caseCombId){
                GlobalCase globalCase = new GlobalCase()
                globalCase.caseId = caseId
                globalCase.versionNum = versionNum
                session.save(globalCase)
                dataObjectService.setCaseMap(caseComb, globalCase.id)
            }
            PvsGlobalTag pvsGlobalTag = new PvsGlobalTag()
            pvsGlobalTag.tagText = it.cat_nm
            pvsGlobalTag.subTagText = it.sub_cat_nm
            pvsGlobalTag.tagId = it.cat_id as Long
            pvsGlobalTag.subTagId = it.sub_cat_id as Long
            pvsGlobalTag.createdAt = new Date()
            pvsGlobalTag.createdBy = it.created_by
            pvsGlobalTag.modifiedAt = new Date()
            pvsGlobalTag.modifiedBy = it.updated_by
            Long privateUserId = it.private_user_id as Long
            if (privateUserId)
                pvsGlobalTag.privateUser = User.findById(privateUserId)?.username
            else
                pvsGlobalTag.privateUser = null
            pvsGlobalTag.martId = it.id as Long
            pvsGlobalTag.priority = it.priority as Integer
            pvsGlobalTag.isMasterCategory = false
            pvsGlobalTag.autoTagged = (it.is_auto_tagged as Integer) > 0? true: false
            pvsGlobalTag.isRetained = (it.is_retained as Integer) > 0? true: false
            pvsGlobalTag.execConfigId = it.ud_number_1 as Long
            pvsGlobalTag.globalId = dataObjectService.getCaseMap(caseComb)
            pvsGlobalTag.domain = Constants.AlertConfigType.SINGLE_CASE_ALERT
            session.save(pvsGlobalTag)
            if(counter.mod(1000)==0) {
                //clear session and save records after every 1000 records
                session.flush();
                session.clear();
            }
        }
        tx.commit()
        session.close()
        Long endTime = System.currentTimeMillis()
        log.info('time taken to save savePvsGlobalTags for case cat:' + (endTime - startTime)/1000 +"sec")

    }
    void runCategoriesMigrationInDB(){
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            sql.call("{call pkg_category_mig_util.p_pop_new_col_data()}")
        } catch (Exception ex) {
            log.error("##################### Error occurred while updating PvsGlobalTag table For AggregateCaseAlert. #############", ex)
        } finally {
            sql?.close()
        }

    }
    void createGPEForAggAlerts(){
        Sql sql = new Sql(dataSource)
        try {
            def list = sql.rows(SignalQueryHelper.get_agg_sql())
            Long startTime = System.currentTimeMillis()
            Session session = sessionFactory.openSession()
            Transaction tx = session.beginTransaction()
            list.eachWithIndex { aca, int i ->
                String productEventComb = aca.smqCode ? aca.productId + "-" + aca.ptCode + "-" + aca.smqCode : aca.productId + "-" + aca.ptCode + "-" + 'null'
                Long pecId = dataObjectService.getPecMap(productEventComb + "-" + aca.prodHierarchyId + "-" + aca.eventHierarchyId)
                if(!pecId){
                    GlobalProductEvent globalProductEvent = new GlobalProductEvent()
                    globalProductEvent?.productEventComb = productEventComb
                    globalProductEvent?.productKeyId = aca.prodHierarchyId as Long
                    globalProductEvent?.eventKeyId = aca.eventHierarchyId as Long
                    session.save(globalProductEvent)
                    dataObjectService.setPecMap(productEventComb + "-" + aca.prodHierarchyId + "-" + aca.eventHierarchyId, globalProductEvent.id)
                }
                if (i.mod(1000) == 0) {
                    //clear session and save records after every 1000 records
                    session.flush()
                    session.clear()
                }
            }
            tx.commit()
            session.close()
            Long endTime = System.currentTimeMillis()
            list.clear()
            log.info('time taken to save GPE for Agg Alert:' + (endTime - startTime) / 1000 + "sec")
        } catch (Exception ex){
            println ex
        } finally {
            sql.close()
        }
    }
    void createGPEForArchivedAggAlerts(){
        Sql sql = new Sql(dataSource)
        try{
            def list = sql.rows(SignalQueryHelper.get_archived_agg_sql())
            Long startTime = System.currentTimeMillis()
            Session session = sessionFactory.openSession()
            Transaction tx = session.beginTransaction()
            list.eachWithIndex{ aca, int i ->
                String productEventComb = aca.smqCode ? aca.productId + "-" + aca.ptCode + "-" + aca.smqCode : aca.productId + "-" + aca.ptCode + "-" + 'null'
                Long pecId = dataObjectService.getPecMap(productEventComb + "-" + aca.prodHierarchyId + "-" + aca.eventHierarchyId)
                if(!pecId){
                    GlobalProductEvent globalProductEvent = new GlobalProductEvent()
                    globalProductEvent?.productEventComb = productEventComb
                    globalProductEvent?.productKeyId = aca.prodHierarchyId as Long
                    globalProductEvent?.eventKeyId = aca.eventHierarchyId as Long
                    session.save(globalProductEvent)
                    dataObjectService.setPecMap(productEventComb + "-" + aca.prodHierarchyId + "-" + aca.eventHierarchyId, globalProductEvent.id)
                }
                if(i.mod(1000)==0) {
                    //clear session and save records after every 1000 records
                    session.flush()
                    session.clear()
                }
            }
            tx.commit()
            session.close()
            Long endTime = System.currentTimeMillis()
            list.clear()
            log.info('time taken to save GPE for Archived Agg Alert:' + (endTime - startTime)/1000 +"sec")
        } catch (Exception ex){
            println ex
        } finally {
            sql.close()
        }
    }

    void createGCForSingleAlerts(){
        Sql sql = new Sql(dataSource)
        try {
            def list = sql.rows(SignalQueryHelper.get_single_sql())
            Long startTime = System.currentTimeMillis()
            Session session = sessionFactory.openSession()
            Transaction tx = session.beginTransaction()
            list.eachWithIndex { sca, int i ->
                String caseComb = sca.caseId + "-" + sca.versionNum
                Long caseId = dataObjectService.getCaseMap(caseComb)
                if(!caseId){
                    GlobalCase globalCase = new GlobalCase()
                    globalCase.caseId = sca.caseId
                    globalCase.versionNum = sca.versionNum
                    session.save(globalCase)
                    dataObjectService.setCaseMap(caseComb, globalCase.id)
                }
                if (i.mod(1000) == 0) {
                    //clear session and save records after every 1000 records
                    session.flush()
                    session.clear()
                }
            }
            tx.commit()
            session.close()
            Long endTime = System.currentTimeMillis()
            list.clear()
            log.info('time taken to save GC for Single Alert:' + (endTime - startTime) / 1000 + "sec")
        } catch (Exception ex){
            log.error("##################### Error occurred while creating Global Cases #############", ex)
        } finally {
            sql.close()
        }
    }
    void createGCForArchivedSingleAlerts(){
        Sql sql = new Sql(dataSource)
        try{
            def list = sql.rows(SignalQueryHelper.get_archived_single_sql())
            Long startTime = System.currentTimeMillis()
            Session session = sessionFactory.openSession()
            Transaction tx = session.beginTransaction()
            list.eachWithIndex { sca, int i ->
                String caseComb = sca.caseId + "-" + sca.versionNum
                Long caseId = dataObjectService.getCaseMap(caseComb)
                if(!caseId){
                    GlobalCase globalCase = new GlobalCase()
                    globalCase.caseId = sca.caseId
                    globalCase.versionNum = sca.versionNum
                    session.save(globalCase)
                    dataObjectService.setCaseMap(caseComb, globalCase.id)
                }
                if (i.mod(1000) == 0) {
                    //clear session and save records after every 1000 records
                    session.flush()
                    session.clear()
                }
            }
            tx.commit()
            session.close()
            Long endTime = System.currentTimeMillis()
            list.clear()
            log.info('time taken to save GC for Archived Single Alert:' + (endTime - startTime)/1000 + "sec")
        } catch (Exception ex){
            println ex
        } finally {
            sql.close()
        }
    }



}

