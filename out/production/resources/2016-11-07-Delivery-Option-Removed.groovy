databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1478518260340-50") {
		dropColumn(columnName: "EX_DELIVERY_ID", tableName: "EX_RCONFIG")
	}

	changeSet(author: "chetansharma (generated)", id: "1478518260340-51") {
		dropColumn(columnName: "DELIVERY_ID", tableName: "RCONFIG")
	}

}
