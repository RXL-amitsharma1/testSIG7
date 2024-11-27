databaseChangeLog = {

    changeSet(author: "shivam vashist(generated)", id: "1662543826647-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'UNDOABLE_DISP')
            }
        }
        createTable(tableName: "UNDOABLE_DISP") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true")
            }
            column(name: "OBJECT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "OBJECT_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "PREV_DISPOSITION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "CURR_DISPOSITION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "PREV_PROPOSED_DISPOSITION", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "PREV_JUSTIFICATION", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
            column(name: "PREV_DISP_PERFORMED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "PREV_WORKFLOW_STATE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "PREV_DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
            column(name: "PAST_PREV_DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
            column(name: "PREV_ACTUAL_DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
            column(name: "PREV_DISP_CHANGE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
            column(name: "is_used", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "is_enabled", type: "number(10, 0)", defaultValue: "1") {
                constraints(nullable: "false")
            }
            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "shivam vashist(generated)", id: "1662543826647-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'IS_UNDO')
            }
        }
        addColumn(tableName: "PRODUCT_EVENT_HISTORY") {
            column(name: "IS_UNDO", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam vashist(generated)", id: "1662543826647-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_HISTORY', columnName: 'IS_UNDO')
            }
        }
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "IS_UNDO", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam vashist (generated)", id: "1662543826647-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERTS', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "ALERTS") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam vashist (generated)", id: "1662543826647-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "LITERATURE_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam vashist(generated)", id: "1662543826647-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_HISTORY', columnName: 'IS_UNDO')
            }
        }
        addColumn(tableName: "LITERATURE_HISTORY") {
            column(name: "IS_UNDO", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary (generated)", id: "1662543826647-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543826647-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_HISTORY', columnName: 'is_undo')
            }
        }
        addColumn(tableName: "SIGNAL_HISTORY") {
            column(name: "is_undo", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543826997-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543826999-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "LITERATURE_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543826999-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543826999-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERTS', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "ALERTS") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543826999-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543932321-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543932322-0") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543932323-0") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary(generated)", id: "1662543932323-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_LITERATURE_ALERT', columnName: 'is_disp_changed')
            }
        }
        addColumn(tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(name: "is_disp_changed", type: "NUMBER(1, 0)", defaultValue: 0 ){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "1662543932323-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNDOABLE_DISP', columnName: 'previous_due_in')
            }
        }
        addColumn(tableName: "UNDOABLE_DISP") {
            column(name: "previous_due_in", type: "NUMBER(10,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "shivam (generated)", id: "1662543932323-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNDOABLE_DISP', columnName: 'is_due_date_changed')
            }
        }
        addColumn(tableName: "UNDOABLE_DISP") {
            column(name: "is_due_date_changed", type: "NUMBER(1,0)", defaultValue: "0") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "hritik chaudhary (generated)", id: "1662543932323-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_HISTORY', columnName: 'is_undo')
            }
        }
        addColumn(tableName: "EVDAS_HISTORY") {
            column(name: "is_undo", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    
    changeSet(author: "isha (generated)", id: "1613989815343-426") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EVDAS_ON_DEMAND_ALERT' AND column_name = 'LISTEDNESS' ;")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(1)", columnName: "listedness", tableName: "EVDAS_ON_DEMAND_ALERT")
    }
    changeSet(author: "Isha (generated)", id: "1613989815343-430") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'DETAILS')
        }
        sql("alter table ACTIONS modify DETAILS VARCHAR2(8000 CHAR);")
    }


}