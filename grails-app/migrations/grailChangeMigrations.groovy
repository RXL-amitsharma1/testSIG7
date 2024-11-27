import com.rxlogix.audit.AuditTrail
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.ViewInstanceService
import com.rxlogix.AlertService
import grails.util.Holders
import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "Hritik Chaudhary (generated)", id: "1675329552-01") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'PRODUCTS_AND_GROUP_COMBINATION')
        }

        grailsChange {
            change {
                try {
                    List<ValidatedSignal> allSignals = ValidatedSignal.getAll()

                    sql.withBatch(1000, "UPDATE VALIDATED_SIGNAL SET PRODUCTS_AND_GROUP_COMBINATION = :products WHERE ID = :id", { preparedStatement ->
                        allSignals.each {
                            String productsName = ctx.alertService.productSelectionSignal(it)
                            preparedStatement.addBatch(id: it.id, products: productsName)
                        }
                    })

                } catch (Exception ex) {
                    println "##### Error Occurred while inserting product and productGroups in PRODUCT_AND_GROUP_COMBINATION column for Validated_Signal Table change-set ####"
                    ex.printStackTrace()
                }
            }
        }
    }
    changeSet(author: "Hritik Chaudhary (generated)", id: "1675329552-02") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'PRODUCTS_AND_GROUP_COMBINATION')
        }

        grailsChange {
            change {
                try {
                    List<ValidatedSignal> allSignals = ValidatedSignal.getAll()

                    sql.withBatch(1000, "UPDATE VALIDATED_SIGNAL SET EVENTS_AND_GROUP_COMBINATION = :events WHERE ID = :id", { preparedStatement ->
                        allSignals.each {
                            String eventsName = ctx.alertService.eventSelectionSignalWithSmq(it)
                            preparedStatement.addBatch(id: it.id, events: eventsName)
                        }
                    })

                } catch (Exception ex) {
                    println "##### Error Occurred while inserting event and eventGroups in EVENTS_AND_GROUP_COMBINATION column for Validated_Signal Table change-set ####"
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1664865102244-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SIGNAL_LINKED_SIGNALS')
        }
        grailsChange {
            change {
                Sql sql
                try {
                    List allSignals = ValidatedSignal.list()
                    def signalWithLinkedSignals = allSignals.findAll {
                        (it.linkedSignals != [] && it.linkedSignals != null)
                    }
                    sql = new Sql(ctx.getBean("dataSource"))
                    signalWithLinkedSignals.each { it1 ->
                        it1.linkedSignals.each { it2 ->
                            def linkSignalCount = sql.rows("""select count(*) from SIGNAL_LINKED_SIGNALS where VALIDATED_SIGNAL_ID =${it2.id}""")
                            def finalCount = linkSignalCount[0][0] == 0 ? 0 : linkSignalCount[0][0] + 1
                            sql.execute("""INSERT INTO SIGNAL_LINKED_SIGNALS(VALIDATED_SIGNAL_ID,LINKED_SIGNAL_ID,linked_signals_idx) VALUES(${it2.id},${it1.id},${finalCount})""")
                            finalCount = finalCount + 1
                        }
                    }
                } catch (Exception e) {
                    println("########## Some error occurred while saving value in SIGNAL_LINKED_SIGNAL TABLE #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "Krishna Joshi (generated)", id: "1677042451840-003") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ROLE')
        }
        grailsChange {
            change {
                try {
                    Role healthRole = Role.findByAuthority("ROLE_HEALTH_CONFIGURATION")
                    if (!healthRole) {
                        healthRole = new Role(authority: "ROLE_HEALTH_CONFIGURATION", description: "Performs system precheck health status",
                                createdBy: "Application", modifiedBy: "Application")
                        healthRole.save(flush: true, failOnError: true)
                    }
                } catch (Exception ex) {
                    println("Some error occured while updating roles")
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "isha (generated)", id: "2019093015000-3") {
        grailsChange {
            change {
                User.withSession { session ->
                    try {
                        User.list().each {
                            it.lastLogin = AuditTrail.findByUsernameAndCategory(it.username,'LOGIN_SUCCESS' , [sort: 'dateCreated', order: 'desc'])?.dateCreated
                            it.save()
                        }
                        session.flush()
                    } catch (Exception ex) {
                        println "##### Error Occurred while updating old records for User Last Login liquibase changeset 2019093015000-3 ####"
                        ex.printStackTrace(System.out)
                    }
                }
            }
        }
    }

    changeSet(author: "Yogesh (generated)", id: "1717067128001-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_activities_alert_id')
            }
        }
        createIndex(indexName: "idx_activities_alert_id", tableName: "activities") {
            column(name: "alert_id")
        }
    }}