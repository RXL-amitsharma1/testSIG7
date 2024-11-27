package com.rxlogix.commandObjects

import grails.validation.Validateable

class SaveCaseSeriesFromSpotfireCO implements Validateable{

    String user
    String seriesName
    String caseNumbers

    static constraints = {
        user nullable: false, blank: false
        seriesName nullable: true
        caseNumbers nullable: false, blank: false
    }

    Set<String> generateSetForCaseNumbers() {
        return caseNumbers? caseNumbers.tokenize(',') : []
    }

}
