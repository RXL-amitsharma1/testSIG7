package com.rxlogix

import com.rxlogix.config.CaseLineListingTemplate
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.ReportFieldLabel
import com.rxlogix.enums.ReportFieldSelectionTypeEnum
import grails.gorm.multitenancy.Tenants
import grails.plugin.cache.Cacheable
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap

@Transactional
class ReportFieldService {
    def dataSource_pva
    def userService
    def customMessageService
    def messageSource
    def cacheService
    def Map<?, ?> values = null
    def valuesFileName = "selectable_list_file.dat"
    private ConcurrentHashMap<String,?> reportFieldCache =[:]
    def getSelectableValuesForFields(String lang) {
        if (values == null) {
            values = readValues()

            if (values == null) {
                values = retrieveValuesFromDatabase(lang)
                if (values != null && values.keySet().size() > 0)
                    serializeValues(values)
            }
        }
        return values
    }

   Map getNonCacheSelectableValuesForFields(String field, String lang, String searchTerm, int max, int offset) {
        ReportField reportField = ReportField.findByNameAndIsDeleted(field,false)
        if (reportField?.nonCacheSelectable) {
            NonCacheSelectableList selectableListInstance = (NonCacheSelectableList) reportField.listDomainClass.newInstance()
            selectableListInstance.dataSource = dataSource_pva
            selectableListInstance.sqlString = getLmSql(reportField.lmSQL,lang)
            return selectableListInstance.getPaginatedSelectableList(searchTerm, max, offset, Holders.config.signal.default.tenant.id)
        }
        return [:]
    }

    public def getExtraValuesForFields() {
        HashMap values = [:]
        values.put("dvListednessReassess", getListValues(ReportField.findByName("dvListednessReassess")))
        return values
    }

    def retrieveValuesFromDatabase(String lang) {
        def localValues = [:]
        ReportField.findAllByIsDeleted( false).each { field ->
            def fieldValues = []
            if(field.name == "cmadlflagEligibleLocalExpdtd"){
                fieldValues = getListValues(field, field.listDomainClass?.name,new Locale("en").toString())
            } else {
                fieldValues = getListValues(field, field.listDomainClass?.name,new Locale(lang).toString())
            }
            if (fieldValues != null) {
                localValues.put(field.name, fieldValues)
            } else {
                localValues.put(field.name, '')
            }
        }
        localValues
    }

    @org.springframework.cache.annotation.Cacheable(value = 'selectableValues', key = '#name.toString().concat(#lang.toString())')
    def getListValues(ReportField reportField, String name, String lang) {
        def selectableList = []
        if (reportField.hasSelectableList() && name != '') {
            // may be redundant if we only look for Reportfields which have a selectable list
            long start = System.currentTimeMillis()
            SelectableList selectableListInstance = (SelectableList) reportField.listDomainClass.newInstance()
            String cachekey = selectableListInstance.class.name
            if (selectableListInstance.hasProperty('dataSource')) {
                selectableListInstance.dataSource = dataSource_pva
                selectableListInstance.fieldName = reportField.name
                String sql = cacheService.getRptToUiLabelInfoPvrSql(reportField.name) ?:cacheService.getRptToUiLabelInfoPvrSql(reportField?.lmSQL?.substring(13))?:getLmSql(reportField.lmSQL,lang)
                if(sql && sql.contains(':SEARCH_TERM')){
                    sql = sql.replace(':SEARCH_TERM', '\'%%\'')
                }
                if(sql && sql.contains(':TENANT_ID')){
                    sql = sql.replace(':TENANT_ID', '1')
                }
                selectableListInstance.sqlString = sql
                // instead of getLmSql for query, add updated query from cache as sqlString here
                cachekey = "${cachekey}-${reportField.lmSQL}"
            }


            String reportFieldName = customMessageService.getMessage("app.reportField." + reportField.name)
            String locale = reportFieldName.endsWith('(J)') ? Locale.JAPANESE : Locale.ENGLISH
            cachekey = "${cachekey}-${locale}"
            //First check in reportFieldCacheMap if the value already existing -PVR-12280
            selectableList = getReportFieldCache(cachekey)
            //If values not existing with the key (classname, and (lmquery + lang in case of datasource property available) -PVR-12280
            if (!selectableList) {
                selectableList = selectableListInstance.getSelectableList()
                //Store fetch value in reportFieldCache map - PVR-12280
                putReportFieldCache(cachekey, new ArrayList(selectableList))
            }
            long end = System.currentTimeMillis()
            log.debug("Found ${reportField.name} lang ${lang} with ${selectableListInstance.class.name} with entries in ${end - start}ms")
        }
        return selectableList
    }
    private void putReportFieldCache(String key,List<Object> value) {
        reportFieldCache.put(key,value);
    }
    private List<Object> getReportFieldCache(String key) {
        return reportFieldCache.get(key)
    }
    private getLmsql(code) {

        def reportFieldLabel = null
        ReportFieldLabel.withTransaction {
            reportFieldLabel = ReportFieldLabel.findByCode(code)
        }
        if (reportFieldLabel) {
            reportFieldLabel.textVal
        } else {
            code
        }
    }

    String getLmSql(String lmSql, String lang) {
        if (!lang) {
            return ""
        }
        String value = ""
        try {
            if (lang == null) {
                lang = "en"
            }
            if (lmSql) {
                value = messageSource.getMessage(lmSql?.trim(), [].toArray(), new Locale(lang))
            }
        } catch (NoSuchMessageException) {
            log.error("No localization lmsql code value found for ${lmSql} ")
            value = lmSql
        }
        return value
    }
    boolean isImportValidatable(List<String> caseNumberFieldNamesList = null,ReportField reportField) {
        List caseNumbers=[]
        def tempData=getSelectableValuesForFields("en");
        if (reportField != null) {
            if(tempData.get(reportField.name).equals(""))
            {
                caseNumbers=[]
            }else{
                caseNumbers=tempData.get(reportField.name)
            }
        }else{
            caseNumbers=tempData
        }
        List<String> caseNumberFieldNames = caseNumberFieldNamesList ?: caseNumbers
        if (caseNumberFieldNamesList && (reportField.listDomainClass || (reportField.dictionaryType && reportField.dictionaryLevel) || reportField.name in caseNumberFieldNames || reportField.name.equals(CaseLineListingTemplate.CLL_TEMPLATE_REPORT_FIELD_NAME) || reportField.name.equals(CaseLineListingTemplate.CLL_TEMPLATE_J_REPORT_FIELD_NAME))) {
            return true
        }else if(reportField.name.equals(CaseLineListingTemplate.CLL_TEMPLATE_REPORT_FIELD_NAME) || reportField.name.equals(CaseLineListingTemplate.CLL_TEMPLATE_J_REPORT_FIELD_NAME)){
            return true
        }else{
            return false
        }
    }

    // Autocomplete
    List retrieveValuesFromDatabaseSingle(ReportField field, String search, String lang) {
        List result = []
        if (field?.hasSelectableList()) {
            if (field.isAutocomplete) {
                Sql sql = new Sql(dataSource_pva)
                try {
                    String columnName = ""
                    List<GroovyRowResult> rows = sql.rows(getLmSql(field.lmSQL,lang), ['%' + search.toUpperCase() + '%']) { ResultSetMetaData meta ->
                        columnName = meta.getColumnName(1)
                    }
                    rows.each {
                        result.add(it.getProperty(columnName))
                    }
                } catch (SQLException e) {
                    log.error("Could not complete autocomplete SQL for ReportField: ${field.name}, search: ${search}")
                    e.printStackTrace()
                } finally {
                    sql.close()
                }
            } else {
                log.error("Field '$field.name' should be marked as autocomplete in the ARGUS database.")
            }
        }
        return result
    }

    def serializeValues(values) {
        FileOutputStream fos = null
        ObjectOutputStream oos = null
        try {
            fos = new FileOutputStream(new File(System.getProperty("java.io.tmpdir"),  valuesFileName))
            oos = new ObjectOutputStream(fos)
            oos.writeObject(values)
        } catch (all) {
            log.error("Errors when serialize selectable values to file", all)
        } finally {
            oos.close()
            fos.close()
        }
    }

    def readValues() {
        File f = new File(System.getProperty("java.io.tmpdir"),  valuesFileName)
        if (f.exists()) {
            FileInputStream fis = null
            ObjectInputStream ois = null
            try {
                fis = new FileInputStream(f)
                ois = new ObjectInputStream(fis)
                def o = ois.readObject()
                return o as Map
            } catch (all) {
                log.error("Error reading the selectable values from file", all)
            } finally {
                ois?.close()
                fis?.close()
            }
        }
        null
    }

    @Cacheable('reportFieldGroups')
    def getReportFieldsForQuery() {

        /*
            At the moment, only the usage from Query is cached.
            Usage from Template will call getReportFields() directly.
         */
        getReportFields(ReportFieldSelectionTypeEnum.QUERY)
    }

    def getReportFields(ReportFieldSelectionTypeEnum reportFieldSelectionTypeEnum) {
        def fieldGroups = ReportFieldGroup.getAll()
        List fieldSelection = []

        def selector
        if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.QUERY) {
            selector = "findAllByFieldGroupAndQuerySelectable"
        } else if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.CLL) {
            selector = "findAllByFieldGroupAndTemplateCLLSelectable"
        } else if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.DT_ROW) {
            selector = "findAllByFieldGroupAndTemplateDTRowSelectable"
        } else if (reportFieldSelectionTypeEnum == ReportFieldSelectionTypeEnum.DT_COLUMN) {
            selector = "findAllByFieldGroupAndTemplateDTColumnSelectable"
        }

        fieldGroups.each {
            def children =  ReportField."$selector"(it, true)
            fieldSelection.add(text: it.name, children: children)
        }

        return fieldSelection
    }

    @Cacheable('reportFields')
    def getAllReportFields() {
        return ReportField.findAllByIsDeleted(false)
    }
}
