package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.controllers.SignalController
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.SignalChartsDTO
import com.rxlogix.enums.*
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.util.TextUtils
import com.rxlogix.exception.FileFormatException
import org.grails.datastore.mapping.model.MappingContext
import org.hibernate.Session
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import javax.servlet.http.Cookie
import javax.servlet.http.HttpSession
import javax.xml.bind.ValidationException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

import static com.rxlogix.util.DateUtil.*
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.NOT_FOUND
import org.apache.commons.lang3.time.DateUtils

@Secured(["isAuthenticated()"])
class ValidatedSignalController implements SignalController {

    def userService
    def activityService
    def workflowService
    def specialPEService
    def alertDocumentService
    def dynamicReportService
    def attachmentableService
    def validatedSignalService
    def alertAttributesService
    def productEventHistoryService
    def dataSource_pva
    def actionTemplateService
    def emailService
    def signalHistoryService
    def evdasHistoryService
    def validatedSignalChartService
    PageRenderer groovyPageRenderer
    def productBasedSecurityService
    def dispositionService
    def viewInstanceService
    def priorityService
    def workflowRuleService
    def alertService
    def messageSource
    def CRUDService
    def justificationService
    def aggregateCaseAlertService
    def singleCaseAlertService
    def evdasAlertService
    def literatureAlertService
    def adHocAlertService
    def spotfireService
    def signalWorkflowService
    def cacheService
    def sessionFactory
    def alertFieldService
    def reportExecutorService
    def signalAuditLogService
    def signalDataSourceService
    def dataObjectService
    def dataSource
    ClobUtil clobUtilInstance = new ClobUtil()
    MappingContext mappingContext

    def index(String callingScreen) {
        ViewInstance viewInstance = viewInstanceService.fetchViewInstanceSignal(Constants.AlertConfigType.SIGNAL_MANAGEMENT)
        [callingScreen: callingScreen, viewId: viewInstance?.id, iconSeq: viewInstance.iconSeq]
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def list(DataTableSearchRequest searchRequest) {
        String selectUserValue = ""
        def parsedParams = parseJsonString(params.args)
        HttpSession session = request.getSession()
        if (params?.callingScreen == Constants.Commons.DASHBOARD) {
            session.setAttribute("signalFilterFromDashboard", parsedParams.selectedAlertsFilterForDashboard)
            selectUserValue = parsedParams.selectedAlertsFilterForDashboard
        }else{
            session.setAttribute("signalFilter", parsedParams.selectedAlertsFilter)
            selectUserValue = parsedParams.selectedAlertsFilter
        }
        Map validatedSignals = validatedSignalService.getValidatedSignalList(searchRequest, params, selectUserValue)
        render validatedSignals as JSON
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    /**
     * Returns paginated JSON data based on a searchTerm and a page count
    */
    def fetchSignalNotInAlertObj() {
        String searchTerm = params.searchTerm ?: Constants.Commons.EVEN
        int page = params.int('page') ?: Constants.Commons.ONE
        int pageSize = Constants.Commons.TWENTY
        Map signals = validatedSignalService.fetchSignalsNotInAlertObj(searchTerm, page, pageSize);
        render signals as JSON
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def getDifferenceInDays(endDate, todayDate) {
        Timestamp sqlTodayDate = new java.sql.Timestamp(todayDate.getTime())
        def diff = endDate.getTime() - sqlTodayDate.getTime();
        def differenceInDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        differenceInDays
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION'])
    def create() {
        Map createMap = validatedSignalService.prepareCreateMap()
        createMap << [showDetectedBy: Holders.config.validatedSignal.show.detected.by, showHealthAuthority: Holders.config.validatedSignal.show.health.authority,
                      showTopicInformation: Holders.config.validatedSignal.show.topic.information, showAggregateDate: Holders.config.validatedSignal.aggregateDate.enabled, showShareWith: Holders.config.validatedSignal.shareWith.enabled,isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM),isQuant:true]
        createMap
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION'])
    def edit() {
        Map createMap = validatedSignalService.editMap(params.signalId)
        createMap

    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION'])
    def save() {
        String defaultValidatedDate = Holders.config.signal.defaultValidatedDate;
        Map paramsMap = [name               : params.name?.trim().replaceAll("\\s{2,}", " "), products: params.productSelection, reasonForEvaluation: params.reasonForEvaluation, productGroupSelection: params.productGroupSelection,
                         eventGroupSelection: params.eventGroupSelection, events: params.eventSelection, assignmentType: Constants.AssignmentType.USER, commentSignalStatus: params.commentSignalStatus,
                         detectedBy         : params.detectedBy, topic: params.topic, description: params.description, genericComment: params.genericComment,
                         signalTypeList     : params.signalTypeList, signalOutcome: params.signalOutcome, linkedSignal: params.linkedSignal,
                         actionTaken        : params.actionTaken, evaluationMethod: params.evaluationMethod, initialDataSource: params.initialDataSource,
                         detectedDate       : params.detectedDate, aggReportStartDate: params.aggReportStartDate, aggReportEndDate: params.aggReportEndDate,
                         lastDecisionDate   : params.lastDecisionDate, haDateClosed: params.haDateClosed, priority: params.priority,
                         params             : params.signalStrategy, assignedToValue: params.assignedToValue, actionTemplate: params.actionTemplate, haSignalStatus: params.haSignalStatus, sharedWith: params.sharedWith,
                         udDate1            : params.udDate1, udDate2: params.udDate2, udText1: params.udText1, udText2: params.udText2, udDropdown1: params.udDropdown1, udDropdown2: params.udDropdown2, isMultiIngredient: Boolean.parseBoolean(params.isMultiIngredient)]


        ValidatedSignal validatedSignal = validatedSignalService.saveValidatedSignal(paramsMap)
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        if (validatedSignal?.id) {
            responseDTO.status = true
        } else {
            responseDTO.status = false
            if (StringUtils.isNotBlank(paramsMap.name)) {
                def notAllowedChar = MiscUtil.validator(paramsMap.name, "Signal Name", (Constants.SpecialCharacters.DEFAULT_CHARS - ["#"]) as String[])
                if (Objects.nonNull(notAllowedChar) && notAllowedChar instanceof List) {
                    responseDTO.data = notAllowedChar[1]
                } else if (Objects.nonNull(notAllowedChar) && notAllowedChar instanceof Boolean && paramsMap.name.size() > Constants.Commons.VALIDATED_SIGNAL_NAME_MAX_LIMIT) {
                    responseDTO.data = message(code: "validated.signal.name.maxSize.exceeded")
                }
            }
            if (params.detectedDate && !validatedSignalService.validateDetectedDateLimit(params.detectedDate as String)) {
                responseDTO.data = g.message(code: "validated.signal.detectedDateError.message")
            }
            if (params.detectedDate && new Date(params.detectedDate) > new Date()) { //Added for PVS-72110
                responseDTO.data = messageSource.getMessage("detectedDate.invalid", null, Locale.default)
            }
        }
        render(responseDTO as JSON)
    }


    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION'])
    def update() {
        ResponseDTO responseDTO = validatedSignalService.updateSignal(params)
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION'])
    def verifySignalOutcomeMapping() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        SignalOutcome mappedSignal= params.targetDispositionId && params.targetDispositionId != '' && params.targetDispositionId != 'undefined' ? SignalOutcome.findByDispositionId(Long.parseLong(params.targetDispositionId)) : null
        if(mappedSignal){
            if (params."signalOutComes[]" instanceof String) {
                SignalOutcome signalOutcome = SignalOutcome.findByName(params."signalOutComes[]")
                if (signalOutcome?.dispositionId?.compareTo(Long.parseLong(params.targetDispositionId)) != 0) {
                    responseDTO.status = false
                }
            } else {
                params."signalOutComes[]".each {
                    SignalOutcome signalOutcome = SignalOutcome.findByName(it)
                    if (signalOutcome?.dispositionId?.compareTo(Long.parseLong(params.targetDispositionId)) != 0) {
                        responseDTO.status = false
                    }
                }
            }
        }
        if (!responseDTO.status) {
            responseDTO.data = [messageSource.getMessage("app.label.signal.information.outcome.mapping.error", null, Locale.default)]
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION'])
    def saveSignalOutcome() {
        ResponseDTO responseDTO = new ResponseDTO(status: false)
        if (validatedSignalService.updateSignalOutcome(params)) {
            responseDTO.status = true
        }
        render(responseDTO as JSON)
    }

    def topicMigrated(Long id) {
        flash.isTopicMigrated = true
        redirect(controller: 'validatedSignal', action: 'details', params: [id: id])
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def details() {
        boolean isEditDueDate=false
        boolean isDueDateUpdate=true
        Integer dueIn = null
        Boolean isTopicMigrated = flash.isTopicMigrated as Boolean
        def signalId = params.id
        def validatedSignal = ValidatedSignal.get(signalId)
        def emergingSafetyList = EmergingIssue.list()
        def safetyIssueList = []
        User currentUser = userService.getUser()
        Boolean isSignalAccessible = isAuthorizedResource(validatedSignal,currentUser)  //Added for PVS-57815
        if(!isSignalAccessible){
            notFound()
            return
        }
        Sql sql = new Sql(signalDataSourceService?.getReportConnection("dataSource"))
        Boolean isUndoable = false
        List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.SIGNAL_MANAGEMENT)
        if (emergingSafetyList) {
            safetyIssueList = validatedSignalService.getEmergingSafetyIssueList(emergingSafetyList)
        }
        if (!TextUtils.isEmpty(params.aggReportStartDate)) {
            validatedSignal.aggReportStartDate = DateUtil.stringToDate(params.aggReportStartDate, "MM/dd/yyyy", Holders.config.server.timezone)
        }
        if (!TextUtils.isEmpty(params.aggReportEndDate)) {
            validatedSignal.aggReportStartDate = DateUtil.stringToDate(params.aggReportStartDate, "MM/dd/yyyy", Holders.config.server.timezone)
        }
        if (!TextUtils.isEmpty(params.lastDecisionDate)) {
            validatedSignal.lastDecisionDate = DateUtil.stringToDate(params.lastDecisionDate, "MM/dd/yyyy", Holders.config.server.timezone)
        }
        if (!TextUtils.isEmpty(params.haDateClosed)) {
            validatedSignal.haDateClosed = DateUtil.stringToDate(params.haDateClosed, "MM/dd/yyyy", Holders.config.server.timezone)
        }

        def emergingIssuesList = []
        def specialPEList = []

        def aggAlerts = validatedSignal?.aggregateAlerts
        Map pecAndCaseCounts = validatedSignalService.pecAndCaseCounts(signalId.toString())
        def pecCountArgus = pecAndCaseCounts.get(Constants.SignalCounts.PVACOUNT) ?: 0
        def caseCount = pecAndCaseCounts.get(Constants.SignalCounts.CASECOUNT) ?: 0
        def evdasCount = pecAndCaseCounts.get(Constants.SignalCounts.EVDASCOUNT) ?: 0

        def pecCountFaers = 0
        def spclPEList = SpecialPE.findAll()
        def selectedDatasource='pva'
        aggAlerts?.each {
            if (safetyIssueList.contains(it.pt)) {
                emergingIssuesList.add(it.pt)
            }
            def isSpecialPE = specialPEService.isSpecialPE(it.productName, it.pt, spclPEList)
            if (isSpecialPE) {
                specialPEList.add(it.productName + " - " + it.pt)
            }

            if (it.alertConfiguration.selectedDatasource == Constants.DataSource.VIGIBASE) {
                pecCountFaers = pecCountFaers + 1
            }
            selectedDatasource=it.alertConfiguration.selectedDatasource
        }

        def chartCount = SignalChart.countByValidatedSignal(validatedSignal)
        def productNameList = validatedSignal?.getProductNameList()
        List userList = User.list().sort { it.fullName?.toLowerCase() }.collect {
            [id: it.id, fullName: it.fullName]
        }
        List<String> reviewCompletedDispostionList = dispositionService.getReviewCompletedDispositionList()
        String reviewCompletedDispostionListJSON = reviewCompletedDispostionList as JSON
        reviewCompletedDispostionListJSON = reviewCompletedDispostionListJSON?.replace("'","\\\'")?.replace("\"","\\\"")

        def adhocAlertDetails = validatedSignal?.adhocAlerts.collect {
            [it.name, it.getProductNameList().join(','), it.topic].join('-')
        }

        Map summaryReportPreference = [:]
        if (validatedSignal?.signalSummaryReportPreference) {
            summaryReportPreference = JSON.parse(validatedSignal?.signalSummaryReportPreference)
        } else {

            List reportSections = SignalSummaryReportSectionsEnum.defaultSections*.val.collect {
                "\"" + it + "\""
            }
            summaryReportPreference.ignore = ["\"Appendix\"","\"Attached Documents\""]
            summaryReportPreference.required = reportSections
        }
        Map heatMap = validatedSignalService.heatMapData(validatedSignal)
        def heatMapModel = [socs: heatMap.socs, years: heatMap.years, data: heatMap.data]

        def datasources = getDataSourceMap()

        if (isTopicMigrated) {
            flash.message = "${validatedSignal?.name} has been upgraded to signal successfully."
        }

        def alertDocuments = alertDocumentService.getSignalUnlikedDocuments()
        def productNames = alertDocuments.collect { it.productName }.findAll { it?.trim() }.unique()
        def documentTypes = alertDocuments.collect { it.documentType }.findAll { it?.trim() }.unique()
        String timezone = userService.getUser()?.preference?.timeZone
        List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.signalPriority], false)*.toDto(timezone)
        availableAlertPriorityJustifications.each{
            it.name = it.name.replace("'","\\\'")?.replace("\"","\\\"")
            it.justificationText = it.justificationText.replace("\r\n","<br>").replace("\"","'")
        }

        Boolean hasConfigurationEditorRole = SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")?true:false
        Map<Disposition, ArrayList> dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        String dispositionIncomingOutgoingJSON = dispositionIncomingOutgoingMap as JSON
        dispositionIncomingOutgoingJSON = dispositionIncomingOutgoingJSON?.replace("'","\\\'")?.replace("\"","\\\"")
        List currentDispositionOptions = dispositionIncomingOutgoingMap[validatedSignal?.disposition?.displayName]
        Boolean forceJustification = userService.user?.workflowGroup?.forceJustification
        List<Map> availablePriorities = priorityService.listPriorityOrder()

        String literatureArticleUrl = grailsApplication.config.app.literature.article.url
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        Long meetingId = ActionConfiguration.findByDisplayName("Meeting")?.id
        String currentUserFullName = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        List<Map> signalHistoryList = validatedSignalService.generateSignalHistory(validatedSignal)
        List<String> signalStatusList = alertAttributesService.get('signalHistoryStatus') as List<String>
        if(SystemConfig.first().displayDueIn)
        {
            signalStatusList?.add(Constants.WorkFlowLog.DUE_DATE)
        }
        boolean allowedProductsAsSafetyLead = false
        User userInstance = User.get(userService.getCurrentUserId())
        if (validatedSignal.productGroupSelection) {
            allowedProductsAsSafetyLead = true
        } else {
            allowedProductsAsSafetyLead = userInstance?.safetyGroups.size() > 0 ? true : false
        }

        List signalRole = [ 'ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER','ROLE_ADMIN','ROLE_DEV'
        ]
        Map userRoleMap = buildUserModel( userInstance )?.roleMap
        for( int i = 0; i < signalRole?.size(); i++ ) {
            if(userRoleMap.containsKey( message( code: 'app.role.' + signalRole?.get( i ) ) ) && userRoleMap.get( message( code: 'app.role.' + signalRole?.get( i ) ) ) ) {
                allowedProductsAsSafetyLead = true;
                break;
            }
        }
        List<Map> dispositionJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertWorkflow], false)*.toDto(timezone)
        String productSelected = alertService.productSelectionSignal(validatedSignal)
        List referenceTypeList = Holders.config.validatedSignal.referenceType
        Map referenceType= [:]
        referenceTypeList.each { val -> referenceType.put(val.toString().replaceAll(" ",""),val) }
        def referenceTypeJson = new JsonBuilder(referenceType.sort({ it.value.toUpperCase() }))
        boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
        Map possibleDispositions = signalWorkflowService.fetchAllPossibleDispositionsForWorkflowState()
        Map possibleWorkflowStates= signalWorkflowService.fetchAllPossibleTransitionsFromCurrentState()
        String defaultValidatedDate=Holders.config.signal.defaultValidatedDate;
        boolean dueInStartEnabled= Holders.config.dueInStart.enabled
        String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
        if (Objects.nonNull(validatedSignal)) {
            dueIn = validatedSignalService.dueInCalculation(timezone, validatedSignal.dueDate)
        }
        Boolean hasSignalReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.VALIDATED_SIGNAL)
        String buttonClass = hasSignalReviewerAccess?"":"hidden"
        User user = userService.getUser();
        Set<Role> roles=user.getAuthorities()

        Map dispositionData = workflowRuleService.fetchDispositionData();
        String dispositionDataJSON = dispositionData as JSON
        dispositionDataJSON = dispositionDataJSON?.replace("'","\\\'")?.replace("\"","\\\"")
        List<String> editAllowedUser=  Holders.config.signal.allowedUser;
        boolean isAllowedUser=false;
        isEditDueDate=Holders.config.signal.editDueDate;
            for (Role role:roles){
                    if(editAllowedUser.contains(role.getAuthority())){
                        isAllowedUser=true;
                        break;
                    }
            }
        isEditDueDate=(true==isEditDueDate && isAllowedUser==true )?true:false;
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', signalId as Long)
            eq('objectType', Constants.AlertConfigType.VALIDATED_SIGNAL)
            order('dateCreated', 'desc')
            maxResults(1)
        } as UndoableDisposition
        isUndoable = undoableDisposition?.isEnabled
        cacheService.prepareUiLabelCacheForSafety()
        Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
        Map columnLabelForSCA = [:]
        // currently we only show 3 UI configurable fields of SCA in signal validated obs
        // Note we are not reloding map from DB, just fetching current info from cache
        columnLabelForSCA.put('caseNumber',rptToUiLabelMap.get('masterCaseNum'))
        columnLabelForSCA.put('productName',rptToUiLabelMap.get('productProductId'))
        columnLabelForSCA.put('masterPrefTermAll',rptToUiLabelMap.get('masterPrefTermSurAll'))


        def validatedAggHelpMap = Holders.config.signal.validatedAggHelpMap
        def validatedSingleHelpMap = Holders.config.signal.validatedSingleHelpMap
        def validatedLiteratureHelpMap =  Holders.config.signal.validatedLiteratureHelpMap

        List justificationList = []
        def justificationObjList = Justification.list()
        justificationObjList.each {
            if (it.getAttr("caseAddition") == "on") {
                justificationList.add(it)
            }
        }
        justificationList = justificationList.collect { [id: it.id, name: it.name, text: it.justification?.replaceAll('\r\n', '\\\n')] }
        String justificationListJSON = JsonOutput.toJson(justificationList)
        justificationListJSON = justificationListJSON?.replace("\'","\\\'")?.replace("\"","\\\"")
        Map labelConfigKeyId = alertFieldService.getAlertFields( 'AGGREGATE_CASE_ALERT', null, null, null ).collectEntries {
            b -> [ b.name, b.keyId ]
        }
        Map hyperlinkConfiguration = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.isHyperLink]    // Fix for bug PVS-54051
        }

        //Code changes to handle adhoc alert dynamic label
        List<Map> adhocConfigurationList = cacheService.getAdhocConfigurationCache()

        // Fetch labels based on the names and only if 'enabled' is true
        Map<String, String> nameToLabelMap = adhocConfigurationList.findAll {
            it.name in Constants.AdhocAlert.signalAdhocReviewObservationLabelList && it.enabled == true
        }.collectEntries {
            [(it.name): it.label]
        }
       
        Map detailsMap = [signal                              : validatedSignal,
                          genericComment                      : validatedSignal.genericComment,
                          validatedDateDispositions           : cacheService.getDispositionByNameInList(Holders.config.alert.validatedDateDispositions).id,
                          emergingIssues                      : emergingIssuesList ?: '-',
                          specialPEList                       : specialPEList,
                          chartCount                          : chartCount,
                          productNameList                     : productNameList,
                          conceptsMap                         : [:],
                          userList                            : userList,
                          caseCount                           : caseCount,
                          pecCountArgus                       : pecCountArgus,
                          pecCountFaers                       : pecCountFaers,
                          evdasCount                          : evdasCount,
                          adhocAlertDetails                   : adhocAlertDetails,
                          heatMap                             : heatMapModel,
                          datasources                         : datasources,
                          summaryReportPreference             : summaryReportPreference,
                          signalAssessmentDateRangeEnum       : SignalAssessmentDateRangeEnum.getDateRangeForFiltering(),
                          productNames                        : productNames ?: [""],
                          documentTypes                       : documentTypes ?: [""],
                          isTopicMigrated                     : isTopicMigrated,
                          dispositionList                     : dispositionService.getDispositionListByDisplayName(),
                          dataSourceMap                       : getDataSourceMap(),
                          dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingJSON,
                          dispositionData                     : dispositionDataJSON,
                          currentDispositionOptions           : currentDispositionOptions,
                          forceJustification                  : forceJustification,
                          availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                          availablePriorities                 : availablePriorities,
                          actionConfigList                    : actionConfigList,
                          meetingId                           : meetingId,
                          actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                          actionTypeList                      : actionTypeAndActionMap.actionTypeList,
                          literatureArticleUrl                : literatureArticleUrl,
                          currentUserFullName                 : currentUserFullName,
                          signalStatusList                    : signalStatusList?.sort({ it?.toUpperCase() }),
                          signalHistoryList                   : signalHistoryList,
                          timezone                            : timezone,
                          allowedProductsAsSafetyLead         : allowedProductsAsSafetyLead,
                          hasConfigurationEditorRole      : hasConfigurationEditorRole,
                          isProductSecurity                   : alertService.isProductSecurity(),
                          dispositionJustifications           : dispositionJustifications,
                          productSelected                     : productSelected,
                          referenceType                       : referenceType,
                          referenceTypeJson                   : referenceTypeJson,
                          workflowStatesSignal                : JsonOutput.toJson(possibleWorkflowStates),
                          enableSignalWorkflow                : enableSignalWorkflow,
                          dueIn                               : dueIn,
                          possibleDispositions                : JsonOutput.toJson(possibleDispositions),
                          isRor                               : cacheService.getRorCache(),
                          hasSignalReviewerAccess             : hasSignalReviewerAccess,
                          hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                          hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                          buttonClass                         : buttonClass,
                          hasDataAnalysisRole                 : SpringSecurityUtils.ifAnyGranted("ROLE_DATA_ANALYSIS"),
                          rmmType                             : JSON.parse("${Holders.config.signal.rmm.type}").sort().toString(),
                          communicationType                   : JSON.parse("${Holders.config.signal.communication.type}").sort().toString(),
                          rmmStatus                           : JSON.parse("${Holders.config.signal.rmm.communication.status}").sort().toString(),
                          isEditDueDate                       : isEditDueDate,
                          isSpotfireEnabled                   : Holders.config.signal.spotfire.enabled,
                          isPriorityEnabled                   : Holders.config.alert.priority.enable,
                          selectedDatasource                  : selectedDatasource,
                          reviewCompletedDispostionList       : reviewCompletedDispostionListJSON,
                          currUserName                        : currentUser.fullName,
                          columnLabelForSCA                   : columnLabelForSCA,
                          isUndoEnabled                       : isUndoable,
                          validatedAggHelpMap                 : validatedAggHelpMap,
                          validatedSingleHelpMap              : validatedSingleHelpMap,
                          validatedLiteratureHelpMap          : validatedLiteratureHelpMap,
                          justification                       : justificationList,
                          justificationJSON                   : justificationListJSON,
                          isMultiIngredient                   : validatedSignal.isMultiIngredient,
                          isPVCM                              : dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM),
                          labelConfigKeyId                    : labelConfigKeyId  as JSON,
                          hyperlinkConfiguration              : hyperlinkConfiguration,  // Fix for bug PVS-54051
                          nameToLabelMap                      : nameToLabelMap
        ]

        Integer prevColCount = Holders.config.signal.quantitative.number.prev.columns
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display + "_" + b.enabled]
        }

        Map labelConfigCopy = new HashMap();
        Map labelConfigDisplayNames = new HashMap();
        Iterator it = labelConfig.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = labelConfig.get(key).toString().split("_")[0];
            Boolean enabled = Boolean.parseBoolean(labelConfig.get(key).toString().split("_")[1]);
            labelConfigDisplayNames.put(key, value)
            labelConfigCopy.put(key, enabled)
            for (int j = 0; j < prevColCount; j++) {
                labelConfigCopy.put("exe" + j + key, enabled)
                labelConfigDisplayNames.put("exe" + j + key, "Prev Period "+(j+1)+" "+value)
            }
        }
        List<Map> labelWithPreviousPeriod = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true)
        List<Map> labelWithPreviousPeriodSubGroup = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll{it.type=="subGroup"}
        List<Map> labelWithPreviousPeriod1 = new ArrayList<Map>()

        for (int j = 0; j < prevColCount; j++) {
            for (int i = 0; i < labelWithPreviousPeriod.size(); i++) {
                if (!(labelWithPreviousPeriod.get(i).name in ['hlt', 'hlgt', 'smqNarrow'] )) {
                    labelWithPreviousPeriod1.add([
                            name   : "exe" + j + labelWithPreviousPeriod.get(i).name,
                            display: "Prev " + (j + 1) + " " + labelWithPreviousPeriod.get(i).display,
                            enabled: labelWithPreviousPeriod.get(i).enabled,
                            visible: true,
                            j      : j

                    ])
                }
            }
        }

        for (int j = 0; j < prevColCount; j++) {
            for (int i = 0; i < labelWithPreviousPeriodSubGroup.size(); i++) {
                labelWithPreviousPeriod1.add([
                        name   : "exe" + j + labelWithPreviousPeriodSubGroup.get(i).name,
                        display: "Prev " + (j + 1) + " " + labelWithPreviousPeriodSubGroup.get(i).display,
                        enabled: labelWithPreviousPeriodSubGroup.get(i).enabled,
                        visible: true,
                        previousPeriodCounter      : j

                ])
            }
        }

        labelWithPreviousPeriod1 = labelWithPreviousPeriod1 + labelWithPreviousPeriod
        detailsMap << validatedSignalService.prepareCreateMap(validatedSignal)
        boolean isEnableSignalCharts = PvsAppConfiguration.findByKey(Constants.ENABLE_SIGNAL_CHARTS).booleanValue
        detailsMap << [showDetectedBy      : Holders.config.validatedSignal.show.detected.by, showHealthAuthority: Holders.config.validatedSignal.show.health.authority,
                       showTopicInformation: Holders.config.validatedSignal.show.topic.information, showAggregateDate: Holders.config.validatedSignal.aggregateDate.enabled, showShareWith: Holders.config.validatedSignal.shareWith.enabled,
                       allSignalOutcomes   : SignalOutcome.list().findAll { !it.isDeleted }.collect { it.name },isQuant:true,
                       signalOutcomes      : SignalOutcome.findAllByDispositionId(validatedSignal.dispositionId).findAll { !it.isDeleted }.collect { it.name }, isEnableSignalCharts: isEnableSignalCharts , labelConfig    : labelConfigDisplayNames,
                                                                                                                                                                labelConfigCopy: labelConfigCopy,
                                                                                                                                                                labelConfigJson: alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true) as JSON,
                                                                                                                                                                labelConfigNew : labelWithPreviousPeriod1
        ]
        render(view: "details", model: detailsMap)
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
    Boolean isAuthorizedResource(ValidatedSignal validatedSignal, User currentUser) {
        if (!validatedSignal) {
            return false
        }

        Set<Group> groups = Group.findAllUserGroupByUser(currentUser)
        Group getAssignedToGroup = groups.find { group -> group.id == validatedSignal.assignedToGroup?.id }
        Set groupsIds = groups ? groups?.id : null

        def isSharedWithUser = validatedSignal.shareWithUser?.contains(currentUser)
        def isSharedWithGroup = validatedSignal.shareWithGroups?.id?.find { groupsIds.contains(it) }

        def isAuthorized = (validatedSignal.createdBy == currentUser.username ||
                validatedSignal.assignedTo?.id == currentUser.id ||
                validatedSignal.assignedToGroup?.members?.id?.contains(currentUser.id) ||
                getAssignedToGroup != null)
        if (isAuthorized) {
            return true
        }

        if (Holders.config.validatedSignal.shareWith.enabled) {
            def isWorkflowGroupAuthorized = Holders.config.pvsignal.workflowGroup.based.security ?
                    validatedSignal.workflowGroup.id == currentUser.workflowGroup?.id && (isSharedWithUser || isSharedWithGroup) :
                    isSharedWithUser || isSharedWithGroup

            if (isWorkflowGroupAuthorized) {
                return true
            } else {
                return false
            }
        } else {
            return true
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def singleCaseAlertList() {
        def result = validatedSignalService.getSingleCaseAlertListForSignal(params.id)
        render result as JSON
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def aggregateCaseAlertList(Long id) {
        def finalMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: []]
        def max = params.length as Integer
        def offset = params.start as Integer
        String searchTerm = params["search[value]"]
        if (StringUtils.isNotBlank(searchTerm) && searchTerm.contains("'")) {
            searchTerm = searchTerm.replaceAll("'","''")
            if (searchTerm.contains('"')) {
                searchTerm = searchTerm.replaceAll('"', "\"")
            }
        }

        def orderColumn = params["order[0][column]"]
        List finalList = []
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        def aggList = validatedSignalService.getAggregateAndEvdasAlertList(id,searchTerm)
        Map optionalFields = ["newCount1"         : ["newCount1", "cumCount1"], "newSeriousCount": ["newSeriousCount", "cumSeriousCount"]]
        if (orderColumn != null && orderColumnMap.name in optionalFields) {
            aggList?.sort { Map config1, Map config2 ->
                Integer  val1 = config1[optionalFields.get((orderColumnMap.name))?.get(0)] == "-" ? null : config1[optionalFields.get((orderColumnMap.name))?.get(0)] as Integer
                Integer  val2 = config2[optionalFields.get((orderColumnMap.name))?.get(0)] == "-"  ? null : config2[optionalFields.get((orderColumnMap.name))?.get(0)] as Integer
                Integer  val3 = config1[optionalFields.get((orderColumnMap.name))?.get(1)] == "-" ? null : config1[optionalFields.get((orderColumnMap.name))?.get(1)] as Integer
                Integer  val4 = config2[optionalFields.get((orderColumnMap.name))?.get(1)] == "-" ? null : config2[optionalFields.get((orderColumnMap.name))?.get(1)] as Integer
                if (orderColumnMap.name in optionalFields) {
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return val2 <=> val1 ?: val4 <=> val3
                    } else {
                        return val1 <=> val2 ?: val3 <=> val4
                    }
                }
            }
        } else if (orderColumn != null) {
            aggList?.sort { Map config1, Map config2 ->
                String sortedColumn = orderColumnMap?.name
                String val1 = config1[sortedColumn]
                String val2 = config2[sortedColumn]
                if (sortedColumn in ["productName","soc","preferredTerm","dataSource"]) {
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
        if(aggList?.size() > 0){
            finalList = (max >= 0 ) ? aggList?.subList(offset, Math.min(offset + max, aggList?.size())): aggList
        }
        finalMap = [aaData: finalList, recordsFiltered: aggList?.size(), recordsTotal: aggList?.size()]
        render(finalMap as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def literatureAlertList(Long id) {
        def finalMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: []]
        List literatureList = validatedSignalService.getLiteratureAlertListByAttachedSignal(id)
        def orderColumn = params["order[0][column]"]
        def max = params.length as Integer
        def offset = params.start as Integer
        String searchTerm = params["search[value]"]
        List finalList = []
        if (searchTerm.contains('"')) {
            searchTerm = searchTerm.replaceAll("\"", "")
        }
        List filteredLiteratureList = literatureList.findAll { literature ->
            // Iterate over the map keys and values
            literature.any { key, value ->
                // Check if any key or value contains the search term

                if (value?.toString()?.contains('"')) {
                    value = value?.toString().replaceAll('"', "")
                    key?.toString()?.toLowerCase()?.contains(searchTerm?.toLowerCase()) || value?.toString()?.toLowerCase()?.contains(searchTerm?.toLowerCase())
                } else {
                    key?.toString()?.toLowerCase()?.contains(searchTerm?.toLowerCase()) || value?.toString()?.toLowerCase()?.contains(searchTerm?.toLowerCase())
                }

            }
        }

        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        if (orderColumn != null) {
            filteredLiteratureList?.sort { Map config1, Map config2 ->
                String sortedColumn = orderColumnMap?.name
                String val1 = config1[sortedColumn]
                String val2 = config2[sortedColumn]
                if (sortedColumn in ["title","authors","publicationDate","disposition"]) {
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
        if(filteredLiteratureList?.size() > 0){
            finalList = (max >= 0 ) ? filteredLiteratureList?.subList(offset, Math.min(offset + max, filteredLiteratureList?.size())): filteredLiteratureList
        }
        finalMap = [aaData: finalList, recordsFiltered: filteredLiteratureList?.size(), recordsTotal: literatureList?.size()]


        render(finalMap as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def aggregateCaseAlertProductAndEventList() {
        def signal = ValidatedSignal.get(Long.parseLong(params.id))
        Map aggList = validatedSignalService.fetchProductAndEventFromAggList(signal.aggregateAlerts as List<AggregateCaseAlert>)
        render aggList as JSON
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def adHocAlertList(Long id) {
        Group workflowGroup = userService.getUser().workflowGroup
        ValidatedSignal signal = ValidatedSignal.get(id)
        Set<AdHocAlert> result = signal.adhocAlerts
        result = result.findAll {
            it.workflowGroup == workflowGroup
        }
        Map responseMap = [:]
        respond result.collect {
            responseMap = it.details(false)
            List<ValidatedSignal> signals = validatedSignalService.getSignalsFromAlertObj(it, Constants.AlertConfigType.AD_HOC_ALERT)
            responseMap.signalsAndTopics = signals?.collect { it.name + "(S)" }.join(",")
            responseMap
        }, [formats: ['json']]
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def changeAssignedTo() {

        def signalId = Long.parseLong(params.id)
        def newUserId = Long.parseLong(params.newValue)

        def newUser = null

        if (signalId && newUserId) {
            newUser = User.findById(newUserId)

            try {
                def validatedSignal = validatedSignalService.changeAssignedToUser(signalId, newUser)
                sendAssignedToEmail([newUser.email], validatedSignal)
            } catch (Exception ex) {
                ex.printStackTrace()
                render(status: BAD_REQUEST)
                return
            }
        }
        render(contentType: "application/json", status: OK.value()) {
            [success: 'true', newValue: newUser?.fullName, newId: newUser?.id]
        }
    }

//    Todo remove this
    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def changeGroup() {
        def signalId = Long.parseLong(params.id)
        def selectedGroups = params.selectedGroups

        def newGroups = ''
        if (signalId && selectedGroups) {
            try {
                def validatedSignal = ValidatedSignal.get(signalId);
                newGroups = validatedSignalService.changeGroup(validatedSignal, selectedGroups)
                List recipientList = validatedSignal.sharedGroups.collect { it.members.collect { it.email } }.flatten()
                sendAssignedToEmail(recipientList, validatedSignal)
            } catch (Exception ex) {
                ex.printStackTrace()
                render(status: BAD_REQUEST)
                return
            }
        }
        render(contentType: "application/json", status: OK.value()) {
            [success: 'true', newValue: newGroups]
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def changePriorityOfSignal(String selectedRows, Priority newPriority, String justification) {
        ValidatedSignal signal = ValidatedSignal.get(JSON.parse(selectedRows).first()."signal.id")
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            User user = userService.getUser();
            Set<Role> roles=user.getAuthorities()
            List<String> editAllowedUser=  Holders.config.signal.allowedUser;
            boolean isAllowedUser=false;
            boolean isEditDueDate=Holders.config.signal.editDueDate;
            for (Role role:roles){
                if(editAllowedUser.contains(role.getAuthority())){
                    isAllowedUser=true;
                    break;
                }
            }
            Integer data = validatedSignalService.changePriority(signal, newPriority, justification)
            responseDTO.data = ['dueIn': data,'dueDate':data!=null?  Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(signal.actualDueDate)).format("dd-MMM-yyyy"):""]
            responseDTO.value=(true==isEditDueDate && isAllowedUser==true )?1:0;

        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.priority.change.error")
        }
        render(responseDTO as JSON)
    }

    private void sendAssignedToEmail(List recipientList, ValidatedSignal signal) {
        if (recipientList) {
            def alertLink = createHref("validatedSignal", "details", ["id": signal.id])
            emailService.sendNotificationEmail([
                    'toAddress': recipientList,
                    "inboxType": "Assigned To Change",
                    'title'    : message(code: "email.signal.assignedTo.change.title"),
                    'map'      : ["map"         : ['Signal Name' : signal.name,
                                                   'Product Name': signal.getProductNameList(),
                                                   'Disposition' : signal.disposition?.displayName,
                                                   'Assigned To' : userService.getAssignedToName(signal)],
                                  "emailMessage": message(code: "email.signal.assignedTo.change.message"),
                                  "screenName"  : "Signal",
                                  "alertLink"   : alertLink
                    ]
            ])
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def getPriorities() {
        def justificationObjList = Justification.list()
        def justificationList = []
        def signalId = Long.parseLong(params.id)
        def signal = ValidatedSignal.get(signalId)

        justificationObjList.each {
            if (it.getAttr("signalPriority") == "on") {
                justificationList.add(it)
            }
        }
        def map = [currentValue: signal.priority.displayName, availableValues: (workflowService.priorities()), justification: justificationList]
        render(map as JSON)
    }
    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def uploadSignalAssessmentReport() {
        File reportFile
        String name=params.addReferenceName
        List dateList=getDateRange(params.dateRange, params.signalId as Long)
        params.startDateCharts = dateList[0]
        params.endDateCharts = dateList[1]
        reportFile = validatedSignalService.generateSignalSummaryReport(params, true, name,true)
        String dateString="""_${(dateList[0]).replace("/","-")}_${(dateList[1]).replace("/","-")}"""
        String nextSequence = validatedSignalService.getNext(true).toString()
        String fileName=params.addReferenceName+dateString+"_"+nextSequence
        User user=userService.getUser()
        ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId)
            try {
                List nameList = validatedSignalService.isFileSavedNameAlreadyAttachedToSignal(validatedSignal,[fileName])
                if(nameList.size()>=1){
                    fileName = validatedSignalService.generateNewFileName(validatedSignal,nameList.get(0))
                }
                attachmentableService.uploadAssessmentReport(user,validatedSignal,reportFile,fileName)
                String fileDescription = params.description
                List<Attachment> attachments = validatedSignal.getAttachments().sort { it.dateCreated }
                if (attachments) {
                    attachments = attachments[-1..-1]
                    attachments.each { attachment ->
                        AttachmentDescription attachmentDescription = new AttachmentDescription()
                        attachmentDescription.attachment = attachment
                        attachmentDescription.createdBy = userService.getUser().getFullName()
                        attachmentDescription.description = fileDescription
                        attachmentDescription.save(flush: true)
                    }
                }
                def attr = [fileName: fileName, signal: validatedSignal.name]

                activityService.createActivityForSignal(validatedSignal, '', "Attachment '${fileName}' is added with Description='${fileDescription ?: ''}'", ActivityType.findByValue(ActivityTypeValue.AttachmentAdded), validatedSignal.assignedTo,
                        userService.getUser(), attr, validatedSignal.assignedToGroup)
                flash.message = message(code: "file.upload.success")
                render(status: 200, message: 'success')
            } catch(FileFormatException e) {
                String errorMessage = message(code:"file.format.not.supported")
                render(status: 400, message: errorMessage)
            }

    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def updateAttachment(){
        ValidatedSignal validatedSignal = ValidatedSignal.findById(params.alertId)
        Attachment attachment = Attachment?.findById(params.attachmentId)
        String oldAttachment = attachment?.referenceLink
        String oldInputName = attachment?.inputName
        String oldAttachmentType = attachment?.referenceType
        String oldDescription = (AttachmentDescription.findByAttachment(attachment))?.description?: ''

            try {
                if(params?.attachments?.filename) {
//                  attachmentableService.removeAttachment(params.attachmentId as Long)
                    upload(params.alertId as Long)
                } else {
                    attachment.referenceType = params.attachmentType
                    attachment.inputName = params.inputName ? params.inputName : attachment.inputName
                    attachment.save()
                    AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(attachment)
                    attachmentDescription.description = params.description
                    attachmentDescription.save()

                    String newAttachment = attachment?.referenceLink
                    String newInputName = attachment?.inputName
                    String newAttachmentType = attachment?.referenceType
                    String newDescription = (AttachmentDescription.findByAttachment(attachment))?.description

                    String activityLog = ""
                    def changes = []
                    if (oldAttachment != newAttachment) {
                        changes << "Attach File changed from '${oldAttachment ?: ''}' to '${newAttachment ?: ''}'"
                    }
                    if (oldInputName != newInputName) {
                        changes << "File Name changed from '${oldInputName ?: ''}' to '${newInputName ?: ''}'"
                    }
                    if (oldAttachmentType != newAttachmentType) {
                        changes << "Reference Type changed from '${oldAttachmentType ?: ''}' to '${newAttachmentType ?: ''}'"
                    }
                    if (oldDescription != newDescription) {
                        changes << "Description changed from '${oldDescription ?: ''}' to '${newDescription ?: ''}'"
                    }
                    activityLog += changes.join(', ')
                    if(activityLog == ''){
                        activityLog = "Attachment '" + attachment.savedName + "' is updated"
                    }
                    Map attr = [fileName: attachment.savedName, signal: validatedSignal.name]
                    activityService.createActivityForSignal(validatedSignal, '',activityLog,
                            ActivityType.findByValue(ActivityTypeValue.AttachmentUpdated), validatedSignal.assignedTo,
                            userService.getUser(), attr, validatedSignal.assignedToGroup)
                }
            } catch (FileFormatException e) {
                String errorMessage = message(code: "file.format.not.supported")
                render(status: 400, message: errorMessage)
            }

    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def upload(final Long id) {
        def validatedSignal = ValidatedSignal.findById(id)
        def fileName = params?.attachments.filename
        if (fileName instanceof String) {
            fileName = [fileName]
        }
        String inputFileName = null
        List nameList = []
        if(params.fileName){
            inputFileName = params.fileName
            nameList = validatedSignalService.isFileSavedNameAlreadyAttachedToSignal(validatedSignal,[inputFileName])
        } else {
            inputFileName = fileName.get(0)
        }

        try {
                User currentUser = userService.getUser()
                if(nameList.size()>=1){
                    inputFileName = validatedSignalService.generateNewFileName(validatedSignal,nameList.get(0))
                }
                Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileName, validatedSignal, request,params.inputName,params.attachmentType, inputFileName)
                def attachmentsSize = 0 - filesStatusMap?.uploadedFiles?.size()
                String fileDescription = params.description
                List<Attachment> attachments = validatedSignal.getAttachments().sort { it.dateCreated }
                AttachmentDescription attachmentDescription = new AttachmentDescription()
                if (attachments) {
                    attachments = attachments[-1..attachmentsSize]
                    attachments.each { attachment ->
                        if(AttachmentDescription.findByAttachment(attachment) == null){
                            attachmentDescription.attachment = attachment
                            attachmentDescription.createdBy =userService.getUser().getFullName()
                            attachmentDescription.description = fileDescription
                            attachmentDescription.save(flush: true)
                        }else{
                            attachmentDescription = AttachmentDescription.findByAttachment(attachment)
                            attachmentDescription.description = fileDescription
                            attachmentDescription.save(flush: true)
                        }
                    }
                }

                if (filesStatusMap?.uploadedFiles) {
                    def attr = [fileName: inputFileName, signal: validatedSignal.name]
                    String description = attachmentDescription.description ? attachmentDescription.description : ""
                    String referenceType = attachmentDescription.attachment.referenceType
                    String uploadedFileName = filesStatusMap?.uploadedFiles*.originalFilename
                    String details = "Attachment '${uploadedFileName}' is added with Description='${description}', Reference Type ='${referenceType}'"
                    activityService.createActivityForSignal(validatedSignal, '', details, ActivityType.findByValue(ActivityTypeValue.AttachmentAdded), validatedSignal.assignedTo,
                            userService.getUser(), attr, validatedSignal.assignedToGroup)
                }
                flash.message = message(code: "file.upload.success")
                render(status: 200, message: 'success')
            } catch (FileFormatException e) {
                String errorMessage = message(code: "file.format.not.supported")
                render(status: 400, message: errorMessage)
            }
    }

    def fetchCurrentAttachmentData(Long attachmentId) {
        Attachment attachment = Attachment.findById(attachmentId)
        Map details = [:]
        if (attachment) {
            String inputName = attachment.inputName
            String savedName = attachment.savedName
            if (savedName == null) {
                savedName = attachment.referenceLink
            }
            details = [inputName: inputName, savedName: savedName]
        }
        render(details as JSON)
    }

    def fetchCurrentRmmData(Long attachmentId) {
        Attachment attachment = Attachment.findById(attachmentId)
        Map details = [:]
        if (attachment) {
            String inputName = attachment.inputName
            String savedName = attachment.name
            if (attachment.attachmentType == AttachmentType.Attachment) {
                savedName = attachment?.ext ? (savedName + "." + attachment.ext) : savedName
            } else {
                savedName = attachment.referenceLink
            }
            details = [inputName: inputName, savedName: savedName]
        }
        render(details as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def deleteAttachment(Long attachmentId, Long alertId) {
        Attachment attachment = Attachment.findById(attachmentId)
        if (attachment) {
            AttachmentType attachmentType = attachment.attachmentType
            String fileName = (attachmentType == AttachmentType.Attachment) ? (attachment.savedName) : (attachment.referenceLink)
            ValidatedSignal validatedSignal = ValidatedSignal.findById(alertId)
            String attachmentReference = attachment.referenceType ?: ""
            AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(attachment)
            String description = (attachmentDescription) ? attachmentDescription.description : ""
//            if (AttachmentDescription.findByAttachment(attachment)) {
//                AttachmentDescription.findByAttachment(attachment).delete()
//            }
            attachmentableService.removeAttachment(attachmentId)
            Map attr = [fileName: fileName, signal: validatedSignal.name]
            activityService.createActivityForSignal(validatedSignal, '', "${attachmentType} '${fileName}' is removed with Description= '${description ?: ''}', Reference Type = ${attachmentReference}",
                    ActivityType.findByValue((attachmentType == AttachmentType.Attachment) ? ActivityTypeValue.AttachmentRemoved : ActivityTypeValue.ReferenceRemoved), validatedSignal.assignedTo,
                    userService.getUser(), attr, validatedSignal.assignedToGroup)
        }
        render(['success': true] as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def updateReference(){
        Attachment attachment = Attachment.findById(params.attachmentId)
        ValidatedSignal validatedSignal = ValidatedSignal.get(params.alertId)
        String oldReferenceLink = attachment?.referenceLink
        String oldInputName = attachment?.inputName
        String oldReferenceType = attachment?.referenceType
        String oldDescription = (AttachmentDescription.findByAttachment(attachment))?.description ?: ''
        try{
            if(params.referenceLink  && (attachment?.attachmentType==AttachmentType.Attachment)) {
                if (attachment) {
                    String referenceLink = attachment.referenceLink
                    String referenceName = attachment.referenceLink
                  //  attachmentableService.removeAttachment(params.attachmentId as Long)
                }
                addReference(params.alertId as Long)
            } else {
                if (params.referenceLink != null && params.referenceLink != "") {
                    attachment.referenceLink = params.referenceLink //params.referenceLink is only there when user changes unless it is null always
                }
                attachment.inputName = params.inputName
                attachment.referenceType = params.attachmentType
                attachment.save()
                AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(attachment)
                attachmentDescription.description = params.description
                attachmentDescription.save()

                String newReferenceLink = attachment?.referenceLink
                String newInputName = attachment?.inputName
                String newReferenceType = attachment?.referenceType
                String newDescription = (AttachmentDescription.findByAttachment(attachment))?.description

                String activityLog = ""
                def changes = []
                if (oldReferenceLink != newReferenceLink) {
                    changes << "Reference Link changed from '${oldReferenceLink ?: ''}' to '${newReferenceLink ?: ''}'"
                }
                if (oldInputName != newInputName) {
                    changes << "References changed from '${oldInputName ?: ''}' to '${newInputName ?: ''}'"
                }
                if (oldReferenceType != newReferenceType) {
                    changes << "Reference Type changed from '${oldReferenceType ?: ''}' to '${newReferenceType ?: ''}'"
                }
                if (oldDescription != newDescription) {
                    changes << "Description changed from '${oldDescription ?: ''}' to '${newDescription ?: ''}'"
                }

                activityLog += changes.join(', ')

                if(activityLog ==  ""){
                    activityLog = "Reference '" + attachment.referenceLink + "' is updated"
                }
                Map attr = [fileName: attachment.savedName, signal: validatedSignal.name]
                activityService.createActivityForSignal(validatedSignal, '',activityLog,
                        ActivityType.findByValue(ActivityTypeValue.ReferenceUpdated), validatedSignal.assignedTo,
                        userService.getUser(), attr, validatedSignal.assignedToGroup)
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
            flash.error = message(code: "reference.error.message")
        }
    }
    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def addReference(Long id) {
        ValidatedSignal validatedSignal = ValidatedSignal.get(id)
        if (validatedSignal) {
            String referenceLink = params.referenceLink
            User currentUser = userService.getUser()
            try {
                attachmentableService.doAddReference(currentUser, validatedSignal, referenceLink,params.inputName,params.attachmentType)
                List<Attachment> attachments = validatedSignal.getReferences()
                AttachmentDescription attachmentDescription = new AttachmentDescription()
                if (attachments) {
                    if (AttachmentDescription.findByAttachment(attachments.first()) == null) {
                        attachmentDescription.attachment = attachments.first()
                        attachmentDescription.createdBy = currentUser.getFullName()
                        attachmentDescription.description = params.description
                        attachmentDescription.save(flush: true)
                    } else {
                        attachmentDescription = AttachmentDescription.findByAttachment(attachments.first())
                        attachmentDescription.description = params.description
                        attachmentDescription.save(flush: true)
                    }
                }

                if (referenceLink) {
                    Map attr = [referenceLink: referenceLink, signal: validatedSignal.name]
                    String description = attachmentDescription.description ? attachmentDescription.description : ""
                    String details = message(code:"com.rxlogix.signal.ValidatedSignal.reference.activity.description", args: [referenceLink, description, attachmentDescription.attachment.referenceType])
                    activityService.createActivityForSignal(validatedSignal, '', details, ActivityType.findByValue(ActivityTypeValue.ReferenceAdded), validatedSignal.assignedTo,
                            userService.getUser(), attr, validatedSignal.assignedToGroup)
                }
                flash.message = message(code: "reference.add.success")
            } catch (ValidationException ve) {
                log.error(ve.printStackTrace())
                flash.error = message(code: "reference.error.message")
            } catch (Exception e) {
                log.error(e.printStackTrace())
                flash.error = message(code: "reference.error.message")
            }
        }
        render(['success': true] as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def deleteReference(Long attachmentId, Long alertId) {
        Attachment attachment = Attachment.get(attachmentId)
        if (attachment) {
            String referenceLink = attachment.referenceLink
            ValidatedSignal validatedSignal = ValidatedSignal.get(alertId)
            String referenceName = attachment.referenceLink
            AttachmentDescription attachmentDescription = AttachmentDescription.findByAttachment(attachment)
            String attachmentReference = attachment.referenceType ?: ""
            String description = (attachmentDescription) ? attachmentDescription.description : ""
//            if (attachmentDescription) {
//                attachmentDescription.delete()
//            }
            attachmentableService.removeAttachment(attachmentId)
            Map attr = [referenceLink: referenceLink, signal: validatedSignal.name]
            activityService.createActivityForSignal(validatedSignal, '', "Reference '${referenceLink}' is removed with Description= '${description ?: ''}', Reference Type = ${attachmentReference}",
                    ActivityType.findByValue(ActivityTypeValue.ReferenceRemoved), validatedSignal.assignedTo,
                    userService.getUser(), attr, validatedSignal.assignedToGroup)
            flash.message = message(code: "reference.remove.success", args: [referenceName])
        }
        render(['success': true] as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def listStrageties() {
        def strategies = SignalStrategy.list().collect { it.toDto() }
        respond strategies, [formats: ['json']]
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def searchStrategyProducts() {
        def strategyProducts = []
        render strategyProducts as JSON
    }

    /**
     * Action to generate the Signal PBRER report.
     * @return
     */
    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def generateSignalReports() {
        def reportType = params.reportType
        if (reportType == Constants.SignalReportTypes.PEBER) {
            def reportFile = validatedSignalService.generateSignalReports(params)
            response.addCookie(new Cookie((params.action).toLowerCase(), ""))
            renderReportOutputType(reportFile, "SignalSummaryReportPbrer")
            ValidatedSignal signal = ValidatedSignal.get(params.signalId as Long)
            String entityValue = signal.getInstanceIdentifierForAuditLog() + ": " + "PBRER Signal Summary Report"
            signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, entityValue, "Signal", params, reportFile.name)
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def generateSignalAssessmentReport(ValidatedSignal validatedSignal) {
        Map assessmentData = validatedSignalService.generateAssessmentDetailsMap(params,validatedSignal)
        File reportFile = dynamicReportService.createAssessmentReport(assessmentData, params)
        response.addCookie(new Cookie((params.action).toLowerCase(), ""))
        renderReportOutputType(reportFile, "SignalAssessmentReport")
        String entityValue = validatedSignal.getInstanceIdentifierForAuditLog() + ": " + "Signal Assessment Report"
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,entityValue,"Signal",params,reportFile.name)
    }

    def getDateRange(date,Long signalId) {
        SignalAssessmentDateRangeEnum dateRange = date as SignalAssessmentDateRangeEnum
        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
        String timeZone = Holders.config.server.timezone
        List dateRangeList=[]
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
                dateRangeList = validatedSignalChartService.fetchDateRangeFromCaseAlerts(validatedSignal.singleCaseAlerts as List<SingleCaseAlert>)
                break
            case SignalAssessmentDateRangeEnum.LAST_1_YEAR:
            default:
                dateRangeList = RelativeDateConverter.lastXYears(null, 1, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
        }
        return dateRangeList
    }


    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def generateSignalSummaryReport(String dataSource) {
        ValidatedSignal validatedSignal = ValidatedSignal.findById(Long.parseLong(params.signalId))
        def summaryReportPreference = JSON.parse(validatedSignal.signalSummaryReportPreference ?: '{"ignore":[],"required":["Signal Information","WorkFlow Log" ,"Safety Observations","References","Actions","Meetings","RMMs","Communication"]}')
        File reportFile = validatedSignalService.generateSignalSummaryReport(params)
        response.addCookie(new Cookie((params.action).toLowerCase(), ""))


        String name = MiscUtil.getValidFileName(validatedSignal.name)
        if(name){
            name = name.substring(0, Math.min(name.length(), 180)) //Added for PVS-60074
        }
        String entityValue = validatedSignal.getInstanceIdentifierForAuditLog() + ": " + "Signal Summary Report"
        if (validatedSignal.attachments && summaryReportPreference.required.contains('Attached Documents')) {
            def files = validatedSignal.attachments.collect { Attachment atta ->
                AttachmentableUtil.getFile(grailsApplication.config, atta)
            }
            files.push(reportFile)
            File zf = zipFiles("${name}_SignalSummaryReport", files)
            params.outputFormat = "zip"
            signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,entityValue,"Signal",params,reportFile.name)
            renderCompressedFile(zf)
        } else {
            renderReportOutputType(reportFile, "${name}_SignalSummaryReport")
            signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,entityValue,"Signal",params,reportFile.name)
        }
    }

    /**
     * Send the output type (PDF/Excel/Word) to the browser which will save it to the user's local file system
     * @param reportFile
     * @return
     */
    void renderReportOutputType(File reportFile, String name) {
        String reportName = name + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

    private renderCompressedFile(File zipFile) {
        String reportName = zipFile.name + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "application/zip"
        response.contentLength = zipFile.size()
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.zip" + "\"")
        response.getOutputStream().write(zipFile.bytes)
        response.outputStream.flush()
    }

    /**
     * The method to initiate the chart data generation and persist that into DB.
     * @return
     */
    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def initiateChartDataGeneration() {
        def signalId = params.signalId
        def signalChartName = params.chartName

        def validatedSignal = ValidatedSignal.get(Long.parseLong(signalId))
        def singleCaseAlertList = validatedSignal.singleCaseAlerts
        def caseData = []
        singleCaseAlertList?.each { sca ->
            caseData.add(sca.caseNumber + "-" + sca.caseVersion)
        }

        if (caseData.size() > 0) {
            caseData = caseData.join(",")
            def response = validatedSignalService.scheduleChartReport(validatedSignal.name, signalChartName, caseData)
            if (response.status == 200) {
                sendResponse(200, response.result)
            } else {
                def errorMessage = message(code: "app.label.chart.error")
                sendResponse(500, errorMessage)
            }
        } else {
            def errorMessage = message(code: "app.label.case.data.error", args: ['Signal'])
            sendResponse(500, errorMessage)
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportReport(DataTableSearchRequest searchRequest) {
        def startTime=System.currentTimeSeconds()
        User user = userService.user
        List<ValidatedSignal> validatedSignalList = []
        if (params.idList) {
            List<Long> idList = params.idList.split(',')?.collect { Long.parseLong(it) }
            validatedSignalList = ValidatedSignal.withCriteria {
                or {
                    idList.collate(1000).each{
                        'in'('id', it)
                    }
                }
            }
        } else {
            def jsonSlurper = new JsonSlurper()
            searchRequest.searchParam = jsonSlurper.parseText(params.dataTableSearchRequest)
            String selectedSignalFilter = session.getAttribute("signalFilter")
            validatedSignalList = validatedSignalService.exportValidatedSignal(searchRequest, params,selectedSignalFilter)
        }
        List criteriaSheetList = []
        String searchString = searchRequest?.searchParam?.search?.getValue() ?: null
        params.criteriaSheetList = dynamicReportService.createCriteriaList(user,null,searchString)
        def workflowStates=signalWorkflowService.fetchSignalWorkflowStates()
        List<Map> validatedSignals = validatedSignalList.collect { it.toExportDto(user.preference.timeZone,workflowStates) }
        // split data for Additional Information/Comments field
        Integer maxSize = 1
        validatedSignals.each { map ->
            def comment = map.find { it.key == 'comments' }?.value
            if (comment) {
                map.remove('comment')
                List commentList = splitCellContent(comment as String)
                maxSize = commentList.size() > maxSize ? commentList.size() : maxSize
                Integer i = 1
                commentList.each { it ->
                    String key = "comments" + i
                    map.put(key, it)
                    i++
                }
            }
        }
        params << [maxSize: maxSize]
        File reportFile = dynamicReportService.createSignalsReport(new JRMapCollectionDataSource(validatedSignals), params)
        log.info("it took total time ${System.currentTimeSeconds()-startTime} seconds to export signal tracker report")
        renderReportOutputType(reportFile, "SignalListing")
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,"Signal Tracker Export","Signal",params,reportFile.name)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def signalOutcomes() {
        def signalId = params.id
        def validatedSignal = ValidatedSignal.get(signalId)
        render([allSignalOutcomes: SignalOutcome.list().findAll { !it.isDeleted }.collect { it.name },
                existingSignals: validatedSignal?.signalOutcomes?.findAll { !it.isDeleted }.collect { it.name },
                signalOutcomes   : SignalOutcome.findAllByDispositionId(validatedSignal?.dispositionId).findAll {!it.isDeleted}.collect { it.name }] as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportSignalDetailReport() {
        def signalList = validatedSignalService.getSignalDetails()
        def nameOfReport = "Signal Detail Report"
        def reportFile = dynamicReportService.createSignalDetailReport(
                new JRMapCollectionDataSource(signalList), params, nameOfReport)
        renderReportOutputType(reportFile, "SignalDetailReport")
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportMeetingDetailReport(Long signalId) {
        def meetingList = validatedSignalService.getMeetingDetails(signalId)
        params.criteriaSheetList = dynamicReportService.createCriteriaList(userService.getUser())
        def reportFile = dynamicReportService.createMeetingDetailReport(new JRMapCollectionDataSource(meetingList), params)
        renderReportOutputType(reportFile, "MeetingDetailReport")
        def entityValue = ValidatedSignal.get(signalId).getInstanceIdentifierForAuditLog()
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, entityValue, "Signal: Meetings", params, reportFile.name)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def exportSignalActionDetailReport() {
        File reportFile = validatedSignalService.exportSignalActionDetailReport(params)
        response.addCookie(new Cookie((params.action).toLowerCase(), ""))
        renderReportOutputType(reportFile, "SignalActionDetailReport")
        ValidatedSignal validatedSignal=ValidatedSignal.get(params.signalId)
        String entityValue = validatedSignal.getInstanceIdentifierForAuditLog() + ": " + "All Signal Actions"
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,entityValue,"Signal",params,reportFile.name)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def getChartData() {
        def response = validatedSignalService.getReportExecutionStatus(params.executedId)
        if (response.status == 200) {
            def reportStatus = response.result.executionStatus
            if (reportStatus == "COMPLETED") {
                def chartResponse = validatedSignalService.getChartData(params.executedId)
                if (chartResponse.status == 200) {
                    def signal = ValidatedSignal.get(params.signalId)
                    def oldChart = SignalChart.findByChartNameAndValidatedSignal(params.chartName, signal)
                    if (!oldChart) {
                        def signalChart = new SignalChart([
                                chartName      : params.chartName,
                                execId         : params.executedId,
                                chartData      : new JsonBuilder(chartResponse.result).toPrettyString(),
                                validatedSignal: signal
                        ])
                        signalChart.save()
                    } else {
                        oldChart.chartData = new JsonBuilder(chartResponse.result).toPrettyString()
                        oldChart.save()
                    }
                    sendResponse(200, chartResponse.result)
                } else {
                    sendResponse(500, message(code: "app.label.chart.error"))
                }
            } else {
                sendResponse(500, message(code: "app.label.chart.error"))
            }
        } else {
            sendResponse(500, message(code: "app.label.chart.error"))
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def getCurrentChartData() {
        def signal = ValidatedSignal.get(params.signalId)
        def chartName = params.chartName

        def signalChart = SignalChart.findByChartNameAndValidatedSignal(chartName, signal)

        def chartResult = []
        if (signalChart) {
            chartResult = [chartData: JSON.parse(signalChart.chartData), chartName: chartName]
            render chartResult as JSON
        } else {
            chartResult = [chartData: null, errorMessage: message(code: "app.label.chart.error"), chartName: chartName]
        }
        render chartResult as JSON
    }

    //Method to prepare the response.
    private def sendResponse(stat, msg) {
        response.status = stat
        render(contentType: "application/json") {
            responseText = msg
            status = stat
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def createTopic() {
        redirect(controller: "topic", action: "create")
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def showTrendAnalysis() {
        def signalId = params.id
        redirect(controller: "trendAnalysis", action: 'showTrendAnalysis', params: [id: signalId, type: Constants.AlertConfigType.VALIDATED_SIGNAL])
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def getTrendData() {
        def productName = params.productName
        def eventName = params.eventName
        def trendData = validatedSignalService.getTrendData(productName, eventName)
        render(contentType: 'text/csv', text: trendData)
    }

    def showProbabilityAnalysis() { [] }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def renderExportFile() {
        def outputType = params.outputFormat;
        def caseType = params.caseType
        response.reset()
        response.contentType = "${outputType}; charset=UTF-8"
        response.setHeader("Content-disposition", "Attachment; filename=\"" + caseType + "Report." + outputType + "\"")
        File outputFile = new File(caseType + "Report." + outputType)
        outputFile.createNewFile()
        response.getOutputStream().write(outputFile.bytes)
        response.getOutputStream().flush()
        response.getOutputStream().close()
        outputFile.delete()
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def partnersList() {
        def partnetList = Holders.config.partnersList
        def signalId = params.signalId
        def validatedSignal = ValidatedSignal.findById(signalId)
        def product = validatedSignal.getProductNameList()

        def pList = []

        partnetList.each {
            if (product.contains(it.productName)) {
                pList.add(it)
            }
        }
        render pList as JSON
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def topology() {
        def ruleList = []
        def stateList = []
        def initialState = PVSState.findByValue("Assessment")
        stateList.add(initialState)
        filltNextLevelState(initialState, ruleList, stateList)
        def topologyData = ['States': stateList, 'Rules': ruleList]
        render(view: "topology", model: [topologyData: topologyData as JSON])
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def filltNextLevelState(state, ruleList, stateList) {
        def rules = WorkflowRule.findAllByIncomeStateAndIsDeleted(state, false)
        if (rules) {
            for (WorkflowRule rule : rules) {
                def targetState = rule.targetState
                ruleList.add(rule)
                stateList.add(targetState)
                if (!targetState.finalState) {
                    filltNextLevelState(targetState, ruleList, stateList)
                }
            }
        }
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchSignalHistory(Long id) {
        ValidatedSignal validatedSignal = ValidatedSignal.get(id)
        List<Map> signalHistory = SignalHistory.findAllByValidatedSignal(validatedSignal, [sort: 'dateCreated', order: 'desc']).collect {
            it.toDto()
        }
        respond signalHistory, [formats: ['json']]
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def peAnalysis(Long id, Boolean isTopic) {
        List<Map> peData = validatedSignalService.fillPeMap(id)
        render(peData as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def listPreviousSignals(Long id) {
        ValidatedSignal vs = ValidatedSignal.get(id)

        List<ValidatedSignal> relatedSignals = validatedSignalService.getRelevantSignals(vs)

        List<Map> relatedSignalResp = relatedSignals.collect { ValidatedSignal signal ->
            [
                    name            : signal.name,
                    term            : signal.eventSelectionList.join(", "),
                    disposition     : signal.disposition?.displayName,
                    dateClosed      : signal.dateClosed,
                    lastReviewedDate: signalHistoryService.getLatestSignalHistory()?.lastUpdated,
                    comments        : signal.comments.collect {
                        it.comments
                    },
                    actions         : signal.actions?.collect { it ->
                        it.details
                    }
            ]
        }
        render(relatedSignalResp as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def heatMap(Long id) {
        ValidatedSignal vs = ValidatedSignal.get(id)
        Map heatMap = validatedSignalService.heatMapData(vs)
        render view: 'heat_map', model: [heatMap: [socs: heatMap.socs, years: heatMap.years, data: heatMap.data]]
    }


    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def disassociateFromSignal(String alertType, String alertId, String signalName, String justification, String productJson, Boolean isArchived,Long signalId,String dueDate) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        Boolean attachedSignalData
        ActivityType activityType
        try {
            def attribute
            def alert
            String details = ''
            Long dueIn=null;
            if (null != dueDate && dueDate.length() > 0) {
                ValidatedSignal signal = ValidatedSignal.findById(signalId)
                Date newDueDate = DateUtil.stringToDate(dueDate, "dd-MMM-yyyy", Holders.config.server.timezone)
                if(newDueDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))){
                    throw new IllegalArgumentException("Invalid date")
                }
                String newDueDateString = DateUtil.fromDateToString(newDueDate, DateUtil.DEFAULT_DATE_FORMAT)
                Integer dueIn1=validatedSignalService.calculateDueIn(signal.id, signal.workflowState)
                Date oldDueDate = (signal.dueDate == null) ? new Date().plus(dueIn1?dueIn1:0) : signal.dueDate
                String oldDueDateString = DateUtil.fromDateToString(oldDueDate, DateUtil.DEFAULT_DATE_FORMAT)
                String detailsMessage = "Due Date has been changed from '${oldDueDateString}' to '${newDueDateString}'"
                activityType = ActivityType.findByValue(ActivityTypeValue.StatusDate)
                signal.dueDate = newDueDate
                signal.actualDueDate = newDueDate
                signal.customAuditProperties = ["justification": justification]
                signal.isDueDateUpdated = true
                CRUDService.update(signal)
                boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
                String defaultValidatedDate = Holders.config.signal.defaultValidatedDate;
                boolean dueInStartEnabled= Holders.config.dueInStart.enabled
                String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
                if(enableSignalWorkflow){
                    dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(signal.id ,dueInStartPoint) : validatedSignalService.calculateDueIn(signal.id ,signal.workflowState)
                } else {
                    dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(signal.id ,dueInStartPoint,true) : validatedSignalService.calculateDueIn(signal.id ,defaultValidatedDate,true)
                }
                activityService.createActivityForSignal(signal, justification, detailsMessage, activityType,
                        signal.assignedTo, userService.getUser(), [:], signal.assignedToGroup);
                validatedSignalService.changeToUndoableDisp(signalId)
                responseDTO.data = ['dueIn': dueIn,'dueDate':dueIn!=null?new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(signal.actualDueDate):""]
                render(responseDTO as JSON)
                return;
            }

            disassociateAlertAndCreateActivity(signalId, alertType, justification, Long.parseLong(alertId), isArchived)

            render(responseDTO as JSON)

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace()
            render(status: BAD_REQUEST, message: "Invalid Date")
        } catch (Exception ex) {
            ex.printStackTrace()
            render(status: BAD_REQUEST)
        }
    }

    def disassociateAlertAndCreateActivity(Long signalId, String alertType, String justification, Long alertId, Boolean isArchived) {

        ValidatedSignal signal = ValidatedSignal.findById(signalId)
        String signalName = signal.name

        ActivityType activityType
        def attribute
        def alert
        String details = ""

        if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
            alert = isArchived ? ArchivedSingleCaseAlert.findById(alertId.toInteger()) : SingleCaseAlert.findById(alertId.toInteger())
            activityType = ActivityType.findByValue(ActivityTypeValue.CaseDissociated)
            details = "Case '${alert.caseNumber}' has been dissociated from '${signalName}'"
            signalAuditLogService.createAuditLog([
                    entityName: "Signal: Individual Case Review Observations",
                    moduleName: "Signal: Individual Case Review Observations",
                    category: AuditTrail.Category.UPDATE.toString(),
                    entityValue: "${signalName}: Case dissociated",
                    username: userService.getUser().username,
                    fullname: userService.getUser().fullName
            ] as Map, [[propertyName: "Case dissociated", oldValue: "Alert Name-${alert?.alertConfiguration?.isStandalone ? "" :alert.name},Product Name-${alert.productName},Case Number-${alert.caseNumber}", newValue:"" ],
                       [propertyName: "Justification", oldValue: "", newValue: justification]] as List)
        } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
            alert = isArchived ? ArchivedAggregateCaseAlert.findById(alertId.toInteger()) : AggregateCaseAlert.findById(alertId.toInteger())
            activityType = ActivityType.findByValue(ActivityTypeValue.PECDissociated)
            details = "PEC '${alert.productName}'-'${alert.pt}' has been dissociated from '${signalName}'"
            signalAuditLogService.createAuditLog([
                    entityName: "Signal: Aggregate Review Observations",
                    moduleName: "Signal: Aggregate Review Observations",
                    category: AuditTrail.Category.UPDATE.toString(),
                    entityValue: "${signalName}: PEC dissociated",
                    username: userService.getUser().username,
                    fullname: userService.getUser().fullName
            ] as Map, [[propertyName: "PEC dissociated", oldValue: "Alert Name-${alert.name},Product Name-${alert.productName},Event PT-${alert.pt}", newValue: ""],
                       [propertyName: "Justification", oldValue: "", newValue: justification]] as List)
        } else if (Constants.AlertConfigType.EVDAS_ALERT == alertType) {
            alert = isArchived ? ArchivedEvdasAlert.findById(alertId.toInteger()) : EvdasAlert.findById(alertId.toInteger())
            activityType = ActivityType.findByValue(ActivityTypeValue.CaseDissociated)
            details = "PEC '${alert.substance}'-'${alert.pt}' has been dissociated from '${signalName}'"
            signalAuditLogService.createAuditLog([
                    entityName: "Signal: Aggregate Review Observations",
                    moduleName: "Signal: Aggregate Review Observations",
                    category: AuditTrail.Category.UPDATE.toString(),
                    entityValue: "${signalName}: PEC dissociated",
                    username: userService.getUser().username,
                    fullname: userService.getUser().fullName
            ] as Map, [[propertyName: "PEC dissociated", oldValue: "Alert Name-${alert.name},Product Name-${alert.substance},Event PT-${alert.pt}", newValue: ""],
                       [propertyName: "Justification", oldValue: "", newValue: justification]] as List)
        } else if (Constants.AlertConfigType.LITERATURE_SEARCH_ALERT == alertType) {
            alert = isArchived ?
                    (ArchivedLiteratureAlert.findById(alertId.toInteger()) ?: ArchivedEmbaseLiteratureAlert.findById(alertId.toInteger())) :
                    (LiteratureAlert.findById(alertId.toInteger()) ?: EmbaseLiteratureAlert.findById(alertId.toInteger()))
            activityType = ActivityType.findByValue(ActivityTypeValue.ArticleDissociated)
            details = "Article '${alert.articleId}' has been dissociated from '${signalName}'"
            signalAuditLogService.createAuditLog([
                    entityName: "Signal: Literature Review Observations",
                    moduleName: "Signal: Literature Review Observations",
                    category: AuditTrail.Category.UPDATE.toString(),
                    entityValue: "${signalName}: Article dissociated",
                    username: userService.getUser().username,
                    fullname: userService.getUser().fullName
            ] as Map, [[propertyName: "Article dissociated", oldValue: "Alert Name-${alert.name},Title-${alert.articleTitle},Author-${alert.articleAuthors}", newValue: ""],
                       [propertyName: "Justification", oldValue: "", newValue: justification]] as List)
        } else {
            alert = AdHocAlert.findById(alertId.toInteger())
            activityType = ActivityType.findByValue(ActivityTypeValue.AdhocAlertDissociated)
            details = "AdHoc '${alert.name}'' has been dissociated from '${signalName}'"
            signalAuditLogService.createAuditLog([
                    entityName: "Signal: Ad-Hoc Review Observations",
                    moduleName: "Signal: Ad-Hoc Review Observations",
                    category: AuditTrail.Category.UPDATE.toString(),
                    entityValue: "${signalName}: AdHoc dissociated",
                    username: userService.getUser().username,
                    fullname: userService.getUser().fullName
            ] as Map, [[propertyName: "AdHoc dissociated", oldValue: "${alert.name}", newValue: ""],
                       [propertyName: "Justification", oldValue: "", newValue: justification]] as List)
        }

        def attr = [alert: alert.name, alertType: alertType, signal: signal.name]

        if (alert.validatedSignals.size() > 1) {
            if (alert.validatedSignals.contains(signal)) {

                alert.validatedSignals = alert.validatedSignals - signal
                CRUDService.update(alert)

            }
            if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
                attribute = [product: getNameFieldFromJson(alert.alertConfiguration.productSelection), event: getNameFieldFromJson(alert.alertConfiguration.eventSelection)]
                activityService.createActivity(alert.executedAlertConfiguration, activityType, userService.getUser(), details, justification, attribute, alert.productName, alert.pt, alert.assignedTo, null, alert.assignedToGroup)

            } else if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
                attribute = [product: getNameFieldFromJson(alert.alertConfiguration.productSelection), event: getNameFieldFromJson(alert.alertConfiguration.eventSelection)]
                activityService.createActivity(alert.executedAlertConfiguration, activityType, userService.getUser(), details, justification, attribute, alert.productName, alert.pt, alert.assignedTo, alert.caseNumber, alert.assignedToGroup)

            } else if (Constants.AlertConfigType.EVDAS_ALERT == alertType) {
                attribute = [product: getNameFieldFromJson(alert.alertConfiguration.productSelection), event: getNameFieldFromJson(alert.alertConfiguration.eventSelection)]
                activityService.createEvdasActivity(alert.executedAlertConfiguration, activityType, userService.getUser(), details, justification, attribute, alert.productName, alert.pt, alert.assignedTo, null, alert.assignedToGroup)

            } else if (Constants.AlertConfigType.AD_HOC_ALERT == alertType) {
                attribute = [product: getNameFieldFromJson(alert.productSelection), event: getNameFieldFromJson(alert.eventSelection)]
                activityService.create(alert, activityType, userService.getUser(), details, justification, attribute)

            } else if (Constants.AlertConfigType.LITERATURE_SEARCH_ALERT == alertType) {
                attribute = [product: getNameFieldFromJson(alert.litSearchConfig.productSelection), event: getNameFieldFromJson(alert.litSearchConfig.eventSelection)]
                activityService.createLiteratureActivity(alert.exLitSearchConfig, activityType, userService.getUser(), details, justification, attribute, attribute.product, attribute.event, alert.assignedTo, alert.articleId, alert.assignedToGroup, alert.searchString)
            }

        } else {
            Disposition targetDisposition

            if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == alertType) {
                targetDisposition = Disposition.get(userService.getUser().workflowGroup.defaultQuantDisposition.id)
                aggregateCaseAlertService.dissociateAggregateCaseAlertFromSignal(alert, targetDisposition, justification, signal, isArchived)

            } else if (Constants.AlertConfigType.SINGLE_CASE_ALERT == alertType) {
                // set alert disposition to initial disposition and create history for single case alert
                targetDisposition = Disposition.get(userService.getUser().workflowGroup.defaultQualiDisposition.id)
                singleCaseAlertService.dissociateSingleCaseAlertFromSignal(alert, targetDisposition, justification, signal, isArchived)
            } else if (Constants.AlertConfigType.EVDAS_ALERT == alertType) {
                // set alert disposition to initial disposition and create History for Evdas Alert
                targetDisposition = Disposition.get(userService.getUser().workflowGroup.defaultEvdasDisposition.id)
                evdasAlertService.dissociateEvdasAlertFromSignal(alert, targetDisposition, justification, signal, isArchived)
            } else if (Constants.AlertConfigType.LITERATURE_SEARCH_ALERT == alertType) {
                // set alert disposition to initial disposition and create History for Literature Alert
                targetDisposition = Disposition.get(userService.getUser().workflowGroup.defaultLitDisposition.id)
                literatureAlertService.dissociateLiteratureAlertFromSignal(alert, targetDisposition, justification, signal, isArchived)
            } else {
                // for other alerts
                targetDisposition = Disposition.get(userService.getUser().workflowGroup.defaultAdhocDisposition.id)
                adHocAlertService.dissociateAdhocAlertFromSignal(alert, targetDisposition, justification, signal)
            }

        }

        activityService.createActivityForSignal(signal, justification, details, activityType,
                signal.assignedTo, userService.getUser(), attr, signal.assignedToGroup, true)

    }


    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def saveSummaryReportPreferenceForUser(ValidatedSignal validatedSignal) {
        validatedSignal.signalSummaryReportPreference = params.preference
        validatedSignal.save(flush: true)
        render(['success': true as Boolean])
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def graphReport(String dataSource, ValidatedSignal validatedSignal, String chartType, String productSelection, String eventSelection, Boolean allChartGenerate) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)

        SignalChartsDTO signalChartsDTO = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)

        Map result = [:]
        switch (chartType) {
            case Constants.SignalCharts.AGE_GROUP :
                result.put(Constants.SignalCharts.AGE_GROUP, validatedSignalChartService.fetchDataForDistributionByAgeOverTime(signalChartsDTO))
                break
            case Constants.SignalCharts.SERIOUSNESS:
                result.put(Constants.SignalCharts.SERIOUSNESS, validatedSignalChartService.fetchDataForDistributionBySeriousnessOverTime(signalChartsDTO))
                break
            case Constants.SignalCharts.COUNTRY:
                result.put(Constants.SignalCharts.COUNTRY, validatedSignalChartService.fetchDataForDistributionByCountryOverTime(signalChartsDTO))
                break
            case Constants.SignalCharts.GENDER:
                result.put(Constants.SignalCharts.GENDER, validatedSignalChartService.fetchDataForDistributionByGenderOverTime(signalChartsDTO))
                break
            case Constants.SignalCharts.OUTCOME:
                result.put(Constants.SignalCharts.OUTCOME, validatedSignalChartService.fetchDataForDistributionByOutcome(signalChartsDTO))
                break
            case Constants.SignalCharts.SERIOUS_PIE_CHART:
                result.put(Constants.SignalCharts.SERIOUS_PIE_CHART, validatedSignalChartService.fetchDataForDistributionBySourceOverTime(signalChartsDTO))
                break
            case Constants.SignalCharts.HEAT_MAP:
                result.put('systemOrganClass', validatedSignalChartService.fetchDataForDistributionBySystemOrganClass(signalChartsDTO))
                break
            case Constants.SignalCharts.ASSESSMENT_DETAILS:
                signalChartsDTO.assessmentDetail = validatedSignalService.generateAssessmentDataMap(signalChartsDTO)
                result.put('assessmentDetailView', groovyPageRenderer.render(template: '/validatedSignal/includes/assessmentDetails', model: [assessmentDetails: signalChartsDTO.assessmentDetail]))
                break
            default:
                signalChartsDTO.assessmentDetail = validatedSignalService.generateAssessmentDataMap(signalChartsDTO)
                SignalChartsDTO signalChartsDTOForAgeGroup = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)
                SignalChartsDTO signalsChartsDTOForSeriousness = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)
                SignalChartsDTO signalChartsDTOForCountry = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)
                SignalChartsDTO signalChartsDTOForGender = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)
                SignalChartsDTO signalChartsDTOForOutcome = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)
                SignalChartsDTO signalChartsDTOForSeriousCountPie = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)
                SignalChartsDTO signalChartsDTOForSystemOrganClass = prepareSignalChartsDTO(validatedSignal, productSelection, eventSelection,params)

                List<SignalChartsDTO> signalChartsDTOs = [signalChartsDTOForAgeGroup, signalsChartsDTOForSeriousness, signalChartsDTOForCountry, signalChartsDTOForGender, signalChartsDTOForOutcome, signalChartsDTOForSeriousCountPie, signalChartsDTOForSystemOrganClass]
                List retValue = validatedSignalChartService.fetchChartsData(signalChartsDTOs)

                result.put(Constants.SignalCharts.AGE_GROUP, retValue[0])
                result.put(Constants.SignalCharts.SERIOUSNESS, retValue[1])
                result.put(Constants.SignalCharts.COUNTRY, retValue[2])
                result.put(Constants.SignalCharts.GENDER, retValue[3])
                result.put(Constants.SignalCharts.OUTCOME, retValue[4])
                result.put(Constants.SignalCharts.SERIOUS_PIE_CHART, retValue[5])
                result.put(Constants.SignalCharts.SYSTEM_ORGAN_CLASS, retValue[6])
                result.put('assessmentDetailView', groovyPageRenderer.render(template: '/validatedSignal/includes/assessmentDetails', model: [assessmentDetails: signalChartsDTO.assessmentDetail]))

                break
        }
        responseDTO.data = result
        render(responseDTO as JSON)
    }

    SignalChartsDTO prepareSignalChartsDTO(ValidatedSignal validatedSignal, String productSelection, String eventSelection, Map params) {
        SignalAssessmentDateRangeEnum dateRange = params.dateRange as SignalAssessmentDateRangeEnum
        List<String> dateRangeList = []
        Map assessmentDetail = [:]
        String caseList = null
        String timeZone = Holders.config.server.timezone

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
                List<SingleCaseAlert> singleCaseAlertList = validatedSignalService.fetchAllSCAForSignalId(validatedSignal.id)
                List<String> caseNumberList = singleCaseAlertList.collect{it.caseNumber}
                caseList = validatedSignalChartService.mapCaseNumberFormatForProc(caseNumberList)
                dateRangeList = validatedSignalChartService.fetchDateRangeFromCaseAlerts(singleCaseAlertList)
                break
            case SignalAssessmentDateRangeEnum.LAST_1_YEAR:
            default:
                dateRangeList = RelativeDateConverter.lastXYears(null, 1, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
        }
        SignalChartsDTO signalChartsDTO = new SignalChartsDTO()
        if(dateRange==SignalAssessmentDateRangeEnum.SIGNAL_DATA){
            signalChartsDTO.productSelection = params.productSelection ?: validatedSignal.products
            signalChartsDTO.eventSelection = params.eventSelection ?: validatedSignal.events
            if(params.productGroupSelection){
                signalChartsDTO.productGroupSelection = params.productGroupSelection ?: validatedSignal.productGroupSelection
            }
            if(params.eventGroupSelection){
                signalChartsDTO.eventGroupSelection = params.eventGroupSelection ?: validatedSignal.eventGroupSelection
            }
        }
        else {
            signalChartsDTO.productSelection = params.productSelection
            signalChartsDTO.eventSelection = params.eventSelection
            if(params.productGroupSelection){
                signalChartsDTO.productGroupSelection = params.productGroupSelection
            }
            if(params.eventGroupSelection){
                signalChartsDTO.eventGroupSelection = params.eventGroupSelection
            }
        }
        signalChartsDTO.dateRange = dateRangeList
        signalChartsDTO.groupingCode = dateRange.groupingCode
        signalChartsDTO.signalId = validatedSignal?.id
        signalChartsDTO.isMultiIngredient = Boolean.parseBoolean(params.isMultiIngredient)
        signalChartsDTO.caseList = caseList
        return signalChartsDTO
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchAssessmentNotes(ValidatedSignal validatedSignal) {
        Sql sql = new Sql(signalDataSourceService?.getReportConnection("dataSource"))
        render([comment: clobUtilInstance.fetchClob(validatedSignal,'genericComment',sql,mappingContext)] as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def saveAssessmentNotes(ValidatedSignal validatedSignal, String comment) {
        validatedSignal.genericComment = comment
        validatedSignal.save(flush: true)
        activityService.createActivityForSignal(validatedSignal, '', "Assessment Note Added: $comment", ActivityType.findOrCreateByValue(ActivityTypeValue.AssessmentNoteAdded), validatedSignal.assignedTo,
                userService.getUser(), [comment: comment, validatedSignal: validatedSignal], validatedSignal.assignedToGroup)
        render([success: true] as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchSignalStatus() {
        ValidatedSignal validatedSignal = ValidatedSignal.findById(params.id)
        def adhocList = validatedSignal?.adhocAlerts?.sort { it.dateCreated }
        def adhocAlert = adhocList ? adhocList[0] : null
        def signalStatus = [aggReportStartDate  : adhocAlert?.aggReportStartDate ? toDateStringPattern(adhocAlert.aggReportStartDate, 'dd-MMM-yyyy') : '',
                            aggReportEndDate    : adhocAlert?.aggReportEndDate ? toDateStringPattern(adhocAlert.aggReportEndDate, 'dd-MMM-yyyy') : '',
                            lastDecisionDate    : adhocAlert?.lastDecisionDate ? toDateStringPattern(adhocAlert.lastDecisionDate, 'dd-MMM-yyyy') : '',
                            haSignalStatus      : adhocAlert?.haSignalStatus?.displayName ?: '',
                            haDateClosed        : adhocAlert?.haDateClosed ? toDateStringPattern(adhocAlert.haDateClosed, 'dd-MMM-yyyy') : '',
                            actionTaken         : adhocAlert?.actionTaken ?: '',
                            commentsSignalStatus: adhocAlert?.commentSignalStatus ?: ''
        ]
        render signalStatus as JSON
    }

    def fetchLinkedConfiguration() {
        def result = validatedSignalService.composeLinkedConfigurationList(params.id)
        render(result as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def changeDisposition(String selectedRows, Disposition targetDisposition, String justification, String incomingDisposition) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        def result
        try {
            result=validatedSignalService.changeDisposition(selectedRows, targetDisposition, justification,incomingDisposition)
            if(result.isDispositionChangeAllowed){
                responseDTO.data=[dueDate:result.dueDate,dueIn:result.dueIn,isValidationDateAdded:result.isValidatedDateFlag]
            }else{
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.disposition.change.error.refresh")
            }
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessageList(vx)[0]
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def revertDisposition(Long id, String justification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        responseDTO.data = [:]
        def result
        try {
            result = validatedSignalService.revertDisposition(id, justification)
            responseDTO.data << [incomingDisposition: result.prevDisposition, targetDisposition: result.currentDisposition, alertDueDateList: result.alertDueDateList, dueDate:result.dueDate,dueIn:result.dueIn]
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.undo.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    /**
     * Checks whether a notification popup should be displayed.
     *
     * This method determines if there are any changes in the signal status history
     * details or if a new entry is created. The popup will only be displayed if there
     * are changes or a new entry is detected.
     *
     * @param signalStatusHistory the current signal status history record to compare against
     * @param params the new parameters containing potential updates
     * @return true if there are changes or a new entry is created, false otherwise
     */
    Boolean checkNotificationStatus(SignalStatusHistory signalStatusHistory, Map params) {
        String signalHistoryFromSignalStatus = signalStatusHistory?.fromSignalStatus ?: ""
        String paramsFromSignalStatus = params.fromSignalStatus ?: ""
        String signalHistoryStatusComment = signalStatusHistory?.statusComment ?: ""
        String paramsStatusComment = params.statusComment ?: ""

        // The popup should only display if there are changes in the details.
        if (signalStatusHistory?.signalStatus != params.signalStatus
                || signalHistoryFromSignalStatus != paramsFromSignalStatus
                || signalHistoryStatusComment != paramsStatusComment
                || params.createdDate != DateUtil.fromDateToString(signalStatusHistory?.dateCreated, DEFAULT_DATE_FORMAT)) {
            return true
        }
        return false
    }

    /**
     * Saves the signal status history.
     * @return renders a JSON response containing the status, code, message, value,
     *         data, and a flag indicating whether to display a notification
     */
    @Secured(['ROLE_SIGNAL_MANAGEMENT_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_REVIEWER'])
    def saveSignalStatusHistory() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        Boolean notificationDisplayFlag = true
        try {
            SignalStatusHistory signalStatusHistory = params.signalHistoryId ? SignalStatusHistory.get(params.signalHistoryId as Long) : null
            if (signalStatusHistory) {
                notificationDisplayFlag = checkNotificationStatus(signalStatusHistory, params)
            }
            String previousDate1 = DateUtil.fromDateToString(signalStatusHistory?.dateCreated, DEFAULT_DATE_FORMAT)
            ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId)
            String previousDueDate = DateUtil.fromDateToString(validatedSignal.actualDueDate, DEFAULT_DATE_FORMAT)
            Integer dueIn = validatedSignalService.saveSignalStatusHistory(params, true)
            if (dueIn != null && SystemConfig.first().displayDueIn) {
                validatedSignalService.saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                                                signalId    : validatedSignal.id, "createdDate": previousDueDate, "previousDate1": previousDate1], true)
            }
            responseDTO.value = dueIn
            responseDTO.data = generateSignalHistoryHtml(params.long('signalId'), params.enableSignalWorkflow?.toBoolean())
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.signal.history.save.error")
        }
        Map responseMap = [
                code                   : responseDTO.code,
                status                 : responseDTO.status,
                message                : responseDTO.message,
                value                  : responseDTO.value,
                data                   : responseDTO.data,
                notificationDisplayFlag: notificationDisplayFlag
        ]
        render(responseMap as JSON)
    }

    def refreshSignalHistory(Long signalId) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            responseDTO.data = generateSignalHistoryHtml(signalId,params.enableSignalWorkflow?.toBoolean())
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.signal.history.refresh.error")
        }
        render(responseDTO as JSON)
    }

    String generateSignalHistoryHtml(Long signalId,Boolean enableSignalWorkflow){
        String signalHistoryHtml
        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
        List<Map> signalHistoryList = validatedSignalService.generateSignalHistory(validatedSignal)
        List<String> signalStatusList = alertAttributesService.get('signalHistoryStatus') as List<String>
        if(SystemConfig.first().displayDueIn)
        {
            signalStatusList.add(Constants.WorkFlowLog.DUE_DATE)
        }
        String currentUserFullName = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        String timezone = userService.getUser()?.preference?.timeZone
        if(enableSignalWorkflow) {
            signalHistoryHtml = g.render(template: '/validatedSignal/includes/signal_history', model: [signalHistoryList: signalHistoryList, currentUserFullName: currentUserFullName,
                                                                                                              signalStatusList : signalStatusList, timezone: timezone])
        } else {
            signalHistoryHtml = g.render(template: '/validatedSignal/includes/signal_history_disable_workflow', model: [signalHistoryList: signalHistoryList, currentUserFullName: currentUserFullName,
                                                                                                       signalStatusList : signalStatusList, timezone: timezone])
        }
        signalHistoryHtml
    }

    def fetchAttachments(final Long alertId) {
        List attachments = validatedSignalService.fetchAttachments(alertId)
        respond attachments, [formats: ['json']]
    }

    Boolean isSignalAccessible(Long id){
        Boolean isSignalAccessible = false
        if(id){
            isSignalAccessible = validatedSignalService.checkAccessibility(id)
        }
        render([success: isSignalAccessible] as JSON)
    }

    def generateSpotfireReportForSignal(){
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        List SCAList
        if(params.dateRange as SignalAssessmentDateRangeEnum == SignalAssessmentDateRangeEnum.SIGNAL_DATA){
            User currentUser = userService.getUser()
            Group group = currentUser.workflowGroup
            SCAList = validatedSignalService.fetchAllSCAForSignalId(params.signalId as Long, group)
        }
        if(params.dateRange as SignalAssessmentDateRangeEnum != SignalAssessmentDateRangeEnum.SIGNAL_DATA || SCAList.size()>=1) {
            try {
                validatedSignalService.generateSpotfireReportForSignal(params, SCAList)
            } catch (grails.validation.ValidationException vx) {
                responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
                responseDTO.status = false
                log.error("Exception is : ${vx}")
            } catch (Exception e) {
                log.error(e.printStackTrace())
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.signal.spotfire.error")
            }
        } else {
            responseDTO.code = 400
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.signal.spotfire.no.signal.data")
        }
        render(responseDTO as JSON)
    }

    def fetchAnalysisData() {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List<Map> spotfireNotificationQuery = SpotfireNotificationQuery.findAllByExecutedConfigurationIdAndIsEnabledAndStatus(params.signalId as Long, true, Constants.SpotfireStatus.FINISHED) collect {
            List<String> analysisParameters = it.signalParameters.split('@@@')
            [fileName   : it.fileName, product: analysisParameters[0], event: analysisParameters[1], dateRange: analysisParameters[2],
             fileUrl    : spotfireService.spotfireFileUrl(it.fileName), generatedBy: analysisParameters[3],
             generatedOn: new Date(DateUtil.toDateStringWithTime(it.created, userTimezone)).format(DateUtil.DATEPICKER_FORMAT_AM_PM).toString()]
        }
        render([aaData: spotfireNotificationQuery, recordsTotal: spotfireNotificationQuery.size()] as JSON)
    }

    def saveSignalRMMs() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId)
            if (validatedSignal && !params.signalRmmId) {
                SignalRMMs signalRMMs = validatedSignalService.saveSignalRMMs(params, validatedSignal, request)
                validatedSignalService.saveActivityForSavingRMMs(signalRMMs, validatedSignal)
            } else if (params.signalRmmId) {
                SignalRMMs signalRMMs = SignalRMMs.get(params.signalRmmId)
                validatedSignalService.saveActivityForUpdatingRMMs(params, signalRMMs, validatedSignal)
                validatedSignalService.updateSignalRMMs(signalRMMs, params, validatedSignal, request)
            }
        }
        catch (FileFormatException fileFormatException) {
            log.error("File Format is not valid", fileFormatException.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code:"signal.rmms.label.fileformat.error.save")
        }
        catch (grails.validation.ValidationException vx) {
            if (vx.toString().contains("emailAddress.email.error")){
                responseDTO.message = messageSource.getMessage("signal.rmms.label.email.error.save", null, Locale.default)
        }
        else {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
        }
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }
        catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            if (e.toString().contains("emailAddress.email.error"))
                responseDTO.message = messageSource.getMessage("signal.rmms.label.email.error.save", null, Locale.default)
            else
                responseDTO.message = message(code: "signal.rmms.label.error.save")
        }
        render(responseDTO as JSON)
    }


    private bindCommunicationFiles(def signalRMMs, def file, String fileName, def ext) {
        try {
            User currentUser = userService.getUser()
            attachmentableService.doAddAttachment(grailsApplication.config,currentUser,signalRMMs,file,null,null,null,fileName,ext)
            String fileDescription = params.description
            List<Attachment> attachments = signalRMMs.getAttachments().sort { it.dateCreated }
            if (attachments) {
                attachments = attachments[-1..-1]
                attachments.each { attachment ->
                    AttachmentDescription attachmentDescription = new AttachmentDescription()
                    attachmentDescription.attachment = attachment
                    attachmentDescription.createdBy = userService.getUser().getFullName()
                    attachmentDescription.description = fileDescription
                    attachmentDescription.save(flush: true)
                }
            }
            } catch (FileFormatException e) {
        }
    }

    private bindReferencesForSignalEmailLog(SignalEmailLog signalEmailLog, referenceName = null, inputName = null) {
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
                attachmentableService.doAddReference(currentUser, signalEmailLog, referenceLink,params.inputName)
                List<Attachment> attachments = validatedSignal.getReferences()
                if (attachments) {
                    AttachmentDescription attachmentDescription = new AttachmentDescription()
                    attachmentDescription.attachment = attachments.first()
                    attachmentDescription.createdBy = currentUser.getFullName()
                    attachmentDescription.description = params.description
                    attachmentDescription.save(flush: true)
                }

            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    CommonsMultipartFile fileAttachments(def signalRMMs){
        InputStream input = null
        OutputStream os = null
        List<Map> attachmentCommunication = signalRMMs.attachments.collect {
            [name: it.inputName ?: it.fileName, file: AttachmentableUtil.getFile(grailsApplication.config, it)]
        }
        File file = attachmentCommunication.file[0]
        file.setExecutable(false)
        String contentType = URLConnection.guessContentTypeFromName(attachmentCommunication.name[0])
        FileItem fileItem = new DiskFileItem("attachments", contentType, false, file.getName(), (int) file.length(), file.getParentFile());
        input = new FileInputStream(file);
        os = fileItem.getOutputStream();
        IOUtils.copy(input, os);
        CommonsMultipartFile multipartFile = new CommonsMultipartFile(fileItem)
        return multipartFile
    }

    def sendEmailForRmms() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId)
        List<Map> attachments = []
        List<Map> references = []
        //Added for PVS-63384
        List<MultipartFile>  filesToUpload = []
        if (request instanceof MultipartHttpServletRequest) {
            request.multipartFiles.each { k, v ->
                if (v instanceof List) {
                    v.each { MultipartFile file ->
                        if(!TextUtils.isEmpty(file?.originalFilename))
                        attachmentableService.checkExtension(file?.originalFilename)
                        filesToUpload << file
                    }
                }  else {
                    filesToUpload << v
                }
            }
        }
        String typeOfCommunication
        if (validatedSignal) {
            SignalRMMs signalRMMs = SignalRMMs.get(params.signalRmmId)
            typeOfCommunication = signalRMMs.communicationType
            if(typeOfCommunication.equals('rmmType')) {
                SignalRMMs signalRMMsCommunication = new SignalRMMs(signalRMMs.properties)
                signalRMMsCommunication.communicationType = 'communication'
                signalRMMsCommunication.communicationRmmId = params.signalRmmId ? params.signalRmmId as Long : null
                signalRMMsCommunication.emailSent = new Date()
                signalRMMsCommunication.isSystemUser = true
                signalRMMsCommunication.signalEmailLog = new SignalEmailLog(assignedTo: params.sentTo, subject: params.subject, body: params.body)
                signalRMMsCommunication.emailAddress = Arrays.toString(params.sentTo).join(',')
                signalRMMsCommunication.dateCreated = new Date()
                validatedSignal.addToSignalRMMs(signalRMMsCommunication)
                validatedSignalService.saveActivityForCommunicationAddedFromEmail(signalRMMs, validatedSignal, true)
                if (signalRMMs.attachments) {
                    List<Map> attachmentCommunication = signalRMMs.attachments.collect {
                        [name: it.inputName ?: it.fileName, file: AttachmentableUtil.getFile(grailsApplication.config, it),ext: it.ext]
                    }
                    bindCommunicationFiles(signalRMMsCommunication, fileAttachments(signalRMMs), attachmentCommunication.name[0], attachmentCommunication.ext[0])
                    bindCommunicationFiles(signalRMMsCommunication.signalEmailLog, fileAttachments(signalRMMs), attachmentCommunication.name[0], attachmentCommunication.ext[0])
                    validatedSignalService.bindFiles(signalRMMsCommunication.signalEmailLog, [:], filesToUpload)
                } else {
                    List<Map> referenceLink = signalRMMs.getReferences().collect {
                        [referenceName: it.referenceLink, inputName: it.inputName]
                    }
                    validatedSignalService.bindReferences(signalRMMsCommunication, [:], validatedSignal, referenceLink.referenceName[0], referenceLink.inputName[0])
                    bindReferencesForSignalEmailLog(signalRMMsCommunication.signalEmailLog, referenceLink.referenceName[0], referenceLink.inputName[0])
                    validatedSignalService.bindFiles(signalRMMsCommunication.signalEmailLog, [:], filesToUpload, "rmmReferenceFile")
                }
                attachments = signalRMMsCommunication.signalEmailLog.attachments.collect {
                    [name: it.inputName ?: it.filename, file: AttachmentableUtil.getFile(grailsApplication.config, it)]
                }
                references = signalRMMsCommunication.signalEmailLog.getReferences().collect {
                    [referenceName: it.referenceLink, inputName: it.inputName]
                }
            } else {
                signalRMMs.signalEmailLog = new SignalEmailLog(assignedTo: params.sentTo, subject: params.subject, body: params.body)
                signalRMMs.emailSent = new Date()
                List<Map> referenceLink = signalRMMs.getReferences().collect {
                    [referenceName: it.referenceLink, inputName: it.inputName]
                }
                signalRMMs.isDeleted = false
                signalRMMs.signalId = validatedSignal.id as Long
                signalRMMs.save(flush : true)
                bindReferencesForSignalEmailLog(signalRMMs.signalEmailLog, referenceLink.referenceName[0], referenceLink.inputName[0])
                validatedSignalService.bindFiles(signalRMMs.signalEmailLog, [:], filesToUpload, "rmmReferenceFile") // File attachment from email modal
                if(signalRMMs.attachments){
                    List<Map> attachmentCommunication = signalRMMs.attachments.collect {
                        [name: it.inputName ?: it.fileName, file: AttachmentableUtil.getFile(grailsApplication.config, it), contentType: it.contentType, ext: it.ext]
                    }
                    bindCommunicationFiles(signalRMMs.signalEmailLog, fileAttachments(signalRMMs), attachmentCommunication.name[0],attachmentCommunication.ext[0])
                }
                attachments = signalRMMs.signalEmailLog.attachments.collect {
                    [name: it.inputName ?: it.filename, file: AttachmentableUtil.getFile(grailsApplication.config, it)]
                }
                references = signalRMMs.signalEmailLog.getReferences().collect {
                    [referenceName: it.referenceLink, inputName: it.inputName]
                }
            }

            emailService.sendCommunicationEmail([
                    'toAddress'        : [params.sentTo],
                    'title'            : params.subject,
                    'map'              : ["emailMessage": params.body, "attachments": attachments, "references": references],
                    'allowNotification': true
            ])
            List fileAndReference=[]
            fileAndReference += attachments.collect {
                it.name
            }

            createAuditForMailSentRmms(params,signalRMMs,fileAndReference)
            String resp
            String fileName
            if(signalRMMs.assignedToId){
                resp = signalRMMs.assignedTo.name
            } else if(signalRMMs.assignedToGroupId){
                resp = signalRMMs.assignedToGroup.name
            } else {
                resp = signalRMMs.emailAddress
            }

            if(signalRMMs.attachments){
                fileName = signalRMMs.attachments.collect { it.inputName ?: it.fileName }.join(',')
            } else {
                fileName = signalRMMs.getReferences().collect { it.inputName ?: it.referenceLink }.join(',')
            }

            String detailsMessage = "Communication sent with Type='${signalRMMs.type ?: ''}', Country = ${validatedSignalService.country(signalRMMs.country)}, Description='${signalRMMs.description?:''}', FileName= '${fileName}', Resp.='${resp?:''}', Status='${signalRMMs.status?:''}', " +
                    "DueDate='${signalRMMs.dueDate ? DateUtil.toDateStringWithoutTimezone(signalRMMs.dueDate) : ''}', EmailSent='${DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, userService.user.preference.timeZone)}'"
            activityService.createActivityForSignal(validatedSignal, '',detailsMessage, ActivityType.findByValue(ActivityTypeValue.CommunicationSent), validatedSignal.assignedTo,
                    userService.getUser(), [signal: validatedSignal.name], validatedSignal.assignedToGroup)
        }
        render(responseDTO as JSON)
    }

    def createAuditForMailSentRmms(def params,SignalRMMs signalRMMs,List attachments){
        List<Map> auditChildMap = []
        def childEntry = [:]
        childEntry = [
                propertyName: "To",
                newValue    : params.sentTo]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Subject",
                newValue    : params.subject]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Body",
                newValue    : params.body?.replaceAll(/<[^>]*>/, '')]
        auditChildMap << childEntry
        if(attachments.size()>0){
            childEntry = [
                    propertyName: "Attachments",
                    newValue    : attachments.toString()
            ]
            auditChildMap << childEntry
        }
        signalAuditLogService.createAuditLog([
                entityName : "signalRmms",
                moduleName : "Signal: Email",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: signalRMMs?.getInstanceIdentifierForAuditLog() + "- Email Sent",
                description: "Email Sent",
                username   : userService.getUser()?.username,
                fullname   : userService.getUser()?.fullName
        ] as Map, auditChildMap,true)
    }

    def fetchRmms() {
        ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId)
        Integer offset = params.start  as Integer
        Integer max = params.length  as Integer
        List finalList =[]
        Integer count = 0
        String timezone = userService.user.preference.timeZone
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        List a = validatedSignal.signalRMMs.findAll {
            it.communicationType == params.type
        }.findAll{
            if(params.type == Constants.AlertConfigType.COMMUNICATION)
                (it.isDeleted == null) ||  (it.isDeleted == false);
            else true;
        }.sort { a, b -> b.dateCreated <=> a.dateCreated }.collect {
            String color
            if(it.dueDate){
                int difference = DateUtils.truncate(it.dueDate, Calendar.DAY_OF_MONTH)- DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH)
                if(difference < 0) {
                    color = "red"
                } else if(difference >= 0 && difference <=2 ) {
                    color = "orange"
                }
            }
            String assignedToFullName
            if(it.assignedToId){
                assignedToFullName = it.assignedTo.name
            } else if(it.assignedToGroupId){
                assignedToFullName = it.assignedToGroup.name
            } else {
                assignedToFullName = it.emailAddress
            }
            [type                : it.type, description: it.description, assignedToFullName: assignedToFullName ?: "",
             assignedToId        : it.assignedToId ?: it.assignedToGroupId, emailAddress: it.emailAddress, id: it.id,
             status              : it.status, dueDate: it.dueDate ? DateUtil.toDateStringWithoutTimezone(it.dueDate) : '', index: count++,
             country             : it.country, emailLog: it.signalEmailLog, emailAttachmentName: it.signalEmailLog?.attachments.collect { it.inputName ?: it.filename }?.join(','),
             emailReferenceLink  : it.signalEmailLog?.getReferences().collect { it.inputName ?: it.referenceLink }?.join(','),
             isEmailReferenceLink: it.signalEmailLog?.getReferences().collect { it.referenceLink }?.join(','),
             fileName            : it.attachments.collect { it.inputName != 'attachments' ? it.inputName : it.name }?.join(','),
             referenceLink       : it.getReferences().collect { it.inputName ?: it.referenceLink }?.join(','),
             isReferenceLinkUrl  : it.getReferences().collect { it.referenceLink }?.join(','),
             attachmentId        : it.attachments.collect { it.id }?.join(','),
             emailAttachmentId   : it.signalEmailLog?.attachments.collect { it.id }?.join(','),
             referenceId         : it.getReferences().collect { it.id }?.join(','),
             isAssignedTo        : it.assignedToId != null, emailSent: it.emailSent ? DateUtil.stringFromDate(it.emailSent, DateUtil.DATEPICKER_FORMAT_AM_PM, timezone): '',
             colorDueDate        : color,
             dueDateSort         : it.dueDate,
             communicationRmmId  : it.communicationRmmId
            ]
        }
        if (orderColumn != null) {
            a?.sort { Map config1, Map config2 ->
                String sortedColumn = orderColumnMap?.name
                def val1
                def val2
                if(sortedColumn == "dueDate"){
                    val1 = StringToDate(config1[sortedColumn] as String, DATEPICKER_FORMAT)
                    val2 = StringToDate(config2[sortedColumn] as String, DATEPICKER_FORMAT)
                }else{
                    val1 = config1[sortedColumn]?.toLowerCase()
                    val2 = config2[sortedColumn]?.toLowerCase()
                }

                if(sortedColumn == "fileName"){
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return (val2?val2.toLowerCase():config2['referenceLink'].toLowerCase())<=> (val1?val1.toLowerCase():config1['referenceLink'].toLowerCase())
                    } else {
                        return (val1?val1.toLowerCase():config1['referenceLink'].toLowerCase()) <=> (val2?val2.toLowerCase():config2['referenceLink'].toLowerCase())
                    }
                } else if (sortedColumn in ["type", "country", "status","description"]) {
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
        if(a?.size() > 0){
            finalList = (max >= 0 ) ? a?.subList(offset, Math.min(offset + max, a?.size())): a
        }
        render([aaData: finalList, recordsTotal: a?.size(), recordsFiltered: a?.size()] as JSON)
    }

    def deleteSignalRMMs() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            ValidatedSignal validatedSignal = ValidatedSignal.get(params.signalId)
            if (validatedSignal && params.signalRmmId) {
                SignalRMMs signalRMMs = SignalRMMs.get(params.signalRmmId)
                validatedSignalService.saveActivityForDeletingRMMs(signalRMMs, validatedSignal)
                validatedSignal.removeFromSignalRMMs(signalRMMs)
                signalRMMs.isDeleted=true
                signalRMMs.save(flush:true)
                CRUDService.update(validatedSignal)
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "signal.rmms.label.error.delete")
        }
        render(responseDTO as JSON)
    }

    def saveWorkflowState() {
        User user = userService.getUser();
        Set<Role> roles=user.getAuthorities()
        List<String> editAllowedUser=  Holders.config.signal.allowedUser;
        boolean isAllowedUser=false;
        boolean  isEditDueDate=Holders.config.signal.editDueDate;
        for (Role role:roles){
            if(editAllowedUser.contains(role.getAuthority())){
                isAllowedUser=true;
                break;
            }
        }
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            Integer data = validatedSignalService.saveWorkflowState(params)
            responseDTO.data = ['dueIn': data,'dueDate':data!=null?new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(ValidatedSignal.get(params.signalId).actualDueDate):"" ]
            responseDTO.value=(true==isEditDueDate && isAllowedUser==true )?1:0;
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.signal.save.workflow.error")
        }
        render(responseDTO as JSON)
    }

    def saveAssignedTo(){
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try{
            validatedSignalService.changeAssignedTo(params)
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.signal.save.assigned.error")
        }
        render(responseDTO as JSON)
    }
/**
 * This method is responsible for hard deletion of validated signals from the system,
 * also deletes all the child records associated with the validated signal domain.
 * @param signalIdList : List of ids of signals for deletion.
 * @param justification : justification provided by the end user.
 * @return: returns information about signals deleted successfully and discarded signals
 */
    def deleteValidatedSignals() {

        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        String signalIdList = params.signalIdList as String
        List signalIds = signalIdList?.split(",")
        String justification = params.justification as String

        if (validateParams(params, responseDTO)) {
            Map data = [:]
            List<Map<String, Object>> discardedSignals = []

            signalIds.each { signalId ->
                Map validatedObservationsData = validatedSignalService.getAllValidatedObservationsData(Long.parseLong(signalId))
                Long validatedCaseCount = validatedObservationsData.get(Constants.SignalCounts.CASECOUNT)
                String signalDeletionMessage = validatedObservationsData.get("signalDeletionMessage")
                if (validatedCaseCount > 0) {
                    String signalName = ValidatedSignal.get(signalId).name
                    discardedSignals.add(["id": signalId, "signalName": signalName, "validatedCaseCounts": validatedCaseCount,"signalDeletionMessage":signalDeletionMessage])
                }
            }

            if (!discardedSignals.isEmpty()) {
                signalIds.removeAll(discardedSignals*.id)
                data.put(Constants.CommonUtils.PRE_REQUISITE_FAIL, discardedSignals)
            }

            if (!signalIds.isEmpty()) {
                signalIds = signalIds?.collect { it as Long }
                try {
                    justificationService.saveActionJustification(signalIds, Constants.AlertConfigType.VALIDATED_SIGNAL, ActionTypeEnum.SIGNAL_DELETED.value(), justification)
                    List<String> deletedSignalNames = ValidatedSignal.findAllByIdInList(signalIds)*.getEntityValueForDeletion()
                    validatedSignalService.deleteValidatedSignals(signalIds)
                    validatedSignalService.deleteSpotfireReportsForSignals(signalIds)
                    responseDTO.message = message(code: "app.label.signal.delete.success")
                    deletedSignalNames.each {
                        signalAuditLogService.createAuditLog([
                                entityName: "Signal",
                                moduleName: "Signal",
                                category: AuditTrail.Category.DELETE.toString(),
                                entityValue: "${it}",
                                username: userService.getUser().username,
                                fullname: userService.getUser().fullName
                        ] as Map, [[propertyName: "isDeleted", oldValue: "false", newValue: "true"]] as List)
                    }
                    data.put(Constants.CommonUtils.SUCCESS, deletedSignalNames)
                } catch (grails.validation.ValidationException vx) {
                    responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
                    responseDTO.status = false
                    log.error("Exception is : ${vx}")
                } catch (Exception e) {
                    log.error(e.printStackTrace())
                    responseDTO.status = false
                    responseDTO.code = 2
                    responseDTO.message = message(code: "app.label.signal.delete.error")
                }

            }

            responseDTO.data = data

        }

        render(responseDTO as JSON)
    }

    boolean validateParams(Map params, ResponseDTO responseDTO) {
        if (!params.justification) {
            responseDTO.status = false
            responseDTO.code = 1
            responseDTO.message = message(code: "app.label.blank.justification.warning")
        }
        if (!params.signalIdList || params.signalIdList == "") {
            responseDTO.status = false
            responseDTO.code = 1
            responseDTO.message += "Signal Id list can't be blank"
        }
        return responseDTO.status
    }

/**
 * This method disassociates all the signals linked with cases/PECs of configuration
 * @param configId : configuration id
 * @param alertType : can be Single case alert, Aggregate case alert, Evdas alert or Literature alert.
 * @param justification : justification provided by the end user.
 * @param deleteLatest : if true, disassociates signals linked with latest version
 */
    def disassociateSignalsFromExConfig() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        Long configId = params.configId as Long
        String alertType = params.alertType as String
        String justification = params.justification as String
        Boolean deleteLatest = params.deleteLatest == "false" ? false : true
        if (!justification) {
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.blank.justification.warning")
        } else {
            try {
                Map linkedSignalsDataMap = validatedSignalService.disassociateSignalsByAlertType(configId, alertType, deleteLatest, justification)
                List<Map> alertValidatedSignalList = linkedSignalsDataMap.get("alertData") as List<Map>
                List<Map> archivedAlertValidatedSignalList = linkedSignalsDataMap.get("archivedAlertData") as List<Map>
                justificationService.saveActionJustification([configId] as List<Long>, alertType, ActionTypeEnum.SIGNALS_DISASSOCIATED.value(), justification)
                alertValidatedSignalList?.each{ it ->
                    it.signalId?.each{ signalId->
                        disassociateAlertAndCreateActivity(signalId as Long, alertType, justification, it.id as Long, false)
                    }
                }
                archivedAlertValidatedSignalList?.each{ it ->
                    it.signalId?.each{ signalId->
                        disassociateAlertAndCreateActivity(signalId as Long, alertType, justification, it.id as Long, true)
                    }
                }
                responseDTO.message = "Successfully disassociated linked signal(s)"
            }catch (grails.validation.ValidationException vx) {
                responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
                responseDTO.status = false
                log.error("Exception is : ${vx}")
            }  catch (Exception e) {
                log.error(e.printStackTrace())
                responseDTO.status = false
                responseDTO.code = 2
                responseDTO.message = "Error occurred while disassociating linked signal(s)"
            }
        }

        render(responseDTO as JSON)
    }

/**
 * This method lists all the validated observations that are associated with signal.
 * @param signalId : Validated signal id
 * @return renders HTML page.
 */
    def validatedObservations(String signalId) {
        try {
            Map validatedObservationsData = validatedSignalService.getAllValidatedObservationsData(Long.parseLong(signalId))
            List scaData = validatedObservationsData.get(Constants.AlertConfigType.SINGLE_CASE_ALERT)
            List aggData = validatedObservationsData.get(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            List evdasData = validatedObservationsData.get(Constants.AlertConfigType.EVDAS_ALERT)
            List literatureData = validatedObservationsData.get(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
            Set adhocData = validatedObservationsData.get(Constants.AlertConfigType.AD_HOC_ALERT)
            Long validatedCaseCount = validatedObservationsData.get(Constants.SignalCounts.CASECOUNT)
            if(aggData?.size() > 0)
                aggData = aggData.unique {
                    [it.productId, it.ptCode, it.soc, it.alertConfigurationId, signalId]
                }
            render view: "validatedObservation", model: [signalId: signalId, "signalName": ValidatedSignal.get(signalId)?.name, totalInstancesCount: validatedCaseCount, scaData: scaData, aggData: aggData, evdasData: evdasData, literatureData: literatureData, adhocData: adhocData]
        } catch (Exception e) {
            log.error(e.printStackTrace())
            redirect(controller: "validatedSignal", action: "index")
            return
        }
    }

/**
 * This method disassociates all the validated observations of particular alert type from the signal.
 * @param signalId : Validated signal id
 * @param alertType : can be Single case alert, Aggregate case alert, Evdas alert or Literature alert.
 * @param justification : justification provided by the end user.
 */
    def disassociateAlertsBySignalId() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        Long signalId = params.signalId as Long
        String alertType = params.alertType as String
        String justification = params.justification as String
        if (!justification) {
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.blank.justification.warning")
        } else {
            try {
                if (alertType == Constants.AlertConfigType.AD_HOC_ALERT) {

                    Set adhocAlerts = validatedSignalService.getAdhocValidatedAlerts(signalId)
                    adhocAlerts.each {
                        disassociateAlertAndCreateActivity(signalId, alertType, justification, it.id, false)
                    }

                } else {

                    List alertDataList = validatedSignalService.getValidatedObservationsDataByAlertType(signalId, alertType, false)
                    List arcihvedAlertDataList = validatedSignalService.getValidatedObservationsDataByAlertType(signalId, alertType, true)

                    if (!alertDataList.isEmpty()) {
                        alertDataList.each {
                            disassociateAlertAndCreateActivity(signalId, alertType, justification, it.id, false)
                        }
                    }

                    if (!arcihvedAlertDataList.isEmpty()) {
                        arcihvedAlertDataList.each {
                            disassociateAlertAndCreateActivity(signalId, alertType, justification, it.id, true)
                        }
                    }
                }

                justificationService.saveActionJustification([signalId] as List<Long>, Constants.AlertConfigType.VALIDATED_SIGNAL, ActionTypeEnum.PEC_DISASSOCIATED.value(), justification)
                responseDTO.message = "Successfully disassociated linked case(s)"
            }catch (grails.validation.ValidationException vx) {
                responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
                responseDTO.status = false
                log.error("Exception is : ${vx}")
            }  catch (Exception e) {
                log.error(e.printStackTrace())
                responseDTO.status = false
                responseDTO.code = 2
                responseDTO.message = "Error occurred while disassociating linked case(s)"
            }
        }

        render(responseDTO as JSON)
    }

    def addCaseToSignal() {
        if (TextUtils.isBlank(params.caseNumber) && !params.file || (params.caseNumber != null && TextUtils.isBlank(params.justification)) || (params.file != null && TextUtils.isBlank(params.justification))) {
            render([success: false, message: message(code: "app.error.fill.all.required")] as JSON)
            return
        }
        try {
            List<String> caseNumber = []
            List singleCaseAlertList = []
            Long signalId = Long.valueOf(params.signalId)

            ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
            Configuration config = createConfigForNewCases(signalId)
            ExecutedConfiguration executedConfig = reportExecutorService.createExecutedConfiguration(config)
            Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfig, true, true, "pva")
            if (seriesId) {
                String updateCaseSeriesHql = alertService.prepareUpdateCaseSeriesHql(executedConfig.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE)
                ExecutedConfiguration.executeUpdate(updateCaseSeriesHql, [pvrCaseSeriesId: seriesId, id: executedConfig.id])
                executedConfig.pvrCaseSeriesId = seriesId
            }

            if (params.file) {
                List<String> caseNumberList = singleCaseAlertService.processExcelFile(params.file)
                if (caseNumberList) {
                    List successCaseNumberList = validatedSignalService.removeDuplicateCases(caseNumberList,validatedSignal)
                    if(successCaseNumberList.size() > 0) {
                        Map map = singleCaseAlertService.saveCaseSeriesInDB(successCaseNumberList, executedConfig, params.justification)
                        singleCaseAlertList = map.singleAlertCaseList
                        Map caseNumbersMap = map.caseNumbersMap

                        if (caseNumbersMap.validCaseNumber || caseNumbersMap.invalidCaseNumber) {
                            caseNumber = caseNumbersMap.validCaseNumber
                            if (singleCaseAlertList) {
                                singleCaseAlertService.createAlert(config, executedConfig, singleCaseAlertList, params.justification, true, true)
                                if (caseNumbersMap.invalidCaseNumber.size() > 0) {
                                    render([success: true, message: message(code: "singleCaseAlert.import.caseNumber.success", args: [caseNumberList.size(), singleCaseAlertList?.size(),
                                                                                                                                      caseNumbersMap.invalidCaseNumber.toString().replaceAll("[\\[\\]]", "")])] as JSON)
                                } else {
                                    render([success: true, message: message(code: "singleCaseAlert.import.caseNumber.successWithoutRejection", args: [caseNumberList.size(), singleCaseAlertList?.size()])] as JSON)
                                }
                            } else {
                                render([success: true, message: message(code: "singleCaseAlert.import.caseNumber.all.Rejected", args: [caseNumberList.size()])] as JSON)
                            }
                        }
                    } else {
                        render([success: true, message: message(code: "singleCaseAlert.import.caseNumber.all.Rejected", args: [caseNumberList.size()])] as JSON)
                    }
                } else {
                    render([success: false, message: message(code: "app.label.no.data.excel.error")] as JSON)
                }
            } else {
                if(!validatedSignalService.validateSingleCases(validatedSignal.id).contains(params.caseNumber)){
                    caseNumber.add(params.caseNumber)
                    Map map = singleCaseAlertService.saveCaseSeriesInDB(caseNumber as List<String>, executedConfig, params.justification)
                    singleCaseAlertList = map?.singleAlertCaseList
                    if (!singleCaseAlertList) {
                        render([success: false, message: message(code: "singleCaseAlert.invalid.caseNumber.data")] as JSON)
                    } else {
                        singleCaseAlertService.createAlert(config, executedConfig, singleCaseAlertList, params.justification, true, true)
                        validatedSignal.save()
                        render([success: true, message: message(code: "singleCaseAlert.add.caseNumber.success")] as JSON)
                    }
                } else {
                    render([success: false, message: message(code: "singleCaseAlert.duplicate.caseNumber.data")] as JSON)
                }
            }
            User user = cacheService.getUserByUserId(userService.getUser()?.id)//userService.getUser()
            User assignee = validatedSignal.assignedTo?cacheService.getUserByUserId(validatedSignal.assignedTo.id):null
            Group assigneeGroup = validatedSignal.assignedToGroup?cacheService.getGroupByGroupId(validatedSignal.assignedToGroup.id):null
            ActivityType activityType =  ActivityType.findByValue(ActivityTypeValue.CaseAdded)
            List<Map> signalActivities = []
            List<Map> signalAlerts = []
            SingleCaseAlert.findAllByExecutedAlertConfiguration(executedConfig).each {alert ->

                signalAlerts << [col1: validatedSignal.id.toString(), col2: alert.id.toString(), caseNumber: alert.caseNumber]

                Activity activity = new Activity(
                        type: activityType,
                        performedBy: user,
                        timestamp:DateTime.now(),
                        justification: params.justification,
                        assignedTo: assignee,
                        details: "Case '" + alert.caseNumber + "' is added to signal '"+ validatedSignal.name +"'",
                        attributes: ([:] as JSON).toString(),
                        assignedToGroup: assigneeGroup
                )

                activity.save(flush:true)
                signalActivities << [col1: validatedSignal.id.toString(), col2: activity.id.toString()]

            }

            if(signalAlerts) {
                batchPersistSignalActivity(signalActivities)
                batchPersistSignalAlerts(signalAlerts)
                signalAuditLogService.createAuditLog([
                        entityName : "Signal: Individual Case Review Observations",
                        moduleName : "Signal: Individual Case Review Observations",
                        category   : AuditTrail.Category.UPDATE.toString(),
                        entityValue: "${validatedSignal.getInstanceIdentifierForAuditLog()}: Case Added",
                        username   : userService.getUser().username,
                        fullname   : userService.getUser().fullName
                ] as Map, [[propertyName: "Added Case Numbers", oldValue: "", newValue: signalAlerts.collect{it.caseNumber}],
                           [propertyName: "Justification", newValue: params.justification]] as List)
            }

            log.info('Audit log saved for user : ' + userService.getUser().username)

        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Some error occurred", ex.printStackTrace())
            if(ex.getMessage() == "Alert failed due to PV Reports inaccessibility.") {
                render([success: false, message: 'Add case failed due to PV Reports inaccessibility.'] as JSON)
            } else {
                render([success: false, message: 'Something unexpected happened at server'] as JSON)
            }
        }
    }

    void batchPersistSignalAlerts(List signalAlerts) {
        log.info("Now Saving the signal alert mapping")
        try {
            Session session = sessionFactory.currentSession
            String insertValidatedQuery = "INSERT INTO validated_single_alerts(validated_signal_id,single_alert_id) VALUES(?,?)"
            alertService.batchPersistForMapping(session, signalAlerts, insertValidatedQuery)
            session.flush()
            session.clear()
        } catch(Exception ex) {
            ex.printStackTrace()
        }
        log.info("Saving the mapping is completed")
    }

    void batchPersistSignalActivity(List signalActivities) {
        log.info("Now Saving the signal activity mapping")
        try {
            Session session = sessionFactory.currentSession
            String insertValidatedQuery = "INSERT INTO VALIDATED_ALERT_ACTIVITIES(validated_signal_id,activity_id) VALUES(?,?)"
            alertService.batchPersistForMapping(session, signalActivities, insertValidatedQuery)
            session.flush()
            session.clear()
        } catch(Exception ex) {
            ex.printStackTrace()
        }
        log.info("Saving the mapping is completed")
    }

    Configuration createConfigForNewCases(Long signalId) {
        ValidatedSignal vs = ValidatedSignal.get(signalId)
        Configuration config = Configuration.findByName(vs?.name + "_vs_" + vs?.id)
        if(config) {
            return config
        }
        User currentUser = userService.getUser()
        Date currentDate = new Date()
        Configuration configurationInstance = new Configuration()
        configurationInstance.type = "Single Case Alert"
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        configurationInstance.priority = Priority.list().get(0)
        configurationInstance.productDictionarySelection = vs?.productDictionarySelection
        configurationInstance.productSelection = vs?.products
        configurationInstance.productGroupSelection = vs?.productGroupSelection
        configurationInstance.owner = vs?.assignedTo ?: currentUser
        configurationInstance.assignedTo =vs?.assignedTo ?: currentUser
        configurationInstance.adhocRun = false
        configurationInstance.isStandalone = true
        configurationInstance.missedCases = false
        configurationInstance.excludeNonValidCases = false
        configurationInstance.includeLockedVersion = true
        configurationInstance.evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
        configurationInstance.dateCreated = currentDate
        configurationInstance.lastUpdated = currentDate
        configurationInstance.createdBy = currentUser
        configurationInstance.modifiedBy = currentUser
        configurationInstance.repeatExecution = false
        configurationInstance.groupBySmq = false
        configurationInstance.name = vs.name + "_vs_" + vs.id
        configurationInstance.isCaseSeries = true
        configurationInstance.workflowGroup = currentUser?.workflowGroup
        configurationInstance.selectedDatasource = "pva"
        AlertDateRangeInformation alertDateRangeInformation = new AlertDateRangeInformation()
        alertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        alertDateRangeInformation.dateRangeStartAbsolute = new Date()
        alertDateRangeInformation.dateRangeEndAbsolute = new Date()
        configurationInstance.alertDateRangeInformation = alertDateRangeInformation
        configurationInstance.skipAudit=true
        configurationInstance.save(flush: true, failOnError: true)

    }


    protected List sortedRoles( ) {
        Role.list().sort { it.toString() }
    }

    protected Map buildUserModel(userInstance) {

        String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
        String authoritiesPropertyName = SpringSecurityUtils.securityConfig.userLookup.authoritiesPropertyName

        List roles = sortedRoles()
        Set userRoleNames = userInstance[authoritiesPropertyName].collect { it[authorityFieldName] }
        def granted = [:]
        def notGranted = [:]
        for (role in roles) {
            String authority = role[authorityFieldName]
            if (userRoleNames.contains(authority)) {
                granted[(role.toString(  ).trim(  ))] = userRoleNames.contains(authority)
            } else {
                notGranted[(role.toString(  ).trim(  ))] = userRoleNames.contains(authority)
            }
        }

        return [userInstance: userInstance, roleMap: granted + notGranted]
    }


    List<String> splitCellContent(String content) {
        List<String> stringList = []
        if (content.length() <= Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX) {
            stringList.add(content)
        } else {
            stringList.add(content.substring(0, Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX))
            stringList.addAll(splitCellContent(content.substring(Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX)))
        }
        return stringList
    }

}
