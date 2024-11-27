package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.ActionConfiguration
import com.rxlogix.config.ActionTemplate
import com.rxlogix.config.ActionType
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestMixin(GrailsUnitTestMixin)
//@TestFor(ActionTemplateService)
//@Mock([CacheService, User, ActionTemplate, ActionType, Disposition, Group, Preference])
class ActionTemplateServiceSpec extends Specification implements ServiceUnitTest<ActionTemplateService> {

    User admin
    ActionTemplate actionTemplate
    ActionType actionType
    ActionConfiguration actionConfiguration

    def setup() {

        Disposition disposition1 = new Disposition(value: "ValidatedSignal2", displayName: "ValidatedSignal2", closed: false,
                validatedConfirmed: false,abbreviation: "C")
        disposition1.save(failOnError: true)

        Group wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition1)
        wfGroup.save(flush:true,failOnError:true)

        Preference preference=new Preference(locale:new Locale('en'),createdBy: 'admin',modifiedBy: 'admin')
        preference.save(validate:false)
        admin = new User(username: 'admin', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Admin User',
                groups: [wfGroup],preference:preference)
        admin.save(flush:true,failOnError:true)

        String actionTemplateMap = """
           {
                "actionConfig": "test",
                "actionConfigId":"1",
                "actionType": "test",
                "actionTypeId": "1",
                "assignedTo": "null",
                "assignedToId": "null",
                "dueIn": "3",
                "details": "test",
                "comments": "test"
           }
        """
        String actionTemplateMap2 = """
           {
                "actionConfig": "test",
                "actionConfigId":"1",
                "actionType": "test",
                "actionTypeId": "1",
                "assignedTo": "null",
                "assignedToId": "null",
                "details": "test",
                "comments": "test"
           }
        """
        actionTemplate = new ActionTemplate(name: 'test', actionJson: actionTemplateMap, id: 1L, guestAttendeeEmail: "test@test.com")
        actionTemplate.save(validate:false)
        ActionTemplate actionTemplate2 = new ActionTemplate(name: 'test', actionJson: actionTemplateMap2, id: 1L, guestAttendeeEmail: "test@test.com")
        actionTemplate2.save(validate:false)
        actionType = new ActionType(description: 'test', displayName: 'test', value: 'test')
        actionType.save(validate:false)
        actionConfiguration = new ActionConfiguration(displayName: 'test', value: 'test')
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getActionTemplateCache(_) >> actionTemplate
        mockCacheService.getUserByUserName(_) >> null
        mockCacheService.getActionConfigurationCache(_) >> actionConfiguration
        mockCacheService.getActionTypeCache(_) >> actionType
        service.cacheService = mockCacheService
    }

    def cleanup() {
    }

    void "test addActionsFromTemplate() for guestAttendeeEmail"() {
        given:
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        when:
        aggregateCaseAlert = service.addActionsFromTemplate(1L, "Aggregate Case Alert", aggregateCaseAlert, true)
        then:
        aggregateCaseAlert.action.guestAttendeeEmail == ["test@test.com"]
        aggregateCaseAlert.action.assignedTo==[null]
        aggregateCaseAlert.action.owner==[null]
        aggregateCaseAlert.action.dueDate!=[null]
    }
    void "test addActionsFromTemplate when actionTemplateId is not given"(){
        given:
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        when:
        AggregateCaseAlert result = service.addActionsFromTemplate(null, "Aggregate Case Alert", aggregateCaseAlert, true)
        then:
        result.action == []
    }
    void "test addActionsFromTemplate when cacheService.getActionTemplateCache does not return actionTemplate"(){
        setup:
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getActionTemplateCache(_) >> null
        mockCacheService.getUserByUserName(_) >> null
        mockCacheService.getActionConfigurationCache(_) >> actionConfiguration
        mockCacheService.getActionTypeCache(_) >> actionType
        service.cacheService = mockCacheService
        when:
        AggregateCaseAlert result = service.addActionsFromTemplate(1L, "Aggregate Case Alert", aggregateCaseAlert, true)
        then:
        result.action==[]
    }
    void "test addActionsFromTemplate when cacheService.getUserByUserName returns an user "(){
        setup:
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getActionTemplateCache(_) >> actionTemplate
        mockCacheService.getUserByUserName(_) >> admin
        mockCacheService.getActionConfigurationCache(_) >> actionConfiguration
        mockCacheService.getActionTypeCache(_) >> actionType
        service.cacheService = mockCacheService
        when:
        AggregateCaseAlert result = service.addActionsFromTemplate(1L, "Aggregate Case Alert", aggregateCaseAlert, true)
        then:
        result.action.guestAttendeeEmail != "test@test.com"
        result.action.assignedTo==[admin]
        result.action.owner==[admin]
        result.action.dueDate!=[null]
    }
    void "test addActionsFromTemplate when dueIn is null in actionTemplate.actionJson "(){
        setup:
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getActionTemplateCache(_) >> ActionTemplate.get(2)
        mockCacheService.getUserByUserName(_) >> null
        mockCacheService.getActionConfigurationCache(_) >> actionConfiguration
        mockCacheService.getActionTypeCache(_) >> actionType
        service.cacheService = mockCacheService
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        when:
        AggregateCaseAlert result = service.addActionsFromTemplate(2L, "Aggregate Case Alert", aggregateCaseAlert, true)
        then:
        result.action.guestAttendeeEmail == ["test@test.com"]
        result.action.assignedTo==[null]
        result.action.owner==[null]
        result.action.dueDate==[null]
    }
}