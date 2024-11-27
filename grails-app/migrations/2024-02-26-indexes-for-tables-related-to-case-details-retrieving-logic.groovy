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
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'rconfig' AND column_name = 'priority_id';")
        }
        sql("ALTER TABLE rconfig ALTER COLUMN priority_id DROP NOT NULL;")
    }


    changeSet(author: "Yogesh Kumar", id: "1710488359642-2") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'ex_rconfig' AND column_name = 'priority_id';")
        }
        sql("ALTER TABLE ex_rconfig ALTER COLUMN priority_id DROP NOT NULL;")
    }


    changeSet(author: "Yogesh Kumar", id: "1710488359642-4") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'evdas_config' AND column_name = 'priority_id';")
        }
        sql("ALTER TABLE evdas_config ALTER COLUMN priority_id DROP NOT NULL;")
    }


    changeSet(author: "Yogesh Kumar", id: "1710488359642-5") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'ex_evdas_config' AND column_name = 'priority_id';")
        }
        sql("ALTER TABLE ex_evdas_config ALTER COLUMN priority_id DROP NOT NULL;")
    }

}
