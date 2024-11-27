databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1478777179756-1") {
		createTable(tableName: "ALERT_COMMENT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALERT_COMMENTPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "alert_type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "case_number", type: "varchar2(255 char)")

			column(name: "COMMENTS", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "product_family", type: "varchar2(255 char)")

			column(name: "product_name", type: "varchar2(255 char)")
		}
	}

}
