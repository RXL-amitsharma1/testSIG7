package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.ProductEventHistory
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification


class ProductEventHistoryServiceSpec extends Specification implements ServiceUnitTest<ProductEventHistoryService> {
    ProductEventHistory peh1
    ProductEventHistory peh2
    ProductEventHistory peh3
    Disposition disposition
    Priority priority

    def setup() {
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true,flush:true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: 'vs')
        disposition.save(flush:true)
        peh1 = new ProductEventHistory(productName: "p1", eventName: "e1" , configId: 1l , execConfigId: 1l ,
                change: Constants.HistoryType.ASSIGNED_TO , priority: priority,
                disposition: disposition , prrValue: '0' , rorValue: '0' , eb05: 0.1 , ebgm: 0.2 ,
                eb95:0.3 , asOfDate: new Date())
        peh1.id = 1L
        peh1.save(flush:true)
        peh2 = new ProductEventHistory(productName: "p1", eventName: "e1" ,  configId: 1l , execConfigId: 1l ,
                change: Constants.HistoryType.PRIORITY , priority: priority,
                disposition: disposition , prrValue: '0' , rorValue: '0' , eb05: 0.1 , ebgm: 0.2 ,
                eb95:0.3 , asOfDate: new Date())
        peh2.id = 2L
        peh2.save(flush:true)
        peh3 = new ProductEventHistory(productName: "p1", eventName: "e1" , priority: priority, configId: 3l , execConfigId: 1l , change: Constants.HistoryType.PRIORITY,
                disposition: disposition , prrValue: '0' , rorValue: '0' , eb05: 0.1 , ebgm: 0.2 ,
                eb95:0.3 ,  asOfDate: new Date())
        peh3.id = 3L
        peh3.save(flush:true)
    }

    def cleanup() {
    }

    void "test getCurrentAlertProductEventHistoryList"() {
        setup:

        when:
        List histories = service.getCurrentAlertProductEventHistoryList("p1", "e1" , 1l ,  1l)
        then:
        histories.size() == 1

    }

    void "test getOtherAlertsProductEventHistoryList"() {
        setup:

        when:
        List histories = service.getCurrentAlertProductEventHistoryList("p1", "e1" , 1l )
        then:
        histories.size() == 1

    }

}
