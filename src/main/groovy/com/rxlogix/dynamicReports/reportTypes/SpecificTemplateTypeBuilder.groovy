package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.config.ReportTemplate
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.jasperreports.engine.design.JasperDesign

/**
 * Created by gologuzov on 10.02.17.
 */
trait SpecificTemplateTypeBuilder {
    abstract JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang)
}
