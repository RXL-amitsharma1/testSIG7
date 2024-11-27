package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.LastReviewDurationDTO
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertAsyncUtil
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.async.Promise
import grails.converters.JSON
import groovy.sql.GroovyResultSet
import org.apache.http.util.TextUtils
import org.grails.datastore.mapping.query.*
import com.rxlogix.util.ViewHelper
import grails.async.PromiseList
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSetExtension
import groovy.sql.OutParameter
import groovy.sql.Sql
import groovy.time.TimeCategory
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import oracle.jdbc.OracleTypes
import org.grails.datastore.mapping.query.Query
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.jdbc.Work
import org.hibernate.sql.JoinType
import org.hibernate.type.LongType
import org.joda.time.DateTimeZone
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import java.sql.Clob
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import static grails.async.Promises.task

import static com.rxlogix.util.MiscUtil.calcDueDate

class EvdasAlertService implements AlertUtil, LinkHelper,AlertAsyncUtil, EventPublisher {

    static transactional = false

    def CRUDService
    def grailsApplication
    def evdasHistoryService
    def emailService
    def actionTemplateService
    def medicalConceptsService
    def validatedSignalService
    def userService
    def activityService
    def evdasAlertService
    def messageSource
    def businessConfigurationService
    def dataSource_eudra
    def cacheService
    def dataObjectService
    def sessionFactory
    def dataSource
    def productBasedSecurityService
    def actionService
    def alertService
    def alertCommentService
    EmailNotificationService emailNotificationService
    def signalExecutorService
    def archiveService
    ViewInstanceService viewInstanceService
    CustomMessageService customMessageService
    def emergingIssueService
    def dynamicReportService
    def attachmentableService
    def dataSheetService
    def aggregateCaseAlertService
    def undoableDispositionService
    def signalAuditLogService
    def signalDataSourceService

    SimpleDateFormat dateWriteFormat = new SimpleDateFormat('dd-MMM-yyyy')

    private setFlagsForAlert(List<Map> alertData, Long configId) {
        log.info("Setting Flags for alert")
        Map selectedEvdasAlertMap = [:]
        List<Map> previousEvdasAlertList = EvdasAlert.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("substance", "substance")
                property("ptCode", "ptCode")
                'disposition' {
                    property('id', 'dispositionId')
                }
            }
            eq("alertConfiguration.id", configId)
            order("id","desc")
        } as List<Map>

        List<Map> previousArchiveEvdasAlertList = ArchivedEvdasAlert.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("substance", "substance")
                property("ptCode", "ptCode")
                'disposition' {
                    property('id', 'dispositionId')
                }
            }
            eq("alertConfiguration.id", configId)
            order("id","desc")
        } as List<Map>

        previousEvdasAlertList.addAll(previousArchiveEvdasAlertList)

        List<Long> reviewedDispositions = cacheService.getDispositionByReviewCompleted()*.id
        alertData.each {alert ->
            Map previousEvdasAlert = previousEvdasAlertList.find {
                it.substance == alert.substance && it.ptCode == alert.ptCode.toInteger()
            }
            if (previousEvdasAlert && previousEvdasAlert.dispositionId in reviewedDispositions) {
                selectedEvdasAlertMap.put(previousEvdasAlert.substance + "-" + previousEvdasAlert.ptCode, Constants.Badges.PREVIOUSLY_REVIEWED)
            } else if (!previousEvdasAlert) {
                selectedEvdasAlertMap.put(alert.substance + "-" + alert.ptCode.toInteger(), Constants.Badges.NEW)
            }
        }
        log.info("Flags set")
        selectedEvdasAlertMap
    }

    private getPreviousAlertMap(ExecutedEvdasConfiguration previousExecutedConfig) {
        Map previousAlertMap = [:]
        List<EvdasAlert> prevEvdasAlertList = EvdasAlert.findAllByExecutedAlertConfiguration(previousExecutedConfig)
        for(EvdasAlert evdasAlert : prevEvdasAlertList) {
            previousAlertMap.put(evdasAlert.substance + "-" + evdasAlert.pt, evdasAlert)
        }
        previousAlertMap
    }

    /**
     * Method to batch persist the aggregate alerts.
     * @param alertList
     * @param config
     */
    @Transactional
    void batchPersistEvdasAlert(List<EvdasAlert> alertList, EvdasConfiguration evdasConfiguration,
                                                  ExecutedEvdasConfiguration executedConfig, String defaultDispositionValue) {

        ExecutedEvdasConfiguration previousExecutedEvdasConfig = fetchLastExecutionOfAlert(executedConfig)
        Map<String, EvdasAlert> previousAlertMap = getPreviousAlertMap(previousExecutedEvdasConfig)
        String alertProductSelection = executedConfig.productSelection
        List impEventList = businessConfigurationService.generateImpEventList()

        List<EvdasAlert> evdAlertList = []
        ExecutorService executorService = signalExecutorService.threadPoolForQuantAlertExec()
        if (!evdasConfiguration.adhocRun) {
            StringBuffer logStringBuilder = new StringBuffer()
            logStringBuilder.append("\nApplying business rule for ${executedConfig.id}")
            alertList.collate(500).each { List<EvdasAlert> evdasAlertList ->
                List<Future<EvdasAlert>> futureList = evdasAlertList.collect { EvdasAlert evdasAlert ->
                    executorService.submit({ ->
                        businessConfigurationService.executeRulesForEvdasAlert(evdasAlert,
                                evdasConfiguration.productSelection, alertProductSelection, defaultDispositionValue, previousAlertMap, impEventList,logStringBuilder)

                        evdasAlert
                    } as Callable)
                }
                futureList.each {
                    evdAlertList.add(it.get())
                }
            }
            Promise promise = task {
                alertService.saveLogsInFile(logStringBuilder,"${evdasConfiguration.id}_${'Evdas Alert'}")
            }
            promise.onError {
                log.error("BR Log File Processing Failed.")
            }
            promise.onComplete {
                log.info("BR Log File Processing Finished.")
            }
        }

        EvdasAlert.withTransaction {
            def batch = []
            for (EvdasAlert alert : evdAlertList) {
                batch += alert
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (EvdasAlert alertIntance in batch) {
                        //Validate false is required to make sure that additional grails related check is not added to db.
                        alertIntance.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (EvdasAlert alertIntance in batch) {
                        alertIntance.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("Alert data is batch persisted.")
        }
    }

    /**
     * Method to batch persist the aggregate alert data and its history, activity and updates the other agg alerts.
     * @param data
     */
    @Transactional
    void batchPersistData(List<EvdasAlert> alertList, ExecutedEvdasConfiguration executedConfig,
                          EvdasConfiguration config, String defaultDispositionValue, Long prevExecConfigId) {
        def time1 = System.currentTimeMillis()
        log.info("Now persisting the execution related data in a batch.")

        //Persist the alerts
        batchPersistEvdasAlert(alertList, config, executedConfig, defaultDispositionValue)
        if (!config.adhocRun && prevExecConfigId) {
            log.info("Now Saving the Actions")
            Session session = sessionFactory.currentSession
            insertEvdasAlertActions(session, executedConfig.id, prevExecConfigId)
        }
        log.info("Now Saving the Audit log for Business Rule Actions")
        signalAuditLogService.saveAuditTrailForBusinessRuleActions(executedConfig.id)
        log.info("Saving the Audit log for Business Rule Actions Completed")

        //Persist the history
        List<Map> peHistoryMapList = dataObjectService.getEvdasBusinessConfigPropertiesMapList(executedConfig.id)
         peHistoryMapList.each {
             EvdasAlert evdAlert = EvdasAlert.findByExecutedAlertConfigurationAndSubstanceAndPt(it.execConfigId,it.productName,it.eventName)
            if (!it.evdasAlertId) {
                it.evdasAlertId = evdAlert?.id
            }
        }
        evdasHistoryService.batchPersistHistory(peHistoryMapList)
        dataObjectService.clearEvdasBusinessConfigPropertiesMap(executedConfig.id)
        //Clearing last review maps
        dataObjectService.clearCurrentEndDateMap(executedConfig.id)
        dataObjectService.clearLastReviewDurationMap(executedConfig.id)

        //Persist the activities
        activityService.setEvdasActivities(executedConfig)

        if (!config.adhocRun) {
            persistValidatedSignalWithEvdasCaseAlert(executedConfig.id, config.id, config.name)
        }
        log.info("Persistance of execution related data in a batch is done.")
        def time2 = System.currentTimeMillis()
        log.info(((time2 - time1) / 1000) + " Secs were taken in the persistance of data for configuration " + executedConfig.name)
    }

    void persistValidatedSignalWithEvdasCaseAlert(Long executedConfigId, Long configId, String name) {
        List<EvdasAlert> attachSignalAlertList = getAttachSignalAlertList(executedConfigId)
        List<Long> prevExecConfigIdList = ExecutedEvdasConfiguration.createCriteria().list {
            projections {
                property('id')
            }
            eq("name", name)
            'not' {
                'eq'("id", executedConfigId)
            }
            order("id", "desc")
            maxResults(1)
        }

        if (attachSignalAlertList.size() > 0) {
            log.info("Now saving the signal across the PE.")
            List<Map<String, String>> alertIdAndSignalIdList = getAlertIdAndSignalIdForBusinessConfig(executedConfigId, configId, attachSignalAlertList)
            List<Map<String, String>> autoAlertIdAndSignalIdList = getAutoAlertIdAndSignalIdForBusinessConfig(executedConfigId, attachSignalAlertList)
            List<String> substanceIdAndPtCodeList = attachSignalAlertList.collect {
                "(" + it.substanceId + "," + it.ptCode + ")"
            }

            Session session = sessionFactory.currentSession
            if (prevExecConfigIdList.size() > 0) {
                log.info("Executing SQL query for signals for previous executions")
                String sql_statement = SignalQueryHelper.signal_alert_ids_evdas(substanceIdAndPtCodeList.join(","), executedConfigId, prevExecConfigIdList[0])
                log.info(sql_statement)
                SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
                sqlQuery.list().each { row ->
                    alertIdAndSignalIdList.add([col2: row[0].toString(), col1: row[1].toString(), col3: '1', autoRouted: '0'])
                }
            }
            if(autoAlertIdAndSignalIdList){
                autoAlertIdAndSignalIdList.each{
                    if(it.col1 != null && it.col2 != null) {
                        it.col2?.each { signal ->
                            if (alertIdAndSignalIdList.contains([col2: it.col1, col1: signal.toString(), col3: '1', autoRouted: '0'])) {
                                alertIdAndSignalIdList.remove([col2: it.col1, col1: signal.toString(), col3: '1', autoRouted: '0'])
                            }
                            alertIdAndSignalIdList.add([col2: it.col1, col1: signal.toString(), col3: '0', autoRouted: '1'])
                        }
                    }
                }
            }
            alertIdAndSignalIdList = alertIdAndSignalIdList.unique {
                [it.col2, it.col1]
            }
            log.info("Batch execution of TABLE VALIDATED_EVDAS_ALERTS started")
            String insertValidatedQuery = "INSERT INTO VALIDATED_EVDAS_ALERTS(VALIDATED_SIGNAL_ID,EVDAS_ALERT_ID,IS_CARRY_FORWARD,DATE_CREATED,AUTO_ROUTED) VALUES(?,?,?,?,?)"
//            alertService.batchPersistForMapping(session, alertIdAndSignalIdList,insertValidatedQuery)
            session.doWork(new Work() {
                public void execute(Connection connection) throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedQuery)
                    def batchSize = Holders.config.signal.batch.size
                    int count = 0
                    try {
                        alertIdAndSignalIdList.each {
                            preparedStatement.setLong(1, it.col1.toLong())
                            preparedStatement.setLong(2, it.col2.toLong())
                            preparedStatement.setBoolean(3, it.col3.toBoolean())
                            preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()))
                            preparedStatement.setBoolean(5, it.autoRouted.toBoolean())
                            preparedStatement.addBatch()
                            count += 1
                            if (count == batchSize) {
                                preparedStatement.executeBatch()
                                count = 0
                            }
                        }
                        preparedStatement.executeBatch()
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        preparedStatement.close()
                        session.flush()
                        session.clear()
                    }
                }
            })
            log.info("Signal are saved across the system.")
        }

    }


    List<EvdasAlert> getAttachSignalAlertList(Long executedConfigId) {
        List<EvdasAlert> attachSignalAlertList = EvdasAlert.createCriteria().list {
            eq("executedAlertConfiguration.id", executedConfigId)
            'disposition' {
                eq("validatedConfirmed", true)
            }
            createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
            isNull('vs.id')
        } as List<EvdasAlert>
        attachSignalAlertList
    }

    void bindDatasheetData(EvdasConfiguration configuration, List dataSheets) {
        Map dataSheetMap = [:]
        String [] dataSheetArr = []
        if (dataSheets) {
            dataSheets?.each { dataSheet ->
                dataSheetArr = dataSheet.split (Constants.DatasheetOptions.SEPARATOR)
                dataSheetMap.put(dataSheetArr[1], dataSheetArr[0])
            }
            configuration.selectedDataSheet = dataSheetMap as JSON
        }

    }

    List<Map<String, String>> getAlertIdAndSignalIdForBusinessConfig(Long executedConfigId, Long configId, List<EvdasAlert> attachSignalAlertList) {
        List<Map> signalAlertMap = dataObjectService.getSignalAlertMapForEvdasAlert(executedConfigId)
        List<Map<String, String>> alertIdAndSignalIdList = []
        List<Activity> signalActivityList = []
        if (signalAlertMap) {
            Disposition defaultDisposition = EvdasConfiguration.get(configId).getWorkflowGroup()?.defaultSignalDisposition
            List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions
            boolean isValidatedDate=validatedDateDispositions.contains(defaultDisposition.value)
            ValidatedSignal validatedSignal = validatedSignalService.createSignalForBusinessConfiguration(signalAlertMap[0].signalName, signalAlertMap[0].alert, Constants.AlertConfigType.EVDAS_ALERT, defaultDisposition, signalAlertMap[0].signalId ? signalAlertMap[0].signalId as Long : null, isValidatedDate)
            signalAlertMap.each { Map map ->
                Long alertId = attachSignalAlertList.find {
                    map.alert.substanceId == it.substanceId && map.alert.ptCode == it.ptCode
                }?.id
                if (alertId) {
                    alertIdAndSignalIdList.add([col2: alertId.toString(), col1: validatedSignal.id.toString(), col3: '0', autoRouted: '0'])
                    signalActivityList.add(activityService.createActivityForSignalBusinessConfigEvdas(map.alert.substance, map.alert.pt,validatedSignal.name, map.alert.name))
                }
            }
            dataObjectService.clearSignalAlertMapForEvdasAlert(executedConfigId)
            alertService.persistActivitesForSignal(validatedSignal.id, signalActivityList)
        }
        alertIdAndSignalIdList
    }

    List<Map<String, String>> getAutoAlertIdAndSignalIdForBusinessConfig(Long executedConfigId, List<EvdasAlert> attachSignalAlertList) {
        Map<Long,List<Map>> autoAlertIdAndSignalIdList = dataObjectService.getAlertIdSignalListMapEvdasAlert(executedConfigId)
        List<Map> autoAlertSignalIdList = []
        Map<Long,List[]> signalActivityMap = [:]
        if (autoAlertIdAndSignalIdList) {
            autoAlertIdAndSignalIdList.each { ruleId, idMap ->
                boolean associateMultipleSignal
                String ruleJSON = RuleInformation.get(ruleId)?.ruleJSON
                JsonSlurper jsonSlurper = new JsonSlurper()
                Map ruleMap = jsonSlurper.parseText(ruleJSON)
                List expressionList = ruleMap?.all?.containerGroups
                associateMultipleSignal = Boolean.parseBoolean(expressionList[0]?.expressions[0]?.assMultSignal)
                Map<Long, List> alertIdAllSignalListMap = [:]
                idMap.each { map ->
                    Long alertId = attachSignalAlertList.find {
                        map.alert.substanceId == it.substanceId && map.alert.ptCode == it.ptCode
                    }?.id
                    if (alertIdAllSignalListMap.get(alertId)) {
                        List tempList = alertIdAllSignalListMap.get(alertId)
                        tempList += map.signalList
                        alertIdAllSignalListMap.put(alertId, tempList)
                    } else {
                        alertIdAllSignalListMap.put(alertId, map.signalList)
                    }
                }
                alertIdAllSignalListMap.each { alertId, signalList ->
                    if (!associateMultipleSignal && signalList.size() > 1) {
                        List signalListLastDisp = signalList.sort { a, b -> ValidatedSignal.get(b).lastDispChange <=> ValidatedSignal.get(a).lastDispChange }
                        signalList = signalListLastDisp[0]
                    }
                    autoAlertSignalIdList.add([col1: alertId.toString(), col2: signalList])
                    EvdasAlert evdasAlert = EvdasAlert.get(alertId)
                    signalList?.each { signalId ->
                        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
                        Activity signalActivity = activityService.createActivityForSignalBusinessConfigEvdas(evdasAlert.substance,evdasAlert.pt, validatedSignal.name, evdasAlert.name)
                        if (signalActivityMap.get(signalId)) {
                            signalActivityMap.get(signalId)?.add(signalActivity)
                        } else {
                            signalActivityMap.put(signalId, [])
                            signalActivityMap.get(signalId)?.add(signalActivity)
                        }
                    }
                }
            }
            dataObjectService.clearAggSignalAlertMapForSignalState(executedConfigId)
            dataObjectService.clearAlertIdSignalListMapEvdasAlert(executedConfigId)
            alertService.persistActivitiesForAutoRouteSignal(signalActivityMap)
        }
        autoAlertSignalIdList
    }



    private printExecutionMessage(executedConfig, config, alertData) {
        def executionMessage = "Execution of Configuration took ${executedConfig.totalExecutionTime}ms for evdas configuration ${config.name} [C:${config.id}, EC: ${executedConfig.id}]. It gave ${alertData ? alertData.size() : 0} PE combinations"
        log.info(executionMessage)
        log.info("Alert data save flow is complete.")
    }

    void saveDispositionAlertCaseHistory(EvdasAlert alert, EvdasHistory caseHistory){
        alert.justification = caseHistory.justification
        alert.dispLastChange = Objects.nonNull(caseHistory.createdTimestamp) ? caseHistory.createdTimestamp : caseHistory.lastUpdated
        String userName = caseHistory.modifiedBy? (caseHistory.modifiedBy == Constants.Commons.SYSTEM ? Constants.SYSTEM_USER: caseHistory.modifiedBy)
                : caseHistory.createdBy == Constants.Commons.SYSTEM ? Constants.SYSTEM_USER: caseHistory.createdBy
        alert.dispPerformedBy = cacheService.getUserByUserNameIlike(userName)?.fullName?:userName
    }

    private setWorkflowMgmtStates(EvdasAlert evdasAlert, EvdasConfiguration config) {

        EvdasHistory existingProductEventHistory = dataObjectService.getEvdasHistoryByConfigId(evdasAlert.substance, evdasAlert.pt, config.id)
        EvdasHistory existingLatestDispositionPEHistory = dataObjectService.getLatestDispositionEvdasHistory(evdasAlert.substance, evdasAlert.pt, config.id)
        try {
            if(existingLatestDispositionPEHistory){
                saveDispositionAlertCaseHistory(evdasAlert,existingLatestDispositionPEHistory)
            }
                if (existingProductEventHistory) {
                    evdasAlert.priority = cacheService.getPriorityByValue(existingProductEventHistory.priorityId)
                    evdasAlert.disposition = cacheService.getDispositionByValue(existingProductEventHistory.dispositionId)
                    if (config.assignedTo) {
                        evdasAlert.assignedTo = cacheService.getUserByUserId(existingProductEventHistory.assignedToId)
                    } else {
                        evdasAlert.assignedToGroup = cacheService.getGroupByGroupId(existingProductEventHistory.assignedToGroupId)
                    }
                    evdasAlert.dueDate = existingProductEventHistory.dueDate
                } else {
                    setDefaultWorkflowStates(evdasAlert, config)
                }
            } catch(Throwable th) {
                log.error("--------------------------------------------------------------")
                log.error("The exception occured which fetching the data from the history")
                log.error("Setting up default values.")
                log.error("--------------------------------------------------------------")
                log.error(th.getMessage())
                setDefaultWorkflowStates(evdasAlert, config)
            }
    }

    private setDefaultWorkflowStates(EvdasAlert evdasAlert, EvdasConfiguration config) {
        if(evdasAlert && config) {
            evdasAlert.priority = config.priority
            if (config.assignedTo) {
                evdasAlert.assignedTo = config.assignedTo
            } else {
                evdasAlert.assignedToGroup = config.assignedToGroup
            }
            if (evdasAlert.disposition.reviewCompleted || evdasAlert.disposition.closed) {
                evdasAlert.dueDate = null
            } else {
                calcDueDate(evdasAlert, evdasAlert.priority, evdasAlert.disposition, false,
                        cacheService.getDispositionConfigsByPriority(evdasAlert.priority.id))
            }
        }
    }

    //This will update the States On Alert Level
    def updateEvdasAlertStates(def alert, Map map) {
        try {

            switch (map.change) {
                case Constants.HistoryType.DISPOSITION:
                    alert.disposition = map.disposition
                    break
                case Constants.HistoryType.ASSIGNED_TO:
                    alert.assignedTo = map.assignedTo
                    alert.assignedToGroup = map.assignedToGroup
                    break
                case Constants.HistoryType.PRIORITY:
                    alert.priority = map.priority
                    calcDueDate(alert, alert.priority, alert.disposition, false,
                            cacheService.getDispositionConfigsByPriority(alert.priority.id))
                    map.dueDate = alert.dueDate
                    break
            }
            alert.save(flush: true)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def toggleEvdasAlertFlag(id) {
        def alert = EvdasAlert.findById(id)
        def orgValue = alert.flagged
        alert.flagged = !orgValue
        alert.save()
        alert.flagged
    }

    def deleteEvdasConfig(EvdasConfiguration ec) {
        ec.isDeleted = true
        ec.isEnabled = false
        ec = (EvdasConfiguration) CRUDService.softDeleteWithAuditLog(ec)

        ec
    }

    def listSelectedAlerts(String alerts, def domainName) {
        String[] alertList = alerts.split(",")
        alertList.collect {
            domainName.findById(Integer.valueOf(it))
        }
    }

    List populatePossibleDateRanges(Date startDate, String uploadFrequency) {
        def possibleDateRanges = []
        switch (uploadFrequency) {
            case '15 Days':
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat('ddMMMyyyy')
                String dateInput = "01" + simpleDateFormat.format(startDate).substring(2,5) + simpleDateFormat.format(startDate).substring(5, 9)
                startDate = DateUtil.stringToDate(dateInput, DateUtil.DEFAULT_DATE_FORMAT_WITHOUT_SEPARATOR, DateTimeZone.UTC.ID)
                possibleDateRanges = populateFifteenDaysCombinationsForDate(startDate)
                break
            case '1 Month':
                possibleDateRanges = populateAllMonthlyCombinationsForDate(startDate)
                break
            case '3 Months':
                possibleDateRanges = populateAllQuarterlyCombinationsForDate(startDate)
                break
            case '6 Months':
                possibleDateRanges = populateAllHalfYearlyCombinationsForDate(startDate)
                break
            case '12 Months':
                possibleDateRanges = populateAllYearlyCombinationsForDate(startDate)
        }
        possibleDateRanges
    }

    List populateDateRangeBasedOnDataUploaded(String substanceName) {
        Set probableStartDate = []
        Set probableEndDate = []
        EvdasFileProcessLog.findAllBySubstances(substanceName, [sort: "recordStartDate", order: "asc"]).collect { fileProcessLog ->
            probableStartDate << dateWriteFormat.format(fileProcessLog.recordStartDate)
            probableEndDate << dateWriteFormat.format(fileProcessLog.recordEndDate)
        }
        [probableStartDate, probableEndDate]
    }

    List populateFifteenDaysCombinationsForDate(Date startDate) {
        List combinations = []
        Date currentDate = new Date()
        Integer time = 0
        Boolean flag = true
        while (flag || Date.parse(dateWriteFormat.toPattern(), combinations.last()[0]) < currentDate) {
            flag = false
            use(TimeCategory) {
                combinations.add([dateWriteFormat.format(startDate + time.month), dateWriteFormat.format(startDate + time.month + 14.day)])
                combinations.add([dateWriteFormat.format(startDate + time.month + 15.day), dateWriteFormat.format(startDate + (time + 1).month - 1.day)])
            }
            time++
        }
        combinations
    }

    List populateAllMonthlyCombinationsForDate(Date startDate) {
        List combinations = []
        Date currentDate = use(TimeCategory) { return new Date() + 1.year }
        Integer time = 0
        Boolean flag = true
        while (flag || Date.parse(dateWriteFormat.toPattern(), combinations.last()[1]) < currentDate) {
            flag = false
            combinations << use(TimeCategory) {
                [dateWriteFormat.format(startDate + time.month), dateWriteFormat.format(startDate + (time + 1).month - 1.day)]
            }
            time++
        }
        combinations
    }

    List populateAllQuarterlyCombinationsForDate(Date startDate) {
        List combinations = []
        Date currentDate = use(TimeCategory) { return new Date() + 1.year }
        Integer time = 0
        Boolean flag = true
        while (flag || Date.parse(dateWriteFormat.toPattern(), combinations.last()[1]) < currentDate) {
            flag = false
            combinations << use(TimeCategory) {
                [dateWriteFormat.format(startDate + (time * 3).month), dateWriteFormat.format(startDate + ((time * 3) + 3).month - 1.day)]
            }
            time++
        }
        combinations
    }

    List populateAllHalfYearlyCombinationsForDate(Date startDate) {
        List combinations = []
        Date currentDate = use(TimeCategory) { return new Date() + 1.year }
        Integer time = 0
        Boolean flag = true
        while (flag || Date.parse(dateWriteFormat.toPattern(), combinations.last()[1]) < currentDate) {
            flag = false
            combinations << use(TimeCategory) {
                [dateWriteFormat.format(startDate + (time * 6).month), dateWriteFormat.format(startDate + ((time * 6) + 6).month - 1.day)]
            }
            time++
        }
        combinations
    }

    List populateAllYearlyCombinationsForDate(Date startDate) {
        List combinations = []
        Date currentDate = use(TimeCategory) { return new Date() + 1.year }
        Integer time = 0
        Boolean flag = true
        while (flag || Date.parse(dateWriteFormat.toPattern(), combinations.last()[1]) < currentDate) {
            flag = false
            combinations << use(TimeCategory) {
                [dateWriteFormat.format(startDate + (time * 1).year), dateWriteFormat.format(startDate + ((time * 1) + 1).year - 1.day)]
            }
            time++
        }
        combinations
    }

    def caseDrillDown(Date startDate, Date endDate, String substance, String pt, Integer flagVar, Boolean isStartDate, String alertName, Integer alertId,Long productGroupId) {
        def data = []
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            int getType() {
                return OracleTypes.CURSOR
            }
        }

        def sql = new Sql(signalDataSourceService.getReportConnection('eudra'))
        try {
            def sDate = null
            def eDate = null
            if (isStartDate) {
                sDate = """TO_DATE('${startDate?.format(SqlGenerationService.DATE_FMT)}', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
                eDate = """TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
            } else {
                sDate = """TO_DATE('01-01-1900', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
                eDate = """TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
            }
            // calling p_evdas_drill_down with case series info and first 50 rows
            def procedure = "call p_evdas_drill_down(${sDate},${eDate},'${substance}','${pt.replaceAll(Constants.DatabaseRegex.SINGLE_QUOTE_REGEX, Constants.DatabaseRegex.FOUR_SINGLE_QUOTES)}',${flagVar}, '${alertName}',$productGroupId,${50} ,?)"
            log.info(procedure)
            sql.call("{${procedure}", [CURSOR_PARAMETER]) { result ->
                result?.eachRow() { GroovyResultSetExtension resultRow ->
                    log.info("Data is present in cursor , start mapping with object - EVDAS")
                }
            }
            def evdasListEql = 'select * from GTT_EVDAS_DRILL_DOWN'
            sql.eachRow(evdasListEql) { GroovyResultSet resultSet ->
                data << readCaseDrillDownData(resultSet,alertId)
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        data
    }

    def caseDrillDownAsync(Date startDate, Date endDate, String substance, String pt, Integer flagVar, Boolean isStartDate, String alertName, Integer alertId,Long productGroupId, Long numberOfCount){
        def data = []
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            int getType() {
                return OracleTypes.CURSOR
            }
        }
        def sql = new Sql(signalDataSourceService.getReportConnection('eudra'))
        try {
            def sDate = null
            def eDate = null
            if (isStartDate) {
                sDate = """TO_DATE('${startDate?.format(SqlGenerationService.DATE_FMT)}', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
                eDate = """TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
            } else {
                sDate = """TO_DATE('01-01-1900', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
                eDate = """TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                    SqlGenerationService.DATETIME_FMT_ORA
                }')"""
            }
            def procedure = "call p_evdas_drill_down(${sDate},${eDate},'${substance}','${pt.replaceAll(Constants.DatabaseRegex.SINGLE_QUOTE_REGEX, Constants.DatabaseRegex.FOUR_SINGLE_QUOTES)}',${flagVar}, '${alertName}',$productGroupId,NULL ,?)"
            log.info(procedure)
            sql.call("{${procedure}}", [CURSOR_PARAMETER]) { result ->
                result?.eachRow() { GroovyResultSetExtension resultRow ->
                    log.info("Fetching Evdas Case List Asynchronously..")
                }
            }
            def evdasListEql = 'select * from GTT_EVDAS_DRILL_DOWN'
            sql.eachRow(evdasListEql) { GroovyResultSet resultSet ->
                data << readCaseDrillDownData(resultSet,alertId)
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        data
    }

    @Transactional
    Map readCaseDrillDownData(resultRow, alertId) {
        String timezone = userService?.user?.preference?.timeZone ?: userService.getCurrentUserPreference()?.timeZone
        [
                caseNum                 : resultRow.getProperty('CASE_NUM'),
                caseIdentifier          : resultRow.getProperty('WORLDWIDE_CASE_IDENTIFIER'),
                hcp                     : resultRow.getProperty('HCP'),
                ageGroup                : resultRow.getProperty('PATIENT_AGE_GROUP_DESC'),
                gender                  : resultRow.getProperty('PATIENT_SEX_DESC'),
                firstReceiptDate        : DateUtil.stringToDate(resultRow.getProperty('DATE_FIRST_RECEIPT'), "dd-MMM-yyyy", timezone),
                dateFirstReceipt        : resultRow.getProperty('DATE_FIRST_RECEIPT').toString().substring(0,4) + resultRow.getProperty('DATE_FIRST_RECEIPT').toString().substring(4).toLowerCase(),
                companyFlag             : resultRow.getProperty('COMPANY_FLAG') as Integer ? true : false,
                primarySourceCountryDesc: resultRow.getProperty('PRIM_SRC_COUNTRY_DESC'),
                suspList                : resultRow.getProperty('SUSP_LIST'),
                suspAddInfoList         : resultRow.getProperty('SOURCE_SUSPECT_LIST'),
                concList                : resultRow.getProperty('CONC_LIST'),
                conMedInfoList          : resultRow.getProperty('SOURCE_CONCOMITANT_LIST'),
                reactList               : resultRow.getProperty('REACT_LIST'),
                eventAddInfoList        : resultRow.getProperty('SOURCE_REACT_LIST'),
                icsr                    : resultRow.getProperty('ICSR_FORM'),
                caseId                  : resultRow.getProperty('CASE_ID'),
                version                 : resultRow.getProperty('VERSION_NUM'),
                alertId                 : alertId
        ]
    }

    def getPreviousPeriodEvdasAlertListForProductSummary(productName, periodStartDate, periodEndDate, dispositionValue, eventList) {
        def evdasAlertList = EvdasAlert.withCriteria {
            eq('substance', productName)
            eq('periodStartDate', periodStartDate)
            eq('periodEndDate', periodEndDate)
            if (dispositionValue.size() > 0) {
                'in'("disposition", dispositionValue)
            }
            or {
                eventList.collate(1000).each {
                    'in'("pt", it)
                }
            }
            order('lastUpdated', 'desc')
        }

        evdasAlertList = evdasAlertList?.unique {
            [it.substanceId, it.ptCode, it.alertConfiguration]
        }

        evdasAlertList
    }

    Closure evdasConfigForEvdasAlert = { Long wfGroupId, String groupIds, List<String> selectedFilterValues ->
        eq("isLatest", true)
        eq("isDeleted", false)
        eq("adhocRun", false)
        eq("isEnabled", true)
        sqlRestriction("""CONFIG_ID IN 
           (${SignalQueryHelper.evdas_configuration_sql(getUserService().getCurrentUserId(), wfGroupId,
                groupIds, selectedFilterValues)}
           )""")
    }

    Map getListOfExecutedEvdasAlerts(int max, int offset, Map orderColumnMap, String searchString, String selectedValuesForFilter) {

        String esc_char = ""
        String ordering_property = orderColumnMap.name ?: "id"
        String direction = orderColumnMap.dir ?: "desc"

        if (ordering_property.equals("lastExecuted")) {
            ordering_property = "date_created"
        } else if (ordering_property.equals("lastModified")) {
            ordering_property = "last_updated"
        }
        List<String> selectedFilterValues = (selectedValuesForFilter == "null" || selectedValuesForFilter == "") ? [] : selectedValuesForFilter?.substring(1,selectedValuesForFilter.length()-1).replaceAll("\"", "").split(",");
        Group workflowGroup = userService.getUser().workflowGroup
        User currentUser = userService.getUser()
        String groupIds = currentUser.groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}.collect { it.id }.join(",")

        searchString = searchString.toLowerCase()
        if (searchString) {
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
        }

        List<Disposition> reviewCompletedDispositionList = Disposition.findAllByReviewCompleted(true)


        String orderJoin = ""
        String ordering_property_nvl = ordering_property
        if(ordering_property == "caseCount") {
            orderJoin = "left join (select evdas.exec_configuration_id as exid, count(*) as case_count from evdas_alert evdas  group by evdas.exec_configuration_id  ) evdascount\n" +
                    "            on evr.id = evdascount.exid"
            ordering_property_nvl = "COALESCE(case_count,0)"
        } else if(ordering_property == "newCases") {
            orderJoin = "left join (select evdas.exec_configuration_id as exid, count(*) as new_cases from evdas_alert evdas where evdas.is_new = true  group by evdas.exec_configuration_id  ) evdascount\n" +
                    "            on evr.id = evdascount.exid"
            ordering_property_nvl = "COALESCE(new_cases,0)"
        } else if(ordering_property == "closedCaseCount") {
            orderJoin = "left join (select evdas.exec_configuration_id as exid, count(*) as closed_case_count from evdas_alert evdas where evdas.disposition_id in (${reviewCompletedDispositionList*.id.join(',')})   group by evdas.exec_configuration_id  ) evdascount\n" +
                    "            on evr.id = evdascount.exid"
            ordering_property_nvl = "COALESCE(closed_case_count,0)"
        }



        String searchClause = ""
        if (esc_char) {
            searchClause =  """

        (
                        (lower(name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}') or
                        (lower(product_selection) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}') or
                        (lower(description) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}')
                    
                    )
                    
        """
        } else {
            searchClause = """
                (
                        (lower(name) like '%${searchString.replaceAll("'", "''")}%') or
                        (lower(product_selection) like '%${searchString.replaceAll("'", "''")}%') or
                        (lower(description) like '%${searchString.replaceAll("'", "''")}%')
                    )
        """
        }




        String evdasIdsSql = """ select evr.id as id from ex_evdas_config evr
            ${orderJoin}
            where evr.is_latest = true and evr.is_deleted =  false and evr.adhoc_run = false and evr.is_enabled = true
            and config_id in (${SignalQueryHelper.evdas_configuration_sql(getUserService().getCurrentUserId(), workflowGroup?.id,
                groupIds, selectedFilterValues)})
            ${searchClause && searchString? " and "+searchClause:""}

            order by ${ordering_property_nvl} ${direction?direction.toLowerCase():'desc'}
            ${direction?.toLowerCase() == "desc"? "NULLS LAST":"NULLS FIRST"}

          

         """

        log.info(evdasIdsSql)
        List<Long> evdasIds = []

        try {
            Session session = sessionFactory.currentSession
            SQLQuery sqlQuery = session.createSQLQuery(evdasIdsSql)
            sqlQuery.addScalar("id", new LongType())
            evdasIds = sqlQuery.list()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            ex.printStackTrace()
        }

        List<ExecutedEvdasConfiguration> resultList = []
        if (evdasIds.size()) {
            // Generate the CASE statement
            StringBuilder clauseStatement = new StringBuilder()
            evdasIds.eachWithIndex { id, index ->
                clauseStatement.append(" WHEN id = ${id} THEN ${index + 1}" )// +1 if you want 1-based indexing
            }.join(' ')

            def caseStatement = "CASE ${clauseStatement.toString()} ELSE ${evdasIds.size() + 1} END"
            resultList = ExecutedEvdasConfiguration.createCriteria().list(max: max, offset: offset) {
                or {
                    evdasIds.collate(999).each { ids ->
                        'in'('id', ids)
                    }
                }

                // Use sqlRestriction with dynamically constructed CASE statement
                sqlRestriction("1=1 GROUP BY ID ORDER BY ${caseStatement}")
            }
        }
        Integer totalCount = ExecutedEvdasConfiguration.createCriteria().count() {
            evdasConfigForEvdasAlert.delegate = delegate
            evdasConfigForEvdasAlert(workflowGroup?.id, groupIds, [])
        } as Integer
        Integer filteredCount = resultList?.totalCount
        return [resultList: resultList, totalCount: totalCount, filteredCount: filteredCount]
    }

    private int getTotalCount(List<String> sharedWithConfigNames) {
        Group workflowGroup = userService.getUser().workflowGroup
        int totalCount = ExecutedEvdasConfiguration.createCriteria().count {
            eq("adhocRun", false)
            eq("isDeleted", false)
            eq("isLatest", true)
            'or'{
                eq("workflowGroup", workflowGroup)
                if(sharedWithConfigNames){
                    'or'{
                        sharedWithConfigNames.collate(1000).each {
                            'in'('name',it)
                        }
                    }
                }

            }
        }
        return totalCount
    }

    private int getFilteredCount(String searchKey,List<String> sharedWithConfigNames) {
        Group workflowGroup = userService.getUser().workflowGroup
        int filteredCount = ExecutedEvdasConfiguration.createCriteria().count {
            if (searchKey) {
                or {
                    ilike("name", "%${searchKey}%")
                    ilike("productSelection", "%${searchKey}%")
                    ilike("description", "%${searchKey}%")
                }
            }
            eq("adhocRun", false)
            eq("isDeleted", false)
            eq("isLatest", true)
            'or'{
                eq("workflowGroup", workflowGroup)
                if(sharedWithConfigNames){
                    'or'{
                        sharedWithConfigNames.collate(1000).each {
                            'in'('name',it)
                        }
                    }
                }

            }
            order("lastUpdated", "desc")
        }
    }

    def saveRequestByForEvdasAlert(params) {
        EvdasAlert.findAllBySubstanceAndPt(params.productName, params.eventName).each {
            it.requestedBy = params.requestedBy
            CRUDService.saveWithoutAuditLog(it)
        }
    }

    def savePreviousExecutedEvdasAlertWithSignal() {

        Session session = sessionFactory.currentSession

        List signalIdProductIdPtCodeList = []
        String sql_statement = SignalQueryHelper.evdasCaseAlert_attached_signals()
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        sqlQuery.list().each { row ->
            signalIdProductIdPtCodeList.add([signalId: row[0].toString(), productId: row[1].toString(), ptCode: row[2].toString()])
        }

        session.flush()
        session.clear()

        if (signalIdProductIdPtCodeList.size() > 0) {

            def productIdAndPtCodeList = signalIdProductIdPtCodeList.collect {
                "(" + it.productId + "," + it.ptCode + ")"
            }

            List alertSignalToAddList = []


            sql_statement = SignalQueryHelper.evdasCaseAlert_signals_to_add(productIdAndPtCodeList.join(","))
            sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list().each { row ->
                alertSignalToAddList.add([alertId: row[0].toString(), productId: row[1].toString(), ptCode: row[2].toString()])
            }
            session.flush()
            session.clear()

            if (alertSignalToAddList.size() > 0) {
                List alertIdAndSignalIdList = []
                signalIdProductIdPtCodeList.each { signalIdProductIdPtCode ->
                    List<String> alertIdList = alertSignalToAddList.findAll {
                        signalIdProductIdPtCode.productId == it.productId && signalIdProductIdPtCode.ptCode == it.ptCode
                    }?.collect {
                        it.alertId
                    }
                    alertIdList.unique { it }
                    if (alertIdList.size() > 0) {
                        alertIdList.each {
                            alertIdAndSignalIdList.add([alertId: it, signalId: signalIdProductIdPtCode.signalId])
                        }
                    }
                }
                def insertValidatedEvdasAlertQuery = "INSERT INTO VALIDATED_EVDAS_ALERTS(VALIDATED_SIGNAL_ID,EVDAS_ALERT_ID) VALUES(?,?)"
                session.doWork(new Work() {
                    public void execute(Connection connection) throws SQLException {
                        PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedEvdasAlertQuery)
                        def batchSize = Holders.config.signal.batch.size
                        int count = 0
                        try {
                            alertIdAndSignalIdList.each {
                                preparedStatement.setString(1, it.signalId)
                                preparedStatement.setString(2, it.alertId)
                                preparedStatement.addBatch()
                                count += 1
                                if (count == batchSize) {
                                    preparedStatement.executeBatch()
                                    count = 0
                                }
                            }
                            preparedStatement.executeBatch()
                        } catch (Exception e) {
                            e.printStackTrace()
                        } finally {
                            preparedStatement.close()
                            session.flush()
                            session.clear()
                        }
                    }
                })
            }
        }
    }

    def checkProductExistsForEvdas(String productDictionarySelection, List<String> productNameList) {
        //todo:fix for dynamic dictionary level ( 1 - ingredient level)
        if (productDictionarySelection != "1") {
            return false
        } else {
            return productBasedSecurityService.checkIngredientExistsForEvdas(productNameList)
        }
    }

    Map createEvdasHistoryMap(def evdasAlert, String changeType, String justification, Boolean isArchived = false, String fullName = null, Boolean isUndo = false) {
        String currUserFullName = fullName?fullName:userService.getUser()?.fullName
        Map evdasHistoryMap = [
                "productName"         : evdasAlert.substance,
                "eventName"           : evdasAlert.pt,
                "priority"            : evdasAlert.priority,
                "change"              : changeType,
                "justification"       : justification,
                "isLatest"            : true,
                "disposition"         : evdasAlert.disposition,
                "executionDate"       : evdasAlert.dateCreated,
                "asOfDate"            : evdasAlert.periodEndDate,
                "assignedTo"          : evdasAlert.assignedTo,
                "assignedToGroup"     : evdasAlert.assignedToGroup,
                "createdBy"           : currUserFullName,
                "modifiedBy"          : currUserFullName,
                "configId"            : evdasAlert.alertConfiguration.id,
                "evdasAlertId"        : !isArchived ? evdasAlert.id : null,
                "archivedEvdasAlertId": isArchived ? evdasAlert.id : null,
                "lastUpdated"         : new Date(),
                "dueDate"             : evdasAlert.dueDate,
                "isUndo"              : isUndo
        ]
        evdasHistoryMap << createCommonEvdasHistoryMap(evdasAlert)
        evdasHistoryMap
    }

    void sendMailForAssignedToChange(List emailDataList, def evdasAlert, String emailTitle, Boolean isArchived = false) {
        List sentEmailList = []
        String alertLink = createHref("evdasAlert", "details", ["callingScreen": Constants.Commons.REVIEW, "configId": evdasAlert.executedAlertConfiguration.id, "isArchived": isArchived])
        emailDataList.each { Map emailMap ->
            if (!sentEmailList.count { it == emailMap.user.email }) {
                emailNotificationService.mailHandlerForAssignedToEvdas(emailMap.user, evdasAlert, alertLink, emailMap.emailMessage, userService.getAssignedToName(evdasAlert), emailTitle)
                sentEmailList << emailMap.user.email
            }
        }

    }

    void saveAlertCaseHistory(def alert, String justification, String fullName = null){
        alert.justification = justification
        alert.dispLastChange = new Date()
        alert.dispPerformedBy = fullName?fullName:userService.getUser()?.fullName
        alert.isDispChanged = true
    }

    @Transactional
    Map changeDisposition(List<Long> evdasAlertIdList, Disposition newDisposition,
                              String justification, String validatedSignalName, String productJson,Boolean isArchived,Long signalId) {
        Date createdTimeStamp = null
        String currUserName = userService.getCurrentUserName()
        User currentUser = cacheService.getUserByUserNameIlike(currUserName)
        Boolean attachedSignalData
        List<Map> alertDueDateList = []
        List<Map> evdasHistoryList = []
        String eventJson = ''
        List<Map> activityList = []
        def domain = getDomainObject(isArchived)
        ValidatedSignal validatedSignal;
        List<EvdasAlert> evdasAlertList = fetchEvdasAlertsForBulkOperations(domain, evdasAlertIdList)
        if (validatedSignalName) {
             validatedSignal = ValidatedSignal.findById(signalId)
            if (validatedSignal) {
                evdasAlertList = evdasAlertList.findAll {
                    !it.validatedSignals.contains(validatedSignal)
                }
            }
        }
        if(evdasAlertList.size()) {
            boolean bulkUpdate = evdasAlertList.size() > 1
            Disposition previousDisposition = evdasAlertList[0].disposition
            List<Map<String, String>> alertIdAndSignalIdList = []
            List<Activity> signalActivityList = []
            List<UndoableDisposition> undoableDispositionList=[]
            List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions;
            boolean  isValidSignal=false;
            if (validatedSignalName) {
                Disposition defaultSignalDisposition = evdasAlertList[0].alertConfiguration.getWorkflowGroup()?.defaultSignalDisposition
                eventJson = alertService.generateBulkEventJSON(evdasAlertList*.pt, evdasAlertList*.ptCode, "4")
                isValidSignal=validatedDateDispositions.contains(defaultSignalDisposition.value)
                validatedSignal = validatedSignalService.createSignalForBusinessConfiguration(validatedSignalName, evdasAlertList[0], Constants.AlertConfigType.EVDAS_ALERT, defaultSignalDisposition, null, eventJson,signalId,isValidSignal)

            }

            Map prevDispCountMap = [:]
            Map execDispCountMap = [:]

            evdasAlertList.each { def evdasAlert ->
                evdasAlert.customAuditProperties = ["justification": justification]
                if (!evdasAlert.disposition.isValidatedConfirmed()) {
                    Disposition oldDisp = evdasAlert?.disposition
                    Map dispDataMap = [objectId: evdasAlert.id, objectType: Constants.AlertType.EVDAS, prevDispositionId: oldDisp.id,
                                       currDispositionId: newDisposition.id, prevJustification: evdasAlert.justification, prevDispPerformedBy: evdasAlert.dispPerformedBy,
                                       prevDueDate: evdasAlert.dueDate, prevDispChangeDate: evdasAlert.dispLastChange, pastPrevDueDate:evdasAlert.previousDueDate]
                    UndoableDisposition undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)

                    undoableDispositionList.add(undoableDisposition)
                    evdasAlert.disposition = newDisposition
                    if((newDisposition.closed || newDisposition.reviewCompleted) && evdasAlert.dueDate!=null) {
                        evdasAlert.previousDueDate = evdasAlert.dueDate
                    }
                    calcDueDate(evdasAlert, evdasAlert.priority, evdasAlert.disposition, true,
                            cacheService.getDispositionConfigsByPriority(evdasAlert.priority.id))
                    def customReviewPeriod = cacheService.getDispositionConfigsByPriority(evdasAlert.priority.id)?.find {it -> it.disposition == newDisposition}?.reviewPeriod
                    if(newDisposition.closed==false && newDisposition.reviewCompleted==false && evdasAlert.previousDueDate!=null && (oldDisp.closed || oldDisp.reviewCompleted) && !customReviewPeriod){
                        evdasAlert.dueDate = evdasAlert.previousDueDate
                    }
                    alertDueDateList << [id: evdasAlert.id, dueDate: evdasAlert.dueDate, dispositionId: evdasAlert.disposition.id,
                                         reviewDate: evdasAlert.reviewDate]
                    if (emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_EVDAS)) {
                        emailNotificationService.mailHandlerForDispChangeEvdas(evdasAlert, previousDisposition, isArchived)
                    }
                    Map evdasHistoryMap = createEvdasHistoryMap(evdasAlert, Constants.HistoryType.DISPOSITION, justification, isArchived, currentUser.fullName)
                    saveAlertCaseHistory(evdasAlert,justification,currentUser?.fullName)
                    evdasHistoryMap.disposition = newDisposition
                    createdTimeStamp = evdasAlert.dispLastChange
                    evdasHistoryMap.put("createdTimestamp", createdTimeStamp)
                    evdasHistoryList << evdasHistoryMap
                }
                else if(previousDisposition.isValidatedConfirmed()){
                    calcDueDate(evdasAlert, evdasAlert.priority, evdasAlert.disposition, true,
                            cacheService.getDispositionConfigsByPriority(evdasAlert.priority.id))

                    if(newDisposition.closed==false && newDisposition.reviewCompleted==false && evdasAlert.previousDueDate!=null) {
                        evdasAlert.dueDate = evdasAlert.previousDueDate
                    }
                    alertDueDateList << [id: evdasAlert.id, dueDate: evdasAlert.dueDate, dispositionId: evdasAlert.disposition.id,
                                         reviewDate: evdasAlert.reviewDate]
                    if(justification){
                        justification = justification.replace('.', ' ') + "-- " + customMessageService.getMessage("validatedObservation.justification.pec", "${validatedSignalName}")
                    }
                    else
                        justification = customMessageService.getMessage("validatedObservation.justification.pec", "${validatedSignalName}")
                    Map evdasHistoryMap = createEvdasHistoryMap(evdasAlert, Constants.HistoryType.DISPOSITION, justification, isArchived, currentUser.fullName)
                    saveAlertCaseHistory(evdasAlert,justification,currentUser.fullName)
                    evdasHistoryMap.disposition = previousDisposition
                    createdTimeStamp = evdasAlert.dispLastChange
                    evdasHistoryMap.put("createdTimestamp", createdTimeStamp)
                    evdasHistoryList << evdasHistoryMap
                }
                prevDispCountMap.put(previousDisposition.id.toString(),(prevDispCountMap.get(previousDisposition.id.toString()) ?: 0) + 1)
                alertService.updateDashboardCountMaps(execDispCountMap, evdasAlert.executedAlertConfigurationId, newDisposition.id.toString())
                Activity activity = createActivityForBulkUpdate(evdasAlert, newDisposition, previousDisposition, justification, validatedSignalName)
                activityList.add([execConfigId: evdasAlert.executedAlertConfigurationId, activity: activity])

                if (validatedSignalName) {
                    alertIdAndSignalIdList.add([col2: evdasAlert.id.toString(), col1: validatedSignal.id.toString()])
                    signalActivityList.add(activityService.createActivityForSignalBusinessConfigEvdas(evdasAlert.substance, evdasAlert.pt, validatedSignal.name, evdasAlert.name))
                    attachedSignalData = true
                    signalAuditLogService.createAuditLog([
                            entityName: "Signal: Aggregate Review Observations",
                            moduleName: "Signal: Aggregate Review Observations",
                            category: AuditTrail.Category.INSERT.toString(),
                            entityValue: "${validatedSignalName}: PEC associated",
                            username: userService.getUser().username,
                            fullname: userService.getUser().fullName
                    ] as Map, [[propertyName: "PEC associated", oldValue: "", newValue: "${evdasAlert.substance}-${evdasAlert.pt}"]] as List)
                }

            }
            if (evdasAlertIdList) {
                //Creating peHistories and activities in background
                Map evdasHistoryActivityMap = [activityList: activityList, evHistoryList: evdasHistoryList, isBulkUpdate: true, execDispCountMap:execDispCountMap, prevDispCountMap:prevDispCountMap]
                notify 'evdas.activity.history.event.published', evdasHistoryActivityMap
                if(!isArchived){
                    notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
                }
            }

            if (validatedSignal) {
                notify 'activity.add.signal.event.published', [signalId: validatedSignal.id, signalActivityList: signalActivityList]
                String insertValidatedQuery
                if (isArchived)
                    insertValidatedQuery = "INSERT INTO VALIDATED_ARCH_EVDAS_ALERTS(VALIDATED_SIGNAL_ID,ARCHIVED_EVDAS_ALERT_ID) VALUES(?,?)"
                else
                    insertValidatedQuery = "INSERT INTO VALIDATED_EVDAS_ALERTS(VALIDATED_SIGNAL_ID,EVDAS_ALERT_ID,DATE_CREATED) VALUES(?,?,?)"
                alertService.batchPersistForMapping(sessionFactory.currentSession, alertIdAndSignalIdList, insertValidatedQuery, !isArchived)
            }
        }
        Map signal=validatedSignalService.fetchSignalDataFromSignal(validatedSignal,productJson,null);
        [attachedSignalData: attachedSignalData, alertDueDateList: alertDueDateList, signal:signal]
    }

    Activity createActivityForUndoAction(def evdasAlert, String justification, String activityChanges){
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.UndoAction.value)
        String changeDetails = Constants.ChangeDetailsUndo.UNDO_DISPOSITION_CHANGE + " with " + activityChanges
        User loggedInUser = cacheService.getUserByUserId(userService.getCurrentUserId())

        Activity activity = activityService.createActivityBulkUpdate(activityType, loggedInUser, changeDetails, justification,
                null, evdasAlert.substance, evdasAlert.pt, evdasAlert.assignedToId ? cacheService.getUserByUserId(evdasAlert.assignedToId) : null, null,
                evdasAlert.assignedToGroupId ? cacheService.getGroupByGroupId(evdasAlert.assignedToGroupId) : null)
        activity
    }

    def undoneEvdasHistory(EvdasAlert evdasAlert) {
        log.info("Marking previous Evdas history as Undone")
        ExecutedConfiguration ec = ExecutedConfiguration.get(evdasAlert.executedAlertConfigurationId as Long)
        EvdasHistory evdasHistory = EvdasHistory.createCriteria().get {
            eq('evdasAlertId', evdasAlert.id)
            eq('productName', evdasAlert.productName)
            eq('configId', evdasAlert.alertConfiguration.id)
            eq("change", Constants.HistoryType.DISPOSITION)
            order("lastUpdated", "desc")
            maxResults(1)
        } as EvdasHistory
        if (evdasHistory) {
            evdasHistory.isUndo = true
            evdasHistory.save(flush:true, failOnError:true)
            log.info("Successfully marked previous Evdas History as Undone for evdasAlert alert: ${evdasAlert?.id}")
        }
    }

    @Transactional
    Map revertDisposition(Long id, String justification) {
        log.info("Reverting Dispostion Started for Evdas Alert")
        Boolean dispositionReverted = false
        List<Map> activityList = []
        List<Map> alertDueDateList = []
        List<Map> evdasHistoryList = []
        String oldDispName = ""
        String targetDisposition = ""
        User currentUser = userService.getUser()
        EvdasAlert evdasAlert = EvdasAlert.get(id)
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertType.EVDAS)
            order('dateCreated', 'desc')
            maxResults(1)
        } as UndoableDisposition

        if (evdasAlert && undoableDisposition?.isEnabled) {
            try {
                Map prevDispCountMap = [:]
                Map execDispCountMap = [:]
                Disposition oldDisp = cacheService.getDispositionByValue(evdasAlert.disposition?.id)
                oldDispName = oldDisp?.displayName
                Disposition newDisposition = cacheService.getDispositionByValue(undoableDisposition.prevDispositionId)
                targetDisposition = newDisposition?.displayName

                undoableDisposition.isUsed = true
                // saving state before undo for activity: 60067
                def prevUndoDisposition = evdasAlert.disposition
                def prevUndoJustification = evdasAlert.justification
                def prevUndoDispPerformedBy = evdasAlert.dispPerformedBy
                def prevUndoDispChangeDate = evdasAlert.dispLastChange
                def prevUndoDueDate = evdasAlert.dueDate

                evdasAlert.disposition = newDisposition
                evdasAlert.justification = undoableDisposition.prevJustification
                evdasAlert.dispPerformedBy = undoableDisposition.prevDispPerformedBy
                evdasAlert.dispLastChange = undoableDisposition.prevDispChangeDate
                evdasAlert.previousDueDate = undoableDisposition.pastPrevDueDate
                evdasAlert.dueDate = undoableDisposition.prevDueDate
                evdasAlert.undoJustification = justification

                def activityMap = [
                        'Disposition': [
                                'previous': prevUndoDisposition ?: "",
                                'current': evdasAlert.disposition ?: ""
                        ],
                        'Justification': [
                                'previous': prevUndoJustification ?: "",
                                'current': evdasAlert.justification ?: ""
                        ],
                        'Performed By': [
                                'previous': prevUndoDispPerformedBy ?: "",
                                'current': evdasAlert.dispPerformedBy ?: ""
                        ],
                        'Last Disposition Date': [
                                'previous': prevUndoDispChangeDate ? new SimpleDateFormat("yyyy-MM-dd").format(prevUndoDispChangeDate) : "",
                                'current': evdasAlert.dispLastChange ? new SimpleDateFormat("yyyy-MM-dd").format(evdasAlert.dispLastChange) : ""
                        ],
                        'Due Date': [
                                'previous': prevUndoDueDate ? new SimpleDateFormat("yyyy-MM-dd").format(prevUndoDueDate) : "",
                                'current': evdasAlert.dueDate ? new SimpleDateFormat("yyyy-MM-dd").format(evdasAlert.dueDate) : ""
                        ]
                ]

                String activityChanges = activityMap.collect { k, v ->
                    def previous = v['previous'] ?: ""
                    def current = v['current'] ?: ""
                    if (previous != current) {
                        "$k changed from \'$previous\' to \'$current\'"
                    } else {
                        null
                    }
                }.findAll().join(', ')
                CRUDService.update(evdasAlert)
                CRUDService.update(undoableDisposition)

                UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: id, type: Constants.AlertType.EVDAS])

                alertDueDateList << [id        : evdasAlert.id, dueDate: evdasAlert.dueDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(evdasAlert.dueDate) : null, dispositionId: evdasAlert.disposition.id,
                                     reviewDate: evdasAlert.reviewDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(evdasAlert.reviewDate) : null]
                undoneEvdasHistory(evdasAlert)
                Map evdasHistoryMap = createEvdasHistoryMap(evdasAlert, Constants.HistoryType.UNDO_ACTION, justification, false, currentUser.fullName, true)
                evdasHistoryMap.disposition = newDisposition
                evdasHistoryList << evdasHistoryMap

                Activity activity = createActivityForUndoAction(evdasAlert, justification, activityChanges)
                activityList.add([execConfigId: evdasAlert.executedAlertConfigurationId, activity: activity])
                prevDispCountMap.put(oldDisp.id.toString(),(prevDispCountMap.get(oldDisp.id.toString()) ?: 0) + 1)
                alertService.updateDashboardCountMaps(execDispCountMap as Map<Long, Map<String, Integer>>, evdasAlert.executedAlertConfigurationId, newDisposition.id.toString())
                Map evdasHistoryActivityMap = [activityList: activityList, evHistoryList: evdasHistoryList, isBulkUpdate: false, execDispCountMap:execDispCountMap, prevDispCountMap:prevDispCountMap]
                notify 'evdas.activity.history.event.published', evdasHistoryActivityMap
                dispositionReverted=true
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("some error occoured while reverting disposition")
            }
        }
        [alertDueDateList: alertDueDateList, prevDisposition: oldDispName, targetDisposition: targetDisposition, dispositionReverted:dispositionReverted]
    }

    List<Map> fetchEvdasAlertList(List eaList, AlertDataDTO alertDataDTO) {
        Map params = alertDataDTO.params
        List list = []
        ConcurrentHashMap dateRangeMap
        def dateRangeRecent
        List<Map> prevEvdasAlertMap = []
        if (alertDataDTO.cumulative && !(params.adhocRun.toBoolean())) {
            dateRangeMap = fetchDateRangeListOfExec(params.frequency)
            dateRangeRecent = dateRangeMap["exeRecent"]
            dateRangeMap?.remove("exeRecent")
        }
        Disposition defaultEvdasDisposition = userService.getUser().workflowGroup.defaultEvdasDisposition
        cacheService.setDefaultDisp(Constants.AlertType.EVDAS, defaultEvdasDisposition.id as Long)
        list = fetchValuesForEvdasReport(eaList as List, alertDataDTO.domainName, alertDataDTO.isFromExport, params.boolean('isArchived'))
        Integer prevColCount = Holders.config.signal.evdas.number.previous.columns
        Boolean isPrevExecutions = !alertDataDTO.cumulative && !(params.adhocRun.toBoolean()) && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS
        List prevExecs = []
        if (!alertDataDTO.cumulative && !(params.adhocRun.toBoolean()) && params.callingScreen != Constants.Commons.DASHBOARD) {
            prevExecs = fetchPrevPeriodExecConfig(alertDataDTO.configId, alertDataDTO.execConfigId)
        }
        if (prevExecs.size() > 0 && !(params.adhocRun.toBoolean()) && params.callingScreen != Constants.Commons.DASHBOARD) {
            List<Long> prevExecutionsIdList = prevExecs.collect {
                it.id
            }
            List<Integer> ptCodeList = eaList.collect {
                it.ptCode
            }

            List<String> substanceList = eaList.collect {
                it.substance
            }

            prevEvdasAlertMap = ArchivedEvdasAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("newEv", "newEv")
                    property("executedAlertConfiguration.id", "executedAlertConfigurationId")
                    property("newSerious", "newSerious")
                    property("newFatal", "newFatal")
                    property("newLit", "newLit")
                    property("allRor", "allRor")
                    property("substance", "substance")
                    property("ptCode", "ptCode")
                }
                or {
                    prevExecutionsIdList.collate(1000).each{
                        'in'('executedAlertConfiguration.id', it)
                    }
                }
                or {
                    ptCodeList.collate(1000).each {
                        'in'('ptCode', it)
                    }
                }
                or {
                    substanceList.collate(1000).each {
                        'in'('substance', it)
                    }
                }
            } as List<Map>
        }
            ExecutorService executorService = signalExecutorService.threadPoolForEvdasListExec()
            eaList.each { def ea ->
                String prevAllRor = list.find { it.id == ea.id }['allRor']?:"0"
                executorService.submit({

                    if (isPrevExecutions) {
                        (0..<prevColCount).each { index ->
                            ExecutedEvdasConfiguration pec = prevExecs[index]
                            Map countMap = [:]
                            if (pec) {
                                Map prevAlert = prevEvdasAlertMap.find {
                                    it.executedAlertConfigurationId == pec.id && it.substance == ea.substance && it.ptCode == ea.ptCode
                                }

                                countMap = [
                                        newEv     : (prevAlert?.newEv != null) ? prevAlert.newEv.toString() : Constants.Commons.UNDEFINED_NUM_STR,
                                        newSerious: (prevAlert?.newSerious != null) ? prevAlert.newSerious.toString() : Constants.Commons.UNDEFINED_NUM_STR,
                                        newFatal  : (prevAlert?.newFatal != null) ? prevAlert.newFatal.toString() : Constants.Commons.UNDEFINED_NUM_STR,
                                        newLit    : (prevAlert?.newLit != null) ? prevAlert.newLit.toString() : Constants.Commons.UNDEFINED_NUM_STR,
                                        allRor    : (prevAlert?.allRor != null) ? prevAlert.allRor.toString() : prevAllRor.toString()
                                ]
                            } else {
                                countMap = [
                                        newEv     : Constants.Commons.UNDEFINED_NUM_STR,
                                        newSerious: Constants.Commons.UNDEFINED_NUM_STR,
                                        newFatal  : Constants.Commons.UNDEFINED_NUM_STR,
                                        newLit    : Constants.Commons.UNDEFINED_NUM_STR,
                                        allRor    : Constants.Commons.UNDEFINED_NUM_STR
                                ]
                            }
                            String exeName = "exe" + (index)
                            list.find { it.id == ea.id }[exeName] = countMap
                        }
                    }

                })

            }
        cacheService.removeDefaultDisp(Constants.AlertType.EVDAS)
        list
    }

    def fetchDateRangeListOfExec(String frequency) {
        SubstanceFrequency substanceFrequency = SubstanceFrequency.findByFrequencyName(frequency)
        def dateRangeMap = [:]
        if (substanceFrequency) {
            List probableDateRange = populatePossibleDateRanges(substanceFrequency.startDate, substanceFrequency.miningFrequency)
            def dateRangeList = []
            probableDateRange.reverse(true)
            probableDateRange.each {
                dateRangeList << Date.parse('dd-MMM-yyyy', it[1])
            }
            def endDateList = ExecutedEvdasConfiguration.findAllByFrequency(frequency).collect {
                it.dateRangeInformation.dateRangeEndAbsolute
            }
            def isLatestExec = 0
            dateRangeList.eachWithIndex { Date date, int i ->
                if (endDateList.contains(date)) {
                    isLatestExec++
                    if (isLatestExec == 1) {
                        def recentDateRange = [
                                "startDate": Date.parse('dd-MMM-yyyy', probableDateRange[i][0]),
                                "endDate"  : Date.parse('dd-MMM-yyyy', probableDateRange[i][1])
                        ]
                        def exeRecent = "exeRecent"
                        dateRangeMap[exeRecent] = recentDateRange
                    } else if (isLatestExec < 7) {
                        def dateRangeInfo = [
                                "startDate": Date.parse('dd-MMM-yyyy', probableDateRange[i][0]),
                                "endDate"  : Date.parse('dd-MMM-yyyy', probableDateRange[i][1])
                        ]
                        def exeName = "exe" + (isLatestExec - 2)
                        dateRangeMap[exeName] = dateRangeInfo
                    }
                }
            }
        }
        dateRangeMap
    }

    //Fetching Configuration Object from ExecutedConfiguration Id
    def getAlertConfigObject(ExecutedEvdasConfiguration ec) {
        ec.configId
    }

    def getAlertConfigObject(String name, User owner) {
        try {
            Long configId = EvdasConfiguration.createCriteria().get {
                projections {
                    property("id")
                }
                eq('owner', owner)
                eq('name', name)
            } as Long
            return configId
        } catch (Throwable th) {
            log.error(th.getMessage())
            return 0L
        }
    }

    Closure saveActivityAndHistory = { AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived,List<Map> bulkUpdateDueDateDataList = []->
        List<Activity> activityList = []
        List<ProductEventHistory> evHistoryList = []
        alertLevelDispositionDTO.activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        alertLevelDispositionDTO.loggedInUser = userService.getUser()
        List<UndoableDisposition> undoableDispositionList = []
        Map prevDispCountMap = alertLevelDispositionDTO.alertList.countBy {it.dispositionId as String}
        Map execDispCountMap = [(alertLevelDispositionDTO.execConfigId) : [(alertLevelDispositionDTO.targetDisposition.id.toString()):alertLevelDispositionDTO.alertList.size()]]
        ExecutorService executorService = Executors.newFixedThreadPool(20)
        List<Future> futureList = alertLevelDispositionDTO.alertList.collect { alertMap ->
            executorService.submit({ ->
                UndoableDisposition undoableDisposition = null
                Activity activity = alertService.createActivityForBulkDisposition(alertMap, alertLevelDispositionDTO)
                EvdasHistory evdasHistory = createEvdasHistoryForBulkDisposition(alertMap, alertLevelDispositionDTO, isArchived)
                if(!isArchived){
                    Map dispDataMap = [objectId: alertMap.id, objectType: Constants.AlertType.EVDAS, prevDispositionId: alertMap.disposition.id,
                                       currDispositionId: alertLevelDispositionDTO.targetDisposition.id, prevJustification: alertMap.justification, prevDispPerformedBy: alertMap.dispPerformedBy,
                                       prevDueDate: alertMap.dueDate, prevDispChangeDate: alertMap.dispLastChange, pastPrevDueDate:alertMap.previousDueDate]
                    undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)
                }
                if (bulkUpdateDueDateDataList) {
                    Map dueDateMap = bulkUpdateDueDateDataList.find { it.priorityId == alertMap?.priority?.id }
                    if (dueDateMap) {
                        evdasHistory.dueDate = !dueDateMap.isClosed ? (dueDateMap.dueDateChange ? dueDateMap.dueDate : evdasHistory.dueDate) : null
                    }
                }
                [activity: activity, evdasHistory: evdasHistory, undoableDisposition: undoableDisposition?:null]
            } as Callable)
        }
        futureList.each {
            activityList.add(it.get()['activity'])
            evHistoryList.add(it.get()['evdasHistory'])
            undoableDispositionList.add(it.get()['undoableDisposition'])
        }
        executorService.shutdown()
        Map evHistoryActivityMap = [activityList: activityList, evHistoryList: evHistoryList, id: alertLevelDispositionDTO.execConfigId, execDispCountMap: execDispCountMap, prevDispCountMap: prevDispCountMap]
        notify 'evdas.activity.history.event.published', evHistoryActivityMap
        undoableDispositionList.removeAll([null])
        if(undoableDispositionList.size()>0){
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }
    }

    Integer changeAlertLevelDisposition(AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived = false){
        alertService.changeAlertLevelDisposition(saveActivityAndHistory, alertLevelDispositionDTO, isArchived)
    }

    EvdasHistory createEvdasHistoryForBulkDisposition(Map alertMap, AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived= false) {
        Map evHistory = [
                "productName"          : alertMap.substance,
                "eventName"            : alertMap.pt,
                "priority"             : alertMap.priority,
                "change"               : Constants.HistoryType.DISPOSITION,
                "justification"        : alertLevelDispositionDTO.justificationText,
                "isLatest"             : true,
                "disposition"          : alertLevelDispositionDTO.targetDisposition,
                "asOfDate"             : alertMap.periodEndDate,
                "assignedTo"           : alertMap.assignedTo,
                "assignedToGroup"      : alertMap.assignedToGroup,
                "createdBy"            : alertLevelDispositionDTO.loggedInUser.fullName,
                "modifiedBy"           : alertLevelDispositionDTO.loggedInUser.fullName,
                "configId"             : alertLevelDispositionDTO.configId,
                "evdasAlertId"         : !isArchived ? alertMap.id : null,
                newEv                  : alertMap.newEv,
                totalEv                : alertMap.totalEv,
                newEea                 : alertMap.newEea,
                totEea                 : alertMap.totEea,
                newHcp                 : alertMap.newHcp,
                totHcp                 : alertMap.totHcp,
                newSerious             : alertMap.newSerious,
                totalSerious           : alertMap.totalSerious,
                newObs                 : alertMap.newObs,
                totObs                 : alertMap.totObs,
                newFatal               : alertMap.newFatal,
                totalFatal             : alertMap.totalFatal,
                newMedErr              : alertMap.newMedErr,
                totMedErr              : alertMap.totMedErr,
                newLit                 : alertMap.newLit,
                totalLit               : alertMap.totalLit,
                newPaed                : alertMap.newPaed,
                totPaed                : alertMap.totPaed,
                newGeria               : alertMap.newGeria,
                totGeria               : alertMap.totGeria,
                newSpont               : alertMap.newSpont,
                totSpont               : alertMap.totSpont,
                totSpontEurope         : alertMap.totSpontEurope,
                totSpontNAmerica       : alertMap.totSpontNAmerica,
                totSpontJapan          : alertMap.totSpontJapan,
                totSpontAsia           : alertMap.totSpontAsia,
                totSpontRest           : alertMap.totSpontRest,
                newRc                  : alertMap.newRc,
                totRc                  : alertMap.totRc,
                "archivedEvdasAlertId" : isArchived ? alertMap.id : null,
                "dueDate"              : alertMap.dueDate
        ]

        EvdasHistory evdasHistory = new EvdasHistory(evHistory)
        evdasHistory.dateCreated = new Date()
        evdasHistory.lastUpdated = new Date()
        evdasHistory.executionDate = new Date()
        return evdasHistory
    }

    String prepareHQLForTotalCount() {
        String hqlTotalCount = "Select new map(executedAlertConfiguration.id as id, count(*) as cnt) from EvdasAlert where executedAlertConfiguration.id in" +
                " (:execConfigList) group by executedAlertConfiguration.id"
        hqlTotalCount
    }

    String prepareHQLForClosedCount() {
        String hqlClosedCount = "Select new map(executedAlertConfiguration.id as id, count(*) as cnt) from EvdasAlert where executedAlertConfiguration.id in" +
                " (:execConfigList) and disposition.id in (:dispositionList) group by executedAlertConfiguration.id"
        hqlClosedCount
    }

    String prepareHQLForNewCount() {
        String hqlNewCount = "Select new map(executedAlertConfiguration.id as id, count(*) as cnt) from EvdasAlert where isNew = true and executedAlertConfiguration.id in" +
                " (:execConfigList) group by executedAlertConfiguration.id"
        hqlNewCount
    }

    Map createCommonEvdasHistoryMap(def evdasAlert) {
        return [
                newEv                  : evdasAlert.newEv,
                totalEv                : evdasAlert.totalEv,
                newEea                 : evdasAlert.newEea,
                totEea                 : evdasAlert.totEea,
                newHcp                 : evdasAlert.newHcp,
                totHcp                 : evdasAlert.totHcp,
                newSerious             : evdasAlert.newSerious,
                totalSerious           : evdasAlert.totalSerious,
                newObs                 : evdasAlert.newObs,
                totObs                 : evdasAlert.totObs,
                newFatal               : evdasAlert.newFatal,
                totalFatal             : evdasAlert.totalFatal,
                newMedErr              : evdasAlert.newMedErr,
                totMedErr              : evdasAlert.totMedErr,
                newLit                 : evdasAlert.newLit,
                totalLit               : evdasAlert.totalLit,
                newPaed                : evdasAlert.newPaed,
                totPaed                : evdasAlert.totPaed,
                newGeria               : evdasAlert.newGeria,
                totGeria               : evdasAlert.totGeria,
                newSpont               : evdasAlert.newSpont,
                totSpont               : evdasAlert.totSpont,
                totSpontEurope         : evdasAlert.totSpontEurope,
                totSpontNAmerica       : evdasAlert.totSpontNAmerica,
                totSpontJapan          : evdasAlert.totSpontJapan,
                totSpontAsia           : evdasAlert.totSpontAsia,
                totSpontRest           : evdasAlert.totSpontRest,
                newRc                  : evdasAlert.newRc,
                totRc                  : evdasAlert.totRc,
        ]
    }


    ExecutedEvdasConfiguration fetchLastExecutionOfAlert(ExecutedEvdasConfiguration ec){
        ExecutedEvdasConfiguration lastExecutedConfiguration
        if (ec) {
            lastExecutedConfiguration = ExecutedEvdasConfiguration.createCriteria().get() {
                eq("name", ec.name)
                owner {
                    eq("id", ec.owner?.id)
                }
                'in'("executionStatus", [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING])
                ne("id", ec.id)
                eq("adhocRun", ec.adhocRun)
                eq("isDeleted", false)
                order("id", "desc")
                maxResults(1)
            }
        }
        lastExecutedConfiguration
    }
    List<Map> getSignalDetectionSummaryMap(List evdasAlertList,List<Map> evdasHistoryMap) {
        List<Map> signalList = []
        Integer workerCnt = Holders.config.signal.worker.count as Integer
        if (evdasAlertList) {
            ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
            List<Future<Map<String, String>>> futureList = evdasAlertList.collect { def evdasAlert ->
                executorService.submit({ ->
                    String dispositionJustification = evdasHistoryMap.find {
                        it.productName == evdasAlert.substance && it.pt == evdasAlert.pt && it.configId == evdasAlert.alertConfigurationId && it.change == Constants.HistoryType.DISPOSITION
                    }?.justification

                    String priorityJustification = evdasHistoryMap.find {
                        it.productName == evdasAlert.substance && it.pt == evdasAlert.pt && it.configId == evdasAlert.alertConfigurationId && it.change == Constants.HistoryType.PRIORITY
                    }?.justification

                    [
                            "product"              : evdasAlert.productName,
                            "event"                : evdasAlert.pt,
                            "justification"        : (dispositionJustification != null && dispositionJustification != "" && dispositionJustification != 'null') ? dispositionJustification : '-',
                            "currentDisposition"   : evdasAlert.disposition.displayName,
                            "priority"             : cacheService.getPriorityByValue(evdasAlert.priorityId).displayName,
                            "priorityJustification": priorityJustification ?: '-'
                    ]
                } as Callable)
            }
            futureList.each {
                signalList.add(it.get())
            }
            executorService.shutdown()
        }
        signalList
    }

    List<Map> generateEvdasHistoryMap(Long evdasConfigId){
        List<Map> evdasHistoryMap = []
        String sql_statement = SignalQueryHelper.evdas_history_change(evdasConfigId)
        log.info(sql_statement)
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        sqlQuery.list().each { row ->
            evdasHistoryMap.add([productName: row[0].toString(), pt: row[1],configId: row[2],justification:row[3].toString(),change: row[4].toString()])
        }
        evdasHistoryMap
    }

    List fetchEvdasAlertsForBulkOperations(def domain, List<Long> evdasAlertIdList) {
        List evdasAlertList = domain.createCriteria().list {
            'or' {
                evdasAlertIdList.collate(1000).each {
                    'in'("id", it)
                }
            }
        } as List
        evdasAlertList
    }

    private Activity createActivityForBulkUpdate(def evdasAlert, Disposition newDisposition, Disposition previousDisposition,
                                                 String justification, String validatedSignalName) {
        Date dispLastChange = null
        ActivityType activityType
        String changeDetails
        User loggedInUser = cacheService.getUserByUserId(userService.getCurrentUserId())
        if (previousDisposition.isValidatedConfirmed()) {
            activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.SignalAdded.value)
            changeDetails = "PEC attached with Signal '$validatedSignalName'"
        } else {
            activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)
            if (validatedSignalName) {
                changeDetails = "Disposition changed from '$previousDisposition.displayName' to '$newDisposition.displayName' and attached with signal '$validatedSignalName'"
            } else {
                changeDetails = "Disposition changed from '$previousDisposition.displayName' to '$newDisposition.displayName'"
            }
        }
        dispLastChange = evdasAlert.dispLastChange

        Activity activity = activityService.createActivityBulkUpdate(activityType, loggedInUser, changeDetails, justification,
                null, evdasAlert.substance, evdasAlert.pt, evdasAlert.assignedToId ? cacheService.getUserByUserId(evdasAlert.assignedToId) : null, null,
                evdasAlert.assignedToGroupId ? cacheService.getGroupByGroupId(evdasAlert.assignedToGroupId) : null, dispLastChange)
        activity
    }

    @Transactional
    void persistEvdasDueDate(List alertDueDateList) {
        def size = Holders.config.signal.batch.size
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            sql.withBatch(size, "update EVDAS_ALERT set disposition_id = :val0, due_date = :val1, review_date = :val2 " +
                    "where id = :val3".toString(), { preparedStatement ->
                alertDueDateList.each { def obj ->
                    preparedStatement.addBatch(val0: obj.dispositionId, val1: obj.dueDate?new Timestamp(obj.dueDate.getTime()):null,
                            val2: obj.reviewDate? new Timestamp(obj.reviewDate.getTime()) : null, val3: obj.id)
                }
            })
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    Map archivedAlertsList(Long id, Map params){
        List<Map> archivedAlertList = []
        int offset = params.start as int
        int max = params.length as int
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        String orderDirection = orderColumnMap?.dir?:Constants.DESCENDING_ORDER
        ExecutedEvdasConfiguration ec = ExecutedEvdasConfiguration.get(id)
        def totalCount = 0
        if (ec) {
            Group workflowGroup = userService.getUser().workflowGroup
            List<Integer> successExStatusVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(ec.configId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], Constants.AlertConfigType.EVDAS_ALERT).reportVersion.collect {
                (int) it
            }
            List<ExecutedEvdasConfiguration> executedEvdasConfigurationList = ExecutedEvdasConfiguration.createCriteria().list(offset: offset, max:max) {
                eq('configId', ec.configId)
                eq("isLatest", false)
                eq("adhocRun", ec.adhocRun)
                eq("isDeleted", false)
                eq("workflowGroup", workflowGroup)
                if (successExStatusVersions.size() > 0) {
                    or {
                        successExStatusVersions.collate(999).each {
                            'in'('numOfExecutions', it)
                        }
                    }
                }
                order("id","${orderDirection}")
            }
            List<ExecutedEvdasConfiguration> totalExecutedConfigurationList = ExecutedEvdasConfiguration.createCriteria().list() {
                eq('configId', ec.configId)
                eq("isLatest", false)
                eq("adhocRun", ec.adhocRun)
                eq("isDeleted", false)
                eq("workflowGroup", workflowGroup)
                if (successExStatusVersions.size() > 0) {
                    or {
                        successExStatusVersions.collate(999).each {
                            'in'('numOfExecutions', it)
                        }
                    }
                }
                order("id","asc")
            }

            totalCount = ExecutedEvdasConfiguration.createCriteria().list() {
                projections {
                    rowCount()
                }
                eq('configId', ec.configId)
                eq("isLatest", false)
                eq("adhocRun", ec.adhocRun)
                eq("isDeleted", false)
                eq("workflowGroup", workflowGroup)
                if (successExStatusVersions.size() > 0) {
                    or {
                        successExStatusVersions.collate(999).each {
                            'in'('numOfExecutions', it)
                        }
                    }
                }
            }

            archivedAlertList = executedEvdasConfigurationList.collect { archivedAlertMap(it, (totalExecutedConfigurationList.indexOf(it)+1)) }
        }
        [aaData:  archivedAlertList, recordsFiltered: totalCount, recordsTotal: totalCount]
    }

    Map archivedAlertMap(ExecutedEvdasConfiguration ec, Integer startingVersion) {
        List<Disposition> reviewedList = Disposition.findAllByValidatedConfirmedOrClosed(true, true)
        String timezone = getUserService().getUser().preference.timeZone
        [exConfigId   : ec.id,
         alertName    : ec.name,
         version      : startingVersion,
         product      : alertService.productSelectionValue(ec, false),
         description  : ec.description,

         caseCount    : ArchivedEvdasAlert.countByExecutedAlertConfiguration(ec),
         reviewedCases: ArchivedEvdasAlert.countByExecutedAlertConfigurationAndDispositionInList(ec, reviewedList),
         dateRange    : DateUtil.toDateStringWithoutTimezone(ec.dateRangeInformation.dateRangeStartAbsolute) +
                 " - " + DateUtil.toDateStringWithoutTimezone(ec.dateRangeInformation.dateRangeEndAbsolute),
         lastModified : DateUtil.toDateString(ec.lastUpdated, timezone)
        ]
    }

    def getDomainObject(Boolean isArchived) {
        isArchived ? ArchivedEvdasAlert : EvdasAlert
    }

    List getColumnListForExcelExport(String alertType, Long viewId) {
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType, viewId)
        JsonSlurper js = new JsonSlurper()
        Map caseListCols = js.parseText(viewInstance?.tempColumnSeq ?: viewInstance?.columnSeq)
        List columnList = []
        caseListCols.each { k, v ->
            if (v.containerView == 1) {
                columnList.addAll(getLabelNameList(v.label, v.name))
            }
        }
        def dispositionTo = [label: 'Disposition To', name: 'currentDisposition']
        if (columnList.contains(dispositionTo)) {
            columnList.remove(dispositionTo)
        }
        Map disposition = [label: 'Disposition', name: 'currentDisposition']
        Map currDisposition = [label: 'Current Disposition', name: 'disposition']
        if (columnList.contains(disposition) && columnList.contains(currDisposition)) {
            columnList = columnList - currDisposition
        }
        columnList
    }

    List getLabelNameList (String currentLabel, String currentName) {
        List tempCoulmnList = []
        String intvCumString = currentLabel
        String[] intvCumArray
        String label
        String name

        if(intvCumString != "IME/DME" && intvCumString?.contains("/")){
            intvCumArray = intvCumString.split("/")
        }

        if (intvCumArray) {
            intvCumArray.each {
                label = it
                name = Holders.config.evdasColumnExcelExportMap[it.trim()]
                tempCoulmnList << [label: label, name: name]
            }
        }else {
            tempCoulmnList << [label: currentLabel, name: currentName]
        }
        tempCoulmnList
    }

    void createEvdasAlert(Long configId, Long executedConfigId, List<Map> alertData) throws Exception {

        log.info("Data mining in PV Datahub gave " + alertData.size() + " PE combinations.")
            EvdasConfiguration config
            try {
                config = EvdasConfiguration.get(configId)
                ExecutedEvdasConfiguration executedConfig = ExecutedEvdasConfiguration.get(executedConfigId)
                List<Date> dateRangeStartEndDate = config.dateRangeInformation.getReportStartAndEndDate()
                log.info("Fetching existing Evdas History List")

                List<EvdasHistory> existingPEHistoryList = EvdasHistory.createCriteria().list {
                    eq("configId", config.id)
                    order("lastUpdated","desc")
                } as List<EvdasHistory>

                dataObjectService.setExistingEvdasHistoryList(config.id, existingPEHistoryList)
                log.info("Existing Evdas History List fetched")

                String query
                Session session = sessionFactory.currentSession
                List<LastReviewDurationDTO> lastReviewDurationDTOList
                List<Long> prevExecConfigIds = alertService.getEvdasPrevExConfigIds(executedConfig,configId)

                if (prevExecConfigIds) {
                    log.info("Fetching LastReviewDurationDTOs")
                    query = config.dateRangeInformation.dateRangeEnum == config.dateRangeInformation?.dateRangeEnum.CUMULATIVE ?
                            SignalQueryHelper.evdas_cumulative_last_review_sql(prevExecConfigIds) : SignalQueryHelper.evdas_custom_last_review_sql(prevExecConfigIds)
                    lastReviewDurationDTOList = alertService.getTransformedResultList(LastReviewDurationDTO.class.name, query, session)
                    log.info("Fetched LastReviewDurationDTOs")

                    dataObjectService.setLastReviewDurationMap(executedConfig.id, lastReviewDurationDTOList)
                    dataObjectService.setCurrentEndDateMap(executedConfig.id, executedConfig.dateRangeInformation.dateRangeEndAbsolute ?: dateRangeStartEndDate[1])
                }

                def selectedEvdasAlertMap = setFlagsForAlert(alertData, config.id)
                Disposition defaultDisposition = config?.getWorkflowGroup()?.defaultEvdasDisposition
                String defaultDispositionValue = defaultDisposition?.value
                List<BusinessConfiguration> businessConfigurationList = BusinessConfiguration.findAllByDataSourceAndIsGlobalRuleAndEnabled(Constants.DataSource.EUDRA, true, true)
                dataObjectService.setEnabledBusinessConfigList(Constants.DataSource.EUDRA,businessConfigurationList)
                boolean isProductGroup = config.productGroupSelection != null
                if (isProductGroup && config.productGroupList) {
                    List<BusinessConfiguration> prodGrpBusinessConfigList = BusinessConfiguration.createCriteria().list {
                        eq('enabled', true)
                        sqlRestriction("""Id in (SELECT id 
                            FROM business_configuration, 
                            LATERAL (
                                SELECT jsonb_array_elements(PRODUCT_GROUP_SELECTION::jsonb) AS element
                            ) t2
                            WHERE (t2.element->>'id')::numeric IN (${config.productGroupList}) -- Replace with actual list or handle case with an empty list
                            AND PRODUCT_GROUP_SELECTION IS NOT NULL
                            )""")
                    } as List<BusinessConfiguration>

                    dataObjectService.setEnabledBusinessConfigProductGrpList(executedConfigId, prodGrpBusinessConfigList)
                }
                List eiList = alertService.getEmergingIssueList()

                Integer workerCnt = Holders.config.signal.worker.count as Integer
                List<EvdasAlert> resultData = []
                ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
                log.info("Thread Starts")
                List listednessData = []
                if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
                    listednessData = aggregateCaseAlertService.evaluateListedness(executedConfig.id, null,false)
                }
                List<Future<EvdasAlert>> futureList = alertData.collect { Map data ->
                    executorService.submit({ ->
                        createEvdasAlertParallely(data, config, executedConfig,selectedEvdasAlertMap, dateRangeStartEndDate, defaultDisposition,eiList,listednessData)
                    } as Callable)
                }
                futureList.each {
                    resultData.add(it.get())
                }
                executorService.shutdown()
                log.info("Thread Ends")

                batchPersistData(resultData, executedConfig, config, defaultDispositionValue, prevExecConfigIds[0] as Long)
                if (!config.adhocRun) {
                    archiveService.moveDatatoArchive(executedConfig, EvdasAlert , prevExecConfigIds)
                }
                alertService.updateOldExecutedConfigurationsEvdasWithDispCount(config, executedConfig.id,ExecutedEvdasConfiguration, resultData.countBy {it.dispositionId})
                if (!config.adhocRun) {
                    archiveService.moveDatatoArchive(executedConfig, EvdasAlert , prevExecConfigIds)
                }
            } catch (Throwable ex) {
                throw ex
            }finally{
                dataObjectService.clearExistingEvdasHistoryList(config.id)
            }
    }

    void insertEvdasAlertActions(Session session, Long executedConfigId, Long prevExecConfigId) {
        // Retrieve the alert actions using a helper method
        String sqlStatement = SignalQueryHelper.evdas_alert_actions(executedConfigId, prevExecConfigId)
        List<Map<String, String>> alertIdAndActionIdList = session.createSQLQuery(sqlStatement)
                .list()
                .collect { row -> [col1: row[0].toString(), col2: row[1].toString(), col3: '1'] }
        // Log the start of the batch execution
        log.info("Batch execution of TABLE EVDAS_ALERT_ACTIONS started")

        // Insert the alert actions into the database
        String insertActionSingleAlertQuery = SignalQueryHelper.insert_evdas_actions_sql()
        alertService.batchPersistForStringParameters(session, alertIdAndActionIdList, insertActionSingleAlertQuery, 3)

        // Log the end of the batch execution
        log.info("Batch execution of TABLE EVDAS_ALERT_ACTIONS ended")

        // Update the actions count in the alert
        log.info("Now updating the Actions Count in Alert")
        String actionCountSql = SignalQueryHelper.evdas_alert_actions_count(executedConfigId)
        session.createSQLQuery(actionCountSql).executeUpdate()
        log.info("Action Count For Alert updated")
    }

    EvdasAlert createEvdasAlertParallely(Map data, EvdasConfiguration config, ExecutedEvdasConfiguration executedConfig, Map selectedEvdasAlertMap,
                                         List<Date> dateRangeStartEndDate, Disposition defaultDisposition,List eiList, List listednessData = []) {
        String rorValue = "0.000"
        if (data.rorValue) {
            if (data.rorValue[0] == ".") {
                rorValue = "0" + data.rorValue
            } else {
                rorValue = data.rorValue
            }
        }

        String flags = selectedEvdasAlertMap.get(data.substance + "-" + data.ptCode) ?: Constants.Commons.BLANK_STRING

        String impEvents = alertService.setImpEventValue(data.pt, data.ptCode, data.substance, data.substanceId, eiList, true)
        EvdasAlert evdasAlert = new EvdasAlert([
                detectedDate              : new Date(),
                alertConfiguration        : config,
                executedAlertConfiguration: executedConfig,
                name                      : executedConfig.name,
                createdBy                 : config.createdBy,
                modifiedBy                : config.modifiedBy,
                disposition               : defaultDisposition,
                dateCreated               : executedConfig.dateCreated,
                lastUpdated               : executedConfig.dateCreated,
                adhocRun                  : config.adhocRun,
                frequency                 : executedConfig.frequency,
                periodStartDate           : dateRangeStartEndDate[0],
                periodEndDate             : dateRangeStartEndDate[1],
                rorValue                  : rorValue,
                allRor                    : rorValue,
                totalFatal                : data.totalFatal,
                newFatal                  : data.newFatal,
                totalSerious              : data.totalSerious,
                newSerious                : data.newSerious,
                totalEv                   : data.totalEv,
                newEv                     : data.newEv,
                totalEvLink               : data.totalEvLink,
                newEvLink                 : data.newEvLink,
                dmeIme                    : data.dmeIme,
                pt                        : data.pt,
                ptCode                    : data.ptCode,
                soc                       : data.soc,
                newLit                    : data.newLit,
                totalLit                  : data.totalLit,
                sdr                       : data.sdr,
                smqNarrow                 : data.smqNarrow,
                substance                 : data.substance,
                substanceId               : data.substanceId,
                hlgt                      : data.hlgt,
                hlt                       : data.hlt,
                newEea                    : data.newEea,
                totEea                    : data.totEea,
                newHcp                    : data.newHcp,
                totHcp                    : data.totHcp,
                newMedErr                 : data.newMedErr,
                totMedErr                 : data.totMedErr,
                newObs                    : data.newObs,
                totObs                    : data.totObs,
                newRc                     : data.newRc,
                totRc                     : data.totRc,
                newPaed                   : data.newPaed,
                totPaed                   : data.totPaed,
                newGeria                  : data.newGeria,
                totGeria                  : data.totGeria,
                europeRor                 : data.europeRor.toString().startsWith('.') ? "0" + data.europeRor : data.europeRor,
                northAmericaRor           : data.northAmericaRor.toString().startsWith('.') ? "0" + data.northAmericaRor : data.northAmericaRor,
                japanRor                  : data.japanRor.toString().startsWith('.') ? "0" + data.japanRor : data.japanRor,
                asiaRor                   : data.asiaRor.toString().startsWith('.') ? "0" + data.asiaRor : data.asiaRor,
                restRor                   : data.restRor.toString().startsWith('.') ? "0" + data.restRor : data.restRor,
                ratioRorPaedVsOthers      : data.ratioRorPaedVsOthers.toString().startsWith('.') ? "0" + data.ratioRorPaedVsOthers : data.ratioRorPaedVsOthers,
                ratioRorGeriatrVsOthers   : data.ratioRorGeriatrVsOthers.toString().startsWith('.') ? "0" + data.ratioRorGeriatrVsOthers : data.ratioRorGeriatrVsOthers,
                changes                   : data.changes ? data.changes : '-',
                sdrPaed                   : data.sdrPaed,
                sdrGeratr                 : data.sdrGeratr,
                newSpont                  : data.newSpont,
                totSpontEurope            : data.totSpontEurope,
                totSpontNAmerica          : data.totSpontNAmerica,
                totSpontJapan             : data.totSpontJapan,
                totSpontAsia              : data.totSpontAsia,
                totSpontRest              : data.totSpontRest,
                totSpont                  : data.totSpont,
                flags                     : flags,
                isNew                     : flags.equalsIgnoreCase("New"),
                attributes                : new JsonBuilder(data.attributes).toPrettyString(),
                listedness                : null,
                impEvents                 : impEvents
        ])
        if(evdasAlert.substanceId == -1){
            if(executedConfig?.productSelection){
                def productJson = JSON.parse(executedConfig.productSelection)
                productJson.each{ index,product ->
                    if(product){
                        if(product[0]['name'] == evdasAlert.substance){
                            evdasAlert.substanceId = product[0]['id'] as Integer
                        }
                    }
                }
            }else if(executedConfig?.productGroupSelection){
                def productGroupJson = JSON.parse(executedConfig.productGroupSelection)
                String alertSubstance = evdasAlert.substance + " ("
                productGroupJson.each{ product ->
                    if(product.name.startsWith(alertSubstance)){
                        evdasAlert.substanceId = product.id as Integer
                    }
                }
            }
        }
        if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
            listednessData?.each {
                if (it["ptCode"] == data.ptCode) {
                    evdasAlert.listedness = evdasAlert.setEvdasListednessValue(it["listed"] as String)?:false
                }
                if( evdasAlert.listedness == null){
                    evdasAlert.listedness = false
                }
            }
        } else {
            evdasAlert.listedness = data.listedness
        }
        if (evdasAlert.impEvents && evdasAlert.impEvents != Constants.Commons.BLANK_STRING) {
            String[] impEventList = evdasAlert.impEvents?.split(',')
            evdasAlert.evImpEventList = []
            impEventList.each {
                if (it) {
                    evdasAlert.evImpEventList.add(it.trim())
                }
            }
        }
        setWorkflowMgmtStates(evdasAlert, config)
        evdasAlert.initialDisposition = evdasAlert.disposition
        evdasAlert.initialDueDate = evdasAlert.dueDate

        evdasAlert
    }
    // set alert disposition to initial disposition and create History for Evdas Alert
    void dissociateEvdasAlertFromSignal(def alert, Disposition targetDisposition, String justification, ValidatedSignal signal,
                                        Boolean isArchived) {
        try {
            Disposition previousDisposition = alert.disposition
            validatedSignalService.changeToInitialDisposition(alert, signal, targetDisposition)
            saveAlertCaseHistory(alert, justification)
            EvdasHistory evdasHistoryMap = createEvdasHistoryMap(alert, Constants.HistoryType.DISPOSITION, justification, isArchived)
            evdasHistoryMap.save(flush: true)
            String changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition' and dissociated from signal '$signal.name'"
            Map attrs = [product: getNameFieldFromJson(alert.alertConfiguration.productSelection),
                         event  : getNameFieldFromJson(alert.alertConfiguration.eventSelection)]
            activityService.createEvdasActivity(alert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.DispositionChange), userService.getUser(),
                    changeDetails, justification, attrs, alert.productName, alert.pt, alert.assignedTo, null, alert.assignedToGroup)
        }
        catch (Exception e) {
            e.printStackTrace()
        }
    }
    List getEvdasAlertCriteriaData(ExecutedEvdasConfiguration ec, Map params, String alertType = null){
        String none = Constants.Commons.NONE.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.NONE.toLowerCase().substring(1)
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType ? alertType : Constants.AlertConfigType.EVDAS_ALERT, params?.viewId as Long)
        //current advandedFilter in view
        AdvancedFilter advancedFilter = AdvancedFilter.findById(params?.advancedFilterId)
        EvdasConfiguration config = EvdasConfiguration.findByName(ec?.name)
        List<Date> dateRange = ec?.dateRangeInformation?.getReportStartAndEndDate()
        String reportDateRange
        if (config?.dateRangeInformation?.dateRangeEnum == config?.dateRangeInformation?.dateRangeEnum?.CUMULATIVE) {
            Date dateRangeEnd = ec?.dateCreated
            reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
        } else {
            reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
        }
        JsonSlurper js = new JsonSlurper()
        def caseListCols = js.parseText(viewInstance.columnSeq)
        def filterList = null
        def sorting = null
        if(viewInstance.filters) {
             filterList = js.parseText(viewInstance.filters)
        }
        if(viewInstance.sorting) {
            sorting = js.parseText(viewInstance.sorting)
        }

        String sortColoum = ""
        String coloumLevelFilter = ""

        Map fixedColSeq = [
                6: "PT",
                5: "SOC",
                4: "Substance",
                3: "A",
                2: "Flag"
        ]
        Map fixedColName = [
                "pt": "PT",
                "soc": "SOC",
                "substance": "Substance",
        ]

        sorting.each { filter ->
            caseListCols.each{
                if(filter.key == it.value.seq.toString()){
                    sortColoum = it.value.label + ": " + filter.value
                }
            }
        }
        if(sortColoum == ""){
            sorting.each{ filter ->
                fixedColSeq.each{
                    if(filter.key == it.key.toString()){
                        sortColoum = it.value + ": " + filter.value
                    }
                }
            }
        }

        if(params.column != "undefined"){
            List<Map> filterColumns = grailsApplication.config.configurations.agaColumnOrderList.clone() as List<Map>
            filterColumns.each{
                if(it.name == params.column){
                    sortColoum = it.label + ": " + params.sorting
                }
            }
        }

        filterList.each{ filter ->
            caseListCols.each{
                if(filter.key == it.value.seq.toString()){
                    coloumLevelFilter = it.value.label + ": " + filter.value
                }
            }
        }

        Map filterMap = [:]
        if (params.filterList && params.filterList != "{}") {
            def jsonSlurper = new JsonSlurper()
            filterMap = jsonSlurper.parseText(params.filterList)
        }

        List filtersList = []
        String filter = ""
        //Added for fixed column, PVS-60177
        filterMap?.each{ object ->
            fixedColName.each{
                if(object.key == it.key.toString()){
                    filtersList.add(it.value + ": " +object.value)
                }
            }
        }
        filterMap.each{object ->
            caseListCols.each{
                if(object.key == it.value.name){
                    filtersList.add(it.value.label + ": " +object.value)
                }
            }
        }
        filter = filtersList.join("\n")
        String datasheets = ""
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
        if(!TextUtils.isEmpty(ec?.selectedDataSheet)){
            datasheets = dataSheetService.formatDatasheetMap(ec)?.text?.join(',')
        }else {
            Boolean isProductGroup = !TextUtils.isEmpty(ec.productGroupSelection)
            String products = ec.productGroupSelection?:ec.productSelection
            datasheets =  dataSheetService.fetchDataSheets(products,enabledSheet, isProductGroup, false)?.dispName?.join(',')
        }
        List criteriaSheetList = [
                ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': ec?.name],
                ['label': Constants.CriteriaSheetLabels.DESCRIPTION, 'value': ec?.description ? ec?.description : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': (getNameFieldFromJson(ec?.productSelection)) ?: getGroupNameFieldFromJson(ec?.productGroupSelection)],
                ['label': Constants.CriteriaSheetLabels.DATASHEETS, 'value': datasheets?:""],
                ['label': Constants.CriteriaSheetLabels.EVENT_SELECTION, 'value': ec.eventSelection ? getAllEventNameFieldFromJson(ec.eventSelection) : (getGroupNameFieldFromJson(ec.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)], //Added for PVS-55056
                ['label': Constants.CriteriaSheetLabels.QUERY_NAME, 'value': config?.queryName ? config.queryName : none],
                ['label': Constants.CriteriaSheetLabels.DATE_RANGE, 'value': reportDateRange ? reportDateRange : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.VIEW, 'value': viewInstance ? viewInstance.name : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.FILTER, 'value': advancedFilter ? advancedFilter?.name : Constants.Commons.NA_LISTED],
                ['label': Constants.CriteriaSheetLabels.COLUMN_LEVEL_FILTER, value: filter?:""],
                ['label': Constants.CriteriaSheetLabels.SORT_ORDER, 'value': sortColoum?:Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': params.quickFilterDisposition?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.PEC_COUNT, 'value': params.totalCount? params.totalCount : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.DATE_CREATED, 'value': DateUtil.stringFromDate(ec?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName?:""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
        ]
        return criteriaSheetList
    }

    List createCriteriaSheetListDrillDown(Map params,Long alertId,boolean autoAttachement, StringBuilder evdasEntityValue){
        String none = Constants.Commons.NONE.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.NONE.toLowerCase().substring(1)
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        def ec
        String ptEvent = ""
        EvdasConfiguration config
        String reportDateRange
        def alert
        if (params.callingScreen == Constants.Commons.DASHBOARD || params.callingScreen == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
            // alert type will be evdas only in dashboard case
                if (params?.isArchived == "true") {
                    alert = ArchivedEvdasAlert.get(alertId as Long)
                } else {
                    alert = EvdasAlert.get(alertId as Long)
                }
                evdasEntityValue.append(alert?.getInstanceIdentifierForAuditLog())
                ec = alert?.executedAlertConfiguration
                config = EvdasConfiguration.findByName(ec?.name)
                List<Date> dateRange = ec?.dateRangeInformation?.getReportStartAndEndDate()
                if (config?.dateRangeInformation?.dateRangeEnum == config?.dateRangeInformation?.dateRangeEnum?.CUMULATIVE) {
                    Date dateRangeEnd = ec?.dateCreated
                    reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
                } else {
                    reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
                }
                ptEvent = alert?.pt
        }else {
            if (params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                ec = ExecutedConfiguration.get(params.id as Long)
                config = EvdasConfiguration.findByName(ec?.name)
                reportDateRange = ec?.evdasDateRange
                if (params?.isArchived == "true") {
                    ptEvent = ArchivedAggregateCaseAlert.get(alertId as Long)?.pt
                    evdasEntityValue.append(ArchivedAggregateCaseAlert.get(alertId as Long)?.getInstanceIdentifierForAuditLog())
                } else {
                    ptEvent = AggregateCaseAlert.get(alertId as Long)?.pt
                    evdasEntityValue.append(AggregateCaseAlert.get(alertId as Long)?.getInstanceIdentifierForAuditLog())
                }
            } else {
                ec = ExecutedEvdasConfiguration.get(params.id as Long)
                config = EvdasConfiguration.findByName(ec?.name)
                List<Date> dateRange = ec?.dateRangeInformation?.getReportStartAndEndDate()
                if (config?.dateRangeInformation?.dateRangeEnum == config?.dateRangeInformation?.dateRangeEnum?.CUMULATIVE) {
                    Date dateRangeEnd = ec?.dateCreated
                    reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
                } else {
                    reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
                }
                if (params?.isArchived == "true") {
                    ptEvent = ArchivedEvdasAlert.get(alertId as Long)?.pt
                    evdasEntityValue.append(ArchivedEvdasAlert.get(alertId as Long)?.getInstanceIdentifierForAuditLog())
                } else {
                    ptEvent = params.alertType == Constants.AlertConfigType.EVDAS_ALERT ? EvdasAlert.get(alertId as Long)?.pt : EvdasOnDemandAlert.get(alertId as Long)?.pt
                    evdasEntityValue.append(params.alertType == Constants.AlertConfigType.EVDAS_ALERT ? EvdasAlert.get(alertId as Long)?.getInstanceIdentifierForAuditLog() : EvdasOnDemandAlert.get(alertId as Long)?.getInstanceIdentifierForAuditLog())
                }
            }
        }
        String description = ""
        if(autoAttachement){
            description = params?.description ?: Constants.Commons.BLANK_STRING
        } else{
            description =  ec?.description ?: Constants.Commons.BLANK_STRING
        }
        List criteriaSheetList = [
                ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': ec?.name ? ec?.name :Constants.Commons.BLANK_STRING ],
                ['label': Constants.CriteriaSheetLabels.DESCRIPTION, 'value': description],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': (getNameFieldFromJson(ec?.productSelection)) ?: getGroupNameFieldFromJson(ec?.productGroupSelection)],
                ['label': Constants.CriteriaSheetLabels.EVENT, 'value': ptEvent ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.QUERY_NAME, 'value': config?.queryName ? config?.queryName : none],
                ['label': Constants.CriteriaSheetLabels.DATE_RANGE, 'value': reportDateRange ? reportDateRange : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.FILTER_TEXT, 'value': params.filterText ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.SORT_ORDER, 'value': params.sortOrder ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.DATE_CREATED, 'value': ec?.dateCreated ? DateUtil.stringFromDate(ec?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) : ""],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName?:""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
        ]
        return criteriaSheetList
    }

    MultipartFile prepareFileEVDASCaseList(List<Map> drillDownData, Map params, String fileName,Long alertId) {
        params.outputFormat = Holders.config.evdas.drilldown.reportFormat
        StringBuilder evdasEntityValue = new StringBuilder()
        params.criteriaSheetList = evdasAlertService.createCriteriaSheetListDrillDown(params,alertId,true,evdasEntityValue)
        File file = dynamicReportService.createEVDASDrillDownReport(new JRMapCollectionDataSource(drillDownData), params, fileName)
        MultipartFile reportFile = validatedSignalService.fileToMultipartFile(file)
        println reportFile.name
        reportFile
    }

    String getCaseListFileName(Long alertId, Boolean isArchived = false,String alertType=null){
        User currentUser = userService.getUser()
        def domain
        if(alertType==Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
            domain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert

        }else{
            domain = isArchived ? ArchivedEvdasAlert : EvdasAlert
        }

        def alert = domain.findById(alertId)
        String pt = alert?.pt?:""
        String ingredient = ""
        if(alertType==Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
            ingredient = alert?.productName?:""

        }else{
            ingredient = alert?.substance?:""
        }
        String timezone = currentUser.preference.timeZone
        ingredient + "-" + pt + " Case List: " + DateUtil.stringFromDate(new Date(), DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone)
    }

    void attachCaseListingFile(List<Map> drillDownData, Map params, MultipartHttpServletRequest request){
        try {
            String alertType=params?.alertType
            boolean isArchived=params?.boolean('isArchived')
            List<String> fileNameList = []
            def alertId = params?.alertId
            def domain
            if(alertType==Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                domain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert

            }else{
                domain = isArchived ? ArchivedEvdasAlert : EvdasAlert
            }
            def evdasAlert = domain.findById(alertId as Long)
            params.id = evdasAlert?.executedAlertConfigurationId
            User currentUser = userService.getUser()
            String fileName = getCaseListFileName(alertId as Long, params.boolean('isArchived'),alertType)
            if (fileName instanceof String) {
                fileNameList = [fileName]
            }
            MultiValueMap<String, Object> fileMap = new LinkedMultiValueMap<>()
            fileMap.add("attachments", prepareFileEVDASCaseList(drillDownData, params, fileName,alertId))
            request.multipartFiles = fileMap

            Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileNameList, evdasAlert, request, fileName)

            String fileDescription = params.description
            List<Attachment> attachments = evdasAlert.getAttachments().sort { it.dateCreated }
            if (attachments) {
                List<Integer> bulkAttachmentIndex = 1..filesStatusMap?.uploadedFiles?.size()
                bulkAttachmentIndex.each {
                    Attachment attachment = attachments[-it]
                    AttachmentDescription attachmentDescription = new AttachmentDescription()
                    attachmentDescription.attachment = attachment
                    attachmentDescription.createdBy = currentUser.fullName
                    attachmentDescription.description = fileDescription
                    attachmentDescription.save(flush: true)
                }
            }
            def fileNameWithExt = filesStatusMap?.uploadedFiles*.originalFilename[0]

            def extIndex=fileNameWithExt.lastIndexOf(".")
            String extension=fileNameWithExt.substring(extIndex)
            if(!fileName.contains(extension)){
                fileName="["+fileName+extension+"]"
            }

            if (filesStatusMap?.uploadedFiles) {
                activityService.createActivityForEvdas(evdasAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentAdded),
                        currentUser, "Attachment " + fileName + " is added", null,
                        [product: getNameFieldFromJson(evdasAlert.alertConfiguration.productSelection), event: getNameFieldFromJson(evdasAlert.alertConfiguration.eventSelection)],
                        evdasAlert.productName, evdasAlert.pt, evdasAlert.assignedTo, null, evdasAlert.assignedToGroup)
            }
        } catch(Exception ex){
            log.error(ex.printStackTrace())
            throw ex
        }
    }

    List<Map> getFilteredDrillDownData(String filterText, List<Map> drillDownData){
        List<Map> filteredDrillDownData = []
        String searchedTerm = filterText
        boolean isTextFound = false
        drillDownData?.each { Map dataMap ->
            isTextFound = false
            dataMap.each { def key, def value ->
                if((key.toString().toLowerCase().equals("eventAddInfoList")||key.toString().toLowerCase().equals("reactList")) &&
                        (value.toString().toLowerCase().contains(searchedTerm.replace(", ",",").toLowerCase()))){
                    isTextFound = true
                }else if(!key.toString().toLowerCase().equals("icsr") && value.toString().toLowerCase().contains(searchedTerm.toLowerCase())){
                    isTextFound = true
                }
                if(isTextFound && !filteredDrillDownData.contains(dataMap)){
                    filteredDrillDownData.add(dataMap)
                }
            }
        }
        filteredDrillDownData
    }

    List<Map> getFinalDrillDownData(List<Map> filteredDrillDownData, Map params){
        if(params.filterText){
            filteredDrillDownData = getFilteredDrillDownData(params.filterText, filteredDrillDownData)
        }
        if(params.sortedCol && params.sortedDir){
            params.sortOrder = getSortedColumn(params.sortedCol as String, params.sortedDir as String)
            if(filteredDrillDownData)
                filteredDrillDownData = getSortedList(params.sortedCol as String, params.sortedDir as String, filteredDrillDownData)
        }
        filteredDrillDownData
    }

    String getSortedColumn(String colNum, String colDir){
        Map colDataMap = ["0" : "Case Number", "1" : "WWID", "2" : "HCP", "3" : "Age Group", "4" : "Gender", "5" : "Receipt Date", "6" : "Source Country", "7" : "Suspected Drug",
                          "8" : "Suspect Drugs w/Additional Info", "9" : "Con-Med", "10" : "ConMed Drugs w/Additional Info", "11" : "Event Terms", "12" : "Event Terms w/Additional Info", "13" : "ICSR Forms"]
        colDataMap.get(colNum) + ": " + colDir
    }

    List<Map> getSortedList(String colToBeSorted, String sortedDir, List<Map> drillDownData){
        List<Map> sortedDataList = []
        Map evdasCaseMap = ["0" : "caseNum", "1" : "caseIdentifier", "2" : "hcp", "3" : "ageGroup", "4" : "gender", "5" : "dateFirstReceipt", "6" : "primarySourceCountryDesc", "7" : "suspList",
                           "8" : "suspAddInfoList", "9" : "concList", "10" : "conMedInfoList", "11" : "reactList", "12" : "eventAddInfoList", "13" : "icsr"]
        List<String> colList
        if(colToBeSorted=="5"){
            colList = drillDownData.collect { it[evdasCaseMap[colToBeSorted]] as String}.sort{ DateUtil.parseDate(it.toString()) }
        }else{
            colList = drillDownData.collect { it[evdasCaseMap[colToBeSorted]] as String}.sort{ it.toString().toLowerCase() }
        }

        if(sortedDir.equals("desc"))
            colList = colList.reverse()

        colList?.each { String col ->
            drillDownData?.each { Map dataMap ->
                dataMap.each { def key, def value ->
                    if(col.equals(value.toString()) && !sortedDataList.contains(dataMap)){
                        sortedDataList.add(dataMap)
                    }
                }
            }
        }
        sortedDataList
    }

    List<Long> getAlertIdsForAttachments(Long alertId, boolean isArchived = false){
        def domain = getDomainObject(isArchived)
        List<Long> evdAlertList = []
        List<Long> archivedAlertIds = []
        def evdasAlert
        ArchivedEvdasAlert.withTransaction {
            evdasAlert = domain.findById(alertId.toInteger())
            archivedAlertIds = ExecutedEvdasConfiguration.findAllByConfigId(evdasAlert.alertConfiguration.id).collect {
                it.id
            }
            evdAlertList = ArchivedEvdasAlert.createCriteria().list {
                projections {
                    property('id')
                }
                eq('substance', evdasAlert?.substance)
                eq('pt', evdasAlert?.pt)
                if (archivedAlertIds){
                    or {
                        archivedAlertIds.collate(1000).each{
                            'in'('executedAlertConfiguration.id', it)
                        }
                    }
                }
            } as List<Long>

            archivedAlertIds = evdAlertList.findAll {
                ArchivedEvdasAlert.get(it).executedAlertConfiguration.id < evdasAlert.executedAlertConfiguration.id
            }
        }
        archivedAlertIds + evdasAlert.id
    }

    def fetchPreviousAlertsListEvdas(List prevExecConfigIdList){
        def prevAlertsList
        ArchivedEvdasAlert.withTransaction {
                prevAlertsList = ArchivedEvdasAlert.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property('id','id')
                        property('substance','substance')
                        property('pt','pt')
                    }
                    if (prevExecConfigIdList){
                        or {
                            prevExecConfigIdList.collate(1000).each{
                                'in'('executedAlertConfiguration.id', it)
                            }
                        }
                    }
                } as List<Long>
        }
        return prevAlertsList
    }

    boolean checkAttachmentsForAlert(List<Long> alertIds){
        boolean  isAttached = false
        ArchivedEvdasAlert.withTransaction {
            alertIds.each { Long evdAlertId ->
                def evdAlert = ArchivedEvdasAlert.get(evdAlertId) ?: EvdasAlert.get(evdAlertId)
                if (evdAlert.attachments)
                    isAttached = true
            }
        }
        isAttached
    }

    List fetchPrevPeriodExecConfig(Long configId, Long executedConfigId) {
        List<ExecutedEvdasConfiguration> prevExecs = ExecutedEvdasConfiguration.createCriteria().list {
            eq("configId", configId)
            eq("isEnabled", true)
            lt("id", executedConfigId)
            order("dateCreated", "desc")
            maxResults(5)
        }
        return prevExecs
    }
    /**
     * Get valid previous executed configurations for the given configuration
     * @param configuration
     * @return List<ExecutedEvdasConfiguration>
     */
    List<ExecutedEvdasConfiguration> getValidPrevExecConfigurations(EvdasConfiguration configuration) {
        List<ExecutedEvdasConfiguration> prevExecs = ExecutedEvdasConfiguration.createCriteria().list {
            eq("configId", configuration.id)
        } as List<ExecutedEvdasConfiguration>
        List<Long> exeIdList = prevExecs*.id
        Set<Long> validIds = []
        exeIdList?.collate(1000)?.each { idBatch ->
            validIds.addAll(ExecutionStatus.createCriteria().list {
                'in'('executedConfigId', idBatch)
                eq('type', Constants.AlertConfigType.EVDAS_ALERT)
                eq('executionStatus', ReportExecutionStatus.COMPLETED)
                projections {
                    property('executedConfigId')
                }
            } as Set)
        }
        prevExecs = prevExecs?.findAll { validIds.contains(it.id) }
        prevExecs?.sort { a, b -> b.id <=> a.id }
        return prevExecs
    }
}
