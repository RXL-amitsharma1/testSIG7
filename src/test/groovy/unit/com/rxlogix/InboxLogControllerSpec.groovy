package unit.com.rxlogix

import com.rxlogix.InboxLog
import com.rxlogix.InboxLogController
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class InboxLogControllerSpec extends Specification implements ControllerUnitTest<InboxLogController> {

    User userObj
    User userObj2
    List<InboxLog> inboxLogList
    InboxLog userObj1Type1ReadInboxLog
    InboxLog userObj1Type1UnReadInboxLog
    InboxLog userObj1Type1DeleteInboxLog
    InboxLog userObj1Type2ReadInboxLog
    InboxLog userObj1Type2UnReadInboxLog
    InboxLog userObj1Type2DeleteInboxLog
    InboxLog userObj2Type1ReadInboxLog
    InboxLog userObj2Type2ReadInboxLog

    def setup() {
        Preference preferenceObj = new Preference(createdBy: "createdBy", modifiedBy: "modifiedBy", locale: new Locale("en"), isEmailEnabled: false)
        preferenceObj.save(validate: false)
        userObj = new User()
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.grailsApplication = grailsApplication
        userObj.preference = preferenceObj
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj.groups = []
        userObj.save(validate: false)

        userObj2 = new User(username: 'username2')
        userObj2.save(validate: false)

        inboxLogList = []
        Date currentDate = new Date()
        userObj1Type1ReadInboxLog = new InboxLog(inboxUserId: userObj.id, type: 'TYPE_1', isRead: true, isDeleted: false, createdOn: currentDate).save(validate: false)
        userObj1Type1UnReadInboxLog = new InboxLog(inboxUserId: userObj.id, type: 'TYPE_1', isRead: false, isDeleted: false, createdOn: currentDate).save(validate: false)
        userObj1Type1DeleteInboxLog = new InboxLog(inboxUserId: userObj.id, type: 'TYPE_1', isRead: true, isDeleted: true, createdOn: currentDate).save(validate: false)
        userObj1Type2ReadInboxLog = new InboxLog(inboxUserId: userObj.id, type: 'TYPE_2', isRead: true, isDeleted: false, createdOn: currentDate).save(validate: false)
        userObj1Type2UnReadInboxLog = new InboxLog(inboxUserId: userObj.id, type: 'TYPE_2', isRead: false, isDeleted: false, createdOn: currentDate).save(validate: false)
        userObj1Type2DeleteInboxLog = new InboxLog(inboxUserId: userObj.id, type: 'TYPE_2', isRead: true, isDeleted: true, createdOn: currentDate).save(validate: false)
        userObj2Type1ReadInboxLog = new InboxLog(inboxUserId: userObj2.id, type: 'TYPE_1', isRead: true, isDeleted: false, createdOn: currentDate).save(validate: false)
        userObj2Type2ReadInboxLog = new InboxLog(inboxUserId: userObj2.id, type: 'TYPE_2', isRead: true, isDeleted: false, createdOn: currentDate).save(validate: false)
        inboxLogList << userObj1Type1ReadInboxLog
        inboxLogList << userObj1Type1UnReadInboxLog
        inboxLogList << userObj1Type1DeleteInboxLog
        inboxLogList << userObj1Type2ReadInboxLog
        inboxLogList << userObj1Type2UnReadInboxLog
        inboxLogList << userObj1Type2DeleteInboxLog
        inboxLogList << userObj2Type1ReadInboxLog
        inboxLogList << userObj2Type2ReadInboxLog
    }

    void "test index() action"() {
        setup:
        params.detectedDate = '12/09/2015'
        controller.userService = [getUser: { return userObj }]

        when:
        Map response = controller.index()

        then:
        response.inboxList.size() == inboxLogList.count { !it.isDeleted && it.inboxUserId == userObj.id }
    }

    void "test index() action default filter"() {
        setup:
        controller.userService = [getUser: { return userObj }]

        when:
        Map response = controller.index()

        then:
        response.activeFilter == "today"
    }


    void "test forUser() action"() {
        setup:
        params.id = userObj.id
        controller.userService = [getUser: { return userObj }]
        def jsonSlurper = new JsonSlurper()

        when:
        controller.forUser()

        then:
        jsonSlurper.parseText(response.contentAsString).size() == inboxLogList.count {
            it.inboxUserId == userObj.id && !it.isRead && !it.isDeleted
        }
    }

    void "test markAsRead() action"() {
        setup:
        params.id = userObj1Type1UnReadInboxLog.id

        when:
        controller.markAsRead()

        then:
        userObj1Type1UnReadInboxLog.isRead
    }

    void "test markAsUnread() action"() {
        setup:
        params.id = userObj1Type1ReadInboxLog.id

        when:
        controller.markAsUnread()

        then:
        !userObj1Type1ReadInboxLog.isRead
    }

    void "test deleteInboxLog() action"() {
        setup:
        params.id = userObj1Type1ReadInboxLog.id

        when:
        controller.deleteInboxLog()

        then:
        userObj1Type1ReadInboxLog.isDeleted
    }

    void "test deleteNotificationsForUserId() action"() {
        setup:
        params.id = userObj2.id

        when:
        controller.deleteNotificationsForUserId()

        then:
        inboxLogList.count { it.inboxUserId == userObj2.id && it.isDeleted } == 0
    }

    void "test deleteNotificationById() action"() {
        setup:
        params.id = userObj1Type2ReadInboxLog.id

        when:
        controller.deleteNotificationById()

        then:
        userObj1Type2ReadInboxLog.isDeleted
    }
}
