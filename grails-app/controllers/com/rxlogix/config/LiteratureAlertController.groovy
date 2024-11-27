package com.rxlogix.config

import com.rxlogix.*
import com.rxlogix.attachments.Attachment
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.signal.AlertTag
import com.rxlogix.signal.AttachmentDescription
import com.rxlogix.signal.Justification
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonOutput
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang3.time.DateUtils
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.HttpSession
import java.text.ParseException

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class LiteratureAlertController implements Alertbililty, AlertController {

    def configurationService
    def signalAuditLogService
    def cacheService
    def productGroupService
    def CRUDService
    def userService
    def reportExecutorService
    def literatureAlertService
    def priorityService
    def workflowRuleService
    def literatureActivityService
    def validatedSignalService
    def dynamicReportService
    def attachmentableService
    def viewInstanceService
    def dispositionService
    def actionService
    def alertService
    def groupService
    def alertCommentService
    LiteratureExecutionService literatureExecutionService
    def alertTagService
    EmailNotificationService emailNotificationService
    LiteratureHistoryService literatureHistoryService
    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"

    def index() {
        redirect(action: 'create')
    }

    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION'])
    def create() {
        render(view: "create", model: modelData(new LiteratureConfiguration(), Constants.AlertActions.CREATE))
    }

    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION', 'ROLE_LITERATURE_CASE_REVIEWER', 'ROLE_LITERATURE_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def review() {
        def litReviewHelpMap = Holders.config.literature.review.helpMap
        render (view: "review",model:[litReviewHelpMap:litReviewHelpMap])
    }

    def copy(LiteratureConfiguration originalConfig) {
        if (!originalConfig) {
            notFound()
            return
        }
        Map model = modelData(originalConfig, Constants.AlertActions.COPY)
        model << [clone:true, currentUser: userService.getUser()]
        render(view: "create", model: model)
    }

    def save() {
        LiteratureConfiguration configurationInstance = new LiteratureConfiguration()
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        bindData(configurationInstance, params, [exclude: ['productGroupSelection','eventGroupSelection','dateRangeStartAbsolute', 'dateRangeEndAbsolute', 'owner']])
        if(!configurationInstance.priority){
            configurationInstance.priority = Priority.findByDefaultPriority(true)?:null
        }
        if(params.productGroupSelection!="[]"){
            configurationInstance.productGroupSelection=params.productGroupSelection
        }
        if(params.eventGroupSelection!="[]"){
            configurationInstance.eventGroupSelection=params.eventGroupSelection
        }

        try {
            configurationInstance = userService.assignGroupOrAssignTo(params[Constants.ASSIGN_TO_PARAM], configurationInstance)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith)
            if (Boolean.parseBoolean(params.repeatExecution)) {
                configurationInstance.setIsEnabled(true)
                literatureAlertService.setNextRunDateAndScheduleDateJSON(configurationInstance)
            } else {
                configurationInstance.setIsEnabled(false)
                configurationInstance.nextRunDate = null
            }
            try {
                if (configurationInstance.scheduleDateJSON) {
                    JSONObject timeObject = JSON.parse(configurationInstance.scheduleDateJSON)
                    Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime)
                    if (startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                        flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                        configurationInstance.nextRunDate = null
                        renderOnErrorScenario(configurationInstance)
                        return
                    }
                }
            }catch(Exception e){
                configurationInstance.scheduleDateJSON = null
            }
            if (!literatureAlertService.checkSearchCriteria(params) && !configurationInstance.validate(["name", "assignedTo", "priority","scheduleDateJSON"])) {
                configurationInstance.errors.reject('default.invalid.search.criteria')
                renderOnErrorScenario(configurationInstance)
                return
            }
            if (!literatureAlertService.checkSearchCriteria(params)) {
                flash.error = message(code: "default.invalid.search.criteria")
                renderOnErrorScenario(configurationInstance)
                return
            }
            configurationInstance.isResume = false
            configurationInstance.owner = configurationInstance?.owner ?: userService.getUser()
            literatureAlertService.setStartAndEndDate(configurationInstance, params)
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup
            configurationInstance = userService.setOwnershipAndModifier(configurationInstance) as LiteratureConfiguration
            literatureAlertService.setDateRange(configurationInstance, params)
            configurationInstance.save(flush:true,failOnError:true)

        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            renderOnErrorScenario(configurationInstance)
            return
        }catch (Exception exception) {
            exception.printStackTrace()
            flash.error = message(code: "app.label.alert.error")
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.literature.alert.configuration'), configurationInstance.name])
        redirect(controller: "literatureAlert", action: "view", id: configurationInstance.id)
    }

    def delete() {
        literatureAlertService.deleteLiteratureConfig(LiteratureConfiguration.get(params?.id))
    }

    def update() {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        LiteratureConfiguration configurationInstance = LiteratureConfiguration.lock(params?.id)
        bindData(configurationInstance, params, [exclude: ['productGroupSelection','eventGroupSelection','dateRangeStartAbsolute', 'dateRangeEndAbsolute', 'owner']])
        if(!configurationInstance.priority){
            configurationInstance.priority = Priority.findByDefaultPriority(true)?:null
        }
        if(params.productGroupSelection!="[]"){
            configurationInstance.productGroupSelection=params.productGroupSelection
        } else {
            configurationInstance.productGroupSelection=null
        }
        if(params.eventGroupSelection!="[]"){
            configurationInstance.eventGroupSelection=params.eventGroupSelection
        } else {
            configurationInstance.eventGroupSelection=null
        }
        try {
            configurationInstance = userService.assignGroupOrAssignTo(params[Constants.ASSIGN_TO_PARAM], configurationInstance)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, true)
            configurationInstance.isResume = false
            try {
                if (configurationInstance.scheduleDateJSON) {
                    JSONObject timeObject = JSON.parse(configurationInstance.scheduleDateJSON)
                    Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime)
                    if (startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                        flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                        configurationInstance.nextRunDate = null
                        renderOnErrorScenario(configurationInstance)
                        return
                    }
                }
            }catch(Exception e){
                configurationInstance.scheduleDateJSON = null
            }
            if (!literatureAlertService.checkSearchCriteria(params)) {
                flash.error = message(code: "default.invalid.search.criteria")
                renderOnErrorScenario(configurationInstance)
                return
            }
            configurationInstance.owner = configurationInstance?.owner ?: userService.getUser()
            literatureAlertService.setStartAndEndDate(configurationInstance, params)
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup
            configurationInstance = userService.setOwnershipAndModifier(configurationInstance) as LiteratureConfiguration
            literatureAlertService.setDateRange(configurationInstance, params)
            configurationInstance.save(flush:true,failOnError:true)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            renderOnErrorScenario(configurationInstance)
            return
        }catch (Exception exception) {
            exception.printStackTrace()
            flash.error = message(code: "app.label.alert.error")
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.literature.alert.configuration'), configurationInstance.name])
        redirect(controller: "literatureAlert", action: "view", id: configurationInstance.id)
    }

    def edit(LiteratureConfiguration configurationInstance) {

        if (!configurationInstance) {
            notFound()
            return
        }
        def firstExecutionDate = '-'
        def lastExecutionDate = '-'
        def nextRunDate = configurationInstance.nextRunDate
        User currentUser = userService.getUser()
        String timeZone = currentUser.preference.timeZone
        try {
            (firstExecutionDate, lastExecutionDate) = literatureAlertService.getFirstAndLastExecutionDate(configurationInstance, timeZone, firstExecutionDate, lastExecutionDate)
        } catch (Throwable th) {
            flash.warn = message(code: "app.alert.view")
            log.error(th.getMessage())
        }
        //May need to change this logic later..
        if (literatureExecutionService.currentlyRunning.contains(configurationInstance.id)) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.name])
            redirect(action: "index")
        }

        if (!(configurationInstance?.isEditableBy(currentUser) ||
                SpringSecurityUtils.ifAllGranted("ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS"))) {
            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.name])
            redirect(controller: "literatureAlert", action: "view", id: configurationInstance.id)
        } else {
            def action = Constants.AlertActions.EDIT
            Map model = modelData(configurationInstance, action)
            model << [configSelectedTimeZone: params?.configSelectedTimeZone, searchString: configurationInstance.searchString,
                      configNextRunDate     : nextRunDate, firstExecutionDate: firstExecutionDate, lastExecutionDate: lastExecutionDate]
            render(view: "edit", model: model)
        }
    }

    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION'])
    def view(LiteratureConfiguration configuration) {

        if (!configuration) {
            notFound()
            return
        }
        def firstExecutionDate = '-'
        def lastExecutionDate = '-'
        def nextRunDate = configuration.nextRunDate
        String timeZone = userService.getUser().preference.timeZone
        boolean isExecutedCase = params?.isExecutedCase
        def isConfigExecuted = false
        try {
            (firstExecutionDate, lastExecutionDate) = literatureAlertService.getFirstAndLastExecutionDate(configuration, timeZone, firstExecutionDate, lastExecutionDate)
        } catch (Throwable th) {
            flash.warn = message(code: "app.alert.view")
            log.error(th.getMessage())
        }
        render(view: "view", model: [configurationInstance: configuration,
                                     isConfigExecuted     : isConfigExecuted,
                                     nextRunDate          : nextRunDate,
                                     firstExecutionDate   : firstExecutionDate,
                                     lastExecutionDate    : lastExecutionDate,
                                     isExecutedCase       : isExecutedCase,
                                     isEdit               : SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || userService.getCurrentUserId() == configuration.owner.id])
    }

    private Map modelData(LiteratureConfiguration configurationInstance, String action) {

        List<User> userList = User.findAllByEnabled(true).sort {
            it.fullName?.toLowerCase()
        }
        List<Priority> priorityList = Priority.findAllByDisplay(true)
        Set<String> sMQList = cacheService.getSMQList()
        Map dateMap = getDateMap(configurationInstance)

        return [configurationInstance: configurationInstance, priorityList: priorityList,
                userList             : userList, action: action, templateList: [],
                productGroupList     : productGroupService.fetchProductGroupsListByDisplay(true),
                sMQList              : sMQList, dateMap: dateMap]
    }

    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION'])
    def run() {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        LiteratureConfiguration configurationInstance
        boolean exiting_config = false
        if (params.id) {
            configurationInstance = LiteratureConfiguration.get(params.id)
            exiting_config = true
        } else {
            configurationInstance = new LiteratureConfiguration()
        }
        bindData(configurationInstance, params, [exclude: ["dateRangeStartDate", "dateRangeEndDate", "owner","productGroupSelection","eventGroupSelection"]])
        if(!configurationInstance.priority){
            configurationInstance.priority = Priority.findByDefaultPriority(true)?:null
        }
        if(params.productGroupSelection != '[]'){
            configurationInstance.productGroupSelection = params.productGroupSelection
        } else {
            configurationInstance.productGroupSelection=null
        }
        if(params.eventGroupSelection!="[]"){
            configurationInstance.eventGroupSelection=params.eventGroupSelection
        } else {
            configurationInstance.eventGroupSelection=null
        }
        try {
            configurationInstance = userService.assignGroupOrAssignTo(params[Constants.ASSIGN_TO_PARAM], configurationInstance)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, exiting_config)
            literatureAlertService.setNextRunDateAndScheduleDateJSON(configurationInstance)
            if (!literatureAlertService.checkSearchCriteria(params) && !configurationInstance.validate(["name", "assignedTo", "priority","scheduleDateJSON"])) {
                configurationInstance.errors.reject('default.invalid.search.criteria')
                renderOnErrorScenario(configurationInstance)
                return
            }
            if (!literatureAlertService.checkSearchCriteria(params)) {
                flash.error = message(code: "default.invalid.search.criteria")
                renderOnErrorScenario(configurationInstance)
                return
            }
            configurationInstance.setIsEnabled(true)
            configurationInstance.repeatExecution = params.repeatExecution
            if(configurationInstance.scheduleDateJSON) {
                JSONObject timeObject = JSON.parse(configurationInstance.scheduleDateJSON)
                Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime)
                if (startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                    flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                    configurationInstance.nextRunDate = null
                    renderOnErrorScenario(configurationInstance)
                    return
                }
            }
            configurationInstance.owner = configurationInstance?.owner ?: userService.getUser()
            literatureAlertService.setStartAndEndDate(configurationInstance, params)
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup
            configurationInstance = userService.setOwnershipAndModifier(configurationInstance) as LiteratureConfiguration
            literatureAlertService.setDateRange(configurationInstance, params)
            configurationInstance.save(flush:true,failOnError:true)
            configurationInstance.isResume = false
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            configurationInstance.nextRunDate = null
            renderOnErrorScenario(configurationInstance)
            return
        } catch (Exception exception) {
            exception.printStackTrace()
            flash.error = message(code: "app.label.alert.error")
        }
        request.withFormat {
            form {
                flash.message = message(code: 'app.Configuration.RunningMessage',
                        args: [message(code: 'configuration.label'), configurationInstance.name])
                redirect(controller: "configuration", action: "executionStatus", params: [alertType: AlertType.LITERATURE_SEARCH_ALERT])
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private void renderOnErrorScenario(LiteratureConfiguration configuration) {
        String action = params.previousAction
        Map model = modelData(configuration, action)
        render(view: "create", model: model)
    }

    def alertTagDetails(Boolean isArchived, String datasource) {
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(datasource)
        def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
        def literatureAlert = domain.findById(params.alertId)
        List alertTagList = literatureAlert.alertTags?.collect { it.name }
        List tagList = AlertTag.list()?.collect { it.name }
        tagList += alertTagService.getMartTagsName()
        render(["tagList": tagList.unique(), alertTagList: alertTagList] as JSON)

    }

    def saveAlertTags(Long alertId, Boolean isArchived, String datasource) {
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(datasource)
        def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        List tagList = []
        User currentUser = userService.getUser()
        def literatureAlert = domain.findById(alertId)
        if (params?.alertTags) {
            List attachedTags = literatureAlert.alertTags?.collect { it.name }
            def updatedTags = params.alertTags == 'null' ? [] : JSON.parse(params.alertTags)
            List addedTags = updatedTags - attachedTags
            List removedTags = attachedTags - updatedTags
            addedTags.each { tagName ->
                AlertTag addAlertTag = AlertTag.findByName(tagName)
                if (!addAlertTag) {
                    addAlertTag = new AlertTag(name: tagName, createdBy: userService.getUser(), dateCreated: new Date())
                    addAlertTag.save(flush: true)
                }
                literatureAlert.addToAlertTags(addAlertTag)
                CRUDService.update(literatureAlert)
                literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig, ActivityType.findByValue(ActivityTypeValue.CategoryAdded), currentUser, "Category Added " + tagName, "", literatureAlert.productSelection, literatureAlert.eventSelection, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)

            }
            removedTags.each { tagName ->
                AlertTag removeAlertTag = AlertTag.findByName(tagName)
                literatureAlert.removeFromAlertTags(removeAlertTag)
                CRUDService.update(literatureAlert)
                literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig, ActivityType.findByValue(ActivityTypeValue.CategoryRemoved), currentUser, "Category Removed " + tagName, null,
                        literatureAlert.productSelection, literatureAlert.eventSelection, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)
            }
            literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,literatureAlert,Constants.HistoryType.ALERT_TAGS,""),isArchived)

        }
        render(responseDTO as JSON)
    }

    def details() {
        User currentUser = userService.getUser()
        groupService.updateMenuVisibilityInSession(request)
        Long id = params.long("configId")
        if(params.callingScreen != Constants.Commons.DASHBOARD && id == -1){
            alertDeletionInProgress()
            return
        }
        Boolean alertDeletionObject = false
        ExecutedLiteratureConfiguration exConfig = ExecutedLiteratureConfiguration.findById(id)
        if(exConfig) {
            alertDeletionObject = alertService.isDeleteInProgress(exConfig.configId as Long, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) ?: false
        }
        Integer literatureConfigurationId = LiteratureConfiguration?.get(exConfig?.configId)?.id
        if(!exConfig){
            notFound()
            return
        }
        if(params.callingScreen != Constants.Commons.DASHBOARD && alertDeletionObject){
            alertDeletionInProgress()
            return
        }

        Boolean isEmbase = false
        if (exConfig.selectedDatasource == 'embase') {
            isEmbase = true
        }

        def litHelpMap = Holders.config.literature.helpMap
        String timezone = userService.getUser()?.preference?.timeZone
        List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone)
        Map dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        Boolean forceJustification = userService.user.workflowGroup.forceJustification
        List<Map> availablePriorities = priorityService.listPriorityOrder()
        List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        ViewInstance viewInstance = viewInstanceService.fetchViewInstanceLiteratureAlert(isEmbase)
        String literatureArticleUrl = grailsApplication.config.app.literature.article.url
        List alertDispositionList = dispositionService.listAlertDispositions()
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        Map dispositionData = workflowRuleService.fetchDispositionData();
        Long meetingId = ActionConfiguration.findByDisplayName("Meeting")?.id
        List<String> reviewCompletedDispostionList = dispositionService.getReviewCompletedDispositionList()
        String dateRange = literatureAlertService.getDateRangeExecutedLiteratureConfig(exConfig)
        Boolean hasLiteratureReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
        String buttonClass = hasLiteratureReviewerAccess?"":"hidden"
        boolean isAbstractView = currentUser.preference.isLiteratureAbstractView
        render(view: "details", model: [
                configId                            : id,
                literatureConfigurationId           : literatureConfigurationId,
                isLatest                            : exConfig?.isLatest,
                callingScreen                       : params.callingScreen,
                isArchived                          : params.boolean('archived') ?: false,
                dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                dispositionData                     : dispositionData as JSON,
                forceJustification                  : forceJustification,
                availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                availablePriorities                 : availablePriorities,
                actionConfigList                    : actionConfigList,
                viewId                              : viewInstance?.id,
                literatureArticleUrl                : literatureArticleUrl,
                actionTypeList                      : actionTypeAndActionMap.actionTypeList,
                actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                meetingId                           : meetingId,
                reviewCompletedDispostionList       : JsonOutput.toJson(reviewCompletedDispostionList),
                alertDispositionList                : alertDispositionList,
                name                                : exConfig?.name,
                dateRange                           : dateRange,
                isPriorityEnabled                   : grailsApplication.config.alert.priority.enable,
                saveCategoryAccess                  : checkAccessForCategorySave(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT),
                hasLiteratureReviewerAccess         : hasLiteratureReviewerAccess,
                hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                buttonClass                         : buttonClass,
                currUserName                        : currentUser.fullName,
                litHelpMap                          : litHelpMap,
                isAbstractView                      : isAbstractView,
                litHelpMap                          : litHelpMap,
                selectedDatasource                  : exConfig?.selectedDatasource
        ])
    }
    void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
    }


    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION', 'ROLE_LITERATURE_CASE_REVIEWER', 'ROLE_LITERATURE_CASE_VIEWER','ROLE_VIEW_ALL'])
    def archivedAlert(Long id) {
        Map executedConfigurationList = literatureArchivedAlertList(id, params)
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        String sortedColumn = orderColumnMap?.name
        if (orderColumn != null && sortedColumn != "version") {
            executedConfigurationList.aaData?.sort { Map config1, Map config2 ->
                def val1 = config1[sortedColumn]
                def val2 = config2[sortedColumn]
                if (sortedColumn in ["alertName","dateRange","lastModified","selectedDatasource"]) {
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return val2?.toLowerCase() <=> val1?.toLowerCase()
                    } else {
                        return val1?.toLowerCase() <=> val2?.toLowerCase()
                    }
                } else {
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return val2 <=> val1
                    } else {
                        return val1 <=> val2
                    }
                }

            }
        }
        render(executedConfigurationList as JSON)
    }

    def literatureSearchAlertList(DataTableSearchRequest searchRequest) {

        def finalMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: []]
        try {
            Map resultMap = literatureAlertService.fetchLiteratureSearchAlertResultMap(params, searchRequest, params.long("configId"), params.boolean('isArchived'), params.get("datasource"))
            finalMap = [recordsTotal: resultMap.resultCount, recordsFiltered: resultMap.filteredCount, aaData: resultMap.resultList, filters: resultMap.filters]
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(finalMap as JSON)
    }

    def changeAssignedToGroup(String selectedId, String assignedToValue, Boolean isArchived){
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: 'app.assignedTo.changed.success'))
        try {
            List<Long> selectedRowsList = JSON.parse(selectedId).collect { it as Long }
            boolean bulkUpdate = selectedRowsList.size() > 1
            List literatureAlertList = domain.getAll(selectedRowsList)
            literatureAlertService.changeAssignedToGroup(literatureAlertList, assignedToValue, isArchived, bulkUpdate)
            //The function is created in the service because async call using notify wasn't available to us in the controller
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception ex) {
            responseDTO.status = false
            responseDTO.message = message(code: 'app.assignedTo.changed.fail')
            log.error(ex.printStackTrace())
        }
        render(responseDTO as JSON)
    }

    def changeDisposition(String selectedRows, Disposition targetDisposition, String validatedSignalName, String justification, Boolean isArchived, Long signalId) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        try {
            validatedSignalName = validatedSignalName ? org.apache.commons.lang.StringEscapeUtils.unescapeHtml(validatedSignalName) : ''
            Map attachedSignalData = literatureAlertService.changeDisposition(selectedRows, targetDisposition, validatedSignalName, justification, isArchived, signalId, isEmbase)
            responseDTO.data=[:]
            def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
            List<Long> literatureCaseAlertIdList = JSON.parse(selectedRows).collect { Map<String, Long> selectedRow ->
                selectedRow["alert.id"]!=""?selectedRow["alert.id"] as Long:null
            }
            Long configId = domain?.get(literatureCaseAlertIdList[0])?.exLitSearchConfig?.id
            Long countOfPreviousDisposition
            if(params.callingScreen != Constants.Commons.DASHBOARD){
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, params.incomingDisposition, domain)
            }else{
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(params.incomingDisposition, domain)
            }
            responseDTO.data << [incomingDisposition:params.incomingDisposition, countOfPreviousDisposition:countOfPreviousDisposition]
            if (attachedSignalData) {
                responseDTO.data << attachedSignalData
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessageList(vx)[0]
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION', 'ROLE_LITERATURE_CASE_REVIEWER', 'ROLE_LITERATURE_CASE_VIEWER'])
    def revertDisposition(Long id, String justification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        def domain = literatureAlertService.getDomainObject(false, isEmbase)
        try {
            responseDTO.data = literatureAlertService.revertDisposition(id, justification, domain)
            Long configId = domain.get(id)?.exLitSearchConfig?.id
            Long countOfPreviousDisposition
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, responseDTO?.data?.incomingDisposition, LiteratureAlert)
            } else {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(responseDTO?.data?.incomingDisposition, LiteratureAlert)
            }
            responseDTO.data << [countOfPreviousDisposition: countOfPreviousDisposition]
            if(!responseDTO.data.dispositionReverted){
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.undo.disposition.change.error.refresh")
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.undo.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    //TODO move cde to service
    def changePriorityOfAlert(String selectedRows, Boolean isArchived) {
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            JSON.parse(selectedRows).each { Map<String, Long> selectedRow ->

                def alert = domain.get(selectedRow["alert.id"])
                Priority newPriority = Priority.get(params.newPriority.id)
                String justification = params.justification
                if (alert) {
                    if (newPriority && (alert.priority != newPriority)) {
                        Priority currentPriority = alert.priority
                        Map map = [change  : Constants.HistoryType.PRIORITY,
                                   priority: newPriority]
                        literatureAlertService.updateLiteratureSearchAlertStates(alert, map, alert.litSearchConfig, isArchived, isEmbase)
                        //Create an activity
                        String productName = getNameFieldFromJson(alert.productSelection)
                        String eventName = getNameFieldFromJson(alert.eventSelection)

                        literatureActivityService.createLiteratureActivity(alert.exLitSearchConfig,literatureAlertService.getActivityByType(ActivityTypeValue.PriorityChange),
                                userService.getUser(), "Priority changed from '$currentPriority.displayName' to '$newPriority.displayName'", justification, productName, eventName, alert.assignedTo, alert.searchString, alert.articleId, alert.assignedToGroup)
                        literatureHistoryService.saveLiteratureArticleHistory(literatureHistoryService.getLiteratureHistoryMap(null,alert,Constants.HistoryType.PRIORITY,justification),isArchived)

                    }
                }
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.priority.change.error")
        }
        render(responseDTO as JSON)
    }

    def listLiteratureAlertActivities() {
        List<Map> result = literatureAlertService.getLiteratureActivityList(params.getLong("configId"))
        respond result.flatten(), [formats: ['json']]
    }

    def exportReport() {
        ExecutedLiteratureConfiguration executedLiteratureConfiguration = ExecutedLiteratureConfiguration.findByIdAndIsEnabled(params.configId, true)
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(executedLiteratureConfiguration.selectedDatasource)
        List<Map> reportList = literatureAlertService?.getExportedList(params, isEmbase)
        params.reportList = reportList
        params.isEmbase = isEmbase
        params.totalCount = reportList?.size()?.toString()
        List criteriaList = literatureAlertService?.getCriteriaList(params, executedLiteratureConfiguration, true)
        params.criteriaSheetList = criteriaList
        def reportFile = dynamicReportService.createLiteratureAlertReport(new JRMapCollectionDataSource(reportList), params)
        cacheService.removeDefaultDisp(Constants.AlertType.LITERATURE)
        renderReportOutputType(reportFile, "Literature Activity")
        signalAuditLogService.createAuditForExport(criteriaList, executedLiteratureConfiguration.getInstanceIdentifierForAuditLog(), Constants.AuditLog.LITERATURE_REVIEW + (executedLiteratureConfiguration.isLatest ? "" : ": Archived Alert"), params, reportFile.name)
    }

    void renderReportOutputType(File reportFile, String name) {
        String reportName = name + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    def exportLiteratureActivityReport() {
        ExecutedLiteratureConfiguration executedLiteratureConfiguration = ExecutedLiteratureConfiguration.findByIdAndIsEnabled(params.configId, true)
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(executedLiteratureConfiguration.selectedDatasource)
        List<Map> reportList = literatureAlertService?.getExportedList(params, isEmbase)
        params.totalCount = reportList?.size()?.toString()
        List criteriaList = literatureAlertService?.getCriteriaList(params, executedLiteratureConfiguration, false)
        params.criteriaSheetList = criteriaList
        File reportFile = literatureAlertService.getLiteratureActivityExportedFile(params)
        renderReportOutputType(reportFile, "Literature Activity")
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, ExecutedLiteratureConfiguration.get(params.getLong("configId")).getInstanceIdentifierForAuditLog() + ": Activities", Constants.AuditLog.LITERATURE_REVIEW, params, reportFile.name)
    }

    def upload() {
        def isArchived = params.boolean('isArchived')
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
        def literatureAlert = domain.findById(params.alertId)
        def fileName = params?.attachments.filename
        if (fileName instanceof String) {
            fileName = [fileName]
        }
        params?.isAlertDomain=true
        User currentUser = userService.getUser()
        Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileName, literatureAlert, request)
        String fileDescription = params.description
        List<Attachment> attachments = literatureAlert.getAttachments().sort { it.dateCreated }
        if (attachments) {
            List<Integer> bulkAttachmentIndex = 1..filesStatusMap?.uploadedFiles?.size()
            bulkAttachmentIndex.each {
                Attachment attachment = attachments[-it]
                AttachmentDescription attachmentDescription = new AttachmentDescription()
                attachmentDescription.attachment = attachment
                attachmentDescription.createdBy = currentUser.fullName
                attachmentDescription.description = fileDescription
                attachmentDescription.skipAudit=true
                attachmentDescription.save(flush: true)
            }
        }
        if (filesStatusMap?.uploadedFiles) {
            literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig, literatureAlertService.getActivityByType(ActivityTypeValue.AttachmentAdded),
                    userService.getUser(), "Attachment " + filesStatusMap?.uploadedFiles*.originalFilename + " is added", null,
                    literatureAlert.productSelection, literatureAlert.eventSelection, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)
        }
        render(['success': true] as JSON)
    }


    def fetchAttachment(final Long alertId, Boolean isArchived) {
        def attachments = []
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))

        List<Long> litAlertList = literatureAlertService.getAlertIdsForAttachments(alertId, isArchived, isEmbase)
        String timezone = userService.user.preference.timeZone
        litAlertList.each { Long litAlertId ->
            def litAlert
            if (isEmbase) {
                litAlert = ArchivedEmbaseLiteratureAlert.get(litAlertId) ?: EmbaseLiteratureAlert.get(litAlertId)
            } else {
                litAlert = ArchivedLiteratureAlert.get(litAlertId) ?: LiteratureAlert.get(litAlertId)
            }
            attachments += litAlert.attachments.collect {
                [
                        id         : it.id,
                        name       : it.name,
                        description: AttachmentDescription.findByAttachment(it)?.description,
                        timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone),
                        modifiedBy : AttachmentDescription.findByAttachment(it)?.createdBy
                ]
            }
        }

        respond attachments.unique(), [formats: ['json']]
    }

    def deleteAttachment(Long alertId, Long attachmentId, Boolean isArchived) {
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
        def literatureAlert = domain.findById(alertId)
        Attachment attachment = Attachment.findById(attachmentId)

        if (attachment) {
            if (AttachmentDescription.findByAttachment(attachment)) {
                AttachmentDescription.findByAttachment(attachment).delete()
            }
            Boolean result = attachmentableService.removeAttachment(attachmentId)
            String fileName = attachment.filename
            if (fileName) {
                String attachmentRemovedMessage = message(code: "app.label.literature.attachment.removed", args: [fileName])
                literatureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig, literatureAlertService.getActivityByType(ActivityTypeValue.AttachmentRemoved),
                        userService.getUser(), attachmentRemovedMessage, null,
                        literatureAlert.productSelection, literatureAlert.eventSelection, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup)
            }
        }
        render(['success': true] as JSON)
    }

    Map getDateMap(LiteratureConfiguration configurationInstance) {
        Map dateMap = [:]
        dateMap.startDate = configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute ? DateUtil.StringFromDate(configurationInstance.dateRangeInformation.dateRangeStartAbsolute, "MM-dd-yyyy", "UTC") : Constants.Commons.BLANK_STRING
        dateMap.endDate = configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute ? DateUtil.StringFromDate(configurationInstance.dateRangeInformation.dateRangeEndAbsolute, "MM-dd-yyyy", "UTC") : Constants.Commons.BLANK_STRING
        return dateMap
    }

    def changeAlertLevelDisposition(Disposition targetDisposition,String justificationText, Boolean isArchived) {
        def isEmbase = Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.get("datasource"))
        def domain = literatureAlertService.getDomainObject(isArchived, isEmbase)
        String alertName = domain.simpleName
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, message: message(code: "alert.level.disposition.successfully.updated"))
        try {
            Integer updatedRowsCount = literatureAlertService.changeAlertLevelDisposition(targetDisposition, justificationText, domain, params.getLong("exConfigId"), isArchived, isEmbase)
            if (updatedRowsCount <= 0) {
                responseDTO.status = true
                responseDTO.message = message(code: "alert.level.review.completed")
            } else {
                dispositionService.sendDispChangeNotification(targetDisposition, alertName)
            }
            List removedDispositionNames = []
            removedDispositionNames = Disposition.findAllByReviewCompleted(false).collect {
                it.displayName
            }
            responseDTO.data = [removedDispositionNames:removedDispositionNames]
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    def updateAutoRouteDisposition(Long literatureId,Boolean isArchived) {
        ResponseDTO responseDTO = new ResponseDTO(status: false)
        literatureAlertService.updateAutoRouteDisposition(literatureId, responseDTO, isArchived)
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION', 'ROLE_EXECUTE_SHARED_ALERTS'])
    def runOnce() {
        LiteratureConfiguration configurationInstance = LiteratureConfiguration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        if (configurationInstance.nextRunDate != null && configurationInstance.isEnabled == true) {
            flash.warn = message(code: 'app.configuration.run.exists')
            redirect(controller: 'configuration', action: "index")
            return

        }
        User currentUser = userService.getUser()
        List runningAlerts = configurationService.fetchRunningAlertList(params.type)
        if (runningAlerts?.contains(configurationInstance.id)) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.name])
            redirect(controller: 'configuration', action: "index")
            return
        } else {
            try {
                configurationInstance.setIsEnabled(true)
                try {
                    configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
                    configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
                    configurationInstance = (LiteratureConfiguration) CRUDService.save(configurationInstance)
                }catch(Exception e){
                    configurationInstance.scheduleDateJSON = null
                }
            } catch (ValidationException ve) {
                configurationInstance.errors = ve.errors
                render view: "create", model: [configurationInstance: configurationInstance]
                return
            }catch (Exception exception) {
                flash.error = message(code: "app.label.alert.error")
                renderOnErrorScenario(configurationInstance)
            }
            flash.message = message(code: 'app.Configuration.RunningMessage',
                    args: [message(code: 'configuration.label'), configurationInstance.name])
            redirect(controller: "configuration", action: "executionStatus", params: [alertType: AlertType.LITERATURE_SEARCH_ALERT])
        }
    }


    def updateAndExecuteLitAlert() {

        LiteratureConfiguration configurationInstance = LiteratureConfiguration.get(params.configId)
        if (!configurationInstance) {
            notFound()
            return
        }
        List runningAlerts = configurationService.fetchRunningAlertList(Constants.ConfigurationType.LITERATURE_TYPE)
        if (runningAlerts?.contains(configurationInstance.id)) {
            // found alert running on given configID, return
            flash.error = message(code: 'app.configuration.alert.running', args: [configurationInstance.name])
            redirect(controller: "alertAdministration", action: "index")
            return
        }

        configurationInstance.setIsEnabled(true)
        configurationInstance.setIsResume(false)
        configurationInstance.setNextRunDate(null)

        LiteratureDateRangeInformation dateRangeInformation = configurationInstance.dateRangeInformation
        if (params.alertDateRangeInformation) {
            bindData(dateRangeInformation, params.alertDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
            dateRangeInformation.literatureConfiguration = configurationInstance
            literatureAlertService.updateAlertDateRange(dateRangeInformation, params)
        }
        if (params.runNow != null) {
            // if run now is selected then newxt run date is now
            configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
            configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
            if (params.futureSchedule != null && params.scheduleDateJSON) {
                configurationInstance.futureScheduleDateJSON = params.scheduleDateJSON
            }
        } else if (params.futureSchedule != null && params.scheduleDateJSON) {
            configurationInstance.setScheduleDateJSON(params.scheduleDateJSON)
            configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
        }

        try {
            if(params.futureSchedule != null && params.scheduleDateJSON){
                Map scheduleDateJSONMap = JSON.parse(params.scheduleDateJSON) as Map
                Date startDate = Date.parse(JSON_DATE, scheduleDateJSONMap.startDateTime as String)
                if(startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))){
                    flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                    redirect(controller: "alertAdministration", action: "index")
                    return
                }
            }
            configurationInstance = (LiteratureConfiguration) CRUDService.save(configurationInstance)
        } catch (ValidationException ve) {
            render ve.errors
            log.error(ve.printStackTrace())
            return
        }

        redirect(controller: "configuration", action: "executionStatus", params: [alertType: AlertType.LITERATURE_SEARCH_ALERT])
    }

    def updateAndExecuteLitAlertBulk() {
        List configIdList = params.get("configIdList")?.split(",")
        def list = []
        List<LiteratureConfiguration> currentlyRunningConfigs = []
        def configToRemove = []
        configIdList.each{ configId ->
            List runningAlerts = configurationService.fetchRunningAlertList(Constants.ConfigurationType.LITERATURE_TYPE)
            if (runningAlerts?.contains(configId)) {
                currentlyRunningConfigs.add(LiteratureConfiguration.get(configId as Long))
            }
        }
        configIdList.removeAll(currentlyRunningConfigs)
        configIdList.each {
            params.configId = it
            LiteratureConfiguration configurationInstance = LiteratureConfiguration.get(params.configId)
            if (!configurationInstance) {
                notFound()
                return
            }

            configurationInstance.setIsEnabled(true)
            configurationInstance.setIsResume(false)
            configurationInstance.setNextRunDate(null)

            LiteratureDateRangeInformation dateRangeInformation = configurationInstance.dateRangeInformation
            if (params.alertDateRangeInformation) {
                bindData(dateRangeInformation, params.alertDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
                dateRangeInformation.literatureConfiguration = configurationInstance
                literatureAlertService.updateAlertDateRange(dateRangeInformation, params)
            }
            if (params.runNow != null) {
                // if run now is selected then next run date is now
                configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
                configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
                if (params.futureSchedule != null && params.scheduleDateJSON) {
                    configurationInstance.futureScheduleDateJSON = params.scheduleDateJSON
                }
            } else if (params.futureSchedule != null && params.scheduleDateJSON) {
                configurationInstance.setScheduleDateJSON(params.scheduleDateJSON)
                configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
            }

            try {
                if(params.futureSchedule != null && params.scheduleDateJSON){
                    Map scheduleDateJSONMap = JSON.parse(params.scheduleDateJSON) as Map
                    Date startDate = Date.parse(JSON_DATE, scheduleDateJSONMap.startDateTime as String)
                    if(startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))){
                        flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                        redirect(controller: "alertAdministration", action: "index")
                        return
                    }
                }
                configurationInstance = (LiteratureConfiguration) CRUDService.save(configurationInstance)
            } catch (ValidationException ve) {
                render ve.errors
                log.error(ve.printStackTrace())
                return
            }
        }
        if(currentlyRunningConfigs.size()>0){
            flash.warn = message(code: 'app.configuration.alert.running', args: [currentlyRunningConfigs*.name.join(",")])
            redirect(controller: "alertAdministration", action: "index")
        } else{
            redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.LITERATURE_SEARCH_ALERT])
        }
    }

    private getRunOnceScheduledDateJson() {
        def startupTime = (new Date()).format(ConfigurationService.JSON_DATE)
        def timeZone = DateUtil.getTimezoneForRunOnce(userService.getUser())
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }

    def listByLiteratureConfiguration(DataTableSearchRequest searchRequest) {
        Map resultMap = [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        def parsedParams = parseJsonString(params.args)
        HttpSession session = request.getSession()
        session.setAttribute("literatureFilter", parsedParams.selectedAlertsFilter)
        resultMap = literatureAlertService.generateResultMap(resultMap, searchRequest, parsedParams.selectedAlertsFilter)
        render(resultMap as JSON)
    }

    def getSharedWithUserAndGroups() {
        Map result = literatureAlertService.getShareWithUserAndGroup(params.id)
        render result as JSON
    }

    @Secured(['ROLE_SHARE_GROUP','ROLE_SHARE_ALL'])
    def editShare() {
        Boolean sharedWithoutException = true
        ExecutedLiteratureConfiguration executedConfiguration = ExecutedLiteratureConfiguration.get(Long.parseLong(params.executedConfigId))
        try {
            literatureAlertService.editShareWith(params)
        } catch (Exception e) {
            sharedWithoutException = false
            e.printStackTrace()
        }
        if(sharedWithoutException){
            flash.message = message(code: "app.configuration.share.success", args: [executedConfiguration?.name])
        }else{
            flash.warn = message(code: "app.configuration.share.warning", args: [executedConfiguration?.name])
        }
        redirect action: "review"
    }

    def changeFilterAttributes(){
        HttpSession session = request.getSession()
        session.removeAttribute("selectedAlertsFilter")
        session.setAttribute("literatureFilter",params["selectedAlertsFilter"])
        render([status:200] as JSON)
    }

    def prepareSearchStringFromQuery(Long queryId) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            responseDTO.data = literatureAlertService.prepareSearchStringForQuery(queryId)
        } catch(Exception ex) {
            responseDTO.status = false
            responseDTO.code = 500
            log.error("Error occurred while preparing search string: ", ex)
            throw ex
        }
        render responseDTO as JSON
    }
}
