databaseChangeLog = {
    changeSet(author: "nitesh (generated)", id: "1608626578698-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'agg_alert', columnName: 'TREND_FLAG')
            }
        }
        addColumn(tableName: "agg_alert") {
            column(name: "TREND_FLAG", type: "varchar(255)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'agg_alert', columnName: 'PROD_N_PERIOD')
            }
        }
        addColumn(tableName: "agg_alert") {
            column(name: "PROD_N_PERIOD", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'agg_alert', columnName: 'PROD_N_CUMUL')
            }
        }
        addColumn(tableName: "agg_alert") {
            column(name: "PROD_N_CUMUL", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'agg_alert', columnName: 'FREQ_PERIOD')
            }
        }
        addColumn(tableName: "agg_alert") {
            column(name: "FREQ_PERIOD", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'agg_alert', columnName: 'FREQ_CUMUL')
            }
        }
        addColumn(tableName: "agg_alert") {
            column(name: "FREQ_CUMUL", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'agg_alert', columnName: 'SAVE_FREQ_PERIOD')
            }
        }
        addColumn(tableName: "agg_alert") {
            column(name: "SAVE_FREQ_PERIOD", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'agg_alert', columnName: 'SAVE_FREQ_CUMUL')
            }
        }
        addColumn(tableName: "agg_alert") {
            column(name: "SAVE_FREQ_CUMUL", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'TREND_FLAG')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "TREND_FLAG", type: "varchar(255)")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-9") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PROD_N_PERIOD')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PROD_N_PERIOD", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-10") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PROD_N_CUMUL')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PROD_N_CUMUL", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-11") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FREQ_PERIOD')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "FREQ_PERIOD", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-12") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'FREQ_CUMUL')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "FREQ_CUMUL", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-13") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'SAVE_FREQ_PERIOD')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "SAVE_FREQ_PERIOD", type: "NUMBER")
        }
    }
    changeSet(author: "nitesh (generated)", id: "1608626578698-14") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'SAVE_FREQ_CUMUL')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "SAVE_FREQ_CUMUL", type: "NUMBER")
        }
    }
}