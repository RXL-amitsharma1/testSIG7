import com.rxlogix.user.Role
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "Krishna Joshi (generated)", id: "16770424512813420-0010") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SYSTEM_PRE_CONFIG')
        }
        dropTable(tableName: "SYSTEM_PRE_CONFIG")
    }

    changeSet(author: "Krishna Joshi (generated)", id: "16770424512228420-0009") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SYSTEM_PRE_CONFIG')
            }
        }
        createTable(tableName: "SYSTEM_PRE_CONFIG") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SYS_PRE_CONF_PKY")
            }
            column(name: "name", type: "VARCHAR(255 )") {
                constraints(nullable: "false")
            }
            column(name: "display_name", type: "VARCHAR(255 )") {
                constraints(nullable: "false")
            }
            column(name: "PREVIOUS_RUNNING_STATUS", type: "boolean", defaultValue: "1") {
                constraints(nullable: "false")
            }
            column(name: "enabled", type: "boolean", defaultValue: "false") {
                constraints(nullable: "false")
            }
            column(name: "running", type: "boolean", defaultValue: "false") {
                constraints(nullable: "false")
            }
            column(name: "optional", type: "boolean", defaultValue: "false") {
                constraints(nullable: "false")
            }
            column(name: "warning", type: "boolean", defaultValue: "false") {
                constraints(nullable: "false")
            }
            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "reason", type: "VARCHAR(8000 )") {
                constraints(nullable: "true")
            }
            column(name: "app_type", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "db_type", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "validation_level", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "entity_type", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "entity_key", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "order_seq", type: "NUMBER(19, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "alert_type", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "table_space_time", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishna Joshi (generated)", id: "16770424512813420-00002") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SYSTEM_PRECHECK_EMAIL')
        }
        dropTable(tableName: "SYSTEM_PRECHECK_EMAIL")
    }

    changeSet(author: "Krishna Joshi (generated)", id: "16770424512840-00003") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SYSTEM_PRECHECK_EMAIL')
            }
        }
        createTable(tableName: "SYSTEM_PRECHECK_EMAIL") {
            column(name: "id", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SYS_PRE_EMAIL_PKY")
            }
            column(name: "name", type: "VARCHAR(255 )") {
                constraints(nullable: "false")
            }
            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "reason", type: "VARCHAR(8000 )") {
                constraints(nullable: "true")
            }
            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "email_sent", type: "boolean", defaultValue: "false") {
                constraints(nullable: "false")
            }
            column(name: "app_type", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
            column(name: "db_type", type: "VARCHAR(255 )") {
                constraints(nullable: "true")
            }
        }
    }
}


