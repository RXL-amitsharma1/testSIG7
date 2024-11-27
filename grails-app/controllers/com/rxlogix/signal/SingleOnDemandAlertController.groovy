package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.ExecutedAlertDateRangeInformation
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.SignalStrategy
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.ReportFormat
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.HttpSession

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class SingleOnDemandAlertController implements AlertController {

    def alertService
    def userService
    def dynamicReportService
    def CRUDService
    def validatedSignalService
    def cacheService
    def viewInstanceService
    def spotfireService
    def reportIntegrationService
    def reportFieldService
    def pvsGlobalTagService
    def singleOnDemandAlertService
    def singleCaseAlertService
    def dataObjectService
    def signalAuditLogService
    def caseNarrativeConfigurationService

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER','ROLE_VIEW_ALL'])
    def listConfig() {
        Map resultMap = [recordsFiltered: 0, recordsTotal: 0, aaData: []]
        try {
            HttpSession session = request.getSession()
            session.setAttribute("icrAdhoc",params["selectedAlertsFilter"])
            resultMap = generateResultMapForSingleCaseReview(resultMap, SingleOnDemandAlert)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(resultMap as JSON)
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def listByExecutedConfig(Long id) {
        Map resultMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: [], configId: id]
        try {
            Integer totalColumns = 28
            Map filterMap = alertService.prepareFilterMap(params, totalColumns)
            Map orderColumnMap = alertService.prepareOrderColumnMap(params)
            AlertDataDTO alertDataDTO = createAlertDataDTO(filterMap, orderColumnMap, SingleOnDemandAlert, true)
            if(params.tempViewId)  {
                ClipboardCases instance = ClipboardCases.get(params.tempViewId as Long)
                if(instance?.isFirstUse || instance?.isUpdated) {
                    alertDataDTO.params.advancedFilterId = null
                    alertDataDTO.params.queryJSON = null
                    alertDataDTO.params.filterMap = [:]
                }
                instance?.isFirstUse = false
                instance?.isUpdated = false
                alertDataDTO.clipBoardCases = instance?.caseIds?.tokenize(',')
                instance?.save()
            }
            Map filterCountAndList = alertService.getAlertFilterCountAndListOnDemandRuns(alertDataDTO)
            List fullCaseList = filterCountAndList.fullCaseList
            List resultList = filterCountAndList.resultList
            resultMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: filterCountAndList.totalFilteredCount, aaData: resultList, configId: id, fullCaseList: fullCaseList]
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(resultMap as JSON)
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def adhocDetails(Boolean isCaseDetailView) {
        // Initiate Ui labels from mart-- currently only added for safety
        cacheService.prepareUiLabelCacheForSafety()
        Boolean alertDeletionObject = false
        String alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND
        Long id = params.configId as Long
        if(params.callingScreen != Constants.Commons.DASHBOARD && id == -1){
            alertDeletionInProgress()
            return
        }
        ExecutedConfiguration exConfig = ExecutedConfiguration.findById(id)
        if(exConfig) {
            alertDeletionObject = alertService.isDeleteInProgress(exConfig?.configId as Long, exConfig?.type) ?: false
        }
        String dateRange = Constants.Commons.BLANK_STRING
        Boolean cumulative = false
        Boolean showDob=Holders.config.enable.show.dob
        Boolean isVaers = (params.getBoolean("isVaers") != null) ? params.getBoolean("isVaers") : false;
        List<Map> fieldList = []
        Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
        Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
        Boolean isCaseVersion = dataObjectService.getDataSourceMap(Constants.DbDataSource.IS_ARISG_PVIP)
        Boolean customFieldsEnabled = Holders.config.custom.qualitative.fields.enabled
        if (exConfig) {
            ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = exConfig?.executedAlertDateRangeInformation
            dateRange = DateUtil.toDateString(executedAlertDateRangeInformation?.dateRangeStartAbsolute) +
                    " - " +
                    DateUtil.toDateString(executedAlertDateRangeInformation?.dateRangeEndAbsolute)
        }else{
            notFound()
            return
        }
        if(params.callingScreen != Constants.Commons.DASHBOARD && alertDeletionObject){
            alertDeletionInProgress()
            return
        }
        List<User> userList = User.findAllByEnabled(true).sort { it.fullName?.toLowerCase() }
        User currentUser = cacheService.getUserByUserNameIlike(userService.getCurrentUserName())
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType,params.viewId as Long)
        Boolean isShareFilterViewAllowed = currentUser.isAdmin()
        Boolean isViewUpdateAllowed = viewInstance?.isViewUpdateAllowed(currentUser)
        fieldList = grailsApplication.config.signal.scaOnDemandColumnList.clone() as List<Map>
        fieldList = removeFDAColumns(customFieldsEnabled, fieldList)
        if(isCaseVersion && !params.boolean("isFaers")){
            fieldList.each{ it->
                if(it.name=='caseNumber'){
                    it.display = Constants.Commons.CASE_VERSION_NO
                }
            }
        }
        // change field list labels according to mart specification
        // change advance filter info according to Ui label, currently only change for SafetyDB
        // currently there is no datasource diff in adhoc single case alert
//        if(!params.boolean("isVigibase") && !params.boolean("isVaers") && !params.boolean("isFaers")){
        fieldList.each {
            if(rptToSignalFieldMap.get(it.name)!=null && rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))!=null){
                it.display = rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))?:it.display
            }
            if(it.get("name")=='suspectProductList' && rptToUiLabelMap.get('masterSuspProdAgg')!=null){
                it.display = rptToUiLabelMap.get('masterSuspProdAgg')?:it.display
            }
            else if(it.get("name")=='allPtList' && rptToUiLabelMap.get('masterPrefTermAll')!=null){
                it.display = rptToUiLabelMap.get('masterPrefTermAll')?:it.display
            }
            else if(it.get("name")=='ptList' && rptToUiLabelMap.get('masterPrefTermSurAll')!=null){
                it.display = rptToUiLabelMap.get('masterPrefTermSurAll')?:it.display
            }
            else if(it.get("name")=='primSuspProdList' && rptToUiLabelMap.get('masterPrimProdName')!=null){
                it.display = rptToUiLabelMap.get('masterPrimProdName')?:it.display
            }
            else if(it.get("name")=='conComitList' && rptToUiLabelMap.get('masterConcomitProdList')!=null){
                it.display = rptToUiLabelMap.get('masterConcomitProdList')?:it.display
            }
        }
//        }
        ClipboardCases clipboardCase = ClipboardCases.createCriteria().get{
            eq('user.id' , currentUser?.id)
            'or'{
                eq('isFirstUse' , true)
                eq('isUpdated', true)
            }

        }
        String tempViewPresent = clipboardCase?.id ?: (params.containsKey('tempViewId')?params.tempViewId:"false")
        Boolean isTempViewSelected = false
        if (params.containsKey("tempViewId")) {
            isTempViewSelected = true
        }
        Map indexMap = alertService.getSingleOnDemandFilterIndexes(customFieldsEnabled)
        Boolean hasSingleReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        String buttonClass = hasSingleReviewerAccess?"":"hidden"
        // also add columnLabelMap as in normal single case alert
        // create Map of signal Field to Ui label by mart, currently only if datasource is safety
        Map columnLabelMap = [:]
        // currently there no datasource specific fields in adhoc sca
//        if(!params.boolean("isVigibase") && !params.boolean("isVaers") && !params.boolean("isFaers")) {
        rptToSignalFieldMap.each {
            if (rptToUiLabelMap.get(it.value) != null) {
                columnLabelMap.put(it.key, rptToUiLabelMap.get(it.value))
            }
        }
//        }

        def adhocIcrHelpMap = Holders.config.single.adhoc.helpMap
        render view: 'adhocDetails', model: [executedConfigId        : id,
                                             backUrl                 : request.getHeader('referer'),
                                             appType                 : Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND,
                                             reportUrl               : reportIntegrationService.fetchReportUrl(exConfig),
                                             analysisFileUrl         : spotfireService.fetchAnalysisFileUrl(exConfig),
                                             callingScreen           : params.callingScreen,
                                             fixedColumnScaCount     : indexMap.fixedColumns,
                                             indexListSca            : JsonOutput.toJson(indexMap.indexList),
                                             name                    : exConfig.name,
                                             dateRange               : dateRange,
                                             strategyList            : SignalStrategy.findAll(),
                                             userList                : userList, cumulative: cumulative,
                                             isCaseDetailView        : isCaseDetailView,
                                             viewInstance            : viewInstance ?: null,
                                             viewId                  : viewInstance ? viewInstance.id : "",
                                             alertType               : alertType,
                                             fieldList               : fieldList.sort({it.display.toUpperCase()}),
                                             isShareFilterViewAllowed: isShareFilterViewAllowed,
                                             isViewUpdateAllowed     : isViewUpdateAllowed,
                                             filterMap               : viewInstance ? viewInstance.filters : "",
                                             columnIndex             : "",
                                             sortedColumn            : viewInstance ? viewInstance.sorting : "",
                                             advancedFilterView      : viewInstance?.advancedFilter ? alertService.fetchAdvancedFilterDetails(viewInstance.advancedFilter) : "",
                                             customFieldsEnabledAdhoc: customFieldsEnabled,
                                             analysisStatus          : spotfireService.fetchAnalysisFileUrlCounts(exConfig),
                                             tempViewPresent         : tempViewPresent,
                                             isTempViewSelected      : isTempViewSelected,
                                             clipboardInterval       : Holders.config.pvs.details.copy.clipboard.interval,
                                             saveCategoryAccess      : checkAccessForCategorySave(Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND),
                                             hasSingleReviewerAccess : hasSingleReviewerAccess,
                                             buttonClass             : buttonClass,
                                             isCaseVersion           : isCaseVersion,
                                             showDob                 : showDob,
                                             columnLabelMap          : columnLabelMap,
                                             adhocIcrHelpMap         : adhocIcrHelpMap,
                                             exportAlways            : caseNarrativeConfigurationService.isExportAlwaysEnabled(),
                                             promptUser              : caseNarrativeConfigurationService.isPromptUserEnabled(),
                                             isVaers                 : isVaers
        ]
    }
    void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
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

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def exportReport() {
        Boolean exportCaseNarrative = checkCaseNarrativeConfiguration(params.outputFormat?.equalsIgnoreCase(ReportFormat.XLSX.name()), "true".equals(params.promptUser))
        params.isOnDemandExport = true
        Map filterMap = [:]
        if (params.filterList && params.filterList != "{}") {
            JsonSlurper jsonSlurper = new JsonSlurper()
            filterMap = jsonSlurper.parseText(params.filterList)
        }
        Map updatedFilterMap = [:]
        filterMap.each { def k, def v ->
            if(k == 'assessListedness'){
                updatedFilterMap.put('listedness', v)
            } else if(k == 'assessSeriousness'){
                updatedFilterMap.put('serious', v)
            } else {
                updatedFilterMap.put(k,v)
            }
        }
        filterMap = updatedFilterMap
        AlertDataDTO alertDataDTO = createAlertDataDTO(filterMap, [name: params.column, dir: params.sorting], SingleOnDemandAlert, true)
        if(params.tempViewId)  {
            alertDataDTO.clipBoardCases = ClipboardCases.get(params.tempViewId as Long)?.caseIds?.tokenize(',')
        }
        alertDataDTO.isFromExport = true
        alertDataDTO.exportCaseNarrative=exportCaseNarrative
        Map filterCountAndList = alertService.getAlertFilterCountAndListOnDemandRuns(alertDataDTO,false,params.selectedCases)
        filterCountAndList.resultList.each {
            List<String> tagsList = []
            it.alertTags.each { tag ->
                String tagString = ""
                if (tag.subTagText == null) {
                    tagString = tagString + tag.tagText + tag.privateUser + tag.tagType
                } else {
                    String subTags = tag.subTagText.split(";").join("(S);")
                    tagString = tagString + tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + "(S)"
                }
                tagsList.add(tagString)

            }
            it.alertTags = tagsList.join(", ")
            if (!it.containsKey('similarEvents')){
                it.put('similarEvents', '0')
            }
        }
        ExecutedConfiguration ec = ExecutedConfiguration.get(params.id as Long)
        params.totalCount = filterCountAndList?.resultList?.size()?.toString()
        List criteriaSheetList = singleCaseAlertService.getSingleCaseAlertCriteriaData(ec, params, Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND)
        params.criteriaSheetList = criteriaSheetList
        List exportList = filterCountAndList.resultList
        Integer maxSize = 1
        if (exportCaseNarrative) {
            exportList.each { map ->
                def caseN = map.find { it.key == 'caseNarrative' }?.value
                if (caseN) {
                    map.remove('caseNarrative')
                    List caseNarrativeList = splitCellContent(caseN as String)
                    maxSize = caseNarrativeList.size() > maxSize ? caseNarrativeList.size() : maxSize
                    Integer i = 1
                    caseNarrativeList.each { it ->
                        String key = "caseNarrative" + i
                        map.put(key, it)
                        i++
                    }
                }
            }
        }
        params << [
                exportCaseNarrative: exportCaseNarrative,
                maxSize            : maxSize
        ]
        Boolean isLongComment = exportList?.any({x -> x.comments?.size() > 100})
        params.isLongComment = isLongComment?: false
        File reportFile = dynamicReportService.createAlertsReport(new JRMapCollectionDataSource(exportList), params)
        renderReportOutputType(reportFile)
        signalAuditLogService.createAuditForExport(criteriaSheetList, ec.getInstanceIdentifierForAuditLog() + ": Alert Details", Constants.AuditLog.ADHOC_SINGLE_REVIEW, params, reportFile.name)
    }

    private renderReportOutputType(File reportFile) {
        String reportName = "Single On Demand Alert" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" +
                "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def adhocReview() {
        Map  singleAdhocReviewHelpMap =Holders.config.single.adhoc.review.helpMap

        render (view: "adhocReview",model:[singleAdhocReviewHelpMap:singleAdhocReviewHelpMap])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER'])
    def toggleFlag(Long id) {
        if (id) {
            Boolean flagged = alertService.toggleAlertFlag(id, SingleOnDemandAlert)
            render([success: 'ok', flagged: flagged] as JSON)
        } else {
            response.status = 404
        }
    }

    @Secured(['ROLE_SHARE_GROUP','ROLE_SHARE_ALL'])
    def editShare() {
        Boolean sharedWithoutException = true
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(Long.parseLong(params.executedConfigId))
        try {
            editShareWith()
        } catch (Exception e) {
            sharedWithoutException = false
            e.printStackTrace()
        }
        if(sharedWithoutException){
            flash.message = message(code: "app.configuration.share.success", args: [executedConfiguration?.name])
        }else{
            flash.warn = message(code: "app.configuration.share.warning", args: [executedConfiguration?.name])
        }
        redirect action: "adhocReview"
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map<String, List> possibleValuesMap = [:]
        Map possibleValuesMapFromConfig = Holders.config.advancedFilter.possible.values.map
        alertService.preparePossibleValuesMapForOnDemand(SingleOnDemandAlert, possibleValuesMap, executedConfigId)
        Map cachedReportFields = reportFieldService.getSelectableValuesForFields("en")
        possibleValuesMap.put("listedness", cachedReportFields.get("assessListedness"))
        possibleValuesMap.put("caseInitReceiptDate", "")
        possibleValuesMap.put("lockedDate", "")
        possibleValuesMap.put("serious", cachedReportFields.get("assessSeriousness"))
        possibleValuesMap.put("caseReportType", cachedReportFields.get("masterRptTypeId"))
        possibleValuesMap.put("reportersHcpFlag", cachedReportFields.get("CsHcpFlag"))
        possibleValuesMap.put("death", cachedReportFields.get("masterFatalFlag"))
        possibleValuesMap.put("rechallenge", [Constants.Commons.POSITIVE,Constants.Commons.NEGATIVE])
        possibleValuesMap.put("caseType", cachedReportFields.get("cmadlflagEligibleLocalExpdtd"))
        possibleValuesMap.put("compoundingFlag", cachedReportFields.get("vwcpai1FlagCompounded"))
        possibleValuesMap.put("dueIn", "")
        List<String> outcomes = singleCaseAlertService.getDistinctValues(SingleOnDemandAlert, executedConfigId, "outcome")
        possibleValuesMap.put("outcome", outcomes.unique{it.toUpperCase()})
        possibleValuesMap.put("currentRun", ["Yes", "No"])
        possibleValuesMap.put("malfunction", ["Yes", "No"])
        possibleValuesMap.put("comboFlag", ["Yes", "No"])
        possibleValuesMap.put("isSusar", ["Yes", "No"])
        possibleValuesMapFromConfig.each { key, value ->
            possibleValuesMap.put(key, cacheService.getPossibleValuesByKey(key))
        }
        ExecutedConfiguration exConfig = ExecutedConfiguration.get(executedConfigId)
        if(exConfig?.selectedDatasource ==  Constants.DataSource.VIGIBASE){
            List<String> ageList = singleCaseAlertService.getDistinctValues(SingleOnDemandAlert, executedConfigId, "age")
            possibleValuesMap.put("age", ageList.unique{it.toUpperCase()})
        } else{
            possibleValuesMap.put("age", cachedReportFields.get("patInfoAgeGroupId"))
        }
        List<String> genders = singleCaseAlertService.getDistinctValues(SingleOnDemandAlert, executedConfigId, "gender")
        possibleValuesMap.put("gender", genders.unique{it.toUpperCase()})
        List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags()
        codeValues.each{
            it.id = it.text
        }
        List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
        List<Map> subTags = pvsGlobalTagService.fetchSubTagsFromMart(codeValues)
        possibleValuesMap.put("tags" , tags.unique{it.text.toUpperCase()})
        possibleValuesMap.put("subTags" , subTags.unique{it.text.toUpperCase()})
        List customFields = [Constants.CustomQualitativeFields.APP_TYPE_AND_NUM,
                             Constants.CustomQualitativeFields.IND_NUM,
                             Constants.CustomQualitativeFields.CASE_TYPE,
                             Constants.CustomQualitativeFields.COMPLETENESS_SCORE,
                             Constants.CustomQualitativeFields.COMPOUNDING_FLAG,
                             Constants.CustomQualitativeFields.MED_ERR_PT_LIST,
                             Constants.CustomQualitativeFields.SUBMITTER,
                             Constants.CustomQualitativeFields.PRE_ANDA]

        if(!Holders.config.custom.qualitative.fields.enabled){
            customFields.each{customValue ->
                possibleValuesMap.remove{it.key == customValue}
            }
        }
        render possibleValuesMap as JSON
    }

    def fetchAllFieldValues() {
        List<Map> fieldValues = grailsApplication.config.signal.scaOnDemandColumnList.clone() as List<Map>
        List customFields = [Constants.CustomQualitativeFields.APP_TYPE_AND_NUM,
                             Constants.CustomQualitativeFields.IND_NUM,
                             Constants.CustomQualitativeFields.CASE_TYPE,
                             Constants.CustomQualitativeFields.COMPLETENESS_SCORE,
                             Constants.CustomQualitativeFields.COMPOUNDING_FLAG,
                             Constants.CustomQualitativeFields.SUBMITTER,
                             Constants.CustomQualitativeFields.PRE_ANDA,
                             Constants.CustomQualitativeFields.MED_ERR_PT_LIST,
                             Constants.CustomQualitativeFields.PAI_ALL_LIST,
                             Constants.CustomQualitativeFields.PRIM_SUSP_PAI_LIST,
                             Constants.CustomQualitativeFields.MED_ERRS_PT,
                             Constants.CustomQualitativeFields.CROSS_REFERENCE_IND
                            ]

        if(!Holders.config.custom.qualitative.fields.enabled){
            customFields.each{customValue ->
                fieldValues.removeAll{it.name == customValue}}
        }
        // check if this is used in advance filter label fetching
        Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
        Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
        // check if this is used in advance filter label fetching
        fieldValues.each {
            if(rptToSignalFieldMap.get(it.get("name"))!=null && rptToUiLabelMap.get(rptToSignalFieldMap.get(it.get("name")))!=null){
                it.display = rptToUiLabelMap.get(rptToSignalFieldMap.get(it.get("name")))?:it.display
            }
            if(it.get("name")=='suspectProductList' && rptToUiLabelMap.get('masterSuspProdAgg')!=null){
                it.display = rptToUiLabelMap.get('masterSuspProdAgg')?:it.display
            }
            else if(it.get("name")=='allPtList' && rptToUiLabelMap.get('masterPrefTermAll')!=null){
                it.display = rptToUiLabelMap.get('masterPrefTermAll')?:it.display
            }
            else if(it.get("name")=='ptList' && rptToUiLabelMap.get('masterPrefTermSurAll')!=null){
                it.display = rptToUiLabelMap.get('masterPrefTermSurAll')?:it.display
            }
            else if(it.get("name")=='primSuspProdList' && rptToUiLabelMap.get('masterPrimProdName')!=null){
                it.display = rptToUiLabelMap.get('masterPrimProdName')?:it.display
            }
            else if(it.get("name")=='conComitList' && rptToUiLabelMap.get('masterConcomitProdList')!=null){
                it.display = rptToUiLabelMap.get('masterConcomitProdList')?:it.display
            }
        }
        render fieldValues as JSON
    }

    def delete() {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(params.id as Long)
        if(executedConfiguration) {
            try {
                singleOnDemandAlertService.deleteOnDemandAlert(executedConfiguration)
                flash.message = message(code: "app.configuration.alert.delete.success", args: [executedConfiguration?.name])
            } catch (Exception e) {
                log.error(e.printStackTrace())
                flash.warn = message(code: "app.configuration.delete.fail", args: [executedConfiguration?.name])
            }
        }
        redirect(action: "adhocReview")
    }
}
