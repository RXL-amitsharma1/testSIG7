package com.rxlogix.signal

import com.rxlogix.CRUDService
import com.rxlogix.ConfigurationService
import com.rxlogix.Constants
import com.rxlogix.DynamicReportService
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
import com.rxlogix.user.UserGroupMapping
import grails.testing.web.controllers.ControllerUnitTest
import com.rxlogix.util.AuditLogConfigUtil
import groovy.transform.SourceURI
import org.grails.plugins.testing.GrailsMockMultipartFile
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class ImportConfigurationControllerSpec extends Specification implements ControllerUnitTest<ImportConfigurationController> {

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
    ImportConfigurationService importConfigurationService
    DynamicReportService dynamicReportServiceMocked
    UserGroupMapping userGroupMapping


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
        user.preference.locale = Locale.ENGLISH
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
                type: Constants.AlertConfigType.SINGLE_CASE_ALERT,
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
                isTemplateAlert: true,
                isDeleted: false,
                scheduleDateJSON: '{}'
        )
        alertConfiguration.metaClass.getProductType = { 'family' }
        alertConfiguration.save(flush: true, failOnError: true)

        importConfigurationLog = new ImportConfigurationLog(importFileName: "1614580907697.xlsx" , importedBy: user, importedDate: new Date(), status: ImportConfigurationProcessState.IN_READ, importConfType: Constants.AlertConfigType.SINGLE_CASE_ALERT)
        importConfigurationLog.save(validate:false)

        userService = Mock(UserService)
        userService.getUser() >> {
            return user
        }
        userService.assignGroupOrAssignTo(_,_,_)>>{
            return alertConfiguration
        }
        userService.user >>{
            return user
        }
        userService.bindSharedWithConfiguration(_, _, _, _) >> null
        controller.userService = userService

        crudService = Mock(CRUDService)
        crudService.save(_) >> alertConfiguration
        controller.CRUDService = crudService

        importConfigurationService = Mock(ImportConfigurationService)
        importConfigurationService.fetchAlertListByType(_,_)>>{
            return [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        }
        importConfigurationService.updateScheduleDateAndNextRunDate(_,_)>>{

        }
        importConfigurationService.getSortedListData(_)>>{
            return []
        }
        controller.importConfigurationService = importConfigurationService
        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        directory = scriptLocation.toString().replace("ImportConfigurationServiceSpec.groovy", "testingFiles/1614580907697.xlsx")
        file=new File(directory)

        dynamicReportServiceMocked = Mock(DynamicReportService)
        dynamicReportServiceMocked.exportToExcelImportConfigurationList(_,_) >> {
            return file
        }
        controller.dynamicReportService = dynamicReportServiceMocked
        GroovyMock(AuditLogConfigUtil, global: true)
    }

    def cleanup() {
    }

    void "test fetchAlertList"() {
        when:
        controller.fetchAlertList(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        then:
        response.status == 200
    }
    void "test fetchAlertTemplateByType"(){
        when:
        def result = controller.fetchAlertTemplateByType(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        then:
        response.status == 200
    }
    void "test createAlertFromTemplate"(){
        when:
        controller.createAlertFromTemplate(Constants.AlertConfigType.SINGLE_CASE_ALERT, 1L)
        then:
        response .status == 200
    }
    void "test editAlertName"(){
        when:
        controller.editAlertName("alertName", 1L)
        then:
        response.json.code == 200
        response.json.message == "Alert name Changed successfully"
    }
    void "test updateDateRangeForAlert"(){
        setup:
        controller.params.id = 1L
        controller.params.dateRangeEnum = DateRangeEnum.CUSTOM.name()
        when:
        controller.updateDateRangeForAlert()
        then:
        response.json.status == true
        response.json.message == "Alert Date Range updated successfully"
    }
    void "test updateDateRangeForAlert fail"(){
        when:
        controller.updateDateRangeForAlert()
        then:
        response.json.status == false
        response.json.message == "Alert does not exists."
    }
    void "test updateScheduleDateJSON"(){
        setup:
        controller.params.id = 1L
        when:
        controller.updateScheduleDateJSON()
        then:
        response.json.status == true
        response.json.message == "Alert updated successfully"
    }
    void "test updateScheduleDateJSON fail"(){
        when:
        controller.updateScheduleDateJSON()
        then:
        response.json.status == false
        response.json.message == "Alert does not exists."
    }
    void "test unScheduleAlert"(){
        setup:
        controller.params.id = 1L
        when:
        controller.unScheduleAlert()
        then:
        response.json.status == true
        response.json.message == "Alert Unscheduled Successfully!"
    }
    void "test unScheduleAlert fail"(){
        when:
        controller.unScheduleAlert()
        then:
        response.json.status == false
        response.json.message == "Alert does not exists."
    }
    void "test changeAssignedToGroup"(){
        when:
        controller.changeAssignedToGroup(1L, "assignToValue")
        then:
        response.json.status == true
    }
    void "test updateShareWithForConf Exception"(){
        when:
        controller.updateShareWithForConf(1L)
        then:
        response.json.status == false
    }
    void "test uploadFile incorrect file format"(){
        given:
        def file = new GrailsMockMultipartFile('file', 'someData'.bytes)
        request.addFile(file)
        when:
        controller.uploadFile()
        then:
        response.json.status == false
        response.json.message == "app.label.Configuration.upload.file.format.incorrect"
    }
    void "test uploadFile"(){
        given:
        importConfigurationService.checkFileFormat(_) >> {
            return true
        }
        importConfigurationService.saveImportConfigurationLog(_,_,_)>>{
            return importConfigurationLog
        }
        importConfigurationService.createDir(_)>>{

        }
        importConfigurationService.copyFile(_,_)>>{

        }
        controller.importConfigurationService = importConfigurationService
        def file = new GrailsMockMultipartFile('file', 'someData'.bytes)
        request.addFile(file)
        when:
        controller.uploadFile()
        then:
        response.json.status == true
        response.json.message == "app.label.Configuration.upload.inprogress"
    }
    void "test fetchImportConfigurationLog"(){
        when:
        controller.fetchImportConfigurationLog()
        then:
        response.json.importLogList.size() == 1
    }
    void "test renderImportConfigurationOutputType"(){
        when:
        controller.renderImportConfigurationOutputType(file, "1614580907697")
        then:
        response.contentType == "groovy charset=UTF-8"
    }
    void "test deleteAlertConfig failure"(){
        given:
        controller.params.id = 1L
        when:
        controller.deleteAlertConfig()
        then:
        response.json.status == false
        response.json.message == "app.common.error"
    }
    void "test deleteAlertConfig"(){
        given:
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.deleteConfig(_) >>{
            return alertConfiguration
        }
        controller.configurationService = configurationService
        controller.params.id = 1L
        user.metaClass.isAdmin = { true }
        when:
        controller.deleteAlertConfig()
        then:
        response.json.status == true
        response.json.message == "app.configuration.delete.success"
    }
    void "test updateProdSelection"(){
        given:
        controller.params.alertId = 1L
        controller.params.productSelection = "{}"
        when:
        controller.updateProdSelection()
        then:
        response.status == 200
        response.json.message == "app.configuration.update.success"
    }

    def "test setWhoDrug with productSelection containing drug record data"() {
        given:
        alertConfiguration.productSelection = '{"1":[{"name":"PARACETAMOL","id":"829189359","isMultiIngredient":true},{"name":"PARACETAMOL/ DICLOFENAC","id":"1513200401","isMultiIngredient":true},{"name":"PARATYPHOID VACCINE A/B","id":"1738071836","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[{"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"},{"name":"PARATYPHOID VACCINE A/B","id":"075540","isMultiIngredient":true,"drugRecordNumber":"075540"},{"name":"DICLOFENAC/ PARACETAMOL","id":"138301","isMultiIngredient":true,"drugRecordNumber":"138301"}]}'

        when:
        controller.setWhoDrug(alertConfiguration)

        then:
        alertConfiguration.includeWHODrugs == true
    }

    def "test setWhoDrug with productSelection not containing drug record data"() {
        given:
        alertConfiguration.productSelection = '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":false}],"2":[],"3":[],"4":[],"100":[]}'

        when:
        controller.setWhoDrug(alertConfiguration)

        then:
        alertConfiguration.includeWHODrugs == false
    }

    def "test setWhoDrug with productGroupSelection containing relevant data"() {
        given:
        alertConfiguration.productGroupSelection = '[{"name":"test WHO PG 1145 (380630)","id":380630,"isMultiIngredient":true,"includeWHODrugs":true}]'
        alertConfiguration.productSelection = ''

        when:
        controller.setWhoDrug(alertConfiguration)

        then:
        alertConfiguration.includeWHODrugs == true
    }

    def "test setWhoDrug with productGroupSelection not containing relevant data"() {
        given:
        alertConfiguration.productGroupSelection = '[{"name":"test WHO PG 1145 (380630)","id":380630,"isMultiIngredient":true,"includeWHODrugs":false}]'
        alertConfiguration.productSelection = ''

        when:
        controller.setWhoDrug(alertConfiguration)

        then:
        alertConfiguration.includeWHODrugs == false
    }

    def "test getAllDrugRecNumberFromJson with valid data containing drug record"() {
        given:
        String productSelection = '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[{"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"}]}'

        when:
        String result = controller.getAllDrugRecNumberFromJson(productSelection)

        then:
        result == "000200 (PARACETAMOL)"
    }

    def "test getAllDrugRecNumberFromJson with valid data not containing drug record"() {
        given:
        String productSelection = '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[]}'

        when:
        String result = controller.getAllDrugRecNumberFromJson(productSelection)

        then:
        result == ""
    }

    def "test getAllDrugRecNumberFromJson with empty string"() {
        given:
        String productSelection = ""

        when:
        String result = controller.getAllDrugRecNumberFromJson(productSelection)

        then:
        result == ""
    }

    def "test getDrugNameFieldFromJson with valid data containing drug number"() {
        given:
        def productSelection = '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[{"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"}]}'

        when:
        def result = controller.getDrugNameFieldFromJson(productSelection)

        then:
        result == "PARACETAMOL"
    }

    def "test getDrugNameFieldFromJson with valid data not containing drug number"() {
        given:
        def productSelection = '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[]}'

        when:
        def result = controller.getDrugNameFieldFromJson(productSelection)

        then:
        result == ""
    }

    def "test getDrugNameFieldFromJson with empty string"() {
        given:
        def productSelection = ""

        when:
        def result = controller.getDrugNameFieldFromJson(productSelection)

        then:
        result == ""
    }

    def "test getDrugIdFieldFromJson with valid data containing drug record"() {
        given:
        def productSelection = '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[{"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"}]}'

        when:
        def result = controller.getDrugIdFieldFromJson(productSelection)

        then:
        result == "000200"
    }

    def "test getDrugIdFieldFromJson with valid data not containing drug record"() {
        given:
        def productSelection = '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[]}'

        when:
        def result = controller.getDrugIdFieldFromJson(productSelection)

        then:
        result == ""
    }

    def "test getDrugIdFieldFromJson with empty string"() {
        given:
        def productSelection = ""

        when:
        def result = controller.getDrugIdFieldFromJson(productSelection)

        then:
        result == ""
    }

    def "test checkProductGroupName with matching group names"() {
        given:
        String paramsProductGroupSelection = '{"name":"Product Group A","id","1031050","isMultiIngredient:"true",includeWHODrugs:"false"}'
        String configProductGroupSelection = '{"name":"Product Group A","id","1031050","isMultiIngredient:"true",includeWHODrugs:"true"}'

        // Mock the behavior of getGroupNameFieldFromJsonForAudit
        AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(paramsProductGroupSelection) >> "Product Group A"
        AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(configProductGroupSelection) >> "Product Group A"

        when:
        Boolean result = controller.checkProductGroupName(paramsProductGroupSelection, configProductGroupSelection)

        then:
        result == true
    }

    def "test checkProductGroupName with different group names"() {
        given:
        String paramsProductGroupSelection = '{"name":"Product Group A","id","1031050","isMultiIngredient:"true",includeWHODrugs:"false"}'
        String configProductGroupSelection = '{"name":"Product Group B","id","1031050","isMultiIngredient:"true",includeWHODrugs:"true"}'

        // Mock the behavior of getGroupNameFieldFromJsonForAudit
        AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(paramsProductGroupSelection) >> "Product Group A"
        AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(configProductGroupSelection) >> "Product Group B"

        when:
        Boolean result = controller.checkProductGroupName(paramsProductGroupSelection, configProductGroupSelection)

        then:
        result == false
    }

    def "test checkProductSelectionValues with matching product names"() {
        given:

        String paramsProductSelection = '{"name":"PARACETAMOL","id":"6363","isMultiIngredient","false"}'
        String configProductSelection = '{"name":"PARACETAMOL","id":"6363","isMultiIngredient","false"}'

        // Mock the behavior of getProductSelectionValuesForAudit
        AuditLogConfigUtil.getProductSelectionValuesForAudit(paramsProductSelection) >> "PARACETAMOL"
        AuditLogConfigUtil.getProductSelectionValuesForAudit(configProductSelection) >> "PARACETAMOL"

        when:
        Boolean result = controller.checkProductSelectionValues(paramsProductSelection, configProductSelection)

        then:
        result == true
    }

    def "test checkProductSelectionValues with different product names"() {
        given:
        String paramsProductSelection = '{"name":"PARACETAMOL","id":"6363","isMultiIngredient","false"}'
        String configProductSelection = '{"name":"IBUPROFEN","id":"6363","isMultiIngredient","false"}'

        AuditLogConfigUtil.getProductSelectionValuesForAudit(paramsProductSelection) >> "PARACETAMOL"
        AuditLogConfigUtil.getProductSelectionValuesForAudit(configProductSelection) >> "IBUPROFEN"

        when:
        Boolean result = controller.checkProductSelectionValues(paramsProductSelection, configProductSelection)

        then:
        result == false
    }


}
