import com.rxlogix.ContextSettingPVSService

databaseChangeLog = {
    changeSet(author: "bhupender", id: "202408011434-01") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'rconfig', columnName: 'study_cases')
            }
        }
        addColumn(tableName: "rconfig") {
            column(name: "study_cases", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender", id: "202408011434-02") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'rconfig', columnName: 'ignore_study_type')
            }
        }
        addColumn(tableName: "rconfig") {
            column(name: "ignore_study_type", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender", id: "202408011434-03") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'rconfig', columnName: 'only_poi')
            }
        }
        addColumn(tableName: "rconfig") {
            column(name: "only_poi", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender", id: "202408011434-04") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ex_rconfig', columnName: 'study_cases')
            }
        }
        addColumn(tableName: "ex_rconfig") {
            column(name: "study_cases", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender", id: "202408011434-05") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ex_rconfig', columnName: 'ignore_study_type')
            }
        }
        addColumn(tableName: "ex_rconfig") {
            column(name: "ignore_study_type", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender", id: "202408011434-06") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ex_rconfig', columnName: 'only_poi')
            }
        }
        addColumn(tableName: "ex_rconfig") {
            column(name: "only_poi", type: "boolean", defaultValue: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bhupender", id: "202408011434-07"){
        preConditions(onFail: 'MARK_RAN'){
            not {
                columnExists(tableName: 'ex_rconfig', columnName: 'study_drugs')
            }
        }
        addColumn(tableName: "ex_rconfig") {
            column(name: "study_drugs", type: "CLOB"){
                constraints(nullable: true)
            }
        }
    }
}