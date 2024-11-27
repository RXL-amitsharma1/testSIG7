package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.config.*
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.helper.AlertHelper
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AttachmentDescription
import com.rxlogix.signal.Justification
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.HttpStatus
import org.apache.http.util.TextUtils
import com.rxlogix.exception.FileFormatException
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.rxlogix.util.MiscUtil
import javax.servlet.http.HttpSession

import static com.rxlogix.util.MiscUtil.calcDueDate
import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class AdHocAlertController implements JsonUtil, AlertController, AlertUtil, LinkHelper {
    def configurationService
    def adHocAlertService
    def userService
    def SpringSecurityService
    def CRUDService
    def activityService
    def dynamicReportService
    def alertService
    def alertAttributesService
    def alertDocumentService
    def emailService
    def workflowRuleService
    def customMessageService
    def priorityService
    def productBasedSecurityService
    def safetyLeadSecurityService
    def validatedSignalService
    def topicService
    def medicalConceptsService
    def actionTemplateService
    def attachmentableService
    def viewInstanceService
    def alertCommentService
    EmailNotificationService emailNotificationService
    def cacheService
    def undoableDispositionService
    def dispositionService
    def signalAuditLogService
    DataObjectService dataObjectService

    @Secured(['ROLE_AD_HOC_CRUD'])
    def create() {
        // Use groupBy to split the list into different sections
        Map<String, List<Map>> groupedConfiguration = cacheService.getAdhocGroupConfigurationCache(Constants.AdhocAlert.GROUPED_CONFIGURATION)
        if (Objects.isNull(groupedConfiguration) || groupedConfiguration.isEmpty()) {
            configurationNotAcceptable()
            return
        }
        AdHocAlert adHocAlertInstance = buildAlert()
        List formulations = adHocAlertService.getFormulations()
        List lmReportTypes = adHocAlertService.getLmRetportTypes()
        List countryNames = adHocAlertService.getCountryNames()
        Boolean isPVCM = dataObjectService?.getDataSourceMap(Constants.DbDataSource.PVCM)
        // Extract individual section-based lists
        List<Map> alertCriteriaList = groupedConfiguration['alertcriteria'] ?: []
        List<Map> alertDetailsList = groupedConfiguration['alertdetails'] ?: []
        List<Map> signalStatusList = groupedConfiguration['signalstatus'] ?: []

        Map<String, String> sectionLabelMap = cacheService.getAdhocSectionLabelMap(Constants.AdhocAlert.SECTION_LABEL_MAP)
        render(view: "create", model: [alertInstance: adHocAlertInstance, alertAttributesService: alertAttributesService, safetyLeadSecurityService: safetyLeadSecurityService,
                                       userService  : userService, isAdhocAlert: true, formulations: formulations, lmReportTypes: lmReportTypes, countryNames: countryNames, isPVCM: isPVCM,
                                       alertCriteriaList: alertCriteriaList, alertDetailsList: alertDetailsList, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap])
    }

    @Secured(['ROLE_AD_HOC_CRUD', 'ROLE_VIEW_ALL'])
    def index() {
        List reviewConfiguration = cacheService.getAdhocReviewCache()
        if (Objects.isNull(reviewConfiguration) || reviewConfiguration.isEmpty()) {
            configurationNotAcceptable()
            return
        }
        ViewInstance viewInstance = viewInstanceService.fetchViewInstanceSignal(Constants.AlertConfigType.ADHOC_ALERT)
        Map<String, String> labelMap = getLabelMap(viewInstance)
        String timezone = userService.getCurrentUserPreference()?.timeZone
        List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone)
        Map dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        Boolean forceJustification = userService.user.workflowGroup?.forceJustification
        List<Map> availablePriorities = priorityService.listPriorityOrder()
        List allowedProductsAsSafetyLead = alertService.isProductSecurity() ? productBasedSecurityService.allAllowedProductIdsForUser(userService.getUser()) : []
        Boolean hasAdhocReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.AD_HOC_ALERT)
        String buttonClass = ""
        if(!hasAdhocReviewerAccess){
            buttonClass = Constants.ButtonClass.HIDDEN
        }
        def adhocHelpMap = Holders.config.adhoc.helpMap
        Map dispositionData = workflowRuleService.fetchDispositionData();
        List<String> reviewCompletedDispostionList = dispositionService.getReviewCompletedDispositionList()
        User currentUser = userService.getUser()
        render(view: 'index', model: [callingScreen                       : params.callingScreen,
                                      backUrl                             : request.getHeader('referer'),
                                      priorities                          : priorityService.listPriorityOrder() as JSON,
                                      strategyList                        : SignalStrategy.findAll(),
                                      viewId                              : viewInstance?.id,
                                      dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                                      dispositionData                     : dispositionData as JSON,
                                      forceJustification                  : forceJustification,
                                      availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                                      availablePriorities                 : availablePriorities,
                                      allowedProductsAsSafetyLead         : allowedProductsAsSafetyLead,
                                      isProductSecurity                   : alertService.isProductSecurity(),
                                      isPriorityEnabled                   : grailsApplication.config.alert.priority.enable,
                                      hasAdhocReviewerAccess              : hasAdhocReviewerAccess,
                                      hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                                      hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                                      buttonClass                         : buttonClass,
                                      currUserName                        : currentUser.fullName,
                                      reviewCompletedDispostionList       : JsonOutput.toJson(reviewCompletedDispostionList),
                                      adhocHelpMap                        : adhocHelpMap,
                                      labelMap                            : labelMap
        ])
    }

    private Map<String, String> getLabelMap(ViewInstance viewInstance) {
        Map<String, String> labelMap = [:]
        List<Map> adhocConfiguration = cacheService.getAdhocConfigurationCache()

        if (viewInstance && adhocConfiguration) {
            // Use JsonSlurper to parse the JSON string safely
            JsonSlurper js = new JsonSlurper()

            // Parse JSON from tempColumnSeq or columnSeq
            String jsonString = viewInstance.tempColumnSeq ?: viewInstance.columnSeq
            if (jsonString) {
                Map caseListCols = js.parseText(jsonString)

                // Map the name to the label
                labelMap = caseListCols.collectEntries { key, value ->
                    [(value.name): value.label]
                }
            }
            adhocConfiguration.findAll { it.name in ['priority', 'name'] && it.enabled == true }.each { config ->
                labelMap[config.name] = config.label
            }
        }

        return labelMap
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def copy(AdHocAlert originalAlert) {
        if (!originalAlert) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        def savedAlert = adHocAlertService.copyAlert(originalAlert, currentUser)
        if (savedAlert.hasErrors()) {
            chain(action: "index", model: [error: savedAlert])
        } else {
            redirect(action: "index")
        }
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def delete(Configuration config) {

        if (!config) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (config.isEditableBy(currentUser)) {
            def deletedConfig = configurationService.deleteConfig(config)
            if (deletedConfig.hasErrors()) {
                chain(action: "index", model: [error: deletedConfig])
            }
            flash.message = message(code: "app.configuration.delete.success", args: [config.name])
        } else {
            flash.warn = message(code: "app.configuration.delete.fail", args: [config.name])
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def edit() {
        def adHocAlert = alertService.findAdhocAlertById(params.id)
        alertAttributesService.addMissingProperties(adHocAlert, params)
        List formulations = adHocAlertService.getFormulations()
        List lmReportTypes = adHocAlertService.getLmRetportTypes()
        List countryNames = adHocAlertService.getCountryNames()
        Boolean isPVCM = dataObjectService?.getDataSourceMap(Constants.DbDataSource.PVCM)
        // Use groupBy to split the list into different sections
        Map<String, List<Map>> groupedConfiguration = cacheService.getAdhocGroupConfigurationCache(Constants.AdhocAlert.GROUPED_CONFIGURATION)
        if (Objects.isNull(groupedConfiguration) || groupedConfiguration.isEmpty()) {
            configurationNotAcceptable()
            return
        }
        // Extract individual section-based lists
        List<Map> alertCriteriaList = groupedConfiguration['alertcriteria'] ?: []
        List<Map> alertDetailsList = groupedConfiguration['alertdetails'] ?: []
        List<Map> signalStatusList = groupedConfiguration['signalstatus'] ?: []

        Map<String, String> sectionLabelMap = cacheService.getAdhocSectionLabelMap(Constants.AdhocAlert.SECTION_LABEL_MAP)
        if (!adHocAlert) {
            notFound()
            return
        }

        render(view: "edit", model: [alertInstance            : adHocAlert, configSelectedTimeZone: params?.configSelectedTimeZone,
                                     alertAttributesService   : alertAttributesService,
                                     safetyLeadSecurityService: safetyLeadSecurityService,
                                     userService              : userService,
                                     detectedDate             :
                                             DateUtil.toUSDateString(adHocAlert.detectedDate,
                                                     grailsApplication.config.server.timezone),
                                     checked                  : getProductSelectionType(adHocAlert.productSelection),
                                     isAdhocAlert             : true,
                                     productName              : getNameFieldFromJson(adHocAlert.productSelection),
                                     formulations             : formulations,
                                     lmReportTypes            : lmReportTypes,
                                     countryNames             : countryNames,
                                     isPVCM                   : isPVCM,
                                     alertCriteriaList: alertCriteriaList, alertDetailsList: alertDetailsList, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap])
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def save() {
        AdHocAlert adHocAlertInstance = buildAlert()
        List formulations = adHocAlertService.getFormulations()
        List lmReportTypes = adHocAlertService.getLmRetportTypes() // bugFix- PVS-38963
        List countryNames = adHocAlertService.getCountryNames()
        try {
            //We need to check for sharedWithGroups param
            if (!params.sharedWithGroups)
                params.sharedWithGroups = []
            //Extra Space is removed from name field
            params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
            bindData(adHocAlertInstance, params, ["productGroupSelection","eventGroupSelection",'detectedDate', 'workflowState', 'disposition', 'sharedWith'])
            if(!adHocAlertInstance.priority){
                adHocAlertInstance.priority = Priority.findByDefaultPriority(true)?:null
            }
            if(params.productGroupSelection!="[]"){
                adHocAlertInstance.productGroupSelection=params.productGroupSelection
            }
            if(params.eventGroupSelection!="[]"){
                adHocAlertInstance.eventGroupSelection=params.eventGroupSelection
            }
            // PVS-56093: Set studySelection based on params.studySelection
            if (params && params.studySelection && params.studySelection != "[]") {
                adHocAlertInstance.studySelection = params.studySelection
            } else {
                adHocAlertInstance.studySelection = null
            }
            adHocAlertInstance.workflowGroup = userService.getUser().workflowGroup
            adHocAlertInstance.noteModifiedBy=userService.getUser().fullName
            adHocAlertInstance.lastUpdatedNote=new Date()
            adHocAlertInstance.disposition = adHocAlertInstance.workflowGroup?.defaultAdhocDisposition

            adHocAlertInstance.detectedDate = DateUtil.stringToDate(
                    params.detectedDate, DateUtil.DEFAULT_DATE_FORMAT,
                    grailsApplication.config.server.timezone)

            List<Map> adhocConfiguration = cacheService.getAdhocConfigurationCache()
            // Use groupBy to split the list into different sections
            Map<String, List<Map>> groupedConfiguration = cacheService.getAdhocGroupConfigurationCache(Constants.AdhocAlert.GROUPED_CONFIGURATION)
            // Extract individual section-based lists
            List<Map> alertCriteriaList = groupedConfiguration['alertcriteria'] ?: []
            List<Map> alertDetailsList = groupedConfiguration['alertdetails'] ?: []
            List<Map> signalStatusList = groupedConfiguration['signalstatus'] ?: []

            Map<String, String> sectionLabelMap = cacheService.getAdhocSectionLabelMap(Constants.AdhocAlert.SECTION_LABEL_MAP)

            if (mandatoryCheck(adhocConfiguration, params)) {
                flash.error = message(code: "app.adhoc.all.mandatory.fields.required")
                render view: "create", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                               safetyLeadSecurityService: safetyLeadSecurityService, userService: userService,
                                               checked                  : getProductSelectionType(adHocAlertInstance.productSelection),formulations: formulations, lmReportTypes: lmReportTypes,
                                               alertCriteriaList: alertCriteriaList, alertDetailsList: alertDetailsList, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap,
                                               isAdhocAlert: true, countryNames: countryNames
                ]
                return
            }

            if (adHocAlertInstance.detectedDate){
                if (DateUtils.truncate(adHocAlertInstance.detectedDate, Calendar.DAY_OF_MONTH) > (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                        flash.error = message(code: "app.label.future.date.error", args: ["Detected Date"])
                        render view: "create", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                                       safetyLeadSecurityService: safetyLeadSecurityService, userService: userService,
                                                       checked                  : getProductSelectionType(adHocAlertInstance.productSelection),formulations: formulations, lmReportTypes: lmReportTypes,
                                                       alertCriteriaList: alertCriteriaList, alertDetailsList: alertDetailsList, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap,
                                                       isAdhocAlert: true, countryNames: countryNames
                        ]
                        return
                    }
                calcDueDate(adHocAlertInstance, adHocAlertInstance.priority, adHocAlertInstance.disposition, false,
                        cacheService.getDispositionConfigsByPriority(adHocAlertInstance.priority.id))
            } else
                adHocAlertInstance.dueDate = null

            adHocAlertInstance.initialDisposition = adHocAlertInstance.disposition
            adHocAlertInstance.initialDueDate = adHocAlertInstance.dueDate
            def isProductOrFamily = allowedDictionarySelection(adHocAlertInstance)
            boolean isProductGroupOnly = adHocAlertInstance.productSelection && adHocAlertInstance.productGroupSelection
            adHocAlertInstance = userService.assignGroupOrAssignTo(params.assignedToValue, adHocAlertInstance)
            userService.bindSharedWithConfiguration(adHocAlertInstance, params.sharedWith, false)
            if (!isProductOrFamily  || isProductGroupOnly) {
                flash.error = message(code: "app.label.product.family.error.message")
                render view: "create", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                               safetyLeadSecurityService: safetyLeadSecurityService, userService: userService,
                                               checked                  : getProductSelectionType(adHocAlertInstance.productSelection),formulations: formulations, lmReportTypes: lmReportTypes,
                                               alertCriteriaList: alertCriteriaList, alertDetailsList: alertDetailsList, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap,
                                               isAdhocAlert: true, countryNames: countryNames
                ]
                return
            }
            if (!adHocAlertInstance.eventGroupSelection) {
                adHocAlertInstance.eventGroupSelection = null
            }
            if (!adHocAlertInstance.validate() || !adHocAlertInstance.save(failOnError: true, flush: true)) {
                render view: "create", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                               safetyLeadSecurityService: safetyLeadSecurityService, userService: userService,
                                               checked                  : getProductSelectionType(adHocAlertInstance.productSelection),formulations: formulations, lmReportTypes: lmReportTypes,
                                               alertCriteriaList: alertCriteriaList, alertDetailsList: alertDetailsList, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap,
                                               isAdhocAlert: true, countryNames: countryNames
                ]
                return
            }

            if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ADHOC_ALERT)) {
                def alertLink = createHref("adHocAlert", "alertDetail", ["id": adHocAlertInstance.id])
                def productName = getNameFieldFromJson(adHocAlertInstance.productSelection)
                emailNotificationService.mailHandlerForAhocPotesntialSignal(adHocAlertInstance, alertLink, productName)
            }
        } catch (Exception ve) {
            ve.printStackTrace()
            render view: "create", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                           safetyLeadSecurityService: safetyLeadSecurityService, userService: userService,
                                           checked                  : getProductSelectionType(adHocAlertInstance.productSelection),formulations: formulations, lmReportTypes: lmReportTypes,
                                           alertCriteriaList: alertCriteriaList, alertDetailsList: alertDetailsList, signalStatusList: signalStatusList, sectionLabelMap: sectionLabelMap,
                                           isAdhocAlert: true, countryNames: countryNames
            ]
            return
        }

        request.withFormat {
            form {
                redirect(action: "index")
            }
            '*' { respond adHocAlertInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def update() {
        AdHocAlert adHocAlertInstance = alertService.findAdhocAlertById(params.id)
        adHocAlertInstance.setIsPublic(true)  //Added for PVS-56093

        if (!adHocAlertInstance) {
            notFound()
            return
        }

        alertAttributesService.addMissingProperties(adHocAlertInstance, params)

        try {
            //We need to check for sharedWithGroups param
            if (!params.sharedWithGroups)
                params.sharedWithGroups = []
            //Extra Space is removed from name field
            params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
            bindData(adHocAlertInstance, params, ['productGroupSelection','eventGroupSelection','detectedDate', 'id','sharedWith'])
            if(!adHocAlertInstance.priority){
                adHocAlertInstance.priority = Priority.findByDefaultPriority(true)?:null
            }
            if(params.productGroupSelection!="[]"){
                adHocAlertInstance.productGroupSelection=params.productGroupSelection
            } else {
                adHocAlertInstance.productGroupSelection=null
            }
            if(params.eventGroupSelection!="[]"){
                adHocAlertInstance.eventGroupSelection=params.eventGroupSelection
            } else {
                adHocAlertInstance.eventGroupSelection=null
            }
            // PVS-56093: Set studySelection based on params.studySelection
            if (params && params.studySelection && params.studySelection != "[]") {
                adHocAlertInstance.studySelection = params.studySelection
            } else {
                adHocAlertInstance.studySelection = null
            }
            adHocAlertInstance.detectedDate = DateUtil.stringToDate(
                    params.detectedDate, DateUtil.DEFAULT_DATE_FORMAT,
                    grailsApplication.config.server.timezone)
            List<Map> adhocConfiguration = cacheService.getAdhocConfigurationCache()
            if (mandatoryCheck(adhocConfiguration, params)) {
                flash.error = message(code: "app.adhoc.all.mandatory.fields.required")
                render view: "create", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                               safetyLeadSecurityService: safetyLeadSecurityService, userService: userService,
                                               checked                  : getProductSelectionType(adHocAlertInstance.productSelection),
                                               isAdhocAlert: true
                ]
                return
            }

            if (adHocAlertInstance.detectedDate){
                if(DateUtils.truncate(adHocAlertInstance.detectedDate, Calendar.DAY_OF_MONTH) > (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                    flash.error = message(code: "app.label.future.date.error", args: ["Evaluation Date"])
                    render view: "create", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                                   safetyLeadSecurityService: safetyLeadSecurityService, userService: userService,
                                                   checked                  : getProductSelectionType(adHocAlertInstance.productSelection)
                    ]
                    return
                }
                calcDueDate(adHocAlertInstance, adHocAlertInstance.priority, adHocAlertInstance.disposition, false,
                        cacheService.getDispositionConfigsByPriority(adHocAlertInstance.priority.id))
            } else
                adHocAlertInstance.dueDate = null

            if (!adHocAlertInstance.validate())
                throw new Exception()
            adHocAlertInstance = (AdHocAlert) userService.assignGroupOrAssignTo(params.assignedToValue, adHocAlertInstance)
            userService.bindSharedWithConfiguration(adHocAlertInstance, params.sharedWith, true)

            adHocAlertInstance = (AdHocAlert) CRUDService.update(adHocAlertInstance)
        } catch (Exception ve) {
            render view: "edit", model: [alertInstance            : adHocAlertInstance, alertAttributesService: alertAttributesService,
                                         safetyLeadSecurityService: safetyLeadSecurityService, userService: userService]
            return
        }

        request.withFormat {
            form {
                redirect(action: "index")
            }
            '*' { respond adHocAlertInstance, [status: OK] }
        }
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def review() {
        render view: "index"
    }

    @Secured(["isAuthenticated()"])
    def executionStatus() {
        User currentUser = userService.getUser()
        render(view: "executionStatus", model: [related: "executionStatusPage", isAdmin: currentUser?.isAdmin()])
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def listAllResults() {
        User currentUser = userService.getUser()
        render(view: "executionStatus", model: [related: "listAllResultsPage", isAdmin: currentUser?.isAdmin()])
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    public getPublicForExecutedConfig() {
        return ExecutedConfiguration.get(params.id).isPublic ? message(code: "app.label.public") : message(code: "app.label.private")
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    @Secured(['ROLE_AD_HOC_CRUD', 'ROLE_VIEW_ALL'])
    def list(Boolean isFilterRequest) {
        HttpSession session = request.getSession()
        if(params?.callingScreen == Constants.Commons.DASHBOARD) {
            session.setAttribute("adhocDashboard", params["selectedAlertsFilterForDashboard"])
        }else{
            session.setAttribute("adhoc",  params["selectedAlertsFilter"])
        }
        Long viewId = 0L
        if (params["viewInstanceId"]) {
            viewId = params["viewInstanceId"] as Long
        }
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(Constants.AlertConfigType.ADHOC_ALERT, viewId)
        List filters = []
        if (!isFilterRequest && !adHocAlertService.checkForEmptyBrackets(viewInstance.selectedDispositions)) {
            filters = adHocAlertService.convertJsonToList(viewInstance.selectedDispositions)
        } else {
            filters = getFiltersFromParamsAdhoc(isFilterRequest, params)
        }
        boolean isFilteredDispositionChanged = false
        if (Objects.nonNull(viewInstance)) {
            isFilteredDispositionChanged = adHocAlertService.checkFilterDispositionChange(filters, viewInstance.selectedDispositions)
        }
        Map fetchValue = fetchAdhocList(filters, params)
        List adHocList = fetchValue.data
        Set dispositionSet = fetchDispositionSet(isFilterRequest, filters)
        if (dispositionSet) {
            adHocAlertService.updateClosedStatus(dispositionSet, filters)
        }
        if (isFilteredDispositionChanged) {
            String filtersJsonString = JsonOutput.toJson(filters)
            viewInstance.selectedDispositions = (filtersJsonString == '{}' ? null : filtersJsonString)
            viewInstance.save(flush:true)
        }
        Map<String, String> labelMap = getLabelMap(viewInstance)
        Map resultSet = [data: adHocList, filters: dispositionSet, configId: '', recordsFiltered: fetchValue.recordsFiltered, recordsTotal: fetchValue.recordsTotal, labelMap: labelMap]
        render resultSet as JSON
    }

    private Map fetchAdhocList(filters, params) {
        List adHocList = []
        Map retValue = [:]
        try {
            Disposition defaultAdHocDisposition = userService.getUser().workflowGroup.defaultAdhocDisposition
            cacheService.setDefaultDisp(Constants.AlertConfigType.AD_HOC_ALERT, defaultAdHocDisposition.id as Long)
            retValue = adHocAlertService.getAllowedAdHocAlerts(AdHocAlert, filters, params.callingScreen as String, params)
            List<AdHocAlert> adhaList = retValue.data
            List<Long> alertIdList = adhaList.collect {it.id}
            List<Long> undoableAlertIdList =  undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertConfigType.AD_HOC_ALERT)
            adhaList.each { AdHocAlert adhoc ->
                def signals = validatedSignalService.getSignalsFromAlertObj(adhoc, Constants.AlertConfigType.AD_HOC_ALERT)
                adHocList.add(adhoc.briefs(true, signals, undoableAlertIdList.contains(adhoc.id)?:false))
            }
            cacheService.removeDefaultDisp(Constants.AlertConfigType.AD_HOC_ALERT)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        return [data: adHocList, recordsFiltered: retValue.recordsFiltered, recordsTotal: retValue.recordsTotal]
    }

    private List createAdhocList(filters, params) {
        List adHocList = []
        try {
            Map adhaMap = adHocAlertService.getAllowedAdHocAlerts(AdHocAlert, filters, params.callingScreen as String, params)
            List<AdHocAlert> adhaList = adhaMap.data
            adhaList.each { AdHocAlert adhoc ->
                def signals = validatedSignalService.getSignalsFromAlertObj(adhoc, Constants.AlertConfigType.AD_HOC_ALERT)
                adHocList.add(adhoc.briefs(true, signals))
            }
        } catch (Throwable th) {
            th.printStackTrace()
        }
        adHocList
    }

    Set fetchDispositionSet(Boolean isFilterRequest, List filters) {

        List dispositionSet = []
        List alertList = AdHocAlert.list()
        List<Disposition> dispositionList = alertList.collect { it.disposition }.unique {
            it?.displayName
        }

        dispositionSet = dispositionList.collect {
            if (params.dashboardFilter == 'total' || params.dashboardFilter == 'new') {
                [value: it.displayName, closed: false]
            } else if (params.dashboardFilter == 'underReview' || params.dashboardFilter == "assignedTo") {
                [value: it.displayName, closed: isFilterRequest ? true : it.closed || it.validatedConfirmed]
            } else if (!(params.callingScreen == 'dashboard' && (it.closed == true || it.reviewCompleted == true))) {
                [value: it.displayName, closed: isFilterRequest ? true : it.closed]
            }
        }

        dispositionSet.minus(null)
    }

    List getFiltersFromParamsAdhoc(Boolean isFilterRequest, def params) {
        List filters = []
        def escapedFilters = null
        if (params.filters) {
            def slurper = new JsonSlurper()
            escapedFilters = slurper.parseText(params.filters)
        }
        if(escapedFilters) {
            filters = new ArrayList(escapedFilters)
        }
        if (params.dashboardFilter && (params.dashboardFilter == 'total' || params.dashboardFilter == 'new') && !isFilterRequest) {
            filters = Disposition.list().collect { it.displayName }
        } else if (params.dashboardFilter && params.dashboardFilter == 'underReview' && !isFilterRequest) {
            filters = Disposition.findAllByClosedNotEqualAndValidatedConfirmedNotEqual(true, true).collect {
                it.displayName
            }
        } else if (!isFilterRequest && (params.filters == "[]" || params.filters == "")) {
            filters = Disposition.findAllByClosed(false).collect { it.displayName }
        } else if (!isFilterRequest && params.filters != "") {
            filters = Disposition.findByDisplayNameInList(params.filters as List).collect { it.displayName }
        }

        filters
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def changePriorityOfAlert(String selectedRows, Priority newPriority, String justification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, data: [])
        try {
            JSON.parse(selectedRows).each { Map<String, Long> selectedRow ->
                AdHocAlert alert = AdHocAlert.get(selectedRow["alert.id"])

                Priority orgPriority = alert.priority
                alert.priority = newPriority
                calcDueDate(alert, alert.priority, alert.disposition, false,
                        cacheService.getDispositionConfigsByPriority(alert.priority.id))
                alert.save()
                responseDTO.data << [id: alert.id, dueIn: alert.dueIn()]
                activityService.create(alert, ActivityType.findByValue(ActivityTypeValue.PriorityChange),
                        userService.getUser(), "Priority changed from $orgPriority to $newPriority",
                        justification, [product: getNameFieldFromJson(alert.productSelection),
                                        event  : getNameFieldFromJson(alert.eventSelection)])
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.priority.change.error")
        }
        render(responseDTO as JSON)
    }

    //TODO : This method is no longer in use
    @Secured(['ROLE_SINGLE_CASE_CRUD'])
    def addSignalToValidationState() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)

        String newValue = params.newValue
        def alertList = params.alertDetails
        JsonSlurper jsonSlurper = new JsonSlurper()

        def signalObj = jsonSlurper.parseText(params.signalList)
        if (TextUtils.isEmpty(signalObj.getAt(0).signalName) || TextUtils.isEmpty(params.medicalConcepts)) {
            responseDTO.status = false
            responseDTO.message = "Please fill the required fields."
            render(responseDTO as JSON)
            return
        }
        try {
            def alertListObj = jsonSlurper.parseText(alertList)
            def alertIdsList = []
            alertListObj.each {
                alertIdsList.add(Long.parseLong(it.get("alertId")))
            }
            alertListObj = AdHocAlert.findAllByIdInList(alertIdsList)
            alertListObj.each { AdHocAlert alert ->

                if (alert) {
                    //Call the signal persistance flow.
                    Disposition defaultSignalDisposition = alert?.alertConfiguration?.getWorkflowGroup()?.defaultSignalDisposition
                    validatedSignalService.attachToSignal(params.signalList, params.products, alert, Constants.AlertConfigType.AD_HOC_ALERT, alert.assignedTo, alert.assignedToGroup, defaultSignalDisposition)
                }
                if (params.medicalConcepts) {
                    //Attach the medical concepts.
                    medicalConceptsService.addMedicalConcepts(alert, params.medicalConcepts)
                }

                //Attach the action templates to the signal
                actionTemplateService.addActionsFromTemplate(params.actionTemplate, Constants.AlertConfigType.SIGNAL_MANAGEMENT, validatedSignal)
            }
            render(responseDTO as JSON)
        } catch (Exception ex) {
            ex.printStackTrace()
            render(status: BAD_REQUEST)
        }
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def changeDisposition(String selectedRows, Disposition targetDisposition, String justification, String validatedSignalName, String productJson,Long signalId) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            validatedSignalName = validatedSignalName? org.apache.commons.lang.StringEscapeUtils.unescapeHtml(validatedSignalName) : ''
            Map attachedSignalData = adHocAlertService.changeDisposition(selectedRows, targetDisposition, justification, validatedSignalName, productJson,signalId)
            if (attachedSignalData) {
                responseDTO.data = attachedSignalData;
            }
        }  catch (grails.validation.ValidationException vx) {
            vx.printStackTrace()
            responseDTO.status = false
            responseDTO.message = MiscUtil.getCustomErrorMessageList(vx)[0]
        }catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def revertDisposition(Long id, String justification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            Map attachedSignalData = adHocAlertService.revertDisposition(id, justification)
            if (attachedSignalData) {
                responseDTO.data = attachedSignalData;
            }
            if(!attachedSignalData.dispositionReverted){
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

    @Secured(['ROLE_AD_HOC_CRUD'])
    def changeAssignedToGroup(String selectedId, String assignedToValue) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: 'app.assignedTo.changed.success'))
        List<Long> selectedRowsList = JSON.parse(selectedId).collect {
            it as Long
        }
        boolean bulkUpdate = selectedRowsList.size() > 1
        try {
            selectedRowsList.each { Long id ->
                AdHocAlert adHocAlert = AdHocAlert.get(id)
                if (assignedToValue) {
                    String oldUserName = userService.getAssignedToName(adHocAlert)
                    List<User> oldUserList = userService.getUserListFromAssignToGroup(adHocAlert)
                    adHocAlert = userService.assignGroupOrAssignTo(assignedToValue, adHocAlert)
                    String newUserName = userService.getAssignedToName(adHocAlert)
                    if (emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.ASSIGNEE_UPDATE)) {
                        List<User> newUserList = userService.getUserListFromAssignToGroup(adHocAlert)
                        if (assignedToValue) {
                            // TODO we need to create an activity log entry here.
                            String alertLink = createHref("adHocAlert", "alertDetail", ["id": adHocAlert.id])
                            String productName = getNameFieldFromJson(adHocAlert.productSelection)
                            List recipientsList = []
                            newUserList.each { User newUser ->
                                if (!recipientsList.count { it == newUser.email }) {
                                    recipientsList << newUser.email
                                    emailNotificationService.mailHandlerForAssignedToNewUserAdhoc(newUser, adHocAlert, alertLink, productName)
                                }
                            }
                            oldUserList.each { User oldUser ->
                                if (!recipientsList.count { it == oldUser.email }) {
                                    recipientsList << oldUser.email
                                    emailNotificationService.mailHandlerForAssignedToOldUserAdhoc(oldUser, adHocAlert, alertLink, productName, newUserName)
                                }
                            }
                        }
                    }
                    String details = message(code: 'app.assignedTo.changed.message', args: [oldUserName, newUserName])
                    activityService.create(adHocAlert.id, ActivityType.findByValue(ActivityTypeValue.AssignedToChange),
                            userService.getUser(), details, null)
                }
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Throwable e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = message(code: 'app.assignedTo.changed.fail')
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def exportReport() {
        def scaList
        def ahaList = []

        params.put("search[value]",params.search)
        params.put("selectedAlertsFilter",session.getAttribute("adhoc"))

        if (params.ids) {
            def alertIds = params.ids.tokenize(",").collect { Long.parseLong(it) }
            scaList = AdHocAlert.findAll { id in alertIds }
            List<AdHocAlert> filteredList = alertService.filterByProductSecurity(scaList)
            filteredList.each { AdHocAlert adhoc ->
                def signals = validatedSignalService.getSignalsFromAlertObj(adhoc, Constants.AlertConfigType.AD_HOC_ALERT)
                def topics = topicService.getTopicsFromAlertObj(adhoc, Constants.AlertConfigType.AD_HOC_ALERT)
                ahaList.add(adhoc.briefs(true, signals))
            }

        } else {
            List filters = getFiltersFromParamsAdhoc(params.isFilterRequest.toBoolean(), params)
            ahaList = createAdhocList(filters, params)

        }
        ahaList.each {
            def signalTopics = ""
            signalTopics = it.signalsAndTopics.collect { it.name }?.join(",")
            it.numOfIcsrs = it.numOfIcsrs ? it.numOfIcsrs + '' : ''
            it.signalsAndTopics = signalTopics
        }
        def reportFile = dynamicReportService.createAdHocAlertsDetailReport(new JRMapCollectionDataSource(ahaList), params, "")
        renderReportOutputType(reportFile,params)
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,Constants.AuditLog.ADHOC_REVIEW,Constants.AuditLog.ADHOC_REVIEW,params,reportFile.name)
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def exportDetailReport() {

        def timezone = grailsApplication.config.server.timezone
        def alerts

        if (params.ids) {
            def alertIds = params.ids.tokenize(",").collect { Long.parseLong(it) }
            alerts = AdHocAlert.findAll { id in alertIds }
        }

        if (params.selectedCases) {
            if (alerts)
                alerts + adHocAlertService.listWithFilter(params)
            else
                alerts = adHocAlertService.listWithFilter(params)
        }

        if (!alerts)
            alerts = adHocAlertService.list()

        def ahaList = alerts?.collect {
            def details = it.details()
            details.assignedTo = userService.getAssignedToName(it)
            details.dueDate = DateUtil.toDateString(it.dueDate, timezone)
            details.issueTracked = it.issuePreviouslyTracked
            details
        }

        def reportFile = dynamicReportService.createAdHocAlertsDetailReport(new JRMapCollectionDataSource(ahaList),
                params, " ")
        renderReportOutputType(reportFile,params)
    }

    /**
     * Send the output type (PDF/Excel/Word) to the browser which will save it to the user's local file system
     * @param reportFile
     * @return
     */
    private renderReportOutputType(File reportFile,def params) {
        String reportName = "Triggered Alerts-" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" +
                "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName="${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def toggleFlag() {
        def id = params.id

        if (id) {
            def flagged = alertService.toggleFlag(id)
            render([success: 'ok', flagged: flagged] as JSON)
        } else {
            response.status = 404
        }
    }

    def importFile() {
        String jsonContent = params.jsonContent

        adHocAlertService.importAlert(jsonContent)
        redirect(action: 'index')
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    @Transactional
    def upload(final Long id) {
        def adHocAlert = AdHocAlert.findById(id)
        def fileName = params?.attachments.filename
        if (fileName instanceof String) {
            fileName = [fileName]
        }
        params?.isAlertDomain = true   //this is added to check whether attachment is added from alert details screen PVS-49054
        User currentUser = userService.getUser()
        try {
            Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileName, adHocAlert, request)
            alertDetail(id)
            String fileDescription = params.description
            List<Attachment> attachments = adHocAlert.getAttachments().sort { it.dateCreated }
            //Get the Last/Latest attachment of this instance
            if (attachments) {
                List<Integer> bulkAttachmentIndex = 1..filesStatusMap?.uploadedFiles?.size()
                bulkAttachmentIndex.each {
                    Attachment attachment = attachments[-it]
                    AttachmentDescription attachmentDescription = new AttachmentDescription()
                    attachmentDescription.attachment = attachment
                    attachmentDescription.createdBy = currentUser.fullName
                    attachmentDescription.description = fileDescription
                    attachmentDescription.skipAudit = true
                    attachmentDescription.save()
                }
            }

            if (filesStatusMap?.uploadedFiles)
                activityService.create(id, ActivityType.findByValue(ActivityTypeValue.AttachmentAdded),
                        userService.getUser(), "Attachment" + filesStatusMap?.uploadedFiles*.originalFilename + "is added", null)
        }catch(FileFormatException e) {
            log.error(e.getMessage())
            redirect(action: "alertDetail", params: [id: params.id])
        }
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def deleteAttachment() {
        def attachment = Attachment.findById(params?.attachmentId)
        if (attachment) {
            def fileName = attachment.name + "." + attachment.ext
            Long alertId = params.id
            if (AttachmentDescription.findByAttachment(attachment)) {
                AttachmentDescription.findByAttachment(attachment).delete()
            }
            attachmentableService.removeAttachment(Long.parseLong(params.attachmentId))
            activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.AttachmentRemoved),
                    userService.getUser(), "Attachment [" + fileName + "] is removed", null)
        }
        redirect(action: "alertDetail", params: [id: params.id])
    }

    @Secured(['ROLE_AD_HOC_CRUD', 'ROLE_VIEW_ALL'])
    def alertDetail(final Long id) {
        List attributes = cacheService.getAdhocDetailsCache()
        AdHocAlert alert = AdHocAlert.findById(id)
        List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.AD_HOC_ALERT)
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        Map displayedAttributes = [indication            : 'Indication',
                                   issuePreviouslyTracked: 'Issue Previously Tracked',
                                   detectedBy            : 'Detected By',
                                   countryOfIncidence    : 'Country',
                                   notes                 : 'Comments',
                                   reportType            : 'Report Type',
                                   numberOfICSRs         : 'Number of ICSRs',
                                   productSelection      : 'Product/Generic Name',
                                   initDataSource        : 'Initial Datasource',
                                   formulations          : 'Formulation',
                                   detectedDate          : 'Detected Date',
                                   aggReportStartDate    : 'Aggregate Start Date',
                                   aggReportEndDate      : 'Aggregate End Date',
                                   lastDecisionDate      : 'Last Decision Date',
                                   haSignalStatus        : 'HA Signal Status',
                                   haDateClosed          : 'HA Date Closed',
                                   commentSignalStatus   : 'Comments',
                                   referenceNumber       : 'Reference Name']

        List<Map> adhocConfiguration = cacheService.getAdhocConfigurationCache()
        // Define the names you want to filter
        Set<String> desiredNames = ["documentManagement", "disposition"] as Set

        // Create a new map with 'name' as the key and the corresponding map as the value, filtered by desiredNames
        Map<String, Map> filteredMap = adhocConfiguration.findAll { config ->
            desiredNames.contains(config.name)  // Keep only those maps whose name is in the desiredNames set
        }.collectEntries { config ->
            [(config.name): config]  // Create key-value pairs with name as key
        }

        Map<String, String> sectionLabelMap = cacheService.getAdhocSectionLabelMap(Constants.AdhocAlert.SECTION_LABEL_MAP)

        Map<String, String> status = [:]
        filteredMap.each { key, value ->
            if (value.enabled && (key == 'disposition')) {
                status[key] = value.label  // Add only if enabled
            }
        }

        def theDisplayAttributes = buildAttributes(displayedAttributes, alert)

        def theStatus = buildAttributes(status, alert)

        //TODO: Fix this attachment thing.
        def theAttachments = alert.getAttachments()

        Map priorityStatusAttr = [populationSpecific: 'PS']

        def priorityStatus = buildAttributes(priorityStatusAttr, alert)

        //Fetch the products and document types
        def alertDocuments = alertDocumentService.getUnlinkedDocuments()
        def productNames = alertDocuments.collect { it.productName }.findAll { it?.trim() }.unique()
        def documentTypes = alertDocuments.collect { it.documentType }.findAll { it?.trim() }.unique()
        def documentTypeList = alertAttributesService.get('documentType')
        def reviewDelay = true
        if (alert.dueIn() > 0) {
            reviewDelay = false
        }
        Boolean hasAdhocReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.AD_HOC_ALERT)
        String buttonClass = ""
        if(!hasAdhocReviewerAccess){
            buttonClass = Constants.ButtonClass.HIDDEN
        }
        if (!params.source)
            render(view: 'alert_detail', model: [alertInst             : alert,
                                                 rows                  : AlertHelper.composeDetailRowsForDisplay(alert,
                                                         grailsApplication.config.server.timezone, attributes),
                                                 attributes            : theDisplayAttributes,
                                                 statusAttributes      : theStatus,
                                                 attachments           : theAttachments,
                                                 documentTypeList      : documentTypeList?documentTypeList.join(", "):"",
                                                 productNames          : productNames ? productNames.sort({it.toUpperCase()}) : [""],
                                                 documentTypes         : documentTypes ? documentTypes.sort({it.toUpperCase()}): [""],
                                                 priorityStatus        : priorityStatus,
                                                 actionConfigList      : actionConfigList,
                                                 actionTypeList        : actionTypeAndActionMap.actionTypeList,
                                                 actionPropertiesMap   : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                                                 reasonForDelay        : alert.reasonForDelay ?: "",
                                                 reviewDelay           : reviewDelay,
                                                 hasAdhocReviewerAccess: hasAdhocReviewerAccess,
                                                 buttonClass           : buttonClass,
                                                 filteredMap           : filteredMap,
                                                 sectionLabelMap       : sectionLabelMap
            ])
        else
            redirect(uri: request.getHeader('referer'))
    }

    @Secured(['ROLE_AD_HOC_CRUD'])
    def findMatchedAlerts(String productName, String genericName, String topic, String event, Long alertId) {
        productName = productName ?: genericName
        String topicName = topic
        String eventSelection = event
        AdHocAlert currentAlert = AdHocAlert.findById(alertId)

        response.status = HttpStatus.SC_OK
        if (productName) {
            def names = productName.toLowerCase().tokenize(',')

            def alerts = adHocAlertService.findMatchedAlerts(names, topicName, eventSelection) - currentAlert
            render alerts.collect { it.details() } as JSON
        } else {
            render([] as JSON)
        }
    }

    private AdHocAlert buildAlert() {
        AdHocAlert alert = new AdHocAlert(priority: adHocAlertService.getDefaultPriority(),
                issuePreviouslyTracked: false)
        // Fix provided for PVS-56093: parameter was not set correctly
        alert.setIsPublic(true)
        alertAttributesService.addMissingProperties(alert, params)
        alert
    }

    def saveDelayReason(AdHocAlert adhocAlert, String reason) {
        adhocAlert.reasonForDelay = reason
        adhocAlert.save(flush: true)
        activityService.create(adhocAlert.id, ActivityType.findOrCreateByValue(ActivityTypeValue.ReasonForDelay),
                userService.getUser(), "Assessment Note Added: $reason", null)
        render([success: true] as JSON)
    }

    def saveComment(String selectedAdhocAlertIds, String comment) {
        Map result = adHocAlertService.saveComment(selectedAdhocAlertIds, comment)
        render(result as JSON)
    }

    def fetchComment(AdHocAlert adhocAlert) {
        User currentUser = userService.getUser()
        String timeZone = currentUser?.preference?.timeZone
        render([comment: adhocAlert.notes ? adhocAlert.notes : "", dateUpdated: adhocAlert.lastUpdatedNote ? DateUtil.toDateStringWithTime(adhocAlert.lastUpdatedNote, timeZone) : '', createdBy: adhocAlert?.noteModifiedBy] as JSON)
    }

    def changeFilterAttributes(){
        HttpSession session = request.getSession()
        session.removeAttribute("selectedAlertsFilter")
        if(params?.callingScreen == Constants.Commons.DASHBOARD) {
            session.setAttribute("adhocDashboard", params["selectedAlertsFilterForDashboard"])
        }else{
            session.setAttribute("adhoc",  params["selectedAlertsFilter"])
        }
        render([status:200] as JSON)
    }

    private boolean mandatoryCheck(List<Map> adhocConfiguration, Map params) {
        // Define the grouped fields for special handling
        Set<String> groupedFields = ["study", "studySelection", "productSelection"] as Set

        // Variables to track mandatory checks
        boolean groupedFieldPresent = false
        boolean areOtherFieldsInvalid = false
        boolean anyGroupedFieldMandatory = false

        // Iterate through adhocConfiguration to handle grouped fields and other mandatory fields in a single pass
        adhocConfiguration.each { config ->
            String fieldName = config?.name
            boolean isMandatory = config?.mandatory == true

            // Skip non-mandatory fields or fields without a name
            if (!isMandatory || !fieldName) {
                return // Skip to the next iteration of the `each` loop
            }

            // Check if the field is part of the grouped fields
            if (groupedFields.contains(fieldName)) {
                // If any grouped field has valid data, mark groupedFieldPresent as true
                if (params[fieldName]?.trim()) {
                    groupedFieldPresent = true
                }
                // Also track if any of the grouped fields is mandatory
                anyGroupedFieldMandatory = true
            } else {
                // Check if other mandatory fields are missing or empty
                if (!params[fieldName]?.trim()) {
                    areOtherFieldsInvalid = true
                }
            }
        }

        // If no grouped field has data, return true indicating failure
        if (anyGroupedFieldMandatory && !groupedFieldPresent) {
            return true
        }

        // Return true if any other mandatory fields are missing or invalid
        return areOtherFieldsInvalid
    }

    protected void configurationNotAcceptable() {
        request.withFormat {
            form {
                flash.message = message(code: 'configuration.not.found.message')
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_ACCEPTABLE }
        }
    }

}
