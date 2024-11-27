package com.rxlogix.commandObjects

import com.rxlogix.Constants
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.util.MiscUtil
import grails.validation.Validateable

class GroupCO implements Validateable{
    String name
    String description
    GroupType groupType
    String justificationText
    Boolean forceJustification
    Disposition defaultQualiDisposition
    Disposition defaultQuantDisposition
    Disposition defaultAdhocDisposition
    Disposition defaultEvdasDisposition
    Disposition defaultLitDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    List alertLevelDispositions = [].withDefault {[]}

    void setAlertLevelDispositions(def alertLevelDispositions) {
        if(alertLevelDispositions instanceof String) {
            this.alertLevelDispositions = [alertLevelDispositions]
        } else {
            this.alertLevelDispositions = alertLevelDispositions
        }
    }
}
