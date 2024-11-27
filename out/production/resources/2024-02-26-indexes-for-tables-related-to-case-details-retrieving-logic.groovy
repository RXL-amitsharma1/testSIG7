databaseChangeLog = {

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "1708947236-58099") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_arc_sin_case_alert_exec_config_id')
            }
        }
        createIndex(indexName: "idx_arc_sin_case_alert_exec_config_id", tableName: "ARCHIVED_SINGLE_CASE_ALERT",  ) {
            column(name: "EXEC_CONFIG_ID")
        }
    }

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "1708947375-58099") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_case_history_config_id')
            }
        }
        createIndex(indexName: "idx_case_history_config_id", tableName: "CASE_HISTORY", unique: "false") {
            column(name: "CONFIG_ID")
        }
    }

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "170774164-58099") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_ex_rconfig_config_id')
            }
        }
        createIndex(indexName: "idx_ex_rconfig_config_id", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "CONFIG_ID")
        }
    }

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "1708947649-58099") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_ex_status_executed_config_id')
            }
        }
        createIndex(indexName: "idx_ex_status_executed_config_id", tableName: "EX_STATUS", unique: "false") {
            column(name: "EXECUTED_CONFIG_ID")
        }
    }

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "1708947713-58099") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_rconfig_type')
            }
        }
        createIndex(indexName: "idx_rconfig_type", tableName: "RCONFIG", unique: "false") {
            column(name: "TYPE")
        }
    }

    changeSet(author: "Yogesh Kumar", id: "1710488359642-1") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'RCONFIG' AND column_name = 'PRIORITY_ID' ;")
        }
        sql("ALTER TABLE RCONFIG MODIFY (PRIORITY_ID NULL);")
    }

    changeSet(author: "Yogesh Kumar", id: "1710488359642-2") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'EX_RCONFIG' AND column_name = 'PRIORITY_ID' ;")
        }
        sql("ALTER TABLE EX_RCONFIG MODIFY (PRIORITY_ID NULL);")
    }

    changeSet(author: "Yogesh Kumar", id: "1710488359642-4") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'EVDAS_CONFIG' AND column_name = 'PRIORITY_ID' ;")
        }
        sql("ALTER TABLE EVDAS_CONFIG MODIFY (PRIORITY_ID NULL);")
    }

    changeSet(author: "Yogesh Kumar", id: "1710488359642-5") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'EX_EVDAS_CONFIG' AND column_name = 'PRIORITY_ID' ;")
        }
        sql("ALTER TABLE EX_EVDAS_CONFIG MODIFY (PRIORITY_ID NULL);")
    }
}
