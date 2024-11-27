package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.signal.AlertComment
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.sql.Sql

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.stream.Collectors

trait AlertAsyncUtil implements AlertUtil {
    private String userTimezone

    List fetchValuesForAggregatedReport(List agaList, Boolean showSpecialPE,
                                        def domainName, String callingScreen,
                                        Boolean isExport, Boolean isArchived,
                                        String dataAnalysisLabel = "", Map analysisStatusJson = [:],
                                        List requiredFields = []) {
        fillSmqMap()
        userTimezone = userService.getCurrentUserPreference()?.timeZone

        // Extracting IDs from agaList
        List<Long> alertIdList = agaList.collect { it.id }
        List<Long> globalIdentityIdList = agaList.collect { it.globalIdentityId }
        Set alertExConfigIdList = agaList.collect { it.executedAlertConfigurationId }
        def isAttachmentList = isAttachmentMap(alertIdList,isArchived? "com.rxlogix.signal.ArchivedAggregateCaseAlert": "com.rxlogix.signal.AggregateCaseAlert")
        List<Map> alertTagNameList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags(globalIdentityIdList, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, callingScreen)
        List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList, domainName)
        def alertCommentList = alertCommentService.getAlertCommentByConfigIdList(alertExConfigIdList, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        List<Long> undoableAlertIdList = (isExport || isArchived) ? [] : undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertType.AGGREGATE_NEW)
        // Processing data
        Map<Long, List<Map>> tagMap = [:]
        Map<Long, List<Map>> valSignalMap = [:]

        for (Long id : alertIdList) {
            List<Map> tags = alertTagNameList.findAll { it.alertId == id }
            tagMap.put(id, tags)

            List<Map> valSignals = alertValidatedSignalList.findAll { v -> v.id == id }
                    ?.collect { v -> [name: v.name + "(S)", signalId: v.signalId, disposition: v.disposition] }
            valSignalMap.put(id, valSignals)
        }

        Map<Long, List<Map>> globalTagMap = [:]
        for (Long id : globalIdentityIdList) {
            List<Map> globalTags = globalTagNameList.findAll { gt -> gt.globalId == id }.unique { it.tagText }
            globalTagMap.put(id, globalTags)
        }
        // Building result list
        List<Map> result = agaList.stream().map { aga ->
            List imgList = aga?.impEvents?.split(',')
            List<Map> peAlertTags = tagMap.getOrDefault(aga.id, Collections.emptyList())
            List<Map> globalTags = globalTagMap.getOrDefault(aga.globalIdentityId, Collections.emptyList())
            List<Map> allTags = (peAlertTags ?: []) + (globalTags ?: [])
            List<Map> tagNameList = allTags.sort { tag1, tag2 -> tag1.priority <=> tag2.priority }

            AlertComment commentObj = alertCommentList.find {
                aga.productId == it.productId && aga.pt == it.eventName && it.exConfigId == aga.executedAlertConfigurationId
            }
            String comments = commentObj?.comments ?: null

            boolean isAttached = isAttachmentList.contains(aga.id)
            Map ptMap = [isIme: imgList?.contains('ime') ?"true": "false", isDme: imgList?.contains('dme') ?"true": "false", isEi: imgList?.contains('ei') ?"true": "false", isSm: imgList?.contains('sm') ?"true": "false"]

            if (requiredFields.isEmpty()) {
                aga.toDto(userTimezone, showSpecialPE, '-', smqMap, ptMap, tagNameList, valSignalMap.getOrDefault(aga.id, Collections.emptyList()), comments, isAttached, isExport, undoableAlertIdList.contains(aga.id) ?: false, commentObj?.id)
            } else {
                Map parameter = [
                        "executedConfigId"            : Holders.config.custom.thirdParty.executedConfigId,
                        "baseId"                      : Holders.config.custom.thirdParty.baseId,
                        "meddraPtCode"                : Holders.config.custom.thirdParty.meddraPtCode,
                        "dateRangeTypeFlag"           : Holders.config.custom.thirdParty.dateRangeTypeFlag,
                        "termScope"                   : Holders.config.custom.thirdParty.termScope,
                        "flag"                        : Holders.config.custom.thirdParty.flag,
                        "queryParam"                  : Holders.config.custom.thirdParty.queryParam,
                        "customAnalysisThirdPartyUrl" : Holders.config.custom.thirdParty.analysis.url,
                        "isCustomAnalysisEnabled"     : Holders.config.custom.thirdParty.analysis.enabled,
                        "pvrIntegrate"                : Holders.config.pvreports.url? true : false,
                        "hasReviewerAccess"           : hasReviewerAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT),
                        "isCaseSeriesAccess"          : SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION"),
                        "customAnalysisLabel"         : Holders.config.custom.thirdParty.analysis.label,
                        "spotfireEnabled"             : Holders.config.signal.spotfire.enabled,
                        "templateListUrl"             : createHref("template", "index", null),
                        "singleCaseUrl"               : createHref("singleCaseAlert", "caseSeriesDetails", null)
                ]

                aga.toRestDto(userTimezone, showSpecialPE, '-', smqMap, ptMap, tagNameList, valSignalMap.getOrDefault(aga.id, Collections.emptyList()),
                        comments, isAttached, isExport, undoableAlertIdList.contains(aga.id) ?: false, commentObj?.id, requiredFields, parameter,
                        dataAnalysisLabel, analysisStatusJson,isArchived)
            }
        }.collect(Collectors.toList())

        return result
    }



    def fetchValuesForEvdasReport(List evdasList, def domainName, Boolean isExport = false, Boolean isArchived = false) {
        userTimezone = userService.getCurrentUserPreference()?.timeZone
        def t1 = System.currentTimeMillis()
        def list = []
        def ime = false, dme = false, ei = false, sm = false
        def returnValue = []
        List<Long> alertIdList = evdasList.collect {it.id}
        Set alertExecutedConfigIdList = evdasList.collect {it.executedAlertConfigurationId}
        List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList,domainName)
        def alertCommentList = alertCommentService.getAlertCommentByConfigIdList(alertExecutedConfigIdList,Constants.AlertConfigType.EVDAS_ALERT)
        List isAttachmentList = isAttachmentMap(alertIdList,isArchived? "com.rxlogix.config.ArchivedEvdasAlert": "com.rxlogix.config.EvdasAlert")
        List<Long> undoableAlertIdList =  undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertType.EVDAS)

        ExecutorService executorService = signalExecutorService.threadPoolForEvdasListExec()
        List<Future> futureList = evdasList.collect { evdas ->
            executorService.submit({ ->
                List imgList = evdas?.impEvents?.split(',')
                List<Map> validatedSignals = alertValidatedSignalList.findAll {
                    it.id == evdas.id
                }?.collect { [name: it.name + "(S)", signalId: it.signalId,disposition:it.disposition] }

                List matchedComments = alertCommentList.findAll{
                    evdas.substance == it.productName && evdas.pt == it.eventName && it.exConfigId == evdas.executedAlertConfigurationId
                }?.sort{it.id}?.reverse()
                String comment = matchedComments.size()>0 ? matchedComments.getAt(0).comments : null
                Long commentId = matchedComments.size()>0 ? matchedComments.getAt(0).id : null
                Boolean isAttached= isAttachmentList.contains(evdas.id)
                Map ptMap = [isIme: imgList?.contains('ime') ? "true" : "false",
                             isDme: imgList?.contains('dme') ? "true" : "false",
                             isEi : imgList?.contains('ei') ? "true" : "false",
                             isSm : imgList?.contains('sm') ? "true" : "false"]
            evdas.toDto(userTimezone, null, null,ptMap,null,validatedSignals,comment,isAttached, isExport, undoableAlertIdList.contains(evdas.id)?:false,commentId)
            } as Callable)
        }
        futureList.each {
            list.add(it.get())
        }
        list
    }

    //need To change this from Config file
    def eventNameList1 = ["Rash", "Victim of crime", "Vertigo", "Vaginal haemorrhage", "Urinary tract infection"]
    def eventNameList2 = ["Cough", "Nausea", "Fracture", "Death", "Asthma"]
    def smqMapping = ["Pseudomembranous colitis": eventNameList1, "Malignancies": eventNameList2]
    TreeMap smqMap = new TreeMap()

    def fillSmqMap() {
        smqMapping.each { smqName, eventList ->
            eventList.each {
                smqMap.put(it, smqName)
            }
        }
    }
    def isAttachmentMap(List<Long> alertIdList, String referenceType){
        Sql sql
        def result
        try {
            String whereClause = ""

            alertIdList.collate(999).each{
                if(whereClause == ""){
                    whereClause += " reference_id IN (${it.join(',')})"
                }
                else{
                    whereClause += " OR reference_id IN  (${it.join(',')})"
                }
            }
            sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
            String sqlQuery = """
        Select reference_id as referenceId from attachment_link where reference_class = '${referenceType}' and"""+whereClause
        result = sql.rows(sqlQuery).collect { it.referenceId as Long}

        } catch (Exception ex){
            ex.printStackTrace()
        } finally {
            sql?.close()
        }

        return result
    }

}
