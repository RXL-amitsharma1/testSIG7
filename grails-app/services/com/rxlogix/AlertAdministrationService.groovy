package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.dto.MissedAlertNotificationDTO
import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.SkippedAlertStateEnum
import com.rxlogix.signal.ActionJustification
import com.rxlogix.signal.AlertPreExecutionCheck
import com.rxlogix.signal.AlertPreExecutionCheckHistory
import com.rxlogix.signal.AutoAdjustmentRule
import com.rxlogix.signal.AutoAdjustmentRuleHistory
import com.rxlogix.signal.SkippedAlertInformation
import com.rxlogix.user.User
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.sql.Sql
import org.apache.http.HttpStatus
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType


@Transactional
class AlertAdministrationService {

    def configurationService
    def skippedAlertService
    def CRUDService
    def emailService
    def reportIntegrationService
    def reportExecutorService
    def evdasAlertExecutionService
    def masterExecutorService
    def userService
    def dataSource_pva
    SignalAuditLogService signalAuditLogService

    AlertPreExecutionCheck alertPreExecutionCheck
    Map<String, AutoAdjustmentRule> autoAdjustmentRules = new HashMap<>()

    Boolean isPvrCheckEnabled
    Boolean isEtlCheckEnabled

    Boolean isPvrAccessible
    Map<String, Object> EtlStatusMap

    void initializePreChecks() {

        alertPreExecutionCheck = AlertPreExecutionCheck.first()

        List<AutoAdjustmentRule> autoAdjustmentRulesList = AutoAdjustmentRule.findAll()
        autoAdjustmentRulesList.each { autoAdjustmentRule -> autoAdjustmentRules.put(autoAdjustmentRule.alertType, autoAdjustmentRule)
        }

        isPvrCheckEnabled = alertPreExecutionCheck?.isPvrCheckEnabled
        isEtlCheckEnabled = alertPreExecutionCheck?.isVersionAsOfCheckEnabled || alertPreExecutionCheck?.isEtlInProgressCheckEnabled || alertPreExecutionCheck?.isEtlFailureCheckEnabled

        if (isPvrCheckEnabled) {
            isPvrAccessible = getPvrConnectionStatus()
        }
        if (isEtlCheckEnabled) {
            EtlStatusMap = getEtlStatus()
        }
    }

    Boolean isPreChecksEnabled() {
        alertPreExecutionCheck = AlertPreExecutionCheck.first()
        return alertPreExecutionCheck?.isPvrCheckEnabled || alertPreExecutionCheck?.isVersionAsOfCheckEnabled || alertPreExecutionCheck?.isEtlInProgressCheckEnabled || alertPreExecutionCheck?.isEtlFailureCheckEnabled
    }

    void autoPauseConfigurationAndTriggerNotification(def configuration, String alertType, String alertDisableReason) {

        log.info("Enabling auto paused state for configuration ${configuration.name} [${configuration.id}].")
        log.info("Alert disable reason ${alertDisableReason}.")

        boolean isSafetyAlert = isSafetyAlert(configuration)
        boolean isScheduledConfiguration = isScheduledConfiguration(configuration)


        if (isSafetyAlert && isScheduledConfiguration) {
            SkippedAlertInformation skippedAlertInstance = skippedAlertService.saveFirstMissedExecutionInformation(configuration, alertType, alertDisableReason)
            configuration.setSkippedAlertGroupCode(skippedAlertInstance.groupCode)
        }

        enableAutoPausedState(configuration, alertDisableReason)
        CRUDService.updateWithoutAuditLog(configuration)

    }

    void autoPauseMasterConfigurationAndTriggerNotification(MasterConfiguration masterConfiguration, Configuration childConfiguration, String alertType, String alertDisableReason) {

        log.info("Enabling auto paused state for configuration ${masterConfiguration.name} [${masterConfiguration.id}].")
        log.info("Alert disable reason ${alertDisableReason}.")


        enableAutoPausedState(masterConfiguration, alertDisableReason)
        CRUDService.updateWithoutAuditLog(masterConfiguration)


    }


    Map<String, List<Object>> fetchAutoPausedConfigurations() {

        Map<String, List<Object>> autoPausedConfigurations = new HashMap<>()

        List<Configuration> pausedSingleCaseConfigurations = Configuration.fetchAutoPausedConfigurations(Constants.AlertConfigType.SINGLE_CASE_ALERT, reportExecutorService.currentlyRunning)
        List<Configuration> pausedAggregateConfigurations = Configuration.fetchAutoPausedConfigurations(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, reportExecutorService.currentlyQuantRunning)
        List<EvdasConfiguration> pausedEvdasConfigurations = EvdasConfiguration.fetchAutoPausedConfigurations(evdasAlertExecutionService.currentlyRunning)

        if (pausedSingleCaseConfigurations) {
            autoPausedConfigurations.put(Constants.AlertConfigType.SINGLE_CASE_ALERT, pausedSingleCaseConfigurations)
        }
        if (pausedAggregateConfigurations) {
            autoPausedConfigurations.put(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, pausedAggregateConfigurations)
        }
        if (pausedEvdasConfigurations) {
            autoPausedConfigurations.put(Constants.AlertConfigType.EVDAS_ALERT, pausedEvdasConfigurations)
        }

        return autoPausedConfigurations
    }

    List<MasterConfiguration> fetchAutoPausedMasterConfigurations() {

        List<MasterConfiguration> pausedMasterConfigurations = MasterConfiguration.fetchPausedMasterConfigurations(masterExecutorService.currentlyMasterRunning)
        return pausedMasterConfigurations
    }


    void updateAutoPausedConfigurations(Map<String, List<Object>> pausedConfigurations) {

        try {
            // fetch the latest values of pvr connection and ETL status
            initializePreChecks()


            //check if the the auto paused alert is now ready for execution, if ready then disable the auto-paused state.
            //then further alert execution job will pick it and execute the alert.
            pausedConfigurations.each { String alertType, List<Object> configurationList ->
                log.info("Found ${configurationList.size()} ${alertType}")
                List<Long> enabledConfigIds = new ArrayList<>()

                configurationList.each { configuration ->

                    boolean isSafetyAlert = isSafetyAlert(configuration)
                    boolean isScheduledConfiguration = isScheduledConfiguration(configuration)

                    boolean isEnabledNow = false


                    if (isSafetyAlert && isScheduledConfiguration) {
                        skippedAlertService.syncSkippedAlerts(configuration, alertType)
                        isEnabledNow = enableSafetyAndScheduledAutoPausedConfiguration(configuration, alertType)
                    } else {
                        isEnabledNow = enableOtherAutoPausedConfiguration(configuration)
                    }
                    if (isEnabledNow) {
                        enabledConfigIds.add(configuration.id)
                    }

                }

                if (!enabledConfigIds.isEmpty()) {
                    log.info("Auto paused state disabled for ${enabledConfigIds.size()} ${alertType} :::::: ${enabledConfigIds.toString()}")
                }

            }
        } catch (Exception ex) {
            log.error('Error occurred during updating auto paused configurations.')
            ex.printStackTrace()
            throw ex
        }

    }

    void updateAutoPausedMasterConfigurations(List<MasterConfiguration> pausedConfigurations) {

        try {
            // fetch the latest values of pvr connection and ETL status
            initializePreChecks()

            //check if the the auto paused alert is now ready for execution, if ready then disable the auto-paused state.
            //then further alert execution job will pick it and execute the alert.
            String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
            log.info("Found ${pausedConfigurations.size()} Master ${alertType}")
            List<Long> enabledConfigIds = new ArrayList<>()


            for (MasterConfiguration masterConfiguration : pausedConfigurations) {
                List<Configuration> configurations = Configuration.findAllByMasterConfigIdAndNextRunDate(masterConfiguration.id, masterConfiguration.nextRunDate)
                Configuration childConfiguration = configurations ? configurations[0] : null
                if (!childConfiguration) {
                    continue
                }

                boolean isEnabledNow = enableAutoPausedMasterConfiguration(masterConfiguration, childConfiguration)

                if (isEnabledNow) {
                    enabledConfigIds.add(masterConfiguration.id)
                }

            }

            if (!enabledConfigIds.isEmpty()) {
                log.info("Auto paused state disabled for ${enabledConfigIds.size()} Master ${alertType} :::::: ${enabledConfigIds.toString()}")
            }


        } catch (Exception ex) {
            log.error('Error occurred during updating paused master configurations.')
            ex.printStackTrace()
            throw ex
        }

    }

    private Boolean enableSafetyAndScheduledAutoPausedConfiguration(Configuration configuration, String alertType) {

        Map<String, Object> resultMap
        Boolean isReadyForExecution = false

        Integer skippedAlertCount = SkippedAlertInformation.countByGroupCodeAndAlertTypeAndStateEnum(configuration.skippedAlertGroupCode, alertType, SkippedAlertStateEnum.CREATED)

        try {
            if (skippedAlertCount == 1) {
                resultMap = isConfigurationReadyForExecution(configuration, true)
                isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)

                if (isReadyForExecution) {
                    log.info("Found only one skipped execution, hence nullifying auto adjustment related flow")
                    // set the groupCode of configuration to null and nullify all the skipped alert flow related date
                    nullifyAutoAdjustmentRelatedFlow(configuration, alertType)
                }
            } else {
                AutoAdjustmentRule autoAdjustmentRule = autoAdjustmentRules.get(alertType)
                AdjustmentTypeEnum adjustmentTypeEnum = autoAdjustmentRule?.adjustmentTypeEnum


                if (autoAdjustmentRule?.isEnabled) {
                    if (adjustmentTypeEnum == AdjustmentTypeEnum.ALERT_PER_SKIPPED_EXECUTION) {
                        resultMap = isConfigurationReadyForExecution(configuration, true)
                        isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
                    } else if (adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
                        SkippedAlertInformation lastSkippedAlertInformation = skippedAlertService.fetchLastSkippedAlertInformation(configuration.skippedAlertGroupCode, alertType)
                        resultMap = isConfigurationReadyForExecution(configuration, true, configuration.evaluateDateAs, lastSkippedAlertInformation.asOfVersionDate)
                        isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
                    }
                    if (isReadyForExecution) {
                        log.info("Auto adjustment rule for ${alertType} is ${adjustmentTypeEnum}")
                        // update configuration and state of skipped alert to READY FOR EXECUTION
                        configuration.adjustmentTypeEnum = adjustmentTypeEnum
                        skippedAlertService.updateSkippedAlertsPostAlertReadyForExecution(configuration, adjustmentTypeEnum)

                        if (adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
                            adjustConfigurationForSingleTypeAutoAdjustment(configuration)
                        }
                    }
                } else {
                    resultMap = isConfigurationReadyForExecution(configuration, true)
                    isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
                    if (isReadyForExecution) {
                        log.info("Auto adjustment rule for ${alertType} is disabled, hence nullifying auto adjustment related flow")
                        // set the groupCode of configuration to null and nullify all the skipped alert flow related date
                        nullifyAutoAdjustmentRelatedFlow(configuration, alertType)
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error occurred while enabling safety alert: ${e.message}", e)
            e.printStackTrace()
        }

        if (isReadyForExecution) {

            log.info("Configuration ${configuration.name}[${configuration.id}] is now ready for execution.")
            log.info("Disabling auto paused state.")

            //disable auto pause state
            disableAutoPausedState(configuration)
            CRUDService.updateWithoutAuditLog(configuration)

        }

        return isReadyForExecution

    }

    private Boolean enableOtherAutoPausedConfiguration(def configuration) {

        Boolean isSafetyAlert = isSafetyAlert(configuration)
        Map<String, Object> resultMap = isConfigurationReadyForExecution(configuration, isSafetyAlert)
        Boolean isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)

        if (isReadyForExecution) {

            log.info("Configuration ${configuration.name}[${configuration.id}] is now ready for execution.")
            log.info("Disabling auto paused state.")

            //disable auto pause state
            disableAutoPausedState(configuration)
            CRUDService.updateWithoutAuditLog(configuration)

        }

        return isReadyForExecution

    }

    private Boolean enableAutoPausedMasterConfiguration(MasterConfiguration masterConfiguration, Configuration childConfiguration) {

        Boolean isSafetyAlert = isSafetyAlert(childConfiguration)
        Map<String, Object> resultMap = isConfigurationReadyForExecution(childConfiguration, isSafetyAlert)
        Boolean isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)

        if (isReadyForExecution) {

            log.info("Master configuration ${masterConfiguration.name}[${masterConfiguration.id}] is now ready for execution.")
            log.info("Disabling auto paused state.")

            //disable auto pause state
            disableAutoPausedState(masterConfiguration)
            CRUDService.updateWithoutAuditLog(masterConfiguration)

        }

        return isReadyForExecution

    }


    void adjustExecutedConfigurationForSingleTypeAutoAdjustment(Configuration configuration, ExecutedConfiguration executedConfiguration) {

        List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.createCriteria().list {
            eq('groupCode', executedConfiguration.skippedAlertGroupCode)
            eq("stateEnum", SkippedAlertStateEnum.READY_FOR_EXECUTION)
            isNull('exConfigId')
            order('id', "asc")
        }
        if (skippedAlertInformationList.size() < 2) {
            return
        }

        log.info("Now adjusting the executed config date range info for single type auto adjustment.")

        SkippedAlertInformation firstSkippedAlertInformation = skippedAlertInformationList.first()
        SkippedAlertInformation lastSkippedAlertInformation = skippedAlertInformationList.last()

        executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUSTOM
        executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute = firstSkippedAlertInformation.skippedAlertDateRangeInformation.getReportStartAndEndDate(configuration).get(0)
        executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute = lastSkippedAlertInformation.skippedAlertDateRangeInformation.getReportStartAndEndDate(configuration).get(1)

    }

    void adjustConfigurationForSingleTypeAutoAdjustment(Configuration configuration) {

        List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.createCriteria().list {
            eq('groupCode', configuration.skippedAlertGroupCode)
            eq("stateEnum", SkippedAlertStateEnum.READY_FOR_EXECUTION)
            isNull('exConfigId')
            order('id', "asc")
        }
        if (skippedAlertInformationList.size() < 2) {
            return
        }

        log.info("Now updating the configuration for single type auto adjustment.")
        SkippedAlertInformation lastSkippedAlertInformation = skippedAlertInformationList.last()

        configuration.nextRunDate = lastSkippedAlertInformation.nextRunDate

        configuration.evaluateDateAs = lastSkippedAlertInformation.evaluateDateAs
        configuration.asOfVersionDate = lastSkippedAlertInformation.asOfVersionDate

        log.info("Successfully updated the configuration...")
    }

    void updateConfigPostAlertExecution(Configuration configuration, ExecutedConfiguration executedConfiguration) {

        // update the status of skipped alert information to EXECUTED and update configuration

        log.info("Updating configuration post alert execution...")
        if (executedConfiguration.adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
            int skippedAlertsCount = SkippedAlertInformation.countByGroupCodeAndExConfigIdAndStateEnum(executedConfiguration.skippedAlertGroupCode, executedConfiguration.id, SkippedAlertStateEnum.EXECUTING)
            for (int counter = 1; counter < skippedAlertsCount; counter++) {
                reportExecutorService.adjustCustomDateRanges(configuration)
            }
        }

        skippedAlertService.updateSkippedAlertsPostAlertCompletion(executedConfiguration)

    }

    void handleFailedSkippedConfiguration(Configuration configuration, ExecutedConfiguration executedConfiguration) {

        if (executedConfiguration.adjustmentTypeEnum == AdjustmentTypeEnum.ALERT_PER_SKIPPED_EXECUTION) {
            configuration.setAdjustmentTypeEnum(AdjustmentTypeEnum.FAILED_ALERT_PER_SKIPPED_EXECUTION)
            SkippedAlertInformation skippedAlertInformation = SkippedAlertInformation.findByGroupCodeAndStateEnumAndExConfigId(executedConfiguration.skippedAlertGroupCode, SkippedAlertStateEnum.EXECUTING, executedConfiguration.id)
            if (skippedAlertInformation) {
                skippedAlertService.updateSkippedAlertState(skippedAlertInformation, SkippedAlertStateEnum.FAILED)
            }
        } else if (executedConfiguration.adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
            configuration.setAdjustmentTypeEnum(AdjustmentTypeEnum.FAILED_SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION)
            List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.findAllByGroupCodeAndStateEnumAndExConfigId(executedConfiguration.skippedAlertGroupCode, SkippedAlertStateEnum.EXECUTING, executedConfiguration.id)
            if (skippedAlertInformationList) {
                skippedAlertInformationList.each { skippedAlertInformation -> skippedAlertService.updateSkippedAlertState(skippedAlertInformation, SkippedAlertStateEnum.FAILED)
                }
            }
        }
    }


    Map<String, Object> isConfigurationReadyForExecution(def configuration, Boolean isSafetyAlert = false, EvaluateCaseDateEnum evaluateDateAs = null, Date asOfVersionDate = null) {

        Map<String, Object> resultMap = new HashMap<>()

        Boolean isReadyForExecution = true
        String alertDisableReason = null

        if (configuration.isResume == true) {
            isReadyForExecution = true
        } else if (configuration instanceof Configuration && configuration.adjustmentTypeEnum && configuration.skippedAlertGroupCode) {
            isReadyForExecution = true
        }else {
            if (isPvrCheckEnabled && !isPvrAccessible) {
                isReadyForExecution = false
                alertDisableReason = Constants.AlertDisableReason.PVR_INACCESSIBLE
            }

            if (isEtlCheckEnabled && isSafetyAlert && isReadyForExecution) {
                evaluateDateAs = evaluateDateAs ?: configuration.evaluateDateAs
                asOfVersionDate = asOfVersionDate ?: configuration.asOfVersionDate

                if (evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF && alertPreExecutionCheck.isVersionAsOfCheckEnabled && asOfVersionDate) {
                    Date latestSuccessETLStartDate = EtlStatusMap.get(Constants.AlertUtils.LATEST_SUCCESS_ETL_START_DATE)
                    // if (last etl start date < asofVersionDate) isReadyForExecution =false
                    if (latestSuccessETLStartDate && latestSuccessETLStartDate < asOfVersionDate) {
                        isReadyForExecution = false
                        alertDisableReason = Constants.AlertDisableReason.ETL_AS_OF_VERSION_FAILURE
                    }

                } else if (evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION) {
                    String lastEtlStatus = EtlStatusMap.get(Constants.AlertUtils.LAST_ETL_STATUS)
                    if (lastEtlStatus) {
                        boolean isEtlInProgress = (lastEtlStatus == "START") ? true : false
                        if (alertPreExecutionCheck.isEtlFailureCheckEnabled && alertPreExecutionCheck.isEtlInProgressCheckEnabled) {
                            //Execute alert if and only if last etl run is successfull
                            if (lastEtlStatus != "SUCCESS") {
                                isReadyForExecution = false
                                alertDisableReason = Constants.AlertDisableReason.LATEST_VERSION_ETL_CHECKS_ENABLED
                            }
                        } else if (alertPreExecutionCheck.isEtlFailureCheckEnabled) {
                            //Execute alert if and only if last etl is success or is in progress
                            if (lastEtlStatus == "ERROR") {
                                isReadyForExecution = false
                                alertDisableReason = Constants.AlertDisableReason.ETL_FAILURE_CHECK_ENABLED
                            }
                        } else if (alertPreExecutionCheck.isEtlInProgressCheckEnabled) {
                            //Execute alert if and only if the last etl is success or failed
                            if (isEtlInProgress) {
                                isReadyForExecution = false
                                alertDisableReason = Constants.AlertDisableReason.ETL_IN_PROGRESS_CHECK_ENABLED
                            }
                        }
                    }
                }
            }
        }

        resultMap.put(Constants.AlertUtils.IS_READY_FOR_EXECUTION, isReadyForExecution)

        if (!isReadyForExecution) {
            resultMap.put(Constants.AlertUtils.ALERT_DISABLE_REASON, alertDisableReason)
        }

        return resultMap

    }

    Map<String, Object> isMasterConfigurationReadyForExecution(MasterConfiguration masterConfiguration, Configuration childConfiguration, boolean isSafetyAlert = false) {

        Map<String, Object> resultMap = new HashMap<>()
        Boolean isReadyForExecution = true

        if (masterConfiguration.isResume == true) {
            isReadyForExecution = true
        } else if (alertPreExecutionCheck?.isEnabledForMasterConfig) {
            return isConfigurationReadyForExecution(childConfiguration, isSafetyAlert)
        }

        resultMap.put(Constants.AlertUtils.IS_READY_FOR_EXECUTION, isReadyForExecution)
        return resultMap

    }


    boolean getPvrConnectionStatus() {
        boolean pvrConnectivity = true
        if (alertPreExecutionCheck.isPvrCheckEnabled) {

            String url = Holders.config.pvreports.url
            String path = Holders.config.pvreports.healthCheck.uri

            try {
                Map response = reportIntegrationService.postData(url, path, null)
                log.info("Health Check API Response from PVR : ${response}")
                if (response.status == HttpStatus.SC_OK) {
                    pvrConnectivity = true
                } else {
                    pvrConnectivity = false
                }
            } catch (Exception ex) {
                pvrConnectivity = false
                log.error(ex.printStackTrace())
            }
            log.info("PVR connectivity status: " + pvrConnectivity)
        }
        pvrConnectivity
    }


    def getEtlStatus() {
        Map<String, Object> EtlStatusMap = new HashMap<>()
        String lastEtlStatusSQl = "select log_id, etl_status from pvr_etl_execution_log where log_id is not null order by log_id desc fetch first 1 row only"
        String LatestSuccessEtlSQL = "select log_id, etl_start_date from pvr_etl_execution_log where etl_status='SUCCESS' order by log_id desc fetch first 1 row only"


        Sql sql = new Sql(dataSource_pva)
        try {

            sql.eachRow(lastEtlStatusSQl, []) { row ->
                if (row.etl_status) {
                    EtlStatusMap.put(Constants.AlertUtils.LAST_ETL_STATUS, row.etl_status)
                    EtlStatusMap.put(Constants.AlertUtils.LAST_ETL_STATUS_LOG_ID, row.log_id)
                }
            }

            sql.eachRow(LatestSuccessEtlSQL, []) { row ->
                if (row.etl_start_date) {
                    EtlStatusMap.put(Constants.AlertUtils.LATEST_SUCCESS_ETL_START_DATE, row.etl_start_date)
                    EtlStatusMap.put(Constants.AlertUtils.LATEST_SUCCESS_ETL_LOG_ID, row.log_id)
                }
            }

        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            sql?.close()
        }
        log.info("ETL status: " + EtlStatusMap)

        return EtlStatusMap
    }


    void updateConfigPostExecutedConfigInitialization(Configuration configuration) {
        log.info("Updating config post ex-config initialization...")
        configuration.setSkippedAlertId(null)
        configuration.setSkippedAlertGroupCode(null)
        configuration.setAdjustmentTypeEnum(null)
    }

    void nullifyAutoAdjustmentRelatedFlow(Configuration configuration, String alertType) {
        // nullify all skipped alert informations if any and set auto adjustment type to manual autoadjustment
        List<SkippedAlertInformation> skippedAlertInformationList = SkippedAlertInformation.findAllByConfigIdAndAlertTypeAndExConfigIdIsNull(configuration.id, alertType)
        skippedAlertInformationList.each { skippedAlertInformation ->
            skippedAlertInformation.exConfigId = -1
            skippedAlertInformation.stateEnum = SkippedAlertStateEnum.DISCARDED
            skippedAlertInformation.save(flush: true)
        }


        //set values for configuration
        configuration.setAdjustmentTypeEnum(null)
        configuration.setSkippedAlertId(null)
        configuration.setSkippedAlertGroupCode(null)


        //enable configuration if autoPaused
        if (configuration.isAutoPaused) {
            disableAutoPausedState(configuration)
        }

        //update configuration instance
        CRUDService.updateWithoutAuditLog(configuration)

    }

    void enableAutoPausedState(def configuration, String alertDisableReason) {
        configuration.isAutoPaused = true
        configuration.autoPausedEmailTriggered = false
        configuration.alertDisableReason = alertDisableReason
    }

    void disableAutoPausedState(def configuration) {
        configuration.isAutoPaused = false
        configuration.autoPausedEmailTriggered = null
        configuration.alertDisableReason = null
    }

    Map<String, List<Long>> disableConfigurations(List<Long> configIdList, String alertType, String justification) {
        List configurations = getAllConfigurationsByAlertType(configIdList, alertType)
        Map<String, List<Long>> data = ["recordsToBeUpdated": configIdList, "recordsUpdated": configurations*.id]
        if (!configurations.isEmpty()) {
            configurations.each { it ->
                it.isManuallyPaused = true
                CRUDService.saveWithoutAuditLog(it)
                saveAuditForManuallyPause(it,justification)
            }
            log.info("${alertType} with id --> ${configurations*.id} has been successfully disabled.")
            data << ["status": true]
        } else {
            log.info("Configurations not found.")
            data << ["status": false]
        }
        return data
    }

    Map<String, List<Long>> enableConfigurations(List<Long> configIdList, String alertType, String justification) {
        List configurations = getAllConfigurationsByAlertType(configIdList, alertType)
        Map<String, List<Long>> data = ["recordsToBeUpdated": configIdList, "recordsUpdated": configurations*.id]
        if (!configurations.isEmpty()) {
            configurations.each { it ->
                it.isManuallyPaused = false
                CRUDService.saveWithoutAuditLog(it)
                saveAuditForManuallyPause(it,justification)
            }
            log.info("${alertType} with id --> ${configurations*.id} has been successfully enabled.")
            data << ["status": true]
        } else {
            log.info("Configurations not found.")
            data << ["status": false]
        }
        return data
    }

    def saveAuditForManuallyPause(def config, String justification = "") {
        def auditTrailMap = [
                entityName : config.getClass().getSimpleName(),
                moduleName : config.getModuleNameForMultiUseDomains(),
                category   : AuditTrail.Category.UPDATE.toString(),
                entityValue: config.getInstanceIdentifierForAuditLog(),
                description: "",
        ]
        List<Map> auditChildMap = []
        def childEntry = [:]
        childEntry = [
                propertyName: "Enabled",
                oldValue    : config.isManuallyPaused ? "Yes" : "No",
                newValue    : config.isManuallyPaused ? "No" : "Yes",]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Justification",
                newValue    : justification]
        auditChildMap << childEntry
        signalAuditLogService.createAuditLog(auditTrailMap, auditChildMap)
    }

    Map<String, Map> prepareDataForChildAlertDeletion(List<Long> configIds) {

        Map childConfigs = new HashMap<>()
        Map masterChildConfigData = new HashMap<>()
        List<String> siblingConfigNames=new ArrayList()
        List siblingConfigIds=new ArrayList()

        configIds.each { configId ->
            Configuration configuration = Configuration.get(configId)
            if (configuration.masterConfigId) {
                childConfigs.put(configId, configuration.name)
                List<String> configNames = null
                if (!masterChildConfigData.get(configuration.masterConfigId)) {
                      siblingConfigNames = Configuration.findAllByMasterConfigId(configuration.masterConfigId)*.name
                      siblingConfigIds = Configuration.findAllByMasterConfigId(configuration.masterConfigId)*.id
                    configNames = [configuration.name]
                    masterChildConfigData.put(configuration.masterConfigId, ["configNames": configNames, "siblingConfigNames": siblingConfigNames])
                } else {
                    masterChildConfigData.get(configuration.masterConfigId)?.get("configNames").add(configuration.name)
                }
            }


        }

        Map<Integer,String> siblingConfigs=new HashMap<Integer,String>()
        siblingConfigIds.size().times {index ->
            siblingConfigs.put(siblingConfigIds.get(index),siblingConfigNames.get(index))
        }

        return ["CHILD_CONFIGS": childConfigs, "MASTER_CHILD_DATA": masterChildConfigData,"SIBLING_CONFIGS": siblingConfigs]

    }


    def getAllConfigurationsByAlertType(List<Long> configIdList, String alertType) {
        def configs = null
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            configs = Configuration.findAllByIdInList(configIdList)
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            configs = EvdasConfiguration.findAllByIdInList(configIdList)
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            configs = LiteratureConfiguration.findAllByIdInList(configIdList)
        }
        return configs
    }

    boolean isScheduledConfiguration(def configuration) {

        if (!(configuration.nextRunDate && configuration.scheduleDateJSON)) return false

        if (isRunOnceAlert(configuration) || isRunHourlyAlert(configuration)) return false

        Date futureNextRunDate = configurationService.getNextDate(configuration)
        return futureNextRunDate ? true : false

    }

    Boolean isRunOnceAlert(def configuration) {
        Boolean runOnce = true
        if (configuration.scheduleDateJSON) {
            if (JSON.parse(configuration.scheduleDateJSON).recurrencePattern) {
                runOnce = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.RUN_ONCE)
            }
        } else {
            runOnce = true
        }
        return runOnce
    }

    Boolean isRunHourlyAlert(def configuration) {
        Boolean runHourly = true
        if (configuration.scheduleDateJSON) {
            if (JSON.parse(configuration.scheduleDateJSON).recurrencePattern) {
                runHourly = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.HOURLY)
            }
        } else {
            runHourly = true
        }
        return runHourly
    }


    boolean isSafetyAlert(def configuration) {
        boolean isSafetyAlert = false
        if (configuration instanceof Configuration) {
            if (configuration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                isSafetyAlert = true
            } else if (configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && configuration.selectedDatasource?.contains(Constants.DataSource.PVA)) {
                isSafetyAlert = true
            }
        }
        return isSafetyAlert
    }

    void triggerBulkPausedAlertNotification(def pausedConfigurations, List<MasterConfiguration> pausedMasterConfigurations) {

        List pausedConfigurationsToBeNotified = filterConfigurationsToBeNotified(pausedConfigurations, pausedMasterConfigurations)

        if (!pausedConfigurationsToBeNotified.isEmpty()) {
            log.info("Triggering email for auto-paused alerts...")
            List<String> etlDisableReasons = [Constants.AlertDisableReason.ETL_AS_OF_VERSION_FAILURE, Constants.AlertDisableReason.ETL_FAILURE_CHECK_ENABLED, Constants.AlertDisableReason.ETL_IN_PROGRESS_CHECK_ENABLED]
            List<MissedAlertNotificationDTO> pvrPausedMissedAlertDTOList = new ArrayList<>()
            List<MissedAlertNotificationDTO> etlPausedMissedAlertDTOList = new ArrayList<>()

            List pvrPausedConfigurations = pausedConfigurationsToBeNotified.findAll { it.alertDisableReason == Constants.AlertDisableReason.PVR_INACCESSIBLE }
            List etlPausedConfigurations = pausedConfigurationsToBeNotified.findAll { it.alertDisableReason in etlDisableReasons }


            pvrPausedConfigurations.each { configuration ->
                MissedAlertNotificationDTO missedAlertInstance = new MissedAlertNotificationDTO(configuration)
                pvrPausedMissedAlertDTOList.add(missedAlertInstance)
            }
            etlPausedConfigurations.each { configuration ->
                MissedAlertNotificationDTO missedAlertInstance = new MissedAlertNotificationDTO(configuration)
                etlPausedMissedAlertDTOList.add(missedAlertInstance)
            }

            if (!pvrPausedMissedAlertDTOList.isEmpty()) {
                String emailSubject = "Alerts disabled due to PVR inaccessibility "
                log.info("${pvrPausedMissedAlertDTOList.size()} ${emailSubject}-->> ${pvrPausedMissedAlertDTOList*.getAlertName().toString()}")
                triggerMissedAlertNotification(pvrPausedMissedAlertDTOList, emailSubject, true)
            }

            if (!etlPausedMissedAlertDTOList.isEmpty()) {
                String emailSubject = "Alerts disabled due to ETL issue "
                log.info("${etlPausedMissedAlertDTOList.size()} ${emailSubject}-->> ${etlPausedMissedAlertDTOList*.getAlertName().toString()}")
                triggerMissedAlertNotification(etlPausedMissedAlertDTOList, emailSubject, false)
            }

            pausedConfigurationsToBeNotified.each { it ->
                it.autoPausedEmailTriggered = true
                CRUDService.updateWithoutAuditLog(it)
            }

        }

    }

    List filterConfigurationsToBeNotified(def pausedConfigurations, List<MasterConfiguration> pausedMasterConfigurations) {
        List pausedConfigurationsToBeNotified = new ArrayList<>()

        pausedConfigurations.each { String alertType, List<Object> configurations ->
            configurations.each { configuration ->
                if (configuration.autoPausedEmailTriggered == false) {
                    pausedConfigurationsToBeNotified.add(configuration)
                }
            }
        }
        pausedMasterConfigurations.each { configuration ->
            if (configuration.autoPausedEmailTriggered == false) {
                pausedConfigurationsToBeNotified.add(configuration)
            }
        }
        return pausedConfigurationsToBeNotified
    }

    void triggerMissedAlertNotification(List<MissedAlertNotificationDTO> missedAlertInstances, String emailSubject, boolean pausedDueToPvrIssue = true) {
        try {
            List<String> toAddressList = Holders.config.alertManagement.toAddressList
            emailService.sendMissedAlertNotificationEmail(['toAddress': toAddressList,
                                                           'title'    : emailSubject,
                                                           'map'      : ["missedAlertInstances": missedAlertInstances, "pausedDueToPvrIssue": pausedDueToPvrIssue]])
        } catch (Exception e) {
            log.error("Error sending Missed Alert Email Notification: ${e.message}", e)
            e.printStackTrace()
        }

    }


    def updatePreChecks(def params, Map customAuditProperties) {
        AlertPreExecutionCheck alertPreExecutionCheck = AlertPreExecutionCheck.first()
        if (alertPreExecutionCheck.isPvrCheckEnabled.toString() != params.pvrCheck || alertPreExecutionCheck.isVersionAsOfCheckEnabled.toString() != params.versionAsOf || alertPreExecutionCheck.isEtlFailureCheckEnabled.toString() != params.etlFailure || alertPreExecutionCheck.isEtlInProgressCheckEnabled.toString() != params.etlInProgress) {
            // Adding history of update to alertPreExecutionCheckHistory
            AlertPreExecutionCheckHistory alertPreExecutionCheckHistory = new AlertPreExecutionCheckHistory()

            alertPreExecutionCheckHistory.isPvrCheckFlagUpdated = alertPreExecutionCheck.isPvrCheckEnabled.toString() != params.pvrCheck
            alertPreExecutionCheckHistory.isVersionAsOfCheckUpdated = alertPreExecutionCheck.isVersionAsOfCheckEnabled.toString() != params.versionAsOf
            alertPreExecutionCheckHistory.isEtlFailureCheckUpdated = alertPreExecutionCheck.isEtlFailureCheckEnabled.toString() != params.etlFailure
            alertPreExecutionCheckHistory.isEtlInProgressCheckUpdated = alertPreExecutionCheck.isEtlInProgressCheckEnabled.toString() != params.etlInProgress

            alertPreExecutionCheckHistory.newPvrCheckValue = params.pvrCheck == "true"
            alertPreExecutionCheckHistory.newVersionAsOfCheckValue = params.versionAsOf == "true"
            alertPreExecutionCheckHistory.newEtlFailureCheckValue = params.etlFailure == "true"
            alertPreExecutionCheckHistory.newEtlInProgressCheckValue = params.etlInProgress == "true"
            alertPreExecutionCheckHistory.alertPreExecutionCheck = alertPreExecutionCheck
            CRUDService.save(alertPreExecutionCheckHistory)
            // Now saving alertPreExecutionCheck Values
            alertPreExecutionCheck.isPvrCheckEnabled = params.pvrCheck == "true"
            alertPreExecutionCheck.isVersionAsOfCheckEnabled = params.versionAsOf == "true"
            alertPreExecutionCheck.isEtlFailureCheckEnabled = params.etlFailure == "true"
            alertPreExecutionCheck.isEtlInProgressCheckEnabled = params.etlInProgress == "true"
        }
        alertPreExecutionCheck.lastUpdated = new Date()
        alertPreExecutionCheck.customAuditProperties = customAuditProperties
        CRUDService.save(alertPreExecutionCheck)
        alertPreExecutionCheck.toDto()
    }

    def updateAutoAdjustmentRules(def params) {
        Map customAuditProperties = [:]
        if (params['Single Case Alert']) {
            AutoAdjustmentRule autoAdjustmentRule = AutoAdjustmentRule.findByAlertType('Single Case Alert')
            if (autoAdjustmentRule.adjustmentTypeEnum.value() != params['Single Case Alert'] || autoAdjustmentRule.isEnabled.toString() != params['scaAutoRuleEnabled']) {
                // saving auto adjustment history
                AutoAdjustmentRuleHistory adjustmentRuleHistory = new AutoAdjustmentRuleHistory()
                adjustmentRuleHistory.isAdjustmentTypeEnumUpdated = autoAdjustmentRule.adjustmentTypeEnum.value() != params['Single Case Alert']
                adjustmentRuleHistory.oldAdjustmentTypeEnumValue = autoAdjustmentRule.adjustmentTypeEnum.value()
                adjustmentRuleHistory.newAdjustmentTypeEnumValue = params['Single Case Alert']
                adjustmentRuleHistory.isEnabledFlagUpdated = autoAdjustmentRule.isEnabled.toString() != params['scaAutoRuleEnabled']
                adjustmentRuleHistory.newIsEnabledFlagValue = params['scaAutoRuleEnabled'] == 'true'
                adjustmentRuleHistory.autoAdjustmentRule = autoAdjustmentRule
                CRUDService.save(adjustmentRuleHistory)
                // save and add history
                autoAdjustmentRule.adjustmentTypeEnum = AdjustmentTypeEnum.valueOf(params['Single Case Alert'] as String)
                autoAdjustmentRule.isEnabled = params['scaAutoRuleEnabled'] == 'true'
                CRUDService.save(autoAdjustmentRule)
                if(adjustmentRuleHistory.isAdjustmentTypeEnumUpdated){
                    customAuditProperties << ["Individual Case Alert(Rule)":["oldValue":adjustmentRuleHistory.oldAdjustmentTypeEnumValue, "newValue":adjustmentRuleHistory.newAdjustmentTypeEnumValue]]
                }
                if(adjustmentRuleHistory.isEnabledFlagUpdated){
                    customAuditProperties << ["Individual Case Alert(Enabled)":["oldValue":!adjustmentRuleHistory.newIsEnabledFlagValue, "newValue":adjustmentRuleHistory.newIsEnabledFlagValue]]
                }
            }
        }
        if (params['Aggregate Case Alert']) {
            AutoAdjustmentRule autoAdjustmentRule = AutoAdjustmentRule.findByAlertType('Aggregate Case Alert')
            if (autoAdjustmentRule.adjustmentTypeEnum.value() != params['Aggregate Case Alert'] || autoAdjustmentRule.isEnabled.toString() != params['aggAutoRuleEnabled']) {
                // saving auto adjustment history
                AutoAdjustmentRuleHistory adjustmentRuleHistory = new AutoAdjustmentRuleHistory()
                adjustmentRuleHistory.isAdjustmentTypeEnumUpdated = autoAdjustmentRule.adjustmentTypeEnum.value() != params['Aggregate Case Alert']
                adjustmentRuleHistory.oldAdjustmentTypeEnumValue = autoAdjustmentRule.adjustmentTypeEnum.value()
                adjustmentRuleHistory.newAdjustmentTypeEnumValue = params['Aggregate Case Alert']
                adjustmentRuleHistory.isEnabledFlagUpdated = autoAdjustmentRule.isEnabled.toString() != params['aggAutoRuleEnabled']
                adjustmentRuleHistory.newIsEnabledFlagValue = params['aggAutoRuleEnabled'] == 'true'
                adjustmentRuleHistory.autoAdjustmentRule = autoAdjustmentRule
                CRUDService.save(adjustmentRuleHistory)
                //save and add history
                autoAdjustmentRule.adjustmentTypeEnum = AdjustmentTypeEnum.valueOf(params['Aggregate Case Alert'] as String)
                autoAdjustmentRule.isEnabled = params['aggAutoRuleEnabled'] == 'true'
                CRUDService.save(autoAdjustmentRule)
                if(adjustmentRuleHistory.isAdjustmentTypeEnumUpdated){
                    customAuditProperties << ["Aggregate Alert(Rule)":["oldValue":adjustmentRuleHistory.oldAdjustmentTypeEnumValue, "newValue":adjustmentRuleHistory.newAdjustmentTypeEnumValue]]
                }
                if(adjustmentRuleHistory.isEnabledFlagUpdated){
                    customAuditProperties << ["Aggregate Alert(Enabled)":["oldValue":!adjustmentRuleHistory.newIsEnabledFlagValue, "newValue":adjustmentRuleHistory.newIsEnabledFlagValue]]
                }
            }
        }
        customAuditProperties
    }



    List listConfigurations(params) {
        User currentUser = userService.getUser()
        Long workflowGroupId = currentUser.workflowGroup.id
        List<Long> groupIds = currentUser.groups?.collect { it.id }
        def configurations
        Boolean isAggOrSingle = params.alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]
        String alertRunType = params.alertRunType
        String searchValue  = params["search[value]"]
        List<Long> userIds = User.findAllByUsernameLike("%${searchValue}%").collect{it.id}
        Closure criteria = {
            createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
            if (isAggOrSingle) {
                createAlias("autoShareWithUser", "autoShareWithUser", JoinType.LEFT_OUTER_JOIN)
                createAlias("autoShareWithGroup", "autoShareWithGroup", JoinType.LEFT_OUTER_JOIN)
            }
            eq("isDeleted", false)
            if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) && (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION"))) {
                ilike('selectedDatasource', "%${Constants.DataSource.FAERS}%")
            } else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) && (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION"))) {
                ilike('selectedDatasource', "%${Constants.DataSource.VAERS}%")
            } else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) && (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION"))) {
                ilike('selectedDatasource', "%${Constants.DataSource.VIGIBASE}%")
            } else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) && (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION"))) {
                ilike('selectedDatasource', "%${Constants.DataSource.JADER}%")
            } else if (params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT) && (SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION"))) {
                ilike('selectedDatasource', "%${Constants.DataSource.PVA}%")
            }

            'or' {
                and {
                    eq("workflowGroup.id", workflowGroupId)
                    or {
                        eq("shareWithUser.id", currentUser.id)
                        if (isAggOrSingle) {
                            eq("autoShareWithUser.id", currentUser.id)
                        }
                        if (groupIds) {
                            or {
                                groupIds.collate(1000).each {
                                    'in'("shareWithGroup.id", groupIds)
                                }
                            }
                            if (isAggOrSingle) {
                                or {
                                    groupIds.collate(1000).each {
                                        'in'("autoShareWithGroup.id", groupIds)
                                    }
                                }
                            }
                        }
                    }
                }
                eq('owner.id', currentUser.id)
            }
            if ((!params.alertType.equals(Constants.AlertConfigType.EVDAS_ALERT))&& (!params.alertType.equals(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT))) {
                isNull("masterTemplateId")
                eq("type", params.alertType)
                eq("isCaseSeries", false)
                eq('adhocRun',false)
            }
            alertAdministrativeSearch.delegate = delegate
            alertAdministrativeSearch(searchValue,params.alertType,userIds)

                order("numOfExecutions", "desc")


        }

        if (params.alertType.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT) || params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
            configurations = Configuration.createCriteria().list(criteria) as List<Configuration>
            configurations = configurations.unique {
                [it.name, it.type, it.owner]
            }
        } else if (params.alertType.equals(Constants.AlertConfigType.EVDAS_ALERT)) {
            configurations = EvdasConfiguration.createCriteria().list {
                executedConfigForViewAlert.delegate = delegate
                executedConfigForViewAlert(workflowGroupId, currentUser.id, groupIds)
                isNull('integratedConfigurationId')
                eq('adhocRun',false)
                alertAdministrativeSearch.delegate = delegate
                alertAdministrativeSearch(searchValue,Constants.AlertConfigType.EVDAS_ALERT,userIds)
            }
            configurations = configurations.unique {
                [it.name, it.owner]
            }
        } else if (params.alertType.equals(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)) {
            configurations = LiteratureConfiguration.createCriteria().list {
                executedConfigForViewAlert.delegate = delegate
                executedConfigForViewAlert(workflowGroupId, currentUser.id, groupIds)
                alertAdministrativeSearch.delegate = delegate
                alertAdministrativeSearch(searchValue,Constants.AlertConfigType.LITERATURE_SEARCH_ALERT,userIds)
            }
            configurations = configurations.unique {
                [it.name, it.owner]
            }
        }

        if (configurations) {
            filterAlerts(configurations, alertRunType)
        }

        return configurations
    }

    Closure alertAdministrativeSearch = {String searchValue, String alertType, List userIds ->
        if(searchValue) {
            or {
                ilike("name", "%${searchValue}%")
                ilike("productGroupSelection", "%${searchValue}%")
                ilike("productSelection", "%${searchValue}%")
                if(alertType!=Constants.AlertConfigType.LITERATURE_SEARCH_ALERT){
                    ilike("description","%${searchValue}%")
                }
                if(userIds.size()!=0) {
                    'in'('owner.id',userIds)
                }
                if((alertType!=Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) && (alertType!=Constants.AlertConfigType.EVDAS_ALERT)){
                    if(Constants.DataSource.DATASOURCE_SAFETY_DB.contains(searchValue.toUpperCase())){
                        ilike("selectedDatasource","%pva%")
                    }
                    ilike("selectedDatasource","%${searchValue.toLowerCase()}%")
                }

            }
        }
    }


    Closure executedConfigForViewAlert = { Long workflowGroupId, Long userId, List<Long> groupIds ->
        createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
        createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
        eq("isDeleted", false)
        'or' {
            and {
                eq("workflowGroup.id", workflowGroupId)
                or {
                    eq("shareWithUser.id", userId)
                    if (groupIds) {
                        or {
                            groupIds.collate(1000).each {
                                'in'("shareWithGroup.id", groupIds)
                            }
                        }
                    }
                }
            }
            eq('owner.id', userId)
        }
    }

    void filterAlerts(def configurations, String alertRunType) {
        def date = new Date()
        if (alertRunType == Constants.AlertStatus.SCHEDULED) {
            def unScheduledConfigurations = []
            configurations.each { def configuration ->
                Boolean isRunOnceAlert = isRunOnceAlert(configuration)
                if ((isRunOnceAlert && date>configuration.nextRunDate) || !configuration.isEnabled) unScheduledConfigurations.add(configuration)
            }
            if (!unScheduledConfigurations.isEmpty()) {
                configurations.removeAll(unScheduledConfigurations)
            }

        } else if (alertRunType == Constants.AlertStatus.UNSCHEDULED) {
            def scheduledConfigurations = []
            configurations.each { def configuration ->
                Boolean isRunOnceAlert = isRunOnceAlert(configuration)
                if ((!isRunOnceAlert || date<configuration.nextRunDate) && configuration.isEnabled) scheduledConfigurations.add(configuration)
            }
            if (!scheduledConfigurations.isEmpty()) {
                configurations.removeAll(scheduledConfigurations)
            }
        }
    }

    Boolean isScheduledConfig(Configuration configuration){
        def date = new Date()
        def scheduledConfigurations = []
        Boolean isRunOnceAlert = isRunOnceAlert(configuration)
        if ((!isRunOnceAlert || date < configuration.nextRunDate) && configuration.isEnabled) return true;
    }

    Boolean isConfigurationScheduledGeneric(def executedConfiguration){
        def date = new Date()
        switch (executedConfiguration.class){
            case ExecutedConfiguration.class:
                Configuration config = Configuration.get(executedConfiguration.configId)
                Boolean isRunOnceAlert = isRunOnceAlert(config)
                if (!isRunOnceAlert || date < config.nextRunDate) return true
                break
            case ExecutedLiteratureConfiguration.class:
                LiteratureConfiguration config = LiteratureConfiguration.get(executedConfiguration.configId)
                Boolean isRunOnceAlert = isRunOnceAlert(config)
                if (!isRunOnceAlert || date < config.nextRunDate) return true
                break
            case ExecutedEvdasConfiguration.class:
                EvdasConfiguration config = EvdasConfiguration.get(executedConfiguration.configId)
                Boolean isRunOnceAlert = isRunOnceAlert(config)
                if (!isRunOnceAlert || date < config.nextRunDate) return true
                break
            default:
                return false
                break
        }
        return false
    }
}
