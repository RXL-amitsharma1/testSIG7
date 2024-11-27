package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum

import java.text.SimpleDateFormat

class DataTabulationMeasure {
    static auditable = false
    MeasureTypeEnum type
    String name
    CountTypeEnum dateRangeCount
    Date customPeriodFrom
    Date customPeriodTo
    PercentageOptionEnum percentageOption

    String customExpression
    boolean showTotal = false

    // Grouping options
    boolean showSubtotalRowAfterGroups = false
    boolean showTotalRowOnly = false
    boolean showTotalAsColumn = false

//    static belongsTo = [dataTabulationTemplate: DataTabulationTemplate]

    static mapping = {
        table name: "DTAB_MEASURE"

//        dataTabulationTemplate column: "DTAB_TEMPLT_ID"

        type column: "MEASURE_TYPE"
        name column: "NAME"
        dateRangeCount columnn: "COUNT_TYPE" // for some reason its not getting renamed in MySQL
        customPeriodFrom column: "FROM_DATE"
        customPeriodTo column: "TO_DATE"
        percentageOption column: "PERCENTAGE"
        customExpression column: "CUSTOM_EXPRESSION"
        showTotal column: "SHOW_TOTAL"
        showSubtotalRowAfterGroups column: "SHOW_SUBTOTALS"
        showTotalRowOnly column: "SHOW_TOTAL_ROWS"
        showTotalAsColumn column: "SHOW_TOTAL_AS_COLS"
    }


    static constraints = {
        customPeriodFrom(nullable: true)
        customPeriodTo(nullable: true)
        customExpression(nullable: true)
    }

    String getNameI18nKey() {
        if (name) {
            return name
        } else {
            return type.getI18nKey()
        }
    }

    String getCustomPeriodFromWithTZ() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.WITH_TZ)
        return customPeriodFrom ? sdf.format(customPeriodFrom) : null
    }

    String getCustomPeriodToWithTZ() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.WITH_TZ)
        return customPeriodTo ? sdf.format(customPeriodTo) : null
    }


}
