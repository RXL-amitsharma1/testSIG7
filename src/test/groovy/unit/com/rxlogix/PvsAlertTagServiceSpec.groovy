package unit.com.rxlogix

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.dto.AlertTagDTO
import com.rxlogix.dto.CategoryDTO
import com.rxlogix.CategoryUtil
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.GlobalCase
import com.rxlogix.signal.PvsAlertTag
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification
import com.rxlogix.dto.CategoryDTO
import com.rxlogix.CategoryUtil
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.GlobalCase
import spock.lang.Unroll
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.ArchivedLiteratureAlert

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class PvsAlertTagServiceSpec extends Specification implements ServiceUnitTest<PvsAlertTagService> {

    Disposition disposition
    Configuration configuration
    ExecutedConfiguration executedConfiguration
    ExecutedConfiguration executedConfiguration1
    User user
    def attrMapObj
    SingleCaseAlert singleCaseAlert
    ArchivedSingleCaseAlert archivedSingleCaseAlert
    GlobalCase globalCase
    AggregateCaseAlert aggregateAlert
    PvsAlertTag pvsAlertTag
    CategoryDTO categoryDTO
    Group wfGroup

    void setup() {

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true , abbreviation:"D")
        disposition.save(failOnError: true)

        Group wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: disposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"

        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)


        mockDomain(Priority, [
                [value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1]
        ])

        configuration = new Configuration(assignedTo: user, productSelection : "[TestProduct]", name: "test",
                owner: user, createdBy: user.username, modifiedBy: user.username, priority: new Priority(value: "High"))
        configuration.save(failOnError: true)

        executedConfiguration = new ExecutedConfiguration(id:2L,name: "test",
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
                pvrCaseSeriesId : 444,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(failOnError: true)

        executedConfiguration1 = new ExecutedConfiguration(id:3L,name: "test",
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
                pvrCaseSeriesId : 444,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration1.save(failOnError: true)

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

        singleCaseAlert = new SingleCaseAlert(id: 50,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseId:1212,
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: configuration.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: configuration.assignedTo.username,
                modifiedBy: configuration.assignedTo.username,
                dateCreated: configuration.dateCreated,
                lastUpdated: configuration.dateCreated,
                productFamily: "Test Product A",
                isNew: true,
                productId:100004,
                followUpExists:true,
                attributesMap: attrMapObj)
       singleCaseAlert.save(validate: false)
        println SingleCaseAlert.get()
        globalCase= new GlobalCase()
        globalCase.caseId = singleCaseAlert.caseId
        globalCase.versionNum = 1
        globalCase.save(failOnError: true)


        archivedSingleCaseAlert = new ArchivedSingleCaseAlert(
                id: 50,
                productName: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                globalIdentity: globalCase,
                caseId:1212,
//                caseNumber: "1S01",
                caseVersion: 4,
//                alertConfiguration: config,
                executedAlertConfiguration: executedConfiguration1,
//                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: configuration.priority,
                disposition: disposition,
                assignedTo: user,
//                productName: "Test Product A",
                pt: "Rash",
//                createdBy: config.assignedTo.username,
//                modifiedBy: config.assignedTo.username,
//                dateCreated: config.dateCreated,
//                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                isNew: true,
                productId:100004,
                followUpExists:true,
//                attributesMap: attrMapObj
        )
        archivedSingleCaseAlert.save(validate: false)

        globalCase= new GlobalCase()
        globalCase.caseId = singleCaseAlert.caseId
        globalCase.versionNum = 1
        globalCase.save(failOnError: true)


        archivedSingleCaseAlert = new ArchivedSingleCaseAlert(
                id: 50,
                productName: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                globalIdentity: globalCase,
                caseId:1212,
                caseVersion: 4,
                executedAlertConfiguration: executedConfiguration1,
                priority: configuration.priority,
                disposition: disposition,
                assignedTo: user,
                pt: "Rash",
                productFamily: "Test Product A",
                isNew: true,
                productId:100004,
                followUpExists:true,
        )
        archivedSingleCaseAlert.save(validate: false)

        aggregateAlert = new AggregateCaseAlert(
                alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: configuration.priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
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
                createdBy: configuration.assignedTo.username,
                modifiedBy: configuration.assignedTo.username,
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
                ebgmStr: "male:0.1, female:0.2, unknown:0.3"

        )
        aggregateAlert.save(failOnError: true)

        pvsAlertTag = new PvsAlertTag()
        pvsAlertTag.tagId = 100L
        pvsAlertTag.tagText = "tag1"
        pvsAlertTag.subTagText = "tag1"
        pvsAlertTag.domain =  "Single Case Alert"
        pvsAlertTag.alertId = 4441312L
        pvsAlertTag.createdAt = new Date()
        pvsAlertTag.modifiedAt = new Date()
        singleCaseAlert.addToPvsAlertTag(pvsAlertTag)
        pvsAlertTag.save(flush:true)

        executedConfiguration1 = new ExecutedConfiguration(id:3L,name: "test",
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
                pvrCaseSeriesId : 444,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration1.save(failOnError: true)


    }

    def cleanup() {
    }

    @Ignore
    void "test saveAlertTag() for single case alert with exception if wrong alertId"() {
        setup:
        AlertTagDTO alertTag = new AlertTagDTO()
        alertTag.tagId = 300L
        alertTag.tagText = "tagTest"
        alertTag.alertId = "4441312"
        alertTag.subTagText = "tag1"
        alertTag.domain = "Single Case Alert"
        alertTag.createdAt = "08/21/1996 00:00:00"
        alertTag.modifiedAt = "08/21/1996 00:00:00"
        alertTag.dmlType = "I"
        when:
        service.saveAlertTag(alertTag)

        then: "Exception is thrown while sending wrong alert id"
        thrown Exception
    }


    @Ignore
    void "test getAllAlertSpecificTags() for single case alert"() {
        setup:
        List<SingleCaseAlert> list = []
        list.add(singleCaseAlert)
        def domain = SingleCaseAlert
        when:
        List<Map> tags = service.getAllAlertSpecificTags(list , domain)
        then:
        assert tags[0].tagText == pvsAlertTag.tagText
    }
    void "test saveAlertTagVersions() Input dmlType"() {
        setup:
        println("-----------Input setup Started----------")
        categoryDTO = new CategoryDTO(
                martId: 123L,
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
                alertId: "1"
        )
        when:
        service.saveAlertTagVersions(categoryDTO)
        then:
        assert PvsAlertTag.count ==1
    }
    void "test saveAlertTagVersions() Delete dmlType"() {
        setup:
        println("-----------Delete setup Started----------")
        categoryDTO = new CategoryDTO(
                martId: 123L,
                catId: 1234L,
                subCatId: 1234L,
                catName: "tag1",
                subCatName: "tag1",
                createdBy: "temp",
                updatedBy: "temp",
                createdDate: new Date(),
                updatedDate: new Date(),
                priority: 1,
                dmlType: "D",
                alertId: "1"
        )
        pvsAlertTag.alertId = 1L
        pvsAlertTag.save(flush:true)
        when:
        service.saveAlertTagVersions(categoryDTO)
        then:
        assert PvsAlertTag.count ==0
    }
    void "test saveAlertTagVersions() Update dmlType"() {
        setup:
        println("-----------Update setup Started----------")
        categoryDTO = new CategoryDTO(
                martId: 123L,
                catId: 1234L,
                subCatId: 1234L,
                catName: "tag1",
                subCatName: "tag1",
                createdBy: "temp",
                updatedBy: "temp",
                createdDate: new Date(),
                updatedDate: new Date(),
                priority: 1,
                dmlType: "U",
                alertId: "1"
        )
        pvsAlertTag.alertId = 1L
        pvsAlertTag.save(flush:true)
        when:
        service.saveAlertTagVersions(categoryDTO)
        then:
        assert PvsAlertTag.count ==1
    }

    void "test for fetchMartDto()" () {
        setup:
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
        when:
        com.rxlogix.CategoryDTO category = service.fetchMartDto(categoryDTO)
        then:
        assert category?.catId == 1234
    }
    void "test for savePvsAlertTag()" (){
        setup:
        categoryDTO = new CategoryDTO(
                martId: 123L,
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
                alertId: "1"
        )
        List<CategoryDTO> categoryDTOList = []
        categoryDTOList.add(categoryDTO)
        service.metaClass.saveAlertTagVersions = { CategoryDTO cat -> return null }
        when:
        service.savePvsAlertTag(categoryDTOList)
        then:
        assert PvsAlertTag.count ==1
    }

    void "test for populateAlertTagsForVersions()"() {
        setup:
        categoryDTO = new CategoryDTO(
                martId: 123L,
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
                alertId: "1",
                factGrpCol1: "1",
                factGrpCol2: "1",
                factGrpCol3: "1212",
                factGrpCol4: "1",
                privateUserId: 1,
                module: "module",
                dataSource: "Safety",
                factGrpId: 1
        )
        List<CategoryDTO> categoryDTOList = []
        categoryDTOList.add(categoryDTO)
        com.rxlogix.CategoryDTO category = new com.rxlogix.CategoryDTO()
        service.metaClass.saveAlertTagVersions = { CategoryDTO cat -> return null }
        service.metaClass.fetchMartDto = { CategoryDTO cat -> return category }
        CategoryUtil.metaClass.static.saveCategories = { List<CategoryDTO> categoryList -> return true}
        when:
        service.populateAlertTagsForVersions(categoryDTOList)
        then:
        assert pvsAlertTag.count == 1
    }

    @Unroll
    void "test saveAlertTag() for single case , Single adhoc alert"() {
        setup:
        AlertTagDTO alertTag = new AlertTagDTO()
        alertTag.tagId = 300L
        alertTag.tagText = "tagTest"
        alertTag.subTagText = "tag1"
        alertTag.alertId = "1"
        alertTag.domain = alertType
        alertTag.createdAt = "08/21/1996 00:00:00"
        alertTag.modifiedAt = "08/21/1996 00:00:00"
        alertTag.dmlType = "I"
        alertTag.isActivity = false
        when:
        def output = service.saveAlertTag(alertTag, user)
        then:
        output.activityMap == [:]
        output.alertTagMap.alertSpecific == true
        output.alertTagMap.type == 'I'
        output.alertTagMap.id == 1

        where:
        alertType << ["Single Case Alert", "Single Case Alert on Demand"]

    }

    @Unroll
    void "test saveAlertTag() for Aggregate case alert, Aggregate on demand alert"() {
        setup:
        AlertTagDTO alertTag = new AlertTagDTO()
        alertTag.tagId = 300L
        alertTag.tagText = "tagTest"
        alertTag.subTagText = "tag1"
        alertTag.alertId = '1'
        alertTag.domain = alertType
        alertTag.createdAt = "08/21/1996 00:00:00"
        alertTag.modifiedAt = "08/21/1996 00:00:00"
        alertTag.dmlType = "I"
        alertTag.isActivity = false
        when:
        def output = service.saveAlertTag(alertTag, user)
        then:
        output.activityMap == [:]
        output.alertTagMap.alertSpecific == true
        output.alertTagMap.type == 'I'
        output.alertTagMap.id == 1
        where:
        alertType << ["Aggregate Case Alert", "Aggregate Case Alert on Demand"]


    }


    void "test saveAlertTag() for Literature alert"() {
        setup:
        AlertTagDTO alertTag = new AlertTagDTO()
        alertTag.tagId = 300L
        alertTag.tagText = "tagTest"
        alertTag.subTagText = "tag1"
        alertTag.alertId = '1'
        alertTag.domain = "Literature Alert"
        alertTag.createdAt = "08/21/1996 00:00:00"
        alertTag.modifiedAt = "08/21/1996 00:00:00"
        alertTag.dmlType = "I"
        alertTag.isActivity = false
        when:
        def output = service.saveAlertTag(alertTag, user)
        then:
        output.activityMap == [:]
        output.alertTagMap.alertSpecific == true
        output.alertTagMap.type == 'I'
        output.alertTagMap.id == 1
    }

    @Unroll
    void "test deleteAlertTag() for single case alert,Single adhoc alert"() {
        setup:
        PvsAlertTag tag = new PvsAlertTag(tagText:"tag1", subTagText: "tag1", alertId: 1L)
        tag.save(flush:true, validate: false)
        AlertTagDTO alertTag = new AlertTagDTO()
        alertTag.tagId = 100L
        alertTag.tagText = "tag1"
        alertTag.subTagText = "tag1"
        alertTag.alertId = "1"
        alertTag.domain = alertType
        alertTag.createdAt = "08/21/1996 00:00:00"
        alertTag.modifiedAt = "08/21/1996 00:00:00"
        alertTag.dmlType = "D"
        alertTag.isActivity = false
        when:
        def output = service.deleteAlertTag(alertTag,user,null)

        then:
        output.activityMap == [:]
        output.alertTagMap.alertSpecific == true
        output.alertTagMap.deleteTagsIds == [tag.id]
        where:
        alertType << ["Single Case Alert", "Single Case Alert on Demand"]

    }

    @Unroll
    void "test deleteAlertTag() for Aggregate Case Alert, Aggregate on demand alert "() {
        setup:
        PvsAlertTag tag = new PvsAlertTag(tagText:"tag1", subTagText: "tag1", alertId: 1L)
        tag.save(flush:true, validate: false)
        AlertTagDTO alertTag = new AlertTagDTO()
        alertTag.tagId = 100L
        alertTag.tagText = "tag1"
        alertTag.subTagText = "tag1"
        alertTag.alertId = "1"
        alertTag.domain = alertType
        alertTag.createdAt = "08/21/1996 00:00:00"
        alertTag.modifiedAt = "08/21/1996 00:00:00"
        alertTag.dmlType = "D"
        alertTag.isActivity = false
        when:
        def output = service.deleteAlertTag(alertTag,user,null)

        then:
        output.activityMap == [:]
        output.alertTagMap.alertSpecific == true
        output.alertTagMap.deleteTagsIds == [tag.id]
        where:
        alertType << ["Aggregate Case Alert", "Aggregate Case Alert on Demand"]

    }

    void "test deleteAlertTag() for Literature alert "() {
        setup:
        PvsAlertTag tag = new PvsAlertTag(tagText:"tag1", subTagText: "tag1", alertId: 1L)
        tag.save(flush:true, validate: false)
        AlertTagDTO alertTag = new AlertTagDTO()
        alertTag.tagId = 100L
        alertTag.tagText = "tag1"
        alertTag.subTagText = "tag1"
        alertTag.alertId = "1"
        alertTag.domain = "Literature Alert"
        alertTag.createdAt = "08/21/1996 00:00:00"
        alertTag.modifiedAt = "08/21/1996 00:00:00"
        alertTag.dmlType = "D"
        alertTag.isActivity = false
        when:
        def output = service.deleteAlertTag(alertTag,user,null)

        then:
        output.activityMap == [:]
        output.alertTagMap.alertSpecific == true
        output.alertTagMap.deleteTagsIds == [tag.id]

    }



}