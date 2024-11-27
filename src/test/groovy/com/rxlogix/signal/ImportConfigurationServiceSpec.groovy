package com.rxlogix.signal

import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.UserService
import com.rxlogix.config.AlertDateRangeInformation
import com.rxlogix.config.Category
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportTemplate
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ImportConfigurationProcessState
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import groovy.transform.SourceURI
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class ImportConfigurationServiceSpec extends Specification implements ServiceUnitTest<ImportConfigurationService> {

    User user
    Group wfGroup
    UserService userService
    Disposition disposition
    Configuration alertConfiguration
    Priority priority1
    CRUDService crudService
    File file
    String directory
    ImportConfigurationLog importConfigurationLog

    def setup() {

        disposition = new Disposition(value: "ValidatedSignal1", displayName: "Validated Signal1", validatedConfirmed: true,
                abbreviation: "C")
        disposition.save(failOnError: true)

        wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"
        user.groups = [wfGroup]
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        AlertDateRangeInformation alertDateRangeInformation1 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUSTOM)
        Category category1 = new Category(name: "category1")
        category1.save(flush: true, failOnError: true)
        ReportTemplate reportTemplate1 = new ReportTemplate(name: "repTemp1", description: "repDesc1", category: category1,
                owner: user, templateType: TemplateTypeEnum.TEMPLATE_SET, dateCreated: new Date(), lastUpdated: new Date() + 4,
                createdBy: "username", modifiedBy: "username")
        reportTemplate1.save(flush: true, failOnError: true)
        priority1 = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority1.save(flush: true, failOnError: true)

        alertConfiguration = new Configuration(
                productSelection: '{"3":[{"name":"product1"}]}',
                executing: false,
                template: reportTemplate1,
                priority: priority1,
                alertTriggerCases: "11",
                alertTriggerDays: "11",
                dateCreated: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "test",
                assignedTo: user,
                createdBy: "username",
                modifiedBy: "username",
                owner: user,
                adhocRun: true,
                alertQueryId: 1L,
                alertQueryName: "AlertQuery1",
                alertDateRangeInformation: alertDateRangeInformation1,
        )
        alertConfiguration.metaClass.getProductType = { 'family' }
        alertConfiguration.save(flush: true, failOnError: true)

        importConfigurationLog = new ImportConfigurationLog(importFileName: "1614580907697.xlsx" , importedBy: user, importedDate: new Date(), status: ImportConfigurationProcessState.IN_READ, importConfType: Constants.AlertConfigType.SINGLE_CASE_ALERT)
        importConfigurationLog.save(validate:false)

        userService = Mock(UserService)
        userService.getUser() >> {
            return user
        }
        service.userService = userService

        crudService = Mock(CRUDService)
        crudService.save(_) >> alertConfiguration
        service.CRUDService = crudService

        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        directory = scriptLocation.toString().replace("ImportConfigurationServiceSpec.groovy", "testingFiles/1614580907697.xlsx")
        file=new File(directory)

    }

    def cleanup() {
    }

    void "test checkAndCreateBaseDirs"() {
        when:
        service.checkAndCreateBaseDirs()
        then:
        File.collect().size() == 1
    }
    void "test fetchAlertListByType"(){
        setup:
        Map resultMap = [
                aaData : [],
                recordsTotal : 0,
                recordsFiltered :0
        ]
        service.metaClass.resultMapData = { List alertResultList ->
            return ["result"]
        }
        when:
        def result = service.fetchAlertListByType(resultMap, Constants.AlertConfigType.SINGLE_CASE_ALERT)
        then:
        result.size() == 3
    }
    void "test resultMapData"(){
        when:
        def result = service.resultMapData([alertConfiguration])
        then:
        result.size() == 1
    }
    void "test createAlertFromTemplate"(){
        when:
        def result = service.createAlertFromTemplate(Constants.AlertConfigType.SINGLE_CASE_ALERT, alertConfiguration, user, false)
        then:
        assert result != null
    }
    void "test updateDateRange"(){
        when:
        def result = service.updateDateRange([dateRangeEnum: DateRangeEnum.CUSTOM.name()], alertConfiguration)
        then:
        assert result.id == 1L
    }
    void "test updateScheduleDateAndNextRunDate()"(){
        when:
        def result = service.updateScheduleDateAndNextRunDate([scheduleDateJSON:"{}", repeatExecution: "false"], alertConfiguration)
        then:
        result.id == 1L
    }
    void "test getSortedListData"(){
        when:
        def result = service.getSortedListData([sortedCol: '3', alertType: Constants.AlertConfigType.SINGLE_CASE_ALERT])
        then:
        result.size() == 0
    }
    void "test checkFileFormat"(){
        when:
        def result = service.checkFileFormat(file)
        then:
        result == false
    }
    void "test saveImportConfigurationLog"(){
        when:
        def result = service.saveImportConfigurationLog("filename", user, "importConfType")
        then:
        assert result.id == 2L
    }
    void "test createDir"(){
        when:
        def result = service.createDir(directory)
        then:
        result == null
    }
    void "test startProcessingUploadedFile"(){
        setup:
        service.metaClass.updateImportConfigurationLog = { ImportConfigurationLog importConfigurationLog1, ImportConfigurationProcessState state1 ->

        }
        service.metaClass.moveFile = { File file1, String destination ->
            return file
        }
        when:
        def result = service.startProcessingUploadedFile()
        then:
        result == null
    }
}
