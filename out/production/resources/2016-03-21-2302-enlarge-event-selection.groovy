databaseChangeLog = {
    changeSet(author: "lei gao", id: "1458626767494-1") {
        modifyDataType(columnName: "EVENT_SELECTION", newDataType: "varchar2(4000 char)", tableName: "ALERTS")
    }
}
