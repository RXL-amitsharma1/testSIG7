databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "2097654321340-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ALERT_PAT_MED_HIST', columnName: 'SCA_PAT_MED_HIST')
        }
        sql("alter table SINGLE_ALERT_PAT_MED_HIST modify SCA_PAT_MED_HIST VARCHAR2(3000 CHAR);")
    }

    changeSet(author: "Amrendra (generated)", id: "2097654321340-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AR_SIN_ALERT_PAT_MED_HIST', columnName: 'SCA_PAT_MED_HIST')
        }
        sql("alter table AR_SIN_ALERT_PAT_MED_HIST modify SCA_PAT_MED_HIST VARCHAR2(3000 CHAR);")
    }

    changeSet(author: "Amrendra (generated)", id: "2097654321340-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_DEMAND_PAT_MED_HIST', columnName: 'SCA_PAT_MED_HIST')
        }
        sql("alter table SINGLE_DEMAND_PAT_MED_HIST modify SCA_PAT_MED_HIST VARCHAR2(3000 CHAR);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "202403191628-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'DETAILS')
        }
        sql("alter table LITERATURE_ACTIVITY add DETAILS1 clob")
        sql("update LITERATURE_ACTIVITY set DETAILS1=DETAILS")
        sql("alter table LITERATURE_ACTIVITY drop column DETAILS")
        sql("alter table LITERATURE_ACTIVITY rename column DETAILS1 to DETAILS")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "202404111250-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PRIORITY', columnName: 'LAST_UPDATED')
            }
        }
        addColumn(tableName: "PRIORITY") {
            column(name: "LAST_UPDATED", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
}