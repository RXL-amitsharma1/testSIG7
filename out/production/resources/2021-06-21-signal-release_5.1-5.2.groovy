import com.rxlogix.Constants
import com.rxlogix.EmergingIssueService
import com.rxlogix.config.Disposition
import com.rxlogix.config.EVDASDateRangeInformation
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEVDASDateRangeInformation
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.PriorityDispositionConfig
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.signal.SignalNotificationMemo
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.converters.JSON
import grails.util.Holders
import org.joda.time.DateTime

databaseChangeLog = {
    changeSet(author: "suraj (generated)", id: "1608626578695-2") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "select Count(*) from user_constraints where constraint_name = UPPER('UC_VALIDATED_SIGNALNAME_COL');")
        }
        dropUniqueConstraint(constraintName: "UC_VALIDATED_SIGNALNAME_COL", tableName: "VALIDATED_SIGNAL")
    }
    changeSet(author: "suraj (generated)", id: "1608626578695-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'IS_DUEDATE_UPDATED')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "IS_DUEDATE_UPDATED", type: "number(1, 0)", defaultValueBoolean: 'false') {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-4") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_NOTIFICATION_MEMO')
            }
        }
        createTable(tableName: "SIGNAL_NOTIFICATION_MEMO") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_NOTIFICATION_MEMOPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "config_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "signal_source", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "trigger_variable", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "trigger_value", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "email_subject", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "email_body", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }

            column(name: "email_address", type: "VARCHAR2(4000 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'criteria')
            }
        }
        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "criteria", type: "VARCHAR2(8000 CHAR)")
        }
    }


    changeSet(author: "ujjwal (generated)", id: "1608626578695-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_NOTIFICATION_MEMO', columnName: 'updated_by')
            }
        }
        addColumn(tableName: "SIGNAL_NOTIFICATION_MEMO") {
            column(name: "updated_by", type: "VARCHAR2(255 CHAR)", defaultValue: "System") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'MAIL_USERS_MEMO')
            }
        }
        createTable(tableName: "MAIL_USERS_MEMO") {
            column(name: "SIGNAL_NOTIFICATION_MEMO_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")

            column(name: "mail_users_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'MAIL_GROUPS_MEMO')
            }
        }
        createTable(tableName: "MAIL_GROUPS_MEMO") {
            column(name: "SIGNAL_NOTIFICATION_MEMO_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "mail_groups_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-9") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'SIGNAL_RMMS' AND column_name = 'STATUS' ;")
        }
        sql("ALTER TABLE SIGNAL_RMMs MODIFY status VARCHAR2(255 CHAR) NULL;")
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-10") {

        preConditions(onFail: 'MARK_RAN') {
        sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = 'SIGNAL_RMMS' AND column_name = 'DUE_DATE' ;")
    }
        sql("ALTER TABLE SIGNAL_RMMs MODIFY DUE_DATE TIMESTAMP(6) NULL;")
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-11") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'signal_email_log', columnName: 'assigned_to')
        }
        sql("alter table signal_email_log modify assigned_to VARCHAR2(8000 CHAR);")
    }
    changeSet(author: "ujjwal (generated)", id: "1608626578695-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_OUTCOME', columnName: 'IS_DISABLED')
            }
        }
        addColumn(tableName: "SIGNAL_OUTCOME") {
            column(name: "IS_DISABLED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608626578695-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_OUTCOME', columnName: 'IS_DELETED')
            }
        }
        addColumn(tableName: "SIGNAL_OUTCOME") {
            column(name: "IS_DELETED", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "rishabh (generated)", id: "1608626578695-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'emerging_issue', columnName: 'product_group_selection')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "product_group_selection", type: "varchar2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608626578695-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'emerging_issue', columnName: 'product_selection')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "product_selection", type: "CLOB")
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608626578695-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'emerging_issue', columnName: 'data_source_dict')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "data_source_dict", type: "varchar2(255 CHAR)")
        }
    }
    changeSet(author: "Nitesh (generated)", id: "1608626578695-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_TEMPLATE_ALERT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_TEMPLATE_ALERT", type: "number(1,0)", defaultValueBoolean: "false"){
                constraints(nullable: "true")
            }
        }
    }


    changeSet(author: "rishabh (generated)", id: "1608626578695-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'emerging_issue', columnName: 'products')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "products", type: "varchar2(8000 CHAR)")
        }
    }


    changeSet(author: "rishabh (generated)", id: "1608626578695-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'emerging_issue', columnName: 'events')
            }
        }
        addColumn(tableName: "emerging_issue") {
            column(name: "events", type: "varchar2(8000 CHAR)")
        }
    }

    //    >>>>>>>>>>>>>>>>>>>>>>>>>>SINGLE_CASE_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608626578695-25") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-26") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-27") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>ARCHIVED_SINGLE_CASE_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608626578695-28") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-29") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-30") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>>>AGG_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608626578695-31") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-32") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-33") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>ARCHIVED_AGG_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608626578695-34") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-35") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-36") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>EVDAS_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608626578695-37") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-38") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-39") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
//    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>ARCHIVED_EVDAS_ALERT>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    changeSet(author: "nitesh (generated)", id: "1608626578695-40") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'justification')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "justification", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-41") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'disp_performed_by')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "disp_performed_by", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-42") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'disp_last_change')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "disp_last_change", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-43") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table SINGLE_CASE_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-44") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table ARCHIVED_SINGLE_CASE_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-45") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'CASE_HISTORY', columnName: 'JUSTIFICATION')
        }
        sql("alter table CASE_HISTORY modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-46") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIVITIES', columnName: 'JUSTIFICATION')
        }
        sql("alter table ACTIVITIES modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-47") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table AGG_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-48") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table ARCHIVED_AGG_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-49") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'product_event_history', columnName: 'JUSTIFICATION')
        }
        sql("alter table product_event_history modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-50") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table EVDAS_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-51") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'JUSTIFICATION')
        }
        sql("alter table ARCHIVED_EVDAS_ALERT modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-52") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'evdas_history', columnName: 'JUSTIFICATION')
        }
        sql("alter table evdas_history modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-53") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_HISTORY', columnName: 'JUSTIFICATION')
        }
        sql("alter table LITERATURE_HISTORY modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-54") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'JUSTIFICATION')
        }
        sql("alter table LITERATURE_ACTIVITY modify JUSTIFICATION VARCHAR2(9000 CHAR);")
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-55") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'DETAILS')
        }
        sql("alter table LITERATURE_ACTIVITY modify DETAILS VARCHAR2(12000 CHAR);")
    }

    changeSet(author: "nitesh (generated)", id: "1608626578695-56") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'changes')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "changes", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608626578695-57") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'changes')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "changes", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608626578695-58") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'Trend_Flag')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "Trend_Flag", type:"varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-59") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PROD_N_PERIOD')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PROD_N_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-60") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PROD_N_CUMUL')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PROD_N_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-61") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'FREQ_PERIOD')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "FREQ_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-62") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'FREQ_CUMUL')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "FREQ_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-63") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'EBGM_RR')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "EBGM_RR", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-64") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'EBGM_E')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "EBGM_E", type: "number") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608626578695-65") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_A')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_A", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-66") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_B')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_B", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-67") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_C')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_C", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-68") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_D')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_D", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-69") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'Trend_Flag')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "Trend_Flag", type:"varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-70") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PROD_N_PERIOD')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PROD_N_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-71") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PROD_N_CUMUL')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PROD_N_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-72") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FREQ_PERIOD')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "FREQ_PERIOD", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-73") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FREQ_CUMUL')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "FREQ_CUMUL", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-74") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'EBGM_RR')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "EBGM_RR", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-75") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'EBGM_E')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "EBGM_E", type: "number") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608626578695-76") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_A')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_A", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-77") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_B')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_B", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-78") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_C')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_C", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578695-79") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PRR_D')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PRR_D", type: "number") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "amrendra (generated)", id: "1608626578695-80") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_susp_prod_alert_id')
            }
        }
        createIndex(indexName: "idx_susp_prod_alert_id", tableName: "SINGLE_ALERT_SUSP_PROD", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1608626578695-81") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_conmed_alert_id')
            }
        }
        createIndex(indexName: "idx_conmed_alert_id", tableName: "SINGLE_ALERT_CON_COMIT", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1608626578695-82") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_allpt_alert_id')
            }
        }
        createIndex(indexName: "idx_allpt_alert_id", tableName: "SINGLE_ALERT_PT", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1608626578695-83") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_patmed_alert_id')
            }
        }
        createIndex(indexName: "idx_patmed_alert_id", tableName: "SINGLE_ALERT_PAT_MED_HIST", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1608626578695-84") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_patdrugs_alert_id')
            }
        }
        createIndex(indexName: "idx_patdrugs_alert_id", tableName: "SINGLE_ALERT_PAT_HIST_DRUGS", unique: "false") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608824568995-56") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CLIPBOARD_CASES', columnName: 'TEMP_CASE_IDS')
            }
        }
        addColumn(tableName: "CLIPBOARD_CASES") {
            column(name: "TEMP_CASE_IDS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "1608824568995-57") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'NEW_COUNT_FREQ_CALC')
            }
        }

        addColumn(tableName: "AGG_ALERT") {
            column(name: "NEW_COUNT_FREQ_CALC", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "1608824568995-58") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'NEW_COUNT_FREQ_CALC')
            }
        }

        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "NEW_COUNT_FREQ_CALC", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608824568995-60") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PREVIOUS_DUE_DATE')
            }
        }

        addColumn(tableName: "AGG_ALERT") {
            column(name: "PREVIOUS_DUE_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608824568995-61") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PREVIOUS_DUE_DATE')
            }
        }

        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PREVIOUS_DUE_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608824568995-62") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PREVIOUS_DUE_DATE')
            }
        }

        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "PREVIOUS_DUE_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608824568995-63") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PREVIOUS_DUE_DATE')
            }
        }

        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "PREVIOUS_DUE_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608824568995-64") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'PREVIOUS_DUE_DATE')
            }
        }

        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "PREVIOUS_DUE_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608824568995-65") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT', columnName: 'PREVIOUS_DUE_DATE')
            }
        }

        addColumn(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "PREVIOUS_DUE_DATE", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }


}
