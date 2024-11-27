databaseChangeLog = {
    changeSet(author: "Krishna Joshi (generated)", id: "1718348463642-09") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_HISTORY', columnName: 'product_group')
            }
        }
        addColumn(tableName: "REPORT_HISTORY") {
            column(name: "product_group", type: "VARCHAR(8000)")
        }
    }

    changeSet(author: "Krishna Joshi (generated)", id: "1718348463642-12") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_HISTORY', columnName: 'product_name')
        }
        addDefaultValue(tableName: "REPORT_HISTORY", columnName: "product_name", defaultValue: "NULL")
    }


}
