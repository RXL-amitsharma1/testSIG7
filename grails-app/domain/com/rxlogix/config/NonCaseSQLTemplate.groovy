package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class NonCaseSQLTemplate extends ReportTemplate {
    static auditable = false

    def auditLogService

    String nonCaseSql
    String columnNamesList

    static hasMany = [customSQLValues: CustomSQLValue]

    static mapping = {
        tablePerHierarchy false
        customSQLValues joinTable: [name:"NONCASE_SQL_TEMPLTS_SQL_VALUES", column: "SQL_VALUE_ID", key:"NONCASE_SQL_TEMPLT_ID"], cascade: 'all-delete-orphan'

        table name: "NONCASE_SQL_TEMPLT"
        nonCaseSql sqlType: DbUtil.longStringType, column: "NON_CASE_SQL"
        columnNamesList column: "COL_NAME_LIST"
    }

    static constraints = {
        nonCaseSql(nullable: false, maxSize: 8192, validator: {val, obj ->
            if (obj?.nonCaseSql?.toLowerCase()?.contains("insert ")
                    || obj?.nonCaseSql?.toLowerCase()?.contains("use ")
                    || obj?.nonCaseSql?.toLowerCase()?.contains("alter ")
                    || obj?.nonCaseSql?.toLowerCase()?.contains("desc ")
                    || obj?.nonCaseSql?.toLowerCase()?.contains("create ")
                    || obj?.nonCaseSql?.toLowerCase()?.contains("drop ")
                    || obj?.nonCaseSql?.toLowerCase()?.contains("delete ")
                    || obj?.nonCaseSql?.toLowerCase()?.contains("update ")
                    || obj?.nonCaseSql?.contains(";"))
                return "com.rxlogix.config.query.customSQLQuery.invalid"
        })
        columnNamesList(nullable: false, maxSize: 2048)
    }

    def detectChangesForAuditLog(theInstance, Map params, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = auditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        changesMade.flatten()
    }
}
