databaseChangeLog = {
    changeSet(author: "uddesh-teke", id: "202404171511-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'DRUG_CLASSIFICATION')
        }
        sql("UPDATE RCONFIG SET DRUG_CLASSIFICATION = NULL")
    }

    changeSet(author: "uddesh-teke", id: "202404171558-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RULE_INFORMATION', columnName: 'IS_FIRST_TIME_RULE')
        }
        sql("UPDATE RULE_INFORMATION SET IS_FIRST_TIME_RULE = 0")
    }

    changeSet(author: "uddesh-teke", id: "202404171559-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RULE_INFORMATION', columnName: 'ACTION_ID')
        }
        sql("UPDATE RULE_INFORMATION SET ACTION_ID = NULL")
    }
}
