databaseChangeLog = {
    changeSet(author: "Amrendra (generated)", id: "1613989815343-215") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'master_child_run_node')
            }
        }
        createTable(tableName: "master_child_run_node") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "node_name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "child_exec_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "master_exec_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "node_uuid", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "is_executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_resume", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "file_generated", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_save_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-216") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'master_config_status')
            }
        }
        createTable(tableName: "master_config_status") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "master_exec_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_flag", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "prr_flag", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "dss_flag", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_mining_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_count_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_ebgm_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_prr_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_dss_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "dss_to_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "dss_executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "node_name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "node_uuid", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "data_source", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "integrated_exec_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "is_event_group", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "data_persisted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "all_db_done", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_db_error", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "error_msg", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

        }
    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-220") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'IMPORT_CONFIGURATION_LOG', columnName: 'node_uuid')
            }
        }

        addColumn(tableName: "IMPORT_CONFIGURATION_LOG") {
            column(name: "node_uuid", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }

    }

    changeSet(author: "Amrendra (generated)", id: "1613989815343-221") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_pe_history_config')
            }
        }
        createIndex(indexName: "idx_pe_history_config", tableName: "product_event_history") {
            column(name: "config_id")
        }

    }

}