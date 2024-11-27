package com.rxlogix.dynamicReports.charts;

import java.awt.Color;

import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.base.JRBasePrintText;
import net.sf.jasperreports.engine.export.ElementGridCell;
import net.sf.jasperreports.engine.export.ElementReplacementGridCell;
import net.sf.jasperreports.engine.export.GenericElementHandler;
import net.sf.jasperreports.engine.export.JRExporterContext;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.LineStyleEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.util.JRTextMeasurerUtil;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 */
public class BaseChartHandler implements GenericElementHandler {
    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }

    public JRPrintText getTextElementReplacement(
            JRExporterContext exporterContext,
            JRGenericPrintElement element
    ) {
        JRBasePrintText text = new JRBasePrintText(exporterContext.getExportedReport().getDefaultStyleProvider());
        text.setX(element.getX());
        text.setY(element.getY());
        text.setWidth(element.getWidth());
        text.setHeight(element.getHeight());
        text.setText("[Open Flash Chart Component]");
        text.setMode(ModeEnum.OPAQUE);
        text.setBackcolor(Color.lightGray);
        text.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        text.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        text.getLineBox().getPen().setLineWidth(1f);
        text.getLineBox().getPen().setLineColor(Color.black);
        text.getLineBox().getPen().setLineStyle(LineStyleEnum.DASHED);

        JRTextMeasurerUtil.getInstance(exporterContext.getJasperReportsContext()).measureTextElement(text);

        return text;
    }

    public JRExporterGridCell getGridCellReplacement(
            JRExporterContext exporterContext,
            JRGenericPrintElement element,
            JRExporterGridCell gridCell
    ) {
        JRPrintText text = getTextElementReplacement(exporterContext, element);

        JRExporterGridCell newGridCell = new ElementReplacementGridCell((ElementGridCell) gridCell, text);
        newGridCell.setBox(text.getLineBox());

        return newGridCell;
    }
}
