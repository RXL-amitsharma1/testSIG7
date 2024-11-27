databaseChangeLog = {

	changeSet(author: "isha (generated)", id: "2019093015000-1") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "PVUSER", columnName: "LAST_LOGIN")
			}
		}
		addColumn(tableName: "PVUSER") {
			column(name: "LAST_LOGIN", type: "timestamp")
		}
	}

	changeSet(author: "isha (generated)", id: "2019093015000-2") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "PVUSER", columnName: "LAST_TO_LAST_LOGIN")
			}
		}
		addColumn(tableName: "PVUSER") {
			column(name: "LAST_TO_LAST_LOGIN", type: "timestamp")
		}
	}

}
