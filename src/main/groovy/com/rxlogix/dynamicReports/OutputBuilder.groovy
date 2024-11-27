package com.rxlogix.dynamicReports

import com.rxlogix.DynamicReportService
import com.rxlogix.SignalAuditLogService
import com.rxlogix.enums.ReportFormat
import com.rxlogix.util.MiscUtil
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.export.JasperDocxExporterBuilder
import net.sf.dynamicreports.jasper.builder.export.JasperXlsxExporterBuilder
import com.rxlogix.Constants
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder
import org.apache.commons.lang3.StringUtils

import java.nio.charset.StandardCharsets

import static net.sf.dynamicreports.report.builder.DynamicReports.export


class OutputBuilder {
    // https://support.office.com/en-us/article/Excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
    private static final int XLSX_MAX_ROWS_PER_SHEET = 1048576
    private static final int MAX_FILE_NAME_LENGTH = 200

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")

    File produceReportOutput(Map params, String name, report,  List<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        name = MiscUtil.getValidFileName(name)

        def reportFile = null
        if (!params.outputFormat || params.outputFormat == ReportFormat.HTML.name()) {
            reportFile = exportHtml(report, name)
        } else if (params.outputFormat == ReportFormat.PDF.name()) {
            reportFile = exportPdf(report, name)
        } else if (params.outputFormat == ReportFormat.XLSX.name()) {
            reportFile = exportXlsx(report, name, jasperReportBuilderEntryList)
        } else if (params.outputFormat == ReportFormat.DOCX.name()) {
            reportFile = exportDocx(report, name)
        }
        return reportFile
    }

    private File exportHtml(report, String name) {
        File reportFile = new File(correctedReportDirectoryPath(dynamicReportService.getReportsDirectory()) + dynamicReportService.getReportFilename(name, ReportFormat.HTML.name()))
        try (FileOutputStream fileOutputStream = new FileOutputStream(reportFile)) {
            report.toHtml(fileOutputStream)
        }
        return reportFile
    }

    private File exportPdf(report, String name) {
        String sanitizeFileName = sanitizeFileName(dynamicReportService.getReportFilename(name, ReportFormat.PDF.name()))
        File reportFile = new File(correctedReportDirectoryPath(dynamicReportService.getReportsDirectory()) + sanitizeFileName)
        try (FileOutputStream fileOutputStream = new FileOutputStream(reportFile)) {
            JasperPdfExporterBuilder pdfExporter = export.pdfExporter(fileOutputStream)
            pdfExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
            pdfExporter.setForceLineBreakPolicy(true)
            pdfExporter.setCompressed(true)
            report.toPdf(pdfExporter)
        }
        return reportFile
    }

    private File exportXlsx(report, String name, List<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        String sanitizeFileName = sanitizeFileName(dynamicReportService.getReportFilename(name, ReportFormat.XLSX.name()))
        List<String> newSheetNames = jasperReportBuilderEntryList?.findAll { it?.excelSheetName }?.collect { it.excelSheetName.replaceAll('\\:', '->') }
        File reportFile = new File(correctedReportDirectoryPath(dynamicReportService.getReportsDirectory()) + sanitizeFileName)
        try (FileOutputStream fileOutputStream = new FileOutputStream(reportFile)) {
            JasperXlsxExporterBuilder xlsxExporter = export.xlsxExporter(fileOutputStream)
            xlsxExporter.removeEmptySpaceBetweenRows = true
            xlsxExporter.sheetNames(newSheetNames.toArray(new String[0]))
            xlsxExporter.removeEmptySpaceBetweenColumns = true // Adjust based on your needs
            xlsxExporter.detectCellType = false
            xlsxExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
            xlsxExporter.setMaxRowsPerSheet(XLSX_MAX_ROWS_PER_SHEET)
            xlsxExporter.collapseRowSpan = true
            xlsxExporter.whitePageBackground = false
            report.toXlsx(xlsxExporter)
        }
        return reportFile
    }





    private File exportDocx(report, String reportName) {
        String sanitizeFileName = sanitizeFileName(dynamicReportService.getReportFilename(reportName, ReportFormat.DOCX.name()))
        File reportFile = new File(correctedReportDirectoryPath(dynamicReportService.getReportsDirectory()) + sanitizeFileName)
        try (FileOutputStream fileOutputStream = new FileOutputStream(reportFile)) {
            JasperDocxExporterBuilder docxExporter = export.docxExporter(fileOutputStream)
            docxExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
            docxExporter.setFlexibleRowHeight(true)
            report.toDocx(docxExporter)
        }
        return reportFile
    }

    private String correctedReportDirectoryPath(String reportsDirectory) { // Added for PVS-54100
        if (StringUtils.isNotBlank(reportsDirectory)) {
            if (reportsDirectory.endsWith("/.signal")) {
                reportsDirectory = reportsDirectory.replace(".signal", "")
            }
        }
        return reportsDirectory
    }

    private String sanitizeFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.')
        String fileNameWithoutExt = fileName.substring(0, dotIndex)
        String fileExtension = fileName.substring(dotIndex)
        // Replace special characters and emojis with underscores
        String sanitizedFileName = fileNameWithoutExt.replaceAll("[^a-zA-Z0-9.-]", "_")
        // Truncate the file name if it exceeds a certain length
        if (sanitizedFileName.length() > MAX_FILE_NAME_LENGTH) {
            sanitizedFileName = sanitizedFileName.substring(0, MAX_FILE_NAME_LENGTH)
        }
        return sanitizedFileName + fileExtension
    }



}
