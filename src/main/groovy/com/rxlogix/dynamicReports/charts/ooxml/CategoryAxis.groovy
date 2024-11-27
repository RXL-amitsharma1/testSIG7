package com.rxlogix.dynamicReports.charts.ooxml

import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisCrosses
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisLabelAlign
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisOrientation
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisPosition
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisTickMark
import grails.gorm.dirty.checking.DirtyCheck
import org.openxmlformats.schemas.drawingml.x2006.chart.*

/**
 * Category axis type.
 */
@DirtyCheck
public class CategoryAxis extends ChartAxis {

    private CTCatAx ctCatAx

    public CategoryAxis(Chart chart, long id, AxisPosition pos) {
        super(chart)
        createAxis(id, pos)
    }

    public CategoryAxis(Chart chart, CTCatAx ctCatAx) {
        super(chart)
        this.ctCatAx = ctCatAx
    }

    @Override
    public long getId() {
        return ctCatAx.getAxId().getVal()
    }

    @Override
    protected CTAxPos getCTAxPos() {
        return ctCatAx.getAxPos()
    }

    @Override
    protected CTNumFmt getCTNumFmt() {
        if (ctCatAx.isSetNumFmt()) {
            return ctCatAx.getNumFmt()
        }
        return ctCatAx.addNewNumFmt()
    }

    @Override
    protected CTScaling getCTScaling() {
        return ctCatAx.getScaling()
    }

    @Override
    protected CTCrosses getCTCrosses() {
        return ctCatAx.getCrosses()
    }

    @Override
    protected CTBoolean getDelete() {
        return ctCatAx.getDelete()
    }

    @Override
    protected CTTickMark getMajorCTTickMark() {
        return ctCatAx.getMajorTickMark()
    }

    @Override
    protected CTTickMark getMinorCTTickMark() {
        return ctCatAx.getMinorTickMark()
    }

    @Override
    public void crossAxis(ChartAxis axis) {
        ctCatAx.getCrossAx().setVal(axis.getId())
    }

    public AxisLabelAlign getLabelAlign() {
        return toAxisLabelAlign(ctCatAx.getLblAlgn())
    }

    public void setLabelAlign(AxisLabelAlign labelAlign) {
        ctCatAx.getLblAlgn().setVal(fromAxisLabelAlign(labelAlign))
    }

    public int getLabelOffset() {
        return ctCatAx.getLblOffset().getVal()
    }

    public void setLabelOffset(int labelOffset) {
        ctCatAx.getLblOffset().setVal(labelOffset)
    }

    private void createAxis(long id, AxisPosition pos) {
        ctCatAx = chart.getCTChart().getPlotArea().addNewCatAx()
        ctCatAx.addNewAxId().setVal(id)
        ctCatAx.addNewAxPos()
        ctCatAx.addNewScaling()
        ctCatAx.addNewCrosses()
        ctCatAx.addNewCrossAx()
        ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO)
        ctCatAx.addNewDelete()
        ctCatAx.addNewMajorTickMark()
        ctCatAx.addNewMinorTickMark()
        ctCatAx.addNewLblAlgn()
        ctCatAx.addNewLblOffset()
        ctCatAx.addNewNoMultiLvlLbl().setVal(false)

        setPosition(pos)
        setOrientation(AxisOrientation.MIN_MAX)
        setCrosses(AxisCrosses.AUTO_ZERO)
        setVisible(true)
        setMajorTickMark(AxisTickMark.OUT)
        setMinorTickMark(AxisTickMark.NONE)
        setLabelAlign(AxisLabelAlign.CENTER)
        setLabelOffset(100)
    }

    private static STLblAlgn.Enum fromAxisLabelAlign(AxisLabelAlign labelAlign) {
        switch (labelAlign) {
            case AxisLabelAlign.CENTER: return STLblAlgn.CTR
            case AxisLabelAlign.LEFT: return STLblAlgn.L
            case AxisLabelAlign.RIGHT: return STLblAlgn.R
            default:
                throw new IllegalArgumentException("Unknown AxisLabelAlign: " + labelAlign)
        }
    }

    private static AxisLabelAlign toAxisLabelAlign(CTLblAlgn ctLabelAlign) {
        switch (ctLabelAlign.getVal().intValue()) {
            case STLblAlgn.INT_CTR: return AxisLabelAlign.CENTER
            case STLblAlgn.INT_L: return AxisLabelAlign.LEFT
            case STLblAlgn.INT_R: return AxisLabelAlign.RIGHT
            default: return AxisLabelAlign.CENTER
        }
    }
}