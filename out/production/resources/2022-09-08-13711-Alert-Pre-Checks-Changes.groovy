databaseChangeLog = {

    changeSet(author: "nikhilkhari (generated)", id: "1473245678582-471") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_PRE_EXECUTION_CHK')
            }
        }
        createTable(tableName: "ALERT_PRE_EXECUTION_CHK") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_PRE_CHECKPK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PVR_CHK_ENABLED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_VER_ASOF_CHK_ENABLED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ETL_FAIL_CHK_ENABLED", type: "number(1,0)") {
                constraints(nullable: "false")
            }


            column(name: "IS_ETL_INPROG_CHK_ENABLED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED_FOR_MASTER_CFG", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "417223456762-467") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_PRE_CHECK_HISTORY')
            }
        }
        createTable(tableName: "ALERT_PRE_CHECK_HISTORY") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_PRE_CHECK_HISTPK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_pre_execution_check_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "pvr_check_updated", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "new_pvr_check_val", type: "number(1,0)") {
                constraints(nullable: "true")
            }


            column(name: "ver_asof_check_updated", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "new_ver_asof_check_val", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "etl_fail_check_updated", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "new_etl_fail_check_val", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "etl_in_prog_check_updated", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "new_etl_in_prog_check", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "121391208765-67"){
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK_fv12dsretryuhgfsdf')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "alert_pre_execution_check_id", baseTableName: "ALERT_PRE_CHECK_HISTORY", constraintName: "FK_fv12dsretryuhgfsdf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_PRE_EXECUTION_CHK", referencesUniqueColumn: "false")
    }


    changeSet(author: "nikhilkhari (generated)", id: "14725098756987-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AUTO_ADJUSTMENT_RULE')
            }
        }
        createTable(tableName: "AUTO_ADJUSTMENT_RULE") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUTO_ADJUSTMENTPK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }


            column(name: "alert_type", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "adjustment_type_enum", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "417223243567-437") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AUTO_ADJUSTMENT_HISTORY')
            }
        }
        createTable(tableName: "AUTO_ADJUSTMENT_HISTORY") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUTO_ADJUST_HISTPK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "auto_adjustment_rule_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "adjustment_enum_updated", type: "number(1,0)") {
                constraints(nullable: "false")
            }
            column(name: "old_adjustment_enum_value", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "new_adjustment_enum_value", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "is_enabled_flag_updated", type: "number(1,0)") {
                constraints(nullable: "false")
            }


            column(name: "new_enabled_flag_value", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "12987654517-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK_fv12ds6o3asasdqw12dsa')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "auto_adjustment_rule_id", baseTableName: "AUTO_ADJUSTMENT_HISTORY", constraintName: "FK_fv12ds6o3asasdqw12dsa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AUTO_ADJUSTMENT_RULE", referencesUniqueColumn: "false")
    }

    changeSet(author: "nikhilkhari (generated)", id: "6821936123123-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SKIPPED_ALERT_DATE_RANGE')
            }
        }
        createTable(tableName: "SKIPPED_ALERT_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SKIPPED_ALERT_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "23231232382-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SKIPPED_ALERT_INFORMATION')
            }
        }
        createTable(tableName: "SKIPPED_ALERT_INFORMATION") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SKIPPED_ALERT_INFOPK")
            }

            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "config_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "SKIPPED_ALERT_DATE_RNG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
            column(name: "EVALUATE_DATE_AS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
            column(name: "NEXT_RUN_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
            column(name: "EX_CONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "ADJUSTMENT_TYPE_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "STATE_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "GROUP_CODE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "ALERT_DISABLE_REASON", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "2013122121112-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKncksxdas12xdqwda_47')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SKIPPED_ALERT_DATE_RNG_ID", baseTableName: "SKIPPED_ALERT_INFORMATION", constraintName: "FKncksxdas12xdqwda_47", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SKIPPED_ALERT_DATE_RANGE")
    }


// New columns added in configuration table


    changeSet(author: "nikhilkhari (generated)", id: "14654612570-471") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_AUTO_PAUSED')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_AUTO_PAUSED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nikhilkhari (generated)", id: "14654612570-472") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_MANUALLY_PAUSED')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_MANUALLY_PAUSED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "146542134430-473") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ADJUSTMENT_TYPE_ENUM')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ADJUSTMENT_TYPE_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nikhilkhari (generated)", id: "1465461253420-478") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'SKIPPED_ALERT_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "SKIPPED_ALERT_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nikhilkhari (generated)", id: "14654612570-473") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'SKIPPED_ALERT_GROUP_CODE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "SKIPPED_ALERT_GROUP_CODE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "nikhilkhari (generated)", id: "14632234240-174") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'FUTURE_SCHEDULE_DATE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "FUTURE_SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "234567887654-151") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'AUTO_PAUSED_EMAIL_SENT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "AUTO_PAUSED_EMAIL_SENT", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "234567887654-152") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_DISABLE_REASON')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_DISABLE_REASON", type: "VARCHAR2(1024 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "14768531443-473") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ADJUSTMENT_TYPE_ENUM')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ADJUSTMENT_TYPE_ENUM", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nikhilkhari (generated)", id: "14687335231320-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SKIPPED_ALERT_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SKIPPED_ALERT_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "nikhilkhari (generated)", id: "14654612570-475") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SKIPPED_ALERT_GROUP_CODE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SKIPPED_ALERT_GROUP_CODE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }


//     New columns added in evdas configuration table


    changeSet(author: "nikhilkhari (generated)", id: "65226152710-471") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'IS_AUTO_PAUSED')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "IS_AUTO_PAUSED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nikhilkhari (generated)", id: "65226152710-472") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'IS_MANUALLY_PAUSED')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "IS_MANUALLY_PAUSED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "nikhilkhari (generated)", id: "23213123130-175") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'FUTURE_SCHEDULE_DATE')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "FUTURE_SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "234567887654-153") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'AUTO_PAUSED_EMAIL_SENT')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "AUTO_PAUSED_EMAIL_SENT", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "234567887654-154") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'ALERT_DISABLE_REASON')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "ALERT_DISABLE_REASON", type: "VARCHAR2(1024 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "nikhilkhari (generated)", id: "1354657760-472") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'IS_MANUALLY_PAUSED')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "IS_MANUALLY_PAUSED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "23213325732-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'FUTURE_SCHEDULE_DATE')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "FUTURE_SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    //     New columns added in master configuration table


    changeSet(author: "nikhilkhari (generated)", id: "612341343240-145") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'MASTER_CONFIGURATION', columnName: 'IS_AUTO_PAUSED')
            }
        }
        addColumn(tableName: "MASTER_CONFIGURATION") {
            column(name: "IS_AUTO_PAUSED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "234567887654-155") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'MASTER_CONFIGURATION', columnName: 'AUTO_PAUSED_EMAIL_SENT')
            }
        }
        addColumn(tableName: "MASTER_CONFIGURATION") {
            column(name: "AUTO_PAUSED_EMAIL_SENT", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nikhilkhari (generated)", id: "234567887654-156") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'MASTER_CONFIGURATION', columnName: 'ALERT_DISABLE_REASON')
            }
        }
        addColumn(tableName: "MASTER_CONFIGURATION") {
            column(name: "ALERT_DISABLE_REASON", type: "VARCHAR2(1024 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

}