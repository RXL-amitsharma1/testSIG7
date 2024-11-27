package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.Constants
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.dynamicReports.JasperReportBuilderEntry
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.SignalJasperReportBuilder
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.SensitivityLabelEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.VerticalAlignment
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class ProductRuleReportBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")

    public void createReport(List productRuleList, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            params.showCompanyLogo = true
            JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
            criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
            criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
            jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            if(params.outputFormat == ReportFormat.XLSX.name())
                params.showCompanyLogo = false
            JasperReportBuilder reportSheet = buildReport(productRuleList,  params)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Aggregate Alert Product Type Configuration"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }
    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder criteriaDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, true, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
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
                    addCriteriaHeaderTable(criteriaList, it.label, it.value)
                }
            }
        }
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(criteriaList))
        JasperReportBuilder report = criteriaDataReport.summary(verticalList)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report
    }

    private void addCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(14).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }

    private JasperReportBuilder buildReport(List productRuleList, Map params) {
        VerticalListBuilder verticalList = cmp.verticalList()
        params.showCompanyLogo = true
        params.showLogo = true
        if(params.outputFormat  == ReportFormat.XLSX.name()) {
            params.showCompanyLogo = false
            params.showLogo = false
        }


        JasperReportBuilder productRuleReportBuilder = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        def dataSource = new JRMapCollectionDataSource(productRuleList)
        productRuleReportBuilder.setDataSource(dataSource)
        addColumns(productRuleReportBuilder, productRuleList)

        setPrintablePageHeader(productRuleReportBuilder, "", false, params)
        dynamicReportService.noDataCheck(dataSource, productRuleReportBuilder)
        verticalList.add(cmp.subreport(productRuleReportBuilder))
        verticalList.add(cmp.verticalGap(20))

        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        if(Holders.config.signal.confidential.logo.enable && !(params.outputFormat == ReportFormat.XLSX.name())){
            ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName)).setFixedDimension(80, 36)
            HorizontalListBuilder confidentialImage = cmp.horizontalList(cmp.horizontalGap((PageType.LEDGER.getHeight() - 130)),img)
                    .setStyle(stl.style().setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM).setTopPadding(45))
            report.pageFooter(confidentialImage)
            report.setPageFooterStyle(stl.style().setBottomPadding(10).setHorizontalAlignment(HorizontalAlignment.CENTER))
        }
        report
    }

    public String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params) {
        if (params.showCompanyLogo) {
            ImageBuilder img = cmp.image(imageService.getImage("company-logo.png"))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            report.pageHeader(cmp.verticalList(img))
        }
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)
        def reportTemplate = report.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)
        if(Holders.config.signal.confidential.logo.enable && !(params.outputFormat == ReportFormat.XLSX.name())){
            ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName)).setFixedDimension(80, 36)
            HorizontalListBuilder confidentialImage = cmp.horizontalList(cmp.horizontalGap((PageType.LEDGER.getHeight() - 150)),img)
                    .setStyle(stl.style().setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM).setTopPadding(50))
            report.pageFooter(confidentialImage)
            report.setPageFooterStyle(stl.style().setBottomPadding(10).setHorizontalAlignment(HorizontalAlignment.CENTER))
        }
        return setPageHeaderText(report, customReportHeader, false, params)
    }

    private void addColumns(JasperReportBuilder report, List<Map> productRuleList) {

        report.addColumn(Columns.column("Name", "name", type.stringType()))
                .addColumn(Columns.column("Product Type", "productType", type.stringType()))
                .addColumn(Columns.column("Role", "roleType", type.stringType()))
    }

    private String setPageHeaderText(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params) {
        String header = customReportHeader
        String title = "Aggregate Alert Product Type Configuration"
        String printDateRange = "false"
        TextFieldBuilder textFieldTitle = cmp.text(title).setStyle(Templates.pageHeader_TitleStyle)
        TextFieldBuilder textFieldDateRange = null
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)
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
