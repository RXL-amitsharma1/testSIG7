package com.rxlogix


import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.ProductAssignmentLog
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import groovy.transform.SourceURI
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class ProductAssignmentControllerSpec extends Specification implements ControllerUnitTest<ProductAssignmentController> {

    User user
    User newUser
    Group wfGroup
    Group userGroup
    Group newUserGroup
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition

    File fileToRead

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(flush: true, failOnError: true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default",
                groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)


        userGroup = new Group(name: "user_group",
                groupType: GroupType.USER_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        userGroup.save(flush: true)

        newUserGroup = new Group(name: "new_user_group",
                groupType: GroupType.USER_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        newUserGroup.save(flush: true)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

        //Save the  user
        newUser = new User(id: '1', username: 'new_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        newUser.addToGroups(wfGroup)
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.timeZone = "UTC"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.metaClass.getFullName = { ' New Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.save(flush: true)

        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("ProductAssignmentControllerSpec.groovy", "import_assignment_file/upload/import-assignment.xlsx")
        fileToRead = new File(directory)

        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> { user }
        controller.userService = mockUserService
    }

    def "test index"() {
        given:
        params.isProductView = "false"

        when:
        controller.index()

        then:
        response.status == 200
        view == '/productAssignment/index'
        model.columnOrder == '[{"name":"assignedUserOrGroup","label":"Assignment","seq":3,"listOrder":0,"containerView":1},{"name":"hierarchy","label":"Product Hierarchy","seq":2,"listOrder":1,"containerView":1},{"name":"product","label":"Product","seq":1,"listOrder":2,"containerView":1},{"name":"workflowGroup","label":"Workflow Group","seq":5,"listOrder":3,"containerView":1},{"name":"createdBy","label":"User Id","seq":4,"listOrder":4,"containerView":3}]'
        model.isProductView == false
        model.isEdit == true
    }

    def "test getColumnOrder on product view"() {
        when:
        params.isProductView = "true"
        List result = controller.getColumnOrder(params.isProductView as Boolean, true)

        then:
        result == [
                [name: "product", label: "Product", seq: 1, listOrder: 0, containerView: 1],
                [name: "hierarchy", label: "Product Hierarchy", seq: 2, listOrder: 1, containerView: 1],
                [name: "assignedUserOrGroup", label: "Assignment", seq: 3, listOrder: 2, containerView: 1],
                [name: "workflowGroup", label: "Workflow Group", seq: 5, listOrder: 3, containerView: 1],
                [name: "createdBy", label: "User Id", seq: 4, listOrder: 0, containerView: 3]
        ]
    }

    def "test getColumnOrder on user view"() {
        when:
        params.isProductView = "false"
        List result = controller.getColumnOrder(params.isProductView as Boolean, true)

        then:
        result == [
                [name: "product", label: "Product", seq: 1, listOrder: 0, containerView: 1],
                [name: "hierarchy", label: "Product Hierarchy", seq: 2, listOrder: 1, containerView: 1],
                [name: "assignedUserOrGroup", label: "Assignment", seq: 3, listOrder: 2, containerView: 1],
                [name: "workflowGroup", label: "Workflow Group", seq: 5, listOrder: 3, containerView: 1],
                [name: "createdBy", label: "User Id", seq: 4, listOrder: 0, containerView: 3]
        ]
    }

    def "test upload in case of incorrect format"() {
        given:
        def file = new GrailsMockMultipartFile('file', 'import-assignment', 'UTF-8', 'someData'.bytes)
        request.addFile(file)

        when:
        controller.upload()

        then:
        JSON.parse(response.text).code == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).message == 'app.label.import.assignment.upload.file.format.incorrect'
    }

    def "test setColumnOrder when product view sets to true"() {
        given:
        params.isProductView = "true"
        params.columnList = '[{"name":"assignedUserOrGroup","label":"Assignment","seq":3,"listOrder":0,"containerView":1},{"name":"hierarchy","label":"Product Hierarchy","seq":2,"listOrder":1,"containerView":1},{"name":"product","label":"Product","seq":1,"listOrder":2,"containerView":1},{"name":"workflowGroup","label":"Workflow Group","seq":5,"listOrder":3,"containerView":1},{"name":"createdBy","label":"User Id","seq":4,"listOrder":4,"containerView":3}]'

        when:
        controller.setColumnOrder()

        then:
        user.colOrder == '[{"name":"assignedUserOrGroup","label":"Assignment","seq":3,"listOrder":0,"containerView":1},{"name":"hierarchy","label":"Product Hierarchy","seq":2,"listOrder":1,"containerView":1},{"name":"product","label":"Product","seq":1,"listOrder":2,"containerView":1},{"name":"workflowGroup","label":"Workflow Group","seq":5,"listOrder":3,"containerView":1},{"name":"createdBy","label":"User Id","seq":4,"listOrder":4,"containerView":3}]'
        response.status == 200
        JSON.parse(response.text).status == 'success'
    }

    def "test setColumnOrder when product view sets to false"() {
        given:
        params.isProductView = "false"
        params.columnList = '[{"name":"product","label":"Product","seq":1,"listOrder":0,"containerView":1},{"name":"hierarchy","label":"Product Hierarchy","seq":2,"listOrder":1,"containerView":1},{"name":"assignedUserOrGroup","label":"Assignment","seq":3,"listOrder":2,"containerView":1},{"name":"workflowGroup","label":"Workflow Group","seq":5,"listOrder":3,"containerView":1},{"name":"createdBy","label":"User Id","seq":4,"listOrder":0,"containerView":3}]'

        when:
        controller.setColumnOrder()

        then:
        user.colUserOrder == '[{"name":"product","label":"Product","seq":1,"listOrder":0,"containerView":1},{"name":"hierarchy","label":"Product Hierarchy","seq":2,"listOrder":1,"containerView":1},{"name":"assignedUserOrGroup","label":"Assignment","seq":3,"listOrder":2,"containerView":1},{"name":"workflowGroup","label":"Workflow Group","seq":5,"listOrder":3,"containerView":1},{"name":"createdBy","label":"User Id","seq":4,"listOrder":0,"containerView":3}]'
        response.status == 200
        JSON.parse(response.text).status == 'success'
    }

    def "test columnOrder"() {
        given:
        params.isProductView = "true"

        when:
        controller.columnOrder()

        then:
        response.status == 200
        JSON.parse(response.text) == [
                [name: "product", label: "Product", seq: 1, listOrder: 0, containerView: 1],
                [name: "hierarchy", label: "Product Hierarchy", seq: 2, listOrder: 1, containerView: 1],
                [name: "assignedUserOrGroup", label: "Assignment", seq: 3, listOrder: 2, containerView: 1],
                [name: "workflowGroup", label: "Workflow Group", seq: 5, listOrder: 3, containerView: 1],
                [name: "createdBy", label: "User Id", seq: 4, listOrder: 0, containerView: 3]
        ]
    }

    def "test prepareDataForCRUDOperation in case of products"() {
        when:
        Map result = controller.prepareDataForCRUDOperation('["User_1114"]', '{"1":[{"name":"APREMILAST","id":"10314401"}],"2":[],"3":[],"4":[]}', '')

        then:
        result.assignmentList == ["User_1114"]
        result.products != null
    }

    def "test prepareDataForCRUDOperation in case of product groups"() {
        when:
        def result = controller.prepareDataForCRUDOperation('["User_1114"]', '', '[{name: "Safety Db-Faers Group (11)", id:11}]')

        then:
        result.assignmentList == ["User_1114"]
        result.products != null
    }
}
