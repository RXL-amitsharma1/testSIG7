databaseChangeLog = {

    changeSet(author: "akshat (generated)", id: "1568109635530-35") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_VALUE')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_VALUE_COPY", type: "NUMBER")
        }
        sql("update AGG_ALERT set PRR_VALUE_COPY = PRR_VALUE;")

        dropColumn(tableName: "AGG_ALERT", columnName: "PRR_VALUE")

        renameColumn(tableName: "AGG_ALERT", oldColumnName: "PRR_VALUE_COPY", newColumnName: "PRR_VALUE")
        addNotNullConstraint(columnDataType: "NUMBER", columnName: "PRR_VALUE", tableName: "AGG_ALERT")
    }
    changeSet(author: "akshat (generated)", id: "1568109635530-36") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'prr05')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr05_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set prr05_COPY = prr05;")

        dropColumn(tableName: "AGG_ALERT", columnName: "prr05")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "prr05_COPY", newColumnName: "prr05")
    }
    changeSet(author: "akshat (generated)", id: "1568109635530-37") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'prr95')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr95_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set prr95_COPY = prr95;")

        dropColumn(tableName: "AGG_ALERT", columnName: "prr95")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "prr95_COPY", newColumnName: "prr95")
    }
    changeSet(author: "akshat (generated)", id: "1568109635530-38") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'ROR_VALUE')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ROR_VALUE_COPY", type: "NUMBER")
        }
        sql("update AGG_ALERT set ROR_VALUE_COPY = ROR_VALUE;")

        dropColumn(tableName: "AGG_ALERT", columnName: "ROR_VALUE")

        renameColumn(tableName: "AGG_ALERT", oldColumnName: "ROR_VALUE_COPY", newColumnName: "ROR_VALUE")
        addNotNullConstraint(columnDataType: "NUMBER", columnName: "ROR_VALUE", tableName: "AGG_ALERT")
    }
    changeSet(author: "akshat (generated)", id: "1568109635530-39") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'ror05')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror05_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set ror05_COPY = ror05;")

        dropColumn(tableName: "AGG_ALERT", columnName: "ror05")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "ror05_COPY", newColumnName: "ror05")
    }
    changeSet(author: "akshat (generated)", id: "1568109635530-40") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'ror95')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror95_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set ror95_COPY = ror95;")

        dropColumn(tableName: "AGG_ALERT", columnName: "ror95")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "ror95_COPY", newColumnName: "ror95")
    }
}
