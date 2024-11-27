databaseChangeLog = {
    changeSet(author: "amrendra (generated)", id: "16617101827367") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'NO', "SELECT is_nullable FROM information_schema.columns WHERE table_name = UPPER('archived_single_case_alert') AND column_name = UPPER('global_identity_id');")
        }
        sql("ALTER TABLE archived_single_case_alert ALTER COLUMN global_identity_id DROP NOT NULL;")
    }



}
