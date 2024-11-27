databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1456255797755-1") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-2") {
		createTable(tableName: "ACTION_CONFIGURATIONS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_CONFIGPK")
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

			column(name: "is_email_enabled", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-3") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-4") {
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

			column(name: "alert_id", type: "number(19,0)")

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "comments", type: "varchar2(4000 char)")

			column(name: "completed_date", type: "timestamp")

			column(name: "config_id", type: "number(19,0)")

			column(name: "created_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "details", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "due_date", type: "timestamp")

			column(name: "owner_id", type: "number(19,0)")

			column(name: "type_id", type: "number(19,0)")

			column(name: "viewed", type: "number(1,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-5") {
		createTable(tableName: "ACTIVITIES") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTIVITIESPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_id", type: "number(19,0)")

			column(name: "attributes", type: "varchar2(4000 char)")

			column(name: "client_name", type: "varchar2(255 char)")

			column(name: "DETAILS", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "display", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "ip_address", type: "varchar2(255 char)")

			column(name: "JUSTIFICATION", type: "varchar2(4000 char)")

			column(name: "performed_by_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "timestamp", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "type_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "activities_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-6") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-7") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-8") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-9") {
		createTable(tableName: "ALERT_GROUPS") {
			column(name: "alert_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "shared_with_groups_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-10") {
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

			column(name: "name", type: "varchar2(255 char)")

			column(name: "number_oficsrs", type: "number(10,0)")

			column(name: "owner_id", type: "number(19,0)")

			column(name: "product_selection", type: "varchar2(4000 char)")

			column(name: "public_alert", type: "number(1,0)")

			column(name: "ref_type", type: "varchar2(255 char)")

			column(name: "report_type", type: "varchar2(255 char)")

			column(name: "shared_with_id", type: "number(19,0)")

			column(name: "study_selection", type: "varchar2(255 char)")

			column(name: "topic", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-11") {
		createTable(tableName: "ARGUS_COLUMN_MASTER") {
			column(name: "REPORT_ITEM", type: "varchar2(80 char)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ARGUS_COLUMN_PK")
			}

			column(name: "COLUMN_NAME", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}

			column(name: "COLUMN_TYPE", type: "varchar2(1 char)") {
				constraints(nullable: "false")
			}

			column(name: "CONCATENATED_FIELD", type: "varchar2(1 char)")

			column(name: "LM_DECODE_COLUMN", type: "varchar2(40 char)")

			column(name: "LM_JOIN_COLUMN", type: "varchar2(40 char)")

			column(name: "LM_JOIN_EQUI_OUTER", type: "varchar2(1 char)")

			column(name: "LM_TABLE_NAME_ATM_ID", type: "varchar2(40 char)")

			column(name: "PRIMARY_KEY_ID", type: "number(19,0)")

			column(name: "TABLE_NAME_ATM_ID", type: "varchar2(40 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-12") {
		createTable(tableName: "ARGUS_TABLE_MASTER") {
			column(name: "TABLE_NAME", type: "varchar2(40 char)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ARGUS_TABLE_MPK")
			}

			column(name: "CASE_JOIN_ORDER", type: "number(10,0)")

			column(name: "CASE_JOIN_EQUI_OUTER", type: "varchar2(1 char)")

			column(name: "TABLE_ALIAS", type: "varchar2(5 char)") {
				constraints(nullable: "false")
			}

			column(name: "TABLE_TYPE", type: "varchar2(1 char)") {
				constraints(nullable: "false")
			}

			column(name: "VERSIONED_DATA", type: "varchar2(1 char)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-13") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-14") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-15") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-16") {
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

			column(name: "NEW", type: "clob") {
				constraints(nullable: "false")
			}

			column(name: "ORIGINAL", type: "clob") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-17") {
		createTable(tableName: "CASE_COLUMN_JOIN_MAPPING") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_COLUMN_JPK")
			}

			column(name: "COLUMN_NAME", type: "varchar2(40 char)") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-18") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-19") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-20") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-21") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-22") {
		createTable(tableName: "CONFIGURATION_GROUPS") {
			column(name: "configuration_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "shared_groups_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-23") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-24") {
		createTable(tableName: "DELIVERIES_EMAIL_USERS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_USER", type: "varchar2(255 char)")

			column(name: "EMAIL_USER_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-25") {
		createTable(tableName: "DELIVERIES_RPT_FORMATS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-26") {
		createTable(tableName: "DELIVERIES_SHARED_WITHS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-27") {
		createTable(tableName: "DELIVERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DELIVERYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-28") {
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

			column(name: "display_name", type: "varchar2(255 char)")

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

	changeSet(author: "leigao (generated)", id: "1456255797755-29") {
		createTable(tableName: "DTAB_COL_MEAS_MEASURES") {
			column(name: "DTAB_COL_MEAS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "MEASURE_ID", type: "number(19,0)")

			column(name: "MEASURES_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-30") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-31") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-32") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-33") {
		createTable(tableName: "DTAB_TEMPLTS_COL_MEAS") {
			column(name: "DTAB_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "COLUMN_MEASURE_ID", type: "number(19,0)")

			column(name: "COLUMN_MEASURE_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-34") {
		createTable(tableName: "EMAIL_LOG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EMAIL_LOGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

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

	changeSet(author: "leigao (generated)", id: "1456255797755-35") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-36") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-37") {
		createTable(tableName: "EX_CLL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CLL_TEMPLTPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-38") {
		createTable(tableName: "EX_CUSTOM_SQL_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CUSTOM_SQLPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-39") {
		createTable(tableName: "EX_CUSTOM_SQL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CUS_TPL_SQLPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-40") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-41") {
		createTable(tableName: "EX_DELIVERIES_EMAIL_USERS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_USER", type: "varchar2(255 char)")

			column(name: "EMAIL_USER_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-42") {
		createTable(tableName: "EX_DELIVERIES_RPT_FORMATS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-43") {
		createTable(tableName: "EX_DELIVERIES_SHARED_WITHS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-44") {
		createTable(tableName: "EX_DELIVERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DELIVERYPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-45") {
		createTable(tableName: "EX_DTAB_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_DTAB_TEMPLPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-46") {
		createTable(tableName: "EX_NCASE_SQL_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_NCASE_SQL_PK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-47") {
		createTable(tableName: "EX_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERYPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-48") {
		createTable(tableName: "EX_QUERY_EXP") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_EXPPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-49") {
		createTable(tableName: "EX_QUERY_SET") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_SETPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-50") {
		createTable(tableName: "EX_QUERY_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_QUERY_VALUPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-51") {
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

			column(name: "EVALUATE_DATE_AS", type: "varchar2(255 char)")

			column(name: "EVENT_SELECTION", type: "clob")

			column(name: "EXCLUDE_FOLLOWUP", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "EXCLUDE_NON_VALID_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "INCLUDE_LOCKED_VERSION", type: "number(1,0)") {
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

			column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")

			column(name: "STUDY_SELECTION", type: "clob")

			column(name: "TOTAL_EXECUTION_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-52") {
		createTable(tableName: "EX_RCONFIGS_TAGS") {
			column(name: "EXC_RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-53") {
		createTable(tableName: "EX_SQL_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_SQL_VALUEPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-54") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-55") {
		createTable(tableName: "EX_STATUSES_RPT_FORMATS") {
			column(name: "EX_STATUS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-56") {
		createTable(tableName: "EX_STATUSES_SHARED_WITHS") {
			column(name: "EX_STATUS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-57") {
		createTable(tableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES") {
			column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_QUERY_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-58") {
		createTable(tableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES") {
			column(name: "EX_TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_TEMPLT_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-59") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-60") {
		createTable(tableName: "EX_TEMPLT_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_TEMPLT_VALPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-61") {
		createTable(tableName: "GROUPS") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "GROUPSPK")
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
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-62") {
		createTable(tableName: "IMPORT_DETAIL") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "IMPORT_DETAILPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "input_identifier", type: "varchar2(255 char)") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-63") {
		createTable(tableName: "IMPORT_LOG") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "IMPORT_LOGPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "end_time", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "num_failed", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "num_succeeded", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "response", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "start_time", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-64") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-65") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-66") {
		createTable(tableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES") {
			column(name: "NONCASE_SQL_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SQL_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-67") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-68") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-69") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-70") {
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

			column(name: "display_name", type: "varchar2(255 char)")

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "review_period", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-71") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-72") {
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

			column(name: "display_name", type: "varchar2(255 char)")

			column(name: "display_name_local", type: "varchar2(255 char)")

			column(name: "final_state", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-73") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-74") {
		createTable(tableName: "PVUSERS_ROLES") {
			column(name: "role_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-75") {
		createTable(tableName: "QUERIES_QRS_EXP_VALUES") {
			column(name: "QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_EXP_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-76") {
		createTable(tableName: "QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERYPK")
			}

			column(name: "reassess_listedness", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-77") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-78") {
		createTable(tableName: "QUERY_SET") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_SETPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-79") {
		createTable(tableName: "QUERY_SETS_SUPER_QRS") {
			column(name: "QUERY_SET_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SUPER_QUERY_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-80") {
		createTable(tableName: "QUERY_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_VALUEPK")
			}

			column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-81") {
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

			column(name: "DELIVERY_ID", type: "number(19,0)")

			column(name: "DESCRIPTION", type: "varchar2(200 char)")

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

			column(name: "review_period", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "SCHEDULE_DATE", type: "varchar2(1024 char)")

			column(name: "STUDY_SELECTION", type: "clob")

			column(name: "TOTAL_EXECUTION_TIME", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-82") {
		createTable(tableName: "RCONFIGS_TAGS") {
			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-83") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-84") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-85") {
		createTable(tableName: "RPT_FIELD") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELDPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ARGUS_COLUMN_MASTER_ID", type: "varchar2(80 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATA_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_format", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(255 char)")

			column(name: "RPT_FIELD_GROUP_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_TEXT", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LIST_DOMAIN_CLASS", type: "varchar2(255 char)")

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

	changeSet(author: "leigao (generated)", id: "1456255797755-86") {
		createTable(tableName: "RPT_FIELD_GROUP") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FIELD_GROPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-87") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-88") {
		createTable(tableName: "RPT_FIELD_INFO_LIST") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_FLD_INFPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-89") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-90") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-91") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-92") {
		createTable(tableName: "RPT_TEMPLTS_TAGS") {
			column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-93") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-94") {
		createTable(tableName: "SQL_QRS_SQL_VALUES") {
			column(name: "SQL_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SQL_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-95") {
		createTable(tableName: "SQL_QUERY") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_QUERYPK")
			}

			column(name: "QUERY", type: "clob") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-96") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-97") {
		createTable(tableName: "SQL_TEMPLT_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_TEMPLT_VAPK")
			}

			column(name: "FIELD", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-98") {
		createTable(tableName: "SQL_TEMPLTS_SQL_VALUES") {
			column(name: "SQL_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SQL_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-99") {
		createTable(tableName: "SQL_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SQL_VALUEPK")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-100") {
		createTable(tableName: "SUPER_QRS_TAGS") {
			column(name: "SUPER_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-101") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-102") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-103") {
		createTable(tableName: "TEMPLT_QRS_QUERY_VALUES") {
			column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-104") {
		createTable(tableName: "TEMPLT_QRS_TEMPLT_VALUES") {
			column(name: "TEMPLT_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TEMPLT_VALUE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-105") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-106") {
		createTable(tableName: "TEMPLT_VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TEMPLT_VALUEPK")
			}

			column(name: "RPT_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-107") {
		createTable(tableName: "user_group_s") {
			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "groups_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-108") {
		createTable(tableName: "USER_GROUPS") {
			column(name: "group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-109") {
		createTable(tableName: "VALUE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "VALUEPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-110") {
		createTable(tableName: "VALUES_PARAMS") {
			column(name: "VALUE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "PARAM_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-111") {
		createTable(tableName: "WkFL_RUL_DISPOSITIONS") {
			column(name: "workflow_rule_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "allowed_dispositions_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-112") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-113") {
		createTable(tableName: "work_flow_rules_actions") {
			column(name: "workflow_rule_actions_id", type: "number(19,0)")

			column(name: "action_id", type: "number(19,0)")

			column(name: "actions_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-114") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-115") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-116") {
		createTable(tableName: "work_flows_work_flow_rules") {
			column(name: "workflow_rule_list_id", type: "number(19,0)")

			column(name: "workflow_rule_id", type: "number(19,0)")

			column(name: "rule_list_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-117") {
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

	changeSet(author: "leigao (generated)", id: "1456255797755-118") {
		addPrimaryKey(columnNames: "role_id, user_id", constraintName: "PVUSERS_ROLESPK", tableName: "PVUSERS_ROLES")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-232") {
		createIndex(indexName: "LDAP_GRP_NM_uniq_145625579", tableName: "ACCESS_CONTROL_GROUP", unique: "true") {
			column(name: "LDAP_GROUP_NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-233") {
		createIndex(indexName: "NAME_uniq_1456255797574", tableName: "ACCESS_CONTROL_GROUP", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-234") {
		createIndex(indexName: "value_uniq_1456255797586", tableName: "AEVAL_TYPE", unique: "true") {
			column(name: "value")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-235") {
		createIndex(indexName: "chrnic_id_uniq_14562557975", tableName: "ALERT_DOCUMENT", unique: "true") {
			column(name: "chronicle_id")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-236") {
		createIndex(indexName: "NAME_uniq_1456255797600", tableName: "CATEGORY", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-237") {
		createIndex(indexName: "SCH_NM_uniq_14562557976", tableName: "ETL_SCHEDULE", unique: "true") {
			column(name: "SCHEDULE_NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-238") {
		createIndex(indexName: "NAME_uniq_1456255797607", tableName: "EVAL_REF_TYPE", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-239") {
		createIndex(indexName: "name_uniq_1456255797613", tableName: "GROUPS", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-240") {
		createIndex(indexName: "USERNAME_uniq_1456255797618", tableName: "PVUSER", unique: "true") {
			column(name: "USERNAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-241") {
		createIndex(indexName: "AUTHORITY_uniq_1456255797621", tableName: "ROLE", unique: "true") {
			column(name: "AUTHORITY")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-242") {
		createIndex(indexName: "NAME_uniq_1456255797623", tableName: "RPT_FIELD", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-243") {
		createIndex(indexName: "NAME_uniq_1456255797624", tableName: "RPT_FIELD_GROUP", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-244") {
		createIndex(indexName: "NAME_uniq_1456255797630", tableName: "TAG", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-245") {
		createSequence(sequenceName: "hibernate_sequence")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-119") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIONS", constraintName: "FK_73phyqwn0d0ckwbxi3ws3hdg7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-120") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIONS", constraintName: "FK_n69qkij7cm262bxvi12ghwnjt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-121") {
		addForeignKeyConstraint(baseColumnNames: "config_id", baseTableName: "ACTIONS", constraintName: "FK_t36hxfaoy31kd9wpyov234eob", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_CONFIGURATIONS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-122") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ACTIONS", constraintName: "FK_lk7kc8d9qffs5o8meyp72kflx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-123") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIONS", constraintName: "FK_bibuxry3asmlma3tura0acd8i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTION_TYPES", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-124") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ACTIVITIES", constraintName: "FK_91tsjpqy6n3r9d70wsmo46i7s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-125") {
		addForeignKeyConstraint(baseColumnNames: "performed_by_id", baseTableName: "ACTIVITIES", constraintName: "FK_pkeo919hjynpg0b6y8qysey4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-126") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "ACTIVITIES", constraintName: "FK_ix2e4vr5aa2aiuyykcq74p1th", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITY_TYPE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-127") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_DOCUMENT", constraintName: "FK_t831lo4l5asolxv16h2x7b2fx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-128") {
		addForeignKeyConstraint(baseColumnNames: "alert_id", baseTableName: "ALERT_GROUPS", constraintName: "FK_5l39cesxpigh2gm93fg4645tr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ALERTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-129") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "ALERT_GROUPS", constraintName: "FK_l8ua081wndv387qa9n4h1pk8h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-130") {
		addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "ALERTS", constraintName: "FK_lfdevjhujdrnelui77hjay5ig", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-131") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ALERTS", constraintName: "FK_rys5r6xmydc1gkgi7ch1ny3ua", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-132") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "ALERTS", constraintName: "FK_95d7f8xxt7vpoxb93xpqsdyl5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-133") {
		addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "ALERTS", constraintName: "FK_3lgsalj2eh6yalfwojcgk5lfq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-134") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "ALERTS", constraintName: "FK_iwx7iqrm2oak26f3tdc3y0956", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-135") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "ALERTS", constraintName: "FK_mw97kowxp84chbdou4cwfgaif", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-136") {
		addForeignKeyConstraint(baseColumnNames: "shared_with_id", baseTableName: "ALERTS", constraintName: "FK_sow3n0bhr81ht5asavqdl1uep", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-137") {
		addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "ALERTS", constraintName: "FK_i8qspqbn1p066jrn8pwm6f3bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-138") {
		addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "ARGUS_COLUMN_MASTER", constraintName: "FK_qis66b1imlcspcltdkdjx5jh1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-139") {
		addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "ARGUS_COLUMN_MASTER", constraintName: "FK_gwngd940pcuxm5sh24i7wf87h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-140") {
		addForeignKeyConstraint(baseColumnNames: "lnk_id", baseTableName: "attachment", constraintName: "FK_njvkgmv4mpqxu1yc3nk26b01o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "attachment_link", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-141") {
		addForeignKeyConstraint(baseColumnNames: "AUDIT_LOG_ID", baseTableName: "AUDIT_LOG_FIELD_CHANGE", constraintName: "FK_1x86whymorfaoe3ue1yxb3fny", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AUDIT_LOG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-142") {
		addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK_mwdoavlp59qoa2xxdrkq2ylvq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-143") {
		addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK_hd6x76aiwkgyltmmw403s865", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "ARGUS_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-144") {
		addForeignKeyConstraint(baseColumnNames: "COLUMNS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK_ayotdydghp7ygfkqs6iuhn0u8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-145") {
		addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK_mgkc77as3kik3tdchonnwjecf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-146") {
		addForeignKeyConstraint(baseColumnNames: "ROW_COLS_RF_INFO_LIST_ID", baseTableName: "CLL_TEMPLT", constraintName: "FK_eiymtbl5n8rwy1vw3j72flgl7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-147") {
		addForeignKeyConstraint(baseColumnNames: "commented_by_id", baseTableName: "COMMENTS", constraintName: "FK_5qas2yte7ho39qilxgyk9v74c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-148") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "COMMENTS", constraintName: "FK_dqk7smk358xns8pnusrcrhs10", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "COMMENTS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-149") {
		addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "CONFIGURATION_GROUPS", constraintName: "FK_j3bki0nr7eufxv4ntq5n7bqmr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-150") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "CONFIGURATION_GROUPS", constraintName: "FK_gihgxj62vjwdeafmbo66ofms8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-151") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "DELIVERIES_SHARED_WITHS", constraintName: "FK_b5kgs7rd3w9d2gijp8cpe3rf6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-152") {
		addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_COL_MEAS_MEASURES", constraintName: "FK_mau0jlcmgs4b1t88pupmrp5xc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_MEASURE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-153") {
		addForeignKeyConstraint(baseColumnNames: "COLUMNS_RFI_LIST_ID", baseTableName: "DTAB_COLUMN_MEASURE", constraintName: "FK_gdd47anilyc472wtlgrm8tptf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-154") {
		addForeignKeyConstraint(baseColumnNames: "ROWS_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FK_avt8dahbs30tolbtfxhdci6sw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-155") {
		addForeignKeyConstraint(baseColumnNames: "COLUMN_MEASURE_ID", baseTableName: "DTAB_TEMPLTS_COL_MEAS", constraintName: "FK_6j0vj30pd28vlwja3s82ywdf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DTAB_COLUMN_MEASURE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-156") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_DELIVERIES_SHARED_WITHS", constraintName: "FK_7r64stirj17b6ucr8k1oyxx9o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-157") {
		addForeignKeyConstraint(baseColumnNames: "EX_DELIVERY_ID", baseTableName: "EX_RCONFIG", constraintName: "FK_chbehobquhn6scec0tqk3rg0w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_DELIVERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-158") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "EX_RCONFIG", constraintName: "FK_i773m62enehtq8ctjek12lpgr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-159") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_RCONFIGS_TAGS", constraintName: "FK_eiybjrvwdxcyr9acq7uxmrlhb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-160") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_STATUS", constraintName: "FK_n23l2ct6mpvkyextk1jddibs1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-161") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_STATUSES_SHARED_WITHS", constraintName: "FK_snvbpier2deh2fhjhcnhycplo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-162") {
		addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK_ebrincpyuy5xq44g9hs6c598l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-163") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_QUERY_VALUES", constraintName: "FK_otrslpvpvw4fvlrwkfn0amp6s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-164") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK_91bbav1aa2hrdwk34aky7ntjc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-165") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_VALUE_ID", baseTableName: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", constraintName: "FK_hdhr9a3agy09vxkv4dkghtr9o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-166") {
		addForeignKeyConstraint(baseColumnNames: "EX_DATE_RANGE_INFO_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_j1vhe6d21a97imbej3lj8ipei", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_DATE_RANGE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-167") {
		addForeignKeyConstraint(baseColumnNames: "EX_QUERY_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_dnbkas9q33iktvq5x890oabi2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-168") {
		addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_efl187h4pxj2k7blook970lf2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-169") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_p9j1aldxwmqkrnvofs2k0qnd6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-170") {
		addForeignKeyConstraint(baseColumnNames: "log_id", baseTableName: "IMPORT_DETAIL", constraintName: "FK_byrgjc0mhmq9pv6ag3f9432gs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "IMPORT_LOG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-171") {
		addForeignKeyConstraint(baseColumnNames: "NONCASE_SQL_TEMPLT_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_80w9mgrrqg7bbi15p7xkhxmes", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "NONCASE_SQL_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-172") {
		addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "NONCASE_SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_cojhmpxfp6cbnnfu9k81aygt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-173") {
		addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "NOTIFICATION", constraintName: "FK_elahxcjybvosq3scydf1ns948", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-174") {
		addForeignKeyConstraint(baseColumnNames: "PREFERENCE_ID", baseTableName: "PVUSER", constraintName: "FK_15ky9pn1mkt607gq2r8pcc4kl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PREFERENCE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-175") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERS_ROLES", constraintName: "FK_1kcl7nv9wqah9py58jjher5l3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ROLE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-176") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERS_ROLES", constraintName: "FK_oyjgh7fr570a0ygemtdj6msq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-177") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_EXP_VALUE_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FK_flu98xfixpr283ssi5egf7tse", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_EXP_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-178") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_ID", baseTableName: "QUERIES_QRS_EXP_VALUES", constraintName: "FK_dtgl5ygpmt49blrr8c3ytplu6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-179") {
		addForeignKeyConstraint(baseColumnNames: "REPORT_FIELD_ID", baseTableName: "QUERY_EXP_VALUE", constraintName: "FK_3xbqnfprt48u7uxl7x7cdkre8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-180") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_SET_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FK_mndinf4qm46cjgsnnbbu6ey89", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_SET", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-181") {
		addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FK_m58uate805vdbmtk7uqdsg4n5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-182") {
		addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "QUERY_VALUE", constraintName: "FK_o7vesodeulidahrra0yogjmbu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-183") {
		addForeignKeyConstraint(baseColumnNames: "DELIVERY_ID", baseTableName: "RCONFIG", constraintName: "FK_2qjsmqa01yvy40b02kd4yaqak", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DELIVERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-184") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "RCONFIG", constraintName: "FK_lf60qnytw72tcmf3n6rt9ek2g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-185") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "RCONFIG", constraintName: "FK_8857pyncipqerh6fe9doj1dwb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-186") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RCONFIGS_TAGS", constraintName: "FK_4r07c0u4qnpc2oaoa9ejdprhf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-187") {
		addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FK_mj9ttjm9d8vdnb7qmwpqgfq9m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-188") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_ERROR", constraintName: "FK_2fjiphm6fb8q89lc1139jcip5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-189") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RPT_ERROR", constraintName: "FK_svut1es5umn8c2c1s3j4t0y4g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-190") {
		addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_ID", baseTableName: "RPT_ERROR", constraintName: "FK_t413dtbnahl64eb3d37hi8v9m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-191") {
		addForeignKeyConstraint(baseColumnNames: "ARGUS_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FK_q9804pjr1xkny0j7k17qk88kp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "ARGUS_COLUMN_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-192") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GROUP_ID", baseTableName: "RPT_FIELD", constraintName: "FK_qva4bnq327x81hswik0xyc0x0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-193") {
		addForeignKeyConstraint(baseColumnNames: "RF_INFO_LIST_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK_nyw2ppms5dfmvjc78yu4wc8a2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-194") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "RPT_FIELD_INFO", constraintName: "FK_tfyjr0f7xthasp4ulu74rl8vb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-195") {
		addForeignKeyConstraint(baseColumnNames: "EX_TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK_b1iuja8ciarbru6ivucu68gmt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-196") {
		addForeignKeyConstraint(baseColumnNames: "RPT_RESULT_DATA_ID", baseTableName: "RPT_RESULT", constraintName: "FK_3v6wx4paah8raulblaacq0v6m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_RESULT_DATA", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-197") {
		addForeignKeyConstraint(baseColumnNames: "SCHEDULED_PVUSER_ID", baseTableName: "RPT_RESULT", constraintName: "FK_ocyw1xhhtuh2wy8womhmcne9n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-198") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "RPT_RESULT", constraintName: "FK_f9fgxo0mr8pr4oir07uklcndy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-199") {
		addForeignKeyConstraint(baseColumnNames: "CATEGORY_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK_2l4hoa5deg2k3mbxj3pf3r2aa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "CATEGORY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-200") {
		addForeignKeyConstraint(baseColumnNames: "PV_USER_ID", baseTableName: "RPT_TEMPLT", constraintName: "FK_8u2ywn411rvrxuiqwfs1bhkeh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-201") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "RPT_TEMPLTS_TAGS", constraintName: "FK_5turyk8hvy1pbarj4qj8usmga", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-202") {
		addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "SHARED_WITH", constraintName: "FK_thham91a8sjhl9wreg4880l8l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-203") {
		addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "SHARED_WITH", constraintName: "FK_70lu15yl8qdmw1rk9o4b06opa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-204") {
		addForeignKeyConstraint(baseColumnNames: "SQL_QUERY_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK_5b3l3airrb9onxra5s7cu8sdl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-205") {
		addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_QRS_SQL_VALUES", constraintName: "FK_f3iqsrdl52ram1chmtyv67w7a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-206") {
		addForeignKeyConstraint(baseColumnNames: "SQL_TEMPLT_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_k9qluss7nbvhijrn92tma73g4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-207") {
		addForeignKeyConstraint(baseColumnNames: "SQL_VALUE_ID", baseTableName: "SQL_TEMPLTS_SQL_VALUES", constraintName: "FK_em42mjqwovxihsoi5rota6i3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SQL_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-208") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "SUPER_QRS_TAGS", constraintName: "FK_e92igfb7ydcch5631di91vq0m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-209") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "SUPER_QUERY", constraintName: "FK_mej236u7r2xy2aqatsnekqt9j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-210") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FK_d9xgir92icqr90gehho1xar58", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-211") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_QUERY_VALUES", constraintName: "FK_c3sp0whrnp78g7q9cro1pr61c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-212") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_QUERY_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK_9tqpvcaqiw8jwyfy1gonskor0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-213") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLT_VALUE_ID", baseTableName: "TEMPLT_QRS_TEMPLT_VALUES", constraintName: "FK_ibsa9mgmrutxlkylbsasjxvhl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "TEMPLT_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-214") {
		addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_jmpfwg11woh9ev84tp18vbmg3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DATE_RANGE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-215") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_j0afuhl5yv1ia6dm9fm54ssxk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-216") {
		addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_d55bqwarp4cgr82x9u7p08mhb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-217") {
		addForeignKeyConstraint(baseColumnNames: "SUPER_QUERY_ID", baseTableName: "TEMPLT_QUERY", constraintName: "FK_nm9txtr8f9hffpldsnndyex4a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-218") {
		addForeignKeyConstraint(baseColumnNames: "RPT_TEMPLT_ID", baseTableName: "TEMPLT_VALUE", constraintName: "FK_n66fhiy57kennxsjctg3c6xny", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-219") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "user_group_s", constraintName: "FK_mkbhqavrsnswsncj6o8hlrvkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-220") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "USER_GROUPS", constraintName: "FK_clm2ud7rf2jlyi3j06k4x4u3y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-221") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "USER_GROUPS", constraintName: "FK_cp9jvnih3k4j5vil4fqh5gy2b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-222") {
		addForeignKeyConstraint(baseColumnNames: "PARAM_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK_9beyqhgld9w1n00ju71972fvf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PARAM", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-223") {
		addForeignKeyConstraint(baseColumnNames: "VALUE_ID", baseTableName: "VALUES_PARAMS", constraintName: "FK_5dtnylqi39k3kumaeg1bhfdfr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-224") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FK_4l2cw6klswchn71ep9d3lqpyo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-225") {
		addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WkFL_RUL_DISPOSITIONS", constraintName: "FK_j4ei009tfwkarfna5xwv68jnc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-226") {
		addForeignKeyConstraint(baseColumnNames: "income_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK_2q71fab410moahaqg08mdl2uf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-227") {
		addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "WORK_FLOW_RULES", constraintName: "FK_rn5skykcehqmmvn58t0h6souq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-228") {
		addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "work_flow_rules_actions", constraintName: "FK_1ir9yaobh6df8p6i32vdxxdra", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-229") {
		addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "work_flows_work_flow_rules", constraintName: "FK_arw0gqe0hcv7tnonjka4ky5wa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-230") {
		addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FK_nq20htbwq79dgemb8ty2fwulq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "GROUPS", referencesUniqueColumn: "false")
	}

	changeSet(author: "leigao (generated)", id: "1456255797755-231") {
		addForeignKeyConstraint(baseColumnNames: "workflow_rule_id", baseTableName: "WORKFLOWRULES_GROUPS", constraintName: "FK_bpga16kermje1m1q08le0a0up", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "WORK_FLOW_RULES", referencesUniqueColumn: "false")
	}

}
