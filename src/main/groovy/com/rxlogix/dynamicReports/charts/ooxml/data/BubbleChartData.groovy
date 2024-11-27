package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.*
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties

/**
 * Holds data for a Line Chart
 */
class BubbleChartData implements ChartData {
    /**
     * Chart options
     */
    private def options

    /**
     * List of all data series.
     */
    private List<Series> series
    private ChartColorTheme colorTheme

    BubbleChartData(def options) {
        this.options = options
        series = new ArrayList<Series>()
        colorTheme = new ChartColorTheme(options.colors)
    }

    static class Series extends AbstractChartSeries {
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
            CTMarker marker = ctLineSer.addNewMarker()
            marker.addNewSymbol().setVal(STMarkerStyle.CIRCLE)
            marker.addNewSize().setVal(ChartColorTheme.DEFAULT_BUBBLE_SIZE)

            CTAxDataSource catDS = ctLineSer.addNewCat()
            ChartUtil.buildAxDataSource(catDS, categories)
            CTNumDataSource valueDS = ctLineSer.addNewVal()
            ChartUtil.buildNumDataSource(valueDS, values)

            if (isTitleSet()) {
                ctLineSer.setTx(getCTSerTx())
            }
            CTShapeProperties shapeProperties = ctLineSer.addNewSpPr()
            CTLineProperties lineProperties = shapeProperties.addNewLn()
            lineProperties.addNewNoFill()

            CTShapeProperties markerProperties = marker.addNewSpPr()
            CTSRgbColor fillColor = markerProperties.addNewSolidFill().addNewSrgbClr()
            fillColor.setVal(colorTheme.nextColorBytes())
            fillColor.addNewAlpha().setVal(ChartColorTheme.DEFAULT_FILL_ALPHA)
        }
    }

    ChartSeries addSeries(ChartDataSource<?> categoryAxisData, ChartDataSource<? extends Number> values) {
        if (!values.isNumeric()) {
            throw new IllegalArgumentException("Value data source must be numeric.")
        }
        int numOfSeries = series.size()
        Series newSeries = new Series(numOfSeries, numOfSeries, categoryAxisData, values, colorTheme)
        series.add(newSeries)
        return newSeries
    }

    List<? extends ChartSeries> getSeries() {
        return series
    }

    void fillChart(Chart chart, ChartAxis... axis) {
        CTPlotArea plotArea = chart.getCTChart().getPlotArea()
        CTLineChart lineChart = plotArea.addNewLineChart()

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
        dataLabels.addNewShowBubbleSize().setVal(true)

        for (ChartAxis ax : axis) {
            lineChart.addNewAxId().setVal(ax.getId())
        }
    }
}