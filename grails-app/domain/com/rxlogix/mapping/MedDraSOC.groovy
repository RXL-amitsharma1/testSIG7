package com.rxlogix.mapping

import grails.util.Holders

class MedDraSOC {

    Long id
    String name
    int langId

    static hasMany = [childEvents: MedDraHLGT]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "PVR_MD_SOC_DSP"
        cache: "read-only"
        version false
        id column: "SOC_CODE", generator: "assigned"
        name column: "SOC_NAME"
        langId column: "LANG_ID"
        childEvents joinTable: [name: "PVR_MD_SOC_HLGT_COMP_DSP", key: "SOC_CODE", column: "HLGT_CODE"]
    }

    static constraints = {
        name(blank:false, maxSize:70)
    }
}
