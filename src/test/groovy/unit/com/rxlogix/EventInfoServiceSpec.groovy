package unit.com.rxlogix

import com.rxlogix.Constants
import com.rxlogix.EventInfoService
import com.rxlogix.UserService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.config.EvdasAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Ignore
class EventInfoServiceSpec extends Specification implements ServiceUnitTest<EventInfoService> {

    User user
    String alertType
    ExecutedConfiguration executedConfiguration
    Configuration configuration
    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    AggregateCaseAlert alert

    def setup() {

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        Group wfGroup = new Group(name: "Default", createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush:true)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush:true)

        configuration = new Configuration(id: 1, assignedTo: user, productSelection: "[TestProduct]", name: "test",
                owner: user, createdBy: user.username, modifiedBy: user.username, priority: new Priority(value: "High"))
        configuration.save(failOnError: true)

        executedConfiguration = new ExecutedConfiguration(name: "test",
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
                assignedTo: user, configId: configuration.id,
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

        alert = new AggregateCaseAlert(
                id: 2345,
                alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
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
                format: "test",
        )
        alert.save(failOnError: true)
        assert (AggregateCaseAlert.count == 1)

        alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
    }


    @ConfineMetaClassChanges([EventInfoService])
    void "test getEventInfoMap"() {
        setup:
        Long alertId = 2345
        EventInfoService.metaClass.getAlertObject = {String type, Long alertID ->
            return alert
        }
        alert = mockDomain(AggregateCaseAlert)
        alert.getUserByUserId(_) >> 1L
        alert.alertConfiguration.groupBySmq(_) >> true

        when:
        def eventInfoMap = service.getEventInfoMap(alertId, alertType, false)

        then:
        eventInfoMap.size() == 4
        eventInfoMap.alertId == 2345
        eventInfoMap.dataKeys != null
        eventInfoMap.dataKeys.size() == 189
    }

    void "test getAlertDetailMap()"(){
        setup:
            alert = mockDomain(AggregateCaseAlert)
            alert.getUserByUserId(_) >> 1L
        when:
            def alertDetailMap = service.getAlertDetailMap(alert, alertType)

        then:
            alertDetailMap.size() == 0
    }


    void "test getAlertObject()"(){
        when:
            Long alertId = 1
            def alertObject = service.getAlertObject(alertType, alertId)

        then:
            alertObject.id == 1
            alertObject.pt == "Rash"
            alertObject.name == "test"
            alertObject.assignedTo == user
    }

    void "test getStandardAlertList()"(){
        setup:
            String productName = "ASPIRIN ALUMINIUM"

        when:
            def standardMap = service.getStandardAlertsList(productName)

        then:
            standardMap.size() == 3
            standardMap.pva == "asp_aluminium_q"
            standardMap.faers == "asp_aluminium_FAERS"
            standardMap.eudra == "aspirin_al_evdas"
    }

    void "test getStandardAlertList() When productName is Wrong"(){
        setup:
        String productName = "APREMILAST"

        when:
        def standardMap = service.getStandardAlertsList(productName)

        then:
        standardMap.size() == 0
    }

    void "test getDetailsMapForPVA()"(){
        setup:
            Map countData = [newFatal_pva: [], cummFatal_pva: [], prr_pva: [], ror_pva: [], newCount_pva: [], cummCount_pva: [], xAxisTitle: [], xAxisTitle_ev : [],
                             newEvpm_ev  : [], totalEvpm_ev: [], ime_ev: [], dme_ev: [], newFatal_ev: [], totalFatal_ev: [], newPaed_ev: [], totalPaed_ev: [],
                             sdrPaed_ev  : [], changes_ev: [], rorAll_ev: [], newCounts_faers: [], cummCounts_faers: [], eb05_faers: [], eb95_faers: []
            ]

            String alertName = "test"
            String productName = "Test Product A"
            String eventName = "Rash"

        when:
            Map resultMap = service.getDetailsMapForPVA(countData, alertName, productName, eventName)

        then:
            resultMap.newFatal_pva[0] == 1
            resultMap.cummFatal_pva[0] == 1
            resultMap.prr_pva[0] == 1.0
            resultMap.ror_pva[0] == 1.0
            resultMap.newCount_pva[0] == 0
            resultMap.cummCount_pva[0] == 0
            resultMap.sdrPaed_ev == []
            resultMap.cummCounts_faers == []
    }

    void "test getDetailsListForPVA()"(){
        setup:
            String alertName = "test"
            String productName = "Test Product A"
            String eventName = "Rash"

        when:
            List resultList = service.getDetailsListForPVA(alertName, productName, eventName)

        then:
            resultList[0].newFatal_pva == 1
            resultList[0].cummFatal_pva == 1
            resultList[0].prr_pva == 1.0
            resultList[0].ror_pva == 1.0
            resultList[0].newCount_pva == 0
            resultList[0].cummCount_pva == 0
            resultList.size() == 1
    }

    void "test countMapData()"(){
        when:
            Long alertId = 1
            List resultList = service.countMapData(alertId, alertType, false)

        then:
            resultList[0].newFatal_pva == 1
            resultList[0].dme_ev == "-"
            resultList[0].newCounts_faers == "-"
            resultList[0].newCount_pva == 0
    }
}
