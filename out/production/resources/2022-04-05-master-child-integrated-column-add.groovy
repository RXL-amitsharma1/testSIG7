databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "1613989815343-300") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_child_run_node', columnName: 'faers_id')
            }
        }

        addColumn(tableName: "master_child_run_node") {
            column(name: "faers_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-301") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_child_run_node', columnName: 'evdas_id')
            }
        }

        addColumn(tableName: "master_child_run_node") {
            column(name: "evdas_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-302") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'faers_ex_ids')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "faers_ex_ids", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-304") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'evdas_ex_ids')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "evdas_ex_ids", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-305") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'faers_master_ex_id')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "faers_master_ex_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-306") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'evdas_master_ex_id')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "evdas_master_ex_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-307") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'all_child_done')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "all_child_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

}