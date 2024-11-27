databaseChangeLog = {


    changeSet(author: "Bhupender (generated)", id: "16139839815343-410") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_AGG_ALERT_DISP_ID')
            }
        }
        createIndex(indexName: "IDX_AGG_ALERT_DISP_ID", tableName: "AGG_ALERT", unique: "false") {
            column(name: "DISPOSITION_ID")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "161398912815343-411") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_AGG_ALERT_ASSIGNEDTO')
            }
        }
        createIndex(indexName: "IDX_AGG_ALERT_ASSIGNEDTO", tableName: "AGG_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_ID")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "161398981455343-412") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_AGG_ALERT_ASSIGNEDTO_GRP')
            }
        }
        createIndex(indexName: "IDX_AGG_ALERT_ASSIGNEDTO_GRP", tableName: "AGG_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_GROUP_ID")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "161398981535243-413") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_RCONFIG_ADHOC_RUN')
            }
        }
        createIndex(indexName: "EX_RCONFIG_ADHOC_RUN", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "ADHOC_RUN")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "1613989815333243-414") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_RCONFIG_IS_DELETED')
            }
        }
        createIndex(indexName: "EX_RCONFIG_IS_DELETED", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "IS_DELETED")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "1613989815341233-415") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_EX_RCONFIG_ISLATEST')
            }
        }
        createIndex(indexName: "IX_EX_RCONFIG_ISLATEST", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "IS_LATEST")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "161398119815343-416") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'EX_RCONFIG_WORKFLOW_GROUP')
            }
        }
        createIndex(indexName: "EX_RCONFIG_WORKFLOW_GROUP", tableName: "EX_RCONFIG", unique: "false") {
            column(name: "WORKFLOW_GROUP")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "16112313989815343-4128") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_GROUP')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_GROUP", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_GROUP_ID")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "1613989815354143-418") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_ASSIGNEDTO')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_ASSIGNEDTO", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_ID")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "1613989815343-420") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_CASESERIES')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_CASESERIES", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "IS_CASE_SERIES")
        }
    }
    changeSet(author: "Bhupender (generated)", id: "161398981522343-421") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IDX_SINGLE_ALERT_DISPOSITION')
            }
        }
        createIndex(indexName: "IDX_SINGLE_ALERT_DISPOSITION", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "DISPOSITION_ID")
        }
    }
}