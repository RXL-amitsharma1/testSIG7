package com.rxlogix.signal

import com.rxlogix.config.ActionTemplate
import com.rxlogix.config.Disposition
import com.rxlogix.config.PVSState
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import groovy.transform.ToString

class RuleInformation implements GroovyInterceptable, AlertUtil, Serializable {
    static auditable = ['ignore': ['isFirstTimeRule', 'action', 'isSingleCaseAlertType','createdBy','modifiedBy']]

    String customSqlQuery
    String ruleName
    Integer ruleRank = 0
    String ruleJSON
    Boolean isFirstTimeRule = false
    Boolean isBreakAfterRule = false
    Disposition disposition
    String justificationText
    ActionTemplate action
    String format
    String signal
    String medicalConcepts
    String tags
    Boolean enabled = true
    Boolean isSingleCaseAlertType = false

    //Common db table fields.
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy


    static belongsTo = [businessConfiguration: BusinessConfiguration]

    static mapping = {
        table("RULE_INFORMATION")
        ruleJSON column: "RULES", sqlType: DbUtil.longStringType
        customSqlQuery column: 'SQL_QUERY', sqlType: DbUtil.stringType
    }

    static constraints = {
        ruleJSON nullable: false, blank: false
        ruleName nullable: false, blank: false, unique: true
        ruleRank nullable: true, blank: true
        action nullable: true, blank: true
        disposition nullable: true, blank: true
        customSqlQuery nullable: true, blank: true
        businessConfiguration nullable: true, blank: true
        format nullable: true, blank: true, maxSize: 4000
        justificationText nullable: true, blank: true, maxSize: 8000
        signal nullable: true, blank: true
        medicalConcepts nullable: true, blank: true
        tags nullable: true, blank: true , maxSize: 4000
    }

    def toDto(String timezone = "UTC" ) {

        [
            id                   : this.id,
            ruleName             : this.ruleName?.trim()?.replaceAll("\\s{2,}", " "),
            ruleRank             : this.ruleRank,
            lastModified         : DateUtil.toDateStringWithTimeInAmPmFormat(this.lastUpdated,timezone),
            modifiedBy           : User.findByUsername(this.modifiedBy)?.fullName,
            description          : '-',
            enabled              : this.enabled,
            isSingleCaseAlertType: this.isSingleCaseAlertType
        ]
    }

    Long getBusinessConfigurationId() {
        this.businessConfiguration.id
    }

    Boolean isDispositionValidatedConfirmed() {
        this.disposition?.isValidatedConfirmed()
    }

    @Override
    String toString() {
        this.ruleName
    }

    def getInstanceIdentifierForAuditLog() {
       return this.businessConfiguration.ruleName+" > "+this.ruleName
    }

}
