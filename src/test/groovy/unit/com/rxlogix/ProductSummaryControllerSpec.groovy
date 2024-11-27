package unit.com.rxlogix

import com.rxlogix.ProductSummaryController
import com.rxlogix.ProductSummaryService
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Ignore
class ProductSummaryControllerSpec extends Specification implements ControllerUnitTest<ProductSummaryController> {
    def setup() {

    }

    def cleanup() {
    }

    void "test index"() {
        given:
        def mockProductSummaryIndexMap = [dataSourceMap : [[pva: "PVA", eudra: "EVDAS", faers: "FAERS"]],
                                          disposition   : ["New Potential Signal", "Requires Review", "Validated Observation"],
                                          productSummary: [] as JSON]
        def mockProdSummaryService = Mock(ProductSummaryService)
        mockProdSummaryService.getProductSummaryIndexMap() >> mockProductSummaryIndexMap
        controller.productSummaryService = mockProdSummaryService
        when:
        controller.index()
        then:
        view == "/productSummary/index"
        model.keySet().size() == 3
        model.dataSourceMap != [:]
        model.disposition != [:]

    }

    void "test search"() {
        given:
        def mockProductSummaryIndexMap = [dataSourceMap       : [[pva: "PVA", eudra: "EVDAS", faers: "FAERS"]],
                                          disposition         : ["New Potential Signal", "Requires Review", "Validated Observation"],
                                          selectedDataSource  : "pva,",
                                          selectedDispositions: "Validated Observation",
                                          productSelection    : '{"1":[],"2":[],"3":[{"name":"Calpol","id":"100060"}],"4":[],"5":[]}',
                                          startDate           : "01-Jul-2018",
                                          endDate             : "31-Dec-2018",
                                          frequency           : "HalfYearly",
                                          productSummary      : [] as JSON]
        def mockProdSummaryService = Mock(ProductSummaryService)
        mockProdSummaryService.getProductSummarySearchMap(_) >> mockProductSummaryIndexMap
        controller.productSummaryService = mockProdSummaryService
        when:
        controller.search()
        then:
        view == "/productSummary/index"
        model.keySet().size() == 9
    }

    void "test search when product selection is empty"() {
        given:
        def mockProductSummaryIndexMap = [dataSourceMap       : [[pva: "PVA", eudra: "EVDAS", faers: "FAERS"]],
                                          disposition         : ["New Potential Signal", "Requires Review", "Validated Observation"],
                                          selectedDataSource  : "pva,",
                                          selectedDispositions: "Validated Observation",
                                          productSelection    : "",
                                          startDate           : "",
                                          endDate             : "",
                                          frequency           : "",
                                          productSummary      : [] as JSON]
        def mockProdSummaryService = Mock(ProductSummaryService)
        mockProdSummaryService.getProductSummarySearchMap(_) >> mockProductSummaryIndexMap
        controller.productSummaryService = mockProdSummaryService
        controller.params.productSelection = ''
        when:
        controller.search()
        then:
        view == "/productSummary/index"
        flash.error == "Please fill all required fields"
        model.keySet().size() == 9
    }

    void "test fetchSubstanceFrequency"() {
        given:
        def mockFrequencyMap = [miningFrequency  : "HalfYearly",
                                probableStartDate: ["01-Jul-2017", "01-Jan-2018", "01-Jul-2018", "01-Jan-2019", "01-Jul-2019"],
                                probableEndDate  : ["31-Dec-2017", "30-Jun-2018", "31-Dec-2018", "30-Jun-2019", "31-Dec-2019"],
                                startDate        : "01-Jul-2017",
                                endDate          : "31-Dec-2017",
                                frequency        : "HalfYearly"]
        def mockProdSummaryService = Mock(ProductSummaryService)
        mockProdSummaryService.getFrequencyMap(_, _) >> mockFrequencyMap
        controller.productSummaryService = mockProdSummaryService
        when:
        controller.fetchSubstanceFrequency()
        then:
        response.getJson()["miningFrequency"] == "HalfYearly"
        response.getJson()["probableStartDate"] == ["01-Jul-2017", "01-Jan-2018", "01-Jul-2018", "01-Jan-2019", "01-Jul-2019"]
        response.getJson()["probableEndDate"] == ["31-Dec-2017", "30-Jun-2018", "31-Dec-2018", "30-Jun-2019", "31-Dec-2019"]
        response.getJson()["startDate"] == "01-Jul-2017"
        response.getJson()["endDate"] == "31-Dec-2017"
        response.getJson()["frequency"] == "HalfYearly"
    }

    void "test fetchSubstanceFrequency when response is empty"() {
        given:
        def mockProdSummaryService = Mock(ProductSummaryService)
        mockProdSummaryService.getFrequencyMap(_, _) >> [:]
        controller.productSummaryService = mockProdSummaryService
        when:
        controller.fetchSubstanceFrequency()
        then:
        response.getJson() == [:]
    }

    void "test requestByForAlert"() {
        given:
        def mockProdSummaryService = Mock(ProductSummaryService)
        mockProdSummaryService.saveRequestByForAlert(_) >> true
        controller.productSummaryService = mockProdSummaryService
        when:
        controller.requestByForAlert()
        then:
        response.getJson()["success"] == true
    }

}
