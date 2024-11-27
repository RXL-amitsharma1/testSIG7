package com.rxlogix.config.metadata

import com.rxlogix.Constants
import com.rxlogix.PvsAlertTagService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.CommentTemplate
import com.rxlogix.config.CommentTemplateController
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedAlertDateRangeInformation
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedDateRangeInformation
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.GlobalProductEvent
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.*

class CommentTemplateControllerSpec extends Specification implements ControllerUnitTest<CommentTemplateController> {
    @Shared
    Disposition disposition
    User user
    Priority priority
    ValidatedSignal validatedSignal, validatedSignal_a
    AlertComment alertComment, alertCommentEvdas
    Group wfGroup
    User user1
    ExecutedConfiguration executedConfiguration
    Configuration alertConfiguration
    AggregateCaseAlert aggregateCaseAlert_a

    def setup() {
        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)
        user1 = new User(id: 2, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user1.addToGroups(wfGroup)
        user1.preference.createdBy = "createdBy"
        user1.preference.modifiedBy = "modifiedBy"
        user1.preference.locale = new Locale("en")
        user1.preference.isEmailEnabled = false
        user1.metaClass.getFullName = { 'Fake Name' }
        user1.metaClass.getEmail = { 'fake.email@fake.com' }
        user1.save(validate: false,flush: true)

        Priority priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(validate: false,flush: true)
        alertConfiguration = new Configuration(
                id:4,
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user1.username, modifiedBy: user1.username,
                assignedTo: user1,
                priority: priority,
                owner: user1
        )
        executedConfiguration = new ExecutedConfiguration(name: "test", isLatest: true, adhocRun: false,
                owner: user1, scheduleDateJSON: "{}", nextRunDate: new Date(), type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, totalExecutionTime: 10,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "{\"1\":{\"name\":\"testVal1\",\"2\":\"testVal2\"}}", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user1.username, modifiedBy: user1.username,
                assignedTo: user1, configId: 4,
                pvrCaseSeriesId: 1,
                pvrCumulativeCaseSeriesId: 1,
                selectedDatasource: "pva",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 1,dataMiningVariable: "Gender")
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        executedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        executedAlertDateRangeInformation.dateRangeStartAbsolute = new Date()
        executedAlertDateRangeInformation.dateRangeEndAbsolute = new Date()
        executedAlertDateRangeInformation.executedAlertConfiguration = executedConfiguration
        executedConfiguration.executedAlertDateRangeInformation = executedAlertDateRangeInformation
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date(),
                executedAsOfVersionDate : new Date()
        )
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: 25l, createdBy: user1.username, modifiedBy: user1.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery, headerProductSelection: false,
                headerDateRange: false,
                blindProtected: false,
                privacyProtected: false,
                queryLevel: QueryLevelEnum.CASE
        )
        executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
        executedConfiguration.save(validate: false,flush: true)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true)
        disposition.save(validate: false)
        priority =
                new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(validate: false)
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
//        user.grailsApplication = grailsApplication
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false

        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(validate: false,flush: true)


        mockDomain(Priority, [
                [value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1]
        ])

        //Save the priority
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(validate: false,flush: true)

        //Save validated signal objects
        validatedSignal = new ValidatedSignal(id: 1, name: "test_name", products: "test_products", endDate: new Date(), assignedTo: user, assignmentType: 'USER',
                modifiedBy: user.username, priority: priority, disposition: disposition, createdBy: user.username, startDate: new Date(), genericComment: "Test notes")
        validatedSignal_a = new ValidatedSignal(id: 2, name: "test_signal_a", products: "test_products", endDate: new Date(), assignedTo: user, assignmentType: 'USER',
                modifiedBy: user.username, priority: priority, disposition: disposition, createdBy: user.username, startDate: new Date(), genericComment: "Test notes")
        validatedSignal_a.workflowGroup = wfGroup
        validatedSignal_a.save(validate: false)
        validatedSignal.workflowGroup = wfGroup
        alertCommentEvdas = new AlertComment(productName: "Test Product A", productFamily: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                comments: "Test Evdas", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        alertCommentEvdas.save(validate: false)
        validatedSignal.addToComments(alertCommentEvdas)
        validatedSignal.save(validate: false)
        aggregateCaseAlert_a = new AggregateCaseAlert(
                id: 2,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: "test name of alert",
                priority: priority,
                alertTags: null,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                soc: "BODY_SYS1",
                pt: 'Rash',
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                cumSponCount: 1,
                newSeriousCount: 1,
                cumSeriousCount: 1,
                newFatalCount: 1,
                cumFatalCount: 1,
                prrValue: 1,
                prrLCI: 1,
                prrUCI: 1,
                prrStr: "1",
                prrStrLCI: "1",
                prrStrUCI: "1",
                prrMh: "1",
                rorValue: 1,
                rorLCI: 1,
                rorUCI: 1,
                rorStr: "1",
                rorStrLCI: "1",
                rorStrUCI: "1",
                rorMh: "1",
                pecImpHigh: "1",
                pecImpLow: "1",
                ebgmAge: 'Adult:01.278, Geriatric:0.5, Paediatric:10',
                rorSubGroup: '{"AGE_GROUP":{"Paediatric":5,"Adult":0,"Geriatric":4},"REGION":{"AMERICA":8,"EUROPE":8,"JAPAN":6,"ASIA":4,"OTHERS":5}}',
                rorLciSubGroup: '{"AGE_GROUP":{"Paediatric":8,"Adult":0,"Geriatric":9},"REGION":{"AMERICA":5,"EUROPE":5,"JAPAN":3,"ASIA":3,"OTHERS":1}}',
                rorUciSubGroup: '{"AGE_GROUP":{"Paediatric":2,"Adult":4,"Geriatric":6},"REGION":{"AMERICA":3,"EUROPE":2,"JAPAN":5,"ASIA":6,"OTHERS":7}}',
                rorUciRelSubGroup: '{"AGE_GROUP":{"Paediatric":5,"Adult":0,"Geriatric":4},"REGION":{"AMERICA":8,"EUROPE":8,"JAPAN":6,"ASIA":4,"OTHERS":5}}',
                chiSquareSubGroup: '{"AGE_GROUP":{"Paediatric":5,"Adult":4,"Geriatric":1},"REGION":{"AMERICA":8,"EUROPE":6,"JAPAN":9,"ASIA":7,"OTHERS":6}}',
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: executedConfiguration.dateCreated,
                lastUpdated: executedConfiguration.dateCreated,
                eb05: new Double(1),
                eb95: new Double(1),
                ebgm: new Double(2),
                dueDate: new Date(),
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test",
                frequency: "Yearly",
                productId: 100083,
                ptCode: 10029404,
                newSponCount: 2,
                smqCode: 1213,
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3")
        aggregateCaseAlert_a.metaClass.toDto = { [productName: "Test Product AJ",
                                                  soc: "BODY_SYS1", preferredTerm: 'Product packaging issue',
                                                  ebgmAge: 'Adult:01.278, Geriatric:0.5, Paediatric:10',
                                                  rorSubGroup: '{"AGE_GROUP":{"Paediatric":5,"Adult":0,"Geriatric":4},"REGION":{"AMERICA":8,"EUROPE":8,"JAPAN":6,"ASIA":4,"OTHERS":5}}',
                                                  rorLciSubGroup: '{"AGE_GROUP":{"Paediatric":8,"Adult":0,"Geriatric":9},"REGION":{"AMERICA":5,"EUROPE":5,"JAPAN":3,"ASIA":3,"OTHERS":1}}',
                                                  rorUciSubGroup: '{"AGE_GROUP":{"Paediatric":2,"Adult":4,"Geriatric":6},"REGION":{"AMERICA":3,"EUROPE":2,"JAPAN":5,"ASIA":6,"OTHERS":7}}',
                                                  rorUciRelSubGroup: '{"AGE_GROUP":{"Paediatric":5,"Adult":0,"Geriatric":4},"REGION":{"AMERICA":8,"EUROPE":8,"JAPAN":6,"ASIA":4,"OTHERS":5}}',
                                                  chiSquareSubGroup: '{"AGE_GROUP":{"Paediatric":5,"Adult":4,"Geriatric":1},"REGION":{"AMERICA":8,"EUROPE":6,"JAPAN":9,"ASIA":7,"OTHERS":6}}',] }
        aggregateCaseAlert_a.save(validate: false,flush: true)

        CommentTemplate commentTemplate1 = new CommentTemplate(
                id: 1,
                name: "Name comment Template",
                comments: "The Product <Product Name> contains <Cum count> Cum cases and <Cum fatal count> Cum fatal cases, with DSS Score=<DSS Score>.",
                createdBy: user1.fullName,
                modifiedBy: user1.fullName
        )
        commentTemplate1.save(validate: false,flush: true)
    }

    def populateValidParams(params) {
        params["id"] = 4
        params["name"] = "test comment template"
        params["comments"] = "test comment <test>"
        params["createdBy"] = "user1"
        params["modifiedBy"] = "user1"
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.commentTemplateList
            model.commentTemplateCount == null
    }


    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == null
            flash.message == null

        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def commentTemplate = new CommentTemplate()
            commentTemplate.validate()
            controller.update(commentTemplate.id as Long)

        then:"The edit view is rendered again with the invalid instance"
            view == null
            model.commentTemplate == null

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            commentTemplate = new CommentTemplate(params as Map).save(flush: true)
            controller.update(commentTemplate.id as Long)

        then:"A redirect is issued to the show action"
            commentTemplate != null
            response.redirectedUrl == null
            flash.message == null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'DELETE'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == null
            flash == [:]

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            CommentTemplate commentTemplate = new CommentTemplate(params as Map).save(flush: true)

        then:"It exists"
            CommentTemplate.count() == 2

        when:"The domain instance is passed to the delete action"
            controller.delete(commentTemplate.id as Long)

        then:"The instance is deleted"
            CommentTemplate.count() == 2
            response.redirectedUrl == null
            flash == [:]
    }

    void "Test getCommentFromCommentTemplate"() {
        given:
        params.templateId = 1
        params.acaId = 2
        CommentTemplate commentTemplate = new CommentTemplate(name: "subgorup template", comments: "The Product <Product Name> and Event <PT>/<SMQ/Event Group> , with ROR(Paediatric) = <ROR(Paediatric)> , ROR(AMERICA) = <ROR(AMERICA)> , ROR UCI(Paediatric) = <ROR UCI(Paediatric)> , ROR LCI(AMERICA) = <ROR LCI(AMERICA)> , Chi-Square(EUROPE) = <Chi-Square(EUROPE)> and ROR UCI-R(AMERICA) = <ROR UCI-R(AMERICA)> , EBGM(Adult) = <EBGM(Adult)>  , EB05(Geriatric) = <EB05(Geriatric)> , Categories = <Categories> .", createdBy: user1.fullName, modifiedBy: user1.fullName)
        commentTemplate.save(failOnError: true)
        AggregateCaseAlert.metaClass.static.findById = { Long acaId ->
            return aggregateCaseAlert_a
        }
        CommentTemplate.metaClass.static.findById = { Long acaId ->
            return commentTemplate
        }
        grailsApplication.config.comment.template.map = [
                'Product Name':'productName',

        ]
        List<Map> categoryMap  = [['tagText':'Action Taken With Drug', 'subTagText':'Dose not changed;Dose Reduced', 'privateUser':'', 'priority':3, 'tagType':'(A)', 'alertId':1267315, 'privateUserName':null]]
        List<Map> globalTagNameList = [['tagText':'test', 'subTagText':null, 'privateUser':'', 'priority':9999, 'tagType':'', 'globalId':499465, 'privateUserName':null], ['tagText':'Adult', 'subTagText':null, 'privateUser':'', 'priority':1, 'tagType':'', 'globalId':499465, 'privateUserName':null], ['tagText':'PRR', 'subTagText':null, 'privateUser':'', 'priority':9999, 'tagType':'', 'globalId':499465, 'privateUserName':null], ['tagText':'Adult', 'subTagText':null, 'privateUser':'', 'priority':9999, 'tagType':'', 'globalId':499465, 'privateUserName':null]]
        Map ebgmSubGroupMap = ['Paediatric':'Age', 'Adult':'Age', 'Geriatric':'Age']
        Map getAllOtherSubGroupColumns = ['ROR(Paediatric)':'rorSubGroup','ROR(AMERICA)':'rorSubGroup','ROR LCI(Paediatric)':'rorLciSubGroup','ROR LCI(AMERICA)':'rorLciSubGroup', 'ROR UCI(Paediatric)':'rorUciSubGroup', 'ROR UCI(Adult)':'rorUciSubGroup', 'ROR UCI(Geriatric)':'rorUciSubGroup', 'ROR UCI(AMERICA)':'rorUciSubGroup','Chi-Square(Paediatric)':'chiSquareSubGroup', 'Chi-Square(Adult)':'chiSquareSubGroup', 'Chi-Square(EUROPE)':'chiSquareSubGroup']
        Map relSubGroupColumns = ['ROR-R(Paediatric)':'rorRelSubGroup','ROR-R(EUROPE)':'rorRelSubGroup','ROR LCI-R(Paediatric)':'rorLciRelSubGroup','ROR LCI-R(AMERICA)':'rorLciRelSubGroup','ROR UCI-R(Paediatric)':'rorUciRelSubGroup', 'ROR UCI-R(Adult)':'rorUciRelSubGroup','ROR UCI-R(AMERICA)':'rorUciRelSubGroup']
        def cacheService = Mock(CacheService)
        cacheService.getSubGroupKeyColumns() >> ebgmSubGroupMap
        cacheService.getAllOtherSubGroupMapCombined(_) >> getAllOtherSubGroupColumns
        cacheService.getRelSubGroupMapCombined(_) >> relSubGroupColumns

        def pvsAlertTagService = Mock(PvsAlertTagService)
        pvsAlertTagService.getAllAlertSpecificTags(_, _) >> categoryMap

        def pvsGlobalTagService = Mock(PvsGlobalTagService)
        pvsGlobalTagService.getAllGlobalTags(_, _, _) >> globalTagNameList
        controller.cacheService = cacheService
        controller.pvsAlertTagService = pvsAlertTagService
        controller.pvsGlobalTagService = pvsGlobalTagService
        when:
        controller.createCommentFromTemplate()
        then:
        response.json == [code:null, data:"The Product Test Product AJ and Event Product packaging issue , with ROR(Paediatric) = 5 , ROR(AMERICA) = 8 , ROR UCI(Paediatric) = 2 , ROR LCI(AMERICA) = 5 , Chi-Square(EUROPE) = 6 and ROR UCI-R(AMERICA) = 8 , EBGM(Adult) = 01.278  , EB05(Geriatric) = - , Categories = Action Taken With Drug Category: Dose not changed(s), Dose Reduced(s), test Category, Adult Category, PRR Category, Adult Category  .", message:null, value:null, status:true]
    }
}
