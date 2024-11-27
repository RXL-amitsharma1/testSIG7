databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1478692244620-1") {
		addColumn(tableName: "ACTIVITIES") {
			column(name: "assigned_to_id", type: "number(19,0)")
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1478692244620-2") {
		addColumn(tableName: "ACTIVITIES") {
			column(name: "case_number", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1478692244620-3") {
		addColumn(tableName: "ACTIVITIES") {
			column(name: "event_name", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1478692244620-4") {
		addColumn(tableName: "ACTIVITIES") {
			column(name: "suspect_product", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1478692244620-43") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTIVITIES", constraintName: "FK_hhu7kkp2chey6kymidh20x5to", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
