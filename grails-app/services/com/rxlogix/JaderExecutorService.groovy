package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.enums.SkippedAlertStateEnum
import com.rxlogix.helper.LinkHelper
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import groovy.sql.Sql


class JaderExecutorService implements LinkHelper, AlertUtil {

    static transactional = false
    def dataSource_jader
    def grailsApplication
    def skippedAlertService
    def reportExecutorService
    def alertService
    List<Long> currentlyJaderRunning = []

    def startJaderAlertExecutionByLevel(ExecutionStatus executionStatus){
        Configuration configurationJader = Configuration.get(executionStatus.configId)
        String dataSource = configurationJader.selectedDatasource
        ExecutedConfiguration executedJaderConfiguration
        Map<String, Long> otherDataSourcesExecIds = [:]
        boolean isFreshRun = false
        boolean isException = false

        if (executionStatus.executedConfigId) {
            executedJaderConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
            if (executedJaderConfiguration?.adjustmentTypeEnum) {
                skippedAlertService.updateSkippedAlertsState(executedJaderConfiguration.id, SkippedAlertStateEnum.EXECUTING)
            }
        }
        try{
            switch (executionStatus.executionLevel) {
                case 0:
                    executedJaderConfiguration = reportExecutorService.createExecutedConfigurationForAlert(configurationJader)
                    reportExecutorService.updateExecutionStatus(executionStatus, executedJaderConfiguration)
                case 1:
                    List alertData = []
                    if(configurationJader.isResume) {
                        if (reportExecutorService.isDataSourceRequiredForFreshRun(executionStatus, dataSource)) {
                            reportExecutorService.clearDataMiningTablesOnResume(executedJaderConfiguration.id, dataSource)
                        }
                    }
                    alertData = reportExecutorService.fetchQuantAlertDataFromMart(executionStatus, configurationJader, executedJaderConfiguration, dataSource)
                    String fileName = "${grailsApplication.config.signal.alert.file}/${executedJaderConfiguration.id}_${executedJaderConfiguration.type}"
                    if(!configurationJader.adhocRun) {
                         fileName = fileName + "_${dataSource}"
                    }
                    alertService.saveAlertDataInFile(alertData, fileName)
                    log.info("after finishing alert data in file.")
                    log.info("before updating log level "+executionStatus.executionLevel)
                    reportExecutorService.updateExecutionStatusLevel(executionStatus)
                case 2:
                    if (!executedJaderConfiguration) {
                        executedJaderConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
                    }
                    if (executedJaderConfiguration)
                        otherDataSourcesExecIds.put(dataSource, executedJaderConfiguration.id)
            }
            switch (executionStatus.executionLevel) {
                case 2:
                    reportExecutorService.saveQuantData(configurationJader,executedJaderConfiguration,executionStatus.alertFilePath,otherDataSourcesExecIds,executionStatus)
                    reportExecutorService.setValuesForConfiguration(configurationJader.id,executedJaderConfiguration.id,executionStatus.id)
                    break
            }
        }catch(Throwable throwable){
            log.error("Exception while running the Jader quantitative alert", throwable)
            isException = true
            throw throwable
        }finally{
            if(configurationJader && (currentlyJaderRunning.contains(configurationJader.id) || executedJaderConfiguration)){
                currentlyJaderRunning.remove(configurationJader.id)
                if(!isException){
                    reportExecutorService.clearDataMiningTables(configurationJader.id,executedJaderConfiguration.id,Constants.DataSource.JADER)
                }
            }
        }

    }
    int getJaderExecutionQueueSize() {
        return currentlyJaderRunning.size()
    }
    def getJaderDateRange() {
        String date
        String startDate
        String endDate
        String jaderDate
        Sql jaderSql
        try {
            jaderSql = new Sql(dataSource_jader)
            String jader_statement = SignalQueryHelper.jader_date_range()
            jaderSql.eachRow(jader_statement, []) { resultSetObj ->
                date = resultSetObj
            }
            int length = date?.size()
            String year = date?.substring(length - 5, length - 1)
            if (date?.contains('MAR')) {
                startDate = '01-01-' + year
                endDate = '31-03-' + year
                jaderDate = '01-Jan-' + year + ' - 31-Mar-' + year
            } else if (date?.contains('JUN')) {
                startDate = '01-04-' + year
                endDate = '30-06-' + year
                jaderDate = '01-Apr-' + year + ' - 30-Jun-' + year
            } else if (date?.contains('SEP')) {
                startDate = '01-07-' + year
                endDate = '30-09-' + year
                jaderDate = '01-Jul-' + year + ' - 30-Sep-' + year
            } else if (date?.contains('DEC')) {
                startDate = '01-10-' + year
                endDate = '31-12-' + year
                jaderDate = '01-Oct-' + year + ' - 31-Dec-' + year
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            jaderSql?.close()
        }


        return [startDate: startDate, endDate: endDate, jaderDate: jaderDate]
    }
}
