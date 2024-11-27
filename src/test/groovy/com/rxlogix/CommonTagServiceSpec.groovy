package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.dto.CategoryDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.GlobalCase
import com.rxlogix.signal.PvsGlobalTag
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Shared
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges
import spock.lang.Shared
import spock.util.mop.ConfineMetaClassChanges
import grails.util.Holders
import javax.sql.DataSource

import javax.sql.DataSource

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
    @ConfineMetaClassChanges([Sql])
class CommonTagServiceSpec extends HibernateSpec implements ServiceUnitTest<CommonTagService> {
    @Shared
    User user
    @Shared
    AggregateCaseAlert aggregateCaseAlert_b
    @Shared
    ExecutedConfiguration executedConfiguration
    @Shared
    Configuration con
    @Shared
    SingleCaseAlert singleCaseAlert
    @Shared
    Group wfGroup
    @Shared
    Disposition disposition
    @Shared
    Disposition defaultDisposition
    @Shared
    Disposition autoRouteDisposition
    @Shared
    Priority priority
    @Shared
    LiteratureAlert literatureAlert
    @Shared
    CategoryDTO categoryDTO
    @Shared
    GlobalCase globalCase
    @Shared
    PvsGlobalTag pvsGlobalTag
    List<Class> getDomainClasses() {
        [AggregateCaseAlert, User, SingleCaseAlert, ExecutedConfiguration, Configuration, LiteratureAlert, Group, Disposition,
         Priority, Sql, GlobalCase, PvsGlobalTag]
    }

        def setup() {
            service.transactionManager = getTransactionManager()
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(flush: true, failOnError: false)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "ValidatedSignal", validatedConfirmed: true,
                reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true, failOnError: true)

        user = new User(id: '1', username: 'test_user1', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name1' }
        user.metaClass.getEmail = { 'fake.email1@fake.com' }
        user.save(flush: true)

        con = new Configuration(assignedTo: user, productSelection: "[TestProduct]", name: "test",
                owner: user, createdBy: user.username, modifiedBy: user.username, selectedDatasource: "safety", priority: priority)
        con.save(flush: true)
        executedConfiguration = new ExecutedConfiguration(id: 2L, name: "test",
                configId: 1L,
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                pvrCaseSeriesId: 444,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(flush: true)
        aggregateCaseAlert_b = new AggregateCaseAlert(
                id: 903,
                alertConfiguration: con,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: con.priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                productId: 100083,
                soc: "BODY_SYS1",
                pt: 'Rash',
                ptCode: 10029404,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                newSponCount: 2,
                cumSponCount: 1,
                newSeriousCount: 1,
                cumSeriousCount: 1,
                newFatalCount: 1,
                cumFatalCount: 1,
                prrValue: "1",
                rorValue: "1",
                createdBy: con.assignedTo.username,
                modifiedBy: con.assignedTo.username,
                dateCreated: executedConfiguration.dateCreated,
                lastUpdated: executedConfiguration.dateCreated,
                eb05: new Double(1),
                eb95: new Double(1),
                ebgm: new Double(2),
                dueDate: new Date(),
                periodStartDate: new Date(),
                periodEndDate: Date.parse('dd-MMM-yyyy', '31-Dec-2015'),
        )
            aggregateCaseAlert_b.save(flush: true, failOnError: false)
        singleCaseAlert = new SingleCaseAlert(id: 903,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseId: 1212,
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: con,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: con.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                productFamily: "Test Product A",
                isNew: true,
                productId: 100004,
                followUpExists: true
        )
        singleCaseAlert.save(flush: true, failOnError: false)
        literatureAlert = new LiteratureAlert(priority: priority, disposition: disposition,
                assignedToGroup: wfGroup, name: "litAlert1", assignedTo: user, searchString: "search", createdBy: "test_user",
                modifiedBy: "test_user", litSearchConfig: new LiteratureConfiguration(name: "litConf", assignedTo: user, createdBy: "test_user", modifiedBy: "test_user",
                owner: user, workflowGroup: wfGroup, priority: priority), exLitSearchConfig:
                new ExecutedLiteratureConfiguration(name: "exLitConf", assignedTo: user, createdBy: "test_user", modifiedBy: "test_user",
                        owner: user, workflowGroup: wfGroup, configId: 1L), dateCreated: new Date(), lastUpdated: new Date())
        literatureAlert.save(flush: true, failOnError: true)
        def mockedSource = Mock(DataSource)
        service.dataSource_pva = mockedSource
    }

    def cleanup() {
    }



    void "test getTagData()"() {
        given:
        Integer alertLevelId = 3
        String params = "1,903"
        User user = new User()
        user.id = 267
        service.reportIntegrationService = [get: { c ->
            return [status: 200, data: [[martId: 6, factGrpId: 1, catId: 29, subCatId: 30, catName: "parent_cat_2", subCatName: "child_cat_2", module: "PVS"]]]
        }]
        when:
        Map result = service.getTagData(alertLevelId, params, user.id)
        then:
        result == [:]
    }
    void "test getQuanAlertCategories()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def params = ["alertId": aggregateCaseAlert_b.id.toString()];
        //params.put("alertId", "903")
        service.userService = [getCurrentUserId: { -> 267 }]
        service.reportIntegrationService = [postData: { c ->
            return [status: 200, data: [success: true, data: []]]
        }]
        when:
        List result = service.getQuanAlertCategories(params)
        then:
        result.size() == 0
    }
    void "test getQualAlertCategories() "() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def params = ["alertId": singleCaseAlert.id.toString()];
        service.userService = [getCurrentUserId: { -> 267 }]
        service.reportIntegrationService = [postData: { c ->
            return [status: 200, data: [success: true, data: []]]
        }]
        when:
        List result = service.getQualAlertCategories(params)
        then:
        assert result.size() == 0
    }
    void "test getLitAlertCategories"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def params = ["alertId": literatureAlert.id.toString()];
        service.userService = [getCurrentUserId: { -> 267 }]
        service.reportIntegrationService = [postData: { c ->
            return [status: 200, data: [success: true, data: []]]
        }]
        when:
        List result = service.getLitAlertCategories(params)
        then:
        assert result.size() == 0
    }
    void "test saveQualAlertCategories() "() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def existingRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1]]
        def newRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1], [private: false, alert: true, category: [name: "Outcome", id: 235], subcategory: [[name: "Not recovered", id: 239]], priority: 2]]
        def isArchived = false
        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        mockUserService.getCurrentUserName() >> {
            return user.username
        }
        mockUserService.getCurrentUserId() >> {
            return user.id
        }
        mockUserService.getUser() >> {
            return user
        }
        service.userService = mockUserService
        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.postData(_, _, _, _) >> {
            return [status: 200, data: [success: true, data: []]]
        }
        service.reportIntegrationService = mockReportIntegrationService
        when:
        Map result = service.saveQualAlertCategories(singleCaseAlert.id, existingRows, newRows, isArchived)
        then:
        assert result?.status == 200
    }
    void "test saveQuanAlertCategories ()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def existingRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1]]
        def newRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1], [private: false, alert: true, category: [name: "Outcome", id: 235], subcategory: [[name: "Not recovered", id: 239]], priority: 2]]
        def isArchived = false
        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        mockUserService.getCurrentUserName() >> {
            return user.username
        }
        mockUserService.getCurrentUserId() >> {
            return user.id
        }
        mockUserService.getUser() >> {
            return user
        }
        service.userService = mockUserService
        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.postData(_, _, _, _) >> {
            return [status: 200, data: [success: true, data: []]]
        }
        service.reportIntegrationService = mockReportIntegrationService
        when:
        Map result = service.saveQuanAlertCategories(aggregateCaseAlert_b.id, existingRows, newRows, isArchived)
        then:
        assert result?.status == 200
    }
    void "test saveLitAlertCategories ()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def existingRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1]]
        def newRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1], [private: false, alert: true, category: [name: "Outcome", id: 235], subcategory: [[name: "Not recovered", id: 239]], priority: 2]]
        def isArchived = false
        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        mockUserService.getCurrentUserName() >> {
            return user.username
        }
        mockUserService.getCurrentUserId() >> {
            return user.id
        }
        mockUserService.getUser() >> {
            return user
        }
        service.userService = mockUserService
        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.postData(_, _, _, _) >> {
            return [status: 200, data: [success: true, data: []]]
        }
        service.reportIntegrationService = mockReportIntegrationService
        when:
        Map result = service.saveLitAlertCategories(literatureAlert.id, existingRows, newRows, isArchived)
        then:
        assert result?.status == 200
    }
    void "test syncETLCasesWithCategories() "() {
        setup:
        service.sessionFactory = sessionFactory
        categoryDTO = new CategoryDTO(
                catId: 1234L,
                subCatId: 1234L,
                catName: "tag1",
                subCatName: "tag1",
                createdBy: "temp",
                updatedBy: "temp",
                createdDate: new Date(),
                updatedDate: new Date(),
                priority: 1,
                dmlType: "I",
                factGrpCol1: "1",
                factGrpCol2: "2",
                factGrpCol3: "3",
                factGrpCol4: "4",
                privateUserId: 1,
                module: "module",
                dataSource: "Safety",
                factGrpId: 1
        )
        globalCase= new GlobalCase()
        globalCase.caseId = categoryDTO.factGrpCol2
        globalCase.versionNum = categoryDTO.factGrpCol3
        globalCase.save(flush: true)
        pvsGlobalTag = new PvsGlobalTag()
        pvsGlobalTag.tagId = 100L
        pvsGlobalTag.tagText = "tag1"
        pvsGlobalTag.domain =  "Single Case Alert"
        pvsGlobalTag.globalId = globalCase.id
        pvsGlobalTag.createdAt = new Date()
        pvsGlobalTag.modifiedAt = new Date()
        globalCase.addToPvsGlobalTag(pvsGlobalTag)
        pvsGlobalTag.save(flush:true)
        println("pvsTag"+ pvsGlobalTag)
        PvsGlobalTagService pvsGlobalTagService = Mock(PvsGlobalTagService)
        pvsGlobalTagService.fetchPvsGlobalTagObject(_,_,_,_,_,_) >> {
            return pvsGlobalTag
        }
        def caseIds = [CASE_ID :1]
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(caseIds)
        }
        service.reportIntegrationService = [get: {String s,String s2, Map c ->
            return [status: 200, data: [factGrpCol2: 1, factGrpCol3: 1, factGrpId: 1, catId: 29, subCatId: 30, catName: "parent_cat_2", subCatName: "child_cat_2", module: "PVS"]]
        }]
        when:
        service.syncETLCasesWithCategories()
        then:
        assert GlobalCase.count == 2
    }
    void "test syncETLCasesWithCategories() if Condition "() {
        setup:
        service.sessionFactory = sessionFactory
        categoryDTO = new CategoryDTO(
                catId: 1234L,
                subCatId: 1234L,
                catName: "tag1",
                subCatName: "tag1",
                createdBy: "temp",
                updatedBy: "temp",
                createdDate: new Date(),
                updatedDate: new Date(),
                priority: 1,
                dmlType: "I",
                factGrpCol1: "1",
                factGrpCol2: "2",
                factGrpCol3: "3",
                factGrpCol4: "4",
                privateUserId: 1,
                module: "module",
                dataSource: "Safety",
                factGrpId: 1
        )
        globalCase= new GlobalCase()
        globalCase.caseId = categoryDTO.factGrpCol2 as Long
        globalCase.versionNum = categoryDTO.factGrpCol3 as Integer
        globalCase.save(flush: true)
        pvsGlobalTag = new PvsGlobalTag()
        pvsGlobalTag.tagId = 100L
        pvsGlobalTag.tagText = "tag1"
        pvsGlobalTag.domain =  "Single Case Alert"
        pvsGlobalTag.globalId = globalCase.id
        pvsGlobalTag.createdAt = new Date()
        pvsGlobalTag.modifiedAt = new Date()
        globalCase.addToPvsGlobalTag(pvsGlobalTag)
        pvsGlobalTag.save(flush:true)
        PvsGlobalTagService pvsGlobalTagService = Mock(PvsGlobalTagService)
        pvsGlobalTagService.fetchPvsGlobalTagObject(_,_,_,_,_,_) >> {
            return pvsGlobalTag
        }
        def caseIds = [CASE_ID :1]
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(caseIds)
        }
        service.reportIntegrationService = [get: {String s,String s2, Map c ->
            return [status: 200, data: [factGrpCol2: categoryDTO.factGrpCol2, factGrpCol3: categoryDTO.factGrpCol3, factGrpId: 1, catId: 29, subCatId: 30, catName: "parent_cat_2", subCatName: "child_cat_2", module: "PVS"]]
        }]
        when:
        service.syncETLCasesWithCategories()
        then:
        assert GlobalCase.count == 1
    }
    void "test getQualAlertCategories()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def params = ["alertId": singleCaseAlert.id.toString()];
        service.userService = [getCurrentUserId: { -> 267 }]
        service.reportIntegrationService = [postData: { c ->
            return [status: 200, data: [success: true, data: []]]
        }]
        when:
        List result = service.getQualAlertCategories(params)
        then:
        assert result.size() == 0
    }

    void "test getLitAlertCategories()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def params = ["alertId": literatureAlert.id.toString()];
        service.userService = [getCurrentUserId: { -> 267 }]
        service.reportIntegrationService = [postData: { c ->
            return [status: 200, data: [success: true, data: []]]
        }]
        when:
        List result = service.getLitAlertCategories(params)
        then:
        assert result.size() == 0
    }

    void "test saveQualAlertCategories()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def existingRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1]]
        def newRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1], [private: false, alert: true, category: [name: "Outcome", id: 235], subcategory: [[name: "Not recovered", id: 239]], priority: 2]]
        def isArchived = false
        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        mockUserService.getCurrentUserName() >> {
            return user.username
        }
        mockUserService.getCurrentUserId() >> {
            return user.id
        }
        mockUserService.getUser() >> {
            return user
        }
        service.userService = mockUserService
        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.postData(_, _, _, _) >> {
            return [status: 200, data: [success: true, data: []]]
        }
        service.reportIntegrationService = mockReportIntegrationService
        when:
        Map result = service.saveQualAlertCategories(singleCaseAlert.id, existingRows, newRows, isArchived)
        then:
        assert result?.status == 200
    }

    void "test saveQuanAlertCategories()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def existingRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1]]
        def newRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1], [private: false, alert: true, category: [name: "Outcome", id: 235], subcategory: [[name: "Not recovered", id: 239]], priority: 2]]
        def isArchived = false
        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        mockUserService.getCurrentUserName() >> {
            return user.username
        }
        mockUserService.getCurrentUserId() >> {
            return user.id
        }
        mockUserService.getUser() >> {
            return user
        }
        service.userService = mockUserService
        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.postData(_, _, _, _) >> {
            return [status: 200, data: [success: true, data: []]]
        }
        service.reportIntegrationService = mockReportIntegrationService
        when:
        Map result = service.saveQuanAlertCategories(aggregateCaseAlert_b.id, existingRows, newRows, isArchived)
        then:
        assert result?.status == 200
    }

    void "test saveLitAlertCategories()"() {
        given:
        MockHttpServletRequest request = new MockHttpServletRequest();
        def existingRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1]]
        def newRows = [[private: false, alert: false, category: [name: "Action Taken With Drug", id: 229], subcategory: [[name: "Unknown", id: 234]], priority: 1], [private: false, alert: true, category: [name: "Outcome", id: 235], subcategory: [[name: "Not recovered", id: 239]], priority: 2]]
        def isArchived = false
        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        mockUserService.getCurrentUserName() >> {
            return user.username
        }
        mockUserService.getCurrentUserId() >> {
            return user.id
        }
        mockUserService.getUser() >> {
            return user
        }
        service.userService = mockUserService
        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.postData(_, _, _, _) >> {
            return [status: 200, data: [success: true, data: []]]
        }
        service.reportIntegrationService = mockReportIntegrationService
        when:
        Map result = service.saveLitAlertCategories(literatureAlert.id, existingRows, newRows, isArchived)
        then:
        assert result?.status == 200
    }

    void "test syncETLCasesWithCategories()"() {
        setup:
        service.sessionFactory = sessionFactory
        categoryDTO = new CategoryDTO(
                catId: 1234L,
                subCatId: 1234L,
                catName: "tag1",
                subCatName: "tag1",
                createdBy: "temp",
                updatedBy: "temp",
                createdDate: new Date(),
                updatedDate: new Date(),
                priority: 1,
                dmlType: "I",
                factGrpCol1: "1",
                factGrpCol2: "2",
                factGrpCol3: "3",
                factGrpCol4: "4",
                privateUserId: 1,
                module: "module",
                dataSource: "Safety",
                factGrpId: 1
        )
        globalCase= new GlobalCase()
        globalCase.caseId = categoryDTO.factGrpCol2
        globalCase.versionNum = categoryDTO.factGrpCol3
        globalCase.save(flush: true)
        pvsGlobalTag = new PvsGlobalTag()
        pvsGlobalTag.tagId = 100L
        pvsGlobalTag.tagText = "tag1"
        pvsGlobalTag.domain =  "Single Case Alert"
        pvsGlobalTag.globalId = globalCase.id
        pvsGlobalTag.createdAt = new Date()
        pvsGlobalTag.modifiedAt = new Date()
        globalCase.addToPvsGlobalTag(pvsGlobalTag)
        pvsGlobalTag.save(flush:true)
        println("pvsTag"+ pvsGlobalTag)
        PvsGlobalTagService pvsGlobalTagService = Mock(PvsGlobalTagService)
        pvsGlobalTagService.fetchPvsGlobalTagObject(_,_,_,_,_,_) >> {
            return pvsGlobalTag
        }
        def caseIds = [CASE_ID :1]
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(caseIds)
        }
        service.reportIntegrationService = [get: {String s,String s2, Map c ->
            return [status: 200, data: [factGrpCol2: 1, factGrpCol3: 1, factGrpId: 1, catId: 29, subCatId: 30, catName: "parent_cat_2", subCatName: "child_cat_2", module: "PVS"]]
        }]
        when:
        service.syncETLCasesWithCategories()
        then:
        assert GlobalCase.count == 2
    }
    void "test syncETLCasesWithCategories() if Condition"() {
        setup:
        service.sessionFactory = sessionFactory
        categoryDTO = new CategoryDTO(
                catId: 1234L,
                subCatId: 1234L,
                catName: "tag1",
                subCatName: "tag1",
                createdBy: "temp",
                updatedBy: "temp",
                createdDate: new Date(),
                updatedDate: new Date(),
                priority: 1,
                dmlType: "I",
                factGrpCol1: "1",
                factGrpCol2: "2",
                factGrpCol3: "3",
                factGrpCol4: "4",
                privateUserId: 1,
                module: "module",
                dataSource: "Safety",
                factGrpId: 1
        )
        globalCase= new GlobalCase()
        globalCase.caseId = categoryDTO.factGrpCol2 as Long
        globalCase.versionNum = categoryDTO.factGrpCol3 as Integer
        globalCase.save(flush: true)
        pvsGlobalTag = new PvsGlobalTag()
        pvsGlobalTag.tagId = 100L
        pvsGlobalTag.tagText = "tag1"
        pvsGlobalTag.domain =  "Single Case Alert"
        pvsGlobalTag.globalId = globalCase.id
        pvsGlobalTag.createdAt = new Date()
        pvsGlobalTag.modifiedAt = new Date()
        globalCase.addToPvsGlobalTag(pvsGlobalTag)
        pvsGlobalTag.save(flush:true)
        PvsGlobalTagService pvsGlobalTagService = Mock(PvsGlobalTagService)
        pvsGlobalTagService.fetchPvsGlobalTagObject(_,_,_,_,_,_) >> {
            return pvsGlobalTag
        }
        def caseIds = [CASE_ID :1]
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(caseIds)
        }
        service.reportIntegrationService = [get: {String s,String s2, Map c ->
            return [status: 200, data: [factGrpCol2: categoryDTO.factGrpCol2, factGrpCol3: categoryDTO.factGrpCol3, factGrpId: 1, catId: 29, subCatId: 30, catName: "parent_cat_2", subCatName: "child_cat_2", module: "PVS"]]
        }]
        when:
        service.syncETLCasesWithCategories()
        then:
        assert GlobalCase.count == 1
    }


        void "test fetchAllAlertAndGlobalCategories - SingleCaseAlert"() {
            given:
            def alertList = [singleCaseAlert]
            def mockReportIntegrationService = Mock(ReportIntegrationService)
            mockReportIntegrationService.postData(_, _, _) >> {
                return [result: []]
            }
            service.reportIntegrationService = mockReportIntegrationService
            when:
            def output = service.fetchAllAlertAndGlobalCategories(alertList, 1, 2)
            then:
            output == []

        }
        void "test fetchAllAlertAndGlobalCategories - AggregateAlert"() {
            given:
            def alertList = [aggregateCaseAlert_b]
            def mockReportIntegrationService = Mock(ReportIntegrationService)
            mockReportIntegrationService.postData(_, _, _) >> {
                return [result: []]
            }
            service.reportIntegrationService = mockReportIntegrationService
            when:
            def output = service.fetchAllAlertAndGlobalCategories(alertList, 3, 4)
            then:
            output == []

        }
        void "test fetchAllAlertAndGlobalCategories - Literature"() {
            given:
            def alertList = [literatureAlert]

            def mockReportIntegrationService = Mock(ReportIntegrationService)
            mockReportIntegrationService.postData(_, _, _) >> {
                return [result: []]
            }
            service.reportIntegrationService = mockReportIntegrationService
            when:
            def output = service.fetchAllAlertAndGlobalCategories(alertList, 5, 6)
            then:
            output == []

        }

        void "test convertCategories"() {
            given:
            def mockUserService = Mock(UserService)
            mockUserService.getUser() >> {
                return user
            }
            service.userService = mockUserService
            List<Map> categories = [[martId : 197775, factGrpId: 1, catId: 150, subCatId: 0, catName: 'Trend Pediatric', subCatName: null, dmlType: null, module: 'PVS', dataSource: 'pva', privateUserId: 0, priority: 9999, createdBy: System, createdDate: '2021-06-08 04:33:19.0', updatedBy: 'System', updatedDate: '2021-06-08 04:33:19.0', isAutoTagged: 1, isRetained: 0, udNumber1: 2108, udNumber2: 0, udNumber3: 0,
                                     udText1: null, udText2: null, udText3: null, udText4: null, udDate1: null, udDate2: null, factGrpCol1: 1, factGrpCol2: 180499, factGrpCol3: 8424922, factGrpCol4: 1, factGrpCol5: null, factGrpCol6: null, factGrpCol7: null, factGrpCol8: null, factGrpCol9: null, factGrpCol10: null, alertId: null, isAdhoc: null],
                                    [martId     : 197775, factGrpId: 1, catId: 150, subCatId: 13, catName: 'Trend Pediatric', subCatName: 'testSubtag', dmlType: null, module: 'PVS', dataSource: 'pva', privateUserId: 0, priority: 9999, createdBy: 'System', createdDate: '2021-06-08 04:33:19.0', updatedBy: 'System', updatedDate: '2021-06-08 04:33:19.0', isAutoTagged: 1, isRetained: 0, udNumber1: 2108, udNumber2: 0,
                                     udNumber3  : 0, udText1: null, udText2: null, udText3: null, udText4: null, udDate1: null, udDate2: null, factGrpCol1: 1, factGrpCol2: 180499,
                                     factGrpCol3: 8424922, factGrpCol4: 1, factGrpCol5: null, factGrpCol6: null, factGrpCol7: null, factGrpCol8: null, factGrpCol9: null, factGrpCol10: null, alertId: null, isAdhoc: null]]
            when:
            def output = service.convertCategories(categories)
            then:
            output[0].category.name == 'Trend Pediatric'
            output[0].subcategory[0].name == 'testSubtag'
        }
}