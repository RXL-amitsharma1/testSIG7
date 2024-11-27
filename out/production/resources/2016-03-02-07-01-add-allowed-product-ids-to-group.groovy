databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1456931019351-1") {
		addColumn(tableName: "GROUPS") {
			column(name: "allowed_product_ids", type: "varchar2(4000 char)")
		}
	}
}
