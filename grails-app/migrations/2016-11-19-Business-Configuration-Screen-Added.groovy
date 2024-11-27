databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1479716980517-1") {
		createTable(tableName: "ALGO_CONFIGURATION") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ALGO_CONFIGURPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "algo_type", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "business_config_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "first_time_rule", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "justification", type: "VARCHAR(255 )")

			column(name: "max_threshold", type: "double precision")

			column(name: "min_threshold", type: "double precision")

			column(name: "target_disposition_id", type: "number(19,0)")

			column(name: "target_state_id", type: "number(19,0)")

			column(name: "config_type", type: "VARCHAR(20 )") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1479716980517-2") {
		createTable(tableName: "BUSINESS_CONFIGURATION") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "BUSINESS_CONFPK")
			}

			column(name: "version", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "analysis_level", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "calculate_ebgm_ci", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "calculate_prr_ci", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "calculate_ror_ci", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "created_by", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ebgm_custom_config", type: "VARCHAR(4000 )")

			column(name: "enable_ebgm", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "enable_ebgm_custom_config", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "enable_prr", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "enable_prr_custom_config", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "enable_ror", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "enable_ror_custom_config", type: "boolean") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "min_cases", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "modified_by", type: "VARCHAR(255 )") {
				constraints(nullable: "false")
			}

			column(name: "prr_custom_config", type: "VARCHAR(4000 )")

			column(name: "ror_custom_config", type: "VARCHAR(4000 )")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1479716980517-42") {
		addForeignKeyConstraint(baseColumnNames: "business_config_id", baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_fvqm6o3ass78s8dy23np04egn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "BUSINESS_CONFIGURATION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1479716980517-43") {
		addForeignKeyConstraint(baseColumnNames: "target_disposition_id", baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_96pf7wii4ac9d92b9yt1nos4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "DISPOSITION", referencesUniqueColumn: "false")
	}

	changeSet(author: "chetansharma (generated)", id: "1479716980517-44") {
		addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "ALGO_CONFIGURATION", constraintName: "FK_rrmw22x2rdc7xm3vqocb9tlfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PVS_STATE", referencesUniqueColumn: "false")
	}
}
