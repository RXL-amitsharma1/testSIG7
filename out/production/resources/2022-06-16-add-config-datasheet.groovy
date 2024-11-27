databaseChangeLog = {
    changeSet(author: "sandeep (generated)", id: "1655378991378-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'SELECTED_DATASHEET')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "SELECTED_DATASHEET", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1655378991378-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'SELECTED_DATASHEET')
            }
        }

        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "SELECTED_DATASHEET", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "isha (generated)", id: "1708626578695-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_DATASHEET_CHECKED')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "IS_DATASHEET_CHECKED", type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "isha (generated)", id: "1708626578695-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'IS_DATASHEET_CHECKED')
            }
        }

        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "IS_DATASHEET_CHECKED", type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "isha (generated)", id: "1708626578695-4") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'DATASHEET_TYPE')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "DATASHEET_TYPE", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "isha (generated)", id: "1708626578695-5") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'DATASHEET_TYPE')
            }
        }

        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "DATASHEET_TYPE", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "isha (generated)", id: "1708626578695-6") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'DATASHEET_TYPE')
            }
        }

        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "DATASHEET_TYPE", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "isha (generated)", id: "1708626578695-7") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'IS_DATASHEET_CHECKED')
            }
        }

        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "IS_DATASHEET_CHECKED",type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "isha (generated)", id: "1708626578695-8") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'SELECTED_DATASHEET')
            }
        }

        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "SELECTED_DATASHEET", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "isha (generated)", id: "1708626578695-9") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'DATASHEET_TYPE')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DATASHEET_TYPE", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "isha (generated)", id: "1708626578695-10") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_DATASHEET_CHECKED')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_DATASHEET_CHECKED",type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "isha (generated)", id: "1708626578695-11") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SELECTED_DATASHEET')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SELECTED_DATASHEET", type: "VARCHAR2(32000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

}
