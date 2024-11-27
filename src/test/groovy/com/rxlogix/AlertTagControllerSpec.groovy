package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.Tag
import com.rxlogix.signal.AlertTag
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
//@Mock([User, Tag, AlertTag, Configuration])
//@TestFor(AlertTagController)
//@TestMixin(DomainClassUnitTestMixin)
@Ignore
class AlertTagControllerSpec extends Specification implements ControllerUnitTest<AlertTagController> {
    User user
    Tag test_tag_a
    Tag test_tag_b
    AlertTag alert_tag_b
    Configuration test_config
    int a

    def setup() {

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.grailsApplication = grailsApplication
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        //prepare mock tag
        test_tag_a = new Tag(id:'11', name:'test_tag_a')
        test_tag_a.save(flush:true)
        test_tag_b = new Tag(id:'12', name:'test_tag_b')
        test_tag_b.save(flush:true)

        //prepare alert tag
        alert_tag_b = new AlertTag(name: 'alert_tag_b', createdBy: user, dateCreated: new Date())
        alert_tag_b.save(flush:true)

        //prepare mock base config
        test_config = new Configuration(id: '2', name:"test_config")
        test_config.save(validate:false)

        a = 3
    }

    def cleanup() {
    }

    void "test saveSystemTag"() {
        setup:
        controller.params.name = "tag_test"
        controller.userService = [getUser: { return user }]
        when:
        controller.saveSystemTag()
        then:
        Tag.countByName("tag_test") == 1
        response.json.success == true
    }

    void "test saveSystemTag error Scenario"() {
        setup:
        controller.userService = [getUser: { return user }]
        when:
        controller.saveSystemTag()
        then:
        response.json.success == false
        response.json.errorMessage == "Fill in the Tag Name correctly"

    }

    void "test saveAlertTag"() {
        setup:
        controller.params.name = "alert_tag_test"
        controller.userService = [getUser: { return user }]
        when:
        controller.saveAlertTag()
        then:
        AlertTag.countByName("alert_tag_test") == 1
        response.json.success == true
    }

    void "test saveAlertTag error Scenario"() {
        setup:
        controller.userService = [getUser: { return user }]
        when:
        controller.saveAlertTag()
        then:
        response.json.success == false
        response.json.errorMessage == "Fill in the Alert Tag Name correctly"
    }

    void "test removeSystemTag"() {
        setup:
        controller.params.id = Tag.findByName('test_tag_a').id
        when:
        controller.removeSystemTag()
        then:
        response.json.success == true
        Tag.countByName('test_tag_a') == 0
    }

    void "test removeAlertTag"() {
        setup:
        controller.params.id = AlertTag.findByName('alert_tag_b').id
        when:
        controller.removeAlertTag()
        then:
        response.json.success == true
        AlertTag.countByName('alert_tag_b') == 0
    }

    void "test editSystemTag"() {
        setup:
        controller.params.id = Tag.findByName('test_tag_a').id
        controller.params.name = 'test_tag_a_edited'
        when:
        controller.editSystemTag()
        then:
        response.json.success == true
        Tag.countByName('test_tag_a') == 0
        Tag.countByName('test_tag_a_edited') == 1
    }

    void "test editTag"() {
        setup:
        controller.params.id = AlertTag.findByName('alert_tag_b').id
        controller.params.name = 'alert_tag_b_edited'
        when:
        controller.editTag()
        then:
        response.json.success == true
        AlertTag.countByName('alert_tag_b') == 0
        AlertTag.countByName('alert_tag_b_edited') == 1
    }

    void "test listSystemTags"(){
        setup:
        controller.userService = [getUser: { return user }]
        when:
        controller.listSystemTags()
        then:
        response.getJson()[0]["name"] == "test_tag_a"
    }

    void "test listTags"(){
        setup:
        controller.userService = [getUser: { return user }]
        when:
        controller.listTags()
        then:
        response.getJson()[0]["name"] == "alert_tag_b"
    }

    void "test removeAlertTag when tag not found"(){
        AlertTag tag = new AlertTag()
        tag.save(validate:true)
        when:
        params.id = 11L
        controller.removeAlertTag()
        then:
        response.success == false

    }

    void "test removeSystemTag when tag not found"(){
        Tag tag = new Tag()
        tag.save(validate:true)
        when:
        params.id = 11L
        controller.removeAlertTag()
        then:
        response.success == false
    }

    void "test editSystemTag error scenario"() {
        Tag tag = new Tag()
        tag.save(validate:true)
        when:
        params.id = 11L
        controller.editSystemTag()
        then:
        response.success == false
    }


}
