package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.DynamicReportService
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.Templates
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.builder.style.TemplateStylesBuilder
import net.sf.dynamicreports.report.definition.datatype.DRIDataType

import static net.sf.dynamicreports.report.builder.DynamicReports.col
import static net.sf.dynamicreports.report.builder.DynamicReports.exp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type

/**
 * Created by gologuzov on 07.11.15.
 */
trait SpecificReportTypeBuilder {
    static final int MIN_COLSPAN_COLUMNS_WIDTH_XLSX = 100
    static final int MAX_CELL_LENGTH_XLSX = 32767
    static final String TRUNCATE_TEXT_XLSX = "...(truncated)"

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    TemplateStylesBuilder templateStyles = stl.templateStyles()

    private StyleBuilder getOrCreateColumnStyle(TextColumnBuilder column) {
        StyleBuilder columnStyle = templateStyles.getStyle(column.name)
        if (!columnStyle) {
            columnStyle = stl.style(Templates.columnStyle)
            columnStyle.setName(column.name)
            column.setStyle(columnStyle)
            templateStyles.addStyle(columnStyle)
        }
        return columnStyle
    }

    private TextColumnBuilder createColumn(String columnLabel, String columnName) {
        TextColumnBuilder column = col.column(columnLabel, columnName, type.stringType())
        column.setTitle(exp.jasperSyntax("\"${columnLabel}\""))
        StyleBuilder columnStyle = getOrCreateColumnStyle(column)
        return column
    }

    private static DRIDataType detectColumnType(ReportFieldInfo reportFieldInfo) {
        try {
            if (!reportFieldInfo.customExpression && !reportFieldInfo.commaSeparatedValue) {
                return type.detectType(reportFieldInfo.reportField.dataType)
            }
        } catch (Exception e) {
            // Using string for unknown field types
            return type.stringType()
        }
        return type.stringType()
    }

    def getAutoColumnWidth(Collection<Integer> columnWidthList) {
        def predefinedWidthCount = columnWidthList.findAll({ it != ReportFieldInfo.AUTO_COLUMN_WIDTH }).size()
        def autoColumnCount = columnWidthList.size() - predefinedWidthCount
        def autoColumnWidth = 0
        if (autoColumnCount > 0) {
            int totalWidth = columnWidthList.sum()
            if (totalWidth < 100) {
                autoColumnWidth = (100 - totalWidth) / autoColumnCount
            }
        }
        return autoColumnWidth
    }

    def getNormalizationFactor(Collection<Integer> columnWidthList) {
        def factor = 1.0
        int totalWidth = columnWidthList.sum()
        if (totalWidth > 100) {
            factor = (totalWidth - 100) / totalWidth
        }
        return factor
    }
}