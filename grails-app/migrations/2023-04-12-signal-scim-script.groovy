databaseChangeLog =  {
    changeSet(author: "sarthak (generated)", id: "1678923383565-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'SCIM_ID')
            }
        }
        addColumn(tableName: "PVUSER") {
            column(name: "SCIM_ID", type: "VARCHAR(255 )"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sarthak (generated)", id: "1678923483565-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'SCIM_ID')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "SCIM_ID", type: "VARCHAR(255 )"){
                constraints(nullable: "true")
            }
        }
    }
}