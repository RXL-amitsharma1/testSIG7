databaseChangeLog = {

	changeSet(author: "ujjwal (generated)", id: "1613730275341-01") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'vaers_date_range')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "vaers_date_range", type: "VARCHAR2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275341-02") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ALERT', columnName: 'vaers_columns')
			}
		}
		addColumn(tableName: "AGG_ALERT") {
			column(name: "vaers_columns", type: "varchar2(4000 CHAR)")
		}
	}

    changeSet(author: "ujjwal (generated)", id: "1613730275341-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'VAERS_COLUMNS')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "VAERS_COLUMNS", type: "varchar2(4000 CHAR)")
        }
    }
}