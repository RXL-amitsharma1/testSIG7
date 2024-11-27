package com.rxlogix;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EbgmSubGroup {

    Map<String, Object> details = new HashMap<>();

    @JsonAnySetter
    void setDetail(String key, Object value) {
        details.put(key, value);
    }

    @Override
    public String toString() {
        return details.keySet().stream()
                .map(key -> key + ":" + details.get(key))
                .collect(Collectors.joining(", ", "\"", "\""));
    }
}
