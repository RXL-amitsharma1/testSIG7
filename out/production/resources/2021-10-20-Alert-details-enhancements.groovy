databaseChangeLog = {
    changeSet(author: "nitesh (generated)", id: "1632254096073-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'generic_name')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "generic_name", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'case_creation_date')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "case_creation_date",type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-3") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'date_of_birth')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "date_of_birth",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-4") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'event_onset_date')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "event_onset_date",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-5") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'pregnancy')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "pregnancy",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-6") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'medically_confirmed')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "medically_confirmed",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-7") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'allpts_outcome')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "allpts_outcome", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_GENERIC_NAME')
            }
        }
        createTable(tableName: "SINGLE_ALERT_GENERIC_NAME") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GENERIC_NAME", type: "varchar(15000)")

            column(name: "GENERIC_NAME_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_ALLPT_OUT_COME')
            }
        }
        createTable(tableName: "SINGLE_ALERT_ALLPT_OUT_COME") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALLPTS_OUTCOME", type: "varchar(15000)")

            column(name: "ALLPTS_OUTCOME_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }

    //ON DEMAND
    changeSet(author: "nitesh (generated)", id: "1632254096073-10") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'generic_name')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "generic_name", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-11") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'case_creation_date')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "case_creation_date",type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-12") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'date_of_birth')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "date_of_birth",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-13") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'event_onset_date')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "event_onset_date",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-14") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'pregnancy')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "pregnancy",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-15") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'medically_confirmed')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "medically_confirmed",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-16") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'allpts_outcome')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "allpts_outcome", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIG_DMD_ALRT_GENERIC_NAME')
            }
        }
        createTable(tableName: "SIG_DMD_ALRT_GENERIC_NAME") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GENERIC_NAME", type: "varchar(15000)")

            column(name: "GENERIC_NAME_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SIG_DMD_ALRT_ALLPT_OUT_COME')
            }
        }
        createTable(tableName: "SIG_DMD_ALRT_ALLPT_OUT_COME") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALLPTS_OUTCOME", type: "varchar(15000)")

            column(name: "ALLPTS_OUTCOME_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }

    // For archived single case alert

    changeSet(author: "nitesh (generated)", id: "1632254096073-19") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'generic_name')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "generic_name", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-20") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'case_creation_date')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "case_creation_date",type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-21") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'date_of_birth')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "date_of_birth",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-22") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'event_onset_date')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "event_onset_date",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-23") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'pregnancy')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "pregnancy",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-24") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'medically_confirmed')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "medically_confirmed",type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-25") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'allpts_outcome')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "allpts_outcome", type: "CLOB") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-26") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SINGLE_ALERT_GENERIC_NAME')
            }
        }
        createTable(tableName: "AR_SINGLE_ALERT_GENERIC_NAME") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GENERIC_NAME", type: "varchar(15000)")

            column(name: "GENERIC_NAME_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096073-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SINGLE_ALERT_ALLPT_OUT_COME')
            }
        }
        createTable(tableName: "AR_SINGLE_ALERT_ALLPT_OUT_COME") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALLPTS_OUTCOME", type: "varchar(15000)")

            column(name: "ALLPTS_OUTCOME_LIST_IDX", type: "NUMBER(10, 0)")
        }
    }

}