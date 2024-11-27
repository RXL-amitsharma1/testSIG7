package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.config.ExecutedCaseLineListingTemplate
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.SuppressRepeatingColumnPrintWhenExpression
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.VariableBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.builder.expression.Expressions
import net.sf.dynamicreports.report.builder.expression.ValueExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.Calculation
import net.sf.dynamicreports.report.constant.GroupHeaderLayout
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.datatype.DRIDataType

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CaseLineListingReportBuilder {

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    public void createCaseLineListingReport(ReportResult reportResult, JasperReportBuilder report, Map params) {
        ExecutedCaseLineListingTemplate executedCaseLineListingTemplate = (ExecutedCaseLineListingTemplate) reportResult?.executedTemplateQuery?.executedTemplate
        List<ReportFieldInfo> selectedColumns = executedCaseLineListingTemplate.columnList.reportFieldInfoList

        processColumnGrouping(executedCaseLineListingTemplate, report, params)
        addCaseLineListingColumns(executedCaseLineListingTemplate, selectedColumns, report, params)
        processColspans(executedCaseLineListingTemplate, report, params)
        processSummary(executedCaseLineListingTemplate, report, params)
    }

    public void addCaseLineListingColumns(ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, List<ReportFieldInfo> selectedColumns, JasperReportBuilder report, Map params, int columnIndexOffset = 0) {
        List columns = makeSelectedColumnsList(executedCaseLineListingTemplate, selectedColumns, report, params, columnIndexOffset)
        report.columns(*columns)
    }

    /**
     * The method adds selected columns to list. Also it adds suppressing for selected columns
     * @param executedCaseLineListingTemplate Report template
     * @param report Report builder
     * @param params Report params
     * @param columnIndexOffset offset for adding columns
     * @return List of
     */
    protected List makeSelectedColumnsList(ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, List<ReportFieldInfo> selectedColumns, JasperReportBuilder report, Map params, int columnIndexOffset = 0) {
        List<String> fieldNameWithIndex = executedCaseLineListingTemplate?.getFieldNameWithIndex()

        ColumnGroupBuilder singleColumnGroup = null
        ConditionalStyleBuilder suppressRepeatingColumnCondition
        Boolean firstSuppressionColumnGrouped = false

        List columns = []
        for (int i = 0; i < selectedColumns?.size(); i++) {
            def reportFieldInfo = selectedColumns.get(i)
            ReportField reportField = reportFieldInfo.reportField
            String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name)
            if (reportFieldInfo.renameValue) {
                columnLabel = reportFieldInfo.renameValue
            }

            String columnValue = fieldNameWithIndex[i + columnIndexOffset]

            TextColumnBuilder<String> column = Columns.column(columnLabel, columnValue, detectColumnType(reportField));
            if (reportField.isDate()) {
                column.setPattern(reportField.dateFormat)
            }
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                column.setStyle(stl.style().setPadding(4))
            } else {
                if (reportFieldInfo.suppressRepeatingValues) {
                    if (!firstSuppressionColumnGrouped) {
                        singleColumnGroup = grp.group(col.column(columnLabel, columnValue, detectColumnType(reportField)))
                                .setHeaderLayout(GroupHeaderLayout.EMPTY)
                                .setHeaderStyle(Templates.emptyPaddingStyle)
                                .setFooterStyle(Templates.emptyPaddingStyle)
                                .setPadding(0)
                        report.groupBy(singleColumnGroup)
                        firstSuppressionColumnGrouped = true
                    }
                    suppressRepeatingColumnCondition = getSuppressRepeatingColumnCondition(singleColumnGroup, column.name)
                    StyleBuilder suppressRepeatingColumnStyle = getSuppressRepeatingColumnStyle(suppressRepeatingColumnCondition)
                    report.setDetailStyle(stl.style().setBorder(stl.pen1Point().setLineWidth(0.0 as Float)).conditionalStyles(suppressRepeatingColumnCondition))
                    column.setPrintWhenExpression(new SuppressRepeatingColumnPrintWhenExpression(singleColumnGroup, column.name))
                            .setStyle(suppressRepeatingColumnStyle)
                } else {
                    suppressRepeatingColumnCondition = getSuppressRepeatingColumnCondition(singleColumnGroup, column.name)
                    StyleBuilder suppressRepeatingColumnStyle = getSuppressRepeatingColumnStyle(suppressRepeatingColumnCondition)
                    column.setStyle(suppressRepeatingColumnStyle)
                }
            }
            columns.add(column);
        }
        return columns
    }

    private addGroupingColumns(ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, List<ReportFieldInfo> groupingColumns, List textColumnBuilderList, List columnGroupList) {
        List<String> fieldNameWithIndex = executedCaseLineListingTemplate?.getFieldNameWithIndex()
        int columnLength = executedCaseLineListingTemplate.columnList.reportFieldInfoList.size()
        List<ColumnGroupBuilder> columnGroups = []

        for (int i = 0; i < groupingColumns?.size(); i++) {
            def reportFieldInfo = groupingColumns.get(i)
            ReportField reportField = reportFieldInfo.reportField
            String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name)
            if (reportFieldInfo.renameValue) {
                columnLabel = reportFieldInfo.renameValue
            }
            TextColumnBuilder column = col.column(columnLabel, fieldNameWithIndex[i + columnLength], detectColumnType(reportField))
            if (reportField.isDate()) {
                column.setPattern(reportField.dateFormat)
            }
            textColumnBuilderList.add(column)
            ColumnGroupBuilder singleColumnGroup = grp.group(column)
            singleColumnGroup.setHeaderLayout(GroupHeaderLayout.EMPTY)

            columnGroups.add(singleColumnGroup)
            if (i == groupingColumns?.size() - 1) {
                singleColumnGroup.showColumnHeaderAndFooter();
                TextFieldBuilder<String> groupHeader = cmp.text(new GroupTextExpression(columnGroups))
                groupHeader.setStyle(stl.style().bold())
                singleColumnGroup.addHeaderComponent(groupHeader)
                singleColumnGroup.reprintHeaderOnEachPage()
                singleColumnGroup.setMinHeightToStartNewPage(60)
            }
            columnGroupList.add(singleColumnGroup)
        }

        [textColumnBuilderList, columnGroupList]
    }

    /**
     * The method adds colspan columns to list
     * @param executedCaseLineListingTemplate Report template
     * @param columnSpanColumns Colspan columns list from report template
     * @param colspanList Colspan column list
     * @return
     */
    private addColspanColumns(ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, List<ReportFieldInfo> columnSpanColumns, JasperReportBuilder report) {
        List<String> fieldNameWithIndex = executedCaseLineListingTemplate?.getFieldNameWithIndex()
        int columnLength = executedCaseLineListingTemplate.columnList.reportFieldInfoList.size()
        int groupingLength = executedCaseLineListingTemplate?.groupingList ? executedCaseLineListingTemplate.groupingList.reportFieldInfoList.size() : 0

        VerticalListBuilder colspanList = cmp.verticalList().setStyle(Templates.colspanStyle)

        for (int i = 0; i < columnSpanColumns?.size(); i++) {
            def reportFieldInfo = columnSpanColumns.get(i)
            ReportField reportField = reportFieldInfo.reportField
            String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name)
            if (reportFieldInfo.renameValue) {
                columnLabel = reportFieldInfo.renameValue
            }
            int columnIndex = i + columnLength + groupingLength
            String columnValue = fieldNameWithIndex[columnIndex]
            TextColumnBuilder column = col.column(columnLabel, columnValue, detectColumnType(reportField))

            HorizontalListBuilder singleColspan = cmp.horizontalFlowList()
            singleColspan.add(cmp.text(columnLabel).setStyle(stl.style().bold()).setMinDimension(20, 10))
            TextFieldBuilder comp = cmp.text(column.getColumn())
            if (reportField.isDate()) {
                comp.setPattern(reportField.dateFormat)
            }
            singleColspan.add(comp)

            if (reportFieldInfo.suppressRepeatingValues) {
                singleColspan.setStyle(Templates.colspanStyle)
                ColumnGroupBuilder singleColumnGroup = grp.group(col.column(columnLabel, columnValue, detectColumnType(reportField)))
                        .setHeaderLayout(GroupHeaderLayout.EMPTY)
                        .setHeaderStyle(Templates.emptyPaddingStyle)
                        .setFooterStyle(Templates.emptyPaddingStyle)
                        .setPadding(0)
                report.groupBy(singleColumnGroup)
                singleColumnGroup.addFooterComponent(singleColspan)
            } else {
                colspanList.add(singleColspan)
            }
        }

        return colspanList
    }

    protected void processColumnGrouping(ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, Map params) {
        int columnLength = executedCaseLineListingTemplate.columnList.reportFieldInfoList.size()
        List<ReportFieldInfo> groupingColumns = executedCaseLineListingTemplate?.groupingList?.reportFieldInfoList

        if (params.outputFormat == ReportFormat.XLSX.name()) {
            //Flatten for Excel
            addCaseLineListingColumns(executedCaseLineListingTemplate, groupingColumns, report, params, columnLength)
        } else {
            List columnGroupList = new ArrayList<ColumnGroupBuilder>()
            List textColumnBuilderList = new ArrayList<TextColumnBuilder>()

            (textColumnBuilderList, columnGroupList) = addGroupingColumns(executedCaseLineListingTemplate, report, groupingColumns, textColumnBuilderList, columnGroupList)

            if (columnGroupList) {
                report.setShowColumnTitle(false)
                report.groupBy(*columnGroupList)
                report.sortBy(*textColumnBuilderList)
            }

            if (executedCaseLineListingTemplate.pageBreakByGroup) {
                for (ColumnGroupBuilder columnGroupBuilder : columnGroupList) {
                    report.addGroupFooter(columnGroupBuilder, cmp.pageBreak())
                }
            }
        }
    }

    protected void processColspans(ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, Map params) {
        List<ReportFieldInfo> columnSpanColumns = executedCaseLineListingTemplate?.rowColumnList?.reportFieldInfoList
        int columnLength = executedCaseLineListingTemplate.columnList.reportFieldInfoList.size()
        int groupingLength = executedCaseLineListingTemplate?.groupingList ? executedCaseLineListingTemplate.groupingList.reportFieldInfoList.size() : 0
        if (columnSpanColumns) {
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                //Flatten for Excel
                addCaseLineListingColumns(executedCaseLineListingTemplate, columnSpanColumns, report, params, columnLength + groupingLength)
            } else {
                VerticalListBuilder colspanList = addColspanColumns(executedCaseLineListingTemplate, columnSpanColumns, report)
                report.addDetailFooter(colspanList)
            }
        }
    }

    protected void processSummary(ExecutedCaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, Map params) {
        if (executedCaseLineListingTemplate.columnShowTotal && params.outputFormat != ReportFormat.XLSX.name()) {
            // Total record count
            HorizontalListBuilder reportSummaryList = cmp.horizontalList()
            addSummaryAttribute(reportSummaryList, customMessageService.getMessage("app.label.totalRowsNumber"), Expressions.reportRowNumber())
            // Total unique case numbers count
            List<ReportFieldInfo> selectedColumns = executedCaseLineListingTemplate.columnList.reportFieldInfoList
            ReportFieldInfo caseNumberColumn = selectedColumns.find {"cm.CASE_NUM".equals(it.argusName)}
            if (caseNumberColumn) {
                int index = selectedColumns.indexOf(caseNumberColumn)
                List<String> fieldNameWithIndex = executedCaseLineListingTemplate?.getFieldNameWithIndex()
                FieldBuilder field = field(fieldNameWithIndex[index], detectColumnType(caseNumberColumn.reportField))
                VariableBuilder<Integer> caseNumberCount = variable(field, Calculation.DISTINCT_COUNT)
                addSummaryAttribute(reportSummaryList, customMessageService.getMessage("app.label.totalCaseNumber"), caseNumberCount)
            }
            report.addSummary(reportSummaryList)
        }
    }

    private void addSummaryAttribute(HorizontalListBuilder list, String label, def value) {
        list.add(cmp.text(label + ":").setFixedColumns(10).setStyle(stl.style().bold()),
                cmp.text(value).setStyle(stl.style())).newRow()
    }

    private static ConditionalStyleBuilder getSuppressRepeatingColumnCondition(ColumnGroupBuilder singleColumnGroup, String columnName) {
        stl.conditionalStyle(new SuppressRepeatingColumnPrintWhenExpression(singleColumnGroup, columnName))
                .setTopBorder(stl.pen1Point().setLineWidth(0.5 as Float).setLineColor(Templates.grey))
    }

    private static StyleBuilder getSuppressRepeatingColumnStyle(ConditionalStyleBuilder suppressRepeatingColumnCondition) {
        stl.style().conditionalStyles(suppressRepeatingColumnCondition).setPadding(4)
    }

    private static DRIDataType detectColumnType(ReportField reportField) {
        try {
            return type.detectType(reportField.dataType)
        } catch (Exception e) {
            // Using string for unknown field types
            return type.stringType()
        }
    }

    private class GroupTextExpression extends AbstractSimpleExpression<String> {
        private List<ColumnGroupBuilder> columnGroups

        public GroupTextExpression(List<ColumnGroupBuilder> columnGroups) {
            this.columnGroups = columnGroups
        }

        public String evaluate(ReportParameters reportParameters) {
            StringBuilder sb = new StringBuilder()
            for(ColumnGroupBuilder group : columnGroups) {
                if (sb.size() > 0) {
                    sb.append(", ")
                }
                sb.append(((ValueExpression)group.group.titleExpression).value)
                sb.append(": ")
                sb.append(reportParameters.getFieldValue(group.group.valueField.valueExpression.name))
            }
            return sb.toString();
        }
    }
}
