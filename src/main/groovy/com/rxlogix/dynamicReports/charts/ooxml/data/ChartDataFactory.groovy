package com.rxlogix.dynamicReports.charts.ooxml.data

public class ChartDataFactory {

    private static ChartDataFactory instance;

    private ChartDataFactory() { }

    public ChartData createChartData(def options) {
        def chartType = options.chart.type
        switch (chartType) {
            // Supported chart types
            case "area":
            case "arearange":
            case "areaspline":
                return new AreaChartData(options)
            case "bar":
            case "column":
            case "columnrange":
                if (options.chart.options3d?.enabled) {
                    return new Bar3DChartData(options)
                } else {
                    return new BarChartData(options)
                }
            case "line":
            case "spline":
                return new LineChartData(options)
            case "pie":
                if (options.plotOptions.pie?.innerSize) {
                    return new DoughnutChartData(options)
                } else if (options.chart.options3d?.enabled) {
                    return new Pie3DChartData(options)
                } else {
                    return new PieChartData(options)
                }
            case "bubble":
                return new BubbleChartData(options)
            case "scatter":
                return new ScatterChartData(options)
        // Not supported chart types
            case "boxplot":
            case "errorbar":
            case "funnel":
            case "gauge":
            case "heatmap":
            case "polygon":
            case "pyramid":
            case "series":
            case "solidgauge":
            case "treemap":
            case "waterfall":
                break
        }
        return new BarChartData(options)
    }

    /**
     * @return factory instance
     */
    public static ChartDataFactory getInstance() {
        if (instance == null) {
            instance = new ChartDataFactory();
        }
        return instance;
    }
}
