databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1476789691278-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "is_auto_trigger", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1476789691278-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "IS_AUTO_TRIGGER", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}
}
