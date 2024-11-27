package com.rxlogix.dynamicReports

import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SensitivityLabelEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.constant.*
import net.sf.dynamicreports.report.builder.component.BreakBuilder


import static net.sf.dynamicreports.report.builder.DynamicReports.*

class FooterBuilder {
    private static final int XSLX_HEADER_ROWS_GAP = 10
    private static final int TEMPLATE_QUERY_FOOTER_MAX_SYMBOL_IN_ROW = 30;
    Integer templateFooterMaxLength = 500
    Integer reportFooterMaxLength = 500
    String setFooter(Map params, JasperReportBuilder report,  boolean isCriteriaSheetOrAppendix) {
        setFooterTemplate(report, params)
        setFooterParameters(params, report, isCriteriaSheetOrAppendix)
    }

    void setFooterTemplate(JasperReportBuilder report,  Map params = null) {

        // Column footer has fixed height. We should determinate lines count and set approximate footer height
        def templateFooter = null
        def showTemplateFooter = false
        HorizontalListBuilder footerComponent = cmp.horizontalList()
                .setStyle(stl.style().setTopBorder(Templates.horizontalLine))

        if (showTemplateFooter && templateFooter != null) {
            // Column footer has fixed height. We should determinate lines count and set approximate footer height
            def delimitersCount = templateFooter.count("\n")
            if (templateFooter.length() > templateFooterMaxLength) {
                templateFooter = templateFooter.substring(0, templateFooterMaxLength)
            }
            report.pageFooter(cmp.horizontalList(cmp.text(templateFooter).setMinRows(delimitersCount + 1)).setStyle(stl.style().setTopBorder(Templates.horizontalLine)) )
        }

        String templateQueryFooter =  ''
        if (templateQueryFooter.length() > reportFooterMaxLength) {
            templateQueryFooter.replaceAll("\\R", "").substring(0, reportFooterMaxLength)
        }

        if (templateQueryFooter.contains("\"")) {
            templateQueryFooter = templateQueryFooter.replaceAll("\"", "\\\\\"")
        }

        TextFieldBuilder textFieldBuilder = cmp.text(exp.jasperSyntax("\"${templateQueryFooter?.replaceAll('"','\\\\"')}\"")) //Added replaceAll to handle doublequotes text.
                .setWidth(180)
                .setMinRows(getTemplateQueryFooterRowCount(templateQueryFooter))
                .setStretchWithOverflow(true)
                .setStyle(stl.style().setVerticalAlignment(VerticalAlignment.MIDDLE))
                .setRemoveLineWhenBlank(true)

        footerComponent.add(textFieldBuilder)
        footerComponent.add(Templates.pageNumberingComponent
                .setWidth(160)
                .setRemoveLineWhenBlank(true))

        int height = (!params?.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) ? 40 : 30

        report.pageFooter(footerComponent)
        report.setPageFooterStyle(Templates.pageFooterStyle)
    }

    String setFooterParameters(Map params, JasperReportBuilder report, boolean isCriteriaSheetOrAppendix) {
        if (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
            report.ignorePagination()
            String footerText = ''
            report.setParameter(CustomReportParameters.PAGE_FOOTER_TEXT.jasperName, footerText)
            report.setParameter(CustomReportParameters.PRINT_PAGE_FOOTER_TEXT.jasperName, !!footerText)
        } else if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            report.setPageFormat(Integer.MAX_VALUE, Integer.MAX_VALUE, PageOrientation.PORTRAIT)
            addMaxXLSXRowsPageBreak(report)
        } else if (params.outputFormat == ReportFormatEnum.DOCX.name() || params.outputFormat == ReportFormatEnum.PDF.name() || params.outputFormat == ReportFormatEnum.PPTX.name()) {
            String footerText =  ''

            def sensitivityLabel = getSensitivityLabel(params)

            if (params.pageOrientation || params.paperSize) {
                report.setPageFormat(getPaperSize(params), getPageOrientation(params))
                report.setPageMargin(Templates.getPageMargin(getPageOrientation(params)))
            }
        }
    }

    private String getSensitivityLabel(Map params) {
        def sensitivityLabel

        if (params.sensitivityLabel) {
            sensitivityLabel = SensitivityLabelEnum.(params.sensitivityLabel).imageName
        } else {
            sensitivityLabel = SensitivityLabelEnum.CONFIDENTIAL.imageName
        }
        sensitivityLabel
    }

    private PageOrientation getPageOrientation(Map params) {
        def pageOrientation

        if (params.pageOrientation) {
            pageOrientation = PageOrientation.(params.pageOrientation)
        } else {
            pageOrientation = PageOrientation.LANDSCAPE
        }

        pageOrientation
    }

    private PageType getPaperSize(Map params) {
        def paperSize

        if (params.paperSize) {
            paperSize = PageType.(params.paperSize)
        } else {
            paperSize = PageType.LETTER
        }
        paperSize
    }

    private int getTemplateQueryFooterRowCount(String templateQueryFooter) {
        int count = 0
        String[] rows = templateQueryFooter.split("\\r\\n?|\\n")
        for (String row : rows) {
            count += Math.ceil((double)row.length() / TEMPLATE_QUERY_FOOTER_MAX_SYMBOL_IN_ROW)
        }
        return count
    }

    private void addMaxXLSXRowsPageBreak(JasperReportBuilder report) {
        int maxRowsPerSheet = OutputBuilder.XLSX_MAX_ROWS_PER_SHEET - XSLX_HEADER_ROWS_GAP
        BreakBuilder pageBreak = cmp.pageBreak()
        pageBreak.setPrintWhenExpression(exp.jasperSyntax("\$V{PAGE_COUNT} >= ${maxRowsPerSheet}"))
        report.detail(pageBreak)
    }
}
