package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.config.AlertType
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.TestCaseDTO
import com.rxlogix.user.User
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonOutput
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import com.rxlogix.enums.ReportFormat

@Secured(["isAuthenticated()"])
class TestSignalRestController {

    def reportIntegrationService
    def userService
    def configurationService
    def testSignalService
    def dynamicReportService

    def index() {
        def url = Holders.config.testBed.automation.url
        def path = Holders.config.testBed.automation.checkIfFileExist
        Map query = ["path": "/home/shivamvashist/importFile/"]
        Map result = reportIntegrationService.get(url, path, query)
        render(view: 'testSignal', model: [fileExists: result.data])
    }

    def uploadFile() {

        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        MultipartFile fileToUpload = request.getFile('file')
        def url = Holders.config.testBed.automation.url
        def path = Holders.config.testBed.automation.uploadTestCases
        String originalFileName = fileToUpload.originalFilename
        String fileName = Constants.SignalAutomation.FILE_NAME
        fileToUpload.transferTo(new File("/tmp/${fileName}"))
        responseDTO.message = message(code: "app.label.Configuration.upload.inprogress")
        render(responseDTO as JSON)
    }

    def testCasesData() {
        def url = Holders.config.testBed.automation.url
        def path = "/api/getFileInfo/"
        Map query = ["path": "/home/shivamvashist/importFile/"]
        Map result = reportIntegrationService.get(url, path, query)
        render(["aaData": result.data] as JSON)
    }

    def viewExecutedTestCases() {
        redirect(action: 'executeTestAlerts')
    }
    /**
     * executeTestAlerts()
     * @return
     */
    def executeTestAlerts() {
        log.info("Executing test alerts")
        ExecutionStatusDTO executionStatusDTO = createExecutionStatusDTO()
        executionStatusDTO.executionStatus = ReportExecutionStatus.COMPLETED
        Map countMap = testSignalService.getCountOfPassedTestCases(executionStatusDTO)

        Map passedCountMap = testSignalService.getCountForChart(executionStatusDTO, ReportExecutionStatus.COMPLETED)

        executionStatusDTO = createExecutionStatusDTO()
        Map failedCountMap = testSignalService.getTotalCountForChart(executionStatusDTO)

        render(view: 'testAlertExecutionStatus', model: [countOfPassedTestCases: countMap.countOfPassedTestCases, countOfTotatTestCases: countMap.countOfTotatTestCases,
                                                         passedCountMap        : passedCountMap as JSON, failedCountMap: failedCountMap as JSON])

    }

    def testAlertExecutionStatus() {

        log.info("Getting test alerts execution status data")

        Map resultData = testSignalService.getTestAlertExecutionStatusData()

        render(resultData as JSON)
    }
    /**
     * createExecutionStatusDTO()
     * @return executionStatusDTO
     */

    ExecutionStatusDTO createExecutionStatusDTO() {
        User user = userService.getUser()
        ExecutionStatusDTO executionStatusDTO = new ExecutionStatusDTO()
        executionStatusDTO.alertType = AlertType.AGGREGATE_CASE_ALERT
        executionStatusDTO.currentUser = user
        executionStatusDTO.max = 100
        executionStatusDTO.offset = 0
        executionStatusDTO.sort = "runDate"
        executionStatusDTO.direction = "asc"
        executionStatusDTO.workflowGroupId = user?.workflowGroup?.id
        executionStatusDTO.configurationDomain = Configuration
        executionStatusDTO.searchString = 'SmokeTestingPVSignalDev'
        executionStatusDTO
    }
    /**
     * handleSelectedCases
     * @return
     */
    def handleSelectedCases() {
        log.info("handleSelectedCases")
        def params = request.JSON
        ResponseDTO status = new ResponseDTO(code: 200, status: true)
        try {
            def url = Holders.config.testBed.automation.url
            def path = Holders.config.testBed.automation.runAlerts
            List<TestCaseDTO> testCaseDTOList = testSignalService.createTestCaseDTO(params)
            def query = JsonOutput.toJson(testCaseDTOList)
            Map result = reportIntegrationService.postData(url, path, query)
        }
        catch (Exception e) {
            status.message = "Internal server Error"
            status.code = "500"
        }
        redirect(action: "executeTestAlerts")
    }
    /**
     * deleteTestAlerts
     * @return
     */
    def deleteTestAlerts() {
        log.info("deleteTestAlerts")
        ResponseDTO responseDTO = new ResponseDTO(status: true, code: 200)
        try {
            def result = testSignalService.deleteTestAggAlerts()
            responseDTO.message = result
        }
        catch (Exception ex) {
            responseDTO.status = false
            responseDTO.code = 500
            ex.printStackTrace()
        }
        render(responseDTO as JSON)
    }

    def exportReport() {
        log.info("exportReport")
        log.info("Export report called")
        Map resultMap = testSignalService.getTestAlertExecutionStatusData()
        List reportData = resultMap.aaData
        def reportFile = dynamicReportService.createTestDataReport(new JRMapCollectionDataSource(reportData), params)
        renderReportOutputType(reportFile)
    }

    private renderReportOutputType(File reportFile) {
        String reportName = "Test Signal Report" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }


//    private void setConfigurationDomain(ExecutionStatusDTO executionStatusDTO){
//        switch (params.alertType){
//            case AlertType.AGGREGATE_CASE_ALERT.name():
//            case AlertType.SINGLE_CASE_ALERT.name():
//                executionStatusDTO.configurationDomain = Configuration
//                break
//            case AlertType.EVDAS_ALERT.name():
//                executionStatusDTO.configurationDomain = EvdasConfiguration
//                break
//            case AlertType.LITERATURE_SEARCH_ALERT.name():
//                executionStatusDTO.configurationDomain = LiteratureConfiguration
//                break
//        }
//    }

}
