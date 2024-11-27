package com.rxlogix.config

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.ValidatedSignal
import org.joda.time.DateTime

class AlertDocument {

    static auditable = true

    Long id
    String documentType
    String documentLink
    String linkText
    Date startDate
    Date targetDate
    String documentStatus
    String author  //TODO: Need to figure out if it should be a real user or a string val
    Date statusDate
    String comments
    String chronicleId
    String productName

    static belongsTo = [alert: Alert]

    static constraints = {
        comments nullable: true
        targetDate nullable: true
        linkText nullable: true, maxSize: 4000
        documentLink nullable: false, maxSize: 4000
        documentType nullable: false
        startDate nullable: false
        documentStatus nullable: false
        author nullable: false, blank: false
        statusDate nullable: false
        chronicleId nullable: false, unique: true, blank: false
        alert nullable: true
        productName nullable: true, maxSize: 4000
    }

    static mapping = {
        table("ALERT_DOCUMENT")
    }

    def updateWithAlertId(alertId) {
        if (alertId) {
            //Calculate the document target date.
            calculateDocumentTargetDate(this, AdHocAlert.get(alertId))

            def alert = AdHocAlert.findById(alertId)
            this.alert = alert
            this.save()

            alert.addToAlertDocuments(this)
            alert.save()
        }

        this
    }

    def updateWithSignalId(signalId) {
        if (signalId) {
            //Calculate the document target date.
            calculateDocumentTargetDateForSignal(this, ValidatedSignal.get(signalId))
            def signal = ValidatedSignal.findById(signalId)
            signal.addToAlertDocuments(this)
            signal.save()
        }
        this
    }

    /**
     * The method to calculate the document due date.
     * @param adhocAlertInstance
     * @param responseJSON
     * @return
     */
    private calculateDocumentTargetDate(alertDocument, AdHocAlert adHocAlertInstance) {
        def priority = adHocAlertInstance.priority

        //There will always be a priority associated with the adhocAlert.
        if (priority) {

            //Fetch the last inserted record for the same alert
            def previousAlertDocument = adHocAlertInstance.alertDocuments?.max { it.id }

            //If there is no previously inserted alert document with same alert the we consider the
            // alert detected date for calculation of target date.
            if (!previousAlertDocument) {
                alertDocument.targetDate = calcTargetDate(priority, alertDocument.documentType, adHocAlertInstance.detectedDate)
            } else { //If there is any previously inserted document then its status date should be considered
                // for calculating target date.
                alertDocument.targetDate = calcTargetDate(priority, alertDocument.documentType, previousAlertDocument.statusDate)
            }
        }
    }

    private calculateDocumentTargetDateForSignal(alertDocument, ValidatedSignal validatedSignal) {
        def priority = validatedSignal.priority

        //There will always be a priority associated with the adhocAlert.
        if (priority) {

            //Fetch the last inserted record for the same alert
            def previousAlertDocument = validatedSignal.alertDocuments?.max { it.id }

            //If there is no previously inserted alert document with same alert the we consider the
            // alert detected date for calculation of target date.
            if (!previousAlertDocument) {
                alertDocument.targetDate = calcTargetDate(priority, alertDocument.documentType, validatedSignal.dateCreated)
            } else { //If there is any previously inserted document then its status date should be considered
                // for calculating target date.
                alertDocument.targetDate = calcTargetDate(priority, alertDocument.documentType, previousAlertDocument.statusDate)
            }
        }
    }

    /**
     * Private method to fetch the priority based value on the basis of priority value,
     * document report type and alert date.
     * @param priority
     * @param reportType
     * @param calcDate
     * @return targetDate
     */
    private calcTargetDate(priority, reportType, Date calcDate) {
        def calcDT = new DateTime(calcDate)
        if (('Low').equals(priority?.value)) {
            if (('Assessment Report').equals(reportType)) {
                return calcDT.plusMonths(3).toDate()
            } else {
                return calcDT.plusDays(45).toDate()
            }
        } else if (('Medium').equals(priority.value)) {
            if (('Assessment Report').equals(reportType)) {
                return calcDT.plusMonths(2).toDate()
            } else {
                return calcDT.plusDays(30).toDate()
            }
        } else if (('High').equals(priority.value)) {
            return calcDT.plusDays(15).toDate()
        }
    }
    @Override
    String toString(){
        "${this.getClass().getSimpleName()} : ${this.id}"
    }
}