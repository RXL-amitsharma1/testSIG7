databaseChangeLog = {

    changeSet(author: "Nikhil (generated)", id: "68297392739-1") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'MASTER_PREF_TERM_ALL')
        }

        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update SINGLE_CASE_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }

    changeSet(author: "Nikhil (generated)", id: "68297392739-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'MASTER_PREF_TERM_ALL')
        }

        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update ARCHIVED_SINGLE_CASE_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }

    changeSet(author: "Nikhil (generated)", id: "68297392739-3") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'MASTER_PREF_TERM_ALL')
        }

        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update SINGLE_ON_DEMAND_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "SINGLE_ON_DEMAND_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "SINGLE_ON_DEMAND_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }
}