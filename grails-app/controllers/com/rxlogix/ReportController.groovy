package com.rxlogix

import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ProductGroup
import com.rxlogix.enums.ReportNameEnum
import com.rxlogix.mapping.MedDraSOC
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ReportHistory
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.time.DateUtils
import org.hibernate.criterion.CriteriaSpecification

import java.nio.charset.StandardCharsets

@Secured(["isAuthenticated()"])
class ReportController implements AlertUtil {

    def dynamicReportService
    def reportService
    def userService
    def productEventHistoryService
    def validatedSignalService
    def dataSource_pva
    def dataObjectService
    def productBasedSecurityService
    def signalAuditLogService
    def emergingIssueService
    def messageSource

    @Secured(['ROLE_REPORTING'])
    def index(Long productId, String selectedDataSource, Long reportHistoryId, Boolean showHistory) {
        def alert
        Map preSelectedData = [:]
        preSelectedData.put("selectedDataSource", selectedDataSource)
        switch (selectedDataSource) {
            case 'PVA':
            case 'FAERS':
                alert = AggregateCaseAlert.get(productId)
                if(!alert){
                    alert = ArchivedAggregateCaseAlert.get(productId)
                }
                preSelectedData.put("productName", alert.name)
                preSelectedData.put("productSelection", alert.alertConfiguration.productSelection)
                break
            case 'EUDRA':
                alert = params.boolean('isArchived') ? ArchivedEvdasAlert.get(productId) : EvdasAlert.get(productId)
                preSelectedData.put("productName", alert.substance)
                preSelectedData.put("productSelection", alert.alertConfiguration.productSelection)
                preSelectedData.put("productGroupSelection", alert.alertConfiguration.productGroupSelection)
                break
        }
        def socList = []
        MedDraSOC.withTransaction {
            socList = MedDraSOC.findAllByLangId(1).collect { ["id": it.id, "name": it.name] }
        }

        def ptList = []
        if (socList.size() > 0) {
            ptList = reportService.getPTsFromSoc(socList[0]?.id as String)
        }

        def datasources = getDataSourceMap()
        datasources.remove("jader")
        Map historicData = null
        if (reportHistoryId) {
            historicData = JSON.parse(new String(ReportHistory.get(reportHistoryId).memoReport, StandardCharsets.UTF_8)) as Map
        }
        [productGroups: ProductGroup.list(), socList: socList, ptList: ptList, datasources: datasources, reportTypes: ReportNameEnum.getAllReportNames(), preSelectedData: preSelectedData, historicData: historicData, showHistory: showHistory, isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)]
    }

    def view(Long reportHistoryId) {
        Map historicData = null
        ReportHistory reportHistory = ReportHistory.get(reportHistoryId)
        if (reportHistoryId) {
            historicData = JSON.parse(new String(reportHistory.memoReport, StandardCharsets.UTF_8)) as Map
        }
        def socList = historicData.reactionGroupSelectedSOCs
        def selectedSOCForReaction = historicData.selectedSOCForReaction
        def ptList = historicData.selectedPtListNames
        String dataSource = getDataSource(reportHistory.dataSource)
        [reportHistory: reportHistory, historicData: historicData, socList: socList, ptList: ptList, timeZone: userService.getUser().preference.timeZone, selectedSOCForReaction: selectedSOCForReaction , dataSource: dataSource]
    }

    def getPTsFromSoc() {
        String soc = params.soc
        def ptList = reportService.getPTsFromSoc(soc)
        def result = ['ptList': ptList]
        render(result as JSON)
    }

    def downloadReport() {
        ReportHistory reportHistory = ReportHistory.get(params.id)
        //Added for PVS-53068
        List criteriaSheetList = dynamicReportService.createCriteriaList(userService.getUser(), null, null, false, false)
        switch (reportHistory.reportType) {
            case ReportNameEnum.MEMO_REPORT.value:
                downloadMemoReport(reportHistory, params.type, criteriaSheetList)
                break
            case ReportNameEnum.PBRER_SIGNAL_SUMMARY.value:
                downloadPBRERReport(reportHistory, params.type, criteriaSheetList)
                break
            case ReportNameEnum.SIGNALS_BY_STATE.value:
                downloadSignalStateReport(reportHistory, params.type, criteriaSheetList)
                break
            case ReportNameEnum.SIGNAL_PRODUCT_ACTIONS.value:
                downloadProductActionReport(reportHistory, params.type, criteriaSheetList)
                break
            case ReportNameEnum.SIGNAL_SUMMARY_REPORT.value:
                downloadSignalSummaryReport(reportHistory, params.type)
        }
        signalAuditLogService.createAuditForExport(criteriaSheetList, reportHistory.getInstanceIdentifierForAuditLog(), "Reporting", params, FileNameCleaner.cleanFileName(reportHistory.reportName))
    }

    private downloadMemoReport(ReportHistory reportHistory, String exportFormat, List criteriaSheetList) {
        params.outputFormat = exportFormat
        File outputFile = reportService.downloadMemoReport(reportHistory, exportFormat, criteriaSheetList)
        response.contentType = "${dynamicReportService.getContentType(exportFormat)}; charset=UTF-8"
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportHistory.reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(outputFile?.bytes)
        response.outputStream.flush()
    }

    private downloadPBRERReport(ReportHistory reportHistory, String exportFormat, List criteriaSheetList) {
        params.outputFormat = exportFormat
        File outputFile = reportService.downloadPBRERReport(reportHistory, exportFormat, criteriaSheetList)
        response.contentType = "${dynamicReportService.getContentType(exportFormat)}; charset=UTF-8"
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportHistory.reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(outputFile?.bytes)
        response.outputStream.flush()
    }

    private downloadSignalStateReport(ReportHistory reportHistory, String exportFormat, List criteriaSheetList) {
        params.outputFormat = exportFormat
        File outputFile = reportService.downloadSignalStateReport(reportHistory, exportFormat, criteriaSheetList)
        response.contentType = "${dynamicReportService.getContentType(exportFormat)}; charset=UTF-8"
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportHistory.reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(outputFile.bytes)
        response.outputStream.flush()
    }

    private downloadProductActionReport(ReportHistory reportHistory, String exportFormat, List criteriaSheetList) {
        params.outputFormat = exportFormat
        File outputFile = reportService.downloadProductActionReport(reportHistory, exportFormat, criteriaSheetList)
        response.contentType = "${dynamicReportService.getContentType(exportFormat)}; charset=UTF-8"
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportHistory.reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(outputFile.bytes)
        response.outputStream.flush()
    }

    private downloadSignalSummaryReport(ReportHistory reportHistory, String exportFormat) {
        params.outputFormat = exportFormat
        File outputFile = reportService.downloadSignalSummaryReport(reportHistory, exportFormat)
        response.contentType = "${dynamicReportService.getContentType(exportFormat)}; charset=UTF-8"
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportHistory.reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(outputFile.bytes)
        response.outputStream.flush()
    }

    def history() {
        List<Map> reportHistoryList = ReportHistory.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("reportName", "reportName")
                property("productName", "productName")
                property("productGroup", "productGroup")
                property("startDate", "startDate")
                property("endDate", "endDate")
                property("dateCreated", "dateCreated")
                property("id", "id")
                property("reportType", "reportType")
                property("isReportGenerated", "isReportGenerated")
                property("dataSource", "dataSource")
                'updatedBy' {
                    property("fullName", "fullName")
                }
            }
        } as List<Map>

        List<Map> historyMapList = reportHistoryList.collect {
            String dataSourceLabel = Constants.DataSource.DATASOURCE_EUDRA
            String dataSourceForDisplay = Constants.DataSource.EUDRA
            if (it.dataSource.toString().equals(Constants.DataSource.PVA)) {
                dataSourceLabel = Constants.DataSource.DATASOURCE_PVA
                dataSourceForDisplay=Constants.DataSource.PVA
            }
            else if (it.dataSource.toString().equals(Constants.DataSource.FAERS)) {
                dataSourceLabel = Constants.DataSource.DATASOURCE_FAERS
                dataSourceForDisplay=Constants.DataSource.FAERS
            }
            else if (it.dataSource.toString().equals(Constants.DataSource.VAERS)){
                dataSourceLabel = Constants.DataSource.DATASOURCE_VAERS
                dataSourceForDisplay=Constants.DataSource.VAERS
            }
            else if (it.dataSource.toString().equals(Constants.DataSource.VIGIBASE)){
                dataSourceLabel = Constants.DataSource.DATASOURCE_VIGIBASE
                dataSourceForDisplay=Constants.DataSource.VIGIBASE
            }
            else if (it.dataSource.toString().equals(Constants.DataSource.JADER)){
                dataSourceLabel = Constants.DataSource.DATASOURCE_JADER
                dataSourceForDisplay=Constants.DataSource.JADER
            }
            [
                    reportName      : it.reportName,
                    productName     : reportService.isValidJson(it.productName) ? reportService.prepareProductMap(it.productName, dataSourceForDisplay) : it.productName,
                    productGroup    : AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(it.productGroup),
                    summaryDateRange: "${DateUtil.toDateString1(it.startDate.clearTime())} to ${DateUtil.toDateString1(it.endDate.clearTime())}",
                    generatedBy     : it.fullName,
                    generatedOn     : new Date(DateUtil.toDateStringWithTime(it.dateCreated, userService.user.preference.timeZone)).format(DateUtil.DATEPICKER_FORMAT_AM_PM).toString(),
                    downloadId      : it.id,
                    reportType      : it.reportType,
                    dataSource      : dataSourceLabel,
                    reportGenerated : it.isReportGenerated
            ]
        }
        render historyMapList as JSON
    }


    def requestReport(String dataSource, String reportName, String productSelection, String productGroupSelection,
                      String startDate, String endDate, String dateRangeType) {

        if(dataSource == Constants.DataSource.EUDRA && params.reportType as ReportNameEnum == ReportNameEnum.MEMO_REPORT){
            render(["message": message(code: "app.label.report.memo.evdas")] as JSON)
            return
        }
        List dateRangeArray = reportService.getDateRangeBasedOnType(startDate, endDate, dateRangeType)

        Date start_date = dateRangeArray[0]
        Date end_date = dateRangeArray[1]

        ReportNameEnum reportType = params.reportType as ReportNameEnum
        String productName
        String productGroup
        Map productSelectionMap
        Map actualProductNameMap
        Boolean isIngredient = false
        if (productSelection) {
            Map productSelectMap = JSON.parse(productSelection);
            productSelectionMap = reportService.parseProductSelectionMap(productSelectMap)
            if(productSelectMap[PVDictionaryConfig.ingredientColumnIndex] != "[]"){
                isIngredient = true
            }
            productName = productSelection
            actualProductNameMap = reportService.getActualProductName(dataSource, productSelection)
            reportService.updateProductSelectionMap(productSelectionMap, actualProductNameMap)
        } else {
            productSelectionMap = reportService.parseProductGroupSelectionList([params["productGroupIds[]"]].flatten())
            productName = productSelection
        }

        if(productGroupSelection && productGroupSelection!= "[]"){
            productGroup = productGroupSelection
            productSelectionMap.productGroupSelectionIds = getIdsForProductGroup(productGroupSelection)
            productSelectionMap.productSelectionNames = productGroup
        }


        ReportHistory reportHistory = new ReportHistory(reportName: reportName, reportType: reportType.value, dateCreated: new Date(),
                startDate: start_date, endDate: end_date, productName: productName, dataSource: dataSource, updatedBy: userService.user, productGroup: productGroup)

        reportHistory.validate()
        if ((!reportHistory.productName && !reportHistory.productGroup) || !reportHistory.reportName) {
            flash.error = messageSource.getMessage("app.label.reporting.all.fields.required", null, Locale.default)
            render(template: '/includes/layout/flashErrorsDivs', model: [theInstance: reportHistory])
            return
        }
        if (dataSource == Constants.DataSource.EUDRA) {
            productSelectionMap = reportService.updateProductIngredientMappings(productSelectionMap)
        }

        if (!reportHistory.hasErrors() && reportHistory.save(flush: true)) {
            log.info("ReportHistory is saved.")
            switch (reportType) {
                case ReportNameEnum.MEMO_REPORT:
                    log.info("Now generating the memo report.")
                    reportService.generateEventForMemoReport(reportHistory,productSelectionMap,isIngredient)
                    break
                case ReportNameEnum.PBRER_SIGNAL_SUMMARY:
                    reportService.generatePBRERReport(start_date, end_date, productSelectionMap, reportHistory.id, dataSource)
                    break
                case ReportNameEnum.SIGNALS_BY_STATE:
                    reportService.generateSignalStateReport(start_date, end_date, productSelectionMap, reportHistory.id, dataSource)
                    break
                case ReportNameEnum.SIGNAL_PRODUCT_ACTIONS:
                    reportService.generateProductActionsReport(start_date, end_date, productSelectionMap, reportHistory.id, dataSource)
                    break
                case ReportNameEnum.SIGNAL_SUMMARY_REPORT:
                    reportService.generateSignalSummaryReport(start_date, end_date, productSelectionMap, reportHistory.id, dataSource)
                    break
            }
            render(["message": message(code: "app.label.report.processing.notification")] as JSON)
        } else {
            if ((!reportHistory.productName && !reportHistory.productGroup) || !reportHistory.reportName) {
                flash.error = messageSource.getMessage("app.label.reporting.all.fields.required", null, Locale.default)
            }
            render(template: '/includes/layout/flashErrorsDivs', model: [theInstance: reportHistory])
        }
    }

    def prepareICSRsReport(String dataSource, String reportName) {
        List productSelectionIds
        List productIngredientIds
        String productSelectionNames
        List productSubstanceIds
        List selectedSocListIds = params["socList[]"]
        List selectedSocListNames = params["socListName[]"]
        Date dateCreated = new Date()
        Boolean isMultiIgredient = Boolean.parseBoolean(params.isMultiIngredient)
        def dateRange = reportService.getDateRangeBasedOnType(params.startDate, params.endDate, params.dateRangeType)

        ReportNameEnum reportType = params.reportType as ReportNameEnum
        Map result = [:]
        def productGroups;
        def productName;
        if (params.productSelection) {
            Map productSelectionMap = reportService.parseProductSelectionMap(JSON.parse(params.productSelection))
            productSelectionIds = productSelectionMap.productSelectionIds + productSelectionMap.productFamilyIds
            productIngredientIds = productSelectionMap.productIngredientIds
            productName = params.productSelection
            productSubstanceIds = productSelectionMap.productSubstanceIds
            productIngredientIds = (productIngredientIds.isEmpty() && productSubstanceIds) ? productSubstanceIds : productIngredientIds
        } else {
            Map productSelectionMap = reportService.parseProductGroupSelectionList([params["productGroupIds[]"]].flatten())
            productSelectionIds = productSelectionMap.productSelectionIds
            productSelectionNames = params.productGroupSelection
            productGroups=productSelectionNames
        }
        ReportHistory reportHistory = new ReportHistory(reportName: reportName, reportType: reportType.value, dateCreated: dateCreated,
                startDate: dateRange[0], endDate: dateRange[1],
                productName: productName, dataSource: dataSource, updatedBy: userService.user, productGroup: productGroups)
        if(reportHistory.validate()) {
            try {
                result = reportService.generateICSRReport(dateRange, productSelectionIds, productIngredientIds, dataSource, selectedSocListIds,isMultiIgredient)
                result.put('reactionGroupSelectedSOCs', selectedSocListNames)
                reportHistory.memoReport = (result as JSON).toString() as byte[]
                reportHistory.isReportGenerated = true
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        if (!reportHistory.hasErrors() && reportHistory.save(flush: true)) {
            render(result + ["message": message(code: "app.label.report.processing.notification"), "reportHistoryId": reportHistory.id] as JSON)
        } else {
            render(template: '/includes/layout/flashErrorsDivs', model: [theInstance: reportHistory])
        }
    }

    def prepareReactionGroupReport(String dataSource) {
        List productSelectionIds
        List productIngredientIds
        List productSubstanceIds
        def selectedSocListIds = params["socList[]"]
        Date dateCreated = new Date()
        List<String> dateRange = reportService.getDateRangeBasedOnType(params.startDate, params.endDate, params.dateRangeType)
        Map result = [:]


        if (params.productSelection) {
            Map productSelectionMap = reportService.parseProductSelectionMap(JSON.parse(params.productSelection))
            productSelectionIds = productSelectionMap.productSelectionIds
            productIngredientIds = productSelectionMap.productIngredientIds
            productSubstanceIds = productSelectionMap.productSubstanceIds
            productIngredientIds = (productIngredientIds.isEmpty() && productSubstanceIds) ? productSubstanceIds : productIngredientIds
        } else {
            Map productSelectionMap = reportService.parseProductGroupSelectionList([params["productGroupIds[]"]].flatten())
            productSelectionIds = productSelectionMap.productSelectionIds
        }

        try {
            result.put('reactionGroup', reportService.fetchDataForReactionGroupChart(dateRange, productSelectionIds, productIngredientIds, [selectedSocListIds].flatten(), dataSource))
        } catch (Exception e) {
            e.printStackTrace()
        }

        render(result as JSON)
    }

    def prepareReactionReport(String dataSource) {
        List productSelectionIds
        List productIngredientIds
        List productSubstanceIds
        Long selectedSOCIdForReaction = params.selectedSOCIdForReaction as Long
        def selectedPtListIds = params["ptList[]"]
        List<String> dateRange = reportService.getDateRangeBasedOnType(params.startDate, params.endDate, params.dateRangeType)
        Map result = [:]


        if (params.productSelection) {
            Map productSelectionMap = reportService.parseProductSelectionMap(JSON.parse(params.productSelection))
            productSelectionIds = productSelectionMap.productSelectionIds
            productIngredientIds = productSelectionMap.productIngredientIds
            productSubstanceIds = productSelectionMap.productSubstanceIds
            productIngredientIds = (productIngredientIds.isEmpty() && productSubstanceIds) ? productSubstanceIds : productIngredientIds
        } else {
            Map productSelectionMap = reportService.parseProductGroupSelectionList([params["productGroupIds[]"]].flatten())
            productSelectionIds = productSelectionMap.productSelectionIds
        }

        try {
            result.put('reaction', reportService.fetchDataForReactionChart(dateRange, productSelectionIds, productIngredientIds,selectedSOCIdForReaction, [selectedPtListIds].flatten(), dataSource))
        } catch (Exception e) {
            e.printStackTrace()
        }
        render(result as JSON)
    }

    def saveICSRsReportHistory() {
        reportService.saveICSRsReportHistory(params)
        render(["message": message(code: "app.label.report.updating.notification")] as JSON)
    }
}
