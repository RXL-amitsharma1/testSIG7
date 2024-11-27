databaseChangeLog = {
    changeSet(author: "Rishabh (generated)", id: "202407180614:001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_MASTER_TEMPLATE_ALERT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_MASTER_TEMPLATE_ALERT", type: "BOOLEAN"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202407180614:002") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'MASTER_TEMPLATE_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "MASTER_TEMPLATE_ID", type: "NUMBER(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202407180614:003") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'PRD_HIERARCHY_MASTER_TMPLT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "PRD_HIERARCHY_MASTER_TMPLT", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202407180614:004") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'EVENT_HIERARCHY_MASTER_TMPLT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "EVENT_HIERARCHY_MASTER_TMPLT", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:001") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'UNIQUE_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "UNIQUE_ID", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:002") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'UNIQUE_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "UNIQUE_ID", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:003") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ALERT_GROUP')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "ALERT_GROUP", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:004") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALERT_GROUP')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALERT_GROUP", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:005") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_MASTER_TEMPLATE_ALERT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_MASTER_TEMPLATE_ALERT", type: "BOOLEAN"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:006") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'EX_MASTER_TEMPLATE_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EX_MASTER_TEMPLATE_ID", type: "NUMBER(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:007") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'PRD_HIERARCHY_MASTER_TMPLT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "PRD_HIERARCHY_MASTER_TMPLT", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rishabh (generated)", id: "202408121151:008") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'EVENT_HIERARCHY_MASTER_TMPLT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EVENT_HIERARCHY_MASTER_TMPLT", type: "VARCHAR(8000)") {
                constraints(nullable: "true")
            }
        }
    }

}