import com.rxlogix.signal.ValidatedSignal
import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "uddesh (generated)", id: "1592527212865-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'a_value')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "a_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'b_value')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "b_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'c_value')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "c_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'd_value')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "d_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'e_value')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "e_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'rr_value')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "rr_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'a_value')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "a_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'b_value')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "b_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'c_value')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "c_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'd_value')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "d_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'e_value')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "e_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'rr_value')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "rr_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'a_value')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "a_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'b_value')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "b_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-15") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'c_value')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "c_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'd_value')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "d_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'e_value')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "e_value", type: "double precision")
        }
    }

    changeSet(author: "uddesh (generated)", id: "1592527212865-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'rr_value')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "rr_value", type: "double precision")
        }
    }

    changeSet(author: "yogesh (generated)", id: "1658126668799-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUERY_EXP_VALUE', columnName: 'operator_value')
            }
        }
        addColumn(tableName: "QUERY_EXP_VALUE") {
            column(name: "operator_value", type: "VARCHAR(255 )"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rahul (generated)", id: "1658126668799-199") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'apply_alert_stop_list')
        }
        sql('''update RCONFIG set apply_alert_stop_list = FALSE WHERE type = 'Aggregate Case Alert' ''')
    }
    changeSet(author: "rahul (generated)", id: "1658126668799-200") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'apply_alert_stop_list')
        }
        sql('''update EX_RCONFIG set apply_alert_stop_list = FALSE WHERE type = 'Aggregate Case Alert' ''')
    }
}