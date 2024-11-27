databaseChangeLog = {

    changeSet(author: "APE (generated)", id: "1552329383420-1") {
        createSequence(sequenceName: "agg_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-2") {
        createSequence(sequenceName: "config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-3") {
        createSequence(sequenceName: "evdas_config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-4") {
        createSequence(sequenceName: "evdas_exec_config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-5") {
        createSequence(sequenceName: "evdas_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-6") {
        createSequence(sequenceName: "exec_config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-7") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-8") {
        createSequence(sequenceName: "sca_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-9") {
        createTable(tableName: "ACCESS_CONTROL_GROUP") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACCESS_CONTROL_GROUPPK")
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

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "LDAP_GROUP_NAME", type: "VARCHAR2(30 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(30 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-10") {
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

            column(name: "agg_alert_id", type: "NUMBER(19, 0)")

            column(name: "alert_id", type: "NUMBER(19, 0)")

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

            column(name: "evdas_alert_id", type: "NUMBER(19, 0)")

            column(name: "literature_alert_id", type: "NUMBER(19, 0)")

            column(name: "meeting_id", type: "VARCHAR2(255 CHAR)")

            column(name: "owner_id", type: "NUMBER(19, 0)")

            column(name: "single_case_alert_id", type: "NUMBER(19, 0)")

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "viewed", type: "NUMBER(1, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-11") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-12") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-13") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-14") {
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

            column(name: "attributes", type: "VARCHAR2(4000 CHAR)")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "client_name", type: "VARCHAR2(255 CHAR)")

            column(name: "DETAILS", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "event_name", type: "VARCHAR2(255 CHAR)")

            column(name: "ip_address", type: "VARCHAR2(255 CHAR)")

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "performed_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

    changeSet(author: "APE (generated)", id: "1552329383420-15") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-16") {
        createTable(tableName: "ADHOC_ALERT_ACTION_TAKEN") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TAKEN", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-17") {
        createTable(tableName: "ADHOC_ALERT_COMMENTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-18") {
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

            column(name: "description", type: "VARCHAR2(255 CHAR)")

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

    changeSet(author: "APE (generated)", id: "1552329383420-19") {
        createTable(tableName: "AD_HOC_SIGNAL_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-20") {
        createTable(tableName: "AD_HOC_TOPIC_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-21") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-22") {
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

            column(name: "detected_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "due_date", type: "TIMESTAMP") {
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

            column(name: "flagged", type: "NUMBER(1, 0)")

            column(name: "flags", type: "VARCHAR2(255 CHAR)")

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "freq_priority", type: "VARCHAR2(255 CHAR)")

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

            column(name: "prr05", type: "VARCHAR2(255 CHAR)")

            column(name: "prr95", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "PRR_STR", type: "CLOB")

            column(name: "prr_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "prr_value", type: "VARCHAR2(255 CHAR)") {
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

            column(name: "ror05", type: "VARCHAR2(255 CHAR)")

            column(name: "ror95", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_mh", type: "VARCHAR2(255 CHAR)")

            column(name: "ROR_STR", type: "CLOB")

            column(name: "ror_str05", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_str95", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "soc", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "trend_type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-23") {
        createTable(tableName: "AGG_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-24") {
        createTable(tableName: "AGG_SIGNAL_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-25") {
        createTable(tableName: "AGG_TOPIC_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-26") {
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

            column(name: "event_selection", type: "VARCHAR2(255 CHAR)")

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

            column(name: "comment_signal_status", type: "VARCHAR2(255 CHAR)")

            column(name: "country_of_incidence", type: "VARCHAR2(255 CHAR)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "detected_by", type: "VARCHAR2(255 CHAR)")

            column(name: "formulations", type: "VARCHAR2(255 CHAR)")

            column(name: "ha_date_closed", type: "TIMESTAMP")

            column(name: "ha_signal_status_id", type: "NUMBER(19, 0)")

            column(name: "indication", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_data_source", type: "VARCHAR2(255 CHAR)")

            column(name: "issue_previously_tracked", type: "NUMBER(1, 0)")

            column(name: "last_decision_date", type: "TIMESTAMP")

            column(name: "last_updated_note", type: "TIMESTAMP")

            column(name: "name", type: "VARCHAR2(4000 CHAR)")

            column(name: "note_modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "number_oficsrs", type: "NUMBER(10, 0)")

            column(name: "owner_id", type: "NUMBER(19, 0)")

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

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

    changeSet(author: "APE (generated)", id: "1552329383420-27") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-28") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-29") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-30") {
        createTable(tableName: "ALERT_QUERY_VALUES") {
            column(name: "ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-31") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-32") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-33") {
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

            column(name: "description", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-34") {
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

            column(name: "PRODUCTS", type: "CLOB")

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-35") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-36") {
        createTable(tableName: "CASE_HISTORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "CASE_HISTORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "tag_name", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-37") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-38") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-39") {
        createTable(tableName: "COGNOS_REPORT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "COGNOS_REPORTPK")
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

            column(name: "DESCRIPTION", type: "VARCHAR2(1000 CHAR)")

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-40") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-41") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-42") {
        createTable(tableName: "DELIVERIES_EMAIL_USERS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-43") {
        createTable(tableName: "DELIVERIES_RPT_FORMATS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-44") {
        createTable(tableName: "DELIVERIES_SHARED_WITHS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-45") {
        createTable(tableName: "DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-46") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-47") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-48") {
        createTable(tableName: "DISPO_RULES_SIGNAL_CATEGORY") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-49") {
        createTable(tableName: "DISPO_RULES_USER_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-50") {
        createTable(tableName: "DISPO_RULES_WORKFLOW_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-51") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-52") {
        createTable(tableName: "DTAB_COL_MEAS_MEASURES") {
            column(name: "DTAB_COL_MEAS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "MEASURES_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-53") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-54") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-55") {
        createTable(tableName: "DTAB_TEMPLTS_COL_MEAS") {
            column(name: "DTAB_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "COLUMN_MEASURE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-56") {
        createTable(tableName: "EMAIL_LOG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EMAIL_LOGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "VARCHAR2(4000 CHAR)")

            column(name: "sent_on", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "sent_to", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "subject", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-57") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-58") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-59") {
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

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "hlgt", type: "VARCHAR2(255 CHAR)")

            column(name: "hlt", type: "VARCHAR2(255 CHAR)")

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

    changeSet(author: "APE (generated)", id: "1552329383420-60") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-61") {
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

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-62") {
        createTable(tableName: "EVDAS_CONFIGURATION_GROUPS") {
            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-63") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-64") {
        createTable(tableName: "EVDAS_SIGNAL_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-65") {
        createTable(tableName: "EVDAS_TOPIC_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-66") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-67") {
        createTable(tableName: "EX_ALERT_QUERY_VALUES") {
            column(name: "EX_ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-68") {
        createTable(tableName: "EX_CLL_TEMPLT") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-69") {
        createTable(tableName: "EX_CUSTOM_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-70") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-71") {
        createTable(tableName: "EX_DELIVERIES_EMAIL_USERS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-72") {
        createTable(tableName: "EX_DELIVERIES_RPT_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-73") {
        createTable(tableName: "EX_DELIVERIES_SHARED_WITHS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-74") {
        createTable(tableName: "EX_DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-75") {
        createTable(tableName: "EX_DTAB_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-76") {
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

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-77") {
        createTable(tableName: "EX_EVDAS_CONFIG_ACTIVITIES") {
            column(name: "EX_EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-78") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-79") {
        createTable(tableName: "EX_LITERATURE_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_LITERATURE_CONFIGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

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

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(200 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-80") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-81") {
        createTable(tableName: "EX_NCASE_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-82") {
        createTable(tableName: "EX_QUERY_EXP") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-83") {
        createTable(tableName: "EX_QUERY_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-84") {
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

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "case_series_execution_status", type: "VARCHAR2(255 CHAR)")

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "EXCLUDE_FOLLOWUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_ALERT_DATE_RANGE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_ALERT_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "GROUP_BY_SMQ", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCLUDE_LOCKED_VERSION", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "NUMBER(1, 0)") {
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

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "priority_id", type: "NUMBER(19, 0)")

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

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

            column(name: "STUDY_SELECTION", type: "CLOB")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-85") {
        createTable(tableName: "EX_RCONFIGS_PROD_GRP") {
            column(name: "EXCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-86") {
        createTable(tableName: "EX_SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-87") {
        createTable(tableName: "EX_STATUS") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_STATUSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "END_TIME", type: "NUMBER(19, 0)")

            column(name: "EX_STATUS", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "FREQUENCY", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "MESSAGE", type: "CLOB")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "RPT_VERSION", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SECTION_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "STACK_TRACE", type: "CLOB")

            column(name: "START_TIME", type: "NUMBER(19, 0)")

            column(name: "TEMPLATE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-88") {
        createTable(tableName: "EX_STATUSES_RPT_FORMATS") {
            column(name: "EX_STATUS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-89") {
        createTable(tableName: "EX_STATUSES_SHARED_WITHS") {
            column(name: "EX_STATUS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-90") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-91") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-92") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-93") {
        createTable(tableName: "EX_TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-94") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-95") {
        createTable(tableName: "GROUPS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "GROUPSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_prod", type: "CLOB")

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(200 CHAR)")

            column(name: "force_justification", type: "NUMBER(1, 0)")

            column(name: "group_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_active", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "selected_datasource", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-96") {
        createTable(tableName: "GUEST_ATTENDEE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "GUEST_ATTENDEEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-97") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-98") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-99") {
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

            column(name: "JUSTIFICATION", type: "VARCHAR2(4000 CHAR)")

            column(name: "performed_by_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

    changeSet(author: "APE (generated)", id: "1552329383420-100") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-101") {
        createTable(tableName: "LITERATURE_ALERT_TAGS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-102") {
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

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "date_range_information_id", type: "NUMBER(19, 0)")

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

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "search_string", type: "VARCHAR2(255 CHAR)")

            column(name: "total_execution_time", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-103") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-104") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-105") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-106") {
        createTable(tableName: "MEETING_ATTACHMENTS") {
            column(name: "MEETING_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ATTACHMENTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-107") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-108") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-109") {
        createTable(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES") {
            column(name: "NONCASE_SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-110") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-111") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-112") {
        createTable(tableName: "PREFERENCE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PREFERENCEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TIME_ZONE", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-113") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-114") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-115") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-116") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-117") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-118") {
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

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "USERNAME", type: "VARCHAR2(30 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-119") {
        createTable(tableName: "PVUSERS_ROLES") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-120") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-121") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-122") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-123") {
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

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "AS_OF_VERSION_DATE_DELTA", type: "NUMBER(10, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "BLANK_VALUES", type: "CLOB")

            column(name: "SELECTED_TIME_ZONE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "limit_primary_path", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_period", type: "NUMBER(10, 0)")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "STUDY_SELECTION", type: "CLOB")

            column(name: "TEMPLATE_ID", type: "NUMBER(19, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")

            column(name: "WORKFLOW_GROUP", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-124") {
        createTable(tableName: "RCONFIGS_PROD_GRP") {
            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-125") {
        createTable(tableName: "RCONFIG_DISPOSITION") {
            column(name: "CONFIGURATION_DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")

            column(name: "RCONFIG_DISPOSITION_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-126") {
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

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-127") {
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

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_RESULT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-128") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-129") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-130") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-131") {
        createTable(tableName: "RPT_FIELD_INFO_LIST") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELD_INFO_LISTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-132") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-133") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-134") {
        createTable(tableName: "RPT_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_TEMPLTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CATEGORY_ID", type: "NUMBER(19, 0)")

            column(name: "created_by", type: "VARCHAR2(20 CHAR)") {
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

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-135") {
        createTable(tableName: "RPT_TEMPLTS_TAGS") {
            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-136") {
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

            column(name: "tags", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-137") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-138") {
        createTable(tableName: "SIGNAL_CATEGORY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_CATEGORYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-139") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-140") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-141") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-142") {
        createTable(tableName: "SINGLE_ALERT_CON_COMIT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CON_COMIT", type: "VARCHAR2(255 CHAR)")

            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-143") {
        createTable(tableName: "SINGLE_ALERT_PT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PT", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-838") {
        modifyDataType(columnName: "PT", newDataType: "VARCHAR2(2000 CHAR)", tableName: "SINGLE_ALERT_PT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-144") {
        createTable(tableName: "SINGLE_ALERT_SUSP_PROD") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-145") {
        createTable(tableName: "SINGLE_ALERT_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-146") {
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

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_group_id", type: "NUMBER(19, 0)")

            column(name: "attributes", type: "CLOB")

            column(name: "case_init_receipt_date", type: "TIMESTAMP")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "case_report_type", type: "VARCHAR2(255 CHAR)")

            column(name: "case_version", type: "NUMBER(10, 0)")

            column(name: "con_comit", type: "VARCHAR2(255 CHAR)")

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

            column(name: "is_case_series", type: "NUMBER(1, 0)")

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "VARCHAR2(255 CHAR)")

            column(name: "locked_date", type: "TIMESTAMP")

            column(name: "master_pref_term_all", type: "VARCHAR2(2000 CHAR)")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "outcome", type: "VARCHAR2(255 CHAR)")

            column(name: "period_end_date", type: "TIMESTAMP")

            column(name: "period_start_date", type: "TIMESTAMP")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_family", type: "VARCHAR2(255 CHAR)") {
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

            column(name: "susp_prod", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-147") {
        createTable(tableName: "SINGLE_SIGNAL_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-148") {
        createTable(tableName: "SINGLE_TOPIC_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-149") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-150") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-151") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-152") {
        createTable(tableName: "SQL_TEMPLTS_SQL_VALUES") {
            column(name: "SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-153") {
        createTable(tableName: "SQL_TEMPLT_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FIELD", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-154") {
        createTable(tableName: "SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-155") {
        createTable(tableName: "STRATEGY_MEDICAL_CONCEPTS") {
            column(name: "SIGNAL_STRATEGY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-156") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-157") {
        createTable(tableName: "TEMPLT_QRS_QUERY_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-158") {
        createTable(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-159") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-160") {
        createTable(tableName: "TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-161") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-162") {
        createTable(tableName: "TOPIC_ACTIVITIES") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-163") {
        createTable(tableName: "TOPIC_ADHOC_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-164") {
        createTable(tableName: "TOPIC_AGG_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-165") {
        createTable(tableName: "TOPIC_COMMENTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-166") {
        createTable(tableName: "TOPIC_GROUP") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-167") {
        createTable(tableName: "TOPIC_SINGLE_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-168") {
        createTable(tableName: "USER_GROUPS") {
            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-169") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-170") {
        createTable(tableName: "VALIDATED_ADHOC_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-171") {
        createTable(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-172") {
        createTable(tableName: "VALIDATED_ALERT_ACTIVITIES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-173") {
        createTable(tableName: "VALIDATED_ALERT_COMMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-174") {
        createTable(tableName: "VALIDATED_ALERT_DOCUMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_DOCUMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-175") {
        createTable(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-176") {
        createTable(tableName: "VALIDATED_LITERATURE_ALERTS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-177") {
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

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "REASON_FOR_EVALUATION", type: "VARCHAR2(4000)")

            column(name: "REPORT_PREFERENCE", type: "VARCHAR2(255 CHAR)")

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "topic", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-178") {
        createTable(tableName: "VALIDATED_SIGNAL_CATEGORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-179") {
        createTable(tableName: "VALIDATED_SIGNAL_GROUP") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-180") {
        createTable(tableName: "VALIDATED_SIGNAL_RCONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-181") {
        createTable(tableName: "VALIDATED_SINGLE_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-182") {
        createTable(tableName: "VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "VALUEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-183") {
        createTable(tableName: "VALUES_PARAMS") {
            column(name: "VALUE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-184") {
        createTable(tableName: "VS_EVAL_METHOD") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVALUATION_METHOD", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-185") {
        createTable(tableName: "VS_EVDAS_CONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-186") {
        createTable(tableName: "WORKFLOWRULES_ACTION_TEMPLATES") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TEMPLATE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-187") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-188") {
        createTable(tableName: "WORKFLOWRULES_SIGNAL_CATEGORY") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-189") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-190") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-191") {
        createTable(tableName: "WkFL_RUL_DISPOSITIONS") {
            column(name: "workflow_rule_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_dispositions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-192") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-193") {
        createTable(tableName: "attachment") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "attachmentPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
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

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "poster_class", type: "VARCHAR2(255 CHAR)")

            column(name: "poster_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-194") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-195") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-196") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-197") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-198") {
        createTable(tableName: "evdas_data_automation") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "evdas_data_automationPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "config_selected_time_zone", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "created_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "ermrtype", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP")

            column(name: "executing", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "file_type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "is_deleted", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "is_enabled", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "medra_reaction_term", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "next_run_date", type: "TIMESTAMP")

            column(name: "schedule_datejson", type: "VARCHAR2(1024 CHAR)")

            column(name: "start_date", type: "TIMESTAMP")

            column(name: "substance", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-199") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-200") {
        createTable(tableName: "evdas_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "evdas_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "evdas_alert_id", type: "NUMBER(19, 0)")

            column(name: "event_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "execution_date", type: "TIMESTAMP")

            column(name: "is_latest", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "justification", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-201") {
        createTable(tableName: "evdas_user_download") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "evdas_user_downloadPK")
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

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "EUD_PASSWORD", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-202") {
        createTable(tableName: "ex_rconfig_activities") {
            column(name: "EX_CONFIG_ACTIVITIES_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-203") {
        createTable(tableName: "inbox_log") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "inbox_logPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "content", type: "VARCHAR2(4000 CHAR)")

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

            column(name: "subject", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-204") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-205") {
        createTable(tableName: "meeting_actions") {
            column(name: "meeting_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-206") {
        createTable(tableName: "meeting_activities") {
            column(name: "meeting_activities_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activity_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-207") {
        createTable(tableName: "meeting_guest_attendee") {
            column(name: "meeting_guest_attendee_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-208") {
        createTable(tableName: "meeting_pvuser") {
            column(name: "meeting_attendees_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-209") {
        createTable(tableName: "product_event_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "product_event_historyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "agg_case_alert_id", type: "NUMBER(19, 0)")

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

            column(name: "justification", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "new_count", type: "NUMBER(10, 0)")

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

            column(name: "tag_name", type: "VARCHAR2(4000 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-210") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-211") {
        createTable(tableName: "pvuser_safety_groups") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "safety_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-212") {
        createTable(tableName: "pvuser_user_department") {
            column(name: "user_user_departments_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_department_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-213") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-214") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-215") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-216") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-217") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-218") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-219") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-220") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-221") {
        createTable(tableName: "test_user_role") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-222") {
        createTable(tableName: "topic_actions") {
            column(name: "topic_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-223") {
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

    changeSet(author: "APE (generated)", id: "1552329383420-224") {
        createTable(tableName: "user_group_s") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-225") {
        createTable(tableName: "valid_mining_frequency") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "valid_mining_frequencyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-226") {
        createTable(tableName: "valid_upload_frequency") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "valid_upload_frequencyPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-227") {
        createTable(tableName: "validated_signal_action_taken") {
            column(name: "validated_signal_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_taken_string", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-228") {
        createTable(tableName: "validated_signal_actions") {
            column(name: "validated_signal_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-229") {
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

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "sorting", type: "VARCHAR2(255 CHAR)")

            column(name: "temp_column_seq", type: "CLOB")

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-230") {
        createTable(tableName: "work_flow_rules_actions") {
            column(name: "workflow_rule_actions_id", type: "NUMBER(19, 0)")

            column(name: "action_id", type: "NUMBER(19, 0)")

            column(name: "actions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552329383420-231") {
        createIndex(indexName: "IX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-232") {
        createIndex(indexName: "IX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-233") {
        createIndex(indexName: "IX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-234") {
        createIndex(indexName: "IX_VALIDATED_ADHOC_ALERTS", tableName: "VALIDATED_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_ADHOC_ALERTS", tableName: "VALIDATED_ADHOC_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-235") {
        createIndex(indexName: "IX_VALIDATED_AGG_ALERTS", tableName: "VALIDATED_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_AGG_ALERTS", tableName: "VALIDATED_AGG_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-236") {
        createIndex(indexName: "IX_VALIDATED_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS", unique: "true") {
            column(name: "EVDAS_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-237") {
        createIndex(indexName: "IX_VALIDATED_LITERATURE_ALERTS", tableName: "VALIDATED_LITERATURE_ALERTS", unique: "true") {
            column(name: "LITERATURE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "LITERATURE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_LITERATURE_ALERTS", tableName: "VALIDATED_LITERATURE_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-238") {
        createIndex(indexName: "IX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-239") {
        createIndex(indexName: "IX_pvuser_safety_groups", tableName: "pvuser_safety_groups", unique: "true") {
            column(name: "user_id")

            column(name: "safety_group_id")
        }

        addPrimaryKey(columnNames: "user_id, safety_group_id", forIndexName: "IX_pvuser_safety_groups", tableName: "pvuser_safety_groups")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-240") {
        createIndex(indexName: "IX_user_group_s", tableName: "user_group_s", unique: "true") {
            column(name: "user_id")

            column(name: "group_id")
        }

        addPrimaryKey(columnNames: "user_id, group_id", forIndexName: "IX_user_group_s", tableName: "user_group_s")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-241") {
        createIndex(indexName: "IX_AGG_ALERTPK", tableName: "AGG_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "AGG_ALERTPK", forIndexName: "IX_AGG_ALERTPK", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-242") {
        createIndex(indexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "CLL_TEMPLTPK", forIndexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-243") {
        createIndex(indexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "DTAB_TEMPLTPK", forIndexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-244") {
        createIndex(indexName: "IX_EVDAS_ALERTPK", tableName: "EVDAS_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_ALERTPK", forIndexName: "IX_EVDAS_ALERTPK", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-245") {
        createIndex(indexName: "IX_EVDAS_CONFIGPK", tableName: "EVDAS_CONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EVDAS_CONFIGPK", forIndexName: "IX_EVDAS_CONFIGPK", tableName: "EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-246") {
        createIndex(indexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_CLL_TEMPLTPK", forIndexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-247") {
        createIndex(indexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_CUSTOM_SQL_TEMPLTPK", forIndexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-248") {
        createIndex(indexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_DTAB_TEMPLTPK", forIndexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-249") {
        createIndex(indexName: "IX_EX_EVDAS_CONFIGPK", tableName: "EX_EVDAS_CONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_EVDAS_CONFIGPK", forIndexName: "IX_EX_EVDAS_CONFIGPK", tableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-250") {
        createIndex(indexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_NCASE_SQL_TEMPLTPK", forIndexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-251") {
        createIndex(indexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_QUERY_EXPPK", forIndexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-252") {
        createIndex(indexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_QUERY_VALUEPK", forIndexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-253") {
        createIndex(indexName: "IX_EX_RCONFIGPK", tableName: "EX_RCONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_RCONFIGPK", forIndexName: "IX_EX_RCONFIGPK", tableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-254") {
        createIndex(indexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_SQL_VALUEPK", forIndexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-255") {
        createIndex(indexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_TEMPLT_VALUEPK", forIndexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-256") {
        createIndex(indexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG", unique: "true") {
            column(name: "KEY_1")
        }

        addPrimaryKey(columnNames: "KEY_1", constraintName: "MISC_CONFIGPK", forIndexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-257") {
        createIndex(indexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "NONCASE_SQL_TEMPLTPK", forIndexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-258") {
        createIndex(indexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "PVUSERS_ROLESPK", forIndexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-259") {
        createIndex(indexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "QUERY_EXP_VALUEPK", forIndexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-260") {
        createIndex(indexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "QUERY_VALUEPK", forIndexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-261") {
        createIndex(indexName: "IX_RCONFIGPK", tableName: "RCONFIG", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "RCONFIGPK", forIndexName: "IX_RCONFIGPK", tableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-262") {
        createIndex(indexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP", unique: "true") {
            column(name: "NAME")
        }

        addPrimaryKey(columnNames: "NAME", constraintName: "RPT_FIELD_GROUPPK", forIndexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-263") {
        createIndex(indexName: "IX_SINGLE_CASE_ALERTPK", tableName: "SINGLE_CASE_ALERT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SINGLE_CASE_ALERTPK", forIndexName: "IX_SINGLE_CASE_ALERTPK", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-264") {
        createIndex(indexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER", unique: "true") {
            column(name: "REPORT_ITEM")
        }

        addPrimaryKey(columnNames: "REPORT_ITEM", constraintName: "SOURCE_COLUMN_MASTERPK", forIndexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-265") {
        createIndex(indexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER", unique: "true") {
            column(name: "TABLE_NAME")
        }

        addPrimaryKey(columnNames: "TABLE_NAME", constraintName: "SOURCE_TABLE_MASTERPK", forIndexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-266") {
        createIndex(indexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SQL_TEMPLTPK", forIndexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-267") {
        createIndex(indexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_TEMPLT_VALUEPK", forIndexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-268") {
        createIndex(indexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_VALUEPK", forIndexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-269") {
        createIndex(indexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "TEMPLT_VALUEPK", forIndexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-270") {
        createIndex(indexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "COMMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, COMMENT_ID", constraintName: "VALIDATED_ALERT_COMMENTSPK", forIndexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-271") {
        createIndex(indexName: "IX_VALIDATED_ALERT_DOCUMENTSPK", tableName: "VALIDATED_ALERT_DOCUMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "ALERT_DOCUMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, ALERT_DOCUMENT_ID", constraintName: "VALIDATED_ALERT_DOCUMENTSPK", forIndexName: "IX_VALIDATED_ALERT_DOCUMENTSPK", tableName: "VALIDATED_ALERT_DOCUMENTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-272") {
        createIndex(indexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities", unique: "true") {
            column(name: "EX_CONFIG_ACTIVITIES_ID")

            column(name: "ACTIVITY_ID")
        }

        addPrimaryKey(columnNames: "EX_CONFIG_ACTIVITIES_ID, ACTIVITY_ID", constraintName: "ex_rconfig_activitiesPK", forIndexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-273") {
        createIndex(indexName: "IX_test_user_rolePK", tableName: "test_user_role", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "test_user_rolePK", forIndexName: "IX_test_user_rolePK", tableName: "test_user_role")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-274") {
        addUniqueConstraint(columnNames: "LDAP_GROUP_NAME", constraintName: "UC_A_C_GLDAP_GRP_NAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-275") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_A_CONTROL_GRPNAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-276") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_ACTION_TEMPLATENAME_COL", tableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-277") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_AEVAL_TYPEVALUE_COL", tableName: "AEVAL_TYPE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-278") {
        addUniqueConstraint(columnNames: "chronicle_id", constraintName: "UC_ALERT_DOCCHRONICLE_ID_COL", tableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-279") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_ALERT_TAGNAME_COL", tableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-280") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_CATEGORYNAME_COL", tableName: "CATEGORY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-281") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_DISPO_DISPLAY_NAME_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-282") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_DISPO_VALUE_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-283") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_DISPO_RULESNAME_COL", tableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-284") {
        addUniqueConstraint(columnNames: "SCHEDULE_NAME", constraintName: "UC_ETL_SCHEDULE_NAME_COL", tableName: "ETL_SCHEDULE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-285") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_EVAL_REF_TYPENAME_COL", tableName: "EVAL_REF_TYPE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-286") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_GROUPSNAME_COL", tableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-287") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_P_DISPLAY_NAME_COL", tableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-288") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PRIORITYVALUE_COL", tableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-289") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PVS_STATEVALUE_COL", tableName: "PVS_STATE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-290") {
        addUniqueConstraint(columnNames: "USERNAME", constraintName: "UC_PVUSERUSERNAME_COL", tableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-291") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_PV_CONCEPTNAME_COL", tableName: "PV_CONCEPT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-292") {
        addUniqueConstraint(columnNames: "report_name", constraintName: "UC_REPORT_HIS_REP_NAME_COL", tableName: "report_history")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-293") {
        addUniqueConstraint(columnNames: "AUTHORITY", constraintName: "UC_ROLEAUTHORITY_COL", tableName: "ROLE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-294") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELDNAME_COL", tableName: "RPT_FIELD")
    }

//    changeSet(author: "APE (generated)", id: "1552329383420-295") {
//        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELD_GROUPNAME_COL", tableName: "RPT_FIELD_GROUP")
//    }

    changeSet(author: "APE (generated)", id: "1552329383420-296") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_SAFETY_GROUPNAME_COL", tableName: "safety_group")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-297") {
        addUniqueConstraint(columnNames: "token", constraintName: "UC_SPOTFIRE_SESSIONTOKEN_COL", tableName: "spotfire_session")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-298") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_TAGNAME_COL", tableName: "TAG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-299") {
        addUniqueConstraint(columnNames: "authority", constraintName: "UC_TEST_ROLEAUTHORITY_COL", tableName: "test_role")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-300") {
        addUniqueConstraint(columnNames: "email", constraintName: "UC_TEST_SAML_USEREMAIL_COL", tableName: "test_saml_user")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-301") {
        addUniqueConstraint(columnNames: "username", constraintName: "UC_TEST_SAML_USERUSERNAME_COL", tableName: "test_saml_user")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-302") {
        addUniqueConstraint(columnNames: "department_name", constraintName: "UC_USER_DEPT_NAME_COL", tableName: "user_department")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-303") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_VALIDATED_SIGNAL_COL", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-304") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_WORK_FLOW_RULESNAME_COL", tableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-305") {
        addUniqueConstraint(columnNames: "alert_type, user_id, name", constraintName: "UK7285101c782d3bb42a19d76eade0", tableName: "view_instance")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-306") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FK1egn4hip9c2px7kmifks81ilj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-307") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "signal_history", constraintName: "FK1gksukhgbnjjbm6tq7sergnf3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-308") {
        addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FK1h2ucyipg6q9plbmkpqs9ml5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-309") {
        addForeignKeyConstraint(baseColumnNames: "EX_CONFIG_ACTIVITIES_ID", baseTableName: "ex_rconfig_activities", constraintName: "FK1lvnr30ngjjrcqtmugj4o7xxx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-310") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_RCONFIG", constraintName: "FK1mah9k2g3a2vog2rc5l3oyhap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-311") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK1pvjhvvvh16fitf3f9aepcrx3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-312") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK1tom8u04lfcjhkpwup2d8arxa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-313") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "product_event_history", constraintName: "FK1vu272crmaxuyfixksks1ij0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-314") {
        addForeignKeyConstraint(baseColumnNames: "literature_configuration_id", baseTableName: "EX_LITERATURE_DATE_RANGE", constraintName: "FK23j99y5cju9o2h64tv6c4xq1f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-315") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FK25gco89k379c8yngl2ixneunj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-316") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK28ogh0ec7qa0wls7dxduf1ee1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-317") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK2alihh3mnbowd4bubkjigrt2q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-318") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ALERTS", constraintName: "FK2asghyg8brvh2hd9ihjye9d8a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-319") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "TAG", constraintName: "FK2jyfx1eu2cdnehuj8dqwrd6pi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-320") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK2mrmhl81t6ttn1ga2kcbl8nc5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-321") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK2skfidis7c3uhb5n3pablyibp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-322") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "SIGNAL_CHART", constraintName: "FK2vyvpc8474sber4f5uljnhef0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-323") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK3277qdq090x04o2i21gcksi92", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-324") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "evdas_history", constraintName: "FK373ar2qi8f0e9w43cus7umvfm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-325") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FK37lt1se9b4d25ksatot1whkt7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-326") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "test_user_role", constraintName: "FK3g03ml77a0pr2nsyno5k23e0f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_role")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-327") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3i2ij0470itt2oyfll08nuagc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-328") {
        addForeignKeyConstraint(baseColumnNames: "MEETING_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FK3kly0ngl8jptbdmurwpdhn15e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-329") {
        addForeignKeyConstraint(baseColumnNames: "EX_DATE_RANGE_INFO_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK3ocntqp0fggudmyw65r9ghtth", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-330") {
        addForeignKeyConstraint(baseColumnNames: "lnk_id", baseTableName: "attachment", constraintName: "FK417wyfahv01xv8s9ypukvvpt6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment_link")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-331") {
        addForeignKeyConstraint(baseColumnNames: "business_configuration_id", baseTableName: "RULE_INFORMATION", constraintName: "FK46deuafhfurfdbrewcoueiogp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-332") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK46gb5ngvp56xrv2j0d91iuqf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-333") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FK4e8ln2cbe20xp8afdh1y7sa9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-334") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "signal_history", constraintName: "FK4he7y9hi5lwrs2d8tn16rsxew", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-335") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK4jhhis76bsysx4xm6i6ioykpc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-336") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "TOPIC", constraintName: "FK4kh9et74csyh0sxnpty9v5bwf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-337") {
        addForeignKeyConstraint(baseColumnNames: "RF_INFO_LIST_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK4u8v80bl2vqgbw7bs11nm3qr2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-338") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FK4xwtik25kkjo898vri20gjn8h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-339") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ALERT", constraintName: "FK51t9uul1wlam3muhfsdeni535", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-340") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FK530mahbw0a4gx3tet1q3w2g0l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-341") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "TOPIC", constraintName: "FK557wy3htik1yrmw2ldt3lujx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-342") {
        addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK568vr26l2ip7hmy80k1umjtw9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-343") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FK58riu8smk8hl4hwwx89gr2v4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-344") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIVITIES", constraintName: "FK5967su0w6fjlm4na1gewgy3ts", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-345") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK5bcrnlc25y3lkkm5d8xsqrb83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-346") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FK5qse9kfhl8x3c7v4yl99yy5ha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-347") {
        addForeignKeyConstraint(baseColumnNames: "commented_by_id", baseTableName: "COMMENTS", constraintName: "FK5r4u2uxs7lo6xkmrbmio08003", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-348") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FK5xqn3nlelifk9hpih50rd7b3s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-349") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "view_instance", constraintName: "FK61t29rs90yvo8nrrjuy8hnrpp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-350") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "evdas_history", constraintName: "FK67y86bymxk3lnavaj4ax1fmmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-351") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK6d5e73n3n8q05npuax4fknirw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-352") {
        addForeignKeyConstraint(baseColumnNames: "SOURCE_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FK6hfxx9xfelgaomred8dn99iig", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-353") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_ID", baseTableName: "RPT_ERROR", constraintName: "FK6m8eikuwcfty44ymm36rxip6c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-354") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FK6o7whfp7w45byxkfkg3ajgy8d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-355") {
        addForeignKeyConstraint(baseColumnNames: "config_id", baseTableName: "ACTIONS", constraintName: "FK6qnyouq2pj8noqbbgr6blr0fa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_CONFIGURATIONS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-356") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EVDAS_CONFIG", constraintName: "FK6sm0nv9c76tc7xx351jufvlo5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-357") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FK6vwyki46c40l23phcl2jsjovv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-358") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK6xlkiqggquuh3unim09gl7s5k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-359") {
        addForeignKeyConstraint(baseColumnNames: "topic_actions_id", baseTableName: "topic_actions", constraintName: "FK6xwerddtt6xmysbwpqm0thskw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-360") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FK73rmou5rxoc3xenxf07klxnvl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-361") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIONS", constraintName: "FK795v9wlhwg7olj50atxgreqp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-362") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FK7e6rwklblhyoo34rnjvek8fin", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-363") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "TOPIC", constraintName: "FK7fprf6132fra32pjukedntqyl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-364") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_RCONFIG", constraintName: "FK7jnrbgs0ar70viidp7mwns3sc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-365") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DELIVERIES_SHARED_WITHS", constraintName: "FK7ofjwn1n4eyo0b9ho6nqthf83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-366") {
        addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK7p44v7rjuor7a67e1uuhh00ki", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-367") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK7uqp71w2q1kumo5txv4soekoi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-368") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FK7uu820jqwt2ggpbwt5q1n3irg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-369") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "SHARED_WITH", constraintName: "FK7yoqaegsqnf7fv4hmpsxc0d5u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-370") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "TOPIC", constraintName: "FK81ax335qvxywo42jwjpk2uj70", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-371") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK81iwl1ty36fpnf3hbv07ge0e7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-372") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_VALUE_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK82nwedxu2puppkbvcn5xj3m15", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-373") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FK82uvw0fbs7p68ocv156ous0ho", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-374") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "RULE_INFORMATION", constraintName: "FK82y4skeo5y3n493ropl4b6ior", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-375") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "ALERTS", constraintName: "FK831h6rebhwqcu11l3bmv3m9gt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-376") {
        addForeignKeyConstraint(baseColumnNames: "PV_USER_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK88o1kq939jqa3ujho728e9hj6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-377") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK8afup0gx565jdbopkd4b9xalg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-378") {
        addForeignKeyConstraint(baseColumnNames: "income_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK8d35ldteverucbekx4ueliwxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-379") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "topic_actions", constraintName: "FK8ewu1k8gy0uup9i3l7edt5u0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-380") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_GROUP", constraintName: "FK8i6w6mah3h65hjsr0xiwwj247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-381") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FK8iktftscl552215n7hl8xr46m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-382") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_ALERT_QUERY_VALUES", constraintName: "FK8ipmwav0ngw8l0nwefligk05i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-383") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_SIGNAL_CATEGORY", constraintName: "FK8j3m9m71xssnstcr7sadeg87f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-384") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK8jqoqf0lhagmub5an0yxoma3g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-385") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK8l9hjbep49n1p5jgt1lwktf1x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-386") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "meeting_activities", constraintName: "FK8o8dfpgm739ed1u78v767vsvt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-387") {
        addForeignKeyConstraint(baseColumnNames: "ROW_COLS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK8p9endljdo1ex5s0am5h174cw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-388") {
        addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK8v8dg0fy3u8lq3l3u4c4u0mcu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-389") {
        addForeignKeyConstraint(baseColumnNames: "SQL_TEMPLT_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK8wopocxfmf15ej1v33p2twnc6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-390") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FK923jql3sdm5jk6ht1an9nn7ov", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-391") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "TOPIC", constraintName: "FK94byri2xuxp9jqxhjmvb4a8x2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-392") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK993e2h8qmb5k5m9d2oo2vh3i8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-393") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK99lpraxpe9sb9vixs14twpoi9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-394") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FK9h7pd11wvccr6lf5yge61layr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-395") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FK9k5fv2tt7vtl5d0rce4f46ws", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-396") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK9lmmhn29xffgygx6iaduvqmqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-397") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUPS", constraintName: "FK9nm9nx0a6hcur0207kh6k5dn9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-398") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ALERTS", constraintName: "FK9nmky66wnq1cefm2snii3dsn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-399") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FK9q2pi8025jxe8bbgtyldlp6tp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-400") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK9tk3chwgfb3ci8vloblnxd53b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-401") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "RCONFIG_DISPOSITION", constraintName: "FK9v26sfjmamkwv5h7yyh7k8vop", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-402") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK9vmxhc2kgh0i6pcw4jqcdm819", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-403") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK9xhb2aa0o52td9v65k84gpvb1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-404") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "RCONFIG", constraintName: "FKa2kbegt02usojx08bka5tlcqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-405") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FKa3cqvwnj1j7q2yi5coqlp2br4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-406") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIONS", constraintName: "FKa3rycbytmp5ea3n5dxc5hiaxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TYPES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-407") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa5gt5n32yb0s455yd36wvutkd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-408") {
        addForeignKeyConstraint(baseColumnNames: "advanced_filter_id", baseTableName: "view_instance", constraintName: "FKa5uu337r7cfmxb18x68709tf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ADVANCED_FILTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-409") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_ALERT", constraintName: "FKa87y6kvqdbxkl5gef3rft9cl0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-410") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa8rlw0012wytmrfayjlx3bw0e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-411") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKaa2wqfteyjdl906bs3t05k0s8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-412") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "test_user_role", constraintName: "FKacxggj2twirtdb9ysv9g5q2sa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_saml_user")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-413") {
        addForeignKeyConstraint(baseColumnNames: "EX_EVDAS_CONFIG_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKahisevpydu0thicdyjm49v5w9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-414") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "TOPIC_GROUP", constraintName: "FKaijamtneohj273nu1ncurw295", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-415") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKaims0i1suspmoa1et17ll8g86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-416") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FKalinqp5i92dk44sa1l98og467", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-417") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FKant7fchwn6ya4i1yvhgqt8djc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-418") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKayc7ooteg4uqk1y8oth5gi85w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-419") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKb004pyaimrqt9rvj6i1vcu382", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-420") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKb1h9h6gsjltr4to435axmlqba", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-421") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_STRATEGY_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FKb309drlgw2wgk40cmpq4iqqmo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-422") {
        addForeignKeyConstraint(baseColumnNames: "EX_ALERT_DATE_RANGE_ID", baseTableName: "EX_RCONFIG", constraintName: "FKb5kjpnalh2rbc3x34i1aw9073", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_ALERT_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-423") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RFI_LIST_ID", baseTableName: "DTAB_COLUMN_MEASURE", constraintName: "FKb61pwfq5cj72hri56yej80i0m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-424") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "evdas_file_process_log", constraintName: "FKb63thlw61wkcwi0uebhwttj0c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-425") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKb790uhowictwtiygfm6n9wsrs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-426") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_STATUSES_SHARED_WITHS", constraintName: "FKb83gyva8nj5ctoykqkov8ongn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-427") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKbaasjf1q5jdh8yb9dgo2e5c5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-428") {
        addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKbbo1adbmuh5tbw11h9j1m54rg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-429") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKbbsstpmcsh6gpgf7v3rxr5tn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-430") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "LITERATURE_CONFIG", constraintName: "FKbh94j1hyx8woow3tuhfpw771n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-431") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "RCONFIGS_PROD_GRP", constraintName: "FKbhhfbf7noaxk5m7dv8fs6vhry", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-432") {
        addForeignKeyConstraint(baseColumnNames: "SCHEDULED_PVUSER_ID", baseTableName: "RPT_RESULT", constraintName: "FKbmfelgea61vxim4d9lodgmw3r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-433") {
        addForeignKeyConstraint(baseColumnNames: "shared_with_id", baseTableName: "ALERTS", constraintName: "FKboxvx12d3pno2w2v4vqcwr3y3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-434") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKbwna09ojl0fcs9fpgv65fnc14", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-435") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_safety_groups", constraintName: "FKc046tqmqedqp9141fa2soxx7x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-436") {
        addForeignKeyConstraint(baseColumnNames: "USER_GROUP_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FKc9jts755p0p0fmxn0ppc276aw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-437") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKcf0w313ha302l1ybaf3vy5d5d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-438") {
        addForeignKeyConstraint(baseColumnNames: "NONCASE_SQL_TEMPLT_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKci1t92wr5cug7k5o1y9u2e9v5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-439") {
        addForeignKeyConstraint(baseColumnNames: "target_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcms27yedf7sdog3u36hlqg9iu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-440") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_group_s", constraintName: "FKcrqry31n9mka1oh51bhsn8r99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-441") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKcuf5j28ivfuw9jkg1mlthxkbn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-442") {
        addForeignKeyConstraint(baseColumnNames: "incoming_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcwneoxfjkmjy0q8v73phwc63n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-443") {
        addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKd2i9nh4hjfnjt0d3hf56kml3h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-444") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKd6owieejvfmafwh8dyofkenut", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-445") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FKd74cpexfn1jrxsjhy23g3kqjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-446") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FKd9ws3gvh9jkokbmx8qej0nt7q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-447") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "CASE_HISTORY", constraintName: "FKdhqqeepqe5rbesr70u9gw7ktm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-448") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FKdk2ms0bbvlg9wt85bl3h172c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-449") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKdr8517c590a2tx7da58eb0med", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-450") {
        addForeignKeyConstraint(baseColumnNames: "ATTACHMENTS_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FKdujjes8cvidp8xttfhe9yltmx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "FILE_ATTACHMENTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-451") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKdumjbc9ixhbyvwdgivdcqbcgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-452") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKdvuy2bffdvtxtcyne7yvm2ufl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-453") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVAL_METHOD", constraintName: "FKe0ksh1e8gfwkuuc38rs6xbavk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-454") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ACTIONS", constraintName: "FKe2e0g80hg3uajb4mb48d1k92y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-455") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FKe3ogu1iyyv0d8jxiu1m5nu1dc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-456") {
        addForeignKeyConstraint(baseColumnNames: "lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKe4a3tfxg6bd0wg0b7pvfmp0pk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-457") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ALERTS", constraintName: "FKe4dcmnjtoep5k5c638sllylqu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-458") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_RCONFIG", constraintName: "FKe9j0svwa1r5mwvgm3bf5m6eq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-459") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIVITIES", constraintName: "FKeaneople79bqyu7x49t5reovi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-460") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "MEETING", constraintName: "FKef6s7ftgt9xb8fvpsp5lbuxrc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-461") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKehg8x540mv0qpy5plqyf7ktqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-462") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKek8566hlirn551hgcxfl9fbpl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-463") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ALERTS", constraintName: "FKel2n4td5vn9pt0x6tmxjkq1o0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-464") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKenxk6e969lecixm4isf1svn0p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-465") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKeq4e569rluxwh2bh0fil7efp8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-466") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "DISPO_RULES_SIGNAL_CATEGORY", constraintName: "FKeslqouxq7266lck0ouermoo0b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-467") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FKf1ryj8ajrpjphnyv68dq5bll2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-468") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "RCONFIG", constraintName: "FKf4d2b2hthl4s6fvbvq0uas5av", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-469") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "work_flow_rules_actions", constraintName: "FKf7jgah5sul11a45tsbuggvek4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-470") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKf8kqtv3nu1fgrdlcikfp4nfcm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-471") {
        addForeignKeyConstraint(baseColumnNames: "meeting_actions_id", baseTableName: "meeting_actions", constraintName: "FKf8ro1wl6geleojkypcnjercbp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-472") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_DELIVERIES_SHARED_WITHS", constraintName: "FKffoorb58n2mlt1sjcg481t97u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-473") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "report_history", constraintName: "FKffw3i3fbs6jyf8kqojxola39s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-474") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_CATEGORY_ID", baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FKfmldojgpely7wlug6e2l6q4oo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-475") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "user_group_s", constraintName: "FKfpfe7d6ea5bnivbe7gnc03622", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-476") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKfubdtk7gwoa8s8919uafl2jj3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-477") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKfvp89jp3dlfpaymistjk1tedp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-478") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKfwtfcguybm93tkxxgtbkf3yn0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-479") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FKg1yueio2f2400ouiv8y29wrji", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-480") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "signal_history", constraintName: "FKg2etf80lqk7p2chk6848imhyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-481") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_TEMPLATE_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FKg7jny08nmnya6p6b4o7fxxpyq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-482") {
        addForeignKeyConstraint(baseColumnNames: "CATEGORY_ID", baseTableName: "RPT_TEMPLT", constraintName: "FKgcan6gm4h4l0i5xg5qppa4247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "CATEGORY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-483") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "meeting_actions", constraintName: "FKgd1k2pcfls85gbq25kdko2lm6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-484") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKgqh0iu7w66xqgio4jv38b5hmf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-485") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DOCUMENT_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FKguaxlxc330u6by2rdbpa7o15s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-486") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DATA_RANGE_ID", baseTableName: "RCONFIG", constraintName: "FKguyscui10qub13216q7omwk6r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-487") {
        addForeignKeyConstraint(baseColumnNames: "attachment_id", baseTableName: "ATTACHMENT_DESCRIPTION", constraintName: "FKhf1vmfljes8qfvmkksj0r56gr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-488") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKhfddlxpqvfaocje2iwp6umh63", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-489") {
        addForeignKeyConstraint(baseColumnNames: "guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKhgeofgkdx1rjj5s2yglnetx6y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GUEST_ATTENDEE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-490") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKhhjqmaurc06u0g2sd8pk0puw3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-491") {
        addForeignKeyConstraint(baseColumnNames: "meeting_owner_id", baseTableName: "MEETING", constraintName: "FKhipac92y3mafijg8snr452hkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-492") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKhjr0k31x2dckpqrnrq4u8hffp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-493") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "AGG_ALERT", constraintName: "FKhp8lobw060slxq44dpqk23aq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-494") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKhqjqjxidasmpsaf9jsa1m6fkf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-495") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "ALERT_QUERY_VALUES", constraintName: "FKhv09gbc0ogxdov8llx7jt5961", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-496") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKhycn6c1tko90ojyv3m9q92b4b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-497") {
        addForeignKeyConstraint(baseColumnNames: "audit_trail_id", baseTableName: "audit_child_log", constraintName: "FKi2lq3l44yxa3o6afk12xj36ip", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audit_log")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-498") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FKid2pii4l2042xd6m3si3yav58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-499") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FKig3y20w0203iai5v122a4em5e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-500") {
        addForeignKeyConstraint(baseColumnNames: "signal_strategy_id", baseTableName: "PV_CONCEPT", constraintName: "FKii9w7n9su0kelhwdy64fnoh4a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-501") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "COMMENTS", constraintName: "FKipm49nd1i0adasdsh4c4pkc88", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "COMMENTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-502") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "product_event_history", constraintName: "FKit1j3spl8apjv7adws38jn99a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-503") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKitf2xvh5yq2hykg0o1ff2yi6k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-504") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "SIGNAL_CHART", constraintName: "FKiwek5gp0875l735x1kt69iaq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-505") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ALERTS", constraintName: "FKixthhci8k983scn1rxj2vrtar", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-506") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "evdas_history", constraintName: "FKj0e4kr6h7ba4h1j66a0qsvwx6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-507") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FKj0jkrus3o90ljhg1b6ebuttyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-508") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RPT_TEMPLTS_TAGS", constraintName: "FKj3em09hnn5j4v4jmtva0y7x4t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-509") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKjdbvyyl4pc5gyonimroq4q9vp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-510") {
        addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FKjf5iat84x47a3wdllmpj3qa8r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-511") {
        addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "SHARED_WITH", constraintName: "FKjpnhmuynt25rwtymb8c6qbcu2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-512") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKjqg0j3akc948s35kbuqglqjqi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-513") {
        addForeignKeyConstraint(baseColumnNames: "VALUE_ID", baseTableName: "VALUES_PARAMS", constraintName: "FKjyc9aqn1mnratmb78qts1vh2x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-514") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKk2v2qa969crmd21cb8ey8eykl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-515") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUPS", constraintName: "FKk93ggugty4vt6eff2xct44qvs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-516") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKkdxec3jbhkxo588a4kpg4h9l4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-517") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_RCONFIG", constraintName: "FKktk446ckoiyq9ccb0k4k1fa0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-518") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKkwhfmdea1iw8ad05voiu1c88x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-519") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKl5ki9ms4pq7oyr4wrne2wejf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-520") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "evdas_history", constraintName: "FKl751la1u7svv60tmydb5q6rfo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-521") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "RULE_INFORMATION", constraintName: "FKlfungwscmq9bwj56xlirfguy1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-522") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKlhyif0bp06538q34cbg7o6ni", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-523") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_actions_id", baseTableName: "validated_signal_actions", constraintName: "FKlqeb869hd13sihhd6a99xq2he", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-524") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "ACTIVITIES", constraintName: "FKls2t8mye4vopk1sjq1tgy9xoc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-525") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "signal_history", constraintName: "FKludcb63lf92hniiym7xpdxhfa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-526") {
        addForeignKeyConstraint(baseColumnNames: "meeting_activities_id", baseTableName: "meeting_activities", constraintName: "FKlxb7shjcemeke6qig312nemnv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-527") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_DATA_ID", baseTableName: "RPT_RESULT", constraintName: "FKly7i8dpff5f3c218d99g8tcyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT_DATA")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-528") {
        addForeignKeyConstraint(baseColumnNames: "meeting_guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKm139ii2baukit0klasjx4oglf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-529") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKm2082jiexg6r0a70a2r34utlj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-530") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "RPT_FIELD", constraintName: "FKmahmpsyseeqtjochaf36fxyap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-531") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKmci0hn62phky4qlig0uy8walr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-532") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIVITIES", constraintName: "FKmeoolraf0sivqb157mg1xxxhn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-533") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_ERROR", constraintName: "FKmkyr5aidjfp14wd8ytgv48xdn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-534") {
        addForeignKeyConstraint(baseColumnNames: "COLUMN_MEASURE_ID", baseTableName: "DTAB_TEMPLTS_COL_MEAS", constraintName: "FKmop226wsllt5fkw8ldhbio60u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_COLUMN_MEASURE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-535") {
        addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_COL_MEAS_MEASURES", constraintName: "FKmowrqjxvrwjdeg7wh33rdsi7e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_MEASURE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-536") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKmq50wqqf7s1y3ec4mdllb6ueu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-537") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKmv3t9bke7ovuq4xhntvupq5b4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-538") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKmw7ferd2w1sq8d034q2a2dare", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-539") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKmx3gs9bdqe5we8t8dyfwlnu57", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-540") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn52svyiofwkw5lpse0bi7tiup", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-541") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn6ofry9re33lb76h4npt3cjr0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-542") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKn6vycx5pad3o4yv5lqgyg9b0k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-543") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKncksxdxhk9k1wg170uk6hxo01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-544") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKneg18gip2c5vrkf5o5d6hjvp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-545") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKnf31xnc5tocj5toxf64f4n5bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-546") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FKnfgumbuu5ryminkxap7nlgc5p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-547") {
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVUSER", constraintName: "FKnjdqpgxtqa9kv92hc0h5a08nh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PREFERENCE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-548") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "AGG_ALERT", constraintName: "FKnpwnuktj5h2nepfrniywuihe2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-549") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "meeting_pvuser", constraintName: "FKnrjk7qtd13c4vbk8cf2fng3c2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-550") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKnu30tvrow0gcfq8mug780icdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-551") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "RCONFIG", constraintName: "FKnwf5n98xlybm9cmlufdikkb31", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-552") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FKnwuo5xfk46w6axjqsd3so9ta4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-553") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "RCONFIG", constraintName: "FKnxr5jgx0rmqc0qwpvfb1hxedb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-554") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FKo3yoly6j5mslepff4epm8i3un", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-555") {
        addForeignKeyConstraint(baseColumnNames: "literature_alert_id", baseTableName: "ACTIONS", constraintName: "FKo8vf0yxsu6jv9d9xddv9owyxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-556") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKodukke9wren1g3gjo55qi9tgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-557") {
        addForeignKeyConstraint(baseColumnNames: "user_department_id", baseTableName: "pvuser_user_department", constraintName: "FKohxfvcmtidnanlyvlj3qq9hyt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_department")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-558") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKop4ektpb6ulll4ubcyvxd2352", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-559") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKopm5twbskg380tovcrs4r2gxs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-560") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "product_event_history", constraintName: "FKoqw80p3b38v6stbo0ic2u08vf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-561") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKorl7uf55p01t8h6cb6jvuc4r0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-562") {
        addForeignKeyConstraint(baseColumnNames: "evdas_alert_id", baseTableName: "ACTIONS", constraintName: "FKotw99rdoeydidva8eqngeoim1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-563") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_DOCUMENT", constraintName: "FKou8nl9rcuc35kn5yuucm1jfso", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-564") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKoxf0kmlxo6jbssodqw9a20l72", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-565") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKp6qbm114tnjnrpkfb5lpvp82k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ROLE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-566") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIONS", constraintName: "FKp8d85ni1hck589sq6qbxtk2ur", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-567") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKpaayw5smfn63cqc92ncfgtcwk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-568") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_ALERT", constraintName: "FKpb61fes9spi2lu1n9k4oq9m05", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-569") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIVITIES", constraintName: "FKpb7op1gsmnueani37t07swp18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-570") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "signal_history", constraintName: "FKpc3meb0lkklinhdq8yvhcc094", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-571") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "EX_RCONFIGS_PROD_GRP", constraintName: "FKpiydv7gsh3jat1r6upba3jfsl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-572") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "AGG_ALERT", constraintName: "FKpj612eglsaa2cqi8ds4c3hs3c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-573") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FKpjt02j3shpuy0tenek9p1ol40", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-574") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "MEETING", constraintName: "FKpkleruks8op5rxe14p0u28dun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-575") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FKpr2qa0uljoummmnormrftubha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-576") {
        addForeignKeyConstraint(baseColumnNames: "CONFIG_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKptphnx7oype1403pwd2wk9963", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-577") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_STATUS", constraintName: "FKptxxpv688i41soevklto2001n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-578") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKpw1bhalfblwu0e31df1oib0cr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-579") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FKq24b4km13myw6pxki435bulke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-580") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "validated_signal_actions", constraintName: "FKq4785d5esdy86x6prso7o75el", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-581") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKq8lv3dxpesh2l167udqs606vn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-582") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "ex_rconfig_activities", constraintName: "FKqaijpt3i6wpk4o7f4j9iofrqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-583") {
        addForeignKeyConstraint(baseColumnNames: "single_case_alert_id", baseTableName: "ACTIONS", constraintName: "FKqcrth3pehaa07ed4hf83abyp4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-584") {
        addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "CASE_HISTORY", constraintName: "FKqh491q7pp8sjet2ep7jy7ilda", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-585") {
        addForeignKeyConstraint(baseColumnNames: "meeting_attendees_id", baseTableName: "meeting_pvuser", constraintName: "FKqiltr5xajp4m5fu0je6set5g3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-586") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKqj7ub0xptnbuhouvowxamuj26", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-587") {
        addForeignKeyConstraint(baseColumnNames: "log_id", baseTableName: "IMPORT_DETAIL", constraintName: "FKqkcg7dewuxqjklxa4xuo3yb6w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "IMPORT_LOG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-588") {
        addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "CASE_HISTORY", constraintName: "FKqqc5icyo2ky7wkegjktf5inwd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-589") {
        addForeignKeyConstraint(baseColumnNames: "agg_alert_id", baseTableName: "ACTIONS", constraintName: "FKqqmeb3p8bx7819mcuweun42qu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-590") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKqry992h07weddj9bprvrhjb3j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-591") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKqtymlypfnndkl92xpmlyn6wgi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-592") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKr2ep7rrgudpgn2aeip674gffo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-593") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKr2gmw2pc2lgkyxy708wuv8u5b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-594") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ALERTS", constraintName: "FKr339y18wo70qkphso8k28p4n5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-595") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FKr4dvi6dx2akkxj6sg1edfu4y8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-596") {
        addForeignKeyConstraint(baseColumnNames: "product_dictionary_cache_id", baseTableName: "ALLOWED_DICTIONARY_CACHE", constraintName: "FKr4nitfmbmp9dht2pk246d68e9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-597") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKrepui5ibyqyi0ylkhdmrggn9x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-598") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "pvuser_safety_groups", constraintName: "FKriwpg5obw3hvucl89nm7cyfg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-599") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "ALERT_TAG", constraintName: "FKrrbo69cgkt2oof0qend38karl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-600") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "RCONFIG", constraintName: "FKrwhfg2lf7qokjhuat482craww", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-601") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_ALERT", constraintName: "FKrx1ksfdkal8plsauewx9wcoje", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-602") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_group_id", baseTableName: "CASE_HISTORY", constraintName: "FKs60gq6t7tjj3hsunqhur4w35a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-603") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKs64fr0mhkeoburd4i5hvvtmxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-604") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKs6fht51c12chh1glukanj1vuu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-605") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_RCONFIG", constraintName: "FKscrg3o7ey1xr3ac0oldqvq1cg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-606") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ALERTS", constraintName: "FKsg3649m44b1j9j7kg3saeacwx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-607") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLATE_ID", baseTableName: "RCONFIG", constraintName: "FKsh216m1mb3g41hm0h6w12wh12", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-608") {
        addForeignKeyConstraint(baseColumnNames: "ex_lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKsm5fds0flxc0po7eqgc5gqgpj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-609") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FKsp010pdhbqal79g9q1y3kl5v3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-610") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKsqppqusike0nnjet4x4elrkom", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-611") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKsy5o19eqrc5385sfoidonp43l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-612") {
        addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKsysb0klybb1kk1o3f5auerjx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-613") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKt2b7h0ovgst4owagdj3bhj57t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-614") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "validated_signal_action_taken", constraintName: "FKt5h2kt8e2kf76xpxs11tw98ek", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-615") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIONS", constraintName: "FKt8jxq56nworpeaop4oarjkmpy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-616") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "ALERTS", constraintName: "FKtawq2r3by3tlx2bx8ohm6ssdb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-617") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_ACTION_TAKEN", constraintName: "FKte7ljx7lhewxd219su74acrd7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-618") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKtirjstq25k3s016xchl6imgv8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-619") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKtlr3lhis3k57s65e2wrfm6tpx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-620") {
        addForeignKeyConstraint(baseColumnNames: "user_user_departments_id", baseTableName: "pvuser_user_department", constraintName: "FKtmsfftkx8df5gdlnxgjyub76b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-621") {
        addForeignKeyConstraint(baseColumnNames: "ROWS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FKxuj1f2muxnhq2fgcqbxuycjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-622") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "product_event_history", constraintName: "FKy156nyus7pkpemc1lk9wuy73", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-623") {
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-624") {
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-625") {
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-626") {
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-627") {
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-628") {
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-629") {
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-630") {
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-631") {
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-632") {
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-633") {
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-634") {
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-635") {
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-636") {
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-637") {
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-638") {
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-639") {
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-640") {
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-641") {
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552329383420-642") {
        dropTable(tableName: "HT_VALUE")
    }
}
