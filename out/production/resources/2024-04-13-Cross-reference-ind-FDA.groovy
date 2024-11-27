databaseChangeLog = {
    changeSet(author: "rishabh-goswami ", id: "160120242339-01") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'cross_reference_ind')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "cross_reference_ind", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh-goswami ", id: "160120242339-02") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'cross_reference_ind')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "cross_reference_ind", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh-goswami", id: "160120242339-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_CROSS_REFERENCE_IND')
            }
        }
        createTable(tableName: "SINGLE_ALERT_CROSS_REFERENCE_IND") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CROSS_REFERENCE_IND", type: "varchar(15000)")

            column(name: "CROSS_REFERENCE_IND_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "rishabh-goswami", id: "160120242339-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SINGLE_ALERT_CROSS_REFERENCE_IND')
            }
        }
        createTable(tableName: "ARCHIVED_SINGLE_ALERT_CROSS_REFERENCE_IND") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CROSS_REFERENCE_IND", type: "varchar(15000)")

            column(name: "CROSS_REFERENCE_IND_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "rishabh-goswami", id: "180120240858-01") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveMasterAlertDataFromArchiveTables.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }

    changeSet(author: "rishabh-goswami ", id: "180120240858-02") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'cross_reference_ind')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "cross_reference_ind", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh-goswami", id: "180120240858-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIG_DMD_ALRT_CROSS_REFERENCE_IND')
            }
        }
        createTable(tableName: "SIG_DMD_ALRT_CROSS_REFERENCE_IND") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CROSS_REFERENCE_IND", type: "varchar(15000)")

            column(name: "CROSS_REFERENCE_IND_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }
}