databaseChangeLog = {

    changeSet(author: "glennsilverman (generated)", id: "3132900004722-1") {
        modifyDataType(columnName: "INPUT_IDENTIFIER", newDataType: "varchar2(4000 char)", tableName: "IMPORT_DETAIL")
    }


}
