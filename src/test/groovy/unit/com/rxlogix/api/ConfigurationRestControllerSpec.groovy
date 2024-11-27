package com.rxlogix.api

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ConfigurationRestControllerSpec extends Specification implements ControllerUnitTest<ConfigurationRestController> {


   /* public static final user = "unitTest"

    def setup() {

        def normalUser = makeNormalUser()
        def adminUser = makeAdminUser()
        controller.springSecurityService = makeSecurityService(adminUser)
        def query = new Query()
        def template = new CaseLineListingTemplate()

        def templateQuery =  new TemplateQuery(template: template, query: new Query(),
                dateRangeInformationForTemplateQuery:new DateRangeInformation(), createdBy: normalUser.username, modifiedBy: normalUser.username)
        def config1 = new Configuration(nextRunDate: new Date(), name: "configuration 1", owner: normalUser, createdBy: normalUser.username, modifiedBy: normalUser.username)
        config1.addToTemplateQueries(templateQuery)
        config1.save(failOnError: true, flush: true)

        def ec1 = new ExecutedConfiguration (config1.properties)
        def executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: ec1, createdBy: normalUser.username, modifiedBy: normalUser.username)
        def inProgressResult1 = new ReportResult(executionStatus: ReportExecutionStatus.GENERATING, statusUser: new SharedWith(), scheduledBy: normalUser,templateQuery: templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)

        def ec2 = new ExecutedConfiguration (config1.properties)
        def executedTemplateQuery2= new ExecutedTemplateQuery(executedConfiguration: ec2, createdBy: normalUser.username, modifiedBy: normalUser.username)
        def generatedResult1 = new ReportResult(templateQuery: templateQuery,  report: config1, executionStatus: ReportExecutionStatus.DELIVERING, statusUser: new SharedWith(), scheduledBy: normalUser,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)

        def ec3 = new ExecutedConfiguration (config1.properties)
        def deliveredResult1 = new ReportResult(executedConfiguration: ec3, report: config1, executionStatus: ReportExecutionStatus.COMPLETED, statusUser: new SharedWith(), scheduledBy: normalUser,templateQuery: templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)

        def config2 = new Configuration(nextRunDate: null, name: "configuration 2", owner: normalUser,createdBy: normalUser.username, modifiedBy: normalUser.username)
        def templateQuery1 = new TemplateQuery(template: template, query: query, dateRangeInformationForTemplateQuery:new DateRangeInformation(), createdBy: normalUser.username, modifiedBy: normalUser.username)
        config2.addToTemplateQueries(templateQuery1).save(failOnError: true, flush: true)
        def ec4 = new ExecutedConfiguration (config2.properties)
        def inProgressResult2 = new ReportResult(executedConfiguration: ec4, report: config2, executionStatus: ReportExecutionStatus.GENERATING, statusUser: new SharedWith(), scheduledBy: normalUser,templateQuery: templateQuery1,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)

        def ec5 = new ExecutedConfiguration (config2.properties)
        def generatedResult2 = new ReportResult(executedConfiguration: ec5, report: config2, executionStatus: ReportExecutionStatus.DELIVERING, statusUser: new SharedWith(), scheduledBy: normalUser, templateQuery: templateQuery1,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)

        def ec6 = new ExecutedConfiguration (config2.properties)
        def deliveredResult2 = new ReportResult(executedConfiguration: ec6, report: config2, executionStatus: ReportExecutionStatus.COMPLETED, statusUser: new SharedWith(), scheduledBy: normalUser, templateQuery: templateQuery1,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)

        request.addHeader("Accept", "application/json")
    }

    def cleanup() {
    }

    def cleanupSpec() {
        User.metaClass.encodePassword = null
    }

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"))
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
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

    private makeSecurityService(User user) {
        def securityMock = mockFor(SpringSecurityService)
        securityMock.demand.getCurrentUser { -> user }
        return securityMock.createMock()
    }*/
//TODO: execution status is not fixed yet
//    void "PVR-226: By default do not show Delivered Reports"() {
//        given: "Two configurations"
//
//        when: "Call executionStatus method"
//        controller.executionStatus()
//
//        then: "Report execution status should not be delivered"
//        response.status == 200
//        if (response.json.executionStatus) {
//            response.json.executionStatus.each {
//                it != [ReportExecutionStatus.COMPLETED.value()]
//            }
//        } else {
//            response.json.nextRunDate != null
//        }
//    }

//    void "PVR-226: Show Delivered Reports"() {
//        given: "Two configurations"
//
//        when: "Call delivered method"
//        controller.delivered()
//
//        then: "Report execution status should be delivered"
//        response.status == 200
//        response.json.executionStaus.each {
//            it == [ReportExecutionStatus.COMPLETED.value()]
//        }
//    }

//    void "PVR-226: Show All execution status"() {
//        given: "Two configurations"
//        def configs = Configuration.where { nextRunDate != null }.list()
//
//        when: "Call listAllResults method"
//        controller.listAllResults()
//
//        then: "Should show all results and the configs with next run date"
//        response.status == 200
//        response.json.size() == ReportResult.list().size() + configs.size()
//    }
}
