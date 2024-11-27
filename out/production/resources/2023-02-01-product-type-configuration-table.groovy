databaseChangeLog = {
    changeSet(author: "uddesh teke(generated)", id: "1497767419312-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'PRODUCT_TYPE_CONFIGURATION')
            }
        }
        createTable(tableName: "PRODUCT_TYPE_CONFIGURATION") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PRODUCT_TYPE_CONFIGURATIONPK")
            }

            column(name: "NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PRODUCT_TYPE_CONFIGURATIONPK")
            }

            column(name: "ROLE_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "ROLE_TYPE_ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PRODUCT_TYPE_CONFIGURATIONPK")
            }

            column(name: "IS_DEFAULT", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

        }
    }
}