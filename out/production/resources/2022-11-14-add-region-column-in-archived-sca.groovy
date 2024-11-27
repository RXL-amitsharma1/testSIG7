databaseChangeLog = {
    changeSet(author: "Krishan Joshi (generated)", id: "1668433144370-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'REGION')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "REGION", type: "varchar2(256 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Krishan Joshi (generated)", id: "1668433144370-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'JSON_FIELD')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "JSON_FIELD", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishan Joshi (generated)", id: "1668689484825-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'IS_SAFETY')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "IS_SAFETY", type: "number(10, 0)", defaultValue: "1") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "Krishan Joshi (generated)", id: "1668433144370-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_LITERATURE_ALERT', columnName: 'DISP_PERFORMED_BY')
            }
        }
        addColumn(tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(name: "DISP_PERFORMED_BY", type: "varchar2(256 char)") {
                constraints(nullable: "true")
            }
        }
    }

}