package com.rxlogix

import com.rxlogix.config.DictionaryMapping
import com.rxlogix.signal.ReportHistory
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class ReportControllerSpec extends Specification implements ControllerUnitTest<ReportController> {
    @Shared
    User userObj
    @Shared
    ReportHistory reportHistory

    def setup() {
        userObj = new User()
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.preference.createdBy = "createdBy"
        userObj.preference.modifiedBy = "modifiedBy"
        userObj.preference.locale = new Locale("en")
        userObj.preference.isEmailEnabled = false
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj.metaClass.isAdmin = { true }
        userObj.groups = []
        userObj.save(validate: false)
        controller.userService = [getUser: { return userObj }]

        reportHistory = new ReportHistory(reportName: "reportName", reportType: 'Memo Reports', dateCreated: new Date(),
                startDate: new Date(), endDate: new Date() + 10, productName: "APREMILAST", dataSource: "pva", updatedBy: userObj)
        reportHistory.save(flush:true, failOnError: true)
    }
    def cleanup() {
    }

    void "test requestReport when report type is MEMO_REPORT"(){
        setup:
        String dataSource ="pva"
        String reportName = "firstReport"
        String productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        String productGroupSelection = '[{"name":"testing_AS (3)","id":"3"}]'
        String dateRangeType = "CUMULATIVE"
        params.reportType = "MEMO_REPORT"
        when:
        controller.requestReport(dataSource,reportName,productSelection,productGroupSelection,null,null,dateRangeType)
        then:
        response.status == 200
    }
    void "test requestReport when report type is PBRER_SIGNAL_SUMMARY"(){
        setup:
        String dataSource ="pva"
        String reportName = "firstReport"
        String productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        String productGroupSelection = '[{"name":"testing_AS (3)","id":"3"}]'
        String dateRangeType = "CUMULATIVE"
        params.reportType = "PBRER_SIGNAL_SUMMARY"
        when:
        controller.requestReport(dataSource,reportName,productSelection,productGroupSelection,null,null,dateRangeType)
        then:
        response.status == 200
    }
    void "test requestReport when report type is SIGNALS_BY_STATE"(){
        setup:
        String dataSource ="pva"
        String reportName = "firstReport"
        String productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        String productGroupSelection = '[{"name":"testing_AS (3)","id":"3"}]'
        String dateRangeType = "CUMULATIVE"
        params.reportType = "SIGNALS_BY_STATE"
        when:
        controller.requestReport(dataSource,reportName,productSelection,productGroupSelection,null,null,dateRangeType)
        then:
        response.status == 200
    }
    void "test requestReport when report type is SIGNAL_PRODUCT_ACTIONS"(){
        setup:
        String dataSource ="pva"
        String reportName = "firstReport"
        String productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        String productGroupSelection = '[{"name":"testing_AS (3)","id":"3"}]'
        String dateRangeType = "CUMULATIVE"
        params.reportType = "SIGNAL_PRODUCT_ACTIONS"
        when:
        controller.requestReport(dataSource,reportName,productSelection,productGroupSelection,null,null,dateRangeType)
        then:
        response.status == 200
    }
    void "test requestReport when report type is SIGNAL_SUMMARY_REPORT"(){
        setup:
        String dataSource ="pva"
        String reportName = "firstReport"
        String productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        String productGroupSelection = '[{"name":"testing_AS (3)","id":"3"}]'
        String dateRangeType = "CUMULATIVE"
        params.reportType = "SIGNAL_SUMMARY_REPORT"
        when:
        controller.requestReport(dataSource,reportName,productSelection,productGroupSelection,null,null,dateRangeType)
        then:
        response.status == 200
    }


}