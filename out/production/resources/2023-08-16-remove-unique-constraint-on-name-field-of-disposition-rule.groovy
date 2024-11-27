databaseChangeLog = {
    changeSet(author: "Rishabh Rajpurohit", id: "16082023121356-9") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: "1"){
                sql("SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE TABLE_NAME = 'DISPOSITION_RULES' AND CONSTRAINT_NAME = 'UC_DISPOSITION_RULESNAME_COL';")
            }
        }
        dropUniqueConstraint(constraintName: "UC_DISPOSITION_RULESNAME_COL", tableName: "DISPOSITION_RULES")
    }
}