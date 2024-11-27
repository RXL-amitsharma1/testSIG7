package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.ExceptionHandlingController
import com.rxlogix.config.Action
import com.rxlogix.config.ActionConfiguration
import com.rxlogix.config.ActionType
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.ArchivedLiteratureAlert
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.Meeting
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.ActionStatus
import com.rxlogix.helper.ActivityHelper
import com.rxlogix.helper.LinkHelper
import com.rxlogix.security.Authorize
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.http.util.TextUtils
import org.springframework.http.HttpStatus

import static com.rxlogix.util.DateUtil.DEFAULT_DATE_FORMAT
import static org.springframework.http.HttpStatus.BAD_REQUEST

@Authorize
class ActionRestController implements AlertUtil, ExceptionHandlingController, LinkHelper, ActivityHelper {

    def actionService
    def alertService
    def userService
    def CRUDService
    def activityService
    def emailNotificationService
    def messageSource
    def aggregateCaseAlertService
    public static String INVALID_DATE_ZERO =  "Invalid date! Year should not be 0000"

    String listAlertActions(Long alertId, Boolean isArchived ) {
        Map responseMap = [:]
        if (alertId) {
            List actions = actionService.listActionsForAggAlerts(alertId, isArchived)
            RestApiResponse.successResponseWithData(responseMap,null,actions)

        } else {
            RestApiResponse.invalidParametersResponse(responseMap)
            render(status: HttpStatus.BAD_REQUEST.value(), text: responseMap as JSON)
            return
        }
        render responseMap as JSON
    }


    String fetchActionDropdowns(){
        Map responseMap = [:]
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        Map dataMap  = [
                actionTypeList           : actionTypeAndActionMap.actionTypeList,
                actionConfigList         : actionTypeAndActionMap.actionPropertiesMap?.configs,
                actionStatusList         : ActionStatus.values().collect{it.id}
        ]
        RestApiResponse.successResponseWithData(responseMap,null,dataMap)
        render responseMap as JSON
    }

    String getById(Long actionId) {
        Map responseMap = [:]
        Action act = Action.findById(actionId)
        Map dataMap = actionService.createAggregateActionDTO(act)
        RestApiResponse.successResponseWithData(responseMap,null,dataMap)
        render responseMap as JSON
    }

    def save() {
        Map params = request.JSON
        Boolean isArchived = params.isArchived ?: false
        String timezone = grailsApplication.config.server.timezone
        List errors = []
        def backUrl = params.backUrl

        Action action = new Action(params)
        if (!TextUtils.isEmpty(params['dueDate'])) {
            action.dueDate = DateUtil.parseDate(params['dueDate'], DEFAULT_DATE_FORMAT)
        }
        if (!TextUtils.isEmpty(params['completionDate'])) {
            action.completedDate = DateUtil.parseDate(params['completionDate'], DEFAULT_DATE_FORMAT)
        } else {
            action.completedDate = null
        }
        ActionStatus actionStatus = ActionStatus.findByValue(params.actionStatus)
        action.actionStatus = actionStatus.name()
        Meeting meeting

        try {
            if (params.config && ActionConfiguration.findById(params.config).displayName == 'Meeting') {
                meeting = setMeetingProperties(params, action)
                action.meetingId = meeting?.id
            } else {
                action.meetingId = null
            }
            User currentUser = userService.getUser()
            Action actionInstance = actionService.populate(action, currentUser, params.alertId, params.assignedToValue, params.appType, isArchived)
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
                if (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    alert = isArchived ? ArchivedAggregateCaseAlert.findById(params.alertId) : AggregateCaseAlert.findById(params.alertId)
                    alert.actionCount = alert?.actionCount ? (alert.actionCount + 1) : 1
                    Long executedConfigId = alert.executedAlertConfigurationId
                    ExecutedConfiguration executedConfig = aggregateCaseAlertService.getExecConfigurationById(executedConfigId)
                    productName = alert.productName
                    alertName = alert.name
                    activityService.createActivity(executedConfig, ActivityType.findByValue(ActivityTypeValue.ActionCreated),
                            currentUser, "Action [$action.id] created with Action Type '${action?.type?.displayName}', Action '${action?.config?.displayName}', Assigned To '${action?.assignedTo?.fullName ?: action?.assignedToGroup?.name}', Due Date '${action?.dueDate ? DateUtil.toDateString(action?.dueDate) :  "-"}', Status '${action?.actionStatus}', Completion Date '${action?.completedDate ?  DateUtil.toDateString(action?.completedDate) : "-"}', Action Details '${action?.details}', Comments '${action?.comments ?: ""}'", null,
                            ['For Aggregate Alert'], productName, alert.pt, action?.assignedTo, null, action?.assignedToGroup, actionInstance.guestAttendeeEmail)
                }

                //This will send mail to all the users present in a group
                if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_CREATION_UPDATION)) {
                    String alertLink = createHref("action", "list", null)
                    (recipientsList.email + userService.getUser().email + action.guestAttendeeEmail).flatten().unique()?.each { String email ->
                        emailNotificationService.mailHandlerForActionCreation(actionInstance, email, null, productName, alertName, timezone, alertLink,params.appType)
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
        } catch (ValidationException vx) {
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
            if (params.config && ActionConfiguration.findById(params.config)?.displayName == 'Meeting' && meeting == null) {
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

    def update() {
        Map params = request.JSON
        boolean isArchived = params.isArchived ?: false
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
        String timezone = grailsApplication.config.server.timezone

        if (params.dueDate)
            action.setDueDate((new Date(params.dueDate)).clearTime())

        ActionStatus actionStatus = ActionStatus.findByValue(params.actionStatus)
        action.actionStatus = actionStatus.name()
        String description = prepareActivityDescription(actionToUpdate, action, "UTC")

        actionToUpdate.details = action.details
        actionToUpdate.comments = action.comments
        actionToUpdate.config = action.config
        actionToUpdate.type = action.type
        actionToUpdate.actionStatus = action.actionStatus
        actionToUpdate.dueDate = action.dueDate ? action.dueDate : null
        actionToUpdate = userService.assignGroupOrAssignTo(params.assignedToValue, actionToUpdate)
        if (actionToUpdate.execConfigId == null) {
            actionToUpdate.execConfigId = params.exeConfigId ?: (params?.alertId?.isEmpty() ? null : params?.alertId as Long)
        }
        if (!actionToUpdate.assignedTo && !actionToUpdate.assignedToGroup)
            actionToUpdate.guestAttendeeEmail = params.assignedToValue
        if (params.completionDate) {
            actionToUpdate.setCompletedDate((new Date(params.completionDate)).clearTime())
        } else {
            actionToUpdate.setCompletedDate(null)
        }

        User currentUser = userService.getUser()
        Action act = actionService.updateAction(actionToUpdate, description, params.alertId as String, currentUser, params.appType)
        List<User> recipientsList = userService.getUserListFromAssignToGroup(act)
        def domain

        if (!act.hasErrors()) {
            if (act.config?.isEmailEnabled) {
                String alertName = Constants.Commons.BLANK_STRING
                String productName = Constants.Commons.BLANK_STRING
                def alert
                if (params.alertId) {
                    if (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
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
        render(status: 200, contentType: 'application/json', text: (action as JSON))
    }
}
