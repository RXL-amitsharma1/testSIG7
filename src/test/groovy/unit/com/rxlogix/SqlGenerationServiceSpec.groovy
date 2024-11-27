package unit.com.rxlogix

import com.rxlogix.Constants
import com.rxlogix.SqlGenerationService
import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

import static com.rxlogix.util.Strings.collapseWhitespace

@Ignore
class SqlGenerationServiceSpec extends Specification implements ServiceUnitTest<SqlGenerationService> {

    String JSONQuery = '{ "all": { "containerGroups": [   { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" }  ] }  ] } }'

    Disposition disposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    User user

    def setup() {
        Priority priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
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
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(validate: false)

        alertConfiguration = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user
        )
        alertConfiguration.save(failOnError: true)

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
                assignedTo: user,type : Constants.AlertConfigType.SINGLE_CASE_ALERT,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
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
                executedTemplate: 25l, createdBy: user.username, modifiedBy: user.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery, headerProductSelection: false,
                headerDateRange: false,
                blindProtected: false,
                privacyProtected: false,
                queryLevel: QueryLevelEnum.CASE
        )
        executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
        executedConfiguration.save(failOnError: true)

    }

    def cleanup() {}

    @Ignore
    void "Query Level: generate ReportSQL for Case Level Query"() {
        given: "A configuration with a Case level query and a template"
        def service = new SqlGenerationService()
        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template: Case Number',
                columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: ReportField.findByName("eventEfficacy"))]))
        Configuration configuration = new Configuration()
        def templateQuery = new TemplateQuery(template: template.id, query: 1, dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                queryLevel: QueryLevelEnum.CASE)
        configuration.addToTemplateQueries(templateQuery)
        configuration.save()

        when: "Generating Query SQL"
        String result = service.generateReportSQL(templateQuery, true, templateQuery.template)

        then: "Report SQL is Case Level"
        templateQuery.queryLevel == QueryLevelEnum.CASE
        collapseWhitespace(result) == collapseWhitespace("""select   cm.CASE_NUM AS CASE_NUM0 from V_CASE_INFO cm  where exists
                        (select 1 from gtt_query_case_list caseList
                            where cm.case_id = caseList.case_id and
                                  cm.version_num = caseList.dlp_revision_number
                                  and caseList.TENANT_ID = cm.TENANT_ID
                        ) """)
    }

    @Ignore
    void "Query Level: generate ReportSQL for Product & Event Level Query"() {
        given: "A configuration with a Product & Event level query and a template"
        def service = new SqlGenerationService()
        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template: Case Number',
                columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: ReportField.findByName("eventEfficacy"))]))
        Configuration configuration = new Configuration()
        def templateQuery = new TemplateQuery(template: template.id, query: 1, dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                queryLevel: QueryLevelEnum.PRODUCT_EVENT)
        configuration.addToTemplateQueries(templateQuery)
        configuration.save()

        when: "Generating Query SQL"
        String result = service.generateReportSQL(templateQuery, true, templateQuery.template)

        then: "Report SQL is normal"
        templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT
        collapseWhitespace(result) == collapseWhitespace(""" select   cm.CASE_NUM AS CASE_NUM0 from V_CASE_INFO cm join CASE_AE_INFO ce on (cm.CASE_ID = ce.CASE_ID and cm.version_num = ce.version_num AND cm.TENANT_ID = ce.TENANT_ID )  join CASE_PROD_INFO cp on (cm.CASE_ID = cp.CASE_ID and cm.version_num = cp.version_num AND cm.TENANT_ID = cp.TENANT_ID )   where exists
                        (select 1 from gtt_query_case_list caseList
                            where cm.case_id = caseList.case_id and
                                  cm.version_num = caseList.dlp_revision_number
                                  and caseList.TENANT_ID = cm.TENANT_ID
                         and ce.ae_seq_num = caseList.event_seq_num and cp.prod_seq_num = caseList.prod_seq_num)""")
    }

    void "test initializeGTTForMissedCases"() {
        service.alertService = [fetchPrevExecConfigId: { ExecutedConfiguration executedConfiguration, Configuration configuration, boolean isCaseSeriesId, boolean isCumulative ->
            [[1, 2], [3, 4]]
        }
        ]
        when:
        String insertMissedCasesSql = service.initializeGTTForMissedCases(alertConfiguration, executedConfiguration)
        then:
        collapseWhitespace(insertMissedCasesSql) == collapseWhitespace(""" Begin execute immediate('delete from GTT_PREV_CASES_ALERT_DTLS'); 
                                                          Insert into GTT_PREV_CASES_ALERT_DTLS (CONFIG_ID,EXECUTION_ID,CASE_SERIES_ID,TENANT_ID) VALUES (1,1,2,1);  
                                                          Insert into GTT_PREV_CASES_ALERT_DTLS (CONFIG_ID,EXECUTION_ID,CASE_SERIES_ID,TENANT_ID) VALUES (1,3,4,1); 
                                                          END;""")
    }

    void "test initializeGTTForMissedCases if there are no previous executions"() {
        service.alertService = [fetchPrevExecConfigId: { ExecutedConfiguration executedConfiguration, Configuration configuration, boolean isCaseSeriesId, boolean isCumulative -> []}]
        when:
        String insertMissedCasesSql = service.initializeGTTForMissedCases(alertConfiguration, executedConfiguration)
        then:
        insertMissedCasesSql.length() == 0
    }


}
