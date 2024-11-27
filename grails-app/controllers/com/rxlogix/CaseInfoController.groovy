package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.dto.CaseDataDTO
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.Justification
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.http.util.TextUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.testng.util.Strings
import com.rxlogix.json.JsonOutput
import grails.util.Holders
import com.rxlogix.signal.ArchivedSingleCaseAlert

@Secured(["isAuthenticated()"])
class CaseInfoController implements AlertUtil {

    def caseInfoService
    def dataSource_pva
    def dynamicReportService
    def userService
    def workflowRuleService
    def priorityService
    def validatedSignalService
    def safetyLeadSecurityService
    def caseHistoryService
    def actionService
    def alertCommentService
    def attachmentableService
    def alertService
    def dataObjectService
    def signalAuditLogService


    def index() {
    }

    def caseDetail(String caseNumber, String version, String followUpNumber, String alertId, Boolean isFaers, Boolean isVaers, Boolean isVigibase, Boolean isJader, Boolean isAdhocRun, Boolean isCaseDuplicate) {
        if(alertId == "null"){
            alertId = null
        }
        if (!isFaers) {
            isFaers = false
        }
        if (!isVaers) {
            isVaers = false
        }
        if (!isVigibase) {
            isVigibase = false
        }
        if (!isJader) {
            isJader = false
        }
        boolean isArchived = params.boolean('isArchived')
        isAdhocRun = isAdhocRun ? isAdhocRun : params.boolean('isAdhocRun')
        Boolean isCaseVersion = dataObjectService.getDataSourceMap(Constants.DbDataSource.IS_ARISG_PVIP)
        boolean isVersion = params.boolean('isVersion')?: false
        boolean isChildCase = params.getBoolean("isChildCase")
        boolean isStandalone = params.boolean('isStandalone')?: false
        boolean isSpotfire = params.boolean('isSpotfire')?: false

        if ((!TextUtils.isEmpty(caseNumber) && !TextUtils.isEmpty(version)) || (!TextUtils.isEmpty(caseNumber) && isChildCase)) {
            String duplicateCheck = params.get('isDuplicate')
            Long icaId = alertId ? alertId as Long: null
            def ica = isArchived ? ArchivedSingleCaseAlert.get(icaId) : SingleCaseAlert.get(icaId)
            if(isAdhocRun) {
                ica = SingleOnDemandAlert.get(icaId)
            }
            Map caseDetailMap = caseInfoService.getCaseDetailMap(caseNumber, version, followUpNumber, alertId, [], isFaers,
                    isVaers, isVigibase, isJader,false, isAdhocRun, isCaseDuplicate,isArchived, duplicateCheck, isVersion, false, false, isChildCase, isStandalone)
            Boolean hasSingleReviewerAccess = false
            String buttonClass = ""
            Boolean isSingleAlertScreen = false
            Boolean isCaseSeries = params.boolean("isCaseSeries")
            if (params.isSingleAlertScreen && params.boolean("isSingleAlertScreen")) {
                isSingleAlertScreen = true
                isCaseSeries = params.boolean("isCaseSeries")
                hasSingleReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.SINGLE_CASE_ALERT, isCaseSeries)
                buttonClass = hasSingleReviewerAccess ? "" : "hidden"
            } else if(params.isSpotfire && params.boolean("isSpotfire")){ // Assigned to and disposition change is allowed when isSpotfire is true
                hasSingleReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.SINGLE_CASE_ALERT, isCaseSeries)
                buttonClass = hasSingleReviewerAccess ? "" : "hidden"
            }
            String timezone = userService.getUser()?.preference?.timeZone
            List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone)
            Map<Disposition, ArrayList> dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
            List currentDispositionOptions = dispositionIncomingOutgoingMap[caseDetailMap.caseDetail.disposition]
            Boolean forceJustification = userService.user.workflowGroup?.forceJustification
            List<Map> availablePriorities = priorityService.listPriorityOrder()
            List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.SINGLE_CASE_ALERT)
            Boolean isPriorityChangeAllowed = !alertService.isProductSecurity() || safetyLeadSecurityService.allAllowedProductsForUser(userService.getCurrentUserId())?.contains(caseDetailMap?.caseDetail?.productName)
            CaseDataDTO caseDataDTO = prepareCaseDataDTO()
            if(params.isFaers){
                caseDataDTO.isFaers = params.getBoolean("isFaers", false)
            }
            if(params.isVaers){
                caseDataDTO.isVaers = params.getBoolean("isVaers", false)
            }
            if(params.isVigibase){
                caseDataDTO.isVigibase = params.getBoolean("isVigibase", false)
            }
            if(params.isJader){
                caseDataDTO.isJader = params.getBoolean("isJader", false)
            }
            Map fullCaseListData = caseInfoService.getFullCaseListData(caseDataDTO,isAdhocRun)
            Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()

            Long caseId = caseDetailMap?.caseDetail?.caseId ?: caseInfoService.fetchCaseId(caseNumber)
            Map categoryMap = caseInfoService.getGobalAndAlertSpecificCategoriesList(alertId, caseDetailMap.versionNum, isArchived, isAdhocRun, caseId)
            Long execConfigId = isCaseSeries ? (alertId != "" && alertId!=null ? ica?.aggExecutionId as Long : params.get('execConfigId') as Long)  : caseDetailMap?.caseDetail?.executedConfigId
            Map availableVersions = caseInfoService.fetchCaseVersions(execConfigId as String, caseId as String,
                    isCaseSeries, caseNumber, alertId, isAdhocRun, isArchived,isChildCase, caseDetailMap?.versionNum)
            List versionsList = availableVersions['versionsList'].clone()
            Map prevVersionMap = versionsList.size() > 1? versionsList[1] : versionsList[0]
            def prevVersionNum = prevVersionMap?.versionNum
            def prevFollowUpNum = prevVersionMap?.followUpNum
            Map currVersionMap = versionsList[0]
            def currVersionNum = currVersionMap?.versionNum
            def currFollowUpNum = currVersionMap?.followUpNum
            Integer followUp = prevFollowUpNum != null ? prevFollowUpNum : currFollowUpNum
            Boolean isAggAdhoc = false
            if (params.isAggregateAdhoc != null && params.boolean("isAggregateAdhoc") == true) {
                isAdhocRun = false
                isAggAdhoc = true
            }
            String caseVisibility = params.caseVisibility ?: ''
            caseDetailMap << [dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                              currentDispositionOptions           : currentDispositionOptions,
                              followUp                            : followUp,
                              forceJustification                  : forceJustification,
                              availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                              availablePriorities                 : availablePriorities,
                              actionConfigList                    : actionConfigList,
                              actionTypeList                      : actionTypeAndActionMap.actionTypeList,
                              actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                              isPriorityChangeAllowed             : isPriorityChangeAllowed,
                              fullCaseListData                    : fullCaseListData,
                              isAdhocRun                          : isAdhocRun,
                              absentValue                         : caseDetailMap.absentValue,
                              isArchived                          : isArchived,
                              hasSingleReviewerAccess             : hasSingleReviewerAccess,
                              buttonClass                         : buttonClass,
                              hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                              hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                              isSingleAlertScreen                 : isSingleAlertScreen,
                              isCaseSeries                        : isCaseSeries,
                              isVersion                           : isVersion,
                              significantFollowup                 : Holders.config.custom.caseInfoMap.Enabled? message(code: 'app.label.chooseVersion'):message(code: 'app.label.chooseFollowUp'),
                              isCaseSeries                        : isCaseSeries,
                              categoriesList                      : categoryMap.categoryList as JSON,
                              isCategoryEditable                  : categoryMap.isCategoryEditable,
                              isFoundAlertArchived                : categoryMap.isFoundAlertArchived,
                              foundAlertId                        : categoryMap.foundAlertId,
                              availableVersions                   : availableVersions['versionsList'],
                              isLastVersionPresent                : availableVersions['isLastVersionPresent'],
                              isArgusDataSource                   : availableVersions['isArgusDataSource'],
                              previousVersion                     : !isChildCase?availableVersions['previousVersion']:prevVersionNum,
                              previousFollowUp                    : !isChildCase?availableVersions['previousFollowUp']:prevFollowUpNum,
                              versionsList                        : versionsList?.sort{it.followUpNum},
                              saveCategoryAccess                  : checkAccessForCategorySave(Constants.AlertConfigType.SINGLE_CASE_ALERT),
                              oldFollowUp                         : !isChildCase?(params.oldFollowUp ?: followUpNumber):currFollowUpNum,
                              oldVersion                          : !isChildCase?(params.oldVersion ?: version):currVersionNum,
                              isPriorityEnabled                   : grailsApplication.config.alert.priority.enable,
                              isCommentsEditEnable                : Holders.config.icr.comments.enable, // added comments flag for PVS-63960
                              isVersion                           : isVersion,
                              isChildCase                         : isChildCase,
                              isFaers                             : isFaers,
                              isCaseVersion                       : isCaseVersion,
                              isStandalone                        : isStandalone,
                              showGenerateCioms                   : Holders.config.caseInfo.generateCioms.enabled,
                              isAggAdhoc                          : isAggAdhoc,
                              isSpotfire                          : isSpotfire,
                              caseVisibility                      : caseVisibility
            ]
            render(view: 'caseDetail', model: caseDetailMap)
        } else {
            render view: '/errors/errorCaseDetail'
        }
    }

    Boolean hasSingleReviewerAccess(){
        Boolean result = false
        if(SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER")){
            result = true
        }
        return result
    }

    def fetchTreeViewNodes(String alertType, String wwid, Long alertId, Boolean isFaers, Boolean isVaers, Boolean isVigibase, Boolean isAdhocRun, String absentValue, Boolean compareScreen) {
        List treeNodes = caseInfoService.getTreeViewNodes(alertType, wwid, alertId, isFaers, isVaers, isVigibase,  isAdhocRun, absentValue, compareScreen)
        render(treeNodes as JSON)
    }

    def exportCaseInfo(Boolean isFaers, Boolean isVaers, Boolean isVigibase, Boolean isJader, String caseNumber, Long alertConfigId, String productFamily, String version, String followUpNumber, Long alertId,Boolean isAdhocRun,
                       String isDuplicate, Boolean isArchived, Long exeConfigId, Boolean isVersion) {
        List exportList = []
        List<Map> caseAttachmentsList =[]
        List<Map> caseCommentsList = []
        List<Map> caseActionsList = []
        List<Map> caseHistoryList = []
        List suspectProductHistoryList = []

        if(alertConfigId){
            caseHistoryList = caseHistoryService.listCaseHistory(caseNumber, alertConfigId, isArchived, exeConfigId)?.collect { CaseHistory ch ->
                caseHistoryService.getCaseHistoryMap(ch)
            }
            if(!caseHistoryList){
                caseHistoryList = caseHistoryService.getDefaultHistoryMap()
            }

            suspectProductHistoryList = caseHistoryService.listSuspectCaseHistory(caseNumber, alertConfigId)?.flatten()?.collect { CaseHistory ch ->
                caseHistoryService.getCaseHistoryMap(ch)
            }
            if(!suspectProductHistoryList){
                suspectProductHistoryList = caseHistoryService.getDefaultHistoryMap()
            }
        }

        Integer caseType
        if (!Strings.isNullOrEmpty(isDuplicate)) {
            caseType = isDuplicate.equalsIgnoreCase('M') ? 1 : 0
        } else {
            def domain = isAdhocRun ? SingleOnDemandAlert : SingleCaseAlert
            caseType = domain.get(alertId)?.getIsDuplicate() ? 1 : 0
        }
        isVersion = isVersion?: false
        Map caseMultiMap = caseInfoService.getCaseInfoMap(caseNumber, version, followUpNumber, exportList,
                isFaers ?: false,isVaers ?: false, isVigibase ?: false,isJader ?: false, params?.boolean('evdasCase') ?: false, caseType, isVersion , [:], params?.boolean('isChildCase'),
                params?.boolean('isStandalone'),true)
        String versionNum = String.valueOf(caseMultiMap.versionNum)
        caseMultiMap.remove("versionNum")
        caseMultiMap.remove("childCaseType")
        caseMultiMap.remove("sectionNameList")
        caseMultiMap.remove("sectionRelatedInfo")
        caseMultiMap.remove("fieldRelatedInfo")

        caseMultiMap = caseInfoService.hasValues(caseMultiMap)

        File reportFile
        if (isAdhocRun) {
            params.isAdhocRun = true
        }
        if(!params?.getBoolean("isChildCase") && !params?.boolean('evdasCase') && !params?.boolean('isStandalone')) {
            caseAttachmentsList = caseInfoService.getAttachmentListMap(alertId)
            if (Holders.config.icr.comments.enable) { // added flag as part of PVS-63960
            caseCommentsList = alertCommentService.getUpdatedCommentMap(caseNumber, versionNum, isFaers)
            }
            caseActionsList = actionService.getActionListMap(alertId)
        }
        replaceValueInNestedMapOrList(caseMultiMap,"-",null)
        reportFile = dynamicReportService.createCaseDetailReport(caseMultiMap,
                new JRMapCollectionDataSource(caseAttachmentsList),
                new JRMapCollectionDataSource(caseCommentsList),
                new JRMapCollectionDataSource(caseActionsList),
                new JRMapCollectionDataSource(caseHistoryList),
                new JRMapCollectionDataSource(suspectProductHistoryList),
                params)

        def entityValueForAudit="Case Detail Report"
        def moduleName=""
        if(alertConfigId && !params?.boolean('evdasCase')){
            entityValueForAudit = ExecutedConfiguration.get(exeConfigId).getInstanceIdentifierForAuditLog() + ": (${caseNumber}(${followUpNumber}))"
            moduleName="Individual Case Review: Case Detail"
        }else if(exeConfigId){
            if (exeConfigId == -1L) {
                if (alertId) {
                    entityValueForAudit = EvdasAlert.get(alertId)?.getExecutedAlertConfiguration()?.getInstanceIdentifierForAuditLog() + ": (${caseNumber}(${getFollowUpNumberForEvdas(version as Integer)}))"
                }
                moduleName="EVDAS Review: Case Detail"
            } else if(ExecutedEvdasConfiguration.get(exeConfigId) == null){
                entityValueForAudit = ExecutedConfiguration.get(exeConfigId).getInstanceIdentifierForAuditLog() + ": (${caseNumber}(${followUpNumber}))"
                moduleName="Individual Case Review: Case Detail"
            } else {
                entityValueForAudit = ExecutedEvdasConfiguration.get(exeConfigId).getInstanceIdentifierForAuditLog() + ": (${caseNumber}(${getFollowUpNumberForEvdas(version as Integer)}))"
                moduleName="EVDAS Review: Case Detail"
            }
        }else{
            entityValueForAudit ="${caseNumber}(${followUpNumber})"
            moduleName="Individual Case Review: Case Detail"
        }
        renderReportOutputType(reportFile,params)
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,entityValueForAudit,moduleName,params,reportFile.name)
    }

    def caseDiff() {
        def caseNumber = params.caseNumber
        def followUpNumber = params.followUpNumber
        def caseDiff = caseInfoService.caseDifference(caseNumber, followUpNumber)
        render(caseDiff as JSON)
    }

    private renderReportOutputType(File reportFile,def params) {
        String reportName = "case detail" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

    def getCaseNarativeInfo() {
        def caseNarativeMap = caseInfoService.getCaseNarativeInfo(params.caseNumber, params.followUpNumber)
        render(caseNarativeMap as JSON)
    }

    def getFollowUpNumberForEvdas(Integer version) {
        int followUpNumber
        if (null == version || version == 1 || version == 0 ) {
            followUpNumber = 0
        } else {
            followUpNumber = version - 1
        }
        followUpNumber
    }

    def evdasCaseDetail(String caseNumber, String version, Integer alertId, String wwid,String exeConfigId) {
        if (!TextUtils.isEmpty(caseNumber) && !TextUtils.isEmpty(version) && !TextUtils.isEmpty(wwid)) {
            Map caseDetailMap = caseInfoService.getEvdasCaseDetailMap(wwid, caseNumber, version, alertId, [], version, Integer.parseInt(params.caseType))
            caseDetailMap.followUpNumber = getFollowUpNumberForEvdas(caseDetailMap.version as Integer)
            caseDetailMap.exeConfigId = exeConfigId
            render(view: 'evdasCaseDetailsFlexible', model: caseDetailMap)
        }
        else if (!TextUtils.isEmpty(caseNumber) && !TextUtils.isEmpty(version) && alertId) {
            Map caseDetailMap = caseInfoService.getEvdasCaseDetailMap(caseNumber, version, alertId, [])
            caseDetailMap.followUpNumber = getFollowUpNumberForEvdas(caseDetailMap.version as Integer)
            caseDetailMap.exeConfigId = exeConfigId
            render(view: 'evdasCaseDetail', model: caseDetailMap)
        } else if (!TextUtils.isEmpty(wwid) && alertId) {
            Map caseDetailMap = caseInfoService.getEvdasCaseDetailMap(wwid, alertId)
            caseDetailMap.followUpNumber = getFollowUpNumberForEvdas(caseDetailMap.version as Integer)
            caseDetailMap.exeConfigId = exeConfigId
            render(view: 'evdasCaseDetailsFlexible', model: caseDetailMap)
        } else {
            render view: '/errors/error404'
        }
    }

    CaseDataDTO prepareCaseDataDTO(){
        CaseDataDTO caseDataDTO = new CaseDataDTO()
        caseDataDTO.caseListString = params.fullCaseList
        if(params.fullCaseList){
            caseDataDTO.caseList = JSON.parse(params.fullCaseList)
        }
        caseDataDTO.caseNumber = params.caseNumber
        caseDataDTO.version = params.getInt('version')
        caseDataDTO.alertId = params.getLong('alertId')
        caseDataDTO.id = params.getLong('execConfigId')
        caseDataDTO.totalCount = params.getInt('totalCount')
        if (params.detailsParameters) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            caseDataDTO.detailsParameters = params.detailsParameters
            caseDataDTO.params = jsonSlurper.parseText(params.detailsParameters)
        }
        caseDataDTO
    }

    def compareVersions(String caseNumber, String followUpNumber, String alertId,Boolean isAdhocRun) {
        boolean isArchived = params.boolean('isArchived')
        isAdhocRun = isAdhocRun ? isAdhocRun : params.boolean('isAdhocRun')

        if((params.versionAvailable as Integer) < (params.versionCompare as Integer) || (params.followUpAvailable as Integer) < (params.followUpCompare as Integer)){
            String temp = params.versionAvailable
            params.versionAvailable = params.versionCompare
            params.versionCompare = temp

            temp = params.followUpAvailable
            params.followUpAvailable = params.followUpCompare
            params.followUpCompare = temp
        }

        if (!TextUtils.isEmpty(caseNumber)) {
            Boolean isCaseSeries = params.boolean("isCaseSeries")
            isAdhocRun = isCaseSeries ? false : isAdhocRun
            Map alertDetailMap = !isAdhocRun ? caseInfoService.getAlertDetailMap(alertId, caseNumber, params.version, followUpNumber,isArchived) : [:]
            Boolean hasSingleReviewerAccess = false
            Boolean isSingleAlertScreen = false
            if(params.isSingleAlertScreen && params.boolean("isSingleAlertScreen")) {
                isSingleAlertScreen = true
                isCaseSeries = params.boolean("isCaseSeries")
                hasSingleReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.SINGLE_CASE_ALERT, isCaseSeries)
            }
            List printData = []
            if(params.isArgusDataSource && params.boolean("isArgusDataSource")){
                printData[0] = " Follow-Up#${params.followUpAvailable} "
                if(params.followUpCompare == "0"){
                    printData[1] = " Initial "
                }else{
                    printData[1] = " Follow-Up#${params.followUpCompare} "
                }
            }else{
                printData[0] = " Version#${params.versionAvailable} "
                if(params.versionCompare == "1"){
                    printData[1] = " Initial "
                }else{
                    printData[1] = " Version#${params.versionCompare} "
                }
            }
            boolean isChildCase = params.getBoolean("isChildCase")

            Long caseId = alertDetailMap?.caseId ?: caseInfoService.fetchCaseId(caseNumber)
            List categoryMapVersionAvailable = caseInfoService.getGobalAndAlertSpecificCategoriesList(alertId,params.versionAvailable,isArchived, isAdhocRun, caseId)?.categoryList
            List categoryMapVersionCompare = caseInfoService.getGobalAndAlertSpecificCategoriesList(alertId,params.versionCompare,isArchived, isAdhocRun, caseId)?.categoryList
            List comparedCategories = caseInfoService.compareCategories(categoryMapVersionAvailable , categoryMapVersionCompare)
            Long execConfigId = isCaseSeries ? (alertId != "" ? SingleCaseAlert.get(alertId as Long)?.aggExecutionId as Long : params.get('execConfigId') as Long)  : alertDetailMap?.executedConfigId
            Map availableVersions = caseInfoService.fetchCaseVersions(execConfigId as String, caseId as String, isCaseSeries,
                    caseNumber, alertId, isAdhocRun, isArchived, isChildCase, params.versionAvailable)
            Map uniqueColumnMap = [:]
            Map returnedMap = caseInfoService.getUniqueColumns(uniqueColumnMap, params.boolean('isFaers'))
            String deviceInformation = returnedMap?.deviceInformation
            List freeTextField = returnedMap?.freeTextField
            List versionsList = availableVersions['versionsList'].clone()
            Map prevVersionMap = versionsList.size() > 1? versionsList[1] : versionsList[0]
            def prevVersionNum = prevVersionMap?.versionNum
            def prevFollowUpNum = prevVersionMap?.followUpNum

            alertDetailMap << [isAdhocRun             : isAdhocRun,
                               isArchived             : isArchived,
                               hasSingleReviewerAccess: hasSingleReviewerAccess,
                               isSingleAlertScreen    : isSingleAlertScreen,
                               isCaseSeries           : isCaseSeries,
                               versionAvailable       : params.versionAvailable,
                               versionCompare         : params.versionCompare,
                               followUpAvailable      : params.followUpAvailable,
                               followUpCompare        : params.followUpCompare,
                               comparedCategories     : comparedCategories as JSON,
                               printData              : printData,
                               availableVersions      : availableVersions['versionsList'],
                               isLastVersionPresent   : availableVersions['isLastVersionPresent'],
                               previousVersion        : !isChildCase?availableVersions['previousVersion']:prevVersionNum,
                               previousFollowUp       : !isChildCase?availableVersions['previousFollowUp']:prevFollowUpNum,
                               isArgusDataSource      : availableVersions['isArgusDataSource'],
                               uniqueMap              : uniqueColumnMap ? JsonOutput.toJson(uniqueColumnMap) : "[]",
                               oldFollowUp            : params.oldFollowUp,
                               oldVersion             : params.oldVersion,
                               deviceInformation      : deviceInformation,
                               freeTextField          : freeTextField as JSON,


            ]

            alertDetailMap << params
            alertDetailMap.remove('_csrf')
            alertDetailMap.isAdhocRun = isAdhocRun
            alertDetailMap.isArchived = isArchived
            alertDetailMap.isChildCase = isChildCase
            render(view: 'compareVersions', model: alertDetailMap)
        } else {
            render view: '/errors/errorCaseDetail'
        }
    }

    def fetchVersionsCaseDetail() {
        Map dataMap = caseInfoService.compareVersionInfo(params)
        render(dataMap as JSON)
    }

    def fetchOutcomeForVAERS(){
        StringJoiner joinOutcomes = new StringJoiner("/ ")
        Map outcomeMap = Holders.config.pvsignal.caseDetail.field.outcome.vaers as Map
        String abbreviatedOutcome = params.abbreviatedOutcome
        abbreviatedOutcome?.split('/').each { String outcome ->
            joinOutcomes.add(outcomeMap[outcome])
        }
        render joinOutcomes.toString()
    }

}
