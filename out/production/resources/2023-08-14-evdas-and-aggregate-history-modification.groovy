databaseChangeLog = {
    changeSet(author: "Siddharth", id: "14082023160555-7") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_HISTORY', columnName: 'MODIFIED_BY')
        }
        sql("UPDATE EVDAS_HISTORY eh SET eh.MODIFIED_BY = (SELECT u.FULL_NAME FROM PVUSER u WHERE eh.MODIFIED_BY = u.USERNAME) WHERE EXISTS (SELECT 1 FROM PVUSER u WHERE eh.MODIFIED_BY = u.USERNAME);")
    }

    changeSet(author: "Siddharth", id: "14082023205025-7") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'MODIFIED_BY')
        }
        sql("UPDATE PRODUCT_EVENT_HISTORY peh SET peh.MODIFIED_BY = (SELECT u.FULL_NAME FROM PVUSER u WHERE peh.MODIFIED_BY = u.USERNAME) WHERE EXISTS (SELECT 1 FROM PVUSER u WHERE peh.MODIFIED_BY = u.USERNAME);")
    }
}