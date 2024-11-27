databaseChangeLog = {
    changeSet(author: 'Rishabh (generated)', id: '202409180957-0001'){
        preConditions(onFail: 'MARK_RAN'){
            not{
                tableExists(tableName: 'ALERT_GROUP')
            }
        }
        createTable(tableName: 'ALERT_GROUP'){
            column(name: 'ID', type: 'NUMBER(19,0)'){
                constraints(nullable: false, primaryKey: true)
            }
            column(name: 'NAME', type: 'VARCHAR(4000)'){
                constraints(nullable: false, unique: true)
            }
            column(name: 'VERSION', type: 'NUMBER(19,0)'){
                constraints(nullable: false)
            }
            column(name: 'IS_GROUPING', type: 'boolean'){
                constraints(nullable: true)
            }
        }
    }
    changeSet(author: 'Rishabh (generated)', id: '202409180957-0002'){
        preConditions(onFail: 'MARK_RAN'){
            columnExists(tableName: 'RCONFIG', columnName: 'ALERT_GROUP')
        }
        sql("ALTER TABLE RCONFIG DROP COLUMN ALERT_GROUP;")
    }
    changeSet(author: 'Rishabh (generated)', id: '202409180957-0003'){
        preConditions(onFail: 'MARK_RAN'){
            columnExists(tableName: 'RCONFIG', columnName: 'UNIQUE_ID')
        }
        sql("ALTER TABLE RCONFIG DROP COLUMN UNIQUE_ID;")
    }
    changeSet(author: 'Rishabh (generated)', id: '202409180957-0004'){
        preConditions(onFail: 'MARK_RAN'){
            columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_GROUP')
        }
        sql("ALTER TABLE EX_RCONFIG DROP COLUMN ALERT_GROUP;")
    }
    changeSet(author: 'Rishabh (generated)', id: '202409180957-0005'){
        preConditions(onFail: 'MARK_RAN'){
            columnExists(tableName: 'EX_RCONFIG', columnName: 'UNIQUE_ID')
        }
        sql("ALTER TABLE EX_RCONFIG DROP COLUMN UNIQUE_ID;")
    }
    changeSet(author: 'Rishabh (generated)', id: '202409180957-0006'){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_GROUP_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_GROUP_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: 'Rishabh (generated)', id: '202409180957-0007'){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_GROUP_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALERT_GROUP_ID", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: 'Bhupender (generated)', id:'202409180957-0008'){
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'GROUPED_ALERT_INFO')
            }
        }
            createTable(tableName: 'GROUPED_ALERT_INFO'){
                column(name: 'id', type: 'NUMBER(19,0)'){
                    constraints(nullable: false,primaryKey: true)
                }
                column(name: 'version', type: 'NUMBER(19,0)'){
                    constraints(nullable: false)
                }
                column(name: 'name', type: 'VARCHAR(4000)'){
                   constraints(nullable:false)
                }
                column(name: 'is_latest', type: 'boolean'){
                  constraints(nullable: true)
                }
                column(name: 'is_enabled', type: 'boolean'){
                    constraints(nullable: true)
                }
                column(name: 'is_deleted', type: 'boolean'){
                    constraints(nullable: true)
                }
                column(name: 'assigned_to_id', type: 'NUMBER(19,0)'){
                    constraints(nullable: true)
                }
                column(name: 'assigned_to_group_id', type: 'NUMBER(19,0)'){
                    constraints(nullable: true)
                }
                column(name: "product_dictionary_selection", type: "VARCHAR(255 )"){
                    constraints(nullable: true)
                }
                column(name: "product_selection", type: "TEXT"){
                    constraints(nullable: true)
                }
                column(name: "product_group_selection", type: "TEXT"){
                    constraints(nullable: true)
                }
                column(name: "disp_counts", type: "VARCHAR(4000)"){
                    constraints(nullable: true)
                }
                column(name: "date_created", type: "timestamp") {
                    constraints(nullable: "false")
                }
                column(name: "last_updated", type: "timestamp") {
                    constraints(nullable: "false")
                }
            }
    }

    changeSet(author: 'Bhupender (generated)', id:'202409180957-0009'){
        preConditions(onFail: 'MARK_RAN'){
            not{
                tableExists(tableName: 'GROUP_ALERT_EXEC_CONFIG')
            }
        }
        createTable(tableName: 'GROUP_ALERT_EXEC_CONFIG'){
            column(name: 'EXEC_CONFIG_ID',type: 'NUMBER(19,0)'){
                constraints(nullable: false)
            }
            column(name: 'ALERT_GROUP_INFO_ID',type: 'NUMBER(19,0)'){
                constraints(nullable: false)
            }
            column(name: 'GROUPED_ALERT_EX_RCONFIG_IDX', type: 'NUMBER(19,0)')
        }
    }

    changeSet(author: "bhupender (generated)", id: "202409180957-010") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "GROUPED_ALERT_INFO", constraintName: "FKa2kbegt02usojx08bka5tutqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }
    changeSet(author: "bhupender (generated)", id: "202409180957-011") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "GROUPED_ALERT_INFO", constraintName: "FKa2kbegt02usojx08bka5tumqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }
    changeSet(author: 'bhupender (generated)', id: '202409180957-012') {
        addForeignKeyConstraint(baseColumnNames: "EXEC_CONFIG_ID", baseTableName: "GROUP_ALERT_EXEC_CONFIG", constraintName: "FKa2kbegt02unt5daka5tumqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName:"EX_RCONFIG")
    }

    changeSet(author: 'Bhupender (generated)', id: '202409180957-0013'){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: "RCONFIG", columnName: 'excluded_alert_groups')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "excluded_alert_groups", type: "VARCHAR(4000)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: 'Bhupender (generated)', id: "202409180957-0014"){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: 'excluded_alert_groups')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "excluded_alert_groups", type: "VARCHAR(4000)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: 'Bhupender (generated)', id: "202409180957-0015"){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: "GROUPED_ALERT_INFO", columnName: 'master_template_config_id')
            }
        }

        addColumn(tableName: "GROUPED_ALERT_INFO") {
            column(name: "master_template_config_id", type: "NUMERIC(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: 'Bhupender (generated)', id: "202409180957-0016"){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: "GROUPED_ALERT_INFO", columnName: 'ex_master_template_config_id')
            }
        }

        addColumn(tableName: "GROUPED_ALERT_INFO") {
            column(name: "ex_master_template_config_id", type: "NUMERIC(19,0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: 'Rishabh (generated)', id: "202410230952-0001"){
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: "GROUPED_ALERT_INFO", columnName: 'alert_group_id')
            }
        }

        addColumn(tableName: "GROUPED_ALERT_INFO") {
            column(name: "alert_group_id", type: "NUMERIC(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: 'bhupender (generated)', id: '202409180957-017') {
        addForeignKeyConstraint(baseColumnNames: "master_template_config_id", baseTableName: "GROUPED_ALERT_INFO", constraintName: "FKa2kbegt02unt5daka5tptbs3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName:"RCONFIG")
    }

    changeSet(author: 'bhupender (generated)', id: '202409180957-018') {
        addForeignKeyConstraint(baseColumnNames: "ex_master_template_config_id", baseTableName: "GROUPED_ALERT_INFO", constraintName: "FKa2kbegt02unt5daka5tptbs1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName:"EX_RCONFIG")
    }



}