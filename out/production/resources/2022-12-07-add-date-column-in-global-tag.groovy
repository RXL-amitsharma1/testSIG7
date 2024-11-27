databaseChangeLog = {

    changeSet(author: "Krishana Joshi (generated)", id: "1670404698571-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_GLOBAL_TAGS', columnName: 'creation_date')
            }
        }
        addColumn(tableName: "SINGLE_GLOBAL_TAGS") {
            column(name: "creation_date", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishana Joshi (generated)", id: "1670404698571-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_GLOBAL_TAGS', columnName: 'creation_date')
            }
        }
        addColumn(tableName: "AGG_GLOBAL_TAGS") {
            column(name: "creation_date", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishana Joshi (generated)", id: "1670404698571-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERAURE_GLOBAL_TAGS', columnName: 'creation_date')
            }
        }
        addColumn(tableName: "LITERAURE_GLOBAL_TAGS") {
            column(name: "creation_date", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishna Joshi (generated)", id: "1670499844081-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT_HISTORY', columnName: 'EXEC_CONFIG_ID')
            }
        }
        addColumn(tableName: "ALERT_COMMENT_HISTORY") {
            column(name: "EXEC_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishna Joshi (generated)", id: "1670833953002-05") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_GLOBAL_TAGS', columnName: 'CREATION_DATE')
        }
        sql("ALTER TABLE SINGLE_GLOBAL_TAGS MODIFY  CREATION_DATE DEFAULT SYSTIMESTAMP;")
    }
    changeSet(author: "Krishna Joshi (generated)", id: "1670833953002-06") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_GLOBAL_TAGS', columnName: 'CREATION_DATE')
        }
        sql("ALTER TABLE AGG_GLOBAL_TAGS MODIFY  CREATION_DATE DEFAULT SYSTIMESTAMP;")
    }

    changeSet(author: "Krishna Joshi (generated)", id: "1670833953002-07") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERAURE_GLOBAL_TAGS', columnName: 'CREATION_DATE')
        }
        sql("ALTER TABLE LITERAURE_GLOBAL_TAGS MODIFY  CREATION_DATE DEFAULT SYSTIMESTAMP;")
    }
}