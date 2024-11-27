databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1573631364906-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'agg_on_demand_sequence')
            }
        }
        createSequence(sequenceName: "agg_on_demand_sequence")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'evdas_on_demand_sequence')
            }
        }
        createSequence(sequenceName: "evdas_on_demand_sequence")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'single_on_demand_sequence')
            }
        }
        createSequence(sequenceName: "single_on_demand_sequence")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AGG_ON_DEMAND_ALERT')
            }
        }
        createTable(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "cum_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "listed", type: "VARCHAR2(255 CHAR)")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_serious_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_spon_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_study_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "positive_dechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "pregnancy", type: "VARCHAR2(255 CHAR)")

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "prr05", type: "FLOAT(24)")

            column(name: "prr95", type: "FLOAT(24)")

            column(name: "prr_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "related", type: "VARCHAR2(255 CHAR)")

            column(name: "ror05", type: "FLOAT(24)")

            column(name: "ror95", type: "FLOAT(24)")

            column(name: "ror_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "smq_code", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'EMAIL_NOTIFICATION')
            }
        }
        createTable(tableName: "EMAIL_NOTIFICATION") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EMAIL_NOTIFICATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DEFAULT_VALUE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "KEY", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MODULE_NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'EVDAS_ON_DEMAND_ALERT')
            }
        }
        createTable(tableName: "EVDAS_ON_DEMAND_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "all_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "asia_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "europe_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "hlgt", type: "VARCHAR2(255 CHAR)")

            column(name: "hlt", type: "VARCHAR2(255 CHAR)")

            column(name: "japan_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ev", type: "NUMBER(10, 0)")

            column(name: "new_fatal", type: "NUMBER(10, 0)")

            column(name: "new_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "new_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "new_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "new_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "new_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "new_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "new_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "new_serious", type: "NUMBER(10, 0)")

            column(name: "new_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "north_america_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "pt", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "rest_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr", type: "VARCHAR2(255 CHAR)")

            column(name: "smq_narrow", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)")

            column(name: "substance", type: "VARCHAR2(255 CHAR)")

            column(name: "substance_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tot_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont", type: "VARCHAR2(255 CHAR)")

            column(name: "total_ev", type: "NUMBER(10, 0)")

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_ADHOC')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_EVDAS_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_LITR_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_ADHOC')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_EVDAS_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_LITR_CONFIG')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_MED_ERR_PT_LIST')
            }
        }
        createTable(tableName: "SINGLE_ALERT_MED_ERR_PT_LIST") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_MED_ERROR", type: "CLOB")

            column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ON_DEMAND_ALERT')
            }
        }
        createTable(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "age", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "app_type_and_num", type: "VARCHAR2(255 CHAR)")

            column(name: "attributes", type: "CLOB")

            column(name: "case_id", type: "NUMBER(19, 0)")

            column(name: "case_init_receipt_date", type: "TIMESTAMP")

            column(name: "CASE_NARRATIVE", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "case_report_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_version", type: "NUMBER(10, 0)")

            column(name: "completeness_score", type: "FLOAT(24)")

            column(name: "compounding_flag", type: "VARCHAR2(4000 CHAR)")

            column(name: "con_comit", type: "CLOB")

            column(name: "CON_MEDS", type: "CLOB")

            column(name: "country", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "death", type: "VARCHAR2(255 CHAR)")

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_exists", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_number", type: "NUMBER(10, 0)")

            column(name: "gender", type: "VARCHAR2(255 CHAR)")

            column(name: "ind_number", type: "VARCHAR2(255 CHAR)")

            column(name: "is_duplicate", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "VARCHAR2(255 CHAR)")

            column(name: "locked_date", type: "TIMESTAMP")

            column(name: "master_pref_term_all", type: "VARCHAR2(2000 CHAR)")

            column(name: "MED_ERRORS_PT", type: "CLOB")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "outcome", type: "VARCHAR2(255 CHAR)")

            column(name: "patient_age", type: "DOUBLE precision")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "product_family", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "reporters_hcp_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "serious", type: "VARCHAR2(255 CHAR)")

            column(name: "submitter", type: "VARCHAR2(255 CHAR)")

            column(name: "SUSP_PROD", type: "CLOB")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'MED_ERRORS_PT')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "MED_ERRORS_PT", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'app_type_and_num')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "app_type_and_num", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'case_type')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "case_type", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'completeness_score')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "completeness_score", type: "double precision")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'compounding_flag')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "compounding_flag", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'ind_number')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "ind_number", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'is_duplicate')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "is_duplicate", type: "number(1, 0)", defaultValue :"0") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'patient_age')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "patient_age", type: "DOUBLE precision")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'submitter')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "submitter", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_AGG_ON_DEMAND_ALERTPK')
            }
        }
        createIndex(indexName: "IX_AGG_ON_DEMAND_ALERTPK", tableName: "AGG_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "AGG_ON_DEMAND_ALERTPK", forIndexName: "IX_AGG_ON_DEMAND_ALERTPK", tableName: "AGG_ON_DEMAND_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-28") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_EVDAS_ON_DEMAND_ALERTPK')
            }
        }
        createIndex(indexName: "IX_EVDAS_ON_DEMAND_ALERTPK", tableName: "EVDAS_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_ON_DEMAND_ALERTPK", forIndexName: "IX_EVDAS_ON_DEMAND_ALERTPK", tableName: "EVDAS_ON_DEMAND_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-29") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_SINGLE_ON_DEMAND_ALERTPK')
            }
        }
        createIndex(indexName: "IX_SINGLE_ON_DEMAND_ALERTPK", tableName: "SINGLE_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SINGLE_ON_DEMAND_ALERTPK", forIndexName: "IX_SINGLE_ON_DEMAND_ALERTPK", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-30") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_RPT_FIELD_GROUPPK')
            }
        }
        createIndex(indexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP", unique: "true") {
            column(name: "NAME")
        }

        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELD_GROUPNAME_COL", forIndexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-31") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK2vgu56trc9yw9pxi6qn8d7upj')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ON_DEMAND_ALERT", constraintName: "FK2vgu56trc9yw9pxi6qn8d7upj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-32") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK7vrrx66aotafbdx6uc56kbqvu')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_ADHOC", constraintName: "FK7vrrx66aotafbdx6uc56kbqvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-33") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK86n78knlffb2f3yg8c9jhhlqj')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_LITR_CONFIG", constraintName: "FK86n78knlffb2f3yg8c9jhhlqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-34") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK88ek3tuwyqh526ly1ij7f84as')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_CONFIG", constraintName: "FK88ek3tuwyqh526ly1ij7f84as", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-35") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FK89l5ihyquqm5cgq9mlooxfyx9')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ON_DEMAND_ALERT", constraintName: "FK89l5ihyquqm5cgq9mlooxfyx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-36") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKfk7kmeqebxjcxsgq733o3uh0d')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_LITR_CONFIG", constraintName: "FKfk7kmeqebxjcxsgq733o3uh0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-37") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKj293s15eb37wlr6gn0yjjpprh')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_ADHOC", constraintName: "FKj293s15eb37wlr6gn0yjjpprh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-38") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKjwr7h59vtbhc7yycaiixb3d30')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_EVDAS_CONFIG", constraintName: "FKjwr7h59vtbhc7yycaiixb3d30", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-39") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKmvf233chim9ujyk92n0fjedw4')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ON_DEMAND_ALERT", constraintName: "FKmvf233chim9ujyk92n0fjedw4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-40") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKob80eqdx25087jypm0j5rehhh')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_EVDAS_CONFIG", constraintName: "FKob80eqdx25087jypm0j5rehhh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-41") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKpcpkomkegxwykoq2am8py9ayt')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_CONFIG", constraintName: "FKpcpkomkegxwykoq2am8py9ayt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-42") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKr4pfuk8ly0nuynw87d5jg8cct')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ON_DEMAND_ALERT", constraintName: "FKr4pfuk8ly0nuynw87d5jg8cct", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-43") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKt89eni1sgfgosaee42phw0j0a')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_ON_DEMAND_ALERT", constraintName: "FKt89eni1sgfgosaee42phw0j0a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-44") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: 'FKtphrpv7ktdcanh8loeb1e5qcv')
            }
        }
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_ON_DEMAND_ALERT", constraintName: "FKtphrpv7ktdcanh8loeb1e5qcv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-65") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'WKFL_RUL_DISPOSITIONS', columnName: 'WORKFLOW_RULE_ID')
        }
        dropColumn(columnName: "WORKFLOW_RULE_ID", tableName: "WKFL_RUL_DISPOSITIONS")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-78") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'PRR_VALUE')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_VALUE_COPY", type: "NUMBER")
        }
        sql("update AGG_ALERT set PRR_VALUE_COPY = PRR_VALUE;")

        dropColumn(tableName: "AGG_ALERT", columnName: "PRR_VALUE")

        renameColumn(tableName: "AGG_ALERT", oldColumnName: "PRR_VALUE_COPY", newColumnName: "PRR_VALUE")
        addNotNullConstraint(columnDataType: "NUMBER", columnName: "PRR_VALUE", tableName: "AGG_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-79") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'prr05')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr05_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set prr05_COPY = prr05;")

        dropColumn(tableName: "AGG_ALERT", columnName: "prr05")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "prr05_COPY", newColumnName: "prr05")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-80") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'prr95')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr95_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set prr95_COPY = prr95;")

        dropColumn(tableName: "AGG_ALERT", columnName: "prr95")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "prr95_COPY", newColumnName: "prr95")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-81") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'ROR_VALUE')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ROR_VALUE_COPY", type: "NUMBER")
        }
        sql("update AGG_ALERT set ROR_VALUE_COPY = ROR_VALUE;")

        dropColumn(tableName: "AGG_ALERT", columnName: "ROR_VALUE")

        renameColumn(tableName: "AGG_ALERT", oldColumnName: "ROR_VALUE_COPY", newColumnName: "ROR_VALUE")
        addNotNullConstraint(columnDataType: "NUMBER", columnName: "ROR_VALUE", tableName: "AGG_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-82") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'ror05')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror05_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set ror05_COPY = ror05;")

        dropColumn(tableName: "AGG_ALERT", columnName: "ror05")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "ror05_COPY", newColumnName: "ror05")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-83") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'AGG_ALERT', columnName: 'ror95')
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror95_COPY", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
        sql("update AGG_ALERT set ror95_COPY = ror95;")

        dropColumn(tableName: "AGG_ALERT", columnName: "ror95")


        renameColumn(tableName: "AGG_ALERT", oldColumnName: "ror95_COPY", newColumnName: "ror95")
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-84") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM AGG_ALERT WHERE ADHOC_RUN = 1 AND ROWNUM = 1;")
        }
        try {
            sql('''
              INSERT INTO AGG_ON_DEMAND_ALERT
                (ID,VERSION,ALERT_CONFIGURATION_ID,CREATED_BY,CUM_FATAL_COUNT,CUM_SERIOUS_COUNT,CUM_SPON_COUNT,CUM_STUDY_COUNT,DATE_CREATED,EB05,EB95,
                 EBGM,EXEC_CONFIGURATION_ID,FLAGGED,LAST_UPDATED,LISTED,MODIFIED_BY,NAME,NEW_FATAL_COUNT,NEW_SERIOUS_COUNT,NEW_SPON_COUNT,NEW_STUDY_COUNT,
                 PERIOD_END_DATE,PERIOD_START_DATE,POSITIVE_DECHALLENGE,POSITIVE_RECHALLENGE,PREGNANCY,PRODUCT_ID,PRODUCT_NAME,PRR05,PRR95,PRR_VALUE,
                 PT,PT_CODE,RELATED,ROR05,ROR95,ROR_VALUE,SMQ_CODE,SOC)
              SELECT agg_on_demand_sequence.nextval,VERSION,ALERT_CONFIGURATION_ID,CREATED_BY,CUM_FATAL_COUNT,CUM_SERIOUS_COUNT,CUM_SPON_COUNT,CUM_STUDY_COUNT,DATE_CREATED,EB05,EB95,EBGM,
              EXEC_CONFIGURATION_ID,FLAGGED,LAST_UPDATED,LISTED,MODIFIED_BY,NAME,NEW_FATAL_COUNT,NEW_SERIOUS_COUNT,NEW_SPON_COUNT,NEW_STUDY_COUNT,PERIOD_END_DATE,
              PERIOD_START_DATE,POSITIVE_DECHALLENGE,POSITIVE_RECHALLENGE,PREGENENCY,PRODUCT_ID,PRODUCT_NAME,PRR05,PRR95,PRR_VALUE,PT,PT_CODE,RELATED,ROR05,ROR95,ROR_VALUE,SMQ_CODE,SOC 
              FROM AGG_ALERT WHERE ADHOC_RUN = 1;

              DELETE FROM AGG_SIGNAL_CONCEPTS WHERE AGG_ALERT_ID IN (SELECT ID FROM AGG_ALERT WHERE ADHOC_RUN=1); 
              DELETE FROM VALIDATED_AGG_ALERTS WHERE AGG_ALERT_ID IN (SELECT ID FROM AGG_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM AGG_ALERT_TAGS WHERE AGG_ALERT_ID IN (SELECT ID FROM AGG_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM AGG_ALERT_ACTIONS WHERE AGG_ALERT_ID IN (SELECT ID FROM AGG_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM AGG_ALERT WHERE ADHOC_RUN=1;
              COMMIT;
              
             ''')
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-85") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN = 1 AND ROWNUM = 1;")
        }
        try {
            sql('''
              INSERT INTO SINGLE_ON_DEMAND_ALERT
                (ID,VERSION,ADHOC_RUN,AGE,ALERT_CONFIGURATION_ID,ATTRIBUTES,CASE_ID,CASE_INIT_RECEIPT_DATE,CASE_NARRATIVE,CASE_NUMBER,CASE_REPORT_TYPE,
                 CASE_VERSION,CON_COMIT,CON_MEDS,COUNTRY,CREATED_BY,DATE_CREATED,DEATH,DETECTED_DATE,EXEC_CONFIG_ID,FLAGGED,FOLLOW_UP_EXISTS,FOLLOW_UP_NUMBER,
                 GENDER,IS_NEW,LAST_UPDATED,LISTEDNESS,LOCKED_DATE,MASTER_PREF_TERM_ALL,MODIFIED_BY,NAME,OUTCOME,PERIOD_END_DATE,PERIOD_START_DATE
                 ,PRODUCT_FAMILY,PRODUCT_ID,PRODUCT_NAME,PT,RECHALLENGE,REPORTERS_HCP_FLAG,REVIEW_DATE,SERIOUS,SUSP_PROD, is_duplicate)
              SELECT single_on_demand_sequence.nextval,VERSION,ADHOC_RUN,AGE,ALERT_CONFIGURATION_ID,ATTRIBUTES,CASE_ID,CASE_INIT_RECEIPT_DATE,CASE_NARRATIVE,CASE_NUMBER,CASE_REPORT_TYPE,CASE_VERSION,
                 CON_COMIT,CON_MEDS,COUNTRY,CREATED_BY,DATE_CREATED,DEATH,DETECTED_DATE,EXEC_CONFIG_ID,FLAGGED,FOLLOW_UP_EXISTS,FOLLOW_UP_NUMBER,GENDER,
                 IS_NEW,LAST_UPDATED,LISTEDNESS,LOCKED_DATE,MASTER_PREF_TERM_ALL,MODIFIED_BY,NAME,OUTCOME,PERIOD_END_DATE,PERIOD_START_DATE,PRODUCT_FAMILY,
                 PRODUCT_ID,PRODUCT_NAME,PT,RECHALLENGE,REPORTERS_HCP_FLAG,REVIEW_DATE,SERIOUS,SUSP_PROD, '0' FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN = 1;


              DELETE FROM SINGLE_SIGNAL_CONCEPTS WHERE SINGLE_CASE_ALERT_ID IN (SELECT ID FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM VALIDATED_SINGLE_ALERTS WHERE SINGLE_ALERT_ID IN (SELECT ID FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM SINGLE_ALERT_PT WHERE SINGLE_ALERT_ID IN (SELECT ID FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM SINGLE_ALERT_CON_COMIT WHERE SINGLE_ALERT_ID IN (SELECT ID FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM SINGLE_ALERT_SUSP_PROD WHERE SINGLE_ALERT_ID IN (SELECT ID FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM SINGLE_ALERT_ACTIONS WHERE SINGLE_CASE_ALERT_ID IN (SELECT ID FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM SINGLE_GLOBAL_TAG_MAPPING WHERE SINGLE_ALERT_ID IN (SELECT ID FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM SINGLE_CASE_ALERT WHERE ADHOC_RUN = 1;
              COMMIT;

             ''')
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "amrendra (generated)", id: "1573631364906-86") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM EVDAS_ALERT WHERE ADHOC_RUN = 1 AND ROWNUM = 1;")
        }
        try {
            sql('''
              INSERT INTO EVDAS_ON_DEMAND_ALERT
                (ID, VERSION, ALERT_CONFIGURATION_ID, ALL_ROR, ASIA_ROR, CREATED_BY, DATE_CREATED, EUROPE_ROR, EXEC_CONFIGURATION_ID, FLAGGED, HLGT, HLT, JAPAN_ROR, LAST_UPDATED,
                 MODIFIED_BY, NAME, NEW_EEA, NEW_EV, NEW_FATAL, NEW_GERIA, NEW_HCP, NEW_LIT, NEW_MED_ERR, NEW_OBS, NEW_PAED, NEW_RC, NEW_SERIOUS, NEW_SPONT, NORTH_AMERICA_ROR, 
                 PERIOD_END_DATE, PERIOD_START_DATE, PT, PT_CODE, REST_ROR, ROR_VALUE, SDR, SMQ_NARROW, SOC, SUBSTANCE, SUBSTANCE_ID, TOT_EEA, TOT_GERIA, TOT_HCP, TOT_MED_ERR,
                 TOT_OBS, TOT_PAED, TOT_RC, TOT_SPONT, TOTAL_EV, TOTAL_FATAL, TOTAL_LIT, TOTAL_SERIOUS)
             SELECT evdas_on_demand_sequence.nextval, VERSION, ALERT_CONFIGURATION_ID, ALL_ROR, ASIA_ROR, CREATED_BY, DATE_CREATED, EUROPE_ROR, EXEC_CONFIGURATION_ID, FLAGGED, HLGT, HLT, JAPAN_ROR, LAST_UPDATED,
                MODIFIED_BY, NAME, NEW_EEA, NEW_EV, NEW_FATAL, NEW_GERIA, NEW_HCP, NEW_LIT, NEW_MED_ERR, NEW_OBS, NEW_PAED, NEW_RC, NEW_SERIOUS, NEW_SPONT, NORTH_AMERICA_ROR, 
                 PERIOD_END_DATE, PERIOD_START_DATE, PT, PT_CODE, REST_ROR, ROR_VALUE, SDR, SMQ_NARROW, SOC, SUBSTANCE, SUBSTANCE_ID, TOT_EEA, TOT_GERIA, TOT_HCP, TOT_MED_ERR,
                 TOT_OBS, TOT_PAED, TOT_RC, TOT_SPONT, TOTAL_EV, TOTAL_FATAL, TOTAL_LIT, TOTAL_SERIOUS
             FROM EVDAS_ALERT WHERE ADHOC_RUN = 1;

              DELETE FROM EVDAS_SIGNAL_CONCEPTS WHERE EVDAS_ALERT_ID IN (SELECT ID FROM EVDAS_ALERT WHERE ADHOC_RUN=1); 
              DELETE FROM VALIDATED_EVDAS_ALERTS WHERE EVDAS_ALERT_ID IN (SELECT ID FROM EVDAS_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM EVDAS_ALERT_ACTIONS WHERE EVDAS_ALERT_ID IN (SELECT ID FROM EVDAS_ALERT WHERE ADHOC_RUN=1);
              DELETE FROM EVDAS_ALERT WHERE ADHOC_RUN=1;
              COMMIT;
              
             ''')
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-1") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
                 insert into groups(id,version,CREATED_BY,DATE_CREATED,GROUP_TYPE,IS_ACTIVE,LAST_UPDATED,MODIFIED_BY,NAME)
                 values((select max(ID)+1 from GROUPS),0,'System',Systimestamp,'USER_GROUP',1,Systimestamp,'System','All Users');
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249432-2") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', '''
                      select count(*) from pvuser where id not in (
                      SELECT usr.id
                      FROM pvuser usr
                      JOIN USER_GROUP_S ug ON (usr.ID = ug.USER_ID)
                      JOIN groups grp ON grp.ID = ug.GROUP_ID
                      where grp.id = (select ID from GROUPS WHERE NAME = 'All Users')) AND ROWNUM = 1
                      ''')
        }
        try {
            sql('''
                INSERT INTO USER_GROUP_S(GROUP_ID,USER_ID)
                   (select (select ID from GROUPS WHERE NAME = 'All Users'),  id from pvuser where id not in (
                      SELECT usr.id
                      FROM pvuser usr
                      JOIN USER_GROUP_S ug ON (usr.ID = ug.USER_ID)
                      JOIN groups grp ON grp.ID = ug.GROUP_ID
                      where grp.id = (select ID from GROUPS WHERE NAME = 'All Users')));
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-3") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
               INSERT INTO SHARE_WITH_GROUP_CONFIG(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,CONFIG_ID)
                (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from RCONFIG);
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-4") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
               INSERT INTO SHARE_WITH_GROUP_EVDAS_CONFIG(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,CONFIG_ID)
               (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from EVDAS_CONFIG);  
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "ankit (generated)", id: "1573016249431-5") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
               INSERT INTO SHARE_WITH_GROUP_ADHOC(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,CONFIG_ID)
               (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from ALERTS);  
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "prashantsahi (generated)", id: "20190823330888-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMAIL_LOG', columnName: 'message')
        }
        addColumn(tableName: "EMAIL_LOG") {
            column(name: "message_COPY", type: "clob") {
                constraints(nullable: "true")
            }
        }
        sql("update EMAIL_LOG set message_COPY = message;")

        dropColumn(tableName: "EMAIL_LOG", columnName: "message")

        renameColumn(tableName: "EMAIL_LOG", oldColumnName: "message_COPY", newColumnName: "message")
    }

    changeSet(author: "rishabhgupta (generated)", id: "20190823140830-6") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EVDAS_HISTORY', columnName: 'JUSTIFICATION')
        }
        modifyDataType(columnName: "JUSTIFICATION", newDataType: "varchar2(4000 char)", tableName: "EVDAS_HISTORY")
    }

    changeSet(author: "rishabhgupta (generated)", id: "20190823140830-9") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'JUSTIFICATION')
        }
        modifyDataType(columnName: "JUSTIFICATION", newDataType: "varchar2(4000 char)", tableName: "PRODUCT_EVENT_HISTORY")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-159") {
        preConditions(onFail: 'MARK_RAN') {
            notNullConstraintExists(columnName: "sent_to", tableName: "EMAIL_LOG")
        }
        dropNotNullConstraint(columnDataType: "varchar(4000 CHAR)", columnName: "sent_to", tableName: "EMAIL_LOG")
    }

    changeSet(author: "amrendra (generated)", id: "1573900006619-161") {
        preConditions(onFail: 'MARK_RAN') {
            notNullConstraintExists(columnName: "subject", tableName: "EMAIL_LOG")
        }
        dropNotNullConstraint(columnDataType: "varchar(4000 CHAR)", columnName: "subject", tableName: "EMAIL_LOG")
    }
}
