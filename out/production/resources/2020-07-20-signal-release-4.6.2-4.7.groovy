import com.rxlogix.ActivityService
import com.rxlogix.BusinessConfigurationService
import com.rxlogix.PvsAlertTagService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.config.Activity
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureActivity
import com.rxlogix.config.LiteratureConfiguration
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1595253951671-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AGG_ALERT_IMP_EVENT_LIST')
            }
        }
        createTable(tableName: "AGG_ALERT_IMP_EVENT_LIST") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGA_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AGG_CASE_ALERT_TAGS')
            }
        }
        createTable(tableName: "AGG_CASE_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AGG_GLOBAL_TAGS')
            }
        }
        createTable(tableName: "AGG_GLOBAL_TAGS") {
            column(name: "GLOBAL_PRODUCT_EVENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_ACA_ACTIONS')
            }
        }
        createTable(tableName: "ARCHIVED_ACA_ACTIONS") {
            column(name: "ARCHIVED_ACA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_AGG_ALERT')
            }
        }
        createTable(tableName: "ARCHIVED_AGG_ALERT") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_AGG_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "chi_square", type: "FLOAT(24)")

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

            column(name: "cumm_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cumm_pediatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb05age", type: "CLOB")

            column(name: "eb05gender", type: "CLOB")

            column(name: "eb05str", type: "CLOB")

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95age", type: "CLOB")

            column(name: "eb95gender", type: "CLOB")

            column(name: "eb95str", type: "CLOB")

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_age", type: "CLOB")

            column(name: "ebgm_gender", type: "CLOB")

            column(name: "ebgm_str", type: "CLOB")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "freq_priority", type: "VARCHAR2(255 CHAR)")

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

            column(name: "imp_events", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_new", type: "NUMBER(1, 0)") {
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

            column(name: "new_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_fatal_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_pediatric_count", type: "NUMBER(10, 0)") {
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

            column(name: "pec_imp_high", type: "VARCHAR2(255 CHAR)")

            column(name: "pec_imp_low", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "positive_dechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "pregenency", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "prr05", type: "FLOAT(24)")

            column(name: "prr95", type: "FLOAT(24)")

            column(name: "prr_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "PRR_STR", type: "CLOB")

            column(name: "prr_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_str95", type: "VARCHAR2(255 CHAR)")

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

            column(name: "requested_by", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "ror05", type: "FLOAT(24)")

            column(name: "ror95", type: "FLOAT(24)")

            column(name: "ror_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "ROR_STR", type: "CLOB")

            column(name: "ror_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_value", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "smq_code", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "trend_type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_AGG_CASE_ALERT_TAGS')
            }
        }
        createTable(tableName: "ARCHIVED_AGG_CASE_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_EA_IMP_EVENT_LIST')
            }
        }
        createTable(tableName: "ARCHIVED_EA_IMP_EVENT_LIST") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "ev_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_EVDAS_ALERT')
            }
        }
        createTable(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_EVDAS_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "all_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "asia_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "changes", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DME_IME", type: "VARCHAR2(255 CHAR)")

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "europe_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "LONG")

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "hlgt", type: "VARCHAR2(255 CHAR)")

            column(name: "hlt", type: "VARCHAR2(255 CHAR)")

            column(name: "imp_events", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "japan_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ev", type: "NUMBER(10, 0)")

            column(name: "new_ev_link", type: "VARCHAR2(600 CHAR)")

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

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "pt", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_code", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ratio_ror_geriatr_vs_others", type: "VARCHAR2(255 CHAR)")

            column(name: "ratio_ror_paed_vs_others", type: "VARCHAR2(255 CHAR)")

            column(name: "requested_by", type: "VARCHAR2(255 CHAR)")

            column(name: "rest_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr_geratr", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr_paed", type: "VARCHAR2(255 CHAR)")

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

            column(name: "tot_spont_asia", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_europe", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_japan", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spontnamerica", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spont_rest", type: "VARCHAR2(255 CHAR)")

            column(name: "total_ev", type: "NUMBER(10, 0)")

            column(name: "total_ev_link", type: "VARCHAR2(600 CHAR)")

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_EVDAS_ALERT_ACTIONS')
            }
        }
        createTable(tableName: "ARCHIVED_EVDAS_ALERT_ACTIONS") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_IMP_EVENT_LIST')
            }
        }
        createTable(tableName: "ARCHIVED_IMP_EVENT_LIST") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGA_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_LITERATURE_ALERT')
            }
        }
        createTable(tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_LITERATURE_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "ARTICLE_ABSTRACT", type: "CLOB")

            column(name: "ARTICLE_AUTHORS", type: "CLOB")

            column(name: "article_id", type: "NUMBER(10, 0)")

            column(name: "ARTICLE_TITLE", type: "CLOB")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "ex_lit_search_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "lit_search_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "PUBLICATION_DATE", type: "VARCHAR2(255 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_LIT_ALERT_ACTIONS')
            }
        }
        createTable(tableName: "ARCHIVED_LIT_ALERT_ACTIONS") {
            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_LIT_CASE_ALERT_TAGS')
            }
        }
        createTable(tableName: "ARCHIVED_LIT_CASE_ALERT_TAGS") {
            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SCA_ACTIONS')
            }
        }
        createTable(tableName: "ARCHIVED_SCA_ACTIONS") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SCA_CON_COMIT')
            }
        }
        createTable(tableName: "ARCHIVED_SCA_CON_COMIT") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_CON_COMIT", type: "CLOB")

            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SCA_MED_ERR_PT_LIST')
            }
        }
        createTable(tableName: "ARCHIVED_SCA_MED_ERR_PT_LIST") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_MED_ERROR", type: "CLOB")

            column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SCA_PT')
            }
        }
        createTable(tableName: "ARCHIVED_SCA_PT") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_SCA_PT", type: "CLOB")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SCA_SUSP_PROD')
            }
        }
        createTable(tableName: "ARCHIVED_SCA_SUSP_PROD") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRODUCT_NAME", type: "CLOB")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SCA_TAGS')
            }
        }
        createTable(tableName: "ARCHIVED_SCA_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT')
            }
        }
        createTable(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ARCHIVED_SINGLE_CASE_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "age", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_alert_id", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_count_type", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_execution_id", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "app_type_and_num", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "badge", type: "VARCHAR2(255 CHAR)")

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

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_exists", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "follow_up_number", type: "NUMBER(10, 0)")

            column(name: "gender", type: "VARCHAR2(255 CHAR)")

            column(name: "global_identity_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ind_number", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "is_duplicate", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "VARCHAR2(255 CHAR)")

            column(name: "locked_date", type: "TIMESTAMP")

            column(name: "master_pref_term_all", type: "CLOB")

            column(name: "MED_ERRORS_PT", type: "CLOB")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "outcome", type: "VARCHAR2(255 CHAR)")

            column(name: "patient_age", type: "FLOAT(24)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "superseded_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "SUSP_PROD", type: "CLOB")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'EVDAS_ALERT_IMP_EVENT_LIST')
            }
        }
        createTable(tableName: "EVDAS_ALERT_IMP_EVENT_LIST") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "ev_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'LITERATURE_CASE_ALERT_TAGS')
            }
        }
        createTable(tableName: "LITERATURE_CASE_ALERT_TAGS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'LITERATURE_HISTORY')
            }
        }
        createTable(tableName: "LITERATURE_HISTORY") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "change", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "current_assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "current_assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "current_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "current_priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "lit_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "lit_exec_config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "sub_tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tags_updated", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'LITERAURE_GLOBAL_TAGS')
            }
        }
        createTable(tableName: "LITERAURE_GLOBAL_TAGS") {
            column(name: "GLOBAL_ARTICLE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_FILTER')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_FILTER") {
            column(name: "ADVANCED_FILTER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-26") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_SIGNAL')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_SIGNAL") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_GROUP_VIEW')
            }
        }
        createTable(tableName: "SHARE_WITH_GROUP_VIEW") {
            column(name: "VIEW_INSTANCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-300") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_FILTER')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_FILTER") {
            column(name: "ADVANCED_FILTER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-29") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_SIGNAL')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_SIGNAL") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-30") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SHARE_WITH_USER_VIEW')
            }
        }
        createTable(tableName: "SHARE_WITH_USER_VIEW") {
            column(name: "VIEW_INSTANCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-31") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_CASE_ALERT_TAGS')
            }
        }
        createTable(tableName: "SINGLE_CASE_ALERT_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-32") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_GLOBAL_TAGS')
            }
        }
        createTable(tableName: "SINGLE_GLOBAL_TAGS") {
            column(name: "GLOBAL_CASE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-33") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'VALIDATED_ARCHIVED_ACA')
            }
        }
        createTable(tableName: "VALIDATED_ARCHIVED_ACA") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_ACA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-34") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'VALIDATED_ARCHIVED_LIT_ALERTS')
            }
        }
        createTable(tableName: "VALIDATED_ARCHIVED_LIT_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-35") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'VALIDATED_ARCHIVED_SCA')
            }
        }
        createTable(tableName: "VALIDATED_ARCHIVED_SCA") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-36") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'VALIDATED_ARCH_EVDAS_ALERTS')
            }
        }
        createTable(tableName: "VALIDATED_ARCH_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-37") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'default_view_mapping')
            }
        }
        createTable(tableName: "default_view_mapping") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "default_view_instance_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-38") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'global_article')
            }
        }
        createTable(tableName: "global_article") {
            column(autoIncrement: "true", name: "globalArticleId", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "global_articlePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-39") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'global_case')
            }
        }
        createTable(tableName: "global_case") {
            column(autoIncrement: "true", name: "globalCaseId", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "global_casePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-40") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'global_product_event')
            }
        }
        createTable(tableName: "global_product_event") {
            column(autoIncrement: "true", name: "globalProductEventId", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "global_product_eventPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_event_comb", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-41") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'priority_disposition_config')
            }
        }
        createTable(tableName: "priority_disposition_config") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "priority_disposition_configPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "disposition_order", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "disposition_configs_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-42") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'pvs_alert_tag')
            }
        }
        createTable(tableName: "pvs_alert_tag") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "pvs_alert_tagPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_at", type: "TIMESTAMP")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "domain", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_master_category", type: "NUMBER(1, 0)")

            column(name: "mart_id", type: "NUMBER(19, 0)")

            column(name: "modified_at", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "priority", type: "NUMBER(10, 0)")

            column(name: "private_user", type: "VARCHAR2(255 CHAR)")

            column(name: "sub_tag_id", type: "NUMBER(19, 0)")

            column(name: "sub_tag_text", type: "VARCHAR2(255 CHAR)")

            column(name: "tag_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tag_text", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-43") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'pvs_global_tag')
            }
        }
        createTable(tableName: "pvs_global_tag") {
            column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "pvs_global_tagPK")
            }
            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_at", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "domain", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "global_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_master_category", type: "NUMBER(1, 0)")

            column(name: "mart_id", type: "NUMBER(19, 0)")

            column(name: "modified_at", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "priority", type: "NUMBER(10, 0)")

            column(name: "private_user", type: "VARCHAR2(255 CHAR)")

            column(name: "sub_tag_id", type: "NUMBER(19, 0)")

            column(name: "sub_tag_text", type: "VARCHAR2(255 CHAR)")

            column(name: "tag_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tag_text", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "amrendra (generated)", id: "1595253951671-44") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'CONFIG_ID')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-45") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'CONFIG_ID')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "CONFIG_ID", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-46") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CONFIG_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CONFIG_ID", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-47") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'NUM_OF_EXECUTIONS')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "NUM_OF_EXECUTIONS", type: "number(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-48") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALUES_PARAMS', columnName: 'PARAM_IDX')
            }
        }
        addColumn(tableName: "VALUES_PARAMS") {
            column(name: "PARAM_IDX", type: "number(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-49") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'SELECTED_DATA_SOURCE')
            }
        }
        addColumn(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "SELECTED_DATA_SOURCE", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-50") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_CONFIG', columnName: 'SELECTED_DATA_SOURCE')
            }
        }
        addColumn(tableName: "LITERATURE_CONFIG") {
            column(name: "SELECTED_DATA_SOURCE", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-51") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'archived_agg_case_alert_id')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "archived_agg_case_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-52") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'archived_evdas_alert_id')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "archived_evdas_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-53") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_HISTORY', columnName: 'archived_single_alert_id')
            }
        }
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "archived_single_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-54") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'chi_square')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "chi_square", type: "double precision")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-55") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'default_adhoc_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "default_adhoc_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-56") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'default_evdas_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "default_evdas_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-57") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'default_lit_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "default_lit_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-58") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'default_quali_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "default_quali_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-59") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GROUPS', columnName: 'default_quant_disposition_id')
            }
        }
        addColumn(tableName: "GROUPS") {
            column(name: "default_quant_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-60") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_HISTORY', columnName: 'due_date')
            }
        }
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-61") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_history', columnName: 'due_date')
            }
        }
        addColumn(tableName: "evdas_history") {
            column(name: "due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-62") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'due_date')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-63") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'signal_history', columnName: 'due_date')
            }
        }
        addColumn(tableName: "signal_history") {
            column(name: "due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-64") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb05age')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb05age", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-65") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb05age')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb05age", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-66") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb05gender')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb05gender", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-67") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb05gender')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb05gender", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-68") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb95age')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb95age", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-69") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb95age')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb95age", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-70") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb95gender')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb95gender", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-71") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb95gender')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb95gender", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-72") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ebgm_age')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ebgm_age", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-73") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ebgm_age')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ebgm_age", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-74") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ebgm_gender')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ebgm_gender", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-75") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ebgm_gender')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ebgm_gender", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-76") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'executed_config_id')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "executed_config_id", type: "number(19, 0)")
        }
    }


    changeSet(author: "amrendra (generated)", id: "1595253951671-77") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'executed_configuration_id')
            }
        }
        addColumn(tableName: "LITERATURE_ACTIVITY") {
            column(name: "executed_configuration_id", type: "number(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-78") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'global_identity_id')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "global_identity_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-79") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_ALERT', columnName: 'global_identity_id')
            }
        }
        addColumn(tableName: "LITERATURE_ALERT") {
            column(name: "global_identity_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-80") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'global_identity_id')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "global_identity_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-81") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTIONS', columnName: 'guest_attendee_email')
            }
        }
        addColumn(tableName: "ACTIONS") {
            column(name: "guest_attendee_email", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-82") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTION_TEMPLATE', columnName: 'guest_attendee_email')
            }
        }
        addColumn(tableName: "ACTION_TEMPLATE") {
            column(name: "guest_attendee_email", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-83") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTIVITIES', columnName: 'guest_attendee_email')
            }
        }
        addColumn(tableName: "ACTIVITIES") {
            column(name: "guest_attendee_email", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-84") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'guest_attendee_email')
            }
        }
        addColumn(tableName: "LITERATURE_ACTIVITY") {
            column(name: "guest_attendee_email", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-85") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'imp_events')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "imp_events", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-86") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'imp_events')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "imp_events", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-87") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'initial_disposition_id')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "initial_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-88") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERTS', columnName: 'initial_disposition_id')
            }
        }
        addColumn(tableName: "ALERTS") {
            column(name: "initial_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-89") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'initial_disposition_id')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "initial_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-90") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'initial_disposition_id')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "initial_disposition_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-91") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'initial_due_date')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "initial_due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-92") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERTS', columnName: 'initial_due_date')
            }
        }
        addColumn(tableName: "ALERTS") {
            column(name: "initial_due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-93") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'initial_due_date')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "initial_due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-94") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'initial_due_date')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "initial_due_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-95") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'is_new')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "is_new", type: "number(1, 0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EVDAS_ALERT set IS_NEW = 1;")

        addNotNullConstraint(columnDataType: "NUMBER(1, 0)", columnName: "IS_NEW", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-96") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ACTIVITIES', columnName: 'private_user_name')
            }
        }
        addColumn(tableName: "ACTIVITIES") {
            column(name: "private_user_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-97") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'private_user_name')
            }
        }
        addColumn(tableName: "LITERATURE_ACTIVITY") {
            column(name: "private_user_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-98") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DISPOSITION', columnName: 'reset_review_process')
            }
        }
        addColumn(tableName: "DISPOSITION") {
            column(name: "reset_review_process", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-99") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'review_date')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "review_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-100") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT', columnName: 'review_date')
            }
        }
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "review_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-101") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'review_date')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "review_date", type: "timestamp")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-102") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'attachment', columnName: 'saved_name')
            }
        }
        addColumn(tableName: "attachment") {
            column(name: "saved_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-103") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'evdas_file_process_log', columnName: 'saved_name')
            }
        }
        addColumn(tableName: "evdas_file_process_log") {
            column(name: "saved_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-104") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_HISTORY', columnName: 'sub_tag_name')
            }
        }
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "sub_tag_name", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-105") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'sub_tag_name')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "sub_tag_name", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-106") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'superseded_flag')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "superseded_flag", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-107") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'suspect_product')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "suspect_product", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-108") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'suspect_product')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "suspect_product", type: "number(1, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-109") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_HISTORY', columnName: 'tags_updated')
            }
        }
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "tags_updated", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-110") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'product_event_history', columnName: 'tags_updated')
            }
        }
        addColumn(tableName: "product_event_history") {
            column(name: "tags_updated", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-112") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_ARCHIVED_ACA')
            }
        }
        createIndex(indexName: "IX_ARCHIVED_ACA", tableName: "VALIDATED_ARCHIVED_ACA", unique: "true") {
            column(name: "ARCHIVED_ACA_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_ACA_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_ACA", tableName: "VALIDATED_ARCHIVED_ACA")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-113") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_ARCHIVED_LIT_ALERT')
            }
        }
        createIndex(indexName: "IX_ARCHIVED_LIT_ALERT", tableName: "VALIDATED_ARCHIVED_LIT_ALERTS", unique: "true") {
            column(name: "ARCHIVED_LIT_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_LIT_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_LIT_ALERT", tableName: "VALIDATED_ARCHIVED_LIT_ALERTS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-114") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_ARCHIVED_SCA')
            }
        }
        createIndex(indexName: "IX_ARCHIVED_SCA", tableName: "VALIDATED_ARCHIVED_SCA", unique: "true") {
            column(name: "ARCHIVED_SCA_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_SCA_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_SCA", tableName: "VALIDATED_ARCHIVED_SCA")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-115") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_ARCHIVED_EVDAS_ALERT')
            }
        }
        createIndex(indexName: "IX_ARCHIVED_EVDAS_ALERT", tableName: "VALIDATED_ARCH_EVDAS_ALERTS", unique: "true") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_ARCHIVED_EVDAS_ALERT", tableName: "VALIDATED_ARCH_EVDAS_ALERTS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-116") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                uniqueKeyConstraintExists(uniqueKeyName: "UC_PRODUCT_GROUPGROUP_NAME_COL")
            }
        }
        addUniqueConstraint(columnNames: "group_name", constraintName: "UC_PRODUCT_GROUPGROUP_NAME_COL", tableName: "product_group")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-414") {
        preConditions(onFail: 'MARK_RAN') {
                sqlCheck(expectedResult: '0', "select Count(1) from user_constraints where constraint_name = UPPER('UC_PRODUCT_GROUPGROUP_NAME_COL');")
        }
        createIndex(indexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP", unique: "true") {
            column(name: "NAME")
        }

        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELD_GROUPNAME_COL", forIndexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-415") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select Count(1) from user_constraints where constraint_name = UPPER('UK210f34b0cfe5b5a551cca85a2fb5');")
        }
        addUniqueConstraint(columnNames: "user_id, alert_type, default_view_instance_id", constraintName: "UK210f34b0cfe5b5a551cca85a2fb5", tableName: "default_view_mapping")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-120") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK1iienwc8wr9cid8iy4byqp0mn")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FK1iienwc8wr9cid8iy4byqp0mn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-121") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK212psev5e8n5368qm45hxdim9")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "default_view_mapping", constraintName: "FK212psev5e8n5368qm45hxdim9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-122") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK254hw7jn9lektmkp3q2rg54xd")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_SCA", constraintName: "FK254hw7jn9lektmkp3q2rg54xd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-123") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK2jg2pehpoo65k30r8hp0ynaia")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK2jg2pehpoo65k30r8hp0ynaia", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-124") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK2mesh43trl6soolkyvph22b2m")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK2mesh43trl6soolkyvph22b2m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-125") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK2no3an617yrnom00t5jooj7ux")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SHARE_WITH_GROUP_FILTER", constraintName: "FK2no3an617yrnom00t5jooj7ux", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-126") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK2vwgh7yejh3h4vol9806scbc3")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_ACA_ACTIONS", constraintName: "FK2vwgh7yejh3h4vol9806scbc3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-127") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK30cc3a899ljc1c1mwuvb4gojk")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "priority_disposition_config", constraintName: "FK30cc3a899ljc1c1mwuvb4gojk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-128") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK36oexbhattm8op3hnya3tcgoy")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FK36oexbhattm8op3hnya3tcgoy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-129") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK3h89qxc4n54ltjr00fh3dfe5x")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_group_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK3h89qxc4n54ltjr00fh3dfe5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-130") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK3hsd6jq62pwevxuymr7c47v4o")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "default_quali_disposition_id", baseTableName: "GROUPS", constraintName: "FK3hsd6jq62pwevxuymr7c47v4o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-131") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK3tqo8ii2855i6p1mreo00bsms")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "ARCHIVED_LIT_CASE_ALERT_TAGS", constraintName: "FK3tqo8ii2855i6p1mreo00bsms", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-132") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK4iba2hmpj3ow92lvk695o6cmr")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "priority_disposition_config", constraintName: "FK4iba2hmpj3ow92lvk695o6cmr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-133") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK4k66uoy690sd2sqgt1whsv0ke")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_LIT_ALERT_ACTIONS", constraintName: "FK4k66uoy690sd2sqgt1whsv0ke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-134") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK50yuip9o1motpj4seu0cxi61d")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK50yuip9o1motpj4seu0cxi61d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-135") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK5395n1fvj46kencfit7o01ycm")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK5395n1fvj46kencfit7o01ycm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-136") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK54h8y7xtiv357hvjskdlhv9nh")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "SHARE_WITH_USER_FILTER", constraintName: "FK54h8y7xtiv357hvjskdlhv9nh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-137") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK5b51h27a8nwqul2cqg130od6x")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FK5b51h27a8nwqul2cqg130od6x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-138") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK5lld0tglw6bwtcm3q9v72afnw")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "LITERATURE_CASE_ALERT_TAGS", constraintName: "FK5lld0tglw6bwtcm3q9v72afnw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-139") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK6aecm7dwqlvw1fubghjflo9wk")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "default_adhoc_disposition_id", baseTableName: "GROUPS", constraintName: "FK6aecm7dwqlvw1fubghjflo9wk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-140") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK71rkprlwc9p2db3fbch29cpfs")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FK71rkprlwc9p2db3fbch29cpfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-141") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK7lc0bubx7p9yabs83kcq36ps")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK7lc0bubx7p9yabs83kcq36ps", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-142") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK7xpvynkdusubvmx9lc02em2i3")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_CASE_ID", baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FK7xpvynkdusubvmx9lc02em2i3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-143") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK81yjfpvqj3gom68hkqv4s3o0w")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ALERTS", constraintName: "FK81yjfpvqj3gom68hkqv4s3o0w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-144") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK82sv7r2pavn03feadmrvxov2r")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FK82sv7r2pavn03feadmrvxov2r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-145") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK87vw6p2kttreaxshoagh8nhxy")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ex_lit_search_config_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FK87vw6p2kttreaxshoagh8nhxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-146") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK8fru5jqrjb81756xvmoh203uw")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_PRODUCT_EVENT_ID", baseTableName: "AGG_GLOBAL_TAGS", constraintName: "FK8fru5jqrjb81756xvmoh203uw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-147") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK8ic0iqi8eynbxkkwxroc6io1r")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK8ic0iqi8eynbxkkwxroc6io1r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-148") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK8r3bv1bet4qwkng62b9umxu85")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_SIGNAL", constraintName: "FK8r3bv1bet4qwkng62b9umxu85", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-149") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK9k0mrfmtad3s0gfgy7uru5v6x")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_EVDAS_ALERT_ID", baseTableName: "ARCHIVED_EVDAS_ALERT_ACTIONS", constraintName: "FK9k0mrfmtad3s0gfgy7uru5v6x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-150") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK9wophmgrppuk00kumrk81waqb")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "AGG_ALERT", constraintName: "FK9wophmgrppuk00kumrk81waqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-151") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FK9x6gb9gkqb0s4m4kmc2rbnbu8")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FK9x6gb9gkqb0s4m4kmc2rbnbu8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-152") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKakgfpbd6u2xm53cbtnadny890")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_SCA_ID", baseTableName: "ARCHIVED_SCA_ACTIONS", constraintName: "FKakgfpbd6u2xm53cbtnadny890", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-153") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKaucsybjvph30abcvy71hafod5")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCH_EVDAS_ALERTS", constraintName: "FKaucsybjvph30abcvy71hafod5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-154") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKbc9bxnvjr82pma735xaxwpt1e")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_SIGNAL", constraintName: "FKbc9bxnvjr82pma735xaxwpt1e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-155") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKbdy37vtjaj0m7xoigaclxb4on")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SHARE_WITH_GROUP_VIEW", constraintName: "FKbdy37vtjaj0m7xoigaclxb4on", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-156") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKc2c8tbtsjri2vwelqffpwsib2")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKc2c8tbtsjri2vwelqffpwsib2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-157") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKc31v2f5sfqjsgixxp1ffhxtjk")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "default_evdas_disposition_id", baseTableName: "GROUPS", constraintName: "FKc31v2f5sfqjsgixxp1ffhxtjk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-158") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKdg7m9of1e7mxdseyjpxvfwdii")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKdg7m9of1e7mxdseyjpxvfwdii", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-159") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKdgp2hsdsukq904rr3ln0yugwh")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKdgp2hsdsukq904rr3ln0yugwh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-161") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKedhgc4ecfee0ta6ixe5syha4e")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKedhgc4ecfee0ta6ixe5syha4e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-162") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKf0xb0id8sbbaribi0u1vlb407")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "default_view_instance_id", baseTableName: "default_view_mapping", constraintName: "FKf0xb0id8sbbaribi0u1vlb407", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "view_instance")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-163") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKf67kp89dp9wnb9at46cfyg4eg")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKf67kp89dp9wnb9at46cfyg4eg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-164") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKgiwl95uuujn62d7d50ay6oye2")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKgiwl95uuujn62d7d50ay6oye2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-165") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKgixjyq9jo9w2uahbvmpxbn3j0")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "SHARE_WITH_USER_VIEW", constraintName: "FKgixjyq9jo9w2uahbvmpxbn3j0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-166") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKgpnn8ylkort0al0ldjjsatkun")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_EVDAS_ALERT_ID", baseTableName: "VALIDATED_ARCH_EVDAS_ALERTS", constraintName: "FKgpnn8ylkort0al0ldjjsatkun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-167") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKgr8oc3jgwcv7ytkh2hea9ktvy")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKgr8oc3jgwcv7ytkh2hea9ktvy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-168") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKhebdw8tpyc76x8vf04mlyty8x")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKhebdw8tpyc76x8vf04mlyty8x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-169") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKhxyvly9aoqmq5w44fdkmajp2f")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKhxyvly9aoqmq5w44fdkmajp2f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-170") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKidddx5vd67thtokgpa8h3t17g")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKidddx5vd67thtokgpa8h3t17g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-171") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKiy6dx1s4p9odra4pxi597sr1s")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKiy6dx1s4p9odra4pxi597sr1s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-172") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKj3x5c42aklughf7f6acvddvk5")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKj3x5c42aklughf7f6acvddvk5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-173") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKjco7ijx982a1bsoh3xb115sjf")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "default_quant_disposition_id", baseTableName: "GROUPS", constraintName: "FKjco7ijx982a1bsoh3xb115sjf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-174") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKjfclvqbnyup3jp6ao1tawmmqe")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKjfclvqbnyup3jp6ao1tawmmqe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-175") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKjl7ycps7whx6d55nn8k2sbgdi")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_ACA_ID", baseTableName: "ARCHIVED_ACA_ACTIONS", constraintName: "FKjl7ycps7whx6d55nn8k2sbgdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-176") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKjuw50f35plt46bw5w8fv5yguy")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "ARCHIVED_LIT_ALERT_ACTIONS", constraintName: "FKjuw50f35plt46bw5w8fv5yguy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-177") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKkkp4tovjgrhx31e6ejnly4tvu")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKkkp4tovjgrhx31e6ejnly4tvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-178") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKkla5tymin5rsewsqnvh6p2o4n")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FKkla5tymin5rsewsqnvh6p2o4n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-179") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKkokn9baeqvmps0m1bmcc9wi7x")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKkokn9baeqvmps0m1bmcc9wi7x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-180") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKkqdk7pluq7eeq58ca93llsq9x")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_LIT_CASE_ALERT_TAGS", constraintName: "FKkqdk7pluq7eeq58ca93llsq9x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-181") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKkwj9luxaslft1a450w0blw0xv")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKkwj9luxaslft1a450w0blw0xv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-182") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKl11led0pu7l1g5k3ux1h0pwq7")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_LIT_ALERTS", constraintName: "FKl11led0pu7l1g5k3ux1h0pwq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-183") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKl48xrhxc36pgh6e24mg0wy87k")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "ARCHIVED_AGG_CASE_ALERT_TAGS", constraintName: "FKl48xrhxc36pgh6e24mg0wy87k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-184") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKl8pwosqr9w21qo798onrflf41")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKl8pwosqr9w21qo798onrflf41", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-185") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKleyd7fd58inli39s2hn65hlan")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKleyd7fd58inli39s2hn65hlan", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-186") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKlialwhjla8foxw2pk52iolbt2")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LITERATURE_CASE_ALERT_TAGS", constraintName: "FKlialwhjla8foxw2pk52iolbt2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-187") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKlsj8tckw1pfcf7pxfx0q4xqsk")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKlsj8tckw1pfcf7pxfx0q4xqsk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-188") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKnksubyeucaerikj09ay4ghone")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_AGG_CASE_ALERT_TAGS", constraintName: "FKnksubyeucaerikj09ay4ghone", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-189") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKo0pbdms4a7hhafpsdgorey0ik")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_ARTICLE_ID", baseTableName: "LITERAURE_GLOBAL_TAGS", constraintName: "FKo0pbdms4a7hhafpsdgorey0ik", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-191") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKoftb386ohtlewbs7o84428801")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKoftb386ohtlewbs7o84428801", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-192") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKp4w6jrqpfd4ypvab6bqyatk9g")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "VALIDATED_ARCHIVED_LIT_ALERTS", constraintName: "FKp4w6jrqpfd4ypvab6bqyatk9g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-193") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKp9mfpvd6hok6q5iu14tsanlk5")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "lit_search_config_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKp9mfpvd6hok6q5iu14tsanlk5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-194") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKq2nl1q0ibqqlvvsykkw40vwir")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKq2nl1q0ibqqlvvsykkw40vwir", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-195") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKqaxchl4ofg9634tb244aw63e5")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKqaxchl4ofg9634tb244aw63e5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-196") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKqk25jtv031p4xd9rjn5p24cse")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_SCA_ID", baseTableName: "VALIDATED_ARCHIVED_SCA", constraintName: "FKqk25jtv031p4xd9rjn5p24cse", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-197") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKqop2dkci70s0amrbl03k9tqpv")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKqop2dkci70s0amrbl03k9tqpv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-198") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKr9n8pkn4fmv9fp6fo3exb8pbd")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKr9n8pkn4fmv9fp6fo3exb8pbd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-200") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKriardutnvjukg5o5ae4i1t23o")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKriardutnvjukg5o5ae4i1t23o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-201") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKrnwq5b3qaj65u7w57lm3o7gy6")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKrnwq5b3qaj65u7w57lm3o7gy6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-202") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKruapp3pulwttybjnqt0ih5cej")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_EVDAS_ALERT_ACTIONS", constraintName: "FKruapp3pulwttybjnqt0ih5cej", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-203") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKs4oc9hkhlcen6gnfwlmmp7gwb")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FKs4oc9hkhlcen6gnfwlmmp7gwb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-204") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKs63kxguooseokrhs0j33gfwq6")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_SCA_ACTIONS", constraintName: "FKs63kxguooseokrhs0j33gfwq6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-206") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKt093cjln518o54qj1rv0u5r4r")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKt093cjln518o54qj1rv0u5r4r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-207") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKt6fm3gafi66mftqot7fu2qs9f")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_ACA", constraintName: "FKt6fm3gafi66mftqot7fu2qs9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-208") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKt6os25wyok9pdgy2suadx9vvf")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKt6os25wyok9pdgy2suadx9vvf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-209") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                foreignKeyConstraintExists(foreignKeyName: "FKtdy0p6crwvybnpn4fr5m4r547")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_ACA_ID", baseTableName: "VALIDATED_ARCHIVED_ACA", constraintName: "FKtdy0p6crwvybnpn4fr5m4r547", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-210") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKCO0Q10J5RIUJ8TJWJXOSURNX6')
        }
        dropForeignKeyConstraint(baseTableName: "GROUPS", constraintName: "FKCO0Q10J5RIUJ8TJWJXOSURNX6")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-211") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FKJYC9AQN1MNRATMB78QTS1VH2X')
        }
        dropForeignKeyConstraint(baseTableName: "VALUES_PARAMS", constraintName: "FKJYC9AQN1MNRATMB78QTS1VH2X")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-212") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_CLL_TEMPLT')
        }
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-213") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_DTAB_TEMPLT')
        }
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-214") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_CLL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-215") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_CUSTOM_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-216") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_DTAB_TEMPLT')
        }
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-217") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_NCASE_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-218") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_QUERY_EXP')
        }
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-219") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_QUERY_VALUE')
        }
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-220") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_SQL_VALUE')
        }
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-221") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_EX_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-222") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_NONCASE_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-223") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_PARAM')
        }
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-224") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_QUERY_EXP_VALUE')
        }
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-225") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_QUERY_VALUE')
        }
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-226") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_RPT_TEMPLT')
        }
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-227") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_TEMPLT')
        }
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-228") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-229") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_SQL_VALUE')
        }
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-230") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_TEMPLT_VALUE')
        }
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-231") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'HT_VALUE')
        }
        dropTable(tableName: "HT_VALUE")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-251") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ACTIVITIES', columnName: 'DETAILS')
        }

        grailsChange {
            change{
                try {
                    ActivityService activityService = ctx.activityService
                    activityService.activityUpdate(Activity, 'ACTIVITIES')
                } catch(Exception ex) {
                    println("##################### Error occurred while updating  ACTIVITIES table. #############")
                    ex.printStackTrace()
                }
            }
        }
    }


    changeSet(author: "amrendra (generated)", id: "1595253951671-252") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ACTIVITY', columnName: 'DETAILS')
        }

        grailsChange {
            change{
                try {
                    ActivityService activityService = ctx.activityService
                    activityService.activityUpdate(LiteratureActivity, 'LITERATURE_ACTIVITY')
                } catch(Exception ex) {
                    println("##################### Error occurred while updating LITERATURE_ACTIVITY table. #############")
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-350") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_group')
            }
        }
        createIndex(indexName: "idx_single_alert_group", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_GROUP_ID")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-351") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_assignedto')
            }
        }
        createIndex(indexName: "idx_single_alert_assignedto", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "ASSIGNED_TO_ID")
        }
    }


    changeSet(author: "amrendra (generated)", id: "1595253951671-305") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM ACTIVITY_TYPE  WHERE VALUE = 'TagAdded';")
            }
        }
        grailsChange {
            change {
                try {
                    String updateActivityTypeQuery = "BEGIN " +
                            "UPDATE ACTIVITY_TYPE SET VALUE = \'CategoryAdded\' WHERE VALUE = \'TagAdded\'; " +
                            "UPDATE ACTIVITY_TYPE SET VALUE = \'CategoryRemoved\' WHERE VALUE = \'TagRemoved\'; " +
                            "END; "
                    sql.execute(updateActivityTypeQuery)
                }catch(Exception ex) {
                    println("##################### Error occurred while updating ACTIVITY_TYPE table. #############")
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-254") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RULE_INFORMATION', columnName: 'TAGS')
        }
        grailsChange {
            change {
                try {
                    BusinessConfigurationService businessConfigurationService = ctx.businessConfigurationService
                    businessConfigurationService.migrateRuleInformationTags()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating Global Case table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-371") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM GLOBAL_CASE;")
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' INSERT INTO GLOBAL_CASE(VERSION , CASE_ID)
                        (SELECT  DISTINCT 0 , CASE_ID FROM SINGLE_CASE_ALERT)  ''')
                    sql.execute('''MERGE INTO SINGLE_CASE_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        GLOBAL_CASE
                                )
                                b ON ( b.CASE_ID = a.CASE_ID )
                                WHEN MATCHED THEN UPDATE SET a.GLOBAL_IDENTITY_ID = GLOBALCASEID   ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating GLOBAL_CASE table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-256") {
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.migrateGlobalCase()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating Global Case table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-372") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM GLOBAL_PRODUCT_EVENT;")
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' INSERT INTO GLOBAL_PRODUCT_EVENT(VERSION , PRODUCT_EVENT_COMB)
                        (SELECT DISTINCT 0 ,  CASE WHEN SMQ_CODE IS NOT NULL
                         THEN ( PRODUCT_ID ||  '-' || PT_CODE || '-' || SMQ_CODE)
                          ELSE (  PRODUCT_ID ||  '-' || PT_CODE || '-' || 'null')
                          END AS  colname FROM agg_alert)''')
                    sql.execute('''MERGE INTO agg_alert a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON ( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || a.smq_code )
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NOT NULL  ''')

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating GLOBAL_PRODUCT_EVENT table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-373") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM GLOBAL_PRODUCT_EVENT;")
            }
        }
        grailsChange {
            change {
                try {

                    sql.execute('''MERGE INTO agg_alert a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON ( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || 'null' )
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NULL  ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating GLOBAL_PRODUCT_EVENT table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-374") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM GLOBAL_ARTICLE;")
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' INSERT INTO GLOBAL_ARTICLE(VERSION , ARTICLE_ID)
                        (SELECT  DISTINCT 0 , ARTICLE_ID FROM LITERATURE_ALERT) ''')
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

    changeSet(author: "amrendra (generated)", id: "1595253951671-263") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'AGG_GLOBAL_TAGS')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''DELETE FROM agg_global_tags WHERE pvs_global_tag_id NOT IN (SELECT id FROM pvs_global_tag) ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while deleting AGG_GLOBAL_TAGS table. #############")
                }
            }
        }
    }


    changeSet(author: "amrendra (generated)", id: "1595253951671-264") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'LITERATURE_ALERT', columnName: 'GLOBAL_IDENTITY_ID')
        }
        grailsChange {
            change {
                try {
                    sql.execute(''' UPDATE LITERATURE_ALERT L SET GLOBAL_IDENTITY_ID = (SELECT GLOBALARTICLEID FROM GLOBAL_ARTICLE WHERE ARTICLE_ID = L.ARTICLE_ID)
                         ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating LITERATURE_ALERT table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-2651") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                columnExists(tableName: 'GROUPS', columnName: 'default_adhoc_disposition_id')
                columnExists(tableName: 'GROUPS', columnName: 'default_evdas_disposition_id')
                columnExists(tableName: 'GROUPS', columnName: 'default_lit_disposition_id')
                columnExists(tableName: 'GROUPS', columnName: 'default_quali_disposition_id')
                columnExists(tableName: 'GROUPS', columnName: 'default_quant_disposition_id')
                columnExists(tableName: 'GROUPS', columnName: 'default_disposition_id')
            }
        }
        sql("update GROUPS set default_adhoc_disposition_id = default_disposition_id where group_type = 'WORKFLOW_GROUP';")
        sql("update GROUPS set default_evdas_disposition_id = default_disposition_id where group_type = 'WORKFLOW_GROUP';")
        sql("update GROUPS set default_lit_disposition_id = default_disposition_id where group_type = 'WORKFLOW_GROUP';")
        sql("update GROUPS set default_quali_disposition_id = default_disposition_id where group_type = 'WORKFLOW_GROUP';")
        sql("update GROUPS set default_quant_disposition_id = default_disposition_id where group_type = 'WORKFLOW_GROUP';")
        dropColumn(columnName: "default_disposition_id", tableName: "GROUPS")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-270") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'DISPOSITION', columnName: 'RESET_REVIEW_PROCESS')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''UPDATE DISPOSITION SET RESET_REVIEW_PROCESS = 1 WHERE CLOSED = 1 OR REVIEW_COMPLETED = 1 OR VALIDATED_CONFIRMED = 1 ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating disposition table. #############")
                }

            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-271") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'DISPOSITION', columnName: 'RESET_REVIEW_PROCESS')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''UPDATE DISPOSITION SET RESET_REVIEW_PROCESS = 0 WHERE RESET_REVIEW_PROCESS IS NULL ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating disposition table. #############")
                }

            }
        }
    }

    if (Holders.config.validatedSignal.shareWith.enabled) {
        changeSet(author: "amrendra (generated)", id: "1595253951671-272") {
            preConditions(onFail: 'MARK_RAN') {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
            }
            try {
                sql('''
                 insert into groups(id,version,CREATED_BY,DATE_CREATED,GROUP_TYPE,IS_ACTIVE,LAST_UPDATED,MODIFIED_BY,NAME)
                 values((select max(ID)+1 from GROUPS),0,'System',Systimestamp,'USER_GROUP',1,Systimestamp,'System','All Users');
                 COMMIT;
                ''')
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }

        changeSet(author: "amrendra (generated)", id: "1595253951671-273") {
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
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }

        changeSet(author: "amrendra (generated)", id: "1595253951671-274") {
            preConditions(onFail: 'MARK_RAN') {
                sqlCheck(expectedResult: '1', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
            }
            try {
                sql('''
               INSERT INTO SHARE_WITH_GROUP_SIGNAL(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,VALIDATED_SIGNAL_ID)
                (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from VALIDATED_SIGNAL);
                 COMMIT;
                ''')
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-3310") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveEvdasConfigId.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-3311") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveConfigId.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-3312") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveLiteratureConfigId.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-333") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'Y', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EX_RCONFIG' AND column_name = 'CONFIG_ID' ;")
        }
        sql("call MOVE_Config_id();")
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "CONFIG_ID", tableName: "EX_RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-334") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'Y', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EX_EVDAS_CONFIG' AND column_name = 'CONFIG_ID' ;")
        }
        sql("call MOVE_Evdas_Config_id();")
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "CONFIG_ID", tableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-335") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'Y', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EX_LITERATURE_CONFIG' AND column_name = 'CONFIG_ID' ;")
        }
        sql("call MOVE_Literature_Config_id();")
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "CONFIG_ID", tableName: "EX_LITERATURE_CONFIG")
    }


    changeSet(author: "amrendra (generated)", id: "1595253951671-336") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveDataToArchiveTables.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-338") {
        try {
            println("Moving Old Data To Archive Tables. Please Wait...")
            sql("call MOVE_ALERT_TO_ARCHIVE();")
            println("Old Data has been moved successfully to Archive Tables")
        } catch(Exception e){
            println("Error occoured while moving the old data to Archived Table: \n"+e.printStackTrace())
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-277") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM GROUPS WHERE NAME = 'All Users'")
        }
        try {
            sql('''
                 insert into groups(id,version,CREATED_BY,DATE_CREATED,GROUP_TYPE,IS_ACTIVE,LAST_UPDATED,MODIFIED_BY,NAME)
                 values((select COALESCE(max(ID), 0)+1 from GROUPS),0,'System',Systimestamp,'USER_GROUP',1,Systimestamp,'System','All Users');
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-278") {
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
                      where grp.id = (select ID from GROUPS WHERE NAME = 'All Users')) AND ENABLED = 1);
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-279") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', '''
                      select count(*) from pvuser where id not in (
                      SELECT usr.id
                      FROM pvuser usr
                      JOIN USER_GROUP_MAPPING ug ON (usr.ID = ug.USER_ID)
                      JOIN groups grp ON grp.ID = ug.GROUP_ID
                      where grp.id = (select ID from GROUPS WHERE NAME = 'All Users')) AND ROWNUM = 1
                      ''')
        }
        try {
            sql('''
                INSERT INTO USER_GROUP_MAPPING(ID, VERSION, GROUP_ID, USER_ID)
                   WITH ID_1 AS
                        (select COALESCE(max(ID), 0) AS ID from USER_GROUP_MAPPING),
                        VERSION AS (SELECT 0 VERSION FROM DUAL),
                        GRP_ID AS (select ID from GROUPS WHERE NAME = 'All Users'),
                        PVUSERID AS (select id from pvuser where id not in (
                                SELECT usr.id FROM pvuser usr
                                JOIN USER_GROUP_MAPPING ug ON (usr.ID = ug.USER_ID)
                                JOIN groups grp ON grp.ID = ug.GROUP_ID
                                where grp.id = (select ID from GROUPS WHERE NAME = 'All Users')) AND ENABLED = 1
                   )
                SELECT ID_1.ID+ROWNUM, VERSION.VERSION, GRP_ID.ID AS GRP_ID, PVUSERID.ID AS PVUSERID FROM ID_1, VERSION, GRP_ID, PVUSERID;
                 COMMIT;
                ''')
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-280") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) from share_with_user_litr_config;")
        }
        grailsChange {
            change {
                sql.execute(
                        '''insert into share_with_user_litr_config (CONFIG_ID,SHARE_WITH_USERID,SHARE_WITH_USER_IDX) 
                                select lit.LIT_SEARCH_CONFIG_ID,lit.assigned_to_id,0 from literature_alert lit group by lit.LIT_SEARCH_CONFIG_ID,lit.assigned_to_id 
                                union 
                                select id as LIT_SEARCH_CONFIG_ID,assigned_To_Id,0 from EX_LITERATURE_CONFIG group by  id,assigned_To_Id'''
                )
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-370") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'NUM_OF_EXECUTIONS')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''UPDATE EX_LITERATURE_CONFIG SET NUM_OF_EXECUTIONS = 0 WHERE NUM_OF_EXECUTIONS IS NULL ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating literature config table. #############")
                }

            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-281") {
        grailsChange {
            change {
                try {
                    List<LiteratureConfiguration> config = LiteratureConfiguration.findAll()
                    config.each { def litConf ->
                        int noOfExecution = 0
                        List<ExecutedLiteratureConfiguration> exLitConfig = ExecutedLiteratureConfiguration.findAllByConfigId(litConf.id)
                        exLitConfig.sort { it.dateCreated }
                        exLitConfig.each { def exConfig ->
                            noOfExecution = noOfExecution +1
                            exConfig.numOfExecutions = noOfExecution
                            exConfig.save(failOnError: true, flush: true)
                        }
                    }

                } catch (Exception ex) {
                    println "##### Error Occurred while Updateing numOfExecution for Executed Literature config in liquibase change-set ####"
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-233") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'WKFL_RUL_DISPOSITIONS', columnName: 'WORKFLOW_RULE_ID')
        }
        dropColumn(columnName: "WORKFLOW_RULE_ID", tableName: "WKFL_RUL_DISPOSITIONS")
    }


    changeSet(author: "amrendra (generated)", id: "1595253951671-245") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'VALIDATED_SIGNAL' AND column_name = 'PRODUCTS' ;")
        }
        dropNotNullConstraint(columnDataType: "clob", columnName: "PRODUCTS", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-410") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'AGG_ALERT' AND column_name = UPPER('due_date') ;")
        }
        dropNotNullConstraint(columnDataType: "timestamp", columnName: "due_date", tableName: "AGG_ALERT")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-411") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'N', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = UPPER('report_history') AND column_name = UPPER('is_report_generated') ;")
        }
        dropNotNullConstraint(columnDataType: "number(1,0)", columnName: "is_report_generated", tableName: "report_history")

        dropDefaultValue(columnDataType: "number(1,0)", columnName: "is_report_generated", tableName: "report_history")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-412") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'Y', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'EX_RCONFIG' AND column_name = UPPER('missed_cases') ;")
        }
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "missed_cases", tableName: "EX_RCONFIG")

        dropDefaultValue(columnDataType: "number(1,0)", columnName: "missed_cases", tableName: "EX_RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-413") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: 'Y', "SELECT  Nullable FROM user_tab_columns " +
                    "WHERE table_name = 'RCONFIG' AND column_name = UPPER('missed_cases') ;")
        }
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "missed_cases", tableName: "RCONFIG")

        dropDefaultValue(columnDataType: "number(1,0)", columnName: "missed_cases", tableName: "RCONFIG")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-320") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'SUSPECT_PRODUCT')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''UPDATE EX_RCONFIG SET SUSPECT_PRODUCT = 0 WHERE SUSPECT_PRODUCT IS NULL ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating ex_rconfig table. #############")
                }

            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-321") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'SUSPECT_PRODUCT')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''UPDATE RCONFIG SET SUSPECT_PRODUCT = 0 WHERE SUSPECT_PRODUCT IS NULL ''')
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating rconfig table. #############")
                }

            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-4691") {
        grailsChange {
            change {
                try {
                    PvsAlertTagService pvsAlertTagService = ctx.pvsAlertTagService
                    pvsAlertTagService.importSingleAlertTags()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PvsAlertTag table. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-4701") {
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.importSingleGlobalTags()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PvsGlobalTag table For SingleCaseAlert. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-776") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM AGG_GLOBAL_TAGS;")
        }
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.migrateAggregateGlobalTags()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PvsGlobalTag table For AggregateCaseAlert. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-777") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM LITERAURE_GLOBAL_TAGS")
        }
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.migrateLiteratureGlobalTags()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PvsGlobalTag table For Literature Alert. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-4711") {
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.importAggregateGlobalTags()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PvsGlobalTag table For AggregateCaseAlert. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-4721") {
        grailsChange {
            change {
                try {
                    PvsGlobalTagService pvsGlobalTagService = ctx.pvsGlobalTagService
                    pvsGlobalTagService.importLiteratureGlobalTags()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating PvsGlobalTag table For Literature Alert. #############")
                }
            }
        }
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-4991") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "moveArchiveTags.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }

    changeSet(author: "amrendra (generated)", id: "1595253951671-5001") {
        try {
            sql("call MOVE_TAGS_ARCHIVE();")
        } catch(Exception e){
            println("Error occoured while moving the old Tags to Archived Table: \n"+e.printStackTrace())
        }
    }

    changeSet(author: "ankit (generated)", id: "1595253951672-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'attachment', columnName: 'NAME')
        }
        sql("alter table attachment modify NAME VARCHAR2(4000 CHAR);")
    }

    changeSet(author: "ankit (generated)", id: "1595253951672-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ATTACHMENT_DESCRIPTION', columnName: 'DESCRIPTION')
        }
        sql("alter table ATTACHMENT_DESCRIPTION modify DESCRIPTION VARCHAR2(4000 CHAR);")
    }

}
