package com.rxlogix.api

import com.rxlogix.commandObjects.BatchLotCO
import com.rxlogix.commandObjects.BatchLotDataCO
import com.rxlogix.commandObjects.DictionaryGroupStatusCO
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.signal.ProductGroupStatus
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import com.rxlogix.commandObjects.TokenAuthenticationCO
import com.rxlogix.dto.ResponseDTO
import grails.rest.RestfulController
import groovy.json.JsonSlurper
import org.springframework.util.StringUtils

import java.text.DateFormat
import java.text.SimpleDateFormat

class BatchRestController extends RestfulController {

    def userService

    def batchRestService

    def productGroupStatusService

    BatchRestController() {
        super(User)
    }

    def importBatchLot(BatchLotDataCO batchLotDataCO) {
        if(StringUtils.isEmpty(batchLotDataCO.getBatchId())) {
            batchLotDataCO.setBatchId("Batch Process-"+getDate(new Date(), DateUtil.DEFAULT_DATE_TIME_FORMAT));
        }
        try {
            if((batchLotDataCO.getClientDatas()==null || batchLotDataCO.getClientDatas().size()==0) && batchLotDataCO.getClientDatasString() != null) {
                batchLotDataCO.setClientDatas((new JsonSlurper()).parseText(batchLotDataCO.getClientDatasString()))
            } else if(batchLotDataCO.getClientDatasString() == null || batchLotDataCO.getClientDatasString().trim().length()>0) {
                batchLotDataCO.setClientDatasString(JsonOutput.toJson(tagsList))
            }
        } catch(Exception ex) {
            log.error(ex.toString())
        }
        TokenAuthenticationCO tokenAuthenticationCO = new TokenAuthenticationCO(batchLotDataCO.apiUsername,batchLotDataCO.apiTocken );
        ResponseDTO responseDTO = authenticateUserTocken(tokenAuthenticationCO)
        batchLotDataCO.apiUsername=tokenAuthenticationCO.username
        int importedDataCount = 0
        int invalidDataCount = 0
        if(responseDTO.status == true) {
            BatchLotCO batchLotCO = new BatchLotCO();
            batchLotCO.setBatchId(batchLotDataCO.getBatchId())
            batchLotCO.setApiTocken(batchLotDataCO.getApiTocken())
            batchLotCO.setApiUsername(batchLotDataCO.getApiUsername())
            batchLotCO.setBatchDate(batchLotDataCO.getBatchDate())
            batchLotCO.setCount(batchLotDataCO.getCount())
            batchLotCO.setClientDatas(batchRestService.getBatchLotDatas(batchLotDataCO.getClientDatas()))
            batchRestService.saveBatchClientRecord(batchLotCO, batchLotDataCO.apiUsername, batchLotDataCO.getId());
            responseDTO.setMessage("Imported Record Count: "+importedDataCount+" and Invalid Record Count: "+invalidDataCount)
        }
        render(responseDTO as JSON)
    }

    def authenticate(TokenAuthenticationCO commandObject) {
        if(commandObject!=null && commandObject.getBatchLotDataReferance()!=null &&
            StringUtils.isEmpty(commandObject.getBatchLotDataReferance().getBatchId())) {
            commandObject.getBatchLotDataReferance().setBatchId("Batch Process-"+getDate(new Date(), DateUtil.DEFAULT_DATE_TIME_FORMAT))
        }
        ResponseDTO responseDTO = authenticateUserTocken(commandObject);
        responseDTO  = saveBatchLotAuditLog(commandObject, responseDTO);
        saveProductGroupStatusAuditLog(commandObject, responseDTO);
        render(responseDTO as JSON)
    }
    def authenticateUserTocken(TokenAuthenticationCO commandObject) {
        ResponseDTO responseDTO = new ResponseDTO()
        try {
            if (!commandObject.validate()) {
                log.warn(commandObject.errors.allErrors?.toString())
                responseDTO.setErrorResponse("Api Token cannot be null and blank")
            } else {
                User user = userService.getUserByToken(commandObject)
                if (!user) {
                    responseDTO.setErrorResponse("Token is not valid, Authentication Failed")
                } else {
                    if(userService.authenticateToken(user,commandObject.apiToken)==true){
                        responseDTO.status = true
                        responseDTO.message = "Authentication is successful"
                    } else {
                        responseDTO.setErrorResponse("Token is not valid, Authentication Failed")
                    }
                }
            }
        } catch (Exception ex) {
            responseDTO.setErrorResponse(ex)
        }
        return responseDTO
    }

    def saveBatchLotAuditLog(TokenAuthenticationCO commandObject, ResponseDTO responseDTO) {
        try {
            if(responseDTO.status==true && commandObject.getBatchLotDataReferance() !=null ) {
                BatchLotCO batchLotCO = new BatchLotCO();
                batchLotCO.setBatchId(commandObject.getBatchLotDataReferance()?.getBatchId());
                batchLotCO.setBatchDate(commandObject.getBatchLotDataReferance()?.getBatchDate());
                batchLotCO.setCount(commandObject.getBatchLotDataReferance()?.getCount());
                batchLotCO.setClientDatas([]);
                BatchLotStatus batchLotStatus = batchRestService.saveBatchLotStatusAndAudit(batchLotCO, commandObject.getUsername(), commandObject.getBatchLotDataReferance().getClientDatasString() );
                responseDTO.setData(["savedBatchLotId":batchLotStatus.getId()])
            }
            BatchLotDataCO batchLotCO = commandObject.getBatchLotDataReferance() as BatchLotDataCO;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        responseDTO
    }

    def saveProductGroupStatusAuditLog(TokenAuthenticationCO commandObject, ResponseDTO responseDTO) {
        try {
            if(responseDTO.status==true && commandObject.getProductGroupStatusReferance() !=null ) {
                DictionaryGroupStatusCO productGroupStatusCO = new DictionaryGroupStatusCO();
                productGroupStatusCO.setUniqueIdentifier(commandObject.getProductGroupStatusReferance()?.getUniqueIdentifier());
                productGroupStatusCO.setCount(commandObject.getProductGroupStatusReferance()?.getCount());
                productGroupStatusCO.setProductGroups([])
                ProductGroupStatus productGroupStatus = productGroupStatusService.saveProductGroupStatusAndAudit(productGroupStatusCO, commandObject.getUsername(), commandObject.getProductGroupStatusReferance().getProductGroupsString() );
                responseDTO.setData(["savedProductGroupStatusId":productGroupStatus.getId()])
            }
            BatchLotDataCO batchLotCO = commandObject.getBatchLotDataReferance() as BatchLotDataCO;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        responseDTO
    }

    def runEtl(BatchLotCO batchLotCO) {
        ResponseDTO responseDTO = authenticateUserTocken(new TokenAuthenticationCO(batchLotCO.apiUsername ,batchLotCO.apiTocken))
        if(responseDTO.status == true ) {
            responseDTO = batchRestService.runEtl(batchLotCO.batchId)
            responseDTO.message = message(code:"signal.etl.started.false")
            if(responseDTO.status == true ) {
                responseDTO.message = message(code:"signal.etl.started.false")
            }
        }
        render(responseDTO as JSON)
    }
    private String getDate(Date date, String dateFormatString){
        TimeZone zone = TimeZone.getDefault();
        String name = zone.getDisplayName();
        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        String strDate = dateFormat.format(date);
        return strDate;
    }

}

