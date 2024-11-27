package com.rxlogix.config

import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck
import groovy.json.JsonSlurper

@DirtyCheck
class CaseLineListingTemplate extends ReportTemplate {

    static CLL_TEMPLATE_REPORT_FIELD_NAME = "masterCaseNum"
    static CLL_TEMPLATE_J_REPORT_FIELD_NAME = "masterCaseNumJ"

    boolean pageBreakByGroup = false
    boolean columnShowTotal = false
    String suppressRepeatingValuesColumnList
    boolean columnShowDistinct = false
    String renamedGrouping
    String renamedRowCols
    String JSONQuery

    ReportFieldInfoList columnList
    ReportFieldInfoList groupingList
    ReportFieldInfoList rowColumnList

    static mapping = {
        tablePerHierarchy false
        table name: "CLL_TEMPLT"
        id column: "ID"

        pageBreakByGroup column: "PAGE_BREAK_BY_GROUP"
        columnShowTotal column: "COL_SHOW_TOTAL"
        suppressRepeatingValuesColumnList column: "SUPPRESS_COLUMN_LIST"
        renamedGrouping column: "RENAME_GROUPING", sqlType: DbUtil.longStringType
        renamedRowCols column: "RENAME_ROW_COLS", sqlType: DbUtil.longStringType
        columnShowDistinct column: "COL_SHOW_DISTINCT"
        'JSONQuery' column: "QUERY", sqlType: DbUtil.longStringType
        columnList column: "COLUMNS_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        groupingList column: "GROUPING_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        rowColumnList column: "ROW_COLS_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
    }

    static constraints = {
        suppressRepeatingValuesColumnList(nullable: true)
        renamedGrouping(nullable: true, maxSize: 4000)
        renamedRowCols(nullable: true, maxSize: 4000)
        JSONQuery(nullable: true, maxSize: 8388608, validator: { val, obj ->
            if (obj.JSONQuery) {
                def jsonSlurper = new JsonSlurper()
                def queryCriteria = jsonSlurper.parseText(obj?.JSONQuery)
                if (!queryCriteria?.all?.containerGroups?.expressions || queryCriteria?.all?.containerGroups?.expressions[0]?.size() == 0) {
                    return "com.rxlogix.config.CLL.JSONQuery.required"
                }
            }
        })
        columnList(nullable: false)
        groupingList(nullable: true)
        rowColumnList(nullable: true)
        owner(nullable: true)
    }

    List<ReportFieldInfo> getAllSelectedFieldsInfo() {
        if (groupingList && rowColumnList) {
            return columnList.reportFieldInfoList + groupingList.reportFieldInfoList + rowColumnList?.reportFieldInfoList
        } else if (groupingList) {
            return columnList.reportFieldInfoList + groupingList.reportFieldInfoList
        } else if (rowColumnList) {
            return columnList.reportFieldInfoList + rowColumnList.reportFieldInfoList
        }
        return columnList.reportFieldInfoList
    }

    List<String> getFieldNameWithIndex() {
        List<String> list = []
        List<ReportField> allFields = getAllSelectedFieldsInfo().reportField
        allFields.eachWithIndex{ it, index  ->
            list.add(it.name + "_" + index)
        }
        return list
    }

    boolean hasStackedColumns() {
        List <ReportFieldInfo> selectedColumns = columnList.reportFieldInfoList
        for (ReportFieldInfo reportFieldInfo : selectedColumns) {
            if (reportFieldInfo.stackId > 0) {
                return true
            }
        }
        return false
    }
}
