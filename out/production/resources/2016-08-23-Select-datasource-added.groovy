databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1472030776574-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "SELECTED_DATA_SOURCE", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1472030776574-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "SELECTED_DATA_SOURCE", type: "varchar2(255 char)")
		}
	}
}
