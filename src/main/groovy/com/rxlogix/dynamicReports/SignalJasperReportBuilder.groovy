package com.rxlogix.dynamicReports

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.DRReport
import net.sf.dynamicreports.report.base.column.DRColumn
import net.sf.dynamicreports.report.builder.column.ColumnBuilder
import org.apache.commons.lang3.Validate

class SignalJasperReportBuilder extends JasperReportBuilder {

    SignalJasperReportBuilder() {
        super()
    }

    SignalJasperReportBuilder addColumn(ColumnBuilder... columns) {
        Validate.notNull(columns, "columns must not be null", new Object[0])
        Validate.noNullElements(columns, "columns must not contains null column", new Object[0])

        for (ColumnBuilder<?, ?> column : columns) {
            if (column.column.hasProperty("valueClass") && column.column.valueClass == String) {
                ((DRReport) this.getObject()).addColumn((DRColumn) column.setValueFormatter(new CSVFormulaFormatter()).build())
            } else {
                ((DRReport) this.getObject()).addColumn((DRColumn) column.build())
            }
        }
        return this
    }
}
