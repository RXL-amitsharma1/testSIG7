package com.rxlogix

import com.rxlogix.config.EvdasAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.time.TimeCategory
import groovyx.net.http.Method
import oracle.jdbc.driver.OracleTypes

@Transactional
class TrendAnalysisService {

    def reportIntegrationService
    def dataSource_pva
    def dataSource_faers
    def signalDataSourceService
    def dataSource_eudra


    def getTrendData(Long alertId = null, String alertType = null, chartType, String frequency, Boolean isFaers = false) {
        def alert = null

        def productName = Constants.Commons.BLANK_STRING
        def productIds = Constants.Commons.BLANK_STRING

        if (alertId) {
            if (isFaers || alertType.toLowerCase() == Constants.DataSource.PVA) {
                alert = AggregateCaseAlert.get(alertId)
                productName = alert.productName
                productIds = alert.productId as BigInteger
            } else {
                alert = EvdasAlert.get(alertId)
                productName = alert.substance
                productIds = alert.substanceId as Integer
            }
        }

        if (alert) {
            Integer eventIds = alert.ptCode
            def eventName = alert.pt

            //Calculate the date range based on the passed frequency
            def dateRange = calculateDateRange(frequency)

            //Calculate the product or family or substance type.
            def prod_fam_sub_flag = productDictionarySelection(alert.alertConfiguration)
            File trendFile = null
            Sql sql = null
            try {
                def milisecondsTime = new Date()
                def trendFileName = productName.replaceAll("[^a-zA-Z0-9]+", "") + eventName.replaceAll("[^a-zA-Z0-9]+", "") + milisecondsTime.getTime()
                trendFileName = (Holders.config.statistics.inputfile.path as String) + trendFileName + ".txt"

                trendFile = new File(trendFileName)


                if (alertType.toLowerCase() == Constants.DataSource.PVA) {
                    if (isFaers) {
                        sql = new Sql(dataSource_faers)
                    } else {
                        sql = new Sql(dataSource_pva)
                    }
                } else {
                    sql = new Sql(dataSource_eudra)
                }

                def timeZone = Holders.config.server.timezone
                def sDate = DateUtil.stringFromDate(dateRange[0], "dd/MM/yyyy", timeZone)
                def eDate = DateUtil.stringFromDate(dateRange[1], "dd/MM/yyyy", timeZone)
                String startDate = """TO_DATE('${sDate}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""
                String endDate = """TO_DATE('${eDate}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""

                String trendQuery = SignalQueryHelper.trend_analysis_sql(startDate, endDate)

                sql.call(trendQuery,
                        [calculateFrequencyFlag(frequency), productIds, eventIds, chartType, prod_fam_sub_flag,
                         Sql.resultSet(OracleTypes.CURSOR)]
                ) { resultSetObj ->

                    if (resultSetObj) {
                        while (resultSetObj.next()) {
                            def rsMetaDataDss = resultSetObj.getMetaData()
                            def rowSet = ""
                            //Prepare the rowset with pipe '|' as delimiter.
                            for (int i = 1; i <= rsMetaDataDss.getColumnCount(); i++) {
                                String columnName = rsMetaDataDss.getColumnName(i)
                                rowSet = rowSet + resultSetObj.getString(columnName) + "|"
                            }
                            //Remove the last delimiter.
                            rowSet = rowSet.substring(0, rowSet.length() - 1)
                            println "--|         "+ rowSet
                            trendFile << "$rowSet" + "\n"
                        }
                    }
                }

                def url = Holders.config.statistics.url
                def path = Holders.config.statistics.path.trendanalysis
                def query = [trendFileName: trendFileName]
                def jsonData = reportIntegrationService.postData(url, path, query, Method.POST)

                jsonToCsv(jsonData)
            } catch (java.sql.SQLException sqe) {
                sqe.printStackTrace()
                [status: 500]
            } catch (Exception e) {
                e.printStackTrace()
                [status: 500]
            } finally {
                trendFile?.delete()
                sql?.close()
            }
        } else {
            [status: 500]
        }
    }

    def jsonToCsv(jsonData) {
        def stringObj = ""
        def testMap = jsonData.result
        testMap.eachWithIndex { obj, index ->
            def keySet = obj.keySet()
            def keys = keySet
            def tmpString = Constants.Commons.BLANK_STRING
            keys.each { key ->
                def fetchedObj = obj.get(key)
                if (key == "RETRIEVALDATE") {
                    if (tmpString) {
                        tmpString = fetchedObj + "," + tmpString + ","
                    } else {
                        tmpString = fetchedObj + ","
                    }
                } else {
                    tmpString = tmpString + fetchedObj + ","
                }
            }
            tmpString = tmpString.substring(0, tmpString.length() - 1)
            stringObj = stringObj + tmpString + "\n"
        }
        println "_______________________Eventually the data coming is _______________________"
        println stringObj
        println "_____________________________________________________________________________"

        stringObj
    }

    def calculateDateRange(String frequency) {
        def nodeCount = 36
        def dateRange = []
        def startDate = null
        def endDate = new Date()
        switch (frequency) {
            case "Monthly" :
                use(TimeCategory) {
                    startDate = endDate - nodeCount.months
                    dateRange = [startDate, endDate]
                }
                break;
            case "Quarterly" :
                use(TimeCategory) {
                    startDate = endDate - (nodeCount*3).months
                    dateRange = [startDate, endDate]
                }
                break;
            case "Yearly" :
                use(TimeCategory) {
                    startDate = endDate - nodeCount.years
                    dateRange = [startDate, endDate]
                }
                break;

            default :
                startDate = endDate - nodeCount.months
                dateRange = [startDate, endDate]
                break

        }
        dateRange
    }

    def calculateFrequencyFlag(frequency) {
        def frequencyFlag = 1
        switch (frequency) {
            case "Quarterly" :
                frequencyFlag = 3
                break;
            case "Yearly" :
                frequencyFlag = 2
                break;
            default :
                frequencyFlag = 1
                break
        }
        frequencyFlag
    }

    def productDictionarySelection(configuration) {
        def productSelection = configuration.productSelection
        def jsonSlurper = new JsonSlurper()
        def ingrediant, family, productName, tradeName
        if (productSelection) {
            def productSelectionObj = jsonSlurper.parseText(productSelection)
            productSelectionObj.find { k, v->
                if (k == "1") {
                    ingrediant = v
                }
                if (k == "2") {
                    family = v
                }
                if (k == "3") {
                    productName = v
                }
                if (k == "4") {
                    tradeName = v
                }
            }
            if (!ingrediant && family && !productName && !tradeName) {
                return 2
            } else if (ingrediant && !family && !productName && !tradeName) {
                return 3
            } else if (!ingrediant && !family && productName && !tradeName) {
                return 1
            } else {
                return 0
            }
        } else {
            return 0
        }
    }

}
