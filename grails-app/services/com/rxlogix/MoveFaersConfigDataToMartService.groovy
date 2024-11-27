package com.rxlogix

import com.rxlogix.config.AlertDateRangeInformation
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutionStatus
import groovy.sql.Sql
import org.hibernate.Session
import org.hibernate.SQLQuery


class MoveFaersConfigDataToMartService {

    def dataSource
    def dataSource_faers
    def sessionFactory
    def sessionFactory_faers
    def grailsApplication

    def migrateDataToMart() {
        if(grailsApplication.config.signal.faers.enabled) {
            Session session = sessionFactory.currentSession
            Session faersSession = sessionFactory_faers.currentSession
            Sql sql = new Sql(dataSource)
            Sql sqlFaers = new Sql(dataSource_faers)
            SQLQuery sqlQuery = faersSession.createSQLQuery("SELECT count(*) FROM user_tab_columns where TABLE_NAME = 'RCONFIG' ")
            def resultSet = sqlQuery.list()
            def columnCount = resultSet[0]
            try {
                if (columnCount > 0) {
                    List<Configuration> configurationList = Configuration.createCriteria().list {
                        like('selectedDatasource', "%faers%")
                        eq('type', "Aggregate Case Alert")
                        isNull("migratedToMartDate")
                    } as List<Configuration>
                    List<Configuration> oldConfigurationList = Configuration.createCriteria().list {
                        like('selectedDatasource', "%faers%")
                        eq('type', "Aggregate Case Alert")
                        sqlRestriction("LAST_UPDATED > MIGRATED_TO_MART_DATE")
                        isNotNull("migratedToMartDate")
                    } as List<Configuration>
                    String dateFormat = 'YYYY-MM-DD HH24:MI:SS.FF'
                    List configIdList = configurationList*.id
                    List<Long> oldConfigIdList = oldConfigurationList*.id

                    Date newDate = new Date()
                    String currentDate = "TO_TIMESTAMP('${newDate.toTimestamp()}','${dateFormat}')"
                    if (configurationList) {
                        log.info("Found ${configurationList.size()} new faers configfuration to be migrated to DB")

                        List<ExecutionStatus> executionStatusList = ExecutionStatus.createCriteria().list {
                            configIdList.collate(1000).each {
                                'in'("configId", it)
                            }
                        } as List<ExecutionStatus>

                        List<AlertDateRangeInformation> alertDateRangeInformationList = configurationList*.alertDateRangeInformation

                        configurationList.each { Configuration configuration ->
                            String insertStatement = insertQueryConfig(configuration)
                            sqlFaers.execute(insertStatement)
                        }
                        executionStatusList?.each { ExecutionStatus executionStatus ->
                            String insertStatement = insertQueryExecutionStatus(executionStatus)
                            sqlFaers.execute(insertStatement)
                        }

                        alertDateRangeInformationList?.each { AlertDateRangeInformation alertDateRangeInformation ->
                            String insertStatement = insertQueryDateRange(alertDateRangeInformation)
                            sqlFaers.execute(insertStatement)
                        }
                        sql.withBatch(100, "update rconfig set MIGRATED_TO_MART_DATE = ${currentDate} WHERE ID = :id", { preparedStatement ->
                            configIdList.each {
                                preparedStatement.addBatch(id: it)
                            }
                        })
                        log.info("Successfully migrated ${configurationList.size()} new faers configfuration to DB")
                    }
                    if (oldConfigurationList) {
                        log.info("Found ${oldConfigurationList.size()} old faers configfuration to be migrated to DB")
                        List<ExecutionStatus> OldExecutionStatusList = ExecutionStatus.createCriteria().list {
                            oldConfigIdList.collate(1000).each {
                                'in'("configId", it)
                            }
                        } as List<ExecutionStatus>

                        List<AlertDateRangeInformation> oldAlertDateRangeInformationList = configurationList*.alertDateRangeInformation

                        List<Long> oldAlertDateRangeInformationIdList = oldAlertDateRangeInformationList*.id
                        List<Long> oldExecutionStatusList = OldExecutionStatusList*.id

                        String deleteQuery = "delete from rconfig where id in (" + oldConfigIdList.join(',') + ")"
                        sqlFaers.execute(deleteQuery)
                        oldConfigurationList?.each { Configuration configuration ->
                            String insertStatement = insertQueryConfig(configuration)
                            sqlFaers.execute(insertStatement)
                        }

                        if (OldExecutionStatusList) {
                            String deleteQueryExecution = "delete from ex_status where id in (" + oldExecutionStatusList.join(',') + ")"
                            sqlFaers.execute(deleteQueryExecution)
                            OldExecutionStatusList.each { ExecutionStatus executionStatus ->
                                String insertStatement = insertQueryExecutionStatus(executionStatus)
                                sqlFaers.execute(insertStatement)
                            }
                        }
                        if (oldAlertDateRangeInformationList) {
                            String deleteQueryDateRange = "delete from ex_status where id in (" + oldAlertDateRangeInformationIdList.join(',') + ")"
                            sqlFaers.execute(deleteQueryDateRange)
                            oldAlertDateRangeInformationList.each { AlertDateRangeInformation alertDateRangeInformation ->
                                String insertStatement = insertQueryDateRange(alertDateRangeInformation)
                                sqlFaers.execute(insertStatement)
                            }
                        }
                        sql.withBatch(100, "update rconfig set MIGRATED_TO_MART_DATE = ${currentDate} WHERE ID = :id", { preparedStatement ->
                            oldConfigIdList.each {
                                preparedStatement.addBatch(id: it)
                            }
                        })
                        log.info("Successfully migrated ${oldConfigurationList.size()} old faers configfuration to DB")
                    }
                } else {
                    log.info("Not configured to send faers data to DB")
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            } finally {
                sql?.close()
                sqlFaers?.close()
                faersSession.flush()
                faersSession.clear()
                session.flush()
                session.clear()
            }
        }
    }


    String insertQueryConfig(Configuration configuration) {
        String dateFormat = 'YYYY-MM-DD HH24:MI:SS.FF'
        def modifiedBy = configuration.modifiedBy ? "'${configuration.modifiedBy}'" : null
        def productDictionarySelection = configuration.productDictionarySelection ? "'${configuration.productDictionarySelection?.replaceAll("(?i)'", "''")}'" : null
        def productGroupSelection = configuration.productGroupSelection ? "'${configuration.productGroupSelection?.replaceAll("(?i)'", "''")}'" : null
        def productSelection = configuration.productSelection ? "'${configuration.productSelection?.replaceAll("(?i)'", "''")}'" : null
        def referenceNumber = configuration.referenceNumber ? "'${configuration.referenceNumber}'" : null
        def scheduleDateJSON = configuration.scheduleDateJSON ? "'${configuration.scheduleDateJSON?.replaceAll("(?i)'", "''")}'" : null
        def type = configuration.type ? "'${configuration.type}'" : null
        def alertCaseSeriesName = configuration.alertCaseSeriesName ? "'${configuration.alertCaseSeriesName}'" : null
        def dataMiningVariable = configuration.dataMiningVariable ? "'${configuration.dataMiningVariable?.replaceAll("(?i)'", "''")}'" : null
        def selectedDataSheet = configuration.selectedDataSheet ? "'${configuration.selectedDataSheet}'" : null
        def datasheetType = configuration.datasheetType ? "'${configuration.datasheetType}'" : null
        def alertForegroundQueryName = configuration.alertForegroundQueryName ? "'${configuration.alertForegroundQueryName?.replaceAll("(?i)'", "''")}'" : null
        def adjustmentTypeEnum = configuration.adjustmentTypeEnum ? "'${configuration.adjustmentTypeEnum}'" : null
        def skippedAlertGroupCode = configuration.skippedAlertGroupCode ? "'${configuration.skippedAlertGroupCode}'" : null
        def futureScheduleDateJSON = configuration.futureScheduleDateJSON ? "'${configuration.futureScheduleDateJSON?.replaceAll("(?i)'", "''")}'" : null
        def alertDisableReason = configuration.alertDisableReason ? "'${configuration.alertDisableReason}'" : null
        def deletionStatus = configuration.deletionStatus ? "'${configuration.deletionStatus}'" : null
        def aggAlertId = configuration.aggAlertId ? "'${configuration.aggAlertId}'" : null
        def aggCountType = configuration.aggCountType ? "'${configuration.aggCountType}'" : null
        def alertQueryName = configuration.alertQueryName ? "'${configuration.alertQueryName?.replaceAll("(?i)'", "''")}'" : null
        def alertRmpRemsRef = configuration.alertRmpRemsRef ? "'${configuration.alertRmpRemsRef?.replaceAll("(?i)'", "''")}'" : null
        def configSelectedTimeZone = configuration.configSelectedTimeZone ? "'${configuration.configSelectedTimeZone}'" : null
        def dateRangeType = configuration.dateRangeType ? "'${configuration.dateRangeType}'" : null
        def description = configuration.description ? "'${configuration.description?.replaceAll("(?i)'", "''")}'" : null
        def drugClassification = configuration.drugClassification ? "'${configuration.drugClassification}'" : null
        def drugType = configuration.drugType ? "'${configuration.drugType}'" : null
        def evaluateDateAs = configuration.evaluateDateAs ? "'${configuration.evaluateDateAs}'" : null
        def eventGroupSelection = configuration.eventGroupSelection ? "'${configuration.eventGroupSelection?.replaceAll("(?i)'", "''")}'" : null
        def eventSelection = configuration.eventSelection ? "'${configuration.eventSelection?.replaceAll("(?i)'", "''")}'" : null
        def nextRunDate = configuration.nextRunDate ? "TO_TIMESTAMP('${configuration.nextRunDate}','${dateFormat}')" : null
        def onOrAfterDate = configuration.onOrAfterDate ? "TO_TIMESTAMP('${configuration.onOrAfterDate}','${dateFormat}')" : null
        def asOfVersionDate = configuration.asOfVersionDate ? "TO_TIMESTAMP('${configuration.asOfVersionDate}','${dateFormat}')" : null
        def dataMiningVariableValue = configuration.dataMiningVariableValue ? "'${configuration.dataMiningVariableValue?.replaceAll("(?i)'", "''")}'" : null
        def foregroundSearchAttr = configuration.foregroundSearchAttr ? "'${configuration.foregroundSearchAttr?.replaceAll("(?i)'", "''")}'" : null
        def spotfireSettings = configuration.spotfireSettings ? "'${configuration.spotfireSettings?.replaceAll("(?i)'", "''")}'" : null
        def studySelection = configuration.studySelection ? "'${configuration.studySelection?.replaceAll("(?i)'", "''")}'" : null
        def name = configuration.name ? "'${configuration.name?.replaceAll("(?i)'", "''")}'" : null
        def blankValuesJSON = configuration.blankValuesJSON ? "'${configuration.blankValuesJSON?.replaceAll("(?i)'", "''")}'" : null

        String insertStatement = """
                                         INSERT INTO RCONFIG (IS_ENABLED,IS_PUBLIC,IS_RESUME,LAST_UPDATED,LIMIT_PRIMARY_PATH,MISSED_CASES,MODIFIED_BY,NAME,NEXT_RUN_DATE,NUM_OF_EXECUTIONS,ON_OR_AFTER_DATE,PVUSER_ID,PRIORITY_ID,
                                         PRODUCT_DICTIONARY_SELECTION,PRODUCT_GROUP_SELECTION,PRODUCT_SELECTION,REFERENCE_NUMBER,REPEAT_EXECUTION,REVIEW_PERIOD,SCHEDULE_DATE,SELECTED_DATA_SOURCE,SPOTFIRE_SETTINGS,STUDY_SELECTION,
                                         SUSPECT_PRODUCT,TEMPLATE_ID,TOTAL_EXECUTION_TIME,TYPE,WORKFLOW_GROUP,IS_TEMPLATE_ALERT,CONFIGURATION_TEMPLATE_ID,ALERT_CASE_SERIES_ID,ALERT_CASE_SERIES_NAME,DATA_MINING_VARIABLE,IS_PRODUCT_MINING,
                                         MASTER_CONFIG_ID,IS_LATEST_MASTER,SELECTED_DATASHEET,IS_DATASHEET_CHECKED,DATASHEET_TYPE,DATA_MINING_VARIABLE_VALUE,ALERT_FG_QUERY_ID,FG_QUERY_NAME,FG_SEARCH,FG_SEARCH_ATTR,IS_AUTO_PAUSED,
                                         IS_MANUALLY_PAUSED,ADJUSTMENT_TYPE_ENUM,SKIPPED_ALERT_ID,SKIPPED_ALERT_GROUP_CODE,FUTURE_SCHEDULE_DATE,AUTO_PAUSED_EMAIL_SENT,ALERT_DISABLE_REASON,DELETION_IN_PROGRESS,DELETION_STATUS,IS_AUTO_TRIGGER,IS_CASE_SERIES,
                                         IS_DELETED,ID,VERSION,ADHOC_RUN,ADJUST_PER_SCHED_FREQUENCY,AGG_ALERT_ID,AGG_COUNT_TYPE,AGG_EXECUTION_ID,ALERT_DATA_RANGE_ID,ALERT_QUERY_ID,QUERY_NAME,ALERT_RMP_REMS_REF,ALERT_TRIGGER_CASES,ALERT_TRIGGER_DAYS,
                                         APPLY_ALERT_STOP_LIST,AS_OF_VERSION_DATE,AS_OF_VERSION_DATE_DELTA,ASSIGNED_TO_ID,ASSIGNED_TO_GROUP_ID,BLANK_VALUES,SELECTED_TIME_ZONE,CREATED_BY,DATE_CREATED,DATE_RANGE_TYPE,DESCRIPTION,DRUG_CLASSIFICATION,
                                         DRUG_TYPE,EVALUATE_DATE_AS,EVENT_GROUP_SELECTION,EVENT_SELECTION,EXCLUDE_FOLLOWUP,EXCLUDE_NON_VALID_CASES,EXECUTING,GROUP_BY_SMQ,INCLUDE_LOCKED_VERSION,INCL_MEDICAL_CONFIRM_CASES,INTEGRATED_CONFIGURATION_ID,IS_AUTO_ASSIGNED_TO)
                                         VALUES (${configuration.isEnabled ? 1 : 0},${configuration.isPublic ? 1 : 0},${configuration.isResume ? 1 : 0},TO_TIMESTAMP('${configuration.lastUpdated}','${dateFormat}'),${configuration.limitPrimaryPath ? 1 : 0},${configuration.missedCases ? 1 : 0},${modifiedBy},${name},${nextRunDate},${configuration.numOfExecutions},${onOrAfterDate},${configuration.owner.id},${configuration.priorityId},
                                         ${productDictionarySelection},${productGroupSelection},${productSelection},${referenceNumber},${configuration.repeatExecution ? 1 : 0},${configuration.reviewPeriod},${scheduleDateJSON},'${configuration.selectedDatasource}',${spotfireSettings},${studySelection},
                                         ${configuration.suspectProduct ? 1 : 0},${configuration.templateId},${configuration.totalExecutionTime},${type},${configuration.workflowGroup.id},${configuration.isTemplateAlert ? 1 : 0},${configuration.configurationTemplateId},${configuration.alertCaseSeriesId},${alertCaseSeriesName},${dataMiningVariable},${configuration.isProductMining? 1 : 0},
                                         ${configuration.masterConfigId},${configuration.isLatestMaster ? 1 : 0},${selectedDataSheet},${configuration.isDatasheetChecked ? 1 : 0},${datasheetType},${dataMiningVariableValue},${configuration.alertForegroundQueryId},${alertForegroundQueryName},${configuration.foregroundSearch ? 1 : 0},${foregroundSearchAttr},${configuration.isAutoPaused ? 1 : 0},
                                         ${configuration.isManuallyPaused ? 1 : 0},${adjustmentTypeEnum},${configuration.skippedAlertId},${skippedAlertGroupCode},${futureScheduleDateJSON},${configuration.autoPausedEmailTriggered ? 1:0},${alertDisableReason},${configuration.deletionInProgress ? 1 : 0},${deletionStatus},${configuration.isAutoTrigger ? 1 : 0},${configuration.isCaseSeries ? 1 : 0},
                                         ${configuration.isDeleted ? 1 : 0},${configuration.id},${configuration.version},${configuration.adhocRun ? 1 : 0},${configuration.adjustPerScheduleFrequency ? 1 : 0},${aggAlertId},${aggCountType},${configuration.aggExecutionId},${configuration.alertDateRangeInformationId},${configuration.alertQueryId},${alertQueryName},${alertRmpRemsRef},${configuration.alertTriggerCases},${configuration.alertTriggerDays},
                                         ${configuration.applyAlertStopList ? 1 : 0},${asOfVersionDate},${configuration.asOfVersionDateDelta},${configuration.assignedTo?.id},${configuration.assignedToGroup?.id},${blankValuesJSON},${configSelectedTimeZone},'${configuration.createdBy}',TO_TIMESTAMP('${configuration.dateCreated}','${dateFormat}'),${dateRangeType},${description},${drugClassification},
                                         ${drugType},${evaluateDateAs},${eventGroupSelection},${eventSelection},${configuration.excludeFollowUp ? 1 : 0},${configuration.excludeNonValidCases ? 1 : 0},${configuration.executing ? 1 : 0},${configuration.groupBySmq ? 1 : 0},${configuration.includeLockedVersion ? 1 : 0},0,${configuration.integratedConfigurationId},${configuration.isAutoAssignedTo ? 1 : 0})
                                         """
        return insertStatement
    }

    String insertQueryExecutionStatus(ExecutionStatus executionStatus){
        String dateFormat = 'YYYY-MM-DD HH24:MI:SS.FF'
        def alertFilePath = executionStatus.alertFilePath ? "'${executionStatus.alertFilePath}'" : null
        def frequency = executionStatus.frequency ? "'${executionStatus.frequency}'" : null
        def reportExecutionStatus = executionStatus.reportExecutionStatus ? "'${executionStatus.reportExecutionStatus}'" : null
        def spotfireExecutionStatus = executionStatus.spotfireExecutionStatus ? "'${executionStatus.spotfireExecutionStatus}'" : null
        def spotfireFileName = executionStatus.spotfireFileName ? "'${executionStatus.spotfireFileName?.replaceAll("(?i)'", "''")}'" : null
        def nodeName = executionStatus.nodeName ? "'${executionStatus.nodeName}'" : null
        def stackTrace = executionStatus.stackTrace ? "'${executionStatus.stackTrace?.replaceAll("(?i)'", "''")}'" : null
        def nextRunDate = executionStatus.nextRunDate ? "TO_TIMESTAMP('${executionStatus.nextRunDate}','${dateFormat}')" : null
        def timeStampJSON = executionStatus.timeStampJSON ? "'${executionStatus.timeStampJSON}'" : null
        def name = executionStatus.name ? "'${executionStatus.name?.replaceAll("(?i)'", "''")}'" : null
        String insertStatement = """
                                         INSERT INTO EX_STATUS (ID,VERSION,ALERT_FILE_PATH,CONFIG_ID,DATE_CREATED,END_TIME,EXECUTED_CONFIG_ID,EXECUTION_LEVEL,EX_STATUS,FREQUENCY,LAST_UPDATED,NAME,NEXT_RUN_DATE,
                                         OWNER_ID,REPORT_EXECUTION_STATUS,RPT_VERSION,SPOTFIRE_EXECUTION_STATUS,SPOTFIRE_FILE_NAME,STACK_TRACE,START_TIME,TIME_STAMPJSON,TYPE,NODE_NAME,IS_MASTER)
                                         VALUES (${executionStatus.id},${executionStatus.version},${alertFilePath},${executionStatus.configId},TO_TIMESTAMP('${executionStatus.dateCreated}','${dateFormat}'),${executionStatus.endTime},${executionStatus.executedConfigId},${executionStatus.executionLevel},'${executionStatus.executionStatus}',${frequency},TO_TIMESTAMP('${executionStatus.lastUpdated}','${dateFormat}'),${name},${nextRunDate},
                                         ${executionStatus.ownerId},${reportExecutionStatus},${executionStatus.reportVersion},${spotfireExecutionStatus},${spotfireFileName},${stackTrace},${executionStatus.startTime},${timeStampJSON},'${executionStatus.type}',${nodeName},${executionStatus.isMaster ? 1 : 0})
                                         """
        return insertStatement
    }
    String insertQueryDateRange(AlertDateRangeInformation alertDateRangeInformation){
        String dateFormat = 'YYYY-MM-DD HH24:MI:SS.FF'
        def dateRangeEndAbsolute = alertDateRangeInformation.dateRangeEndAbsolute ? "TO_TIMESTAMP('${alertDateRangeInformation.dateRangeEndAbsolute}','${dateFormat}')" : null
        def dateRangeStartAbsolute = alertDateRangeInformation.dateRangeStartAbsolute ? "TO_TIMESTAMP('${alertDateRangeInformation.dateRangeStartAbsolute}','${dateFormat}')" : null
        String insertStatement = """
                                         INSERT INTO ALERT_DATE_RANGE (ID,VERSION,DATE_RNG_END_ABSOLUTE,DATE_RNG_END_DELTA,DATE_RNG_ENUM,DATE_RNG_START_ABSOLUTE,DATE_RNG_START_DELTA,RELATIVE_DATE_RNG_VALUE)
                                         VALUES(${alertDateRangeInformation.id},${alertDateRangeInformation.version},${dateRangeEndAbsolute},${alertDateRangeInformation.dateRangeEndAbsoluteDelta},'${alertDateRangeInformation.dateRangeEnum}',${dateRangeStartAbsolute},${alertDateRangeInformation.dateRangeStartAbsoluteDelta},${alertDateRangeInformation.relativeDateRangeValue})
                                         """
        return insertStatement
    }

}
