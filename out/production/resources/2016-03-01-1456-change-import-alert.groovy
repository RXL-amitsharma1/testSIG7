databaseChangeLog = {

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-1") {
        modifyDataType(columnName: "NAME", newDataType: "varchar2(4000 char)", tableName: "ALERTS")
    }

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-2") {
        modifyDataType(columnName: "STUDY_SELECTION", newDataType: "varchar2(4000 char)", tableName: "ALERTS")
    }

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-3") {
        modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(4000 char)", tableName: "ALERTS")
    }

    changeSet(author: "glennsilverman (generated)", id: "1456872414722-4") {
        modifyDataType(columnName: "TOPIC", newDataType: "varchar2(4000 char)", tableName: "ALERTS")
    }
}
