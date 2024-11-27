databaseChangeLog = {

    changeSet(author: "Amrendra (generated)", id: "2097654321339-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'signal_status_history', columnName: 'status_comment')
        }
        sql('''
        ALTER TABLE "signal_status_history" 
        ALTER COLUMN "status_comment" 
        TYPE VARCHAR(8000);
    ''')
    }

    changeSet(author: "Amrendra (generated)", id: "2097654321339-5") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'STATUS_COMMENT')
        }
        try {
            sql('''
        UPDATE SIGNAL_STATUS_HISTORY 
        SET STATUS_COMMENT = '' 
        WHERE STATUS_COMMENT = 'NA';
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
