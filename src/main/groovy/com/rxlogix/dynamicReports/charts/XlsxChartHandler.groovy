package com.rxlogix.dynamicReports.charts

import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.JRPrintImage
import net.sf.jasperreports.engine.export.JRExporterGridCell
import net.sf.jasperreports.engine.export.ooxml.GenericElementXlsxHandler
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporterContext
import net.sf.jasperreports.engine.export.ooxml.XlsxZip
import net.sf.jasperreports.engine.export.zip.ExportZipEntry

public class XlsxChartHandler extends BaseChartHandler implements GenericElementXlsxHandler {
    private JRXlsxExporter exporter
    private XlsxZip xlsxZip
    private XlsxChartHelper chartHelper
    private JRExporterGridCell gridCell
    private int colIndex
    private int rowIndex

    public void exportElement(
            JRXlsxExporterContext exporterContext,
            JRGenericPrintElement element,
            JRExporterGridCell gridCell,
            int colIndex,
            int rowIndex
    ) throws JRException {
        this.gridCell = gridCell
        this.colIndex = colIndex
        this.rowIndex = rowIndex
        this.exporter = (JRXlsxExporter) exporterContext.getExporterRef()
        this.xlsxZip = exporter.xlsxZip
        ExportZipEntry chartEntry = addChartZipEntry(exporter.sheetIndex + 1)
        addChartRelationship(exporter.sheetIndex + 1)
        injectChart(exporter.sheetIndex + 1)
        Writer chartWriter = chartEntry.getWriter()
        def options = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart()
        int chartRowsCount = (int) element.getParameterValue(ChartGenerator.PARAMETER_CHART_ROWS_COUNT)
        List totalRowIndicies = element.getParameterValue(ChartGenerator.PARAMETER_TOTAL_ROW_INDICES)
        this.chartHelper = new XlsxChartHelper(exporter, chartWriter, options, chartRowsCount, totalRowIndicies)
        chartHelper.exportChart()
    }

    @Override
    public JRPrintImage getImage(JRXlsxExporterContext exporterContext, JRGenericPrintElement element) throws JRException {
        return null;
    }

    public ExportZipEntry addChartZipEntry(int index) {
        def chartPath = "xl/charts/chart" + index + ".xml"
        ExportZipEntry chartEntry = xlsxZip.createEntry(chartPath);
        xlsxZip.exportZipEntries.add(chartEntry);
        exporter.ctHelper.write("  <Override PartName=\"/${chartPath}\" ContentType=\"application/vnd.openxmlformats-officedocument.drawingml.chart+xml\"/>\n")
        return chartEntry;
    }

    public void addChartRelationship(int index) {
        this.exporter.drawingRelsHelper.write(" <Relationship Id=\"rId" + index + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart\" Target=\"../charts/chart" + index + ".xml\"/>\n");
    }

    public void injectChart(int index) throws JRException {
        exporter.drawingHelper.write("<xdr:twoCellAnchor editAs=\"oneCell\">\n");
        exporter.drawingHelper.write("    <xdr:from><xdr:col>0</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>3</xdr:row><xdr:rowOff>11160</xdr:rowOff></xdr:from>\n");
        exporter.drawingHelper.write("    <xdr:to><xdr:col>18</xdr:col><xdr:colOff>590550</xdr:colOff><xdr:row>6</xdr:row><xdr:rowOff>83520</xdr:rowOff></xdr:to>\n");
        exporter.drawingHelper.write("    <xdr:graphicFrame>\n");
        exporter.drawingHelper.write("        <xdr:nvGraphicFramePr>\n");
        exporter.drawingHelper.write("            <xdr:cNvPr id=\"2\" name=\"\"/>\n");
        exporter.drawingHelper.write("            <xdr:cNvGraphicFramePr/>\n");
        exporter.drawingHelper.write("        </xdr:nvGraphicFramePr>\n");
        exporter.drawingHelper.write("        <xdr:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/></xdr:xfrm>\n");
        exporter.drawingHelper.write("        <a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">\n");
        exporter.drawingHelper.write("            <c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"\n" +
                " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n" +
                " r:id=\"rId" + index +"\"/>\n");
        exporter.drawingHelper.write("        </a:graphicData></a:graphic>\n");
        exporter.drawingHelper.write("    </xdr:graphicFrame>\n");
        exporter.drawingHelper.write("    <xdr:clientData/>\n");
        exporter.drawingHelper.write("</xdr:twoCellAnchor>\n");
    }
}