package com.rxlogix.config

class PVReportsMiscConfig {
    static auditable = false
    String key
    String value
    
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "Application"
    String modifiedBy = "Application"

    static mapping = {
        table name: "MISC_CONFIG"
        key column:"KEY_1", type: 'string'
        value column: 'VALUE', type: 'string'
        id generator: 'assigned', name: 'key', type :'string'
    }

    def String getValue(key) {
        PVReportsMiscConfig.findByKey(key)
    }

    def static putValue(key, value) {
        def keyValuePair = PVReportsMiscConfig.get(key)

        if (keyValuePair) {
            keyValuePair.value = value
        } else 
            keyValuePair = new PVReportsMiscConfig(key: key, value: value)

        keyValuePair.save()
    }
}
