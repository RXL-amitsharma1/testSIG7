package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.EvdasAlertService
import com.rxlogix.EvdasOnDemandAlertService
import com.rxlogix.ViewInstanceService
import com.rxlogix.enums.ReportFormat
import com.rxlogix.util.DateUtil
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class EvdasAlertReportBuilder extends ReportBuilder {
    private ViewInstanceService viewInstanceService = Holders.applicationContext.getBean("viewInstanceService")
    private EvdasAlertService evdasAlertService = Holders.applicationContext.getBean("evdasAlertService")
    private EvdasOnDemandAlertService evdasOnDemandAlertService = Holders.applicationContext.getBean("evdasOnDemandAlertService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if(params.outputFormat    == ReportFormat.XLSX.name() && params.callingScreen != Constants.Commons.DASHBOARD) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
                params.showCompanyLogo = false
                params.showLogo = false
            }

            JasperReportBuilder reportSheet = buildReport(dataSource, params, "", false)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildCriteriaReport(def criteriaList, Map params) {
        JasperReportBuilder evdasDataReport = ReportBuilder.initializeNewReport(params.portraitType, false , true, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())

        HorizontalListBuilder evdasList = cmp.horizontalList()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaList) {
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            evdasList.add(cmp.verticalList(img)).newRow()
            evdasList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            evdasList.add(cmp.verticalList(filler))
            if(params.callingScreen != Constants.Commons.DASHBOARD) {
                criteriaList.each{
                    if(!params.onDemand){
                        addEvdasCriteriaHeaderTable(evdasList, it.label, it.value)
                    }
                    else if(params.onDemand && it.label != 'Advanced Filter'){
                        addEvdasCriteriaHeaderTable(evdasList, it.label, it.value)
                    }
                }
            }
        }

        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalGap(10))
        verticalList.add(cmp.verticalList(evdasList))

        if (params.outputFormat != ReportFormat.XLSX.name()) {
            verticalList.add(cmp.verticalList(filler))
            verticalList.add(cmp.verticalGap(10))
        }
        JasperReportBuilder report = evdasDataReport.summary(verticalList)

        report.setPageFormat(getPageType(), getPageOrientation())
        report
    }

    private void addEvdasCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }


    protected void addColumns(JasperReportBuilder report, Map params) {

        List<Map> fixedColumnList = [
                ["label": "Substance", "name": "substance"],
                ["label": "SOC", "name": "soc"]]

        if(params.onDemand) {
            fixedColumnList = fixedColumnList +  [["label": "PT", "name": "pt"]]
        }else {
            fixedColumnList = fixedColumnList + [["label": "PT", "name": "pt"], ["label": "Comments", "name": "comment"]]
        }

        String callingScreen = params.callingScreen
        if (callingScreen == Constants.Commons.DASHBOARD || callingScreen == Constants.Commons.TRIGGERED_ALERTS) {

            fixedColumnList << ["label": "Alert Name", "name": "name"]
        }
        Long viewId
        List columnList = []
        String alertType = "EVDAS Alert"
        if (params.viewId && !params.onDemand) {
            viewId = Long.valueOf(params.viewId)
            columnList = params.outputFormat != ReportFormat.XLSX.name() ? viewInstanceService.fetchVisibleColumnList(alertType, viewId) : evdasAlertService.getColumnListForExcelExport(alertType, viewId)
            columnList = columnList - [["label": "Comments", "name": "comment"]]
            fixedColumnList.addAll(columnList)
        }

        if(params.onDemand) {
            List onDemandColumnList = []
            alertType = "EVDAS Alert on Demand"
            if (params.viewId) {
                onDemandColumnList = params.outputFormat != ReportFormat.XLSX.name() ? viewInstanceService.fetchVisibleColumnList(alertType, params.viewId as Long) : evdasAlertService.getColumnListForExcelExport(alertType, params.viewId as Long)
                fixedColumnList.addAll(onDemandColumnList)
            }
        }
        int longCommentSize = 20
        if(params.outputFormat == ReportFormat.XLSX.name()) {
            longCommentSize = 500 // In export comment field width is increased
        }
        fixedColumnList.each {
            if (it.name == "dueIn") {
                report.addColumn(Columns.column(it.label, it.name, type.longType()))
            } else if ( it.name == "comment" && params.isLongComment ) {
                report.addColumn( Columns.column( it.label, it.name, type.stringType() ).setFixedColumns(longCommentSize) )
            } else {
                report.addColumn(Columns.column(it.label, it.name, type.stringType()))
            }
        }
    }

    @Override
    protected String getReportHeader(Map params) {
        return customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
    }

    @Override
    protected PageType getPageType() {
        return PageType.LEDGER
    }

    @Override
    protected PageOrientation getPageOrientation() {
        return PageOrientation.LANDSCAPE
    }
}
