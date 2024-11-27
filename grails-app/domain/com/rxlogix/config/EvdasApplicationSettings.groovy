package com.rxlogix.config

/**
 * Contains application settings configured
 */
class EvdasApplicationSettings {

    Boolean evdasErmrUploadLocked = false
    Boolean evdasCaseListingUploadLocked = false

    static mapping = {
        table name: "EVDAS_APPLICATION_SETTING"
        evdasErmrUploadLocked column: "ERMR_LOCKED"
        evdasCaseListingUploadLocked column: "CASE_LISTING_LOCKED"
    }
    static constraints = {
        evdasErmrUploadLocked nullable: false
        evdasCaseListingUploadLocked nullable: false
    }
}
