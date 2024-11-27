package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.RuleDataDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.BusinessConfiguration
import com.rxlogix.signal.AlertTag
import com.rxlogix.signal.GlobalCase
import com.rxlogix.signal.RuleInformation
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleGlobalTag
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestMixin(ControllerUnitTestMixin)
//@TestFor(BusinessConfigurationService)
@ConfineMetaClassChanges([BusinessConfigurationService])
//@Mock([BusinessConfiguration, Disposition, Configuration, ExecutedConfiguration, AggregateCaseAlert, EvdasAlert,
//        ExecutedEvdasConfiguration, EvdasConfiguration, CacheService, Disposition, User, Group, AlertTag, Priority, GlobalCase,
//        SingleGlobalTag, AlertTag, SingleCaseAlert])
class BusinessConfigurationServiceSpec extends Specification implements ServiceUnitTest<BusinessConfigurationService> {

    BusinessConfiguration businessConfiguration
    AggregateCaseAlert aggregateCaseAlert1
    AggregateCaseAlert aggregateCaseAlert2
    ExecutedConfiguration executedConfiguration1
    ExecutedConfiguration executedConfiguration2
    Configuration configuration

    EvdasAlert evdasAlert1
    EvdasAlert evdasAlert2
    ExecutedEvdasConfiguration executedEvdasConfiguration1
    ExecutedEvdasConfiguration executedEvdasConfiguration2
    EvdasConfiguration evdasConfiguration
    RuleInformation ruleInformation
    User user
    Group wfGroup
    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    SingleCaseAlert sca
    Priority priority
    GlobalCase globalCase

    def setup() {

        String product = "Test Product"
        String event = "Rash"
        configuration = new Configuration()
        configuration.save(validate:false)
        executedConfiguration1 = new ExecutedConfiguration()
        executedConfiguration1.save(validate:false)
        executedConfiguration2 = new ExecutedConfiguration()
        executedConfiguration2.save(validate:false)
        aggregateCaseAlert1 = new AggregateCaseAlert(name: "Aggregate Case Alert 1",executedAlertConfiguration: executedConfiguration1,configuration:configuration, productName: product,event:event)
        aggregateCaseAlert1.save(validate:false)
        aggregateCaseAlert2= new AggregateCaseAlert(name: "Aggregate Case Alert 2",executedAlertConfiguration: executedConfiguration2,configuration:configuration, productName: product,event:event)
        aggregateCaseAlert2.save(validate:false)

        evdasConfiguration = new EvdasConfiguration()
        evdasConfiguration.save(validate:false)
        executedEvdasConfiguration1 = new ExecutedEvdasConfiguration()
        executedEvdasConfiguration1.save(validate:false)
        executedEvdasConfiguration2 = new ExecutedEvdasConfiguration()
        executedEvdasConfiguration2.save(validate:false)
        evdasAlert1 = new EvdasAlert(name: "Evdas Alert 1",executedAlertConfiguration: executedEvdasConfiguration1,configuration:evdasConfiguration, substance: product,event:event)
        evdasAlert1.save(validate:false)
        evdasAlert2 = new EvdasAlert(name: "Evdas Alert 2",executedAlertConfiguration: executedEvdasConfiguration2,configuration:evdasConfiguration, substance: product,event:event)
        evdasAlert2.save(validate:false)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)
        businessConfiguration = new BusinessConfiguration(id: 123L, ruleName: 'testRule', dataSource: 'pva',
                modifiedBy: 'username', createdBy: 'username', productSelection: '{}', isGlobalRule: true , enabled: true)
        businessConfiguration.save(failOnError: true)
        ruleInformation = new RuleInformation(ruleName: "TEST", ruleJSON: "{}")
        //Added user

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review",
                validatedConfirmed: false, abbreviation: "RR")

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)
        user = new User(id: '1', username: 'test_user1', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name1' }
        user.metaClass.getEmail = { 'fake.email1@fake.com' }
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: false)
        user.save(flush: false)
        priority = new Priority(value: "medium", display: true, displayName: "medium", reviewPeriod: 2, priorityOrder: 1)
        priority.save(flush: true, failOnError: true)
        SingleGlobalTag singleGlobalTag1 = new SingleGlobalTag(caseSeriesId: 22L, caseId: 22L, tagId: 22L, owner: "userName",
                lastUpdated: new Date(), tagText: "tagText1")
        AlertTag alertTag1 = new AlertTag(name: "alertTag1", createdBy: user, dateCreated: new Date())
        alertTag1.save(flush: true, failOnError: false)
        globalCase= new GlobalCase()
        globalCase.caseId = 10l
        globalCase.versionNum = 1
        globalCase.save(failOnError: true)
        sca = new SingleCaseAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration1,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: "signaldev",
                modifiedBy: "signaldev",
                dateCreated: new Date(),
                lastUpdated: new Date(),
                productFamily: "Test Product A",
                attributesMap: [:],
                followUpExists: false,
                isNew: false,
                productId: 1,
                followUpNumber: 1,
                tags: singleGlobalTag1,
                alertTags: alertTag1,
                isCaseSeries: false,
                caseId: 1,
                comboFlag: "comboFlag",
                malfunction: "malfunction",
                globalIdentity: globalCase
        )
        sca.save(flush: true, failOnError: false)

    }

    def cleanup() {
    }

    void "test setAutoFlagForAlert currentFlag:New"() {
        when:
        def result = service.setAutoFlagForAlert("New")
        then:
        result == "Auto Flagged,New"
    }

    void "test setAutoFlagForAlert currentFlag:Previously Reviewed"() {
        when:
        def result = service.setAutoFlagForAlert("Previously Reviewed")
        then:
        result == "Auto Flagged & Previously Reviewed"
    }

    void "test setAutoFlagForAlert currentFlag:''"() {
        when:
        def result = service.setAutoFlagForAlert("")
        then:
        result == "Auto Flagged"
    }

    void "test setAutoFlagForAlert currentFlag:null"() {
        when:
        def result = service.setAutoFlagForAlert(null)
        then:
        result == "Auto Flagged"
    }

    @Ignore
    void "test getPreviousPeriodDataOfAggAlert() method"(){
        given:
        service.aggregateCaseAlertService = [fetchLastExecutionOfAlert: { c->
            return executedConfiguration2
        }]

        when:
        Map result = service.getPreviousPeriodDataOfAggAlert(aggregateCaseAlert1)

        then:
        aggregateCaseAlert2.id == result.id

    }

    @Ignore
    void "test getPreviousPeriodDataOfEvdasAlert() method"(){
        given:
        service.evdasAlertService = [fetchLastExecutionOfAlert: { c->
            return executedEvdasConfiguration2
        }]

        when:
        Map result = service.getPreviousPeriodDataOfEvdasAlert(evdasAlert1)

        then:
        evdasAlert2.id == result.id
    }

    void "test for  getStringOperatorsMap() method"() {
        setup:
        def printOperator = { attr1, attr2, op, preText ->
            true
        }
        StringBuilder logString = new StringBuilder()
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        ruleDataDTO.logString = logString
        String dme = "dme"
        when:
        Map result = service.getStringOperatorsMap(ruleDataDTO, printOperator)

        then:
        result['End With'](dme, 'me', 'preText') == true
        result['End With'](dme, 'esd', 'preText') == false
        result['Contains'](dme, 'd', 'preText') == true
        result['Contains'](dme, 'n', 'preText') == false
        result['Does Not Contain'](dme, 'n', 'preText') == true
        result['Does Not Contain'](dme, 'd', 'preText') == false
        result['Does Not End With'](dme, 'esd', 'preText') == true
        result['Does Not End With'](dme, 'me', 'preText') == false
        result['Does Not Start With'](dme, 'me', 'preText') == true
        result['Does Not Start With'](dme, 'dm', 'preText') == false


    }

    void "test for  populateStratificationScore() method with Str and stratification Values"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        aggregateCaseAlert.ebgmAge = 'Adolescent : 3.99 ,Adult : 3.99 ,Child : 3.99 ,Elderly : 3.99 '
        aggregateCaseAlert.ebgmGender = 'Confident : 3.99,CopyConf : 3.99,Female : 0.2361'
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        when:
        ruleDataDTO = service.populateStratificationScore(aggregateCaseAlert, ruleDataDTO)
        then:
        ruleDataDTO.stratificationScoreDTO.mapData() == [EBGM_ADOLESCENT:3.99, EBGM_ADULT:3.99, EBGM_CHILD:3.99, EBGM_ELDERLY:3.99, EBGM_CONFIDENT:3.99, EBGM_COPYCONF:3.99, EBGM_FEMALE:0.2361]
    }

    void "test for  populateStratificationScore() method with null Str and stratification Values"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        when:
        ruleDataDTO = service.populateStratificationScore(aggregateCaseAlert, ruleDataDTO)
        then:
        ruleDataDTO.stratificationScoreDTO.mapData() == [:]
    }

    void "test isOtherRuleEnabled() GLOBAL RULE And ENABLED"(){
        given:
        BusinessConfiguration.metaClass.static.findAllByIsGlobalRule = { boolean ruleTypeCheck ->
            return [businessConfiguration]
        }
        when:
        boolean result = service.isOtherRuleEnabled(true,'pva')
        then:
        result == true
    }

    void "test isOtherRuleEnabled() GLOBAL RULE And DISABLED"(){
        given:
        BusinessConfiguration.metaClass.static.findAllByIsGlobalRule = { boolean ruleTypeCheck ->
            return [new BusinessConfiguration(id: 123L, ruleName: 'testRule', dataSource: 'pva', modifiedBy:
                    'username', createdBy: 'username', productSelection: '{}', isGlobalRule: true)]
        }
        when:
        boolean result = service.isOtherRuleEnabled(false, 'pva')
        then:
        result == false
    }

    @Ignore
    void "test getRuleColumsData() for SUB_GROUP ALGORITHM"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        cacheService.getSubGroupColumns() >>[['Adolescent','Adult','Child','Elderly','Foetus','Infant','Neonate'],['AddedNew','Confident','Female','MALE_NEW','Male','Transege','UNK']]
        QueryService queryService = Mock(QueryService)
        service.queryService = queryService
        queryService.getQueryListForBusinessConfiguration() >> null
        when:
        service.metaClass.getBusinessConfiguration = { Long id -> return businessConfiguration }
        Map abc = service.getSelectBoxValues(123L)
        then:
        abc.categories[0].value == 'PRR'
    }

    @Ignore
    void "test getRuleColumsData() for SUB_GROUP ALGORITHM with EUDRA DB"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        cacheService.getSubGroupColumns() >>[['Adolescent','Adult','Child','Elderly','Foetus','Infant','Neonate'],['AddedNew','Confident','Female','MALE_NEW','Male','Transege','UNK']]
        QueryService queryService = Mock(QueryService)
        service.queryService = queryService
        queryService.getQueryListForBusinessConfiguration() >> null
        when:
        service.metaClass.getBusinessConfiguration = { Long id -> return new BusinessConfiguration(id: 123L, ruleName: 'testRule', dataSource: 'eudra',
                modifiedBy: 'username', createdBy: 'username', productSelection: '{}', isGlobalRule: true , enabled: true) }
        Map abc = service.getSelectBoxValues(123L)
        then:
        abc.toString().contains('value:EVDAS_SDR_PAED')
    }

  void "test for  populateRuleInformation() method without AlertTag"() {
        setup:
        def params = [isFirstTimeRule  : 'true', isBreakAfterRule: 'true',
                      customSQLQuery   : 'Testing SQL Query',
                      JSONQuery        : '{}', ruleName: 'UPDATED NAME',
                      justificationText: 'TESTING', formatInfo: 'InfoTest']
        when:
        ruleInformation = service.populateRuleInformation(params, ruleInformation)
        then:
        ruleInformation.ruleName == "UPDATED NAME"
    }

    void "test for  populateRuleInformation() method with AlertTag"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        cacheService.getUserByUserName(_) >> user
        service.cacheService = cacheService
        def params = [isFirstTimeRule  : 'true', isBreakAfterRule: 'true',
                      customSQLQuery   : 'Testing SQL Query',
                      JSONQuery        : '{}', ruleName: 'UPDATED NAME',
                      justificationText: 'TESTING', formatInfo: 'InfoTest',
                      tags             : ['C1_SP_E', 'CaseSeries']]
        when:
        ruleInformation = service.populateRuleInformation(params, ruleInformation)
        then:
        AlertTag.count() == 1
    }

    @Ignore
    void "test for generateRule() method"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        RuleDataDTO ruleDataDTO = new RuleDataDTO(chiSquare: 4.3848, newCount: 20, ror: 2, newFatalCount: 3, logString: new StringBuilder())
        Map expressionObj = [expressions: [[expressions: [[expressions: [
                [index: 'undefined', category: 'COUNTS', attribute: 'NEW_COUNT', operator: 'Greater Than Equal To', threshold: 3],
                [index: 'undefined', category: 'ALGORITHM', attribute: 'CHI_SQUARE', operator: 'Greater Than Equal To', threshold: 3.84],
                [index: 'undefined', category: 'ALGORITHM', attribute: 'ROR', operator: 'Greater Than Equal To', threshold: 1]],
                                                           keyword    : 'and'
                                                          ]]],
                                           [expressions: [[index: 'undefined', category: 'COUNTS', attribute: 'NEW_FATAL', operator: 'Greater Than Equal To', threshold: 2],
                                                          [index: 'undefined', category: 'COUNTS', attribute: 'NEW_COUNT', operator: 'Greater Than Equal To', threshold: 1]],
                                            keyword    : 'and']], keyword: 'and']
        when:
        Boolean isRuleApplied = service.generateRule(ruleDataDTO, expressionObj ,aggregateCaseAlert1)
        then:
        isRuleApplied
    }

    @Ignore
    void "test for generateRule() method and rule doesn't match"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        RuleDataDTO ruleDataDTO = new RuleDataDTO(chiSquare: 4.3848, newCount: 0, ror: 2, newFatalCount: 3, logString: new StringBuilder())
        Map expressionObj = [expressions: [[expressions: [[expressions: [
                [index: 'undefined', category: 'COUNTS', attribute: 'NEW_COUNT', operator: 'Greater Than Equal To', threshold: 3],
                [index: 'undefined', category: 'ALGORITHM', attribute: 'CHI_SQUARE', operator: 'Greater Than Equal To', threshold: 3.84],
                [index: 'undefined', category: 'ALGORITHM', attribute: 'ROR', operator: 'Greater Than Equal To', threshold: 1]],
                                                           keyword    : 'and'
                                                          ]]],
                                           [expressions: [[index: 'undefined', category: 'COUNTS', attribute: 'NEW_FATAL', operator: 'Greater Than Equal To', threshold: 2],
                                                          [index: 'undefined', category: 'COUNTS', attribute: 'NEW_COUNT', operator: 'Greater Than Equal To', threshold: 1]],
                                            keyword    : 'and']], keyword: 'and']
        when:
        Boolean isRuleApplied = service.generateRule(ruleDataDTO, expressionObj, aggregateCaseAlert1)
        then:
        !isRuleApplied
    }

    @Ignore
    void "test for generateExpression() method"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        RuleDataDTO ruleDataDTO = new RuleDataDTO(chiSquare: 4.3848, newCount: 3, ror: 2, newFatalCount: 3, logString: new StringBuilder())
        Map expressionObj = [index: 'undefined', category: 'COUNTS', attribute: 'NEW_COUNT', operator: 'Greater Than Equal To', threshold: 3]
        when:
        Boolean isRuleApplied = service.generateExpression(ruleDataDTO, expressionObj, "│   ", aggregateCaseAlert1)
        then:
        isRuleApplied
    }

    @Ignore
    void "test for generateExpression() method if rule doesn't match"() {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        RuleDataDTO ruleDataDTO = new RuleDataDTO(chiSquare: 4.3848, newCount: 2, ror: 2, newFatalCount: 3, logString: new StringBuilder())
        Map expressionObj = [index: 'undefined', category: 'COUNTS', attribute: 'NEW_COUNT', operator: 'Greater Than Equal To', threshold: 3]
        when:
        Boolean isRuleApplied = service.generateExpression(ruleDataDTO, expressionObj,"│   ", aggregateCaseAlert1)
        then:
        !isRuleApplied
    }

    void "test processOutcomeOnEvdasAlerts" () {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        EvdasAlert evdasAlert = new EvdasAlert([alertConfiguration : evdasConfiguration, executedAlertConfiguration : executedEvdasConfiguration1])
        List<Map> outComeMapList = [[format: 'Test-format-New-Count', action: '', disposition: '', justificationText: 'For Test', signal: '', medicalConcepts: '', tags: '', subTags: ''],
                                    [format: 'Test-format-Ser-Count', action: '', disposition: '', justificationText: 'For Fun', signal: '', medicalConcepts: '', tags: '', subTags: '']]
        when:
        service.processOutcomeOnEvdasAlerts(evdasAlert, outComeMapList)
        then:
        evdasAlert.format ==  ['Test-format-New-Count', 'Test-format-Ser-Count'].toString()
    }

    void "test processOutcomeOnAggregateAlerts" () {
        setup:
        CacheService cacheService = Mock(CacheService)
        service.cacheService = cacheService
        AggregateCaseAlert alert = new AggregateCaseAlert([alertConfiguration : configuration, executedAlertConfiguration : executedConfiguration1])
        alert.disposition = disposition
        List<Map> outComeMapList = [[format: 'Test-format-New-Count', action: '', disposition: '', justificationText: 'For Test', signal: '', medicalConcepts: '', tags: '', subTags: ''],
                                    [format: 'Test-format-Ser-Count', action: '', disposition: '', justificationText: 'For Fun', signal: '', medicalConcepts: '', tags: '', subTags: '']]
        when:
        service.processOutcomeOnAggregateAlerts(alert, outComeMapList)
        then:
        alert.format ==  ['Test-format-New-Count', 'Test-format-Ser-Count'].toString()
    }
    void "test saveAlertCaseHistory"(){
        when:
        service.saveAlertCaseHistory(sca, "just", "signaldev")
        then:
        sca.justification == "just"
        sca.dispPerformedBy == "signaldev"
    }

}