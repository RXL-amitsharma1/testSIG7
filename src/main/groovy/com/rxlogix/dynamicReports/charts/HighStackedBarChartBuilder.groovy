package com.rxlogix.dynamicReports.charts

import net.sf.dynamicreports.report.base.AbstractScriptlet
import net.sf.dynamicreports.report.base.chart.dataset.DRCategoryChartSerie
import net.sf.dynamicreports.report.builder.chart.CategoryChartSerieBuilder
import net.sf.dynamicreports.report.builder.component.GenericElementBuilder
import net.sf.dynamicreports.report.builder.expression.Expressions
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.chart.dataset.DRIChartSerie
import net.sf.dynamicreports.report.definition.expression.DRIExpression
import net.sf.dynamicreports.report.definition.expression.DRISimpleExpression
import org.apache.commons.lang3.Validate
/**
 * Created by gologuzov on 25.11.15.
 */
public class HighStackedBarChartBuilder extends GenericElementBuilder {
    private static final String NAMESPACE = "http://www.rxlogix.com/highcharts"
    private static final String NAME = "highcharts"

    private StackedBarChart chart

    private DRISimpleExpression<String> titleExpression
    private DRISimpleExpression<String> yAxisTitleExpression
    private DRISimpleExpression<String> categoryExpression
    private List<DRCategoryChartSerie> series
    private ReportScriptlet scriptlet

    public HighStackedBarChartBuilder(def options) {
        super(NAMESPACE, NAME)
        setHeight(200)
        scriptlet = new ReportScriptlet()
        series = new ArrayList<DRIChartSerie>()
        chart = new StackedBarChart(options)
        addParameter(ChartGenerator.PARAMETER_CHART_GENERATOR, chart)
    }

    public HighStackedBarChartBuilder setTitle(String title) {
        Validate.notNull(title, "Chart title must not be null")
        this.titleExpression = Expressions.text(title)
        return this;
    }

    public HighStackedBarChartBuilder setTitle(DRIExpression<String> title) {
        Validate.notNull(title, "Chart title must not be null")
        this.titleExpression = title
        return this
    }

    public HighStackedBarChartBuilder setYAxisTitle(String title) {
        Validate.notNull(title, "Y axis title must not be null")
        this.yAxisTitleExpression = Expressions.text(title)
        return this
    }

    public HighStackedBarChartBuilder setYAxisTitle(DRIExpression<String> title) {
        Validate.notNull(title, "Y axis title must not be null")
        this.yAxisTitleExpression = title
        return this
    }

    public HighStackedBarChartBuilder setCategory(DRIExpression<String> expression) {
        Validate.notNull(expression, "expression must not be null")
        this.categoryExpression = expression
        return this
    }

    public HighStackedBarChartBuilder addSerie(CategoryChartSerieBuilder serie) {
        Validate.notNull(serie, "serie must not be null")
        this.series.add(serie.build())
        return this
    }

    public HighStackedBarChartBuilder setShowLegend(Boolean showLegend) {
        chart.setShowLegend(showLegend)
        return this
    }

    public HighStackedBarChartBuilder setChartRowsCount(int rowsCount) {
        addParameter(ChartGenerator.PARAMETER_CHART_ROWS_COUNT, rowsCount)
        return this
    }

    public HighStackedBarChartBuilder setTotalRowIndices(List indices) {
        addParameter(ChartGenerator.PARAMETER_TOTAL_ROW_INDICES, indices)
        return this
    }

    public ReportScriptlet getScriptlet() {
        return scriptlet
    }

    private class ReportScriptlet extends AbstractScriptlet {
        @Override
        public void afterReportInit(ReportParameters reportParameters) {
            chart.setTitle(titleExpression.evaluate(reportParameters))
            chart.setYAxisTitle(yAxisTitleExpression.evaluate(reportParameters))
            series.each {
                chart.addSerie(it.labelExpression.value)
            }
        }

        @Override
        public void afterDetailEval(ReportParameters reportParameters) {
            super.afterDetailEval(reportParameters)
            List<String> keys = categoryExpression.evaluate(reportParameters)
            series.each {
                Integer value = reportParameters.getValue(it.valueExpression.name)
                chart.addValue(it.labelExpression.value, keys, value)
            }
        }
    }
}
