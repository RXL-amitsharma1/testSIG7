package com.rxlogix.dynamicReports.charts.ooxml
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStopList
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties
import org.openxmlformats.schemas.drawingml.x2006.main.STPositiveFixedPercentage

import java.awt.Color
import java.awt.Point

/**
 * Package private class with utility methods.
 */
class ChartUtil {

    private ChartUtil() {}

    /**
     * Builds CTAxDataSource object content from POI ChartDataSource.
     * @param ctAxDataSource OOXML data source to build
     * @param dataSource POI data source to use
     */
    public static void buildAxDataSource(CTAxDataSource ctAxDataSource, ChartDataSource<?> dataSource) {
        if (dataSource.isNumeric()) {
            if (dataSource.isReference()) {
                buildNumRef(ctAxDataSource.addNewNumRef(), dataSource)
            } else {
                buildNumLit(ctAxDataSource.addNewNumLit(), dataSource)
            }
        } else {
            if (dataSource.isReference()) {
                if (dataSource.isMultiLevel()) {
                    buildMultiLvStrRef(ctAxDataSource.addNewMultiLvlStrRef(), dataSource)
                } else {
                    buildStrRef(ctAxDataSource.addNewStrRef(), dataSource)
                }
            } else {
                buildStrLit(ctAxDataSource.addNewStrLit(), dataSource)
            }
        }
    }

    /**
     * Builds CTNumDataSource object content from POI ChartDataSource
     * @param ctNumDataSource OOXML data source to build
     * @param dataSource POI data source to use
     */
    public static void buildNumDataSource(CTNumDataSource ctNumDataSource,
                                          ChartDataSource<? extends Number> dataSource) {
        if (dataSource.isReference()) {
            buildNumRef(ctNumDataSource.addNewNumRef(), dataSource)
        } else {
            buildNumLit(ctNumDataSource.addNewNumLit(), dataSource)
        }
    }

    private static void buildNumRef(CTNumRef ctNumRef, ChartDataSource<?> dataSource) {
        if (dataSource.hasFormulaString()) {
            ctNumRef.setF(dataSource.getFormulaString())
        }
        CTNumData cache = ctNumRef.addNewNumCache()
        fillNumCache(cache, dataSource)
    }

    private static void buildNumLit(CTNumData ctNumData, ChartDataSource<?> dataSource) {
        fillNumCache(ctNumData, dataSource)
    }

    private static void buildStrRef(CTStrRef ctStrRef, ChartDataSource<?> dataSource) {
        if (dataSource.hasFormulaString()) {
            ctStrRef.setF(dataSource.getFormulaString())
        }
        CTStrData cache = ctStrRef.addNewStrCache()
        fillStringCache(cache, dataSource)
    }

    private static void buildMultiLvStrRef(CTMultiLvlStrRef ctMultiLvStrRef, ChartDataSource<?> dataSource) {
        if (dataSource.hasFormulaString()) {
            ctMultiLvStrRef.setF(dataSource.getFormulaString())
        }
        CTMultiLvlStrData cache = ctMultiLvStrRef.addNewMultiLvlStrCache()
        fillMultiLvStringCache(cache, dataSource)
    }

    private static void buildStrLit(CTStrData ctStrData, ChartDataSource<?> dataSource) {
        fillStringCache(ctStrData, dataSource)
    }

    private static void fillStringCache(CTStrData cache, ChartDataSource<?> dataSource) {
        int numOfPoints = dataSource.getPointCount()
        cache.addNewPtCount().setVal(numOfPoints)
        for (int i = 0; i < numOfPoints; ++i) {
            def value = dataSource.getPointAt(i)
            if (value instanceof List) {
                value = value.first()
            }
            if (value != null) {
                CTStrVal ctStrVal = cache.addNewPt()
                ctStrVal.setIdx(i)
                ctStrVal.setV(value.toString())
            }
        }
    }

    private static void fillMultiLvStringCache(CTMultiLvlStrData cache, ChartDataSource<?> dataSource) {
        int numOfPoints = dataSource.getPointCount()
        cache.addNewPtCount().setVal(numOfPoints)
        def levels = [:]
        def values = [:]
        for (int i = 0; i < numOfPoints; ++i) {
            List<String> value = dataSource.getPointAt(i)
            for (int j = 0; j < value.size(); j++) {
                CTLvl level = levels[j]
                if (level == null) {
                    level = cache.addNewLvl()
                    levels[j] = level
                }
                def previous = values[j]
                def current = value.subList(j, value.size())
                if (previous != current) {
                    CTStrVal ctStrVal = level.addNewPt()
                    ctStrVal.setIdx(i)
                    ctStrVal.setV(value[j])
                    values[j] = current
                }
            }
        }
    }

    private static void fillNumCache(CTNumData cache, ChartDataSource<?> dataSource) {
        int numOfPoints = dataSource.getPointCount()
        cache.addNewPtCount().setVal(numOfPoints)
        for (int i = 0; i < numOfPoints; ++i) {
            Number value = (Number) dataSource.getPointAt(i)
            if (value != null) {
                CTNumVal ctNumVal = cache.addNewPt()
                ctNumVal.setIdx(i)
                ctNumVal.setV(value.toString())
            }
        }
    }

    public static Color decodeColorString(String colorString) {
        // rgba(48, 48, 48, 0.8)
        // rgb(70, 70, 70)
        if (colorString.startsWith("rgb")) {
            def args = colorString.substring(colorString.indexOf("(") + 1, colorString.indexOf(")"))
                    .split(",")
                    .collect {it.contains(".")? (int)(Float.valueOf(it.trim())* 255 + 0.5) : Integer.valueOf(it.trim())}
            return new Color(*args)
        } else {
            return Color.decode(colorString)
        }
    }

    public static byte[] colorString2bytes(String colorString) {
        Color color = decodeColorString(colorString)
        return  color2bytes(color)
    }

    public static byte[] color2bytes(Color color) {
        return  [(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()].toArray()
    }

    public static void fillShapeProperties(CTShapeProperties shapeProperties, def color) {
        if (color == null) {
            return
        } else if (color instanceof String) {
            fillShapeProperties(shapeProperties, decodeColorString(color))
        } else if (color instanceof Color) {
            CTSRgbColor ctsRgbColor = shapeProperties.addNewSolidFill().addNewSrgbClr()
            ctsRgbColor.setVal(color2bytes(color))
            if (color.alpha < 255) {
                ctsRgbColor.addNewAlpha().setVal((color.alpha / 255 * 100 * 1000) as int)
            }
        } else {
            if (color.linearGradient) {
                CTGradientFillProperties gradFillProps = shapeProperties.addNewGradFill()
                CTGradientStopList stopList = gradFillProps.addNewGsLst()
                color.stops.each() {
                    CTGradientStop stop = stopList.addNewGs()
                    STPositiveFixedPercentage
                    stop.setPos(it[0] * 100 * 1000)
                    stop.addNewSrgbClr().setVal(colorString2bytes(it[1]))
                }
                def linearGradient = color.linearGradient
                def angle
                if (linearGradient instanceof Collection) {
                    angle = getGradientAngle(linearGradient[0], linearGradient[1], linearGradient[2], linearGradient[3])
                } else {
                    angle = getGradientAngle(linearGradient.x1, linearGradient.y1, linearGradient.x2, linearGradient.y2)
                }
                gradFillProps.addNewLin().setAng((angle * 60000) as int)
            }
        }
    }

    private static double getGradientAngle(def x1, def y1, def x2, def y2) {
        def x = x2 - x1
        def y = y2 - y1
        def angle = Math.atan(y/x) * 180/Math.PI
        angle = (x < 0) ? angle + 180 : ((y < 0) ? angle + 360 : angle)
        return angle
    }
}
