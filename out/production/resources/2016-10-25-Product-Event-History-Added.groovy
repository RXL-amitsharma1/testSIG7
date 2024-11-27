databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1477569187692-1") {
		createTable(tableName: "product_event_history") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "product_eventPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "as_of_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "varchar2(255 char)")

			column(name: "date_created", type: "timestamp")

			column(name: "disposition_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "eb05", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "eb95", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "ebgm", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "execution_date", type: "timestamp")

			column(name: "is_latest", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "justification", type: "varchar2(255 char)")

			column(name: "last_updated", type: "timestamp")

			column(name: "modified_by", type: "varchar2(255 char)")

			column(name: "priority_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "product_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "prr_value", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "ror", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1477569187692-36") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "product_event_history", constraintName: "FK_4ak5hjs06eo5f3d0uxmaceuwv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1477569187692-37") {
		addForeignKeyConstraint(baseColumnNames: "disposition_id", baseTableName: "product_event_history", constraintName: "FK_shn9bkgudmn4vgad4v8njllxq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1477569187692-38") {
		addForeignKeyConstraint(baseColumnNames: "priority_id", baseTableName: "product_event_history", constraintName: "FK_8tbv3xaf0dxwxym80w1u4s94c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PRIORITY", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1477569187692-39") {
		addForeignKeyConstraint(baseColumnNames: "state_id", baseTableName: "product_event_history", constraintName: "FK_ctjimxg89uplweuic279em856", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1477569187692-40") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_group_s", constraintName: "FK_fwyqsp0akd0daoxy87v3ucj0o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
