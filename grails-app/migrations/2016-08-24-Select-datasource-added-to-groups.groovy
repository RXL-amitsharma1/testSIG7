databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1472207253394-1") {
		addColumn(tableName: "GROUPS") {
			column(name: "selected_datasource", type: "VARCHAR(255 )")
		}
	}
}