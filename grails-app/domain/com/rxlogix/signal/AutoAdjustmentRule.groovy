package com.rxlogix.signal

import com.rxlogix.enums.AdjustmentTypeEnum
import grails.plugins.orm.auditable.SectionModuleAudit

class AutoAdjustmentRule {

    static auditable = false

    String alertType

    AdjustmentTypeEnum adjustmentTypeEnum = AdjustmentTypeEnum.ALERT_PER_SKIPPED_EXECUTION
    Boolean isEnabled = true

    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    static hasMany = [autoAdjustmentRuleHistories: AutoAdjustmentRuleHistory]

    static mapping = {
        table name: "AUTO_ADJUSTMENT_RULE"
    }
    static constraints = {}

    AutoAdjustmentRule(String alertType, String createdBy, String modifiedBy) {
        this.alertType = alertType
        this.createdBy = createdBy
        this.modifiedBy = modifiedBy
    }

    def toDto() {
        ['alertType'         : this.alertType,
         'isEnabled'         : this.isEnabled,
         'adjustmentTypeEnum': this.adjustmentTypeEnum]
    }
}
