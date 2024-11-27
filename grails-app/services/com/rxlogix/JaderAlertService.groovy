package com.rxlogix

import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.audit.AuditTrail
import com.rxlogix.cache.HazelcastService
import com.rxlogix.config.*
import com.rxlogix.dto.*
import com.rxlogix.dto.reports.integration.ExecutedConfigurationSharedWithDTO
import com.rxlogix.enums.*
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.LmProduct
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.Synchronized
import groovy.util.slurpersupport.GPathResult
import oracle.jdbc.OracleTypes
import com.rxlogix.util.AlertAsyncUtil
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpStatus
import org.grails.datastore.mapping.query.Query
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.jdbc.Work
import org.hibernate.sql.JoinType
import org.hibernate.transform.Transformers
import org.hibernate.type.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Propagation

import java.lang.reflect.Field
import java.sql.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Transactional
class JaderAlertService implements Alertbililty ,AlertAsyncUtil {

    def grailsApplication
    def aggregateCaseAlertService
    def pvsAlertTagService
    def pvsGlobalTagService
    def validatedSignalService
    def alertCommentService
    def messageSource
    def dataSource
    def dataSource_jader
    def alertFieldService
    def userService
    def cacheService
    def signalExecutorService
    def dataObjectService
    def undoableDispositionService
    def signalDataSourceService

    List<Future> prepareJaderAlertExportData(List agaList,Boolean isExcelExport,Integer previousExecutionsToConsider,ExecutorService executorService){
        List<Future> futureList = agaList.collect { it ->
            executorService.submit({ ->
                it.name = it?.alertName
                it.pt = it.preferredTerm
                it.soc = it.soc + ""
                it.dueDate = it.dueIn + ""
                it.newCountJader =           isExcelExport ? "" + it.newCountJader        : "    " + it.newCountJader + "\n    " + it.cumCountJader
                it.newSeriousCountJader =    isExcelExport ? "" + it.newSeriousCountJader : "    " + it.newSeriousCountJader + "\n    " + it.cumSeriousCountJader
                it.newPediatricCountJader =  isExcelExport ? "" + it.newPediatricCountJader  : "    " + it.newPediatricCountJader + "\n    " + it.cumPediatricCountJader
                it.newGeriatricCountJader =  isExcelExport ? "" + it.newGeriatricCountJader  : "    " + it.newGeriatricCountJader + "\n    " + it.cumGeriatricCountJader
                it.newFatalCountJader =      isExcelExport ? "" + it.newFatalCountJader      : "    " + it.newFatalCountJader + "\n    " + it.cumFatalCountJader
                it.prrLCIJader =              isExcelExport ? "" + it.prrLCIJader              : "    " + it.prrLCIJader + "\n    " + it.prrUCIJader
                it.prrValueJader =           "" + it.prrValueJader
                it.rorValueJader =           "" + it.rorValueJader
                it.rorLCIJader =              isExcelExport ? "" + it.rorLCIJader              : "    " + it.rorLCIJader + "\n    " + it.rorUCIJader
                it.eb05Jader =               isExcelExport ? "" + it.eb05Jader               : "    " + it.eb05Jader + "" + "\n    " + it.eb95Jader + ""
                it.ebgmJader =               "" + it.ebgmJader
                it.chiSquareJader =          "" + it.chiSquareJader
                it.aValueJader = (it.aValueJader as String != '-' && it.aValueJader != -1) ? ("" + (it.aValueJader as Integer)) : it.aValueJader != -1 ? it.aValueJader : '-'
                it.bValueJader = (it.bValueJader as String != '-' && it.bValueJader != -1) ? ("" + (it.bValueJader as Integer)) : it.bValueJader != -1 ? it.bValueJader : '-'
                it.cValueJader = (it.cValueJader as String != '-' && it.cValueJader != -1) ? ("" + (it.cValueJader as Integer)) : it.cValueJader != -1 ? it.cValueJader : '-'
                it.dValueJader = (it.dValueJader as String != '-' && it.dValueJader != -1) ? ("" + (it.dValueJader as Integer)) : it.dValueJader != -1 ? it.dValueJader : '-'
                it.eValueJader = "" + it.eValueJader
                it.rrValueJader = "" + it.rrValueJader

                if(isExcelExport) {
                    it.cumCountJader = "" + it.cumCountJader
                    it.cumSeriousCountJader ="" + it.cumSeriousCountJader
                    it.cumPediatricCountJader = "" + it.cumPediatricCountJader
                    it.cumGeriatricCountJader = "" + it.cumGeriatricCountJader
                    it.cumFatalCountJader = "" + it.cumFatalCountJader
                    it.prrUCIJader = "" + it.prrUCIJader
                    it.rorUCIJader = "" + it.rorUCIJader
                    it.eb95Jader =  "" + it.eb95Jader

                }
                String signalTopics     = ""
                signalTopics            = it.signalsAndTopics.collect { it.name }?.join(",")
                it.signalsAndTopics     = signalTopics
                it.assignedTo           = userService.getAssignedToName(it)
                List <String> tagsList = []
                it.alertTags.each{tag->
                    String tagString = ""
                    if(tag.subTagText == null) {
                        tagString = tagString + tag.tagText + tag.privateUser + tag.tagType
                    }
                    else{
                        String subTags = tag.subTagText.split(";").join("(S);")
                        tagString = tagString + tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + "(S)"
                    }
                    tagsList.add(tagString)

                }
                it.alertTags = tagsList.join(", ")
                it.currentDisposition = it.disposition
                (0..previousExecutionsToConsider).each { exeNum ->
                    def exeName = 'exe' + exeNum
                    it.put(exeName + 'newCountJader',        isExcelExport ? "" + it[exeName]?.newCountJader   : "    " + it[exeName]?.newCountJader   + "\n    " + it[exeName]?.cumCountJader)
                    it.put(exeName + 'newSeriousCountJader',        isExcelExport ? "" + it[exeName]?.newSeriousCountJader   : "    " + it[exeName]?.newSeriousCountJader   + "\n    " + it[exeName]?.cumSeriousCountJader)
                    it.put(exeName + 'eb05Jader',        isExcelExport ? "" + it[exeName]?.eb05Jader   : "    " + it[exeName]?.eb05Jader   + "\n    " + it[exeName]?.eb95Jader)
                    it.put(exeName + 'prrLCIJader',        isExcelExport ? "" + it[exeName]?.prrLCIJader   : "    " + it[exeName]?.prrLCIJader   + "\n    " + it[exeName]?.prrUCIJader)
                    it.put(exeName + 'newPediatricCountJader',        isExcelExport ? "" + it[exeName]?.newPediatricCountJader   : "    " + it[exeName]?.newPediatricCountJader   + "\n    " + it[exeName]?.cumPediatricCountJader)
                    it.put(exeName + 'newFatalCountJader',        isExcelExport ? "" + it[exeName]?.newFatalCountJader   : "    " + it[exeName]?.newFatalCountJader   + "\n    " + it[exeName]?.cumFatalCountJader)
                    it.put(exeName + 'newGeriatricCountJader',  isExcelExport ? "" + it[exeName]?.newGeriatricCountJader :  "    " + it[exeName]?.newGeriatricCountJader + "\n    " + it[exeName]?.cumGeriatricCountJader)
                    it.put(exeName + 'rorLCIJader',        isExcelExport ? "" + it[exeName]?.rorLCIJader   : "    " + it[exeName]?.rorLCIJader   + "\n    " + it[exeName]?.rorUCIJader)
                    it.put(exeName + 'ebgmJader',                "" + it[exeName]?.ebgmJader)
                    it.put(exeName + 'prrValueJader',                "" + it[exeName]?.prrValueJader)
                    it.put(exeName + 'rorValueJader',                "" + it[exeName]?.rorValueJader)
                    it.put(exeName + 'chiSquareJader',                "" + it[exeName]?.chiSquareJader)

                    if (isExcelExport) {
                        it.put(exeName + 'cumCountJader',  "" + it[exeName]?.cumCountJader)
                        it.put(exeName + 'cumSeriousCountJader',  "" + it[exeName]?.cumSeriousCountJader)
                        it.put(exeName + 'eb95Jader',  "" + it[exeName]?.eb95Jader)
                        it.put(exeName + 'prrUCIJader',  "" + it[exeName]?.prrUCIJader)
                        it.put(exeName + 'cumPediatricCountJader',  "" + it[exeName]?.cumPediatricCountJader)
                        it.put(exeName + 'cumFatalCountJader',  "" + it[exeName]?.cumFatalCountJader)
                        it.put(exeName + 'cumGeriatricCountJader', "" + it[exeName]?.cumGeriatricCountJader)
                        it.put(exeName + 'rorUCIJader',  "" + it[exeName]?.rorUCIJader)


                    }
                }
                //currentDispositionList.add(it?.currentDisposition)

            } as Runnable)
        }
        return futureList
    }

    List fetchResultAlertListJader(List agaList, AlertDataDTO alertDataDTO, String callingScreen = null ) {
        Map params = alertDataDTO.params
        Integer prevColCount = Holders.config.signal.quantitative.number.prev.columns
        List list = []
        Boolean showSpecialPE = Boolean.parseBoolean(params.specialPE)
        List<ExecutedConfiguration> prevExecs = []
        List<Map> prevAggCaseAlertMap = []
        Disposition defaultQuantDisposition = userService.getUser().workflowGroup.defaultQuantDisposition
        cacheService.setDefaultDisp(Constants.AlertType.AGGREGATE_NEW, defaultQuantDisposition.id as Long)
        list = fetchValuesForAggregatedReport(agaList, showSpecialPE, alertDataDTO.domainName, callingScreen, alertDataDTO.isFromExport, params.boolean('isArchived'))
        Boolean isPrevExecutions = !alertDataDTO.cumulative && !(params.adhocRun.toBoolean()) && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS
        if (isPrevExecutions) {
            prevExecs = aggregateCaseAlertService.fetchPrevPeriodExecConfig(alertDataDTO.executedConfiguration.configId as Long, alertDataDTO.execConfigId as Long)
            if (prevExecs.size() > 0) {
                List prevExecutionsId = prevExecs.collect {
                    it.id
                }
                List<Integer> ptCodeList = agaList.collect {
                    it.ptCode
                }

                List<Integer> productIdList = agaList.collect {
                    it.productId
                }
                prevAggCaseAlertMap = ArchivedAggregateCaseAlert.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property("id", "id")
                        property("executedAlertConfiguration.id", "executedAlertConfigurationId")
                        property("productId", "productId")
                        property("ptCode", "ptCode")
                        property("jaderColumns", "jaderColumns")

                    }
                    'in'("executedAlertConfiguration.id", prevExecutionsId)
                    'or' {
                        ptCodeList.collate(1000).each {
                            'in'('ptCode', it)
                        }
                    }
                    'or' {
                        productIdList.collate(1000).each {
                            'in'('productId', it)
                        }
                    }
                } as List<Map>
            }
        }
      ExecutorService executorService = Executors.newFixedThreadPool(6)
        agaList.each { def aga ->
            //If the flow is not cummulative and not the adhoc run then we need to show the previous executions as well.
            executorService.submit({
                if (isPrevExecutions) {
                    (0..<prevColCount).each { index ->
                        def exeName = "exe" + (index)
                        ExecutedConfiguration pec = prevExecs[index]
                        Map countMap = [:]
                        if (pec) {
                            Map prevAlert = prevAggCaseAlertMap.find {
                                it.executedAlertConfigurationId == pec.id && it.productId == aga.productId && it.ptCode == aga.ptCode
                            }
                            Map prevJaderCols
                            if(prevAlert?.jaderColumns){
                                prevJaderCols=JSON.parse(prevAlert.jaderColumns)
                            }
                            countMap = [
                                    newCountJader           : (prevJaderCols?.newCountJader!= null) ? prevJaderCols.newCountJader: Constants.Commons.UNDEFINED_NUM,
                                    cumCountJader          : (prevJaderCols?.cumCountJader!= null) ? prevJaderCols.cumCountJader: Constants.Commons.UNDEFINED_NUM,
                                    newPediatricCountJader          : (prevJaderCols?.newPediatricCountJader!= null) ? prevJaderCols.newPediatricCountJader: Constants.Commons.UNDEFINED_NUM,
                                    newGeriatricCountJader          : (prevJaderCols?.newGeriatricCountJader!= null) ? prevJaderCols.newGeriatricCountJader: Constants.Commons.UNDEFINED_NUM,
                                    cumPediatricCountJader          : (prevJaderCols?.cumPediatricCountJader!= null) ? prevJaderCols.cumPediatricCountJader: Constants.Commons.UNDEFINED_NUM,
                                    cumGeriatricCountJader          : (prevJaderCols?.cumGeriatricCountJader!= null) ? prevJaderCols.cumGeriatricCountJader: Constants.Commons.UNDEFINED_NUM,
                                    newSeriousCountJader          : (prevJaderCols?.newSeriousCountJader!= null) ? prevJaderCols.newSeriousCountJader: Constants.Commons.UNDEFINED_NUM,
                                    cumSeriousCountJader          : (prevJaderCols?.cumSeriousCountJader!= null) ? prevJaderCols.cumSeriousCountJader: Constants.Commons.UNDEFINED_NUM,
                                    newFatalCountJader          : (prevJaderCols?.newFatalCountJader!= null) ? prevJaderCols.newFatalCountJader: Constants.Commons.UNDEFINED_NUM,
                                    cumFatalCountJader          : (prevJaderCols?.cumFatalCountJader!= null) ? prevJaderCols.cumFatalCountJader: Constants.Commons.UNDEFINED_NUM,
                                    chiSquareJader          : (prevJaderCols?.chiSquareJader!= null) ? prevJaderCols.chiSquareJader: Constants.Commons.UNDEFINED_NUM,
                                    eb05Jader          : (prevJaderCols?.eb05Jader!= null) ? prevJaderCols.eb05Jader: Constants.Commons.UNDEFINED_NUM,
                                    eb95Jader          : (prevJaderCols?.eb95Jader!= null) ? prevJaderCols.eb95Jader: Constants.Commons.UNDEFINED_NUM,
                                    prrLCIJader          : (prevJaderCols?.prrLCIJader!= null) ? prevJaderCols.prrLCIJader: Constants.Commons.UNDEFINED_NUM,
                                    prrUCIJader          : (prevJaderCols?.prrUCIJader!= null) ? prevJaderCols.prrUCIJader: Constants.Commons.UNDEFINED_NUM,
                                    rorLCIJader          : (prevJaderCols?.rorLCIJader!= null) ? prevJaderCols.rorLCIJader: Constants.Commons.UNDEFINED_NUM,
                                    rorUCIJader          : (prevJaderCols?.rorUCIJader!= null) ? prevJaderCols.rorUCIJader: Constants.Commons.UNDEFINED_NUM,
                                    prrValueJader          : (prevJaderCols?.prrValueJader!= null) ? prevJaderCols.prrValueJader: Constants.Commons.UNDEFINED_NUM,
                                    rorValueJader          : (prevJaderCols?.rorValueJader!= null) ? prevJaderCols.rorValueJader: Constants.Commons.UNDEFINED_NUM,
                                    ebgmJader          : (prevJaderCols?.ebgmJader!= null) ? prevJaderCols.ebgmJader: Constants.Commons.UNDEFINED_NUM,
                            ]
                        } else {

                            countMap = [
                                    newCountJader           : Constants.Commons.DASH_STRING,
                                    cumCountJader           : Constants.Commons.DASH_STRING,
                                    newPediatricCountJader  : Constants.Commons.DASH_STRING,
                                    newGeriatricCountJader  : Constants.Commons.DASH_STRING,
                                    cumPediatricCountJader : Constants.Commons.DASH_STRING,
                                    cumGeriatricCountJader  : Constants.Commons.DASH_STRING,
                                    newSeriousCountJader    : Constants.Commons.DASH_STRING,
                                    cumSeriousCountJader    : Constants.Commons.DASH_STRING,
                                    newFatalCountJader      : Constants.Commons.DASH_STRING,
                                    cumFatalCountJader      : Constants.Commons.DASH_STRING,
                                    chiSquareJader          : Constants.Commons.DASH_STRING,
                                    eb05Jader               : Constants.Commons.DASH_STRING,
                                    eb95Jader               : Constants.Commons.DASH_STRING,
                                    prrLCIJader              : Constants.Commons.DASH_STRING,
                                    prrUCIJader              : Constants.Commons.DASH_STRING,
                                    rorLCIJader              : Constants.Commons.DASH_STRING,
                                    rorUCIJader              : Constants.Commons.DASH_STRING,
                                    prrValueJader           : Constants.Commons.DASH_STRING,
                                    rorValueJader           : Constants.Commons.DASH_STRING,
                                    ebgmJader               : Constants.Commons.DASH_STRING
                            ]
                        }

                        list.find { it.id == aga.id }[exeName] = countMap
                    }
                }
            })

        }
        cacheService.removeDefaultDisp(Constants.AlertType.AGGREGATE_NEW)
        list
    }
    Map getJaderLatestQuarter() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String jaderDate
        Sql jaderSql
        Map dateRange = [:]
        try {
                jaderSql = new Sql(dataSource_jader)
                String jader_statement = SignalQueryHelper.jader_date_range()
                jaderSql.eachRow(jader_statement, []) { resultSetObj ->
                    jaderDate = resultSetObj
                }
                int length = jaderDate.size()
                String year = jaderDate.substring(length - 5, length - 1)
                if (jaderDate.contains('MAR')) {
                    jaderDate = '31-Mar-' + year
                } else if (jaderDate.contains('JUN')) {
                    jaderDate = '30-Jun-' + year
                } else if (jaderDate.contains('SEP')) {
                    jaderDate = '30-Sep-' + year
                } else if (jaderDate.contains('DEC')) {
                    jaderDate = '31-Dec-' + year
                }
                dateRange.jaderDate = jaderDate
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
                jaderSql?.close()
        }
        return dateRange
    }
}