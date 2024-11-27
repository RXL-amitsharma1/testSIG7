package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentReference
import com.rxlogix.config.AlertType
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.dto.AlertDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.TriggeredReviewDTO
import com.rxlogix.security.Authorize
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.References
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.CustomUserDetails
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.AttachmentableUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import org.apache.commons.io.FilenameUtils
import org.joda.time.DateTime
import org.joda.time.Duration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.http.HttpServletResponse
import static com.rxlogix.util.DateUtil.toDateString
import com.rxlogix.util.DateUtil
import com.rxlogix.enums.TimeZoneEnum


@Secured(["isAuthenticated()"])
class DashboardController implements AlertUtil {

    def userService
    def alertService
    def actionService
    def dashboardService
    def evdasAlertService
    def adHocAlertService
    def singleCaseAlertService
    def validatedSignalService
    def aggregateCaseAlertService
    def productBasedSecurityService
    def pvsProductDictionaryService
    def cacheService
    def referencesService
    def CRUDService
    def preCheckService
    def groupService
    def messageSource

    def index() {
        User currentUser = userService.getUser()
        def dashboardWidgetConfigJSON = userService.user.preference.dashboardConfig
        def dashboardWidgetConfig = [:]
        if (dashboardWidgetConfigJSON) {
            def sluper = new JsonSlurper()
            dashboardWidgetConfig = sluper.parseText(dashboardWidgetConfigJSON)
            if(dashboardWidgetConfig["pvWidgetChart-7"]){
                dashboardWidgetConfig["pvWidgetChart-7"].content = [id: "assignedSignalTable", type: "pvDashReports"]
            }
            if(dashboardWidgetConfig["pvWidgetChart-13"]){
                dashboardWidgetConfig["pvWidgetChart-13"].content = [id: "systemValidationPreChecks", type: "pvDashPreChecks"]
            }
        } else {
            dashboardWidgetConfig = Holders.config.signal.dashboard.widgets.config.clone()
        }
        if (!currentUser?.isAggregateReviewer()) {
            dashboardWidgetConfig.remove("pvWidgetChart-3")
            dashboardWidgetConfig.remove("pvWidgetChart-10")
        }
        if (!currentUser?.isSingleReviewer()) {
            dashboardWidgetConfig.remove("pvWidgetChart-2")
            dashboardWidgetConfig.remove("pvWidgetChart-9")
        }
        if (!currentUser?.isSignalManagement()) {
            dashboardWidgetConfig.remove("pvWidgetChart-7")
            dashboardWidgetConfig.remove("pvWidgetChart-8")
        }
        if (!currentUser?.isSingleReviewer() && !currentUser?.isAggregateReviewer()) {
            dashboardWidgetConfig.remove("pvWidgetChart-4")
        }
        if (!currentUser?.isSingleReviewer() && !currentUser?.isAggregateReviewer() &&
        !currentUser.isEvdasReviewer() && !currentUser.isLiteratureReviewer()) {
            dashboardWidgetConfig.remove("pvWidgetChart-5")
        }
        if (!currentUser?.isAdhocEvaluator()) {
            dashboardWidgetConfig.remove("pvWidgetChart-1")
        }
        List userList = User.list().sort { it.fullName?.toLowerCase() }.collect {
            [id: it.id, fullName: it.fullName]
        }
        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        Map userViewAccessMap = actionService.getUserViewAccess()
        preCheckService.updatePreCheckTableSpaceTime()
        groupService.updateMenuVisibilityInSession(request);
        String aggDataUrl = Holders.config.signal.angular.app.base.url + Holders.config.signal.angular.app.alert.details
        [dashboardWidgetConfigJSON: dashboardWidgetConfig as JSON, userList: userList,
         showWidget               : (currentUser?.isHealthStatusReviewer() && precheckEnabled),
         actionTypeList           : actionTypeAndActionMap.actionTypeList,
         actionPropertiesMap      : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
         actionConfigList         : actionTypeAndActionMap.actionPropertiesMap.configs,
         referenceTypes           : Holders.config.signal.references.type,
         createdBy                : currentUser.getFullName(),
         userViewAccessMap        : userViewAccessMap,
         aggDataUrl               : aggDataUrl]
    }

    def caseByStatus() {
        List<Map> data = []
        try {
            data = dashboardService.generateReviewByStateChart(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(data as JSON)
    }

    def actionList() {
        List<Map> data = []
        try {
            Integer limit = Holders.config.get('pvsignal.action.item.widget.limit') as Integer
            data = actionService.listFirstXRowsByAssignedTo(userService.getUser(), false, limit)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(data as JSON)
    }

    def ahaByStatus() {
        List data = []
        try {
            data = adHocAlertService.byDisposition()
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(data as JSON)
    }

    def aggAlertByStatus() {
        List<Map> data = []
        try {
            data = dashboardService.generateReviewByStateChart(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(data as JSON)
    }

    def alertByDueDate() {
        List<List<Map>> data = []
        try {
           data = dashboardService.alertByDueDate()
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(data as JSON)
    }

    def sideBar() {
        Map<String, Integer> dashboardCountMap = [:]
        try {
            dashboardCountMap = dashboardService.dashboardCounts()
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(dashboardCountMap as JSON)
    }

    def sideBarByType() {
        Map<String, Integer> dashboardCountMap = [:]
        try {
            dashboardCountMap = dashboardService.dashboardCountsByType(params.case)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(dashboardCountMap as JSON)
    }

    def signalList() {
        Map resultMap = [:]
        try {
            resultMap = validatedSignalService.getClosedValidatedSignalList(params.signalType,params.selectedAlertsFilter)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(resultMap as JSON)
    }

    @Secured(['ROLE_HEALTH_CONFIGURATION'])
    def preCheckList() {
        Map<String, List<Map>> preCheckList = [:]
        Boolean isDataSame = false
        Map result = [:]
        String preCheckVal = ''
        def keyChecks = [:]
        Map keyMap = [:]
        try {
            preCheckVal = cacheService.getPreCheckCache()
            if (params.refresh == true || params.refresh == "true") {
                preCheckService.executeSystemConfigurationPrecheck()
            }
            preCheckList = preCheckService.preCheckList()
            keyChecks = preCheckList?.keySet()?.each{
                List keys = it.split('_')
                keyMap[keys[0]] = [keys[1],keys[2]]
            }
            if(preCheckVal?.equals(keyChecks as String)){
                isDataSame = true
            } else {
                cacheService.savePreCheckCache(keyChecks as String)
            }
            result = [data:preCheckList, isDataSame: isDataSame]
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render( result as JSON)
    }

    def topicList() {
        def user = userService.getUser()
        List<String> allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(user)
        def topics = Topic.list()?.findAll {
            def productNameList = it.getProductNameList()
            if (!(productNameList instanceof String) && allowedProductsToUser?.intersect(productNameList).size() > 0
                    && it.assignedTo == user && it.disposition.closed == false) {
                return true
            } else {
                return false
            }
        }
        topics = topics?.collect {
            it.toDto()
        }
        respond topics, [formats: ['json']]
    }

    def alertList() {
        Map resultMap = [:]
        try {
            AlertDTO alertDTO = createAlertDTO()
            resultMap = dashboardService.getFirstXExecutedConfigurationList(alertDTO)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render resultMap as JSON
    }

    AlertDTO createAlertDTO() {
        AlertDTO alertDTO = new AlertDTO()
        alertDTO.searchString = params.searchString
        alertDTO.max = params.getInt('length')
        alertDTO.offset =  params.getInt('start')
        alertDTO.sort = params.sort
        alertDTO.direction = params.direction
        alertDTO.selectedFilterValues = (params["selectedAlertsFilter"] == "null" || params["selectedAlertsFilter"] == "[null]" || params["selectedAlertsFilter"] == "" || params["selectedAlertsFilter"] == '[""]' || params["selectedAlertsFilter"] == null) ? [] : params["selectedAlertsFilter"]?.substring(1,params["selectedAlertsFilter"].length()-1).replaceAll("\"", "").split(",");
        alertDTO.singleCaseRole = hasViewerRoleAccess(Constants.AlertConfigType.SINGLE_CASE_ALERT, userService.getUser())
        alertDTO.aggRole = hasViewerRoleAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, userService.getUser())
        alertDTO.evdasRole = hasViewerRoleAccess(Constants.AlertConfigType.EVDAS_ALERT, userService.getUser())
        alertDTO.literatureRole = hasViewerRoleAccess(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT, userService.getUser())
        alertDTO
    }

    def fetchSchedulesConfigForTriggeredAlert(User currentUser) {
        def scheduledConfigs = []
        def configs = Configuration.findAllByNextRunDateIsNotNull()
        configs.each { Configuration configuration ->
            def nextRunDate = configuration.nextRunDate
            def currentDate = new Date()
            Date endDate = null
            use(TimeCategory) {
                endDate = currentDate + 6.weeks
            }

            if (endDate.after(nextRunDate)) {
                def schedultConfigObj = ["name"       : configuration.name,
                                         "product"    : configuration.productNameList,
                                         "dueIn"      : '-',
                                         "priority"   : configuration.priority.displayName,
                                         "id"         : configuration.id,
                                         "frequency"  : 'DAILY', //TODO: Need to add frequency
                                         "lastRunDate": '-',
                                         "nextRunDate": '-',
                                         "executed"   : false,
                                         "closed"     : "-"
                ]
                if (currentUser?.isSingleReviewer() && configuration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    schedultConfigObj.type = "Qualitative"
                } else {
                    schedultConfigObj.type = "Quantitative"
                }
                schedultConfigObj.total = '-'
                schedultConfigObj.new = '-'
                schedultConfigObj.requiresReview = '-'
                schedultConfigObj.assignedToMe = '-'
                scheduledConfigs.add(schedultConfigObj)
            }
        }
        scheduledConfigs
    }

    def getProductByStatus(String type) {
        Map<String, List> data = [:]
        try {
             data = dashboardService.generateProductsByStatusChart(type)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render(data as JSON)
    }

    def saveDashboardConfig(String dashboardWidgetsConfig) {
        dashboardService.saveDashboardConfiguration(dashboardWidgetsConfig)
        render true
    }

    def deleteTriggeredAlert(Long id, String type) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            dashboardService.deleteTriggeredAlert(id, type)
        } catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.dashboard.remove.alert.error")
        }
        render(responseDTO as JSON)
    }
    def addRefrence(){
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            referencesService.addReference(params,request)
        }catch(Exception ex){
            ex.printStackTrace()
            responseDTO.code=500;
            responseDTO.status=false;
            responseDTO.message = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
        }
        render(responseDTO as JSON)
    }
    def fetchAttachments() {
        List attachments = []
        Integer totalRecords = 0
        try {
            attachments = referencesService.fetchReferences(params)
            totalRecords = referencesService.getTotalRecords(params)
        }catch(Exception ex){
            ex.printStackTrace()
        }
        render([aaData: attachments, recordsTotal: totalRecords, recordsFiltered: attachments.size()] as JSON)
    }

    def download() {
        AttachmentReference attachment = AttachmentReference.get(params.id as Long)

        if (attachment) {
            File file = AttachmentableUtil.getFileForReference(grailsApplication.config, attachment)
            if (file.exists()) {
                String filename
                if(params.type == "assessment"){
                    filename = attachment.filename
                } else {
                    String extension = FilenameUtils.getExtension(attachment.filename)
                    if(attachment.inputName && !attachment.inputName.contains(extension))
                        filename = attachment.inputName + "." + extension ?: attachment.filename
                    else
                        filename = attachment.inputName ?: attachment.filename
                }

                ['Content-disposition': "${params.containsKey('inline') ? 'inline' : 'attachment'};filename=\"$filename\"",
                 'Cache-Control': 'private',
                 'Pragma': ''].each {k, v ->
                    response.setHeader(k, v)
                }

                if (params.containsKey('withContentType')) {
                    response.contentType = attachment.contentType
                } else {
                    response.contentType = 'application/octet-stream'
                }
                file.withInputStream{fis->
                    response.outputStream << fis
                }
                return
            }
        }

        response.status = HttpServletResponse.SC_NOT_FOUND
    }
    def shareWith() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            def shareWithObjects=[];
            shareWithObjects=JSON.parse(params.shareWith)
            References refrence =References.findById( params.refId as Long)
            userService.bindSharedWithConfiguration(refrence, shareWithObjects, true, false, [:], null, true)
            if(refrence.shareWithGroup!=null && refrence.shareWithGroup?.isEmpty()==false)
            {
                refrence.deletedByUser=[];
            }
            if(refrence.shareWithGroup == []) {
                refrence.shareWithGroup = []
            }
            if(refrence.shareWithUser == []) {
                refrence.shareWithUser = []
            }
            CRUDService.update(refrence)
        } catch (Exception e) {
            e.printStackTrace()
        }
        render(responseDTO as JSON)
    }
    def delete() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)

        try {
            referencesService.deleteReferences(params.refId as Long);
          //  CRUDService.update(refrence)
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.code=500
            responseDTO.status=false
        }
        render(responseDTO as JSON)
    }
    def getSharedWith(){
        def response=[:]
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            response=referencesService.getSharedWith(params.refId as Long)
        }catch(Exception ex){
            ex.printStackTrace()
            responseDTO.code=500
            responseDTO.status=false
        }
        responseDTO.data=response
        render(responseDTO as JSON)
    }

    def dragAndDropRefrences() {
        def response = [:]
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            Map params = request.JSON
            List priorityList = JSON.parse(params.refIds)
            Boolean isPinned = params.isPinned
            priorityList = priorityList.collect { it.id }
            response = referencesService.updateRefrencesPriority(priorityList, isPinned ?: false)
        } catch (Exception ex) {
            ex.printStackTrace()
            responseDTO.code = 500
            responseDTO.status = false
        }
        responseDTO.data = response
        render(responseDTO as JSON)
    }

    def pinReferences() {
        def response = [:]
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            response = referencesService.pinReferences(params)
        } catch (Exception ex) {
            ex.printStackTrace()
            responseDTO.code = 500
            responseDTO.status = false
        }
        responseDTO.data = response
        render(responseDTO as JSON)
    }

    /**
     * Method to return Options Displayed on Top and Side Nav which are user specific based on Roles and Application Config.
     * This will be used by angular service.
     * @return JSON Response of Top Nav and Side Nav.
     */
    @Authorize
    String fetchTopNavAndSidePanelDetails() {
        Map<String, Object> response = [:]
        Map<String, Object> currentApplication = dashboardService.getApplicationInformation('PVS')
        List externalApplication = ['PVR']
        if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_DEV")){
            externalApplication.push('PVAdmin')
        }
        List<Map<String, Object>> installedApplications = []
        externalApplication.each { appName ->
            installedApplications.push(dashboardService.getApplicationInformation(appName))
        }
        Map<String, Object> applications = [
                current   : currentApplication,
                installed : installedApplications
        ]
        List<Map<String, Object>> settings = []
        dashboardService.getSettingsData(settings)
        List<Map<String, Object>> sidePanel = []
        dashboardService.getSidePanelData(sidePanel)
//        User user = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        User user = userService.getUser()
        def attrs = [:]
        attrs.date = user.lastToLastLogin
        attrs.timeZone = user.preference?.timeZone
        attrs.formatName = 'user.lastLogin.date.format'
        Boolean neverLoggedIn = user.lastToLastLogin ? false : true
        String userTimeZone = "${message(code: 'app.timezone.TZ.GMT')} " + TimeZoneEnum.values().find { it.timezoneId == attrs.timeZone}?.gmtOffset
        Map<String, Object> header = [
                applications: applications,
                username    : user?.fullName,
                lastLoggedIn: neverLoggedIn ? messageSource.getMessage('user.neverLoggedIn.before.label',null,Locale.ENGLISH) : DateUtil.fromDateToStringWithTimezone(user?.lastToLastLogin,messageSource.getMessage('user.lastLogin.date.format',null,Locale.ENGLISH),attrs.timeZone),
                userTimeZone: userTimeZone,
                setting     : settings
        ]
        Map<String, Object> result = [
                header: header,
                sidePanel : sidePanel
        ]

        response.put("result", result)
        render response as JSON
    }

}
