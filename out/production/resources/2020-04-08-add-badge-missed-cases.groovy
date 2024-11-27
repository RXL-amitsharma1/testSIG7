databaseChangeLog = {
    changeSet(author: "ankit (generated)", id: "1588305011950-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'BADGE')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "BADGE", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1588305011950-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'BADGE')
            }
        }
        addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
            column(name: "BADGE", type: "varchar2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ankit (generated)", id: "1588305011950-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'MISSED_CASES')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "MISSED_CASES", type: "number(1, 0)", defaultValue: "0")
        }
    }

    changeSet(author: "ankit (generated)", id: "1588305011950-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'MISSED_CASES')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "MISSED_CASES", type: "number(1, 0)", defaultValue: "0")
        }
    }
}