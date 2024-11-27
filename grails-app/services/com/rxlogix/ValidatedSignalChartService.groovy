package com.rxlogix

import com.rxlogix.dto.SignalChartsDTO
import com.rxlogix.enums.SignalChartsEnum
import com.rxlogix.mapping.MedDraSOC
import com.rxlogix.signal.SingleCaseAlert
import grails.async.Promise
import grails.gorm.transactions.Transactional
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.transaction.annotation.Propagation

import static grails.async.Promises.task
import static grails.async.Promises.waitAll

class ValidatedSignalChartService {
    static transactional = false

    def dataSource_pva
    def signalDataSourceService
    def validatedSignalService
    def cacheService

    String mapCaseNumberFormatForProc(List<String> caseNumbers) {
        caseNumbers.collect {
            "'" + it + "'"
        }.join(",")
    }

    List fetchDateRangeFromCaseAlerts(List<SingleCaseAlert> singleCaseAlerts) {

        DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

        def tmp = singleCaseAlerts.collect {
            String date = it.getAttr(cacheService.getRptFieldIndexCache('masterInitReptDate'))
            if(date.contains('T')){
                inputFormatter.parseDateTime(date.split('T')[0])
            } else {
                inputFormatter.parseDateTime(date.split(' ')[0])
            }
        }.sort()

        def firstDate = tmp ? tmp.first().withDayOfMonth(1).withTimeAtStartOfDay() : null
        def lastDate = tmp ? tmp.last().dayOfMonth().withMaximumValue().plusDays(1).minusMillis(1) : null
        [firstDate ? outputFormatter.print(firstDate) : null, lastDate ? outputFormatter.print(lastDate) : null]
    }

    List fetchDataForDistributionBySourceOverTime(SignalChartsDTO signalChartsDTO){
        signalChartsDTO.chartType = SignalChartsEnum.SOURCE
        List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        mapFilteredDataForView(filteredData)
    }


    List fetchDataForDistributionByAgeOverTime(SignalChartsDTO signalChartsDTO) {
        signalChartsDTO.chartType = SignalChartsEnum.AGE
        List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        mapFilteredDataForView(filteredData)
    }

    List fetchDataForDistributionByCountryOverTime(SignalChartsDTO signalChartsDTO) {
        signalChartsDTO.chartType = SignalChartsEnum.COUNTRY
        List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        mapFilteredDataForView(filteredData)
    }

    List fetchDataForDistributionByGenderOverTime(SignalChartsDTO signalChartsDTO){
        signalChartsDTO.chartType = SignalChartsEnum.GENDER
        List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        mapFilteredDataForView(filteredData)
    }

    List fetchDataForDistributionByOutcome(SignalChartsDTO signalChartsDTO){
        signalChartsDTO.chartType = SignalChartsEnum.OUTCOME
        List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        mapFilteredDataForView(filteredData)
    }

    List fetchDataForDistributionBySeriousnessOverTime(SignalChartsDTO signalChartsDTO) {
        signalChartsDTO.chartType = SignalChartsEnum.SERIOUSNESS
        List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTO)
        mapSeriousnessCountDataForView(rawData)
    }

    Map fetchDataForDistributionBySystemOrganClass(SignalChartsDTO signalChartsDTO){
        signalChartsDTO.chartType = SignalChartsEnum.ORGAN
        List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        mapHeatChartDataForView(filteredData)
    }

    List mapFilteredDataForView(Map filteredData) {
        filteredData.collect { category, data ->
            ['name': category, 'data': data.collect { year, count -> ['name': year, 'y': count] }]
        }
    }

    List fetchChartDataFromRemoteDatabase(SignalChartsDTO signalChartsDTO) {

        Sql sqlObj = null
        List mainList = []

        try {
            if(signalChartsDTO.dateRange[0]&&signalChartsDTO.dateRange[1]) {
                sqlObj = new Sql(signalDataSourceService.getReportConnection(Constants.DataSource.PVA))
                validatedSignalService.initializeGTTForAssessments(sqlObj, signalChartsDTO)

                def startDate = """TO_DATE('${signalChartsDTO.dateRange[0]}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""

                def endDate = """TO_DATE('${signalChartsDTO.dateRange[1]}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""
                def procedure = "call pkg_signal_management.p_charts_data(${signalChartsDTO.signalId},${startDate},${endDate},?,?,?,?)"
                log.info(procedure)
                sqlObj.call("{${procedure}}",
                        [signalChartsDTO.groupingCode,
                         signalChartsDTO.chartType.value,
                         null,
                         Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)
                        ]
                ) { new_gen_info ->
                    if (signalChartsDTO.chartType == SignalChartsEnum.SERIOUSNESS) {
                        while (new_gen_info.next()) {
                            mainList << [new_gen_info.getAt(0), new_gen_info.getAt(1), new_gen_info.getAt(2), new_gen_info.getAt(3)]
                        }
                    } else {
                        while (new_gen_info.next()) {
                            mainList << [new_gen_info.getAt(0), new_gen_info.getAt(1), new_gen_info.getAt(2)]
                        }
                    }

                }
            }
        } catch(Throwable ex) {
            log.error(ex.getMessage())
        }finally{
            sqlObj?.close()
        }

        mainList
    }

    Map filterData(List rawData) {

        Map filteredData = [:]

        List criteria = rawData.collect { it[1] }.unique()

        List dates = rawData.collect { it[0] }.unique()

        Map defaultMapForGroup = [:]

        dates.each {
            defaultMapForGroup.put(it, null)
        }

        criteria.each {
            filteredData.put(it, defaultMapForGroup.clone())
        }

        rawData.each {
            filteredData[it[1]][it[0]] = it[2]
        }

        filteredData
    }

    List mapSeriousnessCountDataForView(List rawData) {
        def events = ["Serious","Non-Serious", "Not Available"]
        def result = []
        def tempMap
        events.eachWithIndex { event, index ->
            tempMap = [data: [], name: ""]
            rawData.each {
                tempMap.data << [
                        "name": it[0],
                        "y"   :( it[index + 1] == 0 ||  it[index + 1] == null) ? 0 : it[index + 1]
                ]
            }
            tempMap.name = event
            result << tempMap
        }
        result
    }

    Map mapHeatChartDataForView(Map filteredData) {
        def socs = filteredData.collect { soc, data ->
            soc
        }

        def years = filteredData[socs[0]].collect { year, count ->
            year
        }

        def socList = []
        MedDraSOC.withTransaction {
            socList = MedDraSOC.findAllByLangId(1).collect { it.name }
        }
        def data = []
        for (int i = 0; i < years.size(); i++) {
            for (int j = 0; j < socList.size(); j++) {
                data << [i, j, filteredData?.get(socList[j])?.get(years[i]) ?: 0]
            }
        }

        ['socs': socList, 'years': years, data: data]
    }

    List fetchChartsData(List<SignalChartsDTO> signalChartsDTOs) {

        Promise ageGroupTask = task {
            signalChartsDTOs[0].chartType = SignalChartsEnum.AGE
            List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTOs[0])
            Map filteredData = filterData(rawData)
            mapFilteredDataForView(filteredData)
        }

        Promise seriousnessTask = task {
            signalChartsDTOs[1].chartType = SignalChartsEnum.SERIOUSNESS
            List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTOs[1])
            mapSeriousnessCountDataForView(rawData)
        }

        Promise countryTask = task {
            signalChartsDTOs[2].chartType = SignalChartsEnum.COUNTRY
            List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTOs[2])
            Map filteredData = filterData(rawData)
            mapFilteredDataForView(filteredData)
        }

        Promise genderTask = task {
            signalChartsDTOs[3].chartType = SignalChartsEnum.GENDER
            List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTOs[3])
            Map filteredData = filterData(rawData)
            mapFilteredDataForView(filteredData)
        }

        Promise outcomeTask = task {
            signalChartsDTOs[4].chartType = SignalChartsEnum.OUTCOME
            List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTOs[4])
            Map filteredData = filterData(rawData)
            mapFilteredDataForView(filteredData)
        }

        Promise sourceOverTimeTask = task {
            signalChartsDTOs[5].chartType = SignalChartsEnum.SOURCE
            List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTOs[5])
            Map filteredData = filterData(rawData)
            mapFilteredDataForView(filteredData)
        }

        Promise systemOrganClassTask = task {
            signalChartsDTOs[6].chartType = SignalChartsEnum.ORGAN
            List rawData = fetchChartDataFromRemoteDatabase(signalChartsDTOs[6])
            Map filteredData = filterData(rawData)
            mapHeatChartDataForView(filteredData)
        }

        waitAll(ageGroupTask, seriousnessTask, countryTask, genderTask, outcomeTask, sourceOverTimeTask, systemOrganClassTask)
    }
}
