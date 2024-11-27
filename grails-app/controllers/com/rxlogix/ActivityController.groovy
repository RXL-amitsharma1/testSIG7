package com.rxlogix

import com.rxlogix.config.Activity
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.helper.ActivityHelper
import com.rxlogix.signal.Alert
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.signal.ViewInstance
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.ClobUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.config.EvdasAlert
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.type.StringType

import java.text.SimpleDateFormat

import static com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT
import static com.rxlogix.util.DateUtil.fromDateToString

@Secured(["isAuthenticated()"])
class ActivityController implements AlertUtil{
    def activityService
    def exportService
    def dynamicReportService
    def alertService
    def userService
    def validatedSignalService
    def singleCaseAlertService
    def evdasAlertService
    def aggregateCaseAlertService
    def sessionFactory
    def signalAuditLogService
    ClobUtil clobUtilInstance = new ClobUtil()
    def index() {}

    def activitiesByAlert() {
        def alertId = params.id
        def activities = activityService.listByAlert(alertId as Long).collect {it.toDto()}

        response.status = 200
        render activities as JSON
    }

    def activitiesBySignal() {
        def signalId = params.id
        def activities = validatedSignalService.listActivities(signalId)
        response.status = 200
        render activities.collect { it.toDto() }.sort{-it.activity_id} as JSON
    }

    def activitiesByTopic() {
        def topicId = params.id
        def activities = validatedSignalService.listTopicActivities(topicId)
        response.status = 200
        render activities.collect { it.toDto() } as JSON
    }

    def allActivities() {render Activity.findAll() as JSON }

    def listByAlertType() {
        def alertType = params.get('alertType')
        def activities = activityService.listByAlertType(alertType).collect {it.toDto()}
        response.status = 200
        render activities as JSON
    }

    def listByExeConfig() {
        List acaList = []
        response.status = 200
        Set executedIdList = params.get("executedIdList")?.split(",") as Set
        acaList = activityService.listActivitiesByExConfig(executedIdList, params.appType as String)
        respond acaList, [formats:['json']]
    }

    def listActivities() {
        def activities
        if(params.appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            activities = Activity.findAllByCaseNumberIsNotNullAndAssignedTo(userService.getUser())
        } else  if(params.appType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.EVDAS_ALERT] ) {
            String searchAlertQuery = (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT)? (SignalQueryHelper.agg_activity_from_dashboard(userService.getUser().id)) : (SignalQueryHelper.evdas_activity_from_dashboard(userService.getUser().id));
            activities = getResultList(Activity.class, searchAlertQuery)
        }
        String currentUserName = userService.getUser()?.username
        def acaList = activities.collect {Activity a ->
            if(!a.privateUserName || (a.privateUserName && a.privateUserName == currentUserName)) {
                a.toDto()
            }
        }
        acaList.removeAll{it == null}
        respond acaList.flatten(), [formats:['json']]
    }


    List<Activity> getResultList(Class className, String sql){
        List<Activity> activityList = new ArrayList<>()
        List activityIds = new ArrayList<>()
        Activity.withTransaction {
            Session session = sessionFactory.currentSession
            try {
                SQLQuery sqlQuery = session.createSQLQuery(sql)
                sqlQuery.setResultTransformer(CriteriaSpecification.PROJECTION)
                activityIds= sqlQuery.list().collect{it.longValue()}
                if (Objects.nonNull(activityIds) && activityIds.size() < 1000) {
                    activityList = Activity.getAll(activityIds);
                } else {
                    def result
                    activityIds?.collate(65000)?.each { batch ->
                        result = Activity.createCriteria().list {
                            if (batch) {
                                or {
                                    batch.collate(999).each {
                                        'in'("id", it)
                                    }
                                }
                            }
                        }
                        if(result){
                            activityList.addAll(result)
                        }
                    }
                }
            } catch(Exception ex) {
                log.error("Exception: "+ex.toString())
                ex.printStackTrace()
            } finally {
                session.flush()
                session.clear()
            }
        }
        activityList
    }

    def exportByAlert() {
        def alertId = params.get("id")
        def alert = Alert.findById alertId
        def type = params.get('format')

        def acaList = activityService.listByAlert(alert.id).collect {it.toDto()}

        if (acaList) {
            response.contentType = grailsApplication.config.grails.mime.types[type]
            response.setHeader("Content-disposition", "attachment; filename=activities.${params.extension}")

            exportService.export(type, response.outputStream, acaList, [:],
                    [
                            'title': "Activities by Alert(${alertId})"
                    ]
            )
        }

        [list: acaList]
    }

    // TODO: Need to optimise the code, currently multiple loops on same list is being applied which is causing slowness in export
    def exportActivitiesReport(Integer alertId){
        def acaList
        def exeAlert
        def alert = null
        String productName=null;
        List criteriaSheetList
        Session session = sessionFactory.currentSession
        String currentUserName = userService.getUser()?.username
        def auditEntityValue
        def auditEntityModule

        if (params.callingScreen == Constants.Commons.DASHBOARD) {
            if(params.appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                auditEntityValue=Constants.AuditLog.SINGLE_REVIEW_DASHBOARD
                auditEntityModule=Constants.AuditLog.SINGLE_REVIEW_DASHBOARD
                acaList = Activity.findAllByCaseNumberIsNotNullAndAssignedTo(userService.getUser()).collect{
                    if(!it.privateUserName || (it.privateUserName && it.privateUserName == currentUserName)) {
                        it.toDto()
                    }
                }
            } else if (params.appType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.EVDAS_ALERT]) {
                if(params.appType==Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                    auditEntityValue=Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD
                    auditEntityModule=Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD
                }else if(params.appType==Constants.AlertConfigType.EVDAS_ALERT){
                    auditEntityValue=Constants.AuditLog.EVDAS_REVIEW_DASHBOARD
                    auditEntityModule=Constants.AuditLog.EVDAS_REVIEW_DASHBOARD
                }

                String searchAlertQuery = (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) ? (SignalQueryHelper.agg_activity_from_dashboard(userService.getUser().id)) : (SignalQueryHelper.evdas_activity_from_dashboard(userService.getUser().id));
                List<Activity> activities = getResultList(Activity.class, searchAlertQuery)
                acaList = activities.collect {Activity activity ->
                    if (!activity.privateUserName || (activity.privateUserName && activity.privateUserName == currentUserName)) {
                        activity.toDto()
                    }
                }
            }
            params.criteriaSheetList = dynamicReportService.createCriteriaList(userService.getUser())
        } else if(params.appType == Constants.AlertConfigType.AD_HOC_ALERT){
            AdHocAlert adHocAlert = AdHocAlert.findById(params.alertId)
            acaList = Activity.findAllByAlert(adHocAlert).collect {
                it.toDto()
            }
            acaList.each{
                if(!it['performedByDept'].isEmpty()){
                    it['performedByDept']=it['performedByDept'].toString().replace(Constants.ActivityExportRegex.OPEN_SQUARE_BRACKET,Constants.ActivityExportRegex.OPEN_CURLY_BRACKET).replace(Constants.ActivityExportRegex.CLOSED_SQUARE_BRACKET,Constants.ActivityExportRegex.CLOSED_CURLY_BRACKET)
                }
                it['type'] = activityService.breakActivityType(it['type'] as String)
            }
            alert = AdHocAlert.findById(alertId)
            params.eventName = alert?.eventSelection ? getNameFieldFromJson(alert?.eventSelection) : (getGroupNameFieldFromJson(alert?.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)
            params.alertName = alert.name
            params.productName = getNameFieldFromJson(alert.productSelection)
            params.topicName = alert.topic
            String timeZone = userService.getCurrentUserPreference()?.timeZone
            productName= alert.productSelection ? ViewHelper.getDictionaryValues(alert, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(alert, DictionaryTypeEnum.PRODUCT_GROUP)
            if(null==productName || 'null'.equalsIgnoreCase(productName)){
                productName=params.productName;
            }
            if(!alert.productSelection){
                params.productName = productName
            }
            auditEntityValue = alert.name
            auditEntityModule = Constants.AuditLog.ADHOC_REVIEW
            criteriaSheetList = [
                    ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': alert?.name],
                    ['label': Constants.CriteriaSheetLabels.DESCRIPTION, 'value': alert?.description ?:Constants.Commons.BLANK_STRING],
                    ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': productName],
                    ['label': Constants.CriteriaSheetLabels.EVENT_SELECTION, 'value': alert.eventSelection ? getNameFieldFromJson(alert.eventSelection) : (getGroupNameFieldFromJson(alert.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)],
                    ['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': alert?.disposition?.displayName?:Constants.Commons.BLANK_STRING],
                    ['label': Constants.CriteriaSheetLabels.CREATED_DATE, 'value': DateUtil.stringFromDate(alert?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
                    ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName?:""],
                    ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
            ]
            params.criteriaSheetList = criteriaSheetList

        } else {
            if (params.appType == Constants.AlertConfigType.EVDAS_ALERT) {
                exeAlert = ExecutedEvdasConfiguration.findById(params.alertId)
                List currentDispositionList = []
                EvdasAlert.findAllByExecutedAlertConfiguration(exeAlert).each{
                    currentDispositionList.add(it?.disposition.displayName)
                }
                auditEntityValue = exeAlert.getInstanceIdentifierForAuditLog()
                auditEntityModule = Constants.AuditLog.EVDAS_REVIEW
                def uniqueDispositions = currentDispositionList.toSet()
                String quickFilterDisposition = uniqueDispositions?.join(", ")
                params.quickFilterDisposition = quickFilterDisposition
                criteriaSheetList = evdasAlertService.getEvdasAlertCriteriaData(exeAlert, params)
            } else {
                exeAlert = ExecutedConfiguration.findById(params.alertId)
                if(params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                    List currentDispositionList = []
                    AggregateCaseAlert.findAllByExecutedAlertConfiguration(exeAlert)?.each{
                        currentDispositionList.add(it?.disposition.displayName)
                    }
                    auditEntityValue = exeAlert.getInstanceIdentifierForAuditLog()
                    auditEntityModule = Constants.AuditLog.AGGREGATE_REVIEW
                    def uniqueDispositions = currentDispositionList.toSet()
                    String quickFilterDisposition = uniqueDispositions?.join(", ")
                    params.quickFilterDisposition = quickFilterDisposition
                    if(exeAlert.selectedDatasource == Constants.DataSource.JADER){
                        criteriaSheetList = aggregateCaseAlertService.getJaderAggregateCaseAlertCriteriaData(exeAlert, params,null)
                    }else {
                        criteriaSheetList = aggregateCaseAlertService.getAggregateCaseAlertCriteriaData(exeAlert, params, null, true)
                    }
                }
                else{
                    List currentDispositionList = []
                    String singleCaseAlertQuery = SignalQueryHelper.singleCase_activity_list(exeAlert?.id)
                    SQLQuery sqlQuery = session.createSQLQuery(singleCaseAlertQuery)
                    sqlQuery.addScalar("display_name", new StringType())
                    currentDispositionList = sqlQuery.list()
                    String quickFilterDisposition = currentDispositionList?.join(", ")
                    params.quickFilterDisposition = quickFilterDisposition
                    auditEntityValue = exeAlert.getInstanceIdentifierForAuditLog()
                    auditEntityModule = Constants.AuditLog.SINGLE_REVIEW
                    if(exeAlert.selectedDatasource == Constants.DataSource.JADER){
                        criteriaSheetList = singleCaseAlertService.getSingleCaseAlertCriteriaData(exeAlert, params,null,true,true)
                    }
                    else{
                        criteriaSheetList = singleCaseAlertService.getSingleCaseAlertCriteriaData(exeAlert, params,null,true)
                    }
                }

            }
            acaList = activityService.listActivitiesByExConfig([exeAlert.id as Long] as Set, params.appType as String)
            acaList.removeAll([null])
            acaList.each{
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a")
                Date timestampDate = dateFormat.parse(it['timestamp'])
                it['timestamp'] = new Date(DateUtil.toDateStringWithTime(timestampDate, userService.getCurrentUserPreference().timeZone)).format(DateUtil.DATEPICKER_FORMAT_AM_PM)
                if(it['performedByDept']){
                    it['performedByDept']=it['performedByDept'].toString().replace(Constants.ActivityExportRegex.OPEN_SQUARE_BRACKET,Constants.ActivityExportRegex.OPEN_CURLY_BRACKET).replace(Constants.ActivityExportRegex.CLOSED_SQUARE_BRACKET,Constants.ActivityExportRegex.CLOSED_CURLY_BRACKET)
                }
                if(it['currentAssignmentDept']){
                    it['currentAssignmentDept']=it['currentAssignmentDept'].toString().replace(Constants.ActivityExportRegex.OPEN_SQUARE_BRACKET,Constants.ActivityExportRegex.OPEN_CURLY_BRACKET).replace(Constants.ActivityExportRegex.CLOSED_SQUARE_BRACKET,Constants.ActivityExportRegex.CLOSED_CURLY_BRACKET)
                }
                it['type'] = activityService.breakActivityType(it['type'] as String)
            }
            params.criteriaSheetList = criteriaSheetList
        }
        session.flush()
        session.clear()
        acaList.removeAll{it == null}
        def dateFormat = new SimpleDateFormat('dd-MMM-yyyy hh:mm:ss a')
        acaList.sort { a, b ->
            def dateA = dateFormat.parse(a.timestamp)
            def dateB = dateFormat.parse(b.timestamp)
            dateB <=> dateA
        }
        acaList.each{
            it.performedBy = it.performedByDept?it.performedBy + " (${it.performedByDept})" : it.performedBy
            it.currentAssignment = it.currentAssignmentDept?it.currentAssignment + " (${it.currentAssignmentDept})" : it.currentAssignment
            it.details = it.justification?it.details + " -- with Justification '" + it.justification + "'":it.details
            it.timestamp = dateFormat.format(dateFormat.parse(it.timestamp))
        }
        def reportName = exeAlert?.name ?: params.appType

        def reportFile = dynamicReportService.createActiviesReport(new JRMapCollectionDataSource(acaList), params, reportName)
        renderReportOutputType(reportFile,params)
        signalAuditLogService.createAuditForExport(criteriaSheetList, auditEntityValue + " : Activities", auditEntityModule, params, reportFile.name)
    }

    def exportSignalActivitiesReport(){
        def signal = ValidatedSignal.findById(params.signalId)
        def acaList = signal?.activities?.collect {
            it.toDto()
        }
        Integer maxSize = 1
        acaList.each{
            it.performedBy = it.performedByDept?it.performedBy + it.performedByDept : it.performedBy
            it.details = it.justification?it.details + " -- with Justification '" + it.justification + "'":it.details
            it.type = activityService.breakActivityType(it.type as String)
            if(it.details){
                def details = it.details?.replace('<br>','\n')
                it.remove('details')
                List detailsList = clobUtilInstance.splitClobContent(details as String)
                maxSize = detailsList.size() > maxSize ? detailsList.size() : maxSize
                Integer i = 1
                detailsList.each { detail ->
                    String key = "details" + i
                    it.put(key, detail)
                    i++
                }
            }
        }
        params << [maxSize: maxSize]
        acaList = acaList?.sort{
            -it.activity_id
        }
        String product = signal.getProductAndGroupNameList()?.join(",") ?: ""
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        List criteriaSheetList =[
                ['label': Constants.CriteriaSheetLabels.SIGNAL_NAME, 'value': signal.name?:""],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value':product ],
                ['label': Constants.CriteriaSheetLabels.EVENTS, 'value': alertService.eventSelectionSignalWithSmq(signal)],
                ['label': Constants.CriteriaSheetLabels.DETECTED_DATE, 'value': fromDateToString(signal.detectedDate, DateUtil.DEFAULT_DATE_FORMAT)?:""],
                ['label': Constants.CriteriaSheetLabels.SIGNAL_SOURCE, 'value': signal.initialDataSource?.replaceAll("##", ", ")?:""],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName?:""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)? (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone)) : ""],
                ['label': Constants.CriteriaSheetLabels.PRIORITY, 'value': signal.priority?.displayName?:""],
                ['label': Constants.CriteriaSheetLabels.ASSIGNED_TO, 'value': signal.assignedTo?.name?:signal.assignedToGroup?.name],
        ]
        params.criteriaSheetList = criteriaSheetList
        def reportName = signal.name
        def reportFile = dynamicReportService.createActiviesReport(new JRMapCollectionDataSource(acaList), params, reportName)
        renderReportOutputType(reportFile,params)
        signalAuditLogService.createAuditForExport(criteriaSheetList, signal.getInstanceIdentifierForAuditLog(), "Signal: Activity", params, reportName)
    }

    def exportTopicActivitiesReport() {
        def topic = Topic.get(params.topicId)
        def acaList = topic?.activities?.collect {
            it.toDto()
        }
        acaList.each {
            it.performedBy = it.performedByDept ? it.performedBy + it.performedByDept : it.performedBy
            it.details = it.justification ? it.details + " -- with Justification '" + it.justification + "'": it.details
        }
        acaList = acaList?.sort {
            -it.activity_id
        }
        def reportName = topic.name
        def reportFile = dynamicReportService.createActiviesReport(new JRMapCollectionDataSource(acaList), params, reportName)
        renderReportOutputType(reportFile,params)
    }

    private renderReportOutputType(File reportFile,def params) {
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader ("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportFile.name), "UTF-8")}" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = URLEncoder.encode(FileNameCleaner.cleanFileName(reportFile.name), "UTF-8")
    }
}
