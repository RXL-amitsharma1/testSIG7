databaseChangeLog = {
    changeSet(author: "Uddesh Teke (generated)", id: "202405311809-0") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_WHO_DRUGS", type: "boolean", defaultValueBoolean: "false"){
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "Uddesh Teke (generated)", id: "202405311809-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_WHO_DRUGS", type: "boolean", defaultValueBoolean: "false"){
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "202406171756-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'MASTER_CONFIGURATION', columnName: 'INCLUDE_WHO_DRUGS')
            }
        }
        addColumn(tableName: "MASTER_CONFIGURATION") {
            column(name: "INCLUDE_WHO_DRUGS", type: "boolean", defaultValueBoolean: "false"){
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "202406171756-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'PRODUCT_NAME')
        }
        sql("alter table AGG_ALERT ALTER COLUMN PRODUCT_NAME TYPE VARCHAR(1000 );")
    }

    changeSet(author: "Amrendra (generated)", id: "202406171756-4") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PRODUCT_NAME')
        }
        sql("alter table SINGLE_CASE_ALERT ALTER COLUMN PRODUCT_NAME TYPE VARCHAR(1000 );")
    }

    changeSet(author: "Amrendra (generated)", id: "202406171756-5") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRODUCT_NAME')
        }
        sql("alter table ARCHIVED_AGG_ALERT ALTER COLUMN PRODUCT_NAME TYPE VARCHAR(1000 );")
    }

    changeSet(author: "Amrendra (generated)", id: "202406171756-6") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PRODUCT_NAME')
        }
        sql("alter table ARCHIVED_SINGLE_CASE_ALERT ALTER COLUMN PRODUCT_NAME TYPE VARCHAR(1000 );")
    }

    changeSet(author: "Amrendra (generated)", id: "202406171756-7") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'PRODUCT_NAME')
        }
        sql("alter table AGG_ON_DEMAND_ALERT ALTER COLUMN PRODUCT_NAME TYPE VARCHAR(1000 );")
    }

    changeSet(author: "Amrendra (generated)", id: "202406171756-8") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PRODUCT_NAME')
        }
        sql("alter table SINGLE_ON_DEMAND_ALERT ALTER COLUMN PRODUCT_NAME TYPE VARCHAR(1000 );")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "202407181159-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'DRUG_RECORD_NUMBERS')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "DRUG_RECORD_NUMBERS", type: "CLOB")
        }
    }


    changeSet(author: "Uddesh Teke (generated)", id: "202407181159-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'DRUG_RECORD_NUMBERS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DRUG_RECORD_NUMBERS", type: "CLOB")
        }
    }
}

