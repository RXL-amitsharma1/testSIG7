package com.rxlogix

import com.rxlogix.user.User
import grails.converters.JSON

class DataMiningResultController {
    def userService
    def aggregateOnDemandAlertService

    def index(){
        List<Map> fieldList = aggregateOnDemandAlertService.fieldListAdvanceFilter(false, true,false, false, false,"", false)
        User currentUser = userService.getUser()
        Boolean isShareFilterViewAllowed = currentUser.isAdmin()

        render (view: "dataMiningResult",model:[
                fieldList: fieldList.sort({it.display.toUpperCase()}),
                isShareFilterViewAllowed: isShareFilterViewAllowed

        ])
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map<String, List> possibleValuesMap = [:]
        render possibleValuesMap as JSON
    }


    def fetchAdvancedFilterNameAjax(String alertType, String term, Integer page, Integer max) {
        def filterData = [[name:"test_filter", id:134237], [name:"newTestFilter", id:177679], [name:"paracetamol_alert2145", id:132712], [name:"testFilter", id:177684]]
        def totalCount = 4
        render([list : filterData, totalCount : totalCount] as JSON)
    }



}
