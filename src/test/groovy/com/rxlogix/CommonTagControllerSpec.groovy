package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import grails.converters.JSON
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
//@TestFor(CommonTagController)
//@Mock([CommonTagService, LiteratureAlert,ExecutedConfiguration, SingleCaseAlert, ArchivedSingleCaseAlert, SingleOnDemandAlert])
//@TestMixin(DomainClassUnitTestMixin)
class CommonTagControllerSpec extends Specification implements ControllerUnitTest<CommonTagController> {

    def setup() {
        LiteratureAlert aca = new LiteratureAlert(id: 1L, articleId: 101)
        aca.save(validate: false)
        ExecutedConfiguration exConfig = new ExecutedConfiguration(id: 1, pvrCaseSeriesId: 1234)
        exConfig.save(validate: false)
        SingleCaseAlert sca = new SingleCaseAlert(id: 1, caseId: 5678, executedAlertConfiguration: exConfig).save(validate: false)
        SingleCaseAlert sca1 = new SingleCaseAlert(id: 2, caseId: 5678, executedAlertConfiguration: exConfig).save(validate: false)
        ArchivedSingleCaseAlert aga = new ArchivedSingleCaseAlert(id: 1, caseId: 5678, executedAlertConfiguration: exConfig).save(validate: false)
        SingleOnDemandAlert soda = new SingleOnDemandAlert(id: 1, caseId: 5678, executedAlertConfiguration: exConfig).save(validate: false)
    }

    def cleanup() {
    }

    void "test getQuanAlertCategories"() {
        setup:
        controller.params.alertId = "903"
        List<Map> categories = [[id:1]]
        CommonTagService commonTagService = Mock(CommonTagService)
        commonTagService.getQuanAlertCategories(_) >> categories
        controller.commonTagService = commonTagService
        when:
        controller.getQuanAlertCategories()
        then:
        response.status == 200
    }

    void "test getQualAlertCategories"() {
        setup:
        controller.params.alertId = "1"
        List<Map> categories = [[id:1]]
        CommonTagService commonTagService = Mock(CommonTagService)
        commonTagService.getQualAlertCategories(_) >> categories
        controller.commonTagService = commonTagService
        when:
        controller.getQualAlertCategories()
        then:
        response.status == 200
    }

    void "test getLitAlertCategories"() {
        setup:
        controller.params.alertId = "1"
        List<Map> categories = [[id:1]]
        CommonTagService commonTagService = Mock(CommonTagService)
        commonTagService.getLitAlertCategories(_) >> categories
        controller.commonTagService = commonTagService
        when:
        controller.getLitAlertCategories()
        then:
        response.json == [[id:1]]
    }

    void "test commonTagDetails"() {
        setup:
        controller.commonTagService = [getCommonTags: { -> []} ]
        when:
        controller.commonTagDetails()
        then:
        response.status == 200
    }

    void "test saveAlertCategories"() {
        setup:
        request.JSON.alertId = 903L
        request.JSON.type = "Quantitative"
        request.JSON.existingRows = '[{"0": {"alert": false, "category": {"id": 21, "name": "parent_cat_5"}, "createdBy": "AK", "createdDate": null, "private": 0,"subcategory": {"id": 22, "name": "child_cat_5"}}}]'
        request.JSON.newRows = '[{"0": {"alert": false, "category": {"id": 21, "name": "parent_cat_5"}, "createdBy": "AK", "createdDate": null, "private": 0,"subcategory": {"id": 22, "name": "child_cat_5"}}}]'
        def mockService = Mock(CommonTagService)
        mockService.saveQuanAlertCategories(_,_,_,_) >> {
            return [alertId : 903L]
        }
        controller.commonTagService = mockService
        when:
        controller.saveAlertCategories()
        then:
        response.status == 200
    }

    @Unroll
    void "test fetchCategoriesByVersion"() {
        given:
        params.alertId = 1l
        params.domain = domainType
        params.prevAlertId = prevAlertId
        params.isPrevAlertArchived = isPrevArchivedBool
        params.caseVersion = 1
        params.isArchived = isArchivedBool
        def mockCommonTagService = Mock(CommonTagService)
        mockCommonTagService.getCategories(_) >> {
            return []
        }
        controller.commonTagService = mockCommonTagService
        when:
        controller.fetchCategoriesByVersion()
        then:
        response.status == 200
        response.json == []
        where:
        domainType                                      | isArchivedBool | prevAlertId | isPrevArchivedBool
        Constants.AlertConfigType.INDIVIDUAL_ON_DEMAND  | 'false'        | null        | 'true'
        Constants.AlertConfigType.INDIVIDUAL_CASE_ALERT | 'false'        | null        | 'true'
        Constants.AlertConfigType.INDIVIDUAL_CASE_ALERT | 'true'         | '2'         | 'false'
        Constants.AlertConfigType.INDIVIDUAL_CASE_ALERT | 'false'        | '1'         | 'true'
    }
    void "test fetchCategoriesMapByCase"() {
        given:
        params.isAdhoc = false
        params.archived = false
        def mockCaseInfoService = Mock(CaseInfoService)
        mockCaseInfoService.getGobalAndAlertSpecificCategoriesList(_,_,_,_) >> {
            return [categoryList: [result:  'data']]
        }
        controller.caseInfoService = mockCaseInfoService
        when:
        controller.fetchCategoriesMapByCase()
        then:
        response.status == 200
        response.json == [result: 'data']
    }

    void "test fetchCommonCategories"() {
        given:
        def mockCommonTagService = Mock(CommonTagService)
        mockCommonTagService.fetchCommonCategories(_) >> {
            return [result: 'data']
        }
        controller.commonTagService = mockCommonTagService
        when:
        controller.fetchCommonCategories()
        then:
        response.status == 200
        response.json == [result: 'data']
    }

    void "test bulkUpdateCategory"() {
        given:
        request.json = ''
        def mockCommonTagService = Mock(CommonTagService)
        mockCommonTagService.saveCommonCategories(_) >> {
            return [result: 'data']
        }
        controller.commonTagService = mockCommonTagService
        when:
        controller.bulkUpdateCategory()
        then:
        response.status == 200
        response.json == [result: 'data']
    }

}
