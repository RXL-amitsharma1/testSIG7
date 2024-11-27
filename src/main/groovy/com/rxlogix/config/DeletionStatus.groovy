package com.rxlogix.config

enum DeletionStatus {
    READY_TO_DELETE("ready to delete"),
    DELETION_IN_PROGRESS("deletion in progress"),
    DELETED("completely deleted"),
    PARTIALLY_DELETED("partially deleted"),
    ERROR("error")
    private final String val

    DeletionStatus(String val){
        this.val = val
    }

    String value() { return val }


    String getKey(){
        name()
    }

    public getI18nKey() {
        return "app.deletion.status.${this.name()}"
    }

}