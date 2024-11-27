package unit.com.rxlogix

import com.rxlogix.CRUDService
import com.rxlogix.UserService
import com.rxlogix.config.Disposition
import com.rxlogix.signal.Justification
import com.rxlogix.JustificationService
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class JustificationServiceSpec extends Specification implements ServiceUnitTest<JustificationService> {
    Disposition disposition
    Justification justification1
    Justification justification2
    Justification justification3
    Disposition disposition2


    def setup(){
        Preference preference=new Preference(timeZone: "UTC")
        User user=new User(username: "user1",preference: preference)
        user.save(validate:false)
        justification1=new Justification(name: "just1", justification: "justification1", feature: "feature1",
                attributesMap: ["key":"value"])
        justification1.save(flush:true,failOnError:true)
        justification3=new Justification(name: "just3", justification: "justification3", feature: '"alertWorkflow": "on"',
                attributesMap: ["key":"value"])
        justification3.save(flush:true,failOnError:true)
        justification1.save(flush:true,failOnError:true)
        justification2=new Justification(name: "just2", justification: "justification2", feature:'"signalWorkflow": "on"',
                attributesMap: ["key":"value"])
        justification2.save(flush:true,failOnError:true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)
        disposition2 = new Disposition(value: "ValidatedSignal2", displayName: "Validated Signal2", validatedConfirmed: false, reviewCompleted: true, abbreviation: "vs")
        disposition2.save(failOnError: true)
        justification3.addToDispositions(disposition2)
        justification3.addToDispositions(disposition)
        justification3.save(flush:true,failOnError:true)
        justification2.addToDispositions(disposition)
        justification2.save(flush:true,failOnError:true)
    }

    def clean(){
    }

    void "test bindDispositions"(){
        setup:

        when:
        service.bindDispositions(justification1 , [disposition.id , disposition2.id])
        then:
        justification1.dispositions.size()==2
        List dispositions = justification1.dispositions
        dispositions[0].id == disposition.id
        dispositions[1].id == disposition2.id
    }

    void "test bindDispositions when disposition already present"(){
        setup:

        when:
        service.bindDispositions(justification2 , [disposition2.id])
        then:
        justification2.dispositions.size()==1
    }

    void "test bindDispositions when disposition is String"(){
        setup:

        when:
        service.bindDispositions(justification1 , disposition.id.toString())
        then:
        justification1.dispositions.size()==1
    }

    void "test bindDispositions when clearing disposition"(){
        setup:

        when:
        service.bindDispositions(justification2 , null)
        then:
        justification2.dispositions.size()==0
    }

    void "test bindDispositions when no disposition"(){
        setup:

        when:
        service.bindDispositions(justification1 , null)
        then:
        justification1.dispositions == null
    }

    void "test fetchJustificationsForDisposition"(){
        setup:

        when:
        List justifications = service.fetchJustificationsForDisposition(disposition.id , true)
        then:
        justifications.size() == 1
    }

    void "test fetchJustificationsForDisposition when signalWorkflow is false"(){
        setup:

        when:
        List justifications = service.fetchJustificationsForDisposition(disposition2.id , false)
        then:
        justifications.size() == 1
    }

    void "test fetchJustificationsForDispositionForBR"(){
        setup:

        when:
        List result = service.fetchJustificationsForDispositionForBR(disposition.id )
        then:
        result.size()==2
    }

}
