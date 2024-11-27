package com.rxlogix.helper

import com.rxlogix.config.Action
import com.rxlogix.config.ActionConfiguration
import com.rxlogix.config.ActionType
import com.rxlogix.user.User
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by Lei Gao on 3/23/16.
 */
@Ignore
class ActivityHelperSpec extends Specification {
    @Shared
    ActivityHelper activityHelper = new Object() as ActivityHelper

    def setup() {
        def actCfg = new ActionConfiguration(value: "test")
        actCfg.save(validate: false)
        def actType = new ActionType(value: "type")
        actType.save(validate: false)
        def user1 = new User(username: "test")
        user1.save(validate: false)

        def Action act1 = new Action(config: actCfg, details: "test",
                type: actType, createdDate: new Date(), assignedTo: user1, actionStatus: "New")
        act1.save(validate: false)
    }

    def "test for composeDescription on details" () {
        given:
            def actOld = Action.findByDetails('test')
            def actNew = new Action(id: actOld.id, config: actOld.config,
                    type: actOld.type, details: actOld.details,
                    comments: actOld.comments)
            actNew.details = "emmm"

        expect:
            def result = activityHelper.composeDescription(actOld, 'details', actNew, "")
            result == "details changed from 'test' to 'emmm'"
    }

    def "test for composeDescription on actionStatus" () {
        given:
            def actOld = Action.findByDetails('test')
            def actNew = new Action(id: actOld.id, config: actOld.config,
                    type: actOld.type, details: actOld.details,
                    comments: actOld.comments)
            actNew.actionStatus = "emmm"

        expect:
            def result = activityHelper.composeDescription(actOld, 'actionStatus', actNew, "")
            result == "actionStatus changed from 'New' to 'emmm'"
    }

    def "test for composeDescription on action config" () {
        given:
            def actOld = Action.findByDetails('test')
            def actNew = new Action(id: actOld.id, config: actOld.config,
                    type: actOld.type, details: actOld.details,
                    comments: actOld.comments)
            actNew.config = new ActionConfiguration(value: "test2")

        expect:
            def result = activityHelper.composeDescription(actOld, 'config', actNew, "")
            result == "config changed from 'test' to 'test2'"
    }

    def "test for composeDescription on action assignedTo" () {
        given:
            def actOld = Action.findByDetails('test')
            def actNew = new Action(id: actOld.id, config: actOld.config,
                    type: actOld.type, details: actOld.details,
                    comments: actOld.comments)
            actNew.assignedTo = new User(username: "user2")

        expect:
            def result = activityHelper.composeDescription(actOld, 'assignedTo', actNew, "")
            result == "assignedTo changed from 'test' to 'user2'"
    }

    def "test for composeDescription on action config and actionStatus" () {
        given:
            def actOld = Action.findByDetails('test')
            def actNew = new Action(id: actOld.id, config: actOld.config,
                    type: actOld.type, details: actOld.details,
                    comments: actOld.comments)
            actNew.config = new ActionConfiguration(value: "test2")
            actNew.actionStatus = "emmm"

        expect:
            def result = activityHelper.composeDescription(actOld, 'config', actNew,
                    activityHelper.composeDescription(actOld, 'actionStatus', actNew, ""))
            result == "actionStatus changed from 'New' to 'emmm', config changed from 'test' to 'test2'"
    }

    def "test for prepareActivityDescription"() {
        given:
            def actOld = Action.findByDetails('test')
            def actNew = new Action(id: actOld.id, config: actOld.config,
                    type: actOld.type, details: actOld.details,
                    actionStatus: actOld.actionStatus, comments: actOld.comments,
                    assignedTo: actOld.assignedTo)
            actNew.config = new ActionConfiguration(value: "test2")
            actNew.actionStatus = "emmm"

        expect:
            def result = activityHelper.prepareActivityDescription(actOld, actNew)
            result == "Action [" + actOld.id + "] actionStatus changed from 'New' to 'emmm', config changed from 'test' to 'test2'"
    }

    def "test for prepareActivityDescription on no changes"() {
        given:
            def actOld = Action.findByDetails('test')
            def actNew = new Action(id: actOld.id, config: actOld.config,
                    type: actOld.type, details: actOld.details,
                    actionStatus: actOld.actionStatus, comments: actOld.comments,
                    assignedTo: actOld.assignedTo)

        expect:
            def result = activityHelper.prepareActivityDescription(actOld, actNew)
            result == ""
    }
}
