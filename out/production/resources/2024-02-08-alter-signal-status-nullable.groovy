databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "2097654321339-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'STATUS_COMMENT')
        }
        sql("alter table SIGNAL_STATUS_HISTORY modify STATUS_COMMENT VARCHAR2(8000 CHAR) NULL;")
    }

    changeSet(author: "Amrendra (generated)", id: "2097654321339-5") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'STATUS_COMMENT')
        }
        try {
            sql('''
                update SIGNAL_STATUS_HISTORY set STATUS_COMMENT = '' where STATUS_COMMENT = 'NA'; 
                COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "Uddesh Teke(generated)", id: "20230215185437-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNDOABLE_DISP', columnName: 'SIGNAL_OUTCOME_ID')
            }
        }
        addColumn(tableName: "UNDOABLE_DISP") {
            column(name: "SIGNAL_OUTCOME_ID", type: "NUMBER(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }
}
