package unit.com.rxlogix

import com.rxlogix.CRUDService
import com.rxlogix.JustificationController
import com.rxlogix.JustificationService
import com.rxlogix.UserService
import com.rxlogix.config.Disposition
import com.rxlogix.signal.Justification
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class JustificationControllerSpec extends Specification implements ControllerUnitTest<JustificationController> {
    Disposition disposition
    Justification justification1
    Justification justification2

    def setup(){
        Preference preference=new Preference(timeZone: "UTC")
        User user=new User(username: "user1",preference: preference)
        user.save(validate:false)
        justification1=new Justification(name: "just1", justification: "justification1", feature: "feature1",
                attributesMap: ["key":"value"])
        justification1.save(flush:true,failOnError:true)
        justification2=new Justification(name: "just2", justification: "justification2", feature:'"signalWorkflow": "on"',
                attributesMap: ["key":"value"])
        justification2.save(flush:true,failOnError:true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)
        justification2.addToDispositions(disposition)
        justification2.save(flush:true,failOnError:true)
        JustificationService justificationService = Mock(JustificationService)
        controller.justificationService = justificationService
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        justificationService.fetchJustificationsForDisposition()>>{
            return justification2.justification
        }
        controller.CRUDService.userService.springSecurityService=springSecurityService
    }

    def clean(){
    }

    void "test save action when params.id is given"(){
        setup:
        params.id=1L
        params.justificationName="changed name"
        params.justificationText="changed justification"
        params.linkedDisposition=null
        when:
        controller.save()
        Justification justification=Justification.get(1)
        then:
        response.status==200
        JSON.parse(response.text).status==true
        justification.name=="changed name"
        justification.justification=="changed justification"
    }
    void "test save action when params.id is not given"(){
        setup:
        params.justificationName="changed name"
        params.justificationText="changed justification"
        params.linkedDisposition=null
        when:
        controller.save()
        Justification justification=Justification.get(3)
        then:
        response.status==200
        JSON.parse(response.text).status==true
        justification.name=="changed name"
        justification.justification=="changed justification"
    }
    void "test save action when validation exception occurs"(){
        setup:
        params.id=1L
        params.justificationName=""
        params.justificationText="changed justification"
        params.linkedDisposition=null
        when:
        controller.save()
        then:
        response.status==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).message=="Please fill the required fields."
    }
    void "test edit action"(){
        when:
        controller.edit(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==true
        JSON.parse(response.text).data.name==Justification.get(1).name
    }
    void "test handleException method"(){
        when:
        Exception obj=new Exception()
        def result=controller.handleException(obj)
        then:
        response.status==200
        result.status==false
    }
    void "test delete action"(){
        when:
        controller.delete(justification1.id)
        then:
        response.status==200
        JSON.parse(response.text).status==true
        JSON.parse(response.text).data==null
    }
    void "test createFeatureJson action"(){
        when:
        params.alertPriority="serious"
        Map result=controller.createFeatureJson()
        then:
        response.status==200
        result.alertPriority=="serious"
    }
    void "test list action"(){
        when:
        def result=controller.list()
        then:
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].id==2
        JSON.parse(response.text)[1].id==1
    }
    void "test  fetchJustificationsForDisposition"(){
        setup:
        params.id = disposition.id
        params.signalWorkFlow = "true"
        when:
        def result=controller.fetchJustificationsForDisposition()
        then:
        noExceptionThrown()
    }
    void "test fetchJustificationsForDisposition for signalWorkflow false"(){
        setup:
        params.id = disposition.id
        params.signalWorkFlow = "false"
        when:
        def result=controller.fetchJustificationsForDisposition()
        then:
        noExceptionThrown()
    }
    void "test  fetchJustificationsForDispositionForBR"(){
        setup:
        params.id = disposition.id
        when:
        def result=controller.fetchJustificationsForDispositionForBR()
        then:
        noExceptionThrown()
    }
    void "test index"(){

        when:
        def result=controller.index()
        then:
        result.size()==1
        result[0].id == disposition.id
        result[0].name == disposition.displayName
    }
}
