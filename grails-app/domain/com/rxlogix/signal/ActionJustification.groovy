package com.rxlogix.signal

import com.rxlogix.enums.ActionTypeEnum
import groovy.json.JsonBuilder

class ActionJustification {

    static auditable = false

    ActionTypeEnum actionType
    String justification
    String posterClass
    String attributesMap

    Date dateCreated
    Date lastUpdated
    String createdBy

    ActionJustification(String actionType, String justification, String posterClass, Map attributesMapData, String currentUserName) {
        this.actionType = actionType
        this.justification = justification
        this.posterClass = posterClass
        this.attributesMap = new JsonBuilder(attributesMapData).toPrettyString()
        this.createdBy = currentUserName
    }

    static mapping = {
        table("ACTION_JUSTIFICATION")
        attributesMap sqlType: "varchar2(8000 CHAR)"
        justification column: "JUSTIFICATION", sqlType: "varchar2(8000 CHAR)"
    }


}
