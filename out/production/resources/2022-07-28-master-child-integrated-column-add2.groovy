databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "1613989815343-400") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_child_run_node', columnName: 'vaers_id')
            }
        }

        addColumn(tableName: "master_child_run_node") {
            column(name: "vaers_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-401") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_child_run_node', columnName: 'vigibase_id')
            }
        }

        addColumn(tableName: "master_child_run_node") {
            column(name: "vigibase_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-402") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'vaers_ex_ids')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "vaers_ex_ids", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-403") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'vigibase_ex_ids')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "vigibase_ex_ids", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-404") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'vaers_master_ex_id')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "vaers_master_ex_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-405") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_config_status', columnName: 'vigibase_master_ex_id')
            }
        }

        addColumn(tableName: "master_config_status") {
            column(name: "vigibase_master_ex_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

}