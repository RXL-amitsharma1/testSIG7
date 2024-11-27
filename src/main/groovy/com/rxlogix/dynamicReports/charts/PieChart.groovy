package com.rxlogix.dynamicReports.charts

public class PieChart extends AbstractChart {

    public PieChart(def options) {
        this.options = options
        options.chart.type = "pie"
        if (!options.legend.layout) {
            options.legend.layout = "vertical"
        }
        if (!options.legend.align) {
            options.legend.align = "right"
        }
        if (!options.legend.verticalAlign) {
            options.legend.verticalAlign = "middle"
        }
        options.legend.borderWidth = 0
    }
}