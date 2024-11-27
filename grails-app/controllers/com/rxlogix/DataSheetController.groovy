package com.rxlogix

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.util.TextUtils

@Secured(["isAuthenticated()"])
class DataSheetController {
    def dataSheetService
    def reportIntegrationService

    def dataSheets(String term, int page, int max, String enabledSheet, String dataSource) {
        List datasources = dataSource?.split(',')
        List items =[]
        List filteredData = []
        int offset = Math.max(page - 1, 0) * max
        Map dataSheetMap = [:]
        def totalCount = 0
        List productsList = []
        List paginatedList = []
        if(params["products[]"]){
            productsList.add(params["products[]"])
        }
        Boolean isProductGroup = Boolean.parseBoolean(params.isProductGroup)?:false
        Boolean isMultiIngredient = Boolean.parseBoolean(params.isMultiIngredient)?:false
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        if(productsList && !productsList?.isEmpty()){
            if(datasources?.contains(Constants.DataSource.PVA)){
                dataSheetMap = dataSheetService.getDataSheets(term, enabledSheet,offset, max, productsList?.flatten(), isProductGroup, isMultiIngredient)
            } else{
                dataSheetMap =  dataSheetService.getAllActiveDatasheets(term,enabledSheet, Math.max(page - 1, 0) * max, max)
            }
            items = dataSheetMap?.dataSheetList?.unique()?.collect {
                [id: it.dispName + Constants.DatasheetOptions.SEPARATOR + it.id, text: it.dispName]
            }
            totalCount = dataSheetMap.totalCount
            if(term && !TextUtils.isEmpty(term)){
                items?.each{
                    if(it.text?.toLowerCase()?.contains(term?.toLowerCase())){
                        filteredData?.add(it)
                    }
                }
                items = filteredData
                totalCount = filteredData?.size()
            }
            int countLeft = offset + (totalCount-(page-1)*30)
            paginatedList = items?.subList(offset, (totalCount > page * 30?(page * 30):countLeft))
        }else{
            items =[]
            paginatedList=items
        }

        render([list: paginatedList, totalCount: totalCount] as JSON)
    }
}
