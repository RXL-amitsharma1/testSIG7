databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1476333690936-1") {
		addColumn(tableName: "AGG_ALERT") {
			column(name: "eb05", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1476333690936-2") {
		addColumn(tableName: "AGG_ALERT") {
			column(name: "eb95", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1476333690936-3") {
		addColumn(tableName: "AGG_ALERT") {
			column(name: "ebgm", type: "double precision") {
				constraints(nullable: "false")
			}
		}
	}

}
