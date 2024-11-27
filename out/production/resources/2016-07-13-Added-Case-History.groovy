databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1468412879628-2") {

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

			column(name: "case_version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "JUSTIFICATION", type: "varchar2(4000 char)")

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

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "follow_up_exists", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "is_latest", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "previous_assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "previous_disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "previous_priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "previous_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-15") {
		addForeignKeyConstraint(baseColumnNames: "current_assigned_to_id", baseTableName: "CASE_HISTORY", constraintName: "FK_q4mxb497604q60d3fs8624und", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-16") {
		addForeignKeyConstraint(baseColumnNames: "current_disposition_id", baseTableName: "CASE_HISTORY", constraintName: "FK_tn9d5k5b65pvqvej7fwi3ioj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-17") {
		addForeignKeyConstraint(baseColumnNames: "current_priority_id", baseTableName: "CASE_HISTORY", constraintName: "FK_5w7her578fpgwb3e0w5q2von6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-18") {
		addForeignKeyConstraint(baseColumnNames: "current_state_id", baseTableName: "CASE_HISTORY", constraintName: "FK_58y2dla00ia36jab2y0cxghy9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-19") {
		addForeignKeyConstraint(baseColumnNames: "previous_assigned_to_id", baseTableName: "CASE_HISTORY", constraintName: "FK_7f78agp65qp0srgrh4bw11qoc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-20") {
		addForeignKeyConstraint(baseColumnNames: "previous_disposition_id", baseTableName: "CASE_HISTORY", constraintName: "FK_mp02ohnceper3o1omfmcycs7i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-21") {
		addForeignKeyConstraint(baseColumnNames: "previous_priority_id", baseTableName: "CASE_HISTORY", constraintName: "FK_i6djk6ptvrlmx5qeb8ndjhxvu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1468412879628-22") {
		addForeignKeyConstraint(baseColumnNames: "previous_state_id", baseTableName: "CASE_HISTORY", constraintName: "FK_pg00ndeggq88vdd24sn1phqmq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

}
