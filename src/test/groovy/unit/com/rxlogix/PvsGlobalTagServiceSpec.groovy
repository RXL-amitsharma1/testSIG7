package unit.com.rxlogix


import com.rxlogix.PvsGlobalTagService
import com.rxlogix.UserService
import com.rxlogix.config.ActivityType
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.AlertTagDTO
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.GlobalCase
import com.rxlogix.signal.GlobalProductEvent
import com.rxlogix.signal.PvsGlobalTag
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.hibernate.SessionFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import com.rxlogix.signal.GlobalArticle
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.ArchivedLiteratureAlert
import spock.lang.Unroll

class PvsGlobalTagServiceSpec extends Specification implements ServiceUnitTest<PvsGlobalTagService> {
    Disposition disposition
    Configuration configuration
    ExecutedConfiguration executedConfiguration
    User user
    def attrMapObj
    SingleCaseAlert singleCaseAlert
    AggregateCaseAlert aggregateAlert
    PvsGlobalTag pvsGlobalTag
    GlobalCase globalCase
    GlobalProductEvent globalProductEvent
    Group wfGroup

    void setup() {
        service.sessionFactory = Mock(SessionFactory)

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
                priority: config.priority,
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

        globalCase= new GlobalCase()
        globalCase.caseId = singleCaseAlert.caseId
        globalCase.versionNum = 1
        globalCase.save(failOnError: true)


        GlobalCase globalCase1 = new GlobalCase()
        globalCase1.caseId = 1212
        globalCase1.versionNum = 2
        globalCase1.save(failOnError: true)

        singleCaseAlert.globalIdentity = globalCase
        singleCaseAlert.save(validate: false)

        globalProductEvent = new GlobalProductEvent()
        globalProductEvent.productEventComb = "12321312-1421"
        globalProductEvent.save(failOnError: true)

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
        aggregateAlert.globalIdentity = globalProductEvent
        aggregateAlert.save(failOnError: true)

        pvsGlobalTag = new PvsGlobalTag()
        pvsGlobalTag.tagId = 100L
        pvsGlobalTag.tagText = "tag1"
        pvsGlobalTag.domain =  "Single Case Alert"
        pvsGlobalTag.globalId = globalCase.id
        pvsGlobalTag.createdAt = new Date()
        pvsGlobalTag.modifiedAt = new Date()
        globalCase.addToPvsGlobalTag(pvsGlobalTag)
        pvsGlobalTag.save(failOnError: true)

        PvsGlobalTag pvsGlobalTag1 = new PvsGlobalTag()
        pvsGlobalTag1.tagId = 103L
        pvsGlobalTag1.tagText = "tag1"
        pvsGlobalTag1.domain =  "Single Case Alert"
        pvsGlobalTag1.globalId = globalCase1.id
        pvsGlobalTag1.createdAt = new Date()
        pvsGlobalTag1.modifiedAt = new Date()
        globalCase1.addToPvsGlobalTag(pvsGlobalTag1)
        pvsGlobalTag1.save(failOnError: true)

        PvsGlobalTag pvsGlobalTag3 = new PvsGlobalTag()
        pvsGlobalTag3.tagId = 103L
        pvsGlobalTag3.tagText = "tag1"
        pvsGlobalTag3.subTagText = "subTag1"
        pvsGlobalTag3.domain = "Single Case Alert"
        pvsGlobalTag3.globalId = 3
        pvsGlobalTag3.createdAt = new Date()
        pvsGlobalTag3.modifiedAt = new Date()
        pvsGlobalTag3.save(flush: true, validate: false)

        PvsGlobalTag pvsGlobalTag4 = new PvsGlobalTag()
        pvsGlobalTag4.tagId = 103L
        pvsGlobalTag4.tagText = "tag1"
        pvsGlobalTag4.subTagText = "subTag1"
        pvsGlobalTag4.domain = "Single Case Alert"
        pvsGlobalTag4.globalId = 2
        pvsGlobalTag4.createdAt = new Date()
        pvsGlobalTag4.modifiedAt = new Date()
        pvsGlobalTag4.save(flush: true, validate: false)

        PvsGlobalTag pvsGlobalTag5 = new PvsGlobalTag()
        pvsGlobalTag5.tagId = 103L
        pvsGlobalTag5.tagText = "tag1"
        pvsGlobalTag5.subTagText = "subTag1"
        pvsGlobalTag5.domain = "Single Case Alert"
        pvsGlobalTag5.globalId = 1
        pvsGlobalTag5.createdAt = new Date()
        pvsGlobalTag5.modifiedAt = new Date()
        pvsGlobalTag5.save(flush: true, validate: false)
    }

    def cleanup() {
    }


    void "test mapTagSubtags()"() {
        setup:
        List<Map> codeValues = []
        codeValues.add([id: "500" as Long, text: "tag111", parentId: null as Long])
        codeValues.add([id: "501" as Long, text: "tag222", parentId: "501" as Long])
        codeValues.add([id: "502" as Long, text: "tag333", parentId: null as Long])
        codeValues.add([id: "503" as Long, text: "tag444", parentId: "502" as Long])
        when:
        List<Map> tagsubTagsMap = service.mapTagSubtags(codeValues)
        then:
        assert tagsubTagsMap.size() == 2

    }

    void "test getAllGlobalTags() for single case alert"() {
        setup:
        List<Long> list = []
        list.add(singleCaseAlert.id)
        def mockedUserService = Mock(UserService)
        mockedUserService.getCurrentUserName() >> user.username
        service.userService = mockedUserService
        def myCriteria = [
                list : {Closure  cls -> [PvsGlobalTag.findAll()]}
        ]
        PvsGlobalTag.metaClass.static.createCriteria = { myCriteria }
        PvsGlobalTag.createCriteria()
        when:
        List<Map> tags = service.getAllGlobalTags(list , "Single Case Alert")
        then:
        assert tags.size() == 0
    }

    void "test syncGlobalTags() Insert dmlType "(){
        setup:
        List<PvsGlobalTag> globalTagList =[]
        globalTagList.add(pvsGlobalTag)
        def dmlType = "I"
        when:
        service.syncGlobalTags(globalTagList, dmlType)
        then:
        assert PvsGlobalTag.count == 6
    }
    void "test syncGlobalTags() For Delete dmlType"(){
        setup:
        List<PvsGlobalTag> globalTagList =[]
        globalTagList.add(pvsGlobalTag)
        def dmlType = "D"
        println(globalCase.id)
        println(pvsGlobalTag.globalId)
        when:
        service.syncGlobalTags(globalTagList, dmlType)
        then:
        assert PvsGlobalTag.count == 4
    }
    void "test syncGlobalTags() For Update dmlType "(){
        setup:
        List<PvsGlobalTag> globalTagList =[]
        globalTagList.add(pvsGlobalTag)
        def dmlType = "U"
        when:
        service.syncGlobalTags(globalTagList, dmlType)
        then:
        assert PvsGlobalTag.count == 5
    }

    void "test syncGlobalTags() Insert dmlType"(){
        setup:
        List<PvsGlobalTag> globalTagList =[]
        globalTagList.add(pvsGlobalTag)
        def dmlType = "I"
        when:
        service.syncGlobalTags(globalTagList, dmlType)
        then:
        assert PvsGlobalTag.count == 6
    }

    void "test syncGlobalTags() For Delete dmlType "(){
        setup:
        List<PvsGlobalTag> globalTagList =[]
        globalTagList.add(pvsGlobalTag)
        def dmlType = "D"
        println(globalCase.id)
        println(pvsGlobalTag.globalId)
        when:
        service.syncGlobalTags(globalTagList, dmlType)
        then:
        assert PvsGlobalTag.count == 4
    }

    void "test syncGlobalTags() For Update dmlType"(){
        setup:
        List<PvsGlobalTag> globalTagList =[]
        globalTagList.add(pvsGlobalTag)
        def dmlType = "U"
        when:
        service.syncGlobalTags(globalTagList, dmlType)
        then:
        assert PvsGlobalTag.count == 5
    }

    @Unroll
    void "test saveGlobalTag() for single case , Single adhoc alert"() {
        setup:
        AlertTagDTO globalTag = new AlertTagDTO()
        globalTag.tagId = 300L
        globalTag.tagText = "tagTest"
        globalTag.subTagText = "tag1"
        globalTag.alertId = "1"
        globalTag.domain = alertType
        globalTag.createdAt = "08/21/1996 00:00:00"
        globalTag.modifiedAt = "08/21/1996 00:00:00"
        globalTag.dmlType = "I"
        globalTag.globalId = "12321312-1421"
        globalTag.isActivity = isActivity
        when:
        def output = service.saveGlobalTag(globalTag, user)
        then:
        output.activityMap == [:]
        output.alertTagMap.type == 'I'
        output.alertTagMap.globalId != null


        where:
        alertType                     | isActivity
        "Single Case Alert"           | false
        "Single Case Alert on Demand" | true
        "Single Case Alert on Demand" | false
    }


    @Unroll
    void "test saveGlobalTag() for Aggregate case , Aggregate case adhoc "() {
        setup:
        AlertTagDTO globalTag = new AlertTagDTO()
        globalTag.tagId = 300L
        globalTag.tagText = "tagTest"
        globalTag.subTagText = "tag1"
        globalTag.alertId = "1"
        globalTag.domain = alertType
        globalTag.createdAt = "08/21/1996 00:00:00"
        globalTag.modifiedAt = "08/21/1996 00:00:00"
        globalTag.dmlType = "I"
        globalTag.globalId = "12321312-1421"
        globalTag.isActivity = isActivity
        when:
        def output = service.saveGlobalTag(globalTag, user)
        then:
        output.activityMap == [:]
        output.alertTagMap.type == 'I'
        output.alertTagMap.globalId != null


        where:
        alertType                        | isActivity
        "Aggregate Case Alert"           | false
        "Aggregate Case Alert on Demand" | true
        "Aggregate Case Alert on Demand" | false
    }

    void "test saveGlobalTag() for Literature alert"() {
        setup:
        AlertTagDTO globalTag = new AlertTagDTO()
        globalTag.tagId = 300L
        globalTag.tagText = "tagTest"
        globalTag.subTagText = "tag1"
        globalTag.alertId = "1"
        globalTag.domain = alertType
        globalTag.createdAt = "08/21/1996 00:00:00"
        globalTag.modifiedAt = "08/21/1996 00:00:00"
        globalTag.dmlType = "I"
        globalTag.globalId = "1421"
        globalTag.isActivity = isActivity
        when:
        def output = service.saveGlobalTag(globalTag, user)
        then:
        output.activityMap == [:]
        output.alertTagMap.type == 'I'
        output.alertTagMap.globalId != null


        where:
        alertType          | isActivity
        "Literature Alert" | false
    }

    @Unroll
    void "test deleteGlobalTag() for SingleCaseAlert, single adhoc alert"() {
        setup:
        AlertTagDTO globalTag = new AlertTagDTO()
        globalTag.tagId = 300L
        globalTag.tagText = "tag1"
        globalTag.subTagText = "subTag1"
        globalTag.domain = alertType
        globalTag.createdAt = "08/21/1996 00:00:00"
        globalTag.modifiedAt = "08/21/1996 00:00:00"
        globalTag.dmlType = "I"
        globalTag.globalId = globalCase.id + '-1'
        globalTag.isActivity = isActivity
        when:
        def output = service.deleteGlobalTag(globalTag, user, null)
        then:
        output.activityMap == [:]
        output.alertTagMap.deleteTagsIds == [3]
        where:
        alertType                        | isActivity
        "Single Case Alert"              | false
        "Single Case Alert on Demand"    | true
        "Single Case Alert on Demand"    | false

    }
    @Unroll
    void "test deleteGlobalTag() for Aggregate, Aggregate adhoc alert"() {
        setup:
        AlertTagDTO globalTag = new AlertTagDTO()
        globalTag.tagId = 300L
        globalTag.tagText = "tag1"
        globalTag.subTagText = "subTag1"
        globalTag.domain = alertType
        globalTag.createdAt = "08/21/1996 00:00:00"
        globalTag.modifiedAt = "08/21/1996 00:00:00"
        globalTag.dmlType = "I"
        globalTag.globalId = globalCase.id + '-1'
        globalTag.isActivity = isActivity
        when:
        def output = service.deleteGlobalTag(globalTag, user, null)
        then:
        output.activityMap == [:]
        output.alertTagMap.deleteTagsIds == [4]
        where:
        alertType                        | isActivity
        "Aggregate Case Alert"           | false
        "Aggregate Case Alert on Demand" | true
        "Aggregate Case Alert on Demand" | false

    }

    void "test deleteGlobalTag() for Literature alert"() {
        setup:
        AlertTagDTO globalTag = new AlertTagDTO()
        globalTag.tagId = 300L
        globalTag.tagText = "tag1"
        globalTag.subTagText = "subTag1"
        globalTag.domain = alertType
        globalTag.createdAt = "08/21/1996 00:00:00"
        globalTag.modifiedAt = "08/21/1996 00:00:00"
        globalTag.dmlType = "I"
        globalTag.globalId = globalCase.id
        globalTag.isActivity = isActivity
        when:
        def output = service.deleteGlobalTag(globalTag, user, null)
        then:
        output.activityMap == [:]
        output.alertTagMap.deleteTagsIds == [5]
        where:
        alertType          | isActivity
        "Literature Alert" | false
    }

    void "test importAggregateGlobalTagsWithKeyId"(){
        setup:
        Map map = [pEComb:"test",productKeyId:1,eventKeyId:1]
        when:
        Long result = service.getGlobalId(map)
        then:
        result  == 2

    }

}
