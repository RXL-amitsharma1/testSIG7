package com.rxlogix.config

import com.rxlogix.NonCacheSelectableList
import com.rxlogix.SelectableList
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.util.DbUtil

class ReportField {

    def messageSource
    def cacheService

    String name
    String description
    String transform
    ReportFieldGroup fieldGroup
    SourceColumnMaster sourceColumn
    Class dataType
    boolean isText = false      //denotes whether we show this element as select2 in the UI or not
    Class listDomainClass
    String lmSQL
    boolean querySelectable
    boolean templateCLLSelectable
    boolean templateDTRowSelectable
    boolean templateDTColumnSelectable
    DictionaryTypeEnum dictionaryType
    Integer dictionaryLevel

    String dateFormat = "dd-MM-yyyy"
    boolean isAutocomplete
    boolean isEudraField = false
    boolean isDeleted = false

    String preQueryProcedure
    String postQueryProcedure
    String preReportProcedure
    Integer fixedWidth
    Integer widthProportionIndex

    static mapping = {
        cache: "read-only"
        version: false
        /**
         * Because we are heavily dependent on fieldGroup and argusColumn eagerly fetch them
         * when loading this object
         */
        fieldGroup lazy: false
        sourceColumn lazy:false

        table name: "RPT_FIELD"

        name column: "NAME"
        description column: "DESCRIPTION"
        transform column: "TRANSFORM"
        fieldGroup column: "RPT_FIELD_GRPNAME"
        sourceColumn column: "SOURCE_COLUMN_MASTER_ID"
        dataType column: "DATA_TYPE"
        isText column: "IS_TEXT"
        listDomainClass column: "LIST_DOMAIN_CLASS"
        lmSQL column: "LMSQL", sqlType: DbUtil.longStringType
        querySelectable column: "QUERY_SELECTABLE"
        templateCLLSelectable column: "TEMPLT_CLL_SELECTABLE"
        templateDTRowSelectable column: "TEMPLT_DTROW_SELECTABLE"
        templateDTColumnSelectable column: "TEMPLT_DTCOL_SELECTABLE"
        dictionaryType column: "DIC_TYPE"
        dictionaryLevel column: "DIC_LEVEL"
        isDeleted column: "IS_DELETED"
        dateFormat column: "DATE_FORMAT"
        isAutocomplete column: "ISAUTOCOMPLETE"
        isEudraField column : "IS_EUDRAFIELD"
        isDeleted column: "IS_DELETED"
        preQueryProcedure column: "PRE_QUERY_PROCEDURE"
        postQueryProcedure column: "POST_QUERY_PROCEDURE"
        preReportProcedure column: "PRE_REPORT_PROCEDURE"
        fixedWidth column: "FIXED_WIDTH"
        widthProportionIndex column: "WIDTH_PROPORTION_INDEX"
    }

    static constraints = {
        name(maxSize:128, blank: false, unique: true)
        description(nullable: true)
        transform(nullable: true)
        listDomainClass(nullable: true)
        lmSQL(maxSize: 32*1024, nullable:true)
        dictionaryType(nullable: true)
        dictionaryLevel(nullable: true)
        fixedWidth(nullable: true)
        widthProportionIndex(nullable: true)
        preQueryProcedure(nullable: true)
        postQueryProcedure(nullable: true)
        preReportProcedure(nullable: true)
        sourceColumn(nullable: true)
    }

    public boolean isString() {
        return String.class.equals(dataType)
    }

    public boolean isNumber() {
        return Number.class.isAssignableFrom(dataType)
    }

    public boolean isDate() {
        return Date.class.equals(dataType)
    }

    public boolean hasSelectableList() {
        if ( listDomainClass ) {
            return SelectableList.isAssignableFrom(listDomainClass)
        }
        return false
    }

    boolean isNonCacheSelectable() {
        return (listDomainClass?.name == NonCacheSelectableList.name && isAutocomplete)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ReportField that = (ReportField) o

        if (isDeleted != that.isDeleted) return false
        if (isText != that.isText) return false
        if (querySelectable != that.querySelectable) return false
        if (templateCLLSelectable != that.templateCLLSelectable) return false
        if (templateDTColumnSelectable != that.templateDTColumnSelectable) return false
        if (templateDTRowSelectable != that.templateDTRowSelectable) return false
        if (sourceColumn != that.sourceColumn) return false
        if (dataType != that.dataType) return false
        if (dateFormat != that.dateFormat) return false
        if (description != that.description) return false
        if (dictionaryLevel != that.dictionaryLevel) return false
        if (dictionaryType != that.dictionaryType) return false
        if (fieldGroup != that.fieldGroup) return false
        if (listDomainClass != that.listDomainClass) return false
        if (lmSQL != that.lmSQL) return false
        if (name != that.name) return false
        if (transform != that.transform) return false
        if (isAutocomplete != that.isAutocomplete) return false
        if (isEudraField != that.isEudraField) return false

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (transform != null ? transform.hashCode() : 0)
        result = 31 * result + (fieldGroup != null ? fieldGroup.hashCode() : 0)
        result = 31 * result + (sourceColumn != null ? sourceColumn.hashCode() : 0)
        result = 31 * result + dataType.hashCode()
        result = 31 * result + (isText ? 1 : 0)
        result = 31 * result + (listDomainClass != null ? listDomainClass.hashCode() : 0)
        result = 31 * result + (lmSQL != null ? lmSQL.hashCode() : 0)
        result = 31 * result + (querySelectable ? 1 : 0)
        result = 31 * result + (templateCLLSelectable ? 1 : 0)
        result = 31 * result + (templateDTRowSelectable ? 1 : 0)
        result = 31 * result + (templateDTColumnSelectable ? 1 : 0)
        result = 31 * result + (dictionaryType != null ? dictionaryType.hashCode() : 0)
        result = 31 * result + (dictionaryLevel != null ? dictionaryLevel.hashCode() : 0)
        result = 31 * result + (isDeleted ? 1 : 0)
        result = 31 * result + (isAutocomplete ? 1 : 0)
        result = 31 * result + (isEudraField ? 1 : 0)
        result = 31 * result + dateFormat.hashCode()
        return result
    }

    String getLmSql(String lang) {
        if (!lang) {
            return ""
        }
        String value = ""
        try {
            if (lmSQL) {
                value = messageSource.getMessage(lmSQL, [].toArray(), new Locale(lang))
            }
        } catch (NoSuchMessageException) {
            log.error("No localization lmsql code value found for ${lmSQL} with field name as ${name}")
            value = lmSQL
        }
        return value
    }

    String getDateFormat(String lang) {
        if (!lang) {
            return ""
        }
        if (!dateFormat) {
            return "dd-MM-yyyy"
        }
        String value = ""
        try {
            value = messageSource.getMessage(dateFormat, [].toArray(), new Locale(lang))
        } catch (NoSuchMessageException) {
            log.error("No localization date code value found for ${dateFormat} with field name as ${name}")
            value = dateFormat
        }
        return value
    }

    @Override
    String toString(){
        return cacheService.getRptToUiLabelInfoPvrEn(this?.name)?:messageSource.getMessage("app.reportField.$name", null, Locale.ENGLISH)
    }


}
