databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1477403208559-44") {
		dropColumn(columnName: "FOLLOW_UP_EXISTS", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1477403208559-45") {
		dropColumn(columnName: "PREVIOUS_ASSIGNED_TO_ID", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1477403208559-46") {
		dropColumn(columnName: "PREVIOUS_DISPOSITION_ID", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1477403208559-47") {
		dropColumn(columnName: "PREVIOUS_PRIORITY_ID", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1477403208559-48") {
		dropColumn(columnName: "PREVIOUS_STATE_ID", tableName: "CASE_HISTORY")
	}

	changeSet(author: "chetansharma (generated)", id: "1477421005144-4") {
		addColumn(tableName: "CASE_HISTORY") {
			column(name: "product_family", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1477459717874-1") {
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "product_family", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}
}
