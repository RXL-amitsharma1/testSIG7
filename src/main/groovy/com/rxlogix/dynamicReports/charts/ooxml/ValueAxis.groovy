package com.rxlogix.dynamicReports.charts.ooxml

import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisCrossBetween
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisCrosses
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisOrientation
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisPosition
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisTickMark
import grails.gorm.dirty.checking.DirtyCheck
import org.openxmlformats.schemas.drawingml.x2006.chart.*
@DirtyCheck
public class ValueAxis extends ChartAxis {

    private CTValAx ctValAx

    public ValueAxis(Chart chart, long id, AxisPosition pos) {
        super(chart)
        createAxis(id, pos)
    }

    public ValueAxis(Chart chart, CTValAx ctValAx) {
        super(chart)
        this.ctValAx = ctValAx
    }

    @Override
    public long getId() {
        return ctValAx.getAxId().getVal()
    }

    public void setCrossBetween(AxisCrossBetween crossBetween) {
        ctValAx.getCrossBetween().setVal(fromCrossBetween(crossBetween))
    }

    public AxisCrossBetween getCrossBetween() {
        return toCrossBetween(ctValAx.getCrossBetween().getVal())
    }

    @Override
    protected CTAxPos getCTAxPos() {
        return ctValAx.getAxPos()
    }

    @Override
    protected CTNumFmt getCTNumFmt() {
        if (ctValAx.isSetNumFmt()) {
            return ctValAx.getNumFmt()
        }
        return ctValAx.addNewNumFmt()
    }

    @Override
    protected CTScaling getCTScaling() {
        return ctValAx.getScaling()
    }

    @Override
    protected CTCrosses getCTCrosses() {
        return ctValAx.getCrosses()
    }

    @Override
    protected CTBoolean getDelete() {
        return ctValAx.getDelete()
    }

    @Override
    protected CTTickMark getMajorCTTickMark() {
        return ctValAx.getMajorTickMark()
    }

    @Override
    protected CTTickMark getMinorCTTickMark() {
        return ctValAx.getMinorTickMark()
    }

    @Override
    public void crossAxis(ChartAxis axis) {
        ctValAx.getCrossAx().setVal(axis.getId())
    }

    private void createAxis(long id, AxisPosition pos) {
        ctValAx = chart.getCTChart().getPlotArea().addNewValAx()
        ctValAx.addNewAxId().setVal(id)
        ctValAx.addNewAxPos()
        ctValAx.addNewScaling()
        ctValAx.addNewCrossBetween()
        ctValAx.addNewCrosses()
        ctValAx.addNewCrossAx()
        ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO)
        ctValAx.addNewDelete()
        ctValAx.addNewMajorTickMark()
        ctValAx.addNewMinorTickMark()
        ctValAx.addNewMajorGridlines()

        setPosition(pos)
        setOrientation(AxisOrientation.MIN_MAX)
        setCrossBetween(AxisCrossBetween.BETWEEN)
        setCrosses(AxisCrosses.AUTO_ZERO)
        setVisible(true)
        setMajorTickMark(AxisTickMark.OUT)
        setMinorTickMark(AxisTickMark.NONE)
    }

    private static STCrossBetween.Enum fromCrossBetween(AxisCrossBetween crossBetween) {
        switch (crossBetween) {
            case AxisCrossBetween.BETWEEN: return STCrossBetween.BETWEEN
            case AxisCrossBetween.MIDPOINT_CATEGORY: return STCrossBetween.MID_CAT
            default:
                throw new IllegalArgumentException()
        }
    }

    private static AxisCrossBetween toCrossBetween(STCrossBetween.Enum ctCrossBetween) {
        switch (ctCrossBetween.intValue()) {
            case STCrossBetween.INT_BETWEEN: return AxisCrossBetween.BETWEEN
            case STCrossBetween.INT_MID_CAT: return AxisCrossBetween.MIDPOINT_CATEGORY
            default:
                throw new IllegalArgumentException()
        }
    }
}
