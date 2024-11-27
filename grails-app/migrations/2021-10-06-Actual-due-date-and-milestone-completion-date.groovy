databaseChangeLog = {
    changeSet(author: "Krishna (generated)", id: "1632334132391233-151") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ACTUAL_DUE_DATE')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ACTUAL_DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Krishna (generated)", id: "1632334132321310-153") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'MILESTONE_COMPLETION_DATE')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "MILESTONE_COMPLETION_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }

    }
    changeSet(author: "Krishna (generated)", id: "16323341323213153-153") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'MILESTONE_COMPLETION_DATE')
        }
        sql("ALTER TABLE SIGNAL_STATUS_HISTORY DROP COLUMN MILESTONE_COMPLETION_DATE;")
    }

}