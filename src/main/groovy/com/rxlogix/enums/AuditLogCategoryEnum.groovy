package com.rxlogix.enums


public enum AuditLogCategoryEnum {

    LOGIN("Authentication Failure"),
    CREATED("Record Created"),
    MODIFIED("Record Modified"),
    DELETED("Record Deleted"),
    LOGIN_SUCCESS("Login Successful"),
    LOGIN_FAIL('Login failed')

    final String value

    AuditLogCategoryEnum(String value){
        this.value = value
    }

    //Used to get to values for dropdown lists
    String toString() {
        value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    //Used to match up a value from the DB against the enum
    static String getValue(String theValue) {
        for (AuditLogCategoryEnum theEnum : values()){
            String name = theEnum.name();
            if (theValue == name) {
                return (theEnum.value)
            }
        }
        return ""
    }

    //Return a style to use for Twitter Bootstrap labels
    static getAlertLabelStyle(AuditLogCategoryEnum theValue) {

        switch(theValue) {
            case CREATED:
                return "success"
            case MODIFIED:
                return "primary"
            case DELETED:
                return "warning"
            case LOGIN:
                return "danger"
            default:
                return "default"
        }

    }

}
