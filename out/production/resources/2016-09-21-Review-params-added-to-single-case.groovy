databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1474441855599-1") {
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "assigned_to_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1474441855599-2") {
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "disposition_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1474441855599-3") {
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "priority_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1474441855599-4") {
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "state_id", type: "number(19,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1474441855599-24") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_2egb0qm3c8gcwyx9v1sfrvf4u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1474441855599-25") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_jydu1rjydge3ge2r0nvcgwoh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1474441855599-26") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_aly7o1cjpgbl5q54imrdfdwp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1474441855599-27") {
		addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "SINGLE_CASE_ALERT", constraintName: "FK_agxibum7h5051ynumcgh78njr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}
}
