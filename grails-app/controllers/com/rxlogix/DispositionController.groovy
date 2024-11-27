package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.WorkflowRule
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SystemConfig
import com.rxlogix.signal.ValidatedSignal
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK

@Secured(["isAuthenticated()"])
class DispositionController {

    def dispositionService
    def cacheService
    def CRUDService
    UserService userService
    SignalAuditLogService signalAuditLogService
    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        redirect(action: "list", params: params)
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        params.max = Math.min(params.max ? params.max.toInteger() : 100, 100)
        [dispositionInstanceList: Disposition.list(params), dispositionInstanceTotal: Disposition.count()]
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def listDisposition() {
        def data = Disposition.list().collect {
            [
                    "Display Name"       : it.displayName ?: "",
                    "Description"        : it.description ?: "",
                    "Value"              : it.value ?: "",
                    "Display"            : it.display ? "Yes" : "No",
                    "Closed"             : it.closed ? "Yes" : "No",
                    "Validated Confirmed": it.validatedConfirmed ? "Yes" : "No"
            ]
        }


        render contentType: 'application/json',
                text: ([data: data] as JSON).toString()
    }



    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def dispositionInstance = new Disposition()
        dispositionInstance.properties = params
        return [dispositionInstance: dispositionInstance]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")?.replaceAll("\u00A0", "")
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")?.replaceAll("\u00A0", "")
        params.abbreviation = params.abbreviation?.trim()?.replaceAll("\\s{2,}", " ")
        params.colorCode = params.colorCode?.trim()?.replaceAll("\\s{2,}", " ")
        def dispositionInstance = new Disposition(params)
        try {
            if (dispositionService.saveDisposition(dispositionInstance)) {
                cacheService.updateDispositionCache(dispositionInstance)
                flash.message = message(code: "default.created.message", args: ['Disposition', "${dispositionInstance.value}"])
                flash.args = [dispositionInstance.id]
                flash.defaultMessage = "Disposition ${dispositionInstance.id} created"
                redirect(action: "list")
            } else {
                render(view: "create", model: [dispositionInstance: dispositionInstance])
            }
        } catch (Throwable th) {
            log.error(th.getMessage())
            render(view: "create", model: [dispositionInstance: dispositionInstance])
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show() {
        def dispositionInstance = Disposition.get(params.id)
        if (!dispositionInstance) {
            flash.message = message(code: "default.not.found.message", args: ['Disposition', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "Disposition not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [dispositionInstance: dispositionInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def edit() {
        def dispositionInstance = Disposition.get(params.id)
        if (!dispositionInstance) {
            flash.message = message(code: "default.not.found.message", args: ['Disposition', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "Disposition not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [dispositionInstance: dispositionInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")?.replaceAll("\u00A0", "")
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")?.replaceAll("\u00A0", "")
        params.abbreviation = params.abbreviation?.trim()?.replaceAll("\\s{2,}", " ")
        params.colorCode = params.colorCode?.trim()?.replaceAll("\\s{2,}", " ")
        def dispositionInstance = Disposition.get(params.id)
        if (!dispositionInstance) {
            flash.message = message(code: "default.not.found.message", args: ['Disposition', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "Disposition not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
        boolean isClosedBeforeUpdate = dispositionInstance.reviewCompleted
        dispositionService.updateDateClosedBasedOnDisposition(dispositionInstance, params)

        if (params.version && dispositionInstance.version > params.version.toLong()) {
            def optimisticLockingMessage = message(code: "app.disposition.optimistic.locking.error")
            dispositionInstance.errors.rejectValue("version", "disposition.optimistic.locking.failure", optimisticLockingMessage)
            render(view: "edit", model: [dispositionInstance: dispositionInstance])
            return
        }
        if (!params.display) {
            List workflowRules = DispositionRule.withCriteria {
                projections {
                    count()
                }
                and {
                    eq('isDeleted', false)
                    or {
                        'incomingDisposition' {
                            eq('id', dispositionInstance.id)
                        }
                        'targetDisposition' {
                            eq('id', dispositionInstance.id)
                        }
                    }
                }
            }
            if (workflowRules[0] > 0) {
                flash.error = message(code: "app.label.alert.disposition.display")
                flash.args = [params.id]
                render(view: "edit", model: [dispositionInstance: dispositionInstance])
                return
            }
        }
        dispositionInstance.properties = params
        try {
            if (dispositionService.saveDisposition(dispositionInstance)) {
                cacheService.updateDispositionCache(dispositionInstance)
                if (!isClosedBeforeUpdate && dispositionInstance.reviewCompleted) {
                    dispositionService.removeDispCountsInBackground(dispositionInstance.id)
                } else if (isClosedBeforeUpdate && !dispositionInstance.reviewCompleted) {
                    dispositionService.addDispCountsToDashboardCounts(dispositionInstance.id)
                }
                flash.message = message(code: "default.updated.message", args: ['Disposition', "${dispositionInstance.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "Disposition ${params.id} updated"
                redirect(action: "list")
            } else {
                render(view: "edit", model: [dispositionInstance: dispositionInstance])
            }
        } catch (Throwable th) {
            log.error(th.getMessage())
            render(view: "edit", model: [dispositionInstance: dispositionInstance])
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete() {
        def dispositionInstance = Disposition.get(params.id)
        if (dispositionInstance) {
            Boolean isDeleted = true
            String auditEntityName = "Name-${dispositionInstance.displayName}"
            try {
                dispositionInstance.delete(flush:true)
                cacheService.deleteDispositionCache(dispositionInstance)
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                isDeleted = false
                flash.error = message(code: "default.not.deleted.message", args: ['Disposition'])
                flash.args = [params.id]
                flash.defaultMessage = "Disposition ${params.id} could not be deleted"
                redirect(action: "list")
            }
            if(isDeleted){
                signalAuditLogService.createAuditLog([
                        entityName: "Disposition",
                        moduleName: "Disposition",
                        category: AuditTrail.Category.DELETE.toString(),
                        entityValue: auditEntityName,
                        username: userService.getUser().username,
                        fullname: userService.getUser().fullName
                ] as Map, [[propertyName: "isDeleted", oldValue: "false", newValue: "true"]] as List)
                flash.message = message(code: "default.deleted.message", args: ['Disposition', "${dispositionInstance.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "Disposition ${params.id} deleted"
                redirect(action: "list")
            }
        } else {
            flash.message = message(code: "default.not.found.message", args: ['Disposition', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "Disposition not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    def checkForAlerts() {
        def disposition = Disposition.findById(params.id)

        def singleAlerts = SingleCaseAlert.findByDisposition(disposition)
        def aggAlerts = AggregateCaseAlert.findByDisposition(disposition)
        def adhocAlert = AdHocAlert.findByDisposition(disposition)

        if (singleAlerts || aggAlerts || adhocAlert) {
            render(status: OK)
        } else {
            render(status: BAD_REQUEST)
        }
    }

    def fetchDispositionsList() {
        List dispositionList = Disposition.list().collect {
            [displayName : it.displayName ?: ""]
        }
        Map result = ["dispositionList" : dispositionList]
        render result as JSON
    }
}
