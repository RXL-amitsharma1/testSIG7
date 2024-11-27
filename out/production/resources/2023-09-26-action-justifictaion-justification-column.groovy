databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1695734014730-00001") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'JUSTIFICATION' AND TABLE_NAME = 'ACTION_JUSTIFICATION';")
        }
        sql("alter table ACTION_JUSTIFICATION modify JUSTIFICATION VARCHAR2(8000 CHAR);")
    }
}