databaseChangeLog = {

    changeSet(author: "Kundan.Kumar (generated)", id: "1608626578697-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'BATCH_LOT_STATUS')
            }
        }
        createTable(tableName: "BATCH_LOT_STATUS") {
            column(name: "id", type: "number(19,0)") {  constraints(nullable: "false", primaryKey: "true", primaryKeyName: "BATCH_LOT_STATUS_PK")  }
            column(name: "VERSION", type: "number(19,0)") { constraints(nullable: "false") }
            column(name: "BATCH_ID", type: "VARCHAR(300 )") {  constraints(nullable: "false")  }
            column(name: "BATCH_DATE", type: "TIMESTAMP")
            column(name: "COUNT", type: "number(19,0)")
            column(name: "VALID_RECORD_COUNT", type: "number(19,0)")
            column(name: "INVALID_RECORD_COUNT", type: "number(19,0)")
            column(name: "UPLOADED_DATE", type: "TIMESTAMP")
            column(name: "ADDED_BY", type: "VARCHAR(55 )")
            column(name: "IS_API_PROCESSED", type: "boolean")
            column(name: "IS_ETL_PROCESSED", type: "boolean")
            column(name: "ETL_START_DATE", type: "TIMESTAMP")
            column(name: "ETL_STATUS", type: "VARCHAR(20 )")
        }
    }
    changeSet(author: "Kundan.Kumar (generated)", id: "1608626578697-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'BATCH_LOT_STATUS_SEQ')
            }
        }
        createSequence(sequenceName: "BATCH_LOT_STATUS_SEQ")
    }
    changeSet(author: "Kundan.Kumar (generated)", id: "1608626578697-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'BATCH_LOT_DATA')
            }
        }
        createTable(tableName: "BATCH_LOT_DATA") {
            column(name: "id", type: "number(19,0)") {  constraints(nullable: "false", primaryKey: "true", primaryKeyName: "BATCH_LOT_DATA_PK")  }
            column(name: "BATCH_LOT_ID", type: "number(19,0)") {  constraints(nullable: "true")  }
            column(name: "VERSION", type: "number(19,0)") { constraints(nullable: "false") }
            column(name: "PRODUCT_ID", type: "VARCHAR(300 )")
            column(name: "PRODUCT", type: "VARCHAR(300 )")
            column(name: "DESCRIPTION", type: "VARCHAR(4000 )")
            column(name: "BULK_BATCH", type: "VARCHAR(300 )")
            column(name: "BULK_BATCH_DATE", type: "VARCHAR(300 )")
            column(name: "FILL_BATCH", type: "VARCHAR(300 )")
            column(name: "FILL_BATCH_NAME", type: "VARCHAR(1000 )")
            column(name: "FILL_EXPIRY", type: "VARCHAR(300 )")
            column(name: "FILL_UNITS", type: "VARCHAR(300 )")
            column(name: "PACKAGE_BATCH", type: "VARCHAR(300 )")
            column(name: "PACKAGE_COUNTRY", type: "VARCHAR(300 )")
            column(name: "PACKAGE_UNIT", type: "VARCHAR(300 )")
            column(name: "PACKAGE_RELEASE_DATE", type: "VARCHAR(300 )")
            column(name: "SHIPPING_BATCH", type: "VARCHAR(300 )")
            column(name: "COMPONENT_BATCH", type: "VARCHAR(300 )")
            column(name: "DATA_PERIOD", type: "VARCHAR(300 )")
            column(name: "UD_FIELD1", type: "VARCHAR(300 )")
            column(name: "UD_FIELD2", type: "VARCHAR(300 )")
            column(name: "UD_FIELD3", type: "VARCHAR(300 )")
            column(name: "UD_FIELD4", type: "VARCHAR(300 )")
            column(name: "UD_FIELD5", type: "VARCHAR(300 )")
            column(name: "UD_FIELD6", type: "VARCHAR(300 )")
            column(name: "UD_FIELD7", type: "VARCHAR(300 )")
            column(name: "UD_FIELD8", type: "VARCHAR(300 )")
            column(name: "UD_FIELD9", type: "VARCHAR(300 )")
            column(name: "UD_FIELD10", type: "VARCHAR(300 )")
            column(name: "VALIDATION_ERROR", type: "VARCHAR(300 )")
            column(name: "ETL_STATUS", type: "VARCHAR(20 )")
            column(name: "BATCH_ID", type: "VARCHAR(300 )") {  constraints(nullable: "false")  }
            column(name: "BATCH_DATE", type: "TIMESTAMP")
            column(name: "PRODUCT_HIERARCHY", type: "VARCHAR(300 )")
            column(name: "PRODUCT_HIERARCHY_ID", type: "VARCHAR(300 )")
        }
    }
    changeSet(author: "Kundan.Kumar", id: "1608626578697-4") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK_BATCH_LOT_DATA1")
        }
        addForeignKeyConstraint(baseColumnNames: "BATCH_LOT_ID", baseTableName: "BATCH_LOT_DATA", constraintName: "FK_BATCH_LOT_DATA1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BATCH_LOT_STATUS", referencesUniqueColumn: "false")
    }
    changeSet(author: "Kundan.Kumar (generated)", id: "1608626578697-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'BATCH_LOT_DATA_SEQ')
            }
        }
        createSequence(sequenceName: "BATCH_LOT_DATA_SEQ")
    }

}
