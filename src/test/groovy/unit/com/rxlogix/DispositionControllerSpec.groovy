package unit.com.rxlogix

import com.hazelcast.core.HazelcastInstance
import com.rxlogix.BaseSingleAlert
import com.rxlogix.DispositionController
import com.rxlogix.DispositionService
import com.rxlogix.UserService
import com.rxlogix.cache.CacheService
import com.rxlogix.cache.HazelcastService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.User
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class DispositionControllerSpec extends Specification implements ControllerUnitTest<DispositionController> {
    @Shared Disposition disposition1
    @Shared Disposition disposition2
    @Shared DispositionRule dispositionRule
    def setup() {
        disposition1=new Disposition(id:1,value: "disp1",displayName: "name1",description: "desc1",abbreviation: "abb")
        disposition1.save(flush:true,failOnError:true)
        disposition2=new Disposition(id:2,value: "disp2",displayName: "name2",description: "desc2",abbreviation: "ab")
        disposition2.save(flush:true,failOnError:true)
        dispositionRule=new DispositionRule(name: "disposition rule1",description: "rule description",
        incomingDisposition: disposition1,targetDisposition: disposition2,isDeleted: false)
        dispositionRule.save(flush:true,failOnError:true)
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.updateDispositionCache(disposition1) >> {
            return disposition1
        }
        controller.cacheService=mockCacheService
    }

    def cleanup() {
    }

    void "test disposition index action"(){
        when:
        controller.index()
        then:
        response.status==302
        response.redirectedUrl=='/disposition/list'
    }
    void "test disposition list action"(){
        when:
        Map result=controller.list()
        then:
        response.status==200
        result.dispositionInstanceList[0]==Disposition.get(1)
        result.dispositionInstanceTotal==2
    }
    void "test listDisposition action"(){
        when:
        controller.listDisposition()
        then:
        response.status==200
        JSON.parse(response.text).data[0].get("Display Name")=="name1"
        JSON.parse(response.text).data[1].get("Display Name")=="name2"
    }
    void "test create action"(){
        given:
        params.value="test value"
        params.displayName="test name"
        params.description="test description"
        params.abbreviation="C"
        params.colorCode="#000"
        when:
        Map result=controller.create()
        then:
        response.status==200
        result.dispositionInstance.displayName=="test name"
    }
    void "test save action for success"(){
        given:
        params.id=4
        params.value="test value"
        params.displayName="test name"
        params.description="test description"
        params.abbreviation="C"
        params.colorCode="#000"
        Disposition disposition=new Disposition(id:1,value: "disp",displayName: "name",description: "desc1",abbreviation: "abb")
        disposition.save(flush:true,failOnError:true)
        when:
        request.method = 'POST'
        controller.save()
        then:
        response.status==302
        response.redirectedUrl=="/disposition/list"
        flash!=null
    }
    void "test save action when validation error occurs"(){
        given:
        params.id=12
        params.value="disp12"
        params.displayName="name2"
        params.description="test description"
        params.abbreviation="C"
        params.colorCode="#000"
        when:
        request.method = 'POST'
        controller.save()
        then:
        view=="/disposition/create"
        model.dispositionInstance.displayName=="name2"
        response.status==200
    }
    void "test show disposition success and fail both"(){
        when:
        params.id=2
        Map result=controller.show()
        then:
        response.status==200
        result.dispositionInstance==Disposition.get(2)
        when:
        params.id=10
        controller.show()
        then:
        response.status==302
        flash!=null
        response.redirectedUrl=="/disposition/list"
    }
    void "test edit action for both sucess and fail"(){
        when:
        params.id=1
        Map result=controller.edit()
        then:
        response.status==200
        result.dispositionInstance==Disposition.get(1)
        when:
        params.id=10
        controller.edit()
        then:
        response.status==302
        response.redirectedUrl=="/disposition/list"
        flash.message!=null
    }
    void "test update action when params.version is given"() {
        when:
        Disposition disposition = Disposition.get(1)
        disposition.colorCode = "#111"
        disposition.save(flush: true, failOnError: true)
        Disposition disposition2 = Disposition.get(1)
        disposition2.colorCode = "#111"
        disposition2.save(flush: true, failOnError: true)
        params.id = 1
        params.version = 1
        request.method = 'POST'
        controller.update()
        then:
        response.status == 200
        model.dispositionInstance == Disposition.get(1)
        view == "/disposition/edit"
    }
    @Ignore
    void "test update action when params.display is false"() {
        when:
        params.id = 1
        params.display = false
        request.method = "POST"
        controller.update()
        then:
        response.status == 200
        model.dispositionInstance == Disposition.get(1)
        view == "/disposition/edit"
    }
    void "test update action when update successfully happen"() {
        given:
        params.id = 1
        params.value = "test value"
        params.displayName = "test name"
        params.description = "test description"
        params.abbreviation = "C"
        params.colorCode = "#000"
        when:
        request.method = "POST"
        controller.update()
        then:
        response.status==302
        flash != "null"
        flash.args == [params.id]
        flash.defaultMessage == "Disposition ${params.id} updated"
        response.redirectedUrl == "/disposition/list"
    }
    void "test update when exception occurs"(){
        given:
        params.id = 1
        params.value = "disp2"
        params.displayName = "name2"
        params.description = "test description"
        params.abbreviation = "C"
        params.colorCode = "#000"
        when:
        request.method = "POST"
        controller.update()
        then:
        response.status==200
        model.dispositionInstance==Disposition.get(2)
        view=="/disposition/edit"
    }
    void "test update action when value of params.id does not exist"() {
        when:
        params.id=1
        request.method = "POST"
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/disposition/list"
        flash.args==[params.id]
    }
    void "test delete action when value of params.id exist"() {
        when:
        params.id = 1
        request.method = "POST"
        controller.delete()
        then:
        response.status == 302
        flash.args == [params.id]
        flash.defaultMessage == "Disposition ${params.id} deleted"
        response.redirectedUrl == "/disposition/list"
    }
    void "test delete action when value of params.id does not exist"() {
        when:
        params.id=10
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        flash.args==[params.id]
        flash.defaultMessage=="Disposition not found with id ${params.id}"
        response.redirectedUrl=="/disposition/list"
    }
    @Ignore
    void "test delete action when exception occurs"() {
        when:
        params.id=2
        request.method="POST"
        controller.delete()
        then:
        flash.defaultMessage == "Disposition ${params.id} could not be deleted"
        response.status==302
        flash.args==[params.id]
        response.redirectedUrl=="/disposition/list"
    }
    void "test check for alert action for succcess"() {
        setup:
        Disposition disposition=new Disposition(value: "dispTest",displayName: "nameTest",description: "descTest",abbreviation: "abb")
        disposition.save(flush:true,failOnError:true)
        SingleCaseAlert singleCaseAlert=new SingleCaseAlert(disposition: disposition).save(validate:false)
        when:
        params.id=3
        controller.checkForAlerts()
        then:
        response.status == 200
    }
    void "test check for alert action when fails"(){
        when:
        controller.checkForAlerts()
        then:
        response.status==400
    }

    void "test check for fetchDispositionsList()"() {
        when:
        controller.fetchDispositionsList()
        then:
        JSON.parse(response.text).dispositionList.size() == 2
    }
}


