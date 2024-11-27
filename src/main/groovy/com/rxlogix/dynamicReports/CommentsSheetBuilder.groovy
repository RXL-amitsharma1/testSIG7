package com.rxlogix.dynamicReports

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.config.ExecutedConfiguration
//import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.reportTypes.DateValueColumnFormatter
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.ReportFormatEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.col
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class CommentsSheetBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    private static final String DATE_CREATED_FORMAT = "dd-MM-yyyy HH:mm:ss"

    private static final String TEXT_DATA_FIELD = "textData"
    private static final String DATE_CREATED_FIELD = "dateCreated"
    private static final String DATE_LAST_UPDATED_FIELD = "lastUpdated"
    private static final String CREATED_BY_FIELD = "createdBy"
    private static final String MODIFIED_BY_FIELD = "modifiedBy"

    public void createReportCommentsSheet(ExecutedConfiguration executedConfigurationInstance, ReportResult reportResult, Map params,
                               ExecutedTemplateQuery executedTemplateQuery,
                               ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params) && !executedConfigurationInstance?.comments?.empty) {
            JasperReportBuilder commentsSheet = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType._11X17,  PageOrientation.LANDSCAPE)

            String header = customMessageService.getMessage("jasperReports.reportCommentsSheet")
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(executedConfigurationInstance, params, commentsSheet, executedTemplateQuery, header, true)
            buildCommentsSheet(commentsSheet, new JRBeanCollectionDataSource(executedConfigurationInstance.comments), params)
            footerBuilder.setFooter(params, commentsSheet, executedTemplateQuery, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = commentsSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    public void createSectionCommentsSheet(ExecutedConfiguration executedConfigurationInstance, ReportResult reportResult, Map params,
                                           ExecutedTemplateQuery executedTemplateQuery,
                                           ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params) && !reportResult.comments?.empty) {
            JasperReportBuilder commentsSheet = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType._11X17,  PageOrientation.LANDSCAPE)

            String header = customMessageService.getMessage("jasperReports.sectionCommentsSheet")
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(executedConfigurationInstance, params, commentsSheet, executedTemplateQuery, header, true)
            buildCommentsSheet(commentsSheet, new JRBeanCollectionDataSource(reportResult.comments), params)
            footerBuilder.setFooter(params, commentsSheet, executedTemplateQuery, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = commentsSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildCommentsSheet(JasperReportBuilder report, JRDataSource dataSource, Map params) {
        report.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
        TextColumnBuilder textDataColumn = col.column(customMessageService.getMessage("app.label.commentsTextData"), TEXT_DATA_FIELD, type.stringType())
        TextColumnBuilder dateCreatedColumn = col.column(customMessageService.getMessage("app.label.commentsDateCreated"), DATE_CREATED_FIELD, type.dateType())
        TextColumnBuilder createdByColumn = col.column(customMessageService.getMessage("app.label.commentsCreatedBy"), CREATED_BY_FIELD, type.stringType()).setWidth(20)

        dateCreatedColumn.setPattern(DATE_CREATED_FORMAT)
        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            dateCreatedColumn.setValueFormatter(new DateValueColumnFormatter(DATE_CREATED_FORMAT))
        } else {
            textDataColumn.setWidth(75)
            dateCreatedColumn.setWidth(15)
            createdByColumn.setWidth(20)
        }
        report.addColumn(textDataColumn, dateCreatedColumn, createdByColumn)
        report.setDataSource(dataSource)
        report.setPageFormat(PageType._11X17, PageOrientation.LANDSCAPE)
    }
}