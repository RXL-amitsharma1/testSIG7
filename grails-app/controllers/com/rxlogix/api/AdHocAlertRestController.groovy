package com.rxlogix.api

import com.rxlogix.helper.LinkHelper
import com.rxlogix.util.AlertUtil
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_OK

class AdHocAlertRestController implements AlertUtil, LinkHelper {
    def adHocAlertService
    def importLogService
    def alertDocumentService
    def emailService

    static responseFormats = ['json']
    static allowedMethods = [save: "POST"]

    def messageCode = "import.json.valid"

    def importAlert() {
        def importLog = importLogService.createLog("Import AdHocAlert")
        def alertResults
        try {
            def jsonContent = request.JSON
            if (!jsonContent) throw new Throwable("Empty JSON content")

            alertResults = adHocAlertService.importAlert(jsonContent.toString())
            sendAssignedToEmail(alertResults)

            render status: SC_OK
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex)
            messageCode = "import.invalid.or.empty.json.content"
            response.status = SC_BAD_REQUEST
        } finally {
            importLogService.saveLog(importLog, message(code: messageCode) , alertResults)
            emailService.sendImportAlertEmail(['toAddress': grailsApplication.config.pvsignal.alert.import.notification.email.split(",").toList(),
                                               'title': message(code:"import.alert.log.title"),
                                               'map':['logInstance':importLog
                                               ]])

        }
    }
    def importDocument() {
        def importLog = importLogService.createLog("Import Document")
        def ads = []
        try {
            def jsonContent = request.JSON
            if (!jsonContent) throw new Throwable("Empty JSON content")

            ads = alertDocumentService.importDocumentsFromJson(jsonContent)
            render status: SC_OK
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
            messageCode = "import.invalid.or.empty.json.content"
            response.status = SC_BAD_REQUEST
        } finally {
            importLogService.saveLog(importLog, message(code: messageCode) , ads)
            emailService.sendImportDocumentEmail(['toAddress': grailsApplication.config.pvsignal.document.import.notification.email.split(",").toList(),
                                               'title': message(code:"import.documents.log.title"),
                                               'map':['logInstance':importLog
                                               ]])
        }
    }

    def sendAssignedToEmail(adHocAlerts) {
        adHocAlerts.findAll({!it.errors || !it.errors.hasErrors()}).each{
            def alertLink = createHref("adHocAlert", "alertDetail",["id":it.id])
            def productName = getNameFieldFromJson(it.productSelection)

            emailService.sendAlertCreateEmail(['toAddress':[it.assignedTo?.email],
                                               'title':message(code: "app.email.alert.create.title",
                                                       args: [it.initialDataSource, productName]),
                                               'map':['alertInstance':it, "alertLink": alertLink, "productName":productName]])

        }
    }
}
