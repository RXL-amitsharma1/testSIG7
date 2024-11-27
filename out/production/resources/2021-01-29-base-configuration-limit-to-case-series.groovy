databaseChangeLog = {
    changeSet(author: "nitesh (generated)", id: "1608824568977-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_CASE_SERIES_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568977-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALERT_CASE_SERIES_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568977-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_CASE_SERIES_NAME')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_CASE_SERIES_NAME", type: "varchar(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568977-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_CASE_SERIES_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALERT_CASE_SERIES_NAME", type: "varchar(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568977-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CASE_SERIES_SPOTFIRE_FILE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CASE_SERIES_SPOTFIRE_FILE", type: "varchar(2000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

}