package unit.com.rxlogix

import com.rxlogix.DataObjectService
import com.rxlogix.dto.LastReviewDurationDTO
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([DataObjectService])
class DataObjectServiceSpec extends Specification implements ServiceUnitTest<DataObjectService> {

    void "test getStatsDataMapSubgrouping"() {
        setup:
        String productIdVal = "123"
        String eventCodeVal = "456"
        Map valueMap = [EBGM: "0.24"]
        service.statsDataMapSubgrouping = [1L: ["123": ["456": valueMap]]]
        when:
        def statsResults = service.getStatsDataMapSubgrouping(1L, productIdVal, eventCodeVal)
        then:
        statsResults == valueMap

    }

    void "test setStatsDataMapSubgrouping()"() {
        setup:
        String productIdVal = "123"
        String eventCodeVal = "456"
        Map valueMap = [EBGM: "0.24"]
        when:
        service.setStatsDataMapSubgrouping(1L, productIdVal, eventCodeVal,valueMap)
        then:
        service.statsDataMapSubgrouping == [1L: ["123": ["456": valueMap]]]
    }

    void "test setTagsList" () {
        setup :
        String pec = "Test-Medicine-for-Pain"
        List tags = ["Category1", "Category-2"]
        when:
        service.setTagsList(pec, tags)
        then:
        service.tagsList.get(pec) == ["Category1", "Category-2"]
    }

    void "test getTagsList" () {
        setup :
        String pec = "Test-Medicine-for-Fever"
        List tags = ["Category3", "Category-4"]
        service.setTagsList(pec,tags)
        when:
        List tagsList = service.getTagsList(pec)
        then:
        tagsList == ["Category3", "Category-4"]
    }

    @Ignore
     void "test clearTagsList" () {
         setup :
         String pec = "Test-Medicine-for-Cough"
         List tags = ["Category5", "Category-6"]
         service.setTagsList(pec,tags)
         when:
         service.clearTagsList()
         then:
         service.tagsList.isEmpty() == true
     }

    void "test setCaseTags" () {
        setup :
        Long caseId = 123
        List tags = ["Category1", "Category-2"]
        when:
        service.setCaseTags(caseId, tags)
        then:
        service.caseTagMap.get(caseId) == ["Category1", "Category-2"]
    }

    void "test getCaseTags" () {
        setup :
        Long caseId = 456
        List tags = ["Category3", "Category-4"]
        service.setCaseTags(caseId,tags)
        when:
        List tagsList = service.getCaseTags(caseId)
        then:
        tagsList == ["Category3", "Category-4"]
    }

    void "test clearCaseTagMap" () {
        setup :
        Long caseId = 789
        List tags = ["Category5", "Category-6"]
        service.setCaseTags(caseId,tags)
        when:
        service.clearCaseTagMap()
        then:
        service.caseTagMap.isEmpty() == true
    }

    void "test getLastReviewDurationDTO" () {
        setup :
        String product = "ABC"
        String event = "XYZ"
        Date lastEndDate = new Date()
        Long exconfigId = 123L
        LastReviewDurationDTO lastReviewDurationDTO = new LastReviewDurationDTO(product: product, event: event, lastEndDate : lastEndDate)
        service.lastReviewDurationMap.put(exconfigId, lastReviewDurationDTO)
        when :
        LastReviewDurationDTO lastReviewDurationDTO2 = service.getLastReviewDurationDTO(product, event, exconfigId)
        then:
        lastReviewDurationDTO == lastReviewDurationDTO2
    }

    void "test setCurrentEndDateMap" () {
        setup :
        Date currentEndDate = new Date()
        Long exconfigId = 123L
        when :
        service.setCurrentEndDateMap(exconfigId, currentEndDate)
        then:
        service.currentEndDateMap.get(exconfigId) == currentEndDate

    }

    void "test getCurrentEndDateMap" () {
        setup:
        Date currentDate = new Date()
        Long exConfigId = 1234
        when:
        service.currentEndDateMap.put(exConfigId, currentDate)
        then:
        service.getCurrentEndDateMap(exConfigId) == currentDate
    }

    void "test clearCurrentEndDateMap" () {
        setup:
        Date currentDate = new Date()
        Long exConfigId = 1234
        service.currentEndDateMap.put(exConfigId, currentDate)
        when:
        service.clearCurrentEndDateMap(exConfigId)
        then:
        service.currentEndDateMap.get(exConfigId) == null
    }

    void "test setDataSourceMap" (){
        when:
        def result = service.setDataSourceMap("key", true)
        then:
        service.getDataSourceMap("key")
    }

    void "test getDataSourceMap" (){
        when:
        def result = service.setDataSourceMap("key", false)
        then:
        !service.getDataSourceMap("key")
    }

}
