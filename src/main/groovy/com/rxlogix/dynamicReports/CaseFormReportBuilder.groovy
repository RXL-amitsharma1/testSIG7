package com.rxlogix.dynamicReports


import com.rxlogix.*
import com.rxlogix.Constants
import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.dynamicReports.reportTypes.*
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SensitivityLabelEnum
import com.rxlogix.signal.CaseForm
import com.rxlogix.util.FileUtil
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.jasper.constant.JasperProperty
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.MarginBuilder
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.*
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.expression.ValueExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.*
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRField
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import net.sf.jasperreports.engine.design.JasperDesign
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer
import net.sf.jasperreports.engine.util.JRSwapFile
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringEscapeUtils
import org.grails.web.json.JSONObject
import org.springframework.context.i18n.LocaleContextHolder

import java.awt.*
import java.util.List

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CaseFormReportBuilder implements SpecificReportTypeBuilder, SpecificTemplateTypeBuilder {

    ImageService imageService = Holders.applicationContext.getBean("imageService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    public static final StyleBuilder columnTitleStyle
    public static final StyleBuilder printableRootStyle
    public static final dark_blue = new Color(91, 163, 197)
    public static final light_blue = new Color(212, 233, 239)
    public static final light_black = new Color(51, 51, 51)
    public static final grey = new Color(220, 220, 220)
    public static final blue = new Color(0, 113, 165)
    public static final white = new Color(255, 255, 255)
    static final String COLUMN_TITLE_CSS_CLASS = "column-title"
    private static final String DEFAULT_TEMPLATE_SET_DATA_DIR = "signal"

    static {
        printableRootStyle = stl.style().setFontSize(9).setBottomPadding(0).setTopPadding()
        columnTitleStyle = stl.style(printableRootStyle).setBold(true)
                .setForegroundColor(light_black)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
                .setPadding(4)

    }

     void createReport(ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList, Map caseFormMap, CaseForm caseForm,
                       Map criteriaMap, Map commentsMap, Map caseIdsNumMap, Map caseInfoMap, Map sectionRelatedInfo, Map prevCaseInfoMap, String outputFormat,Map reportDataExport) {
         JasperReportBuilder criteriaSheet = buildCriteriaReport(criteriaMap, outputFormat)
         criteriaSheet.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
         JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
         jasperReportBuilderEntry.jasperReportBuilder = criteriaSheet
         jasperReportBuilderEntry.excelSheetName = Constants.CriteriaSheetLabels.CRITERIA
         jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
         if (outputFormat != Constants.SignalReportOutputType.XLSX) {
             JasperReportBuilder reportSheet = null
             if (prevCaseInfoMap.isEmpty()) {
                 reportSheet = buildNoDataReport(outputFormat)
             } else {
                 reportSheet = buildReport(caseFormMap, caseForm, commentsMap, caseIdsNumMap, criteriaMap,
                         caseInfoMap, sectionRelatedInfo, prevCaseInfoMap, outputFormat)
             }
             jasperReportBuilderEntry = new JasperReportBuilderEntry()
             jasperReportBuilderEntry.jasperReportBuilder = reportSheet
             jasperReportBuilderEntry.excelSheetName = "case details"
             jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
         } else if (outputFormat == Constants.SignalReportOutputType.XLSX) {
             if (prevCaseInfoMap.isEmpty()) {
                 JasperReportBuilder reportSheet = buildNoDataReport(outputFormat)
                 JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                 jasperSubReportBuilderEntry.excelSheetName = "Case Form"
                 jasperSubReportBuilderEntry.jasperReportBuilder = reportSheet
                 jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
             } else {
                 for (entry in prevCaseInfoMap) {
                     if (reportDataExport[entry.key]) {
                         List<String> columnNames
                         columnNames = new ArrayList<String>(entry.value.keySet())
                         JasperReportBuilder subreport = initializeNewReport(true, true, outputFormat)
                         subreport.setDataSource(new JRMapCollectionDataSource(reportDataExport[entry.key])).setVirtualizer(new JRGzipVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize()))
                         String columnLabel = "Case Number"
                         if (columnLabel in columnNames) {
                             // this block is to remove case number column showing multiple times for case information section
                             columnNames.remove(columnLabel)
                         }
                         subreport.addColumn(Columns.column(columnLabel, columnLabel, type.stringType()))
                         println(columnNames)
                         columnNames.each {
                             if(it == "Case Narrative" || it == "Company Comment") {
                                 subreport.addColumn(Columns.column(it, it, type.stringType()).setFixedColumns(100))
                             } else {
                                 subreport.addColumn(Columns.column(it, it, type.stringType()))
                             }
                         }
                         setPrintablePageHeaderExcel(subreport)
                         JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                         jasperSubReportBuilderEntry.excelSheetName = entry.key
                         jasperSubReportBuilderEntry.jasperReportBuilder = subreport
                         jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                     }
                 }
             }
             if (reportDataExport.comments && reportDataExport.comments.size() > 0) {
                 JasperReportBuilder commentSubreport = initializeNewReport(true, true,outputFormat)
                 commentSubreport.setDataSource(new JRMapCollectionDataSource(reportDataExport.comments)).setVirtualizer(new JRGzipVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize()))
                 commentSubreport.addColumn(Columns.column("Case Number", "caseId", type.stringType()))
                 commentSubreport.addColumn(Columns.column("Comment", "comments", type.stringType()).setFixedColumns(500))
                 setPrintablePageHeaderExcel(commentSubreport)
                 JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                 jasperSubReportBuilderEntry.excelSheetName = "Comments"
                 jasperSubReportBuilderEntry.jasperReportBuilder = commentSubreport
                 jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
             }
         }
    }
    protected static void setPrintablePageHeaderExcel(JasperReportBuilder report) {
        report.ignorePagination()
        report.ignorePageWidth()
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnHeaderStyle(Templates.columnTitleStyleComments)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setPageMargin(margin(0))
        report.setColumnStyle(Templates.columnStyle)
    }


    private JasperReportBuilder buildReport(Map caseFormMap, CaseForm caseForm, Map commentsMap, Map caseIdsNumMap,
                                            def criteriaMap, Map caseInfoMap, Map sectionRelatedInfo, Map prevCaseInfoMap, String outputFormat) {

        Map params = [showCompanyLogo: false, showLogo: false]

        byte[] bytes = createByteFromData(caseFormMap, commentsMap, caseIdsNumMap)
        JasperReportBuilder dataSourceCaseDetailReport = report()
        dataSourceCaseDetailReport.setDataSource(createDataSource(bytes,caseInfoMap))
                .setTemplate(Templates.reportCaseTemplate.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE).setPageMargin(getPageMargin(PageOrientation.LANDSCAPE)))
                .setVirtualizer(new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(), new JRSwapFile(dynamicReportService.getReportsDirectory(), dynamicReportService.getBlockSize(), dynamicReportService.getMinGrowCount())))
        HeaderBuilder headerBuilder = new HeaderBuilder()
        params.outputFormat = outputFormat

        TemplateSetCsvDataSource mainDataSource = createDataSource(bytes, customMessageService.getMessage("app.label.case.number"))
        def subreports = []
        List fullTextFields = []
        sectionRelatedInfo.findAll { it?.value?.isFullText }?.each { key, val -> fullTextFields.add(key) }
        if (caseInfoMap && !caseInfoMap.isEmpty()) {
            for (entry in caseInfoMap) {
                List<String> columnNames
                columnNames = new ArrayList<String>(entry.value.keySet())
                JasperReportBuilder subreport = DynamicReports.report()
                subreport.setTemplate(Templates.reportCaseTemplate.setPageFormat(PageType.LETTER, PageOrientation.LANDSCAPE).setPageMargin(getPageMargin(PageOrientation.LANDSCAPE)))
                MarginBuilder margins = new MarginBuilder()
                margins.setTop(5)
                margins.setRight(25)
                margins.setLeft(25)
                margins.setBottom(5)
                subreport.addPageHeader(cmp.gap(10, 5))
                subreport.setPageMargin(margins)
                subreport.setDetailSplitType(SplitType.PREVENT)
                Map currFieldMap = entry.getValue()?.get(entry.getKey())
                if (currFieldMap && (currFieldMap?.fieldType in ["FullTextTypeField", "CommentField"])) {
                    subreport.setColumnHeaderStyle(Templates.columnTitleStyleComments)
                    subreport.setColumnTitleStyle(Templates.columnTitleStyleComments)
                    subreport.setColumnStyle(Templates.columnStyle)
                    setSubReportPrintablePageHeader(currFieldMap?.sectionName, subreport)
                } else {
                    setSubReportPrintablePageHeader(entry.key, subreport)
                }
                createSubReport(subreport, columnNames)
                subreports.add(cmp.subreport(subreport)
                        .setMinHeight(1)
                        .setDataSource(new SubreportDataSource(entry.key, mainDataSource, columnNames)))
            }
        }
        List<TextColumnBuilder> groupingColumns = getGroupingColumns()
        processColumnGrouping(groupingColumns, dataSourceCaseDetailReport)
        dataSourceCaseDetailReport.setDataSource(mainDataSource)
        dataSourceCaseDetailReport.detail(*subreports)
        headerBuilder.setHeader(caseForm?.executedConfiguration, params, dataSourceCaseDetailReport, null, false)
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.line())
        if(Holders.config.signal.confidential.logo.enable && outputFormat != Constants.SignalReportOutputType.XLSX){
            Map pageFormat = ReportBuilder.calculatePageWidth(pageOrientation, pageType)
            pageFormat.width = pageFormat.width - 60
            ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName)).setFixedDimension(80, 36)
            HorizontalListBuilder content = cmp.horizontalList(Templates.pageNumberingComponent, img)
            verticalList.add(content)
        }
        dataSourceCaseDetailReport.pageFooter(verticalList)
        dataSourceCaseDetailReport.setPageFooterStyle(stl.style().setBottomPadding(30).setTopPadding(5).setHorizontalAlignment(HorizontalAlignment.LEFT).setTopBorder(stl.pen1Point().setLineWidth(0.5 as Float).setLineColor(grey)))
        dataSourceCaseDetailReport
    }

    private void addCriteriaSheetHeaderTable(HorizontalListBuilder list, def labelId, def value) {
        if (value instanceof List) {
            value = value.join(',')
        }
        list.add(
                cmp.text(labelId + ':').setFixedColumns(25).setStyle(Templates.criteriaNameStyle.setPadding(5)),
                cmp.text(value).setStyle(Templates.criteriaValueStyle.setPadding(5))).newRow()
    }

    protected String getReportHeader(Map params) {
        return customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
    }

    protected PageType getPageType() {
        return PageType.LETTER
    }

    protected PageOrientation getPageOrientation() {
        return PageOrientation.LANDSCAPE
    }

    JasperReportBuilder initializeNewReport(Boolean portraitType = true, Boolean isSubreport = true, String outputFormat, Boolean isNewCase = false) {

        JasperReportBuilder report = report()
        report.setLocale(LocaleContextHolder.getLocale())
        ReportTemplateBuilder reportTemplateBuilder = Templates.reportTemplate
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.line())
        if(Holders.config.signal.confidential.logo.enable && outputFormat != ReportFormat.XLSX.name()){
            Map pageFormat = ReportBuilder.calculatePageWidth(pageOrientation, pageType)
            pageFormat.width = pageFormat.width - 20
            ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName)).setFixedDimension(80, 36)
            HorizontalListBuilder content = cmp.horizontalList(Templates.pageNumberingComponent, img)
            verticalList.add(content)
        }
        report.setTemplate(reportTemplateBuilder)
        MarginBuilder margins = new MarginBuilder()
        margins.setTop(0)
        margins.setRight(10)
        margins.setLeft(10)
        report.addPageHeader(cmp.gap(10, 5))
        reportTemplateBuilder.setSummarySplitType(SplitType.IMMEDIATE)
        reportTemplateBuilder.setSummaryWithPageHeaderAndFooter(true)

        if(!isSubreport && outputFormat == ReportFormat.XLSX.name()){
            report.setPageFooterStyle(stl.style().setBottomPadding(10).setHorizontalAlignment(HorizontalAlignment.CENTER))
        }
        if (isSubreport)
            margins.setBottom(10)
        else {
            margins.setBottom(0)
            report.pageFooter(verticalList)
            report.setPageFooterStyle(stl.style().setBottomPadding(30).setTopPadding(5).setHorizontalAlignment(HorizontalAlignment.LEFT).setTopBorder(stl.pen1Point().setLineWidth(0.5 as Float).setLineColor(grey)))
        }
        report.setPageMargin(margins)
        return report

    }

    private JRDataSource createDataSource(byte[] bytes,Map caseInfoMap) {

        return new JREmptyDataSource(0)
    }

    void executeTemplateSetReportSQLCSV(String key,Map sectionMap, File directoryToArchive,Map caseIdNumsMap, Map commentsMap) {
        try {
            sectionMap.each { tableName, tableRows ->
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
                OutputStreamWriter writer = new OutputStreamWriter(baos)
                try {
                    tableRows.each { row ->
                        Integer count = 0
                        row.each { header, val ->
                            String value = ""
                            if (val != null) {
                                value = val
                                value = StringEscapeUtils.escapeCsv(value.trim())
                            }
                            if (count > 0) {
                                writer.write(",")
                            }
                            writer.write(value)
                            count++
                        }
                        writer?.write("\n")
                    }
                }catch(Exception ex){
                    ex.printStackTrace()
                } finally {
                    writer?.flush()
                    writer?.close()
                    JSONObject groupingData = new JSONObject()
                    groupingData[customMessageService.getMessage("app.label.case.number")] = caseIdNumsMap.get(key)
                    File outputDir = new File(directoryToArchive, DEFAULT_TEMPLATE_SET_DATA_DIR)
                    File groupingFile = new File(outputDir, TemplateSetCsvDataSource.GROUPING_FILE_NAME)
                    if (!groupingFile.exists()) {
                        appendEntryToDir(outputDir, TemplateSetCsvDataSource.GROUPING_FILE_NAME, groupingData.toString().bytes)
                    }
                    String csvFile = tableName.substring(0, Math.min(251, tableName?.length())).replaceAll(" ", "_") + ".csv"
                    appendEntryToDir(outputDir, csvFile, baos.toByteArray())
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            OutputStreamWriter writer = new OutputStreamWriter(baos)
            String value = ""
            if (commentsMap?.containsKey(key as Long)) {
                value = commentsMap.get(key as Long).comments[0]
                value = StringEscapeUtils.escapeCsv(value)
            }
            writer.write(value)
            writer?.flush()
            writer?.close()
            String csvFile = "Comments.csv"
            File outputDir = new File(directoryToArchive, DEFAULT_TEMPLATE_SET_DATA_DIR)
            appendEntryToDir(outputDir, csvFile, baos.toByteArray())

        }
        catch (Exception ex) {
            println("Exception occurred in case form report template csv :=" + ex.getMessage())
            ex.printStackTrace()
        }
    }

    private void appendEntryToDir(File dir, String entryName, byte[] entryContent) throws IOException {
        dir.mkdir()
        File entryFile = new File(dir, entryName)
        entryFile.append(entryContent)
    }

    public static String generateRandomName() {
        return UUID.randomUUID().toString().replaceAll('-', '')
    }

    JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        return report.toJasperDesign()
    }

    protected void processColumnGrouping(List<TextColumnBuilder> columns, JasperReportBuilder report) {
        List<ColumnGroupBuilder> columnGroups = []
        List<ComponentBuilder> groupHeader = []
        columns.each {
            ColumnGroupBuilder singleColumnGroup = grp.group(it)
            singleColumnGroup.showColumnHeaderAndFooter();
            singleColumnGroup.setHeaderLayout(GroupHeaderLayout.EMPTY)
            groupHeader.add(cmp.text(new GroupTextExpression(singleColumnGroup))
                    .setMarkup(Markup.HTML)
            )

            TextFieldBuilder<String> bookmarkHeader = cmp.text(" ").setWidth(1)
            bookmarkHeader.setAnchorName(new GroupBookmarkExpression(singleColumnGroup))
            bookmarkHeader.setBookmarkLevel(2)
            groupHeader.add(bookmarkHeader)

            singleColumnGroup.addHeaderComponent(cmp.horizontalFlowList(*groupHeader.reverse()))
            singleColumnGroup.reprintHeaderOnEachPage()
            singleColumnGroup.setStartInNewPage(true)
            singleColumnGroup.setFooterSplitType(SplitType.IMMEDIATE)
            columnGroups.add(singleColumnGroup)
        }
        report.setShowColumnTitle(false)
        report.groupBy(*columnGroups)
    }

    private List<TextColumnBuilder> getGroupingColumns() {
            List<TextColumnBuilder> columns = []
            String columnLabel = customMessageService.getMessage("app.label.case.number")
            TextColumnBuilder column = createColumn(columnLabel, columnLabel)
            columns.add(column)
            columns
    }

    void setSubReportPrintablePageHeader(String header, JasperReportBuilder subreport) {
        List<ComponentBuilder> subReportHeader = []
        subReportHeader.add(cmp.text(header)
                .setStyle(Templates.subReportPageHeaderStyle)
        )
        TextFieldBuilder<String> bookmarkHeader = cmp.text(" ").setWidth(1)
        bookmarkHeader.setAnchorName(header)
        bookmarkHeader.setBookmarkLevel(3)
        subReportHeader.add(bookmarkHeader)

        subreport.pageHeader(cmp.horizontalFlowList(*subReportHeader.reverse()).newFlowRow().add(cmp.gap(5, 5)))
        subreport.setDefaultFont(Templates.defaultFontStyle)
        subreport.setColumnTitleStyle(Templates.columnTitleStyle)
        subreport.setColumnHeaderStyle(Templates.columnHeaderStyle)
        subreport.setColumnStyle(Templates.columnStyle)

        def reportTemplate = subreport.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)
    }

    private class GroupTextExpression extends AbstractSimpleExpression<String> {
        private ColumnGroupBuilder columnGroup

        GroupTextExpression(ColumnGroupBuilder columnGroup) {
            this.columnGroup = columnGroup
        }

        String evaluate(ReportParameters reportParameters) {
            StringBuilder sb = new StringBuilder()
            sb.append("<b>")
            if (columnGroup.group.titleExpression instanceof  ValueExpression) {
                sb.append(((ValueExpression) columnGroup.group.titleExpression).value)
            } else if (columnGroup.group.titleExpression instanceof  JasperExpression) {
                sb.append(((JasperExpression) columnGroup.group.titleExpression).expression.replace("\"", ""))
            }
            sb.append(": ")
            sb.append("</b>")
            sb.append(reportParameters.getFieldValue(columnGroup.group.valueField.valueExpression.name))
            return sb.toString();
        }
    }

    private class GroupBookmarkExpression extends AbstractSimpleExpression<String> {
        private ColumnGroupBuilder columnGroup

        GroupBookmarkExpression(ColumnGroupBuilder columnGroup) {
            this.columnGroup = columnGroup
        }

        String evaluate(ReportParameters reportParameters) {
            StringBuilder sb = new StringBuilder()
            if (columnGroup.group.titleExpression instanceof  ValueExpression) {
                sb.append(((ValueExpression) columnGroup.group.titleExpression).value)
            } else if (columnGroup.group.titleExpression instanceof  JasperExpression) {
                sb.append(((JasperExpression) columnGroup.group.titleExpression).expression.replace("\"", ""))
            }
            sb.append(": ")
            sb.append(reportParameters.getFieldValue(columnGroup.group.valueField.valueExpression.name))
            return sb.toString();
        }
    }

    private TemplateSetCsvDataSource createDataSource(byte[] bytes, String caseNumberColumnName) {
        if (bytes) {
            return new TemplateSetCsvDataSource(new BufferedInputStream(new ByteArrayInputStream(bytes)), caseNumberColumnName)
        }
        return new TemplateSetCsvDataSource(null, null)
    }

    private static class SubreportDataSource extends AbstractSimpleExpression<JRDataSource> {
        private TemplateSetCsvDataSource mainDataSource
        private List<String> columnNames
        private String key

        SubreportDataSource(String key,TemplateSetCsvDataSource mainDataSource,
                            List<String> columnNames) {
            this.mainDataSource = mainDataSource
            this.columnNames = columnNames
            this.key = key
        }

        JRDataSource evaluate(ReportParameters reportParameters) {
            return mainDataSource.getSubreportDataSource(key, columnNames)
        }
    }

    private JasperReportBuilder buildCriteriaReport(def criteriaMap,String outputFormat) {
        VerticalListBuilder verticalList = cmp.verticalList()
        JasperReportBuilder signalDataReport

        signalDataReport = initializeNewReport(false, false,outputFormat)
        signalDataReport.setPageFormat(getPageType(), getPageOrientation())


        def criteraSheet = {
            HorizontalListBuilder signalSummaryList = cmp.horizontalList()
            //Adding company logo
                ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                        .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
                signalSummaryList.add(cmp.verticalList(img)).newRow()
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)

            signalSummaryList.add(cmp.text(customMessageService.getMessage("jasperReports.criteriaSheet")).setStyle(Templates.pageHeader_TitleStyle)).newRow()

            signalSummaryList.add(cmp.verticalList(filler))
            criteriaMap.each {
                addCriteriaSheetHeaderTable(signalSummaryList, it.key, it.value)
            }
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalList(signalSummaryList))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.pageBreak())
        }
        criteraSheet()
        JasperReportBuilder newReport = signalDataReport.summary(verticalList)
        newReport
    }

    private JasperReportBuilder buildNoDataReport(String outputFormat) {
        VerticalListBuilder verticalList = cmp.verticalList()
        JasperReportBuilder signalDataReport

        signalDataReport = initializeNewReport(false, false,outputFormat)
        signalDataReport.setPageFormat(getPageType(), getPageOrientation())

            HorizontalListBuilder signalSummaryList = cmp.horizontalList()
            //Adding company logo
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            signalSummaryList.add(cmp.verticalList(img)).newRow()
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)

            signalSummaryList.add(cmp.text(customMessageService.getMessage("jasperReports.caseForm")).setStyle(Templates.pageHeader_TitleStyle)).newRow()
            signalSummaryList.add(cmp.verticalList(filler))
            signalSummaryList.add(cmp.text(customMessageService.getMessage("jasperReports.noCaseFormFound")).setStyle(Templates.criteriaNameStyle.setPadding(5))).newRow()

            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalList(signalSummaryList))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.pageBreak())
            signalDataReport.summary(verticalList)
            signalDataReport
    }

    void createSubReport(JasperReportBuilder report, def columnsName){
        this.templateStyles = stl.templateStyles()
        if(columnsName.contains(customMessageService.getMessage("app.label.case.number"))) {
            FieldBuilder field = field(customMessageService.getMessage("app.label.case.number"), type.stringType())
            report.addField(field)
        }
        List<TextColumnBuilder> columns = addCaseLineListingColumns(report, columnsName)
        report.columns(*columns)
    }

    List addCaseLineListingColumns(JasperReportBuilder report, def selectedColumns) {
        List columns = []
        selectedColumns.each {
            TextColumnBuilder<String> column = createColumn(it, it)
            if (it == Constants.CaseDetailUniqueName.REFERENCE_NUMBER) {
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
        return columns
    }

    public MarginBuilder getPageMargin(PageOrientation pageOrientation) {
        if (pageOrientation == PageOrientation.LANDSCAPE) {
            return margin().setLeft(27).setBottom(27).setRight(27).setTop(15)// 3/8, 3/8, 3/8, 3/4 inches
        } else {
            return margin().setLeft(54).setBottom(27).setRight(27).setTop(27)// 3/4, 3/8, 3/8, 3/8 inches
        }
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

    private TextColumnBuilder createColumn(String columnLabel, String columnName) {
        TextColumnBuilder column = col.column(columnLabel, columnName, type.stringType())
        column.setTitle(exp.jasperSyntax("\"${columnLabel?.replaceAll('"','\\\\"')}\"")) //Added replaceAll to handle doublequotes text.
        column.addTitleProperty(JasperProperty.EXPORT_HTML_ID, exp.jasperSyntax("\"${columnName}\""))
        column.addTitleProperty(JasperProperty.EXPORT_HTML_CLASS, exp.jasperSyntax("\"${COLUMN_TITLE_CSS_CLASS}\""))
        StyleBuilder columnStyle = getOrCreateColumnStyle(column)
        return column
    }

    private byte[] createByteFromData(Map caseFormMap, Map commentsMap, Map caseIdsNumMap) {
        File tempFile = null
        byte[] bytes = null
        try {
            File directoryToArchive = new File("${Holders.config.tempDirectory as String}${generateRandomName()}")
            if (directoryToArchive.exists()) {
                FileUtils.deleteDirectory(directoryToArchive)
            }
            directoryToArchive.mkdir()
            caseFormMap.each { key, value ->
                executeTemplateSetReportSQLCSV(key, value, directoryToArchive, caseIdsNumMap, commentsMap)
                    directoryToArchive.eachDir {
                        try {
                            File caseTarFile = new File(directoryToArchive, "${key}.tar.gz")
                            FileUtil.compressFiles(it.listFiles().toList(), caseTarFile)
                            println("File Name to Delete is := " + it)
                            FileUtils.deleteDirectory(it)
                        }catch (IOException e) {
                            System.err.println("Error deleting directory: " +it)
                            e.printStackTrace()
                        }
                    }
            }
            String directory = "${Holders.config.tempDirectory as String}${generateRandomName()}"
            tempFile = new File("${directory}.tar.gz")

            FileUtil.compressFiles(directoryToArchive.listFiles().toList().sort(), tempFile)
            FileUtils.cleanDirectory(directoryToArchive)
            directoryToArchive.delete()
            bytes = tempFile.bytes
        } catch(Exception ex){
            ex.printStackTrace()
        } finally{
            tempFile.delete()
        }
        return bytes
    }
}

