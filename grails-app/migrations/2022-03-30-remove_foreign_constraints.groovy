databaseChangeLog = {
    changeSet(author: "Krishan (generated)", id: "1648639429-1") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK68guqmo0wadnf515ksx1x03sr")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_ALERT_TAG", constraintName: "FK68guqmo0wadnf515ksx1x03sr")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-2") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKeq4e569rluxwh2bh0fil7efp8")
        }
        dropForeignKeyConstraint(baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKeq4e569rluxwh2bh0fil7efp8")
    }


    changeSet(author: "Krishan (generated)", id: "1648639429-3") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKd74cpexfn1jrxsjhy23g3kqjb")
        }
        dropForeignKeyConstraint(baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FKd74cpexfn1jrxsjhy23g3kqjb")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-4") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKsy5o19eqrc5385sfoidonp43l")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKsy5o19eqrc5385sfoidonp43l")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-5") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK7uu820jqwt2ggpbwt5q1n3irg")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FK7uu820jqwt2ggpbwt5q1n3irg")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-6") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKiy6dx1s4p9odra4pxi597sr1s")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKiy6dx1s4p9odra4pxi597sr1s")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-7") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKayc7ooteg4uqk1y8oth5gi85w")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_TAGS", constraintName: "FKayc7ooteg4uqk1y8oth5gi85w")
    }

    changeSet(author: "Krishan (generated)", id: "1648639429-8") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK1spm3adg2morgiavbiypkcowf")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK1spm3adg2morgiavbiypkcowf")
    }
}
