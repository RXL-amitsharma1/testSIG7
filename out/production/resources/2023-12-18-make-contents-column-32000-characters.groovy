databaseChangeLog = {
    changeSet(author: "Ankit (generated)", id: "1702896147881-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'INBOX_LOG', columnName: 'CONTENT')
        }
        sql("alter table INBOX_LOG modify CONTENT VARCHAR2(32000 CHAR);")
    }
}