databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1669711657362-5666789") {
        preCondition(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '8000', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'REASON_FOR_EVALUATION' AND TABLE_NAME = 'VALIDATED_SIGNAL';")
        }
        sql("alter table VALIDATED_SIGNAL modify REASON_FOR_EVALUATION VARCHAR2(8000 CHAR);")
    }
}