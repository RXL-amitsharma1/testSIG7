import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "rishabh goswami(generated)", id: "16089917102260-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(*) from validated_signal where signal_status <> 'Date Closed' AND id in (select sig.validated_signal_id from signal_sig_status_history sig join signal_status_history his on (sig.sig_status_history_id = his.id) where his.signal_status = 'Date Closed')")
            }
        }
        sql("update validated_signal set signal_status = 'Date Closed' where id in (select sig.validated_signal_id from signal_sig_status_history sig join signal_status_history his on (sig.sig_status_history_id = his.id) where his.signal_status = 'Date Closed'); commit;")
    }
    changeSet(author: "krishan (generated)", id: "1646896501-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENTS')
        }
        sql("alter table EMERGING_ISSUE add EVENTS1 clob")
        sql("update EMERGING_ISSUE set EVENTS1=EVENTS")
        sql("alter table EMERGING_ISSUE drop column  EVENTS")
        sql("alter table EMERGING_ISSUE rename column EVENTS1 to EVENTS")
    }

    changeSet(author: "krishan (generated)", id: "1646896501-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add EVENT_SELECTION1 clob")
        sql("update EMERGING_ISSUE set EVENT_SELECTION1=EVENT_SELECTION")
        sql("alter table EMERGING_ISSUE drop column  EVENT_SELECTION")
        sql("alter table EMERGING_ISSUE rename column EVENT_SELECTION1 to EVENT_SELECTION")
    }



    changeSet(author: "krishan (generated)", id: "1646896501-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add PRODUCT_SELECTION1 clob")
        sql("update EMERGING_ISSUE set PRODUCT_SELECTION1=PRODUCT_SELECTION")
        sql("alter table EMERGING_ISSUE drop column  PRODUCT_SELECTION")
        sql("alter table EMERGING_ISSUE rename column PRODUCT_SELECTION1 to PRODUCT_SELECTION")
    }



    changeSet(author: "krishan (generated)", id: "1646896501-4") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'EVENT_GROUP_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add EVENT_GROUP_SELECTION1 clob")
        sql("update EMERGING_ISSUE set EVENT_GROUP_SELECTION1=EVENT_GROUP_SELECTION")
        sql("alter table EMERGING_ISSUE drop column  EVENT_GROUP_SELECTION")
        sql("alter table EMERGING_ISSUE rename column EVENT_GROUP_SELECTION1 to EVENT_GROUP_SELECTION")
    }

    changeSet(author: "krishan (generated)", id: "1646896501-5") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCTS')
        }
        sql("alter table EMERGING_ISSUE add PRODUCTS1 clob")
        sql("update EMERGING_ISSUE set PRODUCTS1=PRODUCTS")
        sql("alter table EMERGING_ISSUE drop column  PRODUCTS")
        sql("alter table EMERGING_ISSUE rename column PRODUCTS1 to PRODUCTS")
    }



    changeSet(author: "krishan (generated)", id: "1646896501-6") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EMERGING_ISSUE', columnName: 'PRODUCT_GROUP_SELECTION')
        }
        sql("alter table EMERGING_ISSUE add PRODUCT_GROUP_SELECTION1 clob")
        sql("update EMERGING_ISSUE set PRODUCT_GROUP_SELECTION1=PRODUCT_GROUP_SELECTION")
        sql("alter table EMERGING_ISSUE drop column  PRODUCT_GROUP_SELECTION")
        sql("alter table EMERGING_ISSUE rename column PRODUCT_GROUP_SELECTION1 to PRODUCT_GROUP_SELECTION")
    }

    changeSet(author: "rahul (generated)", id: "37283791301-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PRODUCT_EVENT_HISTORY', columnName: 'PRODUCT_ID')
            }
        }
        addColumn(tableName: "PRODUCT_EVENT_HISTORY") {
            column(name: "PRODUCT_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rahul (generated)", id: "37283791301-8") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'JUSTIFICATION', columnName:"JUSTIFICATION")
        }
        modifyDataType(columnName: "JUSTIFICATION", newDataType: "varchar2(8000 char)", tableName: "JUSTIFICATION")
    }

    changeSet(author: "yogesh (generated)", id: "37283791301-10") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VALIDATED_SIGNAL')
        }
        grailsChange {
            change {
                Sql sql
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE VALIDATED_SIGNAL SET  SIGNAL_STATUS = 'Ongoing' WHERE SIGNAL_STATUS IS NULL")

                } catch(Exception e){
                    println("########## Some error occurred while saving value in VALIDATED SIGNAL STATUS #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1649401661916-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SINGLE_CASE_ALERT')
        }
        grailsChange {
            change {
                Sql sql
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE SINGLE_CASE_ALERT SET  OUTCOME = null WHERE OUTCOME='undefined' ")

                } catch(Exception e){
                    println("########## Some error occurred while saving value in SINGLE CASE ALERT #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1649401661916-2") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SINGLE_ON_DEMAND_ALERT')
        }
        grailsChange {
            change {
                Sql sql
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE SINGLE_ON_DEMAND_ALERT SET  OUTCOME = null WHERE OUTCOME='undefined' ")

                } catch(Exception e){
                    println("########## Some error occurred while saving value in SINGLE ON DEMAND CASE ALERT #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }


    changeSet(author: "Rahul (generated)", id: "37283791301-22") {
            preConditions(onFail: 'MARK_RAN') {
                not {
                    columnExists(tableName: 'EX_RCONFIG', columnName: 'STRATIFICATION_COLUMNS')
                }
            }
            addColumn(tableName: "EX_RCONFIG") {
                column(name: "STRATIFICATION_COLUMNS",  type: "varchar2(8000 CHAR)"){
                    constraints(nullable: "true")
                }
            }
    }

    changeSet(author: "Rahul (generated)", id: "37283791301-23") {
            preConditions(onFail: 'MARK_RAN') {
                not {
                    columnExists(tableName: 'EX_RCONFIG', columnName: 'STRAT_COL_DATA_MINING')
                }
            }
            addColumn(tableName: "EX_RCONFIG") {
                column(name: "STRAT_COL_DATA_MINING",  type: "varchar2(8000 CHAR)"){
                    constraints(nullable: "true")
                }
            }
    }

}
