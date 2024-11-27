databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1465467220870-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1465467220870-2") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "limit_primary_path", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1465467220870-3") {
		addColumn(tableName: "RCONFIG") {
			column(name: "INCL_MEDICAL_CONFIRM_CASES", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1465467220870-4") {
		addColumn(tableName: "RCONFIG") {
			column(name: "limit_primary_path", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

}
