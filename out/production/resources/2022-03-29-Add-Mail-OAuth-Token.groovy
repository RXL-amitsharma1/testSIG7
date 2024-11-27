databaseChangeLog = {

    changeSet(author: "anshulbhatia", id: "202203250227-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'MAIL_OAUTH_TOKEN')
            }
        }

        createTable(tableName: "MAIL_OAUTH_TOKEN") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "MAIL_OAUTH_TOKEN_PK")
            }
            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "EXPIRES_IN", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "EXPIRE_AT", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "ACCESS_TOKEN", type: "varchar2(4000)") {
                constraints(nullable: "false")
            }
            column(name: "REFRESH_TOKEN", type: "varchar2(4000)") {
                constraints(nullable: "false")
            }
            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
        }
        addUniqueConstraint(tableName: "MAIL_OAUTH_TOKEN", columnNames: "NAME", constraintName: "mail_token_name_unique_c121", deferrable: "false", disabled: "false", initiallyDeferred: "false")
    }

}