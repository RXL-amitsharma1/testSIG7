package com.rxlogix

import com.rxlogix.enums.DrugClassificationTypeEnum
import com.rxlogix.signal.DrugClassification
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
//@Mock([DrugClassification])
//@TestFor(DrugClassificationController)
//@TestMixin(GrailsUnitTestMixin)
@Ignore
class DrugClassificationControllerSpec extends Specification implements ControllerUnitTest<DrugClassificationController> {

    DrugClassification drugClassification_b
    DrugClassification drugClassification_c

    def setup() {
        drugClassification_b = new DrugClassification(productDictionarySelection: '{"1":[],"2":[],"3":[{"name":"product_a","id":"100062"}],"4":[],"5":[]}',
                className: 'test_className_b',
                classification: 'test_classification_b',
                classificationType: 'DRUG',
                product: 'Product',
                productIds: '100062',
                productNames: 'product_a')
        drugClassification_b.save(flush: true)

        drugClassification_c = new DrugClassification(productDictionarySelection: '{"1":[],"2":[],"3":[{"name":"product_a","id":"100062"}],"4":[],"5":[]}',
                className: 'test_className_c',
                classification: 'test_classification_c',
                classificationType: 'DRUG',
                product: 'Product',
                productIds: '100062',
                productNames: 'product_a')
        drugClassification_c.save(flush: true)
    }

    def cleanup() {
    }

    void "test index"() {
        when:
        def result = controller.index()
        then:
        result.classificationTypeEnums == [DrugClassificationTypeEnum.DRUG, DrugClassificationTypeEnum.DRUG.SUBS, DrugClassificationTypeEnum.ATC]
    }

    void "test save"() {
        setup:
        controller.params.productSelection = '{"1":[],"2":[],"3":[{"name":"product_a","id":"100062"}],"4":[],"5":[]}'
        controller.params.className = 'className_a'
        controller.params.classification = ''
        controller.params.classificationList = '[test_a, test_b]'
        controller.params.classificationType = 'DRUG'

        controller.drugClassificationService = [save: { return null }]

        when:
        controller.save()

        then:
        response.json.status == true
        response.json.message == "Drug Classification created"
    }

    void "test save error scenario"() {
        setup:
        controller.params.productSelection = ''
        controller.params.className = ''
        controller.params.classification = 'classification_a'
        controller.params.classificationType = 'DRUG'

        controller.drugClassificationService = [save: { return null }]

        when:
        controller.save()

        then:
        response.json.status == false
        response.json.message == "Please fill all the required fields"
    }

    void "test update"() {
        setup:
        controller.params.id = '1'
        controller.params.productSelection = '{"1":[],"2":[],"3":[{"name":"product_a","id":"100062"}],"4":[],"5":[]}'
        controller.params.className = 'test_className_b_edit'
        controller.params.classification = 'test_classification_b'
        controller.params.classificationType = 'DRUG'

        controller.drugClassificationService = [update: { drugClassification, valueMap -> return null }]

        when:
        controller.update()

        then:
        response.json.status == true
        response.json.message == "Drug Classification updated"
    }

    void "test update error scenario"() {
        setup:
        controller.params.id = '1'
        controller.params.productSelection = ''
        controller.params.className = ''
        controller.params.classification = 'test_classification_b'
        controller.params.classificationType = 'DRUG'

        controller.drugClassificationService = [update: { drugClassification, valueMap -> return null }]

        when:
        controller.update()

        then:
        response.json.status == false
        response.json.message == "Please fill all the required fields"
    }

    void "test delete"() {
        setup:
        controller.params.id = '1'

        controller.drugClassificationService = [delete: { drugClassification -> return null }]

        when:
        controller.delete()

        then:
        response.json.status == true
        response.json.message == "Drug Classification deleted"
    }

    void "test delete error scenario"() {
        setup:
        controller.params.id = ''

        controller.drugClassificationService = [delete: { a, b -> return null }]

        when:
        controller.delete()

        then:
        notThrown(Exception)
        response.json.status == false
        response.json.message == "Drug Classification could not be deleted"
    }

    void "test prepareValueMap"() {
        setup:
        def param = [productSelection  : '{"1":[],"2":[],"3":[{"name":"product_a","id":"100062"}, {"name":"product_b","id":"100040"}],"4":[],"5":[]}',
                     className         : 'className_a',
                     classification    : 'classification_a',
                     classificationType: 'DRUG']
        when:
        def resultMap = controller.prepareValueMap(param, param.classification)
        then:
        resultMap.productDictionarySelection == '{"1":[],"2":[],"3":[{"name":"product_a","id":"100062"}, {"name":"product_b","id":"100040"}],"4":[],"5":[]}'
        resultMap.className == 'className_a'
        resultMap.classification == 'classification_a'
        resultMap.classificationType == 'DRUG'
        resultMap.product == 'Product'
        resultMap.productIds == '100062,100040'
        resultMap.productNames == 'product_a,product_b'
    }

    void "test list"() {
        when:
        controller.list()
        then:
        controller.response.json[0].product == 'Product'
        controller.response.json[0].classificationType == 'Product'
        controller.response.json[0].className == 'test_className_b'
        controller.response.json[0].id == 1
        controller.response.json[0].classification == 'test_classification_b'
        controller.response.json[1].product == 'Product'
        controller.response.json[1].classificationType == 'Product'
        controller.response.json[1].className == 'test_className_c'
        controller.response.json[1].id == 2
        controller.response.json[1].classification == 'test_classification_c'
    }

    void "test fetchDrugClassification"() {
        setup:
        controller.params.id = '1'
        when:
        controller.fetchDrugClassification()
        then:
        controller.response.json.success == true
        controller.response.json.valMap.id == 1
        controller.response.json.valMap.productSelection == '{"1":[],"2":[],"3":[{"name":"product_a","id":"100062"}],"4":[],"5":[]}'
        controller.response.json.valMap.className == 'test_className_b'
        controller.response.json.valMap.classification == 'test_classification_b'
        controller.response.json.valMap.classificationType == 'DRUG'
    }

    void "test fetchDrugClassification error scenario"() {
        setup:
        controller.params.id = '123'
        when:
        controller.fetchDrugClassification()
        then:
        notThrown(Exception)
        controller.response.json.success == false
    }
}
