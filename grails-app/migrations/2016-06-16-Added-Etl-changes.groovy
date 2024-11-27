databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1466052194114-1") {
		addColumn(tableName: "ETL_SCHEDULE") {
			column(name: "IS_INITIAL", type: "boolean", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

}
