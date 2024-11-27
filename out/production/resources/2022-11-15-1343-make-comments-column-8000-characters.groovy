databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1668493429135-0102") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'COMMENTS')
        }
        sql("alter table ACTIONS modify COMMENTS VARCHAR2(8000 CHAR);")
    }
}