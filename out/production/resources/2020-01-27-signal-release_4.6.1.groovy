import com.rxlogix.CaseHistoryService
import com.rxlogix.EmailNotification
import com.rxlogix.ProductGroupService
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.config.SignalStrategy
import com.rxlogix.signal.Alert
import com.rxlogix.signal.BusinessConfiguration
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.ProductIngredientMapping
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal

databaseChangeLog = {
    changeSet(author: "anshul (generated)", id: "1574116848588-1") {

        preConditions(onFail: 'MARK_RAN') {
            not{
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'DISPOSITION_AUTO_ROUTE_LA'")
        }

        grailsChange {
            change{
                try {
                    EmailNotification autoRouteLiteratureNotification = new EmailNotification(key: 'DISPOSITION_AUTO_ROUTE_LA', moduleName: 'Auto Route Disposition for Literature Alerts', isEnabled: false, defaultValue: false)
                    autoRouteLiteratureNotification.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Auto Route Disposition for Literature Alerts ###########")
                }
            }
        }
    }

    changeSet(author: "rishabhgupta (generated)", id: "1577349618529-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERTS', columnName: 'EVENT_SELECTION')
        }
        modifyDataType(columnName: "EVENT_SELECTION", newDataType: "varchar2(8000 char)", tableName: "ALERTS")
    }

    changeSet(author: "rishabhgupta (generated)", id: "1577349618529-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'INBOX_LOG', columnName: 'CONTENT')
        }
        modifyDataType(columnName: "CONTENT", newDataType: "varchar2(8000 char)", tableName: "INBOX_LOG")
    }

    changeSet(author: "anshul (generated)", id: "1588796599646-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ATTACHMENT', columnName: 'REFERENCE_LINK')
        }
        modifyDataType(tableName: "ATTACHMENT", columnName: "REFERENCE_LINK", newDataType: "varchar2(2000 CHAR)")
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'RCONFIG')
        }

        grailsChange {
            change {
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(Configuration, "RCONFIG", "PRODUCT_SELECTION")
                } catch (Exception ex) {
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in RCONFIG table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-2") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_RCONFIG')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(ExecutedConfiguration, "EX_RCONFIG", "PRODUCT_SELECTION")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in EX_RCONFIG table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-3") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'LITERATURE_CONFIG')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(LiteratureConfiguration, "LITERATURE_CONFIG", "PRODUCT_SELECTION")

                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in LITERATURE_CONFIG table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-4") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_LITERATURE_CONFIG')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(ExecutedLiteratureConfiguration, "EX_LITERATURE_CONFIG", "PRODUCT_SELECTION")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in EX_LITERATURE_CONFIG table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-5") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'LITERATURE_ALERT')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(LiteratureAlert, "LITERATURE_ALERT", "PRODUCT_SELECTION")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in LITERATURE_ALERT table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-6") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'SIGNAL_STRATEGY')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(SignalStrategy, "SIGNAL_STRATEGY", "PRODUCT_SELECTION")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in SIGNAL_STRATEGY table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-7") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ALERTS')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(Alert, "ALERTS", "PRODUCT_SELECTION")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in ALERTS table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-8") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'BUSINESS_CONFIGURATION')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(BusinessConfiguration, "BUSINESS_CONFIGURATION", "PRODUCTS")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in BUSINESS_CONFIGURATION table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-9") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'PRODUCT_INGREDIENT_MAPPING')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(ProductIngredientMapping, "PRODUCT_INGREDIENT_MAPPING", "PRODUCTS")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in PRODUCT_INGREDIENT_MAPPING table ###########")
                }
            }
        }
    }

    changeSet(author: "sandeep (generated)", id: "1576573896983-10") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'VALIDATED_SIGNAL')
        }

        grailsChange {
            change{
                try {
                    ProductGroupService productGroupService = ctx.productGroupService
                    productGroupService.productSelectionUpgrade(ValidatedSignal, "VALIDATED_SIGNAL", "PRODUCTS")
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while running the migration for the product group addition in product dictionary, in VALIDATED_SIGNAL table ###########")
                }
            }
        }
    }

    changeSet(author: "amit (generated)", id: "15736313649001-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_disposition')
            }
        }
        createIndex(indexName: "idx_single_alert_disposition", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "DISPOSITION_ID")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_single_alert_exconfig", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "EXEC_CONFIG_ID")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_single_alert_isNew')
            }
        }
        createIndex(indexName: "idx_single_alert_isNew", tableName: "SINGLE_CASE_ALERT", unique: "false") {
            column(name: "is_New")
        }

        preConditions(onFail: 'MARK_RAN') {
            not {
                indexExists(indexName: 'idx_agg_alert_exconfig')
            }
        }
        createIndex(indexName: "idx_agg_alert_exconfig", tableName: "AGG_ALERT", unique: "false") {
            column(name: "EXEC_CONFIGURATION_ID")
        }
    }

      changeSet(author: "amrendra (generated)", id: "1580891106734-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'MASTER_PREF_TERM_ALL')
        }

        addColumn(tableName: "SINGLE_CASE_ALERT") {
            column(name: "MASTER_PREF_TERM_ALL_COPY", type: "CLOB")
        }

        sql("update SINGLE_CASE_ALERT set MASTER_PREF_TERM_ALL_COPY = MASTER_PREF_TERM_ALL;")

        dropColumn(tableName: "SINGLE_CASE_ALERT", columnName: "MASTER_PREF_TERM_ALL")

        renameColumn(tableName: "SINGLE_CASE_ALERT", oldColumnName: "MASTER_PREF_TERM_ALL_COPY", newColumnName: "MASTER_PREF_TERM_ALL")

    }

    changeSet(author: "ankit (generated)", id: "1580891106735-2") {

        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'REPORT_HISTORY', columnName: 'IS_REPORT_GENERATED')
            }
        }

        addColumn(tableName: "REPORT_HISTORY") {
            column(name: "IS_REPORT_GENERATED", type: "NUMBER(1, 0)",defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
        sql('''update REPORT_HISTORY set IS_REPORT_GENERATED = 1 where memo_report is not null''')

    }

    changeSet(author: "sandeep (generated)", id: "1581484264048-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'CASE_HISTORY')
        }
        grailsChange{
            change{
                try {
                    List<CaseHistory> caseHistoryList = CaseHistory.findAllByExecConfigId(null)
                    Configuration configuration
                    caseHistoryList.each {
                        configuration = Configuration.get(it.configId)
                        it.execConfigId = SingleCaseAlert.findByAlertConfiguration(configuration).executedAlertConfiguration?.id
                    }
                    CaseHistoryService caseHistoryService = ctx.caseHistoryService
                    caseHistoryService.batchPersistCaseHistory(caseHistoryList)
                }catch(Exception e){
                    println("################## Error occurred while updating ExecutedConfigId in Case History table ###############################")
                }
            }
        }
    }
}
