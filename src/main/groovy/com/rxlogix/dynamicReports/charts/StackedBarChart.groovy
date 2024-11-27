package com.rxlogix.dynamicReports.charts

public class StackedBarChart extends AbstractChart {

    public StackedBarChart(def options) {
        this.options = options
        setReversedStacks(false)
    }
}
