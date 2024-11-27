import com.rxlogix.ValidatedSignalService
import com.rxlogix.signal.ValidatedSignal
import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "yogesh (generated)", id: "1661417794399-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_PRODUCTS')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ALL_PRODUCTS", type: "varchar2(8000 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1661417794399-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'VALIDATED_SIGNAL_ALL_PRODUCT')
            }
        }
        createTable(tableName: "VALIDATED_SIGNAL_ALL_PRODUCT") {
            column(name: "VALIDATED_SIGNAL_ID", type: "NUMBER(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "SIGNAL_ALL_PRODUCTS", type: "varchar(15000)")

            column(name: "ALL_PRODUCTS_IDX", type: "NUMBER(10, 0)")
        }
    }

    changeSet(author: "yogesh (generated)", id: "1665467828217-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ALL_PRODUCTS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ALL_PRODUCTS", type: "varchar2(8000 char)"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rahul (generated)", id: "1665467828217-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_EVENTS')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ALL_EVENTS", type: "varchar2(8000 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1665467828217-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_EVENTS_WITHOUT_HIERARCHY')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ALL_EVENTS_WITHOUT_HIERARCHY", type: "varchar2(8000 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1665467828217-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_SMQS')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "ALL_SMQS", type: "varchar2(8000 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1666882901-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'ALL_PRODUCTS')
            }
        }
        addColumn(tableName: "EX_EVDAS_CONFIG") {
            column(name: "ALL_PRODUCTS", type: "varchar2(8000 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1667368625-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'last_disp_change')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "last_disp_change", type: "timestamp"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1669188660-11") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_EVENTS_WITHOUT_HIERARCHY')
        }
        sql("alter table VALIDATED_SIGNAL add ALL_EVENTS_WITHOUT_HIERARCHY1 clob")
        sql("update VALIDATED_SIGNAL set ALL_EVENTS_WITHOUT_HIERARCHY1=ALL_EVENTS_WITHOUT_HIERARCHY")
        sql("alter table VALIDATED_SIGNAL drop column  ALL_EVENTS_WITHOUT_HIERARCHY")
        sql("alter table VALIDATED_SIGNAL rename column ALL_EVENTS_WITHOUT_HIERARCHY1 to ALL_EVENTS_WITHOUT_HIERARCHY")
    }

    changeSet(author: "rahul (generated)", id: "1669188660-12") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_EVENTS')
        }
        sql("alter table VALIDATED_SIGNAL add ALL_EVENTS1 clob")
        sql("update VALIDATED_SIGNAL set ALL_EVENTS1=ALL_EVENTS")
        sql("alter table VALIDATED_SIGNAL drop column  ALL_EVENTS")
        sql("alter table VALIDATED_SIGNAL rename column ALL_EVENTS1 to ALL_EVENTS")
    }

    changeSet(author: "rahul (generated)", id: "1669188660-13") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_SMQS')
        }
        sql("alter table VALIDATED_SIGNAL add ALL_SMQS1 clob")
        sql("update VALIDATED_SIGNAL set ALL_SMQS1=ALL_SMQS")
        sql("alter table VALIDATED_SIGNAL drop column  ALL_SMQS")
        sql("alter table VALIDATED_SIGNAL rename column ALL_SMQS1 to ALL_SMQS")
    }

    changeSet(author: "rahul (generated)", id: "1669188660-14") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_RCONFIG', columnName: 'ALL_PRODUCTS')
        }
        sql("alter table EX_RCONFIG add ALL_PRODUCTS1 clob")
        sql("update EX_RCONFIG set ALL_PRODUCTS1=ALL_PRODUCTS")
        sql("alter table EX_RCONFIG drop column  ALL_PRODUCTS")
        sql("alter table EX_RCONFIG rename column ALL_PRODUCTS1 to ALL_PRODUCTS")
    }

    changeSet(author: "rahul (generated)", id: "1669188660-15") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'ALL_PRODUCTS')
        }
        sql("alter table EX_EVDAS_CONFIG add ALL_PRODUCTS1 clob")
        sql("update EX_EVDAS_CONFIG set ALL_PRODUCTS1=ALL_PRODUCTS")
        sql("alter table EX_EVDAS_CONFIG drop column  ALL_PRODUCTS")
        sql("alter table EX_EVDAS_CONFIG rename column ALL_PRODUCTS1 to ALL_PRODUCTS")
    }
    changeSet(author: "yogesh (generated)", id: "1669188660-31") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'ALL_EVENTS_WITHOUT_HIERARCHY')
        }
        grailsChange {
            change {
                Sql sql
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE VALIDATED_SIGNAL SET ALL_EVENTS_WITHOUT_HIERARCHY = NULL WHERE ALL_EVENTS_WITHOUT_HIERARCHY IS NOT NULL")

                } catch(Exception e){
                    println("########## Some error occurred while saving value in VALIDATE SIGNAL #############")
                    println e
                } finally {
                    sql?.close()
                }
            }
        }
    }


    changeSet(author: "yogesh (generated)", id: "1668161823-106") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VALIDATED_SIGNAL')
        }
        grailsChange {
            change {
                Sql sql
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE VALIDATED_SIGNAL SET  LAST_DISP_CHANGE = LAST_UPDATED WHERE LAST_DISP_CHANGE IS NULL")

                } catch(Exception e){
                    println("########## Some error occurred while saving value in VALIDATE SIGNAL #############")
                    println e
                } finally {
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1669188660-101") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VALIDATED_SIGNAL')
        }
        grailsChange {
            change {
                try {
                    def signalList = ValidatedSignal.list().findAll {
                        (it.allEventsWithoutHierarchy == null)
                    }
                    List<Map> signalDetailsList = []
                    ValidatedSignalService validatedSignalService = ctx.getBean("validatedSignalService")
                    signalList.each { validatedSignal ->
                        try {
                            ValidatedSignal.withTransaction {
                                Map<String, List<String>> allEventList = validatedSignalService.getAllEventsWithHierarchy(validatedSignal)
                                String allEventsString = allEventList?.eventListHierarchy?.unique() as String
                                String allEventsWithoutHierarchy = allEventList?.eventList?.unique() as String
                                if (allEventsString && allEventsString.length() > 0) {
                                    allEventsString = allEventsString.substring(1, allEventsString.length() - 1)
                                }
                                if (allEventsWithoutHierarchy && allEventsWithoutHierarchy.length() > 0) {
                                    allEventsWithoutHierarchy = allEventsWithoutHierarchy.substring(1, allEventsWithoutHierarchy.length() - 1)
                                }
                                String allSmqString = ""
                                if (validatedSignal.events) {
                                    allSmqString = validatedSignalService.getEventFromJsonWithSmq(validatedSignal.events)?.unique() as String
                                    if (allSmqString && allSmqString.length() > 0) {
                                        allSmqString = allSmqString.substring(1, allSmqString.length() - 1)
                                    }
                                }
                                if((allSmqString?.size() > 31999) || (allEventsString?.size() > 31999) || (allEventsWithoutHierarchy?.size() > 31999)){
                                    println("Selected Events size exceeds 32000 hence data can not be migrated for this signal")
                                }else {
                                    signalDetailsList.add(id: validatedSignal.id, allSmqs: allSmqString, allEvents: allEventsString, allEventsWithoutHierarchy: allEventsWithoutHierarchy)
                                }
                            }
                        } catch (Exception e) {
                            println("########## Some error fetching event group info from pvr for signal ${validatedSignal.name} and eg ${validatedSignal.eventGroupSelection} and pt ${validatedSignal.events}  #############")
                            println e
                        }
                    }
                    sql.withBatch(100, "UPDATE VALIDATED_SIGNAL SET ALL_EVENTS = TO_CLOB(:allEvents), ALL_SMQS = TO_CLOB(:allSmqs), ALL_EVENTS_WITHOUT_HIERARCHY = TO_CLOB(:allEventsWithoutHierarchy) WHERE ID = :id and ALL_EVENTS_WITHOUT_HIERARCHY is null", { preparedStatement ->
                        signalDetailsList.each {
                            preparedStatement.addBatch(id: it.id, allEvents: it.allEvents, allSmqs: it.allSmqs, allEventsWithoutHierarchy: it.allEventsWithoutHierarchy)
                        }
                    })
                }catch(Exception ex){
                    println ex
                    println "Error Occurred while migrating signals information"
                }
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1668161823-108") {
        preConditions(onFail: 'MARK_RAN') {
            and{
                tableExists(tableName: 'VALIDATED_SIGNAL')
                not{
                    sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM VALIDATED_SIGNAL ;")
                }
            }
        }
        grailsChange {
            change {
                Sql sql
                def signalList =  ValidatedSignal.list().findAll {
                    (it.allProducts == [] || it.allProducts == null)
                }
                ValidatedSignalService validatedSignalService=ctx.getBean("validatedSignalService")
                signalList.each{validatedSignal->
                    try{
                        sql = new Sql(ctx.getBean("dataSource"))
                        List<String> allProductList = validatedSignalService.getAllProductsWithHierarchy(validatedSignal)
                        allProductList = allProductList?.unique()
                        Integer index = 0
                        allProductList.each{
                            sql.execute("""INSERT INTO VALIDATED_SIGNAL_ALL_PRODUCT(VALIDATED_SIGNAL_ID,SIGNAL_ALL_PRODUCTS,ALL_PRODUCTS_IDX) VALUES(${validatedSignal.id},${it},${index})""")
                            index = index + 1
                        }
                    }catch(Exception e) {
                        println("########## Some error fetching product group info from pvr for signal ${validatedSignal.name} and pg ${validatedSignal.productGroupSelection} and pt ${validatedSignal.products}  #############")
                        println e
                    } finally {
                        sql?.close()
                    }
                }

            }
        }
    }

}