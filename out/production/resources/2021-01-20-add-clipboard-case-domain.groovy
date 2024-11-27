databaseChangeLog = {
    changeSet(author: "rishabh (generated)", id: "1611139538773-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'clipboard_cases')
            }
        }
        createTable(tableName: "clipboard_cases") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "clipboard_casesPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "case_ids", type: "clob") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_temp_view", type: "number(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "is_deleted", type: "number(1, 0)"){
                constraints(nullable: "true")
            }

            column(name: "is_first_use", type: "number(1, 0)"){
                constraints(nullable: "true")
            }

            column(name: "is_updated", type: "number(1, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal", id: "1611139538773-21") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'VARCHAR2', "SELECT data_type FROM all_tab_columns where table_name = 'PARAM' AND COLUMN_NAME = 'VALUE';")
        }
        addColumn(tableName: "PARAM") {
            column(name: "VALUE_CLOB", type: "clob")
        }

        sql("update PARAM set VALUE_CLOB = VALUE;")
        sql("alter table PARAM drop column VALUE;")
        sql("alter table PARAM rename column VALUE_CLOB to VALUE;")
    }
}
