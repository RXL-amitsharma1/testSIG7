package com.rxlogix

import com.rxlogix.signal.DrugClassification
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
//@Mock([DrugClassification])
//@TestFor(DrugClassificationService)
//@TestMixin(GrailsUnitTestMixin)
@Ignore
class DrugClassificationServiceSpec extends Specification implements ServiceUnitTest<DrugClassificationService> {

    DrugClassification drugClassification_b

    def setup() {
        drugClassification_b = new DrugClassification(productDictionarySelection: 'test_product_selection_b',
                className: 'test_className_b',
                classification: 'test_classification_b',
                classificationType: 'SUBS',
                product: 'test_product_b',
                productIds: 'test_product_ids_b',
                productNames: 'test_product_names_b')
        drugClassification_b.save(flush: true)
    }

    def cleanup() {
    }

    void "test save"() {
        setup:
        def paramsMap_a = [
                productDictionarySelection: 'test_product_selection_a',
                className                 : 'test_className_a',
                classification            : 'test_classification_a',
                classificationType        : 'DRUG',
                product                   : 'test_product_a',
                productIds                : 'test_product_ids_a',
                productNames              : 'test_product_names_a'
        ]

        when:
        service.save(paramsMap_a)

        then:
        DrugClassification.countByProductDictionarySelection("test_product_selection_a") == 1
        DrugClassification.countByClassName("test_className_a") == 1
        DrugClassification.countByClassification("test_classification_a") == 1
        DrugClassification.countByClassificationType('DRUG') == 1
        DrugClassification.countByProductIds("test_product_ids_a") == 1
        DrugClassification.countByProductNames("test_product_names_a") == 1
    }

    void "test update"() {
        setup:
        def paramsMap_b_edit = [
                productDictionarySelection: 'test_product_selection_b',
                className                 : 'test_className_b_edit',
                classification            : 'test_classification_b',
                classificationType        : 'SUBS',
                product                   : 'test_product_b',
                productIds                : 'test_product_ids_b',
                productNames              : 'test_product_names_b'
        ]
        when:
        service.update(drugClassification_b, paramsMap_b_edit)
        then:
        DrugClassification.countByClassName('test_className_b') == 0
        DrugClassification.countByClassName('test_className_b_edit') == 1
    }

    void "test delete"() {
        when:
        service.delete(drugClassification_b)
        then:
        DrugClassification.countByProductDictionarySelection('test_product_selection_b') == 0
    }

    void "test drugClassificationList"(){
        when:
        def drugClassificationList = service.drugClassificationList()
        then:
        drugClassificationList[0]["product"] == "test_product_names_b"
        drugClassificationList[0]["className"] == "test_className_b"

    }
}
