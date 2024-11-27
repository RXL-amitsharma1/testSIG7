databaseChangeLog = {

    changeSet(author: "hemlata (generated)", id: "1671166057553-47") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ALERT_REVIEW_COMPLETED')
            }
        }
        createTable(tableName: "ALERT_REVIEW_COMPLETED") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_REVIEW_COMPLETEDPK")
            }
            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "EXE_CONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "CONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CASE_NUMBER", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "ALERT_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

        }
    }

}