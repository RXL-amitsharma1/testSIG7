databaseChangeLog = {
    changeSet(author: "Krishna (generated)", id: "1666271101000-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_FG_QUERY_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_FG_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_FG_QUERY_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALERT_FG_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Krishna (generated)", id: "1666271101000-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'FG_QUERY_NAME')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "FG_QUERY_NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'FG_QUERY_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "FG_QUERY_NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishna (generated)", id: "1666271101000-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_QUERY_FG_VALUES')
            }
        }
        createTable(tableName: "ALERT_QUERY_FG_VALUES") {
            column(name: "ALERT_FG_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FG_QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "FG_QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }


    changeSet(author: "Krishan (generated)", id: "1666271101000-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'EX_FG_ALERT_QUERY_VALUES')
            }
        }
        createTable(tableName: "EX_FG_ALERT_QUERY_VALUES") {
            column(name: "EX_ALERT_FG_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_FG_QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_FG_QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "Krishan (generated)", id: "1666271101000-05") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'EX_ALERT_FG_QUERY_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "EX_ALERT_FG_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'EX_ALERT_FG_QUERY_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EX_ALERT_FG_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "Krishan (generated)", id: "1666271101000-06") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'FG_SEARCH')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "FG_SEARCH", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'FG_SEARCH')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "FG_SEARCH", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishna (generated)", id: "1666271101000-07") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'FG_SEARCH_ATTR')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "FG_SEARCH_ATTR", type: "clob") {
                constraints(nullable: "true")
            }
        }
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'FG_SEARCH_ATTR')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "FG_SEARCH_ATTR", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
}