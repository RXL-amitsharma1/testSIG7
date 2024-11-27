package com.rxlogix

import com.rxlogix.signal.AdHocAlert
import grails.util.Holders
import org.grails.web.json.JSONObject

class AlertAttributesService {

    def attributesMap

    def slurp(URL url) {
        def jsonMenus = defaults

        if (url) {
            jsonMenus = readAttributes(url)
        } else {
            jsonMenus = Holders.config.alert.attributes.default.values
        }
        attributesMap = jsonMenus as JSONObject
    }

    def attributeNames(){
        attributesMap.keySet()
    }

    def get(menu){
        def rt = attributesMap ? attributesMap[menu]?.collect {it['value']} : []
        rt?.sort()
    }

    def getDefault(menu){
        def option = attributesMap[menu].find({it["default"]})
        option ? option["value"] : null
    }

    def addMissingProperties(AdHocAlert alert, Map params){
        attributeNames().each {
            alert.propertyMissing(it, params.get(it))
        }

        return alert
    }

    def readAttributes(URL url) {
        if(!url)
            return null

        String result = url.readLines().toString()
        return "{" + result.substring(1, result.length() - 1) + "}"

    }

    def getDisabled(menu) {
        def option = attributesMap[menu].findAll({ it["disabled"].equals("Yes") })
        option ? option["value"] : null
    }
    def getUnsorted(menu){
        def rt = attributesMap[menu].collect {it['value']}
           return rt
    }
    //Sample menu format: fi"publicHealthImpact": [ {"value": "High", "default": true}, {"value": "Medium", "disabled": = true}, {"value": "Low"} ]
    //Keys "default" and "disabled" are booleans (true/false) and optional. There can be only one default: true value in a menu list.
    def defaults = Holders.config.alert.attributes.default.values

    List getSignalStatusList() {
        return attributesMap ? attributesMap['signalHistoryStatus'].sort { it.order }.collect { it.value } : []
    }
}
