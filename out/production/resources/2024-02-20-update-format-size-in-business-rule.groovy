databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "1997654321337-346") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'FORMAT' AND TABLE_NAME = 'RULE_INFORMATION';")
        }
        sql("alter table RULE_INFORMATION modify FORMAT VARCHAR2(4000 CHAR);")
    }
}