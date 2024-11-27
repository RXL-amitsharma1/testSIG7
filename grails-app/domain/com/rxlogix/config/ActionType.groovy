package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.domain.ValueObject
import com.rxlogix.util.MiscUtil

class ActionType implements ValueObject,Serializable {
    static auditable = [ignoreEvents: ["onDelete"]]
    static mapping = {
        table('ACTION_TYPES')
        sort("displayName")
    }

    static constraints = {
        value nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Name", (Constants.SpecialCharacters.DEFAULT_CHARS+["'"]) as String[])
        }
        displayName nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Display Name", (Constants.SpecialCharacters.DEFAULT_CHARS+["'"]) as String[])
        }
        displayName_local nullable: true
        description nullable: true, validator: { value, object ->
            return MiscUtil.validator(value, "Description", (Constants.SpecialCharacters.TEXTAREA_CHARS+["'"]) as String[])
        }
        description_local nullable: true
    }

    Map toMap() {
        [
                id: this.id
        ]
    }

}

