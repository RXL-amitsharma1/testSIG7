import com.rxlogix.GroupService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.UserService
import com.rxlogix.PvsAlertTagService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.UserDashboardCounts
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import groovy.json.JsonBuilder
import groovy.sql.Sql
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.SingleCaseAlertService

databaseChangeLog = {

    changeSet(author: "amit (generated)", id: "1608824568972-68") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'cum_geriatric_count')
            }
        }

        addColumn(tableName: "AGG_ALERT") {
            column(name: "cum_geriatric_count", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-69") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'cum_non_serious')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "cum_non_serious", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-70") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'new_geriatric_count')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "new_geriatric_count", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-71") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'new_non_serious')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "new_non_serious", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-72") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'cum_geriatric_count')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "cum_geriatric_count", type: "number(10, 0)" , defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-73") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'cum_non_serious')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "cum_non_serious", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-74") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'new_geriatric_count')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "new_geriatric_count", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-75") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'new_non_serious')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "new_non_serious", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-76") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_WORKFLOWRULES_GROUPS')
            }
        }
        createTable(tableName: "SIGNAL_WORKFLOWRULES_GROUPS") {
            column(name: "SIGNAL_WORKFLOW_RULE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-77") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_WORKFLOW_RULE')
            }
        }
        createTable(tableName: "SIGNAL_WORKFLOW_RULE") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_WORKFLOW_RULEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "from_state", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "to_state", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-78") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_WORKFLOW_RULE', columnName: 'date_created')
            }
        }
        addColumn(tableName: "SIGNAL_WORKFLOW_RULE") {
            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-79") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_WORKFLOW_STATE')
            }
        }
        createTable(tableName: "SIGNAL_WORKFLOW_STATE") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_WORKFLOW_STATEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "default_display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "due_in_display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-80") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_WkFL_STATE_DISPOSITIONS')
            }
        }
        createTable(tableName: "SIGNAL_WkFL_STATE_DISPOSITIONS") {
            column(name: "SIGNAL_WORKFLOW_STATE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-81") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK54cfbd7h3c167gaqlyabvg3kv')
            }
        }

        addForeignKeyConstraint(baseColumnNames: "SIGNAL_WORKFLOW_STATE_ID", baseTableName: "SIGNAL_WkFL_STATE_DISPOSITIONS", constraintName: "FK54cfbd7h3c167gaqlyabvg3kv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_WORKFLOW_STATE")
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-82") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKccvy6qv59qmm3iuurrx835kxa')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_WORKFLOW_RULE_ID", baseTableName: "SIGNAL_WORKFLOWRULES_GROUPS", constraintName: "FKccvy6qv59qmm3iuurrx835kxa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_WORKFLOW_RULE")
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-83") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKdlb58c2f4w9w8ku3pqkttavyc')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "SIGNAL_WkFL_STATE_DISPOSITIONS", constraintName: "FKdlb58c2f4w9w8ku3pqkttavyc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-84") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKgybopaos7fxg7ba01jeond3do')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SIGNAL_WORKFLOWRULES_GROUPS", constraintName: "FKgybopaos7fxg7ba01jeond3do", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-85") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'SIGNAL_STATUS')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "SIGNAL_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1608824568972-86") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'SIGNAL_STATUS')
        }
        grailsChange {
            change{
                try {
                    List validatedList = ValidatedSignal.list().id
                    List statusList = ValidatedSignal.createCriteria().list {
                        projections{
                            property("id")
                        }
                        'signalStatusHistories' {
                            'eq'('signalStatus', "Date Closed")
                        }
                    }


                    sql.withBatch(100, "UPDATE Validated_signal SET SIGNAL_STATUS = :signalStatus WHERE ID = :id", { preparedStatement ->
                        validatedList.each { id ->
                            preparedStatement.addBatch(id: id, signalStatus: statusList.find {it == id} ? "Date Closed" : "Ongoing")
                        }
                    })
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Auto Route Disposition for Literature Alerts ###########")
                }
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-87") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'combo_flag')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "combo_flag", type: "varchar2(255 CHAR)", defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-88") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'malfunction')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "malfunction", type: "varchar2(255 CHAR)", defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-89") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'combo_flag')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "combo_flag", type: "varchar2(255 CHAR)",defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-90") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'malfunction')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "malfunction", type: "varchar2(255 CHAR)",defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-91") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'combo_flag')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "combo_flag", type: "varchar2(255 CHAR)", defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-92") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'malfunction')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "malfunction", type: "varchar2(255 CHAR)", defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    /*changeSet(author: "amit (generated)", id: "1608824568972-93") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'combo_flag')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "combo_flag", type: "varchar2(255 CHAR)",defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-94") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'malfunction')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "malfunction", type: "varchar2(255 CHAR)",defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }*/

    changeSet(author: "nitesh (generated)", id: "1608824568972-95") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ATTACHMENT', columnName: 'REFERENCE_TYPE')
            }
        }
        addColumn(tableName: "ATTACHMENT") {
            column(name: "REFERENCE_TYPE", type: "varchar2(255 CHAR)")
        }
        sql("UPDATE ATTACHMENT SET REFERENCE_TYPE='Others' where REFERENCE_TYPE='null' and LNK_ID in( SELECT ID from ATTACHMENT_LINK where REFERENCE_CLASS = 'com.rxlogix.signal.ValidatedSignal');")
    }

    changeSet(author: "nitesh (generated)", id: "1608824568972-96") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SPOTFIRE_NOTIFICATION_QUERY', columnName: 'SIGNAL_PARAMETERS')
            }
        }
        addColumn(tableName: "SPOTFIRE_NOTIFICATION_QUERY") {
            column(name: "SIGNAL_PARAMETERS", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568972-97") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SPOTFIRE_NOTIFICATION_QUERY', columnName: 'IS_ENABLED')
            }
        }
        addColumn(tableName: "SPOTFIRE_NOTIFICATION_QUERY") {
            column(name: "IS_ENABLED", type: "number(1,0)", defaultValueBoolean: "false")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568972-98") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ATTACHMENT', columnName: 'INPUT_NAME')
        }
        sql("alter table ATTACHMENT modify INPUT_NAME VARCHAR2(4000 CHAR);")
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-99") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'view_instance', columnName: 'icon_seq')
            }
        }
        addColumn(tableName: "view_instance") {
            column(name: "icon_seq", type: "varchar2(255 CHAR)")
        }
        sql(''' UPDATE VIEW_INSTANCE SET ICON_SEQ = '{"#ic-create-signal":false,"#ic-toggle-column-filters":false,"#ic-exportTypes":false,"#ic-configureValidatedSignalFields":false}' where alert_type = 'Signal Management' ''')
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-100") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'view_instance', columnName: 'temp_Column_Seq')
        }
        sql(''' UPDATE VIEW_INSTANCE SET temp_Column_Seq = NULL where alert_type = 'Signal Management' ''')
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-101") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_RMMs')
            }
        }
        createTable(tableName: "SIGNAL_RMMs") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_RMMsPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "due_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-102") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'communication_type')
            }
        }
        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "communication_type", type: "varchar2(255 CHAR)", defaultValue: 'rmmType')
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-103") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'email_Address')
            }
        }
        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "email_Address", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-104") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'COUNTRY')
            }
        }
        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "COUNTRY", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-105") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'date_created')
            }
        }
        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-106") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'email_sent')
            }
        }
        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "email_sent", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-107") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_RMMs', columnName: 'signal_email_log_id')
            }
        }
        addColumn(tableName: "SIGNAL_RMMs") {
            column(name: "signal_email_log_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-174") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'col_Order')
            }
        }
        addColumn(tableName: "PVUSER") {
            column(name: "col_Order", type: "varchar2(2000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-175") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PVUSER', columnName: 'col_USER_Order')
            }
        }
        addColumn(tableName: "PVUSER") {
            column(name: "col_USER_Order", type: "varchar2(2000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-108") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKoberuc40mjb3128ejdorbau0e')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SIGNAL_RMMs", constraintName: "FKoberuc40mjb3128ejdorbau0e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-109") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKow3p31b4b79hwuuy57jouk2jr')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "SIGNAL_RMMs", constraintName: "FKow3p31b4b79hwuuy57jouk2jr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-110") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIGNAL_SIG_RMMS')
            }
        }
        createTable(tableName: "SIGNAL_SIG_RMMS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIG_RMM_ID", type: "NUMBER(19, 0)")

            column(name: "PARAM_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-111") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKawq2m3d2ipw0es858bfmat8y9')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SIG_RMM_ID", baseTableName: "SIGNAL_SIG_RMMS", constraintName: "FKawq2m3d2ipw0es858bfmat8y9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_RMMs")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-112") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'signal_email_log')
            }
        }
        createTable(tableName: "signal_email_log") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "signal_email_logPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "body", type: "VARCHAR2(8000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "subject", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-113") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKpi5e4cypnqf6yihkdpyso1f2c')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "signal_email_log_id", baseTableName: "SIGNAL_RMMs", constraintName: "FKpi5e4cypnqf6yihkdpyso1f2c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "signal_email_log")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-114") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ATTACHMENT', columnName: 'REFERENCE_TYPE')
            }
        }
        addColumn(tableName: "ATTACHMENT") {
            column(name: "REFERENCE_TYPE", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-115") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'WORKFLOW_STATE')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "WORKFLOW_STATE", type: "varchar2(255 CHAR)", defaultValue: "Safety Observation Validation")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-116") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SIGNAL_STATUS_HISTORY', columnName: 'FROM_SIGNAL_STATUS')
            }
        }
        addColumn(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "FROM_SIGNAL_STATUS", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-117") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SYSTEM_CONFIG')
            }
        }
        createTable(tableName: "SYSTEM_CONFIG") {
            column(name: "id", type: "NUMBER(19,0)") {
                constraints(primaryKey: "true", primaryKeyName: "SYSTEM_CONFIGPK")
            }

            column(name: "ENABLE_SIGNAL_WORKFLOW", type: "number(1, 0)", defaultValue: "0"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-118") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'WS_UPDATED')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "WS_UPDATED", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-119") {
        sql("update VALIDATED_SIGNAL set generic_Comment = description;")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-120") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PVUSER', columnName: 'ENABLE_SIGNAL_WORKFLOW')
        }
        sql("ALTER TABLE PVUSER DROP COLUMN ENABLE_SIGNAL_WORKFLOW;")
    }

    changeSet(author: "nitesh (generated)", id: "1608824568972-121") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_AUTO_ASSIGNED_TO')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_AUTO_ASSIGNED_TO", type: "number(1,0)", defaultValueBoolean: "false")
        }
        sql("UPDATE RCONFIG SET IS_AUTO_ASSIGNED_TO= 0;")
    }

    changeSet(author: "nitesh (generated)",id: "1608824568972-122") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AUTO_SHARE_WITH_USER_CONFIG')
            }
        }

        createTable(tableName: "AUTO_SHARE_WITH_USER_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTO_SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "AUTO_SHARE_WITH_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "nitesh (generated)",id: "1608824568972-123") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AUTO_SHARE_WITH_GROUP_CONFIG')
            }
        }

        createTable(tableName: "AUTO_SHARE_WITH_GROUP_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "AUTO_SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")
            column(name: "AUTO_SHARE_WITH_GROUP_IDX", type: "NUMBER(10, 0)")
        }
    }


    changeSet(author: "nitesh (generated)",id: "1608824568972-124") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_AUTO_ASSIGNED_TO')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_AUTO_ASSIGNED_TO", type:  "number(1,0)", defaultValueBoolean: "false")
        }
        sql("UPDATE EX_RCONFIG SET IS_AUTO_ASSIGNED_TO= 0;")
    }

    changeSet(author: "nitesh (generated)",id: "1608824568972-125") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AUTO_SHARE_WITH_USER_ECONFIG')
            }
        }

        createTable(tableName: "AUTO_SHARE_WITH_USER_ECONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTO_SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "AUTO_SHARE_WITH_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "nitesh (generated)",id: "1608824568972-126") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AUTO_SHARE_WITH_GROUP_ECONFIG')
            }
        }

        createTable(tableName: "AUTO_SHARE_WITH_GROUP_ECONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTO_SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "AUTO_SHARE_WITH_GROUP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608824568972-127") {

        grailsChange {
            change {
                try {
                    UserService userService = ctx.userService
                    userService.migrateAllUsersToPvUserWebappTable()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while saving all user's data. #############")
                }
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608824568972-128") {

        grailsChange {
            change {
                try {
                    GroupService groupService = ctx.groupService
                    groupService.migrateAllGroupsToGroupsWebappTable()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while saving all group's data. #############")
                }
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-129") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'alert_file_path')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "alert_file_path", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-130") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'executed_config_id')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "executed_config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-131") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'execution_level')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "execution_level", type: "number(10, 0)",defaultValueNumeric: "0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-132") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'report_execution_status')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "report_execution_status", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-133") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'spotfire_execution_status')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "spotfire_execution_status", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-134") {

        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKB83GYVA8NJ5CTOYKQKOV8ONGN')
        }
        dropForeignKeyConstraint(baseTableName: "EX_STATUSES_SHARED_WITHS", constraintName: "FKB83GYVA8NJ5CTOYKQKOV8ONGN")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-135") {

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_STATUSES_RPT_FORMATS')
        }
        dropTable(tableName: "EX_STATUSES_RPT_FORMATS")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-136") {

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_STATUSES_SHARED_WITHS')
        }
        dropTable(tableName: "EX_STATUSES_SHARED_WITHS")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-137") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_STATUS', columnName: 'QUERY_ID')
        }
        dropColumn(columnName: "QUERY_ID", tableName: "EX_STATUS")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-138") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_STATUS', columnName: 'SECTION_NAME')
        }
        dropColumn(columnName: "SECTION_NAME", tableName: "EX_STATUS")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-139") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_STATUS', columnName: 'TEMPLATE_ID')
        }
        dropColumn(columnName: "TEMPLATE_ID", tableName: "EX_STATUS")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-140") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'is_resume')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "is_resume", type: "number(1, 0)",defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-141") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_STATUS', columnName: 'MESSAGE')
        }
        dropColumn(columnName: "MESSAGE", tableName: "EX_STATUS")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-142") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'spotfire_file_name')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "spotfire_file_name", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-143") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_CONFIG', columnName: 'is_resume')
            }
        }
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "is_resume", type: "number(1, 0)",defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-144") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'is_resume')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "is_resume", type: "number(1, 0)",defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-145") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'time_stampjson')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "time_stampjson", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608824568972-146") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'user_pin_configuration')
            }
        }
        createTable(tableName: "user_pin_configuration") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "user_pin_configurationPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "field_code", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "is_pinned", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1608824568972-147") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FORMAT')
        }
        sql("alter table ARCHIVED_AGG_ALERT modify FORMAT VARCHAR2(8000 CHAR);")
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-148") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'global_case', columnName: 'version_num')
            }
        }
        addColumn(tableName: "global_case") {
            column(name: "version_num", type: "number(10, 0)")
        }
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-149") {

        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKRBK26CKL20Y61BNM3FLKO1MMK')
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FKRBK26CKL20Y61BNM3FLKO1MMK")
    }

    changeSet(author: "Nikhil (generated)", id: "1608824568972-150") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EVDAS_ALERT' AND column_name = 'LISTEDNESS' ;")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(1)", columnName: "listedness", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "Nikhil (generated)", id: "1608824568972-151") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns" +
                    " WHERE table_name = 'ARCHIVED_EVDAS_ALERT' AND column_name = 'LISTEDNESS' ;")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(1)", columnName: "listedness", tableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-152") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'user_dashboard_counts')
            }
        }
        createTable(tableName: "user_dashboard_counts") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_disp_case_counts", type: "varchar2(4000 CHAR)")
            column(name: "group_disp_case_counts", type: "varchar2(4000 CHAR)")
            column(name: "user_due_date_case_counts", type: "varchar2(8000 CHAR)")
            column(name: "group_due_date_case_counts", type: "varchar2(8000 CHAR)")
            column(name: "user_disppecounts", type: "varchar2(4000 CHAR)")
            column(name: "group_disppecounts", type: "varchar2(4000 CHAR)")
            column(name: "user_due_datepecounts", type: "varchar2(8000 CHAR)")
            column(name: "group_due_datepecounts", type: "varchar2(8000 CHAR)")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-153") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_user_dashboard_countsPK')
            }
        }
        createIndex(indexName: "IX_user_dashboard_countsPK", tableName: "user_dashboard_counts", unique: "true") {
            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "user_id", constraintName: "user_dashboard_countsPK", forIndexName: "IX_user_dashboard_countsPK", tableName: "user_dashboard_counts")
    }


    changeSet(author: "ankit (generated)", id: "1608824568972-154") {

        grailsChange {
            change {
                try {
                    if (UserDashboardCounts.count()) {
                        return
                    }
                    Sql sql = new Sql(ctx.getBean("dataSource"))
                    List<Map> userDispCaseCountList = []
                    Map<Long, Integer> userDispCaseCountsMap = [:]
                    List<Map> groupDispCaseCountList = []
                    Map<Long, Map> groupDispCaseCountsMap = [:]
                    List<Map> userDueDateCaseCountsList = []
                    Map<String, Integer> userDueDateCaseCountsMap = [:]
                    List<Map> dueDateGroupCaseCountList = []
                    Map<Long, Map> dueDateGroupCaseCountsMap = [:]

                    List<Map> userDispPECountList = []
                    Map<Long, Integer> userDispPECountsMap = [:]
                    List<Map> groupDispPECountList = []
                    Map<Long, Map> groupDispPECountsMap = [:]
                    List<Map> userDueDatePECountsList = []
                    Map<String, Integer> userDueDatePECountsMap = [:]
                    List<Map> dueDateGroupPECountList = []
                    Map<Long, Map> dueDateGroupPECountsMap = [:]


                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(true), []) { row ->
                        userDispCaseCountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(false), []) { row ->
                        groupDispCaseCountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(true), []) { row ->
                        userDueDateCaseCountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(false), []) { row ->
                        dueDateGroupCaseCountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(true), []) { row ->
                        userDispPECountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(false), []) { row ->
                        groupDispPECountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(true), []) { row ->
                        userDueDatePECountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(false), []) { row ->
                        dueDateGroupPECountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
                    }

                    User.list().each { user ->
                        Group workflowgroup = user.workflowGroup
                        List<Long> groupIdList = user.groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.id
                        groupIdList.each { id ->
                            Map dispCountMap = [:]
                            Map dueDateCountMap = [:]
                            Map dispPECountMap = [:]
                            Map dueDatePECountMap = [:]

                            groupDispCaseCountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dispCountMap.put(it.dispositionId, it.count)
                            }

                            if (dispCountMap) {
                                groupDispCaseCountsMap.put(id, dispCountMap)
                            }

                            dueDateGroupCaseCountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dueDateCountMap.put(it.due_date, it.count)
                            }

                            if (dueDateCountMap) {
                                dueDateGroupCaseCountsMap.put(id, dueDateCountMap)
                            }

                            groupDispPECountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dispPECountMap.put(it.dispositionId, it.count)
                            }

                            if (dispPECountMap) {
                                groupDispPECountsMap.put(id, dispPECountMap)
                            }

                            dueDateGroupPECountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowgroup.id }.each {
                                dueDatePECountMap.put(it.due_date, it.count)
                            }

                            if (dueDatePECountMap) {
                                dueDateGroupPECountsMap.put(id, dueDatePECountMap)
                            }
                        }

                        userDispCaseCountList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDispCaseCountsMap.put(it.dispositionId, it.count)
                        }

                        userDueDateCaseCountsList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDueDateCaseCountsMap.put(it.due_date, it.count)
                        }

                        userDispPECountList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDispPECountsMap.put(it.dispositionId, it.count)
                        }

                        userDueDatePECountsList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowgroup.id }.each {
                            userDueDatePECountsMap.put(it.due_date, it.count)
                        }

                        sql.execute("""INSERT INTO 
                                                  user_dashboard_counts (user_id, user_disp_case_counts, group_disp_case_counts, user_due_date_case_counts, group_due_date_case_counts, 
                                                  user_disppecounts, group_disppecounts, user_due_datepecounts,group_due_datepecounts)
                                                  VALUES (${user.id}, ${userDispCaseCountsMap ? new JsonBuilder(userDispCaseCountsMap).toPrettyString() : null},
                                                          ${groupDispCaseCountsMap ? new JsonBuilder(groupDispCaseCountsMap).toPrettyString() : null},
                                                          ${userDueDateCaseCountsMap ? new JsonBuilder(userDueDateCaseCountsMap).toPrettyString() : null},
                                                          ${dueDateGroupCaseCountsMap ? new JsonBuilder(dueDateGroupCaseCountsMap).toPrettyString() : null},
                                                          ${userDispPECountsMap ? new JsonBuilder(userDispPECountsMap).toPrettyString() : null},
                                                          ${groupDispPECountsMap ? new JsonBuilder(groupDispPECountsMap).toPrettyString() : null},
                                                          ${userDueDatePECountsMap ? new JsonBuilder(userDueDatePECountsMap).toPrettyString() : null},
                                                          ${dueDateGroupPECountsMap ? new JsonBuilder(dueDateGroupPECountsMap).toPrettyString() : null})
                                           """)
                        userDispCaseCountsMap.clear()
                        groupDispCaseCountsMap.clear()
                        userDueDateCaseCountsMap.clear()
                        dueDateGroupCaseCountsMap.clear()
                        userDispPECountsMap.clear()
                        groupDispPECountsMap.clear()
                        userDueDatePECountsMap.clear()
                        dueDateGroupPECountsMap.clear()
                    }
                }
                catch (Exception ex) {
                    println "##### Error Occurred while updating the UserDashboardCounts ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-155") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'disp_counts')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "disp_counts", type: "varchar2(4000 CHAR)") {
                constraints(nullable: "true")
            }
        }

        grailsChange {
            change {
                List<Map> execConfigDispCountList = []
                sql.eachRow("""
                     select exec_config_id,disposition_id,count(id) from single_case_alert 
                        where  exec_config_id in (select id from ex_rconfig)
                               and exec_config_id is not null
                        group by exec_config_id,disposition_id""") { row ->
                    execConfigDispCountList.add(execConfigId: row[0], dispositionId: row[1], count: row[2])
                }

                sql.eachRow("""
                     select exec_configuration_id,disposition_id,count(id) from agg_alert 
                        where  exec_configuration_id in (select id from ex_rconfig)
                               and exec_configuration_id is not null
                        group by exec_configuration_id,disposition_id""") { row ->
                    execConfigDispCountList.add(execConfigId: row[0], dispositionId: row[1], count: row[2])
                }

                Map execConfigDispCountMap = execConfigDispCountList.groupBy({
                    it.execConfigId
                }).collectEntries { key, val -> [(key): val.collectEntries { [it.dispositionId, it.count] }] }
                sql.withBatch(100, "UPDATE EX_RCONFIG SET disp_counts = :dispCounts WHERE ID = :id", { preparedStatement ->
                    execConfigDispCountMap.each { key, value ->
                        preparedStatement.addBatch(id: key, dispCounts: value ? new JsonBuilder(value).toPrettyString() : null)
                    }
                })
                confirm "Successfully Updated Disp_count values in EX_RCONFIG Table."
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-156") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_pvs_global_tag_global_id')
            }
        }
        createIndex(indexName: "IX_pvs_global_tag_global_id", tableName: "pvs_global_tag") {
            column(name: "global_id")
            column(name: "domain")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-157") {

        preConditions(onFail: 'MARK_RAN') {
            indexExists(indexName: 'IDX_SINGLE_ALERT_ISNEW')
        }
        dropIndex(indexName: "IDX_SINGLE_ALERT_ISNEW", tableName: "single_case_alert")
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-158") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'new_counts')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "new_counts", type: "number(10, 0)", defaultValue: "0") {
                constraints(nullable: "false")
            }
        }

        grailsChange {
            change {
                List<Map> execConfigNewCountList = []
                sql.eachRow("""
                     select exec_config_id,count(id) from single_case_alert 
                        where  exec_config_id in (select id from ex_rconfig) AND is_new = 1
                        group by exec_config_id""") { row ->
                    execConfigNewCountList.add(execConfigId: row[0], count: row[1])
                }

                sql.withBatch(100, "UPDATE EX_RCONFIG SET new_counts = :newCounts WHERE ID = :id", { preparedStatement ->
                    execConfigNewCountList.each {
                        preparedStatement.addBatch(id: it.execConfigId, newCounts: it.count)
                    }
                })
                confirm "Successfully Updated new_count values in EX_RCONFIG Table."
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-159") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'single_alert_id_idx')
            }
        }
        createIndex(indexName: "single_alert_id_idx", tableName: "CASE_HISTORY") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1608824568972-160") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'MASTER_PREF_TERM_ALL')
        }

        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update SINGLE_CASE_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }

    changeSet(author: "amrendra (generated)", id: "1608824568972-161") {

        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK5395n1fvj46kencfit7o01ycm")
        }
        dropForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK5395n1fvj46kencfit7o01ycm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1608824568972-162") {

        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK36oexbhattm8op3hnya3tcgoy")
        }
        dropForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FK36oexbhattm8op3hnya3tcgoy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "anshul (generated)", id: "1608824568972-181") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'VARCHAR2', "SELECT data_type FROM all_tab_columns where table_name = 'ARCHIVED_SINGLE_CASE_ALERT' AND COLUMN_NAME = 'MASTER_PREF_TERM_ALL';")
        }

        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update ARCHIVED_SINGLE_CASE_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }

    changeSet(author: "akshat (generated)", id: "1608824568973-163") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM GLOBAL_CASE where VERSION_NUM IS NULL;")
            }
        }
        grailsChange {
            change {
                try {
                    sql.execute("""
                    CREATE TABLE PVS_GLOBAL_TAG_TEMP
                        ("ID" NUMBER(19,0),
                         "VERSION" NUMBER(19,0),
                         "CREATED_AT" TIMESTAMP (6),
                         "CREATED_BY" VARCHAR2(255 CHAR), 
                         "DOMAIN" VARCHAR2(255 CHAR), 
                         "GLOBAL_ID" NUMBER(19,0), 
                         "IS_MASTER_CATEGORY" NUMBER(1,0), 
                         "MART_ID" NUMBER(19,0), 
                         "MODIFIED_AT" TIMESTAMP (6), 
                         "MODIFIED_BY" VARCHAR2(255 CHAR), 
                         "PRIORITY" NUMBER(10,0), 
                         "PRIVATE_USER" VARCHAR2(255 CHAR), 
                         "SUB_TAG_ID" NUMBER(19,0), 
                         "SUB_TAG_TEXT" VARCHAR2(255 CHAR), 
                         "TAG_ID" NUMBER(19,0), 
                         "TAG_TEXT" VARCHAR2(255 CHAR), 
                         "AUTO_TAGGED" NUMBER(1,0), 
                         "EXEC_CONFIG_ID" NUMBER(19,0), 
                         "IS_RETAINED" NUMBER(1,0))
                                    """)

                    sql.execute(""" insert into PVS_GLOBAL_TAG_TEMP  
                                            select * from PVS_GLOBAL_TAG where DOMAIN <> 'Single Case Alert' """)
                    sql.execute("TRUNCATE TABLE PVS_GLOBAL_TAG")
                    sql.execute(""" insert into PVS_GLOBAL_TAG   
                                            select * from PVS_GLOBAL_TAG_TEMP """)
                    sql.execute("Drop TABLE PVS_GLOBAL_TAG_TEMP")
                    sql.execute("TRUNCATE TABLE SINGLE_GLOBAL_TAGS")
                    sql.execute('''INSERT INTO GLOBAL_CASE(globalcaseid,VERSION , CASE_ID, VERSION_NUM)
                                        select hibernate_sequence.nextval ,VERSION,case_id,case_Version from (
                                        (SELECT 0 as VERSION, CASE_ID, CASE_VERSION FROM SINGLE_CASE_ALERT UNION SELECT 0 , CASE_ID, CASE_VERSION FROM ARCHIVED_SINGLE_CASE_ALERT ))  ''')
                    sql.execute('''MERGE INTO SINGLE_CASE_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        GLOBAL_CASE
                                )
                                b ON ( b.CASE_ID = a.CASE_ID AND b.VERSION_NUM = a.CASE_VERSION)
                                WHEN MATCHED THEN UPDATE SET a.GLOBAL_IDENTITY_ID = GLOBALCASEID   ''')
                    sql.execute('''MERGE INTO ARCHIVED_SINGLE_CASE_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        GLOBAL_CASE
                                )
                                b ON ( b.CASE_ID = a.CASE_ID AND b.VERSION_NUM = a.CASE_VERSION)
                                WHEN MATCHED THEN UPDATE SET a.GLOBAL_IDENTITY_ID = GLOBALCASEID   ''')
                    sql.execute('''ALTER TABLE SINGLE_CASE_ALERT DISABLE CONstraint FK8ic0iqi8eynbxkkwxroc6io1r ''')
                    sql.execute('''ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT DISABLE CONstraint FKqaxchl4ofg9634tb244aw63e5 ''')
                    sql.execute('''ALTER TABLE SINGLE_GLOBAL_TAGS DISABLE CONSTRAINT FK7xpvynkdusubvmx9lc02em2i3 ''')
                    sql.execute("""
                                  CREATE TABLE GLOBAL_CASE_TEMP 
                                              ("GLOBALCASEID" NUMBER(19,0), 
                                              "VERSION" NUMBER(19,0), 
                                              "CASE_ID" NUMBER(19,0), 
                                             "VERSION_NUM" NUMBER(10,0)) 
                    """)

                    sql.execute(""" insert into GLOBAL_CASE_TEMP  
                                            select * from GLOBAL_CASE where VERSION_NUM IS NOT NULL """)
                    sql.execute("TRUNCATE TABLE GLOBAL_CASE")
                    sql.execute(""" insert into GLOBAL_CASE   
                                            select * from GLOBAL_CASE_TEMP """)
                    sql.execute("Drop TABLE GLOBAL_CASE_TEMP")
                    sql.execute('''ALTER TABLE SINGLE_CASE_ALERT ENABLE CONstraint FK8ic0iqi8eynbxkkwxroc6io1r ''')
                    sql.execute('''ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT ENABLE CONstraint FKqaxchl4ofg9634tb244aw63e5 ''')
                    sql.execute('''ALTER TABLE SINGLE_GLOBAL_TAGS ENABLE CONSTRAINT FK7xpvynkdusubvmx9lc02em2i3 ''')

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating GLOBAL_CASE table. #############")
                }

            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568973-164") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_global_case_id_version')
            }
        }
        createIndex(indexName: "IX_global_case_id_version", tableName: "GLOBAL_CASE") {
            column(name: "CASE_ID")
            column(name: "VERSION_NUM")
        }
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-164") {

        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.migrateGlobalCase()
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating Global Case table. #############")
                }
            }
        }
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-165") {

        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.importSingleGlobalTags()
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating PvsGlobalTag table For SingleCaseAlert. #############")
                }
            }
        }
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-166") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM PVS_ALERT_TAG WHERE DOMAIN = 'Single Case Alert';")
            }
        }
        grailsChange {
            change {
                try {
                    sql.execute("DELETE FROM SINGLE_CASE_ALERT_TAGS")
                    sql.execute("DELETE FROM ARCHIVED_SCA_TAGS")
                    sql.execute("DELETE FROM PVS_ALERT_TAG WHERE DOMAIN = 'Single Case Alert'")

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PVS_ALERT_TAG table. #############")
                }

            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1608824568972-167") {

        grailsChange {
            change {
                try {
                    PvsAlertTagService pvsAlertTagService = ctx.pvsAlertTagService
                    pvsAlertTagService.importSingleAlertTags(false)
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating SingleAlertTags table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1608824568972-168") {

        grailsChange {
            change {
                try {
                    PvsAlertTagService pvsAlertTagService = ctx.pvsAlertTagService
                    pvsAlertTagService.importSingleAlertTags(true)
                } catch (Exception ex) {
                    println(ex.printStackTrace())
                    println("##################### Error occurred while updating Archived SingleAlertTags table. #############")
                }
            }
        }
    }

    changeSet(author: "akshat (generated)", id: "1608824568972-169") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'DISPOSITION_JUSTIFICATIONS')
            }
        }
        createTable(tableName: "DISPOSITION_JUSTIFICATIONS") {
            column(name: "JUSTIFICTION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")

            column(name: "dispositions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "rishabhgupta(generated)", id: "1608824568972-170") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'disp_counts')
            }
        }

        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "disp_counts", type: "varchar2(4000 CHAR)") {
                constraints(nullable: "true")
            }
        }

        grailsChange {
            change {
                List<Map> execConfigDispCountList = []
                sql.eachRow("""
                     select exec_configuration_id,disposition_id,count(id) from evdas_alert 
                        where  exec_configuration_id in (select id from ex_evdas_config)
                               and exec_configuration_id is not null
                        group by exec_configuration_id,disposition_id""") { row ->
                    execConfigDispCountList.add(execConfigId: row[0], dispositionId: row[1], count: row[2])
                }

                Map execConfigDispCountMap = execConfigDispCountList.groupBy({
                    it.execConfigId
                }).collectEntries { key, val -> [(key): val.collectEntries { [it.dispositionId, it.count] }] }
                sql.withBatch(100, "UPDATE EX_EVDAS_CONFIG SET disp_counts = :dispCounts WHERE ID = :id", { preparedStatement ->
                    execConfigDispCountMap.each { key, value ->
                        preparedStatement.addBatch(id: key, dispCounts: value ? new JsonBuilder(value).toPrettyString() : null)
                    }
                })
                confirm "Successfully Updated Disp_count values in EX_EVDAS_CONFIG Table."
            }
        }
    }

    changeSet(author: "rishabhgupta(generated)", id: "1608824568972-171") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PREFERENCE', columnName: 'DASHBOARD_CONFIG_JSON')
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' UPDATE PREFERENCE SET DASHBOARD_CONFIG_JSON = null ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while mirating dashboard config. #############")
                }
            }
        }

    }

    changeSet(author: "ankit (generated)", id: "1608824568972-172") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'PRODUCT_ASSIGNMENT_LOG')
            }
        }
        createTable(tableName: "PRODUCT_ASSIGNMENT_LOG") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRODUCT_ASSIGNMENT_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "imported_file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "imported_by_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "imported_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-173") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PRODUCT_ASSIGNMENT_LOG', columnName: 'status')
            }
        }
        addColumn(tableName: "PRODUCT_ASSIGNMENT_LOG") {
            column(name: "status", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1608824568972-176") {
        sql("update VALIDATED_SIGNAL set REPORT_PREFERENCE = null where REPORT_PREFERENCE is not null;")
    }

    changeSet(author: "shivamvashist(generated)", id: "1608824568972-177") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ROLE', columnName: 'DESCRIPTION')
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' UPDATE ROLE SET DESCRIPTION = 'Create, read, update, and delete Individual Case Alerts' WHERE authority ='ROLE_SINGLE_CASE_CRUD' ''')
                    sql.execute(''' UPDATE ROLE SET DESCRIPTION = 'Create, read, update, and delete Aggregate Alerts' WHERE authority ='ROLE_AGGREGATE_CASE_CRUD' ''')
                    sql.execute(''' UPDATE ROLE SET DESCRIPTION = 'Role to Access Alerts Operational Metrics' WHERE authority ='ROLE_PRODUCTIVITY_AND_COMPLIANCE' ''')
                } catch (Exception ex) {
                    println(ex)
                }
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608824568972-178") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VIEW_INSTANCE', columnName: 'TEMP_COLUMN_SEQ')
        }
        grailsChange {
            change {
                try {
                    List<ViewInstance> viewInstanceList = ViewInstance.findAllByAlertTypeAndTempColumnSeqIsNotNull("Signal Management")
                    viewInstanceList.each{viewInstance ->
                        Map viewInstanceCollection = JSON.parse(viewInstance.tempColumnSeq)
                        viewInstanceCollection.each{key , value->
                            if(value.name == 'monitoringStatus') {
                                value.label = "Disposition"
                            }
                        }
                        viewInstance.tempColumnSeq = JsonOutput.toJson(viewInstanceCollection)
                        viewInstance.save(flush:true)
                    }

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error updating temp_column_seq #############")
                }
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608824568972-179") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM GLOBAL_ARTICLE WHERE ARTICLE_ID IS NULL;")
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' INSERT INTO GLOBAL_ARTICLE(VERSION , ARTICLE_ID)
                        (SELECT  DISTINCT 0 , ARTICLE_ID FROM LITERATURE_ALERT la  where not exists(select * from global_article where ARTICLE_ID=la.article_id)) ''')
                    sql.execute(''' MERGE INTO LITERATURE_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        GLOBAL_ARTICLE
                                )
                                b ON ( b.ARTICLE_ID = a.ARTICLE_ID )
                                WHEN MATCHED THEN UPDATE SET a.GLOBAL_IDENTITY_ID = GLOBALARTICLEID ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating GLOBAL_ARTICLE table. #############")
                }
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "1608824568972-180") {


        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'VARCHAR2', "SELECT data_type FROM all_tab_columns where table_name = 'SINGLE_CASE_ALERT' AND COLUMN_NAME = 'MASTER_PREF_TERM_ALL';")
        }

        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update SINGLE_CASE_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }



    changeSet(author: "anshul (generated)", id: "1608824568972-182") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'VARCHAR2', "SELECT data_type FROM all_tab_columns where table_name = 'SINGLE_ON_DEMAND_ALERT' AND COLUMN_NAME = 'MASTER_PREF_TERM_ALL';")
        }

        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update SINGLE_ON_DEMAND_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "SINGLE_ON_DEMAND_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "SINGLE_ON_DEMAND_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }

    changeSet(author: "ankit (generated)", id: "1608824568972-183") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_LITERATURE_EXEC_CONFIG_ID')
            }
        }
        createIndex(indexName: "IX_LITERATURE_EXEC_CONFIG_ID", tableName: "LITERATURE_ALERT") {
            column(name: "EX_LIT_SEARCH_CONFIG_ID")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-184") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_single_alert_exconfig", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "EXEC_CONFIG_ID")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608824568972-185") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_agg_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_agg_alert_exconfig", tableName: "AGG_ALERT", unique: "false") {
            column(name: "EXEC_CONFIGURATION_ID")
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608824568972-186") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(1) from ex_rconfig where is_case_series = 1 and pvr_case_series_id is null;")
            }
        }

        grailsChange {
            change {
                try {
                    ExecutedConfiguration.findAllByIsCaseSeries(true).each{
                        Long seriesId = ctx.getBean("singleCaseAlertService").generateExecutedCaseSeries(it, false)
                        if (seriesId) {
                            String updateCaseSeriesHql = ctx.getBean("alertService").prepareUpdateCaseSeriesHql(it?.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE)
                            ExecutedConfiguration.executeUpdate(updateCaseSeriesHql, [pvrCaseSeriesId: seriesId, id: it?.id])

                        }
                    }
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating EX_rconfig. #############")
                }
            }
        }
    }

}