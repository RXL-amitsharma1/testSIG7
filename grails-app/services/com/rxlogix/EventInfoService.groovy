package com.rxlogix

import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.signal.*
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import grails.util.Holders
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.time.DateUtils


@Transactional
class EventInfoService implements AlertUtil{

    def grailsApplication
    def userService
    def evdasHistoryService
    def productEventHistoryService
    def workflowRuleService
    def priorityService
    def validatedSignalService
    def safetyLeadSecurityService
    def alertService


    Map fetchEventDetailMap(Long alertId, String alertType, Boolean isArchived, Boolean isAlertScreen) {
        Map eventDetailMap = getEventInfoMap(alertId, alertType, isArchived)
        User user = userService.getUser()
        String timezone = user?.preference?.timeZone
        List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone)
        Map<Disposition, ArrayList> dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        List currentDispositionOptions = dispositionIncomingOutgoingMap[eventDetailMap.alertDetailMap.disposition]
        Boolean forceJustification = user?.workflowGroup?.forceJustification
        List<Map> availablePriorities = priorityService.listPriorityOrder()
        List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        Boolean isPriorityChangeAllowed = !alertService.isProductSecurity() || safetyLeadSecurityService.allAllowedProductsForUser(userService.getCurrentUserId())?.contains(eventDetailMap?.alertDetailMap?.productName)
        def aggregateCaseAlert = isArchived ? ArchivedAggregateCaseAlert.get(alertId) : AggregateCaseAlert.get(alertId)
        if (alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
            if (aggregateCaseAlert?.executedAlertConfiguration?.productGroupSelection) {
                isPriorityChangeAllowed = true
            }
        }
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        Map flagsMap = fetchFlagsForEventDetail()
        def alert = getAlertObject(alertType, alertId, isArchived)
        String groupSelected = alert.executedAlertConfiguration.productGroupSelection
        boolean groupBySmq = false
        if(alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) && alert.alertConfiguration.groupBySmq){
            groupBySmq = aggregateCaseAlert?.alertConfiguration.groupBySmq
        }
        Boolean hasAlertReviewerAccess = false
        String buttonClass = ""
        if(isAlertScreen){
            if(alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                hasAlertReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            } else if(alertType == Constants.AlertConfigType.EVDAS_ALERT){
                hasAlertReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.EVDAS_ALERT)
            }
            buttonClass = hasAlertReviewerAccess ? "" : "hidden"
        }
        eventDetailMap << [dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                           currentDispositionOptions           : currentDispositionOptions,
                           forceJustification                  : forceJustification,
                           availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                           availablePriorities                 : availablePriorities,
                           actionConfigList                    : actionConfigList,
                           flagsMap                            : flagsMap,
                           actionTypeList                      : actionTypeAndActionMap?.actionTypeList,
                           actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap?.actionPropertiesMap),
                           isPriorityChangeAllowed             : groupSelected != 'null' ? true : isPriorityChangeAllowed,
                           isArchived                          : isArchived,
                           groupBySmq                          : groupBySmq,
                           hasReviewerAccess                   : hasAlertReviewerAccess,
                           buttonClass                         : buttonClass,
                           hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                           hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                           isPriorityEnabled                   : grailsApplication.config.alert.priority.enable,

        ]
        return eventDetailMap
    }


    Map getEventInfoMap(Long alertId, String type, Boolean isArchived) {
        def alert = getAlertObject(type, alertId, isArchived)
        String listed = ""
        if (type.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
            listed = alert?.listed
        } else {
            listed = alert?.listedness == true ? 'Yes' : (alert?.listedness == false ? 'No' : 'N/A')
        }
        Map alertDetailMap = getAlertDetailMap(alert, type)

        Map eventInfoMap
        eventInfoMap = [eventInformation: [[productName: alert?.productName, soc: alert?.soc, pt: alert?.pt, listed: listed]]
        ]
        List keys = []
        eventInfoMap.each { k, v ->
            keys << [key: k, title: "${k?.toLowerCase()?.replaceAll("\\s", "_")}_container"]
        }

        if(alert?.soc.equals(Constants.Commons.UNDEFINED)){
            alert?.soc = 'N/A'
        }
        def dataKeys = JsonOutput.toJson(keys)
        Map eventDetailMap = [data          : eventInfoMap, alertId: alertId,
                              alertDetailMap: alertDetailMap, dataKeys: dataKeys]
        return eventDetailMap
    }


    Map getAlertDetailMap(def alert, String type) {
        Map alertDetail = [:]
        if (alert) {
            alertDetail = [
                    alertId                  : alert?.id,
                    execConfigId             : alert?.executedAlertConfigurationId,
                    configId                 : alert?.alertConfigurationId,
                    priority                 : [value: alert?.priority?.value, iconClass: alert?.priority?.iconClass],
                    disposition              : alert?.disposition?.displayName,
                    dispositionId            : alert?.dispositionId,
                    isValidationStateAchieved: alert?.disposition?.validatedConfirmed,
                    productName              : alert?.productName,
                    appType                  : type,
                    dataSource               : (type.equals(Constants.AlertConfigType.EVDAS_ALERT)) ? Constants.DataSource.EUDRA : alert?.alertConfiguration?.selectedDatasource,
                    pt                       : alert?.pt,
                    ptCode                   : alert?.ptCode,
                    productId                : (type.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) ? alert?.productId : alert?.substanceId,
                    assignedTo               : alert.assignedToId ? alert.getUserByUserId(alert.assignedToId).toMap() : alert.assignedToGroupId ? alert.getGroupByGroupId(alert.assignedToGroupId).toMap():[:],
            ]
        }
        return alertDetail
    }

    def getAlertObject(String type, Long alertId, Boolean isArchived = false) {
        def alert = null
        if (type.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
            alert = isArchived ? ArchivedAggregateCaseAlert.get(alertId) : AggregateCaseAlert.get(alertId)
        } else {
            alert = isArchived ? ArchivedEvdasAlert.get(alertId) : EvdasAlert.get(alertId)
        }
        return alert
    }

    Map getStandardAlertsList(String productName) {
        def alertProductMap = grailsApplication.config.standard.alerts.mappings
        Map resultMap = [:]

        def tempMap = alertProductMap.find { it.productName == productName }

        if (tempMap) {
            List alertsList = tempMap.standardAlerts

            alertsList.each {
                resultMap.put(it.dataSource, it.alertName)
            }
        }
        return resultMap
    }

    Map getDetailsMapForEvdas(Map countData, String alertName, String productName, String eventName) {

        List prevExecs = ExecutedEvdasConfiguration.findAllByNameAndIsDeleted(alertName, false, [sort: "id", order: "asc"])
        int prevCounts = Holders.config.previous.alerts.count.evdas.charts

        prevExecs = prevExecs?.unique {
            it.dateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.dateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.dateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)
        if (prevExecs) {
            ExecutedEvdasConfiguration currentExecutedConfig = prevExecs.get(0)
            Double allRor
            List prevAlertList = []
            EvdasAlert latestAlert = EvdasAlert.findByExecutedAlertConfigurationAndSubstanceAndPt(currentExecutedConfig, productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedEvdasAlert.findByExecutedAlertConfigurationAndSubstanceAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { prevAlert ->
                allRor = prevAlert.allRor ? Double.valueOf(prevAlert.allRor) : 0
                countData.newEvpm_ev.add(prevAlert?.newEv ?: 0)
                countData.totalEvpm_ev.add(prevAlert?.totalEv ?: 0)
                countData.ime_ev.add(prevAlert?.dmeIme ?: Constants.Commons.BLANK_STRING)
                countData.dme_ev.add(prevAlert?.dmeIme ?: Constants.Commons.BLANK_STRING)
                countData.newFatal_ev.add(prevAlert?.newFatal ?: 0)
                countData.totalFatal_ev.add(prevAlert?.totalFatal ?: 0)
                countData.newPaed_ev.add(prevAlert?.newPaed ?: 0)
                countData.totalPaed_ev.add(prevAlert?.totPaed ?: 0)
                countData.sdrPaed_ev.add(prevAlert?.sdrPaed ?: 0)
                countData.changes_ev.add(prevAlert?.changes ?: 0)
                countData.rorAll_ev.add(allRor)
                countData.xAxisTitle_ev.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"))
            }
        }
        return countData
    }

    Map getDetailsMapForEvdasIntegratedReview(Map countData, String alertName, String productName, String eventName) {

        List prevExecs = ExecutedConfiguration.findAllByNameAndIsDeleted(alertName, false, [sort: "id", order: "asc"])
        int prevCounts = Holders.config.previous.alerts.count.evdas.charts

        prevExecs = prevExecs?.unique {
            it.executedAlertDateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.executedAlertDateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)
        if (prevExecs) {
            ExecutedConfiguration currentExecutedConfig = prevExecs.get(0)
            Double allRor
            def newEvpm
            def totalEvpm
            def ime
            def dme
            def newFatal
            def totalFatal
            def newPaed
            def totalPaed
            def sdrPaed
            def changes
            List prevAlertList = []
            AggregateCaseAlert latestAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(currentExecutedConfig, productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedAggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { AggregateCaseAlert prevAlert ->
                allRor = prevAlert.getEvdasColumnValue("allRorEvdas") ? Double.valueOf(prevAlert.getEvdasColumnValue("allRorEvdas")) : 0
                newEvpm = prevAlert?.getEvdasColumnValue("newEvEvdas")
                countData.newEvpm_ev.add(newEvpm ? (Double.valueOf(newEvpm)!=-1?Double.valueOf(newEvpm):0) : 0)
                totalEvpm = prevAlert?.getEvdasColumnValue("totalEvEvdas")
                countData.totalEvpm_ev.add(totalEvpm ? (Double.valueOf(totalEvpm)!=-1?Double.valueOf(totalEvpm):0) : 0)
                dme = prevAlert?.getEvdasColumnValue("dmeImeEvdas")
                countData.ime_ev.add(dme ? (Double.valueOf(dme)!=-1?Double.valueOf(dme):0) : 0)
                ime = prevAlert?.getEvdasColumnValue("dmeImeEvdas")
                countData.dme_ev.add(ime ? (Double.valueOf(ime)!=-1?Double.valueOf(ime):0) : 0)
                newFatal = prevAlert?.getEvdasColumnValue("newFatalEvdas")
                countData.newFatal_ev.add(newFatal ? (Double.valueOf(newFatal)!=-1?Double.valueOf(newFatal):0) : 0)
                totalFatal = prevAlert?.getEvdasColumnValue("totalFatalEvdas")
                countData.totalFatal_ev.add(totalFatal ? (Double.valueOf(totalFatal)!=-1?Double.valueOf(totalFatal):0) : 0)
                newPaed = prevAlert?.getEvdasColumnValue("totalFatalEvdas")
                countData.newPaed_ev.add(newPaed ? (Double.valueOf(newPaed)!=-1?Double.valueOf(newPaed):0) : 0)
                totalPaed = prevAlert?.getEvdasColumnValue("totPaedEvdas")
                countData.totalPaed_ev.add(totalPaed ? (Double.valueOf(totalPaed)!=-1?Double.valueOf(totalPaed):0) : 0)
                countData.sdrPaed_ev.add(prevAlert?.getEvdasColumnValue("sdrPaedEvdas") ?: 0)
                countData.changes_ev.add(prevAlert?.getEvdasColumnValue("changesEvdas") ?: 0)
                countData.rorAll_ev.add(allRor)
                countData.xAxisTitle_ev.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"))
            }
        }
        return countData
    }


    List getDetailsListForEvdas(String alertName, String productName, String eventName) {
        List countData = []
        List prevExecs = ExecutedEvdasConfiguration.findAllByName(alertName, [sort: "id", order: "desc"])

        int prevCounts = Holders.config.previous.alerts.count.evdas.charts

        prevExecs = prevExecs?.unique {
            it.dateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.dateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.dateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)
        if (prevExecs) {
            ExecutedEvdasConfiguration currentExecutedConfig = prevExecs.get(0)
            List prevAlertList = []
            EvdasAlert latestAlert = EvdasAlert.findByExecutedAlertConfigurationAndSubstanceAndPt(currentExecutedConfig, productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedEvdasAlert.findByExecutedAlertConfigurationAndSubstanceAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { prevAlert ->

                countData << [
                        xAxisTitle_ev: DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"),
                        id_ev        : prevAlert?.id ?: 0,
                        newEvpm_ev   : prevAlert?.newEv ?: 0,
                        totalEvpm_ev : prevAlert?.totalEv ?: 0,
                        ime_ev       : prevAlert?.dmeIme ?: Constants.Commons.DASH_STRING,
                        dme_ev       : prevAlert?.dmeIme ?: Constants.Commons.DASH_STRING,
                        newFatal_ev  : prevAlert?.newFatal ?: 0,
                        totalFatal_ev: prevAlert?.totalFatal ?: 0,
                        newPaed_ev   : prevAlert?.newPaed ?: 0,
                        totalPaed_ev : prevAlert?.totPaed ?: 0,
                        sdrPaed_ev   : prevAlert?.sdrPaed ?: 0,
                        sdr_ev       : prevAlert?.sdr ?: 0,
                        changes_ev   : prevAlert?.changes ?: 0,
                        rorAll_ev    : prevAlert?.allRor ?: 0
                ]
            }
        }
        return countData
    }

    List getDetailsListForEvdasIntegratedReview(String alertName, String productName, String eventName) {
        List countData = []
        List prevExecs = ExecutedConfiguration.findAllByName(alertName, [sort: "id", order: "desc"])

        int prevCounts = Holders.config.previous.alerts.count.evdas.charts

        prevExecs = prevExecs?.unique {
            it.executedAlertDateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.executedAlertDateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)
        if (prevExecs) {
            ExecutedConfiguration currentExecutedConfig = prevExecs.get(0)
            List prevAlertList = []
            AggregateCaseAlert latestAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(currentExecutedConfig, productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedAggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { prevAlert ->
                countData << [
                        xAxisTitle_ev    : DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"),
                        id_ev            : prevAlert?.id ?: 0,
                        newEvpm_ev       : prevAlert?.getEvdasColumnValue("newEvEvdas") ?: 0,
                        totalEvpm_ev     : prevAlert?.getEvdasColumnValue("totalEvEvdas") ?: 0,
                        ime_ev           : prevAlert?.getEvdasColumnValue("dmeImeEvdas") ?: Constants.Commons.DASH_STRING,
                        dme_ev           : prevAlert?.getEvdasColumnValue("dmeImeEvdas") ?: Constants.Commons.DASH_STRING,
                        newFatal_ev      : prevAlert?.getEvdasColumnValue("newFatalEvdas") ?: 0,
                        totalFatal_ev    : prevAlert?.getEvdasColumnValue("totalFatalEvdas") ?: 0,
                        newPaed_ev       : prevAlert?.getEvdasColumnValue("newPaedEvdas") ?: 0,
                        totalPaed_ev     : prevAlert?.getEvdasColumnValue("totPaedEvdas") ?: 0,
                            sdrPaed_ev   : prevAlert?.getEvdasColumnValue("sdrPaedEvdas") ?: 0,
                            sdr_ev       : prevAlert?.getEvdasColumnValue("sdrEvdas") ?: 0,
                            changes_ev   : prevAlert?.getEvdasColumnValue("changesEvdas") ?: 0,
                            rorAll_ev    : prevAlert?.getEvdasColumnValue("allRorEvdas") ?: 0
                    ]
            }
        }
        return countData
    }


    Map getDetailsMapForPVA(Map countData, String alertName, String productName, String eventName) {

        List<ExecutedConfiguration> prevExecs = ExecutedConfiguration.findAllByNameAndIsDeleted(alertName, false, [sort: "id", order: "asc"])
        int prevCounts = Holders.config.previous.alerts.count.quantitative.charts

        prevExecs = prevExecs?.unique {
            it.executedAlertDateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.executedAlertDateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)
        if(prevExecs) {
            ExecutedConfiguration currentExecutedConfig = prevExecs.last()
            List prevAlertList = []
            AggregateCaseAlert latestAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(prevExecs.last(), productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedAggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { prevAlert ->
                countData.newFatal_pva.add(prevAlert?.newFatalCount ?(prevAlert?.newFatalCount!=-1?prevAlert?.newFatalCount:0): 0)
                countData.cummFatal_pva.add(prevAlert?.cumFatalCount ?(prevAlert?.cumFatalCount!= -1?prevAlert?.cumFatalCount:0): 0)
                countData.prr_pva.add(prevAlert?.prrValue?(prevAlert?.prrValue != -1?prevAlert?.prrValue:0):0)
                countData.ror_pva.add(prevAlert?.rorValue? (prevAlert?.rorValue !=-1?prevAlert?.rorValue:0):0)
                countData.newCount_pva.add(prevAlert?.newCount ?(prevAlert?.newCount !=-1?prevAlert?.newCount:0): 0)
                countData.cummCount_pva.add(prevAlert?.cummCount ?(prevAlert?.cummCount !=-1?prevAlert?.cummCount:0): 0)
                countData.xAxisTitle.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"))

            }
        }

        return countData
    }

    List getDetailsListForPVA(String alertName, String productName, String eventName) {
        List countData = []
        int prevCounts = Holders.config.previous.alerts.count.quantitative.charts

        List<ExecutedConfiguration> prevExecs = ExecutedConfiguration.findAllByName(alertName, [sort: "id", order: "desc"])

        prevExecs = prevExecs?.unique {
            it.executedAlertDateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.executedAlertDateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)
        if(prevExecs) {
            ExecutedConfiguration currentExecutedConfig = prevExecs.get(0)
            List prevAlertList = []
            AggregateCaseAlert latestAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(currentExecutedConfig, productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedAggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { prevAlert ->

                countData << [
                        xAxisTitle   : DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"),
                        newFatal_pva : prevAlert?.newFatalCount ?: 0,
                        cummFatal_pva: prevAlert?.cumFatalCount ?: 0,
                        prr_pva      : prevAlert?.prrValue ?: 0,
                        ror_pva      : prevAlert?.rorValue ?: 0,
                        newCount_pva : prevAlert?.newCount ?: 0,
                        cummCount_pva: prevAlert?.cummCount ?: 0
                ]
            }
        }

        return countData
    }


    Map getDetailsMapForFaers(Map countData, String alertName, String productName, String eventName,String dataSource) {

        def prevExecs
        if(dataSource.split(',').size()>1 && dataSource.contains('pva')) {
            prevExecs = ExecutedConfiguration.findAllByNameAndSelectedDatasourceNotEqualAndIsDeleted(alertName, 'faers', false,[sort: "id", order: "asc"])
        } else {
            prevExecs = ExecutedConfiguration.findAllByNameAndSelectedDatasourceAndIsDeleted(alertName, 'faers',false, [sort: "id", order: "asc"])
        }
        int prevCounts = Holders.config.previous.alerts.count.quantitative.charts

        prevExecs = prevExecs?.unique {
            it.executedAlertDateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.executedAlertDateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)
        if(prevExecs) {
            def newCount
            def cummCount
            def faersEB05
            def faersEB95
            ExecutedConfiguration currentExecutedConfig = prevExecs.get(0)
            List prevAlertList = []
            AggregateCaseAlert latestAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(currentExecutedConfig, productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedAggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { prevAlert ->
                newCount = prevAlert?.getFaersColumnValue("newCountFaers")
                countData.newCounts_faers.add(newCount ? (Double.valueOf(newCount)!=-1?Double.valueOf(newCount):0) : 0)
                cummCount = prevAlert?.getFaersColumnValue("cummCountFaers")
                countData.cummCounts_faers.add(cummCount ? (Double.valueOf(cummCount)!=-1?Double.valueOf(cummCount):0) : 0)
                faersEB05 = prevAlert?.getFaersColumnValue("eb05Faers")
                countData.eb05_faers.add(faersEB05 ? (Double.valueOf(faersEB05)!=-1?Double.valueOf(faersEB05):0) : 0)
                faersEB95 = prevAlert?.getFaersColumnValue("eb95Faers")
                countData.eb95_faers.add(faersEB95 ? (Double.valueOf(faersEB95)!=-1?Double.valueOf(faersEB95):0) : 0)
                countData.xAxisTitle_faers.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"))
            }
        }
        return countData
    }

    List getDetailsListForFaers(String alertName, String productName, String eventName, String dataSource) {

        List countData = []
        def prevExecs
        if(dataSource.split(',').size()>1 && dataSource.contains('pva')) {
            prevExecs = ExecutedConfiguration.findAllByNameAndSelectedDatasourceNotEqual(alertName, 'faers', [sort: "id", order: "desc"])
        } else {
            prevExecs = ExecutedConfiguration.findAllByNameAndSelectedDatasource(alertName, 'faers', [sort: "id", order: "desc"])
        }
        int prevCounts = Holders.config.previous.alerts.count.quantitative.charts

        prevExecs = prevExecs?.unique {
            it.executedAlertDateRangeInformation.dateRangeEndAbsolute ? DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH) : it.executedAlertDateRangeInformation.dateRangeEndAbsolute
        }?.takeRight(prevCounts)

        if(prevExecs) {
            ExecutedConfiguration currentExecutedConfig = prevExecs.get(0)
            List prevAlertList = []
            AggregateCaseAlert latestAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(currentExecutedConfig, productName, eventName)
            prevAlertList = [latestAlert]
            prevExecs.remove(currentExecutedConfig)
            if (prevExecs) {
                prevExecs.each { it ->
                    prevAlertList.add(ArchivedAggregateCaseAlert.findByExecutedAlertConfigurationAndProductNameAndPt(it, productName, eventName))
                }
            }
            prevAlertList = prevAlertList?.flatten()?.findAll()
            prevAlertList.each { prevAlert ->
                countData << [
                        xAxisTitle_faers: DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"),
                        newCounts_faers : prevAlert?.getFaersColumnValue("newCountFaers") ?: 0,
                        cummCounts_faers: prevAlert?.getFaersColumnValue("cummCountFaers") ?: 0,
                        eb05_faers      : prevAlert?.getFaersColumnValue("eb05Faers") ?: 0,
                        eb95_faers      : prevAlert?.getFaersColumnValue("eb95Faers") ?: 0
                ]
            }
        }
        return countData
    }


    List countMapData(Long alertId, String alertType, Boolean isArchived) {

        def alertObj = getAlertObject(alertType, alertId, isArchived)

        List countData = []
        List countData_ev = []
        List countData_faers = []
        List countData_pva = []

        if (alertObj) {
            def configuration = alertObj.alertConfiguration
            String productName = alertObj.productName
            String eventName = alertObj.pt
            String dataSource = (alertType == Constants.AlertConfigType.EVDAS_ALERT) ? Constants.DataSource.EUDRA : configuration.selectedDatasource

            Map resultMap = getStandardAlertsList(productName)
            resultMap[dataSource] = configuration.name
            resultMap.each { key, value ->
                key.split(',').each { it ->
                    if (it.equals(Constants.DataSource.EUDRA)) {
                        if (dataSource.split(',').size() == 1) {
                            countData_ev = getDetailsListForEvdas(value, productName, eventName)
                        } else {
                            countData_ev = getDetailsListForEvdasIntegratedReview(value, productName, eventName)
                        }
                    } else if (it.equals(Constants.DataSource.FAERS)) {
                        countData_faers = getDetailsListForFaers(value, productName, eventName,dataSource)
                    } else {
                        countData_pva = getDetailsListForPVA(value, productName, eventName)
                    }
                }
            }
        }

        countData = getPreviousCountsMap(countData_pva, countData_ev, countData_faers)
        return countData
    }

    List fetchOtherSourceHistory(String alertType, Long alertId, Boolean isArchived) {
        def alertObj = getAlertObject(alertType, alertId, isArchived)
        def configurationObj = alertObj.alertConfiguration
        String productName = alertObj.productName
        String eventName = alertObj.pt
        Map resultMap = getStandardAlertsList(productName)
        String dataSource = (alertType.equals(Constants.AlertConfigType.EVDAS_ALERT)) ? Constants.DataSource.EUDRA : configurationObj.selectedDatasource
        List historyList = []
        resultMap.each { key, value ->
            if (!key.equals(dataSource)) {
                if (key.equals(Constants.DataSource.EUDRA)) {
                    Map historyEV = evdasLatestHistory(value, eventName, productName)
                    if (historyEV) {
                        historyList << historyEV
                    }
                }
                if (key.equals(Constants.DataSource.PVA)) {
                    Map historyArgus = argusLatestHistory(value, eventName, productName)
                    if (historyArgus) {
                        historyList << historyArgus
                    }
                }
                if (key.equals(Constants.DataSource.FAERS)) {
                    Map historyFaers = faersLatestHistory(value, eventName, productName)
                    if (historyFaers) {
                        historyList << historyFaers
                    }
                }
            }
        }
        return historyList
    }

    Map evdasLatestHistory(String alertName, String eventName, String productName) {

        EvdasAlert evdasAlert = EvdasAlert.findByNameAndPtAndSubstance(alertName, eventName, productName)
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC

        Map historyMap = [:]

        if (evdasAlert) {

            EvdasConfiguration configObj = evdasAlert?.alertConfiguration
            Map evdasHistoryMap = evdasHistoryService.getEvdasHistoryByPEC(productName, eventName, configObj.id)?.toDto(userTimezone)

            historyMap = [
                    dataSource   : Constants.DataSource.DATASOURCE_EUDRA,
                    alertName    : alertName,
                    disposition  : evdasHistoryMap?.disposition ?: evdasAlert?.disposition?.displayName,
                    justification: evdasHistoryMap?.justification ?: Constants.Commons.DASH_STRING,
                    performedBy  : evdasHistoryMap?.createdBy ?: evdasAlert?.createdBy,
                    date         : evdasHistoryMap?.timestamp ?: DateUtil.toDateTimeString(evdasAlert?.dateCreated)
            ]
        }
        return historyMap
    }

    Map argusLatestHistory(String alertName, String eventName, String productName) {

        AggregateCaseAlert aggAlert = AggregateCaseAlert.findByNameAndPtAndProductName(alertName, eventName, productName)
        Map historyMap = [:]
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        if (aggAlert) {

            Configuration configObj = aggAlert?.alertConfiguration
            Map argusHistoryMap = productEventHistoryService.getPEHistoryByPEC(productName, eventName, configObj.id)?.toDto(userTimezone)
            historyMap = [
                    dataSource   : Constants.DataSource.DATASOURCE_PVA,
                    alertName    : alertName,
                    disposition  : argusHistoryMap?.disposition ?: aggAlert?.disposition?.displayName,
                    justification: argusHistoryMap?.justification ?: Constants.Commons.DASH_STRING,
                    performedBy  : argusHistoryMap?.createdBy ?: aggAlert?.createdBy,
                    date         : argusHistoryMap?.timestamp ?: DateUtil.toDateStringWithTime(aggAlert?.dateCreated, userTimezone)
            ]
        }
        return historyMap
    }


    Map faersLatestHistory(String alertName, String eventName, String productName) {

        AggregateCaseAlert aggAlert = AggregateCaseAlert.findByNameAndPtAndProductName(alertName, eventName, productName)
        Map historyMap = [:]
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC

        if (aggAlert) {

            Configuration configObj = aggAlert?.alertConfiguration
            Map faersHistoryMap = productEventHistoryService.getPEHistoryByPEC(productName, eventName, configObj.id)?.toDto(userTimezone)

            historyMap = [
                    dataSource   : Constants.DataSource.DATASOURCE_FAERS,
                    alertName    : alertName,
                    disposition  : faersHistoryMap?.disposition ?: aggAlert?.disposition?.displayName,
                    justification: faersHistoryMap?.justification ?: Constants.Commons.DASH_STRING,
                    performedBy  : faersHistoryMap?.createdBy ?: aggAlert?.createdBy,
                    date         : faersHistoryMap?.timestamp ?: DateUtil.toDateStringWithTime(aggAlert?.dateCreated, userTimezone)
            ]
        }
        return historyMap
    }


    Map fetchFlagsForEventDetail() {
        def configFileObj = grailsApplication.config

        [
                showFaers: configFileObj.signal.faers.enabled,
                showEvdas: configFileObj.signal.evdas.enabled,
                showPrr  : configFileObj.statistics.enable.prr,
                showRor  : configFileObj.statistics.enable.ror,
                showEbgm : configFileObj.statistics.enable.ebgm
        ]
    }

    List getPreviousCountsMap(List pvaList, List evdasList, List faersList){
        List countData = []

        Integer pvaListCount = pvaList.size()
        Integer evdasListCount = evdasList.size()
        Integer faersListCount = faersList.size()
        if(pvaListCount > 0){
            countData = prevCountListPVA(pvaList, evdasList, faersList, evdasListCount, faersListCount)
        }else if(pvaListCount == 0 && evdasListCount > 0){
            countData = prevCountListEvdas(evdasList, faersList, faersListCount)
        }else{
            countData = prevCountListFaers(faersList)
        }

        return countData
    }

    //Count data List is populated when PVA scores and counts are available
    List prevCountListPVA(List pvaList, List evdasList, List faersList, Integer evdasListCount, Integer faersListCount){
        List countData = []
        pvaList.eachWithIndex { def entry, int i ->
            if (i < evdasListCount) {
                entry << evdasList[i]
            } else if (i >= evdasListCount) {
                entry << [
                        id_ev        : Constants.Commons.DASH_STRING,
                        newEvpm_ev   : Constants.Commons.DASH_STRING,
                        totalEvpm_ev : Constants.Commons.DASH_STRING,
                        ime_ev       : Constants.Commons.DASH_STRING,
                        dme_ev       : Constants.Commons.DASH_STRING,
                        newFatal_ev  : Constants.Commons.DASH_STRING,
                        totalFatal_ev: Constants.Commons.DASH_STRING,
                        newPaed_ev   : Constants.Commons.DASH_STRING,
                        totalPaed_ev : Constants.Commons.DASH_STRING,
                        sdrPaed_ev   : Constants.Commons.DASH_STRING,
                        sdr_ev       : Constants.Commons.DASH_STRING,
                        changes_ev   : Constants.Commons.DASH_STRING,
                        rorAll_ev    : Constants.Commons.DASH_STRING
                ]
            }

            if (i < faersListCount) {
                entry << faersList[i]
            } else if (i >= faersListCount) {
                entry << [
                        newCounts_faers : Constants.Commons.DASH_STRING,
                        cummCounts_faers: Constants.Commons.DASH_STRING,
                        eb05_faers      : Constants.Commons.DASH_STRING,
                        eb95_faers      : Constants.Commons.DASH_STRING
                ]
            }
            countData << entry
        }
        return countData
    }

    //When PVA counts and Scores are not available but evdas counts are available
    List prevCountListEvdas(List evdasList, List faersList, Integer faersListCount){
        List countData = []
        evdasList.eachWithIndex { def entry, int i ->
            if (i < faersListCount) {
                entry << faersList[i]
            } else if (i >= faersListCount) {
                entry << [
                        newCounts_faers : Constants.Commons.DASH_STRING,
                        cummCounts_faers: Constants.Commons.DASH_STRING,
                        eb05_faers      : Constants.Commons.DASH_STRING,
                        eb95_faers      : Constants.Commons.DASH_STRING
                ]
            }

            entry << [
                    newFatal_pva    : Constants.Commons.DASH_STRING,
                    cummFatal_pva   : Constants.Commons.DASH_STRING,
                    prr_pva         : Constants.Commons.DASH_STRING,
                    ror_pva         : Constants.Commons.DASH_STRING,
                    newCount_pva    : Constants.Commons.DASH_STRING,
                    cummCount_pva   : Constants.Commons.DASH_STRING
            ]
            countData << entry
        }
    }

    //When only faers scores and counts are available and rest two are not present
    List prevCountListFaers(List faersList){
        List countData = []
        faersList.eachWithIndex { def entry, int i ->
            entry << [
                    newFatal_pva    : Constants.Commons.DASH_STRING,
                    cummFatal_pva   : Constants.Commons.DASH_STRING,
                    prr_pva         : Constants.Commons.DASH_STRING,
                    ror_pva         : Constants.Commons.DASH_STRING,
                    newCount_pva    : Constants.Commons.DASH_STRING,
                    cummCount_pva   : Constants.Commons.DASH_STRING,
                    id_ev           : Constants.Commons.DASH_STRING,
                    newEvpm_ev      : Constants.Commons.DASH_STRING,
                    totalEvpm_ev    : Constants.Commons.DASH_STRING,
                    ime_ev          : Constants.Commons.DASH_STRING,
                    dme_ev          : Constants.Commons.DASH_STRING,
                    newFatal_ev     : Constants.Commons.DASH_STRING,
                    totalFatal_ev   : Constants.Commons.DASH_STRING,
                    newPaed_ev      : Constants.Commons.DASH_STRING,
                    totalPaed_ev    : Constants.Commons.DASH_STRING,
                    sdrPaed_ev      : Constants.Commons.DASH_STRING,
                    sdr_ev          : Constants.Commons.DASH_STRING,
                    changes_ev      : Constants.Commons.DASH_STRING,
                    rorAll_ev       : Constants.Commons.DASH_STRING
            ]

            countData << entry
        }
    }
}

