package com.rxlogix

import com.rxlogix.signal.ProductGroupData
import com.rxlogix.signal.ProductGroupStatus
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@Secured(["isAuthenticated()"])
class ProductGroupStatusController {

    def productGroupStatusService;
    def dynamicReportService
    def signalAuditLogService

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def index() {}

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def list(DataTableSearchRequest searchRequest) {
        log.info("ProductGroupStatusController list - In")
        Map validatedSignals = productGroupStatusService.getProductGroupStatusList(searchRequest, params)
        log.info("ProductGroupStatusController list - Out")
        render validatedSignals as JSON
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportProductGroupDatas(Long productGroupsId, String pid) {
        log.info("ProductGroupStatusController exportProductGroupDatas - In: [productGroupsId"+productGroupsId+",pid:"+pid+"]")
        Map columns = Holders.config.signal.prodGroups.excel.columns
        List summaryList = productGroupStatusService.getProductGroupSummaryData(productGroupsId)
        renderExcelReport(pid, productGroupStatusService.getProductGroupsDatasByLotId(productGroupsId), columns, summaryList)

    }

    void renderExcelReport(String name, List data, Map columns,List summaryList) {
        Map reportParams = new LinkedHashMap()
        reportParams.outputFormat = "XLSX"
        reportParams.name = name
        reportParams.columns = columns
        reportParams.summaryList = summaryList
        File file = dynamicReportService.createEtlBatchReport(new JRMapCollectionDataSource(data), reportParams)
        String reportName = name
        reportName =reportName.replaceAll("[ :]", "_")
        response.contentType = "${dynamicReportService.getContentType("XLSX")}; charset=UTF-8"
        response.contentLength = file.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.XLSX" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
    }

    void renderExcelReport(String name, List data, Map columns) {
        Map reportParams = new LinkedHashMap()
        reportParams.outputFormat = Constants.SignalReportOutputType.XLSX
        reportParams.name = name
        reportParams.columns = columns
        File file = dynamicReportService.createProductGroupBatchReport(new JRMapCollectionDataSource(data), reportParams)
        String reportName = name +"_"+ DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(Constants.SignalReportOutputType.XLSX)}; charset=UTF-8"
        response.contentLength = file.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}."+ Constants.SignalReportOutputType.XLSX+ "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,"Product Group Import Log" , "Control Panel", params, reportName)
    }
    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportProductGroupsStatus(String searchRequestString) {
        log.info("ProductGroupStatusController exportProductGroupsStatus - In")
        DataTableSearchRequest searchRequest = DataTableSearchRequest.fromJSON(searchRequestString, DataTableSearchRequest.class)
        List<ProductGroupStatus> productGroupStatusList = getBathLotStatusList(productGroupStatusService.getProductGroupsStatus(searchRequest, params, true));
        Map columns = ["uniqueIdentifier":"Name","count":"Processed/Total", "uploadedAt": "Processed Date", "addedBy": "Added By", "isApiProcessed":"Status"]
        renderExcelReport("ALL_PRODUCT_GROUPS_STATUS", productGroupStatusService.getProductGroupsMap(productGroupStatusList, columns), columns)
    }
    def getBathLotStatusList(List<ProductGroupStatus> productGroupStatusList) {
        List<ProductGroupStatus> productGroupStatuses = [];
        for (bl in productGroupStatusList) {
            productGroupStatuses.add(new ProductGroupStatus(bl.getUniqueIdentifier(), bl.getCount(),
                    bl.getValidRecordCount(), bl.getInvalidRecordCount(), bl.getUploadedAt(), bl.getAddedBy(),
                    bl.getIsApiProcessed(), ProductGroupData.createCriteria().list { eq("productGroupStatusId", bl.getId()) }));
        }
        productGroupStatuses
    }
    void renderCsvResponse(String csvFileName, String fileData) {
        csvFileName = csvFileName + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename="+csvFileName+".csv");
        response.setContentLength(fileData.size());
        response.getOutputStream().write(fileData.bytes);
        response.outputStream.flush()
    }

}
