package com.rxlogix.signal
import com.rxlogix.mart.CaseSeriesTagMapping

class SingleGlobalTag {

    Long caseId
    Long tagId
    String tagText
    Long caseSeriesId
    String owner
    Date lastUpdated

    static constraints = {
        caseSeriesId nullable: true
        owner nullable: true
    }

    SingleGlobalTag (CaseSeriesTagMapping caseSeriesTagMapping){
        caseId = caseSeriesTagMapping.caseId
        tagId = caseSeriesTagMapping.tag?.getId()
        tagText = caseSeriesTagMapping.tag?.name
        lastUpdated = caseSeriesTagMapping.lastUpdated
        owner = caseSeriesTagMapping.owner
        caseSeriesId = caseSeriesTagMapping.caseSeriesExecId
    }
    
    static mapping = {
        table "SINGLE_CASE_ALL_TAG"
        autoTimestamp false

    }
}
