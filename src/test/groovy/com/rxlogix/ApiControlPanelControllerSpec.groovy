package com.rxlogix

import com.rxlogix.signal.BatchLotStatus
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
//@TestFor(ApiControlPanelController)
//@Mock([BatchLotStatus])
class ApiControlPanelControllerSpec extends Specification implements ControllerUnitTest<ApiControlPanelController> {
    BatchLotStatus batchLotStatus
    Date dt1
    Date dt2

    def setup() {
        dt1 = new Date()
        dt2 = new Date()
        batchLotStatus = new BatchLotStatus(version: 1,etlStartDate: dt1,uploadedAt: dt2)
        batchLotStatus.save(validate:false)
    }

    def cleanup() {
    }

    void "test index"(){
        setup:
        batchLotStatus.etlStatus = "STARTED"
        batchLotStatus.save(validate: false)
        List list = BatchLotStatus.list()
        BatchRestService mockService = Mock(BatchRestService)
        mockService.getLastETLBatchLots()>> {
            return list
        }
        controller.batchRestService = mockService
        when:
        Map result =controller.index()
        then:
        result.lastEtlStatus == "RUNNING"
        result.lastEtlDate == dt1
    }

    void "test index etlStatus FAILED"(){
        setup:
        batchLotStatus.etlStatus = "FAILED"
        batchLotStatus.save(validate: false)
        List list = BatchLotStatus.list()
        BatchRestService mockService = Mock(BatchRestService)
        mockService.getLastETLBatchLots()>> {
            return list
        }
        controller.batchRestService = mockService
        when:
        Map result =controller.index()
        then:
        result.lastEtlStatus == "FAILED"
        result.lastEtlDate == dt1
    }

    void "test index etlStatus  COMPLETED"(){
        setup:
        batchLotStatus.etlStatus = "COMPLETED"
        batchLotStatus.save(validate: false)
        List list = BatchLotStatus.list()
        BatchRestService mockService = Mock(BatchRestService)
        mockService.getLastETLBatchLots()>> {
            return list
        }
        controller.batchRestService = mockService
        when:
        Map result =controller.index()
        then:
        result.lastEtlStatus == "SUCCESS"
        result.lastEtlDate == dt1
    }

    void "test runApiETL"(){
        setup:
        batchLotStatus.etlStatus = "STARTED"
        batchLotStatus.save(validate: false)
        List list = BatchLotStatus.list()
        BatchRestService mockService = Mock(BatchRestService)
        mockService.getLastETLBatchLots()>> {
            return list
        }
        mockService.runEtlForRemainingApiBatchLot()>> {
            return "count"
        }
        controller.batchRestService = mockService
        when:
        controller.runApiETL()
        then:
        response.status == 200
        response.json.lastEtlStatus == "RUNNING"
        response.json.count == "count"
    }
}
