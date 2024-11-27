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
            column(name: "operator_value", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rahul (generated)", id: "1658126668799-199") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'RCONFIG', columnName: 'apply_alert_stop_list')
        }
        sql('''update RCONFIG set apply_alert_stop_list = 0 WHERE type = 'Aggregate Case Alert' ''')
    }
    changeSet(author: "rahul (generated)", id: "1658126668799-200") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'apply_alert_stop_list')
        }
        sql('''update EX_RCONFIG set apply_alert_stop_list = 0 WHERE type = 'Aggregate Case Alert' ''')
    }

    changeSet(author: "yogesh (generated)", id: "1664865102244-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SIGNAL_LINKED_SIGNALS')
        }
        grailsChange {
            change {
                Sql sql
                try {
                    List allSignals = ValidatedSignal.list()
                    def signalWithLinkedSignals = allSignals.findAll {
                        (it.linkedSignals != [] && it.linkedSignals != null)
                    }
                    sql = new Sql(ctx.getBean("dataSource"))
                    signalWithLinkedSignals.each { it1 ->
                        it1.linkedSignals.each { it2 ->
                            def linkSignalCount = sql.rows("""select count(*) from SIGNAL_LINKED_SIGNALS where VALIDATED_SIGNAL_ID =${it2.id}""")
                            def finalCount = linkSignalCount[0][0] == 0 ? 0 : linkSignalCount[0][0] + 1
                            sql.execute("""INSERT INTO SIGNAL_LINKED_SIGNALS(VALIDATED_SIGNAL_ID,LINKED_SIGNAL_ID,linked_signals_idx) VALUES(${it2.id},${it1.id},${finalCount})""")
                            finalCount = finalCount + 1
                        }
                    }
                } catch (Exception e) {
                    println("########## Some error occurred while saving value in SIGNAL_LINKED_SIGNAL TABLE #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }
}