package com.rxlogix.util

import com.rxlogix.Constants
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import grails.util.Holders
import org.joda.time.DateTimeZone


class CsvDataImporter {
    static def config = Holders.config

    static List<Date> getStartAndEndDateRange(String fileName) {
        List<Date> dateArr = []
        try {
            String[] splitArray = fileName.split(Holders.config.signal.evdas.ermr.csv.filename.date.seperator)
            dateArr << DateUtil.stringToDate(splitArray[splitArray.size()-2][-11..-1], DateUtil.DEFAULT_DATE_FORMAT, DateTimeZone.UTC.ID)
            dateArr << DateUtil.stringToDate(splitArray[splitArray.size()-1][0..10], DateUtil.DEFAULT_DATE_FORMAT, DateTimeZone.UTC.ID)
            return dateArr
        }
        catch (Exception exception) {
            exception.printStackTrace()
            return null
        }

    }

    static Boolean checkIfMappingIsValid(File file, Map baseColumnTypeMap) {
        try {
            Map actualColumnMapping = fetchActualColumnMapping(file, baseColumnTypeMap, Constants.ExcelDataUpload .ERMR_DATA_CAPTURE_POINTER)
            return actualColumnMapping.keySet().size() == baseColumnTypeMap.keySet().size()
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace()
            return false
        }
    }

    static Map fetchActualColumnMapping(File file, Map baseColumnTypeMap, String initializer) {
        Map evpmColumnMapping = config.signal.evdas.ermr.data.upload.evpm.hyperlink.column.name
        Map actualColumnMapping = [:]
        Integer position = 0
        Reader inputFile = new FileReader(file)
        CSVParser parser = CSVParser.parse(inputFile, CSVFormat.EXCEL)
        for (CSVRecord csvRecord : parser) {
            if (removeBom(csvRecord.get(0)) == initializer) {
                for (column in csvRecord) {
                    if (baseColumnTypeMap.containsKey(removeBom(column.toString().toUpperCase())) && !actualColumnMapping.containsKey(removeBom(column.toString().toUpperCase()))) {
                        actualColumnMapping.put(removeBom(column.toString().toUpperCase()), position)
                    }
                    position++
                }

            }

        }
        if (actualColumnMapping.containsKey(evpmColumnMapping[Constants.ExcelDataUpload.ERMR_LINK_NEW_EVPM])) {
            actualColumnMapping.put(evpmColumnMapping['NEW EVPM LINK'], actualColumnMapping[evpmColumnMapping[Constants.ExcelDataUpload.ERMR_LINK_NEW_EVPM]] + 1)
        }

        if (actualColumnMapping.containsKey(evpmColumnMapping[Constants.ExcelDataUpload.ERMR_LINK_TOTAL_EVPM])) {
            actualColumnMapping.put(evpmColumnMapping['TOT EVPM LINK'], actualColumnMapping[evpmColumnMapping[Constants.ExcelDataUpload.ERMR_LINK_TOTAL_EVPM]] + 1)
        }
        parser.close()
        actualColumnMapping
    }

    static String getSubstanceName(File file) {
        String substanceName
        Reader inputFile = new FileReader(file)
        CSVParser parser = CSVParser.parse(inputFile, CSVFormat.EXCEL)
        Integer rowCount = 0
        for (CSVRecord csvRecord : parser) {
            if (removeBom(csvRecord.get(0)) == Constants.ExcelDataUpload.ERMR_DATA_CAPTURE_POINTER) {
                rowCount = 1
                continue
            }
            if (rowCount > 0) {
                substanceName = csvRecord.get(0)
                break
            }

        }
        return substanceName
    }

    static String removeBom(String columnName) {
        final String UTF8_BOM = "\uFEFF"
        return columnName.replaceAll(UTF8_BOM, "").trim()
    }


    static String trimHyperLink(String link) {
        try {
            link.substring(link.indexOf('href="') + 6, link.indexOf('">'))
        }
        catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    static List fetchColumnsCaseListing(File file) {
        List actualColumnMapping = []
        Reader inputFile = new FileReader(file)
        CSVParser parser = CSVParser.parse(inputFile, CSVFormat.EXCEL)
        for (CSVRecord csvRecord : parser) {
            if (removeBom(csvRecord.get(0)) == Constants.ExcelDataUpload.CASE_LISTING_CAPTURE_POINTER) {
                for (column in csvRecord) {
                    actualColumnMapping.add(removeBom(column.toString().toUpperCase()))
                }

            }

        }
        return actualColumnMapping
    }

    static String changeDateFormat(String date) {
        DateUtil.simpleDateReformat(date, "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy")
    }

    static String trimCarriage(String value) {
        return value.replace("<BR>", "")
    }
}
