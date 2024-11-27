package com.rxlogix

import com.rxlogix.PriorityService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.config.PriorityDispositionConfig
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

class PriorityServiceSpec extends Specification implements ServiceUnitTest<PriorityService> {

    Priority priority, newPriority, aNewPriority

    def setup() {
        priority =
                new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        newPriority =
                new Priority([displayName: "newPriority", value: "newPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        newPriority.save(failOnError: true)

        aNewPriority =
                new Priority([displayName: "newPriority", value: "newPriority", display: true, defaultPriority: false, reviewPeriod: 1])


        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.updatePriorityCache(newPriority) >> {
            return newPriority
        }
        mockCacheService.updatePriorityCache(aNewPriority) >> {
            return aNewPriority
        }
        service.cacheService = mockCacheService
    }

    def cleanup() {
    }

    void "test the service method savePriority"() {
        when:
        service.savePriority(newPriority)

        then:
        newPriority.id != null
    }

    void "test the service method savePriority with validation errors"() {
        setup:
        Priority newPriority = new Priority()

        when:
        service.savePriority(newPriority)

        then:
        newPriority.id == null
        thrown ValidationException
    }

    void "test the service method savePriority with defaultPriority update"() {
        when:
        service.savePriority(newPriority)

        then:
        newPriority.id != null
        !priority.defaultPriority
    }

    void "test the service method savePriority without defaultPriority update"() {
        when:
        service.savePriority(aNewPriority)

        then:
        aNewPriority.id != null
        priority.defaultPriority
    }

    void "test the service method savePriority without existing priorities"() {
        when:
        service.savePriority(newPriority)

        then:
        newPriority.id != null
    }

    void "test listPriorityOrder"() {
        when:
        List<Priority> listPriorityOrder = service.listPriorityOrder()

        then:
        listPriorityOrder.size() == 2
        listPriorityOrder[0][0] == Priority.get(1).displayName
        listPriorityOrder[0][0] != Priority.get(2).displayName
    }

    void "test listPriorityAdvancedFilter"() {
        setup:
        Map<Long, Priority> priorityMap = [:]
        priorityMap.put(1L, priority)
        priorityMap.put(2L, newPriority)

        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getPriorityCacheMap() >> {
            return priorityMap
        }
        service.cacheService = mockCacheService

        when:
        List<Map> listPriorityAdvancedFilter = service.listPriorityAdvancedFilter()

        then:
        listPriorityAdvancedFilter[0].id == 1L
        listPriorityAdvancedFilter[1].id == 2L
    }

    void "test for fetchDispositionConfigList()"(){
        setup:
        Disposition disposition1 = new Disposition(id: 111, value: "disp1", displayName: "name1", description: "desc1", abbreviation: "ab1")
        disposition1.save(flush: true, failOnError: true)
        Disposition disposition2 = new Disposition(id: 222, value: "disp2", displayName: "name2", description: "desc1", abbreviation: "ab2")
        disposition2.save(flush: true, failOnError: true)
        Disposition disposition3 = new Disposition(id: 333, value: "disp3", displayName: "name3", description: "desc1", abbreviation: "ab3")
        disposition3.save(flush: true, failOnError: true)

        PriorityDispositionConfig priorityDispositionConfig1 = new PriorityDispositionConfig("priority": priority, "disposition": disposition1, "dispositionOrder": 1, "reviewPeriod": 10).save(flush: true, failOnError: true)
        PriorityDispositionConfig priorityDispositionConfig2 = new PriorityDispositionConfig("priority": priority, "disposition": disposition2, "dispositionOrder": 1, "reviewPeriod": 10).save(flush: true, failOnError: true)
        PriorityDispositionConfig priorityDispositionConfig3 = new PriorityDispositionConfig("priority": priority, "disposition": disposition3, "dispositionOrder": 2, "reviewPeriod": 30).save(flush: true, failOnError: true)

        when:
        List priorityConfigList = service.fetchDispositionConfigList(priority)

        then:
        priorityConfigList.size() == 2
        priorityConfigList[0].size() == 2
        priorityConfigList[0][0].reviewPeriod == 10
        priorityConfigList[1][0].reviewPeriod == 30

    }

    void "test for saveDispositionConfig()"(){
        setup:
        Disposition disposition1 = new Disposition(id: 111, value: "disp1", displayName: "name1", description: "desc1", abbreviation: "ab1")
        disposition1.save(flush: true, failOnError: true)
        Disposition disposition2 = new Disposition(id: 222, value: "disp2", displayName: "name2", description: "desc1", abbreviation: "ab2")
        disposition2.save(flush: true, failOnError: true)
        Map params = [:]
        params.dispositions = '[{"displayName":"name1","reviewPeriod":"3","order":1},{"displayName":"name1","reviewPeriod":"3","order":1}]'

        when:
        service.saveDispositionConfig(params , priority)

        then:
        PriorityDispositionConfig.list().size() == 2

    }


}
