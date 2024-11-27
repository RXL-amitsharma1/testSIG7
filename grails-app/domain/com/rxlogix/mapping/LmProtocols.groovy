package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders

class LmProtocols implements SelectableList {

    Long id
    String description

    static mapping = {

        table "VW_PROTOCOL_DSP"
        datasource "pva"
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        version false
        cache usage: "read-only"
        id column: "PROTOCOL_ID", generator: "assigned"
        description column: "PROTOCOL_DESCRIPTION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(maxSize: 40)
    }

    @Override
    def getSelectableList() {
        LmProtocols.withTransaction {
            return this.executeQuery("select distinct c.description from LmProtocols c order by c.description asc")
        }
    }
}
