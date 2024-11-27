import com.rxlogix.EmailNotification
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Preference
import grails.util.Holders
import groovy.sql.Sql

databaseChangeLog = {
    changeSet(author: "nitesh (generated)", id: "1632254096076-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'RPT_FIELD_INFO')
        }
        grailsChange {
            change {

                Map customExpressionMap = ["masterSuspProdList":"rtrim(ltrim(REGEXP_REPLACE (REPLACE(REPLACE( ccpa.SUSP_PROD_2_ALL ,'!@##@!',', '),'!@_@!','-'),'[0-9]'||'!@.@!',''),', '),', ')",
                                           "masterPregnancyFlag":"DECODE(cf.flag_pregnancy,1,'Yes','No')"]
                customExpressionMap.each {key, val ->
                    Sql sql
                    try{
                        sql = new Sql(ctx.getBean("dataSource"))
                        sql.execute("UPDATE RPT_FIELD_INFO SET  CUSTOM_EXPRESSION = ${val} WHERE ARGUS_NAME = ${key}")

                    } catch(Exception e){
                        println("########## Some error occurred while saving value in ReportFieldInfo #############")
                        e.printStackTrace()
                    } finally {
                        sql?.close()
                    }
                }
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1632254096076-3") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'RPT_FIELD_INFO')
        }
        grailsChange {
            change {
                Sql sql
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE RPT_FIELD_INFO SET  CUSTOM_EXPRESSION = null WHERE ARGUS_NAME = 'masterPrefTermList'")

                } catch(Exception e){
                    println("########## Some error occurred while saving value in ReportFieldInfo #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "shivam (generated)", id: "1613730275341-04") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'ACA_VAERS'")
        }

        grailsChange {
            change {
                try {
                    EmailNotification acaVaers = new EmailNotification(key: 'ACA_VAERS', moduleName: 'Alert Trigger Email for Aggregate Review Alert for VAERS', isEnabled: true, defaultValue: true)
                    acaVaers.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Alert Trigger Email for Aggregate Review Alert for VAERS ###########")
                }
            }
        }
    }

    changeSet(author: "shivam (generated)", id: "1613730275341-05") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'DISPOSITION_CHANGE_ACA_VAERS'")
        }

        grailsChange {
            change {
                try {
                    EmailNotification notification = new EmailNotification(key: 'DISPOSITION_CHANGE_ACA_VAERS', moduleName: 'Disposition Change Notification for Aggregate Review Alerts for VAERS', isEnabled: false, defaultValue: false)
                    notification.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Disposition Change Notification for Aggregate Review Alerts for VAERS ###########")
                }
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "1613730275341-06") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'ACA_INTEGRATED'")
        }

        grailsChange {
            change {
                try {
                    EmailNotification emailNotification = new EmailNotification(key: 'ACA_INTEGRATED', moduleName: 'Alert Trigger Email for Aggregate Review Alert for Integrated review', isEnabled: true, defaultValue: true)
                    emailNotification.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Alert Trigger Email for Aggregate Review Alert for Integrated review ###########")
                }
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "1613730275341-07") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'DISPOSITION_CHANGE_ACA_INTEGRATED'")
        }

        grailsChange {
            change {
                try {
                    EmailNotification emailNotification = new EmailNotification(key: 'DISPOSITION_CHANGE_ACA_INTEGRATED', moduleName: 'Disposition Change Notification for Aggregate Review Alerts for Integrated review', isEnabled: false, defaultValue: false)
                    emailNotification.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Disposition Change Notification for Aggregate Review Alerts for Integrated review ###########")
                }
            }
        }
    }
    changeSet(author: "suraj (generated)", id: "1631641054247-160") {

        grailsChange {
            change {
                try {
                    List<Preference> prefrences = Preference.createCriteria().list {
                        isNotNull("dashboardConfig")
                    }
                    prefrences.each {
                        if (it != null) {
                            Map widgetConfig = Holders.config.signal.dashboard.widgets.config
                            it.dashboardConfig = JsonOutput.toJson(widgetConfig)
                            ctx.CRUDService.update(it)
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace()
                    println(ex)
                    println("##################### Error updating dashboardConfig #############")
                }
            }
        }
    }

    changeSet(author: "nitesh (generated)", id: "1632254096076-4") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'RPT_FIELD_INFO')
        }
        grailsChange {
            change {

                Map customExpressionMap = ["masterPregnancyFlag":"(SELECT a.state_ynu FROM vw_clp_state_ynu a WHERE a.id=cf.flag_pregnancy AND a.tenant_id=cf.tenant_id)",
                                           "eventPrimaryOnsetDatePartial":"SUBSTR(ce.TXT_DATE_AE_START,1,11)",
                                           "masterCreateTime":"TO_DATE(TO_CHAR(cm.DATE_FIRST_CREATE,'DD-MON-YYYY'),'DD-MON-YYYY')",
                                           "patInfoPatDobPartial":"SUBSTR(cpi.TXT_DATE_PATIENT_BIRTH,1,11)"]
                customExpressionMap.each {key, val ->
                    Sql sql
                    try{
                        sql = new Sql(ctx.getBean("dataSource"))
                        sql.execute("UPDATE RPT_FIELD_INFO SET  CUSTOM_EXPRESSION = ${val} WHERE ARGUS_NAME = ${key}")

                    } catch(Exception e){
                        println("########## Some error occurred while saving value in ReportFieldInfo #############")
                        e.printStackTrace()
                    } finally {
                        sql?.close()
                    }
                }
            }
        }
    }
    changeSet(author: "nitesh (generated)", id: "1632254096076-5") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'RPT_FIELD_INFO')
        }
        grailsChange {
            change {
                Sql sql
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    sql.execute("UPDATE RPT_FIELD_INFO SET  CUSTOM_EXPRESSION = null WHERE ARGUS_NAME = 'masterCreateTime'")

                } catch(Exception e){
                    println("########## Some error occurred while saving value in ReportFieldInfo #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }

    // ################## Grails Change For Due In ######################
    changeSet(author: "ujjwal (generated)", id: "20211209141656-202") {
        grailsChange {
            change {
                try {
                    println "Executing upgrade scenarios for signal."
                    List<ValidatedSignal> validatedSignalList = ValidatedSignal.list()
                    validatedSignalList.each { ValidatedSignal validatedSignal ->
                        List<Map> signalHistoryList = validatedSignal?.signalStatusHistories as List<Map>
                        SignalStatusHistory signalStatusHistory = signalHistoryList.find { it.signalStatus == 'Date Closed' }
                        boolean isSignalClosed =  signalStatusHistory != null
                        if(isSignalClosed){
                            if(!validatedSignal.actualDueDate){
                                validatedSignal.actualDueDate = signalStatusHistory?.dateCreated
                            }
                            if(!validatedSignal.milestoneCompletionDate){
                                validatedSignal.milestoneCompletionDate = signalStatusHistory?.dateCreated
                            }
                        } else {
                            if(!validatedSignal.actualDueDate){
                                validatedSignal.actualDueDate = validatedSignal.dueDate
                            }
                        }
                        ctx.CRUDService.update(validatedSignal)
                    }
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while upgrading the signal. #############")
                }
            }
        }
    }
}