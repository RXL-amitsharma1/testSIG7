package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.ViewInstanceService
import com.rxlogix.cache.CacheService
import com.rxlogix.enums.ReportFormat
import com.rxlogix.signal.ViewInstance
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

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class SingleCaseReviewAlertsReportBuilder extends ReportBuilder {
    private ViewInstanceService viewInstanceService = Holders.applicationContext.getBean("viewInstanceService")
    private CacheService cacheService = Holders.applicationContext.getBean("cacheService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if(params.outputFormat    == ReportFormat.XLSX.name() && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }
            params.portraitType = false
            JasperReportBuilder reportSheet = buildReport(dataSource, params, "",false)
            reportSheet.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }
    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder singleCaseDataReport = ReportBuilder.initializeNewReport(params.portraitType, false , true, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), PageOrientation.LANDSCAPE)

        HorizontalListBuilder singleCaseList = cmp.horizontalList()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaMap) {
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            singleCaseList.add(cmp.verticalList(img)).newRow()
            singleCaseList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            singleCaseList.add(cmp.verticalList(filler))
            if(params.callingScreen != Constants.Commons.DASHBOARD) {
                criteriaMap.each{
                    addSingleCaseCriteriaHeaderTable(singleCaseList, it.label, it.value)
                }
            }
        }

        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(singleCaseList))
        JasperReportBuilder report = singleCaseDataReport.summary(verticalList)

        report.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
        report
    }
    private void addSingleCaseCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }
    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        int longCommentSize = 20
        if(params.outputFormat == ReportFormat.XLSX.name()) {
            longCommentSize = 500 // In export comment field width is increased
        }
        if(params.isOnDemandExport) {
            //change here for on demand export
            Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
            Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
            def labelForCaseNum = rptToUiLabelMap.get(rptToSignalFieldMap.get("caseNumber"))?:customMessageService.getMessage("app.label.case.number")
            List<Map> fixedColumnList = [["label": labelForCaseNum, "name": "caseNumber"]]
            if (params.exportCaseNarrative) {
                for (Integer i = 1; i <= params.maxSize; i++) {
                    fixedColumnList += [["label": "Case Narrative", "name": "caseNarrative" + i]]
                }
            }
            List<Map> onDemandColumnList = []
            onDemandColumnList = viewInstanceService.fetchVisibleColumnList("Single Case Alert on Demand", params.viewId ? params.viewId as Long : 0)
            // this should be changed only for safety alerts
            ViewInstance vi = ViewInstance.findById(params.viewId ? params.viewId as Long : 0)
            if(!vi.alertType.contains('Vaers') && !vi.alertType.contains('Faers') && !vi.alertType.contains('Vigibase')){
                onDemandColumnList.each{
                    if(rptToSignalFieldMap.get(it.name)!=null && rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))!=null){
                        it.label = rptToUiLabelMap.get(rptToSignalFieldMap.get(it.get("name")))?:it.display
                    }
                }
            }
            fixedColumnList.addAll(onDemandColumnList)
            fixedColumnList.each {
                if (it.name == "comments" && params.isLongComment) {
                    report.addColumn(Columns.column(it.label, it.name, type.stringType()).setFixedColumns(longCommentSize))
                } else if (params.exportCaseNarrative && it.name.startsWith("caseNarrative")) {
                    report.addColumn(Columns.column(it.label, it.name, type.stringType()).setFixedColumns(longCommentSize))
                }  else {
                    report.addColumn(Columns.column(it.label, it.name, type.stringType()))
                }
            }
        } else {
            // change label here according to actual using cacheService map get method
            // on adding labels also
            Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
            Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
            boolean isSafety = true
            boolean isJader = false
            if (null != params?.isSafety) {
                isSafety = Boolean.valueOf(params.isSafety)
            }
            if (params?.isJader != null) {
                isJader = Boolean.valueOf(params.isJader)
            }
            boolean isCaseSeries = false
            if (null != params?.isCaseSeries) {
                isCaseSeries = Boolean.valueOf(params.isCaseSeries)
            }
            def labelForCaseNum = rptToUiLabelMap.get(rptToSignalFieldMap.get("caseNumber")) ?: customMessageService.getMessage("app.label.case.number")
            if (!isSafety && isCaseSeries) {
                labelForCaseNum = "Case"
            }
            List<Map> fixedColumnList = [["label": "Priority", "name": "priority"],
                                         ["label": labelForCaseNum, "name": "caseNumber"], ["label": "Comments", "name": "comments"]]

            if (params.exportCaseNarrative) {
                for (Integer i = 1; i <= params.maxSize; i++) {
                    fixedColumnList += [["label": "Case Narrative", "name": "caseNarrative" + i]]
                }
            }
            if(isCaseSeries && isJader){
                fixedColumnList -= [["label": "Comments", "name": "comments"]]
            }
            if(!Holders.config.alert.priority.enable || (isCaseSeries && !isSafety)){
                fixedColumnList -= [["label": "Priority", "name": "priority"]]
            }

            String callingScreen = params.callingScreen
            if (callingScreen == Constants.Commons.DASHBOARD || callingScreen == Constants.Commons.TRIGGERED_ALERTS) {
                fixedColumnList << ["label": "Alert Name", "name": "alertName"]
            }
            List<Map> columnList = []
            columnList = viewInstanceService.fetchVisibleColumnList("Single Case Alert", params.viewId ? params.viewId as Long : 0)
            columnList = columnList - [["label": "Comments", "name": 'comments']]
             // this should be changed only for safety alerts
            ViewInstance vi = ViewInstance.findById(params.viewId ? params.viewId as Long : 0)
            if(!vi?.alertType?.contains('Vaers') && !vi?.alertType?.contains('Faers') && !vi?.alertType?.contains('Vigibase') && !vi?.alertType?.contains('Jader')){
                columnList.each{
                    if(rptToSignalFieldMap.get(it.name)!=null && rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))!=null){
                        it.label = rptToUiLabelMap.get(rptToSignalFieldMap.get(it.get("name")))?:it.display
                    }
                }
            }
            fixedColumnList.addAll(columnList)
            if (!Holders.config.icr.comments.enable) { //removed comments column from export if flag is disabled
                fixedColumnList.removeIf { it.name == Constants.AdvancedFilter.COMMENTS }
            }
            fixedColumnList.each {
                if (it.name == "comments" && params.isLongComment) {
                    report.addColumn(Columns.column(it.label, it.name, type.stringType()).setFixedColumns(longCommentSize))
                } else if (params.exportCaseNarrative && it.name.startsWith("caseNarrative")) {
                    report.addColumn(Columns.column(it.label, it.name, type.stringType()).setFixedColumns(longCommentSize))
                } else {
                    report.addColumn(Columns.column(it.label, it.name, it.name == "dueIn" ? type.longType() : type.stringType()))
                }
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
        return PageOrientation.PORTRAIT
    }
}
