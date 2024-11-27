databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1474891511518-1") {
		createTable(tableName: "RCONFIG_DISPOSITION") {
			column(name: "CONFIGURATION_DISPOSITION_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
			column(name: "DISPOSITION_ID", type: "number(19,0)")
			column(name: "RCONFIG_DISPOSITION_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1474891511518-29") {
		addForeignKeyConstraint(baseColumnNames: "DISPOSITION_ID", baseTableName: "RCONFIG_DISPOSITION", constraintName: "FK_ajf3ju8powk325c5moogn2yqw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}
}
