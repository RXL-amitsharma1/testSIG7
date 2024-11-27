databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1481637455894-1") {
		addColumn(tableName: "CASE_HISTORY") {
			column(name: "change", type: "VARCHAR(255 )")
		}
	}

}
