package com.rxlogix.config

import com.rxlogix.util.DbUtil

class RuleMigrationStatus {
    // Make Entries in this table for migration through UI with error where data loss may occur
    Long entityId
    String entityClass // Domain name should be inserted here
    String datasource
    boolean isMigrationCompleted = false
    String error
    String initialData
    String migratedData
    Date dateCreated
    Date lastUpdated

    static constraints = {
        error nullable: true, blank: true
        datasource nullable: true, blank: true
        initialData nullable: true, blank: true
        migratedData nullable: true, blank: true
    }
    static mapping = {
        table name: "RULE_MIGRATION_STATUS"
        initialData column: "initial_data", sqlType: DbUtil.longStringType
        migratedData column: "migrated_data", sqlType: DbUtil.longStringType
    }

    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }
}
