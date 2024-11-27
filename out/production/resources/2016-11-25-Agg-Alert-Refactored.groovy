databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1480076405176-1") {
		addColumn(tableName: "AGG_ALERT") {
			column(name: "product_id", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1480076405176-2") {
		addColumn(tableName: "AGG_ALERT") {
			column(name: "pt_code", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1480076405176-3") {
		addColumn(tableName: "AGG_ALERT") {
			column(name: "ror_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1480076405176-50") {
		dropColumn(columnName: "ATTRIBUTES", tableName: "AGG_ALERT")
	}

	changeSet(author: "chetansharma (generated)", id: "1480076405176-51") {
		dropColumn(columnName: "HGLT", tableName: "AGG_ALERT")
	}

	changeSet(author: "chetansharma (generated)", id: "1480076405176-52") {
		dropColumn(columnName: "HLT", tableName: "AGG_ALERT")
	}

	changeSet(author: "chetansharma (generated)", id: "1480076405176-53") {
		dropColumn(columnName: "LLT", tableName: "AGG_ALERT")
	}

}
