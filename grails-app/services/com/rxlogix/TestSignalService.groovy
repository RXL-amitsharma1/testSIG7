package com.rxlogix

import com.rxlogix.config.AlertType
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.dto.TestCaseDTO
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.Sql

class TestSignalService {
    def dataSource
    def configurationService
    def userService
    def alertService

    def serviceMethod() {

    }
    /**
     *
     * @param selectedDataList
     * @return testCaseDTOList
     */
    List<TestCaseDTO> createTestCaseDTO(List selectedDataList) {


        List<TestCaseDTO> testCaseDTOList = new LinkedList<>()
        selectedDataList.each { selectedData ->
            TestCaseDTO testCaseDTO = new TestCaseDTO()
            testCaseDTO.setAlertType(selectedData.alertType)
            testCaseDTO.setAssignedTo(selectedData.assignedTo)
            testCaseDTO.setxForDateRange(selectedData.xForDateRange)
            testCaseDTO.setOwner(selectedData.owner)
            testCaseDTO.setProducts(selectedData.products)
            testCaseDTO.setPriority(selectedData.priority)
            testCaseDTO.setDateRangeType(selectedData.dateRangeType)
            testCaseDTO.setStartDate(selectedData.startDate)
            testCaseDTO.setEndDate(selectedData.endDate)
            testCaseDTO.setVersionAsOfDate(selectedData.versionAsOfDate)
            testCaseDTO.setShareWith(selectedData.shareWith)
            testCaseDTO.setScheduler(selectedData.scheduler)
            testCaseDTO.setDataSource(selectedData.dataSource)
            testCaseDTO.setIsAdhoc(selectedData.isAdhoc)
            testCaseDTO.setIsExcludeFollowUp(selectedData.isExcludeFollowUp)
            testCaseDTO.setIsDataMiningSMQ(selectedData.isDataMiningSMQ)
            testCaseDTO.setIsExcludeNonValidCases(selectedData.isExcludeNonValidCases)
            testCaseDTO.setIsIncludeMissingCases(selectedData.isIncludeMissingCases)
            testCaseDTO.setIsApplyAlertStopList(selectedData.isApplyAlertStopList)
            testCaseDTO.setIsIncludeMedicallyConfirmedCases(selectedData.isIncludeMedicallyConfirmedCases)
            testCaseDTO.setDateRange(selectedData.dateRange)
            testCaseDTO.setEvaluateCaseDateOn(selectedData.evaluateCaseDateOn)
            testCaseDTO.setDrugType(selectedData.drugType)
            testCaseDTO.setLimitCaseSeries(selectedData.limitCaseSeries)
            testCaseDTOList.add(testCaseDTO)
        }
        return testCaseDTOList

    }

    /**
     *
     * @param alertType
     * @return typeHQL
     */

    String getAlertTypeStringForExecutionStatus(AlertType alertType) {
        String typeHQL = ''
        if (alertType == AlertType.SINGLE_CASE_ALERT || alertType == AlertType.AGGREGATE_CASE_ALERT) {
            if (alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION"))) {
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%faers%'"

            } else if (alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION"))) {
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%vaers%'"

            } else if (alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION"))) {
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%vigibase%'"

            } else if (alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION"))) {
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%pva%'"

            } else {
                typeHQL = "and config.type = ec.type"
            }
        }
        typeHQL
    }
    /**
     *
     * @param executionStatusDTO
     * @param searchHQL
     * @return
     */
    String prepareCountHqlForPassedTestCases(ExecutionStatusDTO executionStatusDTO, String searchHQL) {
        String hqlForConfig = """
               Select count(*) from ExecutionStatus ec,${executionStatusDTO.configurationDomain.getSimpleName()} config 
                 where config.name = ec.name
                 and config.isDeleted = 0
                 and config.workflowGroup.id = ${executionStatusDTO.workflowGroupId}
                 and ec.executionStatus = '${executionStatusDTO.executionStatus}'
                 ${searchHQL}
               """
        hqlForConfig
    }

    String getIsAdhocHql(Boolean isAdhoc) {
        if (isAdhoc) {
            return "and config.adhocRun = 1"
        } else {
            return "and config.adhocRun = 0"
        }
    }
    /**
     *
     * @param executionStatusDTO
     * @param searchHQL
     * @param isAdhoc
     * @return
     */
    String prepareCountHqlForTestCases(ExecutionStatusDTO executionStatusDTO, String searchHQL, Boolean isAdhoc = false) {
        String typeHQL = getAlertTypeStringForExecutionStatus(executionStatusDTO.alertType)
        String isAdhocHQL = getIsAdhocHql(isAdhoc)
        String hqlForConfig = """
               Select count(*) from ExecutionStatus ec,${executionStatusDTO.configurationDomain.getSimpleName()} config 
                 where config.name = ec.name 
                 ${typeHQL}
                 and ec.type = '${executionStatusDTO.alertType.value()}'
                 and config.isDeleted = 0
                 ${isAdhocHQL}
                 and config.workflowGroup.id = ${executionStatusDTO.workflowGroupId}
                 and ec.executionStatus = '${executionStatusDTO.executionStatus}'
                 ${searchHQL}
               """
        hqlForConfig
    }
    /**
     *
     * @param executionStatusDTO
     * @param searchHQL
     * @return
     */
    String prepareCountHqlForAllTestCases(ExecutionStatusDTO executionStatusDTO, String searchHQL) {
        String hqlForConfig = """
               Select count(*) from ExecutionStatus ec,${executionStatusDTO.configurationDomain.getSimpleName()} config 
                 where config.name = ec.name
                 and config.isDeleted = 0
                 and config.workflowGroup.id = ${executionStatusDTO.workflowGroupId}
                 ${searchHQL}
               """
        hqlForConfig
    }
    /**
     *
     * @param executionStatusDTO
     * @param searchHQL
     * @param isAdhoc
     * @return
     */
    String prepareCountHqlForTotalTestCases(ExecutionStatusDTO executionStatusDTO, String searchHQL, Boolean isAdhoc = false) {
        String typeHQL = getAlertTypeStringForExecutionStatus(executionStatusDTO.alertType)
        String isAdhocHQL = getIsAdhocHql(isAdhoc)
        String hqlForConfig = """
               Select count(*) from ExecutionStatus ec,${executionStatusDTO.configurationDomain.getSimpleName()} config 
                 where config.name = ec.name 
                 ${typeHQL}
                 and ec.type = '${executionStatusDTO.alertType.value()}'
                 ${isAdhocHQL}
                 and config.isDeleted = 0
                 and config.workflowGroup.id = ${executionStatusDTO.workflowGroupId}
                 ${searchHQL}
               """
        hqlForConfig
    }
    /**
     *
     * @param searchString
     * @return
     */
    String getSearchStringForExecutionStatus(String searchString) {
        String searchHQL = ''
        if (searchString) {
            searchHQL = "AND( lower(ec.name) like lower('%${searchString}%') OR lower(ec.owner.fullName) like lower('%${searchString}%') )"
        }
        searchHQL
    }

    /**
     *
     * @param executionStatusDTO
     * @return
     */
    Map getCountOfPassedTestCases(ExecutionStatusDTO executionStatusDTO) {

        log.info("Getting count of passed test cases")

        String searchHQL = getSearchStringForExecutionStatus(executionStatusDTO.searchString)

        Integer countOfPassedTestCases = ExecutionStatus.executeQuery(prepareCountHqlForPassedTestCases(executionStatusDTO, searchHQL))[0] as Integer

        Integer countOfTotatTestCases = ExecutionStatus.executeQuery(prepareCountHqlForAllTestCases(executionStatusDTO, searchHQL))[0] as Integer

        return [countOfPassedTestCases: countOfPassedTestCases, countOfTotatTestCases: countOfTotatTestCases]

    }

    /**
     *
     * @param executionStatusDTO
     * @param reportExecutionStatus
     * @return
     */
    Map getCountForChart(ExecutionStatusDTO executionStatusDTO, ReportExecutionStatus reportExecutionStatus = ReportExecutionStatus.ERROR) {

        log.info("Get count for chart with execution status:" + reportExecutionStatus.value())

        String searchHQL = getSearchStringForExecutionStatus(executionStatusDTO.searchString)
        executionStatusDTO.executionStatus = reportExecutionStatus
        Integer aggAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTestCases(executionStatusDTO, searchHQL))[0] as Integer
        Integer aggAdhocAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTestCases(executionStatusDTO, searchHQL, true))[0] as Integer

        executionStatusDTO.alertType = AlertType.SINGLE_CASE_ALERT
        Integer icrAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTestCases(executionStatusDTO, searchHQL))[0] as Integer
        Integer icrAdhocAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTestCases(executionStatusDTO, searchHQL, true))[0] as Integer

        executionStatusDTO.alertType = AlertType.EVDAS_ALERT
        Integer evdasAlert = ExecutionStatus.executeQuery(prepareCountHqlForTestCases(executionStatusDTO, searchHQL))[0] as Integer

        return ["aggAlerts": aggAlerts, "icrAlerts": icrAlerts, "evdasAlert": evdasAlert, "aggAdhocAlerts": aggAdhocAlerts, "icrAdhocAlerts": icrAdhocAlerts]
    }
    /**
     *
     * @param executionStatusDTO
     * @return
     */
    Map getTotalCountForChart(ExecutionStatusDTO executionStatusDTO) {

        log.info("Getting total count for chart")

        String searchHQL = getSearchStringForExecutionStatus(executionStatusDTO.searchString)

        Integer aggAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTotalTestCases(executionStatusDTO, searchHQL))[0] as Integer
        Integer aggAdhocAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTotalTestCases(executionStatusDTO, searchHQL, true))[0] as Integer

        executionStatusDTO.alertType = AlertType.SINGLE_CASE_ALERT
        Integer icrAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTotalTestCases(executionStatusDTO, searchHQL))[0] as Integer
        Integer icrAdhocAlerts = ExecutionStatus.executeQuery(prepareCountHqlForTotalTestCases(executionStatusDTO, searchHQL, true))[0] as Integer

        executionStatusDTO.alertType = AlertType.EVDAS_ALERT
        Integer evdasAlert = ExecutionStatus.executeQuery(prepareCountHqlForTotalTestCases(executionStatusDTO, searchHQL))[0] as Integer

        return ["aggAlerts": aggAlerts, "icrAlerts": icrAlerts, "evdasAlert": evdasAlert, "aggAdhocAlerts": aggAdhocAlerts, "icrAdhocAlerts": icrAdhocAlerts]
    }

    def deleteTestAggAlerts() {
        log.info("Deleting Test alerts")
        Sql sql = new Sql(dataSource)
        def rows = sql.rows("select id, config_id, name from ex_rconfig where name like '%SmokeTestingPVSignalDev%'")
        def exeConfigList = rows.collect { it.id }.join(",")
        def configIdList = rows.collect { it.config_id }.join(",")
        try {
            if (rows) {
                sql.execute("delete from ex_rconfig_activities where ex_config_activities_id in (" + exeConfigList.replace("'", "") + ")")
                sql.execute("delete from agg_alert where exec_configuration_id in (" + exeConfigList.replace("'", "") + ")")
                sql.execute("delete from ex_rconfig where id in (" + exeConfigList.replace("'", "") + ")")
                sql.execute("delete from rconfig where id in (" + configIdList.replace("'", "") + ")")
                sql.execute("delete from ex_status where executed_config_id in (" + exeConfigList.replace("'", "") + ")")
                sql.execute("delete from inbox_log where executed_config_id in (" + exeConfigList.replace("'", "") + ")")
                //alertService.restoreDashboardCounts()
            }
        } catch (Exception ex) {
            log.error("Error occoured while deleting test alerts")
            ex.printStackTrace()
        } finally {
            sql.close()
        }
        return rows?.collect { it.name }?.join(",")
    }

    Map getTestAlertExecutionStatusData() {

        ExecutionStatusDTO executionStatusDTO = createExecutionStatusDTO()
        Map aggAlertData = getExecutionStatusDataForAlerts(executionStatusDTO, AlertType.AGGREGATE_CASE_ALERT)
        Map icrAlertData = getExecutionStatusDataForAlerts(executionStatusDTO, AlertType.SINGLE_CASE_ALERT)


        Map alertDataMap = [aaData: aggAlertData.aaData + icrAlertData.aaData, recordsTotal: aggAlertData.recordsTotal + icrAlertData.recordsTotal, recordsFilteredCount: aggAlertData.recordsFilteredCount + icrAlertData.recordsFilteredCount]

        return alertDataMap
    }

    ExecutionStatusDTO createExecutionStatusDTO() {
        log.info("Creating ExecutionStatusDTO")
        User user = userService.getUser()
        ExecutionStatusDTO executionStatusDTO = new ExecutionStatusDTO()
        executionStatusDTO.alertType = AlertType.AGGREGATE_CASE_ALERT
        executionStatusDTO.currentUser = user
        executionStatusDTO.max = 100
        executionStatusDTO.offset = 0
        executionStatusDTO.sort = Constants.SignalAutomation.SORT
        executionStatusDTO.direction = Constants.SignalAutomation.DIRECTION_ASC
        executionStatusDTO.workflowGroupId = user?.workflowGroup?.id
        executionStatusDTO.configurationDomain = Configuration
        executionStatusDTO.searchString = Constants.SignalAutomation.SMOKE_TESTING
        executionStatusDTO
    }

    /**
     *
     * @param executionStatusDTO
     * @param alertType
     * @return
     */
    def getExecutionStatusDataForAlerts(ExecutionStatusDTO executionStatusDTO, AlertType alertType) {

        log.info("Getting execution status data for alert type: " + alertType.value())

        Map result = [:]
        Map resultMap = [:]
        Integer recordsTotal = 0
        Integer recordsFilteredCount = 0
        List alertList = new ArrayList()

        try {
            executionStatusDTO.alertType = alertType

            executionStatusDTO.executionStatus = ReportExecutionStatus.SCHEDULED
            result = configurationService.showExecutionsScheduled(executionStatusDTO)
            alertList += result.aaData
            recordsTotal += result.recordsTotal ?: 0
            recordsFilteredCount += result.recordsFilteredCount ?: 0
            resultMap.put(ReportExecutionStatus.SCHEDULED, result)

            executionStatusDTO = createExecutionStatusDTO()
            executionStatusDTO.executionStatus = ReportExecutionStatus.COMPLETED
            executionStatusDTO.alertType = alertType
            result = configurationService.generateMapForTestAlertExecutionStatus(executionStatusDTO)
            alertList += result.aaData
            recordsTotal += result.recordsTotal ?: 0
            recordsFilteredCount += result.recordsFilteredCount ?: 0
            resultMap.put(ReportExecutionStatus.COMPLETED, result)

            executionStatusDTO = createExecutionStatusDTO()
            executionStatusDTO.executionStatus = ReportExecutionStatus.ERROR
            executionStatusDTO.alertType = alertType
            result = configurationService.generateMapForTestAlertExecutionStatus(executionStatusDTO)
            alertList += result.aaData
            recordsTotal += result.recordsTotal ?: 0
            recordsFilteredCount += result.recordsFilteredCount ?: 0
            resultMap.put(ReportExecutionStatus.ERROR, result)

            executionStatusDTO = createExecutionStatusDTO()
            executionStatusDTO.executionStatus = ReportExecutionStatus.GENERATING
            executionStatusDTO.alertType = alertType
            result = configurationService.generateMapForTestAlertExecutionStatus(executionStatusDTO)
            alertList += result.aaData
            recordsTotal += result.recordsTotal ?: 0
            recordsFilteredCount += result.recordsFilteredCount ?: 0
            resultMap.put(ReportExecutionStatus.GENERATING, result)
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        Map alertDataMap = [aaData: alertList ?: [], recordsTotal: recordsTotal ?: 0, recordsFilteredCount: recordsFilteredCount ?: 0]
        return alertDataMap
    }

    def prepareDataForChart(Map passedCountMap, Map failedCountMap) {

        log.info("Prepare data for chart")

        List dataList = new ArrayList()
        Map resultEntryMap = [:]
        List categories = ['Aggregate Review', 'Individual Case Review', 'Aggregate Adhoc Review', 'Individual Adhoc Case Review']
        Map categoryMapping = [
                'Aggregate Review'            : 'aggAlerts',
                'Individual Case Review'      : 'icrAlerts',
                'Aggregate Adhoc Review'      : 'aggAdhocAlerts',
                'Individual Adhoc Case Review': 'icrAdhocAlerts'

        ]
        categories.each { it ->
            Map data = [
                    'Category'   : it,
                    'Test Passed': passedCountMap.get(categoryMapping.get(it)),
                    'Test Failed': failedCountMap.get(categoryMapping.get(it)) - passedCountMap.get(categoryMapping.get(it))
            ]
            dataList.add(data)
        }
        List keyset = dataList[0]?.keySet() as List
        resultEntryMap.put('data', dataList)
        resultEntryMap.put('title', 'Test Case Stats')
        resultEntryMap.put('rowNames', ["${keyset[0]}"])
        resultEntryMap.put('columnNames', keyset.subList(1, keyset.size()))

        return resultEntryMap
    }

    def getDataForChartBuilder() {

        log.info("Get data for chart builder")

        ExecutionStatusDTO executionStatusDTO = createExecutionStatusDTO()
        executionStatusDTO.executionStatus = ReportExecutionStatus.COMPLETED

        Map passedCountMap = getCountForChart(executionStatusDTO, ReportExecutionStatus.COMPLETED)

        executionStatusDTO = createExecutionStatusDTO()
        Map failedCountMap = getTotalCountForChart(executionStatusDTO)

        return prepareDataForChart(passedCountMap, failedCountMap)

    }


}
