package unit.com.rxlogix.config

import com.rxlogix.config.LiteratureActivity
import com.rxlogix.config.LiteratureActivityService
import com.rxlogix.dto.AlertLevelDispositionDTO
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class LiteratureActivityServiceSpec extends Specification implements ServiceUnitTest<LiteratureActivityService> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true
    }

    def "test createLiteratureActivityAlertLevelDisposition() method"(){
        given:
        Map alertMap = [productName:'Test Product']
        AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO()

        when:
        LiteratureActivity literatureActivity = service.createLiteratureActivityAlertLevelDisposition(alertMap,alertLevelDispositionDTO)

        then:
        alertMap.productName == literatureActivity.productName

    }
}
