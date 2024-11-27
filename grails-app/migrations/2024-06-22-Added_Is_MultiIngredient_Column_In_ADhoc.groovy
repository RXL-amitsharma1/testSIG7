databaseChangeLog = {
    changeSet(author: "Hemlata (generated)", id: "1721628451363-0001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ALERTS', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "ALERTS") {
            column(name: "IS_MULTI_INGREDIENT", type: "BOOLEAN", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul (generated)", id: "1721969809-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ALERT_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "SINGLE_ALERT_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul (generated)", id: "1721969809-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_SCA_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "ARCHIVED_SCA_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul (generated)", id: "1721969809-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "AGG_ALERT_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul (generated)", id: "1721969809-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_ACA_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "ARCHIVED_ACA_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul (generated)", id: "1721969809-05") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'LIT_ALERT_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "LIT_ALERT_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul (generated)", id: "1721969809-06") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_LIT_ALERT_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "ARCHIVED_LIT_ALERT_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Rahul (generated)", id: "1721969809-07") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EVDAS_ALERT_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "EVDAS_ALERT_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul (generated)", id: "1721969809-08") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_EVDAS_ALERT_ACTIONS', columnName: 'IS_RETAINED')
            }
        }
        addColumn(tableName: "ARCHIVED_EVDAS_ALERT_ACTIONS") {
            column(name: "IS_RETAINED", type: "BOOLEAN", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
}