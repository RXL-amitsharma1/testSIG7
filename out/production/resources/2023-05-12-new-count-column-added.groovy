databaseChangeLog = {
    changeSet(author: "Krishna Joshi(generated)", id: "1686900535159-004") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'NEW_COUNTS_JSON')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "NEW_COUNTS_JSON", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Krishna Joshi(generated)", id: "1686900535159-005") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'NEW_COUNTS_JSON')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "NEW_COUNTS_JSON", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Rahul Khichar(generated)", id: "1686900535159-006") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'NEW_COUNTS_JSON')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "NEW_COUNTS_JSON", type: "clob") {
                constraints(nullable: "true")
            }
        }
    }
    

    changeSet( author: "Krishna Joshi(generated)", id: "1692250766534-0034" ) {
        grailsChange {
            change {
                ctx.alertFieldService.updateMeddraFields()
            }
        }
    }

    changeSet( author: "Krishna Joshi (generated)", id: "1692250766534-0030" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'LITERATURE_ACTIVITY', columnName: 'PRODUCT_NAME' )
        }
        sql( "alter table LITERATURE_ACTIVITY add PRODUCT_NAME1 CLOB;" )
        sql( "UPDATE LITERATURE_ACTIVITY SET PRODUCT_NAME1=PRODUCT_NAME;" )
        sql( "ALTER TABLE LITERATURE_ACTIVITY DROP COLUMN PRODUCT_NAME;" )
        sql( "ALTER TABLE LITERATURE_ACTIVITY RENAME COLUMN PRODUCT_NAME1 TO PRODUCT_NAME;" )
    }

    changeSet( author: "Krishna Joshi (generated)", id: "1692250766534-0040" ) {
        preConditions( onFail: 'MARK_RAN' ) {
            columnExists( tableName: 'LITERATURE_ACTIVITY', columnName: 'EVENT_NAME' )
        }
        sql( "alter table LITERATURE_ACTIVITY add EVENT_NAME1 CLOB;" )
        sql( "UPDATE LITERATURE_ACTIVITY SET EVENT_NAME1=EVENT_NAME;" )
        sql( "ALTER TABLE LITERATURE_ACTIVITY DROP COLUMN EVENT_NAME;" )
        sql( "ALTER TABLE LITERATURE_ACTIVITY RENAME COLUMN EVENT_NAME1 TO EVENT_NAME;" )
    }

    changeSet( author: "Krishna Joshi(generated)", id: "1692250766534-002" ) {
        grailsChange {
            change {
                ctx.alertFieldService.updateNewEvEvdasLabel()
            }
        }
    }

    changeSet(author: "Krishna Joshi (generated)", id: "1704360006983-0042") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SIGNAL_REPORT', columnName: 'REPORT_NAME')
        }
        sql("alter table SIGNAL_REPORT modify REPORT_NAME VARCHAR2(1500 CHAR);")

    }


    changeSet(author: "Krishna Joshi (generated)", id: "1704360006983-0043") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'INBOX_LOG', columnName: 'MESSAGE')
        }
        sql("alter table INBOX_LOG modify MESSAGE VARCHAR2(1500 CHAR);")

    }

    changeSet(author: "Krishna Joshi (generated)", id: "1704360006983-0044") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'INBOX_LOG', columnName: 'MESSAGE_ARGS')
        }
        sql("alter table INBOX_LOG modify MESSAGE_ARGS VARCHAR2(1500 CHAR);")

    }

    changeSet( author: "Krishna Joshi(generated)", id: "1707117971400-003" ) {
        grailsChange {
            change {
                ctx.alertFieldService.updateSpecialCharacters()
            }
        }
    }
    //Removed migration ,handled from DB side
}
