package com.rxlogix.config

class ImportDetail {
    static auditable = false
    Long id

    int recNum

    //Alert name or chronicle id
    String inputIdentifier
    String message

    ImportLog importLog

    static mapping = {table name: "IMPORT_DETAIL"}

    static constraints = {
        inputIdentifier nullable: false, maxSize: 4000
        message nullable: true, maxSize: 4000
    }
}
