databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1472030776574-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR(255 )")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1472030776574-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "SELECTED_DATA_SOURCE", type: "VARCHAR(255 )")
		}
	}
}
