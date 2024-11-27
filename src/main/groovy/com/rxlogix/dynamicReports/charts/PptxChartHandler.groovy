package com.rxlogix.dynamicReports.charts

import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.JRPrintElementIndex
import net.sf.jasperreports.engine.export.ooxml.GenericElementPptxHandler
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporterContext
import net.sf.jasperreports.engine.export.ooxml.PptxZip
import net.sf.jasperreports.engine.export.zip.ExportZipEntry

public class PptxChartHandler extends BaseChartHandler implements GenericElementPptxHandler {
    private JRPptxExporterContext exporterContext
    private JRGenericPrintElement element
    private JRPptxExporter exporter
    private PptxZip pptxZip
    private PptxChartHelper chartHelper


    public void exportElement(
            JRPptxExporterContext exporterContext,
            JRGenericPrintElement element
    ) throws JRException {
        this.exporterContext = exporterContext
        this.element = element
        this.exporter = (JRPptxExporter) exporterContext.getExporterRef()
        this.pptxZip = exporter.pptxZip

        JRPrintElementIndex index = exporter.getElementIndex()
        ExportZipEntry chartEntry = addChartZipEntry(index.toString())
        addChartRelationship(index.toString())
        injectChart(index.toString())
        Writer chartWriter = chartEntry.getWriter()
        def options = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart()
        int chartRowsCount = (int) element.getParameterValue(ChartGenerator.PARAMETER_CHART_ROWS_COUNT)
        this.chartHelper = new PptxChartHelper(exporter, chartWriter, options, chartRowsCount)
        chartHelper.exportChart()
    }

    public ExportZipEntry addChartZipEntry(String index) {
        def chartPath = "ppt/charts/chart" + index + ".xml"
        ExportZipEntry chartEntry = pptxZip.createEntry(chartPath)
        pptxZip.exportZipEntries.add(chartEntry)
        exporter.ctHelper.write("  <Override PartName=\"/${chartPath}\" ContentType=\"application/vnd.openxmlformats-officedocument.drawingml.chart+xml\"/>\n")
        return chartEntry;
    }

    public void addChartRelationship(String index) {
        this.exporter.slideRelsHelper.write(" <Relationship Id=\"rId" + index + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart\" Target=\"../charts/chart" + index + ".xml\"/>\n");
    }

    public void injectChart(String index) throws JRException {
        exporter.slideHelper.write(
                        "            <p:graphicFrame>\n" +
                        "                <p:nvGraphicFramePr>\n" +
                        "                    <p:cNvPr id=\"3\" name=\"Chart 2\"/>\n" +
                        "                    <p:cNvGraphicFramePr>\n" +
                        "                        <a:graphicFrameLocks noGrp=\"1\"/>\n" +
                        "                    </p:cNvGraphicFramePr>\n" +
                        "                    <p:nvPr/>\n" +
                        "                </p:nvGraphicFramePr>\n" +
                        "                <p:xfrm>\n" +
                        "                    <a:off x=\"342900\" y=\"1365920\"/>\n" +
                        "                    <a:ext cx=\"9372600\" cy=\"5472608\"/>\n" +
                        "                </p:xfrm>\n" +
                        "                <a:graphic>\n" +
                        "                    <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">\n" +
                        "                        <c:chart\n" +
                        "                            xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"\n" +
                        "                            xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n" +
                        "                                         r:id=\"rId${index}\"/>\n" +
                        "                        </a:graphicData>\n" +
                        "                    </a:graphic>\n" +
                        "                </p:graphicFrame>")
    }
}