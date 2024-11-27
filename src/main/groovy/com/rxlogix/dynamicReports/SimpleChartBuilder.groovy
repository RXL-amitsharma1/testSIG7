package com.rxlogix.dynamicReports

import com.rxlogix.TemplateService
import com.rxlogix.util.ChartOptionsUtils
import grails.util.Holders
import net.sf.dynamicreports.design.transformation.chartcustomizer.PieChartLabelFormatCustomizer
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.chart.AbstractChartBuilder
import net.sf.dynamicreports.report.builder.chart.LineChartBuilder
import net.sf.dynamicreports.report.builder.chart.PieChartBuilder
import net.sf.dynamicreports.report.builder.chart.StackedBarChartBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.labels.ItemLabelAnchor
import org.jfree.chart.labels.ItemLabelPosition
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.renderer.category.StackedBarRenderer
import org.jfree.ui.TextAnchor

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class SimpleChartBuilder {
    TemplateService templateService = Holders.applicationContext.getBean("templateService")

    void createChart(Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {

        JasperReportBuilder chartReportBuilder = report()
        def ROW_NAMES = ["Event System Organ Class"]
        def COLUMN_NAMES = ["Case Count", "Event Count"]

        def data = []
        for (int i = 0; i < 3; i++) {
            def item = [:]
            ROW_NAMES.collect {
                item[it] = "${it}_${i}"
            }
            COLUMN_NAMES.collect {
                item[it] = (int) Math.round(Math.random() * 100)
            }
            data.add(item)
        }

        List<FieldBuilder> rowFields
        List<FieldBuilder> columnFields
        List<TextColumnBuilder> rows
        List<TextColumnBuilder> columns
        (rowFields, columnFields, rows, columns) = parseFields(ROW_NAMES, COLUMN_NAMES)
        chartReportBuilder.addField(*rowFields)
        chartReportBuilder.addField(*columnFields)
        chartReportBuilder.setDataSource(new JRMapCollectionDataSource(data))

        def chartOptions = [
                chart      : [:],
                legend     : [:],
                plotOptions: [
                        series: [
                                dataLabels: [:]
                        ]
                ]
        ]
        chartOptions = ChartOptionsUtils.deserialize(templateService.getChartDefaultOptions(), chartOptions)

        def chartTitle = "Test Chart"
        ComponentBuilder chart = addChart(chartOptions, chartTitle, rows, columns, chartReportBuilder, params)
        chart.setFixedHeight(415)
        chartReportBuilder.addSummary(chart)


        JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
        jasperReportBuilderEntry.jasperReportBuilder = chartReportBuilder
        jasperReportBuilderEntry.excelSheetName = chartTitle
        jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
    }

    JasperReportBuilder createSubChart(Map params, Map chartMetaData) {

        JasperReportBuilder chartReportBuilder = new SignalJasperReportBuilder()
        def ROW_NAMES = chartMetaData.rowNames
        def COLUMN_NAMES = chartMetaData.columnNames


        List<FieldBuilder> rowFields
        List<FieldBuilder> columnFields
        List<TextColumnBuilder> rows
        List<TextColumnBuilder> columns
        (rowFields, columnFields, rows, columns) = parseFields(ROW_NAMES, COLUMN_NAMES)
        chartReportBuilder.addField(*rowFields)
        chartReportBuilder.addField(*columnFields)


        chartReportBuilder.setDataSource(new JRMapCollectionDataSource(chartMetaData.data))

        def chartOptions = [
                chart      : [:],
                legend     : [:],
                plotOptions: [
                        series: [
                                dataLabels: [:]
                        ]
                ]
        ]
        chartOptions = ChartOptionsUtils.deserialize(templateService.getChartDefaultOptions(), chartOptions)

        if (params.chartType) {
            chartOptions.chart.type = params.chartType
        }
        def chartTitle = chartMetaData.title
        ComponentBuilder chart = addChart(chartOptions, chartTitle, rows, columns, chartReportBuilder, params)
        chart.setFixedHeight(400)
        chartReportBuilder.addSummary(chart)

        chartReportBuilder
    }

    private List parseFields(List rowNames, List columnNames) {
        List<FieldBuilder> rowFields = []
        List<FieldBuilder> columnFields = []
        List<TextColumnBuilder> rows = []
        List<TextColumnBuilder> columns = []

        rowNames.collect {
            def field = field(it, type.stringType())
            rowFields.add(field)
            rows.add(Columns.column(it, field))
        }

        columnNames.collect {
            def field = field(it, type.integerType())
            columnFields.add(field)
            columns.add(Columns.column(it, field))
        }
        [rowFields, columnFields, rows, columns]
    }

    private AbstractChartBuilder addChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        def type = options.chart.type
        switch (type) {
            case "pie":
                return addPieChart(options, chartTitle, rows, columns, report, params)
            case "line":
                return addLineChart(options, chartTitle, rows, columns, report, params)
            case "column":
            default:
                return addStackedBarChart(options, chartTitle, rows, columns, report, params)
        }
    }

    private AbstractChartBuilder addPieChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        PieChartBuilder chart = cht.pieChart()
                .setTitle(chartTitle)
                .setLabelFormat("{1}")
                .setKey(new SingleCategoryExpression(rows))
        //.addCustomizer(new PieChartLabelFormatCustomizer())
                .addSerie(cht.serie(columns.head()))
        chart
    }

    private AbstractChartBuilder addLineChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        LineChartBuilder chart = cht.lineChart()
                .setTitle(chartTitle)
                .setShowLegend(true)
                .setCategory(new SingleCategoryExpression(rows))
        columns.each {
            chart.addSerie(cht.serie(it))
        }
        chart
    }

    private AbstractChartBuilder addStackedBarChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        StackedBarChartBuilder chart = cht.stackedBarChart()
                .setTitle(chartTitle)
                .setShowLegend(true)
                .setCategory(new SingleCategoryExpression(rows))
                .addCustomizer(new StackedBarChartLabelFormatCustomizer())
                .addCustomizer(new StackedBarChartTotalValuesCustomizer())
        columns.each {
            chart.addSerie(cht.serie(it))
        }
        chart
    }

    private class StackedBarChartLabelFormatCustomizer implements DRIChartCustomizer, Serializable {
        void customize(JFreeChart chart, ReportParameters reportParameters) {
            org.jfree.chart.axis.CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis()
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(45))
        }
    }

    private class StackedBarChartTotalValuesCustomizer implements DRIChartCustomizer, Serializable {
        void customize(JFreeChart chart, ReportParameters reportParameters) {
            StackedBarRenderer barRenderer = (StackedBarRenderer) chart.getCategoryPlot().getRenderer()
            barRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator())
            barRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                    ItemLabelAnchor.CENTER, TextAnchor.CENTER))
            barRenderer.setBaseItemLabelsVisible(true)
        }
    }

    private class SingleCategoryExpression extends AbstractSimpleExpression<String> {
        private List<TextColumnBuilder> rows;

        public SingleCategoryExpression(List<TextColumnBuilder> rows) {
            this.rows = rows
        }

        @Override
        public String evaluate(ReportParameters reportParameters) {
            String value = reportParameters.getFieldValue(rows.last().name)
            value == null ? "N/A" : value
        }
    }
}
