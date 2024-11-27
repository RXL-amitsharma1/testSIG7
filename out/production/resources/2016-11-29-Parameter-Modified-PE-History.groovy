databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1480418752647-1") {
		addColumn(tableName: "product_event_history") {
			column(name: "ror_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1480418752647-47") {
		dropColumn(columnName: "ROR", tableName: "PRODUCT_EVENT_HISTORY")
	}


}
