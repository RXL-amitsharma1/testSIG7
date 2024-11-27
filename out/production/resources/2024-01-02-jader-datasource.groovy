import com.rxlogix.EmailNotification
import com.rxlogix.user.Role
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "rahul (generated)", id: "1704176741-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'jader_date_range')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "jader_date_range", type: "VARCHAR2(255 CHAR)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1704176741-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'jader_columns')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "jader_columns", type: "varchar2(4000 CHAR)")
        }
    }

    changeSet(author: "rahul (generated)", id: "1704176741-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'jader_columns')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "jader_columns", type: "varchar2(4000 CHAR)")
        }
    }
    changeSet(author: "rahul (generated)", id: "1704176741-04") {

        preConditions(onFail: 'MARK_RAN') {
            not{
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'ACA_JADER'")
        }

        grailsChange {
            change{
                try {
                    EmailNotification acaJader = new EmailNotification(key: 'ACA_JADER', moduleName: 'Alert Trigger Email for Aggregate Review Alert for JADER', isEnabled: true, defaultValue: true)
                    acaJader.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Alert Trigger Email for Aggregate Review Alert for JADER ###########")
                }
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1704176741-05") {

        preConditions(onFail: 'MARK_RAN') {
            not{
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'DISPOSITION_CHANGE_ACA_JADER'")
        }

        grailsChange {
            change{
                try {
                    EmailNotification notification = new EmailNotification(key: 'DISPOSITION_CHANGE_ACA_JADER', moduleName: 'Disposition Change Notification for Aggregate Review Alerts for JADER', isEnabled: true, defaultValue: true)
                    notification.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Disposition Change Notification for Aggregate Review Alerts for JADER ###########")
                }
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1704176741-06") {

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ROLE')
        }
        grailsChange {
            change{
                try {
                    if(Holders.config.signal.jader.enabled){
                        Role jaderRole = Role.findByAuthority("ROLE_JADER_CONFIGURATION")
                        if(!jaderRole){
                            jaderRole = new Role(authority: "ROLE_JADER_CONFIGURATION", description: "Perform JADER alerts configuration and review",
                                    createdBy: "Application", modifiedBy: "Application")
                            jaderRole.save(flush: true, failOnError: true)
                        }
                    }

                } catch (Exception ex){
                    println ("Some error occured while updating roles")
                    ex.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1704176741-11") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'risk_category')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "risk_category", type: "varchar2(256 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rahul (generated)", id: "1704176741-12") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'reporter_qualification')
            }
        }
        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "reporter_qualification", type: "varchar2(256 char)") {
                constraints(nullable: "true")
            }
        }
    }
}