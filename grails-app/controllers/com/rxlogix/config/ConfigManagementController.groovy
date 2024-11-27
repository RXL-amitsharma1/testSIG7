package com.rxlogix.config

import com.rxlogix.InboxLog
import com.rxlogix.audit.AuditTrail
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.async.Promise
import grails.util.Holders
import com.rxlogix.Constants
import org.springframework.http.HttpStatus
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import groovyx.net.http.Method
import java.text.SimpleDateFormat

import static grails.async.Promises.task

@Secured(["isAuthenticated()"])
class ConfigManagementController {
    def  notificationHelper
    def userService
    def reportIntegrationService
    def configManagementService
    def cacheService
    static final exportExcelName = "APP_CONFIG.xlsx"
    def index() {

    }

    def generateConfigurationFile() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        def currentUser = userService.getUser()

        log.info("Generate configuration file method called")
        Promise promise = task {
            String filepath = Holders.config.pvadmin.api.export.file
            XSSFWorkbook workbook = new XSSFWorkbook()
            configManagementService.exportCriteriaSheet(workbook)
            configManagementService.exportBusinessConfiguration(workbook)
            configManagementService.exportTechnicalConfiguration(workbook)
            File exportDir = new File(configManagementService.USER_HOME_DIR + filepath)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            String fileName = configManagementService.USER_HOME_DIR + filepath + "${exportExcelName}"
            FileOutputStream outputStream = new FileOutputStream(fileName)
            workbook.write(outputStream);
            workbook.close();
            log.info("Generated file exported to path: " + filepath)
            return fileName
        }
        promise.onComplete { def filepath ->
            InboxLog.withTransaction {
                InboxLog inboxLog = new InboxLog(notificationUserId:currentUser?.id, level: NotificationLevel.INFO, message: "Configuration Export Generated",
                        messageArgs: "", type:  "Configuration Export", subject:" EXPORT FILE",
                        content: "<a href=\"${Holders.config.grails.serverURL}/configManagement/downloadFile?isExport=true&filepath=${filepath}\">Download Configuration export file</a>", createdOn: new Date(), inboxUserId: currentUser?.id, isNotification: true)
                inboxLog.save(flush: true)
                notificationHelper.pushNotification(inboxLog)
            }
        }
        promise.onError { Throwable err ->
            log.info("An error occured while generating configuration file.")
            err.printStackTrace()
            InboxLog.withTransaction {
                InboxLog inboxLog = new InboxLog(notificationUserId:currentUser?.id, level: NotificationLevel.ERROR, message: "Configuration Export failed",
                        messageArgs: "", type: "Configuration Export", subject:" Configuration Export failed.",
                        content: "An error occurred while generating export file.", createdOn: new Date(), inboxUserId: currentUser?.id, isNotification: true)
                inboxLog.save(flush: true)
                notificationHelper.pushNotification(inboxLog)
            }

        }
        log.info("Generate configuration file method Ended")

        render(responseDTO as JSON)
    }

    def downloadFile() {
        String fileName
        File file
        if (params.isExport) {
            String appURL = Holders.config.grails.serverURL
            def url = new URL(appURL)
            String environmentName = url.host
            file = new File(params.filepath)
            Date date = new Date()
            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MMM-dd HH:mm" )
            fileName = "${environmentName}_" + sdf.format(date) + "GMT.xlsx"

        } else {
            file = new File(params.filepath as String)
            fileName = file.getName()
        }
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)

    }

    def compareConfigurations() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)

        MultipartFile configFileFirst = params.configFileFirst
        MultipartFile configFileSecond = params.configFileSecond
        def currentUser = userService.getUser()

        log.info("Compare configuration file method called.")
        def url = Holders.config.pvadmin.api.url
        def path = Holders.config.pvadmin.api.compare.uri
        String filePath = Holders.config.pvadmin.api.compare.file.path
        String firstFileName = configFileFirst.getOriginalFilename()
        String secondFileName = configFileSecond.getOriginalFilename()
        File fileDir = new File(configManagementService.USER_HOME_DIR + filePath)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        configFileFirst.transferTo(new File(configManagementService.USER_HOME_DIR + filePath + "/${firstFileName}"))
        configFileSecond.transferTo(new File(configManagementService.USER_HOME_DIR + filePath + "/${secondFileName}"))
        log.info("files moved to location : " + configManagementService.USER_HOME_DIR + filePath)
        Promise promise = task {
            String compareGenerateTime = DateUtil.toDateStringWithTimeInAmPmFormat(currentUser) + userService.getGmtOffset(currentUser.preference.timeZone)
            Map queryMap = ["configFileFirst": firstFileName, "configFileSecond": secondFileName, "path": filePath, "sheetPrimaryKeys": Holders.config.compare.sheet.primaryKeys,"user":currentUser.fullName,"compareGenerateTime":compareGenerateTime]
            def response = reportIntegrationService.postData(url, path, queryMap, Method.POST ,"PVAdmin")
            log.info("API response from admin: " + response)
            if (response.status == HttpStatus.OK) {
                return response.result?.generatedFilePath
            } else {
                throw new Exception("An error occurred at pvadmin.")
            }
        }
        promise.onComplete { String filepath ->
            InboxLog.withTransaction {
                InboxLog inboxLog = new InboxLog(notificationUserId:currentUser?.id, level: NotificationLevel.INFO, message: "Configuration Difference File Generated",
                        messageArgs: "", type: "Compare Configurations", subject:" Compare Configuration file",
                        content: "<a href=\"${Holders.config.grails.serverURL}/configManagement/downloadFile?filepath=${configManagementService.USER_HOME_DIR}${filepath}\">Download Config Difference file</a>", createdOn: new Date(), inboxUserId: currentUser?.id, isNotification: true)
                inboxLog.save(flush: true)
                notificationHelper.pushNotification(inboxLog)
            }
        }
        promise.onError { Throwable err ->
            log.info("An error occurred while generating difference file.")
            err.printStackTrace()
            InboxLog.withTransaction {
                InboxLog inboxLog = new InboxLog(notificationUserId:currentUser?.id, level: NotificationLevel.ERROR, message: "Configuration Difference File Failed",
                        messageArgs: "", type: "Compare Configurations", subject:" Compare Configuration file generation failed",
                        content: "An error occurred while generating difference file.", createdOn: new Date(), inboxUserId: currentUser?.id, isNotification: true)
                inboxLog.save(flush: true)
                notificationHelper.pushNotification(inboxLog)
            }
        }
        log.info("Compare configuration file method Ended")
        render(responseDTO as JSON)
    }


    def importDataFromFile() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        def currentUser = userService.getUser()
        MultipartFile configFile = params.configFile
        log.info("Import configuration file method called.")
        String baseDirectoryPath = Holders.config.pvadmin.api.import.read.directory
        File baseFolder = new File(configManagementService.USER_HOME_DIR + baseDirectoryPath)
        if (!baseFolder.exists()) {
            baseFolder.mkdirs()
        }
        File convFile = new File(configManagementService.USER_HOME_DIR + baseDirectoryPath + exportExcelName)
        configFile.transferTo(convFile)
        Promise promise = task {
            configManagementService.currentConfigurationsBackup()
            configManagementService.parseExcelAndPopulateDB(baseDirectoryPath + exportExcelName )
        }
        promise.onComplete {
            log.info("Config file import completed successfully.")
            InboxLog.withTransaction {
                InboxLog inboxLog = new InboxLog(notificationUserId:currentUser?.id, level: NotificationLevel.INFO, message: "Configuration import Completed.",
                        messageArgs: "", type: "Configurations Import", subject:" Import Configuration file",
                        content: "Configuration import Completed.", createdOn: new Date(), inboxUserId: currentUser?.id, isNotification: true)
                inboxLog.save(flush: true)
                notificationHelper.pushNotification(inboxLog)
            }
        }
        promise.onError { Throwable err ->
            log.info("An error occurred while importing config file.")
            err.printStackTrace()
            InboxLog.withTransaction {
                InboxLog inboxLog = new InboxLog(notificationUserId:currentUser?.id, level: NotificationLevel.ERROR, message: "Configuration import Failed.",
                        messageArgs: "", type: "Configurations Import", subject:" Import Configuration file",
                        content: "Configuration import Failed.", createdOn: new Date(), inboxUserId: currentUser?.id, isNotification: true)
                inboxLog.save(flush: true)
                notificationHelper.pushNotification(inboxLog)
            }
        }
        log.info("Import configuration file method Ended")
        render(responseDTO as JSON)
    }

    def refreshTechConfig() {
        ResponseDTO responseDTO = new ResponseDTO(status: false)
        try {
            def url = Holders.config.pvadmin.api.url
            def path = Holders.config.pvadmin.api.refresh.configurations.uri
            Map queryMap = ["appName": "PVS"]
            def response = reportIntegrationService.postData(url, path, queryMap, Method.POST, "PVAdmin")
            log.info("API response from admin: " + response)

            if (response.status == HttpStatus.OK) {
                Holders.config.refresh.tech.success = false
                Boolean checkStatus = configManagementService.checkLastUpdatedTimeSync(0)
                log.info("tech config update status : "  + checkStatus)
                responseDTO.status = checkStatus
                responseDTO.data = checkStatus ? Holders.config.last.configuration.refreshed : [:]
            }
        } catch (Exception exception) {
            log.info("An error occurred when refreshing config")
            exception.printStackTrace()
        }
        render(responseDTO as JSON)
    }

}