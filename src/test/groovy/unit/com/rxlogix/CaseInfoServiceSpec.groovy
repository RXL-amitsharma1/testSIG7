package unit.com.rxlogix

import com.rxlogix.CaseInfoService
import com.rxlogix.Constants
import com.rxlogix.cache.CacheService
import com.rxlogix.SingleCaseAlertService
import com.rxlogix.config.*
import com.rxlogix.dto.CaseDataDTO
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll
import com.rxlogix.signal.GlobalCase
import spock.util.mop.ConfineMetaClassChanges

import javax.sql.DataSource
import java.sql.ResultSet

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@ConfineMetaClassChanges([CaseInfoService])
class CaseInfoServiceSpec extends Specification implements ServiceUnitTest<CaseInfoService> {

    def resultSet
    def resultSet1
    def attrMapObj
    Disposition disposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    Disposition defaultDisposition
    User user
    SingleCaseAlert singleCaseAlert
    ArchivedSingleCaseAlert archivedSingleCaseAlert
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    Group wfGroup
    GroovyResultSet groovyResultSet
    GlobalCase globalCase

    def setup() {
        disposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [disposition, defaultDisposition, defaultSignalDisposition, autoRouteDisposition].collect {
            it.save(failOnError: true)
        }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true,
        createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        wfGroup.save(validate: false)

        def mockedSource = Mock(DataSource)
        service.dataSource_pva = mockedSource
        service.dataSource_eudra = mockedSource
        def mockCaseInfoResult = ["Case Number"                 : "17JP00000000001410", "Initial Receipt Date": "07-01-17", "Follow Up Date": [null],
                                  "Report type"                 : "Report From Study", "Country": "JAPAN", "Death Date": [null],
                                  "Autopsy"                     : "<NULL>", "Generic Name": "oracle.sql.CLOB@14dbc126", "Drug Type": "Suspect", "Age Group": [null],
                                  "Indication"                  : [null], "Lot Number": [null], "Product Name": "Calpol", "Ongoing": [null], "Therapy Duration": [null],
                                  "Patient Medical Condition PT": [null], "Event PT": "Nausea", "Reported Term": "Nausea", "SOC": "Gastrointestinal disorders",
                                  "Onset Date"                  : [null], "Seriousness": "Serious", "Event Outcome": [null], "Seriousness Criteria": "-/-/-/-/Dis/RI/MS",
                                  "Pregnancy"                   : [null], "Autopsy Results": [null], "Latest followUp Date": "07-01-17",
                                  "HCP"                         : [null], "Reporter Country": [null], "Formulation": "Tablet  mg", "Core Listedness": "Not Listed",
                                  "IB Listedness"               : "Not Listed", "Reported Causality": [null], "Family Name": "Japan test family", "Determined Causality": [null],
                                  "Action Taken"                : [null], "rechallenge": [null], "Dechallenge": [null], "Notes": [null], "Patient Condition Type": [null],
                                  "Cause Of Death Coded"        : [null], "Therapy Start Date": [null], "Therapy Stop Date": [null], "Duration": [null],
                                  "Dose"                        : [null], "Dose Unit": [null], "Daily Dose": [null], "Expiry Date": [null], "Lab Test Name": [null], "Test Date": [null],
                                  "Lab Data Result"             : [null], "Lab Data result Unit": [null], "Normal High": [null], "Normal Low": [null], "Lab Data Assessment": [null],
                                  "Case Narrative"              : [null], "Case Abbreviated Narrative": [null], "Versions": "1"]

        def values = mockCaseInfoResult.values()
        def rows = []
        rows.add(values)
        resultSet = makeResultSet(mockCaseInfoResult.keySet().toList(), rows)

        groovyResultSet = makeGroovyResultSet(mockCaseInfoResult.keySet().toList(), rows)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: false)
        user.save(failOnError: true)

        mockDomain(Priority, [
                [value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1]
        ])

        alertConfiguration = new Configuration(id: 1,
                executing: false,
                priority: "High",
                alertTriggerCases: "11",
                alertTriggerDays: "11")

        executedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,configId: 1,
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

        GlobalCase globalCase = new GlobalCase()
        globalCase.caseId = 1212
        globalCase.versionNum = 1
        globalCase.save(failOnError: true)

        singleCaseAlert = new SingleCaseAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: alertConfiguration.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                productId: 894239,
                isNew: true,
                followUpExists: false,
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                malfunction: "function1",
                comboFlag: "flag1",
                attributesMap: attrMapObj)
        singleCaseAlert.save(failOnError: false)

        archivedSingleCaseAlert = new ArchivedSingleCaseAlert(id: 111L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: alertConfiguration.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                productId: 894239,
                isNew: true,
                followUpExists: false,
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                malfunction: "function1",
                comboFlag: "flag1",
                globalIdentity: globalCase,
                attributesMap: attrMapObj)
        archivedSingleCaseAlert.save(failOnError: false)


    }

    def cleanup() {
        Sql.metaClass = null
    }


    void "test getCaseDetailMap"() {
        given:
        String caseNumber = "17JP00000000001410"
        String version = "1"
        String followUpNumber = ""
        String alertId = "1"
        List exportList = []
        Boolean isFaers = false
        Boolean evdasCase = true
        Boolean isArchived = false
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        when:
        def caseDetailMap = service.getCaseDetailMap(caseNumber, version, followUpNumber, alertId, exportList, isFaers,
                evdasCase, false, false, isArchived)
        then:
        caseDetailMap.size() == 13
    }

    void "test getCaseInfoMap"() {
        given:
        String caseNumber = "17JP00000000001410"
        String version = "1"
        String followUpNumber = ""
        List exportList = []

        when:
        Map caseInfoMap = service.getCaseInfoMap(caseNumber, version, followUpNumber, exportList, false, true, 0)

        then:
        caseInfoMap.size() == 6
    }

    void "test getCaseInfoMap when caseNumber is empty "() {
        given:
        String caseNumber = "91US000472"
        String version = ""
        String followUpNumber = "0"

        when:
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(resultSet)

        }
        def caseInfoMap = service.getCaseInfoMap(caseNumber, version, followUpNumber, [], false, false, 1)
        then:
        if (Holders.config.custom.caseInfoMap.Enabled) {
            assert caseInfoMap.size() == 21
        } else {
            assert caseInfoMap.size() == 18
        }
    }

    void "test getCaseInfoMap when version is empty "() {
        given:
        String caseNumber = ""
        String version = "1"
        String followUpNumber = "0"

        when:
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(resultSet)

        }
        def caseInfoMap = service.getCaseInfoMap(caseNumber, version, followUpNumber, [], false, false, 1)
        then:
        if (Holders.config.custom.caseInfoMap.Enabled) {
            assert caseInfoMap.size() == 21
        } else {
            assert caseInfoMap.size() == 18
        }
    }

    void "test getVersionNumberList"() {
        given:
        List<String> versionNumbers = ["1", "2"]

        when:
        def versionNumberList = service.getVersionNumberList(versionNumbers)
        then:
        versionNumberList.size() == 2
        versionNumberList[0]["id"] == 1
        versionNumberList[0]["desc"] == "Follow-up Number#1"
        versionNumberList[1]["id"] == 2
        versionNumberList[1]["desc"] == "Follow-up Number#2"
    }


    void "test getVersionNumberList when versionNumbers are empty"() {
        given:
        List<String> versionNumbers = []

        when:
        def versionNumberList = service.getVersionNumberList(versionNumbers)
        then:
        assert versionNumberList.size() == 0

    }

    void "test getCiomsReportUrl"() {
        given:
        String caseNumber = "17JP00000000001410"
        String version = "1"

        when:
        def ciomsReportUrl = service.getCiomsReportUrl(caseNumber, version)
        then:
        assert ciomsReportUrl == Holders.config.pvreports.ciomsI.export.uri + caseNumber + "&versionNumber=" + version

    }

    void "test getCiomsReportUrl when caseNumber is empty"() {
        given:
        String caseNumber = ""
        String version = "1"

        when:
        def ciomsReportUrl = service.getCiomsReportUrl(caseNumber, version)
        then:
        assert ciomsReportUrl == ""

    }

    void "test getCiomsReportUrl when version is empty"() {
        given:
        String caseNumber = "17JP00000000001410"
        String version = ""

        when:
        def ciomsReportUrl = service.getCiomsReportUrl(caseNumber, version)
        then:
        assert ciomsReportUrl == ""

    }

    void "test getAlertDetailMap"() {
        given:
        String alertId = 1
        String caseNumber = "17JP00000000001410"
        String version = "1"
        String followUpNumber = ""
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService

        when:
        def alertDetail = service.getAlertDetailMap(alertId, caseNumber, version, followUpNumber, false)
        then:
        alertDetail.size() == 20
        alertDetail["alertId"] == "1"
        alertDetail["productName"] == "Test Product A"

    }

    void "test getAlertDetailMap when alertId is empty"() {
        given:
        String alertId = ""
        String caseNumber = "17JP00000000001410"
        String version = "1"
        String followUpNumber = ""

        when:
        def alertDetail = service.getAlertDetailMap(alertId, caseNumber, version, followUpNumber, false)
        then:
        alertDetail.size() == 0
    }


    @Unroll
    void "test getTreeViewNodes"() {
        when:
        def keys = service.getTreeViewNodes(alertType, wwid, 1L, false, false, "")

        then:
        keys.size() == keySize

        where:
        alertType           | wwid        | keySize
        "Single Case Alert" | null        | 6
        "Single Case Alert" | "Some WWID" | 6
        "EVDAS Alert"       | ""          | 1
        "EVDAS Alert"       | "Some WWID" | 1
    }

    void "test childNodesCaseDetail"() {
        given:
        Map caseInfoMapParam = caseInfoMapInput
        when:
        def keys = service.childNodesCaseDetail(caseInfoMapParam)
        then:
        caseInfoMapInput.keySet().size() - 1 == keys.size()
        keys[0]["text"] == "Case Information"
        keys[1]["text"] == "Product Information"

        where:
        alertType           | caseInfoMapInput
        "Single Case Alert" | [
                'Case Information'          : ['Case Number': '', 'Initial Receipt Date': '', 'Latest followUp Date': '', 'Report Type': '', 'Country': '', 'Seriousness': '', 'HCP': '', 'Reporter Country': '', 'Pregnancy': '', 'Age Group': ''],
                'Product Information'       : ['Product Name': '', 'Family Name': '', 'Generic Name': '', 'Indication': '', 'Formulation': '', 'Lot Number': '', 'Drug Type': ''],
                'Event Information'         : ['Event PT': '', 'Reported Term': '', 'SOC': '', 'Onset Date': '', 'Seriousness Criteria': '', 'Event Outcome': ''],
                'Product Event Information' : ['Product Name': '', 'Event PT': '', 'Reported Term': '', 'Core Listedness': '', 'IB Listedness': '', 'Reported Causality': '', 'Determined Causality': '', 'Action Taken': '', 'Rechallenge': '', 'Dechallenge': ''],
                'Patient Medical History'   : ['Patient Condition Type': '', 'Patient Medical Condition PT': '', 'Notes': ''],
                'Cause Of Death Information': ['Cause Of Death Coded': '', 'Death Date': '', 'Autopsy': '', 'Autopsy Results': ''],
                'Dosage Regimen'            : ['Product Name': '', 'Therapy Start Date': '', 'Therapy Stop Date': '', 'Therapy Duration': '', 'Ongoing': '', 'Dose': '', 'Dose Unit': '', 'Daily Dose': '', 'lot Number': '', 'Expiry Date': ''],
                'Lab Results'               : ['Lab Test Name': '', 'Test Date': '', 'Lab Data Result': '', 'Lab Data Result Unit': '', 'Normal High': '', 'Normal Low': '', 'Lab Data Assessment': ''],
                'Narrative'                 : ['Case Narrative': '', 'Case Abbreviated Narrative': ''],
                'Versions'                  : ['Versions': '']
        ]
        "EVDAS Alert"       | [
                'Case Information'         : ['Case Number': '', 'EV Gateway Receipt Date': '', 'Report Type': '', 'Country': '', 'Seriousness': '', 'HCP': '', 'Reporter Country': '', 'Age Group': ''],
                'Product Information'      : ['Product Name': '', 'Indication': '', 'Drug Type': ''],
                'Event Information'        : ['Event PT': '', 'Reported Term': '', 'SOC': '', 'Seriousness Criteria': '', 'Event Outcome': ''],
                'Product Event Information': ['Product Name': '', 'Event PT': '', 'Reported Term': '', 'Action Taken': ''],
                'Dosage Regimen'           : ['Product Name': '', 'Therapy Duration': '', 'Dose': ''],
                'Versions'                 : ['Versions': '']
        ]
    }


    void "test getEvdasCaseDetailMap"() {
        given:
        String caseNumber = "17JP00000000001410"
        String version = "1"
        Integer alertId = 1
        List exportList = []
        when:
        def caseDetailMap = service.getEvdasCaseDetailMap(caseNumber, version, alertId, exportList)
        then:
        caseDetailMap.size() == 7
    }


    void "test getEvdasCaseDetailMapWithWWID"() {
        given:
        String wwid = "17JP00000000001410"
        Integer alertId = 1

        when:
        def caseDetailMap = service.getEvdasCaseDetailMap(wwid, alertId)

        then:
        caseDetailMap.size() == 0
    }


    void "test getEvdasCaseInfoMap"() {
        given:
        String caseNumber = "17JP00000000001410"
        String version = "1"

        when:
        def caseInfoMap = service.getEvdasCaseInfoMap(caseNumber, version, [])
        then:
        caseInfoMap.size() == 6
    }


    void "test getFullCaseListData()"() {
        given:
        String caseListString = '[{"caseNumber":"18JP0005514","caseVersion":1,"alertId":5363},{"caseNumber":"18JP0004432","caseVersion":2,"alertId":5362},{"caseNumber":"91US000472","caseVersion":3,"alertId":5364},{"caseNumber":"13US000195","caseVersion":1,"alertId":5361}]'
        List caseList = [["caseNumber": "18JP0005514", "caseVersion": 1, "alertId": 5363], ["caseNumber": "18JP0004432", "caseVersion": 2, "alertId": 5362], ["caseNumber": "91US000472", "caseVersion": 3, "alertId": 5364], ["caseNumber": "13US000195", "caseVersion": 1, "alertId": 5361]]
        String caseNumber = "18JP0004432"
        Integer version = 2
        Long alertId = 5362
        CaseDataDTO caseDataDTO =
                new CaseDataDTO(caseList: caseList, caseNumber: caseNumber, caseListString: caseListString, version: version, alertId: alertId)

        when:
        Map resultData = service.getFullCaseListData(caseDataDTO)

        then:
        resultData.next == [caseNumber: "91US000472", caseVersion: 3, alertId: 5364]
        resultData.previous == [caseNumber: "18JP0005514", caseVersion: 1, alertId: 5363]
        resultData.size() == 6
        resultData.fullCaseList == '[{"caseNumber":"18JP0005514","caseVersion":1,"alertId":5363},{"caseNumber":"18JP0004432","caseVersion":2,"alertId":5362},{"caseNumber":"91US000472","caseVersion":3,"alertId":5364},{"caseNumber":"13US000195","caseVersion":1,"alertId":5361}]'
    }

    void "test getFullCaseListData"() {
        given:
        String caseListString = '[{"caseNumber":"18JP0005514","caseVersion":1,"alertId":5363},{"caseNumber":"18JP0004432","caseVersion":2,"alertId":5362},{"caseNumber":"91US000472","caseVersion":3,"alertId":5364},{"caseNumber":"13US000195","caseVersion":1,"alertId":5361}]'
        List caseList = [["caseNumber": "18JP0005514", "caseVersion": 1, "alertId": 5363], ["caseNumber": "18JP0004432", "caseVersion": 2, "alertId": 5362], ["caseNumber": "91US000472", "caseVersion": 3, "alertId": 5364], ["caseNumber": "13US000195", "caseVersion": 1, "alertId": 5361]]
        String caseNumber = "18JP0004432"
        Integer version = 2
        Long alertId = 5362
        Map params = [length:2,start:0]
        CaseDataDTO caseDataDTO =
                new CaseDataDTO(caseList: caseList, caseNumber: caseNumber, caseListString: caseListString, version: version, alertId: alertId, totalCount:200, id:executedConfiguration?.id, params:params)
        SingleCaseAlertService singleCaseAlertServiceMock = Mock(SingleCaseAlertService)
        singleCaseAlertServiceMock.fetchNextXFullCaseList(_) >> {
            return [["caseNumber": "18JP0005514", "caseVersion": 1, "alertId": 5363], ["caseNumber": "18JP0004432", "caseVersion": 2, "alertId": 5362], ["caseNumber": "91US000472", "caseVersion": 3, "alertId": 5364], ["caseNumber": "13US000195", "caseVersion": 1, "alertId": 5361]]
        }
        service.singleCaseAlertService = singleCaseAlertServiceMock

        when:
        Map resultData = service.getFullCaseListData(caseDataDTO)

        then:
        assert resultData.next.caseNumber == "18JP0005514"
        assert resultData.previous.caseNumber == "91US000472"
    }

    void "test getFullCaseListData2()"() {
        given:
        String caseListString = null
        List caseList = [["caseNumber": "18JP0005514", "caseVersion": 1, "alertId": 5363], ["caseNumber": "18JP0004432", "caseVersion": 2, "alertId": 5362], ["caseNumber": "91US000472", "caseVersion": 3, "alertId": 5364], ["caseNumber": "13US000195", "caseVersion": 1, "alertId": 5361]]
        String caseNumber = "18JP0005514"
        Integer version = 1
        Long alertId = 5363
        Map params = [length:2,start:0]
        CaseDataDTO caseDataDTO =
                new CaseDataDTO(caseList: caseList, caseNumber: caseNumber, caseListString: caseListString, version: version, alertId: alertId, totalCount:200, id:executedConfiguration?.id, params:params)
        SingleCaseAlertService singleCaseAlertServiceMock = Mock(SingleCaseAlertService)
        singleCaseAlertServiceMock.fetchNextXFullCaseList(_) >> {
            return [["caseNumber": "18JP0005514", "caseVersion": 1, "alertId": 5363], ["caseNumber": "18JP0004432", "caseVersion": 2, "alertId": 5362], ["caseNumber": "91US000472", "caseVersion": 3, "alertId": 5364], ["caseNumber": "13US000195", "caseVersion": 1, "alertId": 5361]]
        }
        service.singleCaseAlertService = singleCaseAlertServiceMock


        when:
        Map resultData = service.getFullCaseListData(caseDataDTO)

        then:
        assert resultData.previous == null
        assert resultData.next.caseNumber == "18JP0004432"
    }


    void "test getFullCaseListData when caseNumber is Blank"() {
        given:
        String caseListString = '[{"caseNumber":"18JP0005514","caseVersion":1,"alertId":5363},{"caseNumber":"18JP0004432","caseVersion":2,"alertId":5362},{"caseNumber":"91US000472","caseVersion":3,"alertId":5364},{"caseNumber":"13US000195","caseVersion":1,"alertId":5361}]'
        List caseList = [["caseNumber": "18JP0005514", "caseVersion": 1, "alertId": 5363], ["caseNumber": "18JP0004432", "caseVersion": 2, "alertId": 5362], ["caseNumber": "91US000472", "caseVersion": 3, "alertId": 5364], ["caseNumber": "13US000195", "caseVersion": 1, "alertId": 5361]]
        String caseNumber = ""
        Integer version = null
        Long alertId = 5362
        CaseDataDTO caseDataDTO =
                new CaseDataDTO(caseList: caseList, caseNumber: caseNumber, caseListString: caseListString, version: version, alertId: alertId)

        when:
        Map resultData = service.getFullCaseListData(caseDataDTO)

        then:
        assert resultData == null
    }

    void "test prepareDataMap"() {
        given:
        Map caseInfoMap = service.grailsApplication.config.pvsignal.caseInformationMap_3
        Map dataMap = [:]
        caseInfoMap.each { k, v ->
            dataMap.put(k, [])
        }
        when:
        service.prepareDataMap(groovyResultSet, caseInfoMap, dataMap, [], Constants.CaseInforMapFields.LAB_INFORMATION)
        then:
        dataMap['Lab Data'].size() == 1
    }

    private ResultSet makeResultSet(List<String> aColumns, List rows) {
        ResultSet result = Mock()
        int currentIndex = -1
        result.next() >> { ++currentIndex < rows.size() }
        result./get(String|Short|Date|Int|Timestamp)/(_) >> { String argument ->
            rows[currentIndex][aColumns.indexOf(argument)]
        }
        return result
    }

    private GroovyResultSet makeGroovyResultSet(List<String> aColumns, List rows) {
        GroovyResultSet result = Mock()
        int currentIndex = -1
        result.next() >> { ++currentIndex < rows.size() }
        result./get(String|Short|Date|Int|Timestamp)/(_) >> { String argument ->
            rows[currentIndex][aColumns.indexOf(argument)]
        }
        return result
    }
    void "test populateCaseInformationMapPVA"() {
        given:
        String caseNumber = "91US000472"
        String version = ""
        String followUpNumber = "0"

        when:
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(resultSet)

        }
        def caseInfoMap = service.populateCaseInformationMapPVA(caseNumber, version, followUpNumber, [], 1)
        then:
        if (Holders.config.custom.caseInfoMap.Enabled) {
            assert caseInfoMap.size() == 21
        } else {
            assert caseInfoMap.size() == 18
        }
    }
    void "test getDefaultFollowUpNumber"() {
        given:
        String caseNumber = "91US000472"
        String version = ""
        service.signalDataSourceService = [getReportConnection: { String selectedDatasource -> return service.dataSource_pva}]

        when:
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(resultSet)

        }
        String followUpNumber = service.getDefaultFollowUpNumber(caseNumber, version)
        then:
        noExceptionThrown()
    }

    void "test fetchCaseVersions when source is argus"() {
        given:
        String executionId = "1"
        String caseId = "2121"
        String caseNumber = "33333"
        String alertId = "111"
        def mockVersionsListResult = [["versionNum" : 1, "receiptDate": "21-Apr-2019", "followUpNum": "0", "flagSignificant": "1", "sourceType": "ARGUS"],
                                  ["versionNum" : 2, "receiptDate": "21-Dec-2019", "followUpNum": "1", "flagSignificant": "1", "sourceType": "ARGUS"]]
        CacheService cacheService = Mock(CacheService)
        cacheService.getDispositionByReviewCompleted() >> [disposition]
        service.cacheService = cacheService
        service.metaClass.fetchLastSingleReviewedVersion = { Long execution, String caseNum -> return 1 }
        service.metaClass.callVersionListProc = { String execution, String caseIds, Sql sql -> return mockVersionsListResult }

        when:
        def versionsList = service.fetchCaseVersions(executionId, caseId, false, caseNumber, alertId,false,false)

        then:
        assert versionsList.size() == 5
        assert versionsList.previousVersion == 1
        assert versionsList.isArgusDataSource == true

    }

    void "test fetchCaseVersions when source is arisg"() {
        given:
        String executionId = "1"
        String caseId = "2121"
        String caseNumber = "33333"
        String alertId = "111"
        def mockVersionsListResult = [["versionNum" : 1, "receiptDate": "21-Apr-2019", "followUpNum": "0", "flagSignificant": "1", "sourceType": "ARIS"],
                                      ["versionNum" : 2, "receiptDate": "21-Dec-2019", "followUpNum": "1", "flagSignificant": "1", "sourceType": "ARIS"]]
        CacheService cacheService = Mock(CacheService)
        cacheService.getDispositionByReviewCompleted() >> [disposition]
        service.cacheService = cacheService
        service.metaClass.fetchLastSingleReviewedVersion = { Long execution, String caseNum, String isAdhoc -> return null }
        service.metaClass.callVersionListProc = { String execution, String caseIds, Sql sql -> return mockVersionsListResult }

        when:
        def versionsList = service.fetchCaseVersions(executionId, caseId, false, caseNumber, alertId,false,false)

        then:
        assert versionsList.size() == 5
        assert versionsList.previousVersion == null
        assert versionsList.isArgusDataSource == false
    }

    void "test fetchLastSingleReviewedVersion"(){
        given:
        String caseNumber = "1S01"
        CacheService cacheService = Mock(CacheService)
        cacheService.getDispositionByReviewCompleted() >> [disposition]
        service.cacheService = cacheService

        when:
        Integer version = service.fetchLastSingleReviewedVersion(executedConfiguration?.id, caseNumber,false)

        then:
        assert version == 1
    }

    void "test compareCategories"(){
        given:
        Map map1 = ['tagText': 'tag1', 'subTagText': 'subTag1;subTag2', 'privateUser': '', 'tagType': '']
        Map map2 = ['tagText': 'tag2', 'subTagText': 'subTag2', 'privateUser': '', 'tagType': '']
        Map map3 = ['tagText': 'tag3', 'subTagText': 'subTag3', 'privateUser': '', 'tagType': '']
        Map map4 = ['tagText': 'tag1', 'subTagText': 'subTag2', 'privateUser': '', 'tagType': '']
        List categories1 = [map1,map2]
        List cateegories2 = [map3,map4]

        when:
        List categories = service.compareCategories(categories1,cateegories2)
        List categoriesAdded = categories.findAll{it.comparedType == 'A'}
        List categoriesDeleted = categories.findAll{it.comparedType == 'D'}
        List categoriesUnchanged = categories.findAll{it.comparedType == 'U'}


        then:
        assert categories.size() == 4
        assert categoriesUnchanged.size() == 1
        assert categoriesDeleted.size() == 1
        assert categoriesAdded.size() == 2

    }

    void "test compareCategories2"(){
        given:
        Map map1 = ['tagText': 'tag1', 'subTagText': 'subTag1;subTag2', 'privateUser': '', 'tagType': '']
        Map map2 = ['tagText': 'tag2', 'subTagText': 'subTag2', 'privateUser': '', 'tagType': '']
        Map map6 = ['tagText': 'tag4', 'subTagText': 'subTag5;subTag6;subTag7', 'privateUser': '', 'tagType': '']
        Map map3 = ['tagText': 'tag3', 'subTagText': 'subTag3', 'privateUser': '', 'tagType': '']
        Map map4 = ['tagText': 'tag1', 'subTagText': 'subTag2;subTag4', 'privateUser': '', 'tagType': '']
        Map map5 = ['tagText': 'tag2', 'subTagText': 'subTag2', 'privateUser': '', 'tagType': '(A)']
        Map map7 = ['tagText': 'tag4', 'subTagText': 'subTag5;subTag6', 'privateUser': '', 'tagType': '']
        List categories1 = [map1,map2,map6]
        List cateegories2 = [map3,map4,map5,map7]

        when:
        List categories = service.compareCategories(categories1,cateegories2)
        List categoriesAdded = categories.findAll{it.comparedType == 'A'}
        List categoriesDeleted = categories.findAll{it.comparedType == 'D'}
        List categoriesUnchanged = categories.findAll{it.comparedType == 'U'}


        then:
        assert categories.size() ==8
        assert categoriesUnchanged.size() ==2
        assert categoriesDeleted.size() == 3
        assert categoriesAdded.size() == 3

    }

    void "test putValueInSectionMap"(){
        when:
        def result = service.putValueInSectionMap([],[:])
        then:
        result == null
    }

    void "test getCaseDetailsMap"(){
        setup:
        service.metaClass.getAlertDetailMap = {
            String alertId, String caseNumber ,String version, String followUpNumber ,boolean isArchived ->
            return [:]
        }
        when:
        def result = service.getCaseDetailMap("","","","",[],true,false,true,false,false)
        then:
        result.data == [:]
    }

    void "test sorDataMap"(){
        when:
        def result = service.sorDataMap([:],[],[:])
        then:
        result == null
    }

    void "test populateValueInMap"(){
        setup:
        Map map = new LinkedHashMap()
        when:
        def result = service.populateValueInMap("","",map)
        then:
        result == null
    }

    void "test putValueInSectionMap_"(){
        setup:
        Map map = new LinkedHashMap()
        when:
        def result = service.putValueInSectionMap([],map)
        then:
        result == null
    }

    void "test getTreeViewNodesForFlexible"(){
        setup:
        List list = new ArrayList()
        when:
        def result = service.getTreeViewNodesForFlexible("", null,null, [] as String)
        then:
        result[0].children == []
    }

    void "test childNodesCaseDetailForFlexible"(){
        setup:
        List list = new ArrayList()
        when:
        def result = service.childNodesCaseDetailForFlexible(list)
        then:
        result == []
    }

    void "test getUniqueColumns"(){
        setup:
        Map map = new LinkedHashMap()
        when:
        def result = service.getUniqueColumns(map, false)
        then:
        result == [deviceInformation:"", freeTextField:[]]
    }

    void "test createTreeMapForCaseComp"(){
        setup:
        List list = new ArrayList()
        when:
        def result = service.createTreeMapForCaseComp(list, list)
        then:
        result == []
    }

    void "test compareVersionInfo"(){
        setup:
        List list = new ArrayList()
        when:
        def result = service.createTreeMapForCaseComp(list, list)
        then:
        result == []
    }

}
