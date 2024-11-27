package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.mapping.*
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import grails.util.Holders
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.web.json.JSONElement
import org.springframework.web.multipart.MultipartFile

import javax.sql.DataSource
import java.sql.SQLException
import java.text.ParseException
import java.text.SimpleDateFormat

@Transactional
class ImportService {

    def userService
    def grailsApplication
    DataSource dataSource_pva

    private Set<CustomSQLValue> getParsedCustomSQLValues(JSONElement it) {
        Set<CustomSQLValue> parsedCustomSQLValues = []
        if (it?.customSQLValues) {
            it?.customSQLValues?.each { customSQLValue ->
                parsedCustomSQLValues.add(new CustomSQLValue(key: customSQLValue.key, value: customSQLValue.value).save())
            }
        }

        return parsedCustomSQLValues
    }

    private ReportFieldInfoList getParsedRows(JSONElement it) {
        ReportFieldInfoList parsedRows = null
        if (it?.rowList) {
            parsedRows = new ReportFieldInfoList()
            it.rowList.each {
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
                reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
                if (!reportFieldInfo.reportField) {
                    log.error("ReportField not found for name : ${it.reportFieldName}")
                }
                parsedRows.addToReportFieldInfoList(reportFieldInfo)
            }
            parsedRows.save(failOnError: true)
        }
        return parsedRows
    }

    private ReportFieldInfoList getParsedServiceCols(JSONElement it) {
        ReportFieldInfoList parsedServiceCols = new ReportFieldInfoList()
        it.serviceColumnList.each {
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            parsedServiceCols.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedServiceCols.save(failOnError: true)
        return parsedServiceCols
    }

    private ReportFieldInfoList getParsedRowCols(JSONElement it, ReportFieldInfoList parsedRowCols) {
        parsedRowCols = new ReportFieldInfoList()
        it.rowColumnList.each {
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            parsedRowCols.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedRowCols.save(failOnError: true)
        return parsedRowCols
    }

    private ReportFieldInfoList getParsedGrouping(JSONElement it, ReportFieldInfoList parsedGrouping) {
        parsedGrouping = new ReportFieldInfoList()
        it.groupingList.each {
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            if (it.sortEnumValue) {
                reportFieldInfo.sort = SortEnum.valueOfName(it.sortEnumValue)
            }
            parsedGrouping.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedGrouping.save(failOnError: true)
        return parsedGrouping
    }

    private ReportFieldInfoList getParsedColumns(JSONElement it, ReportFieldInfoList parsedColumns) {
        parsedColumns = new ReportFieldInfoList()
        it.columnList.each {
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(it)
            reportFieldInfo.reportField = ReportField.findByNameAndIsDeleted(it.reportFieldName, false)
            if (!reportFieldInfo.reportField) {
                log.error("ReportField not found for name : ${it.reportFieldName}")
            }
            if (it.sortEnumValue) {
                reportFieldInfo.sort = SortEnum.valueOfName(it.sortEnumValue)
            }
            parsedColumns.addToReportFieldInfoList(reportFieldInfo)
        }
        parsedColumns.save(failOnError: true)
        return parsedColumns
    }

    Map<String, List> getValidInvalidValues(ReportFieldService reportFieldService, List values, String fieldName, String lang) {
        log.debug("Validating for field name: ${fieldName}")
        Long tenantId = Holders.config.signal.default.tenant.id as Long
        List validValues = []
        Set invalidValues = values
        if (values && fieldName) {
            ReportField reportField = ReportField.findByName(fieldName)
            if (fieldName in [CaseLineListingTemplate.CLL_TEMPLATE_REPORT_FIELD_NAME, CaseLineListingTemplate.CLL_TEMPLATE_J_REPORT_FIELD_NAME]) {
                values.collate(500).each { list ->
                    validValues += CaseInfo.createCriteria().list {
                        projections {
                            distinct('caseNumber')
                        }
                        inList("caseNumber", list)
                    }
                }
                invalidValues = values - validValues
            }else if(!reportField){
                validValues += getDmvValidValues(values,fieldName)
                List valuesToCompareWith = validValues*.replaceAll("(?i)'", "''")*.toUpperCase()
                validValues = values.findAll {
                    it.replaceAll("(?i)'", "''").toUpperCase() in valuesToCompareWith
                }
                invalidValues = values - validValues
            }

            //We are validating only for those for which Domain class do exist.
           if (reportField && reportFieldService.isImportValidatable(values,reportField)) {
          if (reportField?.listDomainClass?.name) {
                 if (reportField.isAutocomplete) {
                     Sql sql = new Sql(dataSource_pva)
                    try {
                        String columnName = ''
                        String sqlString = ''
                        def result
                        String validateValues = ''
                         values.collate(500)?.each { inputValue ->
                            validateValues = "'" + inputValue.collect {
                                it.replaceAll("(?i)'", "''").toUpperCase()
                            }?.join("','") + "'"
                            sqlString = reportFieldService.getLmSql(reportField.lmSQL,lang) ?.replace("like ?", "in (${validateValues})")?.replace("LIKE :SEARCH_TERM", "in (${validateValues})") //TO handle Non cache and Auto queries. Also added tenant parameter.
                             if(sqlString.contains(":TENANT_ID")){
                                 result = sql.rows(sqlString, [TENANT_ID: tenantId]) { meta ->
                                     columnName = meta.getColumnName(1)
                                 }
                             }else{
                                 result = sql.rows(sqlString) { meta ->
                                     columnName = meta.getColumnName(1)
                                 }
                             }
                            validValues += result.collect { it.getProperty(columnName) }
                        }
                        List valuesToCompareWith = validValues*.replaceAll("(?i)'", "''")*.toUpperCase()
                        validValues = values.findAll {
                            it.replaceAll("(?i)'", "''").toUpperCase() in valuesToCompareWith
                        }
                        invalidValues = values - validValues
                    } catch (SQLException e) {
                        log.error("Could not Validate autocomplete SQL for ReportField: ${reportField.name}, ${e.message}")
                    } finally {
                        sql.close()
                    }
                } else {
                    List<Object> referenceValues = grailsApplication.mainContext.reportFieldService.getListValues(reportField, reportField.listDomainClass.name, lang)*.toUpperCase()
                     validValues = values.findAll { it.toUpperCase() in referenceValues }
                    invalidValues = values - validValues
                }
            } else if (reportField?.dictionaryType && reportField?.dictionaryLevel) {
                values = values.collect {it.replace("'","''")}
                switch (reportField.dictionaryType) {
                    case DictionaryTypeEnum.EVENT:
                        validValues = validateValuesFromEventDic(values, reportField.dictionaryLevel)*.toUpperCase()
                        break
                    case DictionaryTypeEnum.STUDY:
                        validValues = validateValuesFromStudyDic(values, reportField.dictionaryLevel)*.toUpperCase()
                        break
                    case DictionaryTypeEnum.PRODUCT:
                        validValues = validateValuesFromProductDic(values, reportField.dictionaryLevel)*.toUpperCase()
                        break

                }
                values = values.collect {it.replace("''","'")}
                validValues = values.findAll {it.toUpperCase() in validValues}
                invalidValues = values - validValues
            }
            }
        }
        return [validValues: validValues, invalidValues: invalidValues as List]
    }



    List getDmvValidValues(List<Object> values,String useCase) {
        Map tableInfoMap=new HashMap()
        List result=[]
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.rows("SELECT DECODE_TABLE,DECODE_COLUMN FROM pvs_batch_signal_constants_dsp WHERE use_case ='"+useCase+"'").collect {
                tableInfoMap.put(it.DECODE_TABLE,it.DECODE_COLUMN)
            }
            String tableName=tableInfoMap.keySet().getAt(0)
            String columnName=tableInfoMap.values().getAt(0)
            String inQuery=""
            values.each {
                inQuery+="lower('"+it+"'),"
            }
            inQuery=inQuery.substring(0, inQuery.length() - 1)
            String  query="SELECT "+columnName+" from "+tableName+" where lower("+columnName+") in ("+inQuery+")";
            sql.rows(query).collect{
                result.add(it[columnName])
            }
        } finally {
            sql.close()
        }
        return result

    }

    private List<Object> validateValuesFromEventDic(List<Object> values, int level) {
        List<Object> validValues = []
        switch (level) {
            case 1:
                validValues = MedDraSOC.createCriteria().list {
                    sqlRestriction("upper(SOC_NAME) in ('${values*.toUpperCase()?.join("','")}') and lang_id = 1")
                }.name
                break
            case 2:
                validValues = MedDraHLGT.createCriteria().list {
                    sqlRestriction("upper(HLGT_NAME) in ('${values*.toUpperCase()?.join("','")}') and lang_id = 1")
                }.name
                break
            case 3:
                validValues = MedDraHLT.createCriteria().list {
                    sqlRestriction("upper(HLT_NAME) in ('${values*.toUpperCase()?.join("','")}') and lang_id = 1")
                }.name
                break
            case 4:
                validValues = MedDraPT.createCriteria().list {
                    sqlRestriction("upper(PT_NAME) in ('${values*.toUpperCase()?.join("','")}') and lang_id = 1")
                }.name
                break
            case 5:
                validValues = MedDraLLT.createCriteria().list {
                    sqlRestriction("upper(LLT_NAME) in ('${values*.toUpperCase()?.join("','")}') and lang_id = 1")
                }.name
                break
            case 6:
                validValues = MedDraSynonyms.createCriteria().list {
                    sqlRestriction("upper(SYN) in ('${values*.toUpperCase()?.join("','")}') and lang_id = 1")
                }.name
                break

        }
        return validValues
    }

    private List<Object> validateValuesFromProductDic(List<Object> values, int level) {
        List<Object> validValues = []
        switch (level) {
            case 1:
                validValues = LmIngredient.createCriteria().list {
                    sqlRestriction("upper(INGREDIENT) in ('${values*.toUpperCase()?.join("','")}')")
                }.ingredient
                break
            case 2:
                validValues = LmProductFamily.createCriteria().list {
                    sqlRestriction("upper(FAMILY_NAME) in ('${values*.toUpperCase()?.join("','")}')")
                }.name
                break
            case 3:
                validValues = LmProduct.createCriteria().list {
                    sqlRestriction("upper(PRODUCT_NAME) in ('${values*.toUpperCase()?.join("','")}')")
                }.name
                break
            case 4:
                validValues = LmLicense.createCriteria().list {
                    sqlRestriction("upper(TRADE_NAME_APPROVAL_NUMBER) in ('${values*.toUpperCase()?.join("','")}')")
                }.tradeName
                break
        }
        return validValues
    }

    private List<Object> validateValuesFromStudyDic(List<Object> values, int level) {
        List<Object> validValues = []
        switch (level) {
            case 1:
                validValues = LmProtocols.createCriteria().list {
                    sqlRestriction("upper(PROTOCOL_DESCRIPTION) in ('${values*.toUpperCase()?.join("','")}')")
                }.description
                break
            case 2:
                validValues = LmStudies.createCriteria().list {
                    sqlRestriction("upper(STUDY_NUM) in ('${values*.toUpperCase()?.join("','")}')")
                }.studyNum
                break
            case 3:
                validValues = LmProduct.createCriteria().list {
                    sqlRestriction("upper(PRODUCT_NAME) in ('${values*.toUpperCase()?.join("','")}')")
                }.name
                break
        }
        return validValues
    }


    private createCustomSQLTemplate(JSONElement it, Map bindingMap, ReportTemplate reportTemplate) {
        Set<CustomSQLValue> parsedCustomSQLValues = getParsedCustomSQLValues(it)

        bindingMap.putAt("customSQLTemplateSelectFrom", it.customSQLTemplateSelectFrom)
        bindingMap.putAt("customSQLTemplateWhere", it.customSQLTemplateWhere)
        bindingMap.putAt("columnNamesList", it.columnNamesList)
        bindingMap.putAt("customSQLValues", parsedCustomSQLValues)
        bindingMap.putAt("hasBlanks", it?.hasBlanks)

        reportTemplate = new CustomSQLTemplate(bindingMap)

        return reportTemplate
    }


    private createNonCaseTemplate(JSONElement it, Map bindingMap, ReportTemplate reportTemplate) {
        Set<CustomSQLValue> parsedCustomSQLValues = getParsedCustomSQLValues(it)

        bindingMap.putAt("nonCaseSql", it?.nonCaseSql)
        bindingMap.putAt("columnNamesList", it?.columnNamesList)
        bindingMap.putAt("customSQLValues", parsedCustomSQLValues)
        bindingMap.putAt("hasBlanks", it?.hasBlanks)
        bindingMap.putAt("usePvrDB", it?.usePvrDB)
        bindingMap.putAt("chartCustomOptions", it?.chartCustomOptions)

        reportTemplate = new NonCaseSQLTemplate(bindingMap)

        return reportTemplate
    }

    private Map getBindingMapForLoading(def it, User user) {
        Map bindingMap = [
                category          : it.category,
                name              : it?.name,
                description       : it?.description,
                qualityChecked    : it.qualityChecked ?: false,
                createdBy         : user.username,
                modifiedBy        : user.username,
                owner             : user,
                templateType      : TemplateTypeEnum.valueOf(it.templateType.name),
                reassessListedness: it?.reassessListedness ? ReassessListednessEnum.valueOf(it?.reassessListedness?.name) : null,
                reassessForProduct: it?.reassessForProduct ?: false,
                templateFooter    : it?.templateFooter
        ]
        return bindingMap
    }

    public static List readFromExcel(MultipartFile file) {
        List set = []
        Workbook workbook = null

        if (file.originalFilename.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream);
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream);
        }

        Sheet sheet = workbook.getSheetAt(0);  //get the first worksheet from excel
        Row row;
        Cell cell;

        // starts reading the values from 2nd row
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            if ((row = sheet.getRow(i)) != null) {
                cell = (Cell) row?.getCell(0);  //get the first column from excel
                cell?.setCellType(Cell.CELL_TYPE_STRING);
                if (cell?.getStringCellValue()?.trim()?.length()) {
                    set << cell?.getStringCellValue()?.trim()
                }
            }
        }
        return set.sort() as List
    }

    Date tryParse(String dateString) {
        List<String> formatStrings = grailsApplication.config.grails.databinding.dateFormats
        for (String formatString : formatStrings) {
            try {
                return new SimpleDateFormat(formatString).parse(dateString)
            }
            catch (ParseException e) {
            }
        }
        return null
    }

}
