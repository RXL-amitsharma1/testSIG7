package com.rxlogix.dynamicReports.charts

import com.rxlogix.dynamicReports.charts.docx.ContentTypesZipEntry
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.JRPrintElementIndex
import net.sf.jasperreports.engine.export.JRExporterGridCell
import net.sf.jasperreports.engine.export.ooxml.DocxZip
import net.sf.jasperreports.engine.export.ooxml.GenericElementDocxHandler
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporterContext
import net.sf.jasperreports.engine.export.zip.ExportZipEntry

class DocxChartHandler extends BaseChartHandler implements GenericElementDocxHandler {
    private JRDocxExporterContext exporterContext
    private JRGenericPrintElement element
    private JRDocxExporter exporter
    private DocxZip docxZip
    private DocxChartHelper chartHelper
    private JRExporterGridCell gridCell


    void exportElement(
            JRDocxExporterContext exporterContext,
            JRGenericPrintElement element,
            JRExporterGridCell gridCell
    ) throws JRException {
        this.exporterContext = exporterContext
        this.element = element
        this.gridCell = gridCell
        this.exporter = (JRDocxExporter) exporterContext.getExporterRef()
        this.docxZip = exporter.docxZip

        // Very durty hack: removing existing content types entry and adding own implementation
        ContentTypesZipEntry contentTypesEntry = docxZip.exportZipEntries.find {
            it.name == "[Content_Types].xml" && it instanceof ContentTypesZipEntry
        }
        if (!contentTypesEntry) {
            docxZip.exportZipEntries.removeAll {
                it.name == "[Content_Types].xml"
            }
            contentTypesEntry = new ContentTypesZipEntry("[Content_Types].xml")
            docxZip.addEntry(contentTypesEntry)
        }
        // End of the very durty hack

        JRPrintElementIndex index = exporter.getElementIndex(gridCell)
        ExportZipEntry chartEntry = addChartZipEntry(index.toString())
        contentTypesEntry.addContentType("<Override PartName=\"/${chartEntry.name}\" ContentType=\"application/vnd.openxmlformats-officedocument.drawingml.chart+xml\"/>")
        addChartRelationship(index.toString())
        injectChart(index.toString())
        Writer chartWriter = chartEntry.getWriter()
        def options = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart()
        int chartRowsCount = (int) element.getParameterValue(ChartGenerator.PARAMETER_CHART_ROWS_COUNT)
        this.chartHelper = new DocxChartHelper(exporter, chartWriter, options, chartRowsCount)
        chartHelper.exportChart()
    }

    ExportZipEntry addChartZipEntry(String index) {
        def chartPath = "word/charts/chart" + index + ".xml"
        ExportZipEntry chartEntry = docxZip.createEntry(chartPath)
        docxZip.exportZipEntries.add(chartEntry)
        return chartEntry;
    }

    void addChartRelationship(String index) {
        this.exporter.relsHelper.write(" <Relationship Id=\"rId" + index + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart\" Target=\"charts/chart" + index + ".xml\"/>\n");
    }

    void injectChart(String index) throws JRException {
        exporterContext.tableHelper.cellHelper.exportHeader(element, gridCell)
        exporter.docHelper.write(
                "        <w:p>\n" +
                "            <w:pPr>\n" +
                "                <w:spacing w:lineRule=\"auto\" w:line=\"240\" w:after=\"0\" w:before=\"0\"/>\n" +
                "            </w:pPr>\n" +
                "            <w:r>\n" +
                "                <w:rPr/>\n" +
                "                <w:drawing>\n" +
                "                    <wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">\n" +
                "                        <wp:extent cx=\"9372600\" cy=\"5270500\" />\n" +
                "                        <wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\" />\n" +
                "                        <wp:docPr id=\"1\" name=\"chart\" />\n" +
                "                        <a:graphic>\n" +
                "                            <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">\n" +
                "                                <c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"\n" +
                "                                         xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n" +
                "                                         r:id=\"rId${index}\"/>\n" +
                "                            </a:graphicData>\n" +
                "                        </a:graphic>\n" +
                "                    </wp:inline>\n" +
                "                </w:drawing>\n" +
                "            </w:r>\n" +
                "        </w:p>");
        exporterContext.tableHelper.cellHelper.exportFooter()
    }
}