databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1668597948631-010087") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'COMMENT_SIGNAL_STATUS')
        }
        sql("alter table VALIDATED_SIGNAL modify COMMENT_SIGNAL_STATUS VARCHAR2(8000 CHAR);")
    }
}