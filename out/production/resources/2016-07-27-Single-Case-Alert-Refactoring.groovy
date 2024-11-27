databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1469689116665-1") {
		createTable(tableName: "SINGLE_CASE_ALERT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SINGLE_CASE_APK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_configuration_id", type: "number(19,0)")

			column(name: "attributes", type: "clob")

			column(name: "case_number", type: "varchar2(255 char)")

			column(name: "case_version", type: "number(19,0)")

			column(name: "created_by", type: "varchar2(255 char)")

			column(name: "date_created", type: "timestamp")

			column(name: "detected_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "due_date", type: "timestamp")

			column(name: "exec_config_id", type: "number(19,0)")

			column(name: "flagged", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "varchar2(255 char)")

			column(name: "review_date", type: "timestamp")

			column(name: "name", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1469689116665-17") {
		addForeignKeyConstraint(baseColumnNames: "alert_configuration_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_s1qdu47x5gen885jmrrbgeeka", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1469689116665-19") {
		addForeignKeyConstraint(baseColumnNames: "exec_config_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_59c03i0dium9bv3pnqvguiwke", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}
}
