package com.rxlogix

import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.util.CollectionUtils

@Secured(["isAuthenticated()"])
class ApiUploadStatusReportController {

    def batchRestService

    def dynamicReportService;
    def signalAuditLogService

    def index() {
        List<BatchLotStatus> batchLotStatuses = batchRestService.getLastETLBatchLots()
        String lastEtlStatus = null;
        Date lastEtlDate = null;
        String lastEtlBatchIds = "";
        if(!CollectionUtils.isEmpty(batchLotStatuses)) {
            batchLotStatuses.eachWithIndex { item, index ->
                if(item.getEtlStatus()==null || item.getEtlStatus()=='STARTED') {
                    lastEtlStatus = "RUNNING";
                } else if(item.getEtlStatus()!='STARTED' && item.getEtlStatus()=='FAILED') {
                    lastEtlStatus = "FAILED";
                } else if(item.getEtlStatus()!='FAILED' && item.getEtlStatus()=='SUCCESS') {
                    lastEtlStatus = "SUCCESS";
                }
                if(lastEtlDate==null) {
                    lastEtlDate=item.getEtlStartDate();
                } else if(lastEtlDate.before(item.getEtlStartDate())) {
                    lastEtlDate=item.getEtlStartDate();
                }
                lastEtlBatchIds=lastEtlBatchIds+item.getBatchId()+", ";
            }
        }
        [   callingScreen: null,
            lastEtlStatus: lastEtlStatus,
            lastEtlDate:lastEtlDate,
            lastEtlBatchIds:lastEtlBatchIds,
            remainingBatchLotCountForETL: batchRestService.getRemainingBatchLotCountForETL()
        ]
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def list(DataTableSearchRequest searchRequest) {
        Map validatedSignals = batchRestService.getBatchLotStatusList(searchRequest, params)
        render validatedSignals as JSON
    }


    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportBatchLots(String searchRequestString) {
        DataTableSearchRequest searchRequest = DataTableSearchRequest.fromJSON(searchRequestString, DataTableSearchRequest.class)
        List<BatchLotStatus> batchLotStatusList = getBathLotStatusList(batchRestService.getBatchLotStatuses(searchRequest, params, true));
        Map columns = ["batchId":"Name","batchDate":"Date Range","validCount":"Processed/Total","uploadedAt":"Processed Date","addedBy":"Added By","etlProcessed":"Status"]
        renderExcelReport("ALL_BATCH_LOT_STATUS", batchRestService.getBatchLotExcelData(batchLotStatusList), columns);
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportLastETLBatchLots() {
        List<BatchLotStatus> batchLotStatusList = getETLBathLotStatusList(batchRestService.getLastSuccessfullETLBatchLots());
        Map columns = ["batchId":"Name","batchDate":"Date Range","product":"Product", "description":"Description", "bulkBatch":"Bulk Batch",
                       "bulkBatchDate":"Bulk Batch Date", "fillBatch":"Fill Batch",
                       "fillExpiry":"Fill Expiry", "fillUnits":"Fill Units", "packageBatch":"Package Batch",
                       "packageCountry":"Package Country", "packageUnit":"Package Unit", "packageReleaseDate":"Package Release Date",
                       "shippingBatch":"Shipping Batch", "componentBatch":"Component Batch", "startDate":"Start Date",
                       "endDate":"End Date", "productGroupName":"Product Group Name",  "productHierarchy":"Product Hierarchy",
                       "etlStatus":"Import Result","validationError":"Info"
        ]
        List summaryList = batchRestService.getBatchLotETLImportSummaryData(batchLotStatusList)
        renderETLExcelReport("LAST_ETL_BATCH_LOT", batchRestService.getBatchLotETLExcelData(batchLotStatusList), columns,summaryList);
        signalAuditLogService.createAuditForExport(null,"Last Batch Load" , "Control Panel", params, "LAST_ETL_BATCH_LOT")
    }


    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportBatchLotDatas(Long batchLotId, String batchId ) {
        Map columns = Holders.config.signal.batchSignal.csv.columns
        List summaryList = batchRestService.getBatchLotETLSummaryData(batchRestService.getBatchLotStatusByLotIds([batchLotId]))
        renderExcelBatchDataReport(batchId, batchRestService.getExcelDataByLotId(batchLotId), columns, summaryList)
    }

    def getBathLotStatusList(List<BatchLotStatus> batchLotStatusList) {
        List<BatchLotStatus> batchLotStatuses = [];
        for (bl in batchLotStatusList) {
            batchLotStatuses.add(new BatchLotStatus(bl.getId(), bl.getVersion(), bl.getBatchId(), bl.getBatchDate(), bl.getCount(),
                    bl.getValidRecordCount(), bl.getInvalidRecordCount(), bl.getUploadedAt(), bl.getAddedBy(), bl.getIsApiProcessed(),
                    bl.getIsEtlProcessed(), bl.getEtlStartDate(), bl.getEtlStatus(), bl.getDateRange(), null));
        }
        batchLotStatuses
    }

    def getETLBathLotStatusList(List<BatchLotStatus> batchLotStatusList) {
        List<BatchLotStatus> batchLotStatuses = [];
        for (bl in batchLotStatusList) {
            batchLotStatuses.add(new BatchLotStatus(bl.getId(), bl.getVersion(), bl.getBatchId(), bl.getBatchDate(), bl.getCount(),
                    bl.getValidRecordCount(), bl.getInvalidRecordCount(), bl.getUploadedAt(), bl.getAddedBy(), bl.getIsApiProcessed(),
                    bl.getIsEtlProcessed(), bl.getEtlStartDate(), bl.getEtlStatus(), bl.getDateRange(), batchRestService.getBatchLotDateByBatchLotId(bl.getId())));
        }
        batchLotStatuses
    }

    String getValue(Object obj) {
        return obj==null?"":String.valueOf(obj);
    }

    void renderCsvResponse(String csvFileName, String fileData) {
        csvFileName = csvFileName +DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename="+csvFileName+".csv");
        response.setContentLength(fileData.size());
        response.getOutputStream().write(fileData.bytes);
        response.outputStream.flush()
    }

    void renderExcelReport(String name, List data, Map columns,List summaryList = []) {
        Map reportParams = new LinkedHashMap()
        reportParams.outputFormat = "XLSX"
        reportParams.name = name
        reportParams.columns = columns
        reportParams.summaryList = summaryList
        File file = dynamicReportService.createEtlBatchReport(new JRMapCollectionDataSource(data), reportParams)
        String reportName = name +"_"+DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        reportName =reportName.replaceAll("[ :]", "_")
        response.contentType = "${dynamicReportService.getContentType("XLSX")}; charset=UTF-8"
        response.contentLength = file.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.XLSX" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,"Batch Data Update Status" , "Control Panel", params, "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.XLSX")
    }

    void renderETLExcelReport(String name, List data, Map columns,List summaryList = []) {
        Map reportParams = new LinkedHashMap()
        reportParams.outputFormat = "XLSX"
        reportParams.name = name
        reportParams.columns = columns
        reportParams.summaryList = summaryList
        reportParams.etlLogExport = "YES"
        File file = dynamicReportService.createEtlBatchReport(new JRMapCollectionDataSource(data), reportParams)
        String reportName = name +"_"+DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        reportName =reportName.replaceAll("[ :]", "_")
        response.contentType = "${dynamicReportService.getContentType("XLSX")}; charset=UTF-8"
        response.contentLength = file.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.XLSX" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
        params.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.XLSX"
    }

    void renderExcelBatchDataReport(String name, List data, Map columns,List summaryList = []) {
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
        signalAuditLogService.createAuditForExport(null,"Batch Lot Data" , "Control Panel", params, "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.XLSX")
    }

}
