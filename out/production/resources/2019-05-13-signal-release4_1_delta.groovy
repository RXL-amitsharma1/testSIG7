import com.rxlogix.user.Preference
import grails.converters.JSON
import grails.util.Holders
import groovy.json.JsonSlurper

databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-1") {
        createTable(tableName: "GRP_ALERT_DISP") {
            column(name: "GRP_ALERT_DISP_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-2") {
        createTable(tableName: "spotfire_notification_pvuser") {
            column(name: "recipients_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-3") {
        createTable(tableName: "spotfire_notification_query") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "spotfire_notification_queryPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "configuration_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "executed_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "run_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'CASE_NARRATIVE')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "CASE_NARRATIVE", type: "clob")
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'CON_MEDS')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "CON_MEDS", type: "clob")
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-85") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'CON_COMIT')
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "CON_COMIT_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_CASE_ALERT set CON_COMIT_COPY = CON_COMIT;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "CON_COMIT")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "CON_COMIT_COPY", newColumnName: "CON_COMIT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-86") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ALERT_CON_COMIT', columnName: 'CON_COMIT')
        }
        addColumn(tableName: "SINGLE_ALERT_CON_COMIT") {
            column(name: "CON_COMIT_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_ALERT_CON_COMIT set CON_COMIT_COPY = CON_COMIT;")

        dropColumn(tableName: "SINGLE_ALERT_CON_COMIT", columnName: "CON_COMIT")

        renameColumn(tableName: "SINGLE_ALERT_CON_COMIT", oldColumnName: "CON_COMIT_COPY", newColumnName: "ALERT_CON_COMIT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-88") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ALERT_PT', columnName: 'PT')
        }
        addColumn(tableName: "SINGLE_ALERT_PT") {
            column(name: "PT_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_ALERT_PT set PT_COPY = PT;")

        dropColumn(tableName: "SINGLE_ALERT_PT", columnName: "PT")

        renameColumn(tableName: "SINGLE_ALERT_PT", oldColumnName: "PT_COPY", newColumnName: "SCA_PT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-89") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_ALERT_SUSP_PROD', columnName: 'PRODUCT_NAME')
        }
        addColumn(tableName: "SINGLE_ALERT_SUSP_PROD") {
            column(name: "PRODUCT_NAME_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update SINGLE_ALERT_SUSP_PROD set PRODUCT_NAME_COPY = PRODUCT_NAME;")

        dropColumn(tableName: "SINGLE_ALERT_SUSP_PROD", columnName: "PRODUCT_NAME")

        renameColumn(tableName: "SINGLE_ALERT_SUSP_PROD", oldColumnName: "PRODUCT_NAME_COPY", newColumnName: "SCA_PRODUCT_NAME")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SPOTFIRE_SETTINGS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SPOTFIRE_SETTINGS", type: "CLOB")
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'SPOTFIRE_SETTINGS')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "SPOTFIRE_SETTINGS", type: "CLOB")
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'WORKFLOW_GROUP')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE VALIDATED_SIGNAL SET WORKFLOW_GROUP = (SELECT nvl(ID,0) FROM GROUPS WHERE GROUP_TYPE = 'WORKFLOW_GROUP' AND NAME = 'Default')")
                confirm "Successfully set default value for WORKFLOW_GROUP for VALIDATED_SIGNAL table."
            }
        }
        addNotNullConstraint(tableName: "VALIDATED_SIGNAL", columnName: "WORKFLOW_GROUP", columnDataType: "number(19,0)")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'auto_route_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "auto_route_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-10") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'default_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "default_disposition_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update GROUPS set default_disposition_id = (SELECT ID FROM DISPOSITION where value = 'New');")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'default_signal_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "default_signal_disposition_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update GROUPS set default_signal_disposition_id = (SELECT ID FROM DISPOSITION where value = 'Validated Observation');")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'justification_text')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "justification_text", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "prashant.sahi (generated)", id: "1557756299465-83") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'GROUPS', columnName: 'DESCRIPTION')
        }
        modifyDataType(tableName: "GROUPS", columnName: "DESCRIPTION", newDataType: "varchar(4000)")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'workflow_group_id')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "workflow_group_id", type: "NUMBER(19, 0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE EX_LITERATURE_CONFIG SET workflow_group_id = (SELECT nvl(ID,0) FROM GROUPS WHERE GROUP_TYPE = 'WORKFLOW_GROUP' AND NAME = 'Default')")
                confirm "Successfully set default value for WORKFLOW_GROUP."
            }
        }
        addNotNullConstraint(tableName: "EX_LITERATURE_CONFIG", columnName: "workflow_group_id", columnDataType: "number(19,0)")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'workflow_group_id')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "workflow_group_id", type: "NUMBER(19, 0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE LITERATURE_CONFIG SET workflow_group_id = (SELECT nvl(ID,0) FROM GROUPS WHERE GROUP_TYPE = 'WORKFLOW_GROUP' AND NAME = 'Default')")
                confirm "Successfully set default value for WORKFLOW_GROUP."
            }
        }
        addNotNullConstraint(tableName: "LITERATURE_CONFIG", columnName: "workflow_group_id", columnDataType: "number(19,0)")
    }

    changeSet(author: 'APE', id: 'updateDashboardConfigInPreferences') {
        grailsChange {
            change {
                try {
                    Map inboxDefaultConfigMap = Holders.config.signal.dashboard.widgets.config.inbox.clone()
                    JsonSlurper slurper = new JsonSlurper()
                    Preference.findAllByDashboardConfigIsNotNull().each {
                        Map tempMap = slurper.parseText(it.dashboardConfig)
                        if (!tempMap.inbox) {
                            println inboxDefaultConfigMap
                            tempMap.inbox = inboxDefaultConfigMap
                            it.dashboardConfig = tempMap as JSON
                            it.save(flush: true)
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating dashboard preferences for liquibase change-set updateDashboardConfigInPreferences ####"
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK1lbgy2e2cp3bspkxb7ay9xsk7')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "recipients_id", baseTableName: "spotfire_notification_pvuser", constraintName: "FK1lbgy2e2cp3bspkxb7ay9xsk7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "spotfire_notification_query")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK7jp5xha2vlmovt0uxbyjki99')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "auto_route_disposition_id", baseTableName: "GROUPS", constraintName: "FK7jp5xha2vlmovt0uxbyjki99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK8rwroyawlknqh2oxffgfixhfe')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK8rwroyawlknqh2oxffgfixhfe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKajsw0c9d9gu2wlqqxvi1h1a30')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "GRP_ALERT_DISP_ID", baseTableName: "GRP_ALERT_DISP", constraintName: "FKajsw0c9d9gu2wlqqxvi1h1a30", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKco0q10j5riuj8tjwjxosurnx6')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "default_disposition_id", baseTableName: "GROUPS", constraintName: "FKco0q10j5riuj8tjwjxosurnx6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKjq3undeff3wy615xd3cjxb13l')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "default_signal_disposition_id", baseTableName: "GROUPS", constraintName: "FKjq3undeff3wy615xd3cjxb13l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKpigynbh2vwpt1xx49uypi3ddo')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "workflow_group_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKpigynbh2vwpt1xx49uypi3ddo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKqeqpmqe77w39we8m3l33b48kt')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "GRP_ALERT_DISP", constraintName: "FKqeqpmqe77w39we8m3l33b48kt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKrdeku7oiw606oc27n077l4agf')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "spotfire_notification_pvuser", constraintName: "FKrdeku7oiw606oc27n077l4agf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKrnu8n3wqjbeb1qdcgs36odomn')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "workflow_group_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKrnu8n3wqjbeb1qdcgs36odomn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-26") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_CLL_TEMPLT')
        }
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-27") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_DTAB_TEMPLT')
        }
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-28") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_CLL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-29") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_CUSTOM_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-30") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_DTAB_TEMPLT')
        }
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-31") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_NCASE_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-32") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_QUERY_EXP')
        }
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-33") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_QUERY_VALUE')
        }
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-34") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_SQL_VALUE')
        }
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-35") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-36") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_NONCASE_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-37") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_PARAM')
        }
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-38") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_QUERY_EXP_VALUE')
        }
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-39") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_QUERY_VALUE')
        }
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-40") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_RPT_TEMPLT')
        }
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-41") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-42") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-43") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_VALUE')
        }
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-44") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-45") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_VALUE')
        }
        dropTable(tableName: "HT_VALUE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-54") {
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "flagged", tableName: "AGG_ALERT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-56") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "RCONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-57") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "RCONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-58") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "PVUSER")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-59") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "PVUSER")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-60") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "evdas_user_download")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-61") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "evdas_user_download")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-62") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "COGNOS_REPORT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-63") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "COGNOS_REPORT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-64") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-65") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-66") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-67") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-68") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "RPT_ERROR")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-69") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "RPT_ERROR")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-70") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "RPT_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-71") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "RPT_TEMPLT")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-72") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-73") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-74") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "PREFERENCE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-75") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "PREFERENCE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-76") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "ROLE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-77") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "ROLE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-78") {
        modifyDataType(columnName: "username", newDataType: "varchar2(100 char)", tableName: "PVUSER")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-79") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "EX_RCONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-80") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "EX_RCONFIG")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-81") {
        modifyDataType(columnName: "CREATED_BY", newDataType: "varchar2(100 char)", tableName: "GROUPS")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-82") {
        modifyDataType(columnName: "MODIFIED_BY", newDataType: "varchar2(100 char)", tableName: "GROUPS")
    }

    changeSet(author: "prashantsahi (generated)", id: "1557756299465-87") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CUM_CASE_SERIES_EXEC_STATUS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CUM_CASE_SERIES_EXEC_STATUS", type: "varchar2(255 CHAR)")
        }
    }
}