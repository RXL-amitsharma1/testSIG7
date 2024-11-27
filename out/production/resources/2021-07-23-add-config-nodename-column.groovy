databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1627037810531-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'node_name')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "node_name", type: "varchar2(255 CHAR)")
        }
    }
}
