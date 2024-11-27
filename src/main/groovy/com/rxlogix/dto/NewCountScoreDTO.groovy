package com.rxlogix.dto

import com.fasterxml.jackson.databind.ObjectMapper


class NewCountScoreDTO {

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

    NewCountScoreDTO addNewCountData(String counts) {
        int newCount
        int cumCount
        String name
        Map<String, String> newCountsJsonMap = new ObjectMapper().readValue(counts, Map.class);
        for (Map.Entry<String, Object> entry : newCountsJsonMap?.entrySet()) {
            if (entry.getKey().contains("new")) {
                newCount = entry.getValue().new
                cumCount = entry.getValue().cum
                name = entry.getKey()
                entry.setValue(newCount)
                this."${name}" = entry.getValue()
                this."${name.replace("new", "cum")}" = cumCount
            }
        }
        this
    }


}