databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1470203546754-6") {
		dropNotNullConstraint(columnDataType: "VARCHAR(255 )", columnName: "CREATED_BY", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1470203546754-7") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "DATE_CREATED", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1470203546754-8") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "LAST_UPDATED", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1470203546754-9") {
		dropNotNullConstraint(columnDataType: "VARCHAR(255 )", columnName: "MODIFIED_BY", tableName: "CASE_HISTORY")
	}

}
