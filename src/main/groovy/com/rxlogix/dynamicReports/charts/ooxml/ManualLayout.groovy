package com.rxlogix.dynamicReports.charts.ooxml

import com.rxlogix.dynamicReports.charts.ooxml.enums.LayoutMode
import com.rxlogix.dynamicReports.charts.ooxml.enums.LayoutTarget;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

/**
 * Represents a SpreadsheetML manual layout.
 */
public final class ManualLayout {

    /**
     * Underlaying CTManualLayout bean.
     */
    private CTManualLayout layout;

    private static final LayoutMode defaultLayoutMode = LayoutMode.EDGE;
    private static final LayoutTarget defaultLayoutTarget = LayoutTarget.INNER;

    /**
     * Create a new SpreadsheetML manual layout.
     * @param ctLayout a Spreadsheet ML layout that should be used as base.
     */
    public ManualLayout(CTLayout ctLayout) {
        initLayout(ctLayout);
    }

    /**
     * Create a new SpreadsheetML manual layout for chart.
     * @param chart a chart to create layout for.
     */
    public ManualLayout(Chart chart) {
        CTPlotArea ctPlotArea = chart.getCTChart().getPlotArea();
        CTLayout ctLayout = ctPlotArea.isSetLayout() ?
                ctPlotArea.getLayout() : ctPlotArea.addNewLayout();

        initLayout(ctLayout);
    }

    /**
     * Return the underlying CTManualLayout bean.
     *
     * @return the underlying CTManualLayout bean.
     */
    public CTManualLayout getCTManualLayout() {
        return layout;
    }

    public void setWidthRatio(double ratio) {
        if (!layout.isSetW()) {
            layout.addNewW();
        }
        layout.getW().setVal(ratio);
    }

    public double getWidthRatio() {
        if (!layout.isSetW()) {
            return 0.0;
        }
        return layout.getW().getVal();
    }

    public void setHeightRatio(double ratio) {
        if (!layout.isSetH()) {
            layout.addNewH();
        }
        layout.getH().setVal(ratio);
    }

    public double getHeightRatio() {
        if (!layout.isSetH()) {
            return 0.0;
        }
        return layout.getH().getVal();
    }

    public LayoutTarget getTarget() {
        if (!layout.isSetLayoutTarget()) {
            return defaultLayoutTarget;
        }
        return toLayoutTarget(layout.getLayoutTarget());
    }

    public void setTarget(LayoutTarget target) {
        if (!layout.isSetLayoutTarget()) {
            layout.addNewLayoutTarget();
        }
        layout.getLayoutTarget().setVal(fromLayoutTarget(target));
    }

    public LayoutMode getXMode() {
        if (!layout.isSetXMode()) {
            return defaultLayoutMode;
        }
        return toLayoutMode(layout.getXMode());
    }

    public void setXMode(LayoutMode mode) {
        if (!layout.isSetXMode()) {
            layout.addNewXMode();
        }
        layout.getXMode().setVal(fromLayoutMode(mode));
    }

    public LayoutMode getYMode() {
        if (!layout.isSetYMode()) {
            return defaultLayoutMode;
        }
        return toLayoutMode(layout.getYMode());
    }

    public void setYMode(LayoutMode mode) {
        if (!layout.isSetYMode()) {
            layout.addNewYMode();
        }
        layout.getYMode().setVal(fromLayoutMode(mode));
    }

    public double getX() {
        if (!layout.isSetX()) {
            return 0.0;
        }
        return layout.getX().getVal();
    }

    public void setX(double x) {
        if (!layout.isSetX()) {
            layout.addNewX();
        }
        layout.getX().setVal(x);
    }

    public double getY() {
        if (!layout.isSetY()) {
            return 0.0;
        }
        return layout.getY().getVal();
    }

    public void setY(double y) {
        if (!layout.isSetY()) {
            layout.addNewY();
        }
        layout.getY().setVal(y);
    }

    public LayoutMode getWidthMode() {
        if (!layout.isSetWMode()) {
            return defaultLayoutMode;
        }
        return toLayoutMode(layout.getWMode());
    }

    public void setWidthMode(LayoutMode mode) {
        if (!layout.isSetWMode()) {
            layout.addNewWMode();
        }
        layout.getWMode().setVal(fromLayoutMode(mode));
    }

    public LayoutMode getHeightMode() {
        if (!layout.isSetHMode()) {
            return defaultLayoutMode;
        }
        return toLayoutMode(layout.getHMode());
    }

    public void setHeightMode(LayoutMode mode) {
        if (!layout.isSetHMode()) {
            layout.addNewHMode();
        }
        layout.getHMode().setVal(fromLayoutMode(mode));
    }

    private void initLayout(CTLayout ctLayout) {
        if (ctLayout.isSetManualLayout()) {
            this.layout = ctLayout.getManualLayout();
        } else {
            this.layout = ctLayout.addNewManualLayout();
        }
    }

    private STLayoutMode.Enum fromLayoutMode(LayoutMode mode) {
        switch (mode) {
            case LayoutMode.EDGE: return STLayoutMode.EDGE;
            case LayoutMode.FACTOR: return STLayoutMode.FACTOR;
            default:
                throw new IllegalArgumentException();
        }
    }

    private LayoutMode toLayoutMode(CTLayoutMode ctLayoutMode) {
        switch (ctLayoutMode.getVal().intValue()) {
            case STLayoutMode.INT_EDGE: return LayoutMode.EDGE;
            case STLayoutMode.INT_FACTOR: return LayoutMode.FACTOR;
            default:
                throw new IllegalArgumentException();
        }
    }

    private STLayoutTarget.Enum fromLayoutTarget(LayoutTarget target) {
        switch (target) {
            case LayoutTarget.INNER: return STLayoutTarget.INNER;
            case LayoutTarget.OUTER: return STLayoutTarget.OUTER;
            default:
                throw new IllegalArgumentException();
        }
    }

    private LayoutTarget toLayoutTarget(CTLayoutTarget ctLayoutTarget) {
        switch (ctLayoutTarget.getVal().intValue()) {
            case STLayoutTarget.INT_INNER: return LayoutTarget.INNER;
            case STLayoutTarget.INT_OUTER: return LayoutTarget.OUTER;
            default:
                throw new IllegalArgumentException();
        }
    }
}
