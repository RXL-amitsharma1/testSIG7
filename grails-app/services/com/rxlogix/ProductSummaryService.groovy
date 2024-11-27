package com.rxlogix

import com.rxlogix.enums.DataSourceEnum
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.http.util.TextUtils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Transactional
class ProductSummaryService implements AlertUtil {

    def alertCommentService
    def CRUDService
    def dispositionService
    def aggregateCaseAlertService
    def evdasAlertService
    def dynamicReportService
    def dataSource


    def getProductSummaryIndexMap() {
        def disposition = dispositionService.getDispositionListByDisplayName()
        def dataSourceMap = getDataSourceMap()
        def prodSummaryIndexMap = [dataSourceMap: dataSourceMap, disposition: disposition, productSummary: [] as JSON]
        prodSummaryIndexMap
    }

    def getProductSummarySearchMap(params) {
        def selectedDispositions = params.disposition
        def disposition = dispositionService.getDispositionListByDisplayName()
        def dataSourceMap = getDataSourceMap()

        def productSummaryResult = []
        if (!TextUtils.isEmpty(params.productSelection) && params.startDate != "null") {
            productSummaryResult = getProductSummary(params)
        }
        def prodSummarySearchMap = [dataSourceMap       : dataSourceMap,
                                    disposition         : disposition,
                                    selectedDataSource  : params.selectedDatasource,
                                    selectedDispositions: selectedDispositions.toString(),
                                    productSelection    : params.productSelection,
                                    startDate           : params.startDate,
                                    previousStartDate   : params.previousStartDate,
                                    previousEndDate     : params.previousEndDate,
                                    endDate             : params.endDate,
                                    frequency           : params.frequency,
                                    productSummary      : productSummaryResult as JSON]
        prodSummarySearchMap
    }


    def getProductSummary(params) {
        String productName = getNameFieldFromJson(params.productSelection)
        Integer productId = getIdFieldFromJson(params.productSelection) as Integer
        int allTheColumns = 9
        Map filterMap = [:]
        (0..allTheColumns).each {
            if (params["columns[${it}][search][value]"]) {
                String key = params["columns[${it}][data]"]
                String value = params["columns[${it}][search][value]"]
                filterMap.put(key, value)
            }
        }


        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]

        def periodStartDate = params.startDate
        def periodEndDate = params.endDate

        def previousStartDate = null
        if (!TextUtils.isEmpty(params.previousStartDate) && params.previousStartDate != "null") {
            previousStartDate = DateUtil.stringToDate(params.previousStartDate, "dd-MMM-yyyy", Holders.config.server.timezone)
        }
        def previousEndDate = null
        if (!TextUtils.isEmpty(params.previousEndDate) && params.previousEndDate != "null") {
            previousEndDate = DateUtil.stringToDate(params.previousEndDate, "dd-MMM-yyyy", Holders.config.server.timezone)
        }
        def dispositionValue = getSelectedDispositions(params.disposition)

        def alertList = []
        def prevAlertList = []
        def resultCount = 0
        def filterdCount = 0
        def dataSrc = params.selectedDatasource
        def disposition = dispositionValue?.collect { it.id }
        alertList = getAlertList(productName, productId, periodStartDate, periodEndDate, disposition, dataSrc, params.length, params.start, params.containsKey('outputFormat'), filterMap, orderColumnMap)
        if (alertList) {
            resultCount = alertList[0]["totalCount"]
            filterdCount = alertList[0]["filteredCount"]
            def eventList = alertList.collect { it.get("event") }
            if (dataSrc == Constants.DataSource.EUDRA) {
                prevAlertList = evdasAlertService.getPreviousPeriodEvdasAlertListForProductSummary(productName, previousStartDate, previousEndDate, dispositionValue, eventList)
            } else {
                prevAlertList = aggregateCaseAlertService.getPreviousPeriodAggregateAlertListForProductSummary(productId, previousStartDate, previousEndDate, dispositionValue, dataSrc, eventList)
            }
        } else if (alertList.size() == 0 && filterMap.size() > 0) {
            resultCount = getTotalCount(productName, productId, periodStartDate, periodEndDate, disposition, dataSrc)
        }
        [resultList: createProductSummaryList(alertList, prevAlertList, params.selectedDatasource), resultCount: resultCount, filteredCount: filterdCount]
    }

    def createProductSummaryList(alertList, prevAlertList, selectedDatasource) {
        def productSummaryList = []
        alertList?.each { alert ->
            def event = alert.event
            def value = selectedDatasource.toString().toUpperCase()
            def dataSource = DataSourceEnum."$value".value()
            def prevAlert = null
            if (prevAlertList.size() > 0) {
                prevAlert = prevAlertList.find { it.pt == event }
            }
            if (selectedDatasource == Constants.DataSource.PVA || selectedDatasource == Constants.DataSource.FAERS) {
                productSummaryList.add(createProductSummaryMapForAggAlert(alert, dataSource, prevAlert))
            } else {
                productSummaryList.add(createProductSummaryMapForEvdasAlert(alert, prevAlert))
            }

        }
        productSummaryList
    }

    def getSelectedDispositions(dispositionValue) {
        dispositionValue = dispositionValue?.tokenize(',').collect { it.trim() }
        def dispositionList = dispositionService.getDispositionFromDisplayName(dispositionValue)
        dispositionList
    }

    def createProductSummaryMapForAggAlert(aggCaseAlert, dataSource, prevAggAlert) {

        aggCaseAlert << [
                "source"    : getDataSource(dataSource),
                "sponCounts": "${aggCaseAlert.newSponCount}/${prevAggAlert ? prevAggAlert.newSponCount : '-'}/${aggCaseAlert.cumSponCount}",
                "eb05Counts": "${aggCaseAlert.eb05 == 0.0 ? 0 : aggCaseAlert.eb05}/${prevAggAlert ? (prevAggAlert.eb05 == 0.0 ? 0 : prevAggAlert.eb05) : '-'}",
                "eb95Counts": "${aggCaseAlert.eb95 == 0.0 ? 0 : aggCaseAlert.eb95}/${prevAggAlert ? (prevAggAlert.eb95 == 0.0 ? 0 : prevAggAlert.eb95) : '-'}"
        ]
        aggCaseAlert
    }

    def createProductSummaryMapForEvdasAlert(evdasAlert, prevEvdasAlert) {

        evdasAlert << [
                "sponCounts": "${evdasAlert.newSponCount}/${prevEvdasAlert ? prevEvdasAlert.newSpont : '-'}/${evdasAlert.cumSponCount}",
                "eb05Counts": '-/-',
                "eb95Counts": '-/-'
        ]
        evdasAlert
    }

    def getFrequencyMap(String prodName, String selectedDatasource) {
        def productName = getNameFieldFromJson(prodName)
        SubstanceFrequency frequency = SubstanceFrequency.findByName(productName)
        Map frequencyMap = [:]
        if (frequency) {
            frequencyMap.miningFrequency = frequency.frequencyName
            List probableDateRange = evdasAlertService.populatePossibleDateRanges(frequency.startDate, frequency.miningFrequency)
            frequencyMap.probableStartDate = probableDateRange.collect { it[0] }
            frequencyMap.probableEndDate = probableDateRange.collect { it[1] }
            def selectedIndex = getSelectedIndex(frequencyMap.probableEndDate)
            if (selectedIndex > 0) {
                frequencyMap.startDate = frequencyMap.probableStartDate[selectedIndex - 1]
                frequencyMap.endDate = frequencyMap.probableEndDate[selectedIndex - 1]
                frequencyMap.frequency = frequency.frequencyName
            }
        }
        frequencyMap
    }

    def getExportedFile(params) {
        def productSummaryResult = getProductSummary(params)
        productSummaryResult.resultList?.each {
            it.sponCounts = "" + it.sponCounts
            it.eb05Counts = "" + it.eb05Counts
            it.eb95Counts = "" + it.eb95Counts
        }
        def reportFile = dynamicReportService.createProductSummaryReport(new JRMapCollectionDataSource(productSummaryResult.resultList), params)
        reportFile
    }

    def saveRequestByForAlert(params){
        if(params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT || params.alertType == Constants.AlertConfigType.SIGNAL_MANAGEMENT){
            aggregateCaseAlertService.saveRequestByForAggCaseAlert(params)
        }else if(params.alertType == Constants.AlertConfigType.EVDAS_ALERT){
            evdasAlertService.saveRequestByForEvdasAlert(params)
        }
    }

    def getSelectedIndex(frequencyLastDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DateFormat.STANDARD_DATE)
        LocalDate localDate = LocalDate.now()
        int index = 0
        for (String lastDate : frequencyLastDate) {
                LocalDate firstDate = LocalDate.parse(lastDate, formatter)
                if(ChronoUnit.DAYS.between(firstDate, localDate) > 0){
                    index++
                }else{
                    break
                }

        }
        index

    }

    def getAlertList(productName, Integer productId, periodStartDate, periodEndDate, disposition, dataSrc, length, start, outputFormat, Map filterMap, orderColumnMap) {
        disposition = disposition.toString().replaceAll("[\\[\\]]", "")
        length = length?.isInteger() ? length.toInteger() : 0
        start = start?.isInteger() ? start.toInteger() : 0
        boolean isEvdas = (dataSrc == Constants.DataSource.EUDRA)
        def orderByCriteria = ""

        if (orderColumnMap.name == "event") {
            orderByCriteria = "ORDER BY PT ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "disposition") {
            orderByCriteria = "ORDER BY DISPLAY_NAME ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "name") {
            orderByCriteria = "ORDER BY NAME ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "source") {
            orderByCriteria = "ORDER BY SELECTED_DATA_SOURCE ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "sponCounts" && !isEvdas ) {
            orderByCriteria = "ORDER BY NEW_SPON_COUNT ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "sponCounts" && isEvdas ) {
            orderByCriteria = "ORDER BY NEW_SPONT ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "eb05Counts" && !isEvdas) {
            orderByCriteria = "ORDER BY EB05 ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "eb95Counts" && !isEvdas) {
            orderByCriteria = "ORDER BY EB95 ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "requestedBy") {
            orderByCriteria = "ORDER BY REQUESTED_BY ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "comments") {
            orderByCriteria = "ORDER BY COMMENTS ${orderColumnMap.dir}"
        } else if (orderColumnMap.name == "assessmentComments") {
            orderByCriteria = "ORDER BY GENERIC_COMMENT ${orderColumnMap.dir}"
        }

        if(isEvdas){
            filterMap.remove('eb05Counts')
            filterMap.remove('eb95Counts')
        }

        StringBuilder searchCriteria = new StringBuilder()
        if (filterMap.size() > 0) {
            searchCriteria.append("where ")
            filterMap.each { k, v ->
                if (k == 'event') {
                    searchCriteria.append("LOWER(PT) LIKE LOWER('%${v}%')")
                }
                if (k == 'disposition') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(DISPLAY_NAME) LIKE LOWER('%${v}%')")
                }
                if (k == 'name') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(NAME) LIKE LOWER('%${v}%')")
                }
                if (k == 'source') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(SELECTED_DATA_SOURCE) LIKE LOWER('%${v}%')")
                }
                if (k == 'sponCounts' && !isEvdas) {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(NEW_SPON_COUNT) LIKE LOWER('${v}')")
                }
                if (k == 'sponCounts' && isEvdas) {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(NEW_SPONT) LIKE LOWER('${v}')")
                }
                if (k == 'eb05Counts') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(EB05) LIKE LOWER('${v}')")
                }
                if (k == 'eb95Counts') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(EB95) LIKE LOWER('${v}')")
                }
                if (k == 'requestedBy') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(REQUESTED_BY) LIKE LOWER('%${v}%')")
                }
                if (k == 'comments') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(COMMENTS) LIKE LOWER('%${v}%')")
                }
                if (k == 'assessmentComments') {
                    searchCriteria.append("${searchCriteria.contains("LOWER") ? ' AND ' : ''}LOWER(GENERIC_COMMENT) LIKE LOWER('%${v}%')")
                }

            }
        }
        def sql_statement = ''
        if (isEvdas) {
            def value = dataSrc.toString().toUpperCase()
            def dataSource = DataSourceEnum."$value".value()
            sql_statement = SignalQueryHelper.product_summary_evdas_sql(productName, periodStartDate, periodEndDate, start, length, disposition, outputFormat, orderByCriteria, searchCriteria,dataSource)
        } else {
            sql_statement = SignalQueryHelper.product_summary_sql(productId, periodStartDate, periodEndDate, start, length, disposition, dataSrc, outputFormat, orderByCriteria, searchCriteria)
        }
        log.info("Product Summary Listing Sql : ${sql_statement}")
        def alertSignalList = []
        Sql sql = new Sql(dataSource)
        try {
            sql.eachRow(sql_statement, []) { row ->
                alertSignalList.add([
                        "event"            : row.PT,
                        "disposition"      : row.DISPLAY_NAME,
                        "productName"      : productName,
                        "productId"        : row.PRODUCT_ID,
                        "ptCode"           : row.PT_CODE,
                        "name"             : row.NAME,
                        "newSponCount"     : row.NEW_SPON_COUNT,
                        "cumSponCount"     : row.CUM_SPON_COUNT,
                        "eb05"             : !isEvdas ? (row.EB05 == 0.0 ? 0 : row.EB05) : '',
                        "eb95"             : !isEvdas ? (row.EB95 == 0.0 ? 0 : row.EB95) : '',
                        "requestedBy"      : row.REQUESTED_BY ?: '',
                        "alertType"        : row.VALIDATED_SIGNAL_ID ? Constants.AlertConfigType.SIGNAL_MANAGEMENT : (!isEvdas ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT : Constants.AlertConfigType.EVDAS_ALERT),
                        "executedConfigId" : row.EXEC_CONFIGURATION_ID,
                        "assignedTo"       : row.ASSIGNED_TO_ID,
                        "comment"          : row.COMMENTS ?: '',
                        "validatedSignalId": row.VALIDATED_SIGNAL_ID,
                        "totalCount"       : row.TOTAL_COUNT,
                        "filteredCount"    : row.FILTERED_COUNT,
                        "source"           : row.SELECTED_DATA_SOURCE,
                        "assessmentComment": row.GENERIC_COMMENT ?: '',
                        "configId"         : row.ALERT_CONFIGURATION_ID ?: ''
                ])
            }
        }catch (Exception e){
            log.error(e.printStackTrace())
        }finally {
            sql?.close()
        }
        alertSignalList
    }


    def getTotalCount(productName, Integer productId, periodStartDate, periodEndDate, disposition, dataSrc) {
        disposition = disposition.toString().replaceAll("[\\[\\]]", "")
        def sql_statement = null
        if (dataSrc == Constants.DataSource.EUDRA) {
            def value = dataSrc.toString().toUpperCase()
            def dataSource = DataSourceEnum."$value".value()
            sql_statement = SignalQueryHelper.product_summary_evdas_count(productName, periodStartDate, periodEndDate, disposition,dataSource)
        } else {
            sql_statement = SignalQueryHelper.product_summary_count(productId, periodStartDate, periodEndDate, disposition, dataSrc)
        }
        Integer count = 0
        Sql sql = new Sql(dataSource)
        try {
            sql.eachRow(sql_statement, []) { row ->
                count = row.cnt
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
        count
    }

}
