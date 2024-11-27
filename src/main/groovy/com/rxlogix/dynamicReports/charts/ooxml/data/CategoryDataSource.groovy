package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.ChartDataSource

/**
 * Created by gologuzov on 10.09.16.
 */
class CategoryDataSource implements ChartDataSource<List<String>> {
    private List<List<String>> data
    private String dataSheetName
    private String firstColumn
    private String lastColumn
    private List totalRowIndices

    public CategoryDataSource(List<Map<String, ?>> data) {
        this(data, null, null, null, [])
    }

    public CategoryDataSource(List<Map<String, ?>> data, String dataSheetName, String firstColumn, String lastColumn, List totalRowIndices) {
        this.data = transformData (data)
        this.dataSheetName = dataSheetName
        this.firstColumn = firstColumn
        this.lastColumn = lastColumn
        this.totalRowIndices = totalRowIndices
    }

    @Override
    int getPointCount() {
        return data.size()
    }

    private static List<List<String>> transformData (List<?> data) {
        def result = []
        data.each { item ->
            if (item instanceof String) {
                result.push([item])
            } else {
                result += transformData (item.categories).each {it.push(item.name)}
            }
        }
        return result
    }

    @Override
    List<String> getPointAt(int index) {
        return data[index]
    }

    @Override
    boolean isReference() {
        return true
    }

    @Override
    boolean isNumeric() {
        return false
    }

    @Override
    boolean hasFormulaString() {
        return dataSheetName != null && firstColumn != null && lastColumn != null
    }

    @Override
    String getFormulaString() {
        def startIndex = 4
        def endIndex = startIndex + data.size() + totalRowIndices.size() - 1
        def formula = []
        def currentIndex = startIndex
        totalRowIndices.each {
            if (currentIndex < endIndex) {
                formula.add("'${dataSheetName}'!\$${firstColumn}\$${currentIndex}:\$${lastColumn}\$${startIndex + it - 1}")
                currentIndex = startIndex + it + 1
            }
        }
        if (currentIndex < endIndex) {
            formula.add("'${dataSheetName}'!\$${firstColumn}\$${currentIndex}:\$${lastColumn}\$${endIndex}")
        }
        return formula.join(";")
    }

    @Override
    boolean isMultiLevel() {
        return data.find{it.size() > 1} != null
    }
}
