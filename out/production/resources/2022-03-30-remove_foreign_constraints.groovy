databaseChangeLog = {
    changeSet(author: "Krishan (generated)", id: "1648639429-1") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK68GUQMO0WADNF515KSX1X03SR")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_ALERT_TAG", constraintName: "FK68GUQMO0WADNF515KSX1X03SR")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-2") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKEQ4E569RLUXWH2BH0FIL7EFP8")
        }
        dropForeignKeyConstraint(baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKEQ4E569RLUXWH2BH0FIL7EFP8")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-3") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKD74CPEXFN1JRXSJHY23G3KQJB")
        }
        dropForeignKeyConstraint(baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FKD74CPEXFN1JRXSJHY23G3KQJB")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-4") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKSY5O19EQRC5385SFOIDONP43L")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKSY5O19EQRC5385SFOIDONP43L")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-5") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK7UU820JQWT2GGPBWT5Q1N3IRG")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FK7UU820JQWT2GGPBWT5Q1N3IRG")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-6") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKIY6DX1S4P9ODRA4PXI597SR1S")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKIY6DX1S4P9ODRA4PXI597SR1S")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-7") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKAYC7OOTEG4UQK1Y8OTH5GI85W")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_TAGS", constraintName: "FKAYC7OOTEG4UQK1Y8OTH5GI85W")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-8") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK1SPM3ADG2MORGIAVBIYPKCOWF")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK1SPM3ADG2MORGIAVBIYPKCOWF")
    }
}
