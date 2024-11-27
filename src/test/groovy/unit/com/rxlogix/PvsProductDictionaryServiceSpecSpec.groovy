package unit.com.rxlogix


import com.rxlogix.DataObjectService
import com.rxlogix.PvsProductDictionaryService
import com.rxlogix.config.DictionaryMapping
import com.rxlogix.config.ExecutedConfiguration
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([PvsProductDictionaryService])
class PvsProductDictionaryServiceSpecSpec extends Specification implements ServiceUnitTest<PvsProductDictionaryService> {

    ExecutedConfiguration executedConfiguration1
    ExecutedConfiguration executedConfiguration2
    String productViewName
    String dictionaryProductLevel

    def setup() {
        executedConfiguration1 = new ExecutedConfiguration(productSelection: '{"1":[],"2":[],"3":[],"4":[],"5":[],"6":[{"name":"IFOSFAMIDE","id":"-470484647"}],"7":[],"8":[],"9":[]}', productDictionarySelection: '6')
        executedConfiguration1.save(validate: false)
        executedConfiguration2 = new ExecutedConfiguration(productSelection: '{"1":[],"2":[],"3":[],"4":[],"5":[],"6":[],"7":[],"8":[{"name":"Newusers-1113792536","id":"-1113792536"}],"9":[]}', productDictionarySelection: '8')
        executedConfiguration2.save(validate: false)

        dictionaryProductLevel = '6'
        productViewName = 'DISP_VIEW_205'
    }

    def cleanup() {

    }

    void "Test isLevelGreaterThanProductLevel() when selected level is greater than product level "() {
        def mockDataObjectService = Mock(DataObjectService)
        service.dataObjectService = mockDataObjectService
        service.dataObjectService.getDictionaryProductLevel() >> dictionaryProductLevel

        when:
        Boolean result = service.isLevelGreaterThanProductLevel(executedConfiguration2)

        then: "It return true"
        result
    }

    void "Test isLevelGreaterThanProductLevel() when selected level is less than product level "() {
        def mockDataObjectService = Mock(DataObjectService)
        service.dataObjectService = mockDataObjectService
        service.dataObjectService.getDictionaryProductLevel() >> dictionaryProductLevel
        when:
        Boolean result = service.isLevelGreaterThanProductLevel(executedConfiguration1)

        then: "It return false"
        !result
    }

    void "Test fetchDictionaryLevelAndIds() when selected level is greater than product level "() {
        def mockDataObjectService = Mock(DataObjectService)
        service.dataObjectService = mockDataObjectService
        service.dataObjectService.getDictionaryProductLevel() >> dictionaryProductLevel

        when:
        Map result = service.fetchDictionaryLevelAndIds(executedConfiguration2.productSelection)

        then: "It return particular level and its associated product ids"
        result.level == 8
        result.ids == ['-1113792536']
    }

    void "Test fetchDictionaryLevelAndIds() when selected level is less than product level "() {
        def mockDataObjectService = Mock(DataObjectService)
        service.dataObjectService = mockDataObjectService
        service.dataObjectService.getDictionaryProductLevel() >> dictionaryProductLevel

        when:
        Map result = service.fetchDictionaryLevelAndIds(executedConfiguration1.productSelection)

        then: "It return null"
        result.level == null
        result.ids == null
    }

    void "Test prodNameListSql() when selected level is greater than product level "() {
        def mockDataObjectService = Mock(DataObjectService)
        service.dataObjectService = mockDataObjectService
        service.dataObjectService.getProductViewName() >> productViewName

        when:
        String result = service.prodNameListSql("VIEW_207_PRODUCT_LINK", '-1113792536')

        then:
        result == """select distinct(prod.COL_3) from DISP_VIEW_205 prod join VIEW_207_PRODUCT_LINK vw
        on prod.COL_1 = vw.COL_2 and vw.COL_1 in (-1113792536) 
        order by prod.COL_3"""
    }

}