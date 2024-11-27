package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

//@FreshRuntime
//@TestFor(LiteratureHistoryService)
@ConfineMetaClassChanges(LiteratureHistory)
//@TestMixin(DomainClassUnitTestMixin)
//@Mock([Priority, User, LiteratureHistory, LiteratureAlert, Group, ActivityType, LiteratureActivity, Disposition, LiteratureConfiguration, ExecutedLiteratureConfiguration])
@Ignore
class LiteratureHistoryServiceSpec extends Specification implements ServiceUnitTest<LiteratureHistoryService> {

    User user
    Group group
    Priority priority
    Disposition disposition
    LiteratureAlert literatureAlert
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    LiteratureHistory literatureHistory
    LiteratureConfiguration literatureConfiguration
    ExecutedLiteratureConfiguration executedLiteratureConfiguration

    void setup() {

        priority = new Priority([displayName    : "mockPriority",
                                 value          : "mockPriority",
                                 display        : true,
                                 defaultPriority: true,
                                 reviewPeriod   : 1])
        priority.save(failOnError: true)

        //Prepare the mock disposition
        disposition = new Disposition(value: "ValidatedSignal",
                displayName: "Validated Signal",
                validatedConfirmed: true,
                abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New",
                displayName: "New",
                validatedConfirmed: false,
                abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review",
                displayName: "Required Review",
                validatedConfirmed: false,
                abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        //Prepare the mock Group
        group = new Group(name: "Default",
                groupType: GroupType.WORKFLOW_GROUP,
                defaultDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition",
                forceJustification: true)
        group.save(validate: false)

        //Prepare the mock user
        user = new User(id: 1L, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(group)
        user.save(validate: false)

        //Prepare the mock configuration
        literatureConfiguration = new LiteratureConfiguration(
                id: 1,
                executing: true,
                name: "test",
                productSelection: "Test Product A",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user,
                isEnabled: true,
                workflowGroup: group
        )
        LiteratureDateRangeInformation literatureDateRangeInformation = new LiteratureDateRangeInformation()
        literatureDateRangeInformation.dateRangeStartAbsolute = new Date()
        literatureDateRangeInformation.dateRangeEndAbsolute = new Date()
        literatureDateRangeInformation.literatureConfiguration = literatureConfiguration
        literatureDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        literatureConfiguration.dateRangeInformation = literatureDateRangeInformation
        literatureConfiguration.save(failOnError: true)

        //Prepare the mock Executed Literature Configuration
        executedLiteratureConfiguration = new ExecutedLiteratureConfiguration(id: 1L, name: "test",
                owner: user, assignedTo: user,
                scheduleDateJSON: "{}",
                nextRunDate: new Date(),
                description: "test",
                dateCreated: new Date(),
                lastUpdated: new Date(),
                isPublic: true,
                isDeleted: true,
                isEnabled: true,
                productSelection: "['testproduct2']",
                eventSelection: "['rash']",
                searchString: "['test']",
                createdBy: user.username,
                modifiedBy: user.username,
                workflowGroup: group,
                assignedToGroup: group,
                totalExecutionTime: 0,
                isLatest: true,
                selectedDatasource: "pubmed",configId:literatureConfiguration.id)
        executedLiteratureConfiguration.save(failOnError: true)

        //Prepare the mock literatureHistory
        literatureHistory = new LiteratureHistory(id: 1L,
                currentDisposition: disposition,
                currentAssignedTo: user,
                currentAssignedToGroup: group,
                currentPriority: priority,
                change: 'Priority',
                createdBy: user.username, modifiedBy: user.username,
                dateCreated: new Date(),
                lastUpdated: new Date(),
                justification: "test",
                searchString: "Pubmed",
                articleId: 1L,
                litConfigId: 1L,
                litExecConfigId: 1L,
                tagName: "test",
                isLatest: true)
        literatureHistory.save(failOnError: true)
        literatureHistory.findAllByArticleIdAndLitExecConfigId(literatureHistory.id, literatureHistory.litExecConfigId) >> [literatureHistory]

        //Prepare Literature Alert to check justification update
        literatureAlert = new LiteratureAlert(
                litSearchConfig: literatureConfiguration,
                exLitSearchConfig: executedLiteratureConfiguration,
                dateCreated: new Date(),
                lastUpdated: new Date(),
                name: 'test',
                assignedTo: user,
                assignedToGroup: group,
                disposition: defaultDisposition,
                priority: priority,
                productSelection: 'test',
                eventSelection: 'test',
                searchString: 'Pubmed',
                articleId: 1L,
                articleTitle: 'test',
                articleAbstract: 'test',
                articleAuthors: 'test',
                publicationDate: new Date()
        )
        literatureAlert.save(failOnError: true)

        //prepare mock user service methods
        def userServiceSpy = [
                getUser                 : { this.user },
                getUserLitConfigurations: { ["test"] },
                getCurrentUserPreference: { this.user.preference },
                getCurrentUserId        : { 1L },
                setOwnershipAndModifier : { literatureHistory }
        ] as UserService
        service.userService = userServiceSpy
        service.CRUDService = [updateWithoutAuditLog: {  LiteratureHistory literatureHistory1-> }]
        service.CRUDService = [saveWithoutAuditLog: {  LiteratureHistory literatureHistory1-> }]

    }

    def cleanup() {
    }

    @ConfineMetaClassChanges(LiteratureHistory)
    void "test saveLiteratureArticleHistory with change as Disposition"() {
        given:
        def cacheService = Mock(CacheService)
        service.cacheService = cacheService
        literatureHistory.change = 'DISPOSITION'
        when:
        Map historyMap = service.getLiteratureHistoryMap(literatureHistory, literatureAlert, 'DISPOSITION', 'Update Disposition')
        service.saveLiteratureArticleHistory(historyMap)
        then:
        literatureHistory.change == 'DISPOSITION'
    }

    @ConfineMetaClassChanges(LiteratureHistory)
    void "test saveLiteratureArticleHistory with change as PRIORITY"() {
        given:
        def cacheService = Mock(CacheService)
        service.cacheService = cacheService
        literatureHistory.change = 'PRIORITY'
        when:
        Map historyMap = service.getLiteratureHistoryMap(literatureHistory, literatureAlert, 'PRIORITY', 'Update Disposition')
        service.saveLiteratureArticleHistory(historyMap)
        then:
        literatureHistory.change == 'PRIORITY'
    }

    @ConfineMetaClassChanges(LiteratureHistory)
    void "test saveLiteratureArticleHistory with change as ASSIGNED_TO"() {
        given:
        def cacheService = Mock(CacheService)
        service.cacheService = cacheService
        literatureHistory.change = 'ASSIGNED_TO'
        when:
        Map historyMap = service.getLiteratureHistoryMap(literatureHistory, literatureAlert, 'ASSIGNED_TO', 'Update Disposition')
        service.saveLiteratureArticleHistory(historyMap)
        then:
        literatureHistory.change == 'ASSIGNED_TO'
    }

    @ConfineMetaClassChanges(LiteratureHistory)
    void "test saveLiteratureArticleHistory with change as ALERT_TAGS"() {
        given:
        def cacheService = Mock(CacheService)
        service.cacheService = cacheService
        literatureHistory.change = 'ALERT_TAGS'
        when:
        Map historyMap = service.getLiteratureHistoryMap(literatureHistory, literatureAlert, 'ALERT_TAGS', 'Update Disposition')
        service.saveLiteratureArticleHistory(historyMap)
        then:
        literatureHistory.change == 'ALERT_TAGS'
    }
}