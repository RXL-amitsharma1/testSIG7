package com.rxlogix.dynamicReports.charts
import com.rxlogix.dynamicReports.charts.ooxml.Chart
import com.rxlogix.dynamicReports.charts.ooxml.ChartDataSource
import com.rxlogix.dynamicReports.charts.ooxml.data.CategoryDataSource
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartData
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartSeries
import com.rxlogix.dynamicReports.charts.ooxml.data.ValueDataSource
import net.sf.jasperreports.engine.export.ooxml.BaseHelper
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import org.apache.poi.ss.util.CellReference
/**
 * Created by gologuzov on 04.02.16.
 */
class XlsxChartHelper extends BaseHelper {
    private static final MAX_SHEET_NAME_LENGTH = 31
    private def options
    private int chartRowsCount
    private def totalRowIndices
    private JRXlsxExporter exporter

    private Chart chart

    XlsxChartHelper(JRXlsxExporter exporter, Writer writer, def options, int chartRowsCount, List totalRowIndicies) {
        super(exporter.jasperReportsContext, writer)
        this.exporter = exporter
        this.options = options
        this.chartRowsCount = chartRowsCount ? chartRowsCount : 1
        this.totalRowIndices = totalRowIndicies
        createChart()
    }

    private void createChart() {
        chart = new Chart(options)
        ChartData data = chart.getChartDataFactory().createChartData(options)
        def dataSheetName = getDataSheetName()
        char columnPrefix = 0
        char columnIndex = 'A'
        String firstCategoryColumn = (columnPrefix == 0 ? "" : String.valueOf(columnPrefix)) + String.valueOf(columnIndex)
        String lastCategoryColumn = (columnPrefix == 0 ? "" : String.valueOf(columnPrefix)) + String.valueOf((char) (columnIndex + chartRowsCount - 1))
        ChartDataSource<String> categoryDS = new CategoryDataSource(options.xAxis[0].categories, dataSheetName, firstCategoryColumn, lastCategoryColumn, totalRowIndices)
        for (def series : options.series) {
            String valueColumn = (columnPrefix == 0 ? "" : String.valueOf(columnPrefix)) + String.valueOf((char) (columnIndex + chartRowsCount))
            ChartDataSource<Number> valueDS = new ValueDataSource(series.data, dataSheetName, valueColumn, totalRowIndices)
            ChartSeries chartSeries = data.addSeries(categoryDS, valueDS)
            chartSeries.setTitle(new CellReference(dataSheetName, 2, CellReference.convertColStringToIndex(valueColumn), true, true))

            if (columnIndex == 'Z') {
                if (columnPrefix == 0) {
                    columnPrefix = 'A'
                } else {
                    columnPrefix++
                }
                columnIndex = 'A'
            } else {
                columnIndex++
            }
        }
        chart.plot(data)
    }

    public void exportChart() {
        this.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        def chartStr = chart.toString()
        this.write(chartStr)
        this.flush()
    }

    private String getDataSheetName() {
        // Next sheet (index is started from 1)
        def sheetName = exporter.sheetNames[exporter.sheetIndex]
        return getSheetName(sheetName)
    }

    /**
     * A bit changed method from JRXlsAbstractExporter
     * @param sheetName Original sheet name
     * @return optimized sheet name with index if repeated, etc
     */
    private String getSheetName(String sheetName) {
        if(exporter.sheetNames != null && exporter.sheetNamesIndex < exporter.sheetNames.length) {
            sheetName = exporter.sheetNames[exporter.sheetNamesIndex];
        }

        if(sheetName == null) {
            return "Page " + (exporter.sheetIndex + 1);
        } else {
            int crtIndex = Integer.valueOf(1).intValue();
            String txtIndex = "";
            String validSheetName = sheetName.length() < 32?sheetName:sheetName.substring(0, 31);
            if(exporter.sheetNamesMap.containsKey(validSheetName)) {
                crtIndex = ((Integer)exporter.sheetNamesMap.get(validSheetName)).intValue() + 1;
                txtIndex = String.valueOf(crtIndex);
            }

            //this.sheetNamesMap.put(validSheetName, Integer.valueOf(crtIndex));
            String name = sheetName;
            if(txtIndex.length() > 0) {
                name = sheetName + " " + txtIndex;
            }

            if(name.length() > 31) {
                name = (sheetName + " ").substring(0, 31 - txtIndex.length()) + txtIndex;
            }

            return name;
        }
    }
}
