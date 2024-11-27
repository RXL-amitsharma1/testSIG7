package com.rxlogix.signal

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.user.User
import com.rxlogix.config.ReportExecutionStatus

class CaseForm implements Serializable {

    String formName
    String caseIds
    String versionNum
    String followUpNum
    String isDuplicate
    Date dateCreated
    User createdBy
    String caseNumbers
    Boolean isFullCaseSeries
    String advancedFilterName
    String viewInstanceName
    ReportExecutionStatus executionStatus = ReportExecutionStatus.SCHEDULED
    String savedName
    Boolean excelGenerated

    static belongsTo = [executedConfiguration : ExecutedConfiguration]

    static mapping = {
        id generator:'sequence', params:[sequence:'case_form_sequence']
        caseIds type: "text", sqlType: "clob"
        versionNum type: "text", sqlType: "clob"
        followUpNum type: "text", sqlType: "clob"
        isDuplicate type: "text", sqlType: "clob"
        caseNumbers type: "text", sqlType: "clob"
        executionStatus column: "EX_STATUS"
    }

    static constraints = {
        versionNum nullable: true
        isDuplicate nullable: true
        followUpNum nullable: true
        isFullCaseSeries nullable: true
        advancedFilterName nullable: true
        viewInstanceName nullable: true
        savedName nullable: true
        caseIds nullable: true
        caseNumbers nullable: true
        excelGenerated nullable: true
    }

    @Override
    String toString() {
        this.formName
    }
}
