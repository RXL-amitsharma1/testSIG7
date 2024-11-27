package com.rxlogix.spotfire

import com.rxlogix.*
import com.rxlogix.audit.AuditTrail
import com.rxlogix.commandObjects.SpotfireCommand
import com.rxlogix.config.*
import com.rxlogix.dto.SpotfireSettingsDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.ProductClassification
import com.rxlogix.enums.SignalAssessmentDateRangeEnum
import com.rxlogix.json.JsonOutput
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.signal.SpotfireNotificationQuery
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.JsonUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SpotfireUtil
import com.rxlogix.json.JsonOutput
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDateTime
import org.springframework.transaction.annotation.Propagation
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import groovy.json.JsonBuilder


import java.sql.Clob
import java.sql.ResultSet
import java.text.SimpleDateFormat

class SpotfireService implements AlertUtil{

    def sessionFactory
    def dataSource_spotfire
    def grailsApplication
    def userService
    def grailsLinkGenerator
    def customMessageService
    def messageSource
    def userGroupService
    def pvsProductDictionaryService
    def productBasedSecurityService
    def singleCaseAlertService
    def notificationHelper
    EmailNotificationService emailNotificationService
    def dataSource_pva
    SqlGenerationService sqlGenerationService
    def signalDataSourceService
    def alertService
    CRUDService CRUDService
    def dataObjectService
    def reportExecutorService
    def aggregateCaseAlertService
    def signalAuditLogService
    def importConfigurationService
    def dataSource
    def configurationService

    def detailUrlMap = [(Constants.AlertConfigType.SINGLE_CASE_ALERT)   : [adhocRun: "sca_adhoc_reportRedirectURL", dataMiningRun: "sca_reportRedirectURL"],
                        (Constants.AlertConfigType.AGGREGATE_CASE_ALERT): [adhocRun: "aga_adhoc_reportRedirectURL", dataMiningRun: "aga_reportRedirectURL"],
                        (Constants.AlertConfigType.SIGNAL_MANAGEMENT): [validatedSignal: "validatedSignalRedirectURL"]]
    def tokenMap = ['token': '', 'time': '']

    def getSpotFireConfig() {
        grailsApplication.config.spotfire
    }

    Set<String> fileNameCache = Collections.synchronizedSet(new HashSet<String>())

    void getStatusOfSpotfireFileGeneration() {
            boolean status = false
            List<SpotfireJobInstances> jobListForSignal = SpotfireJobInstances.findAllByTypeAndExecutionStatus(Constants.AlertConfigType.SIGNAL_MANAGEMENT, Constants.SpotfireStatus.IN_PROGRESS)
            jobListForSignal?.each { spotfireJob ->
                spotFireReportProcessing(spotfireJob, status, true)
            }
            List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutionStatusAndSpotfireExecutionStatusInList(ReportExecutionStatus.COMPLETED,
                    [ReportExecutionStatus.GENERATING,ReportExecutionStatus.SCHEDULED])
            status = false
            executionStatusList?.each { exStatus ->
                List<SpotfireJobInstances> jobList = SpotfireJobInstances.findAllByExecutedConfigIdAndExecutionStatusInList(exStatus.executedConfigId as Long, [Constants.SpotfireStatus.IN_PROGRESS, Constants.SpotfireStatus.QUEUED])
                jobList?.each { spotfireJob ->
                    spotFireReportProcessing(spotfireJob, status)
                }
            }

            List<SpotfireJobInstances> jobList = SpotfireJobInstances.findAllByExecutionStatusInList([Constants.SpotfireStatus.IN_PROGRESS, Constants.SpotfireStatus.QUEUED])
            jobList?.each { spotfireJob ->
                spotFireReportProcessing(spotfireJob, status)
            }
    }

    protected void spotFireReportProcessing(SpotfireJobInstances spotfireJob, boolean status, boolean isSignal = false) {
        if (new Date() < (spotfireJob.dateCreated + 3)) {
            validateToken()
            spotfireJob.executionStatus = getSpotfireReportStatus(spotfireJob, tokenMap.token)
            spotfireJob.save(flush: true)
            if (spotfireJob.executionStatus == Constants.SpotfireStatus.FINISHED) {
                status = true
            }
            if (spotfireJob.executionStatus != Constants.SpotfireStatus.IN_PROGRESS) {
                if (isSignal) {
                    log.info("Spotfire Status Job found a report for notification (Signal Management) for signal id : " + spotfireJob.executedConfigId)
                } else {
                    log.info("Spotfire Status Job found a report for notification for executed configuration id : " + spotfireJob.executedConfigId)
                }
                spotfireReportNotification(spotfireJob.executedConfigId, spotfireJob.fileName, status)
            }
            status = false
        }
    }

    // fetch spotfire report status
    String getSpotfireReportStatus(SpotfireJobInstances spotfireJob, String access_token) {
        String url = grailsApplication.config.spotfire.automationServer
        String path = grailsApplication.config.spotfire.statusUri
        String port = grailsApplication.config.spotfire.automationPort
        String protocol = grailsApplication.config.spotfire.automationProtocol
        HttpContext httpContext = new BasicHttpContext()
        CloseableHttpClient httpClient = HttpClientBuilder.create().build()
        HttpGet statusUrl = new HttpGet("$protocol://$url:$port/$path/$spotfireJob.jobId")
        statusUrl.setHeader("Authorization", String.format("Bearer %s", access_token))
        HttpResponse response2 = httpClient.execute(statusUrl, httpContext)
        if (response2) {
            HttpEntity entity = response2.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JsonSlurper slurper = new JsonSlurper()
            def jsonRsp = responseString ? slurper.parseText(responseString) : null
            Boolean failedAtMailStatus = false
            failedAtMailStatus =  jsonRsp.Message && jsonRsp.Message.length() > 5 ? jsonRsp.Message.substring(0,6)?.trim() == 'Task 3':false
            if(jsonRsp.StatusCode == Constants.SpotfireStatus.FAILED && failedAtMailStatus)
                jsonRsp.StatusCode = Constants.SpotfireStatus.FINISHED
            return jsonRsp.StatusCode
        } else {
            return null
        }
    }

    void spotfireReportNotification(Long id, String filename, boolean status) {
        try {
            SpotfireNotificationQuery spotfireNotificationQuery = SpotfireNotificationQuery.findByFileNameAndExecutedConfigurationId(filename, id)
            if (spotfireNotificationQuery) {
                if (status) {
                    ExecutedConfiguration ec = ExecutedConfiguration.get(id)
                    ValidatedSignal validatedSignal = ValidatedSignal.get(id)
                    // previously un-handled signal for success notification handled
                    sendSuccessNotification(spotfireNotificationQuery, ec, validatedSignal)
                    if (spotfireNotificationQuery.type == "Signal Management") {
                        spotfireNotificationQuery.isEnabled = true
                        spotfireNotificationQuery.status = Constants.SpotfireStatus.FINISHED
                        spotfireNotificationQuery.save(flush: true)
                    }
                    List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutionStatusAndSpotfireExecutionStatusInListAndExecutedConfigId(ReportExecutionStatus.COMPLETED,
                            [ReportExecutionStatus.GENERATING, ReportExecutionStatus.SCHEDULED], id)
                    executionStatusList?.each {
                        it.spotfireExecutionStatus = ReportExecutionStatus.COMPLETED
                        CRUDService.saveWithoutAuditLog(it)
                    }
                    log.info('Notification Sent Successfully')
                } else {
                    List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutionStatusAndSpotfireExecutionStatusAndExecutedConfigId(ReportExecutionStatus.COMPLETED,
                            ReportExecutionStatus.GENERATING, id)
                    executionStatusList.each {
                        it.spotfireExecutionStatus = ReportExecutionStatus.ERROR
                        CRUDService.saveWithoutAuditLog(it)
                    }
                    spotfireNotificationQuery.status = Constants.SpotfireStatus.FAILED
                    spotfireNotificationQuery.save(flush:true)
                    ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(id)
                    if (!executedConfiguration) {
                        ValidatedSignal validatedSignal = ValidatedSignal.get(id)
                        sendErrorNotification(null, validatedSignal)
                    } else {
                        sendErrorNotification(executedConfiguration)
                    }
                    log.info('Notification Unsuccessful')
                }
            } else {
                List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutionStatusAndSpotfireExecutionStatusAndExecutedConfigId(ReportExecutionStatus.COMPLETED,
                        ReportExecutionStatus.GENERATING, id)
                executionStatusList.each {
                    it.spotfireExecutionStatus = ReportExecutionStatus.ERROR
                    CRUDService.saveWithoutAuditLog(it)
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
    }
    Integer getCountByListOfFileNames(String names) {
        Integer count
        Sql sql = null
        try {
            sql = new Sql(dataSource_spotfire)
            if (dataSource_spotfire) {
                sql.eachRow("$spotFireConfig.checkStatus and ll.TITLE in ($names)", [spotFireConfig.libraryFolder]) {
                    count = it.CNT
                }
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        } finally {
            sql?.close()
        }
        count
    }


    List<String> getReportFiles() {
        List<String> titles = []
        Sql sql = null
        try {
            sql = new Sql(dataSource_spotfire)
            if (dataSource_spotfire) {
                sql.eachRow("$spotFireConfig.query.sql.findReportFilesByTitle", [spotFireConfig.libraryFolder]) {
                    titles.push it.title
                }
                titles
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        } finally {
            sql?.close()
        }
        titles.sort()
    }

    List<String> getReportFilesByName(String name) {
        List<String> titles = []
        Sql sql = null
        try {
            sql = new Sql(dataSource_spotfire)
            if (dataSource_spotfire) {
                sql.eachRow("${spotFireConfig.query.sql.findReportFilesByTitle} and ll.TITLE IN  ($name)", [spotFireConfig.libraryFolder]) {
                    titles.push it.title
                }
                titles
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        } finally {
            sql?.close()
        }
        titles.sort()
    }

    List<String> getReportFilesDescriptionByName(String name) {
        List<String> descriptions = []
        Sql sql = null
        try {
            sql = new Sql(dataSource_spotfire)
            if (dataSource_spotfire) {
                sql.eachRow("${spotFireConfig.query.sql.findReportFilesByTitle} and ll.TITLE IN  (${name})", [spotFireConfig.libraryFolder]) {
                    descriptions.push it.description
                }
                descriptions
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        } finally {
            sql?.close()
        }
        descriptions.sort()
    }
    String createHref(String controller, String action, params){
        def url = [grailsLinkGenerator.serverBaseURL, controller, action].join("/")
        if(params){
            return url + "?" + params.collect { k,v -> "$k=$v" }.join('&')
        }
        return url
    }
    boolean deleteSfReports(List<String> descriptions){
        Sql sql =null
        boolean deleted=Boolean.FALSE
        try {
            sql = new Sql(dataSource_spotfire)
            String sfQuery=""
            descriptions.each {
                sql.execute("DELETE from lib_data where item_id in (select item_id from lib_items where DESCRIPTION = '${it}')")
                sql.execute("DELETE from lib_items WHERE DESCRIPTION= '${it}'")
            }
            sql.execute("Commit")
            deleted=Boolean.TRUE
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
        }
        finally {
            sql?.close()
        }
        return deleted
    }

    List<Map> getReportFilesMapData() {
        String userTimeZone = userService?.getUser()?.preference?.timeZone?:'UTC'
        List<Map> spoftfireFilesData = []
        Sql sql = null
        Integer lastIndex = -1
        try {
            sql = new Sql(dataSource_spotfire)
            if (dataSource_spotfire) {
                sql.eachRow("$spotFireConfig.query.sql.findReportFilesByTitle", [spotFireConfig.libraryFolder]) {
                    Boolean isFaersVaersDataSource = false
                    String faersVaersString = ""
                    lastIndex = it.title?.lastIndexOf('_')
                    if(lastIndex != -1){
                        faersVaersString = it.title.substring(0, lastIndex)
                    }
                    if(faersVaersString != ""){
                        isFaersVaersDataSource = faersVaersString.endsWith('Drug_Interval_faers_ex') || faersVaersString.endsWith('Drug_Cumulative_faers_ex') || faersVaersString.endsWith('Vaccine_Interval_vaers_ex') || faersVaersString.endsWith('Vaccine_Cumulative_vaers_ex')
                    }
                    Date dateCreated = DateUtil.parseDate(it.dateCreated.toString(), "$spotFireConfig.date.parseFormat")
                    Date dateAccessed = DateUtil.parseDate(it.dateAccessed.toString(), "$spotFireConfig.date.parseFormat")
                    Date lastUpdated = DateUtil.parseDate(it.lastUpdated.toString(), "$spotFireConfig.date.parseFormat")
                    spoftfireFilesData.add([fileName     : it.title,
                                            dateCreated  : DateUtil.toDateStringWithTime(dateCreated, userTimeZone),
                                            lastUpdated  : DateUtil.toDateStringWithTime(lastUpdated, userTimeZone),
                                            executionTime: it.executionTime,
                                            dateAccessed : (dateCreated && dateAccessed && dateAccessed > dateCreated) ? DateUtil.toDateStringWithTime(dateAccessed, userTimeZone) : "",
                                            isFaersVaersDataSource : isFaersVaersDataSource])
                }
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        } finally {
            sql?.close()
        }
        return spoftfireFilesData
    }

    def getUserByName(username) {
        Sql sql = null
        try {
            sql = new Sql(dataSource_spotfire)
            if (dataSource_spotfire) {
                def usernameFound = sql.firstRow("$spotFireConfig.query.sql.findByUserName", [username])
                if (usernameFound) return usernameFound.USER_NAME
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        } finally {
            sql?.close()
        }
        null
    }

    String getHashedValue(String username) {
        if (!spotFireConfig.secureAccess) {
            log.debug("###### Secure Access for Spotfire has not been activated ##########")
            return username
        }
        return username?.encodeAsMD5()
    }

    String getActualValue(String hashedUsername) {
        if (!spotFireConfig.secureAccess) {
            log.debug("###### Secure Access for Spotfire has not been activated ##########")
            return hashedUsername
        }
        if (!hashedUsername) {
            return null
        }
        List<String> usersNamesList = User.createCriteria().list {
            projections {
                property("username")
            }
        }
        return usersNamesList.find { it.encodeAsMD5() == hashedUsername }
    }

    String invokeReportGenerationAPI(configurationBlock , emailMessage, fileName, templatePath, List<String> recipients,
                                     String selectedDataSource, ExecutedConfiguration executedConfiguration, String fullFileName, ValidatedSignal validatedSignal) {
        try {
            Class.forName("com.rxlogix.spotfireclient.automation.JobExecutor",
                    true, Thread.currentThread().contextClassLoader)
        } catch (Throwable ex) {
            log.error(ex)
            return false
        }

        String analysisPath = "$spotFireConfig.analysisRoot/$templatePath"
        if(selectedDataSource && selectedDataSource == Constants.DataSource.FAERS){
            analysisPath = "$spotFireConfig.faersAnalysisRoot/$templatePath"
        }

        if(selectedDataSource && selectedDataSource == Constants.DataSource.VIGIBASE){
            analysisPath = "$spotFireConfig.vigibaseAnalysisRoot/$templatePath"
        }

        if(selectedDataSource && selectedDataSource == Constants.DataSource.VAERS){
            analysisPath = "$spotFireConfig.vaersAnalysisRoot/$templatePath"
        }

        //multi-ingredient-change for params
        Map params = [
                openTitle              : "$spotFireConfig.analysis.openTitle",
                AnalysisPath           : analysisPath,
                ConfigurationBlock     : configurationBlock,
                saveTitle              : "$spotFireConfig.analysis.saveTitle",
                LibraryPath            : "$spotFireConfig.libraryRoot/$fileName",
                EmbedData              : true,
                DeleteExistingBookmarks: false,
                Recipients             : recipients ?: [userService.getUser()?.email],
                emailTitle             : "$spotFireConfig.analysis.emailTitle",
                Subject                : "$spotFireConfig.emailSubject. File name: $fileName",
                EmailMessage           : emailMessage
        ]

        boolean  spotfireMailEnabled = emailNotificationService.mailHandlerForSpotfire()
        String xml = SpotfireUtil.composeXmlBodyForTask(params, spotfireMailEnabled)
        log.info("The request to spotfire server body is: \n" + xml)

        String resp = ''
        validateToken()
        if (Holders.config.spotfire.fileBasedReportGen) {
            log.info("PVR will generate spotfire report into a file folder ${Holders.config.spotfire.fileFolder}")
            try {
                File file = SpotfireUtil.generateAutomationXml(new File(Holders.config.spotfire.fileFolder), xml)
                if (file.exists()) {
                    log.info("File [${file.getAbsoluteFile()}] is generated")
                    return resp
                } else {
                    return null
                }
            } catch (Exception ex) {
                log.error("Failed generate spotfire report file", ex)
                ex.printStackTrace()
                return null

            }
        } else if (Holders.config.spotfire.automationNTLM) {
            log.info('NTLM will be executed')
            resp = SpotfireUtil.triggerJobOnNTML(
                    Holders.config.spotfire.automationServer as String,
                    Holders.config.spotfire.automationPort as Integer,
                    Holders.config.spotfire.automationProtocol as String,
                    xml as String,
                    Holders.config.spotfire.automationNTLMAcct as String,
                    Holders.config.spotfire.automationNTLMPass as String)

        } else {
            log.info('HTTP(s) will be executed')
            try {
                resp = SpotfireUtil.triggerJob(
                        Holders.config.spotfire.automationServer as String,
                        Holders.config.spotfire.automationPort as Integer,
                        Holders.config.spotfire.automationProtocol as String,
                        xml as String,
                        Holders.config.spotfire.automationUsername as String,
                        Holders.config.spotfire.automationPassword as String, tokenMap.token as String)
            }
            catch (Exception ex) {
                log.error("Exception while connecting to Spotfire")
                ex.printStackTrace()
            }
        }
        JsonSlurper slurper = new JsonSlurper()
        def jsonRsp = resp? slurper.parseText(resp) : null
        if (jsonRsp){
            SpotfireJobInstances spotfireJobInstance = new SpotfireJobInstances(jobId:jsonRsp.JobId, jobContent: xml, executedConfigId: executedConfiguration?.id, signalId: validatedSignal?.id , fileName: fullFileName)
            if(validatedSignal){
                spotfireJobInstance.executedConfigId = validatedSignal.id
                spotfireJobInstance.type = Constants.AlertConfigType.SIGNAL_MANAGEMENT
            }
            if (jsonRsp.StatusCode) {
                    spotfireJobInstance.executionStatus = jsonRsp.StatusCode
            }
            spotfireJobInstance.save(flush: true, failOnError: true)
        }
        log.info("Response from Spotfire Server is: " + resp)
        resp
    }
    // Check and fetch token if expired ( token expiry -> 2 hours)
    void validateToken() {
        try {
            Date token_time = tokenMap.time ? DateUtil.stringToDate(tokenMap.time?.toString(), DateUtil.DEFAULT_DATE_TIME_FORMAT, 'UTC') : null
            Date add2hours = null
            use(TimeCategory) {
                add2hours = token_time ? token_time + 2.hour : null
            }
            if (!tokenMap.token || (token_time && new Date() >= add2hours)) {
                String res = SpotfireUtil.getTokenForSpotfire(Holders.config.spotfire.automationProtocol as String, Holders.config.spotfire.automationServer as String,
                        Holders.config.spotfire.automationPort as Integer, Holders.config.spotfire.authUrl as String)
                JsonSlurper slurper = new JsonSlurper()
                def jsonRsp = res ? slurper.parseText(res) : null
                tokenMap.put('token', jsonRsp.access_token as String)
                tokenMap.put('time', new Date().format(DateUtil.DEFAULT_DATE_TIME_FORMAT) as String)

            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }
//    TODO in case of CaseSeriesId what to do?? Need to check with Awais.
    private String messageToSend(Set<String> productFamilyIds, Date fromDate, Date endDate, Date asOfDate, String fullFileName) {
        String products = ""
        if(productFamilyIds){
            products = LmProductFamily.getAllNamesForIds(productFamilyIds.toList()).join(" - ")
        }
        String emailMessage1 = customMessageService.getMessage('spotfire.email.message1', products)
        String emailMessage2 = customMessageService.getMessage('spotfire.email.message2', products ?: customMessageService.getMessage('app.label.NA'), convertDateForEmailFormat(fromDate), convertDateForEmailFormat(endDate), asOfDate ? convertDateForEmailFormat(asOfDate) : customMessageService.getMessage('app.label.NA'), fullFileName, DateUtil.StringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, userService.user?.preference?.timeZone ?: Constants.UTC))
        String emailMessage3 = customMessageService.getMessage('spotfire.email.message3', grailsLinkGenerator?.link(controller: 'dataAnalysis', action: 'index', absolute: true))
        String emailMessage = emailMessage1 + " \n \n" + emailMessage2 + " \n \n" + emailMessage3
        return emailMessage
    }

    String buildConfigurationBlock(String productFamilyIds, Date fromDate, Date endDate, Date asOfDate, Long executedCaseSeriesId, String type, int n,
                                   ExecutedConfiguration executedConfiguration, Boolean generatedFromAlert,String selectedDataSource = null,ValidatedSignal validatedSignal = null) {
        Boolean isLimitToCaseSeries = executedCaseSeriesId > 0
        if (isLimitToCaseSeries && !generatedFromAlert) {
            executedConfiguration = ExecutedConfiguration.findByPvrCaseSeriesId(executedCaseSeriesId)
        }
        String configBlock = (0..n).inject("") { str, i ->

            "$str${type}_p${i * 4 + 1}.prod_family=$productFamilyIds;" +
                    "${type}_p${i * 4 + 2}.start_date={\"${convertDateString(fromDate)}\"};" +
                    "${type}_p${i * 4 + 3}.end_date={\"${convertDateString(endDate)}\"};" +
                    "${type}_p${i * 4 + 4}.as_of_date={\"${convertDateString(asOfDate)}\"};"
        }
        configBlock += ((n * 4 + 5)..(n * 4 + 4 + 19 + (type == 'drug' ? 5 : 0))).inject("") { str, i ->
            "$str${type}_p${i}.case_list_id={\"${executedCaseSeriesId}\"};"
        }
        configBlock += "server_url_ip={\"${grailsApplication.config.pvreports.url}\"};"
        configBlock += "server_url={\"${grailsApplication.config.pvreports.ciomsI.export.uri}\"};"
        if (executedConfiguration) {
            configBlock += "FlagOpenCase={\"${executedConfiguration.includeLockedVersion ? 0 : 1}\"};"
            configBlock += "callback_url={\"${grailsLinkGenerator.link(base:grailsApplication.config.signal.serverURL,controller: "publicReport", action: "spotfireReportCallback", params: [id: executedConfiguration?.id])}\"};"
            if (selectedDataSource == Constants.DataSource.FAERS) {
                configBlock += "caseSeriesOwner={\"${alertService.getFaersDbUserName()}\"};"
            } else if (selectedDataSource == Constants.DataSource.VAERS) {
                configBlock += "caseSeriesOwner={\"${alertService.getVaersDbUserName()}\"};"
            } else if (selectedDataSource == Constants.DataSource.VIGIBASE) {
                configBlock += "caseSeriesOwner={\"${alertService.getVigibaseDbUserName()}\"};"
            } else {
                configBlock += "caseSeriesOwner={\"${Constants.PVS_CASE_SERIES_OWNER}\"};"
            }
            configBlock += "FILEGENERATEDFROM={\"${getFileGeneratedFromParameter(executedConfiguration?.type, selectedDataSource)}\"};"

        } else if (validatedSignal) {
            configBlock += "FlagOpenCase={\"${0}\"};"
            configBlock += "callback_url={\"${grailsLinkGenerator.link(base:grailsApplication.config.signal.serverURL,controller: "publicReport", action: "spotfireReportCallback", params: [id: validatedSignal?.id])}\"};"
            configBlock += "caseSeriesOwner={\"${Constants.PVS_CASE_SERIES_OWNER}\"};"
            configBlock += "FILEGENERATEDFROM={\"${getFileGeneratedFromParameter('Signal', selectedDataSource)}\"};"
        } else {
            configBlock += "FlagOpenCase={\"${0}\"};"
            configBlock += "caseSeriesOwner={\"${Constants.PVS_CASE_SERIES_OWNER}\"};"
        }
        configBlock += "file_generated_by={\"${userService.getUser()?.username}\"};"
        configBlock += "file_generated_fn={\"${userService.getUser()?.fullName}\"};"
        if(selectedDataSource == Constants.DataSource.FAERS || selectedDataSource == Constants.DataSource.VAERS) {
            configBlock += "PVSCRITERIA=${generatePVSAlertCriteria(executedConfiguration, selectedDataSource)};"
        }
        return configBlock
    }

    String generateReport(Set<String> productFamilyIds, Date fromDate, Date endDate, Date asOfDate, Long caseSeriesId, String type, String fullFileName, Boolean generatedFromAlert, ExecutedConfiguration executedConfiguration = null,
                          String selectedDataSource = null, ValidatedSignal validatedSignal = null, Boolean isWhoDrug = false, Set<String> whoFamilyIds = null) {
        log.info("Calling generateReport")
        fullFileName = fullFileName.replaceAll("[^a-zA-Z0-9\\-]", "_");
        String formattedProductFamilyIds = productFamilyIds ? "{\"${productFamilyIds?.join(",")}\"}" : "{\"\"}"
        String configurationBlock = buildConfigurationBlock(formattedProductFamilyIds, fromDate, endDate, asOfDate, caseSeriesId, type, 18, executedConfiguration, generatedFromAlert,selectedDataSource,validatedSignal)
        configurationBlock = "File_name={\"$fullFileName\"};" + "DataSource={\"$selectedDataSource\"};"  + configurationBlock
        if (selectedDataSource == Constants.DataSource.PVA) {
            configurationBlock = "LabelChange={\"PVA\"};" + configurationBlock
            // add code for who flag and who family ids
            configurationBlock = "FlagWho={\"${isWhoDrug?1:0}\"};" + configurationBlock
            configurationBlock = "WhoFamilyId={\"${whoFamilyIds ? whoFamilyIds?.join(",") : ""}\"};" + configurationBlock   //whoFamilyIds actually stores drug record numbers
        }
        String url = spotfireFileUrl(fullFileName,null,selectedDataSource)
        configurationBlock += configurationBlock + "SF_URL_Flag={\"${url}\"};"
        List<String> notificationRecipient = []
        if(executedConfiguration) {
            notificationRecipient = notificationRecipients(executedConfiguration).collect {
                it.email?.trim()
            }?.findAll { it }
        }

        if (validatedSignal) {
            notificationRecipient = notificationRecipients(validatedSignal).collect {
                it.email?.trim()
            }?.findAll { it }
        }

        if (caseSeriesId > 0 && executedConfiguration) {
            fromDate = caseSeriesId == executedConfiguration.pvrCumulativeCaseSeriesId ? Date.parse("yyyy-MM-dd", "1900-01-01") : executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute
            endDate = executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute
            asOfDate = executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF ? executedConfiguration?.asOfVersionDate : null
        }
        String emailMessage = messageToSend(productFamilyIds, fromDate, endDate, asOfDate, fullFileName)

        invokeReportGenerationAPI(configurationBlock, emailMessage, fullFileName, type == 'drug' ?
                spotFireConfig.drugPath : selectedDataSource == 'vaers' ? spotFireConfig.drugPath : spotFireConfig.vaccPath, notificationRecipient,selectedDataSource, executedConfiguration,fullFileName,validatedSignal)
    }

    void generate(SpotfireCommand spotfireCommand, ExecutedConfiguration executedConfiguration,String selectedDataSource) {
        log.info("The product family ids are : " + spotfireCommand.productFamilyIds.toString())
        log.info("Reporting Start Date is : " + spotfireCommand.fromDate)
        log.info("Reporting End Date is : " + spotfireCommand.endDate)
        log.info("As of Date is : " + spotfireCommand.asOfDate)
        log.info("Case Series Id is : " + spotfireCommand.caseSeriesId)
        log.info("Report Type is : " + spotfireCommand.type)
        log.info("File Full Name is : " + spotfireCommand.fullFileName.replaceAll("[^a-zA-Z0-9\\-]", "_"))
        if (executedConfiguration.includeWHODrugs) {
            log.info("The WHO Drug Flag is : " + spotfireCommand.isWHODrug)
            log.info("The WHO drug record numbers are : " + spotfireCommand.whoFamilyIds?.toString())
        }

        if (spotfireCommand.validate(failOnError: true)) {
            reserveFileName(spotfireCommand.fullFileName.replaceAll("[^a-zA-Z0-9\\-]", "_"))

            def respMsg = generateReport(
                    spotfireCommand.productFamilyIds,
                    spotfireCommand.fromDate,
                    spotfireCommand.endDate,
                    spotfireCommand.asOfDate,
                    spotfireCommand.caseSeriesId,
                    spotfireCommand.type,
                    spotfireCommand.fullFileName,
                    true, executedConfiguration,selectedDataSource, null,
                    spotfireCommand.isWHODrug, spotfireCommand.whoFamilyIds)

            JsonSlurper slurper = new JsonSlurper()

            def jsonRsp = respMsg? slurper.parseText(respMsg) : null
            if (!Holders.config.spotfire.fileBasedReportGen && (!jsonRsp || !jsonRsp?.JobId) )
                throw new Exception("Unknown response received from spotfire: " + respMsg)
        } else {
            spotfireCommand.errors.allErrors.each{
                println it
            }
            throw new IllegalArgumentException("spotfireCommand is not valid:" + spotfireCommand?.toJsonString())
        }
    }

    def logout(String username) {
        deleteSession(username)
    }

    @Transactional
    def deleteSession(String username) {
        List<SpotfireSession> sessions = SpotfireSession.findAllByUsername(username)
        if (sessions) {
            sessions.each {
                it.deleted = true
                it.save()
            }
        }
    }

    def findFileNameInDatabase(fileName) {
        fileName ? getReportFiles().any { it.toLowerCase() == fileName.toLowerCase() } : false
    }

    def findFileNameInCache(fileName) {
        fileNameCache.any { it.toLowerCase() == fileName.toLowerCase() }
    }

    def invalidFileNameLength(fileName) {
        fileName ? fileName.length() < 1 || fileName.length() > spotFireConfig.fileNameLimit : true
    }

    def fileNameExist(fileName) {
        (findFileNameInCache(fileName) || findFileNameInDatabase(fileName))
    }

    @Transactional
    void addAuthToken(String authToken, String username, String fullName, String email) {
        try {
            SpotfireSession spotfireSession = SpotfireSession.findByToken(authToken)
            if (!spotfireSession) {
                spotfireSession = new SpotfireSession(token: authToken,
                        username: username,
                        fullName: fullName,
                        email: email, timestamp: new Date())
            }
            spotfireSession.deleted = false
            spotfireSession.timestamp = new Date()
            spotfireSession.save(flush:true)
        } catch (Exception ex){
            log.error('Error While Updating or creating Spotfire Session.')
            ex.printStackTrace()
        }

    }

    SpotfireSession getSpotfireSessionInfo(String authToken) {
        SpotfireSession.findByTokenAndDeleted(authToken, false)
    }

    def reserveFileName(String fileName) {
        synchronized (this) {
            fileNameCache.add(fileName.toLowerCase())
        }

        Timer timer = new Timer(true)
        def cacheCleanInterval = spotFireConfig.fileNameCachedPeriod ?: 3600
        timer.schedule(new CacheUpdateTimerTask(fileName, timer), cacheCleanInterval * 1000)
    }

    def updateCache(String fileName) {
        fileNameCache.remove(fileName.toLowerCase())
    }

    class CacheUpdateTimerTask extends TimerTask {
        String fileName
        Timer timer

        CacheUpdateTimerTask(String fileName, Timer timer) {
            super()

            if (fileName)
                this.fileName = fileName
            else
                throw new IllegalArgumentException("File name can not be empty")

            this.timer = timer
        }

        void run() {
            updateCache(this.fileName)
            timer.cancel()
        }
    }

    private String convertDateString(Date date) {
        return date?.format("$spotFireConfig.date.xmlFormat")
    }

    private String convertDateForEmailFormat(Date date) {
        return date?.format(DateUtil.DATEPICKER_FORMAT)
    }

    List<Map> fetchCaseSeriesForSpotfire() {
        List<Map> executedConfigMapList = ExecutedConfiguration.createCriteria().list() {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property('pvrCaseSeriesId', 'id')
                property('name', 'name')
            }
            eq("isLatest", true)
            eq("type", Constants.AlertConfigType.SINGLE_CASE_ALERT)
            eq("adhocRun", false)
            eq("isCaseSeries", false)
            eq("isDeleted", false)
            eq("isEnabled", true)
            isNotNull('pvrCaseSeriesId')
        } as List<Map>
        executedConfigMapList

    }

    void sendSuccessNotification(SpotfireNotificationQuery notification, ExecutedConfiguration ec = null, ValidatedSignal vs = null) {
        Locale locale = userService.user?.preference?.locale
        List messageArgs = [notification.fileName]
        String type = "Analysis File Generated"
        String message = "app.signal.spotfire.report.saved"
        notification.notificationRecipients.each { User notificationRecipient ->
            InboxLog inboxLog = new InboxLog(
                    notificationUserId: notificationRecipient.id,
                    level: NotificationLevel.INFO,
                    message: message,
                    messageArgs: messageArgs,
                    type: type,
                    subject: messageSource.getMessage('app.notification.spotfire.completed', [notification.fileName, notification.configurationName].toArray(), locale),
                    content: "<span>${type}</span>",
                    createdOn: new Date(),
                    inboxUserId: notificationRecipient.id,
                    isNotification: true,
                    detailUrl: ec ? spotfireFileUrl(notification.fileName, null, ec.selectedDatasource): spotfireFileUrl(notification.fileName, null)
            )
            inboxLog.save(failOnError: true, flush: true)
            notificationHelper.pushNotification(inboxLog)
        }
        String dateRange = ""
        if(notification.type=='Signal Management'){
            dateRange = notification?.signalParameters?.split('@@@')?.getAt(2)?.replace('to', '-')
        }else{
            String[] parts = notification.fileName.split("_")
            dateRange = parts.length > 4 ? parts[parts.length - 4] : 'Interval'
        }
        signalAuditLogService.createAuditLog([
                entityName : Constants.AuditLog.typeToEntityMap.get(ec?.type) ?: "Signal",
                moduleName : ec ? (Constants.AuditLog.typeToEntityMap.get(ec?.type)+(ec.isLatest ? "" : ": Archived Alert")+": Data Analysis") : "Signal: Data Analysis",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: (ec ? ec.getInstanceIdentifierForAuditLog() : vs?.getInstanceIdentifierForAuditLog()) + "(Execution Completed)",
                description: "Generated ${notification.fileName}",
                username   : Constants.Commons.SYSTEM,
                fullname   : Constants.Commons.BLANK_STRING
        ] as Map, [[propertyName: ec?"Alert Name":"Signal Name", oldValue: "", newValue: ec?.name ?: vs?.name],
                   [propertyName: "File Name", oldValue: "", newValue: notification?.fileName],
                   [propertyName: "Date Range", oldValue: "", newValue: dateRange],
                   [propertyName: "Owner", oldValue: "", newValue: (ec?.owner?.fullName ?: User.findByUsername(vs.createdBy).fullName)]] as List)
    }

    void sendWarningNotification(SpotfireNotificationQuery notification) {
        List messageArgs = [notification.configurationName]
        String message = "app.notification.spotfire.warn"
        String inboxType
        if (notification.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            inboxType = "Quantitative Alert Execution"
        } else if (notification.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            inboxType = "Qualitative Alert Execution"
        }

        notification.notificationRecipients.each { User notificationRecipient ->
            InboxLog inboxLog = new InboxLog(
                    notificationUserId: notificationRecipient.id,
                    level: NotificationLevel.WARN,
                    message: message,
                    messageArgs: messageArgs,
                    type: inboxType,
                    subject: messageSource.getMessage(message, messageArgs.toArray(), notificationRecipient.preference.locale),
                    content: "<span>Analysis File Generated</span>",
                    createdOn: new Date(),
                    inboxUserId: notificationRecipient.id,
                    isNotification: true,
                    detailUrl: detailUrlMap[notification.type][notificationrunType])
            inboxLog.save(failOnError: true, flush: true)
            notificationHelper.pushNotification(inboxLog)
        }
    }

    void sendErrorNotification(ExecutedConfiguration executedConfiguration, ValidatedSignal validatedSignal = null) {
        List messageArgs = executedConfiguration ? [executedConfiguration?.name] : [validatedSignal?.name]
        String message = "app.signal.spotfire.report.failed"
        String type = "Analysis File Failed"
        List<User> notificationRecipientsList
        if(validatedSignal)
            notificationRecipientsList = notificationRecipientsForSignal(validatedSignal)
        else
            notificationRecipientsList = notificationRecipients(executedConfiguration)
        notificationRecipientsList?.each { User notificationRecipient ->
            InboxLog inboxLog = new InboxLog(
                    notificationUserId: notificationRecipient.id,
                    level: NotificationLevel.WARN,
                    message: message,
                    messageArgs: messageArgs,
                    type: type,
                    subject: messageSource.getMessage(message, messageArgs.toArray(), notificationRecipient.preference.locale),
                    content: "<span>${type}</span>",
                    createdOn: new Date(),
                    inboxUserId: notificationRecipient.id,
                    isNotification: true,
                    executedConfigId:executedConfiguration ? executedConfiguration.id: validatedSignal.id,
                    detailUrl: detailUrlMap[executedConfiguration ? executedConfiguration.type : Constants.AlertConfigType.SIGNAL_MANAGEMENT][executedConfiguration ? (executedConfiguration.adhocRun ? "adhocRun" : "dataMiningRun"):"validatedSignal"])
            inboxLog.save(failOnError: true, flush: true)
            notificationHelper.pushNotification(inboxLog)
        }
        signalAuditLogService.createAuditLog([
                entityName : executedConfiguration ? (Constants.AuditLog.typeToEntityMap.get(executedConfiguration.type)) : "Validated Signal",
                moduleName : executedConfiguration ? (Constants.AuditLog.typeToEntityMap.get(executedConfiguration.type)+(executedConfiguration.isLatest ? "" : ": Archived Alert")+": Data Analysis") : "Signal: Data Analysis",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: (executedConfiguration ? executedConfiguration.getInstanceIdentifierForAuditLog() : validatedSignal.getInstanceIdentifierForAuditLog()) + "(Execution Failed)",
                description: "Analysis File Failed",
                username   : Constants.Commons.SYSTEM,
                fullname   : Constants.Commons.BLANK_STRING
        ] as Map, [[propertyName: executedConfiguration?"Alert Name":"Signal Name", oldValue: "", newValue: executedConfiguration?.name ?: validatedSignal?.name],
                   [propertyName: "Owner", oldValue: "", newValue: (executedConfiguration ? executedConfiguration?.owner?.fullName : User.findByUsername(validatedSignal?.createdBy)?.fullName)]] as List)
    }

    String getFileName(ExecutedConfiguration executedConfiguration, ProductClassification type, DateRangeEnum range,String dataSource) {
        String rangeName = messageSource.getMessage(range.getI18nKey(), new Object[0], Locale.ENGLISH)
        //english locale for filename
        String fileName = executedConfiguration.name + "_"

        fileName = MiscUtil.getValidFileName(fileName?.replaceAll("[^a-zA-Z0-9\\-]", "_")?.replaceAll(Constants.SPOTFIRE_FILE_NAME_REGEX, '_')?.replaceAll("\\\\", "_").replaceAll(/^[\W_]+/, 'S_'), 160)
        fileName = fileName + type.getId() + "_" + rangeName.split(" ")[0] + "_" + dataSource + "_" + "ex_" + executedConfiguration.id
    }

    String getFileName(ExecutedConfiguration executedConfiguration, ProductClassification type, DateRangeEnum range) {
        String rangeName = messageSource.getMessage(range.getI18nKey(), new Object[0], Locale.ENGLISH)
        //english locale for filename
        String fileName = executedConfiguration.name + "_"
        fileName = MiscUtil.getValidFileName(fileName?.replaceAll("[^a-zA-Z0-9\\-]", "_")?.replaceAll(Constants.SPOTFIRE_FILE_NAME_REGEX, '_')?.replaceAll("\\\\", "_").replaceAll(/^[\W_]+/, 'S_'), 160)
        fileName = fileName + type.getId() + "_" + rangeName + "_" + "ex_" + executedConfiguration.id
    }

    String getExistingFileName(BaseConfiguration executedConfiguration, ProductClassification type, DateRangeEnum range) {
        String fileName = executedConfiguration.name + "_"
        fileName = MiscUtil.getValidFileName(fileName?.replaceAll("[^a-zA-Z0-9\\-]", "_")?.replaceAll(Constants.SPOTFIRE_FILE_NAME_REGEX, '_')?.replaceAll("\\\\", "_").replaceAll(/^[\W_]+/, 'S_'), 160)
        fileName = fileName + type.getId() + "_" + messageSource.getMessage(range.getI18nKey(), new Object[0], Locale.ENGLISH) +
                "_" + "v" + executedConfiguration.numOfExecutions
    }

    Map getSpotfireNames(ExecutedConfiguration executedConfiguration) {

        if (executedConfiguration?.spotfireSettings) {
            try {
                SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
                Map result = [:]
                settings.dataSource.each { String selectedDataSource ->
                    if (selectedDataSource.equals(Constants.DataSource.PVA)) {
                        settings.rangeType.each { it ->
                            Map reportFileData = getReportFileName(executedConfiguration,settings.type,it,"pva")
                            result.put(it, reportFileData.reportFiles ? URLEncoder.encode(reportFileData.fileName, "UTF-8") : '')
                        }
                    } else if(selectedDataSource.equals(Constants.DataSource.VIGIBASE)) {
                        settings.rangeType.each { it ->
                            Map reportFileData = getReportFileName(executedConfiguration,settings.type,it,"vigibase")
                            result.put(it, reportFileData.reportFiles ? URLEncoder.encode(reportFileData.fileName, "UTF-8") : '')
                        }
                    } else if(selectedDataSource.equals(Constants.DataSource.VAERS)) {
                        settings.rangeType.each { it ->
                            Map reportFileData = getReportFileName(executedConfiguration,settings.type,it,"vaers")
                            result.put(it, reportFileData.reportFiles ? URLEncoder.encode(reportFileData.fileName, "UTF-8") : '')
                        }
                    } else {
                        settings.rangeType.each { it ->
                            Map reportFileData = getReportFileName(executedConfiguration,settings.type,it,"faers")
                            result.put(it, reportFileData.reportFiles ? URLEncoder.encode(reportFileData.fileName, "UTF-8") : '')
                        }
                    }
                }
                return result
            } catch (e) {
                e.printStackTrace()
            }
        }
        [:]
    }

    Map getReportFileName(ExecutedConfiguration executedConfiguration, ProductClassification type, DateRangeEnum range, String selectedDataSource) {
        List fileNames = [getExistingFileName(executedConfiguration, type, range), getFileName(executedConfiguration, type, range),
                          getFileName(executedConfiguration, type, range, selectedDataSource)].collect { "'$it'" }
        List reportFiles = getReportFilesByName(fileNames.join(','))
        //Added code for to check older version file format name
        if (Objects.nonNull(reportFiles) && reportFiles.isEmpty()) {
            List fileNamesWithOutSpecialChar = [getExistingFileNameWithoutRegex(executedConfiguration, type, range), getFileNameWithoutRegex(executedConfiguration, type, range),
                              getFileNameWithoutRegex(executedConfiguration, type, range, selectedDataSource)].collect { "'$it'" }
            reportFiles = getReportFilesByName(fileNamesWithOutSpecialChar.join(','))
        }
        return [reportFiles: reportFiles, fileName: reportFiles[0]]
    }
    void generateSpotfireReport(ExecutedConfiguration executedConfiguration, ExecutedConfiguration executedConfigurationFaers = null) {
        if (executedConfiguration && executedConfiguration.spotfireSettings) {
            ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfiguration.id, executedConfiguration.type)
            try {
                String updateQuery = "Update Ex_status set Spotfire_Execution_Status = 'GENERATING' where id = ${executionStatus.id}"
                updateExecutionStatusForReportingBySQL(updateQuery)
                log.info("Calling generateSpotfireReport()")
                SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
                settings.dataSource.each { String selectedDataSource ->
                    if (selectedDataSource.equals(Constants.DataSource.PVA)) {
                        settings.rangeType.each { it ->
                            if (it in [DateRangeEnum.CUMULATIVE_SAFETYDB, DateRangeEnum.CUMULATIVE]) {
                                executeSpotfireReport(executedConfiguration, true)
                            } else if(it in [ DateRangeEnum.PR_DATE_RANGE_SAFETYDB, DateRangeEnum.PR_DATE_RANGE]) {
                                executeSpotfireReport(executedConfiguration, false)
                            }
                        }
                    } else if (selectedDataSource.equals(Constants.DataSource.FAERS)) {
                        settings.rangeType.each { it ->
                            if (it == DateRangeEnum.CUMULATIVE_FAERS) {
                                executeSpotfireReportFaers(executedConfiguration, true)
                            } else if (it in [ DateRangeEnum.PR_DATE_RANGE_FAERS]) {
                                executeSpotfireReportFaers(executedConfiguration, false)
                            }
                        }
                    } else if (selectedDataSource.equals(Constants.DataSource.VIGIBASE)) {
                        settings.rangeType.each { it ->
                            if (it == DateRangeEnum.CUMULATIVE_VIGIBASE) {
                                executeSpotfireReportVigibase(executedConfiguration, true)
                            } else if (it in [ DateRangeEnum.PR_DATE_RANGE_VIGIBASE]) {
                                executeSpotfireReportVigibase(executedConfiguration, false)
                            }
                        }
                    } else if (selectedDataSource.equals(Constants.DataSource.VAERS)) {
                        settings.rangeType.each { it ->
                            if (it == DateRangeEnum.CUMULATIVE_VAERS) {
                                executeSpotfireReportVaers(executedConfiguration, true)
                            } else if (it in [ DateRangeEnum.PR_DATE_RANGE_VAERS]) {
                                executeSpotfireReportVaers(executedConfiguration, false)
                            }
                        }
                    }
                }
            } catch (Throwable ex) {
                String stackTrace = alertService.exceptionString(ex).replaceAll("'", "''")
                String updateQuery = "Update EX_STATUS set SPOTFIRE_EXECUTION_STATUS= 'ERROR' ${stackTrace ? ", STACK_TRACE= '${stackTrace}'" : ""} where ID = ${executionStatus.id}"
                updateExecutionStatusForReportingBySQL(updateQuery)
                sendErrorNotification(executedConfiguration)
                log.error("Failed generate spotfire report file while generating", ex.printStackTrace())
            }
        }
    }

    void generateNewCumulativeCaseSeries(ExecutedConfiguration executedConfiguration, boolean isSpotfire = false) {
        try {
            Long cumCaseSeriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true, true)
            if (cumCaseSeriesId) {
                executedConfiguration.pvrCumulativeCaseSeriesId = cumCaseSeriesId
                executedConfiguration.save(flush: true, failOnError: true)
            }
            reportExecutorService.generateAlertResultQualitative(Configuration.get(executedConfiguration.configId), executedConfiguration, null, false, true, null, isSpotfire)
        } catch(Exception ex){
            ex.printStackTrace()
            throw ex
        } finally {
            dataObjectService.removeCumCaseSeriesThread(executedConfiguration.configId)
        }
    }


    String getFileGeneratedFromParameter(String alertType, String selectedDatasource) {
        String FILEGENERATEDFROM = ''
        if (alertType.equals("Aggregate Case Alert")) {
            if (selectedDatasource.indexOf("pva") >= 0) {
                FILEGENERATEDFROM = "agg_alert_safety";
            } else if (selectedDatasource.indexOf("faers") >= 0) {
                FILEGENERATEDFROM = "agg_alert_faers";
            } else if (selectedDatasource.indexOf("vaers") >= 0) {
                FILEGENERATEDFROM = "agg_alert_vaers";
            } else if (selectedDatasource.indexOf("vigibase") >= 0) {
                FILEGENERATEDFROM = "agg_alert_vigibase";
            } else if (selectedDatasource.indexOf("eudra") >= 0) {
                FILEGENERATEDFROM = "agg_alert_eudra";
            }
        } else if (alertType.equals("Single Case Alert")) {
            FILEGENERATEDFROM = "icr_alert";
        } else if (alertType.equals("Signal")) {
            FILEGENERATEDFROM = "signal";
        }
        return FILEGENERATEDFROM;
    }
    String generatePVSAlertCriteria(ExecutedConfiguration executedConfiguration, String selectedDataSource){
        List<String> queryParameters = []
        String queryName = ''
        String all = Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)
        String none = Constants.Commons.NONE.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.NONE.toLowerCase().substring(1)
        if (executedConfiguration.executedAlertQueryId && executedConfiguration.alertQueryName && selectedDataSource == Constants.DataSource.FAERS) {
            queryName = executedConfiguration.alertQueryName
            if (executedConfiguration.executedAlertQueryValueLists.size() > 0) {
                executedConfiguration.executedAlertQueryValueLists.each { eaqvl ->
                    StringBuilder queryParameter = new StringBuilder()
                    eaqvl.parameterValues.each { parameter ->
                        if (parameter.hasProperty('reportField')) {
                            queryParameter.append(messageSource.getMessage("app.reportField.${parameter.reportField.name}", null, Locale.default))
                            queryParameter.append(" ")
                            queryParameter.append(messageSource.getMessage("${parameter.operator.getI18nKey()}", null, Locale.default))
                            queryParameter.append(" ")
                            if(parameter.value)
                            {
                                queryParameter.append(parameter.value)
                            }else{
                                if(queryName){
                                    queryParameter.append(all)
                                }else{
                                    queryParameter.append(none)
                                }
                            }
                            queryParameters.add(queryParameter.toString())
                        } else {
                            queryParameters.add("${parameter.key} : ${parameter.value}")
                        }
                        queryParameter.setLength(0);
                    }
                }
            }
        }
        String dateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                " to " + DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
        String timeZone = userService.user?.preference?.timeZone ?: Constants.UTC
        String dateRangeType = ""
        ViewHelper.getDateRangeTypeI18n().each{
            if(it.name == executedConfiguration.dateRangeType ){
                dateRangeType = it.display
            }
        }
        String alertType = ""
        if (executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            alertType = "Aggregate Alert"
        } else {
            alertType = executedConfiguration.type
        }
        Map criteriaMap =  ["Alert_Name" :  executedConfiguration.name,
                            "Alert_Type": alertType,
                            "Data_Source": selectedDataSource?.toUpperCase(),
                            "Product_Dictionary": getProductSelectionValues(executedConfiguration?.productSelection,executedConfiguration?.productGroupSelection)?: Constants.Commons.BLANK_STRING,
                            "Event_Dictionary" : getEventSelectionValues(executedConfiguration?.eventSelection,executedConfiguration?.eventGroupSelection)?: Constants.Commons.BLANK_STRING,
                            "Product_Type" : executedConfiguration.drugTypeName ?: Constants.Commons.BLANK_STRING,
                            "Adhoc_Run":  executedConfiguration?.adhocRun ? "Yes": "No",
                            "Data_Mining_based_on_SMQ":  executedConfiguration?.groupBySmq ? "Yes": "No",
                            "Date_Range_Type" : executedConfiguration.dateRangeType ? dateRangeType : Constants.Commons.BLANK_STRING,
                            "Date_Range" : dateRange?  dateRange :Constants.Commons.BLANK_STRING,
                            "Evaluate_On" : executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ?"Latest Version": "Version As Of",
                            "Date_Range_on" : executedConfiguration.asOfVersionDate ?executedConfiguration.asOfVersionDate : Constants.Commons.BLANK_STRING,
                            "Generated_On" : DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT, timeZone)?: Constants.Commons.BLANK_STRING
        ]
        if(selectedDataSource == Constants.DataSource.FAERS){
            criteriaMap << [ "Exclude_Follow_up" : executedConfiguration?.excludeFollowUp ? "Yes" : "No",
                            "Background_Query": executedConfiguration.alertQueryName ?: Constants.Commons.BLANK_STRING ,
                            "Background_Query_Parameters" : queryParameters ?: Constants.Commons.BLANK_STRING,
                            "Background_Query_URL"        : executedConfiguration?.executedAlertQueryId ? Holders.config.pvreports.query.view.uri + "/" + executedConfiguration?.executedAlertQueryId : Constants.Commons.BLANK_STRING

                              ]
        }
        def jsonString = new JsonBuilder(criteriaMap).toPrettyString()
        String criteriaMapAsString = jsonString.replaceAll('\"', '##')
        criteriaMapAsString = "{\"{##criteria##:[" + criteriaMapAsString + "]}\"}"
        criteriaMapAsString.replaceAll("\n", "")
        criteriaMapAsString.replaceAll(/\s+/, ' ')
    }
    def getProductSelectionValues(String productSelection,String productGroupSelection ) {
        List dicList = PVDictionaryConfig.ProductConfig.views.collect {
            messageSource.getMessage(it.code, null, Locale.default)
        }
        Map productMap = [:]
        dicList.eachWithIndex { value, index ->
            productMap[index+1] = value
        }
        def jsonObj = null
        def data=[]
        String jsonString = productSelection ? productSelection : productGroupSelection
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                return ""
            else {
                jsonObj.collect {
                    if(it.value!= []) {
                        if(productSelection) {
                            it.value?.each { product ->
                                data << product.name + " (" + productMap.get(it.key as Integer) + ")"
                            }
                        }else{
                            data << it.name.replace(it.id as String,"Product Group")
                        }
                    }
                }
            }
        }
        return data.join(",")
    }
    def getEventSelectionValues(String eventSelection,String eventGroupSelection) {
        Map eventMap = [1:"SOC", 2:"HLGT", 3:"HLT", 4:"PT", 5:"LLT", 6:"Synonyms"]
        def jsonObj = null
        def data=[]
        String jsonString = eventSelection ? eventSelection : eventGroupSelection
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                return ""
            else {
                jsonObj.collect {
                    if(it.value!= []) {
                        if(eventSelection) {
                            it.value?.each { event ->
                                data << event.name + " (" + eventMap.get(it.key as Integer) + ")"
                            }
                        }else{
                            data << it.name.replace(it.id as String,"Event Group")
                        }
                    }
                }
            }
        }
        return data.join(",")
    }
    void executeSpotfireReport(ExecutedConfiguration executedConfiguration, Boolean isCumulative, boolean isSpotfire = false) {
        log.info("Calling executeSpotfireReport()")
        ExecutionStatus executionStatus = null
        ExecutionStatus.withNewSession {
            executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfiguration.id, executedConfiguration.type)
        }
        String spotfireFileName = null
        ReportExecutionStatus spotfireExecutionStatus = ReportExecutionStatus.GENERATING
        String stackTrace = null
        try {
            if (executedConfiguration.spotfireSettings) {
                SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
                Set<String> familiesId = []
                familiesId = executedConfiguration.productSelection ? prepareFamilyIds(executedConfiguration.productSelection, executedConfiguration.isMultiIngredient) : prepareFamilyIdsForProductGroup(executedConfiguration.productGroupList, executedConfiguration.isMultiIngredient)
                if(familiesId?.size() == 0){
                    familiesId = null
                }
                SpotfireCommand cmd = new SpotfireCommand()
                cmd.productFamilyIds = familiesId
                // drug family id section
                if (executedConfiguration.includeWHODrugs) {
                    Set<String> drugRecordNumbers = []
                    drugRecordNumbers = executedConfiguration.productSelection ? alertService.fetchDrugRecordNumbersAsList(executedConfiguration.productSelection) : []
                    cmd.whoFamilyIds = drugRecordNumbers?.size() == 0 ? null : drugRecordNumbers
                    cmd.isWHODrug = true
                }
                if (isCumulative && executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT && !executedConfiguration.pvrCumulativeCaseSeriesId) {
                    generateNewCumulativeCaseSeries(executedConfiguration, isSpotfire)
                }
                if (isCumulative || (!isCumulative && executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE  && !executedConfiguration.alertCaseSeriesId)) {
                    cmd.caseSeriesId = executedConfiguration.pvrCumulativeCaseSeriesId
                    cmd.fromDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[0]
                    cmd.endDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[1]
                } else {
                    cmd.caseSeriesId = executedConfiguration.pvrCaseSeriesId
                    cmd.fromDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[0]
                    cmd.endDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[1]
                }
                cmd.asOfDate = executedConfiguration.asOfVersionDate ? executedConfiguration.asOfVersionDate : new Date()
                cmd.type = (settings.type == ProductClassification.DRUG ? "drug" : "vacc")
                cmd.fullFileName = getFileName(executedConfiguration, settings.type, isCumulative ? DateRangeEnum.CUMULATIVE : DateRangeEnum
                        .PR_DATE_RANGE,Constants.DataSource.PVA)
                if (!SpotfireNotificationQuery.countByExecutedConfigurationIdAndFileName(executedConfiguration.id, cmd.fullFileName)) {
                    if (executionStatus.spotfireFileName) {
                        spotfireFileName = executionStatus.spotfireFileName + '\r\n@@@' + cmd.fullFileName
                    } else {
                        spotfireFileName = cmd.fullFileName
                    }
                    generate(cmd, executedConfiguration,Constants.DataSource.PVA)
                    try {
                        createNotifications(executedConfiguration, cmd.fullFileName)
                    }catch (Exception e){
                        spotfireExecutionStatus = ReportExecutionStatus.ERROR
                        log.error("Some error while creating the notification",e)
                    }
                }else{
                    log.info("set error status into spotforeexecutionstatus.")
                    spotfireExecutionStatus = ReportExecutionStatus.ERROR
                }
            }
        } catch (Throwable ex) {
            spotfireExecutionStatus = ReportExecutionStatus.ERROR
            stackTrace = alertService.exceptionString(ex).replaceAll("'", "''")
            sendErrorNotification(executedConfiguration)
            log.error("Failed generate spotfire report file while executing", ex)
        }finally {
            String updateQuery = "Update EX_STATUS set SPOTFIRE_FILE_NAME = '${spotfireFileName}', SPOTFIRE_EXECUTION_STATUS= '${spotfireExecutionStatus}' ${stackTrace ? ", STACK_TRACE= '${stackTrace}'" : ""} where ID = ${executionStatus.id}"
            updateExecutionStatusForReportingBySQL(updateQuery)
        }
    }


    void executeSpotfireReportFaers(ExecutedConfiguration executedConfiguration, Boolean isCumulative) {
        log.info("Calling executeSpotfireReport()")
        ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfiguration.id, executedConfiguration.type)
        String spotfireFileName = null
        ReportExecutionStatus spotfireExecutionStatus = ReportExecutionStatus.GENERATING
        String stackTrace = null
        try {
            SpotfireCommand cmd = new SpotfireCommand()
            cmd.productFamilyIds = []
            cmd.caseSeriesId = isCumulative ? executedConfiguration.faersCumCaseSeriesId : executedConfiguration.faersCaseSeriesId
            cmd.fromDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[0]
            cmd.endDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[1]
            cmd.asOfDate = executedConfiguration.asOfVersionDate ? executedConfiguration.asOfVersionDate : new Date()
            cmd.type = "drug"
            cmd.fullFileName = getFileName(executedConfiguration, ProductClassification.DRUG, isCumulative ? DateRangeEnum.CUMULATIVE : DateRangeEnum
                    .PR_DATE_RANGE,Constants.DataSource.FAERS)

            if (executionStatus.spotfireFileName) {
                spotfireFileName = executionStatus.spotfireFileName + '\r\n@@@' + cmd.fullFileName
            } else {
                spotfireFileName = cmd.fullFileName
            }
            generate(cmd, executedConfiguration, Constants.DataSource.FAERS)
            createNotifications(executedConfiguration, cmd.fullFileName)
        } catch (Throwable ex) {
            spotfireExecutionStatus = ReportExecutionStatus.ERROR
            sendErrorNotification(executedConfiguration)
            stackTrace = alertService.exceptionString(ex).replaceAll("'", "''")
            log.error("Failed generate spotfire report file while executing", ex)
        } finally{
            String updateQuery = "Update EX_STATUS set SPOTFIRE_FILE_NAME = '${spotfireFileName}', SPOTFIRE_EXECUTION_STATUS= '${spotfireExecutionStatus}' ${stackTrace ? ", STACK_TRACE= '${stackTrace}'" : ""} where ID = ${executionStatus.id}"
            updateExecutionStatusForReportingBySQL(updateQuery)
        }
    }

    void executeSpotfireReportVigibase(ExecutedConfiguration executedConfiguration, Boolean isCumulative) {
        log.info("Calling executeSpotfireReportVigibase()")
        ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfiguration.id, executedConfiguration.type)
        String spotfireFileName = null
        ReportExecutionStatus spotfireExecutionStatus = ReportExecutionStatus.GENERATING
        String stackTrace = null
        try {
            if (executedConfiguration.spotfireSettings) {
                SpotfireCommand cmd = new SpotfireCommand()
                SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
                cmd.productFamilyIds = []
                cmd.caseSeriesId = isCumulative ? executedConfiguration.vigibaseCumCaseSeriesId : executedConfiguration.vigibaseCaseSeriesId
                cmd.fromDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[0]
                cmd.endDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[1]
                cmd.asOfDate = executedConfiguration.asOfVersionDate ? executedConfiguration.asOfVersionDate : new Date()
                cmd.type = (settings.type == ProductClassification.DRUG ? "drug" : "vacc")
                cmd.fullFileName = getFileName(executedConfiguration, settings.type, isCumulative ? DateRangeEnum.CUMULATIVE : DateRangeEnum
                        .PR_DATE_RANGE, Constants.DataSource.VIGIBASE)

                if (executionStatus.spotfireFileName) {
                    spotfireFileName = executionStatus.spotfireFileName + '\r\n@@@' + cmd.fullFileName
                } else {
                    spotfireFileName = cmd.fullFileName
                }
                generate(cmd, executedConfiguration, Constants.DataSource.VIGIBASE)
                createNotifications(executedConfiguration, cmd.fullFileName)
            }
        } catch (Throwable ex) {
            spotfireExecutionStatus = ReportExecutionStatus.ERROR
            sendErrorNotification(executedConfiguration)
            stackTrace = alertService.exceptionString(ex).replaceAll("'", "''")
            log.error("Failed generate vigibase spotfire report file while executing", ex)
        } finally{
            String updateQuery = "Update EX_STATUS set SPOTFIRE_FILE_NAME = '${spotfireFileName}', SPOTFIRE_EXECUTION_STATUS= '${spotfireExecutionStatus}' ${stackTrace ? ", STACK_TRACE= '${stackTrace}'" : ""} where ID = ${executionStatus.id}"
            updateExecutionStatusForReportingBySQL(updateQuery)
        }
    }

    void executeSpotfireReportVaers(ExecutedConfiguration executedConfiguration, Boolean isCumulative) {
        log.info("Calling executeSpotfireReportVaers()")
        ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfiguration.id, executedConfiguration.type)
        String spotfireFileName = null
        ReportExecutionStatus spotfireExecutionStatus = ReportExecutionStatus.GENERATING
        String stackTrace = null
        try {
            SpotfireCommand cmd = new SpotfireCommand()
            cmd.productFamilyIds = []
            cmd.caseSeriesId = isCumulative ? executedConfiguration.vaersCumCaseSeriesId : executedConfiguration.vaersCaseSeriesId
            cmd.fromDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[0]
            cmd.endDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[1]
            cmd.asOfDate = executedConfiguration.asOfVersionDate ? executedConfiguration.asOfVersionDate : new Date()
            //cmd.type = "vacc"
            cmd.type = "drug"
            cmd.fullFileName = getFileName(executedConfiguration, ProductClassification.VACCINCE, isCumulative ? DateRangeEnum.CUMULATIVE : DateRangeEnum
                    .PR_DATE_RANGE,Constants.DataSource.VAERS)
            if (executionStatus.spotfireFileName) {
                spotfireFileName = executionStatus.spotfireFileName + '\r\n@@@' + cmd.fullFileName
            } else {
                spotfireFileName = cmd.fullFileName
            }
            generate(cmd, executedConfiguration, Constants.DataSource.VAERS)
            createNotifications(executedConfiguration, cmd.fullFileName)
        } catch (Throwable ex) {
            spotfireExecutionStatus = ReportExecutionStatus.ERROR
            sendErrorNotification(executedConfiguration)
            stackTrace = alertService.exceptionString(ex).replaceAll("'", "''")
            log.error("Failed generate vaers spotfire report file while executing", ex)
        } finally{
            String updateQuery = "Update EX_STATUS set SPOTFIRE_FILE_NAME = '${spotfireFileName}', SPOTFIRE_EXECUTION_STATUS= '${spotfireExecutionStatus}' ${stackTrace ? ", STACK_TRACE= '${stackTrace}'" : ""} where ID = ${executionStatus.id}"
            updateExecutionStatusForReportingBySQL(updateQuery)
        }
    }

    private createNotifications(ExecutedConfiguration executedConfiguration, String name) {
        SpotfireNotificationQuery notification = new SpotfireNotificationQuery()
        notification.fileName = name.replaceAll("[^a-zA-Z0-9\\-]", "_");
        notification.configurationName = executedConfiguration.name
        notification.type = executedConfiguration.type
        notification.runType = executedConfiguration.adhocRun ? "adhocRun" : "dataMiningRun"
        notification.executedConfigurationId = executedConfiguration.id
        notification.notificationRecipients = notificationRecipients(executedConfiguration)
        notification.save(failOnError: true, flush: true)
    }

    private Set<String> prepareFamilyIds(String productDictionarySelection, Boolean isMultiIngredient = false) throws Exception{
        Set<String> familiesId = []
        final Sql sql
        try {
            sql = new Sql(dataSource_pva)
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_filter_key_values'); "

            insertStatement += initializeGTTForSpotfire(productDictionarySelection)
            insertStatement += " INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_MULTI_INGREDIENT','${isMultiIngredient?1:0}'); "
            insertStatement += " END;"
            if (insertStatement) {
                sql.execute(insertStatement)
            }
            //multi-ingredient-change for ingredient to family ids

            sql.call("{call f_resolve_dict_values(${Sql.CLOB})}") {Clob familyId ->
                if(familyId){
                    String familyIdString = familyId.getSubString(1,(int)familyId.length())
                    familiesId.addAll(familyIdString.split(","))
                }
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage())
            throw ex
        } finally {
            sql?.close()
        }
        familiesId.removeAll([null])
        familiesId
    }

    /**
     * Get set of family ids for drug selection
     * @param productDictionarySelection
     * @param isMultiIngredient
     * @param includeWhoDrugs
     * @return set of family ids
     * @throws Exception
     */
    private Set<String> prepareFamilyIdsForDrugs(String productDictionarySelection, Boolean isMultiIngredient = false, Boolean includeWhoDrugs = false) throws Exception{
        Set<String> familiesId = []
        final Sql sql
        try {
            sql = new Sql(dataSource_pva)
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_filter_key_values'); "

            insertStatement += initializeGTTForSpotfire(productDictionarySelection)
            insertStatement += " INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_MULTI_INGREDIENT','${isMultiIngredient?1:0}'); "
            insertStatement += " INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('INCLUDE_WHO_DRUGS','${includeWhoDrugs?1:0}'); "
            insertStatement += " END;"
            if (sql && insertStatement) {
                sql.execute(insertStatement)
                //fetch query drug to family ids
                getDrugFamilyIds(sql, familiesId)
            }

        } catch (Throwable ex) {
            log.error(ex.getMessage())
            throw ex
        } finally {
            sql?.close()
        }
        familiesId
    }

    /**
     * Call procedure to fetch family id for drug
     * @param sql
     * @param familiesId
     * @return set of family ids
     */
    private Set<String> getDrugFamilyIds(Sql sql, Set<String> familiesId) {
        sql.call("{call P_DRUG_REC_NUM_TO_FAMILY(${Sql.CLOB})}") {Clob familyId ->
            if(familyId){
                String familyIdString = familyId.getSubString(1,(int)familyId.length())
                familiesId.addAll(familyIdString.split(","))
            }
        }
        familiesId
    }


    Set<String> prepareFamilyIdsForProductGroup(String productGroupList, Boolean isMultiIngredient=false, Boolean includeWhoDrugs=false){
        Set<String> familiesId = []
        Sql sql
        String productJson
        try {
            sql = new Sql(dataSource_pva)
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_filter_key_values'); "

            productGroupList.split(',').each {
                sql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID = ${it}") { ResultSet resultSetObj ->
                    Clob clob = resultSetObj.getClob("GRP_DATA")
                    if (clob) {
                        productJson = clob.getSubString(1, (int) clob.length())
                    }
                }

                insertStatement += initializeGTTForSpotfire(productJson)
            }
            insertStatement += " INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_MULTI_INGREDIENT','${isMultiIngredient?1:0}'); "
            insertStatement += " INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('INCLUDE_WHO_DRUGS','${includeWhoDrugs?1:0}'); "
            insertStatement += " END;"
            if (insertStatement) {
                sql.execute(insertStatement)
            }

            sql.call("{call f_resolve_dict_values(${Sql.CLOB})}") {Clob familyId ->
                if(familyId){
                    String familyIdString = familyId.getSubString(1,(int)familyId.length())
                    familiesId.addAll(familyIdString.split(","))
                }
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage())
        } finally {
            sql?.close()
        }
        familiesId.removeAll([null])
        familiesId
    }

    String getJsonForGroup(List productGroupList){
        Sql sql
        String productJson
        try {
            sql = new Sql(dataSource_pva)

            productGroupList.each {
                sql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID = ${it}") { ResultSet resultSetObj ->
                    Clob clob = resultSetObj.getClob("GRP_DATA")
                    if (clob) {
                        productJson = clob.getSubString(1, (int) clob.length())
                    }
                }
            }

        } catch (Throwable ex) {
            log.error(ex.getMessage())
        } finally {
            sql?.close()
        }
        productJson
    }

    private String initializeGTTForSpotfire(String productSelection){
        sqlGenerationService.initializeGTTForSpotfire(productSelection)
    }

    Map fetchAnalysisFileUrl(ExecutedConfiguration executedConfiguration) {
        Map analysisFileUrls = [:]
        if(executedConfiguration && executedConfiguration.caseSeriesSpotfireFile) {
            List<String> reportFiles = getReportFilesByName("'${executedConfiguration.caseSeriesSpotfireFile.split("@@@@DateRange")[0]}'")
            if (reportFiles) {
                List<String> fileNames = executedConfiguration.caseSeriesSpotfireFile.split("@@@@DateRange")
                analysisFileUrls << [spotfireUrl: spotfireFileUrl(reportFiles[0], "\"${executedConfiguration.pvrCaseSeriesId}\"",executedConfiguration.selectedDatasource)+";", name: "${executedConfiguration.alertCaseSeriesName}(${fileNames[1]})"]
            }
        }
        analysisFileUrls
    }

    List<Map> fetchAnalysisFileUrlIntegratedReview(ExecutedConfiguration executedConfiguration, Boolean isAlertBursting = false) {
        List<Map> analysisFileUrls = []
        if(isAlertBursting){
            return analysisFileUrls
        }
        if (executedConfiguration && executedConfiguration.spotfireSettings) {
            SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
            if(settings.dataSource) {
                settings.dataSource.each { String selectedDataSource ->
                    if (selectedDataSource.equals(Constants.DataSource.PVA)) {
                        settings.rangeType.each { it ->
                            if (it in [DateRangeEnum.CUMULATIVE_SAFETYDB, DateRangeEnum.CUMULATIVE, DateRangeEnum.PR_DATE_RANGE_SAFETYDB]) {
                                Map reportFileData = getReportFileName(executedConfiguration, settings.type, it, Constants.DataSource.PVA)
                                if (reportFileData.reportFiles) {
                                    analysisFileUrls << [fileName: reportFileData.fileName, spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,selectedDataSource), name: it == DateRangeEnum.CUMULATIVE_SAFETYDB ? Constants.SpotfireFileName.CUMM_FILE_PVA : Constants.SpotfireFileName.INTERVAL_FILE_PVA]
                                }
                            }
                        }
                    } else if (selectedDataSource.equals(Constants.DataSource.FAERS)) {
                        settings.rangeType.each { it ->
                            if (it in [DateRangeEnum.CUMULATIVE_FAERS, DateRangeEnum.PR_DATE_RANGE_FAERS]) {
                                Map reportFileData = getReportFileName(executedConfiguration, settings.type, it, Constants.DataSource.FAERS)
                                if (reportFileData.reportFiles) {
                                    analysisFileUrls << [fileName: reportFileData.fileName,spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,selectedDataSource), name: it == DateRangeEnum.CUMULATIVE_FAERS ? Constants.SpotfireFileName.CUMM_FILE_FAERS : Constants.SpotfireFileName.INTERVAL_FILE_FAERS]
                                }
                            }
                        }
                    } else if (selectedDataSource.equals(Constants.DataSource.VIGIBASE)) {
                        settings.rangeType.each { it ->
                            if (it in [DateRangeEnum.CUMULATIVE_VIGIBASE, DateRangeEnum.PR_DATE_RANGE_VIGIBASE]) {
                                Map reportFileData = getReportFileName(executedConfiguration, settings.type, it, Constants.DataSource.VIGIBASE)
                                if (reportFileData.reportFiles) {
                                    analysisFileUrls << [fileName: reportFileData.fileName,spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,selectedDataSource), name: it == DateRangeEnum.CUMULATIVE_VIGIBASE ? Constants.SpotfireFileName.CUMM_FILE_VIGIBASE : Constants.SpotfireFileName.INTERVAL_FILE_VIGIBASE]
                                }
                            }
                        }
                    } else if (selectedDataSource.equals(Constants.DataSource.VAERS)) {
                        settings.rangeType.each { it ->
                            if (it in [DateRangeEnum.CUMULATIVE_VAERS, DateRangeEnum.PR_DATE_RANGE_VAERS]) {
                                Map reportFileData = getReportFileName(executedConfiguration, settings.type, it, Constants.DataSource.VAERS)
                                if (reportFileData.reportFiles) {
                                    analysisFileUrls << [fileName: reportFileData.fileName,spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,selectedDataSource), name: it == DateRangeEnum.CUMULATIVE_VAERS ? Constants.SpotfireFileName.CUMM_FILE_VAERS : Constants.SpotfireFileName.INTERVAL_FILE_VAERS]
                                }
                            }
                        }
                    }
                }
            } else {
                settings.rangeType.each { it ->
                    Map reportFileData = getReportFileName(executedConfiguration, settings.type, it,executedConfiguration.selectedDatasource)
                    if (reportFileData.reportFiles) {
                        if(executedConfiguration.selectedDatasource.equals(Constants.DataSource.PVA)) {
                            analysisFileUrls << [fileName: reportFileData.fileName,spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,executedConfiguration.selectedDatasource), name: it == DateRangeEnum.CUMULATIVE ? Constants.SpotfireFileName.CUMM_FILE_PVA : Constants.SpotfireFileName.INTERVAL_FILE_PVA]
                        } else if(executedConfiguration.selectedDatasource.equals(Constants.DataSource.VIGIBASE)) {
                            analysisFileUrls << [fileName: reportFileData.fileName,spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,executedConfiguration.selectedDatasource), name: it == DateRangeEnum.CUMULATIVE ? Constants.SpotfireFileName.CUMM_FILE_VIGIBASE : Constants.SpotfireFileName.INTERVAL_FILE_VIGIBASE]
                        } else if(executedConfiguration.selectedDatasource.equals(Constants.DataSource.VAERS)) {
                            analysisFileUrls << [fileName: reportFileData.fileName,spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,executedConfiguration.selectedDatasource), name: it == DateRangeEnum.CUMULATIVE ? Constants.SpotfireFileName.CUMM_FILE_VAERS : Constants.SpotfireFileName.INTERVAL_FILE_VAERS]
                        } else {
                            analysisFileUrls << [fileName: reportFileData.fileName,spotfireUrl: spotfireFileUrl(reportFileData.fileName,null,executedConfiguration.selectedDatasource), name: it == DateRangeEnum.CUMULATIVE ? Constants.SpotfireFileName.CUMM_FILE_FAERS : Constants.SpotfireFileName.INTERVAL_FILE_FAERS]
                        }
                    }
                }
            }
        }
        analysisFileUrls
    }

    String spotfireFileUrl(String fileName, String caseSeriesId = null, String selectedDatasource =  Constants.DataSource.PVA) {
        String value = Holders.config.spotfire.symptomsComanifestation
        String columnName = Holders.config.spotfire.symptomsComanifestationColumnName
        String setFilterString = ""
        if(selectedDatasource == Constants.DataSource.PVA) {
            switch (value?.toLowerCase()) {
                case 'yes':
                    value = 'Yes'
                    break
                case 'no':
                    value = 'No'
                    break
                default:
                    value = ''
                    break
            }
            if (value)
                setFilterString = 'SetFilter(tableName=\"Case, Demographics & AEs\", columnName = \"' + columnName + '\", values= {"' + value + '"});'
            else
                setFilterString = 'SetFilter(tableName=\"Case, Demographics & AEs\", columnName = \"' + columnName + '\", Operation= {\"Reset\"});'
        }
        if(!caseSeriesId) {
            grailsLinkGenerator.link(controller: "dataAnalysis", action: "view", absolute: true, params:
                    [fileName: Holders.config.spotfire.libraryRoot + "/" + fileName,configurationBlock:setFilterString])
        } else {
            grailsLinkGenerator.link(controller: "dataAnalysis", action: "view", absolute: true, params:
                    [fileName: Holders.config.spotfire.libraryRoot + "/" + fileName,"configurationBlock":"CaseSeriesId=${caseSeriesId};${setFilterString}"])
        }
    }

    List<User> notificationRecipients(def executedConfiguration) {
        Configuration configuration = Configuration.findById(executedConfiguration?.configId)
        if(configuration && (configuration.shareWithGroup || configuration.shareWithUser)){
            Set<User> allSharedUsers = []
            if(configuration?.shareWithGroup){
                configuration?.shareWithGroup.each{it->
                    allSharedUsers << userGroupService.fetchUserListForGroup(it)
                }
            }
            if(configuration?.shareWithUser){
                allSharedUsers << configuration?.shareWithUser
            }
            new ArrayList<User>(allSharedUsers.flatten())
        }
        else{
            executedConfiguration.assignedToGroup ? userGroupService.fetchUserListForGroup(executedConfiguration.assignedToGroup) : [executedConfiguration.assignedTo]
        }
    }

    List<User> notificationRecipientsForSignal(ValidatedSignal signal) {
        ValidatedSignal validatedSignal = signal
        if(validatedSignal && (validatedSignal.shareWithGroup || validatedSignal.shareWithUser)){
            Set<User> allSharedUsers = []
            if(validatedSignal?.shareWithGroup){
                validatedSignal?.shareWithGroup.each{it->
                    allSharedUsers << userGroupService.fetchUserListForGroup(it)
                }
            }
            if(validatedSignal?.shareWithUser){
                allSharedUsers << validatedSignal?.shareWithUser
            }
            new ArrayList<User>(allSharedUsers.flatten())
        }
        else{
            validatedSignal.assignedToGroup ? userGroupService.fetchUserListForGroup(validatedSignal.assignedToGroup) : [validatedSignal.assignedTo]
        }

    }

    List<DateRangeEnum> spotfireFileDRType(ExecutedConfiguration executedConfiguration){
        SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
        settings.rangeType
    }

    String fetchHidingOptions(String fileName) {
        List hidingOption
        String hidingParameter
        try {
           hidingOption = fetchReportType(fileName)
           if(hidingOption?.size() == 0) {
               hidingParameter = null
           } else{
               Map optionCodes = grailsApplication.config.spotfire.hiding.options.code
               hidingOption.each{
                   String codeValue = optionCodes[it]
                   if(codeValue) {
                       if(hidingParameter) {
                           hidingParameter += "," + codeValue + "-0"
                       }else {
                           hidingParameter = codeValue + "-0"
                       }
                   }
               }
           }
        } catch (Exception ex) {
            log.error(ex.getMessage())
            hidingParameter = null
        }
        return hidingParameter
    }

    List fetchReportType(String fileName) {
        List hidingOption
        try {
            switch (fileName) {
                case grailsApplication.config.spotfire.operationalReport.url:
                    hidingOption = grailsApplication.config.spotfire.operationalReport.hidingOptions
                    break
                case grailsApplication.config.spotfire.riskSummaryReport.url:
                    hidingOption = grailsApplication.config.spotfire.riskSummaryReport.hidingOptions
                    break
                case grailsApplication.config.spotfire.productivityAndComplianceReport.url:
                    hidingOption = grailsApplication.config.spotfire.productivityAndComplianceReport.hidingOptions
                    break
                default:
                    hidingOption = grailsApplication.config.spotfire.dataAnalysis.hidingOptions
            }
        }catch(Throwable th){
            throw th
        }
        return hidingOption
    }

    /**
     * Creates notification signal.
     *
     * @param validatedSignal the validated signal for which the notification is being created
     * @param name the name of the notification file
     * @param params a map containing various parameters such as product selection and event dictionary values
     * @param startDate the start date
     * @param endDate the end date
     * @param currentUser the current logged in user
     * @param dateRange the date range type for signal assessment
     * @return void
     */
    void createNotificationsSignal(ValidatedSignal validatedSignal, String name, Map params, String startDate, String endDate, User currentUser, SignalAssessmentDateRangeEnum dateRange) {
        List prodList = importConfigurationService.getProductSelectionWithType(params.productSelection, params.productGroupSelection)
        String productList = prodList.collect {it.trim()}.join(", ")
        String eventList = ViewHelper.getEventDictionaryValues(params)
        log.info("$productList-$eventList")
        SpotfireNotificationQuery notification = new SpotfireNotificationQuery()
        notification.fileName = name.replaceAll("[^a-zA-Z0-9\\-]", "_")
        notification.configurationName = validatedSignal.name
        notification.type = "Signal Management"
        notification.runType = "Signal"
        notification.executedConfigurationId = validatedSignal.id
        notification.isEnabled = true
        // the below code is introduced to send notification to the current logged in user only
        notification.notificationRecipients = [currentUser]
        if(dateRange == SignalAssessmentDateRangeEnum.SIGNAL_DATA){
            notification.signalParameters = "$productList@@@$eventList@@@Signal Data@@@${currentUser.fullName}@@@${new Date().format(DateUtil.DATEPICKER_FORMAT_AM_PM)}"
        } else {
            notification.signalParameters = "$productList@@@$eventList@@@$startDate to $endDate@@@${currentUser.fullName}@@@${new Date().format(DateUtil.DATEPICKER_FORMAT_AM_PM)}"
        }
        notification.save(failOnError: true, flush: true)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateExecutionStatusForReporting(String updateHql, Map updateParams) {
        ExecutionStatus.executeUpdate(updateHql, updateParams)
    }

    void updateExecutionStatusForReportingBySQL(String updateSql) {
        Sql sql = new Sql(dataSource)
        try {
            sql.withTransaction {
                sql.execute(updateSql)
            }
        } catch (Exception ex) {
            // Proper error handling, you can customize this as per your application's needs
            log.error("Error updating execution status: ${ex.message}")
        } finally {
            // Ensure that the SQL connection is properly closed
            sql?.close()
        }
    }


    Map<String,Map> fetchAnalysisFileUrlCounts(ExecutedConfiguration executedConfiguration) {

        Map resultMap = [:]
        if (executedConfiguration && executedConfiguration.spotfireSettings) {
            SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
            if (!settings.dataSource) {
                settings.dataSource = (executedConfiguration.type != Constants.AlertConfigType.SINGLE_CASE_ALERT) ?
                        executedConfiguration.selectedDatasource.split(",") : [Constants.DataSource.PVA] as List<String>
            }
            if (settings.dataSource) {
                settings.rangeType.each { it ->
                    if (it == DateRangeEnum.PR_DATE_RANGE || it == DateRangeEnum.PR_DATE_RANGE_SAFETYDB) {
                        Map reportFileData = getReportFileName(executedConfiguration, settings.type, it, Constants.DataSource.PVA)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE.name() , ['status' : 2 , 'message' : "Current Period Analysis" , 'url' : spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.PVA) ])
                        } else if (!resultMap.containsKey(DateRangeEnum.PR_DATE_RANGE.name())) {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE.name() , ['status' : 1 , 'message' : "Current Period Analysis"])
                        }
                    } else if(it == DateRangeEnum.CUMULATIVE || it == DateRangeEnum.CUMULATIVE_SAFETYDB){
                        Map reportFileData = getReportFileName(executedConfiguration, settings.type, it, Constants.DataSource.PVA)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.CUMULATIVE.name() , ['status' : 2 , 'message' : "Cumulative Period Analysis" , 'url' : spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.PVA)])
                        } else if (!resultMap.containsKey(DateRangeEnum.CUMULATIVE.name())) {
                            resultMap.put(DateRangeEnum.CUMULATIVE.name() , ['status' : 1 , 'message' : "Cumulative Period Analysis"])
                        }
                    } else if (it == DateRangeEnum.PR_DATE_RANGE_FAERS) {
                        Map reportFileData = getReportFileName(executedConfiguration, ProductClassification.DRUG, it, Constants.DataSource.FAERS)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE_FAERS.name() , ['status' : 2 , 'message' : "Current Period Analysis (FAERS)" , 'url': spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.FAERS)])
                        } else {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE_FAERS.name() , ['status' : 1 , 'message' : "Current Period Analysis (FAERS)"])
                        }
                    } else if(it == DateRangeEnum.CUMULATIVE_FAERS ){
                        Map reportFileData = getReportFileName(executedConfiguration, ProductClassification.DRUG, it, Constants.DataSource.FAERS)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.CUMULATIVE_FAERS.name() , ['status' : 2 , 'message' : "Cumulative Period Analysis (FAERS)" , 'url' : spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.FAERS)])
                        } else {
                            resultMap.put(DateRangeEnum.CUMULATIVE_FAERS.name() , ['status' : 1 , 'message' : "Cumulative Period Analysis (FAERS)"])
                        }
                    } else if (it == DateRangeEnum.PR_DATE_RANGE_VIGIBASE) {
                        Map reportFileData = getReportFileName(executedConfiguration, ProductClassification.DRUG, it, Constants.DataSource.VIGIBASE)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE_VIGIBASE.name() , ['status' : 2 , 'message' : "Current Period Analysis (VIGIBASE)" , 'url': spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.VIGIBASE)])
                        } else {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE_VIGIBASE.name() , ['status' : 1 , 'message' : "Current Period Analysis (VIGIBASE)"])
                        }
                    } else if(it == DateRangeEnum.CUMULATIVE_VIGIBASE ){
                        Map reportFileData = getReportFileName(executedConfiguration, ProductClassification.DRUG, it, Constants.DataSource.VIGIBASE)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.CUMULATIVE_VIGIBASE.name() , ['status' : 2 , 'message' : "Cumulative Period Analysis (VIGIBASE)" , 'url' : spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.VIGIBASE)])
                        } else {
                            resultMap.put(DateRangeEnum.CUMULATIVE_VIGIBASE.name() , ['status' : 1 , 'message' : "Cumulative Period Analysis (VIGIBASE)"])
                        }
                    } else if (it == DateRangeEnum.PR_DATE_RANGE_VAERS) {
                        Map reportFileData = getReportFileName(executedConfiguration, ProductClassification.VACCINCE, it, Constants.DataSource.VAERS)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE_VAERS.name() , ['status' : 2 , 'message' : "Current Period Analysis (VAERS)" , 'url': spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.VAERS)])
                        } else {
                            resultMap.put(DateRangeEnum.PR_DATE_RANGE_VAERS.name() , ['status' : 1 , 'message' : "Current Period Analysis (VAERS)"])
                        }
                    } else if(it == DateRangeEnum.CUMULATIVE_VAERS){
                        Map reportFileData = getReportFileName(executedConfiguration, ProductClassification.VACCINCE, it, Constants.DataSource.VAERS)
                        if (reportFileData.reportFiles) {
                            resultMap.put(DateRangeEnum.CUMULATIVE_VAERS.name() , ['status' : 2 , 'message' : "Cumulative Period Analysis (VAERS)" , 'url' : spotfireFileUrl(reportFileData.fileName, null,Constants.DataSource.VAERS)])
                        } else {
                            resultMap.put(DateRangeEnum.CUMULATIVE_VAERS.name() , ['status' : 1 , 'message' : "Cumulative Period Analysis (VAERS)"])
                        }
                    }

                }
            }
        }
        if(!resultMap.containsKey(DateRangeEnum.CUMULATIVE.name())) {
            resultMap.put(DateRangeEnum.CUMULATIVE.name() , ['status' : 0 , 'message' : "Generate Cumulative Period Analysis"])
        }
        if(!resultMap.containsKey(DateRangeEnum.PR_DATE_RANGE.name())) {
            resultMap.put(DateRangeEnum.PR_DATE_RANGE.name() , ['status' : 0 , 'message' : "Generate Current Period Analysis"])
        }
        if(!resultMap.containsKey(DateRangeEnum.CUMULATIVE_FAERS.name()) && executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            resultMap.put(DateRangeEnum.CUMULATIVE_FAERS.name() , ['status' : 0 , 'message' : "Generate Cumulative Period Analysis (FAERS)"])
        }
        if(!resultMap.containsKey(DateRangeEnum.PR_DATE_RANGE_FAERS.name()) && executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            resultMap.put(DateRangeEnum.PR_DATE_RANGE_FAERS.name() , ['status' : 0 , 'message' : "Generate Current Period Analysis (FAERS)"])
        }
        if(!resultMap.containsKey(DateRangeEnum.CUMULATIVE_VIGIBASE.name()) && executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            resultMap.put(DateRangeEnum.CUMULATIVE_VIGIBASE.name() , ['status' : 0 , 'message' : "Generate Cumulative Period Analysis (VIGIBASE)"])
        }
        if(!resultMap.containsKey(DateRangeEnum.PR_DATE_RANGE_VIGIBASE.name()) && executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            resultMap.put(DateRangeEnum.PR_DATE_RANGE_VIGIBASE.name() , ['status' : 0 , 'message' : "Generate Current Period Analysis (VIGIBASE)"])
        }
        if(!resultMap.containsKey(DateRangeEnum.CUMULATIVE_VAERS.name()) && executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            resultMap.put(DateRangeEnum.CUMULATIVE_VAERS.name() , ['status' : 0 , 'message' : "Generate Cumulative Period Analysis (VAERS)"])
        }
        if(!resultMap.containsKey(DateRangeEnum.PR_DATE_RANGE_VAERS.name()) && executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            resultMap.put(DateRangeEnum.PR_DATE_RANGE_VAERS.name() , ['status' : 0 , 'message' : "Generate Current Period Analysis (VAERS)"])
        }
        resultMap
    }


    String getFileNameWithoutRegex(ExecutedConfiguration executedConfiguration, ProductClassification type, DateRangeEnum range,String dataSource) {
        String rangeName = messageSource.getMessage(range.getI18nKey(), new Object[0], Locale.ENGLISH)
//english locale for filename
        String fileName = executedConfiguration.name + "_" + type.getId() + "_" + rangeName.split(" ")[0] + "_" + dataSource + "_" + "ex_" + executedConfiguration.id
        fileName?.replaceAll(Constants.SPOTFIRE_FILE_NAME_REGEX, '_')?.replaceAll("\\\\","_").replaceAll(/^[\W_]+/, 'S_')
    }

    String getFileNameWithoutRegex(ExecutedConfiguration executedConfiguration, ProductClassification type, DateRangeEnum range) {
        String rangeName = messageSource.getMessage(range.getI18nKey(), new Object[0], Locale.ENGLISH)
//english locale for filename
        String fileName = executedConfiguration.name + "_" + type.getId() + "_" + rangeName + "_" + "ex_" + executedConfiguration.id
        fileName?.replaceAll(Constants.SPOTFIRE_FILE_NAME_REGEX, '_')?.replaceAll("\\\\","_").replaceAll(/^[\W_]+/, 'S_')
    }

    String getExistingFileNameWithoutRegex(BaseConfiguration executedConfiguration, ProductClassification type, DateRangeEnum range) {
        String fileName = executedConfiguration.name + "_" + type.getId() + "_" + messageSource.getMessage(range.getI18nKey(), new Object[0], Locale.ENGLISH) +
                "_" + "v" + executedConfiguration.numOfExecutions
        fileName?.replaceAll(Constants.SPOTFIRE_FILE_NAME_REGEX, '_')?.replaceAll("\\\\","_").replaceAll(/^[\W_]+/, 'S_')
    }

    Boolean generateDataAnalysis(Map params) {

        ExecutedConfiguration ec = ExecutedConfiguration.findById(params.executedConfigId as Long)
        // moved audit-log code to top, so that
        // entry will created before triggering spotfire API
        String dateRangeSpotfire = ""
        if(params.spotfireDateRange.contains('PR_DATE_RANGE')){
            dateRangeSpotfire = "Interval"
        }else{
            dateRangeSpotfire = "Cumulative"
        }
        signalAuditLogService.createAuditLog([
                entityName : Constants.AuditLog.typeToEntityMap.get(ec.type),
                moduleName : Constants.AuditLog.typeToEntityMap.get(ec.type)+(ec.isLatest ? "" : ": Archived Alert")+": Data Analysis",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: ec.getInstanceIdentifierForAuditLog(),
                description: "Executing Spotfire Report",
                username   : userService.getUser()?.username ?: ec.owner.username,
                fullname   : userService.getUser()?.fullName ?: ec.owner.fullName
        ] as Map, [[propertyName: "Alert Name", oldValue: "", newValue: ec?.name],
                   [propertyName: "Date Range", oldValue: "", newValue: dateRangeSpotfire],
                   [propertyName: "Owner", oldValue: "", newValue: ec?.owner?.fullName ?: userService.getUser()?.fullName]] as List)
        Configuration configuration = Configuration.get(ec.configId)
        ExecutedConfiguration executedConfigurationFaers = ExecutedConfiguration.findByConfigId(Configuration.findByIntegratedConfigurationId(configuration?.id)?.id)
        ExecutedConfiguration executedConfigurationVigibase = ExecutedConfiguration.findByConfigId(Configuration.findByIntegratedConfigurationId(configuration?.id)?.id)
        ExecutedConfiguration executedConfigurationVaers = ExecutedConfiguration.findByConfigId(Configuration.findByIntegratedConfigurationId(configuration?.id)?.id)
        String drugType = ProductClassification.DRUG
        if (ec.spotfireSettings) {
            DateRangeEnum currentDateRange = params.spotfireDateRange
            List<DateRangeEnum> listOfDateRange = SpotfireSettingsDTO.fromJson(ec.spotfireSettings).rangeType
            if (listOfDateRange.contains(currentDateRange) && (ExecutionStatus.findByExecutedConfigIdAndType(ec.id, ec.type).spotfireExecutionStatus != null)) {
                return false
            }
        }
        drugType = ec?.drugType == Constants.DrugType.VACCINE ? ProductClassification.VACCINCE : ProductClassification.DRUG
        String productSelection = ec.productSelection ?: ec?.productGroupSelection
        String studySelection = ec.studySelection
        DateRangeEnum dateRange = params.spotfireDateRange
        List<DateRangeEnum> dateRangeEnums = []
        if (ec.spotfireSettings) {
            SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(ec.spotfireSettings)
            drugType = settings.type
            dateRangeEnums = settings.rangeType
        }
        dateRangeEnums.add(dateRange)
        if (studySelection || productSelection || configuration.productGroupSelection || (ec.dataMiningVariable && ec.adhocRun)) {
            SpotfireSettingsDTO settings = new SpotfireSettingsDTO()
            settings.type = drugType
            settings.rangeType = dateRangeEnums
            if (configuration.type != Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                settings.dataSource = configuration.selectedDatasource.split(",")
            } else {
                settings.dataSource = [Constants.DataSource.PVA]
            }
            ec.spotfireSettings = settings.toJsonString()
        } else {
            ec.spotfireSettings = null
        }
        ec.save(flush: true)
        boolean isReportingCompleted = false
        if (ec.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            boolean isSpotfire = false
            boolean isFaersSpotfire = false
            boolean isVigibaseSpotfire = false
            boolean isVaersSpotfire = false
            if (ec.spotfireSettings) {
                SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(ec.spotfireSettings)
                isSpotfire = settings.dataSource.contains('pva')
                isFaersSpotfire = settings.dataSource.contains('faers')
                isVigibaseSpotfire = settings.dataSource.contains('vigibase')
                isVaersSpotfire = settings.dataSource.contains('vaers')
            }
            ec = ExecutedConfiguration.findById(params.executedConfigId as Long)
            if (ec.pvrCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForQuantAlert(ec, false)
            }
            if (ec.pvrCumulativeCaseSeriesId && ec.pvrCaseSeriesId != ec.pvrCumulativeCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForQuantAlert(ec, true)
            }
            if (ec.faersCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForFaersAlert(ec, executedConfigurationFaers?.id, false)
            }
            if (ec.faersCumCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForFaersAlert(ec, executedConfigurationFaers?.id, true)
            }
            if (ec.vigibaseCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVigibaseAlert(ec, executedConfigurationVigibase?.id, false)
            }
            if (ec.vigibaseCumCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVigibaseAlert(ec, executedConfigurationVigibase?.id, true)
            }
            if (ec.vaersCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVaersAlert(ec, executedConfigurationVaers?.id, false)
            }
            if (ec.vaersCumCaseSeriesId) {
                isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVaersAlert(ec, executedConfigurationVaers?.id, true)
            }
        } else {
            isReportingCompleted = true
        }
        if (isReportingCompleted) {
            ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(ec.id, ec.type)
            if (!executionStatus) {
                executionStatus = createExecutionStatus(ec)
            }
            try {
                String updateQuery = "Update ExecutionStatus set spotfireExecutionStatus = :status where id = :id"
                updateExecutionStatusForReporting(updateQuery, [status: ReportExecutionStatus.GENERATING, id: executionStatus?.id])
                log.info("Calling generateSpotfireReport()")
                if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE.name() || params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_SAFETYDB.name()) {
                    executeSpotfireReport(ec, false)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE.name() || params.spotfireDateRange == DateRangeEnum.CUMULATIVE_SAFETYDB.name()) {
                    executeSpotfireReport(ec, true, true)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE_FAERS.name()) {
                    executeSpotfireReportFaers(ec, true)
                } else if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_FAERS.name()) {
                    executeSpotfireReportFaers(ec, false)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE_VIGIBASE.name()) {
                    executeSpotfireReportVigibase(ec, true)
                } else if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_VIGIBASE.name()) {
                    executeSpotfireReportVigibase(ec, false)
                } else if (params.spotfireDateRange == DateRangeEnum.CUMULATIVE_VAERS.name()) {
                    executeSpotfireReportVaers(ec, true)
                } else if (params.spotfireDateRange == DateRangeEnum.PR_DATE_RANGE_VAERS.name()) {
                    executeSpotfireReportVaers(ec, false)
                }
            } catch (Throwable ex) {
                String stackTrace = alertService.exceptionString(ex).replaceAll("'", "''")
                String updateQuery = "Update ExecutionStatus set spotfireExecutionStatus = :status, stackTrace = :stackTrace where id = :id"
                updateExecutionStatusForReporting(updateQuery, [status: ReportExecutionStatus.ERROR, stackTrace: stackTrace, id: executionStatus?.id])
                sendErrorNotification(ec)
                log.error("Failed generate spotfire report file while generating", ex.printStackTrace())
            }
        }
        return true
    }

    ExecutionStatus createExecutionStatus(ExecutedConfiguration scheduledConfiguration) {
        ExecutedConfiguration lockedConfiguration = ExecutedConfiguration.lock(scheduledConfiguration.id)
        ExecutionStatus executionStatus = new ExecutionStatus(executedConfigId: lockedConfiguration.id, configId: lockedConfiguration.configId,
                reportVersion: lockedConfiguration.numOfExecutions + 1,
                startTime: System.currentTimeMillis(), nextRunDate: lockedConfiguration.nextRunDate,
                owner: lockedConfiguration.owner, name: lockedConfiguration.name,
                attachmentFormats: null, sharedWith: null, type: lockedConfiguration.type)
        executionStatus.frequency = configurationService.calculateFrequency(lockedConfiguration)
        executionStatus.executionLevel = 2
        executionStatus.executionStatus = ReportExecutionStatus.COMPLETED
        CRUDService.saveWithoutAuditLog(executionStatus)
        executionStatus
    }


    String composeSpotfireUrl() {
        def spotfireConfig = Holders.config.spotfire

        StringBuilder spotfireUrl = new StringBuilder()

        spotfireUrl.append(spotfireConfig.protocol ?: 'http')
        spotfireUrl.append('://' + spotfireConfig.server)
        if (spotfireConfig.port) {
            spotfireUrl.append(':' + spotfireConfig.port)
        }
        spotfireUrl.append('/' + spotfireConfig.path)

        return spotfireUrl.toString()
    }





}
