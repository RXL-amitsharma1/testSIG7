databaseChangeLog = {

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "1707741177-57097") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_alert_comment')
            }
        }
        createIndex(indexName: "idx_alert_comment", tableName: "ALERT_COMMENT", unique: "false") {
            column(name: "product_id")
            column(name: "config_id")
            column(name: "alert_type")
        }
    }

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "170774148-57097") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_alert_comment_hist_product')
            }
        }
        createIndex(indexName: "idx_alert_comment_hist_product", tableName: "ALERT_COMMENT_HISTORY", unique: "false") {
            column(name: "product_id")
        }
    }

    changeSet(author: "Rxl-Dmitry-Razorvin", id: "170774164-57097") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_alert_comment_history_event')
            }
        }
        createIndex(indexName: "idx_alert_comment_history_event", tableName: "ALERT_COMMENT_HISTORY", unique: "false") {
            column(name: "event_id")
        }
    }
}
