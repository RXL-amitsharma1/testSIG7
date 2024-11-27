import com.rxlogix.audit.AuditTrail
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.ViewInstanceService
import com.rxlogix.AlertService
import grails.util.Holders

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
}