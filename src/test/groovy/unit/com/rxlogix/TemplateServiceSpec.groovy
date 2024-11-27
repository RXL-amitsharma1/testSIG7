package com.rxlogix

import com.rxlogix.config.DataTabulationTemplate
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore
class TemplateServiceSpec extends Specification implements ServiceUnitTest<TemplateService> {

    @Shared caseMasterColumnCountry
    @Shared caseInformationRFG

    def setup() {
        // Build report field data
        buildReportFields()

        ReportField querySelectableField = new ReportField(name: "for query", fieldGroup: caseInformationRFG,
                sourceColumn: caseMasterColumnCountry, dataType: String.class, querySelectable: true,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: false).save(failOnError: true)
        ReportField templateCLLSelectableField = new ReportField(name: "for query", fieldGroup: caseInformationRFG,
                sourceColumn: caseMasterColumnCountry, dataType: String.class, querySelectable: false,
                templateCLLSelectable: true, templateDTRowSelectable: false, templateDTColumnSelectable: false).save(failOnError: true)
        ReportField templateDTRowSelectableField = new ReportField(name: "for query", fieldGroup: caseInformationRFG,
                sourceColumn: caseMasterColumnCountry, dataType: String.class, querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: true, templateDTColumnSelectable: false).save(failOnError: true)
        ReportField templateDTColumnSelectableField = new ReportField(name: "for query", fieldGroup: caseInformationRFG,
                sourceColumn: caseMasterColumnCountry, dataType: String.class, querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: true).save(failOnError: true)
    }

    private User makeAdminUser() {
        def user = "unitTest"
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"))
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    void buildReportFields() {
        SourceTableMaster caseMasterTable = new SourceTableMaster(tableName: "CASE_MASTER", tableAlias: "cm", tableType: "C", caseJoinOrder: 1).save(failOnError: true)
        SourceTableMaster lmCountriesTable = new SourceTableMaster(tableName: "LM_COUNTRIES", tableAlias: "lco", tableType: "L", caseJoinOrder: null).save(failOnError: true)
        caseMasterColumnCountry = new SourceColumnMaster(tableName: caseMasterTable, columnName: "COUNTRY_ID",
                primaryKey: null, lmTableName: lmCountriesTable, lmJoinColumn: "COUNTRY_ID",
                lmDecodeColumn: "COUNTRY", columnType: "N", reportItem: "CM_COUNTRY_ID").save(failOnError: true)
        caseInformationRFG = new ReportFieldGroup(name: "Case Information")
        caseInformationRFG.id = System.currentTimeMillis()
        caseInformationRFG.save(failOnError: true)

        //Purposely leaving out listDomainClass
        ReportField countryOfIncidenceRF = new ReportField(name: "masterCountryId", fieldGroup: caseInformationRFG,
                sourceColumn: caseMasterColumnCountry, dataType: String.class, querySelectable: false,
                templateCLLSelectable: false, templateDTRowSelectable: false, templateDTColumnSelectable: false).save(failOnError: true)
    }

    def cleanup() {
    }

    void "Test getting report fields only for Case Line Listing Template"() {
        given: "Some report fields with flags in setup"

        when: "Call getReportFields"
        List fields = service.getReportFieldsForCLL()

        then: "Return only CLL selectable fields"
        ReportField.findAll().size() == 5
        fields.size() == 1
    }

    void "Test getting report fields only for Data Tabulation Template Row"() {
        given: "Some report fields with flags in setup"

        when: "Call getReportFields"
        List fields = service.getReportFieldsForDTRow()

        then: "Return only CLL selectable fields"
        ReportField.findAll().size() == 5
        fields.size() == 1
    }

    void "Test getting report fields only for Data Tabulation Template Column"() {
        given: "Some report fields with flags in setup"

        when: "Call getReportFields"
        List fields = service.getReportFieldsForDTColumn()

        then: "Return only CLL selectable fields"
        ReportField.findAll().size() == 5
        fields.size() == 1
    }

    def "Create measures from a JSON String" () {
        given: "A valid json string for measures has 2 measures json object"
            def measuresJson = """[{"name":"Case Count","type":"CASE_COUNT","count":"PERIOD_COUNT","percentage":"NO_PERCENTAGE",
                                "customExpression":"","showTotal":false},{"name":"Event Count","type":"EVENT_COUNT",
                                "count":"PERIOD_COUNT","percentage":"NO_PERCENTAGE","customExpression":"","showTotal":false}]"""
            def measures
        when: "calling createMeasureListFromJson and passing the json string"
            measures = service.createMeasureListFromJson(new DataTabulationTemplate(), measuresJson)
        then: "the list of measure's size should equals to 2"
            measures.size == 2
    }

    def "Return empty list when measuresJSON is null" () {
        given: "An empty string for measures"
            def measuresJson = ""
            def measures

        when: "calling createMeasureListFromJson and passing the empty string"
            measures = service.createMeasureListFromJson(new DataTabulationTemplate(), measuresJson)

        then: "should return null"
            measures == null
    }
}
