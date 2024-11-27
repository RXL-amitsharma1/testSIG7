package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.reportTemplate.CountTypeEnum
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat

class DataTabulationColumnMeasure {
    static auditable = false

    ReportFieldInfoList columnList
    List<DataTabulationMeasure> measures
    boolean showTotalIntervalCases = false
    boolean showTotalCumulativeCases = false

    static hasMany = [measures: DataTabulationMeasure]

    static mapping = {
        tablePerHierarchy false
        table name: "DTAB_COLUMN_MEASURE"

        columnList column: "COLUMNS_RFI_LIST_ID", cascade: 'all-delete-orphan'
        measures joinTable: [name: "DTAB_COL_MEAS_MEASURES", column: "MEASURE_ID", key: "DTAB_COL_MEAS_ID"], indexColumn: [name:"MEASURES_IDX"]
        showTotalIntervalCases column: "SHOW_TOTAL_INTERVAL_CASES"
        showTotalCumulativeCases column: "SHOW_TOTAL_CUMULATIVE_CASES"
    }

    static constraints = {
        measures(nullable: true) // Add custom validation in DataTabulationTemplate

        columnList(nullable: true, validator: {val ->
            if (val?.reportFieldInfoList?.size() > 5) {
                return "com.rxlogix.config.DataTabulationTemplate.columnList.exceedMax"
            }
        })
    }

    boolean hasCumulativeOrCustomPeriod() {
        boolean hasCumulativeOrCustomPeriod = false
        measures.each {
            if (it.dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT || it.dateRangeCount == CountTypeEnum.CUSTOM_PERIOD_COUNT) {
                hasCumulativeOrCustomPeriod = true
            }
        }
        return hasCumulativeOrCustomPeriod
    }

    String getJSONStringMeasures() {
        JSONArray JSONMeasures = new JSONArray()
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.WITH_TZ)
        if (measures) {
            measures.each {
                JSONObject measure = new JSONObject([name: it.name, type: it.type.name(), count: it.dateRangeCount.name(),
                                                     percentage: it.percentageOption.name(), showTotal: it.showTotal,
                                                     customExpression: it.customExpression?:""])
                // add date range info
                measure.customPeriodFrom = it.customPeriodFrom ? sdf.format(it.customPeriodFrom) : null
                measure.customPeriodTo = it.customPeriodTo ? sdf.format(it.customPeriodTo) : null
                JSONMeasures.add(measure)
            }
        }
        return JSONMeasures.toString()
    }
}
