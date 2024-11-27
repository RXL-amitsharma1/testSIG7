package unit.com.rxlogix.reactorService

import com.rxlogix.AlertService
import com.rxlogix.SingleCaseAlertService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.reactorService.ConsumerService
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by tushar on 30/04/19.
 */

class ConsumerServiceSpec extends Specification implements ServiceUnitTest<ConsumerService> {
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    User user
    Disposition disposition

    Map map
    def setup(){

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true , abbreviation:"D")
        disposition.save(validate: false)

        Group wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultDisposition: disposition, defaultSignalDisposition: disposition, autoRouteDisposition: disposition, justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"

        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(validate: false)

        alertConfiguration = new Configuration(id: 1L, assignedTo: user, productSelection : "[TestProduct]", name: "test",
                owner: user, createdBy: user.username, modifiedBy: user.username, priority: new Priority(value: "High"))
        alertConfiguration.save(validate: false)

        executedConfiguration = new ExecutedConfiguration(id:2L,name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(validate: false)

        def mockedAlertService = Mock(AlertService)
        service.alertService = mockedAlertService
        map = [:]
    }

    @Ignore
    def "PersistActivityAndPEHistory"() {
        when:
        service.persistActivityAndPEHistory(map)

        then:
        1 * service.alertService.persistActivityAndPEHistory(_)
    }

    def "PersistActivityAndCaseHistories"() {
        when:
        service.persistActivityAndCaseHistories(map)

        then:
        1 * service.alertService.persistActivityAndCaseHistories(_)
    }

    def "PersistActivityAndEVDASHistories"() {
        when:
        service.persistActivityAndEVDASHistories(map)

        then:
        1 * service.alertService.persistActivityAndEvdasHistory(_)
    }

    @Ignore
    def "PersistActivity"() {
        when:
        service.persistActivity([])

        then:
        1 * service.alertService.persistActivity(_)
    }

    @Ignore
    def "generateCaseSeriesData when data is blank"() {
        given:
        def singleCaseAlertServiceMocked = Mock(SingleCaseAlertService)
        service.singleCaseAlertService = singleCaseAlertServiceMocked
        map = [newCaseSeriesConfig: null, newCaseSeriesExConfig: null, caseInfo: []]
        when:
        def result = service.generateCaseSeriesData(map)

        then:
        result == null
    }

    @Ignore
    def "generateCaseSeriesData when caseInfo list is blank"() {
        given:
        def singleCaseAlertServiceMocked = Mock(SingleCaseAlertService)
        service.singleCaseAlertService = singleCaseAlertServiceMocked
        map = [newCaseSeriesConfig: alertConfiguration, newCaseSeriesExConfig: executedConfiguration, caseInfo: []]
        when:
        def result = service.generateCaseSeriesData(map)

        then:
        result == null
    }
}
