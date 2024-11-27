package com.rxlogix

import com.rxlogix.config.EtlSchedule
import com.rxlogix.config.EtlStatus
import com.rxlogix.customException.EtlUpdateException
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class EtlScheduleController {

    static allowedMethods = [save: "POST", update: "PUT"]

    def CRUDService
    def userService
    def etlJobService


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        def etlSchedule = null
        def etlStatus = null
        try {
            etlSchedule = etlJobService.getSchedule()
            //Fetch the etl status and pass in the params map.
            EtlStatus.withTransaction {
                etlStatus = EtlStatus.first()
            }
        } catch (Exception ex) {
            log.error(ex.getMessage())
            flash.error = message(code: "app.etl.exception.label")
        }
        render view: "index", model: [etlScheduleInstance: etlSchedule, etlStatus: etlStatus?.status, lastRunDateTime: etlStatus?.lastRunDateTime]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit() {
        def etlScheduleInstance = etlJobService.getSchedule()
        render view: "edit", model: [etlScheduleInstance: etlScheduleInstance]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update(EtlSchedule etlScheduleInstance) {

        if (!etlScheduleInstance) {
            notFound()
            return
        }

        def etlScheduleInstancePre = etlScheduleInstance

        try {
            etlJobService.update(etlScheduleInstance)
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)

        } catch (EtlUpdateException ex) {
            flash.error = message(code: "modify.schedule.request.invalid.date")
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstancePre]
            return
        } catch (Exception ve) {
            flash.error = message(code: "modify.schedule.request.failed")
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstancePre]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'etlSchedule.label'), etlScheduleInstance.scheduleName])
                redirect action: "index"
            }

        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def disable() {
        def etlScheduleInstance = etlJobService.getSchedule()

        etlScheduleInstance.isDisabled = true;

        try {
            etlJobService.disable(etlScheduleInstance)
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)

        } catch (Exception ve) {
            etlScheduleInstance.isDisabled = false
            flash.error = message(code: "disable.schedule.request.failed")
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstance]
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'etlSchedule.label'), etlScheduleInstance.scheduleName])
                redirect action: "index"
            }
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def enable() {

        def etlScheduleInstance = etlJobService.getSchedule()

        //Change the isDisabled flag to false.
        etlScheduleInstance.isDisabled = false;

        try {
            etlJobService.enable()
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)
        } catch (Exception ve) {
            etlScheduleInstance.isDisabled = true;
            flash.error = message(code: "enable.schedule.request.failed")
            render view: "edit", model: [etlScheduleInstance: etlScheduleInstance]
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'default.enabled.message', args: [message(code: 'etlSchedule.label'), etlScheduleInstance.scheduleName])
                redirect action: "index"
            }

        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def initialize() {

        def etlScheduleInstance = etlJobService.getSchedule()

        etlScheduleInstance.isInitial = false;

        try {
            etlJobService.initialize(etlScheduleInstance)
            etlScheduleInstance = (EtlSchedule) CRUDService.update(etlScheduleInstance)
        } catch (Exception ve) {
            flash.error = message(code: "request.to.run.initial.etl.failed")
            redirect action: "index"
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.etlinitialized.message')
                redirect action: "index"
            }

        }
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def getEtlStatus() {
        def etlStatus = null
        EtlStatus.withTransaction {
            etlStatus = EtlStatus."pva".first()
        }
        def statusJson = [
                "status": etlStatus.status.name()
        ]
        response.status = 200
        render statusJson as JSON;
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def getEtlScheduleResult() {
        def resultList = etlJobService.getEtlScheduleResult()?.collect {
            [
                    stageKey  : message(code: it.stageKey.messageKey),
                    startTime : it.startTime,
                    finishTime: it.finishTime,
                    passStatus: it.passStatus
            ]
        }
        response.status = 200
        render resultList as JSON
    }

    def etlInfo() {
        def result = [:]
        def etlSchedule = etlJobService.getSchedule()
        def etlStatus = etlJobService.getEtlStatus()
        String status = etlStatus.status?.name()
        if(status != null) {
            result.status = status?.substring(0, 1)?.toUpperCase() + status?.substring(1)?.toLowerCase()
        }
        result.enabled = (etlSchedule?.isDisabled ? ViewHelper.getMessage("default.button.no.label") : ViewHelper.getMessage("default.button.yes.label"))
        result.lastRun = DateUtil.getLongDateStringForTimeZone(etlStatus.lastRunDateTime, userService.getUser()?.preference?.timeZone)
        result.repeat = []
        etlSchedule?.repeatInterval?.split(';')?.each {
            def set = it.split("=")
            String frequency=set[1]?.substring(0,1)?.toUpperCase()+set[1]?.substring(1)?.toLowerCase()
            result.repeat <<[label: g.message(code: "scheduler." + set[0].toLowerCase(), default: set[0].toLowerCase()), value: g.message(code: "scheduler." + frequency, default: frequency)]
        }
        render([result: result] as JSON)
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'etlSchedule.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

}
