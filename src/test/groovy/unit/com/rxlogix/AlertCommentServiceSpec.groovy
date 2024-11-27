package unit.com.rxlogix

import com.rxlogix.dto.CommentDTO
import com.rxlogix.dto.RequestCommentDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.signal.AlertComment
import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll
import com.rxlogix.cache.CacheService
import spock.lang.Ignore

import javax.sql.DataSource

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class AlertCommentServiceSpec extends Specification implements ServiceUnitTest<AlertCommentService> {

    Disposition disposition
    User user
    Priority priority
    ValidatedSignal validatedSignal, validatedSignal_a
    AlertComment alertComment,alertCommentEvdas
    Group wfGroup
    User user1
    ExecutedConfiguration executedConfiguration
    Configuration alertConfiguration


    def setup() {

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)
        user1 = new User(id: 2, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user1.addToGroups(wfGroup)
        user1.preference.createdBy = "createdBy"
        user1.preference.modifiedBy = "modifiedBy"
        user1.preference.locale = new Locale("en")
        user1.preference.isEmailEnabled = false
        user1.metaClass.getFullName = { 'Fake Name' }
        user1.metaClass.getEmail = { 'fake.email@fake.com' }
        user1.save(flush: true)

        Priority priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)
        alertConfiguration = new Configuration(
                id:4,
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user1.username, modifiedBy: user1.username,
                assignedTo: user1,
                priority: priority,
                owner: user1
        )
        executedConfiguration = new ExecutedConfiguration(name: "test", isLatest: true, adhocRun: false,
                owner: user1, scheduleDateJSON: "{}", nextRunDate: new Date(), type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, totalExecutionTime: 10,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "{\"1\":{\"name\":\"testVal1\",\"2\":\"testVal2\"}}", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user1.username, modifiedBy: user1.username,
                assignedTo: user1, configId: 4,
                pvrCaseSeriesId: 1,
                pvrCumulativeCaseSeriesId: 1,
                selectedDatasource: "pva",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 1,dataMiningVariable: "Gender")
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        executedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        executedAlertDateRangeInformation.dateRangeStartAbsolute = new Date()
        executedAlertDateRangeInformation.dateRangeEndAbsolute = new Date()
        executedConfiguration.executedAlertDateRangeInformation = executedAlertDateRangeInformation
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date()
        )
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: 25l, createdBy: user1.username, modifiedBy: user1.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery, headerProductSelection: false,
                headerDateRange: false,
                blindProtected: false,
                privacyProtected: false,
                queryLevel: QueryLevelEnum.CASE
        )
        executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
        executedConfiguration.save(failOnError: true)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true)
        disposition.save(validate: false)
        priority =
                new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(validate: false)
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
//        user.grailsApplication = grailsApplication
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false

        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(validate: false)


        mockDomain(Priority, [
                [value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1]
        ])

        //Save the priority
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        //Save validated signal objects
        validatedSignal = new ValidatedSignal(id: 1, name: "test_name", products: "test_products", endDate: new Date(), assignedTo: user, assignmentType: 'USER',
                modifiedBy: user.username, priority: priority, disposition: disposition, createdBy: user.username, startDate: new Date(), genericComment: "Test notes")
        validatedSignal_a = new ValidatedSignal(id: 2, name: "test_signal_a", products: "test_products", endDate: new Date(), assignedTo: user, assignmentType: 'USER',
                modifiedBy: user.username, priority: priority, disposition: disposition, createdBy: user.username, startDate: new Date(), genericComment: "Test notes")
        validatedSignal_a.workflowGroup = wfGroup
        validatedSignal_a.save(validate: false)
        validatedSignal.workflowGroup = wfGroup
        alertCommentEvdas = new AlertComment(productName: "Test Product A", productFamily: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Evdas", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        alertCommentEvdas.save(validate: false)
        validatedSignal.addToComments(alertCommentEvdas)
        validatedSignal.save(validate: false)
    }

    def cleanup() {
    }

    void "test listSignalComments"() {
        when:
        def comment = service.listSignalComments(validatedSignal)
        then:
        comment.comments == "Test"
    }

    void "test alertCommentListForProductSummary"() {
        setup:
        def alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        def productName = "Test Product A"
        def eventList = ["Rash"]
        when:
        def comment = service.alertCommentListForProductSummary(alertType,productName,eventList)
        then:
        comment.comments.get(0) == "Test"
    }

    void "test alertCommentListForProductSummary if list is empty"() {
        setup:
        def alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        def productName = "Test Product"
        def eventList = ["Rash"]
        when:
        def comment = service.alertCommentListForProductSummary(alertType,productName,eventList)
        then:
        comment.size() == 0
    }

    void "test alertCommentListForProductSummary for Evdas Alert"() {
        setup:
        def alertType = Constants.AlertConfigType.EVDAS_ALERT
        def productName = "Test Product A"
        def eventList = ["Rash"]
        when:
        def comment = service.alertCommentListForProductSummary(alertType,productName,eventList)
        then:
        comment.comments.get(0) == "Test Evdas"
    }

    void "test alertCommentListForProductSummary for Evdas Alert if list is empty"() {
        setup:
        def alertType = Constants.AlertConfigType.EVDAS_ALERT
        def productName = "Test Product"
        def eventList = ["Rash"]
        when:
        def comment = service.alertCommentListForProductSummary(alertType,productName,eventList)
        then:
        comment.size() == 0
    }

    void "test saveValidatedSignalComments"() {
        setup:
        ValidatedSignal valSignal = new ValidatedSignal(
                id: 3,
                name: "test_val_signal",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        valSignal.save(failOnError: true)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.CommentAdded)
        activityType.save(validate:false)
        mockCRUDService()
        service.activityService = mockService(ActivityService)
        when:
        service.saveValidatedSignalComments(alertComment, 3)
        then:
        valSignal.activities.size() == 1
        valSignal.comments.size() == 1
    }

    void "test saveTopicComments"() {
        setup:
        Topic topic = new Topic(
                id: 1,
                name: "test_topic",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        topic.save(failOnError: true)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.CommentAdded)
        activityType.save(validate:false)
        mockCRUDService()
        service.activityService = mockService(ActivityService)
        when:
        service.saveTopicComments(alertComment, 1)
        then:
        topic.comments.last().comments == "Test"
        topic.activities.size() == 1

    }

    void "test saveAdhocAlertComments"() {
        setup:
        AdHocAlert adHocAlert = new AdHocAlert(id: 1, name: "Test adhoc", detectedDate: new Date(),
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                detectedBy: "user",
                priority: priority,
                initialDataSource: "pva",
                topic: "test topic")
        adHocAlert.save(failOnError: true)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.CommentAdded)
        activityType.save(validate:false)
        mockCRUDService()
        service.activityService = mockService(ActivityService)
        when:
        service.saveAdhocAlertComments(alertComment, 1)
        then:
        adHocAlert.comments.last().comments == "Test"

    }

    void "test updateSignalComment"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Evdas", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        aComment.save(failOnError: true)
        ValidatedSignal valSignal = new ValidatedSignal(
                id: 3,
                name: "test_val_signal",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        valSignal.save(failOnError: true)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.CommentAdded)
        activityType.save(validate: false)
        mockCRUDService()
        service.activityService = mockService(ActivityService)
        Map params = [:]
        params.id = 3
        params.comment = "updatedComment"
        params.validatedSignalId = 3
        when:
        def isSuccess = service.updateSignalComment(params)
        then:
        aComment.comments == "updatedComment"
        isSuccess
        valSignal.activities.size() == 1

    }

    void "test updateSignalComment for topic"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Evdas", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        aComment.save(failOnError: true)
        Topic topic = new Topic(
                id: 1,
                name: "test_topic",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        topic.save(failOnError: true)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.CommentAdded)
        activityType.save(validate: false)
        mockCRUDService()
        service.activityService = mockService(ActivityService)
        Map params = [:]
        params.id = 3
        params.comment = "updatedComment"
        params.topicId = 1
        when:
        def isSuccess = service.updateSignalComment(params)
        then:
        aComment.comments == "updatedComment"
        isSuccess
        topic.activities.size() == 1

    }

    void "test updateSignalComment for evdasAlert"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Evdas", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        aComment.save(failOnError: true)
        ExecutedEvdasConfiguration executedEvdasConfiguration = new ExecutedEvdasConfiguration(id: 1, name: "test",
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
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10 , configId:1245)
        executedEvdasConfiguration.save(failOnError: true)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.CommentUpdated)
        activityType.save(validate: false)
        mockCRUDService()
        service.activityService = mockService(ActivityService)
        Map params = [:]
        params.id = 3
        params.comment = "updatedComment"
        params.executedConfigId = "1"
        params.alertType = Constants.AlertConfigType.EVDAS_ALERT
        when:
        def isSuccess = service.updateSignalComment(params)
        then:
        aComment.comments == "updatedComment"
        isSuccess
        executedEvdasConfiguration.activities.size() == 1

    }

    void "test updateSignalComment for aggAlert"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Evdas", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        aComment.save(failOnError: true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(id: 1, name: "test",
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
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10,configId:1234)
        executedConfiguration.save(failOnError: true)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.CommentUpdated)
        activityType.save(validate: false)
        mockCRUDService()
        service.activityService = mockService(ActivityService)
        Map params = [:]
        params.id = 3
        params.comment = "updatedComment"
        params.executedConfigId = "1"
        params.alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        when:
        def isSuccess = service.updateSignalComment(params)
        then:
        aComment.comments == "updatedComment"
        isSuccess
        executedConfiguration.activities.size() == 1

    }

    void "listSingleCaseComments"() {
        setup:
        String prodFamily = "Test Product A"
        String caseNumber = "1US"
        Integer versionNum = 1
        when:
        def comment = service.listSingleCaseComments(prodFamily, caseNumber, versionNum)
        then:
        comment.comments == "Test"
    }

    void "listSingleCaseComments when there is no matching comment"() {
        setup:
        String prodFamily = "Test Produc"
        String caseNumber = "1US"
        Integer versionNum = 1
        when:
        def comment = service.listSingleCaseComments(prodFamily, caseNumber, versionNum)
        then:
        comment == null
    }

    void "listAggregateCaseComments"() {
        setup:
        String prodName = "Test Product A"
        String eventName = "Rash"
        when:
        def comment = service.listAggregateCaseComments(prodName, eventName , null)
        then:
        comment.comments == null
    }

    void "listAggregateCaseComments when there is no matching comment"() {
        setup:
        String prodName = "Test Produc"
        String eventName = "Rash"
        when:
        def comment = service.listAggregateCaseComments(prodName, eventName)
        then:
        comment == null
    }

    void "listEvdasComments"() {
        setup:
        String prodName = "Test Product A"
        String eventName = "Rash"
        when:
        def comment = service.listEvdasComments(prodName, eventName)
        then:
        comment.comments == "Test Evdas"
    }

    void "listEvdasComments when there is no matching comment"() {
        setup:
        String prodName = "Test Produc"
        String eventName = "1US"
        when:
        def comment = service.listEvdasComments(prodName, eventName)
        then:
        comment == null
    }

    void "test listTopicComments"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Topic", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        Topic topic = new Topic(
                id: 1,
                name: "test_topic",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        topic.addToComments(aComment)
        topic.save(failOnError: true)
        when:
        def comment = service.listTopicComments(topic)
        then:
        comment.comments == "Test Topic"
    }

    void "test listTopicComments if no comments"() {
        setup:
        Topic topic = new Topic(
                id: 1,
                name: "test_topic",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        topic.save(failOnError: true)
        when:
        def comment = service.listTopicComments(topic)
        then:
        comment == null
    }

    void "test listAdhocComments"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Adhoc", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        AdHocAlert adHocAlert = new AdHocAlert(id: 1, name: "Test adhoc", detectedDate: new Date(),
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                detectedBy: "user",
                priority: priority,
                initialDataSource: "pva",
                topic: "test topic")
        adHocAlert.addToComments(aComment)
        adHocAlert.save(failOnError: true)
        when:
        def comment = service.listAdhocComments(adHocAlert)
        then:
        comment.comments == "Test Adhoc"
    }

    void "test listAdhocComments if no comments"() {
        setup:
        AdHocAlert adHocAlert = new AdHocAlert(id: 1, name: "Test adhoc", detectedDate: new Date(),
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                detectedBy: "user",
                priority: priority,
                initialDataSource: "pva",
                topic: "test topic")
        adHocAlert.save(failOnError: true)
        when:
        def comment = service.listAdhocComments(adHocAlert)
        then:
        comment == null
    }

    void "getComment for Single Case Alert"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [productFamily: "Test Product A",
                      caseNumber   : "1US",
                      alertType    : Constants.AlertConfigType.SINGLE_CASE_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        noExceptionThrown()

    }

    void "getComment for Single Case Alert if no comments"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [productFamily: "Test Product",
                      caseNumber   : "1US",
                      alertType    : Constants.AlertConfigType.SINGLE_CASE_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        comment == null
    }

    void "getComment for aggregateCaseAlert"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [productName: "Test Product A",
                      eventName  : "Rash",
                      alertType  : Constants.AlertConfigType.AGGREGATE_CASE_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        noExceptionThrown()
    }

    void "getComment for aggregateCaseAlert if no comments"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [productName: "Test Product",
                      eventName  : "Rash",
                      alertType  : Constants.AlertConfigType.AGGREGATE_CASE_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        comment == null
    }

    void "getComment for evdasAlert"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [productName: "Test Product A",
                      eventName  : "Rash",
                      alertType  : Constants.AlertConfigType.EVDAS_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        comment.comments == "Test Evdas"
    }

    void "getComment for evdasAlert if no comments"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [productName: "Test Product",
                      eventName  : "Rash",
                      alertType  : Constants.AlertConfigType.EVDAS_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        comment == null
    }

    void "getComment for validatedSignal"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [validatedSignalId: validatedSignal.id,
                      alertType        : Constants.AlertConfigType.SIGNAL_MANAGEMENT]
        when:
        def comment = service.getComment(params)
        then:
        comment.comments == "Test Evdas"
    }

    void "getComment for validatedSignal if no comments"() {
        setup:
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [validatedSignalId: validatedSignal_a.id,
                      alertType        : Constants.AlertConfigType.SIGNAL_MANAGEMENT]
        when:
        def comment = service.getComment(params)
        then:
        comment == null
    }
    @Ignore
    void "getComment for topic"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Topic", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        Topic topic = new Topic(
                id: 1,
                name: "test_topic",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        topic.addToComments(aComment)
        topic.save(failOnError: true)
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [topicId  : "${topic.id}",
                      alertType: Constants.AlertConfigType.TOPIC]
        when:
        def comment = service.getComment(params)
        then:
        comment.comments == "Test Topic"

    }
    @Ignore
    void "getComment for topic if no comments"() {
        setup:
        Topic topic = new Topic(
                id: 1,
                name: "test_topic",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        topic.save(failOnError: true)
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [topicId  : "${topic.id}",
                      alertType: Constants.AlertConfigType.TOPIC]
        when:
        def comment = service.getComment(params)
        then:
        comment == null
    }

    void "getComment for adhocAlert"() {
        setup:
        AlertComment aComment = new AlertComment(id: 3, productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Adhoc", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        AdHocAlert adHocAlert = new AdHocAlert(id: 1, name: "Test adhoc", detectedDate: new Date(),
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                detectedBy: "user",
                priority: priority,
                initialDataSource: "pva",
                topic: "test topic",assignedTo: user)
        adHocAlert.addToComments(aComment)
        adHocAlert.save(failOnError: true)
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [adhocAlertId: "${adHocAlert.id}",
                      alertType   : Constants.AlertConfigType.AD_HOC_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        comment.comments == "Test Adhoc"

    }

    void "getComment for adhocAlert if no comments"() {
        setup:
        AdHocAlert adHocAlert = new AdHocAlert(id: 1, name: "Test adhoc", detectedDate: new Date(),
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                detectedBy: "user",
                priority: priority,
                initialDataSource: "pva",
                topic: "test topic" , assignedTo: user)
        adHocAlert.save(failOnError: true)
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        Map params = [adhocAlertId: "${adHocAlert.id}",
                      alertType   : Constants.AlertConfigType.AD_HOC_ALERT]
        when:
        def comment = service.getComment(params)
        then:
        comment == null
    }
    @Unroll
    void "test createActivityForComment" (){
        given:

            def alertObj=value
            AlertComment commentobj=new AlertComment()
            commentobj.comments=alertObj.comments
            User user =new User()
            Activity activity=new Activity(details:'new Comment Added')
            def mockCacheService=Mock(CacheService)
            mockCacheService.getUserByUserId(_)>> {
                return user
            }
            mockCacheService.getActivityTypeByValue(_)>>{
                return
            }
            service.cacheService=mockCacheService
            def mockUserService=Mock(UserService)
            mockUserService.getCurrentUserId()>>{

            }
            service.userService=mockUserService
            def mockActivityService=Mock(ActivityService)
            mockActivityService.createActivityBulkUpdate(_,_,_,_,_,_,_,_,_)>>{
                return activity
            }
            service.activityService=mockActivityService
        when:
            def result=service.createActivityForComment(commentobj,alertObj)
        then:
            result==activity

        where:
            value<<[[alertType:'testAlertType', productName:'Test Product',assignedTo:'24',caseNumber:null, eventName:'testEvent', comments:'new test comment!', productFamily:'Test Product Family', productId:null, ptCode:null],
                [alertType:'testAlertType', productName:'Test Product',caseNumber:null, eventName:'testEvent', comments:'new test comment!', productFamily:'Test Product Family', productId:null, ptCode:null]
        ]
    }

    private void mockCRUDService() {
        service.CRUDService = mockService(CRUDService)
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        service.CRUDService.userService = mockService(UserService)
        service.CRUDService.userService.springSecurityService = mockService(SpringSecurityService)
    }

    void "test getCommentHistory for pva"(){
        setup:
        Map params = [:]
        params.caseId = 123L
        params.versionNum = 1
        params.isFaers = false
        def userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        service.reportIntegrationService = [get: {String s,String s2, Map c ->
            return [status: 200, data: ["data": [[alertName: "aj-1", caseNumber: 123]]]]
        }]
        when:
        List res = service.getCommentHistory(params)
        then:
        assert res.size() == 1
    }

    void "test saveGlobalCommentInMart"(){
        given:
        RequestCommentDTO requestCommentDTO = new RequestCommentDTO()
        List<CommentDTO> commentDTOS = []
        requestCommentDTO.commentDTOS = commentDTOS
        service.reportIntegrationService = [postData: { c ->
            return [status: 200, data: [success: true, data: true]]
        }]

        when:
        boolean result = service.saveGlobalCommentInMart(requestCommentDTO)

        then:
        result == true
    }

    void "test getPeriodForCommentHistory"() {
        when:
        String result = service.getPeriodForCommentHistory(executedConfiguration)

        then:
        result=="20-Apr-2022 - 20-Apr-2022"
    }


}