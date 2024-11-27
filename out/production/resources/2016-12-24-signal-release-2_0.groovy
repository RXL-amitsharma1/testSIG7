databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1482582270254-1") {
		createTable(tableName: "ACCESS_CONTROL_GROUP") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACCESS_CONTROPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(200 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "LDAP_GROUP_NAME", type: "varchar2(30 char)") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(30 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-2") {
		createTable(tableName: "ACTION_CONFIGURATIONS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_CONFIGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "description_local", type: "varchar2(255 char)")

			column(name: "display_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "is_email_enabled", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-3") {
		createTable(tableName: "ACTION_TYPES") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_TYPESPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "description_local", type: "varchar2(255 char)")

			column(name: "display_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-4") {
		createTable(tableName: "ACTIONS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTIONSPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "action_status", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "agg_alert_id", type: "number(19,0)")

			column(name: "alert_id", type: "number(19,0)")

			column(name: "alert_type", type: "varchar2(255 char)")

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "comments", type: "varchar2(4000 char)")

			column(name: "completed_date", type: "timestamp")

			column(name: "config_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "details", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "due_date", type: "timestamp")

			column(name: "owner_id", type: "number(19,0)")

			column(name: "single_case_alert_id", type: "number(19,0)")

			column(name: "type_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "viewed", type: "number(1,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-5") {
		createTable(tableName: "ACTIVITIES") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTIVITIESPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_id", type: "number(19,0)")

			column(name: "assigned_to_id", type: "number(19,0)")

			column(name: "attributes", type: "varchar2(4000 char)")

			column(name: "case_number", type: "varchar2(255 char)")

			column(name: "client_name", type: "varchar2(255 char)")

			column(name: "DETAILS", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "display", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "varchar2(255 char)")

			column(name: "ip_address", type: "varchar2(255 char)")

			column(name: "JUSTIFICATION", type: "varchar2(4000 char)")

			column(name: "performed_by_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "suspect_product", type: "varchar2(255 char)")

			column(name: "timestamp", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "type_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "activities_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-6") {
		createTable(tableName: "ACTIVITY_TYPE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTIVITY_TYPEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-7") {
		createTable(tableName: "AEVAL_TYPE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AEVAL_TYPEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "description_local", type: "varchar2(255 char)")

			column(name: "display", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "display_name", type: "varchar2(255 char)")

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-8") {
		createTable(tableName: "AGG_ALERT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AGG_ALERTPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_configuration_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "cum_fatal_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "cum_serious_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "cum_spon_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "cum_study_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "detected_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "due_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "eb05", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "eb95", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "ebgm", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "exec_configuration_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "flagged", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "new_fatal_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "new_serious_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "new_spon_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "new_study_count", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "product_id", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "product_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "prr_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "pt", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "pt_code", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "ror_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "soc", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "workflow_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-9") {
		createTable(tableName: "ALERT_COMMENT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_COMMENTPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "case_number", type: "varchar2(255 char)")

			column(name: "COMMENTS", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "product_family", type: "varchar2(255 char)")

			column(name: "product_name", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-10") {
		createTable(tableName: "ALERT_DOCUMENT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_DOCUMENPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_id", type: "number(19,0)")

			column(name: "author", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "chronicle_id", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "comments", type: "varchar2(255 char)")

			column(name: "document_link", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "document_status", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "document_type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "product_name", type: "varchar2(4000 char)")

			column(name: "start_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "status_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "target_date", type: "timestamp")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-11") {
		createTable(tableName: "ALERT_GROUPS") {
			column(name: "alert_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-12") {
		createTable(tableName: "alert_stop_list") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "alert_stop_liPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "activated", type: "number(1,0)")

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "date_deactivated", type: "timestamp")

			column(name: "EVENT_SELECTION", type: "clob")

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "varchar2(255 char)")

			column(name: "PRODUCT_SELECTION", type: "clob")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-13") {
		createTable(tableName: "ALERTS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERTSPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_configuration_id", type: "number(19,0)")

			column(name: "alert_id", type: "number(19,0)")

			column(name: "alert_version", type: "number(19,0)")

			column(name: "assigned_to_id", type: "number(19,0)")

			column(name: "attributes", type: "clob")

			column(name: "case_number", type: "varchar2(255 char)")

			column(name: "created_by", type: "varchar2(255 char)")

			column(name: "date_created", type: "timestamp")

			column(name: "detected_date", type: "timestamp")

			column(name: "disposition_id", type: "number(19,0)")

			column(name: "due_date", type: "timestamp")

			column(name: "event_selection", type: "varchar2(255 char)")

			column(name: "exec_config_id", type: "number(19,0)")

			column(name: "flagged", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "varchar2(255 char)")

			column(name: "notes", type: "varchar2(255 char)")

			column(name: "priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "review_date", type: "timestamp")

			column(name: "workflow_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_type", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "alert_rmp_rems_ref", type: "varchar2(255 char)")

			column(name: "country_of_incidence", type: "varchar2(255 char)")

			column(name: "description", type: "varchar2(4000 char)")

			column(name: "detected_by", type: "varchar2(255 char)")

			column(name: "formulations", type: "varchar2(255 char)")

			column(name: "indication", type: "varchar2(255 char)")

			column(name: "initial_data_source", type: "varchar2(255 char)")

			column(name: "issue_previously_tracked", type: "number(1,0)")

			column(name: "name", type: "varchar2(4000 char)")

			column(name: "number_oficsrs", type: "number(10,0)")

			column(name: "owner_id", type: "number(19,0)")

			column(name: "product_selection", type: "varchar2(4000 char)")

			column(name: "public_alert", type: "number(1,0)")

			column(name: "ref_type", type: "varchar2(255 char)")

			column(name: "report_type", type: "varchar2(255 char)")

			column(name: "shared_with_id", type: "number(19,0)")

			column(name: "study_selection", type: "varchar2(4000 char)")

			column(name: "topic", type: "varchar2(4000 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-14") {
		createTable(tableName: "ALGO_CONFIGURATION") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALGO_CONFIGURPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "algo_type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "business_config_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "first_time_rule", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "justification", type: "varchar2(255 char)")

			column(name: "max_threshold", type: "double precision")

			column(name: "min_threshold", type: "double precision")

			column(name: "target_disposition_id", type: "number(19,0)")

			column(name: "target_state_id", type: "number(19,0)")

			column(name: "config_type", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-15") {
		createTable(tableName: "attachment") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "attachmentPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "content_type", type: "varchar2(255 char)")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ext", type: "varchar2(255 char)")

			column(name: "input_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "length", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "lnk_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "poster_class", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "poster_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-16") {
		createTable(tableName: "attachment_link") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "attachment_liPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "reference_class", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "reference_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-17") {
		createTable(tableName: "AUDIT_LOG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUDIT_LOGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CATEGORY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "PARENT_OBJECT", type: "varchar2(255 char)")

			column(name: "PARENT_OBJECT_ID", type: "number(19,0)")

			column(name: "USERNAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-18") {
		createTable(tableName: "AUDIT_LOG_FIELD_CHANGE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "AUDIT_LOG_FIEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "AUDIT_LOG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ENTITY_ID", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "ENTITY_NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "FIELD_NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NEW", type: "clob")

			column(name: "ORIGINAL", type: "clob")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-19") {
		createTable(tableName: "BUSINESS_CONFIGURATION") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "BUSINESS_CONFPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "analysis_level", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "auto_state_configuration", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "calculate_ebgm_ci", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "calculate_prr_ci", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "calculate_ror_ci", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ebgm_custom_config", type: "varchar2(4000 char)")

			column(name: "enable_ebgm", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "enable_ebgm_custom_config", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "enable_prr", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "enable_prr_custom_config", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "enable_ror", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "enable_ror_custom_config", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "min_cases", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "prr_custom_config", type: "varchar2(4000 char)")

			column(name: "ror_custom_config", type: "varchar2(4000 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-20") {
		createTable(tableName: "CASE_COLUMN_JOIN_MAPPING") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_COLUMN_JPK")
			}

			column(name: "COLUMN_NAME", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "MAP_COLUMN_NAME", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}

			column(name: "MAP_TABLE_NAME_ATM_ID", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}

			column(name: "TABLE_NAME_ATM_ID", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-21") {
		createTable(tableName: "CASE_HISTORY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_HISTORYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "case_number", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "case_version", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "change", type: "varchar2(255 char)")

			column(name: "created_by", type: "varchar2(255 char)")

			column(name: "current_assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "current_disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "current_priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "current_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp")

			column(name: "follow_up_number", type: "number(10,0)")

			column(name: "is_latest", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "JUSTIFICATION", type: "varchar2(4000 char)")

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "varchar2(255 char)")

			column(name: "product_family", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-22") {
		createTable(tableName: "CATEGORY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CATEGORYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DEFAULT_NAME", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-23") {
		createTable(tableName: "CLL_TEMPLT") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CLL_TEMPLTPK")
			}

			column(name: "COLUMNS_RF_INFO_LIST_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "COL_SHOW_TOTAL", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "GROUPING_RF_INFO_LIST_ID", type: "number(19,0)")

			column(name: "PAGE_BREAK_BY_GROUP", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "RENAME_GROUPING", type: "clob")

			column(name: "RENAME_ROW_COLS", type: "clob")

			column(name: "ROW_COLS_RF_INFO_LIST_ID", type: "number(19,0)")

			column(name: "SUPPRESS_COLUMN_LIST", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-24") {
		createTable(tableName: "COGNOS_REPORT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "COGNOS_REPORTPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(1000 char)")

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "URL", type: "varchar2(1000 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-25") {
		createTable(tableName: "COMMENTS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "COMMENTSPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "commented_by_id", type: "number(19,0)")

			column(name: "content", type: "varchar2(255 char)")

			column(name: "input_date", type: "timestamp")

			column(name: "parent_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-26") {
		createTable(tableName: "CONFIGURATION_GROUPS") {
			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "configuration_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "shared_groups_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-27") {
		createTable(tableName: "DATE_RANGE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DATE_RANGEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RNG_END_ABSOLUTE", type: "timestamp")

			column(name: "DATE_RNG_END_DELTA", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RNG_ENUM", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RNG_START_ABSOLUTE", type: "timestamp")

			column(name: "DATE_RNG_START_DELTA", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-28") {
		createTable(tableName: "DELIVERIES_EMAIL_USERS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_USER", type: "varchar2(255 char)")

			column(name: "EMAIL_USER_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-29") {
		createTable(tableName: "DELIVERIES_RPT_FORMATS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-30") {
		createTable(tableName: "DELIVERIES_SHARED_WITHS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-31") {
		createTable(tableName: "DELIVERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DELIVERYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-32") {
		createTable(tableName: "DISPOSITION") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DISPOSITIONPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "description_local", type: "varchar2(255 char)")

			column(name: "display", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "display_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "notify", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "validated_confirmed", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-33") {
		createTable(tableName: "DTAB_COL_MEAS_MEASURES") {
			column(name: "DTAB_COL_MEAS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "MEASURE_ID", type: "number(19,0)")

			column(name: "MEASURES_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-34") {
		createTable(tableName: "DTAB_COLUMN_MEASURE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DTAB_COLUMN_MPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "COLUMNS_RFI_LIST_ID", type: "number(19,0)")

			column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-35") {
		createTable(tableName: "DTAB_MEASURE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DTAB_MEASUREPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CUSTOM_EXPRESSION", type: "varchar2(255 char)")

			column(name: "FROM_DATE", type: "timestamp")

			column(name: "TO_DATE", type: "timestamp")

			column(name: "date_range_count", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "PERCENTAGE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_SUBTOTALS", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_TOTAL", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_TOTAL_AS_COLS", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_TOTAL_ROWS", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "MEASURE_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-36") {
		createTable(tableName: "DTAB_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DTAB_TEMPLTPK")
			}

			column(name: "ROWS_RF_INFO_LIST_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-37") {
		createTable(tableName: "DTAB_TEMPLTS_COL_MEAS") {
			column(name: "DTAB_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "COLUMN_MEASURE_ID", type: "number(19,0)")

			column(name: "COLUMN_MEASURE_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-38") {
		createTable(tableName: "EMAIL_LOG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EMAIL_LOGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "message", type: "varchar2(4000 char)")

			column(name: "sent_on", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "sent_to", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "subject", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-39") {
		createTable(tableName: "ETL_SCHEDULE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ETL_SCHEDULEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DISABLED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_INITIAL", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "REPEAT_INTERVAL", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "SCHEDULE_NAME", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "START_DATETIME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-40") {
		createTable(tableName: "EVAL_REF_TYPE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EVAL_REF_TYPEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(255 char)")

			column(name: "DISPLAY", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-41") {
		createTable(tableName: "EX_CLL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CLL_TEMPLTPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-42") {
		createTable(tableName: "EX_CUSTOM_SQL_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CUSTOM_SQLPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-43") {
		createTable(tableName: "EX_CUSTOM_SQL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CUS_TPL_SQLPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-44") {
		createTable(tableName: "EX_DATE_RANGE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DATE_RANGEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RNG_END_ABSOLUTE", type: "timestamp")

			column(name: "DATE_RNG_ENUM", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RNG_START_ABSOLUTE", type: "timestamp")

			column(name: "EXECUTED_AS_OF", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-45") {
		createTable(tableName: "EX_DELIVERIES_EMAIL_USERS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_USER", type: "varchar2(255 char)")

			column(name: "EMAIL_USER_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-46") {
		createTable(tableName: "EX_DELIVERIES_RPT_FORMATS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-47") {
		createTable(tableName: "EX_DELIVERIES_SHARED_WITHS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-48") {
		createTable(tableName: "EX_DELIVERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DELIVERYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-49") {
		createTable(tableName: "EX_DTAB_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DTAB_TEMPLPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-50") {
		createTable(tableName: "EX_NCASE_SQL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_NCASE_SQL_PK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-51") {
		createTable(tableName: "EX_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERYPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-52") {
		createTable(tableName: "EX_QUERY_EXP") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_EXPPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-53") {
		createTable(tableName: "EX_QUERY_SET") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_SETPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-54") {
		createTable(tableName: "EX_QUERY_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_VALUPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-55") {
		createTable(tableName: "EX_RCONFIG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_RCONFIGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "AS_OF_VERSION_DATE", type: "timestamp")

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "BLANK_VALUES", type: "clob")

			column(name: "SELECTED_TIME_ZONE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

			column(name: "DESCRIPTION", type: "varchar2(200 char)")

			column(name: "DRUG_TYPE", type: "varchar2(255 char)")

			column(name: "EVALUATE_DATE_AS", type: "varchar2(255 char)")

			column(name: "EVENT_SELECTION", type: "clob")

			column(name: "EXCLUDE_FOLLOWUP", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "INCLUDE_LOCKED_VERSION", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "is_auto_trigger", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_ENABLED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_PUBLIC", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp")

			column(name: "limit_primary_path", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(200 char)") {
				constraints(nullable: "false")
			}

			column(name: "NEXT_RUN_DATE", type: "timestamp")

			column(name: "NUM_OF_EXECUTIONS", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "PVUSER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "PRODUCT_SELECTION", type: "clob")

			column(name: "repeat_execution", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")

			column(name: "SELECTED_DATA_SOURCE", type: "varchar2(255 char)")

			column(name: "STUDY_SELECTION", type: "clob")

			column(name: "TOTAL_EXECUTION_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-56") {
		createTable(tableName: "ex_rconfig_activities") {
			column(name: "EX_CONFIG_ACTIVITIES_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ACTIVITY_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-57") {
		createTable(tableName: "EX_RCONFIGS_TAGS") {
			column(name: "EXC_RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-58") {
		createTable(tableName: "EX_SQL_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_SQL_VALUEPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-59") {
		createTable(tableName: "EX_STATUS") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_STATUSPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "END_TIME", type: "number(19,0)")

			column(name: "EX_STATUS", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "FREQUENCY", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MESSAGE", type: "clob")

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "next_run_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "owner_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_ID", type: "number(19,0)")

			column(name: "RPT_VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SECTION_NAME", type: "varchar2(255 char)")

			column(name: "STACK_TRACE", type: "clob")

			column(name: "START_TIME", type: "number(19,0)")

			column(name: "TEMPLATE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-60") {
		createTable(tableName: "EX_STATUSES_RPT_FORMATS") {
			column(name: "EX_STATUS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-61") {
		createTable(tableName: "EX_STATUSES_SHARED_WITHS") {
			column(name: "EX_STATUS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-62") {
		createTable(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
			column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_QUERY_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-63") {
		createTable(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
			column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_TEMPLT_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-64") {
		createTable(tableName: "EX_TEMPLT_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_TEMPLT_QUEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_DATE_RANGE_INFO_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_QUERY_ID", type: "number(19,0)")

			column(name: "EX_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "FOOTER", type: "varchar2(255 char)")

			column(name: "HEADER", type: "varchar2(255 char)")

			column(name: "HEADER_DATE_RANGE", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_LEVEL", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "TITLE", type: "varchar2(255 char)")

			column(name: "EX_TEMPLT_QUERY_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-65") {
		createTable(tableName: "EX_TEMPLT_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_TEMPLT_VALPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-66") {
		createTable(tableName: "GROUPS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "GROUPSPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "allowed_prod", type: "clob")

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(200 char)")

			column(name: "is_active", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "selected_datasource", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-67") {
		createTable(tableName: "IMPORT_DETAIL") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "IMPORT_DETAILPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "input_identifier", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "log_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "message", type: "varchar2(4000 char)")

			column(name: "rec_num", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-68") {
		createTable(tableName: "IMPORT_LOG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "IMPORT_LOGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "end_time", type: "timestamp")

			column(name: "num_failed", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "num_succeeded", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "response", type: "varchar2(255 char)")

			column(name: "start_time", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-69") {
		createTable(tableName: "MISC_CONFIG") {
			column(name: "KEY_1", type: "varchar2(255 char)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "MISC_CONFIGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "VALUE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-70") {
		createTable(tableName: "NONCASE_SQL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "NONCASE_SQL_TPK")
			}

			column(name: "COL_NAME_LIST", type: "varchar2(2048 char)") {
				constraints(nullable: "false")
			}

			column(name: "NON_CASE_SQL", type: "clob") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-71") {
		createTable(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES") {
			column(name: "NONCASE_SQL_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SQL_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-72") {
		createTable(tableName: "NOTIFICATION") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "NOTIFICATIONPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "detail_url", type: "varchar2(255 char)")

			column(name: "EC_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "LVL", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "MESSAGE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "MSG_ARGS", type: "varchar2(255 char)")

			column(name: "USER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-73") {
		createTable(tableName: "PARAM") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PARAMPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "LOOKUP", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "VALUE", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-74") {
		createTable(tableName: "PREFERENCE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PREFERENCEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "is_email_enabled", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "LOCALE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "TIME_ZONE", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-75") {
		createTable(tableName: "PRIORITY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PRIORITYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "default_priority", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "description_local", type: "varchar2(255 char)")

			column(name: "display", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "display_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "priority_order", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "review_period", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-76") {
		createTable(tableName: "product_event_history") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "product_eventPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "as_of_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "change", type: "varchar2(255 char)")

			column(name: "created_by", type: "varchar2(255 char)")

			column(name: "date_created", type: "timestamp")

			column(name: "disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "eb05", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "eb95", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "ebgm", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "execution_date", type: "timestamp")

			column(name: "is_latest", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "justification", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "varchar2(255 char)")

			column(name: "priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "product_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "prr_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "ror_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-77") {
		createTable(tableName: "PVS_AUDIT_LOG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PVS_AUDIT_LOGPK")
			}

			column(name: "actor", type: "varchar2(255 char)")

			column(name: "class_name", type: "varchar2(255 char)")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "new_value", type: "varchar2(255 char)")

			column(name: "old_value", type: "varchar2(255 char)")

			column(name: "persisted_object_id", type: "varchar2(255 char)")

			column(name: "persisted_object_version", type: "number(19,0)")

			column(name: "property_name", type: "varchar2(255 char)")

			column(name: "uri", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-78") {
		createTable(tableName: "PVS_STATE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PVS_STATEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "description_local", type: "varchar2(255 char)")

			column(name: "display", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "display_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "final_state", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-79") {
		createTable(tableName: "PVUSER") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PVUSERPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ACCOUNT_EXPIRED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "ACCOUNT_LOCKED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "BAD_PASSWORD_ATTEMPTS", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ENABLED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "PASSWORD_EXPIRED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "PREFERENCE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "USERNAME", type: "varchar2(30 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-80") {
		createTable(tableName: "pvuser_safety_groups") {
			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "safety_group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-81") {
		createTable(tableName: "pvuser_user_department") {
			column(name: "user_user_departments_id", type: "number(19,0)")

			column(name: "user_department_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-82") {
		createTable(tableName: "PVUSERS_ROLES") {
			column(name: "role_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-83") {
		createTable(tableName: "QUERIES_QRS_EXP_VALUES") {
			column(name: "QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_EXP_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-84") {
		createTable(tableName: "QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERYPK")
			}

			column(name: "reassess_listedness", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-85") {
		createTable(tableName: "QUERY_EXP_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_EXP_VALPK")
			}

			column(name: "OPERATOR_ID", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "REPORT_FIELD_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-86") {
		createTable(tableName: "QUERY_SET") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_SETPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-87") {
		createTable(tableName: "QUERY_SETS_SUPER_QRS") {
			column(name: "QUERY_SET_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SUPER_QUERY_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-88") {
		createTable(tableName: "QUERY_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_VALUEPK")
			}

			column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-89") {
		createTable(tableName: "RCONFIG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RCONFIGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ADJUST_PER_SCHED_FREQUENCY", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_rmp_rems_ref", type: "varchar2(255 char)")

			column(name: "alert_trigger_cases", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_trigger_days", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "AS_OF_VERSION_DATE", type: "timestamp")

			column(name: "AS_OF_VERSION_DATE_DELTA", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "BLANK_VALUES", type: "clob")

			column(name: "SELECTED_TIME_ZONE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RANGE_TYPE", type: "varchar2(255 char)")

			column(name: "DESCRIPTION", type: "varchar2(200 char)")

			column(name: "DRUG_TYPE", type: "varchar2(255 char)")

			column(name: "EVALUATE_DATE_AS", type: "varchar2(255 char)")

			column(name: "EVENT_SELECTION", type: "clob")

			column(name: "EXCLUDE_FOLLOWUP", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "EXECUTING", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "INCLUDE_LOCKED_VERSION", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_AUTO_TRIGGER", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_ENABLED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_PUBLIC", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp")

			column(name: "limit_primary_path", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(200 char)") {
				constraints(nullable: "false")
			}

			column(name: "NEXT_RUN_DATE", type: "timestamp")

			column(name: "NUM_OF_EXECUTIONS", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "on_or_after_date", type: "timestamp")

			column(name: "PVUSER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "PRODUCT_SELECTION", type: "clob")

			column(name: "repeat_execution", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "review_period", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")

			column(name: "SELECTED_DATA_SOURCE", type: "varchar2(255 char)")

			column(name: "STUDY_SELECTION", type: "clob")

			column(name: "TOTAL_EXECUTION_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-90") {
		createTable(tableName: "RCONFIG_DISPOSITION") {
			column(name: "CONFIGURATION_DISPOSITION_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DISPOSITION_ID", type: "number(19,0)")

			column(name: "RCONFIG_DISPOSITION_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-91") {
		createTable(tableName: "RCONFIGS_TAGS") {
			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-92") {
		createTable(tableName: "ROLE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ROLEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "AUTHORITY", type: "varchar2(50 char)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(200 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-93") {
		createTable(tableName: "RPT_ERROR") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_ERRORPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_STATUS", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MESSAGE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_RESULT_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-94") {
		createTable(tableName: "RPT_FIELD") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELDPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SOURCE_COLUMN_MASTER_ID", type: "varchar2(80 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATA_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_FORMAT", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(255 char)")

			column(name: "DIC_LEVEL", type: "number(10,0)")

			column(name: "DIC_TYPE", type: "varchar2(255 char)")

			column(name: "RPT_FIELD_GRPNAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "ISAUTOCOMPLETE", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_TEXT", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LIST_DOMAIN_CLASS", type: "varchar2(255 char)")

			column(name: "LMSQL", type: "clob")

			column(name: "NAME", type: "varchar2(128 char)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_SELECTABLE", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "TEMPLT_CLL_SELECTABLE", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "TEMPLT_DTCOL_SELECTABLE", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "TEMPLT_DTROW_SELECTABLE", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "TRANSFORM", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-95") {
		createTable(tableName: "RPT_FIELD_GROUP") {
			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELD_GROPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-96") {
		createTable(tableName: "RPT_FIELD_INFO") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELD_INFPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ARGUS_NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "BLINDED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "COMMA_SEPARATED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "CUSTOM_EXPRESSION", type: "varchar2(255 char)")

			column(name: "DATASHEET", type: "varchar2(255 char)")

			column(name: "RENAME_VALUE", type: "varchar2(255 char)")

			column(name: "RPT_FIELD_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RF_INFO_LIST_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "sort", type: "varchar2(255 char)")

			column(name: "SORT_LEVEL", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "STACK_ID", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "SUPPRESS_REPEATING", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "RF_INFO_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-97") {
		createTable(tableName: "RPT_FIELD_INFO_LIST") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FLD_INFPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-98") {
		createTable(tableName: "RPT_RESULT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_RESULTPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_RESULT_DATA_ID", type: "number(19,0)")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)")

			column(name: "EX_STATUS", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "FILTER_VERSION_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "FREQUENCY", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_ROWS", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "REASSESS_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "REPORT_ROWS", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "REPORT_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RUN_DATE", type: "timestamp")

			column(name: "SCHEDULED_PVUSER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SEQUENCE", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TOTAL_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "VERSION_ROWS", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "FILTERED_VERSION_ROWS", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "VERSION_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-99") {
		createTable(tableName: "RPT_RESULT_DATA") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_RESULT_DAPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CROSS_TAB_SQL", type: "clob")

			column(name: "GTT_SQL", type: "clob")

			column(name: "HEADER_SQL", type: "clob")

			column(name: "QUERY_SQL", type: "clob")

			column(name: "REPORT_SQL", type: "clob")

			column(name: "VALUE", type: "blob")

			column(name: "VERSION_SQL", type: "clob")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-100") {
		createTable(tableName: "RPT_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_TEMPLTPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CATEGORY_ID", type: "number(19,0)")

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(200 char)")

			column(name: "factory_default", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "HASBLANKS", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_PUBLIC", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(200 char)") {
				constraints(nullable: "false")
			}

			column(name: "ORIG_TEMPLT_ID", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "PV_USER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "REASSESS_LISTEDNESS", type: "varchar2(255 char)")

			column(name: "TEMPLATE_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-101") {
		createTable(tableName: "RPT_TEMPLTS_TAGS") {
			column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-102") {
		createTable(tableName: "safety_group") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "safety_groupPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "allowed_prod", type: "clob")

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-103") {
		createTable(tableName: "SHARED_WITH") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SHARED_WITHPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "STATUS", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_USER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-104") {
		createTable(tableName: "SINGLE_CASE_ALERT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SINGLE_CASE_APK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_configuration_id", type: "number(19,0)")

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "attributes", type: "clob")

			column(name: "case_number", type: "varchar2(255 char)")

			column(name: "case_version", type: "number(10,0)")

			column(name: "created_by", type: "varchar2(255 char)")

			column(name: "date_created", type: "timestamp")

			column(name: "detected_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "due_date", type: "timestamp")

			column(name: "exec_config_id", type: "number(19,0)")

			column(name: "flagged", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "follow_up_exists", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "follow_up_number", type: "number(10,0)")

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "varchar2(255 char)")

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "product_family", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "product_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "pt", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "review_date", type: "timestamp")

			column(name: "STATE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-105") {
		createTable(tableName: "SOURCE_COLUMN_MASTER") {
			column(name: "REPORT_ITEM", type: "varchar2(80 char)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SOURCE_COLUMNPK")
			}

			column(name: "COLUMN_NAME", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}

			column(name: "COLUMN_TYPE", type: "varchar2(1 char)") {
				constraints(nullable: "false")
			}

			column(name: "CONCATENATED_FIELD", type: "varchar2(1 char)")

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LM_DECODE_COLUMN", type: "varchar2(40 char)")

			column(name: "LM_JOIN_COLUMN", type: "varchar2(40 char)")

			column(name: "LM_JOIN_EQUI_OUTER", type: "varchar2(1 char)")

			column(name: "LM_TABLE_NAME_ATM_ID", type: "varchar2(40 char)")

			column(name: "MIN_COLUMNS", type: "number(10,0)")

			column(name: "PRIMARY_KEY_ID", type: "number(19,0)")

			column(name: "TABLE_NAME_ATM_ID", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-106") {
		createTable(tableName: "SOURCE_TABLE_MASTER") {
			column(name: "TABLE_NAME", type: "varchar2(40 char)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SOURCE_TABLE_PK")
			}

			column(name: "CASE_JOIN_ORDER", type: "number(10,0)")

			column(name: "CASE_JOIN_EQUI_OUTER", type: "varchar2(1 char)")

			column(name: "HAS_ENTERPRISE_ID", type: "number(10,0)")

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "TABLE_ALIAS", type: "varchar2(5 char)") {
				constraints(nullable: "false")
			}

			column(name: "TABLE_TYPE", type: "varchar2(1 char)") {
				constraints(nullable: "false")
			}

			column(name: "VERSIONED_DATA", type: "varchar2(1 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-107") {
		createTable(tableName: "SQL_QRS_SQL_VALUES") {
			column(name: "SQL_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SQL_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-108") {
		createTable(tableName: "SQL_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_QUERYPK")
			}

			column(name: "QUERY", type: "clob") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-109") {
		createTable(tableName: "SQL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_TEMPLTPK")
			}

			column(name: "COLUMN_NAMES", type: "varchar2(2048 char)") {
				constraints(nullable: "false")
			}

			column(name: "SELECT_FROM_STMT", type: "clob") {
				constraints(nullable: "false")
			}

			column(name: "WHERE_STMT", type: "clob")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-110") {
		createTable(tableName: "SQL_TEMPLT_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_TEMPLT_VAPK")
			}

			column(name: "FIELD", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-111") {
		createTable(tableName: "SQL_TEMPLTS_SQL_VALUES") {
			column(name: "SQL_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SQL_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-112") {
		createTable(tableName: "SQL_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_VALUEPK")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-113") {
		createTable(tableName: "SUPER_QRS_TAGS") {
			column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-114") {
		createTable(tableName: "SUPER_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SUPER_QUERYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY", type: "clob")

			column(name: "created_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(200 char)")

			column(name: "factory_default", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "HAS_BLANKS", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_PUBLIC", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(200 char)") {
				constraints(nullable: "false")
			}

			column(name: "ORIG_QUERY_ID", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "PVUSER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-115") {
		createTable(tableName: "TAG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TAGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-116") {
		createTable(tableName: "TEMPLT_QRS_QUERY_VALUES") {
			column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-117") {
		createTable(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
			column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TEMPLT_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-118") {
		createTable(tableName: "TEMPLT_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TEMPLT_QUERYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RANGE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "FOOTER", type: "varchar2(255 char)")

			column(name: "HEADER", type: "varchar2(255 char)")

			column(name: "HEADER_DATE_RANGE", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "INDX", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "SUPER_QUERY_ID", type: "number(19,0)")

			column(name: "QUERY_LEVEL", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TITLE", type: "varchar2(255 char)")

			column(name: "TEMPLT_QUERY_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-119") {
		createTable(tableName: "TEMPLT_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TEMPLT_VALUEPK")
			}

			column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-120") {
		createTable(tableName: "user_department") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_departmePK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "department_name", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-121") {
		createTable(tableName: "user_group_s") {
			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-122") {
		createTable(tableName: "USER_GROUPS") {
			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-123") {
		createTable(tableName: "VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "VALUEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-124") {
		createTable(tableName: "VALUES_PARAMS") {
			column(name: "VALUE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "PARAM_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-125") {
		createTable(tableName: "WkFL_RUL_DISPOSITIONS") {
			column(name: "disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "workflow_rule_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "allowed_dispositions_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-126") {
		createTable(tableName: "WORK_FLOW_RULES") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "WORK_FLOW_RULPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "approve_required", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "income_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "notify", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "target_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-127") {
		createTable(tableName: "work_flow_rules_actions") {
			column(name: "workflow_rule_actions_id", type: "number(19,0)")

			column(name: "action_id", type: "number(19,0)")

			column(name: "actions_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-128") {
		createTable(tableName: "WORK_FLOW_VARIABLES") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "WORK_FLOW_VARPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-129") {
		createTable(tableName: "WORK_FLOWS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "WORK_FLOWSPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-130") {
		createTable(tableName: "work_flows_work_flow_rules") {
			column(name: "workflow_rule_list_id", type: "number(19,0)")

			column(name: "workflow_rule_id", type: "number(19,0)")

			column(name: "rule_list_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-131") {
		createTable(tableName: "WORKFLOWRULES_GROUPS") {
			column(name: "workflow_rule_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "allowed_groups_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-132") {
		addPrimaryKey(columnNames: "alert_id, group_id", tableName: "ALERT_GROUPS")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-133") {
		addPrimaryKey(columnNames: "role_id, user_id", constraintName: "PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-134") {
		addPrimaryKey(columnNames: "user_id, safety_group_id", tableName: "pvuser_safety_groups")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-135") {
		addPrimaryKey(columnNames: "user_id, group_id", tableName: "user_group_s")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-283") {
		createIndex(indexName: "LDAP_GROUP_NAME_uniq_1482582", tableName: "ACCESS_CONTROL_GROUP", unique: "true") {
			column(name: "LDAP_GROUP_NAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-284") {
		createIndex(indexName: "NAME_uniq_1482582223775", tableName: "ACCESS_CONTROL_GROUP", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-285") {
		createIndex(indexName: "value_uniq_1482582223786", tableName: "AEVAL_TYPE", unique: "true") {
			column(name: "value")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-286") {
		createIndex(indexName: "chronicle_id_uniq_14825822237", tableName: "ALERT_DOCUMENT", unique: "true") {
			column(name: "chronicle_id")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-287") {
		createIndex(indexName: "NAME_uniq_1482582223801", tableName: "CATEGORY", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-288") {
		createIndex(indexName: "value_uniq_1482582223804", tableName: "DISPOSITION", unique: "true") {
			column(name: "value")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-289") {
		createIndex(indexName: "SCHEDULE_NAME_uniq_1482582223", tableName: "ETL_SCHEDULE", unique: "true") {
			column(name: "SCHEDULE_NAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-290") {
		createIndex(indexName: "NAME_uniq_1482582223806", tableName: "EVAL_REF_TYPE", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-291") {
		createIndex(indexName: "name_uniq_1482582223812", tableName: "GROUPS", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-293") {
		createIndex(indexName: "value_uniq_1482582223815", tableName: "PVS_STATE", unique: "true") {
			column(name: "value")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-294") {
		createIndex(indexName: "USERNAME_uniq_1482582223816", tableName: "PVUSER", unique: "true") {
			column(name: "USERNAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-295") {
		createIndex(indexName: "AUTHORITY_uniq_1482582223819", tableName: "ROLE", unique: "true") {
			column(name: "AUTHORITY")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-296") {
		createIndex(indexName: "NAME_uniq_1482582223820", tableName: "RPT_FIELD", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-298") {
		createIndex(indexName: "NAME_uniq_1482582223826", tableName: "TAG", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-299") {
		createIndex(indexName: "name_uniq_1482582223829", tableName: "WORK_FLOW_RULES", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-300") {
		createIndex(indexName: "name_uniq_1482582223832", tableName: "safety_group", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-301") {
		createIndex(indexName: "department_name_uniq_148258", tableName: "user_department", unique: "true") {
			column(name: "department_name")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-302") {
		createSequence(sequenceName: "hibernate_sequence")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-136") {
		addForeignKeyConstraint(baseColumnNames: "agg_alert_id", baseTableName: "ACTIONS", constraintName: "FK_rn3pfvh8hppokjl03hrj5qo8k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-137") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIONS", constraintName: "FK_73phyqwn0d0ckwbxi3ws3hdg7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-138") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIONS", constraintName: "FK_n69qkij7cm262bxvi12ghwnjt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-139") {
		addForeignKeyConstraint(baseColumnNames: "config_id", baseTableName: "ACTIONS", constraintName: "FK_t36hxfaoy31kd9wpyov234eob", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_CONFIGURATIONS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-140") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ACTIONS", constraintName: "FK_lk7kc8d9qffs5o8meyp72kflx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-141") {
		addForeignKeyConstraint(baseColumnNames: "single_case_alert_id", baseTableName: "ACTIONS", constraintName: "FK_nuo909hma39nddbsa7y5lbf0k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-142") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIONS", constraintName: "FK_bibuxry3asmlma3tura0acd8i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TYPES", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-143") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIVITIES", constraintName: "FK_91tsjpqy6n3r9d70wsmo46i7s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-144") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIVITIES", constraintName: "FK_hhu7kkp2chey6kymidh20x5to", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-145") {
		addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "ACTIVITIES", constraintName: "FK_pkeo919hjynpg0b6y8qysey4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-146") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIVITIES", constraintName: "FK_ix2e4vr5aa2aiuyykcq74p1th", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-147") {
		addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FK_r30ffdp2dmycptbusa00x6tar", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-148") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "AGG_ALERT", constraintName: "FK_n7dll8vhg3ykpx0jn0t471rhk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-149") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK_lyybra8sk7nvjxeq8ptsnww0v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-150") {
		addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FK_m7e2g4974ejxe7mhua226stks", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-151") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "AGG_ALERT", constraintName: "FK_jxrfnk4vd3btciga07rw4ljrd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-152") {
		addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "AGG_ALERT", constraintName: "FK_pnk180oe4gbga5hpemhp4qtxt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-153") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_DOCUMENT", constraintName: "FK_t831lo4l5asolxv16h2x7b2fx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-154") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_GROUPS", constraintName: "FK_5l39cesxpigh2gm93fg4645tr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-155") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "ALERT_GROUPS", constraintName: "FK_l8ua081wndv387qa9n4h1pk8h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-156") {
		addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ALERTS", constraintName: "FK_lfdevjhujdrnelui77hjay5ig", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-157") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ALERTS", constraintName: "FK_rys5r6xmydc1gkgi7ch1ny3ua", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-158") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ALERTS", constraintName: "FK_95d7f8xxt7vpoxb93xpqsdyl5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-159") {
		addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ALERTS", constraintName: "FK_3lgsalj2eh6yalfwojcgk5lfq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-160") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ALERTS", constraintName: "FK_iwx7iqrm2oak26f3tdc3y0956", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-161") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ALERTS", constraintName: "FK_mw97kowxp84chbdou4cwfgaif", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-162") {
		addForeignKeyConstraint(baseColumnNames: "shared_with_id", baseTableName: "ALERTS", constraintName: "FK_sow3n0bhr81ht5asavqdl1uep", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-163") {
		addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "ALERTS", constraintName: "FK_i8qspqbn1p066jrn8pwm6f3bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-164") {
		addForeignKeyConstraint(baseColumnNames: "business_config_id", baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_fvqm6o3ass78s8dy23np04egn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BUSINESS_CONFIGURATION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-165") {
		addForeignKeyConstraint(baseColumnNames: "target_disposition_id", baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_96pf7wii4ac9d92b9yt1nos4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-166") {
		addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_rrmw22x2rdc7xm3vqocb9tlfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-167") {
		addForeignKeyConstraint(baseColumnNames: "lnk_id", baseTableName: "attachment", constraintName: "FK_njvkgmv4mpqxu1yc3nk26b01o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment_link", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-168") {
		addForeignKeyConstraint(baseColumnNames: "AUDIT_LOG_ID", baseTableName: "AUDIT_LOG_FIELD_CHANGE", constraintName: "FK_1x86whymorfaoe3ue1yxb3fny", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AUDIT_LOG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-169") {
		addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK_mwdoavlp59qoa2xxdrkq2ylvq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-170") {
		addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK_hd6x76aiwkgyltmmw403s865", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-171") {
		addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "CASE_HISTORY", constraintName: "FK_q4mxb497604q60d3fs8624und", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-172") {
		addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "CASE_HISTORY", constraintName: "FK_tn9d5k5b65pvqvej7fwi3ioj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-173") {
		addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "CASE_HISTORY", constraintName: "FK_5w7her578fpgwb3e0w5q2von6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-174") {
		addForeignKeyConstraint(baseColumnNames: "current_state_id", baseTableName: "CASE_HISTORY", constraintName: "FK_58y2dla00ia36jab2y0cxghy9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-175") {
		addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK_ayotdydghp7ygfkqs6iuhn0u8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-176") {
		addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK_mgkc77as3kik3tdchonnwjecf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-177") {
		addForeignKeyConstraint(baseColumnNames: "ROW_COLS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK_eiymtbl5n8rwy1vw3j72flgl7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-178") {
		addForeignKeyConstraint(baseColumnNames: "commented_by_id", baseTableName: "COMMENTS", constraintName: "FK_5qas2yte7ho39qilxgyk9v74c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-179") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "COMMENTS", constraintName: "FK_dqk7smk358xns8pnusrcrhs10", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "COMMENTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-180") {
		addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "CONFIGURATION_GROUPS", constraintName: "FK_j3bki0nr7eufxv4ntq5n7bqmr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-181") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "CONFIGURATION_GROUPS", constraintName: "FK_gihgxj62vjwdeafmbo66ofms8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-182") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DELIVERIES_SHARED_WITHS", constraintName: "FK_b5kgs7rd3w9d2gijp8cpe3rf6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-183") {
		addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_COL_MEAS_MEASURES", constraintName: "FK_mau0jlcmgs4b1t88pupmrp5xc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_MEASURE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-184") {
		addForeignKeyConstraint(baseColumnNames: "COLUMNS_RFI_LIST_ID", baseTableName: "DTAB_COLUMN_MEASURE", constraintName: "FK_gdd47anilyc472wtlgrm8tptf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-185") {
		addForeignKeyConstraint(baseColumnNames: "ROWS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FK_avt8dahbs30tolbtfxhdci6sw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-186") {
		addForeignKeyConstraint(baseColumnNames: "COLUMN_MEASURE_ID", baseTableName: "DTAB_TEMPLTS_COL_MEAS", constraintName: "FK_6j0vj30pd28vlwja3s82ywdf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_COLUMN_MEASURE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-187") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_DELIVERIES_SHARED_WITHS", constraintName: "FK_7r64stirj17b6ucr8k1oyxx9o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-188") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_RCONFIG", constraintName: "FK_d8x6wwjt63b2x38txdx729mn8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-189") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_RCONFIG", constraintName: "FK_i773m62enehtq8ctjek12lpgr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-190") {
		addForeignKeyConstraint(baseColumnNames: "ACTIVITY_ID", baseTableName: "ex_rconfig_activities", constraintName: "FK_td2af2juou6ds79v4c8jqm60u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-191") {
		addForeignKeyConstraint(baseColumnNames: "EX_CONFIG_ACTIVITIES_ID", baseTableName: "ex_rconfig_activities", constraintName: "FK_akbd446gw3t3wnc7947v849lh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-192") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_RCONFIGS_TAGS", constraintName: "FK_eiybjrvwdxcyr9acq7uxmrlhb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-193") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_STATUS", constraintName: "FK_n23l2ct6mpvkyextk1jddibs1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-194") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_STATUSES_SHARED_WITHS", constraintName: "FK_snvbpier2deh2fhjhcnhycplo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-195") {
		addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK_ebrincpyuy5xq44g9hs6c598l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-196") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK_otrslpvpvw4fvlrwkfn0amp6s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-197") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK_91bbav1aa2hrdwk34aky7ntjc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-198") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK_hdhr9a3agy09vxkv4dkghtr9o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-199") {
		addForeignKeyConstraint(baseColumnNames: "EX_DATE_RANGE_INFO_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_j1vhe6d21a97imbej3lj8ipei", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_DATE_RANGE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-200") {
		addForeignKeyConstraint(baseColumnNames: "EX_QUERY_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_dnbkas9q33iktvq5x890oabi2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-201") {
		addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_efl187h4pxj2k7blook970lf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-202") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_p9j1aldxwmqkrnvofs2k0qnd6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-203") {
		addForeignKeyConstraint(baseColumnNames: "log_id", baseTableName: "IMPORT_DETAIL", constraintName: "FK_byrgjc0mhmq9pv6ag3f9432gs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "IMPORT_LOG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-204") {
		addForeignKeyConstraint(baseColumnNames: "NONCASE_SQL_TEMPLT_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_80w9mgrrqg7bbi15p7xkhxmes", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-205") {
		addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_cojhmpxfp6cbnnfu9k81aygt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-206") {
		addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "NOTIFICATION", constraintName: "FK_elahxcjybvosq3scydf1ns948", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-207") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "product_event_history", constraintName: "FK_4ak5hjs06eo5f3d0uxmaceuwv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-208") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "product_event_history", constraintName: "FK_shn9bkgudmn4vgad4v8njllxq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-209") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "product_event_history", constraintName: "FK_8tbv3xaf0dxwxym80w1u4s94c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-210") {
		addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "product_event_history", constraintName: "FK_ctjimxg89uplweuic279em856", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-211") {
		addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVUSER", constraintName: "FK_15ky9pn1mkt607gq2r8pcc4kl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PREFERENCE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-212") {
		addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "pvuser_safety_groups", constraintName: "FK_2f62drogu4d59ix7lv555gvn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-213") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_safety_groups", constraintName: "FK_4nbamxoaxjnvilrffxwxa3ge5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-214") {
		addForeignKeyConstraint(baseColumnNames: "user_department_id", baseTableName: "pvuser_user_department", constraintName: "FK_kk8q35v4d43xmp17vg11owi0r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_department", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-215") {
		addForeignKeyConstraint(baseColumnNames: "user_user_departments_id", baseTableName: "pvuser_user_department", constraintName: "FK_59qhkvgqv9c8f7ruavhbyqogx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-216") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERS_ROLES", constraintName: "FK_1kcl7nv9wqah9py58jjher5l3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ROLE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-217") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERS_ROLES", constraintName: "FK_oyjgh7fr570a0ygemtdj6msq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-218") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_EXP_VALUE_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FK_flu98xfixpr283ssi5egf7tse", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_EXP_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-219") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FK_dtgl5ygpmt49blrr8c3ytplu6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-220") {
		addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK_3xbqnfprt48u7uxl7x7cdkre8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-221") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_SET_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FK_mndinf4qm46cjgsnnbbu6ey89", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_SET", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-222") {
		addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FK_m58uate805vdbmtk7uqdsg4n5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-223") {
		addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_VALUE", constraintName: "FK_o7vesodeulidahrra0yogjmbu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-224") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "RCONFIG", constraintName: "FK_h8sk39efsjooi9hu7q0c9ecjx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-225") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "RCONFIG", constraintName: "FK_lf60qnytw72tcmf3n6rt9ek2g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-226") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "RCONFIG", constraintName: "FK_8857pyncipqerh6fe9doj1dwb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-227") {
		addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "RCONFIG_DISPOSITION", constraintName: "FK_ajf3ju8powk325c5moogn2yqw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-228") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RCONFIGS_TAGS", constraintName: "FK_4r07c0u4qnpc2oaoa9ejdprhf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-229") {
		addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FK_mj9ttjm9d8vdnb7qmwpqgfq9m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-230") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_ERROR", constraintName: "FK_2fjiphm6fb8q89lc1139jcip5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-231") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FK_svut1es5umn8c2c1s3j4t0y4g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-232") {
		addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_ID", baseTableName: "RPT_ERROR", constraintName: "FK_t413dtbnahl64eb3d37hi8v9m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-233") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "RPT_FIELD", constraintName: "FK_9qpw6t9tl4rdwmnsxj83vfkix", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-234") {
		addForeignKeyConstraint(baseColumnNames: "SOURCE_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FK_smj5apkwf1ym4aixvl314mwgb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "SOURCE_COLUMN_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-235") {
		addForeignKeyConstraint(baseColumnNames: "RF_INFO_LIST_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK_nyw2ppms5dfmvjc78yu4wc8a2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-236") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK_tfyjr0f7xthasp4ulu74rl8vb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-237") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK_b1iuja8ciarbru6ivucu68gmt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-238") {
		addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_DATA_ID", baseTableName: "RPT_RESULT", constraintName: "FK_3v6wx4paah8raulblaacq0v6m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT_DATA", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-239") {
		addForeignKeyConstraint(baseColumnNames: "SCHEDULED_PVUSER_ID", baseTableName: "RPT_RESULT", constraintName: "FK_ocyw1xhhtuh2wy8womhmcne9n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-240") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK_f9fgxo0mr8pr4oir07uklcndy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-241") {
		addForeignKeyConstraint(baseColumnNames: "CATEGORY_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK_2l4hoa5deg2k3mbxj3pf3r2aa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "CATEGORY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-242") {
		addForeignKeyConstraint(baseColumnNames: "PV_USER_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK_8u2ywn411rvrxuiqwfs1bhkeh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-243") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RPT_TEMPLTS_TAGS", constraintName: "FK_5turyk8hvy1pbarj4qj8usmga", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-244") {
		addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "SHARED_WITH", constraintName: "FK_thham91a8sjhl9wreg4880l8l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-245") {
		addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "SHARED_WITH", constraintName: "FK_70lu15yl8qdmw1rk9o4b06opa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-246") {
		addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_s1qdu47x5gen885jmrrbgeeka", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-247") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_2egb0qm3c8gcwyx9v1sfrvf4u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-248") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_jydu1rjydge3ge2r0nvcgwoh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-249") {
		addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_59c03i0dium9bv3pnqvguiwke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-250") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_aly7o1cjpgbl5q54imrdfdwp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-251") {
		addForeignKeyConstraint(baseColumnNames: "STATE_ID", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_lgtajnilitwgjm5m7rva1sxrh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-252") {
		addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FK_1p5woi8os4xhykn9sa42c30ch", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-253") {
		addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FK_9f3bqo44q2fuawhoilvmrrnne", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-254") {
		addForeignKeyConstraint(baseColumnNames: "SQL_QUERY_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK_5b3l3airrb9onxra5s7cu8sdl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-255") {
		addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK_f3iqsrdl52ram1chmtyv67w7a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-256") {
		addForeignKeyConstraint(baseColumnNames: "SQL_TEMPLT_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_k9qluss7nbvhijrn92tma73g4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-257") {
		addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_em42mjqwovxihsoi5rota6i3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-258") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "SUPER_QRS_TAGS", constraintName: "FK_e92igfb7ydcch5631di91vq0m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-259") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "SUPER_QUERY", constraintName: "FK_mej236u7r2xy2aqatsnekqt9j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-260") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FK_d9xgir92icqr90gehho1xar58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-261") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FK_c3sp0whrnp78g7q9cro1pr61c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-262") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK_9tqpvcaqiw8jwyfy1gonskor0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-263") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_VALUE_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK_ibsa9mgmrutxlkylbsasjxvhl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-264") {
		addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_jmpfwg11woh9ev84tp18vbmg3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DATE_RANGE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-265") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_j0afuhl5yv1ia6dm9fm54ssxk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-266") {
		addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_d55bqwarp4cgr82x9u7p08mhb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-267") {
		addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_nm9txtr8f9hffpldsnndyex4a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-268") {
		addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_VALUE", constraintName: "FK_n66fhiy57kennxsjctg3c6xny", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-269") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "user_group_s", constraintName: "FK_mkbhqavrsnswsncj6o8hlrvkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-270") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_group_s", constraintName: "FK_fwyqsp0akd0daoxy87v3ucj0o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-271") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUPS", constraintName: "FK_clm2ud7rf2jlyi3j06k4x4u3y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-272") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUPS", constraintName: "FK_cp9jvnih3k4j5vil4fqh5gy2b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-273") {
		addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK_9beyqhgld9w1n00ju71972fvf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-274") {
		addForeignKeyConstraint(baseColumnNames: "VALUE_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK_5dtnylqi39k3kumaeg1bhfdfr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-275") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FK_4l2cw6klswchn71ep9d3lqpyo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-276") {
		addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FK_j4ei009tfwkarfna5xwv68jnc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-277") {
		addForeignKeyConstraint(baseColumnNames: "income_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK_2q71fab410moahaqg08mdl2uf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-278") {
		addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK_rn5skykcehqmmvn58t0h6souq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-279") {
		addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "work_flow_rules_actions", constraintName: "FK_1ir9yaobh6df8p6i32vdxxdra", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-280") {
		addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "work_flows_work_flow_rules", constraintName: "FK_arw0gqe0hcv7tnonjka4ky5wa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-281") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FK_nq20htbwq79dgemb8ty2fwulq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1482582270254-282") {
		addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FK_bpga16kermje1m1q08le0a0up", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES", referencesUniqueColumn: "false")
	}
}
