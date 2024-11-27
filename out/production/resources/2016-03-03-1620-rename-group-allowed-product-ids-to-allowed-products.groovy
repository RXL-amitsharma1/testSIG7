databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1457051062902-1") {
		addColumn(tableName: "GROUPS") {
			column(name: "allowed_products", type: "varchar2(4000 char)")
		}
	}

	changeSet(author: "leigao (generated)", id: "1457051062902-2") {
		dropColumn(columnName: "ALLOWED_PRODUCT_IDS", tableName: "GROUPS")
	}
}
