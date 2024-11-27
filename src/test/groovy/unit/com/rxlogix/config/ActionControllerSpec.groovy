package com.rxlogix.config

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.*

class ActionControllerSpec extends Specification implements ControllerUnitTest<ActionController> {

    def setup() {
    }

    def cleanup() {
    }

    void "test setMeetingProperties"(){
        Meeting meet = new Meeting()
        meet.save(validate:false)
        Action action = new Action()
        action.save(failOnError:true,validate:false)
        when:
        params.meetingId = "${meet.id}"
        controller.setMeetingProperties(params,action)
        then:
        meet.actions[0].id == action.id
    }

    void "test update"(){
        Action action = new Action(comments:"comment")
        action.save(failOnError:true,validate:false)
        when:
        params.comments = "updated comment"
        params.id = action.id
        controller.update()
        then:
        action.comments != "updated comment"
    }

    void "test show"(){
        Action action = new Action()
        action.save(failOnError:true,validate:false)
        when:
        params.id = action.id
        Map mp = controller.show()
        then:
        mp['actionInstance'] == action
    }
    void "test show missing"(){
        Action action = new Action()
        action.save(failOnError:true,validate:false)
        when:
        params.id = 234
        controller.show()
        then:
        flash.message == 'action.not.found'
    }

    void "test edit"(){
        Action action = new Action()
        action.save(failOnError:true,validate:false)
        when:
        params.id = action.id
        Map mp = controller.edit()
        then:
        mp['actionInstance'] == action
    }
    void "test edit missing"(){
        Action action = new Action()
        action.save(failOnError:true,validate:false)
        when:
        params.id = 234
        controller.edit()
        then:
        flash.message == 'action.not.found'
    }

}
