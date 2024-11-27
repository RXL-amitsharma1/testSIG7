databaseChangeLog={
    changeSet(author: "Mohit Kumar (generated)", id: "16831840325678-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_HISTORY', columnName: 'CREATED_TIMESTAMP')
            }
        }
        sql("alter table CASE_HISTORY add CREATED_TIMESTAMP TIMESTAMP(6) NULL;")
    }

    changeSet(author: "Mohit Kumar (generated)", id: "16831840325678-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'CREATED_TIMESTAMP')
            }
        }
        sql("alter table PRODUCT_EVENT_HISTORY add CREATED_TIMESTAMP TIMESTAMP(6) NULL;")
    }

    changeSet(author: "Mohit Kumar (generated)", id: "16831840325678-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_HISTORY', columnName: 'CREATED_TIMESTAMP')
            }
        }
        sql("alter table EVDAS_HISTORY add CREATED_TIMESTAMP TIMESTAMP(6) NULL;")
    }

    changeSet(author: "Mohit Kumar (generated)", id: "16831840325678-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_HISTORY', columnName: 'CREATED_TIMESTAMP')
            }
        }
        sql("alter table SIGNAL_HISTORY add CREATED_TIMESTAMP TIMESTAMP(6) NULL;")
    }

    changeSet(author: "Mohit Kumar (generated)", id: "16831840325678-6") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'TEMPLT_QUERY', columnName: 'QUERY_NAME')
        }
        sql("alter table TEMPLT_QUERY modify QUERY_NAME VARCHAR2(1000 CHAR);")
    }


}