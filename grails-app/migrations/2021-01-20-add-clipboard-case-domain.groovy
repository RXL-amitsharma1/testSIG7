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

            column(name: "name", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }

            column(name: "case_ids", type: "clob") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_temp_view", type: "boolean") {
                constraints(nullable: "true")
            }

            column(name: "is_deleted", type: "boolean"){
                constraints(nullable: "true")
            }

            column(name: "is_first_use", type: "boolean"){
                constraints(nullable: "true")
            }

            column(name: "is_updated", type: "boolean"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal", id: "1611139538773-21") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'character varying', "SELECT data_type FROM information_schema.columns WHERE table_name = 'param' AND column_name = 'value';")
        }
        addColumn(tableName: "PARAM") {
            column(name: "VALUE_CLOB", type: "clob")
        }

        sql("update PARAM set VALUE_CLOB = VALUE;")
        sql("alter table PARAM drop column VALUE;")
        sql("alter table PARAM rename column VALUE_CLOB to VALUE;")
    }
}
