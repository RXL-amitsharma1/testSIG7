databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1456275836722-1") {
		modifyDataType(columnName: "END_TIME", newDataType: "timestamp", tableName: "IMPORT_LOG")
	}

	changeSet(author: "leigao (generated)", id: "1456275836722-2") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "END_TIME", tableName: "IMPORT_LOG")
	}

	changeSet(author: "leigao (generated)", id: "1456275836722-3") {
		modifyDataType(columnName: "RESPONSE", newDataType: "VARCHAR(255 )", tableName: "IMPORT_LOG")
	}

	changeSet(author: "leigao (generated)", id: "1456275836722-4") {
		dropNotNullConstraint(columnDataType: "VARCHAR(255 )", columnName: "RESPONSE", tableName: "IMPORT_LOG")
	}
}
