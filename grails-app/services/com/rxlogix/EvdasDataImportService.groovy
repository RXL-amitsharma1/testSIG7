package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.mapping.ERMRConfig
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.user.User
import com.rxlogix.util.CsvDataImporter
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ExcelDataImporter
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.io.FilenameUtils
import org.joda.time.DateTimeZone
import org.springframework.context.MessageSource
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import java.text.SimpleDateFormat

@Transactional
class EvdasDataImportService {

    def signalDataSourceService
    def userService
    def applicationSettingsService
    def signalAuditLogService
    MessageSource messageSource
    def config = Holders.config
    def emailNotificationService
    File readFolder
    File uploadFolder
    File processSuccessDir
    File processFailDir
    def dataSource_eudra
    SimpleDateFormat sdf = new SimpleDateFormat('ddMMMyyyy')
    static List firstFifteenDaysCombinations = [['01Jan', '15Jan'], ['01Feb', '15Feb'], ['01Mar', '15Mar'], ['01Apr', '15Apr'], ['01May', '15May'], ['01Jun', '15Jun'],
                                      ['01Jul', '15Jul'], ['01Aug', '15Aug'], ['01Sep', '15Sep'], ['01Oct', '15Oct'], ['01Nov', '15Nov'], ['01Dec', '15Dec']]

    def checkAndCreateBaseDirs() {
        File baseFolder = new File(config.signal.evdas.data.import.folder.base as String)
        readFolder = new File(config.signal.evdas.data.import.folder.read as String)
        uploadFolder = new File(config.signal.evdas.data.import.folder.upload as String)
        processSuccessDir = new File(config.signal.evdas.data.import.folder.success as String)
        processFailDir = new File(config.signal.evdas.data.import.folder.fail as String)
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
    }

    def initiateDataImport() {
        List filesToUpload = []
        List allFileInSourceFolder = readAllFilesFromSourceFolder()
        allFileInSourceFolder.each{ File file ->
            String fileExtension = FilenameUtils.getExtension(file.name)
            if (fileExtension == 'xls') {
                String convertedFileName = convertXLStoXLSXExt(file, readFolder)
                filesToUpload.add(convertedFileName)
            } else {
                filesToUpload.add(file.name)
            }
        }
        Map<String, List> filteredFileList = filterFilesForDataImport(filesToUpload, false)
        filterSourceFolder(User.first(), filteredFileList, [:], false, "eRMR", config.signal.evdas.duplicate.data.handle as Integer)
    }

    def processWaitingFiles() {
        List<EvdasFileProcessLog> listToProcess = EvdasFileProcessLog.findAllByStatusAndDataType(EvdasFileProcessState.IN_PROCESS, 'eRMR', [sort: "dateCreated", order: "asc"])
        if (listToProcess) {
            log.info("eRMR file process found ${listToProcess?.size()} files to process.")
            try {
                log.info("eRMR file process is acquiring lock.")
                applicationSettingsService.enableEvdasErmrUploadLock()
                startProcessing(listToProcess)
                if(!StringUtils.isEmpty(Holders.config.signal.evdas.file.upload.ermr.toAddresses)) {
                    List<String> toAddresses = Holders.config.signal.evdas.file.upload.ermr.toAddresses.split(",")
                    emailNotificationService.evdasERMRFileUploadEmailNotification(listToProcess, toAddresses)
                } else {
                    log.error("Evdas eRMR file upload mail cannot be sent because signal.evdas.file.upload.ermr.toAddresses properties is missing...")
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                log.info("eRMR file process is releasing lock.")
                applicationSettingsService.releaseEvdasErmrUploadLock()
            }
        }
    }

    def startProcessing(List<EvdasFileProcessLog> filesNameToProcess) {
        EvdasFileProcessState fileProcessState
        File fileToProcess
        Map parsedDataFromFile
        filesNameToProcess.each { EvdasFileProcessLog fileProcessLog ->
            fileToProcess = new File("${fileProcessLog.isManual ? uploadFolder.absolutePath : readFolder.absolutePath}/${fileProcessLog.savedName ? fileProcessLog.savedName : fileProcessLog.fileName}")
            log.info "Processing $fileToProcess.name ${fileProcessLog.isManual ? "uploaded manually" : ""}"
            parsedDataFromFile = processFile(fileToProcess)
            Map persistDataStatus = persistDataInDatabase(parsedDataFromFile['processable'] as List, fileToProcess, fileProcessLog.duplicateDataHandling , fileProcessLog.fileName)
            fileProcessState = persistDataStatus.status ? EvdasFileProcessState.SUCCESS : EvdasFileProcessState.FAILED
            moveFile([[name: fileProcessLog.savedName?:fileProcessLog.fileName]], fileProcessState, fileProcessLog.isManual)
            updateLog(fileProcessLog.fileName, fileProcessState, parsedDataFromFile, persistDataStatus.message)
        }
    }

    void filterSourceFolder(User user, Map<String, List> filteredFileList, Map fileAndDescriptionMap, Boolean isManual, String dataType, Integer optDuplicate , String savedName=null) {
        saveLogs(filteredFileList['fail'], EvdasFileProcessState.FAILED, user, fileAndDescriptionMap, isManual, dataType, optDuplicate , savedName)
        moveFile(filteredFileList['fail'], EvdasFileProcessState.FAILED, isManual)
        saveLogs(filteredFileList['pass'], EvdasFileProcessState.IN_PROCESS, user, fileAndDescriptionMap, isManual, dataType, optDuplicate , savedName)
    }

    List readAllFilesFromSourceFolder() {
        List fileListToProcess = []
        if (readFolder?.exists() && readFolder?.isDirectory()) {
            readFolder?.eachFile { File file ->
                if (!file.isDirectory() && (EvdasFileProcessLog.countByFileNameAndStatusAndDataType(file.name, EvdasFileProcessState.IN_PROCESS, "eRMR") == 0)) {
                    fileListToProcess.add(file)
                }
            }
        }
        fileListToProcess
    }

    String convertXLStoXLSX(String fileName, File sourceFolder) {
        FileConversionXLSToXLSX fileConversionXLSToXLXS = FileConversionXLSToXLSX.getInstance()
        String xlsxFilePath = fileConversionXLSToXLXS.convertXLS2XLSX("${sourceFolder.absolutePath}/${fileName}")
        new File("${sourceFolder.absolutePath}/${fileName}").delete()
        fetchFileNameFromFilePath(xlsxFilePath)
    }

    String convertXLStoXLSXExt(File fileToUpload, File sourceFolder) {
        FileConversionXLSToXLSX fileConversionXLSToXLXS = FileConversionXLSToXLSX.getInstance()
        String xlsxFilePath = fileConversionXLSToXLXS.convertXLS2XLSX("${sourceFolder.absolutePath}/${fileToUpload.name}")
        new File("${sourceFolder.absolutePath}/${fileToUpload.name}").delete()
        fetchFileNameFromFilePath(xlsxFilePath)
    }

    String fetchFileNameFromFilePath(String xlsxFilePath){
        Integer lastIndex = xlsxFilePath.replaceAll("\\\\", "/").lastIndexOf("/")
        lastIndex >= 0 ? xlsxFilePath.substring(lastIndex + 1) : xlsxFilePath
    }

    Map filterFilesForDataImport(List fileListToProcess, Boolean isManual , String savedFileName = null) {
        Map<String, List> fileToProcess = [pass: [], fail: []]
        Integer sheetPosition = config.signal.evdas.ermr.data.import.sheet.number
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
        Boolean isConfigAndActualColumnsMapInFile
        String substanceName
        Map baseColumnAndTypeMapping = [:]
        String dataSourceValue = "pva"
        if (Holders.config.signal.evdas.enabled) {
            dataSourceValue = "eudra"
        }
        baseColumnAndTypeMapping = getColumnTypeMapping(dataSourceValue)
        fileListToProcess.each { String fileName ->
            boolean isValidRecord = false
            Boolean isStartEndDatePresent = false
            def file = new File("${isManual ? uploadFolder.absolutePath : readFolder.absolutePath}/${isManual ? savedFileName : fileName}")
            Boolean isValidFileName = validateFileName(file)
            if (FilenameUtils.getExtension(fileName) == 'csv') {
                if (isValidFileName) {
                    isStartEndDatePresent = CsvDataImporter.getStartAndEndDateRange(fileName) as Boolean
                    isConfigAndActualColumnsMapInFile = CsvDataImporter.checkIfMappingIsValid(file, baseColumnAndTypeMapping)
                }
                if (isStartEndDatePresent) {
                    List<Date> startEndDateRange = CsvDataImporter.getStartAndEndDateRange(fileName)
                    substanceName = CsvDataImporter.getSubstanceName(file)
                    isValidRecord = isValidSubstanceAndDateRange(substanceName, startEndDateRange)
                }
            } else {
                if (isValidFileName) {
                    isStartEndDatePresent = ExcelDataImporter.getStartAndEndDateRange(file) as Boolean
                    isConfigAndActualColumnsMapInFile = ExcelDataImporter.checkIfMappingIsValid(file, baseColumnAndTypeMapping, sheetPosition)
                }

                if (isStartEndDatePresent) {
                    List<Date> startEndDateRange = ExcelDataImporter.getStartAndEndDateRange(file)
                    substanceName = ExcelDataImporter.getSubstanceName(file, sheetPosition)

                    isValidRecord = isValidSubstanceAndDateRange(substanceName, startEndDateRange)
                }
            }
            if (isValidFileName && isStartEndDatePresent && isValidRecord && isConfigAndActualColumnsMapInFile) {
                fileToProcess['pass'] << ['name': fileName, 'reason': null, 'substanceName': substanceName]
            } else if (!isValidFileName) {
                String reason = messageSource.getMessage('app.label.evdas.data.upload.file.format.not.supported', null, locale)
                fileToProcess['fail'] << ['name': fileName, 'reason': reason, 'substanceName': substanceName]
            } else if (!isStartEndDatePresent) {
                String reason = messageSource.getMessage('app.label.evdas.data.upload.startandEndDate.not.present', null, locale)
                fileToProcess['fail'] << ['name': fileName, 'reason': reason, 'substanceName': substanceName]
            } else if (!isValidRecord) {
                String reason = messageSource.getMessage('app.label.evdas.data.upload.load.frequency.does.not.match', null, locale)
                fileToProcess['fail'] << ['name': fileName, 'reason': reason, 'substanceName': substanceName]
            } else if (!isConfigAndActualColumnsMapInFile) {
                String reason = messageSource.getMessage('app.label.evdas.data.upload.eRMR.does.not.match', null, locale)
                fileToProcess['fail'] << ['name': fileName, 'reason': reason, 'substanceName': substanceName]
            }
        }
        fileToProcess
    }

    Boolean validateFileName(File fileToProcess) {
        String fileExtension = FilenameUtils.getExtension(fileToProcess.name)
        List supportedFormats = ['xls', 'xlsx' , 'csv']
        supportedFormats.contains(fileExtension)
    }

    Boolean isValidSubstanceAndDateRange(String substanceName, List<Date> dateRange) {
        SubstanceFrequency substanceFrequency = SubstanceFrequency.findByName(substanceName)
        if (substanceFrequency) {
            log.info("Substance ${substanceName} has frequency configured for ${substanceFrequency.uploadFrequency} upload starting from ${DateUtil.stringFromDate(substanceFrequency.startDate, DateUtil.DEFAULT_DATE_FORMAT, DateTimeZone.UTC.ID)} to ${DateUtil.stringFromDate(substanceFrequency.endDate, DateUtil.DEFAULT_DATE_FORMAT, DateTimeZone.UTC.ID)}")
        } else {
            log.info("Substance ${substanceName} is not configured.")
        }
        substanceFrequency && isValidDateRange(substanceFrequency.startDate, substanceFrequency.uploadFrequency, dateRange)
    }

    Boolean isValidDateRange(Date startDate, String uploadFrequency, List<Date> inputDateRange) {
        def inputDates = inputDateRange.collect {
            DateUtil.stringFromDate(it, 'ddMMMyyyy', DateTimeZone.UTC.ID).substring(0, 5)
        }

        populatePossibleDateRanges(startDate, uploadFrequency, DateUtil.stringFromDate(inputDateRange.first(), 'ddMMMyyyy', DateTimeZone.UTC.ID)).findAll {
            it[0] == inputDates[0] && it[1] == inputDates[1]
        }
    }

    List<String> populatePossibleDateRanges(Date startDate, String uploadFrequency, String inputDateRange) {
        def possibleDateRanges = []
        String dateInput
        switch (uploadFrequency) {
            case '15 Days':
                dateInput = sdf.format(startDate).substring(0, 2) + "JAN" + inputDateRange.substring(5, 9)
                startDate = DateUtil.stringToDate(dateInput, DateUtil.DEFAULT_DATE_FORMAT_WITHOUT_SEPARATOR, DateTimeZone.UTC.ID)
                possibleDateRanges = populateAllFifteenDaysCombinationsForDate(startDate)
                break
            case '1 Month':
                dateInput = sdf.format(startDate).substring(0, 2) + "JAN" + inputDateRange.substring(5, 9)
                startDate = DateUtil.stringToDate(dateInput, "ddMMMyyyy", DateTimeZone.UTC.ID)
                possibleDateRanges = populateAllMonthlyCombinationsForDate(startDate)
                break
            case '3 Months':
                dateInput = sdf.format(startDate).substring(0, 5) + inputDateRange.substring(5, 9)
                startDate = DateUtil.stringToDate(dateInput, DateUtil.DEFAULT_DATE_FORMAT_WITHOUT_SEPARATOR, DateTimeZone.UTC.ID)
                possibleDateRanges = populateAllQuarterlyCombinationsForDate(startDate)
                break
            case '6 Months':
                dateInput = sdf.format(startDate).substring(0, 5) + inputDateRange.substring(5, 9)
                startDate = DateUtil.stringToDate(dateInput, DateUtil.DEFAULT_DATE_FORMAT_WITHOUT_SEPARATOR, DateTimeZone.UTC.ID)
                possibleDateRanges = populateAllHalfYearlyCombinationsForDate(startDate)
                break
            case '12 Months':
                dateInput = sdf.format(startDate).substring(0, 5) + inputDateRange.substring(5, 9)
                startDate = DateUtil.stringToDate(dateInput, DateUtil.DEFAULT_DATE_FORMAT_WITHOUT_SEPARATOR, DateTimeZone.UTC.ID)
                possibleDateRanges = populateAllYearlyCombinationsForDate(startDate)
        }
        possibleDateRanges
    }

    List populateAllFifteenDaysCombinationsForDate(Date startDate) {
        List combinations = []
        if (sdf.format(startDate).substring(0, 2) == '01') {
            12.times { time ->
                combinations << use(TimeCategory) {
                    [sdf.format(startDate + time.month).substring(0, 5), sdf.format(startDate + (time).month + 14.day).substring(0, 5)]
                }
            }
            12.times { time ->
                combinations << use(TimeCategory) {
                    [sdf.format(startDate + time.month + 15.day).substring(0, 5), sdf.format(startDate + (time + 1).month - 1.day).substring(0, 5)]
                }
            }
        } else {
            12.times { time ->
                combinations << use(TimeCategory) {
                    [sdf.format(startDate + time.month - 15.day).substring(0, 5), sdf.format(startDate + (time).month - 1.day).substring(0, 5)]
                }
            }
            12.times { time ->
                combinations << use(TimeCategory) {
                    [sdf.format(startDate + time.month).substring(0, 5), sdf.format(startDate + (time + 1).month - 16.day).substring(0, 5)]
                }
            }
        }

        combinations
    }

    List populateAllMonthlyCombinationsForDate(Date startDate) {
        List combinations = []
        12.times { time ->
            combinations << use(TimeCategory) {
                [sdf.format(startDate + time.month).substring(0, 5), sdf.format(startDate + (time + 1).month - 1.day).substring(0, 5)]
            }
        }
        combinations
    }

    List populateAllQuarterlyCombinationsForDate(Date startDate) {
        List combinations = []
        4.times { time ->
            combinations << use(TimeCategory) {
                [sdf.format(startDate + (time * 3).month).substring(0, 5), sdf.format(startDate + ((time * 3) + 3).month - 1.day).substring(0, 5)]
            }
        }
        combinations
    }

    List populateAllHalfYearlyCombinationsForDate(Date startDate) {
        List combinations = []
        2.times { time ->
            combinations << use(TimeCategory) {
                [sdf.format(startDate + (time * 6).month).substring(0, 5), sdf.format(startDate + ((time * 6) + 6).month - 1.day).substring(0, 5)]
            }
        }
        combinations
    }

    List populateAllYearlyCombinationsForDate(Date startDate) {
        List combinations = []
        combinations << use(TimeCategory) {
            [sdf.format(startDate).substring(0, 5), sdf.format(startDate + 1.year - 1.day).substring(0, 5)]
        }
        combinations
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
            case 'csv':
                processCsvFile(fileToBeProcessed)
                break
            default:
                log.error("Not supported")
        }
    }

    Map processExcelFile(File fileToBeProcessed) {
        Map baseColumnAndTypeMapping = [:]
        ERMRConfig.withTransaction {
            ERMRConfig.findAllByExcludedIlike('n', [sort: "id"]).each {
                baseColumnAndTypeMapping.put(it.sourceColumnName.toUpperCase(), it.sourceDataType)
            }
        }
        ExcelDataImporter.getData(fileToBeProcessed, baseColumnAndTypeMapping, config.signal.evdas.ermr.data.import.sheet.number)
    }

    Map processCsvFile(File fileToBeProcessed) {
        Map baseColumnAndTypeMapping = [:]
        baseColumnAndTypeMapping = getColumnTypeMapping("eudra")
        getCsvData(fileToBeProcessed, baseColumnAndTypeMapping)
    }

    Map persistDataInDatabase(List data, File fileToProcess, Integer optDuplicate , String originalFileName) {
        String extension = FilenameUtils.getExtension(fileToProcess.name)
        List<Date> startEndDateList = extension == 'csv' ? CsvDataImporter.getStartAndEndDateRange(originalFileName) : ExcelDataImporter.getStartAndEndDateRange(fileToProcess)
        Date recordStartDate = startEndDateList[0]
        Date recordEndDate = startEndDateList[1]

        String recordStartDateString = DateUtil.stringFromDate(recordStartDate, DateUtil.DEFAULT_DATE_TIME_FORMAT, DateTimeZone.UTC.ID)
        String recordEndDateString = DateUtil.stringFromDate(recordEndDate, DateUtil.DEFAULT_DATE_TIME_FORMAT, DateTimeZone.UTC.ID)
        Boolean status
        String message

        final Sql sqlObj
        try {
            sqlObj = new Sql(signalDataSourceService.getDataSource('eudra'))
            String sqlQuery
            sqlObj.call("{? = call f_get_insert_ermr()}", [Sql.VARCHAR]) { result ->
                sqlQuery = result
            }

            sqlObj.withBatch(10000, sqlQuery) { preparedStatement ->
                data.each { List record ->
                    record.add(recordStartDateString)
                    record.add(recordEndDateString)
                    preparedStatement.addBatch(record)
                }
            }
            log.info("Done persisting in db")
            log.info("Calling stored procedure {call PKG_EVDAS_UPLOAD.p_evdas_main(?,?,?)}")
            sqlObj.call("{call PKG_EVDAS_UPLOAD.p_evdas_main(?,?,?)}",
                    [fileToProcess.name, optDuplicate, Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]
            ) { new_gen_info ->
                if (new_gen_info != null) {
                    while (new_gen_info.next()) {
                        //Data base returns the value as 10 meaning that processing was successfull.
                        if (new_gen_info.getAt(0) == 10) {
                            status = true
                        }
                    }
                }
            }
            log.info(status as String)
        } catch (Exception e) {
            e.printStackTrace()
            status = false
            message = "Some error has occurred during data load process. Please contact your database administrator."
        } finally {
            sqlObj?.close()
            return [status: status, message: message]
        }
    }

    void moveFile(List fileNames, EvdasFileProcessState fileProcessState, Boolean isManual) {
        File fileToMove
        fileNames.each { Map fileNameAndReasonMap ->
            fileToMove = new File("${isManual ? uploadFolder.absolutePath : readFolder.absolutePath}/${fileNameAndReasonMap.name}")
            fileToMove.renameTo(new File((fileProcessState == EvdasFileProcessState.SUCCESS) ? processSuccessDir : processFailDir, fileToMove.name))
        }
    }

    void saveLogs(List fileNames, EvdasFileProcessState fileProcessState, User user, Map<String, String> fileAndDescriptionMap = [:], Boolean isManual, String dataType, Integer optDuplicate , String savedName) {
        File fileToProcess
        fileNames.each { Map fileNameAndReasonMap ->
            Date recordStartDate = null
            Date recordEndDate = null
            String reason = fileNameAndReasonMap.reason
            String extension = FilenameUtils.getExtension(fileNameAndReasonMap.name)
            String substanceName = fileNameAndReasonMap.substanceName
            fileToProcess = new File("${isManual ? uploadFolder.absolutePath : readFolder.absolutePath}/${isManual ? savedName : fileNameAndReasonMap.name}")
            List<Date> startEndDateList = extension == 'csv' ? CsvDataImporter.getStartAndEndDateRange(fileNameAndReasonMap.name) : ExcelDataImporter.getStartAndEndDateRange(fileToProcess)
            if (startEndDateList.size() == 2) {
                recordStartDate = startEndDateList[0]
                recordEndDate = startEndDateList[1]
            }
            String description = fileAndDescriptionMap[fileNameAndReasonMap.name] ?: null
            new EvdasFileProcessLog(fileName: fileNameAndReasonMap.name, status: fileProcessState, description: description, updatedBy: user, reason: reason, substances: substanceName,
                    recordStartDate: recordStartDate, recordEndDate: recordEndDate, dataType: dataType, duplicateDataHandling: optDuplicate, isManual: isManual , savedName: savedName).save()
        }
    }

    void updateLog(String fullFileName, EvdasFileProcessState fileProcessState, Map parsedDataFromFile, String message) {
        EvdasFileProcessLog fileProcessLog = EvdasFileProcessLog.findByFileNameAndStatus(fullFileName, EvdasFileProcessState.IN_PROCESS)
        fileProcessLog?.status = fileProcessState
        fileProcessLog?.totalRecords = parsedDataFromFile['totalRecords'] as Integer
        fileProcessLog?.processedRecords = parsedDataFromFile['processable'].size()
        fileProcessLog?.reason = message ?: parsedDataFromFile['discarded'] ? parsedDataFromFile['discarded']?.join("\n") : null
        fileProcessLog.save(flush: true, failOnError: true)
        log.info("Total records -> $fileProcessLog.totalRecords")
        log.info("Processed records -> $fileProcessLog.processedRecords")
        log.info("Discarded records -> ${parsedDataFromFile['discarded']?.size()}")
        log.info("Log updated successfully.")
        if(fileProcessState in [EvdasFileProcessState.SUCCESS,EvdasFileProcessState.FAILED]){
            log.info("Now creating audit entry for success or error")
            createAuditLogForEvdasUpload(fileProcessLog)
        }
    }

    def createAuditLogForEvdasUpload(EvdasFileProcessLog fileProcessLog){
        Map auditTrail = [:]
        auditTrail.category = AuditTrail.Category.INSERT.toString()
        auditTrail.entityValue = fileProcessLog.dataType + ": ${fileProcessLog.fileName.substring(fileProcessLog.fileName.lastIndexOf("/")+1)}"
        auditTrail.entityName = "evdasDataUpload"
        auditTrail.moduleName = "EVDAS Data Upload"
        auditTrail.username = fileProcessLog.updatedBy.username
        auditTrail.fullname = fileProcessLog.updatedBy.fullName
        auditTrail.description = "Uploaded ${fileProcessLog.dataType} : ${fileProcessLog.fileName}"
        List<Map> auditChildMap = []
        def childEntry=[:]
        childEntry = [
                 propertyName    : "Status",
                 newValue     : fileProcessLog.status]
        auditChildMap << childEntry
        childEntry = [
                 propertyName    : "Substance",
                 newValue     : fileProcessLog.substances]
        auditChildMap << childEntry
        childEntry = [
                propertyName    : "Description",
                newValue     : fileProcessLog.description]
        auditChildMap << childEntry
        signalAuditLogService.createAuditLog(auditTrail, auditChildMap)
    }

    Map getColumnTypeMapping(String dataSourceValue) {
        Map baseColumnAndTypeMapping = [:]
        ERMRConfig."$dataSourceValue".withTransaction {
            ERMRConfig.findAllByExcludedIlike('n', [sort: "id"]).each {
                baseColumnAndTypeMapping.put(it.sourceColumnName.toUpperCase(), it.sourceDataType)
            }
        }
        return baseColumnAndTypeMapping
    }

    Map getCsvData(File file, Map baseColumnTypeMap) {
        def valuesToProcess = []
        def valuesToDiscard = []
        Map<String, Integer> actualColumnMapping = CsvDataImporter.fetchActualColumnMapping(file, baseColumnTypeMap, Constants.ExcelDataUpload.ERMR_DATA_CAPTURE_POINTER)

        Map evpmColumnMapping = config.signal.evdas.ermr.data.upload.evpm.hyperlink.column.name

        def totalRowCount = 0
        def isProcessable = true
        Boolean startProcessing = false
        Reader inputFile = new FileReader(file)
        CSVParser parser = CSVParser.parse(inputFile, CSVFormat.EXCEL)
        for (CSVRecord csvRecord : parser) {
            if (!startProcessing) {
                if (CsvDataImporter.removeBom(csvRecord.get(0)) == Constants.ExcelDataUpload.ERMR_DATA_CAPTURE_POINTER) {
                    startProcessing = true
                }
            } else {
                def list = []
                def cell
                if (csvRecord.get(0) && !csvRecord.get(0).trim().isEmpty()) {

                    totalRowCount++

                    baseColumnTypeMap.each { String name, String type ->
                        cell = csvRecord.get(actualColumnMapping[name.trim()])
                        if (cell) {
                            if (type == Constants.ExcelDataUpload.CELL_DATA_TYPE_NUMBER && isProcessable && cell) {
                                try {
                                    list.add(cell as Double)
                                } catch (NumberFormatException e) {
                                    list.add(0, "Row #$totalRowCount Cell #${actualColumnMapping[name]} => Actual $cell : Expected ${type}")
                                    list.add(cell)
                                    isProcessable = false
                                }
                            } else if (name == evpmColumnMapping['NEW EVPM LINK'] || name == evpmColumnMapping['TOT EVPM LINK']) {
                                list.add(CsvDataImporter.trimHyperLink(cell))
                            } else {
                                list.add(cell)
                            }


                        } else {
                            list.add(null)
                        }
                    }
                }
                if (list) {
                    isProcessable ? valuesToProcess.add(list) : valuesToDiscard.add(list.take(5).join(" | "))
                }
                isProcessable = true
            }
        }
        [processable: valuesToProcess, discarded: valuesToDiscard, totalRecords: totalRowCount]

    }
}