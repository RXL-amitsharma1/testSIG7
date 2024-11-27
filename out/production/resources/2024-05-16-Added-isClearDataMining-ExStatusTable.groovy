databaseChangeLog={


    changeSet(author: "Mohit Kumar (generated)", id: "16831840325999-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'IS_CLEAR_DATA_MINING')
            }
        }
        sql("alter table EX_STATUS add IS_CLEAR_DATA_MINING NUMBER(1, 0) DEFAULT 0 NULL;")
    }



}