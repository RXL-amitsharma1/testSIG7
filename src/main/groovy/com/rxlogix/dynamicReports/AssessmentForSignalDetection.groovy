package com.rxlogix.dynamicReports

import com.rxlogix.*
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.component.*
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.VerticalAlignment

import java.awt.*

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl

class AssessmentForSignalDetection {
    def messageSource
    UserService userService = Holders.applicationContext.getBean("userService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")
    ConfigurationService configurationService = Holders.applicationContext.getBean("configurationService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    public static final StyleBuilder columnTitleStyle
    public static final StyleBuilder sectionTitleStyle
    public static final StyleBuilder printableRootStyle
    public static final dark_blue = new Color(91,163,197)
    public static final light_blue = new Color(212,233,239)
    public static final light_black = new Color(51, 51, 51)
    public static final grey = new Color(220, 220, 220)
    public static final blue = new Color(31,78,120)
    public static final white = new Color(255, 255, 255)

    static {
        printableRootStyle = stl.style().setFontSize(9).setBottomPadding(0).setTopPadding()
        columnTitleStyle = stl.style(printableRootStyle).setBold(true)
                .setBorder(stl.pen1Point().setLineColor(grey))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
                .setPadding(0)
                .setTopPadding()
        sectionTitleStyle = stl.style(printableRootStyle).setBold(true)
                .setBorder(stl.pen1Point().setLineColor(grey))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
                .setPadding(0)
                .setTopPadding()
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
    public void createReport(signalData, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
            criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
            criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
            jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            JasperReportBuilder reportSheet = buildReport(signalData, params)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Signal Detection Summary for Individual Case  Alert"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }
    private void createRowData(HorizontalListBuilder list, String firstCol, HorizontalListBuilder col) {
        list.add(cmp.text((firstCol)).setStyle(Templates.criteriaNameStyle),
                col).newRow()
    }
    private void createSmallRow(HorizontalListBuilder list, String firstCol, BigDecimal val) {
        def secondCol = val.toString()
        if(!(firstCol.contains('Counts'))){
            secondCol = val.toString() + "%"
        }
        list.add(cmp.text((firstCol)).setStyle(Templates.criteriaNameStyle),
                cmp.text(secondCol).setStyle(Templates.criteriaValueStyle).setStyle(Templates.criteriaNameStyle)).newRow()
    }

    private void createHeaderRow1(HorizontalListBuilder list, String firstCol, String secondCol, String thirdCol ){
        def thirdC = thirdCol.toString()
        StyleBuilder style = Templates.columnTitleStyle
        style.setHorizontalAlignment(HorizontalAlignment.CENTER)
        list.add(cmp.text(firstCol).setStyle(Templates.criteriaNameStyle),
                cmp.text(secondCol).setStyle(Templates.criteriaNameStyle),
                cmp.text(thirdC).setStyle(Templates.criteriaNameStyle))
                .setStyle(style)
                .newRow()
    }
    private JasperReportBuilder buildReport(signalData, Map params) {

        def header = "Assessment for Signal detection"

        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penThin())).setFixedHeight(1)
        params.portraitType = false
        JasperReportBuilder signalDataReport = ReportBuilder.initializeNewReport(params.portraitType as Boolean, false,false, (params.outputFormat == ReportFormat.XLSX.name()),PageType._11X17, PageOrientation.PORTRAIT)
        HorizontalListBuilder horizontalListHeader = cmp.horizontalList()
        createHeaderRow1(horizontalListHeader, "Characteristic", "Category", "Count of Cases %")
        params.showCompanyLogo = true
        params.showLogo = true


        HorizontalListBuilder signalSummaryList = cmp.horizontalList()
        if(params.outputFormat != Constants.SignalReportOutputType.XLSX){
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            signalSummaryList.add(cmp.verticalList(img)).newRow()
        }
        signalSummaryList.add(cmp.text(header).setStyle(Templates.pageHeader_TitleStyle)).newRow()
        signalSummaryList.add(cmp.verticalList(filler))

        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(signalSummaryList)
        verticalList.add(cmp.verticalGap(10))
        verticalList.add(horizontalListHeader)
        verticalList.add(cmp.verticalGap(10))

        generateVerticalList(signalData, filler, verticalList)

        verticalList.add(cmp.verticalGap(10))

        params.showCompanyLogo = false
        params.showLogo = false
        JasperReportBuilder report = signalDataReport.summary(verticalList)
        report.setPageFormat(PageType._11X17, PageOrientation.PORTRAIT)
        ReportTemplateBuilder reportTemplateBuilder = Templates.reportTemplate
        reportTemplateBuilder.setBackgroundStyle(new StyleBuilder().setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM).setTopPadding(750))
        report.setTemplate(reportTemplateBuilder)
    }

    public void generateVerticalList(signalData, FillerBuilder filler, VerticalListBuilder verticalList) {
        HorizontalListBuilder horizontalSmallListGender = cmp.horizontalList()
        HorizontalListBuilder horizontalListGender = cmp.horizontalList()
        def gender = signalData["GENDER"] ?: null
        gender.each { list ->
            list.each { key, value ->
                println "KEy value " + key + "  " + value
                createSmallRow(horizontalSmallListGender, key, value)
            }
        }
        createRowData(horizontalListGender, "GENDER", horizontalSmallListGender)
        horizontalListGender.add(cmp.verticalList(filler))

        //Age Group
        HorizontalListBuilder horizontalSmallListAgeGroup = cmp.horizontalList()
        HorizontalListBuilder horizontalListAgeGroup = cmp.horizontalList()
        def ageGroup = signalData["AGE GROUP"] ?: null
        ageGroup.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListAgeGroup, key, value)
            }
        }
        createRowData(horizontalListAgeGroup, messageSource.getMessage("app.label.assessment.details.age.group", null, locale), horizontalSmallListAgeGroup)
        horizontalListAgeGroup.add(cmp.verticalList(filler))

        // report type data
        HorizontalListBuilder horizontalSmallListReportType = cmp.horizontalList()
        HorizontalListBuilder horizontalListreportType = cmp.horizontalList()

        def reportType = signalData["REPORT TYPE"] ?: null
        reportType.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListReportType, key, value)
            }
        }
        createRowData(horizontalListreportType, messageSource.getMessage("app.label.assessment.details.source", null, locale), horizontalSmallListReportType)
        horizontalListreportType.add(cmp.verticalList(filler))

        // Report Source data
        HorizontalListBuilder horizontalSmallListReportSource = cmp.horizontalList()
        HorizontalListBuilder horizontalListReportSource = cmp.horizontalList()

        def reportSource = signalData["HCP CASE"] ?: null
        reportSource.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListReportSource, key, value)
            }
        }
        createRowData(horizontalListReportSource, messageSource.getMessage("app.label.assessment.details.hcp.case", null, locale), horizontalSmallListReportSource)
        horizontalListReportSource.add(cmp.verticalList(filler))

        // Country (most frequently reported)
        HorizontalListBuilder horizontalSmallListCountry = cmp.horizontalList()
        HorizontalListBuilder horizontalListCountry = cmp.horizontalList()

        def country = signalData["COUNTRY"] ?: null
        country.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListCountry, key, value)
            }
        }
        createRowData(horizontalListCountry, messageSource.getMessage("app.label.assessment.details.case.country", null, locale), horizontalSmallListCountry)
        horizontalListCountry.add(cmp.verticalList(filler))

        // Seriousness Serious
        HorizontalListBuilder horizontalSmallListSeriousness = cmp.horizontalList()
        HorizontalListBuilder horizontalListSeriousness = cmp.horizontalList()
        def seriousness = signalData["SERIOUSNESS CRITERIA"] ?: null
        seriousness.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListSeriousness, key, value)
            }
        }
        createRowData(horizontalListSeriousness, messageSource.getMessage("app.label.assessment.details.seriousness", null, locale), horizontalSmallListSeriousness)
        horizontalListSeriousness.add(cmp.verticalList(filler))

        //Action Taken with product
        HorizontalListBuilder horizontalSmallListAction = cmp.horizontalList()
        HorizontalListBuilder horizontalListAction = cmp.horizontalList()
        def actionTaken = signalData["ACTION TAKEN"] ?: null
        actionTaken.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListAction, key, value)
            }
        }
        createRowData(horizontalListAction, messageSource.getMessage("app.label.assessment.details.action.taken", null, locale), horizontalSmallListAction)
        horizontalListAction.add(cmp.verticalList(filler))

        //outcome data
        HorizontalListBuilder horizontalSmallListOutcome = cmp.horizontalList()
        HorizontalListBuilder horizontalListOutcome = cmp.horizontalList()
        def outcome = signalData["CASE OUTCOME"] ?: null
        outcome.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListOutcome, key, value)
            }
        }
        createRowData(horizontalListOutcome, messageSource.getMessage("app.label.assessment.details.case.outcome", null, locale), horizontalSmallListOutcome)
        horizontalListOutcome.add(cmp.verticalList(filler))

        //Rechallenge
        HorizontalListBuilder horizontalSmallListRechallenge = cmp.horizontalList()
        HorizontalListBuilder horizontalListRechallenge = cmp.horizontalList()
        def rechallenge = signalData["RECHALLENGE"] ?: null
        rechallenge.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListRechallenge, key, value)
            }
        }
        createRowData(horizontalListRechallenge, messageSource.getMessage("app.label.assessment.details.rechallenge", null, locale), horizontalSmallListRechallenge)
        horizontalListRechallenge.add(cmp.verticalList(filler))

        //Dechallenge
        HorizontalListBuilder horizontalSmallListDechallenge = cmp.horizontalList()
        HorizontalListBuilder horizontalListDechallenge = cmp.horizontalList()
        def dechallenge = signalData["DECHALLENGE"] ?: null
        dechallenge.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListDechallenge, key, value)
            }
        }
        createRowData(horizontalListDechallenge, messageSource.getMessage("app.label.assessment.details.dechallenge", null, locale), horizontalSmallListDechallenge)
        horizontalListDechallenge.add(cmp.verticalList(filler))

        //Route of administration
        HorizontalListBuilder horizontalSmallListRoute = cmp.horizontalList()
        HorizontalListBuilder horizontalListRoute = cmp.horizontalList()
        def route = signalData["ROUTE OF ADMINISTRATION"] ?: null
        route.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListRoute, key, value)
            }
        }
        createRowData(horizontalListRoute, messageSource.getMessage("app.label.assessment.details.patient.route", null, locale), horizontalSmallListRoute)
        horizontalListRoute.add(cmp.verticalList(filler))

        //Formulation
        HorizontalListBuilder horizontalSmallListFormulation = cmp.horizontalList()
        HorizontalListBuilder horizontalListFormulation = cmp.horizontalList()
        def formulation = signalData["FORMULATION"] ?: null
        formulation.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListFormulation, key, value)
            }
        }
        createRowData(horizontalListFormulation, messageSource.getMessage("app.label.assessment.details.dosage.form", null, locale), horizontalSmallListFormulation)
        horizontalListFormulation.add(cmp.verticalList(filler))

        //relevant medical history
        HorizontalListBuilder horizontalSmallListHistory = cmp.horizontalList()
        HorizontalListBuilder horizontalListHistory = cmp.horizontalList()
        def relevantHistory = signalData["RELEVANT HISTORY"] ?: null
        relevantHistory.each { list ->
            list.each { key, value ->
                createSmallRow(horizontalSmallListHistory, key, value)
            }
        }
        createRowData(horizontalListHistory, messageSource.getMessage("app.label.assessment.details.medical.history", null, locale), horizontalSmallListHistory)
        horizontalListHistory.add(cmp.verticalList(filler))




        verticalList.add(cmp.verticalList(horizontalListGender))
        verticalList.add(cmp.verticalList(horizontalListAgeGroup))
        verticalList.add(cmp.verticalList(horizontalListreportType))
        verticalList.add(cmp.verticalList(horizontalListReportSource))
        verticalList.add(cmp.verticalList(horizontalListCountry))
        verticalList.add(cmp.verticalList(horizontalListAction))
        verticalList.add(cmp.verticalList(horizontalListSeriousness))
        verticalList.add(cmp.verticalList(horizontalListOutcome))
        verticalList.add(cmp.verticalList(horizontalListRechallenge))
        verticalList.add(cmp.verticalList(horizontalListDechallenge))
        verticalList.add(cmp.verticalList(horizontalListRoute))
        verticalList.add(cmp.verticalList(horizontalListFormulation))
        verticalList.add(cmp.verticalList(horizontalListHistory))
    }

    public String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params, String tableName) {

        if (params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0")&&params.showLogo) {
            ImageBuilder img = cmp.image(imageService.getImage("company-logo.png"))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))

            report.pageHeader(cmp.verticalList(img))
        }

        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)

        def reportTemplate = report.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)

        return setPageHeaderText(report, customReportHeader, isCriteriaSheetOrAppendix, tableName)
    }

    private String setPageHeaderText(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, String tableName) {
        String header = customReportHeader
        String title = tableName
        String printDateRange = "false"
        TextFieldBuilder textFieldHeader
        TextFieldBuilder textFieldTitle
        TextFieldBuilder textFieldDateRange
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(0)
        if(title) {
            textFieldTitle = cmp.text(title).setStyle(stl.style(sectionTitleStyle).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM))
            textFieldDateRange = null

            if (header) {

                textFieldHeader = cmp.text(header).setStyle(Templates.pageHeader_HeaderStyle_left)

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

        }else {
            if (header) {
                textFieldDateRange = null
                textFieldHeader = cmp.text(header).setStyle(Templates.pageHeader_HeaderStyle_left)
                if (printDateRange == "true" && !isCriteriaSheetOrAppendix) {
                    report.pageHeader(cmp.verticalList(textFieldHeader, textFieldDateRange).add(filler))
                } else {
                    report.pageHeader(cmp.verticalList(textFieldHeader).add(filler))
                }
            }
        }


        return title
    }

    public String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params) {
        if (params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0")) {
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

    }

}
