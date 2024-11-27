package com.rxlogix.signal


import com.rxlogix.Constants
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.EvdasOnDemandAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.SignalStrategy
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import grails.util.Holders
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ReportFormat
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.HttpSession

@Secured(["isAuthenticated()"])
class EvdasOnDemandAlertController implements AlertController {

    def evdasOnDemandAlertService
    def dynamicReportService
    def userService
    def CRUDService
    def validatedSignalService
    def evdasAlertService
    def viewInstanceService
    def alertService
    def cacheService
    def signalAuditLogService

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def listAdhocConfig() {
        HttpSession session = request.getSession()
        session.setAttribute("evdasAdhoc",params["selectedAlertsFilter"])
        Group workflowGroup = userService.getUser().workflowGroup
        User currentUser = userService.getUser()
        String groupIds = currentUser.groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}.collect { it.id }.join(",")
        String selectedValuesForFilter = params["selectedAlertsFilter"]
        List<String> selectedFilterValues = (selectedValuesForFilter == "null" || selectedValuesForFilter == "") ? [] : selectedValuesForFilter?.substring(1,selectedValuesForFilter.length()-1).replaceAll("\"", "").split(",");
        List<ExecutedEvdasConfiguration> configList = ExecutedEvdasConfiguration.createCriteria().list {
            evdasConfigForEvdasAlert.delegate = delegate
            evdasConfigForEvdasAlert(workflowGroup?.id, groupIds, selectedFilterValues)
            order("lastUpdated", "desc")
        }

        List list = []
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        configList.each { ExecutedEvdasConfiguration c ->
            EvdasConfiguration configuration = EvdasConfiguration.findByNameAndAdhocRun(c.name, true)
            List dateRange = configuration?.dateRangeInformation?.getReportStartAndEndDate()
            Map va = [

                    id              : c.id,
                    name            : c.name,
                    version         : c.numOfExecutions,
                    description     : c.description,
                    productSelection: getNameFieldFromJson(c.productSelection),
                    dateRagne       : (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-"),
                    caseCount       : (EvdasOnDemandAlert.where { (executedAlertConfiguration == c) }).size(),
                    closedCaseCount : 0,
                    newCases        : (EvdasOnDemandAlert.where { (executedAlertConfiguration == c) }).size(),
                    lastExecuted    : DateUtil.stringFromDate(c.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                    lastModified    : DateUtil.stringFromDate(c.lastUpdated, DateUtil.DATEPICKER_FORMAT, timeZone),
                    IsShareWithAccess : userService.hasAccessShareWith()

            ]
            list.add(va)
        }
        Set executedSet = list as Set
        render(executedSet as JSON)
    }

    Closure evdasConfigForEvdasAlert = {  wfGroupId,  groupIds,  selectedFilterValues->
        eq("isLatest", true)
        eq("isDeleted", false)
        eq("adhocRun", true)
        eq("isEnabled", true)
        sqlRestriction("""CONFIG_ID IN 
           (${SignalQueryHelper.evdas_configuration_sql(getUserService().getCurrentUserId(), wfGroupId,
                groupIds,selectedFilterValues)}
           )""")
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def listByExecutedConfig(Boolean cumulative, Long id) {

        Map finalMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: [], configId: id]

        try {

            Integer totalColumns = 65
            Map filterMap = alertService.prepareFilterMap(params, totalColumns)
            Map orderColumnMap = alertService.prepareOrderColumnMap(params)
            ExecutedEvdasConfiguration executedConfig = ExecutedEvdasConfiguration.findByIdAndIsEnabled(id, true)
            String timeZone = userService.getCurrentUserPreference()?.timeZone

            AlertDataDTO alertDataDTO = new AlertDataDTO()
            alertDataDTO.params = params
            alertDataDTO.domainName = EvdasOnDemandAlert
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            alertDataDTO.configId = executedConfig?.id
            alertDataDTO.filterMap = filterMap
            alertDataDTO.orderColumnMap = orderColumnMap
            alertDataDTO.userId = userService.getUser().id
            alertDataDTO.cumulative = cumulative
            alertDataDTO.timeZone = timeZone
            alertDataDTO.length = params.int("length")
            alertDataDTO.start = params.int("start")

            Map filterCountAndList = alertService.getAlertFilterCountAndListOnDemandRuns(alertDataDTO)
            finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: filterCountAndList.totalFilteredCount, aaData: filterCountAndList.resultList, configId: id]
        } catch (Throwable th) {

            th.printStackTrace()
        }

        render finalMap as JSON
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def adhocDetails() {
        def id = params.configId
        if(params.callingScreen != Constants.Commons.DASHBOARD && id == -1){
            alertDeletionInProgress()
            return
        }
        Boolean cumulative = false
        def name = Constants.Commons.BLANK_STRING
        def dr = Constants.Commons.BLANK_STRING
        def endDate = Constants.Commons.BLANK_STRING
        List freqNames = []
        def listDateRange = []
        Boolean alertDeletionObject = false
        ExecutedEvdasConfiguration executedEvdasConfiguration = ExecutedEvdasConfiguration.findById(id)
        if(executedEvdasConfiguration) {
            alertDeletionObject = alertService.isDeleteInProgress(executedEvdasConfiguration?.configId as Long, Constants.AlertConfigType.EVDAS_ALERT_DEMAND) ?: false
        }
        if(params.callingScreen != Constants.Commons.DASHBOARD && alertDeletionObject){
            alertDeletionInProgress()
            return
        }
        if (params.callingScreen == Constants.Commons.REVIEW) {
            name = ExecutedEvdasConfiguration.findById(id)?.name
            def config = EvdasConfiguration.findByName(name)
            def dateRange = config?.dateRangeInformation?.getReportStartAndEndDate()
            if (config.dateRangeInformation.dateRangeEnum == config?.dateRangeInformation?.dateRangeEnum.CUMULATIVE) {
                def dateRangeEnd = ExecutedEvdasConfiguration.findById(id)?.dateCreated
                dr = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
                endDate = Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(dateRangeEnd)).format("dd/MMM/yy")
            } else {
                dr = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
                endDate = Date.parse("dd-MMM-yyyy", dateRange ? DateUtil.toDateString1(dateRange[1]) : "-").format("dd/MMM/yy")
            }
        }
        User currentUser = userService.getUser()
        String alertType = Constants.AlertConfigType.EVDAS_ALERT_DEMAND
        params.callingScreen = Constants.Commons.REVIEW
        def userList = User.list().sort { it.fullName?.toLowerCase() }
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType, params.viewId as Long)
        List<Map> fieldList = grailsApplication.config.signal.evdasOnDemandColumnList as List<Map>
        Boolean isShareFilterViewAllowed = currentUser.isAdmin()
        Boolean isViewUpdateAllowed = viewInstance?.isViewUpdateAllowed(currentUser)
        Boolean hasEvdasReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.EVDAS_ALERT)
        String buttonClass = hasEvdasReviewerAccess?"":"hidden"
        def evdasAdhocHelpMap =Holders.config.evdas.adhoc.helpMap
        render view: 'adhocDetails', model: [executedConfigId        : id,
                                             backUrl                 : request.getHeader('referer'),
                                             appName                 : Constants.AlertConfigType.EVDAS_ALERT_DEMAND,
                                             callingScreen           : params.callingScreen,
                                             freqNames               : freqNames,
                                             name                    : name,
                                             dateRange               : dr,
                                             listDr                  : listDateRange,
                                             userList                : userList,
                                             cumulative              : cumulative,
                                             viewInstance            : viewInstance ?: null,
                                             isShareFilterViewAllowed: isShareFilterViewAllowed,
                                             isViewUpdateAllowed     : isViewUpdateAllowed,
                                             filterMap               : viewInstance ? viewInstance.filters : "",
                                             columnIndex             : "",
                                             sortedColumn            : viewInstance ? viewInstance.sorting : "",
                                             advancedFilterView      : viewInstance?.advancedFilter ? alertService.fetchAdvancedFilterDetails(viewInstance.advancedFilter) : "",
                                             viewId                  : viewInstance ? viewInstance.id : "",
                                             fieldList               : fieldList.sort({it.display.toUpperCase()}),
                                             alertType               : alertType,
                                             appType                 : alertType,
                                             buttonClass             : buttonClass,
                                             evdasAdhocHelpMap       : evdasAdhocHelpMap
        ]
    }
    void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def exportEVDASOnDemandCaseListColumns(){
        String sessionId = request.getSession().getId()
        List<Map> filteredDrillDownData = cacheService.getEvdasDrillDownData(sessionId)
        Long evdasAlertId = cacheService.getEvdasAlertId(sessionId)
        EvdasOnDemandAlert evdasOnDemandAlert=EvdasOnDemandAlert.get(evdasAlertId)
        filteredDrillDownData = evdasAlertService.getFinalDrillDownData(filteredDrillDownData, params)
        StringBuilder evdasEntityValue = new StringBuilder()
        params.criteriaSheetList = evdasAlertService.createCriteriaSheetListDrillDown(params,evdasAlertId,false,evdasEntityValue)
        def reportFile = dynamicReportService.createEVDASDrillDownReport(new JRMapCollectionDataSource(filteredDrillDownData), params)
        renderReportOutputType(reportFile, evdasOnDemandAlertService.getCaseListFileName(evdasAlertId))
        signalAuditLogService.createAuditForExport(params.criteriaSheetList, evdasEntityValue.toString(), Constants.AuditLog.EVDAS_REVIEW, params, reportFile.name)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def exportReport() {

        Boolean cumulative = false
        Boolean isExport = true
        List evdasList
        ExecutedEvdasConfiguration executedConfig = null
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.userId = userService.getUser().id
        alertDataDTO.cumulative = cumulative
        boolean isExcelExport = isExcelExportFormat(params.outputFormat)

        if (params.selectedCases) {

            List eaList = evdasAlertService.listSelectedAlerts(params.selectedCases)
            executedConfig = ExecutedEvdasConfiguration.findByIdAndIsEnabled(params.id, true)
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            evdasList = evdasAlertService.fetchEvdasAlertList(eaList, alertDataDTO)
        } else {

            Map filterMap = [:]
            if (params.filterList && params.filterList != "{}") {

                JsonSlurper jsonSlurper = new JsonSlurper()
                filterMap = jsonSlurper.parseText(params.filterList)
            }

            if ((!cumulative && params.callingScreen != Constants.Commons.DASHBOARD) || params.adhocRun.toBoolean()) {

                executedConfig = ExecutedEvdasConfiguration.findByIdAndIsEnabled(params.id, true)
            }

            alertDataDTO.domainName = EvdasOnDemandAlert
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            alertDataDTO.configId = executedConfig?.id
            alertDataDTO.filterMap = filterMap
            alertDataDTO.orderColumnMap = [name: "substance", dir: "asc"]
            alertDataDTO.isFromExport = true

            Map filterCountAndList = alertService.getAlertFilterCountAndListOnDemandRuns(alertDataDTO,isExport)
            evdasList = filterCountAndList.resultList
        }

        evdasList.each {
            it.name         = it?.alertName
            it.substance    = it?.productName
            it.pt           = it?.preferredTerm
            it.newFatal     = isExcelExport ? "" + it.newFatal       : "    " + it.newFatal   + "\n    " + it.totalFatal
            it.newSerious   = isExcelExport ? "" + it.newSerious     : "    " + it.newSerious + "\n    " + it.totalSerious
            it.newEv        = isExcelExport ? "" + it.newEv          : "    " + it.newEv      + "\n    " + it.totalEv
            it.newLit       = isExcelExport ? "" + it.newLit         : "    " + it.newLit     + "\n    " + it.totalLit
            it.newEea       = isExcelExport ? "" + it.newEea         : "    " + it.newEea     + "\n    " + it.totEea
            it.newHcp       = isExcelExport ? "" + it.newHcp         : "    " + it.newHcp     + "\n    " + it.totHcp
            it.newMedErr    = isExcelExport ? "" + it.newMedErr      : "    " + it.newMedErr  + "\n    " + it.totMedErr
            it.newObs       = isExcelExport ? "" + it.newObs         : "    " + it.newObs     + "\n    " + it.totObs
            it.newRc        = isExcelExport ? "" + it.newRc          : "    " + it.newRc      + "\n    " + it.totRc
            it.newPaed      = isExcelExport ? "" + it.newPaed        : "    " + it.newPaed    + "\n    " + it.totPaed
            it.newGeria     = isExcelExport ? "" + it.newGeria       : "    " + it.newGeria   + "\n    " + it.totGeria
            it.newSpont      = isExcelExport ? "" + it.newSpont       : "    " + it.newSpont   + "\n    " + it.totSpont

            if(isExcelExport) {
                it.totalFatal   = "" + it.totalFatal
                it.totalSerious = "" + it.totalSerious
                it.totalEv      = "" + it.totalEv
                it.totalLit     = "" + it.totalLit
                it.totEea       = "" + it.totEea
                it.totHcp       = "" + it.totHcp
                it.totMedErr    = "" + it.totMedErr
                it.totObs       = "" + it.totObs
                it.totRc        = "" + it.totRc
                it.totPaed      = "" + it.totPaed
                it.totGeria     = "" + it.totGeria
                it.totSpont     = "" + it.totSpont
            }
        }

        params.onDemand = true
        if(isExcelExport) {
            EvdasConfiguration config  =  EvdasConfiguration.findByName(executedConfig.name)
            List<Date> dateRange = executedConfig?.dateRangeInformation?.getReportStartAndEndDate()
            String reportDateRange
            if (config.dateRangeInformation.dateRangeEnum == config.dateRangeInformation?.dateRangeEnum.CUMULATIVE) {
                Date dateRangeEnd = executedConfig?.dateCreated
                reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
            }else {
                reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
            }
            Map criteriaData      =  [alertName      : executedConfig.name, productName: executedConfig.getProductNameList(),
                                      dateRange      : reportDateRange, cumulative: false]
            Map reportParamsMap   =  ["showCompanyLogo" : true, "showLogo" : true, "header" : "EVDAS On-Demand Review Report"]
            params << reportParamsMap
            params.criteriaData = criteriaData
        }
        params.totalCount = evdasList?.size()?.toString()
        List criteriaSheetList = evdasAlertService.getEvdasAlertCriteriaData(executedConfig, params, Constants.AlertConfigType.EVDAS_ALERT_DEMAND)
        params.criteriaSheetList = criteriaSheetList
        Boolean isLongComment = evdasList?.any({x -> x.comment?.size() > 100})
        params.isLongComment = isLongComment?: false
        File reportFile = dynamicReportService.createEvdasAlertsReport(new JRMapCollectionDataSource(evdasList), params)
        renderReportOutputType(reportFile)
        signalAuditLogService.createAuditForExport(criteriaSheetList, executedConfig.getInstanceIdentifierForAuditLog()+ ": Alert Details",Constants.AuditLog.ADHOC_EVDAS_REVIEW,params,reportFile.name)
    }

    private renderReportOutputType(File reportFile, String fileName = null) {

        String reportName = fileName ? fileName : "EVDAS Adhoc Alert" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())

        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER'])
    def toggleFlag() {

        long id = params.id as long
        if (id) {

            boolean flagged = evdasOnDemandAlertService.toggleEvdasOnDemandAlertFlag(id)
            render([success: 'ok', flagged: flagged] as JSON)
        } else {

            response.status = 404
        }
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def adhocReview() {
        def evdasAdhocReviewHelpMap = Holders.config.evdas.adhoc.review.helpMap
        render (view: "adhocReview",model:[evdasAdhocReviewHelpMap:evdasAdhocReviewHelpMap])
    }

    @Secured(['ROLE_SHARE_GROUP','ROLE_SHARE_ALL'])
    def editShare() {
        Boolean sharedWithoutException = true
        ExecutedEvdasConfiguration executedConfiguration
        if (params.sharedWith && params.executedConfigId) {
            executedConfiguration = ExecutedEvdasConfiguration.get(Long.parseLong(params.executedConfigId))
            EvdasConfiguration config = EvdasConfiguration.findByName(executedConfiguration.name)
            userService.bindSharedWithConfiguration(config, params.sharedWith, true)
            CRUDService.update(config)
        } else {
            sharedWithoutException = false
            log.info("No valid executed config id")
        }
        if(sharedWithoutException){
            flash.message = message(code: "app.configuration.share.success", args: [executedConfiguration?.name])
        }else{
            flash.warn = message(code: "app.configuration.share.warning", args: [executedConfiguration?.name])
        }
        redirect action: "adhocReview"
    }

    private boolean isExcelExportFormat(String outputFormat) {
        return outputFormat == ReportFormat.XLSX.name() ? true : false
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map<String, List> possibleValuesMap = [:]
        alertService.preparePossibleValuesMapForOnDemand(EvdasOnDemandAlert, possibleValuesMap, executedConfigId)
        List<String> yesNoFieldsList = ["sdr", "sdrPaed", "sdrGeratr"]
        List<Map<String, String>> yesNoMapList = [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
        yesNoFieldsList.each {
            possibleValuesMap.put(it, yesNoMapList)
        }
        possibleValuesMap.put("listedness", [[id: "true", text: "Yes"], [id: "false", text: "No"]])

        render possibleValuesMap as JSON
    }

    def fetchSubstanceFreqNames() {
        def ecFreqList = ExecutedEvdasConfiguration.findAllByFrequencyIsNotNull().collect { it.frequency }
        def freqNames = ecFreqList.unique()
        def frequencyList = SubstanceFrequency.findAllByAlertType("EVDAS Alert On Demand")
        frequencyList.each {
            if (!freqNames.contains(it.frequencyName)) {
                freqNames.add(it.frequencyName)
            }
        }
        freqNames
    }

    def delete(){
        ExecutedEvdasConfiguration executedEvdasConfiguration =  ExecutedEvdasConfiguration.get(params.id as Long)
        if(executedEvdasConfiguration) {
            try {
                evdasOnDemandAlertService.deleteOnDemandAlert(executedEvdasConfiguration)
                flash.message = message(code: "app.configuration.alert.delete.success", args: [executedEvdasConfiguration?.name])
            } catch (Exception e) {
                log.error(e.printStackTrace())
                flash.warn = message(code: "app.configuration.delete.fail", args: [executedEvdasConfiguration?.name])
            }
        }
        redirect(action: "adhocReview")
    }
}
