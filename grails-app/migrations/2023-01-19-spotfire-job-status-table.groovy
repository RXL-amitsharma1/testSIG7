databaseChangeLog = {
    changeSet(author: "isha(generated)", id: "1675329551-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SPOTFIRE_JOB_INSTANCES')
            }
        }
        createTable(tableName: "SPOTFIRE_JOB_INSTANCES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "JOB_ID", type: "varchar(50)") {
                constraints(nullable: "false", primaryKey: "true")
            }
            column(name: "JOB_CONTENT", type: "clob") {
                constraints(nullable: "true")
            }
            column(name: "EXECUTION_STATUS", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "EXECUTED_CONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
            column(name: "FILE_NAME", type:  "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "DATE_CREATED", type:  "timestamp") {
                constraints(nullable: "true")
            }
            column(name: "TYPE", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Isha (generated)", id: "Inbox-detail-size-increase") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'INBOX_LOG', columnName: 'DETAIL_URL')
        }
        sql("alter table INBOX_LOG ALTER COLUMN DETAIL_URL TYPE VARCHAR(1000 );")
    }

}