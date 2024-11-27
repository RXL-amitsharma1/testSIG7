package unit.com.rxlogix
import com.rxlogix.ActivityService
import com.rxlogix.AlertAttributesService
import com.rxlogix.AlertService
import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.DataObjectService
import com.rxlogix.EmailService
import com.rxlogix.SignalAuditLogService
import com.rxlogix.DataObjectService
import com.rxlogix.SignalWorkflowService
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.SignalWorkflowService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Activity
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.AllowedDictionaryDataCache
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.config.ProductDictionaryCache
import com.rxlogix.config.SafetyGroup
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SignalEmailLog
import com.rxlogix.signal.SignalRMMs
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.UndoableDisposition
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.signal.SystemConfig
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.testing.services.ServiceUnitTest
import org.springframework.web.multipart.MultipartFile
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import java.text.SimpleDateFormat
@ConfineMetaClassChanges([ValidatedSignalService])
class ValidatedSignalServiceSpec extends Specification implements ServiceUnitTest<ValidatedSignalService> {

    User user
    Disposition defaultSignalDisposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Priority priority
    Group wfGroup
    ValidatedSignal validatedSignal
    ValidatedSignal validatedSignal2
    ValidatedSignal validatedSignal3
    ValidatedSignal validatedSignal4
    SignalStatusHistory signalStatusHistory
    SignalStatusHistory signalStatusHistory2
    SignalStatusHistory signalStatusHistory3
    SafetyGroup safetyGroup
    AllowedDictionaryDataCache allowedDictionaryDataCache
    ProductDictionaryCache productDictionaryCache
    Configuration configuration_a
    Activity activity1
    ActivityType activityType
    def setup() {
        priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush: true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        [defaultDisposition, defaultSignalDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }
        activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save(flush:true)
        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(flush:true)
        activity1 = new Activity(type: activityType,
                performedBy: user, timestamp: DateTime.now(), justification: "change needed", assignedTo: user,
                details: "Case has been added", attributes: "attributes", assignedToGroup: wfGroup)
        activity1.save(flush:true,fainOnError:true)
        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true,performedBy: "Test User",id:1)
        signalStatusHistory.save()
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save(flush:true)
        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true,performedBy: "Test User",id:1)
        signalStatusHistory.save()
        signalStatusHistory2 = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Assessment Date", signalStatus: "Assessment Date",
                dispositionUpdated: false, performedBy: "Test User", id: 1)
        signalStatusHistory2.save()
        signalStatusHistory3 = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Date Closed", signalStatus: "Date Closed",
                dispositionUpdated: false, performedBy: "Test User", id: 1)
        signalStatusHistory3.save()
        validatedSignal = new ValidatedSignal(
                name: "test_name",
                type:"Test type",
        )

                signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true,performedBy: "Test User",id:1)
        signalStatusHistory.save()

        validatedSignal = new ValidatedSignal(
                name: "test_name",
                type: "Test type",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                activities: [activityType],
                id:1,
                genericComment: "Test notes",
                workflowGroup: wfGroup,
        )
        validatedSignal.addToActivities(activity1)
        validatedSignal.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal2 = new ValidatedSignal(
                name: "test_name",
                type: "Test type",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                id: 1,
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )
        validatedSignal2.addToSignalStatusHistories(signalStatusHistory2)
        validatedSignal2.save(flush: true)
        validatedSignal3 = new ValidatedSignal(
                name: "test_name",
                type: "Test type",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                id: 1,
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )
        validatedSignal3.addToSignalStatusHistories(signalStatusHistory2)
        validatedSignal3.addToSignalStatusHistories(signalStatusHistory3)
        validatedSignal3.save(flush: true)
        validatedSignal4 = new ValidatedSignal(
                name: "test_name",
                type: "Test type",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                id: 1,
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )
        validatedSignal4.save(flush: true)
        safetyGroup = new SafetyGroup(name: "Test Safety", createdBy: user.name , dateCreated: new Date(), lastUpdated: new Date(), modifiedBy: user.name, allowedProd: "Test Product A, Test Product B, Test Product C", allowedProductList: ["Test Product A", "Test Product B", "Test Product C"], members: [user])
        validatedSignal.save(flush: true, failOnError: true)  // Add failOnError for more detailed error reporting
        ValidatedSignal.metaClass.'static'.get = { ->
            validatedSignal
        }

        SystemConfig systemConfig = new SystemConfig()
        SystemConfig.metaClass.'static'.first = { ->
            systemConfig
        }

        configuration_a = new Configuration(
                id: 100001,
                name: 'case_series_config',
                assignedTo: user,
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                productGroupSelection: '{["name":"product group","id":1]}',
                eventGroupSelection: '{["name":"event group","id":1]}',
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                priority: priority,
                isCaseSeries: true,
                alertCaseSeriesId: 1L,
                alertCaseSeriesName: "case series a"
        )
        configuration_a.save(flush: true)
        safetyGroup = new SafetyGroup(name: "Test Safety", createdBy: user.username, dateCreated: new Date(), lastUpdated: new Date(), modifiedBy: user.username, allowedProd: "Test Product A, Test Product B, Test Product C", allowedProductList: ["Test Product A", "Test Product B", "Test Product C"], members: [user])
        safetyGroup.save(flush: true)
        productDictionaryCache = new ProductDictionaryCache(safetyGroup: safetyGroup)
        productDictionaryCache.save(flush: true)
        allowedDictionaryDataCache = new AllowedDictionaryDataCache(fieldLevelId: 1, label: "Product Name", isProduct: true, allowedData: "Test Product A, Test Product B, Test Product C", allowedDataIds: "123, 456, 789")
        allowedDictionaryDataCache.productDictionaryCache = productDictionaryCache
        allowedDictionaryDataCache.save(flush: true)
    }
    void "Test saveValidatedSignal"() {
        given: "A map with required parameters"
        ValidatedSignal validatedSignal1
        Map<String, String> map = ['productSelection': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'eventSelection': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': 'Sahi', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: '1', description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', 'assignedToValue': 'User_1', initialDataSource: "Data mining - FAERS database"]
        def mockUserService = Mock(UserService)
        service.userService = mockUserService
        SignalWorkflowService signalWorkflowService = Mock(SignalWorkflowService)
        service.signalWorkflowService = signalWorkflowService
        signalWorkflowService.defaultSignalWorkflowState() >> "Safety Observation Validation"
        AlertService alertService = Mock(AlertService)
        service.alertService = alertService
        alertService.productSelectionSignal(_) >> "Test"
        mockUserService.getUser() >> user
        service.metaClass.setDatesForSignal = { ValidatedSignal validatedSignal, Map<String, String> params -> }
        service.metaClass.setNonStringFields = { Map<String, String> params, ValidatedSignal validatedSignal -> }
        service.metaClass.bindTopicCategory = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindActionTaken = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindEvaluationMethod = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindOutcomes = { ValidatedSignal validatedSignal, def signaloutcomeList -> }
        service.metaClass.bindLinkedSignals = { ValidatedSignal validatedSignal, def linkedSignalList -> }
        when:
        validatedSignal1 = service.saveValidatedSignal(map)
        then:
        validatedSignal1.disposition == defaultSignalDisposition
    }
    void "test validateSignal with all required parameters"() {
        given: "A map with required parameters"
        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> {
            return (User.get(1))
        }
        service.userService = mockUserService
        SignalWorkflowService signalWorkflowService = Mock(SignalWorkflowService)
        service.signalWorkflowService = signalWorkflowService
        signalWorkflowService.defaultSignalWorkflowState() >> "Safety Observation Validation"
        AlertService alertService = Mock(AlertService)
        service.alertService = alertService
        Map<String, String> map = ['products': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'events': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],' +
                '"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': 'Sahi', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: Priority.findByValue('High'),
                                   description                                   : 'Description', reasonForEvaluation: '', commentSignalStatus: '', assignedTo: User.findByUsername('test_user'), dueDate: new Date(), detectedDate: new Date(), initialDataSource: "Data mining - FAERS database", workflowGroup: wfGroup, assignmentType: 'USER', modifiedBy: 'modifiedBy', disposition: defaultDisposition, createdBy: 'createdBy']

        ValidatedSignal validatedSignal1 =
                new ValidatedSignal(map)
        validatedSignal1.save(flush: true)
        Boolean result = false
        when:
        result = service.validateSignal(validatedSignal1)
        then:
        result
    }
    void "test validateSignal without all required parameters"() {
        given: "A map without required parameters"
        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> {
            return (User.get(1))
        }
        service.userService = mockUserService
        Map<String, String> map = ['products': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'events': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': '', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: Priority.findByValue('High'), description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', assignedTo: User.findByUsername('test_user'), dueDate: new Date(), detectedDate: new Date(), initialDataSource: "Data mining - FAERS database"]
        ValidatedSignal validatedSignal = new ValidatedSignal(map)
        Boolean result = false
        when:
        result = service.validateSignal(validatedSignal)
        then:
        !result
    }
    void "test generateSignalHistory when it has StatusHistory"() {
        setup:
        AlertAttributesService mockAlertAttributeService = Mock(AlertAttributesService)
        mockAlertAttributeService.get(_) >> { return ['testA', 'testB'] }
        service.alertAttributesService = mockAlertAttributeService
        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignal)
        then:
        signalStatusHistoryMapList.size() == 1
        signalStatusHistoryMapList[0].signalStatus == 'Signal Status'
        signalStatusHistoryMapList[0].dispositionUpdated == true
        signalStatusHistoryMapList[0].performedBy == 'Test User'
        signalStatusHistoryMapList[0].statusComment == 'Test Status Comment'
        signalStatusHistoryMapList[0].isAddRow == true
    }
    void "test generateSignalHistory when StatusHistory is empty"() {
        setup:
        ValidatedSignal validatedSignalObj = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes",
                actualDueDate: new Date()
        )
        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignalObj)
        then:
        signalStatusHistoryMapList.size() == 0
    }
    void "test generateSignalHistory when ValidatedSignal is null"() {
        setup:
        ValidatedSignal validatedSignalObj = null
        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignalObj)
        then:
        signalStatusHistoryMapList.size() == 0
    }
    void "test saveActivityForSignalHistory"() {
        setup:
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save()
        UserService userService = Mock(UserService)
        userService.getCurrentUserId() >> 1
        service.userService = userService
        String details = "Details"
        when:
        service.saveActivityForSignalHistory(Constants.SYSTEM_USER, validatedSignal, details)
        then:
        validatedSignal.activities.size() == 1
    }
    void "test saveActivityForSignalHistory when ValidatedSignal is null"() {
        setup:
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save()
        ValidatedSignal validatedSignalObj = null
        service.userService = mockService(UserService)
        when:
        service.saveActivityForSignalHistory(Constants.SYSTEM_USER, validatedSignalObj, null)
        then:
        validatedSignalObj == null
    }
    void "test saveActivityForSignalHistory when Activity type is null"() {
        setup:
        service.userService = mockService(UserService)
        when:
        service.saveActivityForSignalHistory(Constants.SYSTEM_USER, validatedSignal, null)
        then:
        validatedSignal.activities == null
    }
    void "test saveSignalStatusHistory"() {
        setup:
        Holders.config.server.timezone = 'UTC'
        Holders.config.signal.defaultValidatedDate = 'Validation Date'
        Map params = [signalId: 1, statusComment: "Test Status Comment", signalStatus: "Assessment Date", dateCreated: new Date()]
        UserService userServiceMock = Mock(UserService)
        userServiceMock.getUserFromCacheByUsername(_) >> user
        service.userService = userServiceMock
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> validatedSignal
        service.CRUDService = crudServiceMock
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save()

        when:
        def result = service.saveSignalStatusHistory(params, true)
        then:
        result.size() == 2
        result[1] == true
        validatedSignal.activities.size() == 1
    }
    void "test saveSignalStatusHistory in edit mode"() {
        setup:
        Holders.config.server.timezone = 'UTC'
        Holders.config.signal.defaultValidatedDate = 'Validation Date'
        Map params = ["signalHistoryId": "1", statusComment: "Test Status Comment Edit", signalStatus: "Assessment Date", dateCreated: new Date()]
        UserService userServiceMock = Mock(UserService)
        userServiceMock.getUserFromCacheByUsername(_) >> user
        service.userService = userServiceMock
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> validatedSignal
        service.CRUDService = crudServiceMock
        when:
        service.saveSignalStatusHistory(params, true)
        then:
        signalStatusHistory.statusComment == "Test Status Comment Edit"
    }
    void "test saveSignalStatusHistory when isDispositionUpdated is false"() {
        setup:
        Holders.config.server.timezone = 'UTC'
        Holders.config.signal.defaultValidatedDate = 'Validation Date'
        Map params = [signalId: "1", statusComment: "Test Status Comment", signalStatus: "Assessment Date", dateCreated: new Date()]
        UserService userServiceMock = Mock(UserService)
        userServiceMock.getUserFromCacheByUsername(_) >> user
        service.userService = userServiceMock
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> validatedSignal
        service.CRUDService = crudServiceMock
        when:
        service.saveSignalStatusHistory(params, false)
        then:
        signalStatusHistory.statusComment == "Test Status Comment"
    }
    @Ignore
    void "test getStringIdList"() {
        given:
        String str = "ID_0, ID_1, ID_2"
        when:
        List<String> list = service.getStringIdList(str)
        then:
        list == ["ID_0", "ID_1", "ID_3"]
    }
    @Ignore
    void "test getAllowedProductIds"() {
        given:
        SafetyGroup safetyGroup1 = safetyGroup
        service.getStringIdList(_) >> ["Test A"]
        when:
        List<String> allowedProductIds = service.getAllowedProductIds(safetyGroup1)
        then:
        allowedProductIds == ["Test A"]
    }
    void "test getSingleAlertListForSignalSummaryReport"() {
        setup:
        def mockAlert = [
                id: 1,
                isStandalone: false,
                alertName: "Test Alert",
                priority: [value: "High"],
                isCaseNumberAvailable: true,
                caseNumber: "CN123",
                followUpNumber: 1,
                productName: "Product A",
                masterPrefTermAll: "Event PT",
                disposition: "Test Disposition"
        ]

        service.metaClass.getSingleCaseAlertListForSignal = { String id ->
            [mockAlert]
        }

        service.metaClass.getSignalsFromAlertObj = { def alert, String alertType ->
            [validatedSignal]
        }
        String signalId = validatedSignal.id.toString()

        when:
        List result = service.getSingleAlertListForSignalSummaryReport(signalId)

        then:
        result.size() == 1
        result[0].alertName == "Test Alert"
        result[0].priority == "High"
        result[0].caseNumber == "CN123(1)"
        result[0].productName == "Product A"
        result[0].eventPt == "Event PT"
        result[0].disposition == "Test Disposition"
        result[0].signalNames == "test_name"
    }

    void "test for setUpdatedValuesForStringFields for Generic Comment check"() {
        setup:
        ValidatedSignal validatedSignal = new ValidatedSignal() // or however you initialize it
        validatedSignal.genericComment = "Comment"
        Map<String, String> params = [genericComment: 'Generic Comment']
        StringBuilder details = new StringBuilder()
        DataObjectService dataObjectService = Mock(DataObjectService)
        dataObjectService.getProbDataMap() >> {
            return
        }
        service.dataObjectService = dataObjectService
        AlertService alertService = Mock(AlertService)
        alertService.productSelectionSignal(_) >> "Test"
        service.alertService = alertService
        when:
        service.setUpdatedValuesForStringFields(validatedSignal, params, details)
        then:
        validatedSignal.genericComment == params.genericComment
    }

    void "revertDisposition"(){
        setup:
        // Mocking the ValidatedSignal.get(id) call
        ValidatedSignal.metaClass.static.get = { Long id ->
            return validatedSignal
        }
        // Mocking the UndoableDisposition.createCriteria().get call
        UndoableDisposition.metaClass.createCriteria = {
            return [get: { Closure closure ->
                def criteria = Mock(grails.gorm.DetachedCriteria)
                criteria.get(_) >> {
                    // Create a mock UndoableDisposition instance with desired properties
                    def undoableDisposition = new UndoableDisposition(
                            objectId: 1L,
                            objectType: Constants.AlertConfigType.VALIDATED_SIGNAL,
                            dateCreated: new Date(),
                            isEnabled: true,
                            prevDispositionId: 1L,
                            prevActualDueDate: new Date(),
                            prevDueDate: new Date(),
                            prevMilestoneDate: new Date(),
                            prevDispPerformedBy: 'User1',
                            signalOutcomeId: null,
                            isDueDateChanged: false,
                            previousDueIn:new Date()
                    )
                    return undoableDisposition
                }
                return criteria
            }]
        }
        // Mocking the SystemConfig.first() call
        SystemConfig.metaClass.static.first = {
            def systemConfig = Mock(SystemConfig)
            systemConfig.enableEndOfMilestone >> true // or false, depending on your test case
            systemConfig.selectedEndPoints >> "some,end,points"
            systemConfig.enableSignalWorkflow >> true
            return systemConfig
        }
        when:
        service.revertDisposition(1,"Test Justification")
        then:
        validatedSignal != null
    }

    void "Test addConfigurationToSignal"() {
        given:
        Long configId = configuration_a.id
        String signalId = validatedSignal.id.toString()

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> user
        service.userService = mockUserService

        ActivityService mockActivityService = Mock(ActivityService)
        service.activityService = mockActivityService

        SignalAuditLogService mockSignalAuditLogService = Mock(SignalAuditLogService)
        service.signalAuditLogService = mockSignalAuditLogService

        when:
        service.addConfigurationToSignal(signalId, configId)

        then:
        validatedSignal.configuration.contains(configuration_a)
    }


    void "test checkIfSignalContainsDateClosed"() {
        when:
        boolean testValidatedSignal2 = service.checkIfSignalContainsDateClosed(validatedSignal2) // Date Closed not present
        boolean testValidatedSignal3 = service.checkIfSignalContainsDateClosed(validatedSignal3) // Date Closed present
        boolean testValidatedSignal4 = service.checkIfSignalContainsDateClosed(validatedSignal4) // Signal Status History not present
        then:
        testValidatedSignal2 == false
        testValidatedSignal3 == true
        testValidatedSignal4 == false
    }

    void "test getSignalStatus"() {
        when:
        String testValidatedSignal2 = service.getSignalStatus(validatedSignal2) // Date Closed not present
        String testValidatedSignal3 = service.getSignalStatus(validatedSignal3) // Date Closed present
        String testValidatedSignal4 = service.getSignalStatus(validatedSignal4) // Signal Status History not present
        then:
        testValidatedSignal2 == 'Ongoing'
        testValidatedSignal3 == 'Closed'
        testValidatedSignal4 == 'Ongoing'
    }

    void "test fetchSignalNotInAlertObj"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        UserService userService = Mock(UserService)
        service.cacheService = cacheService
        service.userService = userService

        String searchTerm = "Test"
        int page = 1
        int pageSize = 10

        User mockUser = Mock(User) {
            getWorkflowGroup() >> new Group(forceJustification: true, groupType: GroupType.WORKFLOW_GROUP, id: 2)
        }
        cacheService.getUserByUserNameIlike("test_user") >> mockUser
        userService.getCurrentUserName() >> "test_user"

        ValidatedSignal validatedSignal = new ValidatedSignal(validatedSignal.properties)
        validatedSignal.id = 1
        validatedSignal.disposition = new Disposition(displayName: 'Disposition', value: 'Disposition', id: 1)
        validatedSignal.name = "Test Signal"
        validatedSignal.products = "{\"1\":[{\"name\":\"PARACETAMOL\",\"id\":\"9446\",\"isMultiIngredient\":false}],\"2\":[],\"3\":[],\"4\":[]}"
        validatedSignal.detectedDate = new Date()
        validatedSignal.workflowGroup = Group.read(2)
        validatedSignal.save(flush:true)
        ValidatedSignal.metaClass.static.executeQuery = { String query ->
            return [1]
        }
        ValidatedSignal.metaClass.static.executeQuery = { String query, Map list ->
                return [validatedSignal]
        }
        Holders.config.alert.selectProductDictionaryItems = ['1', '2', '3', '4']
        String detectedDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date())

        when:
        Map result = service.fetchSignalsNotInAlertObj(searchTerm, page, pageSize)

        then:
        result.total_count == 1
        result.items.size() == 1
        result.items[0].name == "Test Signal"
        result.items[0].id == 1
        result.items[0].isClosed == false
        result.items[0].products == "(PARACETAMOL)"
        result.items[0].detectedDate == detectedDate
        result.items[0].disposition == "Disposition"
        result.items[0].forceJustification == true
    }
    void "test saveSignalMemoInCommunication"() {
        given:
        def params = [body: "Test Body", subject: "Test Subject", sentTo: "test@example.com"]
        def criteria = '{"10189":["10179","9638"]}'
        def username = "Test User"
        def signalRMMs = Mock(SignalRMMs)
        def multipartFile = Mock(MultipartFile)
        def signalEmailLog = Mock(SignalEmailLog)
        def emailService = Mock(EmailService)
        def activity = Mock(Activity)

        when:
        validatedSignal.getProductAndGroupName() >> "Test Product Group"
        validatedSignal.signalRMMs >> []
        validatedSignal.name >> "Test Signal"
        validatedSignal.id >> 1L
        validatedSignal.signalEmailLog >> signalEmailLog

        and:
        Holders.config.signal.autoNotification.reportFormat >> "pdf"
        Holders.config.signal.autoNotification.description >> "Test Description"

        and:
        service.prepareSignalMemoReport(validatedSignal, username, params.inputName) >> multipartFile
        service.createHref("validatedSignal", "details", ["id": validatedSignal.id]) >> "http://example.com/signal/1"

        then:
        service.saveSignalMemoInCommunication(params, validatedSignal, criteria, username)

        then:
        1 * signalRMMs.setDueDate(null)
        1 * signalRMMs.setType("Signal Memo")
        1 * signalRMMs.setCommunicationType("communication")
        1 * signalRMMs.setDescription("Test Description")
        1 * signalRMMs.setDateCreated(_ as Date)
        1 * signalRMMs.setEmailSent(_ as Date)
        1 * signalRMMs.setCriteria(criteria)
        1 * signalRMMs.setIsDeleted(false)
        1 * signalRMMs.setSignalId(validatedSignal.id as Long)
        1 * signalRMMs.setSignalEmailLog(signalEmailLog)
        1 * service.bindSignalRMMsToSignal(signalRMMs, validatedSignal)
        1 * service.bindMemoFiles(signalRMMs, params, multipartFile)
        1 * service.bindMemoFiles(signalRMMs.signalEmailLog, params, multipartFile)
        1 * service.createActivityForJobs(signalRMMs, validatedSignal, params.inputName as String)
        1 * emailService.sendCommunicationEmail([
                'toAddress'        : [params.sentTo],
                'title'            : params.subject,
                'map'              : ["emailMessage": params.body, "attachments": _],
                'allowNotification': true
        ])
    }

}
