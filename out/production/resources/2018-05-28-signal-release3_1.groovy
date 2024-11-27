databaseChangeLog = {

    changeSet(author: "chetansharma (generated)", id: "1527527772654-1") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-2") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-3") {
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

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "meeting_id", type: "VARCHAR2(255 CHAR)")

            column(name: "owner_id", type: "NUMBER(19, 0)")

            column(name: "single_case_alert_id", type: "NUMBER(19, 0)")

            column(name: "type_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "viewed", type: "NUMBER(1, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-4") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-5") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-6") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-7") {
        createTable(tableName: "ACTIVITIES") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "ACTIVITIESPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)")

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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-8") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-9") {
        createTable(tableName: "ADHOC_ALERT_COMMENTS") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-10") {
        createTable(tableName: "AD_HOC_SIGNAL_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-11") {
        createTable(tableName: "AD_HOC_TOPIC_CONCEPTS") {
            column(name: "AD_HOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-12") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-13") {
        createTable(tableName: "AGG_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "AGG_ALERTPK")
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

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
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

            column(name: "flagged", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "format", type: "VARCHAR2(255 CHAR)")

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "listed", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

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

            column(name: "positive_dechallenge", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "positive_rechallenge", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "pregenency", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

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

            column(name: "related", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

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

            column(name: "workflow_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-14") {
        createTable(tableName: "AGG_SIGNAL_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-15") {
        createTable(tableName: "AGG_TOPIC_CONCEPTS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-16") {
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

            column(name: "workflow_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_type", type: "VARCHAR2(20 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "country_of_incidence", type: "VARCHAR2(255 CHAR)")

            column(name: "description", type: "VARCHAR2(4000 CHAR)")

            column(name: "detected_by", type: "VARCHAR2(255 CHAR)")

            column(name: "formulations", type: "VARCHAR2(255 CHAR)")

            column(name: "indication", type: "VARCHAR2(255 CHAR)")

            column(name: "initial_data_source", type: "VARCHAR2(255 CHAR)")

            column(name: "issue_previously_tracked", type: "NUMBER(1, 0)")

            column(name: "name", type: "VARCHAR2(4000 CHAR)")

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
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-17") {
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

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "COMMENTS", type: "VARCHAR2(4000 CHAR)") {
                constraints(nullable: "false")
            }

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

            column(name: "product_name", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-18") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-19") {
        createTable(tableName: "ALERT_GROUPS") {
            column(name: "alert_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-20") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-21") {
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

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCTS", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "rule_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-22") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-23") {
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

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "current_assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "current_disposition_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "current_priority_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "current_state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP")

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
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-24") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-25") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-26") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-27") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-28") {
        createTable(tableName: "CONFIGURATION_GROUPS") {
            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "configuration_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "shared_groups_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-29") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-30") {
        createTable(tableName: "DELIVERIES_EMAIL_USERS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-31") {
        createTable(tableName: "DELIVERIES_RPT_FORMATS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-32") {
        createTable(tableName: "DELIVERIES_SHARED_WITHS") {
            column(name: "DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-33") {
        createTable(tableName: "DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-34") {
        createTable(tableName: "DISPOSITION") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "DISPOSITIONPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "closed", type: "NUMBER(1, 0)")

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

            column(name: "notify", type: "NUMBER(1, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-35") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-36") {
        createTable(tableName: "DTAB_COL_MEAS_MEASURES") {
            column(name: "DTAB_COL_MEAS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "MEASURES_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-37") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-38") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-39") {
        createTable(tableName: "DTAB_TEMPLTS_COL_MEAS") {
            column(name: "DTAB_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COLUMN_MEASURE_ID", type: "NUMBER(19, 0)")

            column(name: "COLUMN_MEASURE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-40") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-41") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-42") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-43") {
        createTable(tableName: "EVDAS_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EVDAS_ALERTPK")
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-44") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-45") {
        createTable(tableName: "EVDAS_CONFIGURATION_GROUPS") {
            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-46") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-47") {
        createTable(tableName: "EVDAS_SIGNAL_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-48") {
        createTable(tableName: "EVDAS_TAGS") {
            column(name: "EVDAS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-49") {
        createTable(tableName: "EVDAS_TOPIC_CONCEPTS") {
            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-50") {
        createTable(tableName: "EX_CLL_TEMPLT") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-51") {
        createTable(tableName: "EX_CUSTOM_SQL_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-52") {
        createTable(tableName: "EX_CUSTOM_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-53") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-54") {
        createTable(tableName: "EX_DELIVERIES_EMAIL_USERS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EMAIL_USER", type: "VARCHAR2(255 CHAR)")

            column(name: "EMAIL_USER_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-55") {
        createTable(tableName: "EX_DELIVERIES_RPT_FORMATS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-56") {
        createTable(tableName: "EX_DELIVERIES_SHARED_WITHS") {
            column(name: "EX_DELIVERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-57") {
        createTable(tableName: "EX_DELIVERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_DELIVERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-58") {
        createTable(tableName: "EX_DTAB_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-59") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-60") {
        createTable(tableName: "EX_EVDAS_CONFIG_ACTIVITIES") {
            column(name: "EX_EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-61") {
        createTable(tableName: "EX_EVDAS_CONFIG_TAGS") {
            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-62") {
        createTable(tableName: "EX_NCASE_SQL_TEMPLT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-63") {
        createTable(tableName: "EX_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-64") {
        createTable(tableName: "EX_QUERY_EXP") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-65") {
        createTable(tableName: "EX_QUERY_SET") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-66") {
        createTable(tableName: "EX_QUERY_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-67") {
        createTable(tableName: "EX_RCONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_RCONFIGPK")
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

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

            column(name: "DRUG_TYPE", type: "VARCHAR2(255 CHAR)")

            column(name: "EVALUATE_DATE_AS", type: "VARCHAR2(255 CHAR)")

            column(name: "EVENT_SELECTION", type: "CLOB")

            column(name: "EXCLUDE_FOLLOWUP", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EXCLUDE_NON_VALID_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

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

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "product_dictionary_selection", type: "VARCHAR2(255 CHAR)")

            column(name: "PRODUCT_SELECTION", type: "CLOB")

            column(name: "reference_number", type: "VARCHAR2(255 CHAR)")

            column(name: "repeat_execution", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "review_due_date", type: "TIMESTAMP")

            column(name: "SCHEDULE_DATE", type: "VARCHAR2(1024 CHAR)")

            column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR2(255 CHAR)")

            column(name: "STUDY_SELECTION", type: "CLOB")

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-68") {
        createTable(tableName: "EX_RCONFIGS_TAGS") {
            column(name: "EXC_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-69") {
        createTable(tableName: "EX_SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-70") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-71") {
        createTable(tableName: "EX_STATUSES_RPT_FORMATS") {
            column(name: "EX_STATUS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_FORMAT", type: "VARCHAR2(255 CHAR)")

            column(name: "RPT_FORMAT_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-72") {
        createTable(tableName: "EX_STATUSES_SHARED_WITHS") {
            column(name: "EX_STATUS_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SHARED_WITH_ID", type: "NUMBER(19, 0)")

            column(name: "SHARED_WITH_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-73") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-74") {
        createTable(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
            column(name: "EX_TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-75") {
        createTable(tableName: "EX_TEMPLT_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "EX_TEMPLT_QUERYPK")
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

            column(name: "EX_RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_DATE_RANGE_INFO_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "EX_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FOOTER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER_DATE_RANGE", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "modified_by", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_LEVEL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TITLE", type: "VARCHAR2(255 CHAR)")

            column(name: "EX_TEMPLT_QUERY_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-76") {
        createTable(tableName: "EX_TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-77") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-78") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-79") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-80") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-81") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-82") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-83") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-84") {
        createTable(tableName: "MEETING_ATTACHMENTS") {
            column(name: "MEETING_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ATTACHMENTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-85") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-86") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-87") {
        createTable(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES") {
            column(name: "NONCASE_SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-88") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-89") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-90") {
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

            column(defaultValueComputed: 1, name: "is_cumulative_alert_enabled", type: "number(1, 0)") {
                constraints(nullable: "false")
            }

            column(defaultValueComputed: 1, name: "is_email_enabled", type: "number(1, 0)") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-91") {
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

            column(name: "description_local", type: "VARCHAR2(255 CHAR)")

            column(name: "display", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "display_name_local", type: "VARCHAR2(255 CHAR)")

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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-92") {
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

            column(name: "group_id", type: "NUMBER(19, 0)")

            column(name: "safety_group_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-93") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-94") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-95") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-96") {
        createTable(tableName: "PVUSERS_ROLES") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-97") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-98") {
        createTable(tableName: "QUERIES_QRS_EXP_VALUES") {
            column(name: "QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_EXP_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-99") {
        createTable(tableName: "QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "reassess_listedness", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-100") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-101") {
        createTable(tableName: "QUERY_SET") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-102") {
        createTable(tableName: "QUERY_SETS_SUPER_QRS") {
            column(name: "QUERY_SET_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-103") {
        createTable(tableName: "QUERY_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-104") {
        createTable(tableName: "RCONFIG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RCONFIGPK")
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

            column(name: "alert_rmp_rems_ref", type: "VARCHAR2(255 CHAR)")

            column(name: "alert_trigger_cases", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_trigger_days", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AS_OF_VERSION_DATE", type: "TIMESTAMP")

            column(name: "AS_OF_VERSION_DATE_DELTA", type: "NUMBER(10, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

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

            column(name: "DESCRIPTION", type: "VARCHAR2(200 CHAR)")

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

            column(name: "frequency", type: "VARCHAR2(255 CHAR)")

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

            column(name: "TOTAL_EXECUTION_TIME", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-105") {
        createTable(tableName: "RCONFIGS_TAGS") {
            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-106") {
        createTable(tableName: "RCONFIG_DISPOSITION") {
            column(name: "CONFIGURATION_DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)")

            column(name: "RCONFIG_DISPOSITION_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-107") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-108") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-109") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-110") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-111") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-112") {
        createTable(tableName: "RPT_FIELD_INFO_LIST") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "RPT_FIELD_INFO_LISTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-113") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-114") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-115") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-116") {
        createTable(tableName: "RPT_TEMPLTS_TAGS") {
            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-117") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-118") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-119") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-120") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-121") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-122") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-123") {
        createTable(tableName: "SINGLE_CASE_ALERT") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SINGLE_CASE_ALERTPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_count", type: "NUMBER(10, 0)")

            column(name: "adhoc_run", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "alert_configuration_id", type: "NUMBER(19, 0)")

            column(name: "assigned_to_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "attributes", type: "CLOB")

            column(name: "case_number", type: "VARCHAR2(255 CHAR)")

            column(name: "case_version", type: "NUMBER(10, 0)")

            column(name: "created_by", type: "VARCHAR2(255 CHAR)")

            column(name: "date_created", type: "TIMESTAMP")

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

            column(name: "is_new", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP")

            column(name: "listedness", type: "VARCHAR2(255 CHAR)")

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

            column(name: "review_date", type: "TIMESTAMP")

            column(name: "STATE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-124") {
        createTable(tableName: "SINGLE_SIGNAL_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-125") {
        createTable(tableName: "SINGLE_TOPIC_CONCEPTS") {
            column(name: "SINGLE_CASE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-126") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-127") {
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

            column(name: "TABLE_ALIAS", type: "VARCHAR2(5 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "TABLE_TYPE", type: "VARCHAR2(1 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "VERSIONED_DATA", type: "VARCHAR2(1 CHAR)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-128") {
        createTable(tableName: "SQL_QRS_SQL_VALUES") {
            column(name: "SQL_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-129") {
        createTable(tableName: "SQL_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-130") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-131") {
        createTable(tableName: "SQL_TEMPLTS_SQL_VALUES") {
            column(name: "SQL_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SQL_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-132") {
        createTable(tableName: "SQL_TEMPLT_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FIELD", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-133") {
        createTable(tableName: "SQL_VALUE") {
            column(name: "ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-134") {
        createTable(tableName: "STRATEGY_MEDICAL_CONCEPTS") {
            column(name: "SIGNAL_STRATEGY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "MEDICAL_CONCEPTS_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-135") {
        createTable(tableName: "SUPER_QRS_TAGS") {
            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TAG_ID", type: "NUMBER(19, 0)")

            column(name: "TAG_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-136") {
        createTable(tableName: "SUPER_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "SUPER_QUERYPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY", type: "CLOB")

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

            column(name: "HAS_BLANKS", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_EUDRA_QUERY", type: "NUMBER(1, 0)") {
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

            column(name: "NON_VALID_CASES", type: "NUMBER(1, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ORIG_QUERY_ID", type: "NUMBER(10, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_TYPE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-137") {
        createTable(tableName: "TAG") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TAGPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "NAME", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-138") {
        createTable(tableName: "TEMPLT_QRS_QUERY_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-139") {
        createTable(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
            column(name: "TEMPLT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TEMPLT_VALUE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-140") {
        createTable(tableName: "TEMPLT_QUERY") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "TEMPLT_QUERYPK")
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

            column(name: "DATE_RANGE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "FOOTER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER", type: "VARCHAR2(255 CHAR)")

            column(name: "HEADER_DATE_RANGE", type: "NUMBER(1, 0)") {
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

            column(name: "SUPER_QUERY_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_LEVEL", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }

            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TITLE", type: "VARCHAR2(255 CHAR)")

            column(name: "TEMPLT_QUERY_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-141") {
        createTable(tableName: "TEMPLT_VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_TEMPLT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-142") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-143") {
        createTable(tableName: "TOPIC_ACTIVITIES") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-144") {
        createTable(tableName: "TOPIC_ADHOC_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-145") {
        createTable(tableName: "TOPIC_AGG_ALERTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-146") {
        createTable(tableName: "TOPIC_COMMENTS") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-147") {
        createTable(tableName: "TOPIC_GROUP") {
            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-148") {
        createTable(tableName: "TOPIC_SINGLE_ALERTS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "TOPIC_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-149") {
        createTable(tableName: "USER_GROUPS") {
            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-150") {
        createTable(tableName: "VALIDATED_ADHOC_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-151") {
        createTable(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-152") {
        createTable(tableName: "VALIDATED_ALERT_ACTIVITIES") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-153") {
        createTable(tableName: "VALIDATED_ALERT_COMMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-154") {
        createTable(tableName: "VALIDATED_ALERT_DOCUMENTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_DOCUMENT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-155") {
        createTable(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-156") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-157") {
        createTable(tableName: "VALIDATED_SIGNAL_CATEGORY") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-158") {
        createTable(tableName: "VALIDATED_SIGNAL_GROUP") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-159") {
        createTable(tableName: "VALIDATED_SINGLE_ALERTS") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-160") {
        createTable(tableName: "VALUE") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "VALUEPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-161") {
        createTable(tableName: "VALUES_PARAMS") {
            column(name: "VALUE_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PARAM_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-162") {
        createTable(tableName: "WORKFLOWRULES_ACTION_TEMPLATES") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TEMPLATE_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-163") {
        createTable(tableName: "WORKFLOWRULES_GROUPS") {
            column(name: "workflow_rule_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "allowed_groups_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-164") {
        createTable(tableName: "WORKFLOWRULES_SIGNAL_CATEGORY") {
            column(name: "WORKFLOW_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-165") {
        createTable(tableName: "WORK_FLOWS") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "WORK_FLOWSPK")
            }

            column(name: "version", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR2(255 CHAR)")

            column(name: "name", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-166") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-167") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-168") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-169") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-170") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-171") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-172") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-173") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-174") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-175") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-176") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-177") {
        createTable(tableName: "ex_rconfig_activities") {
            column(name: "EX_CONFIG_ACTIVITIES_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTIVITY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-178") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-179") {
        createTable(tableName: "meeting_actions") {
            column(name: "meeting_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-180") {
        createTable(tableName: "meeting_activities") {
            column(name: "meeting_activities_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "activity_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-181") {
        createTable(tableName: "meeting_guest_attendee") {
            column(name: "meeting_guest_attendee_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "guest_attendee_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-182") {
        createTable(tableName: "meeting_pvuser") {
            column(name: "meeting_attendees_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-183") {
        createTable(tableName: "product_event_history") {
            column(name: "id", type: "NUMBER(19, 0)") {
                constraints(primaryKey: "true", primaryKeyName: "product_event_historyPK")
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

            column(name: "state_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-184") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-185") {
        createTable(tableName: "pvuser_safety_groups") {
            column(name: "safety_group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-186") {
        createTable(tableName: "pvuser_user_department") {
            column(name: "user_user_departments_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_department_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-187") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-188") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-189") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-190") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-191") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-192") {
        createTable(tableName: "topic_actions") {
            column(name: "topic_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-193") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-194") {
        createTable(tableName: "user_group_s") {
            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-195") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-196") {
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

    changeSet(author: "chetansharma (generated)", id: "1527527772654-197") {
        createTable(tableName: "validated_signal_actions") {
            column(name: "validated_signal_actions_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_id", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-198") {
        createTable(tableName: "work_flow_rules_actions") {
            column(name: "workflow_rule_actions_id", type: "NUMBER(19, 0)")

            column(name: "action_id", type: "NUMBER(19, 0)")

            column(name: "actions_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-199") {
        createTable(tableName: "work_flows_work_flow_rules") {
            column(name: "workflow_rule_list_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "workflow_rule_id", type: "NUMBER(19, 0)")

            column(name: "rule_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-200") {
        createIndex(indexName: "IX_ALERT_GROUPS", tableName: "ALERT_GROUPS", unique: "true") {
            column(name: "alert_id")

            column(name: "group_id")
        }

        addPrimaryKey(columnNames: "alert_id, group_id", forIndexName: "IX_ALERT_GROUPS", tableName: "ALERT_GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-201") {
        createIndex(indexName: "IX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_ADHOC_ALERTS", tableName: "TOPIC_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-202") {
        createIndex(indexName: "IX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_AGG_ALERTS", tableName: "TOPIC_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-203") {
        createIndex(indexName: "IX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "TOPIC_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, TOPIC_ID", forIndexName: "IX_TOPIC_SINGLE_ALERTS", tableName: "TOPIC_SINGLE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-204") {
        createIndex(indexName: "IX_VALIDATED_ADHOC_ALERTS", tableName: "VALIDATED_ADHOC_ALERTS", unique: "true") {
            column(name: "ADHOC_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "ADHOC_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_ADHOC_ALERTS", tableName: "VALIDATED_ADHOC_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-205") {
        createIndex(indexName: "IX_VALIDATED_AGG_ALERTS", tableName: "VALIDATED_AGG_ALERTS", unique: "true") {
            column(name: "AGG_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "AGG_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_AGG_ALERTS", tableName: "VALIDATED_AGG_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-206") {
        createIndex(indexName: "IX_VALIDATED_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS", unique: "true") {
            column(name: "EVDAS_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "EVDAS_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_EVDAS_ALERTS", tableName: "VALIDATED_EVDAS_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-207") {
        createIndex(indexName: "IX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS", unique: "true") {
            column(name: "SINGLE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "SINGLE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_SINGLE_ALERTS", tableName: "VALIDATED_SINGLE_ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-208") {
        createIndex(indexName: "IX_SAFETY_GROUPS", tableName: "pvuser_safety_groups", unique: "true") {
            column(name: "user_id")

            column(name: "safety_group_id")
        }

        addPrimaryKey(columnNames: "user_id, safety_group_id", forIndexName: "IX_SAFETY_GROUPS", tableName: "pvuser_safety_groups")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-209") {
        createIndex(indexName: "IX_USER_GROUP", tableName: "user_group_s", unique: "true") {
            column(name: "user_id")

            column(name: "group_id")
        }

        addPrimaryKey(columnNames: "user_id, group_id", forIndexName: "IX_USER_GROUP", tableName: "user_group_s")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-210") {
        createIndex(indexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "CLL_TEMPLTPK", forIndexName: "IX_CLL_TEMPLTPK", tableName: "CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-211") {
        createIndex(indexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "DTAB_TEMPLTPK", forIndexName: "IX_DTAB_TEMPLTPK", tableName: "DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-212") {
        createIndex(indexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_CLL_TEMPLTPK", forIndexName: "IX_EX_CLL_TEMPLTPK", tableName: "EX_CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-213") {
        createIndex(indexName: "IX_EX_CUSTOM_SQL_QUERYPK", tableName: "EX_CUSTOM_SQL_QUERY", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_CUSTOM_SQL_QUERYPK", forIndexName: "IX_EX_CUSTOM_SQL_QUERYPK", tableName: "EX_CUSTOM_SQL_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-214") {
        createIndex(indexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_CUSTOM_SQL_TEMPLTPK", forIndexName: "IX_EX_CUSTOM_SQL_TEMPLTPK", tableName: "EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-215") {
        createIndex(indexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_DTAB_TEMPLTPK", forIndexName: "IX_EX_DTAB_TEMPLTPK", tableName: "EX_DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-216") {
        createIndex(indexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_NCASE_SQL_TEMPLTPK", forIndexName: "IX_EX_NCASE_SQL_TEMPLTPK", tableName: "EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-217") {
        createIndex(indexName: "IX_EX_QUERYPK", tableName: "EX_QUERY", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_QUERYPK", forIndexName: "IX_EX_QUERYPK", tableName: "EX_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-218") {
        createIndex(indexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_QUERY_EXPPK", forIndexName: "IX_EX_QUERY_EXPPK", tableName: "EX_QUERY_EXP")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-219") {
        createIndex(indexName: "IX_EX_QUERY_SETPK", tableName: "EX_QUERY_SET", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_QUERY_SETPK", forIndexName: "IX_EX_QUERY_SETPK", tableName: "EX_QUERY_SET")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-220") {
        createIndex(indexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_QUERY_VALUEPK", forIndexName: "IX_EX_QUERY_VALUEPK", tableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-221") {
        createIndex(indexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "EX_SQL_VALUEPK", forIndexName: "IX_EX_SQL_VALUEPK", tableName: "EX_SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-222") {
        createIndex(indexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "EX_TEMPLT_VALUEPK", forIndexName: "IX_EX_TEMPLT_VALUEPK", tableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-223") {
        createIndex(indexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG", unique: "true") {
            column(name: "KEY_1")
        }

        addPrimaryKey(columnNames: "KEY_1", constraintName: "MISC_CONFIGPK", forIndexName: "IX_MISC_CONFIGPK", tableName: "MISC_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-224") {
        createIndex(indexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "NONCASE_SQL_TEMPLTPK", forIndexName: "IX_NONCASE_SQL_TEMPLTPK", tableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-225") {
        createIndex(indexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "PVUSERS_ROLESPK", forIndexName: "IX_PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-226") {
        createIndex(indexName: "IX_QUERYPK", tableName: "QUERY", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "QUERYPK", forIndexName: "IX_QUERYPK", tableName: "QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-227") {
        createIndex(indexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "QUERY_EXP_VALUEPK", forIndexName: "IX_QUERY_EXP_VALUEPK", tableName: "QUERY_EXP_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-228") {
        createIndex(indexName: "IX_QUERY_SETPK", tableName: "QUERY_SET", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "QUERY_SETPK", forIndexName: "IX_QUERY_SETPK", tableName: "QUERY_SET")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-229") {
        createIndex(indexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "QUERY_VALUEPK", forIndexName: "IX_QUERY_VALUEPK", tableName: "QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-230") {
        createIndex(indexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP", unique: "true") {
            column(name: "NAME")
        }

        addPrimaryKey(columnNames: "NAME", constraintName: "RPT_FIELD_GROUPPK", forIndexName: "IX_RPT_FIELD_GROUPPK", tableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-231") {
        createIndex(indexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER", unique: "true") {
            column(name: "REPORT_ITEM")
        }

        addPrimaryKey(columnNames: "REPORT_ITEM", constraintName: "SOURCE_COLUMN_MASTERPK", forIndexName: "IX_SOURCE_COLUMN_MASTERPK", tableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-232") {
        createIndex(indexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER", unique: "true") {
            column(name: "TABLE_NAME")
        }

        addPrimaryKey(columnNames: "TABLE_NAME", constraintName: "SOURCE_TABLE_MASTERPK", forIndexName: "IX_SOURCE_TABLE_MASTERPK", tableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-233") {
        createIndex(indexName: "IX_SQL_QUERYPK", tableName: "SQL_QUERY", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SQL_QUERYPK", forIndexName: "IX_SQL_QUERYPK", tableName: "SQL_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-234") {
        createIndex(indexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "SQL_TEMPLTPK", forIndexName: "IX_SQL_TEMPLTPK", tableName: "SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-235") {
        createIndex(indexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_TEMPLT_VALUEPK", forIndexName: "IX_SQL_TEMPLT_VALUEPK", tableName: "SQL_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-236") {
        createIndex(indexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE", unique: "true") {
            column(name: "ID")
        }

        addPrimaryKey(columnNames: "ID", constraintName: "SQL_VALUEPK", forIndexName: "IX_SQL_VALUEPK", tableName: "SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-237") {
        createIndex(indexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE", unique: "true") {
            column(name: "id")
        }

        addPrimaryKey(columnNames: "id", constraintName: "TEMPLT_VALUEPK", forIndexName: "IX_TEMPLT_VALUEPK", tableName: "TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-238") {
        createIndex(indexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "COMMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, COMMENT_ID", constraintName: "VALIDATED_ALERT_COMMENTSPK", forIndexName: "IX_VALIDATED_ALERT_COMMENTSPK", tableName: "VALIDATED_ALERT_COMMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-239") {
        createIndex(indexName: "IX_VALIDATED_ALERT_DOCPK", tableName: "VALIDATED_ALERT_DOCUMENTS", unique: "true") {
            column(name: "VALIDATED_SIGNAL_ID")

            column(name: "ALERT_DOCUMENT_ID")
        }

        addPrimaryKey(columnNames: "VALIDATED_SIGNAL_ID, ALERT_DOCUMENT_ID", constraintName: "VALIDATED_ALERT_DOCUMENTSPK", forIndexName: "IX_VALIDATED_ALERT_DOCPK", tableName: "VALIDATED_ALERT_DOCUMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-240") {
        addUniqueConstraint(columnNames: "LDAP_GROUP_NAME", constraintName: "UC_A_C_GRPLDAP_GRP_NAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-241") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_A_CTRL_GRPNAME_COL", tableName: "ACCESS_CONTROL_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-242") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_ACTION_TEMPLATENAME_COL", tableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-243") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_AEVAL_TYPEVALUE_COL", tableName: "AEVAL_TYPE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-244") {
        addUniqueConstraint(columnNames: "chronicle_id", constraintName: "UC_ALERT_DOCCHRONICLE_ID_COL", tableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-245") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_CATEGORYNAME_COL", tableName: "CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-246") {
        addUniqueConstraint(columnNames: "display_name_local", constraintName: "UC_DISDISPLAY_NAME_LOCAL_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-247") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_DISPOSITIONVALUE_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-248") {
        addUniqueConstraint(columnNames: "SCHEDULE_NAME", constraintName: "UC_ETL_CHEDULE_NAME_COL", tableName: "ETL_SCHEDULE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-249") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_EVAL_REF_TYPENAME_COL", tableName: "EVAL_REF_TYPE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-250") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_GROUPSNAME_COL", tableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-251") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PRIORITYVALUE_COL", tableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-252") {
        addUniqueConstraint(columnNames: "value", constraintName: "UC_PVS_STATEVALUE_COL", tableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-253") {
        addUniqueConstraint(columnNames: "USERNAME", constraintName: "UC_PVUSERUSERNAME_COL", tableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-254") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_PV_CONCEPTNAME_COL", tableName: "PV_CONCEPT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-255") {
        addUniqueConstraint(columnNames: "report_name", constraintName: "UC_REPORT_HISREPORT_NAME_COL", tableName: "report_history")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-256") {
        addUniqueConstraint(columnNames: "AUTHORITY", constraintName: "UC_ROLEAUTHORITY_COL", tableName: "ROLE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-257") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FIELDNAME_COL", tableName: "RPT_FIELD")
    }

//    changeSet(author: "chetansharma (generated)", id: "1527527772654-258") {
//        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_RPT_FLD_GRPNAME_COL", tableName: "RPT_FIELD_GROUP")
//    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-259") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_SAFETY_GRPNAME_COL", tableName: "safety_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-260") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_TAGNAME_COL", tableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-261") {
        addUniqueConstraint(columnNames: "department_name", constraintName: "UC_USER_DEPARTMENT_NAME_COL", tableName: "user_department")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-262") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_VALIDATED_SIGNALNAME_COL", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-263") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_WORK_FLOW_RULESNAME_COL", tableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-264") {
        addForeignKeyConstraint(baseColumnNames: "pvs_state_id", baseTableName: "RULE_INFORMATION", constraintName: "FK13b21d1j7ivojo0uelga0e95l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-265") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "signal_history", constraintName: "FK1gksukhgbnjjbm6tq7sergnf3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-266") {
        addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FK1h2ucyipg6q9plbmkpqs9ml5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-267") {
        addForeignKeyConstraint(baseColumnNames: "EX_CONFIG_ACTIVITIES_ID", baseTableName: "ex_rconfig_activities", constraintName: "FK1lvnr30ngjjrcqtmugj4o7xxx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-268") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK1pvjhvvvh16fitf3f9aepcrx3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-269") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK1tom8u04lfcjhkpwup2d8arxa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-270") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "product_event_history", constraintName: "FK1vu272crmaxuyfixksks1ij0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-271") {
        addForeignKeyConstraint(baseColumnNames: "current_state_id", baseTableName: "CASE_HISTORY", constraintName: "FK1xejyde30qqco71chaaud68wa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-272") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FK25gco89k379c8yngl2ixneunj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-273") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK28ogh0ec7qa0wls7dxduf1ee1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-274") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ALERTS", constraintName: "FK2asghyg8brvh2hd9ihjye9d8a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-275") {
        addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_VALUE", constraintName: "FK2ct5vgls6xkw3m81a4bpnj6pn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-276") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK2mrmhl81t6ttn1ga2kcbl8nc5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-277") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK2skfidis7c3uhb5n3pablyibp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-278") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "SIGNAL_CHART", constraintName: "FK2vyvpc8474sber4f5uljnhef0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-279") {
        addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK34mof8i2imjgyqy96jwaus2w2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-280") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3hq5p8kqtby4pli3sl769bqdq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-281") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3i2ij0470itt2oyfll08nuagc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-282") {
        addForeignKeyConstraint(baseColumnNames: "MEETING_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FK3kly0ngl8jptbdmurwpdhn15e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-283") {
        addForeignKeyConstraint(baseColumnNames: "EX_DATE_RANGE_INFO_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK3ocntqp0fggudmyw65r9ghtth", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-284") {
        addForeignKeyConstraint(baseColumnNames: "lnk_id", baseTableName: "attachment", constraintName: "FK417wyfahv01xv8s9ypukvvpt6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment_link")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-285") {
        addForeignKeyConstraint(baseColumnNames: "SQL_QUERY_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK42yow13fee7hxgtslsy44d2jl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-286") {
        addForeignKeyConstraint(baseColumnNames: "business_configuration_id", baseTableName: "RULE_INFORMATION", constraintName: "FK46deuafhfurfdbrewcoueiogp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-287") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK46gb5ngvp56xrv2j0d91iuqf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-288") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_SET_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FK498l090335m7h30rb26qy51h5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_SET")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-289") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "ALERTS", constraintName: "FK49mvquqce3y88y7bg6gq6cyfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-290") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FK4e8ln2cbe20xp8afdh1y7sa9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-291") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "signal_history", constraintName: "FK4he7y9hi5lwrs2d8tn16rsxew", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-292") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK4jhhis76bsysx4xm6i6ioykpc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-293") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "TOPIC", constraintName: "FK4kh9et74csyh0sxnpty9v5bwf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-294") {
        addForeignKeyConstraint(baseColumnNames: "RF_INFO_LIST_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK4u8v80bl2vqgbw7bs11nm3qr2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-295") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FK4xwtik25kkjo898vri20gjn8h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-296") {
        addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "signal_history", constraintName: "FK52s7ra0yhx7nlww3j0soms7i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-297") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "TOPIC", constraintName: "FK557wy3htik1yrmw2ldt3lujx9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-298") {
        addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK568vr26l2ip7hmy80k1umjtw9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-299") {
        addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "product_event_history", constraintName: "FK58hg7qhauwr3oxu6j6yw8b649", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-300") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FK58riu8smk8hl4hwwx89gr2v4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-301") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIVITIES", constraintName: "FK5967su0w6fjlm4na1gewgy3ts", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-302") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK5bcrnlc25y3lkkm5d8xsqrb83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-303") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK5jhxvs3et4nysr91wkhnwv15m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-304") {
        addForeignKeyConstraint(baseColumnNames: "commented_by_id", baseTableName: "COMMENTS", constraintName: "FK5r4u2uxs7lo6xkmrbmio08003", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-305") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FK5xqn3nlelifk9hpih50rd7b3s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-306") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "evdas_history", constraintName: "FK67y86bymxk3lnavaj4ax1fmmm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-307") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK6d5e73n3n8q05npuax4fknirw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-308") {
        addForeignKeyConstraint(baseColumnNames: "SOURCE_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FK6hfxx9xfelgaomred8dn99iig", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "SOURCE_COLUMN_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-309") {
        addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "evdas_history", constraintName: "FK6lcp6urq7u4nokl4c70rdnhdb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-310") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_ID", baseTableName: "RPT_ERROR", constraintName: "FK6m8eikuwcfty44ymm36rxip6c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-311") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RCONFIGS_TAGS", constraintName: "FK6n3123mkgcufgbtx0l6uk4mpa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-312") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FK6o7whfp7w45byxkfkg3ajgy8d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-313") {
        addForeignKeyConstraint(baseColumnNames: "config_id", baseTableName: "ACTIONS", constraintName: "FK6qnyouq2pj8noqbbgr6blr0fa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_CONFIGURATIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-314") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_SIGNAL_CONCEPTS", constraintName: "FK6xlkiqggquuh3unim09gl7s5k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-315") {
        addForeignKeyConstraint(baseColumnNames: "topic_actions_id", baseTableName: "topic_actions", constraintName: "FK6xwerddtt6xmysbwpqm0thskw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-316") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FK73rmou5rxoc3xenxf07klxnvl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-317") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIONS", constraintName: "FK795v9wlhwg7olj50atxgreqp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-318") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "TOPIC", constraintName: "FK7fprf6132fra32pjukedntqyl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-319") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DELIVERIES_SHARED_WITHS", constraintName: "FK7ofjwn1n4eyo0b9ho6nqthf83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-320") {
        addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK7p44v7rjuor7a67e1uuhh00ki", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-321") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK7uqp71w2q1kumo5txv4soekoi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-322") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FK7uu820jqwt2ggpbwt5q1n3irg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-323") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "SHARED_WITH", constraintName: "FK7yoqaegsqnf7fv4hmpsxc0d5u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-324") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "TOPIC", constraintName: "FK81ax335qvxywo42jwjpk2uj70", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-325") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK81iwl1ty36fpnf3hbv07ge0e7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-326") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_VALUE_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK82nwedxu2puppkbvcn5xj3m15", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-327") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FK82uvw0fbs7p68ocv156ous0ho", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-328") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "RULE_INFORMATION", constraintName: "FK82y4skeo5y3n493ropl4b6ior", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-329") {
        addForeignKeyConstraint(baseColumnNames: "PV_USER_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK88o1kq939jqa3ujho728e9hj6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-330") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK89ymed0wlhc1tw9k2r0yv53yy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-331") {
        addForeignKeyConstraint(baseColumnNames: "income_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK8d35ldteverucbekx4ueliwxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-332") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "topic_actions", constraintName: "FK8ewu1k8gy0uup9i3l7edt5u0g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-333") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_GROUP", constraintName: "FK8i6w6mah3h65hjsr0xiwwj247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-334") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FK8iktftscl552215n7hl8xr46m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-335") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK8l9hjbep49n1p5jgt1lwktf1x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-336") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "meeting_activities", constraintName: "FK8o8dfpgm739ed1u78v767vsvt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-337") {
        addForeignKeyConstraint(baseColumnNames: "ROW_COLS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK8p9endljdo1ex5s0am5h174cw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-338") {
        addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK8v8dg0fy3u8lq3l3u4c4u0mcu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-339") {
        addForeignKeyConstraint(baseColumnNames: "SQL_TEMPLT_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK8wopocxfmf15ej1v33p2twnc6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-340") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FK923jql3sdm5jk6ht1an9nn7ov", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-341") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "TOPIC", constraintName: "FK94byri2xuxp9jqxhjmvb4a8x2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-342") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AD_HOC_SIGNAL_CONCEPTS", constraintName: "FK993e2h8qmb5k5m9d2oo2vh3i8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-343") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK99lpraxpe9sb9vixs14twpoi9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-344") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FK9h7pd11wvccr6lf5yge61layr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-345") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FK9k5fv2tt7vtl5d0rce4f46ws", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-346") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ADHOC_ALERTS", constraintName: "FK9lmmhn29xffgygx6iaduvqmqb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-347") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUPS", constraintName: "FK9nm9nx0a6hcur0207kh6k5dn9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-348") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ALERTS", constraintName: "FK9nmky66wnq1cefm2snii3dsn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-349") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FK9q2pi8025jxe8bbgtyldlp6tp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-350") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "RCONFIG_DISPOSITION", constraintName: "FK9v26sfjmamkwv5h7yyh7k8vop", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-351") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_SIGNAL_CONCEPTS", constraintName: "FK9vmxhc2kgh0i6pcw4jqcdm819", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-352") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK9xhb2aa0o52td9v65k84gpvb1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-353") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "TOPIC_COMMENTS", constraintName: "FKa3cqvwnj1j7q2yi5coqlp2br4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-354") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIONS", constraintName: "FKa3rycbytmp5ea3n5dxc5hiaxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TYPES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-355") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_ALERT", constraintName: "FKa87y6kvqdbxkl5gef3rft9cl0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-356") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKaa2wqfteyjdl906bs3t05k0s8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-357") {
        addForeignKeyConstraint(baseColumnNames: "EX_EVDAS_CONFIG_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKahisevpydu0thicdyjm49v5w9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-358") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "TOPIC_GROUP", constraintName: "FKaijamtneohj273nu1ncurw295", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-359") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_ACTIVITIES", constraintName: "FKalinqp5i92dk44sa1l98og467", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-360") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "VALIDATED_SIGNAL_GROUP", constraintName: "FKant7fchwn6ya4i1yvhgqt8djc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-361") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EVDAS_TAGS", constraintName: "FKau1wuyclgdhggx6leydf3942a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-362") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_STRATEGY_ID", baseTableName: "STRATEGY_MEDICAL_CONCEPTS", constraintName: "FKb309drlgw2wgk40cmpq4iqqmo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-363") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RFI_LIST_ID", baseTableName: "DTAB_COLUMN_MEASURE", constraintName: "FKb61pwfq5cj72hri56yej80i0m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-364") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "evdas_file_process_log", constraintName: "FKb63thlw61wkcwi0uebhwttj0c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-365") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKb790uhowictwtiygfm6n9wsrs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-366") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_STATUSES_SHARED_WITHS", constraintName: "FKb83gyva8nj5ctoykqkov8ongn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-367") {
        addForeignKeyConstraint(baseColumnNames: "STATE_ID", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKba6uhhjxf8imdf8y0gokp9c8e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-368") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKbaasjf1q5jdh8yb9dgo2e5c5x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-369") {
        addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKbbo1adbmuh5tbw11h9j1m54rg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-370") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKbbsstpmcsh6gpgf7v3rxr5tn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-371") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "EVDAS_ALERT", constraintName: "FKbl1p01g7xkc98cbthaqndcebh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-372") {
        addForeignKeyConstraint(baseColumnNames: "SCHEDULED_PVUSER_ID", baseTableName: "RPT_RESULT", constraintName: "FKbmfelgea61vxim4d9lodgmw3r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-373") {
        addForeignKeyConstraint(baseColumnNames: "shared_with_id", baseTableName: "ALERTS", constraintName: "FKboxvx12d3pno2w2v4vqcwr3y3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-374") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "CONFIGURATION_GROUPS", constraintName: "FKbvcym800dfq16dafhvvbbo56", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-375") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKbwna09ojl0fcs9fpgv65fnc14", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-376") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_safety_groups", constraintName: "FKc046tqmqedqp9141fa2soxx7x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-377") {
        addForeignKeyConstraint(baseColumnNames: "NONCASE_SQL_TEMPLT_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKci1t92wr5cug7k5o1y9u2e9v5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-378") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_group_s", constraintName: "FKcrqry31n9mka1oh51bhsn8r99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-379") {
        addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FKd2i9nh4hjfnjt0d3hf56kml3h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-380") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKd6owieejvfmafwh8dyofkenut", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-381") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "TOPIC_AGG_ALERTS", constraintName: "FKd74cpexfn1jrxsjhy23g3kqjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-382") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "VALIDATED_SINGLE_ALERTS", constraintName: "FKd9ws3gvh9jkokbmx8qej0nt7q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-383") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "CASE_HISTORY", constraintName: "FKdhqqeepqe5rbesr70u9gw7ktm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-384") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ACTIVITIES", constraintName: "FKdk2ms0bbvlg9wt85bl3h172c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-385") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKdr8517c590a2tx7da58eb0med", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-386") {
        addForeignKeyConstraint(baseColumnNames: "ATTACHMENTS_ID", baseTableName: "MEETING_ATTACHMENTS", constraintName: "FKdujjes8cvidp8xttfhe9yltmx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "FILE_ATTACHMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-387") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKdumjbc9ixhbyvwdgivdcqbcgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-388") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKdvuy2bffdvtxtcyne7yvm2ufl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-389") {
        addForeignKeyConstraint(baseColumnNames: "action_template_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKe0p5nb3v4datmb7gxcdvah75o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-390") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ACTIONS", constraintName: "FKe2e0g80hg3uajb4mb48d1k92y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-391") {
        addForeignKeyConstraint(baseColumnNames: "AD_HOC_ALERT_ID", baseTableName: "AD_HOC_TOPIC_CONCEPTS", constraintName: "FKe3ogu1iyyv0d8jxiu1m5nu1dc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-392") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ALERTS", constraintName: "FKe4dcmnjtoep5k5c638sllylqu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-393") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_RCONFIG", constraintName: "FKe9j0svwa1r5mwvgm3bf5m6eq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-394") {
        addForeignKeyConstraint(baseColumnNames: "topic_id", baseTableName: "MEETING", constraintName: "FKef6s7ftgt9xb8fvpsp5lbuxrc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-395") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_EVDAS_ALERTS", constraintName: "FKek8566hlirn551hgcxfl9fbpl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-396") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ALERTS", constraintName: "FKel2n4td5vn9pt0x6tmxjkq1o0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-397") {
        addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKenxk6e969lecixm4isf1svn0p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-398") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "VALIDATED_AGG_ALERTS", constraintName: "FKeq4e569rluxwh2bh0fil7efp8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-399") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "RCONFIG", constraintName: "FKf4d2b2hthl4s6fvbvq0uas5av", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-400") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "work_flow_rules_actions", constraintName: "FKf7jgah5sul11a45tsbuggvek4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-401") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKf8kqtv3nu1fgrdlcikfp4nfcm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-402") {
        addForeignKeyConstraint(baseColumnNames: "meeting_actions_id", baseTableName: "meeting_actions", constraintName: "FKf8ro1wl6geleojkypcnjercbp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-403") {
        addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_DELIVERIES_SHARED_WITHS", constraintName: "FKffoorb58n2mlt1sjcg481t97u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-404") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "report_history", constraintName: "FKffw3i3fbs6jyf8kqojxola39s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-405") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_CATEGORY_ID", baseTableName: "VALIDATED_SIGNAL_CATEGORY", constraintName: "FKfmldojgpely7wlug6e2l6q4oo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-406") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "user_group_s", constraintName: "FKfpfe7d6ea5bnivbe7gnc03622", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-407") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_ID", baseTableName: "WORKFLOWRULES_SIGNAL_CATEGORY", constraintName: "FKfubdtk7gwoa8s8919uafl2jj3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-408") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_CASE_ALERT_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKfwtfcguybm93tkxxgtbkf3yn0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-409") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FKg1yueio2f2400ouiv8y29wrji", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-410") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "signal_history", constraintName: "FKg2etf80lqk7p2chk6848imhyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-411") {
        addForeignKeyConstraint(baseColumnNames: "ACTION_TEMPLATE_ID", baseTableName: "WORKFLOWRULES_ACTION_TEMPLATES", constraintName: "FKg7jny08nmnya6p6b4o7fxxpyq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TEMPLATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-412") {
        addForeignKeyConstraint(baseColumnNames: "CATEGORY_ID", baseTableName: "RPT_TEMPLT", constraintName: "FKgcan6gm4h4l0i5xg5qppa4247", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "CATEGORY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-413") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "meeting_actions", constraintName: "FKgd1k2pcfls85gbq25kdko2lm6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-414") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_RCONFIGS_TAGS", constraintName: "FKgjjwq7m2xs0xqjfu6cusg1t5i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-415") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKgqh0iu7w66xqgio4jv38b5hmf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-416") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DOCUMENT_ID", baseTableName: "VALIDATED_ALERT_DOCUMENTS", constraintName: "FKguaxlxc330u6by2rdbpa7o15s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DOCUMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-417") {
        addForeignKeyConstraint(baseColumnNames: "attachment_id", baseTableName: "ATTACHMENT_DESCRIPTION", constraintName: "FKhf1vmfljes8qfvmkksj0r56gr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-418") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "EX_EVDAS_CONFIG_ACTIVITIES", constraintName: "FKhfddlxpqvfaocje2iwp6umh63", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-419") {
        addForeignKeyConstraint(baseColumnNames: "guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKhgeofgkdx1rjj5s2yglnetx6y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GUEST_ATTENDEE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-420") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKhhjqmaurc06u0g2sd8pk0puw3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-421") {
        addForeignKeyConstraint(baseColumnNames: "meeting_owner_id", baseTableName: "MEETING", constraintName: "FKhipac92y3mafijg8snr452hkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-422") {
        addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_VALUE", constraintName: "FKhp88stjs8dwso3j48vdftsop", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-423") {
        addForeignKeyConstraint(baseColumnNames: "GROUP_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKhqjqjxidasmpsaf9jsa1m6fkf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-424") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "SUPER_QUERY", constraintName: "FKhvnmkfa7ifsqm0e5qheseojig", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-425") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "TOPIC_SINGLE_ALERTS", constraintName: "FKhycn6c1tko90ojyv3m9q92b4b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-426") {
        addForeignKeyConstraint(baseColumnNames: "audit_trail_id", baseTableName: "audit_child_log", constraintName: "FKi2lq3l44yxa3o6afk12xj36ip", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audit_log")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-427") {
        addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "EVDAS_CONFIG", constraintName: "FKi7oiwcj0vwl0hljvu0cfgnbat", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-428") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FKig3y20w0203iai5v122a4em5e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-429") {
        addForeignKeyConstraint(baseColumnNames: "signal_strategy_id", baseTableName: "PV_CONCEPT", constraintName: "FKii9w7n9su0kelhwdy64fnoh4a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-430") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "COMMENTS", constraintName: "FKipm49nd1i0adasdsh4c4pkc88", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "COMMENTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-431") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "product_event_history", constraintName: "FKit1j3spl8apjv7adws38jn99a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-432") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "SIGNAL_CHART", constraintName: "FKiwek5gp0875l735x1kt69iaq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-433") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ALERTS", constraintName: "FKixthhci8k983scn1rxj2vrtar", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-434") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "evdas_history", constraintName: "FKj0e4kr6h7ba4h1j66a0qsvwx6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-435") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FKj0jkrus3o90ljhg1b6ebuttyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-436") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RPT_TEMPLTS_TAGS", constraintName: "FKj3em09hnn5j4v4jmtva0y7x4t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-437") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "EVDAS_ALERT", constraintName: "FKjdbvyyl4pc5gyonimroq4q9vp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-438") {
        addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FKjf5iat84x47a3wdllmpj3qa8r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-439") {
        addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "SHARED_WITH", constraintName: "FKjpnhmuynt25rwtymb8c6qbcu2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-440") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKjqg0j3akc948s35kbuqglqjqi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-441") {
        addForeignKeyConstraint(baseColumnNames: "VALUE_ID", baseTableName: "VALUES_PARAMS", constraintName: "FKjyc9aqn1mnratmb78qts1vh2x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-442") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKk2v2qa969crmd21cb8ey8eykl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-443") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUPS", constraintName: "FKk93ggugty4vt6eff2xct44qvs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-444") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKkdxec3jbhkxo588a4kpg4h9l4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-445") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_GROUPS", constraintName: "FKktxqfutcchko7u9fuaksacutx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-446") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FKkwhfmdea1iw8ad05voiu1c88x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-447") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKl5ki9ms4pq7oyr4wrne2wejf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_STRATEGY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-448") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "evdas_history", constraintName: "FKl751la1u7svv60tmydb5q6rfo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-449") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "RULE_INFORMATION", constraintName: "FKlfungwscmq9bwj56xlirfguy1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-450") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "ALERT_GROUPS", constraintName: "FKllryx3v1k30f1bvhsjbl9knl2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-451") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_actions_id", baseTableName: "validated_signal_actions", constraintName: "FKlqeb869hd13sihhd6a99xq2he", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-452") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "ACTIVITIES", constraintName: "FKls2t8mye4vopk1sjq1tgy9xoc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-453") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "signal_history", constraintName: "FKludcb63lf92hniiym7xpdxhfa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-454") {
        addForeignKeyConstraint(baseColumnNames: "meeting_activities_id", baseTableName: "meeting_activities", constraintName: "FKlxb7shjcemeke6qig312nemnv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-455") {
        addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_DATA_ID", baseTableName: "RPT_RESULT", constraintName: "FKly7i8dpff5f3c218d99g8tcyj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT_DATA")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-456") {
        addForeignKeyConstraint(baseColumnNames: "meeting_guest_attendee_id", baseTableName: "meeting_guest_attendee", constraintName: "FKm139ii2baukit0klasjx4oglf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-457") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKm2082jiexg6r0a70a2r34utlj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-458") {
        addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "RPT_FIELD", constraintName: "FKmahmpsyseeqtjochaf36fxyap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-459") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKmci0hn62phky4qlig0uy8walr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-460") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIVITIES", constraintName: "FKmeoolraf0sivqb157mg1xxxhn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-461") {
        addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FKmgkm5e29p5ywffcfmh13k9vj3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-462") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_ERROR", constraintName: "FKmkyr5aidjfp14wd8ytgv48xdn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-463") {
        addForeignKeyConstraint(baseColumnNames: "COLUMN_MEASURE_ID", baseTableName: "DTAB_TEMPLTS_COL_MEAS", constraintName: "FKmop226wsllt5fkw8ldhbio60u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_COLUMN_MEASURE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-464") {
        addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_COL_MEAS_MEASURES", constraintName: "FKmowrqjxvrwjdeg7wh33rdsi7e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_MEASURE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-465") {
        addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "AGG_ALERT", constraintName: "FKmsj5gkjd0efc694obp77atnam", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-466") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKmv3t9bke7ovuq4xhntvupq5b4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-467") {
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKmx3gs9bdqe5we8t8dyfwlnu57", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_COMMENT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-468") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "PRODUCT_DICTIONARY_CACHE", constraintName: "FKn6vycx5pad3o4yv5lqgyg9b0k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-469") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKncksxdxhk9k1wg170uk6hxo01", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-470") {
        addForeignKeyConstraint(baseColumnNames: "TOPIC_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKneg18gip2c5vrkf5o5d6hjvp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TOPIC")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-471") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "TOPIC_ADHOC_ALERTS", constraintName: "FKnf31xnc5tocj5toxf64f4n5bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-472") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_SIGNAL_CONCEPTS", constraintName: "FKnfgumbuu5ryminkxap7nlgc5p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-473") {
        addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVUSER", constraintName: "FKnjdqpgxtqa9kv92hc0h5a08nh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PREFERENCE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-474") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "AGG_ALERT", constraintName: "FKnpwnuktj5h2nepfrniywuihe2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-475") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "meeting_pvuser", constraintName: "FKnrjk7qtd13c4vbk8cf2fng3c2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-476") {
        addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FKnwuo5xfk46w6axjqsd3so9ta4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-477") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "RCONFIG", constraintName: "FKnxr5jgx0rmqc0qwpvfb1hxedb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-478") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_EVDAS_CONFIG_TAGS", constraintName: "FKo38uflw88p4l42e75wfeg4fvv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-479") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "EVDAS_ALERT", constraintName: "FKo3yoly6j5mslepff4epm8i3un", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-480") {
        addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FKodukke9wren1g3gjo55qi9tgp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-481") {
        addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "CONFIGURATION_GROUPS", constraintName: "FKogeskmvxyhq4smapupwir8iya", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-482") {
        addForeignKeyConstraint(baseColumnNames: "user_department_id", baseTableName: "pvuser_user_department", constraintName: "FKohxfvcmtidnanlyvlj3qq9hyt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_department")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-483") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "SINGLE_TOPIC_CONCEPTS", constraintName: "FKop4ektpb6ulll4ubcyvxd2352", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-484") {
        addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FKorl7uf55p01t8h6cb6jvuc4r0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-485") {
        addForeignKeyConstraint(baseColumnNames: "evdas_alert_id", baseTableName: "ACTIONS", constraintName: "FKotw99rdoeydidva8eqngeoim1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-486") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_DOCUMENT", constraintName: "FKou8nl9rcuc35kn5yuucm1jfso", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-487") {
        addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FKov8bwj2sfdyxlom6956cikgmk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-488") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKp6qbm114tnjnrpkfb5lpvp82k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ROLE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-489") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIONS", constraintName: "FKp8d85ni1hck589sq6qbxtk2ur", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-490") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_ALERT_ID", baseTableName: "EVDAS_TOPIC_CONCEPTS", constraintName: "FKpaayw5smfn63cqc92ncfgtcwk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-491") {
        addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIVITIES", constraintName: "FKpb7op1gsmnueani37t07swp18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-492") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "signal_history", constraintName: "FKpc3meb0lkklinhdq8yvhcc094", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-493") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "AGG_ALERT", constraintName: "FKpj612eglsaa2cqi8ds4c3hs3c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-494") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FKpjt02j3shpuy0tenek9p1ol40", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-495") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "MEETING", constraintName: "FKpkleruks8op5rxe14p0u28dun", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-496") {
        addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "work_flows_work_flow_rules", constraintName: "FKpoh9sko55k4u4l4bg6lc198hv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-497") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_STATUS", constraintName: "FKptxxpv688i41soevklto2001n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-498") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "EVDAS_CONFIGURATION_GROUPS", constraintName: "FKpw1bhalfblwu0e31df1oib0cr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-499") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_ALERT_COMMENTS", constraintName: "FKq24b4km13myw6pxki435bulke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-500") {
        addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "validated_signal_actions", constraintName: "FKq4785d5esdy86x6prso7o75el", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-501") {
        addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "ex_rconfig_activities", constraintName: "FKqaijpt3i6wpk4o7f4j9iofrqj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-502") {
        addForeignKeyConstraint(baseColumnNames: "single_case_alert_id", baseTableName: "ACTIONS", constraintName: "FKqcrth3pehaa07ed4hf83abyp4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-503") {
        addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "CASE_HISTORY", constraintName: "FKqh491q7pp8sjet2ep7jy7ilda", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-504") {
        addForeignKeyConstraint(baseColumnNames: "meeting_attendees_id", baseTableName: "meeting_pvuser", constraintName: "FKqiltr5xajp4m5fu0je6set5g3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEETING")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-505") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_COMMENTS", constraintName: "FKqj7ub0xptnbuhouvowxamuj26", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-506") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKqjo3uqjw699p81p6wscpc2b3r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_DATE_RANGE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-507") {
        addForeignKeyConstraint(baseColumnNames: "log_id", baseTableName: "IMPORT_DETAIL", constraintName: "FKqkcg7dewuxqjklxa4xuo3yb6w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "IMPORT_LOG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-508") {
        addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "CASE_HISTORY", constraintName: "FKqqc5icyo2ky7wkegjktf5inwd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-509") {
        addForeignKeyConstraint(baseColumnNames: "agg_alert_id", baseTableName: "ACTIONS", constraintName: "FKqqmeb3p8bx7819mcuweun42qu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-510") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_EXP_VALUE_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FKqtw40uo0tk5l20r78rp1h37wf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUERY_EXP_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-511") {
        addForeignKeyConstraint(baseColumnNames: "MEDICAL_CONCEPTS_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKr2ep7rrgudpgn2aeip674gffo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "MEDICAL_CONCEPTS")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-512") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FKr4dvi6dx2akkxj6sg1edfu4y8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-513") {
        addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKrepui5ibyqyi0ylkhdmrggn9x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-514") {
        addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "pvuser_safety_groups", constraintName: "FKriwpg5obw3hvucl89nm7cyfg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-515") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "RCONFIG", constraintName: "FKrwhfg2lf7qokjhuat482craww", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-516") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EVDAS_ALERT", constraintName: "FKrx1ksfdkal8plsauewx9wcoje", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-517") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERS_ROLES", constraintName: "FKs6fht51c12chh1glukanj1vuu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-518") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_RCONFIG", constraintName: "FKscrg3o7ey1xr3ac0oldqvq1cg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-519") {
        addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "SUPER_QRS_TAGS", constraintName: "FKsfbmqgx8dwvbbdtkas0af9v09", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-520") {
        addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ALERTS", constraintName: "FKsg3649m44b1j9j7kg3saeacwx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-521") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FKskup7oh10mmw5vlh2ivihu010", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-522") {
        addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FKsqppqusike0nnjet4x4elrkom", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-523") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_TOPIC_CONCEPTS", constraintName: "FKsy5o19eqrc5385sfoidonp43l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-524") {
        addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FKsysb0klybb1kk1o3f5auerjx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-525") {
        addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FKt2b7h0ovgst4owagdj3bhj57t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-526") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKtirjstq25k3s016xchl6imgv8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-527") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKtlpylud98ev61g2km1cl6e7q0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-528") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKtlr3lhis3k57s65e2wrfm6tpx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-529") {
        addForeignKeyConstraint(baseColumnNames: "user_user_departments_id", baseTableName: "pvuser_user_department", constraintName: "FKtmsfftkx8df5gdlnxgjyub76b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-530") {
        addForeignKeyConstraint(baseColumnNames: "ROWS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FKxuj1f2muxnhq2fgcqbxuycjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-531") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "product_event_history", constraintName: "FKy156nyus7pkpemc1lk9wuy73", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-532") {
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-533") {
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-534") {
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-535") {
        dropTable(tableName: "HT_EX_CUSTOM_SQL_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-536") {
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-537") {
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-538") {
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-539") {
        dropTable(tableName: "HT_EX_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-540") {
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-541") {
        dropTable(tableName: "HT_EX_QUERY_SET")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-542") {
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-543") {
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-544") {
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-545") {
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-546") {
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-547") {
        dropTable(tableName: "HT_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-548") {
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-549") {
        dropTable(tableName: "HT_QUERY_SET")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-550") {
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-551") {
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-552") {
        dropTable(tableName: "HT_SQL_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-553") {
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-554") {
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-555") {
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-556") {
        dropTable(tableName: "HT_SUPER_QUERY")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-557") {
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "chetansharma (generated)", id: "1527527772654-558") {
        dropTable(tableName: "HT_VALUE")
    }
}
