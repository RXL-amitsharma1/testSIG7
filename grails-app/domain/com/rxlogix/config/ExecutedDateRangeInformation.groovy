package com.rxlogix.config

import com.rxlogix.BaseDateRangeInformation
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ExecutedDateRangeInformation extends BaseDateRangeInformation {
    static auditable = false
    Date executedAsOfVersionDate

    static belongsTo = [executedTemplateQuery:ExecutedTemplateQuery]

    static mapping = {
        table name: "EX_DATE_RANGE"

        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseDateRangeInformation.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()

        executedAsOfVersionDate column: "EXECUTED_AS_OF"
    }

    static constraints = {
        executedAsOfVersionDate (nullable: false)
    }

    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }

}
