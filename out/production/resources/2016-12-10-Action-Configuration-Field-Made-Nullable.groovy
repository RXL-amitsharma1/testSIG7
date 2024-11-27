databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1481309294878-2") {
		dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DESCRIPTION", tableName: "ACTION_CONFIGURATIONS")
	}
}
