package com.rxlogix.signal

import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil

import java.text.SimpleDateFormat

class EvdasHistory implements Serializable {

    String productName
    String eventName
    boolean isLatest
    Date executionDate
    Date asOfDate
    String change
    Boolean isUndo = false

    //Workflow management related params.
    Disposition disposition
    User assignedTo
    Group assignedToGroup
    Priority priority

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    String justification

    Long configId
    Long evdasAlertId
    Long archivedEvdasAlertId

    //Evdas related parameters.
    Integer newEv
    Integer totalEv
    String newEea
    String totEea
    String newHcp
    String totHcp
    Integer newSerious
    Integer totalSerious
    String newObs
    String totObs
    Integer newFatal
    Integer totalFatal
    String newMedErr
    String totMedErr
    String newLit
    String totalLit
    String newPaed
    String totPaed
    String newGeria
    String totGeria
    String newSpont
    String totSpont
    String totSpontEurope
    String totSpontNAmerica
    String totSpontJapan
    String totSpontAsia
    String totSpontRest
    String newRc
    String totRc
    Date dueDate
    Date createdTimestamp

    static constraints = {
        justification nullable: true , maxSize: 9000
        createdBy nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        modifiedBy nullable: true
        executionDate nullable: true
        change nullable: true
        assignedTo nullable: true
        assignedToGroup nullable: true
        configId nullable: true
        evdasAlertId nullable: true
        newEv nullable:true
        totalEv nullable:true
        newEea nullable:true
        totEea nullable:true
        newHcp nullable:true
        totHcp nullable:true
        newSerious nullable:true
        totalSerious nullable:true
        newObs nullable:true
        totObs nullable:true
        newFatal nullable:true
        totalFatal nullable:true
        newMedErr nullable:true
        totMedErr nullable:true
        newLit nullable:true
        totalLit nullable:true
        newPaed nullable:true
        totPaed nullable:true
        newGeria nullable:true
        totGeria nullable:true
        newSpont nullable:true
        totSpont nullable:true
        totSpontEurope nullable:true
        totSpontNAmerica nullable:true
        totSpontJapan nullable:true
        totSpontAsia nullable:true
        totSpontRest nullable:true
        newRc nullable:true
        totRc nullable:true
        archivedEvdasAlertId nullable: true
        dueDate nullable: true
        isUndo nullable: true
        createdTimestamp nullable: true
    }

    static mapping = {
        priority lazy: false
        disposition lazy: false
        assignedTo lazy: false
        assignedToGroup lazy: false
        justification column: "JUSTIFICATION", length: 9000
    }

    def toDto(String userTimezone) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
        Date timeStamp = null
        if (Objects.nonNull(this.createdTimestamp)) {
            timeStamp = this.createdTimestamp
        } else {
            timeStamp = this.dateCreated
        }
        [
           id : this.id,
           disposition : this.disposition.displayName,
           priority : this.priority.displayName,
           assignedTo : this.assignedTo ? this.assignedTo.fullName : this.assignedToGroup?.name,
           createdBy : this.createdBy,
           updatedBy : this.modifiedBy,
           timestamp : sdf.format(new Date(DateUtil.toDateStringWithTime(timeStamp, userTimezone))).toString(),
           justification : this.justification,
        ]
    }
}
