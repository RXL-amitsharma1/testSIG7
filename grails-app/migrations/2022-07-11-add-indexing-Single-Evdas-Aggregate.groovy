databaseChangeLog = {


    changeSet(author: "Nikhil (generated)", id: "9799655998989-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_SCA_CN_cnf')
            }
        }
        createIndex(indexName: "idx_SCA_CN_cnf", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ALERT_CONFIGURATION_ID")
            column(name: "CASE_NUMBER")
        }

    }
    changeSet(author: "Nikhil (generated)", id: "9799655998989-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_ASC_CN_cnf')
            }
        }
        createIndex(indexName: "idx_ASC_CN_cnf", tableName: "ARCHIVED_SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ALERT_CONFIGURATION_ID")
            column(name: "CASE_NUMBER")

        }

    }

    changeSet(author: "Nikhil (generated)", id: "9799655998989-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_AGG_PE_cnf')
            }
        }
        createIndex(indexName: "idx_AGG_PE_cnf", tableName: "AGG_ALERT", unique: "false") {
            column(name: "ALERT_CONFIGURATION_ID")
            column(name: "PRODUCT_NAME")
            column(name: "PT")

        }

    }

    changeSet(author: "Nikhil (generated)", id: "9799655998989-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_AAG_PE_cnf')
            }
        }
        createIndex(indexName: "idx_AAG_PE_cnf", tableName: "ARCHIVED_AGG_ALERT", unique: "false") {
            column(name: "ALERT_CONFIGURATION_ID")
            column(name: "PRODUCT_NAME")
            column(name: "PT")

        }

    }

    changeSet(author: "Nikhil (generated)", id: "9799655998989-05") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_EV_PE_cnf')
            }
        }
        createIndex(indexName: "idx_EV_PE_cnf", tableName: "EVDAS_ALERT", unique: "false") {
            column(name: "ALERT_CONFIGURATION_ID")
            column(name: "SUBSTANCE")
            column(name: "PT")
        }

    }

    changeSet(author: "Nikhil (generated)", id: "9799655998989-06") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_AEV_PE_cnf')
            }
        }
        createIndex(indexName: "idx_AEV_PE_cnf", tableName: "ARCHIVED_EVDAS_ALERT", unique: "false") {
            column(name: "ALERT_CONFIGURATION_ID")
            column(name: "SUBSTANCE")
            column(name: "PT")
        }

    }

}