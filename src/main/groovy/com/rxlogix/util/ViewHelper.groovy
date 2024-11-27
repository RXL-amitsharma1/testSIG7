package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.config.SetOperator
import com.rxlogix.enums.*
import com.rxlogix.mapping.LmDatasheet
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import grails.util.Holders
import groovy.json.JsonSlurper
import org.springframework.context.i18n.LocaleContextHolder as LCH
import com.rxlogix.pvdictionary.config.PVDictionaryConfig

import java.text.SimpleDateFormat

class ViewHelper {

    static String getMessage(String code, Object[] params = null, String defaultLabel='') {
        return getMessageSource().getMessage(
                code,
                params,
                defaultLabel,
                LCH.getLocale())
    }

    static getStrategyFrequency() {
        return (FrequencyEnum.values() - FrequencyEnum.HOURLY  - FrequencyEnum.DAILY  - FrequencyEnum.RUN_ONCE).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getStretegyType() {
        return (StrategyTypeEnum.values()).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getSignalSummaryReportsSection() {
        return (SignalSummaryReportSectionsEnum.values()).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getTrendFrequencyEnum() {
        return (TrendFrequencyEnum.values()).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }



    static getDateRangeTypeI18n() {
        return (DateRangeTypeCaseEnum.values() - DateRangeTypeCaseEnum.SUBMISSION_DATE).findAll { it.getI18nKey() }.sort({it?.getI18nKey()?.toUpperCase()}).collect{
            [name: it, display: it?.getI18nKey()]
        }
    }

    static getBusinessConfigurationType() {
        return (BusinessConfigTypeEnum.values() - BusinessConfigTypeEnum.EB05 - BusinessConfigTypeEnum.EB95 - BusinessConfigTypeEnum.EB05CHECK).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }


    static getDrugTypeI18n() {
        return DrugTypeEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getEvaluateCaseDateI18n() {
        return (EvaluateCaseDateEnum.values() - EvaluateCaseDateEnum.VERSION_ASOF_GENERATION_DATE - EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD).sort({ getMessage(it?.getI18nKey()) }).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getEvaluateCaseDateSubmissionI18n() {
        return (EvaluateCaseDateEnum.values() - EvaluateCaseDateEnum.VERSION_ASOF).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getSetOperatorI18n() {
        return SetOperator.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDateRange() {
        return (DateRangeEnum.getDateRange()).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDateRangeLiterature() {
        return (DateRangeEnum.getDateOperatorsLiterature()).collect {
                [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDateRangeReportSection() {
        List<DateRangeEnum> dateRangeEnumList = DateRangeEnum.periodicReportTemplateDateRangeOptions
        return dateRangeEnumList.sort({getMessage(it?.getI18nKey())}).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getNewDateRangeReportSection() {
        List<DateRangeEnum> dateRangeEnumList = []
        if(Holders.config.signal.faers.enabled || Holders.config.signal.vigibase.enabled || Holders.config.signal.vaers.enabled){
            dateRangeEnumList =  DateRangeEnum.newPeriodicReportTemplateDateRangeOptions
        } else {
            dateRangeEnumList =  DateRangeEnum.newPeriodicReportTemplateDateRangeOptionsForPVA
        }
        return dateRangeEnumList.collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getAssessmentFilterDateRange() {
        return SignalAssessmentDateRangeEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getEudraDateRange() {
        return DateRangeEnum.getEudraDateOperators().sort({ getMessage(it?.getI18nKey()) }).collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getQuantitativeDateRange() {
        return DateRangeEnum.getQuantitativeDateOperators().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getQueryLevels() {

        return com.rxlogix.enums.QueryLevelEnum.values().sort()
    }

    static getAttachmentFormat(List<ReportFormat> reportFormats) {
        def formats = []
        reportFormats.each {
            formats.add(getMessage(it.getI18nKey()))
        }
        return formats
    }

    static String getTimeZone(User user) {
       return user?.preference?.timeZone?:TimeZone.getDefault()?.getID()
    }

    static String dateRangeString(def dateGiven,def timezone) {
        if(dateGiven) {
            if (!dateGiven instanceof Date) {
                getDateRangeStringForGivenTimezone(new Date(dateGiven.getTime()), timezone)
            } else {
                getDateRangeStringForGivenTimezone(dateGiven.getTime(), timezone)
            }

        }else {
            return null
        }
    }

    private static getDateRangeStringForGivenTimezone(Date date, String tz) {
        SimpleDateFormat sdf = new SimpleDateFormat(RelativeDateConverter.longDateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(tz))
        String dateValue = sdf.format(date) // gives string date original value
        def val = dateValue.tokenize(" ")
        return sdf.parse(val[0])
    }

    static getDataTabulationMeasures() {
        return MeasureTypeEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDataTabulationCounts() {
        return CountTypeEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDatasheet() {
        return LmDatasheet.findAll().sheetName.unique().collect {
            [name: it, display: it] // use id and i18n later
        }
    }

    static getReassessListedness() {
        return ReassessListednessEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDataTabulationPercentageOptions() {
        return PercentageOptionEnum.values()
    }

    static getAllProductFamilies() {
        return LmProductFamily.findAll().unique().sort({it.name.toUpperCase()})
    }

    //This helper method will serve the purpose for all the Alert types i.e. EvdasConfiguration, BaseConfiguration and LiteratureConfiguration.
    static getDictionaryValues(def obj, DictionaryTypeEnum dictionaryType, Boolean isShowWHODrug = false) {
        if(!(obj && obj.(dictionaryType.value()))) return ""
        getDictionaryValues(obj.(dictionaryType.value()), dictionaryType, isShowWHODrug)
    }
    static getProductHierarchyData(def jsonStr){
        def jsonParser = new JsonSlurper()
        def data = jsonParser.parseText(jsonStr)
        def result = data.values().collect { value -> "${value} (All Products)" }.join(", ")
        return result
    }

    static getDictionaryValues(String jsonString, DictionaryTypeEnum dictionaryType, Boolean isShowWHODrug = false) {

        Map eventSelectionMap = [1: "SOC", 2: "HLGT", 3: "HLT", 4: "PT", 5: "LLT", 6: "Synonyms", 7: "SMQ Broad", 8: "SMQ Narrow"]
        Map productSelection = [1: "Ingredient", 2: "Family", 3: "Product Name", 4: "Trade Name"]
        Map studySelection = [1: "Study Number", 2: "Project Number", 3: "Center"]

        def data = []

        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(jsonString)
        if (dictionaryType.value() == DictionaryTypeEnum.EVENT.value()) {
            object.collect { map ->
                if (map.value != []) {
                    data << (map.value.name.collect {
                        it + " (" + eventSelectionMap.get(map.key as Integer) + ")"
                    }.join(", "))
                }
            }
        }
        if (isShowWHODrug) {
            object.collect { key, value ->
                if (key == Constants.DRUG_RECORD_NUMBER_LEVEL && value != []) {
                    value.flatten().findAll { it.id && it.name } // Flatten and filter entries with id and name
                            .collect { entry ->
                                data << "${entry.id} (${entry.name})"
                            }

                }
            }
        } else if (dictionaryType.value() == DictionaryTypeEnum.PRODUCT.value()) {
            object.collect {
                if (it.value != [] && it.key != Constants.DRUG_RECORD_NUMBER_LEVEL) {
                    data << ((it.value.name.join(", ")))
                }
            }
        }
        if(dictionaryType.value() == DictionaryTypeEnum.STUDY.value()) {
            object.collect {
                if(it.value!= []) {
                    data << ((it.value.name.join(", "))+" ("+studySelection.get(it.key as Integer)+")")
                }
            }
        }
        if (dictionaryType.value() == DictionaryTypeEnum.PRODUCT_GROUP.value()) {
            return object.name.join(" (Product Group), ") + ' (Product Group)'
        }
        if (dictionaryType.value() == DictionaryTypeEnum.EVENT_GROUP.value()) {
            return object.name.join(" (Event Group), ") + ' (Event Group)'
        }

        return data.join(", ")
    }

    static getProductDictionaryValues(def obj, DictionaryTypeEnum dictionaryType) {
        if(!obj) return ""
        String jsonString = obj.(dictionaryType.value())
        Map productSelection = [:]
        def productList = getHiearchyValues()
        productList.eachWithIndex { field, index ->
            productSelection[index + 1] = field
        }
        productSelection[Constants.DRUG_RECORD_NUMBER_LEVEL_IDX] = Constants.DRUG_RECORD_NUMBER
        def data = []

        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(jsonString)
        if (dictionaryType.value() == DictionaryTypeEnum.PRODUCT.value()) {
            object.collect { map ->
                if (map.value != []) {
                    data << (map.value.collect {
                        if (map.key == Constants.DRUG_RECORD_NUMBER_LEVEL) {
                            it.id + " (" + it.name + ") (" + productSelection.get(map.key as Integer) + ")"
                        } else {
                            it.name + " (" + productSelection.get(map.key as Integer) + ")"
                        }
                    }.join(", "))
                }
            }
        }
        return data.join(", ")
    }


    static getEventDictionaryValues(def configuration) {

        Map eventSelectionMap = [1: "SOC", 2: "HLGT", 3: "HLT", 4: "PT", 5: "LLT", 6: "Synonyms", 7: "SMQ Broad", 8: "SMQ Narrow"]
        List data = []

        JsonSlurper jsonSlurper = new JsonSlurper()
        def object
        if (configuration.eventGroupSelection) {
            object = jsonSlurper.parseText(configuration.eventGroupSelection)
            object.each { map ->
                data << map.name.substring(0, map.name.lastIndexOf('(') - 1) + " (Event Group)"
            }
        }

        if (configuration.eventSelection) {
            object = jsonSlurper.parseText(configuration.eventSelection)
            object.collect { map ->
                if (map.value != []) {
                    data << (map.value.name.collect {
                        it + " (" + eventSelectionMap.get(map.key as Integer) + ")"
                    }.join(", "))
                }
            }
        }

        return data.join(", ")
    }

    static getUserTimeZoneForConfig(def configurationInstance, User user) {
        if(configurationInstance?.nextRunDate && configurationInstance.isEnabled) {
            return configurationInstance?.configSelectedTimeZone
        } else {
            return user?.preference?.timeZone
        }

    }

    static getCommaSeperatedFromList(def tagsList) {
        tagsList.inject( '' ) { s, v ->
            s + ( s ? ', ' : '' ) + v.name
        }
    }

    static List getReportExecutionStatusEnumI18n(){
        return ReportExecutionStatus.getExeuctionStatusList().collect{
            [name: it.key, display: getMessage(it.getI18nValueForExecutionStatusDropDown())]
        }
    }

    static getTimezoneValues() {
        return TimeZoneEnum.values().collect {
            [name: it?.timezoneId, display: getMessage(it?.getI18nKey(), it?.getGmtOffset())]
        }
    }

    static getMessageByTimeZoneId(String timezone) {
        for(TimeZoneEnum e : TimeZoneEnum.values()) {
            if(e.timezoneId.equals(timezone)) {
                return getMessage(e?.getI18nKey(), e?.getGmtOffset())
            }
        }
    }

    static getHiearchyValues() {
        PVDictionaryConfig.ProductConfig.views.collect {
            getMessage(it.code, null, Locale.default)
        }
    }
    static getHiearchyValuesWithIndex(String hierarchy) {
        List <String> productList = getHiearchyValues()
        return productList.indexOf(hierarchy)
    }
}
