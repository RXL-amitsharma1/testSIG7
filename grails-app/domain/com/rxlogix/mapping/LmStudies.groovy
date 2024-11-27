package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders

class LmStudies implements SelectableList {

    Long id
    String studyNum
    LmProtocols protocol

    static hasMany = [centers: LmCenters]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_STUDY_NUM_DSP"
        cache: "read-only"
        version false
        id column: "STUDY_KEY", generator: "assigned"
        studyNum column: "STUDY_NUM"
        protocol column: "ID_PROTOCOL"
        centers joinTable: [name: "VW_STUDY_CENTER_LINK", key: "STUDY_KEY", column: "CENTER_ID"]
    }

    static constraints = {
        id(nullable:false, unique:true)
        studyNum(blank:false, maxSize:35)

    }

    @Override
    def getSelectableList() {
        LmStudies.withTransaction {
            return this.executeQuery("select distinct lms.studyNum from LmStudies lms order by lms.studyNum asc")
        }
    }
}
