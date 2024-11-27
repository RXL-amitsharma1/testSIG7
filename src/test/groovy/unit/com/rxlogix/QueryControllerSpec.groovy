package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Ignore
import spock.lang.Specification

import javax.sql.DataSource

@Ignore
class QueryControllerSpec extends Specification implements ControllerUnitTest<QueryController> {

    QueryService queryService = new QueryService()
    CRUDService crudService = new CRUDService()
    //Use this to get past the constraint that requires a JSONQuery string.
    public static final user = "unitTest"
    def JSONQuery = """{ "all": { "containerGroups": [   
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""

    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    //Use this to get past the constraint that requires a JSONQuery string.

    static doWithSpring = {
        dataSource_pva(InstanceFactoryBean, [:] as DataSource, DataSource)
    }

    def setup() {
        controller.dataSource_pva = [:] as DataSource
    }

    def cleanup() {}

    def setupSpec() {
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
    }

    def cleanupSpec() {
        TimeZone.setDefault(ORIGINAL_TZ)
        User.metaClass.encodePassword = null
    }

    // couldn't put these in the setup/cleanup because of issues w/ @Shared. May be missing something to make that work. This is workaround.
    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user, isEmailEnabled: true)
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user, isEmailEnabled: true)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private makeSecurityService(User user) {
        def securityMock = [getUser: { user }] as UserService
        securityMock
    }

    void "test queryList"() {
        given:
        def pvaQueryList = [[id:1,text:'TEST PVA']]
        def faersQueryList = [[id:2,text:'TEST FAERS']]
        def queryServiceMock = [getPvaQueryList: { pvaQueryList },getFAERSQueryList : { faersQueryList }] as QueryService
        controller.queryService = queryServiceMock
        when:
        controller.queryList()
        then:
        response.status == 200
        response.getJson()["pva"] == [[id: 1,text: 'TEST PVA']]
        response.getJson()["faers"] == [[id: 2,text: 'TEST FAERS']]
    }

    void "test queryList when there is no query in PVA"() {
        given:
        def pvaQueryList = []
        def faersQueryList = [[id:2,text:'TEST FAERS']]
        def queryServiceMock = [getPvaQueryList: { pvaQueryList },getFAERSQueryList : { faersQueryList }] as QueryService
        controller.queryService = queryServiceMock
        when:
        controller.queryList()
        then:
        response.status == 200
        response.getJson()["pva"] == []
        response.getJson()["faers"] == [[id: 2,text: 'TEST FAERS']]
    }

    void "test queryList when there is no query in faers"() {
        given:
        def pvaQueryList = [[id:1,text:'TEST PVA']]
        def faersQueryList = []
        def queryServiceMock = [getPvaQueryList: { pvaQueryList },getFAERSQueryList : { faersQueryList }] as QueryService
        controller.queryService = queryServiceMock
        when:
        controller.queryList()
        then:
        response.status == 200
        response.getJson()["pva"] == [[id: 1,text: 'TEST PVA']]
        response.getJson()["faers"] == []
    }

    void "test queryList when there is no query either in faers or in pva"() {
        given:
        def pvaQueryList = []
        def faersQueryList = []
        def queryServiceMock = [getPvaQueryList: { pvaQueryList },getFAERSQueryList : { faersQueryList }] as QueryService
        controller.queryService = queryServiceMock
        when:
        controller.queryList()
        then:
        response.status == 200
        response.getJson()["pva"] == []
        response.getJson()["faers"] == []
    }


}
