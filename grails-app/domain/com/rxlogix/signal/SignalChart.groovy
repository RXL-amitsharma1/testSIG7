package com.rxlogix.signal

import com.rxlogix.util.DbUtil

class SignalChart {
    static auditable = true

    String chartName
    Long execId
    String chartData

    static belongsTo = [validatedSignal: ValidatedSignal, topic: Topic]

    static mapping= {
        table name: "SIGNAL_CHART"
        chartData column: "CHART_DATA", sqlType: DbUtil.longStringType
    }

    static constraints = {
        validatedSignal nullable: true
        topic nullable: true
    }

    def toDto() {
        [
           chartId: this.id,
           chartName: this.chartName,
           execId: this.execId,
           chartData: this.chartData
        ]
    }
}
