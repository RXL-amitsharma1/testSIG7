databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1716353150378-0001") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ALERT_THERAPY_DATES', columnName: 'SCA_THERAPY_DATES')
        }
        sql("alter table SINGLE_ALERT_THERAPY_DATES modify SCA_THERAPY_DATES VARCHAR2(3000 CHAR);")
    }
    changeSet(author: "Hemlata (generated)", id: "1716353150378-0002") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_DEMAND_THERAPY_DATES', columnName: 'SCA_THERAPY_DATES')
        }
        sql("alter table SINGLE_DEMAND_THERAPY_DATES modify SCA_THERAPY_DATES VARCHAR2(3000 CHAR);")
    }
    changeSet(author: "Hemlata (generated)", id: "1716353150378-0003") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AR_SIN_ALERT_THERAPY_DATES', columnName: 'SCA_THERAPY_DATES')
        }
        sql("alter table AR_SIN_ALERT_THERAPY_DATES modify SCA_THERAPY_DATES VARCHAR2(3000 CHAR);")
    }

}