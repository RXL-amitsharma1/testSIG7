databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1625821218679-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_ALL_PT')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_ALL_PT") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_ALL_PT", type: "VARCHAR2(15000)")

            column(name: "all_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625821218679-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_ALL_PT')
            }
        }
        createTable(tableName: "SINGLE_ALERT_ALL_PT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_ALL_PT", type: "VARCHAR2(15000)")

            column(name: "all_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625821218679-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_ALL_PT')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_ALL_PT") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_ALL_PT", type: "VARCHAR2(15000)")

            column(name: "all_pt_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625821218679-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'all_pt')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "all_pt", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625821218679-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'all_pt')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "all_pt", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625821218679-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'all_pt')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "all_pt", type: "clob")
        }
    }
}
