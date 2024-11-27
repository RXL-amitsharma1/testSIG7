package com.rxlogix.dynamicReports.charts.ooxml

import com.rxlogix.dynamicReports.charts.ooxml.enums.LegendPosition
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegendPos
import org.openxmlformats.schemas.drawingml.x2006.chart.STLegendPos

/**
 * Represents a SpreadsheetML chart legend
 */
public final class ChartLegend {

    /**
     * Underlaying CTLagend bean
     */
    private CTLegend legend

    /**
     * Chart options
     */
    private def options

    /**
     * Create a new SpreadsheetML chart legend
     */
    public ChartLegend(Chart chart) {
        CTChart ctChart = chart.getCTChart()
        this.legend = (ctChart.isSetLegend()) ?
                ctChart.getLegend() :
                ctChart.addNewLegend()
        this.options = chart.options
        setDefaults()
    }

    /**
     * Set sensible default styling.
     */
    private void setDefaults() {
        if (!legend.isSetOverlay()) {
            legend.addNewOverlay()
        }
        legend.addNewLayout()
        legend.getOverlay().setVal(Boolean.valueOf(options.legend?.floating))
        switch(options.legend?.verticalAlign) {
            case "top" :
                if (options.legend?.align == "right") {
                    setPosition(LegendPosition.TOP_RIGHT)
                } else {
                    setPosition(LegendPosition.TOP)
                }
                break
            case "bottom" :
            default:
                setPosition(LegendPosition.BOTTOM)
                break
        }
        switch(options.legend?.align) {
            case "left" :
                setPosition(LegendPosition.LEFT)
                break
            case "right" :
                setPosition(LegendPosition.RIGHT)
                break
        }
    }

    /**
     * Return the underlying CTLegend bean.
     *
     * @return the underlying CTLegend bean
     */
    public CTLegend getCTLegend() {
        return legend
    }

    public void setPosition(LegendPosition position) {
        if (!legend.isSetLegendPos()) {
            legend.addNewLegendPos()
        }
        legend.getLegendPos().setVal(fromLegendPosition(position))
    }

    /*
     * According to ECMA-376 default position is RIGHT.
     */
    public LegendPosition getPosition() {
        if (legend.isSetLegendPos()) {
            return toLegendPosition(legend.getLegendPos())
        } else {
            return LegendPosition.RIGHT
        }
    }

    public ManualLayout getManualLayout() {
        if (!legend.isSetLayout()) {
            legend.addNewLayout()
        }
        return new ManualLayout(legend.getLayout())
    }

    public boolean isOverlay() {
        return legend.getOverlay().getVal()
    }

    public void setOverlay(boolean value) {
        legend.getOverlay().setVal(value)
    }

    private STLegendPos.Enum fromLegendPosition(LegendPosition position) {
        switch (position) {
            case LegendPosition.BOTTOM: return STLegendPos.B
            case LegendPosition.LEFT: return STLegendPos.L
            case LegendPosition.RIGHT: return STLegendPos.R
            case LegendPosition.TOP: return STLegendPos.T
            case LegendPosition.TOP_RIGHT: return STLegendPos.TR
            default:
                throw new IllegalArgumentException()
        }
    }

    private LegendPosition toLegendPosition(CTLegendPos ctLegendPos) {
        switch (ctLegendPos.getVal().intValue()) {
            case STLegendPos.INT_B: return LegendPosition.BOTTOM
            case STLegendPos.INT_L: return LegendPosition.LEFT
            case STLegendPos.INT_R: return LegendPosition.RIGHT
            case STLegendPos.INT_T: return LegendPosition.TOP
            case STLegendPos.INT_TR: return LegendPosition.TOP_RIGHT
            default:
                throw new IllegalArgumentException()
        }
    }
}
