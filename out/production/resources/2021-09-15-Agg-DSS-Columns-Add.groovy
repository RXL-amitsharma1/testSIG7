databaseChangeLog = {

    changeSet(author: "sandeep (generated)", id: "1593475988790-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'DSS_COMMENTS')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "DSS_COMMENTS", type: "varchar2(4000 CHAR)")
        }
    }



    changeSet(author: "sandeep (generated)", id: "1593475988790-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'dss_confirmation')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "dss_confirmation", type: "varchar2(255 CHAR)")
        }
    }



    changeSet(author: "sandeep (generated)", id: "1593475988790-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'dss_score')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "dss_score", type: "clob")
        }
    }



    changeSet(author: "sandeep (generated)", id: "1593475988790-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'pec_imp_num_high')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "pec_imp_num_high", type: "double precision")
        }
    }



    changeSet(author: "sandeep (generated)", id: "1593475988790-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'pec_imp_num_low')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "pec_imp_num_low", type: "double precision")
        }
    }



    changeSet(author: "sandeep (generated)", id: "1593475988790-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'proposed_disposition')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "proposed_disposition", type: "varchar2(255 CHAR)")
        }
    }



    changeSet(author: "sandeep (generated)", id: "1593475988790-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'rationale')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "rationale", type: "varchar2(255 CHAR)")
        }
    }
}