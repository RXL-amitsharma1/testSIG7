package com.rxlogix.signal

import com.rxlogix.enums.ProdAssignmentProcessState
import com.rxlogix.user.User

class ProductAssignmentLog {

    static auditable = false

    String importedFileName
    User importedBy
    Date importedDate
    ProdAssignmentProcessState status

    static mapping = {
        table("PRODUCT_ASSIGNMENT_LOG")
    }

    static constraints = {
        importedDate nullable: true
    }
}
