import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.PriorityDispositionConfig
import org.joda.time.DateTime
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.util.Holders
import com.rxlogix.Constants


databaseChangeLog = {
    changeSet(author: "Nikhil (generated)", id: "1617101827367-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_TEMPLATE_ALERT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_TEMPLATE_ALERT", type: "BOOLEAN"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Nikhil (generated)", id: "1617101827367-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'CONFIGURATION_TEMPLATE_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "CONFIGURATION_TEMPLATE_ID", type: "NUMBER(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Nikhil (generated)", id: "1617101827367-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'IMPORT_CONFIGURATION_LOG')
            }
        }
        createTable(tableName: "IMPORT_CONFIGURATION_LOG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "IMPORT_CONFIGURATION_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "import_file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
            column(name: "import_log_file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }

            column(name: "imported_by_id", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "imported_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }
            column(name: "status", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "Nikhil (generated)", id: "1617101827367-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'IMPORT_CONFIGURATION_LOG', columnName: 'import_conf_type')
            }
        }
        addColumn(tableName: "IMPORT_CONFIGURATION_LOG") {
            column(name: "import_conf_type", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1617101827367-5") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'INDICATION')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "INDICATION", type: "varchar2(16000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-6") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'EVENT_OUTCOME')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "EVENT_OUTCOME", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-7") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'CAUSE_OF_DEATH')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "CAUSE_OF_DEATH", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-8") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'SERIOUS_UNLISTED_RELATED')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "SERIOUS_UNLISTED_RELATED", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-9") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PATIENT_MED_HIST')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "PATIENT_MED_HIST", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-10") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PATIENT_HIST_DRUGS')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "PATIENT_HIST_DRUGS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-11") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'BATCH_LOT_NO')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "BATCH_LOT_NO", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-12") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'TIME_TO_ONSET')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "TIME_TO_ONSET", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-13") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'CASE_CLASSIFICATION')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "CASE_CLASSIFICATION", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-14") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'INITIAL_FU')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "INITIAL_FU", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-15") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PROTOCOL_NO')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "PROTOCOL_NO", type: "varchar2(500 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-16") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'IS_SUSAR')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "IS_SUSAR", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-17") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'THERAPY_DATES')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "THERAPY_DATES", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-18") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'DOSE_DETAILS')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "DOSE_DETAILS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-19") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'PRE_ANDA')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "PRE_ANDA", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1617101827367-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_INDICATION_LIST')
            }
        }
        createTable(tableName: "SINGLE_ALERT_INDICATION_LIST") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_INDICATION", type: "varchar(500)")

            column(name: "indication_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_CAUSE_OF_DEATH')
            }
        }
        createTable(tableName: "SINGLE_ALERT_CAUSE_OF_DEATH") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_CAUSE_OF_DEATH", type: "varchar(1500)")

            column(name: "cause_of_death_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_PAT_MED_HIST')
            }
        }
        createTable(tableName: "SINGLE_ALERT_PAT_MED_HIST") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PAT_MED_HIST", type: "varchar(500)")

            column(name: "patient_med_hist_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_PAT_HIST_DRUGS')
            }
        }
        createTable(tableName: "SINGLE_ALERT_PAT_HIST_DRUGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PAT_HIST_DRUGS", type: "varchar(500)")

            column(name: "patient_hist_drugs_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_BATCH_LOT_NO')
            }
        }
        createTable(tableName: "SINGLE_ALERT_BATCH_LOT_NO") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_BATCH_LOT_NO", type: "varchar(500)")

            column(name: "batch_lot_no_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_CASE_CLASSIFI')
            }
        }
        createTable(tableName: "SINGLE_ALERT_CASE_CLASSIFI") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_CASE_CLASSIFICATION", type: "varchar(3000)")

            column(name: "case_classification_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-26") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_THERAPY_DATES')
            }
        }
        createTable(tableName: "SINGLE_ALERT_THERAPY_DATES") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_THERAPY_DATES", type: "varchar(500)")

            column(name: "therapy_dates_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1617101827367-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_DOSE_DETAILS')
            }
        }
        createTable(tableName: "SINGLE_ALERT_DOSE_DETAILS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_DOSE_DETAILS", type: "varchar(15000)")

            column(name: "dose_details_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1617101827367-28") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'INDICATION')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "INDICATION", type: "varchar2(16000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-29") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'EVENT_OUTCOME')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "EVENT_OUTCOME", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-30") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'CAUSE_OF_DEATH')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "CAUSE_OF_DEATH", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-31") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'SERIOUS_UNLISTED_RELATED')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "SERIOUS_UNLISTED_RELATED", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-32") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PATIENT_MED_HIST')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "PATIENT_MED_HIST", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-33") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PATIENT_HIST_DRUGS')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "PATIENT_HIST_DRUGS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-34") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'BATCH_LOT_NO')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "BATCH_LOT_NO", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-35") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'TIME_TO_ONSET')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "TIME_TO_ONSET", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-36") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'CASE_CLASSIFICATION')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "CASE_CLASSIFICATION", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-37") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'INITIAL_FU')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "INITIAL_FU", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-38") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PROTOCOL_NO')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "PROTOCOL_NO", type: "varchar2(500 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-39") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'IS_SUSAR')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "IS_SUSAR", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-40") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'THERAPY_DATES')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "THERAPY_DATES", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-41") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'DOSE_DETAILS')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "DOSE_DETAILS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-42") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'PRE_ANDA')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "PRE_ANDA", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1617101827367-43") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_INDICATION_LIST')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_INDICATION_LIST") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_INDICATION", type: "varchar(500)")

            column(name: "indication_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-44") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_CAUSE_OF_DEATH')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_CAUSE_OF_DEATH") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_CAUSE_OF_DEATH", type: "varchar(1500)")

            column(name: "cause_of_death_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-45") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_PAT_MED_HIST')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_PAT_MED_HIST") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PAT_MED_HIST", type: "varchar(500)")

            column(name: "patient_med_hist_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-46") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_PAT_HIST_DRUGS')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_PAT_HIST_DRUGS") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PAT_HIST_DRUGS", type: "varchar(500)")

            column(name: "patient_hist_drugs_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_BATCH_LOT_NO')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_BATCH_LOT_NO") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_BATCH_LOT_NO", type: "varchar(500)")

            column(name: "batch_lot_no_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-48") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_CASE_CLASSIFI')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_CASE_CLASSIFI") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_CASE_CLASSIFICATION", type: "varchar(3000)")

            column(name: "case_classification_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-49") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_THERAPY_DATES')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_THERAPY_DATES") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_THERAPY_DATES", type: "varchar(500)")

            column(name: "therapy_dates_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1617101827367-50") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_DOSE_DETAILS')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_DOSE_DETAILS") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_DOSE_DETAILS", type: "varchar(15000)")

            column(name: "dose_details_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-51") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'combo_flag')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "combo_flag", type: "varchar2(255 CHAR)", defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-52") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'malfunction')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "malfunction", type: "varchar2(255 CHAR)", defaultValue :"No") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-53") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'new_ev_link')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "new_ev_link", type: "varchar2(600 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-54") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'total_ev_link')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "total_ev_link", type: "varchar2(600 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-55") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'dme_ime')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "dme_ime", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-56") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'attributes')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "attributes", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-57") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'ratio_ror_paed_vs_others')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "ratio_ror_paed_vs_others", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-58") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'ratio_ror_geriatr_vs_others')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "ratio_ror_geriatr_vs_others", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-59") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'changes')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "changes", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-60") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'sdr_paed')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "sdr_paed", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-61") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'sdr_geratr')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "sdr_geratr", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-62") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_europe')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "tot_spont_europe", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-63") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spontnamerica')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "tot_spontnamerica", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-64") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_japan')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "tot_spont_japan", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-65") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_asia')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "tot_spont_asia", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-66") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_rest')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "tot_spont_rest", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-67") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'format')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "format", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-68") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'frequency')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "frequency", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-69") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'flags')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "flags", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-70") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'listedness')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "listedness", type: "number(1, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EVDAS_ON_DEMAND_ALERT set listedness = 0;")
        addNotNullConstraint(tableName: "EVDAS_ON_DEMAND_ALERT", columnName: "listedness")
    }


    changeSet(author: "rxlogix (generated)", id: "1617101827367-71") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'imp_events')
            }
        }
        addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "imp_events", type: "VARCHAR(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxlogix (generated)", id: "1617101827367-72") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'EOD_ALERT_IMP_EVENT_LIST')
            }
        }
        createTable(tableName: "EOD_ALERT_IMP_EVENT_LIST") {
            column(name: "EVDAS_ON_DEMAND_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_ON_DEMAND_IMP_EVENTS", type: "VARCHAR(255 CHAR)")

            column(name: "EV_IMP_EVENT_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-73") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_mh')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "prr_mh", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-74") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_str05')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "prr_str05", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-75") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_str95')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "prr_str95", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-76") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_mh')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_mh", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-77") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_str05')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_str05", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-78") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_str95')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_str95", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-79") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_str')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "prr_str", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-80") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'pec_imp_high')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "pec_imp_high", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-81") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'pec_imp_low')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "pec_imp_low", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-82") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'format')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "format", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-83") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_str')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_str", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-84") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cumm_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "cumm_count", type: "number(10, 0)"){
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-85") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cumm_interacting_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "cumm_interacting_count", type: "number(10, 0)"){
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-86") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cumm_pediatric_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "cumm_pediatric_count", type: "number(10, 0)"){
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-87") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "new_count", type: "number(10, 0)"){
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-88") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_interacting_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "new_interacting_count", type: "number(10, 0)"){
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-89") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_pediatric_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "new_pediatric_count", type: "number(10, 0)"){
            }
        }
    }

    changeSet(author: "amit (generated)", id: "1617101827367-90") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cum_geriatric_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "cum_geriatric_count", type: "number(10, 0)") {
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-91") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cum_non_serious')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "cum_non_serious", type: "number(10, 0)") {
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-92") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_geriatric_count')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "new_geriatric_count", type: "number(10, 0)") {
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-93") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_non_serious')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "new_non_serious", type: "number(10, 0)") {
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-94") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb05str')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb05str", type: "clob"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-95") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb95str')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb95str", type: "clob"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-96") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ebgm_str')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ebgm_str", type: "clob"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-97") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'imp_events')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "imp_events", type: "VARCHAR(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-98") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AOD_ALERT_IMP_EVENT_LIST')
            }
        }
        createTable(tableName: "AOD_ALERT_IMP_EVENT_LIST") {
            column(name: "AGG_ON_DEMAND_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGA_ON_DEMAND_IMP_EVENTS", type: "VARCHAR(255 CHAR)")

            column(name: "AGA_IMP_EVENT_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-99") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'global_identity_id')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "global_identity_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-100") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'global_identity_id')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "global_identity_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-101") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_OD_PT')
            }
        }
        createTable(tableName: "SINGLE_ALERT_OD_PT") {
            column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PT", type: "CLOB")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-102") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_OD_SUSP_PROD')
            }
        }
        createTable(tableName: "SINGLE_ALERT_OD_SUSP_PROD") {
            column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_NAME", type: "CLOB")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-103") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_OD_CON_COMIT')
            }
        }
        createTable(tableName: "SINGLE_ALERT_OD_CON_COMIT") {
            column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "CON_COMIT", type: "CLOB")
            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1617101827367-104") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_OD_MED_ERR')
            }
        }
        createTable(tableName: "SINGLE_ALERT_OD_MED_ERR") {
            column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
            column(name: "MED_ERROR", type: "CLOB")

            column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1617101827367-105") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'INDICATION')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "INDICATION", type: "varchar2(16000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-106") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'EVENT_OUTCOME')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "EVENT_OUTCOME", type: "varchar2(2000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-107") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'CAUSE_OF_DEATH')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "CAUSE_OF_DEATH", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-108") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'SERIOUS_UNLISTED_RELATED')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "SERIOUS_UNLISTED_RELATED", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-109") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PATIENT_MED_HIST')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "PATIENT_MED_HIST", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1617101827367-110") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PATIENT_HIST_DRUGS')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "PATIENT_HIST_DRUGS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-111") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'BATCH_LOT_NO')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "BATCH_LOT_NO", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-112") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'TIME_TO_ONSET')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "TIME_TO_ONSET", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-113") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'CASE_CLASSIFICATION')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "CASE_CLASSIFICATION", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-114") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'INITIAL_FU')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "INITIAL_FU", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-115") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PROTOCOL_NO')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "PROTOCOL_NO", type: "varchar2(500 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "111617101827367-116") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'IS_SUSAR')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "IS_SUSAR", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-117") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'THERAPY_DATES')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "THERAPY_DATES", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-118") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'DOSE_DETAILS')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "DOSE_DETAILS", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "ujjwal (generated)", id: "11617101827367-119") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PRE_ANDA')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "PRE_ANDA", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-120") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_INDICATION_LIST')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_INDICATION_LIST") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_INDICATION", type: "varchar(500)")

            column(name: "indication_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "ujjwal (generated)", id: "11617101827367-121") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_CAUSE_OF_DEATH')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_CAUSE_OF_DEATH") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_CAUSE_OF_DEATH", type: "varchar(1500)")

            column(name: "cause_of_death_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "ujjwal (generated)", id: "11617101827367-122") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_PAT_MED_HIST')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_PAT_MED_HIST") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PAT_MED_HIST", type: "varchar(500)")

            column(name: "patient_med_hist_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "ujjwal (generated)", id: "11617101827367-123") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_PAT_HIST_DRUGS')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_PAT_HIST_DRUGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PAT_HIST_DRUGS", type: "varchar(500)")

            column(name: "patient_hist_drugs_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "ujjwal (generated)", id: "11617101827367-124") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_BATCH_LOT_NO')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_BATCH_LOT_NO") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_BATCH_LOT_NO", type: "varchar(500)")

            column(name: "batch_lot_no_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "ujjwal (generated)", id: "11617101827367-125") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_CASE_CLASSIFI')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_CASE_CLASSIFI") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_CASE_CLASSIFICATION", type: "varchar(3000)")

            column(name: "case_classification_list_idx", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "ujjwal (generated)", id: "11617101827367-126") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_THERAPY_DATES')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_THERAPY_DATES") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_THERAPY_DATES", type: "varchar(500)")

            column(name: "therapy_dates_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-127") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_DOSE_DETAILS')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_DOSE_DETAILS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_DOSE_DETAILS", type: "varchar(15000)")

            column(name: "dose_details_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-128") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_ALERT_TAGS')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_ALERT_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-129") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AGG_DEMAND_ALERT_TAGS')
            }
        }
        createTable(tableName: "AGG_DEMAND_ALERT_TAGS") {
            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "nitesh (generated)", id: "11617101827367-175") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CASE_SERIES_SPOTFIRE_FILE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CASE_SERIES_SPOTFIRE_FILE", type: "varchar(2000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "11617101827367-171") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_CASE_SERIES_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "11617101827367-184") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_CASE_SERIES_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALERT_CASE_SERIES_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "11617101827367-173") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_CASE_SERIES_NAME')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_CASE_SERIES_NAME", type: "varchar(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "11617101827367-174") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_CASE_SERIES_NAME')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALERT_CASE_SERIES_NAME", type: "varchar(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-130") {
        sql("update AGG_ON_DEMAND_ALERT set new_count = new_spon_count where new_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-131") {
        sql("update AGG_ON_DEMAND_ALERT set cumm_count = cum_spon_count where cumm_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-132") {
        sql("update AGG_ON_DEMAND_ALERT set new_pediatric_count = 0 where new_pediatric_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-133") {
        sql("update AGG_ON_DEMAND_ALERT set cumm_pediatric_count = 0 where cumm_pediatric_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-134") {
        sql("update AGG_ON_DEMAND_ALERT set new_interacting_count = 0 where new_interacting_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-135") {
        sql("update AGG_ON_DEMAND_ALERT set cumm_interacting_count = 0 where cumm_interacting_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-136") {
        sql("update AGG_ON_DEMAND_ALERT set new_geriatric_count = 0 where new_geriatric_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-137") {
        sql("update AGG_ON_DEMAND_ALERT set cum_geriatric_count = 0 where cum_geriatric_count is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-138") {
        sql("update AGG_ON_DEMAND_ALERT set new_non_serious = 0 where new_non_serious is null;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-139") {
        sql("update AGG_ON_DEMAND_ALERT set cum_non_serious = 0 where cum_non_serious is null;")
    }

    changeSet(author: "rishabh (generated)", id: "11617101827367-140") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'clipboard_cases')
            }
        }
        createTable(tableName: "clipboard_cases") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "clipboard_casesPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "case_ids", type: "clob") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_temp_view", type: "number(1, 0)") {
                constraints(nullable: "true")
            }

            column(name: "is_deleted", type: "number(1, 0)"){
                constraints(nullable: "true")
            }

            column(name: "is_first_use", type: "number(1, 0)"){
                constraints(nullable: "true")
            }

            column(name: "is_updated", type: "number(1, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ujjwal", id: "11617101827367-141") {

        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'VARCHAR2', "SELECT data_type FROM all_tab_columns where table_name = 'PARAM' AND COLUMN_NAME = 'VALUE';")
        }
        addColumn(tableName: "PARAM") {
            column(name: "VALUE_CLOB", type: "clob")
        }

        sql("update PARAM set VALUE_CLOB = VALUE;")
        sql("alter table PARAM drop column VALUE;")
        sql("alter table PARAM rename column VALUE_CLOB to VALUE;")
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-142") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'removed_users')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "removed_users", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-143") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'removed_users')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "removed_users", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-144") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'removed_users')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "removed_users", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-145") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'requires_review_count')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(defaultValue: "0", name: "requires_review_count", type: "varchar2(255 CHAR)")
        }

    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-146") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'requires_review_count')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(defaultValue: "0", name: "requires_review_count", type: "varchar2(255 CHAR)")
        }

    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-147") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'product_name')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "product_name", type: "clob")
        }

    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-148") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'product_name')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "product_name", type: "varchar2(1000 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-149") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'review_due_date')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "review_due_date", type: "TIMESTAMP")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-150") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'requires_review_count')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(defaultValue: "0", name: "requires_review_count", type: "varchar2(255 CHAR)")
        }

    }

    changeSet(author: "ujjwal (generated)", id: "11617101827367-151") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'product_name')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "product_name", type: "clob")
        }


    }


    changeSet(author: "amrendra (generated)", id: "11617101827367-153") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'case_id')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "case_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "11617101827367-154") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'data_source')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "data_source", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "11617101827367-155") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'follow_up_num')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "follow_up_num", type: "number(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "11617101827367-156") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'sync_flag')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "sync_flag", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "11617101827367-157") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'version_num')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "version_num", type: "number(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "11617101827367-158") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'ex_config_id')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "ex_config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "11617101827367-159") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_COMMENT', columnName: 'alert_name')
            }
        }
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "alert_name", type: "varchar2(200 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "11617101827367-160") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT', columnName: 'COMMENTS')
        }

        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "COMMENTS_COPY", type: "CLOB")
        }

        sql("update ALERT_COMMENT set COMMENTS_COPY = COMMENTS;")

        dropColumn(tableName: "ALERT_COMMENT", columnName: "COMMENTS")

        renameColumn(tableName: "ALERT_COMMENT", oldColumnName: "COMMENTS_COPY", newColumnName: "COMMENTS")

    }


    changeSet(author: "rxlogix (generated)", id: "11617101827367-162") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'case_form')
            }
        }
        createTable(tableName: "case_form") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_ids", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "case_numbers", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "executed_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_num", type: "CLOB")

            column(name: "form_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_duplicate", type: "CLOB")

            column(name: "version_num", type: "CLOB")

            column(name: "advanced_filter_name", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "true")
            }

            column(name: "view_instance_name", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "true")
            }

            column(name: "is_full_case_series", type: "number(1, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rxlogix (generated)", id: "11617101827367-163") {
        preConditions(onFail: 'MARK_RAN') {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM user_sequences WHERE sequence_name = UPPER('case_form_sequence')")
        }
        createSequence(sequenceName: "case_form_sequence")
    }

    changeSet(author: "rxlogix (generated)", id: "11617101827367-164") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'case_form', columnName: 'saved_name')
            }
        }
        addColumn(tableName: "case_form") {
            column(name: "saved_name", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "11617101827367-165") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = UPPER('case_form') AND column_name = UPPER('case_ids') ;")
        }
        sql("alter table case_form modify (case_ids NULL);")
    }

    changeSet(author: "nitesh (generated)", id: "11617101827367-166") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns WHERE table_name = UPPER('case_form') AND column_name = UPPER('case_numbers') ;")
        }
        sql("alter table case_form modify (case_numbers NULL);")
    }


    changeSet(author: "anshul (generated)", id: "11617101827367-176") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK8fru5jqrjb81756xvmoh203uw")
        }
        dropForeignKeyConstraint(baseTableName: "AGG_GLOBAL_TAGS", constraintName: "FK8fru5jqrjb81756xvmoh203uw")
    }

    changeSet(author: "anshul (generated)", id: "11617101827367-177") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK7xpvynkdusubvmx9lc02em2i3")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FK7xpvynkdusubvmx9lc02em2i3")
    }

    changeSet(author: "anshul (generated)", id: "11617101827367-178") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKrbk26ckl20y61bnm3flko1mmk")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FKrbk26ckl20y61bnm3flko1mmk")
    }

    changeSet(author: "anshul (generated)", id: "11617101827367-179") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERT_DOCUMENT', columnName: 'link_text')
            }
        }

        addColumn(tableName: "ALERT_DOCUMENT") {
            column(name: "link_text", type: "varchar2(4000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anshul (generated)", id: "11617101827367-180") {
        preConditions(onFail: 'MARK_RAN') {
            notNullConstraintExists(columnName: "date_range_information_id", tableName: "EVDAS_CONFIG")
        }
        dropNotNullConstraint(columnDataType: "NUMBER(19, 0)", columnName: "date_range_information_id", tableName: "EVDAS_CONFIG")
    }

    changeSet(author: "sandeep (generated)", id: "11617101827367-190") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_HISTORY', columnName: 'REPORT_NAME')
        }

        addColumn(tableName: "REPORT_HISTORY") {
            column(name: "REPORT_NAME_COPY", type: "varchar2(8000 CHAR)")
        }

        sql("update REPORT_HISTORY set REPORT_NAME_COPY = REPORT_NAME;")

        dropColumn(tableName: "REPORT_HISTORY", columnName: "REPORT_NAME")

        renameColumn(tableName: "REPORT_HISTORY", oldColumnName: "REPORT_NAME_COPY", newColumnName: "REPORT_NAME")

        addNotNullConstraint(tableName: "REPORT_HISTORY", columnName: "REPORT_NAME")
    }

    changeSet(author: "sandeep (generated)", id: "11617101827367-191") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'REPORT_HISTORY', columnName: 'PRODUCT_NAME')
        }

        addColumn(tableName: "REPORT_HISTORY") {
            column(name: "PRODUCT_NAME_COPY", type: "varchar2(8000 CHAR)")
        }

        sql("update REPORT_HISTORY set PRODUCT_NAME_COPY = PRODUCT_NAME;")

        dropColumn(tableName: "REPORT_HISTORY", columnName: "PRODUCT_NAME")

        renameColumn(tableName: "REPORT_HISTORY", oldColumnName: "PRODUCT_NAME_COPY", newColumnName: "PRODUCT_NAME")

        addNotNullConstraint(tableName: "REPORT_HISTORY", columnName: "PRODUCT_NAME")
    }

    changeSet(author: "sandeep (generated)", id: "11617101827367-181") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(*) from ATTACHMENT;")
            }
        }
        sql("""UPDATE ATTACHMENT SET REFERENCE_TYPE='Others' where REFERENCE_TYPE is null and ATTACHMENT_TYPE = 'Attachment' AND LNK_ID in( SELECT ID from ATTACHMENT_LINK where REFERENCE_CLASS = 'com.rxlogix.signal.ValidatedSignal');
                     UPDATE ATTACHMENT SET REFERENCE_TYPE='Reference' where REFERENCE_TYPE is null and ATTACHMENT_TYPE = 'Reference' AND LNK_ID in( SELECT ID from ATTACHMENT_LINK where REFERENCE_CLASS = 'com.rxlogix.signal.ValidatedSignal');
                     COMMIT;
                     """)
    }

    changeSet(author: "sandeep (generated)", id: "11617101827367-182") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'COMMENT_BACKUP')
        }

        def config = Holders.grailsApplication.config
        String nextLine = Constants.Alias.NEXT_LINE
        String descriptionStartComment = config.signal.description.migration.start.comment + nextLine
        String descriptionEndComment = nextLine + config.signal.description.migration.end.comment + nextLine

        sql("""update validated_signal set generic_comment = '${descriptionStartComment}' || description ||  '${descriptionEndComment}' || comment_backup;
                     alter table validated_signal
                     drop column comment_backup;
                     commit;
                  """)
    }

    changeSet(author: "sandeep (generated)", id: "11617101827367-183") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ATTACHMENT')
        }
        sql("""update attachment set saved_name = name || '.' || ext where saved_name is null and attachment_type = 'Attachment' and ext is not null;
                     update attachment set input_name = name where input_name = 'attachments';
                     commit;
                  """)
    }



    changeSet(author: "ankit (generated)", id: "11617101827367-192") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK21HX2FDB61YFLSA58Y8YKJUT8")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FK21HX2FDB61YFLSA58Y8YKJUT8")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-193") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK4jhhis76bsysx4xm6i6ioykpc")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK4jhhis76bsysx4xm6i6ioykpc")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-194") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKfwtfcguybm93tkxxgtbkf3yn0")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKfwtfcguybm93tkxxgtbkf3yn0")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-195") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKd9ws3gvh9jkokbmx8qej0nt7q")
        }
        dropForeignKeyConstraint(baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FKd9ws3gvh9jkokbmx8qej0nt7q")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-196") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKf8kqtv3nu1fgrdlcikfp4nfcm")
        }
        dropForeignKeyConstraint(baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKf8kqtv3nu1fgrdlcikfp4nfcm")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-197") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKopm5twbskg380tovcrs4r2gxs")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKopm5twbskg380tovcrs4r2gxs")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-198") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FKk07u3gx8x9cfv56t566onjcfs")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FKk07u3gx8x9cfv56t566onjcfs")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-199") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK2jg2pehpoo65k30r8hp0ynaia")
        }
        dropForeignKeyConstraint(baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK2jg2pehpoo65k30r8hp0ynaia")

    }

    changeSet(author: "ankit (generated)", id: "11617101827367-200") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_LITERATURE_EXEC_CONFIG_ID')
            }
        }
        createIndex(indexName: "IX_LITERATURE_EXEC_CONFIG_ID", tableName: "LITERATURE_ALERT") {
            column(name: "EX_LIT_SEARCH_CONFIG_ID")
        }
    }

    changeSet(author: "ankit (generated)", id: "11617101827367-201") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_single_alert_exconfig", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "EXEC_CONFIG_ID")
        }
    }
}
