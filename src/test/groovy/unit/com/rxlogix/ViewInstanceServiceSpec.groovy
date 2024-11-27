package unit.com.rxlogix

import com.rxlogix.UserService
import com.rxlogix.ViewInstanceService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.DefaultViewMapping
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.UserPinConfiguration
import com.rxlogix.signal.UserViewOrder
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */

class ViewInstanceServiceSpec extends HibernateSpec implements ServiceUnitTest<ViewInstanceService> {

    ViewInstance viewInstance_a
    ViewInstance viewInstance_b
    ViewInstance viewInstance_c
    ViewInstance defaultViewInstance
    ViewInstance systemDefaultView
    ViewInstance systemDefaultViewAggregate
    UserViewOrder userViewOrder1
    UserViewOrder userViewOrder2

    Group wfGroup
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    User userObj
    User userObj_a
    DefaultViewMapping defaultViewMapping

    List<Class> getDomainClasses() { [User, ViewInstance, DefaultViewMapping, UserViewOrder, UserPinConfiguration, UserService] }


    def setup() {
        service.transactionManager = transactionManager

        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush:true)

        userObj = new User()
        userObj.addToGroups(wfGroup)
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.preference.createdBy = "createdBy"
        userObj.preference.modifiedBy = "modifiedBy"
        userObj.preference.locale = new Locale("en")
        userObj.preference.isEmailEnabled = false
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj.groups = []
        userObj.save(validate: false)

        userObj_a = new User()
        userObj_a.username = 'username_a'
        userObj_a.createdBy = 'createdBy'
        userObj_a.modifiedBy = 'modifiedBy'
        userObj_a.preference.createdBy = "createdBy"
        userObj_a.preference.modifiedBy = "modifiedBy"
        userObj_a.preference.locale = new Locale("en")
        userObj_a.preference.isEmailEnabled = false
        userObj_a.metaClass.getFullName = { "Fake Name" }
        userObj_a.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj_a.groups = []
        userObj_a.save(validate: false)


        String columnSeq = """
                {"1":{"containerView":1,"label":"Tags","name":"alertTags","listOrder":0,"seq":6},"2":{"containerView":1,"label":"Receipt Date","name":"caseInitReceiptDate","listOrder":1,"seq":7},"3":{"containerView":1,"label":"Product Name","name":"productName","listOrder":2,"seq":8},"4":{"containerView":1,"label":"PT","name":"pt","listOrder":3,"seq":9},"5":{"containerView":1,"label":"Listedness","name":"listedness","listOrder":4,"seq":10},"6":{"containerView":1,"label":"Outcome","name":"outcome","listOrder":5,"seq":11},"7":{"containerView":1,"label":"Signal / Topic","name":"signalsAndTopics","listOrder":6,"seq":12},
                "8":{"containerView":1,"label":"Disposition","name":"currentDisposition","listOrder":7,"seq":13},"9":{"containerView":1,"label":"Current Disposition","name":"disposition","listOrder":8,"seq":14},"10":{"containerView":1,"label":"Assigned To","name":"assignedToUser","listOrder":9,"seq":15},"11":{"containerView":1,"label":"Due In","name":"dueDate","listOrder":10,"seq":16},"12":{"containerView":3,"label":"Suspect Products","name":"suspProd","listOrder":9999,"seq":17},"13":{"containerView":3,"label":"Con Med","name":"conComit","listOrder":9999,"seq":18},
                "14":{"containerView":3,"label":"PT List","name":"masterPrefTermAll","listOrder":9999,"seq":19},"15":{"containerView":1,"label":"Serious","name":"serious","listOrder":14,"seq":20},"16":{"containerView":3,"label":"Report Type","name":"caseReportType","listOrder":9999,"seq":21},"17":{"containerView":3,"label":"HCP","name":"reportersHcpFlag","listOrder":9999,"seq":22},"18":{"containerView":3,"label":"Country","name":"country","listOrder":9999,"seq":23},"19":{"containerView":3,"label":"Age Group","name":"age","listOrder":9999,"seq":24},"20":{"containerView":3,"label":"Gender","name":"gender","listOrder":9999,"seq":25},
                "21":{"containerView":3,"label":"Positive Rechallenge","name":"rechallenge","listOrder":9999,"seq":26},"22":{"containerView":3,"label":"Locked Date","name":"lockedDate","listOrder":9999,"seq":27},"23":{"containerView":3,"label":"Death","name":"death","listOrder":9999,"seq":28},"24":{"containerView":3,"label":"Medication Error PTs","name":"medErrorsPt","listOrder":9999,"seq":37},"25":{"containerView":3,"label":"Age","name":"patientAge","listOrder":9999,"seq":29},"26":{"containerView":3,"label":"Case Type","name":"caseType","listOrder":9999,"seq":31},
                "27":{"containerView":3,"label":"Completeness Score","name":"completenessScore","listOrder":9999,"seq":32},"28":{"containerView":3,"label":"Primary IND#","name":"indNumber","listOrder":9999,"seq":33},"29":{"containerView":3,"label":"Application#","name":"appTypeAndNum","listOrder":9999,"seq":34},"30":{"containerView":3,"label":"Compounding Flag","name":"compoundingFlag","listOrder":9999,"seq":35},"31":{"containerView":3,"label":"Indications","name":"indications","listOrder":9999,"seq":30},"32":{"containerView":3,"label":"Medication Error PT Count","name":"medErrorPtCount","listOrder":9999,"seq":38}}
           """
        viewInstance_a = new ViewInstance(id: 1, name: "vi_a", alertType: "Single Case Alert", user: userObj, defaultValue: true, columnSeq: columnSeq)
        viewInstance_a.save(flush: true)
        viewInstance_b = new ViewInstance(id: 2, name: "vi_b", alertType: "Single Case Alert", user: userObj, defaultValue: false, columnSeq: columnSeq)
        viewInstance_b.save(flush: true)
        viewInstance_c = new ViewInstance(id: 3, name: "vi_c", alertType: "Single Case Alert", user: userObj_a, defaultValue: false, columnSeq: columnSeq)
        viewInstance_c.save(flush: true)
        systemDefaultView = new ViewInstance(name: "System View", alertType: "Single Case Alert", user: null, defaultValue: false, columnSeq: columnSeq)
        systemDefaultView.save(flush: true)
        systemDefaultViewAggregate = new ViewInstance(name: "System View", alertType: "Aggregate Case Alert", user: null, defaultValue: false, columnSeq: columnSeq)
        systemDefaultViewAggregate.save(flush: true)
        defaultViewInstance = viewInstance_a
        defaultViewInstance.save()
        defaultViewMapping = new DefaultViewMapping(userObj, 'Single Case Alert', defaultViewInstance)
        defaultViewMapping.save()

        userViewOrder1 = new UserViewOrder()
        userViewOrder1.user = userObj
        userViewOrder1.viewInstance = viewInstance_a
        userViewOrder1.viewOrder = 1
        userViewOrder1.save()

        userViewOrder2 = new UserViewOrder()
        userViewOrder2.user = userObj
        userViewOrder2.viewInstance = viewInstance_b
        userViewOrder2.viewOrder = 2
        userViewOrder2.save()

        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername("_") >> {
            return userObj
        }
        mockUserService.getCurrentUserId() >> {
            return userObj.id
        }
        mockUserService.getUser() >> {
            return userObj
        }
        service.userService = mockUserService

    }

    def cleanup() {
    }


    void "test fetchViewInstanceList"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        List viewsList = [[id:viewInstance_a.id],
                          [id:viewInstance_b.id],
                          [id:systemDefaultView.id]]
        when:
        def result = service.fetchViewInstanceList(viewsList, userObj, viewInstance_a.id, "Single Case Alert", systemDefaultView.id)

        then:
        result.size() == 4 //returns 2 view instances with current user
    }


    void "test for fetchSelectedViewInstance"() {
        setup:
        def viewId = viewInstance_b.id //send specific viewId

        when:
        ViewInstance vi_result = service.fetchSelectedViewInstance("Single Case Alert", viewId)

        then:
        vi_result.name == "vi_b" //returns view instance with given id
    }


    void "test for fetchSelectedViewInstance return default"() {
        setup:
        def viewId = null //null id

        when:
        ViewInstance vi_result = service.fetchSelectedViewInstance("Single Case Alert", viewId)

        then:
        vi_result.name == "System View" //returns default view instance
    }

    void "test for populateDynamicSubGroups()"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        cacheService.getSubGroupMap() >> ["AGE_GROUP": [1: 'Adolescent', 2: 'Adult', 3: 'Child', 4: 'Elderly', 5: 'Foetus'], "GENDER": [6: 'AddedNew', 7: 'Confident', 8: 'Female', 9: 'MALE_NEW',]]


        when:
        Map populateMap = service.populateDynamicSubGroup([:], 0, 8, "Aggregate Case Alert")

        then:
        populateMap.counter == 27
    }

    void "test for populateDynamicColumnsPrev()"() {
        setup:

        when:
        Map populateMap = service.populateDynamicColumnsPrev("Male", 1, 2, [:], 0, 9)

        then:
        populateMap.counter == 3
    }

    void "test fetchViewInstanceList with defaultViewId null"() {
        setup:

        when:
        def result = service.fetchViewInstanceList([],userObj, viewInstance_a.id, "Single Case Alert", null)

        then:
        thrown Exception
    }

    void "test fetchViewInstanceList with view list but defaultViewId is null"() {
        setup:
        List viewsList = [[id:viewInstance_a.id],
                          [id:viewInstance_b.id],
                          [id:systemDefaultView.id]]
        when:
        def result = service.fetchViewInstanceList(viewsList, userObj, viewInstance_a.id, "Single Case Alert", null)

        then:
        result.size() == 3
    }

    void "test fetchViewInstanceList with view list but defaultViewId is not null"() {
        setup:
        List viewsList = [[id:viewInstance_a.id],
                          [id:viewInstance_b.id],
                          [id:systemDefaultView.id]]
        when:
        def result = service.fetchViewInstanceList(viewsList, userObj, viewInstance_a.id, "Single Case Alert", defaultViewInstance.id)

        then:
        result.size() == 3
    }

    void "test fetchViewInstanceList with where user is owner of the view"() {
        setup:
        List viewsList = [[id:viewInstance_a.id],
                          [id:viewInstance_b.id],
                          [id:systemDefaultView.id]]
        when:
        def result = service.fetchViewInstanceList(viewsList, userObj, viewInstance_a.id, "Single Case Alert", defaultViewInstance.id)

        then:
        result.size() == 3
    }

    void "test for fetchSelectedViewInstance if viewId is passed"() {
        setup:
        def viewId = viewInstance_b.id //send specific viewId
        when:
        ViewInstance vi_result = service.fetchSelectedViewInstance("Single Case Alert", viewId)

        then:
        vi_result.name == "vi_b" //returns view instance with given id
    }

    void "test for fetchSelectedViewInstance if viewId is not passed"() {
        setup:
        Long viewId = null
        Long defaultViewInstanceId = defaultViewInstance.id

        when:
        ViewInstance vi_result = service.fetchSelectedViewInstance("Single Case Alert", viewId)

        then:
        vi_result.name == "System View" //returns view instance with default view Id
    }

    void "test for fetchSelectedViewInstance if viewId is not passed and defaultView doesn't exist"() {
        setup:
        Long viewId = null
        Long defaultViewInstanceId = defaultViewInstance.id
        when:
        ViewInstance vi_result = service.fetchSelectedViewInstance("Aggregate Case Alert", viewId)

        then:
        vi_result.name == "System View" //System View is returned if viewId is null and defaultView doesn't exists
    }

    @Ignore
    void "test fetchViewsListAndSelectedViewMap"() {
        setup:
        Long viewId = 1
        def myCriteria = {
            return ViewInstance.findAllByAlertType("Single Case Alert")
        }

        ViewInstance.metaClass.static.withCriteria = { myCriteria }

        when:
        List result = service.fetchViewsListAndSelectedViewMap('Single Case Alert', viewId)

        then:
        result.size() == 4
    }

    void "test updateOrder"() {
        setup:
        Map params = [:]
        params.updatedViewsOrder = '[{"id":' + viewInstance_a.id + ',"order":"2"},{"id":' + viewInstance_b.id + ',"order":"1"}]'

        when:
        service.updateOrder(params)

        then:
        userViewOrder1.viewOrder == 2
    }

    void "test savePinnedConfAlerts"() {

        when:
        service.savePinnedConfAlerts("disposition" , true)

        then:
        UserPinConfiguration.list().size() == 1
    }

    void "test savePinnedConfAlerts when unpinning"() {
        setup:
        UserPinConfiguration userPinConfiguration = new UserPinConfiguration()
        userPinConfiguration.user = userObj
        userPinConfiguration.isPinned = true
        userPinConfiguration.fieldCode = "disposition"
        userPinConfiguration.save(flush:true)

        when:
        service.savePinnedConfAlerts("disposition" , false)

        then:
        UserPinConfiguration.list().size() == 0
    }
}
