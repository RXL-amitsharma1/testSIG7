package com.rxlogix.dynamicReports.charts.ooxml

import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisCrosses
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisLabelAlign
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisOrientation
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisPosition
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisTickMark
import grails.gorm.dirty.checking.DirtyCheck
import org.openxmlformats.schemas.drawingml.x2006.chart.*

/**
 * Base class for all axis types.
 */
@DirtyCheck
public abstract class ChartAxis {

    protected Chart chart

    private static final double MIN_LOG_BASE = 2.0
    private static final double MAX_LOG_BASE = 1000.0

    /**
     * @return axis id
     */
    abstract long getId()

    /**
     * Declare this axis cross another axis.
     * @param axis that this axis should cross
     */
    abstract void crossAxis(ChartAxis axis);

    protected ChartAxis(Chart chart) {
        this.chart = chart
    }

    public AxisPosition getPosition() {
        return toAxisPosition(getCTAxPos())
    }

    public void setPosition(AxisPosition position) {
        getCTAxPos().setVal(fromAxisPosition(position))
    }

    public void setNumberFormat(String format) {
        getCTNumFmt().setFormatCode(format)
        getCTNumFmt().setSourceLinked(true)
    }

    public String getNumberFormat() {
        return getCTNumFmt().getFormatCode()
    }

    public boolean isSetLogBase() {
        return getCTScaling().isSetLogBase()
    }

    public void setLogBase(double logBase) {
        if (logBase < MIN_LOG_BASE ||
                MAX_LOG_BASE < logBase) {
            throw new IllegalArgumentException("Axis log base must be between 2 and 1000 (inclusive), got: " + logBase)
        }
        CTScaling scaling = getCTScaling()
        if (scaling.isSetLogBase()) {
            scaling.getLogBase().setVal(logBase)
        } else {
            scaling.addNewLogBase().setVal(logBase)
        }
    }

    public double getLogBase() {
        CTLogBase logBase = getCTScaling().getLogBase()
        if (logBase != null) {
            return logBase.getVal()
        }
        return 0.0
    }

    public boolean isSetMinimum() {
        return getCTScaling().isSetMin()
    }

    public void setMinimum(double min) {
        CTScaling scaling = getCTScaling()
        if (scaling.isSetMin()) {
            scaling.getMin().setVal(min)
        } else {
            scaling.addNewMin().setVal(min)
        }
    }

    public double getMinimum() {
        CTScaling scaling = getCTScaling()
        if (scaling.isSetMin()) {
            return scaling.getMin().getVal()
        } else {
            return 0.0
        }
    }

    public boolean isSetMaximum() {
        return getCTScaling().isSetMax()
    }

    public void setMaximum(double max) {
        CTScaling scaling = getCTScaling()
        if (scaling.isSetMax()) {
            scaling.getMax().setVal(max)
        } else {
            scaling.addNewMax().setVal(max)
        }
    }

    public double getMaximum() {
        CTScaling scaling = getCTScaling()
        if (scaling.isSetMax()) {
            return scaling.getMax().getVal()
        } else {
            return 0.0
        }
    }

    public AxisOrientation getOrientation() {
        return toAxisOrientation(getCTScaling().getOrientation())
    }

    public void setOrientation(AxisOrientation orientation) {
        CTScaling scaling = getCTScaling()
        STOrientation.Enum stOrientation = fromAxisOrientation(orientation)
        if (scaling.isSetOrientation()) {
            scaling.getOrientation().setVal(stOrientation)
        } else {
            getCTScaling().addNewOrientation().setVal(stOrientation)
        }
    }

    public AxisCrosses getCrosses() {
        return toAxisCrosses(getCTCrosses())
    }

    public void setCrosses(AxisCrosses crosses) {
        getCTCrosses().setVal(fromAxisCrosses(crosses))
    }

    public boolean isVisible() {
        return !getDelete().getVal()
    }

    public void setVisible(boolean value) {
        getDelete().setVal(!value)
    }

    public AxisTickMark getMajorTickMark() {
        return toAxisTickMark(getMajorCTTickMark())
    }

    public void setMajorTickMark(AxisTickMark tickMark) {
        getMajorCTTickMark().setVal(fromAxisTickMark(tickMark))
    }

    public AxisTickMark getMinorTickMark() {
        return toAxisTickMark(getMinorCTTickMark())
    }

    public void setMinorTickMark(AxisTickMark tickMark) {
        getMinorCTTickMark().setVal(fromAxisTickMark(tickMark))
    }

    protected abstract CTAxPos getCTAxPos()

    protected abstract CTNumFmt getCTNumFmt()

    protected abstract CTScaling getCTScaling()

    protected abstract CTCrosses getCTCrosses()

    protected abstract CTBoolean getDelete()

    protected abstract CTTickMark getMajorCTTickMark()

    protected abstract CTTickMark getMinorCTTickMark()

    private static STOrientation.Enum fromAxisOrientation(AxisOrientation orientation) {
        switch (orientation) {
            case AxisOrientation.MIN_MAX: return STOrientation.MIN_MAX
            case AxisOrientation.MAX_MIN: return STOrientation.MAX_MIN
            default:
                throw new IllegalArgumentException()
        }
    }

    private static AxisOrientation toAxisOrientation(CTOrientation ctOrientation) {
        switch (ctOrientation.getVal().intValue()) {
            case STOrientation.INT_MIN_MAX: return AxisOrientation.MIN_MAX
            case STOrientation.INT_MAX_MIN: return AxisOrientation.MAX_MIN
            default:
                throw new IllegalArgumentException()
        }
    }

    private static STCrosses.Enum fromAxisCrosses(AxisCrosses crosses) {
        switch (crosses) {
            case AxisCrosses.AUTO_ZERO: return STCrosses.AUTO_ZERO
            case AxisCrosses.MIN: return STCrosses.MIN
            case AxisCrosses.MAX: return STCrosses.MAX
            default:
                throw new IllegalArgumentException()
        }
    }

    private static AxisCrosses toAxisCrosses(CTCrosses ctCrosses) {
        switch (ctCrosses.getVal().intValue()) {
            case STCrosses.INT_AUTO_ZERO: return AxisCrosses.AUTO_ZERO
            case STCrosses.INT_MAX: return AxisCrosses.MAX
            case STCrosses.INT_MIN: return AxisCrosses.MIN
            default:
                throw new IllegalArgumentException()
        }
    }

    private static STAxPos.Enum fromAxisPosition(AxisPosition position) {
        switch (position) {
            case AxisPosition.BOTTOM: return STAxPos.B
            case AxisPosition.LEFT: return STAxPos.L
            case AxisPosition.RIGHT: return STAxPos.R
            case AxisPosition.TOP: return STAxPos.T
            default:
                throw new IllegalArgumentException()
        }
    }

    private static AxisPosition toAxisPosition(CTAxPos ctAxPos) {
        switch (ctAxPos.getVal().intValue()) {
            case STAxPos.INT_B: return AxisPosition.BOTTOM
            case STAxPos.INT_L: return AxisPosition.LEFT
            case STAxPos.INT_R: return AxisPosition.RIGHT
            case STAxPos.INT_T: return AxisPosition.TOP
            default: return AxisPosition.BOTTOM
        }
    }

    private static STTickMark.Enum fromAxisTickMark(AxisTickMark tickMark) {
        switch (tickMark) {
            case AxisTickMark.NONE: return STTickMark.NONE
            case AxisTickMark.IN: return STTickMark.IN
            case AxisTickMark.OUT: return STTickMark.OUT
            case AxisTickMark.CROSS: return STTickMark.CROSS
            default:
                throw new IllegalArgumentException("Unknown AxisTickMark: " + tickMark)
        }
    }

    private static AxisTickMark toAxisTickMark(CTTickMark ctTickMark) {
        switch (ctTickMark.getVal().intValue()) {
            case STTickMark.INT_NONE: return AxisTickMark.NONE
            case STTickMark.INT_IN: return AxisTickMark.IN
            case STTickMark.INT_OUT: return AxisTickMark.OUT
            case STTickMark.INT_CROSS: return AxisTickMark.CROSS
            default: return AxisTickMark.CROSS
        }
    }
}
