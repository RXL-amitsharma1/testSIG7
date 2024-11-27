package com.rxlogix.config

class ReportFieldInfo {

    ReportField reportField

    String argusName //= reportField.argusColumn.tableName.tableAlias + "." + reportField.argusColumn.columnName
    String renameValue
    String customExpression
    String datasheet
    String advancedSorting

    int stackId
    Integer sortLevel = 0
    Sort sort

    // For CLL
    boolean commaSeparatedValue = false
    boolean suppressRepeatingValues = false
    boolean blindedValue = false

    static belongsTo = [reportFieldInfoList: ReportFieldInfoList]

    static constraints = {
        reportField(nullable: false)
        argusName(nullable: false)
        renameValue(nullable: true)
        customExpression(nullable: true)
        datasheet(nullable: true)
        advancedSorting(nullable: true, maxSize: 2000)
        stackId(nullable: true)
        sortLevel(nullable: true)
        sort(nullable: true)
        commaSeparatedValue(nullable: false)
        suppressRepeatingValues(nullable: false)
        blindedValue(nullable: false)
    }

    static mapping = {
        table name: "RPT_FIELD_INFO"

        reportFieldInfoList column: "RF_INFO_LIST_ID"
        reportField column: "RPT_FIELD_ID"
        argusName column: "ARGUS_NAME"
        renameValue column: "RENAME_VALUE"
        customExpression column: "CUSTOM_EXPRESSION"
        datasheet column: "DATASHEET"
        advancedSorting column: "ADVANCED_SORTING", length: 2000
        stackId column: "STACK_ID"
        sortLevel column: "SORT_LEVEL"
        sort column: "SORT"
        commaSeparatedValue column: "COMMA_SEPARATED"
        suppressRepeatingValues column: "SUPPRESS_REPEATING"
        blindedValue column: "BLINDED"
    }
}
