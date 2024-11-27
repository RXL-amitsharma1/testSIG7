package unit.com.rxlogix

import com.rxlogix.ConfigurationService
import com.rxlogix.Constants
import com.rxlogix.EvdasAlertExecutionService
import com.rxlogix.LiteratureExecutionService
import com.rxlogix.ReportExecutorService
import com.rxlogix.QueryService
import grails.converters.JSON
import grails.testing.services.ServiceUnitTest
import net.fortuna.ical4j.model.property.RRule
import org.grails.web.json.JSONObject
import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.enums.*
import com.rxlogix.user.*
import grails.plugin.springsecurity.SpringSecurityService
import groovy.time.TimeCategory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.text.SimpleDateFormat

@Ignore
class ConfigurationServiceSpec extends Specification implements ServiceUnitTest<ConfigurationService> {

    private static final String SIMPLE_DATE = "yyyy-MM-dd HH:mm"
    public static final user = "unitTest"

    @Shared caseMasterTable
    @Shared lmCountriesTable
    @Shared caseMasterColumnCountry
    @Shared caseInformationRFG
    @Shared countryOfIncidenceRF

    @Autowired
    ReportExecutorService reportExecutorService

    /**
     * Generally speaking the recurrence JSON string ignores the timeZone object; it uses the timezone in the passed in startDateTime
     *
     * Use -Duser.timezone=GMT to force tests to run in a different timezone
     * use --echoOut for force println output into standard out
     */

    User userNew
    Disposition defaultSignalDisposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    ExecutedConfiguration executedConfiguration
    ExecutedTemplateQuery executedTemplateQuery
    ExecutedTemplateQuery executedTemplateQueryCustom
    Priority priority
    Group wfGroup
    Configuration configuration
    ExecutionStatus executionStatus
    ExecutionStatusDTO executionStatusDTO

    def setup() {

        priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush:true)

        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, defaultSignalDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        userNew = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        userNew.addToGroups(wfGroup)
        userNew.preference.createdBy = "createdBy"
        userNew.preference.modifiedBy = "modifiedBy"
        userNew.preference.locale = new Locale("en")
        userNew.preference.isEmailEnabled = false
        userNew.preference.timeZone = 'UTC'
        userNew.metaClass.getFullName = { 'Fake Name' }
        userNew.metaClass.getEmail = { 'fake.email@fake.com' }
        userNew.save(flush:true)

        configuration = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: userNew.username, modifiedBy: userNew.username,
                assignedTo: userNew,
                priority: priority,
                owner: userNew,
                isEnabled: true,
                nextRunDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
        )
        configuration.save(flush:true,failOnError: true)

        executedConfiguration = new ExecutedConfiguration(name: "test", configId: configuration.id,
                owner: userNew, scheduleDateJSON: "{}", nextRunDate: new Date(),
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
                createdBy: userNew.username, modifiedBy: userNew.username,
                assignedTo: userNew,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        executedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        executedAlertDateRangeInformation.dateRangeStartAbsolute = new Date()
        executedAlertDateRangeInformation.dateRangeEndAbsolute = new Date()
        executedConfiguration.executedAlertDateRangeInformation = executedAlertDateRangeInformation
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                dateRangeEndAbsolute: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(), dateRangeStartAbsolute: new Date()
        )
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQueryCustom = new ExecutedDateRangeInformation(
                dateRangeEnum: DateRangeEnum.CUSTOM,
                dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date()
        )
        executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: 25l, createdBy: userNew.username, modifiedBy: userNew.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery, headerProductSelection : false,
                headerDateRange : false,
                blindProtected : false,
                privacyProtected : false,
                queryLevel : QueryLevelEnum.CASE
        )
        executedTemplateQueryCustom = new ExecutedTemplateQuery(
                executedTemplate: 25l, createdBy: userNew.username, modifiedBy: userNew.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQueryCustom, headerProductSelection : false,
                headerDateRange : false,
                blindProtected : false,
                privacyProtected : false,
                queryLevel : QueryLevelEnum.CASE
        )
        executedConfiguration.save(failOnError: true)

        SpringSecurityService mockSpringSecurityService = Mock(SpringSecurityService)
        mockSpringSecurityService.getCurrentUser() >> {
            return userNew
        }
        service.springSecurityService = mockSpringSecurityService

        executionStatus = new ExecutionStatus(configId: configuration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(),
                nextRunDate: new Date(),
                reportVersion: 1, message: 'for testing purpose',
                owner: userNew, name: 'executionStatus')

        executionStatus.save(flush:true)

        executionStatusDTO = new ExecutionStatusDTO(alertType: AlertType.AGGREGATE_CASE_ALERT, searchString: "hql",offset: 5,max: 20,
                executionStatus: ReportExecutionStatus.SCHEDULED , currentUser : userNew,configurationDomain: configuration.getClass(),
                sort: "nextRunDate", direction: "direction",workflowGroupId: userNew.workflowGroup.id)
    }

    void setupSpec() {
        buildReportFields()
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"))
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    void buildReportFields() {
        caseMasterTable = new SourceTableMaster(tableName: "CASE_MASTER", tableAlias: "cm", tableType: "C", caseJoinOrder: 1)
        lmCountriesTable = new SourceTableMaster(tableName: "LM_COUNTRIES", tableAlias: "lco", tableType: "L", caseJoinOrder: null)
        caseMasterColumnCountry = new SourceColumnMaster(tableName: caseMasterTable, columnName: "COUNTRY_ID",
                primaryKey: null, lmTableName: lmCountriesTable, lmJoinColumn: "COUNTRY_ID",
                lmDecodeColumn: "COUNTRY", columnType: "N", reportItem: "CM_COUNTRY_ID")
        caseInformationRFG = new ReportFieldGroup(name: "Case Information")

        //Purposely leaving out listDomainClass
        countryOfIncidenceRF = new ReportField(name: "masterCountryId",
                fieldGroup: caseInformationRFG, sourceColumn: caseMasterColumnCountry,
                dataType: String.class)
    }

    void saveReportFields() {
        caseMasterTable.save(failOnError: true)
        lmCountriesTable.save(failOnError: true)
        caseMasterColumnCountry.save(failOnError: true)
        caseInformationRFG.save(failOnError: true)
        countryOfIncidenceRF.save(failOnError: true)
    }

    private toJSON(Date date) {
        new SimpleDateFormat(ConfigurationService.JSON_DATE).format(date)
    }

    private Date toDate(String simpleDate) {
        Date.parse(SIMPLE_DATE, simpleDate)
    }

    // Use this method to help debug time/date & timeZone issues, have to use --echoOut for force println output into standard out on console
    private void debugSummary(Date startTime, Date next, Date expectedRecurrence, String recurrenceJSON) {
        println "Summary TimeZone: ${TimeZone.default.ID}, startTime: ${startTime}, next: ${next}, expectedRecurrence: ${expectedRecurrence}"
        println "Summary recurrenceJSON: ${recurrenceJSON}"
        println "Summary times as long startTime: ${startTime.time}, next: ${next.time}, expectedRecurrence: ${expectedRecurrence.time}"
    }

    void "test deleteConfig"(){
        when:
        def result = service.deleteConfig(configuration)

        then:
        result == configuration
        result.isDeleted == true
        result.isEnabled == false
    }

    void "test getDelta when isEnabled is true"(){
        setup:
        service.metaClass.getNextDate = { Date -> new Date() }

        when:
        Integer result = service.getDelta(configuration)

        then:
        result == new Date() - new DateTime(2015, 12, 15, 0, 0, 0).toDate()
    }

    void "test getDelta when isEnabled is false"(){
        given:
        Configuration config1 = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test1",
                productSelection: "Test Product A",
                createdBy: userNew.username, modifiedBy: userNew.username,
                assignedTo: userNew,
                priority: priority,
                owner: userNew,
                isEnabled: false,
                nextRunDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
        )
        config1.save(flush:true)

        when:
        Integer result = service.getDelta(config1)

        then:
        result == 0
    }

    void "test prepareHqlForExecutionStatus"(){
        when:
        String result = service.prepareHqlForExecutionStatus(executionStatusDTO,"hql")

        then:
        result ==  """
               Select ec from ExecutionStatus ec,Configuration config 
                 where config.name = ec.name 
                 and config.type = ec.type
                 and ec.type = 'Aggregate Case Alert'
                 and config.isDeleted = 0
                 and config.workflowGroup.id = 1
                 and ec.executionStatus = 'SCHEDULED'
                 hql
                 order by ec.nextRunDate direction
               """
    }

    void "test prepareCountHqlForExecutionStatus"(){
        when:
        String result = service.prepareCountHqlForExecutionStatus(executionStatusDTO,"hql")

        then:
        result ==  """
               Select count(*) from ExecutionStatus ec,Configuration config 
                 where config.name = ec.name 
                 and config.type = ec.type
                 and ec.type = 'Aggregate Case Alert'
                 and config.isDeleted = 0
                 and config.workflowGroup.id = 1
                 and ec.executionStatus = 'SCHEDULED'
                 hql
               """
    }

    void "test getOrderStringForExecutionStatus"(){
        expect:
        service.getOrderStringForExecutionStatus(nextRunDate,direction) == result

        where:
        nextRunDate | direction || result
        "nextRunDate" | "direction" || "order by ec.nextRunDate direction"
        "currentToNextRunDate" | "direction" || "order by UPPER(ec.currentToNextRunDate) direction"
    }

    void "test getSearchStringForExecutionStatus"() {
        expect:
        service.getSearchStringForExecutionStatus(searchString) == result

        where:
        searchString              || result
        ""                        || ""
        "searchString" || "AND( lower(ec.name) like lower('%searchString%') OR " +
                "lower(ec.owner.fullName) like lower('%searchString%') )"
    }

    void "test getAlertTypeStringForExecutionStatus"(){
        expect:
        service.getAlertTypeStringForExecutionStatus(alertType) == result

        where:
        alertType || result
        AlertType.AGGREGATE_CASE_ALERT || "and config.type = ec.type"
        AlertType.SINGLE_CASE_ALERT || "and config.type = ec.type"
        AlertType.EVDAS_ALERT || ''
    }

    void "test ConfigurationMap"(){
        setup:
        Configuration config1 = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test1",
                productSelection: "Test Product A",
                createdBy: userNew.username, modifiedBy: userNew.username,
                assignedTo: userNew,
                priority: priority,
                owner: userNew,
                isEnabled: false,
                nextRunDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
        )
        config1.save(flush:true)

        MessageSource mockMessageSource = Mock(MessageSource)
        mockMessageSource.getMessage(_,_,_) >> {
            return "app.alert.type.dropdown"
        }
        service.messageSource = mockMessageSource

        when:
        List<Map> result = service.configurationMap([configuration,config1],AlertType.AGGREGATE_CASE_ALERT)

        then:

        result[0].name == "test"
        result[0].frequency == "Run Once"
        result[0].executionStatus == "Scheduled"

        result[1].name == "test1"
        result[1].frequency == "Run Once"
        result[1].executionStatus == "Scheduled"
    }

    void "test fetchRunningAlertList"(){
        setup:
        ReportExecutorService mockReportExecutorService = Mock(ReportExecutorService)
        mockReportExecutorService.currentlyQuantRunning >> {
            return [1L,2L]
        }
        mockReportExecutorService.currentlyRunning >> {
            return [1L]
        }
        service.reportExecutorService = mockReportExecutorService

        EvdasAlertExecutionService mockEvdasAlertExecutionService = Mock(EvdasAlertExecutionService)
        mockEvdasAlertExecutionService.currentlyRunning >> {
            return [1L,3L]
        }
        service.evdasAlertExecutionService = mockEvdasAlertExecutionService

        LiteratureExecutionService mockLiteratureExecutionService = Mock(LiteratureExecutionService)
        mockLiteratureExecutionService.currentlyRunning >> {
            return [3L,5L]
        }
        service.literatureExecutionService = mockLiteratureExecutionService

        expect:
        service.fetchRunningAlertList(type) == result

        where:
        type                                        || result
        Constants.ConfigurationType.QUAL_TYPE       || [1L]
        Constants.ConfigurationType.QUANT_TYPE      || [1L, 2L]
        Constants.ConfigurationType.EVDAS_TYPE      || [1L,3L]
        Constants.ConfigurationType.LITERATURE_TYPE || [3L,5L]
    }


    void "No scheduler"() {
        given: "A configuration with no scheduled date"
        def config = new Configuration()

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "No start time in scheduler"() {
        given: "A configuration"

        when: "We try to get the recurring date with a null startDateTime"
        def recurrenceJSON = /{"timeZone":{"name":"Pacific Standard Time","offset":"-8:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "No time zone in scheduler"() {
        given: "Start date is now"
        def now = new Date()
        when: "We try to get the recurring date with a null timeZone"
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "No recurrence pattern in scheduler"() {
        given: "Start date is now"
        def now = new Date()

        when: "We try to get the recurring date with a null recurrencePattern"
        def recurrenceJSON = /{"startDateTime":"$now","timeZone":{"name":"Pacific Standard Time","offset":"-8:00"}}/
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)
        Date next = service.getNextDate(config)

        then: "Next run date is null"
        next == null
    }

    void "Recurrence returns a null date after recurring x number of times"() {
        given: "A configuration which recurs daily multiple times from 7 days ago"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(new Date() - 7)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=$count;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date after we have recurred x number of times"
        Date next = service.getNextDate(config)
        int recurrences = -1
        while (next != null) {
            next = service.getNextDate(config)
            config.nextRunDate = next
            recurrences++
        }

        then: "Next run date is null"
        recurrences == count
        next == null

        where:
        count << [2, 3, 5, 10, 15]
    }

    void "Recurrence occurs until the end date, excluding the end date unless it is the same day as the start date"() {
        given: "A configuration which runs daily until past days like yesterday"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=$frequency;INTERVAL=$interval;UNTIL=$endDate;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON)

        when: "Calculating the next run date"
        Date next
        for (int i = 0; i < 2; i++) {
            next = service.getNextDate(config)
            config.nextRunDate = next
        }

        then: "Next run date matches the recurrence rule until the end date"
        Calendar cal = Calendar.getInstance()
        Date expectedDate
        if (next) {
            cal.setTime(startTime)
            cal.add(Calendar.DAY_OF_YEAR, interval)
            expectedDate = cal.getTime()
        }
        next == expectedDate

        where:
        // Add frequency as another parameter to test
        startTime << [toDate("2014-08-24 21:30"), toDate("2014-08-24 21:30"), toDate("2014-04-24 21:30")]
        endDate << ["20140829", "20140825", "20140510"]
        interval << [3, 3, 10]
        frequency << ["DAILY", "DAILY", "DAILY"]
    }

    void "Run once 10 minutes from now"() {
        given: "A configuration with scheduled run once 10 mins from now"
        def now = new Date()
        def tenMinutesFromNow = new Date()
        tenMinutesFromNow.clearTime()
        use(TimeCategory) {
            tenMinutesFromNow += now.hours.hours + now.minutes.minutes + 10.minutes
        }
        def recurrenceJSON = """{"startDateTime":"${ toJSON(tenMinutesFromNow)
        }","timeZone":{"name":"Pacific Standard Time","offset":"-8:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is 10 minutes from now"
        next.after(now)
        next == tenMinutesFromNow
    }


    void "PVR-131: Schedule Recurrence: None (only once) -- test PAST dates"() {
        given: "A configuration with no recurrence (run once)"
        Calendar cal = Calendar.getInstance()
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.SECOND, 0)
        Date today = cal.getTime()
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=$interval;COUNT=1;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date in the past"
        Date next = service.getNextDate(config)
        cal.setTime(next)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.SECOND, 0)
        next = cal.getTime()

        then: "Next run date is today"
        next == today
        SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE)

        where:
        // generate dates which have no seconds/millis
        startTime << [toDate(sdf.format((new Date() - 1))),
                      toDate(sdf.format((new Date() - 10))),
                      toDate(sdf.format((new Date() - 1000)))]
        // In this case, interval is ignored.
        interval << [1, 10, 10000]
    }

    /**
     *  CLEANED UP BELOW THIS COMMENT
     */

    void "PVR-131: Schedule Recurrence: None (only once) -- test FUTURE dates"() {
        given: "A configuration with no recurrence (run once)"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=$interval;COUNT=1;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date in the future (ignore the seconds)"
        Date next = service.getNextDate(config)

        then: "Next run date is the start date"
        next == startTime
        SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE)

        where:
        // generate dates which have no seconds/millis
        startTime << [toDate(sdf.format((new Date() + 1))),
                      toDate(sdf.format((new Date() + 10))),
                      toDate(sdf.format((new Date() + 1000)))]
        // In this case, interval is ignored.
        interval << [5, 6, 7]
    }

    void "PVR-132: Schedule Recurrence: Hourly -- test PAST dates"() {
        given: "A configuration which runs hourly with start date 10 minutes before"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=HOURLY;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next
        for (int i = 0; i < 2; i++) {
            next = service.getNextDate(config)
            config.nextRunDate = next
        }
        Calendar cal = Calendar.getInstance()
        cal.setTime(startTime)
        cal.add(Calendar.HOUR, interval)
        Date recurDate = cal.getTime()

        then: "Next run date is an hour later than start time"
        next.after(startTime)
        next == recurDate

        where:
        startTime << [toDate("2014-08-24 21:30"), toDate("2014-02-24 21:30"), toDate("2014-02-24 23:30")]
        interval << [1, 2, 3]
    }

    void "PVR-132: Schedule Recurrence: Hourly -- test FUTURE dates"() {
        given: "A configuration which runs hourly with future start date"
        def now = new Date()
        startTime.clearTime()
        use(TimeCategory) {
            startTime += now.hours.hours + now.minutes.minutes + 1000.minutes
        }

        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=HOURLY;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is start time"
        next == startTime

        where:
        startTime << [new Date() + 1, new Date() + 2, new Date() + 10, new Date() + 100, new Date() + 1000]
        interval << [1, 2, 5, 10, 100]
    }

    void "PVR-134: Schedule Recurrence: Weekdays"() {
        given: "A configuration which runs on weekdays"
        Calendar cal = Calendar.getInstance() // locale-specific
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)
        cal.setTime(next)

        then: "Next run date is next weekday (No Saturday or Sunday)"
        cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
        cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
        // Should compare the actual date

        where:
        startTime << [toDate("2014-08-23 21:30"), toDate("2014-12-28 21:30"),
                      (new Date() - 10),
                      (new Date() - 1),
                      (new Date() + 1),
                      (new Date() + 100),
                      (new Date() + 1000)]
        interval << [1, 2, 3, 4, 5, 10, 100]
    }

    void "PVR-138: Schedule Recurrence: Weekly"() {
        given: "A configuration which runs on every Tuesday"
        Calendar cal = Calendar.getInstance() // locale-specific
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=WEEKLY;BYDAY=TU;INTERVAL=$interval;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)
        cal.setTime(next)

        then: "Next run date is next Tuesday"
        cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY
        // Can compare the next run date

        where:
        startTime << [toDate("2014-08-23 21:30"), toDate("2014-09-05 21:30"),
                      (new Date() - 10),
                      (new Date() - 1),
                      (new Date() + 1),
                      (new Date() + 100),
                      (new Date() + 1000)]
        interval << [1, 2, 3, 4, 5, 10, 100]
    }

    void "PVR-139: Schedule Recurrence: Monthly by day of month"() {
        given: "A configuration which runs monthly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"+07:00"},"recurrencePattern":"FREQ=MONTHLY;INTERVAL=$interval;BYMONTHDAY=$onDay;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the next available month that has this day"

        debugSummary(startTime, next, expectedRecurrence, recurrenceJSON)
        next == expectedRecurrence

        where: "There are different start times"
        //First date accounts for daylights saving time
        startTime << [toDate("2014-02-23 21:30"), toDate("2014-09-05 21:30")]
        expectedRecurrence << [toDate("2014-03-30 21:30"), toDate("2014-09-05 21:30")]
        onDay << [30, 5]
        interval << [1, 2]
    }

    void "PVR-139: Schedule Recurrence: Monthly -- test BYSETPOS"() {
        given: "A configuration which runs monthly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=MONTHLY;INTERVAL=1;BYDAY=$onDay;BYSETPOS=$pos;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the first day of month"
        next == expectedRecurrence

        where:
        startTime << [toDate("2014-02-23 21:30"), toDate("2014-09-15 21:30")]
        expectedRecurrence << [toDate("2014-03-1 21:30"), toDate("2014-09-21 21:30")]
        onDay << ["SA", "SU"]
        pos << [1, 3]
    }

    void "PVR-140: Schedule Recurrence: Yearly -- test BYMONTHDAY"() {
        given: "A configuration which runs yearly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=YEARLY;BYMONTH=$onMonth;BYMONTHDAY=$onDay;"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the date in recurrence pattern"
        next == expectedRecurrence

        where:
        startTime << [toDate("2014-02-03 21:30"), toDate("2014-09-15 21:30"), toDate("2014-09-15 21:30")]
        onMonth << [6, 2, 1]
        onDay << [22, 30, 1]
        expectedRecurrence << [toDate("2014-06-22 21:30"), null, toDate("2015-01-01 21:30")]
    }

    void "PVR-140: Schedule Recurrence: Yearly -- test BYSETPOS"() {
        given: "A configuration which runs yearly"
        def recurrenceJSON = """{"startDateTime":"${ toJSON(startTime)
        }","timeZone":{"name":"Mountain Standard Time","offset":"-07:00"},"recurrencePattern":"FREQ=YEARLY;BYDAY=$onDay;BYSETPOS=$pos;BYMONTH=$onMonth"}"""
        def config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)

        when: "Calculating the next run date"
        Date next = service.getNextDate(config)

        then: "Next run date is the first day of month"
        next == expectedRecurrence

        where:
        startTime << [toDate("2014-05-23 21:30"), toDate("2014-09-15 21:30")]
        expectedRecurrence << [toDate("2014-09-06 21:30"), toDate("2015-01-18 21:30")]
        pos << [1, 3]
        onDay << ["SA", "SU"]
        onMonth << [9, 1]
    }

    def "check start date" () {
        given:
        def scheduledJSON1 = """{"startDateTime":"2020-07-03T00:00Z","timeZone":{"name":"UTC","offset":"+00:00"},
                            "recurrencePattern":"FREQ=MONTHLY;INTERVAL=3;BYMONTHDAY=3;COUNT=3"}"""
        def scheduledJSON2 = """{"startDateTime":"2020-07-03T00:00Z","timeZone":{"name":"UTC","offset":"+00:00"},
                            "recurrencePattern":"FREQ=MONTHLY;INTERVAL=3;BYMONTHDAY=1;COUNT=3"}"""
        JSONObject timeObject1 = JSON.parse(scheduledJSON1)
        JSONObject timeObject2 = JSON.parse(scheduledJSON2)
        Date startDate1 = Date.parse("yyyy-MM-dd'T'HH:mmXXX", timeObject1.startDateTime)
        Date startDate2 = Date.parse("yyyy-MM-dd'T'HH:mmXXX", timeObject2.startDateTime)
        RRule recurRule1 = new RRule(timeObject1.recurrencePattern)
        RRule recurRule2 = new RRule(timeObject2.recurrencePattern)

        when:
        boolean actualResponse1 = service.checkStartDate(startDate1, recurRule1)
        boolean actualResponse2 = service.checkStartDate(startDate2, recurRule2)

        then:
        assert actualResponse1 == false
        assert actualResponse2 == true
    }

    void "test getUpdatedStartandEndDate"() {
        given: "A configuration which runs on every Tuesday"
            def recurrenceJSON = """{"startDateTime":"2020-07-21T05:38Z","timeZone":{"name":"Mountain Standard Time","offset":"+00:00"},"recurrencePattern":"FREQ=$frequency;BYDAY=TU;INTERVAL=1;"}"""
            Configuration config = new Configuration(scheduleDateJSON: recurrenceJSON, isEnabled: true)
            AlertDateRangeInformation alertDateRangeInformation = new AlertDateRangeInformation(dateRangeStartAbsolute: toDate("2019-03-01 21:30"), dateRangeEndAbsolute: toDate("2019-03-31 21:30"))
            config.alertDateRangeInformation = alertDateRangeInformation
        when: "Calculating the updated date Range"
            List<Date> updatedDateRange = service.getUpdatedStartandEndDate(config)

        then: "Next run date is next Tuesday"
            updatedDateRange[0] == expectedValueStartDate
            updatedDateRange[1] == expectedValueEndDate

        where:
            frequency | expectedValueStartDate     | expectedValueEndDate
            'WEEKLY'  | toDate("2019-03-08 21:30") | toDate("2019-04-7 21:30")
            'MONTHLY' | toDate("2019-04-01 21:30") | toDate("2019-04-30 21:30")
            'YEARLY'  | toDate("2020-03-01 21:30") | toDate("2020-03-31 21:30")
            'DAILY'   | toDate("2019-03-02 21:30") | toDate("2019-04-01 21:30")
    }
}
