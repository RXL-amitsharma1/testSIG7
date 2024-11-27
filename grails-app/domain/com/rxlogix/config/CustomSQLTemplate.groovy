package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class CustomSQLTemplate extends ReportTemplate {

    def auditLogService

    String customSQLTemplateSelectFrom
    String customSQLTemplateWhere
    String columnNamesList

    static hasMany = [customSQLValues: CustomSQLValue]

    static mapping = {
        tablePerHierarchy false

        table name: "SQL_TEMPLT"
        customSQLValues joinTable: [name:"SQL_TEMPLTS_SQL_VALUES", column: "SQL_VALUE_ID", key:"SQL_TEMPLT_ID",], cascade: 'all-delete-orphan'

        customSQLTemplateSelectFrom column: "SELECT_FROM_STMT", sqlType: DbUtil.longStringType
        customSQLTemplateWhere column: "WHERE_STMT", sqlType: DbUtil.longStringType
        columnNamesList column: "COLUMN_NAMES"
    }

    static constraints = {
        customSQLTemplateSelectFrom(nullable: false, maxSize: 8192, validator: {val, obj ->
            if (obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("insert ")
                || obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("use ")
                || obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("alter ")
                || obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("desc ")
                || obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("create ")
                || obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("drop ")
                || obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("delete ")
                || obj?.customSQLTemplateSelectFrom?.toLowerCase()?.contains("update ")
                || obj?.customSQLTemplateSelectFrom?.contains(";"))
            return "com.rxlogix.config.query.customSQLQuery.invalid"
        })
        customSQLTemplateWhere(nullable: true, maxSize: 8192, validator: {val, obj ->
            if (obj?.customSQLTemplateWhere?.toLowerCase()?.contains("select ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("use ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("alter ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("desc ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("create ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("insert ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("drop ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("delete ")
                || obj?.customSQLTemplateWhere?.toLowerCase()?.contains("update ")
                || obj?.customSQLTemplateWhere?.contains(";"))
            return "com.rxlogix.config.query.customSQLQuery.invalid"
        })
        columnNamesList(nullable: false, maxSize: 2048)
        //TODO temp
        owner nullable: true
    }

    def detectChangesForAuditLog(theInstance, Map params, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = auditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        changesMade.flatten()
    }
}
