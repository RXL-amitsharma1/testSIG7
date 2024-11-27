package com.rxlogix.config

enum AlertDeletionStatus {

    CREATED("created"),
    READY_TO_DELETE("ready to delete"),
    DELETION_IN_PROGRESS("deletion in progress"),
    DELETED("deleted"),
    ERROR("error")
    private final String val

    AlertDeletionStatus(String val) {
        this.val = val
    }

    String value() { return val }


    String getKey(){
        name()
    }

    public getI18nKey() {
        return "app.alert.deletion.status.${this.name()}"
    }

}