package com.rxlogix.dynamicReports.charts.ooxml

import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle

import java.awt.Color

/**
 * Created by gologuzov on 21.09.16.
 */
class ChartColorTheme {
    public static final List<String> DEFAULT_SERIES_COLORS = ["#7cb5ec", "#434348", "#90ed7d", "#f7a35c", "#8085e9", "#f15c80", "#e4d354", "#2b908f", "#f45b5b", "#91e8e1"]
    public static final List<STMarkerStyle.Enum> DEFAULT_SERIES_MARKERS = [STMarkerStyle.CIRCLE, STMarkerStyle.DIAMOND, STMarkerStyle.SQUARE, STMarkerStyle.TRIANGLE, STMarkerStyle.INT_TRIANGLE]
    public static final String DEFAULT_CHART_BACKGROUND_COLOR = "#ffffff"
    public static final String DEFAULT_CHART_BORDER_COLOR = "#4572a7"
    public static final String DEFAULT_PLOT_BORDER_COLOR = "#c0c0c0"
    public static final Integer DEFAULT_FILL_ALPHA = 75000 //75%
    public static final Short DEFAULT_BUBBLE_SIZE = 30

    private def colorsIterator
    private def markersIterator

    private Iterator iterator(def objects) {
        int index = 0
        return [
                hasNext: { true },
                next   : {
                    if (index >= objects.size()) {
                        index = 0
                    }
                    return objects[index++]
                }] as Iterator
    }

    ChartColorTheme(def colors) {
        if (!colors) {
            colors = DEFAULT_SERIES_COLORS
        }
        colorsIterator = iterator(colors.collect { Color.decode(it) })
        markersIterator = iterator(DEFAULT_SERIES_MARKERS)
    }

    Color nextColor() {
        return colorsIterator.next()
    }

    byte[] nextColorBytes() {
        Color color = colorsIterator.next()
        return  [(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()].toArray()
    }

    STMarkerStyle.Enum nextMarker() {
        return markersIterator.next()
    }
}
