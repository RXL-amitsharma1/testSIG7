package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.SignalStrategy
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.HttpSession

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class AggregateOnDemandAlertController implements AlertController {

    def alertService
    def userService
    def dynamicReportService
    def CRUDService
    def aggregateOnDemandAlertService
    def pvsGlobalTagService
    def cacheService
    def spotfireService
    def reportIntegrationService
    def viewInstanceService
    def aggregateCaseAlertService
    def safetyLeadSecurityService
    def dataObjectService
    def signalAuditLogService
    def alertFieldService

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def listConfig() {
        Map resultMap = [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        try {
            HttpSession session = request.getSession()
            session.setAttribute("aggAdhoc",params["selectedAlertsFilter"])
            resultMap = generateResultMap(resultMap, AggregateOnDemandAlert)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(resultMap as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def listByExecutedConfig(Long id) {
        Map finalMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: [], configId: id]
        try {
            Integer totalColumns = 65
            Map filterMap = alertService.prepareFilterMap(params, totalColumns)
            Map orderColumnMap = alertService.prepareOrderColumnMap(params)
            AlertDataDTO alertDataDTO = createAlertDataDTO(filterMap, orderColumnMap, AggregateOnDemandAlert)
            Map filterCountAndList = alertService.getAlertFilterCountAndListOnDemandRuns(alertDataDTO)
            List<String> productNames = alertService.getDistinctProductName(alertDataDTO.domainName, alertDataDTO,params.callingScreen)
            finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: filterCountAndList.totalFilteredCount, aaData: filterCountAndList.resultList, configId: id, productNameList :productNames.sort({it.toUpperCase()})]
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render finalMap as JSON
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def exportReport() {
        Map filterMap = [:]
        List aggList = []
        boolean isExcelExport = isExcelExportFormat(params.outputFormat)
        if (params.filterList) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            filterMap = jsonSlurper.parseText(params.filterList)
        }
        AlertDataDTO alertDataDTO = createAlertDataDTO(filterMap, [name: params.column, dir: params.sorting], AggregateOnDemandAlert)
        alertDataDTO.isFromExport = true
        Map filterCountAndList = alertService.getAlertFilterCountAndListOnDemandRuns(alertDataDTO,true,params.selectedCases)
        params.onDemand = true
        Boolean groupBySmq = false
        ExecutedConfiguration executedConfig = ExecutedConfiguration.get(params.id)
        groupBySmq = executedConfig?.groupBySmq
        List subGroupingFields = Holders.config.subgrouping.pva.subGroupColumnsList?.keySet() as List
        if(isExcelExport) {
            String reportDateRange   =  DateUtil.toDateString1(executedConfig.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString1(executedConfig.executedAlertDateRangeInformation.dateRangeEndAbsolute)
            String productSelection = executedConfig?.productSelection ? ViewHelper.getDictionaryValues(executedConfig, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(executedConfig, DictionaryTypeEnum.PRODUCT_GROUP)
            String productName = ""
            if(executedConfig.dataMiningVariable){
                productName = executedConfig.dataMiningVariable
                if(productSelection){
                    productName = productName + "(" + productSelection + ")"
                }
            } else {
                productName = productSelection
            }
            Map criteriaData = [alertName                     : executedConfig?.name,
                                dataSource                    : executedConfig?.getDataSource(executedConfig?.selectedDatasource),
                                productName                   : productName,
                                dateRange                     : reportDateRange,
                                cumulative                    : false,
                                excludeFollowUp               : executedConfig?.excludeFollowUp ? "Yes" : "No",
                                missedcase                    : executedConfig?.missedCases ? "Yes" : "No",
                                groupBySmq                    : executedConfig?.groupBySmq ? "Yes" : "No",
                                includeMedicallyConfirmedCases: executedConfig?.includeMedicallyConfirmedCases ? "Yes" : "No",
                                limitToPrimaryPath            : executedConfig?.limitPrimaryPath ? "Yes" : "No",
                                rmpReference                  : executedConfig?.alertRmpRemsRef ?: "-",
                                scheduledBy                   : executedConfig?.owner?.fullName ?: "-",
                                evdasDateRange                : executedConfig?.evdasDateRange ?: "-",
                                faersDateRange                : executedConfig?.faersDateRange ?: "-",
                                onAndAfterDate                : executedConfig?.onOrAfterDate ? DateUtil.toDateString1(executedConfig?.onOrAfterDate) : "-"
            ]

            Map reportParamsMap   =  ["showCompanyLogo" : true, "showLogo" : true, "header" : "Quantitative On-Demand Review Report"]
            params << reportParamsMap
            params.criteriaData = criteriaData
        }
        List adhocColumnList = alertFieldService.getAggOnDemandColumnList(executedConfig?.selectedDatasource,groupBySmq).findAll{it.isNewColumn == true}
        List ebgmSubGroupingFields = ["ebgm","eb05","eb95"]
        List relativeSubGroupingFields = ["ror","rorLci","rorUci"]
        if (executedConfig?.selectedDatasource == "pva") {
            filterCountAndList.resultList.each {
                it << fetchEachSubCategory(Holders.config.subgrouping.ageGroup.name, '', it , "Age")
                it << fetchEachSubCategory(Holders.config.subgrouping.gender.name, '', it , "Gender")
                ebgmSubGroupingFields?.each{category ->
                    it << fetchAllSubCategoryColumn(category,null,'',it)
                }
                subGroupingFields?.each{category ->
                    it << fetchAllSubCategoryColumn(category,null,'',it)
                }
                relativeSubGroupingFields?.each{category ->
                    it << fetchAllSubCategoryColumn(category,null,"Rel",it)
                }
            }
        } else {
            filterCountAndList.resultList.each {
                it << fetchEachSubCategory(Holders.config.subgrouping.faers.ageGroup.name, '', it , "Age", params.adhocRun as Boolean)
                it << fetchEachSubCategory(Holders.config.subgrouping.faers.gender.name, '', it , "Gender", params.adhocRun as Boolean)
            }
        }
        aggList = filterCountAndList.resultList
        aggList.each {
            it.name                = it?.alertName
            it.pt                  = it.preferredTerm
            it.newSponCount        = isExcelExport ? "" + it.newSponCount    : "    " + it.newSponCount      + "\n    " + it.cumSponCount
            it.newCount            = isExcelExport ? "" + it.newCount             : "    " + it.newCount            + "\n    " + it.cummCount
            it.newPediatricCount   = isExcelExport ? "" + it.newPediatricCount    : "    " + it.newPediatricCount   + "\n    " + it.cummPediatricCount
            it.newInteractingCount = isExcelExport ? "" + it.newInteractingCount  : "    " + it.newInteractingCount + "\n    " + it.cummInteractingCount
            it.newGeriatricCount   = isExcelExport ? "" + it.newGeriatricCount    : "    " + it.newGeriatricCount   + "\n    " + it.cumGeriatricCount
            it.newNonSerious       = isExcelExport ? "" + it.newNonSerious        : "    " + it.newNonSerious       + "\n    " + it.cumNonSerious
            it.newSeriousCount     = isExcelExport ? "" + it.newSeriousCount : "    " + it.newSeriousCount   + "\n    " + it.cumSeriousCount
            it.newFatalCount       = isExcelExport ? "" + it.newFatalCount   : "    " + it.newFatalCount     + "\n    " + it.cumFatalCount
            it.newStudyCount       = isExcelExport ? "" + it.newStudyCount   : "    " + it.newStudyCount     + "\n    " + it.cumStudyCount
            it.prrLCI               = isExcelExport ? "" + it.prrLCI                : "    " + it.prrLCI               + "\n    " + it.prrUCI
            it.eb05                = isExcelExport ? "" + it.eb05 + ""       : "    " + it.eb05 + ""         + "\n    " + it.eb95 + ""
            it.rorLCI               = isExcelExport ? "" + it.rorLCI + ""       : "    " + it.rorLCI + ""         + "\n    " + it.rorUCI + ""
            it.prrLCI               = isExcelExport ? "" + it.prrLCI + ""       : "    " + it.prrLCI + ""         + "\n    " + it.prrUCI + ""
            it.ebgm                = "" + it.ebgm
            it.chiSquare           = it.chiSquare as String !="-" ? String.format("%s",it.chiSquare) : "-" + ""
            it.aValue = (it.aValue as String != '-' && it.aValue != -1)  ? ("" + (it.aValue as Integer)) : it.aValue != -1 ? it.aValue : '-'
            it.bValue = (it.bValue as String != '-' && it.bValue != -1) ? ("" + (it.bValue as Integer)) : it.bValue != -1 ? it.bValue : '-'
            it.cValue = (it.cValue as String != '-' && it.cValue != -1) ? ("" + (it.cValue as Integer)) : it.cValue != -1 ? it.cValue : '-'
            it.dValue = (it.dValue as String != '-' && it.dValue != -1) ? ("" + (it.dValue as Integer)) : it.dValue != -1 ? it.dValue : '-'
            it.eValue = "" + it.eValue
            it.rrValue = "" + it.rrValue
//            it.soc = dataObjectService.getAbbreviationMap(it.soc) + ""
            it.smqNarrow = it.smqNarrow ?  it.smqNarrow?.replaceAll("<br>", "\n")?.replaceAll("<BR>", "\n") : ""

            List <String> tagsList = []
            it.alertTags.each{tag->
                String tagString = ""
                if(tag.subTagText == null) {
                    tagString = tagString + tag.tagText + tag.privateUser + tag.tagType
                }
                else{
                    String subTags = tag.subTagText.split(";").join("(S);")
                    tagString = tagString + tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + "(S)"
                }
                tagsList.add(tagString)

            }
            it.alertTags = tagsList.join(", ")
            if(!isExcelExport){
                adhocColumnList.each{columnInfo ->
                    String name = columnInfo.name
                    String secondaryName = columnInfo.secondaryName
                    if(secondaryName) {
                        it."${name}" =  "    " + it."${name}" + "\n    " + it."${secondaryName}"
                    }
                }
            }

            if (isExcelExport) {
                it.cumGeriatricCount    = "" + it.cumGeriatricCount
                it.cumNonSerious        = "" + it.cumNonSerious
                it.cumStudyCount        = "" + it.cumStudyCount
                it.cummCount            = "" + it.cummCount
                it.cummPediatricCount   = "" + it.cummPediatricCount
                it.cummInteractingCount = "" + it.cummInteractingCount
                it.cumSponCount         = "" + it.cumSponCount
                it.cumSeriousCount      = "" + it.cumSeriousCount
                it.cumFatalCount        = "" + it.cumFatalCount
                it.cumStudyCount        = "" + it.cumStudyCount
                it.eb95                 = "" + it.eb95 + ""
                it.prrUCI                = "" + it.prrUCI
                it.rorUCI                = "" + it.rorUCI
            }
        }
        params.totalCount = aggList?.size()?.toString()
        List criteriaSheetList
        if(executedConfig.selectedDatasource == Constants.DataSource.JADER){
            criteriaSheetList = aggregateCaseAlertService.getJaderAggregateCaseAlertCriteriaData(executedConfig, params,Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND)
        }else {
            criteriaSheetList = aggregateCaseAlertService.getAggregateCaseAlertCriteriaData(executedConfig, params, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND)
        }
        params.criteriaSheetList = criteriaSheetList
        params.miningVariable = executedConfig?.dataMiningVariable
        params.groupBySmq = groupBySmq
        Boolean isLongComment = filterCountAndList.resultList?.any({x -> x.comment?.size() > 100})
        params.isLongComment = isLongComment?: false
        File reportFile = dynamicReportService.createAggregateAlertsReport(new JRMapCollectionDataSource(filterCountAndList.resultList), params)
        renderReportOutputType(reportFile)
        signalAuditLogService.createAuditForExport(criteriaSheetList, executedConfig.getInstanceIdentifierForAuditLog() + ": Alert Details", Constants.AuditLog.ADHOC_AGGREGATE_REVIEW, params, reportFile.name)

    }

    private renderReportOutputType(File reportFile) {
        String reportName = "Aggregate On Demand Alert" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }


    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def toggleFlag(Long id) {
        if (id) {
            Boolean flagged = alertService.toggleAlertFlag(id, AggregateOnDemandAlert)
            render([success: 'ok', flagged: flagged] as JSON)
        } else {
            response.status = 404
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def adhocReview() {
        def aggAdhocReviewHelpMap =Holders.config.aggregate.adhoc.review.helpMap
        render (view: "adhocReview", model:[roles: aggregateCaseAlertService.reviewRoles(),aggAdhocReviewHelpMap:aggAdhocReviewHelpMap])
    }

    def adhocDetails() {
        Long executedConfigId = params.long("configId")
        if(params.callingScreen != Constants.Commons.DASHBOARD && executedConfigId == -1){
            alertDeletionInProgress()
            return
        }
        Boolean alertDeletionObject = false
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId)
        if(executedConfiguration) {
            alertDeletionObject = alertService.isDeleteInProgress(executedConfiguration.configId as Long, executedConfiguration?.type) ?: false
        }
        Boolean cumulative = false
        String dateRange = Constants.Commons.BLANK_STRING
        Boolean groupBySmq = false
        Long configId = 0L
        String currentScreenType = ''


        if (executedConfiguration) {
            configId = executedConfiguration ? aggregateCaseAlertService.getAlertConfigObject(executedConfiguration) : 0L
            if(!aggregateCaseAlertService.detailsAccessPermission(executedConfiguration.selectedDatasource.split(","))){
                notFound()
                return
            }
        }else{
            notFound()
            return
        }
        if(params.callingScreen != Constants.Commons.DASHBOARD && alertDeletionObject){
            alertDeletionInProgress()
            return
        }

        String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND
        List freqNames = []

        String name = executedConfiguration?.name
        String dr = Constants.Commons.BLANK_STRING
        groupBySmq = executedConfiguration.groupBySmq
        if (groupBySmq) {
            alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND
        }
        if (params.callingScreen != Constants.Commons.DASHBOARD && !cumulative) {
            dateRange = DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation.dateRangeEndAbsolute)
            Date sd = executedConfiguration?.executedAlertDateRangeInformation.dateRangeEndAbsolute
            dr = Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(sd)).format("dd/MMM/yy")
        }

        if (executedConfiguration && executedConfiguration.selectedDatasource.startsWith(Constants.DataSource.FAERS) && !executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.EUDRA) && !executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.VIGIBASE)) {
            alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_FAERS : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_FAERS
        }

        if (executedConfiguration && executedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VAERS) && !executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.EUDRA) && !executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.VIGIBASE)) {
            alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_VAERS : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_VAERS
        }

        if (executedConfiguration && executedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE) && !executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.EUDRA)) {
            alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_VIGIBASE : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_VIGIBASE
        }
        if (executedConfiguration && executedConfiguration.selectedDatasource.startsWith(Constants.DataSource.JADER) ) {
            alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_JADER : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_JADER
        }

        if(executedConfiguration?.dataMiningVariable && executedConfiguration?.dataMiningVariable !="null"){
            alertType = alertType + "-" + executedConfiguration?.dataMiningVariable
        }
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType, params.viewId as Long)
        String dssUrl = grailsApplication.config.dss.url

        List<User> userList = User.findAllByEnabled(true).sort { it.fullName?.toLowerCase() }

        List subGroupsColumnsList = []
        List faersSubGroupsColumnsList = []
        Map subGroupMap = cacheService.getSubGroupMap()
        Map<String,String> prrRorSubGroupMap =  cacheService.allOtherSubGroupColumnUIList(Constants.DataSource.PVA)
        Map<String,String> relativeSubGroupMap = cacheService.relativeSubGroupColumnUIList(Constants.DataSource.PVA)
        subGroupMap[Holders.config.subgrouping.ageGroup.name]?.each { id, value ->
            subGroupsColumnsList.add(value)
        }
        subGroupMap[Holders.config.subgrouping.gender.name]?.each { id, value ->
            subGroupsColumnsList.add(value)
        }
        Boolean forceJustification = userService.user.workflowGroup?.forceJustification
        User currentUser = userService.getUser()
        Boolean isShareFilterViewAllowed = currentUser.isAdmin()
        Boolean isViewUpdateAllowed = viewInstance?.isViewUpdateAllowed(currentUser)

        subGroupMap[Holders.config.subgrouping.faers.ageGroup.name]?.each { id, value ->
            faersSubGroupsColumnsList.add(value)
        }
        subGroupMap[Holders.config.subgrouping.faers.gender.name]?.each { id, value ->
            faersSubGroupsColumnsList.add(value)
        }

        String selectedDatasource = executedConfiguration?.selectedDatasource
        String evdasDateRange = executedConfiguration?.evdasDateRange
        String faersDateRange = executedConfiguration?.faersDateRange
        String vaersDateRange = executedConfiguration?.vaersDateRange
        String vigibaseDateRange = executedConfiguration?.vigibaseDateRange
        List allowedProductsAsSafetyLead = alertService.isProductSecurity() ? safetyLeadSecurityService.allAllowedProductsForUser(userService.getCurrentUserId()) : []
        boolean isFaersEnabled = selectedDatasource.contains(Constants.DataSource.FAERS)
        boolean isVaersEnabled = selectedDatasource.contains(Constants.DataSource.VAERS)
        boolean isEvdasEnabled = selectedDatasource.contains(Constants.DataSource.EUDRA)
        boolean isPvaEnabled = selectedDatasource.contains(Constants.DataSource.PVA)
        boolean isVigibaseEnabled = selectedDatasource.contains(Constants.DataSource.VIGIBASE)
        boolean isJaderEnabled = selectedDatasource.contains(Constants.DataSource.JADER)
        boolean isDataMining = executedConfiguration.dataMiningVariable ? true : false
        List searchableColumnList = alertService.generateSearchableColumnsForOnDemandRuns(groupBySmq, isFaersEnabled, isPvaEnabled,isVaersEnabled, isVigibaseEnabled,isJaderEnabled,isDataMining)
        List<Map> fieldList = aggregateOnDemandAlertService.fieldListAdvanceFilter(isFaersEnabled, isPvaEnabled,isVaersEnabled, isVigibaseEnabled, isJaderEnabled, executedConfiguration?.dataMiningVariable, groupBySmq)
        boolean showPrr = false
        boolean showRor = false
        boolean showEbgm = false

        if(alertType.toLowerCase().contains("vaers") ) {
            showPrr = grailsApplication.config.statistics.vaers.enable.prr
        } else if(alertType.toLowerCase().contains("vigibase") ) {
            showPrr = grailsApplication.config.statistics.vigibase.enable.prr
        } else if(alertType.toLowerCase().contains("faers") ) {
            showPrr = grailsApplication.config.statistics.faers.enable.prr
        } else {
            showPrr = grailsApplication.config.statistics.enable.prr
        }
        if(alertType.toLowerCase().contains("vaers") ) {
            showRor = grailsApplication.config.statistics.vaers.enable.ror
        } else if(alertType.toLowerCase().contains("vigibase") ) {
            showRor = grailsApplication.config.statistics.vigibase.enable.ror
        } else if(alertType.toLowerCase().contains("faers") ) {
            showRor = grailsApplication.config.statistics.faers.enable.ror
        } else {
            showRor = grailsApplication.config.statistics.enable.ror
        }
        if(alertType.toLowerCase().contains("vaers") ) {
            showEbgm = grailsApplication.config.statistics.vaers.enable.ebgm
        } else if(alertType.toLowerCase().contains("vigibase") ) {
            showEbgm = grailsApplication.config.statistics.vigibase.enable.ebgm
        } else if(alertType.toLowerCase().contains("faers") ) {
            showEbgm = grailsApplication.config.statistics.faers.enable.ebgm
        } else {
            showEbgm = grailsApplication.config.statistics.enable.ebgm
        }

        Boolean hasAggReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        String buttonClass = hasAggReviewerAccess?"":"hidden"

        if(groupBySmq && isFaersEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_FAERS
        } else if(groupBySmq && isVaersEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_VAERS
        } else if(groupBySmq && isVigibaseEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_VIGIBASE
        } else if(groupBySmq && isJaderEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND_JADER
        } else if(groupBySmq){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND
        } else if(isFaersEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_FAERS
        } else if(isVaersEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_VAERS
        } else if(isVigibaseEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_VIGIBASE
        } else if(isJaderEnabled){
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND_JADER
        } else {
            currentScreenType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND
        }
        List adhocColumnList = alertFieldService.getAggOnDemandColumnList(selectedDatasource,groupBySmq)
        Map subGroupColumnInfo = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll{it.type=="subGroup" && it.enabled== true}.collectEntries {
            b -> [b.name, b.display]
        }
        prrRorSubGroupMap?.values()?.retainAll(subGroupColumnInfo?.keySet())
        relativeSubGroupMap?.values()?.retainAll(subGroupColumnInfo?.keySet())

        Map<String , Map> statusMap =  spotfireService.fetchAnalysisFileUrlCounts(executedConfiguration)
        Boolean isBatchAlert = false
        String miningVariable = ""

        if(executedConfiguration.dataMiningVariable){
            currentScreenType = currentScreenType + "-" + executedConfiguration.dataMiningVariable
            isBatchAlert = true
            miningVariable = executedConfiguration.dataMiningVariable
        }
        def aggAdhocDetailHelpMap = Holders.config.aggregate.adhoc.helpMap

        render(view: 'adhocDetails', model: [executedConfigId           : executedConfigId,
                                             selectedDatasource         : selectedDatasource,
                                             evdasDateRange             : evdasDateRange,
                                             faersDateRange             : faersDateRange,
                                             vaersDateRange             : vaersDateRange,
                                             vigibaseDateRange          : vigibaseDateRange,
                                             callingScreen              : params.callingScreen,
                                             name                       : name,
                                             configId                   : configId,
                                             showPrr                    : showPrr,
                                             showRor                    : showRor,
                                             showEbgm                   : showEbgm,
                                             showDss                    : grailsApplication.config.statistics.enable.dss && !executedConfiguration.dataMiningVariable,
                                             userList                   : userList,
                                             groupBySmq                 : groupBySmq,
                                             strategyList               : SignalStrategy.findAll(),
                                             backUrl                    : request.getHeader('referer'),
                                             dr                         : dr,
                                             dateRange                  : dateRange,
                                             dssUrl                     : dssUrl,
                                             freqNames                  : freqNames,
                                             viewInstance               : viewInstance ?: null,
                                             isShareFilterViewAllowed   : isShareFilterViewAllowed,
                                             isViewUpdateAllowed        : isViewUpdateAllowed,
                                             filterMap                  : viewInstance ? viewInstance.filters : "",
                                             columnIndex                : "",
                                             sortedColumn               : viewInstance ? viewInstance.sorting : "",
                                             advancedFilterView         : viewInstance?.advancedFilter ? alertService.fetchAdvancedFilterDetails(viewInstance.advancedFilter) : "",
                                             viewId                     : viewInstance ? viewInstance.id : "",
                                             tagName                    : params.tagName,
                                             allowedProductsAsSafetyLead: allowedProductsAsSafetyLead?.join(","),
                                             isLatest                   : executedConfiguration?.isLatest,
                                             appType                    : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND,
                                             fieldList                  : fieldList.sort({it.display.toUpperCase()}),
                                             isProductSecurity          : alertService.isProductSecurity(),
                                             subGroupsColumnList        : subGroupsColumnsList,
                                             relativeSubGroupMap        : relativeSubGroupMap,
                                             prrRorSubGroupMap          : prrRorSubGroupMap,
                                             subGroupColumnInfo         : subGroupColumnInfo,
                                             filterIndex                : JsonOutput.toJson(searchableColumnList[0]),
                                             filterIndexMap             : JsonOutput.toJson(searchableColumnList[1]),
                                             alertType                  : alertType,
                                             cumulative                 : cumulative,
                                             reportUrl                  : reportIntegrationService.fetchReportUrl(executedConfiguration),
                                             isFaers                    : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.FAERS) ?: false,
                                             isVaers                    : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.VAERS) ?: false,
                                             isVigibase                 : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.VIGIBASE) ?: false,
                                             isJader                    : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.JADER) ?: false,
                                             analysisFileUrl            : spotfireService.fetchAnalysisFileUrlIntegratedReview(executedConfiguration),
                                             isRor                      : cacheService.getRorCache(),
                                             isFaersEnabled             : isFaersEnabled,
                                             isVaersEnabled             : isVaersEnabled,
                                             isVigibaseEnabled          : isVigibaseEnabled,
                                             isEvdasEnabled             : isEvdasEnabled,
                                             isPvaEnabled               : isPvaEnabled,
                                             isDataMining               : isDataMining,
                                             faersSubGroupsColumnList   : faersSubGroupsColumnsList,
                                             forceJustification         : forceJustification,
                                             currentScreenType          : currentScreenType,
                                             analysisStatus             : statusMap,
                                             analysisStatusJson         : statusMap as JSON,
                                             saveCategoryAccess         : checkAccessForCategorySave(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND),
                                             isCaseSeriesAccess         : SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION"),
                                             hasAggReviewerAccess       : hasAggReviewerAccess,
                                             buttonClass                : buttonClass,
                                             isBatchAlert               : isBatchAlert,
                                             miningVariable             : miningVariable,
                                             aggAdhocDetailHelpMap      : aggAdhocDetailHelpMap,
                                             adhocColumnListNew         : adhocColumnList
        ])
    }
    void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message',
                        args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    @Secured(['ROLE_SHARE_GROUP','ROLE_SHARE_ALL'])
    def editShare() {
        Boolean sharedWithoutException = true
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(Long.parseLong(params.executedConfigId))
        try {
            editShareWith()
        } catch (Exception e) {
            sharedWithoutException = false
            e.printStackTrace()
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

    def fetchAllFieldValues() {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(params?.executedConfigId as Long)
        String dataSource = executedConfiguration?.getDataSource(executedConfiguration?.selectedDatasource)
        boolean isPvaEnabled = dataSource?.toLowerCase() == 'safety db'
        boolean isFaersEnabled = dataSource?.toLowerCase() == 'faers'
        boolean isVaersEnabled  = dataSource?.toLowerCase() == 'vaers'
        boolean isVigibaseEnabled  = dataSource?.toLowerCase() == 'vigibase'
        boolean isJaderEnabled  = dataSource?.toLowerCase() == 'jader'
        Boolean groupBySmq = executedConfiguration?.groupBySmq ?: false
        List<Map> fieldList = aggregateOnDemandAlertService.fieldListAdvanceFilter(isFaersEnabled, isPvaEnabled,isVaersEnabled, isVigibaseEnabled, isJaderEnabled,executedConfiguration?.dataMiningVariable, groupBySmq)
        render fieldList as JSON
    }

    String getEvdasAndFaersEndDate(String dateRange){
        if(dateRange){
            String date = dateRange.substring(dateRange.indexOf(" - ")+" - ".size(),dateRange.length()).replace('-','/')
            date.replace(date[-4..-1],date[-2..-1])
        } else {
            null
        }
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map<String, List> possibleValuesMap = [:]
        possibleValuesMap.put("alertTags", AlertTag.list()?.collect { [id: it.name, text: it.name] })
        alertService.preparePossibleValuesMapForOnDemand(AggregateOnDemandAlert, possibleValuesMap, executedConfigId)
        List<String> yesNoFieldsList = ["listed","positiveRechallenge","positiveDechallenge", "related", "pregnancy", "currentRun"]
        List<Map<String, String>> yesNoMapList = [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
        yesNoFieldsList.each {
            possibleValuesMap.put(it, yesNoMapList)
        }
        List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags()
        codeValues.each{
            it.id = it.text
        }
        List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
        List<Map> subTags = pvsGlobalTagService.fetchSubTagsFromMart(codeValues)
        possibleValuesMap.put("tags" , tags.unique{it.text.toUpperCase()})
        possibleValuesMap.put("subTags" , subTags.unique{it.text.toUpperCase()})
        render possibleValuesMap as JSON
    }

    def delete(){
        ExecutedConfiguration executedConfiguration =  ExecutedConfiguration.get(params.id as Long)
        if(executedConfiguration) {
            try {
                aggregateOnDemandAlertService.deleteOnDemandAlert(executedConfiguration)
                flash.message = message(code: "app.configuration.alert.delete.success", args: [executedConfiguration?.name])
            } catch (Exception e) {
                log.error(e.printStackTrace())
                flash.warn = message(code: "app.configuration.delete.fail", args: [executedConfiguration?.name])
            }
        }
        redirect(action: "adhocReview")
    }

}
