package com.rxlogix.api
import com.rxlogix.config.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class ReportResultRestControllerSpec extends Specification implements ControllerUnitTest<ReportResultRestController> {

    def setup() {
        def normalUser = makeNormalUser()
        def resultData = new ReportResultData(value: "unit test".bytes, versionSQL: "", querySQL: "", reportSQL: "")

        controller.springSecurityService = makeSecurityService(normalUser)
        def templateQuery = new TemplateQuery()
        def config1 = new Configuration(name: "test config" ,owner:normalUser, createdBy: normalUser.username, modifiedBy: normalUser.username).addToTemplateQueries(templateQuery)

        def ec1 = new ExecutedConfiguration (config1.properties)

        List<ReportField> selectedFields = new ArrayList<ReportField>()
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: new ReportTemplate(name:"test", createdBy:normalUser, clumnList: new ReportFieldInfoList()),
                executedConfiguration: ec1, createdBy: normalUser.username, modifiedBy: normalUser.username)
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery = new ExecutedDateRangeInformation()
        def newResult = new ReportResult( report: config1, data: resultData, executionStatus: ReportExecutionStatus.COMPLETED, status: ReportResultStatus.NEW, scheduledBy: normalUser, templateQuery:templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)


        def archivedResult = new ReportResult( report: config1, data: resultData, executionStatus: ReportExecutionStatus.COMPLETED, status: ReportResultStatus.REVIEWED, scheduledBy: normalUser, templateQuery:templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)

        def analysisResult = new ReportResult( report: config1, data: resultData, executionStatus: ReportExecutionStatus.COMPLETED, status: ReportResultStatus.NON_REVIEWED, scheduledBy: normalUser, templateQuery:templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
        request.addHeader("Accept", "application/json")
    }

    def cleanup() {
    }

    def cleanupSpec() {
        User.metaClass.encodePassword = null
    }

    private User makeNormalUser() {
        def user = "unitTest"
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"))
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    private makeSecurityService(User user) {
        def securityMock = mockFor(SpringSecurityService)
        securityMock.demand.getCurrentUser { -> user }
        return securityMock.createMock()
    }
//we cannot test criteria query in unit test so we just test method call and response.
    @Ignore
    void "PVR-151: By default do not show Archived Reports in My Inbox"() {
        given: "Report results with json format"
//        controller.metaClass.getCompletedReport = { [ec1] }
        def c = mockCriteriaForAll()
       // def d = mockGetReportCompleted()

        ReportResultRestController restController = Mock()
//        def restController = Mock(ReportResultRestController)

        ReportResultRestController.metaClass.static.getCompletedReport = { List<ExecutedConfiguration> executedConfigurations -> [ExecutedConfiguration.get(1)] }
        ReportResultRestController.metaClass.static.getExecutedConfigMaps = { List<ExecutedConfiguration> executedConfigurations -> ["please": 'work'] }

        when: "Call index method to show My Inbox"
        controller.index()

        then: "Report status should not be archived"
        response.status == 200
//        response?.json?.status?.name?.each {
//            it != [ReportResultStatus.REVIEWED.name()]
//        }
    }


    @Ignore
    void "PVR-152: Show Archived Reports in My Inbox"() {
        given: "Report results with json format"
        def c = mockCriteriaForAll()
        def r = mockGetReportCompleted()

        when: "Call archived method to show My Inbox"
        controller.archived()

        then: "Report status should be archived"
        response.status == 200
    }
    //TODO: After the new implementation  of shared with this relation ship will change and test will be more specific
    @Ignore
    void "PVR-152: Show All Reports in My Inbox"() {
        given: "Report results with json format"
        def c = mockCriteriaForAll()
        when: "Call UnderReview method to show My Inbox"
        controller.UnderReview()

        then: "Should return all reports"
        response.status == 200
    ///    response.json.size() == ReportResult.list().size()
    }

    private mockCriteriaForAll() {
        def myCriteria = [
                list : {Closure  cls -> [ExecutedConfiguration.findAll()]}
        ]
        ExecutedConfiguration.metaClass.static.createCriteria = { myCriteria }

        def c = ExecutedConfiguration.createCriteria()
        return c
    }

    def mockGetReportCompleted() {
        def statusMock = mockFor(ExecutedConfiguration)
        statusMock.demand.getCompletedReport { [] }
        return statusMock.createMock()
    }

}
