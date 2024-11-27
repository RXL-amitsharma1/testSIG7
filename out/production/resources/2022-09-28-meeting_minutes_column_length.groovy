databaseChangeLog = {

    changeSet(author: "anshul (generated)", id: "37283741301-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'MEETING', columnName: "MEETING_MINUTES")
        }
        modifyDataType(columnName: "MEETING_MINUTES", newDataType: "varchar2(8000 char)", tableName: "MEETING")
    }
}