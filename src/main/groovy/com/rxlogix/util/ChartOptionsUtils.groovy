package com.rxlogix.util

import com.rxlogix.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Created by gologuzov on 29.08.16.
 */
class ChartOptionsUtils {
    def static deserialize(String source, def target = null) {
        def slurper = new JsonSlurper()
        if (target == null) {
            target = [:]
        }
        def options = slurper.parseText(source)
        return deepMerge(options, target)
    }

    def static serialize(def object) {
        JsonOutput.toJson(object)
    }

    public static def deepMerge(def source, def target) {
        if (source instanceof Map && target instanceof Map) {
            for (String key : source.keySet()) {
                Object value = source.get(key);
                if (!target.containsKey(key)) {
                    // new value for "key":
                    target.put(key, value);
                } else {
                    // existing value for "key" - recursively deep merge:
                    if (value instanceof Map || value instanceof List) {
                        deepMerge(value, target.get(key));
                    } else {
                        target.put(key, value);
                    }
                }
            }
        } else if (source instanceof List && target instanceof List) {
            source.eachWithIndex{ def entry, int i ->
                if (target.size() <= i) {
                    target.push(entry)
                } else {
                    deepMerge(entry, target[i])
                }
            }
        }
        return target;
    }
}
