package com.rxlogix.dto


import com.rxlogix.util.SignalUtil
import grails.converters.JSON
import liquibase.util.StringUtil

class StratificationScoreDTO {

    Map props = [:]

    def getProperty(String property) {
        return props[property]
    }

    void setProperty(String property, Object newValue) {
        props[property] = newValue
    }

    Map mapData() {
        this.props
    }

    StratificationScoreDTO addStratificationScore(String stratificationScore, String type) {
        if (stratificationScore && stratificationScore != "0") {
            Integer jsonStringLength = stratificationScore.length() - 1
            String jsonString = stratificationScore.charAt(0) == '"' && stratificationScore.charAt(jsonStringLength) ? stratificationScore?.substring(1, jsonStringLength) : stratificationScore
            List<String> splitJson = jsonString?.split(",")
            splitJson.each {
                List<String> keyValue = it?.trim()?.split(":")
                String label = keyValue[0]?.toString()?.trim()
                if (StringUtil.isNotEmpty(label)) {
                    this."${type.toUpperCase()}_${label.toUpperCase()}" = SignalUtil.getDoubleFromString(keyValue[1])
                }
            }
        }
        this
    }

    StratificationScoreDTO addAllSubGroupScore(String stratificationScore, String type) {
        if (stratificationScore && stratificationScore != "0") {
            String jsonString = stratificationScore
            Map keyValueMap = JSON.parse(jsonString)
            keyValueMap?.each { subGroup, columnDataMap ->
                columnDataMap.each { key, value ->
                    String label = "${type.toUpperCase()}_${key.toUpperCase()}"
                    this."${label}" = SignalUtil.getDoubleFromString(value.toString())
                }
            }
        }
        this
    }
}