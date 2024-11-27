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
            column(name: "BATCH_ID", type: "varchar2(300 char)") {  constraints(nullable: "false")  }
            column(name: "BATCH_DATE", type: "TIMESTAMP")
            column(name: "COUNT", type: "number(19,0)")
            column(name: "VALID_RECORD_COUNT", type: "number(19,0)")
            column(name: "INVALID_RECORD_COUNT", type: "number(19,0)")
            column(name: "UPLOADED_DATE", type: "TIMESTAMP")
            column(name: "ADDED_BY", type: "varchar2(55 char)")
            column(name: "IS_API_PROCESSED", type: "NUMBER(1, 0)")
            column(name: "IS_ETL_PROCESSED", type: "NUMBER(1, 0)")
            column(name: "ETL_START_DATE", type: "TIMESTAMP")
            column(name: "ETL_STATUS", type: "varchar2(20 char)")
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
            column(name: "PRODUCT_ID", type: "varchar2(300 char)")
            column(name: "PRODUCT", type: "varchar2(300 char)")
            column(name: "DESCRIPTION", type: "varchar2(4000 char)")
            column(name: "BULK_BATCH", type: "varchar2(300 char)")
            column(name: "BULK_BATCH_DATE", type: "varchar2(300 char)")
            column(name: "FILL_BATCH", type: "varchar2(300 char)")
            column(name: "FILL_BATCH_NAME", type: "varchar2(1000 char)")
            column(name: "FILL_EXPIRY", type: "varchar2(300 char)")
            column(name: "FILL_UNITS", type: "varchar2(300 char)")
            column(name: "PACKAGE_BATCH", type: "varchar2(300 char)")
            column(name: "PACKAGE_COUNTRY", type: "varchar2(300 char)")
            column(name: "PACKAGE_UNIT", type: "varchar2(300 char)")
            column(name: "PACKAGE_RELEASE_DATE", type: "varchar2(300 char)")
            column(name: "SHIPPING_BATCH", type: "varchar2(300 char)")
            column(name: "COMPONENT_BATCH", type: "varchar2(300 char)")
            column(name: "DATA_PERIOD", type: "varchar2(300 char)")
            column(name: "UD_FIELD1", type: "varchar2(300 char)")
            column(name: "UD_FIELD2", type: "varchar2(300 char)")
            column(name: "UD_FIELD3", type: "varchar2(300 char)")
            column(name: "UD_FIELD4", type: "varchar2(300 char)")
            column(name: "UD_FIELD5", type: "varchar2(300 char)")
            column(name: "UD_FIELD6", type: "varchar2(300 char)")
            column(name: "UD_FIELD7", type: "varchar2(300 char)")
            column(name: "UD_FIELD8", type: "varchar2(300 char)")
            column(name: "UD_FIELD9", type: "varchar2(300 char)")
            column(name: "UD_FIELD10", type: "varchar2(300 char)")
            column(name: "VALIDATION_ERROR", type: "varchar2(300 char)")
            column(name: "ETL_STATUS", type: "varchar2(20 char)")
            column(name: "BATCH_ID", type: "varchar2(300 char)") {  constraints(nullable: "false")  }
            column(name: "BATCH_DATE", type: "TIMESTAMP")
            column(name: "PRODUCT_HIERARCHY", type: "varchar2(300 char)")
            column(name: "PRODUCT_HIERARCHY_ID", type: "varchar2(300 char)")
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
