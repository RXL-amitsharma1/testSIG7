databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1473515163167-1") {
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

			column(name: "attributes", type: "clob") {
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

			column(name: "exec_configuration_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "flagged", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "hglt", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "hlt", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "llt", type: "varchar2(255 char)") {
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

			column(name: "product_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "prr_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "pt", type: "varchar2(255 char)") {
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

	changeSet(author: "chetansharma (generated)", id: "1473515163167-2") {
		createTable(tableName: "AGG_ALERT_ACTIONS") {
			column(name: "AGG_ALERT_ACTIONS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
			column(name: "ACTION_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-22") {
		addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FK_r30ffdp2dmycptbusa00x6tar", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-23") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "AGG_ALERT", constraintName: "FK_n7dll8vhg3ykpx0jn0t471rhk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-24") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "AGG_ALERT", constraintName: "FK_lyybra8sk7nvjxeq8ptsnww0v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-25") {
		addForeignKeyConstraint(baseColumnNames: "exec_configuration_id", baseTableName: "AGG_ALERT", constraintName: "FK_m7e2g4974ejxe7mhua226stks", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-26") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "AGG_ALERT", constraintName: "FK_jxrfnk4vd3btciga07rw4ljrd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-27") {
		addForeignKeyConstraint(baseColumnNames: "workflow_state_id", baseTableName: "AGG_ALERT", constraintName: "FK_pnk180oe4gbga5hpemhp4qtxt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-28") {
		addForeignKeyConstraint(baseColumnNames: "ACTION_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK_6m33wcyuk8e9bhhk4nhsqamf9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473515163167-29") {
		addForeignKeyConstraint(baseColumnNames: "AGG_ALERT_ACTIONS_ID", baseTableName: "AGG_ALERT_ACTIONS", constraintName: "FK_q9psmpjun6pwt73fa1ea87st0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT", referencesUniqueColumn: "false")
	}

}
