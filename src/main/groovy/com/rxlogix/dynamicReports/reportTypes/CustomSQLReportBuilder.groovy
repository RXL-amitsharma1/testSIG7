package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.config.ReportResult
import grails.converters.JSON
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.util.zip.GZIPInputStream

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class CustomSQLReportBuilder {

    public void createCustomSQLReport(ReportResult reportResult, JasperReportBuilder report) {
        JSONArray columnNamesList = JSON.parse(reportResult.executedTemplateQuery.executedTemplate.columnNamesList)
        if (columnNamesList) {
            columnNamesList.each {
                report.addColumn(Columns.column(it, it, type.stringType()))
            }
        } else {
            // If we have blank values, just use the column headers present in the table
            if (reportResult?.data?.value) {
                JSONArray result = JSON.parse(new GZIPInputStream(new ByteArrayInputStream(reportResult?.data?.value)), "utf-8")
                JSONObject sample = result.first()
                sample.keys().each {
                    report.addColumn(Columns.column(it, it, type.stringType()))
                }
            }
        }

    }
}