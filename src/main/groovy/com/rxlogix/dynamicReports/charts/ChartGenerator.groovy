package com.rxlogix.dynamicReports.charts

public interface ChartGenerator {
    public static final String PARAMETER_CHART_GENERATOR = "CHART_GENERATOR"
    public static final String PARAMETER_CHART_ROWS_COUNT = "CHART_ROWS_COUNT"
    public static final String PARAMETER_TOTAL_ROW_INDICES = "TOTAL_ROW_INDICES"

    def generateChart()
}
