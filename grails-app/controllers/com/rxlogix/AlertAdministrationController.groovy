package com.rxlogix

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.controllers.AlertController
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.ActionTypeEnum
import com.rxlogix.enums.DataSourceEnum
import com.rxlogix.signal.AlertPreExecutionCheck
import com.rxlogix.signal.AutoAdjustmentRule
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.springframework.security.core.context.SecurityContextHolder


@Secured(["isAuthenticated()"])
class AlertAdministrationController implements AlertController {
    def alertAdministrationService
    def userService
    def etlJobService
    def alertDeletionDataService
    def justificationService
    def validatedSignalService
    def sessionFactory
    def alertService


    def index() {
        String etlStatus = etlJobService.getEtlStatus().status?.value()
        Map model = ["etlStatus": etlStatus, "isPreChecksEnabled": alertAdministrationService.isPreChecksEnabled()]

        render(view: "index", model: model)
    }

    def editAlertPreCheck() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        AlertPreExecutionCheck alertPreExecutionCheck = AlertPreExecutionCheck.first()
        responseDTO.data = alertPreExecutionCheck.toDto()
        Map autoAdjustmentRuleData = [:]
        autoAdjustmentRuleData = AutoAdjustmentRule.all as Map
        responseDTO.data.autoAdjustmentRuleData = autoAdjustmentRuleData
        render(responseDTO as JSON)
    }

    def updateAlertPreCheck() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        Map customAuditProperties = alertAdministrationService.updateAutoAdjustmentRules(params as Map)
        responseDTO.data = alertAdministrationService.updatePreChecks(params as Map, customAuditProperties)
        render(responseDTO as JSON)
    }

    def list() {

        int allTheColumns = 13
        Map filterMap = [:]
        Integer offset = params.start  as Integer
        Integer max = params.length  as Integer
        List finalList =[]
        (0..allTheColumns).each {
            if (params["columns[${it}][search][value]"]) {
                String key = params["columns[${it}][data]"]
                String value = params["columns[${it}][search][value]"]
                filterMap.put(key, value)
            }
        }
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]

        List<Map> alertList = []

        def configurations = alertAdministrationService.listConfigurations(params)
        if (configurations) {
            if (params.alertType.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT) || params.alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
                alertList = briefProperties(configurations)
            } else if (params.alertType.equals(Constants.AlertConfigType.EVDAS_ALERT)) {
                alertList = briefEvdasProperties(configurations)
            } else if (params.alertType.equals(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)) {
                alertList = briefLiteratureProperties(configurations)
            }

        }

        filterMap?.each { k, v ->
            List dateFields = ['dateModified', 'dateCreated', 'lastExecution', 'nextRunDate']
            alertList = alertList?.findAll {
                 if (k.toString() in dateFields) {
                     if (v == '-' ) {
                         it."${k}" == null || it."${k}" == '' || it."${k}" == '-'
                     } else if (it."${k}") {
                        String val1 = DateUtil.stringFromDate(it."${k}", 'MMM dd, YYYY HH:mm a',  userService.getUser()?.preference?.timeZone)
                        val1?.toLowerCase()?.contains(v?.toLowerCase())
                    }
                } else {
                    it."${k}"?.toString()?.toLowerCase()?.contains(v?.toString()?.toLowerCase())
                }
            } as List<Map>
        }
        if (orderColumn != null) {
            alertList?.sort { Map config1, Map config2 ->
                String sortedColumn = orderColumnMap?.name ?: 'noOfExecutions'
                if (sortedColumn in ["name", "productSelection", "status", "description", "dataSource", "createdBy"]) {
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return config2[sortedColumn]?.toLowerCase()?.trim() <=> config1[sortedColumn]?.toLowerCase()?.trim()
                    } else {
                        return config1[sortedColumn]?.toLowerCase()?.trim() <=> config2[sortedColumn]?.toLowerCase()?.trim()
                    }
                } else {
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return config2[sortedColumn] <=> config1[sortedColumn]
                    } else {
                        return config1[sortedColumn] <=> config2[sortedColumn]
                    }
                }

            }
        }

        if(alertList?.size() > 0){
            finalList = (max >= 0 ) ? alertList?.subList(offset, Math.min(offset + max, alertList?.size())): alertList
        }

        render([aaData: finalList?.flatten(), recordsTotal: alertList?.size(), recordsFiltered: alertList?.size()] as JSON)

    }

    private List briefProperties(List<Configuration> configurations) {
        Map<Long, ExecutedConfiguration> executedConfigurationMap = getExecutedConfigurationMap(configurations)
        configurations.collect {
            ExecutedConfiguration executedConfiguration = executedConfigurationMap.get(it.id)
            [id                    : it.id,
             name                  : it.name,
             status                : getAlertStatus(it, executedConfiguration),
             isManuallyPaused      : it.isManuallyPaused,
             description           : it.description,
             productSelection      : getProductsListByConfig(it),
             dataSource            : getDataSource(it.selectedDatasource),
             noOfExecution         : it.numOfExecutions,
             dateCreated           : it.dateCreated,
             dateModified          : it.lastUpdated,
             lastExecution         : executedConfiguration?.nextRunDate ?: null,
             dateRange             : executedConfiguration ? (DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation?.dateRangeStartAbsolute) + " to " + DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation?.dateRangeEndAbsolute)) : "-",
             nextRunDate           : it?.nextRunDate ?: null,
             nextExecutionDateRange: it?.nextRunDate ? DateUtil.toDateString(it.alertDateRangeInformation.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString(it.alertDateRangeInformation.getReportStartAndEndDate()[1]) : "-",
             createdBy             : it.owner.fullName,
             isMasterTemplateAlert : it.isMasterTemplateAlert,
             isDataMiningAdvancedRole : SecurityContextHolder.context.authentication.authorities*.authority.contains('ROLE_DATA_MINING_ADVANCED_RUNS')]
        }


    }

    private Map<Long, ExecutedConfiguration> getExecutedConfigurationMap(List<Configuration> configurations) {
        List<Long> configIds = new ArrayList<>()
        Map<Long, ExecutedConfiguration> executedConfigurationMap = new HashMap<>()
        configurations.forEach {
            configIds.add(it.id)
        }
        if (!configIds.isEmpty()) {
            List<ExecutedConfiguration> executedConfigurations = ExecutedConfiguration.createCriteria().list {
                configIds.collate(1000).each {
                    'in'("configId", it)
                }
                and {
                    eq('isLatest', true)
                }
            }
            if (Objects.nonNull(executedConfigurations) && !executedConfigurations.isEmpty()) {
                executedConfigurations.forEach {
                    executedConfigurationMap.put(it.configId, it)
                }
            }
        }
        executedConfigurationMap
    }

    private List briefEvdasProperties(List<EvdasConfiguration> configurations) {
        configurations.collect {
            ExecutedEvdasConfiguration executedEvdasConfiguration = ExecutedEvdasConfiguration.findByConfigIdAndIsLatest(it.id, true)
            [id                    : it.id,
             name                  : it.name,
             status                : getAlertStatus(it),
             isManuallyPaused      : it.isManuallyPaused,
             description           : it.description,
             productSelection      : it.productSelection ? getNameFieldFromJson(it.productSelection) : getGroupNameFieldFromJson(it.productGroupSelection),
             dataSource            : DataSourceEnum.EUDRA.value(),
             noOfExecution         : it.numOfExecutions,
             dateCreated           : it.dateCreated,
             dateModified          : it.lastUpdated,
             lastExecution         : executedEvdasConfiguration?.nextRunDate ?: null,
             dateRange             : executedEvdasConfiguration ? (DateUtil.toDateString(executedEvdasConfiguration.dateRangeInformation.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString(executedEvdasConfiguration.dateRangeInformation.getReportStartAndEndDate()[1])) : "-",
             nextRunDate           : it.nextRunDate ?: null,
             nextExecutionDateRange: DateUtil.toDateString(it.dateRangeInformation.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString(it.dateRangeInformation.getReportStartAndEndDate()[1]),
             createdBy             : it.owner.fullName]
        }
    }

    private List briefLiteratureProperties(List<LiteratureConfiguration> configurations) {
        configurations.collect {
            ExecutedLiteratureConfiguration executedLiteratureConfiguration = ExecutedLiteratureConfiguration.findByConfigIdAndIsLatest(it.id, true)
            [id                    : it.id,
             name                  : it.name,
             status                : getAlertStatus(it),
             isManuallyPaused      : it.isManuallyPaused,
             description           : "-",
             productSelection      : it.productSelection ? getNameFieldFromJson(it.productSelection) : getGroupNameFieldFromJson(it.productGroupSelection),
             dataSource            : it?.selectedDatasource?.equalsIgnoreCase(Constants.DataSource.PUB_MED) ? Constants.DataSource.PUB_MED : it?.selectedDatasource,
             noOfExecution         : it.numOfExecutions,
             dateCreated           : it.dateCreated,
             dateModified          : it.lastUpdated,
             lastExecution         : executedLiteratureConfiguration?.dateCreated ?: null,
             dateRange             : executedLiteratureConfiguration ? (DateUtil.toDateString(executedLiteratureConfiguration.dateRangeInformation.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString(executedLiteratureConfiguration.dateRangeInformation.getReportStartAndEndDate()[1])) : "-",
             nextRunDate           : it.nextRunDate ?: null,
             nextExecutionDateRange: DateUtil.toDateString(it.dateRangeInformation.getReportStartAndEndDate()[0]) + " to " + DateUtil.toDateString(it.dateRangeInformation.getReportStartAndEndDate()[1]),
             createdBy             : it.owner.fullName]
        }
    }

/**
 * This method manually disables the configuration, updates the value of flag 'isManuallyPaused' to true.
 *  Manually paused configurations are not picked by alert executor job for execution, if flag is true.
 * @param configIdList : List of ids of configuration for disabling.
 * @param alertType : can be Single case alert, Aggregate case alert, Evdas alert or Literature alert.
 * @param justification : justification provided by the end user.
 */
    def disableConfigurations(String configIdList, String alertType) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        List configIds = configIdList?.split(",")?.collect { it as Long }
        if (validateParams(params, responseDTO)) {
            try {
                Map<String, List<Long>> data = alertAdministrationService.disableConfigurations(configIds, alertType,params.justification)
                if (data.status) {
                    justificationService.saveActionJustification(configIds as List<Long>, alertType, ActionTypeEnum.ALERT_DISABLED.value(), params.justification)
                }
                responseDTO.status = data.status
                responseDTO.data = data
                responseDTO.message = data.status ? message(code: "app.configuration.disable.success") : message(code: "app.configuration.id.null")
            } catch (Exception e) {
                e.printStackTrace()
                responseDTO.status = false
                responseDTO.message = message(code: "app.configuration.update.error")
            }
        }
        render(responseDTO as JSON)
    }

/**
 * This method manually enables the configuration, updates the value of flag 'isManuallyPaused' to false.
 * @param configIdList : List of ids of configuration for disabling.
 * @param alertType : can be Single case alert, Aggregate case alert, Evdas alert or Literature alert.
 * @param justification : justification provided by the end user.
 */
    def enableConfigurations(String configIdList, String alertType) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        List configIds = configIdList?.split(",")?.collect { it as Long }
        if (validateParams(params, responseDTO)) {
            try {
                Map<String, List<Long>> data = alertAdministrationService.enableConfigurations(configIds, alertType,params.justification)
                if (data.status) {
                    justificationService.saveActionJustification(configIds, alertType, ActionTypeEnum.ALERT_ENABLED.value(), params.justification)
                }
                responseDTO.status = data.status
                responseDTO.data = data
                responseDTO.message = data.status ? message(code: "app.configuration.enable.success") : message(code: "app.configuration.id.null")
            } catch (Exception e) {
                e.printStackTrace()
                responseDTO.status = false
                responseDTO.message = message(code: "app.configuration.update.error")
            }
        }
        render(responseDTO as JSON)
    }

/**
 * This method is responsible for hard deletion of alerts.
 * @param configIdList : List of ids of configuration for deletion.
 * @param alertType : can be Single case alert, Aggregate case alert, Evdas alert or Literature alert.
 * @param justification : justification provided by the end user.
 * @param deleteLatest : will be true if latest version of alert needs to be deleted, else false.
 * @param masterIdList : List of master configuration ids for deletion, if not empty then all the
 *                       associated child alerts will also be deleted
 * @param deleteChildSiblings : if true, associated child alerts also needs to be deleted.
 * @return: returns information about alerts deleted successfully and discarded alerts
 */
    def deleteAlerts() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        String configIdList = params.configIdList as String
        String alertType = params.alertType as String
        String justification = params.justification as String
        Boolean deleteLatest = params.deleteLatest != "false"
        Boolean deleteChildSiblings = params.deleteChildAlertSiblings ? (params.deleteChildAlertSiblings == "false" ? false : true) : false
        String masterIdList = params.masterIdList as String
        List configIds = configIdList?.split(",")
        List masterIds = masterIdList?.split(",")
        if (validateParams(params, responseDTO)) {
            Map data = [:]
            List<Map<String, Object>> discardedAlerts = []
            List<String> deletedAlertNames = []
            List<Integer> alertsWithCarryForwardSignals = []
            Map joinTableNames = ["SingleCaseAlert"   : "VALIDATED_SINGLE_ALERTS",
                                  "AggregateCaseAlert": "VALIDATED_AGG_ALERTS",
                                  "EvdasAlert"        : "VALIDATED_EVDAS_ALERTS",
                                  "LiteratureAlert"   : "VALIDATED_LITERATURE_ALERTS"]
            Map joinColumnNames = ["SingleCaseAlert"   : "SINGLE_ALERT_ID",
                                   "AggregateCaseAlert": "AGG_ALERT_ID",
                                   "EvdasAlert"        : "EVDAS_ALERT_ID",
                                   "LiteratureAlert"   : "LITERATURE_ALERT_ID"]
            def domainName = alertService.getAlertDomainName(alertType)

            String joinTableName = joinTableNames.get(domainName.getSimpleName())
            String joinColumnName = joinColumnNames.get(domainName.getSimpleName())
            def domain = getConfigurationDomainByAlertType(alertType)
            configIds.removeAll("")
            masterIds?.removeAll("")
            if(!configIds.isEmpty()){
                configIds = configIds?.collect { it as Long }
                configIds.each { configId ->
                    Map linkedSignalsData = validatedSignalService.getLinkedSignalsDataMap(configId, alertType, deleteLatest)
                    Long linkedSignalsCount = linkedSignalsData.get("size")
                    if(deleteLatest && linkedSignalsCount>0){
                        // Remove all linked signals that are auto-populated if all of them are auto populated
                        // check this only if latest delete was called
                        List signalAlertLinkData = linkedSignalsData.get("data") as List
                        // if any of mapping is manually added, ignore
                        Session session = sessionFactory.currentSession
                        String totalCountSql = SignalQueryHelper.get_manually_added_signal_count(signalAlertLinkData*.id, joinTableName, joinColumnName)
                        Integer totalCount = alertService.getResultListCount(totalCountSql, session)
                        session.flush()
                        session.clear()
                        if(totalCount==0){
                            // all of attached signals are carry- forward
                            linkedSignalsCount=0
                            alertsWithCarryForwardSignals.addAll(signalAlertLinkData*.id)
                        }
                    }
                    if (linkedSignalsCount > 0) {
                        String alertName = domain.get(configId).name
                        discardedAlerts.add(["id": configId, "alertName": alertName, "linkedSignalsCount": linkedSignalsCount, "deleteLatest": deleteLatest, "data": linkedSignalsData.get("data"), "alertType": alertType])
                    }
                }
            }
            if (!discardedAlerts.isEmpty()) {
                configIds.removeAll(discardedAlerts*.id)
                data.put(Constants.CommonUtils.PRE_REQUISITE_FAIL, discardedAlerts)
            }
            try {
                ActionTypeEnum actionTypeEnum = null
                if(!alertsWithCarryForwardSignals.isEmpty()){
                    // delete carry forwarded signals, because config will delete later
                    SQLQuery sql = null
                    Session session = sessionFactory.currentSession
                    String disassociateSignalSql = SignalQueryHelper.delete_carry_forward_signals(alertsWithCarryForwardSignals, joinTableName, joinColumnName)
                    sql = session.createSQLQuery(disassociateSignalSql)
                    sql.executeUpdate()
                    session.flush()
                    session.clear()
                }
                if (!configIds.isEmpty()) {
                    actionTypeEnum = deleteLatest ? ActionTypeEnum.LATEST_CONFIG_DELETED : ActionTypeEnum.ALL_CONFIG_DELETED
                    alertDeletionDataService.saveAlertDeletionData(alertType, justification, configIds, deleteLatest)
                    responseDTO.message = "Alert(s) deletion is in progress."
                    configIds.each { configId ->
                        String alertName = domain.get(configId).name
                        deletedAlertNames.add(alertName)
                    }
                    justificationService.saveActionJustification(configIds, alertType, actionTypeEnum.value(), justification)
                    data.put(Constants.CommonUtils.SUCCESS, deletedAlertNames)
                }
                if (!masterIds?.isEmpty() && deleteChildSiblings) {
                    masterIds = masterIds?.collect { it as Long }
                    actionTypeEnum = deleteLatest ? ActionTypeEnum.LATEST_MASTER_DELETED : ActionTypeEnum.ALL_MASTER_DELETED
                    alertDeletionDataService.saveMasterAlertDeletionData(alertType, justification, masterIds, deleteLatest)
                    responseDTO.message = "Alert(s) deletion is in progress."
                    masterIds.each { masterConfigId ->
                        String alertName = MasterConfiguration.get(masterConfigId).name
                        deletedAlertNames.add(alertName)
                    }
                    List<Map> instancesInfoMap = justificationService.prepareDataForMasterConfigJustification(masterIds)
                    justificationService.saveActionJustification(masterIds, alertType, actionTypeEnum.value(), justification, instancesInfoMap)
                    data.put(Constants.CommonUtils.SUCCESS, deletedAlertNames)
                }
            } catch (Exception e) {
                log.error(e.printStackTrace())
                responseDTO.status = false
                responseDTO.code = 2
                responseDTO.message = message(code: "signal.rmms.label.error.delete")
            }
            responseDTO.data = data
        }
        render(responseDTO as JSON)
    }

    boolean validateParams(Map params, ResponseDTO responseDTO) {
        if (!params.justification) {
            responseDTO.status = false
            responseDTO.code = 1
            responseDTO.message = "Justification can't be blank. <br>"
        }
        if (!params.configIdList && !params.masterIdList) {
            responseDTO.status = false
            responseDTO.code = 1
            responseDTO.message += "Alert Id list can't be blank"
        }
        return responseDTO.status
    }


/**
 * This method lists all cases/PECs which are associated with signal.
 * @param configId : configuration id
 * @param alertType : can be Single case alert, Aggregate case alert, Evdas alert or Literature alert.
 * @param deleteLatest : if true, shows signals linked with latest version
 */
    def linkedSignals(String configId, String alertType, Boolean deleteLatest) {
        try {
            Map linkedSignalsData = validatedSignalService.getLinkedSignalsDataMap(Long.parseLong(configId), alertType, deleteLatest)
            render view: "linkedSignals", model: [totalInstancesCount: linkedSignalsData.get("data")?.size() ?: 0, linkedSignalsData: linkedSignalsData, configId: configId, alertType: alertType, deleteLatest: deleteLatest, "alertName": linkedSignalsData.get("alertName")]
        } catch (Exception e) {
            log.error(e.printStackTrace())
            redirect(controller: "alertAdministration", action: "index")
            return
        }
    }


/**
 * This method returns information map for child alert deletion, helps in consolidating
 * precise information for delete alert request.
 * @param configIdList : list of configuration ids, selected by user from UI.
 * @param alertType : can be Single case alert, Aggregate case alert, Evdas alert or Literature alert.
 */
    def fetchDataForChildAlertDeletion(String configIdList) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        List configIds = configIdList?.split(",")
        try {
            Map data = alertAdministrationService.prepareDataForChildAlertDeletion(configIds)
            responseDTO.data = data
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = "Error occurred while fetching child configurations."
        }
        render(responseDTO as JSON)
    }

}
