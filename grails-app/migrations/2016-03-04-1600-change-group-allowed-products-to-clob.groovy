databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1457138666707-1") {
		addColumn(tableName: "GROUPS") {
			column(name: "allowed_prod", type: "clob")
		}
	}

	changeSet(author: "leigao (generated)", id: "1457138666707-2") {
		dropColumn(columnName: "ALLOWED_PRODUCTS", tableName: "GROUPS")
	}
}
