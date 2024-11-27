databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "178590699331-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_MULTI_INGREDIENT", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "Amrendra(generated)", id: "178590699331-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_MULTI_INGREDIENT", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "178590699331-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'MASTER_CONFIGURATION', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "MASTER_CONFIGURATION") {
            column(name: "IS_MULTI_INGREDIENT", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }

    }

}