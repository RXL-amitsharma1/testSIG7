databaseChangeLog = {

    changeSet(author: "chetansharma (generated)", id: "1517200204498-1") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-2") {
        createTable(tableName: "AD_HOC_SIGNAL_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-3") {
        createTable(tableName: "AD_HOC_TOPIC_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-4") {
        createTable(tableName: "AGG_SIGNAL_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-5") {
        createTable(tableName: "AGG_TOPIC_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-6") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-7") {
        createTable(tableName: "EVDAS_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVDAS_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "all_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "asia_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "attributes", type: "CLOB")

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

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "hlgt", type: "VARCHAR2(255 CHAR)")

            column(name: "hlt", type: "VARCHAR2(255 CHAR)")

            column(name: "japan_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "new_abuse", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ct", type: "VARCHAR2(255 CHAR)")

            column(name: "new_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "new_ev", type: "NUMBER(10, 0)")

            column(name: "new_fatal", type: "NUMBER(10, 0)")

            column(name: "new_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "new_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "new_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "new_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "new_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "new_occup", type: "VARCHAR2(255 CHAR)")

            column(name: "new_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "new_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "new_roa1", type: "VARCHAR2(255 CHAR)")

            column(name: "new_serious", type: "NUMBER(10, 0)")

            column(name: "new_spon", type: "VARCHAR2(255 CHAR)")

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

            column(name: "rest_ror", type: "VARCHAR2(255 CHAR)")

            column(name: "roa1", type: "VARCHAR2(255 CHAR)")

            column(name: "ror_value", type: "VARCHAR2(255 CHAR)")

            column(name: "sdr", type: "VARCHAR2(255 CHAR)")

            column(name: "smq_narrow", type: "VARCHAR2(255 CHAR)")

            column(name: "soc", type: "VARCHAR2(255 CHAR)")

            column(name: "substance", type: "VARCHAR2(255 CHAR)")

            column(name: "substance_id", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "tot_abuse", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_ct", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_eea", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_geria", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_hcp", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_med_err", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_obs", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_occup", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_paed", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_rc", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_roa1", type: "VARCHAR2(255 CHAR)")

            column(name: "tot_spon", type: "VARCHAR2(255 CHAR)")

            column(name: "total_ev", type: "NUMBER(10, 0)")

            column(name: "total_fatal", type: "NUMBER(10, 0)")

            column(name: "total_lit", type: "VARCHAR2(255 CHAR)")

            column(name: "total_serious", type: "NUMBER(10, 0)")

            column(name: "workflow_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-8") {
        createTable(tableName: "EVDAS_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVDAS_CONFIGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "description", type: "VARCHAR2(255 CHAR)")

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

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-9") {
        createTable(tableName: "EVDAS_CONFIGURATION_GROUPS") {
            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-10") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-11") {
        createTable(tableName: "EVDAS_SIGNAL_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-12") {
        createTable(tableName: "EVDAS_TAGS") {
            column(name: "EVDAS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-13") {
        createTable(tableName: "EVDAS_TOPIC_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-14") {
        createTable(tableName: "EX_EVDAS_CONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_EVDAS_CONFIGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "adhoc_run", type: "NUMBER(1, 0)")

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

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

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

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-15") {
        createTable(tableName: "EX_EVDAS_CONFIG_ACTIVITIES") {
            column(name: "EX_EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-16") {
        createTable(tableName: "EX_EVDAS_CONFIG_TAGS") {
            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-17") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-18") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-19") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-20") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-21") {
        createTable(tableName: "MEETING_ATTACHMENTS") {
            column(name: "MEETING_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ATTACHMENTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-22") {
        createTable(tableName: "PRODUCT_DICTIONARY_CACHE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "PRODUCT_DICTIONARY_CACHEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_families", type: "CLOB")

            column(name: "allowed_ingredients", type: "CLOB")

            column(name: "allowed_licenses", type: "CLOB")

            column(name: "allowed_products", type: "CLOB")

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-23") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-24") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-25") {
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

            column(name: "pvs_state_id", type: "NUMBER(19, 0)")

            column(name: "RULES", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "rule_rank", type: "NUMBER(10, 0)")

            column(name: "signal", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-26") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-27") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-28") {
        createTable(tableName: "SIGNAL_REPORT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SIGNAL_REPORTPK")
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

            column(name: "excel_report", type: "BLOB")

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

            column(name: "report_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "word_report", type: "BLOB")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-29") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-30") {
        createTable(tableName: "SINGLE_SIGNAL_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-31") {
        createTable(tableName: "SINGLE_TOPIC_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-32") {
        createTable(tableName: "STRATEGY_MEDICAL_CONCEPTS") {
            column(name: "SIGNAL_STRATEGY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-33") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-34") {
        createTable(tableName: "TOPIC_ACTIVITIES") {
            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-35") {
        createTable(tableName: "TOPIC_ADHOC_ALERTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-36") {
        createTable(tableName: "TOPIC_AGG_ALERTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-37") {
        createTable(tableName: "TOPIC_COMMENTS") {
            column(name: "COMMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-38") {
        createTable(tableName: "TOPIC_GROUP") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-39") {
        createTable(tableName: "TOPIC_SINGLE_ALERTS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-40") {
        createTable(tableName: "VALIDATED_ADHOC_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-41") {
        createTable(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-42") {
        createTable(tableName: "VALIDATED_ALERT_ACTIVITIES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-43") {
        createTable(tableName: "VALIDATED_ALERT_COMMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-44") {
        createTable(tableName: "VALIDATED_ALERT_DOCUMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_DOCUMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-45") {
        createTable(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-46") {
        createTable(tableName: "VALIDATED_SIGNAL") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "VALIDATED_SIGNALPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_template_id", type: "NUMBER(19, 0)")

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

            column(name: "EVENTS", type: "CLOB")

            column(name: "generic_comment", type: "CLOB")

            column(name: "include_in_aggregate_report", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "initial_data_source", type: "VARCHAR2(1000)")

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

            column(name: "start_date", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "strategy_id", type: "NUMBER(19, 0)")

            column(name: "workflow_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-47") {
        createTable(tableName: "VALIDATED_SIGNAL_CATEGORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-48") {
        createTable(tableName: "VALIDATED_SIGNAL_GROUP") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-49") {
        createTable(tableName: "VALIDATED_SINGLE_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-50") {
        createTable(tableName: "WORKFLOWRULES_ACTION_TEMPLATES") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TEMPLATE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-51") {
        createTable(tableName: "WORKFLOWRULES_SIGNAL_CATEGORY") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-52") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-53") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-54") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-55") {
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

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "change", type: "VARCHAR2(255 CHAR)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

            column(name: "disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-56") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-57") {
        createTable(tableName: "meeting_actions") {
            column(name: "meeting_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-58") {
        createTable(tableName: "meeting_activities") {
            column(name: "meeting_activities_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activity_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-59") {
        createTable(tableName: "meeting_guest_attendee") {
            column(name: "meeting_guest_attendee_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-60") {
        createTable(tableName: "meeting_pvuser") {
            column(name: "meeting_attendees_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-61") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-62") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-63") {
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

            column(name: "state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "validated_signal_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-64") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-65") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-66") {
        createTable(tableName: "topic_actions") {
            column(name: "topic_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-67") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-68") {
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

    changeSet(author: "chetansharma (generated)", id: "1517200204498-69") {
        createTable(tableName: "validated_signal_actions") {
            column(name: "validated_signal_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-70") {
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "ADVANCED_SORTING", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-71") {
        addColumn(tableName: "CLL_TEMPLT") {
            column(name: "COL_SHOW_DISTINCT", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-72") {
        addColumn(tableName: "PREFERENCE") {
            column(name: "DASHBOARD_CONFIG_JSON", type: "clob")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-73") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "FIXED_WIDTH", type: "number(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-74") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "GROUP_BY_SMQ", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-75") {
        addColumn(tableName: "RCONFIG") {
            column(name: "GROUP_BY_SMQ", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-76") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "IS_EUDRAFIELD", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-77") {
        addColumn(tableName: "RPT_FIELD_GROUP") {
            column(name: "IS_EUDRAFIELD", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-78") {
        addColumn(tableName: "SOURCE_COLUMN_MASTER") {
            column(name: "IS_EUDRAFIELD", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-79") {
        addColumn(tableName: "SOURCE_TABLE_MASTER") {
            column(name: "IS_EUDRAFIELD", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-80") {
        addColumn(tableName: "SUPER_QUERY") {
            column(name: "IS_EUDRA_QUERY", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-81") {
        addColumn(tableName: "SUPER_QUERY") {
            column(name: "NON_VALID_CASES", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-82") {
        addColumn(tableName: "NOTIFICATION") {
            column(name: "NOTIFICATION_USER_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-83") {
        addColumn(tableName: "PVUSER") {
            column(name: "OUTLOOK_ACCESS_TOKEN", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-84") {
        addColumn(tableName: "PVUSER") {
            column(name: "OUTLOOK_REFRESH_TOKEN", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-85") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "POST_QUERY_PROCEDURE", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-86") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "PRE_QUERY_PROCEDURE", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-87") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "PRE_REPORT_PROCEDURE", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-88") {
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "PRODUCTS", type: "clob") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-89") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PRR_STR", type: "clob")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-90") {
        addColumn(tableName: "CLL_TEMPLT") {
            column(name: "QUERY", type: "clob")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-91") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ROR_STR", type: "clob")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-92") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "WIDTH_PROPORTION_INDEX", type: "number(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-93") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "adhoc_run", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-94") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "adhoc_run", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-95") {
        addColumn(tableName: "RCONFIG") {
            column(name: "adhoc_run", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-96") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "adhoc_run", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-97") {
        addColumn(tableName: "audit_log") {
            column(name: "application_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-98") {
        addColumn(tableName: "DISPOSITION") {
            column(name: "closed", type: "number(1, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-99") {
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "data_source", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-100") {
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "description", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-101") {
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "enabled", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-102") {
        addColumn(tableName: "audit_log") {
            column(name: "entity_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-103") {
        addColumn(tableName: "audit_log") {
            column(name: "entity_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-104") {
        addColumn(tableName: "audit_log") {
            column(name: "entity_value", type: "VARCHAR2(2000)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-105") {
        addColumn(tableName: "ACTIONS") {
            column(name: "evdas_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-106") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "format", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-107") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "frequency", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-108") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "frequency", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-109") {
        addColumn(tableName: "RCONFIG") {
            column(name: "frequency", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-110") {
        addColumn(tableName: "audit_log") {
            column(name: "fullname", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-111") {
        addColumn(tableName: "PREFERENCE") {
            column(defaultValueBoolean: "true", name: "is_cumulative_alert_enabled", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-112") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "listed", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-113") {
        addColumn(tableName: "ACTIONS") {
            column(name: "meeting_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-114") {
        addColumn(tableName: "product_event_history") {
            column(name: "new_count", type: "number(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-115") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "pec_imp_high", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-116") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "pec_imp_low", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-117") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "period_end_date", type: "timestamp")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-118") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "period_end_date", type: "timestamp")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-119") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "period_start_date", type: "timestamp")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-120") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "period_start_date", type: "timestamp")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-121") {
        addColumn(tableName: "audit_log") {
            column(name: "persisted_object_version", type: "number(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-122") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "positive_dechallenge", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-123") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "positive_rechallenge", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-124") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "pregenency", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-125") {
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "product_dictionary_selection", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-126") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "product_dictionary_selection", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-127") {
        addColumn(tableName: "RCONFIG") {
            column(name: "product_dictionary_selection", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-128") {
        addColumn(tableName: "audit_log") {
            column(name: "property_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-129") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr05", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-130") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr95", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-131") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_mh", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-132") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_str05", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-133") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_str95", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-134") {
        addColumn(tableName: "ALERTS") {
            column(name: "reference_number", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-135") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "reference_number", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-136") {
        addColumn(tableName: "RCONFIG") {
            column(name: "reference_number", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-137") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "related", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-138") {
        addColumn(tableName: "PVS_STATE") {
            column(name: "review_period", type: "number(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-139") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror05", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-140") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror95", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-141") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_mh", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-142") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_str05", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-143") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_str95", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-144") {
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "rule_name", type: "varchar2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-145") {
        addColumn(tableName: "audit_log") {
            column(name: "sent_on_server", type: "number(1, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-146") {
        addColumn(tableName: "WORK_FLOW_RULES") {
            column(name: "signal_rule", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-147") {
        addColumn(tableName: "audit_log") {
            column(name: "uri", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-148") {
        createIndex(indexName: "IX_null", tableName: "user_group_s", unique: "true") {
            column(name: "user_id")

            column(name: "group_id")
        }

        addPrimaryKey(columnNames: "user_id, group_id", forIndexName: "IX_null", tableName: "user_group_s")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-149") {
        createIndex(indexName: "IX_VALIDATED_ADHOC_ALERTSPK", tableName: "VALIDATED_ADHOC_ALERTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "ADHOC_ALERT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, ADHOC_ALERT_ID", constraintName: "VALIDATED_ADHOC_ALERTSPK", forIndexName: "IX_VALIDATED_ADHOC_ALERTSPK", tableName: "VALIDATED_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-150") {
        createIndex(indexName: "IX_VALIDATED_AGG_ALERTSPK", tableName: "VALIDATED_AGG_ALERTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "AGG_ALERT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, AGG_ALERT_ID", constraintName: "VALIDATED_AGG_ALERTSPK", forIndexName: "IX_VALIDATED_AGG_ALERTSPK", tableName: "VALIDATED_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-151") {
        createIndex(indexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "COMMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, COMMENT_ID", constraintName: "VALIDATED_ALERT_COMMENTSPK", forIndexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-152") {
        createIndex(indexName: "IX_VALIDATED_ALERT_DOCUMENTSPK", tableName: "VALIDATED_ALERT_DOCUMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "ALERT_DOCUMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, ALERT_DOCUMENT_ID", constraintName: "VALIDATED_ALERT_DOCUMENTSPK", forIndexName: "IX_VALIDATED_ALERT_DOCUMENTSPK", tableName: "VALIDATED_ALERT_DOCUMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-153") {
        createIndex(indexName: "IX_VALIDATED_EVDAS_ALERTSPK", tableName: "VALIDATED_EVDAS_ALERTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "EVDAS_ALERT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, EVDAS_ALERT_ID", constraintName: "VALIDATED_EVDAS_ALERTSPK", forIndexName: "IX_VALIDATED_EVDAS_ALERTSPK", tableName: "VALIDATED_EVDAS_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-154") {
        createIndex(indexName: "IX_VALIDATED_SINGLE_ALERTSPK", tableName: "VALIDATED_SINGLE_ALERTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "SINGLE_ALERT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, SINGLE_ALERT_ID", constraintName: "VALIDATED_SINGLE_ALERTSPK", forIndexName: "IX_VALIDATED_SINGLE_ALERTSPK", tableName: "VALIDATED_SINGLE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-156") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_ACCESS_CTRL_GROUPNAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-157") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_ACTION_TEMPLATENAME_COL", tableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-163") {
        addUniqueConstraint(columnNames: "SCHEDULE_NAME", constraintName: "UC_ETL_SCHEDULE_NAME_COL", tableName: "ETL_SCHEDULE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-170") {
        addUniqueConstraint(columnNames: "report_name", constraintName: "UC_RPT_HISTORYRPT_NAME_COL", tableName: "report_history")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-177") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_VALIDATED_SIGNALNAME_COL", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-179") {
        addForeignKeyConstraint(baseColumnNames: "pvs_state_id", baseTableName: "RULE_INFORMATION", constraintName: "FK13b21d1j7ivojo0uelga0e95l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-180") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "signal_history", constraintName: "FK1gksukhgbnjjbm6tq7sergnf3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-181") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK1pvjhvvvh16fitf3f9aepcrx3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-182") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FK25gco89k379c8yngl2ixneunj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-183") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK2mrmhl81t6ttn1ga2kcbl8nc5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-184") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "SIGNAL_CHART", constraintName: "FK2vyvpc8474sber4f5uljnhef0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-185") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FK31jab223wuoin37mihjgmd5o3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-186") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3hq5p8kqtby4pli3sl769bqdq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-187") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3i2ij0470itt2oyfll08nuagc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-188") {
        addForeignKeyConstraint(baseColumnNames: "MEETING_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FK3kly0ngl8jptbdmurwpdhn15e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-189") {
        addForeignKeyConstraint(baseColumnNames: "business_configuration_id", baseTableName: "RULE_INFORMATION", constraintName: "FK46deuafhfurfdbrewcoueiogp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-190") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FK4e8ln2cbe20xp8afdh1y7sa9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-191") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "signal_history", constraintName: "FK4he7y9hi5lwrs2d8tn16rsxew", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-192") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK4jhhis76bsysx4xm6i6ioykpc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-193") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "TOPIC", constraintName: "FK4kh9et74csyh0sxnpty9v5bwf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-194") {
        addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "signal_history", constraintName: "FK52s7ra0yhx7nlww3j0soms7i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-195") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "TOPIC", constraintName: "FK557wy3htik1yrmw2ldt3lujx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-196") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FK58riu8smk8hl4hwwx89gr2v4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-197") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FK58ry6bymei63ycju6ms99buw5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-198") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK5bcrnlc25y3lkkm5d8xsqrb83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-199") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FK5xqn3nlelifk9hpih50rd7b3s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-200") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "evdas_history", constraintName: "FK67y86bymxk3lnavaj4ax1fmmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-201") {
        addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "evdas_history", constraintName: "FK6lcp6urq7u4nokl4c70rdnhdb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-202") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FK6o7whfp7w45byxkfkg3ajgy8d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-203") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK6xlkiqggquuh3unim09gl7s5k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-204") {
        addForeignKeyConstraint(baseColumnNames: "topic_actions_id", baseTableName: "topic_actions", constraintName: "FK6xwerddtt6xmysbwpqm0thskw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-205") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FK73rmou5rxoc3xenxf07klxnvl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-206") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FK743ertpl99bhh1sp7vd91asmi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-207") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "TOPIC", constraintName: "FK7fprf6132fra32pjukedntqyl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-208") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FK7rawb1uursvbk5hki32go9ifo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-209") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FK7uu820jqwt2ggpbwt5q1n3irg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-210") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "TOPIC", constraintName: "FK81ax335qvxywo42jwjpk2uj70", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-211") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FK82uvw0fbs7p68ocv156ous0ho", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-212") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "RULE_INFORMATION", constraintName: "FK82y4skeo5y3n493ropl4b6ior", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-213") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FK888dtfrh3hvmo8utwvc6sfuya", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-214") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "topic_actions", constraintName: "FK8ewu1k8gy0uup9i3l7edt5u0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-215") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_GROUP", constraintName: "FK8i6w6mah3h65hjsr0xiwwj247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-216") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FK8iktftscl552215n7hl8xr46m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-217") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "meeting_activities", constraintName: "FK8o8dfpgm739ed1u78v767vsvt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-218") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "TOPIC", constraintName: "FK94byri2xuxp9jqxhjmvb4a8x2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-219") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK993e2h8qmb5k5m9d2oo2vh3i8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-220") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK99lpraxpe9sb9vixs14twpoi9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-221") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FK9h7pd11wvccr6lf5yge61layr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-222") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FK9k5fv2tt7vtl5d0rce4f46ws", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-223") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK9lmmhn29xffgygx6iaduvqmqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-224") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK9vmxhc2kgh0i6pcw4jqcdm819", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-225") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_ALERT", constraintName: "FKa87y6kvqdbxkl5gef3rft9cl0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-226") {
        addForeignKeyConstraint(baseColumnNames: "EX_EVDAS_CONFIG_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKahisevpydu0thicdyjm49v5w9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-227") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "TOPIC_GROUP", constraintName: "FKaijamtneohj273nu1ncurw295", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-228") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FKalinqp5i92dk44sa1l98og467", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-229") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FKant7fchwn6ya4i1yvhgqt8djc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-230") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EVDAS_TAGS", constraintName: "FKau1wuyclgdhggx6leydf3942a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-231") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_STRATEGY_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FKb309drlgw2wgk40cmpq4iqqmo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-232") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "evdas_file_process_log", constraintName: "FKb63thlw61wkcwi0uebhwttj0c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-233") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKb790uhowictwtiygfm6n9wsrs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-234") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "EVDAS_ALERT", constraintName: "FKbl1p01g7xkc98cbthaqndcebh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-235") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKbwna09ojl0fcs9fpgv65fnc14", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-236") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKd6owieejvfmafwh8dyofkenut", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-237") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FKd9ws3gvh9jkokbmx8qej0nt7q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-238") {
        addForeignKeyConstraint(baseColumnNames: "ATTACHMENTS_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FKdujjes8cvidp8xttfhe9yltmx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "FILE_ATTACHMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-239") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKdumjbc9ixhbyvwdgivdcqbcgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-240") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKdvuy2bffdvtxtcyne7yvm2ufl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-241") {
        addForeignKeyConstraint(baseColumnNames: "action_template_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKe0p5nb3v4datmb7gxcdvah75o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-242") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FKe3ogu1iyyv0d8jxiu1m5nu1dc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-243") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "MEETING", constraintName: "FKef6s7ftgt9xb8fvpsp5lbuxrc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-244") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKek8566hlirn551hgcxfl9fbpl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-245") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKeq4e569rluxwh2bh0fil7efp8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-246") {
        addForeignKeyConstraint(baseColumnNames: "meeting_actions_id", baseTableName: "meeting_actions", constraintName: "FKf8ro1wl6geleojkypcnjercbp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-247") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "report_history", constraintName: "FKffw3i3fbs6jyf8kqojxola39s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-248") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_CATEGORY_ID", baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FKfmldojgpely7wlug6e2l6q4oo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-249") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKfubdtk7gwoa8s8919uafl2jj3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-250") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKfwtfcguybm93tkxxgtbkf3yn0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-251") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "signal_history", constraintName: "FKg2etf80lqk7p2chk6848imhyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-252") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_TEMPLATE_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FKg7jny08nmnya6p6b4o7fxxpyq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-253") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "meeting_actions", constraintName: "FKgd1k2pcfls85gbq25kdko2lm6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-254") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKgg9js6ntsjtyeswgiedq6re87", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-255") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKgqh0iu7w66xqgio4jv38b5hmf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-256") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DOCUMENT_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FKguaxlxc330u6by2rdbpa7o15s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-257") {
        addForeignKeyConstraint(baseColumnNames: "attachment_id", baseTableName: "ATTACHMENT_DESCRIPTION", constraintName: "FKhf1vmfljes8qfvmkksj0r56gr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-258") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKhfddlxpqvfaocje2iwp6umh63", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-259") {
        addForeignKeyConstraint(baseColumnNames: "guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKhgeofgkdx1rjj5s2yglnetx6y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GUEST_ATTENDEE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-260") {
        addForeignKeyConstraint(baseColumnNames: "meeting_owner_id", baseTableName: "MEETING", constraintName: "FKhipac92y3mafijg8snr452hkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-261") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKhqjqjxidasmpsaf9jsa1m6fkf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-262") {
        addForeignKeyConstraint(baseColumnNames: "audit_trail_id", baseTableName: "audit_child_log", constraintName: "FKi2lq3l44yxa3o6afk12xj36ip", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audit_log")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-263") {
        addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "EVDAS_CONFIG", constraintName: "FKi7oiwcj0vwl0hljvu0cfgnbat", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-264") {
        addForeignKeyConstraint(baseColumnNames: "signal_strategy_id", baseTableName: "PV_CONCEPT", constraintName: "FKii9w7n9su0kelhwdy64fnoh4a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-265") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "SIGNAL_CHART", constraintName: "FKiwek5gp0875l735x1kt69iaq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-266") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "evdas_history", constraintName: "FKj0e4kr6h7ba4h1j66a0qsvwx6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-267") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKjdbvyyl4pc5gyonimroq4q9vp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-268") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FKk00oq8i0rb555mrs1ua1u1bmv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-269") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKk2v2qa969crmd21cb8ey8eykl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-270") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKl5ki9ms4pq7oyr4wrne2wejf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-271") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "evdas_history", constraintName: "FKl751la1u7svv60tmydb5q6rfo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-272") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "RULE_INFORMATION", constraintName: "FKlfungwscmq9bwj56xlirfguy1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-273") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_actions_id", baseTableName: "validated_signal_actions", constraintName: "FKlqeb869hd13sihhd6a99xq2he", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-274") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "signal_history", constraintName: "FKludcb63lf92hniiym7xpdxhfa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-275") {
        addForeignKeyConstraint(baseColumnNames: "meeting_activities_id", baseTableName: "meeting_activities", constraintName: "FKlxb7shjcemeke6qig312nemnv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-276") {
        addForeignKeyConstraint(baseColumnNames: "meeting_guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKm139ii2baukit0klasjx4oglf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-277") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKmci0hn62phky4qlig0uy8walr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-278") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKmip3dewjmmtbxme02ddf93bni", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-279") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKmv3t9bke7ovuq4xhntvupq5b4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-280") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKn6vycx5pad3o4yv5lqgyg9b0k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-281") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKncksxdxhk9k1wg170uk6hxo01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-282") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FKnfgumbuu5ryminkxap7nlgc5p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-283") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "meeting_pvuser", constraintName: "FKnrjk7qtd13c4vbk8cf2fng3c2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-284") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_EVDAS_CONFIG_TAGS", constraintName: "FKo38uflw88p4l42e75wfeg4fvv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-285") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FKo3yoly6j5mslepff4epm8i3un", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-286") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FKon2n40jnxmx59n3cqmig3ytc5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-287") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKop4ektpb6ulll4ubcyvxd2352", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-288") {
        addForeignKeyConstraint(baseColumnNames: "evdas_alert_id", baseTableName: "ACTIONS", constraintName: "FKotw99rdoeydidva8eqngeoim1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-289") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKpaayw5smfn63cqc92ncfgtcwk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-290") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "signal_history", constraintName: "FKpc3meb0lkklinhdq8yvhcc094", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-291") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "MEETING", constraintName: "FKpkleruks8op5rxe14p0u28dun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-292") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKpw1bhalfblwu0e31df1oib0cr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-293") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FKq24b4km13myw6pxki435bulke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-294") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "validated_signal_actions", constraintName: "FKq4785d5esdy86x6prso7o75el", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-295") {
        addForeignKeyConstraint(baseColumnNames: "meeting_attendees_id", baseTableName: "meeting_pvuser", constraintName: "FKqiltr5xajp4m5fu0je6set5g3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-296") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKqjo3uqjw699p81p6wscpc2b3r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-297") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKr2ep7rrgudpgn2aeip674gffo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-298") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_ALERT", constraintName: "FKrx1ksfdkal8plsauewx9wcoje", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-299") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKsy5o19eqrc5385sfoidonp43l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-300") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKtirjstq25k3s016xchl6imgv8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-301") {
        dropForeignKeyConstraint(baseTableName: "AUDIT_LOG_FIELD_CHANGE", constraintName: "FK_1X86WHYMORFAOE3UE1YXB3FNY")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-302") {
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK_6M33WCYUK8E9BHHK4NHSQAMF9")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-303") {
        dropForeignKeyConstraint(baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_96PF7WII4AC9D92B9YT1NOS4X")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-304") {
        dropForeignKeyConstraint(baseTableName: "SINGLE_CASE_ALERT_ACTIONS", constraintName: "FK_BIYY409IKRBSTN7R6F7KO8K2O")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-305") {
        dropForeignKeyConstraint(baseTableName: "NOTIFICATION", constraintName: "FK_ELAHXCJYBVOSQ3SCYDF1NS948")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-306") {
        dropForeignKeyConstraint(baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_FVQM6O3ASS78S8DY23NP04EGN")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-307") {
        dropForeignKeyConstraint(baseTableName: "SINGLE_CASE_ALERT_ACTIONS", constraintName: "FK_JNHBWXIRWVEU68VLC2JW5PB6V")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-308") {
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK_Q9PSMPJUN6PWT73FA1EA87ST0")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-309") {
        dropForeignKeyConstraint(baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_RRMW22X2RDC7XM3VQOCB9TLFS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-310") {
        dropTable(tableName: "AGG_ALERT_ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-311") {
        dropTable(tableName: "ALGO_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-312") {
        dropTable(tableName: "AUDIT_LOG_FIELD_CHANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-340") {
        dropTable(tableName: "PVS_AUDIT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-341") {
        dropTable(tableName: "SINGLE_CASE_ALERT_ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-342") {
        dropColumn(columnName: "ANALYSIS_LEVEL", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-343") {
        dropColumn(columnName: "AUTO_STATE_CONFIGURATION", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-344") {
        dropColumn(columnName: "CALCULATE_EBGM_CI", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-345") {
        dropColumn(columnName: "CALCULATE_PRR_CI", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-346") {
        dropColumn(columnName: "CALCULATE_ROR_CI", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-347") {
        dropColumn(columnName: "CREATED_BY", tableName: "AUDIT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-348") {
        dropColumn(columnName: "DESCRIPTION", tableName: "AUDIT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-349") {
        dropColumn(columnName: "EBGM_CUSTOM_CONFIG", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-350") {
        dropColumn(columnName: "ENABLE_EBGM", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-351") {
        dropColumn(columnName: "ENABLE_EBGM_CUSTOM_CONFIG", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-352") {
        dropColumn(columnName: "ENABLE_PRR", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-353") {
        dropColumn(columnName: "ENABLE_PRR_CUSTOM_CONFIG", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-354") {
        dropColumn(columnName: "ENABLE_ROR", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-355") {
        dropColumn(columnName: "ENABLE_ROR_CUSTOM_CONFIG", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-357") {
        dropColumn(columnName: "MIN_CASES", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-358") {
        dropColumn(columnName: "MODIFIED_BY", tableName: "AUDIT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-359") {
        dropColumn(columnName: "PARENT_OBJECT", tableName: "AUDIT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-360") {
        dropColumn(columnName: "PARENT_OBJECT_ID", tableName: "AUDIT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-361") {
        dropColumn(columnName: "PRR_CUSTOM_CONFIG", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-362") {
        dropColumn(columnName: "ROR_CUSTOM_CONFIG", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-363") {
        dropColumn(columnName: "USER_ID", tableName: "NOTIFICATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-364") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "version", tableName: "AUDIT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1517200204498-365") {
        dropNotNullConstraint(columnDataType: "varchar(80 CHAR)", columnName: "SOURCE_COLUMN_MASTER_ID", tableName: "RPT_FIELD")
    }

    changeSet(author: "root (generated)", id: "1518170392885-76") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "due_date", tableName: "ACTIONS")
    }
}
