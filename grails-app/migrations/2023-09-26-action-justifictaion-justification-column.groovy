databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1695734014730-00001") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'action_justification' AND column_name = 'justification';")
        }
        sql("alter table ACTION_JUSTIFICATION ALTER COLUMN JUSTIFICATION TYPE VARCHAR(8000 );")
    }
}