databaseChangeLog = {

    changeSet(author: "Hemlata (generated)", id: "1710919699723-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_dropdown1')
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_dropdown1_copy", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update VALIDATED_SIGNAL set ud_dropdown1_copy = ud_dropdown1;")

        dropColumn(tableName: "VALIDATED_SIGNAL", columnName: "ud_dropdown1")

        renameColumn(tableName: "VALIDATED_SIGNAL", oldColumnName: "ud_dropdown1_copy", newColumnName: "ud_dropdown1")

    }
    changeSet(author: "Hemlata (generated)", id: "1710919699723-02") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ud_dropdown2')
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ud_dropdown2_copy", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update VALIDATED_SIGNAL set ud_dropdown2_copy = ud_dropdown2;")

        dropColumn(tableName: "VALIDATED_SIGNAL", columnName: "ud_dropdown2")

        renameColumn(tableName: "VALIDATED_SIGNAL", oldColumnName: "ud_dropdown2_copy", newColumnName: "ud_dropdown2")

    }
    changeSet(author: "Hemlata (generated)", id: "1710919699723-03") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'dd_value1')
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "dd_value1_copy", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update VALIDATED_SIGNAL set dd_value1_copy = dd_value1;")

        dropColumn(tableName: "VALIDATED_SIGNAL", columnName: "dd_value1")

        renameColumn(tableName: "VALIDATED_SIGNAL", oldColumnName: "dd_value1_copy", newColumnName: "dd_value1")

    }
    changeSet(author: "Hemlata (generated)", id: "1710919699723-04") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'dd_value2')
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "dd_value2_copy", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update VALIDATED_SIGNAL set dd_value2_copy = dd_value2;")

        dropColumn(tableName: "VALIDATED_SIGNAL", columnName: "dd_value2")

        renameColumn(tableName: "VALIDATED_SIGNAL", oldColumnName: "dd_value2_copy", newColumnName: "dd_value2")

    }

}