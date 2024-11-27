package unit.com.rxlogix

import com.rxlogix.DispositionService
import com.rxlogix.InboxLog
import com.rxlogix.UserService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.enums.GroupType
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DispositionServiceSpec extends Specification implements ServiceUnitTest<DispositionService> {
    @Shared
    Disposition disposition=new Disposition(id:3,abbreviation: "C",value: "test value", displayName: "test name", validatedConfirmed: false)
    def setup() {
    }

    def cleanup() {
    }

    void "test saveDisposition for both success and failure"(){
        given:
        Disposition disposition1=new Disposition(id:1,abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(id:2,abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)
        expect:
        service.saveDisposition(instance)==result
        where:
        instance                                                                                                                |  result
        new Disposition(id:3,abbreviation: "C",value: "test value1",displayName: "test name1", validatedConfirmed: false)       |  true
        new Disposition(id:4,abbreviation: "C",value: "Requires Review",displayName: "test name2", validatedConfirmed: false)   |  false
    }
    void "test getDispositionListByDisplayName"() {
        given:
        Disposition disposition1=new Disposition(id:1,abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(id:2,abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)
        when:
        List dispositionList = service.getDispositionListByDisplayName()
        then:
        dispositionList.size() == 2
        dispositionList.get(0) == "Requires Review"
        dispositionList.get(1) == "New Potential Signal"
    }
    void "test getDispositionListByDisplayName when there is no disposition"() {
        when:
        List dispositionList = service.getDispositionListByDisplayName()
        then:
        dispositionList.size() == 0
    }
    void "test getDispositionFromDisplayName"() {
        given:
        Disposition disposition1=new Disposition(id:1,abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(id:2,abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)
        List dispositionValue = ["Requires Review"]
        when:
        List dispositionList = service.getDispositionFromDisplayName(dispositionValue)
        then:
        dispositionList.size() == 1
        dispositionList.get(0)["value"] == "Requires Review"
    }
    void "test getDispositionFromDisplayName when there is no disposition"() {
        setup:
        List dispositionValue = ["Requires Review"]
        when:
        List dispositionList = service.getDispositionFromDisplayName(dispositionValue)
        then:
        dispositionList.size() == 0
    }
    void "test getDispositionFromDisplayName when disposition value is empty"() {
        setup:
        List dispositionValue = []
        when:
        List dispositionList = service.getDispositionFromDisplayName(dispositionValue)
        then:
        dispositionList.size() == 0
    }
    void "test listDispositionAdvancedFilter action when there are values in cache"(){
        setup:
        Disposition disposition1=new Disposition(id:1,abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(id:2,abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)
        CacheService cacheService=Mock(CacheService)
        cacheService.getDispositionCacheMap()>>{
            return[1:disposition1,2:disposition2]
        }
        service.cacheService=cacheService
        when:
        List result=service.listDispositionAdvancedFilter()
        then:
        result.size()==2
        result[0].id==1
        result[0].text=="Requires Review"
        result.get(1).id==2
        result[1].text=="New Potential Signal"
    }
    void "test listDispositionAdvancedFilter action when there is no values in cache"(){
        setup:
        CacheService cacheService=Mock(CacheService)
        cacheService.getDispositionCacheMap()>>{
            return[:]
        }
        service.cacheService=cacheService
        when:
        List result=service.listDispositionAdvancedFilter()
        then:
        result.size()==0
    }
    private User createUser(String username, Group wfGroup, String authority=null) {
        User.metaClass.encodePassword = { "password" }
        Preference preference = new Preference(locale: new Locale("en"))
        User user = new User(username: username, password: 'password', fullName: username, preference: preference, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.save(failOnError: true)
        if(authority) {
            Role role = new Role(authority: authority, createdBy: 'createdBy', modifiedBy: 'modifiedBy').save(flush: true)
            UserRole.create(user, role, true)
        }
        return user
    }
    void "test listAlertDispositions action when there is some alert"(){
        setup:
        Disposition disposition1 = new Disposition(id: 1, abbreviation: "C", value: "Requires Review",
                displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush: true, failOnError: true)
        Disposition disposition2 = new Disposition(id: 2, abbreviation: "C", value: "New Potential Signal",
                displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush: true, failOnError: true)

        Group wfGroup1 = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition2)
        wfGroup1.save(flush: true, failOnError: true)

        User user1 = createUser("user1", wfGroup1)
        wfGroup1.addToAlertDispositions(disposition1)
        wfGroup1.addToAlertDispositions(disposition2)
        wfGroup1.save(flush:true,failOnError:true)
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        service.userService=userService
        when:
        List result=service.listAlertDispositions()
        then:
        result.size()==2
        result.get(0).id==1
        result.get(0).displayName=="Requires Review"
        result.get(1).id==2
        result.get(1).displayName=="New Potential Signal"
    }
    void "test listAlertDispositions when there is no alert"(){
        setup:
        Disposition disposition1=new Disposition(id:1,abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(id:2,abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)

        Group wfGroup1 = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition2)
        wfGroup1.save(flush: true, failOnError: true)

        User user1 = createUser("user1", wfGroup1)
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        service.userService=userService
        when:
        List result=service.listAlertDispositions()
        then:
        result==null
    }
    @Unroll
    void "test populateAlertLevelDispositionDTO action"(){
        setup:
        disposition.save(flush:true,failOnError:true)
        expect:
        service.populateAlertLevelDispositionDTO(a,b,c,d,e).evdasalertConfiguration==result[0]
        service.populateAlertLevelDispositionDTO(a,b,c,d,e).configId==result[1]
        service.populateAlertLevelDispositionDTO(a,b,c,d,e).execConfig==result[2]
        service.populateAlertLevelDispositionDTO(a,b,c,d,e).execConfigId==result[3]
        service.populateAlertLevelDispositionDTO(a,b,c,d,e).targetDisposition==disposition
        where:
        a           |   b               |   c          |  d                    |  e                                       ||  result
        disposition |"justificationText"| null         | null                  | null                                     || [null,null,null,null,disposition]
        disposition |"justificationText"| EvdasAlert   | null                  | new Configuration().save(validate:false) || [null,1,null,null,disposition]
    }
    void "test sendDispChangeNotification action"(){
        setup:
        Disposition disposition=new Disposition(id:1,abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition.save(flush:true,failOnError:true)
        Group wfGroup1 = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition)
        wfGroup1.save(flush:true,failOnError:true)
        User user1 = createUser("user1", wfGroup1)
        UserService userService=Mock(UserService)
        userService.user>>{
            return user1
        }
        service.userService=userService
        NotificationHelper notificationHelper=Mock(NotificationHelper)
        notificationHelper.pushNotification(_)>>{
            return true
        }
        service.notificationHelper=notificationHelper
        expect:
        service.sendDispChangeNotification(a,b)==result
        where:
        a            |    b       |     result
        disposition  |   "Serious"|     true
    }
    void "test getReviewCompletedDispositionList when there is some disposition"(){
        setup:
        Disposition disposition1=new Disposition(id:1,abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false,reviewCompleted: true)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(id:3,abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false,reviewCompleted: true)
        disposition2.save(flush:true,failOnError:true)
        Disposition disposition3=new Disposition(id:4,abbreviation: "C",value: "New Potential Signal2", displayName: "New Potential Signal2", validatedConfirmed: false)
        disposition3.save(flush:true,failOnError:true)
        when:
        List result=service.getReviewCompletedDispositionList()
        then:
        result.size()==2
        result[0]=="Requires Review"
        result[1]=="New Potential Signal"
    }
    void "test getReviewCompletedDispositionList when there is no disposition"(){
        when:
        List result=service.getReviewCompletedDispositionList()
        then:
        result.size()==0
    }
}
