package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.CaseForm
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import grails.web.mapping.LinkGenerator
import groovy.sql.GroovyResultSet
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.context.MessageSource
import org.springframework.test.context.jdbc.Sql
import spock.lang.*
import spock.util.mop.ConfineMetaClassChanges
import groovy.sql.Sql
import com.rxlogix.helper.LinkHelper

import javax.activation.DataSource
import java.sql.Connection
import java.sql.ResultSet

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestMixin(GrailsUnitTestMixin)
//@TestFor(CaseFormService)
@ConfineMetaClassChanges([CaseFormService])
//@Mock([SingleCaseAlertService, User, Priority, Configuration, ExecutedConfiguration, CaseForm, ViewInstance, AdvancedFilter, Disposition, Group,
//        SingleCaseAlert, InboxLog, AlertComment])
class CaseFormServiceSpec extends Specification implements ServiceUnitTest<CaseFormService>{

    Group wfGroup
    User user
    Priority priority
    Disposition disposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    CaseForm caseForm
    ViewInstance viewInstance
    AdvancedFilter advanceFilter
    def resultSet
    DataSource mockedSource
    GroovyResultSet groovyResultSet
    SignalDataSourceService signalDataSourceService
    SingleCaseAlert singleCaseAlert
    def attrMapObj

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(flush: true, failOnError: true)
        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultSignalDisposition: disposition,
                autoRouteDisposition: disposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(wfGroup);
        user.save(flush: true)

        priority = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush: true, failOnError: true)

        alertConfiguration = new Configuration(
                executing: false,
                priority: "High",
                alertTriggerCases: "11",
                alertTriggerDays: "11")

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

        executedConfiguration = new ExecutedConfiguration(name: "test",
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
                assignedTo: user, configId: 1,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(flush: true, failOnError: true)

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


        singleCaseAlert = new SingleCaseAlert(id: 1L,
                productId: 101,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseId: 1L,
                caseVersion: 1,
                priority: config.priority,
                productFamily: "Test Product A",
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                isNew: true,
                followUpExists: true,
                pt: "Rash",
                createdBy: user.username,
                modifiedBy: user.username,
                dateCreated: new Date(),
                lastUpdated: new Date(),
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                attributesMap: attrMapObj,
                comboFlag: "comboFlag",
                malfunction: "malfunction"
        )
        singleCaseAlert.save(flush: true, failOnError: true)


        caseForm = new CaseForm(
                formName: "formName",
                caseIds: "1",
                versionNum : "1",
                followUpNum: "",
                isDuplicate: "false",
                dateCreated: new Date(),
                createdBy : user,
                caseNumbers: "17JP00000000001410",
                isFullCaseSeries: false,
                advancedFilterName: "ad listed 1" ,
                viewInstanceName: "viewInstance",
                executedConfiguration: executedConfiguration
        )
        caseForm.save(flush:true, failOnError:true)

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

        def dataSource = Mock(Connection)
        signalDataSourceService = Mock(SignalDataSourceService)
        signalDataSourceService.getReportConnection(_) >> {
            return dataSource
        }
        service.signalDataSourceService = signalDataSourceService

        CacheService cacheService = Mock(CacheService)
        cacheService.getPreferenceByUserId(_) >> {
            return user.preference
        }
        service.cacheService=cacheService
        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.generateAdvancedFilterClosure(_,_) >> { return null }
        mockAlertService.generateDomainName(_) >> {
            return SingleCaseAlert
        }
        mockAlertService.getDispositionsForName(_)>> { return [] }
        mockAlertService.generateAlertList(_,_,_)>> {
            return [singleCaseAlert]
        }
        service.alertService = mockAlertService

        NotificationHelper notificationHelper=Mock(NotificationHelper)
        notificationHelper.pushNotification(_)>>{
            return true
        }
        service.notificationHelper=notificationHelper
    }

    def cleanup() {
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

    void "test callCaseFormProc"() {
        given:
        service.metaClass.getCaseDetailsMap = { CaseForm caseForm, Map caseInfoMap, Sql sql, List exportList, Map dataMap, Map mapObjectCursor ->

        }
        Sql sql
        Map<String, Map> dataMap = [:]
        when:
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(resultSet)

        }
        service.callCaseFormProc(caseForm, sql, dataMap )
        then:
        dataMap.size() ==0
    }

    void "test prepareDataMap"() {
        given:
        Map caseInfoMap = [
                'Dosage Regimen'            : ['Product Name': '', 'Therapy Start Date': '', 'Therapy Stop Date': '', 'Therapy Duration': '', 'Ongoing': '', 'Dose': '', 'Dose Unit': '', 'Daily Dose': '', 'Route': '', 'Frequency': '', 'Lot Number': '', 'Expiry Date': ''],
                'Event Information'         : ['Event PT': '', 'Reported Term': '', 'Event SOC': '', 'Event HLGT': '', 'Event HLT': '', 'Event LLT': '', 'Onset Date': '', 'Seriousness Criteria': '','Event Seriousness': '', 'Event Outcome': '', 'Medication Error?': '', 'Onset Latency': ''],
                'Cause Of Death Information': ['Cause Of Death Coded': '', 'Death Date': '', 'Autopsy': '', 'Autopsy Results': ''],
                'Product Event Information' : ['Product Name': '', 'Event PT': '', 'Core Listedness': '', 'IB Listedness': '', 'Reporter Causality': '', 'Company Causality': '', 'Rechallenge': '', 'Dechallenge': '', 'Time To Onset (Days)': ''],
                'Device Information'        : ['Product Name': '' ,'Generic Name': '' ,'Brand Name': '','Common Device Name': '', 'Malfunction': '','Device Usage': '','Product Code': '',
                                               'Serial#': '', 'Model#': '', 'Catalog#': '', 'Product Type/Others': '','Manufacturer': '', 'Lot#': '','UDI Number': '', 'Remedial Action': '','Follow-up Correction': ''],
                'Device Problems'           : ['Product Name': '', 'Generic Name': '', 'Brand Name/Common Device Name': '', 'Product Code':'', 'Product Type/Others': '', 'Device Problem Codes': ''],
                'Narrative'                 : ['Case Narrative': '', 'Case Abbreviated Narrative': ''],
                'Patient Medical History'   : ['Patient Condition Type': '', 'Patient Medical Condition PT': '', 'Notes': '', 'Start Date': '', 'End Date': ''],
                'Lab Data'                  : ['Lab Test Name': '', 'Test Date': '', 'Lab Data Result': '', 'Lab Data Result Unit': '', 'Normal High': '', 'Normal Low': '', 'Lab Data Assessment': ''],
                'Case References'           : ['Reference Type': '', 'Reference Number': ''],
                'Study Information'         : ['Study Name': '', 'Project ID': '', 'Study Title': '', 'Study ID': '', 'Center ID': ''],
                'Literature Information'    : ['Journal Title': '', 'Article Title': '', 'Author': '', 'Volume': '', 'Year': '', 'Page Number': ''],
                'Versions'                  : ['Versions': '']
        ]
        Map dataMap = [:]
        caseInfoMap.each { k, v ->
            dataMap.put(k, [])
        }
        when:
        service.prepareDataMap(groovyResultSet, caseInfoMap, dataMap, [:], Constants.CaseInforMapFields.LAB_INFORMATION)
        then:
        dataMap['Lab Data'].size() == 0
    }

    void "test listCaseForms"(){
        given:
        caseForm.executionStatus = ReportExecutionStatus.COMPLETED
        caseForm.save(flush: true)
        when:
        def result = service.listCaseForms(1L)
        then:
        result.size() ==1
    }
    void "test generateCaseFormExport"(){
        setup:
        service.metaClass.batchPersistGtt = { CaseForm caseForm1, Sql sql ->

        }
        when:
        service.generateCaseFormExport()
        then:
        caseForm.executionStatus == ReportExecutionStatus.GENERATING
    }
    //boolean error
    void "test fetchDataBackground"(){
        setup:
        service.metaClass.getFiltersFromParams = { Boolean request, Map param ->
            return []
        }
        service.metaClass.saveCaseForm = { def scaList, String filename, ExecutedConfiguration executedConfiguration, User user, def params ->
            return caseForm.id
        }
        Map params = [
                id: executedConfiguration.id,
                selectedCases: false,
                filterList: false
        ]
        params.isArchived = "true"
        params.isFaers = "true"
        when:
        def result = service.fetchDataBackground(params, user)
        then:
        result == null
    }

    void "test saveCaseForm"(){
        setup:
        Map params = [
                selectedCases: false,
                viewId: viewInstance.id
        ]
        when:
        def result =service.saveCaseForm([singleCaseAlert], "filename", executedConfiguration, user, params)
        then:
        result == 2
    }
    void "test sendSuccessNotification"(){
        setup:
        MessageSource messageSource = Mock(MessageSource)
        messageSource.getMessage(_, _, _) >> {
            return "app.signal.caseForm.report.success"
        }
        service.messageSource = messageSource
        when:
        service.sendSuccessNotification(caseForm, user)
        then:
        InboxLog.countById(1L) == 1
    }
    void "test sendErrorNotification"(){
        setup:
        MessageSource messageSource = Mock(MessageSource)
        messageSource.getMessage(_, _, _) >> {
            return "app.signal.caseForm.report.success"
        }
        service.messageSource = messageSource
        when:
        service.sendErrorNotification([id: executedConfiguration.id, filename: "filename"], user)
        then:
        InboxLog.countById(1L) == 1
    }
    void "test listCaseFormNames"(){
        when:
        def result = service.listCaseFormNames(executedConfiguration.id)
        then:
        result == ["formName"]
    }
    void "test fetchFile"(){
        setup:
        caseForm.savedName = "savedName"
        caseForm.save(flush:true)
        when:
        def result = service.fetchFile(caseForm.id)
        then:
        result.filename == "formName"
    }
    void "test getFiltersFromParams"(){
        setup:
        Map params = [
                filters: false,
                dashboardFilter: "total"
        ]
        when:
        def result = service.getFiltersFromParams(false, params)
        then:
        result == ["Validated Signal"]
    }

    void "test populateValueInMap"(){
        when:
        def result = service.populateValueInMap("",[:],[:],"",0,null,null,[:])
        then:
        println result
    }

}