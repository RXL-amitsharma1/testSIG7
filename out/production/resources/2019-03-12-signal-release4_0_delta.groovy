import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue

databaseChangeLog = {

    changeSet(author: "APE (generated)", id: "1552389595224-1") {
        createSequence(sequenceName: "agg_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-2") {
        createSequence(sequenceName: "config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-3") {
        createSequence(sequenceName: "evdas_config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-4") {
        createSequence(sequenceName: "evdas_exec_config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-5") {
        createSequence(sequenceName: "evdas_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-6") {
        createSequence(sequenceName: "exec_config_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-7") {
        createSequence(sequenceName: "sca_sequence")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-8") {
        createTable(tableName: "ADHOC_ALERT_ACTION_TAKEN") {
            column(name: "ADHOC_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ACTION_TAKEN", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-9") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-10") {
        createTable(tableName: "AGG_ALERT_TAGS") {
            column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-11") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-12") {
        createTable(tableName: "ALERT_QUERY_VALUES") {
            column(name: "ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-13") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-14") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-15") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-16") {
        createTable(tableName: "DISPO_RULES_SIGNAL_CATEGORY") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_CATEGORY_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-17") {
        createTable(tableName: "DISPO_RULES_USER_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "USER_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-18") {
        createTable(tableName: "DISPO_RULES_WORKFLOW_GROUP") {
            column(name: "DISPOSITION_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "WORKFLOW_GROUP_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-19") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-20") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-21") {
        createTable(tableName: "EX_ALERT_QUERY_VALUES") {
            column(name: "EX_ALERT_QUERY_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EX_QUERY_VALUE_ID", type: "NUMBER(19, 0)")

            column(name: "EX_QUERY_VALUE_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-22") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-23") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-24") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-25") {
        createTable(tableName: "EX_RCONFIGS_PROD_GRP") {
            column(name: "EXCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-26") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-27") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-28") {
        createTable(tableName: "LITERATURE_ALERT_TAGS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-29") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-30") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-31") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-32") {
        createTable(tableName: "RCONFIGS_PROD_GRP") {
            column(name: "RCONFIG_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PROD_GRP_ID", type: "NUMBER(19, 0)")

            column(name: "PROD_GRP_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-33") {
        createTable(tableName: "SINGLE_ALERT_CON_COMIT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CON_COMIT", type: "VARCHAR2(255 CHAR)")

            column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-34") {
        createTable(tableName: "SINGLE_ALERT_PT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PT", type: "VARCHAR2(255 CHAR)")

            column(name: "pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-35") {
        createTable(tableName: "SINGLE_ALERT_SUSP_PROD") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "PRODUCT_NAME", type: "VARCHAR2(255 CHAR)")

            column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-36") {
        createTable(tableName: "SINGLE_ALERT_TAGS") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "ALERT_TAG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-37") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-38") {
        createTable(tableName: "VALIDATED_LITERATURE_ALERTS") {
            column(name: "LITERATURE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-39") {
        createTable(tableName: "VALIDATED_SIGNAL_RCONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-40") {
        createTable(tableName: "VS_EVAL_METHOD") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVALUATION_METHOD", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-41") {
        createTable(tableName: "VS_EVDAS_CONFIG") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "EVDAS_CONFIG_ID", type: "NUMBER(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-42") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-43") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-44") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-45") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-46") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-47") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-48") {
        createTable(tableName: "test_user_role") {
            column(name: "role_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-49") {
        createTable(tableName: "validated_signal_action_taken") {
            column(name: "validated_signal_id", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "action_taken_string", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-50") {
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

    changeSet(author: "APE (generated)", id: "1552389595224-51") {
        addColumn(tableName: "ALERTS") {
            column(name: "AGG_END_DATE", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-52") {
        addColumn(tableName: "ALERTS") {
            column(name: "AGG_START_DATE", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-53") {
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_DATA_RANGE_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-54") {
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_QUERY_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-55") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "BLIND_PROTECTED", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-56") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "BLIND_PROTECTED", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-57") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CUMULATIVE_CASE_SERIES_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-58") {
        addColumn(tableName: "SIGNAL_REPORT") {
            column(name: "EXECUTED_ALERT_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-59") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EX_ALERT_DATE_RANGE_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-60") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EX_ALERT_QUERY_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-61") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "EX_QUERY_NAME", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-62") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "EX_TEMPLT_NAME", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-63") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "HEADER_PRODUCT_SELECTION", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-64") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "HEADER_PRODUCT_SELECTION", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-65") {
        addColumn(tableName: "SIGNAL_REPORT") {
            column(name: "IS_ALERT_REPORT", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-66") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "IS_LATEST", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-67") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "PRIVACY_PROTECTED", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-68") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "PRIVACY_PROTECTED", type: "number(1, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-69") {
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "QUERY_NAME", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-70") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "QUERY_NAME", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-71") {
        addColumn(tableName: "RCONFIG") {
            column(name: "QUERY_NAME", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-72") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "REPORT_EXECUTION_STATUS", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-73") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "REPORT_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-74") {
        addColumn(tableName: "SIGNAL_REPORT") {
            column(name: "REPORT_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-75") {
        addColumn(tableName: "RCONFIG") {
            column(name: "TEMPLATE_ID", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-76") {
        addColumn(tableName: "ALERTS") {
            column(name: "WORKFLOW_GROUP", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-77") {
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "WORKFLOW_GROUP", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-78") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "WORKFLOW_GROUP", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-79") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "WORKFLOW_GROUP", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-80") {
        addColumn(tableName: "RCONFIG") {
            column(name: "WORKFLOW_GROUP", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-81") {
        addColumn(tableName: "DISPOSITION") {
            column(name: "abbreviation", type: "varchar2(3 CHAR)", defaultValue: "NA") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-82") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "age", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-83") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "agg_alert_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-84") {
        addColumn(tableName: "RCONFIG") {
            column(name: "agg_alert_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-85") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "agg_alert_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-86") {
        addColumn(tableName: "product_event_history") {
            column(name: "agg_case_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-87") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "agg_count_type", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-88") {
        addColumn(tableName: "RCONFIG") {
            column(name: "agg_count_type", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-89") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "agg_count_type", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-90") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "agg_execution_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-91") {
        addColumn(tableName: "RCONFIG") {
            column(name: "agg_execution_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-92") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "agg_execution_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-93") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "agg_report_end_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-94") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "agg_report_start_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-95") {
        addColumn(tableName: "SIGNAL_REPORT") {
            column(name: "alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-96") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "alert_rmp_rems_ref", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-97") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "alert_trigger_cases", type: "number(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-98") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "alert_trigger_days", type: "number(10, 0)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-99") {
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "article_id", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-100") {
        addColumn(tableName: "ACTIONS") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-101") {
        addColumn(tableName: "ACTIVITIES") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-102") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-103") {
        addColumn(tableName: "ALERTS") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-104") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-105") {
        addColumn(tableName: "EVDAS_CONFIG") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-106") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-107") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-108") {
        addColumn(tableName: "RCONFIG") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-109") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-110") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-111") {
        addColumn(tableName: "evdas_history") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-112") {
        addColumn(tableName: "product_event_history") {
            column(name: "assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-113") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "assigned_to_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-114") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "case_init_receipt_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-115") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "case_report_type", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-116") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "case_series_execution_status", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-117") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "changes", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-118") {
        addColumn(tableName: "DISPOSITION") {
            column(name: "color_code", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-119") {
        addColumn(tableName: "ALERTS") {
            column(name: "comment_signal_status", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-120") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "comment_signal_status", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-121") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "con_comit", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-122") {
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-123") {
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-124") {
        addColumn(tableName: "evdas_history") {
            column(name: "config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-125") {
        addColumn(tableName: "product_event_history") {
            column(name: "config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-126") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "country", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-127") {
        addColumn(tableName: "TAG") {
            column(name: "created_by_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-128") {
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "current_assigned_to_group_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-129") {
        addColumn(tableName: "TAG") {
            column(name: "date_created", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-130") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "death", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-131") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "description", type: "clob")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-132") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "detected_by", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-133") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "detected_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-134") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "drug_classification", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-135") {
        addColumn(tableName: "RCONFIG") {
            column(name: "drug_classification", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-136") {
        addColumn(tableName: "evdas_history") {
            column(name: "evdas_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-137") {
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "exec_config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-138") {
        addColumn(tableName: "product_event_history") {
            column(name: "exec_config_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-139") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "executed_query", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-140") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "executed_query_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-141") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "flags", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-142") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "flags", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-143") {
        addColumn(tableName: "GROUPS") {
            column(name: "force_justification", type: "number(1, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-144") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "freq_priority", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-145") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "gender", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-146") {
        addColumn(tableName: "GROUPS") {
            column(name: "group_type", type: "varchar2(255 CHAR)", defaultValue: "USER_GROUP") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-147") {
        addColumn(tableName: "ALERTS") {
            column(name: "ha_date_closed", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-148") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ha_date_closed", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-149") {
        addColumn(tableName: "ALERTS") {
            column(name: "ha_signal_status_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-150") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ha_signal_status_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-151") {
        addColumn(tableName: "PRIORITY") {
            column(name: "icon_class", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-152") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "is_case_series", type: "number(1, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-153") {
        addColumn(tableName: "RCONFIG") {
            column(name: "is_case_series", type: "number(1, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-154") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "is_case_series", type: "number(1, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-155") {
        addColumn(tableName: "WORK_FLOW_RULES") {
            column(name: "is_deleted", type: "number(1, 0)", defaultValueBoolean: 'false') {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-156") {
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "is_global_rule", type: "number(1, 0)", defaultValueBoolean: 'false') {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-157") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "is_latest", type: "number(1, 0)", defaultValueBoolean: 'false') {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-158") {
        addColumn(tableName: "ALERTS") {
            column(name: "last_decision_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-159") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "last_decision_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-160") {
        addColumn(tableName: "ALERTS") {
            column(name: "last_updated_note", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-161") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "listedness", type: "number(1, 0)", defaultValueBoolean: 'false') {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-162") {
        addColumn(tableName: "ACTIONS") {
            column(name: "literature_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-163") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "locked_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-164") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "master_pref_term_all", type: "varchar2(2000 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-165") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "new_ev_link", type: "varchar2(600 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-166") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "new_spont", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-167") {
        addColumn(tableName: "ALERTS") {
            column(name: "note_modified_by", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-168") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "on_or_after_date", type: "timestamp")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-169") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "priority_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-170") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "priority_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-171") {
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "product_id", type: "number(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-172") {
        addColumn(tableName: "ALERT_COMMENT") {
            column(name: "pt_code", type: "number(10, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-173") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "pvr_case_series_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-174") {
        addColumn(tableName: "QUERY_VALUE") {
            column(name: "query_name", type: "varchar2(255 CHAR)", defaultValue: '') {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-175") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "query_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-176") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "ratio_ror_geriatr_vs_others", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-177") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "ratio_ror_paed_vs_others", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-178") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "rechallenge", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-179") {
        addColumn(tableName: "SIGNAL_REPORT") {
            column(name: "report_execution_status", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-180") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "reporters_hcp_flag", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-181") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "requested_by", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-182") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "requested_by", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-183") {
        addColumn(tableName: "DISPOSITION") {
            column(name: "review_completed", type: "number(1, 0)", defaultValueBoolean: 'false') {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-184") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "sdr_geratr", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-185") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "sdr_paed", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-186") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "serious", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-187") {
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "single_alert_id", type: "number(19, 0)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-188") {
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "susp_prod", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-189") {
        addColumn(tableName: "CASE_HISTORY") {
            column(name: "tag_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-190") {
        addColumn(tableName: "product_event_history") {
            column(name: "tag_name", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-191") {
        addColumn(tableName: "RULE_INFORMATION") {
            column(name: "tags", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-192") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "template_name", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-193") {
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "topic", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-194") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "tot_spont", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-195") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "tot_spont_asia", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-196") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "tot_spont_europe", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-197") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "tot_spont_japan", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-198") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "tot_spont_rest", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-199") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "tot_spontnamerica", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-200") {
        addColumn(tableName: "EVDAS_ALERT") {
            column(name: "total_ev_link", type: "varchar2(600 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-201") {
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "total_execution_time", type: "number(19, 0)", defaultValue: '0') {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-202") {
        addColumn(tableName: "AGG_ALERT") {
            column(name: "trend_type", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-203") {
        addColumn(tableName: "SIGNAL_REPORT") {
            column(name: "type", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-204") {
        addColumn(tableName: "SIGNAL_REPORT") {
            column(name: "type_flag", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "APE (generated)", id: "1552389595224-205") {
        createIndex(indexName: "IX_VALIDATED_LITERATURE_ALERTS", tableName: "VALIDATED_LITERATURE_ALERTS", unique: "true") {
            column(name: "LITERATURE_ALERT_ID")

            column(name: "VALIDATED_SIGNAL_ID")
        }

        addPrimaryKey(columnNames: "LITERATURE_ALERT_ID, VALIDATED_SIGNAL_ID", forIndexName: "IX_VALIDATED_LITERATURE_ALERTS", tableName: "VALIDATED_LITERATURE_ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-206") {
        createIndex(indexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities", unique: "true") {
            column(name: "EX_CONFIG_ACTIVITIES_ID")

            column(name: "ACTIVITY_ID")
        }

        addPrimaryKey(columnNames: "EX_CONFIG_ACTIVITIES_ID, ACTIVITY_ID", constraintName: "ex_rconfig_activitiesPK", forIndexName: "IX_ex_rconfig_activitiesPK", tableName: "ex_rconfig_activities")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-207") {
        createIndex(indexName: "IX_test_user_rolePK", tableName: "test_user_role", unique: "true") {
            column(name: "role_id")

            column(name: "user_id")
        }

        addPrimaryKey(columnNames: "role_id, user_id", constraintName: "test_user_rolePK", forIndexName: "IX_test_user_rolePK", tableName: "test_user_role")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-208") {
        addUniqueConstraint(columnNames: "NAME", constraintName: "UC_ALERT_TAGNAME_COL", tableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-209") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_DISPO_DISPLAY_NAME_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-210") {
        addUniqueConstraint(columnNames: "name", constraintName: "UC_DISPOSITION_RULESNAME_COL", tableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-211") {
        addUniqueConstraint(columnNames: "display_name", constraintName: "UC_PRIO_DISP_NAME_COL", tableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-213") {
        addUniqueConstraint(columnNames: "token", constraintName: "UC_SPOTFIRE_SESSIONTOKEN_COL", tableName: "spotfire_session")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-214") {
        addUniqueConstraint(columnNames: "authority", constraintName: "UC_TEST_ROLEAUTHORITY_COL", tableName: "test_role")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-215") {
        addUniqueConstraint(columnNames: "email", constraintName: "UC_TEST_SAML_USEREMAIL_COL", tableName: "test_saml_user")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-216") {
        addUniqueConstraint(columnNames: "username", constraintName: "UC_TEST_SAML_USERUSERNAME_COL", tableName: "test_saml_user")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-217") {
        addUniqueConstraint(columnNames: "alert_type, user_id, name", constraintName: "UK7285101c782d3bb42a19d76eade0", tableName: "view_instance")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-218") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FK1egn4hip9c2px7kmifks81ilj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-219") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_RCONFIG", constraintName: "FK1mah9k2g3a2vog2rc5l3oyhap", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-220") {
        addForeignKeyConstraint(baseColumnNames: "literature_configuration_id", baseTableName: "EX_LITERATURE_DATE_RANGE", constraintName: "FK23j99y5cju9o2h64tv6c4xq1f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-221") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK2alihh3mnbowd4bubkjigrt2q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-222") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "TAG", constraintName: "FK2jyfx1eu2cdnehuj8dqwrd6pi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-223") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK3277qdq090x04o2i21gcksi92", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-224") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "evdas_history", constraintName: "FK373ar2qi8f0e9w43cus7umvfm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-225") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FK37lt1se9b4d25ksatot1whkt7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-226") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "test_user_role", constraintName: "FK3g03ml77a0pr2nsyno5k23e0f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_role")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-227") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ALERT", constraintName: "FK51t9uul1wlam3muhfsdeni535", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-228") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FK530mahbw0a4gx3tet1q3w2g0l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-229") {
        addForeignKeyConstraint(baseColumnNames: "EVDAS_CONFIG_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FK5qse9kfhl8x3c7v4yl99yy5ha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-230") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "view_instance", constraintName: "FK61t29rs90yvo8nrrjuy8hnrpp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-231") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "EVDAS_CONFIG", constraintName: "FK6sm0nv9c76tc7xx351jufvlo5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-232") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FK6vwyki46c40l23phcl2jsjovv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-233") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FK7e6rwklblhyoo34rnjvek8fin", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-234") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_RCONFIG", constraintName: "FK7jnrbgs0ar70viidp7mwns3sc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-235") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "ALERTS", constraintName: "FK831h6rebhwqcu11l3bmv3m9gt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-236") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FK8afup0gx565jdbopkd4b9xalg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-237") {
        addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_ALERT_QUERY_VALUES", constraintName: "FK8ipmwav0ngw8l0nwefligk05i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-238") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_SIGNAL_CATEGORY", constraintName: "FK8j3m9m71xssnstcr7sadeg87f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-239") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FK8jqoqf0lhagmub5an0yxoma3g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-240") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FK9tk3chwgfb3ci8vloblnxd53b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_EVDAS_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-241") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "RCONFIG", constraintName: "FKa2kbegt02usojx08bka5tlcqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-242") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa5gt5n32yb0s455yd36wvutkd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-243") {
        addForeignKeyConstraint(baseColumnNames: "advanced_filter_id", baseTableName: "view_instance", constraintName: "FKa5uu337r7cfmxb18x68709tf1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ADVANCED_FILTER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-244") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKa8rlw0012wytmrfayjlx3bw0e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-245") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "test_user_role", constraintName: "FKacxggj2twirtdb9ysv9g5q2sa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "test_saml_user")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-246") {
        addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKaims0i1suspmoa1et17ll8g86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-247") {
        addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKayc7ooteg4uqk1y8oth5gi85w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-248") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKb004pyaimrqt9rvj6i1vcu382", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-249") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKb1h9h6gsjltr4to435axmlqba", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-250") {
        addForeignKeyConstraint(baseColumnNames: "EX_ALERT_DATE_RANGE_ID", baseTableName: "EX_RCONFIG", constraintName: "FKb5kjpnalh2rbc3x34i1aw9073", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_ALERT_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-251") {
        addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "LITERATURE_CONFIG", constraintName: "FKbh94j1hyx8woow3tuhfpw771n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-252") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "RCONFIGS_PROD_GRP", constraintName: "FKbhhfbf7noaxk5m7dv8fs6vhry", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-253") {
        addForeignKeyConstraint(baseColumnNames: "USER_GROUP_ID", baseTableName: "DISPO_RULES_USER_GROUP", constraintName: "FKc9jts755p0p0fmxn0ppc276aw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-254") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKcf0w313ha302l1ybaf3vy5d5d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-255") {
        addForeignKeyConstraint(baseColumnNames: "target_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcms27yedf7sdog3u36hlqg9iu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-256") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKcuf5j28ivfuw9jkg1mlthxkbn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-257") {
        addForeignKeyConstraint(baseColumnNames: "incoming_disposition_id", baseTableName: "DISPOSITION_RULES", constraintName: "FKcwneoxfjkmjy0q8v73phwc63n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-258") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVAL_METHOD", constraintName: "FKe0ksh1e8gfwkuuc38rs6xbavk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-259") {
        addForeignKeyConstraint(baseColumnNames: "lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKe4a3tfxg6bd0wg0b7pvfmp0pk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-260") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIVITIES", constraintName: "FKeaneople79bqyu7x49t5reovi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-261") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKehg8x540mv0qpy5plqyf7ktqt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-262") {
        addForeignKeyConstraint(baseColumnNames: "SIGNAL_CATEGORY_ID", baseTableName: "DISPO_RULES_SIGNAL_CATEGORY", constraintName: "FKeslqouxq7266lck0ouermoo0b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SIGNAL_CATEGORY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-263") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VS_EVDAS_CONFIG", constraintName: "FKf1ryj8ajrpjphnyv68dq5bll2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-264") {
        addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "LITERATURE_CONFIG", constraintName: "FKfvp89jp3dlfpaymistjk1tedp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-265") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_DATA_RANGE_ID", baseTableName: "RCONFIG", constraintName: "FKguyscui10qub13216q7omwk6r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-266") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_CONFIG", constraintName: "FKhjr0k31x2dckpqrnrq4u8hffp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-267") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "AGG_ALERT", constraintName: "FKhp8lobw060slxq44dpqk23aq7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-268") {
        addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "ALERT_QUERY_VALUES", constraintName: "FKhv09gbc0ogxdov8llx7jt5961", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-269") {
        addForeignKeyConstraint(baseColumnNames: "VALIDATED_SIGNAL_ID", baseTableName: "VALIDATED_LITERATURE_ALERTS", constraintName: "FKid2pii4l2042xd6m3si3yav58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-270") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKitf2xvh5yq2hykg0o1ff2yi6k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-271") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_RCONFIG", constraintName: "FKktk446ckoiyq9ccb0k4k1fa0d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-272") {
        addForeignKeyConstraint(baseColumnNames: "date_range_information_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKlhyif0bp06538q34cbg7o6ni", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_DATE_RANGE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-273") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_LITERATURE_CONFIG", constraintName: "FKmq50wqqf7s1y3ec4mdllb6ueu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-274") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "AGG_ALERT_TAGS", constraintName: "FKmw7ferd2w1sq8d034q2a2dare", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-275") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn52svyiofwkw5lpse0bi7tiup", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-276") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKn6ofry9re33lb76h4npt3cjr0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-277") {
        addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKnu30tvrow0gcfq8mug780icdi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-278") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "RCONFIG", constraintName: "FKnwf5n98xlybm9cmlufdikkb31", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-279") {
        addForeignKeyConstraint(baseColumnNames: "literature_alert_id", baseTableName: "ACTIONS", constraintName: "FKo8vf0yxsu6jv9d9xddv9owyxo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-280") {
        addForeignKeyConstraint(baseColumnNames: "SINGLE_ALERT_ID", baseTableName: "SINGLE_ALERT_TAGS", constraintName: "FKopm5twbskg380tovcrs4r2gxs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-281") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "product_event_history", constraintName: "FKoqw80p3b38v6stbo0ic2u08vf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-282") {
        addForeignKeyConstraint(baseColumnNames: "ALERT_TAG_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKoxf0kmlxo6jbssodqw9a20l72", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERT_TAG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-283") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "EVDAS_ALERT", constraintName: "FKpb61fes9spi2lu1n9k4oq9m05", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-284") {
        addForeignKeyConstraint(baseColumnNames: "PROD_GRP_ID", baseTableName: "EX_RCONFIGS_PROD_GRP", constraintName: "FKpiydv7gsh3jat1r6upba3jfsl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_group")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-285") {
        addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "DISPO_RULES_WORKFLOW_GROUP", constraintName: "FKpr2qa0uljoummmnormrftubha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-286") {
        addForeignKeyConstraint(baseColumnNames: "CONFIG_ID", baseTableName: "VALIDATED_SIGNAL_RCONFIG", constraintName: "FKptphnx7oype1403pwd2wk9963", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-287") {
        addForeignKeyConstraint(baseColumnNames: "LITERATURE_ALERT_ID", baseTableName: "LITERATURE_ALERT_TAGS", constraintName: "FKq8lv3dxpesh2l167udqs606vn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "LITERATURE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-288") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKqry992h07weddj9bprvrhjb3j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-289") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKqtymlypfnndkl92xpmlyn6wgi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-290") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "LITERATURE_ACTIVITY", constraintName: "FKr2gmw2pc2lgkyxy708wuv8u5b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-291") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ALERTS", constraintName: "FKr339y18wo70qkphso8k28p4n5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-292") {
        addForeignKeyConstraint(baseColumnNames: "product_dictionary_cache_id", baseTableName: "ALLOWED_DICTIONARY_CACHE", constraintName: "FKr4nitfmbmp9dht2pk246d68e9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-293") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "ALERT_TAG", constraintName: "FKrrbo69cgkt2oof0qend38karl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-294") {
        addForeignKeyConstraint(baseColumnNames: "current_assigned_to_group_id", baseTableName: "CASE_HISTORY", constraintName: "FKs60gq6t7tjj3hsunqhur4w35a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-295") {
        addForeignKeyConstraint(baseColumnNames: "ha_signal_status_id", baseTableName: "VALIDATED_SIGNAL", constraintName: "FKs64fr0mhkeoburd4i5hvvtmxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-296") {
        addForeignKeyConstraint(baseColumnNames: "TEMPLATE_ID", baseTableName: "RCONFIG", constraintName: "FKsh216m1mb3g41hm0h6w12wh12", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-297") {
        addForeignKeyConstraint(baseColumnNames: "ex_lit_search_config_id", baseTableName: "LITERATURE_ALERT", constraintName: "FKsm5fds0flxc0po7eqgc5gqgpj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_LITERATURE_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-298") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUP_MAPPING", constraintName: "FKsp010pdhbqal79g9q1y3kl5v3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-299") {
        addForeignKeyConstraint(baseColumnNames: "validated_signal_id", baseTableName: "validated_signal_action_taken", constraintName: "FKt5h2kt8e2kf76xpxs11tw98ek", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-300") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_group_id", baseTableName: "ACTIONS", constraintName: "FKt8jxq56nworpeaop4oarjkmpy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-301") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_GROUP", baseTableName: "ALERTS", constraintName: "FKtawq2r3by3tlx2bx8ohm6ssdb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-302") {
        addForeignKeyConstraint(baseColumnNames: "ADHOC_ALERT_ID", baseTableName: "ADHOC_ALERT_ACTION_TAKEN", constraintName: "FKte7ljx7lhewxd219su74acrd7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-303") {
        dropForeignKeyConstraint(baseTableName: "RULE_INFORMATION", constraintName: "FK13B21D1J7IVOJO0UELGA0E95L")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-304") {
        dropForeignKeyConstraint(baseTableName: "CASE_HISTORY", constraintName: "FK1XEJYDE30QQCO71CHAAUD68WA")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-305") {
        dropForeignKeyConstraint(baseTableName: "QUERY_VALUE", constraintName: "FK2CT5VGLS6XKW3M81A4BPNJ6PN")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-306") {
        dropForeignKeyConstraint(baseTableName: "TEMPLT_QUERY", constraintName: "FK34MOF8I2IMJGYQY96JWAUS2W2")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-307") {
        dropForeignKeyConstraint(baseTableName: "VALIDATED_SIGNAL", constraintName: "FK3HQ5P8KQTBY4PLI3SL769BQDQ")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-308") {
        dropForeignKeyConstraint(baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK42YOW13FEE7HXGTSLSY44D2JL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-309") {
        dropForeignKeyConstraint(baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FK498L090335M7H30RB26QY51H5")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-310") {
        dropForeignKeyConstraint(baseTableName: "ALERTS", constraintName: "FK49MVQUQCE3Y88Y7BG6GQ6CYFS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-311") {
        dropForeignKeyConstraint(baseTableName: "SIGNAL_HISTORY", constraintName: "FK52S7RA0YHX7NLWW3J0SOMS7I")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-312") {
        dropForeignKeyConstraint(baseTableName: "PRODUCT_EVENT_HISTORY", constraintName: "FK58HG7QHAUWR3OXU6J6YW8B649")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-313") {
        dropForeignKeyConstraint(baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK5JHXVS3ET4NYSR91WKHNWV15M")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-314") {
        dropForeignKeyConstraint(baseTableName: "EVDAS_HISTORY", constraintName: "FK6LCP6URQ7U4NOKL4C70RDNHDB")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-315") {
        dropForeignKeyConstraint(baseTableName: "RCONFIGS_TAGS", constraintName: "FK6N3123MKGCUFGBTX0L6UK4MPA")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-316") {
        dropForeignKeyConstraint(baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK89YMED0WLHC1TW9K2R0YV53YY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-317") {
        dropForeignKeyConstraint(baseTableName: "EVDAS_TAGS", constraintName: "FKAU1WUYCLGDHGGX6LEYDF3942A")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-318") {
        dropForeignKeyConstraint(baseTableName: "SINGLE_CASE_ALERT", constraintName: "FKBA6UHHJXF8IMDF8Y0GOKP9C8E")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-319") {
        dropForeignKeyConstraint(baseTableName: "EVDAS_ALERT", constraintName: "FKBL1P01G7XKC98CBTHAQNDCEBH")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-320") {
        dropForeignKeyConstraint(baseTableName: "CONFIGURATION_GROUPS", constraintName: "FKBVCYM800DFQ16DAFHVVBBO56")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-321") {
        dropForeignKeyConstraint(baseTableName: "VALIDATED_SIGNAL", constraintName: "FKE0P5NB3V4DATMB7GXCDVAH75O")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-322") {
        dropForeignKeyConstraint(baseTableName: "EX_RCONFIGS_TAGS", constraintName: "FKGJJWQ7M2XS0XQJFU6CUSG1T5I")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-323") {
        dropForeignKeyConstraint(baseTableName: "TEMPLT_VALUE", constraintName: "FKHP88STJS8DWSO3J48VDFTSOP")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-324") {
        dropForeignKeyConstraint(baseTableName: "SUPER_QUERY", constraintName: "FKHVNMKFA7IFSQM0E5QHESEOJIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-325") {
        dropForeignKeyConstraint(baseTableName: "EVDAS_CONFIG", constraintName: "FKI7OIWCJ0VWL0HLJVU0CFGNBAT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-326") {
        dropForeignKeyConstraint(baseTableName: "ALERT_GROUPS", constraintName: "FKKTXQFUTCCHKO7U9FUAKSACUTX")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-327") {
        dropForeignKeyConstraint(baseTableName: "ALERT_GROUPS", constraintName: "FKLLRYX3V1K30F1BVHSJBL9KNL2")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-328") {
        dropForeignKeyConstraint(baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FKMGKM5E29P5YWFFCFMH13K9VJ3")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-329") {
        dropForeignKeyConstraint(baseTableName: "AGG_ALERT", constraintName: "FKMSJ5GKJD0EFC694OBP77ATNAM")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-330") {
        dropForeignKeyConstraint(baseTableName: "EX_EVDAS_CONFIG_TAGS", constraintName: "FKO38UFLW88P4L42E75WFEG4FVV")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-331") {
        dropForeignKeyConstraint(baseTableName: "CONFIGURATION_GROUPS", constraintName: "FKOGESKMVXYHQ4SMAPUPWIR8IYA")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-332") {
        dropForeignKeyConstraint(baseTableName: "TEMPLT_QUERY", constraintName: "FKOV8BWJ2SFDYXLOM6956CIKGMK")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-333") {
        dropForeignKeyConstraint(baseTableName: "WORK_FLOWS_WORK_FLOW_RULES", constraintName: "FKPOH9SKO55K4U4L4BG6LC198HV")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-334") {
        dropForeignKeyConstraint(baseTableName: "EX_EVDAS_CONFIG", constraintName: "FKQJO3UQJW699P81P6WSCPC2B3R")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-335") {
        dropForeignKeyConstraint(baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FKQTW40UO0TK5L20R78RP1H37WF")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-336") {
        dropForeignKeyConstraint(baseTableName: "SUPER_QRS_TAGS", constraintName: "FKSFBMQGX8DWVBBDTKAS0AF9V09")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-337") {
        dropForeignKeyConstraint(baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FKSKUP7OH10MMW5VLH2IVIHU010")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-338") {
        dropForeignKeyConstraint(baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKTLPYLUD98EV61G2KM1CL6E7Q0")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-339") {
        dropUniqueConstraint(constraintName: "UC_DISDISPLAY_NAME_LOCAL_COL", tableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-340") {
        dropTable(tableName: "ALERT_GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-341") {
        dropTable(tableName: "CONFIGURATION_GROUPS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-342") {
        dropTable(tableName: "EVDAS_TAGS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-343") {
        dropTable(tableName: "EX_CUSTOM_SQL_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-344") {
        dropTable(tableName: "EX_EVDAS_CONFIG_TAGS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-345") {
        dropTable(tableName: "EX_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-346") {
        dropTable(tableName: "EX_QUERY_SET")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-347") {
        dropTable(tableName: "EX_RCONFIGS_TAGS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-348") {
        dropTable(tableName: "HT_CLL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-349") {
        dropTable(tableName: "HT_DTAB_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-350") {
        dropTable(tableName: "HT_EX_CLL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-351") {
        dropTable(tableName: "HT_EX_CUSTOM_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-352") {
        dropTable(tableName: "HT_EX_DTAB_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-353") {
        dropTable(tableName: "HT_EX_NCASE_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-354") {
        dropTable(tableName: "HT_EX_QUERY_EXP")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-355") {
        dropTable(tableName: "HT_EX_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-356") {
        dropTable(tableName: "HT_EX_SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-357") {
        dropTable(tableName: "HT_EX_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-358") {
        dropTable(tableName: "HT_NONCASE_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-359") {
        dropTable(tableName: "HT_PARAM")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-360") {
        dropTable(tableName: "HT_QUERY_EXP_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-361") {
        dropTable(tableName: "HT_QUERY_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-362") {
        dropTable(tableName: "HT_RPT_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-363") {
        dropTable(tableName: "HT_SQL_TEMPLT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-364") {
        dropTable(tableName: "HT_SQL_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-365") {
        dropTable(tableName: "HT_SQL_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-366") {
        dropTable(tableName: "HT_TEMPLT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-367") {
        dropTable(tableName: "HT_VALUE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-368") {
        dropTable(tableName: "QUERIES_QRS_EXP_VALUES")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-369") {
        dropTable(tableName: "QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-370") {
        dropTable(tableName: "QUERY_SET")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-371") {
        dropTable(tableName: "QUERY_SETS_SUPER_QRS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-372") {
        dropTable(tableName: "RCONFIGS_TAGS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-373") {
        dropTable(tableName: "SQL_QRS_SQL_VALUES")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-374") {
        dropTable(tableName: "SQL_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-375") {
        dropTable(tableName: "SUPER_QRS_TAGS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-376") {
        dropTable(tableName: "SUPER_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-377") {
        dropTable(tableName: "WORK_FLOWS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-378") {
        dropTable(tableName: "WORK_FLOWS_WORK_FLOW_RULES")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-379") {
        dropColumn(columnName: "ACTION_TEMPLATE_ID", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-380") {
        dropColumn(columnName: "ALLOWED_FAMILIES", tableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-381") {
        dropColumn(columnName: "ALLOWED_INGREDIENTS", tableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-382") {
        dropColumn(columnName: "ALLOWED_LICENSES", tableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-383") {
        dropColumn(columnName: "ALLOWED_PRODUCTS", tableName: "PRODUCT_DICTIONARY_CACHE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-384") {
        dropColumn(columnName: "CURRENT_STATE_ID", tableName: "CASE_HISTORY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-385") {
        dropColumn(columnName: "DESCRIPTION_LOCAL", tableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-386") {
        dropColumn(columnName: "DESCRIPTION_LOCAL", tableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-387") {
        dropColumn(columnName: "DISPLAY_NAME_LOCAL", tableName: "DISPOSITION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-388") {
        dropColumn(columnName: "DISPLAY_NAME_LOCAL", tableName: "PRIORITY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-389") {
        dropColumn(columnName: "END_DATE", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-390") {
        dropColumn(columnName: "EX_TEMPLT_QUERY_IDX", tableName: "EX_TEMPLT_QUERY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-391") {
        dropColumn(columnName: "FREQUENCY", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-392") {
        dropColumn(columnName: "FREQUENCY", tableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-393") {
        dropColumn(columnName: "FREQUENCY", tableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-394") {
        dropColumn(columnName: "NEW_ABUSE", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-395") {
        dropColumn(columnName: "NEW_CT", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-396") {
        dropColumn(columnName: "NEW_OCCUP", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-397") {
        dropColumn(columnName: "NEW_ROA1", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-398") {
        dropColumn(columnName: "NEW_SPON", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-399") {
        dropColumn(columnName: "PVS_STATE_ID", tableName: "RULE_INFORMATION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-400") {
        dropColumn(columnName: "ROA1", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-401") {
        dropColumn(columnName: "START_DATE", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-402") {
        dropColumn(columnName: "STATE_ID", tableName: "EVDAS_HISTORY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-403") {
        dropColumn(columnName: "STATE_ID", tableName: "PRODUCT_EVENT_HISTORY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-404") {
        dropColumn(columnName: "STATE_ID", tableName: "SIGNAL_HISTORY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-405") {
        dropColumn(columnName: "STATE_ID", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-406") {
        dropColumn(columnName: "TOT_ABUSE", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-407") {
        dropColumn(columnName: "TOT_CT", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-408") {
        dropColumn(columnName: "TOT_OCCUP", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-409") {
        dropColumn(columnName: "TOT_ROA1", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-410") {
        dropColumn(columnName: "TOT_SPON", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-411") {
        dropColumn(columnName: "WORKFLOW_STATE_ID", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-412") {
        dropColumn(columnName: "WORKFLOW_STATE_ID", tableName: "ALERTS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-413") {
        dropColumn(columnName: "WORKFLOW_STATE_ID", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-414") {
        dropColumn(columnName: "WORKFLOW_STATE_ID", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-416") {
        dropNotNullConstraint(columnDataType: "clob", columnName: "PRODUCTS", tableName: "BUSINESS_CONFIGURATION")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-417") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "ACTIONS")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-418") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-419") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "EVDAS_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-420") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "EVDAS_CONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-421") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "EX_RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-422") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "RCONFIG")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-423") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "SINGLE_CASE_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-424") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "VALIDATED_SIGNAL")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-425") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "evdas_history")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-426") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "assigned_to_id", tableName: "product_event_history")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-427") {
        dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "current_assigned_to_id", tableName: "CASE_HISTORY")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-428") {
        dropDefaultValue(columnDataType: "number(1,0)", columnName: "is_cumulative_alert_enabled", tableName: "PREFERENCE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-429") {
        dropDefaultValue(columnDataType: "number(1,0)", columnName: "is_email_enabled", tableName: "PREFERENCE")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-430") {
        dropNotNullConstraint(columnDataType: "varchar(255 CHAR)", columnName: "listed", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-431") {
        dropNotNullConstraint(columnDataType: "varchar(255 CHAR)", columnName: "positive_dechallenge", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-432") {
        dropNotNullConstraint(columnDataType: "varchar(255 CHAR)", columnName: "positive_rechallenge", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-433") {
        dropNotNullConstraint(columnDataType: "varchar(255 CHAR)", columnName: "pregenency", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-434") {
        dropNotNullConstraint(columnDataType: "varchar(255 CHAR)", columnName: "related", tableName: "AGG_ALERT")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-436") {
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "ACTIVITY_ID", tableName: "ex_rconfig_activities")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-437") {
        modifyDataType(columnName: "TABLE_ALIAS", newDataType: "varchar2(10 char)", tableName: "SOURCE_TABLE_MASTER")
    }

    changeSet(author: "APE (generated)", id: "1552389595224-438") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM ACTIVITY_TYPE where value = 'JustificationChange';")
        }
        grailsChange {
            change {
                try {
                    ActivityType activityType = new ActivityType(value: ActivityTypeValue.JustificationChange)
                    activityType.save()
                } catch (Exception ex) {
                    println "##### Error Occurred while add the JustificationChange ActivityType for liquibase change-set 1548777777777-1 ####"
                    ex.printStackTrace()
                }
            }
        }
    }
}
