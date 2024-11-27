databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1457051062902-1") {
		addColumn(tableName: "GROUPS") {
			column(name: "allowed_products", type: "VARCHAR(4000 )")
		}
	}

	changeSet(author: "leigao (generated)", id: "1457051062902-2") {
		dropColumn(columnName: "ALLOWED_PRODUCT_IDS", tableName: "GROUPS")
	}
}
