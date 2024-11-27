package com.rxlogix.alertDocument

import com.rxlogix.config.AlertDocument
import com.rxlogix.domain.CriteriaAggregator
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.ValidatedAlertDocument
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.util.DateUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import javax.sql.DataSource
import groovy.sql.Sql
import java.time.ZoneId

@Transactional
class AlertDocumentService {

    def grailsApplication
    def signalDataSourceService
    def dataSource

    def updateMultipleDocWithAlertId(chronicleIds, alertId) {
        if (chronicleIds && alertId) {
            chronicleIds.each { chronicleId ->
                AlertDocument.findByChronicleId(chronicleId)?.updateWithAlertId(alertId)
            }
        }
    }

    def updateMultipleDocWithSignalId(chronicleIds, signalId) {
        if (chronicleIds && signalId) {
            chronicleIds.each { chronicleId ->
                AlertDocument.findByChronicleId(chronicleId)?.updateWithSignalId(signalId)
            }
        }
    }

    def updateWithAlertId(chronicleId, alertId) {
        if (chronicleId && alertId) {
            def alertDoc = AlertDocument.findByChronicleId(chronicleId)
            def alert = Alert.get(alertId)
            //Calculate the document target date.
            alertDoc.calculateDocumentTargetDate(alertDoc, Alert.get(alertId))
            alertDoc.alert = alert
            alertDoc.save()

            alertDoc
        }
    }

    def filterAlert(productName, documentType) {
        def customerQueryAggregator = new CriteriaAggregator(AlertDocument)

        customerQueryAggregator.addCriteria {
            isNull('alert')
        }

        if (productName) {
            customerQueryAggregator.addCriteria {
                eq('productName', productName)
            }
        }

        if (documentType) {
            customerQueryAggregator.addCriteria {
                eq('documentType', documentType)
            }
        }

        customerQueryAggregator.list()
    }

    def getUnlinkedDocuments() {
        AlertDocument.findAll {alert == null}
    }

    def getSignalUnlikedDocuments() {

        List<Map> data = []
        def sql = new Sql(dataSource)
        try {
            def sql_statement = "select * from ALERT_DOCUMENT WHERE ID NOT IN (select ALERT_DOCUMENT_ID from VALIDATED_ALERT_DOCUMENTS)"
            data=sql.rows(sql_statement)
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }

            getSignalUnlikedDocumentsListFromSqlData(data)

    }

    List<Map> getSignalUnlikedDocumentsListFromSqlData(List<Map> documentList) {
        documentList?.collect { def document ->
            [
                    documentType  : document.DOCUMENT_TYPE,
                    documentLink  : document.DOCUMENT_LINK,
                    linkText      : document.LINK_TEXT,
                    startDate     : Date.from(document.START_DATE.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()),
                    targetDate    : document.TARGET_DATE ? Date.from(document.TARGET_DATE.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()) : null,
                    documentStatus: document.DOCUMENT_STATUS,
                    author        : document.AUTHOR,
                    statusDate    : Date.from(document.STATUS_DATE.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()),
                    comments      : document.COMMENTS,
                    chronicleId   : document.CHRONICLE_ID,
                    productName   : document.PRODUCT_NAME
            ]
        }
    }

    def listByAlert(alertId) {
        AdHocAlert.get(alertId)?.alertDocuments?.sort()
    }

    def listBySignal(signalId) {
        ValidatedSignal.get(signalId).alertDocuments?.sort()
    }

    @Transactional
    def updateAlertDocument(chronicleId, comment) {
        if (chronicleId) {
            def alertDoc = AlertDocument.findByChronicleId(chronicleId)
            alertDoc.comments = comment
            alertDoc.save()
            alertDoc
        }
    }

    @Transactional
    def unlinkDocument(chronicleId) {
        if (chronicleId) {
            def alertDoc = AlertDocument.findByChronicleId(chronicleId)
            if (alertDoc.alert) {
                try {
                    def adHocAlert = alertDoc.alert as AdHocAlert
                    adHocAlert.removeFromAlertDocuments(alertDoc)
                    adHocAlert.save()
                    alertDoc.comments = null
                    alertDoc.targetDate = null
                    alertDoc.alert = null
                    alertDoc.save()
                } catch (Throwable throwable) {
                    log.error("Error in unlinking the document from alert", throwable.getMessage())
                }
                alertDoc
            }

        }
    }

    @Transactional
    def unlinkDocumentForSignal(chronicleId, validatedSignal) {
        if (chronicleId) {
            def alertDoc = AlertDocument.findByChronicleId(chronicleId)
            validatedSignal.removeFromAlertDocuments(alertDoc)
            alertDoc
        }
    }

    def importDocumentsFromJson(ads) {
        def alertDocuments = []
        def formats = Holders.config.grails.databinding.dateFormats
        def timezone = Holders.config.server.timezone

        for (def i = 0; i < ads.length(); i ++) {
            def it = ads.get(i)

            AlertDocument ad = AlertDocument.findByChronicleId(it['chronicleId'] ?: " ")
            if(!ad)
                ad = new AlertDocument()

            ad.chronicleId = it['chronicleId']
            ad.startDate = DateUtil.getDate(it['startDate'], formats, timezone)
            ad.statusDate = DateUtil.getDate(it['statusDate'],formats, timezone)
            ad.author = it['author']
            ad.documentLink = it['documentLink']
            ad.linkText = it['linkText']
            ad.documentStatus = it['documentStatus']
            ad.productName = it['productName']
            ad.documentType = it['documentType']
            if(!ad.hasErrors()) {
                ad.save()
                alertDocuments << ad
            }
        }

        return alertDocuments
    }
}