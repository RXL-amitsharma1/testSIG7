databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "19976543213-370") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_ALERT', columnName: 'CITATION')
            }
        }
        addColumn(tableName: "LITERATURE_ALERT") {
            column(name: "CITATION", type: "VARCHAR(32000)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Gaurav (generated)", id: "19976543213-371") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_LITERATURE_ALERT', columnName: 'CITATION')
            }
        }
        addColumn(tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(name: "CITATION", type: "VARCHAR(32000)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Gaurav (generated)", id: "19976543213-372") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PREFERENCE', columnName: 'IS_LITERATURE_ABSTRACT_VIEW')
            }
        }
        addColumn(tableName: "PREFERENCE") {
            column(name: "IS_LITERATURE_ABSTRACT_VIEW", type: "BOOLEAN", defaultValueBoolean: false){
                constraints(nullable: "true")
            }
        }
    }
}