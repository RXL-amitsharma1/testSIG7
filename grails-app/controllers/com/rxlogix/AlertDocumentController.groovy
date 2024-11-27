package com.rxlogix

import com.rxlogix.util.DateUtil
import com.rxlogix.config.AlertDocument
import grails.converters.JSON
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.StringUtils
import com.rxlogix.signal.ValidatedSignal


@Secured(["isAuthenticated()"])
class AlertDocumentController {

    def alertDocumentService
    def activityService
    def userService

    /**
     * Adds document to alert.
     * @return
     */
    def addToAlert() {
        def chronicleIds = params["chronicle_ids"].split(',')
        def alertId = params['alertId']
        def blankedChronicleIds = chronicleIds.join('').trim()

        if (blankedChronicleIds){
            alertDocumentService.updateMultipleDocWithAlertId(chronicleIds, alertId)
            activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.DocumentAddedToAlert),
                    userService.getUser(), "Documents with chronical ids $chronicleIds added to alert", null)

            render {status: 200}
        }
    }

    def addToSignal() {
        def chronicleIds = params["chronicle_ids"].split(',')
        def signalId = params['signalId']
        def blankedChronicleIds = chronicleIds.join('').trim()

        if (blankedChronicleIds){
            alertDocumentService.updateMultipleDocWithSignalId(chronicleIds, signalId)
            def validatedSignal = ValidatedSignal.get(Long.parseLong(signalId))
            activityService.createActivityForSignal(validatedSignal, "", "Documents with chronical ids $chronicleIds added to signal",
            ActivityType.findByValue(ActivityTypeValue.DocumentAddedToSignal), validatedSignal.assignedTo, userService.getUser(), null,validatedSignal.assignedToGroup)
            render {status: 200}
        }
    }

    /**
     * Thie method fetches the unlined documents.
     * @return
     */
    def getUnlinkedDocuments() {
        def docList = getDocumentListDTO(alertDocumentService.getUnlinkedDocuments())
        respond docList, [formats:['json']]
    }

    def getUnlinkedsignalDocuments() {
        def documentList = alertDocumentService.getSignalUnlikedDocuments()
        def docList = getDocumentListDTO(documentList)
        respond docList, [formats:['json']]
    }

    /**
     * Fetches the alert list based on the alert id.
     * @return
     */
    def listByAlert() {
        def alertId = params['alertId']
        def documentList = alertDocumentService.listByAlert(alertId)
        def docList = getDocumentListDTO(documentList)
        if (!docList) {
            docList = []
        }
        respond docList, [formats:['json']]
    }

    def listBySignal() {
        def signalId = params['signalId']
        def docList = getDocumentListDTO(alertDocumentService.listBySignal(signalId))
        if (!docList) {
            docList = []
        }
        respond docList, [formats:['json']]
    }

    def filterAlerts() {
        def productName = params['productName']
        def documentType = params['documentType']
        def alertDocument = alertDocumentService.filterAlert(productName, documentType)
        def docList = getDocumentListDTO(alertDocument)
        respond docList, [formats:['json']]
    }

    /**
     * Method to prepare the list of document dto
     * @param documentList
     * @return
     */
    private def getDocumentListDTO(documentList) {
        def timezone = grailsApplication.config.server.timezone

        documentList?.collect { def document ->
            [
                    documentType  : document.documentType,
                    documentLink  : document.documentLink,
                    linkText      : document.linkText ?: document.documentLink,
                    startDate     : DateUtil.toDateString(document.startDate, timezone),
                    targetDate    : DateUtil.toDateString(document.targetDate, timezone),
                    documentStatus: document.documentStatus,
                    author        : document.author,
                    statusDate    : DateUtil.toDateString(document.statusDate, timezone),
                    comments      : document.comments,
                    chronicleId   : document.chronicleId,
                    productName   : document.productName
            ]
        }
    }

    /**
     * This method is responsible for updating the alert document.
     * @return
     */
    def updateAlertDocument() {
        def chronicleId = params["chronicleId"]
        def comment = params['comment']
        def document = alertDocumentService.updateAlertDocument(chronicleId, comment)

        def alertId = params['alertId']
        def signalId = params['signalId']

        if (signalId) {
            def validatedSignal = ValidatedSignal.get(signalId)
            activityService.createActivityForSignal(validatedSignal, "", "Documents with chronical id $chronicleId updated",
                    ActivityType.findByValue(ActivityTypeValue.DocumentUpdated), validatedSignal.assignedTo, userService.getUser(), null,validatedSignal.assignedToGroup)
        } else {
            activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.DocumentUpdated),
                    userService.getUser(), "Document with chronical id $chronicleId updated", null)
        }
        response.status = 200
        render document as JSON
    }

    /**
     * This method is responsible for unlinking the document.
     * @return
     */
    def unlinkDocument() {
        def chronicleId = params["chronicleId"]
        def alertId = params['alertId']
        def signalId = params['signalId']
        def document = null

        if (signalId) {
            def validatedSignal = ValidatedSignal.get(signalId)
            document = alertDocumentService.unlinkDocumentForSignal(chronicleId, validatedSignal)
            activityService.createActivityForSignal(validatedSignal, "", "Documents with chronical id $chronicleId removed from signal",
                    ActivityType.findByValue(ActivityTypeValue.DocumentRemovedFromSignal), validatedSignal.assignedTo, userService.getUser(), null,validatedSignal.assignedToGroup)
        } else {
            document = alertDocumentService.unlinkDocument(chronicleId)
            activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.DocumentRemovedFromAlert),
                    userService.getUser(), "Document with chronical id $chronicleId removed from alert", null)

        }
        response.status = 200
        render document as JSON
    }

}
