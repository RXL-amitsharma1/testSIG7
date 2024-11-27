databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1478777179756-1") {
		createTable(tableName: "ALERT_COMMENT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_COMMENTPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_type", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "case_number", type: "VARCHAR(255 )")

			column(name: "COMMENTS", type: "VARCHAR(4000 )") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "VARCHAR(255 )")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "product_family", type: "VARCHAR(255 )")

			column(name: "product_name", type: "VARCHAR(255 )")
		}
	}

}
