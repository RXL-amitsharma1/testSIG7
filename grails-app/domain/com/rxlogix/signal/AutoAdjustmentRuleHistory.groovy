package com.rxlogix.signal

class AutoAdjustmentRuleHistory {

    Boolean isAdjustmentTypeEnumUpdated = false
    String oldAdjustmentTypeEnumValue
    String newAdjustmentTypeEnumValue

    Boolean isEnabledFlagUpdated = false
    Boolean newIsEnabledFlagValue

    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    static belongsTo = [autoAdjustmentRule: AutoAdjustmentRule]

    static mapping = {
        table name: "AUTO_ADJUSTMENT_HISTORY"
        isAdjustmentTypeEnumUpdated column: "adjustment_enum_updated"
        oldAdjustmentTypeEnumValue column: "old_adjustment_enum_value"
        newAdjustmentTypeEnumValue column: "new_adjustment_enum_value"
        newIsEnabledFlagValue column: "new_enabled_flag_value"
    }

    static constraints = {
        oldAdjustmentTypeEnumValue nullable: true
        newAdjustmentTypeEnumValue nullable: true
        newIsEnabledFlagValue nullable: true
    }
}
