databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1457747468553-1") {
		modifyDataType(columnName: "CONFIG_ID", newDataType: "number(19,0)", tableName: "ACTIONS")
	}

	changeSet(author: "leigao (generated)", id: "1457747468553-2") {
		addNotNullConstraint(columnDataType: "number(19,0)", columnName: "CONFIG_ID", tableName: "ACTIONS")
	}

	changeSet(author: "leigao (generated)", id: "1457747468553-3") {
		modifyDataType(columnName: "TYPE_ID", newDataType: "number(19,0)", tableName: "ACTIONS")
	}

	changeSet(author: "leigao (generated)", id: "1457747468553-4") {
		addNotNullConstraint(columnDataType: "number(19,0)", columnName: "TYPE_ID", tableName: "ACTIONS")
	}

	changeSet(author: "leigao (generated)", id: "1457747468553-5") {
		addPrimaryKey(columnNames: "alert_id, group_id", tableName: "ALERT_GROUPS")
	}
}
