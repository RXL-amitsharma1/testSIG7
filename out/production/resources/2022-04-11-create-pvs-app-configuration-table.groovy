databaseChangeLog = {
    changeSet(author: "Nikhil (generated)", id: "4999231424111110-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'PVS_APP_CONFIGURATION')
            }
        }
        createTable(tableName: "PVS_APP_CONFIGURATION") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "pvs_app_configPK")
            }
            column(name: "KEY", type: "VARCHAR2(4000)") {
                constraints(nullable: "false")
            }

            column(name: "BOOLEAN_VALUE", type: "NUMBER(1)") {
                constraints(nullable: "true")
            }

            column(name: "STRING_VALUE", type: "VARCHAR2(4000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nikhil (generated)", id: "15643587683383-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'TOPIC')
        }

        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "TOPIC_COPY", type: "VARCHAR2(4000)")
        }

        sql("update VALIDATED_SIGNAL set TOPIC_COPY = TOPIC;")

        dropColumn(tableName: "VALIDATED_SIGNAL", columnName: "TOPIC")

        renameColumn(tableName: "VALIDATED_SIGNAL", oldColumnName: "TOPIC_COPY", newColumnName: "TOPIC")

    }

    changeSet(author: "Nikhil (generated)", id: "15643587683383-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'TOPIC')
        }

        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "TOPIC_COPY", type: "VARCHAR2(4000)")
        }

        sql("update VALIDATED_SIGNAL set TOPIC_COPY = TOPIC;")

        dropColumn(tableName: "VALIDATED_SIGNAL", columnName: "TOPIC")

        renameColumn(tableName: "VALIDATED_SIGNAL", oldColumnName: "TOPIC_COPY", newColumnName: "TOPIC")

    }

}
