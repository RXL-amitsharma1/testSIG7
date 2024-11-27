databaseChangeLog = {
    changeSet(author: "sarthak (generated)", id: "1681809631-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'EVENT_ID')
            }
        }
        addColumn(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "EVENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sarthak (generated)", id: "1681369329-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'EVENT_ID')
            }
        }
        addColumn(tableName: "PRODUCT_EVENT_HISTORY") {
            column(name: "EVENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }
}
