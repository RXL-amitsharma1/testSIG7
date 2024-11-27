databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "37283765411-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_HISTORY', columnName:"JUSTIFICATION")
        }
        modifyDataType(columnName: "JUSTIFICATION", newDataType: "VARCHAR(8000)", tableName: "SIGNAL_HISTORY")
    }

    changeSet(author: "anshul (generated)", id: "37283765411-10") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName:"STATUS_COMMENT")
        }
        modifyDataType(columnName: "STATUS_COMMENT", newDataType: "VARCHAR(8000)", tableName: "SIGNAL_STATUS_HISTORY")
    }
}