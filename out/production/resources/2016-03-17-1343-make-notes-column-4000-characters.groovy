databaseChangeLog = {
    changeSet(author: "glennsilverman (generated)", id: "1457747468553-1") {
        modifyDataType(columnName: "NOTES", newDataType: "varchar2(4000 char)", tableName: "ALERTS")
    }
}
