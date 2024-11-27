databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1463396688844-2") {
		addColumn(tableName: "ARGUS_COLUMN_MASTER") {
			column(name: "IS_DELETED", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-3") {
		addColumn(tableName: "ARGUS_COLUMN_MASTER") {
			column(name: "MIN_COLUMNS", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-4") {
		addColumn(tableName: "ARGUS_TABLE_MASTER") {
			column(name: "HAS_ENTERPRISE_ID", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-5") {
		addColumn(tableName: "ARGUS_TABLE_MASTER") {
			column(name: "IS_DELETED", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-6") {
		addColumn(tableName: "CASE_COLUMN_JOIN_MAPPING") {
			column(name: "IS_DELETED", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-7") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "DIC_LEVEL", type: "number(10,0)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-8") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "DIC_TYPE", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-9") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "IS_DELETED", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-10") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "ISAUTOCOMPLETE", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-11") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "LMSQL", type: "clob")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-12") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "RPT_FIELD_GRPNAME", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-13") {
		addColumn(tableName: "RPT_FIELD_GROUP") {
			column(name: "IS_DELETED", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-14") {
		grailsChange {
			change {
				sql.execute("UPDATE rpt_field a  SET RPT_FIELD_GRPNAME = (SELECT NAME FROM rpt_field_group b WHERE a.rpt_field_group_id = b.ID)")
				confirm "Successfully set default value for rpt_field_group_name."
			}
		}
		addNotNullConstraint(tableName: "RPT_FIELD", columnName: "RPT_FIELD_GRPNAME", columnDataType: "varchar2(255 char)")
	}


	changeSet(author: "chetansharma (generated)", id: "1463396688844-19") {
		dropForeignKeyConstraint(baseTableName: "RPT_FIELD", constraintName: "FK_QVA4BNQ327X81HSWIK0XYC0X0")
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-18") {
		dropPrimaryKey(constraintName: "RPT_FIELD_GROPK", tableName: "RPT_FIELD_GROUP")
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-24") {
		dropColumn(columnName: "ID", tableName: "RPT_FIELD_GROUP")
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-16") {
		addPrimaryKey(columnNames: "NAME", constraintName: "RPT_FIELD_GROPK", tableName: "RPT_FIELD_GROUP")
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-23") {
		dropColumn(columnName: "RPT_FIELD_GROUP_ID", tableName: "RPT_FIELD")
	}

	changeSet(author: "chetansharma (generated)", id: "1463396688844-20") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_GRPNAME", baseTableName: "RPT_FIELD", constraintName: "FK_9qpw6t9tl4rdwmnsxj83vfkix", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "NAME", referencedTableName: "RPT_FIELD_GROUP", referencesUniqueColumn: "false")
	}

}
