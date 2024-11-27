databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "1714653924159-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_AllPT_AlertId')
            }
        }
        createIndex(indexName: "idx_AllPT_AlertId", tableName: "single_alert_all_pt") {
            column(name: "SINGLE_ALERT_ID")
        }
    }
    changeSet(author: "Krishan Joshi (generated)", id: "1714653924159-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_ARCH_LIT_ARTICLE_ID')
            }
        }
        createIndex(indexName: "idx_ARCH_LIT_ARTICLE_ID", tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(name: "ARTICLE_ID")
        }
    }
    changeSet(author: "Uddesh Teke (generated)", id: "202405211432-1") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'NAME')
        }
        sql("ALTER TABLE RCONFIG ALTER COLUMN NAME TYPE VARCHAR(512);")
        sql("ALTER TABLE RCONFIG ALTER COLUMN NAME DROP NOT NULL;")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "202405211432-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'NAME')
        }
        sql("ALTER TABLE EX_RCONFIG ALTER COLUMN NAME TYPE VARCHAR(512 );")
        sql("ALTER TABLE EX_RCONFIG ALTER COLUMN NAME DROP NOT NULL;")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "202405211432-3") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'NAME')
        }
        sql("ALTER TABLE SINGLE_CASE_ALERT ALTER COLUMN NAME TYPE VARCHAR(512) ;")
        sql("ALTER TABLE SINGLE_CASE_ALERT ALTER COLUMN NAME DROP NOT NULL;")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "202405211432-4") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'NAME')
        }
        sql("ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT ALTER COLUMN NAME TYPE VARCHAR(512 );")
        sql("ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT ALTER COLUMN NAME DROP NOT NULL;")

    }

    changeSet(author: "Amrendra (generated)", id: "1714653924159-05") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_ex_status_status')
            }
        }
        createIndex(indexName: "idx_ex_status_status", tableName: "ex_status") {
            column(name: "ex_status")
        }
    }
}

