databaseChangeLog = {

    changeSet(author: "Hemlata (generated)", id: "1683184039086-47") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'DESCRIPTION' AND TABLE_NAME = 'BUSINESS_CONFIGURATION';")
        }
        sql("alter table BUSINESS_CONFIGURATION modify DESCRIPTION VARCHAR2(4000 CHAR);")
    }
    changeSet(author: "Hemlata (generated)", id: "1683190309917-17") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'JUSTIFICATION_TEXT' AND TABLE_NAME = 'RULE_INFORMATION';")
        }
        sql("alter table RULE_INFORMATION modify JUSTIFICATION_TEXT VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Isha (generated)", id: "1683190309917-18") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'TOPIC')
        }
        sql("alter table VALIDATED_SIGNAL modify TOPIC VARCHAR2(4000 CHAR);")
    }

}