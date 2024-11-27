databaseChangeLog = {
	changeSet(author: "chetansharma (generated)", id: "1480578535886-1") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE ARGUS_COLUMN_MASTER RENAME TO SOURCE_COLUMN_MASTER")
				sql.execute("ALTER TABLE ARGUS_TABLE_MASTER RENAME TO SOURCE_TABLE_MASTER")
				sql.execute("ALTER TABLE RPT_FIELD RENAME COLUMN ARGUS_COLUMN_MASTER_ID TO SOURCE_COLUMN_MASTER_ID")
			}
		}
	}
}
