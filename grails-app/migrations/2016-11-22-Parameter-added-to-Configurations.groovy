databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1479758198199-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "repeat_execution", type: "boolean", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1479758198199-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "repeat_execution", type: "boolean", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

}