package com.rxlogix

import com.rxlogix.user.AccessControlGroup
import grails.validation.ValidationException
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.client.RestTemplate

import static org.springframework.http.HttpStatus.*

@Secured(['ROLE_CONFIGURATION_CRUD'])

class AccessControlGroupController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def CRUDService

    def index() {
        params.max = Constants.Search.MAX_SEARCH_RESULTS
        render view: "index", model: [acgInstanceList:  AccessControlGroup.list(params), acgInstanceTotal: AccessControlGroup.count()]
    }

    def show(AccessControlGroup acgInstance) {
        if (!acgInstance) {
            notFound()
            return
        }

        render view: "show", model: [acgInstance: acgInstance]
    }

    def create() {
        def acgInstance = new AccessControlGroup(params)
        render view: "create", model: [acgInstance: acgInstance]
    }

    def save(AccessControlGroup acgInstance) {
        try {
            acgInstance = (AccessControlGroup) CRUDService.save(acgInstance)
        } catch (ValidationException ve) {
            render view: "create", model: [acgInstance: acgInstance]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'accessControlGroup.label'), acgInstance.name])
                redirect acgInstance
            }
            '*' { respond acgInstance, [status: CREATED] }
        }
    }

    def edit(AccessControlGroup acgInstance) {
        if (!acgInstance) {
            notFound()
            return
        }

        render view: "edit", model: [acgInstance: acgInstance]
    }

    def update(AccessControlGroup acgInstance) {
        if (acgInstance == null) {
            notFound()
            return
        }

        try {
            acgInstance = (AccessControlGroup) CRUDService.update(acgInstance)
        } catch (ValidationException ve) {
            render view: "edit", model: [acgInstance: acgInstance]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'accessControlGroup.label'), acgInstance.name])
                redirect acgInstance
            }
            '*' { respond acgInstance, [status: OK] }
        }
    }

    def delete(AccessControlGroup acgInstance) {

        if (acgInstance == null) {
            notFound()
            return
        }

        try {
            CRUDService.delete(acgInstance)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'accessControlGroup.label'), acgInstance.name])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'accessControlGroup.label'), acgInstance.name])
                    redirect(action: "show", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }

    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'accessControlGroup.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
