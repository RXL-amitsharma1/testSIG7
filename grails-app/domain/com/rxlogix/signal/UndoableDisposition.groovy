package com.rxlogix.signal

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class UndoableDisposition {

    Long objectId
    String objectType

    Long prevDispositionId
    Long currDispositionId

    String prevProposedDisposition
    String prevJustification
    String prevDispPerformedBy
    String prevWorkflowState

    Integer previousDueIn

    Date prevDueDate
    Date pastPrevDueDate
    Date prevDispChangeDate
    Date prevActualDueDate
    Date prevMilestoneDate

    Date dateCreated
    Date lastUpdated

    Boolean isEnabled = true
    Boolean isUsed=false
    Boolean isDueDateChanged = false

    String prevSignalStatus
    // Outcome auto-populated by disposition change
    Long signalOutcomeId
    static constraints = {

        prevProposedDisposition nullable: true
        prevJustification nullable: true, maxSize: 8000
        prevDueDate nullable: true
        pastPrevDueDate nullable: true
        prevWorkflowState nullable: true
        prevDispChangeDate nullable: true
        prevDispPerformedBy nullable: true
        prevActualDueDate nullable: true
        previousDueIn nullable:true
        isDueDateChanged nullable: true
        prevMilestoneDate nullable: true
        prevSignalStatus nullable: true
        signalOutcomeId nullable: true
    }
    static mapping = {
        table("UNDOABLE_DISP")
        version false
    }
}
