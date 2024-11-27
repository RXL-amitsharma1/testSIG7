package com.rxlogix.signal

import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.enums.*
import com.rxlogix.helper.LinkHelper
import com.rxlogix.mart.MartTags
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import spock.lang.Shared
import spock.util.mop.ConfineMetaClassChanges

import javax.sql.DataSource

@ConfineMetaClassChanges([Sql])
class SingleOnDemandAlertServiceSpec extends HibernateSpec implements ServiceUnitTest<SingleOnDemandAlertService>, LinkHelper {
    @Shared
    Disposition disposition
    Configuration alertConfiguration, alertConfiguration2
    @Shared
    ExecutedConfiguration executedConfiguration, executedConfiguration2, executedConfiguration3
    @Shared
    User user, newUser
    def attrMapObj
    @Shared
    SingleOnDemandAlert alert
    @Shared
    Group wfGroup
    Logger logger = Mock(Logger)
    @Shared
    List execIds
    MartTags martTags1
    Activity activity1
    Priority priority1, priority2
    @Shared
    AlertComment alertComment
    @Shared
    ViewInstance viewInstance
    @Shared
    AdvancedFilter advanceFilter

    List<Class> getDomainClasses() {
        [SingleOnDemandAlert, User, ExecutedConfiguration, Disposition, Group, Priority, BusinessConfiguration, CaseHistory, EmergingIssue,
         ExecutedAlertDateRangeInformation, SingleGlobalTag, com.rxlogix.mart.CaseSeriesTagMapping, MartTags, ExecutionStatus, ParameterValue,
         ExecutedQueryValueList, AlertTag, AlertDateRangeInformation, Category, ReportTemplate, AlertComment, DispositionRule, SingleCaseAlertService,
         ViewInstance, AdvancedFilter, ViewHelper, UserService, Preference]
    }

    void setup() {
        service.log = logger
        alertComment = new AlertComment(comments: "commetn1", alertType: "Single Case Alert", createdBy: "username",
                modifiedBy: "username", caseNumber: "1S01")
        alertComment.save(flush: true, failOnError: true)
        disposition = new Disposition(value: "ValidatedSignal1", displayName: "Validated Signal1", validatedConfirmed: true,
                abbreviation: "C")
        disposition.save(failOnError: true)

        wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"
        user.groups = [wfGroup]
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        newUser = new User(id: '2', username: 'username2', createdBy: 'createdBy2', modifiedBy: 'modifiedBy2')
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.groups = [wfGroup]
        newUser.metaClass.getFullName = { 'Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.save(failOnError: true)

        priority1 = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority1.save(flush: true, failOnError: true)
        priority2 = new Priority(value: "medium", display: true, displayName: "medium", reviewPeriod: 2, priorityOrder: 1)
        priority2.save(flush: true, failOnError: true)
        AlertDateRangeInformation alertDateRangeInformation1 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUSTOM)
        Category category1 = new Category(name: "category1")
        category1.save(flush: true, failOnError: true)
        ReportTemplate reportTemplate1 = new ReportTemplate(name: "repTemp1", description: "repDesc1", category: category1,
                owner: user, templateType: TemplateTypeEnum.TEMPLATE_SET, dateCreated: new Date(), lastUpdated: new Date() + 4,
                createdBy: "username", modifiedBy: "username")
        reportTemplate1.save(flush: true, failOnError: true)

        alertConfiguration = new Configuration(
                productSelection: '{"3":[{"name":"product1"}]}',
                executing: false,
                template: reportTemplate1,
                priority: priority1,
                alertTriggerCases: "11",
                alertTriggerDays: "11",
                dateCreated: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "test",
                assignedTo: user,
                createdBy: "username",
                modifiedBy: "username",
                owner: user,
                adhocRun: true,
                alertQueryId: 1L,
                alertQueryName: "AlertQuery1",
                alertDateRangeInformation: alertDateRangeInformation1,
        )
        alertConfiguration.metaClass.getProductType = { 'family' }
        alertConfiguration.save(flush: true, failOnError: true)
        alertConfiguration2 = new Configuration(
                productSelection: '{"3":[{"name":"product2"}]}',
                executing: false,
                priority: priority1,
                alertTriggerCases: "11",
                alertTriggerDays: "11",
                dateCreated: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "test1",
                assignedTo: user,
                createdBy: "username",
                modifiedBy: "username",
                owner: user,
                adhocRun: true,
                alertQueryId: 1L,
                alertQueryName: "AlertQuery1",
        )
        alertConfiguration2.metaClass.getProductType = { 'family2' }
        alertConfiguration2.save(flush: true, failOnError: true)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsolute: new Date() + 2)
        executedAlertDateRangeInformation.save(validate: false)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation2 = new ExecutedAlertDateRangeInformation(dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsolute: new Date() + 2, dateRangeEnum: DateRangeEnum.CUSTOM)
        executedAlertDateRangeInformation2.save(validate: false)
        AlertTag alertTag1 = new AlertTag(name: "alertTag1", createdBy: user, dateCreated: new Date())
        alertTag1.save(flush: true, failOnError: true)
        SingleGlobalTag singleGlobalTag1 = new SingleGlobalTag(caseSeriesId: 22L, caseId: 22L, tagId: 22L, owner: "userName",
                lastUpdated: new Date(), tagText: "tagText1")
        singleGlobalTag1.save(flush: true, failOnError: true)
        SingleGlobalTag singleGlobalTag2 = new SingleGlobalTag(/*caseSeriesId: 22L,*/ caseId: 2L, tagId: 1L, owner: "userName",
                lastUpdated: new Date(), tagText: "tagText2")
        singleGlobalTag2.save(flush: true, failOnError: true)
        SingleGlobalTag singleGlobalTag3 = new SingleGlobalTag(caseSeriesId: 22L, caseId: 2L, tagId: 1L, owner: "userName",
                lastUpdated: new Date(), tagText: "tagText3")
        singleGlobalTag3.save(flush: true, failOnError: true)
        ParameterValue parameterValue1 = new ParameterValue(key: "paramKey1", value: "paramValue1")
        parameterValue1.save(flush: true, failOnError: true)
        ExecutedQueryValueList executedQueryValueList1 = new ExecutedQueryValueList(query: 1L, queryName: "qName1", parameterValues: [parameterValue1])
        executedQueryValueList1.save(flush: true, failOnError: true)
        ExecutedQueryValueList executedQueryValueList2 = new ExecutedQueryValueList(query: 2L, queryName: "qName2")
        executedQueryValueList2.save(flush: true, failOnError: true)
        executedConfiguration = new ExecutedConfiguration(id: 2L, name: "test",
                executedAlertQueryValueLists: [executedQueryValueList1, executedQueryValueList2],
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                pvrCaseSeriesId: 22L,
                caseSeriesExecutionStatus: ReportExecutionStatus.GENERATING,
                type: "executedConfiguration",
                isLatest: true, configId: alertConfiguration.id,
                workflowGroup: wfGroup,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(failOnError: true)
        executedConfiguration2 = new ExecutedConfiguration(id: 3L, name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                executedAlertDateRangeInformation: executedAlertDateRangeInformation2,
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
                assignedTo: user, configId: alertConfiguration.id,
                pvrCaseSeriesId: 22L,
                type: "executedConfiguration",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration2.save(failOnError: true)

        executedConfiguration3 = new ExecutedConfiguration(id: 3L, name: "test2",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                description: "test2", dateCreated: new Date(), lastUpdated: new Date(),
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
                assignedTo: user, configId: alertConfiguration.id,
                pvrCaseSeriesId: 22L,
                type: "executedConfiguration",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration3.save(failOnError: true)
        ExecutionStatus executionStatus1 = new ExecutionStatus(configId: Configuration.list().get(0).id, reportVersion: Configuration.list().get(0).numOfExecutions + 1,
                type: "executedConfiguration", name: "executionStatus1", nextRunDate: new Date() + 1, owner: user)
        executionStatus1.save(flush: true, failOnError: true)
        ExecutionStatus executionStatus2 = new ExecutionStatus(configId: Configuration.list().get(0).id, reportVersion: Configuration.list().get(0).numOfExecutions,
                type: "executedConfiguration", name: "executionStatus2", nextRunDate: new Date() + 1, owner: user)
        executionStatus2.save(flush: true, failOnError: true)
        attrMapObj = ['masterFollowupDate_5' : new Date(),
                      'masterRptTypeId_3'    : "test type",
                      'masterInitReptDate_4' : new Date(),
                      'masterFollowupDate_5' : new Date(),
                      'reportersHcpFlag_2'   : "true",
                      'masterProdTypeList_6' : "test",
                      'masterPrefTermAll_7'  : "test",
                      'assessOutcome'        : "Death",
                      'assessListedness_9'   : "test",
                      'assessAgentSuspect_10': "test"]
        alert = new SingleOnDemandAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: priority2,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                attributesMap: attrMapObj,
                followUpExists: false,
                isNew: false,
                productId: 1,
                followUpNumber: 1,
                isCaseSeries: false,
                caseId: 1,
                comboFlag: "comboFlag",
                malfunction: "malfunction",
                ptList: ['PT', 'SINGLE_ALERT_OD_PT','SINGLE_ALERT_OD_ID'],
                conComitList      : ['SINGLE_ALERT_OD_CON_COMIT', 'SINGLE_ALERT_CON_COMIT','SINGLE_ALERT_OD_ID'],
                suspectProductList: ['SINGLE_ALERT_OD_SUSP_PROD','SINGLE_ALERT_SUSP_PROD','SINGLE_ALERT_OD_ID'],
                medErrorPtList    : ['SINGLE_ALERT_OD_MED_ERR','SINGLE_ALERT_MED_ERR_PT_LIST','SINGLE_ALERT_OD_ID'],
        )
        alert.save(flush: true, failOnError: true)

        ActivityType activityType1 = new ActivityType(value: ActivityTypeValue.JustificationChange)
        activityType1.save(flush: true, failOnError: true)
        activity1 = new Activity(details: "activityDetails", performedBy: user, timestamp: new Date(),
                justification: "change needed", assignedTo: user, type: activityType1)
        activity1.save(validate: false)

        ViewHelper viewHelper = Mock(ViewHelper)
        viewHelper.metaClass.getDictionaryValues(_) >> {
            return "product"
        }

        String columnSeq = """
                {"7":{"containerView":1,"label":"Tags","name":"alertTags","listOrder":0,"seq":6},"8":{"containerView":1,"label":"Receipt Date","name":"caseInitReceiptDate","listOrder":1,"seq":7},"9":{"containerView":1,"label":"Product Name","name":"productName","listOrder":2,"seq":8},"10":{"containerView":1,"label":"PT","name":"pt","listOrder":3,"seq":9},"11":{"containerView":1,"label":"Listedness","name":"listedness","listOrder":4,"seq":10},"12":{"containerView":3,"label":"Suspect Products","name":"suspProd","listOrder":9999,"seq":17},"13":{"containerView":3,"label":"Con Med","name":"conComit","listOrder":9999,"seq":18},
                "14":{"containerView":3,"label":"PT List","name":"masterPrefTermAll","listOrder":9999,"seq":19},"15":{"containerView":1,"label":"Serious","name":"serious","listOrder":14,"seq":20},"16":{"containerView":3,"label":"Report Type","name":"caseReportType","listOrder":9999,"seq":21},"17":{"containerView":3,"label":"HCP","name":"reportersHcpFlag","listOrder":9999,"seq":22},"18":{"containerView":3,"label":"Country","name":"country","listOrder":9999,"seq":23},"19":{"containerView":3,"label":"Age Group","name":"age","listOrder":9999,"seq":24},"20":{"containerView":3,"label":"Gender","name":"gender","listOrder":9999,"seq":25},
                "21":{"containerView":3,"label":"Positive Rechallenge","name":"rechallenge","listOrder":9999,"seq":26},"22":{"containerView":3,"label":"Locked Date","name":"lockedDate","listOrder":9999,"seq":27},"23":{"containerView":3,"label":"Death","name":"death","listOrder":9999,"seq":28},"24":{"containerView":3,"label":"Medication Error PTs","name":"medErrorsPt","listOrder":9999,"seq":37},"25":{"containerView":3,"label":"Age","name":"patientAge","listOrder":9999,"seq":29},"26":{"containerView":3,"label":"Case Type","name":"caseType","listOrder":9999,"seq":31},
                "27":{"containerView":3,"label":"Completeness Score","name":"completenessScore","listOrder":9999,"seq":32},"28":{"containerView":3,"label":"Primary IND#","name":"indNumber","listOrder":9999,"seq":33},"29":{"containerView":3,"label":"Application#","name":"appTypeAndNum","listOrder":9999,"seq":34},"30":{"containerView":3,"label":"Compounding Flag","name":"compoundingFlag","listOrder":9999,"seq":35},"31":{"containerView":3,"label":"Indications","name":"indications","listOrder":9999,"seq":30},"32":{"containerView":3,"label":"Medication Error PT Count","name":"medErrorPtCount","listOrder":9999,"seq":38}}
           """
        String filters = """
                {"1":"pyrexia"}
        """
        String sorting = """
                {"1":"asc"}
        """
        viewInstance = new ViewInstance(name: "viewInstance", alertType: "Single Case Alert", user: user, columnSeq: columnSeq, filters: filters, sorting: sorting)
        viewInstance.save(flush: true, failOnError: true)

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
        advanceFilter.name = "ad listed 1"
        advanceFilter.user = user
        advanceFilter.save(validate: false)

        MartTags.withTransaction {
            martTags1 = new MartTags(name: "martTag1", type: "mart1", isCaseSeriesTag: true)
            martTags1.save(flush: true, failOnError: true)
        }
        com.rxlogix.mart.CaseSeriesTagMapping.withTransaction {
            com.rxlogix.mart.CaseSeriesTagMapping caseSeriesTagMapping1 = new com.rxlogix.mart.CaseSeriesTagMapping(caseId: 1L, caseSeriesExecId: 1L, owner: "username",
                    tag: martTags1, lastUpdated: new Date())
            caseSeriesTagMapping1.save(flush: true, failOnError: true)
        }
        CacheService cacheService = Mock(CacheService)
        cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdText20') >> {
            return "20"
        }
        cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdText10') >> {
            return "10"
        }
        cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdText11') >> {
            return "11"
        }
        cacheService.getRptFieldIndexCache('malfunctionDevices') >> {
            return "Yes"
        }
        cacheService.getRptFieldIndexCache('deviceComboProduct') >> {
            return "No"
        }
        cacheService.getRptFieldIndexCache(_) >> {
            return Constants.Commons.BLANK_STRING
        }
        cacheService.getDispositionByValue(_) >> {
            return disposition
        }
        cacheService.getPriorityByValue(_) >> {
            return priority1
        }
        cacheService.getUserByUserId(_) >> {
            return user
        }
        cacheService.getGroupByGroupId(_) >> {
            return wfGroup
        }
        service.cacheService = cacheService

        SingleCaseAlert.metaClass.composeAlert = { String s, List l, Boolean b, String s2, Boolean b2, String s3 ->
            [id: alert.id, globalId: 12]
        }
        UserService userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> {
            return user
        }
        service.userService = userService
        SpringSecurityService springSecurityService = Mock(SpringSecurityService)
        springSecurityService.isLoggedIn() >> {
            return true
        }
        springSecurityService.principal >> {
            return user
        }
        service.userService.springSecurityService = springSecurityService
        CaseHistory caseHistory1 = new CaseHistory(currentDisposition: disposition, currentAssignedTo: user, currentAssignedToGroup:
                wfGroup, currentPriority: new Priority(value: "tooLow", displayName: "priority1"), caseNumber: "1S01", caseVersion: 1,
                productFamily: "productFamily1", isLatest: true, followUpNumber: 1)
        caseHistory1.save(flush: true, failOnError: true)
        CaseHistory caseHistory2 = new CaseHistory(currentDisposition: disposition, currentAssignedTo: user, currentAssignedToGroup:
                wfGroup, currentPriority: new Priority(value: "High", displayName: "priority2"), caseNumber: '2', caseVersion: 1,
                productFamily: "productFamily2", isLatest: true)
        Long confId = Configuration.list().get(0).id
        PvsGlobalTagService pvsGlobalTagService = Mock(PvsGlobalTagService)
        pvsGlobalTagService.getAllGlobalTags(_, _, _) >> {
            [[globalId: 12]]
        }
        pvsGlobalTagService.batchPersistGlobalTags(_) >> {
            PvsGlobalTag pvsGlobalTag = new PvsGlobalTag(tagId: 1L, subTagId: 1L, tagText: "text", subTagText: "text",
                    globalId: 1L, domain: "Single Case Alert", createdAt: new Date())
            pvsGlobalTag.save(flush: true, failOnError: true)
            [[col1: pvsGlobalTag.id.toString(), col2: pvsGlobalTag.globalId.toString()]]
        }
        pvsGlobalTagService.fetchTagsAndSubtags() >> {
            return [["text": "text", "id": 1L]]
        }
        service.pvsGlobalTagService = pvsGlobalTagService
        DataObjectService dataObjectService = Mock(DataObjectService)
        dataObjectService.getDefaultDisposition(_) >> {
            return disposition
        }
        dataObjectService.getCaseIdList(_) >> {
            return [alertConfiguration.id]
        }
        dataObjectService.getCaseTags(_) >> {
            return ['{"tags":"[{tagText:text,subTags:[text]}]"}']
        }
        dataObjectService.getExistingCaseHistory(_, _) >> {
            return null
        }
        dataObjectService.getSCAActivityList(_) >> {
            return [activity1]
        }
        dataObjectService.getCaseHistoryList(confId) >> {
            return [caseHistory2]
        }
        dataObjectService.clearCaseHistoryMap(confId) >> {
            return null
        }
        dataObjectService.getExistingCaseHistoryList(confId) >> {
            CaseHistory.list().get(0).isLatest = false
            [CaseHistory.list().get(0).id]
        }
        dataObjectService.getSignalAlertMap(_) >> {
            return [[alert: [caseNumber: "1S01", productFamily: "Test Product A"]]]
        }
        dataObjectService.clearSignalAlertMap(_) >> {
            return true
        }
        service.dataObjectService = dataObjectService

        AlertService alertService = Mock(AlertService)
        alertService.getAttachmentMap(_, _) >> {
            return [[alertId: alert.id]]
        }
        alertService.prepareFilterMap(_, _) >> {
            return ['newDashboardFilter': true]
        }
        alertService.isProductSecurity() >> {
            return false
        }
        alertService.fetchAllowedProductsForConfiguration() >> {
            return ["product1"]
        }
        alertService.fetchPrevExecConfigId(executedConfiguration, alertConfiguration) >> {
            return [executedConfiguration2.id, executedConfiguration3.id]
        }
        alertService.fetchPrevPeriodSCAlerts(alert, [executedConfiguration2.id]) >> {
            return [[caseNumber: '101']]
        }
        List caseHistoryList = [caseHistory2]
        alertService.batchPersistForDomain(caseHistoryList, CaseHistory) >> {
            CaseHistory.withTransaction {
                caseHistoryList.each {
                    it.save(validate: false)
                }
            }
        }
        service.alertService = alertService
        PvsAlertTagService pvsAlertTagService = Mock(PvsAlertTagService)
        pvsAlertTagService.getAllAlertSpecificTags(_, _) >> {
            return [[alertId: alert.id]]
        }
        service.pvsGlobalTagService = pvsAlertTagService

        EmergingIssue emergingIssue = new EmergingIssue(eventName: '{"4":[{"name":"event11"},{"name":"event12"}]}', createdBy: "createdBy1",
                dateCreated: new Date() - 1, dme: true, emergingIssue: true, ime: true, specialMonitoring: true)
        emergingIssue.save(failOnError: true)

        def dataSource = Mock(DataSource)
        service.dataSource = dataSource
        execIds = ExecutedConfiguration.list().id
        service.transactionManager = getTransactionManager()
    }

    void "test getSingleCaseAlertList"() {
        given:
        AlertDataDTO alertDataDTO = new AlertDataDTO(isFromExport: true, timeZone: 'UTC')

        when:
        List result = service.getSingleCaseAlertList([alert], alertDataDTO)

        then:
        result[0].alertConfigId == alertConfiguration.id
        result[0].productName == 'Test Product A'
        result[0].pt == 'Rash'
    }
}