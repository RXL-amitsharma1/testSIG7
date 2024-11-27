package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.audit.AuditTrail
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.config.Configuration
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.ImportConfigurationProcessState
import com.rxlogix.enums.ProdAssignmentProcessState
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonSlurper
import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.rxlogix.exception.FileFormatException
import org.springframework.web.multipart.MultipartFile
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DateFormat
import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class ImportConfigurationController implements AlertController{
    ImportConfigurationService importConfigurationService
    def userService
    def CRUDService
    def dynamicReportService
    def configurationService
    def messageSource
    def aggregateCaseAlertService
    def signalAuditLogService
    def dataObjectService
    @Secured(["ROLE_CONFIGURE_TEMPLATE_ALERT"])
    def importScreen() {
        render(view: 'importScreen', model: [isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)])
    }

    def fetchAlertList(String alertType) {
        Map resultMap = [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        try {
            resultMap = importConfigurationService.fetchAlertListByType(resultMap, alertType,params)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(resultMap as JSON)
    }

    List<Map> fetchAlertTemplateByType(String alertType) {
        User user = userService.getUser()
        List<Map> templateAlerts = Configuration.createCriteria().list() {
            'eq'('type', alertType)
            'eq'('isTemplateAlert', true)
            'eq'('isDeleted', false)
            'eq'('workflowGroup', user?.workflowGroup)
        }.collect {
            [id: it.id, name: it.name]
        }.sort({it.name.toUpperCase()})
        render(templateAlerts as JSON)
    }

    def createAlertFromTemplate(String alertType, Long templateAlertId) {
        Map result = [status: false]
        try {
            Configuration alertTemplate = Configuration.get(templateAlertId)
            Configuration config
            if (alertTemplate) {
                config = importConfigurationService.createAlertFromTemplate(alertType, alertTemplate, userService.getUser())
                CRUDService.save(config)
                result.status = true
            }

        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(result as JSON)

    }

    def editAlertName(String alertName, Long id) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200)
        try {
            Configuration config = Configuration.get(id)
            if (config) {
                List<Configuration> alertConfig = Configuration.findAllByNameAndWorkflowGroupAndIsDeleted(alertName, userService?.getUser()?.workflowGroup, false)
                if (alertConfig.size() == 0) {
                    config.name = alertName
                    CRUDService.updateWithAuditLog(config)
                    alertService.renameExecConfig(config.id, config.name, alertName, config.owner.id, config.type)
                    responseDTO.status = true
                    responseDTO.message = "Alert name Changed successfully"

                } else {
                    responseDTO.status = false
                    responseDTO.message = "Alert name already Exists."
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = "An error occurred while saving Alert name."
        }
        render(responseDTO as JSON)
    }

    def updateDateRangeForAlert() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200)
        try {
            Configuration config = Configuration.get(params.id)
            if (config) {
                importConfigurationService.updateDateRange(params, config)
                responseDTO.status = true
                responseDTO.message = "Alert Date Range updated successfully"

            } else {
                responseDTO.status = false
                responseDTO.message = "Alert does not exists."
            }
        } catch (Throwable th) {
            th.printStackTrace()
            responseDTO.status = false
            responseDTO.message = "An error occurred while saving Alert name."
        }
        render(responseDTO as JSON)

    }

    def updateScheduleDateJSON() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200)
        try {
            Configuration config = Configuration.get(params.id)
            if (config) {
                importConfigurationService.updateScheduleDateAndNextRunDate(params, config)
                responseDTO.status = true
                responseDTO.message = "Alert updated successfully"

            } else {
                responseDTO.status = false
                responseDTO.message = "Alert does not exists."
            }
        } catch (Throwable th) {
            th.printStackTrace()
            responseDTO.status = false
            responseDTO.message = "An error occurred while updating Alert."
        }
        render(responseDTO as JSON)

    }


    def unScheduleAlert() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200)
        try {
            Configuration config = Configuration.get(params.id)
            if (config) {
                params.scheduleDateJSON = importConfigurationService.getDefaultScheduleJSON()
                importConfigurationService.updateScheduleDateAndNextRunDate(params, config)
                responseDTO.status = true
                responseDTO.message = "Alert Unscheduled Successfully!"

            } else {
                responseDTO.status = false
                responseDTO.message = "Alert does not exists."
            }
        } catch (Throwable th) {
            th.printStackTrace()
            responseDTO.status = false
            responseDTO.message = "An error occurred while unscheduling Alert."
        }
        render(responseDTO as JSON)

    }

    def exportToExcel() {
        Map reportData = [:]
        reportData = importConfigurationService.getSortedListData(params)
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
        Map metadata = [sheetName1: "Configurations",
                        columns1  : [
                                [title: messageSource.getMessage('import.configuraton.label.alertName', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.alertType', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.owner', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.configuration.master', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.configuration.template', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.products', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.includeWHODrugs', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.multiIngredient', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.dataSheets', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.date.range.type', null, locale), width: 25],
                                [title: ' X ', width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.start.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.cofiguration.alert.end.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.versionAsOf.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.assigned.to', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.share.with', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.scheduler', null, locale), width: 25]
                        ],
                        sheetName2: "Master Config Template",
                        columns2  : [
                                [title: messageSource.getMessage('import.configuration.label.configuration.master', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.configuration.template', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.products.hierarchy', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.includeWHODrugs', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.multiIngredient', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.date.range.type', null, locale), width: 25],
                                [title: ' X ', width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.start.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.cofiguration.alert.end.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.versionAsOf.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.scheduler', null, locale), width: 25]
                        ]]
        params.criteriaSheetList = dynamicReportService.createCriteriaList(userService.getUser(), null, null, true)
        byte[] file = dynamicReportService.exportToExcelImportConfigurationList(reportData, metadata, params, true)
        String fileName = "Exported Configuration-"+DateUtil.toDateStringWithoutTimezone(new Date()) +"-" + System.currentTimeMillis() + ".xlsx"
        signalAuditLogService.createAuditForExport(params.criteriaSheetList,"Import Configuration: "+params.alertType , "Import Configuration", params, fileName)
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }


    def changeAssignedToGroup(Long alertId, String assignedToValue) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: 'app.assignedTo.changed.success'))
        try {
            Configuration configurationInstance = Configuration.get(alertId)
            Long groupId
            Long userId
            if (configurationInstance) {
                Map productMap = [product: configurationInstance.productSelection, productGroup: configurationInstance.productGroupSelection]
                configurationInstance = userService.assignGroupOrAssignTo(assignedToValue, configurationInstance, productMap)
                configurationInstance = (Configuration) CRUDService.update(configurationInstance)
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = message(code: 'app.assignedTo.changed.fail')
        }
        render(responseDTO as JSON)
    }
    def updateDatasheetsConfig(Long id){
        List datasheetList = params.datasheetValue?.split(',')
        List datasheets = []
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "Updated Configuration Successfully!")
        try {
            Configuration configurationInstance = Configuration.get(id)
            if (configurationInstance) {
                aggregateCaseAlertService.bindDatasheetData(configurationInstance, datasheetList ? datasheetList : [] )
                configurationInstance?.datasheetType = params.enabledSheet?:Constants.DatasheetOptions.CORE_SHEET
                configurationInstance = (Configuration) CRUDService.update(configurationInstance)
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = "Error while updating Configuration"
        }
        render(responseDTO as JSON)
    }
    def updateShareWithForConf(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "Updated Configuration Successfully!")
        try {
            Configuration configurationInstance = Configuration.get(id)

            if (configurationInstance) {
                userService.bindSharedWithConfiguration(configurationInstance, JSON.parse(params.shareWithValue), true, false)

                configurationInstance = (Configuration) CRUDService.update(configurationInstance)
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = "Error while updating Configuration"
        }
        render(responseDTO as JSON)
    }


    String uploadFile() {

        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        MultipartFile fileToUpload = request.getFile('file')
        String originalFileName = fileToUpload.originalFilename
        try {
            Integer fileCount = ImportConfigurationLog.countByImportFileNameAndStatusInList(originalFileName, [ImportConfigurationProcessState.IN_PROCESS, ImportConfigurationProcessState.IN_READ])
           if (fileCount == 0) {
               File sourceFolder = new File(Holders.config.signal.configuration.import.folder.read as String)
               String savedFilePath = "${sourceFolder.absolutePath}/${originalFileName}"
               fileToUpload.transferTo(new File(savedFilePath))
               File uploadedFile = new File(savedFilePath)
               boolean isCorrectFormat = importConfigurationService.checkFileFormat(new File(savedFilePath), params.importConfigurationType)
               XSSFWorkbook workbook = new XSSFWorkbook(savedFilePath)
               XSSFSheet sheet = workbook.getSheetAt(0)
               int lastRowNum = sheet.getLastRowNum()
               if(lastRowNum-2 > 1500 && params.importConfigurationType=='Single Case Alert'){
                   uploadedFile.delete()
                   responseDTO.message = message(code: "app.label.Configuration.upload.file.too.large")
                   responseDTO.status = false
               } else if (isCorrectFormat) {
                   ImportConfigurationLog importConfigurationLog = importConfigurationService.saveImportConfigurationLog(originalFileName, userService.getUser(), params.importConfigurationType)
                   String logsFilePath = "${Holders.config.signal.configuration.import.folder.logs}/${importConfigurationLog.id}"
                   importConfigurationService.createDir(logsFilePath)
                   importConfigurationService.copyFile(uploadedFile, logsFilePath)
                   createAuditForImportConfiguration(importConfigurationLog)
                   responseDTO.message = message(code: "app.label.Configuration.upload.inprogress")
               } else {
                   uploadedFile.delete()
                   responseDTO.message = message(code: "app.label.Configuration.upload.file.format.incorrect")
                   responseDTO.status = false
               }
            } else {
                responseDTO.message = message(code: "app.label.Configuration.upload.inprogress.same.file")
                responseDTO.status = false
            }
        } catch (FileFormatException ex) {
            responseDTO.message = message(code: "app.label.Configuration.please.choose.file")
            responseDTO.status = false
        } catch (Exception ex) {
            responseDTO.message = message(code: "app.label.Configuration.upload.file.format.incorrect")
            responseDTO.status = false
            log.error(alertService.exceptionString(ex))
        }
        render(responseDTO as JSON)
    }

    void createAuditForImportConfiguration(ImportConfigurationLog importConfigurationLog) {
        def auditTrailMap = [
                entityName : 'importConfigurationLog',
                moduleName : "Import Configuration: Import",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: importConfigurationLog.importFileName,
                description: "",
                username   : importConfigurationLog.importedBy.username,
                fullname   : importConfigurationLog.importedBy.fullName
        ]
        List<Map> auditChildMap = []
        def childEntry = [:]
        childEntry = [
                propertyName: "File Name",
                newValue    : importConfigurationLog?.importFileName]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Alert Type",
                newValue    : Constants.AuditLog.typeToConfigMap.get(importConfigurationLog?.importConfType)]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Imported By",
                newValue    : importConfigurationLog?.importedBy?.fullName]
        auditChildMap << childEntry
        signalAuditLogService.createAuditLog(auditTrailMap, auditChildMap)
    }

    def fetchImportConfigurationLog(){
        String timezone = userService.user.preference.timeZone
        List importedConfigurationList = ImportConfigurationLog.list([sort: "importedDate", order: "desc"]).collect {
            [importFileName: it.importFileName, id: it.id,
             generatedFileName:it.status == ImportConfigurationProcessState.SUCCESS ? "${FilenameUtils.removeExtension(it.importFileName)}_Log.xlsx" : '-',
             importedBy: it.importedBy.name,
             importedDate: DateUtil.toDateStringWithTimeInAmPmFormat( it.importedDate, timezone ),
             status: it.status.value()]
        }
        render([importLogList: importedConfigurationList,recordsFiltered: importedConfigurationList?.size()] as JSON)
    }

    def importedConfigurationFile() {
        Boolean isImport = params.boolean('importType') ?: false
        XSSFSheet sheet
        File searchedFile = new File("${Holders.config.signal.configuration.import.folder.logs}/${params.logsId}/${params.fileName}")
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream("${Holders.config.signal.configuration.import.folder.logs}/${params.logsId}/${params.fileName}"))
        if(isImport) {
            sheet = workbook.getSheetAt(0)
            if(!(workbook.getSheet("Criteria"))) {
                XSSFSheet criteriaWorksheet = workbook.createSheet("Criteria")
                workbook.setSheetOrder(criteriaWorksheet.getSheetName(), 0)
                dynamicReportService.writeImage(workbook, criteriaWorksheet)
                dynamicReportService.writeCriteriaSheet(workbook, criteriaWorksheet, userService.getUser(), null, true, false)
                //Filter not required PVS-53068
            }
        } else{
            sheet = workbook.getSheetAt(1)
        }
        User currentUser = userService.getUser()
        String timeZone = currentUser?.preference?.timeZone
        File tempFile = File.createTempFile("${Holders.config.signal.configuration.import.folder.logs}/${params.logsId}/" + new Date().getTime() + "_temp", ".xlsx");
        FileOutputStream fileOutputStream = null;
        if(!isImport){
            XSSFSheet criteriaSheet = workbook.getSheet("Criteria")
            Cell exportUserCell = criteriaSheet.getRow(6).getCell(1)
            Cell exportTimeCell = criteriaSheet.getRow(7).getCell(1)
            Row exportFilterRow = criteriaSheet.getRow(8)
            exportTimeCell.setCellValue((DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone)))
            exportUserCell.setCellValue(userService.getUser().getFullName())
            // Remove Filter row PVS-53068
            if (exportFilterRow != null) {
                criteriaSheet.removeRow(exportFilterRow)
            }
        }
        try {
            fileOutputStream = new FileOutputStream(tempFile)
            workbook.write(fileOutputStream)
            renderImportConfigurationOutputType(tempFile, searchedFile.name)
            signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,isImport ? "Imported File": "Import Log" , "Import Configuration", [:], params.fileName)
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            tempFile.delete()
            workbook?.close()
            fileOutputStream?.close()
        }
    }

    void renderImportConfigurationOutputType(File importConfigFile, String name) {
        String extension = FilenameUtils.getExtension(importConfigFile.name)
        response.contentType = "$extension charset=UTF-8"
        response.contentLength = importConfigFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${name}" + "\"")
        response.getOutputStream().write(importConfigFile.bytes)
        response.outputStream.flush()
    }

    def deleteAlertConfig(){
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: 'app.assignedTo.changed.success'))
        try {
            Configuration config = Configuration.get(params.id)
            User currentUser = userService.getUser()
            if (config.isEditableBy(currentUser)) {
                def deletedConfig = configurationService.deleteConfig(config)

                responseDTO.message = message(code: "app.configuration.delete.success", args: [config.name])
            }else{
                responseDTO.message = message(code: "app.configuration.delete.fail", args: [config.name])
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = message(code: "app.common.error")
        }
        render(responseDTO as JSON)
    }

    def updateProdSelection(){
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        String productSelection = ""
        String productGroupSelection = ""
        try {
            Configuration config = Configuration.get(params.alertId)
            Boolean isProductChanged = false
            Boolean isAuditSkip = false
            if (config) {
                if (params.productSelection == "" || params.productSelection == "[]") {
                    params.productSelection = null
                }
                if (params.productGroupSelection == "" || params.productGroupSelection == "[]") {
                    params.productGroupSelection = null
                }
                if (params.productSelection != null) {
                    isAuditSkip = checkProductSelectionValues(params.productSelection, config.productSelection)
                } else if (params.productGroupSelection != null) {
                    isAuditSkip = checkProductGroupName(params.productGroupSelection, config.productGroupSelection)
                }
                isProductChanged = !(params.productGroupSelection == config.productGroupSelection) || !(params.productSelection == config.productSelection)
                config.productSelection = !isProductChanged  ? config.productSelection : (params.productSelection != "[]" ? params.productSelection : null)
                config.productGroupSelection =  !isProductChanged ?config.productGroupSelection : ( params.productGroupSelection != "[]" ? params.productGroupSelection : null)
                def isProductOrFamily = allowedDictionarySelection(config)
                boolean isProductGroupOnly = config.productSelection && config.productGroupSelection
                if ((!isProductOrFamily || isProductGroupOnly) && config.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    responseDTO.status = false
                    responseDTO.message = message(code: "app.label.product.family.error.message")
                }else {
                    responseDTO.data = [isChanged : isProductChanged]
                    if (isProductChanged) {
                        if (isAuditSkip) {
                            config.skipAudit = true
                        }
                        responseDTO.message = message(code: "app.configuration.update.success")
                    }
                    config.save(flush: true, failOnError: true)
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = "Product is required"
        }
        render(responseDTO as JSON)
    }

    def schedulerHelpExcelDownload(int sampleFile) {
        String sampleScheduleFileDirectory = Holders.config.signal.configuration.sample.schedule.file.path
        def filename = sampleFile == 1 ? Holders.config.signal.configuration.sample.file.name : Holders.config.signal.configuration.sample.schdeule.file.name
        File file = new File(sampleScheduleFileDirectory + filename)
        if(!file.exists()){
            URL res = getClass().getClassLoader().getResource("sample/Sample Scheduler Format.xlsx");
            file = new File(res.toURI().getPath())
        }
        response.contentType = "${dynamicReportService.getContentType("XLSX")}; charset=UTF-8"
        if (file.size()) {
            response.contentLength = file.size()
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-disposition", "Attachment; filename=\"" + "${filename}")
            response.getOutputStream().write(file.bytes)
            response.outputStream.flush()
        } else {
            redirect(controller: "importConfiguration", action: "importScreen")
        }
    }

    def exportMasterChildAlertSample(){
        String filename = 'MasterChildAlertSample.xlsx'
        URL res = getClass().getClassLoader().getResource("sample/MasterChildAlertSample.xlsx")
        File file = new File(res.toURI().getPath())
        if (file.size()) {
            response.contentLength = file.size()
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-disposition", "Attachment; filename=\"" + "${filename}")
            response.getOutputStream().write(file.bytes)
            response.outputStream.flush()
        } else {
            redirect(controller: "importConfiguration", action: "importScreen")
        }
    }

    def exportICRAlertSampleTemplate(){
        String filename = 'ICRAlertTemplate.xlsx'
        URL res = getClass().getClassLoader().getResource("sample/ICRAlertTemplate.xlsx")
        File file = new File(res.toURI().getPath())
        if (file.size()) {
            response.contentLength = file.size()
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-disposition", "Attachment; filename=\"" + "${filename}")
            response.getOutputStream().write(file.bytes)
            response.outputStream.flush()
        } else {
            redirect(controller: "importConfiguration", action: "importScreen")
        }
    }

    def exportAggAlertTemplate(){
        String filename = 'AggregateAlertTemplate.xlsx'
        URL res = getClass().getClassLoader().getResource("sample/AggregateAlertTemplate.xlsx")
        File file = new File(res.toURI().getPath())
        if (file.size()) {
            response.contentLength = file.size()
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-disposition", "Attachment; filename=\"" + "${filename}")
            response.getOutputStream().write(file.bytes)
            response.outputStream.flush()
        } else {
            redirect(controller: "importConfiguration", action: "importScreen")
        }
    }

    def exportMasterAlertUseCases(){
        String filename = 'MasterChildUseCases.pdf'
        URL res = getClass().getClassLoader().getResource("sample/MasterChildUseCases.pdf")
        File file = new File(res.toURI().getPath())
        if (file.size()) {
            response.contentLength = file.size()
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-disposition", "Attachment; filename=\"" + "${filename}")
            response.getOutputStream().write(file.bytes)
            response.outputStream.flush()
        } else {
            redirect(controller: "importConfiguration", action: "importScreen")
        }
    }
    /**
     *Compares the product group name of paramsProductGroupSelection with configProductGroupSelection.
     * @param paramsProductGroupSelection
     * @param configProductGroupSelection
     * @return
     */

    Boolean checkProductGroupName(String paramsProductGroupSelection, String configProductGroupSelection){
        String paramsProductName = AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(paramsProductGroupSelection)
        String configProductName = AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(configProductGroupSelection)
        return paramsProductName == configProductName
    }
    /**
     * Compares the product name of paramsProductSelection with configProductSelection
     * @param paramsProductSelection
     * @param configProductSelection
     * @return
     */
    Boolean checkProductSelectionValues(String paramsProductSelection, String configProductSelection){
        String paramsProductName = AuditLogConfigUtil.getProductSelectionValuesForAudit(paramsProductSelection)
        String configProductName = AuditLogConfigUtil.getProductSelectionValuesForAudit(configProductSelection)
        return paramsProductName == configProductName
    }
}
