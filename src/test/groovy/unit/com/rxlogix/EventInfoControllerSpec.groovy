package unit.com.rxlogix

import com.rxlogix.AlertService
import com.rxlogix.Constants
import com.rxlogix.EventInfoController
import com.rxlogix.EventInfoService
import com.rxlogix.PriorityService
import com.rxlogix.SafetyLeadSecurityService
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.WorkflowRuleService
import com.rxlogix.WorkflowService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.Justification
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@Ignore
class EventInfoControllerSpec extends Specification implements ControllerUnitTest<EventInfoController> {


    User user
    ExecutedConfiguration executedConfiguration
    Configuration configuration
    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    AggregateCaseAlert aca

    def setup() {

        disposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: true, abbreviation: "New")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        Group wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(validate: false)

        configuration = new Configuration(assignedTo: user, productSelection: "[TestProduct]", name: "test",
                owner: user, createdBy: user.username, modifiedBy: user.username, priority: new Priority(value: "High"))
        configuration.save(failOnError: true)


        executedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['ASPIRIN ALUMINUM']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,configId: configuration.id,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)

        executedConfiguration.save(failOnError: true)

        def today = new Date()

        mockDomain(User, [
                [username: 'admin', password: 'admin', fullName: "Peter Fletcher", createdBy: 'test', modifiedBy: 'test', groups: [wfGroup]]
        ])

        assert User.count == 2

        mockDomain(Priority, [
                [value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1]
        ])

        def priority = Priority.findByValue("High")
        assert priority != null

        mockDomain(Disposition, [
                [validatedConfirmed: false, displayName: "Validated Signal1", value: 'ValidatedSignal1', abbreviation: 'VO1'],
                [validatedConfirmed: true, displayName: "Validated Signal2", value: 'ValidatedSignal2', abbreviation: 'VO2']

        ])

        assert (Disposition.count == 5)

        def configSCA = new Configuration(priority: priority, owner: User.findByUsername('admin'), productSelection: "Test Product A", assignedTo: User.findByUsername('admin'), type: 'Single Case Alert', isPublic: false, name: 'SAE - Clinical Reconciliation Death Case1111', description: 'Config to identify SAE - Clinical Reconciliation death cases', isEnabled: false, createdBy: 'test', modifiedBy: 'test')
        configSCA.save(failOnError: true)
        def configACA = new Configuration(priority: priority, owner: User.findByUsername('admin'), productSelection: "Test Product A", assignedTo: User.findByUsername('admin'), type: 'Aggregate Case Alert', isPublic: false, name: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', isEnabled: false, createdBy: 'test', modifiedBy: 'test')
        configACA.save(failOnError: true)
        assert Configuration.count == 3

        aca = new AggregateCaseAlert(
                id: 2345,
                alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product d",
                productId: 12321312,
                soc: "BODY_SYS1",
                pt: 'Rash',
                ptCode: 1421,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                newSponCount: 1,
                cumSponCount: 1,
                newSeriousCount: 1,
                cumSeriousCount: 1,
                newFatalCount: 1,
                cumFatalCount: 1,
                prrValue: "1",
                prrLCI: "1",
                prrUCI: "1",
                prrStr: "1",
                prrStrLCI: "1",
                prrStrUCI: "1",
                prrMh: "1",
                rorValue: "1",
                rorLCI: "1",
                rorUCI: "1",
                rorStr: "1",
                rorStrLCI: "1",
                rorStrUCI: "1",
                rorMh: "1",
                pecImpHigh: "1",
                pecImpLow: "1",
                createdBy: configuration.assignedTo.username,
                modifiedBy: configuration.assignedTo.username,
                dateCreated: executedConfiguration.dateCreated,
                lastUpdated: executedConfiguration.dateCreated,
                eb05: new Double(1),
                eb95: new Double(1),
                ebgm: new Double(2),
                dueDate: new Date(),
                periodStartDate: new Date(),
                periodEndDate: new Date(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "Yes",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test"

        )
        aca.save(failOnError: true)

        assert (AggregateCaseAlert.count == 1)


    }



    void "test listPreviousCounts()"() {
        setup:
        Long alertId = 1
        String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT

        when:
        controller.listPreviousCounts(alertId, alertType, false)

        then:
        response.status == 200
        response.json.aaData[0].cummFatal_pva == 1
        response.json.aaData[0].newFatal_pva == 1
        response.json.aaData[0].ror_pva == 1.0
        response.json.aaData[0].newCount_pva == 0
    }

    void "test showCharts()"() {
        setup:
        Long alertId = 1
        String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT

        when:
        controller.showCharts(alertId, alertType, false)

        then:
        response.status == 200
        response.json.cummFatal_pva[0] == 1
        response.json.newFatal_pva[0] == 1
        response.json.ror_pva[0] == 1
        response.json.newCount_pva[0] == 0
    }

}

