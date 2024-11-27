package com.rxlogix.plugable

import com.rxlogix.config.*
import com.rxlogix.enums.ActionStatus
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Ignore
class ActionDueNotificationServiceSpec extends Specification implements ServiceUnitTest<ActionDueNotificationService> {

    @Shared
    def actionService

    def setup() {

        createTestData()

        prepareAlerts_cond()
        prepareActions_cond()
    }

    def cleanup() {
    }


    void "test action due today"() {
        when:
        def alert = AdHocAlert.findByName('aha1')
        def action = Action.findByDetails('testing')
        action.dueDate = new DateTime().toDate()
        action.alert = alert
        alert.addToActions(action)


        then:
        service.actionDueCondition(alert, DateTime.now()) != null
    }

    void "test action due date in 2 days"() {
        when:
        def alert = AdHocAlert.findByName('aha1')
        def action = Action.findByDetails('testing')
        action.dueDate = DateTime.now().plusDays(2).toDate()
        action.alert = alert
        alert.addToActions(action)

        then:
        service.actionDueCondition(alert, DateTime.now()) == null
    }

    void "test open action due date was yesterday"() {
        when:
        def alert = AdHocAlert.findByName('aha1')
        def action = Action.findByDetails('testing')
        action.dueDate = DateTime.now().minusDays(1).toDate()
        action.alert = alert
        alert.addToActions(action)

        then:
        service.actionDueCondition(alert, DateTime.now()) != null

    }

    void "test closed action due date was yesterday"() {
        when:
        def alert = AdHocAlert.findByName('aha1')
        def action = Action.findByDetails('testing')
        action.dueDate = DateTime.now().minusDays(1).toDate()
        action.alert = alert
        action.actionStatus = 'Closed'
        alert.addToActions(action)

        then:
        service.actionDueCondition(alert, DateTime.now()) == null

    }

    //Setup Alerts and Documents *****************************************************************
    def prepareAlerts_cond() {
        new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2016, 02, 03, 0, 0, 0).toDate(),
                detectedBy: "Company",
                topic:  "rash",
                initialDataSource: "test",
                name: "aha1",
                alertVersion: 0,
                priority: Priority.findByValue("High")).save(flush: true)
        assert AdHocAlert.count() == 1
    }

    def prepareActions_cond() {
        def ac = new ActionConfiguration(value: 'PRV',
                displayName: 'Periodic review',
                isEmailEnabled: true,
                description: 'testing')
        ac.save(validate: false)

        def at = new ActionType(value: "type1", displayName: "Type1")
        at.save(validate: false)
        assert ActionConfiguration.count == 1

        def act = new Action(
                dueDate: DateTime.now().plusDays(4),
                createdDate: DateTime.now().minusDays(2),
                assignedTo: User.findByUsername('admin'),
                details: 'testing',
                actionStatus: ActionStatus.New,
                type: at,
                config: ac,
        )
        act.save(flush: true)

        println act

        assert Action.count() == 1
    }
}
