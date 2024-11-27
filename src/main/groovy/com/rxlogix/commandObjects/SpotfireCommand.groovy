package com.rxlogix.commandObjects

import grails.util.Holders
import grails.validation.Validateable
import groovy.json.JsonOutput

class SpotfireCommand implements Validateable {

    static final Date defaultDate = Date.parse("MM/DD/YYYY", "01/01/1800") // when existing casesId used
    static final Long defaultCaseSeriesId = -1

    Set<String> productFamilyIds
    Date fromDate
    Date endDate
    Date asOfDate
    String type
    String fullFileName
    Long caseSeriesId
    Set<String> whoFamilyIds
    Boolean isWHODrug


    static constraints = {
        productFamilyIds(nullable: true, blank: false, validator: { value, obj ->
            if (obj.caseSeriesId) {
                return true
            }
        })

        fromDate(nullable: true, blank: false, validator: { value, obj ->
            if (obj.caseSeriesId) {
                return true
            }
        })

        endDate(nullable: true, blank: false, validator: { value, obj ->
            if (obj.caseSeriesId) {
                return true
            }

            if (value < obj.fromDate) {
                return "com.rxlogix.commandObjects.SpotfireCommand.endDate.before.fromDate"
            }
        })

        asOfDate(nullable: true, blank: false, validator: { value, obj ->
            if (obj.caseSeriesId) {
                return true
            }
        })

        fullFileName nullable: false, blank: false, validator: { value, obj ->
            def spotfireService = Holders.applicationContext.getBean("spotfireService")
            if (spotfireService.invalidFileNameLength(value)) {
                return ["tooLong"]
            }
            if (!(value ==~ /^[a-zA-Z0-9]+((-|_|.|\s)+[a-zA-Z0-9]+)*$/)) {
//                TODO We need to work for Japanese names validator
//                We are allowing only Alphabets of English, Numeric Numbers, Underscores(in between of name), dashes(in between of name), spaces(in between of name) only. Start should be with Alphabet or Numeric and end should be with Alphabet or Numeric
                return ["invalid"]
            }
            if (spotfireService.fileNameExist(value)) {
                return ["duplicated"]
            }
        }
        caseSeriesId nullable: true
        isWHODrug nullable: true
    }

    Date getFromDate() {
        return caseSeriesId ? fromDate : defaultDate
    }

    Date getEndDate() {
        return caseSeriesId ? endDate : defaultDate
    }

    Date getAsOfDate() {
        return caseSeriesId ? asOfDate : defaultDate
    }

    Long getCaseSeriesId() {
        return caseSeriesId ?: defaultCaseSeriesId
    }

    Date getActualFromDate() {
        return fromDate ?: defaultDate
    }

    Date getActualEndDate() {
        return endDate ?: defaultDate
    }

    Date getActualAsOfDate() {
        return asOfDate ?: defaultDate
    }

    String toJsonString() {
        JsonOutput.toJson([productFamilyIds     : productFamilyIds,
                           fromDate             : fromDate,
                           endDate              : endDate,
                           type                 : type,
                           fullFileName         : fullFileName,
                           caseSeriesId         : caseSeriesId])
    }
}
