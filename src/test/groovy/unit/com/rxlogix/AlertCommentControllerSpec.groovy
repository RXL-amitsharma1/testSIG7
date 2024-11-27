package unit.com.rxlogix
import com.rxlogix.DynamicReportService
import com.rxlogix.SignalAuditLogService
import com.rxlogix.UserService

import com.rxlogix.AlertCommentController
import com.rxlogix.AlertCommentService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.signal.AlertCommentHistory
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */

class AlertCommentControllerSpec extends Specification implements ControllerUnitTest<AlertCommentController> {
    def setup() {

        mockDomain(AlertCommentHistory)
        AlertCommentHistory alertCommentHistory = new AlertCommentHistory(aggAlertId: 1, comments: "comment", alertName: "test alert", period: "2022")
        alertCommentHistory.save(failOnError: false)
    }

    def cleanup() {
    }

    void "test listComments"() {
        given:
        def mockListComment = [id           : 1,
                               comments     : "Test",
                               productName  : "Test Product AJ",
                               eventName    : "Rash",
                               caseNumber   : "1US",
                               productFamily: "Test Product AJ",
                               alertType    : "EVDAS Alert",
                               dateCreated  : "01-Jun-2018",
                               dateUpdated  : "01-Jun-2018",
                               createdBy    : "user",
                               editable     : true]
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.getComment(_) >> mockListComment
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.listComments()
        then:
        response.getJson()["comments"] == "Test"
        response.getJson()["productName"] == "Test Product AJ"
        response.getJson()["eventName"] == "Rash"
        response.getJson()["productFamily"] == "Test Product AJ"
        response.getJson()["alertType"] == "EVDAS Alert"
    }

    void "test listComments when there is no comment"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.getComment(_) >> null
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.listComments()
        then:
        response.status == 200
    }

    void "test saveComment"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.createAlertComment(_) >> true
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.saveComment()
        then:
        response.getJson()["success"] == true
    }

    void "test saveComment if not success"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.createAlertComment(_) >> false
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.saveComment()
        then:
        response.getJson()["success"] == false
    }

    void "test updateComment"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.updateSignalComment(_) >> true
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.updateComment()
        then:
        response.getJson()["success"] == true
    }

    void "test updateComment if not success"() {
        given:
        def mockAlertCommentService = Mock(AlertCommentService)
        mockAlertCommentService.updateSignalComment(_) >> false
        controller.alertCommentService = mockAlertCommentService
        when:
        controller.updateComment()
        then:
        response.getJson()["success"] == false
    }

    void "test listAggCommentsHistory"() {
        when:
        controller.listAggCommentsHistory(1)
        then:
        println response.json
    }

    void "test getBulkCheck"() {
        when:
        controller.getBulkCheck()
        then:
        println "result"
    }

    void "test exportCommentHistory with no search criteria"() {
        given:
        params.searchField = null
        params.isArchived = false
        def mockDynamicReportService = Mock(DynamicReportService)
        mockDynamicReportService.createCommentHistoryReport(_, _, _) >> [name: "ReportName"]
        controller.dynamicReportService = mockDynamicReportService
        def mockSignalAuditLogService = Mock(SignalAuditLogService)
        mockSignalAuditLogService.createAuditForExport(_, _, _, _, _) >> {}
        controller.signalAuditLogService = mockSignalAuditLogService
        when:
        controller.exportCommentHistory(1)
        then:
        response.status == 200
        response.getJson()["name"] == "ReportName"
        1 * mockDynamicReportService.createCommentHistoryReport(_, _, _)
        1 * mockSignalAuditLogService.createAuditForExport(_, _, _, _, "ReportName")
    }

    void "test exportCommentHistory with search criteria containing underscore"() {
        given:
        params.searchField = "comment_example"
        params.isArchived = false
        def mockDynamicReportService = Mock(DynamicReportService)
        mockDynamicReportService.createCommentHistoryReport(_, _, _) >> [name: "ReportName"]
        controller.dynamicReportService = mockDynamicReportService
        def mockSignalAuditLogService = Mock(SignalAuditLogService)
        mockSignalAuditLogService.createAuditForExport(_, _, _, _, _) >> {}
        controller.signalAuditLogService = mockSignalAuditLogService
        when:
        controller.exportCommentHistory(1)
        then:
        response.status == 200
        response.getJson()["name"] == "ReportName"
        1 * mockDynamicReportService.createCommentHistoryReport(_, _, _)
        1 * mockSignalAuditLogService.createAuditForExport(_, _, _, _, "ReportName")
    }

    void "test exportCommentHistory with search criteria containing percent sign"() {
        given:
        params.searchField = "comment%"
        params.isArchived = false
        def mockDynamicReportService = Mock(DynamicReportService)
        mockDynamicReportService.createCommentHistoryReport(_, _, _) >> [name: "ReportName"]
        controller.dynamicReportService = mockDynamicReportService
        def mockSignalAuditLogService = Mock(SignalAuditLogService)
        mockSignalAuditLogService.createAuditForExport(_, _, _, _, _) >> {}
        controller.signalAuditLogService = mockSignalAuditLogService
        when:

        userService
        controller.exportCommentHistory(1)
        then:
        response.status == 200
        response.getJson()["name"] == "ReportName"
        1 * mockDynamicReportService.createCommentHistoryReport(_, _, _)
        1 * mockSignalAuditLogService.createAuditForExport(_, _, _, _, "ReportName")
    }

    void "test exportCommentHistory with valid search criteria and date conversion"() {
        given:
        params.searchField = "alertName"
        params.isArchived = false
        def mockDynamicReportService = Mock(DynamicReportService)
        mockDynamicReportService.createCommentHistoryReport(_, _, _) >> [name: "ReportName"]
        controller.dynamicReportService = mockDynamicReportService
        def mockSignalAuditLogService = Mock(SignalAuditLogService)
        mockSignalAuditLogService.createAuditForExport(_, _, _, _, _) >> {}
        controller.signalAuditLogService = mockSignalAuditLogService
        when:
        UserService userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> new User(id: 20)
        controller.userService = userService
        controller.exportCommentHistory(1)
        then:
        response.status == 200
        response.getJson()["name"] == "ReportName"
        1 * mockDynamicReportService.createCommentHistoryReport(_, _, _)
        1 * mockSignalAuditLogService.createAuditForExport(_, _, _, _, "ReportName")
    }

    void "test exportCommentHistory with archived case"() {
        given:
        params.searchField = null
        params.isArchived = true
        def mockDynamicReportService = Mock(DynamicReportService)
        mockDynamicReportService.createCommentHistoryReport(_, _, _) >> [name: "ReportName"]
        controller.dynamicReportService = mockDynamicReportService
        def mockSignalAuditLogService = Mock(SignalAuditLogService)
        mockSignalAuditLogService.createAuditForExport(_, _, _, _, _) >> {}
        controller.signalAuditLogService = mockSignalAuditLogService
        when:
        controller.exportCommentHistory(1)
        then:
        response.status == 200
        response.getJson()["name"] == "ReportName"
        1 * mockDynamicReportService.createCommentHistoryReport(_, _, _)
        1 * mockSignalAuditLogService.createAuditForExport(_, _, _, _, "ReportName")
    }

}
