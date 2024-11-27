package unit.com.rxlogix

import com.rxlogix.AdvancedFilterController
import com.rxlogix.AdvancedFilterService
import com.rxlogix.Constants
import com.rxlogix.UserService
import com.rxlogix.ViewInstanceService
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.enums.GroupType
import com.rxlogix.config.Disposition
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification


class AdvancedFilterControllerSpec extends Specification implements ControllerUnitTest<AdvancedFilterController> {
    AdvancedFilter advanceFilter

    User user
    UserService mockUserService
    ViewInstanceService mockViewInstanceService
    AdvancedFilterService advancedFilterService
    Group wfGroup
    Disposition disposition,autoRouteDisposition,defaultDisposition

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs",productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]')
        disposition.save(flush: true, failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(wfGroup);
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(flush: true,failOnError:true)
        advanceFilter = new AdvancedFilter()
        advanceFilter.alertType = "Single Case Alert"
        advanceFilter.criteria = "{ ->\n" +
                "criteriaConditions('listedness','EQUALS','true')\n" +
                "}"
        advanceFilter.createdBy = "fakeuser"
        advanceFilter.dateCreated = new Date()
        advanceFilter.description = "test advanced filter"
        advanceFilter.JSONQuery = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"listedness\",\"op\":\"EQUALS\",\"value\":\"true\"} ] }  ] } }"
        advanceFilter.lastUpdated = new Date()
        advanceFilter.modifiedBy = "fakeuser"
        advanceFilter.name = "fake filter 1"
        advanceFilter.user = user
        advanceFilter.shareWithUser = [user]
        advanceFilter.save(validate: false)

        mockUserService = Mock(UserService)

        mockUserService.getUser() >> {
            return user
        }
        controller.userService = mockUserService

        mockViewInstanceService = Mock(ViewInstanceService)
        mockViewInstanceService.isViewFilterSharingAllowed(_,_,_) >> {
            return false
        }
        mockUserService.bindSharedWithConfiguration(_,_,_)>>{

        }
        controller.viewInstanceService = mockViewInstanceService

        advancedFilterService = Mock(AdvancedFilterService)
        advancedFilterService.createAdvancedFilterCriteria(_)>>{
            return "{ ->\n" +
                    "criteriaConditionsForSubGroup('EBGM:Confident','EQUALS',\"153\")\n" +
                    "}"
        }
        advancedFilterService.getValidInvalidValues(_,_,_,_)>>{
            return ["validValues": [1,2],
                    "invalidValues" :[3,4]
            ]
        }
        advancedFilterService.getDuplicates(_) >> {
            List list = ["1"]
            return list.toSet()
        }
        controller.advancedFilterService = advancedFilterService
    }

    def cleanup() {
    }

    void "test save"(){
        setup:
        user.metaClass.isAdmin={false}
        controller.params.filterId = advanceFilter.id as Long
        controller.params.name = "fake filter 2"
        controller.params.alertType = "Single Case Alert"
        controller.params.createdBy = "fakeuser"
        controller.params.dateCreated = new Date()
        controller.params.description = "test advanced filter"
        controller.params.JSONQuery = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"listedness\",\"op\":\"EQUALS\",\"value\":\"true\"} ] }  ] } }"
        controller.params.lastUpdated = new Date()
        controller.params.modifiedBy = "fakeuser"
        controller.userService = [getUser: { return user }]
        controller.params.miningVariable = [:]

        when:
        controller.save()

        then:
        AdvancedFilter.countByName("fake filter 2") == 0
        response.json.status == true
        response.json.message == null
        response.json.data == [id:1, text:"fake filter 1(S)"]
    }

    void "test delete"(){
        setup:
        controller.params.id = AdvancedFilter.findByName("fake filter 1").getId()
        controller.userService = [getUser: { return userObj }]

        when:
        controller.delete()

        then:
        AdvancedFilter.countByName("fake filter 1") == 1
        response.json.status == true
    }
    void "test validateValue()"(){
        setup:
        controller.params.selectedField = "selectedField"
        controller.params.values = ""
        when:
        controller.validateValue()
        then:
        response.json.success == false
    }
    @Ignore
    void "test validateValue() success"(){
        setup:
        controller.params.selectedField = "selectedField"
        controller.params.values = "a;b;c"
        when:
        controller.validateValue()
        then:
        response.json.success == true
    }
    void "test fetchAjaxAdvancedFilterSearch for comments in single case alert"(){
        setup:
        controller.advancedFilterService=[getAjaxFilterData:{String term, int offset, int max, Long executedConfigId,
                                                             String field, def domainName, Map filterMap->[jsonData:[["id":"SYSTEM","text":"SYSTEM"],["id":"Blinded","text":"Blinded"],["id":"Redacted","text":"Redacted"]],
                                                                                                           possibleValuesListSize:3]}]
        String alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        Long executedConfigId = 0l
        String term = "com blinded redacted"
        int page =0
        int max = 30
        String field = Constants.AdvancedFilter.COMMENTS
        when:
        controller.fetchAjaxAdvancedFilterSearch(executedConfigId, term, page, max, field, alertType)
        then:
        response.json == [list:[["id":"SYSTEM","text":"SYSTEM"],["id":"Blinded","text":"Blinded"],["id":"Redacted","text":"Redacted"]], totalCount:3]
    }
    void "test fetchAjaxAdvancedFilterSearch for comments in aggregate alert"(){
        setup:
        controller.advancedFilterService=[getAjaxFilterData:{String term, int offset, int max, Long executedConfigId,
                                                             String field, def domainName, Map filterMap->[jsonData:[["id":"SYSTEM","text":"SYSTEM"],["id":"SYSTEM","text":"SYSTEM"]],
                                                                                                           possibleValuesListSize:2]}]
        String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        Long executedConfigId = 0l
        String term = "comm"
        int page =0
        int max = 30
        String field = Constants.AdvancedFilter.COMMENT
        when:
        controller.fetchAjaxAdvancedFilterSearch(executedConfigId, term, page, max, field, alertType)
        then:
        response.json == [list:[["id":"SYSTEM","text":"SYSTEM"],["id":"SYSTEM","text":"SYSTEM"]], totalCount:2]
    }
    void "test fetchAjaxAdvancedFilterSearch for comments in evdas alert"(){
        setup:
        controller.advancedFilterService=[getAjaxFilterData:{String term, int offset, int max, Long executedConfigId,
                                                             String field, def domainName, Map filterMap->[jsonData:[["id":"SYSTEM","text":"SYSTEM"],["id":"SYSTEM","text":"SYSTEM"]],
                                                                                                           possibleValuesListSize:2]}]
        String alertType = Constants.AlertConfigType.EVDAS_ALERT
        Long executedConfigId = 0l
        String term = "comm"
        int page =0
        int max = 30
        String field = Constants.AdvancedFilter.COMMENT
        when:
        controller.fetchAjaxAdvancedFilterSearch(executedConfigId, term, page, max, field, alertType)
        then:
        response.json == [list:[["id":"SYSTEM","text":"SYSTEM"],["id":"SYSTEM","text":"SYSTEM"]], totalCount:2]
    }
}
