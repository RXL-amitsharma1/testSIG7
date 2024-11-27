databaseChangeLog = {

    changeSet(author: "sandeep (generated)", id: "1456274736372-5") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_HISTORY', columnName: 'REPORT_NAME')
        }

        addColumn(tableName: "REPORT_HISTORY") {
            column(name: "REPORT_NAME_COPY", type: "varchar2(8000 CHAR)")
        }

        sql("update REPORT_HISTORY set REPORT_NAME_COPY = REPORT_NAME;")

        dropColumn(tableName: "REPORT_HISTORY", columnName: "REPORT_NAME")

        renameColumn(tableName: "REPORT_HISTORY", oldColumnName: "REPORT_NAME_COPY", newColumnName: "REPORT_NAME")

        addNotNullConstraint(tableName: "REPORT_HISTORY", columnName: "REPORT_NAME")
    }

    changeSet(author: "sandeep (generated)", id: "1456274736372-6") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_HISTORY', columnName: 'PRODUCT_NAME')
        }

        addColumn(tableName: "REPORT_HISTORY") {
            column(name: "PRODUCT_NAME_COPY", type: "varchar2(8000 CHAR)")
        }

        sql("update REPORT_HISTORY set PRODUCT_NAME_COPY = PRODUCT_NAME;")

        dropColumn(tableName: "REPORT_HISTORY", columnName: "PRODUCT_NAME")

        renameColumn(tableName: "REPORT_HISTORY", oldColumnName: "PRODUCT_NAME_COPY", newColumnName: "PRODUCT_NAME")

        addNotNullConstraint(tableName: "REPORT_HISTORY", columnName: "PRODUCT_NAME")
    }
}