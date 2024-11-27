databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1479968820015-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "DRUG_TYPE", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1479968820015-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "DRUG_TYPE", type: "varchar2(255 char)")
		}
	}

}
