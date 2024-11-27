databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "1997654321337-345") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'REFERENCE_LINK' AND TABLE_NAME = 'ATTACHMENT';")
        }
        sql("alter table ATTACHMENT modify REFERENCE_LINK VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Gaurav (generated)", id: "1997654321338-345") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'INPUT_NAME' AND TABLE_NAME = 'ATTACHMENT';")
        }
        sql("alter table ATTACHMENT modify INPUT_NAME VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Gaurav (generated)", id: "1997654321339-345") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'NAME' AND TABLE_NAME = 'ATTACHMENT';")
        }
        sql("alter table ATTACHMENT modify NAME VARCHAR2(8000 CHAR);")
    }
}