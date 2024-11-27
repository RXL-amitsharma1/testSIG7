databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "1573900089089-89") {
        preConditions(onFail: 'MARK_RAN') {
            notNullConstraintExists(columnName: "date_range_information_id", tableName: "EVDAS_CONFIG")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(19, 0)", columnName: "date_range_information_id", tableName: "EVDAS_CONFIG")
    }
}