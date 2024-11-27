package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.domain.ValueObject
import com.rxlogix.util.MiscUtil

class ActionConfiguration implements ValueObject,Serializable {
    static auditable = true

    boolean isEmailEnabled

    static mapping = {
        table('ACTION_CONFIGURATIONS')
    }

    static hasMany = [actionsOfConfiguration: Action]

    static constraints = {
        value nullable: false, maxSize: 255, validator: { value, object ->
            return MiscUtil.validator(value, "Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        displayName nullable: false, maxSize: 255, validator: { value, object ->
            return MiscUtil.validator(value, "Display Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        displayName_local nullable: true
        description_local nullable: true
        description nullable: true, maxSize: 255, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
    }
    Map toMap() {
        [
                id: this.id
        ]
    }

    def getEntityValueForDeletion(){
        return "Name-${displayName}"
    }

}
