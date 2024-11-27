databaseChangeLog={
    changeSet(author: "hemlata (generated)", id: "1708626578695-1") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'signal_rmms' and column_name = 'date_created' ;")
        }
        sql('ALTER TABLE signal_rmms ALTER COLUMN DATE_CREATED TYPE TIMESTAMP(6);')
    }
}