package com.rxlogix.util

import com.monitorjbl.xlsx.StreamingReader
import com.rxlogix.Constants
import com.rxlogix.util.DateUtil
import grails.util.Holders
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTimeZone
import org.springframework.web.multipart.MultipartFile

class ExcelDataImporter {
    static def config = Holders.config

    static List<Date> getStartAndEndDate(File file, Integer sheetPosition) {
        List<Date> startEndDates = []
        InputStream is = new FileInputStream(file)
        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(40000)     // number of rows to keep in memory (defaults to 10)
                .bufferSize(4096)           // buffer size to use when reading InputStream to file (defaults to 1024)
                .open(is)
        try {
            def sheet = workbook.getSheetAt(sheetPosition)
            List<String> dateRanges = sheet.first().first().stringCellValue.split("_")

            if (dateRanges.size() == 2) {
                startEndDates << Date.parse("ddMMMyyyy", dateRanges[0])
                startEndDates << Date.parse("ddMMMyyyy", dateRanges[1])
            }
        } catch (Exception e) {
            e.printStackTrace()
            startEndDates = []
        }
        workbook.close()
        startEndDates
    }

    static List<Date> getStartAndEndDateRange(File file) {
        List<Date> dateArr = []
        InputStream is = new FileInputStream(file)
        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(40000)     // number of rows to keep in memory (defaults to 10)
                .bufferSize(4096)           // buffer size to use when reading InputStream to file (defaults to 1024)
                .open(is)

        def sheetCount = workbook.numberOfSheets

        def totalRowCount = 0
        Boolean found = false
        sheetCount.times {
            if (!found) {
                for (row in workbook.getSheetAt(it).rowIterator()) {
                    totalRowCount++
                    for (cell in row.cellIterator()) {
                        if (cell?.stringCellValue.toLowerCase().contains('The selected reference period for defining "new" cases is'.toLowerCase())) {
                            dateArr << DateUtil.stringToDate(cell?.stringCellValue.split(" ")[-3], 'dd/MM/yyyy', DateTimeZone.UTC.ID)
                            dateArr << DateUtil.stringToDate(cell?.stringCellValue.split(" ")[-1], 'dd/MM/yyyy', DateTimeZone.UTC.ID)
                            found = true
                            break
                        }
                    }
                    if (found) {
                        break
                    }
                }
            }
        }
        workbook.close()
        dateArr
    }

    static Boolean checkIfMappingIsValid(File file, Map baseColumnTypeMap, Integer sheetPosition) {
        InputStream is = new FileInputStream(file)
        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(40000)     // number of rows to keep in memory (defaults to 10)
                .bufferSize(4096)           // buffer size to use when reading InputStream to file (defaults to 1024)
                .open(is)
        try {
            def sheet = workbook.getSheetAt(sheetPosition)
            Map actualColumnMapping = fetchActualColumnMapping(sheet, baseColumnTypeMap)
            return actualColumnMapping.keySet().size() == baseColumnTypeMap.keySet().size()
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace()
            return false
        } finally {
            workbook.close()
        }
    }

    static Map fetchActualColumnMapping(Sheet sheet, Map baseColumnTypeMap) {
        Map actualColumnMapping = [:]
        Map evpmColumnMapping = config.signal.evdas.ermr.data.upload.evpm.hyperlink.column.name
        Integer position = 0
        //This is our assumption that the IDs will always exists in the first column
        for (row in sheet.rowIterator()) {
            if (row.getCell(0)?.stringCellValue == "Active Substance") {
                for (cell in row.cellIterator()) {
                    if (baseColumnTypeMap.containsKey(cell?.stringCellValue.toUpperCase()) && !actualColumnMapping.containsKey(cell?.stringCellValue.toUpperCase())) {
                        actualColumnMapping.put(cell?.stringCellValue.toUpperCase(), position)
                    }
                    position++
                }
            }
        }

        if (actualColumnMapping.containsKey(evpmColumnMapping['NEW EVPM'])) {
            actualColumnMapping.put(evpmColumnMapping['NEW EVPM LINK'], actualColumnMapping[evpmColumnMapping['NEW EVPM']] + 1)
        }

        if (actualColumnMapping.containsKey(evpmColumnMapping['TOT EVPM'])) {
            actualColumnMapping.put(evpmColumnMapping['TOT EVPM LINK'], actualColumnMapping[evpmColumnMapping['TOT EVPM']] + 1)
        }
        actualColumnMapping
    }

    static String getSubstanceName(File file, Integer sheetPosition) {
        InputStream is = new FileInputStream(file)
        Workbook workbook = new XSSFWorkbook(is)
        def sheet = workbook.getSheetAt(sheetPosition)
        def rowCount = 0
        for (row in sheet.rowIterator()) {
            rowCount++
            if (row.getCell(0)?.stringCellValue == "Active Substance") {
                break
            }
        }
        while(sheet.getRow(rowCount).getCell(0)?.stringCellValue == "Active Substance") {
            rowCount++
        }
        String substanceName = sheet.getRow(rowCount).getCell(0)?.stringCellValue
        workbook.close()
        substanceName
    }

    static Map getData(File file, Map baseColumnTypeMap, Integer sheetPosition) {
        def valuesToProcess = []
        def valuesToDiscard = []
        InputStream is = new FileInputStream(file)
        Workbook workbook1 = StreamingReader.builder()
                .rowCacheSize(40000)     // number of rows to keep in memory (defaults to 10)
                .bufferSize(4096)           // buffer size to use when reading InputStream to file (defaults to 1024)
                .open(is)

        def sheet1 = workbook1.getSheetAt(sheetPosition)
        Map<String, Integer> actualColumnMapping = fetchActualColumnMapping(sheet1, baseColumnTypeMap)
        workbook1.close()


        Map evpmColumnMapping = config.signal.evdas.ermr.data.upload.evpm.hyperlink.column.name

        Workbook workbook = new XSSFWorkbook(file)

        def sheet = workbook.getSheetAt(sheetPosition)
        def totalRowCount = 0
        def isProcessable = true
        Boolean startProcessing = false
        for (row in sheet.rowIterator()) {
            if (!startProcessing) {
                if (row.getCell(0)?.stringCellValue == Constants.ExcelDataUpload.ERMR_DATA_CAPTURE_POINTER) {
                    startProcessing = true
                }
            } else {
                def list = []
                def cell
                if (row.getCell(0) && !row.getCell(0)?.stringCellValue.trim().isEmpty()) {
                    totalRowCount++
                    baseColumnTypeMap.each { String name, String type ->
                        cell = row.getCell(actualColumnMapping[name.trim()])
                        if (cell) {
                            switch (cell.cellType) {
                                case CellType.NUMERIC:
                                    list.add(cell.numericCellValue)
                                    break
                                case CellType.STRING:
                                    if (type == Constants.ExcelDataUpload.CELL_DATA_TYPE_NUMBER && isProcessable && cell?.stringCellValue) {
                                        try {
                                            list.add(cell?.stringCellValue as Long)
                                        } catch (NumberFormatException e) {
                                            list.add(0, "Row #$totalRowCount Cell #${actualColumnMapping[name]} => Actual $cell.stringCellValue : Expected ${type}")
                                            list.add(cell?.stringCellValue)
                                            isProcessable = false
                                        }
                                    } else if (name == evpmColumnMapping['NEW EVPM LINK'] || name == evpmColumnMapping['TOT EVPM LINK']) {
                                        list.add(cell.hyperlink?.address)
                                    } else {
                                        list.add(cell?.stringCellValue)
                                    }
                                    break
                                case CellType.FORMULA:
                                    if (type == Constants.ExcelDataUpload.CELL_DATA_TYPE_NUMBER && isProcessable && cell?.stringCellValue) {
                                        try {
                                            list.add(cell.numericCellValue)
                                        } catch (NumberFormatException e) {
                                            list.add(0, "Row #$totalRowCount Cell #${actualColumnMapping[name]} => Actual $cell.stringCellValue : Expected ${type}")
                                            list.add(cell?.stringCellValue)
                                            isProcessable = false
                                        }
                                    } else {
                                        list.add(cell?.stringCellValue)
                                    }
                                    break
                                default:
                                    list.add(null)
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
        workbook.close()
        [processable: valuesToProcess, discarded: valuesToDiscard, totalRecords: totalRowCount]
    }

    static Map getCaseListingData(File file, Map COLUMN_TYPE_MAP, Integer sheetPosition) {
        Map resultMap = [processable: null, discarded: 0, totalRecords: 0, status: false]
        List valuesToProcess = []
        List valuesToDiscard = []
        Boolean startCapturingData = false
        Workbook workbook = null
        try {
            workbook = new XSSFWorkbook(file)
            if (workbook) {
                String substanceName = file.parentFile.name

                Sheet sheet = workbook.getSheetAt(sheetPosition)
                List columnList = fetchCaseListingColumnMap(sheet)
                Integer totalColumns = COLUMN_TYPE_MAP.keySet().size()
                Integer totalRowCount = 0
                Boolean isProcessable = true

                for (row in sheet.rowIterator()) {
                    if (!startCapturingData) {
                        if (row.getCell(0)?.stringCellValue == "EU Local Number") {
                            startCapturingData = true
                        }
                    } else {
                        Map map = [:]
                        Cell cell
                        map.put("substance_name" , substanceName)
                        if (row.getCell(0)?.stringCellValue) {
                            totalRowCount += 1
                            for (int columnNumber = 0; columnNumber < totalColumns; columnNumber++) {
                                cell = row.getCell(columnNumber)
                                if (cell) {
                                    switch (COLUMN_TYPE_MAP[columnNumber]) {
                                        case 'HYPERLINK':
                                            map.put(columnList.get(columnNumber) ,cell.hyperlink.address)
                                            break
                                        case 'STRING':
                                        default:
                                            if (cell.cellType == CellType.NUMERIC) {
                                                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator()
                                                DataFormatter formatter = new DataFormatter()
                                                formatter.addFormat("m/d/yy", new java.text.SimpleDateFormat("dd/MM/yyyy"))
                                                map.put(columnList.get(columnNumber) , formatter.formatCellValue(cell, evaluator))
                                            } else {
                                                map.put(columnList.get(columnNumber) , cell.stringCellValue)
                                            }
                                    }

                                } else {
                                    map.put(columnList.get(columnNumber) , null)
                                }
                            }
                            isProcessable ? valuesToProcess.add(map) : valuesToDiscard.add(map.take(5).join(" | "))
                        }
                        isProcessable = true
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

    static List<String> readFromExcelForAddCases(MultipartFile file) {
        List<String> set = []
        Workbook workbook = null

        if (file.originalFilename.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream)
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream)
        }

        Sheet sheet = workbook.getSheetAt(0)  //get the first worksheet from excel
        Row row
        Cell cell

        // starts reading the values from 2nd row
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            if ((row = sheet.getRow(i)) != null) {
                cell = (Cell) row?.getCell(0)  //get the first column from excel
                cell?.setCellType(CellType.STRING)
                if (cell?.getStringCellValue()?.trim()?.length()) {
                    set << cell?.getStringCellValue()?.trim()
                }
            }
        }
        return set.sort() as List
    }

    static List fetchCaseListingColumnMap(Sheet sheet) {
        List columnList = []
        for (row in sheet.rowIterator()) {
            if (row.getCell(0).stringCellValue == Constants.ExcelDataUpload.CASE_LISTING_CAPTURE_POINTER) {
                for (cell in row.cellIterator()) {
                    columnList.add(cell.stringCellValue.toUpperCase())
                }
            }
        }
        return columnList
    }

    static Map fetchDataFromFile(MultipartFile file) {
        def sheetheader = []
        def values = []
        Map resultMap = [values: [], status: false]
        def workbook = new XSSFWorkbook(file.inputStream)
        def sheet = workbook.getSheetAt(0)
        if (sheet.rowIterator().hasNext()) {
            for (cell in sheet.getRow(0).cellIterator()) {
                sheetheader << cell.stringCellValue
            }
            def headerFlag = true
            for (row in sheet.rowIterator()) {
                if (headerFlag) {
                    headerFlag = false
                    continue
                }
                def value = ""
                def map = [:]
                for (cell in row.cellIterator()) {
                    switch (cell.cellType) {
                        case CellType.STRING:
                            value = cell.stringCellValue
                            if (value == 'English') {
                                map["${sheetheader[cell.columnIndex]}"] = 'en'
                            } else if (value == "Japanese") {
                                map["${sheetheader[cell.columnIndex]}"] = 'ja'
                            } else if (value == "YES") {
                                map["${sheetheader[cell.columnIndex]}"] = true
                            } else if (value == "NO") {
                                map["${sheetheader[cell.columnIndex]}"] = false
                            } else {
                                map["${sheetheader[cell.columnIndex]}"] = value
                            }
                            break
                        case CellType.NUMERIC:
                            value = cell.numericCellValue
                            map["${sheetheader[cell.columnIndex]}"] = value
                            break
                        case CellType.BOOLEAN:
                            value = cell.booleanCellValue
                            map["${sheetheader[cell.columnIndex]}"] = value
                            break
                        default:
                            value = ''
                    }
                }
                values.add(map)
            }
            resultMap.values = values
            resultMap.status = true
        }
        return resultMap
    }
}
