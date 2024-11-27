package com.rxlogix

import com.rxlogix.config.SafetyGroup
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Ignore
class SafetyLeadSecurityServiceSpec extends Specification implements ServiceUnitTest<SafetyLeadSecurityService> {


    @Shared
    def user1
    @Shared
    def user2
    @Shared
    def user3
    @Shared
    def safetyGroup1
    @Shared
    def safetyGroup2
    @Shared
    def safetyGroup3

    def setup() {
        user1 = new User(username: 'u1', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user1.save()
        user2 = new User(username: 'u2', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user2.save()
        user3 = new User(username: 'u3', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user3.save()

        assert User.count == 3
        safetyGroup1 = new SafetyGroup(name: 'sg1')
        safetyGroup1.allowedProductList = ['ibuprofen', 'motrin']
        safetyGroup1.beforeValidate()

        safetyGroup2 = new SafetyGroup(name: 'sg2')
        safetyGroup2.allowedProductList = ['ibuprofen', 'aspirin']
        safetyGroup2.beforeValidate()

        safetyGroup3 = new SafetyGroup(name: 'sg3')
        safetyGroup3.allowedProductList = ['motrin', 'placebo']
        safetyGroup3.beforeValidate()

        user1.safetyGroups = [safetyGroup1, safetyGroup3]
        user1.save()

        user3.safetyGroups = [safetyGroup2]
        user3.save()



    }
    def cleanup() {
    }

    def "test user is safety lead for MOTRIN"(){
        when:
        def productList = service.allowedProductForUser(user1) as Set
        def genericList = ['drug1'] as Set
        def allowedProductSet = [] as Set

        then:
        assert service.isProductSafetyLead(['MOTRIN'] as Set, productList, genericList, allowedProductSet) == false
    }

    def "test user is not safety lead for ASPIRIN"(){
        when:
        def productList = service.allowedProductForUser(user1) as Set
        def genericList = ['drug1'] as Set
        def allowedProductSet = [] as Set
        then:
        assert !service.isProductSafetyLead(['ASPIRIN'] as Set, productList, genericList, allowedProductSet)
    }

    def "test user without SafetyGroups is not a SafetyLead"(){
        when:
        def productList = service.allowedProductForUser(user2) as Set
        def genericList = ['drug1'] as Set
        def allowedProductSet = [] as Set
        then:
        assert service.isProductSafetyLead(['ASPIRIN'] as Set, productList, genericList, allowedProductSet) == false

    }

    def 'test user is safety lead for PLACEBO,MOTRIN'(){
        when:
        def productList = service.allowedProductForUser(user1) as Set
        def genericList = ['drug1'] as Set
        def allowedProductSet = [] as Set

        then:
        assert service.isProductSafetyLead(['placebo', 'motrin'] as Set, productList, genericList, allowedProductSet)
    }

    def 'test user is safety lead for generic ASPIRIN'(){
        when:
        def productList = service.allowedProductForUser(user3) as Set
        def genericList = ['asprin'] as Set
        def allowedProductSet = [] as Set

        then:
        assert service.isProductSafetyLead(['aspirin'] as Set, productList, genericList, allowedProductSet)
    }
}
