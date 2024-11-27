databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "1997654321337-346") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'rule_information' AND column_name = 'format';")
        }
        sql("alter table RULE_INFORMATION ALTER COLUMN FORMAT TYPE VARCHAR(4000 );")
    }
}