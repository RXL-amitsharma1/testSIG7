package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.ActivityDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.SignalQueryHelper
import groovy.sql.Sql
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.exception.SQLGrammarException
import org.hibernate.transform.Transformers
import org.hibernate.type.LongType
import org.hibernate.type.StringType
import org.hibernate.type.TimestampType
import org.joda.time.DateTime

import java.sql.Timestamp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Slf4j
class ActivityService {

    def sessionFactory
    def dataObjectService
    def userService
    def cacheService
    def messageSource
    def dataSource

    def list() {
        Activity.all
    }

    def listByAlert(Long id) {
        String currentUserName = userService.getUser().username
        List activityList = Activity.where { alert.id == id  && (privateUserName == null || privateUserName == currentUserName)}.order('timestamp', 'desc').list()
    }

    @Transactional
    def create(alertId, type, user, details, justification) {
        if (alertId) {
            Alert alert = Alert.findById(alertId)
            if (alert) {
                Activity activity = new Activity(type: type, performedBy: user,
                        details: details, timestamp: DateTime.now(), alert: alert, justification: justification)

                if (alert.addToActivities(activity).save()) {
                    log.debug("New activity with id ${activity.id} has been created.")
                    return activity
                } else {
                    log.error("Activity creation attempt failed")
                    return null
                }
            }
        }
        return null
    }

    @Transactional
    def createUpdateActivityForSignal(signalId, type, user, details, justification, assignedToGroup, assignedTo) {
        ValidatedSignal validatedSignal = ValidatedSignal.findById(signalId)
        if (validatedSignal) {

            def activity = new Activity(type: type, performedBy: user,
                    details: details, timestamp: DateTime.now(), justification: justification)

            if (assignedTo) {
                activity.assignedTo = assignedTo
            } else {
                activity.assignedToGroup = assignedToGroup
            }

            if (validatedSignal.addToActivities(activity).save()) {
                log.debug("New activity with id ${activity.id} has been created.")
                return activity
            } else {
                log.error("Activity creation attempt failed")
                return null
            }
        }
        return null
    }

    @Transactional
    def create(Alert alert, type, user, details, justification, attrs) {
        def activity = new Activity(
                type: type,
                performedBy: user,
                details: details,
                timestamp: DateTime.now(),
                alert: alert,
                justification: justification,
                attributes: (attrs as JSON).toString()
        )
        if (alert) {
            alert.addToActivities(activity)
        }
        activity.save()
        if (alert) {
            alert.save()
        }
    }

    @Transactional
    def createActivity(ExecutedConfiguration executedConfiguration,
                       ActivityType type,
                       User loggedInUser,
                       String details,
                       String justification,
                       def attrs,
                       String product,
                       String event,
                       User assignedToUser,
                       String caseNumber, Group assignToGroup = null, String guestAttendeeEmail = null , String privateUserName = null, Date timeStamp = null) {
        if (executedConfiguration) {
            if (Objects.isNull(timeStamp)) {
                timeStamp = DateTime.now().toDate()
            }
            Activity activity = new Activity(
                    type: type,
                    details: details,
                    timestamp: timeStamp,
                    justification: justification,
                    attributes: (attrs as JSON).toString(),
                    suspectProduct: product,
                    eventName: event,
                    caseNumber: caseNumber,
                    privateUserName : privateUserName
            )
            if (loggedInUser) {
                activity.performedBy = loggedInUser
            }
            if (assignedToUser) {
                activity.assignedTo = assignedToUser
            } else if (assignToGroup)
                activity.assignedToGroup = assignToGroup
            else {
                activity.guestAttendeeEmail = guestAttendeeEmail
            }
            executedConfiguration.addToActivities(activity)
            executedConfiguration.save(failOnError: true)
        }
    }

    def createActivityForBusinessConfiguration(ExecutedConfiguration executedConfiguration,
                                               ActivityType type,
                                               User loggedInUser,
                                               String details,
                                               String justification,
                                               def attrs,
                                               String product,
                                               String event,
                                               User assignedToUser,
                                               String caseNumber, Group assignToGroup = null) {
        if (executedConfiguration) {
            Activity activity = new Activity(
                    type: type,
                    details: details,
                    timestamp: DateTime.now(),
                    justification: justification,
                    attributes: (attrs as JSON).toString(),
                    suspectProduct: product,
                    eventName: event,
                    caseNumber: caseNumber
            )
            if (loggedInUser) {
                activity.performedBy = loggedInUser
            }
            if (assignedToUser) {
                activity.assignedTo = assignedToUser
            } else {
                activity.assignedToGroup = assignToGroup
            }
            executedConfiguration.addToActivities(activity)
        }
    }

    def createActivityAlertLevelDisposition(Map alertMap, AlertLevelDispositionDTO alertLevelDispositionDTO) {
        Activity activity = new Activity(
                type: alertLevelDispositionDTO.activityType,
                details: alertMap.details,
                timestamp: DateTime.now(),
                justification: alertLevelDispositionDTO.justificationText,
                attributes: (alertMap.attrs as JSON).toString(),
                suspectProduct: alertLevelDispositionDTO.domainName == EvdasAlert ? alertMap.substance : alertMap.productName,
                eventName: alertMap.pt,
                caseNumber: alertMap.caseNumber
        )
        if (alertLevelDispositionDTO.loggedInUser) {
            activity.performedBy = alertLevelDispositionDTO.loggedInUser
        }
        if (alertMap.assignedTo) {
            activity.assignedTo = alertMap.assignedTo
        } else {
            activity.assignedToGroup = alertMap.assignedToGroup
        }
        activity
    }

    @Transactional
    def createActivityForEvdas(def executedConfiguration,
                               type,
                               loggedInUser,
                               details,
                               justification,
                               attrs,
                               product,
                               event,
                               assignedToUser,
                               caseNumber, Group assignedToGroup = null, String guestAttendeeEmail = null, Date timeStamp = null) {
        if (executedConfiguration) {
            if (Objects.isNull(timeStamp)) {
                timeStamp = DateTime.now().toDate()
            }
            Activity activity = new Activity(
                    type: type,
                    performedBy: User.get(loggedInUser.id),
                    details: details,
                    timestamp: timeStamp,
                    justification: justification,
                    attributes: (attrs as JSON).toString(),
                    suspectProduct: product,
                    eventName: event,
                    assignedTo: assignedToUser,
                    caseNumber: caseNumber,
                    assignedToGroup: assignedToGroup
            )
            if (assignedToUser) {
                activity.assignedTo = assignedToUser
            } else if (assignedToGroup) {
                activity.assignedToGroup = assignedToGroup
            } else
                activity.guestAttendeeEmail = guestAttendeeEmail
            executedConfiguration.addToActivities(activity)
            executedConfiguration.save()
        }
    }

    def createMeetingActivity(type, currentUser, details, owner) {
        Activity activity = new Activity(
                type: type,
                performedBy: currentUser,
                details: details,
                timestamp: DateTime.now(),
                justification: null,
                assignedTo: owner,
        )
        activity
    }

    def createSignalManagementActivity(validatedId, type, user, details) {
        ValidatedSignal alert = ValidatedSignal.findById(validatedId)
        if (alert) {
            def activity = new Activity(type: type, performedBy: user,
                    details: details, timestamp: DateTime.now(), alert: alert)

            if (alert.addToActivities(activity).save()) {
                log.debug("New activity with id ${activity.id} has been created.")
                return activity
            } else {
                log.error("Activity creation attempt failed")
                return null
            }
        }
        return null
    }

    @Transactional
    def createActivityForSignal(ValidatedSignal validatedSignal, String justification, String details, ActivityType activityType, User newUser, User loggedInUser, Map attrs, Group assignedToGroup = null, Boolean isDisassociated = false) {
        if (validatedSignal) {
            Date timeStamp = null
            if (validatedSignal.lastDispChange && StringUtils.equalsIgnoreCase(activityType.getValue().value, ActivityTypeValue.DispositionChange.value)) {
                timeStamp = validatedSignal.lastDispChange
            } else {
                timeStamp = new Date()  //removed the condition for else as it causing wrong time stamp when objects of supported domains(i.e meeting,action,Rmms) is updated
            }
            Activity activity = new Activity(
                    type: activityType,
                    performedBy: loggedInUser,
                    timestamp:timeStamp,
                    justification: justification,
                    assignedTo: newUser,
                    details: details,
                    attributes: (attrs as JSON).toString(),
                    assignedToGroup: assignedToGroup
            )
            validatedSignal.addToActivities(activity)
            validatedSignal.save(flush: true, failOnError: true)
        }
    }

    @Transactional
    def createActivityForTopic(topic, justification, details, activityType, newUser, loggedInUser, attrs) {

        if (topic) {
            def activity = new Activity(
                    type: activityType,
                    performedBy: loggedInUser,
                    timestamp: DateTime.now(),
                    justification: justification,
                    assignedTo: newUser,
                    details: details,
                    attributes: (attrs as JSON).toString()
            )
            topic.addToActivities(activity)
        }
    }

    def listByAlertType(type) {
        try {
            Activity.where { alert.id in Alert.idByAlertType(type) }.order('timestamp', 'desc').list()
        } catch (SQLGrammarException sqlGrammarException) {
            []
        } catch (Throwable ex) {
            log.error("Error happened to find activities by alert type", ex)
            []
        }
    }

    def createActivityDto(def executedConfiguration,
                          type,
                          loggedInUser,
                          details,
                          justification,
                          attrs,
                          product,
                          event,
                          assignedToUser,
                          caseNumber, Group group = null, String guestAttendeeEmail = null) {
        ActivityDTO activityDTO = new ActivityDTO()
        activityDTO.executedConfiguration = executedConfiguration
        activityDTO.type = type
        activityDTO.loggedInUser = loggedInUser
        activityDTO.details = details
        activityDTO.justification = justification
        activityDTO.attributes = attrs
        activityDTO.product = product
        activityDTO.event = event
        activityDTO.assignedToUser = assignedToUser
        activityDTO.caseNumber = caseNumber
        activityDTO.assignedToGroup = group
        activityDTO.guestAttendeeEmail = guestAttendeeEmail
        activityDTO
    }

    def setEvdasActivities(ExecutedEvdasConfiguration executedConfig) {
        if (executedConfig?.id) {
            List<ActivityDTO> activityDTOList = dataObjectService.getEvdasActivityDtoList(executedConfig?.id)
            List activityList = []
            List mappingList = []
            if (activityDTOList) {
                for (ActivityDTO activityDTO : activityDTOList) {
                    if (activityDTO) {
                        Activity activity = new Activity(
                                type: activityDTO.type,
                                performedBy: User.findByUsername('System'),
                                details: activityDTO.details,
                                timestamp: DateTime.now(),
                                justification: activityDTO.justification,
                                attributes: (activityDTO.attributes as JSON).toString(),
                                suspectProduct: activityDTO.product,
                                eventName: activityDTO.event,
                                assignedTo: activityDTO.assignedToUser,
                                caseNumber: activityDTO.caseNumber,
                                assignedToGroup: activityDTO.assignedToGroup,
                                guestAttendeeEmail: activityDTO.guestAttendeeEmail
                        )
                        activityList.add(activity)
                        ExecutedEvdasConfigurationActivityMapping mapping = new ExecutedEvdasConfigurationActivityMapping(
                                executedEvdasConfiguration: executedConfig,
                                activity: activity
                        )
                        mappingList.add(mapping)
                    }
                }
                batchPersistActivity(activityList)
                batchPersistEvdasMapping(mappingList)
                dataObjectService.clearEvdasActivityMap(executedConfig?.id)
            }
        }
    }


    def setActivities(Long executedConfigId) {
        ExecutedConfiguration executedConfig = ExecutedConfiguration.get(executedConfigId)
        if (executedConfigId) {
            List<ActivityDTO> activityDTOList = dataObjectService.getActivityDtoList(executedConfigId)
            List activityList = []
            List mappingList = []
            if (activityDTOList) {
                for (ActivityDTO activityDTO : activityDTOList) {
                    if (activityDTO) {
                        Activity activity = new Activity(
                                type: activityDTO.type,
                                performedBy: User.findByUsername('System'),
                                details: activityDTO.details,
                                timestamp: DateTime.now(),
                                justification: activityDTO.justification,
                                attributes: (activityDTO.attributes as JSON).toString(),
                                suspectProduct: activityDTO.product,
                                eventName: activityDTO.event,
                                assignedTo: activityDTO.assignedToUser,
                                caseNumber: activityDTO.caseNumber,
                                assignedToGroup: activityDTO.assignedToGroup,
                                guestAttendeeEmail: activityDTO.guestAttendeeEmail
                        )
                        activityList.add(activity)
                        ExecutedConfigurationActivityMapping mapping = new ExecutedConfigurationActivityMapping(
                                executedConfiguration: executedConfig,
                                activity: activity
                        )
                        mappingList.add(mapping)
                    }
                }
                batchPersistActivity(activityList)
                batchPersistMapping(mappingList)
                dataObjectService.clearActivityMap(executedConfigId)
            }
        }
    }

    void batchPersistActivity(activityList) {
        Activity.withTransaction {
            def batch = []

            for (Activity activity : activityList) {
                batch += activity
                Session session = sessionFactory.currentSession
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (Activity act in batch) {
                        act.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (Activity act in batch) {
                        act.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("Activity data is batch persisted.")
        }
    }

    List<Long> batchPersistAlertLevelActivity(activityList) {
        List<Long> activityIdList = []
        Activity.withTransaction {
            def batch = []
            for (Activity activity : activityList) {
                batch += activity
                Session session = sessionFactory.currentSession
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (Activity act in batch) {
                        act.save(validate: false)
                        activityIdList.add(act.id)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (Activity act in batch) {
                        act.save(validate: false)
                        activityIdList.add(act.id)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
        }
        activityIdList

    }

    void batchPersistMapping(mappingList) {
        ExecutedConfigurationActivityMapping.withTransaction {
            def batch = []

            for (ExecutedConfigurationActivityMapping mapping : mappingList) {
                batch += mapping
                Session session = sessionFactory.currentSession
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (ExecutedConfigurationActivityMapping m in batch) {
                        m.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (ExecutedConfigurationActivityMapping m in batch) {
                        m.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("Mapping data is batch persisted.")
        }
    }

    void batchPersistEvdasMapping(mappingList) {
        ExecutedEvdasConfigurationActivityMapping.withTransaction {
            def batch = []

            for (ExecutedEvdasConfigurationActivityMapping mapping : mappingList) {
                batch += mapping
                Session session = sessionFactory.currentSession
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (ExecutedEvdasConfigurationActivityMapping executedEvdasConfigurationActivityMapping in batch) {
                        executedEvdasConfigurationActivityMapping.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (ExecutedEvdasConfigurationActivityMapping executedEvdasConfigurationActivityMapping in batch) {
                        executedEvdasConfigurationActivityMapping.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("Mapping of Evdas data is batch persisted.")
        }
    }

    def setActivityForAlert(ExecutedConfiguration ex) {
        User user = userService?.getUser() ?: ex.owner
        Activity activity = new Activity(
                type: ActivityType.findByValue(ActivityTypeValue.ReportGenerated),
                performedBy: user,
                details: ex.reportExecutionStatus == ReportExecutionStatus.COMPLETED ? "Report ${ex.name} generated for Alert ${ex.name}" : "Report ${ex.name} failed for Alert ${ex.name}",
                timestamp: DateTime.now(),
                assignedTo: ex.assignedTo,
                assignedToGroup: ex.assignedToGroup)
        activity.save(flush:true)
    }

    def createActivityForSingleCaseAlert(ActivityType type,
                                         User loggedInUser,
                                         String details,
                                         String justification,
                                         Map attrs,
                                         String product,
                                         String event,
                                         User assignedToUser,
                                         String caseNumber, Group group = null, String guestAttendeeEmail = null) {

        Activity activity = new Activity()
        activity.type = type
        activity.timestamp = new Date()
        activity.performedBy = loggedInUser
        activity.details = details
        activity.justification = justification
        activity.attributes = (attrs as JSON).toString()
        activity.suspectProduct = product
        activity.eventName = event
        activity.assignedTo = assignedToUser
        activity.caseNumber = caseNumber
        activity.assignedToGroup = group
        activity.guestAttendeeEmail = guestAttendeeEmail
        activity
    }

    Activity createActivityForSignalBusinessConfig(String caseNumber,String signalName,String alertName) {
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.CaseAssociated.value)
        String details = messageSource.getMessage("app.case.added.signal",[caseNumber,signalName] as Object[],Locale.default)
        Map attrs = [alert: alertName, alertType: Constants.AlertType.QUALITATIVE, signal: signalName]
        Activity activity = new Activity(
                type: activityType,
                performedBy: userService.getUser() ?: userService.getUserFromCacheByUsername(Constants.SYSTEM_USER),
                timestamp: DateTime.now(),
                details: details,
                attributes: (attrs as JSON).toString(),
        )
        activity
    }

    Activity createActivityForSignalBusinessConfigAgg(AggregateCaseAlert aca,String signalName,String alertName) {
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.PECAssociated.value)
        String details = "PEC '${aca.productName}'-'${aca.pt}' has been added to '${signalName}'"
        Map attrs = [alert: alertName, alertType: Constants.AlertType.QUANTITATIVE, signal: signalName]
        Activity activity = new Activity(
                type: activityType,
                performedBy: userService.getUser() ?: userService.getUserFromCacheByUsername(Constants.SYSTEM_USER),
                timestamp: DateTime.now(),
                details: details,
                attributes: (attrs as JSON).toString(),
        )
        activity
    }

    Activity createActivityForSignalBusinessConfigEvdas(String substance, String pt, String signalName, String alertName, Date dispLastChange = null) {
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.PECAssociated.value)
        String details = messageSource.getMessage("app.case.added.signal.evdas", [substance, pt, signalName] as Object[], Locale.default)
        Map attrs = [alert: alertName, alertType: Constants.AlertConfigType.EVDAS_ALERT, signal: signalName]
        if (Objects.isNull(dispLastChange)) {
            dispLastChange = DateTime.now().toDate()
        }
        Activity activity = new Activity(
                type: activityType,
                performedBy: userService.getUser() ?: userService.getUserFromCacheByUsername(Constants.SYSTEM_USER),
                timestamp: dispLastChange,
                details: details,
                attributes: (attrs as JSON).toString(),
        )
        activity
    }

    Activity createActivityBulkUpdate(ActivityType type,
                       User loggedInUser,
                       String details,
                       String justification,
                       def attrs,
                       String product,
                       String event,
                       User assignedToUser,
                       String caseNumber, Group assignToGroup = null, Date timeStamp = null) {

            if (Objects.isNull(timeStamp)) {
                timeStamp = DateTime.now().toDate()
            }
            Activity activity = new Activity(
                    type: type,
                    details: details,
                    timestamp: timeStamp,
                    justification: justification,
                    attributes: (attrs as JSON).toString(),
                    suspectProduct: product,
                    eventName: event,
                    caseNumber: caseNumber
            )
            if (loggedInUser) {
                activity.performedBy = loggedInUser
            }
            if (assignedToUser) {
                activity.assignedTo = assignedToUser
            } else {
                activity.assignedToGroup = assignToGroup
            }
        activity
    }

    List<Map> batchPersistBulkUpdateActivity(List<Map> activityList) {
        List<Map> activityExecConfIdsMap = []
        Integer batchSize = Holders.config.signal.batch.size as Integer
        Activity.withTransaction {
            List batch = []
            activityList.each {
                batch += it.activity
                it.activity.save(validate: false)
                activityExecConfIdsMap.add([execConfigId:it.execConfigId,activityId:it.activity.id])
                if (batch.size().mod(batchSize) == 0) {
                    Session session = sessionFactory.currentSession
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }
        activityExecConfIdsMap
    }

    List<Map> batchPersistSignalMemoActivity(List<Map> activityList) {
        List<Map> activityExecConfIdsMap = []
        Integer batchSize = Holders.config.signal.batch.size as Integer
        Activity.withTransaction {
            List batch = []
            activityList.eachWithIndex { Map activityMap, Integer index ->
                batch += activityMap.activity
                activityMap.activity.save(validate: false)
                activityExecConfIdsMap.add([signalId:activityMap.signalId, activityId:activityMap.activity.id])
                if (index && index.mod(batchSize) == 0) {
                    Session session = sessionFactory.currentSession
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }
        activityExecConfIdsMap
    }

    void activityUpdate(def domain, String tableName) {
        Sql sql
        try {
            sql = new Sql(dataSource)
            List activityList = domain.withCriteria {
                like('details', '%Tag%')
            }

            String updatectivityQuery = ''
            activityList.each {
                updatectivityQuery += "UPDATE ${tableName} SET DETAILS = \'" + it.details?.replaceAll("Tag", "Category") + "\' WHERE ID = " + it.id + ";"
            }
            if (updatectivityQuery) {
                sql.executeUpdate(updatectivityQuery)
            }
        } catch (Exception ex) {
            println(ex.stackTrace)
        } finally {
            sql?.close()
        }
    }

    String breakActivityType(String activityType){
            if(activityType=='PECDissociated'){
                activityType = 'PEC Dissociated'
            } else if(activityType=='PECAssociated') {
                activityType= 'PEC Associated'
            } else if(activityType == 'RMMAdded'){
                activityType ='RMM Added'
            } else if(activityType == 'RMMUpdated'){
                activityType= 'RMM Updated'
            } else if(activityType== 'RMMDeleted'){
                activityType= 'RMM Deleted'
            }else{
                activityType = activityType ? activityType.split(/(?=[A-Z])/).join(' ') : activityType
            }
        activityType
    }

    @Transactional
    def createEvdasActivity(ExecutedEvdasConfiguration executedConfiguration,
                       ActivityType type,
                       User loggedInUser,
                       String details,
                       String justification,
                       def attrs,
                       String product,
                       String event,
                       User assignedToUser,
                       String caseNumber, Group assignToGroup = null, String guestAttendeeEmail = null , String privateUserName = null) {
        if (executedConfiguration) {
            Activity activity = new Activity(
                    type: type,
                    details: details,
                    timestamp: DateTime.now(),
                    justification: justification,
                    attributes: (attrs as JSON).toString(),
                    suspectProduct: product,
                    eventName: event,
                    caseNumber: caseNumber,
                    privateUserName : privateUserName
            )
            if (loggedInUser) {
                activity.performedBy = loggedInUser
            }
            if (assignedToUser) {
                activity.assignedTo = assignedToUser
            } else if (assignToGroup)
                activity.assignedToGroup = assignToGroup
            else {
                activity.guestAttendeeEmail = guestAttendeeEmail
            }
            executedConfiguration.addToActivities(activity)
            executedConfiguration.save(failOnError: true)
        }
    }

    @Transactional
    def createLiteratureActivity(ExecutedLiteratureConfiguration executedConfiguration,
                            ActivityType type,
                            User loggedInUser,
                            String details,
                            String justification,
                            def attrs,
                            String product,
                            String event,
                            User assignedToUser,
                            Integer articleId, Group assignToGroup = null, String searchString) {
        if (executedConfiguration) {
            LiteratureActivity activity = new LiteratureActivity(
                    type: type,
                    details: details,
                    timestamp: DateTime.now(),
                    justification: justification,
                    attributes: (attrs as JSON).toString(),
                    suspectProduct: product,
                    eventName: event,
                    articleId: articleId,
                    searchString: searchString
            )
            if (loggedInUser) {
                activity.performedBy = loggedInUser
            }
            if (assignedToUser) {
                activity.assignedTo = assignedToUser
            } else if (assignToGroup)
                activity.assignedToGroup = assignToGroup
            else {
                activity.guestAttendeeEmail = guestAttendeeEmail
            }
            executedConfiguration.addToLiteratureActivity(activity)
            executedConfiguration.save(failOnError: true)
        }
    }

    /**
     * This method is returning the activities list for each executed config id through
     * SQL query to enhance the performance
     * @param exConfigs - This consists all the executed configurations required to fetch activities
     * @return - Returning a list of map of activities after converting each entity as per activity DTO
     */
    List listActivitiesByExConfig(Set exConfigs, String appType){
        List<Map> result = []
        if (exConfigs.size() > 0) {
            Session session = sessionFactory.currentSession
            String query = ''
            User currentUser = userService.getUser()
            String userTimeZone = currentUser.preference.timeZone
            if (appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                Map blindingFieldsForGroup = userService.getBlindingFieldsForGroup(currentUser.groups*.id)
                Map blindingFieldsForUser = userService.getBlindingFieldsForUser(blindingFieldsForGroup, true)
                Map blindedDataForSQL = userService.getBlindedDataForSQL(blindingFieldsForUser)
                boolean isAllFieldsRedacted = blindingFieldsForGroup.isAllFieldsRedacted == 1
                boolean isUserMappingAvailable = blindingFieldsForGroup.isUserMappingAvailable
                isAllFieldsRedacted = !(!isUserMappingAvailable && !isAllFieldsRedacted)
                query = SignalQueryHelper.list_ICR_activities_by_executed_config(exConfigs.join(","), currentUser.username, blindedDataForSQL, isAllFieldsRedacted ? 1 : 0)
            } else if (appType == Constants.AlertConfigType.EVDAS_ALERT){
                query = SignalQueryHelper.list_activities_by_executed_config(exConfigs.join(","), currentUser.username, 'EX_EVDAS_CONFIG_ACTIVITIES', 'EX_EVDAS_CONFIG_ID')
            } else {
                query = SignalQueryHelper.list_activities_by_executed_config(exConfigs.join(","), currentUser.username)
            }
            try {
                SQLQuery sqlQuery = session.createSQLQuery(query)

                sqlQuery.addScalar("alertName", new StringType())
                sqlQuery.addScalar("alertId", new LongType())
                sqlQuery.addScalar("activity_id", new LongType())
                sqlQuery.addScalar("details", new StringType()) // TODO: Need to do the handling of replaceNullCharacter for details as similar to toDTO
                sqlQuery.addScalar("timestamp", new TimestampType())
                sqlQuery.addScalar("justification", new StringType())
                sqlQuery.addScalar("caseNumber", new StringType())
                sqlQuery.addScalar("suspect", new StringType())
                sqlQuery.addScalar("eventName", new StringType())
                sqlQuery.addScalar("currentAssignment", new StringType())
                sqlQuery.addScalar("performedBy", new StringType())
                sqlQuery.addScalar("currentAssignmentDept", new StringType())
                sqlQuery.addScalar("performedByDept", new StringType())
                sqlQuery.addScalar("type", new StringType())
                sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                result = sqlQuery.list()
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a");
                for (Map activity : result) {
                    Timestamp timestamp = (Timestamp) activity.get("timestamp");
                    if (timestamp != null) {
                        ZonedDateTime userZoneTime = convertToUserTimeZone(timestamp, userTimeZone)
                        String formattedTimestamp = userZoneTime.format(formatter)
                        activity.put("timestamp", formattedTimestamp)
                    }
                }
            } catch (Exception exception) {
                log.error("Error came while listActivitiesByExConfig executing: " + exception.getMessage())
            } finally {
                session.flush()
                session.clear()
            }
        }
        return result.flatten()
    }

    ZonedDateTime convertToUserTimeZone(Timestamp timestamp, String userTimeZone) {
        ZonedDateTime systemZoneTime = timestamp.toInstant().atZone(ZoneId.systemDefault());
        ZoneId userZoneId = ZoneId.of(userTimeZone);
        return systemZoneTime.withZoneSameInstant(userZoneId);
    }


}
