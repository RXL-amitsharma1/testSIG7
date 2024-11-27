package com.rxlogix.config

import com.rxlogix.enums.TemplateTypeEnum
import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@Ignore
class ReportTemplateSpec extends Specification implements DomainUnitTest<ReportTemplate> {

    def setup() {
    }

    def cleanup() {
    }

    void "Test sortInfo() method for Custom SQL Template" () {
        given:"A custom sql template"
        CustomSQLTemplate customSQLTemplate = new CustomSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL)

        when:"call sortInfo()"
        List sortInfo = customSQLTemplate.sortInfo()

        then:"return empty list"
        sortInfo == []
    }

    void "Test sortInfo() method for Non Case SQL Template" () {
        given:"A non case sql template"
        NonCaseSQLTemplate nonCaseSQLTemplate = new NonCaseSQLTemplate(templateType: TemplateTypeEnum.NON_CASE)

        when:"call sortInfo()"
        List sortInfo = nonCaseSQLTemplate.sortInfo()

        then:"return empty list"
        sortInfo == []
    }

    void "Test sortInfo() method for Case Line Listing Template" () {
        given:"A case line listing template with sort order"
        ReportFieldInfo reportFieldInfo1 = new ReportFieldInfo(sortLevel: 2, sort: Sort.ASCENDING)
        ReportFieldInfo reportFieldInfo2 = new ReportFieldInfo(sortLevel: 0)
        ReportFieldInfo reportFieldInfo3 = new ReportFieldInfo(sortLevel: 1, sort: Sort.DESCENDING)
        ReportFieldInfo reportFieldInfo4 = new ReportFieldInfo(sortLevel: 0)
        CaseLineListingTemplate caseLineListingTemplate = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE,
                columnList: new ReportFieldInfoList(reportFieldInfoList: [reportFieldInfo1, reportFieldInfo2, reportFieldInfo3, reportFieldInfo4]))

        when:"call sortInfo()"
        List sortInfo = caseLineListingTemplate.sortInfo()

        then:"return sort order info"
        sortInfo == [[2, "desc"], [0, "asc"]]
    }

    void "Test sortInfo() method for Data Tabulation Template" () {
        given:"A data tabulation template with sort order"
        ReportFieldInfo reportFieldInfo1 = new ReportFieldInfo(sortLevel: 2, sort: Sort.ASCENDING)
        ReportFieldInfo reportFieldInfo2 = new ReportFieldInfo(sortLevel: 0)
        ReportFieldInfo reportFieldInfo3 = new ReportFieldInfo(sortLevel: 1, sort: Sort.DESCENDING)
        ReportFieldInfo reportFieldInfo4 = new ReportFieldInfo(sortLevel: 0)
        DataTabulationTemplate dataTabulationTemplate = new DataTabulationTemplate(templateType: TemplateTypeEnum.DATA_TAB,
                rowList: new ReportFieldInfoList(reportFieldInfoList: [reportFieldInfo3, reportFieldInfo4]))

        DataTabulationColumnMeasure columnMeasure = new DataTabulationColumnMeasure(columnList: new ReportFieldInfoList(reportFieldInfoList: [reportFieldInfo1, reportFieldInfo2]))
        dataTabulationTemplate.columnMeasureList = [columnMeasure]

        when:"call sortInfo()"
        List sortInfo = dataTabulationTemplate.sortInfo()

        then:"return sort order info"
        sortInfo == [[2, "desc"], [0, "asc"]]
    }
}
