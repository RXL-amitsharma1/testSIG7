databaseChangeLog = {

    changeSet(author: "sandeep (generated)", id: "1692527239867-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'DSS_COMMENTS')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "DSS_COMMENTS", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "sandeep (generated)", id: "1692527239867-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'dss_confirmation')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "dss_confirmation", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "sandeep (generated)", id: "1692527239867-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'dss_score')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "dss_score", type: "clob")
        }
    }

    changeSet(author: "sandeep (generated)", id: "1692527239867-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'pec_imp_num_high')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "pec_imp_num_high", type: "double precision")
        }
    }

    changeSet(author: "sandeep (generated)", id: "1692527239867-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'pec_imp_num_low')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "pec_imp_num_low", type: "double precision")
        }
    }

    changeSet(author: "sandeep (generated)", id: "1693475988769-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'proposed_disposition')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "proposed_disposition", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "sandeep (generated)", id: "1693475988769-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'rationale')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "rationale", type: "varchar2(255 CHAR)")
        }
    }

    changeSet(author: "ujjwal (generated)", id: "1693475988769-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'is_safety')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "is_safety", type: "number(10, 0)", defaultValue: "1") {
                constraints(nullable: "true")
            }
        }
    }


}