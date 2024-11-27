package com.rxlogix.config

class ReportFieldInfoList {

    List<ReportFieldInfo> reportFieldInfoList

    static hasMany = [reportFieldInfoList: ReportFieldInfo]
//    static belongsTo = [reportTemplate: ReportTemplate]

    static constraints = {
        reportFieldInfoList(nullable: true)
    }

    static mapping = {
        table name: "RPT_FIELD_INFO_LIST"
        reportFieldInfoList joinTable: [name:"RF_INFO_LISTS_RF_INFO", column:"RF_INFO_ID", key: "RF_INFO_LIST_ID"],
                indexColumn: [name:"RF_INFO_IDX"], cascade: 'all-delete-orphan'
    }
}
