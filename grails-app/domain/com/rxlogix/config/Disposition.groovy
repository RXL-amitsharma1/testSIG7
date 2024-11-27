package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier

class Disposition implements Serializable {
    static auditable = [ignore:['notify','signalStatusForDueDate'],ignoreEvents: ["onDelete"]]
    Long id
    String value
    @AuditEntityIdentifier
    String displayName
    String description
    String abbreviation
    String colorCode
    boolean display = true
    boolean validatedConfirmed = false
    boolean notify = false
    boolean closed = false
    boolean reviewCompleted = false
    Boolean resetReviewProcess = false
    String signalStatusForDueDate


    static mapping = {
        table('DISPOSITION')
        sort("displayName")
    }

    static constraints = {
        displayName nullable: false, blank: false,unique: true, maxSize: 255, validator: { value, object ->
            return MiscUtil.validator(value, "Display Name", Constants.SpecialCharacters.DEFAULT_DISP_CHARS as String[])
        }
        description nullable: true, maxSize: 255, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
        value unique: true, blank: false, maxSize: 255,validator: { value, object ->
            return MiscUtil.validator(value, "Value", Constants.SpecialCharacters.DEFAULT_DISP_CHARS as String[])
        }
        validatedConfirmed validator: { val, obj ->
            if (obj.validatedConfirmed && obj.closed) {
                return "disposition.validated.and.closed"
            }
        }
        closed nullable: true, validator: { val, obj ->
            if (obj.validatedConfirmed && obj.closed) {
                return "disposition.validated.and.closed"
            }
        }
        abbreviation nullable: false, size: 1..3, validator: { value, object ->
            return MiscUtil.validator(value, "Abbreviation", (Constants.SpecialCharacters.DEFAULT_CHARS-["#"]) as String[])
        }
        colorCode nullable: true, validator: { value, object ->
            return MiscUtil.validator(value, "Color Code", (Constants.SpecialCharacters.DEFAULT_CHARS-["#"]) as String[])
        }
        signalStatusForDueDate nullable: true
    }

    @Override
    boolean equals(object) {
        if (object) {
            this.value == object.value
        } else {
            false
        }
    }

    @Override
    def String toString() { displayName }

    def getEntityValueForDeletion(){
        return "Name-${displayName}"
    }

}
