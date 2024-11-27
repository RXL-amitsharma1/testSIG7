databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1640244010-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'master_configuration')
            }
        }
        createTable(tableName: "master_configuration") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "master_configurationPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "as_of_version_date", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "config_template", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "lastx", type: "NUMBER(10, 0)") {
                constraints(nullable: "true")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "product_hierarchy", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "scheduler", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "SCHEDULE_DATEJSON", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "is_resume", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "num_of_executions", type: "NUMBER(10, 0)") {
                constraints(nullable: "true")
            }

            column(name: "next_run_date", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }

            column(name: "datasource", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "integrated_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'master_executed_configuration')
            }
        }
        createTable(tableName: "master_executed_configuration") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "master_executed_configPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "as_of_version_date", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "config_template", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "date_range_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "lastx", type: "NUMBER(10, 0)") {
                constraints(nullable: "true")
            }

            column(name: "master_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_hierarchy", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "scheduler", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "datasource", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "integrated_ex_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'master_config_id')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "master_config_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'is_latest_master')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "is_latest_master", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'master_ex_config_id')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "master_ex_config_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'is_master')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "is_master", type: "NUMBER(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'master_evdas_configuration')
            }
        }
        createTable(tableName: "master_evdas_configuration") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "master_evdas_configPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "master_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }

        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'master_evdas_executed_config')
            }
        }
        createTable(tableName: "master_evdas_executed_config") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "master_ev_executed_configPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "master_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'master_config_id')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "master_config_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1640244010-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'master_ex_config_id')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "master_ex_config_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1635495301070-369") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'run_count')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "run_count", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1635495301070-370") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'master_executed_configuration', columnName: 'run_count')
            }
        }
        addColumn(tableName: "master_executed_configuration") {
            column(name: "run_count", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1635495301070-371") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'disabled_dss_nodes')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "disabled_dss_nodes", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }

}
