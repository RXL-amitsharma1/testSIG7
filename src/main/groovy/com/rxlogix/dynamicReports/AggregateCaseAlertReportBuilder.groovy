package com.rxlogix.dynamicReports

import com.rxlogix.AggregateCaseAlertService
import com.rxlogix.Constants
import com.rxlogix.ViewInstanceService
import com.rxlogix.config.AlertFieldService
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.enums.ReportFormat
import com.rxlogix.cache.CacheService
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

class AggregateCaseAlertReportBuilder extends ReportBuilder {

    private ViewInstanceService viewInstanceService = Holders.applicationContext.getBean("viewInstanceService")
    private AggregateCaseAlertService aggregateCaseAlertService = Holders.applicationContext.getBean("aggregateCaseAlertService")
    private CacheService cacheService = Holders.applicationContext.getBean("cacheService")
    private AlertFieldService alertFieldService= Holders.applicationContext.getBean("alertFieldService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if(params.outputFormat == ReportFormat.XLSX.name() && params.callingScreen != Constants.Commons.DASHBOARD) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
                params.showCompanyLogo = false
                params.showLogo = false
            }

            JasperReportBuilder reportSheet = buildReport(dataSource, params, "", false)
            reportSheet.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildCriteriaReport(def criteriaList, Map params) {
        JasperReportBuilder aggDataReport = ReportBuilder.initializeNewReport(params.portraitType, false , true, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())

        HorizontalListBuilder aggregateList = cmp.horizontalList()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaList) {
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            aggregateList.add(cmp.verticalList(img)).newRow()
            aggregateList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            aggregateList.add(cmp.verticalList(filler))
            if(params.callingScreen != Constants.Commons.DASHBOARD) {
                criteriaList.each{
                    if(!params.onDemand){
                        addAggCriteriaHeaderTable(aggregateList, it.label, it.value)
                    }
                    else if(params.onDemand && it.label != 'Advanced Filter'){
                        addAggCriteriaHeaderTable(aggregateList, it.label, it.value)
                    }
                }
            }
        }

        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalGap(10))
        verticalList.add(cmp.verticalList(aggregateList))
        JasperReportBuilder report = aggDataReport.summary(verticalList)

        report.setPageFormat(getPageType(), getPageOrientation())
        report
    }

    private void addAggCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(20).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        Map labelConfigDisplay = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }
        Map jaderColumnDisplay = alertFieldService.getJaderColumnList("jader",false).collectEntries {
            b -> [b.name, b.display]
        }

        Boolean isJader = params.isJader ? true : false
        List<Map> fixedColumnList = [
                                ["label": isJader ? jaderColumnDisplay.get("productName") : labelConfigDisplay.get( "productName" ), "name": "productName"],
                                ["label": isJader ? jaderColumnDisplay.get("soc") :labelConfigDisplay.get( "soc" ), "name": "soc"],
                                ["label": isJader ? jaderColumnDisplay.get("pt") :labelConfigDisplay.get( "pt" ).split("#OR")[0]?.trim(), "name": "pt"],
                                ["label": isJader ? jaderColumnDisplay.get("comment") :labelConfigDisplay.get( "comment" ), "name": 'comment']
        ]
        if(params.groupBySmq){
            fixedColumnList = [
                    ["label": isJader ? jaderColumnDisplay.get("productName") : labelConfigDisplay.get( "productName" ), "name": "productName"],
                    ["label": isJader ? jaderColumnDisplay.get("pt") : labelConfigDisplay.get( "pt" ).split("#OR")[1]?.trim(), "name": "pt"],
                    ["label": isJader ? jaderColumnDisplay.get("comment") : labelConfigDisplay.get( "comment" ), "name": 'comment']
            ]
        }
        String callingScreen = params.callingScreen
        if(callingScreen == Constants.Commons.DASHBOARD || callingScreen == Constants.Commons.TRIGGERED_ALERTS){
            fixedColumnList << ["label": labelConfigDisplay.get( "name" ), "name": "name"]
        }

        List<Map> columnList = aggregateCaseAlertService.getColumnListForAggregateExport(params)
        int longCommentSize = 20
        if(params.outputFormat == ReportFormat.XLSX.name()){
            longCommentSize = 500 // In export comment field width is increased
        }
        columnList = columnList - [["label": isJader ? jaderColumnDisplay.get("comment") : labelConfigDisplay.get("comment"), "name": 'comment']]

        fixedColumnList.addAll(columnList)

        if(params.onDemand){
            fixedColumnList = fixedColumnList - [["label": labelConfigDisplay.get( "comment" ), "name": 'comment']]
        }
        Boolean isRor = cacheService.getRorCache()
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get( params.id )
        boolean labelCondition = params.groupBySmq ? true : viewInstanceService.isLabelChangeRequired( executedConfiguration?.selectedDatasource )
        for(int i=0;i<fixedColumnList.size();i++){
            Map it = fixedColumnList.get(i)
            if (params.adhocRun && it.name == 'pregenency') {
                report.addColumn(Columns.column(it.label, 'pregnancy', type.stringType()))
            } else if(it.name == "productName" && params.adhocRun && params.miningVariable){
                report.addColumn(Columns.column(params.miningVariable, 'productName', type.stringType()))
            } else if (it.label && it.name != 'currentDisposition') { // Report should not contain currentDisposition field
                if(it.name in ['rorValue','rorValueFaers','rorValueVaers','rorValueVigibase','rorValueEvdas']){
                    if( it.name?.equals( "rorValue" ) ) {
                        if( isRor == true ) {
                            report.addColumn( Columns.column( it.label?.toString()?.contains( "/" )?it.label?.split( "/" )[ 0 ]:it.label, it.name, type.stringType() ) )
                        } else {
                            report.addColumn( Columns.column( it.label?.toString()?.contains( "/" )?it.label?.split( "/" )[ 1 ]:it.label, it.name, type.stringType() ) )
                        }
                    } else {
                        report.addColumn( Columns.column( it.label, it.name, type.stringType() ) )
                    }
                }else  if(it.name.equals("pt") && !params.adhocRun){
                    if(params.groupBySmq){
                        it.label = it.label.toString().contains("#OR")? it.label.split("#OR")[1]:it.label
                    }else{
                        it.label = it.label.toString().contains("#OR")? it.label.split("#OR")[0]:it.label
                    }
                    report.addColumn(Columns.column(it.label, it.name, type.stringType()))
                } else if ( it.name == "dueIn" ) {
                    report.addColumn( Columns.column( it.label, it.name, type.longType() ) )
                } else if ( it.name == "comment" && params.isLongComment ) {
                    report.addColumn( Columns.column( it.label, it.name, type.stringType() ).setFixedColumns(longCommentSize) )
                } else {
                    report.addColumn( Columns.column( it.label, it.name, type.stringType() ) )
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
