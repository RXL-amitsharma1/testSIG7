package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.*
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties

/**
 * Holds data for a Line Chart
 */
public class LineChartData implements ChartData {
    /**
     * Chart options
     */
    private def options

    /**
     * List of all data series.
     */
    private List<Series> series
    private ChartColorTheme colorTheme

    public LineChartData(def options) {
        this.options = options
        series = new ArrayList<Series>()
        colorTheme = new ChartColorTheme(options.colors)
    }

    class Series extends AbstractChartSeries {
        protected Series(int id, int order,
                         ChartDataSource<?> categories,
                         ChartDataSource<? extends Number> values,
                         ChartColorTheme colorTheme) {
            super(id, order, categories, values, colorTheme)
        }

        protected void addToChart(CTLineChart ctLineChart) {
            CTLineSer ctLineSer = ctLineChart.addNewSer()
            ctLineSer.addNewIdx().setVal(id)
            ctLineSer.addNewOrder().setVal(order)

            CTAxDataSource catDS = ctLineSer.addNewCat()
            ChartUtil.buildAxDataSource(catDS, categories)
            CTNumDataSource valueDS = ctLineSer.addNewVal()
            ChartUtil.buildNumDataSource(valueDS, values)

            if (isTitleSet()) {
                ctLineSer.setTx(getCTSerTx())
            }
            CTShapeProperties shapeProperties = ctLineSer.addNewSpPr()
            CTLineProperties lineProperties = shapeProperties.addNewLn()
            lineProperties.addNewSolidFill().addNewSrgbClr().setVal(colorTheme.nextColorBytes())

            ctLineSer.addNewSmooth().setVal(options.chart.type == "spline")
        }
    }

    public ChartSeries addSeries(ChartDataSource<?> categoryAxisData, ChartDataSource<? extends Number> values) {
        if (!values.isNumeric()) {
            throw new IllegalArgumentException("Value data source must be numeric.")
        }
        int numOfSeries = series.size()
        Series newSeries = new Series(numOfSeries, numOfSeries, categoryAxisData, values, colorTheme)
        series.add(newSeries)
        return newSeries
    }

    public List<? extends ChartSeries> getSeries() {
        return series
    }

    public void fillChart(Chart chart, ChartAxis... axis) {
        CTPlotArea plotArea = chart.getCTChart().getPlotArea()
        CTLineChart lineChart = plotArea.addNewLineChart()

        def stacking = options.plotOptions.series.stacking
        CTGrouping barGrouping = lineChart.addNewGrouping()
        switch (stacking) {
            case "percent":
                barGrouping.setVal(STGrouping.PERCENT_STACKED)
                break
            case "normal":
                barGrouping.setVal(STGrouping.STACKED)
                break
            default:
                barGrouping.setVal(STGrouping.STANDARD)
                break
        }

        lineChart.addNewVaryColors().setVal(false)

        for (Series s : series) {
            s.addToChart(lineChart)
        }

        CTDLbls dataLabels = lineChart.addNewDLbls()
        dataLabels.addNewShowLegendKey().setVal(false)
        dataLabels.addNewShowVal().setVal(options.plotOptions?.series?.dataLabels?.enabled as boolean)
        dataLabels.addNewShowCatName().setVal(false)
        dataLabels.addNewShowSerName().setVal(false)
        dataLabels.addNewShowPercent().setVal(false)
        dataLabels.addNewShowBubbleSize().setVal(false)

        for (ChartAxis ax : axis) {
            lineChart.addNewAxId().setVal(ax.getId())
        }

        lineChart.addNewSmooth().setVal(options.chart.type == "spline")
        CTMarker marker = lineChart.addNewMarker().setVal(true)
    }
}
