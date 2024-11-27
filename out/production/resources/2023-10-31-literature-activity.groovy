databaseChangeLog = {

    changeSet(author: "Hemlata (generated)", id: "1698745898178-0002") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'DETAILS')
        }
        sql("alter table LITERATURE_ACTIVITY  modify DETAILS VARCHAR2(32000 CHAR);")
    }

}