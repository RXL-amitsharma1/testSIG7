databaseChangeLog = {

    changeSet(author: "Nikhil (generated)", id: "68323253245-1") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'IMPORT_DETAIL', columnName: 'LOG_ID')
        }

        renameColumn(tableName: "IMPORT_DETAIL", oldColumnName: "LOG_ID", newColumnName: "IMPORT_LOG_ID")

    }

}
