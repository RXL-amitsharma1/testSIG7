package com.rxlogix.cache

import com.rxlogix.Constants
import com.rxlogix.signal.ProductEventHistory
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Ignore
class CacheServiceSpec extends Specification implements ServiceUnitTest<CacheService> {
    ProductEventHistory peh1
    ProductEventHistory peh2
    ProductEventHistory peh3
    def hazelcastService = Mock(HazelcastService)

    def setup() {
        service.hazelcastService = hazelcastService
        peh1 = new ProductEventHistory(productName: "p1", eventName: "e1")
        peh1.id = 1L
        peh2 = new ProductEventHistory(productName: "p2", eventName: "e2")
        peh2.id = 2L
        peh3 = new ProductEventHistory(productName: "p2", eventName: "e2")
        peh3.id = 3L

        // Mock configuration values from Holders
        config.adhoc.alert.configuration.dynamic.fields = [
                [name: 'alertName', label: 'Alert Name', enabled: true, mandatory: true, orderSequence: 1,
                 section: 'General', orderSequence_detailPage: 1, section_label: 'General Section'],
                [name: 'description', label: 'Description', enabled: false, mandatory: false, orderSequence: 2,
                 section: 'Details', orderSequence_detailPage: 2, section_label: 'Detail Section']
        ]
    }

    def cleanup() {
       // CacheService.clearAlertCommentCache()
    }

    void "test for producePEHKey with valid product and event names"() {
        expect:
        CacheService.produceKey("a", "b") == "a".hashCode() + "b".hashCode()
    }

    void "test for producePEHKey with valid product and empty event names"() {
        expect:
        CacheService.produceKey("a", "") == "a".hashCode()
    }

    void "test for producePEHKey with valid product and null event names"() {
        expect:
        CacheService.produceKey("a", null) == "a".hashCode()
    }

    void "test for producePEHKey with empty product and null event names"() {
        expect:
        CacheService.produceKey("", "b") == "b".hashCode()
    }

    void "test for producePEHKey with empty product and empty event names"() {
        expect:
        CacheService.produceKey("", "") == null
    }

    void "test for producePEHKey with null product and null event names"() {
        expect:
        CacheService.produceKey(null, null) == null
    }

    void "test getDispositionListById" (){
        when:
        def result = service.getDispositionListById([])
        then:
        println(result)
    }

    def "test composeAdhocAlertDetailList with empty list"() {
        setup:
        List<Map> detailPageList = []

        when:
        List<List<Object>> result = service.composeAdhocAlertDetailList(detailPageList)

        then:
        result.isEmpty()
    }

    def "test composeAdhocAlertDetailList with no attributes"() {
        given:
        List<Map> detailPageList = []

        when:
        List<List<Object>> result = service.composeAdhocAlertDetailList(detailPageList)

        then:
        result.isEmpty()
    }

    def "test composeAdhocAlertDetailList with mixed attributes"() {
        setup:
        List<Map> detailPageList = [
                createItem("label1", "name1", "12"),
                createItem("label2", "name2", "6"),
                createItem("label3", "name3", "12"),
                createItem("label4", "matchingAlerts", "6"), // should be ignored
                createItem("label5", "name4", "6"),
                createItem("label6", "currentDisposition", "6"), // should be ignored
                createItem("label7", "name5", "6")
        ]

        when:
        List<List<Object>> result = service.composeAdhocAlertDetailList(detailPageList)

        then:
        result.size() == 1
    }

    def "test formatterReviewList with multiple entries"() {
        setup:
        List<Map<String, Object>> thirdList = [
                ["label": "Label 1", "name": "initDataSrc"],
                ["label": "Label 2", "name": "dataSrc"],
                ["label": "Label 3", "name": "anotherSrc"]
        ]

        when:
        List<Map<String, Object>> result = service.formatterReviewList(thirdList)

        then:
        result.size() == 3
        result[0].containerView == 3
        result[0].label == "Label 1"
        result[0].name == "initDataSrc"

        result[1].containerView == 1
        result[1].label == "Label 2"
        result[1].name == "dataSrc"

        result[2].containerView == 1
        result[2].label == "Label 3"
        result[2].name == "anotherSrc"
    }

    def "test formatterReviewList with empty input"() {
        setup:
        List<Map<String, Object>> thirdList = []

        when:
        List<Map<String, Object>> result = service.formatterReviewList(thirdList)

        then:
        result.isEmpty()
    }

    def "test composeAdhocAlertDetailList with mixed attributes"() {
        given:
        List<Map> detailPageList = [
                [name: 'notes', label: 'Notes', orderSequence: 1],
                [name: 'actionTaken', label: 'Action Taken', orderSequence: 2],
                [name: 'description', label: 'Description', orderSequence: 3],
                [name: 'customField', label: 'Custom Field', orderSequence: 4],
                [name: 'matchingAlerts', label: 'Matching Alerts', orderSequence: 5],
                [name: 'disposition', label: 'Disposition', orderSequence: 6],
                [name: 'commentSignalStatus', label: 'Comment Signal Status', orderSequence: 7],
                [name: 'currentDisposition', label: 'Current Disposition', orderSequence: 8],
                [name: 'extraField', label: 'Extra Field', orderSequence: 9]
        ]

        when:
        List<List<Object>> result = service.composeAdhocAlertDetailList(detailPageList)

        then:
        result.size() == 4 // 5 groups expected
        println ""+result[0]
        result[0].size() == 3 // Standalone attribute
        result[1].size() == 1 // Standalone attribute
    }

    void "test getWidth returns correct width for given names"() {
        expect:
        service.getWidth('notes') == '12'
        service.getWidth('actionTaken') == '4'
        service.getWidth('otherField') == '2'
    }
}
