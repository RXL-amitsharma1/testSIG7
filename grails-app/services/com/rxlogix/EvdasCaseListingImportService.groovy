package com.rxlogix

import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.user.User
import com.rxlogix.util.CsvDataImporter
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ExcelDataImporter
import com.rxlogix.util.ViewHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.io.FileType
import groovy.sql.Sql
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.io.FilenameUtils
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException
import org.joda.time.DateTimeZone
import com.rxlogix.util.CsvDataImporter
import com.rxlogix.util.ViewHelper
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException
import org.springframework.util.StringUtils
import java.text.ParseException

@Transactional
class EvdasCaseListingImportService {

    def signalDataSourceService
    def userService
    def messageSource
    EvdasDataImportService evdasDataImportService
    def applicationSettingsService
    def emailNotificationService
    def config = Holders.config
    File readFolder
    File uploadFolder
    File processSuccessDir
    File processFailDir
    def dataSource_eudra

    def checkAndCreateBaseDirs() {
        File baseFolder = new File(config.signal.evdas.case.line.listing.import.folder.base as String)
        readFolder = new File(config.signal.evdas.case.line.listing.import.folder.read as String)
        uploadFolder = new File(config.signal.evdas.case.line.listing.import.folder.upload as String)
        processSuccessDir = new File(config.signal.evdas.case.line.listing.import.folder.success as String)
        processFailDir = new File(config.signal.evdas.case.line.listing.import.folder.fail as String)
        if (!baseFolder.exists()) {
            log.info("Base folder not found, creating it.")
            baseFolder.mkdir()
        }
        if (!readFolder.exists()) {
            log.info("Source folder not found, creating it.")
            readFolder.mkdir()
        }

        if (!uploadFolder.exists()) {
            log.info("Upload folder not found, creating it.")
            uploadFolder.mkdir()
        }
        if (!processSuccessDir.exists()) {
            log.info("Success folder not found, creating it.")
            processSuccessDir.mkdir()
        }
        if (!processFailDir.exists()) {
            log.info("Fail folder not found, creating it.")
            processFailDir.mkdir()
        }

        checkAndCreateSubstanceFolders()
    }

    def checkAndCreateSubstanceFolders() {
        List substanceNameList = config.signal.evdas.case.line.listing.default.substance.list
        substanceNameList.each { String substanceName ->
            if (substanceName.length() > Constants.EvdasUpload.FOLDER_MAX_SIZE_LIMIT) {
                substanceName = substanceName.substring(Constants.EvdasUpload.FOLDER_MIN_SIZE_LIMIT, Constants.EvdasUpload.FOLDER_MAX_SIZE_LIMIT)
            }
            File readFolderSubstance = new File(readFolder.absolutePath + "/" + substanceName)
            File uploadFolderSubstance = new File(uploadFolder.absolutePath + "/" + substanceName)
            File processSuccessDirSubstance = new File(processSuccessDir.absolutePath + "/" + substanceName)
            File processFailDirSubstance = new File(processFailDir.absolutePath + "/" + substanceName)

            if (!readFolderSubstance.exists()) {
                log.info("${substanceName} folder not found in Read folder, creating it.")
                readFolderSubstance.mkdir()
            }
            if (!uploadFolderSubstance.exists()) {
                log.info("${substanceName} folder not found in Upload folder, creating it.")
                uploadFolderSubstance.mkdir()
            }
            if (!processSuccessDirSubstance.exists()) {
                log.info("${substanceName} folder not found in Process Success Dir folder, creating it.")
                processSuccessDirSubstance.mkdir()
            }
            if (!processFailDirSubstance.exists()) {
                log.info("${substanceName} folder not found in Process Fail Dir folder, creating it.")
                processFailDirSubstance.mkdir()
            }
        }
    }

    def initiateDataImport() {
        List allFilesInSourceFolder = readAllFilesFromSourceFolder()
        Map<String, List> filteredFileList = filterFilesForDataImport(allFilesInSourceFolder)
        filterSourceFolder(User.first(), filteredFileList, [:], false, "Case Listing")
    }

    def processWaitingFiles() {
        List<EvdasFileProcessLog> listToProcess = EvdasFileProcessLog.findAllByStatusAndDataType(EvdasFileProcessState.IN_PROCESS, 'Case Listing', [sort: "dateCreated", order: "asc"])
        if (listToProcess) {
            log.info("Case Listing file process found ${listToProcess?.size()} files to process.")
            try {
                log.info("Case Listing file process is acquiring lock.")
                applicationSettingsService.enableEvdasCaseListingUploadLock()
                startProcessing(listToProcess)
                if(!StringUtils.isEmpty(Holders.config.signal.evdas.file.upload.caselisting.toAddresses)) {
                    List<String> toAddresses = Holders.config.signal.evdas.file.upload.caselisting.toAddresses.split(",")
                    emailNotificationService.evdasCaseListingFileUploadEmailNotification(listToProcess, toAddresses)
                } else {
                    log.error("Evdas Case Listing file upload mail cannot be sent because signal.evdas.file.upload.caselisting.toAddresses properties is missing...")
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                log.info("Case Listing file process is releasing lock.")
                applicationSettingsService.releaseEvdasCaseListingUploadLock()
            }
        }
    }

    void startProcessing(List<EvdasFileProcessLog> filesNameToProcess) {
        EvdasFileProcessState fileProcessState
        File fileToProcess
        Map parsedDataFromFile
        filesNameToProcess.each { EvdasFileProcessLog fileProcessLog ->
            fileToProcess = new File(fileProcessLog.savedName ? fileProcessLog.savedName : fileProcessLog.fileName)
            log.info "Processing ${fileToProcess.name} ${fileProcessLog.isManual ? "uploaded manually" : ""}"
            try {
                parsedDataFromFile = processFile(fileToProcess)
                if (!parsedDataFromFile.status) {
                    fileProcessState = EvdasFileProcessState.FAILED
                    parsedDataFromFile.failMessage = messageSource.getMessage("app.label.evdas.data.upload.case.listing.file.error", null, Locale.getDefault())
                } else {
                    List dataList = parsedDataFromFile?.processable ? parsedDataFromFile?.processable as List : []
                    fileProcessState = persistDataInDatabase(fileToProcess.name,dataList) ? EvdasFileProcessState.SUCCESS : EvdasFileProcessState.FAILED
                    if (fileProcessState == EvdasFileProcessState.FAILED) {
                        parsedDataFromFile.failMessage = messageSource.getMessage("app.label.evdas.data.upload.case.listing.file.data.error", null, Locale.getDefault())
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace()
                parsedDataFromFile.failMessage = messageSource.getMessage("app.label.evdas.data.upload.case.listing.file.error", null, Locale.getDefault())
                fileProcessState = EvdasFileProcessState.FAILED
            }
            moveFile([fileProcessLog.savedName?:fileProcessLog.fileName], fileProcessState)
            updateLog(fileProcessLog.savedName?:fileProcessLog.fileName, fileProcessState, parsedDataFromFile)
        }
    }

    void filterSourceFolder(User user, Map<String, List> filteredFileList, Map fileAndDescriptionMap, Boolean isManual, String dataType , String savedName = null) {
        saveLogs(filteredFileList['fail'], EvdasFileProcessState.FAILED, user, fileAndDescriptionMap, isManual, dataType , savedName)
        moveFile(filteredFileList['fail'], EvdasFileProcessState.FAILED)
        saveLogs(filteredFileList['pass'], EvdasFileProcessState.IN_PROCESS, user, fileAndDescriptionMap, isManual, dataType , savedName)
    }

    List readAllFilesFromSourceFolder() {
        List fileListToProcess = []
        if (readFolder?.exists() && readFolder?.isDirectory()) {
            readFolder?.eachFileRecurse(FileType.FILES) { file ->
                if (EvdasFileProcessLog.countByFileNameAndStatusAndDataType(file.path, EvdasFileProcessState.IN_PROCESS, "Case Listing") == 0) {
                    fileListToProcess << file.absolutePath
                }
            }
        }
        fileListToProcess
    }

    Map filterFilesForDataImport(List fileListToProcess) {
        Map<String, List> fileToProcess = [pass: [], fail: []]
        List temp
        fileListToProcess.each { String absoluteFileName ->
            if (validateFileName(absoluteFileName)) {
                temp = fileToProcess['pass']
                temp.add(absoluteFileName)
                fileToProcess['pass'] = temp
            } else {
                temp = fileToProcess['fail']
                temp.add(absoluteFileName)
                fileToProcess['fail'] = temp
            }
        }
        fileToProcess
    }

    Boolean validateFileName(String fullFileName) {
        String fileExtension = FilenameUtils.getExtension(fullFileName)
        List supportedFormats = ['xls', 'xlsx' , 'csv']
        supportedFormats.contains(fileExtension)
    }

    Map processFile(File fileToBeProcessed) {
        switch (FilenameUtils.getExtension(fileToBeProcessed.name)) {
            case 'xls':
            case 'xlsx':
            case 'xlx':
            case 'xlsm':
            case 'xlm':
                processExcelFile(fileToBeProcessed)
                break
            case 'csv':
                processCsvFile(fileToBeProcessed)
                break
            default:
                log.error("Not supported")
        }
    }

    Map processExcelFile(File fileToBeProcessed) {
        ExcelDataImporter.getCaseListingData(fileToBeProcessed, config.signal.evdas.case.line.listing.import.COLUMN_TYPE_MAP as Map, 0)
    }

    Boolean persistDataInDatabase(String fileToProcessName,List data) {
        Boolean status = false
        Map caseListingColumnMap = config.signal.evdas.case.line.listing.column.name.mapping.map
        final Sql sqlObj
        try {
            if (data) {
                log.info("Going to persist data")
                sqlObj = new Sql(signalDataSourceService.getDataSource('eudra'))

                //This clears any existing data in the staging tables.
                sqlObj.call("call p_truncate_stage_tables()")

                sqlObj.withBatch(10000) { stmt ->
                    data.each {
                        String sql = """insert into evdas_case_listing_stg 
                 (substance_name, eu_local_number, wwid, ev_receipt_date, report_type, prim_source_qual, prim_source_country, lit_ref, pat_age_grp, pat_age_grp_rept, pat_sex, par_child_report, react_list, suspect_list, concomitant_list, icsr_form)
                  values
                 ('${it[caseListingColumnMap["substance_name"]]}', '${it[caseListingColumnMap["eu_local_number"]]}', '${it[caseListingColumnMap["wwid"]]}', 
                    TO_DATE('${it[caseListingColumnMap["ev_receipt_date"]]}', 'DD/MM/YYYY'), '${it[caseListingColumnMap["report_type"]]}', 
                    '${it[caseListingColumnMap["prim_source_qual"]]}', '${it[caseListingColumnMap["prim_source_country"]]}', 
                    '${it[caseListingColumnMap["lit_ref"]]}', '${it[caseListingColumnMap["pat_age_grp"]]}', '${it[caseListingColumnMap["pat_age_grp_rept"]]}', 
                    '${it[caseListingColumnMap["pat_sex"]]}', '${ it[caseListingColumnMap["par_child_report"]]}', 
                    '${it[caseListingColumnMap["react_list"]]?.replaceAll("'", "''")}', '${ it[caseListingColumnMap["suspect_list"]]?.replaceAll("'", "''") }', 
                    '${it[caseListingColumnMap["concomitant_list"]]?.replaceAll("'", "''")}', '${ it[caseListingColumnMap["icsr_form"]]}')"""
                        stmt.addBatch(sql)
                    }
                }
                log.info("Done persisting in db")
                log.info("Calling stored procedure")
                def query = "{call pkg_evdas_transform.p_main(?,?)}"
                sqlObj.call("{call pkg_evdas_transform.p_main(?,?)}",
                        [fileToProcessName,Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]
                ) { new_gen_info ->
                    if (new_gen_info != null) {
                        while (new_gen_info.next()) {
                            if (new_gen_info.getAt(0) == 1) {
                                status = true
                            }
                        }
                    }
                }
                log.info(status as String)
            }
        } catch (Exception e) {
            e.printStackTrace()
            status = false
        } finally {
            sqlObj?.close()
            return status
        }
    }

    void moveFile(List fileNames, EvdasFileProcessState fileProcessState) {
        File fileToMove
        fileNames.each { String fileName ->
            fileToMove = new File(fileName)
            def fileToMovePathBreakdown = fileToMove.absolutePath.split('/')
            fileToMovePathBreakdown[-3] = (fileProcessState == EvdasFileProcessState.SUCCESS) ? processSuccessDir.name : processFailDir.name
            File newDestination = new File(fileToMovePathBreakdown.join('/'))
            fileToMove.renameTo(newDestination)
        }
    }

    void saveLogs(List fileNames, EvdasFileProcessState fileProcessState, User user, Map<String, String> fileAndDescriptionMap = [:], Boolean isManual, String dataType , String savedName) {
        fileNames.each { String absoluteFileName ->
            File fileToProcess = savedName ? new File(savedName) : new File(absoluteFileName)
            Date recordStartDate = null
            Date recordEndDate = null
            String reason = null
            List<Date> startEndDateList = getStartAndEndDate(fileToProcess.name)
            String description = fileAndDescriptionMap[fileToProcess.name] ?: null
            String substanceName = fileToProcess.getParentFile().name
            if (startEndDateList && startEndDateList.size() == 2) {
                recordStartDate = startEndDateList[0]
                recordEndDate = startEndDateList[1]
            }
            new EvdasFileProcessLog(substances: substanceName, fileName: absoluteFileName, status: fileProcessState, description: description, isManual: isManual,
                    updatedBy: user, reason: reason, recordStartDate: recordStartDate, recordEndDate: recordEndDate, dataType: dataType, savedName: savedName).save()
        }
    }

    void updateLog(String fullFileName, EvdasFileProcessState fileProcessState, Map parsedDataFromFile) {
        EvdasFileProcessLog fileProcessLog = EvdasFileProcessLog.findBySavedNameAndStatus(fullFileName, EvdasFileProcessState.IN_PROCESS) ?:EvdasFileProcessLog.findByFileNameAndStatus(fullFileName, EvdasFileProcessState.IN_PROCESS)
        fileProcessLog?.status = fileProcessState
        fileProcessLog?.totalRecords = parsedDataFromFile?.totalRecords ?: Constants.Commons.UNDEFINED_NUM as Integer
        fileProcessLog?.processedRecords = parsedDataFromFile?.processable ? parsedDataFromFile?.processable.size() : Constants.Commons.UNDEFINED_NUM
        fileProcessLog?.reason = parsedDataFromFile?.failMessage
        fileProcessLog?.status = fileProcessState
        fileProcessLog.save(flush: true, failOnError: true)
        log.info("Total records -> ${fileProcessLog.totalRecords}")
        log.info("Processed records -> ${fileProcessLog.processedRecords}")
        log.info("Discarded records -> ${parsedDataFromFile.discarded ? parsedDataFromFile.discarded.size() : Constants.Commons.UNDEFINED_NUM}")
        log.info("Log updated successfully.")
        if(fileProcessState in [EvdasFileProcessState.SUCCESS,EvdasFileProcessState.FAILED]){
            log.info("Now creating audit entry for success or error")
            evdasDataImportService.createAuditLogForEvdasUpload(fileProcessLog)
        }
    }

    List<Date> getStartAndEndDate(String fileName) {
        List<Date> startEndDate = []
        if (fileName.contains('$$')) {
            try {
                startEndDate = fileName.split('\\$\\$').last().split('\\.').first().split('-').collect {
                    DateUtil.stringToDate(it, DateUtil.DEFAULT_DATE_FORMAT_WITHOUT_SEPARATOR, DateTimeZone.UTC.ID)
                }
            } catch (ParseException e) {
                e.printStackTrace()
            }
        }
        startEndDate
    }

    List<String> listAllSubstanceName() {
        List<String> substanceName = []
        uploadFolder?.eachFile(FileType.DIRECTORIES) { directory ->
            substanceName << directory.name
        }
        substanceName.sort({it.toUpperCase()})
    }

    Map processCsvFile(File fileToBeProcessed) {
        getCsvCaseListingData(fileToBeProcessed, config.signal.evdas.case.line.listing.import.COLUMN_TYPE_MAP as Map)
    }

    Map getCsvCaseListingData(File file, Map COLUMN_TYPE_MAP) {
        Map resultMap = [processable: null, discarded: 0, totalRecords: 0, status: false]
        List actualColumnMapping = CsvDataImporter.fetchColumnsCaseListing(file)
        List valuesToProcess = []
        List valuesToDiscard = []
        Boolean startCapturingData = false
        try {
            Reader inputFile = new FileReader(file)
            CSVParser parser = CSVParser.parse(inputFile, CSVFormat.EXCEL)
            if (parser) {
                String substanceName = file.parentFile.name
                Integer totalColumns = COLUMN_TYPE_MAP.keySet().size()
                Integer totalRowCount = 0
                Integer columnNumber = 0

                for (CSVRecord csvRecord : parser) {
                    columnNumber = 0
                    if (!startCapturingData) {
                        if (CsvDataImporter.removeBom(csvRecord.get(0)) == Constants.ExcelDataUpload.CASE_LISTING_CAPTURE_POINTER) {
                            startCapturingData = true
                        }
                    } else {
                        Map map = [:]
                        map.put("substance_name", substanceName)
                        if (csvRecord.get(0)) {
                            totalRowCount += 1
                            for (column in csvRecord) {
                                if (actualColumnMapping.get(columnNumber) == "EV Gateway Receipt Date".toUpperCase()) {
                                    column = CsvDataImporter.changeDateFormat(column)
                                }
                                if (column) {
                                    switch (COLUMN_TYPE_MAP[columnNumber]) {
                                        case 'HYPERLINK':
                                            map.put(actualColumnMapping.get(columnNumber), CsvDataImporter.trimHyperLink(column))
                                            break
                                        case 'STRING':
                                        default:
                                            map.put(actualColumnMapping.get(columnNumber), CsvDataImporter.trimCarriage(column))
                                    }
                                } else {
                                    map.put(actualColumnMapping.get(columnNumber), null)
                                }
                                columnNumber++
                            }
                            valuesToProcess.add(map)
                        }
                    }
                }
                resultMap = [processable: valuesToProcess, discarded: valuesToDiscard, totalRecords: totalRowCount, status: true]
            }
        } catch (NotOfficeXmlFileException ex) {
            resultMap.failMessage = ViewHelper.getMessage("app.label.evdas.data.upload.case.listing.file.corrupt.error")
            ex.printStackTrace()
        } catch (Exception ex) {
            ex.printStackTrace()
            resultMap.failMessage = ViewHelper.getMessage("app.label.evdas.data.upload.case.listing.file.error")
        }
        return resultMap
    }

}