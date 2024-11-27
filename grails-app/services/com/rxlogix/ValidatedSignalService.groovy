package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.AlertSignalsDataDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.SignalChartsDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedCaseSeriesDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.enums.*
import com.rxlogix.helper.LinkHelper
import com.rxlogix.mapping.MedDraSOC
import com.rxlogix.pvdictionary.DictionaryGroupCmd
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.async.Promise
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.time.DateUtils
import org.apache.groovy.json.internal.LazyMap
import groovy.sql.GroovyResultSet
import groovy.sql.GroovyResultSetExtension
import groovy.sql.OutParameter
import groovy.sql.Sql
import groovyx.net.http.Method
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import oracle.jdbc.OracleTypes
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.apache.http.util.TextUtils
import com.rxlogix.exception.FileFormatException
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.hibernate.transform.Transformers
import org.hibernate.type.LongType
import org.hibernate.type.StringType
import org.hibernate.type.IntegerType
import org.hibernate.type.BooleanType
import org.hibernate.type.DoubleType
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.sql.Clob
import java.text.SimpleDateFormat
import java.time.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT
import static com.rxlogix.util.DateUtil.*
import static com.rxlogix.util.MiscUtil.*
import static grails.async.Promises.task
import org.apache.commons.lang3.StringUtils
import org.apache.groovy.dateutil.extensions.DateUtilExtensions
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Transactional
class ValidatedSignalService implements AlertUtil, LinkHelper {

    static final String NO_OF_PECS = "noOfPec"
    static final String NO_OF_CASES = "noOfCases"
    static final String PRODUCTS = "products"
    static final String SIGNAL_STATUS = "signalStatus"
    static final List<String> SORTED_COLUMNS = Arrays.asList(NO_OF_PECS,NO_OF_CASES, PRODUCTS, SIGNAL_STATUS)
    static final Integer SPOTFIRE_ANALYSIS_FILE_NAME_LENGTH = 234

    def userService
    def CRUDService
    def activityService
    def signalAuditLogService
    def reportIntegrationService
    def dataSource
    def pvsProductDictionaryService
    def signalHistoryService
    def dataSource_pva
    def emailService
    def signalDataSourceService
    def dynamicReportService
    GrailsApplication grailsApplication
    def cacheService
    def specialPEService
    def sessionFactory
    def messageSource
    def alertAttributesService
    def literatureActivityService
    def sqlGenerationService
    def validatedSignalChartService
    def dataObjectService
    def restAPIService
    EmailNotificationService emailNotificationService
    def spotfireService
    def alertService
    def signalWorkflowService
    def attachmentableService
    def undoableDispositionService
    def springSecurityService
    def customMessageService

    def saveSignal(ValidatedSignal validatedSignal, Boolean newSignal, Boolean isLastDispChange = false) {
        CRUDService.save(validatedSignal)
        //Save the history for signal only when the signal is new.
        if (newSignal) {
            if (isLastDispChange) {
                saveHistoryForSignal(validatedSignal, validatedSignal.lastDispChange)
            } else {
                saveHistoryForSignal(validatedSignal)
            }
        }
    }

    //TODO : Method no longer in used in application, used only in obsolete method
    ValidatedSignal attachToSignal(signalJson, products,
                                   def alert, String alertType, User assignedTo, Group assignedToGroup = null, Disposition defaultSignalDisposition) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        ValidatedSignal validatedSignal
        try {
            def signalObj = jsonSlurper.parseText(signalJson)

            def newSignal = false

            signalObj.each {
                if (it.signalName) {
                    validatedSignal = ValidatedSignal.findByName(it.signalName)
                    if (!validatedSignal) {
                        validatedSignal = new ValidatedSignal()
                        validatedSignal.name = it.signalName
                        validatedSignal.products = products
                        validatedSignal.events = it.signalEvent
                        validatedSignal.assignmentType = "USER"
                        validatedSignal.assignedTo = assignedTo
                        validatedSignal.assignedToGroup = assignedToGroup
                        validatedSignal.assignmentType = Constants.AssignmentType.USER
                        validatedSignal.disposition = defaultSignalDisposition
                        validatedSignal.priority = cacheService.prepareDefaultPriority()
                        validatedSignal.createdBy = alert.createdBy
                        validatedSignal.modifiedBy = alert.modifiedBy
                        validatedSignal.dateCreated = alert.dateCreated
                        validatedSignal.lastUpdated = alert.lastUpdated
                        calcDueDate(validatedSignal, validatedSignal.priority, validatedSignal.disposition, false,
                            cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id))
                        addAlertToSignal(validatedSignal, alert)
                        newSignal = true
                    } else {
                        addAlertToSignal(validatedSignal, alert)
                    }
                    //Now save the signal.
                    saveSignal(validatedSignal, newSignal)
                    String details = ""
                    ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.CaseAdded)
                    if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
                        details = "Case '${alert.caseNumber}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.CaseAssociated)
                    } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
                        details = "PEC '${alert.productName}'-'${alert.pt}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.PECAssociated)
                    } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
                        details = "AdHoc '${alert.name}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.AdhocAlertAssociated)
                    } else if (Constants.AlertConfigType.EVDAS_ALERT == alertType) {
                        details = "PEC '${alert.substance}'-'${alert.pt}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.CaseAssociated)
                    }

                    def attr = [alert: alert.name, alertType: alertType, signal: validatedSignal.name]

                    //TODO Disable this
                    User user = userService.getUser() ? userService.getUser() : User.findByUsername(alert.modifiedBy)
                    activityService.createActivityForSignal(validatedSignal, '', details, activityType,
                            validatedSignal.assignedTo, user, attr, validatedSignal.assignedToGroup)
                }
            }
        } catch (Throwable ex) {
            log.error("Exception attaching to signal", ex)
        }
        validatedSignal
    }

    ValidatedSignal attachAlertToSignal(String signalName, String productJson,
                             def alert, String alertType, Disposition defaultSignalDisposition, String eventJSON = null,Long signalId,boolean isDateValidated) {
        ValidatedSignal validatedSignal
        try {
            Boolean newSignal = false
            User currentUser = userService.getUser()
            if (signalName) {
                validatedSignal = ValidatedSignal.findByName(signalName)
                List<SignalStatusHistory> listSignalHistory=[];
                Long  signalHistoryId=null;
                if(null!=validatedSignal) {
                    listSignalHistory=validatedSignal.getSignalStatusHistories();
                    if(listSignalHistory.size()>0){
                        signalHistoryId=listSignalHistory.get(0).id;
                    }
                }
                if (!validatedSignal) {
                    validatedSignal = new ValidatedSignal()
                    validatedSignal.name = signalName
                    validatedSignal.products = productJson ?: (alertType != Constants.AlertConfigType.LITERATURE_SEARCH_ALERT ? alert?.executedAlertConfiguration?.productSelection : null)
                    validatedSignal.events = eventJSON
                    validatedSignal.assignedTo = alert.assignedTo
                    validatedSignal.assignedToGroup = alert.assignedToGroup
                    validatedSignal.assignmentType = Constants.AssignmentType.USER
                    validatedSignal.disposition = defaultSignalDisposition
                    validatedSignal.signalStatus= Constants.ONGOING_SIGNAL
                    validatedSignal.lastDispChange= new Date()
                    validatedSignal.workflowState = signalWorkflowService.defaultSignalWorkflowState()
                    boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
                    if(enableSignalWorkflow && signalWorkflowService.defaultSignalWorkflowState() == signalWorkflowService.calculateDueInSignalWorkflowState()){
                        validatedSignal.wsUpdated = new Date()
                    }
                    validatedSignal.priority = Priority.findByDefaultPriority(true) ?: Priority.findByValue('High')
                    validatedSignal.createdBy = currentUser ? currentUser.username : Constants.Commons.SYSTEM
                    validatedSignal.modifiedBy = currentUser ? currentUser.username : Constants.Commons.SYSTEM
                    validatedSignal.workflowGroup = currentUser ? currentUser.workflowGroup : userService.getUserFromCacheByUsername(Constants.SYSTEM_USER)?.workflowGroup
                    validatedSignal.dateCreated = new Date()
                    validatedSignal.lastUpdated = new Date()
                    validatedSignal.detectedDate = new Date()
                    calcDueDate(validatedSignal, validatedSignal.priority, validatedSignal.disposition, false,
                        cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id))
                    if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
                        validatedSignal.events = alert.eventSelection
                        validatedSignal.priority = alert?.priority ?: cacheService.prepareDefaultPriority()
                        validatedSignal.detectedBy = alert?.detectedBy
                        validatedSignal.detectedDate = alert?.detectedDate
                        validatedSignal.aggReportStartDate = alert?.aggReportStartDate
                        validatedSignal.aggReportEndDate = alert?.aggReportEndDate
                        validatedSignal.haSignalStatus = alert?.haSignalStatus
                        validatedSignal.haDateClosed = alert?.haDateClosed
                        validatedSignal.topic = alert.topic
                        validatedSignal.initialDataSource = alert.getAttr('initialDataSource')
                        validatedSignal.description = alert.description
                        validatedSignal.genericComment = alert.notes
                        validatedSignal.isMultiIngredient = parseMultiIngredientProductJson(productJson)
                        validatedSignal.productGroupSelection = alert.productDictionarySelection == null ? alert.productGroupSelection : null
                        validatedSignal.eventGroupSelection = alert.eventSelection==null ? alert.eventGroupSelection : null
                        alert.actionTaken.each {
                            validatedSignal.addToActionTaken(it)
                        }
                        validatedSignal.commentSignalStatus = alert?.commentSignalStatus
                        def evalMethods = alert?.getAttr("evaluationMethods")
                        if(evalMethods){
                            validatedSignal.evaluationMethod = convertStringToList(evalMethods)
                        }
                    }
                    else if(alertType.equalsIgnoreCase(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)){
                        validatedSignal.productGroupSelection = alert.litSearchConfig.productGroupSelection
                        validatedSignal.eventGroupSelection = alert.litSearchConfig.eventGroupSelection
                    }
                    validatedSignal.productsAndGroupCombination = alertService.productSelectionSignal(validatedSignal);
                    validatedSignal.eventsAndGroupCombination = alertService.eventSelectionSignalWithSmq(validatedSignal);
                    validatedSignal.productDictionarySelection = fetchProductDictionarySelection(alert)
                    Group allUsers = Group.findByName('All Users')
                    if (Holders.config.validatedSignal.shareWith.enabled && allUsers) {
                        validatedSignal.addToShareWithGroup(allUsers)
                    }
                    addAlertToSignal(validatedSignal, alert)
                    saveSignal(validatedSignal, true)
                    if(isDateValidated){
                        String createdDate =Holders.config.detectedDateAndValidationDate.synch.enabled?DateUtil.fromDateToString(validatedSignal.detectedDate,DateUtil.DEFAULT_DATE_FORMAT):DateUtil.fromDateToStringWithTimezone(new Date(), DateUtil.DEFAULT_DATE_FORMAT,Holders.config.server.timezone)
                        String defaultValidatedDate=Holders.config.signal.defaultValidatedDate
                        saveSignalStatusHistory([signalStatus: (defaultValidatedDate)?defaultValidatedDate:Constants.WorkFlowLog.VALIDATION_DATE, statusComment: Constants.WorkFlowLog.VALIDATION_STATUS_COMMENT,
                                                 signalId: validatedSignal.id, assignedTo: alert.assignedTo, signalHistoryId: signalHistoryId, "isSystemUser": true, "createdDate": createdDate], true)

                    }else{
                        saveSignalStatusHistory([signalStatus: Constants.SignalHistory.SIGNAL_CREATED, statusComment: Constants.SignalHistory.SIGNAL_CREATED,
                                                 signalId    : validatedSignal.id,assignedTo: alert.assignedTo], false)
                    }
                    newSignal = true
                }
                if (validatedSignal) {
                    addAlertToSignal(validatedSignal, alert)
                    saveSignal(validatedSignal, newSignal)
                    String details = ""
                    ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.CaseAdded)
                    if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
                        details = "Case '${alert.caseNumber}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.CaseAssociated)
                    } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
                        details = "PEC '${alert.productName}'-'${alert.pt}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.PECAssociated)
                    } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
                        details = "AdHoc '${alert.name}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.AdhocAlertAssociated)
                    } else if (Constants.AlertConfigType.EVDAS_ALERT == alertType) {
                        details = "PEC '${alert.substance}'-'${alert.pt}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.PECAssociated)
                    } else if (Constants.AlertConfigType.LITERATURE_SEARCH_ALERT == alertType) {
                        details = "Article Id '${alert.articleId}' has been added to '${validatedSignal.name}'"
                        activityType = ActivityType.findByValue(ActivityTypeValue.LiteratureAlertAssociated)
                    }

                    def attr = [alert: alert.name, alertType: alertType, signal: validatedSignal.name]

                    //TODO: This needs to be refactored.
                    User user = currentUser ?: User.findByUsername(alert.modifiedBy as String)
                    if (Constants.AlertConfigType.LITERATURE_SEARCH_ALERT == alertType) {
                        literatureActivityService.createLiteratureActivityForSignal(alert.exLitSearchConfig,activityType, user, details, '', alert.assignedTo, alert.searchString, alert.articleId, alert.assignedToGroup,getNameFieldFromJson(productJson),getNameFieldFromJson(eventJSON))
                    }
                    activityService.createActivityForSignal(validatedSignal, '', details, activityType,
                            validatedSignal.assignedTo, user, attr)
                    if(isDateValidated && newSignal==false){
                        String defaultValidatedDate=Holders.config.signal.defaultValidatedDate
                        String createdDate =Holders.config.detectedDateAndValidationDate.synch.enabled?DateUtil.fromDateToString(validatedSignal.detectedDate,DateUtil.DEFAULT_DATE_FORMAT):DateUtil.fromDateToStringWithTimezone(new Date(), DateUtil.DEFAULT_DATE_FORMAT,Holders.config.server.timezone)
                        saveSignalStatusHistory([signalStatus:(defaultValidatedDate)?defaultValidatedDate: Constants.WorkFlowLog.VALIDATION_DATE, statusComment: Constants.WorkFlowLog.VALIDATION_STATUS_COMMENT,
                                                 signalId: validatedSignal.id, assignedTo: alert.assignedTo, signalHistoryId: signalHistoryId, "isSystemUser": true, "createdDate": createdDate], true)
                    }
                }
            }
        } catch (Throwable ex) {
            log.error("Exception attaching to signal", ex)
        }
        return validatedSignal;
    }

    def saveHistoryForSignal(ValidatedSignal validatedSignal, Date createdTimeStamp = null) {
        //Create the Signal history.
        Map signalHistoryMap = [
                "assignedTo"     : validatedSignal.assignedTo,
                "assignedToGroup": validatedSignal.assignedToGroup,
                "priority"       : validatedSignal.priority,
                "disposition"    : validatedSignal.disposition,
                "validatedSignal": validatedSignal,
                "dueDate"        : validatedSignal.dueDate,
                "createdTimestamp": createdTimeStamp
        ]
        signalHistoryService.saveSignalHistory(signalHistoryMap)
    }

    void addAlertToSignal(ValidatedSignal validatedSignal, def alert){
        switch (alert){
            case SingleCaseAlert :
                validatedSignal.addToSingleCaseAlerts(alert)
                break
            case AggregateCaseAlert :
                validatedSignal.addToAggregateAlerts(alert)
                break
            case EvdasAlert :
                validatedSignal.addToEvdasAlerts(alert)
                break
            case LiteratureAlert :
                validatedSignal.addToLiteratureAlerts(alert)
                break
            case AdHocAlert :
                validatedSignal.addToAdhocAlerts(alert)
                break
            case ArchivedSingleCaseAlert :
                validatedSignal.addToArchivedSingleCaseAlerts(alert)
                break
            case ArchivedAggregateCaseAlert :
                validatedSignal.addToArchivedAggregateAlerts(alert)
                break
            case ArchivedEvdasAlert :
                validatedSignal.addToArchivedEvdasAlerts(alert)
                break
            case ArchivedLiteratureAlert :
                validatedSignal.addToArchivedLiteratureAlerts(alert)
                break
            case EmbaseLiteratureAlert :
                validatedSignal.addToEmbaseLiteratureAlerts(alert)
                break
            case ArchivedEmbaseLiteratureAlert :
                validatedSignal.addToArchivedEmbaseLiteratureAlerts(alert)
                break
            default:
                log.info("Invalid Alert")
                break
        }
    }

    def getMeetingDetails(Long signalId) {
        String timeZone = userService.getUser().preference.timeZone
        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
        List<Meeting> meetingList = Meeting.findAllByValidatedSignal(validatedSignal)
        List data = meetingList.collect {
            [
                    signalName   : validatedSignal.name,
                    title        : it.meetingStatus == MeetingStatus.CANCELLED ? it.meetingTitle + "(${it.meetingStatus.id})" : it.meetingTitle,
                    meetingDate  : DateUtil.toDateStringWithTimeInAmPmFormat(it.meetingDate, timeZone),
                    agenda       : it.meetingAgenda,
                    minutes      : it.meetingMinutes,
                    lastUpdatedBy: it.modifiedBy,
                    lastUpdated  : DateUtil.toDateStringWithTimeInAmPmFormat(it.lastUpdated, timeZone)
            ]
        }
        data
    }

    List<Map> getSignalActionDetails(signalId) {
        List<Map> data = []
        def sql = new Sql(dataSource)
        try {
            def sql_statement = SignalQueryHelper.signal_action_sql(signalId)
            sql.eachRow(sql_statement, []) { row ->
                data << readActionData(row)
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        data
    }

    def readActionData(resultSet) {
        [
                signalName    : resultSet.getString('Signal Name'),
                actionName    : resultSet.getString('Action Name'),
                actionType    : resultSet.getString('Action Type'),
                assignedTo    : User.findByUsername(resultSet.getString('Assigned To'))?.fullName?: (Group.findByName(resultSet.getString('Assigned To Group'))?.name?: resultSet.getString('Guest Email')),
                status        : ActionStatus[resultSet.getString('Status')].id,
                creationDate  : resultSet.getString('Creation Date'),
                dueDate       : resultSet.getString('Due Date'),
                completionDate: resultSet.getString('Completion Date'),
                details       : resultSet.getString('Details'),
                comments      : resultSet.getString('Comments')
        ]
    }

//    Todo remove this.
    def changeAssignedToUser(signalId, newUser) {
        def validatedSignal = ValidatedSignal.get(signalId)
        def oldUser = validatedSignal.assignedTo
        validatedSignal.assignedTo = newUser
        def details = "Assigned To changed from '${oldUser?.fullName}' to '${newUser?.fullName}' for signal '${validatedSignal.name}'"
        def attr = [oldUser: oldUser?.fullName, newUser: newUser?.fullName, signal: validatedSignal.name]
        activityService.createActivityForSignal(validatedSignal, '', details, ActivityType.findByValue(ActivityTypeValue.AssignedToChange),
                newUser, userService.getUser(), attr)
        validatedSignal.assignmentType = Constants.AssignmentType.USER

        //Create the Signal history.
        def signalHistoryMap = [
                "assignedTo"     : newUser,
                "change"         : Constants.HistoryType.ASSIGNED_TO,
                "validatedSignal": validatedSignal
        ]

        signalHistoryService.saveSignalHistory(signalHistoryMap)

        CRUDService.update(validatedSignal)
        validatedSignal
    }

    def changeGroup(validatedSignal, selectedGroups) {

        def oldGroups = validatedSignal.sharedGroups
        def oldGroupNames = ''
        if (oldGroups) {
            oldGroups.each {
                oldGroupNames = oldGroupNames + it.name + ","
            }
        }

        Sql sqlDeletion = new Sql(dataSource)

        def deletionQuery = "delete from VALIDATED_SIGNAL_GROUP where VALIDATED_SIGNAL_ID=" + validatedSignal.id

        try {
            sqlDeletion.execute(deletionQuery);
        } catch (Exception ex) {
            sqlDeletion.rollback()
        } finally {
            sqlDeletion.commit()
            sqlDeletion.close()
        }
        def newGroupNames = ""
        Sql sqlInsertion = new Sql(dataSource)
        try {
            selectedGroups.split(',').each {
                def groupId = Long.parseLong(it)
                def groupObj = Group.findById(groupId, [null: null])
                if (groupObj) {
                    newGroupNames = newGroupNames + groupObj.name + ","

                }
                def sqlstr = """INSERT INTO VALIDATED_SIGNAL_GROUP VALUES ($validatedSignal.id, $groupId)"""
                sqlInsertion.execute(sqlstr);
                validatedSignal.assignmentType = Constants.AssignmentType.GROUP
                validatedSignal.save(flush: true)

                //Create the Signal history.
                def signalHistoryMap = [
                        "group"          : groupObj,
                        "change"         : Constants.HistoryType.GROUP,
                        "validatedSignal": validatedSignal
                ]
                signalHistoryService.saveSignalHistory(signalHistoryMap)


                def details = "Assigned To(Group) changed from '${oldGroupNames}' to '${newGroupNames}' for signal '${validatedSignal.name}'"
                def attr = [oldGroups: oldGroupNames, newGroups: newGroupNames, signal: validatedSignal.name]
                activityService.createActivityForSignal(validatedSignal, '', details, ActivityType.findByValue(ActivityTypeValue.AssignedToChange),
                        null, userService.getUser(), attr)

            }
        } catch (Exception ex) {
            sqlInsertion.rollback()
        } finally {
            sqlInsertion.commit()
            sqlInsertion.close()
        }
        return newGroupNames
    }

    def changePriority(ValidatedSignal signal, Priority newPriority, String justification) {
        Priority oldPriority = signal.priority
        signal.priority = newPriority
        signal.customAuditProperties = ["justification": justification]
        String previousDueDate=DateUtil.fromDateToString(signal.actualDueDate,DEFAULT_DATE_FORMAT)
        calcDueDate(signal, signal.priority, signal.disposition, false,
                cacheService.getDispositionConfigsByPriority(signal.priority.id))
        String details = "Priority changed from '${oldPriority?.displayName}' to '${newPriority?.displayName}' for signal '${signal.name}'"
        Map attr = [oldPriority: oldPriority?.displayName, newPriority: newPriority?.displayName, signal: signal.name]
        activityService.createActivityForSignal(signal, justification, details, ActivityType.findByValue(ActivityTypeValue.PriorityChange),
                signal.assignedTo, userService.getUser(), attr, signal.assignedToGroup)



        //Create the Signal history.
        def signalHistoryMap = [
                "priority"       : newPriority,
                "change"         : Constants.HistoryType.PRIORITY,
                "justification"  : justification,
                "validatedSignal": signal,
                "dueDate"        : signal.dueDate
        ]
        signalHistoryService.saveSignalHistory(signalHistoryMap)
        signal.isDueDateUpdated = false
        CRUDService.update(signal)
        List<Map> signalHistoryList = generateSignalHistory(signal)
        Integer dueIn
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDueInDisplay(true)
        String defaultValidatedDate=Holders.config.signal.defaultValidatedDate;
        SystemConfig systemConfig = SystemConfig.first()
        List<String> dueInEndPoints = systemConfig.selectedEndPoints?.split(",").collect {it} as List<String>

        boolean dueInStartEnabled= Holders.config.dueInStart.enabled
        String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
        boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow;
        if (signalHistoryList.signalStatus.contains(Constants.DATE_CLOSED)) {
            dueIn = calculateDueIn(signal.id, Constants.DATE_CLOSED)
        } else {
            if (dueInStartEnabled && signalHistoryList.signalStatus.contains(dueInStartPoint)) {
                defaultValidatedDate=(defaultValidatedDate!=null)?defaultValidatedDate:Constants.VALIDATION_DATE
                dueIn = calculateDueIn(signal.id, dueInStartPoint)
            }else if (signalHistoryList.signalStatus.contains('Validation Date')||signalHistoryList.signalStatus.contains(defaultValidatedDate)) {
                defaultValidatedDate=(defaultValidatedDate!=null)?defaultValidatedDate:Constants.VALIDATION_DATE
                dueIn = calculateDueIn(signal.id, defaultValidatedDate)
            } else {
                dueIn = calculateDueIn(signal.id, signal.workflowState)
            }
        }
        if(dueIn != null && SystemConfig.first().displayDueIn) {
            saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                     signalId    : signal.id, "createdDate":previousDueDate,"currentDispositionId":signal?.disposition?.id], true)
        }
        return dueIn
    }

    def getSignalsFromAlert(alert, alertType) {
        String timeZone = userService.user.preference.timeZone
        def signalMap = [:]
        def selectedSignal = []
        def allSignals = []
        if (alert) {
            def validatedSignals = ValidatedSignal.list()

            validatedSignals?.each { ValidatedSignal signal ->

                if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
                    if (signal.singleCaseAlerts?.contains(alert)) {
                        selectedSignal.add(signal)
                    }
                } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
                    if (signal.aggregateAlerts?.contains(alert)) {
                        selectedSignal.add(signal)
                    }
                } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
                    if (signal.adhocAlerts?.contains(alert)) {
                        selectedSignal.add(signal)
                    }
                }
                allSignals.add([
                        signalName: signal.name,
                        priority  : signal.priority.displayName,
                        strategy  : [
                                name    : signal.strategy?.name,
                                products: signal.strategy?.productNameList
                        ]
                ])
            }

            selectedSignal = selectedSignal?.collect {
                [
                        signalName        : it.name,
                        products          : it.products,
                        events            : it.events,
                        priorityAndUrgency: it.priority.displayName,
                        creationDate      : toDateString(it.dateCreated, timeZone),
                        endDate           : toDateTimeString(it.endDate),
                        strategy          : [
                                name    : it.strategy?.name,
                                products: it.strategy?.productNameList
                        ]
                ]
            }
        }
        signalMap.selectedSignal = selectedSignal
        signalMap.allSignals = allSignals
        signalMap
    }

    List<ValidatedSignal> getSignalsFromAlertObj(def alert, String alertType) {
        def inCriteria = 'singleCaseAlerts'
        if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
            inCriteria = 'aggregateAlerts'
        } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
            inCriteria = 'adhocAlerts'
        } else if (Constants.AlertConfigType.EVDAS_ALERT == alertType) {
            inCriteria = 'evdasAlerts'
        }
        List<ValidatedSignal> signalList = ValidatedSignal.createCriteria().list {
            "$inCriteria" {
                eq('id', alert.id)
            }
        }
        signalList
    }

    //todo: This logic will update post beta 1 release
    Map fetchSignalsNotInAlertObj(String searchTerm = null, Integer page = 1, Integer pageSize = 50) {
        log.info("fetching signal for searchTerm :"+searchTerm)
        User user = cacheService.getUserByUserNameIlike(userService.getCurrentUserName())
        List<Long> groupIds = user.groups?.collect { it.id }
        String query = prepareValidatedSignalHQL(null,null,groupIds,user,'name','asc')
        int offset = 0
        int totalCount = 0
        offset = (page - 1) * pageSize
        if(searchTerm) {
            query = query.replace(Constants.DEFAULT_SQL_CONDITION, "lower(vs.name) like '%" + searchTerm + "%'")
        }
        String totalCountQuery = query.replace('new Map(vs.name as name, vs.id as id , vs.products as products, vs.signalStatus as signalStatus,vs.detectedDate as detectedDate ,vs.disposition as disposition,vs.productGroupSelection as productGroupSelection)','COUNT(*)')
        int orderByIndex = totalCountQuery.indexOf("ORDER BY")
        if (orderByIndex != -1) {
            // Replace everything after "ORDER BY" with the new clause
            totalCountQuery = totalCountQuery.substring(0, orderByIndex-1)
        }
        totalCount = ValidatedSignal.executeQuery(totalCountQuery)?.first()?:0
        List<Map> validatedSignals = ValidatedSignal.executeQuery(query, [max: pageSize, offset: offset])
        List<Map> signals = []
        boolean isClosed = false
        boolean forceJustification = user?.getWorkflowGroup()?.forceJustification?:false
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.STANDARD_DATE)
        validatedSignals.each{
            isClosed=false;
            StringBuilder sb=new StringBuilder("")
            def jsonSlurper = new JsonSlurper()
            List<Object> productGroupSelection = (StringUtils.isNotBlank(it.productGroupSelection))?jsonSlurper.parseText(it.productGroupSelection):null;
            Map object = (null!=it.products)?jsonSlurper.parseText(it.products):null;
            List<String>  selectProductDictionaryItems=Holders.config.alert.selectProductDictionaryItems;
            String rgx= "[(]{1}[0-9]+[)]{1}";
            if(productGroupSelection!=null&& productGroupSelection.size()>0){
                productGroupSelection.each({
                    sb.append(it.name.replace(",",", ") .replaceAll(rgx, "")).append(", ")
                });
            }
           for (String selectProduct:selectProductDictionaryItems){
               List<Object> pros=(null!=object)?object."${selectProduct}":null;
               if(pros!=null&& pros.size()>0){
                   pros.each({
                       sb.append(it.name).append(", ");
                   });
               }
           }
            if(sb.length()>0){
                int index=sb.lastIndexOf(",");
                if(sb.charAt(index-1)==' '){
                    index--;
                }
                String str=sb.substring(0,index)
                sb=new StringBuilder().append("(").append(str).append(")");
            }
            Disposition disposition=it.disposition;
            isClosed = (it.signalStatus == null) ? false : Constants.WorkFlowLog.DATE_CLOSED.equalsIgnoreCase(it.signalStatus) ? true : false;
            signals.add([name: it.name, id: it.id, isClosed: isClosed, products: sb?.toString(), detectedDate:(it?.detectedDate != null) ? sdf?.format(it?.detectedDate) : "-" , disposition: disposition?.value, forceJustification: forceJustification])
        }
        return [items: signals.sort({it.name.toUpperCase() }), total_count: totalCount] as Map
    }


    Map fetchAlertGridSignals(String searchTerm = null, Integer page = 1, Integer pageSize = 50) {
        User user = cacheService.getUserByUserNameIlike(userService.getCurrentUserName())
        List<Long> groupIds = user.groups?.collect { it.id }
        String query = prepareValidatedSignalHQL(null,null,groupIds,user,'name','asc')
        int offset = 0
        int totalCount = 0
        offset = (page - 1) * pageSize
        if(searchTerm) {
            query = query.replace(Constants.DEFAULT_SQL_CONDITION, "vs.name like '%" + searchTerm + "%'")
        }
        String totalCountQuery = query.replace('new Map(vs.name as name, vs.id as id , vs.products as products, vs.signalStatus as signalStatus,vs.detectedDate as detectedDate ,vs.disposition as disposition,vs.productGroupSelection as productGroupSelection)','COUNT(*)')
        int orderByIndex = totalCountQuery.indexOf("ORDER BY")
        if (orderByIndex != -1) {
            // Replace everything after "ORDER BY" with the new clause
            totalCountQuery = totalCountQuery.substring(0, orderByIndex-1)
        }
        totalCount = ValidatedSignal.executeQuery(totalCountQuery)?.first()?:0
        List<Map> validatedSignals = ValidatedSignal.executeQuery(query, [max: pageSize, offset: offset])
        List<Map> signals = []
        boolean isClosed = false
        boolean forceJustification = user?.getWorkflowGroup()?.forceJustification?:false
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.STANDARD_DATE)
        validatedSignals.each{
            isClosed=false;
            StringBuilder sb=new StringBuilder("")
            def jsonSlurper = new JsonSlurper()
            List<Object> productGroupSelection = (StringUtils.isNotBlank(it.productGroupSelection))?jsonSlurper.parseText(it.productGroupSelection):null;
            Map object = (null!=it.products)?jsonSlurper.parseText(it.products):null;
            List<String>  selectProductDictionaryItems=Holders.config.alert.selectProductDictionaryItems;
            String rgx= "[(]{1}[0-9]+[)]{1}";
            if(productGroupSelection!=null&& productGroupSelection.size()>0){
                productGroupSelection.each({
                    sb.append(it.name.replace(",",", ") .replaceAll(rgx, "")).append(", ")
                });
            }
            for (String selectProduct:selectProductDictionaryItems){
                List<Object> pros=(null!=object)?object."${selectProduct}":null;
                if(pros!=null&& pros.size()>0){
                    pros.each({
                        sb.append(it.name).append(", ");
                    });
                }
            }
            if(sb.length()>0){
                int index=sb.lastIndexOf(",");
                if(sb.charAt(index-1)==' '){
                    index--;
                }
                String str=sb.substring(0,index)
                sb=new StringBuilder().append("(").append(str).append(")");
            }
            Disposition disposition=it.disposition;
            isClosed = (it.signalStatus == null) ? false : Constants.WorkFlowLog.DATE_CLOSED.equalsIgnoreCase(it.signalStatus) ? true : false;
            signals.add([name: it.name, id: it.id, isClosed: isClosed, products: sb?.toString(), detectedDate:(it?.detectedDate != null) ? sdf?.format(it?.detectedDate) : "-" , disposition: disposition?.value, forceJustification: forceJustification])
        }
        return [signals: signals.sort({it.name.toUpperCase() }), totalCount: totalCount] as Map
    }
    Map fetchSignalDataFromSignal(ValidatedSignal validatedSignal,String productJson,String productGroup ) {
        if(null==validatedSignal){
            return [:];
        }
        boolean isClosed = false
        StringBuilder sb=new StringBuilder("")
        def jsonSlurper = new JsonSlurper()
        if (validatedSignal.productGroupSelection) {
            List<Object> productGroupSelection = (null != validatedSignal.productGroupSelection) ? jsonSlurper.parseText(validatedSignal.productGroupSelection) : null
            String rgx = "[(]{1}[0-9]+[)]{1}";
            if (productGroupSelection != null && productGroupSelection.size() > 0) {
                productGroupSelection.each({
                    sb.append(it.name.replace(",", ", ").replaceAll(rgx, "")).append(", ")
                })
            }
        }
        Map object = (null!=validatedSignal.products)?jsonSlurper.parseText(validatedSignal.products):null;
        List<String>  selectProductDictionaryItems=Holders.config.alert.selectProductDictionaryItems;
        for (String selectProduct:selectProductDictionaryItems){
            List<Object> pros=(null!=object)?object."${selectProduct}":null;
            if(pros!=null&& pros.size()>0){
                pros.each({
                    sb.append(it.name).append(", ");
                });
            }
        }
        if(sb.length()>0){
            int index=sb.lastIndexOf(",");
            if(sb.charAt(index-1)==' '){
                index--;
            }
            String str=sb.substring(0,index)
            sb=new StringBuilder().append("(").append(str).append(")");
        }
        Disposition disposition=validatedSignal.disposition;
        isClosed= (validatedSignal.signalStatus==null)?false:Constants.WorkFlowLog.DATE_CLOSED.equalsIgnoreCase(validatedSignal.signalStatus)?true:false;
        [name: validatedSignal.name, id:validatedSignal.id, isClosed:isClosed, products: sb.toString(),
         detectedDate:(null!=validatedSignal.detectedDate)?new SimpleDateFormat("dd-MMM-yyyy").format(validatedSignal.detectedDate):"-" , disposition:disposition.value]

    }

    def listActivities(signalId) {
        def activities = []
        if (signalId) {
            def signal = ValidatedSignal.get(signalId)
            activities = signal.activities
        }
        activities
    }

    def listTopicActivities(topicId) {
        def activities = []
        if (topicId) {
            def topic = Topic.get(topicId)
            activities = topic.activities
        }
        activities
    }

    def scheduleChartReport(signalName, chartName, caseData) {
        def user = userService.getUser()
        try {
            def url = Holders.config.pvreports.url
            def reportTemplates = ["severity"                  : "PVS - Distribution By Seriousness Over Time", "ageGroup": "PVS - Distribution By Age Group Over Time",
                                   "country"                   : "PVS - Distribution By Country Over Time", "gender": "PVS - Distribution By Gender Over Time",
                                   "onset"                     : "PVS - Distribution By Time to Onset",
                                   "quantitativeReviewOverTime": "SIGNAL_REVIEW_TEMPLATE_QUANTITATIVE"]
            def path = Holders.config.pvreports.api.scheduleReportApi
            def query = [userName   : user.username,
                         reportName : signalName,
                         template   : reportTemplates.get(chartName),  //Put the template from actual data.
                         caseNumbers: caseData]
            reportIntegrationService.postData(url, path, query, Method.POST)
        } catch (Exception e) {
            log.error(e.getMessage())
            [status: 500]
        }
    }

    def getReportExecutionStatus(id) {
        try {
            def url = Holders.config.pvreports.url
            def path = Holders.config.pvreports.api.reportStatusApi
            def query = [id: id]
            reportIntegrationService.postData(url, path, query, Method.POST)
        } catch (Exception e) {
            e.printStackTrace()
            [status: 500]
        }
    }

    def getChartData(id) {
        try {
            def url = Holders.config.pvreports.url
            def path = Holders.config.pvreports.api.reportChartApi
            def query = [id: id]
            reportIntegrationService.postData(url, path, query, Method.POST)
        } catch (Exception ex) {
            log.error(ex.getMessage())
            [status: 500]
        }
    }

    def createSignalWithDefaultValues() {
        ValidatedSignal validatedSignal = new ValidatedSignal()
        validatedSignal.disposition = Disposition.findByValue(Holders.config.pvsignal.disposition.default.value)
        validatedSignal.assignedTo = userService.getUser()
        validatedSignal.priority = Priority.findByValue(Holders.config.pvsignal.priority.signal.default.value)
        validatedSignal.initialDataSource = params.initialDataSource
        validatedSignal
    }

    def getEmergingSafetyIssueList(emergingSafetyList) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        def issueList = []
        emergingSafetyList.each { emergingIssue ->
            if(emergingIssue.eventName) {
                def signalObj = jsonSlurper.parseText(emergingIssue.eventName)
                signalObj["4"].each {
                    issueList.add(it.name)
                }
            }
        }
        issueList
    }

    List getSignalsByAggregateAlerts(List<AggregateCaseAlert> aggregateCaseAlerts) {
        def signalObjList = []
        if (aggregateCaseAlerts && aggregateCaseAlerts.size() > 0) {
            def aggAlertIds = (aggregateCaseAlerts?.collect { it.id }).join(",")
            def sql_statement = SignalQueryHelper.signal_agg_alerts_sql(aggAlertIds)
            signalObjList = getSignalList(sql_statement)
        }
        signalObjList
    }

    List getSignalsByEvdasAlerts(List<EvdasAlert> evdasAlerts) {
        def signalObjList = []
        if (evdasAlerts && evdasAlerts.size() > 0) {
            def evdasAlertsIds = (evdasAlerts.collect { it?.id }).join(",")
            def sql_statement = SignalQueryHelper.signal_evdas_alerts_sql(evdasAlertsIds)
            signalObjList = getSignalList(sql_statement)
        }
        signalObjList
    }

    private def getSignalList(sql_query) {
        def sql = new Sql(dataSource)
        def signalIdList = []
        def signalObjList = []
        try {
            sql.eachRow(sql_query, []) { row ->
                signalIdList.add(row["validated_signal_id"])
            }
            signalIdList.each { def signalId ->
                def signalObj = ValidatedSignal.get(signalId)
                signalObjList.add(signalObj)
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        signalObjList
    }

    /**
     * Gives the relevent signals based on the current signal.
     * @param: ValidatedSignal
     * @return: List of ValidatedSignal
     */
    def getRelevantSignals(ValidatedSignal vs) {
        List<ValidatedSignal> signals = []

        try {
            def pe_list = (vs.aggregateAlerts.collect { [it.productName, it.pt] } +
                    vs.evdasAlerts.collect { [it.substance, it.pt] }).unique()

            def acaAlerts = pe_list.collect {
                AggregateCaseAlert.findAllByProductNameAndPt(it[0], it[1])
            }.unique()

            acaAlerts = acaAlerts.flatten()
            if (acaAlerts) {
                signals = getSignalsByAggregateAlerts(acaAlerts)
            }

            def evdasList = pe_list.collect {
                EvdasAlert.findAllBySubstanceAndPt(it[0], it[1])
            }.unique()

            evdasList = evdasList.flatten()
            if (evdasList) {
                signals = signals + getSignalsByEvdasAlerts(evdasList)
            }
            signals = signals - vs
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error(ex.getMessage())
        }
        signals
    }

    def heatMapData(ValidatedSignal vs) {
        def socs = (vs.aggregateAlerts + vs.evdasAlerts).collect { it.soc }.unique().sort()
        def prodNames = (vs.aggregateAlerts + vs.evdasAlerts).collect { it.productName }.unique().sort()
        def data = []

        socs.eachWithIndex { soc, i ->
            def rowData = []
            prodNames.eachWithIndex { prodName, j ->
                def sumOfCumSpont = vs.aggregateAlerts.findAll { it.productName == prodName }.inject(0, {
                    Integer memo, AggregateCaseAlert next ->
                        memo = memo + next.cumSponCount
                        memo

                })

                def sumOfTotalEv = vs.evdasAlerts.findAll { it.productName == prodName }.inject(0, {
                    Integer memo, EvdasAlert next ->
                        memo = memo + next.totalEv
                        memo
                })

                rowData.push(sumOfCumSpont + sumOfTotalEv)
            }

            data.push(rowData)
        }

        socs = MedDraSOC.withTransaction {
            MedDraSOC.findAllByLangId(1).unique().collect { it.name }.sort()
        }
        prodNames = prodNames

        /* TODO We are making some fake data for the demo */
        def years = ['Jan-2017', 'Feb-2017', 'Mar-2017', 'Apr-2017', 'May-2017', 'Jun-2017', 'Jul-2017']

        def random = new Random()
        data = []

        years.eachWithIndex { iv, j ->
            socs.eachWithIndex { String entry, int i ->
                def ran = random.nextInt(200)
                if (ran)
                    data.add([j, i, ran])
            }
        }

        data = [[0, 0, 9], [0, 1, 16], [0, 2, 18], [0, 3, 19], [0, 4, 18], [0, 5, 13], [0, 6, 4], [0, 7, 10], [0, 8, 19], [0, 9, 12], [0, 10, 44], [0, 11, 62], [0, 12, 12], [0, 13, 72], [0, 14, 8], [0, 15, 15], [0, 16, 43], [0, 17, 18], [0, 18, 14], [0, 19, 4], [0, 20, 14], [0, 21, 64], [0, 22, 3], [0, 23, 35], [0, 24, 25], [0, 25, 52], [1, 0, 25], [1, 1, 2], [1, 2, 50], [1, 3, 27], [1, 4, 52], [1, 5, 9], [1, 6, 13], [1, 7, 18], [1, 8, 18], [1, 9, 8], [1, 10, 1], [1, 11, 17], [1, 12, 14], [1, 13, 30], [1, 14, 20], [1, 15, 34], [1, 16, 14], [1, 17, 16], [1, 18, 12], [1, 19, 44], [1, 20, 22], [1, 21, 4], [1, 22, 32], [1, 23, 69], [1, 24, 18], [1, 25, 12], [2, 0, 34], [2, 1, 15], [2, 2, 44], [2, 3, 14], [2, 4, 38], [2, 5, 19], [2, 6, 18], [2, 7, 12], [2, 8, 22], [2, 9, 37], [2, 10, 22], [2, 11, 31], [2, 12, 13], [2, 13, 23], [2, 14, 70], [2, 15, 12], [2, 16, 24], [2, 17, 36], [2, 18, 32], [2, 19, 70], [2, 20, 17], [2, 21, 17], [2, 22, 14], [2, 23, 36], [2, 24, 8], [2, 25, 50], [3, 0, 10], [3, 1, 39], [3, 2, 37], [3, 3, 41], [3, 4, 23], [3, 5, 16], [3, 6, 17], [3, 7, 18], [3, 8, 19], [3, 9, 6], [3, 10, 24], [3, 11, 17], [3, 12, 26], [3, 13, 6], [3, 14, 32], [3, 15, 26], [3, 16, 15], [3, 17, 7], [3, 18, 11], [3, 19, 40], [3, 20, 62], [3, 21, 32], [3, 22, 25], [3, 23, 7], [3, 24, 34], [3, 25, 18], [4, 0, 24], [4, 1, 38], [4, 2, 7], [4, 3, 38], [4, 4, 14], [4, 5, 7], [4, 6, 4], [4, 7, 42], [4, 8, 11], [4, 9, 30], [4, 10, 16], [4, 11, 49], [4, 12, 27], [4, 13, 11], [4, 14, 19], [4, 15, 41], [4, 16, 24], [4, 17, 74], [4, 18, 29], [4, 19, 60], [4, 20, 13], [4, 21, 15], [4, 22, 25], [4, 23, 25], [4, 24, 10], [4, 25, 14], [5, 0, 18], [5, 1, 17], [5, 2, 37], [5, 3, 17], [5, 4, 8], [5, 5, 12], [5, 6, 55], [5, 7, 33], [5, 8, 19], [5, 9, 31], [5, 10, 34], [5, 11, 6], [5, 12, 22], [5, 13, 17], [5, 14, 18], [5, 15, 42], [5, 16, 8], [5, 17, 14], [5, 18, 34], [5, 19, 7], [5, 20, 12], [5, 21, 5], [5, 22, 8], [5, 23, 18], [5, 24, 21], [5, 25, 10], [6, 0, 36], [6, 1, 35], [6, 2, 25], [6, 3, 7], [6, 4, 52], [6, 5, 36], [6, 6, 60], [6, 7, 73], [6, 8, 29], [6, 9, 12], [6, 10, 28], [6, 11, 3], [6, 12, 9], [6, 13, 4], [6, 14, 10], [6, 15, 37], [6, 16, 43], [6, 17, 8], [6, 18, 4], [6, 19, 10], [6, 20, 6], [6, 21, 7], [6, 22, 5], [6, 23, 14], [6, 24, 38], [6, 25, 50]]

        [socs: socs as JSON, years: years as JSON, data: data as JSON]
    }

    Map insertSingleAndAggregateCases(ValidatedSignal validatedSignal) {
        List<Map> scaList = []
        List<Map> aggList = []
        Map dataMap = [:]

        validatedSignal.aggregateAlerts.each { agg ->
            Map map = [:]
            map.put("caseNumber", null)
            map.put("verNumber", agg.productId)
            map.put("type", "PRODUCT_ID")
            aggList.add(map)
        }

        validatedSignal.singleCaseAlerts.each { sca ->
            Map map = [:]
            map.put("caseNumber", sca.caseNumber)
            map.put("verNumber", sca.caseVersion)
            map.put("type", "CASE_ID")
            scaList.add(map)
        }
        if (aggList || scaList) {
            Sql sql = null
            try {
                sql = new Sql(dataSource_pva)
                deletePreviousDataFromTable(sql)
                //populate data in table
                callSqlBatchStatement(aggList, sql)
                callSqlBatchStatement(scaList, sql)
                dataMap = callAssessmentProc(sql)
            } catch (Exception e) {
                log.error(e.printStackTrace())
            } finally {
                sql?.close()
            }
        }
        dataMap

    }

    Map generateAssessmentDataMap(SignalChartsDTO signalChartsDTO) {
        Sql sql = null
        Map dataMap = [:]
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            dataMap = callAssessmentProc(sql,signalChartsDTO)
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        dataMap
    }

    def deletePreviousDataFromTable(sql) {
        sql.execute("delete from signal_report_cases")
        sql.execute("COMMIT")
    }

    def callSqlBatchStatement(def list, sql) {
        def size = list.size()
        sql.withBatch(size, "insert into signal_report_cases(case_id, case_num, version_num, type) values (:val0, :val1, :val2, :val3)".toString(), { preparedStatement ->
            list.each {
                preparedStatement.addBatch(val0: null, val1: it.caseNumber, val2: it.verNumber, val3: it.type)
            }
        })
    }

    def callAssessmentProc(Sql sql,SignalChartsDTO signalChartsDTO = null) {
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            public int getType() {
                return OracleTypes.CURSOR
            }
        }

        if(signalChartsDTO){
            initializeGTTForAssessments(sql,signalChartsDTO)
        }

        def startDate = null
        def endDate = null
        if (signalChartsDTO?.dateRange && signalChartsDTO.dateRange[0] && signalChartsDTO.dateRange[1]) {
            startDate = """TO_DATE('${signalChartsDTO.dateRange[0]}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""
            endDate = """TO_DATE('${signalChartsDTO.dateRange[1]}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""
        } else {
            startDate = """TO_DATE('${new Date(Constants.DateFormat.MIN_DATE).format(SqlGenerationService.DATE_FMT)}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""
            endDate = """TO_DATE('${new Date().format(SqlGenerationService.DATE_FMT)}', '${SqlGenerationService.DATETIME_FMT_ORA}')"""
        }

        def map = [:]

        def procedure = "call pkg_signal_management.P_SIGNAL_REPORTS(${signalChartsDTO?.signalId},${startDate},${endDate},?)"
        log.info(procedure)

        try {
            sql.call("{${procedure}}",
                    [CURSOR_PARAMETER
                    ]) { result ->
                result.eachRow() { GroovyResultSetExtension resultRow ->
                    def rowList = []
                    def tempMap = [:]
                    def header = resultRow.getProperty("FULL_GROUP")
                    def val = resultRow.getProperty("VAL")
                    def percent = resultRow.getProperty("PERCENT")
                    def counts = resultRow.getProperty("COUNTS")


                    tempMap.put(val, percent)
                    tempMap.put(val+"Counts", counts)
                    rowList.add(tempMap)
                    if (!map[header]) {
                        map.put(header, rowList)
                    } else {
                        List tempList = map[header]
                        tempList = tempList + rowList
                        map.put(header, tempList)

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        map
    }

    void initializeGTTForAssessments(Sql sql, SignalChartsDTO signalChartsDTO) {
        String initialParamsInsert = sqlGenerationService.initializeAssessmentGtts(signalChartsDTO)
        if (initialParamsInsert) {
            sql.execute(initialParamsInsert)
        }
    }

    // Methods for sending data for exporting format.

    Map fetchExportDataForDistributionBySourceOverTime(SignalChartsDTO signalChartsDTO) {
        signalChartsDTO.chartType = SignalChartsEnum.SOURCE
        List rawData = validatedSignalChartService.fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        Map resultEntryMap = [:]
        List<Map> data = mapFilteredDataForExport(filteredData)
        List keyset = data[0]?.keySet() as List
        resultEntryMap.put('data', data)
        resultEntryMap.put('title', "Distribution By Source Over Time")
        resultEntryMap.put('rowNames', ["${keyset[0]}"])
        resultEntryMap.put('columnNames', keyset.subList(1, keyset.size()))
        resultEntryMap
    }

    Map fetchExportDataForDistributionByAgeOverTime(SignalChartsDTO signalChartsDTO){
        signalChartsDTO.chartType = SignalChartsEnum.AGE
        List rawData = validatedSignalChartService.fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        Map resultEntryMap = [:]
        List<Map> data = mapFilteredDataForExport(filteredData)
        List keyset = data[0]?.keySet() as List
        resultEntryMap.put('data', data)
        resultEntryMap.put('title', "Distribution By Age Group Over Time")
        resultEntryMap.put('rowNames', ["${keyset[0]}"])
        resultEntryMap.put('columnNames', keyset.subList(1, keyset.size()))
        resultEntryMap
    }

    Map fetchExportDataForDistributionByCountryOverTime(SignalChartsDTO signalChartsDTO){
        signalChartsDTO.chartType = SignalChartsEnum.COUNTRY
        List rawData = validatedSignalChartService.fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        Map resultEntryMap = [:]
        List<Map> data = mapFilteredDataForExport(filteredData)
        List keyset = data[0]?.keySet() as List
        resultEntryMap.put('data', data)
        resultEntryMap.put('title', "Distribution By Country Over Time")
        resultEntryMap.put('rowNames', ["${keyset[0]}"])
        resultEntryMap.put('columnNames', keyset.subList(1, keyset.size()))
        resultEntryMap
    }

    Map fetchExportDataForDistributionByGenderOverTime(SignalChartsDTO signalChartsDTO) {
        signalChartsDTO.chartType = SignalChartsEnum.GENDER
        List rawData = validatedSignalChartService.fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        Map resultEntryMap = [:]
        List<Map> data = mapFilteredDataForExport(filteredData)
        List keyset = data[0]?.keySet() as List
        resultEntryMap.put('data', data)
        resultEntryMap.put('title', "Distribution By Gender Over Time")
        resultEntryMap.put('rowNames', ["${keyset[0]}"])
        resultEntryMap.put('columnNames', keyset.subList(1, keyset.size()))
        resultEntryMap
    }

    Map fetchExportDataForDistributionByCaseOutcome(SignalChartsDTO signalChartsDTO){
        signalChartsDTO.chartType = SignalChartsEnum.OUTCOME
        List rawData = validatedSignalChartService.fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map filteredData = filterData(rawData)
        Map resultEntryMap = [:]
        List<Map> data = mapFilteredDataForExport(filteredData)
        List keyset = data[0]?.keySet() as List
        resultEntryMap.put('data', data)
        resultEntryMap.put('title', "Distribution By Case Outcome")
        resultEntryMap.put('rowNames', ["${keyset[0]}"])
        resultEntryMap.put('columnNames', keyset.subList(1, keyset.size()))
        resultEntryMap
    }

    Map fetchExportDataForDistributionBySeriousnessOverTime(SignalChartsDTO signalChartsDTO) {
        signalChartsDTO.chartType = SignalChartsEnum.SERIOUSNESS
        List rawData = validatedSignalChartService.fetchChartDataFromRemoteDatabase(signalChartsDTO)
        Map resultEntryMap = [:]
        List<Map> data = mapSeriousnessCountDataForExport(rawData)
        if (data) {
            List keyset = data[0]?.keySet() as List
            resultEntryMap.put('data', data)
            resultEntryMap.put('title', "Distribution By Seriousness Over Time")
            resultEntryMap.put('rowNames', ["${keyset[0]}"])
            resultEntryMap.put('columnNames', keyset.subList(1, keyset.size()))
        }
        resultEntryMap
    }

    Map filterData(List rawData) {

        Map filteredData = [:]

        List criteria = rawData.collect { it[1] }.unique()

        List dates = rawData.collect { it[0] }.unique()

        Map defaultMapForGroup = [:]

        dates.each {
            defaultMapForGroup.put(it, 0)
        }

        criteria.each {
            filteredData.put(it, defaultMapForGroup.clone())
        }

        rawData.each {
            filteredData[it[1]][it[0]] = it[2]
        }

        filteredData
    }

    List mapFilteredDataForExport(Map filteredData) {
        def result = []
        def keyset = filteredData.keySet()

        filteredData[keyset[0]]?.keySet().each { category ->
            Map temp = [:]
            temp.put("Category", category)
            keyset.each {
                temp.put(it, filteredData[it][category])
            }
            result << temp
        }
        result
    }

    List mapSeriousnessCountDataForExport(List rawData) {
        List result = []
        rawData.each {
            result << [
                    "Category"              : it[0],
                    "Serious"               : it[1],
                    "Non-Serious"           : it[2],
                    "Not Available"         : it[3]
            ]
        }

        result
    }

     List fetchDateRangeFromCaseAlerts(List<SingleCaseAlert> singleCaseAlerts) {

        DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

        def tmp = singleCaseAlerts.collect {
            inputFormatter.parseDateTime(it.getAttr(cacheService.getRptFieldIndexCache('masterInitReptDate')).split('T')[0])
        }.sort()

        def firstDate = tmp.first().withDayOfMonth(1).withTimeAtStartOfDay()
        def lastDate = tmp.last().dayOfMonth().withMaximumValue().plusDays(1).minusMillis(1)
        [outputFormatter.print(firstDate), outputFormatter.print(lastDate)]
    }

    Map<String, List<Map<String, String>>> createConceptsMap(ValidatedSignal validatedSignal) {
        Session session = sessionFactory.currentSession
        String sql_statement = SignalQueryHelper.signal_concepts_map(validatedSignal.id)
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        String conceptMapKey
        Map<String, List<Map<String, String>>> conceptsList = [:]

        sqlQuery.list().each { row ->
            conceptMapKey = row[0]
            conceptsList.put(conceptMapKey, [aggregateAlerts: row[1].toString(), singleCaseAlerts: row[2].toString(),
                                             evdasAlerts    : row[3].toString()])
        }
        session.flush()
        session.clear()
        conceptsList
    }

    def calculateSeriousness(serVal) {
        String calculatedSeriousness
        switch (serVal) {
            case "Serious":
                calculatedSeriousness = "Y"
                break
            case "Non-Serious":
                calculatedSeriousness = "N"
                break
            case "Unknown":
                calculatedSeriousness = "U"
                break
            default:
                calculatedSeriousness = "-"
        }
        return calculatedSeriousness
    }

    def calculateListedness(listVal) {
        String calculatedListedness
        switch (listVal) {
            case "Listed":
                calculatedListedness = "N"
                break
            case "Unlisted":
                calculatedListedness = "Y"
                break
            case "Unknown":
                calculatedListedness = "U"
                break;
            default:
                calculatedListedness = "-"
        }
        return calculatedListedness
    }

    Map fetchProductAndEventFromAggList(List<AggregateCaseAlert> aggregateCaseAlertList) {
        Map aggList = ['productList': new HashSet(), 'eventList': new HashSet()]

        aggregateCaseAlertList.each {
            aggList['productList'] << ["name": it.productName, "id": it.productId]
            aggList['eventList'] << ["name": it.pt, "id": it.ptCode]
        }
        aggList
    }

    List getAssignedSignalList(User user = null) {
        User currentUser = user ?: userService.getUser()
        List<Long> groupIds = currentUser.groups?.collect { it.id }
        List signalList = ValidatedSignal.withCriteria {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("productDictionarySelection", "productDictionarySelection")
                property(PRODUCTS, PRODUCTS)
            }
            or {
                'eq'("assignedTo", currentUser)
                if (groupIds) {
                    or {
                        groupIds.collate(700).each {
                            'in'("assignedToGroup.id", groupIds)
                        }
                    }
                }
            }

            'eq'("workflowGroup", currentUser.workflowGroup)
            'disposition' {
                'eq'('closed', false)
            }
        }
        signalList
    }

    def getProductNameList(String products) {
        String prdName = getNameFieldArrayFromJson(products)
        def prdList = Constants.Commons.DASH_STRING
        if (prdName) {
            prdList = prdName.tokenize(',')
        }
        return prdList
    }

    def generateSignalSummaryReport(def params, Boolean uploadAssessment = false, String addReferenceName = null,Boolean criteriaSheetCheck = false) {

        ValidatedSignal validatedSignal = ValidatedSignal.findById(Long.parseLong(params.signalId))
        def timezone = grailsApplication.config.server.timezone
        String userTimeZone = userService.user.preference.timeZone

        def summaryReportPreference
        if (uploadAssessment) {
            summaryReportPreference = JSON.parse('{"ignore":[],"required":["Appendix"]}')
        } else {
            summaryReportPreference = JSON.parse(validatedSignal.signalSummaryReportPreference ?: '{"ignore":[],"required":["Signal Information",' +
                    ' "WorkFlow Log", "Safety Observations", "References","Actions","Meetings", "RMMs", "Communication"]}')
        }

        def eventNames = []
        Iterator iter = getProductIdsWithName(validatedSignal.events).iterator()
        while(iter.hasNext())
            eventNames += iter.next() as String

        Map<String,List> signalSummary = [:]
        String initialDataSource = validatedSignal?.initialDataSource?.replace("##", ", ")
        def signalSummaryData = []
        if (uploadAssessment) {
            signalSummaryData.add([
                    'signalName'       : validatedSignal.name,
                    "product"          : alertService.productSelectionSignal(validatedSignal),
                    "initialDataSource": initialDataSource ?: "-",
                    "detectedDate"     : validatedSignal.detectedDate ? fromDateToString(validatedSignal.detectedDate, DEFAULT_DATE_FORMAT) : "-",
                    "priority"         : validatedSignal.priority.displayName ?: "-",
                    "disposition"      : validatedSignal.disposition.displayName ?: "-",
            ])
        } else {
            signalSummaryData.add([
                    'signalId'     : validatedSignal.id + "",
                    'signalName'   : validatedSignal.name,
                    'product'      : alertService.productSelectionSignal(validatedSignal),
                    'event'        : alertService.eventSelectionSignalWithSmq(validatedSignal),
                    "detectedDate" : validatedSignal.detectedDate ? fromDateToString(validatedSignal.detectedDate, DEFAULT_DATE_FORMAT) : "-",
                    "status"       : getSignalStatus(validatedSignal),
                    "closedDate"   : DateUtil.fromDateToString(validatedSignal.signalStatusHistories?.find { it.signalStatus == Constants.DATE_CLOSED }?.dateCreated, DateUtil.DEFAULT_DATE_FORMAT) ?: "-",
                    "signalSource" : initialDataSource,
                    "signalOutcome": validatedSignal.signalOutcomes*.name?.join(", "),
                    "actionTaken"  : validatedSignal.actionTaken.join(', ') ?: "-",
                    "udText1"      : validatedSignal.udText1,
                    "udText2"      : validatedSignal.udText2,
                    "udDropdown1"  : validatedSignal.ddValue1,
                    "udDropdown2"  : validatedSignal.ddValue2,
                    "udDate1"      : validatedSignal.udDate1? DateUtil.fromDateToString(validatedSignal.udDate1, DEFAULT_DATE_FORMAT) : null,
                    "udDate2"      : validatedSignal.udDate2? DateUtil.fromDateToString(validatedSignal.udDate2, DEFAULT_DATE_FORMAT) : null
            ])
        }
        signalSummary.put('signalSummaryData',signalSummaryData)

        def signalDetails = []
        signalDetails.add([
                "signalName"         : validatedSignal.name,
                "linkedSignal"       : validatedSignal.linkedSignals*.name?.join(", "),
                "evaluationMethod"   : validatedSignal.evaluationMethod?.join(", ") ?: "-",
                "risk/Topic"         : validatedSignal.topicCategories*.name?.join(", "),
                "reasonForEvaluation": validatedSignal.reasonForEvaluation,
                "comments"           : validatedSignal.genericComment?.split("\0")?.join("")
        ])
        signalSummary.put('signalDetails',signalDetails)
        def workflowLog = []
        def workFlowLogMap =[:]
        List<String> statusLists=alertAttributesService.getUnsorted('signalHistoryStatus');
        String validationDateStr=Holders.config.signal.defaultValidatedDate
        List<String> sortedList=alertAttributesService.get('signalHistoryStatus');
        if(SystemConfig.first().displayDueIn)
        {
            sortedList.add(Constants.WorkFlowLog.DUE_DATE)
        }
        List<SignalStatusHistory> signalStatusHistoryList = validatedSignal.signalStatusHistories;
        sortedList.each { String status ->
            SignalStatusHistory signalStatusHistory = signalStatusHistoryList.find {
                it.signalStatus == status
            }
               String dateKey = "${status.trim().replace(' ', '').toLowerCase()}Date"
               if(validationDateStr.equalsIgnoreCase(status)){
                   workFlowLogMap.put(dateKey, signalStatusHistory?.dateCreated ? DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT) : null)
                   workFlowLogMap.put("validationdateDate", signalStatusHistory?.dateCreated ? DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT) : null)
               }else if(dateKey.equals("duedateDate")){
                   workFlowLogMap.put(dateKey, validatedSignal?.actualDueDate?DateUtil.toDateStringPattern(validatedSignal?.actualDueDate, DateUtil.DATEPICKER_FORMAT)  : null)
               }else{
                   workFlowLogMap.put(dateKey, signalStatusHistory?.dateCreated ? DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT) : null)
               }
        }
        workFlowLogMap.putAll([
                'signalName'   : validatedSignal.name,
                'priority'     : validatedSignal.priority.displayName ,
                'disposition'  : validatedSignal.disposition.displayName ,
                'assignedTo'   : validatedSignal.assignedTo ? validatedSignal.assignedTo.fullName : validatedSignal.assignedToGroup.name
        ])

        workflowLog.add(workFlowLogMap)
        signalSummary.put('workflowLog',workflowLog)
        List<AttachmentDescription> attachmentDescriptions = []
        if (validatedSignal?.getReferences()?.size()>0) {
            attachmentDescriptions = AttachmentDescription.findAllByAttachmentInList(validatedSignal?.getReferences())
        }
        List references = validatedSignal?.getReferences()?.collect {
            AttachmentDescription attachmentDescription = attachmentDescriptions.find {ad -> ad?.attachmentId == it?.id}
            [
                    inputName  : it.inputName?:it.referenceLink,
                    referenceType: it.referenceType,
                    description: attachmentDescription?.description ?: Constants.Commons.BLANK_STRING,
                    timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, userTimeZone),
                    modifiedBy : attachmentDescription?.createdBy
            ]
        }
        if (validatedSignal?.attachments?.size()>0) {
            attachmentDescriptions = AttachmentDescription.findAllByAttachmentInList(validatedSignal?.attachments)
        }
        references.addAll(validatedSignal?.attachments.collect {
            AttachmentDescription attachmentDescription = attachmentDescriptions.find {ad -> ad.attachmentId == it.id}
            [
                    inputName  : it.inputName?:it.name,
                    referenceType: it.referenceType,
                    description: attachmentDescription?.description ?: Constants.Commons.BLANK_STRING,
                    timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, userTimeZone),
                    modifiedBy : attachmentDescription?.createdBy
            ]
        })

        signalSummary.put("references",references)

        def adhocCaseAlertListingData = []
        validatedSignal.adhocAlerts?.each {
            def signals = getSignalsFromAlertObj(it, Constants.AlertConfigType.AD_HOC_ALERT)
            adhocCaseAlertListingData.add([
                    "signalsAndTopics": signals?.collect { it.name + "(S)" }?.join(","),
                    "name"            : it.name ?: "",
                    "productSelection": it.productSelection == null ? getGroupNameFieldFromJson(it.productGroupSelection) : (getNameFieldFromJson(it.productSelection) ?: Constants.Commons.BLANK_STRING),
                    "eventSelection"  : it.eventSelection ? getNameFieldFromJson(it.eventSelection) : (getGroupNameFieldFromJson(it.eventGroupSelection) ?: Constants.Commons.BLANK_STRING),
                    "detectedBy"      : it.detectedBy,
                    "initDataSrc"     : it.initialDataSource,
                    "disposition"     : it.disposition?.displayName
            ])
        }

       List rMMs =  validatedSignal.signalRMMs.findAll {
            it.communicationType == "rmmType"
        }.collect {
           AttachmentLink attachmentLink = AttachmentLink.findByReferenceClassAndReferenceId("com.rxlogix.signal.SignalRMMs", it.id)
            [type                : it.type,
             country             : country(it.country),
             description         : it.description,
             status              : it.status,
             dueDate             : DateUtil.toDateStringWithoutTimezone(it.dueDate),
             fileName            : attachmentLink ? attachmentLink.attachments?.join(',') : it.attachments.collect { it.inputName != 'attachments' ? it.inputName : it.name }?.join(','),
             assignedToFullName  : it.assignedTo ? it.assignedTo.name : (it.assignedToGroup ? it.assignedToGroup.name : "-"),

            ]
        }
        signalSummary.put("rmmType",rMMs)


       List communication = validatedSignal.signalRMMs.findAll {
            it.communicationType == "communication"
        }.collect {
           AttachmentLink attachmentLink = AttachmentLink.findByReferenceClassAndReferenceId("com.rxlogix.signal.SignalRMMs", it.id)
            [type                : it.type,
             country             : country(it.country),
             description         : it.description,
             status              : it.status,
             dueDate             : DateUtil.toDateStringWithoutTimezone(it.dueDate),
             fileName            : attachmentLink ? attachmentLink.attachments?.join(',') : it.attachments.collect { it.inputName != 'attachments' ? it.inputName : it.name }?.join(','),
             assignedToFullName  : it.assignedTo ? it.assignedTo.name : (it.assignedToGroup ? it.assignedToGroup.name : "-"),
             email               : it.emailSent? DateUtil.stringFromDate(it.emailSent, DateUtil.DATEPICKER_FORMAT_AM_PM, userTimeZone): '_',
            ]
        }
        signalSummary.put("communication",communication)

        def quantitativeCaseAlertListingData = []
        Map aggAndEvdasAlertMap = combinedAndPrevAggAndEvdasAlertListMap(Long.parseLong(params.signalId))
        List prevAggAlertList = aggAndEvdasAlertMap.prevAggAlertList
        List prevEvdasAlertList = aggAndEvdasAlertMap.prevEvdasAlertList
        List combinedList = aggAndEvdasAlertMap.combinedList
        combinedList?.each { it ->
            def existingSignals = getSignalsFromAlertObj(it, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            def prevAggCaseAlert = prevAggAlertList.find { record ->
                it.productId == record.productId && it.ptCode == record.ptCode && it.alertConfigId == record.alertConfigurationId
            }
            if (it.dataSource != Constants.DataSource.EVDAS.toUpperCase()) {
                quantitativeCaseAlertListingData.add([
                        "alertName"       : it.alertName,
                        "productName"     : it.productName,
                        "soc"             : it.soc,
                        "eventPT"         : it.preferredTerm,
                        "newCount/cummCount": it.newCount1 + " / " + it.cumCount1,
                        "newSer/cummSer"  : it.newSeriousCount + " / " + it.cumSeriousCount,
                        "prr"             : it.prrValue.toString(),
                        "ror"             : it.rorValue.toString(),
                        "ebgm"            : it.ebgm == 0.0 ? "0" : it.ebgm.toString(),
                        "eb05/eb95"       : (it.eb05 == 0.0 ? "0" : it.eb05) + " / " + (it.eb95 == 0.0 ? "0" : it.eb95),
                        "trend"           : getTrend(it?.newCount, prevAggCaseAlert?.newSponCount),
                        "dataSource"      : it.dataSource,
                        "signalNames"     : existingSignals?.size() ? existingSignals.collect {
                            it?.name
                        }.join(",") : "-",
                        "disposition"     : it.disposition
                ])
            }
        }
        aggAndEvdasAlertMap.evdasAlerts?.each { it ->
            def existingSignals = getSignalsFromAlertObj(it, Constants.AlertConfigType.EVDAS_ALERT)
            def prevEvdasCaseAlert = prevEvdasAlertList.find { record ->
                it.substanceId == record.substanceId && it.ptCode == record.ptCode && it.alertConfigurationId == record.alertConfigurationId
            }
            quantitativeCaseAlertListingData.add([
                    "alertName"       : it.alertName,
                    "productName"     : it.productName,
                    "soc"             : it.soc,
                    "eventPT"         : it.preferredTerm,
                    "newCount/cummCount": it.newCount1 + " / " + it.cumCount1,
                    "newSer/cummSer"  : it.newSeriousCount + " / " + it.cumSeriousCount,
                    "prr"             : "-",
                    "ror"             : it.rorValue,
                    "ebgm"            : "-",
                    "eb05/eb95"       : "- / -",
                    "trend"           : getTrend(it?.newSpont, prevEvdasCaseAlert?.newSpont),
                    "dataSource"      : 'EVDAS',
                    "signalNames"     : existingSignals?.size() ? existingSignals.collect {
                        it.name
                    }.join(",") : "-",
                    "disposition"     : it.disposition
            ])
        }

        // fetching the single case alert list on the basis of user's blinding status for report
        List qualitativeCaseAlertListingData = getSingleAlertListForSignalSummaryReport(params.signalId as String)

        def literatureCaseAlertListingData = []
        validatedSignal.literatureAlerts?.each {
            def existingSignals = getSignalsFromAlertObj(it, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
            literatureCaseAlertListingData.add([
                    "alertName"  : it.name,
                    "priority"   : it.priority?.displayName,
                    "disposition": it.disposition?.displayName,
                    "articleTitle" : it.articleTitle,
                    "articleAuthors" : it.articleAuthors,
                    "publicationDate" : it.publicationDate
            ])
        }

        def actionsList = []
        validatedSignal.actions?.each {
            actionsList.add([
                    "type"          : it.meetingId ? Meeting.get(it.meetingId)?.meetingTitle : it.type.displayName,
                    "action"        : it.config?.displayName,
                    "details"       : it.details,
                    "dueDate"       : it.dueDate ? DateUtil.stringFromDate(it.dueDate, DateUtil.DEFAULT_DATE_FORMAT, timezone) : "-",
                    "assignedTo"    : it.assignedTo ? it.assignedTo.fullName : (it.assignedToGroup ? it.assignedToGroup.name : "-"),
                    "status"        : it.actionStatus == 'InProcess' ? ActionStatus["InProgress"].id : ActionStatus[it.actionStatus].id,
                    "completionDate": it.completedDate ? DateUtil.stringFromDate(it.completedDate, DateUtil.DEFAULT_DATE_FORMAT, timezone) : "-"
            ])
        }

        def meetingList = []
        validatedSignal.meetings?.sort { it.dateCreated }?.each {
            meetingList.add([
                    "meetingTitle"  : it.meetingTitle,
                    "meetingMinutes": it.meetingMinutes,
                    'meetingDate'   : it.meetingDate ? DateUtil.stringFromDate(it.meetingDate, DateUtil.DEFAULT_DATE_FORMAT, timezone) : "-",
                    'meetingOwner'  : it.meetingOwner.fullName ?: "-",
                    'meetingAgenda' : it.meetingAgenda ?: "-"
            ])
        }

        Map assessmentDetailMap = generateAssessmentDetailsMap(params, validatedSignal)
        List<Map> assessmentDetailsList = []
        String characteristicVal
        assessmentDetailMap.each { characteristic, val ->
            val.each { it ->
                it.each { category, number ->
                    if (!category.contains("Counts")) {
                        assessmentDetailsList << [
                                "characteristics": characteristicVal == characteristic ? "" : characteristic,
                                "category"       : category,
                                "number"         : number.toString() + '%',
                                "counts"         : it."${category}Counts".toString()
                        ]
                        characteristicVal = characteristic
                    }
                }
            }
        }
        Map<String, List<Map<String, String>>> conceptsMap = createConceptsMap(validatedSignal)
        List medicalConceptDistributionList = []
        conceptsMap.each { String medicalConcept, Map<String, String> value ->
            medicalConceptDistributionList << [
                    'medicalConcept': medicalConcept,
                    'caseCount'     : value['singleCaseAlerts'] ?: '0',
                    'argusCount'    : value['aggregateAlerts'] ?: '0',
                    'evdasCount'    : value['evdasAlerts'] ?: '0',
            ]
        }

        User user = userService.getUser()
        params.reportingInterval = toDateString(validatedSignal.startDate, user.preference.getTimeZone()) +
                " to " + toDateString(validatedSignal.endDate, user.preference.getTimeZone())

        def reportFile = dynamicReportService.createSignalSummaryReport(quantitativeCaseAlertListingData, qualitativeCaseAlertListingData,
                adhocCaseAlertListingData, literatureCaseAlertListingData, actionsList, meetingList, signalSummaryData, signalSummary, params, summaryReportPreference,
                assessmentDetailsList, medicalConceptDistributionList, false,uploadAssessment,addReferenceName,criteriaSheetCheck)
        reportFile
    }

    Map generateAssessmentDetailsMap(Map params,ValidatedSignal validatedSignal){
        SignalAssessmentDateRangeEnum dateRange = params.dateRange as SignalAssessmentDateRangeEnum
        List<String> dateRangeList = []
        String timeZone = Holders.config.server.timezone
        Map assessmentDetail = [:]
        String caseList = null

        switch (dateRange) {
            case SignalAssessmentDateRangeEnum.CUSTOM:
                dateRangeList = [params.startDate,params.endDate]
                break
            case SignalAssessmentDateRangeEnum.LAST_3_MONTH:
                dateRangeList = RelativeDateConverter.lastXMonths(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_6_MONTH:
                dateRangeList = RelativeDateConverter.lastXMonths(null, 6, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_3_YEAR:
                dateRangeList = RelativeDateConverter.lastXYears(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_5_YEAR:
                dateRangeList = RelativeDateConverter.lastXYears(null, 5, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.SIGNAL_DATA:
                List<SingleCaseAlert> singleCaseAlertList = fetchAllSCAForSignalId(validatedSignal.id)
                List<String> caseNumberList = singleCaseAlertList.collect{it.caseNumber}
                caseList = validatedSignalChartService.mapCaseNumberFormatForProc(caseNumberList)
                dateRangeList = validatedSignalChartService.fetchDateRangeFromCaseAlerts(validatedSignal.singleCaseAlerts as List<SingleCaseAlert>)
                assessmentDetail = insertSingleAndAggregateCases(validatedSignal)
                break
            case SignalAssessmentDateRangeEnum.LAST_1_YEAR:
            default:
                dateRangeList = RelativeDateConverter.lastXYears(null, 1, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
        }

        if(!assessmentDetail){
            SignalChartsDTO signalChartsDTO = new SignalChartsDTO()
            signalChartsDTO.productSelection = params.productSelection
            signalChartsDTO.productGroupSelection = params.productGroupSelection
            signalChartsDTO.eventSelection = params.eventSelection
            signalChartsDTO.eventGroupSelection = params.eventGroupSelection
            signalChartsDTO.signalId = validatedSignal.id
            signalChartsDTO.dateRange = dateRangeList
            signalChartsDTO.caseList = caseList
            signalChartsDTO.isMultiIngredient = validatedSignal?.isMultiIngredient
            assessmentDetail = generateAssessmentDataMap(signalChartsDTO)
        }
        assessmentDetail
    }

    File exportSignalActionDetailReport(Map params) {
        Long signalId = 0L
        if (params.signalId) {
            signalId = Long.parseLong(params.signalId)
        }
        List<Map> signalList = getSignalActionDetails(signalId)
        params.reportingInterval = Constants.Commons.BLANK_STRING
        params.products = Constants.Commons.BLANK_STRING
        params.showProductAndInterval = false
        File reportFile = dynamicReportService.createSignalActionDetailReport(new JRMapCollectionDataSource(signalList), params)
        reportFile
    }

    File generateSignalReports(def params) {
        ValidatedSignal signal = ValidatedSignal.get(params.signalId as Long)
        User user = userService.getUser()
        Boolean isClosed
        String closedDate
        signal.signalStatusHistories?.find {
            if (it.signalStatus == Constants.WorkFlowLog.DATE_CLOSED) {
                isClosed = true
                closedDate = toDateString(it.dateCreated)
                return true
            }
        }
        Map reportData = [
                'signalName'         : signal.topic ?: signal.name,
                'dateDetected'       : signal.detectedDate ? toDateString(signal.detectedDate) : Constants.Commons.DASH_STRING,
                'status'             : getSignalStatus(signal),
                'dateClosed'         : closedDate ?: Constants.Commons.DASH_STRING,
                'signalSource'       : signal.initialDataSource ? signal.initialDataSource.replace('[', '').replace(']', '').replace(Constants.Commons.SIGNAL_DATASOURCE_SEPERATOR , ", ") : Constants.Commons.DASH_STRING,
                'evaluationMethod'   : signal.evaluationMethod.toString().replace('[', '').replace(']', ''),
                'reasonForEvaluation': signal.reasonForEvaluation,
                'actionTaken'        : isClosed ? signal.actionTaken.join(", ") + "\n\n" + signal.signalOutcomes.name.join(", ") : Constants.Commons.DASH_STRING
        ]

        Set<AggregateCaseAlert> aggAlertList = signal.aggregateAlerts

        List reportDataList = []

        List filledPt = []
        Map data = [:]
        if (signal.events)
            data = ["signalTerm": ViewHelper.getDictionaryValues(signal.events, DictionaryTypeEnum.EVENT)]

        data += reportData
        reportDataList.add(data)

        params.reportingInterval = toDateString(signal.startDate, user.preference.getTimeZone()) +
                " to " + toDateString(signal.endDate, user.preference.getTimeZone())
        params.signalName = signal.name
        params.products = signal.products==null ? getGroupNameFieldFromJson(signal.productGroupSelection) : getProductNameList(signal.products)?.join(", ")
        //Added for PVS-53068
        params.criteriaSheetList = dynamicReportService.createCriteriaList(userService.getUser(), null, null, false, false)
        File reportFile = dynamicReportService.createPeberSignalReport(new JRMapCollectionDataSource(reportDataList), params, false)
        reportFile
    }

    String getTrend(currentNewCount, previousNewCount) {
        if (previousNewCount && currentNewCount) {
            if (previousNewCount * 1.5 < currentNewCount)
                return Constants.Commons.POSITIVE
            else
                return Constants.Commons.NEGATIVE
        }
        return Constants.Commons.EVEN
    }

    String getEbgmTrend(currentEbgmScore, previousEbgmScore) {

        if (previousEbgmScore && currentEbgmScore) {
            int compareScore = Double.compare(currentEbgmScore, previousEbgmScore);
            if (compareScore > 0) {
                return Constants.Commons.POSITIVE
            } else {
                return Constants.Commons.NEGATIVE
            }
        }
        return Constants.Commons.EVEN
    }

    String getPrrTrend(currentPrrValue, previousPrrValue) {

        if (currentPrrValue && previousPrrValue) {
            int compareScore = Double.compare(currentPrrValue, previousPrrValue);
            if (compareScore > 0) {
                return Constants.Commons.POSITIVE
            } else {
                return Constants.Commons.NEGATIVE
            }
        }
        return Constants.Commons.EVEN
    }

    def getAggregateAndEvdasAlertList(Long signalId, String searchTerm = null) {
        List combinedListForAggAndEvdas = []
        List prevAggAlertList = []
        Map aggAndEvdasAlertMap = getAggEvdasCombinedAndPrevAggAlertListsMap(signalId, searchTerm)
        combinedListForAggAndEvdas = aggAndEvdasAlertMap.aggEvdasCombinedAlertList
        prevAggAlertList = aggAndEvdasAlertMap.prevAggAlertList
        List<SpecialPE> specialPEList = SpecialPE.findAll()
        List<Map> resultList = []
        Boolean isSpecialPE
        String trend

        ExecutorService executorService = Executors.newFixedThreadPool(10)
        List<Future> futureList = combinedListForAggAndEvdas.collect { alert ->
            executorService.submit({ ->
                if (alert.dataSource == Constants.DataSource.EVDAS.toUpperCase()) {
                    isSpecialPE = specialPEService.isSpecialPE(alert.productName, alert.pt, specialPEList)
                    trend = Constants.Commons.EVEN
                } else {
                    isSpecialPE = specialPEService.isSpecialPE(alert.productName, alert.pt, specialPEList)
                    def prevAggCaseAlert = prevAggAlertList.find {
                        it.productId == alert.productId && it.ptCode == alert.ptCode && it.alertConfigurationId == alert.alertConfigId
                    }
                    trend = getTrend(alert?.newSponCount, prevAggCaseAlert?.newSponCount)
                }
                if (alert.dataSource == Constants.DataSource.VAERS.toUpperCase()) {
                    trend = Constants.Commons.BLANK_STRING
                }
                if (alert.dataSource == Constants.DataSource.VIGIBASE.toUpperCase()) {
                    trend = Constants.Commons.BLANK_STRING
                }

                if (alert.dataSource.contains("PVA")) {
                    alert.dataSource = alert.dataSource.replace("PVA", "Safety DB")
                } else if (alert.dataSource.contains("pva")) {
                    alert.dataSource = alert.dataSource.replace("pva", "Safety DB")
                } else if (alert.dataSource.contains("VIGIBASE")) {
                    alert.dataSource = alert.dataSource.replace("VIGIBASE", "VigiBase")
                } else if (alert.dataSource.contains("EUDRA")) {
                    alert.dataSource = alert.dataSource.replace("EUDRA", "EVDAS")
                }else if (alert.dataSource.contains("eudra")) {
                    alert.dataSource = alert.dataSource.replace("eudra", "EVDAS")
                }



                alert << [
                        trend      : trend,
                        isSpecialPE: isSpecialPE
                ]
                alert
            } as Callable)
        }
        futureList.each {
            resultList.add(it.get())
        }
        executorService.shutdown()
        resultList
    }

    List<Map> fillPeMap(Long signalId) {
        List combinedListForAggAndEvdas = []
        List prevAggAlertList = []
        List prevEvdasAlertList = []
        Map aggAndEvdasAlertMap = combinedAndPrevAggAndEvdasAlertListMap(signalId)
        combinedListForAggAndEvdas = aggAndEvdasAlertMap.combinedList
        prevAggAlertList = aggAndEvdasAlertMap.prevAggAlertList
        prevEvdasAlertList = aggAndEvdasAlertMap.prevEvdasAlertList

        List<Map> resultList = []
        String trend
        String ebgmTrend
        String prrTrend
        String serious
        Boolean ime = false, dme = false, ei = false, sm = false
        List<EmergingIssue> eiList = EmergingIssue.getAll()

        ExecutorService executorService = Executors.newFixedThreadPool(10)
        List<Future> futureList = combinedListForAggAndEvdas.collect { alert ->
            executorService.submit({ ->
                ime = false
                dme = false
                ei = false
                sm = false
                serious = "No"
                for (EmergingIssue emergingIssue in eiList) {
                    String eventJason = emergingIssue.eventName
                    def eventNameArray = getNameFieldFromJson(eventJason)?.tokenize(',')
                    if (eventNameArray*.toLowerCase().contains(alert.pt?.toLowerCase())) {
                        ime = emergingIssue.ime
                        dme = emergingIssue.dme
                        ei = emergingIssue.emergingIssue
                        sm = emergingIssue.specialMonitoring
                        break
                    }
                }
                if (alert instanceof EvdasAlert) {
                    def prevEvdasAlert = prevEvdasAlertList.find {
                        it.substanceId == alert.substanceId && it.ptCode == alert.ptCode && it.alertConfigurationId == alert.alertConfigurationId
                    }
                    trend = getTrend(alert.newEv, prevEvdasAlert?.newEv)
                    ebgmTrend = "-"
                    prrTrend = getPrrTrend(alert.prrValue, prevEvdasAlert?.prrValue)
                    if (alert.totalSerious > 0) {
                        serious = "Yes"
                    }

                } else {
                    AggregateCaseAlert prevAggAlert = prevAggAlertList.find {
                        it.productId == alert.productId && it.ptCode == alert.ptCode && it.alertConfigurationId == alert.alertConfigurationId
                    }
                    trend = getTrend(alert.newSponCount, prevAggAlert?.newSponCount)
                    ebgmTrend = getEbgmTrend(alert.ebgm, prevAggAlert?.ebgm)
                    prrTrend = getPrrTrend(alert.prrValue, prevAggAlert?.prrValue)
                    if (alert.cumSeriousCount > 0) {
                        serious = "Yes"
                    }
                }

                alert << [
                        ime      : ime.toString(),
                        dme      : dme.toString(),
                        ei       : ei.toString(),
                        sm       : sm.toString(),
                        trend    : trend,
                        prrTrend : prrTrend,
                        ebgmTrend: ebgmTrend,
                        serious  : serious
                ]
            } as Callable)
        }
        futureList.each {
            resultList.add(it.get())
        }
        executorService.shutdown()
        resultList
    }

    private Map getAggEvdasCombinedAndPrevAggAlertListsMap(Long signalId, String searchTerm) {
        String sql = SignalQueryHelper.agg_evdas_combined_alert_list_sql(
                signalId, userService.getCurrentUserId(), Constants.DataSource.DATASOURCE_PVA, Constants.DataSource.EVDAS, searchTerm
        )
        Session session = sessionFactory.currentSession
        List<Map> aggEvdasCombinedAlertList = []
        try {
            SQLQuery sqlQuery = session.createSQLQuery(sql)

            sqlQuery.addScalar("id", new LongType())
            sqlQuery.addScalar("alertConfigId", new LongType())
            sqlQuery.addScalar("alertName", new StringType())
            sqlQuery.addScalar("productId", new LongType())
            sqlQuery.addScalar("productName", new StringType())
            sqlQuery.addScalar("soc", new StringType())
            sqlQuery.addScalar("preferredTerm", new StringType())
            sqlQuery.addScalar("ptCode", new StringType())
            sqlQuery.addScalar("execConfigId", new LongType())
            sqlQuery.addScalar("dataSourceValue", new StringType())
            sqlQuery.addScalar("dataSource", new StringType())
            sqlQuery.addScalar("disposition", new StringType())

            sqlQuery.addScalar("newCount1", new IntegerType())
            sqlQuery.addScalar("cumCount1", new IntegerType())
            sqlQuery.addScalar("newSeriousCount", new IntegerType())
            sqlQuery.addScalar("cumSeriousCount", new IntegerType())

            sqlQuery.addScalar("prrValue", new DoubleType())
            sqlQuery.addScalar("rorValue", new DoubleType())
            sqlQuery.addScalar("ebgm", new DoubleType())
            sqlQuery.addScalar("eb05", new DoubleType())
            sqlQuery.addScalar("eb95", new DoubleType())

            sqlQuery.addScalar("isAggAlert", new BooleanType())
            sqlQuery.addScalar("isArchived", new BooleanType())

            sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
            aggEvdasCombinedAlertList = sqlQuery.list()
        } catch (Exception ex) {
            log.error("Error came while agg_evdas_combined_alert_list_sql executing: " + ex.getMessage())
        } finally {
            session.flush()
            session.clear()
        }

        List aggAlertList = aggEvdasCombinedAlertList.findAll{ it.isAggAlert == true }.unique {
            [it.productId, it.ptCode, it.soc, it.alertConfigId,it.preferredTerm]
        }
        List evdasAlertList = aggEvdasCombinedAlertList.findAll{ it.isAggAlert == false }.unique {
            [it.productId, it.ptCode, it.alertConfigId,it.preferredTerm]
        }

        List combinedList = aggAlertList
        combinedList += evdasAlertList

        List prevAggAlertList = []

        if (aggAlertList) {
            prevAggAlertList = getPrevAggAlertList(aggAlertList)
        }

        [aggEvdasCombinedAlertList: combinedList, prevAggAlertList: prevAggAlertList]
    }

    private Map combinedAndPrevAggAndEvdasAlertListMap(Long signalId) {
        String searchTerm = null
        List combinedList = []
        List aggAlertList = []
        List prevAggAlertList = []
        List prevEvdasAlertList = []
        List evdasAlertList = []
        aggAlertList = getCurrAggAlertList(signalId,searchTerm)
        aggAlertList += getCurrArchiveAggAlertList(signalId,searchTerm)
        aggAlertList.unique {
            [it.productId, it.ptCode, it.soc, it.alertConfigId, it.smqCode, it.preferredTerm]
        }
        if (aggAlertList) {
            combinedList += aggAlertList
            prevAggAlertList = getPrevAggAlertList(aggAlertList)
        }
        evdasAlertList = getCurrEvdasAlertList(signalId,searchTerm)
        evdasAlertList += getCurrArchivedEvdasAlertList(signalId, searchTerm)
        evdasAlertList.unique {
            [it.substanceId, it.ptCode, it.alertConfigId]
        }
        if (evdasAlertList) {
            combinedList += evdasAlertList
            prevEvdasAlertList = getPrevEvdasAlertList(evdasAlertList)
        }
        [combinedList: combinedList, prevAggAlertList: prevAggAlertList, prevEvdasAlertList: prevEvdasAlertList, aggregateAlerts: aggAlertList, evdasAlerts: evdasAlertList]
    }


    private List getCurrEvdasAlertList(Long signalId, String searchTerm = null) {
        List evdasAlertList = []
        Group workflowGroup = userService.getUser().workflowGroup
        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
        Date dateClosed = validatedSignal.milestoneCompletionDate
        List<Long> evdasAlertIds = []
        if (dateClosed && validatedSignal.signalStatus == Constants.DATE_CLOSED) {
            final Sql sql = new Sql(dataSource)
            try {
                String sqlQuery = SignalQueryHelper.validated_evdas_alert_ids(signalId, dateClosed)
                List rows = sql.rows(sqlQuery)
                evdasAlertIds = rows.collect { it.evdas_alert_id as Long }
            } catch(Exception ex){
                log.error(ex)
            } finally{
                sql?.close()
            }
            if (evdasAlertIds.size() == 0) {
                return []
            }
        }
        evdasAlertList = EvdasAlert.createCriteria().list {
            'executedAlertConfiguration' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("adhocRun", false)
                eq("workflowGroup", workflowGroup)
            }
            if (evdasAlertIds.size() > 0) {
                or {
                    evdasAlertIds.collate(1000).each{
                        'in'("id", it)
                    }
                }
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            if(searchTerm){
                or{
                    ilike('name', '%' + searchTerm + '%')
                    ilike('substance', '%' + searchTerm + '%')
                    ilike('soc', '%' + searchTerm + '%')
                    ilike('pt', '%' + searchTerm + '%')
                    ilike('rorValue', '%' + searchTerm + '%')
                    if(searchTerm.isInteger()){
                        eq('newEv', searchTerm.toInteger())
                        eq('totalEv', searchTerm.toInteger())
                        eq('newSerious', searchTerm.toInteger())
                        eq('totalSerious', searchTerm.toInteger())
                    }
                    disposition{
                        ilike('displayName', '%'+ searchTerm +'%')
                    }
                }
            }
            order("id", "desc")
        }

        evdasAlertList = evdasAlertList.unique {
            [it.substanceId, it.ptCode, it.alertConfigurationId]
        }

        evdasAlertList = evdasAlertList.collect {
            createAggEvdasMap(it, false, false)
        }
        evdasAlertList
    }

    private List getCurrArchivedEvdasAlertList(Long signalId, String searchTerm = null) {
        List archivedEvdasAlertList = []
        Group workflowGroup = userService.getUser().workflowGroup
        archivedEvdasAlertList = ArchivedEvdasAlert.createCriteria().list {
            'executedAlertConfiguration' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("adhocRun", false)
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            if(searchTerm){
                or{
                    ilike('name', '%' + searchTerm + '%')
                    ilike('substance', '%' + searchTerm + '%')
                    ilike('soc', '%' + searchTerm + '%')
                    ilike('pt', '%' + searchTerm + '%')
                    ilike('rorValue', '%' + searchTerm + '%')
                    if(searchTerm.isInteger()){
                        eq('newEv', searchTerm.toInteger())
                        eq('totalEv', searchTerm.toInteger())
                        eq('newSerious', searchTerm.toInteger())
                        eq('totalSerious', searchTerm.toInteger())
                    }
                    disposition{
                        ilike('displayName', '%'+ searchTerm +'%')
                    }
                }
            }
            order("id", "desc")
        }

        archivedEvdasAlertList = archivedEvdasAlertList.collect {
            createAggEvdasMap(it,false, false)
        }
        archivedEvdasAlertList
    }

    private List getPrevEvdasAlertList(List<Map> evdasAlertList) {
        List prevEvdasAlertList = []
        evdasAlertList.collate(999).each { evdasAlert ->
            List<String> nameList = evdasAlert.collect { it.alertName }
            List<Long> alertIdList = evdasAlert.collect { it.id }
            List<String> substanceIdPtCodeList = evdasAlert.collect {
                "(" + it.productId + "," + it.ptCode + ")"
            }

            prevEvdasAlertList += EvdasAlert.createCriteria().list {
                or {
                    nameList.collate(1000).each{
                        'in'('name', it)
                    }
                }
                'not' {
                    or {
                        alertIdList.collate(1000).each{
                            'in'('id', it)
                        }
                    }
                }
                sqlRestriction("(SUBSTANCE_ID ,PT_CODE) IN (${substanceIdPtCodeList.join(",")})")
                order("id", "desc")
            } as List<EvdasAlert>

            prevEvdasAlertList += ArchivedEvdasAlert.createCriteria().list {
                or {
                    nameList.collate(1000).each{
                        'in'('name', it)
                    }
                }
                'not' {
                    or {
                        alertIdList.collate(1000).each{
                            'in'('id', it)
                        }
                    }
                }
                sqlRestriction("(SUBSTANCE_ID ,PT_CODE) IN (${substanceIdPtCodeList.join(",")})")
                order("id", "desc")
            } as List<ArchivedEvdasAlert>
        }

        prevEvdasAlertList
    }

    List countEvdasAggregateAlerts(List signalIds) {
        final Sql sql = new Sql(dataSource)
        final Sql sqlEvdas = new Sql(dataSource)
        List combinedList = []
        List aggAlertIds = []
        List archiveAggCaseAlertList = []
        List archivedEvdasAlertList = []
        List aggCaseAlertList = []
        List evdasAlertIds = []
        List evdasAlertList = []
        List alertCountList = []
        Group workflowGroup = userService.getUser().workflowGroup
        try {
            String sqlQuery = SignalQueryHelper.validated_agg_alert_id_List(signalIds)
            List rows = sql.rows(sqlQuery)
            aggAlertIds = rows?.collect { it.agg_alert_id as Long}
            String sqlQueryEvdas = SignalQueryHelper.validated_evdas_alert_ids_List(signalIds)
            List rowsEvdas = sqlEvdas.rows(sqlQueryEvdas)
            evdasAlertIds = rowsEvdas.collect {it.evdas_alert_id as Long}
            aggCaseAlertList = AggregateCaseAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('productId', 'productId')
                    property('ptCode', 'ptCode')
                    property('smqCode', 'smqCode')
                    property('soc', 'soc')
                    property('alertConfiguration.id', 'alertConfigurationId')
                    property('vs.id', 'signalId')
                }
                'executedAlertConfiguration' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("adhocRun", false)
                    eq("workflowGroup", workflowGroup)
                }
                if (aggAlertIds.size() > 0) {
                    'or' {
                        aggAlertIds.collate(999).each {
                            'in'('id', it)
                        }
                    }
                }
                createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
                if (signalIds.size() > 0) {
                    'or' {
                        signalIds.collate(999).each {
                            'in'('vs.id', it)
                        }
                    }
                }
                order("id", "desc")
            } as List
            evdasAlertList = EvdasAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('substanceId', 'substanceId')
                    property('ptCode', 'ptCode')
                    property('alertConfiguration.id', 'alertConfigurationId')
                    property('vs.id', 'signalId')

                }
                'executedAlertConfiguration' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("adhocRun", false)
                    eq("workflowGroup", workflowGroup)
                }
                if (evdasAlertIds.size() > 0) {
                    evdasAlertIds.collate(999).each {
                        'in'('id', it)
                    }
                }
                createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
                if (signalIds.size() > 0) {
                    'or' {
                        signalIds.collate(999).each {
                            'in'('vs.id', it)
                        }
                    }
                }
                order("id", "desc")
            } as List
            archiveAggCaseAlertList = ArchivedAggregateCaseAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('productId', 'productId')
                    property('ptCode', 'ptCode')
                    property('smqCode', 'smqCode')
                    property('soc', 'soc')
                    property('alertConfiguration.id', 'alertConfigurationId')
                    property('vs.id', 'signalId')

                }
                'executedAlertConfiguration' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("adhocRun", false)
                    eq("workflowGroup", workflowGroup)
                }
                createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
                if (signalIds.size() > 0) {
                    'or' {
                        signalIds.collate(999).each {
                            'in'('vs.id', it)
                        }
                    }
                }
                order("id", "desc")
            } as List

            archivedEvdasAlertList = ArchivedEvdasAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('substanceId', 'substanceId')
                    property('ptCode', 'ptCode')
                    property('alertConfiguration.id', 'alertConfigurationId')
                    property('vs.id', 'signalId')

                }
                'executedAlertConfiguration' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("adhocRun", false)
                    eq("workflowGroup", workflowGroup)
                }
                createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
                if (signalIds.size() > 0) {
                    'or' {
                        signalIds.collate(999).each {
                            'in'('vs.id', it)
                        }
                    }
                }
                order("id", "desc")
            } as List
            if (aggCaseAlertList) {
                aggCaseAlertList.addAll(archiveAggCaseAlertList)
                aggCaseAlertList = uniqueList(aggCaseAlertList, ['productId', 'ptCode', 'soc', 'alertConfigurationId', 'signalId', 'smqCode'])
            }
            if (evdasAlertList) {
                evdasAlertList.addAll(archivedEvdasAlertList)
                evdasAlertList = uniqueList(evdasAlertList, ['substanceId', 'ptCode', 'alertConfigurationId', 'signalId'])
            }
            // Pre-allocate combined list with initial capacity Added changes for PVS-57034
            combinedList = new ArrayList(aggCaseAlertList.size() + evdasAlertList.size())

            // Add all elements from both lists to the combined list Added changes for PVS-57034
            combinedList.addAll(aggCaseAlertList)
            combinedList.addAll(evdasAlertList)

            // Create a map to store the counts of each signalId Added changes for PVS-57034
            Map<String, Integer> signalIdCounts = [:].withDefault { Constants.Commons.ZERO }

            // Populate the map with counts in a single pass Added changes for PVS-57034
            combinedList.each { item ->
                signalIdCounts[item.signalId]++
            }

            // Create the alertCountList using the pre-computed counts Added changes for PVS-57034
            signalIds?.each { signalId ->
                alertCountList.add([SIGNALID: signalId, COUNT: signalIdCounts[signalId] ?: Constants.Commons.ZERO])
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
            sqlEvdas?.close()
        }
        alertCountList
    }

    def uniqueList(list, properties) {
        HashSet uniqueSet = new HashSet()
        List resultList = []
        StringBuilder keyBuilder = new StringBuilder()

        for (item in list) {
            keyBuilder.setLength(Constants.Commons.ZERO) // Reset the StringBuilder added for PVS-57034
            properties.each { prop ->
                keyBuilder.append(item[prop])
            }
            String key = keyBuilder.toString()
            if (!uniqueSet.contains(key)) {
                uniqueSet.add(key)
                resultList.add(item)
            }
        }
        return resultList
    }
    private List getCurrAggAlertList(Long signalId, String searchTerm = null) {
        List aggCaseAlertList = []
        Group workflowGroup = userService.getUser().workflowGroup
        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
        Date dateClosed = validatedSignal.milestoneCompletionDate
        List<Long> aggAlertIds = []
        if (dateClosed && validatedSignal.signalStatus == Constants.DATE_CLOSED) {
            final Sql sql = new Sql(dataSource)
            try {
                String sqlQuery = SignalQueryHelper.validated_agg_alert_ids(signalId, dateClosed)
                List rows = sql.rows(sqlQuery)
                aggAlertIds = rows.collect { it.agg_alert_id as Long }
            } catch(Exception ex){
                log.error(ex)
            } finally{
                sql?.close()
            }
            if (aggAlertIds.size() == 0) {
                return []
            }
        }
        List safetyMapping = alertService.getDataSourceSubstring("Safety Db")
        Boolean isMappingRequired = safetyMapping.contains(searchTerm)
        aggCaseAlertList = AggregateCaseAlert.createCriteria().list {
            'executedAlertConfiguration' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("adhocRun", false)
                eq("workflowGroup", workflowGroup)
            }
            if (aggAlertIds.size() > 0) {
                or {
                    aggAlertIds.collate(1000).each{
                        'in'("id", it)
                    }
                }
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            if(searchTerm){
                or{
                    ilike('name', '%' + searchTerm + '%')
                    ilike('productName', '%' + searchTerm + '%')
                    ilike('soc', '%' + searchTerm + '%')
                    ilike('pt', '%' + searchTerm + '%')
                    if(searchTerm.isInteger()){
                        eq('newSponCount', searchTerm.toInteger())
                        eq('cumSponCount', searchTerm.toInteger())
                        eq('newSeriousCount', searchTerm.toInteger())
                        eq('cumSeriousCount', searchTerm.toInteger())
                    }
                    if(searchTerm.isDouble()){
                        eq('prrValue', searchTerm.toDouble())
                        eq('rorValue', searchTerm.toDouble())
                        eq('ebgm', searchTerm.toDouble())
                        eq('eb05', searchTerm.toDouble())
                        eq('eb95', searchTerm.toDouble())
                    }
                    alertConfiguration{
                        ilike('selectedDatasource', isMappingRequired ? 'pva': '%' + searchTerm +'%')
                    }
                    disposition{
                        ilike('displayName', '%'+ searchTerm +'%')
                    }
                }
            }
            order("id", "desc")
        }

        aggCaseAlertList = aggCaseAlertList.unique {
            [it.productId, it.ptCode, it.soc, it.alertConfigurationId, it.smqCode]
        }

        aggCaseAlertList = aggCaseAlertList.collect {
            Map data = toAggEvdasAlertMap(it, true)
            data.disposition = it.disposition.displayName
            data.productSelection = it.executedAlertConfiguration.productSelection
            data.dataSource = getDataSource(it.alertConfiguration.selectedDatasource)
            data.dataSourceValue = it.alertConfiguration.selectedDatasource
            data.priority = it.priority.displayName
            data.isArchived = false
            data
        }
        aggCaseAlertList
    }

    private List getCurrAggAlertListForReport(Long signalId) {
        List aggCaseAlertList = []
        aggCaseAlertList = AggregateCaseAlert.createCriteria().list {
            'executedAlertConfiguration' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("adhocRun", false)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            order("id", "desc")
        }

        aggCaseAlertList = aggCaseAlertList.unique {
            [it.productId, it.ptCode, it.soc, it.alertConfigurationId]
        }

        aggCaseAlertList = aggCaseAlertList.collect {
            Map data = toAggEvdasAlertMap(it, true)
            data.disposition = it.disposition.displayName
            data.productSelection = it.executedAlertConfiguration.productSelection
            data.dataSource = getDataSource(it.alertConfiguration.selectedDatasource)
            data.priority = it.priority.displayName
            data.isArchived = false
            data
        }
        aggCaseAlertList
    }

    private List getCurrArchiveAggAlertList(Long signalId, String searchTerm = null) {
        List archiveAggCaseAlertList = []
        Group workflowGroup = userService.getUser().workflowGroup
        List safetyMapping = alertService.getDataSourceSubstring("Safety Db")
        Boolean isMappingRequired = safetyMapping.contains(searchTerm)
        archiveAggCaseAlertList = ArchivedAggregateCaseAlert.createCriteria().list {
            'executedAlertConfiguration' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("adhocRun", false)
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            if(searchTerm){
                or{
                    ilike('name', '%' + searchTerm + '%')
                    ilike('productName', '%' + searchTerm + '%')
                    ilike('soc', '%' + searchTerm + '%')
                    ilike('pt', '%' + searchTerm + '%')
                    if(searchTerm.isInteger()){
                        eq('newSponCount', searchTerm.toInteger())
                        eq('cumSponCount', searchTerm.toInteger())
                        eq('newSeriousCount', searchTerm.toInteger())
                        eq('cumSeriousCount', searchTerm.toInteger())
                    }
                    if(searchTerm.isDouble()){
                        eq('prrValue', searchTerm.toDouble())
                        eq('rorValue', searchTerm.toDouble())
                        eq('ebgm', searchTerm.toDouble())
                        eq('eb05', searchTerm.toDouble())
                        eq('eb95', searchTerm.toDouble())
                    }
                    alertConfiguration{
                        ilike('selectedDatasource', isMappingRequired ? 'pva': '%' + searchTerm +'%')
                    }
                    disposition{
                        ilike('displayName', '%'+ searchTerm +'%')
                    }
                }
            }
            order("id", "desc")
        }
        archiveAggCaseAlertList = archiveAggCaseAlertList.collect {
            createAggEvdasMap(it, true, true)
        }
        archiveAggCaseAlertList
    }

    private createAggEvdasMap(def alert, boolean isAggAlert, boolean isArchived)
    {
        Map data = toAggEvdasAlertMap(alert, isAggAlert)
        data.disposition = alert.disposition.displayName
        data.productSelection = alert.executedAlertConfiguration.productSelection
        data.dataSource = isAggAlert ? alert.alertConfiguration.selectedDatasource.toUpperCase(): Constants.DataSource.EVDAS.toUpperCase()
        data.priority = alert.priority.displayName
        data.isArchived = isArchived
        data
    }

    private List getPrevAggAlertList(List<Map> aggCaseAlertList) {
        List prevAggregateCaseAlertList = []

        aggCaseAlertList.collate(999).each { aggCaseAlert ->
            List<String> nameList = aggCaseAlert.collect { it.alertName }
            List<Long> alertIdList = aggCaseAlert.collect { it.id }
            List<String> prodIdPtSocList = aggCaseAlert.collect {
                "(" + it.productId + "," + it.ptCode + ",'" + it.soc + "')"
            }

            prevAggregateCaseAlertList += AggregateCaseAlert.createCriteria().list {
                or {
                    nameList.collate(1000).each{
                        'in'('name', it)
                    }
                }
                'not' {
                    or {
                        alertIdList.collate(1000).each{
                            'in'('id', it)
                        }
                    }
                }
                sqlRestriction("(PRODUCT_ID ,PT_CODE,SOC) IN (${prodIdPtSocList.join(",")})")
                order("id", "desc")
            } as List<AggregateCaseAlert>

            prevAggregateCaseAlertList += ArchivedAggregateCaseAlert.createCriteria().list {
                or {
                    nameList.collate(1000).each{
                        'in'('name', it)
                    }
                }
                'not' {
                    or {
                        alertIdList.collate(1000).each{
                            'in'('id', it)
                        }
                    }
                }
                sqlRestriction("(PRODUCT_ID ,PT_CODE,SOC) IN (${prodIdPtSocList.join(",")})")
                order("id", "desc")
            } as List<ArchivedAggregateCaseAlert>
        }

        prevAggregateCaseAlertList
    }

    List<Map> getLiteratureAlertListByAttachedSignal(Long signalId)
    {
        List<Map> literatureAlertList =  getLiteratureAlertList(signalId)
        literatureAlertList += getArchiveLiteratureAlertList(signalId)
        return literatureAlertList.unique {
            [it.id, it.alertName]
        }
    }

    List getArchiveLiteratureAlertList(Long signalId) {
        Group workflowGroup = userService.getUser().workflowGroup
        def result = ArchivedLiteratureAlert.createCriteria().list {
            'exLitSearchConfig' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            order("id", "desc")
        }

        result += ArchivedEmbaseLiteratureAlert.createCriteria().list {
            'exLitSearchConfig' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            order("id", "desc")
        }

        List archiveLiteratureAlertList = []
        ExecutorService executorService = Executors.newFixedThreadPool(10)
        List<Future> futureList = result.collect { def alert ->
            executorService.submit({ ->
                composeDataLiterature(alert, true)
            } as Callable)
        }
        futureList.each {
            archiveLiteratureAlertList.add(it.get())
        }
        executorService.shutdown()
        return archiveLiteratureAlertList
    }

    List getLiteratureAlertList(Long signalId) {
        Group workflowGroup = userService.getUser().workflowGroup
        def result = LiteratureAlert.createCriteria().list {
            'exLitSearchConfig' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            order("id", "desc")
        }

        result += EmbaseLiteratureAlert.createCriteria().list {
            'exLitSearchConfig' {
                eq("isDeleted", false)
                eq("isEnabled", true)
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
            order("id", "desc")
        }

        //TODO: Is this really neccessary..may be remove it later
        def resultDispDataSrc = result.collect {
            [id         : it.id,
             disposition: it.disposition.displayName]
        }

        List literatureAlertList = []

        ExecutorService executorService = Executors.newFixedThreadPool(10)
        List<Future> futureList = result.collect { def alert ->
            def df = executorService.submit({ ->
                def resultData = resultDispDataSrc.find {
                    alert.id == it.id
                }
                composeDataLiterature(alert, false)
            } as Callable)
        }
        futureList.each {
            literatureAlertList.add(it.get())
        }
        executorService.shutdown()
        return literatureAlertList
    }

    Map getPriorityMap(Long priorityId) {
        Priority priority = cacheService.getPriorityByValue(priorityId)
        [value: priority?.value, iconClass: priority?.iconClass]
    }

    Map composeDataLiterature(def alert, boolean isArchived) {
        Map literatureAlertData = [
                id             : alert.id,
                alertName      : alert.name,
                priority       : getPriorityMap(alert.priorityId),
                title          : alert.articleTitle ?: "-",
                authors        : alert.articleAuthors ?: "-",
                publicationDate: alert.publicationDate ?: "-",
                disposition    : cacheService.getDispositionByValue(alert.dispositionId)?.value,
                articleId      : alert.articleId,
                assignedTo     : alert.assignedToId,
                isArchived     : isArchived
        ]
        return literatureAlertData
    }

    Map getValidatedSignalList(DataTableSearchRequest searchRequest, params, String selectedAlertsFilter = "null") {
        User user = userService.getUser()
        List<Long> groupIds = user.groups?.collect { it.id }
        String timeZone = user?.preference.timeZone
        String orderByProperty = searchRequest?.orderBy() == 'id' ? Constants.Commons.DATE_CREATED : searchRequest?.orderBy()
        String orderDirection = searchRequest?.searchParam?.orderDir()
        String searchAlertQuery = prepareValidatedSignalHQL(searchRequest, params, groupIds, user, orderByProperty, orderDirection, selectedAlertsFilter)
        List<Map> signals = getValidatedSignalsForSelectedPage(orderByProperty, searchAlertQuery, orderDirection, searchRequest, timeZone)

        Integer totalCount = getValidatedSignalCounts(user, searchRequest, searchRequest?.searchParam?.search?.getValue()?.toLowerCase(), params, groupIds, true, "")
        Integer filteredCount = getValidatedSignalCounts(user, searchRequest, searchRequest?.searchParam?.search?.getValue()?.toLowerCase(), params, groupIds, false, selectedAlertsFilter)

        [aaData: signals, recordsTotal: totalCount, recordsFiltered: filteredCount]
    }

    protected List<Map> getValidatedSignalsForSelectedPage(String orderByProperty, String searchAlertQuery, String orderDirection, DataTableSearchRequest searchRequest, String timeZone) {
        List<ValidatedSignal> validatedSignals
        Map<String, List<Map>> countersMap = [validatedSignalsWithCounts: [],
                                              aggList                   : [],
                                              finalCaseCountList        : []]

        if (SORTED_COLUMNS.contains(orderByProperty)) { // custom sorting
            // gel all validated signals by defined criteria
            validatedSignals = ValidatedSignal.executeQuery(searchAlertQuery)
            if (orderByProperty in [NO_OF_PECS, NO_OF_CASES]) {
                // set only PEC or case counts for signals and return counter lists in order not to initialize it 2nd time by db call
                countersMap = initCountsCollectionsAndCountsForValidatedSignals(validatedSignals, orderByProperty)
                // sort validated signals by appropriate counter
                List<Map> validatedSignalsWithCounts = countersMap.validatedSignalsWithCounts
                sortSignalsByOrderByProperty(validatedSignalsWithCounts, orderByProperty, orderDirection)
                validatedSignals = validatedSignalsWithCounts.collect { it.validatedSignal }
            } else {
                // set product names and statuses - this is uses groovy code, not db calls
                List<Map> validatedSignalsWithProductNamesAndStatuses = initProductNamesAndStatusesForValidatedSignals(validatedSignals)
                sortSignalsByOrderByProperty(validatedSignalsWithProductNamesAndStatuses, orderByProperty, orderDirection)
                validatedSignals = validatedSignalsWithProductNamesAndStatuses.collect { it.validatedSignal }
            }

            // match correct page
            int start = searchRequest?.searchParam?.start ?: 0
            int pageSize = searchRequest?.pageSize() ?: validatedSignals.size()
            validatedSignals = validatedSignals.subList(start, Math.min(start + pageSize, validatedSignals.size()))
        } else {
            validatedSignals = ValidatedSignal.executeQuery(searchAlertQuery, [offset: searchRequest?.searchParam?.start, max: searchRequest?.pageSize()])
        }
        List<Map> signalsPerPage = createValidatedSignalDTO(validatedSignals, timeZone, false, countersMap)
        signalsPerPage
    }

    protected void sortSignalsByOrderByProperty(List<Map> signals, String orderByProperty, String orderDirection) {
        switch (orderByProperty) {
            case NO_OF_PECS:
                signals.sort { signal1, signal2 -> orderDirection == "asc" ? signal1.noOfPec <=> signal2.noOfPec : signal2.noOfPec <=> signal1.noOfPec}
                break
            case NO_OF_CASES:
                signals.sort { signal1, signal2 -> orderDirection == "asc" ? signal1.noOfCases <=> signal2.noOfCases : signal2.noOfCases <=> signal1.noOfCases}
                break
            case PRODUCTS:
                signals.sort { signal1, signal2 -> orderDirection == "asc" ? StringUtils.compare(signal1.productName, signal2.productName) : StringUtils.compare(signal2.productName, signal1.productName)}
                break
            case SIGNAL_STATUS:
                signals.sort { signal1, signal2 -> orderDirection == "asc" ? StringUtils.compare(signal1.status, signal2.status) : StringUtils.compare(signal2.status, signal1.status)}
                break
        }
    }

    private int validatedSignalTotalCount(params, User user, List<Long> groupIds) {
        List<ValidatedSignal> totalList = ValidatedSignal.createCriteria().listDistinct {
            if (Holders.config.validatedSignal.shareWith.enabled) {
                createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
                createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
            }
            if (params?.callingScreen == 'dashboard') {
                or {
                    'eq'("assignedTo.id", user.id)
                    if (groupIds) {
                        or {
                            groupIds.collate(1000).each {
                                'in'("assignedToGroup.id", it)
                            }
                        }
                    }
                }
                'disposition' {
                    'eq'('closed', false)
                }
            }
            if (Holders.config.pvsignal.workflowGroup.based.security)
                eq('workflowGroup.id', user.workflowGroup.id)
            if (Holders.config.validatedSignal.shareWith.enabled) {
                or {
                    eq("shareWithUser.id", user.id)
                    if (groupIds) {
                        or {
                            groupIds.collate(1000).each {
                                'in'("shareWithGroup.id", it)
                            }
                        }
                    }
                    eq("createdBy", user.username)
                }
            }
        }
        totalList.size()
    }

    List exportValidatedSignal(DataTableSearchRequest searchRequest, params, String selectedSignalFilter= "null") {
        User user = userService.getUser()
        List<Long> groupIds = user.groups?.collect { it.id }
        List<ValidatedSignal> validatedSignals = []

        DataTableSearchRequest.DataTableSearchParam request = searchRequest?.searchParam
        String orderByProperty = StringUtils.defaultIfBlank(request.columns[request.order[0].column].name, 'id')
        String orderDirection = request.order[0].dir
        String searchAlertQuery = prepareValidatedSignalHQL(searchRequest, params, groupIds, user, orderByProperty, orderDirection, selectedSignalFilter)
        validatedSignals = ValidatedSignal.executeQuery(searchAlertQuery)
        validatedSignals
    }

    String prepareValidatedSignalHQL(DataTableSearchRequest searchRequest, params, List<Long> groupIds, User user,
                                     String orderByProperty, String orderDirection,String filterWithUsersAndGroups="null") {
        String searchKey = searchRequest?.searchParam?.search?.getValue()?.toLowerCase()
        StringBuilder searchAlertQuery = new StringBuilder()
        if (searchRequest) {
            searchAlertQuery.append("SELECT vs FROM ValidatedSignal vs WHERE 1=1")
        } else {
            searchAlertQuery.append("SELECT new Map(vs.name as name, vs.id as id , vs.products as products, vs.signalStatus as signalStatus,vs.detectedDate as detectedDate ,vs.disposition as disposition,vs.productGroupSelection as productGroupSelection) FROM ValidatedSignal vs WHERE 1=1")
        }
        validatedSignalSearchFilters(params, groupIds, searchAlertQuery, user, searchKey, searchRequest, false, filterWithUsersAndGroups)

            if (orderByProperty == 'signalId') {
                orderByProperty = 'id'
            }

            if (orderByProperty in ['detectedDate', 'lastUpdated', 'dateCreated','actualDateClosed']) {
                searchAlertQuery.append(" ORDER BY vs.${orderByProperty} ${orderDirection} ")
            } else {
                if (StringUtils.upperCase(orderByProperty) == "EVENTS") {
                    orderByProperty = 'eventsAndGroupCombination';
                    searchAlertQuery.append(" ORDER BY upper(SUBSTRING(vs.${orderByProperty}, 1, 4000)) ${orderDirection} ")
                }
                else if (SORTED_COLUMNS.contains(orderByProperty)) {
                    // actual sorting will happen using groovy code
                    searchAlertQuery.append("")
                } else if (orderByProperty in ["id"]) {
                    searchAlertQuery.append(" ORDER BY vs.${orderByProperty} ${orderDirection} ")
                } else {
                    searchAlertQuery.append(" ORDER BY lower(vs.${orderByProperty}) ${orderDirection} ")
                }
            }
        searchAlertQuery.toString()
    }

    // TODO - Need to verify with business analyst or check in the specification
    // if the code below really calculate number of case/PEC and might be useful in the future.
    // Currently application uses another logic for init cases/PECs for displaying on summary grid
    String orderByCount(String orderByProperty, String orderDirection) {
        String aggCountSql = """ SELECT
  vs.id,
  COALESCE(SUM(no_of_pec), 0) AS total_no_of_pec
FROM
  validated_signal vs
    LEFT JOIN (
    SELECT
      vga.validated_signal_id AS vsid,
      aa.product_id,
      aa.pt_code,
      COUNT(DISTINCT vga.agg_alert_id) AS no_of_pec
    FROM
      validated_agg_alerts vga
        JOIN agg_alert aa ON vga.agg_alert_id = aa.id
    GROUP BY
      vga.validated_signal_id, aa.product_id, aa.pt_code

    UNION

    SELECT
      vga.validated_signal_id AS vsid,
      aaa.PRODUCT_ID,
      aaa.pt_code,
      COUNT(DISTINCT vga.archived_aca_id) AS no_of_pec
    FROM
      validated_archived_aca vga
        JOIN archived_agg_alert aaa ON vga.archived_aca_id = aaa.id
    GROUP BY
      vga.validated_signal_id,  aaa.product_id, aaa.pt_code

    UNION

    SELECT
      vga.validated_signal_id AS vsid,
      ea.SUBSTANCE_ID,
      ea.pt_code,
      COUNT(DISTINCT vga.evdas_alert_id) AS no_of_pec
    FROM
      validated_evdas_alerts vga
        JOIN evdas_alert ea ON vga.evdas_alert_id = ea.id
    GROUP BY
      vga.validated_signal_id, ea.SUBSTANCE_ID, ea.pt_code

    UNION

    SELECT
      vga.validated_signal_id AS vsid,
      aea.SUBSTANCE_ID,
      aea.pt_code,
      COUNT(DISTINCT vga.archived_evdas_alert_id) AS no_of_pec
    FROM
      validated_arch_evdas_alerts vga
        JOIN archived_evdas_alert aea ON vga.archived_evdas_alert_id = aea.id
    GROUP BY
      vga.validated_signal_id, aea.SUBSTANCE_ID, aea.pt_code
  ) counts ON vs.id = counts.vsid
GROUP BY
  vs.id
ORDER BY
  total_no_of_pec ${orderDirection}"""

        String scaCountSql = """ SELECT
    vs.id,
    nvl(vgacount.no_of_cases, 0) + nvl(arcvgacount.no_of_cases, 0) AS count
FROM
    validated_signal vs
    LEFT JOIN (
        SELECT
            vga.validated_signal_id AS vsid,
            COUNT(1) AS no_of_cases
        FROM
            validated_single_alerts   vga
            JOIN single_case_alert         sca ON vga.single_alert_id = sca.id
        GROUP BY
            vga.validated_signal_id
    ) vgacount ON vs.id = vgacount.vsid
    LEFT JOIN (
        SELECT
            vga.validated_signal_id AS vsid,
            COUNT(1) AS no_of_cases
        FROM
            validated_archived_sca       vga
            JOIN archived_single_case_alert   asca ON vga.archived_sca_id = asca.id
        GROUP BY
            validated_signal_id) arcvgacount ON vs.id = arcvgacount.vsid
ORDER BY
    nvl(vgacount.no_of_cases, 0) + nvl(arcvgacount.no_of_cases, 0) ${orderDirection} """



        String evdasIdsSql =  orderByProperty == NO_OF_PECS?  aggCountSql :  scaCountSql


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

        if(evdasIds) {
            return ("order by decode(id, ${evdasIds.collect { [it, evdasIds.indexOf(it)] }?.flatten()?.join(',')})")
        } else {
            return ""
        }

    }

    String prepareValidatedSignalHQLDashboard(DataTableSearchRequest searchRequest, params, List<Long> groupIds, User user,
                                     String orderByProperty, String orderDirection , String filterWithUsersAndGroups = "null") {

        String searchKey = searchRequest?.searchParam?.search?.getValue()?.toLowerCase()
        StringBuilder searchAlertQuery = new StringBuilder()
        if(searchRequest) {
            searchAlertQuery.append("SELECT vs FROM ValidatedSignal vs WHERE 1=1")
        } else {
            searchAlertQuery.append("SELECT new Map(vs.name as name, vs.id as id , vs.products as products, vs.signalStatus as signalStatus,vs.detectedDate as detectedDate ,vs.disposition as disposition,vs.productGroupSelection as productGroupSelection) FROM ValidatedSignal vs WHERE 1=1")
        }
        validatedSignalSearchFiltersForDashboard(params, groupIds, searchAlertQuery, user, searchKey, searchRequest, false, filterWithUsersAndGroups)

        if (StringUtils.upperCase(orderByProperty) in [Constants.Commons.EVENTS, Constants.Commons.PRODUCTS]) {
            searchAlertQuery.append(" ORDER BY SUBSTRING(vs.${orderByProperty}, LENGTH(vs.${orderByProperty}), 1) ${orderDirection}")
        } else {
            String orderDir = orderByProperty == Constants.Commons.DATE_CREATED ? 'desc' : orderDirection;
            searchAlertQuery.append(" ORDER BY vs.${orderByProperty} ${orderDir} ")
        }
        searchAlertQuery.toString()
    }

    private void validatedSignalSearchFilters(params, List<Long> groupIds, StringBuilder searchAlertQuery, User user,
                                              String searchKey, DataTableSearchRequest searchRequest, boolean isTotalCount = false, String filterWithUsersAndGroups = "") {

        if (Holders.config.pvsignal.workflowGroup.based.security)
            searchAlertQuery.append(" and vs.workflowGroup = ${user?.workflowGroup?.id} ")
        if (params?.callingScreen == Constants.Commons.DASHBOARD) {
            String inClause
            if (groupIds && groupIds.size() <= 1000) {
                inClause = "(${groupIds.join(",")})"
            } else if (groupIds && groupIds.size() > 1000) {
                inClause = groupIds.collate(1000).join(" OR assignedToGroup.id IN ").replace("[", "(").replace("]", ")")
            }

            searchAlertQuery.append("and (vs.assignedTo =${user.id} or vs.assignedToGroup.id in ${inClause} ) and ")
            searchAlertQuery.append(" vs.disposition.closed = false ")

        } else {
            if (Holders.config.validatedSignal.shareWith.enabled) {
                searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.shareWithUser swu left join vss.shareWithGroup swg WHERE")
                String shareWithInClause
                if (groupIds && groupIds.size() <= 1000) {
                    shareWithInClause = "(${groupIds.join(",")})"
                } else if (groupIds && groupIds.size() > 1000) {
                    shareWithInClause = groupIds.collate(1000).join(" OR swg.id IN ").replace("[", "(").replace("]", ")")
                }
                searchAlertQuery.append(" ((swu.id =${user.id} or swg.id in ${shareWithInClause} )) or")
                searchAlertQuery.append(" (vs.createdBy ='${user.username}' or vs.assignedTo = ${user.id}) )")
            }
        }
        String esc_char = ""
        if (searchKey?.contains('_')) {
            searchKey = searchKey.replaceAll("\\_", "!_")
            esc_char = "!"
        }
        if (!isTotalCount && searchKey) {
            List<User> allMatchedUsers = User.findAllByFullNameIlike('%' + searchKey + '%')*.id
            List<Group> allMatchedGroups = Group.findAllByNameIlike('%' + searchKey + '%')*.id
            String regexSearchKey = searchKey.trim().replaceAll("'", "''");
            String regexLowerSearchKey = searchKey.trim().toLowerCase().replaceAll("'", "''");
            searchAlertQuery.append(" and (lower(vs.name) like '%${regexSearchKey}%' " + (esc_char ? "escape '${esc_char}'" : "") + "or ")
            searchAlertQuery.append(" (lower(vs.signalStatus)) like '%${regexSearchKey}%' " + (esc_char ? "escape '${esc_char}'" : "") + "or ")
            searchAlertQuery.append(" (lower(vs.productsAndGroupCombination) like '%${regexSearchKey}%'" + (esc_char ? "escape '${esc_char}'" : "") + ") or ")
            searchAlertQuery.append(" (lower(vs.eventsAndGroupCombination) like '%${regexSearchKey}%'" + (esc_char ? "escape '${esc_char}'" : "") + " )or ")
            searchAlertQuery.append(" lower(vs.disposition.displayName) like '%${regexSearchKey}%' " + (esc_char ? "escape '${esc_char}'" : "") + "or ")
            searchAlertQuery.append(" lower(vs.signalStatus) like '%${regexSearchKey}%'" + (esc_char ? "escape '${esc_char}'" : "") + " or ")
            if (searchKey.isNumber()) { // Check if searchKey is numeric
                searchAlertQuery.append(" vs.id = ${searchKey} or ")
            }
            if (allMatchedUsers.size() > 0) {
                searchAlertQuery.append(" vs.assignedTo in (${allMatchedUsers.join(",")}) or ")
            }
            if (allMatchedGroups.size() > 0) {
                searchAlertQuery.append(" vs.assignedToGroup in (${allMatchedGroups.join(",")}) or ")
            }

            searchAlertQuery.append(" lower(vs.priority.displayName) like '%${regexSearchKey}%' )")
        }

        if (!isTotalCount) {
            searchRequest?.searchParam?.columns.each {
                String searchValue = it?.search?.value?.toLowerCase()
                String escape_char = ""
                if (searchValue) {
                    if (searchValue.contains('_')) {
                        searchValue = searchValue.replaceAll("\\_", "!_")
                        escape_char = "!"
                    }
                    searchValue = searchValue.replaceAll("'", "''")
                    if (it.name == Constants.Commons.PRODUCTS.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + " OR  lower(vs.productGroupSelection) like '%${searchValue}%' " + (escape_char ? "escape '${escape_char}'" : "") + ")")
                    } else if (it.name == Constants.Commons.EVENTS.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + " OR  lower(vs.eventGroupSelection) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + ")")
                    } else if (it.data == "signalOutcome") {
                        searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.signalOutcomes swu WHERE lower(swu.name) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + ") ")
                    } else if (it.data == "actionTaken") {
                        searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.actionTaken swu WHERE lower(swu) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + ") ")
                    } else if (it.name == "detectedDate") {
                        searchAlertQuery.append(" and to_char(vs.detectedDate,'dd-mon-yyyy') like '%${searchValue}%' " + (escape_char ? "escape '${escape_char}'" : ""))
                    } else if (it.name == "actualDateClosed") {
                        searchAlertQuery.append(" and to_char(vs.actualDateClosed,'dd-mon-yyyy') like '%${searchValue}%' " + (escape_char ? "escape '${escape_char}'" : ""))
                    } else if (it.data == "topicCategory") {
                        searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.topicCategories swu WHERE lower(swu.name) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + ") ")
                    } else if (it.data == "assignedTo") {
                        searchAlertQuery.append(" and ( vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.assignedTo swu WHERE lower(swu.fullName) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + ") ")
                        searchAlertQuery.append(" or vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.assignedToGroup swu WHERE lower(swu.name) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : "") + ") ) ")
                    } else {
                        searchAlertQuery.append(" and lower(vs.${it.name}) like '%${searchValue}%'" + (escape_char ? "escape '${escape_char}'" : ""))
                    }

                }
            }
            List<String> selectedDataForFilteringSignals = (filterWithUsersAndGroups == null || filterWithUsersAndGroups == "null" || filterWithUsersAndGroups == "") ? [] : filterWithUsersAndGroups?.substring(1,filterWithUsersAndGroups?.length()-1).replaceAll("\"", "").split(",");
            searchAlertQuery.append(createFilterQueryForSignals(selectedDataForFilteringSignals, user))
        }
    }

    String createFilterQueryForSignals(List<String> selectedDataForFilteringSignals, User user) {
        String filterAlertsQuery = ""
        String x = "AllSignals_" + user.id.toString()
        Boolean filterApplied = false
        if(selectedDataForFilteringSignals.size() && !selectedDataForFilteringSignals.contains(x)) {
            filterAlertsQuery = " and ( "
            selectedDataForFilteringSignals.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "vs.assignedTo=${extractedId} or "
                    filterApplied = true
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "vs.assignedToGroup=${extractedId} or "
                    filterApplied = true
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    User userById = User.findById(extractedId as Long)
                    filterAlertsQuery += "vs.createdBy='${userById.username}' or "
                    filterApplied = true
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "vs.assignedTo=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "vs.assignedToGroup=${it.id} or "
                    }
                    filterApplied = true
                }
            }
            if(filterApplied) {
                filterAlertsQuery = filterAlertsQuery.substring(0, filterAlertsQuery.length() - 3)
                filterAlertsQuery += " ) "
            }
        }
        filterApplied? filterAlertsQuery: ""
    }

    private void validatedSignalSearchFiltersForDashboard(params, List<Long> groupIds, StringBuilder searchAlertQuery, User user,
                                              String searchKey, DataTableSearchRequest searchRequest, boolean isTotalCount = false, String filterWithUsersAndGroups = "") {

        if (Holders.config.pvsignal.workflowGroup.based.security)
            searchAlertQuery.append(" and vs.workflowGroup = ${user.workflowGroup.id} ")
        if (Holders.config.validatedSignal.shareWith.enabled) {
            searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.shareWithUser swu left join vss.shareWithGroup swg WHERE")
            String shareWithInClause
            if (groupIds && groupIds.size() <= 1000) {
                shareWithInClause = "(${groupIds.join(",")})"
            } else if (groupIds && groupIds.size() > 1000) {
                shareWithInClause = groupIds.collate(1000).join(" OR swg.id IN ").replace("[", "(").replace("]", ")")
            }
            searchAlertQuery.append(" ((swu.id =${user.id} or swg.id in ${shareWithInClause} )) or")
            searchAlertQuery.append(" vs.createdBy ='${user.username}' )")
        }
        if (!isTotalCount && searchKey) {
            List<User> allMatchedUsers = User.findAllByFullNameIlike('%' + searchKey + '%')*.id
            List<Group> allMatchedGroups = Group.findAllByNameIlike('%' + searchKey + '%')*.id
            searchAlertQuery.append(" and (lower(vs.name) like '%${searchKey}%' or ")
            searchAlertQuery.append(" (lower(vs.products) like '%${searchKey}%' OR  lower(vs.productGroupSelection) like '%${searchKey}%') or ")
            searchAlertQuery.append(" (lower(vs.events) like '%${searchKey}%' OR  lower(vs.eventGroupSelection) like '%${searchKey}%' )or ")
            searchAlertQuery.append(" lower(vs.disposition.displayName) like '%${searchKey}%' or ")
            searchAlertQuery.append(" lower(NVL(vs.signalStatus,'${Constants.ONGOING_SIGNAL}')) like '%${searchKey}%' or ")
            if (allMatchedUsers.size() > 0) {
                searchAlertQuery.append(" vs.assignedTo in (${allMatchedUsers.join(",")}) or ")
            }
            if (allMatchedGroups.size() > 0) {
                searchAlertQuery.append(" vs.assignedToGroup in (${allMatchedGroups.join(",")}) or ")
            }

            searchAlertQuery.append(" lower(vs.priority.displayName) like '%${searchKey}%' )")
        }

        if(!isTotalCount) {
            searchRequest?.searchParam?.columns.each {
                String searchValue = it?.search?.value?.toLowerCase()
                if (searchValue) {
                    if (it.name == Constants.Commons.PRODUCTS.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like '%${searchValue}%' OR  lower(vs.productGroupSelection) like '%${searchValue}%' )")
                    } else if (it.name == Constants.Commons.EVENTS.toLowerCase()) {
                        searchAlertQuery.append(" and (lower(vs.${it.name}) like '%${searchValue}%' OR  lower(vs.eventGroupSelection) like '%${searchValue}%' )")
                    } else if (it.data == "signalOutcome") {
                        searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.signalOutcomes swu WHERE lower(swu.name) like '%${searchValue}%') ")
                    } else if (it.data == "actionTaken") {
                        searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.actionTaken swu WHERE lower(swu) like '%${searchValue}%') ")
                    } else if (it.name == "detectedDate") {
                        searchAlertQuery.append(" and to_char(vs.detectedDate,'dd-mon-yyyy') like '%${searchValue}%' ")
                    } else if (it.data == "topicCategory") {
                        searchAlertQuery.append(" and vs.id in (select distinct vss.id from ValidatedSignal vss  left join vss.topicCategories swu WHERE lower(swu.name) like '%${searchValue}%') ")
                    } else if (it.data == "status") {
                        searchAlertQuery.append(" and lower(NVL(vs.signalStatus,'${Constants.ONGOING_SIGNAL}')) like '%${searchValue}%' ")
                    } else {
                        searchAlertQuery.append(" and lower(vs.${it.name}) like '%${searchValue}%' ")
                    }
                }
            }
            List<String> selectedDataForFilteringSignals = (filterWithUsersAndGroups == "null" || filterWithUsersAndGroups == "" || filterWithUsersAndGroups == "[null]" || filterWithUsersAndGroups == null) ? [] : filterWithUsersAndGroups?.substring(1,filterWithUsersAndGroups.length()-1).replaceAll("\"", "").split(",");
            searchAlertQuery.append(createFilterQueryForSignals(selectedDataForFilteringSignals, user))
        }
    }

    Map<String, List<Map>> initCountsCollectionsAndCountsForValidatedSignals(List<ValidatedSignal> validatedSignals, String orderByProperty) {
        List<Map> validatedSignalsWithCounts = []
        List<Map> finalCaseCountList = []
        List<Map> aggList = []

        if (validatedSignals.size()) {
            if (orderByProperty == NO_OF_CASES) {
                Session session = sessionFactory.currentSession
                List<Long> signalIdList = validatedSignals.collect { it.id }
                initCaseCounts(session, signalIdList, finalCaseCountList)
            }

            if (orderByProperty == NO_OF_PECS) {
                if(validatedSignals?.size() > 0)
                    aggList = countEvdasAggregateAlerts(validatedSignals*.id)
            }

            // Create lookup maps for faster access
            Map pecCountMap = aggList?.collectEntries { [(it.SIGNALID): it.COUNT] } ?: [:]
            Integer caseCount = 0
            validatedSignalsWithCounts= validatedSignals.collect { validatedSignal ->
                Integer pecCount = 0

                if (orderByProperty == NO_OF_PECS) {
                    pecCount = pecCountMap.get(validatedSignal.id, 0)
                }

                if (orderByProperty == NO_OF_CASES) {
                    caseCount = finalCaseCountList.find {
                        it.SIGNALID == validatedSignal.id
                    }?.COUNT ?: 0
                }

                [
                        validatedSignal  : validatedSignal,
                        noOfCases        : caseCount,
                        noOfPec          : pecCount
                ]
            }
        }

        [
                validatedSignalsWithCounts : validatedSignalsWithCounts,
                aggList                    : aggList,
                finalCaseCountList         : finalCaseCountList,
        ]
    }
    List<Map> initProductNamesAndStatusesForValidatedSignals(List<ValidatedSignal> validatedSignals) {
        List<Map> validatedSignalsWithProductNamesAndStatuses = []

        if (validatedSignals.size()) {
            validatedSignalsWithProductNamesAndStatuses = validatedSignals.collect { validatedSignal ->
                [
                        validatedSignal  : validatedSignal,
                        productName      : alertService.productSelectionSignal(validatedSignal),
                        status           : getSignalStatus(validatedSignal)
                ]
            }
        }

        validatedSignalsWithProductNamesAndStatuses
    }

    List<Map> createValidatedSignalDTO(List<ValidatedSignal> validatedSignals, String timeZone, boolean isDashboard = false,  Map<String, List<Map>> countersMap) {
        List<Map> validatedSignalsDTO = []
        List<Map> finalCaseCountList = countersMap.finalCaseCountList
        List<Map> aggList = countersMap.aggList

        if (validatedSignals.size()) {
            Session session = sessionFactory.currentSession
            List<Long> signalIdList = validatedSignals.collect { it.id }

            if (finalCaseCountList.isEmpty()) {
                initCaseCounts(session, signalIdList, finalCaseCountList)
            }

            List<Map> actionCountList = []
            List<Map> signalTopicCategoryList = []
            List<Map> signalOutcomesList = []
            List<Map> actionTakenList = getSignalActionTakenList(session, SignalQueryHelper.signal_action_taken_sql(signalIdList))

            if (!isDashboard) {
                actionCountList = generateSignalCountList(session, SignalQueryHelper.signal_action_count(signalIdList))
                signalTopicCategoryList = getValidatedSignalMultipleValuesList(signalIdList,'topicCategories')
                signalOutcomesList = getValidatedSignalMultipleValuesList(signalIdList,'signalOutcomes')
            }

            Integer pecCount
            Integer caseCount
            Integer actionCount
            Integer aggPecCount
            String topicCategory
            String outcome
            String actionTaken

            if (aggList.isEmpty()) {
                if (validatedSignals?.size() > 0)
                    aggList = countEvdasAggregateAlerts(validatedSignals*.id)
            }

            validatedSignalsDTO = validatedSignals.collect { validatedSignal ->

                pecCount = aggList?.find {
                    it.SIGNALID == validatedSignal.id
                }?.COUNT ?: 0

                caseCount = finalCaseCountList.find {
                    it.SIGNALID == validatedSignal.id
                }?.COUNT ?: 0

                actionCount = actionCountList.find {
                    it.SIGNALID == validatedSignal.id
                }?.COUNT ?: 0

                topicCategory = signalTopicCategoryList.findAll {
                    it.id == validatedSignal.id
                }.collect { it.name }.join(", ") ?: "-"

                outcome = signalOutcomesList.findAll {
                    it.id == validatedSignal.id
                }.collect { it.name }.join(", ") ?: "-"

                actionTaken = actionTakenList.findAll {
                    it.ID == validatedSignal.id
                }.collect { it.NAME }.join(", ") ?: "-"
                Integer dueIn

              if(!isDashboard){
                  dueIn = dueInCalculation(timeZone, validatedSignal.dueDate)
              }

                signalDto(validatedSignal, timeZone, pecCount, caseCount, actionCount, topicCategory, outcome, dueIn, actionTaken)
            }
        }
        validatedSignalsDTO
    }

    protected void initCaseCounts(Session session, List<Long> signalIdList, List<Map> finalCaseCountList) {
        List<Map> caseCountList = generateSignalCountList(session, SignalQueryHelper.signal_case_count(signalIdList))
        List icrSignalsIds = caseCountList ? caseCountList?.findAll { it.signalid } : []
        List archivedSignalIds = signalIdList - icrSignalsIds
        if (archivedSignalIds) {
            session = sessionFactory.currentSession
            caseCountList += generateSignalCountList(session, SignalQueryHelper.signal_case_archived_count(archivedSignalIds.flatten()))
        }

        def resultCount = caseCountList.groupBy { it.signalid }
        for (def res in resultCount) {
            finalCaseCountList.add(['SIGNALID': res.key, 'COUNT': res.value.count.sum()])
        }
    }

    List<Map> generateSignalCountList(Session session,String signalCountSql){
        List<Map> signalCountList = session.createSQLQuery(signalCountSql)
                                   .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                                   .list()
        session.flush()
        session.clear()
        signalCountList
    }

    private int getValidatedSignalCounts(User user, DataTableSearchRequest searchRequest, String searchKey, params, List<Long> groupIds, boolean isTotalCount, String selectedAlertsFilter="null") {
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append("SELECT count(vs.id) FROM ValidatedSignal vs WHERE 1=1 ")
        validatedSignalSearchFilters(params, groupIds, searchAlertQuery, user, searchKey, searchRequest, isTotalCount, selectedAlertsFilter)
        return ValidatedSignal.executeQuery(searchAlertQuery.toString())[0] as int
    }

    int getLevel(String productJson) {
        Map jsonMap = parseJsonString(productJson)
        def level = jsonMap.find { it.value }?.key
        level!=null ? level as int : 3
    }

    Map signalDto(ValidatedSignal validatedSignal, String timeZone = "UTC", Integer noOfPecs, Integer caseCount,Integer actionCount
                  ,String topicCategory, String outcome, Integer dueIn, String actionTaken) {
        String detectedDate = validatedSignal.detectedDate ? DateUtil.toDateStringWithoutTimezone(validatedSignal.detectedDate) : ""
        String actualDateClosed = validatedSignal.actualDateClosed ? DateUtil.toDateStringWithoutTimezone(validatedSignal.actualDateClosed) : "-"
        String rgx= "[(]{1}[0-9]+[)]{1}";
        String productName=alertService.productSelectionSignal(validatedSignal)
        productName=productName.replaceAll(rgx, "")

        [
                signalId         : validatedSignal.id,
                signalName       : validatedSignal.name,
                productName      : productName,
                eventName        : alertService.eventSelectionSignalWithSmq(validatedSignal),
                noOfCases        : caseCount,
                noOfPec          : noOfPecs,
                priority         : cacheService.getPriorityByValue(validatedSignal.priorityId).displayName,
                assignedTo       : userService.getAssignedToName(validatedSignal),
                actions          : actionCount,
                strategy         : validatedSignal.strategy ? validatedSignal.strategy.name : '-',
                topicCategory    : topicCategory,
                monitoringStatus : cacheService.getDispositionByValue(validatedSignal.dispositionId).displayName,
                signalSource     : validatedSignal.initialDataSource ? validatedSignal.initialDataSource?.split("##")?.join(", "): '-',                lastSubmitted    : '-',
                disposition      : cacheService.getDispositionByValue(validatedSignal.dispositionId).displayName,
                detectedDate     : detectedDate,
                status           : getSignalStatus(validatedSignal),
                dateClosed       : actualDateClosed,
                actionTaken      : actionTaken,
                signalOutcome    : outcome,
                dueIn            : dueIn != null ? dueIn : '-'
        ]
    }

    /**
     * Checks if a signal has a signal workflow log with 'Date Closed' as signal status.
     *
     * @param validatedSignalInstance
     */
    protected boolean checkIfSignalContainsDateClosed(ValidatedSignal validatedSignalInstance){
        validatedSignalInstance.signalStatusHistories.find{it.signalStatus == Constants.DATE_CLOSED} ? true : false
    }

    /**
     * Returns Signal Status [Closed/Ongoing] based on a single condition
     *
     * @param validatedSignalInstance
     */
    protected String getSignalStatus(ValidatedSignal validatedSignalInstance) {
        checkIfSignalContainsDateClosed(validatedSignalInstance) ? Constants.ActionStatus.CLOSED : Constants.ONGOING_SIGNAL
    }

    def getActionConfigurationList(String callingScreen) {
        List actionConfigList = []
        if (callingScreen == 'Topic' || callingScreen == 'Signal Management') {
            actionConfigList = ActionConfiguration.withCriteria {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("value", "value")
                    property("id", "id")
                }
                order("value", 'asc')
            }
        } else {
            actionConfigList = ActionConfiguration.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("value", "value")
                    property("id", "id")
                }
                ne('value', 'Meeting')
            }
        }
        actionConfigList
    }

    def pecAndCaseCounts(String signalId) {
        Session session = sessionFactory.currentSession
        String sql_statement = SignalQueryHelper.signal_pec_validated_signal_count(signalId)
        List pecAndCaseCountList = session.createSQLQuery(sql_statement).list()
        session.flush()
        session.clear()
        Map pecAndCaseCount = [:]
        pecAndCaseCountList.each { row ->
            pecAndCaseCount.put(row[0].toString(), row[1].toString())
        }
        pecAndCaseCount
    }


    /**
     * This method is responsible to fetch all the cases that are linked to the signal
     * for validated observations tab
     * @param validatedSignalId - signal id whose cases are required to be fetched
     * @return - return a Map of transformed values of each case
     */
    List<Map> getSingleCaseAlertListForSignal(String validatedSignalId) {
        Long signalId = Long.parseLong(validatedSignalId)
        User currentUser = userService.getUser()
        Map blindingFieldsForGroup = userService.getBlindingFieldsForGroup(currentUser.groups*.id)
        Map blindingFieldsForUser = userService.getBlindingFieldsForUser(blindingFieldsForGroup, true)
        Map blindedDataForSQL = userService.getBlindedDataForSQL(blindingFieldsForUser)
        boolean isAllFieldsRedacted = blindingFieldsForGroup.isAllFieldsRedacted == 1
        boolean isUserMappingAvailable = blindingFieldsForGroup.isUserMappingAvailable
        isAllFieldsRedacted = !(!isUserMappingAvailable && !isAllFieldsRedacted)
        String sql = SignalQueryHelper.single_case_and_single_archive_alert_list_sql(signalId, blindedDataForSQL.blindedData, blindedDataForSQL.redactedData, blindedDataForSQL.availableData, isAllFieldsRedacted ? 1 : 0)
        List<Map> singleCaseAlertMap = []
        Session session = sessionFactory.currentSession
        try {
            SQLQuery sqlQuery = session.createSQLQuery(sql)

            sqlQuery.addScalar("id", new LongType())
            sqlQuery.addScalar("alertConfigId", new LongType())
            sqlQuery.addScalar("caseId", new LongType())
            sqlQuery.addScalar("alertName", new StringType())
            sqlQuery.addScalar("caseNumber", new StringType())
            sqlQuery.addScalar("productName", new StringType())
            sqlQuery.addScalar("productFamily", new StringType())
            sqlQuery.addScalar("masterPrefTermAll", new StringType())
            sqlQuery.addScalar("caseVersion", new IntegerType())
            sqlQuery.addScalar("followUpNumber", new IntegerType())
            sqlQuery.addScalar("execConfigId", new LongType())
            sqlQuery.addScalar("disposition", new StringType())
            sqlQuery.addScalar("priorityId", new LongType())
            sqlQuery.addScalar("isStandalone", new BooleanType())
            sqlQuery.addScalar("isArchived", new BooleanType())
            sqlQuery.addScalar("actualCaseNumber", new StringType())
            sqlQuery.addScalar("actualProductName", new StringType())

            sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
            singleCaseAlertMap = sqlQuery.list()
        } catch (Exception ex) {
            log.error("Error came while single_case_and_single_archive_alert_list_sql executing: " + ex.getMessage())
        } finally {
            session.flush()
            session.clear()
        }

        Map scaData = [:]
        List<Map> result = singleCaseAlertMap.collect { alert ->
            scaData = alert
            scaData.isCaseNumberAvailable = (alert.caseNumber in [Constants.BlindingStatus.BLINDED, Constants.BlindingStatus.REDACTED]) ? false : true
            scaData.followUpNumber = scaData.followUpNumber ?: Constants.Commons.UNDEFINED_NUM
            scaData.alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
            Priority priority = cacheService.getPriorityByValue((Long) scaData.priorityId)
            scaData.priority = [value: priority?.value, iconClass: priority?.iconClass]
            scaData
        }
        result
    }

    ValidatedSignal saveValidatedSignal(Map<String, String> paramsMap) {
        ValidatedSignal validatedSignal = new ValidatedSignal([name                 : paramsMap.name, products: paramsMap.products,
                                                               reasonForEvaluation  : paramsMap.reasonForEvaluation,
                                                               events               : paramsMap.events,
                                                               lastDispChange       : new Date(),
                                                               signalStatus         : Constants.ONGOING_SIGNAL,
                                                               assignmentType       : paramsMap.assignmentType,
                                                               commentSignalStatus  : paramsMap.commentSignalStatus,
                                                               detectedBy           : paramsMap.detectedBy,
                                                               topic                : paramsMap.topic,
                                                               description          : paramsMap.description,
                                                               genericComment       : paramsMap.genericComment,
                                                               workflowState        : signalWorkflowService.defaultSignalWorkflowState(),
                                                               productGroupSelection: paramsMap.productGroupSelection != "[]" ? paramsMap.productGroupSelection : null,
                                                               eventGroupSelection  : paramsMap.eventGroupSelection != "[]" ? paramsMap.eventGroupSelection : null, therapeuticArea: paramsMap.therapeuticArea,dispPerformedBy: userService.getUser().getFullName(),isMultiIngredient: paramsMap.isMultiIngredient])
        try {
            if(Holders.config.signal.summary.dynamic.fields.find {it.fieldName == "UD_Text1"}?.enabled == true) {
                validatedSignal.udText1 = paramsMap.udText1 ? paramsMap.udText1: ""
            }

            if(Holders.config.signal.summary.dynamic.fields.find {it.fieldName == "UD_Text2"}?.enabled == true) {
                validatedSignal.udText2 = paramsMap.udText2 ? paramsMap.udText2: ""
            }

            if(Holders.config.signal.summary.dynamic.fields.find {it.fieldName == "UD_Dropdown1"}?.enabled == true) {
                if (paramsMap.udDropdown1 instanceof String[]) {
                    validatedSignal.udDropdown1 = paramsMap.udDropdown1.join(",")
                } else {
                    validatedSignal.udDropdown1 = paramsMap.udDropdown1
                }
                String[] list = paramsMap.udDropdown1 instanceof String ? [paramsMap.udDropdown1] : (String[]) paramsMap.udDropdown1
                String ddValue1 = Holders.config.signal.summary.dynamic.dropdown.values["UD_Dropdown1"]?.findAll {
                    list?.contains(it.key)
                }?.values()?.join(",")
                validatedSignal.ddValue1 = ddValue1 ? ddValue1: ""
            }

            if(Holders.config.signal.summary.dynamic.fields.find {it.fieldName == "UD_Dropdown2"}?.enabled == true) {
                if (paramsMap.udDropdown2 instanceof String[]) {
                    validatedSignal.udDropdown2 = paramsMap.udDropdown2.join(",")
                } else {
                    validatedSignal.udDropdown2 = paramsMap.udDropdown2
                }
                String[] list = paramsMap.udDropdown2 instanceof String ? [paramsMap.udDropdown2] : (String[]) paramsMap.udDropdown2
                String ddValue2 = Holders.config.signal.summary.dynamic.dropdown.values["UD_Dropdown2"]?.findAll {
                    list?.contains(it.key)
                }?.values()?.join(",")
                validatedSignal.ddValue2 = ddValue2 ? ddValue2: ""
            }
            Group workflowGroup = userService.getUser().workflowGroup
            setDatesForSignal(validatedSignal, paramsMap, workflowGroup)
            setNonStringFields(paramsMap, validatedSignal)
            bindTopicCategory(validatedSignal, paramsMap.signalTypeList)
            bindOutcomes(validatedSignal, paramsMap.signalOutcome)
            bindActionTaken(validatedSignal, paramsMap.actionTaken)
            bindEvaluationMethod(validatedSignal, paramsMap.evaluationMethod)
            bindInitialDataSource(validatedSignal, paramsMap.initialDataSource)
            validatedSignal.productsAndGroupCombination = alertService.productSelectionSignal(validatedSignal);
            validatedSignal.eventsAndGroupCombination = alertService.eventSelectionSignalWithSmq(validatedSignal);
            validatedSignal.workflowGroup = workflowGroup
            boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
            if (enableSignalWorkflow && signalWorkflowService.defaultSignalWorkflowState() == signalWorkflowService.calculateDueInSignalWorkflowState()) {
                validatedSignal.wsUpdated = new Date()
            }
            validatedSignal.disposition = workflowGroup?.defaultSignalDisposition
            assignAllEventAndProductValues(validatedSignal)
            String defaultValidatedDate = Holders.config.signal.defaultValidatedDate
            String createdDate = DateUtil.fromDateToString(validatedSignal.detectedDate, DateUtil.DEFAULT_DATE_FORMAT)
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
            if (validateSignal(validatedSignal) && validatedSignal.detectedDate>=sdf.parse('01-Jan-1900')) {
                saveSignal(validatedSignal, true)
                bindLinkedSignals(validatedSignal, paramsMap.linkedSignal)
                activityService.createActivityForSignal(validatedSignal, null, "Signal '$paramsMap.name' created", ActivityType.findByValue(ActivityTypeValue.SignalCreated), validatedSignal.assignedTo, userService.getUser(), null, validatedSignal.assignedToGroup)
                List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions;
                if (validatedDateDispositions.contains(validatedSignal.disposition.value)) {
                    saveSignalStatusHistory([signalStatus: (defaultValidatedDate) ? defaultValidatedDate : Constants.WorkFlowLog.VALIDATION_DATE, statusComment: Constants.WorkFlowLog.VALIDATION_STATUS_COMMENT,
                                             signalId    : validatedSignal.id, "createdDate": createdDate, "isSystemUser": true], true)
                } else {
                    saveSignalStatusHistory([signalStatus: Constants.SignalHistory.SIGNAL_CREATED, statusComment: Constants.SignalHistory.SIGNAL_CREATED,
                                             signalId    : validatedSignal.id], false);
                }
                Integer dueIn
                String previousDueDate = DateUtil.fromDateToString(validatedSignal.actualDueDate, DEFAULT_DATE_FORMAT)
                boolean dueInStartEnabled = Holders.config.dueInStart.enabled
                String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
                if (enableSignalWorkflow) {
                    dueIn = dueInStartEnabled ? calculateDueIn(validatedSignal.id, dueInStartPoint) : calculateDueIn(validatedSignal.id, validatedSignal.workflowState)
                } else {
                    dueIn = dueInStartEnabled ? calculateDueIn(validatedSignal.id, dueInStartPoint) : calculateDueIn(validatedSignal.id, defaultValidatedDate)
                }
                if (!dueInStartEnabled && SystemConfig.first().displayDueIn && !previousDueDate.equals(validatedSignal.actualDueDate)) {
                    saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                             signalId    : validatedSignal.id, "createdDate": previousDueDate], true)
                }
                if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.SIGNAL_CREATION)) {
                    emailNotificationService.mailHandlerForSignalCreation(validatedSignal)
                }
                if (Holders.config.detectedDateAndValidationDate.synch.enabled) {
                    List<SignalStatusHistory> signalStatusHistories = validatedSignal.getSignalStatusHistories().findAll()
                    signalStatusHistories.each {
                        if (it.signalStatus.contains(Constants.WorkFlowLog.VALIDATION_DATE)) {
                            it.dateCreated = validatedSignal.detectedDate
                            it.isSystemUser = true
                        }
                    }
                }
            }
        } catch (ValidationException validationException) {
            validationException.printStackTrace()
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            boolean isProductOrFamilyOrIngredient = allowedDictionarySelection(validatedSignal)
            if (!isProductOrFamilyOrIngredient) {
                validatedSignal.errors.rejectValue("version", null, messageSource.getMessage("app.label.product.family.error.message", null, Locale.default))
            }
        }
        validatedSignal
    }

    Boolean validateDetectedDateLimit(String detectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
        (sdf.parse(detectedDate) >= sdf.parse('01-Jan-1900'))
    }

    List<String> getAllProductsWithHierarchy(ValidatedSignal validatedSignal){
        JsonSlurper jsonSlurper = new JsonSlurper()
        List<String> productList = []
        List indexList = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
        String userName = ""
        if(springSecurityService.principal?.username){
            userName = springSecurityService.principal?.username
        }else{
            userName = 'admin'
        }
        if (validatedSignal.productGroupSelection) {
            List productGroupList = jsonSlurper.parseText(validatedSignal.productGroupSelection) as List
            productGroupList.each { pg ->
                Map resultMap = [:]
                def response = fetchGroupDetails(pg.id as Long, userName, true)
                resultMap = response?.properties
                if (resultMap) {
                    List dataSourcesName = resultMap.dataSourceNames as List
                    def finalOutput
                    if(resultMap?.data){
                        finalOutput = jsonSlurper.parseText(resultMap?.data as String)
                    }
                    dataSourcesName?.each { it ->
                        indexList.each { ind ->
                            finalOutput?.get(it)?.get(ind)?.each { product ->
                                String productHierarchyComb = "" + ind + "-" + product.name + ""
                                productList.add(productHierarchyComb)
                            }
                        }
                    }
                }
            }

        }
        if(validatedSignal.products) {
            Map parsedProducts = jsonSlurper.parseText(validatedSignal.products) as Map
            if(parsedProducts) {
                indexList.each { ind ->
                    parsedProducts?.get(ind)?.each { product ->
                        String productHierarchyComb = "" + ind + "-" + product.name + ""
                        productList.add(productHierarchyComb)
                    }
                }
            }
        }
        return productList
    }

    Map<String,List<String>> getAllEventsWithHierarchy(ValidatedSignal validatedSignal){
        Map allEventMap
        JsonSlurper jsonSlurper = new JsonSlurper()
        List<String> eventListHierarchy = []
        List<String> eventList = []
        Map smqMap = [7: Constants.EventFields.BROAD, 8: Constants.EventFields.NARROW]
        List indexList = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
        String userName = ""
        if(springSecurityService.principal?.username){
            userName = springSecurityService.principal?.username
        }else{
            userName = 'admin'
        }
        if (validatedSignal.eventGroupSelection) {
            List eventGroupList = jsonSlurper.parseText(validatedSignal.eventGroupSelection) as List
            Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String sql_statement = ""
            eventGroupList.each { pg ->
                Map resultMap = [:]
                def response = fetchGroupDetails(pg.id as Long, userName, true)
                resultMap = response?.properties
                if (resultMap) {
                    try {
                        List dataSourcesName = resultMap.dataSourceNames as List
                        def finalOutput
                        if(resultMap?.data){
                            finalOutput = jsonSlurper.parseText(resultMap?.data as String)
                        }
                        dataSourcesName?.each { it ->
                            indexList.each { ind ->
                                finalOutput?.get(it)?.get(ind)?.each { product ->
                                    if (ind == '7' || ind == '8') {
                                        String eventHierarchyCombSMQ = "'" + product?.name + "" + smqMap.get((ind as Integer)) + "'"
                                        eventListHierarchy.add(eventHierarchyCombSMQ)
                                        Long smqId = product.id as Long
                                        Long termScope
                                        if (ind == '7') {
                                            termScope = 1
                                        } else if (ind == '8') {
                                            termScope = 2
                                        }
                                        sql_statement = SignalQueryHelper.fetch_pt_from_smq(smqId, termScope)
                                        sql.eachRow(sql_statement) { row ->
                                            if (row) {
                                                String eventComb = "'" + row[0] + "'"
                                                eventList.add(eventComb)
                                            }
                                        }
                                    } else if (ind == '1') {
                                        Long socCode = product.id as Long
                                        sql_statement = SignalQueryHelper.fetch_pt_from_soc(socCode)
                                        sql.eachRow(sql_statement) { row ->
                                            if (row) {
                                                String eventComb = "'" + row[0] + "'"
                                                eventList.add(eventComb)
                                            }
                                        }
                                    } else if (ind == '2') {
                                        Long hlgtCode = product.id as Long
                                        sql_statement = SignalQueryHelper.fetch_pt_from_hlgt(hlgtCode)
                                        sql.eachRow(sql_statement) { row ->
                                            if (row) {
                                                String eventComb = "'" + row[0] + "'"
                                                eventList.add(eventComb)
                                            }
                                        }
                                    } else if (ind == '3') {
                                        Long hltCode = product.id as Long
                                        sql_statement = SignalQueryHelper.fetch_pt_from_hlt(hltCode)
                                        sql.eachRow(sql_statement) { row ->
                                            if (row) {
                                                String eventComb = "'" + row[0] + "'"
                                                eventList.add(eventComb)
                                            }
                                        }
                                    } else {
                                        String eventComb = "'" + product.name + "'"
                                        eventList.add(eventComb)
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error occoured in fetching events : ",ex)
                    } finally {
                        sql.close()
                    }
                }
            }
        }
        if (validatedSignal.events) {
            Map parsedEvents = jsonSlurper.parseText(validatedSignal.events) as Map
            Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String sql_statement = ""
            try {
                if (parsedEvents) {
                    indexList.each { ind ->
                        parsedEvents?.get(ind)?.each { product ->
                            if (ind == '7' || ind == '8') {
                                String eventHierarchyCombSMQ = "'" + product?.name + "" + smqMap.get((ind as Integer)) + "'"
                                eventListHierarchy.add(eventHierarchyCombSMQ)
                                Long smqId = product.id as Long
                                Long termScope
                                if (ind == '7') {
                                    termScope = 1
                                } else if (ind == '8') {
                                    termScope = 2
                                }
                                sql_statement = SignalQueryHelper.fetch_pt_from_smq(smqId, termScope)
                                sql.eachRow(sql_statement) { row ->
                                    if (row) {
                                        String eventComb = "'" + row[0] + "'"
                                        eventList.add(eventComb)
                                    }
                                }
                            } else if (ind == '1') {
                                Long socCode = product.id as Long
                                sql_statement = SignalQueryHelper.fetch_pt_from_soc(socCode)
                                sql.eachRow(sql_statement) { row ->
                                    if (row) {
                                        String eventComb = "'" + row[0] + "'"
                                        eventList.add(eventComb)
                                    }
                                }
                            } else if (ind == '2') {
                                Long hlgtCode = product.id as Long
                                sql_statement = SignalQueryHelper.fetch_pt_from_hlgt(hlgtCode)
                                sql.eachRow(sql_statement) { row ->
                                    if (row) {
                                        String eventComb = "'" + row[0] + "'"
                                        eventList.add(eventComb)
                                    }
                                }
                            } else if (ind == '3') {
                                Long hltCode = product.id as Long
                                sql_statement = SignalQueryHelper.fetch_pt_from_hlt(hltCode)
                                sql.eachRow(sql_statement) { row ->
                                    if (row) {
                                        String eventComb = "'" + row[0] + "'"
                                        eventList.add(eventComb)
                                    }
                                }
                            } else {
                                String eventHierarchyComb = "'" + ind + "-" + product.name + "'"
                                eventListHierarchy.add(eventHierarchyComb)
                                String eventComb = "'" + product.name + "'"
                                eventList.add(eventComb)
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("Error occoured in fetching events : ",ex)
            } finally {
                sql.close()
            }
        }
        allEventMap = [eventListHierarchy: eventListHierarchy, eventList: eventList]
        return allEventMap
    }

    DictionaryGroupCmd fetchGroupDetails(Long id, String userName, boolean withData = false) {
        String url = grailsApplication.config.app.dictionary.base.url
        String path = grailsApplication.config.app.dictionary.group.details.api
        Map response = restAPIService.get(url, path, [id: id, userName:userName, withData: withData])
        if(response.data){
            return new DictionaryGroupCmd(response.data as Map)
        }else{
            return null
        }
    }

    def getEventFromJsonWithSmq(def jsonString) {
        Map smqMap = [7: Constants.EventFields.BROAD, 8: Constants.EventFields.NARROW]
        def jsonObj = null
        List prdList = []
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
                def prdVal = jsonObj.findAll {k,v->
                    v.find { it.containsKey('name') || it.containsKey('genericName')}
                }
                prdVal.each {k,v->
                    if((k as Integer) in [7,8]){
                        v.each {
                            prdList.add("'" + it?.name + "" + smqMap.get((k as Integer)) + "'")
                        }
                    }
                }
                prdList = prdList?.sort()
        }
        prdList
    }

    Boolean validateSignal(ValidatedSignal vs, ResponseDTO responseDTO = null) {
        Boolean isValidated = false
        String username = userService.user?.username
        if (!vs.createdBy)
            vs.createdBy = username
        if (!vs.modifiedBy)
            vs.modifiedBy = username
        if (validateRequiredFields(vs)) {
            if (vs.validate()) {
                if (validateDetectedDate(toDateStringWithTime(vs.detectedDate))) {
                    isValidated = true
                    if (responseDTO != null) {
                        responseDTO.data = messageSource.getMessage("app.label.signal.information.updated.successfully", null, Locale.default)
                        responseDTO.status = true
                    }
                } else {
                    if (responseDTO != null) {
                        responseDTO.data = messageSource.getMessage("detectedDate.invalid", null, Locale.default)
                        responseDTO.status = false
                    }
                }
            } else {
                isValidated = false
                if (responseDTO != null) {
                    if (vs.errors.hasFieldErrors('name')) {
                        if (vs.errors.getFieldError('name').getCode().contains('unique'))
                            responseDTO.data = messageSource.getMessage("com.rxlogix.signal.name.is.unique", null, Locale.default)
                        else if (vs.errors.getFieldError('name').getCode().contains('maxSize'))
                            responseDTO.data = messageSource.getMessage("com.rxlogix.signal.ValidatedSignal.name.maxSize.exceeded", null, Locale.default)

                                if (vs.errors.getFieldError('name').getCode().contains('special.character')) {
                                    String value= vs.getName()
                                    String[] chars = Constants.SpecialCharacters.DEFAULT_CHARS as String[]
                                    if (!chars) {
                                        chars = Constants.SpecialCharacters.DEFAULT_CHARS;
                                    }
                                    def invalidSpecialCharacters = value?.findAll { a ->
                                        chars?.any { a.contains(it) }
                                    }?.unique()
                                    responseDTO.data=[String.join(" , ", invalidSpecialCharacters) + " special character" + (invalidSpecialCharacters?.size() > 1 ? "s are" : " is") + " not allowed in " + "Signal Name"]
                                }

                    } else if (vs.errors.hasFieldErrors('topic') && vs.errors.getFieldError('topic').getCode().contains('maxSize')) {
                        responseDTO.data = messageSource.getMessage("com.rxlogix.signal.ValidatedSignal.topic.maxSize.exceeded", null, Locale.default)
                    } else if (vs.errors.hasFieldErrors('detectedDate')) {
                        responseDTO.data = messageSource.getMessage("com.rxlogix.signal.ValidatedSignal.topic.maxSize.exceeded", null, Locale.default)
                    } else if(vs.errors.hasFieldErrors('udText1') || vs.errors.hasFieldErrors('udText2')){
                        responseDTO.data = messageSource.getMessage("com.rxlogix.signal.ValidatedSignal.udText.maxSize.exceeded", null, Locale.default)
                    }
                    responseDTO.status = false
                }
            }
        } else {
            isValidated = false
            if (responseDTO != null && vs?.id) {
                vs.validate()
                responseDTO.status = false
                responseDTO.data = messageSource.getMessage("validated.signal.error.message.all.fields.required", null, Locale.default)
            }
        }
        isValidated
    }

    /**
     * Validates if the provided detected date is before or equal to the current date.
     *
     * @param newDetectedDate The detected date as a string, typically in a format that can be parsed into a Date object.
     * @return {@code true} if the detected date is less than or equal to the current date;
     *         {@code false} otherwise.
     */
    Boolean validateDetectedDate(String newDetectedDate) {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        Date currentDate = new Date(toDateStringWithTime(new Date(), userTimezone))
        Date detectedDate = new Date(newDetectedDate)
        return detectedDate <= currentDate
    }

    Boolean validateRequiredFields(ValidatedSignal vs) {
        //updated change for story PVS-38882
        vs.name && (vs.products || vs.productGroupSelection) && vs.detectedDate && vs.initialDataSource
    }

    Boolean requiredFieldsPresent(Map paramsMap) {
        //updated change for story PVS-38882
        paramsMap['name'] && paramsMap['detectedDate'] && paramsMap['initialDataSource'] && (paramsMap['productSelection'] || paramsMap['productGroupSelection'])
    }

    private void setNonStringFields(Map<String, String> paramsMap, ValidatedSignal validatedSignal) {
        validatedSignal.priority = Priority.findByDefaultPriority(true)
        validatedSignal.assignedTo = userService.getUser()
        if (!TextUtils.isEmpty(paramsMap.priority)) {
            validatedSignal.priority = Priority.get(Long.parseLong(paramsMap.priority))
        }
        if (!TextUtils.isEmpty(paramsMap.signalStrategy)) {
            validatedSignal.strategy = SignalStrategy.get(Long.parseLong(paramsMap.signalStrategy))
        }
        if (!TextUtils.isEmpty(paramsMap.assignedToValue)) {
            validatedSignal = userService.assignGroupOrAssignTo(paramsMap.assignedToValue, validatedSignal)
        }
        if (!TextUtils.isEmpty(paramsMap.actionTemplate)) {
            validatedSignal.actionTemplate = ActionTemplate.get(Long.parseLong(paramsMap.actionTemplate))
        }
        if (!TextUtils.isEmpty(paramsMap.haSignalStatus)) {
            validatedSignal.haSignalStatus = Disposition.get(Long.parseLong(paramsMap.haSignalStatus))
        }
        if (paramsMap.sharedWith) {
            userService.bindSharedWithConfiguration(validatedSignal, paramsMap.sharedWith, false)
        }
    }

     Map prepareCreateMap(ValidatedSignal validatedSignal = null) {
        if (!validatedSignal) {
            validatedSignal = new ValidatedSignal()
            Group allUsers = Group.findByName('All Users')
            if (Holders.config.validatedSignal.shareWith.enabled && allUsers) {
                validatedSignal.addToShareWithGroup(allUsers)
            }
        }
        User currentUser = userService.user
        String timezone = currentUser?.preference?.timeZone
        def initialDataSource = alertAttributesService.get('initialDataSource')
        def evaluationMethods = alertAttributesService.get('evaluationMethods')
        def detectedBy = alertAttributesService.get('detectedBy')
         List<TopicCategory> signalTypeList = TopicCategory.list().unique({ it.name })
         List<Map<String, String>> haSignalStatusList = Disposition.list().sort({
            it.value.toUpperCase()
        }).collect {
            [id: it.id, value: it.value]
        }
        List<Map<String, String>> priorityList = Priority.findAllByDisplay(true).collect {
            [id: it.id, displayName: it.displayName]
        }

        List<Map<String, String>> userList = User.findAllByEnabled(true).collect {
            [id: it.id, fullName: it.fullName]
        }
        List<String> actionTakenList = alertAttributesService.get('actionsTaken')
         actionTakenList?.removeAll([null])

        List<Map<Long, String>> linkedSignals = ValidatedSignal.findAllByWorkflowGroup(currentUser.workflowGroup)?.collect {
            String productName = it.productGroupSelection ? getGroupNameFieldFromJson(it.productGroupSelection) : getAllProductNameFieldFromJson(it.products)
            String signalName = it.name + " (" + productName + ")"
            [id: it.id, name: signalName]
        }
        if (validatedSignal.id) {
            linkedSignals.removeAll { it.id == validatedSignal.id }
        }

        List<SignalOutcome> signalOutcomes = SignalOutcome.list()

        [validatedSignal   : validatedSignal, initialDataSource: initialDataSource, signalTypeList: signalTypeList, signalOutcomes: signalOutcomes.name,
         haSignalStatusList: haSignalStatusList, priorityList: priorityList, userList: userList, linkedSignals: linkedSignals, timezone: timezone,
         actionTakenList   : actionTakenList, evaluationMethods: evaluationMethods, detectedBy: detectedBy,dataSourceMap     : getDataSourceMap(), udText1: validatedSignal.udText1, udText2: validatedSignal.udText2]
    }

    private Map editMap(String signalId) {
        ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(signalId))
        Map editMap = prepareCreateMap(validatedSignal)
        editMap
    }

    private void bindTopicCategory(ValidatedSignal validatedSignal, def signalTypeList) {

        if (signalTypeList) {
            if (validatedSignal?.topicCategories) {
                validatedSignal.topicCategories.clear()
            }
            if (signalTypeList.getClass() == String) {
                signalTypeList = [signalTypeList]
            }

            Set<String> topicCategoryList = signalTypeList.toList() as Set

            topicCategoryList.each {
                TopicCategory topicCategory = TopicCategory.findByName("" + it)
                if (topicCategory) {
                    validatedSignal.addToTopicCategories(topicCategory)
                }
            }
        }
    }

    private void bindActionTaken(ValidatedSignal validatedSignal, def actionTaken) {

        validatedSignal?.actionTaken?.clear()
        if (actionTaken.toString() != "null") {
            if (actionTaken instanceof String) {
                actionTaken = [actionTaken]
            }
            actionTaken?.each {
                validatedSignal.addToActionTaken(it.toString())
            }
        }
    }

    private void bindEvaluationMethod(ValidatedSignal validatedSignal, def evaluationMethods) {

        validatedSignal?.evaluationMethod?.clear()
        if (evaluationMethods.toString() != "null") {
            if (evaluationMethods instanceof String) {
                evaluationMethods = [evaluationMethods]
            }
            evaluationMethods?.each {
                validatedSignal.addToEvaluationMethod(it.toString())
            }
        }
    }

    private void bindInitialDataSource(ValidatedSignal validatedSignal, def initialDataSources) {
        List<String> initDataSources = []
        if(initialDataSources.getClass() == String) {
            initDataSources.add(initialDataSources)
        } else{
            initDataSources = initialDataSources as List<String>
        }
        validatedSignal.initialDataSource = initDataSources?.join("##")
    }

    void bindLinkedSignals(ValidatedSignal validatedSignal, def linkedSignals) {
        if (linkedSignals) {
            if (validatedSignal?.linkedSignals) {
                validatedSignal.linkedSignals.clear()
            }
            if (linkedSignals.getClass() == String) {
                linkedSignals = [linkedSignals]
            }

            linkedSignals?.collect { it }.each {
                ValidatedSignal linkedSignal = ValidatedSignal.get(it)
                if (linkedSignal) {
                    validatedSignal.addToLinkedSignals(linkedSignal)
                    linkedSignal.addToLinkedSignals(validatedSignal)
                }
            }
        }
    }

    void bindOutcomes(ValidatedSignal validatedSignal, def signalOutcomes) {
        if (signalOutcomes) {
            if (validatedSignal?.signalOutcomes) {
                validatedSignal.signalOutcomes.clear()
            }
            if (signalOutcomes.getClass() == String) {
                signalOutcomes = [signalOutcomes]
            }

            signalOutcomes.each {
                SignalOutcome signalOutcome = SignalOutcome.findByName(it)
                if (signalOutcome) {
                    validatedSignal.addToSignalOutcomes(signalOutcome)
                }
            }
        }
    }

    private void setDatesForSignal(ValidatedSignal validatedSignal, Map<String, String> paramsMap, Group workflowGroup) {
        if (!TextUtils.isEmpty(paramsMap.detectedDate)) {
            validatedSignal.detectedDate =DateUtil.parseDate(paramsMap.detectedDate, DEFAULT_DATE_FORMAT)
        }
        if (!TextUtils.isEmpty(paramsMap.aggReportStartDate)) {
            validatedSignal.aggReportStartDate = DateUtil.stringToDate(paramsMap.aggReportStartDate, DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
        }
        if (!TextUtils.isEmpty(paramsMap.aggReportEndDate)) {
            validatedSignal.aggReportEndDate = DateUtil.stringToDate(paramsMap.aggReportEndDate, DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
        }
        if (!TextUtils.isEmpty(paramsMap.lastDecisionDate)) {
            validatedSignal.lastDecisionDate = DateUtil.stringToDate(paramsMap.lastDecisionDate, "MM/dd/yyyy", Holders.config.server.timezone)
        }
        if (!TextUtils.isEmpty(paramsMap.haDateClosed)) {
            validatedSignal.haDateClosed = DateUtil.stringToDate(paramsMap.haDateClosed, DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
        }

        if(Holders.config.signal.summary.dynamic.fields.find {it.fieldName == "UD_Date1"}?.enabled == true) {
            validatedSignal.udDate1 = paramsMap.udDate1 ? DateUtil.stringToDate(paramsMap.udDate1, DEFAULT_DATE_FORMAT,  Holders.config.server.timezone) : null

        }

        if(Holders.config.signal.summary.dynamic.fields.find {it.fieldName == "UD_Date2"}?.enabled == true) {
            validatedSignal.udDate2 = paramsMap.udDate2 ? DateUtil.stringToDate(paramsMap.udDate2, DEFAULT_DATE_FORMAT,  Holders.config.server.timezone) : null
        }


    }

    ResponseDTO updateSignal(Map<String, String> params) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        ValidatedSignal validatedSignal = ValidatedSignal.findById(Long.parseLong(params.signalId))
        Date signalDetectedDate = validatedSignal.detectedDate
        String oldDetectedDate = DateUtil.toDateString(signalDetectedDate)
        String newDetectedDate = params.detectedDate
        boolean updateDetectedDateActivity = false
        if (oldDetectedDate != newDetectedDate) {
            updateDetectedDateActivity = true
        }
        if(params.name!=null && params.name.trim()==''){
            params.name = null
        }

        StringBuilder details = new StringBuilder()
        if(newDetectedDate && !validateDetectedDateLimit(newDetectedDate)){
            responseDTO.status = false
            responseDTO.data = messageSource.getMessage("validated.signal.detectedDateError.message", null, Locale.default)
        } else if (newDetectedDate && !validateDetectedDate(newDetectedDate)) {
            responseDTO.status = false
            responseDTO.data = messageSource.getMessage("detectedDate.invalid", null, Locale.default)
        } else if (!requiredFieldsPresent(params)){
            responseDTO.status = false
            responseDTO.data = messageSource.getMessage("validated.signal.error.message.all.fields.required", null, Locale.default)
        } else if(!params.sharedWith && Holders.config.validatedSignal.shareWith.enabled){
            responseDTO.status = false
            responseDTO.data = messageSource.getMessage("validated.signal.error.shareWith.required", null, Locale.default)
        }
        else{
            try {
                validatedSignal.isMultiIngredient = Boolean.parseBoolean(params.isMultiIngredient)
                setUpdatedValuesForStringFields(validatedSignal, params, details)
                setUpdatedDatesForSignal(validatedSignal, params, details)
                setUpdatedValuesForNonStringFields(params, validatedSignal, details)
                editTopicCategory(validatedSignal, params.signalTypeList, details)
                editActionTaken(validatedSignal, params.actionTaken, details)
                editEvaluationMethod(validatedSignal, params.evaluationMethod, details)
                editLinkedSignals(validatedSignal, params.linkedSignal, details)
                editSignalOutcome(validatedSignal, params.signalOutcome, details)
                validateAndUpdateSignal(validatedSignal, responseDTO, details)
                setMileStoneCompletionDateForSignal(validatedSignal)
                setDueDateForSignal(validatedSignal)
                String previousDueDate=DateUtil.fromDateToString(validatedSignal.actualDueDate,DEFAULT_DATE_FORMAT)
                responseDTO.value = calculateDueIn(validatedSignal.id, validatedSignal.workflowState, false, false, false, false, updateDetectedDateActivity, true)

                if(SystemConfig.first().displayDueIn && responseDTO.value!=null){
                    saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                             signalId    : validatedSignal.id, "createdDate": previousDueDate], true, false, false, false, updateDetectedDateActivity, true)
                }
                List<SignalStatusHistory> signalStatusHistoryList = validatedSignal.signalStatusHistories
                Set<String> distinctSignalStatus = signalStatusHistoryList.collect { it.signalStatus }.unique()

                if (Holders.config.detectedDateAndValidationDate.synch.enabled && updateDetectedDateActivity && params.detectedDate && distinctSignalStatus.contains(Holders.config.signal.defaultValidatedDate)) {
                    String details1="Detected Date is updated with the '${Holders.config.signal.defaultValidatedDate}', “ Detected Date changed from '"+new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(signalDetectedDate)+"' to '"+DateUtil.fromDateToString(new Date(params.detectedDate), DEFAULT_DATE_FORMAT)+"'“"
                    saveActivityForSignalHistory(userService?.getUser()?.username ,validatedSignal, details1)
                }
                else if(Holders.config.detectedDateAndValidationDate.synch.enabled && updateDetectedDateActivity && params.detectedDate){
                    String details1="Detected Date changed from '"+new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(signalDetectedDate)+"' to '"+DateUtil.fromDateToString(new Date(params.detectedDate), DEFAULT_DATE_FORMAT)+"'"
                    saveActivityForSignalHistory(userService?.getUser()?.username ,validatedSignal, details1)
                }
            } catch (ValidationException validationException) {
                validationException.printStackTrace()

                if (!validateRequiredFields(validatedSignal) && org.apache.commons.lang3.StringUtils.isNotBlank(params.name)) {
                    def notAllowedChar = MiscUtil.validator(params.name, "Signal Name", (Constants.SpecialCharacters.DEFAULT_CHARS - ["#"]) as String[])
                    if (Objects.nonNull(notAllowedChar) && notAllowedChar instanceof List) {
                        responseDTO.data = notAllowedChar[1] + " and " + messageSource.getMessage("validated.signal.error.message.all.fields.required", null, Locale.default)
                    }
                }

                if(new Date(newDetectedDate) > new Date()){
                    responseDTO.data = messageSource.getMessage("detectedDate.invalid", null, Locale.default)

                }
            } catch (Exception e) {
                responseDTO.status = false
                responseDTO.data = [messageSource.getMessage("app.label.signal.information.updated.error", null, Locale.default)]
                e.printStackTrace()
            } finally {
                boolean isProductOrFamilyOrIngredient = allowedDictionarySelection(validatedSignal)
                if (!isProductOrFamilyOrIngredient) {
                    responseDTO.data << messageSource.getMessage("com.rxlogix.signal.ValidatedSignal.products.customError", null, Locale.default)
                }
                if(responseDTO.data){
                    responseDTO.data=[dueDate:validatedSignal.actualDueDate?new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(validatedSignal.actualDueDate):"",message:responseDTO.data]

                }
                return responseDTO
            }
        }
        return responseDTO
    }

    boolean updateSignalOutcome(Map<String, String> params) {
        boolean status = false;
        try {
            ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(params.id))
            SignalOutcome outcome=SignalOutcome.findByName(params.outcome);
            if(!validatedSignal.signalOutcomes.contains(outcome)){
                validatedSignal.signalOutcomes.add(outcome)
                validatedSignal.isSystemUser = true
                validatedSignal.save(flush: true)
                StringBuilder details=new StringBuilder()
                details.append(messageSource.getMessage("signal.updated.values", ["Signal Outcome", " ", outcome.name, validatedSignal.name] as Object[], Locale.default))
                if (!TextUtils.isEmpty(details)) {
                    activityService.createActivityForSignal(validatedSignal, '', details.toString(), ActivityType.findByValue(ActivityTypeValue.SignalUpdated),
                            validatedSignal.assignedTo, User.findByUsername(Constants.SYSTEM_USER), [:], validatedSignal.assignedToGroup)
                }
                status = true
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        return status;
    }

    private void validateAndUpdateSignal(ValidatedSignal validatedSignal, ResponseDTO responseDTO, StringBuilder details){
        if (validateSignal(validatedSignal, responseDTO)) {
            saveSignal(validatedSignal, false)
            if(Holders.config.detectedDateAndValidationDate.synch.enabled)
            {
                List<SignalStatusHistory> signalStatusHistories=validatedSignal.getSignalStatusHistories().findAll()
                signalStatusHistories.each {
                    if(it.signalStatus.contains(Holders.config.signal.defaultValidatedDate)){
                        it.dateCreated=validatedSignal.detectedDate
                        it.isSystemUser = true
                        it.save(flush:true)
                        String updateDateCreated = "Update SignalStatusHistory set dateCreated = :dateCreated where id = :id"
                        SignalStatusHistory.executeUpdate(updateDateCreated, [dateCreated: validatedSignal.detectedDate, id: it.id])

                    }
                }
           }
            if (!TextUtils.isEmpty(details)) {
                activityService.createActivityForSignal(validatedSignal, '', details.toString(), ActivityType.findByValue(ActivityTypeValue.SignalUpdated),
                        validatedSignal.assignedTo, userService.getUser(), [:], validatedSignal.assignedToGroup)
            }
        } else {
            validatedSignal.discard()
        }
    }

    private void setUpdatedDatesForSignal(ValidatedSignal validatedSignal, Map<String, String> params, StringBuilder details) {

        Date newUpdatedDate = null
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT)
        String prevDate = validatedSignal.detectedDate ? sdf.format(validatedSignal.detectedDate): null
        String newDate = !TextUtils.isEmpty(params.detectedDate) ? params.detectedDate : null
        if (prevDate != newDate) {
            newUpdatedDate = newDate ? DateUtil.parseDate(params.detectedDate, DEFAULT_DATE_FORMAT) : null
            if(!Holders.config.detectedDateAndValidationDate.synch.enabled)
            {
                details.append(messageSource.getMessage("signal.updated.values", ["Detected Date", prevDate ?: " ", newDate ?: " ", validatedSignal.name] as Object[], Locale.default))
            }
            validatedSignal.detectedDate = newUpdatedDate
        }


        prevDate = validatedSignal.aggReportStartDate ? sdf.format(validatedSignal.aggReportStartDate) : null
        newDate = !TextUtils.isEmpty(params.aggReportStartDate) ? params.aggReportStartDate : null
        if (prevDate != newDate) {
            newUpdatedDate = newDate ? DateUtil.stringToDate(params.aggReportStartDate, DEFAULT_DATE_FORMAT, Holders.config.server.timezone) : null
            details.append(messageSource.getMessage("signal.updated.values", ["Aggregate Report Start Date", prevDate ?: " ", newDate ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.aggReportStartDate = newUpdatedDate
        }

        prevDate = validatedSignal.aggReportEndDate ? sdf.format(validatedSignal.aggReportEndDate) : null
        newDate = !TextUtils.isEmpty(params.aggReportEndDate) ? params.aggReportEndDate : null
        if (prevDate != newDate) {
            newUpdatedDate = newDate ? DateUtil.stringToDate(params.aggReportEndDate, DEFAULT_DATE_FORMAT, Holders.config.server.timezone) : null
            details.append(messageSource.getMessage("signal.updated.values", ["Aggregate Report End Date", prevDate ?: " ", newDate ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.aggReportEndDate = newUpdatedDate
        }
        SimpleDateFormat otherSdf = new SimpleDateFormat("MM/dd/yyyy")
        prevDate = validatedSignal.lastDecisionDate ? otherSdf.format(validatedSignal.lastDecisionDate) : null
        newDate = !TextUtils.isEmpty(params.lastDecisionDate) ? params.lastDecisionDate : null

        if (prevDate != newDate) {
            newUpdatedDate = newDate ? DateUtil.stringToDate(params.lastDecisionDate, "MM/dd/yyyy", Holders.config.server.timezone) : null
            details.append(messageSource.getMessage("signal.updated.values", ["Last Decision Date", prevDate ?: " ", newDate ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.lastDecisionDate = newUpdatedDate
        }

        prevDate = validatedSignal.haDateClosed ? validatedSignal.haDateClosed.format(DEFAULT_DATE_FORMAT) : null
        newDate = !TextUtils.isEmpty(params.haDateClosed) ? params.haDateClosed : null
        if (prevDate != newDate) {
            newUpdatedDate = newDate ? DateUtil.stringToDate(params.haDateClosed, DEFAULT_DATE_FORMAT, Holders.config.server.timezone) : null
            details.append(messageSource.getMessage("signal.updated.values", ["HA Date Closed", prevDate ?: " ", newDate ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.haDateClosed = newUpdatedDate
        }

    }

    private void setUpdatedValuesForStringFields(ValidatedSignal validatedSignal, Map<String, String> params, StringBuilder details) {
        params.name = !TextUtils.isEmpty(params.name) ? params.name?.trim() : null
        if (validatedSignal.name != params.name) {
            details.append(messageSource.getMessage("signal.updated.values", ["Name", validatedSignal.name, params.name, validatedSignal.name] as Object[], Locale.default))
            validatedSignal.name = params.name
        }
        params.topic = !TextUtils.isEmpty(params.topic) ? params.topic : null
        if (validatedSignal.topic != params.topic) {
            details.append(messageSource.getMessage("signal.updated.values", ["Topic Information", validatedSignal.topic ?: " ", params.topic ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.topic = params.topic
        }
        changeInProduct(validatedSignal.name, validatedSignal.products, params.productSelection, details,validatedSignal.productGroupSelection,params.productGroupSelection)
        validatedSignal.products = params.productSelection
        if(params.productGroupSelection != '[]'){
            validatedSignal.productGroupSelection = params.productGroupSelection
        } else {
            validatedSignal.productGroupSelection = null
        }
        validatedSignal.productsAndGroupCombination = alertService.productSelectionSignal(validatedSignal);

        changeInEvents(validatedSignal.name, validatedSignal.events, params.eventSelection, details,validatedSignal.eventGroupSelection, params.eventGroupSelection)
        validatedSignal.events = params.eventSelection
        if(params.eventGroupSelection != '[]'){
            validatedSignal.eventGroupSelection = params.eventGroupSelection
        } else {
            validatedSignal.eventGroupSelection = null
        }
        validatedSignal.eventsAndGroupCombination = alertService.eventSelectionSignalWithSmq(validatedSignal);

        assignAllEventAndProductValues(validatedSignal)

        params.reasonForEvaluation = !TextUtils.isEmpty(params.reasonForEvaluation) ? params.reasonForEvaluation : null
        if (validatedSignal.reasonForEvaluation != params.reasonForEvaluation) {
            details.append(messageSource.getMessage("signal.updated.values", ["Reason for Evaluation & Summary of Key Data", validatedSignal.reasonForEvaluation ?: " ", params.reasonForEvaluation ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.reasonForEvaluation = params.reasonForEvaluation
        }

        params.detectedBy = !TextUtils.isEmpty(params.detectedBy) ? params.detectedBy : null
        if (validatedSignal.detectedBy != params.detectedBy) {
            details.append(messageSource.getMessage("signal.updated.values", ["Detected By", validatedSignal.detectedBy ?: " ", params.detectedBy ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.detectedBy = params.detectedBy
        }

        params.description = !TextUtils.isEmpty(params.description) ? params.description : null
        if (validatedSignal.description != params.description) {
            details.append(messageSource.getMessage("signal.updated.values", ["Description", validatedSignal.description ?: " ", params.description ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.description = params.description
        }

        params.commentSignalStatus = !TextUtils.isEmpty(params.commentSignalStatus) ? params.commentSignalStatus : null
        if (validatedSignal.commentSignalStatus != params.commentSignalStatus) {
            details.append(messageSource.getMessage("signal.updated.values", ["Comments on HA Signal Status", validatedSignal.commentSignalStatus ?: " ", params.commentSignalStatus ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.commentSignalStatus = params.commentSignalStatus
        }

        String genericCommentWithoutNullCharacter = validatedSignal.genericComment?.split(Constants.NULL_CHARACTER)?.join(Constants.EMPTY_STRING) ?: Constants.NULL_VALUE
        params.genericComment = !TextUtils.isEmpty(params.genericComment) ? params.genericComment : Constants.NULL_VALUE
        if (genericCommentWithoutNullCharacter != params.genericComment) {
            if(params?.genericComment?.length() == Constants.EIGHT_THOUSAND){
                params?.genericComment = params?.genericComment + Constants.SPACE
            }
            details.append(messageSource.getMessage("signal.updated.values", ["Comments", validatedSignal.genericComment ?: " ", params.genericComment ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.genericComment = params.genericComment
        }

        ArrayList<String> newSource = []
        if (params.initialDataSource.getClass() == String) {
            newSource.add(params.initialDataSource)
        } else {
            newSource = params.initialDataSource as List<String>
        }
        if ((validatedSignal.initialDataSource != null || params.initialDataSource != null) && validatedSignal.initialDataSource != newSource?.join("##")) {
            details.append(messageSource.getMessage("signal.updated.values", ["Source", validatedSignal.initialDataSource ?: " ", params.initialDataSource?.toString() ?: " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.initialDataSource = newSource?.join("##")
        }

        Holders.config.signal.summary.dynamic.fields.sort {it.sequence}.each {

            if (it.fieldName == "UD_Text1" && it.enabled == true) {
                Map udText1Field = it
                if (!(validatedSignal.udText1 ?: "").equals(params.udText1 ?: "")) {
                    details.append(messageSource.getMessage("signal.updated.values", ["${udText1Field.label}", validatedSignal.udText1 ?: "", params.udText1 ?: "", validatedSignal.name] as Object[], Locale.default))
                    validatedSignal.udText1 = params.udText1 ? params.udText1 : ""
                }
            }

            if (it.fieldName == "UD_Text2" && it.enabled == true) {
                Map udText2Field = it
                if (!(validatedSignal.udText2 ?: "").equals(params.udText2 ?: "")) {
                    details.append(messageSource.getMessage("signal.updated.values", ["${udText2Field.label}", validatedSignal.udText2 ?: "", params.udText2 ?: "", validatedSignal.name] as Object[], Locale.default))
                    validatedSignal.udText2 = params.udText2 ? params.udText2 : ""

                }
            }

            if (it.fieldName == "UD_Dropdown1" && it.enabled == true) {
                Map udDropdown1Field = it
                String[] list = params.udDropdown1 instanceof String ? [params.udDropdown1] : (String[]) params.udDropdown1
                String ddValue1 = Holders.config.signal.summary.dynamic.dropdown.values["UD_Dropdown1"]?.findAll {
                    list?.contains(it.key)
                }?.values()?.join(",")
                if (!(validatedSignal.ddValue1 ?: "").equals(ddValue1 ?: "")) {
                    details.append(messageSource.getMessage("signal.updated.values", ["${udDropdown1Field.label}", validatedSignal.ddValue1 ?: "", ddValue1 ?: "", validatedSignal.name] as Object[], Locale.default))
                    if (params.udDropdown1 instanceof String[]) {
                        validatedSignal.udDropdown1 = params.udDropdown1.join(",")
                    } else {
                        validatedSignal.udDropdown1 = params.udDropdown1
                    }
                    validatedSignal.ddValue1 = ddValue1 ? ddValue1 : ""
                }

            }

            if (it.fieldName == "UD_Dropdown2" && it.enabled == true) {
                Map udDropdown2Field = it
                String[] list = params.udDropdown2 instanceof String ? [params.udDropdown2] : (String[]) params.udDropdown2
                String ddValue2 = Holders.config.signal.summary.dynamic.dropdown.values["UD_Dropdown2"]?.findAll {
                    list?.contains(it.key)
                }?.values()?.join(",")
                if (!(validatedSignal.ddValue2 ?: "").equals(ddValue2 ?: "")) {
                    details.append(messageSource.getMessage("signal.updated.values", ["${udDropdown2Field.label}", validatedSignal.ddValue2 ?: "", ddValue2 ?: "", validatedSignal.name] as Object[], Locale.default))
                    validatedSignal.udDropdown2 = params.udDropdown2 ? params.udDropdown2 : ""
                    if (params.udDropdown2 instanceof String[]) {
                        validatedSignal.udDropdown2 = params.udDropdown2.join(",")
                    } else {
                        validatedSignal.udDropdown2 = params.udDropdown2
                    }
                    validatedSignal.ddValue2 = ddValue2 ? ddValue2 : ""
                }
            }

            String prevDate = ""
            String newDate = ""

            if (it.fieldName == "UD_Date1" && it.enabled == true) {
                Map udDate1Field = it
                prevDate = validatedSignal.udDate1 ? validatedSignal.udDate1.format(DEFAULT_DATE_FORMAT) : ""
                newDate = !TextUtils.isEmpty(params.udDate1) ? params.udDate1 : ""
                if (prevDate != newDate) {
                    details.append(messageSource.getMessage("signal.updated.values", ["${udDate1Field.label}", prevDate ?: " ", newDate ?: " ", validatedSignal.name] as Object[], Locale.default))
                    validatedSignal.udDate1 = params.udDate1 ? DateUtil.parseDate(params.udDate1, DEFAULT_DATE_FORMAT) : null
                }

            }
            if (it.fieldName == "UD_Date2" && it.enabled == true) {
                Map udDate2Field = it
                prevDate = validatedSignal.udDate2 ? validatedSignal.udDate2.format(DEFAULT_DATE_FORMAT) : ""
                newDate = !TextUtils.isEmpty(params.udDate2) ? params.udDate2 : ""
                if (prevDate != newDate) {
                    details.append(messageSource.getMessage("signal.updated.values", ["${udDate2Field.label}", prevDate ?: " ", newDate ?: " ", validatedSignal.name] as Object[], Locale.default))
                    validatedSignal.udDate2 = params.udDate2 ? DateUtil.parseDate(params.udDate2, DEFAULT_DATE_FORMAT) : null
                }
            }
        }

    }


    private void setUpdatedValuesForNonStringFields(Map<String, String> params, ValidatedSignal validatedSignal, StringBuilder details) {

        Disposition updatedHaSignalStatus = !TextUtils.isEmpty(params.haSignalStatus) ? Disposition.get(Long.parseLong(params.haSignalStatus)) : null
        if (updatedHaSignalStatus != validatedSignal.haSignalStatus) {
            details.append(messageSource.getMessage("signal.updated.values", ["HA Signal Status", validatedSignal.haSignalStatus ? validatedSignal.haSignalStatus.displayName : " ", updatedHaSignalStatus ? updatedHaSignalStatus.displayName : " ", validatedSignal.name] as Object[], Locale.default))
            validatedSignal.haSignalStatus = updatedHaSignalStatus
        }
        Set<User> oldUsers = validatedSignal.getShareWithUsers()
        Set<Group> oldGroups = validatedSignal.getShareWithGroups()
        userService.bindSharedWithConfiguration(validatedSignal, params.sharedWith, true)
        if (Holders.config.validatedSignal.shareWith.enabled) {
            userService.bindSharedWithConfiguration(validatedSignal, params.sharedWith, true)
        }
        Set<User> newUsers = validatedSignal.getShareWithUsers()
        Set<Group> newGroups = validatedSignal.getShareWithGroups()
        if (oldUsers != newUsers || oldGroups != newGroups)
            details.append(messageSource.getMessage("signal.updated.values", [messageSource.getMessage("shared.with", null, Locale.default), oldUsers + oldGroups, newUsers + newGroups,validatedSignal.name] as Object[], Locale.default))

    }

    private void changeInProduct(String signalName, String oldProducts, String newProducts, StringBuilder details, String oldGroups, String newGroups) {
        JsonSlurper slurper = new JsonSlurper()
        def productDictionaryLabel = dataObjectService.getProductMapping()
        def oldProduct = null
        def newProduct = null
        def oldGroup = null
        def newGroup = null
        def oldPrdSet = [] as Set
        def newPrdSet = [] as Set
        def oldGrpSet = [] as Set
        def newGrpSet = [] as Set

        boolean isOldProducts = TextUtils.isEmpty(oldProducts)
        boolean isnewProducts = TextUtils.isEmpty(newProducts)
        boolean isOldGroups = TextUtils.isEmpty(oldGroups)
        boolean isnewGroups= TextUtils.isEmpty(newGroups)

        oldProduct = !isOldProducts ? slurper.parseText(oldProducts) : null
        newProduct = !isnewProducts ? slurper.parseText(newProducts) : null
        oldGroup = !isOldGroups ? slurper.parseText(oldGroups) : null
        newGroup = !isnewGroups ? slurper.parseText(newGroups) : null

        oldGrpSet = oldGroup ? oldGroup?.collect { it.name } as Set : [] as Set
        newGrpSet = newGroup ? newGroup?.collect { it.name } as Set : [] as Set
        if(oldGrpSet != newGrpSet) {
            details.append("Product Groups changed from '${!oldGrpSet.isEmpty() ? oldGrpSet.join(",") : " "}' to '${!newGrpSet.isEmpty() ? newGrpSet.join(",") : ""}' for signal '${signalName}'<br>")
        }
        oldPrdSet = oldProduct ? oldProduct['1']?.collect { it.name } as Set : [] as Set
        newPrdSet = newProduct ? newProduct['1']?.collect { it.name } as Set : [] as Set
        if (oldPrdSet != newPrdSet) {
            details.append("${productDictionaryLabel[1]?.label} changed from '${!oldPrdSet.isEmpty() ? oldPrdSet.join(",") : " "}' to '${!newPrdSet.isEmpty() ? newPrdSet.join(",") : ""}' for signal '${signalName}'<br>")
        }
        oldPrdSet = oldProduct ? oldProduct['2']?.collect { it.name } as Set : [] as Set
        newPrdSet = newProduct ? newProduct['2']?.collect { it.name } as Set : [] as Set
        if (oldPrdSet != newPrdSet) {
            details.append("${productDictionaryLabel[2]?.label} changed from '${!oldPrdSet.isEmpty() ? oldPrdSet.join(",") : " "}' to '${!newPrdSet.isEmpty() ? newPrdSet.join(",") : ""}' for signal '${signalName}'<br>")
        }
        oldPrdSet = oldProduct ? oldProduct['3']?.collect { it.name } as Set : [] as Set
        newPrdSet = newProduct ? newProduct['3']?.collect { it.name } as Set : [] as Set
        if (oldPrdSet != newPrdSet) {
            details.append("${productDictionaryLabel[3]?.label} changed from '${!oldPrdSet.isEmpty() ? oldPrdSet.join(",") : " "}' to '${!newPrdSet.isEmpty() ? newPrdSet.join(",") : ""}' for signal '${signalName}'<br>")
        }
        oldPrdSet = oldProduct ? oldProduct['4']?.collect { it.name } as Set : [] as Set
        newPrdSet = newProduct ? newProduct['4']?.collect { it.name } as Set : [] as Set
        if (oldPrdSet != newPrdSet) {
            details.append("${productDictionaryLabel[4]?.label} changed from '${!oldPrdSet.isEmpty() ? oldPrdSet.join(",") : " "}' to '${!newPrdSet.isEmpty() ? newPrdSet.join(",") : ""}' for signal '${signalName}'<br>")
        }

    }

    private void changeInEvents(String signalName, String oldEvents, String newEvents, StringBuilder details, String oldGroups, String newGroups) {
        JsonSlurper slurper = new JsonSlurper()
        def oldEvent = null
        def newEvent = null
        def oldGroup = null
        def newGroup = null
        def oldEventSet = [] as Set
        def newEventSet = [] as Set
        def oldGrpSet = [] as Set
        def newGrpSet = [] as Set

        boolean isOldEvents = TextUtils.isEmpty(oldEvents)
        boolean isnewEvents = TextUtils.isEmpty(newEvents)
        boolean isOldGroups = TextUtils.isEmpty(oldGroups)
        boolean isNewGroups= TextUtils.isEmpty(newGroups)

        oldEvent = !isOldEvents ? slurper.parseText(oldEvents) : null
        newEvent = !isnewEvents ? slurper.parseText(newEvents) : null
        oldGroup = !isOldGroups ? slurper.parseText(oldGroups) : null
        newGroup = !isNewGroups ? slurper.parseText(newGroups) : null

        oldGrpSet = oldGroup ? oldGroup?.collect { it.name } as Set : [] as Set
        newGrpSet = newGroup ? newGroup?.collect { it.name } as Set : [] as Set
        if(oldGrpSet != newGrpSet){
            details.append("Event Groups changed from '${(oldGrpSet != null && !oldGrpSet.isEmpty()) ? oldGrpSet.join(",") : " "}' to '${!newGrpSet.isEmpty() ? newGrpSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['1'] ? oldEvent['1']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['1'] ? newEvent['1']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("SOC changed from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['2'] ? oldEvent['2']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['2'] ? newEvent['2']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("HLGT changed from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['3'] ? oldEvent['3']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['3'] ? newEvent['3']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("HLT changed from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['4'] ? oldEvent['4']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['4'] ? newEvent['4']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("PT changed from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['5'] ? oldEvent['5']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['5'] ? newEvent['5']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("LLT changed from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['6'] ? oldEvent['6']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['6'] ? newEvent['6']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("Synonyms changed from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['7'] ? oldEvent['7']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['7'] ? newEvent['7']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("SMQ Broad from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }
        oldEventSet = oldEvent ? (oldEvent['8'] ? oldEvent['8']?.collect { it.name } as Set : [] as Set) : [] as Set
        newEventSet = newEvent ? (newEvent['8'] ? newEvent['8']?.collect { it.name } as Set : [] as Set) : [] as Set
        if (oldEventSet != newEventSet) {
            details.append("SMQ Narrow from '${(oldEventSet != null && !oldEventSet.isEmpty()) ? oldEventSet.join(",") : " "}' to '${!newEventSet.isEmpty() ? newEventSet.join(",") : " "}' for signal '${signalName}'<br>")
        }

    }

    private void editTopicCategory(ValidatedSignal validatedSignal, def signalTypeList, StringBuilder details) {
        if (signalTypeList) {
            if (signalTypeList.getClass() == String) {
                signalTypeList = [signalTypeList]
            }
            def topicCategoryList = signalTypeList.toList() as Set
            if (topicCategoryList != validatedSignal.topicCategories*.name as Set) {
                details.append(messageSource.getMessage("signal.updated.values", ["Risk/Topic Category", !validatedSignal.topicCategories.isEmpty() ? validatedSignal.topicCategories*.name.join(",") : " ", !topicCategoryList.isEmpty() ? topicCategoryList.join(",") : " ", validatedSignal.name] as Object[], Locale.default))
                if (validatedSignal?.topicCategories) {
                    validatedSignal.topicCategories.clear()
                }
                topicCategoryList.each {
                    TopicCategory topicCategory = TopicCategory.findByName("" + it)
                    if (topicCategory) {
                        validatedSignal.addToTopicCategories(topicCategory)
                    }
                }
            }
        } else if (!signalTypeList && !validatedSignal.topicCategories.isEmpty()) {
            details.append(messageSource.getMessage("signal.updated.values", ["Risk/Topic Category", validatedSignal.topicCategories*.name.join(","), " ", validatedSignal.name] as Object[], Locale.default))
            if (validatedSignal?.topicCategories) {
                validatedSignal.topicCategories.clear()
            }
        }
    }

    private void editSignalOutcome(ValidatedSignal validatedSignal, def signalOutcome, StringBuilder details) {
        if (signalOutcome) {
            signalOutcome = convertStringToList(signalOutcome)
            if (signalOutcome != validatedSignal.signalOutcomes*.name) {
                details.append(messageSource.getMessage("signal.updated.values", ["Signal Outcomes", validatedSignal.signalOutcomes ? validatedSignal.signalOutcomes*.name.join(",") : " ", signalOutcome ? signalOutcome.join(",") : " ", validatedSignal.name] as Object[], Locale.default))
                if (validatedSignal?.signalOutcomes) {
                    validatedSignal.signalOutcomes.clear()
                }
                signalOutcome.each {
                    SignalOutcome sigOutcome = SignalOutcome.findByName(it)
                    if (sigOutcome) {
                        validatedSignal.addToSignalOutcomes(sigOutcome)
                    }
                }
            }
        } else if (!signalOutcome && validatedSignal.signalOutcomes) {
            details.append(messageSource.getMessage("signal.updated.values", ["Signal Outcomes", validatedSignal.signalOutcomes*.name.join(","), " ", validatedSignal.name] as Object[], Locale.default))
            if (validatedSignal?.signalOutcomes) {
                validatedSignal.signalOutcomes.clear()
            }
        }
    }

    private void editLinkedSignals(ValidatedSignal validatedSignal, def linkedSignal, StringBuilder details) {
        if (linkedSignal.getClass() == String) {
            linkedSignal = [linkedSignal]
        }
        def linkedSignalsId=validatedSignal.linkedSignals*.id
        def linkedSignalIdsAsLong=linkedSignal.collect{it as Long}
        def removedSignalIds=[]
        removedSignalIds=linkedSignalsId.minus(linkedSignalIdsAsLong)
        if(removedSignalIds.size()>0)
        {
            removeCurrentSignalFromLinked(validatedSignal,removedSignalIds)
        }

        if (linkedSignal) {
            linkedSignal = linkedSignal.collect { Long.valueOf(it) }

            def sortedLinkedSignal = linkedSignal.collect { Long.valueOf(it) }.sort()
            def sortedValidatedLinkedSignals = validatedSignal.linkedSignals*.id.collect { it }.sort()

            if (!sortedLinkedSignal.equals(sortedValidatedLinkedSignals)) {
                details.append(messageSource.getMessage("signal.updated.values", ["Linked Signal", validatedSignal.linkedSignals ? validatedSignal.linkedSignals*.name.join(",") : " ", linkedSignal ? ValidatedSignal.getAll(linkedSignal)*.name?.join(",") : " ", validatedSignal.name] as Object[], Locale.default))
                if (validatedSignal?.linkedSignals) {
                    validatedSignal.linkedSignals.clear()
                }
                linkedSignal.each {
                    ValidatedSignal linkSignal = ValidatedSignal.get(it)
                    if (linkSignal) {
                        validatedSignal.addToLinkedSignals(linkSignal)
                        linkSignal.addToLinkedSignals(validatedSignal)
                    }
                }
            }
        } else if (!linkedSignal && validatedSignal.linkedSignals) {
            details.append(messageSource.getMessage("signal.updated.values", ["Linked Signal", validatedSignal.linkedSignals*.name.join(","), " ", validatedSignal.name] as Object[], Locale.default))
            if (validatedSignal?.linkedSignals) {
                validatedSignal.linkedSignals.clear()
            }
        }
    }

    def removeCurrentSignalFromLinked(ValidatedSignal validatedSignal,List linkedIdSignalList){
        linkedIdSignalList.each{
            if (Objects.nonNull(it)) {
                ValidatedSignal linkSignal = ValidatedSignal.get(it)
                linkSignal.removeFromLinkedSignals(validatedSignal)
            }
        }
    }

    private void editActionTaken(ValidatedSignal validatedSignal, def actionTaken, StringBuilder details) {
        if (actionTaken.toString() != 'null') {
            if (actionTaken instanceof String) {
                actionTaken = [actionTaken]
            }
            def actionTakenSet = actionTaken.toList() as Set

            if (actionTakenSet != validatedSignal.actionTaken) {
                details.append(messageSource.getMessage("signal.updated.values", ["Action Taken", !validatedSignal.actionTaken.isEmpty() ? validatedSignal.actionTaken.join(",") : " ", !actionTakenSet.isEmpty() ? actionTakenSet.join(",") : " ", validatedSignal.name] as Object[], Locale.default))
            }
            validatedSignal.actionTaken = actionTakenSet.collect({it.toString()})
        } else if (actionTaken.toString() == 'null' && !validatedSignal.actionTaken.isEmpty()) {
            details.append(messageSource.getMessage("signal.updated.values", ["Action Taken", validatedSignal.actionTaken.join(","), " ", validatedSignal.name] as Object[], Locale.default))
            if (validatedSignal.actionTaken) {
                validatedSignal?.actionTaken = []
            }
        }
    }

    private void editEvaluationMethod(ValidatedSignal validatedSignal, def evaluationMethod, StringBuilder details) {

        if (evaluationMethod.toString() != 'null') {
            if (evaluationMethod instanceof String) {
                evaluationMethod = [evaluationMethod]
            }
            def evaluationMethodSet = evaluationMethod.toList() as Set
            if (evaluationMethodSet != validatedSignal.evaluationMethod) {
                details.append(messageSource.getMessage("signal.updated.values", ["Evaluation Method", !validatedSignal.evaluationMethod.isEmpty() ? validatedSignal.evaluationMethod.join(",") : " ", !evaluationMethodSet.isEmpty() ? evaluationMethodSet.join(",") : " ", validatedSignal.name] as Object[], Locale.default))
            }
            validatedSignal.evaluationMethod = evaluationMethodSet.collect({it.toString()})
        } else if (evaluationMethod.toString() == 'null' && !validatedSignal.evaluationMethod.isEmpty()) {
            details.append(messageSource.getMessage("signal.updated.values", ["Evaluation Method", validatedSignal.evaluationMethod.join(","), " ", validatedSignal.name] as Object[], Locale.default))
            if (validatedSignal.evaluationMethod) {
                validatedSignal?.evaluationMethod = []
            }
        }
    }

    /**
     * Adds a configuration to a validated signal.
     *
     * This method retrieves a configuration and a validated signal based on their IDs,
     * adds the configuration to the signal, creates activity, and saves the changes.
     * It also creates an audit log entry.
     *
     * @param signalId the ID of the validated signal as a String
     * @param configId the ID of the configuration as a Long
     * @return void
     */
    void addConfigurationToSignal(String signalId, Long configId) {
        Configuration configuration = Configuration.get(configId)
        ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(signalId))
        String oldConfigurations = validatedSignal?.configuration?.toString()
        validatedSignal.addToConfiguration(configuration)
        // here we are setting skipAudit to true because in logs we were getting 'SYSTEM' instead of user name, hence now we are manually creating the audit log
        validatedSignal.skipAudit = true
        User user = userService.getUser() ?: configuration?.owner
        activityService.createActivityForSignal(validatedSignal, '', "${configuration.type} '${configuration.name}' linked", ActivityType.findByValue(ActivityTypeValue.AlertLinked),
                validatedSignal.assignedTo, user, null, validatedSignal.assignedToGroup);
        validatedSignal.save(flush: true)
        validatedSignal.refresh()

        String newConfigurations = validatedSignal?.configuration?.toString()
        signalAuditLogService.createAuditLog([
                username   : user.username,
                fullname   : user.fullName,
                entityName : StringUtils.uncapitalize(validatedSignal.class.getSimpleName()),
                moduleName : "Signal",
                category   : AuditTrail.Category.UPDATE.toString(),
                entityValue: validatedSignal.name,
                description: ""
        ] as Map, [[propertyName: "Configuration", oldValue: oldConfigurations, newValue: newConfigurations]])
    }

    def addEvdasConfigurationToSignal(String signalId, EvdasConfiguration configuration) {
        ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(signalId))
        validatedSignal.addToEvdasConfiguration(configuration)
        activityService.createActivityForSignal(validatedSignal, '', Constants.AlertConfigType.EVDAS_ALERT + " '${configuration.name}' linked",
                ActivityType.findByValue(ActivityTypeValue.AlertLinked), validatedSignal.assignedTo, userService.getUser(), null, validatedSignal.assignedToGroup);
    }

    def composeLinkedConfigurationList(String signalId) {
        User currentUser = userService.getUser()
        Set<Group> groups = currentUser?.groups
        String groupIds = currentUser?.groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}.collect { it.id }.join(",")
        Group workflowGroup = currentUser?.workflowGroup
        ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(signalId))
        List<Configuration> configurationList = validatedSignal.configuration as List<Configuration>
        String timeZone = userService.getUser().preference.timeZone
        List<Map> result = []
        boolean isEnabled = true
        if (configurationList.size() > 0) {
            List<ExecutedConfiguration> linkedConfigurationList = []
            List<String> configurationNameList = configurationList.collect {
                it.name
            }
            linkedConfigurationList = ExecutedConfiguration.createCriteria().list(sort: "id", order: "desc") {
                or {
                    configurationNameList.collate(1000).each{
                        'in'("name", it)
                    }
                }
                'eq'("isDeleted", false)
                'eq'("workflowGroup", workflowGroup)
                'or' {
                    'or' {
                        'eq'('assignedTo', currentUser)
                    }
                    'or'{
                        'in'('assignedToGroup', groups)
                    }
                    'or' {
                        sqlRestriction("""CONFIG_ID IN
                        (${SignalQueryHelper.user_configuration_sql(getUserService().getCurrentUserId(), workflowGroup.id, groupIds, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)}
                         )""")
                    }
                    'or' {
                        sqlRestriction("""CONFIG_ID IN
                        (${SignalQueryHelper.user_configuration_sql(getUserService().getCurrentUserId(), workflowGroup.id, groupIds, Constants.AlertConfigType.SINGLE_CASE_ALERT)}
                         )""")
                    }

                }
            } as List<ExecutedConfiguration>

            if (linkedConfigurationList.size() > 0) {
                linkedConfigurationList.each {ExecutedConfiguration executedConfiguration ->
                    isEnabled = true
                    Configuration configuration = configurationList.find {
                        executedConfiguration.name == it.name
                    }
                    if (executedConfiguration.isLatest && configuration.executing) {
                        isEnabled = false
                    }
                    ExecutionStatus executionStatus = ExecutionStatus.findByConfigIdAndTypeAndReportVersion(configuration.id,executedConfiguration.type,executedConfiguration.numOfExecutions)
                    if(executionStatus && executionStatus.executionStatus == ReportExecutionStatus.COMPLETED){
                        result.add(linkedConfigurationDTO(executedConfiguration, timeZone, isEnabled))
                    }
                }
            }
        }

        List<EvdasConfiguration> evdasConfigurationList = validatedSignal.evdasConfiguration as List<EvdasConfiguration>

        if (evdasConfigurationList) {
            List<ExecutedEvdasConfiguration> linkedEvdasConfigurationList = []
            List<String> evdasConfigurationNameList = evdasConfigurationList.collect {
                it.name
            }
            linkedEvdasConfigurationList = ExecutedEvdasConfiguration.createCriteria().list(sort: "id", order: "desc") {
                or {
                    evdasConfigurationNameList.collate(1000).each{
                        'in'("name", it)
                    }
                }
                'eq'("isDeleted", false)
                'eq'("workflowGroup", workflowGroup)
                'or' {
                    'or' {
                        'eq'('assignedTo', currentUser)
                    }
                    'or'{
                        'in'('assignedToGroup', groups)
                    }
                    'or' {
                        sqlRestriction("""CONFIG_ID IN
                        (${SignalQueryHelper.evdas_configuration_sql(getUserService().getCurrentUserId(), workflowGroup.id, groupIds)}
                         )""")
                    }

                }
            } as List<ExecutedEvdasConfiguration>

            if (linkedEvdasConfigurationList.size() > 0) {
                linkedEvdasConfigurationList.each { ExecutedEvdasConfiguration executedEvdasConfiguration ->
                    isEnabled = true
                    EvdasConfiguration evdasConfiguration = evdasConfigurationList.find {
                        executedEvdasConfiguration.name == it.name
                    }
                    if (executedEvdasConfiguration.isLatest && evdasConfiguration.executing) {
                        isEnabled = false
                    }
                    ExecutionStatus executionStatus = ExecutionStatus.findByConfigIdAndTypeAndReportVersion(evdasConfiguration.id, Constants.AlertConfigType.EVDAS_ALERT, executedEvdasConfiguration.numOfExecutions)
                    if (executionStatus && executionStatus.executionStatus == ReportExecutionStatus.COMPLETED) {
                        result.add(linkedEvdasConfigurationDTO(executedEvdasConfiguration, timeZone, evdasConfiguration, isEnabled))
                    }
                }
            }
        }
        result
    }

    def linkedConfigurationDTO(ExecutedConfiguration executedConfiguration, String timeZone, boolean isEnabled) {
        String dateRange = DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeStartAbsolute) +
                " - " +
                DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeEndAbsolute)
        StringBuilder criteria = new StringBuilder()

        String events = ''
        if (executedConfiguration.eventSelection) {
            events = executedConfiguration.allEventSelectionList.join(",")
        }
        if (executedConfiguration.eventGroupSelection) {
            events = executedConfiguration.eventGroupSelectionList
        }
        List<String> queryParameters = []
        String queryName = ''
        if (executedConfiguration.executedAlertQueryId) {
            queryName = executedConfiguration.alertQueryName
            if (executedConfiguration.executedAlertQueryValueLists.size() > 0) {
                executedConfiguration.executedAlertQueryValueLists.each { eaqvl ->
                    criteria.append(eaqvl.queryName)
                    StringBuilder queryParameter = new StringBuilder()
                    eaqvl.parameterValues.each { parameter ->
                        if (parameter.hasProperty('reportField')) {
                            queryParameter.append(messageSource.getMessage("app.reportField.${parameter.reportField.name}", null, Locale.default))
                            queryParameter.append(" ")
                            queryParameter.append(messageSource.getMessage("${parameter.operator.getI18nKey()}", null, Locale.default))
                            queryParameter.append(" ")
                            queryParameter.append(parameter.value)
                            queryParameters.add(queryParameter.toString())
                        } else {
                            queryParameters.add("${parameter.key} : ${parameter.value}")
                        }
                        queryParameter.setLength(0);
                    }
                }
            }
        }

        [
                name           : executedConfiguration.name,
                id             : executedConfiguration.id,
                version        : executedConfiguration.numOfExecutions,
                type           : executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ? Constants.AlertConfigTypeShort.QUALITATIVE_ALERT: Constants.AlertConfigTypeShort.QUANTITATIVE_ALERT,
                isOndemand     : executedConfiguration.adhocRun,
                dateRange      : dateRange,
                criteria       : criteria,
                isEnabled      : isEnabled,
                productName    : (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(executedConfiguration) ? getCacheService().getUpperHierarchyProductDictionaryCache(executedConfiguration.id) : getNameFieldFromJson(executedConfiguration.productSelection))?:getGroupNameFieldFromJson(executedConfiguration.productGroupSelection),
                events         : events,
                queryName      : queryName,
                queryParameters: queryParameters,
                lastExecuted   : DateUtil.stringFromDate(executedConfiguration.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone)
        ]

    }

    def linkedEvdasConfigurationDTO(ExecutedEvdasConfiguration executedConfiguration, String timeZone, EvdasConfiguration evdasConfiguration, boolean isExecuting) {
        String dr = Constants.Commons.BLANK_STRING

        List<Date> dateRange = executedConfiguration.dateRangeInformation?.getReportStartAndEndDate()
        if (executedConfiguration.dateRangeInformation.dateRangeEnum == executedConfiguration.dateRangeInformation?.dateRangeEnum.CUMULATIVE) {
            Date dateRangeEnd = executedConfiguration.dateCreated
            dr = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
        } else {
            dr = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
        }

        StringBuilder criteria = new StringBuilder()
        String events = ""
        if (executedConfiguration.eventSelection) {
            events = executedConfiguration.eventSelectionList.join(",")
        }
        if (executedConfiguration.eventGroupSelection) {
            events = executedConfiguration.eventGroupSelectionList
        }
        String query = ""
        if (evdasConfiguration.query) {
            query = evdasConfiguration.queryName
        }
        [
                name        : executedConfiguration.name,
                id          : executedConfiguration.id,
                version     : executedConfiguration.numOfExecutions,
                type        : 'EVDAS',
                isOndemand  : executedConfiguration.adhocRun,
                productName : executedConfiguration.productGroupSelection? getGroupNameFieldFromJson(executedConfiguration.productGroupSelection) : ViewHelper.getDictionaryValues(executedConfiguration.productSelection, DictionaryTypeEnum.PRODUCT),
                events      : events,
                queryName   : query,
                dataSource  : Constants.DataSource.EVDAS.toUpperCase(),
                dateRange   : dr,
                criteria    : criteria,
                isEnabled   : isExecuting,
                lastExecuted: DateUtil.stringFromDate(executedConfiguration.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone)
        ]

    }

    def changeDisposition(String selectedRows, Disposition targetDisposition, String justification, String incomingDisposition) {
        String currentUserFullName = userService.getUser()?.fullName
        String userTimeZone = userService.getUser()?.preference?.timeZone
        boolean isValidatedDateFlag = false
        SystemConfig systemConfig = SystemConfig.first()
        List dateClosedBasedOnDispList = systemConfig.dateClosedDisposition ? systemConfig.dateClosedDisposition.split(',') : []
        List dateClosedBasedOnWorkflowList = systemConfig.dateClosedDispositionWorkflow ? systemConfig.dateClosedDispositionWorkflow.split(',') : []
        List<String> dueInEndPoints = systemConfig.selectedEndPoints?.split(",").collect {it} as List<String>
        ValidatedSignal signal = ValidatedSignal.get(JSON.parse(selectedRows).first()."signal.id")
        boolean isDispositionChangeAllowed = true
        if (!incomingDisposition.equals(signal.disposition.displayName)) {
            isDispositionChangeAllowed = false
        }
        Integer dueIn = null
        if (isDispositionChangeAllowed) {
            Long updationTime = new Date().getTime()
            def milestoneCompletionDate = signal.milestoneCompletionDate
            String previousDueDate = DateUtil.fromDateToString(signal.actualDueDate, DEFAULT_DATE_FORMAT)
            Disposition defaultSignalDisposition = userService.getUser()?.getWorkflowGroup()?.defaultSignalDisposition
            Boolean changeDueDateToNull = false
            if ((targetDisposition.isClosed() || targetDisposition.isReviewCompleted()) && (targetDisposition.id != defaultSignalDisposition.id)) {
                changeDueDateToNull = true
            }
            Disposition previousDisposition = signal.disposition
            signal.customAuditProperties = ["justification": justification]
            // PVS-40946 added justification text for disposition change== Issue 3
            signal.disposition = targetDisposition
            signal.lastDispChange = new Date()
            Date prevDueDate = signal.dueDate
            Date prevActualDueDate = signal.actualDueDate
            if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_SIGNAL)) {
                emailNotificationService.mailHandlerForDispChangeSignal(signal, previousDisposition)
            }
            createActivityForDispositionChange(signal, previousDisposition, justification)
            createProductEventHistoryForDispositionChange(signal, justification)
            signal.dispPerformedBy = currentUserFullName;
            List<String> validatedDateDispositions = Holders.config.alert.validatedDateDispositions;
            boolean isValidatedDateSignal = false;
            isValidatedDateSignal = validatedDateDispositions.contains(targetDisposition.value);
            List<SignalStatusHistory> listSignalHistory = [];
            Long signalHistoryId = null;
            listSignalHistory = signal.getSignalStatusHistories();
            String validationDateStr = Holders.config.signal.defaultValidatedDate
            Boolean isValidationDateEntryPresent = false
            listSignalHistory?.each {
                if (it?.signalStatus == validationDateStr && isValidatedDateSignal) {
                    isValidationDateEntryPresent = true
                }
            }
            Boolean retain = false
            if (listSignalHistory.size() > 0 && !isValidationDateEntryPresent) {
                SignalStatusHistory signalStatusHistory = listSignalHistory.get(0)
                if ("New Signal Created".equalsIgnoreCase(signalStatusHistory.signalStatus) && isValidatedDateSignal) {
                    signalStatusHistory.signalId = signal.id
                    isValidatedDateFlag = true
                    if (Holders.config.detectedDateAndValidationDate.synch.enabled && validatedDateDispositions.contains(signal.disposition.value)) {
                        def oldDate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(signal.detectedDate)
                        def newDate = DateUtil.fromDateToString(new Date(), DEFAULT_DATE_FORMAT)
                        if (oldDate != newDate) {
                            String details = "Detected Date changed from '" + oldDate + "' to '" + newDate + "'"
                            saveActivityForSignalHistory(Constants.SYSTEM_USER, signal, details)
                            signal.detectedDate = new Date()
                        }
                    }
                    String userTimeZoneFormatedDate = DateUtil.fromDateToStringWithTimezone(new Date(), DateUtil.DEFAULT_DATE_FORMAT, userTimeZone)
                    String formatedDateWithTime = DateUtil.fromDateToStringWithTimezone(new Date(), DateUtil.DEFAULT_DATE_TIME_FORMAT, Holders.config.server.timezone)
                    signalStatusHistory.statusComment = Constants.WorkFlowLog.VALIDATION_STATUS_COMMENT
                    signalStatusHistory.isSystemUser = true
                    String defaultValidatedDate = Holders.config.signal.defaultValidatedDate
                    signalStatusHistory.signalStatus = (defaultValidatedDate) ? defaultValidatedDate : Constants.WorkFlowLog.VALIDATION_DATE;
                    String details = signalStatusHistory.signalStatus + " has been updated to '${userTimeZoneFormatedDate}' -- with Justification '${signalStatusHistory.statusComment}'"
                    saveActivityForSignalHistory(Constants.SYSTEM_USER, signal, details)
                    signalStatusHistory.dispositionUpdated = true
                    signalStatusHistory.performedBy = Constants.SYSTEM_USER
                    signalStatusHistory.dateCreated = DateUtil.parseDate(formatedDateWithTime, DateUtil.DEFAULT_DATE_TIME_FORMAT, Holders.config.server.timezone)
                    signalStatusHistory.currentDispositionId = targetDisposition.id
                    signalStatusHistory.updateTime = updationTime
                    CRUDService.update(signalStatusHistory);
                    String updateDateCreated = "Update SignalStatusHistory set dateCreated = :dateCreated where id = :id"
                    SignalStatusHistory.executeUpdate(updateDateCreated, [dateCreated: signalStatusHistory.dateCreated, id: signalStatusHistory.id])
                    saveSignalStatusHistory([signalStatus: targetDisposition.displayName, statusComment: justification, signalId: signal.id, "currentDispositionId": targetDisposition.id, isAutoPopulate: true, dueDate: prevDueDate, updationTime: updationTime], false)
                } else {
                    if (!(previousDisposition.id != defaultSignalDisposition.id && previousDisposition.resetReviewProcess && previousDisposition.reviewCompleted && (!targetDisposition.reviewCompleted ||
                            targetDisposition.id == defaultSignalDisposition.id))) {
                        retain = true
                    }
                    saveSignalStatusHistory([signalStatus: targetDisposition.displayName, statusComment: justification, signalId: signal.id, "currentDispositionId": targetDisposition.id, isAutoPopulate: true, dueDate: prevDueDate, updationTime: updationTime], false, false, false, retain)
                }
            } else if (!isValidationDateEntryPresent) {
                saveSignalStatusHistory([signalStatus: targetDisposition.displayName, statusComment: justification, signalId: signal.id, "currentDispositionId": targetDisposition.id, isAutoPopulate: true, dueDate: prevDueDate, updationTime: updationTime], false)
            } else if (isValidationDateEntryPresent && isValidatedDateSignal){
                saveSignalStatusHistory([signalStatus: targetDisposition.displayName, statusComment: justification, signalId: signal.id, "currentDispositionId": targetDisposition.id, isAutoPopulate: true, dueDate: prevDueDate, updationTime: updationTime], false)
            }
            Boolean isReset = false
            boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
            boolean enableEndOfMilestone = SystemConfig.first()?.enableEndOfMilestone
            String defaultValidatedDate = Holders.config.signal.defaultValidatedDate;
            boolean dueInStartEnabled = Holders.config.dueInStart.enabled
            String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
            Date dueInStartBaseDate = signal.signalStatusHistories.find {
                it.signalStatus == dueInStartPoint
            }?.dateCreated
            Long signalOutcome = null
            List outcomeList = SignalOutcome.findAllByDispositionId(signal?.dispositionId).findAll {!it.isDeleted}.collect { it.id }
            if(signal.signalOutcomes.size()==0 && outcomeList.size()==1 && Holders.config.disposition.signal.outcome.mapping.enabled){
                // This signal outcome will be auto-populated to signal via Ajax call
                signalOutcome = outcomeList[0]
            }
            Map dispDataMap = [objectId         : signal.id, objectType: Constants.AlertConfigType.VALIDATED_SIGNAL, prevDispositionId: previousDisposition.id,
                               currDispositionId: targetDisposition.id, prevDispPerformedBy: signal.dispPerformedBy,
                               prevDueDate      : prevDueDate, previousDueIn: dueIn, prevActualDueDate: prevActualDueDate,
                               prevMilestoneDate: milestoneCompletionDate, prevSignalStatus: signal.signalStatus, signalOutcomeId: signalOutcome]
            if (enableSignalWorkflow) {
                dueIn = dueInStartEnabled ? calculateDueIn(signal.id, dueInStartPoint) : calculateDueIn(signal.id, signal.workflowState)
                if (systemConfig.isDisposition && dateClosedBasedOnWorkflowList?.contains(targetDisposition.displayName)) {
                    String statusComment = Holders.config.validatedSignal.dateClosedBasedOnDisposition
                    String createDate = DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
                    if(!signal.actualDateClosed){
                        saveSignalStatusHistory([signalStatus: Constants.WorkFlowLog.DATE_CLOSED, statusComment: statusComment, signalId: signal.id, "createdDate": createDate, "isAutoPopulate": true, "currentDispositionId": targetDisposition.id, "isSystemUser": true, updationTime: updationTime], true)
                    }
                    if (dueInEndPoints.contains(Constants.WorkFlowLog.DATE_CLOSED)) {
                        dueIn = null
                    }
                }
            } else if (enableEndOfMilestone && (dueInStartBaseDate || !dueInStartEnabled)) { // use case - 1
                // check if disposition change from non review to review completed
                //previousDisposition, targetDisposition
                if ((previousDisposition.reviewCompleted == false || previousDisposition.id == defaultSignalDisposition.id)
                        && targetDisposition.reviewCompleted == true && targetDisposition.id != defaultSignalDisposition.id) {
                    dueIn = null
                    if (targetDisposition.signalStatusForDueDate &&
                            !signal.signalStatusHistories.find {
                                it.signalStatus == targetDisposition.signalStatusForDueDate
                            }) {
                        String signalStatus = targetDisposition.signalStatusForDueDate
                        String statusComment = "Signal end of review date added based on disposition transition."
                        String createdDate = DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_FORMAT, Holders.config.server.timezone)

                        saveSignalStatusHistory([signalStatus          : signalStatus, statusComment: statusComment,
                                                 signalId              : signal.id, "createdDate": createdDate, "isAutoPopulate": true,
                                                 "currentDispositionId": targetDisposition.id, dueDate: prevDueDate,"isSystemUser": true, updationTime: updationTime], true)

                    }
                    // If target disposition is default then Milestone date should be null
                    if (targetDisposition.id == defaultSignalDisposition.id) {
                        updateSignalStatus(signal.id, null, null)
                    } else {
                        updateSignalStatus(signal.id, null, new Date())
                    }

                } else if (previousDisposition.resetReviewProcess == false && previousDisposition.reviewCompleted == true
                        && (targetDisposition.reviewCompleted == false || targetDisposition.id == defaultSignalDisposition.id)) {
                    // use case - 3
                    // retain
                    dueIn = dueInStartEnabled ? calculateDueIn(signal.id, dueInStartPoint) : calculateDueIn(signal.id, defaultValidatedDate)
                    signal.milestoneCompletionDate = null
                    signal.dueDate = signal.actualDueDate
                    retain = true
                    CRUDService.update(signal)
                } else if (previousDisposition.id != defaultSignalDisposition.id && previousDisposition.resetReviewProcess == true && previousDisposition.reviewCompleted == true && (targetDisposition.reviewCompleted == false ||
                        targetDisposition.id == defaultSignalDisposition.id)) { // use case - 3
                    // reset
                    isReset = true
                    String serverTimeZone = Holders.config.server.timezone
                    int reviewPeriod = signal.priority.reviewPeriod
                    DateTime reviewDate = new DateTime(new Date(), DateTimeZone.forID(serverTimeZone)).plusDays(reviewPeriod)
                    Date dueDate = reviewDate.toDate()
                    dueIn =  DateUtils.truncate(new DateTime(reviewDate).toDate(), Calendar.DAY_OF_MONTH) - DateUtils.truncate(new DateTime().toDate(), Calendar.DAY_OF_MONTH)
                    updateSignalStatusReset(signal.id, dueDate, null)
                } else {
                    if (signal.actualDueDate && !changeDueDateToNull) {
                        dueIn = DateUtils.truncate(new DateTime(signal.actualDueDate).toDate(), Calendar.DAY_OF_MONTH) - DateUtils.truncate(new DateTime().toDate(), Calendar.DAY_OF_MONTH)
                        retain = true
                    } else {
                        dueIn = dueInStartEnabled ? calculateDueIn(signal.id, dueInStartPoint) : null
                    }
                }
            } else {
                if (targetDisposition && defaultSignalDisposition && targetDisposition.reviewCompleted == false || targetDisposition.id == defaultSignalDisposition.id) {
                    Boolean isDueDateNullFromDueInEndPoint = false
                    isDueDateNullFromDueInEndPoint = signal.getSignalStatusHistories()?.any {it?.signalStatus in dueInEndPoints}
                    if(!isDueDateNullFromDueInEndPoint){
                        dueIn = dueInStartEnabled ? calculateDueIn(signal.id, dueInStartPoint) : calculateDueIn(signal.id, defaultValidatedDate, false, false, false, true)
                    }
                } else {
                    dueIn = dueInStartEnabled ? calculateDueIn(signal.id, dueInStartPoint) : calculateDueIn(signal.id, defaultValidatedDate)
                }
                if ((previousDisposition.reviewCompleted == false || previousDisposition.id == defaultSignalDisposition.id)
                        && targetDisposition.reviewCompleted == true && targetDisposition.id != defaultSignalDisposition.id) {
                    // this scenerio is to handle when dueInStart point is not triggered but signal marked as non valid than Date closed workflow entry should be created based on end of review milestone configured
                    if (targetDisposition.signalStatusForDueDate &&
                            !signal.signalStatusHistories.find {
                                it.signalStatus == targetDisposition.signalStatusForDueDate
                            } && enableEndOfMilestone) {
                        String signalStatus = targetDisposition.signalStatusForDueDate
                        String statusComment = "Signal end of review date added based on disposition transition."
                        String createdDate = DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_FORMAT, Holders.config.server.timezone)

                        saveSignalStatusHistory([signalStatus          : signalStatus, statusComment: statusComment,
                                                 signalId              : signal.id, "createdDate": createdDate, "isAutoPopulate": true,
                                                 "currentDispositionId": targetDisposition.id, dueDate: prevDueDate,"isSystemUser": true, updationTime: updationTime], true)

                    }
                    Date dueDate = dueIn != null ? signal.actualDueDate : null
                    // If target disposition is default then Milestone date should be null
                    if (targetDisposition.id == defaultSignalDisposition.id) {
                        updateSignalStatus(signal.id, dueDate, null)
                    } else {
                        updateSignalStatus(signal.id, dueDate, new Date())
                    }

                }
            }
            if (!enableSignalWorkflow && dateClosedBasedOnDispList?.contains(targetDisposition.displayName)) {
                String statusComment = Holders.config.validatedSignal.dateClosedBasedOnDisposition
                String createDate = DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
                if(!signal.actualDateClosed){
                    saveSignalStatusHistory([signalStatus: Constants.WorkFlowLog.DATE_CLOSED, statusComment: statusComment, signalId: signal.id, "createdDate": createDate, "isAutoPopulate": true, "currentDispositionId": targetDisposition.id, "isSystemUser": true, updationTime: updationTime], true)
                }
                if (dueInEndPoints.contains(Constants.WorkFlowLog.DATE_CLOSED)) {
                    dueIn = null
                }
            }
            if (dueIn != null && SystemConfig.first().displayDueIn) {
                saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                         signalId    : signal.id, "createdDate": previousDueDate, "currentDispositionId": targetDisposition?.id, updationTime: updationTime], true, false, isReset, retain)
            }
            UndoableDisposition undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)
            undoableDisposition.save(flush: true)
        }
        [dueDate: dueIn != null ? new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(signal.actualDueDate) : "", dueIn: dueIn, isValidatedDateFlag: isValidatedDateFlag, isDispositionChangeAllowed: isDispositionChangeAllowed]

    }

    def updateSignalStatusReset(Long signalId, Date dueDate=null, Date milestoneCompletionDate=null) {
        ValidatedSignal signal = ValidatedSignal.findById(signalId)
        signal.milestoneCompletionDate = milestoneCompletionDate
        signal.dueDate = dueDate
        signal.actualDueDate = dueDate
        signal.newDetectedDate = new Date()
        signal.save(flush:true)
    }

    def updateSignalStatus(Long signalId, Date dueDate=null, Date milestoneCompletionDate=null) {

        try{
            String updateQuery = "update validated_signal set"
            SimpleDateFormat sdf = new SimpleDateFormat(SqlGenerationService.DATETIME_FMT)
            if(dueDate){
                updateQuery = updateQuery + " due_date = " + "TO_DATE('${sdf.format(dueDate)}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
            } else {
                updateQuery = updateQuery + " due_date = null" + ""
            }
            if(milestoneCompletionDate){
                updateQuery = updateQuery + ", milestone_completion_date = " + "TO_DATE('${sdf.format(milestoneCompletionDate)}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
            } else {
                updateQuery = updateQuery + ", milestone_completion_date = null" + ""
            }
            updateQuery = updateQuery + " where id = " + signalId + ""
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.openSession()
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()

        } catch(Exception ex) {
            ex.printStackTrace()
        }

    }

    void changeToUndoableDisp(Long id){
        ValidatedSignal signal = ValidatedSignal.get(id)
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertConfigType.VALIDATED_SIGNAL)
            order('dateCreated', 'desc')
            maxResults(1)
        }

        if(signal && undoableDisposition?.isEnabled){
            log.info("Enabling isDueDateChanged in Undoable disposition for signal: "+id)
            undoableDisposition.isDueDateChanged = true
            CRUDService.update(undoableDisposition)
        }
    }

    def revertDisposition(Long id, String justification) {
        log.info("Reverting Dispostion Started")
        List<Map> activityList = []
        List<Map> alertDueDateList = []
        List<Map> peHistoryList = []
        String oldDispName = ""
        String currDispName = ""
        ValidatedSignal signal = ValidatedSignal.get(id)
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertConfigType.VALIDATED_SIGNAL)
            order('dateCreated', 'desc')
            maxResults(1)
        }

        boolean enableEndOfMilestone = SystemConfig.first()?.enableEndOfMilestone
        if (signal && undoableDisposition?.isEnabled) {
            try {
                // fetched validationDateStatusHistory before Updating signalStatusHistories instance
                SignalStatusHistory validationDateStatusHistory = signal?.signalStatusHistories.find{ it.signalStatus == Constants.VALIDATION_DATE }
                Disposition oldDisp = Disposition.findById(signal.disposition?.id)
                oldDispName = oldDisp?.displayName
                Disposition newDisposition = Disposition.findById(undoableDisposition.prevDispositionId)
                currDispName = newDisposition?.displayName
                Disposition defaultSignalDisposition = userService.getUser()?.getWorkflowGroup()?.defaultSignalDisposition
                undoableDisposition.isUsed = true
                Integer reviewPeriod = signal?.priority?.reviewPeriod
                String serverTimeZone = Holders.config.server.timezone
                List<String> criteriaList = signal.signalRMMs.sort { -it.dateCreated.time }.collect { it.criteria }
                criteriaList.removeAll { it == null }
                if (!criteriaList) {
                    signal.signalMemoFlagForUndoDis = false
                } else {
                    signal.signalMemoFlagForUndoDis = true
                }
                if(undoableDisposition.signalOutcomeId != null){
                    // removing auto-populated signal outcome
                    SignalOutcome autoSignalOutcome = SignalOutcome.get(undoableDisposition.signalOutcomeId)
                    signal.signalOutcomes.remove(autoSignalOutcome)
                }
                // customAuditProperties is transient field
                signal.disposition = newDisposition
                signal.customAuditProperties = ["justification": justification]
                // make sure both properties persist in same session
                createActivityForUndoAction(signal, oldDisp, justification)
                createActivityForDispositionUndo(currDispName, oldDispName, signal)
                Integer dispReviewPeriod = null
                signal.dispPerformedBy = undoableDisposition.prevDispPerformedBy
                deleteAutoPopulatedEntriesOnUndo(signal.id, oldDisp.id)
                List dispositionConfigs = cacheService.getDispositionConfigsByPriority(signal?.priority?.id)
                dispReviewPeriod = dispositionConfigs?.find { it -> it.disposition == newDisposition }?.reviewPeriod
                boolean dueInStartEnabled = Holders.config.dueInStart.enabled
                String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
                List<Map> signalHistoryList = generateSignalHistory(signal)
                SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDueInDisplay(true)
                SignalStatusHistory dueInStartBaseDate = signal.signalStatusHistories.find { it.signalStatus == dueInStartPoint }
                SystemConfig systemConfig = SystemConfig.first()
                List<String> dueInEndPoints = systemConfig?.selectedEndPoints?.split(",")?.collect { it } as List<String>
                Boolean endOfMilestone = systemConfig?.enableEndOfMilestone
                boolean enableSignalWorkflow = systemConfig?.enableSignalWorkflow
                Boolean isDispoStatusNullReviewComp = endOfMilestone && signal?.disposition?.isReviewCompleted() && signal?.disposition?.id != defaultSignalDisposition?.id
                boolean isCloseStatusExistInHistory = signalHistoryList?.find { it.signalStatus == signal.disposition?.signalStatusForDueDate } != null
                boolean defaultCloseCondition = signalHistoryList?.find { it.signalStatus in dueInEndPoints } != null
                boolean basicConditionToCloseSignal = defaultCloseCondition || (dueInEndPoints?.contains(signal?.workflowState)) || isCloseStatusExistInHistory || isDispoStatusNullReviewComp
                String dueDate = DateUtil.fromDateToString(signal.dueDate, DateUtil.DEFAULT_DATE_FORMAT)
                if (undoableDisposition?.isDueDateChanged && newDisposition?.reviewCompleted && newDisposition?.id != defaultSignalDisposition?.id) {
                    signal.actualDueDate = undoableDisposition.prevActualDueDate
                    signal.dueDate = undoableDisposition.prevDueDate
                } else if (defaultCloseCondition) {
                    signal.dueDate = null
                    // removed as required PVS-55078
                } else if (!undoableDisposition?.isDueDateChanged) {
                    if ((dueInStartEnabled && !dueInStartBaseDate)) {
                        signal.actualDueDate = undoableDisposition.prevActualDueDate
                        signal.dueDate = undoableDisposition.prevDueDate
                    } else if (newDisposition?.id != defaultSignalDisposition?.id && newDisposition?.reviewCompleted) {
                        signal.actualDueDate = undoableDisposition.prevActualDueDate
                        signal.dueDate = undoableDisposition.prevDueDate
                    } else if (enableSignalWorkflow && !dueInStartEnabled) {
                        if (basicConditionToCloseSignal) {
                            signal.actualDueDate = null
                            signal.dueDate = null
                        } else if (signalWorkflowState?.value == signal?.workflowState) {
                            if (dispReviewPeriod) {
                                reviewPeriod = dispReviewPeriod
                            }
                            Date date = new DateTime(signal.detectedDate,DateTimeZone.forID(serverTimeZone))?.plusDays(reviewPeriod)?.toDate()
                            signal.actualDueDate = date ? DateUtils.truncate(date, Calendar.DAY_OF_MONTH) : null
                            signal.dueDate = date ? DateUtils.truncate(date, Calendar.DAY_OF_MONTH) : null
                        }
                    } else {
                        if (dispReviewPeriod) {
                            reviewPeriod = dispReviewPeriod
                        }
                        Date date = new DateTime(signal.detectedDate,DateTimeZone.forID(serverTimeZone))?.plusDays(reviewPeriod)?.toDate()
                        signal.actualDueDate = date ? DateUtils.truncate(date, Calendar.DAY_OF_MONTH) : null
                        signal.dueDate = date ? DateUtils.truncate(date, Calendar.DAY_OF_MONTH) : null
                    }
                } else if (undoableDisposition.prevActualDueDate == null && undoableDisposition.prevDueDate == null) {
                    signal.isDueDateUpdated = false;
                    signal.actualDueDate = undoableDisposition.prevActualDueDate
                    signal.dueDate = undoableDisposition.prevDueDate
                }
                String updatedDueDate = DateUtil.fromDateToString(signal.dueDate, DateUtil.DEFAULT_DATE_FORMAT)
                if (dueDate != updatedDueDate && dueDate != null && updatedDueDate != null) {
                    createActivityForDueDate(dueDate, updatedDueDate, signal)
                }
                // set actual completion date null when target is not review completed
                if (enableEndOfMilestone && (newDisposition?.reviewCompleted == false || defaultSignalDisposition?.id == newDisposition?.id)) {
                    signal.milestoneCompletionDate = null
                } else if (enableEndOfMilestone && newDisposition?.reviewCompleted == true) {
                    signal.milestoneCompletionDate = undoableDisposition.prevMilestoneDate
                }
                // New activity logging for revert disposition
                List<String> validatedDateDispositions = Holders.config.alert.validatedDateDispositions
                boolean isValidatedDateSignal = validatedDateDispositions.contains(oldDisp.value)
                String defaultValidatedDate = Holders.config.signal.defaultValidatedDate
                if (isValidatedDateSignal) {
                    String currentValidationDate = DateUtil.fromDateToStringWithTimezone(validationDateStatusHistory?.dateCreated, DateUtil.DEFAULT_DATE_FORMAT, serverTimeZone)
                    String details = (defaultValidatedDate ?: "Validation Date") + " has been changed from '${currentValidationDate}' to '' -- with Justification '${justification}'"
                    saveActivityForSignalHistory(Constants.SYSTEM_USER, signal, details)
                }

                CRUDService.update(signal)
                CRUDService.update(undoableDisposition)

                UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: id, type: Constants.AlertConfigType.VALIDATED_SIGNAL])

                alertDueDateList << [id        : signal.id, dueDate: signal.dueDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(signal.dueDate) : null, dispositionId: signal.disposition.id,
                                     reviewDate: signal.reviewDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(signal.reviewDate) : null]


                createProductEventHistoryForDispositionChange(signal, justification, true)

                // delete auto populated entries

                log.info("Dispostion reverted successfully for alert Id: " + id)
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("some error occoured while reverting disposition")
            }
        }
        Map responseData = [alertDueDateList: alertDueDateList, prevDisposition: oldDispName, currentDisposition: currDispName]
        if (!undoableDisposition?.isDueDateChanged) {
            responseData << [dueDate: undoableDisposition.previousDueIn != null ? new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(signal.actualDueDate) : "", dueIn: undoableDisposition.previousDueIn]
        }
        if (signal.dueDate != null) {
            // calculating and returning actual due in (due_date-current_date)
            Integer dueInDays = DateUtils.truncate(new DateTime(signal.dueDate).toDate(), Calendar.DAY_OF_MONTH) - DateUtils.truncate(new DateTime().toDate(), Calendar.DAY_OF_MONTH)
            def dueDate = signal.dueDate!=null ? new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(signal.dueDate) : null
            // if due date is null then dueIn should be displayed as -
            responseData << [dueDate: dueDate, dueIn: dueDate ? dueInDays : null]
        }
        return responseData
    }

    def createActivityForUndoAction(ValidatedSignal signal, Disposition previousDisposition, String justification) {
        log.info("Creating Activity for reverting disposition")
        String details = "Undo Disposition"
        Map attr = [oldDisposition: previousDisposition.displayName, newDisposition: signal.disposition.displayName, signal: signal.name]
        activityService.createActivityForSignal(signal, justification, details,
                ActivityType.findByValue(ActivityTypeValue.UndoAction), userService.getUser(), userService.getUser(), attr)
    }


    void createActivityForDispositionChange(ValidatedSignal validatedSignal, Disposition previousDisposition, String justification) {
        String details = "Disposition changed from '${previousDisposition.displayName}' to '${validatedSignal.disposition.displayName}' for signal '${validatedSignal.name}'"
        Map attr = [oldDisposition: previousDisposition.displayName, newDisposition: validatedSignal.disposition.displayName, signal: validatedSignal.name]
        activityService.createActivityForSignal(validatedSignal, justification, details,
                ActivityType.findByValue(ActivityTypeValue.DispositionChange), userService.getUser(), userService.getUser(), attr)
    }

    void createProductEventHistoryForDispositionChange(ValidatedSignal validatedSignal, String justification, Boolean isUndo=false) {
        Map signalHistoryMap = [
                "disposition"    : validatedSignal.disposition,
                "change"         : isUndo ? Constants.HistoryType.UNDO_ACTION : Constants.HistoryType.DISPOSITION,
                "justification"  : justification,
                "validatedSignal": validatedSignal,
                "dueDate"        : validatedSignal.dueDate,
                "isUndo"         : isUndo
        ]
        if(!isUndo){
            signalHistoryService.saveSignalHistory(signalHistoryMap)
        }
    }

    List<String> listSignalAdvancedFilter() {
        List<String> validatedSignals = ValidatedSignal.createCriteria().list{
            projections{
                property("name")
                order("name", "asc")
            }
        } as List<String>
    }

    Boolean isFileAlreadyAttachedToSignal(ValidatedSignal validatedSignal, List<String> fileNames) {
        validatedSignal.getAttachments().collect {
            it.name + "." + it.ext
        }.intersect(fileNames) as Boolean
    }

    List isFileSavedNameAlreadyAttachedToSignal(ValidatedSignal validatedSignal, List<String> fileNames) {
        validatedSignal.getAttachments().collect {
            it.savedName
        }.intersect(fileNames)
    }

    String generateNewFileName(ValidatedSignal validatedSignal,String fileName)   {
        int extensionIndex = fileName.lastIndexOf(".")
        String extension = fileName.substring(extensionIndex)
        String result = fileName
        if(fileName.charAt(extensionIndex-1)==")" && fileName.contains("(")){
            int startIndex = (fileName.substring(0,extensionIndex)).lastIndexOf("(")
            if(startIndex < extensionIndex && fileName.substring(startIndex+1,extensionIndex-1).isInteger()){
                int sequence = fileName.substring(startIndex+1,extensionIndex-1).toInteger()+1
                result = fileName.substring(0,startIndex)+"(${sequence})${extension}"
            }
        } else {
            result = fileName.substring(0,extensionIndex)+"(1)${extension}"
        }
        List nameList = isFileSavedNameAlreadyAttachedToSignal(validatedSignal,[result])
        if (nameList.size()>=1){
            result = generateNewFileName(validatedSignal,nameList.get(0))
        }
        return result
    }

    String generateNewReferenceFileName(String fileName)   {
        int extensionIndex = fileName.lastIndexOf(".")
        String extension = fileName.substring(extensionIndex)
        String result = fileName
        if(fileName.charAt(extensionIndex-1)==")" && fileName.contains("(")){
            int startIndex = (fileName.substring(0,extensionIndex)).lastIndexOf("(")
            if(startIndex < extensionIndex && fileName.substring(startIndex+1,extensionIndex-1).isInteger()){
                int sequence = fileName.substring(startIndex+1,extensionIndex-1).toInteger()+1
                result = fileName.substring(0,startIndex)+"(${sequence})${extension}"
            }else{
                result = fileName.substring(0,extensionIndex)+"(1)${extension}"
            }
        } else {
            result = fileName.substring(0,extensionIndex)+"(1)${extension}"
        }
        return result
    }
    Map toAggEvdasAlertMap(def alert, Boolean isAggregateCaseAlert) {
        ExecutedConfiguration exConfiguration = isAggregateCaseAlert ? alert.executedAlertConfiguration : null
        String dataSource = isAggregateCaseAlert ? exConfiguration?.selectedDatasource : null
        [
                id             : alert.id,
                alertName      : alert.name,
                productId      : isAggregateCaseAlert ? alert.productId : alert.substanceId,
                level          : getLevel(alert.executedAlertConfiguration.productSelection),
                productName    : isAggregateCaseAlert ? alert.productName : alert.substance,
                preferredTerm  : alert.pt,
                newCount1      : isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "newCount"  , Constants.Commons.UNDEFINED_NUM_INT_REVIEW) : alert.newEv,
                cumCount1      : isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "cummCount"  , Constants.Commons.UNDEFINED_NUM_INT_REVIEW): alert.totalEv,
                newSeriousCount: isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "newSeriousCount"  , Constants.Commons.UNDEFINED_NUM_INT_REVIEW) : alert.newSerious,
                cumSeriousCount: isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "cumSeriousCount"  , Constants.Commons.UNDEFINED_NUM_INT_REVIEW) : alert.totalSerious,
                execConfigId   : alert.executedAlertConfigurationId,
                alertConfigId  : alert.alertConfigurationId,
                ptCode         : alert.ptCode,
                prrValue       : isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "prrValue"  , Constants.Commons.UNDEFINED_NUM_DOUBLE) : '-',
                rorValue       : isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "rorValue"  , Constants.Commons.UNDEFINED_NUM_DOUBLE) : alert.rorValue,
                ebgm           : isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "ebgm"  , Constants.Commons.UNDEFINED_NUM_DOUBLE) : '-',
                eb05           : isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "eb05"  , Constants.Commons.UNDEFINED_NUM_DOUBLE) : '-',
                eb95           : isAggregateCaseAlert ? fetchAggregateData(alert , dataSource , "eb95"  , Constants.Commons.UNDEFINED_NUM_DOUBLE) : '-',
                assignedTo     : alert.assignedToId,
                soc            : alert.soc,
                sdr            : alert.hasProperty('sdr') ? alert.sdr : Constants.Commons.DASH_STRING,
                dmeIme         : alert.hasProperty('dmeIme') ? alert.dmeIme : Constants.Commons.DASH_STRING,
                listed         : alert.hasProperty('listed') ? fetchAggregateData(alert , dataSource , "listed"  , Constants.Commons.UNDEFINED_NUM_INT_REVIEW): Constants.Commons.DASH_STRING
        ]
    }

    String fetchProductDictionarySelection(def alert) {
        String productionDictSelection = null
        Class clazz = alert.class
        switch (clazz) {
            case AdHocAlert:
                productionDictSelection = alert?.productDictionarySelection
                break
            case SingleCaseAlert:
                productionDictSelection = dataObjectService.getDictionaryProductLevel()
                break
            case EvdasAlert :
            case AggregateCaseAlert:
                productionDictSelection = alert?.executedAlertConfiguration?.productSelection && alert?.executedAlertConfiguration?.productSelection != '' ? SignalUtil.getDictionarySelectionType(SignalUtil.parseJsonString(alert?.executedAlertConfiguration?.productSelection)): null
                break
        }
        productionDictSelection
    }


    List<Map> getAlertValidatedSignalList(List<Long> alertIdList, def domain) {
        List<Map> results = []
        alertIdList.collate(Constants.AggregateAlertFields.BATCH_SIZE).each { batchIds ->
            List<Map> batchResult = domain.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("id", "id")
                    'validatedSignals' {
                        property("id", "signalId")
                        property("name", "name")
                        property("disposition", "disposition")
                    }
                }
                or {
                    batchIds.collate(1000).each {
                        'in'('id', it)
                    }
                }
            } as List<Map>
            results.addAll(batchResult)
            sessionFactory.currentSession.clear()
        }
        results
    }

    List<Map> getValidatedSignalMultipleValuesList(List<Long> signalIdList, String propertyValue) {
        List<Map> results = ValidatedSignal.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                "$propertyValue" {
                    property("id", "topic")
                    property("name", "name")
                }
            }
            or {
                signalIdList.collate(1000).each {
                    'in'('id', it)
                }
            }
        } as List<Map>
        results
    }

    /**
     * Saves the signal status history.
     *
     * @param params              A map containing the parameters for saving the signal status history.
     * @param isDispositionUpdated A boolean indicating if the disposition has been updated.
     * @param isWorkflowUpdate     A boolean indicating if this is a workflow update. Default is false.
     * @param isReset              A boolean indicating if the signal status is being reset. Default is false.
     * @param retainDueDate        A boolean indicating if the due date should be retained. Default is false.
     * @return                     An Integer
     */
    Integer saveSignalStatusHistory(Map params, Boolean isDispositionUpdated, Boolean isWorkflowUpdate = false, Boolean isReset = false, Boolean retainDueDate = false, Boolean updateDetectedDateActivity = false,  Boolean isUpdate = false) {
        boolean workflowChange = false
        SignalStatusHistory signalStatusHistory
        Boolean isDateChange
        Boolean isCommentChange
        Boolean isSignalStatusChange
        String oldCreatedDate
        String userTimeZone = userService.getUser()?.preference?.timeZone
        String presentDate = DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_TIME_FORMAT, Holders.config.server.timezone)
        ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId as Long)
        String previousDueDate = DateUtil.fromDateToString(validatedSignal.actualDueDate, DEFAULT_DATE_FORMAT)
        String presentCreatedDate = presentDate ? DateUtil.stringFromDate(new Date(presentDate), DEFAULT_TIME_FORMAT, Holders.config.server.timezone) : null
        params.createdDate = params.createdDate ? params.createdDate + " " + presentCreatedDate : null
        Date newCreatedDate = params.createdDate ? DateUtil.parseDate(params.createdDate, DEFAULT_DATE_TIME_FORMAT) : null
        signalStatusHistory = params.signalHistoryId ? SignalStatusHistory.get(params.signalHistoryId as Long) : new SignalStatusHistory()
        String previousDate1 = DateUtil.fromDateToString(signalStatusHistory?.dateCreated, DEFAULT_DATE_FORMAT)
        params.previousDate1 = (params.signalStatus == Constants.WorkFlowLog.DUE_DATE) ? previousDueDate : previousDate1
        oldCreatedDate = signalStatusHistory.dateCreated ? DateUtil.stringFromDate(signalStatusHistory.dateCreated, DEFAULT_DATE_FORMAT, Holders.config.server.timezone) : null
        String prevComment = signalStatusHistory.statusComment && !signalStatusHistory.statusComment?.equals('') ? signalStatusHistory.statusComment.trim() : ''
        String newComment = params.statusComment ? params.statusComment?.toString().trim() : ''
        isCommentChange = !prevComment.equals(newComment)
        isDateChange = newCreatedDate != signalStatusHistory.dateCreated
        String prevStatus = signalStatusHistory.signalStatus
        isSignalStatusChange = !params.signalStatus.equals(signalStatusHistory.signalStatus)
        SystemConfig systemConfig = SystemConfig.first()
        List<String> dueInEndPoints = systemConfig.selectedEndPoints?.split(",").collect { it } as List<String>
        if (!(params.signalStatus.equalsIgnoreCase(Constants.WorkFlowLog.DATE_CLOSED)) && signalStatusHistory?.signalStatus?.equalsIgnoreCase(Constants.WorkFlowLog.DATE_CLOSED)) {
            validatedSignal.signalStatus = Constants.ONGOING_SIGNAL
            validatedSignal.actualDateClosed = null
        }
        if (params.signalStatus == Constants.WorkFlowLog.DATE_CLOSED) {
            validatedSignal.signalStatus = Constants.WorkFlowLog.DATE_CLOSED
            validatedSignal.actualDateClosed = newCreatedDate
        }
        if (newCreatedDate) {
            signalStatusHistory.dateCreated = newCreatedDate
        }
        if (!isDispositionUpdated || !newCreatedDate) {
            signalStatusHistory.dateCreated = new Date(DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_TIME_FORMAT, Holders.config.server.timezone))
        }
        signalStatusHistory.fromSignalStatus = params.fromSignalStatus
        if(isWorkflowUpdate) {
            if(signalWorkflowService.isCalculateDueInChecked(params.signalStatus)){
                validatedSignal.wsUpdated = new Date(DateUtil.fromDateToStringWithTimezone(new Date(),DEFAULT_DATE_FORMAT,Holders.config.server.timezone))
            }
            signalStatusHistory.fromSignalStatus = params?.signalStatusStart
        }
        signalStatusHistory.statusComment = params.statusComment ?: ""
        String userName
        if(Constants.WorkFlowLog.DUE_DATE.equalsIgnoreCase(params.signalStatus)) {
            signalStatusHistory.performedBy = Constants.SYSTEM_USER
            signalStatusHistory.skipAudit = true
            userName=Constants.SYSTEM_USER
        }else{
            if (params.isSystemUser == true) {
                signalStatusHistory.isSystemUser = true
                signalStatusHistory.performedBy = Constants.SYSTEM_USER
                userName= Constants.SYSTEM_USER
            }else{
                signalStatusHistory.performedBy = params.assignedTo ?: userService.getUserFromCacheByUsername(userService.getCurrentUserName()?:Constants.SYSTEM_USER)?.fullName
                userName=User.get(userService.getCurrentUserId())?.username ?: Constants.SYSTEM_USER
            }

        }
        if(params.isWorkflowUpdated && !isSignalStatusChange){
            signalStatusHistory.currentDispositionId = params.currentDispositionId?:signalStatusHistory.currentDispositionId?:null
        }else{
            signalStatusHistory.currentDispositionId = params.currentDispositionId?:null
        }
        signalStatusHistory.signalStatus = params.signalStatus
        signalStatusHistory.dispositionUpdated = isDispositionUpdated
        signalStatusHistory.isAutoPopulate = params.isAutoPopulate ?: false
        if (isWorkflowUpdate){
            signalStatusHistory.isAutoPopulate = true
        }
        signalStatusHistory.currentDispositionId = (params.signalHistoryId && !isSignalStatusChange && signalStatusHistory.currentDispositionId) ? signalStatusHistory.currentDispositionId :  (params.currentDispositionId?:null)
        signalStatusHistory.dueDate = params.dueDate?:null
        signalStatusHistory.signalId=validatedSignal.id
        signalStatusHistory.updateTime = params.updationTime? params.updationTime as long: null
        if (params.signalStatus.contains(Holders.config.signal.defaultValidatedDate) && Holders.config.detectedDateAndValidationDate.synch.enabled) {
            validatedSignal.detectedDate=newCreatedDate
            validatedSignal.isSystemUser = true
        }
        if (!((grailsApplication.config.pvsignal.showAutoGeneratedHistory || signalStatusHistory.dispositionUpdated) && (signalStatusHistory.signalStatus!=Constants.SignalHistory.SIGNAL_CREATED))) {
            //this block is added to handle audit entry on flag basis same condition is used for listing of signal work flow log
            signalStatusHistory.skipAudit=true
        }
        if (params.signalHistoryId) {
            String details = ""
            if (isDispositionUpdated) {
                if (isDateChange) {
                    String createDate = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(new Date(params.createdDate))
                    if (signalStatusHistory.isAutoPopulate || signalStatusHistory.performedBy == Constants.Commons.SYSTEM) {
                        createDate = params.createdDate ? fromDateToStringWithTimezone(new Date(params.createdDate), DEFAULT_DATE_FORMAT, userTimeZone) : ""
                    }
                    if (Holders.config.detectedDateAndValidationDate.synch.enabled && params.signalStatus == Holders.config.signal.defaultValidatedDate && params.previousDate1 != createDate) {
                        if (params.previousDate1) {
                            details = "'${params.signalStatus}' is updated with the Detected Date, '${params.signalStatus}' changed from '${params.previousDate1}' to '${new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(new Date(params.createdDate))}'"
                        } else {
                            details = "'${params.signalStatus}' is updated with the Detected Date, '${params.signalStatus}' updated to '${new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(new Date(params.createdDate))}'"
                        }
                    } else if (params.previousDate1 != createDate) {
                        if (params.previousDate1) {
                            details = "${params.signalStatus} has been changed from '${params.previousDate1}' to '${createDate}' -- with Justification '${params.statusComment}'"
                        } else {
                            details = "${params.signalStatus} has been updated to '${createDate}' -- with Justification '${params.statusComment}'"
                        }
                    }
                }
                if (isCommentChange) {
                    if (prevComment != '') {
                        details += "Comment for ${params.signalStatus} has been changed from '${prevComment}' to '${newComment}'"
                    } else {
                        details += "Comment '${newComment}' for ${params.signalStatus} has been added"
                    }
                }
                if (isSignalStatusChange) {
                    if (isCommentChange) {
                        details += " <br>"
                    }
                    if (prevStatus != ''){
                        details += "Status has been changed from '${prevStatus}' to '${params.signalStatus}'"
                    }
                    else{
                        details += "Status '${params.signalStatus}' added"
                    }
                }
            }
            if (details) {
                details += (params?.fromSignalStatus && params?.fromSignalStatus != "undefined") ? (", From State = ${params.fromSignalStatus}") : ("")
                saveActivityForSignalHistory(userName ,validatedSignal, details)
            }
            signalStatusHistory?.save(flush: true, failOnError: true)
            SignalStatusHistory.executeUpdate("update SignalStatusHistory set dateCreated=:dateC where Id=:id", [dateC: signalStatusHistory.dateCreated, id: signalStatusHistory.id])
             if(dueInEndPoints.contains(params.signalStatus)) {
                validatedSignal.signalStatus =params.signalStatus;
                validatedSignal.dueDate=null
                CRUDService.update(validatedSignal)
            }

        } else {
            validatedSignal.addToSignalStatusHistories(signalStatusHistory)
            if (isDispositionUpdated && !isWorkflowUpdate) {
                String details
                String createDate = params.createdDate ? new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(new Date(params.createdDate)) : ""
                if (signalStatusHistory.isAutoPopulate || signalStatusHistory.performedBy == Constants.Commons.SYSTEM) {
                    createDate = params.createdDate ? fromDateToStringWithTimezone(new Date(params.createdDate), DEFAULT_DATE_FORMAT, userTimeZone) : ""
                }
                if (Holders.config.detectedDateAndValidationDate.synch.enabled && params.signalStatus == Holders.config.signal.defaultValidatedDate && params.previousDate1 != createDate) {
                    if(params.previousDate1){
                        details = "'${params.signalStatus}' is updated with the Detected Date, '${params.signalStatus}' changed from '${params.previousDate1 ? params.previousDate1 : ""}' to '${new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(new Date(params.createdDate))}'"
                    }
                    else{
                        details = "'${params.signalStatus}' is updated with the Detected Date, '${params.signalStatus}' updated to '${new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(new Date(params.createdDate))}'"
                    }
                } else if (params.previousDate1 != createDate) {
                    if (params.signalStatus == Constants.WorkFlowLog.DUE_DATE) {
                        if (createDate) {
                            details = "${params.signalStatus} has been changed from '${createDate}' to '${previousDueDate}' -- with Justification '${params.statusComment}'"
                        } else if(!params.notCreated){
                            // added condition for PVS-69988- Activity for Due date should not be created at signal creation
                            details = "${params.signalStatus} '${previousDueDate}' has been added  -- with Justification '${params.statusComment}'"
                        }

                    } else {
                        if (params.previousDate1) {
                            details = "${params.signalStatus} has been changed from '${params.previousDate1}' to '${createDate}' -- with Justification '${params.statusComment}'"
                        } else {
                            details = "${params.signalStatus} '${createDate}' has been added  -- with Justification '${params.statusComment}'"
                        }
                    }
                }
                if (details) {
                    details += (params?.fromSignalStatus && params?.fromSignalStatus != "undefined") ? (", From State = ${params.fromSignalStatus}") : ("")
                    saveActivityForSignalHistory(userName, validatedSignal, details)
                }
            }
            if (isWorkflowUpdate) {
                String details
                if (params.signalStatusStart) {
                    details = "Workflow state has been changed from '${params.signalStatusStart}' to '${params.signalStatus}'"
                } else {
                    details = "Workflow state '${params.signalStatus}' has been added"
                }

                workflowChange = true
                saveActivityForSignalHistory(userName, validatedSignal, details)
            }
            if (dueInEndPoints.contains(params.signalStatus)) {
                validatedSignal.signalStatus = params.signalStatus
                validatedSignal.dueDate = null;
            }

            CRUDService.update(validatedSignal)
        }
        setMileStoneCompletionDateForSignal(validatedSignal)
        List<Map> signalHistoryList = generateSignalHistory(validatedSignal)
        if (validatedSignal.dueDate == null && signalStatusHistory.isAutoPopulate) {
            return null
        }
        Integer dueIn = new DateTime(validatedSignal.dueDate).toDate().clearTime() - new DateTime().toDate().clearTime() ?: null
        return retainDueDate ? dueIn : calculateDueIn(validatedSignal.id, params.signalStatus, false, false, isReset, false, updateDetectedDateActivity, isUpdate)
    }

    def setMileStoneCompletionDateForSignal(ValidatedSignal validatedSignal){
        List<Map> signalHistoryList = validatedSignal.signalStatusHistories as List<Map>
        SystemConfig systemConfig = SystemConfig.first()
        List<String> dueInEndPoints = systemConfig.selectedEndPoints?.split(",").collect {it} as List<String>
        if(!systemConfig.enableEndOfMilestone) {
            validatedSignal.milestoneCompletionDate = signalHistoryList.findAll { it.signalStatus in dueInEndPoints } != [] ? signalHistoryList.findAll { it.signalStatus in dueInEndPoints }?.sort { it.dateCreated }?.dateCreated?.first() : null
            if (validateRequiredFields(validatedSignal)) {
                CRUDService.update(validatedSignal)
            }
        }
    }

    def setDueDateForSignal(ValidatedSignal validatedSignal){
        List<Map> signalHistoryList = validatedSignal.signalStatusHistories as List<Map>
        SystemConfig systemConfig = SystemConfig.first()
        List<String> dueInEndPoints = systemConfig.selectedEndPoints?.split(",").collect {it} as List<String>
        validatedSignal.dueDate = signalHistoryList.findAll { it.signalStatus in dueInEndPoints } != [] ? null : validatedSignal.dueDate
        validatedSignal.dueDate = signalHistoryList.findAll { it.signalStatus in dueInEndPoints } != [] ? null : validatedSignal.dueDate
        if (validateRequiredFields(validatedSignal)) {
            CRUDService.update(validatedSignal)
        }
    }

    void saveActivityForSignalHistory(String userName,ValidatedSignal validatedSignal,String details,Boolean workflowChange = false) {
        ActivityType activityType
        if(workflowChange){
            activityType = ActivityType.findByValue(ActivityTypeValue.WorkflowStateChange)
        } else {
            activityType = ActivityType.findByValue(ActivityTypeValue.StatusDate)
        }
        if (activityType && validatedSignal) {
            Activity activity = new Activity(
                    type: activityType,
                    performedBy: User.findByUsername(userName),
                    timestamp: DateTime.now(),
                    assignedTo: validatedSignal.assignedTo,
                    details: details
            )
            validatedSignal.addToActivities(activity)
            validatedSignal.save(flush: true, failOnError: false)
        }
    }

    List<Map> generateSignalHistory(ValidatedSignal validatedSignal) {
        List<Map> signalStatusHistoryMapList = []
        List<SignalStatusHistory> signalStatusHistories = new ArrayList<>(validatedSignal?.signalStatusHistories)
        // Custom comparator for sorting
        def customComparator = { sigStatusHist1, sigStatusHist2 ->

            // Perform null checks
            if (sigStatusHist1 == null && sigStatusHist2 == null) {
                return 0
            } else if (sigStatusHist1 == null) {
                return -1
            } else if (sigStatusHist2 == null) {
                return 1
            }

            // Compare by dateCreated
            int dateComparison = 0
            if (null != sigStatusHist1.dateCreated && null != sigStatusHist2.dateCreated) {
                dateComparison = sigStatusHist1.dateCreated.compareTo(sigStatusHist2.dateCreated)
            } else if (null == sigStatusHist1.dateCreated) {
                return -1
            } else if (null == sigStatusHist2.dateCreated) {
                return 1
            }

            if (dateComparison != 0) {
                // If dateCreated is not the same, return the result of date comparison
                return dateComparison
            } else {
                // If dateCreated is the same, compare by name
                return sigStatusHist1.signalStatus.compareTo(sigStatusHist2.signalStatus)
            }
        }
        List<SignalStatusHistory> signalStatusHistoryList = signalStatusHistories?.sort(customComparator)

        boolean dueDateFirstEntry = false
        signalStatusHistoryList?.each {
            if (!dueDateFirstEntry && it?.signalStatus == Constants.WorkFlowLog.DUE_DATE) {
                dueDateFirstEntry = true
            } else if (dueDateFirstEntry && it?.signalStatus == Constants.WorkFlowLog.DUE_DATE) {
                signalStatusHistoryList = signalStatusHistoryList - it
            }
        }

        String validationDateStr = Holders.config.signal.defaultValidatedDate
        boolean validationDateFirstEntry = false
        signalStatusHistoryList?.each {
            if (!validationDateFirstEntry && it?.signalStatus == validationDateStr) {
                validationDateFirstEntry = true
            } else if (validationDateFirstEntry && it?.signalStatus == validationDateStr) {
                signalStatusHistoryList = signalStatusHistoryList - it
            }
        }

        for(int i=0;i<signalStatusHistoryList?.size();i++){
            if(!SystemConfig.first().displayDueIn && signalStatusHistoryList?.get(i)?.signalStatus?.equals(Constants.WorkFlowLog.DUE_DATE))
            {

                signalStatusHistoryList.remove(i)
            }
        }
        if (signalStatusHistoryList) {
            List<String> signalStatusList = alertAttributesService.get('signalHistoryStatus') as List<String>
            if(SystemConfig.first().displayDueIn)
            {
                signalStatusList?.add(Constants.WorkFlowLog.DUE_DATE)
            }
            List<String> selectedStatusList = signalStatusHistoryList.collect { it.signalStatus }
            List<String> availableStatusList = signalStatusList?.minus(selectedStatusList)
            Boolean isAddRow = (availableStatusList?.size() ?: 0) > 0
            signalStatusHistoryMapList = signalStatusHistoryList.collect {
                Date dateCreated
                if(it.signalStatus.equals(Constants.WorkFlowLog.DUE_DATE)){
                    dateCreated=validatedSignal.actualDueDate
                }else{
                    dateCreated=it.dateCreated
                }
                if ((grailsApplication.config.pvsignal.showAutoGeneratedHistory || it.dispositionUpdated) && (it.signalStatus!=Constants.SignalHistory.SIGNAL_CREATED)) {
                    [signalStatus : it.signalStatus, dateCreated: dateCreated, dispositionUpdated: it.dispositionUpdated, performedBy: it.performedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:it.performedBy,
                     statusComment: it.statusComment, signalStatusList: ([it.signalStatus]).plus(availableStatusList), isAddRow: isAddRow, id: it.id , fromSignalStatus: it.fromSignalStatus ?: "", isAutoPopulate: it.isAutoPopulate]
                }

            } - null
            signalStatusHistoryMapList.sort(customComparator)
        }
        signalStatusHistoryMapList
    }

    List<String> convertStringToList(def object){
        if (object.getClass() == String) {
            object = [object] as List<String>
        }
        object
    }

    void assignAllEventAndProductValues(ValidatedSignal validatedSignal) {
        try{
            //product and product group breakdown with hierarchy number
            List<String> allProductList = getAllProductsWithHierarchy(validatedSignal)
            validatedSignal.allProducts = allProductList?.unique()
            //Events and Event group breakdown with hierarchy number
            Map<String, List<String>> allEventList = getAllEventsWithHierarchy(validatedSignal)
            String allEventsString = allEventList?.eventListHierarchy?.unique() as String
            String allEventsWithoutHierarchy = allEventList?.eventList?.unique() as String
            if (allEventsString && allEventsString.length() > 0) {
                allEventsString = allEventsString.substring(1, allEventsString.length() - 1)
            }
            if (allEventsWithoutHierarchy && allEventsWithoutHierarchy.length() > 0) {
                allEventsWithoutHierarchy = allEventsWithoutHierarchy.substring(1, allEventsWithoutHierarchy.length() - 1)
            }
            if (validatedSignal.events) {
                String allSmqString = getEventFromJsonWithSmq(validatedSignal.events)?.unique() as String
                if (allSmqString && allSmqString.length() > 0) {
                    allSmqString = allSmqString.substring(1, allSmqString.length() - 1)
                }
                validatedSignal.allSmqs = allSmqString
            }
            validatedSignal.allEvents = allEventsString
            validatedSignal.allEventsWithoutHierarchy = allEventsWithoutHierarchy
        }catch(Exception ex){
            ex.printStackTrace()
        }

    }


    ValidatedSignal createSignalForBusinessConfiguration(String signalName, def alert, String alertType,
                                                         Disposition defaultSignalDisposition, String productJson = null,
                                                         String eventJSON = null , String productGroupJson = null,
                                                         Long signalId, boolean  isValidatedDate,boolean isNonCompany=false, Boolean adhocSignalCheck = false) {
        ValidatedSignal validatedSignal = null
        Date lastDispChange = null
        if (signalId != null) {
            validatedSignal = ValidatedSignal.findById(signalId)
        }
        String defaultValidatedDate = Holders.config.signal.defaultValidatedDate
        User user = userService.getUser()
        List<SignalStatusHistory> listSignalHistory=[]
        Long signalHistoryId = null
        if (null != validatedSignal) {
            listSignalHistory = validatedSignal.getSignalStatusHistories()
            if (listSignalHistory.size() > 0) {
                signalHistoryId = listSignalHistory.get(0).id
            }
        }
        if (!validatedSignal) {
            validatedSignal = new ValidatedSignal()
            validatedSignal.name = signalName
            validatedSignal.products = productJson ?: alert.executedAlertConfiguration.productSelection
            validatedSignal.assignmentType = "USER"
            validatedSignal.assignedTo = alert.assignedTo
            validatedSignal.assignedToGroup = alert.assignedToGroup
            validatedSignal.assignmentType = Constants.AssignmentType.USER
            validatedSignal.disposition = defaultSignalDisposition
            validatedSignal.priority = Priority.findByDefaultPriority(true)
            validatedSignal.createdBy = user ? user.username : Constants.Commons.SYSTEM
            validatedSignal.modifiedBy = user ? user.username : Constants.Commons.SYSTEM
            validatedSignal.signalStatus = Constants.ONGOING_SIGNAL
            validatedSignal.workflowGroup = user ? user.workflowGroup
                    : (alert.assignedTo ? cacheService.getUserByUserId(alert.assignedTo.id)?.workflowGroup
                    : userService.getUserFromCacheByUsername(Constants.SYSTEM_USER)?.workflowGroup)
            validatedSignal.dateCreated = new Date(DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_FORMAT, Holders.config.server.timezone))
            validatedSignal.lastUpdated = new Date()
            validatedSignal.detectedDate = validatedSignal.dateCreated
            validatedSignal.productDictionarySelection = fetchProductDictionarySelection(alert)
            validatedSignal.events = eventJSON
            validatedSignal.productGroupSelection = productGroupJson
            validatedSignal.workflowState = signalWorkflowService.defaultSignalWorkflowState()
            validatedSignal.lastDispChange = new Date()
            boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
            if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT || alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                validatedSignal.isMultiIngredient = alert.executedAlertConfiguration?.isMultiIngredient
            } else {
                validatedSignal.isMultiIngredient = false
            }
            if (enableSignalWorkflow && signalWorkflowService.defaultSignalWorkflowState() == signalWorkflowService.calculateDueInSignalWorkflowState()) {
                validatedSignal.wsUpdated = new Date()
            }
            validatedSignal.eventGroupSelection = eventJSON == null ? alert.executedAlertConfiguration.eventGroupSelection : null
            assignAllEventAndProductValues(validatedSignal)
            calcDueDate(validatedSignal, validatedSignal.priority, validatedSignal.disposition, false,
                    cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id))
            Group allUsers = Group.findByName('All Users')
            if (Holders.config.validatedSignal.shareWith.enabled && allUsers) {
                validatedSignal.addToShareWithGroup(allUsers)
            }
            validatedSignal.productsAndGroupCombination = alertService.productSelectionSignal(validatedSignal)
            validatedSignal.eventsAndGroupCombination = alertService.eventSelectionSignalWithSmq(validatedSignal)

            // Passing product details as NULL if the product name is Blinded or Redacted for the user
            if (user && alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                setProductValuesForBlindedData(validatedSignal, user, alert)
            }
            saveSignal(validatedSignal, true, true)
            log.info("Signal: " + validatedSignal)
            if(adhocSignalCheck) {
                activityService.createActivityForSignal(validatedSignal, '', "Signal '${validatedSignal?.name}' created", ActivityType.findByValue(ActivityTypeValue.SignalCreated), validatedSignal.assignedTo,
                        userService.getUser(), [signal: validatedSignal.name], validatedSignal.assignedToGroup)
            }
            String createdDate = Holders.config.detectedDateAndValidationDate.synch.enabled
                    ? DateUtil.fromDateToString(validatedSignal.detectedDate,DateUtil.DEFAULT_DATE_FORMAT)
                    : DateUtil.fromDateToStringWithTimezone(new Date(), DateUtil.DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
            log.info("isValidatedDate: " + isValidatedDate)
            if (isValidatedDate) {
                saveSignalStatusHistory([signalStatus   : (defaultValidatedDate) ? defaultValidatedDate : Constants.WorkFlowLog.VALIDATION_DATE,
                                         statusComment  : Constants.WorkFlowLog.VALIDATION_STATUS_COMMENT,
                                         signalId       : validatedSignal.id,
                                         assignedTo     : alert.assignedTo,
                                         signalHistoryId: signalHistoryId,
                                         "createdDate"  : createdDate,
                                         "isSystemUser" : true], true)
            } else {
                saveSignalStatusHistory([signalStatus : Constants.SignalHistory.SIGNAL_CREATED,
                                         statusComment: Constants.SignalHistory.SIGNAL_CREATED,
                                         signalId     : validatedSignal.id,
                                         assignedTo   : alert.assignedTo], false)
            }

            log.info("Signal History saved")

            boolean dueInStartEnabled = Holders.config.dueInStart.enabled
            String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
            if (enableSignalWorkflow) {
                dueInStartEnabled ? calculateDueIn(validatedSignal.id, dueInStartPoint) : calculateDueIn(validatedSignal.id, validatedSignal.workflowState)
            } else {
                dueInStartEnabled ? calculateDueIn(validatedSignal.id, dueInStartPoint) : calculateDueIn(validatedSignal.id, defaultValidatedDate)
            }
            //PVS-61523: Due Date Changes status is removed from new signal creation


        } else {
            if (listSignalHistory.size()>0) {
                SignalStatusHistory signalStatusHistory = listSignalHistory.get(0)
                if ("New Signal Created".equalsIgnoreCase(signalStatusHistory.signalStatus) && isValidatedDate) {
                    signalStatusHistory.statusComment = Constants.WorkFlowLog.VALIDATION_STATUS_COMMENT
                    signalStatusHistory.signalStatus = (defaultValidatedDate) ? defaultValidatedDate : Constants.WorkFlowLog.VALIDATION_DATE
                    signalStatusHistory.isSystemUser = true
                    signalStatusHistory.performedBy = Constants.SYSTEM_USER
                    String formatedDate = Holders.config.detectedDateAndValidationDate.synch.enabled
                            ? DateUtil.fromDateToString(validatedSignal.detectedDate, DateUtil.DEFAULT_DATE_FORMAT)
                            : DateUtil.fromDateToStringWithTimezone(new Date(), DateUtil.DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
                    String details = "Validation Date has been changed from '' to '${formatedDate}' -- with Justification '${signalStatusHistory.statusComment}'"
                    saveActivityForSignalHistory(Constants.SYSTEM_USER, validatedSignal, details)
                    signalStatusHistory.dispositionUpdated = true
                    CRUDService.update(signalStatusHistory)
                }
            }
        }
        validatedSignal
    }

    Boolean allowedDictionarySelectionByString(String productSelection) {
        def jsonSlurper = new JsonSlurper()
        if (productSelection) {
            def productSelectionObj = jsonSlurper.parseText(productSelection)
            //Determine if user has selected multiple dictionary values.
            //In case of the same, flow is returned back
            int selectionCheck = 0
            productSelectionObj.each { k, v->
                if (!v.isEmpty()) {
                    selectionCheck = selectionCheck + 1
                }
            }
            if (selectionCheck > 1) {
                return false
            }
            return true
        } else {
            return true
        }
    }

    List fetchAttachments(Long alertId){
        ValidatedSignal validatedSignal = ValidatedSignal.findById(alertId.toInteger())
        String timezone = userService.user.preference.timeZone
        Sql sql = new Sql(dataSource)
        String sqlQuery = "select id from attachment where lnk_id in (select id from attachment_link where reference_id = ${alertId}) and attachment_type = 'Attachment'"
        List<Attachment> attachmentList = []
        sql.eachRow(sqlQuery){ row ->
            attachmentList.add(Attachment.get(row.id as Long))
        }
        List attachments = attachmentList.collect {
            AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(it)
            [
                    id         : it.id,
                    link       : it.inputName,
                    savedName  : it.savedName,
                    type       : it.attachmentType.id,
                    referenceType: it.referenceType,
                    description: attachmentDescription?.description ?: Constants.Commons.BLANK_STRING,
                    timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timezone),
                    modifiedBy : attachmentDescription?.createdBy
            ]
        }
        String sqlQuery2 = "select id from attachment where lnk_id in (select id from attachment_link where reference_id = ${alertId}) and attachment_type = 'Reference'"
        List<Attachment> referenceList = []
        sql.eachRow(sqlQuery2){ row ->
            referenceList.add(Attachment.get(row.id as Long))
        }
         List references = referenceList.collect {
            AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(it)
            [
                    id         : it.id,
                    inputName  : it.inputName,
                    link       : it.referenceLink,
                    savedName  : it.savedName,
                    referenceType: it.referenceType,
                    type       : it.attachmentType.id,
                    description: attachmentDescription?.description ?: Constants.Commons.BLANK_STRING,
                    timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timezone),
                    modifiedBy : attachmentDescription?.createdBy
            ]
        }

        attachments.addAll(references)
        sql?.close()

        return attachments
    }

    boolean isProductAllowAsSafetyLead(List<String> productIdList){
        List <String> allowedProductIdList = []
        List <String> allowedProducts = []
        Boolean result
        Set<SafetyGroup> safetyGroups = User.get(userService.getCurrentUserId()).safetyGroups

        safetyGroups?.each {
            allowedProducts.addAll(it.allowedProductList)
            allowedProductIdList.addAll(getAllowedProductIds(it))
        }

        if (allowedProducts && allowedProductIdList) {
            result = allowedProductIdList.flatten().containsAll(productIdList)
        } else {
            result = false
        }
        return result
    }

    List<ValidatedSignal> getValidatedSignalList() {
        User user = userService.getUser()
        List<Long> groupIds = user.groups?.collect{it.id}
        List<ValidatedSignal> validatedSignals = ValidatedSignal.findAll()
        validatedSignals
    }

    def getClosedValidatedSignalList(signalType, selectedAlertsFilter) {
        List<Map> signalsList =[]
        User user = userService.getUser()
        List<Long> groupIds = user.groups?.collect { it.id }
        String timeZone = user?.preference.timeZone
        List<Map> validatedSignals = []
        def searchRequest=null
        def params=["callingScreen":"dashboard"];
        String orderByProperty = "dateCreated"
        String orderDirection = "desc"
        params.signalType = signalType
        String searchAlertQuery = prepareValidatedSignalHQLDashboard(searchRequest, params, groupIds, user, orderByProperty, orderDirection, selectedAlertsFilter)
        List<ValidatedSignal> signals=[]
        validatedSignals = ValidatedSignal.executeQuery(searchAlertQuery, [offset: searchRequest?.searchParam?.start, max: searchRequest?.pageSize()])
        validatedSignals.each{
                StringBuilder sb=new StringBuilder("")
                def jsonSlurper = new JsonSlurper()
                List<Object> productGroupSelection = (StringUtils.isNotBlank(it.productGroupSelection))?jsonSlurper.parseText(it.productGroupSelection):null;
                Map object = (null!=it.products)?jsonSlurper.parseText(it.products):null;
                List<String>  selectProductDictionaryItems=Holders.config.alert.selectProductDictionaryItems;
                String rgx= "[(]{1}[0-9]+[)]{1}";
                if(productGroupSelection!=null&& productGroupSelection.size()>0){
                    productGroupSelection.each({
                        sb.append(it.name.replace(",",", ") .replaceAll(rgx, "")).append(", ")
                    });
                }
                for (String selectProduct:selectProductDictionaryItems){
                    List<Object> pros=(null!=object)?object."${selectProduct}":null;
                    if(pros!=null&& pros.size()>0){
                        pros.each({
                            sb.append(it.name).append(Constants.COMMA_SEPARATOR);
                        });
                    }
                }
            if(sb.length()>0)
            {
                if(sb[sb.length()-2]==","){
                    sb.setLength(sb.length() - 2);
                }

            }
            String productName=sb.toString()
            Map map=[:]
            map.put("signalId",it.id);
            map.put("signalName",it.name);
            map.put("productName",productName)
            map.put("disposition",it.disposition.displayName)
            if(params.signalType == Constants.Commons.ASSIGNED_DASHBOARD){
                if("Date Closed".equalsIgnoreCase(it.signalStatus)==false){
                    signalsList.add(map)
                }
            }else {
                signalsList.add(map)
            }
        }
        Integer totalCount = getValidatedSignalCounts(user, searchRequest, searchRequest?.searchParam?.search?.getValue()?.toLowerCase(), params, groupIds, true, "")
        Integer filteredCount = signalsList.size()

        [aaData: signalsList, recordsTotal: totalCount, recordsFiltered: filteredCount]
    }

    Boolean checkAccessibility(Long id) {
        User user = userService.getUser()
        List<Long> groupIds = user?.groups?.collect { it.id }
        StringBuilder searchAlertQuery = new StringBuilder()
        searchAlertQuery.append("SELECT vs FROM ValidatedSignal vs WHERE 1=1 ")
        validatedSignalSearchFilters(null, groupIds, searchAlertQuery, user, null, null)
        searchAlertQuery.append(" and vs.id = $id ")
        List<ValidatedSignal> validatedSignals = ValidatedSignal.executeQuery(searchAlertQuery.toString())
        if (validatedSignals)
            return true
        else
            return false
    }

    List<String> getStringIdList (String str) {
        str.split(",") as List
    }

    List getAllowedProductIds (SafetyGroup safetyGroup) {
        List allowedProductIds = []
        ProductDictionaryCache productDictionaryCache = ProductDictionaryCache.findBySafetyGroup(safetyGroup)
        productDictionaryCache?.allowedDictionaryData.each {
            allowedProductIds.addAll(getStringIdList(it.allowedDataIds))
        }
        allowedProductIds
    }

    List<SingleCaseAlert> fetchAllSCAForSignalId(Long id, Group group = null){
        Group workflowGroup
        if(!group){
            workflowGroup = userService.getUser().workflowGroup
        } else {
            workflowGroup = group
        }
        List<SingleCaseAlert> singleCaseAlertList = SingleCaseAlert.createCriteria().list {
            'executedAlertConfiguration' {
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", id)
            order("id", "desc")
        }

        singleCaseAlertList += ArchivedSingleCaseAlert.createCriteria().list {
            'executedAlertConfiguration' {
                eq("workflowGroup", workflowGroup)
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", id)
            order("id", "desc")
        }

        singleCaseAlertList = singleCaseAlertList.unique {
            [it.productFamily, it.caseNumber, it.alertConfiguration]
        }
        singleCaseAlertList
    }

    List<SingleCaseAlert> fetchAllSCAForSignalIdforReport(Long id){

        List<SingleCaseAlert> singleCaseAlertList = SingleCaseAlert.createCriteria().list {
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", id)
            order("id", "desc")
        }

        singleCaseAlertList = singleCaseAlertList.unique {
            [it.productFamily, it.caseNumber, it.alertConfiguration]
        }
        singleCaseAlertList
    }

    def fetchAggregateData(def alert , String dataSource , String field , def checkingValue) {
        if(dataSource.contains("pva")){
            if(alert.getProperty(field)!=checkingValue)
                return alert.getProperty(field)
            else
                return '-'
        }
        else if(dataSource.contains("vaers")){
            return alert.getVaersColumnValue(field + 'Vaers') ?: '-'
        }
        else if(dataSource.contains("vigibase")){
            return alert.getVigibaseColumnValue(field + 'Vigibase') ?: '-'
        }
        else if(dataSource.contains("jader")){
            if(field == "cummCount"){
                field = "cumCount"
            }
            if(field == "newSeriousCount" || field == "cumSeriousCount" ){
                return '-'
            }
            return alert.getJaderColumnValue(field + 'Jader') ?: '-'
        }
        else{
            return alert.getFaersColumnValue(field + 'Faers') ?: '-'
        }
    }

    void changeAssignedTo(params) {
        ValidatedSignal signalAssigned = ValidatedSignal.get(params.signalId)
        String assignedTo = params.currentUser
        User oldUser = signalAssigned.assignedTo
        Group oldUserGroup = signalAssigned.assignedToGroup
        Group assignedToGroup
        User assignedToUser
        List<String>userEmailList=[]
        if(oldUser)
        {
            userEmailList.add(oldUser?.email)
        }
        if(oldUserGroup)
        {
            List<User> oldUserList = userService.getUserListFromAssignToGroup(signalAssigned)
            oldUserList.each {
                userEmailList.add(it.email)
            }

        }

        if (!TextUtils.isEmpty(assignedTo)) {
            signalAssigned = userService.assignGroupOrAssignTo(assignedTo, signalAssigned)
            if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                assignedToGroup = Group.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
            } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                assignedToUser = User.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
            }
            String oldUserName = oldUser? oldUser.fullName : oldUserGroup.name
            String newUserName = assignedToUser? assignedToUser.fullName : assignedToGroup.name
            def details = "Assigned To changed from '${oldUserName}' to '${newUserName}' for signal '${signalAssigned.name}'"
            def attr = [oldUser: oldUserName, newUser: newUserName, signal: signalAssigned.name]
            if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ASSIGNEE_UPDATE)) {
                emailNotificationService.mailHandlerForSignalAssigneeChange(signalAssigned,userEmailList)
            }
            activityService.createActivityForSignal(signalAssigned, '', details, ActivityType.findByValue(ActivityTypeValue.AssignedToChange),
                    assignedToUser, userService.getUser(), attr, assignedToGroup)

        }
    }
    void updateUndoableDispositons(Long signalId){
        log.info("Disabling the undoable dispositon objects on change of signal workflow state for signalId: "+ signalId)
        try{
            UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: signalId, type: Constants.AlertConfigType.VALIDATED_SIGNAL])
        } catch(Exception ex){
            log.error("Error occoured while disabling the undoable dispositon objects on change of signal workflow state: "+ex.printStackTrace())
        }
    }

    Integer saveWorkflowState(Map params){
        boolean dueInStartEnabled= Holders.config.dueInStart.enabled
        ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId as Long)
        SystemConfig systemConfig = SystemConfig.first()
        List dateClosedBasedOnWorkflowList = systemConfig.dateClosedWorkflow ? systemConfig.dateClosedWorkflow.split(',') : []
        String previousDueDate=DateUtil.fromDateToString(validatedSignal.actualDueDate,DEFAULT_DATE_FORMAT)
        validatedSignal.workflowState = params.workflowStateEnd
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDueInDisplay(true)
        if(validatedSignal && signalWorkflowState){
            if(validatedSignal.workflowState == signalWorkflowState.value){
                validatedSignal.isDueDateUpdated = false
                CRUDService.update(validatedSignal)
            }
        }
        updateUndoableDispositons(params.signalId as Long)
        if(params.isWorkflowUpdate as Boolean==true){
            saveSignalStatusHistory([signalStatusStart: params.workflowStateStart,signalStatus: params.workflowStateEnd, statusComment: '', signalId: params.signalId as Long], true,true)
        } else {
            saveSignalStatusHistory([signalStatus: params.workflowState, statusComment: '', signalId: params.signalId as Long], true)
        }
        if (!systemConfig.isDisposition && dateClosedBasedOnWorkflowList?.contains(params.workflowStateEnd) && !validatedSignal.actualDateClosed) {
            String statusComment = Holders.config.validatedSignal.dateClosedBasedOnWorkflow
            String createDate = DateUtil.fromDateToStringWithTimezone(new Date(), DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
            saveSignalStatusHistory([signalStatus: Constants.WorkFlowLog.DATE_CLOSED, statusComment: statusComment, signalId: params.signalId, "createdDate": createDate, "isAutoPopulate": true,"isSystemUser":true], true)
        }
        Integer dueInDays=null;
        dueInDays=calculateDueIn(params.signalId as Long, params.workflowStateEnd, true)
        if(systemConfig.displayDueIn && dueInDays!=null){
            saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                     signalId    : validatedSignal.id, "createdDate":previousDueDate], true)
        }
        return dueInDays;
    }


    void generateSpotfireReportForSignal(Map params, List SCAList) {
        Long execConfigId = getNext()
        User currentUser = userService.getUser()
        Group group = currentUser.workflowGroup
        String category = AuditTrail.Category.INSERT.toString()
        ValidatedSignal validatedSignal1 = ValidatedSignal.get(params.signalId as Long)
        String newValue = User.findByUsername(validatedSignal1?.createdBy)?.fullName ?: "System"
        Promise promise = task {
            spotfireFileExecution(params, execConfigId, currentUser, group, SCAList)
        }
        promise.onComplete {
            log.info("Spotfire Analysis Succcessfull")
            try {
                signalAuditLogService.createAuditLog([
                        entityName : "Signal",
                        moduleName : "Signal: Data Analysis",
                        category   : category,
                        entityValue: validatedSignal1?.getInstanceIdentifierForAuditLog(),
                        description: "Executing Spotfire Report",
                        username   : currentUser?.username ?:validatedSignal1?.assignedTo?.username,
                        fullname   : currentUser?.fullName ?:validatedSignal1?.assignedTo?.fullName
                ] as Map, [[propertyName: "Owner", oldValue: "", newValue: newValue],
                           [propertyName: "Signal Name", oldValue: "", newValue: validatedSignal1?.name],
                           [propertyName: "Date Range", oldValue: "", newValue: ViewHelper.getAssessmentFilterDateRange().find { it.name.toString() == params.dateRange }.display]] as List)
            } catch (Exception ex) {
                log.error("audit log failed after generating spotfire analysis.")
                ex.printStackTrace()
            }
        }

        promise.onError { Throwable err ->
            err.printStackTrace()
        }

    }


    Long getNext(Boolean isHibernateSeq = false) {
        Session session = sessionFactory.currentSession
        SQLQuery query
        if(!isHibernateSeq) {
            query = session.createSQLQuery("SELECT nextval('exec_config_sequence') as num;")
                    .addScalar("num", new LongType())
        } else {
            query = session.createSQLQuery("SELECT nextval('hibernate_sequence') as num;")
                    .addScalar("num", new LongType())
        }

        return ((BigInteger) query.uniqueResult()).longValue()
    }

    List getDateRangeList(SignalAssessmentDateRangeEnum dateRange, Group group, Map params){
        List<String> dateRangeList = []
        String timeZone = Holders.config.server.timezone
        switch (dateRange) {
            case SignalAssessmentDateRangeEnum.CUSTOM:
                dateRangeList = [params.startDate, params.endDate]
                break
            case SignalAssessmentDateRangeEnum.LAST_3_MONTH:
                dateRangeList = RelativeDateConverter.lastXMonths(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_6_MONTH:
                dateRangeList = RelativeDateConverter.lastXMonths(null, 6, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_3_YEAR:
                dateRangeList = RelativeDateConverter.lastXYears(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_5_YEAR:
                dateRangeList = RelativeDateConverter.lastXYears(null, 5, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.SIGNAL_DATA:
                List<SingleCaseAlert> singleCaseAlertList = fetchAllSCAForSignalId(params.signalId as Long, group)
                dateRangeList = validatedSignalChartService.fetchDateRangeFromCaseAlerts(singleCaseAlertList)
                break
            case SignalAssessmentDateRangeEnum.LAST_1_YEAR:
            default:
                dateRangeList = RelativeDateConverter.lastXYears(null, 1, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
        }
        return dateRangeList
    }

    List getResultDataForSpotfire(Map params, List dateRangeList, Configuration configurationFaers, List resultData, Long execConfigId){
        final Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        try {
            String initialParamsInsert = sqlGenerationService.initializeAlertGttsSignal(params, dateRangeList)
            if (initialParamsInsert) {
                sql.execute(initialParamsInsert)
            }
            String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(configurationFaers, null, configurationFaers?.excludeNonValidCases)
            if (insertQueryData) {
                sql?.execute(insertQueryData)
                def createReportSql = "{call PKG_PVS_ALERT_EXECUTION.P_PERSIST_CASE_SERIES_DATA_QL($execConfigId)}"
                sql?.call(createReportSql)
            }
            String reportSql
            log.info("generateCaseLineListingSql called.")
            sql?.call("{? = call pkg_create_report_sql.p_main()}", [Sql.VARCHAR]) { String sqlValue ->
                reportSql = sqlValue
            }
            List fieldNameWithIndex = []

            ReportTemplate templateObj = configurationFaers.template
            templateObj = GrailsHibernateUtil.unwrapIfProxy(templateObj)

            if (templateObj.templateType == TemplateTypeEnum.CASE_LINE) {
                CaseLineListingTemplate template = templateObj
                fieldNameWithIndex = template.getFieldNameWithIndex()
            }

            sql.eachRow(reportSql) { GroovyResultSet resultSet ->
                Map map = [:]
                resultSet.toRowResult().eachWithIndex { result, i ->
                    def value = ""
                    if (result.value instanceof Clob) {
                        //Handle Clob data
                        value = result.value.asciiStream.text
                    } else {
                        value = result.value
                    }

                    if (templateObj.templateType == TemplateTypeEnum.CASE_LINE) {
                        map.put(fieldNameWithIndex[i], value)
                    } else {
                        map.put(result.key, value)
                    }
                }
                resultData.add(map)
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            sql.close()
        }
        return resultData
    }

    void saveCasesForSpotfire(List dateRangeList, String fileName, User currentUser, Long caseSeriesId, List resultData){
        Sql sql1 = new Sql(signalDataSourceService.getDataSource("pva"))
        Set<String> warnings = []
        Integer result = 0

        try {
            Long startTime = System.currentTimeMillis()
            if (resultData) {
                sql1.execute(sqlGenerationService.delPrevGTTSForCaseSeries())
                sql1.withBatch(1000) { stmt ->
                    resultData.each { Map caseVersionMap ->
                        stmt.addBatch("""insert into GTT_FILTER_KEY_VALUES(CODE,TEXT) values(${caseVersionMap."${cacheService.getRptFieldIndexCache('masterVersionNum')}"},'${caseVersionMap."${cacheService.getRptFieldIndexCache('masterCaseNum')}"}')""")
                    }
                }
            }

            String endDate = dateRangeList[1]
            String dateRangeType = DateRangeTypeCaseEnum.CASE_RECEIPT_DATE.value()
            String evaluateDateAs = null
            String versionAsOfDate = null
            Integer includeLockedVersion = 1
            String owner = Constants.PVS_CASE_SERIES_OWNER
            sql1.call("{?= call PKG_QUERY_HANDLER.f_save_cstm_case_series(?,?,?,?,?,?,?,?,?,?,?,?)}",
                    [Sql.NUMERIC, fileName, 0, currentUser.id, caseSeriesId, null, 1, endDate, dateRangeType, evaluateDateAs, versionAsOfDate, includeLockedVersion, owner]) { res ->
                result = res
            }
            if (result != 0) {
                warnings = sql1.rows("SELECT * from GTT_REPORT_INPUT_PARAMS").collect { it.PARAM_KEY }
            }
            log.info("Cases not saved in DB : ${warnings}")

        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            sql1.close()
        }
    }

    void spotfireFileExecution(Map params, Long execConfigId, User currentUser, Group group, List SCAList){
        List resultData = []
        String timeZone = Holders.config.server.timezone

        SignalAssessmentDateRangeEnum dateRange = params.dateRange as SignalAssessmentDateRangeEnum

        List<String> dateRangeList = getDateRangeList(dateRange,group,params)

        ValidatedSignal signal = ValidatedSignal.get(params.signalId as Long)
        Boolean isMultiIngredient = params.isMultiIngredient? true:false
        Configuration configurationFaers = new Configuration(name: "spotfire",
                dateRangeType: DateRangeTypeCaseEnum.CASE_RECEIPT_DATE,
                productGroupSelection: params.productGroupSelection, eventSelection: params.eventSelection,
                productDictionarySelection: params.productDictionarySelection, limitPrimaryPath: false,
                includeMedicallyConfirmedCases: false, excludeFollowUp: false,
                includeLockedVersion: true,
                selectedDatasource: Constants.DataSource.PVA,
                excludeNonValidCases: true,
                asOfVersionDate: null,
                suspectProduct: false, missedCases: false, eventGroupSelection: params.eventGroupSelection,
                type: SINGLE_CASE_ALERT,
                template: ReportTemplate.findByName("Case_num"),
                isMultiIngredient: isMultiIngredient)

        if(dateRange == SignalAssessmentDateRangeEnum.SIGNAL_DATA){
            resultData = SCAList.collect{
                [(cacheService.getRptFieldIndexCache('masterVersionNum').toString()): it.caseVersion, (cacheService.getRptFieldIndexCache('masterCaseNum').toString()): it.caseNumber]
            }
        } else {
            resultData = getResultDataForSpotfire(params, dateRangeList, configurationFaers, resultData, execConfigId)
        }


        Long caseSeriesId
        ExecutedCaseSeriesDTO executedCaseSeriesDTO = new ExecutedCaseSeriesDTO()
        ExecutedDateRangeInfoDTO executedCaseSeriesDateRangeInformationDTO = new ExecutedDateRangeInfoDTO()
        executedCaseSeriesDateRangeInformationDTO.dateRangeStartAbsolute = new Date()
        executedCaseSeriesDateRangeInformationDTO.dateRangeEndAbsolute = new Date()
        executedCaseSeriesDateRangeInformationDTO.dateRangeEnum = DateRangeEnum.CUMULATIVE
        executedCaseSeriesDateRangeInformationDTO.relativeDateRangeValue = 1
        List<QueryValueListDTO> executedGlobalQueryValueDTOLists = []
        executedCaseSeriesDTO.with {
            seriesName = "${execConfigId}name"
            description = "Desc"
            dateRangeType = configurationFaers.dateRangeType.value()
            //Setting asOfVersionDate as null bcoz PVR report engine with expect asOfversionDate as null when Evaluate Case date on is selected as LATEST_VERSION. -PS
            asOfVersionDate = null
            evaluateDateAs = null
            excludeFollowUp = true
            includeLockedVersion = true
            excludeNonValidCases = true
            productSelection = params.productSelection
            studySelection = params.studySelection
            eventSelection = params.eventSelection
            ownerName = "Signal"
            globalQueryId = null
            suspectProduct = false
            executedCaseSeriesDateRangeInformation = executedCaseSeriesDateRangeInformationDTO
            executedGlobalQueryValueLists = []
            callbackURL = grailsLinkGenerator?.link(base: grailsApplication.config.signal.serverURL, controller: 'singleCaseAlert', action: 'caseSeriesCallback', params: [id: execConfigId])
            callbackURL += "/${false}"
            isTemporary = true
        }
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.caseSeries.generation.uri
        Map response = reportIntegrationService.postData(url, path, executedCaseSeriesDTO)
        log.info("Case Series API Response from PVR : ${response}")
        if (response.status == HttpStatus.SC_OK) {
            if (response.result.status) {
                caseSeriesId = response.result.data as Long
            }
        }
        //Added new method to changes for PVS-68227.
        String fileName = generateFileName(signal?.name, execConfigId)
        log.debug("Full File Name with applying regex, length and added execConfigId : "+ fileName)

        saveCasesForSpotfire(dateRangeList, fileName, currentUser, caseSeriesId, resultData)

        Set<String> familiesId
        if(params.productSelection){
            familiesId = spotfireService.prepareFamilyIds(params.productSelection, isMultiIngredient)
        } else {
            familiesId = spotfireService.prepareFamilyIdsForProductGroup(getIdsForProductGroup(params.productGroupSelection), isMultiIngredient)

        }
        Date startDate = DateUtil.stringToDate(dateRangeList[0], "dd/MM/yyyy", timeZone)
        Date endDate = DateUtil.stringToDate(dateRangeList[1], "dd/MM/yyyy", timeZone)
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
        spotfireService.createNotificationsSignal(signal, fileName, params, sdf.format(startDate), sdf.format(endDate),currentUser, dateRange)


        log.info("The product family ids are : " + familiesId.toString())
        log.info("Case Series Id is : " + caseSeriesId)
        log.info("Report Type is : " + "drug")
        log.info("File Full Name is : " + fileName)


        spotfireService.reserveFileName(fileName)

        def respMsg = spotfireService.generateReport(
                familiesId,
                startDate,
                endDate,
                new Date(),
                caseSeriesId,
                "drug",
                fileName,
                false,null,Constants.DataSource.PVA,signal)


        JsonSlurper slurper = new JsonSlurper()
        if(respMsg){
            def jsonRsp = slurper.parseText(respMsg)
            if (!jsonRsp.JobId)
                throw new Exception("Unknown response received from spotfire: " + respMsg)
        }
    }

    /**
     * Generates a sanitized SpotFire file name by applying necessary regex patterns and appending the execution configuration ID.
     *
     * Changes implemented for PVS-68227:
     *
     * @param signalName The original name of the signal which needs to be sanitized.
     * @param execConfigId The execution configuration ID to be appended to the sanitized file name.
     * @return A valid and sanitized file name with the execution configuration ID appended.
     */
    String generateFileName(String signalName, long execConfigId) {
        String fileName = MiscUtil.generateSpotFireFileName(signalName, SPOTFIRE_ANALYSIS_FILE_NAME_LENGTH)
        log.debug("File Name(applied regex pattern and length limit restriction) :"+fileName)
        fileName = fileName + Constants.Commons.UNDERSCORE + execConfigId
        return fileName
    }

    //Calculate duein date
    Integer getCalculatedDueIn(ValidatedSignal validatedSignal, String workflowState, Date dueInStartBaseDate, Date dueIn, Boolean isDueDateUpdate = false) {
        Integer dueInDays
        String modifiedByUser =  validatedSignal.modifiedBy
        if(dueIn){
            String serverTimeZone = Holders.config.server.timezone
            String currentDate = stringFromDate(new Date(), "yyyy-MM-dd", serverTimeZone)
            String dueInDate= new SimpleDateFormat("yyyy-MM-dd").format(dueIn)
            String dueDate=new SimpleDateFormat("yyyy-MM-dd").format(validatedSignal.dueDate)
            boolean dueInStartEnabled = Holders.config.dueInStart.enabled
            if(dueInStartEnabled || validatedSignal.isDueDateUpdated){
                dueInDays = new DateTime(dueDate).toDate() - new DateTime(currentDate).toDate()
            } else {
                dueInDays = DateUtils.truncate(new DateTime(dueInDate).toDate(), Calendar.DAY_OF_MONTH) - DateUtils.truncate(new DateTime(currentDate).toDate(), Calendar.DAY_OF_MONTH)
            }
        }
        if(validatedSignal.dueDate){
            validatedSignal.actualDueDate = validatedSignal.dueDate
        }
        SignalStatusHistory dueDateHistory= validatedSignal.signalStatusHistories?.find { it.signalStatus == Constants.WorkFlowLog.DUE_DATE }
        dueDateHistory?.dateCreated = validatedSignal.actualDueDate
        dueDateHistory?.skipAudit=true
        dueDateHistory?.save(flush: true)
        CRUDService.update(validatedSignal,isDueDateUpdate,modifiedByUser)
        dueInDays
    }

    Integer calculateDueIn(Long id, String workflowState, boolean isUpdateWorkflow = false, Boolean isDueDateUpdate = false, Boolean isReset = false, Boolean retain = false, Boolean updateDetectedDateActivity = false, Boolean isUpdate = false) {
        Date dueIn = null
        ValidatedSignal validatedSignal = ValidatedSignal.get(id)
        boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDueInDisplay(true)
        SystemConfig systemConfig = SystemConfig.first()
        List<String> dueInEndPoints = systemConfig.selectedEndPoints?.split(",").collect {it} as List<String>
        boolean dueInStartEnabled = Holders.config.dueInStart.enabled
        String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
        List<Map> signalHistoryList = validatedSignal?.signalStatusHistories as List<Map>
        boolean defaultCloseCondition = signalHistoryList.find { it.signalStatus in dueInEndPoints } != null
        // if disposition.signalStatusForDueDate status exist in signal then signal due in should be -
        boolean isCloseStatusExistInHistory = signalHistoryList.find { it.signalStatus == validatedSignal.disposition.signalStatusForDueDate} != null && systemConfig.enableEndOfMilestone
        Disposition defaultSignalDisposition = userService.getUser()?.getWorkflowGroup()?.defaultSignalDisposition
        Boolean endOfMilestone = SystemConfig.first()?.enableEndOfMilestone
        Boolean isDispoStatusNullReviewComp = endOfMilestone && validatedSignal.disposition.isReviewCompleted() && validatedSignal.disposition.id != defaultSignalDisposition.id
        boolean basicConditionToCloseSignal = defaultCloseCondition || (dueInEndPoints.contains(workflowState)) || isCloseStatusExistInHistory || isDispoStatusNullReviewComp
        Date dueInStartBaseDate = validatedSignal.signalStatusHistories.find { it.signalStatus == dueInStartPoint }?.dateCreated
        if (dueInStartEnabled) {
            dueIn = basicConditionToCloseSignal ? null : calcDueDateForDueInStartPoint(validatedSignal, validatedSignal.priority, validatedSignal.disposition, isReset, cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id))
        } else if (!enableSignalWorkflow) {
            if(retain)
                basicConditionToCloseSignal = false
            dueIn = basicConditionToCloseSignal ? null : calcDueDate(validatedSignal, validatedSignal.priority, validatedSignal.disposition, false, cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id))
        } else {
            if (basicConditionToCloseSignal) {
                dueIn = null
            } else if (isUpdateWorkflow && signalWorkflowState?.value == workflowState) {
                dueIn = calcDueDateForSignalWorkflow(validatedSignal, validatedSignal.priority, validatedSignal.disposition, false, cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id), new Date())
            } else if (validatedSignal.wsUpdated) {
                dueIn = calcDueDateForSignalWorkflow(validatedSignal, validatedSignal.priority, validatedSignal.disposition, false, cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id))
            } else if (!validatedSignal.wsUpdated && isUpdate && validatedSignal.dueDate) {
                dueIn = validatedSignal.dueDate
                if (updateDetectedDateActivity) {
                    dueIn = calcDueDateForSignalWorkflow(validatedSignal, validatedSignal.priority, validatedSignal.disposition, false, cacheService.getDispositionConfigsByPriority(validatedSignal.priority.id))
                }
            }
        }
        if(!dueIn){
            validatedSignal.dueDate = null
            validatedSignal.save(flush:true)
        }
        dueIn ? getCalculatedDueIn(validatedSignal, workflowState, dueInStartBaseDate, dueIn,isDueDateUpdate) : null
    }




    def changeToInitialDisposition(def alert, ValidatedSignal signal, Disposition targetDisposition) {
        alert.disposition = targetDisposition
        if(alert instanceof AggregateCaseAlert){
            //Due date should be initial date as per PVS-25263
            alert.dueDate = alert.initialDueDate
        }
        if (!(alert instanceof LiteratureAlert || alert instanceof ArchivedLiteratureAlert
                || alert instanceof EmbaseLiteratureAlert || alert instanceof ArchivedEmbaseLiteratureAlert)) {
            calcDueDate(alert, alert.priority, alert.disposition, true,
                    cacheService.getDispositionConfigsByPriority(alert.priority.id))
        }
        alert.validatedSignals = alert.validatedSignals - signal
        CRUDService.update(alert)
    }

    List<Map> getSignalStatusHistories(List<Long> signalIdList) {
        ValidatedSignal.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                "signalStatusHistories" {
                    property(SIGNAL_STATUS, SIGNAL_STATUS)
                }
            }
            or {
                signalIdList.collate(1000).each {
                    'in'('id', it)
                }
            }
        } as List<Map>
    }

    List<Map> getSignalActionTakenList(Session session, String sqlQuery) {
        List<Map> signalActionTakenList = session.createSQLQuery(sqlQuery)
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .list()
        signalActionTakenList
    }

    Integer calculateDueInForSignalList(ValidatedSignal validatedSignal, String workflowState, boolean enableSignalWorkflow) {
        Date dueIn
        SystemConfig systemConfigInstance = SystemConfig.first()
        List<String> dueInEndPoints = systemConfigInstance.selectedEndPoints?.split(",").collect {it} as List<String>
        String defaultValidatedDate=Holders.config.signal.defaultValidatedDate;
        if (!enableSignalWorkflow) {
            if (workflowState == Constants.DATE_CLOSED) {
                return null
            } else {
                dueIn = calcDueDate(validatedSignal, cacheService.getPriorityByValue(validatedSignal.priorityId), cacheService.getDispositionByValue(validatedSignal.dispositionId), false, cacheService.getDispositionConfigsByPriority(validatedSignal.priorityId))
            }
        } else {
            if (workflowState == Constants.DATE_CLOSED ||  dueInEndPoints.contains(workflowState)) {
                return null
            } else if ((workflowState == 'Validation Date' || workflowState==defaultValidatedDate) && !dueInEndPoints.contains(workflowState)) {
                dueIn = calcDueDateForSignalWorkflow(workflowState,validatedSignal, cacheService.getPriorityByValue(validatedSignal.priorityId), cacheService.getDispositionByValue(validatedSignal.dispositionId), false, cacheService.getDispositionConfigsByPriority(validatedSignal.priorityId))
            } else if (validatedSignal.wsUpdated) {
                dueIn = calcDueDateForSignalWorkflow(workflowState,validatedSignal, cacheService.getPriorityByValue(validatedSignal.priorityId), cacheService.getDispositionByValue(validatedSignal.dispositionId), false, cacheService.getDispositionConfigsByPriority(validatedSignal.priorityId))
            } else {
                dueIn = null
            }
        }
        if (dueIn) {
            return DateUtils.truncate(dueIn, Calendar.DAY_OF_MONTH) - DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH)
        } else {
            return null
        }
    }


    private void bindFiles(def signalRMMs, Map params = null, List<MultipartFile>  filesToUpload, String fileName = null) {
        try {
            User currentUser = userService.getUser()
            if(params.inputName=="" || fileName.equals("rmmReferenceFile")){
                params.inputName=null
            }
            Map filesStatusMap = attachmentableService.attachUploadFileToRMMs(currentUser, signalRMMs, filesToUpload,null,null,null, params.inputName as String, true)
            def attachmentsSize = 0 - filesStatusMap?.uploadedFiles?.size()
            String fileDescription = params.description
            List<Attachment> attachments = signalRMMs.getAttachments().sort { it.dateCreated }
            if (attachments) {
                attachments = attachments[-1..attachmentsSize]
                attachments.each { attachment ->
                    if(AttachmentDescription.findByAttachment(attachment) == null){
                        AttachmentDescription attachmentDescription = new AttachmentDescription()
                        attachmentDescription.attachment = attachment
                        attachmentDescription.createdBy = userService.getUser().getFullName()
                        attachmentDescription.description = fileDescription
                        attachmentDescription.save(flush: true)
                    }else{
                        AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(attachment)
                        attachmentDescription.description = fileDescription
                        attachmentDescription.save(flush: true)
                    }
                }
            }

        } catch (FileFormatException e) {
            e.printStackTrace()
        }
    }

    private void bindMemoFiles(def signalRMMs, Map params = null, MultipartFile file, String fileName = null) {
        try {
            List<MultipartFile> filesToUpload = [file]
            User currentUser = userService.getUserFromCacheByUsername(Constants.SYSTEM_USER)
            if(params.inputName=="" || fileName.equals("rmmReferenceFile")){
                params.inputName=null
            }
            List<MultipartFile> uploadedFiles = attachmentableService.upload(currentUser, signalRMMs, filesToUpload,params.inputName)
            def attachmentsSize = 0 - uploadedFiles?.size()
            String fileDescription = params.description
            List<Attachment> attachments = signalRMMs.getAttachments().sort { it.dateCreated }
            if (attachments) {
                attachments = attachments[-1..attachmentsSize]
                attachments.each { attachment ->
                    AttachmentDescription attachmentDescription = new AttachmentDescription()
                    attachmentDescription.attachment = attachment
                    attachmentDescription.createdBy = currentUser.getFullName()
                    attachmentDescription.description = fileDescription
                    attachmentDescription.save(flush: true)
                }
            }

        } catch (FileFormatException e) {
            e.printStackTrace()
        }
    }

    private void bindReferences(SignalRMMs signalRMMs, Map params = null, ValidatedSignal validatedSignal, referenceName = null, inputName = null) {
        String referenceLink
        if(referenceName){
            referenceLink = referenceName
        } else {
            referenceLink = params.referenceLink
        }
        if(params.inputName == null){
            params.inputName = inputName
        }
        if (referenceLink) {
            User currentUser = userService.getUser()
            try {
                attachmentableService.doAddReference(currentUser, signalRMMs, referenceLink,params.inputName as String, null, true)
                List<Attachment> attachments = validatedSignal.getReferences()
                if (attachments) {
                    if(AttachmentDescription.findByAttachment(attachments.first()) == null){
                        AttachmentDescription attachmentDescription = new AttachmentDescription()
                        attachmentDescription.attachment = attachments.first()
                        attachmentDescription.createdBy = currentUser.getFullName()
                        attachmentDescription.description = params.description
                        attachmentDescription.save(flush: true)
                    }else{
                        AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(attachments.first())
                        attachmentDescription.description = params.description
                        attachmentDescription.save(flush: true)
                    }
                }

            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    CommonsMultipartFile fileToMultipartFile(File file){
        InputStream input = null
        OutputStream os = null
        file.setExecutable(false)
        String contentType = URLConnection.guessContentTypeFromName(file.name)
        FileItem fileItem = new DiskFileItem("attachments", contentType, false, file.getName(), (int) file.length(), file.getParentFile());
        input = new FileInputStream(file);
        os = fileItem.getOutputStream();
        IOUtils.copy(input, os);
        CommonsMultipartFile multipartFile = new CommonsMultipartFile(fileItem)
        return multipartFile
    }

    MultipartFile prepareSignalMemoReport(ValidatedSignal validatedSignal, String username = null, String inputName = null) {
        User user = userService.getUser() ?: userService.getUserFromCacheByUsername(username) ?: userService.getUserFromCacheByUsername(Constants.SYSTEM_USER)
        String timezone = user?.preference?.timeZone
        String reportFormat = Holders.config.signal.autoNotification.reportFormat
        Map memoReportData = validatedSignal.exportSignalMemoReport(timezone)
        if(Holders.config.signal.autoNotification.show.date.closed){
            def dateClosedSignalStatusDate = validatedSignal.signalStatusHistories.find { it.signalStatus == Constants.DATE_CLOSED }?.dateCreated
            memoReportData.put("dateClosed", dateClosedSignalStatusDate ? DateUtil.toDateStringPattern(dateClosedSignalStatusDate, DateUtil.DATEPICKER_FORMAT) : null )
        }
        Map params = [outputFormat: reportFormat, showCompanyLogo: false, timezone: timezone, inputName: inputName]
        File file = dynamicReportService.createSignalMemoReport(new JRMapCollectionDataSource([memoReportData]), params)
        MultipartFile reportFile = fileToMultipartFile(file)
        reportFile
    }

    SignalRMMs saveSignalRMMs(Map params, ValidatedSignal validatedSignal, MultipartHttpServletRequest request){
        SignalRMMs signalRMMs = new SignalRMMs()
        List<MultipartFile>  filesToUpload = []
        // Removed from here , due to fix of PVS-64816

        if(params.communicationType == 'communication' && params.type == 'Signal Memo'){
            if(!params.attachments){
                def signalMemoEntriesCount = validatedSignal.signalRMMs?.count { it.type == "Signal Memo" && it.communicationType == "communication" }
                Integer indexName = signalMemoEntriesCount ? signalMemoEntriesCount + 1 : 1
                String extension = Holders.config.signal.autoNotification.reportFormat
                params.inputName = (validatedSignal.name.length() > 100 ? validatedSignal.name.substring(0, 100) : validatedSignal.name) + "_" +
                        validatedSignal.id + "-Notification Memo-" + indexName + '.' + extension.toLowerCase()
                MultiValueMap<String, Object> fileMap = new LinkedMultiValueMap<>()
                fileMap.add("attachments", prepareSignalMemoReport(validatedSignal))
                request.multipartFiles = fileMap
            }
        }
        // Added for PVS-64816
        filesToUpload = fetchRmmCommunicationList(request)
        if (!TextUtils.isEmpty(params.dueDate)) {
            signalRMMs.dueDate = DateUtil.stringToDate(params.dueDate, DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
        }
        if (params.rmmResp != 'null' && !TextUtils.isEmpty(params.rmmResp)) {
            signalRMMs = userService.assignGroupOrAssignTo(params.rmmResp, signalRMMs)
            if(!signalRMMs.assignedTo && !signalRMMs.assignedToGroup){
                signalRMMs.emailAddress = params.rmmResp
            }
        } else {
            signalRMMs.assignedTo = null
            signalRMMs.assignedToGroup = null
            signalRMMs.emailAddress = ""
        }
        signalRMMs.type = params.type
        signalRMMs.country = params.country == "null" || params.country == "undefined"? "" : params.country
        signalRMMs.communicationType = params.communicationType
        signalRMMs.status = params.status
        signalRMMs.description = params.description
        signalRMMs.dateCreated = new Date()
        signalRMMs.emailSent = null
        signalRMMs.signalId=validatedSignal.id
        validatedSignal.addToSignalRMMs(signalRMMs)
        CRUDService.update(validatedSignal)
        bindFiles(signalRMMs, params, filesToUpload)
        bindReferences(signalRMMs, params, validatedSignal)
        if(params.inputName in [null, ""]){
            signalRMMs?.getAttachments()?.each {
                attachmentableService.removeAttachment(it.id)
            }
            signalRMMs?.getReferences()?.each {
                attachmentableService.removeAttachment(it.id)
            }
            bindReferences(signalRMMs, params, validatedSignal, null, null)
        }
        signalRMMs
    }
    // Added for PVS-64816
    List fetchRmmCommunicationList(MultipartHttpServletRequest request) {
        List<MultipartFile> filesToUpload = []
        if (request instanceof MultipartHttpServletRequest) {
            request.multipartFiles.each { k, v ->
                if (v instanceof List) {
                    v.each { MultipartFile file ->
                        attachmentableService.checkExtension(file.originalFilename)
                        filesToUpload << file
                    }
                } else {
                    filesToUpload << v
                }
            }
        }
        return filesToUpload
    }
    void removeDeletedSignalMemoReportsForUpdatedSignals(List signalRmms){

        List associatedSignalRmms = signalRmms.flatten().unique();
        List associatedSignalMemos = new ArrayList();
        List signalRmmList = new ArrayList();
        associatedSignalRmms.each { SignalRMMs signalRMM->
            if(signalRMM.type=="Signal Memo" || signalRMM.description=="Signal Notification Memo"){
                associatedSignalMemos = getAssociatedSignalMemoList(signalRMM?.criteria);
                if(!checkIfSignalMemosExist(associatedSignalMemos)){
                    signalRmmList.add(signalRMM);
                }
            }
        }
        batchPersistSignalRmm(signalRmmList);
        return;
    }

    void batchPersistSignalRmm(List SignalRmmList) {
        SignalRMMs.withTransaction {
            def batch = []

            for (SignalRMMs signalRmm : SignalRmmList) {
                batch += signalRmm
                Session session = sessionFactory.currentSession
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (SignalRMMs sigRmm in batch) {
                        if (sigRmm.communicationType != "rmmType") {
                            sigRmm.isDeleted = true
                            sigRmm.save(validate: false)
                        }
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory.currentSession
                    for (SignalRMMs sigRmm in batch) {
                        sigRmm.isDeleted = true
                        sigRmm.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
            log.info("SignalRMMs data is batch persisted.")
        }
    }

    List getAssociatedSignalMemoList(String criteria){
        JsonSlurper parser = new JsonSlurper();
        LazyMap criteriaMap = new LazyMap();
        List criteriaList = new ArrayList();
        if(criteria) {
            criteriaMap = parser.parseText(criteria);
            criteriaList = criteriaMap.values()[0];
        }
        return criteriaList;
    }

    Boolean checkIfSignalMemosExist(List associatedSignalMemos) {
        List signalNotificationMemoList = SignalNotificationMemo.getAll(associatedSignalMemos) - null
        if(signalNotificationMemoList.size())
        return true;
        else return false;
    }

    def saveSignalMemoInCommunication(Map params, ValidatedSignal validatedSignal, String criteria, String username){
        SignalRMMs signalRMMs = new SignalRMMs()
        List<Map> attachments = []
        String productAndGroupName = validatedSignal.getProductAndGroupName()
        def signalMemoEntriesCount = validatedSignal.signalRMMs?.count { it.type == "Signal Memo" && it.communicationType == "communication" }
        Integer indexName = signalMemoEntriesCount ? signalMemoEntriesCount + 1 : 1
        String extension = Holders.config.signal.autoNotification.reportFormat
        String memoFileName = "${validatedSignal.name}_${validatedSignal.id.toString()}_${productAndGroupName}"
        Integer maxLength = Constants.MAX_FILE_NAME_LENGTH;
        params.inputName = (memoFileName.length() > maxLength ? memoFileName.substring(0, maxLength) : memoFileName) + "-Notification Memo-${indexName}.${extension.toLowerCase()}"
        MultipartFile multipartFile = prepareSignalMemoReport(validatedSignal, username, params.inputName)
        String signalLink = createHref("validatedSignal", "details", ["id": validatedSignal.id])
        String bodyWithRemovePlaceholder = params.body
                .replaceAll(Constants.SIGNAL_NAME, validatedSignal.name)
                .replaceAll(Constants.PRODUCT_NAME, productAndGroupName)
                .replaceAll(Constants.SIGNAL_LINK, "<a href='${signalLink}'>${signalLink}</a>")

        params.body = bodyWithRemovePlaceholder

        String newSubject = params.subject.replaceAll(Constants.PRODUCT_NAME, productAndGroupName)
        params.subject = newSubject.substring(Constants.MIN_LENGTH, Math.min(newSubject.length(), Constants.MAX_LENGTH))
        signalRMMs.dueDate = null
        signalRMMs.type = "Signal Memo"
        signalRMMs.communicationType = "communication"
        signalRMMs.description = Holders.config.signal.autoNotification.description
        signalRMMs.dateCreated = new Date()
        signalRMMs.emailSent = new Date()
        signalRMMs.criteria = criteria
        signalRMMs.isDeleted = false
        signalRMMs.signalId = validatedSignal.id as Long
        signalRMMs.signalEmailLog = new SignalEmailLog(assignedTo: params.sentTo, subject: params.subject, body: params.body)
        bindSignalRMMsToSignal(signalRMMs, validatedSignal)
        bindMemoFiles(signalRMMs, params, multipartFile)
        bindMemoFiles(signalRMMs.signalEmailLog, params, multipartFile)
        createActivityForJobs(signalRMMs, validatedSignal, params.inputName as String)
        attachments = signalRMMs.signalEmailLog.attachments.collect {
            [name: it.inputName ?: it.filename, file: AttachmentableUtil.getFile(grailsApplication.config, it)]
        }
        emailService.sendCommunicationEmail([
                'toAddress'        : [params.sentTo],
                'title'            : params.subject,
                'map'              : ["emailMessage": params.body, "attachments": attachments],
                'allowNotification': true
        ])
    }

    void bindSignalRMMsToSignal(SignalRMMs signalRMMs, ValidatedSignal validatedSignal){
        List<Map> signalRMMsMap = batchPersistSignalRMMs([[signalId: validatedSignal.id, signalRMMs: signalRMMs]])
        String insertSignalRMMsQuery = SignalQueryHelper.add_signalRMMs_for_signal()
        alertService.batchPersistSignalRMMaMapping(signalRMMsMap, insertSignalRMMsQuery)
    }

    List<Map> batchPersistSignalRMMs(List<Map> signalRMMsList) {
        List<Map> signalRMMsMapIds = []
        Integer batchSize = Holders.config.signal.batch.size as Integer
        SignalRMMs.withTransaction {
            List batch = []
            signalRMMsList.eachWithIndex { Map signalRMMsMap, Integer index ->
                batch += signalRMMsMap.signalRMMs
                signalRMMsMap.signalRMMs.save(validate: false)
                signalRMMsMapIds.add([signalId:signalRMMsMap.signalId, signalRMMsId:signalRMMsMap.signalRMMs.id])
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
        signalRMMsMapIds
    }

    void createActivityForJobs(SignalRMMs signalRMMs, ValidatedSignal validatedSignal, String fileName){
        String detailsMessage = ''
        ActivityType activityType
        User user = userService.getUserFromCacheByUsername(Constants.SYSTEM_USER)
        detailsMessage = "Communication created with Type='${signalRMMs.type}', Country='${country(signalRMMs.country)}', Description='${signalRMMs.description?:''}', FileName= '${fileName}', Resp.='', Status='', DueDate=''"
        activityType = ActivityType.findByValue(ActivityTypeValue.CommunicationAdded)
        def attr = [signal: validatedSignal.name]
        Activity activity = new Activity(
                type: activityType,
                performedBy: user,
                timestamp: DateTime.now(),
                justification: '',
                assignedTo: validatedSignal.assignedTo,
                details: detailsMessage,
                attributes: (attr as JSON).toString(),
                assignedToGroup: validatedSignal.assignedToGroup
        )
        List<Map> activitySignalMemoMap = activityService.batchPersistSignalMemoActivity([[signalId: validatedSignal.id, activity: activity]])
        String insertSignalMemoActivityQuery = SignalQueryHelper.add_activity_for_signal_memo()
        alertService.batchPersistSignalMemoActivityMapping(activitySignalMemoMap, insertSignalMemoActivityQuery)
    }

    SignalRMMs updateSignalRMMs(SignalRMMs signalRMMs, Map params, ValidatedSignal validatedSignal, MultipartHttpServletRequest request){
        if (!(params.rmmResp.contains("null") || params.rmmResp == 'null') && !TextUtils.isEmpty(params.rmmResp)) {
            signalRMMs = userService.assignGroupOrAssignTo(params.rmmResp, signalRMMs)
            if(!signalRMMs.assignedTo && !signalRMMs.assignedToGroup){
                signalRMMs.emailAddress = params.rmmResp
            }
        } else {
            signalRMMs.assignedTo = null
            signalRMMs.assignedToGroup = null
            signalRMMs.emailAddress = ""
        }
        List<MultipartFile> filesToUpload = []
        // Removed from here , due to fix of PVS-64816

        if(params.communicationType == 'communication' && params.type == 'Signal Memo'){
            if(!params.attachmentId){
                def signalMemoEntriesCount = validatedSignal.signalRMMs?.count { it.type == "Signal Memo" && it.communicationType == "communication" }
                Integer indexName = signalMemoEntriesCount ? signalMemoEntriesCount + 1 : 1
                String extension = Holders.config.signal.autoNotification.reportFormat
                params.inputName = (validatedSignal.name.length() > 100 ? validatedSignal.name.substring(0, 100) : validatedSignal.name) + "_" +
                        validatedSignal.id + "-Notification Memo-" + indexName + '.' + extension.toLowerCase()
                MultiValueMap<String, Object> fileMap = new LinkedMultiValueMap<>();
                fileMap.add("attachments", prepareSignalMemoReport(validatedSignal))
                request.multipartFiles = fileMap
            }
        }
        // Added for PVS-64816
        filesToUpload = fetchRmmCommunicationList(request)
        if (!TextUtils.isEmpty(params.dueDate)) {
            signalRMMs.dueDate = DateUtil.stringToDate(params.dueDate, DEFAULT_DATE_FORMAT, Holders.config.server.timezone)
        }
        signalRMMs.emailSent = null
        signalRMMs.type = params.type
        signalRMMs.status = params.status == 'null' ? '' : params.status
        signalRMMs.country = params.country == "null" || params.country == "undefined"? "" : params.country
        signalRMMs.description = params.description
        signalRMMs.communicationType = params.communicationType
        signalRMMs.signalId = validatedSignal.id as Long
        signalRMMs.isDeleted = false
        signalRMMs.save()
        if (params.inputName && request instanceof MultipartHttpServletRequest && request.multipartFiles) {
            // changed existed file object instead of deleting and creating new
            bindFiles(signalRMMs, params, filesToUpload)
        }

        if (params.inputName && params.referenceLink) {
            // changed existing reference object instead of deleting and creating new
            bindReferences(signalRMMs, params, validatedSignal)
        }
        signalRMMs
    }

    def saveActivityForSavingRMMs(SignalRMMs signalRMMs, ValidatedSignal validatedSignal){
        String detailsMessage,resp,attachments
        ActivityType activityType
        String country = country(signalRMMs.country)
        if(signalRMMs.assignedToId){
            resp = signalRMMs.assignedTo.name
        } else if(signalRMMs.assignedToGroupId){
            resp = signalRMMs.assignedToGroup.name
        } else {
            resp = signalRMMs.emailAddress
        }
        if(signalRMMs.attachments){
            attachments = signalRMMs.attachments.collect { it.inputName ?: it.fileName }.join(',')
        } else {
            attachments = signalRMMs.getReferences().collect { it.inputName ?: it.referenceLink }.join(',')
        }
        if(signalRMMs.communicationType == 'rmmType'){
            detailsMessage = "RMM created with Type='${signalRMMs.type ?: ''}', Country = '${country}', Description='${signalRMMs.description?:''}', FileName= '${attachments}', Resp.='${resp}', Status='${signalRMMs.status?:''}', DueDate='${DateUtil.toDateStringWithoutTimezone(signalRMMs.dueDate)}'"
            activityType = ActivityType.findByValue(ActivityTypeValue.RMMAdded)
        } else {
            detailsMessage = "Communication created with Type='${signalRMMs.type ?: ''}', Country = '${country}', Description='${signalRMMs.description?:''}', FileName= '${attachments}', Resp.='${resp}', Status='${signalRMMs.status?:''}', DueDate='${signalRMMs.dueDate ? DateUtil.toDateStringWithoutTimezone(signalRMMs.dueDate) : ''}'"
            activityType = ActivityType.findByValue(ActivityTypeValue.CommunicationAdded)
        }
        def attr = [signal: validatedSignal.name]
        activityService.createActivityForSignal(validatedSignal, '',detailsMessage, activityType, validatedSignal.assignedTo,
                userService.getUser(), attr, validatedSignal.assignedToGroup)
    }

    //Country List must be synced with the list in rmm_communication.js
    def country = { def shortname ->
        if(shortname == null || shortname == "") return "";
        Map<String, String> fullName = [ 'AF' : 'Afghanistan' , 'AX' : 'Aland Islands' , 'AL' : 'Albania' , 'DZ' : 'Algeria' , 'AS' : 'American Samoa' , 'AD' : 'Andorra' , 'AO' : 'Angola' , 'AI' : 'Anguilla' , 'AQ' : 'Antarctica' , 'AG' : 'Antigua And Barbuda' , 'AR' : 'Argentina' , 'AM' : 'Armenia' , 'AW' : 'Aruba' , 'AU' : 'Australia' , 'AT' : 'Austria' , 'AZ' : 'Azerbaijan' , 'BS' : 'Bahamas' , 'BH' : 'Bahrain' , 'BD' : 'Bangladesh' , 'BB' : 'Barbados' , 'BY' : 'Belarus' , 'BE' : 'Belgium' , 'BZ' : 'Belize' , 'BJ' : 'Benin' , 'BM' : 'Bermuda' , 'BT' : 'Bhutan' , 'BO' : 'Bolivia' , 'BA' : 'Bosnia And Herzegovina' , 'BW' : 'Botswana' , 'BV' : 'Bouvet Island' , 'BR' : 'Brazil' , 'IO' : 'British Indian Ocean Territory' , 'BN' : 'Brunei Darussalam' , 'BG' : 'Bulgaria' , 'BF' : 'Burkina Faso' , 'BI' : 'Burundi' , 'KH' : 'Cambodia' , 'CM' : 'Cameroon' , 'CA' : 'Canada' , 'CV' : 'CapeVerde' , 'KY' : 'Cayman Islands' , 'CF' : 'Central African Republic' , 'TD' : 'Chad' , 'CL' : 'Chile' , 'CN' : 'China' , 'CX' : 'Christmas Island' , 'CC' : 'Cocos (Keeling) Islands' , 'CO' : 'Colombia' , 'KM' : 'Comoros' , 'CG' : 'Congo' , 'CD' : 'Democratic Republic of the Congo' , 'CK' : 'Cook Islands' , 'CR' : 'Costa Rica' , 'CI' : 'Cote D\'Ivoire' , 'HR' : 'Croatia' , 'CU' : 'Cuba' , 'CY' : 'Cyprus' , 'CZ' : 'Czech Republic' , 'DK' : 'Denmark' , 'DJ' : 'Djibouti' , 'DM' : 'Dominica' , 'DO' : 'Dominican Republic' , 'EC' : 'Ecuador' , 'EG' : 'Egypt' , 'SV' : 'El Salvador' , 'GQ' : 'Equatorial Guinea' , 'ER' : 'Eritrea' , 'EE' : 'Estonia' , 'ET' : 'Ethiopia' , 'FK' : 'Falkland Islands (Malvinas)' , 'FO' : 'Faroe Islands' , 'FJ' : 'Fiji' , 'FI' : 'Finland' , 'FR' : 'France' , 'GF' : 'French Guiana' , 'PF' : 'French Polynesia' , 'TF' : 'French Southern Territories' , 'GA' : 'Gabon' , 'GM' : 'Gambia' , 'GE' : 'Georgia' , 'DE' : 'Germany' , 'GH' : 'Ghana' , 'GI' : 'Gibraltar' , 'GR' : 'Greece' , 'GL' : 'Greenland' , 'GD' : 'Grenada' , 'GP' : 'Guadeloupe' , 'GU' : 'Guam' , 'GT' : 'Guatemala' , 'GG' : 'Guernsey' , 'GN' : 'Guinea' , 'GW' : 'Guinea-Bissau' , 'GY' : 'Guyana' , 'HT' : 'Haiti' , 'HM' : 'Heard Island & Mcdonald Islands' , 'VA' : 'Holy See (Vatican City State)' , 'HN' : 'Honduras' , 'HK' : 'Hong Kong' , 'HU' : 'Hungary' , 'IS' : 'Iceland' , 'IN' : 'India' , 'ID' : 'Indonesia' , 'IR' : 'Islamic Republic Of Iran' , 'IQ' : 'Iraq' , 'IE' : 'Ireland' , 'IM' : 'Isle Of Man' , 'IL' : 'Israel' , 'IT' : 'Italy' , 'JM' : 'Jamaica' , 'JP' : 'Japan' , 'JE' : 'Jersey' , 'JO' : 'Jordan' , 'KZ' : 'Kazakhstan' , 'KE' : 'Kenya' , 'KI' : 'Kiribati' , 'KR' : 'Korea' , 'KW' : 'Kuwait' , 'KG' : 'Kyrgyzstan' , 'LA' : 'Lao People\'s Democratic Republic' , 'LV' : 'Latvia' , 'LB' : 'Lebanon' , 'LS' : 'Lesotho' , 'LR' : 'Liberia' , 'LY' : 'Libyan Arab Jamahiriya' , 'LI' : 'Liechtenstein' , 'LT' : 'Lithuania' , 'LU' : 'Luxembourg' , 'MO' : 'Macao' , 'MK' : 'Macedonia' , 'MG' : 'Madagascar' , 'MW' : 'Malawi' , 'MY' : 'Malaysia' , 'MV' : 'Maldives' , 'ML' : 'Mali' , 'MT' : 'Malta' , 'MH' : 'Marshall Islands' , 'MQ' : 'Martinique' , 'MR' : 'Mauritania' , 'MU' : 'Mauritius' , 'YT' : 'Mayotte' , 'MX' : 'Mexico' , 'FM' : 'Federated States Of Micronesia' , 'MD' : 'Moldova' , 'MC' : 'Monaco' , 'MN' : 'Mongolia' , 'ME' : 'Montenegro' , 'MS' : 'Montserrat' , 'MA' : 'Morocco' , 'MZ' : 'Mozambique' , 'MM' : 'Myanmar' , 'NA' : 'Namibia' , 'NR' : 'Nauru' , 'NP' : 'Nepal' , 'NL' : 'Netherlands' , 'AN' : 'Netherlands Antilles' , 'NC' : 'New Caledonia' , 'NZ' : 'New Zealand' , 'NI' : 'Nicaragua' , 'NE' : 'Niger' , 'NG' : 'Nigeria' , 'NU' : 'Niue' , 'NF' : 'Norfolk Island' , 'MP' : 'Northern Mariana Islands' , 'NO' : 'Norway' , 'OM' : 'Oman' , 'PK' : 'Pakistan' , 'PW' : 'Palau' , 'PS' : 'Occupied Palestinian Territory' , 'PA' : 'Panama' , 'PG' : 'Papua New Guinea' , 'PY' : 'Paraguay' , 'PE' : 'Peru' , 'PH' : 'Philippines' , 'PN' : 'Pitcairn' , 'PL' : 'Poland' , 'PT' : 'Portugal' , 'PR' : 'Puerto Rico' , 'QA' : 'Qatar' , 'RE' : 'Reunion' , 'RO' : 'Romania' , 'RU' : 'Russian Federation' , 'RW' : 'Rwanda' , 'BL' : 'Saint Barthelemy' , 'SH' : 'Saint Helena' , 'KN' : 'Saint Kitts AndNevis' , 'LC' : 'Saint Lucia' , 'MF' : 'Saint Martin' , 'PM' : 'Saint Pierre And Miquelon' , 'VC' : 'Saint Vincent And Grenadines' , 'WS' : 'Samoa' , 'SM' : 'San Marino' , 'ST' : 'Sao Tome And Principe' , 'SA' : 'Saudi Arabia' , 'SN' : 'Senegal' , 'RS' : 'Serbia' , 'SC' : 'Seychelles' , 'SL' : 'Sierra Leone' , 'SG' : 'Singapore' , 'SK' : 'Slovakia' , 'SI' : 'Slovenia' , 'SB' : 'Solomon Islands' , 'SO' : 'Somalia' , 'ZA' : 'South Africa' , 'GS' : 'South Georgia And Sandwich Isl.' , 'ES' : 'Spain' , 'LK' : 'Sri Lanka' , 'SD' : 'Sudan' , 'SR' : 'Suriname' , 'SJ' : 'Svalbard And Jan Mayen' , 'SZ' : 'Swaziland' , 'SE' : 'Sweden' , 'CH' : 'Switzerland' , 'SY' : 'Syrian Arab Republic' , 'TW' : 'Taiwan' , 'TJ' : 'Tajikistan' , 'TZ' : 'Tanzania' , 'TH' : 'Thailand' , 'TL' : 'Timor-Leste' , 'TG' : 'Togo' , 'TK' : 'Tokelau' , 'TO' : 'Tonga' , 'TT' : 'Trinidad And Tobago' , 'TN' : 'Tunisia' , 'TR' : 'Turkey' , 'TM' : 'Turkmenistan' , 'TC' : 'Turks And Caicos Islands' , 'TV' : 'Tuvalu' , 'UG' : 'Uganda' , 'UA' : 'Ukraine' , 'AE' : 'United Arab Emirates' , 'GB' : 'United Kingdom' , 'US' : 'United States' , 'UM' : 'United States Outlying Islands' , 'UY' : 'Uruguay' , 'UZ' : 'Uzbekistan' , 'VU' : 'Vanuatu' , 'VE' : 'Venezuela' , 'VN' : 'VietNam' , 'VG' : 'British Virgin Islands' , 'VI' : 'U.S. Virgin Islands' , 'WF' : 'Wallis And Futuna' , 'EH' : 'Western Sahara' , 'YE' : 'Yemen' , 'ZM' : 'Zambia' , 'ZW' : 'Zimbabwe' ];
        return (fullName.get(shortname) != null) ? (fullName.get(shortname)) : "";
    }

    def saveActivityForUpdatingRMMs(Map params, SignalRMMs signalRMMs, ValidatedSignal validatedSignal){
        String respNew, resp, attachments
        ActivityType activityType
        StringBuilder details = new StringBuilder()
        if(signalRMMs.communicationType == 'rmmType') {
            activityType = ActivityType.findByValue(ActivityTypeValue.RMMUpdated)
        } else {
            activityType = ActivityType.findByValue(ActivityTypeValue.CommunicationUpdated)
        }
        if (signalRMMs.type != params.type) {
            details.append("Type changed from '${signalRMMs.type}' to '${params.type}'\n")
        }
        if (signalRMMs.country != params.country) {
            String previousCountry = country(signalRMMs.country);
            String newCountry = country(params.country);
            if(previousCountry != "" || newCountry != "")
                details.append("Country changed from '${previousCountry}' to '${newCountry}'\n")
        }
        if(!(signalRMMs.description == null && params.description == '') && signalRMMs.description != params.description){
            if(signalRMMs.description == null){
                details.append("Description changed from '' to '${params.description}'\n")
            } else {
                details.append("Description changed from '${signalRMMs.description}' to '${params.description}'\n")
            }
        }

        def signalMemoEntriesCount = validatedSignal.signalRMMs?.count { it.type == "Signal Memo" && it.communicationType == "communication" }
        Integer indexName = signalMemoEntriesCount ? signalMemoEntriesCount + 1 : 1
        String extension = Holders.config.signal.autoNotification.reportFormat
        if(params.communicationType == 'communication' && params.type == 'Signal Memo'){
            if(!params.attachmentId){
                params.inputName = (validatedSignal.name.length() > 100 ? validatedSignal.name.substring(0, 100) : validatedSignal.name) + "_" +
                        validatedSignal.id + "-Notification Memo-" + indexName + '.' + extension.toLowerCase()
            }
        }
        if(signalRMMs.attachments){
            attachments = signalRMMs.attachments.collect { it.inputName ?: it.fileName }.join(',')
        } else {
            attachments = signalRMMs.getReferences().collect { it.inputName ?: it.referenceLink }.join(',')
        }
        if(!(attachments == '' && params.inputName == null) && attachments != params.inputName && params.inputName != null){
            details.append("FileName changed from '${attachments}' to '${params.inputName}'\n")
        }
        if(signalRMMs.assignedToId){
            resp = signalRMMs.assignedTo.name
        } else if(signalRMMs.assignedToGroupId){
            resp = signalRMMs.assignedToGroup.name
        } else {
            resp = signalRMMs.emailAddress
        }
        params.status = (params.status == 'null' || params.status == null) ? '' : params.status
        if((signalRMMs.status || params.status) && signalRMMs.status != params.status){
            details.append("Status changed from '${signalRMMs.status ?: ''}' to '${params.status}'\n")
        }
        if(signalRMMs.dueDate != (params.dueDate != "undefined" ? DateUtil.stringToDate(params.dueDate, DEFAULT_DATE_FORMAT, Holders.config.server.timezone): '')){
            details.append("Due Date changed from '${DateUtil.toDateStringWithoutTimezone(signalRMMs.dueDate)}' to '${params.dueDate != "undefined" ? params.dueDate : ''}'\n")
        }
        if (!params.rmmResp.contains('null') && !TextUtils.isEmpty(params.rmmResp)) {
            signalRMMs = userService.assignGroupOrAssignTo(params.rmmResp, signalRMMs)
            if(!signalRMMs.assignedTo && !signalRMMs.assignedToGroup){
                signalRMMs.emailAddress = params.rmmResp
            }
        }
        if(signalRMMs.assignedToId){
            respNew = signalRMMs.assignedTo.name
        } else if(signalRMMs.assignedToGroupId){
            respNew = signalRMMs.assignedToGroup.name
        } else {
            respNew = signalRMMs.emailAddress
        }
        resp = resp ?: ''
        respNew = respNew ?: ''
        if(resp != respNew){
            details.append("Resp. has changed from '${resp}' to '${respNew}'")
        }
        if(details) {
            def attr = [signal: validatedSignal.name]
            activityService.createActivityForSignal(validatedSignal, '', details.toString(), activityType, validatedSignal.assignedTo,
                    userService.getUser(), attr, validatedSignal.assignedToGroup)
        }
    }

    def saveActivityForDeletingRMMs(SignalRMMs signalRMMs, ValidatedSignal validatedSignal){
        String detailsMessage
        ActivityType activityType
        String communicationType = signalRMMs.communicationType
        String type = signalRMMs.type
        if(communicationType == 'rmmType'){
            detailsMessage = "RMM record with Type='${type}' has been deleted"
            activityType = ActivityType.findByValue(ActivityTypeValue.RMMDeleted)
        } else {
            detailsMessage = "Communication record with Type='${type}' has been deleted"
            activityType = ActivityType.findByValue(ActivityTypeValue.CommunicationDeleted)
        }
        def attr = [signal: validatedSignal.name]
        activityService.createActivityForSignal(validatedSignal, '',detailsMessage, activityType, validatedSignal.assignedTo,
                userService.getUser(), attr, validatedSignal.assignedToGroup)
    }

    def saveActivityForCommunicationAddedFromEmail(SignalRMMs signalRMMs, ValidatedSignal validatedSignal, Boolean isSystemUser = false){
        String resp,attachmentsEmail,detailsMessage
        ActivityType activityType
        String timezone = userService.user.preference.timeZone
        if(signalRMMs.assignedToId){
            resp = signalRMMs.assignedTo.name
        } else if(signalRMMs.assignedToGroupId){
            resp = signalRMMs.assignedToGroup.name
        } else {
            resp = signalRMMs.emailAddress
        }
        if(signalRMMs.attachments){
            attachmentsEmail = signalRMMs.attachments.collect { it.inputName ?: it.fileName }.join(',')
        } else {
            attachmentsEmail = signalRMMs.getReferences().collect { it.inputName ?: it.referenceLink }.join(',')
        }
        detailsMessage = "Communication created with Type='${signalRMMs.type ?: ''}', Description='${signalRMMs.description?:''}', FileName= '${attachmentsEmail}', Resp.='${resp}', Status='${signalRMMs.status}', DueDate='${DateUtil.toDateStringWithoutTimezone(signalRMMs.dueDate)}, EmailSent=${DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timezone)}'"
        activityType = ActivityType.findByValue(ActivityTypeValue.CommunicationAdded)
        def attr = [signal: validatedSignal.name]
        User performedBy = userService.getUser()
        if (isSystemUser) {
            performedBy = User.findByUsername(Constants.SYSTEM_USER)
        }
        activityService.createActivityForSignal(validatedSignal, '',detailsMessage, activityType, validatedSignal.assignedTo,
                performedBy, attr, validatedSignal.assignedToGroup)
    }

    void shareWithAllUsers() {
        Session session = sessionFactory.currentSession
        SQLQuery sql = null
        List<Map> signalsNotShareWithAll = []
        try {
            signalsNotShareWithAll = generateSignalCountList(session, SignalQueryHelper.signals_without_share_with)
            if (signalsNotShareWithAll) {
                signalsNotShareWithAll.collate(999).each{ def signalList ->
                    sql = session.createSQLQuery(SignalQueryHelper.signal_share_with_all(signalList?.collect {it.ID}.join(',')))
                    sql.executeUpdate()
                }
            }
            log.debug("${signalsNotShareWithAll?.size() ?: 0 } signals updated with 'All Users' share with group.")
        } catch (Throwable throwable) {
            log.error("Error in sharing signals to All users group ${throwable.printStackTrace()}")
            throw throwable
        } finally {
            session.flush()
            session.clear()
        }
        signalsNotShareWithAll?.each{
            updateSignalActivityLogForShareWithAllUsers(it?.ID as Long)}
    }
    def updateSignalActivityLogForShareWithAllUsers(Long signalId){
        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
        String details = "Shared With Group changed from '-' to '${validatedSignal?.shareWithGroup?.get(0).toString()?:'All Users'}' for signal '${validatedSignal.name}."
        Map attr = [signal: validatedSignal.name]
        activityService.createActivityForSignal(validatedSignal, "", details, ActivityType.findByValue(ActivityTypeValue.SharedWithChange),
                validatedSignal?.assignedTo as User, User?.findByUsername(Constants?.SYSTEM_USER) as User, attr)
        signalAuditLogService.createAuditLog([
                entityName : "validatedSignal",
                entityId   : signalId.toString(),
                moduleName : "Signal",
                category   : AuditTrail.Category.UPDATE.toString(),
                entityValue: validatedSignal.getInstanceIdentifierForAuditLog()
        ], [[propertyName: "shareWithGroup", oldValue: '', newValue: validatedSignal?.shareWithGroup?.get(0).toString()?:'[All Users]']] as List<Map>)
        CRUDService.update(validatedSignal)
        validatedSignal
    }

    SignalStatusHistory getLastSignalHistory(Long signalId, Long dispositionId) {
        ValidatedSignal validatedSignal = ValidatedSignal.findById(signalId)
        List<SignalStatusHistory> signalHistoryList = validatedSignal.signalStatusHistories.findAll {
            it.currentDispositionId == dispositionId && it.isAutoPopulate == true
        }
        signalHistoryList.sort({a,b-> b.dateCreated<=>a.dateCreated})
        signalHistoryList?signalHistoryList[0]:null
    }

    void deleteAutoPopulatedEntriesOnUndo(Long signalId, Long dispositionId) {
        ValidatedSignal validatedSignal = ValidatedSignal.findById(signalId)
        List<SignalStatusHistory> signalHistoryList = validatedSignal.signalStatusHistories.findAll {
           it.signalStatus != Constants.SignalHistory.SIGNAL_CREATED
        }?.sort{a,b -> b.id <=> a.id}
        List<SignalStatusHistory> latestSignalHistoryList = []
        boolean isDispositionId = false
        String defaultValidatedDate=Holders.config.signal.defaultValidatedDate;
        SignalStatusHistory validationStatusHist = signalHistoryList.find {it.signalStatus.equals(defaultValidatedDate)}
        Boolean validateNotRemoved = true
        for(SignalStatusHistory signalStatusHistory : signalHistoryList){
          if(signalStatusHistory.currentDispositionId == dispositionId || signalStatusHistory.currentDispositionId == null){
              if(signalStatusHistory.currentDispositionId == dispositionId){
                  log.info("defaultValidatedDate: ${defaultValidatedDate} signalStatusHistory.signalStatus: ${signalStatusHistory.signalStatus}")
                  if(signalStatusHistory.signalStatus.equals(defaultValidatedDate)){
                      signalStatusHistory.signalStatus = Constants.SignalHistory.SIGNAL_CREATED
                      signalStatusHistory.statusComment = Constants.SignalHistory.SIGNAL_CREATED
                      signalStatusHistory.skipAudit=true
                      signalStatusHistory.updateTime = new Date().getTime()
                      signalStatusHistory.save(flush: true)
                      validateNotRemoved = false
                  } else {
                      if(signalStatusHistory.signalStatus != 'Validated Signal'){
                          isDispositionId = true
                      }
                      if(signalStatusHistory.signalStatus == Constants.WorkFlowLog.DATE_CLOSED){
                          validatedSignal.actualDateClosed = null
                          validatedSignal.signalStatus = Constants.ONGOING_SIGNAL
                      }
                      latestSignalHistoryList.add(signalStatusHistory)
                  }
              } else if (isDispositionId) {
                  break
              }
          }else{
              // as status history is sorted, break when disposition ID are not matched
              break
          }
        }
        if(validateNotRemoved && validationStatusHist && latestSignalHistoryList &&
                validationStatusHist.updateTime == latestSignalHistoryList[0].updateTime){
            log.info("validationStatusHist.signalStatus: " + validationStatusHist.signalStatus)
            validationStatusHist.signalStatus = Constants.SignalHistory.SIGNAL_CREATED
            validationStatusHist.statusComment = Constants.SignalHistory.SIGNAL_CREATED
            validationStatusHist.skipAudit=true
            validationStatusHist.updateTime = new Date().getTime()
            validationStatusHist.save(flush: true)
        }
        createActivityForUndoHistory(latestSignalHistoryList,validatedSignal)
        validatedSignal.signalStatusHistories.removeAll(latestSignalHistoryList)
        CRUDService.update(validatedSignal)
    }

    def getAllValidatedObservationsData(Long signalId) {
        Map validatedObservationsData = new HashMap<>()
        Long totalCount = 0
        Long caseCountSize = 0
        Long pecsCountSize = 0
        Long articleCountSize = 0
        Long safetyObservationCountSize = 0
        String signalDeletionMessage = customMessageService.getMessage("app.signal.deletion.message")
        List<String> signalMessage = []
        validatedObservationsData.put(Constants.AlertConfigType.SINGLE_CASE_ALERT, getValidatedObservationsDataByAlertType(signalId, Constants.AlertConfigType.SINGLE_CASE_ALERT))
        validatedObservationsData.put(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, getValidatedObservationsDataByAlertType(signalId, Constants.AlertConfigType.AGGREGATE_CASE_ALERT))
        validatedObservationsData.put(Constants.AlertConfigType.EVDAS_ALERT, getValidatedObservationsDataByAlertType(signalId, Constants.AlertConfigType.EVDAS_ALERT))
        validatedObservationsData.put(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT, getValidatedObservationsDataByAlertType(signalId, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT))
        validatedObservationsData.put(Constants.AlertConfigType.AD_HOC_ALERT, getAdhocValidatedAlerts(signalId))
        validatedObservationsData.each { key, value ->
            // number of unique configurations should be counted and displayed in signal deletion popup
            if(key == Constants.AlertConfigType.SINGLE_CASE_ALERT){
                caseCountSize = value.alertConfiguration*.id.unique().size()
            } else if(key == Constants.AlertConfigType.AGGREGATE_CASE_ALERT || key == Constants.AlertConfigType.EVDAS_ALERT ){
                pecsCountSize += value.alertConfiguration*.id.unique().size()
            } else if(key == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT){
                articleCountSize = value.litSearchConfig*.id.unique().size()
            } else{
                safetyObservationCountSize = value.alertConfiguration*.id.unique().size()
            }
            if(key!=Constants.AlertConfigType.LITERATURE_SEARCH_ALERT){
                totalCount += value.alertConfiguration*.id.unique().size()
            } else{
                totalCount += value.litSearchConfig*.id.unique().size()
            }
        }
        caseCountSize > 0 ? signalMessage.add("Cases")  : ""
        pecsCountSize > 0 ? signalMessage.add("PECs") : ""
        articleCountSize > 0 ? signalMessage.add("Articles") : ""
        safetyObservationCountSize > 0 ? signalMessage.add("Safety Observations") : ""
        signalDeletionMessage += signalMessage.join('/')
        validatedObservationsData.put(Constants.SignalCounts.CASECOUNT, totalCount)
        validatedObservationsData.put("signalDeletionMessage",signalDeletionMessage)
        return validatedObservationsData
    }


    List getValidatedObservationsDataByAlertType(Long signalId, String alertType, Boolean getArchivedData = null) {
        def domain = alertService.getAlertDomainName(alertType)
        def archivedDomain = alertService.getAlertDomainName(alertType, true)
        Group workflowGroup = userService.getUser().workflowGroup

        List alertData = domain.createCriteria().list {
            if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                'exLitSearchConfig' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("workflowGroup", workflowGroup)
                }
            } else {
                'executedAlertConfiguration' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("adhocRun", false)
                    eq("workflowGroup", workflowGroup)
                }
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
        }

        List archivedAlertData = archivedDomain.createCriteria().list {
            if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                'exLitSearchConfig' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("workflowGroup", workflowGroup)
                }
            } else {
                'executedAlertConfiguration' {
                    eq("isDeleted", false)
                    eq("isEnabled", true)
                    eq("adhocRun", false)
                    eq("workflowGroup", workflowGroup)
                }
            }
            createAlias("validatedSignals", "vs", JoinType.INNER_JOIN)
            eq("vs.id", signalId)
        }

        if (getArchivedData == true) {
            return archivedAlertData
        } else if (getArchivedData == false) {
            return alertData
        } else {
            return alertData + archivedAlertData
        }

    }


    Set getAdhocValidatedAlerts(signalId) {
        ValidatedSignal signal = ValidatedSignal.findById(signalId)
        Set<AdHocAlert> adhocAlerts = signal.adhocAlerts
        Group workflowGroup = userService.getUser().workflowGroup
        adhocAlerts = adhocAlerts.findAll {
            it.workflowGroup.id == workflowGroup.id
        }
        return adhocAlerts
    }

    void deleteValidatedSignals(List<Long> signalIds) {
        log.info("Deleting signals with ids :::: ${signalIds}")
        deleteSignalChildAssociationsData(signalIds)
        String signalDeleteQuery = SignalQueryHelper.delete_signal_by_ids(signalIds.join(','))
        SQLQuery sql = null
        Session session = null
        Transaction tx = null                                   //Fix for bug PVS-55059
        try {
            log.info("Now Deleting Signals from Database.")
            session = sessionFactory.openSession()
            tx = session.beginTransaction()                     //Fix for bug PVS-55059
            sql = session.createSQLQuery(signalDeleteQuery)
            sql.executeUpdate()
            log.info("Successfully deleted signals.")
            tx.commit()                                         //Fix for bug PVS-55059
        } catch (Throwable throwable) {
            if (tx != null && tx.getStatus()?.canRollback()) {  //Fix for bug PVS-55059
                tx.rollback()
            }
            throw throwable
        } finally {
            if (session != null) {                              //Fix for bug PVS-55059
                session.flush()
                session.clear()
                if (session.isOpen()) {
                    session.close()
                }
            }
        }

    }

    void deleteSignalChildAssociationsData(List<Long> signalIds) {
        //when adding new child properties, skipAudit should be added in respective domain
        List childrenPropertyNames = ["actions"]
        try {
            log.info("Now Deleting children table data from Database.")
            signalIds.each { signalId ->
                ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
                if (validatedSignal) {
                    childrenPropertyNames?.each { propertyName ->
                        def data = validatedSignal."${propertyName}"
                        if (data) {
                            data*.skipAudit = true
                            validatedSignal.actions?.each { it ->
                                Meeting meeting = Meeting.get(it?.meetingId)
                                meeting?.removeFromActions(it) //removing action from the list of actions associated with meeting.
                            }
                            data.clear()
                        }
                    }
                    validatedSignal.save(flush: true)
                }
            }
            log.info("Successfully deleted.")
        } catch (Throwable throwable) {
            throw throwable
        }
    }

    void deleteSpotfireReportsForSignals(List<Long> signalIds) {

        log.info("Deleting analysis file data for signals with ids :::: ${signalIds}")

        try {
            List<String> analysisFileNames = SpotfireNotificationQuery.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("fileName", "fileName")
                }
                eq("type", Constants.AlertConfigType.SIGNAL_MANAGEMENT)
                or {
                    signalIds.collate(1000).each{
                        'in'("executedConfigurationId", it)
                    }
                }
            }

            if (analysisFileNames.size() > 0) {
                analysisFileNames = analysisFileNames*.fileName?.collect { "'" + it + "'" }
                List<String> descriptions = spotfireService.getReportFilesDescriptionByName(analysisFileNames.join(','))
                if (descriptions.size() > 0) {
                    log.info("Now deleting files with description  : " + descriptions.toString())
                    spotfireService.deleteSfReports(descriptions)
                    log.info("Successfully deleted analysis data.")
                }
            }

        } catch (Throwable throwable) {
            throw throwable
        }
    }

    Map disassociateSignalsByAlertType(Long configId, String alertType, Boolean deleteLatest = true, String justification) {
        Map linkedSignalsDataMap = getLinkedSignalsDataMap(configId, alertType, deleteLatest)
        List<Map> alertValidatedSignalList = linkedSignalsDataMap.get("alertData")
        List<Map> archivedAlertValidatedSignalList = linkedSignalsDataMap.get("archivedAlertData")

        Map joinTableNames = ["SingleCaseAlert"           : "VALIDATED_SINGLE_ALERTS",
                              "ArchivedSingleCaseAlert"   : "VALIDATED_ARCHIVED_SCA",
                              "AggregateCaseAlert"        : "VALIDATED_AGG_ALERTS",
                              "ArchivedAggregateCaseAlert": "VALIDATED_ARCHIVED_ACA",
                              "EvdasAlert"                : "VALIDATED_EVDAS_ALERTS",
                              "ArchivedEvdasAlert"        : "VALIDATED_ARCH_EVDAS_ALERTS",
                              "LiteratureAlert"           : "VALIDATED_LITERATURE_ALERTS",
                              "ArchivedLiteratureAlert"   : "VALIDATED_ARCHIVED_LIT_ALERTS"]

        Map joinColumnNames = ["SingleCaseAlert"           : "SINGLE_ALERT_ID",
                               "ArchivedSingleCaseAlert"   : "ARCHIVED_SCA_ID",
                               "AggregateCaseAlert"        : "AGG_ALERT_ID",
                               "ArchivedAggregateCaseAlert": "ARCHIVED_ACA_ID",
                               "EvdasAlert"                : "EVDAS_ALERT_ID",
                               "ArchivedEvdasAlert"        : "ARCHIVED_EVDAS_ALERT_ID",
                               "LiteratureAlert"           : "LITERATURE_ALERT_ID",
                               "ArchivedLiteratureAlert"   : "ARCHIVED_LIT_ALERT_ID"]

        if (!alertValidatedSignalList.isEmpty()) {

            def domainName = alertService.getAlertDomainName(alertType)

            String joinTableName = joinTableNames.get(domainName.getSimpleName())
            String joinColumnName = joinColumnNames.get(domainName.getSimpleName())

            try {
                SQLQuery sql = null
                Session session = sessionFactory.currentSession


                String sqlStatement = SignalQueryHelper.disassociate_signal_from_pec(joinTableName, joinColumnName, alertValidatedSignalList*.id.join(","))
                log.info("Now disassociating signals from : " + domainName.getSimpleName())
                log.info(sqlStatement)
                log.info("Successfully disassociated")
                sql = session.createSQLQuery(sqlStatement)
                sql.executeUpdate()
                session.flush()
                session.clear()
                return linkedSignalsDataMap
            } catch (Exception ex) {
                log.error(ex.printStackTrace())
            }
        }
        if (!archivedAlertValidatedSignalList.isEmpty()) {
            SQLQuery sql = null
            Session session = sessionFactory.currentSession

            def archivedDomainName = alertService.getAlertDomainName(alertType, true)
            String archivedJoinTableName = joinTableNames.get(archivedDomainName.getSimpleName())
            String archivedJoinColumnName = joinColumnNames.get(archivedDomainName.getSimpleName())

            String archivedsqlStatement = SignalQueryHelper.disassociate_signal_from_pec(archivedJoinTableName, archivedJoinColumnName, archivedAlertValidatedSignalList*.id.join(","))
            log.info("Now disassociating signals from :: " + archivedDomainName.getSimpleName())
            log.info(archivedsqlStatement)
            log.info("Successfully disassociated")
            sql = session.createSQLQuery(archivedsqlStatement)
            sql.executeUpdate()
            session.flush()
            session.clear()

        }
    }

    Map getLinkedSignalsDataMap(Long configId, String alertType, Boolean deleteLatest = true) {

        List<Map> alertValidatedSignalList = []
        List<Map> archivedAlertValidatedSignalList = []
        List<Map> uniqueValidatedSignals = []

        AlertSignalsDataDTO alertSignalsDataDTO = new AlertSignalsDataDTO(configId, alertType, deleteLatest)
        alertSignalsDataDTO.configuration = alertService.getConfiguration(alertSignalsDataDTO.domainName, configId)
        String alertName = alertSignalsDataDTO?.configuration?.name
        alertSignalsDataDTO.executedConfiguration = alertService.getExecutedConfiguration(alertSignalsDataDTO?.executedDomainName, configId)


        if (alertSignalsDataDTO.executedConfiguration) {
            Closure latestCriteria = { Long exConfigId ->
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("id", "id")
                    if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                        property("exLitSearchConfig.id", "executedAlertConfigurationId")
                    } else {
                        property("executedAlertConfiguration.id", "executedAlertConfigurationId")
                    }
                    property("name", "alertName")
                }
                if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                    eq("exLitSearchConfig.id", alertSignalsDataDTO.executedConfiguration.id)
                } else {
                    eq("executedAlertConfiguration.id", alertSignalsDataDTO.executedConfiguration.id)
                }

            }
            List alertList = alertSignalsDataDTO.alertDomainName.createCriteria().list(latestCriteria)

            List<Long> alertIdList = alertList.collect { it.id }

            if (!alertIdList.isEmpty()) {
                alertValidatedSignalList = getAlertValidatedSignalListByDomain(alertType, alertIdList, alertSignalsDataDTO.alertDomainName)
                uniqueValidatedSignals.addAll(alertValidatedSignalList)
            }


            if (!deleteLatest) {

                alertSignalsDataDTO.archivedExecutedConfigurations = alertService.getArchivedExecutedConfigurations(alertSignalsDataDTO?.executedConfiguration?.id, alertSignalsDataDTO.domainName, alertSignalsDataDTO.executedDomainName, alertType)
                List<Long> archivedExConfigIds = alertSignalsDataDTO?.archivedExecutedConfigurations*.id

                if (!archivedExConfigIds.isEmpty()) {
                    Closure allVersionCriteria = {
                        resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                        projections {
                            property("id", "id")
                            if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                                property("exLitSearchConfig.id", "executedAlertConfigurationId")
                            } else {
                                property("executedAlertConfiguration.id", "executedAlertConfigurationId")
                            }
                            property("name", "alertName")
                        }
                        if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                            or {
                                archivedExConfigIds.collate(1000).each{
                                    'in'("exLitSearchConfig.id", it)
                                }
                            }
                        } else {
                            or {
                                archivedExConfigIds.collate(1000).each{
                                    'in'("executedAlertConfiguration.id", it)
                                }
                            }
                        }
                    }
                    List archivedAlertList = alertSignalsDataDTO.archivedAlertDomainName.createCriteria().list(allVersionCriteria)
                    List<Long> archivedAlertIdList = archivedAlertList.collect { it.id }

                    if (!archivedAlertIdList.isEmpty()) {
                        archivedAlertValidatedSignalList = getAlertValidatedSignalListByDomain(alertType, archivedAlertIdList, alertSignalsDataDTO.archivedAlertDomainName)
                        uniqueValidatedSignals.addAll(archivedAlertValidatedSignalList)
                    }
                }
            }

            if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                uniqueValidatedSignals = uniqueValidatedSignals?.unique {
                    [it.signalId, it.caseNumber, it.eventName]
                }
            } else if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                uniqueValidatedSignals = uniqueValidatedSignals?.unique {
                    [it.signalId, it.productId, it.soc, it.eventName]
                }
            } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                uniqueValidatedSignals = uniqueValidatedSignals?.unique {
                    [it.signalId, it.soc, it.eventName]
                }
            }

        }

        int uniqueValidatedSignalsSize = (!uniqueValidatedSignals.isEmpty()) ? (uniqueValidatedSignals*.signalId.unique().size()) : 0

        return ["data": uniqueValidatedSignals, "size": uniqueValidatedSignalsSize, "alertData": alertValidatedSignalList, "archivedAlertData": archivedAlertValidatedSignalList, "alertName": alertName]

    }

    List<Map> getAlertValidatedSignalListByDomain(String alertType, List<Long> alertIdList, def domain) {
        List<Map> results = domain.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    property("productName", "productName")
                    property("caseNumber", "caseNumber")
                    property("pt", "eventName")
                } else if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    property("productName", "productName")
                    property("soc", "soc")
                    property("productId", "productId")
                    property("pt", "eventName")
                } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                    property("articleTitle", "title")
                    property("articleAuthors", "articleAuthor")
                } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                    property("soc", "soc")
                    property("substance", "productName")
                    property("pt", "eventName")
                }
                'validatedSignals' {
                    property("id", "signalId")
                    property("name", "name")
                }
            }
            and {
                sizeGt("validatedSignals", 0)
                or {
                    alertIdList.collate(1000).each {
                        'in'('id', it)
                    }
                }
            }
        } as List<Map>
        results
    }

    def disassociatePECsByAlertTypeAndSignalId(Long signalId, String alertType, String justification) {
        Map joinTableNames = ["SingleCaseAlert"           : "VALIDATED_SINGLE_ALERTS",
                              "ArchivedSingleCaseAlert"   : "VALIDATED_ARCHIVED_SCA",
                              "AggregateCaseAlert"        : "VALIDATED_AGG_ALERTS",
                              "ArchivedAggregateCaseAlert": "VALIDATED_ARCHIVED_ACA",
                              "EvdasAlert"                : "VALIDATED_EVDAS_ALERTS",
                              "ArchivedEvdasAlert"        : "VALIDATED_ARCH_EVDAS_ALERTS",
                              "LiteratureAlert"           : "VALIDATED_LITERATURE_ALERTS",
                              "ArchivedLiteratureAlert"   : "VALIDATED_ARCHIVED_LIT_ALERTS",
                              "AdHocAlert"                : "VALIDATED_ADHOC_ALERTS",]

        def domainName = alertService.getAlertDomainName(alertType)
        String joinTableName = joinTableNames.get(domainName?.getSimpleName())

        try {
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            String sqlStatement = SignalQueryHelper.disassociate_pec_from_signal(joinTableName, signalId)
            log.info("Disassociating ${domainName?.getSimpleName()} validated observations.")
            log.info(sqlStatement)
            log.info("Successfully disassociated.")
            sql = session.createSQLQuery(sqlStatement)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        }


        def archivedDomainName = alertService.getAlertDomainName(alertType, true)
        String archivedJoinTableName = joinTableNames.get(archivedDomainName?.getSimpleName())

        try {
            SQLQuery sql = null
            Session session = sessionFactory.currentSession

            String archivedsqlStatement = SignalQueryHelper.disassociate_pec_from_signal(archivedJoinTableName, signalId)
            log.info("Disassociating ${archivedDomainName?.getSimpleName()} validated observations..")
            log.info(archivedsqlStatement)
            log.info("Successfully disassociated.")
            sql = session.createSQLQuery(archivedsqlStatement)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    void createActivityForUndoHistory(List<SignalStatusHistory> latestSignalStatusHistoryList, ValidatedSignal validatedSignal) {
        latestSignalStatusHistoryList.each {
            if ((grailsApplication.config.pvsignal.showAutoGeneratedHistory || it.dispositionUpdated) && (it.signalStatus != Constants.WorkFlowLog.DUE_DATE)) {
                String details = "${it.signalStatus} status removed from workflow log due to undo disposition"
                String username = User.get(userService.getCurrentUserId())?.username ?: Constants.SYSTEM_USER
                saveActivityForUndoSignalStatusHistory(username, validatedSignal, details)
            }
        }
    }

    void saveActivityForUndoSignalStatusHistory(String userName, ValidatedSignal validatedSignal, String details) {
        ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.UndoAction)
        if (activityType && validatedSignal) {
            Activity activity = new Activity(
                    type: activityType,
                    performedBy: User.findByUsername(userName),
                    timestamp: DateTime.now(),
                    assignedTo: validatedSignal.assignedTo,
                    details: details
            )
            validatedSignal.addToActivities(activity)
        }
    }

    List removeDuplicateCases(List singleAlertCaseList,ValidatedSignal vs){
        List singleCaseAlertList = []
        singleAlertCaseList.each { sca->
            if (!validateSingleCases(vs.id).contains(sca)) {
                singleCaseAlertList.add(sca)
            }
        }
        return singleCaseAlertList
    }

    List<String> validateSingleCases(Long signalId) {
        String caseSql = """select sca.case_number as caseNum from validated_single_alerts vs inner join single_case_alert sca 
        on vs.single_alert_id = sca.id where vs.validated_signal_id = ${signalId}"""

        List<String> singleCases = []

        try {
            Session session = sessionFactory.currentSession
            SQLQuery sqlQuery = session.createSQLQuery(caseSql)
            sqlQuery.addScalar("caseNum", new StringType())
            singleCases = sqlQuery.list()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            ex.printStackTrace()
        }
        singleCases


    }

    String dateRangeForSpotfire(SignalAssessmentDateRangeEnum dateRange, Group group, Map params){
        List<String> dateRangeList = getDateRangeList(dateRange,group,params)
        return dateRangeList.collect{ convertDateStringToFormat(it as String,"dd/MM/yyyy","dd-MMM-yyyy")}.join(" - ")
    }

    Boolean parseMultiIngredientProductJson(String productJson) {
        try {
            JsonSlurper slurper = new JsonSlurper()
            Map productMap = slurper.parseText(productJson)
            Boolean isMultiIngredient = false
            String ingredientLevel = PVDictionaryConfig.ingredientColumnIndex

            // Checking if the list exists and is not empty
            if (productMap[ingredientLevel] && !productMap[ingredientLevel].isEmpty()) {
                def product = productMap[ingredientLevel][0] // Get the first product in the list
                if (product.isMultiIngredient != null) {
                    isMultiIngredient = product.isMultiIngredient
                }
            }
            return isMultiIngredient
        } catch (Exception exception) {
            log.info("Failed to parse parseMultiIngredientProductJson saving signal as non multi-ingredient")
            exception.printStackTrace()
            return  false
        }
        isMultiIngredient
    }

    void createActivityForDueDate(String dueDate, String updatedDueDate, ValidatedSignal signal){
        String details = "Due date changed from '${dueDate?:""}' to '${updatedDueDate?:""}'"
        String username = User.get(userService.getCurrentUserId())?.username ?: Constants.SYSTEM_USER
        saveActivityForUndoSignalStatusHistory(username, signal, details)
    }

    void createActivityForDispositionUndo(String currDispName, String oldDispName, ValidatedSignal signal){
        String details = "Disposition changed from '${oldDispName}' to '${currDispName}' for signal '${signal.name}'"
        String username = User.get(userService.getCurrentUserId())?.username ?: Constants.SYSTEM_USER
        saveActivityForUndoSignalStatusHistory(username, signal, details)
    }

    /**
     * Provides a transformed list of cases linked to a signal for signal summary report
     * @param signalId - signal id whose cases are required to be fetched
     * @return
     */
    List getSingleAlertListForSignalSummaryReport(String signalId) {
        List resultList = []
        getSingleCaseAlertListForSignal(signalId)?.each {
            def existingSignals = getSignalsFromAlertObj(it, Constants.AlertConfigType.SINGLE_CASE_ALERT)
            resultList.add([
                    "alertName"  : it.isStandalone ? '-' : it.alertName,
                    "priority"   : it.priority.value,
                    "caseNumber" : it.isCaseNumberAvailable ? getExportCaseNumber(it.caseNumber, it.followUpNumber) : it.caseNumber,
                    "productName": it.productName,
                    "eventPt"    : it.masterPrefTermAll,
                    "disposition": it.isStandalone ? '-' : it.disposition,
                    "signalNames": existingSignals.size() != 0 ? existingSignals.collect {
                        it.name
                    }.join(",") : "-"
            ])
        }
        return resultList
    }

    /**
     * This method updates all the product related fields to null of the signal
     * if the signal is created from a case whose product is blinded or redacted
     * @param validatedSignal - validated signal which is being created
     * @param user - current user
     * @param alert - Single case alert's object
     */
    void setProductValuesForBlindedData(ValidatedSignal validatedSignal, User user, def alert) {
        boolean isUserBlinded = cacheService.getUserBlindingCache()?.contains(user.id)
        boolean isBlindedConditionMet = (alert.studyBlindingStatus && (isUserBlinded || alert.caseBlindingStatus))
        Map blindingFieldsForGroup = userService.getBlindingFieldsForGroup(user.groups*.id)

        boolean isAllFieldsRedacted = blindingFieldsForGroup.isAllFieldsRedacted == 1
        boolean isUserMappingAvailable = blindingFieldsForGroup.isUserMappingAvailable
        isAllFieldsRedacted = !(!isUserMappingAvailable && !isAllFieldsRedacted)

        boolean isProductFieldRedacted = blindingFieldsForGroup.redactedFields?.contains('productProductId')

        if (isBlindedConditionMet || isProductFieldRedacted || isAllFieldsRedacted) {
            validatedSignal.productGroupSelection = null
            validatedSignal.products = null
            validatedSignal.productDictionarySelection = null
            validatedSignal.allProducts = null
            validatedSignal.productsAndGroupCombination = null
        }
    }

    Integer dueInCalculation(String timezone = "UTC", Date dueDate) {
        Integer dueIn = null
        if (Objects.nonNull(dueDate)) {
            DateTimeZone userTimeZone = DateTimeZone.forID(timezone)
            DateTime dueDateTime = new DateTime(dueDate, userTimeZone).withTimeAtStartOfDay()
            DateTime currentDateTime = new DateTime(userTimeZone).withTimeAtStartOfDay()
            dueIn = (dueDateTime.toDate().time - currentDateTime.toDate().time) / (1000 * 60 * 60 * 24) as Integer
        }
        return dueIn
    }
}

