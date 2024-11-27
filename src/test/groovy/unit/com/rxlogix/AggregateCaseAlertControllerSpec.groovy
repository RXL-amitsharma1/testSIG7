package unit.com.rxlogix

import com.rxlogix.*
import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentableService
import com.rxlogix.cache.CacheService
import com.rxlogix.commandObjects.TokenAuthenticationCO
import com.rxlogix.config.*
import groovy.json.JsonSlurper
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.signal.*
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import groovy.transform.SourceURI
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.web.multipart.MultipartFile
import spock.lang.Ignore
import spock.lang.Specification
import javax.sql.DataSource
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class AggregateCaseAlertControllerSpec extends Specification implements ControllerUnitTest<AggregateCaseAlertController> {

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    Configuration configuration
    ExecutedAlertDateRangeInformation executedAlertDateRangeInformation
    ExecutedConfiguration executedConfiguration
    User user, newUser
    def attrMapObj
    AggregateCaseAlert alert
    Priority priority
    Priority priorityNew
    AlertTag alert_tag
    ExecutionStatus executionStatus
    ValidatedSignal validatedSignal
    DateRangeInformation dateRangeInformation
    AlertDateRangeInformation alertDateRangeInformation
    ReportTemplate reportTemplate
    TemplateQuery templateQuery
    AggregateCaseAlertService aggregateCaseAlertService
    Justification justification
    def caseHistoryService
    File file

    void setup() {
        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("AdHocAlertControllerSpec.groovy", "testingFiles/Details.html")
        file = new File(directory)

        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        priorityNew = new Priority([displayName: "mockPriorityNew", value: "mockPriorityNew", display: true, defaultPriority: true, reviewPeriod: 1])
        priorityNew.save(failOnError: true)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VSS")
        disposition.save(flush: true, failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }
        Group wfGroup = new Group(name: "Default", createdBy: "ujjwal", modifiedBy: "ujjwal", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.enabled = true
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(failOnError: false)

        //Prepare the mock new User
        newUser = new User(id: '2', username: 'usernameNew', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        newUser.addToGroups(wfGroup)
        newUser.enabled = true
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.metaClass.getFullName = { 'Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.save(failOnError: true)


        mockDomain(Priority, [
                [value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1]
        ])

        dateRangeInformation = new DateRangeInformation(dateRangeStartAbsoluteDelta: 1, dateRangeEndAbsoluteDelta: 1,
                dateRangeEndAbsolute: new Date() + 1, dateRangeStartAbsolute: new Date())
        alertDateRangeInformation = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 5, dateRangeStartAbsoluteDelta: 2, dateRangeEnum: DateRangeEnum.CUSTOM)
        Category category1 = new Category(name: "category1")
        category1.save(flush: true, failOnError: true)
        reportTemplate = new ReportTemplate(name: "repTemp1", description: "repDesc1", category: category1,
                owner: user, templateType: TemplateTypeEnum.TEMPLATE_SET, dateCreated: new Date(), lastUpdated: new Date(),
                createdBy: "username", modifiedBy: "username")
        reportTemplate.save(flush: true, failOnError: true)
        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/
        alertConfiguration = new Configuration(
                executing: false,
                template: reportTemplate,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                productSelection: "Test Product A",
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                drugType: '6267,6219,6283',
                scheduleDateJSON: recurrenceJSON,
                owner: user,
                isEnabled: true,
                alertDateRangeInformation: alertDateRangeInformation,
                nextRunDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
        )
        alertConfiguration.save(flush: true, failOnError: true)

        templateQuery = new TemplateQuery(template: 1L, query: 1L, templateName: "temp1", queryName: "query1",
                dateRangeInformationForTemplateQuery: dateRangeInformation, dateCreated: new Date(), lastUpdated: new Date(),
                createdBy: user.username, modifiedBy: user.username, report: alertConfiguration)
        templateQuery.save(flush: true, failOnError: true)

        configuration = new Configuration(
                id: 1,
                executing: false,
                template: reportTemplate,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                missedCases: false,
                scheduleDateJSON: recurrenceJSON,
                selectedDatasource: "pva",
                name: "test",
                spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                productSelection: '{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}',
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                createdBy: newUser.username, modifiedBy: newUser.username,
                assignedTo: newUser,
                priority: priority,
                drugType: '6267,6219,6283',
                owner: newUser,
                isEnabled: true,
                alertDateRangeInformation: alertDateRangeInformation,
                nextRunDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
        )

        executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.LAST_YEAR,
                dateRangeStartAbsolute: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), dateRangeEndAbsolute: Date.parse('dd-MMM-yyyy', '31-Dec-2014'))

        executedConfiguration = new ExecutedConfiguration(id: configuration.id, name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(), executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, selectedDatasource: Constants.DataSource.FAERS,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                dataMiningVariable : 'Gender',
                adhocRun: true,
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true, spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true, groupBySmq: false,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                configId :247,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10 )
        executedConfiguration.save(validate:false)

        attrMapObj = ['masterFollowupDate_5' : new Date(),
                      'masterRptTypeId_3'    : "test type",
                      'masterInitReptDate_4' : new Date(),
                      'reportersHcpFlag_2'   : "true",
                      'masterProdTypeList_6' : "test",
                      'masterPrefTermAll_7'  : "test",
                      'assessOutcome'        : "Death",
                      'assessListedness_9'   : "test",
                      'assessAgentSuspect_10': "test"]

        alert_tag = new AlertTag(name: 'alert_tag', createdBy: user, dateCreated: new Date())
        alert_tag.save(flush: true)

        alert = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product",
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
                createdBy: alertConfiguration.assignedTo.username,
                modifiedBy: alertConfiguration.assignedTo.username,
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
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test",
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3",
                alertTags: alert_tag
        )
        alert.save(failOnError: true)

        executionStatus = new ExecutionStatus(configId: alertConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.COMPLETED,
                reportVersion: 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                nextRunDate: new Date())
        executionStatus.save(flush: true)

        SubstanceFrequency frequency = new SubstanceFrequency(name: 'Test Product', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: "Aggregate Case Alert")
        frequency.save(flush: true)
        SubstanceFrequency frequency1 = new SubstanceFrequency(name: 'Test Product 1', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS)
        frequency1.save(flush: true)

        validatedSignal = new ValidatedSignal(name: "ValidatedSignal", assignedTo: user, assignmentType: "signalAssignment",
                createdBy: "username", disposition: disposition, modifiedBy: "username", priority: priority, products: "product1", workflowGroup: wfGroup)
        validatedSignal.save(flush: true, failOnError: true)

        Alert alert1 = new Alert(assignedTo: user, priority: priority)
        alert1.save(flush: true, failOnError: true)

        def alertServiceObj = Mock(AlertService)
        def allowedProducts = ["Calpol01", "Test Product AJ", "ALL-LIC-PROD", "Wonder Product"]
        def resultMapData = [totalCount: 1, totalFilteredCount: 1, resultList: [[alertName: "test case alert"]]]
        alertServiceObj.fetchAllowedProductsForConfiguration() >> allowedProducts
        alertServiceObj.getAlertFilterCountAndList(_) >> resultMapData
        alertServiceObj.isProductSecurity() >> true
        alertServiceObj.prepareFilterMap(_, _) >> [productName: 'productA', assigned: user.username, newDashboardFilter: true]
        alertServiceObj.prepareOrderColumnMap(_) >> [name: 'productA', dir: 'dir']
        alertServiceObj.getAlertFilterCountAndList(_) >> [totalCount: 10, totalFilteredCount: 20, resultList: [alert], fullCaseList: [alert]]
        alertServiceObj.getDistinctProductName(_, _, _) >> ['productA', 'productB']
        controller.alertService = alertServiceObj

        ProductEventHistoryService mockProductEventHistoryService = Mock(ProductEventHistoryService)
        mockProductEventHistoryService.batchPersistHistory(_) >> {
            return true
        }
        controller.productEventHistoryService = mockProductEventHistoryService

        UserService mockUserService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        preference.isCumulativeAlertEnabled >> true
        mockUserService.getCurrentUserPreference() >> preference
        mockUserService.getUser() >> {
            return user
        }
        mockUserService.bindSharedWithConfiguration(configuration, "shared", true) >> {
            return configuration
        }
        mockUserService.assignGroupOrAssignTo(_, configuration) >> {
            return configuration
        }
        controller.userService = mockUserService
        controller.dispositionService.userService = mockUserService
        controller.aggregateCaseAlertService.userService = mockUserService

        ActionConfiguration actionConfiguration = new ActionConfiguration(value: 'PRV',
                displayName: 'Periodic review',
                isEmailEnabled: true,
                description: 'testing')
        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        mockValidatedSignalService.getActionConfigurationList(_) >> {
            return [actionConfiguration]
        }
        controller.validatedSignalService = mockValidatedSignalService
        SpotfireService mockSpotfireService = Mock(SpotfireService)
        mockSpotfireService.fetchAnalysisFileUrl(executedConfiguration) >> {
            return ['file1', 'file2']
        }

        CacheService cacheService = Mock(CacheService)
        controller.cacheService = cacheService
        cacheService.getSubGroupMap() >> ["AGE_GROUP": [1: 'Adolescent', 2: 'Adult', 3: 'Child', 4: 'Elderly', 5: 'Foetus'], "GENDER": [6: 'AddedNew', 7: 'Confident', 8: 'Female', 9: 'MALE_NEW',]]

        this.aggregateCaseAlertService = Mock(AggregateCaseAlertService)

        this.justification = new Justification(name: "Test Justification", justification: "Test", feature : "feature")
        this.justification.save()
    }

    @Ignore
    void "test create"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        controller.create()
        then:
        response.status == 200
        model.configurationInstance.type == 'Aggregate Case Alert'
        model.priorityList[0]["value"] == "mockPriority"
        model.userList[0]["username"] == "username"
        model.action == "create"

    }
    @Ignore
    void "test listByExecutedConfig without passed values"() {
        when:

        controller.listByExecutedConfig()

        then:
        response.status == 200
        response.json.recordsTotal == 0
        response.json.recordsFiltered == 0
    }

    void "test listByExecutedConfig with passed values"() {
        when:

        controller.listByExecutedConfig(false, 1, false)

        then:
        response.status == 200
        response.json.recordsTotal == 0
        response.json.recordsFiltered == 0
    }
    @Ignore
    void "test for fetchSubGroupsMap"() {
        setup:
        controller.params.dataSource = "pva"

        when:
        controller.fetchSubGroupsMap()

        then:
        response.status == 200
        response.json.subGroupsMap.AGE_GROUP == ['Adolescent', 'Adult', 'Child', 'Elderly', 'Foetus']
        response.json.subGroupsMap.GENDER == ['AddedNew', 'Confident', 'Female', 'MALE_NEW']
        response.json.isAgeEnabled == true
        response.json.isGenderEnabled == true
    }

    void "test for fetchSubGroupsMap when datasource is faers"() {
        setup:
        controller.params.dataSource = "faers"

        when:
        controller.fetchSubGroupsMap()

        then:
        response.status == 200
        response.json.subGroupsMap == [:]
        response.json.isAgeEnabled == false
        response.json.isGenderEnabled == false
    }
    @Ignore
    void "test editShare when exception"() {
        when:
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/review'
    }
    void "test editShare"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration
        }
        controller.CRUDService = mockCRUDService
        when:
        params.sharedWith = "User_${user.id}"
        params.executedConfigId = "1"
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/review'
    }
    @Ignore
    void "test fetchFaersDisabledColumnsIndexes"() {
        when:
        controller.fetchFaersDisabledColumnsIndexes()

        then:
        response.status == 200
        response.json.disabledIndexValues == [3, 5]
    }

    void "test AlertLevelDisposition in case of Exception"() {
        when:
        controller.changeAlertLevelDisposition(disposition, "text", executedConfiguration, false)

        then:
        response.status == 200
        response.json.status == false
        response.json.message == "app.label.disposition.change.error"
    }

    void "test changeAlertLevelDisposition in case of Rows Count equal to zero"() {
        setup:
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.changeAlertLevelDisposition(_) >> {
            return 0
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        when:
        controller.changeAlertLevelDisposition(disposition, "text", executedConfiguration, false)

        then:
        response.status == 200
        response.json.status == true
        response.json.message == "alert.level.review.completed"
    }


    void "test changeAlertLevelDisposition in case of Rows Count greater than zero"() {
        setup:
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.changeAlertLevelDisposition(_) >> {
            return 1
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        NotificationHelper notificationHelper = Mock(NotificationHelper)
        notificationHelper.pushNotification(_) >> {
            return true
        }
        controller.dispositionService.notificationHelper = notificationHelper


        when:
        controller.changeAlertLevelDisposition(disposition, "text", executedConfiguration, false)

        then:
        response.status == 200
        response.json.status == true
        response.json.message == "alert.level.review.completed"
    }

    void "test fetchAllFieldValues with default map"() {
        setup:
        AggregateCaseAlertService aggregateCaseAlertService = Mock(AggregateCaseAlertService)
        aggregateCaseAlertService.fieldListAdvanceFilter(_,_,_,_) >> { [[:]] }
        controller.aggregateCaseAlertService = aggregateCaseAlertService

        params.callingScreen = "dashboard"
        params.executedConfigId = 1L
        when:
        controller.fetchAllFieldValues()

        then:
        response.status == 200
        response.json.size() == 1
        response.json == [[:]]
    }
    @Ignore
    void "test fetchAllFieldValues with value map"() {
        setup:
        AggregateCaseAlertService aggregateCaseAlertService = Mock(AggregateCaseAlertService)

        aggregateCaseAlertService.fieldListAdvanceFilter(_,_,_,_) >> { [[name: "flags", display: "Flags", dataType: 'java.lang.String'],
                                                                        [name: "priority.id", display: "Priority", dataType: 'java.lang.String'],
                                                                        [name: "productName", display: "Product Name", dataType: 'java.lang.String']] }

        controller.aggregateCaseAlertService = aggregateCaseAlertService
        params.callingScreen = "dashboard"
        params.executedConfigId = 1L

        when:
        controller.fetchAllFieldValues()

        then:
        response.status == 200
        response.json.size() == 3
        response.json == [[display:'Flags', dataType:'java.lang.String', name:'flags'], [display:'Product Name', dataType:'java.lang.String', name:'productName'], [display:'Priority', dataType:'java.lang.String', name:'priority.id']]
    }

    void "test fetchAllFieldValues with null coming from service"() {
        setup:

        AggregateCaseAlertService aggregateCaseAlertService = Mock(AggregateCaseAlertService)
        aggregateCaseAlertService.fieldListAdvanceFilter(_) >> {
            return null
        }

        controller.aggregateCaseAlertService = aggregateCaseAlertService
        when:
        controller.fetchAllFieldValues()

        then:
        thrown Exception
    }

    void "test fetchPossibleValues"() {
        setup:
        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.preparePossibleValuesMap(_, _, _) >> {
            return [alertTags: [[id: alert_tag, text: alert_tag]]]
        }
        controller.alertService = mockAlertService

        when:
        controller.fetchPossibleValues(executedConfiguration.id)

        then:
        response.status == 200
        response.json.alertTags == [[id: alert_tag.name, text: alert_tag.name]]
        response.json.listed == [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
        response.json.positiveRechallenge == [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
    }

    void "test archivedAlert"() {
        setup:
        params.start = 0
        params.length = 2
        when:
        controller.archivedAlert(executedConfiguration.id)

        then:
        response.status == 200
    }

    void "test changeDisposition action on success"() {
        setup:
        String selectedRows = '[{"alert.id":1},{"alert.id":2}]'
        Disposition targetDisposition = Disposition.get(1)
        String justification = "change needed"
        String validatedSignalName = "ValidatedSignal2"
        String productJson = "product1"
        String incomingDisposition = 'Threshold Not Met'

        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.changeDisposition([1L, 2L], targetDisposition, justification, validatedSignalName, productJson, false,1,incomingDisposition) >> {
            return true
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        when:
        controller.changeDisposition(selectedRows, targetDisposition, justification, validatedSignalName, productJson,1,incomingDisposition)

        then:
        JSON.parse(response.text).code == 200
        JSON.parse(response.text).status == false
    }

    void "test changeDisposition action when exception occurs"() {
        setup:
        String selectedRows = '[{"alert.id":1},{"alert.id":2}]'
        Disposition targetDisposition = Disposition.get(1)
        String justification = "change needed"
        String validatedSignalName = "ValidatedSignal2"
        String productJson = "product1"
        String incomingDisposition = 'Threshold Not Met'

        when:
        controller.changeDisposition(selectedRows, targetDisposition, justification, validatedSignalName, productJson,1,incomingDisposition)

        then:
        JSON.parse(response.text).code == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).data == null
        JSON.parse(response.text).message == "app.label.disposition.change.error"
    }
    @Ignore
    void "test modelData"() {
        setup:
        params.dataSource = "pva"
        params.selectedDataSource = "pva"
        params.signalId = "1"
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.checkProductNameListExistsForFAERS(_, _) >> {
            return true
        }
        mockAggregateCaseAlertService.getEnabledOptions()>>{
            return [enabledOptions:["PVA"], defaultSelected:""]
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService

        when:
        Map result = controller.modelData(alertConfiguration, "action")

        then:
        result.configurationInstance == alertConfiguration
        result.userList == [user, newUser]
        result.action == "action"
        result.spotfireEnabled == true
    }

    void "test fetchFreqName"() {
        when:
        controller.fetchFreqName("Yearly")

        then:
        response.status == 200
        response.json.frequency == 'Yearly'
    }

    void "test alertTagDetails"() {
        setup:
        AlertTagService mockAlertTagService = Mock(AlertTagService)
        mockAlertTagService.getMartTagsName()>> {
            return [alert_tag.name]
        }

        controller.alertTagService = mockAlertTagService
        when:
        controller.alertTagDetails(alert.id, false)

        then:
        response.status == 200
        response.json.tagList == [alert_tag.name]
        response.json.alertTagList == [alert_tag.name]
    }

    void "test getSubstanceFrequency"() {
        setup:
        params.dataSource = "faers"

        when:
        controller.getSubstanceFrequency("Test Product 1")

        then:
        response.status == 200
        response.json.miningFrequency == 'Yearly'
    }
    @Ignore
    void "test showCharts"() {
        setup:
        def listExe=ExecutedConfiguration.metaClass.static.findAllByConfigIdAndIsEnabled = {Long ,Boolean, Map->
            return [executedConfiguration]
        }
        alert.alertConfiguration = configuration
        alert.save(validate:false)
        params.alertId = alert.id
        when:
        controller.showCharts()
        then:
        response.status == 200
        response.json.studyCount == [1]
        response.json.frequency == 'Yearly'
        response.json.rorValue == [1.0]
        response.json.fatalCount == [1]
        response.json.prrValue == [1.0]
    }
    @Ignore
    void "test showCharts when alertid absent"() {
        setup:
        alert.alertConfiguration = configuration
        alert.save(validate:false)
        params.alertId = null
        when:
        controller.showCharts()
        then:
        response.status == 200
        response.json.studyCount == []
        response.json.isRor == null
        response.json.rorvalue == null
        response.json.sponCount == null
        response.json.prrValue == []
        response.json.fatalCount == []
        response.json.frequency == ""

    }
    void "test fetchStratifiedScores in case of PRR"() {
        setup:
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.getDomainObject(_) >> {
            return AggregateCaseAlert
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.PRR, false)

        then:
        response.status == 200
        response.json.PRR == 1.0
        response.json."PRR(MH)" == "1"
    }

    void "test fetchStratifiedScores in case of ROR"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.ROR, false)

        then:
        response.status == 200
        response.json."ROR(MH)" == "1"
        response.json.ROR == 1.0
    }

    void "test fetchStratifiedScores in case of PRRLCI"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.PRRLCI, false)

        then:
        response.status == 200
        response.json."PRR(MH)" == "1"
        response.json.PRRLCI == 1.0
    }

    void "test fetchStratifiedScores in case of PRRUCI"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.PRRUCI, false)

        then:
        response.status == 200
        response.json."PRR(MH)" == "1"
        response.json.PRRUCI == 1.0
    }

    void "test fetchStratifiedScores in case of ROR05"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.RORLCI, false)

        then:
        response.status == 200
        response.json."ROR(MH)" == "1"
        response.json.RORLCI == 1.0
    }

    void "test fetchStratifiedScores in case of ROR95"() {
        when:
        controller.fetchStratifiedScores(alert.id, Constants.Stratification_Fields.RORUCI, false)

        then:
        response.status == 200
        response.json."ROR(MH)" == "1"
        response.json.RORUCI == 1.0
    }

    void "test fetchStratifiedScores in case of error"() {
        when:
        controller.fetchStratifiedScores(alert.id, null, false)

        then:
        response.status == 200
        response.json.ErrorMessage == "app.label.stratification.values.error"
    }

    void "test pecTreeJson"() {
        when:
        controller.pecTreeJson()

        then:
        response.status == 200
        JSON.parse(response.text) != null
    }

    void "test showTrendAnalysis faers true"() {
        when:
        params.alertId = alert.id
        controller.showTrendAnalysis(true)

        then:
        response.status == 302
        response.redirectedUrl == '/trendAnalysis/showTrendAnalysis?type=Aggregate+Case+Alert&isFaers=true'
    }
    void "test showTrendAnalysis not fears"() {
        when:
        params.alertId = alert.id
        controller.showTrendAnalysis(false)

        then:
        response.status == 302
        response.redirectedUrl == '/trendAnalysis/showTrendAnalysis?type=Aggregate+Case+Alert&isFaers=false'
    }
    void "test listConfig"() {
        when:
        controller.listConfig()

        then:
        response.status == 200
    }

    void "test getListWithoutFilter with Dashboard and true"() {
        when:
        params.callingScreen = Constants.Commons.DASHBOARD
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with Dashboard and false"() {
        when:
        params.callingScreen = Constants.Commons.DASHBOARD
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with Review and true"() {
        when:
        params.callingScreen = Constants.Commons.REVIEW
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with Review and false"() {
        when:
        params.callingScreen = Constants.Commons.REVIEW
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with TriggeredAlerts and true"() {
        when:
        params.callingScreen = Constants.Commons.TRIGGERED_ALERT
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with TriggeredAlerts and false"() {
        when:
        params.callingScreen = Constants.Commons.TRIGGERED_ALERT
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with ec and showClosed true"() {
        when:
        List result = controller.getListWithoutFilter(executedConfiguration, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter without ec and showClosed true"() {
        when:
        List result = controller.getListWithoutFilter(null, true)

        then:
        result == [alert]
    }

    void "test getListWithoutFilter with ec and showClosed false"() {
        when:
        List result = controller.getListWithoutFilter(executedConfiguration, false)

        then:
        result == [alert]
    }

    void "test toggleFlag"() {
        when:
        params.id = alert.id
        controller.toggleFlag()

        then:
        response.status == 200
        response.json.flagged == true
        response.json.success == 'ok'
    }

    void "test toggleFlag in case of null id"() {
        when:
        params.id = null
        controller.toggleFlag()

        then:
        response.status == 404
    }

    void "test changeAssignedToGroup in case of Exception"() {
        setup:
        String selectedId = '["1"]'
        UserService mockUserService = Mock(UserService)
        mockUserService.getAssignedToName(alert) >> {
            return "Alert A"
        }
        mockUserService.getUserListFromAssignToGroup(alert) >> {
            return [user, newUser]
        }
        mockUserService.assignGroupOrAssignTo(alert) >> {
            return alert
        }
        controller.userService = mockUserService

        AlertFieldService alertFieldService = Mock(AlertFieldService)
        alertFieldService.getAlertFields(_,_,_,_)>> {
            return [[ type:"SAFETY"],[type:"FAERS"]]
        }
        controller.alertFieldService = alertFieldService
        controller.aggregateCaseAlertService = [getActivityByType: { _ -> }]
        controller.aggregateCaseAlertService = [getDomainObject : { _ ->AggregateCaseAlert}]

        when:
        controller.changeAssignedToGroup(selectedId, "Assigned", false)

        then:
        response.status == 200
        response.json.status == false
        response.json.message == 'app.assignedTo.changed.fail'
    }

    void "test changeAssignedToGroup"() {
        setup:
        params.controller = 'aggregateCaseAlert'
        List newUserList = [user, newUser]
        List oldUserList = [user, newUser]
        String newEmailMessage = 'new email message'
        String oldEmailMessage = 'old email message'
        List emailDataList  = []
        Map peHistoryMap = [
                "justification"   : '',
                "change"          : Constants.HistoryType.ASSIGNED_TO,
                cumFatalCount           : alert.cumFatalCount,
                cumSeriousCount         : alert.cumSeriousCount,
                cumSponCount            : alert.cumSponCount,
                cumStudyCount           : alert.cumStudyCount,
                newFatalCount           : alert.newFatalCount,
                newSeriousCount         : alert.newSeriousCount,
                newSponCount            : alert.newSponCount,
                newStudyCount           : alert.newStudyCount,
                positiveRechallenge     : alert.positiveRechallenge,
                "productName"           : alert.productName,
                "eventName"             : alert.pt,
                "prrValue"              : alert.prrValue,
                "rorValue"              : alert.rorValue,
                "ebgm"                  : alert.ebgm,
                "eb05"                  : alert.eb05,
                "asOfDate"              : alert.periodEndDate,
                "assignedTo"            : alert.assignedTo,
                "assignedToGroup"       : alert.assignedToGroup,
                "disposition"           : alert.disposition,
                "eb95"                  : alert.eb95,
                "executionDate"         : alert.dateCreated,
                "createdBy"             : user?.fullName,
                "modifiedBy"            : user?.fullName,
                "aggCaseAlertObj"       : alert.id,
                "archivedAggCaseAlertId":  null,
                "aggCaseAlertId"        : alert.id,
                "execConfigId"          : alert.executedAlertConfigurationId,
                "configId"              : alert.alertConfigurationId,
                "priority"              : alert.priority,
                "isLatest"              : true,
                "dueDate"               : alert.dueDate
        ]
        def dashboardCountDTO = (["dispCountKey" :Constants.UserDashboardCounts.USER_DISP_PECOUNTS,"dueDateCountKey":Constants.UserDashboardCounts.USER_DUE_DATE_PECOUNTS,"groupDispCountKey":Constants.UserDashboardCounts.GROUP_DISP_PECOUNTS,"groupDueDateCountKey":Constants.UserDashboardCounts.GROUP_DUE_DATE_PECOUNTS])
        String selectedId = '[1]'
        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.prepareDashboardCountDTO(_) >> {
            return dashboardCountDTO
        }
        controller.alertService = mockAlertService
        ActivityService mockActivityService = Mock(ActivityService)
        mockActivityService.createActivity(_,_,_,_,_,_,_,_,_,_,_,_,_) >> {}
        controller.activityService = mockActivityService
        UserService mockUserService = Mock(UserService)
        mockUserService.getAssignToValue(alert) >> {
            return [[ type:"SAFETY"],[type:"FAERS"]]
        }
        mockUserService.getAssignedToName(alert) >> {
            return "Alert A"
        }
        mockUserService.getUserListFromAssignToGroup(alert) >> {
            return [user, newUser]
        }
        mockUserService.assignGroupOrAssignTo(_,_,_,_,_) >> {
            return alert
        }
        mockUserService.generateEmailDataForAssignedToChange(newEmailMessage, newUserList, oldEmailMessage, oldUserList) >> {
            return [user: newUser, emailMessage: newEmailMessage]

        }
        controller.userService = mockUserService
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.AssignedToChange)
        activityType.save(flush: true, failOnError: true)
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.getAlertConfigObject(executedConfiguration.name, executedConfiguration.owner) >> {
            return 1L
        }
        mockAggregateCaseAlertService.createPEHistoryMapForAssignedToChange(alert, configuration.id, false) >> {
            return peHistoryMap
        }
        mockAggregateCaseAlertService.getActivityByType(ActivityTypeValue.AssignedToChange) >> {
            return activityType
        }
        mockAggregateCaseAlertService.getDomainObject(false) >> {
            return AggregateCaseAlert
        }
        mockAggregateCaseAlertService.sendMailForAssignedToChange(emailDataList, alert, false) >> {}
        controller.aggregateCaseAlertService = mockAggregateCaseAlertService
        EmailNotificationService mockService = Mock(EmailNotificationService)
        mockService.emailNotificationWithBulkUpdate(_, _) >> {
            return true
        }
        AlertFieldService alertFieldService = Mock(AlertFieldService)
        alertFieldService.getAlertFields(_,_,_,_)>> {
            return [[ type:"SAFETY"],[type:"FAERS"]]
        }
        controller.alertFieldService = alertFieldService
        controller.emailNotificationService = mockService
        executedConfiguration.configId = 2l
        executedConfiguration.save(validate:false)
        when:
        controller.changeAssignedToGroup(selectedId, "Constants.USER_GROUP_TOKEN", false)

        then:
        response.status == 200
    }

    void "test changePriorityOfAlert in case of Exception"() {
        setup:
        String selectedRows = '[["alert.id":1],["executedConfigObj.id":1],["configObj.id":1]]'

        when:
        controller.changePriorityOfAlert(selectedRows, priorityNew, "justification", false)

        then:
        response.status == 200
        response.json.status == false
        response.json.message == "app.label.priority.change.error"
    }

    void "test changePriority in case of Exception"() {
        when:
        String alertList = '[["alert.id":1]]'
        controller.changePriority(executedConfiguration.id, "newPriority", "justified", alertList)

        then:
        response.status == 400
    }

    void "test setNextRunDateAndScheduleDateJSON in case of null"() {
        when:
        controller.setNextRunDateAndScheduleDateJSON(configuration)

        then:
        configuration.nextRunDate == null
    }

    void "test setNextRunDateAndScheduleDateJSON"() {
        setup:
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService

        when:
        controller.setNextRunDateAndScheduleDateJSON(configuration)

        then:
        configuration.nextRunDate == new DateTime(2015, 12, 15, 0, 0, 0).toDate()
    }
    @Ignore
    void "test copy in case of null config"() {
        when:
        controller.copy(null)

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test copy"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        alertConfiguration.alertDateRangeInformation = null
        alertConfiguration.save(validate:false)
        when:
        controller.copy(alertConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/create'
    }
    @Ignore
    void "test viewExecutedConfig in case of null"() {
        when:
        controller.viewExecutedConfig(null)

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test viewExecutedConfig"() {
        setup:
        Map map_EBGM =["pva": [age: [], gender: [], receiptYear: [], ageSubGroup: [], genderSubGroup: [], "isEBGM": false],"faers":["isEBGM": false]]
        Map map_PRR = ["pva": [age: [], gender: [], receiptYear: [], ageSubGroup: [], genderSubGroup: [], "isPRR": false]]
        AggregateCaseAlertService mockService = Mock(AggregateCaseAlertService)
        mockService.getStratificationValuesDataMiningVariables(_,_)>> {
            return [map_EBGM: map_EBGM, map_PRR: map_PRR]
        }
        controller.aggregateCaseAlertService = mockService
        when:
        controller.viewExecutedConfig(executedConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/view'
        model.isExecuted == true
    }
    @Ignore
    void "test delete in case of null"() {
        when:
        controller.delete(null)

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        flash.message == 'default.not.found.message'
    }



    void "test delete in case of not owner"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }

        when:
        controller.delete(configuration)

        then:
        controller.flash.warn == 'app.configuration.delete.fail'
        response.status == 302
        response.redirectedUrl == '/configuration/index'
    }

    void "test review"() {
        when:
        controller.review()

        then:
        response.status == 200
        view == '/aggregateCaseAlert/review'
    }

    void "test viewConfig in case of null id"() {
        when:
        params.id = null
        controller.viewConfig()

        then:
        controller.flash.error == "app.configuration.id.null"
        response.status == 302
        response.redirectedUrl == '/configuration/listAllResults'
    }

    void "test viewConfig in case of other params than result"() {
        when:
        params.id = 1
        params.from = 'notResult'
        controller.viewConfig()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/view/1'
    }

    void "test viewConfig"() {
        when:
        params.id = 1
        params.from = 'result'
        controller.viewConfig()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/viewExecutedConfig/1'
    }

    void "test populateModel"() {
        setup:
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration
        }
        controller.userService = mockUserService
        params.alertDateRangeInformation = alertDateRangeInformation
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.missedCases = false
        params.selectedDatasource = "pva"
        params.productSelection = "Test Product A"
        params.assignedToValue = "assigned"
        params.sharedWith = "shared"

        when:
        Configuration result = controller.populateModel(configuration)

        then:
        result.productGroupSelection == '{["name":"product group","id":1]}'
        result.eventGroupSelection == '{["name":"event group","id":1]}'
        result.missedCases == false
    }


    void "test listAllResults"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }

        when:
        controller.listAllResults()

        then:
        response.status == 200
        view == '/aggregateCaseAlert/executionStatus'
    }
    @Ignore
    void "test renderOnErrorScenario"() {
        when:
        params.previousAction = Constants.AlertActions.CREATE
        params.id = 1
        controller.renderOnErrorScenario(alertConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/edit'
    }
    @Ignore
    void "test renderOnErrorScenario in case of no id"() {
        when:
        params.previousAction = Constants.AlertActions.CREATE
        params.id = null
        controller.renderOnErrorScenario(alertConfiguration)

        then:
        response.status == 200
        view == '/aggregateCaseAlert/create'
    }

    @Ignore
    void "test runOnce in case of null id"() {
        when:
        params.id = null
        params.dataMiningVariable = 'Gender'
        controller.runOnce()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test runOnce in case of isEnabled true"() {
        when:
        params.id = 1
        params.dataMiningVariable = 'Gender'
        controller.runOnce()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        controller.flash.warn == 'app.configuration.run.exists'
    }

    void "test runOnce"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        Configuration configurationNew = new Configuration(
                executing: false,
                template: reportTemplate,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                missedCases: false,
                selectedDatasource: "pva",
                name: "test",
                spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                productSelection: "Test Product A",
                type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                createdBy: newUser.username, modifiedBy: newUser.username,
                assignedTo: newUser,
                drugType: '6267,6219,6283',
                priority: priority,
                owner: newUser,
                isEnabled: false,
                alertDateRangeInformation: alertDateRangeInformation,
                nextRunDate: null
        )
        configurationNew.save(flush: true)
        params.dataMiningVariable = 'Batch'
        params.id = configurationNew.id

        when:
        controller.runOnce()

        then:
        response != null
    }

    void "test getRunOnceScheduledDateJson"() {
        when:
        String result = controller.getRunOnceScheduledDateJson()

        then:
        result.contains('{"name" :"UTC","offset" : "+00:00"}') == true
    }
    @Ignore
    void "test edit in case of null instance"() {
        when:
        controller.edit(null)

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test edit"(){
        given:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        ReportExecutorService mockReportExecutorService = Mock(ReportExecutorService)
        mockReportExecutorService.currentlyQuantRunning >> {
            return [1L,2L]
        }
        controller.reportExecutorService = mockReportExecutorService
        when:
        controller.edit(configuration)

        then:
        view == '/aggregateCaseAlert/edit'
        response.status == 200
    }
    @Ignore
    void "test edit when reportExecutorService returns true"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.getUser() >> {
            return newUser
        }
        controller.userService = mockUserService

        ReportExecutorService reportExecutorService = Mock(ReportExecutorService)
        reportExecutorService.currentlyQuantRunning >> {
            return [configuration.id]
        }
        controller.reportExecutorService = reportExecutorService

        when:
        controller.edit(configuration)
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/index"
        flash.warn == "app.configuration.running.fail"
    }

    void "test edit when configuration is not editable by current user"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }
        SpringSecurityUtils.metaClass.static.ifAllGranted = { String role ->
            return false
        }
        UserService userService = Mock(UserService)
        userService.getUser() >> {
            return user
        }
        controller.userService = userService
        ReportExecutorService reportExecutorService = Mock(ReportExecutorService)
        reportExecutorService.currentlyQuantRunning >> {
            return []
        }
        controller.reportExecutorService = reportExecutorService
        when:
        controller.edit(configuration)
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/index"
        flash.warn == "app.configuration.edit.permission"
    }
    @Ignore
    void "test details method"() {
        setup:
        def map = [viewdList: [], selectedViewInstance: null]
        ViewInstanceService viewInstanceServiceMock = Mock(ViewInstanceService)

        viewInstanceServiceMock.fetchViewsListAndSelectedViewMap(_, _) >> {
            return map
        }
        controller.viewInstanceService = viewInstanceServiceMock

        WorkflowRuleService workflowRuleService = Mock(WorkflowRuleService)
        workflowRuleService.fetchDispositionIncomingOutgoingMap() >> {
            return null
        }
        controller.workflowRuleService = workflowRuleService

        PriorityService priorityService = Mock(PriorityService)

        priorityService.listPriorityOrder() >> {
            return null
        }

        controller.priorityService = priorityService

        AlertService alertService = Mock(AlertService)

        alertService.listPriorityOrder() >> {
            return null
        }

        alertService.getActionTypeAndActionMap() >> {
            return [actionTypeList: null, actionPropertiesMap: null]
        }

        alertService.generateSearchableColumns(_,_,_) >> {
            List<Integer> filterIndex = []
            Map<Integer, String> filterIndexMap = [:]
            return [filterIndex, filterIndexMap]
        }

        controller.alertService = alertService

        aggregateCaseAlertService.fieldListAdvanceFilter(true,'AGGREGATE_CASE_ALERT') >> {
            return ["vaers"]
        }

        controller.aggregateCaseAlertService = aggregateCaseAlertService

        DispositionService dispositionService = Mock(DispositionService)
        dispositionService.getReviewCompletedDispositionList() >> {
            return ["ValidatedSignal"]
        }

        controller.dispositionService = dispositionService
        params.callingScreen = "review"
        when:
        controller.details()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        model.fieldList == null
    }

    void "test fetchMiningVariables"(){
        setup:
        CacheService mockCache = Mock(CacheService)
        mockCache.getMiningVariables("pva")>>{
            return [GENDER: [label:"Gender"]]
        }
        controller.cacheService = mockCache
        String selectedDatasource = "pva"
        when:
        controller.fetchMiningVariables(selectedDatasource)
        then:
        response.status == 200
    }

    void "test emergingIssues"(){
        when:
        controller.emergingIssues()
        then:
        response.status == 200
        view == '/aggregateCaseAlert/emergingIssues'
    }

    void "test statisticalComparison"(){
        when:
        controller.statisticalComparison()
        then:
        response.status == 200
        view == '/statisticalComparison/statisticalComparison'
    }

    void "test getEndDate"() {
        when:
        def result1 = controller.getEndDate("01-006-2023 - 04-007-2023")
        def result2 = controller.getEndDate(null)
        then:
        result1 == "04/007/23"
        result2 == null
    }
    @Ignore
    void "test view in case of null"() {
        when:
        controller.view(null)
        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test view"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        controller.view(configuration)
        then:
        response.status == 200
        view == '/aggregateCaseAlert/view'
    }

    void "test getDataMiningVariableLabel for pva dataSource"(){
        setup:
        params.dataMiningVariable = "Gender;false;false;true;true"
        params.selectedDatasource = "pva"

        CacheService mockCache = Mock(CacheService)
        mockCache.getMiningVariables("pva")>>{
            return [GENDER: [label:"Gender"]]
        }
        controller.cacheService = mockCache
        when:
        controller.getDataMiningVariableLabel(params)
        then:
        response.status == 200
    }

    void "test getDataMiningVariableLabel for faers dataSource"(){
        setup:
        params.dataMiningVariable = "Gender;false;false;true;true"
        params.selectedDatasource = "faers"

        CacheService mockCache = Mock(CacheService)
        mockCache.getMiningVariables("pva")>>{
            return [GENDER: [label:"Gender"]]
        }
        controller.cacheService = mockCache
        when:
        controller.getDataMiningVariableLabel(params)
        then:
        response.status == 200
    }

    void "test getDataMiningVariableLabel for vaers dataSource"(){
        setup:
        params.dataMiningVariable = "Gender;false;false;true;true"
        params.selectedDatasource = "vaers"

        CacheService mockCache = Mock(CacheService)
        mockCache.getMiningVariables("pva")>>{
            return [GENDER: [label:"Gender"]]
        }
        controller.cacheService = mockCache
        when:
        controller.getDataMiningVariableLabel(params)
        then:
        response.status == 200
    }
    @Ignore
    void "test run when content type is form"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration
        }
        mockUserService.getUser() >> {
            return newUser
        }
        controller.userService = mockUserService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration
        }
        mockCRUDService.updateWithAuditLog(_)>>{
            return configuration
        }
        controller.CRUDService=mockCRUDService

        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.dataMiningVariable = "Gender;false;false;true;true"
        params.id = configuration.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration.adhocRun = true
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/executionStatus?alertType=AGGREGATE_CASE_ALERT"
        flash.message == 'app.Configuration.RunningMessage'
    }

    void "test run when content type is not form"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration
        }
        mockUserService.getUser() >> {
            return newUser
        }
        controller.userService = mockUserService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration
        }
        mockCRUDService.updateWithAuditLog(_)>>{
            return configuration
        }
        controller.CRUDService=mockCRUDService

        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.dataMiningVariable = "Gender;false;false;true;true"
        params.id = configuration.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 201
    }

    void "test run when params.id is not given"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration
        }
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.getUser() >> {
            return newUser
        }
        controller.userService = mockUserService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration
        }
        controller.CRUDService = mockCRUDService
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.dataMiningVariable = "Gender;false;false;true;true"
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 201
    }
    @Ignore
    void "test run"(){
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        params.dataMiningVariable = "Gender;false;false;true;true"
        params.selectedDatasource = "pva"
        params.previousAction = 'create'
        params.id = 1
        params.name = 'name'
        params.repeatExecution = 'true'
        params.reviewPeriod = '1'
        when:
        controller.run()
        then:
        response.status == 200
    }
    void "test bindSelectedDatasourceforinstanceofString"(){
        when:
        controller.bindSelectedDatasource("pva",configuration)
        then:
        configuration.selectedDatasource == "pva"
        response.status == 200
    }

    void "test bindSelectedDatasourceforinstanceofString[]"(){
        setup:
        String[] selectedDatasource = ["faers","vaers"]
        configuration.selectedDatasource = selectedDatasource
        when:
        controller.bindSelectedDatasource(selectedDatasource,configuration)
        then:
        response.status == 200
        configuration.selectedDatasource == "faers,vaers"
    }
    @Ignore
    void "test disable in case of null"(){
        setup:
        params.id = null
        when:
        controller.disable()
        then:
        response.status == 302
        response.redirectedUrl == '/aggregateCaseAlert/index'
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test disable"(){
        setup:
        Configuration.metaClass.static.get = {Map ->
            return configuration
        }
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration
        }
        controller.userService = mockUserService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration
        }
        controller.CRUDService = mockCRUDService

        when:
        controller.disable()
        then:
        controller.flash.message == 'default.disabled.message'
        response.redirectedUrl == '/aggregateCaseAlert/view'
        response.status == 302
    }
    @Ignore
    void "test enable in case of null"(){
        setup:
        params.id = null
        Configuration.metaClass.static.get = {Map ->
            return null
        }

        when:
        controller.enable()
        then:
        response.status == 302
    }
    @Ignore
    void "test enable"(){
        setup:
        Configuration.metaClass.static.get = {Map ->
            return configuration
        }
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration
        }
        controller.userService = mockUserService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {
            return configuration
        }
        controller.CRUDService = mockCRUDService

        params.alertDateRangeInformation = alertDateRangeInformation
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.missedCases = false
        params.selectedDatasource = "pva"
        params.productSelection = "Test Product A"
        params.assignedToValue = "assigned"
        params.sharedWith = "shared"

        when:
        controller.enable()
        then:
        controller.flash.message == 'default.enabled.message'
        response.redirectedUrl == '/aggregateCaseAlert/view'
        response.status == 302
    }

    void "test getPublicForExecutedConfig for public"(){
        setup:
        params.id = 1
        when:
        def result = controller.getPublicForExecutedConfig()
        then:
        assert result == "app.label.public"
        response.status == 200
    }

    void "test getPublicForExecutedConfig for private"(){
        setup:
        params.id = 1
        ExecutedConfiguration.get(params.id).isPublic = false
        when:
        def result = controller.getPublicForExecutedConfig()
        then:
        assert result == "app.label.private"
        response.status == 200
    }
    @Ignore
    void "test notFound"(){
        setup:
        params.id = 1
        when:
        controller.notFound()
        then:
        response.status == 302
        controller.flash.message == 'default.not.found.message'
        response.redirectedUrl == '/aggregateCaseAlert/index'
    }
    @Ignore
    void "test renderReportOutputType"(){
        given:
        params.outputFormat ="pdf"
        def dynamicReportService = Mock(DynamicReportService)
        dynamicReportService.getContentType(ReportFormat.PDF)
        controller.dynamicReportService = dynamicReportService
        when:
        controller.renderReportOutputType(file)
        then:
        noExceptionThrown()
    }

    void "test isExcelExportFormat for PDF"(){
        setup:
        String outputFormat ="PDF"
        when:
        boolean result = controller.isExcelExportFormat(outputFormat)
        then:
        result == false
    }
    void "test isExcelExportFormat for XLSX"(){
        setup:
        String outputFormat ="XLSX"
        when:
        boolean result = controller.isExcelExportFormat(outputFormat)
        then:
        result == true
    }

    void "test getListWithFilter"(){
        when:
        List result = controller.getListWithFilter(executedConfiguration)
        then:
        response.status == 200
        assert result == [alert]
    }

    void "test fetchMiningVariables for faers"(){
        setup:
        CacheService mockCache = Mock(CacheService)
        mockCache.getMiningVariables("faers")>>{
            return [GENDER: [label:"Gender"]]
        }
        controller.cacheService = mockCache
        String selectedDatasource = "faers"
        when:
        controller.fetchMiningVariables(selectedDatasource)
        then:
        response.status == 200
    }

    void "test list"(){
        when:
        controller.list()
        then:
        response.status == 200
        response.json.id == [1]
    }

    void "test caseDrillDown"(){
        given:
        def id = 1
        def type = "testType"
        def typeFlag = "testFlag"
        def executedConfigId = 123
        def productId = 456
        def caseDrillDown = 789
        AggregateCaseAlertService mockAggregateCaseAlert = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlert.caseDrillDown(_,_,_,_,_,_)>>{
            return [:]
        }
        controller.aggregateCaseAlertService = mockAggregateCaseAlert
        when:
        controller.caseDrillDown(id,type,typeFlag,executedConfigId,productId,caseDrillDown)
        then:
        response.status == 200
    }

    void "test deleteAttachment"(){
        setup:
        def attachmentId= 1
        def alertId = alert.id
        def isArchived = true
        AttachmentableService mockattachmentableService=Mock(AttachmentableService)
        mockattachmentableService.removeAttachment(attachmentId)>>{
            Attachment.get(1L).delete()
            return true
        }
        controller.attachmentableService= mockattachmentableService
        when:
        controller.deleteAttachment(alertId,attachmentId,isArchived)
        then:
        response.status==200
    }

    void "test deleteAttachment for null"(){
        setup:
        def attachmentId= null
        def alertId = null
        def isArchived = null
        when:
        controller.deleteAttachment(alertId,attachmentId,isArchived)
        then:
        response.json.success == false
        response.status==200
    }

    void "test revertDisposition"(){
        setup:
        params.callingScreen = "dashboard"
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.revertDisposition(_,_)>>{
            return [:]
        }
        mockAggregateCaseAlertService.persistAlertDueDate(_)>>{
            return true
        }
        controller.aggregateCaseAlertService= mockAggregateCaseAlertService
        when:
        controller.revertDisposition(1,"test")
        then:
        response.status == 200
    }

    void "test getDefaultReviewPeriod"(){
        when:
        def result = controller.getDefaultReviewPeriod(configuration)
        then:
        response.status == 200
        result == 1
    }

    void "fetchSubGroupsMapIntegratedReview"(){
        setup:
        CacheService cacheService = Mock(CacheService)
        controller.cacheService = cacheService
        cacheService.getSubGroupMap() >> ["AGE_GROUP": [1: 'Adolescent', 2: 'Adult', 3: 'Child', 4: 'Elderly', 5: 'Foetus'], "GENDER": [6: 'AddedNew', 7: 'Confident', 8: 'Female', 9: 'MALE_NEW',]]
        AlertFieldService alertFieldService = Mock(AlertFieldService)
        controller.alertFieldService = alertFieldService
        alertFieldService.getAlertFields(_) >> []
        when:
        controller.fetchSubGroupsMapIntegratedReview()
        then:
        JSON.parse(response.text) != null
        response.status == 200
    }

    void "test delete in case of owner"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        ConfigurationService mockconfigurationService = Mock(ConfigurationService)
        mockconfigurationService.deleteConfig(_)>>{
            return configuration
        }
        controller.configurationService= mockconfigurationService

        when:
        controller.delete(alertConfiguration)

        then:
        controller.flash.message == 'app.configuration.delete.success'
        response.status == 302
        response.redirectedUrl == '/configuration/index'
    }

    void "test listEmergingIssues"(){
        setup:
        params.filterApplied = false
        params.specialPE = true
        EmergingIssueService mockemergingIssueService = Mock(EmergingIssueService)
        mockemergingIssueService.getEmergingIssues()>>{
            ["event11", "event12"]
        }
        controller.emergingIssueService= mockemergingIssueService
        when:
        controller.listEmergingIssues()
        then:
        response.status == 200
    }

    void "test listEmergingIssues with filterApplied true"(){
        params.filterApplied = true
        params.specialPE = true
        EmergingIssueService mockemergingIssueService = Mock(EmergingIssueService)
        mockemergingIssueService.getEmergingIssues()>>{
            ["event11", "event12"]
        }
        controller.emergingIssueService= mockemergingIssueService
        when:
        controller.listEmergingIssues()
        then:
        response.status == 200
    }

    void "test getAggregateRuleJson"(){
        when:
        controller.getAggregateRuleJson()
        then:
        response.status == 200
    }

    void "test getFilteredAggAlerts"(){
        setup:
        String timezone = "UTC"
        List list = []
        params.specialPE = true
        when:
        controller.getFilteredAggAlerts(list,timezone)
        then:
        response.status == 200
    }
    @Ignore
    void "test save"(){
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration
        }
        controller.userService = mockUserService
        params.dataMiningVariable = "Gender;false;false;true;null;null;true"
        params.selectedDatasource = "pva"
        params.assignedToValue = "assigned"
        params.repeatExecution = "false"
        params.reviewPeriod = "1"
        params.name = "newName"
        CacheService mockCache = Mock(CacheService)
        mockCache.getMiningVariables("pva")>>{
            return [GENDER: [label:"Gender"]]
        }
        controller.cacheService = mockCache
        when:
        controller.save()
        then:
        response.status == 200
    }

    void "test saveAlertTags"(){
        setup:
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        controller.CRUDService.userService.springSecurityService=springSecurityService
        params.justification = "changed name"
        Long executedConfigId = 1
        Long alertId = alert.id
        boolean isArchived = true
        params.alertTags = '["tag1", "tag2", "tag3"]'
        ActivityService mockActivityService = Mock(ActivityService)
        mockActivityService.createActivity(_,_,_,_,_,_,_,_,_,_,_,_,_) >> {}
        controller.activityService = mockActivityService
        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.getDomainObject(_) >> {
            return AggregateCaseAlert
        }
        mockAggregateCaseAlertService.createBasePEHistoryMap(_,_,_)>>{
            return [customProperty: 'customProperty']
        }
        controller.aggregateCaseAlertService= mockAggregateCaseAlertService
        when:
        controller.saveAlertTags(executedConfigId,alertId,isArchived)
        then:
        response.status == 200
    }

    void "test getVigibaseLatestQuarter"(){
        given:
        params.selectedDatasource = "vigibase"

        def mockedSource = Mock(DataSource)
        controller.dataSource_vigibase = mockedSource
        String date = "[VIGIBASE_DATE:27-08-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        when:
        controller.getVigibaseLatestQuarter()
        then:
        notThrown(Exception)
    }

    void "test getFaersLatestQuarter"(){
        setup:
        def mockedSource = Mock(DataSource)
        controller.dataSource_faers = mockedSource
        String date = "[FAERS_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        params.selectedDatasource = "faers"

        when:
        controller.getFaersLatestQuarter()
        then:
        notThrown(Exception)
    }

    void "test getEvdasAndFaersDateRange for faers dataSource"(){
        given:
        params.selectedDatasource = "faers"
        params.productGroupId = "1"
        def mockedSource = Mock(DataSource)
        controller.dataSource_faers = mockedSource
        String date = "[FAERS_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        when:
        controller.getEvdasAndFaersDateRange()
        then:
        notThrown(Exception)
    }

    void "test getEvdasAndFaersDateRange for evdas dataSource"(){
        given:
        params.selectedDatasource = "evdas"
        params.productGroupId = "1"
        def mockedSource = Mock(DataSource)
        controller.dataSource_eudra = mockedSource
        String date = "[EVDAS_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        when:
        controller.getEvdasAndFaersDateRange()
        then:
        notThrown(Exception)
    }

    void "test getVaersDateRange"() {
        given:
        DataSource mockedSource = Mock(DataSource)
        controller.dataSource_vaers = mockedSource
        String date = "[LATEST_UPLOADED_DATE_RANGE:01-01-2024/28-06-2024]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        Map dateMap = [
                '01-01-2024': new DateTime(2024, 1, 1, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                '28-06-2024': new DateTime(2024, 6, 28, 0, 0, DateTimeZone.forID('UTC')).toDate()
        ]
        DateUtil.metaClass.static.StringToDate = { String strDate, String format ->
            return dateMap[strDate] ?: new DateTime(1970, 1, 1, 0, 0, DateTimeZone.forID('UTC')).toDate()
        }

        when:
        controller.getVaersDateRange()

        then:
        Map responseJson = new JsonSlurper().parseText(response.text)

        and:
        responseJson.data == '01-Jan-2024 to 28-Jun-2024'
        responseJson.status == true
    }
    void "test getVaersDateRange when date is empty"() {
        given:
        DataSource mockedSource = Mock(DataSource)
        controller.dataSource_vaers = mockedSource
        String date = "[LATEST_UPLOADED_DATE_RANGE:/]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        when:
        controller.getVaersDateRange()

        then:
        Map responseJson = new JsonSlurper().parseText(response.text)

        and:
        Date today = new Date()
        SimpleDateFormat sdf = new SimpleDateFormat('dd-MMM-yyyy')
        String formattedDate = sdf.format(today)
        responseJson.data == formattedDate + ' to ' + formattedDate
        responseJson.status == true
    }

    void "test getEvdasAndVigibaseDateRange for evdas dataSource"(){
        given:
        params.selectedDatasource = "evdas"
        params.productGroupId = "1"
        def mockedSource = Mock(DataSource)
        controller.dataSource_eudra = mockedSource
        String date = "[EVDAS_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        when:
        controller.getEvdasAndVigibaseDateRange()
        then:
        notThrown(Exception)
    }

    void "test getEvdasAndVigibaseDateRange for vigibase dataSource"(){
        given:
        params.selectedDatasource = "vigibase"
        params.productGroupId = "1"
        def mockedSource = Mock(DataSource)
        controller.dataSource_vigibase = mockedSource
        String date = "[VIGIBASE_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        when:
        controller.getEvdasAndVigibaseDateRange()
        then:
        notThrown(Exception)
    }

    void "test getVigibaseEvdasAndFaersDateRange for vigibase dataSource"(){
        given:
        params.selectedDatasource = "vigibase"
        params.productGroupId = "1"
        def mockedSource = Mock(DataSource)
        controller.dataSource_vigibase = mockedSource
        String date = "[VIGIBASE_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        DateUtil.metaClass.static.StringToDate = { String strDate,String format -> return new DateTime(2015, 1, 5, 8, 0, DateTimeZone.forID('UTC')).toDate()}
        when:
        controller.getVigibaseEvdasAndFaersDateRange()
        then:
        notThrown(Exception)
    }

    void "test getVigibaseEvdasAndFaersDateRange for evdas dataSource"(){
        given:
        params.selectedDatasource = "evdas"
        params.productGroupId = "1"
        def mockedSource = Mock(DataSource)
        controller.dataSource_eudra = mockedSource
        String date = "[EVDAS_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        DateUtil.metaClass.static.StringToDate = { String strDate,String format -> return new DateTime(2015, 1, 5, 8, 0, DateTimeZone.forID('UTC')).toDate()}
        when:
        controller.getVigibaseEvdasAndFaersDateRange()
        then:
        notThrown(Exception)
    }

    void "test getVigibaseEvdasAndFaersDateRange for faers dataSource"(){
        given:
        params.selectedDatasource = "faers"
        params.productGroupId = "1"
        def mockedSource = Mock(DataSource)
        controller.dataSource_faers = mockedSource
        String date = "[FAERS_DATE:27-Mar-2021]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        DateUtil.metaClass.static.StringToDate = { String strDate,String format -> return new DateTime(2015, 1, 5, 8, 0, DateTimeZone.forID('UTC')).toDate()}
        when:
        controller.getVigibaseEvdasAndFaersDateRange()
        then:
        notThrown(Exception)
    }

    void "test getAllEmailsUnique"(){
        setup:
        UserService userService = Mock(UserService)
        userService.getAllEmails(_) >> {
            return ["user2@gmail.com","user3@gmail.com"]
        }
        controller.userService = userService
        when:
        def result = controller.getAllEmailsUnique()
        then:
        result == ["user2@gmail.com","user3@gmail.com"]
        response.status == 200

    }

    @Ignore
    void "test upload"(){
        setup:
        List<MultipartFile> filesToUpload = []
        List<MultipartFile> uploadedFiles = []

        AttachmentableService mockAttachmentableService = Mock(AttachmentableService)
        mockAttachmentableService.attachUploadFileTo(_,_,_,_) >> {
            return [filesToUpload: filesToUpload, uploadedFiles: uploadedFiles]
        }
        controller.attachmentableService = mockAttachmentableService

        AggregateCaseAlert.metaClass.static.getAttachments.sort = {[new Attachment(dateCreated: new Date())]}

        params.alertId = 1
        params.isArchived = false
        params.attachments=[filename:"file"]
        params.description="description3"
        when:
        controller.upload()
        then:
        response.status == 200
    }

    @Ignore
    void "test generateCaseSeries"(){
        setup:
        params.seriesName = "seriesName"
        params.metaInfo = "typeFlag:1,type:2,executedConfigId:1,productId:4,ptCode:5,id:1"
        params.selectedDataSource = "pva"
        AggregateOnDemandAlert.metaClass.static.get = {Long ->
            return alert
        }
        when:
        controller.generateCaseSeries()
        then:
        response.status == 200
    }

    @Ignore
    void "test getRationaleDetails"(){
        setup:
        Long executedConfigId = executedConfiguration.id
        String pt = 'rash'
        Long configId = 1
        Boolean isArchived = false
        String productDss = "productDss"
        String socDss = "socDss"
        def myCriteria = [
                get :{'eq'("executedAlertConfiguration.id", executedConfigId)
                    'eq'("pt", pt)
                    'eq'("productName", productDss)
                    'eq'("soc", socDss)}
        ]
        AggregateCaseAlert.metaClass.static.createCriteria = { myCriteria }
        AggregateCaseAlert.createCriteria()

        when:
        controller.getRationaleDetails(executedConfigId,configId,pt,isArchived,productDss,socDss)
        then:
        response.status == 200
    }

    @Ignore
    void "test fetchAttachments"(){
        setup:
        def alertId = 1L
        boolean isArchived = true

        AggregateCaseAlertService mockaggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockaggregateCaseAlertService.getAlertIdsForAttachments(_,_)>>{
            return [1, 2, 3, 4]
        }

        controller.aggregateCaseAlertService= mockaggregateCaseAlertService

        when:
        controller.fetchAttachment(alertId,isArchived)
        then:
        response.status == 200
    }

    @Ignore
    void "getDssHistoryDetails"(){
        setup:
        AlertService mockAlert = Mock(AlertService)
        mockAlert.fetchPrevExecConfigId(_,_)>>{
            return [[1, 2], [3, 4]]
        }
        controller.alertService = mockAlert

        def myCriteria = [
                get :{'eq'("executedAlertConfiguration.id", executedConfigId)
                    'eq'("pt", pt)}
        ]
        AggregateCaseAlert.metaClass.static.createCriteria = { myCriteria }
        AggregateCaseAlert.createCriteria()

        DateUtil.metaClass.static.toDateString = { Date date-> return "06-Jan-2015"}
        Long executedConfigId = 1
        String pt = 'rash'
        Long configId = 1
        Boolean isArchived = false
        when:
        controller.getDssHistoryDetails(executedConfigId,configId,pt,isArchived)
        then:
        response.status == 200
    }
}