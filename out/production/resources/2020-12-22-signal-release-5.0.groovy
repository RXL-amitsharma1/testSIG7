databaseChangeLog = {

    changeSet(author: "chetansharma (generated)", id: "1608626578687-1") {
        createSequence(sequenceName: "agg_on_demand_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-2") {
        createSequence(sequenceName: "agg_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-3") {
        createSequence(sequenceName: "config_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-4") {
        createSequence(sequenceName: "evdas_config_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-5") {
        createSequence(sequenceName: "evdas_exec_config_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-6") {
        createSequence(sequenceName: "evdas_on_demand_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-7") {
        createSequence(sequenceName: "evdas_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-8") {
        createSequence(sequenceName: "exec_config_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-9") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-10") {
        createSequence(sequenceName: "sca_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-11") {
        createSequence(sequenceName: "single_on_demand_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-12") {
        createTable(tableName: "ACCESS_CONTROL_GROUP") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACCESS_CONTROL_GROUPPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "LDAP_GROUP_NAME", type: "VARCHAR2(30 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(30 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-13") {
        createTable(tableName: "ACTIONS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTIONSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "comments", type: "VARCHAR2(4000 CHAR)")

            column(name: "completed_date", type: "TIMESTAMP")

            column(name: "config_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "details", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "meeting_id", type: "VARCHAR2(255 CHAR)")

            column(name: "owner_id", type: "NUMBER(19, 0)")

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "viewed", type: "NUMBER(1, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-14") {
        createTable(tableName: "ACTION_CONFIGURATIONS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTION_CONFIGURATIONSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "is_email_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-15") {
        createTable(tableName: "ACTION_TEMPLATE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTION_TEMPLATEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_json", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-16") {
        createTable(tableName: "ACTION_TYPES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTION_TYPESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-17") {
        createTable(tableName: "ACTIVITIES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTIVITIESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "client_name", type: "VARCHAR2(255 CHAR)")

            column(name: "DETAILS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)")

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "ip_address", type: "VARCHAR2(255 CHAR)")

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "performed_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "private_user_name", type: "VARCHAR2(255 CHAR)")

            column(name: "suspect_product", type: "VARCHAR2(255 CHAR)")

            column(name: "timestamp", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activities_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-18") {
        createTable(tableName: "ACTIVITY_TYPE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTIVITY_TYPEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-19") {
        createTable(tableName: "ADHOC_ALERT_ACTIONS") {
            column(name: "ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-20") {
        createTable(tableName: "ADHOC_ALERT_ACTION_TAKEN") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TAKEN", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-21") {
        createTable(tableName: "ADHOC_ALERT_COMMENTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-22") {
        createTable(tableName: "ADVANCED_FILTER") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ADVANCED_FILTERPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "criteria", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-23") {
        createTable(tableName: "AD_HOC_SIGNAL_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-24") {
        createTable(tableName: "AD_HOC_TOPIC_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-25") {
        createTable(tableName: "AEVAL_TYPE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "AEVAL_TYPEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)")

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-26") {
        createTable(tableName: "AGG_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
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

            column(name: "cum_geriatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_non_serious", type: "NUMBER(10, 0)") {
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

            column(name: "eb05age_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb05gender", type: "CLOB")

            column(name: "eb05gender_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb05str", type: "CLOB")

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95age", type: "CLOB")

            column(name: "eb95age_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb95gender", type: "CLOB")

            column(name: "eb95gender_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb95str", type: "CLOB")

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_age", type: "CLOB")

            column(name: "ebgm_age_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ebgm_gender", type: "CLOB")

            column(name: "ebgm_gender_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ebgm_str", type: "CLOB")

            column(name: "evdas_columns", type: "VARCHAR2(4000 CHAR)")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "faers_columns", type: "VARCHAR2(4000 CHAR)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "VARCHAR2(8000 CHAR)")

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

            column(name: "new_geriatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_non_serious", type: "NUMBER(10, 0)") {
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

            column(name: "prr_str05faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "prr_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_str95faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "prr_str_faers", type: "VARCHAR2(4000 CHAR)")

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

            column(name: "ror_str05faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ror_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_str95faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ror_str_faers", type: "VARCHAR2(4000 CHAR)")

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-27") {
        createTable(tableName: "AGG_ALERT_ACTIONS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-28") {
        createTable(tableName: "AGG_ALERT_IMP_EVENT_LIST") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGA_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-29") {
        createTable(tableName: "AGG_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-30") {
        createTable(tableName: "AGG_CASE_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-31") {
        createTable(tableName: "AGG_GLOBAL_TAGS") {
            column(name: "GLOBAL_PRODUCT_EVENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-32") {
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

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb05age", type: "CLOB")

            column(name: "eb05gender", type: "CLOB")

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95age", type: "CLOB")

            column(name: "eb95gender", type: "CLOB")

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_age", type: "CLOB")

            column(name: "ebgm_gender", type: "CLOB")

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-33") {
        createTable(tableName: "AGG_SIGNAL_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-34") {
        createTable(tableName: "AGG_TOPIC_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-35") {
        createTable(tableName: "ALERTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "alert_version", type: "NUMBER(19, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "detected_date", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)")

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "event_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "notes", type: "VARCHAR2(4000 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "alert_type", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_END_DATE", type: "TIMESTAMP")

            column(name: "AGG_START_DATE", type: "TIMESTAMP")

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "comment_signal_status", type: "VARCHAR2(4000 CHAR)")

            column(name: "country_of_incidence", type: "VARCHAR2(255 CHAR)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "detected_by", type: "VARCHAR2(255 CHAR)")

            column(name: "event_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "formulations", type: "VARCHAR2(255 CHAR)")

            column(name: "ha_date_closed", type: "TIMESTAMP")

            column(name: "ha_signal_status_id", type: "NUMBER(19, 0)")

            column(name: "indication", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_data_source", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_disposition_id", type: "NUMBER(19, 0)")

            column(name: "initial_due_date", type: "TIMESTAMP")

            column(name: "issue_previously_tracked", type: "NUMBER(1, 0)")

            column(name: "last_decision_date", type: "TIMESTAMP")

            column(name: "last_updated_note", type: "TIMESTAMP")

            column(name: "name", type: "VARCHAR2(4000 CHAR)")

            column(name: "note_modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "number_oficsrs", type: "NUMBER(10, 0)")

            column(name: "owner_id", type: "NUMBER(19, 0)")

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "product_selection", type: "VARCHAR2(4000 CHAR)")

            column(name: "public_alert", type: "NUMBER(1, 0)")

            column(name: "reason_for_delay", type: "VARCHAR2(255 CHAR)")

            column(name: "ref_type", type: "VARCHAR2(255 CHAR)")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "report_type", type: "VARCHAR2(255 CHAR)")

            column(name: "shared_with_id", type: "NUMBER(19, 0)")

            column(name: "study_selection", type: "VARCHAR2(4000 CHAR)")

            column(name: "topic", type: "VARCHAR2(4000 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-36") {
        createTable(tableName: "ALERT_COMMENT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_COMMENTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "VARCHAR2(255 CHAR)")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "COMMENTS", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "config_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_family", type: "VARCHAR2(255 CHAR)")

            column(name: "product_id", type: "NUMBER(10, 0)")

            column(name: "product_name", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_code", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-37") {
        createTable(tableName: "ALERT_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-38") {
        createTable(tableName: "ALERT_DOCUMENT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_DOCUMENTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "author", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "chronicle_id", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "comments", type: "VARCHAR2(255 CHAR)")

            column(name: "document_link", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "document_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "document_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "status_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "target_date", type: "TIMESTAMP")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-39") {
        createTable(tableName: "ALERT_QUERY_VALUES") {
            column(name: "ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-40") {
        createTable(tableName: "ALERT_TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALERT_TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "NUMBER(19, 0)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-41") {
        createTable(tableName: "ALLOWED_DICTIONARY_CACHE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ALLOWED_DICTIONARY_CACHEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_data", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "allowed_data_ids", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "field_level_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_product", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_cache_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-42") {
        createTable(tableName: "ARCHIVED_ACA_ACTIONS") {
            column(name: "ARCHIVED_ACA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-43") {
        createTable(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

            column(name: "cum_geriatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "cum_non_serious", type: "NUMBER(10, 0)") {
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

            column(name: "eb05age_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb05gender", type: "CLOB")

            column(name: "eb05gender_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb05str", type: "CLOB")

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95age", type: "CLOB")

            column(name: "eb95age_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb95gender", type: "CLOB")

            column(name: "eb95gender_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "eb95str", type: "CLOB")

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm_age", type: "CLOB")

            column(name: "ebgm_age_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ebgm_gender", type: "CLOB")

            column(name: "ebgm_gender_faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ebgm_str", type: "CLOB")

            column(name: "evdas_columns", type: "VARCHAR2(4000 CHAR)")

            column(name: "exec_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "faers_columns", type: "VARCHAR2(4000 CHAR)")

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "VARCHAR2(8000 CHAR)")

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

            column(name: "new_geriatric_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_interacting_count", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "new_non_serious", type: "NUMBER(10, 0)") {
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

            column(name: "prr_str05faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "prr_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_str95faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "prr_str_faers", type: "VARCHAR2(4000 CHAR)")

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

            column(name: "ror_str05faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ror_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_str95faers", type: "VARCHAR2(4000 CHAR)")

            column(name: "ror_str_faers", type: "VARCHAR2(4000 CHAR)")

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-44") {
        createTable(tableName: "ARCHIVED_AGG_CASE_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-45") {
        createTable(tableName: "ARCHIVED_EA_IMP_EVENT_LIST") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "ev_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-46") {
        createTable(tableName: "ARCHIVED_EVDAS_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

            column(name: "format", type: "VARCHAR2(8000 CHAR)")

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

            column(name: "listedness", type: "NUMBER(1, 0)")

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-47") {
        createTable(tableName: "ARCHIVED_EVDAS_ALERT_ACTIONS") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-48") {
        createTable(tableName: "ARCHIVED_IMP_EVENT_LIST") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGA_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-49") {
        createTable(tableName: "ARCHIVED_LITERATURE_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-50") {
        createTable(tableName: "ARCHIVED_LIT_ALERT_ACTIONS") {
            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-51") {
        createTable(tableName: "ARCHIVED_LIT_CASE_ALERT_TAGS") {
            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-52") {
        createTable(tableName: "ARCHIVED_SCA_ACTIONS") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-53") {
        createTable(tableName: "ARCHIVED_SCA_CON_COMIT") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_CON_COMIT", type: "CLOB")

            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-54") {
        createTable(tableName: "ARCHIVED_SCA_MED_ERR_PT_LIST") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_MED_ERROR", type: "CLOB")

            column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-55") {
        createTable(tableName: "ARCHIVED_SCA_PT") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_SCA_PT", type: "CLOB")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-56") {
        createTable(tableName: "ARCHIVED_SCA_SUSP_PROD") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRODUCT_NAME", type: "CLOB")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-57") {
        createTable(tableName: "ARCHIVED_SCA_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-58") {
        createTable(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

            column(name: "combo_flag", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

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

            column(name: "malfunction", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-59") {
        createTable(tableName: "ATTACHMENT_DESCRIPTION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ATTACHMENT_DESCRIPTIONPK")
            }

            column(name: "attachment_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-60") {
        createTable(tableName: "AUTO_SHARE_WITH_GROUP_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTO_SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "auto_share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-61") {
        createTable(tableName: "AUTO_SHARE_WITH_GROUP_ECONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTO_SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "auto_share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-62") {
        createTable(tableName: "AUTO_SHARE_WITH_USER_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTO_SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "auto_share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-63") {
        createTable(tableName: "AUTO_SHARE_WITH_USER_ECONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTO_SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "auto_share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-64") {
        createTable(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "BUSINESS_CONFIGURATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "data_source", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_global_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCTS", type: "CLOB")

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-65") {
        createTable(tableName: "CASE_COLUMN_JOIN_MAPPING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "CASE_COLUMN_JOIN_MAPPINGPK")
            }

            column(name: "COLUMN_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MAP_COLUMN_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MAP_TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-66") {
        createTable(tableName: "CASE_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "CASE_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "archived_single_alert_id", type: "NUMBER(19, 0)")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "case_version", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "change", type: "VARCHAR2(255 CHAR)")

            column(name: "config_id", type: "NUMBER(19, 0)")

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

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "follow_up_number", type: "NUMBER(10, 0)")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "product_family", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "single_alert_id", type: "NUMBER(19, 0)")

            column(name: "sub_tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tags_updated", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-67") {
        createTable(tableName: "CATEGORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "CATEGORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DEFAULT_NAME", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-68") {
        createTable(tableName: "CLL_TEMPLT") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY", type: "CLOB")

            column(name: "COLUMNS_RF_INFO_LIST_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COL_SHOW_DISTINCT", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COL_SHOW_TOTAL", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUPING_RF_INFO_LIST_ID", type: "NUMBER(19, 0)")

            column(name: "PAGE_BREAK_BY_GROUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RENAME_GROUPING", type: "CLOB")

            column(name: "RENAME_ROW_COLS", type: "CLOB")

            column(name: "ROW_COLS_RF_INFO_LIST_ID", type: "NUMBER(19, 0)")

            column(name: "SUPPRESS_COLUMN_LIST", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-69") {
        createTable(tableName: "COGNOS_REPORT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "COGNOS_REPORTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(1000 CHAR)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "URL", type: "VARCHAR2(1000 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-70") {
        createTable(tableName: "COMMENTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "COMMENTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "commented_by_id", type: "NUMBER(19, 0)")

            column(name: "content", type: "VARCHAR2(255 CHAR)")

            column(name: "input_date", type: "TIMESTAMP")

            column(name: "parent_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-71") {
        createTable(tableName: "DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-72") {
        createTable(tableName: "DELIVERIES_EMAIL_USERS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-73") {
        createTable(tableName: "DELIVERIES_RPT_FORMATS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-74") {
        createTable(tableName: "DELIVERIES_SHARED_WITHS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-75") {
        createTable(tableName: "DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-76") {
        createTable(tableName: "DISPOSITION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DISPOSITIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "abbreviation", type: "VARCHAR2(3 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "closed", type: "NUMBER(1, 0)")

            column(name: "color_code", type: "VARCHAR2(255 CHAR)")

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "notify", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reset_review_process", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_completed", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "validated_confirmed", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-77") {
        createTable(tableName: "DISPOSITION_JUSTIFICATIONS") {
            column(name: "JUSTIFICTION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")

            column(name: "dispositions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-78") {
        createTable(tableName: "DISPOSITION_RULES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DISPOSITION_RULESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "approval_required", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "incoming_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "notify", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "signal_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "target_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-79") {
        createTable(tableName: "DISPO_RULES_TOPIC_CATEGORY") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-80") {
        createTable(tableName: "DISPO_RULES_USER_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-81") {
        createTable(tableName: "DISPO_RULES_WORKFLOW_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-82") {
        createTable(tableName: "DTAB_COLUMN_MEASURE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DTAB_COLUMN_MEASUREPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMNS_RFI_LIST_ID", type: "NUMBER(19, 0)")

            column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-83") {
        createTable(tableName: "DTAB_COL_MEAS_MEASURES") {
            column(name: "DTAB_COL_MEAS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "MEASURES_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-84") {
        createTable(tableName: "DTAB_MEASURE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DTAB_MEASUREPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CUSTOM_EXPRESSION", type: "VARCHAR2(255 CHAR)")

            column(name: "FROM_DATE", type: "TIMESTAMP")

            column(name: "TO_DATE", type: "TIMESTAMP")

            column(name: "date_range_count", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PERCENTAGE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_SUBTOTALS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_AS_COLS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_ROWS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEASURE_TYPE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-85") {
        createTable(tableName: "DTAB_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ROWS_RF_INFO_LIST_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-86") {
        createTable(tableName: "DTAB_TEMPLTS_COL_MEAS") {
            column(name: "DTAB_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "COLUMN_MEASURE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-87") {
        createTable(tableName: "EMAIL_LOG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EMAIL_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "CLOB")

            column(name: "sent_on", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "sent_to", type: "VARCHAR2(4000 CHAR)")

            column(name: "subject", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-88") {
        createTable(tableName: "EMAIL_NOTIFICATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-89") {
        createTable(tableName: "ETL_SCHEDULE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ETL_SCHEDULEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DISABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_INITIAL", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "REPEAT_INTERVAL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SCHEDULE_NAME", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "START_DATETIME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-90") {
        createTable(tableName: "EVAL_REF_TYPE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVAL_REF_TYPEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(255 CHAR)")

            column(name: "DISPLAY", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-91") {
        createTable(tableName: "EVDAS_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
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

            column(name: "format", type: "VARCHAR2(8000 CHAR)")

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

            column(name: "listedness", type: "NUMBER(1, 0)")

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-92") {
        createTable(tableName: "EVDAS_ALERT_ACTIONS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-93") {
        createTable(tableName: "EVDAS_ALERT_IMP_EVENT_LIST") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_IMP_EVENTS", type: "VARCHAR2(255 CHAR)")

            column(name: "ev_imp_event_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-94") {
        createTable(tableName: "EVDAS_APPLICATION_SETTING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVDAS_APPLICATION_SETTINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CASE_LISTING_LOCKED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ERMR_LOCKED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-95") {
        createTable(tableName: "EVDAS_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "event_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "integrated_configuration_id", type: "NUMBER(19, 0)")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_resume", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-96") {
        createTable(tableName: "EVDAS_CONFIGURATION_GROUPS") {
            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-97") {
        createTable(tableName: "EVDAS_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVDAS_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-98") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-99") {
        createTable(tableName: "EVDAS_SIGNAL_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-100") {
        createTable(tableName: "EVDAS_TOPIC_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-101") {
        createTable(tableName: "EX_ALERT_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_ALERT_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-102") {
        createTable(tableName: "EX_ALERT_QUERY_VALUES") {
            column(name: "EX_ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-103") {
        createTable(tableName: "EX_CLL_TEMPLT") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-104") {
        createTable(tableName: "EX_CUSTOM_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-105") {
        createTable(tableName: "EX_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "EXECUTED_AS_OF", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-106") {
        createTable(tableName: "EX_DELIVERIES_EMAIL_USERS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-107") {
        createTable(tableName: "EX_DELIVERIES_RPT_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-108") {
        createTable(tableName: "EX_DELIVERIES_SHARED_WITHS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-109") {
        createTable(tableName: "EX_DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-110") {
        createTable(tableName: "EX_DTAB_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-111") {
        createTable(tableName: "EX_EVDAS_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "disp_counts", type: "VARCHAR2(255 CHAR)")

            column(name: "event_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "executed_query", type: "NUMBER(19, 0)")

            column(name: "executed_query_name", type: "VARCHAR2(255 CHAR)")

            column(name: "execution_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_LATEST", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_public", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)")

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-112") {
        createTable(tableName: "EX_EVDAS_CONFIG_ACTIVITIES") {
            column(name: "EX_EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-113") {
        createTable(tableName: "EX_EVDAS_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_EVDAS_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_END_DELTA", type: "NUMBER(10, 0)")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_START_DELTA", type: "NUMBER(10, 0)")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-114") {
        createTable(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_LITERATURE_CONFIGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

            column(name: "EVENT_GROUP_SELECTION", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)")

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_GROUP_SELECTION", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "workflow_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-115") {
        createTable(tableName: "EX_LITERATURE_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_LITERATURE_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "literature_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-116") {
        createTable(tableName: "EX_NCASE_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-117") {
        createTable(tableName: "EX_QUERY_EXP") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-118") {
        createTable(tableName: "EX_QUERY_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-119") {
        createTable(tableName: "EX_RCONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_alert_id", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_count_type", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_execution_id", type: "VARCHAR2(255 CHAR)")

            column(name: "QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_trigger_cases", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_trigger_days", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "apply_alert_stop_list", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "case_series_execution_status", type: "VARCHAR2(255 CHAR)")

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "CUM_CASE_SERIES_EXEC_STATUS", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "DESCRIPTION", type: "VARCHAR2(4000 CHAR)")

            column(name: "disp_counts", type: "VARCHAR2(255 CHAR)")

            column(name: "drug_classification", type: "VARCHAR2(255 CHAR)")

            column(name: "DRUG_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "EVALUATE_DATE_AS", type: "VARCHAR2(255 CHAR)")

            column(name: "evdas_date_range", type: "VARCHAR2(255 CHAR)")

            column(name: "event_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "EXCLUDE_FOLLOWUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_ALERT_DATE_RANGE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_ALERT_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "faers_case_series_id", type: "NUMBER(19, 0)")

            column(name: "faers_cum_case_series_id", type: "NUMBER(19, 0)")

            column(name: "faers_date_range", type: "VARCHAR2(255 CHAR)")

            column(name: "GROUP_BY_SMQ", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCLUDE_LOCKED_VERSION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_auto_assigned_to", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_auto_trigger", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PUBLIC", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "limit_primary_path", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "missed_cases", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_counts", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NEXT_RUN_DATE", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "on_or_after_date", type: "TIMESTAMP")

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)")

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "pvr_case_series_id", type: "NUMBER(19, 0)")

            column(name: "CUMULATIVE_CASE_SERIES_ID", type: "NUMBER(19, 0)")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_EXECUTION_STATUS", type: "VARCHAR2(255 CHAR)")

            column(name: "REPORT_ID", type: "NUMBER(19, 0)")

            column(name: "review_due_date", type: "TIMESTAMP")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "SPOTFIRE_SETTINGS", type: "CLOB")

            column(name: "STUDY_SELECTION", type: "CLOB")

            column(name: "suspect_product", type: "NUMBER(1, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-120") {
        createTable(tableName: "EX_RCONFIGS_PROD_GRP") {
            column(name: "EXCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-121") {
        createTable(tableName: "EX_SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-122") {
        createTable(tableName: "EX_STATUS") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_STATUSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_file_path", type: "VARCHAR2(255 CHAR)")

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "END_TIME", type: "NUMBER(19, 0)")

            column(name: "executed_config_id", type: "NUMBER(19, 0)")

            column(name: "execution_level", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "FREQUENCY", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "report_execution_status", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_VERSION", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "spotfire_execution_status", type: "VARCHAR2(255 CHAR)")

            column(name: "spotfire_file_name", type: "VARCHAR2(2000 CHAR)")

            column(name: "STACK_TRACE", type: "CLOB")

            column(name: "START_TIME", type: "NUMBER(19, 0)")

            column(name: "time_stampjson", type: "VARCHAR2(255 CHAR)")

            column(name: "type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-123") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-124") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-125") {
        createTable(tableName: "EX_TEMPLT_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_TEMPLT_QUERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "BLIND_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_DATE_RANGE_INFO_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "EX_QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "EX_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "FOOTER", type: "VARCHAR2(1000 CHAR)")

            column(name: "HEADER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER_DATE_RANGE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "HEADER_PRODUCT_SELECTION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRIVACY_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_LEVEL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TITLE", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-126") {
        createTable(tableName: "EX_TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-127") {
        createTable(tableName: "FILE_ATTACHMENTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "FILE_ATTACHMENTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "app_type", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)")

            column(name: "source_attachments", type: "BLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-128") {
        createTable(tableName: "GROUPS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "GROUPSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_prod", type: "CLOB")

            column(name: "auto_route_disposition_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "default_adhoc_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_evdas_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_lit_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_quali_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_quant_disposition_id", type: "NUMBER(19, 0)")

            column(name: "default_signal_disposition_id", type: "NUMBER(19, 0)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "force_justification", type: "NUMBER(1, 0)")

            column(name: "group_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_active", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "justification_text", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "selected_datasource", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-129") {
        createTable(tableName: "GRP_ALERT_DISP") {
            column(name: "GRP_ALERT_DISP_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-130") {
        createTable(tableName: "GUEST_ATTENDEE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "GUEST_ATTENDEEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-131") {
        createTable(tableName: "IMPORT_DETAIL") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "IMPORT_DETAILPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "input_identifier", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "log_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "VARCHAR2(4000 CHAR)")

            column(name: "rec_num", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-132") {
        createTable(tableName: "IMPORT_LOG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "IMPORT_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "end_time", type: "TIMESTAMP")

            column(name: "num_failed", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "num_succeeded", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "response", type: "VARCHAR2(255 CHAR)")

            column(name: "start_time", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-133") {
        createTable(tableName: "LITERATURE_ACTIVITY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_ACTIVITYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "article_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "DETAILS", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)")

            column(name: "executed_configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_email", type: "VARCHAR2(255 CHAR)")

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "performed_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "private_user_name", type: "VARCHAR2(255 CHAR)")

            column(name: "product_name", type: "VARCHAR2(255 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "timestamp", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-134") {
        createTable(tableName: "LITERATURE_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_ALERTPK")
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-135") {
        createTable(tableName: "LITERATURE_ALERT_TAGS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-136") {
        createTable(tableName: "LITERATURE_CASE_ALERT_TAGS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-137") {
        createTable(tableName: "LITERATURE_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_CONFIGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "config_selected_time_zone", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

            column(name: "event_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_resume", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NEXT_RUN_DATE", type: "TIMESTAMP")

            column(name: "num_of_executions", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "workflow_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-138") {
        createTable(tableName: "LITERATURE_DATE_RANGE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "LITERATURE_DATE_RANGEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_END_ABSOLUTE", type: "TIMESTAMP")

            column(name: "DATE_RNG_ENUM", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RNG_START_ABSOLUTE", type: "TIMESTAMP")

            column(name: "RELATIVE_DATE_RNG_VALUE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-139") {
        createTable(tableName: "LITERATURE_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-140") {
        createTable(tableName: "LITERAURE_GLOBAL_TAGS") {
            column(name: "GLOBAL_ARTICLE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-141") {
        createTable(tableName: "LIT_ALERT_ACTIONS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-142") {
        createTable(tableName: "MEDICAL_CONCEPTS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "MEDICAL_CONCEPTSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "NUMBER(1, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-143") {
        createTable(tableName: "MEETING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "MEETINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "duration", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_recurrence_meeting", type: "NUMBER(1, 0)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "linking_id", type: "VARCHAR2(255 CHAR)")

            column(name: "meeting_agenda", type: "VARCHAR2(4000 CHAR)")

            column(name: "meeting_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "meeting_minutes", type: "VARCHAR2(4000 CHAR)")

            column(name: "meeting_owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "meeting_status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "meeting_title", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "SCHEDULAR_JSON", type: "CLOB")

            column(name: "time_zone", type: "VARCHAR2(255 CHAR)")

            column(name: "topic_id", type: "NUMBER(19, 0)")

            column(name: "validated_signal_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-144") {
        createTable(tableName: "MEETING_ATTACHMENTS") {
            column(name: "MEETING_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ATTACHMENTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-145") {
        createTable(tableName: "MISC_CONFIG") {
            column(name: "KEY_1", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "VALUE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-146") {
        createTable(tableName: "NONCASE_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COL_NAME_LIST", type: "VARCHAR2(2048 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NON_CASE_SQL", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-147") {
        createTable(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES") {
            column(name: "NONCASE_SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-148") {
        createTable(tableName: "NOTIFICATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "NOTIFICATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "detail_url", type: "VARCHAR2(255 CHAR)")

            column(name: "EC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LVL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MESSAGE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "MSG_ARGS", type: "VARCHAR2(255 CHAR)")

            column(name: "NOTIFICATION_USER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-149") {
        createTable(tableName: "PARAM") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PARAMPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LOOKUP", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "VALUE", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-150") {
        createTable(tableName: "PREFERENCE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PREFERENCEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DASHBOARD_CONFIG_JSON", type: "CLOB")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "is_cumulative_alert_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_email_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "LOCALE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TIME_ZONE", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-151") {
        createTable(tableName: "PRIORITY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRIORITYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "default_priority", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "icon_class", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_order", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-152") {
        createTable(tableName: "PRODUCT_ASSIGNMENT_LOG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRODUCT_ASSIGNMENT_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "imported_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "imported_date", type: "TIMESTAMP")

            column(name: "imported_file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-153") {
        createTable(tableName: "PRODUCT_DICTIONARY_CACHE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRODUCT_DICTIONARY_CACHEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)")

            column(name: "safety_group_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-154") {
        createTable(tableName: "PRODUCT_INGREDIENT_MAPPING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRODUCT_INGREDIENT_MAPPINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "other_data_source", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "PVA_PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-155") {
        createTable(tableName: "PVDMS_APPLICATION_SETTINGS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PVDMS_APPLICATION_SETTINGSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DMS_INTEGRATION", type: "CLOB")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-156") {
        createTable(tableName: "PVS_STATE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PVS_STATEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "final_state", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)")

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-157") {
        createTable(tableName: "PVUSER") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PVUSERPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACCOUNT_EXPIRED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACCOUNT_LOCKED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "BAD_PASSWORD_ATTEMPTS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COL_ORDER", type: "VARCHAR2(2000 CHAR)")

            column(name: "COL_USER_ORDER", type: "VARCHAR2(2000 CHAR)")

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR2(255 CHAR)")

            column(name: "ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "full_name", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "OUTLOOK_ACCESS_TOKEN", type: "VARCHAR2(2000 CHAR)")

            column(name: "OUTLOOK_REFRESH_TOKEN", type: "VARCHAR2(2000 CHAR)")

            column(name: "PASSWORD_EXPIRED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PREFERENCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USERNAME", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-158") {
        createTable(tableName: "PVUSERS_ROLES") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-159") {
        createTable(tableName: "PV_CONCEPT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PV_CONCEPTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "signal_strategy_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-160") {
        createTable(tableName: "QUERY_EXP_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "OPERATOR_ID", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_FIELD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-161") {
        createTable(tableName: "QUERY_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "query_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-162") {
        createTable(tableName: "RCONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_alert_id", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_count_type", type: "VARCHAR2(255 CHAR)")

            column(name: "agg_execution_id", type: "VARCHAR2(255 CHAR)")

            column(name: "ALERT_DATA_RANGE_ID", type: "NUMBER(19, 0)")

            column(name: "ALERT_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_trigger_cases", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_trigger_days", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "apply_alert_stop_list", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "AS_OF_VERSION_DATE_DELTA", type: "NUMBER(10, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "DESCRIPTION", type: "VARCHAR2(4000 CHAR)")

            column(name: "drug_classification", type: "VARCHAR2(255 CHAR)")

            column(name: "DRUG_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "EVALUATE_DATE_AS", type: "VARCHAR2(255 CHAR)")

            column(name: "event_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "EXCLUDE_FOLLOWUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXECUTING", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_BY_SMQ", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCLUDE_LOCKED_VERSION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "integrated_configuration_id", type: "NUMBER(19, 0)")

            column(name: "is_auto_assigned_to", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_AUTO_TRIGGER", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_ENABLED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PUBLIC", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_resume", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "limit_primary_path", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "missed_cases", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NEXT_RUN_DATE", type: "TIMESTAMP")

            column(name: "NUM_OF_EXECUTIONS", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "on_or_after_date", type: "TIMESTAMP")

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "SPOTFIRE_SETTINGS", type: "CLOB")

            column(name: "STUDY_SELECTION", type: "CLOB")

            column(name: "suspect_product", type: "NUMBER(1, 0)")

            column(name: "TEMPLATE_ID", type: "NUMBER(19, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-163") {
        createTable(tableName: "RCONFIGS_PROD_GRP") {
            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-164") {
        createTable(tableName: "RCONFIG_DISPOSITION") {
            column(name: "CONFIGURATION_DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")

            column(name: "RCONFIG_DISPOSITION_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-165") {
        createTable(tableName: "ROLE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ROLEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AUTHORITY", type: "VARCHAR2(50 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-166") {
        createTable(tableName: "RPT_ERROR") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_ERRORPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "MESSAGE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_RESULT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-167") {
        createTable(tableName: "RPT_FIELD") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELDPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATA_TYPE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_FORMAT", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(255 CHAR)")

            column(name: "DIC_LEVEL", type: "NUMBER(10, 0)")

            column(name: "DIC_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FIELD_GRPNAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "FIXED_WIDTH", type: "NUMBER(10, 0)")

            column(name: "ISAUTOCOMPLETE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_TEXT", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LIST_DOMAIN_CLASS", type: "VARCHAR2(255 CHAR)")

            column(name: "LMSQL", type: "CLOB")

            column(name: "NAME", type: "VARCHAR2(128 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "POST_QUERY_PROCEDURE", type: "VARCHAR2(255 CHAR)")

            column(name: "PRE_QUERY_PROCEDURE", type: "VARCHAR2(255 CHAR)")

            column(name: "PRE_REPORT_PROCEDURE", type: "VARCHAR2(255 CHAR)")

            column(name: "QUERY_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SOURCE_COLUMN_MASTER_ID", type: "VARCHAR2(80 CHAR)")

            column(name: "TEMPLT_CLL_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_DTCOL_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_DTROW_SELECTABLE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TRANSFORM", type: "VARCHAR2(255 CHAR)")

            column(name: "WIDTH_PROPORTION_INDEX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-168") {
        createTable(tableName: "RPT_FIELD_GROUP") {
            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-169") {
        createTable(tableName: "RPT_FIELD_INFO") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELD_INFOPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADVANCED_SORTING", type: "VARCHAR2(2000 CHAR)")

            column(name: "ARGUS_NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "BLINDED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMA_SEPARATED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CUSTOM_EXPRESSION", type: "VARCHAR2(255 CHAR)")

            column(name: "DATASHEET", type: "VARCHAR2(255 CHAR)")

            column(name: "RENAME_VALUE", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FIELD_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RF_INFO_LIST_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "sort", type: "VARCHAR2(255 CHAR)")

            column(name: "SORT_LEVEL", type: "NUMBER(10, 0)")

            column(name: "STACK_ID", type: "NUMBER(10, 0)")

            column(name: "SUPPRESS_REPEATING", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RF_INFO_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-170") {
        createTable(tableName: "RPT_FIELD_INFO_LIST") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELD_INFO_LISTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-171") {
        createTable(tableName: "RPT_RESULT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_RESULTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_RESULT_DATA_ID", type: "NUMBER(19, 0)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "EX_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "FILTER_VERSION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FREQUENCY", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REASSESS_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RUN_DATE", type: "TIMESTAMP")

            column(name: "SCHEDULED_PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SEQUENCE", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOTAL_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FILTERED_VERSION_ROWS", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-172") {
        createTable(tableName: "RPT_RESULT_DATA") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_RESULT_DATAPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CROSS_TAB_SQL", type: "CLOB")

            column(name: "GTT_SQL", type: "CLOB")

            column(name: "HEADER_SQL", type: "CLOB")

            column(name: "QUERY_SQL", type: "CLOB")

            column(name: "REPORT_SQL", type: "CLOB")

            column(name: "VALUE", type: "BLOB")

            column(name: "VERSION_SQL", type: "CLOB")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-173") {
        createTable(tableName: "RPT_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_TEMPLTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CATEGORY_ID", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "factory_default", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "HASBLANKS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_PUBLIC", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(100 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "ORIG_TEMPLT_ID", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PV_USER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "REASSESS_LISTEDNESS", type: "VARCHAR2(255 CHAR)")

            column(name: "TEMPLATE_TYPE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-174") {
        createTable(tableName: "RPT_TEMPLTS_TAGS") {
            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-175") {
        createTable(tableName: "RULE_INFORMATION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RULE_INFORMATIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")

            column(name: "business_configuration_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_QUERY", type: "CLOB")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)")

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "is_break_after_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_first_time_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_single_case_alert_type", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "justification_text", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "medical_concepts", type: "VARCHAR2(255 CHAR)")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RULES", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rule_rank", type: "NUMBER(10, 0)")

            column(name: "signal", type: "VARCHAR2(255 CHAR)")

            column(name: "tags", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-176") {
        createTable(tableName: "SHARED_WITH") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SHARED_WITHPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_USER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-177") {
        createTable(tableName: "SHARE_WITH_GROUP_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-178") {
        createTable(tableName: "SHARE_WITH_GROUP_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-179") {
        createTable(tableName: "SHARE_WITH_GROUP_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-180") {
        createTable(tableName: "SHARE_WITH_GROUP_FILTER") {
            column(name: "ADVANCED_FILTER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-181") {
        createTable(tableName: "SHARE_WITH_GROUP_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-182") {
        createTable(tableName: "SHARE_WITH_GROUP_SIGNAL") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_GROUPID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-183") {
        createTable(tableName: "SHARE_WITH_GROUP_VIEW") {
            column(name: "VIEW_INSTANCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_group_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-184") {
        createTable(tableName: "SHARE_WITH_USER_ADHOC") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-185") {
        createTable(tableName: "SHARE_WITH_USER_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-186") {
        createTable(tableName: "SHARE_WITH_USER_EVDAS_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-187") {
        createTable(tableName: "SHARE_WITH_USER_FILTER") {
            column(name: "ADVANCED_FILTER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-188") {
        createTable(tableName: "SHARE_WITH_USER_LITR_CONFIG") {
            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-189") {
        createTable(tableName: "SHARE_WITH_USER_SIGNAL") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARE_WITH_USERID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-190") {
        createTable(tableName: "SHARE_WITH_USER_VIEW") {
            column(name: "VIEW_INSTANCE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")

            column(name: "share_with_user_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-191") {
        createTable(tableName: "SIGNAL_CHART") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_CHARTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CHART_DATA", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "chart_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "exec_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "topic_id", type: "NUMBER(19, 0)")

            column(name: "validated_signal_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-192") {
        createTable(tableName: "SIGNAL_LINKED_SIGNALS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LINKED_SIGNAL_ID", type: "NUMBER(19, 0)")

            column(name: "linked_signals_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-193") {
        createTable(tableName: "SIGNAL_OUTCOME") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_OUTCOMEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-194") {
        createTable(tableName: "SIGNAL_REPORT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_REPORTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "excel_report", type: "BLOB")

            column(name: "EXECUTED_ALERT_ID", type: "NUMBER(19, 0)")

            column(name: "IS_ALERT_REPORT", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_generating", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "link_url", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pdf_report", type: "BLOB")

            column(name: "report_execution_status", type: "VARCHAR2(255 CHAR)")

            column(name: "REPORT_ID", type: "NUMBER(19, 0)")

            column(name: "report_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "type_flag", type: "VARCHAR2(255 CHAR)")

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "word_report", type: "BLOB")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-195") {
        createTable(tableName: "SIGNAL_RMMs") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_RMMsPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "communication_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "country", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "due_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "email_address", type: "VARCHAR2(255 CHAR)")

            column(name: "email_sent", type: "TIMESTAMP")

            column(name: "signal_email_log_id", type: "NUMBER(19, 0)")

            column(name: "status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-196") {
        createTable(tableName: "SIGNAL_SIG_RMMS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIG_RMM_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-197") {
        createTable(tableName: "SIGNAL_SIG_STATUS_HISTORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIG_STATUS_HISTORY_ID", type: "NUMBER(19, 0)")

            column(name: "signal_status_histories_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-198") {
        createTable(tableName: "SIGNAL_STATUS_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_STATUS_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_updated", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "from_signal_status", type: "VARCHAR2(255 CHAR)")

            column(name: "performed_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "signal_status", type: "VARCHAR2(255 CHAR)")

            column(name: "status_comment", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-199") {
        createTable(tableName: "SIGNAL_STRATEGY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_STRATEGYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_selection", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP")

            column(name: "type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-200") {
        createTable(tableName: "SIGNAL_WORKFLOWRULES_GROUPS") {
            column(name: "SIGNAL_WORKFLOW_RULE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-201") {
        createTable(tableName: "SIGNAL_WORKFLOW_RULE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_WORKFLOW_RULEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-202") {
        createTable(tableName: "SIGNAL_WORKFLOW_STATE") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-203") {
        createTable(tableName: "SIGNAL_WkFL_STATE_DISPOSITIONS") {
            column(name: "SIGNAL_WORKFLOW_STATE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-204") {
        createTable(tableName: "SINGLE_ALERT_ACTIONS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-205") {
        createTable(tableName: "SINGLE_ALERT_CON_COMIT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_CON_COMIT", type: "CLOB")

            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-206") {
        createTable(tableName: "SINGLE_ALERT_MED_ERR_PT_LIST") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_MED_ERROR", type: "CLOB")

            column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-207") {
        createTable(tableName: "SINGLE_ALERT_PT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PT", type: "CLOB")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-208") {
        createTable(tableName: "SINGLE_ALERT_SUSP_PROD") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRODUCT_NAME", type: "CLOB")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-209") {
        createTable(tableName: "SINGLE_ALERT_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-210") {
        createTable(tableName: "SINGLE_CASE_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
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

            column(name: "combo_flag", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

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

            column(name: "global_identity_id", type: "NUMBER(19, 0)")

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

            column(name: "malfunction", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-211") {
        createTable(tableName: "SINGLE_CASE_ALERT_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-212") {
        createTable(tableName: "SINGLE_CASE_ALL_TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SINGLE_CASE_ALL_TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_series_id", type: "NUMBER(19, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "owner", type: "VARCHAR2(255 CHAR)")

            column(name: "tag_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tag_text", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-213") {
        createTable(tableName: "SINGLE_GLOBAL_TAGS") {
            column(name: "GLOBAL_CASE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVS_GLOBAL_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-214") {
        createTable(tableName: "SINGLE_GLOBAL_TAG_MAPPING") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_GLOBAL_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-215") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-216") {
        createTable(tableName: "SINGLE_SIGNAL_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-217") {
        createTable(tableName: "SINGLE_TOPIC_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-218") {
        createTable(tableName: "SOURCE_COLUMN_MASTER") {
            column(name: "REPORT_ITEM", type: "VARCHAR2(80 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_TYPE", type: "VARCHAR2(1 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "CONCATENATED_FIELD", type: "VARCHAR2(1 CHAR)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LM_DECODE_COLUMN", type: "VARCHAR2(40 CHAR)")

            column(name: "LM_JOIN_COLUMN", type: "VARCHAR2(40 CHAR)")

            column(name: "LM_JOIN_EQUI_OUTER", type: "VARCHAR2(1 CHAR)")

            column(name: "LM_TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)")

            column(name: "MIN_COLUMNS", type: "NUMBER(10, 0)")

            column(name: "PRIMARY_KEY_ID", type: "NUMBER(19, 0)")

            column(name: "TABLE_NAME_ATM_ID", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-219") {
        createTable(tableName: "SOURCE_TABLE_MASTER") {
            column(name: "TABLE_NAME", type: "VARCHAR2(40 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "CASE_JOIN_ORDER", type: "NUMBER(10, 0)")

            column(name: "CASE_JOIN_EQUI_OUTER", type: "VARCHAR2(1 CHAR)")

            column(name: "HAS_ENTERPRISE_ID", type: "NUMBER(10, 0)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRAFIELD", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TABLE_ALIAS", type: "VARCHAR2(10 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TABLE_TYPE", type: "VARCHAR2(1 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "VERSIONED_DATA", type: "VARCHAR2(1 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-220") {
        createTable(tableName: "SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_NAMES", type: "VARCHAR2(2048 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SELECT_FROM_STMT", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "WHERE_STMT", type: "CLOB")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-221") {
        createTable(tableName: "SQL_TEMPLTS_SQL_VALUES") {
            column(name: "SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-222") {
        createTable(tableName: "SQL_TEMPLT_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FIELD", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-223") {
        createTable(tableName: "SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-224") {
        createTable(tableName: "STRATEGY_MEDICAL_CONCEPTS") {
            column(name: "SIGNAL_STRATEGY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-225") {
        createTable(tableName: "SYSTEM_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SYSTEM_CONFIGPK")
            }

            column(name: "ENABLE_SIGNAL_WORKFLOW", type: "NUMBER(1, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-226") {
        createTable(tableName: "TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "NUMBER(19, 0)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-227") {
        createTable(tableName: "TEMPLT_QRS_QUERY_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-228") {
        createTable(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-229") {
        createTable(tableName: "TEMPLT_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TEMPLT_QUERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "BLIND_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FOOTER", type: "VARCHAR2(1000 CHAR)")

            column(name: "HEADER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER_DATE_RANGE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "HEADER_PRODUCT_SELECTION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INDX", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRIVACY_PROTECTED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_LEVEL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "query_name", type: "VARCHAR2(255 CHAR)")

            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "template_name", type: "VARCHAR2(255 CHAR)")

            column(name: "TITLE", type: "VARCHAR2(255 CHAR)")

            column(name: "TEMPLT_QUERY_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-230") {
        createTable(tableName: "TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-231") {
        createTable(tableName: "TOPIC") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TOPICPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assignment_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "initial_data_source", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "workflow_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-232") {
        createTable(tableName: "TOPIC_ACTIVITIES") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-233") {
        createTable(tableName: "TOPIC_ADHOC_ALERTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-234") {
        createTable(tableName: "TOPIC_AGG_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-235") {
        createTable(tableName: "TOPIC_CATEGORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TOPIC_CATEGORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-236") {
        createTable(tableName: "TOPIC_COMMENTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-237") {
        createTable(tableName: "TOPIC_GROUP") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-238") {
        createTable(tableName: "TOPIC_SINGLE_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-239") {
        createTable(tableName: "USER_GROUPS") {
            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-240") {
        createTable(tableName: "USER_GROUP_MAPPING") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "USER_GROUP_MAPPINGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-241") {
        createTable(tableName: "VALIDATED_ADHOC_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-242") {
        createTable(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-243") {
        createTable(tableName: "VALIDATED_ALERT_ACTIVITIES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-244") {
        createTable(tableName: "VALIDATED_ALERT_COMMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-245") {
        createTable(tableName: "VALIDATED_ALERT_DOCUMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_DOCUMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-246") {
        createTable(tableName: "VALIDATED_ARCHIVED_ACA") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_ACA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-247") {
        createTable(tableName: "VALIDATED_ARCHIVED_LIT_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_LIT_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-248") {
        createTable(tableName: "VALIDATED_ARCHIVED_SCA") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-249") {
        createTable(tableName: "VALIDATED_ARCH_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ARCHIVED_EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-250") {
        createTable(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-251") {
        createTable(tableName: "VALIDATED_LITERATURE_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-252") {
        createTable(tableName: "VALIDATED_SIGNAL") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "VALIDATED_SIGNALPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_report_end_date", type: "TIMESTAMP")

            column(name: "agg_report_start_date", type: "TIMESTAMP")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "assignment_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "comment_signal_status", type: "VARCHAR2(4000 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")

            column(name: "detected_by", type: "VARCHAR2(255 CHAR)")

            column(name: "detected_date", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "event_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENTS", type: "CLOB")

            column(name: "generic_comment", type: "CLOB")

            column(name: "ha_date_closed", type: "TIMESTAMP")

            column(name: "ha_signal_status_id", type: "NUMBER(19, 0)")

            column(name: "include_in_aggregate_report", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "initial_data_source", type: "VARCHAR2(1000)")

            column(name: "last_decision_date", type: "TIMESTAMP")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "product_group_selection", type: "VARCHAR2(8000 CHAR)")

            column(name: "PRODUCTS", type: "CLOB")

            column(name: "REASON_FOR_EVALUATION", type: "VARCHAR2(4000)")

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "signal_status", type: "VARCHAR2(255 CHAR)")

            column(name: "REPORT_PREFERENCE", type: "VARCHAR2(255 CHAR)")

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "topic", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "workflow_state", type: "VARCHAR2(255 CHAR)")

            column(name: "ws_updated", type: "TIMESTAMP")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-253") {
        createTable(tableName: "VALIDATED_SIGNAL_GROUP") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-254") {
        createTable(tableName: "VALIDATED_SIGNAL_OUTCOMES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_OUTCOME_ID", type: "NUMBER(19, 0)")

            column(name: "signal_outcomes_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-255") {
        createTable(tableName: "VALIDATED_SIGNAL_RCONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-256") {
        createTable(tableName: "VALIDATED_SINGLE_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-257") {
        createTable(tableName: "VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "VALUEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-258") {
        createTable(tableName: "VALUES_PARAMS") {
            column(name: "VALUE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "NUMBER(19, 0)")

            column(name: "PARAM_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-259") {
        createTable(tableName: "VAL_SIGNAL_TOPIC_CATEGORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-260") {
        createTable(tableName: "VS_EVAL_METHOD") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVALUATION_METHOD", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-261") {
        createTable(tableName: "VS_EVDAS_CONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-262") {
        createTable(tableName: "WORKFLOWRULES_ACTION_TEMPLATES") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TEMPLATE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-263") {
        createTable(tableName: "WORKFLOWRULES_GROUPS") {
            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "workflow_rule_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_groups_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-264") {
        createTable(tableName: "WORKFLOWRULES_SIGNAL_CATEGORY") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-265") {
        createTable(tableName: "WORK_FLOW_RULES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "WORK_FLOW_RULESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "approve_required", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "income_state_id", type: "NUMBER(19, 0)")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "notify", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "signal_rule", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "target_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-266") {
        createTable(tableName: "WORK_FLOW_VARIABLES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "WORK_FLOW_VARIABLESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-267") {
        createTable(tableName: "WkFL_RUL_DISPOSITIONS") {
            column(name: "workflow_rule_id", type: "NUMBER(19, 0)")

            column(name: "disposition_id", type: "NUMBER(19, 0)")

            column(name: "allowed_dispositions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-268") {
        createTable(tableName: "agg_alert_alert_tag") {
            column(name: "agg_alert_tags_set_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_tag_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-269") {
        createTable(tableName: "alert_stop_list") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "alert_stop_listPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activated", type: "NUMBER(1, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_deactivated", type: "TIMESTAMP")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-270") {
        createTable(tableName: "attachment") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "attachmentPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "attachment_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "content_type", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "ext", type: "VARCHAR2(255 CHAR)")

            column(name: "input_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "length", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "lnk_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "poster_class", type: "VARCHAR2(255 CHAR)")

            column(name: "poster_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reference_link", type: "VARCHAR2(4000 CHAR)")

            column(name: "reference_type", type: "VARCHAR2(255 CHAR)")

            column(name: "saved_name", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-271") {
        createTable(tableName: "attachment_link") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "attachment_linkPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reference_class", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "reference_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-272") {
        createTable(tableName: "audit_child_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "audit_child_logPK")
            }

            column(name: "audit_trail_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "new_value", type: "CLOB")

            column(name: "old_value", type: "CLOB")

            column(name: "property_name", type: "VARCHAR2(500)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-273") {
        createTable(tableName: "audit_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "audit_logPK")
            }

            column(name: "application_name", type: "VARCHAR2(255 CHAR)")

            column(name: "category", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "entity_id", type: "VARCHAR2(255 CHAR)")

            column(name: "entity_name", type: "VARCHAR2(255 CHAR)")

            column(name: "entity_value", type: "VARCHAR2(2000)")

            column(name: "fullname", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "persisted_object_version", type: "NUMBER(19, 0)")

            column(name: "property_name", type: "VARCHAR2(255 CHAR)")

            column(name: "sent_on_server", type: "NUMBER(1, 0)")

            column(name: "uri", type: "VARCHAR2(255 CHAR)")

            column(name: "username", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-274") {
        createTable(tableName: "default_view_mapping") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "default_view_mappingPK")
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-275") {
        createTable(tableName: "emerging_issue") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "emerging_issuePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "dme", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "emerging_issue", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVENT_GROUP_SELECTION", type: "VARCHAR2(8000 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "ime", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "special_monitoring", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-276") {
        createTable(tableName: "evdas_file_process_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "evdas_file_process_logPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "data_type", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "duplicate_data_handling", type: "NUMBER(10, 0)")

            column(name: "file_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_manual", type: "NUMBER(1, 0)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "processed_records", type: "NUMBER(10, 0)")

            column(name: "REASON", type: "CLOB")

            column(name: "record_end_date", type: "TIMESTAMP")

            column(name: "record_start_date", type: "TIMESTAMP")

            column(name: "saved_name", type: "VARCHAR2(255 CHAR)")

            column(name: "status", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "SUBSTANCES", type: "VARCHAR2(4000 CHAR)")

            column(name: "total_records", type: "NUMBER(10, 0)")

            column(name: "updated_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-277") {
        createTable(tableName: "evdas_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "evdas_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "archived_evdas_alert_id", type: "NUMBER(19, 0)")

            column(name: "as_of_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "change", type: "VARCHAR2(255 CHAR)")

            column(name: "config_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "evdas_alert_id", type: "NUMBER(19, 0)")

            column(name: "event_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "execution_date", type: "TIMESTAMP")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

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

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
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

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-278") {
        createTable(tableName: "ex_rconfig_activities") {
            column(name: "EX_CONFIG_ACTIVITIES_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-279") {
        createTable(tableName: "global_article") {
            column(name: "globalArticleId", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-280") {
        createTable(tableName: "global_case") {
            column(name: "globalCaseId", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "global_casePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "case_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "version_num", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-281") {
        createTable(tableName: "global_product_event") {
            column(name: "globalProductEventId", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-282") {
        createTable(tableName: "inbox_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "inbox_logPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "content", type: "VARCHAR2(8000 CHAR)")

            column(name: "created_on", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "detail_url", type: "VARCHAR2(255 CHAR)")

            column(name: "executed_config_id", type: "NUMBER(19, 0)")

            column(name: "inbox_user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_notification", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_read", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "LVL", type: "VARCHAR2(255 CHAR)")

            column(name: "message", type: "VARCHAR2(255 CHAR)")

            column(name: "message_args", type: "VARCHAR2(255 CHAR)")

            column(name: "notification_user_id", type: "NUMBER(19, 0)")

            column(name: "subject", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-283") {
        createTable(tableName: "justification") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "justificationPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "feature", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "justification", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-284") {
        createTable(tableName: "meeting_actions") {
            column(name: "meeting_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-285") {
        createTable(tableName: "meeting_activities") {
            column(name: "meeting_activities_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activity_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-286") {
        createTable(tableName: "meeting_guest_attendee") {
            column(name: "meeting_guest_attendee_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-287") {
        createTable(tableName: "meeting_pvuser") {
            column(name: "meeting_attendees_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-288") {
        createTable(tableName: "priority_disposition_config") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-289") {
        createTable(tableName: "product_event_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "product_event_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_case_alert_id", type: "NUMBER(19, 0)")

            column(name: "archived_agg_case_alert_id", type: "NUMBER(19, 0)")

            column(name: "as_of_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "change", type: "VARCHAR2(255 CHAR)")

            column(name: "config_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "cum_fatal_count", type: "NUMBER(10, 0)")

            column(name: "cum_serious_count", type: "NUMBER(10, 0)")

            column(name: "cum_spon_count", type: "NUMBER(10, 0)")

            column(name: "cum_study_count", type: "NUMBER(10, 0)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "eb05", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "eb95", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "ebgm", type: "FLOAT(24)") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "execution_date", type: "TIMESTAMP")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "new_count", type: "NUMBER(10, 0)")

            column(name: "new_fatal_count", type: "NUMBER(10, 0)")

            column(name: "new_serious_count", type: "NUMBER(10, 0)")

            column(name: "new_spon_count", type: "NUMBER(10, 0)")

            column(name: "new_study_count", type: "NUMBER(10, 0)")

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "prr_value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "sub_tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tag_name", type: "VARCHAR2(4000 CHAR)")

            column(name: "tags_updated", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-290") {
        createTable(tableName: "product_group") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "product_groupPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "classification", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-291") {
        createTable(tableName: "pvs_alert_tag") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "pvs_alert_tagPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "auto_tagged", type: "NUMBER(1, 0)")

            column(name: "created_at", type: "TIMESTAMP")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "domain", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "is_master_category", type: "NUMBER(1, 0)")

            column(name: "is_retained", type: "NUMBER(1, 0)")

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-292") {
        createTable(tableName: "pvs_global_tag") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "pvs_global_tagPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "auto_tagged", type: "NUMBER(1, 0)")

            column(name: "created_at", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "domain", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "exec_config_id", type: "NUMBER(19, 0)")

            column(name: "global_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_master_category", type: "NUMBER(1, 0)")

            column(name: "is_retained", type: "NUMBER(1, 0)")

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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-293") {
        createTable(tableName: "pvuser_safety_groups") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "safety_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-294") {
        createTable(tableName: "pvuser_user_department") {
            column(name: "user_user_departments_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_department_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-295") {
        createTable(tableName: "report_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "report_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "data_source", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "is_report_generated", type: "NUMBER(1, 0)")

            column(name: "memo_report", type: "BLOB")

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "report_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "report_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "updated_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-296") {
        createTable(tableName: "safety_group") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "safety_groupPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_prod", type: "CLOB")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-297") {
        createTable(tableName: "signal_email_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1608626578687-298") {
        createTable(tableName: "signal_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "signal_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "change", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP")

            column(name: "group_id", type: "NUMBER(19, 0)")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "justification", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "validated_signal_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-299") {
        createTable(tableName: "specialpe") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "specialpePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "special_events", type: "CLOB")

            column(name: "special_products", type: "CLOB")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-300") {
        createTable(tableName: "spotfire_notification_pvuser") {
            column(name: "recipients_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-301") {
        createTable(tableName: "spotfire_notification_query") {
            column(name: "id", type: "NUMBER(19, 0)") {
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

            column(name: "is_enabled", type: "NUMBER(1, 0)")

            column(name: "run_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "signal_parameters", type: "VARCHAR2(4000 CHAR)")

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-302") {
        createTable(tableName: "spotfire_session") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "spotfire_sessionPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR2(255 CHAR)")

            column(name: "full_name", type: "VARCHAR2(255 CHAR)")

            column(name: "timestamp", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "token", type: "VARCHAR2(255 CHAR)")

            column(name: "username", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-303") {
        createTable(tableName: "substance_frequency") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "substance_frequencyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "frequency_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "mining_frequency", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "upload_frequency", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-304") {
        createTable(tableName: "test_role") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "test_rolePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "authority", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-305") {
        createTable(tableName: "test_saml_user") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "test_saml_userPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "account_expired", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "account_locked", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR2(255 CHAR)")

            column(name: "enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "first_name", type: "VARCHAR2(255 CHAR)")

            column(name: "password", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "password_expired", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-306") {
        createTable(tableName: "test_user_role") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-307") {
        createTable(tableName: "topic_actions") {
            column(name: "topic_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-308") {
        createTable(tableName: "user_dashboard_counts") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_disp_case_counts", type: "VARCHAR2(4000 CHAR)")

            column(name: "group_disppecounts", type: "VARCHAR2(4000 CHAR)")

            column(name: "group_due_date_case_counts", type: "VARCHAR2(8000 CHAR)")

            column(name: "group_due_datepecounts", type: "VARCHAR2(8000 CHAR)")

            column(name: "user_disp_case_counts", type: "VARCHAR2(4000 CHAR)")

            column(name: "user_disppecounts", type: "VARCHAR2(4000 CHAR)")

            column(name: "user_due_date_case_counts", type: "VARCHAR2(8000 CHAR)")

            column(name: "user_due_datepecounts", type: "VARCHAR2(8000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-309") {
        createTable(tableName: "user_department") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "user_departmentPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "department_name", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-310") {
        createTable(tableName: "user_group_s") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-311") {
        createTable(tableName: "user_pin_configuration") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "user_pin_configurationPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "field_code", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_pinned", type: "NUMBER(1, 0)")

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-312") {
        createTable(tableName: "user_view_order") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "user_view_orderPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "view_instance_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "view_order", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-313") {
        createTable(tableName: "validated_signal_action_taken") {
            column(name: "validated_signal_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_taken_string", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-314") {
        createTable(tableName: "validated_signal_actions") {
            column(name: "validated_signal_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-315") {
        createTable(tableName: "view_instance") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "view_instancePK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "advanced_filter_id", type: "NUMBER(19, 0)")

            column(name: "alert_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "column_seq", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "default_value", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "filters", type: "VARCHAR2(255 CHAR)")

            column(name: "icon_seq", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "sorting", type: "VARCHAR2(255 CHAR)")

            column(name: "temp_column_seq", type: "CLOB")

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-316") {
        createTable(tableName: "work_flow_rules_actions") {
            column(name: "workflow_rule_actions_id", type: "NUMBER(19, 0)")

            column(name: "action_id", type: "NUMBER(19, 0)")

            column(name: "actions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-317") {
        createIndex(indexName: "TOPIC_ADHOC_ALERTS_IDX", tableName: "TOPIC_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, TOPIC_ID", forIndexName: "TOPIC_ADHOC_ALERTS_IDX", tableName: "TOPIC_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-318") {
        createIndex(indexName: "TOPIC_AGG_ALERTS_IDX", tableName: "TOPIC_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, TOPIC_ID", forIndexName: "TOPIC_AGG_ALERTS_IDX", tableName: "TOPIC_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-319") {
        createIndex(indexName: "TOPIC_SINGLE_ALERTS_IDX", tableName: "TOPIC_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, TOPIC_ID", forIndexName: "TOPIC_SINGLE_ALERTS_IDX", tableName: "TOPIC_SINGLE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-320") {
        createIndex(indexName: "VALIDATED_ADHOC_ALERTS_IDX", tableName: "VALIDATED_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_ADHOC_ALERTS_IDX", tableName: "VALIDATED_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-321") {
        createIndex(indexName: "VALIDATED_AGG_ALERTS_IDX", tableName: "VALIDATED_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_AGG_ALERTS_IDX", tableName: "VALIDATED_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-322") {
        createIndex(indexName: "VALIDATED_ARCHIVED_ACA_IDX", tableName: "VALIDATED_ARCHIVED_ACA", unique: "true") {
            column(name: "ARCHIVED_ACA_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_ACA_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_ARCHIVED_ACA_IDX", tableName: "VALIDATED_ARCHIVED_ACA")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-323") {
        createIndex(indexName: "VALIDATED_ARC_LIT_ALERTS_IDX", tableName: "VALIDATED_ARCHIVED_LIT_ALERTS", unique: "true") {
            column(name: "ARCHIVED_LIT_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_LIT_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_ARC_LIT_ALERTS_IDX", tableName: "VALIDATED_ARCHIVED_LIT_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-324") {
        createIndex(indexName: "VALIDATED_ARCHIVED_SCA_IDX", tableName: "VALIDATED_ARCHIVED_SCA", unique: "true") {
            column(name: "ARCHIVED_SCA_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_SCA_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_ARCHIVED_SCA_IDX", tableName: "VALIDATED_ARCHIVED_SCA")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-325") {
        createIndex(indexName: "VAL_ARCH_EVDAS_ALERTS_IDX", tableName: "VALIDATED_ARCH_EVDAS_ALERTS", unique: "true") {
            column(name: "ARCHIVED_EVDAS_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ARCHIVED_EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "VAL_ARCH_EVDAS_ALERTS_IDX", tableName: "VALIDATED_ARCH_EVDAS_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-326") {
        createIndex(indexName: "VALIDATED_EVDAS_ALERTS_IDX", tableName: "VALIDATED_EVDAS_ALERTS", unique: "true") {
            column(name: "EVDAS_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_EVDAS_ALERTS_IDX", tableName: "VALIDATED_EVDAS_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-327") {
        createIndex(indexName: "VALIDATED_LIT_ALERTS_IDX", tableName: "VALIDATED_LITERATURE_ALERTS", unique: "true") {
            column(name: "LITERATURE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "LITERATURE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_LIT_ALERTS_IDX", tableName: "VALIDATED_LITERATURE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-328") {
        createIndex(indexName: "VALIDATED_SINGLE_ALERTS_IDX", tableName: "VALIDATED_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "VALIDATED_SINGLE_ALERTS_IDX", tableName: "VALIDATED_SINGLE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-329") {
        createIndex(indexName: "pvuser_safety_groups_idx", tableName: "pvuser_safety_groups", unique: "true") {
            column(name: "user_id")

            column(name: "safety_group_id")
        }

        addPrimaryKey(columnNames: "user_id, safety_group_id", forIndexName: "pvuser_safety_groups_idx", tableName: "pvuser_safety_groups")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-330") {
        createIndex(indexName: "user_group_s_idx", tableName: "user_group_s", unique: "true") {
            column(name: "user_id")

            column(name: "group_id")
        }

        addPrimaryKey(columnNames: "user_id, group_id", forIndexName: "user_group_s_idx", tableName: "user_group_s")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-331") {
        createIndex(indexName: "IX_AGG_ALERTPK", tableName: "AGG_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "AGG_ALERTPK", forIndexName: "IX_AGG_ALERTPK", tableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-332") {
        createIndex(indexName: "IX_AGG_ON_DEMAND_ALERTPK", tableName: "AGG_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "AGG_ON_DEMAND_ALERTPK", forIndexName: "IX_AGG_ON_DEMAND_ALERTPK", tableName: "AGG_ON_DEMAND_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-333") {
        createIndex(indexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "CLL_TEMPLTPK", forIndexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-334") {
        createIndex(indexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "DTAB_TEMPLTPK", forIndexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-335") {
        createIndex(indexName: "IX_EVDAS_ALERTPK", tableName: "EVDAS_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_ALERTPK", forIndexName: "IX_EVDAS_ALERTPK", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-336") {
        createIndex(indexName: "IX_EVDAS_CONFIGPK", tableName: "EVDAS_CONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_CONFIGPK", forIndexName: "IX_EVDAS_CONFIGPK", tableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-337") {
        createIndex(indexName: "IX_EVDAS_ON_DEMAND_ALERTPK", tableName: "EVDAS_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_ON_DEMAND_ALERTPK", forIndexName: "IX_EVDAS_ON_DEMAND_ALERTPK", tableName: "EVDAS_ON_DEMAND_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-338") {
        createIndex(indexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_CLL_TEMPLTPK", forIndexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-339") {
        createIndex(indexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_CUSTOM_SQL_TEMPLTPK", forIndexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-340") {
        createIndex(indexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_DTAB_TEMPLTPK", forIndexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-341") {
        createIndex(indexName: "IX_EX_EVDAS_CONFIGPK", tableName: "EX_EVDAS_CONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_EVDAS_CONFIGPK", forIndexName: "IX_EX_EVDAS_CONFIGPK", tableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-342") {
        createIndex(indexName: "IX_EX_EVDAS_C_ACTIVITIES_PK", tableName: "EX_EVDAS_CONFIG_ACTIVITIES", unique: "true") {
            column(name: "EX_EVDAS_CONFIG_ID")

            column(name: "ACTIVITY_ID")
        }

        addPrimaryKey(columnNames: "EX_EVDAS_CONFIG_ID, ACTIVITY_ID", constraintName: "EX_EVDAS_CONFIG_ACTIVITIESPK", forIndexName: "IX_EX_EVDAS_C_ACTIVITIES_PK", tableName: "EX_EVDAS_CONFIG_ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-343") {
        createIndex(indexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_NCASE_SQL_TEMPLTPK", forIndexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-344") {
        createIndex(indexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_QUERY_EXPPK", forIndexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-345") {
        createIndex(indexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_QUERY_VALUEPK", forIndexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-346") {
        createIndex(indexName: "IX_EX_RCONFIGPK", tableName: "EX_RCONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_RCONFIGPK", forIndexName: "IX_EX_RCONFIGPK", tableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-347") {
        createIndex(indexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_SQL_VALUEPK", forIndexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-348") {
        createIndex(indexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_TEMPLT_VALUEPK", forIndexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-349") {
        createIndex(indexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG", unique: "true") {
            column(name: "KEY_1")
        }

        addPrimaryKey(columnNames: "KEY_1", constraintName: "MISC_CONFIGPK", forIndexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-350") {
        createIndex(indexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "NONCASE_SQL_TEMPLTPK", forIndexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-351") {
        createIndex(indexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "PVUSERS_ROLESPK", forIndexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-352") {
        createIndex(indexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "QUERY_EXP_VALUEPK", forIndexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-353") {
        createIndex(indexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "QUERY_VALUEPK", forIndexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-354") {
        createIndex(indexName: "IX_RCONFIGPK", tableName: "RCONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "RCONFIGPK", forIndexName: "IX_RCONFIGPK", tableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-355") {
        createIndex(indexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP", unique: "true") {
            column(name: "NAME")
        }

        addPrimaryKey(columnNames: "NAME", constraintName: "RPT_FIELD_GROUPPK", forIndexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-356") {
        createIndex(indexName: "IX_SINGLE_CASE_ALERTPK", tableName: "SINGLE_CASE_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SINGLE_CASE_ALERTPK", forIndexName: "IX_SINGLE_CASE_ALERTPK", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-357") {
        createIndex(indexName: "IX_SINGLE_ON_DEMAND_ALERTPK", tableName: "SINGLE_ON_DEMAND_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SINGLE_ON_DEMAND_ALERTPK", forIndexName: "IX_SINGLE_ON_DEMAND_ALERTPK", tableName: "SINGLE_ON_DEMAND_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-358") {
        createIndex(indexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER", unique: "true") {
            column(name: "REPORT_ITEM")
        }

        addPrimaryKey(columnNames: "REPORT_ITEM", constraintName: "SOURCE_COLUMN_MASTERPK", forIndexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-359") {
        createIndex(indexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER", unique: "true") {
            column(name: "TABLE_NAME")
        }

        addPrimaryKey(columnNames: "TABLE_NAME", constraintName: "SOURCE_TABLE_MASTERPK", forIndexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-360") {
        createIndex(indexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SQL_TEMPLTPK", forIndexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-361") {
        createIndex(indexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_TEMPLT_VALUEPK", forIndexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-362") {
        createIndex(indexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_VALUEPK", forIndexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-363") {
        createIndex(indexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "TEMPLT_VALUEPK", forIndexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-364") {
        createIndex(indexName: "IX_VALIDATED_ALERT_C_PK", tableName: "VALIDATED_ALERT_COMMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "COMMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, COMMENT_ID", constraintName: "VALIDATED_ALERT_COMMENTSPK", forIndexName: "IX_VALIDATED_ALERT_C_PK", tableName: "VALIDATED_ALERT_COMMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-365") {
        createIndex(indexName: "IX_VALIDATED_ALERT_D_PK", tableName: "VALIDATED_ALERT_DOCUMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "ALERT_DOCUMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, ALERT_DOCUMENT_ID", constraintName: "VALIDATED_ALERT_DOCUMENTSPK", forIndexName: "IX_VALIDATED_ALERT_D_PK", tableName: "VALIDATED_ALERT_DOCUMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-366") {
        createIndex(indexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities", unique: "true") {
            column(name: "EX_CONFIG_ACTIVITIES_ID")

            column(name: "ACTIVITY_ID")
        }

        addPrimaryKey(columnNames: "EX_CONFIG_ACTIVITIES_ID, ACTIVITY_ID", constraintName: "ex_rconfig_activitiesPK", forIndexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-367") {
        createIndex(indexName: "IX_test_user_rolePK", tableName: "test_user_role", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "test_user_rolePK", forIndexName: "IX_test_user_rolePK", tableName: "test_user_role")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-368") {
        createIndex(indexName: "IX_user_dashboard_countsPK", tableName: "user_dashboard_counts", unique: "true") {
            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "user_id", constraintName: "user_dashboard_countsPK", forIndexName: "IX_user_dashboard_countsPK", tableName: "user_dashboard_counts")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-369") {
        addUniqueConstraint(columnNames: "LDAP_GROUP_NAME", constraintName: "UC_AC_G_LDAP_G_N_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-370") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_ACCESS_CONTROL_GRP_NAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-371") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_ACTION_TEMPLATENAME_COL", tableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-372") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_AEVAL_TYPEVALUE_COL", tableName: "AEVAL_TYPE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-373") {
        addUniqueConstraint(columnNames: "chronicle_id", constraintName: "UC_ALERT_D_CHRONICLE_ID_COL", tableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-374") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_ALERT_TAGNAME_COL", tableName: "ALERT_TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-375") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_CATEGORYNAME_COL", tableName: "CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-376") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_DISPOSITION_DISP_NAME_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-377") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_DISPOSITIONVALUE_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-378") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_DISPOSITION_RULESNAME_COL", tableName: "DISPOSITION_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-379") {
        addUniqueConstraint(columnNames: "SCHEDULE_NAME", constraintName: "UC_ETL_SCHEDULE_NAME_COL", tableName: "ETL_SCHEDULE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-380") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_EVAL_REF_TYPENAME_COL", tableName: "EVAL_REF_TYPE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-381") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_GROUPSNAME_COL", tableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-382") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_PRIORITYDISPLAY_NAME_COL", tableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-383") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PRIORITYVALUE_COL", tableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-384") {
        addUniqueConstraint(columnNames: "group_name", constraintName: "UC_PRODUCT_GROUP_NAME_COL", tableName: "product_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-385") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PVS_STATEVALUE_COL", tableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-386") {
        addUniqueConstraint(columnNames: "USERNAME", constraintName: "UC_PVUSERUSERNAME_COL", tableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-387") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_PV_CONCEPTNAME_COL", tableName: "PV_CONCEPT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-388") {
        addUniqueConstraint(columnNames: "report_name", constraintName: "UC_R_HIST_RPT_NAME_COL", tableName: "report_history")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-389") {
        addUniqueConstraint(columnNames: "AUTHORITY", constraintName: "UC_ROLEAUTHORITY_COL", tableName: "ROLE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-390") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELDNAME_COL", tableName: "RPT_FIELD")
    }

//    changeSet(author: "chetansharma (generated)", id: "1608626578687-391") {
//        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_R_F_G_NAME_COL", tableName: "RPT_FIELD_GROUP")
//    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-392") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_SAFETY_GROUPNAME_COL", tableName: "safety_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-393") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_SIGNAL_W_S_DISP_NAME_COL", tableName: "SIGNAL_WORKFLOW_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-394") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_SIGNAL_W_S_VALUE_COL", tableName: "SIGNAL_WORKFLOW_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-395") {
        addUniqueConstraint(columnNames: "token", constraintName: "UC_SPOTFIRE_SESSIONTOKEN_COL", tableName: "spotfire_session")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-396") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_TAGNAME_COL", tableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-397") {
        addUniqueConstraint(columnNames: "authority", constraintName: "UC_TEST_ROLEAUTHORITY_COL", tableName: "test_role")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-398") {
        addUniqueConstraint(columnNames: "email", constraintName: "UC_TEST_SAML_USEREMAIL_COL", tableName: "test_saml_user")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-399") {
        addUniqueConstraint(columnNames: "username", constraintName: "UC_TEST_SAML_USERUSERNAME_COL", tableName: "test_saml_user")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-400") {
        addUniqueConstraint(columnNames: "department_name", constraintName: "UC_USER_DEPARTMENT_NAME_COL", tableName: "user_department")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-401") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_VALIDATED_SIGNALNAME_COL", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-402") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_WORK_FLOW_RULESNAME_COL", tableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-403") {
        addUniqueConstraint(columnNames: "user_id, alert_type, default_view_instance_id", constraintName: "UK210f34b0cfe5b5a551cca85a_U", tableName: "default_view_mapping")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-404") {
        addUniqueConstraint(columnNames: "alert_type, user_id, name", constraintName: "UK7285101c782d3bb42a19d76_U", tableName: "view_instance")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-405") {
        createIndex(indexName: "IX_pvs_global_tag_global_id", tableName: "pvs_global_tag") {
            column(name: "domain")

            column(name: "global_id")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-406") {
        createIndex(indexName: "single_alert_id_idx", tableName: "CASE_HISTORY") {
            column(name: "single_alert_id")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-407") {
        addForeignKeyConstraint(baseColumnNames: "PVS_GLOBAL_TAG_ID", baseTableName: "LITERAURE_GLOBAL_TAGS", constraintName: "FK1at0psmn6g5i36gg7xtivct8o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_global_tag")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-408") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FK1egn4hip9c2px7kmifks81ilj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-409") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "signal_history", constraintName: "FK1gksukhgbnjjbm6tq7sergnf3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-410") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FK1iienwc8wr9cid8iy4byqp0mn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-411") {
        addForeignKeyConstraint(baseColumnNames: "recipients_id", baseTableName: "spotfire_notification_pvuser", constraintName: "FK1lbgy2e2cp3bspkxb7ay9xsk7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "spotfire_notification_query")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-412") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_CATEGORY_ID", baseTableName: "DISPO_RULES_TOPIC_CATEGORY", constraintName: "FK1lqlnx26rn2mthsyqoajgfwtr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-413") {
        addForeignKeyConstraint(baseColumnNames: "EX_CONFIG_ACTIVITIES_ID", baseTableName: "ex_rconfig_activities", constraintName: "FK1lvnr30ngjjrcqtmugj4o7xxx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-414") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_RCONFIG", constraintName: "FK1mah9k2g3a2vog2rc5l3oyhap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-415") {
        addForeignKeyConstraint(baseColumnNames: "view_instance_id", baseTableName: "user_view_order", constraintName: "FK1o4lv2wri14x2kmor19gv99dw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "view_instance")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-416") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_GLOBAL_ID", baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FK1pciu4e4nwseetvpojr0hrh3q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALL_TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-417") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK1pvjhvvvh16fitf3f9aepcrx3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-418") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK1spm3adg2morgiavbiypkcowf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-419") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK1tom8u04lfcjhkpwup2d8arxa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-420") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "product_event_history", constraintName: "FK1vu272crmaxuyfixksks1ij0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-421") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "default_view_mapping", constraintName: "FK212psev5e8n5368qm45hxdim9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-422") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FK21hx2fdb61yflsa58y8ykjut8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-423") {
        addForeignKeyConstraint(baseColumnNames: "literature_configuration_id", baseTableName: "EX_LITERATURE_DATE_RANGE", constraintName: "FK23j99y5cju9o2h64tv6c4xq1f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-424") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_SCA", constraintName: "FK254hw7jn9lektmkp3q2rg54xd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-425") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FK25gco89k379c8yngl2ixneunj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-426") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK28ogh0ec7qa0wls7dxduf1ee1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-427") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK2alihh3mnbowd4bubkjigrt2q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-428") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ALERTS", constraintName: "FK2asghyg8brvh2hd9ihjye9d8a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-429") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_CASE_ALERT_TAGS", constraintName: "FK2jg2pehpoo65k30r8hp0ynaia", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-430") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "TAG", constraintName: "FK2jyfx1eu2cdnehuj8dqwrd6pi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-431") {
        addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK2mesh43trl6soolkyvph22b2m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-432") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK2mrmhl81t6ttn1ga2kcbl8nc5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-433") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SHARE_WITH_GROUP_FILTER", constraintName: "FK2no3an617yrnom00t5jooj7ux", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-434") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK2skfidis7c3uhb5n3pablyibp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-435") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ON_DEMAND_ALERT", constraintName: "FK2vgu56trc9yw9pxi6qn8d7upj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-436") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_ACA_ACTIONS", constraintName: "FK2vwgh7yejh3h4vol9806scbc3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-437") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "SIGNAL_CHART", constraintName: "FK2vyvpc8474sber4f5uljnhef0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-438") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "priority_disposition_config", constraintName: "FK30cc3a899ljc1c1mwuvb4gojk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-439") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK3277qdq090x04o2i21gcksi92", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-440") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FK36oexbhattm8op3hnya3tcgoy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-441") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "evdas_history", constraintName: "FK373ar2qi8f0e9w43cus7umvfm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-442") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FK37lt1se9b4d25ksatot1whkt7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-443") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "test_user_role", constraintName: "FK3g03ml77a0pr2nsyno5k23e0f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_role")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-444") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_group_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK3h89qxc4n54ltjr00fh3dfe5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-445") {
        addForeignKeyConstraint(baseColumnNames: "default_quali_disposition_id", baseTableName: "GROUPS", constraintName: "FK3hsd6jq62pwevxuymr7c47v4o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-446") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3i2ij0470itt2oyfll08nuagc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-447") {
        addForeignKeyConstraint(baseColumnNames: "MEETING_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FK3kly0ngl8jptbdmurwpdhn15e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-448") {
        addForeignKeyConstraint(baseColumnNames: "EX_DATE_RANGE_INFO_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK3ocntqp0fggudmyw65r9ghtth", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-449") {
        addForeignKeyConstraint(baseColumnNames: "imported_by_id", baseTableName: "PRODUCT_ASSIGNMENT_LOG", constraintName: "FK3r0nk6nb3se4bmm3pxj4met1q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-450") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "SIGNAL_SIG_RMMS", constraintName: "FK3r750k9lp0f4g98qjvkcn6tde", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-451") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "ARCHIVED_LIT_CASE_ALERT_TAGS", constraintName: "FK3tqo8ii2855i6p1mreo00bsms", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-452") {
        addForeignKeyConstraint(baseColumnNames: "lnk_id", baseTableName: "attachment", constraintName: "FK417wyfahv01xv8s9ypukvvpt6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment_link")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-453") {
        addForeignKeyConstraint(baseColumnNames: "business_configuration_id", baseTableName: "RULE_INFORMATION", constraintName: "FK46deuafhfurfdbrewcoueiogp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-454") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK46gb5ngvp56xrv2j0d91iuqf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-455") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FK4e8ln2cbe20xp8afdh1y7sa9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-456") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "signal_history", constraintName: "FK4he7y9hi5lwrs2d8tn16rsxew", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-457") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "priority_disposition_config", constraintName: "FK4iba2hmpj3ow92lvk695o6cmr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-458") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK4jhhis76bsysx4xm6i6ioykpc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-459") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_LIT_ALERT_ACTIONS", constraintName: "FK4k66uoy690sd2sqgt1whsv0ke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-460") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "TOPIC", constraintName: "FK4kh9et74csyh0sxnpty9v5bwf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-461") {
        addForeignKeyConstraint(baseColumnNames: "RF_INFO_LIST_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK4u8v80bl2vqgbw7bs11nm3qr2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-462") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FK4xwtik25kkjo898vri20gjn8h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-463") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK50yuip9o1motpj4seu0cxi61d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-464") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ALERT", constraintName: "FK51t9uul1wlam3muhfsdeni535", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-465") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FK530mahbw0a4gx3tet1q3w2g0l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-467") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_WORKFLOW_STATE_ID", baseTableName: "SIGNAL_WkFL_STATE_DISPOSITIONS", constraintName: "FK54cfbd7h3c167gaqlyabvg3kv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_WORKFLOW_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-468") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "SHARE_WITH_USER_FILTER", constraintName: "FK54h8y7xtiv357hvjskdlhv9nh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-469") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "TOPIC", constraintName: "FK557wy3htik1yrmw2ldt3lujx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-470") {
        addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK568vr26l2ip7hmy80k1umjtw9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-471") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FK58riu8smk8hl4hwwx89gr2v4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-472") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIVITIES", constraintName: "FK5967su0w6fjlm4na1gewgy3ts", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-473") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FK5b51h27a8nwqul2cqg130od6x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-474") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK5bcrnlc25y3lkkm5d8xsqrb83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-476") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FK5qse9kfhl8x3c7v4yl99yy5ha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-477") {
        addForeignKeyConstraint(baseColumnNames: "commented_by_id", baseTableName: "COMMENTS", constraintName: "FK5r4u2uxs7lo6xkmrbmio08003", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-478") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "view_instance", constraintName: "FK61t29rs90yvo8nrrjuy8hnrpp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-479") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "evdas_history", constraintName: "FK67y86bymxk3lnavaj4ax1fmmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-480") {
        addForeignKeyConstraint(baseColumnNames: "agg_alert_tags_set_id", baseTableName: "agg_alert_alert_tag", constraintName: "FK68guqmo0wadnf515ksx1x03sr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-481") {
        addForeignKeyConstraint(baseColumnNames: "default_adhoc_disposition_id", baseTableName: "GROUPS", constraintName: "FK6aecm7dwqlvw1fubghjflo9wk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-482") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_ID", baseTableName: "ADHOC_ALERT_ACTIONS", constraintName: "FK6b0na7al8svxgc22f6rmb8fyr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-483") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK6d5e73n3n8q05npuax4fknirw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-484") {
        addForeignKeyConstraint(baseColumnNames: "SOURCE_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FK6hfxx9xfelgaomred8dn99iig", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-485") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_ID", baseTableName: "RPT_ERROR", constraintName: "FK6m8eikuwcfty44ymm36rxip6c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-486") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FK6o7whfp7w45byxkfkg3ajgy8d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-487") {
        addForeignKeyConstraint(baseColumnNames: "config_id", baseTableName: "ACTIONS", constraintName: "FK6qnyouq2pj8noqbbgr6blr0fa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_CONFIGURATIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-488") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EVDAS_CONFIG", constraintName: "FK6sm0nv9c76tc7xx351jufvlo5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-489") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FK6vwyki46c40l23phcl2jsjovv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-490") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK6xlkiqggquuh3unim09gl7s5k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-491") {
        addForeignKeyConstraint(baseColumnNames: "topic_actions_id", baseTableName: "topic_actions", constraintName: "FK6xwerddtt6xmysbwpqm0thskw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-492") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FK71rkprlwc9p2db3fbch29cpfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-493") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FK73rmou5rxoc3xenxf07klxnvl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-494") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIONS", constraintName: "FK795v9wlhwg7olj50atxgreqp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-495") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FK7e6rwklblhyoo34rnjvek8fin", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-496") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "TOPIC", constraintName: "FK7fprf6132fra32pjukedntqyl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-497") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_RCONFIG", constraintName: "FK7jnrbgs0ar70viidp7mwns3sc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-498") {
        addForeignKeyConstraint(baseColumnNames: "auto_route_disposition_id", baseTableName: "GROUPS", constraintName: "FK7jp5xha2vlmovt0uxbyjki99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-499") {
        addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FK7lc0bubx7p9yabs83kcq36ps", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-500") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DELIVERIES_SHARED_WITHS", constraintName: "FK7ofjwn1n4eyo0b9ho6nqthf83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-501") {
        addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK7p44v7rjuor7a67e1uuhh00ki", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-502") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK7uqp71w2q1kumo5txv4soekoi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-503") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FK7uu820jqwt2ggpbwt5q1n3irg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-504") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_ADHOC", constraintName: "FK7vrrx66aotafbdx6uc56kbqvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-505") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_CASE_ID", baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FK7xpvynkdusubvmx9lc02em2i3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-506") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "SHARED_WITH", constraintName: "FK7yoqaegsqnf7fv4hmpsxc0d5u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-507") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "TOPIC", constraintName: "FK81ax335qvxywo42jwjpk2uj70", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-508") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK81iwl1ty36fpnf3hbv07ge0e7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-509") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ALERTS", constraintName: "FK81yjfpvqj3gom68hkqv4s3o0w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-510") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_VALUE_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK82nwedxu2puppkbvcn5xj3m15", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-511") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FK82sv7r2pavn03feadmrvxov2r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-512") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FK82uvw0fbs7p68ocv156ous0ho", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-513") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "RULE_INFORMATION", constraintName: "FK82y4skeo5y3n493ropl4b6ior", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-514") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "ALERTS", constraintName: "FK831h6rebhwqcu11l3bmv3m9gt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-515") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_LITR_CONFIG", constraintName: "FK86n78knlffb2f3yg8c9jhhlqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-516") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VAL_SIGNAL_TOPIC_CATEGORY", constraintName: "FK86r3dj8pbqyfn9p1d3cphcxwd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-517") {
        addForeignKeyConstraint(baseColumnNames: "ex_lit_search_config_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FK87vw6p2kttreaxshoagh8nhxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-518") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_CONFIG", constraintName: "FK88ek3tuwyqh526ly1ij7f84as", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-519") {
        addForeignKeyConstraint(baseColumnNames: "PV_USER_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK88o1kq939jqa3ujho728e9hj6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-520") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ON_DEMAND_ALERT", constraintName: "FK89l5ihyquqm5cgq9mlooxfyx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-521") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK8afup0gx565jdbopkd4b9xalg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-522") {
        addForeignKeyConstraint(baseColumnNames: "income_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK8d35ldteverucbekx4ueliwxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-523") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "topic_actions", constraintName: "FK8ewu1k8gy0uup9i3l7edt5u0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-524") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_PRODUCT_EVENT_ID", baseTableName: "AGG_GLOBAL_TAGS", constraintName: "FK8fru5jqrjb81756xvmoh203uw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-525") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_GROUP", constraintName: "FK8i6w6mah3h65hjsr0xiwwj247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-526") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK8ic0iqi8eynbxkkwxroc6io1r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-527") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FK8iktftscl552215n7hl8xr46m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-528") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_ALERT_QUERY_VALUES", constraintName: "FK8ipmwav0ngw8l0nwefligk05i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-529") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK8jqoqf0lhagmub5an0yxoma3g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-530") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK8l9hjbep49n1p5jgt1lwktf1x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-531") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "meeting_activities", constraintName: "FK8o8dfpgm739ed1u78v767vsvt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-532") {
        addForeignKeyConstraint(baseColumnNames: "ROW_COLS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK8p9endljdo1ex5s0am5h174cw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-533") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_SIGNAL", constraintName: "FK8r3bv1bet4qwkng62b9umxu85", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-534") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK8rwroyawlknqh2oxffgfixhfe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-535") {
        addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK8v8dg0fy3u8lq3l3u4c4u0mcu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-536") {
        addForeignKeyConstraint(baseColumnNames: "SQL_TEMPLT_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK8wopocxfmf15ej1v33p2twnc6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-537") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FK923jql3sdm5jk6ht1an9nn7ov", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-538") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "TOPIC", constraintName: "FK94byri2xuxp9jqxhjmvb4a8x2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-539") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK993e2h8qmb5k5m9d2oo2vh3i8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-540") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK99lpraxpe9sb9vixs14twpoi9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-541") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_EVDAS_ALERT_ID", baseTableName: "ARCHIVED_EVDAS_ALERT_ACTIONS", constraintName: "FK9k0mrfmtad3s0gfgy7uru5v6x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-542") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FK9k5fv2tt7vtl5d0rce4f46ws", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-543") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK9lmmhn29xffgygx6iaduvqmqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-544") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUPS", constraintName: "FK9nm9nx0a6hcur0207kh6k5dn9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-545") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ALERTS", constraintName: "FK9nmky66wnq1cefm2snii3dsn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-546") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FK9q2pi8025jxe8bbgtyldlp6tp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-547") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK9tk3chwgfb3ci8vloblnxd53b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-548") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "RCONFIG_DISPOSITION", constraintName: "FK9v26sfjmamkwv5h7yyh7k8vop", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-549") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK9vmxhc2kgh0i6pcw4jqcdm819", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-550") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "AGG_ALERT", constraintName: "FK9wophmgrppuk00kumrk81waqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-551") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FK9x6gb9gkqb0s4m4kmc2rbnbu8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-552") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK9xhb2aa0o52td9v65k84gpvb1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-553") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "RCONFIG", constraintName: "FKa2kbegt02usojx08bka5tlcqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-554") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FKa3cqvwnj1j7q2yi5coqlp2br4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-555") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIONS", constraintName: "FKa3rycbytmp5ea3n5dxc5hiaxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TYPES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-556") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa5gt5n32yb0s455yd36wvutkd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-557") {
        addForeignKeyConstraint(baseColumnNames: "advanced_filter_id", baseTableName: "view_instance", constraintName: "FKa5uu337r7cfmxb18x68709tf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ADVANCED_FILTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-558") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_ALERT", constraintName: "FKa87y6kvqdbxkl5gef3rft9cl0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-559") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa8rlw0012wytmrfayjlx3bw0e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-560") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKaa2wqfteyjdl906bs3t05k0s8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-561") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "test_user_role", constraintName: "FKacxggj2twirtdb9ysv9g5q2sa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_saml_user")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-562") {
        addForeignKeyConstraint(baseColumnNames: "EX_EVDAS_CONFIG_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKahisevpydu0thicdyjm49v5w9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-563") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "TOPIC_GROUP", constraintName: "FKaijamtneohj273nu1ncurw295", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-564") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKaims0i1suspmoa1et17ll8g86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-565") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_CATEGORY_ID", baseTableName: "VAL_SIGNAL_TOPIC_CATEGORY", constraintName: "FKaj0rnca6k5rfil9h32x5m6pbu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-566") {
        addForeignKeyConstraint(baseColumnNames: "GRP_ALERT_DISP_ID", baseTableName: "GRP_ALERT_DISP", constraintName: "FKajsw0c9d9gu2wlqqxvi1h1a30", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-567") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_SCA_ID", baseTableName: "ARCHIVED_SCA_ACTIONS", constraintName: "FKakgfpbd6u2xm53cbtnadny890", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-568") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FKalinqp5i92dk44sa1l98og467", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-569") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FKant7fchwn6ya4i1yvhgqt8djc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-570") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCH_EVDAS_ALERTS", constraintName: "FKaucsybjvph30abcvy71hafod5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-571") {
        addForeignKeyConstraint(baseColumnNames: "SIG_RMM_ID", baseTableName: "SIGNAL_SIG_RMMS", constraintName: "FKawq2m3d2ipw0es858bfmat8y9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_RMMs")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-572") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKayc7ooteg4uqk1y8oth5gi85w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-573") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKb004pyaimrqt9rvj6i1vcu382", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-574") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKb1h9h6gsjltr4to435axmlqba", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-575") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_STRATEGY_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FKb309drlgw2wgk40cmpq4iqqmo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-576") {
        addForeignKeyConstraint(baseColumnNames: "EX_ALERT_DATE_RANGE_ID", baseTableName: "EX_RCONFIG", constraintName: "FKb5kjpnalh2rbc3x34i1aw9073", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_ALERT_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-577") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RFI_LIST_ID", baseTableName: "DTAB_COLUMN_MEASURE", constraintName: "FKb61pwfq5cj72hri56yej80i0m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-578") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "evdas_file_process_log", constraintName: "FKb63thlw61wkcwi0uebhwttj0c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-579") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKb790uhowictwtiygfm6n9wsrs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-580") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKbaasjf1q5jdh8yb9dgo2e5c5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-581") {
        addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKbbo1adbmuh5tbw11h9j1m54rg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-582") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKbbsstpmcsh6gpgf7v3rxr5tn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-583") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_SIGNAL", constraintName: "FKbc9bxnvjr82pma735xaxwpt1e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-584") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SHARE_WITH_GROUP_VIEW", constraintName: "FKbdy37vtjaj0m7xoigaclxb4on", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-585") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "LITERATURE_CONFIG", constraintName: "FKbh94j1hyx8woow3tuhfpw771n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-586") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "RCONFIGS_PROD_GRP", constraintName: "FKbhhfbf7noaxk5m7dv8fs6vhry", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-587") {
        addForeignKeyConstraint(baseColumnNames: "SCHEDULED_PVUSER_ID", baseTableName: "RPT_RESULT", constraintName: "FKbmfelgea61vxim4d9lodgmw3r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-588") {
        addForeignKeyConstraint(baseColumnNames: "shared_with_id", baseTableName: "ALERTS", constraintName: "FKboxvx12d3pno2w2v4vqcwr3y3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-589") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKbwna09ojl0fcs9fpgv65fnc14", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-590") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LIT_ALERT_ACTIONS", constraintName: "FKbwscav6fgh7pp95mn2p7joyr6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-591") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_safety_groups", constraintName: "FKc046tqmqedqp9141fa2soxx7x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-592") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKc2c8tbtsjri2vwelqffpwsib2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-593") {
        addForeignKeyConstraint(baseColumnNames: "default_evdas_disposition_id", baseTableName: "GROUPS", constraintName: "FKc31v2f5sfqjsgixxp1ffhxtjk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-594") {
        addForeignKeyConstraint(baseColumnNames: "USER_GROUP_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FKc9jts755p0p0fmxn0ppc276aw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-595") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_WORKFLOW_RULE_ID", baseTableName: "SIGNAL_WORKFLOWRULES_GROUPS", constraintName: "FKccvy6qv59qmm3iuurrx835kxa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_WORKFLOW_RULE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-596") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKcf0w313ha302l1ybaf3vy5d5d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-597") {
        addForeignKeyConstraint(baseColumnNames: "NONCASE_SQL_TEMPLT_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKci1t92wr5cug7k5o1y9u2e9v5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-598") {
        addForeignKeyConstraint(baseColumnNames: "target_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcms27yedf7sdog3u36hlqg9iu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-599") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_group_s", constraintName: "FKcrqry31n9mka1oh51bhsn8r99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-600") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKcuf5j28ivfuw9jkg1mlthxkbn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-601") {
        addForeignKeyConstraint(baseColumnNames: "incoming_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcwneoxfjkmjy0q8v73phwc63n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-602") {
        addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKd2i9nh4hjfnjt0d3hf56kml3h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-603") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKd6owieejvfmafwh8dyofkenut", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-604") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FKd74cpexfn1jrxsjhy23g3kqjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-605") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FKd9ws3gvh9jkokbmx8qej0nt7q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-606") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FKdaa6kaucnowbyqhb0salrcwsl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-607") {
        addForeignKeyConstraint(baseColumnNames: "AUTO_SHARE_WITH_GROUPID", baseTableName: "AUTO_SHARE_WITH_GROUP_CONFIG", constraintName: "FKdfldop4meafylidvisclbly1x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-608") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKdg7m9of1e7mxdseyjpxvfwdii", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-609") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKdgp2hsdsukq904rr3ln0yugwh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-610") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "CASE_HISTORY", constraintName: "FKdhqqeepqe5rbesr70u9gw7ktm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-611") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FKdk2ms0bbvlg9wt85bl3h172c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-612") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "SIGNAL_WkFL_STATE_DISPOSITIONS", constraintName: "FKdlb58c2f4w9w8ku3pqkttavyc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-613") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKdr8517c590a2tx7da58eb0med", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-614") {
        addForeignKeyConstraint(baseColumnNames: "ATTACHMENTS_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FKdujjes8cvidp8xttfhe9yltmx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "FILE_ATTACHMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-615") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKdumjbc9ixhbyvwdgivdcqbcgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-616") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKdvuy2bffdvtxtcyne7yvm2ufl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-617") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVAL_METHOD", constraintName: "FKe0ksh1e8gfwkuuc38rs6xbavk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-618") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ACTIONS", constraintName: "FKe2e0g80hg3uajb4mb48d1k92y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-619") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FKe3ogu1iyyv0d8jxiu1m5nu1dc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-620") {
        addForeignKeyConstraint(baseColumnNames: "lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKe4a3tfxg6bd0wg0b7pvfmp0pk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-621") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ALERTS", constraintName: "FKe4dcmnjtoep5k5c638sllylqu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-622") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_RCONFIG", constraintName: "FKe9j0svwa1r5mwvgm3bf5m6eq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-623") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIVITIES", constraintName: "FKeaneople79bqyu7x49t5reovi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-624") {
        addForeignKeyConstraint(baseColumnNames: "PVS_GLOBAL_TAG_ID", baseTableName: "AGG_GLOBAL_TAGS", constraintName: "FKebu3iq0wax2046bsgqmyvre2d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_global_tag")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-625") {
        addForeignKeyConstraint(baseColumnNames: "AUTO_SHARE_WITH_USERID", baseTableName: "AUTO_SHARE_WITH_USER_ECONFIG", constraintName: "FKed73tmxex4ebs3emlubmadb4e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-626") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKedhgc4ecfee0ta6ixe5syha4e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalProductEventId", referencedTableName: "global_product_event")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-627") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "MEETING", constraintName: "FKef6s7ftgt9xb8fvpsp5lbuxrc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-628") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKehg8x540mv0qpy5plqyf7ktqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-629") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKek8566hlirn551hgcxfl9fbpl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-630") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ALERTS", constraintName: "FKel2n4td5vn9pt0x6tmxjkq1o0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-631") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKenxk6e969lecixm4isf1svn0p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-632") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKeq4e569rluxwh2bh0fil7efp8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-633") {
        addForeignKeyConstraint(baseColumnNames: "default_view_instance_id", baseTableName: "default_view_mapping", constraintName: "FKf0xb0id8sbbaribi0u1vlb407", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "view_instance")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-634") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FKf1ryj8ajrpjphnyv68dq5bll2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-635") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "RCONFIG", constraintName: "FKf4d2b2hthl4s6fvbvq0uas5av", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-636") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKf67kp89dp9wnb9at46cfyg4eg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-637") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_view_order", constraintName: "FKf7gbc9crus1wo6nyvqr00ne1b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-638") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "work_flow_rules_actions", constraintName: "FKf7jgah5sul11a45tsbuggvek4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-639") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKf8kqtv3nu1fgrdlcikfp4nfcm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-640") {
        addForeignKeyConstraint(baseColumnNames: "meeting_actions_id", baseTableName: "meeting_actions", constraintName: "FKf8ro1wl6geleojkypcnjercbp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-641") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_ALERT_ACTIONS", constraintName: "FKfajq9c8l8as3u0p8h65m522ua", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-642") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_DELIVERIES_SHARED_WITHS", constraintName: "FKffoorb58n2mlt1sjcg481t97u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-643") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "report_history", constraintName: "FKffw3i3fbs6jyf8kqojxola39s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-644") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_LITR_CONFIG", constraintName: "FKfk7kmeqebxjcxsgq733o3uh0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-645") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "user_group_s", constraintName: "FKfpfe7d6ea5bnivbe7gnc03622", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-646") {
        addForeignKeyConstraint(baseColumnNames: "AUTO_SHARE_WITH_GROUPID", baseTableName: "AUTO_SHARE_WITH_GROUP_ECONFIG", constraintName: "FKfpgkegmup3pxlgvvdv7rbn3gu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-647") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKfubdtk7gwoa8s8919uafl2jj3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-648") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKfvp89jp3dlfpaymistjk1tedp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-649") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKfwtfcguybm93tkxxgtbkf3yn0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-650") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKg049m72c3c0vmmyyg47jky5w5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC_CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-651") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FKg1yueio2f2400ouiv8y29wrji", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-652") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "signal_history", constraintName: "FKg2etf80lqk7p2chk6848imhyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-653") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_TEMPLATE_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FKg7jny08nmnya6p6b4o7fxxpyq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-654") {
        addForeignKeyConstraint(baseColumnNames: "CATEGORY_ID", baseTableName: "RPT_TEMPLT", constraintName: "FKgcan6gm4h4l0i5xg5qppa4247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-655") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "meeting_actions", constraintName: "FKgd1k2pcfls85gbq25kdko2lm6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-656") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKgiwl95uuujn62d7d50ay6oye2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-657") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "SHARE_WITH_USER_VIEW", constraintName: "FKgixjyq9jo9w2uahbvmpxbn3j0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-658") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_EVDAS_ALERT_ID", baseTableName: "VALIDATED_ARCH_EVDAS_ALERTS", constraintName: "FKgpnn8ylkort0al0ldjjsatkun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-659") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKgqh0iu7w66xqgio4jv38b5hmf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-660") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKgr8oc3jgwcv7ytkh2hea9ktvy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-661") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DOCUMENT_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FKguaxlxc330u6by2rdbpa7o15s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-662") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DATA_RANGE_ID", baseTableName: "RCONFIG", constraintName: "FKguyscui10qub13216q7omwk6r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-663") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "SIGNAL_WORKFLOWRULES_GROUPS", constraintName: "FKgybopaos7fxg7ba01jeond3do", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-664") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKhebdw8tpyc76x8vf04mlyty8x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-665") {
        addForeignKeyConstraint(baseColumnNames: "attachment_id", baseTableName: "ATTACHMENT_DESCRIPTION", constraintName: "FKhf1vmfljes8qfvmkksj0r56gr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-666") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKhfddlxpqvfaocje2iwp6umh63", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-667") {
        addForeignKeyConstraint(baseColumnNames: "guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKhgeofgkdx1rjj5s2yglnetx6y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GUEST_ATTENDEE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-668") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "LIT_ALERT_ACTIONS", constraintName: "FKhhg10vfuqjejwgwe05cngrij5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-669") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKhhjqmaurc06u0g2sd8pk0puw3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-670") {
        addForeignKeyConstraint(baseColumnNames: "meeting_owner_id", baseTableName: "MEETING", constraintName: "FKhipac92y3mafijg8snr452hkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-671") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKhjr0k31x2dckpqrnrq4u8hffp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-672") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "AGG_ALERT", constraintName: "FKhp8lobw060slxq44dpqk23aq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-673") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKhqjqjxidasmpsaf9jsa1m6fkf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-674") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "ADVANCED_FILTER", constraintName: "FKhrs1not8xdn6i0qofrejeridc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-675") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "ALERT_QUERY_VALUES", constraintName: "FKhv09gbc0ogxdov8llx7jt5961", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-676") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKhxyvly9aoqmq5w44fdkmajp2f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-677") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKhycn6c1tko90ojyv3m9q92b4b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-678") {
        addForeignKeyConstraint(baseColumnNames: "audit_trail_id", baseTableName: "audit_child_log", constraintName: "FKi2lq3l44yxa3o6afk12xj36ip", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audit_log")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-679") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FKid2pii4l2042xd6m3si3yav58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-680") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKidddx5vd67thtokgpa8h3t17g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-681") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FKig3y20w0203iai5v122a4em5e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-682") {
        addForeignKeyConstraint(baseColumnNames: "signal_strategy_id", baseTableName: "PV_CONCEPT", constraintName: "FKii9w7n9su0kelhwdy64fnoh4a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-683") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "COMMENTS", constraintName: "FKipm49nd1i0adasdsh4c4pkc88", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "COMMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-684") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "product_event_history", constraintName: "FKit1j3spl8apjv7adws38jn99a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-685") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKitf2xvh5yq2hykg0o1ff2yi6k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-686") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "SIGNAL_CHART", constraintName: "FKiwek5gp0875l735x1kt69iaq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-687") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ALERTS", constraintName: "FKixthhci8k983scn1rxj2vrtar", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-688") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_CASE_ALERT_TAGS", constraintName: "FKiy6dx1s4p9odra4pxi597sr1s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-689") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "evdas_history", constraintName: "FKj0e4kr6h7ba4h1j66a0qsvwx6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-690") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FKj0jkrus3o90ljhg1b6ebuttyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-691") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_ADHOC", constraintName: "FKj293s15eb37wlr6gn0yjjpprh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-692") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RPT_TEMPLTS_TAGS", constraintName: "FKj3em09hnn5j4v4jmtva0y7x4t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-694") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_TOPIC_CATEGORY", constraintName: "FKj5pmuktmewe6h1pbfljto0p4u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-695") {
        addForeignKeyConstraint(baseColumnNames: "default_quant_disposition_id", baseTableName: "GROUPS", constraintName: "FKjco7ijx982a1bsoh3xb115sjf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-696") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKjdbvyyl4pc5gyonimroq4q9vp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-697") {
        addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FKjf5iat84x47a3wdllmpj3qa8r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-698") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKjfclvqbnyup3jp6ao1tawmmqe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-699") {
        addForeignKeyConstraint(baseColumnNames: "AUTO_SHARE_WITH_USERID", baseTableName: "AUTO_SHARE_WITH_USER_CONFIG", constraintName: "FKjiqmo9kttr6jvf8a3wds5ux3m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-700") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_ACA_ID", baseTableName: "ARCHIVED_ACA_ACTIONS", constraintName: "FKjl7ycps7whx6d55nn8k2sbgdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-701") {
        addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "SHARED_WITH", constraintName: "FKjpnhmuynt25rwtymb8c6qbcu2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-702") {
        addForeignKeyConstraint(baseColumnNames: "SIG_STATUS_HISTORY_ID", baseTableName: "SIGNAL_SIG_STATUS_HISTORY", constraintName: "FKjpwbsduge17rl18csfbwj2rog", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STATUS_HISTORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-703") {
        addForeignKeyConstraint(baseColumnNames: "default_signal_disposition_id", baseTableName: "GROUPS", constraintName: "FKjq3undeff3wy615xd3cjxb13l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-704") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKjqg0j3akc948s35kbuqglqjqi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-705") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "ARCHIVED_LIT_ALERT_ACTIONS", constraintName: "FKjuw50f35plt46bw5w8fv5yguy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-706") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_EVDAS_CONFIG", constraintName: "FKjwr7h59vtbhc7yycaiixb3d30", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-707") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_GLOBAL_TAG_MAPPING", constraintName: "FKk07u3gx8x9cfv56t566onjcfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-708") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPOSITION_JUSTIFICATIONS", constraintName: "FKk0ppk4ji9vj7qa04043tq4jta", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-709") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKk2v2qa969crmd21cb8ey8eykl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-710") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUPS", constraintName: "FKk93ggugty4vt6eff2xct44qvs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-711") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKkdxec3jbhkxo588a4kpg4h9l4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-712") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKkkp4tovjgrhx31e6ejnly4tvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-713") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "LITERATURE_HISTORY", constraintName: "FKkla5tymin5rsewsqnvh6p2o4n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-714") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKkokn9baeqvmps0m1bmcc9wi7x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-715") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_LIT_CASE_ALERT_TAGS", constraintName: "FKkqdk7pluq7eeq58ca93llsq9x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-716") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_RCONFIG", constraintName: "FKktk446ckoiyq9ccb0k4k1fa0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-717") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKkwhfmdea1iw8ad05voiu1c88x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-718") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKkwj9luxaslft1a450w0blw0xv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-719") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_LIT_ALERTS", constraintName: "FKl11led0pu7l1g5k3ux1h0pwq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-720") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "ARCHIVED_AGG_CASE_ALERT_TAGS", constraintName: "FKl48xrhxc36pgh6e24mg0wy87k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-721") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKl5ki9ms4pq7oyr4wrne2wejf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-722") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "evdas_history", constraintName: "FKl751la1u7svv60tmydb5q6rfo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-723") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKl8pwosqr9w21qo798onrflf41", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-724") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKleyd7fd58inli39s2hn65hlan", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-725") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "RULE_INFORMATION", constraintName: "FKlfungwscmq9bwj56xlirfguy1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-726") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKlhyif0bp06538q34cbg7o6ni", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-727") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LITERATURE_CASE_ALERT_TAGS", constraintName: "FKlialwhjla8foxw2pk52iolbt2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-728") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_actions_id", baseTableName: "validated_signal_actions", constraintName: "FKlqeb869hd13sihhd6a99xq2he", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-729") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "ACTIVITIES", constraintName: "FKls2t8mye4vopk1sjq1tgy9xoc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-730") {
        addForeignKeyConstraint(baseColumnNames: "initial_disposition_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKlsj8tckw1pfcf7pxfx0q4xqsk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-731") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "signal_history", constraintName: "FKludcb63lf92hniiym7xpdxhfa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-732") {
        addForeignKeyConstraint(baseColumnNames: "meeting_activities_id", baseTableName: "meeting_activities", constraintName: "FKlxb7shjcemeke6qig312nemnv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-733") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_DATA_ID", baseTableName: "RPT_RESULT", constraintName: "FKly7i8dpff5f3c218d99g8tcyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT_DATA")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-734") {
        addForeignKeyConstraint(baseColumnNames: "meeting_guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKm139ii2baukit0klasjx4oglf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-735") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKm2082jiexg6r0a70a2r34utlj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-736") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "RPT_FIELD", constraintName: "FKmahmpsyseeqtjochaf36fxyap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-737") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKmci0hn62phky4qlig0uy8walr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-738") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIVITIES", constraintName: "FKmeoolraf0sivqb157mg1xxxhn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-739") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_ERROR", constraintName: "FKmkyr5aidjfp14wd8ytgv48xdn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-740") {
        addForeignKeyConstraint(baseColumnNames: "COLUMN_MEASURE_ID", baseTableName: "DTAB_TEMPLTS_COL_MEAS", constraintName: "FKmop226wsllt5fkw8ldhbio60u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_COLUMN_MEASURE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-741") {
        addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_COL_MEAS_MEASURES", constraintName: "FKmowrqjxvrwjdeg7wh33rdsi7e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_MEASURE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-742") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKmq50wqqf7s1y3ec4mdllb6ueu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-743") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKmv3t9bke7ovuq4xhntvupq5b4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-744") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ON_DEMAND_ALERT", constraintName: "FKmvf233chim9ujyk92n0fjedw4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-745") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKmw7ferd2w1sq8d034q2a2dare", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-746") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKmx3gs9bdqe5we8t8dyfwlnu57", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-747") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn52svyiofwkw5lpse0bi7tiup", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-748") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn6ofry9re33lb76h4npt3cjr0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-749") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKn6vycx5pad3o4yv5lqgyg9b0k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-750") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKncksxdxhk9k1wg170uk6hxo01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-751") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKneg18gip2c5vrkf5o5d6hjvp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-752") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKnf31xnc5tocj5toxf64f4n5bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-753") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FKnfgumbuu5ryminkxap7nlgc5p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-754") {
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVUSER", constraintName: "FKnjdqpgxtqa9kv92hc0h5a08nh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PREFERENCE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-755") {
        addForeignKeyConstraint(baseColumnNames: "PVS_ALERT_TAG_ID", baseTableName: "ARCHIVED_AGG_CASE_ALERT_TAGS", constraintName: "FKnksubyeucaerikj09ay4ghone", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_alert_tag")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-756") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "AGG_ALERT", constraintName: "FKnpwnuktj5h2nepfrniywuihe2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-757") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "meeting_pvuser", constraintName: "FKnrjk7qtd13c4vbk8cf2fng3c2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-758") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKnu30tvrow0gcfq8mug780icdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-759") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "RCONFIG", constraintName: "FKnwf5n98xlybm9cmlufdikkb31", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-760") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FKnwuo5xfk46w6axjqsd3so9ta4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-761") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "RCONFIG", constraintName: "FKnxr5jgx0rmqc0qwpvfb1hxedb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-762") {
        addForeignKeyConstraint(baseColumnNames: "GLOBAL_ARTICLE_ID", baseTableName: "LITERAURE_GLOBAL_TAGS", constraintName: "FKo0pbdms4a7hhafpsdgorey0ik", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalArticleId", referencedTableName: "global_article")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-763") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FKo3yoly6j5mslepff4epm8i3un", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-764") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "EVDAS_ALERT_ACTIONS", constraintName: "FKo4mw5tnngdjph3v8iouiqmkt4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-765") {
        addForeignKeyConstraint(baseColumnNames: "executed_configuration_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKoamgni17duaoqjg3twyy362ue", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-766") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_GROUPID", baseTableName: "SHARE_WITH_GROUP_EVDAS_CONFIG", constraintName: "FKob80eqdx25087jypm0j5rehhh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-767") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SIGNAL_RMMs", constraintName: "FKoberuc40mjb3128ejdorbau0e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-768") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKodukke9wren1g3gjo55qi9tgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-769") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKoftb386ohtlewbs7o84428801", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-770") {
        addForeignKeyConstraint(baseColumnNames: "user_department_id", baseTableName: "pvuser_user_department", constraintName: "FKohxfvcmtidnanlyvlj3qq9hyt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_department")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-771") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKop4ektpb6ulll4ubcyvxd2352", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-772") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKopm5twbskg380tovcrs4r2gxs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-773") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "product_event_history", constraintName: "FKoqw80p3b38v6stbo0ic2u08vf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-774") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKorl7uf55p01t8h6cb6jvuc4r0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-775") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_DOCUMENT", constraintName: "FKou8nl9rcuc35kn5yuucm1jfso", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-776") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "SIGNAL_RMMs", constraintName: "FKow3p31b4b79hwuuy57jouk2jr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-777") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKoxf0kmlxo6jbssodqw9a20l72", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-778") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_LIT_ALERT_ID", baseTableName: "VALIDATED_ARCHIVED_LIT_ALERTS", constraintName: "FKp4w6jrqpfd4ypvab6bqyatk9g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_LITERATURE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-779") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKp6qbm114tnjnrpkfb5lpvp82k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ROLE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-780") {
        addForeignKeyConstraint(baseColumnNames: "lit_search_config_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKp9mfpvd6hok6q5iu14tsanlk5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-781") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKpaayw5smfn63cqc92ncfgtcwk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-782") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_ALERT", constraintName: "FKpb61fes9spi2lu1n9k4oq9m05", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-783") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIVITIES", constraintName: "FKpb7op1gsmnueani37t07swp18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-784") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "signal_history", constraintName: "FKpc3meb0lkklinhdq8yvhcc094", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-785") {
        addForeignKeyConstraint(baseColumnNames: "SHARE_WITH_USERID", baseTableName: "SHARE_WITH_USER_CONFIG", constraintName: "FKpcpkomkegxwykoq2am8py9ayt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-786") {
        addForeignKeyConstraint(baseColumnNames: "signal_email_log_id", baseTableName: "SIGNAL_RMMs", constraintName: "FKpi5e4cypnqf6yihkdpyso1f2c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "signal_email_log")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-787") {
        addForeignKeyConstraint(baseColumnNames: "workflow_group_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKpigynbh2vwpt1xx49uypi3ddo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-788") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "EX_RCONFIGS_PROD_GRP", constraintName: "FKpiydv7gsh3jat1r6upba3jfsl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-789") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "AGG_ALERT", constraintName: "FKpj612eglsaa2cqi8ds4c3hs3c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-790") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FKpjt02j3shpuy0tenek9p1ol40", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-791") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "MEETING", constraintName: "FKpkleruks8op5rxe14p0u28dun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-792") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FKpr2qa0uljoummmnormrftubha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-793") {
        addForeignKeyConstraint(baseColumnNames: "CONFIG_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKptphnx7oype1403pwd2wk9963", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-794") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_STATUS", constraintName: "FKptxxpv688i41soevklto2001n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-795") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKpw1bhalfblwu0e31df1oib0cr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-796") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FKq24b4km13myw6pxki435bulke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-797") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKq2nl1q0ibqqlvvsykkw40vwir", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-798") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "validated_signal_actions", constraintName: "FKq4785d5esdy86x6prso7o75el", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-799") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKq8lv3dxpesh2l167udqs606vn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-800") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "ex_rconfig_activities", constraintName: "FKqaijpt3i6wpk4o7f4j9iofrqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-801") {
        addForeignKeyConstraint(baseColumnNames: "global_identity_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKqaxchl4ofg9634tb244aw63e5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "globalCaseId", referencedTableName: "global_case")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-802") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "GRP_ALERT_DISP", constraintName: "FKqeqpmqe77w39we8m3l33b48kt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-803") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ADHOC_ALERT_ACTIONS", constraintName: "FKqf474sj5dkn95xc433napf95b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-804") {
        addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "CASE_HISTORY", constraintName: "FKqh491q7pp8sjet2ep7jy7ilda", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-805") {
        addForeignKeyConstraint(baseColumnNames: "meeting_attendees_id", baseTableName: "meeting_pvuser", constraintName: "FKqiltr5xajp4m5fu0je6set5g3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-806") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKqj7ub0xptnbuhouvowxamuj26", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-807") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_SCA_ID", baseTableName: "VALIDATED_ARCHIVED_SCA", constraintName: "FKqk25jtv031p4xd9rjn5p24cse", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-808") {
        addForeignKeyConstraint(baseColumnNames: "log_id", baseTableName: "IMPORT_DETAIL", constraintName: "FKqkcg7dewuxqjklxa4xuo3yb6w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "IMPORT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-809") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKqop2dkci70s0amrbl03k9tqpv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-810") {
        addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "CASE_HISTORY", constraintName: "FKqqc5icyo2ky7wkegjktf5inwd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-811") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKqry992h07weddj9bprvrhjb3j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-812") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKqtymlypfnndkl92xpmlyn6wgi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-813") {
        addForeignKeyConstraint(baseColumnNames: "alert_tag_id", baseTableName: "agg_alert_alert_tag", constraintName: "FKqu8ugqs2k5pihokc4dv3u2a18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-814") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKr2ep7rrgudpgn2aeip674gffo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-815") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKr2gmw2pc2lgkyxy708wuv8u5b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-816") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ALERTS", constraintName: "FKr339y18wo70qkphso8k28p4n5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-817") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FKr4dvi6dx2akkxj6sg1edfu4y8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-818") {
        addForeignKeyConstraint(baseColumnNames: "product_dictionary_cache_id", baseTableName: "ALLOWED_DICTIONARY_CACHE", constraintName: "FKr4nitfmbmp9dht2pk246d68e9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-819") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ON_DEMAND_ALERT", constraintName: "FKr4pfuk8ly0nuynw87d5jg8cct", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-820") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ARCHIVED_SINGLE_CASE_ALERT", constraintName: "FKr9n8pkn4fmv9fp6fo3exb8pbd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-821") {
        addForeignKeyConstraint(baseColumnNames: "PVS_GLOBAL_TAG_ID", baseTableName: "SINGLE_GLOBAL_TAGS", constraintName: "FKrbk26ckl20y61bnm3flko1mmk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "pvs_global_tag")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-822") {
        addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "spotfire_notification_pvuser", constraintName: "FKrdeku7oiw606oc27n077l4agf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-823") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_OUTCOME_ID", baseTableName: "VALIDATED_SIGNAL_OUTCOMES", constraintName: "FKre3rort06h65skghf2j23ad7p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_OUTCOME")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-824") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKrepui5ibyqyi0ylkhdmrggn9x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-825") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKriardutnvjukg5o5ae4i1t23o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-826") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "pvuser_safety_groups", constraintName: "FKriwpg5obw3hvucl89nm7cyfg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-827") {
        addForeignKeyConstraint(baseColumnNames: "workflow_group_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKrnu8n3wqjbeb1qdcgs36odomn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-828") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_LITERATURE_ALERT", constraintName: "FKrnwq5b3qaj65u7w57lm3o7gy6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-829") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "ALERT_TAG", constraintName: "FKrrbo69cgkt2oof0qend38karl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-830") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_EVDAS_ALERT_ACTIONS", constraintName: "FKruapp3pulwttybjnqt0ih5cej", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-831") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "RCONFIG", constraintName: "FKrwhfg2lf7qokjhuat482craww", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-832") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_ALERT", constraintName: "FKrx1ksfdkal8plsauewx9wcoje", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-833") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "ARCHIVED_SCA_TAGS", constraintName: "FKs4oc9hkhlcen6gnfwlmmp7gwb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-834") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_group_id", baseTableName: "CASE_HISTORY", constraintName: "FKs60gq6t7tjj3hsunqhur4w35a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-835") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "ARCHIVED_SCA_ACTIONS", constraintName: "FKs63kxguooseokrhs0j33gfwq6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-836") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKs64fr0mhkeoburd4i5hvvtmxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-837") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKs6fht51c12chh1glukanj1vuu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-838") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_RCONFIG", constraintName: "FKscrg3o7ey1xr3ac0oldqvq1cg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-839") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ALERTS", constraintName: "FKsg3649m44b1j9j7kg3saeacwx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-840") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLATE_ID", baseTableName: "RCONFIG", constraintName: "FKsh216m1mb3g41hm0h6w12wh12", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-841") {
        addForeignKeyConstraint(baseColumnNames: "default_lit_disposition_id", baseTableName: "GROUPS", constraintName: "FKsh4dl71unjyng5oae37810ugs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-842") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "SINGLE_ALERT_ACTIONS", constraintName: "FKshd0nte1n4d4v5cp5dav55ic0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-843") {
        addForeignKeyConstraint(baseColumnNames: "ex_lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKsm5fds0flxc0po7eqgc5gqgpj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-844") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FKsp010pdhbqal79g9q1y3kl5v3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-845") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKsqppqusike0nnjet4x4elrkom", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-846") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKsy5o19eqrc5385sfoidonp43l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-847") {
        addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKsysb0klybb1kk1o3f5auerjx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-848") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ARCHIVED_EVDAS_ALERT", constraintName: "FKt093cjln518o54qj1rv0u5r4r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-849") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKt2b7h0ovgst4owagdj3bhj57t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-850") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "validated_signal_action_taken", constraintName: "FKt5h2kt8e2kf76xpxs11tw98ek", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-851") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ARCHIVED_ACA", constraintName: "FKt6fm3gafi66mftqot7fu2qs9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-852") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ARCHIVED_AGG_ALERT", constraintName: "FKt6os25wyok9pdgy2suadx9vvf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-853") {
        addForeignKeyConstraint(baseColumnNames: "LINKED_SIGNAL_ID", baseTableName: "SIGNAL_LINKED_SIGNALS", constraintName: "FKt7saax9k5yl609iwcsu4x1gr7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-854") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_ON_DEMAND_ALERT", constraintName: "FKt89eni1sgfgosaee42phw0j0a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-855") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIONS", constraintName: "FKt8jxq56nworpeaop4oarjkmpy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-856") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "ALERTS", constraintName: "FKtawq2r3by3tlx2bx8ohm6ssdb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-857") {
        addForeignKeyConstraint(baseColumnNames: "ARCHIVED_ACA_ID", baseTableName: "VALIDATED_ARCHIVED_ACA", constraintName: "FKtdy0p6crwvybnpn4fr5m4r547", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ARCHIVED_AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-858") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_ACTION_TAKEN", constraintName: "FKte7ljx7lhewxd219su74acrd7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-859") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_pin_configuration", constraintName: "FKthlc7b2a9b7xpi9eoo31y0al7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-860") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKtirjstq25k3s016xchl6imgv8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-861") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKtlr3lhis3k57s65e2wrfm6tpx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-862") {
        addForeignKeyConstraint(baseColumnNames: "user_user_departments_id", baseTableName: "pvuser_user_department", constraintName: "FKtmsfftkx8df5gdlnxgjyub76b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-863") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_ON_DEMAND_ALERT", constraintName: "FKtphrpv7ktdcanh8loeb1e5qcv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-864") {
        addForeignKeyConstraint(baseColumnNames: "ROWS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FKxuj1f2muxnhq2fgcqbxuycjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-865") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "product_event_history", constraintName: "FKy156nyus7pkpemc1lk9wuy73", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-866") {
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-867") {
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-868") {
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-869") {
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-870") {
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-871") {
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-872") {
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-873") {
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-874") {
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-875") {
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-876") {
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-877") {
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-878") {
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-879") {
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-880") {
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-881") {
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-882") {
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-883") {
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-884") {
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1608626578687-885") {
        dropTable(tableName: "HT_VALUE")
    }

    changeSet(author: "anshul (generated)", id: "1608626578687-886") {

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

    changeSet(author: "anshul (generated)", id: "1608626578687-887") {

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

    changeSet(author: "anshul (generated)", id: "1608626578687-888") {

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

    changeSet(author: "ankit (generated)", id: "1608626578687-889") {
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

    changeSet(author: "ankit (generated)", id: "1608626578687-890") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'IX_LITERATURE_EXEC_CONFIG_ID')
            }
        }
        createIndex(indexName: "IX_LITERATURE_EXEC_CONFIG_ID", tableName: "LITERATURE_ALERT") {
            column(name: "ex_lit_search_config_id")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608626578687-891") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_single_alert_exconfig", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "EXEC_CONFIG_ID")
        }
    }

    changeSet(author: "ankit (generated)", id: "1608626578687-892") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_agg_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_agg_alert_exconfig", tableName: "AGG_ALERT", unique: "false") {
            column(name: "EXEC_CONFIGURATION_ID")
        }
    }

    changeSet(author: "nitesh (generated)", id: "1608626578687-893") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ATTACHMENT', columnName: 'INPUT_NAME')
        }
        sql("alter table ATTACHMENT modify INPUT_NAME VARCHAR2(4000 CHAR);")
    }

    changeSet(author: "ankit (generated)", id: "1608626578687-894") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: 'FK1o4lv2wri14x2kmor19gv99dw')
        }
        dropForeignKeyConstraint(baseTableName: "user_view_order", constraintName: "FK1o4lv2wri14x2kmor19gv99dw")
    }

    changeSet(author: "yogesh (generated)", id: "167895932323831565-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'USER_IP_ADDRESS')
            }
        }
        addColumn(tableName: "audit_log") {
            column(name: "USER_IP_ADDRESS", type: "varchar2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "16831841030211-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'TRANSACTION_ID')
            }
        }
        sql("alter table AUDIT_LOG  add TRANSACTION_ID VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "16831841030211-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'IS_FIRST_ENTRY_IN_TRANSACTION')
            }
        }
        sql("alter table AUDIT_LOG  add IS_FIRST_ENTRY_IN_TRANSACTION NUMBER(1);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "16831841030211-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'MODULE_NAME')
            }
        }
        sql("alter table AUDIT_LOG  add MODULE_NAME VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "16831841030211-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'DEVICE')
            }
        }
        sql("alter table AUDIT_LOG  add DEVICE VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "16831814030211-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'BROWSER')
            }
        }
        sql("alter table AUDIT_LOG  add BROWSER VARCHAR2(255);")
    }

    changeSet(author: "Uddesh Teke (generated)", id: "16831841030211-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'TIME_ZONE')
            }
        }
        sql("alter table AUDIT_LOG  add TIME_ZONE VARCHAR2(255);")
    }
    changeSet(author: "Uddesh Teke (generated)", id: "16831841030211-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUDIT_LOG', columnName: 'DESCRIPTION')
            }
        }
        sql("alter table AUDIT_LOG  add DESCRIPTION VARCHAR2(255);")
    }

}