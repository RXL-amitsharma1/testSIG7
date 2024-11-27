databaseChangeLog = {
    changeSet(author: "Krishna (generated)", id: "160862657869511-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_OUTCOME', columnName: 'DISPOSITION_ID')
            }
        }
        addColumn(tableName: "SIGNAL_OUTCOME") {
            column(name: "DISPOSITION_ID", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Krishna (generated)", id: "1608626578691-2") {
        sql("update VALIDATED_SIGNAL set ACTUAL_DUE_DATE=DUE_DATE;")
    }
}