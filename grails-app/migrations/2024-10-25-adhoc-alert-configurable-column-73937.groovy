databaseChangeLog = {
    changeSet(author: "Mohit (generated)", id: "19976545678-371") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'view_instance', columnName: 'selected_dispositions')
            }
        }
        addColumn(tableName: "view_instance") {
            column(name: "selected_dispositions", type: "VARCHAR(4000)"){
                constraints(nullable: "true")
            }
        }
    }
}