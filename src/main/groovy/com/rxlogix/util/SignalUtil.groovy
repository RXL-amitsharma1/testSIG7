package com.rxlogix.util

import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.ValidatedSignal
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import com.rxlogix.Constants
import groovy.util.logging.Slf4j

@Slf4j
class SignalUtil{

    static List<Map> joinSignalNames(List<ValidatedSignal> signalList) {
        signalList?.collect { [name: it.name + "(S)", signalId: it.id, disposition: it.disposition] }
    }

    static List getProductIdsFromProductSelection(String productSelection){
        List prdIdList = []
        Map jsonObj = null
        if (productSelection) {
            jsonObj = parseJsonString(productSelection)
            if (!jsonObj)
                prdIdList = []
            else {
                List prdVal = jsonObj.find { k,v->
                    v.find {
                        it.containsKey('id')
                    }
                }.value.findAll {
                    it.containsKey('id')
                }.collect {
                    it.id
                }
                prdIdList = prdVal
            }
        }
        prdIdList
    }

    static Map parseJsonString(String jsonString) {
        Map parsedData = [:]
        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            parsedData = jsonSlurper.parseText(jsonString)
        } catch (Exception ex) {
            log.error("Some error occurred",ex)
        }
        parsedData
    }

    static Map generateProductDictionaryMap(String productId, String productName, String dictionarySelection,Boolean isMultiIngredient = false){
        Integer len = PVDictionaryConfig.ProductConfig.columns.size()
        Map map = new HashMap<String, Map>()
        (1..len).each { map.put(String.valueOf(it), []) }
        if(dictionarySelection == PVDictionaryConfig.ingredientColumnIndex)
        {
            map[dictionarySelection] << [name: productName, id:productId, isMultiIngredient: isMultiIngredient?:false]
        } else{
            map[dictionarySelection] << [name:productName,id:productId]
        }
        map
    }

    static Map inititalizeProductDictionaryMap(){
        Integer len = PVDictionaryConfig.ProductConfig.columns.size()
        Map map = new HashMap<String, Map>()
        (1..len).each { map.put(String.valueOf(it), []) }
        map
    }

    static String generateProductDictionaryJson(String productId, String productName, String dictionarySelection, Boolean isMultiIngredient = false) {
        Map map = generateProductDictionaryMap(productId, productName, dictionarySelection, isMultiIngredient)
        ObjectMapper mapper = new ObjectMapper()
        String jsonString = mapper.writeValueAsString(map)  //Added for PVS-63058
        jsonString
    }

    static String getDictionarySelectionType(Map productSelection){
        String dictionarySelection = null
        productSelection.each{ k,v ->
            if(k != Constants.DRUG_RECORD_NUMBER_LEVEL && v){
                dictionarySelection = k
            }
        }
        dictionarySelection
    }

    static String getTermScopeFromSMQ(String smq){
        String result = smq.substring(smq.lastIndexOf("(") + 1, smq.lastIndexOf(")"))
        result
    }

    static Integer getIntegerFromString(String value){
        Integer intValue = 0
        try{
            intValue = Integer.parseInt(value)
        }catch (Exception e){
            intValue = 0
        }
        intValue
    }

    static Double getDoubleFromString(String value) {
        Double doubleValue = 0
        try {
            doubleValue = Double.parseDouble(value)
        } catch (Exception e) {
            doubleValue = 0
        }
        doubleValue
    }

    static getMapFromClob(String clob) {
        Map map = [:]
        clob = clob.replace('"', '')
        try {
            clob.split(",").each {
                String value = it.split(":").size() > 1 ? it.split(":")[1] : "0"
                map.put(it.split(":")[0].trim(), value)
            }
        } catch (Exception e){
            log.error(e.getMessage(), e)
        }
        return map
    }
}
