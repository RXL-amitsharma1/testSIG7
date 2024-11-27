package com.rxlogix.signal

import com.rxlogix.config.AdvancedFilter
import com.rxlogix.user.Group
import com.rxlogix.user.User

class ClipboardCases {

    static auditable = false
    String name
    User user
    Boolean isDeleted
    String caseIds
    String tempCaseIds
    Boolean isFirstUse
    Boolean isUpdated

    static mapping = {
        caseIds type: "text", sqlType: "clob"
        tempCaseIds type: "text", sqlType: "clob"
    }

    static constraints = {
        name nullable: true
        caseIds nullable: true
        isFirstUse nullable: true
        isDeleted nullable: true
        tempCaseIds nullable: true
    }

}
