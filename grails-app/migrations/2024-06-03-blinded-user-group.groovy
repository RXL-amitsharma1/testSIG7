databaseChangeLog ={
    changeSet(author: 'bhupender', id: '202406042303-01'){
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'user_group_field_mapping')
            }
        }
        createTable(tableName: "user_group_field_mapping") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "version", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "BLINDED_FIELDS", type: "CLOB") {
                constraints(nullable: "true")
            }
            column(name: "REDACTED_FIELDS", type: "CLOB") {
                constraints(nullable: "true")
            }

            column(name: "AVAILABLE_FIELDS", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bhupender", id: "202406042303-02") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'single_case_alert)', columnName: 'study_blinding_status')
            }
        }
        addColumn(tableName: "single_case_alert") {
            column(name: "study_blinding_status", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bhupender", id: "202406042303-03") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'single_case_alert)', columnName: 'case_blinding_status')
            }
        }
        addColumn(tableName: "single_case_alert") {
            column(name: "case_blinding_status", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Gaurav (generated)", id: "202406042303-04") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'archived_single_case_alert)', columnName: 'study_blinding_status')
            }
        }
        addColumn(tableName: "archived_single_case_alert") {
            column(name: "study_blinding_status", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Gaurav (generated)", id: "202406042303-05") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'archived_single_case_alert)', columnName: 'case_blinding_status')
            }
        }
        addColumn(tableName: "archived_single_case_alert") {
            column(name: "case_blinding_status", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bhupender", id: "202406042303-06") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'single_case_alert)', columnName: 'reported_product_name')
            }
        }
        addColumn(tableName: "single_case_alert") {
            column(name: "reported_product_name", type: "varchar(8000)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bhupender", id: "202406042303-07") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'archived_single_case_alert)', columnName: 'reported_product_name')
            }
        }
        addColumn(tableName: "archived_single_case_alert") {
            column(name: "reported_product_name", type: "varchar(8000)") {
                constraints(nullable: "true")
            }
        }
    }

}