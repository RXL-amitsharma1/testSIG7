package com.rxlogix.dynamicReports

import com.rxlogix.ConfigurationService
import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.UserService
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportResult
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment

import java.text.SimpleDateFormat

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl

class CriteriaSheetBuilder {

    UserService userService = Holders.applicationContext.getBean("userService")
    ConfigurationService configurationService = Holders.applicationContext.getBean("configurationService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    public void createCriteriaSheet(ExecutedConfiguration executedConfigurationInstance, ReportResult reportResult, Map params,
                                     ExecutedTemplateQuery executedTemplateQuery,
                                     ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder criteriaSheet = buildCriteriaSheet(executedConfigurationInstance, reportResult, params)

            //todo:  get the header from setHeaderAndFooter return value?
            String header = customMessageService.getMessage("jasperReports.criteriaSheet")
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(executedConfigurationInstance, params, criteriaSheet, executedTemplateQuery, header, true)
            footerBuilder.setFooter(params, criteriaSheet, executedTemplateQuery, true)

            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = criteriaSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildCriteriaSheet(ExecutedConfiguration executedConfiguration, ReportResult reportResult, Map params) {
        JasperReportBuilder report = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()))
        CriteriaSheetBuilder criteriaSheetBuilder = new CriteriaSheetBuilder()
        report.summary(cmp.horizontalList().add(
                cmp.hListCell(criteriaSheetBuilder.createCriteriaSheetComponent(executedConfiguration, reportResult)))
        )
        report
    }

    public ComponentBuilder<?, ?> createCriteriaSheetComponent(ExecutedConfiguration executedConfigurationInstance, ReportResult reportResult) {
        HorizontalListBuilder reportCriteriaList = cmp.horizontalList()

        //Top Section
        addCriteriaSheetSectionTitle(reportCriteriaList, "app.reportCriteria.label")
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.name", executedConfigurationInstance.name)
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.description", executedConfigurationInstance.description?:"")
        addCriteriaSheetAttribute(reportCriteriaList, "app.productStudySelection.label", getProductStudySelectionValue(executedConfigurationInstance))
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.eventSelection", getEventSelectionValue(executedConfigurationInstance))
        addCriteriaSheetAttribute(reportCriteriaList, "app.label.DateRangeType", customMessageService.getMessage(executedConfigurationInstance.dateRangeType?.getI18nKey()))
        addCriteriaSheetAttribute(reportCriteriaList, "app.includeLockedCasesOnly.label",
                executedConfigurationInstance.includeLockedVersion? customMessageService.getMessage("app.yes.label"):
                        customMessageService.getMessage("app.no.label"))
        addCriteriaSheetAttribute(reportCriteriaList, "app.runDateAndTime.label", formatDateForCriteria(executedConfigurationInstance.nextRunDate))

        //Table section
        HorizontalListBuilder reportSectionsList = cmp.horizontalList().setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(30))
        addCriteriaSheetSectionTitle(reportSectionsList, "report.sections.criteria")
        reportSectionsList.add(cmp.text(customMessageService.getMessage("app.sectionTitle.label")).setStyle(Templates.horizontalListColumnTitleStyle))
        reportSectionsList.add(cmp.text(customMessageService.getMessage("app.templateName.label")).setStyle(Templates.horizontalListColumnTitleStyle))
        reportSectionsList.add(cmp.text(customMessageService.getMessage("app.queries.label")).setStyle(Templates.horizontalListColumnTitleStyle))
        reportSectionsList.add(cmp.text(customMessageService.getMessage("app.DateRange.label")).setStyle(Templates.horizontalListColumnTitleStyle))
        reportSectionsList.add(cmp.text(customMessageService.getMessage("app.versionasof.label")).setStyle(Templates.horizontalListColumnTitleStyle))
        reportSectionsList.newRow()

        List executedTemplateQueries = []

        if (reportResult) {
            executedTemplateQueries << reportResult.executedTemplateQuery
        } else {
            executedTemplateQueries = executedConfigurationInstance.executedTemplateQueries
        }

        for (ExecutedTemplateQuery executedTemplateQuery: executedTemplateQueries) {
            reportSectionsList.add(cmp.text(dynamicReportService.getNameAsTitle(executedConfigurationInstance, executedTemplateQuery)).setStyle(Templates.columnStyle))
            reportSectionsList.add(cmp.text(executedTemplateQuery.executedTemplate.name).setStyle(Templates.columnStyle))
            reportSectionsList.add(cmp.text(executedTemplateQuery?.executedQuery?.name?:"(No query)").setStyle(Templates.columnStyle))

            //todo:  these dates need to be obtained from a central place; the logic to determine what to display and the format of it is sitting in a GSP (criteria.gsp) -morett
            reportSectionsList.add(cmp.text(configurationService.getDateRangeValueForCriteria(executedTemplateQuery)).setStyle(Templates.columnStyle))
            reportSectionsList.add(cmp.text(formatDateForCriteria(executedTemplateQuery.executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate)).setStyle(Templates.columnStyle))

            reportSectionsList.newRow()
        }

        return cmp.verticalList(reportCriteriaList, reportSectionsList);
    }

    private void addCriteriaSheetSectionTitle(HorizontalListBuilder list, String labelId) {
        list.add(cmp.text(customMessageService.getMessage(labelId)).setStyle(Templates.criteriaSectionTitleStyle)).newRow()
    }

    private void addCriteriaSheetAttribute(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(customMessageService.getMessage(labelId) + ":").setFixedColumns(16).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.printableRootStyle)).newRow()
    }

    private String getProductStudySelectionValue(ExecutedConfiguration executedConfigurationInstance) {
        if (executedConfigurationInstance.productSelection) {
            return ViewHelper.getDictionaryValues(executedConfigurationInstance.productSelection, DictionaryTypeEnum.PRODUCT)
        } else if(executedConfigurationInstance.studySelection) {
            return ViewHelper.getDictionaryValues(executedConfigurationInstance.studySelection, DictionaryTypeEnum.STUDY)
        }
        return ""
    }

    private String getEventSelectionValue(ExecutedConfiguration executedConfigurationInstance) {
        if (executedConfigurationInstance.eventSelection) {
            return ViewHelper.getDictionaryValues(executedConfigurationInstance.eventSelection, DictionaryTypeEnum.EVENT)
        }
        return ""
    }

    private formatDateForCriteria (Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(customMessageService.getMessage("dateFormatFull"))
        sdf.setTimeZone(TimeZone.getTimeZone(ViewHelper.getTimeZone(userService.getUser())))
        return sdf.format(date)
    }
}
