databaseChangeLog = {

    changeSet(author: "shivam (generated)", id: "1668509331050-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'deletion_In_Progress')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "DELETION_IN_PROGRESS", type: "BOOLEAN", defaultValue: "FALSE"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "1668509331050-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'deletion_Status')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "deletion_status", type: "varchar(255)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "1668509331050-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_DELETION_DATA')
            }
        }
        createTable(tableName: "ALERT_DELETION_DATA") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_DELETION_PKY")
            }
            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "config_Id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "ex_Config_Id", type: "varchar(8000)"){
                constraints(nullable: "false")
            }
            column(name: "alert_Type", type:  "varchar(8000)")
            column(name: "justification", type:  "varchar(8000)"){
                constraints(nullable: "false")
            }
            column(name: "is_master", type: "boolean", defaultValue: "false"){
                constraints(nullable: "false")
            }
            column(name: "deletion_Status", type:  "varchar(255)"){
                constraints(nullable: "false")
            }
            column(name: "deletion_completed", type: "boolean", defaultValue: "false"){
                constraints(nullable: "false")
            }
            column(name: "date_created", type: "timestamp"){
                constraints(nullable: "false")
            }
            column(name: "last_updated", type: "timestamp"){
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "shivam (generated)", id: "1668509331050-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_DELETE_ENTRY')
            }
        }
        createTable(tableName: "ALERT_DELETE_ENTRY") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_DEL_PK")
            }
            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "ex_Config_Id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "pvr_Completed", type: "boolean", defaultValue: "false"){
                constraints(nullable: "false")
            }
            column(name: "spotfire_Completed", type: "boolean", defaultValue: "false"){
                constraints(nullable: "false")
            }
            column(name: "db_Completed", type: "boolean", defaultValue: "false"){
                constraints(nullable: "false")
            }
            column(name: "is_Deletion_Completed", type: "boolean", defaultValue: "false"){
                constraints(nullable: "false")
            }
            column(name: "data_Migration_Completed", type: "boolean", defaultValue: "false"){
                constraints(nullable: "false")
            }
            column(name: "alert_Deletion_Status", type:  "varchar(255)"){
                constraints(nullable: "false")
            }
            column(name: "date_created", type: "timestamp"){
                constraints(nullable: "false")
            }
            column(name: "last_updated", type: "timestamp"){
                constraints(nullable: "false")
            }
            column(name: "ALERT_DELETION_DATA_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "shivamvashist (generated)", id: "1668509331050-5"){
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK_fv12dsretakjsndajs')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ALERT_DELETION_DATA_ID", baseTableName: "ALERT_DELETE_ENTRY", constraintName: "FK_fv12dsretakjsndajs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DELETION_DATA", referencesUniqueColumn: "false")
    }

    changeSet(author: "shivamvashist (generated)", id: "1723016844-002") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveDataFromArchiveTables.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }

    changeSet(author: "Krishna Joshi (generated)", id: "1723016844-003") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveMasterAlertDataFromArchiveTables.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }
    changeSet(author: "shivamvashist (generated)", id: "1668677710593-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'deletion_In_Progress')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "DELETION_IN_PROGRESS", type: "BOOLEAN", defaultValue: "FALSE"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "shivamvashist (generated)", id: "1668677710593-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'deletion_Status')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "deletion_status", type: "varchar(255)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "shivamvashist (generated)", id: "1668509331050-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'deletion_In_Progress')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "DELETION_IN_PROGRESS", type: "BOOLEAN", defaultValue: "FALSE"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "shivamvashist (generated)", id: "1668762613374-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'deletion_Status')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "deletion_status", type: "varchar(255)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "147324567892-665") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ACTION_JUSTIFICATION')
            }
        }
        createTable(tableName: "ACTION_JUSTIFICATION") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_JUSTIFICATIONPK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TYPE", type: "VARCHAR(255 )") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR(255 )") {
                constraints(nullable: "false")
            }

            column(name: "POSTER_CLASS", type: "VARCHAR(255 )") {
                constraints(nullable: "false")
            }

            column(name: "ATTRIBUTES_MAP", type: "VARCHAR(8000 )") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "VARCHAR(255 )") {
                constraints(nullable: "false")
            }

        }
    }

}