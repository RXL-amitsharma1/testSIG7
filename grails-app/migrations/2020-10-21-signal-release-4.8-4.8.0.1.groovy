databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "1598698970934-60") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK8fru5jqrjb81756xvmoh203uw")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_GLOBAL_TAGS", constraintName: "FK8fru5jqrjb81756xvmoh203uw")
    }

    changeSet(author: "anshul (generated)", id: "1598698970935-61") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK7xpvynkdusubvmx9lc02em2i3")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FK7xpvynkdusubvmx9lc02em2i3")
    }

    changeSet(author: "anshul (generated)", id: "1598698970936-62") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKrbk26ckl20y61bnm3flko1mmk")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FKrbk26ckl20y61bnm3flko1mmk")
    }
}