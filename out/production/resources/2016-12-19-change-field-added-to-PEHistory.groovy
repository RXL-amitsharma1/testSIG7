databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1482155774663-1") {
		addColumn(tableName: "product_event_history") {
			column(name: "change", type: "varchar2(255 char)")
		}
	}
}
