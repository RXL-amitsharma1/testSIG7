package com.rxlogix.dto

import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.ProductClassification
import grails.validation.Validateable
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class SpotfireSettingsDTO implements Validateable {

    ProductClassification type
    List<DateRangeEnum> rangeType
    List<String> dataSource

    static constraints = {
        type nullable: false
        rangeType nullable: true
    }

    static SpotfireSettingsDTO fromJson(String json) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(json)

        SpotfireSettingsDTO sc = new SpotfireSettingsDTO()
        sc.type = object.type as ProductClassification
        sc.rangeType = object.rangeType.collect { it as DateRangeEnum }
        sc.dataSource = object.dataSource.collect {it}
        sc
    }


    String toJsonString() {
        JsonOutput.toJson([type                : type,
                           rangeType           : rangeType,
                           dataSource          : dataSource])
    }
}
