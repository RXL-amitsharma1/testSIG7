databaseChangeLog = {
    changeSet(author: "Uddesh Teke (generated)", id: "1843982126497-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_AGG_ALERTS', columnName: 'IS_CARRY_FORWARD')
            }
        }
        addColumn(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "IS_CARRY_FORWARD", type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1843982126497-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SINGLE_ALERTS', columnName: 'IS_CARRY_FORWARD')
            }
        }
        addColumn(tableName: "VALIDATED_SINGLE_ALERTS") {
            column(name: "IS_CARRY_FORWARD", type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1843982126497-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_LITERATURE_ALERTS', columnName: 'IS_CARRY_FORWARD')
            }
        }
        addColumn(tableName: "VALIDATED_LITERATURE_ALERTS") {
            column(name: "IS_CARRY_FORWARD", type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Uddesh Teke (generated)", id: "1843982126497-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_EVDAS_ALERTS', columnName: 'IS_CARRY_FORWARD')
            }
        }
        addColumn(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "IS_CARRY_FORWARD", type: "NUMBER(1,0)") {
                constraints(nullable: "true")
            }
        }
    }
}