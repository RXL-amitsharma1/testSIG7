package com.rxlogix

import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ProductIngredientMapping
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper

@Transactional
class StatisticalComparisonService {

    def aggregateCaseAlertService
    def evdasAlertService
    def userService
    def productEventHistoryService

    //TODO: Following code is not good and requires refactoring.
    //1) The complexity is high.
    //2) Turnaround time for the loops will increase when there is increase in the data of comparing alerts.
    def getEvdasStatsComparisonData(Long id, params, orderColumnMap) {
        def length = params.length?.isInteger() ? params.length.toInteger() : 0
        def start = params.start?.isInteger() ? params.start.toInteger() : 0
        def listColName = ["productName", "soc", "pt"]
        def ec = ExecutedEvdasConfiguration.findByIdAndIsEnabled(id, true)

        def evdasAlerts = EvdasAlert.withCriteria {
            eq("executedAlertConfiguration", ec)
            if (orderColumnMap.name in listColName && orderColumnMap.name != "productName") {
                order(orderColumnMap.name, orderColumnMap.dir)
            } else if (orderColumnMap.name != "productName") {
                order("substance", orderColumnMap.dir)
            } else {
                order("lastUpdated", "desc")
            }
            maxResults(length)
            firstResult(start)
        }

        def timezone = Holders.config.server.timezone
        def startEndDate = ec.dateRangeInformation.getReportStartAndEndDate()
        def startDate = startEndDate[0]
        def endDate = DateUtil.getEndDateForCalendar(DateUtil.stringFromDate(startEndDate[1], DateUtil.DEFAULT_DATE_FORMAT, timezone), timezone)

        def argusAggAlertsCriteria = AggregateCaseAlert.createCriteria()
        def argusAggAlertResults = argusAggAlertsCriteria.list {
            between("periodEndDate", startDate, endDate)
        }.collect { it.toDto() }


        def statsData = compareAndAttach(evdasAlerts, argusAggAlertResults)
        String evdasEndDate = endDate ? DateUtil.stringFromDate(endDate, DateUtil.DEFAULT_DATE_FORMAT, userService.user.preference.timeZone) : ""
        [statsData: statsData.data, argusEndDate: statsData.argusEndDate, evdasEndDate: evdasEndDate]
    }

    Map<String, List<String>> getProductNameAndPtMapForArgusAndFaers(List<AggregateCaseAlert> aggAlerts) {
        Map<String, List<String>> productNameAndPtMap = [:]
        List tmp = []
        aggAlerts.each {
            tmp = productNameAndPtMap[it.productName]
            if (tmp) {
                tmp.add(it.pt)
            } else {
                tmp = [it.pt]
            }
            productNameAndPtMap[it.productName] = tmp
        }
        productNameAndPtMap
    }

    Map<String, List<String>> getProductNameAndPtMapForEvdas(List<EvdasAlert> aggAlerts) {
        Map<String, List<String>> productNameAndPtMap = [:]
        List tmp = []
        aggAlerts.each {
            tmp = productNameAndPtMap[it.substance]
            if (tmp) {
                tmp.add(it.pt)
            } else {
                tmp = [it.pt]
            }
            productNameAndPtMap[it.substance] = tmp
        }
        productNameAndPtMap
    }

    def getAggStatsComparisonData(Long id, params, orderColumnMap) {
        def length = params.length?.isInteger() ? params.length.toInteger() : 0
        def start = params.start?.isInteger() ? params.start.toInteger() : 0
        def listColName = ["productName", "soc", "pt"]
        def statsData
        def argusAggAlertResultsDtos
        def faersAggAlertResults
        List productNameList = []
        List preferredTermList = []

        def ec = ExecutedConfiguration.findByIdAndIsEnabled(id, true)
        Date startDate = ec.executedAlertDateRangeInformation.dateRangeStartAbsolute
        Date endDate = ec.executedAlertDateRangeInformation.dateRangeEndAbsolute

        List<AggregateCaseAlert> aggCaseAlerts = AggregateCaseAlert.withCriteria {
            eq("executedAlertConfiguration", ec)
            if (orderColumnMap.name in listColName) {
                order(orderColumnMap.name, orderColumnMap.dir)
            } else {
                order("lastUpdated", "desc")
            }
            maxResults(length)
            firstResult(start)
        }
        Map<String, List<String>> productNameAndPtMap = getProductNameAndPtMapForArgusAndFaers(aggCaseAlerts)

        if (ec.selectedDatasource == 'pva') {
            argusAggAlertResultsDtos = aggCaseAlerts.collect { it.toDto() }

            faersAggAlertResults = AggregateCaseAlert.createCriteria().list {
                between("periodEndDate", startDate, endDate)
                or {
                    productNameAndPtMap.each { productName, prefTerm ->
                        and {
                            eq('productName', productName)
                            inList('pt', prefTerm)
                        }
                    }
                }
                'executedAlertConfiguration' {
                    eq('selectedDatasource', 'faers')
                }
                if (orderColumnMap.name in listColName) {
                    order(orderColumnMap.name, orderColumnMap.dir)
                } else {
                    order("lastUpdated", "desc")
                }
            }.collect { it.toDto() }
        } else {
            faersAggAlertResults = aggCaseAlerts.collect { it.toDto() }

            argusAggAlertResultsDtos = AggregateCaseAlert.createCriteria().list {
                between("periodEndDate", startDate, endDate)
                or {
                    productNameAndPtMap.each { productName, prefTerm ->
                        and {
                            eq('productName', productName)
                            inList('pt', prefTerm)
                        }
                    }
                }
                'executedAlertConfiguration' {
                    eq('selectedDatasource', 'pva')
                }
                if (orderColumnMap.name in listColName) {
                    order(orderColumnMap.name, orderColumnMap.dir)
                } else {
                    order("lastUpdated", "desc")
                }
            }.collect { it.toDto() }
        }

        def timezone = Holders.config.server.timezone
        List<EvdasAlert> evdasAlerts = EvdasAlert.createCriteria().list {
            between("periodEndDate", startDate, DateUtil.getEndDateForCalendar(DateUtil.stringFromDate(endDate, DateUtil.DEFAULT_DATE_FORMAT, timezone), timezone))
            if (orderColumnMap.name in listColName && orderColumnMap.name != "productName") {
                order(orderColumnMap.name, orderColumnMap.dir)
            } else if (orderColumnMap.name != "productName") {
                order("substance", orderColumnMap.dir)
            } else {
                order("lastUpdated", "desc")
            }
        }

        if (ec.selectedDatasource == 'pva') {
            statsData = compareAndAttachAggAlert(argusAggAlertResultsDtos, faersAggAlertResults, evdasAlerts)
        } else {
            statsData = compareAndAttachAggAlert(faersAggAlertResults, argusAggAlertResultsDtos, evdasAlerts)
        }
        def argusEndDate = endDate ? DateUtil.stringFromDate(new Date(endDate.getTime()), DateUtil.DEFAULT_DATE_FORMAT, userService.user.preference.timeZone) : ""
        [statsData: statsData.data, argusEndDate: argusEndDate, evdasEndDate: statsData.evdasEndDate]
    }

    def compareAndAttachAggAlert(List<Map> baseDatasourceAggAlertResults, List<Map> secondaryDatasourceAggAlertResults, List<EvdasAlert> evdasAlerts) {

        def statsData = []
        Date evdasEndDate
        def prodMapping = getEvdasProductMapping()
        baseDatasourceAggAlertResults.each { Map baseAggAlert ->

            def statsDataMap = [:]

            def baseDatasource = baseAggAlert.dataSource.toLowerCase()
            statsDataMap.productName = baseAggAlert.productName
            statsDataMap.soc = baseAggAlert.soc
            statsDataMap.pt = baseAggAlert.preferredTerm
            statsDataMap.isValidationStateAchieved = baseAggAlert.isValidationStateAchieved
            statsDataMap.disposition = baseAggAlert.disposition
            statsDataMap.id = baseAggAlert.id

            statsDataMap.put("${baseDatasource}NewSponCount", baseAggAlert.newSponCount)
            statsDataMap.put("${baseDatasource}CumSponCount", baseAggAlert.cumSponCount)
            statsDataMap.put("${baseDatasource}PrrValue", baseAggAlert.prrValue)
            statsDataMap.put("${baseDatasource}RorrValue", baseAggAlert.rorValue)
            statsDataMap.put("${baseDatasource}Ebo5Value", baseAggAlert.eb05)

            def secondaryDatasource
            if (secondaryDatasourceAggAlertResults) {
                secondaryDatasourceAggAlertResults.each { Map secondaryAggAlert ->
                    secondaryDatasource = secondaryAggAlert.dataSource
                    if ((baseAggAlert.productName.toUpperCase() == secondaryAggAlert.productName.toUpperCase()) &&
                            (baseAggAlert.preferredTerm.toUpperCase() == secondaryAggAlert.preferredTerm.toUpperCase())) {
                        statsDataMap.put("${secondaryDatasource}NewSponCount", secondaryAggAlert.newSponCount)
                        statsDataMap.put("${secondaryDatasource}CumSponCount", secondaryAggAlert.cumSponCount)
                        statsDataMap.put("${secondaryDatasource}PrrValue", secondaryAggAlert.prrValue)
                        statsDataMap.put("${secondaryDatasource}RorrValue", secondaryAggAlert.rorValue)
                        statsDataMap.put("${secondaryDatasource}Ebo5Value", secondaryAggAlert.eb05)
                    }
                }
            } else {
                secondaryDatasource = baseDatasource == "pva" ? "faers" : "pva"
                statsDataMap.put("${secondaryDatasource}NewSponCount", Constants.Commons.UNDEFINED_NUM)
                statsDataMap.put("${secondaryDatasource}CumSponCount", Constants.Commons.UNDEFINED_NUM)
                statsDataMap.put("${secondaryDatasource}PrrValue", Constants.Commons.UNDEFINED_NUM)
                statsDataMap.put("${secondaryDatasource}RorrValue", Constants.Commons.UNDEFINED_NUM)
                statsDataMap.put("${secondaryDatasource}Ebo5Value", Constants.Commons.UNDEFINED_NUM)
            }
            def evdasAlertId = 0
            if (evdasAlerts) {

                evdasAlerts.each { evdasAlert ->
                    if (prodMapping[evdasAlert.substance.toUpperCase()] &&
                            (prodMapping[evdasAlert.substance.toUpperCase()].contains(baseAggAlert.productName.toUpperCase())) &&
                            (baseAggAlert.preferredTerm.toUpperCase() == evdasAlert.pt.toUpperCase()) &&
                            (baseAggAlert.id > evdasAlertId)) {
                        Date currentEndDate = evdasAlert.executedAlertConfiguration.dateRangeInformation.getReportStartAndEndDate()[1]
                        if (!evdasEndDate) {
                            evdasEndDate = currentEndDate
                        } else {
                            evdasEndDate = evdasEndDate < currentEndDate ? currentEndDate : evdasEndDate
                        }
                        evdasAlertId = evdasAlert.id
                        statsDataMap.statsId = evdasAlert.id
                        statsDataMap.newEv = evdasAlert.newEv
                        statsDataMap.totalEv = evdasAlert.totalEv
                        statsDataMap.prr = Constants.Commons.UNDEFINED_NUM
                        statsDataMap.ror = evdasAlert.rorValue
                    }
                }
            } else {
                statsDataMap.statsId = Constants.Commons.UNDEFINED_NUM
                statsDataMap.newEv = Constants.Commons.UNDEFINED_NUM
                statsDataMap.totalEv = Constants.Commons.UNDEFINED_NUM
                statsDataMap.prr = Constants.Commons.UNDEFINED_NUM
                statsDataMap.ror = Constants.Commons.UNDEFINED_NUM
            }
            statsData.add(statsDataMap)
        }
        [data: statsData, evdasEndDate: evdasEndDate ? DateUtil.stringFromDate(evdasEndDate, DateUtil.DEFAULT_DATE_FORMAT, userService.user.preference.timeZone) : ""]
    }

    def compareAndAttach(List<EvdasAlert> evdasAlerts, List argusAggAlertResults) {
        def statsData = []
        def prodMapping = getEvdasProductMapping()
        Date argusEndDate

        evdasAlerts.each { EvdasAlert evdasAlert ->

            Map statsDataMap = [:]
            statsDataMap.productName = evdasAlert.substance
            statsDataMap.soc = evdasAlert.soc
            statsDataMap.pt = evdasAlert.pt
            statsDataMap.newEv = evdasAlert.newEv
            statsDataMap.totalEv = evdasAlert.totalEv
            statsDataMap.prr = Constants.Commons.UNDEFINED_NUM
            statsDataMap.ror = evdasAlert.rorValue
            statsDataMap.isValidationStateAchieved = evdasAlert.disposition.validatedConfirmed
            statsDataMap.disposition = evdasAlert.disposition?.displayName
            statsDataMap.id = evdasAlert.id
            statsDataMap.currentDispositionId = evdasAlert.dispositionId

            def aggAlertId = 0
            if (argusAggAlertResults) {

                argusAggAlertResults.each { Map aggAlert ->
                    def selectedDatasource = aggAlert.dataSource.toLowerCase()

                    if (prodMapping[evdasAlert.substance.toUpperCase()] &&
                            (prodMapping[evdasAlert.substance.toUpperCase()].contains(aggAlert.productName.toUpperCase())) &&
                            (aggAlert.preferredTerm.toUpperCase() == evdasAlert.pt.toUpperCase()) &&
                            (aggAlert.id > aggAlertId)) {
                        aggAlertId = aggAlert.id
                        statsDataMap.statsId = aggAlert.id

                        Date currentEndDate = AggregateCaseAlert.get(aggAlert.id).periodEndDate
                        if (!argusEndDate) {
                            argusEndDate = currentEndDate
                        } else {
                            argusEndDate = argusEndDate < currentEndDate ? currentEndDate : argusEndDate
                        }
                        statsDataMap.put("${selectedDatasource}NewSponCount", aggAlert.newSponCount)
                        statsDataMap.put("${selectedDatasource}CumSponCount", aggAlert.cumSponCount)
                        statsDataMap.put("${selectedDatasource}PrrValue", aggAlert.prrValue)
                        statsDataMap.put("${selectedDatasource}RorrValue", aggAlert.rorValue)
                        statsDataMap.put("${selectedDatasource}Ebo5Value", aggAlert.eb05)
                    }
                }
            } else {
                statsDataMap.statsId = Constants.Commons.UNDEFINED_NUM
                setBlackStatsValue(statsDataMap, Constants.DataSource.PVA)
                setBlackStatsValue(statsDataMap, Constants.DataSource.FAERS)
            }
            statsData.add(statsDataMap)
        }
        [data: statsData, argusEndDate: argusEndDate ? DateUtil.stringFromDate(argusEndDate, DateUtil.DEFAULT_DATE_FORMAT, userService.user.preference.timeZone) : ""]
    }

    Map getEvdasProductMapping() {
        Map prodMapping = [:]
        ProductIngredientMapping.findAllByEnabled(true).each {
            String substanceName = JSON.parse(it.productSelection)['1'].collect { it.name }.join(', ')
            def selectedProducts = JSON.parse(it.pvaProductSelection)
            def mappedProducts = selectedProducts['2'].collect {
                it.name.toUpperCase()
            } + selectedProducts['3'].collect {
                it.name.toUpperCase()
            }
            if (prodMapping.containsKey(substanceName)) {
                def tempProdList = prodMapping[substanceName]
                prodMapping.put(substanceName, (tempProdList + mappedProducts).unique())
            } else {
                prodMapping.put(substanceName, mappedProducts)
            }
        }
        prodMapping
    }

    private setBlackStatsValue(statsDataMap, selectedDatasource) {
        ["NewSponCount", "CumSponCount", "PrrValue", "RorrValue", "Ebo5Value"].collect {
            def key = selectedDatasource + it
            if (!statsDataMap.containsKey(key)) {
                statsDataMap.put(key, Constants.Commons.UNDEFINED_NUM)
            }
        }
    }
}