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
            columnExists(tableName: "SIGNAL_RMMS", columnName: 'DESCRIPTION')
        }
        sql('alter table signal_rmms  ALTER COLUMN DESCRIPTION TYPE VARCHAR(8000 );')
    }

    changeSet(author: "isha (generated)", id: "1613989815343-426") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EVDAS_ON_DEMAND_ALERT' AND column_name = 'LISTEDNESS' ;")
        }
        dropNotNullConstraint(columnDataType: "BOOLEAN", columnName: "listedness", tableName: "EVDAS_ON_DEMAND_ALERT")
    }
    changeSet(author: "Isha (generated)", id: "1613989815343-430") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIONS', columnName: 'DETAILS')
        }
        sql("alter table ACTIONS ALTER COLUMN DETAILS TYPE VARCHAR(8000 );")
    }
    changeSet(author: "Bhupender (generated)", id: "1613989815355543-427") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '32000', "SELECT character_maximum_length FROM information_schema.columns WHERE column_name = 'sca_batch_lot_no' AND table_name = 'single_alert_batch_lot_no'")
            }
        }
        sql("alter table SINGLE_ALERT_BATCH_LOT_NO ALTER COLUMN SCA_BATCH_LOT_NO TYPE VARCHAR(32000);")
    }
}