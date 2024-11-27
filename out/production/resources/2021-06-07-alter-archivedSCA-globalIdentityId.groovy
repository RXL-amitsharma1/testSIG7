databaseChangeLog = {
    changeSet(author: "amrendra (generated)", id: "16617101827367") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = UPPER('archived_single_case_alert') AND column_name = UPPER('global_identity_id') ;")
        }
        sql("alter table archived_single_case_alert modify (global_identity_id NULL);")
    }


}
