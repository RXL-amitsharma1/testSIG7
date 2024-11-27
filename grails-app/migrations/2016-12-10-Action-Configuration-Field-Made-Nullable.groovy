databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1481309294878-2") {
		dropNotNullConstraint(columnDataType: "VARCHAR(255 )", columnName: "DESCRIPTION", tableName: "ACTION_CONFIGURATIONS")
	}
}
