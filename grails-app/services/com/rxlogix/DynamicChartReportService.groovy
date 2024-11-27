package com.rxlogix


import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.dynamicReports.reportTypes.WatermarkComponentBuilder
import com.rxlogix.user.User
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder

class DynamicChartReportService {
    def grailsApplication

    String getReportName(ReportResult reportResult, boolean isInDraftMode, Map params) {
        def suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix = Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }

        if(isInDraftMode){
            suffix += "Draft"
        }

        return "R$reportResult.$suffix"
    }

    String getReportFilename(String reportName, String outputFormat) {
        if (!outputFormat) {
            outputFormat = "html"
        }
        return reportName + "." + outputFormat.toLowerCase()
    }

    File getReportFile(String filename) {
        File file = new File(getReportsDirectory() + filename)
        if (file?.exists() && file?.size() > 0) {
            return file
        }
        return null
    }

    public String getReportsDirectory() {
        return "${System.getProperty("java.io.tmpdir")}/pvsignal/"
    }

    private setReportTheme(User user) {
        Templates.applyTheme(user?.preference?.theme)
    }

    public addWatermarkIfNeeded(boolean isInDraftMode, List<JasperReportBuilder> jasperReportBuilderList){
        if(isInDraftMode){
            jasperReportBuilderList.each {it.background(new WatermarkComponentBuilder(customMessageService.getMessage("app.label.draftWatermark"), it.report.template))}
        }
    }

}

