databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1712034810523-0001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '32000', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'NOTES' and table_name = 'ALERTS';")
            }
        }
        sql("alter table ALERTS modify NOTES VARCHAR2(32000 CHAR);")
    }
    changeSet(author: "hemlata (generated)", id: "1712840832320-001") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERTS', columnName: 'NOTES')
        }
        sql("alter table ALERTS add NOTES1 clob")
        sql("update ALERTS set NOTES1=NOTES")
        sql("alter table ALERTS drop column  NOTES")
        sql("alter table ALERTS rename column NOTES1 to NOTES")
    }
}