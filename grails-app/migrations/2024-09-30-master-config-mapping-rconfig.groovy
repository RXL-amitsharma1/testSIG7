databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "19976543213-358") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'MASTER_CONFIG_MAPPING')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "MASTER_CONFIG_MAPPING", type: "VARCHAR(8000)"){
                constraints(nullable: "true")
            }
        }
    }
}