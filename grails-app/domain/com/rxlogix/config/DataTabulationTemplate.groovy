package com.rxlogix.config
import com.rxlogix.Constants
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import grails.gorm.dirty.checking.DirtyCheck
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat

@DirtyCheck
class DataTabulationTemplate extends ReportTemplate {
    static auditable = false

    def auditLogService
    boolean showTotalIntervalCases = false
    boolean showTotalCumulativeCases = false
    List<DataTabulationColumnMeasure> columnMeasureList
    ReportFieldInfoList rowList

    static hasMany = [columnMeasureList: DataTabulationColumnMeasure]

    static mapping = {
        tablePerHierarchy false
        table name: "DTAB_TEMPLT"

        showTotalIntervalCases column: "SHOW_TOTAL_INTERVAL_CASES"
        showTotalCumulativeCases column: "SHOW_TOTAL_CUMULATIVE_CASES"

        columnMeasureList joinTable: [name: "DTAB_TEMPLTS_COL_MEAS", column: "COLUMN_MEASURE_ID", key: "DTAB_TEMPLT_ID"], indexColumn: [name:"COLUMN_MEASURE_IDX"]
        rowList column: "ROWS_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
    }

    static constraints = {
        columnMeasureList(minSize: 1, validator: {val ->
            boolean hasMeasure = true
            val.each { colMeas ->
                if (!colMeas.measures || colMeas.measures.size() == 0) {
                    hasMeasure = false
                }
            }
            if (!hasMeasure) {
                return "com.rxlogix.config.DataTabulationTemplate.measures.null"
            }
        })

        rowList(nullable: false, validator: {val ->
            if (val?.reportFieldInfoList?.size() > 5) {
                return "com.rxlogix.config.DataTabulationTemplate.rowList.exceedMax"
            }
        })
    }

    List<ReportFieldInfo> getAllSelectedFieldsInfo() {
        List allColumns = []
        columnMeasureList.each {
            if (it.columnList) {
                allColumns.add(it.columnList.reportFieldInfoList)
            }
        }
        if (allColumns) {
            return allColumns.flatten() + rowList.reportFieldInfoList
        } else {
            return rowList.reportFieldInfoList
        }
    }

    boolean hasCumulativeOrCustomPeriod() {
        boolean hasCumulativeOrCustomPeriod = false
        columnMeasureList.each {
            it.measures.each {
                if (it.dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT || it.dateRangeCount == CountTypeEnum.CUSTOM_PERIOD_COUNT) {
                    hasCumulativeOrCustomPeriod = true
                }
            }
        }
        return hasCumulativeOrCustomPeriod
    }

    String getJSONStringMeasures() {
        JSONArray measureList = new JSONArray()
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.WITH_TZ)

        columnMeasureList.each { colMeas ->
            JSONArray JSONMeasures = new JSONArray()
            if (colMeas.measures) {
                colMeas.measures.each {
                    JSONObject measure = new JSONObject([name: it.name, type: it.type.name(), count: it.dateRangeCount.name(),
                                                         percentage: it.percentageOption.name(), showTotal: it.showTotal,
                                                         customExpression: it.customExpression?:""])
                    // add date range info
                    measure.customPeriodFrom = it.customPeriodFrom ? sdf.format(it.customPeriodFrom) : null
                    measure.customPeriodTo = it.customPeriodTo ? sdf.format(it.customPeriodTo) : null
                    JSONMeasures.add(measure)
                }
                measureList.add(JSONMeasures)
            }
        }

        return measureList.toString()
    }

    def detectChangesForAuditLog(theInstance, Map params, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = auditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        changesMade.flatten()
    }

}
