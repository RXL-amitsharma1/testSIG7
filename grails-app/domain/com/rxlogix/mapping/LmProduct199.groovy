package com.rxlogix.mapping

import grails.util.Holders

class LmProduct199 implements Serializable {

    String viewId
    String name
    String lang

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().supported.datasource.call())
        table "DISP_VIEW_199"
        cache: "read-only"
        version false
        viewId column: "COL_1"
        name column: "COL_2"
        lang column: "lang_id"
        id composite: ['viewId', 'lang']
    }

    static constraints = {
    }
}
