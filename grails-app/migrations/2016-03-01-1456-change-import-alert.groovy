databaseChangeLog = {

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-1") {
        modifyDataType(columnName: "NAME", newDataType: "VARCHAR(4000 )", tableName: "ALERTS")
    }

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-2") {
        modifyDataType(columnName: "STUDY_SELECTION", newDataType: "VARCHAR(4000 )", tableName: "ALERTS")
    }

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-3") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "VARCHAR(4000 )", tableName: "ALERTS")
    }

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-4") {
        modifyDataType(columnName: "TOPIC", newDataType: "VARCHAR(4000 )", tableName: "ALERTS")
    }
}
