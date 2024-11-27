databaseChangeLog = {

	changeSet(author: "glennsilverman (generated)", id: "1458605626520-1") {
		addColumn(tableName: "PRIORITY") {
			column(name: "PRIORITY_ORDER", type: "number(10,0)")
		}
		addNotNullConstraint(tableName: "PRIORITY", columnName:"PRIORITY_ORDER", defaultNullValue: "5")

	}

}
