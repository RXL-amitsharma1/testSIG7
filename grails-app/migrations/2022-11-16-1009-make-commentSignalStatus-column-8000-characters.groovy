databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1668597948631-010087") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'COMMENT_SIGNAL_STATUS')
        }
        sql("alter table VALIDATED_SIGNAL ALTER COLUMN COMMENT_SIGNAL_STATUS TYPE VARCHAR(8000 );")
    }
}