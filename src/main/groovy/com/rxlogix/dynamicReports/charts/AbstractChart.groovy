package com.rxlogix.dynamicReports.charts

import grails.gorm.dirty.checking.DirtyCheck

/**
 * Created by gologuzov on 30.11.15.
 */
abstract class AbstractChart implements ChartGenerator {

    protected def options
    protected def series = [:]

    void setTitle(String title) {
        if (!options.title) {
            options.title = [:]
        }
        options.title.text = title
    }

    void addSerie(String name) {
        def serie = [name: name, data: []]
        if (!options.series) {
            options.series = []
        }
        options.series.push(serie)
        series[name] = serie
    }

    void setShowLegend(Boolean showLegend) {
        options.plotOptions?.series?.showInLegend = showLegend
    }

    void setShowPercentages(Boolean showPercentages) {
        if (showPercentages) {
            options.plotOptions.series.dataLabels.formatter = "function() {return \'\'+ (this.point.name.length > 15 ? this.point.name.substring(0, 15) +\'...\' : this.point.name) +\' (\'+ Math.round(this.percentage) +\'%)\';}"
        }
    }

    void addValue(String serieName, List<String> labels, Number value) {
        def categories = options.xAxis[0].categories
        if (!categories) {
            categories = options.xAxis[0].categories = []
        }

        buildCategoriesTree(categories, labels)
        series[serieName].data.push([name: labels.last(), y: value])
    }

    private void buildCategoriesTree(def categories, List<String> labels) {
        def label = labels.first()
        def child = categories.find {
            (it instanceof String) ? label.equals(it) : label.equals(it.name)
        }
        if (!child) {
            if (labels.size() > 1) {
                child = [name: label, categories: []]
            } else {
                child = label
            }
            categories.push(child)
        }
        if (labels.size() > 1) {
            buildCategoriesTree(child.categories, labels.tail())
        }
    }

    void setYAxisTitle(String title) {
        if (!options.yAxis[0]) {
            options.yAxis[0] = [:]
        }
        if (!options.yAxis[0].title) {
            options.yAxis[0].title = [:]
        }
        options.yAxis[0].title.text = title;
    }

    void setReversedStacks(Boolean value) {
        if (!options.yAxis[0]) {
            options.yAxis[0] = [:]
        }
        options.yAxis[0].reversedStacks = value
    }

    @Override
    def generateChart() {
        return options
    }
}
