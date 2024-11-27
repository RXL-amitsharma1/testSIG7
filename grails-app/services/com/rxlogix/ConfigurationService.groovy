package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.FrequencyEnum
import com.rxlogix.signal.ProductTypeConfiguration
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.NotTransactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.gorm.transactions.Transactional
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Dur
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.PeriodList
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.RRule
import org.apache.commons.lang3.time.DateUtils
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.springframework.transaction.annotation.Propagation
import java.util.Base64
import groovy.sql.Sql


import java.text.SimpleDateFormat

@Transactional
class ConfigurationService {

    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"

    def queryService
    def customMessageService
    def springSecurityService
    def CRUDService
    def userService
    def messageSource
    ConfigurationService configurationService
    def evdasAlertExecutionService
    def literatureExecutionService
    def reportExecutorService
    def sessionFactory
    def appAlertProgressStatusService
    def signalAuditLogService
    def cacheService
    def aggregateCaseAlertService
    def jaderExecutorService
    def dataSource

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def deleteConfig(Configuration config) {
        config.isDeleted = true
        config.isEnabled = false
        config = (Configuration) CRUDService.softDeleteWithAuditLog(config)
        if (config.isMasterTemplateAlert) {
            Sql sql = new Sql(dataSource)
            try {
                String sqlString1 = """
                        UPDATE RCONFIG SET IS_DELETED = true WHERE master_template_id = ${config.id}
                        """
                String sqlString2 = """
                        UPDATE GROUPED_ALERT_INFO SET IS_DELETED = true WHERE master_template_config_id = ${config.id}       
                        """
                sql.execute(sqlString1)
                sql.execute(sqlString2)
            } catch (Exception ex){
                ex.printStackTrace()
                throw ex
            } finally {
                sql?.close()
            }


        }
        config
    }

    Integer getDelta(configuration, Boolean isNextRunDatePassed = false, Date nextRunDate = null) {
        if (configuration.isEnabled) {
            Date currentToNextRunDate = getNextDate(configuration, isNextRunDatePassed, nextRunDate)
            if (currentToNextRunDate && configuration.nextRunDate) {
                return currentToNextRunDate - configuration.nextRunDate
            }
        }
        return 0
    }

    def getUpdatedAsOfVersionDate(config){
        if (config.scheduleDateJSON && config.isEnabled == true) {
            JSONObject timeObject = JSON.parse(config.scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                Date newAsOfVersionDate
                RRule recurRule = new RRule(timeObject.recurrencePattern)

                int interval = recurRule.recur.getInterval()
                String freq = recurRule.recur.getFrequency()
                if (interval < 0) interval = 1
                Date asOfVersionDate = config.asOfVersionDate
                if (!asOfVersionDate) {
                    asOfVersionDate = new Date()
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(asOfVersionDate)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                Boolean lastDayOfMonth = asOfVersionDate == cal.getTime()
                use(TimeCategory) {
                    switch (freq) {
                        case Recur.WEEKLY:
                            newAsOfVersionDate = asOfVersionDate + interval.weeks
                            break
                        case Recur.MONTHLY:
                            newAsOfVersionDate = asOfVersionDate + interval.months
                            if (lastDayOfMonth) {
                                newAsOfVersionDate = updateDateToLastDateOfMonth(newAsOfVersionDate)
                            }
                            break
                        case Recur.YEARLY:
                            newAsOfVersionDate = asOfVersionDate + interval.years
                            if (lastDayOfMonth) {
                                newAsOfVersionDate = updateDateToLastDateOfMonth(newAsOfVersionDate)
                            }
                            break
                        case Recur.DAILY:
                            newAsOfVersionDate = asOfVersionDate + interval.days
                            break
                        default:
                            newAsOfVersionDate = asOfVersionDate
                            break
                    }
                }
                newAsOfVersionDate
            }
        }
    }

    List<Date> getUpdatedStartandEndDate(config) {
        if (config.scheduleDateJSON && config.isEnabled == true) {
            JSONObject timeObject = JSON.parse(config.scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                Date newStartDate
                Date newEndDate
                RRule recurRule = new RRule(timeObject.recurrencePattern)

                int interval = recurRule.recur.getInterval()
                String freq = recurRule.recur.getFrequency()
                if (interval < 0) interval = 1
                Date startDate = config.alertDateRangeInformation.dateRangeStartAbsolute
                if (!startDate) {
                    startDate = new Date()
                }
                Date endDate = config.alertDateRangeInformation.dateRangeEndAbsolute
                if (!endDate) {
                    endDate = new Date()
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                Boolean lastDayOfMonth = endDate == cal.getTime()
                use(TimeCategory) {
                    switch (freq) {
                        case Recur.WEEKLY:
                            newStartDate = startDate + interval.weeks
                            newEndDate = endDate + interval.weeks
                            break
                        case Recur.MONTHLY:
                            newStartDate = startDate + interval.months
                            newEndDate = endDate + interval.months
                            if (lastDayOfMonth) {
                                newEndDate = updateDateToLastDateOfMonth(newEndDate)
                            }
                            break
                        case Recur.YEARLY:
                            newStartDate = startDate + interval.years
                            newEndDate = endDate + interval.years
                            if (lastDayOfMonth) {
                                newEndDate = updateDateToLastDateOfMonth(newEndDate)
                            }
                            break
                        default:
                            newStartDate = startDate
                            newEndDate = endDate
                            break
                    }
                }
                return [newStartDate, newEndDate]
            }
        }
    }

    /**
     *
     * @param config a Configuration object with a scheduleDateJSON field
     * @return a Date in the local timezone or null if there is no next run date
     */
    @NotTransactional
    def getNextDate(config, Boolean isNextRunDatePassed = false, Date nextRunDate = null) {
        if (config.scheduleDateJSON && config.isEnabled == true) {
            JSONObject timeObject = JSON.parse(config.scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                Date now = new Date()
                Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime)
                DateTime from = new DateTime(startDate)
                DateTime to = new DateTime(Long.MAX_VALUE)
                Date lastRunDate = isNextRunDatePassed ? nextRunDate : config?.nextRunDate
                RRule recurRule = new RRule(timeObject.recurrencePattern)
                //Check if the scheduler will never end
                if (recurRule.recur.count == -1 && !recurRule.recur.until) {
                    //We temporarily set the recurrence count to 2 because we only need the next recur date
                    recurRule.recur.setCount(2)
                    if (lastRunDate) {
                        from = new DateTime(lastRunDate)
                    }
                }

                //Check if the recurrence is run once/now
                if (recurRule?.recur?.count == 1 && startDate.before(now)) {
                    //Do not return a nextRunDate if we have already run this configuration once
                    if (lastRunDate) {
                        return null
                    }

                    //Run once anytime in the past is generated with today's date
                    from = new DateTime(now)
                }

                VEvent event = new VEvent(from, "event")
                event.getProperties().add(recurRule)
                Period period = new Period(from, to)
                PeriodList periodList = event.calculateRecurrenceSet(period)

                //Check if the start date matches recurrence pattern
                boolean excludeStartDate = checkStartDate(startDate, recurRule)

                def futureRunDates = []
                if (periodList) {
                    if (excludeStartDate) {
                        periodList.remove(periodList.first())
                    }
                    if (!lastRunDate) {
                        lastRunDate = startDate - 1
                    }
                    futureRunDates = periodList.findAll {
                        new DateTime(it.toString().split("/").first()).after(lastRunDate)
                    }
                }
                if (futureRunDates) {
                    DateTime nextRun = new DateTime(futureRunDates?.first()?.toString()?.split("/")?.first())
                    Date futureRunDate = new Date(nextRun.time)
                    return futureRunDate
                }
            } else if (config.adhocRun && config.nextRunDate == null) {
                return new Date()
            }
        }
        return null
    }

    @NotTransactional
    boolean checkStartDate(Date startDate, RRule originalRule) {
        def interval = originalRule.recur.getInterval()
        String freq = originalRule.recur.getFrequency()
        if (interval < 0) interval = 1
        DateTime fromBefore
        DateTime toLater
        use(TimeCategory) {
            switch (freq) {
                case Recur.WEEKLY:
                    fromBefore = new DateTime(startDate - interval.weeks)
                    toLater = new DateTime(startDate + interval.weeks)
                    break
                case Recur.MONTHLY:
                    fromBefore = new DateTime(startDate - interval.months)
                    toLater = new DateTime(startDate + interval.months)
                    break
                case Recur.YEARLY:
                    fromBefore = new DateTime(startDate - interval.years)
                    toLater = new DateTime(startDate + interval.years)
                    break
                default:
                    fromBefore = new DateTime(startDate - interval.days)
                    toLater = new DateTime(startDate + interval.days)
                    break
            }
        }
        VEvent eventTest = new VEvent(fromBefore, "event test")
        RRule ruleTest = originalRule
        ruleTest.recur.setCount(20) //pvs-55480 change for weekly schedule
        eventTest.getProperties().add(ruleTest)
        Period periodTest = new Period(fromBefore, toLater)
        PeriodList testPeriodList = eventTest.calculateRecurrenceSet(periodTest)

        DateTime from = new DateTime(startDate)
        Period start = new Period(from, new Dur(0, 0, 0, 0))
        if (testPeriodList.contains(start) || originalRule.recur.frequency == "HOURLY") {
            return false
        }
        return true
    }

    def calculateFrequency(configuration) {
        if (configuration.scheduleDateJSON && configuration.nextRunDate) {
            if (configuration.scheduleDateJSON.contains(FrequencyEnum.HOURLY.name())) {
                return FrequencyEnum.HOURLY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.DAILY.name())) {
                if(configuration.numOfExecutions > 0 && !configuration.nextRunDate) {
                    return FrequencyEnum.RUN_ONCE
                }
                if(configuration.numOfExecutions >= 0 && !getNextDate(configuration)) {
                    return FrequencyEnum.RUN_ONCE
                }
                return FrequencyEnum.DAILY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.WEEKLY.name())) {
                return FrequencyEnum.WEEKLY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.MONTHLY.name())) {
                return FrequencyEnum.MONTHLY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.YEARLY.name())) {
                return FrequencyEnum.YEARLY
            }
        }
        return FrequencyEnum.RUN_ONCE
    }

    Map generateMapForExecutionStatus(ExecutionStatusDTO executionStatusDTO) {
        switch (executionStatusDTO.sort) {
            case "runDate":
                executionStatusDTO.sort = "nextRunDate"
                break
            case "owner":
                executionStatusDTO.sort = "owner.fullName"
                break
        }
        String searchHQL = getSearchStringForExecutionStatus(executionStatusDTO.searchString)
        String hqlStatement = prepareHqlForExecutionStatus(executionStatusDTO, searchHQL)
        List<ExecutionStatus> executionStatusList = ExecutionStatus.executeQuery(hqlStatement, [offset: executionStatusDTO.offset, max: executionStatusDTO.max])
        Integer recordsTotal = ExecutionStatus.executeQuery(prepareCountHqlForExecutionStatus(executionStatusDTO, ''))[0] as Integer
        Integer recordsFilteredCount = ExecutionStatus.executeQuery(prepareCountHqlForExecutionStatus(executionStatusDTO, searchHQL))[0] as Integer
        [aaData: executedReportConfigurationMap(executionStatusList, executionStatusDTO.alertType), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount]
    }

    Map showExecutionsScheduled(ExecutionStatusDTO executionStatusDTO) {
        switch (executionStatusDTO.sort) {
            case "runDate":
                executionStatusDTO.sort = "nextRunDate"
                break
            case "owner":
                executionStatusDTO.sort = "owner.fullName"
                break
        }
        Map sortingAndPaginationMap = [max  : executionStatusDTO.max, offset: executionStatusDTO.offset, sort: executionStatusDTO.sort,
                                       order: executionStatusDTO.direction]
        List<Configuration> configurationList = executionStatusDTO.configurationDomain.findAllScheduledForUser(executionStatusDTO).list(sortingAndPaginationMap)
        Integer recordsFilteredCount = executionStatusDTO.configurationDomain.findAllScheduledForUser(executionStatusDTO).count()
        executionStatusDTO.searchString = null
        Integer recordsTotal = executionStatusDTO.configurationDomain.findAllScheduledForUser(executionStatusDTO).count()
        [aaData: configurationMap(configurationList, executionStatusDTO.alertType as AlertType), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount]
    }

    String prepareHqlForExecutionStatus(ExecutionStatusDTO executionStatusDTO, String searchHQL) {
        String orderHQL = getOrderStringForExecutionStatus(executionStatusDTO.sort, executionStatusDTO.direction)
        String typeHQL = getAlertTypeStringForExecutionStatus(executionStatusDTO.alertType)
        String hqlForConfig = """
               Select ec from ExecutionStatus ec,${executionStatusDTO.configurationDomain.getSimpleName()} config 
                 where ec.configId = config.id
                 ${typeHQL}
                 and ec.type = '${executionStatusDTO.alertType.value()}'
                 and config.isDeleted = false
                 and config.workflowGroup.id = ${executionStatusDTO.workflowGroupId}
                 and ec.executionStatus = '${executionStatusDTO.executionStatus}'
                 ${searchHQL}
                 ${orderHQL}
               """
        hqlForConfig
    }

    String prepareCountHqlForExecutionStatus(ExecutionStatusDTO executionStatusDTO, String searchHQL) {
        String typeHQL = getAlertTypeStringForExecutionStatus(executionStatusDTO.alertType)
        String hqlForConfig = """
               Select count(*) from ExecutionStatus ec,${executionStatusDTO.configurationDomain.getSimpleName()} config 
                 where config.id = ec.configId 
                 ${typeHQL}
                 and ec.type = '${executionStatusDTO.alertType.value()}'
                 and config.isDeleted = false
                 and config.workflowGroup.id = ${executionStatusDTO.workflowGroupId}
                 and ec.executionStatus = '${executionStatusDTO.executionStatus}'
                 ${searchHQL}
               """
        hqlForConfig
    }

    String getOrderStringForExecutionStatus(String sort, String direction) {
        String orderHQL
        if (sort == 'nextRunDate') {
            orderHQL = "order by ec.${sort} ${direction}"
        } else {
            orderHQL = "order by UPPER(ec.${sort}) ${direction}"
        }
        orderHQL
    }

    String getSearchStringForExecutionStatus(String searchString) {
        String searchHQL = ''
        String esc_char = ''
        if (searchString) {
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "escape '!'"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "escape '!'"
            }
            //Added code for PVS-55957
            if (searchString.contains("'")) {
                searchString = searchString.replaceAll("'", "''")
            }
            if(esc_char) {
                searchHQL = "AND( lower(ec.name) like lower('%${searchString}%') ${esc_char} OR lower(ec.owner.fullName) like lower('%${searchString}%') ${esc_char} )"
            } else {
                searchHQL = "AND( lower(ec.name) like lower('%${searchString}%') OR lower(ec.owner.fullName) like lower('%${searchString}%') )"
            }
        }
        searchHQL
    }

    String getAlertTypeStringForExecutionStatus(AlertType alertType) {
        String typeHQL = ''
        if(alertType == AlertType.SINGLE_CASE_ALERT || alertType == AlertType.AGGREGATE_CASE_ALERT){
            if(alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION")) ){
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%faers%'"

            }  else if(alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION")) ){
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%vaers%'"

            }  else if(alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ){
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%vigibase%'"

            }else if(alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) ){
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%jader%'"

            } else if(alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) ){
                typeHQL = "and config.type = ec.type AND config.selectedDatasource like '%pva%'"

            } else {
                typeHQL = "and config.type = ec.type"
            }
        }
        typeHQL
    }

    List<Map> configurationMap(List<Configuration> configurations, AlertType alertType) {
        List<Map> configurationList = []
        String userTimeZone = userService?.getUser()?.preference?.timeZone?:"UTC"
        configurations.each {
            configurationList += [id                     : it.id,
                                  name                   : it.name,
                                  frequency              : configurationService.calculateFrequency(it)?.value(),
                                  runDate                : DateUtil.StringFromDate(it.nextRunDate,DateUtil.DATEPICKER_FORMAT_AM_PM_2.toString(),userTimeZone),
                                  executionTime          : '-',
                                  owner                  : it.owner.fullName,
                                  executionStatus        : ReportExecutionStatus.SCHEDULED.value(),
                                  reportExecutionStatus  : it.hasProperty("templateQueries") ? it.templateQueries.size() ? ReportExecutionStatus.SCHEDULED.value() : '-' : '-',
                                  spotfireExecutionStatus: it.hasProperty("spotfireSettings") ? it.spotfireSettings ? ReportExecutionStatus.SCHEDULED.value() : '-' : '-',
                                  errorMessage           : "",
                                  errorTitle             : "",
                                  dateCreated            : new SimpleDateFormat(DateUtil.DATEPICKER_UTC_FORMAT).format(it.dateCreated),
                                  alertType              : messageSource.getMessage(alertType.getI18nValueForExecutionStatusDropDown(), null, Locale.default)
            ]
        }
        return configurationList
    }

    List<Map> executedReportConfigurationMap(List<ExecutionStatus> data, AlertType alertType) {
        List<Map> executedReportConfigurationList = []
        data.each {
            executedReportConfigurationList.add(getDataMap(it.id, it.executionLevel, alertType))
        }
        return executedReportConfigurationList
    }

    Map getDataMap(Long executionStatusId, Integer executionLevel, AlertType alertType) {
        User currentUser = userService.getUser()
        float progressPercentage = appAlertProgressStatusService.calculateAlertProgress(executionStatusId,-1)
        ExecutionStatus executionStatus = ExecutionStatus.get(executionStatusId)
        Map progressTrackerMap = new HashMap()
        int highestExecutionLevel=1
        if (executionLevel == 1) {
            progressTrackerMap.put(1, appAlertProgressStatusService.calculateAlertProgress(executionStatusId, 1));
            highestExecutionLevel=1
        } else if (executionLevel == 2) {
            progressTrackerMap.put(1, appAlertProgressStatusService.calculateAlertProgress(executionStatusId, 1));
            progressTrackerMap.put(2, appAlertProgressStatusService.calculateAlertProgress(executionStatusId, 2));
            highestExecutionLevel=2
        } else if (executionLevel == 3) {
            progressTrackerMap.put(1, appAlertProgressStatusService.calculateAlertProgress(executionStatusId, 1));
            progressTrackerMap.put(2, appAlertProgressStatusService.calculateAlertProgress(executionStatusId, 2));
            progressTrackerMap.put(3, appAlertProgressStatusService.calculateAlertProgress(executionStatusId, 3));
            highestExecutionLevel=3
        }
        String userTimeZone = userService?.getUser()?.preference?.timeZone?:"UTC"
        Map data = [id                     : executionStatus.configId,
                    executionStatusId      : executionStatus?.id,
                    execConfigId           : executionStatus.executedConfigId,
                    name                   : executionStatus.name,
                    frequency              : executionStatus.frequency?.value(),
                    runDate                : DateUtil.StringFromDate(executionStatus.nextRunDate,DateUtil.DATEPICKER_FORMAT_AM_PM_2.toString(),userTimeZone),
                    executionTime          : executionStatus.executionTime,
                    owner                  : executionStatus.owner.fullName,
                    executionStatus        : executionStatus.executionStatus?.value() ?: ReportExecutionStatus.ERROR.value(),
                    reportExecutionStatus  : executionStatus.reportExecutionStatus?.value() ?: '-',
                    spotfireExecutionStatus: executionStatus.spotfireExecutionStatus?.value() ?: '-',
                    executionLevel         : executionStatus.executionLevel,
                    dateCreated            : executionStatus.dateCreated,
                    progressTrackerMap     : progressTrackerMap,
                    highestExecutionLevel  : highestExecutionLevel,
                    alertType              : messageSource.getMessage(alertType.getI18nValueForExecutionStatusDropDown(), null, Locale.default),
                    timeStampJSON          : executionStatus.timeStampJSON,
                    startTime              : executionStatus.startTime,
                    nodeName               : executionStatus.nodeName,
                    progressPercentage     : progressPercentage,
                    resumeAccess           : shareWithCurrentUser(executionStatus.configId) || currentUser.equals(executionStatus.owner),
                    isLatest               : ExecutedConfiguration.get(executionStatus.executedConfigId)?.isLatest
        ]
        return data
    }

    boolean shareWithCurrentUser(long configId) {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN"))
            return true
        Configuration config = Configuration.get(configId)
        User currentUser = userService.getUser()
        if (SpringSecurityUtils.ifAnyGranted("ROLE_EXECUTE_SHARED_ALERT")) {
            if (config.shareWithUser.contains(currentUser.id) || config.shareWithGroup?.find { it -> currentUser.groups?.contains(it) }) {
                return true
            }
        }
        return false
    }

    List fetchRunningAlertList(String type) {
        List runningAlerts
        switch (type) {
            case Constants.ConfigurationType.QUAL_TYPE:
                runningAlerts = reportExecutorService.currentlyRunning
                break
            case Constants.ConfigurationType.QUANT_TYPE:
                runningAlerts = reportExecutorService.currentlyQuantRunning
                break
            case Constants.ConfigurationType.EVDAS_TYPE:
                runningAlerts = evdasAlertExecutionService.currentlyRunning
                break
            case Constants.ConfigurationType.LITERATURE_TYPE:
                runningAlerts = literatureExecutionService.currentlyRunning
                break
        }
        return runningAlerts

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateConfiguration(ExecutionStatus executionStatus) {
        def domain
        def executedDomain
        Integer count
        if(executionStatus.type in [Constants.AlertConfigType.SINGLE_CASE_ALERT,Constants.AlertConfigType.AGGREGATE_CASE_ALERT]){
            domain = Configuration
            executedDomain = ExecutedConfiguration
        }else if(executionStatus.type.equals(Constants.AlertConfigType.EVDAS_ALERT)){
            domain = EvdasConfiguration
            executedDomain = ExecutedEvdasConfiguration
        }else{
            domain = LiteratureConfiguration
            executedDomain = ExecutedLiteratureConfiguration
        }
        def configuration = domain.findById(executionStatus.configId)
        def executedConfiguration = executedDomain.findById(executionStatus.executedConfigId)
        if(configuration instanceof Configuration && configuration.masterConfigId){
            MasterConfiguration masterConfiguration = MasterConfiguration.get(configuration.masterConfigId)
            Integer status = executionStatus.executionLevel
            if(status > 1){
                if(executionStatus.executionLevel >= 3)
                    status = 2
                executedConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
                MasterExecutedConfiguration masterExecutedConfiguration = MasterExecutedConfiguration.get(executedConfiguration.masterExConfigId)
                ExecutionStatus masterExecutionStatus = ExecutionStatus.findByExecutedConfigId(masterExecutedConfiguration?.id)
                masterExecutionStatus.executionStatus = ReportExecutionStatus.SCHEDULED
                masterExecutionStatus.executionLevel = status
                masterExecutionStatus.save(flush:true)
                count = masterExecutedConfiguration.runCount?:0
                masterExecutedConfiguration.runCount = count + 1
                List<ExecutedConfiguration> allExConfigs = ExecutedConfiguration.findAllByMasterExConfigId(executedConfiguration.masterExConfigId)
                List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutedConfigIdInListAndExecutionLevel(allExConfigs*.id, executionStatus.executionLevel)
                List<ExecutedConfiguration> exConfigs = ExecutedConfiguration.findAllByIdInList(executionStatusList*.executedConfigId)
                updateExecutionStatusList(executionStatusList, status)
                updateExecutedConfigurationList(exConfigs, count)
                updateMasterConfig(masterConfiguration, true, status)
                masterConfiguration.refresh()
                updateConfigurationList(exConfigs, masterConfiguration)
                updateMasterRunNodeList(masterExecutedConfiguration.id, exConfigs)
            } else {
                executedConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
                List<ExecutedConfiguration> allExConfigs = ExecutedConfiguration.findAllByMasterExConfigId(executedConfiguration.masterExConfigId)
                List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutedConfigIdInListAndExecutionLevel(allExConfigs*.id, executionStatus.executionLevel)
                List<ExecutedConfiguration> exConfigs = ExecutedConfiguration.findAllByIdInList(executionStatusList*.executedConfigId)
                if(exConfigs) {
                    updateMasterConfig(masterConfiguration, false)
                    masterConfiguration.refresh()
                    updateResumeConfigurationList(exConfigs*.configId, masterConfiguration)
                }
            }
        } else {
            configuration.nextRunDate = new Date()
            configuration.isResume = true
            configuration.isEnabled = true
            configuration.executing = false
            if (executedConfiguration instanceof ExecutedConfiguration && executedConfiguration?.adjustmentTypeEnum) {
                configuration.setAdjustmentTypeEnum(null)
                configuration.nextRunDate = executedConfiguration.nextRunDate
            }
            CRUDService.saveWithoutAuditLog(configuration)
            if (executedConfiguration)
                CRUDService.update(executedConfiguration) // done to update lastUpdateBy values so that right values can be fetched  in audit PVS-39037
        }
        configuration
    }

    def updateMasterConfig(MasterConfiguration config, Boolean isResume = false, Integer status = 0) {
        int resume = isResume?1:0
        String updateQuery = "update master_configuration set executing = " + 0 + ", is_resume = " + resume +  ""
            Date nextRunDate = DateUtils.truncate(new Date(), Calendar.SECOND)
            if(nextRunDate){
                updateQuery = updateQuery + ", next_run_date = " + "TO_DATE('${nextRunDate?new SimpleDateFormat(SqlGenerationService.DATETIME_FMT).format(nextRunDate):null}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
            } else {
                updateQuery = updateQuery + ", next_run_date = " + null + ""
            }
        updateQuery = updateQuery + " where id = " + config.id
        log.info("update master config")
        log.info(updateQuery)
        if(status==2){
            cacheService.setPartialAlertCache(config?.id, true)
        }
        SQLQuery sql = null
        Session session = sessionFactory.openSession()
        sql = session.createSQLQuery(updateQuery)
        sql.executeUpdate()
        session.flush()
        session.clear()
        session.close()
        config.refresh()
    }

    def updateMasterRunNodeList(Long masterExecId, List<ExecutedConfiguration> exConfigs = []) {
        String updateQuery = "update master_child_run_node set is_save_done = " + 0 + " where master_exec_id = " + masterExecId +
                " AND child_exec_id in (" + exConfigs*.id.join(",") + ")"
        log.info(updateQuery)
        SQLQuery sql = null
        Session session = sessionFactory.openSession()
        sql = session.createSQLQuery(updateQuery)
        sql.executeUpdate()
        session.flush()
        session.clear()
        session.close()
    }


    def updateResumeConfigurationList(List<Long> configIds, MasterConfiguration masterConfiguration){
        try {
            Date nextRunDate = masterConfiguration.nextRunDate
            String updateQuery = "update rconfig set"
            if(nextRunDate){
                updateQuery = updateQuery + " next_run_date = " + "TO_DATE('${nextRunDate?new SimpleDateFormat(SqlGenerationService.DATETIME_FMT).format(nextRunDate):null}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
            } else {
                updateQuery = updateQuery + " next_run_date = " + null + ""
            }
            updateQuery = updateQuery + " where id in (" + configIds.join(",") + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateExecutionStatusList(List<ExecutionStatus> executionStatusList, Integer status = null){
        String updateQuery
        if(status == Constants.Commons.RESUME_REPORT){
            updateQuery = "update ex_status set REPORT_EXECUTION_STATUS = '" + ReportExecutionStatus.SCHEDULED + "', EXECUTION_LEVEL = " + status + " where id in (" + executionStatusList*.id.join(",") + ")"
        } else if(status == Constants.Commons.RESUME_SPOTFIRE){
            updateQuery = "update ex_status set SPOTFIRE_EXECUTION_STATUS = '" + ReportExecutionStatus.SCHEDULED + "', EXECUTION_LEVEL = " + status + " where id in (" + executionStatusList*.id.join(",") + ")"
        } else {
            updateQuery = "update ex_status set EX_STATUS = '" + ReportExecutionStatus.SCHEDULED + "', EXECUTION_LEVEL = " + status
            if(executionStatusList[0].reportExecutionStatus == ReportExecutionStatus.ERROR) {
                updateQuery = updateQuery + ", REPORT_EXECUTION_STATUS = '" + ReportExecutionStatus.SCHEDULED + "'"
            }
            if(executionStatusList[0].spotfireExecutionStatus == ReportExecutionStatus.ERROR) {
                updateQuery = updateQuery + ", SPOTFIRE_EXECUTION_STATUS = '" + ReportExecutionStatus.SCHEDULED + "'"
            }
            updateQuery = updateQuery + " where id in (" + executionStatusList*.id.join(",") + ")"

        }
        log.info(updateQuery)
        try {
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateExecutedConfigurationList(List<ExecutedConfiguration> exConfigs, Integer count){
        try {
            String updateQuery = "update ex_rconfig set RUN_COUNT= " + (count + 1)  + " where id in (" + exConfigs*.id.join(",") + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateConfigurationList(List<ExecutedConfiguration> exConfigs, MasterConfiguration masterConfiguration){
        try {
            Date nextRunDate = masterConfiguration.nextRunDate
            String updateQuery = "update rconfig set"
            if(nextRunDate){
                updateQuery = updateQuery + " executing = 0, is_enabled = 1, next_run_date = " + "TO_DATE('${nextRunDate?new SimpleDateFormat(SqlGenerationService.DATETIME_FMT).format(nextRunDate):null}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
            } else {
                updateQuery = updateQuery + " next_run_date = " + null + ""
            }
            updateQuery = updateQuery + " where id in (" + exConfigs*.configId.join(",") + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateConfigurationAndExecutionStatus(Configuration configuration, ExecutionStatus executionStatus, Integer status) {
        Integer count
        if(configuration.masterConfigId){
            MasterConfiguration masterConfiguration = MasterConfiguration.get(configuration.masterConfigId)
            if(executionStatus.executionLevel >= 3){
                ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
                MasterExecutedConfiguration masterExecutedConfiguration = MasterExecutedConfiguration.get(executedConfiguration.masterExConfigId)
                ExecutionStatus masterExecutionStatus = ExecutionStatus.findByExecutedConfigId(masterExecutedConfiguration?.id)
                if(status == Constants.Commons.RESUME_REPORT){
                    masterExecutionStatus.reportExecutionStatus = ReportExecutionStatus.SCHEDULED
                } else if(status == Constants.Commons.RESUME_SPOTFIRE){
                    masterExecutionStatus.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
                }
                masterExecutionStatus.executionLevel = status
                masterExecutionStatus.save(flush:true)
                count = masterExecutedConfiguration.runCount?:0
                masterExecutedConfiguration.runCount = count + 1
                masterExecutedConfiguration.save()
                List<ExecutedConfiguration> allExConfigs = ExecutedConfiguration.findAllByMasterExConfigId(executedConfiguration.masterExConfigId)
                List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutedConfigIdInListAndExecutionLevel(allExConfigs*.id, executionStatus.executionLevel)
                List<ExecutedConfiguration> exConfigs = ExecutedConfiguration.findAllByIdInList(executionStatusList*.executedConfigId)
                updateExecutionStatusList(executionStatusList, status)
                updateExecutedConfigurationList(exConfigs, count)
                updateMasterConfig(masterConfiguration, true)
                updateConfigurationList(exConfigs, masterConfiguration)
            }
        } else {
            configuration.nextRunDate = new Date()
            configuration.isResume = true
            configuration.executing = false
            configuration.isEnabled = true
            configuration.save(flush: true)
        }
        configuration
    }

    Map getCompletedMilestonesMap(ExecutionStatus exStatus) {
        AlertType alertType
        AlertType.getAlertTypeList().each {
            if (it.value().equalsIgnoreCase(exStatus.type)) {
                alertType = it
                return it
            }
        }
        Map data = [ "alertType": messageSource.getMessage(alertType.getI18nValueForExecutionStatusDropDown(), null, Locale.default),
                    "timeStampJSON"     : exStatus.timeStampJSON,
                    "startTime"         : exStatus.startTime
        ]
        return data
    }

    String prepareHqlForIncompleteJob(def domainName) {
        String hqlForConfig = """
               Select ec from ExecutionStatus ec,${domainName.getSimpleName()} config 
                 where config.name = ec.name 
                 and config.isDeleted = false
                 and ec.executionStatus = 'GENERATING'
               """
        hqlForConfig
    }

    Date updateDateToLastDateOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.getTime()
    }


    /**
     Methods For test alert execution screen
     */
    List<Map> testExecutedReportConfigurationMap(List<ExecutionStatus> data, AlertType alertType) {
        List<Map> executedReportConfigurationList = []
        data.each {
            executedReportConfigurationList += getDataMapForTestExecution(it, alertType)
        }
        return executedReportConfigurationList
    }

    Map generateMapForTestAlertExecutionStatus(ExecutionStatusDTO executionStatusDTO) {
        switch (executionStatusDTO.sort) {
            case "runDate":
                executionStatusDTO.sort = "nextRunDate"
                break
            case "owner":
                executionStatusDTO.sort = "owner.fullName"
                break
        }
        String searchHQL = getSearchStringForExecutionStatus(executionStatusDTO.searchString)
        String hqlStatement = prepareHqlForExecutionStatus(executionStatusDTO, searchHQL)
        List<ExecutionStatus> executionStatusList = ExecutionStatus.executeQuery(hqlStatement, [offset: executionStatusDTO.offset, max: executionStatusDTO.max])
        Integer recordsTotal = ExecutionStatus.executeQuery(prepareCountHqlForExecutionStatus(executionStatusDTO, ''))[0] as Integer
        Integer recordsFilteredCount = ExecutionStatus.executeQuery(prepareCountHqlForExecutionStatus(executionStatusDTO, searchHQL))[0] as Integer
        [aaData: testExecutedReportConfigurationMap(executionStatusList, executionStatusDTO.alertType), recordsTotal: recordsTotal, recordsFiltered: recordsFilteredCount]
    }

    Map getDataMapForTestExecution(ExecutionStatus executionStatus, AlertType alertType) {
        ExecutedConfiguration ec = ExecutedConfiguration.get(executionStatus.executedConfigId as Long)
        String productSelection = ec?.productSelection ? ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT_GROUP)
        Map data = [id                     : executionStatus.configId,
                    executionStatusId      : executionStatus.id,
                    execConfigId           : executionStatus.executedConfigId,
                    name                   : executionStatus.name,
                    frequency              : executionStatus.frequency?.value(),
                    runDate                : executionStatus.nextRunDate,
                    executionTime          : executionStatus.executionTime,
                    owner                  : executionStatus.owner.fullName,
                    executionStatus        : executionStatus.executionStatus?.value() ?: ReportExecutionStatus.ERROR.value(),
                    reportExecutionStatus  : executionStatus.reportExecutionStatus?.value() ?: '-',
                    spotfireExecutionStatus: executionStatus.spotfireExecutionStatus?.value() ?: '-',
                    executionLevel         : executionStatus.executionLevel,
                    dateCreated            : executionStatus.dateCreated,
                    alertType              : messageSource.getMessage(alertType.getI18nValueForExecutionStatusDropDown(), null, Locale.default),
                    timeStampJSON          : executionStatus.timeStampJSON,
                    startTime              : executionStatus.startTime,
                    nodeName               : executionStatus.nodeName,
                    product                : productSelection?:"-",
                    peCount                : getTotalCountsForExecConfig(ec?.dispCounts)
        ]
        return data
    }

    private Integer getTotalCountsForExecConfig(String execDispCounts) {
        execDispCounts ? new JsonSlurper().parseText(execDispCounts).values().sum() : 0
    }

    List getModifiedDateRangeChanges(theInstance, AuditLogCategoryEnum auditLogCategoryEnum) {
        List modifiedDateRangeChanges = signalAuditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        if (modifiedDateRangeChanges.size() > 0) {
            Map dateRangeEnumMap = modifiedDateRangeChanges.find({ it.fieldName = "dateRangeEnum" })
            if (dateRangeEnumMap) {
                modifiedDateRangeChanges -= dateRangeEnumMap
                modifiedDateRangeChanges << [
                        fieldName    : "Date Range Type",
                        originalValue: dateRangeEnumMap.originalValue,
                        newValue     : dateRangeEnumMap.newValue
                ]

            }
            Map dateRangeEndMap = modifiedDateRangeChanges.find({ it.fieldName = "dateRangeEndAbsolute" })
            if (dateRangeEndMap) {
                modifiedDateRangeChanges -= dateRangeEndMap
                modifiedDateRangeChanges << [
                        fieldName    : "Date Range End",
                        originalValue: dateRangeEndMap.originalValue,
                        newValue     : dateRangeEndMap.newValue
                ]

            }

            Map dateRangeStartMap = modifiedDateRangeChanges.find({ it.fieldName = "dateRangeStartAbsolute" })
            if (dateRangeStartMap) {
                modifiedDateRangeChanges -= dateRangeStartMap
                modifiedDateRangeChanges << [
                        fieldName    : "Date Range Start",
                        originalValue: dateRangeStartMap.originalValue,
                        newValue     : dateRangeStartMap.newValue
                ]
            }
            Map relativeDateRangeValueMap = modifiedDateRangeChanges.find({ it.fieldName = "relativeDateRangeValue" })
            if (relativeDateRangeValueMap) {
                modifiedDateRangeChanges -= relativeDateRangeValueMap
                modifiedDateRangeChanges << [
                        fieldName    : "Date Range X Value",
                        originalValue: relativeDateRangeValueMap.originalValue,
                        newValue     : relativeDateRangeValueMap.newValue
                ]
            }

        }
        return modifiedDateRangeChanges
    }

    List getCreatedDateRangeChanges(theInstance) {
        List createdDateRangeChanges = []
        if (theInstance.dateRangeEnum != "" && theInstance.dateRangeEnum != null) {
            createdDateRangeChanges << [
                    fieldName    : "Date Range Type",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : theInstance.dateRangeEnum
            ]
        }

        if (theInstance.dateRangeEndAbsolute != "" && theInstance.dateRangeEndAbsolute != null) {
            createdDateRangeChanges << [
                    fieldName    : "Date Range End",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : theInstance.dateRangeEndAbsolute
            ]
        }

        if(theInstance.dateRangeStartAbsolute != "" && theInstance.dateRangeStartAbsolute != null){
            createdDateRangeChanges << [
                    fieldName    : "Date Range Start",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : theInstance.dateRangeStartAbsolute
            ]
        }

        if(theInstance.relativeDateRangeValue != "" && theInstance.relativeDateRangeValue != null){
            createdDateRangeChanges << [
                    fieldName    : "Date Range X Value",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : theInstance.relativeDateRangeValue
            ]
        }

        return createdDateRangeChanges
    }

    def detectChangesMade(theInstance, AuditLogCategoryEnum auditLogCategoryEnum) {
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
        try{
            List fieldsToIgnore = ['isPublic', 'isEnabled', 'totalExecutionTime', 'executing', 'numOfExecutions','productGroups',
                                   'nextRunDate', 'lastUpdated', 'spotfireSettings','scheduleDateJSON','productDictionarySelection','repeatExecution']
            List changesMade = signalAuditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
            if(AuditLogCategoryEnum.CREATED ==auditLogCategoryEnum){
                def emptyAssociations = changesMade.findAll {
                    it.newValue == '[]'
                }
                changesMade -= emptyAssociations
            }
            if(theInstance instanceof EvdasConfiguration){
                fieldsToIgnore.remove('nextRunDate')
            }
            if(theInstance instanceof Configuration){
                changesMade = getMiningAndForegroundChanges(theInstance, auditLogCategoryEnum, changesMade)
            }

            changesMade = getProductEventChanges(theInstance, auditLogCategoryEnum, changesMade)

            if (AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum && theInstance.shareWithUser.dirty) {
                String originalValue = (theInstance.shareWithUser?.getStoredSnapshot()?.collect { it.value }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
                String newValue = (theInstance.shareWithUser?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
                if (newValue != originalValue) {
                    changesMade << [
                            fieldName    : "Share With User",
                            originalValue: originalValue,
                            newValue     : newValue
                    ]
                }
            } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.shareWithUser?.size() > 0) {
                Map shareWithUserMap = changesMade.find { it.fieldName == 'shareWithUser' }
                if (shareWithUserMap) {
                    changesMade -= shareWithUserMap
                }
                changesMade << [
                        fieldName    : "Share With User",
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : (theInstance.shareWithUser?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
                ]
            }

            if (AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum && theInstance.shareWithGroup.dirty) {
                String originalValue = (theInstance.shareWithGroup?.getStoredSnapshot()?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
                String newValue = (theInstance.shareWithGroup?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
                if (newValue != originalValue) {
                    changesMade << [
                            fieldName    : "Share With Group",
                            originalValue: originalValue,
                            newValue     : newValue
                    ]
                }
            } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.shareWithGroup?.size() > 0) {
                Map shareWithGroupMap = changesMade.find { it.fieldName == 'shareWithGroup' }
                if (shareWithGroupMap) {
                    changesMade -= shareWithGroupMap
                }
                changesMade << [
                        fieldName    : "Share With Group",
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : (theInstance.shareWithGroup?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
                ]
            }
            if (auditLogCategoryEnum == AuditLogCategoryEnum.MODIFIED) {
                List modifiedDateRangeChanges = []
                if (theInstance instanceof Configuration && theInstance.alertDateRangeInformation.dirty) {
                    modifiedDateRangeChanges = getModifiedDateRangeChanges(theInstance.alertDateRangeInformation, auditLogCategoryEnum)
                } else if (theInstance instanceof EvdasConfiguration && theInstance.dateRangeInformation.dirty) {
                    modifiedDateRangeChanges = getModifiedDateRangeChanges(theInstance.dateRangeInformation, auditLogCategoryEnum)
                }
                changesMade.addAll(modifiedDateRangeChanges)
            } else if (auditLogCategoryEnum == AuditLogCategoryEnum.CREATED) {
                List createdDateRangeChanges = []
                if (theInstance instanceof Configuration && theInstance.alertDateRangeInformation) {
                    createdDateRangeChanges = getCreatedDateRangeChanges(theInstance.alertDateRangeInformation)
                } else if (theInstance instanceof EvdasConfiguration && theInstance.dateRangeInformation) {
                    createdDateRangeChanges = getCreatedDateRangeChanges(theInstance.dateRangeInformation)
                }
                changesMade.addAll(createdDateRangeChanges)
            }
        if (theInstance instanceof Configuration && AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance?.drugType) {
                Map drugType = changesMade.find { it.fieldName == 'drugType' }
                if (drugType) {
                    changesMade -= drugType
                }
                List drugTypeValue = []
                    theInstance.drugType?.split(',').each {
                        drugTypeValue +=  ProductTypeConfiguration.get(it)?.name ?: ""
                    }
                changesMade << [
                        fieldName    : messageSource.getMessage('app.reportField.productDrugType', null, locale),
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : drugTypeValue?.join(", ")
                ]
            }
            changesMade.removeAll({ it.fieldName in fieldsToIgnore })
            changesMade
        }catch(Exception ex){
            ex.printStackTrace()
        }

    }


    def getMiningVariableValue(def miningVariable,def miningVariableValue){
        def valuesMap=miningVariableValue ? JSON.parse(miningVariableValue): null
        String dataMiningVariableValue = valuesMap?.value ?: Constants.Commons.BLANK_STRING
        String dataMiningVariableOperator = valuesMap?.operatorDisplay ?: Constants.Commons.BLANK_STRING
        String finalValue = aggregateCaseAlertService.getDmvData(miningVariable, dataMiningVariableValue, dataMiningVariableOperator)
        return  finalValue
    }

    def getForegroundSearchAtt(String data){
        def parsedData=""
        try{
            JsonSlurper jsonSlurper = new JsonSlurper()
            parsedData = jsonSlurper.parseText(data).collect { item ->
                def label = item.label
                def text = item.text ?: ""
                "${label} :${text}"
            }.findAll { it != null }.join(" ,")
            return parsedData as String
        }catch(Exception ex){
            ex.printStackTrace()
            return ""
        }

    }

    def getProductEventChanges(theInstance,auditLogCategoryEnum,changesMade){
        if (changesMade.find { it.fieldName == 'productSelection' } != null && AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum) {
            String originalValue = theInstance.getPersistentValue("productSelection") ? ViewHelper.getDictionaryValues(theInstance.getPersistentValue("productSelection"), DictionaryTypeEnum.PRODUCT) : ""
            String newValue = theInstance.productSelection ? ViewHelper.getDictionaryValues(theInstance.productSelection, DictionaryTypeEnum.PRODUCT) : Constants.AuditLog.EMPTY_VALUE
            def productMap = changesMade.find { it.fieldName == 'productSelection' }
            if (productMap) {
                changesMade -= productMap
            }

            if (newValue != originalValue) {
                changesMade << [
                        fieldName    : "Product Selection",
                        originalValue: originalValue,
                        newValue     : newValue
                ]
            }
        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.productSelection?.length()>0) {
            String newValue = theInstance.productSelection ? ViewHelper.getDictionaryValues(theInstance.productSelection, DictionaryTypeEnum.PRODUCT) : Constants.AuditLog.EMPTY_VALUE
            def productMap = changesMade.find { it.fieldName == 'productSelection' }
            if (productMap) {
                changesMade -= productMap
            }
            if(newValue !='[]'){
                changesMade << [
                        fieldName    : "Product Selection",
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : newValue
                ]
            }


        }

        if (changesMade.find { it.fieldName == 'productGroupSelection' } != null && AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum) {
            String originalValue = theInstance.getPersistentValue("productGroupSelection") ? ViewHelper.getDictionaryValues(theInstance.getPersistentValue("productGroupSelection"), DictionaryTypeEnum.PRODUCT_GROUP) : ""
            String newValue = theInstance.productGroupSelection ? ViewHelper.getDictionaryValues(theInstance.productGroupSelection, DictionaryTypeEnum.PRODUCT_GROUP) : Constants.AuditLog.EMPTY_VALUE
            def productGroupMap = changesMade.find { it.fieldName == 'productGroupSelection' }
            if (productGroupMap) {
                changesMade -= productGroupMap
            }
            if (newValue != originalValue) {
                changesMade << [
                        fieldName    : "Product group selection",
                        originalValue: originalValue,
                        newValue     : newValue
                ]
            }
        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.productGroupSelection?.length()>0) {
            String newValue = theInstance.productGroupSelection ? ViewHelper.getDictionaryValues(theInstance.productGroupSelection, DictionaryTypeEnum.PRODUCT_GROUP) : Constants.AuditLog.EMPTY_VALUE
            def productMap = changesMade.find { it.fieldName == 'productGroupSelection' }
            if (productMap) {
                changesMade -= productMap
            }
            if(newValue !='[]'){
                changesMade << [
                        fieldName    : "Product Group Selection",
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : newValue
                ]
            }
        }

        if (changesMade.find { it.fieldName == 'eventSelection' } != null && AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum) {
            String originalValue = theInstance.getPersistentValue("eventSelection") ? ViewHelper.getDictionaryValues(theInstance.getPersistentValue("eventSelection"), DictionaryTypeEnum.EVENT) : ""
            String newValue = theInstance.eventSelection ? ViewHelper.getDictionaryValues(theInstance.eventSelection, DictionaryTypeEnum.EVENT) : Constants.AuditLog.EMPTY_VALUE
            def productMap = changesMade.find { it.fieldName == 'eventSelection' }
            if (productMap) {
                changesMade -= productMap
            }
            if (newValue != originalValue) {
                changesMade << [
                        fieldName    : "Event Selection",
                        originalValue: originalValue,
                        newValue     : newValue
                ]
            }
        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.eventSelection?.length()>0) {
            String newValue = theInstance.eventSelection ? ViewHelper.getDictionaryValues(theInstance.eventSelection, DictionaryTypeEnum.EVENT) : Constants.AuditLog.EMPTY_VALUE
            def productMap = changesMade.find { it.fieldName == 'eventSelection' }
            if (productMap) {
                changesMade -= productMap
            }
            if (newValue != '[]'){
                changesMade << [
                        fieldName    : "Event Selection",
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : newValue
                ]
            }

        }

        if (changesMade.find { it.fieldName == 'eventGroupSelection' } != null && AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum) {
            String originalValue = theInstance.getPersistentValue("eventGroupSelection") ? ViewHelper.getDictionaryValues(theInstance.getPersistentValue("eventGroupSelection"), DictionaryTypeEnum.EVENT_GROUP) : ""
            String newValue = theInstance.eventGroupSelection ? ViewHelper.getDictionaryValues(theInstance.eventGroupSelection, DictionaryTypeEnum.EVENT_GROUP) : Constants.AuditLog.EMPTY_VALUE
            def productGroupMap = changesMade.find { it.fieldName == 'eventGroupSelection' }
            if (productGroupMap) {
                changesMade -= productGroupMap
            }
            if (newValue != originalValue) {
                changesMade << [
                        fieldName    : "Event group selection",
                        originalValue: originalValue,
                        newValue     : newValue
                ]
            }
        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.eventGroupSelection?.length()>0) {
            String newValue = theInstance.eventGroupSelection ? ViewHelper.getDictionaryValues(theInstance.eventGroupSelection, DictionaryTypeEnum.EVENT_GROUP) : Constants.AuditLog.EMPTY_VALUE
            def productMap = changesMade.find { it.fieldName == 'eventGroupSelection' }
            if (productMap) {
                changesMade -= productMap
            }

            if(newValue!='[]'){
                changesMade << [
                        fieldName    : "Event Group Selection",
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : newValue
                ]
            }

        }

        return changesMade
    }

    def getMiningAndForegroundChanges(theInstance,auditLogCategoryEnum,changesMade){

        if (changesMade.find { it.fieldName == 'dataMiningVariableValue' } != null && AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum) {
            String originalValue = theInstance.getPersistentValue("dataMiningVariableValue") ?: Constants.AuditLog.EMPTY_VALUE
            String oldMiningVariable=theInstance.getPersistentValue("dataMiningVariable") ?: ""
            String newValue = theInstance.dataMiningVariableValue ?: Constants.AuditLog.EMPTY_VALUE

            def valueMap = changesMade.find { it.fieldName == 'dataMiningVariableValue' }
            if (valueMap) {
                changesMade -= valueMap
            }
            if (newValue != originalValue) {
                changesMade << [
                        fieldName    : "Data mining variable value",
                        originalValue: getMiningVariableValue(oldMiningVariable,originalValue),
                        newValue     : getMiningVariableValue(theInstance?.dataMiningVariable,theInstance?.dataMiningVariableValue)
                ]
            }

        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.dataMiningVariableValue?.length()>0) {
            String newValue = getMiningVariableValue(theInstance.dataMiningVariable,theInstance.dataMiningVariableValue) ?: Constants.AuditLog.EMPTY_VALUE
            def valueMap = changesMade.find { it.fieldName == 'dataMiningVariableValue' }
            if (valueMap) {
                changesMade -= valueMap
            }
            if(newValue!=null && newValue!=""){
                changesMade << [
                        fieldName    : "Data mining variable value",
                        originalValue: Constants.AuditLog.EMPTY_VALUE,
                        newValue     : getMiningVariableValue(theInstance.dataMiningVariable,newValue),
                ]
            }

        }

        if (changesMade.find { it.fieldName == 'foregroundSearchAttr' } != null && AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum) {
            String originalValue = theInstance.getPersistentValue("foregroundSearchAttr") ?: Constants.AuditLog.EMPTY_VALUE
            String newValue = theInstance.foregroundSearchAttr ?: Constants.AuditLog.EMPTY_VALUE

            def valueMap = changesMade.find { it.fieldName == 'foregroundSearchAttr' }
            if (valueMap) {
                changesMade -= valueMap
            }
            if (newValue != originalValue) {
                changesMade << [
                        fieldName    : "Foregroung search attributes",
                        originalValue: getForegroundSearchAtt(originalValue),
                        newValue     : getForegroundSearchAtt(newValue)
                ]
            }

        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.foregroundSearchAttr?.length()>0) {
            String newValue = theInstance.foregroundSearchAttr ?: Constants.AuditLog.EMPTY_VALUE
            def valueMap = changesMade.find { it.fieldName == 'foregroundSearchAttr' }
            if (valueMap) {
                changesMade -= valueMap
            }
            changesMade << [
                    fieldName    : "Foreground Search attributes",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : getForegroundSearchAtt(newValue),
            ]
        }

        return changesMade

    }

    Map getDateRangeForPublicDS(ExecutedConfiguration executedConfiguration) {
        def resultMap = [:]
        try {
            Boolean isStandalone = false
            String faersDateRange = executedConfiguration?.faersDateRange
            String vaersDateRange = executedConfiguration?.vaersDateRange
            String vigibaseDateRange = executedConfiguration?.vigibaseDateRange
            String evdasDateRange = executedConfiguration?.evdasDateRange
            String dateRange = Constants.Commons.BLANK_STRING

            dateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
            String dateRangeStart = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute);
            if (executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                String dateRangeEnd;
                if (executedConfiguration?.selectedDatasource == Constants.DataSource.FAERS) {
                    isStandalone=true
                    dateRangeEnd = reportExecutorService.getFaersDateRange().faersDate.substring(13);
                    dateRange = dateRangeStart + " - " + dateRangeEnd;
                    faersDateRange = dateRange
                } else if (executedConfiguration?.selectedDatasource == Constants.DataSource.VAERS) {
                    isStandalone = true
                    //Updated Vaers Date Range for Integrated Alert PVS-67898
                    dateRangeEnd = reportExecutorService.getVaersDateRange().vaersDate.substring(Constants.Commons.VAERS_END_DATE_INDEX);
                    dateRange = dateRangeStart + " - " + dateRangeEnd;
                    vaersDateRange = dateRange
                } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.FAERS)) {
                    faersDateRange = dateRangeStart + " - " + reportExecutorService.getFaersDateRange().faersDate.substring(13);
                } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VAERS)) {
                    //Updated Vaers Date Range for Integrated Alert PVS-67898
                    vaersDateRange = dateRangeStart + " - " + reportExecutorService.getVaersDateRange().vaersDate.substring(Constants.Commons.VAERS_END_DATE_INDEX);
                } else {
                    dateRangeEnd = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute);
                    dateRange = dateRangeStart + " - " + dateRangeEnd;
                }
                if(executedConfiguration?.selectedDatasource == Constants.DataSource.VIGIBASE){
                    isStandalone=true
                    dateRange = dateRangeStart + " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
                    vigibaseDateRange = dateRange
                } else if(executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VIGIBASE)){
                    vigibaseDateRange = dateRangeStart + " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
                } else if(executedConfiguration?.selectedDatasource.contains(Constants.DataSource.JADER)){
                    isStandalone=true //As per PO comment jader is always standalone
                    dateRange = dateRangeStart + " - " + (jaderExecutorService.getJaderDateRange().jaderDate).substring(13)
                }
            }
            if (executedConfiguration?.getDataSource(executedConfiguration?.selectedDatasource).contains("EVDAS")) {
                List idList = []
                def jsonObj = aggregateCaseAlertService.parseJsonString(executedConfiguration.productGroupSelection)
                if (jsonObj) {
                    jsonObj?.each {
                        idList?.add(it?.id)
                    }
                }
                evdasDateRange = reportExecutorService.getEvdasDateRange(idList)
            }
            if (isStandalone) {
                resultMap = ["Date Range": dateRange]
            } else {
                resultMap = ["FAERS Date Range"   : faersDateRange,
                             "VAERS Date Range"   : vaersDateRange,
                             "VigiBase Date Range": vigibaseDateRange,
                             "EVDAS Date Range"   : evdasDateRange,
                             "Date Range"         : dateRange]

            }
            

            resultMap.entrySet().removeIf { it.value == null }
            return resultMap
        } catch (Exception ex) {
            ex.printStackTrace()
            return resultMap
        }

    }


    void pushCustomAuditForUnscheduling(def configInstance,String entityName) {
        //This entry will automatically merge with plugin entry if any other changes also done during unscheduling due to same transaction and instance properties
        signalAuditLogService.createAuditLog([
                entityName : entityName,
                moduleName : configInstance?.getModuleNameForMultiUseDomains(),
                category   : AuditTrail.Category.UPDATE.toString(),
                entityValue: configInstance.getInstanceIdentifierForAuditLog(),
                entityId   : configInstance?.id.toString(),
                username   : userService.getUser().username,
                fullname   : userService.getUser().fullName
        ] as Map, [[propertyName: "Unscheduled", oldValue: "", newValue: "Yes"]] as List)

    }
    String encodeId(Long id){
        return Base64.encoder.encodeToString(id.toString().getBytes("UTF-8"))
    }
    Long decodeId(String encodedId){
        return new String(Base64.decoder.decode(encodedId), "UTF-8").toLong()
    }
}