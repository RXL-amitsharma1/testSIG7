databaseChangeLog = {
    changeSet(author: "Krishna Joshi (generated)", id: "16729022894384-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTION_JUSTIFICATION', columnName: 'JUSTIFICATION')
        }
        sql("alter table ACTION_JUSTIFICATION add JUSTIFICATION1 VARCHAR2(8000 CHAR);")
        sql("update ACTION_JUSTIFICATION set JUSTIFICATION1=JUSTIFICATION;")
        sql("alter table ACTION_JUSTIFICATION drop column  JUSTIFICATION;")
        sql("alter table ACTION_JUSTIFICATION rename column JUSTIFICATION1 to JUSTIFICATION;")
    }

    changeSet(author: "rishabh-goswami", id: "2306202116081999-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(*) from rconfig where FG_SEARCH is null;")
            }
        }
        sql("update rconfig set FG_SEARCH = 0 where FG_SEARCH is null; commit;")

    }

    changeSet(author: "rishabh-goswami", id: "565772216081999-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(*) from ex_rconfig where FG_SEARCH is null;")
            }
        }
        sql("update ex_rconfig set FG_SEARCH = 0 where FG_SEARCH is null; commit;")

    }
    changeSet(author: "yogesh (generated)", id: "1678959383565-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'USER_IP_ADDRESS')
            }
        }
        addColumn(tableName: "audit_log") {
            column(name: "USER_IP_ADDRESS", type: "varchar2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh-goswami", id: "0101197010071973-32") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'SEARCH_STRING' AND TABLE_NAME = 'LITERATURE_CONFIG';")
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "SEARCH_STRING_2", type: "VARCHAR2(8000 CHAR)")
        }

        sql("update LITERATURE_CONFIG set SEARCH_STRING_2 = SEARCH_STRING;")
        sql("alter table LITERATURE_CONFIG drop column SEARCH_STRING;")
        sql("alter table LITERATURE_CONFIG rename column SEARCH_STRING_2 to SEARCH_STRING;")
    }
    changeSet(author: "rishabh-goswami", id: "2909199527012023-00") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'SEARCH_STRING' AND TABLE_NAME = 'EX_LITERATURE_CONFIG';")
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "SEARCH_STRING_2", type: "VARCHAR2(8000 CHAR)")
        }

        sql("update EX_LITERATURE_CONFIG set SEARCH_STRING_2 = SEARCH_STRING;")
        sql("alter table EX_LITERATURE_CONFIG drop column SEARCH_STRING;")
        sql("alter table EX_LITERATURE_CONFIG rename column SEARCH_STRING_2 to SEARCH_STRING;")
    }
    changeSet(author: "rishabh-goswami", id: "1608199905111999-23") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'SEARCH_STRING' AND TABLE_NAME = 'LITERATURE_ALERT';")
        }
        addColumn(tableName: "LITERATURE_ALERT") {
            column(name: "SEARCH_STRING_2", type: "VARCHAR2(8000 CHAR)")
        }

        sql("update LITERATURE_ALERT set SEARCH_STRING_2 = SEARCH_STRING;")
        sql("alter table LITERATURE_ALERT drop column SEARCH_STRING;")
        sql("alter table LITERATURE_ALERT rename column SEARCH_STRING_2 to SEARCH_STRING;")
    }
    changeSet(author: "rishabh-goswami", id: "1007199129091995-04") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'SEARCH_STRING' AND TABLE_NAME = 'ARCHIVED_LITERATURE_ALERT';")
        }
        addColumn(tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(name: "SEARCH_STRING_2", type: "VARCHAR2(8000 CHAR)")
        }

        sql("update ARCHIVED_LITERATURE_ALERT set SEARCH_STRING_2 = SEARCH_STRING;")
        sql("alter table ARCHIVED_LITERATURE_ALERT drop column SEARCH_STRING;")
        sql("alter table ARCHIVED_LITERATURE_ALERT rename column SEARCH_STRING_2 to SEARCH_STRING;")
    }
}