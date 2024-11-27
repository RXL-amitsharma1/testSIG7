databaseChangeLog = {

    changeSet(author: "Hemlata (generated)", id: "1683184039086-47") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'business_configuration' AND column_name = 'description';")
        }
        sql("alter table BUSINESS_CONFIGURATION ALTER COLUMN DESCRIPTION TYPE VARCHAR(4000 );")
    }
    changeSet(author: "Hemlata (generated)", id: "1683190309917-17") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '255', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'rule_information' AND column_name = 'justification_text';")
        }
        sql("alter table RULE_INFORMATION ALTER COLUMN JUSTIFICATION_TEXT TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Isha (generated)", id: "1683190309917-18") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'TOPIC')
        }
        sql("alter table VALIDATED_SIGNAL ALTER COLUMN TOPIC TYPE VARCHAR(4000 );")
    }

}