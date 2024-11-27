databaseChangeLog = {

    changeSet(author: "Siddharth Singh", id: "230905181650-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'MASTER_CHILD_RUN_NODE', columnName: 'EX_EVDAS_ID')
            }
        }
        addColumn(tableName: "MASTER_CHILD_RUN_NODE") {
            column(name: "EX_EVDAS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }
}
