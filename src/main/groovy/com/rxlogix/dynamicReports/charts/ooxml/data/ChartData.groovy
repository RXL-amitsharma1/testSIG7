package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.Chart
import com.rxlogix.dynamicReports.charts.ooxml.ChartAxis
import com.rxlogix.dynamicReports.charts.ooxml.ChartDataSource

/**
 * A base for all charts data types.
 */
public interface ChartData {

    ChartSeries addSeries(ChartDataSource<?> categoryAxisData, ChartDataSource<? extends Number> values)

    /**
     * Fills a charts with data specified by implementation.
     *
     * @param chart a charts to fill in
     * @param axis charts axis to use
     */
    void fillChart(Chart chart, ChartAxis... axis)
}
