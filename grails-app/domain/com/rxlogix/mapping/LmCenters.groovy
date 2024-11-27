package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders

class LmCenters implements SelectableList {

    Long id
    String center

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_LCE_CENTER_NAME_DSP"

        cache: "read-only"
        version false

        id column: "CENTER_ID", generator: "assigned"
        center column: "CENTER_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        center(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmCenters.withTransaction {
            return this.executeQuery("select distinct c.center from LmCenters c order by c.center asc")
        }
    }
}
