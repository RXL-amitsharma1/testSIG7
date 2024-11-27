databaseChangeLog = {
    changeSet(author: "Krishna Joshi (generated)", id: "1670928280218-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTIONS', columnName: 'EXEC_CONFIG_ID')
            }
        }
        addColumn(tableName: "ACTIONS") {
            column(name: "EXEC_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

}