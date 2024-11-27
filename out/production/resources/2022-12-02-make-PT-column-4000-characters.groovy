databaseChangeLog = {

    changeSet(author: "Nikhil (generated)", id: "76655443311-47") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PT')
        }
        sql("alter table SINGLE_CASE_ALERT modify PT VARCHAR2(4000 CHAR);")
    }

    changeSet(author: "Nikhil (generated)", id: "76655443311-48") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PT')
        }
        sql("alter table ARCHIVED_SINGLE_CASE_ALERT modify PT VARCHAR2(4000 CHAR);")
    }

    changeSet(author: "Nikhil (generated)", id: "76655443311-49") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PT')
        }
        sql("alter table SINGLE_ON_DEMAND_ALERT modify PT VARCHAR2(4000 CHAR);")
    }
}