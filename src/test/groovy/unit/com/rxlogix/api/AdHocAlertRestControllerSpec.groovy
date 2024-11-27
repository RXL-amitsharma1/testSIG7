package com.rxlogix.api

import com.rxlogix.*
import com.rxlogix.alertDocument.AlertDocumentService
import com.rxlogix.config.*
import com.rxlogix.mapping.LmCountry
import com.rxlogix.mapping.LmReportType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.context.MessageSource
import org.springframework.context.support.AbstractMessageSource
import spock.lang.Ignore
import spock.lang.Shared

@Ignore
class AdHocAlertRestControllerSpec extends spock.lang.Specification implements ControllerUnitTest<AdHocAlertRestController> {
    def springSecurityService = mockFor(SpringSecurityService)
    MessageSource messageSource
    def alertDocumentService

    def setup() {

        def admin = new User(username: 'admin', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Admin User')
        def detector = new User(username: 'detector', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Detector')
        def sharedUser = new User(username: 'drno', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Dr. No')
        def importer = new User(username: 'importer', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Interface User')
        admin.save()
        detector.save()
        sharedUser.save()
        importer.save()

        assert(User.count == 4)

        springSecurityService.demand.getCurrentUser {-> admin }

        def priority1 = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, defaultPriority: true, priorityOrder: 1)
        def priority2 = new Priority(value: "Medium", display: true, displayName: "Medium", reviewPeriod: 3, priorityOrder: 2)
        priority1.save()
        priority2.save()
        assert(Priority.count == 2)

        PVSStateValue.values().each {
            def pvsstate =new PVSState(value:it.value, displayName: it.value)
            pvsstate.save()
        }

        def importLogServiceMock = mockFor(ImportLogService)
        importLogServiceMock.demand.createLog {type ->
            return new ImportLog(type: "test", startTime: new Date(), numSucceeded: 0, numFailed: 0).save()
        }

        importLogServiceMock.demand.saveLog {log, resp, importItems  ->
            log.numSucceeded = importItems?.count { it.errors.allErrors.size() == 0 } ?: 0
            log.numFailed = importItems?.count { it.errors.allErrors.size() > 0 } ?: 0
            log.details = logDetails(log, importItems, Locale.ENGLISH)
            log.save()
        }

        importLogServiceMock.demand.logDetails {log, importItems, locale -> def i = -1
            def details = []
            importItems?.collect{ item ->
                i = i + 1
                if(item.errors.allErrors) {
                    def inputId
                    if(item.class == AlertDocument)
                        inputId = item.chronicleId
                    else
                        inputId = item.slimId

                    def detail = new ImportDetail(recNum: i, inputIdentifier: inputId ?: "Unknown", message: "test" ?: "Saved", log: log)
                    details << detail
                }
            }
            return details}

        def alertDocumentServiceMock = mockFor(AlertDocumentService)
        alertDocumentServiceMock.demand.importDocumentsFromJson{ads ->
            def alertDocuments = []

            def formats = grailsApplication.config.grails.databinding.dateFormats
            def timezone = grailsApplication.config.server.timezone

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
                ad.documentStatus = it['documentStatus']
                ad.productName = it['productName']
                ad.documentType = it['documentType']
                if(ad.validate())
                    ad.save()
                alertDocuments << ad

            }

            return alertDocuments
        }

        controller.importLogService = importLogServiceMock.createMock()
        controller.alertDocumentService = alertDocumentServiceMock.createMock()



    }

    def cleanup() {
    }

    void "test import alert without Errors"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }
        def speidImportService = Spy(AdHocAlertService) {
            importAlert(_) >> {}
        }
        controller.emailService = spiedEmailService
        controller.adHocAlertService = speidImportService

        when:
        request.json = '''[
                {"id":"1385437113430",
                 "detectedByUCB":"UCB",
                 "description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",
                 "safetyObservation":"01-Mar-2016",
                 "initialDataSource":"SLIM",
                 "alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",
                 "productName":"Lidocaine",
                 "topicName":"Injection of lidocaine supressed lactation in  apatient: SNB"}
            ]'''
        controller.importAlert()

        then:
        assert response.status == 200
        assert ImportLog.count == 1
        assert ImportDetail.count == 0
    }

    void "test import alert without alertName and productName"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService

        when:
        request.json = '''[
            {
                "Formulation":"Formulation 2",
                "Relevant Indication":"Indication 2",
                "Responsible Physician":"admin",
                "Topic of Interest":"Alert Name 2",
                "Safety Observation Date of Awareness":"11-Jan-15",
                "Detected by UCB, Health Authority, Other":"Other",
                "Initial Data Source":"Empirica",
                "Issue Previously Tracked":"No",
                "Is associated with the RMP":"No",
                "Number of ICSRs (if applicable)":"4",
                "SOC of Interest":"Blood 2",
                "Priority (H, M, L)":"High"
            }
        ]'''

        controller.importAlert()

        then:
        assert response.status == 200
        assert AdHocAlert.count == 0
        assert ImportLog.count == 1
        assert ImportLog.list()[0].numSucceeded == 0
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 0
    }

    void "test import alert with invalid date format"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService

        when:
        request.json = '''[
            {"id":"1385437113430",
             "detectedByUCB":"UCB",
             "description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",
             "safetyObservation":"01",
             "initialDataSource":"SLIM",
             "alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",
             "productName":"Lidocaine",
             "topicName":"Injection of lidocaine supressed lactation in  apatient: SNB"}
        ]'''

        controller.importAlert()

        then:
        assert response.status == 200
        assert AdHocAlert.count == 0
        assert ImportLog.count == 1
        assert ImportLog.list()[0].numSucceeded == 0
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 0
    }

    void "test importing documents without errors" () {
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService

        when:
        request.json = """[{
        "chronicleId": "test-chronicle-id",
        "documentType": "Test",
        "documentLink": "http://test_link",
        "documentStatus": "test status",
        "startDate": "03.05.2016",
        "statusDate": "03.05.2016",
        "author": "John Doe"
        }]"""

        controller.importDocument()
        then:
        response.status == 200
        AlertDocument.count() == 1
        assert ImportLog.list()[0].numSucceeded == 1
        assert ImportLog.list()[0].numFailed == 0
        assert ImportDetail.count == 0

    }

    void "test importing documents with errors" () {
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService
        when:
        request.json = """[{
        "documentType": "Test",
        "documentLink": "http://test_link",
        "documentStatus": "test status",
        "startDate": "2016",
        "statusDate": "03.05.2016",
        "author": "John Doe"
        }]"""
        controller.importDocument()
        then:
        response.status == 200
        AlertDocument.count() == 0
        assert ImportLog.list()[0].numSucceeded == 0
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 0

    }

    void "test import alert with and without Errors"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService
        when:
        request.json = '''[
            {"id":"1383622846673",
             "detectedByUCB":"UCB",
             "description":"Brandlistuen Ragnhild Eek, Prenatal paracetamol exposure and child neurodevelopment: a sibling-controlled cohort study., International journal of epidemiology, 2013",
             "safetyObservation":"01-Mar-2016","initialDataSource":"SLIM","alertName":"Paracetamol, paracetamol causing neurodevelopment problems, although autism is associated with the use of paracetamol/ acetaminophen but since already it is considered a potential signal.:  regulatory authorities have expressed concern about the same ,SNB, 1383622846673",
             "productName":"Paracetamol",
             "topicName":"paracetamol causing neurodevelopment problems, although autism is associated with the use of paracetamol/ acetaminophen but since already regulatory authorities have expressed concern about the same , it is considered a potential signal.: SNB"},
                {"id":"1385320016151",
                 "detectedByUCB":"UCB",
                 "description":"Pizzo F., Intravenous valproate and levetiracetam as innovative therapy of epileptic emergencies, Bollettino - Lega Italiana contro l''Epilessia, 2011",
                 "safetyObservation":"01-Mar-2016",
                 "initialDataSource":"SLIM",
                 "alertName":"Levetiracetam, , 1385320016151",
                 "productName":"Levetiracetam",
                 "topicName":null}
        ]'''

        controller.importAlert()

        then:
        assert response.status == 200
        assert AdHocAlert.count == 1
        assert ImportLog.list()[0].numSucceeded == 1
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 1

    }

    void "test import documents with no chronicleId"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService

        when:
        request.json = '''[
           {
            "documentType": "Test",
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2016",
            "statusDate": "03.05.2016",
            "author": "Author"
        }]'''

        controller.importDocument()

        then:
        response.status == 200
        AlertDocument.count() == 0
        assert ImportLog.list()[0].numSucceeded == 0
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 0
    }

    void "test import documents with empty chronicleId"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService
        when:
        request.json = '''[
           {
            "chronicleId":"",
            "documentType": "Test",
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2016",
            "statusDate": "03.05.2016",
            "author": "Author"
        }]'''
        controller.importDocument()

        then:
        response.status == 200
        AlertDocument.count() == 0
        assert ImportLog.list()[0].numSucceeded == 0
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 0
    }

    void "test import documents with null chronicleId"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService
        when:
        request.json = '''[
           {
            "chronicleId": null,
            "documentType": "Test",
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2016",
            "statusDate": "03.05.2016",
            "author": "Author"
        }]'''
        controller.importDocument()

        then:
        response.status == 200
        AlertDocument.count() == 0
        assert ImportLog.list()[0].numSucceeded == 0
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 0
    }

    void "test import documents with no documentType"(){
        setup:
        def spiedEmailService = Spy(EmailService) {
            logMail(_,_,_,_) >> {}
            sendEmailHtml(_,_,_,_) >> {}
        }

        controller.emailService = spiedEmailService

        when:
        request.json = '''[
           {
            "chronicleId": null,
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2016",
            "statusDate": "03.05.2016",
            "author": "Author"
        }]'''
        controller.importDocument()

        then:
        response.status == 200
        AlertDocument.count() == 0
        assert ImportLog.list()[0].numSucceeded == 0
        assert ImportLog.list()[0].numFailed == 1
        assert ImportDetail.count == 1
        assert ImportDetail.list()[0].recNum == 0
    }
}
