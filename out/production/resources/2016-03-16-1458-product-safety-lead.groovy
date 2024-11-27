databaseChangeLog = {

	changeSet(author: "glennsilverman (generated)", id: "1458170265619-1") {
		createTable(tableName: "pvuser_safety_groups") {
			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "safety_group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1458170265619-2") {
		createTable(tableName: "safety_group") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "safety_groupPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "allowed_prod", type: "clob")

			column(name: "created_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1458170265619-3") {
		addPrimaryKey(columnNames: "user_id, safety_group_id", tableName: "pvuser_safety_groups")
	}

	changeSet(author: "glennsilverman (generated)", id: "1458170265619-6") {
		createIndex(indexName: "name_uniq_1458170234036", tableName: "safety_group", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1458170265619-4") {
		addForeignKeyConstraint(baseColumnNames: "safety_group_id", baseTableName: "pvuser_safety_groups", constraintName: "FK_2f62drogu4d59ix7lv555gvn6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "safety_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "glennsilverman (generated)", id: "1458170265619-5") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "pvuser_safety_groups", constraintName: "FK_4nbamxoaxjnvilrffxwxa3ge5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
