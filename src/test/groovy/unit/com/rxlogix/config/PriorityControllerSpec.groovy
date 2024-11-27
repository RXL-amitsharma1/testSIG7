package com.rxlogix.config

import com.rxlogix.cache.CacheService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.*

@Ignore
class PriorityControllerSpec extends Specification implements ControllerUnitTest<PriorityController> {

    def setup() {

        controller.params.value = "testPriority"
        controller.params.displayName = "testPriority"
        controller.params.description = "test description"
        controller.params.displayName_local =  "testPriority"
        controller.params.description_local = "test description"
        controller.params.display = true
        controller.params.defaultPriority = false
        controller.params.reviewPeriod = 0
    }

    def cleanup() {

    }

    void "test the create action"() {

        when:
        def model = controller.create()

        then:
        model.priorityInstance.value == "testPriority"
        model.priorityInstance.displayName == "testPriority"
        model.priorityInstance.description == "test description"
        model.priorityInstance.display == true
        model.priorityInstance.defaultPriority == false
        model.priorityInstance.reviewPeriod == 0
    }

    void "test the create action with change in the value of default priority"() {

        setup:
        controller.params.defaultPriority = true

        when:
        def model = controller.create()

        then:
        model.priorityInstance.defaultPriority == true
    }

    void "test the save action"() {

        setup:
        //Mocking the service
        controller.priorityService = [savePriority: { return true}]
        controller.cacheService = [updatePriorityCache : { return true }]

        when:
        request.method = 'POST'
        controller.save()

        then:
        response.status == 302  // Verification that status is 302 meaning that url has been redirected.
        response.redirectUrl == '/priority/list' // Verification of the redirect url.
        controller.flash.message != null
        controller.flash.args != null
        controller.flash.defaultMessage != null
    }

    void "test the save action when error occurs"(){

        when:
        request.method = 'POST'
        params.id = 1
        controller.save()

        then:
        response.status == 200
        view == "/priority/create"
        model.priorityInstance.id == params.id
    }

    void "test the show action."() {
        setup:
        priorityInstance.save()
        controller.params.id = priorityInstance.id

        expect:
        controller.show()  == [priorityInstance: priorityInstance]

        where:
        //Prepare the priority instance.
        priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : false, reviewPeriod :1])

    }


    void "test the show action for the user that doesnot exist."() {
        setup:
        controller.params.id = -1L

        when:
        controller.show()

        then:
        response.status == 302
        response.redirectUrl != null
        response.redirectUrl == '/priority/list'
        controller.flash.message != null
    }

    void 'test the edit action.'() {
        setup:
        priorityInstance.save()
        controller.params.id = priorityInstance.id

        expect:
        controller.edit()  == [priorityInstance: priorityInstance]

        where:
        //Prepare the priority instance.
        priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : false, reviewPeriod :1])

    }

    void 'test the edit action for the user that doesnot exist.'() {
        setup:
        controller.params.id = -1L

        when:
        controller.edit()

        then:
        response.status == 302
        response.redirectUrl != null
        response.redirectUrl == '/priority/list'
        controller.flash.message != null
    }

    void "test the update action when priority found" () {
        setup:
        //Priority Instance has been created
        Priority priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : false, reviewPeriod :1])

        priorityInstance.save()
        controller.params.id = priorityInstance.id

        //Mocking the service
        controller.priorityService = [savePriority: { return true}]

        when:
        request.method = 'POST'
        controller.update()

        then:
        response.status == 302 // Verification that status is 302 meaning that url has been redirected.
        response.redirectUrl == '/priority/list' // Verification of the redirect url.
        controller.flash.message != null
        controller.flash.args != null
        controller.flash.defaultMessage != null
    }


    void "test the update action based on version"(){
        when:
        request.method = 'POST'

        Priority priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : false, reviewPeriod :1])
        priorityInstance.save(flush:true,failOnError:true)
        Priority priorityInstanceNew = Priority.get(1)
        priorityInstanceNew.displayName = "testPriorityNew"
        priorityInstanceNew.save(flush:true,failOnError:true)
        Priority priorityInstanceNew2 = Priority.get(1)
        priorityInstanceNew2.displayName = "testPriorityNew2"
        priorityInstanceNew2.save(flush:true,failOnError:true)

        params.id = 1
        params.version = 1

        controller.update()

        then:
        response.status == 200
        view == '/priority/edit'
    }

    void  "test the update action when priority not found"() {
        setup:
        Priority priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : false, reviewPeriod :1])
        priorityInstance.save(flush:true,failOnError:true)

        when:
        request.method = 'POST'
        params.id = 2
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == '/priority/edit/'+params.id
        controller.flash.message != null
        controller.flash.defaultMessage.startsWith('Priority not found') == true
    }

    void "test the update action when validation failure occurs"(){
        setup:

        Priority priorityInstance =
                new Priority(displayName: 'testPriority',value: 'testPriority',display: true,defaultPriority: false,reviewPeriod: 1)
        priorityInstance.save(flush:true,failOnError:true)

        when:
        request.method = 'POST'
        params.id = 1
        params.value = 'testPriority'
        params.displayName = 'testPriority'
        params.display = true
        params.defaultPriority = false
        params.reviewPeriod = null
        controller.update()

        then:
        response.status == 200
        view == '/priority/edit'
        model.priorityInstance.id == params.id
    }

    void "test the delete action when priority is found"(){
        setup:
        Priority priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : false, reviewPeriod :1])
        priorityInstance.save(failOnError:true,flush:true)

        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.deletePriorityCache(priorityInstance) >> {
            return true
        }
        controller.cacheService = mockCacheService

        when:
        request.method = 'POST'
        params.id = 1
        controller.delete()

        then:
        response.status == 302
        response.redirectedUrl == '/priority/list'
        controller.flash.message != null
        controller.flash.defaultMessage.endsWith("deleted") == true
    }

    void "test the delete action when priority is not found"(){
        Priority priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : false, reviewPeriod :1])
        priorityInstance.save(failOnError:true,flush:true)

        when:
        request.method = 'POST'
        params.id = 2
        controller.delete()

        then:
        response.status == 302
        response.redirectedUrl == '/priority/list'
    }
}
