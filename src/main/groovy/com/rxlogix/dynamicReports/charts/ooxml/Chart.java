package com.rxlogix.dynamicReports.charts.ooxml;

import com.rxlogix.dynamicReports.charts.ooxml.data.ChartData;
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartDataFactory;
import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisPosition;
import com.rxlogix.dynamicReports.charts.ooxml.enums.LegendPosition;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

import javax.xml.namespace.QName;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Represents a SpreadsheetML Chart
 */
public final class Chart implements ManuallyPositionable, ChartAxisFactory {

    /**
     * Parent graphic frame.
     */
    //private GraphicFrame frame

    /**
     * Root element of the SpreadsheetML Chart part
     */
    private CTChartSpace chartSpace;
    /**
     * The Chart within that
     */
    private CTChart chart;

    /**
     * Chart options
     */
    private Map<String, ?> options;

    List<ChartAxis> axis = new ArrayList<ChartAxis>();

    /**
     * Create a new SpreadsheetML chart
     */
    public Chart(Map<String, ?> options) {
        super();
        this.options = options;
        createChart();
    }

    /**
     * Construct a new CTChartSpace bean.
     * By default, it's just an empty placeholder for chart objects.
     *
     * @return a new CTChartSpace bean
     */
    private void createChart() {
        chartSpace = CTChartSpace.Factory.newInstance();
        chartSpace.addNewDate1904().setVal(false);
        chartSpace.addNewLang().setVal("en-US");
        chartSpace.addNewRoundedCorners().setVal(false);
        chartSpace.addNewStyle().setVal((short)2);


        CTShapeProperties chartSpaceProperties = chartSpace.addNewSpPr();
        Object backGroundColor = ((Map)options.get("chart")).get("backgroundColor");
        if (backGroundColor == null) {
            backGroundColor = ChartColorTheme.DEFAULT_CHART_BACKGROUND_COLOR;
        }
        ChartUtil.fillShapeProperties(chartSpaceProperties, backGroundColor);
        Integer borderWidth = (Integer) ((Map)options.get("chart")).get("borderWidth");
        if (borderWidth != null) {
            CTLineProperties chartSpaceBorder = chartSpaceProperties.addNewLn();
            chartSpaceBorder.setW(Units.toEMU(borderWidth));
            String borderColor = (String) ((Map)options.get("chart")).get("borderColor");
            if (borderColor == null) {
                borderColor = ChartColorTheme.DEFAULT_CHART_BORDER_COLOR;
            }
            chartSpaceBorder.addNewSolidFill().addNewSrgbClr().setVal(ChartUtil.colorString2bytes(borderColor));
            Integer borderRadius = (Integer) ((Map)options.get("chart")).get("borderRadius");
            if (borderRadius != null) {
                chartSpace.addNewRoundedCorners().setVal(true);
            }
        }

        chart = chartSpace.addNewChart();
        CTPlotArea plotArea = chart.addNewPlotArea();
        Object plotBackgroundColor = ((Map)options.get("chart")).get("plotBackgroundColor");
        CTShapeProperties plotAreaProperties = plotArea.addNewSpPr();
        if (plotBackgroundColor != null) {
            ChartUtil.fillShapeProperties(plotAreaProperties, plotBackgroundColor);
        } else {
            plotAreaProperties.addNewNoFill();
        }

        Integer plotBorderWidth = (Integer) ((Map)options.get("chart")).get("plotBorderWidth");
        if (plotBorderWidth != null) {
            CTLineProperties plotAreaBorder = plotAreaProperties.addNewLn();
            plotAreaBorder.setW(Units.toEMU(plotBorderWidth));
            String plotBorderColor = (String) ((Map)options.get("chart")).get("plotBorderColor");
            if (plotBorderColor == null) {
                plotBorderColor = ChartColorTheme.DEFAULT_PLOT_BORDER_COLOR;
            }
            plotAreaBorder.addNewSolidFill().addNewSrgbClr().setVal(ChartUtil.colorString2bytes(plotBorderColor));
        }

        plotArea.addNewLayout();
        chart.addNewPlotVisOnly().setVal(true);
        chart.addNewDispBlanksAs().setVal(STDispBlanksAs.GAP);
        chart.addNewShowDLblsOverMax().setVal(false);

        CTPrintSettings printSettings = chartSpace.addNewPrintSettings();
        printSettings.addNewHeaderFooter();

        CTPageMargins pageMargins = printSettings.addNewPageMargins();
        pageMargins.setB(0.75);
        pageMargins.setL(0.70);
        pageMargins.setR(0.70);
        pageMargins.setT(0.75);
        pageMargins.setHeader(0.30);
        pageMargins.setFooter(0.30);
        printSettings.addNewPageSetup();

        String chartType = (String) ((Map)options.get("chart")).get("type");

        ChartLegend chartLegend = getOrCreateLegend();
        if (!"pie".equals(chartType)) {
            createCategoryAxis(AxisPosition.BOTTOM);
            createValueAxis(AxisPosition.LEFT);
        }
        String title = (String) ((Map)options.get("title")).get("text");
        if (title != null) {
            setTitle(title);
        }
    }

    /**
     * Return the underlying CTChartSpace bean, the root element of the SpreadsheetML Chart part.
     *
     * @return the underlying CTChartSpace bean
     */
    public CTChartSpace getCTChartSpace() {
        return chartSpace;
    }

    /**
     * Return the underlying CTChart bean, within the Chart Space
     *
     * @return the underlying CTChart bean
     */
    public CTChart getCTChart() {
        return chart;
    }

    @Override
    public String toString() {
        try {
            XmlOptions xmlOptions = new XmlOptions();

            /*
               Saved chart space must have the following namespaces set:
               <c:chartSpace
                  xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"
                  xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
             */
            xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

            StringWriter sw = new StringWriter();
            chartSpace.save(sw, xmlOptions);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the parent graphic frame.
     *
     * @return the graphic frame this chart belongs to
     */
    /*
    public GraphicFrame getGraphicFrame() {
        return frame
    }
    */

    /**
     * Sets the parent graphic frame.
     */
    /*
    protected void setGraphicFrame(GraphicFrame frame) {
        this.frame = frame
    }
    */

    public ChartDataFactory getChartDataFactory() {
        return ChartDataFactory.getInstance();
    }

    public Chart getChartAxisFactory() {
        return this;
    }

    public void plot(ChartData data) {
        data.fillChart(this, axis.toArray(new ChartAxis[axis.size()]));
    }

    public ValueAxis createValueAxis(AxisPosition pos) {
        long id = axis.size() + 1;
        ValueAxis valueAxis = new ValueAxis(this, id, pos);
        if (axis.size() == 1) {
            ChartAxis ax = axis.get(0);
            ax.crossAxis(valueAxis);
            valueAxis.crossAxis(ax);
        }
        axis.add(valueAxis);
        return valueAxis;
    }

    public CategoryAxis createCategoryAxis(AxisPosition pos) {
        long id = axis.size() + 1;
        CategoryAxis categoryAxis = new CategoryAxis(this, id, pos);
        if (axis.size() == 1) {
            ChartAxis ax = axis.get(0);
            ax.crossAxis(categoryAxis);
            categoryAxis.crossAxis(ax);
        }
        axis.add(categoryAxis);
        return categoryAxis;
    }

    public List<? extends ChartAxis> getAxis() {
        if (axis.isEmpty() && hasAxis()) {
            parseAxis();
        }
        return axis;
    }

    public ManualLayout getManualLayout() {
        return new ManualLayout(this);
    }

    /**
     * @return true if only visible cells will be present on the chart,
     * false otherwise
     */
    public boolean isPlotOnlyVisibleCells() {
        return chart.getPlotVisOnly().getVal();
    }

    /**
     * @param plotVisOnly a flag specifying if only visible cells should be
     *                    present on the chart
     */
    public void setPlotOnlyVisibleCells(boolean plotVisOnly) {
        chart.getPlotVisOnly().setVal(plotVisOnly);
    }

    /**
     * Returns the title, or null if none is set
     */
    /*
    public RichTextString getTitle() {
        if (!chart.isSetTitle()) {
            return null
        }

        // TODO Do properly
        CTTitle title = chart.getTitle()

        StringBuffer text = new StringBuffer()
        XmlObject[] t = title
                .selectPath("declare namespace a='" + Drawing.NAMESPACE_A + "' .//a:t")
        for (int m = 0 m < t.length m++) {
            NodeList kids = t[m].getDomNode().getChildNodes()
            for (int n = 0 n < kids.getLength() n++) {
                if (kids.item(n) instanceof Text) {
                    text.append(kids.item(n).getNodeValue())
                }
            }
        }

        return new RichTextString(text.toString())
    }*/

    /**
     * Sets the title text.
     */
    public void setTitle(String newTitle) {
        CTTitle ctTitle;
        if (chart.isSetTitle()) {
            ctTitle = chart.getTitle();
        } else {
            ctTitle = chart.addNewTitle();
        }
        ctTitle.addNewOverlay().setVal(false);

        CTTx tx;
        if (ctTitle.isSetTx()) {
            tx = ctTitle.getTx();
        } else {
            tx = ctTitle.addNewTx();
        }

        if (tx.isSetStrRef()) {
            tx.unsetStrRef();
        }

        CTTextBody rich;
        if (tx.isSetRich()) {
            rich = tx.getRich();
        } else {
            rich = tx.addNewRich();
            rich.addNewBodyPr();  // body properties must exist (but can be empty)
        }
        rich.addNewLstStyle();

        CTTextParagraph para;
        if (rich.sizeOfPArray() > 0) {
            para = rich.getPArray(0);
        } else {
            para = rich.addNewP();
        }

        if (para.sizeOfRArray() > 0) {
            CTRegularTextRun run = para.getRArray(0);
            run.setT(newTitle);
        } else if (para.sizeOfFldArray() > 0) {
            CTTextField fld = para.getFldArray(0);
            fld.setT(newTitle);
        } else {
            CTRegularTextRun run = para.addNewR();
            run.setT(newTitle);
        }
    }

    public ChartLegend getOrCreateLegend() {
        return new ChartLegend(this);
    }

    public void deleteLegend() {
        if (chart.isSetLegend()) {
            chart.unsetLegend();
        }
    }

    private boolean hasAxis() {
        CTPlotArea ctPlotArea = chart.getPlotArea();
        int totalAxisCount =
                ctPlotArea.sizeOfValAxArray() +
                        ctPlotArea.sizeOfCatAxArray() +
                        ctPlotArea.sizeOfDateAxArray() +
                        ctPlotArea.sizeOfSerAxArray();
        return totalAxisCount > 0;
    }

    private void parseAxis() {
        // TODO: add other axis types
        parseCategoryAxis();
        parseValueAxis();
    }

    private void parseCategoryAxis() {
        for (CTCatAx catAx : chart.getPlotArea().getCatAxArray()) {
            axis.add(new CategoryAxis(this, catAx));
        }
    }

    private void parseValueAxis() {
        for (CTValAx valAx : chart.getPlotArea().getValAxArray()) {
            axis.add(new ValueAxis(this, valAx));
        }
    }

    public Map<String, ?> getOptions() {
        return options;
    }
}
