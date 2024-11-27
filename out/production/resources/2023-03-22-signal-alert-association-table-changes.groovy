databaseChangeLog = {

    changeSet(author: "Shivam Vashist (generated)", id: "1677128577357-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_AGG_ALERTS', columnName: 'DATE_CREATED')
            }
        }
        addColumn(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "DATE_CREATED", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Shivam Vashist (generated)", id: "1677128577357-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_AGG_ALERTS', columnName: 'AUTO_ROUTED')
            }
        }
        addColumn(tableName: "VALIDATED_AGG_ALERTS") {
            column(name: "AUTO_ROUTED", type: "NUMBER(1,0)", defaultValue: "0") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shivam Vashist (generated)", id: "1677128577357-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SINGLE_ALERTS', columnName: 'DATE_CREATED')
            }
        }
        addColumn(tableName: "VALIDATED_SINGLE_ALERTS") {
            column(name: "DATE_CREATED", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Shivam Vashist (generated)", id: "1677128577357-04") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_EVDAS_ALERTS', columnName: 'DATE_CREATED')
            }
        }
        addColumn(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "DATE_CREATED", type: "TIMESTAMP") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Shivam Vashist (generated)", id: "1677128577357-05") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_EVDAS_ALERTS', columnName: 'AUTO_ROUTED')
            }
        }
        addColumn(tableName: "VALIDATED_EVDAS_ALERTS") {
            column(name: "AUTO_ROUTED", type: "NUMBER(1,0)", defaultValue: "0") {
                constraints(nullable: "true")
            }
        }
    }

}