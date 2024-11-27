import com.rxlogix.AggregateCaseAlertService
import com.rxlogix.signal.AggregateOnDemandAlertService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.ValidatedSignal

databaseChangeLog = {
    changeSet(author: "rishabh (generated)", id: "20211209141656-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'DATA_MINING_VARIABLE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "DATA_MINING_VARIABLE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "20211209141656-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'DATA_MINING_VARIABLE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DATA_MINING_VARIABLE", type: "VARCHAR2(255 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "20211209141656-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_PRODUCT_MINING')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_PRODUCT_MINING", type: "BOOLEAN") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "20211209141656-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_PRODUCT_MINING')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_PRODUCT_MINING", type: "BOOLEAN") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "20211209141656-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VIEW_INSTANCE', columnName: 'KEY_ID')
            }
        }
        addColumn(tableName: "VIEW_INSTANCE") {
            column(name: "KEY_ID", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-6") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ADVANCED_FILTER', columnName: 'KEY_ID')
            }
        }
        addColumn(tableName: "ADVANCED_FILTER") {
            column(name: "KEY_ID", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GLOBAL_PRODUCT_EVENT', columnName: 'PRODUCT_KEY_ID')
            }
        }
        addColumn(tableName: "GLOBAL_PRODUCT_EVENT") {
            column(name: "PRODUCT_KEY_ID", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-8") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'GLOBAL_PRODUCT_EVENT', columnName: 'EVENT_KEY_ID')
            }
        }
        addColumn(tableName: "GLOBAL_PRODUCT_EVENT") {
            column(name: "EVENT_KEY_ID", type: "NUMBER") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608626578696-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PREFERENCE', columnName: 'API_TOKEN')
            }
        }
        addColumn(tableName: "PREFERENCE") {
            column(name: "API_TOKEN", type: "varchar2(4000 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "rishabh (generated)", id: "1608626578696-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PREFERENCE', columnName: 'TOKEN_UPDATE_DATE')
            }
        }
        addColumn(tableName: "PREFERENCE") {
            column(name: "TOKEN_UPDATE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608626578695-103") {
        not {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM AGG_ALERT WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1)")
        }
        sql("""UPDATE AGG_ALERT A SET A.DUE_DATE = NULL WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1);
                         UPDATE ARCHIVED_AGG_ALERT A SET A.DUE_DATE = NULL WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1);
                     COMMIT;
                     """)
    }

    changeSet(author: "rishabh (generated)", id: "1608626578695-104") {
        not {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM SINGLE_CASE_ALERT WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1)")
        }
        sql("""UPDATE SINGLE_CASE_ALERT A SET A.DUE_DATE = NULL WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1);
                         UPDATE ARCHIVED_SINGLE_CASE_ALERT A SET A.DUE_DATE = NULL WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1);
                     COMMIT;
                     """)
    }

    changeSet(author: "rishabh (generated)", id: "1608626578695-105") {
        not {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM EVDAS_ALERT WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1)")
        }
        sql("""UPDATE EVDAS_ALERT A SET A.DUE_DATE = NULL WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1);
                         UPDATE ARCHIVED_EVDAS_ALERT A SET A.DUE_DATE = NULL WHERE DISPOSITION_ID IN (SELECT ID FROM DISPOSITION WHERE CLOSED=1 OR REVIEW_COMPLETED=1);
                     COMMIT;
                     """)
    }

    changeSet(author: "shivam (generated)", id: "20211209141656-16") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'PROD_HIERARCHY_ID')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "PROD_HIERARCHY_ID", type: "NUMBER", defaultValue: '-1') {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-17") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ALERT', columnName: 'EVENT_HIERARCHY_ID')
            }
        }
        addColumn(tableName: "AGG_ALERT") {
            column(name: "EVENT_HIERARCHY_ID", type: "NUMBER", defaultValue: '-1') {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-18") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'PROD_HIERARCHY_ID')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "PROD_HIERARCHY_ID", type: "NUMBER", defaultValue: '-1') {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-19") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'EVENT_HIERARCHY_ID')
            }
        }
        addColumn(tableName: "ARCHIVED_AGG_ALERT") {
            column(name: "EVENT_HIERARCHY_ID", type: "NUMBER", defaultValue: '-1') {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-20") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'PROD_HIERARCHY_ID')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "PROD_HIERARCHY_ID", type: "NUMBER", defaultValue: '-1') {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20211209141656-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'EVENT_HIERARCHY_ID')
            }
        }
        addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
            column(name: "EVENT_HIERARCHY_ID", type: "NUMBER", defaultValue: '-1') {
                constraints(nullable: "true")
            }
        }
    }

    // #################### Product Groups Changes #####################
    changeSet(author: "Kundan.Kumar (generated)", id: "1632254096072-122") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'PROD_GROUPS_STATUS')
            }
        }
        createTable(tableName: "PROD_GROUPS_STATUS") {
            column(name: "id", type: "number(19,0)") {  constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PROD_GROUPS_STATUS_PK")  }
            column(name: "VERSION", type: "number(19,0)") { constraints(nullable: "false") }
            column(name: "UNIQUE_IDENTIFIER", type: "varchar2(300 char)") {  constraints(nullable: "false")  }
            column(name: "COUNT", type: "number(19,0)")
            column(name: "VALID_RECORD_COUNT", type: "number(19,0)")
            column(name: "INVALID_RECORD_COUNT", type: "number(19,0)")
            column(name: "UPLOADED_DATE", type: "TIMESTAMP")
            column(name: "ADDED_BY", type: "varchar2(55 char)")
            column(name: "IS_API_PROCESSED", type: "NUMBER(1, 0)")
        }
    }
    changeSet(author: "Kundan.Kumar (generated)", id: "1632254096072-123") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'PROD_GROUPS_STATUS_SEQ')
            }
        }
        createSequence(sequenceName: "PROD_GROUPS_STATUS_SEQ")
    }
    changeSet(author: "Kundan.Kumar (generated)", id: "1632254096072-124") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'PROD_GROUPS_DATA')
            }
        }
        createTable(tableName: "PROD_GROUPS_DATA") {
            column(name: "id", type: "number(19,0)") {  constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PROD_GROUPS_DATA_PK")  }
            column(name: "PRODUCT_GROUP_STATUS_ID", type: "number(19,0)") {  constraints(nullable: "true")  }
            column(name: "VERSION", type: "number(19,0)") { constraints(nullable: "false") }
            column(name: "UNIQUE_IDENTIFIER", type: "varchar2(300 char)") {  constraints(nullable: "false")  }
            column(name: "GROUP_NAME", type: "varchar2(300 char)")
            column(name: "GROUP_OLD_NAME", type: "varchar2(300 char)")
            column(name: "DESCRIPTION", type: "varchar2(4000 char)")
            column(name: "COPY_GROUPS", type: "varchar2(300 char)")
            column(name: "SHARED_WITH", type: "varchar2(300 char)")
            column(name: "OWNER", type: "varchar2(300 char)")
            column(name: "INCLUDE_SOURCES", type: "varchar2(1000 char)")
            column(name: "TYPE", type: "number(19,0)")
            column(name: "TENANT_ID", type: "number(19,0)")
            column(name: "DATA", type: "varchar2(4000 char)")
            column(name: "VALIDATION_ERROR", type: "varchar2(300 char)")
            column(name: "STATUS", type: "varchar2(50 char)")
        }
    }
    changeSet(author: "Kundan.Kumar (generated)", id: "1632254096072-125") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sequenceExists(sequenceName: 'PROD_GROUPS_DATA_SEQ')
            }
        }
        createSequence(sequenceName: "PROD_GROUPS_DATA_SEQ")
    }
    changeSet(author: "Kundan.Kumar (generated)", id: "1632254096072-126") {
        preConditions(onFail: 'MARK_RAN') {
            foreignKeyConstraintExists(foreignKeyName: "FK_PROD_GROUPS_DATA1")
        }
        addForeignKeyConstraint(baseColumnNames: "PRODUCT_GROUP_STATUS_ID", baseTableName: "PROD_GROUPS_DATA", constraintName: "FK_PROD_GROUPS_DATA1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "PROD_GROUPS_STATUS", referencesUniqueColumn: "false")
    }
    //      ####### For Grails Change of Aggregate Case Alerts ###################
    changeSet(author: "shivam (generated)", id: "20220120122204-22") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'AGG_ALERT')
        }
        grailsChange {
            change {
                try {
                    AggregateCaseAlertService aggregateCaseAlertService = ctx.aggregateCaseAlertService
                    aggregateCaseAlertService.migrateProdIdAggregateCaseAlerts()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while migrating existing Aggregate Alert for Prod Hirearchy Id. #############")
                }
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20220120122204-23") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'AGG_ALERT')
        }
        grailsChange {
            change {
                try {
                    AggregateCaseAlertService aggregateCaseAlertService = ctx.aggregateCaseAlertService
                    aggregateCaseAlertService.migrateEventIdAggregateCaseAlerts()
                } catch (Exception ex) {
                    println(ex)
                    println("#####################  Error occurred while migrating existing Aggregate Alert for Event Hirearchy Id. #############")
                }
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20220120122204-24") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'agg_on_demand_alert')
        }
        grailsChange {
            change {
                try {
                    AggregateOnDemandAlertService aggregateOnDemandAlertService = ctx.aggregateOnDemandAlertService
                    aggregateOnDemandAlertService.migrateProdIdAggregateOnDemandAlerts()
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while migrating existing Aggregate On Demand Alert for Prod Hirearchy Id. #############")
                }
            }
        }
    }

    changeSet(author: "shivam (generated)", id: "20220120122204-26") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'agg_on_demand_alert')
        }
        grailsChange {
            change {
                try {
                    AggregateOnDemandAlertService aggregateOnDemandAlertService = ctx.aggregateOnDemandAlertService
                    aggregateOnDemandAlertService.migrateSmqForAggregateOnDemandAlerts()
                } catch (Exception ex) {
                    println(ex)
                    println("#####################  Error occurred while migrating existingAggregate On Demand Alert for Event Hirearchy Id. #############")
                }
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20220120122204-28") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'archived_agg_alert')
        }
        grailsChange {
            change {
                try {
                    AggregateCaseAlertService aggregateCaseAlertService = ctx.aggregateCaseAlertService
                    aggregateCaseAlertService.migrateSmqArchivedAggregateCaseAlerts()
                } catch (Exception ex) {
                    println(ex)
                    println("#####################  Error occurred while migrating existing Archived Aggregate Alert for Event Hirearchy Id. #############")
                }
            }
        }
    }

    changeSet(author: "yogesh (generated)", id: "1632254096072-128") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PROD_GROUPS_STATUS', columnName: 'PVR_ERROR')
            }
        }
        addColumn(tableName: "PROD_GROUPS_STATUS") {
            column(name: "PVR_ERROR", type: "varchar2(8000 char)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "shivam (generated)", id: "20220120122204-1") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "agg_alert_webapp_mig.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }
    changeSet(author: "shivam (generated)", id: "20220120122204-2") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "agg_on_deman_alert_webapp_mig.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }
    changeSet(author: "shivam (generated)", id: "20220120122204-3") {
        sqlFile(dbms: "oracle", encoding: "UTF-8", path: "archived_agg_alert_webapp_mig.sql", relativeToChangelogFile: "true", splitStatements: "false", stripComments: "false")
    }
}