databaseChangeLog = {


    changeSet(author: "Kundan (generated)", id: "1613989815343-410") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_AGG_ALERT_DISP_ID')
            }
        }
        createIndex(indexName: "IDX_AGG_ALERT_DISP_ID", tableName: "AGG_ALERT", unique: "false") {
            column(name: "DISPOSITION_ID")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-411") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_AGG_ALERT_ASSIGNEDTO')
            }
        }
        createIndex(indexName: "IDX_AGG_ALERT_ASSIGNEDTO", tableName: "AGG_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_ID")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-412") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_AGG_ALERT_ASSIGNEDTO_GRP')
            }
        }
        createIndex(indexName: "IDX_AGG_ALERT_ASSIGNEDTO_GRP", tableName: "AGG_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_GROUP_ID")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-413") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_RCONFIG_ADHOC_RUN')
            }
        }
        createIndex(indexName: "EX_RCONFIG_ADHOC_RUN", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "ADHOC_RUN")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-414") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_RCONFIG_IS_DELETED')
            }
        }
        createIndex(indexName: "EX_RCONFIG_IS_DELETED", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "IS_DELETED")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-415") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_EX_RCONFIG_ISLATEST')
            }
        }
        createIndex(indexName: "IX_EX_RCONFIG_ISLATEST", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "IS_LATEST")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-416") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_RCONFIG_WORKFLOW_GROUP')
            }
        }
        createIndex(indexName: "EX_RCONFIG_WORKFLOW_GROUP", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "WORKFLOW_GROUP")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-422") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_DISPOSITION_RVW_COMP')
            }
        }
        createIndex(indexName: "IDX_DISPOSITION_RVW_COMP", tableName: "DISPOSITION", unique: "false") {
            column(name: "REVIEW_COMPLETED")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-418") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_GROUP')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_GROUP", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_GROUP_ID")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-419") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_ASSIGNEDTO')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_ASSIGNEDTO", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_ID")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-420") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_CASESERIES')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_CASESERIES", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "IS_CASE_SERIES")
        }
    }
    changeSet(author: "Kundan (generated)", id: "1613989815343-421") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_DISPOSITION')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_DISPOSITION", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "DISPOSITION_ID")
        }
    }
    changeSet(author: "Isha (generated)", id: "1613989815343-423") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_RMMs', columnName: 'DESCRIPTION')
        }
        sql("alter table SIGNAL_RMMs modify DESCRIPTION VARCHAR2(8000 CHAR);")
    }

    changeSet(author: "isha (generated)", id: "1613989815343-426") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EVDAS_ON_DEMAND_ALERT' AND column_name = 'LISTEDNESS' ;")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(1)", columnName: "listedness", tableName: "EVDAS_ON_DEMAND_ALERT")
    }
    changeSet(author: "Isha (generated)", id: "1613989815343-430") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'DETAILS')
        }
        sql("alter table ACTIONS modify DETAILS VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "Bhupender (generated)", id: "1613989815355543-427") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '32000', "SELECT CHAR_LENGTH FROM ALL_TAB_COLUMNS WHERE COLUMN_NAME = 'SCA_BATCH_LOT_NO' AND TABLE_NAME = 'SINGLE_ALERT_BATCH_LOT_NO';")
            }
        }
        sql("alter table SINGLE_ALERT_BATCH_LOT_NO modify SCA_BATCH_LOT_NO VARCHAR2(32000);")
    }
}