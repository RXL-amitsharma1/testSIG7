databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1479471639872-1") {
		addColumn(tableName: "CASE_HISTORY") {
			column(name: "follow_up_number", type: "number(10,0)")
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1479471639872-2") {
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "follow_up_exists", type: "number(1,0)")
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1479471639872-3") {
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "follow_up_number", type: "number(10,0)")
		}
	}

}
