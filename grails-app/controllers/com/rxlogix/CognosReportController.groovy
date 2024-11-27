package com.rxlogix

import com.rxlogix.config.CognosReport
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

@Secured(["isAuthenticated()"])
class CognosReportController {

    def CRUDService

    @Secured(['ROLE_COGNOS_VIEW'])
    def index() {
        params.max = Constants.Search.MAX_SEARCH_RESULTS
        render view: "index", model: [cognosReportInstanceList:  CognosReport.findAllByIsDeleted(false, params), cognosReportInstanceTotal: CognosReport.countByIsDeleted(false)]
    }

    @Secured(['ROLE_COGNOS_VIEW'])
    def show() {
        def cognosReportInstance = CognosReport.findByIdAndIsDeleted(params.id, false)
        if (!cognosReportInstance) {
            notFound()
            return
        }

        render view: "show", model: [cognosReportInstance: cognosReportInstance]
    }

    @Secured(['ROLE_COGNOS_CRUD'])
    def create() {
        def cognosReportInstance = new CognosReport(params)
        render view: "create", model: [cognosReportInstance: cognosReportInstance]
    }

    @Secured(['ROLE_COGNOS_CRUD'])
    def save(CognosReport cognosReportInstance) {
        try {
            cognosReportInstance = (CognosReport) CRUDService.save(cognosReportInstance)
        } catch (ValidationException ve) {
            render view: "create", model: [cognosReportInstance: cognosReportInstance]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'cognosReport.label'), cognosReportInstance.name])
                redirect cognosReportInstance
            }
            '*' { respond cognosReportInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_COGNOS_CRUD'])
    def edit() {
        def cognosReportInstance = CognosReport.findByIdAndIsDeleted(params.id, false)
        if (!cognosReportInstance) {
            notFound()
            return
        }

        render view: "edit", model: [cognosReportInstance: cognosReportInstance]
    }

    @Secured(['ROLE_COGNOS_CRUD'])
    def update(CognosReport cognosReportInstance) {

        if (cognosReportInstance == null) {
            notFound()
            return
        }

        try {
            cognosReportInstance = (CognosReport) CRUDService.update(cognosReportInstance)
        } catch (ValidationException ve) {
            render view: "edit", model: [cognosReportInstance: cognosReportInstance]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'cognosReport.label'), cognosReportInstance.name])
                redirect(action: "index")
            }
            '*' { respond cognosReportInstance, [status: OK] }
        }
    }

    @Secured(['ROLE_COGNOS_CRUD'])
    def delete(CognosReport cognosReportInstance) {

        if (cognosReportInstance == null) {
            notFound()
            return
        }

        try {
            cognosReportInstance = (CognosReport) CRUDService.softDelete(cognosReportInstance)
        } catch (ValidationException ve) {
            render view: "edit", model: [cognosReportInstance: cognosReportInstance]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'cognosReport.label'), cognosReportInstance.name])
                redirect(action: "index")
            }
            '*' { respond cognosReportInstance, [status: OK] }
        }

        //redirect(action: "index")
    }



    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'cognosReport.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
