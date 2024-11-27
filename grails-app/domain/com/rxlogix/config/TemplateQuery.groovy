package com.rxlogix.config
import com.rxlogix.BaseTemplateQuery
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.SectionModuleAudit

@DirtyCheck
@SectionModuleAudit(parentClassName = ['configuration'])
class TemplateQuery extends BaseTemplateQuery {
    static auditable = [ignore:['query', 'template','report','queryValueLists','index']]
    def queryService
    Long template
    Long query
    String templateName
    String queryName
    DateRangeInformation dateRangeInformationForTemplateQuery  = new DateRangeInformation()
    // String for auditing key values of report fields
    String parameterValueAuditString
    //For the one-to-many dynamic form
    int index
    boolean dynamicFormEntryDeleted

    static transients = ['dynamicFormEntryDeleted']

    static belongsTo = [report: Configuration]

    static hasMany = [results: ReportResult, queryValueLists: QueryValueList, templateValueLists: TemplateValueList]

    static mapping = {
        table name: "TEMPLT_QUERY"

        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseTemplateQuery.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()

        results joinTable: [name: "RPT_RESULT", column: "ID", key: "TEMPLT_QUERY_ID"]
        queryValueLists joinTable: [name:"TEMPLT_QRS_QUERY_VALUES", column: "QUERY_VALUE_ID", key:"TEMPLT_QUERY_ID"]
        templateValueLists joinTable: [name:"TEMPLT_QRS_TEMPLT_VALUES", column: "TEMPLT_VALUE_ID", key:"TEMPLT_QUERY_ID"]

        template column: "RPT_TEMPLT_ID"
        query column: "SUPER_QUERY_ID"
        report column: "RCONFIG_ID"

        dateRangeInformationForTemplateQuery column: "DATE_RANGE_ID"
        index column:"INDX"
    }

    static constraints = {
        template nullable: false
        query(nullable:true)
        results(nullable:true)
        templateName nullable: true
        queryName nullable: true
        dateRangeInformationForTemplateQuery(nullable: false)
        dynamicFormEntryDeleted(bindable: true)
        queryValueLists (cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.TemplateQuery.parameterValues.valueless"
            }
            return hasValues
        })
        templateValueLists (cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.TemplateQuery.parameterValues.valueless"
            }
            return hasValues
        })
        parameterValueAuditString(nullable: true)
    }

    String getQueriesIdsAsString() {
        queryService.getQueriesIdsAsString(query)
    }

    @Override
    String toString() {
        "$queryName"
    }

    def getInstanceIdentifierForAuditLog() {
        return this.report.getInstanceIdentifierForAuditLog()
    }

}
