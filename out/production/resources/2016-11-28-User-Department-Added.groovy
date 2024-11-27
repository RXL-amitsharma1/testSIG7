databaseChangeLog = {

	changeSet(author: "SuhailJahangir (generated)", id: "1480337023711-1") {
		createTable(tableName: "pvuser_user_department") {
			column(name: "user_user_departments_id", type: "number(19,0)")
			column(name: "user_department_id", type: "number(19,0)")
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1480337023711-2") {
		createTable(tableName: "user_department") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_departmePK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "department_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1480337023711-45") {
		addForeignKeyConstraint(baseColumnNames: "user_department_id", baseTableName: "pvuser_user_department", constraintName: "FK_kk8q35v4d43xmp17vg11owi0r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user_department", referencesUniqueColumn: "false")
	}

	changeSet(author: "SuhailJahangir (generated)", id: "1480337023711-46") {
		addForeignKeyConstraint(baseColumnNames: "user_user_departments_id", baseTableName: "pvuser_user_department", constraintName: "FK_59qhkvgqv9c8f7ruavhbyqogx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
