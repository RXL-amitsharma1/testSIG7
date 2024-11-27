package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.DataTableSearchRequest
import com.rxlogix.EmailNotificationService
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.ActionStatus
import com.rxlogix.helper.ActivityHelper
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.util.TextUtils
import org.springframework.context.MessageSource


import static com.rxlogix.util.DateUtil.DEFAULT_DATE_FORMAT
import static org.springframework.http.HttpStatus.BAD_REQUEST

@Secured(["isAuthenticated()"])
class ActionController implements LinkHelper, ActivityHelper, AlertUtil {
    def userService
    def activityService
    def actionService
    def emailService
    def singleCaseAlertService
    def aggregateCaseAlertService
    def evdasAlertService
    MessageSource messageSource
    def CRUDService
    def meetingService
    def dynamicReportService
    def literatureActivityService
    def alertService
    def signalAuditLogService
    LiteratureAlertService literatureAlertService
    EmailNotificationService emailNotificationService

    public static String INVALID_DATE_ZERO =  "Invalid date! Year should not be 0000"

    def index() {
        redirect(action: "list", params: params)
    }

    def listByCurrentUser(DataTableSearchRequest searchRequest, String filterType) {

        Map actionResultList = [:]
        User currentUser = userService.getUser()
        filterType = filterType ?: Constants.ActionItemFilterType.MY_OPEN
        actionResultList = actionService.getActionList(searchRequest, currentUser, filterType)
        render actionResultList as JSON
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def list() {
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        String literatureArticleUrl = grailsApplication.config.app.literature.article.url
        Map userViewAccessMap = actionService.getUserViewAccess()
        Map actionPropertiesJSON = actionService.actionPropertiesJSON([])
        Boolean hasSignalReviewerAccess = hasReviewerAccess(Constants.AuditLog.ACTION)
        String buttonClass = hasSignalReviewerAccess?"":"hidden"
        [actionTypeList      : actionTypeAndActionMap.actionTypeList, actionConfigList: actionPropertiesJSON.configs,
         literatureArticleUrl: literatureArticleUrl,buttonClass:buttonClass,
         actionPropertiesMap : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap), userViewAccessMap: userViewAccessMap]
    }

    def create() {
        def id = params.id
        Alert alert = Alert.findById(id)
        def actionInstance = new Action()
        actionInstance.alert = alert
        actionInstance.properties = params
        return [actionInstance: actionInstance, alertId: alert.id, backUrl: request.getHeader('referer')]
    }

    //todo: Refactor this method
    def save(String appType) {
        Boolean isArchived = params.boolean('isArchived') ?: false
        Boolean isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        List formats = grailsApplication.config.grails.databinding.dateFormats
        String timezone = grailsApplication.config.server.timezone
        String userTimeZone = userService?.getUser()?.preference?.timeZone ?: 'UTC'
        List errors = []
        def backUrl = params.backUrl

        Action action = new Action(params)
        if (!TextUtils.isEmpty(params['dueDate'])) {
            action.dueDate = DateUtil.parseDate(params['dueDate'], DEFAULT_DATE_FORMAT)
        }
        if (!TextUtils.isEmpty(params['completedDate'])) {
            action.completedDate = DateUtil.parseDate(params['completedDate'], DEFAULT_DATE_FORMAT)
        } else {
            action.completedDate = null
        }
        Meeting meeting

        try {
            if (!TextUtils.isEmpty(params.config) && ActionConfiguration.findById(params.config).displayName == 'Meeting') {
                meeting = setMeetingProperties(params, action)
                action.meetingId = meeting?.id
            } else {
                action.meetingId = null
            }
            User currentUser = userService.getUser()
            Action actionInstance = actionService.populate(action, currentUser, params.alertId, params.assignedToValue, appType, isArchived, isEmbase)
            if (actionInstance.meetingId) {
                CRUDService.update(meeting)
            }
            if (actionInstance?.id) {
                flash.message = "One action has been created"
                flash.args = [actionInstance.id]
                flash.defaultMessage = "Action ${actionInstance.id} created"

                def alert
                String productName
                String alertName
                List<User> recipientsList = userService.getUserListFromAssignToGroup(actionInstance)
                if (appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    alert = isArchived ? ArchivedSingleCaseAlert.findById(params.alertId) : SingleCaseAlert.findById(params.alertId)
                    alert.actionCount = alert?.actionCount ? (alert.actionCount + 1) : 1
                    Long executedConfigId = alert.executedAlertConfigurationId
                    ExecutedConfiguration executedConfig = singleCaseAlertService.getExecConfigurationById(executedConfigId)
                    productName = alert.productName
                    alertName = alert.name
                    activityService.createActivity(executedConfig, ActivityType.findByValue(ActivityTypeValue.ActionCreated),
                            currentUser, "Action [$action.id] created with Action Type '${action?.type?.displayName}', Action '${action?.config?.displayName}', Assigned To '${action?.assignedTo?.fullName ?: action?.assignedToGroup?.name}', Due Date '${action?.dueDate ? DateUtil.toDateString(action?.dueDate) :  "-"}', Status '${action?.actionStatus}', Completion Date '${action?.completedDate ?  DateUtil.toDateString(action?.completedDate) : "-"}', Action Details '${action?.details}', Comments '${action?.comments ?: ""}'", null,
                            ['For Case Number': alert.caseNumber], productName, alert.getAttr('masterPrefTermAll_7') ?: alert.pt, action?.assignedTo, alert.caseNumber, action?.assignedToGroup, actionInstance.guestAttendeeEmail)
                } else if (appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    alert = isArchived ? ArchivedAggregateCaseAlert.findById(params.alertId) : AggregateCaseAlert.findById(params.alertId)
                    alert.actionCount = alert?.actionCount ? (alert.actionCount + 1) : 1
                    Long executedConfigId = alert.executedAlertConfigurationId
                    ExecutedConfiguration executedConfig = aggregateCaseAlertService.getExecConfigurationById(executedConfigId)
                    productName = alert.productName
                    alertName = alert.name
                    activityService.createActivity(executedConfig, ActivityType.findByValue(ActivityTypeValue.ActionCreated),
                            currentUser, "Action [$action.id] created with Action Type '${action?.type?.displayName}', Action '${action?.config?.displayName}', Assigned To '${action?.assignedTo?.fullName ?: action?.assignedToGroup?.name}', Due Date '${action?.dueDate ? DateUtil.toDateString(action?.dueDate) :  "-"}', Status '${action?.actionStatus}', Completion Date '${action?.completedDate ?  DateUtil.toDateString(action?.completedDate) : "-"}', Action Details '${action?.details}', Comments '${action?.comments ?: ""}'", null,
                            ['For Aggregate Alert'], productName, alert.pt, action?.assignedTo, null, action?.assignedToGroup, actionInstance.guestAttendeeEmail)
                } else if (appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
                    alert = ValidatedSignal.findById(params.alertId)
                    activityService.createActivityForSignal(alert, null, "Action [$action.id] created with Action Type '${action?.type?.displayName}', Action '${action?.config?.displayName}', Assigned To '${action?.assignedTo?.fullName ?: action?.assignedToGroup?.name}', Due Date '${action?.dueDate ? DateUtil.toDateString(action?.dueDate) :  "-"}', Status '${action?.actionStatus}', Completion Date '${action?.completedDate ?  DateUtil.toDateString(action?.completedDate) : "-"}', Action Details '${action?.details}', Comments '${action?.comments ?: ""}'",
                            ActivityType.findByValue(ActivityTypeValue.ActionCreated), action?.assignedTo, currentUser,
                            ['For Signal': alert.name], action?.assignedToGroup)
                    if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_CREATION_UPDATION)) {
                        alertName = alert.name
                        productName = getNameFieldFromJson(alert.products)
                        String alertLink = createHref("validatedSignal", "details", [id: alert.id])
                        if (!params.type || params.type == 'null') {

                            (meeting.attendees.email + userService.getUser().email + action.guestAttendeeEmail).flatten().unique()?.each { String email ->
                                emailNotificationService.mailHandlerForActionCreation(actionInstance, email, null, productName, alertName, timezone, alertLink,appType)
                            }

                        } else {
                            (recipientsList.email + userService.getUser().email + action.guestAttendeeEmail).flatten().unique()?.each { String email ->
                                emailNotificationService.mailHandlerForActionCreation(actionInstance, email, null, productName, alertName, timezone, alertLink,appType)
                            }

                        }
                    }
                } else if (appType == Constants.AlertConfigType.EVDAS_ALERT) {
                    alert = isArchived ? ArchivedEvdasAlert.findById(params.alertId) : EvdasAlert.findById(params.alertId)
                    alert.actionCount = alert?.actionCount ? (alert.actionCount + 1) : 1
                    Long executedConfigId = alert.executedAlertConfigurationId
                    ExecutedEvdasConfiguration executedConfig = ExecutedEvdasConfiguration.get(executedConfigId)
                    productName = alert.getProductName()
                    alertName = alert.getName()
                    activityService.createActivityForEvdas(executedConfig,
                            ActivityType.findByValue(ActivityTypeValue.ActionCreated),
                            currentUser,
                            "Action [$action.id] created with Action Type '${action?.type?.displayName}', Action '${action?.config?.displayName}', Assigned To '${action?.assignedTo?.fullName ?: action?.assignedToGroup?.name}', Due Date '${action?.dueDate ? DateUtil.toDateString(action?.dueDate) :  "-"}', Status '${action?.actionStatus}', Completion Date '${action?.completedDate ?  DateUtil.toDateString(action?.completedDate) : "-"}', Action Details '${action?.details}', Comments '${action?.comments ?: ""}'",
                            null,
                            ['For EVDAS Alert': alertName],
                            productName,
                            alert?.pt,
                            action?.assignedTo,
                            null, action?.assignedToGroup, actionInstance.guestAttendeeEmail)
                } else if (params.appType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {

                    def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)

                    alert = domain.findById(params.alertId)
                    alert.actionCount = alert?.actionCount ? (alert.actionCount + 1) : 1
                    alertName = alert.name
                    productName = alert.productSelection
                    literatureActivityService.createLiteratureActivity(alert.exLitSearchConfig, ActivityType.findByValue(ActivityTypeValue.ActionCreated),
                            userService.getUser(), "Action [$action.id] created with Action Type '${action?.type?.displayName}', Action '${action?.config?.displayName}', Assigned To '${action?.assignedTo?.fullName ?: action?.assignedToGroup?.name}', Due Date '${action?.dueDate ? DateUtil.toDateString(action?.dueDate) :  "-"}', Status '${action?.actionStatus}', Completion Date '${action?.completedDate ?  DateUtil.toDateString(action?.completedDate) : "-"}', Action Details '${action?.details}', Comments '${action?.comments ?: ""}'", null, alert.productSelection, alert.eventSelection, action?.assignedTo, alert.searchString, alert.articleId, action?.assignedToGroup, actionInstance.guestAttendeeEmail)
                    if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_CREATION_UPDATION)) {
                        String alertLink = createHref("action", "list", null)
                        (recipientsList.email + userService.getUser().email + action.guestAttendeeEmail).flatten().unique()?.each { String email ->
                            emailNotificationService.mailHandlerForActionCreation(actionInstance, email, alert.articleId, productName, alertName, timezone, alertLink,appType)
                        }
                    }
                } else {
                    alert = Alert.findById(params.alertId)
                    alertName = alert?.name
                    productName = getNameFieldFromJson(alert?.productSelection)
                    activityService.create(alert, ActivityType.findByValue(ActivityTypeValue.ActionCreated),
                            currentUser, "Action [$action.id] created with Action Type '${action?.type?.displayName}', Action '${action?.config?.displayName}', Assigned To '${action?.assignedTo?.fullName ?: action?.assignedToGroup?.name}', Due Date '${action?.dueDate ? DateUtil.toDateString(action?.dueDate) :  "-"}', Status '${action?.actionStatus}', Completion Date '${action?.completedDate ?  DateUtil.toDateString(action?.completedDate) : "-"}', Action Details '${action?.details}', Comments '${action?.comments ?: ""}'", null, actionInstance)
                }

                //This will send mail to all the users present in a group
                if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_CREATION_UPDATION)) {
                    if (appType != Constants.AlertConfigType.SIGNAL_MANAGEMENT && appType != Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                        String alertLink = createHref("action", "list", null)
                        (recipientsList.email + userService.getUser().email + action.guestAttendeeEmail).flatten().unique()?.each { String email ->
                            emailNotificationService.mailHandlerForActionCreation(actionInstance, email, null, productName, alertName, timezone, alertLink,appType)
                        }
                    }
                }
                //Added new parameter action?.assignedToGroup for PVS-68125
                actionService.addNotification(params.appType, currentUser, action?.assignedTo?.fullName ?: action?.assignedToGroup?.name, action?.dueDate ? DateUtil.toDateString(action?.dueDate) : "-", action?.completedDate ? DateUtil.toDateString(action?.completedDate) : "-", action?.details, action?.comments ?: "", action?.type?.displayName, action?.config?.displayName, action?.actionStatus, productName, action?.assignedToGroup)

                if (alert) {
                    render([actionInstance: actionInstance, actionCount: alert.actions.size()] as JSON)
                } else {
                    ResponseDTO responseDTO = new ResponseDTO()
                    responseDTO.status = true
                    responseDTO.data = []
                    render(responseDTO as JSON)
                }
            } else {
                render(contentType: 'application/json', status: BAD_REQUEST) {
                    [
                            actionInstance: actionInstance,
                            backUrl       : backUrl,
                            errors        : actionInstance.errors.allErrors.collect {
                                messageSource.getMessage(it, Locale.default)
                            }
                    ]
                }

            }
        } catch (grails.validation.ValidationException vx) {
            vx.printStackTrace()
            boolean isNull = false
            Boolean isValidDate = true
            ResponseDTO responseDTO = new ResponseDTO()
            responseDTO.status = false
            if (vx.errors?.fieldErrors?.find { it.field == 'guestAttendeeEmail' }) {
                responseDTO.data = ["Entered email address is not valid."]
            }
            if (null != action.dueDate && !DateUtil.checkValidDateYear(action.dueDate)) {
                isValidDate = false
            }
            if (null != action.completedDate && !DateUtil.checkValidDateYear(action.completedDate)) {
                isValidDate = false
            }
            if (vx.toString()?.contains("Action.completedDate.nullable")) {
                if(responseDTO.data ) {
                    responseDTO.data << [messageSource.getMessage("com.rxlogix.config.Action.completedDate.nullable", null, Locale.default)]
                }
                else {
                    responseDTO.data = [messageSource.getMessage("com.rxlogix.config.Action.completedDate.nullable", null, Locale.default)]
                }
            }

            if (vx.toString()?.contains("Action.details.nullable") ||vx.toString()?.contains("Action.assignedTo.assignedTo.nullable")||
                    vx.toString()?.contains("Action.dueDate.nullable")|| vx.toString()?.contains("Action.config.nullable")|| vx.toString()?.contains("Action.type.nullable")) {
                isNull = true
                if(responseDTO.data ) {
                    responseDTO.data << [message(code: "action.configuration.all.fields.required")]
                }
                else {
                    responseDTO.data = [message(code: "action.configuration.all.fields.required")]
                }
                if (!isValidDate) {
                    responseDTO.data = [message(code: "action.configuration.all.fields.required"), INVALID_DATE_ZERO]
                }
            }else if(vx.toString()?.contains("Action.completedDate.future")){
                responseDTO.data = [message(code: "com.rxlogix.config.Action.completedDate.future")]
            }
            if (!isValidDate && !isNull) {
                responseDTO.data = [INVALID_DATE_ZERO]
            }

            render(responseDTO as JSON)

        } catch (Exception e) {
            e.printStackTrace()
            action.errors.allErrors.each {
                  errors.add(messageSource.getMessage(it, Locale.default))
            }
            if (!TextUtils.isEmpty(params.config) && ActionConfiguration.findById(params.config)?.displayName == 'Meeting' && meeting == null) {
                  errors.add(message(code: "meetingTitle.nullable"))
            }
            ResponseDTO responseDTO = new ResponseDTO()
            responseDTO.status = false
            responseDTO.data = errors
            render(responseDTO as JSON)
        }
    }

    //Need to change this method
    def setMeetingProperties(params, Action action) {
        //set dummy value for action type as it is null in this scenario and it is nullable false
        action.type = params.meetingId ? (ActionType.findByValue("AESM") ?: ActionType.first()) : ''
        action.actionStatus = params.actionStatus
        action.createdDate = new Date()
        Meeting meeting
        try {
            meeting = Meeting.findById(params.meetingId)
            meeting.addToActions(action)
        } catch (Exception e) {
            e.printStackTrace()
        }
        meeting
    }

    def show() {
        def actionInstance = Action.get(params.id)
        if (!actionInstance) {
            flash.message = "action.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Action not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [actionInstance: actionInstance]
        }
    }

    def edit() {
        def actionInstance = Action.get(params.id)
        if (!actionInstance) {
            flash.message = "action.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Action not found with id ${params.id}"
            redirect(action: "index", controller: 'dashboard')
        } else {
            return [actionInstance: actionInstance]
        }
    }

    def update() {
        def actionInstance = Action.get(params.id)
        if (actionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (actionInstance.version > version) {

                    actionInstance.errors.rejectValue("version",
                            "action.optimistic.locking.failure",
                            "Another user has updated this Action while you were editing")
                    render(view: "edit", model: [actionInstance: actionInstance])
                    return
                }
            }
            actionInstance.properties = params
            def formats = grailsApplication.config.grails.databinding.dateFormats
            def timezone = userService.getUser()?.preference?.timeZone ?: grailsApplication.config.server.timezone

            if (params['dueDate'])
                actionInstance.setDueDate(DateUtil.parseDate(params.dueDate, DEFAULT_DATE_FORMAT))

            if (!TextUtils.isEmpty(params['completedDate'])) {
                actionInstance.completedDate = DateUtil.parseDate(params['completedDate'], DEFAULT_DATE_FORMAT)
            } else {
                actionInstance.completedDate = null
            }

            if (!actionInstance.hasErrors() && actionInstance.save()) {
                flash.message = "action.updated"
                flash.args = [params.id]
                flash.defaultMessage = "Action ${params.id} updated"

                if (actionInstance.config?.isEmailEnabled) {
                    def alertLink = createHref("adHocAlert", "alertDetail", ["id": actionInstance.alert.id])
                    def productName = actionInstance.alert.productSelection ? getNameFieldFromJson(actionInstance.alert.productSelection) : getGroupNameFieldFromJson(actionInstance.alert.productGroupSelection)
                    emailService.sendActionCreateEmail(['toAddress': [actionInstance.assignedTo?.email, userService.getUser().email],
                                                        'inboxType': "Action Updated",
                                                        'title'    : message(code: "app.email.action.create.title",
                                                                args: [productName]),
                                                        'map'      : [action         : actionInstance, config: actionInstance.config, actionType: actionInstance.type,
                                                                      'alertInstance': actionInstance.alert, "alertLink": alertLink, "productName": productName]])
                }

                redirect(action: "index", controller: 'dashboard')
            } else {
                render(view: "edit", model: [actionInstance: actionInstance])
            }
        } else {
            flash.message = "action.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Action not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def bulkUpdateAction() {
        def timezone = grailsApplication.config.server.timezone
        JsonSlurper jsonSlurper = new JsonSlurper()
        def actionListObj = jsonSlurper.parseText(params.actionList)
        def actionIdsList = []
        actionListObj.each {
            actionIdsList.add(Long.parseLong(it))
        }
        def actionListFromDB = Action.findAllByIdInList(actionIdsList)
        Date completionDate = new Date()
        actionListFromDB.each {
            def actionToUpdate = it
            if (actionToUpdate) {
                def action = new Action()

                if (!TextUtils.isEmpty(params.dueDate) && actionToUpdate.dueDate != (new Date(params.dueDate)).format(DEFAULT_DATE_FORMAT)) {
                    actionToUpdate.dueDate = DateUtil.parseDate(params.dueDate, DEFAULT_DATE_FORMAT)
                }


                def description = prepareActivityDescription(actionToUpdate, action, "UTC")

                if (!TextUtils.isEmpty(params.actionStatus) && params.actionStatus != actionToUpdate.actionStatus) {
                    actionToUpdate.actionStatus = params.actionStatus
                    if (action.actionStatus == ActionStatus.Closed.name()) {
                        actionToUpdate.completedDate = completionDate
                    }
                }
                if (!TextUtils.isEmpty(params.assignedTo) && User.get(Long.parseLong(params.assignedTo)) != actionToUpdate.assignedTo) {
                    actionToUpdate.assignedTo = User.get(Long.parseLong(params.assignedTo))
                }

                def alertId = null
                def alertType = actionToUpdate.alertType

                if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    alertId = actionToUpdate.aggAlert.id
                } else if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    alertId = actionToUpdate.singleCaseAlert.id
                } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                    alertId = actionToUpdate.evdasAlert.id
                } else if (alertType == Constants.AlertConfigType.AD_HOC_ALERT) {
                    alertId = actionToUpdate.alert.id
                }

                def act = actionService.updateAction(actionToUpdate, description, alertId.toString(), userService.getUser())

                if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_CREATION_UPDATION)) {
                    if (!act.hasErrors()) {
                        if (act.config?.isEmailEnabled) {
                            def alertName = Constants.Commons.BLANK_STRING
                            def productName = Constants.Commons.BLANK_STRING
                            def alertLink = createHref("action", "list", null)
                            emailNotificationService.mailHandlerForActionUpdate(actionToUpdate, alertName, productName, timezone, alertLink)
                        }
                    }
                }
            }
        }
        render status: 200
    }


    def delete() {
        def actionInstance = Action.get(params.id)
        if (actionInstance) {
            try {
                actionInstance.delete()
                flash.message = "action.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "Action ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "action.not.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "Action ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = "action.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Action not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    def listActions() {
        render Action.findAll() as JSON
    }

    def read() {
        Action.findById(params.id)
    }

    def listByAlert() {
        def alertId = params.alertId
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        if (alertId) {
            def actions = actionService.listActionsForAlert(alertId, params.appType, params.boolean('isArchived'), isEmbase)
            render(actions as JSON)
        } else {
            response.status = 200
            render(text: "[]", contentType: 'application/json')
        }
    }

    def getById(Long id) {
        Action act = Action.findById(id)
        Map map = actionService.createActionDTO(act)
        render map as JSON
    }

    def selectValues() {
        def values = [types: ActionType.list(), configs: ActionConfiguration.list().sort({
            it.value.toUpperCase()
        }), allStatus      : ActionStatus.values()]
        render values as JSON
    }

    def updateAction(String appType, Long exeConfigId, String dueDate) {
        String userTimeZone = userService?.getUser()?.preference?.timeZone ?: 'UTC'
        boolean isArchived = params.boolean('isArchived') ?: false
        Boolean isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        Action action = new Action()
        bindData(action, params)

        if (params.assignedToValue) {
            String[] assigneeData = params.assignedToValue.split("_", 2)
            if (assigneeData[0] == "User") {
                User user = User.get(assigneeData[1] as Long)
                action.assignedTo = user
            } else if (assigneeData[0] == "UserGroup") {
                Group group = Group.get(assigneeData[1] as Long)
                action.assignedToGroup = group
            }
        }
        Action actionToUpdate = Action.findById(params.actionId)
        Map actionGroupAndAssignedToMap = [user: actionToUpdate.assignedTo, group: actionToUpdate.assignedToGroup]
        List oldUserList = userService.getUserListFromAssignToGroup(actionToUpdate)
        List formats = grailsApplication.config.grails.databinding.dateFormats
        String timezone = grailsApplication.config.server.timezone

        if (dueDate)
            action.setDueDate(DateUtils.truncate(new Date(dueDate), Calendar.DAY_OF_MONTH))

        String description = prepareActivityDescription(actionToUpdate, action, "UTC")

        actionToUpdate.details = action.details
        actionToUpdate.comments = action.comments
        actionToUpdate.config = action.config
        actionToUpdate.type = action.type
        actionToUpdate.actionStatus = action.actionStatus
        actionToUpdate.dueDate = action.dueDate ? action.dueDate : null
        actionToUpdate = userService.assignGroupOrAssignTo(params.assignedToValue, actionToUpdate)
        if (actionToUpdate.execConfigId == null) {
            actionToUpdate.execConfigId = exeConfigId ?: (params?.alertId?.isEmpty() ? null : params?.alertId as Long)
        }
        if (!actionToUpdate.assignedTo && !actionToUpdate.assignedToGroup)
            actionToUpdate.guestAttendeeEmail = params.assignedToValue
        if (params.completedDate) {
            actionToUpdate.setCompletedDate(DateUtils.truncate(new Date(params.completedDate), Calendar.DAY_OF_MONTH))
        } else {
            actionToUpdate.setCompletedDate(null)
        }

        User currentUser = userService.getUser()
        Action act = actionService.updateAction(actionToUpdate, description, params.alertId, currentUser, appType)
        List<User> recipientsList = userService.getUserListFromAssignToGroup(act)
        def domain

        if (!act.hasErrors()) {
            if (act.config?.isEmailEnabled) {
                String alertName = Constants.Commons.BLANK_STRING
                String productName = Constants.Commons.BLANK_STRING
                def alert
                if (params.alertId) {
                    if (appType == Constants.AlertConfigType.AD_HOC_ALERT) {
                        alert = Alert.createCriteria().get {
                            'actions' {
                                'eq'("id", actionToUpdate.id)
                            }
                        }
                        alertName = alert.name
                        productName = getNameFieldFromJson(alert.productSelection)
                    } else if (appType == Constants.AlertConfigType.EVDAS_ALERT) {
                        //TODO get doman name from evdasservice
                        domain = isArchived ? ArchivedEvdasAlert : EvdasAlert
                        alert = domain.createCriteria().get {
                            'actions' {
                                'eq'("id", actionToUpdate.id)
                            }
                        }
                        alertName = alert.name
                        productName = alert?.substance
                        ExecutedEvdasConfiguration exeConfig = alert.executedAlertConfiguration
                        if (description) {
                            activityService.createActivityForEvdas(exeConfig,
                                    ActivityType.findByValue(ActivityTypeValue.ActionChange),
                                    currentUser,
                                    description,
                                    null,
                                    ['For EVDAS Alert': alertName],
                                    productName,
                                    alert?.pt,
                                    act.assignedTo,
                                    null, act.assignedToGroup, act.guestAttendeeEmail)
                        }

                    } else if (appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                        domain = alertService.generateDomainName(isArchived)
                        alert = domain.createCriteria().get {
                            'actions' {
                                'eq'("id", actionToUpdate.id)
                            }
                        }
                        alertName = alert.name
                        productName = alert?.productName
                        ExecutedConfiguration exeConfig = alert.executedAlertConfiguration
                        if (description) {
                            activityService.createActivity(exeConfig, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                                    currentUser, description, null,
                                    ['For Case Number': alert.caseNumber], productName, alert.getAttr('masterPrefTermAll_7') ?: alert.pt, act.assignedTo, alert.caseNumber, act.assignedToGroup, act.guestAttendeeEmail)
                        }
                    } else if (appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                        //todo getdomainname from service
                        domain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
                        alert = domain.createCriteria().get {
                            'actions' {
                                'eq'("id", actionToUpdate.id)
                            }
                        }
                        alertName = alert.name
                        productName = alert?.productName
                        ExecutedConfiguration exeConfig = alert.executedAlertConfiguration
                        if (description) {
                            activityService.createActivity(exeConfig, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                                    currentUser, description, null,
                                    ['For Aggregate Alert'], productName, alert.pt, act.assignedTo, null, act.assignedToGroup, act.guestAttendeeEmail)
                        }
                    } else if (appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
                        ValidatedSignal signal = ValidatedSignal.createCriteria().get {
                            'actions' {
                                'eq'("id", actionToUpdate.id)
                            }
                        }
                        alertName = signal.name
                        productName = getNameFieldFromJson(signal.products)
                    } else if (appType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                        domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
                        alert = domain.createCriteria().get {
                            'actions' {
                                'eq'("id", actionToUpdate.id)
                            }
                        }
                        alertName = alert.name
                        productName = alert.productSelection
                        if (description) {
                            literatureActivityService.createLiteratureActivity(alert.exLitSearchConfig, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                                    userService.getUser(), description, null, productName, alert.eventSelection, act.assignedTo, alert.searchString, alert.articleId, act.assignedToGroup)
                        }

                    }
                }
                String alertLink = createHref("action", "list", null)
                if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_ASSIGNMENT_UPDATE)) {
                    List sentEmailList = []
                    //Send email to assigned User
                    String newMessage = message(code: 'app.email.case.assignment.update.message.newUser')
                    String oldMessage = message(code: 'app.email.case.assignment.update.message.oldUser')
                    List emailDataList = userService.generateEmailDataForAssignedToChange(newMessage, recipientsList, oldMessage, oldUserList)
                    if (actionService.assignToUpdated(actionGroupAndAssignedToMap, actionToUpdate)) {
                        emailDataList.each { Map emailMap ->
                            if (!sentEmailList.count { it == emailMap.user.email }) {
                                emailNotificationService.mailHandlerForActionAssignmentUpdate(emailMap.user, emailMap.emailMessage, actionToUpdate, alertName, productName, timezone, alertLink)
                                sentEmailList << emailMap.user.email
                            }
                        }
                        }
                    }
                    if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_CREATION_UPDATION)) {
                        (recipientsList.email + actionToUpdate.guestAttendeeEmail).flatten().unique()?.each { String email ->
                            emailNotificationService.mailHandlerForActionUpdate(actionToUpdate, email, alertName, productName, timezone, alertLink)
                        }
                    }
                }
            } else {
                boolean isValidDate = true
                def errors = actionToUpdate.errors.allErrors.collect {
                    messageSource.getMessage(it, Locale.default)
                }
                //Added check validation for year in udpate action not mentioned in bug 55892 but it should be part of update action too.
                if (null != action.dueDate && !DateUtil.checkValidDateYear(action.dueDate)) {
                    isValidDate = false
                }
                if (null != action.completedDate && !DateUtil.checkValidDateYear(action.completedDate)) {
                    isValidDate = false
                }
                if (!isValidDate) {
                    errors.add(INVALID_DATE_ZERO)
                }
                ResponseDTO responseDTO = new ResponseDTO(status: false)
                responseDTO.message = errors
                responseDTO.code = 500
                responseDTO.status = false
                responseDTO.data = errors
                render(responseDTO as JSON)
            }
            render status: 200
        }


        def updateMeetingAction(Long actionId, Long meetingId, Long alertId, String actionStatus, String details, String comments, Long config,
                                Long type, String dueDate, String assignedToValue, String completedDate) {
            List formats = grailsApplication.config.grails.databinding.dateFormats
            String timezone = grailsApplication.config.server.timezone
            String timezoneForDueDate = userService.getUser()?.preference?.timeZone ?: grailsApplication.config.server.timezone
            ValidatedSignal topicOrValidationObj = ValidatedSignal.findById(alertId)
            Action oldAction = Action.get(actionId)
            Meeting meeting = Meeting.get(meetingId)
            Action action = new Action()
            bindData(action, oldAction)
            action.id = actionId
            User currentUser = userService.getUser()
            List<Meeting> meetingList = meetingService.findMeetingsByActionId(actionId)
            List<User> recipientsList = userService.getUserListFromAssignToGroup(oldAction)
            meetingList*.removeFromActions(oldAction)
            oldAction.meetingId = meetingId
            oldAction.setActionStatus(actionStatus)
            oldAction.setDueDate(DateUtil.parseDate(dueDate, DEFAULT_DATE_FORMAT))
            oldAction.setCompletedDate(DateUtil.parseDate(completedDate, DEFAULT_DATE_FORMAT))
            oldAction.details = details
            oldAction.comments = comments
            oldAction.config = ActionConfiguration.get(config)
            oldAction.type = (type == null) ? oldAction.type : ActionType.get(type)
            oldAction = userService.assignGroupOrAssignTo(assignedToValue, oldAction)
            if (!oldAction.assignedTo && !oldAction.assignedToGroup)
                oldAction.guestAttendeeEmail = assignedToValue

            String description = prepareActivityDescription(action, oldAction, timezoneForDueDate)

            try {
                meeting.addToActions(oldAction)
                CRUDService.update(meeting)
                if (description) {
                    if (params.appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
                        activityService.createUpdateActivityForSignal(alertId, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                                currentUser, description, null, oldAction?.assignedToGroup, oldAction?.assignedTo)
                    } else {
                        activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                                currentUser, description, appType)
                    }
                }
                if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_CREATION_UPDATION)) {
                    String alertLink = createHref("validatedSignal", "details", null)
                    String productName = getNameFieldFromJson(topicOrValidationObj.products)
                    (recipientsList.email + oldAction.guestAttendeeEmail).flatten().unique()?.each { String email ->
                        emailNotificationService.mailHandlerForActionUpdate(oldAction, email, topicOrValidationObj.name, productName, timezone, alertLink)
                    }
                }
                render status: 200
            } catch (Throwable ex) {
                ex.printStackTrace()
                List errors = []
                if (meeting == null) {
                    errors.add(message(code: "meetingTitle.nullable"))
                }
                if (meeting) {
                    errors.add("Please fill all the required fields")
                }
                ResponseDTO responseDTO = new ResponseDTO(code: 500, status: false, data: errors, message: errors)
                render(responseDTO as JSON)
            }

        }

        @Secured(['ROLE_AD_HOC_CRUD', 'ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
                'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
        def viewAction(Action actionInstance) {
            [actionInstance: actionInstance]
        }

        def exportActionsReport(Integer alertId, String appType) {
            def alert = AdHocAlert.findById(alertId)
            List actionList = actionService.listActionsForAlert(alertId, appType)

            String reportName = "$appType Actions Report"
            params.eventName = getNameFieldFromJson(alert.eventSelection)
            params.alertName = alert.name
            params.productName = getNameFieldFromJson(alert.productSelection)
            params.topicName = alert.topic
            def reportFile = dynamicReportService.createActionsReport(new JRMapCollectionDataSource(actionList), params, reportName, false)
            renderReportOutputType(reportFile)
            signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, alert.getInstanceIdentifierForAuditLog(), Constants.AuditLog.ADHOC_REVIEW + ": " + Constants.AuditLog.ACTION, params, reportFile.name)
        }

        void renderReportOutputType(File reportFile) {
            response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
            response.contentLength = reportFile.size()
            response.setCharacterEncoding("UTF-8")
            response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportFile.name), "UTF-8")}" + "\"")
            response.getOutputStream().write(reportFile.bytes)
            response.outputStream.flush()
        }
    }
