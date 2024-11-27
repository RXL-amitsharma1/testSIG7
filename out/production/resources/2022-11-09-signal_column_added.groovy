databaseChangeLog = {
    changeSet(author: "amrendra (generated)", id: "234567887654-200") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'NEW_DETECTED_DATE')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "NEW_DETECTED_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "234567887654-201") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNDOABLE_DISP', columnName: 'PREV_MILESTONE_DATE')
            }
        }
        addColumn(tableName: "UNDOABLE_DISP") {
            column(name: "PREV_MILESTONE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "234567887654-202") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'CURRENT_DISPOSITION_ID')
            }
        }
        addColumn(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "CURRENT_DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "234567887654-203") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'IS_AUTO_POPULATE')
            }
        }
        addColumn(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "IS_AUTO_POPULATE", type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "234567887654-204") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'due_date')
            }
        }
        addColumn(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "due_date", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "uddesh teke (generated)", id: "234567887654-205") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNDOABLE_DISP', columnName: 'PREV_SIGNAL_STATUS')
            }
        }
        addColumn(tableName: "UNDOABLE_DISP") {
            column(name: "PREV_SIGNAL_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }


}