databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "1613989815343-501") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: "SIGNAL_RMMS", columnName: 'communication_rmm_id')
            }
        }

        addColumn(tableName: "SIGNAL_RMMS") {
            column(name: "communication_rmm_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }
}