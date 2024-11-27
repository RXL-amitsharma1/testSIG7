package com.rxlogix
import com.rxlogix.config.AlertStopList
import grails.plugin.springsecurity.annotation.Secured
import com.rxlogix.controllers.AlertController

@Secured(["isAuthenticated()"])
class AlertStopListController implements AlertController{
    def alertStopListService
    def CRUDService
    def userService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        AlertStopList alertStopList = new AlertStopList()
        render (view: "index", model: [alertStopList:alertStopList])
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def alertStopList = alertStopListService.getAlertStopList()
        respond alertStopList, [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        AlertStopList alertStopList = new AlertStopList()
        if(!allowedDictionarySelection(params)) {
            flash.error = message(code: "app.label.product.family.error.message")
            render (view: "index", model: [alertStopList:alertStopList])
            return
        }
        if(!isPrefferedTerm(params)) {
            flash.error = message(code: "app.label.pt.error.message")
            render (view: "index", model: [alertStopList:alertStopList])
            return
        }
        alertStopList.productName = params.productSelection
        alertStopList.eventName = params.eventSelection
        alertStopList.dateCreated = new Date()
        alertStopList = alertStopListService.saveList(alertStopList)
        render (view: "index", model: [alertStopList:alertStopList])
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {
        AlertStopList asl = AlertStopList.findById(params.id)
        asl.activated = Boolean.parseBoolean(params.activated)
        if(!asl.activated) {
            asl.dateDeactivated = new Date()
        }
        asl = alertStopListService.updateList(asl)
        render (view: "index")
    }

}
