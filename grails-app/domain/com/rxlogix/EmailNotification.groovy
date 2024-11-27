package com.rxlogix

class EmailNotification implements  Serializable{
    String moduleName
    String key
    Boolean isEnabled
    Boolean defaultValue

    static auditable = true

    static mapping = {
        table("EMAIL_NOTIFICATION")
        moduleName(column: "MODULE_NAME")
        isEnabled(column: "IS_ENABLED")
        key(column: "KEY")
        defaultValue(column: 'DEFAULT_VALUE')
    }

    static constraints = {
        moduleName nullable: false
        isEnabled nullable: false
        key nullable: false
        defaultValue nullable: false
    }

    def getInstanceIdentifierForAuditLog() {
        return moduleName ? moduleName : key
    }
}
