databaseChangeLog = {
    changeSet(author: "Krishna (generated)", id: "1665385734818-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_CONFIG', columnName: 'DESCRIPTION')
        }
        sql("alter table EVDAS_CONFIG  ALTER COLUMN DESCRIPTION TYPE VARCHAR(8000);")
    }

    changeSet(author: "Krishna (generated)", id: "1665385734818-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'DESCRIPTION')
        }
        sql("alter table EX_EVDAS_CONFIG  ALTER COLUMN DESCRIPTION TYPE VARCHAR(8000);")
    }
}