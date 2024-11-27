package com.rxlogix

import com.lowagie.text.Meta
import com.rxlogix.api.BatchRestController
import com.rxlogix.audit.AuditTrail
import com.rxlogix.commandObjects.BatchLotCO
import com.rxlogix.commandObjects.BatchLotDataCO
import com.rxlogix.commandObjects.TokenAuthenticationCO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.BatchLotData
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

//@Mock([BatchLotStatus,User,BatchLotStatus,BatchLotData, AuditTrail])
//@TestFor(BatchRestController)
class BatchRestControllerSpec extends Specification implements ControllerUnitTest<BatchRestController> {
    def setup(){
    }
    void "test importClientData when username and Api Token are null"(){
        setup:
        BatchLotCO batchLotCO = new BatchLotCO()
        when:
        controller.importClientData(batchLotCO)
        then:
        response.status == 200
        response.json.message == "Username and Api Token cannot be null and blank"
    }

    void "test importClientData when username and Api Token are invalid"(){
        setup:
        BatchLotCO batchLotCO = new BatchLotCO(apiUsername: "test",apiTocken: "2@abc")
        when:
        controller.importClientData(batchLotCO)
        then:
        response.status == 200
        response.json.message == "case.series.spotfire.user.not.exist"
    }

    void "test importClientData when valid username and Token are present"(){
        setup:
        ResponseDTO responseDTO = new ResponseDTO()
        responseDTO.status = true
        responseDTO.message = "Authentication is successful"
        controller.metaClass.authenticateUserTocken = { TokenAuthenticationCO commandObject->
            return responseDTO
        }
        BatchRestService mockService = Mock(BatchRestService)
        mockService.saveBatchClientRecord(_, _,_) >> {
        }
        controller.batchRestService = mockService
        BatchLotCO batchLotCO = new BatchLotCO(apiUsername: "test",apiTocken: "2@abc")
        when:
        controller.importClientData(batchLotCO)
        then:
        response.status == 200
        response.json.message == "Imported Record Count: 0 and Invalid Record Count: 0"
    }

    void "test importBatchLot when batchLotDataCo blank"(){
        setup:
        BatchLotDataCO batchLotDataCO = new BatchLotDataCO()
        when:
        controller.importBatchLot(batchLotDataCO)
        then:
        response.status == 200
        response.json.message == "Username and Api Token cannot be null and blank"
    }

    void "test importBatchLot when"(){
        setup:
        ResponseDTO responseDTO = new ResponseDTO()
        responseDTO.status = true
        responseDTO.message = "Authentication is successful"
        controller.metaClass.authenticateUserTocken = { TokenAuthenticationCO commandObject->
            return responseDTO
        }
        BatchRestService mockService = Mock(BatchRestService)
        mockService.getBatchLotDatas(_) >> {
            return []
        }
        controller.batchRestService = mockService
        BatchLotDataCO batchLotDataCO = new BatchLotDataCO(apiTocken: "R@123",apiUsername: "test")
        when:
        controller.importBatchLot(batchLotDataCO)
        then:
        response.status == 200
        response.json.message == "Imported Record Count: 0 and Invalid Record Count: 0"
        response.json.status == true
    }

    void "test  authenticate Username and Api Token cannot be null and blank"(){
        setup:
        TokenAuthenticationCO tokenAuthenticationCO = new TokenAuthenticationCO()
        when:
        controller.authenticate(tokenAuthenticationCO)
        then:
        response.status == 200
        response.json.message == "Username and Api Token cannot be null and blank"
    }

    void "test  authenticate"(){
        setup:
        ResponseDTO responseDTO = new ResponseDTO()
        responseDTO.status = true
        responseDTO.message = "Authentication is successful"
        responseDTO.data = 1
        controller.metaClass.authenticateUserTocken = { TokenAuthenticationCO commandObject->
            return responseDTO
        }
        controller.metaClass.saveBatchLotAuditLog = { TokenAuthenticationCO commandObject, ResponseDTO responseDO->
            return responseDTO
        }
        TokenAuthenticationCO tokenAuthenticationCO = new TokenAuthenticationCO(apiToken: "R@123",username: "test")
        when:
        controller.authenticate(tokenAuthenticationCO)
        then:
        response.status == 200
        response.json.message == "Authentication is successful"
        response.json.data==1
    }

    void "test saveBatchLotAuditLog"(){
        setup:
        ResponseDTO responseDTO = new ResponseDTO()
        responseDTO.status = true
        responseDTO.message = "Authentication is successful"
        TokenAuthenticationCO tokenAuthenticationCO = new TokenAuthenticationCO()
        when:
        controller.saveBatchLotAuditLog(tokenAuthenticationCO,responseDTO)
        then:
        response.status ==200
    }

}