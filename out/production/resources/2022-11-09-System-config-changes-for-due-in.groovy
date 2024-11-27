databaseChangeLog = {
    changeSet(author: "uddesh teke (generated)", id: "612341319583-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'DISPLAY_DUE_IN')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "DISPLAY_DUE_IN", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "612341319583-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'ENABLE_AUTO_POPULATE')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "ENABLE_AUTO_POPULATE", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "612341319583-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'ENABLE_END_OF_MILESTONE')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "ENABLE_END_OF_MILESTONE", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "612341319583-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DISPOSITION', columnName: 'SIGNAL_STATUS_FOR_DUE_DATE')
            }
        }
        addColumn(tableName: "DISPOSITION") {
            column(name: "SIGNAL_STATUS_FOR_DUE_DATE", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "uddesh teke (generated)", id: "612341319583-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'SELECTED_END_POINTS')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "SELECTED_END_POINTS", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "uddesh teke (generated)", id: "612341319583-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SYSTEM_CONFIG', columnName: 'FIRST_TIME')
            }
        }
        addColumn(tableName: "SYSTEM_CONFIG") {
            column(name: "FIRST_TIME", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }


}
