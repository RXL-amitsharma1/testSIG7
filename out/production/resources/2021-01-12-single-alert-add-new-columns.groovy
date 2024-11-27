databaseChangeLog = {

    changeSet(author: "nitesh (generated)", id: "1608824568978-1") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-2") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-3") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-4") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-5") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-6") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-7") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-8") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-9") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-10") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-11") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-12") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-13") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-14") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568978-15") {

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

    changeSet(author: "nitesh (generated)", id: "1608824568979-4") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568979-5") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568979-6") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568979-7") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568979-8") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568979-9") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568979-10") {
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

    changeSet(author: "nitesh (generated)", id: "1608824568979-11") {
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

    changeSet(author: "nitesh (generated)", id: "1608824568980-1") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-2") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-3") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-4") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-5") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-6") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-7") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-8") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-9") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-10") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-11") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-12") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-13") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-14") {

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
    changeSet(author: "nitesh (generated)", id: "1608824568980-15") {

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

    changeSet(author: "nitesh (generated)", id: "1608824568980-16") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568980-17") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568980-18") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568980-19") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568980-20") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568980-21") {
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
    changeSet(author: "nitesh (generated)", id: "1608824568980-22") {
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

    changeSet(author: "nitesh (generated)", id: "1608824568980-23") {
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

}
