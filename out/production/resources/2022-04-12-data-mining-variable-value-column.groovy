databaseChangeLog = {
    changeSet(author: "Krishna (generated)", id: "1651039886-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'DATA_MINING_VARIABLE_VALUE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "DATA_MINING_VARIABLE_VALUE", type: "clob") {
                constraints(nullable: "true")
            }
        }
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'DATA_MINING_VARIABLE_VALUE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DATA_MINING_VARIABLE_VALUE", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
}