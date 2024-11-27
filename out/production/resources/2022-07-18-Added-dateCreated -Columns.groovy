databaseChangeLog={
    changeSet(author: "hemlata (generated)", id: "1708626578695-1") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'SIGNAL_RMMS' AND column_name = 'DATE_CREATED' ;")
        }
        sql("ALTER TABLE SIGNAL_RMMs MODIFY DATE_CREATED TIMESTAMP(6) NULL;")
    }
}