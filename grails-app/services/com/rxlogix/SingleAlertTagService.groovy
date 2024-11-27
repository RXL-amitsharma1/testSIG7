package com.rxlogix

import com.rxlogix.mart.CaseSeriesTagMapping
import com.rxlogix.mart.GlobalTagMapping
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleGlobalTag
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import org.apache.commons.lang.time.DateUtils
import org.hibernate.criterion.CriteriaSpecification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Transactional
class SingleAlertTagService {

    static Boolean serviceAvailable = true
    static Date lastUpdatedDate
    static Date lastUpdatedCaseDate
    AlertService alertService
    def signalDataSourceService
    def dataSource

    List<SingleGlobalTag> getGlobaltags(Long caseId, Date lastUpdated) {
        List<GlobalTagMapping> globalTags = []
        List<SingleGlobalTag> singleGlobalTags = []
        GlobalTagMapping.withTransaction {
            globalTags = GlobalTagMapping.createCriteria().list {
                if (lastUpdated) {
                    ge("lastUpdated", lastUpdated)
                }
                if (caseId) {
                    eq("caseId", caseId)
                }
            }
            if(globalTags && lastUpdated){
                lastUpdatedDate = globalTags.max {it.lastUpdated}.lastUpdated
            }
            SingleGlobalTag newSGT
            globalTags.each { tagObj ->
                newSGT = new SingleGlobalTag()
                newSGT.caseId = tagObj.caseId
                newSGT.tagId = tagObj.tags?.getId()
                newSGT.tagText = tagObj.tags?.name
                newSGT.lastUpdated = tagObj.lastUpdated
                singleGlobalTags << newSGT
            }
        }
        singleGlobalTags
    }

    List<SingleGlobalTag> getCaseSeriesTags(Long caseId, Long caseSeriesId, Date lastUpdated) {
        List<CaseSeriesTagMapping> caseSeriesTags = []
        List<SingleGlobalTag> singleGlobalTags = []
        CaseSeriesTagMapping.withTransaction {
            caseSeriesTags = CaseSeriesTagMapping.createCriteria().list {
                if (lastUpdated) {
                    ge("lastUpdated", lastUpdated)
                }
                if (caseSeriesId)
                    eq("caseSeriesExecId", caseSeriesId)
                if (caseId) {
                    eq("caseId", caseId)
                }
            }
            if(caseSeriesTags && lastUpdated){
                lastUpdatedCaseDate = caseSeriesTags.max {it.lastUpdated}.lastUpdated
            }
            SingleGlobalTag newSGT
            caseSeriesTags.each { tagObj ->
                newSGT = new SingleGlobalTag()
                newSGT.caseId = tagObj.caseId
                newSGT.tagId = tagObj.tag?.getId()
                newSGT.tagText = tagObj.tag?.name
                newSGT.lastUpdated = tagObj.lastUpdated
                newSGT.owner = tagObj.owner
                newSGT.caseSeriesId = tagObj.caseSeriesExecId
                singleGlobalTags << newSGT
            }
        }
        singleGlobalTags
    }

    void saveExistingTags(List<SingleGlobalTag> singleGlobalTagList) {
        if (singleGlobalTagList) {

            List<Long> scaCaseIds = singleGlobalTagList.collect {
                it.caseId
            }?.unique()

            List<SingleCaseAlert> scaAlerts = SingleCaseAlert.createCriteria().list {
                or {
                    scaCaseIds.collate(1000).each {
                        'in'('caseId', it)
                    }
                }
                isNotNull("caseId")
            }

            List<SingleCaseAlert> singleCaseAlertList = []
            scaAlerts.each { sca ->
                try {
                    List<SingleGlobalTag> newTags = singleGlobalTagList.findAll { it.caseId == sca.caseId }

                    if (newTags) {
                        SingleGlobalTag newSGT
                        newTags.each { tagObj ->
                            newSGT = new SingleGlobalTag()
                            newSGT.caseId = tagObj.caseId
                            newSGT.tagId = tagObj.tagId
                            newSGT.tagText = tagObj.tagText
                            newSGT.lastUpdated = tagObj.lastUpdated
                            newSGT.owner = tagObj.owner
                            newSGT.caseSeriesId = tagObj.caseSeriesId
                            sca.addToTags(newSGT)

                        }
                        singleCaseAlertList.add(sca)
                    }

                } catch (Exception ex) {
                    log.error("Single Global tag insertion/deletion failed for: " + sca.caseId)
                    log.error(ex.printStackTrace())
                }
            }
            if (singleCaseAlertList) {
                alertService.batchPersistForDomain(singleCaseAlertList, SingleCaseAlert.class.name)
            }
        }
    }

    // called when save particular case from review screen
    @Transactional
    def reloadTags(Long pvrCaseSeriesId, Long caseId) {

        List<SingleGlobalTag> singleGlobalTags = []
        ExecutorService executorService = Executors.newFixedThreadPool(2)
        try {
            Set<Callable> callables = new HashSet<Callable>()

            callables.add({ ->
                getGlobaltags(caseId, null)

            })

            callables.add({ ->
                getCaseSeriesTags(caseId, null, null)
            })

            List<Future> futureList = executorService.invokeAll(callables)

            futureList.each {
                singleGlobalTags << it.get()
            }

            singleGlobalTags = singleGlobalTags.flatten()
            clearTags(caseId)
            SingleCaseAlert.withTransaction {
                SingleCaseAlert.findAllByCaseId(caseId).each { sca ->

                    SingleGlobalTag newSGT
                    singleGlobalTags.each { tag ->

                        if (tag.caseSeriesId == null ||
                                tag.caseSeriesId == sca.executedAlertConfiguration.pvrCaseSeriesId) {
                            newSGT = new SingleGlobalTag()
                            newSGT.tagId = tag.tagId
                            newSGT.tagText = tag.tagText
                            newSGT.caseId = tag.caseId
                            newSGT.lastUpdated = tag.lastUpdated
                            if (tag.caseSeriesId == sca.executedAlertConfiguration.pvrCaseSeriesId) {
                                newSGT.owner = tag.owner
                                newSGT.caseSeriesId = tag.caseSeriesId
                            }
                            sca.addToTags(newSGT)
                            sca.save()
                        }
                    }
                }
            }

        }
        catch (Exception ex) {
            log.error(ex.printStackTrace())
        }
        finally {
            executorService.shutdown()
        }


    }

    void clearTags(Long caseId) {
        def dataSourceObj = signalDataSourceService.getReportConnection('dataSource')
        Sql sql = null
        try{
            if(caseId) {
                sql = new Sql(dataSourceObj)
                String deleteTagsFromChild = "DELETE FROM SINGLE_GLOBAL_TAG_MAPPING WHERE SINGLE_GLOBAL_ID IN (SELECT ID FROM SINGLE_CASE_ALL_TAG WHERE CASE_ID = ${caseId})"
                String deleteTagsFromParent = "DELETE FROM SINGLE_CASE_ALL_TAG WHERE CASE_ID = ${caseId}"
                log.info(deleteTagsFromChild)
                sql.execute(deleteTagsFromChild)
                log.info(deleteTagsFromParent)
                sql.execute(deleteTagsFromParent)
                log.info("Tags deleted")
            }
        }catch(java.sql.SQLSyntaxErrorException sqe){
            sqe.printStackTrace()
            log.error("Exception occurred while clearing tags.")
        }catch(Throwable e) {
            e.printStackTrace()
            log.error("Exception occurred while clearing tags.")
        } finally{
            sql?.close()
        }
    }

    List<Map> getSingleAlertTags(List<Long> idList) {

        List<Map> results = SingleCaseAlert.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                'tags' {
                    property("caseId", "caseId")
                    property("tagId", "tagId")
                    property("tagText", "tagText")
                    property("caseSeriesId", "caseSeriesId")
                }
            }
            or {
                idList.collate(1000).each {
                    'in'('caseId', it)
                }
            }
        } as List<Map>

        results
    }

}
