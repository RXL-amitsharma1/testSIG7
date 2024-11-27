databaseChangeLog = {

    changeSet(author: "amrendra (generated)", id: "1625553580416-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_ALL_PAI')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_ALL_PAI") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_ALL_PAI", type: "VARCHAR2(15000)")

            column(name: "pai_all_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_PRIM_PAI')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_PRIM_PAI") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRIM_PAI", type: "VARCHAR2(15000)")

            column(name: "prim_susp_pai_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'AR_SIN_ALERT_PRIM_SUSP')
            }
        }
        createTable(tableName: "AR_SIN_ALERT_PRIM_SUSP") {
            column(name: "ARCHIVED_SCA_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRIM_SUSP", type: "VARCHAR2(15000)")

            column(name: "prim_susp_prod_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_ALL_PAI')
            }
        }
        createTable(tableName: "SINGLE_ALERT_ALL_PAI") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_ALL_PAI", type: "VARCHAR2(15000)")

            column(name: "pai_all_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_PRIM_PAI')
            }
        }
        createTable(tableName: "SINGLE_ALERT_PRIM_PAI") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRIM_PAI", type: "VARCHAR2(15000)")

            column(name: "prim_susp_pai_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_ALERT_PRIM_SUSP')
            }
        }
        createTable(tableName: "SINGLE_ALERT_PRIM_SUSP") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRIM_SUSP", type: "VARCHAR2(15000)")

            column(name: "prim_susp_prod_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_ALL_PAI')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_ALL_PAI") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_ALL_PAI", type: "VARCHAR2(15000)")

            column(name: "pai_all_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_PRIM_PAI')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_PRIM_PAI") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRIM_PAI", type: "VARCHAR2(15000)")

            column(name: "prim_susp_pai_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'SINGLE_DEMAND_PRIM_SUSP')
            }
        }
        createTable(tableName: "SINGLE_DEMAND_PRIM_SUSP") {
            column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SCA_PRIM_SUSP", type: "VARCHAR2(15000)")

            column(name: "prim_susp_prod_list_idx", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'pai_all')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "pai_all", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'pai_all')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "pai_all", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'pai_all')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "pai_all", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'prim_susp_pai')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "prim_susp_pai", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'prim_susp_pai')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "prim_susp_pai", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'prim_susp_pai')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "prim_susp_pai", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SINGLE_CASE_ALERT', columnName: 'prim_susp_prod')
            }
        }
        addColumn(tableName: "ARCHIVED_SINGLE_CASE_ALERT") {
            column(name: "prim_susp_prod", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'prim_susp_prod')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "prim_susp_prod", type: "clob")
        }
    }

    changeSet(author: "amrendra (generated)", id: "1625553580416-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'prim_susp_prod')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "prim_susp_prod", type: "clob")
        }
    }

}
