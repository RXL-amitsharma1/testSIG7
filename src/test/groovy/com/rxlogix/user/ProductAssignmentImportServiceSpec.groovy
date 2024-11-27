package com.rxlogix.user

import com.rxlogix.ProductAssignmentImportService
import com.rxlogix.ProductAssignmentService
import com.rxlogix.UserService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ProdAssignmentProcessState
import com.rxlogix.signal.ProductAssignmentLog
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import groovy.transform.SourceURI
import spock.util.mop.ConfineMetaClassChanges

import java.nio.file.Path
import java.nio.file.Paths

@ConfineMetaClassChanges([ProductAssignmentImportService])
class ProductAssignmentImportServiceSpec extends HibernateSpec implements ServiceUnitTest<ProductAssignmentImportService> {

    User user
    User newUser
    Group wfGroup
    Group userGroup
    Group newUserGroup
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    Map dicMap
    ProductAssignmentLog productAssignmentLog

    File fileToRead
    File readFolder
    File uploadFolder
    File failFolder
    File logsDir

    List<Class> getDomainClasses() {
        [User, UserService, Disposition, Group, ProductAssignmentService, CacheService, ProductViewAssignment, ProductAssignmentLog]
    }

    def setup() {

        readFolder = new File(config.signal.product.assignment.import.folder.read as String)
        uploadFolder = new File(config.signal.product.assignment.import.folder.upload as String)
        failFolder = new File(config.signal.product.assignment.import.folder.fail as String)
        logsDir = new File(config.signal.product.assignment.import.folder.logs as String)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(flush: true, failOnError: true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default",
                groupType: GroupType.WORKFLOW_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)


        userGroup = new Group(name: "user_group",
                groupType: GroupType.USER_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        userGroup.save(flush: true)

        newUserGroup = new Group(name: "new_user_group",
                groupType: GroupType.USER_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        newUserGroup.save(flush: true)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

        //Save the  user
        newUser = new User(id: '2', username: 'new_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        newUser.addToGroups(wfGroup)
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.timeZone = "UTC"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.metaClass.getFullName = { ' New Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.save(flush: true)

        dicMap = [1: "Ingredient", 2: "Family", 3: "Product Name", 4: "Trade Name"]

        productAssignmentLog = new ProductAssignmentLog(importedFileName: "import-file", importedBy: user, importedDate: new Date(),
                status: ProdAssignmentProcessState.IN_READ)
        productAssignmentLog.save()

        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("ProductAssignmentControllerSpec.groovy", "import_assignment_file/upload/import-assignment.xlsx")
        fileToRead = new File(directory)

        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }

        service.transactionManager = getTransactionManager()
        service.sessionFactory_pva = sessionFactory

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> { user }
        service.userService = mockUserService

        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getUserByUserName(_) >> {
            return user
        }
        mockCacheService.getUserGroups() >> {
            return [userGroup, newUserGroup]
        }
        mockCacheService.getGroupByGroupId(_) >> {
            return wfGroup
        }
        service.cacheService = mockCacheService
    }

    void "test checkAndCreateBaseDirs"() {
        when:
        File baseFolder = new File(config.signal.product.assignment.import.folder.base as String)
        service.checkAndCreateBaseDirs()

        then:
        assert readFolder.exists()
        assert baseFolder.exists()
        assert uploadFolder.exists()
        assert failFolder.exists()
        assert logsDir.exists()
    }

    void "test checkIfFileExistsInUploadFolder"() {
        when:
        service.checkIfFileExistsInUploadFolder()

        then:
        true
    }

    void "test saveProductAssignmentLog"() {
        when:
        service.saveProductAssignmentLog('file', user)

        then:
        noExceptionThrown()
    }

    void "test generateUserOrGroupId"() {
        when:
        List userList = []
        List<Map> result = service.generateUserOrGroupId([1114], [11], userList)

        then:
        userList == ["User_1114", "UserGroup_11"]
    }

    void "test getNextFileProdAssignLog"() {
        when:
        service.getNextFileProdAssignLog()

        then:
        productAssignmentLog
    }

    void "test generateUserIdFromCache"() {
        when:
        service.generateUserIdFromCache(user.username, [user])

        then:
        user.id
    }

    void "test generateUserIdForExcel"() {
        when:
        service.generateUserIdForExcel(user.username, [user])

        then:
        user.id
    }

    void "test groupIdFromTheSystem"() {
        when:
        Long resultId = service.groupIdFromTheSystem('user_group')

        then:
        resultId == userGroup.id
    }

    void "test addUserOrGroupIdList when there is user assignment"() {
        when:
        List userIdList = []
        List groupIdList = []
        service.addUserOrGroupIdList('test_user', [user, newUser], userIdList, groupIdList)

        then:
        userIdList == [user.id]
        groupIdList == []
    }

    void "test addUserOrGroupIdList when there is group assignment"() {
        given:
        service.metaClass.userIdFromLdap = { String username -> return null }

        when:
        List userIdList = []
        List groupIdList = []
        service.addUserOrGroupIdList('user_group', [user, newUser], userIdList, groupIdList)

        then:
        userIdList == []
        groupIdList == [userGroup.id]
    }

}
