databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id:"19976543213-345") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName:"TEMPLT-QUERY-INDEX")
            }
        }
        createIndex(indexName: "TEMPLT-QUERY-INDEX", tableName: "TEMPLT_QUERY", unique: "false") {
            column(name: "RPT_TEMPLT_ID")
            column(name: "SUPER_QUERY_ID")
        }
    }
    changeSet(author: "Gaurav (generated)", id:"199765432131-345") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName:"EX-TEMPLT-QUERY-INDEX")
            }
        }
        createIndex(indexName: "EX-TEMPLT-QUERY-INDEX", tableName: "EX_TEMPLT_QUERY", unique: "false") {
            column(name: "EX_TEMPLT_ID")
            column(name: "EX_QUERY_ID")
        }
    }
}