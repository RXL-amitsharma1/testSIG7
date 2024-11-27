package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.user.User
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.PriorityDispositionConfig
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class LiteratureExecutionServiceSpec extends Specification implements ServiceUnitTest<LiteratureExecutionService> {
//class LiteratureExecutionServiceSpec extends HibernateSpec implements ServiceUnitTest<LiteratureExecutionService> {
    @Shared
    User user
    @Shared
    LiteratureConfiguration literatureConfiguration
    @Shared
    Group group, wfGroup
    @Shared
    Priority priority
    @Shared
    ExecutedLiteratureConfiguration executedLiteratureConfiguration
    @Shared
    Disposition defaultDisposition
    @Shared
    PriorityDispositionConfig priorityDispositionConfig
    @Shared
    Disposition disposition
    @Shared
    Disposition autoRouteDisposition
    @Shared
    ExecutedLiteratureConfiguration executedLiteratureConfiguration2
//    List<Class> getDomainClasses() {
//        [LiteratureConfiguration,Group, Priority, User, ExecutedLiteratureConfiguration, Disposition, PriorityDispositionConfig]
//    }

    def setup() {

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(failOnError: true)
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        priority = new Priority([displayName    : "mockPriority",
                                 value          : "mockPriority",
                                 display        : true,
                                 defaultPriority: true,
                                 reviewPeriod   : 1])
        priority.save(failOnError: true)
        priorityDispositionConfig = new PriorityDispositionConfig("priority": priority,
                "disposition": defaultDisposition, "dispositionOrder": 1, "reviewPeriod": 10).save(flush: true, failOnError: true)

        group = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultDisposition: defaultDisposition,
                defaultSignalDisposition: disposition, autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition",
                forceJustification: true, defaultQualiDisposition: disposition, defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition, defaultEvdasDisposition: disposition, defaultLitDisposition: disposition,
                createdBy: "ujjwal", modifiedBy: "ujjwal")
        group.save(validate: false)

        wfGroup = new Group(name: "Default",
                groupType: GroupType.WORKFLOW_GROUP,
                defaultDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                justificationText: "Update Disposition",
                forceJustification: true)
        wfGroup.save(validate: false)

        user = new User(id: 1L, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(group)
        user.addToGroups(wfGroup)
        user.save(validate: false)

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
                configId: 1,
                reviewDueDate: new Date(),
                selectedDatasource: "pubmed")
        executedLiteratureConfiguration.save(failOnError: true)

        executedLiteratureConfiguration2 = new ExecutedLiteratureConfiguration(id: 5, name: "test",
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
                isLatest: false,
                configId: 1,
                reviewDueDate: new Date(),
                selectedDatasource: "pubmed")
        executedLiteratureConfiguration2.save(failOnError: true)
    }

    def cleanup() {
    }

    void "test generateReviewDueDate"() {
        setup:
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getDispositionConfigsByPriority(_) >> {
            return [priorityDispositionConfig]
        }
        service.cacheService = mockCacheService
        when:
        service.generateReviewDueDate(literatureConfiguration, executedLiteratureConfiguration)
        then:
        executedLiteratureConfiguration.reviewDueDate != new Date()
    }

    def "test updateIsLatestForOldExecutedConfigurations"() {
        given:
        ExecutedLiteratureConfiguration.metaClass.'static'.findAllByConfigIdAndOwner = { int id, User owner ->
            return [executedLiteratureConfiguration, executedLiteratureConfiguration2]
        }
        when:
        service.updateIsLatestForOldExecutedConfigurations(literatureConfiguration, executedLiteratureConfiguration2.id)

        then:
        executedLiteratureConfiguration2.isLatest == true
    }
}
