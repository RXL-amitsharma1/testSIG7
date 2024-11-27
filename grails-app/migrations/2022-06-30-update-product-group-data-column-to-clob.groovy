databaseChangeLog = {

    changeSet(author: "nikhil (generated)", id: "15606457732-47") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PROD_GROUPS_DATA', columnName: 'DATA')
        }
        addColumn(tableName: "PROD_GROUPS_DATA") {
            column(name: "DATA_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update PROD_GROUPS_DATA set DATA_COPY = DATA;")

        dropColumn(tableName: "PROD_GROUPS_DATA", columnName: "DATA")

        renameColumn(tableName: "PROD_GROUPS_DATA", oldColumnName: "DATA_COPY", newColumnName: "DATA")
    }
}