databaseChangeLog = {
    changeSet(author: "Rishabh Rajpurohit", id: "16082023121356-9") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: "1") {
                sql("""
                SELECT COUNT(*) 
                FROM pg_constraint c
                JOIN pg_class t ON t.oid = c.conrelid
                WHERE t.relname = 'disposition_rules'
                AND c.conname = 'uc_disposition_rulesname_col'
                AND c.contype = 'u';
            """)
            }
        }
        dropUniqueConstraint(constraintName: "uc_disposition_rulesname_col", tableName: "disposition_rules")
    }
}