package com.rxlogix.dynamicReports

import com.rxlogix.*
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.SensitivityLabelEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.jasper.constant.JasperProperty
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.MarginBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.builder.style.TemplateStylesBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.Markup
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.VerticalAlignment
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import java.util.List

import java.awt.*

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CaseDetailReportBuilder {

    UserService userService = Holders.applicationContext.getBean("userService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    DataObjectService dataObjectService = Holders.applicationContext.getBean("dataObjectService")
    public static final StyleBuilder columnTitleStyle
    public static final StyleBuilder printableRootStyle
    public static final light_black = new Color(51, 51, 51)
    public static final grey = new Color(220, 220, 220)
    public static final blue = new Color(31,78,120)
    public static final white = new Color(255, 255, 255)
    static final String COLUMN_TITLE_CSS_CLASS = "column-title"

    static{
        printableRootStyle = stl.style().setFontSize(9).setBottomPadding(0).setTopPadding()
        columnTitleStyle = stl.style(printableRootStyle).setBold(true)
                .setForegroundColor(light_black)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
                .setPadding(4)
    }

    TemplateStylesBuilder templateStyles = stl.templateStyles()

    void createReport(Map caseMultiMap, JRDataSource dataSourceCaseAttachments,
                             JRDataSource dataSourceCaseComments, JRDataSource dataSourceCaseActions, JRDataSource dataSourceCaseHistory,
                             JRDataSource dataSourceSuspectProductHistory, Map params,
                             ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params, false)
                criteriaJasperReportBuilderEntry.excelSheetName = customMessageService.getMessage('app.label.criteria', null, "Criteria", Locale.default)
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }

            JasperReportBuilder reportSheet = buildReport(caseMultiMap, dataSourceCaseAttachments, dataSourceCaseComments, dataSourceCaseActions,
                    dataSourceCaseHistory, dataSourceSuspectProductHistory, params)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.CaseDetailReportBuilder.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params, Boolean isSubreport) {
        JasperReportBuilder criteriaDataReport = ReportBuilder.initializeNewReport(params.portraitType, isSubreport, true, (params.outputFormat == ReportFormat.XLSX.name()),PageType.LEDGER, PageOrientation.LANDSCAPE)
        HorizontalListBuilder criteriaList = cmp.horizontalList()
        params.showCompanyLogo = true
        params.showLogo = true

        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaMap) {
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            criteriaList.add(cmp.verticalList(img)).newRow()
            criteriaList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            criteriaList.add(cmp.verticalList(filler))
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                criteriaMap.each {
                    criteriaList.add(cmp.text(it.label).setFixedColumns(14).setStyle(Templates.criteriaNameStyle),
                            cmp.text(it.value).setStyle(Templates.criteriaValueStyle)).newRow()
                }
            }
        }
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(criteriaList))
        JasperReportBuilder report = criteriaDataReport.summary(verticalList)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report
    }

    private void addCaseDetailsColumns(JasperReportBuilder report, Map sectionMap, def params) {
        List<TextColumnBuilder> columns = []
        sectionMap.each {
            TextColumnBuilder<String> column
            if(it.key.contains(Constants.CaseInforMapFields.CASE_NARRATIVE)) {
                column = createColumn(Constants.CaseInforMapFields.CASE_NARRATIVE, it.key)
            } else {
                column = createColumn(it.key, it.key)
            }
            if (it.key == Constants.CaseDetailUniqueName.REFERENCE_NUMBER && params.outputFormat != ReportFormat.XLSX.name()) {
                column.setTitle(exp.jasperSyntax("\"${column.name}\""))
                StyleBuilder columnStyle = getOrCreateColumnStyle(column)
                if(columnStyle){
                    columnStyle.setMarkup(Markup.HTML)
                    columnStyle.setForegroundColor(new Color(51, 122, 183))
                    if(Holders.config.caseReference.isHyperlink == true) {
                        column.setValueFormatter(new HyperlinkColumnFormatter())
                    }
                }
            }
            columns.add(column)
        }
        report.columns(*columns)
    }

    private TextColumnBuilder createColumn(String columnLabel, String columnName) {
        TextColumnBuilder column = col.column(columnLabel, columnName, type.stringType())
        column.setTitle(exp.jasperSyntax("\"${columnLabel?.replaceAll('"','\\\\"')}\"")) //Added replaceAll to handle doublequotes text.
        column.addTitleProperty(JasperProperty.EXPORT_HTML_ID, exp.jasperSyntax("\"${columnName}\""))
        column.addTitleProperty(JasperProperty.EXPORT_HTML_CLASS, exp.jasperSyntax("\"${COLUMN_TITLE_CSS_CLASS}\""))
        return column
    }

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

    private JasperReportBuilder buildReport(Map caseMultiMap, JRDataSource dataSourcecaseAttachments, JRDataSource dataSourcecaseComments, JRDataSource dataSourcecaseActions,
            JRDataSource dataSourcecaseHistory, JRDataSource dataSourcesuspectProductHistory, Map params) {
        List reportList = []
        Boolean isExcelReport = params.outputFormat == ReportFormat.XLSX.name()
        caseMultiMap.remove("Versions")
        caseMultiMap.remove("versionNum")
        this.templateStyles = stl.templateStyles()

        HorizontalListBuilder content = cmp.horizontalList(Templates.pageNumberingInMidComponent)
        if (Holders.config.signal.confidential.logo.enable && params.outputFormat != ReportFormat.XLSX.name()) {
            ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            content = cmp.horizontalList(Templates.pageNumberingInMidComponent, img)
        }
        params.showCompanyLogo = false
        params.showLogo = false

        caseMultiMap.each {
            Map sectionMap = it.value[0]
            if (sectionMap?.containsValues) {
                JasperReportBuilder dataSourceCaseDetailReport = DynamicReports.report()
                dataSourceCaseDetailReport.setDataSource(new JRMapCollectionDataSource(caseMultiMap[it.key]))
                sectionMap.remove(Constants.CaseDetailFields.CONTAINS_VALUES)
                sectionMap.remove(Constants.CaseDetailFields.CHECK_SEQ_NUM)
                sectionMap.remove(Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO)
                addCaseDetailsColumns(dataSourceCaseDetailReport, sectionMap, params)
                if(it.key == Constants.CaseInforMapFields.CASE_INFORMATION || it.key == Constants.CaseInforMapFields.GENERAL_INFORMATION){
                    String header = customMessageService.getMessage("app.caseInfo.caseDetail.caseNumberAndFollowUp", params.caseVisibility ?: params.caseNumber, params.followUpNumber)
                    setPrintablePageHeader(dataSourceCaseDetailReport, header, false, params, it.key)
                    params.showCompanyLogo = false          //added fix for PVS-55060
                    params.showLogo = false                 //added fix for PVS-55060
                } else {
                    setPrintablePageHeader(dataSourceCaseDetailReport, "\n", false, params, it.key)
                }
                reportList.add(dataSourceCaseDetailReport)
            }
        }

        if(!params?.getBoolean("isChildCase") && (!params.isAdhocRun || params.isAdhocRun == 'false') && !params.containsKey('evdasCase') && params.alertId) {
            JasperReportBuilder dataSourceCaseAttachmentsReport = ReportBuilder.initializeNewReport(true,true,false,(isExcelReport), PageType.LEDGER, PageOrientation.LANDSCAPE)
            dataSourceCaseAttachmentsReport.setDataSource(dataSourcecaseAttachments)
            addAttachmentsColumns(dataSourceCaseAttachmentsReport)
            setPrintablePageHeader(dataSourceCaseAttachmentsReport, "\n", false, params, customMessageService.getMessage("app.label.attachments"))
            reportList.add(dataSourceCaseAttachmentsReport)

            if (Holders.config.icr.comments.enable) { // added flag as part of PVS-63960
                JasperReportBuilder dataSourceCaseCommentsReport = ReportBuilder.initializeNewReport(true, true, false, (isExcelReport), PageType.LEDGER, PageOrientation.LANDSCAPE)
                dataSourceCaseCommentsReport.setDataSource(dataSourcecaseComments)
                addCommentsColumns(dataSourceCaseCommentsReport)
                setPrintablePageHeader(dataSourceCaseCommentsReport, "\n", false, params, customMessageService.getMessage("app.label.comments"))
                reportList.add(dataSourceCaseCommentsReport)
            }

            JasperReportBuilder dataSourceCaseActionsReport = ReportBuilder.initializeNewReport(true,true,false,(isExcelReport), PageType.LEDGER, PageOrientation.LANDSCAPE)
            dataSourceCaseActionsReport.setDataSource(dataSourcecaseActions)
            addActionsColumns(dataSourceCaseActionsReport)
            setPrintablePageHeader(dataSourceCaseActionsReport, "\n", false, params, customMessageService.getMessage("product.assignment.label.actions"))
            reportList.add(dataSourceCaseActionsReport)
        }

        if(!params?.getBoolean("isChildCase") && (!params.isAdhocRun || params.isAdhocRun == 'false')) {
            JasperReportBuilder dataSourceCaseHistoryReport = ReportBuilder.initializeNewReport(true,true,false,(isExcelReport), PageType.LEDGER, PageOrientation.LANDSCAPE)
            dataSourceCaseHistoryReport.setDataSource(dataSourcecaseHistory)
            addHistoryColumns(dataSourceCaseHistoryReport)
            setPrintablePageHeader(dataSourceCaseHistoryReport, "\n", false, params, customMessageService.getMessage('caseDetails.review.history.current.alertProduct'))
            reportList.add(dataSourceCaseHistoryReport)

            JasperReportBuilder dataSourceSuspectProductHistoryReport = ReportBuilder.initializeNewReport(true,true,false,(isExcelReport), PageType.LEDGER, PageOrientation.LANDSCAPE)
            dataSourceSuspectProductHistoryReport.setDataSource(dataSourcesuspectProductHistory)
            addHistoryColumns(dataSourceSuspectProductHistoryReport)
            setPrintablePageHeader(dataSourceSuspectProductHistoryReport, "\n", false, params, customMessageService.getMessage('caseDetails.review.history.other.alert'))
            reportList.add(dataSourceSuspectProductHistoryReport)
        }

        JasperReportBuilder mainReport = ReportBuilder.initializeNewCaseDetailReport(true, false, false, isExcelReport)

        VerticalListBuilder verticalListBuilder = cmp.verticalList()

        if (!isExcelReport) {
            verticalListBuilder.add(cmp.verticalGap(10))
            verticalListBuilder.add(cmp.subreport(buildCriteriaReport(params.criteriaSheetList, params, true)))
            verticalListBuilder.add(cmp.verticalGap(10))
            verticalListBuilder.add(cmp.pageBreak())
        }

        reportList.each {
            verticalListBuilder.add(cmp.subreport(it))
        }

        mainReport.summary(verticalListBuilder)
        mainReport.pageFooter(content)
        mainReport.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)

        mainReport
    }

    private JasperReportBuilder buildReportAdhocRun(JRDataSource dataSourceCaseInfo,
                                            JRDataSource dataSourceProdInfo,
                                            JRDataSource dataSourceEventInfo,
                                            JRDataSource dataSourceProdEventInfo,
                                            JRDataSource dataSourcePatMedInfo,
                                            JRDataSource dataSourceCOD,
                                            JRDataSource dataSourceDosage,
                                            JRDataSource dataSourceLabResult,
                                            JRDataSource dataSourceNarrative,
                                            Map params) {
        JasperReportBuilder dataSourceCaseInfoReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceCaseInfoReport.setDataSource(dataSourceCaseInfo)
        addCaseInfoColumns(dataSourceCaseInfoReport, false)
        params.showCompanyLogo = true
        params.showLogo = true
        String header = "Case Details \n" +"Case Number : " + params.caseNumber + " - Version: " + params.version
        setPrintablePageHeader(dataSourceCaseInfoReport, header, false, params, "Case Information")

        params.showCompanyLogo = false
        params.showLogo = false

        JasperReportBuilder dataSourceProdInfoReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceProdInfoReport.setDataSource(dataSourceProdInfo)
        addProductInfoColumns(dataSourceProdInfoReport)

        setPrintablePageHeader(dataSourceProdInfoReport, "\n", false, params, 'Product Information')

        JasperReportBuilder dataSourceEventInfoReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceEventInfoReport.setDataSource(dataSourceEventInfo)
        addEventInfoColumns(dataSourceEventInfoReport)
        setPrintablePageHeader(dataSourceEventInfoReport, "\n", false, params, 'Event Information')

        JasperReportBuilder dataSourceProdEventInfoReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceProdEventInfoReport.setDataSource(dataSourceProdEventInfo)
        addProductEventInfoColumns(dataSourceProdEventInfoReport)
        setPrintablePageHeader(dataSourceProdEventInfoReport, "\n", false, params, 'Product Event Information')

        JasperReportBuilder dataSourcePatMedInfoReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourcePatMedInfoReport.setDataSource(dataSourcePatMedInfo)
        addPatMedColumns(dataSourcePatMedInfoReport)
        setPrintablePageHeader(dataSourcePatMedInfoReport, "\n", false, params, 'Patient Medical History')

        JasperReportBuilder dataSourceCODReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceCODReport.setDataSource(dataSourceCOD)
        addCODColumns(dataSourceCODReport)
        setPrintablePageHeader(dataSourceCODReport, "\n", false, params,'Cause Of Death Information')

        JasperReportBuilder dataSourceDosageReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceDosageReport.setDataSource(dataSourceDosage)
        addDosageRegimenColumns(dataSourceDosageReport)
        setPrintablePageHeader(dataSourceDosageReport, "\n", false, params, 'Dosage Regimen')

        JasperReportBuilder dataSourceLabResultReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceLabResultReport.setDataSource(dataSourceLabResult)
        addLabResultColumns(dataSourceLabResultReport)
        setPrintablePageHeader(dataSourceLabResultReport, "\n", false, params, 'Lab Results')

        JasperReportBuilder dataSourceNarrativeReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        dataSourceNarrativeReport.setDataSource(dataSourceNarrative)
        addNarrativeColumns(dataSourceNarrativeReport)
        setPrintablePageHeader(dataSourceNarrativeReport, "\n", false, params, 'Narrative')

        JasperReportBuilder mainReport = ReportBuilder.initializeNewReport(true, false, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        mainReport
                .summary(cmp.verticalList(cmp.subreport(dataSourceCaseInfoReport),
                cmp.subreport(dataSourceProdInfoReport),
                cmp.subreport(dataSourceEventInfoReport),
                cmp.subreport(dataSourceProdEventInfoReport),
                cmp.subreport(dataSourcePatMedInfoReport),
                cmp.subreport(dataSourceCODReport),
                cmp.subreport(dataSourceDosageReport),
                cmp.subreport(dataSourceLabResultReport),
                cmp.subreport(dataSourceNarrativeReport)))
        mainReport.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)

        mainReport
    }

    private void addCaseInfoColumns(JasperReportBuilder report , Boolean evdasCase) {
        report.addColumn(Columns.column("Case Number", "Case Number", type.stringType()))
        if (evdasCase)
            report.addColumn(Columns.column("EV Gateway Receipt Date", "EV Gateway Receipt Date", type.stringType()))
        else
            report.addColumn(Columns.column("Initial Receipt Date", "Initial Receipt Date", type.stringType()))

        report.addColumn(Columns.column("Latest followUp Date", "Latest followUp Date", type.stringType()))
                .addColumn(Columns.column("Report Type", "Report Type", type.stringType()))
                .addColumn(Columns.column("Country", "Country", type.stringType()))
                .addColumn(Columns.column("Seriousness", "Seriousness", type.stringType()))
                .addColumn(Columns.column("HCP", "HCP", type.stringType()))
                .addColumn(Columns.column("Reporter Country", "Reporter Country", type.stringType()))
                .addColumn(Columns.column("Pregnancy", "Pregnancy", type.stringType()))
                .addColumn(Columns.column("Age Group", "Age Group", type.stringType()))
    }

    private void addProductInfoColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Product Name", "Product Name", type.stringType()))
                .addColumn(Columns.column("Family Name", "Family Name", type.stringType()))
                .addColumn(Columns.column("Generic Name", "Generic Name", type.stringType()))
                .addColumn(Columns.column("Indication", "Indication", type.stringType()))
                .addColumn(Columns.column("Formulation", "Formulation", type.stringType()))
                .addColumn(Columns.column("Lot Number", "Lot Number", type.stringType()))
                .addColumn(Columns.column("Drug Type", "Drug Type", type.stringType()))
    }

    private void addEventInfoColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Event PT", "Event PT", type.stringType()))
                .addColumn(Columns.column("Reported Term", "Reported Term", type.stringType()))
                .addColumn(Columns.column("SOC", "SOC", type.stringType()))
                .addColumn(Columns.column("Onset Date", "Onset Date", type.stringType()))
                .addColumn(Columns.column("Seriousness Criteria", "Seriousness Criteria", type.stringType()))
                .addColumn(Columns.column("Event Outcome", "Event Outcome", type.stringType()))
                .addColumn(Columns.column("Onset Latency", "Onset Latency", type.stringType()))
    }

    private void addProductEventInfoColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Product Name", "Product Name", type.stringType()))
                .addColumn(Columns.column("Event PT", "Event PT", type.stringType()))
                .addColumn(Columns.column("Reported Term", "Reported Term", type.stringType()))
                .addColumn(Columns.column("Core Listedness", "Core Listedness", type.stringType()))
                .addColumn(Columns.column("IB Listedness", "IB Listedness", type.stringType()))
                .addColumn(Columns.column("Reported Causality", "Reported Causality", type.stringType()))
                .addColumn(Columns.column("Determined Causality", "Determined Causality", type.stringType()))
                .addColumn(Columns.column("Action Taken", "Action Taken", type.stringType()))
                .addColumn(Columns.column("Rechallenge", "Rechallenge", type.stringType()))
                .addColumn(Columns.column("Dechallenge", "Dechallenge", type.stringType()))
                .addColumn(Columns.column("Time To Onset (Days)", "Time To Onset (Days)", type.stringType()))
    }

    private void addPatMedColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Patient Condition Type", "Patient Condition Type", type.stringType()))
                .addColumn(Columns.column("Patient Medical Condition PT", "Patient Medical Condition PT", type.stringType()))
                .addColumn(Columns.column("Notes", "Notes", type.stringType()))
    }

    private void addCODColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Cause Of Death Coded", "Cause Of Death Coded", type.stringType()))
                .addColumn(Columns.column("Death Date", "Death Date", type.stringType()))
                .addColumn(Columns.column("Autopsy", "Autopsy", type.stringType()))
                .addColumn(Columns.column("Autopsy Results", "Autopsy Results", type.stringType()))
    }

    private void addDosageRegimenColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Product Name", "Product Name", type.stringType()))
                .addColumn(Columns.column("Therapy Start Date", "Therapy Start Date", type.stringType()))
                .addColumn(Columns.column("Therapy Stop Date", "Therapy Stop Date", type.stringType()))
                .addColumn(Columns.column("Therapy Duration", "Therapy Duration", type.stringType()))
                .addColumn(Columns.column("Ongoing", "Ongoing", type.stringType()))
                .addColumn(Columns.column("Dose", "Dose", type.stringType()))
                .addColumn(Columns.column("Dose Unit", "Dose Unit", type.stringType()))
                .addColumn(Columns.column("Daily Dose", "Daily Dose", type.stringType()))
                .addColumn(Columns.column("Lot Number", "Lot Number", type.stringType()))
                .addColumn(Columns.column("Expiry Date", "Expiry Date", type.stringType()))
    }

    private void addLabResultColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Lab Test Name", "Lab Test Name", type.stringType()))
                .addColumn(Columns.column("Test Date", "Test Date", type.stringType()))
                .addColumn(Columns.column("Lab Data Result", "Lab Data Result", type.stringType()))
                .addColumn(Columns.column("Lab Data Result Unit", "Lab Data Result Unit", type.stringType()))
                .addColumn(Columns.column("Normal High", "Normal High", type.stringType()))
                .addColumn(Columns.column("Normal Low", "Normal Low", type.stringType()))
                .addColumn(Columns.column("Lab Data Assessment", "Lab Data Assessment", type.stringType()))
    }

    private void addNarrativeColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Case Narrative", "Case Narrative", type.stringType()))
                .addColumn(Columns.column("Case Abbreviated Narrative", "Case Abbreviated Narrative", type.stringType()))
    }

    private void addAttachmentsColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column(customMessageService.getMessage("caseDetails.link"), "name", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("caseDetails.description"), "description", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("caseDetails.timestamp"), "timeStamp", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("caseDetails.modified.by"), "modifiedBy", type.stringType()))

    }


    private void addCommentsColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("", "comments", type.stringType()))
    }

    private void addActionsColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column(customMessageService.getMessage("app.action.id"), "id", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.action.list.type.label"), "type", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("caseDetails.action"), "action", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.details"), "details", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.due.date"), "dueDate", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.action.item.assigned.to"), "assignedTo", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("signal.history.label.status"), "status", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.action.list.completion.label"), "completionDate", type.stringType()))
    }

    private void addHistoryColumns(JasperReportBuilder report) {
        Boolean isCaseVersion = dataObjectService.getDataSourceMap(Constants.DbDataSource.IS_ARISG_PVIP)
        report.addColumn(Columns.column(customMessageService.getMessage("app.label.alert.name"), "alertName", type.stringType()))
                .addColumn(Columns.column( customMessageService.getMessage(isCaseVersion?"app.label.qualitative.details.column.caseNumber.version":"app.label.qualitative.details.column.caseNumber"),"caseNumber",type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.disposition"), "disposition", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.case.history.justification"), "justification", type.stringType()))
        if(Holders.config.alert.priority.enable){
            report.addColumn(Columns.column("Priority", "priority", type.stringType()))
        }
        report.addColumn(Columns.column(customMessageService.getMessage("app.label.alert.category"), "alertTags", type.listType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.subTag.column"), "alertSubTags", type.listType()))
                .addColumn(Columns.column(customMessageService.getMessage("signal.history.label.performedBy"), "updatedBy", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("signal.history.label.date"), "timestamp", type.stringType()))
    }

    String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params, String tableName) {

        if (params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0")&&params.showLogo) {
            ImageBuilder img = cmp.image(imageService.getImage("company-logo.png"))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))

            report.pageHeader(cmp.verticalList(img))
        }

        MarginBuilder margins = new MarginBuilder()
        margins.setTop(0)
        margins.setRight(25)
        margins.setLeft(25)
        margins.setBottom(1)
        report.setPageMargin(margins)

        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)

        def reportTemplate = report.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)

        return setPageHeaderText(report, customReportHeader, isCriteriaSheetOrAppendix, tableName)
    }

    private String setPageHeaderText(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, String tableName) {
        String header = customReportHeader
        String title

        if (!isCriteriaSheetOrAppendix) {
            title = tableName
        } else {
            title = ""
        }

        String printDateRange = "false"
        TextFieldBuilder textFieldTitle = cmp.text(title).setStyle(stl.style(columnTitleStyle).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM))
        TextFieldBuilder textFieldDateRange = null
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(0)

        if (header) {
            TextFieldBuilder textFieldHeader = cmp.text(header).setStyle(Templates.pageHeader_HeaderStyle)

            if (printDateRange == "true" && !isCriteriaSheetOrAppendix) {
                report.pageHeader(cmp.verticalList(textFieldHeader, textFieldTitle, textFieldDateRange).add(filler))
            } else {
                report.pageHeader(cmp.verticalList(textFieldHeader, textFieldTitle).add(filler))
            }
        } else {
            if (printDateRange == "true" && !isCriteriaSheetOrAppendix) {
                report.pageHeader(cmp.verticalList(textFieldTitle, textFieldDateRange).add(filler))
            } else {
                report.pageHeader(cmp.verticalList(textFieldTitle).add(filler))
            }
        }
        return title
    }
}

