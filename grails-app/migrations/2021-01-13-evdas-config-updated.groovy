databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "1573900089089-89") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EVDAS_CONFIG' AND column_name = 'DATE_RANGE_INFORMATION_ID' ;")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(19, 0)", columnName: "date_range_information_id", tableName: "EVDAS_CONFIG")
    }
}