databaseChangeLog = {
    changeSet(author: "Krishan (generated)", id: "37283791301-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'JSON_FIELD')
            }
        }

        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "JSON_FIELD", type: "CLOB") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "ujjwal (generated)", id: "37283791301-4") {

        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'JSON_FIELD')
            }
        }

        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "JSON_FIELD", type: "CLOB") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "rahul (generated)", id: "37283791301-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTIONS', columnName: 'notification_date')
            }
        }
        addColumn(tableName: "ACTIONS") {
            column(name: "notification_date", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "37283791301-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_arch_agg_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_arch_agg_alert_exconfig", tableName: "ARCHIVED_AGG_ALERT", unique: "false") {
            column(name: "exec_configuration_id")
        }
    }

}