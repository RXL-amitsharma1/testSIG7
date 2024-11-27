databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1465542654185-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1465542654185-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1465542654185-8") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "EX_RCONFIG", constraintName: "FK_d8x6wwjt63b2x38txdx729mn8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1465542654185-9") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "RCONFIG", constraintName: "FK_h8sk39efsjooi9hu7q0c9ecjx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
