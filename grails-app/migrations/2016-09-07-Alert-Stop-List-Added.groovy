databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1473252615297-1") {
		createTable(tableName: "alert_stop_list") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "alert_stop_liPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "activated", type: "boolean")

			column(name: "created_by", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "date_deactivated", type: "timestamp")

			column(name: "EVENT_SELECTION", type: "clob")

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "VARCHAR(255 )")

			column(name: "PRODUCT_SELECTION", type: "clob")
		}
	}
}