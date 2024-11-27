package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

//@FreshRuntime
//@TestFor(LiteratureHistoryController)
@ConfineMetaClassChanges(LiteratureHistory)
//@TestMixin(DomainClassUnitTestMixin)
//@Mock([Priority, User, LiteratureHistory, LiteratureAlert, Group, ActivityType, LiteratureActivity, Disposition,
//        LiteratureConfiguration, ExecutedLiteratureConfiguration, LiteratureHistoryService, LiteratureActivityService])
@Ignore
class LiteratureHistoryControllerSpec extends Specification implements ControllerUnitTest<LiteratureHistoryController> {

    User user
    Group group
    Priority priority
    Disposition disposition
    ActivityType activityType
    LiteratureActivity literatureActivity
    LiteratureAlert literatureAlert
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    LiteratureHistory literatureHistory
    LiteratureConfiguration literatureConfiguration
    LiteratureActivityService literatureActivityService
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
                configId:111,
                selectedDatasource: "pubmed")
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
                tagName: null,
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

        //Prepare Activity type mock the pass in LiteratureActivity
        activityType = new ActivityType(value: ActivityTypeValue.JustificationChange)
        activityType.save(failOnError: true)

        //Prepare literature Activity to create activity for justificaiton change
        literatureActivity = new LiteratureActivity(
                type: activityType,
                details: "test",
                timestamp: new DateTime().now(),
                justification: 'Update Disposition',
                productName: 'test',
                eventName: 'test',
                articleId: 1L,
                searchString: 'Pubmed',
                performedBy: user,
                assignedTo: user,
                assignedToGroup: group,
                executedConfiguration: executedLiteratureConfiguration
        )
        literatureActivity.save(failOnError: true)

        //prepare mock user service methods
        def userServiceSpy = [
                getUser                 : { this.user },
                getUserLitConfigurations: { ["test"] },
                getCurrentUserPreference: { this.user.preference },
                getCurrentUserId        : { 1L }
        ] as UserService
        controller.literatureHistoryService.userService = userServiceSpy

    }

    def cleanup() {
    }

    @ConfineMetaClassChanges(LiteratureHistory)
    void "test listCaseHistory"() {
        given:
        List<Map> literatureHistoryList = []
        [literatureHistory].collect {
            literatureHistoryList.add(it.toDto())
        }
        LiteratureHistory.metaClass.static.findAllByArticleIdAndLitConfigId = { Long articleId, Long execId ->
            return [literatureHistory]
        }
        when:
        controller.listCaseHistory(literatureHistory.id, literatureHistory.litConfigId)
        then:
        response.getJson()['articleId'] == [1]
        response.status == 200
    }

    void "test listCaseHistory is null"() {
        given:
        List<Map> literatureHistoryList = []
        [literatureHistory].collect {
            literatureHistoryList.add(it.toDto())
        }
        LiteratureHistory.metaClass.static.findAllByArticleIdAndLitConfigId = { Long articleId, Long execId ->
            return []
        }
        when:
        controller.listCaseHistory(literatureHistory.id, literatureHistory.litConfigId)
        then:
        response.getJson() == []
        response.status == 200
    }

    @ConfineMetaClassChanges(LiteratureHistory)
    void "test listArticleHistoryInOtherAlerts"() {
        given:
        List<Map> literatureHistoryList = []
        [literatureHistory].collect {
            literatureHistoryList.add(it.toDto())
        }
        LiteratureHistory.metaClass.static.findAllByArticleIdAndLitConfigIdNotEqual = { Long articleId, Long execId ->
            return [literatureHistory]
        }
        when:
        controller.listArticleHistoryInOtherAlerts(literatureHistory.id, literatureHistory.litConfigId)
        then:
        response.getJson()['articleId'] == [1]
        response.status == 200
    }

    @ConfineMetaClassChanges(LiteratureHistory)
    void "test listArticleHistoryInOtherAlerts is null"() {
        given:
        List<Map> literatureHistoryList = []
        [literatureHistory].collect {
            literatureHistoryList.add(it.toDto())
        }
        LiteratureHistory.metaClass.static.findAllByArticleIdAndLitConfigIdNotEqual = { Long articleId, Long execId ->
            return []
        }
        when:
        controller.listArticleHistoryInOtherAlerts(literatureHistory.id, literatureHistory.litConfigId)
        then:
        response.getJson() == []
        response.status == 200
    }

    @ConfineMetaClassChanges(ActivityType)
    void "test updateJustification"() {
        given:
        def service = Mock(CRUDService)
        controller.literatureHistoryService.CRUDService = service
        LiteratureAlert.metaClass.static.findByArticleIdAndExLitSearchConfig = { Long articleId, ExecutedLiteratureConfiguration executedLiteratureConfiguration1 ->
            return literatureAlert
        }
        ActivityType.metaClass.static.findByValue = { ActivityTypeValue value ->
            return activityType
        }
        when:
        controller.updateJustification(literatureHistory.id, 'Update Disposition')
        then:
        response.status == 200
    }

    @ConfineMetaClassChanges(ActivityType)
    void "test updateJustification with Exception"() {
        given:
        def service = Mock(CRUDService)
        controller.literatureHistoryService.CRUDService = service
        LiteratureAlert.metaClass.static.findByArticleIdAndExLitSearchConfig = { Long articleId, ExecutedLiteratureConfiguration executedLiteratureConfiguration1 ->
            return literatureAlert
        }
        ActivityType.metaClass.static.findByValue = { ActivityTypeValue value ->
            throw new Exception()
        }
        when:
        controller.updateJustification(literatureHistory.id, 'Update Disposition')
        then:
        response.status == 200

    }
}