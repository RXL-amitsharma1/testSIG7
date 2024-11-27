package com.rxlogix.user

class UserDepartment implements Serializable {
//    static auditable = true // removed as required in PVS-47032
    String departmentName

    static constraints = {
        departmentName(unique: true, nullable: true)
    }
    static mapping = {
        sort "departmentName"
    }

    @Override
    String toString() { "${departmentName}" }

    def getInstanceIdentifierForAuditLog() {
        return departmentName
    }
}
