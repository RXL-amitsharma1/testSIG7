databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1456275836722-1") {
		modifyDataType(columnName: "END_TIME", newDataType: "timestamp", tableName: "IMPORT_LOG")
	}

	changeSet(author: "leigao (generated)", id: "1456275836722-2") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "END_TIME", tableName: "IMPORT_LOG")
	}

	changeSet(author: "leigao (generated)", id: "1456275836722-3") {
		modifyDataType(columnName: "RESPONSE", newDataType: "varchar2(255 char)", tableName: "IMPORT_LOG")
	}

	changeSet(author: "leigao (generated)", id: "1456275836722-4") {
		dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "RESPONSE", tableName: "IMPORT_LOG")
	}
}
