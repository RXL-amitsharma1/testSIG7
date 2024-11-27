databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1668503802459-1009") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERTS', columnName: 'NOTES')
        }
        sql("alter table ALERTS modify NOTES VARCHAR2(8000 CHAR);")
    }
}
