databaseChangeLog = {
    changeSet(author: "amrendra (generated)", id: "1624861949-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_susp_prod_alert_id')
            }
        }
        createIndex(indexName: "idx_susp_prod_alert_id", tableName: "SINGLE_ALERT_SUSP_PROD", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1624861949-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_conmed_alert_id')
            }
        }
        createIndex(indexName: "idx_conmed_alert_id", tableName: "SINGLE_ALERT_CON_COMIT", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1624861949-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_allpt_alert_id')
            }
        }
        createIndex(indexName: "idx_allpt_alert_id", tableName: "SINGLE_ALERT_PT", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1624861949-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_patmed_alert_id')
            }
        }
        createIndex(indexName: "idx_patmed_alert_id", tableName: "SINGLE_ALERT_PAT_MED_HIST", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1624861949-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_patdrugs_alert_id')
            }
        }
        createIndex(indexName: "idx_patdrugs_alert_id", tableName: "SINGLE_ALERT_PAT_HIST_DRUGS", unique: "false") {
            column(name: "single_alert_id")
        }
    }
}
