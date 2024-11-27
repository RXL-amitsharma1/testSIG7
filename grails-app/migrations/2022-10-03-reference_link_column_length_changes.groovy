databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "1997654321337-345") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'attachment' AND column_name = 'reference_link';")
        }
        sql("alter table ATTACHMENT ALTER COLUMN REFERENCE_LINK TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Gaurav (generated)", id: "1997654321338-345") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'attachment' AND column_name = 'input_name';")
        }
        sql("alter table ATTACHMENT ALTER COLUMN INPUT_NAME TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Gaurav (generated)", id: "1997654321339-345") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '4000', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'attachment' AND column_name = 'name';")
        }
        sql("alter table ATTACHMENT ALTER COLUMN NAME TYPE VARCHAR(8000 );")
    }
}