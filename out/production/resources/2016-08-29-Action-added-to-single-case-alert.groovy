databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1472472998053-1") {
		createTable(tableName: "single_case_alert_actions") {
			column(name: "single_case_alert_actions_id", type: "number(19,0)")
			column(name: "action_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1472472998053-21") {
		addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "single_case_alert_actions", constraintName: "FK_biyy409ikrbstn7r6f7ko8k2o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIONS", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1472472998053-22") {
		addForeignKeyConstraint(baseColumnNames: "single_case_alert_actions_id", baseTableName: "single_case_alert_actions", constraintName: "FK_jnhbwxirwveu68vlc2jw5pb6v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT", referencesUniqueColumn: "false")
	}
}
