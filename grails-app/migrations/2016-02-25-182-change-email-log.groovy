databaseChangeLog = {

	changeSet(author: "glennsilverman (generated)", id: "1456426027402-1") {
		addColumn(tableName: "EMAIL_LOG") {
			column(name: "message", type: "VARCHAR(4000 )") {
				constraints(nullable: "true")
			}
		}
	}
}
