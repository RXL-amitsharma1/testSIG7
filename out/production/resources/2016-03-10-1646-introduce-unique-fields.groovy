databaseChangeLog = {
	/*
	changeSet(author: "leigao (generated)", id: "1457724626090-1") {
		createIndex(indexName: "name_uniq_1457724574287", tableName: "ALERTS", unique: "true") {
			column(name: "name")
		}
	}
	*/

	changeSet(author: "leigao (generated)", id: "1457724626090-2") {
		dropColumn(columnName: "SHARED_WITH_GROUPS_IDX", tableName: "ALERT_GROUPS")
	}
}
