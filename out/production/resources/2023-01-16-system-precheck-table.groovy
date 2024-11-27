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
            column(name: "name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "display_name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "previous_running_status ", type: "NUMBER(1, 0)", defaultValue: "1") {
                constraints(nullable: "false")
            }
            column(name: "enabled", type: "NUMBER(1, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "running", type: "NUMBER(1, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "optional", type: "NUMBER(1, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "warning", type: "NUMBER(1, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "reason", type: "varchar2(8000 char)") {
                constraints(nullable: "true")
            }
            column(name: "app_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "db_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "validation_level", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "entity_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "entity_key", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "order_seq", type: "NUMBER(19, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "alert_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "table_space_time", type: "varchar2(255 char)") {
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
            column(name: "name", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }
            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "reason", type: "varchar2(8000 char)") {
                constraints(nullable: "true")
            }
            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }
            column(name: "email_sent", type: "NUMBER(1, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
            column(name: "app_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
            column(name: "db_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
}


