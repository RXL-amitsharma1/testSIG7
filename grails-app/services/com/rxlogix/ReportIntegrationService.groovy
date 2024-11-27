package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.reports.integration.ExecutedConfigurationDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.SignalReport
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder
import groovy.sql.Sql
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.apache.http.util.TextUtils
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Propagation
import com.rxlogix.dto.ExecutedGlobalDateRangeInformation

class ReportIntegrationService {


    def aggregateCaseAlertService
    def singleCaseAlertService
    def userService
    def dataSource_pva
    def grailsApplication
    MessageSource messageSource
    def activityService
    LinkGenerator grailsLinkGenerator
    def reportTemplateRestService
    AlertService alertService
    def dataObjectService
    def notificationHelper
    CRUDService CRUDService
    def sessionFactory
    def cacheService
    def userGroupService
    def caseFormService
    def signalAuditLogService


    List<Map> caseDataForExport(ExecutedConfiguration config, String type, Boolean isAlertReport, Long alertId,
                                def typeFlag = false,def params, Boolean isReportScreen = false) {
        if (config.type in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND]) {
            return singleCaseData(config, params,isReportScreen)
        } else if (config.type in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND]) {
            return isAlertReport ? aggregateCaseDataForReport(config.id) : aggregateCaseData(alertId, type, typeFlag, config.id)
        } else
            return null
    }

    List<Map> singleCaseData(ExecutedConfiguration config, def params, Boolean isReportScreen = false) {
        List<Map> caseData = []
        Map filterMap = [:]
        def domain
        if (config.adhocRun && !config.isCaseSeries) {
            domain = SingleOnDemandAlert
        } else if (!config.isLatest  && !config.isCaseSeries) {
            domain = ArchivedSingleCaseAlert
        } else {
            domain = SingleCaseAlert
        }
        List scaList = []
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        if (isReportScreen) {
            if (params.selectedCases && !TextUtils.isEmpty(params.selectedCases)) {
                scaList = singleCaseAlertService.listSelectedAlerts(params.selectedCases, domain) as List

            } else {
                if (params.filterList && params.filterList != "{}") {
                    def jsonSlurper = new JsonSlurper()
                    filterMap = jsonSlurper.parseText(params.filterList) as Map
                    filterMap?.each { def k, def v ->
                        if (k == 'assessListedness' && config.adhocRun) {
                            filterMap.put('listedness', filterMap.remove('assessListedness'))
                        } else if (k == 'assessSeriousness' && config.adhocRun) {
                            filterMap.put('serious', filterMap.remove('assessSeriousness'))
                        } else {
                            filterMap.put(k, v)
                        }
                    }
                }
                alertDataDTO.filterMap = filterMap
                alertDataDTO.domainName = domain
                alertDataDTO.params = params
                alertDataDTO.isVaers = false
                alertDataDTO.isVigibase = false
                alertDataDTO.executedConfiguration = config
                alertDataDTO.execConfigId = config?.id
                Closure advancedFilterClosure
                advancedFilterClosure = alertService.generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
                alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
                List dispositionFilters = caseFormService.getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)
                alertDataDTO.dispositionFilters = dispositionFilters
                alertDataDTO.isProjection = true
                if(config.adhocRun){
                    scaList = alertService.generateAlertListForOnDemandRuns(advancedFilterClosure, alertDataDTO)
                }else{
                    List<Disposition> openDispositions = alertService.getDispositionsForName(alertDataDTO.dispositionFilters)
                    scaList = alertService.generateAlertList(advancedFilterClosure, alertDataDTO, openDispositions)
                }

            }
        } else {
            scaList = SingleCaseAlert?.findAllByExecutedAlertConfiguration(config)
        }

        scaList?.each {  sca ->
            Map data = [id: sca.id, text: sca.name, case_number: sca.caseNumber, version: sca.caseVersion]
            caseData.add(data)
        }
        return caseData
    }

    List<Map> aggregateCaseData(alertId, type, typeFlag, execId) {
        def alert
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execId)
        if(executedConfiguration?.adhocRun){
            alert = AggregateOnDemandAlert.get(alertId)
        } else {
            alert = executedConfiguration.isLatest ? AggregateCaseAlert.get(alertId) : ArchivedAggregateCaseAlert.get(alertId)
        }
        boolean isEventGroup = false
        if(executedConfiguration.eventGroupSelection){
            isEventGroup = true
        }
        List<Map> caseData = aggregateCaseAlertService.caseDrillDown(type, typeFlag, execId, alert.productId, alert.ptCode, 'pva', executedConfiguration.groupBySmq, alert, isEventGroup).collect {
            [case_number: it.get(cacheService.getRptFieldIndexCache('masterCaseNum')), version: it.get(cacheService.getRptFieldIndexCache('masterVersionNum'))]
        }
        return caseData
    }

    List<Map> aggregateCaseDataForReport(Long executedConfigurationId) {
        Sql sql = new Sql(dataSource_pva)
        List<Map> caseList = []
        try {
            String sql_statement = """SELECT DISTINCT ci.case_num case_number, ci.version_num version
           FROM pvs_case_drill_down pcdd JOIN c_identification ci
                ON (    pcdd.case_id = ci.case_id
                    AND pcdd.version_num = ci.version_num
                   )
          WHERE execution_id = ${executedConfigurationId}"""
            sql.eachRow(sql_statement, []) { row ->
                caseList.add([case_number: row.getString('case_number'), verison: row.getString('version')])
            }
        } catch (ConnectException cex) {
            log.error(cex.getMessage(), cex)
        } catch (Throwable th) {
            log.error(th.getMessage(), th)
        }finally{
            sql.close()
        }
        return caseList
    }

    Map getTemplateList(String search, int offset, int max) {
        Map result = [templateList: [], totalCount: 0]
        try {
            String username = userService.getUser().username
            String url = grailsApplication.config.pvreports.url
            String listTemplateURI = grailsApplication.config.pvreports.templates.uri
            Map res = get(url, listTemplateURI, [username: username, search: search, offset: offset, max: max])
            result = res.data
            result['templateList']?.unique{
                it.id
            }

        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    Map getTemplateListForAdHocreport(Map data) {
        Map resultMap = [recordsTotal: 0, recordsFiltered: 0, aaData: []]
        try {
            String url = grailsApplication.config.pvreports.url
            String listTemplateURI = grailsApplication.config.pvreports.adHoc.report.templates.uri
            Map res = get(url, listTemplateURI, data)
            Map response = res["data"]
            resultMap = [recordsFiltered: data.searchString == "" ? response["recordsTotal"]: response["recordsFiltered"], recordsTotal: response["recordsTotal"], aaData: response["aaData"]]
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return resultMap
    }

    Map getQueryList(String search, int offset, int max, boolean isNonParameterisedQuery = false, boolean isFaersQuery = false, boolean isEvdasQuery = false, boolean isSafetyQuery = true, boolean isEmbaseQuery = false) {
        Map result = [queryList: [], totalCount: 0]
        try {
            String username = userService.getUser()?.username
            String url = grailsApplication.config.pvreports.url
            String listQueryURI = grailsApplication.config.pvreports.queries.uri
            Map requestMap = [username: username, search: search, offset: offset, max: max, isNonParameterisedQuery: isNonParameterisedQuery, isFaersQuery: isFaersQuery, isEvdasQuery: isEvdasQuery, isSafetyQuery: isSafetyQuery, isEmbaseQuery: isEmbaseQuery]
            Map res = get(url, listQueryURI, requestMap)
            result = res.data
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    Map getTemplateNameIdList(List<String> templateIdList) {
        Map result = [:]
        try {
            String username = userService.getUser()?.username
            String url = grailsApplication.config.pvreports.url
            String listTemplateIdURI = grailsApplication.config.pvreports.templateIdList.uri
            Map res = get(url, listTemplateIdURI, [templateList: templateIdList.join(",")])
            result.templateIdNameList = res?.data?.templateIdNameList?.collect {
                [id: it.id, text: it.name + " " + (it.description ? "(" + it.description + ")" : ""), name: it.name]
            }
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    Map getQueryNameIdList(List<String> queryIdList) {
        Map result = [:]
        try {
            String username = userService.getUser()?.username
            String url = grailsApplication.config.pvreports.url
            String listQureyIdURI = grailsApplication.config.pvreports.queryIdList.uri
            Map res = get(url, listQureyIdURI, [queryList: queryIdList.join(",")])
            log.info("Name and Id of queries fetched from PVR are: " + (res?.data?.queryIdNameList))
            result.queryIdNameList = res?.data?.queryIdNameList?.collect {
                [id: it.id, text: it.name + " " + (it.description ? "(" + it.description + ")" : ""), name: it.name]
            }
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }


    Long getCioms1Id() {
        Long cioms1Id = 0
        try {
            String url = grailsApplication.config.pvreports.url
            String cioms1IdURI = grailsApplication.config.pvreports.ciomsId.uri
            Map res = get(url, cioms1IdURI, null)
            cioms1Id = res.data.cioms1Id
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return cioms1Id
    }

    Map importConfiguration(String templateIds, ExecutedConfiguration executedConfiguration, String type, Long alertId,
                            Boolean typeFlag, String linkUrl, Boolean isAlertReport = false) {
        String url = grailsApplication.config.pvreports.url
        String importConfigurationURI = grailsApplication.config.pvreports.import.configuration.uri
        User user = executedConfiguration.assignedTo

        List<Map> caseData = caseDataForExport(executedConfiguration, type, isAlertReport, alertId, typeFlag)
        Map configData = [name                          : executedConfiguration.reportName ?: executedConfiguration.name, version: executedConfiguration.version,
                          description                   : executedConfiguration.description,
                          productSelection              : executedConfiguration.productSelection,
                          eventSelection                : executedConfiguration.eventSelection,
                          studySelection                : executedConfiguration.studySelection,
                          dateRangeType                 : executedConfiguration.dateRangeType.value(),
                          asOfVersionDate               : executedConfiguration.asOfVersionDate,
                          excludeFollowUp               : executedConfiguration.excludeFollowUp,
                          excludeNonValidCases          : executedConfiguration.excludeNonValidCases,
                          includeMedicallyConfirmedCases: 0,
                          executedDeliveryOption        : [ReportFormat.PDF],
                          isAlertReport                 : isAlertReport,
                          userId                        : user.id,
                          linkUrl                       : linkUrl,
                          templateId                    : templateIds]

        StringWriter writer = new StringWriter()
        try {
            StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
            builder.reportBuilder(config: configData, cases: caseData,
                    user: ["user": user.username])

        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            return [status: -1]
        }
        log.info("Calling Report Generation API for \"${executedConfiguration.reportName} (EC:${executedConfiguration.id})\"")
        Map res = postData(url, importConfigurationURI, writer.toString())
        return res
    }

    void runReport(Long executedConfigurationId) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigurationId)
        if (executedConfiguration && !(executedConfiguration.reportId) && executedConfiguration.executedTemplateQueries?.size()) {
            ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfiguration.id,executedConfiguration.type)
            updateExecutionStatusOnly(executionStatus, ReportExecutionStatus.GENERATING)
            log.info("runReport called for EC: ${executedConfigurationId}")
            try {
                executeReport(executedConfiguration)
            } catch (Throwable th) {
                log.error("Error occured in runReport" + th.getMessage())
                ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set reportExecutionStatus = :executionStatus where id = :id",
                        [executionStatus:ReportExecutionStatus.ERROR,id:executedConfigurationId])
                updateExecutionStatusOnly(executionStatus, ReportExecutionStatus.ERROR, alertService.exceptionString(th).replaceAll("'","''"))
                saveConfigurationAndReportNotificationAndActivity(executedConfiguration)
            }
        }
    }

    void executeReport(ExecutedConfiguration executedConfiguration) throws Exception {
        if (!(executedConfiguration?.reportId)) {
            log.info("executeReport called for EC: ${executedConfiguration.id}")
            try {
                Long pvrCaseSeriesId = getCumulativeCaseSeriesId(executedConfiguration)
                Date startDate = dataObjectService.getfirstVersionExecMap(executedConfiguration.id)
                ExecutedConfigurationDTO executedConfigurationDTO = new ExecutedConfigurationDTO(executedConfiguration,0,pvrCaseSeriesId,null,startDate)
                executedConfigurationDTO.executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation()
                executedConfigurationDTO.executedGlobalDateRangeInformation.dateRangeStartAbsolute = executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute
                executedConfigurationDTO.executedGlobalDateRangeInformation.dateRangeEndAbsolute = executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute
                executedConfigurationDTO.executedGlobalDateRangeInformation.executedAsOfVersionDate = executedConfiguration.asOfVersionDate
                executedConfigurationDTO.executedGlobalDateRangeInformation.relativeDateRangeValue = executedConfiguration.executedAlertDateRangeInformation.relativeDateRangeValue
                executedConfigurationDTO.executedGlobalDateRangeInformation.dateRangeEnum = executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum
                Configuration configuration =  alertService.getAlertConfigObjectByType(executedConfiguration)
                executedConfigurationDTO.sharedWithUsers = fetchSharedUsers(configuration, executedConfiguration)
                executedConfigurationDTO.sharedWithGroups = fetchSharedGroups(configuration, executedConfiguration)
                executedConfigurationDTO.callbackURL = grailsLinkGenerator.link(base:grailsApplication.config.signal.serverURL,controller: 'publicReport', action: 'reportsCallback', id: executedConfiguration.id)
                executedConfigurationDTO.isMultiIngredient = executedConfiguration.isMultiIngredient ?: false
                executedConfigurationDTO.includeWHODrugs = executedConfiguration.includeWHODrugs ?: false
                String url = Holders.config.pvreports.url
                String path = Holders.config.pvreports.importConfiguration.uri
                Map response = postData(url, path, executedConfigurationDTO)
                log.info("Executed Report API Response from PVR : ${response}")
                if (response.status == HttpStatus.SC_OK) {
                    if (response.result.status && response.result.data) {
                        ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set reportId = :reportId where id = :id",
                                [reportId : response.result.data as Long,id:executedConfiguration.id])
                    } else {
                        throw new Exception("Error in creating Executed Report in PVR")
                    }
                } else {
                    throw new Exception("Something unexpected happen in PVR" + response.status)
                }
            } catch (Throwable th) {
                log.error("Error occured in executeReport" + th.getMessage())
                throw th
            }
        }
    }

    Map executeAdhocReport(ExecutedConfiguration executedConfiguration, Long templateId, Long seriesId, SignalReport signalReport, String type = "", String preferredTerm = "") {
        log.info("executeAdhocReport called for report ${signalReport.reportName} [templateId:${templateId}] for EC: ${executedConfiguration.id}")
        Map res = [status: false]
        try {
            ExecutedConfigurationDTO executedConfigurationDTO = new ExecutedConfigurationDTO(executedConfiguration, templateId, seriesId, signalReport.reportName, null, type)
            Configuration configuration =  alertService.getAlertConfigObjectByType(executedConfiguration)
            executedConfigurationDTO.executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation()
            executedConfigurationDTO.executedGlobalDateRangeInformation.dateRangeStartAbsolute = executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute
            executedConfigurationDTO.executedGlobalDateRangeInformation.dateRangeEndAbsolute = executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute
            executedConfigurationDTO.executedGlobalDateRangeInformation.executedAsOfVersionDate = executedConfiguration.asOfVersionDate
            executedConfigurationDTO.executedGlobalDateRangeInformation.relativeDateRangeValue = executedConfiguration.executedAlertDateRangeInformation.relativeDateRangeValue
            executedConfigurationDTO.executedGlobalDateRangeInformation.dateRangeEnum = executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum
            executedConfigurationDTO.isMultiIngredient = executedConfiguration.isMultiIngredient ?: false
            executedConfigurationDTO.includeWHODrugs = executedConfiguration.includeWHODrugs ?: false
            executedConfigurationDTO.sharedWithUsers = fetchSharedUsers(configuration, executedConfiguration)
            executedConfigurationDTO.sharedWithGroups = fetchSharedGroups(configuration, executedConfiguration)
            executedConfigurationDTO.callbackURL = grailsLinkGenerator.link(base:grailsApplication.config.signal.serverURL,controller: 'publicReport', action: 'adhocReportsCallback', params: [id: signalReport.id])
            String url = Holders.config.pvreports.url
            String path = Holders.config.pvreports.importConfiguration.uri
            Map response = postData(url, path, executedConfigurationDTO)
            createAuditForReportGenerate(signalReport,"(Executing)", preferredTerm)
            //To do
            //log.info("Executed Adhoc Report API Response from PVR : ${response}")
            if (response.status == HttpStatus.SC_OK) {
                if (response.result.status && response.result.data) {
                    signalReport.reportId = response.result.data as Long
                    signalReport.save()
                    res.status = true
                } else {
                    throw new Exception("Error in creating Executed Adhoc Report in PVR")
                }
            } else {
                throw new Exception("Something unexpected happen in PVR" + response.status)
            }
        } catch (Throwable th) {
            log.error("Error occured in executeAdhocReport" + th.getMessage())
            signalReport.reportExecutionStatus = ReportExecutionStatus.ERROR
            reportTemplateRestService.saveSignalReportNotification(signalReport)
        }
        return res
    }

    void createAuditForReportGenerate(SignalReport signalReport, def status = "(Executed)", String preferredTerm = "") {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(signalReport.executedAlertId as Long)
        String productString = executedConfiguration.productGroupSelection ? AuditLogConfigUtil.getGroupNameFieldFromJsonForAudit(executedConfiguration.productGroupSelection) : AuditLogConfigUtil.getProductSelectionValuesForAudit(executedConfiguration.productSelection)
        //getProductSelectionValuesForAudit
        if (executedConfiguration.eventSelection || executedConfiguration.eventGroupSelection) {
            productString += "-" + executedConfiguration.eventGroupSelection ? AuditLogConfigUtil.getEventGroupFromJsonForAudit(executedConfiguration.eventGroupSelection) : AuditLogConfigUtil.getAllEventNameFieldFromJsonForAudit(executedConfiguration.eventSelection)
        }
        User user =User.get(signalReport?.userId)
        if(preferredTerm == "undefined") {
            preferredTerm = ""
        }

        def auditTrailMap = [
                entityName : 'singleCaseReportGeneration',
                moduleName : Constants.AuditLog.typeToEntityMap.get(executedConfiguration.type) + (executedConfiguration.isLatest ? ": Report" : ": Archived Alert: Report"),
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: executedConfiguration.getInstanceIdentifierForAuditLog() + (preferredTerm ? "-(PT - " + preferredTerm + ")" : "") + "(${productString})${status}",
                description: "Report generation started",
                username   : status != "(Executing)" ? Constants.Commons.SYSTEM : user.username,
                fullname   : status != "(Executing)" ? Constants.Commons.BLANK_STRING : user.fullName
        ]
        List<Map> auditChildMap = []
        def childEntry = [:]
        childEntry = [
                propertyName: "Report Name",
                newValue    : signalReport.reportName]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Owner Name",
                newValue    : user.fullName]
        auditChildMap << childEntry
        signalAuditLogService.createAuditLog(auditTrailMap, auditChildMap)
    }

    void saveConfigurationAndReportNotificationAndActivity(ExecutedConfiguration ec) {
        Activity activity = saveReportNotificationAndActivity(ec)
        String insertExConfigActivityQuery = "INSERT INTO ex_rconfig_activities(EX_CONFIG_ACTIVITIES_ID,ACTIVITY_ID) VALUES(?,?)"
        alertService.batchPersistExecConfigActivityMapping(ec.id, [activity.id], insertExConfigActivityQuery)
    }

    Activity saveReportNotificationAndActivity(ExecutedConfiguration ec) {
        InboxLog signalNotification
        Set<Long> users = ec.assignedToGroup ? userGroupService.fetchUserListIdForGroup(ec.assignedToGroup) : [ec.assignedTo?.id]
        String detailUrl = ""
        String message = ""
        String type = ""
        String subject = ""
        NotificationLevel level = NotificationLevel.ERROR
        try {
            switch (ec.reportExecutionStatus) {
                case ReportExecutionStatus.COMPLETED:
                    level = NotificationLevel.INFO
                    message = "app.signal.report.saved"
                    type = "Report Generated"
                    subject = "Report \"${ec.name}\" associated with \"${ec.name}\" is completed."
                    detailUrl = Holders.config.pvreports.reports.view.uri + "/" + ec.reportId
                    break
                case ReportExecutionStatus.WARN:
                    level = NotificationLevel.WARN
                    message = "app.signal.report.saved"
                    type = "Report Generated"
                    subject = "Report \"${ec.name}\" associated with \"${ec.name}\" is completed."
                    detailUrl = Holders.config.pvreports.reports.view.uri + "/" + ec.reportId
                    break
                case ReportExecutionStatus.ERROR:
                default:
                    level = NotificationLevel.ERROR
                    message = "app.signal.report.failed"
                    type = "Report Failed"
                    subject = "Report \"${ec.name}\" associated with \"${ec.name}\" is failed."
                    detailUrl = ""
                    break
            }
            String reportName = ec.type == 'Single Case Alert' ? ec.name : ('(' + ec.productName + '-' + ec.getAllEventNameFieldFromJson(ec.eventSelection) + ')')
            List<Map> auditChildMap = []
            def childEntry = [:]
            childEntry = [
                    propertyName: "Report Name",
                    newValue    : ec.name]
            auditChildMap << childEntry
            childEntry = [
                    propertyName: "Owner",
                    newValue    : ec.owner?.fullName ? ec.owner.fullName : ec.owner?.username ]
            auditChildMap << childEntry
            signalAuditLogService.createAuditLog([
                    entityName : Constants.AuditLog.typeToEntityMap.get(ec.type),
                    moduleName : Constants.AuditLog.typeToEntityMap.get(ec.type) + ": Report",
                    category   : AuditTrail.Category.INSERT.toString(),
                    entityValue: ec.getInstanceIdentifierForAuditLog() + ": " + type,
                    description: "Generated Report: Case line listing (${reportName})",
                    username   : ec.owner.username,
                    fullname   : ec.owner.fullName
            ] as Map, auditChildMap)

            users.each { Long userId ->
                signalNotification = new InboxLog()
                signalNotification.createdOn = new Date()
                signalNotification.detailUrl = detailUrl
                signalNotification.messageArgs = ec.name
                signalNotification.message = message
                signalNotification.type = type
                signalNotification.subject = subject
                signalNotification.level = level
                signalNotification.isRead = false
                signalNotification.notificationUserId = userId
                signalNotification.inboxUserId = userId
                signalNotification.content = "<span>${type}</span>"
                signalNotification.save(flush: true)
                notificationHelper.pushNotification(signalNotification)
            }
            activityService.setActivityForAlert(ec)
        } catch (Exception ex) {
            log.error("Error occcoured in saveConfigurationAndReportNotificationAndActivity.\n" + ex.printStackTrace())
        }
    }

    Map fetchPublicToken(String appName = "PVR") {
        Map publicToken = (appName == "PVR") ? Holders.config.pvr.publicApi.token : Holders.config.pvadmin.publicApi.token
        publicToken
    }

    Map get(String url, String path, Map query, String appName = "PVR") {
        Map ret = [:]
        int retryCount = 3
        int retryTimes = 0
        while(retryTimes < retryCount){
            log.info("API Call Retry time: ${retryTimes + 1}")
            try {
                RESTClient endpoint = new RESTClient(url)
                Map publicToken = fetchPublicToken(appName)
                endpoint.handler.failure = { resp -> ret = [status: resp.status] }
                if (publicToken)
                    endpoint.setHeaders(publicToken)
                def resp = endpoint.get(
                        path: path,
                        query: query
                )

                if (resp.status == 200) {
                    ret = [status: resp.status, data: resp.data]
                    break
                }
            } catch (ConnectException ct) {
                log.error(ct.getMessage())
            } catch (Throwable t) {
                log.error(t.getMessage(), t)
            }
            retryTimes++
        }
        return ret
    }

    Map postData(String baseUrl, String path, def data, method = Method.POST, String appName = "PVR") {

        Map ret = [:]
        HTTPBuilder http = new HTTPBuilder(baseUrl)
        Map publicToken = fetchPublicToken(appName)
        int retryCount = 3
        int retryTimes = 0
        while(retryTimes < retryCount) {
            log.info("API Call Retry time: ${retryTimes + 1}")
            try {
                // perform a POST request, expecting JSON response
                http.request(method, ContentType.JSON) {
                    uri.path = path
                    body = data

                    if (publicToken)
                        headers = publicToken

                    // response handlers
                    response.success = { resp, reader ->
                        ret = [status: 200, result: reader]
                    }
                    response.failure = { resp, reader ->
                        log.error(reader.message)
                        ret = [status: 500, error: reader.message ?: ""]
                    }
                }
                if(ret.status == 200){
                    break
                }

            } catch (ConnectException ct) {
                log.error(ct.getMessage())
                ret = [status: 500, error: ct.getMessage()]
            } catch (Throwable t) {
                log.error(t.getMessage(), t)
                ret = [status: 500, error: t.getMessage()]
            }
            retryTimes++
        }
        return ret
    }

    String fetchReportUrl(ExecutedConfiguration ec, Boolean isAlertBursting = false) {
        String reportUrl = Constants.Commons.BLANK_STRING
        if(isAlertBursting){
            return reportUrl
        }
        if (ec) {
            reportUrl = ec.reportExecutionStatus == ReportExecutionStatus.COMPLETED && ec.reportId ? Holders.config.pvreports.reports.view.uri + "/${ec.reportId}" : Constants.Commons.BLANK_STRING
        }
        return reportUrl
    }

    String getExecutedReportUrl(Long reportId) {
        reportId ? Holders.config.pvreports.reports.view.uri + "/${reportId}" : ""
    }

    void saveSignalReportNotification(String linkUrl, String reportName, Long userId, NotificationLevel level, String type, String subject) {
        InboxLog signalNotification = new InboxLog()
        signalNotification.createdOn = new Date()
        signalNotification.detailUrl = linkUrl
        signalNotification.messageArgs = reportName
        signalNotification.message = "app.signal.report.saved"
        signalNotification.type = type
        signalNotification.subject = subject
        signalNotification.level = level
        signalNotification.isRead = false
        signalNotification.notificationUserId = userId
        signalNotification.inboxUserId = userId
        signalNotification.content = "<span>${type}</span>"
        signalNotification.save(failOnError: true, flush: true)
    }

    List<String> fetchSharedGroups(Configuration configuration, ExecutedConfiguration executedConfiguration) {
        if (configuration.isAutoAssignedTo) {
            return executedConfiguration.autoShareWithGroup.collect { it.name.trim() }
        } else {
            return configuration.shareWithGroup.collect { it.name.trim() }
        }
    }

    List<String> fetchSharedUsers(Configuration configuration, ExecutedConfiguration executedConfiguration) {
        if (configuration.isAutoAssignedTo) {
            return executedConfiguration.autoShareWithUser.collect { it.username.trim() }
        } else {
            return configuration.shareWithUser.collect { it.username.trim() }
        }
    }

    Map postDataWithoutPath(String baseUrl,  def data, method = Method.POST) {

        Map ret = [:]
        HTTPBuilder http = new HTTPBuilder(baseUrl)
        Map publicToken = fetchPublicToken()

        try {
            // perform a POST request, expecting JSON response
            http.request(method, ContentType.JSON) {

                body = data

                if (publicToken)
                    headers = publicToken

                // response handlers
                response.success = { resp, reader -> ret = [status: 200, result: reader] }
                response.failure = { resp -> ret = [status: 500] }
            }
        } catch (ConnectException ct) {
            log.error(ct.getMessage())
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
        }
        return ret
    }

    Long getCumulativeCaseSeriesId(ExecutedConfiguration executedConfiguration){
        if(executedConfiguration.pvrCaseSeriesId && executedConfiguration.pvrCumulativeCaseSeriesId){
            if(executedConfiguration.pvrCumulativeCaseSeriesId != executedConfiguration.pvrCaseSeriesId){
                return executedConfiguration.pvrCumulativeCaseSeriesId
            }
        }
        return 0
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateExecutionStatusOnly(ExecutionStatus executionStatus, ReportExecutionStatus status, String stackTrace="") {
        String updateQuery = "Update ExecutionStatus set reportExecutionStatus=:status, stackTrace= :stackTrace where id = :id"
        ExecutionStatus.executeUpdate(updateQuery, [status: status, stackTrace:stackTrace,id: executionStatus.id])
    }

    Map fetchCaseSeriesList(String search, int offset, int max, String username) {
        Map result = [queryList: [], totalCount: 0]
        try {
            String url = grailsApplication.config.pvreports.url
            String listQueryURI = grailsApplication.config.pvreports.caseSeries.list.uri
            Map requestMap = [username: username, searchString: search, offset: offset, max: max]
            Map res = get(url, listQueryURI, requestMap)
            result = res.data
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    Map fetchLatestCaseSeriesId(Long exCaseSeriesId) {
        Map result = [:]
        try {
            String url = grailsApplication.config.pvreports.url
            String listQueryURI = grailsApplication.config.pvreports.caseSeries.latest.id
            Map requestMap = [id:exCaseSeriesId]
            Map res = get(url, listQueryURI, requestMap)
            result = res.data
        } catch (ConnectException cex) {
            log.error(cex.getMessage())
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        return result
    }

    def jsonToMap(def jsonContent) {
        if (!jsonContent) throw new Throwable("Empty JSON content")
        String jsonToString =  jsonContent.toString()
        JsonSlurper jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(jsonToString)
        return json
    }

}
