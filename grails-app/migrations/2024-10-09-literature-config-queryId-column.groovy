databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "19976543213-359") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'LITERATURE_QUERY_ID')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "LITERATURE_QUERY_ID", type: "NUMBER"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Gaurav (generated)", id: "19976543213-360") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'LITERATURE_QUERY_ID')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "LITERATURE_QUERY_ID", type: "NUMBER"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Gaurav (generated)", id: "19976543213-361") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'LITERATURE_QUERY_NAME')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "LITERATURE_QUERY_NAME", type: "VARCHAR(4000)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Gaurav (generated)", id: "19976543213-362") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'LITERATURE_QUERY_NAME')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "LITERATURE_QUERY_NAME", type: "VARCHAR(4000)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shoaib Akhtar (generated)", id: "19913543213-909"){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'CO_PACKAGED_PRODUCT')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "CO_PACKAGED_PRODUCT", type: "VARCHAR(255)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shoaib Akhtar (generated)", id: "19913543213-910") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'CO_PACKAGED_PRODUCT')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "CO_PACKAGED_PRODUCT", type: "VARCHAR(255)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Shoaib Akhtar (generated)", id: "19913543213-911") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'CO_PACKAGED_PRODUCT')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "CO_PACKAGED_PRODUCT", type: "VARCHAR(255)"){
                constraints(nullable: "true")
            }
        }
    }
}