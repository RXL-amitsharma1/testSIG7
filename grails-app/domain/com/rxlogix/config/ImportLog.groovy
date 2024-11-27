package com.rxlogix.config

class ImportLog {
    static auditable = false
    Long id
    String type
    Date startTime
    Date endTime
    String response
    int numSucceeded
    int numFailed

    static hasMany = [details: ImportDetail]

    static mapping = {table name: "IMPORT_LOG"}

    static constraints = {
        endTime nullable: true
        response nullable: true
    }

}
