databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1480332119536-1") {
		addColumn(tableName: "BUSINESS_CONFIGURATION") {
			column(name: "auto_state_configuration", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}
}
