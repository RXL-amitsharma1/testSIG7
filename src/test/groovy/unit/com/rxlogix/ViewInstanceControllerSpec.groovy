package unit.com.rxlogix

import com.rxlogix.UserService
import com.rxlogix.ViewInstanceController
import com.rxlogix.ViewInstanceService
import com.rxlogix.cache.CacheService
import com.rxlogix.signal.ClipboardCases
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class ViewInstanceControllerSpec extends Specification implements ControllerUnitTest<ViewInstanceController> {

    ViewInstance viewInstance
    ViewInstance viewInstance_prev
    User userObj
    ClipboardCases clipboardCases

    def setup() {
        userObj = new User()
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.preference.createdBy = "createdBy"
        userObj.preference.modifiedBy = "modifiedBy"
        userObj.preference.locale = new Locale("en")
        userObj.preference.isEmailEnabled = false
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj.metaClass.isAdmin = { true }
        userObj.groups = []
        userObj.save(validate: false)

        String columnSeq = """
                {"1":{"containerView":1,"label":"Tags","name":"alertTags","listOrder":0,"seq":6},"2":{"containerView":1,"label":"Receipt Date","name":"caseInitReceiptDate","listOrder":1,"seq":7},"3":{"containerView":1,"label":"Product Name","name":"productName","listOrder":2,"seq":8},"4":{"containerView":1,"label":"PT","name":"pt","listOrder":3,"seq":9},"5":{"containerView":1,"label":"Listedness","name":"listedness","listOrder":4,"seq":10},"6":{"containerView":1,"label":"Outcome","name":"outcome","listOrder":5,"seq":11},"7":{"containerView":1,"label":"Signal / Topic","name":"signalsAndTopics","listOrder":6,"seq":12},
                "8":{"containerView":1,"label":"Disposition","name":"currentDisposition","listOrder":7,"seq":13},"9":{"containerView":1,"label":"Current Disposition","name":"disposition","listOrder":8,"seq":14},"10":{"containerView":1,"label":"Assigned To","name":"assignedToUser","listOrder":9,"seq":15},"11":{"containerView":1,"label":"Due In","name":"dueDate","listOrder":10,"seq":16},"12":{"containerView":3,"label":"Suspect Products","name":"suspProd","listOrder":9999,"seq":17},"13":{"containerView":3,"label":"Con Med","name":"conComit","listOrder":9999,"seq":18},
                "14":{"containerView":3,"label":"PT List","name":"masterPrefTermAll","listOrder":9999,"seq":19},"15":{"containerView":1,"label":"Serious","name":"serious","listOrder":14,"seq":20},"16":{"containerView":3,"label":"Report Type","name":"caseReportType","listOrder":9999,"seq":21},"17":{"containerView":3,"label":"HCP","name":"reportersHcpFlag","listOrder":9999,"seq":22},"18":{"containerView":3,"label":"Country","name":"country","listOrder":9999,"seq":23},"19":{"containerView":3,"label":"Age Group","name":"age","listOrder":9999,"seq":24},"20":{"containerView":3,"label":"Gender","name":"gender","listOrder":9999,"seq":25},
                "21":{"containerView":3,"label":"Positive Rechallenge","name":"rechallenge","listOrder":9999,"seq":26},"22":{"containerView":3,"label":"Locked Date","name":"lockedDate","listOrder":9999,"seq":27},"23":{"containerView":3,"label":"Death","name":"death","listOrder":9999,"seq":28},"24":{"containerView":3,"label":"Medication Error PTs","name":"medErrorsPt","listOrder":9999,"seq":37},"25":{"containerView":3,"label":"Age","name":"patientAge","listOrder":9999,"seq":29},"26":{"containerView":3,"label":"Case Type","name":"caseType","listOrder":9999,"seq":31},
                "27":{"containerView":3,"label":"Completeness Score","name":"completenessScore","listOrder":9999,"seq":32},"28":{"containerView":3,"label":"Primary IND#","name":"indNumber","listOrder":9999,"seq":33},"29":{"containerView":3,"label":"Application#","name":"appTypeAndNum","listOrder":9999,"seq":34},"30":{"containerView":3,"label":"Compounding Flag","name":"compoundingFlag","listOrder":9999,"seq":35},"31":{"containerView":3,"label":"Indications","name":"indications","listOrder":9999,"seq":30},"32":{"containerView":3,"label":"Medication Error PT Count","name":"medErrorPtCount","listOrder":9999,"seq":38}}
           """
        viewInstance = new ViewInstance(id: 1, name: "test_view", alertType: "test_alertType", columnSeq: columnSeq, user: userObj)
        viewInstance.save(flush: true)

        viewInstance_prev = new ViewInstance(name: "test_view_prev", alertType: "test_alertType", columnSeq: columnSeq, user: userObj)
        viewInstance_prev.save(flush: true)

        clipboardCases = new ClipboardCases(id: 1, name: "test", user: userObj, caseIds: "1")
        clipboardCases.save(flush:true, validate:false)
    }

    def cleanup() {
    }

    void "test saveView"() {
        setup:
        controller.params.name = "new_view"
        controller.params.alertType = "Single Case Alert"
        controller.params.defaultView = "false"
        controller.params.currentViewId = viewInstance_prev.id
        def userServiceMocked = Mock(UserService)
        def viewInstanceServiceMocked = Mock(ViewInstanceService)
        userServiceMocked.getUser() >> userObj
        userServiceMocked.bindSharedWithConfiguration(_, _, _, _) >> null
        viewInstanceServiceMocked.propagateViews(_, _, _) >> null
        controller.userService = userServiceMocked
        controller.viewInstanceService = viewInstanceServiceMocked

        when:
        controller.saveView()

        then:
        ViewInstance.countByName("new_view") == 1
        response.json.success == true
    }

    void "test saveView exception handling"() {
        setup:
        controller.userService = [getUser: { return userObj }]

        when:
        controller.saveView()

        then:
        notThrown(Exception)
        response.json.success == false
        response.json.errorMessage == "Fill in the View Name correctly"
    }

    @ConfineMetaClassChanges([ViewInstance])
    void "test updateView"() {
        setup:
        ViewInstance.metaClass.isViewUpdateAllowed = { User user -> return true }
        def userServiceMocked = Mock(UserService)
        def viewInstanceServiceMocked = Mock(ViewInstanceService)
        userServiceMocked.getUser() >> userObj
        userServiceMocked.bindSharedWithConfiguration(_, _, _, _) >> null
        viewInstanceServiceMocked.propagateViews(_, _, _) >> null
        viewInstance.name = "updated_name"
        viewInstance.save(flush:true)
        viewInstanceServiceMocked.updateViewInstance(_, _, _, _) >> viewInstance
        viewInstanceServiceMocked.userService = userServiceMocked
        controller.userService = userServiceMocked
        controller.viewInstanceService = viewInstanceServiceMocked

        when:
        controller.params.id = viewInstance.id
        controller.params.defaultView = "true"
        controller.updateView()

        then:
        ViewInstance.countByName("test_view") == 0
        ViewInstance.countByName("updated_name") == 1
        response.json.success == true
    }

    void "test updateView exception handling"() {

        when:
        controller.updateView()

        then:
        notThrown(Exception)
        response.json.success == false
        response.json.errorMessage == "Fill in the View Name correctly"
    }


    void "test deleteView"() {
        setup:
        def viewId = viewInstance.id
        controller.params.id = viewId
        controller.params.alertType = viewInstance.alertType
        def viewInstanceServiceMocked = Mock(ViewInstanceService)
        viewInstanceServiceMocked.userService.bindSharedWithConfiguration(_, _, _, _) >> null
        controller.viewInstanceService = viewInstanceServiceMocked

        when:
        controller.deleteView()

        then:
        ViewInstance.countById(viewId) == 0
        response.json.success == true
    }

    void "test deleteView exception handling"() {
        setup:
        controller.params.id = "non_existing_id"

        when:
        controller.deleteView()

        then:
        notThrown(Exception)
        response.json.success == false
    }

    void "test viewColumnInfo "() {
        setup:
        def cacheServiceMocked = Mock(CacheService)
        cacheServiceMocked.getRorCache() >> false

        when:
        def output = controller.viewColumnInfo(viewInstance)

        then:
        notThrown(Exception)
    }
    void "test deleteTempView()"(){
        setup:
        def userServiceMocked = Mock(UserService)
        userServiceMocked.getUser() >> {
            return userObj
        }
        controller.userService = userServiceMocked
        when:
        controller.deleteTempView()
        then:
        response.status == 200
        clipboardCases.isDeleted == true
    }
    void "test updateTempView()"(){
        setup:
        def userServiceMocked = Mock(UserService)
        userServiceMocked.getUser() >> {
            return userObj
        }
        controller.userService = userServiceMocked
        when:
        controller.updateTempView(false)
        then:
        response.status == 200
        clipboardCases.isDeleted == false
    }
    void "test fetchIfTempAvailable()"(){
        setup:
        def userServiceMocked = Mock(UserService)
        userServiceMocked.getUser() >> {
            return userObj
        }
        userServiceMocked.getCurrentUserId() >>{
            return userObj
        }
        controller.userService = userServiceMocked
        when:
        controller.updateTempView(false)
        then:
        response.status == 200
    }

}