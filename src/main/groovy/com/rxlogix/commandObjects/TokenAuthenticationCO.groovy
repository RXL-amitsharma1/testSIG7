package com.rxlogix.commandObjects

import com.rxlogix.ProductGroupStatusController
import grails.validation.Validateable

class TokenAuthenticationCO implements Validateable{

    String username
    String apiToken
    BatchLotDataCO batchLotDataReferance;
    DictionaryGroupStatusCO productGroupStatusReferance

    TokenAuthenticationCO(String username, String apiToken) {
        this.username = username
        this.apiToken = apiToken
    }

    TokenAuthenticationCO() {
    }

    static constraints = {
        username nullable: true, blank: false
        apiToken nullable: false, blank: false
        batchLotDataReferance nullable: true
        productGroupStatusReferance nullable: true
    }
}
