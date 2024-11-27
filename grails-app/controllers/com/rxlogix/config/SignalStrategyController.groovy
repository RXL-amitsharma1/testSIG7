package com.rxlogix.config

import com.rxlogix.util.DateUtil
import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.CREATED
import com.rxlogix.Constants
import com.rxlogix.signal.AdHocAlert
import static com.rxlogix.util.DateUtil.toDateTimeString

@Secured(["isAuthenticated()"])
class SignalStrategyController {

    def signalStrategyService
    def CRUDService
    def userService

    def index() {}

    def list() {
        def timeZone = userService.getUser().preference.timeZone
        def strategies = SignalStrategy.list().collect {
            it.toDto(timeZone)
        }
        respond strategies, [formats:['json']]
    }

    def create() {
        def signalStrategy = new SignalStrategy()
        signalStrategy.properties = params
        [signalStrategy: signalStrategy]
    }

    def edit() {
        def signalStrategy = SignalStrategy.findById(params.id)
        [signalStrategy: signalStrategy]
    }

    def save() {

        def signalStrategy = new SignalStrategy()
        try {
            bindData(signalStrategy, params, ['startDate', 'medicalConcepts'])
            signalStrategy.startDate = DateUtil.stringToDate(
                    params.startDate, "MM/dd/yyyy",
                    grailsApplication.config.server.timezone)

            bindConcepts(signalStrategy)

            CRUDService.save(signalStrategy)
        } catch(Exception ex) {
            ex.printStackTrace()
            render view: "create", model: [signalStrategy: signalStrategy]
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message',
                        args: ['Signal Strategy', signalStrategy.name])
                redirect(action: "index")
            }
            '*' { respond signalStrategy, [status: CREATED] }
        }
    }


    def update() {
        def signalStrategyId = params.id
        if (signalStrategyId) {
            def signalStrategy = SignalStrategy.get(Long.parseLong(signalStrategyId))
            try {
                bindData(signalStrategy, params, ['startDate'])
                signalStrategy.startDate = DateUtil.stringToDate(
                        params.startDate, "MM/dd/yyyy",
                        grailsApplication.config.server.timezone)

                bindConcepts(signalStrategy)

                CRUDService.save(signalStrategy)
            } catch(Exception ex) {
                ex.printStackTrace()
                render view: "edit", model: [signalStrategy: signalStrategy]
                return
            }
            request.withFormat {
                form {
                    flash.message = message(code: 'default.created.message',
                            args: ['Signal Strategy', signalStrategy.name])
                    redirect(action: "index")
                }
                '*' { respond signalStrategy, [status: CREATED] }
            }
        } else {
            render view: "index"
        }
    }

    private SignalStrategy bindConcepts(SignalStrategy signalStrategy) {

        if (params['medicalConcepts']) {
            if (signalStrategy?.medicalConcepts) {
                signalStrategy?.medicalConcepts.each {
                    MedicalConcepts str = MedicalConcepts.findById(it.id)
                    signalStrategy.removeFromMedicalConcepts(str)
                }
            }
            if (params.medicalConcepts.getClass() == String) {
                params.medicalConcepts = [params.medicalConcepts]
            }
            def pvConcepts = params.medicalConcepts.toList() as Set

            pvConcepts.each {
                def conceptId = Long.parseLong(it)
                MedicalConcepts pvConcept = MedicalConcepts.findById(it)
                if (pvConcept) {
                    signalStrategy.addToMedicalConcepts(pvConcept)
                }
            }
        }
        return signalStrategy
    }

    def getAlertConfigurationData() {
        def strategyId = Long.parseLong(params.id)
        def alertType = params.alertType
        def strategy = SignalStrategy.get(strategyId)
        def alerts = []
        if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
            alerts = AdHocAlert.findAllByStrategy(strategy)?.collect {
                [
                    "name" : it.name,
                    "description" : it.description,
                    "product" : it.getProductNameList(),
                    "event" : it.getEventSelectionList(),
                    "formulation" : it.formulations,
                    "indication" : it.indication,
                    "reportType" : it.reportType,
                    "numberOfICSRs" : it.numberOfICSRs,
                    "initialDataSource" : it.initialDataSource,
                    "priority" : it.priority.displayName,
                    "dateCreated" : toDateTimeString(it.dateCreated),
                ]
            }
        } else {
            alerts = Configuration.findAllByStrategyAndType(strategy, alertType)?.collect {
                [
                    "name" : it.name,
                    "description" : it.description,
                    "product" : it.getProductNameList(),
                    "event" : it.getEventSelectionList(),
                    "dateCreated" : toDateTimeString(it.dateCreated),
                    "priority" : it.priority.displayName
                ]
            }
        }
        respond alerts, [formats:['json']]
    }

}
