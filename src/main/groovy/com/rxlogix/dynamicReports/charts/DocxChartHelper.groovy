package com.rxlogix.dynamicReports.charts

import com.rxlogix.dynamicReports.charts.ooxml.Chart
import com.rxlogix.dynamicReports.charts.ooxml.data.CategoryDataSource
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartData
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartSeries
import com.rxlogix.dynamicReports.charts.ooxml.data.ValueDataSource
import net.sf.jasperreports.engine.export.ooxml.BaseHelper
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
/**
 * Created by gologuzov on 04.02.16.
 */
class DocxChartHelper extends BaseHelper {
    private def options
    private int chartRowsCount
    private JRDocxExporter exporter

    private Chart chart

    DocxChartHelper(JRDocxExporter exporter, Writer writer, def options, int chartRowsCount) {
        super(exporter.jasperReportsContext, writer)
        this.exporter = exporter
        this.options = options
        this.chartRowsCount = chartRowsCount ? chartRowsCount : 1

        createChart()
    }

    private void createChart() {
        chart = new Chart(options)
        ChartData data = chart.getChartDataFactory().createChartData(options)
        for (def series : options.series) {
            ChartSeries chartSeries = data.addSeries(new CategoryDataSource(options.xAxis[0].categories), new ValueDataSource(series.data))
            chartSeries.setTitle(series.name)
        }
        chart.plot(data)
    }

    public void exportChart() {
        this.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        this.write(chart.toString())
        this.flush()
    }
}
