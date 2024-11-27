package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.ActionStatus
import com.rxlogix.enums.ReportFormat
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.config.ViewConfig
import com.rxlogix.signal.*
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.apache.commons.lang3.time.DateUtils
import org.joda.time.DateTime
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.joda.time.DateTimeZone

import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.zip.GZIPInputStream

import static com.rxlogix.util.DateUtil.*
import static grails.async.Promises.task

@Transactional
@Slf4j
class ReportService implements AlertUtil, EventPublisher {

    def userService
    def productEventHistoryService
    def caseHistoryService
    def dynamicReportService
    def validatedSignalService
    def alertCommentService
    def pvsProductDictionaryService
    def productBasedSecurityService
    def signalDataSourceService
    def alertService
    def cacheService
    def sessionFactory
    def dataSource
    def alertAttributesService
    def dataSource_pva
    def dataSource_faers
    def dataSource_eudra
    def sqlGenerationService
    def dataObjectService

    Map fetchDataForIndividualCasesChart(List dateRange, List productList, List substanceList, String dataSource) {
        Map result = [:]
        result.put('individualCasesByAgeChart', getIndividualCasesByAgeData(dateRange, productList, substanceList, 1, dataSource))
        result.put('individualCasesByGenderChart', getIndividualCasesByGenderData(dateRange, productList, substanceList, 2, dataSource))
        result.put('individualCasesByRegionChart', getIndividualCasesByRegionData(dateRange, productList, substanceList, 3, dataSource))
        result

    }

    Map fetchDataForReactionGroupChart(List dateRange, List productList, List substanceList, List socList, String dataSource) {
        Map result = [:]
        result.put('ageAndGenderGroupChart', getReactionAgeAndGenderGroupChart(dateRange, productList, substanceList, socList, 4, dataSource))
        result.put('reporterGroupChart', getReactionReporterGroupChart(dateRange, productList, substanceList, socList, 4, dataSource))
        result.put('geographicRegionChart', getReactionRegionGroupChart(dateRange, productList, substanceList, socList, 4, dataSource))
        result
    }

    Map fetchDataForReactionChart(List dateRange, List productList, List substanceList, Long SOCId, List ptList, String dataSource) {
        Map result = [:]
        result.put('reactionChart', getReactionAgeAndGenderChart(dateRange, productList, substanceList, SOCId, ptList, 5, dataSource))
        result.put('reporterReactionChart', getReactionReporterChart(dateRange, productList, substanceList, SOCId, ptList, 5, dataSource))
        result.put('reactionOutcomeChart', getReactionOutcomeChart(dateRange, productList, substanceList, SOCId, ptList, 5, dataSource))
        result
    }

    Map fetchDataForIndividualCasesByReactionGroupChart(List dateRange, List productList, List substanceList, String dataSource) {
        Map result = [:]
        result.put("reactionGroupByRegionChart", getIndividualCaseReactionGroupRegionChart(dateRange, productList, substanceList, 6, dataSource))
        result.put("reactionGroupByReporterChart", getIndividualCaseReactionGroupReporterChart(dateRange, productList, substanceList, 6, dataSource))
        result.put("reactionGroupByAgeChart", getIndividualCaseReactionGroupAgeChart(dateRange, productList, substanceList, 6, dataSource))
        result.put("reactionGroupByGenderChart", getIndividualCaseReactionGroupGenderChart(dateRange, productList, substanceList, 6, dataSource))
        result
    }

    def readReportResultJson(ReportResult reportResult) {
        if (reportResult?.data?.value) {
            def br = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new ByteArrayInputStream(reportResult?.data?.value))))

            def line = null
            def sb = new StringBuilder()
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n")
            }
            br.close()
            sb.toString()
        } else
            null
    }

    def getPTsFromSoc(String soc) {
        Sql sql = new Sql(dataSource_pva)
        def sql_statement = SignalQueryHelper.soc_pt_sql(soc)
        def ptList = []
        try {
            sql.eachRow(sql_statement, []) { row ->
                ptList.add([name: row.getString('pt_name'), "id": row.getString('pt_code')])
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        ptList.sort()
    }

    def getIndividualCasesByAgeData(List dateRange, List productList, List substanceList, Integer groupId, String dataSource,Boolean isMultiIngredient = false) {
        def resultSet = fetchEvdasChartsData(dateRange, productList, substanceList, [], [], groupId, null, dataSource, isMultiIngredient)
        ['data': resultSet.collect { it[0] }, 'categories': resultSet.collect { it[1] }]
    }

    def getIndividualCasesByGenderData(List dateRange, List productList, List substanceList, Integer groupId, String dataSource) {
        def resultSet = fetchEvdasChartsData(dateRange, productList, substanceList, [], [], groupId, null, dataSource)
        ['data': resultSet.collect { it[0] }, 'categories': resultSet.collect { it[1] }]
    }

    def getIndividualCasesByRegionData(List dateRange, List productList, List substanceList, Integer groupId, String dataSource, Boolean isMultiIngredient = false) {
        def resultSet = fetchEvdasChartsData(dateRange, productList, substanceList, [], [], groupId, null, dataSource,isMultiIngredient)
        ['data': resultSet.collect { it[0] }, 'categories': resultSet.collect { it[1] }]
    }

    def getReactionAgeAndGenderGroupChart(List dateRange, List productList, List substanceList, List socList, Integer groupId, String dataSource,Boolean isMultiIngredient = false) {
        filterDataForStackedChart(fetchEvdasChartsData(dateRange, productList, substanceList, socList, [], groupId, 5, dataSource, isMultiIngredient))
    }

    def getReactionReporterGroupChart(List dateRange, List productList, List substanceList, List socList, Integer groupId, String dataSource, Boolean isMultiIngredient = false) {
        def resultSet = fetchEvdasChartsData(dateRange, productList, substanceList, socList, [], groupId, 3, dataSource, isMultiIngredient)
        ['data': resultSet.collect { it[0] }, 'categories': resultSet.collect { it[1] }]
    }

    def getReactionRegionGroupChart(List dateRange, List productList, List substanceList, List socList, Integer groupId, String dataSource, Boolean isMultiIngredient = false) {
        def resultSet = fetchEvdasChartsData(dateRange, productList, substanceList, socList, [], groupId, 4, dataSource, isMultiIngredient)
        ['data': resultSet.collect { it[0] }, 'categories': resultSet.collect { it[1] }]
    }

    def getReactionAgeAndGenderChart(List dateRange, List productList, List substanceList, Long SOCId, List ptList, Integer groupId, String dataSource) {
        filterDataForStackedChart(fetchEvdasChartsData(dateRange, productList, substanceList, [SOCId], ptList, groupId, 1, dataSource))
    }

    def getReactionReporterChart(List dateRange, List productList, List substanceList, Long SOCId, List ptList, Integer groupId, String dataSource) {
        def resultSet = fetchEvdasChartsData(dateRange, productList, substanceList, [SOCId], ptList, groupId, 2, dataSource)
        ['data': resultSet.collect { it[0] }, 'categories': resultSet.collect { it[1] }]
    }

    def getReactionOutcomeChart(List dateRange, List productList, List substanceList, Long SOCId, List ptList, Integer groupId, String dataSource) {
        def resultSet = fetchEvdasChartsData(dateRange, productList, substanceList, [SOCId], ptList, groupId, 3, dataSource)
        ['data': resultSet.collect { it[0] }, 'categories': resultSet.collect { it[1] }]
    }

    def getIndividualCaseReactionGroupAgeChart(List dateRange, List productList, List substanceList, Integer groupId, String dataSource,Boolean isMultiIngredient = false) {
        filterDataForStackedChart(fetchEvdasChartsData(dateRange, productList, substanceList, [], [], groupId, 1, dataSource,isMultiIngredient))
    }

    def getIndividualCaseReactionGroupGenderChart(List dateRange, List productList, List substanceList, Integer groupId, String dataSource,Boolean isMultiIngredient = false) {
        filterDataForStackedChart(fetchEvdasChartsData(dateRange, productList, substanceList, [], [], groupId, 2, dataSource,isMultiIngredient))
    }

    def getIndividualCaseReactionGroupReporterChart(List dateRange, List productList, List substanceList, Integer groupId, String dataSource,Boolean isMultiIngredient = false) {
        filterDataForStackedChart(fetchEvdasChartsData(dateRange, productList, substanceList, [], [], groupId, 3, dataSource,isMultiIngredient))
    }

    def getIndividualCaseReactionGroupRegionChart(List dateRange, List productList, List substanceList, Integer groupId, String dataSource,Boolean isMultiIngredient =false) {
        filterDataForStackedChart(fetchEvdasChartsData(dateRange, productList, substanceList, [], [], groupId, 4, dataSource,isMultiIngredient))
    }

    Map filterDataForStackedChart(List resultSet) {
        def categories = resultSet.collect { it[1] }.unique()
        def data = []
        def result = [:]
        resultSet.collect { it[2] }.unique().each {
            def tmpMap = [:]
            resultSet.collect {
                it[1]
            }.unique().each {
                tmpMap.put(it, 0)
            }
            result.put(it, tmpMap)
        }
        resultSet.each {
            result[it[2]][it[1]] = it[0]
        }
        result.keySet().each { age ->
            def tmpL = []
            categories.each { gender ->
                tmpL << result[age][gender]
            }
            data << ["data": tmpL, "name": age]
        }

        ['data': data, 'categories': categories]
    }

    List fetchEvdasChartsData(List dateRange, List productList, List substanceList, List socList, List reactionList, Integer groupId, Integer subGroupId, String dataSource, Boolean isMultiIngredient = false) {
        List mainList = []

        final Sql sqlObj = new Sql(signalDataSourceService.getDataSource(dataSource))
        try {
            String insertGttStatement = sqlGenerationService.initializeEvdasChartGtt(isMultiIngredient)
            String timeZone = Holders.config.server.timezone
            def sDate = DateUtil.stringFromDate(dateRange[0], "dd/MM/yyyy", timeZone)
            def eDate = DateUtil.stringFromDate(dateRange[1], "dd/MM/yyyy", timeZone)

            def startDate = """TO_DATE('${sDate}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""

            def endDate = """TO_DATE('${eDate}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""

            sqlObj.call("{call p_evdas_charts(?,?,${startDate},${endDate},?,?,?,?,?)}",
                    [productList?.join(","),
                     substanceList?.join(","),
                     groupId,
                     subGroupId,
                     socList?.join(","),
                     reactionList?.join(","),
                     Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)
                    ]
            ) { new_gen_info ->
                if (new_gen_info != null) {
                    while (new_gen_info.next()) {
                        if (groupId in [1, 2, 3, 4, 5]) {
                            if ((groupId == 5 && subGroupId == 1) || (groupId == 4 && subGroupId == 5)) {
                                mainList << [new_gen_info.getAt(0), new_gen_info.getAt(1), new_gen_info.getAt(2)]
                            } else {
                                mainList << [new_gen_info.getAt(0), new_gen_info.getAt(1)]
                            }
                        } else {
                            mainList << [new_gen_info.getAt(0), new_gen_info.getAt(1), new_gen_info.getAt(2)]
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace()
        }finally{
            sqlObj?.close()
        }
        mainList
    }

    def saveICSRsReportHistory(Map params) {
        ReportHistory reportHistory = ReportHistory.get(params.reportHistory)
        List productSelectionIds
        List productIngredientIds
        def selectedSocListIds = params["socList[]"]
        def selectedSocListNames = (params["socListName[]"] instanceof String) ? [params["socListName[]"]] : params["socListName[]"]
        def selectedPtListIds = params["ptList[]"]
        def selectedPtListNames = (params["ptListName[]"] instanceof String) ? [params["ptListName[]"]] : params["ptListName[]"]
        def selectedSOCForReaction = params.selectedSOCForReaction
        Long selectedSOCIdForReaction = params.selectedSOCIdForReaction as Long
        List<String> dateRange = [reportHistory.startDate, reportHistory.endDate]

        String dataSource = reportHistory.dataSource
        if (params.productSelection) {
            Map productSelectionMap = parseProductSelectionMap(JSON.parse(params.productSelection))
            productSelectionIds = productSelectionMap.productSelectionIds
            productIngredientIds = productSelectionMap.productIngredientIds
            if((productIngredientIds.isEmpty() && productSelectionMap.productSubstanceIds)){
                productIngredientIds = productSelectionMap.productSubstanceIds
            }
        } else {
            Map productSelectionMap = parseProductGroupSelectionList([params["productGroupIds[]"]].flatten())
            productSelectionIds = productSelectionMap.productSelectionIds
        }
        reportHistory.memoReport = null
        reportHistory.save(flush: true)
        task {
            try {
                log.info("Preparing data for ICSR Report")
                Map result = [:]
                result.put('reactionGroupSelectedSOCs', selectedSocListNames)
                result.put('selectedPtListNames', selectedPtListNames)
                result.put('selectedSOCForReaction', selectedSOCForReaction)
                result.put('individualCasesChart', fetchDataForIndividualCasesChart(dateRange, productSelectionIds, productIngredientIds, dataSource))
                result.put('individualCasesByReactionGroup', fetchDataForIndividualCasesByReactionGroupChart(dateRange, productSelectionIds, productIngredientIds, dataSource))
                result.put('reactionGroup', fetchDataForReactionGroupChart(dateRange, productSelectionIds, productIngredientIds, [selectedSocListIds].flatten(), dataSource))
                result.put('reaction', fetchDataForReactionChart(dateRange, productSelectionIds, productIngredientIds, selectedSOCIdForReaction, [selectedPtListIds].flatten(), dataSource))
                ReportHistory.withTransaction {
                    log.info("Saving ICSR Report")
                    ReportHistory history = ReportHistory.get(reportHistory.id)
                    history.memoReport = (result as JSON).toString().getBytes(StandardCharsets.UTF_8)
                    history.isReportGenerated = true
                    history.save(flush: true, failOnError: true)
                    log.info("ICSR Report Saved")
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    List getDateRangeBasedOnType(startDate, endDate, dateRangeType) {
        List dateRange
        String timeZone = Holders.config.server.timezone
        switch (dateRangeType) {
            case "CUSTOM":
                Date sDate = DateUtil.stringToDate(startDate, DateUtil.DEFAULT_DATE_FORMAT, timeZone)
                Date eDate = DateUtil.getEndDateForCalendar(endDate, timeZone)
                dateRange = [sDate, eDate]
                break
            case "CUMULATIVE":
                dateRange = [new Date(Constants.DateFormat.MIN_DATE), new Date()]
            default:
                dateRange = [new Date(Constants.DateFormat.MIN_DATE), new Date()]
        }
        dateRange
    }

    Map parseProductSelectionMap(Map productSelectionMap) {

        //ingredietnOrderId, and productOrderId are added to handle dynamic nature of PV Dictionary.
        String ingredientOrderId
        String productOrderId
        String familyOrderId
        String substanceOrderId
        String productDosageOrderId
        String tradeOrderId
        DictionaryMapping.withTransaction {
            String ingredient = Holders.config.dictionary.ingredient.column.name
            String product = Holders.config.dictionary.product.column.name
            String family = Holders.config.dictionary.family.column.name
            String substance = Holders.config.dictionary.substance.column.name
            String productDosage = Holders.config.dictionary.dosage.forms.column.name
            String trade = Holders.config.dictionary.trade.column.name
            ingredientOrderId = ingredient ? DictionaryMapping.findByLabel(ingredient)?.getId() : ""
            productOrderId = product ? DictionaryMapping.findByLabel(product)?.getId() : ""
            familyOrderId = family ? DictionaryMapping.findByLabel(family)?.getId() : ""
            substanceOrderId=substance?DictionaryMapping.findByLabel(substance)?.getId() : ""
            productDosageOrderId=productDosage?DictionaryMapping.findByLabel(productDosage)?.getId() : ""
            tradeOrderId=trade?DictionaryMapping.findByLabel(trade)?.getId() : ""
        }

        def productSelectionIds = productSelectionMap.collect { k, v ->
            if (k != ingredientOrderId && k != familyOrderId) {
                v*.id
            }
        }.flatten().unique() - null
        def productIngredientIds = productSelectionMap[ingredientOrderId].collect { it.id }
        def productSelectionNames = productSelectionMap.collect { k, v -> v*.name }?.flatten()?.unique()?.join(",")
        def productFamilyIds = productSelectionMap[familyOrderId].collect { it.id }
        def productSubstanceIds=productSelectionMap[substanceOrderId].collect { it.id }
        def productDosageIds=productSelectionMap[productDosageOrderId].collect { it.id }
        def productTradeIds=productSelectionMap[tradeOrderId].collect { it.id }
        ["productSelectionIds": productSelectionIds, "productIngredientIds": productIngredientIds, "productSelectionNames": productSelectionNames, "productFamilyIds": productFamilyIds,productSubstanceIds:productSubstanceIds,productDosageIds:productDosageIds,productTradeIds:productTradeIds]
    }

    Map parseProductGroupSelectionList(List productGroupIds) {
        List productGroups = ProductGroup.findAllByIdInList(productGroupIds)
        def productSelectionIds = productGroups*.productSelection.collect {
            JSON.parse(it).collect { k, v -> v*.id }
        }.flatten().unique()
        def productSelectionNames = productGroups*.productSelection.collect {
            JSON.parse(it).collect { k, v -> v*.name }
        }.flatten().unique().join(",")
        ["productSelectionIds": productSelectionIds, "productSelectionNames": productSelectionNames]
    }

    void generateEventForMemoReport(ReportHistory reportHistory,Map productSelectionMap, Boolean isIngredient = false) {
        //Encapsulating it in the task so that the thread runs in backgroud without halting the current execution flow.
        notify 'generate.memo.report.event.published', [productSelectionMap: productSelectionMap, reportHistory: reportHistory, isIngredient: isIngredient]
    }

    def generatePBRERReport(Date startDate, Date endDate, Map productSelectionMap, Long reportHistoryId, String dataSource) {
        Date closedDate
        User user = userService.getUser()
        String timezone = user?.preference?.getTimeZone()
        List allowedProducts = productBasedSecurityService.allAllowedProductForUser(userService.getUser())
        task {
            log.info("PBRER Report data filtering started.")
            try {
                List reportDataList = []
                fetchMatchingSignals(startDate, endDate, productSelectionMap, true)?.each { signal ->
                        closedDate = null
                        signal.signalStatusHistories?.find {
                            if (it.signalStatus == "Date Closed") {
                                closedDate = it.dateCreated
                                return true
                            }
                        }
                        Map reportData = [
                                'signalName'              : signal.topic ?: signal.name,
                                'dateDetected'            : signal.detectedDate ? toDateString(signal.detectedDate) : '-',
                                'status'                  : fetchSignalStatus(startDate, endDate, signal.detectedDate, closedDate),
                                'dateClosed'              : closedDate > endDate || closedDate == null ? Constants.Commons.DASH_STRING : toDateString(closedDate),
                                'signalSource'            : signal.initialDataSource ? signal.initialDataSource.replace('[', '').replace(']', '').replace(Constants.Commons.SIGNAL_DATASOURCE_SEPERATOR , ", ") : '-',
                                'signalSummary'           : signal.genericComment,
                                'methodOfSignalEvaluation': signal.reasonForEvaluation,
                                'actionTaken'             : closedDate > endDate || closedDate == null ? "" : signal.actionTaken.join(",") + "\n\n" + signal.signalOutcomes.name.join(", "),
                                'evaluationMethod'        : signal.evaluationMethod.toString().replace('[', '').replace(']', ''),
                                'reasonForEvaluation'     : signal.reasonForEvaluation
                        ]

                        Set<AggregateCaseAlert> aggAlertList = signal.aggregateAlerts

                        List filledPt = []
                        if (aggAlertList) {
                            aggAlertList.each {
                                if (!filledPt.contains(it.pt)) {
                                    Map data = ["signalTerm": it.pt]
                                    data = data + reportData
                                    reportDataList.add(data)
                                    filledPt.add(it.pt)
                                }
                            }
                        } else {
                            Map data = ["signalTerm": signal.getEventSelectionList()?.join(',')]
                            data += reportData
                            reportDataList.add(data)
                        }
                    }
                String reportingInterval = "${DateUtil.toDateString1(DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH))} to ${DateUtil.toDateString1(DateUtils.truncate(endDate, Calendar.DAY_OF_MONTH))}"

                String productsName = productSelectionMap['productSelectionNames']

                ReportHistory.withTransaction {
                    ReportHistory reportHistory = ReportHistory.get(reportHistoryId)
                    reportHistory.memoReport = (["reportingInterval": reportingInterval, "productsName": productsName, "reportDataList": reportDataList] as JSON).toString().getBytes(StandardCharsets.UTF_8)
                    reportHistory.isReportGenerated = true
                }
                log.info("PBRER Report data filtering complete.")
            } catch (Exception e) {
                e.printStackTrace()
                updateReportHistory(reportHistoryId)
            }
        }
    }

    String fetchSignalStatus(Date startDate, Date endDate, Date detectedDate, Date closedDate) {
        String signalStatus = Constants.Commons.DASH_STRING
        if (detectedDate >= startDate && detectedDate <= endDate) {
            signalStatus = Holders.config.pvsignal.pbrerReport.signalStatus.newSignal
            if (closedDate >= startDate && closedDate <= endDate) {
                signalStatus = Holders.config.pvsignal.pbrerReport.signalStatus.newAndClosed
            }
        } else if (detectedDate < startDate) {
            if (closedDate >= startDate && closedDate <= endDate) {
                signalStatus = Holders.config.pvsignal.pbrerReport.signalStatus.closed
            } else if (closedDate > endDate || closedDate == null) {
                signalStatus = Holders.config.pvsignal.pbrerReport.signalStatus.ongoing
            }
        }
        return signalStatus
    }

    def generateSignalStateReport(Date startDate, Date endDate, Map productSelectionMap, Long reportHistoryId, String dataSource) {

        def user = userService.getUser()
        String timezone = user?.preference?.getTimeZone()
        def allowedProducts = productBasedSecurityService.allAllowedProductForUser(userService.getUser())

        task {
            log.info("Signal state report data filtering started.")
            def reportDataList = []
            try {
                ValidatedSignal.withTransaction {
                    reportDataList = fetchMatchingSignals(startDate, endDate, productSelectionMap)?.collect {
                        it.toStateReportExport(timezone)
                    }
                }
                def reportingInterval = toDateStringWithoutTimezone(startDate) +
                        " to " + toDateStringWithoutTimezone(endDate)
                ReportHistory.withTransaction {
                    ReportHistory reportHistory = ReportHistory.get(reportHistoryId)
                    reportHistory.memoReport = (["reportingInterval": reportingInterval, "productsName": processReportHistory(reportHistory), "reportDataList": reportDataList] as JSON).toString().getBytes(StandardCharsets.UTF_8)
                    reportHistory.isReportGenerated = true
                }
                log.info("Signal state report data filtering complete.")
            } catch (Exception e) {
                e.printStackTrace()
                updateReportHistory(reportHistoryId)
            }
        }
    }

    def generateProductActionsReport(Date startDate, Date endDate, Map productSelectionMap, Long reportHistoryId, String dataSource) {

        def user = userService.getUser()
        String timezone = user?.preference?.getTimeZone()
        def allowedProducts = productBasedSecurityService.allAllowedProductForUser(userService.getUser())

        task {
            log.info("Product action report data filtering started.")
            def reportDataList = []
            try {
                ValidatedSignal.withTransaction {
                    fetchMatchingSignals(startDate, endDate, productSelectionMap)?.each { ValidatedSignal signal ->
                        signal.actions?.each { action ->
                            reportDataList << [
                                    signalName    : signal.name,
                                    actionName    : action.config.displayName,
                                    actionType    : action.meetingId ? Meeting.get(action.meetingId)?.meetingTitle : action.type.displayName,
                                    assignedTo    : action.assignedTo ? action.assignedTo.fullName : (action.assignedToGroup ? action.assignedToGroup.name : action.guestAttendeeEmail),
                                    status        : ActionStatus[action.actionStatus].id,
                                    creationDate  : action.createdDate ? toDateString(action.createdDate, timezone) : "-",
                                    completionDate: action.completedDate ? DateUtil.toDateString(action.completedDate, "UTC") : "-",
                                    details       : action.details,
                                    comments      : action.comments
                            ]
                        }
                    }
                }
                if (!reportDataList) {
                    reportDataList << [
                            signalName    : Constants.Commons.DASH_STRING,
                            actionName    : Constants.Commons.DASH_STRING,
                            actionType    : Constants.Commons.DASH_STRING,
                            assignedTo    : Constants.Commons.DASH_STRING,
                            status        : Constants.Commons.DASH_STRING,
                            creationDate  : Constants.Commons.DASH_STRING,
                            completionDate: Constants.Commons.DASH_STRING,
                            details       : Constants.Commons.DASH_STRING,
                            comments      : Constants.Commons.DASH_STRING
                    ]
                }
                def reportingInterval = toDateStringWithoutTimezone(startDate) +
                        " to " + toDateStringWithoutTimezone(endDate)
                def productsName = productSelectionMap['productSelectionNames']

                ReportHistory.withTransaction {
                    ReportHistory reportHistory = ReportHistory.get(reportHistoryId)
                    reportHistory.memoReport = (["reportingInterval": reportingInterval, "productsName": productsName, "reportDataList": reportDataList] as JSON).toString().getBytes(StandardCharsets.UTF_8)
                    reportHistory.isReportGenerated = true
                }
                log.info("Product action report data filtering complete.")
            } catch (Exception e) {
                e.printStackTrace()
                updateReportHistory(reportHistoryId)
            }
        }
    }

    def generateSignalSummaryReport(Date startDate, Date endDate, Map productSelectionMap, Long reportHistoryId, String dataSource) {

        def user = userService.getUser()
        String timezone = user?.preference?.getTimeZone()
        def allowedProducts = productBasedSecurityService.allAllowedProductForUser(userService.getUser())

        task {
            log.info("Signal summary report data filtering started.")
            def reportDataList = [:]
            reportDataList.signalSummaryData = []
            reportDataList.signalDetails = []
            reportDataList.workflowLog = []
            reportDataList.references = []
            reportDataList.rmmType = []
            reportDataList.communication = []
            reportDataList.adhocCaseAlertListingData = []
            reportDataList.quantitativeCaseAlertListingData = []
            reportDataList.qualitativeCaseAlertListingData = []
            reportDataList.literatureCaseAlertListingData = []
            reportDataList.commentList = []
            reportDataList.actionsList = []
            reportDataList.meetingList = []
            reportDataList.assessmentDetailsList = []
            reportDataList.medicalConceptDistributionList = []

            try {
                ValidatedSignal.withTransaction {
                    fetchMatchingSignals(startDate, endDate, productSelectionMap)?.each { validatedSignal ->
                        String initialDataSource = validatedSignal?.initialDataSource?.replace("##", ", ")
                        reportDataList.signalSummaryData << [
                                'signalId'          : validatedSignal.id + "",
                                'signalName'        : validatedSignal.name,
                                'product'           : validatedSignal.getProductAndGroupNameList()?.join(",") ?: "-",
                                'event'             : alertService.eventSelectionSignalWithSmq(validatedSignal),
                                "detectedDate"      : validatedSignal.detectedDate ? fromDateToString(validatedSignal.detectedDate, DEFAULT_DATE_FORMAT) : "-",
                                "status"            : validatedSignalService.getSignalStatus(validatedSignal),
                                "closedDate"        : DateUtil.fromDateToString(validatedSignal.signalStatusHistories?.find {it.signalStatus == Constants.DATE_CLOSED}?.dateCreated, DateUtil.DEFAULT_DATE_FORMAT) ?: "-",
                                "signalSource"      : initialDataSource,
                                "signalOutcome"     : validatedSignal.signalOutcomes*.name?.join(", "),
                                "actionTaken"       : validatedSignal.actionTaken.join(', ') ?: "-",
                                "udText1"           : validatedSignal.udText1,
                                "udText2"           : validatedSignal.udText2,
                                "udDropdown1"       : validatedSignal.ddValue1,
                                "udDropdown2"       : validatedSignal.ddValue2,
                                "udDate1"           : validatedSignal.udDate1? DateUtil.fromDateToString(validatedSignal.udDate1, DEFAULT_DATE_FORMAT) : null,
                                "udDate2"           : validatedSignal.udDate2? DateUtil.fromDateToString(validatedSignal.udDate2, DEFAULT_DATE_FORMAT) : null
                        ]

                        reportDataList.signalDetails << [
                                "signalName"         : validatedSignal.name,
                                "linkedSignal"       : validatedSignal.linkedSignals*.name?.join(", "),
                                "evaluationMethod"   : validatedSignal.evaluationMethod?.join(", ") ?: "-",
                                "risk/Topic"         : validatedSignal.topicCategories*.name?.join(", "),
                                "reasonForEvaluation": validatedSignal.reasonForEvaluation,
                                "comments"           : validatedSignal.genericComment,
                        ]

                        def workFlowLogMap =[:]
                        List<String> statusLists=alertAttributesService.getUnsorted('signalHistoryStatus');
                        if(SystemConfig.first().displayDueIn)
                        {
                            statusLists.add(Constants.WorkFlowLog.DUE_DATE)
                        }
                        String validationDateStr = Holders.config.signal.defaultValidatedDate;
                        List<String> sortedList=alertAttributesService.get('signalHistoryStatus');
                        if(SystemConfig.first().displayDueIn)
                        {
                            sortedList.add(Constants.WorkFlowLog.DUE_DATE)
                        }
                        List<SignalStatusHistory> signalStatusHistoryList = validatedSignal.signalStatusHistories;
                        sortedList.each { String status ->
                            SignalStatusHistory signalStatusHistory = signalStatusHistoryList.find {
                                it.signalStatus == status
                            }
                            String dateKey = "${status.trim().replace(' ', '').toLowerCase()}Date"
                            if(validationDateStr.equalsIgnoreCase(status)){

                                workFlowLogMap.put(dateKey, signalStatusHistory?.dateCreated ? DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT) : null)
                                workFlowLogMap.put("validationdateDate", signalStatusHistory?.dateCreated ? DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT) : null)
                            } else if (dateKey.equals("duedateDate")) { // Added for PVS-64838
                                workFlowLogMap.put(dateKey, validatedSignal?.actualDueDate ? DateUtil.toDateStringPattern(validatedSignal?.actualDueDate, DateUtil.DATEPICKER_FORMAT) : null)
                            }else{
                                workFlowLogMap.put(dateKey, signalStatusHistory?.dateCreated ? DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT) : null)
                            }
                        }
                        workFlowLogMap.putAll([
                                'signalName'   : validatedSignal.name,
                                'priority'     : validatedSignal.priority.displayName ,
                                'disposition'  : validatedSignal.disposition.displayName ,
                                'assignedTo'   : validatedSignal.assignedTo ? validatedSignal.assignedTo.fullName : validatedSignal.assignedToGroup.name
                        ])

                        reportDataList.workflowLog.add(workFlowLogMap)

                        validatedSignal?.getReferences()?.each {
                            AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(it)
                            reportDataList.references << [
                                    signalName : validatedSignal.name,
                                    inputName  : it.inputName,
                                    link       : it.referenceLink,
                                    referenceType: it.referenceType,
                                    description: attachmentDescription?.description ?: Constants.Commons.BLANK_STRING,
                                    timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timezone),
                                    modifiedBy : attachmentDescription?.createdBy
                            ]
                        }

                        validatedSignal?.attachments.each {
                            AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(it)
                            reportDataList.references << [
                                    signalName : validatedSignal.name,
                                    inputName  : it.inputName,
                                    link       : it.inputName,
                                    referenceType: it.referenceType,
                                    description: attachmentDescription?.description ?: Constants.Commons.BLANK_STRING,
                                    timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timezone),
                                    modifiedBy : attachmentDescription?.createdBy
                            ]
                        }

                        validatedSignal.signalRMMs.findAll {
                            it.communicationType == "rmmType"
                        }.each {
                            reportDataList.rmmType << [
                                    signalName          : validatedSignal.name,
                                    type                : it.type,
                                    description         : it.description,
                                    status              : it.status,
                                    dueDate             : DateUtil.toDateStringWithoutTimezone(it.dueDate),
                                    fileName            : it.attachments.collect { it.inputName != 'attachments' ? it.inputName : it.name }?.join(','),
                                    assignedToFullName  : it.assignedTo ? it.assignedTo.name : (it.assignedToGroup ? it.assignedToGroup.name : "-"),

                            ]
                        }

                        validatedSignal.signalRMMs.findAll {
                            it.communicationType == "communication"
                        }.each {
                            reportDataList.communication << [
                                    signalName      : validatedSignal.name,
                                    type                : it.type,
                                    description         : it.description,
                                    status              : it.status,
                                    dueDate             : DateUtil.toDateStringWithoutTimezone(it.dueDate),
                                    fileName            : it.attachments.collect { it.inputName != 'attachments' ? it.inputName : it.name }?.join(','),
                                    assignedToFullName  : it.assignedTo ? it.assignedTo.name : (it.assignedToGroup ? it.assignedToGroup.name : "-"),
                                    email               : it.emailSent? DateUtil.toDateStringWithoutTimezone(it.emailSent) : "-"
                            ]
                        }

                        validatedSignal.adhocAlerts?.each {
                            def signals = validatedSignalService.getSignalsFromAlertObj(it, Constants.AlertConfigType.AD_HOC_ALERT)
                            reportDataList.adhocCaseAlertListingData << [
                                    'signalName'      : validatedSignal.name,
                                    "signalsAndTopics": signals?.collect { it.name + "(S)" }?.join(","),
                                    "name"            : it.name ?: "",
                                    "productSelection": getNameFieldFromJson(it.productSelection),
                                    "eventSelection"  : getNameFieldFromJson(it.eventSelection),
                                    "detectedBy"      : it.detectedBy,
                                    "initDataSrc"     : it.initialDataSource,
                                    "disposition"     : it.disposition?.displayName
                            ]
                        }

                        Map aggAndEvdasAlertMap = validatedSignalService.combinedAndPrevAggAndEvdasAlertListMap(validatedSignal.id)
                        List combinedList = aggAndEvdasAlertMap.combinedList
                        combinedList?.each {
                            if (it.dataSource != Constants.DataSource.EVDAS.toUpperCase()) {
                                def existingSignals = validatedSignalService.getSignalsFromAlertObj(it, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
                                def ebgmTrend = productEventHistoryService.getProductEventHistoryTrend(it.productName, it.pt)
                                Configuration alertConfiguration = Configuration.get(it.alertConfigId)
                                reportDataList.quantitativeCaseAlertListingData << [
                                        'signalName'        : validatedSignal.name,
                                        "alertName"         : alertConfiguration.name,
                                        "productName"       : it.productName,
                                        "soc"               : (it.soc != Constants.Commons.SMQ_ADVANCEDFILTER_LABEL) ? it.soc : "",
                                        "eventPT"           : it.preferredTerm,
                                        "newCount/cummCount": it.newCount1 + " / " + it.cumCount1,
                                        "newSer/cummSer"    : it.newSeriousCount + " / " + it.cumSeriousCount,
                                        "prr"               : it.prrValue.toString(),
                                        "ror"               : it.rorValue.toString(),
                                        "ebgm"              : it.ebgm.toString(),
                                        "eb05/eb95"         : it.eb05 + " / " + it.eb95,
                                        "trend"             : ebgmTrend,
                                        "dataSource"        : getDataSource(alertConfiguration.selectedDatasource),
                                        "signalNames"       : SignalUtil.joinSignalNames(existingSignals),
                                        "disposition"       : it.disposition
                                ]
                            }
                        }
                        aggAndEvdasAlertMap.evdasAlerts?.each {
                            def existingSignals = validatedSignalService.getSignalsFromAlertObj(it, Constants.AlertConfigType.EVDAS_ALERT)
                            def ebgmTrend = productEventHistoryService.getProductEventHistoryTrend(it.productName, it.pt)
                            reportDataList.quantitativeCaseAlertListingData << [
                                    'signalName'        : validatedSignal.name,
                                    "alertName"         : it.alertName,
                                    "productName"       : it.productName,
                                    "soc"               : it.soc,
                                    "eventPT"           : it.preferredTerm,
                                    "newCount/cummCount": it.newCount1 + " / " + it.cumCount1,
                                    "newSer/cummSer"    : it.newSeriousCount + " / " + it.cumSeriousCount,
                                    "prr"               : '-',
                                    "ror"               : it.rorValue,
                                    "ebgm"              : "-",
                                    "eb05/eb95"         : "- / -",
                                    "trend"             : ebgmTrend,
                                    "dataSource"        : 'EVDAS',
                                    "signalNames"       : SignalUtil.joinSignalNames(existingSignals),
                                    "disposition"       : it.disposition
                            ]
                        }

                        // fetching the single case alert list on the basis of user's blinding status for report
                        reportDataList.qualitativeCaseAlertListingData = validatedSignalService.getSingleAlertListForSignalSummaryReport(validatedSignal.id as String)

                        validatedSignal.literatureAlerts?.each {
                            reportDataList.literatureCaseAlertListingData << [
                                    "signalName" : validatedSignal.name,
                                    "alertName"  : it.name,
                                    "priority"   : it.priority?.displayName,
                                    "disposition": it.disposition?.displayName,
                                    "articleTitle" : it.articleTitle,
                                    "articleAuthors" : it.articleAuthors,
                                    "publicationDate" : it.publicationDate
                        ]
                    }

                        alertCommentService.listSignalComments(validatedSignal).each {
                            reportDataList.commentList << [
                                    'signalName' : validatedSignal.name,
                                    "dateCreated": DateUtil.stringFromDate(it.dateCreated, DateUtil.DEFAULT_DATE_FORMAT, DateTimeZone.UTC.ID),
                                    "comment"    : it.comments
                            ]
                        }

                        validatedSignal.actions?.each {
                            reportDataList.actionsList << [
                                    'signalName'    : validatedSignal.name,
                                    "type"          : it.meetingId ? Meeting.get(it.meetingId)?.meetingTitle : it.type.displayName,
                                    "action"        : it.config?.displayName,
                                    "details"       : it.details,
                                    "assignedTo"    : it.assignedTo ? it.assignedTo.fullName : (it.assignedToGroup ? it.assignedToGroup.name : "-"),
                                    "status"        : ActionStatus[it.actionStatus].id,
                                    "dueDate"       : DateUtil.toDateString(it.dueDate, "UTC"),
                                    "completionDate": it.completedDate ? DateUtil.toDateString(it.completedDate, "UTC") : "-"
                            ]
                        }

                        validatedSignal.meetings?.sort { it.dateCreated }?.each {
                            reportDataList.meetingList.add([
                                    'signalName'    : validatedSignal.name,
                                    "meetingTitle"  : it.meetingTitle,
                                    "meetingMinutes": it.meetingMinutes,
                                    'meetingDate'   : it.meetingDate ? DateUtil.stringFromDate(it.meetingDate, DateUtil.DEFAULT_DATE_FORMAT, timezone) : "-",
                                    'meetingOwner'  : it.meetingOwner.fullName ?: "-",
                                    'meetingAgenda' : it.meetingAgenda ?: "-"
                            ])
                        }

                        String characteristicVal
                        String signalName = validatedSignal.name
                        validatedSignalService.insertSingleAndAggregateCases(validatedSignal).each { characteristic, val ->
                            val.each { value ->
                                value.each { category, number ->
                                    reportDataList.assessmentDetailsList << [
                                            'signalName'     : signalName,
                                            "characteristics": characteristicVal == characteristic ? "" : characteristic,
                                            "category"       : category,
                                            "number"         : number.toString()
                                    ]
                                    characteristicVal = characteristic
                                    signalName = ""
                                }
                            }
                        }

                        Map<String, List<Map<String, String>>> conceptsMap = validatedSignalService.createConceptsMap(validatedSignal)

                        conceptsMap.each { String medicalConcept, Map<String, String> value ->
                            reportDataList.medicalConceptDistributionList << [
                                    'signalName'    : validatedSignal.name,
                                    'medicalConcept': medicalConcept,
                                    'caseCount'     : value['singleCaseAlerts']?: '0',
                                    'argusCount'    : value['aggregateAlerts']?: '0',
                                    'evdasCount'    : value['evdasAlerts']?: '0',
                            ]
                        }


                    }
                }
                def reportingInterval = toDateStringWithoutTimezone(startDate) +
                        " to " + toDateStringWithoutTimezone(endDate)
                def productsName = productSelectionMap['productSelectionNames']

                ReportHistory.withTransaction {
                    ReportHistory reportHistory = ReportHistory.get(reportHistoryId)
                    reportHistory.memoReport = (["reportingInterval": reportingInterval, "productsName": productsName, "reportDataList": reportDataList] as JSON).toString().getBytes(StandardCharsets.UTF_8)
                    reportHistory.isReportGenerated = true
                }
                log.info("Signal summary report data filtering complete.")
            } catch (Exception e) {
                e.printStackTrace()
                updateReportHistory(reportHistoryId)
            }
        }
    }

    List<ValidatedSignal> fetchMatchingSignals(Date startDate, Date endDate, Map productSelectionMap, Boolean pbrerReport = false) {
        List<Long> excludedDispositionsId = Holders.config.pvsignal.pbrerReport.signalInclusionsAndExclusions.excludedDispositions ? Disposition.findAllByValueInList(Holders.config.pvsignal.pbrerReport.signalInclusionsAndExclusions.excludedDispositions as List)*.id: []
        ValidatedSignal.withTransaction {
            ValidatedSignal.list().findAll { signal ->
                if (!signal.products && !signal.productGroupSelection) {
                    return false
                }
                if(!signal.detectedDate) {
                    return false
                }

                //ingredientOrderId, familyOrderId and productOrderId are added to handle dynamic nature of PV Dictionary.
                String ingredientOrderId
                String familyOrderId
                String productOrderId
                String substanceOrderId
                String productDosageOrderId
                String tradeOrderId
                DictionaryMapping.withTransaction {
                    String ingredient = Holders.config.dictionary.ingredient.column.name
                    String family = Holders.config.dictionary.family.column.name
                    String Substance = Holders.config.dictionary.substance.column.name
                    String productDosage = Holders.config.dictionary.dosage.forms.column.name
                    String trade = Holders.config.dictionary.trade.column.name
                    String productName = Holders.config.dictionary.product.column.name

                    productDosageOrderId = productDosage ? DictionaryMapping.findByLabel(productDosage)?.getId() : ""
                    substanceOrderId = Substance ? DictionaryMapping.findByLabel(Substance)?.getId() : ""
                    tradeOrderId = trade ? DictionaryMapping.findByLabel(trade)?.getId() : ""
                    ingredientOrderId = ingredient ? DictionaryMapping.findByLabel(ingredient)?.getId() : ""
                    familyOrderId = family ? DictionaryMapping.findByLabel(family)?.getId() : ""
                    productOrderId = productName ? DictionaryMapping.findByLabel(productName)?.getId() : ""
                }


                Map prod =[:]
                if(signal.products){
                    prod = JSON.parse(signal.products)
                }
                if(signal.productGroupSelection){
                    prod.productGroupSelectionIds = getIdsForProductGroup(signal.productGroupSelection).split(',')
                }
                if (pbrerReport) {
                    (
                            (prod[ingredientOrderId]?.findAll {
                                productSelectionMap['productIngredientIds']*.toString()?.contains(it.id)
                            } || prod[familyOrderId]?.findAll {
                                productSelectionMap['productFamilyIds']*.toString()?.contains(it.id)
                            } || prod[substanceOrderId]?.findAll {
                                productSelectionMap['productSubstanceIds']*.toString()?.contains(it.id)
                            } || prod[productDosageOrderId]?.findAll {
                                productSelectionMap['productDosageIds']*.toString()?.contains(it.id)
                            }  || prod[tradeOrderId]?.findAll {
                                productSelectionMap['productTradeIds']*.toString()?.contains(it.id)
                            } || prod[productOrderId]?.findAll {
                                productSelectionMap['productSelectionIds']*.toString()?.contains(it.id)
                            } || prod['productGroupSelectionIds']?.findAll {
                                (productSelectionMap['productGroupSelectionIds'])?.split(',')?.contains(it)
                            })
                                    &&
                                    !(signal.disposition.id in excludedDispositionsId)

                                    &&
                                    (
                                            (signal.detectedDate >= startDate && signal.detectedDate <= endDate)

                                                    ||
                                                    signal.signalStatusHistories.find {
                                                        it.signalStatus in Holders.config.pvsignal.pbrerReport.signalInclusionsAndExclusions.signalHistoryStatus &&
                                                                it.dateCreated >= startDate && it.dateCreated <= endDate
                                                    }

                                                    ||
                                                    (
                                                            signal.detectedDate < startDate && (signal.signalStatusHistories.find {
                                                                it.signalStatus == Constants.DATE_CLOSED
                                                            }?.dateCreated ?: new Date()) >= new Date(endDate.getYear(), endDate.getMonth(), endDate.getDate())
                                                    )
                                    )
                    )
                } else {
                    signal.detectedDate >= startDate && signal.detectedDate <= endDate && (
                            prod[ingredientOrderId]?.findAll {
                                productSelectionMap['productIngredientIds']*.toString()?.contains(it.id)
                            } || prod[familyOrderId]?.findAll {
                                productSelectionMap['productFamilyIds']*.toString()?.contains(it.id)
                            } || prod[substanceOrderId]?.findAll {
                                productSelectionMap['productSubstanceIds']*.toString()?.contains(it.id)
                            } || prod[productDosageOrderId]?.findAll {
                                productSelectionMap['productDosageIds']*.toString()?.contains(it.id)
                            } || prod[tradeOrderId]?.findAll {
                                productSelectionMap['productTradeIds']*.toString()?.contains(it.id)
                            } || prod[productOrderId]?.findAll {
                                productSelectionMap['productSelectionIds']*.toString()?.contains(it.id)
                            } || prod['productGroupSelectionIds']?.findAll {
                                (productSelectionMap['productGroupSelectionIds'])?.split(',')?.contains(it)
                            })
                }

            }?.sort { it.detectedDate }
        }
    }

    /**
     * Retrieves the report product name from the JSON string if it is valid JSON,
     * otherwise returns the original product name.
     *
     * @param reportHistory the ReportHistory object containing the product name and data source
     * @return the processed product name based on JSON validity
     */
    String getReportProductNameFromJsonString(ReportHistory reportHistory) {
        String productName = reportHistory?.productName
        String dataSource = reportHistory?.dataSource.toString()


        if (isValidJson(productName)) {
            productName = prepareProductMap(productName, dataSource)
        }
        return productName
    }

    /**
     * Generates and downloads a memo report based on the provided ReportHistory, output format, and criteria sheet list.
     *
     * @param reportHistory the ReportHistory object containing memo report data
     * @param outputFormat the format in which the report should be generated
     * @param criteriaSheetList the list of criteria sheets for the report
     * @return the generated report file
     */
    File downloadMemoReport(ReportHistory reportHistory, String outputFormat, List criteriaSheetList) {
        File reportFile = null
        if (reportHistory.memoReport) {
            ByteArrayInputStream byteArrayInputStream
            ObjectInputStream objectInputStream
            try {
                log.info("Starting downloadMemoReport for ReportHistory id: ${reportHistory?.id}")
                byteArrayInputStream = new ByteArrayInputStream(reportHistory.memoReport)
                objectInputStream = new ObjectInputStream(byteArrayInputStream)
                Map memoReportMap = (LinkedHashMap) objectInputStream.readObject()
                Map params = [:]
                List singleCaseAlerts = memoReportMap.singleCaseAlerts ?: []
                List aggregateCaseAlerts = memoReportMap.aggregateCaseAlerts ?: []
                Date startDate = reportHistory.startDate
                Date endDate = reportHistory.endDate

                JRDataSource singleCaseAlertsSummaryList = new JRMapCollectionDataSource(singleCaseAlerts)
                JRDataSource aggregateCaseAlertsSummaryList = new JRMapCollectionDataSource(aggregateCaseAlerts)

                String reportDateRange = (startDate ? DateUtil.toDateString1(startDate.clearTime()) : "-") + " to " + (endDate ? DateUtil.toDateString1(endDate.clearTime()) : "-")
                String productName = null
                if (reportHistory) {
                    productName = processReportHistory(reportHistory)
                }
                Map signalData = [productName: productName?.toString()?.replaceAll('"', ''), dateRange: reportDateRange]
                params.showCompanyLogo = true
                params.showLogo = true
                params.outputFormat = outputFormat
                params.criteriaSheetList = criteriaSheetList

                log.info("Creating product memo report with signalData: $signalData and params: $params")
                reportFile = dynamicReportService.createProductMemoReport(singleCaseAlertsSummaryList, aggregateCaseAlertsSummaryList, signalData, params)
            } catch (IndexOutOfBoundsException e) {
                log.error("IndexOutOfBoundsException occurred: ${e.message}", e)
                e.printStackTrace()
            } catch (Exception exp) {
                log.error("Exception occurred: ${exp.message}", exp)
                exp.printStackTrace()
            } catch (Throwable t) {
                log.error("Throwable occurred: ${t.message}", t)
                t.printStackTrace()
            } finally {
                objectInputStream?.close()
                byteArrayInputStream?.close()
            }
        }
        log.info("Finished downloadMemoReport for ReportHistory id: ${reportHistory?.id}")
        reportFile
    }

    File downloadPBRERReport(ReportHistory reportHistory, String outputFormat, List criteriaSheetList) {
        Map pbrerReportData = JSON.parse(new String(reportHistory.memoReport, StandardCharsets.UTF_8)) as Map
        Map params = [:]
        File reportFile
        if (reportHistory) {
            params.products = processReportHistory(reportHistory)
        }
        params.reportingInterval = pbrerReportData.reportingInterval
        params.generatedOn = DateUtil.stringFromDate(reportHistory.dateCreated, Constants.DateFormat.STANDARD_DATE_WITH_TIME, userService.user.preference.timeZone) + userService?.getGmtOffset(userService.user.preference.timeZone)
        params.criteriaSheetList = criteriaSheetList
        try {
            params.outputFormat = outputFormat
            reportFile = dynamicReportService.createPeberSignalReport(new JRMapCollectionDataSource(pbrerReportData.reportDataList), params, true)
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace()
        } catch (Exception exp) {
            exp.printStackTrace()
        }
        reportFile
    }

    File downloadSignalStateReport(ReportHistory reportHistory, String outputFormat, List criteriaSheetList) {
        Map signalStateReportData = JSON.parse(new String(reportHistory.memoReport, StandardCharsets.UTF_8)) as Map
        Map params = [:]
        File reportFile
        if (reportHistory) {
            params.products = processReportHistory(reportHistory)
        }
        params.reportingInterval = signalStateReportData.reportingInterval
        params.criteriaSheetList = criteriaSheetList

        try {
            params.outputFormat = outputFormat
            for(Object report :signalStateReportData.reportDataList){
                report.put("monitoringStatus",report.get("disposition"))
            }
            reportFile = dynamicReportService.createSignalStateReport(new JRMapCollectionDataSource(signalStateReportData.reportDataList), params)
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace()
        } catch (Exception exp) {
            exp.printStackTrace()
        }
        reportFile
    }

    File downloadProductActionReport(ReportHistory reportHistory, String outputFormat, List criteriaSheetList) {
        Map signalStateReportData = JSON.parse(new String(reportHistory.memoReport, StandardCharsets.UTF_8)) as Map
        Map params = [:]
        File reportFile
        if (reportHistory) {
            params.products = processReportHistory(reportHistory)
        }
        params.reportingInterval = signalStateReportData.reportingInterval
        params.criteriaSheetList = criteriaSheetList
        try {
            params.outputFormat = outputFormat
            params.showProductAndInterval = true
            if (params.outputFormat == ReportFormat.XLSX.name()){
                params.signalActionReport = true
            }
            reportFile = dynamicReportService.createSignalActionDetailReport(new JRMapCollectionDataSource(signalStateReportData.reportDataList), params)
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace()
        } catch (Exception exp) {
            exp.printStackTrace()
        }
        reportFile
    }

    /**
     * Processes the report history to extract the product name.
     *
     * @param reportHistory The report history object containing the product group information.
     * @return The product name as a string. If the product group is present, it returns the name from the audit log config utility.
     *         If the product group is not present, it parses the product name from a JSON string.
     *         If parsing fails, it logs a warning and returns the original JSON string.
     */
    String processReportHistory(reportHistory) {
        String productName = ""
        try {
            if (reportHistory.productGroup) {
                productName = AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(reportHistory.productGroup)
            } else {
                String jsonString = getReportProductNameFromJsonString(reportHistory)
                def json = JSON.parse(jsonString)

                if (json) {
                    productName = json.collect { key, value ->
                        "${key}: ${value?.join(', ')}"
                    }.join('\n').replaceAll('"', '')
                } else {
                    productName = jsonString
                    log.warn("Parsed JSON is null or empty for reportHistory: ${reportHistory}")
                }
            }
        } catch (Exception e) {
            log.error("Error processing report history: ${reportHistory}", e)
        }
        return productName
    }

    File downloadSignalSummaryReport(ReportHistory reportHistory, String outputFormat) {
        Map signalStateReportData = JSON.parse(new String(reportHistory.memoReport, StandardCharsets.UTF_8)) as Map
        Map params = [:]
        Map<String,List> signalSummary = [:]
        File reportFile
        if (reportHistory) {
            params.products = processReportHistory(reportHistory)
        }
        params.reportingInterval = signalStateReportData.reportingInterval
        params.signalSummaryReport = true
        Map reportDataList = signalStateReportData.reportDataList
        Map summaryReportPreference = params.assessmentRequired ? JSON.parse('{"ignore":[],"required":["Signal Information", "WorkFlow Log", "Safety Observations","References","Actions","Meetings","RMMs","Communication" "Appendix"]}') as Map :
                                                                  JSON.parse('{"ignore":[],"required":["Signal Information", "WorkFlow Log", "Safety Observations","References","Actions","Meetings","RMMs","Communication"]}') as Map
        List signalSummaryData = reportDataList.signalSummaryData
        signalSummary.put("signalSummaryData",reportDataList.signalSummaryData)
        signalSummary.put("signalDetails",reportDataList.signalDetails)
        signalSummary.put("workflowLog",reportDataList.workflowLog)
        signalSummary.put("references",reportDataList.references)
        signalSummary.put("rmmType",reportDataList.rmmType)
        signalSummary.put("communication",reportDataList.communication)
        List adhocCaseAlertListingData = reportDataList.adhocCaseAlertListingData
        List quantitativeCaseAlertListingData = reportDataList.quantitativeCaseAlertListingData
        List qualitativeCaseAlertListingData = reportDataList.qualitativeCaseAlertListingData
        List literatureCaseAlertListingData = reportDataList.literatureCaseAlertListingData
        List commentList = reportDataList.commentList
        List actionsList = reportDataList.actionsList
        List meetingList = reportDataList.meetingList
        List assessmentDetailsList = reportDataList.assessmentDetailsList
        List medicalConceptDistributionList = reportDataList.medicalConceptDistributionList
        try {
            params.outputFormat = outputFormat
            reportFile = dynamicReportService.createSignalSummaryReport(quantitativeCaseAlertListingData, qualitativeCaseAlertListingData,
                    adhocCaseAlertListingData, literatureCaseAlertListingData, actionsList, meetingList, signalSummaryData, signalSummary , params, summaryReportPreference,
                    assessmentDetailsList, medicalConceptDistributionList, true)
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace()
        } catch (Exception exp) {
            exp.printStackTrace()
        }
        reportFile
    }

    Map updateProductIngredientMappings(Map productSelectionMap) {
        ProductIngredientMapping.list().findAll {
            JSON.parse(it.productSelection)["1"].collect { it.id }.each {
                return productSelectionMap.productIngredientIds.contains(it)
            }
        }.each {
            Map pvaProductSelection = JSON.parse(it.pvaProductSelection)

            productSelectionMap.productSelectionIds += pvaProductSelection.collect { k, v ->
                if (k != "1") {
                    v*.id
                }
            }.flatten().unique() - null
            productSelectionMap.productIngredientIds += pvaProductSelection["1"].collect { it.id }
            productSelectionMap.productFamilyIds += pvaProductSelection["2"].collect { it.id }
        }
        productSelectionMap
    }

    Map generateICSRReport(List dateRange, List productList, List substanceList, String dataSource, List socList, Boolean isMultiIngredient = false) {
        Map result = [:]
        List chartsData = []
        ExecutorService executorService
        try {
            executorService = Executors.newFixedThreadPool(10)
            List<Callable> callables = new LinkedList<>()
            addIndividualCases(callables, dateRange, productList, substanceList, dataSource,isMultiIngredient)
            addIndividualCasesByReactionGRP(callables, dateRange, productList, substanceList, dataSource,isMultiIngredient)
            addReactionGroup(callables, dateRange, productList, substanceList, socList, dataSource,isMultiIngredient)
            List<Future> futureList = executorService.invokeAll(callables)
            futureList.each {
                chartsData << it.get()
            }
            result.put('individualCasesChart', prepareDataForIndividualCasesChart(chartsData))
            result.put('individualCasesByReactionGroup', prepareDataForIndividualCasesByReactionGroupChart(chartsData))
            result.put('reactionGroup', prepareDataForReactionGroupChart(chartsData))
        } catch (Exception exception) {
            log.error(exception.printStackTrace())
        } finally {
            executorService?.shutdown()
        }
        result
    }

    private void addReactionGroup(LinkedList<Callable> callables, List dateRange, List productList, List substanceList, List socList, String dataSource, Boolean isMultiIngredient = false) {
        callables.add({ ->
            getReactionAgeAndGenderGroupChart(dateRange, productList, substanceList, socList, 4, dataSource, isMultiIngredient)
        })

        callables.add({ ->
            getReactionReporterGroupChart(dateRange, productList, substanceList, socList, 4, dataSource,isMultiIngredient)
        })

        callables.add({ ->
            getReactionRegionGroupChart(dateRange, productList, substanceList, socList, 4, dataSource,isMultiIngredient)
        })
    }

    private void addIndividualCasesByReactionGRP(LinkedList<Callable> callables, List dateRange, List productList, List substanceList, String dataSource, Boolean isMultiIngredient = false) {
        callables.add({ ->
            getIndividualCaseReactionGroupRegionChart(dateRange, productList, substanceList, 6, dataSource,isMultiIngredient)
        })

        callables.add({ ->
            getIndividualCaseReactionGroupReporterChart(dateRange, productList, substanceList, 6, dataSource,isMultiIngredient)
        })

        callables.add({ ->
            getIndividualCaseReactionGroupAgeChart(dateRange, productList, substanceList, 6, dataSource,isMultiIngredient)
        })

        callables.add({ ->
            getIndividualCaseReactionGroupGenderChart(dateRange, productList, substanceList, 6, dataSource,isMultiIngredient)
        })
    }

    private void addIndividualCases(LinkedList<Callable> callables, List dateRange, List productList, List substanceList, String dataSource, Boolean isMultiIngredient = false) {
        callables.add({ ->
            getIndividualCasesByAgeData(dateRange, productList, substanceList, 1, dataSource,isMultiIngredient)
        })

        callables.add({ ->
            getIndividualCasesByRegionData(dateRange, productList, substanceList, 2, dataSource, isMultiIngredient)

        })

        callables.add({ ->
            getIndividualCasesByRegionData(dateRange, productList, substanceList, 3, dataSource, isMultiIngredient)
        })
    }

    Map prepareDataForIndividualCasesChart(List chartsData) {
        Map result = [:]
        result.put('individualCasesByAgeChart', chartsData[0])
        result.put('individualCasesByGenderChart', chartsData[1])
        result.put('individualCasesByRegionChart', chartsData[2])
        result

    }

    Map prepareDataForIndividualCasesByReactionGroupChart(List chartsData) {
        Map result = [:]
        result.put("reactionGroupByRegionChart", chartsData[3])
        result.put("reactionGroupByReporterChart", chartsData[4])
        result.put("reactionGroupByAgeChart", chartsData[5])
        result.put("reactionGroupByGenderChart", chartsData[6])
        result
    }


    Map prepareDataForReactionGroupChart(List chartsData) {
        Map result = [:]
        result.put('ageAndGenderGroupChart', chartsData[7])
        result.put('reporterGroupChart', chartsData[8])
        result.put('geographicRegionChart', chartsData[9])
        result
    }

    void generateMemoReport(Map memoReportMap) {
        log.info("Fetching the configurations")
        List scaList = []
        List agaList = []
        ByteArrayOutputStream byteArrayOutputStream
        ObjectOutputStream outputStream
        try {
            String productSelectionIds = getProductSelectionIds(memoReportMap.productSelectionMap)
            String productSelectionNames = getProductSelectionName(memoReportMap.productSelectionMap)
            Map reviewCompletedMap = prepareReviewCompltedMap()
            prepareSingleCaseAlertListForMemoReport(memoReportMap, scaList, productSelectionIds?:null, getProductGroupSelectionIds(memoReportMap.productSelectionMap.productGroupSelectionIds), SingleCaseAlert,productSelectionNames,getProductFamilyIds(memoReportMap.productSelectionMap.productFamilyIds), reviewCompletedMap)
            log.info("prepareSingleCaseAlertListForMemoReport executed for singleCasealert")
            prepareSingleCaseAlertListForMemoReport(memoReportMap, scaList, productSelectionIds?:null, getProductGroupSelectionIds(memoReportMap.productSelectionMap.productGroupSelectionIds), ArchivedSingleCaseAlert,productSelectionNames,getProductFamilyIds(memoReportMap.productSelectionMap.productFamilyIds),reviewCompletedMap)
            prepareAggCaseAlertListForMemoReport(memoReportMap, agaList, productSelectionIds?:null, getProductGroupSelectionIds(memoReportMap.productSelectionMap.productGroupSelectionIds), AggregateCaseAlert, productSelectionNames)
            prepareAggCaseAlertListForMemoReport(memoReportMap, agaList, productSelectionIds?:null, getProductGroupSelectionIds(memoReportMap.productSelectionMap.productGroupSelectionIds),
            ArchivedAggregateCaseAlert, productSelectionNames)
            Map data = ["singleCaseAlerts": scaList, "aggregateCaseAlerts": agaList]

            // Using stream as toString() method was not working for 50 lakhs of rows in list
            byteArrayOutputStream = new ByteArrayOutputStream()
            outputStream = new ObjectOutputStream(byteArrayOutputStream)
            outputStream.writeObject(data)
            ReportHistory reportHistoryObj = ReportHistory.get(memoReportMap.reportHistory.id)
            reportHistoryObj.memoReport = byteArrayOutputStream.toByteArray()
            reportHistoryObj.isReportGenerated = true
            reportHistoryObj.save(flush: true)
        } catch (Exception e) {
            e.printStackTrace()
            log.error("Exception occurred while processing generateMemo report.", e)
            updateReportHistory(memoReportMap.reportHistory.id)

        } finally {
            outputStream?.close()
            byteArrayOutputStream?.close()
        }
    }

    private String getProductSelectionIds(Map productSelectionMap) {
        List<String> productSelectionIdList = []
        productSelectionMap.findAll { it.value.size() && it.key != 'productSelectionNames'&& it.key != 'productGroupSelectionIds' }.values().each {
            it.each {
                productSelectionIdList.add("'$it'")
            }
        }
        productSelectionIdList.join(",")
    }
    private String getProductSelectionName(Map productSelectionMap) {
        String productSelectionName = productSelectionMap.productSelectionNames
        List productSelectionNameList = productSelectionName.split(",")
        List finalList = []
        productSelectionNameList?.each{
            finalList.add("'$it'")
        }
        finalList.join(",")
    }
    private String getProductGroupSelectionIds(String productGroupSelectionIds) {
        List<String> productSelectionIdList = []
        if (productGroupSelectionIds){
            productGroupSelectionIds.split(",").each {
                productSelectionIdList.add("'$it'")
            }
            return productSelectionIdList.join(",")
        }
        return null
    }
    private String getProductFamilyIds(def productFamilyIds) {
        List<String> productFamilyIdList = []
        if (productFamilyIds){
            productFamilyIds.each {
                productFamilyIdList.add("'$it'")
            }
            return productFamilyIdList.join(",")
        }
        return null
    }
    private String getProductFamilyIds(String productFamilyIds) {
        List<String> productFamilyIdList = []
        if (productFamilyIds){
            productFamilyIds.split(",").each {
                productFamilyIdList.add("'$it'")
            }
            return productFamilyIdList.join(",")
        }
        return null
    }

    private void prepareSingleCaseAlertListForMemoReport(Map memoReport, List scaList, String productSelectionIds, String productGroupSelectionIds, def domain, def productSelectionNames, String productFamilyIds, Map reviewCompletedMap = [:]) {
        int offset = 0
        int batchSize = 1000

        List<Map> singleCaseAlertList
        domain == SingleCaseAlert ? log.info("Now Fetching the SingleCaseAlerts") : log.info("Now Fetching the ArchivedSingleCaseAlerts")
        List<Long> execConfigIdList = []
        List<Long> reviewedDispositions = cacheService.getDispositionByReviewCompleted()*.id
        Sql sql = new Sql(dataSource)
        sql.eachRow(SignalQueryHelper.memo_reports_sql(productSelectionIds?:productGroupSelectionIds, Constants.AlertConfigType.SINGLE_CASE_ALERT),[]){ row ->
            execConfigIdList.add(row[0] as Long)
        }
        log.info("After generated execConfigIdList....")
        Closure memoReportCriteria = {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                getDomainPropertyMap(domain).each { key, value ->
                    property(key, value)
                }
            }
            'or' {
                execConfigIdList.collate(999).each {
                    'in'('executedAlertConfiguration.id', it)
                }
            }
            'or' {
                if (productFamilyIds) {
                    sqlRestriction("PRODUCT_FAMILY in (${productSelectionNames})")
                }
                if(productSelectionIds && !(memoReport.isIngredient)){
                    sqlRestriction("product_id in (${productSelectionIds})")
                }
            }
            'ge'("periodStartDate", memoReport.reportHistory.startDate)
            'le'("periodEndDate", memoReport.reportHistory.endDate)
        }
        log.info("After closure preparation....")
        if(execConfigIdList.size()) {
            User currentUser = userService.getUser()
            Map blindingFieldsForGroup = userService.getBlindingFieldsForGroup(currentUser.groups*.id)
            Map blindingFieldsForUser = userService.getBlindingFieldsForUser(blindingFieldsForGroup, true)
            boolean isAllFieldsRedacted = blindingFieldsForGroup.isAllFieldsRedacted == 1
            boolean isUserMappingAvailable = blindingFieldsForGroup.isUserMappingAvailable
            isAllFieldsRedacted = !(!isUserMappingAvailable && !isAllFieldsRedacted)
            List<String> blindedFields = []
            List<String> fieldsAvailable = []
            List<String> redactedFields = []
            Map<String, String> conversionMap = Holders.config.icrFields.rptMap
            alertService.categorizeFields(conversionMap, blindingFieldsForUser, blindedFields, fieldsAvailable, redactedFields, isAllFieldsRedacted, isUserMappingAvailable)
            while (singleCaseAlertList = domain.createCriteria().list([max: batchSize, offset: offset],
                    memoReportCriteria)) {
                List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(singleCaseAlertList.collect {
                    it.id
                }, domain)
                List<String> caseNumberAndConfigIdList = singleCaseAlertList.collect {
                    "('" + it.caseNumber + "','" + it.alertConfigurationId + "')"
                }

                List<Map> caseHistoryMap = []
                String sql_statement = SignalQueryHelper.case_history_change(caseNumberAndConfigIdList.join(","))
                Session session = sessionFactory.currentSession
                SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
                sqlQuery.list().each { row ->
                    caseHistoryMap.add([caseNumber: row[0].toString(), configId: row[1], justification: row[2].toString(), change: row[3].toString()])
                }
                scaList.addAll(singleCaseAlertList.collect { Map alert ->

                    String dispositionJustification = caseHistoryMap.find {
                        it.caseNumber == alert.caseNumber && it.configId == alert.alertConfigurationId && it.change == Constants.HistoryType.DISPOSITION
                    }?.justification

                    String priorityJustification = caseHistoryMap.find {
                        it.caseNumber == alert.caseNumber && it.configId == alert.alertConfigurationId && it.change == Constants.HistoryType.PRIORITY
                    }?.justification

                    [
                            "caseNumber"           : redactedFields.contains(Constants.AdvancedFilter.CASE_NUMBER) ? Constants.BlindingStatus.REDACTED : (blindedFields.contains(Constants.AdvancedFilter.CASE_NUMBER) && alert.studyBlindingStatus ? Constants.BlindingStatus.BLINDED : getExportCaseNumber(alert.caseNumber, alert.followUpNumber)),
                            "product"              : redactedFields.contains(Constants.ICRFields.PRODUCT_NAME) ? Constants.BlindingStatus.REDACTED : (blindedFields.contains(Constants.ICRFields.PRODUCT_NAME) && alert.studyBlindingStatus ? Constants.BlindingStatus.BLINDED : alert.productName),
                            "event"                : redactedFields.contains(Constants.ICRFields.PT) ? Constants.BlindingStatus.REDACTED : (blindedFields.contains(Constants.ICRFields.PT) && alert.studyBlindingStatus ? Constants.BlindingStatus.BLINDED : alert.pt),
                            "disposition"          : cacheService.getDispositionByValue(alert.dispositionId)?.displayName,
                            "justification"        : dispositionJustification && dispositionJustification != "null" ?
                                    dispositionJustification : Constants.Commons.DASH_STRING,
                            "priority"             : cacheService.getPriorityByValue(alert.priorityId)?.displayName,
                            "priorityJustification": priorityJustification ?: Constants.Commons.DASH_STRING,
                            "lastUpdateTimestamp"  : DateUtil.stringFromDate(alert.lastUpdated, Constants.DateFormat.STANDARD_DATE_WITH_TIME, DateTimeZone.UTC.ID),
                            "due/overdue"          : calculateDueOverdue(domain, alert, reviewCompletedMap),
                            "signalName"           : alertValidatedSignalList.findAll {
                                it.id == alert.id
                            }?.name?.join(', ') ?: "-"

                    ]
                })
                offset = offset + singleCaseAlertList.size()
            }
            log.info("Finished execConfigIdList condition process")
        }

        domain == SingleCaseAlert ? log.info("Fetched ${scaList ? scaList.size() : 0} SingleCaseAlerts") : log.info("Fetched ${scaList ? scaList.size() : 0} ArchivedSingleCaseAlerts")
        if (!scaList) {
            scaList = [[
                               "caseNumber"           : Constants.Commons.DASH_STRING,
                               "product"              : Constants.Commons.DASH_STRING,
                               "event"                : Constants.Commons.DASH_STRING,
                               "justification"        : Constants.Commons.DASH_STRING,
                               "priority"             : Constants.Commons.DASH_STRING,
                               "priorityJustification": Constants.Commons.DASH_STRING,
                               "disposition"          : Constants.Commons.DASH_STRING,
                               "lastUpdateTimestamp"  : Constants.Commons.DASH_STRING,
                               "due/overdue"          : Constants.Commons.DASH_STRING,
                               "signalName"           : Constants.Commons.DASH_STRING
                       ]]
        }

    }

    private void prepareAggCaseAlertListForMemoReport(Map memoReport, List agaList, String productSelectionIds, String productGroupSelectionIds, def domain, def productSelectionNames) {
        int offset = 0
        int batchSize = 1000
        List<Map> aggCaseAlertList
        List<Long> execConfigIdList = []
        List<Long> reviewedDispositions = cacheService.getDispositionByReviewCompleted()*.id
        Sql sql = new Sql(dataSource)
        sql.eachRow(SignalQueryHelper.memo_reports_sql(productSelectionIds?:productGroupSelectionIds, Constants.AlertConfigType.AGGREGATE_CASE_ALERT),[]){ row ->
            execConfigIdList.add(row[0] as Long)
        }
        Closure memoReportCriteria = {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                getDomainPropertyMap(domain).each { key, value ->
                    property(key, value)
                }
            }
            'or' {
                execConfigIdList.collate(999).each {
                    'in'('executedAlertConfiguration.id', it)
                }
            }
            'or' {
                if(productSelectionNames){
                    sqlRestriction("PRODUCT_NAME in (${productSelectionNames})")
                }
                if(productSelectionIds && !(memoReport.isIngredient)){
                    sqlRestriction("product_id in (${productSelectionIds})")
                }
            }
            'ge'("periodStartDate", memoReport.reportHistory.startDate)
            'le'("periodEndDate", memoReport.reportHistory.endDate)
        }
        domain == AggregateCaseAlert ? log.info("Now Fetching the Aggregate Case Alerts") : log.info("Now Fetching the Archived Aggregate Case Alerts")
        if(execConfigIdList.size()) {
            while (aggCaseAlertList = domain.createCriteria().list([max: batchSize, offset: offset],
                    memoReportCriteria)) {
                List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(aggCaseAlertList.collect {
                    it.id
                }, domain)

                List<Map> peHistoryMap = []
                List<String> productNamePtConfigIds = aggCaseAlertList.collect {
                    "('" + it.productName + "','" + it.pt.replaceAll("'", "''") + "','" + it.alertConfigurationId + "')"
                }
                String sql_statement = SignalQueryHelper.product_event_history_change(productNamePtConfigIds.join(","))
                Session session = sessionFactory.currentSession
                SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
                sqlQuery.list().each { row ->
                    peHistoryMap.add([productName: row[0].toString(), pt: row[1], configId: row[2], justification: row[3].toString(), change: row[5].toString()])
                }

                agaList.addAll(aggCaseAlertList.collect { Map alert ->

                    String dispositionJustification = peHistoryMap.find {
                        it.productName == alert.productName && it.pt == alert.pt && it.configId == alert.alertConfigurationId && it.change == Constants.HistoryType.DISPOSITION
                    }?.justification

                    String priorityJustification = peHistoryMap.find {
                        it.productName == alert.productName && it.pt == alert.pt && it.configId == alert.alertConfigurationId && it.change == Constants.HistoryType.PRIORITY
                    }?.justification

                    [
                            "product"              : alert.productName,
                            "event"                : alert.pt,
                            "justification"        :  dispositionJustification && dispositionJustification != "null" ?
                                                        dispositionJustification: Constants.Commons.DASH_STRING,
                            "disposition"          : cacheService.getDispositionByValue(alert.dispositionId)?.displayName,
                            "priority"             : cacheService.getPriorityByValue(alert.priorityId)?.displayName,
                            "priorityJustification": priorityJustification ?: Constants.Commons.DASH_STRING,
                            "lastUpdateTimestamp"  : DateUtil.stringFromDate(alert.lastUpdated, Constants.DateFormat.STANDARD_DATE_WITH_TIME, DateTimeZone.UTC.ID),
                            "due/overdue"          : !alert.dueDate ? Constants.Commons.BLANK_STRING : (DateTime.now().withTimeAtStartOfDay() <= new DateTime(alert.dueDate).withTimeAtStartOfDay() ? "Due" : "Overdue"),
                            "signalName"           : alertValidatedSignalList.findAll {
                                it.id == alert.id
                            }?.name?.join(', ') ?: "-"

                    ]
                })
                offset = offset + aggCaseAlertList.size()
            }
        }
        domain == AggregateCaseAlert ? log.info("Fetched ${agaList ? agaList.size() : 0} AggregateCaseAlerts") : log.info("Fetched ${agaList ? agaList.size() : 0} ArchivedAggregateCaseAlerts")

        if (!agaList) {
            agaList = [[
                               "product"              : Constants.Commons.DASH_STRING,
                               "event"                : Constants.Commons.DASH_STRING,
                               "justification"        : Constants.Commons.DASH_STRING,
                               "priority"             : Constants.Commons.DASH_STRING,
                               "priorityJustification": Constants.Commons.DASH_STRING,
                               "disposition"          : Constants.Commons.DASH_STRING,
                               "lastUpdateTimestamp"  : Constants.Commons.DASH_STRING,
                               "due/overdue"          : Constants.Commons.DASH_STRING,
                               "signalName"           : Constants.Commons.DASH_STRING
                       ]]
        }
        sql?.close()
    }
    private Map<String, String> getDomainPropertyMap(def domain) {
        Map domainPropertyMap = ["id"         : "id", "pt": "pt", "priority.id": "priorityId", "disposition.id": "dispositionId",
                                 "lastUpdated": "lastUpdated", "dueDate": "dueDate", "productName": "productName","alertConfiguration.id":"alertConfigurationId"]
        if (domain == SingleCaseAlert || domain == ArchivedSingleCaseAlert) {
            domainPropertyMap << [
                    "caseNumber": "caseNumber",
                    "followUpNumber": "followUpNumber",
                    "studyBlindingStatus": "studyBlindingStatus"
            ]
        }
        domainPropertyMap
    }

     Map getActualProductName(String datasource, String productSelection) {
         List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
         List<Map> productDetails = MiscUtil?.getProductDictionaryValues(productSelection, true)
         String productNameColumn = Holders.config.dictionary.product.name.column
         String otherNameColumn = Holders.config.dictionary.other.name.column
         String idColumn = Holders.config.dictionary.id.column
         Map actualProductNameMap = [:]
         Sql sql = null
         try {
             if (datasource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_PVA) || datasource.equalsIgnoreCase(Constants.DataSource.PVA)) {
                 sql = new Sql(dataSource_pva)
             } else if (datasource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_FAERS)) {
                 sql = new Sql(dataSource_faers)
             } else {
                 sql = new Sql(dataSource_eudra)
             }
             productDetails.eachWithIndex { Map entry, int i ->
                 int keyId = productViewsList.get(i).keyId
                 entry.each { k, v ->
                     sql.eachRow("Select $idColumn, $productNameColumn, $otherNameColumn from DISP_VIEW_${keyId} where $idColumn=${k} and $otherNameColumn='${v?.replaceAll("(?i)'", "''")}'", [])
                             { GroovyResultSet resultSet ->
                                 resultSet.getString(productNameColumn) ? actualProductNameMap.put(resultSet.getLong(idColumn) as String, resultSet.getString(productNameColumn)) : actualProductNameMap.put(resultSet.getLong(idColumn) as String, resultSet.getString(otherNameColumn))
                             }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace()
         } finally {
             sql?.close()
         }
         actualProductNameMap
     }

         void updateProductSelectionMap(Map productSelectionMap, Map actualProductNameMap) {
             List productNames = []
             if(productSelectionMap.containsKey("productSelectionNames")) {
                 productSelectionMap.remove("productSelectionNames")
             }
             productSelectionMap.each { k, v ->
                 if(v) {
                     v.each { it ->
                         productNames.add(actualProductNameMap.get(it))
                     }
                 }
             }
             productSelectionMap.put("productSelectionNames", productNames?.unique()?.join(","))
         }

    Map prepareReviewCompltedMap() {
        List reviewCompletedCases = AlertReviewCompleted.createCriteria().list() {
            projections {
                property("caseNumber", "caseNumber")
                property("configId", "configId")
            }
        }
        Map reviewCompletedCasesMap = [:]
        reviewCompletedCases?.each {
            reviewCompletedCasesMap[it[0]] = it[1]
        }
        reviewCompletedCasesMap
    }

    String calculateDueOverdue(def domain, def alert, Map reviewCompletedMap) {
        String dueOverdue = ''
        if (domain == ArchivedSingleCaseAlert && (reviewCompletedMap.get(alert.caseNumber) == alert.alertConfigurationId)) {
            dueOverdue = Constants.Commons.BLANK_STRING
        } else {
            dueOverdue = !alert.dueDate ? Constants.Commons.BLANK_STRING : (DateTime.now().withTimeAtStartOfDay() <= new DateTime(alert.dueDate).withTimeAtStartOfDay() ? "Due" : "Overdue")
        }
        dueOverdue
    }

    void updateReportHistory(Long reportHistoryId) {
        ReportHistory reportHistory = null
        if (null != reportHistoryId) {
            reportHistory = ReportHistory.get(reportHistoryId)
            if (Objects.nonNull(reportHistory)) {
                reportHistory.isReportGenerated = null
                reportHistory.save(flush: true)
            }
        }
    }

    /**
     * Checks if the provided string is a valid JSON.
     *
     * @param jsonString the string to be validated as JSON
     * @return true if the string is valid JSON, false otherwise
     */
    boolean isValidJson(String jsonString) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        try {
            jsonSlurper.parseText(jsonString)
            return true
        } catch (Exception e) {
            return false
        }
    }

    /**
     * Prepares a map of products from a JSON string and data source dictionary.
     *
     * @param jsonString the JSON string representing product data
     * @param dataSourceDict the data source dictionary string
     * @return a map of product types to product names
     */
    Map prepareProductMap(String jsonString, String dataSourceDict) {

        String dataSource = dataSourceDict?.split(';')?.getAt(0)
        Map dataSourceMap = getDataSourceMap()
        dataSource = dataSourceMap.get(dataSource)
        String prdName = ""
        def jsonObj = null
        Map productMap = [:]
        Map productTypeMap


        if (dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)) {
            productTypeMap = Holders.config.pvsignal.pvcm.dictionary.list
        } else {
            productTypeMap = Holders.config.custom.caseInfoMap.Enabled ? Holders.config.custom.dictionary.list : Holders.config.pvsignal.pva.dictionary.list
        }

        if (jsonString) {
            jsonObj = parseJsonString(jsonString)

            if (!jsonObj) {
                prdName = jsonString
            } else {
                def prdVal = jsonObj.findAll()
                prdVal.each { heirarchy ->
                    List productName = []
                    heirarchy.value.each {
                        if (dataSource) {
                            productName += it.name ? it.name + ' (' + dataSource + ')' : it.genericName + ' (' + dataSource + ')'
                        } else {
                            productName += it.name ? it.name : it.genericName
                        }
                    }
                    if (productName) {
                        productMap.put(productTypeMap.get(heirarchy.key), productName)
                    }
                }
            }
        }

        return productMap
    }


}
