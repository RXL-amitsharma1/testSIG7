databaseChangeLog = {
    changeSet(author: "RxL-Eugen-Semenov", id: "170774164-57098") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_agg_alert_name')
            }
        }
        createIndex(indexName: "idx_agg_alert_name", tableName: "AGG_ALERT", unique: "false") {
            column(name: "name")
        }
    }

    changeSet(author: "RxL-Eugen-Semenov", id: "170774164-57099") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_arch_agg_alert_name')
            }
        }
        createIndex(indexName: "idx_arch_agg_alert_name", tableName: "ARCHIVED_AGG_ALERT", unique: "false") {
            column(name: "name")
        }
    }
}