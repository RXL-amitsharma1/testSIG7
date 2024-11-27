package com.rxlogix

import com.rxlogix.config.ActionTemplate
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.MedicalConcepts
import com.rxlogix.config.Priority
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

@Secured(['isAuthenticated()'])
class WorkflowController {

    def userService
    def workflowService

    def validatedSignalService
    def safetyLeadSecurityService
    def cacheService

    @Secured(['ROLE_CONFIGURATION_CRUD','ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def workflowState() {
        respond workflowService.workflowStates(), [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD','ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def priorities() {
        respond workflowService.priorities(), [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD','ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def getPriority() {

        def appName = params.get('appName')
        def user = userService.getUser()
        def checkedIdList = params.checkedIdList
        JsonSlurper jsonSlurper = new JsonSlurper()
        def checkedIdListStr = jsonSlurper.parseText(checkedIdList)
        def alert, priorityValue = null
        def selectedDatasource = Constants.DataSource.PVA
        checkedIdListStr.each { id ->

            if (Constants.AlertConfigType.SINGLE_CASE_ALERT == appName) {
                alert = SingleCaseAlert.findById(id)
            } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == appName) {
                alert = AggregateCaseAlert.findById(id)
                selectedDatasource = alert.executedAlertConfiguration.selectedDatasource
            } else if (Constants.AlertConfigType.EVDAS_ALERT == appName) {
                alert = EvdasAlert.findById(id)
            } else {
                alert = Alert.findById(id)
            }

            if (Constants.AlertConfigType.EVDAS_ALERT != appName &&
                    selectedDatasource != Constants.DataSource.FAERS) {
                if (!safetyLeadSecurityService.isUserSafetyLead(user, alert)) {
                    render status: HttpStatus.FORBIDDEN
                }
            }
        }

        def justificationObjList = Justification.list()
        def justificationList = []
        justificationObjList.each {
            if (it.getAttr("alertPriority") == "on") {
                justificationList.add(it)
            }
        }

        if(Constants.AlertConfigType.SINGLE_CASE_ALERT == appName) {
            priorityValue = alert.priority.displayName
        } else {
            priorityValue = alert.priority.value
        }
        def map = [currentValue: priorityValue, availableValues: (workflowService.priorities()), justification: justificationList]
        render(map as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD','ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER','ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def getWorkflowState() {

        def appName = params.get('appName')
        def id = params.get('id')


        def alert = null
        def eventList = []
        def productList = []
        def justificationObjList = Justification.list()
        def justificationList = []
        if (Constants.AlertConfigType.SINGLE_CASE_ALERT == appName) {
            alert = SingleCaseAlert.findById(id)
            productList.add([productName: alert?.productName])
            def eventListObjs = alert.getAttr(cacheService.getRptFieldIndexCache('masterPrefTermAll'))
            if (eventListObjs) {
                def eventArray = eventListObjs.split(',')
                def evList = []
                eventArray.each {
                    if (it) {
                        evList.add([eventName: it.trim()?.substring(2)])
                    }
                }
                eventList.add(evList)
            }

            justificationObjList.each {
                if (it.getAttr("alertWorkflow") == "on") {
                    justificationList.add(it)
                }
            }
        } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == appName) {
            alert = AggregateCaseAlert.findById(id)
            productList.add([productName: alert.productName])
            eventList.add([eventName: alert.pt])

            justificationObjList.each {
                if (it.getAttr("alertWorkflow") == "on") {
                    justificationList.add(it)
                }
            }
        } else if (Constants.AlertConfigType.EVDAS_ALERT == appName) {
            alert = EvdasAlert.findById(id)
            productList.add([productName: alert.substance])
            eventList.add([eventName: alert.pt])

            justificationObjList.each {
                if (it.getAttr("alertWorkflow") == "on") {
                    justificationList.add(it)
                }
            }
        } else if (Constants.AlertConfigType.SIGNAL_MANAGEMENT == appName) {
            alert = ValidatedSignal.findById(id)

            justificationObjList.each {
                if(it.getAttr("signalWorkflow") == "on") {
                    justificationList.add(it)
                }
            }
        } else if (Constants.AlertConfigType.TOPIC == appName) {
            alert = Topic.findById(id)

            justificationObjList.each {
                if(it.getAttr("topicWorkflow") == "on") {
                    justificationList.add(it)
                }
            }
        } else {
            alert = Alert.findById(id)
            productList.add(alert.getNameFieldFromJson(alert.productSelection).tokenize(',').collect { [productName: it] })
            eventList.add(alert.getNameFieldFromJson(alert.eventSelection).tokenize(',').collect { [eventName: it] })
            productList.add(alert.getNameFieldFromJson(alert.productSelection).tokenize(',').collect {
                [productName: it]
            })
            eventList.add(alert.getNameFieldFromJson(alert.eventSelection).tokenize(',').collect {
                [eventName: it]
            })
            justificationObjList.each {
                if (it.getAttr("alertWorkflow") == "on") {
                    justificationList.add(it)
                }
            }
        }

        def workflowState = alert.workflowState

        User currentUser = userService.user
        Set<Group> groups = currentUser.groups

        def availableStates =  null

        //TODO: Correct the below code.
        if (Constants.AlertConfigType.SIGNAL_MANAGEMENT == appName) {
            if (alert.topicCategories?.size() > 0) {
                availableStates = workflowService.getSignalsForSignal(alert, workflowState, groups)
            } else {
                availableStates = workflowService.getAvailableWorkflowStates(workflowState, groups)
            }
        } else {
            availableStates = workflowService.getAvailableWorkflowStates(workflowState, groups)
        }

        def priorities = Priority.list()

        def dispositionsAvailable = false

        if (availableStates?.dispositions?.size != 0) {
            for (def disposition : availableStates.dispositions) {
                if (disposition?.size != 0 && !disposition?.empty) {
                    dispositionsAvailable = true
                    break
                }
            }
        }

        def attachedSignalObjs = validatedSignalService.getSignalsFromAlert(alert, appName)

        def priority = grailsApplication.config.pvsignal.priority.signal.default.value

        List signals = attachedSignalObjs?.allSignals?.flatten()
        List existingSignals = attachedSignalObjs?.selectedSignal?.flatten()

        def map = [
                currentValue         : workflowState?.value,
                availableValues      : availableStates,
                dispositionsAvailable: dispositionsAvailable,
                current_extra_value  : alert?.disposition?.value,
                current_extra_value2 : alert?.priority?.value,
                extra_values2        : priorities,
                signals              : signals - existingSignals,
                products             : productList?.flatten(),
                events               : eventList?.flatten(),
                existingSignals      : existingSignals,
                priorityUrgency      : priority,
                justification        : justificationList,
                medicalConcepts      : MedicalConcepts.list(),
                actionTemplates      : ActionTemplate.list()
        ]
        render(map as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD','ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def getPriorityBatchUpdate() {

        def id = params.get('id')
        def appName = params.get('appName')

        def alert = null
        def priorityValue
        def eventList = []
        def productList = []
        def justificationObjList = Justification.list()
        def justificationList = []
        if (Constants.AlertConfigType.SINGLE_CASE_ALERT == appName) {
            alert = SingleCaseAlert.findById(id)
            priorityValue = alert.priority.displayName
        } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == appName) {
            alert = AggregateCaseAlert.findById(id)
            priorityValue = alert.priority.displayName
        } else {
            alert = Alert.findById(id)
            priorityValue = alert.priority.displayName
        }

        justificationObjList.each {
            if(it.getAttr("alertPriority") == "on") {
                justificationList.add(it)
            }
        }


        User currentUser = userService.user
        Set<Group> groups = currentUser.groups


        if (safetyLeadSecurityService.isUserSafetyLead(currentUser, alert)) {
            def map = [currentValue: priorityValue, availableValues: (workflowService.priorities()), justification: justificationList]
            render(map as JSON)
        } else {
            render status: HttpStatus.FORBIDDEN
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD','ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def getDisposition() {
        def id = params.get('id')
        def alert = Alert.findById(id)
        def map = [currentValue: alert.disposition?.value, availableValues: workflowService.dispositions()]
        render(map as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD','ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def getAssignedTo() {
        def id = params.get('id')
        def alert = Alert.get(id)
        def map = [currentValue: alert.assignedTo?.fullName, availableValues: workflowService.allUsers()]
        render(map as JSON)
    }
}