databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1473670268454-1") {
		addColumn(tableName: "ACTIONS") {
			column(name: "agg_alert_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1473670268454-2") {
		addColumn(tableName: "ACTIONS") {
			column(name: "alert_type", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1473670268454-3") {
		addColumn(tableName: "ACTIONS") {
			column(name: "single_case_alert_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1473670268454-23") {
		addForeignKeyConstraint(baseColumnNames: "agg_alert_id", baseTableName: "ACTIONS", constraintName: "FK_rn3pfvh8hppokjl03hrj5qo8k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "AGG_ALERT", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1473670268454-24") {
		addForeignKeyConstraint(baseColumnNames: "single_case_alert_id", baseTableName: "ACTIONS", constraintName: "FK_nuo909hma39nddbsa7y5lbf0k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "SINGLE_CASE_ALERT", referencesUniqueColumn: "false")
	}
}
