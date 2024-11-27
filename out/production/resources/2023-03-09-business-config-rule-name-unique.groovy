databaseChangeLog = {
    changeSet(author: "farhanali (generated)", id: "1675537102576-01") {
        addUniqueConstraint(columnNames: "RULE_NAME", constraintName: "BUSINESS_RULE_NAME_COL", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "farhanali (generated)", id: "1675537102576-02") {
        addUniqueConstraint(columnNames: "RULE_NAME", constraintName: "RULE_INFO_RULE_NAME", tableName: "RULE_INFORMATION")
    }
}