databaseChangeLog = {

    changeSet(author: "chetansharma (generated)", id: "1560242765272-7") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'SUSP_PROD')
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "SUSP_PROD_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_CASE_ALERT set SUSP_PROD_COPY = SUSP_PROD;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "SUSP_PROD")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "SUSP_PROD_COPY", newColumnName: "SUSP_PROD")
    }
}
