databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1712034810523-0001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '32000', "SELECT character_maximum_length FROM information_schema.columns WHERE table_name = 'alerts' AND column_name = 'notes';")
            }
        }
        sql("alter table ALERTS ALTER COLUMN NOTES TYPE VARCHAR(32000 );")
    }
    changeSet(author: "hemlata (generated)", id: "1712840832320-001") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERTS', columnName: 'NOTES')
        }
        sql("alter table ALERTS add NOTES1 TEXT")
        sql("update ALERTS set NOTES1=NOTES")
        sql("alter table ALERTS drop column  NOTES")
        sql("alter table ALERTS rename column NOTES1 to NOTES")
    }
}