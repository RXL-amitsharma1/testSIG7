package com.rxlogix

import com.rxlogix.config.AlertProgressExecutionType
import com.rxlogix.config.AppAlertProgressStatus
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.enums.AlertType
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.apache.commons.lang3.StringUtils
import org.springframework.transaction.annotation.Propagation

import java.sql.ResultSet

class AppAlertProgressStatusService {

    def signalDataSourceService

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    AppAlertProgressStatus createAppAlertProgressStatus(ExecutionStatus executionStatus, Long executedConfigId, String name, Integer progressStatus, Integer finalStatus, Long startTime, AlertProgressExecutionType type) {
        AppAlertProgressStatus appAlertProgressStatus = null
        try {
            if (Objects.nonNull(executionStatus) && null != executedConfigId && StringUtils.isNotEmpty(name)) {
                appAlertProgressStatus = AppAlertProgressStatus.findByExStatusIdAndExecutedConfigIdAndName(executionStatus.id, executedConfigId, name)
                if (Objects.isNull(appAlertProgressStatus)) {
                    appAlertProgressStatus = new AppAlertProgressStatus(
                            exStatusId: executionStatus.id,
                            executedConfigId: executedConfigId,
                            name: name,
                            progressStatus: progressStatus,
                            finalStatus: finalStatus,
                            startTime: startTime,
                            type: type.toString())
                } else {
                    appAlertProgressStatus.progressStatus = progressStatus
                    appAlertProgressStatus.finalStatus = finalStatus
                    appAlertProgressStatus.startTime = startTime
                    appAlertProgressStatus.endTime = 0L
                }
                appAlertProgressStatus.save()
            }
        }catch(Exception ex){
            log.error("Error while saving alert progress")
            ex.printStackTrace()
        }

        return appAlertProgressStatus
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateAppAlertProgressStatus(Long exStatusId, Long exConfigId, String name, Integer progressStatus, Integer finalStatus, Long endTime) {
        AppAlertProgressStatus appAlertProgressStatus = AppAlertProgressStatus.findByExStatusIdAndExecutedConfigIdAndName(exStatusId, exConfigId, name)
        if (Objects.nonNull(appAlertProgressStatus) && appAlertProgressStatus.finalStatus != 1) {
            if (Objects.nonNull(progressStatus)) {
                appAlertProgressStatus.progressStatus = progressStatus
            }
            appAlertProgressStatus.finalStatus = finalStatus
            if (null != endTime && endTime != 0L) {
                appAlertProgressStatus.endTime = endTime
            }
            appAlertProgressStatus.save()
            log.info("App alert progress status updated.")
        }
    }

    float calculateAlertProgress(Long executionStatusId, Integer level) {
        float progressResult = 0.0
        String[] steps = [Constants.AlertProgress.BUSINESS_RULES, Constants.AlertProgress.PERSIST, Constants.AlertProgress.ARCHIEVE]
        ExecutionStatus executionStatus=ExecutionStatus.get(executionStatusId)
        Integer executionLevel = executionStatus?.executionLevel
        Long configId = executionStatus?.configId
        String type=executionStatus?.type

        if (Objects.nonNull(executionLevel)) {
            if (level != -1) {
                executionLevel = level
            }
            if (Constants.AlertProgress.SINGLE_CASE_ALERT.equals(type)) {
                Configuration configuration = Configuration.findById(configId)
                if (Objects.nonNull(configuration)) {
                    switch (executionLevel) {
                        case 0:
                            progressResult = 0.0
                            break
                        case 1:
                            progressResult = 10.0
                            break
                        case 2:
                            String dataSource = configuration.selectedDatasource
                            progressResult = progressResult + calculateProgress(executionStatusId, dataSource, 1, 2, Constants.AlertProgress.SINGLE_CASE_ALERT, progressResult)
                            break
                        case 3:
                            steps.each { step ->
                                progressResult = calculateProgress(executionStatusId, step, 1, 3, Constants.AlertProgress.SINGLE_CASE_ALERT, progressResult)
                            }
                            break
                        default:
                            progressResult
                    }
                }
            }
            if (Constants.AlertProgress.AGGREGATE_CASE_ALERT.equals(type)) {
                Configuration configuration = Configuration.findById(configId)
                if (Objects.nonNull(configuration)) {
                    switch (executionLevel) {
                        case 0:
                            progressResult = 0.0
                            break
                        case 1:
                            String[] dataSources = configuration.selectedDatasource?.split(Constants.AlertProgress.COMMA)
                            List<Integer> progressResults = new ArrayList<>()
                            dataSources.each { dataSource ->
                                progressResults.add(calculateProgress(executionStatusId, dataSource, dataSources.size(), 1, Constants.AlertProgress.AGGREGATE_CASE_ALERT, progressResult))
                            }
                            progressResult = 10.0
                            for (Integer i : progressResults) {
                                progressResult += i
                            }
                            progressResults = null
                            break
                        case 2:
                            steps.each { step ->
                                progressResult = calculateProgress(executionStatusId, step, 1, 2, Constants.AlertProgress.AGGREGATE_CASE_ALERT, progressResult)
                            }
                            break
                        default:
                            progressResult
                    }
                }

            }
        }
        progressResult
    }

    float calculateProgress(Long executionStatusId, String name, int size, int executionLevel, String executionType, float progress) {
        AppAlertProgressStatus appAlertProgressStatus = AppAlertProgressStatus.findByExStatusIdAndName(executionStatusId, name)
        if (Objects.nonNull(appAlertProgressStatus)) {
            if (Constants.AlertProgress.SINGLE_CASE_ALERT.equals(executionType)) {
                switch (executionLevel) {
                    case 2:
                        if (appAlertProgressStatus.finalStatus == 3)
                            progress = 60.0
                        else
                            progress = 20.0
                        break
                    case 3:
                        if (Constants.AlertProgress.BUSINESS_RULES.equals(name)) {
                            if (appAlertProgressStatus.finalStatus == 3)
                                progress = 70.0
                            else
                                progress = 60.0
                            break
                        }
                        if (Constants.AlertProgress.PERSIST.equals(name)) {
                            if (appAlertProgressStatus.finalStatus == 3)
                                progress = 90.0
                            else
                                progress = 70.0
                            break
                        }
                        if (Constants.AlertProgress.ARCHIEVE.equals(name)) {
                            if (appAlertProgressStatus.finalStatus == 3)
                                progress = 100.0
                            else
                                progress = 90.0
                            break
                        }
                        break
                }
            }
            if (Constants.AlertProgress.AGGREGATE_CASE_ALERT.equals(executionType)) {
                switch (executionLevel) {
                    case 1:
                        progress = (((appAlertProgressStatus.progressStatus * 50) / size) / 100)
                        break
                    case 2:
                        if (Constants.AlertProgress.BUSINESS_RULES.equals(name)) {
                            if (appAlertProgressStatus.finalStatus == 3)
                                progress = 70.0
                            else
                                progress = 60.0
                            break
                        }
                        if (Constants.AlertProgress.PERSIST.equals(name)) {
                            if (appAlertProgressStatus.finalStatus == 3)
                                progress = 90.0
                            else
                                progress = 70.0
                            break
                        }
                        if (Constants.AlertProgress.ARCHIEVE.equals(name)) {
                            if (appAlertProgressStatus.finalStatus == 3)
                                progress = 100.0
                            else
                                progress = 90.0
                            break
                        }
                        break
                }
            }
        }
        if (Constants.AlertProgress.BUSINESS_RULES.equals(name) && progress == 0.0) {
            progress = 60.0
        }
        return progress
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateAppAlertProgressStatusWithOutExStatusId(Long exConfigId, String name, Integer progressStatus, Integer finalStatus, Long endTime) {
        AppAlertProgressStatus appAlertProgressStatus = AppAlertProgressStatus.findByExecutedConfigIdAndName(exConfigId, name)
        if (Objects.nonNull(appAlertProgressStatus) && appAlertProgressStatus.finalStatus != 3) {
            appAlertProgressStatus.progressStatus = progressStatus
            appAlertProgressStatus.finalStatus = finalStatus
            if (null != endTime && endTime != 0L) {
                appAlertProgressStatus.endTime = endTime
            }
            appAlertProgressStatus.save()
            log.info("App alert progress status updated.")
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void deleteAppAlertProgressStatus(Long exStatusId) {
        List<AppAlertProgressStatus> appAlertProgressStatusList = AppAlertProgressStatus.findAllByExStatusId(exStatusId)
        if (Objects.nonNull(appAlertProgressStatusList) && !appAlertProgressStatusList.isEmpty()) {
            AppAlertProgressStatus.deleteAll(appAlertProgressStatusList)
            log.info("All successfully completed Alerts deleted from AppAlertProgressStatus table.")
        }
    }
}
