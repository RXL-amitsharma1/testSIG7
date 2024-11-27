package com.rxlogix

import com.rxlogix.enums.ReportFormat
import com.rxlogix.user.User
import com.rxlogix.enums.DictionaryTypeEnum
import grails.converters.JSON
import groovy.sql.GroovyRowResult
import org.hibernate.type.*
import com.rxlogix.dto.*
import com.rxlogix.signal.*
import com.rxlogix.util.*
import grails.async.Promise
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

import static grails.async.Promises.task
import com.rxlogix.config.*
import com.rxlogix.helper.NotificationHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import oracle.jdbc.driver.OracleTypes
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.context.MessageSource
import com.rxlogix.helper.LinkHelper
import org.springframework.transaction.annotation.Propagation

class CaseFormService implements LinkHelper  {

    def userService
    def dataSource_pva
    def alertService
    def singleCaseAlertService
    def cacheService
    def signalDataSourceService
    MessageSource messageSource
    def sqlGenerationService
    def dynamicReportService
    def customMessageService
    def notificationHelper
    def attachmentableService
    def emailService
    def productAssignmentImportService
    def signalExecutorService
    def signalAuditLogService
    def dataObjectService
    def caseInfoService

    void batchPersistGtt(CaseForm caseForm, Sql sql,String caseFormDataSource) {
        List<String> warnings = []
        List caseNums = caseForm?.caseNumbers?.split(',')
        List versionNums = caseForm?.versionNum?.split(',')
        List followUpNums = caseForm?.followUpNum?.split(',')
        List isDuplicates = caseForm?.isDuplicate?.split(',')
        List caseIds = caseForm?.caseIds?.split(',')
        String username = caseForm?.createdBy.username
        Map sectionRelatedInfo = [:]
        Map caseInfoMap = [:]
        Map reportDataExport = [:]
        Date initialDBDate = new Date()
        try {
            Map<String, Map> dataMap = [:]
            Map<String, Map> excelFullTextData = [:]
            Integer batchNum = 1
            Integer index = 0
            Integer count = 0
            if (caseForm) {
                User user = caseForm.createdBy
                String query = sqlGenerationService.insertGTTForBlindedDataForCaseDetail(user)
                sql.execute(query)
                caseIds.each {
                    sql.withBatch(Holders.config.signal.caseForm.batch.size) { stmt ->
                        if(caseFormDataSource == Constants.DataSource.FAERS || caseFormDataSource == Constants.DataSource.VIGIBASE) {
                            stmt.addBatch("INSERT INTO GTT_INP_CASE_DETAILS(ID,BATCH_NO,CASE_NUM,VERSION_NUM,MASTER_CASE,LST_INS_UPD_USER,LST_INS_UPD_DATE) VALUES('${caseForm?.id as String}',${batchNum},'${caseNums[index]}',${versionNums[index] as Integer},${isDuplicates[index] != '-1' && isDuplicates[index] == true ? 1 : 0},'${username}',TO_DATE('${sqlGenerationService.convertDateToSQLDateTime(new Date())}', '${sqlGenerationService.DATETIME_FMT_ORA}'))")
                        }
                        else{
                            stmt.addBatch("INSERT INTO GTT_INP_CASE_DETAILS(ID,BATCH_NO,CASE_NUM,VERSION_NUM,FOLLOWUP_NUM,MASTER_CASE,LST_INS_UPD_USER,LST_INS_UPD_DATE) VALUES('${caseForm?.id as String}',${batchNum},'${caseNums[index]}',${versionNums[index] as Integer},${followUpNums[index] as Integer},${isDuplicates[index] != '-1' && isDuplicates[index] == true ? 1 : 0},'${username}',TO_DATE('${sqlGenerationService.convertDateToSQLDateTime(new Date())}', '${sqlGenerationService.DATETIME_FMT_ORA}'))")
                        }
                        index++
                        count++
                    }
                    if (count == Holders.config.signal.caseForm.batch.size) {
                        callCaseFormProc(caseForm, sql, dataMap, batchNum, sectionRelatedInfo, caseInfoMap,excelFullTextData,caseFormDataSource)
                        batchNum++
                        count = 0
                        def dataSourceObj = signalDataSourceService.getReportConnection(caseFormDataSource)
                        sql = new Sql(dataSourceObj)
                    }
                }
            }
            if (count > 0) {
                callCaseFormProc(caseForm, sql, dataMap, batchNum, sectionRelatedInfo, caseInfoMap,excelFullTextData,caseFormDataSource)
            }
            Date finalDBDate = new Date()
            log.info("DB Side time taken " + (finalDBDate.time - initialDBDate.time) + "ms")
            Map commentsMap = [:]
            if(Holders.config.icr.comments.enable) {
                commentsMap = fetchGlobalComments(caseIds, versionNums)
            }
            Map caseIdsNumsMap = [:]
            Map caseNumsMap = [:]
            caseIds.eachWithIndex { value, idx ->
                caseIdsNumsMap.put(value, caseNums[idx] + "-" + versionNums[idx])
                caseNumsMap.put(value, caseNums[idx])
            }
            Map criteriaMap = [:]
            List<String> queryParameters = []
            String queryName = ''
            ExecutedConfiguration exConfig = caseForm?.executedConfiguration
            if (exConfig.executedAlertQueryId) {
                queryName = exConfig.alertQueryName
                if (exConfig.executedAlertQueryValueLists.size() > 0) {
                    exConfig.executedAlertQueryValueLists.each { eaqvl ->
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
            String productSelection = exConfig?.productSelection ? ViewHelper.getDictionaryValues(exConfig, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(exConfig, DictionaryTypeEnum.PRODUCT_GROUP)
            String productName = ""
            boolean isPVCM = dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
            if(exConfig.dataMiningVariable && exConfig.adhocRun){
                productName = exConfig.dataMiningVariable
                if(productSelection){
                    productName = productName + "(" + productSelection + ")"
                }
            } else {
                productName = productSelection
            }
            String drugRecordSelection = exConfig?.productSelection ? ViewHelper.getDictionaryValues(exConfig, DictionaryTypeEnum.PRODUCT, true) : ""

            criteriaMap.put(customMessageService.getMessage("app.label.alert.name"), exConfig?.name)
            criteriaMap.put(customMessageService.getMessage("app.label.description"), exConfig?.description ?: "")
            criteriaMap.put(customMessageService.getMessage("app.label.productSelection"), productName)
            criteriaMap.put((isPVCM ? customMessageService.getMessage("app.label.productDictionary.multi.substance") : customMessageService.getMessage("app.label.productDictionary.multi.ingredient")), (exConfig?.isMultiIngredient ? "Yes" : "No"))
            if (exConfig && exConfig.selectedDatasource == Constants.DataSource.PVA) {
                criteriaMap.put(customMessageService.getMessage("app.label.productDictionary.include.WHODrugs"), (exConfig.includeWHODrugs ? Constants.Commons.YES : Constants.Commons.NO))
                if (exConfig.includeWHODrugs && exConfig.productSelection) {
                    criteriaMap.put(customMessageService.getMessage("app.label.productDictionary.Drug.Record"), drugRecordSelection)
                }
            }
            criteriaMap.put(customMessageService.getMessage("app.label.eventSelection"), alertService.eventSelectionValue(exConfig) ?: "")
            criteriaMap.put(customMessageService.getMessage("app.label.queryName"), exConfig?.executedAlertQueryValueLists?.queryName?.join(',') ?: "")
            criteriaMap.put(customMessageService.getMessage("app.label.queryParameters"), exConfig.executedAlertQueryValueLists ? queryParameters?.join(', ') : "")
            String dateRangeType = "app.dateRangeType." + exConfig?.dateRangeType?.name()
            criteriaMap.put(customMessageService.getMessage("app.label.DateRangeType"), customMessageService.getMessage(dateRangeType))
            criteriaMap.put(customMessageService.getMessage("app.label.configuration.exclude.invalid.cases"), exConfig.excludeNonValidCases ? "Yes" : "No")
            criteriaMap.put(customMessageService.getMessage("reportCriteria.exclude.follow.up"), exConfig.excludeFollowUp ? "Yes" : "No")
            criteriaMap.put(customMessageService.getMessage("app.label.configuration.suspectProduct"), exConfig.suspectProduct ? "Yes" : "No")
            criteriaMap.put(customMessageService.getMessage("app.label.includeLockedCasesOnly"), exConfig.includeLockedVersion ? "Yes" : "No")
            criteriaMap.put(customMessageService.getMessage("app.label.case.series"), caseForm?.isFullCaseSeries ? exConfig.name : caseNums?.join(', '))
            criteriaMap.put(customMessageService.getMessage("app.label.configuration.study.cases"),exConfig.studyCases ? (exConfig?.studyDrugs?:Constants.Commons.BLANK_STRING) : "No")
            criteriaMap.put(customMessageService.getMessage("app.label.configuration.poi"), exConfig.onlyPoi? "Yes": "No")
            criteriaMap.put(customMessageService.getMessage("app.label.configuration.medication.ignore"),exConfig.ignoreStudyType? "Yes": "No" )
            String viewName = caseForm?.viewInstanceName + (caseForm?.advancedFilterName ? "/" + caseForm?.advancedFilterName : "")
            criteriaMap.put(customMessageService.getMessage("report.label.view.filter"), viewName)
            criteriaMap.put(customMessageService.getMessage("app.report.label.total.cases"), caseIds?caseIds.size():"0")
            String ownerName = caseForm?.createdBy?.fullName + " (" + caseForm?.createdBy.username + ")"
            def userInfoMap = ["userName": caseForm?.createdBy?.username, "userFullName": caseForm?.createdBy?.fullName,"isGenerate":"true"]
            criteriaMap.put(customMessageService.getMessage("app.label.reportGeneratedBy"), ownerName)
            String timeZone = caseForm?.createdBy?.preference?.timeZone
            criteriaMap.put(customMessageService.getMessage("app.label.case.form.created.date"), DateUtil.toDateStringWithTimeInAmFormat(exConfig.dateCreated, timeZone) + userService.getGmtOffset(timeZone))
            Map updatedCaseInfoMap = [:]
            List fullTextFields = []
            sectionRelatedInfo.findAll { it?.value?.isFullText }?.each { key, val -> fullTextFields.add(key) }
            caseInfoMap.each({
                reportDataExport[it.key] = []
                if (it.key in fullTextFields) {
                    it.value?.each { key, val ->
                        // This map will be used in report builder to fetch full text type and it's section name
                        Map tmpFullMap = [(key): [fieldType: Constants.CaseDetailFields.FULL_TEXT_FIELD, sectionName: it.key]]
                        updatedCaseInfoMap.put(key, tmpFullMap)
                    }
                } else {
                    updatedCaseInfoMap.put(it.key, it.value)
                }
            })
            reportDataExport.comments = []
            dataMap.each{ k,v ->
                caseInfoMap.each{i,j ->
                    if(v[i] && (!(i in fullTextFields))) {
                        v[i].each { it ->
                            it.put("Case Number", caseNumsMap.get(k))
                            reportDataExport[i].add(it)
                        }
                    }
                }
            }
            excelFullTextData.each{l,m ->
                caseInfoMap.each{i,j ->
                    if(m[i]){
                        m[i].each{it ->
                            it.put("Case Number", caseNumsMap.get(l))
                            reportDataExport[i].add(it)
                        }
                    }
                }
            }
            commentsMap?.each{ key, value ->
                Map newCaseCommentMap = [:]
                newCaseCommentMap = ["caseId":caseNumsMap.get(value[0]["caseId"] as String),"comments":value[0]["comments"]]
                reportDataExport.comments.add(newCaseCommentMap)
            }
            if(Holders.config.icr.comments.enable) {
                updatedCaseInfoMap.put(Constants.CaseDetailFields.COMMENTS, [(Constants.CaseDetailFields.COMMENTS): [fieldType: Constants.CaseDetailFields.COMMENT_FIELD, sectionName: Constants.CaseDetailFields.COMMENTS]])
            }
                String savedName = caseForm?.formName

            log.info("PDF Report generation Started")
            String outputFormat = ReportFormat.PDF.name()
            File filePDF = dynamicReportService.createCaseFormReport(dataMap, caseForm, criteriaMap, commentsMap, caseIdsNumsMap, updatedCaseInfoMap, sectionRelatedInfo, caseInfoMap, outputFormat,reportDataExport)
            String savedFileNamePDF = savedName + '.PDF'
            File sourceFolder = new File(Holders.config.signal.case.form.folder.save as String)
            if (!sourceFolder.exists()) {
                log.info("Base folder not found, creating it.")
                sourceFolder.mkdir()
            }
            productAssignmentImportService.moveFile(filePDF, Holders.config.signal.case.form.folder.save as String, savedFileNamePDF.replace(" ", "+"))
            log.info("PDF Report generation Completed")

            if(caseFormDataSource != Constants.DataSource.FAERS) {
                log.info("Word Report generation Started")
                outputFormat = ReportFormat.DOCX.name()
                File fileWord = dynamicReportService.createCaseFormReport(dataMap, caseForm, criteriaMap, commentsMap, caseIdsNumsMap, updatedCaseInfoMap, sectionRelatedInfo, caseInfoMap, outputFormat, reportDataExport)
                String savedFileNameWord = savedName + '.DOCX'
                productAssignmentImportService.moveFile(fileWord, Holders.config.signal.case.form.folder.save as String, savedFileNameWord.replace(" ", "+"))
                log.info("Word Report generation Completed")
            }

            log.info("Excel Report generation Started")
            outputFormat = ReportFormat.XLSX.name()
            File fileXLSX = dynamicReportService.createCaseFormReport(dataMap, caseForm, criteriaMap, commentsMap, caseIdsNumsMap, updatedCaseInfoMap, sectionRelatedInfo, caseInfoMap, outputFormat,reportDataExport)
            String savedFileNameXLSX = savedName + '.XLSX'
            productAssignmentImportService.moveFile(fileXLSX, Holders.config.signal.case.form.folder.save as String, savedFileNameXLSX.replace(" ", "+"))
            log.info("Excel Report generation Completed")

            updateCaseForm(caseForm, ReportExecutionStatus.COMPLETED, savedName,true)

            def criteriaListForAudit = criteriaMap.collect { k, v ->
                [label: k, value: v]
            }
            signalAuditLogService.createAuditForExport(criteriaListForAudit, exConfig.getInstanceIdentifierForAuditLog() +": ${caseForm?.formName}", Constants.AuditLog.SINGLE_REVIEW+": Case Form", null, "", userInfoMap)
            sendSuccessNotification(caseForm, caseForm.createdBy)
            sendNotificationMail(caseForm)


        } catch (Exception ex) {

            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    public Long saveCaseForm(def scaList, String filename, ExecutedConfiguration executedConfiguration, User user, def params) {
        CaseForm caseForm = new CaseForm()
        String caseNums
        String versionNums
        String followUpNums
        String isDuplicates
        String caseIds
        caseForm.formName = filename
        caseForm.executedConfiguration = executedConfiguration
        caseForm.createdBy = user
        caseForm.isFullCaseSeries = params.selectedCases ? false : true
        if (params.advancedFilterId) {
            caseForm.advancedFilterName = AdvancedFilter.get(params.advancedFilterId as Long)?.name
        }
        if (params.tempViewId) {
            caseForm.viewInstanceName = "Temporary View"
        } else {
            caseForm.viewInstanceName = ViewInstance.get(params.viewId as Long)?.name
        }
        if(scaList && scaList[0]){
            if(scaList[0].caseVersion == null){
                scaList[0].caseVersion = '-1'
            }
            if(scaList[0].isDuplicate == null){
                scaList[0].isDuplicate = '-1'
            }
        }
        caseNums = scaList*.caseNumber.join(",")
        versionNums = scaList*.caseVersion.join(",")
        followUpNums = scaList*.followUpNumber.join(",")
        isDuplicates = scaList*.isDuplicate.join(",")
        caseIds = scaList*.caseId.join(",")
        caseForm.caseNumbers = caseNums
        caseForm.versionNum = versionNums
        caseForm.followUpNum = followUpNums
        caseForm.isDuplicate = isDuplicates
        caseForm.executionStatus = ReportExecutionStatus.SCHEDULED
        caseForm.caseIds = caseIds
        caseForm.save(flush: true)
        return caseForm?.id
    }

    void generateCaseFormExport() {
        List<CaseForm> caseFormExecutingList = CaseForm.createCriteria().list {
            eq('executionStatus', ReportExecutionStatus.SCHEDULED)
            order("dateCreated", "asc")
            maxResults(1)
        }
        if (caseFormExecutingList) {
            CaseForm caseFormExecuting = caseFormExecutingList[0]
            log.info("Found Case Form " + caseFormExecuting?.formName + " to execute")
            updateCaseForm(caseFormExecuting, ReportExecutionStatus.GENERATING, null, false)
            String caseFormDataSource = ''
            if(caseFormExecuting?.executedConfiguration?.selectedDatasource){
                caseFormDataSource = caseFormExecuting.executedConfiguration.selectedDatasource
            }else{
                caseFormDataSource = Constants.DataSource.PVA
            }
            def dataSourceObj = signalDataSourceService.getReportConnection(caseFormDataSource)
            Sql sql = new Sql(dataSourceObj)
            batchPersistGtt(caseFormExecuting, sql,caseFormDataSource)
            sql?.close()
        }

    }

    void initializeGeneration(def params, User user) {
        Promise promise = task {
            fetchDataBackground(params, user)
        }
        promise.onError { Throwable th ->
            th.printStackTrace()
            sendErrorNotification(params, user)
            throw th
        }
    }

    @Transactional
    void fetchDataBackground(Map params, User user) {
        def scaList
        String timeZone = cacheService.getPreferenceByUserId(user?.id)?.timeZone
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.timeZone = timeZone
        alertDataDTO.userId = user?.id
        alertDataDTO.isFromExport = true

        if (params.tempViewId) {
            alertDataDTO.clipBoardCases = ClipboardCases.get(params.tempViewId as Long)?.caseIds?.tokenize(',')
        }
        if(params.isFaers){
            alertDataDTO.isFaers = params.getBoolean("isFaers", false)
        }
        params.isFaers = params.boolean("isFaers")
        ExecutedConfiguration ec = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
        if (params.selectedCases) {
            if(params.adhocRun.toBoolean()){
                alertDataDTO.domainName = SingleOnDemandAlert
            } else {
                if (params.isArchived)
                    alertDataDTO.domainName = alertService.generateDomainName(params.boolean('isArchived'))
                else
                    alertDataDTO.domainName = alertService.generateDomainName(false)
            }
            scaList = singleCaseAlertService.listSelectedAlerts(params.selectedCases, alertDataDTO.domainName, true)
        } else {
            Map filterMap = [:]
            if (params.filterList && params.filterList != "{}") {
                def jsonSlurper = new JsonSlurper()
                filterMap = jsonSlurper.parseText(params.filterList)
            }
            alertDataDTO.filterMap = filterMap
            Closure advancedFilterClosure
            advancedFilterClosure = alertService.generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
            alertDataDTO.executedConfiguration = ec
            alertDataDTO.execConfigId = ec?.id
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.orderColumnMap = [name: "lastUpdated", dir: "desc"]
            if(params.adhocRun.toBoolean()){
                alertDataDTO.domainName = SingleOnDemandAlert
                alertDataDTO.isCaseFormProjection = true
                scaList = alertService.generateAlertListForOnDemandRuns(advancedFilterClosure, alertDataDTO)
            } else {
                alertDataDTO.domainName = alertService.generateDomainName(params.boolean('isArchived'))
                List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)
                alertDataDTO.dispositionFilters = dispositionFilters
                List<Disposition> openDispositions = alertService.getDispositionsForName(alertDataDTO.dispositionFilters)
                scaList = alertService.generateAlertList(advancedFilterClosure, alertDataDTO, openDispositions)
            }
        }
        String filename = params.filename
        params.filename = filename?.replaceAll(Constants.CASE_FORM_FILE_NAME_REGEX, '_')?.replaceAll("\\\\","_")
        saveCaseForm(scaList, params.filename, ec, user, params)
    }

    void callCaseFormProc(CaseForm caseForm, Sql sql, Map dataMap, Integer batchNum, Map sectionRelatedInfo, Map caseInfoMap,Map excelFullTextData,String caseFormDataSource) {
        try {
            getCaseDetailsMap(caseForm, caseInfoMap, sql, dataMap, batchNum, sectionRelatedInfo,excelFullTextData,caseFormDataSource)

        } catch (Throwable th) {
            log.error(th.getMessage())
            th.printStackTrace()
        }
    }

    private void getCaseDetailsMap(CaseForm caseForm, Map caseInfoMap, Sql sql, Map dataMap, Integer batchNum, Map sectionRelatedInfo,Map excelFullTextData,String caseFormDataSource) {

        log.info("Started fetching values for batch " + batchNum)
        //Added for PII policy
        if (Constants.DataSource.PVA.equalsIgnoreCase(caseFormDataSource)) {
            DbUtil.executePIIProcCall(sql, Constants.PII_OWNER, Constants.PII_ENCRYPTION_KEY)
        }
        //end of pii policy
        Map map = [:]
        List sectionMapInfo = sql.rows("select * from case_details_section where flag_enable = 1 AND (SECTION_KEY != 'Versions' OR SECTION_KEY is null) ORDER BY SECTION_POSITION ASC").unique()
        if (!sectionRelatedInfo && sectionMapInfo) {
            sectionMapInfo.collectEntries { [it?.UD_SECTION_NAME ?: it?.SECTION_NAME, [sectionKey: it?.SECTION_KEY, isFullText: it?.IS_FULL_TEXT == 1]] }
                    ?.each { key, val -> sectionRelatedInfo.put(key, val) }
        }
        sectionMapInfo.each { GroovyRowResult eachSection ->
            try {
                Map tmpFieldMap = [:]
                String sectionNameStr = eachSection.SECTION_NAME
                String sectionNameUd = eachSection.UD_SECTION_NAME ?: eachSection.SECTION_NAME
                sql.call("{call pkg_module_case_details.p_dynamic_case_details(?,?,?,?)}", [eachSection.SECTION_NAME, batchNum, caseForm?.id, Sql.resultSet(OracleTypes.CURSOR)]) { presentCursor ->
                    Object metaData = presentCursor.invokeMethod("getMetaData", null)
                    int colCount = metaData?.getColumnCount()
                    List currentSectionList = sql.rows("SELECT * from case_details_field_mapping where SECTION_NAME = ${sectionNameStr}")
                    tmpFieldMap.put(sectionNameUd, [:])
                    currentSectionList.each {
                        tmpFieldMap.get(sectionNameUd).put(it.UI_LABEL, it.FIELD_VARIABLE)
                    }
                    //Have to populate UI label and field variable manually for case references because it is fixed.
                    if (sectionNameUd == "Case References") {
                        tmpFieldMap.put(sectionNameUd, [(Constants.CaseDetailUniqueName.REFERENCE_TYPE)  : Constants.CaseDetailUniqueName.REFERENCE_TYPE_VAL,
                                                        (Constants.CaseDetailUniqueName.REFERENCE_NUMBER): Constants.CaseDetailUniqueName.REFERENCE_NUMBER_VAL])
                    }
                    String caseId
                    while (presentCursor.next()) {
                        caseId = presentCursor.getString(Constants.CaseDetailFields.CASE_ID)
                        populateValueInMap(sectionNameUd, tmpFieldMap, map, caseId, colCount, metaData, presentCursor, caseInfoMap,caseFormDataSource)
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace()
            }
        }


        // this is the map which is processed after everything has been fetched
        List<String> sectionList = [ 'Dosage Regimen', 'Event Information', 'Cause Of Death Information', 'Product Event Information', 'Device Information', 'Device Problems',
                                    'Narrative', 'Patient Medical History', 'Lab Data', 'Case References', 'Study Information', 'Literature Information', 'Versions',
                                    'Additional Case Information', 'Product Reference Information', 'Case Information', 'Patient Information', 'Patient', 'Pregnancy Information',
                                    'Dose Information', 'Dosage Regimen', 'General',  'Product(s)', 'Batch Information', 'Event(s)', 'Death Details', 'Product Event Matrix',
                                    'Device(s)', 'Summary', 'Patient Past Drug History', 'Test(s)', 'Study', 'Literatures(s)']
        List fullTextFields = []
        sectionRelatedInfo.findAll { it?.value?.isFullText }?.each { key, val -> fullTextFields.add(key) }
        map.each { key, value ->
            Map valueMap = value
            dataMap.put(key, [:])
            excelFullTextData.put(key, [:])
            caseInfoMap.each {
                if (valueMap.containsKey(it.key)) {
                    if (it.key in fullTextFields) {
                        valueMap.keySet()
                        Set freeTextKeySet = it?.getValue()?.keySet()
                        freeTextKeySet?.each { String field ->
                            if (valueMap[it.key][0][field]) {
                                dataMap[key].put(field, [[(field): valueMap[it.key][0][field]]])
                            }
                        }
                        excelFullTextData[key].put(it.key, valueMap[it.key])
                    } else {
                        dataMap[key].put(it.key, valueMap[it.key])
                    }
                }
            }
            // Traverse sectionList and remove entries based on flag
            sectionList.each { section ->
                boolean ifValueExist = false

                if (valueMap[section]) {
                    valueMap[section].each { vmMap ->
                        vmMap.each { vmKey, vmValue ->
                            if (vmValue != null && vmValue.toString().trim().length() > 0) {
                                ifValueExist = true
                            } else {
                                vmValue = null
                            }
                        }
                    }
                }
                // Remove the section if no entry found
                if (!ifValueExist) {
                    dataMap[key].remove(section)
                }
            }
        }
    }

    void populateValueInMap(String sectionKey, Map tmpFieldMap, Map map, String caseId, Integer colCount, Object metaData, def presentCursor, Map caseInfoMap,String caseFormDataSource) {
        //TODO: check if need to improve for Event seriousness and Product Event Information
        Map tmpMap = [:]
        boolean addKeys = false
        if (!caseInfoMap.containsKey(sectionKey)) {
            addKeys = true
            caseInfoMap.put(sectionKey, [:])
        }
        Map tmpKeyMap = [:]
        (1..colCount).each {
            String colName = metaData?.getColumnName(it)
            String val = presentCursor ? presentCursor.getString(colName) : null
            if (!(colName in [Constants.CaseDetailFields.TENANT_ID, Constants.CaseDetailFields.SECTION,
                              Constants.CaseDetailFields.CASE_ID, Constants.CaseDetailFields.VERSION_NUM,
                              Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO,
                              Constants.CaseDetailFields.MASTER_CASE])) {
                if (addKeys) {
                    tmpKeyMap.put(colName, "")
                }
                if (colName == Constants.CaseDetailUniqueName.DOB_VAL && Holders.config.enable.show.dob == false) {
                    tmpMap.put(colName, "#########")
                } else {
                    tmpMap.put(colName, val)
                }
            }
        }

        if (addKeys) {
            caseInfoMap.put(sectionKey, tmpKeyMap)
        }
        Boolean isValPresent = false
        tmpMap.each { key, value ->
            if (value) {
                isValPresent = true
                return
            }
        }
        if (isValPresent) {
            if (map.containsKey(caseId)) {
                Map caseMap = map.get(caseId)
                if (caseMap.containsKey(sectionKey)) {
                    List sectionList = caseMap.get(sectionKey)
                    sectionList.add(tmpMap)
                    caseMap.put(sectionKey, sectionList)
                    map.put(caseId, caseMap)
                } else {
                    caseMap.put(sectionKey, [tmpMap])
                    map.put(caseId, caseMap)
                }
            } else {
                Map sectionMap = [:]
                sectionMap.put(sectionKey, [tmpMap])
                map.put(caseId, sectionMap)
            }
        }
    }

    private void prepareDataMap(GroovyResultSet case_info, Map caseInfoMap, Map dataMap, Map resultMap, String mapKey) {
        Map map = [:]
        Map reporterCompCausa = ["Reporter Causality": Holders.config.pvs.case.info.reporter.causality,
                                 "Company Causality" : Holders.config.pvs.case.info.company.causality]
        caseInfoMap.get(mapKey).each { key, value ->
            //Thus code is encapsulated in try catch so that it doesn't break.
            try {
                if (key == 'Event Seriousness' && Holders.config.custom.caseInfoMap.Enabled == false) {
                    String caseInfoString = (case_info ? case_info.getString("Seriousness Criteria") : null)
                    map[key] = caseInfoString && caseInfoString == "-/-/-/-/-/-/-" ? "No" : "Yes"
                } else if (mapKey == "Product Event Information" && key in reporterCompCausa && reporterCompCausa.get(key)) {
                    map[reporterCompCausa.get(key)] = case_info ? case_info.getString(key) : null
                } else {
                    map[key] = case_info ? case_info.getString(key) : null
                }
            } catch (Throwable th) {
                th.printStackTrace()
                log.error(th.getMessage())
            }
        }
        Boolean isValPresent = false
        map.each { key, value ->
            if (value) {
                isValPresent = true
                return
            }
        }
        if (isValPresent) {
            if (resultMap.containsKey(case_info?.getString("CASE_ID"))) {
                Map caseMap = resultMap.get(case_info?.getString("CASE_ID"))
                if (caseMap.containsKey(mapKey)) {
                    Map caseMapKey = resultMap.get(case_info?.getString("CASE_ID"))
                    List mapList = caseMapKey.get(mapKey)
                    mapList.add(map)
                    caseMapKey.put(mapKey, mapList)
                    resultMap.put(case_info?.getString("CASE_ID"), caseMapKey)
                } else {
                    Map caseMapKey = resultMap.get(case_info?.getString("CASE_ID"))
                    caseMapKey.put(mapKey, [map])
                    resultMap.put(case_info?.getString("CASE_ID"), caseMapKey)
                }
            } else {
                Map caseMap = [:]
                caseMap.put(mapKey, [map])
                resultMap.put(case_info?.getString("CASE_ID"), caseMap)
            }
        }
    }

    List getFiltersFromParams(Boolean isFilterRequest, def params) {
        List filters = []
        if (params.filters) {
            filters = Eval.me(params.filters)
        }
        if (params.dashboardFilter && (params.dashboardFilter == 'total' || params.dashboardFilter == 'new') && !isFilterRequest) {
            filters = Disposition.list().collect { it.displayName }
        } else if (params.dashboardFilter && params.dashboardFilter == 'underReview' && !isFilterRequest) {
            filters = Disposition.findAllByClosedNotEqualAndValidatedConfirmedNotEqual(true, true).collect {
                it.displayName
            }
        } else if (!isFilterRequest) {
            filters = Disposition.findAllByClosedAndReviewCompleted(false, false).collect { it.displayName }
        }
        filters
    }

    List listCaseForms(Long exConfigId) {
        Map result = [:]
        String userTimeZone = userService.user.preference.timeZone
        List<CaseForm> caseFormList = CaseForm.findAllByExecutedConfigurationAndExecutionStatus(ExecutedConfiguration.get(exConfigId), ReportExecutionStatus.COMPLETED)
        List<Map> resultMap = caseFormList.collect {
            [id: it?.id, reportName: it.formName, reportBy: it?.createdBy?.fullName, reportOn: DateUtil.stringFromDate(it?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, userTimeZone),excelGenerated:it?.excelGenerated]
        }
        return resultMap
    }

    Map fetchGlobalComments(List caseIds, List versionNums, Boolean isFaers=false) {
        if(caseIds) {
            def domain = isFaers? GlobalCaseCommentMapping."faers": GlobalCaseCommentMapping."pva"

            List globalComments = []
            caseIds.eachWithIndex { val, index ->
                def comment = domain.createCriteria().get {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property('caseId', 'caseId')
                        property('comments', 'comments')
                    }
                    sqlRestriction("case_id = ${val} AND version_num = ${versionNums[index]}")
                    order('commentSeqNum', 'desc')
                    maxResults(1)
                }
                if(comment?.comments){
                    globalComments.add(comment)
                }
            }

            return globalComments.groupBy { it.caseId }
        } else {
            return [:]
        }
    }

    @Transactional
    void sendErrorNotification(Map params, User user) {
        List messageArgs = [params.filename]
        String message = "app.signal.caseForm.report.failed"
        String type = "Case Form Report Failed"
        InboxLog inboxLog = new InboxLog(
                notificationUserId: user?.id,
                level: NotificationLevel.WARN,
                message: message,
                messageArgs: messageArgs,
                type: type,
                subject: messageSource.getMessage(message, messageArgs.toArray(), user.preference.locale),
                content: "<span>${type}</span>",
                createdOn: new Date(),
                inboxUserId: user.id,
                isNotification: true,
                detailUrl: params.adhocRun ? "sca_adhoc_reportRedirectURL" : "sca_reportRedirectURL")
        inboxLog.save(failOnError: true, flush: true)
        notificationHelper.pushNotification(inboxLog)

    }

    List listCaseFormNames(Long execConfigId) {
        ExecutedConfiguration exConfig = ExecutedConfiguration.get(execConfigId)
        List formNames = CaseForm.findAllByExecutedConfiguration(exConfig)*.formName
        return formNames
    }

    Map fetchFile(Long id,String outputFormat) {
        CaseForm caseForm = CaseForm.get(id)
        def isAdhoc=caseForm.executedConfiguration.isAdhocRun()
        String savedName = caseForm?.formName
        String alertName = caseForm.executedConfiguration.getInstanceIdentifierForAuditLog()
        String savedFileName = savedName + "."+ outputFormat
        File sourceFolder = new File(Holders.config.signal.case.form.folder.save as String)
        String savedFilePath = "${sourceFolder.absolutePath}/${savedFileName.replace(" ","+")}"
        File file = new File(savedFilePath)
        [file: file, filename: file.name, alertName: alertName,isAdhoc:isAdhoc as String, isLatest: caseForm?.executedConfiguration?.isLatest]
    }

    void sendSuccessNotification(CaseForm caseForm, User user) {
        ExecutedConfiguration ec = caseForm.executedConfiguration
        List messageArgs = [caseForm?.formName]
        String message = "app.signal.caseForm.report.success"
        String type = "Case Form Report Generated"
        InboxLog inboxLog = new InboxLog(
                notificationUserId: user?.id,
                level: NotificationLevel.INFO,
                message: message,
                messageArgs: messageArgs,
                type: type,
                subject: messageSource.getMessage(message, messageArgs.toArray(), user.preference.locale),
                content: "<span>${type}</span>",
                createdOn: new Date(),
                inboxUserId: user.id,
                isNotification: true,
                executedConfigId: ec.id,
                detailUrl: ec.adhocRun ? "sca_adhoc_reportRedirectURL" : "sca_reportRedirectURL")
        inboxLog.save(failOnError: true, flush: true)
        notificationHelper.pushNotification(inboxLog)

    }

    void sendNotificationMail(CaseForm caseForm) {
        ExecutedConfiguration ec = caseForm?.executedConfiguration
        def alertLink = createHref("singleCaseAlert", "details", ["configId": ec.id,"callingScreen" : "review"])
        File filePDF = fetchFile(caseForm?.id, ReportFormat.PDF.name()).file
        File fileWORD = fetchFile(caseForm?.id, ReportFormat.DOCX.name()).file
        File fileXLSX = fetchFile(caseForm?.id, ReportFormat.XLSX.name()).file
        List<Map> files = [[name: filePDF.name, file: filePDF],[name: fileWORD.name, file: fileWORD],[name: fileXLSX.name, file: fileXLSX]]
        String subject = "Case Form Export for Alert " + ec.name
        String dateRange = DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                " - " + DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeEndAbsolute)
        String pdfUrl = Holders.config.grails.serverURL + "/singleCaseAlert/downloadCaseForm?id=" + caseForm?.id + "&outputFormat=PDF"
        String wordUrl = Holders.config.grails.serverURL + "/singleCaseAlert/downloadCaseForm?id=" + caseForm?.id + "&outputFormat=DOCX"
        String excelUrl = Holders.config.grails.serverURL + "/singleCaseAlert/downloadCaseForm?id=" + caseForm?.id + "&outputFormat=XLSX"
        def totalFileLength = filePDF.length() + fileWORD.length() + fileXLSX.length()
        if (totalFileLength > Holders.config.signal.caseForm.max.size) {
            String message = "The case form report can be accessed via the following link , corresponding to alert " + ec.name + " The alert was generated for date range " + dateRange + "."
            emailService.sendNotificationEmail(['toAddress': [caseForm?.createdBy?.email],
                                                "inboxType": "Assigned To Change",
                                                'title'    : subject,
                                                'map'      : ["map"         : ['PDF Report Link': pdfUrl,'Word Report Link': wordUrl,'Excel Report Link': excelUrl],
                                                              "emailMessage": message,
                                                              "screenName"  : "alert",
                                                              "alertLink"   : alertLink]])
        } else {
            String message = "The case form report is attached with the email, corresponding to alert " + ec.name + " The alert was generated for date range " + dateRange + "."
            emailService.sendNotificationEmail(['toAddress': [caseForm?.createdBy?.email],
                                                "inboxType": "Assigned To Change",
                                                'title'    : subject,
                                                'map'      : [
                                                        "attachments" : files,
                                                        "emailMessage": message,
                                                        "screenName"  : "alert",
                                                        "alertLink"   : alertLink]])
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateCaseForm(CaseForm caseForm, ReportExecutionStatus status, String savedName = null,Boolean excel) {
        if (savedName) {
            CaseForm.executeUpdate("Update CaseForm set executionStatus = :status , savedName = :savedName, excelGenerated = :excel where id = :id", [status: status, id: caseForm.id, savedName: savedName,excel:excel])
        } else {
            CaseForm.executeUpdate("Update CaseForm set executionStatus = :status, excelGenerated = :excel where id = :id", [status: status, id: caseForm.id,excel:excel])
        }
    }

    Map createCaseInfoMap() {
        Map caseInfoMap = Holders.config.pvsignal.caseInformationMap_1

        if (Holders.config.custom.caseInfoMap.Enabled) {
            caseInfoMap = caseInfoMap + Holders.config.pvsignal.customAdditionalCaseInfoMap
        } else {
            caseInfoMap[Constants.CaseInforMapFields.CASE_INFORMATION].remove(Constants.CaseInforMapFields.PRIMARY_IND_INFORMATION)
            caseInfoMap[Constants.CaseInforMapFields.CASE_INFORMATION].remove(Constants.CaseInforMapFields.PRE_ANDA)
        }

        caseInfoMap = caseInfoMap + Holders.config.pvsignal.caseInformationMap_2

        if (Holders.config.custom.caseInfoMap.Enabled) {
            caseInfoMap = caseInfoMap + Holders.config.pvsignal.customProductRefCaseInfoMap
        }
        caseInfoMap = caseInfoMap + Holders.config.pvsignal.caseInformationMap_3

        if (Holders.config.custom.caseInfoMap.Enabled) {
            caseInfoMap = caseInfoMap + Holders.config.pvsignal.customReporteCaseInfoMap
            caseInfoMap[Constants.CaseInforMapFields.EVENT_INFORMATION].remove("Onset Latency")
            caseInfoMap[Constants.CaseInforMapFields.PE_INFORMATION].remove("Time To Onset (Days)")
            caseInfoMap[Constants.CaseInforMapFields.DOSAGE_INFORMATION].remove("Daily Dose")
        }

        return caseInfoMap
    }

}
