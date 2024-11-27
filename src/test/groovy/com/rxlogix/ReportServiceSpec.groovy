package com.rxlogix

import com.rxlogix.enums.GroupType
import com.rxlogix.config.Disposition
import com.rxlogix.signal.ReportHistory
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.testing.services.ServiceUnitTest
import spock.lang.*

/**
 * Unit tests for the ReportService.
 */
class ReportServiceSpec extends Specification implements ServiceUnitTest<ReportService> {
    @Shared
    ValidatedSignal validatedSignal

    @Shared
    Disposition disposition

    @Shared
    Disposition defaultSignalDisposition

    @Shared
    Disposition autoRouteDisposition

    @Shared
    Disposition defaultQualiDisposition

    @Shared
    Disposition defaultQuantDisposition

    @Shared
    Disposition defaultEvdasDisposition

    @Shared
    Disposition defaultAdhocDisposition

    @Shared
    Disposition defaultLitDisposition

    @Shared
    Disposition defaultDisposition

    @Shared
    Group group

    @Shared
    ReportHistory reportHistory

    @Shared
    User user

    /**
     * Setup method to initialize shared objects before each test.
     */
    def setup() {
        // Prepare the mock dispositions
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        defaultLitDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        defaultSignalDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        defaultQualiDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        defaultQuantDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        defaultEvdasDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        defaultAdhocDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        [autoRouteDisposition, defaultAdhocDisposition, defaultEvdasDisposition, defaultLitDisposition, defaultSignalDisposition, autoRouteDisposition, defaultQualiDisposition, defaultQuantDisposition].each {
            it.save(failOnError: true)
        }

        // Prepare the mock Group
        group = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultDisposition: defaultDisposition,
                defaultSignalDisposition: disposition, autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition",
                forceJustification: true, defaultQualiDisposition: disposition, defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition, defaultEvdasDisposition: disposition, defaultLitDisposition: disposition,
                createdBy: "ujjwal", modifiedBy: "ujjwal")
        group.save(validate: false)

        Group wfGroup = new Group(name: "Default", createdBy: "ujjwal", modifiedBy: "ujjwal", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition, defaultQuantDisposition: disposition, defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition, defaultLitDisposition: disposition, defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        // Prepare the mock user
        user = new User(id: 1L, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(group)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(validate: false)

        // Prepare the mock report history
        reportHistory = new ReportHistory(
                reportName: "reportName",
                reportType: 'Memo Reports',
                dateCreated: new Date(),
                startDate: new Date(),
                endDate: new Date() + 10,
                productName: "APREMILAST",
                dataSource: "pva",
                updatedBy: user
        )
        reportHistory.save(flush: true, failOnError: true)

        // Prepare the mock validated signal
        validatedSignal = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date() + 10,
                assignmentType: 'USER',
                disposition: disposition,
                productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]',
                startDate: new Date(),
                detectedDate: new Date(),
                id: 1,
                genericComment: "Test notes"
        )
        validatedSignal.save(flush: true, validate: false)

        // Mocking services
        service.userService = [getUser: { return user }]
        service.productBasedSecurityService = [allAllowedProductForUser: { User user -> }]
    }

    /**
     * Cleanup method to release resources after each test.
     */
    def cleanup() {
    }

    /**
     * Test to verify fetchMatchingSignals for other reports.
     */
    @Ignore
    void "test fetchMatchingSignals for other report"() {
        setup:
        Map productSelectionMap = [productGroupSelectionIds: '3']
        Date startDate = new Date() - 3
        Date endDate = new Date() + 3
        when:
        List result = service.fetchMatchingSignals(startDate, endDate, productSelectionMap, false)
        then:
        result == [validatedSignal]
    }

    /**
     * Test to verify fetchMatchingSignals for PBRER reports.
     */
    @Ignore
    void "test fetchMatchingSignals for pbrer report"() {
        setup:
        Map productSelectionMap = [productGroupSelectionIds: '3']
        Date startDate = new Date() - 3
        Date endDate = new Date() + 3
        when:
        List result = service.fetchMatchingSignals(startDate, endDate, productSelectionMap, true)
        then:
        result == [validatedSignal]
    }

    /**
     * Test to verify getProductSelectionIds method.
     */
    void "test getProductSelectionIds"() {
        setup:
        Map productSelectionMap = [productSelection: [1, 2]]
        when:
        String result = service.getProductSelectionIds(productSelectionMap)
        then:
        result == "'1','2'"
    }

    /**
     * Test to verify getProductGroupSelectionIds method.
     */
    void "test getProductGroupSelectionIds"() {
        when:
        String result = service.getProductGroupSelectionIds('1,2')
        then:
        result == "'1','2'"
    }

    /**
     * Test to verify generatePBRERReport method.
     */
    void "test generatePBRERReport"() {
        setup:
        Date startDate = new Date() - 3
        Date endDate = new Date() + 3
        Map productSelectionMap = [productGroupSelectionIds: '3']
        when:
        def result = service.generatePBRERReport(startDate, endDate, productSelectionMap, 1L, 'pva')
        then:
        result != null
    }

    /**
     * Test to verify generateSignalStateReport method.
     */
    void "test generateSignalStateReport"() {
        setup:
        Date startDate = new Date() - 3
        Date endDate = new Date() + 3
        Map productSelectionMap = [productGroupSelectionIds: '3']
        when:
        def result = service.generateSignalStateReport(startDate, endDate, productSelectionMap, 1L, 'pva')
        then:
        result != null
    }

    /**
     * Test to verify generateProductActionsReport method.
     */
    void "test generateProductActionsReport"() {
        setup:
        Date startDate = new Date() - 3
        Date endDate = new Date() + 3
        Map productSelectionMap = [productGroupSelectionIds: '3']
        when:
        def result = service.generateProductActionsReport(startDate, endDate, productSelectionMap, 1L, 'pva')
        then:
        result != null
    }

    /**
     * Test to verify generateSignalSummaryReport method.
     */
    void "test generateSignalSummaryReport"() {
        setup:
        Date startDate = new Date() - 3
        Date endDate = new Date() + 3
        Map productSelectionMap = [productGroupSelectionIds: '3']
        when:
        def result = service.generateSignalSummaryReport(startDate, endDate, productSelectionMap, 1L, 'pva')
        then:
        result != null
    }

    /**
     * Test to verify isValidJson method with a valid JSON string.
     */
    def "test isValidJson with valid JSON string"() {
        given: "a valid JSON string"
        String validJson = '{"key": "value"}'

        when: "isValidJson is called"
        boolean result = service.isValidJson(validJson)

        then: "the result should be true"
        result == true
    }

    /**
     * Test to verify isValidJson method with an invalid JSON string.
     */
    def "test isValidJson with invalid JSON string"() {
        given: "an invalid JSON string"
        String invalidJson = '{"key": "value"'

        when: "isValidJson is called"
        boolean result = service.isValidJson(invalidJson)

        then: "the result should be false"
        result == false
    }

    /**
     * Test to verify isValidJson method with an empty string.
     */
    def "test isValidJson with empty string"() {
        given: "an empty JSON string"
        String emptyJson = ''

        when: "isValidJson is called"
        boolean result = service.isValidJson(emptyJson)

        then: "the result should be false"
        result == false
    }

    /**
     * Test to verify isValidJson method with a null string.
     */
    def "test isValidJson with null string"() {
        given: "a null JSON string"
        String nullJson = null

        when: "isValidJson is called"
        boolean result = service.isValidJson(nullJson)

        then: "the result should be false"
        result == false
    }
}
