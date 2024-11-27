
databaseChangeLog = {
    changeSet(author: "rahul (generated)", id: "1684911925-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_lci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_lci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-05") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_lci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_lci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-06") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_lci_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_lci_sub_group", type: "varchar2(8000 char)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1684911925-07") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_uci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_uci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-08") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_uci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_uci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-09") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_uci_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_uci_sub_group", type: "varchar2(8000 char)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1684911925-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'prr_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "prr_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'prr_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "prr_sub_group", type: "varchar2(8000 char)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1684911925-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'prr_lci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_lci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_lci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "prr_lci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'prr_lci_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "prr_lci_sub_group", type: "varchar2(8000 char)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1684911925-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'prr_uci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "prr_uci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_uci_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "prr_uci_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'prr_uci_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "prr_uci_sub_group", type: "varchar2(8000 char)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1684911925-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ebgm_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ebgm_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ebgm_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ebgm_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ebgm_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ebgm_sub_group", type: "varchar2(8000 char)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1684911925-22") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb05_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb05_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-23") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb05_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb05_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-24") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'eb05_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "eb05_sub_group", type: "varchar2(8000 char)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1684911925-25") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'eb95_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "eb95_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-26") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb95_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "eb95_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-27") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'eb95_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "eb95_sub_group", type: "varchar2(8000 char)")
        }
    }


    changeSet(author: "rahul (generated)", id: "1684911925-28") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'chi_square_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "chi_square_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-29") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'chi_square_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "chi_square_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-30") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'chi_square_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "chi_square_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-51") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_rel_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-52") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_rel_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-53") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_rel_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-54") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_lci_rel_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_lci_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-55") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_lci_rel_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_lci_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-56") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_lci_rel_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_lci_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-57") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'ror_uci_rel_sub_group')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "ror_uci_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-58") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_uci_rel_sub_group')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "ror_uci_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1684911925-59") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'ror_uci_rel_sub_group')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "ror_uci_rel_sub_group", type: "varchar2(8000 char)")
        }
    }
}
