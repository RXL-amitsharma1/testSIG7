package com.rxlogix.spotfire

import org.grails.cxf.utils.GrailsCxfEndpoint

import javax.jws.WebMethod
import javax.jws.WebParam
import javax.jws.WebResult
import javax.jws.WebService

//@WebService(targetNamespace = 'http://spotfire.tibco.com/SpotfireWebDeveloper.CustomWebAuthenticationSite')
@GrailsCxfEndpoint
class SpotfireAPIService {

    def spotfireService

    @WebMethod(exclude = true)
    void setSpotfireService(service) {
        spotfireService = service
    }

    @WebMethod(exclude = true)
    def getSpotfireService() {
        spotfireService
    }

//    It's used by Spotfire Server for verification of User
    @WebMethod(operationName = "LookupUser", action = "http://spotfire.tibco.com/SpotfireWebDeveloper.CustomWebAuthenticationSite/LookupUser")
    @WebResult(name = "LookupUserResult", targetNamespace = "http://spotfire.tibco.com/SpotfireWebDeveloper.CustomWebAuthenticationSite")
    def lookupUser(
            @WebParam(name = 'ticket', targetNamespace = "http://spotfire.tibco.com/SpotfireWebDeveloper.CustomWebAuthenticationSite") String ticket) {
        log.debug("###### SOAP Api Request has been received for verification of user with ticket : ${ticket} ##########")
        return (spotfireService.getActualValue(ticket))
    }
}
