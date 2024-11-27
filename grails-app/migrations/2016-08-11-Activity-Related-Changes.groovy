databaseChangeLog = {

	changeSet(author: "Suhail (generated)", id: "1470892993934-1") {
		createTable(tableName: "ex_rconfig_activities") {
			column(name: "ex_config_activities_id", type: "number(19,0)")
			column(name: "activity_id", type: "number(19,0)")
		}
	}

	changeSet(author: "Suhail (generated)", id: "1470892993934-19") {
		addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "ex_rconfig_activities", constraintName: "FK_52oo30ac3mlwh5avdr25x5ui", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "ACTIVITIES", referencesUniqueColumn: "false")
	}

	changeSet(author: "Suhail (generated)", id: "1470892993934-20") {
		addForeignKeyConstraint(baseColumnNames: "ex_config_activities_id", baseTableName: "ex_rconfig_activities", constraintName: "FK_g9t8tg39ubmfcfr7rpn4l2o6d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

}
