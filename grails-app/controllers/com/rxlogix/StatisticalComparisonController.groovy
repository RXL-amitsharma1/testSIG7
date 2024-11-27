package com.rxlogix

import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import com.rxlogix.signal.Justification
import com.rxlogix.enums.JustificationFeatureEnum

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK

@Secured(["isAuthenticated()"])
class StatisticalComparisonController implements LinkHelper, AlertUtil {

    def statisticalComparisonService
    def productEventHistoryService
    def actionTemplateService
    def medicalConceptsService
    def validatedSignalService
    def emailService
    def userService
    def activityService
    def workflowRuleService

    def index() {}

    def showComparison(Long configId, String appName) {
        def name
        String timezone = userService.getUser()?.preference?.timeZone
        Map dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        Boolean forceJustification = userService.user.workflowGroup?.forceJustification
        List<Map> availableDispositionJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertWorkflow], false)*.toDto(timezone)
        if (appName == Constants.AlertConfigType.EVDAS_ALERT) {
            name = ExecutedEvdasConfiguration.get(configId)?.name
        } else {
            name = ExecutedConfiguration.get(configId)?.name
        }

        Boolean hasReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.EVDAS_ALERT)

        render(view: "statisticalComparison", model: [executedConfigId                    : configId,
                                                      configName                          : name,
                                                      dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                                                      forceJustification                  : forceJustification,
                                                      availableSignals                    : availableSignals,
                                                      availableDispositionJustifications  : availableDispositionJustifications,
                                                      appName                             : params.appName,
                                                      isArchived                          : params.boolean('isArchived')?:false,
                                                      hasReviewerAccess                   : hasReviewerAccess,
                                                      hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                                                      hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
        ])
    }

    def fetchStatsComparisonData(Long id, String appName) {
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        def compList
        def count
        if (appName == Constants.AlertConfigType.EVDAS_ALERT) {
            compList = statisticalComparisonService.getEvdasStatsComparisonData(id, params, orderColumnMap)
            count = EvdasAlert.countByExecutedAlertConfiguration(ExecutedEvdasConfiguration.load(id))
        } else {
            compList = statisticalComparisonService.getAggStatsComparisonData(id, params, orderColumnMap)
            count = AggregateCaseAlert.countByExecutedAlertConfiguration(ExecutedConfiguration.load(id))

        }
        compList.recordsTotal = count
        compList.recordsFiltered = count
        render(compList as JSON)
    }

    def routeToReviewScreen() {
        def paramsMap = [callingScreen: "review", configId: params.configId]
        if (params.appName == Constants.AlertConfigType.EVDAS_ALERT) {
            redirect(controller: "evdasAlert", action: "details", params: paramsMap)
        } else {
            redirect(controller: "aggregateCaseAlert", action: "details", params: paramsMap)
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def changeWorkflowState(String appName, String newValue, String justification, String extra_value, String alertDetails) {
        try {
            statisticalComparisonService.changeMatchingWorkflowStates(appName, newValue, justification,
                    extra_value, alertDetails, params)
            render(contentType: "application/json", status: OK.value()) {
                [success: 'true', newValue: newValue]
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            render(status: BAD_REQUEST)
        }
    }

    def showComparisionCharts() {
        def sponCount = []
        def seriousCount = []
        def seriousEv = []
        def fatalCount = []
        def evFatalCount = []
        def newEv = []
        def prrValue = []
        def rorValue = []
        def rorEv = []
        def xAxisTitle = []
        def frequency = Constants.Commons.BLANK_STRING
        def evEnabled = grailsApplication.config.signal.evdas.enabled

        def aga
        def evdas
        if (params.appName == Constants.AlertConfigType.EVDAS_ALERT) {
            evdas = EvdasAlert.findById(params.id)
            if (params.statsId != 0 && params.statsId !='undefined') {
                aga = AggregateCaseAlert.findById(params.statsId)
            }
        } else {
            aga = AggregateCaseAlert.findById(params.id)
            if (params.statsId != 0 && params.statsId !='undefined') {
                evdas = EvdasAlert.findById(params.statsId)
            }
        }

        if (aga) {
            def configuration = aga.alertConfiguration
            def productId = aga.productId
            def ptCode = aga.ptCode

            def prevExecs = ExecutedConfiguration.findAllByName(configuration.name, [max: 6, order: "desc"])
            prevExecs = prevExecs.sort {
                it.executedTemplateQueries.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute[0]
            }

            SubstanceFrequency substanceFrequency = SubstanceFrequency.findByNameIlike(aga.productName)
            if (substanceFrequency) {
                frequency = substanceFrequency.uploadFrequency
            } else {
                frequency = getRecurrencePattern(prevExecs[0].scheduleDateJSON)
            }

            prevExecs.eachWithIndex { pec, index ->
                def prevAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndProductIdAndPtCode(pec, productId, ptCode)
                if (prevAlert) {
                    sponCount.add(prevAlert?.newSponCount ?: 0)
                    seriousCount.add(prevAlert?.newSeriousCount ?: 0)
                    fatalCount.add(prevAlert?.newFatalCount ?: 0)
                    prrValue.add(prevAlert?.prrValue ?: 0)
                    rorValue.add(prevAlert?.rorValue ?: 0)
                    if (frequency.equals("HOURLY")) {
                        xAxisTitle.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy HH:mm:ss"))
                    } else{
                        xAxisTitle.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"))
                    }
                    if (params.statsId == 0 || params.statsId =='undefined') {
                        newEv.add(0)
                        evFatalCount.add(0)
                        seriousEv.add(0)
                        rorEv.add(0)
                    }

                }
            }
        }
        if (evdas) {
            def configuration = evdas.alertConfiguration
            def substance = evdas.substance
            def ptCode = evdas.ptCode
            frequency = Constants.Commons.BLANK_STRING

            def prevExecs = ExecutedEvdasConfiguration.findAllByName(configuration.name, [max: 6, order: "desc"])
            prevExecs = prevExecs.sort{
                it.dateRangeInformation?.getReportStartAndEndDate()[0]
            }

            SubstanceFrequency substanceFrequency = SubstanceFrequency.findByNameIlike(evdas.substance)
            if (substanceFrequency) {
                frequency = substanceFrequency.uploadFrequency
            } else {
                frequency = getRecurrencePattern(prevExecs[0].scheduleDateJSON)
            }

            prevExecs.eachWithIndex { pec, index ->
                def prevAlert = EvdasAlert.findByExecutedAlertConfigurationAndSubstanceAndPtCode(pec, substance, ptCode)
                if (prevAlert) {
                    newEv.add(prevAlert?.newEv ?: 0)
                    evFatalCount.add(prevAlert?.newFatal ?: 0)
                    seriousEv.add(prevAlert?.newSerious ?: 0)
                    rorEv.add((prevAlert?.rorValue != null) ? Double.parseDouble(prevAlert?.rorValue) : 0)
                    if (frequency.equals("HOURLY")) {
                        xAxisTitle.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy HH:mm:ss"))
                    } else {
                        xAxisTitle.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"))
                    }
                    if (params.statsId == 0 || params.statsId =='undefined') {
                        sponCount.add(0)
                        seriousCount.add(0)
                        fatalCount.add(0)
                        prrValue.add(0)
                        rorValue.add(0)
                    }

                }
            }
        }
        def result = ["sponCount"   : sponCount, "prrValue": prrValue, "rorValue": rorValue, "newEv": newEv, "rorEv": rorEv, "xAxisTitle": xAxisTitle,
                      "seriousCount": seriousCount, "seriousEv": seriousEv, "fatalCount": fatalCount, "evFatalCount": evFatalCount,
                      "frequency"   : frequency, evEnabled: evEnabled]
        render(result as JSON)
    }
}
