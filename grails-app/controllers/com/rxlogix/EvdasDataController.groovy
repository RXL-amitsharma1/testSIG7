package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.enums.EvdasFileProcessState
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.io.FileType
import com.rxlogix.user.User
import com.rxlogix.util.FileNameCleaner
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import com.rxlogix.exception.FileFormatException
import org.springframework.web.multipart.MultipartFile

@Secured(["isAuthenticated()"])
class EvdasDataController {

    def evdasDataImportService
    def evdasCaseListingImportService
    def evdasDataService
    def userService
    def attachmentableService
    SignalAuditLogService signalAuditLogService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        [substanceNames: evdasCaseListingImportService.listAllSubstanceName()]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def upload(String description, String dataType, Integer optDuplicate, String substanceName) {
        def config = Holders.config
        MultipartFile fileToUpload = request.getFile('file')
        User user = userService.user
        String originalFileName = fileToUpload.originalFilename
        def fileCount = EvdasFileProcessLog.countByFileNameAndStatusAndDataType(fileToUpload.originalFilename, EvdasFileProcessState.IN_PROCESS, dataType)
        if (fileCount == 0) {
            try {
                String savedName = attachmentableService.getRandomNameFile()
                String extension =  FilenameUtils.getExtension(fileToUpload.originalFilename)
                String savedFileName = savedName + '.' + extension
                attachmentableService.checkExtension(originalFileName)
                if (dataType == "Case Listing") {
                    File sourceFolder = new File(config.signal.evdas.case.line.listing.import.folder.upload as String)
                    Map fileAndDescriptionMap = [:]
                    String savedFilePath = "${sourceFolder.absolutePath}/${substanceName}/${savedFileName}"
                    File uploadedFileDestination = new File("${savedFilePath}")
                    fileToUpload.transferTo(uploadedFileDestination)
                    fileAndDescriptionMap["$savedFileName"] = description
                    String originalFilePath = "${sourceFolder.absolutePath}/${substanceName}/${originalFileName}"
                    Map<String, List> filteredFileMap = evdasCaseListingImportService.filterFilesForDataImport([originalFilePath])
                    if (filteredFileMap.pass.isEmpty()) {
                        flash.error = message(code: 'app.label.evdas.data.upload.file.format.not.supported')
                    } else {
                        evdasCaseListingImportService.filterSourceFolder(user, filteredFileMap, fileAndDescriptionMap, true, dataType , savedFilePath)
                        flash.message = message(code: "app.label.evdas.data.upload.inprogress")
                    }
                } else {
                    File sourceFolder = new File(config.signal.evdas.data.import.folder.upload as String)
                    Map fileAndDescriptionMap = [:]
                    String fileName
                    String savedFilePath = "${sourceFolder.absolutePath}/${savedFileName}"
                    String fileExtension = FilenameUtils.getExtension(fileToUpload.originalFilename)
                    fileToUpload.transferTo(new File(savedFilePath))
                    if (fileExtension == 'xls') {
                        fileName = evdasDataImportService.convertXLStoXLSX(savedFileName, sourceFolder)
                    } else {
                        fileName = "${fileToUpload.originalFilename}"
                    }
                    fileAndDescriptionMap["$fileName"] = description
                    Map<String, List> filteredFileMap = evdasDataImportService.filterFilesForDataImport([fileName], true ,savedFileName)
                    if (!filteredFileMap.fail.isEmpty()) {
                        flash.error = filteredFileMap.fail.reason.first()
                    } else {
                        evdasDataImportService.filterSourceFolder(user, filteredFileMap, fileAndDescriptionMap, true, dataType, optDuplicate , savedFileName)
                        flash.message = message(code: "app.label.evdas.data.upload.inprogress")
                    }
                }
            } catch(FileFormatException ex) {
                flash.error = message(code: "file.format.not.supported")
            } catch (IOException ex) {
                log.error(ex.getMessage())
                flash.error = message(code: "app.label.evdas.data.upload.no.file")
            } catch (Exception ex) {
                log.error(ex.getMessage())
                flash.error = message(code: "app.label.evdas.data.upload.eRMR.file.error")
            }
        } else {
            flash.message = message(code: "app.label.evdas.data.upload.inprogress.same.file")
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def fetchEvdasData() {
        render([response: evdasDataService.fetchFileProcessLog()] as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def downloadDocument(EvdasFileProcessLog evdasFileProcessLog) {
        File file = evdasDataService.fetchDownloadableFile(evdasFileProcessLog)
        String fileExtension = FilenameUtils.getExtension(evdasFileProcessLog.fileName)
        String reportName = evdasFileProcessLog.fileName
        response.contentType = "$fileExtension charset=UTF-8"
        response.contentLength = file.length()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def downloadErrorLog(EvdasFileProcessLog evdasFileProcessLog) {
        File file = File.createTempFile("log", ".txt")
        file.write(evdasFileProcessLog.reason)
        response.setHeader "Content-disposition", "attachment; filename=${file.name}"
        response.contentType = 'text-plain'
        response.outputStream << file.text
        response.outputStream.flush()
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def getFilesInfo() {

        def readFilePath = Holders.getGrailsApplication()?.getConfig()?.signal.evdas.data.import.folder.read
        def successFilePath = Holders.getGrailsApplication()?.getConfig()?.signal.evdas.data.import.folder.success
        def failFilePath = Holders.getGrailsApplication()?.getConfig()?.signal.evdas.data.import.folder.fail

        def readFileList = []
        def readFiles = new File(readFilePath)
        readFiles.eachFileRecurse(FileType.FILES) { file ->
            def fPath = file.getPath()
            def fPathName = fPath.split(readFilePath)[1]
            readFileList.add(['files': fPathName.substring(1)])
        }

        def successFileList = []
        def successFiles = new File(successFilePath)
        successFiles.eachFileRecurse(FileType.FILES) { file ->
            def fPath = file.getPath()
            def fPathName = fPath.split(successFilePath)[1]
            successFileList.add(['files': fPathName.substring(1)])
        }

        def failFileList = []
        def failFiles = new File(failFilePath)
        failFiles.eachFileRecurse(FileType.FILES) { file ->
            def fPath = file.getPath()
            def fPathName = fPath.split(failFilePath)[1]
            failFileList.add(['files': fPathName.substring(1)])
        }

        def fileData = ["filesDetected": readFileList, "success": successFileList, "fail": failFileList, path: readFilePath]

        render(fileData as JSON)
    }
}
