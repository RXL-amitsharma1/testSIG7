databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1669711657362-5666789") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '8000', "SELECT character_maximum_length FROM information_schema.columns WHERE column_name = 'reason_for_evaluation' AND table_name = 'validated_signal'")
        }
        sql("alter table VALIDATED_SIGNAL ALTER COLUMN REASON_FOR_EVALUATION TYPE VARCHAR(8000 );")
    }
}