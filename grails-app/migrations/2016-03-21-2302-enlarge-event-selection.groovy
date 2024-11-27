databaseChangeLog = {
    changeSet(author: "lei gao", id: "1458626767494-1") {
        modifyDataType(columnName: "EVENT_SELECTION", newDataType: "VARCHAR(4000 )", tableName: "ALERTS")
    }
}
