package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.domain.CriteriaAggregator
import com.rxlogix.dto.DueDateCountDTO
import com.rxlogix.helper.LinkHelper
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.LmCountry
import com.rxlogix.mapping.LmFormulation
import com.rxlogix.mapping.LmReportType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.SystemConfig
import com.rxlogix.signal.UndoableDisposition
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.SignalUtil
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.sql.JoinType
import java.text.SimpleDateFormat

import java.security.MessageDigest
import org.apache.commons.lang3.time.DateUtils

import static com.rxlogix.util.MiscUtil.calcDueDate

@Transactional
class AdHocAlertService implements Alertbililty, LinkHelper, EventPublisher {
    def userService
    def messageSource
    def alertService
    def CRUDService
    def activityService
    def validatedSignalService
    def medicalConceptsService
    def emailService
    def adHocAlertService
    EmailNotificationService emailNotificationService
    SessionFactory sessionFactory
    def cacheService
    def undoableDispositionService
    def signalAuditLogService

    private def priorityValueList = ['Low', 'Medium', 'High']

    def list() {
        AdHocAlert.list()
    }

    def listWithFilter(Map filters) {
        def validFilters = filters.findAll {k, v-> (k ==~ /.*Filter/) && (v)}.collectEntries {k, v->
            [k.replace('Filter', ''), v]
        }

        if (validFilters) {
            def customerQueryAggregator = new CriteriaAggregator(AdHocAlert)

            validFilters.each { key, value ->
                if (['priority', 'disposition', 'assignedTo'].contains(key)) {
                    customerQueryAggregator.addCriteria { "$key" {
                        eq ('id', Long.parseLong(value)) }
                    }
                } else if(['productSelection', 'eventSelection'].contains(key)) {
                    customerQueryAggregator.addCriteria {
                        ilike(key, '%' + value + '%')
                    }
                }
            }
            customerQueryAggregator.list()
        } else
            list()
    }

    def listSelectedAlerts(String alerts){
        String[] alertList = alerts.split(",")
        alertList.collect{AdHocAlert.findById(Integer.valueOf(it))}
    }

    def importAlert(String jsonContent) {
        importFileMapping.each{ k, v->
            jsonContent = jsonContent.replaceAll(k, v)
        }

        def jsonSlurper = new JsonSlurper()
        def jsonArrays = jsonSlurper.parseText(jsonContent)
        def alerts = jsonArrays.collect{ it -> buildAlert(it) }

        saveAlerts(alerts)

        return alerts

    }

    def saveAlerts(List<AdHocAlert> alerts) {
        alerts.findAll({!it.errors || !it.errors.hasErrors()}).collect { it.save() }
    }

    def buildAlert(Map<String, String> jsonAlert){
        def formats = Holders.config.grails.databinding.dateFormats
        def timezone = Holders.config.server.timezone

        AdHocAlert aa = new AdHocAlert(jsonAlert)
        String creatorName = Holders.config.pvsignal.alert.import.creator.username
        User creator = User.findByUsername(creatorName)
        aa.priority = Priority.findByValue(jsonAlert.myPriority ?: Priority.findByDefaultPriority(true))
        aa.detectedDate = DateUtil.getDate(jsonAlert.myDetectedDate, formats, timezone)
        aa.sharedWith = User.findByUsername(jsonAlert.sharedUser)
        aa.shareWithGroup = [Group.findByName('All Users')]
        aa.detectedBy = jsonAlert.detector
        aa.createdBy = creator
        aa.modifiedBy = creator
        aa.reviewDate = DateUtil.getDate(jsonAlert.myReviewDate, formats, timezone)
        aa.owner = creator
        aa.disposition = creator?.getWorkflowGroup()?.defaultAdhocDisposition
        aa.workflowGroup = creator?.getWorkflowGroup()
        aa.description = jsonAlert.description
        calcDueDate(aa, aa.priority, aa.disposition, false,
                cacheService.getDispositionConfigsByPriority(aa.priority.id))
        def assignedTo = Holders.config.pvsignal.alert.import.assignedto.default.username
        aa.assignedTo = User.findByUsername(assignedTo) ?: User.findByUsername(jsonAlert.assignedUser)
        aa.productSelection = buildProductSelection(jsonAlert)
        aa.slimId = jsonAlert.id

        aa
    }

    def importFileMapping = [
            'alertName': 'name',
            'productName': 'productName',
            'genericName': 'genericNames',
            'SOC of Interest': 'eventSelection',
            'Formulation': 'formulations',
            'Relevant Indication': 'indication',
            'detectedByUCB': 'detector',
            'Responsible Physician': 'sharedUser',
            'Issue Previously Tracked': 'issuePreviouslyTracked',
            'Number of ICSRs \\(if applicable\\)': 'numberOfICSRs',
            'topicName': 'topic',
            'priority': 'myPriority',
            'safetyObservation': 'myDetectedDate',
            'Is associated with the RMP': 'rmpAssociated',
            'Assigned To':'assignedUser',
            'Review Date':'myReviewDate'
    ]

    /**
     * Method: getDefaultPriority
     * Service method to fetch the default priority of the system.
     */
    def getDefaultPriority() {
        Priority.findByDefaultPriority(true)
    }

    def buildProductSelection(def jsonAlert){
        if(!jsonAlert.productName && !jsonAlert.genericNames)
            return null
        (jsonAlert.productName) ? /{"1":[],"2":[],"3":[{"name":"${jsonAlert.productName}"}],"4":[],"5":[]}/ : /{"1":[],"2":[],"3":[],"4":[],"5":[{"genericName":"${jsonAlert.genericNames}"}]}/
    }

    def byDisposition() {
        User currentUser = userService.getUser()
        List<AdHocAlert> filterList = adHocAlertService.getAssignedAdhocAlertList(currentUser)
        alertCountsByDisposition(filterList)
    }

    def findAlertsByProductName(productNames) {
        if (productNames) {
            def out = AdHocAlert.list().findAll {
                productNames.intersect(it.productNameList)
            }
            out
        } else {
            []
        }
    }

    def findMatchedAlerts(prodNames, topic, eventSelection) {
        def alertsWithTheSameProducts = findAlertsByProductName(prodNames)
        def MessageDigest md = MessageDigest.getInstance("MD5");
        topic = topic == null ? new String(md.digest()) : topic
        eventSelection = eventSelection == null ? new String(md.digest()) : eventSelection
        def alerts = alertsWithTheSameProducts.findAll {
            (topic?.toLowerCase() == it.topic?.toLowerCase()) ||
                    (intersectEvents(getNameFieldFromJson(eventSelection?.toLowerCase()), getNameFieldFromJson(it.eventSelection?.toLowerCase())))
        }
        alerts
    }

    def intersectEvents(eventSelection1, eventSelection2) {
        if (eventSelection1 && eventSelection2) {
            def evt1List = eventSelection1.split(',') as List
            def evt2List = eventSelection2.split(',') as List

            evt1List.intersect(evt2List)
        } else
            []
    }

    def copyAlert(AdHocAlert alert, User owner) {
        def newAlert = new AdHocAlert(
                name: generateUniqueName(alert),
                disposition: alert.disposition,
                reviewDate: alert.reviewDate,
                assignedTo: alert.assignedTo,
                assignedToGroup: alert.assignedToGroup,
                detectedDate: new Date(),
                priority: alert.priority,
                attributes: alert.attributes,
                createdBy: owner.username,
                detectedBy:alert.detectedBy,
                dateCreated: new Date(),
                eventSelection: alert.eventSelection,
                countryOfIncidence: alert.countryOfIncidence,
                reportType: alert.reportType,
                formulations: alert.formulations,
                refType: alert.refType,
                productSelection: alert.productSelection,
                studySelection: alert.studySelection,
                indication: alert.indication,
                sharedWith: alert.sharedWith,
                owner: owner,
                issuePreviouslyTracked: alert.issuePreviouslyTracked,
                numberOfICSRs: alert.numberOfICSRs,
                initialDataSource: alert.initialDataSource,
                topic: alert.topic,
                description: alert.description,
                alertRmpRemsRef: alert.alertRmpRemsRef,
                workflowGroup: alert.workflowGroup,
                dueDate: alert.dueDate,
                initialDisposition: alert.disposition,
                initialDueDate: alert.dueDate,
                notes: alert.notes
        )

        CRUDService.save(newAlert)
        return newAlert

    }

    private String generateUniqueName(AdHocAlert alert) {
        String newName = "Copy of $alert.name"

        if (AdHocAlert.findByNameAndOwner(newName, alert.owner)) {
            int count = 1
            newName = "Copy of $alert.name ($count)"
            while (AdHocAlert.findByNameAndOwner(newName, alert.owner)) {
                newName = "Copy of $alert.name (${count++})"
            }
        }

        return newName
    }

    List<AdHocAlert> getAssignedAdhocAlertList(User currentUser) {
        List<Long> groupIdList = currentUser.groups.collect { it.id }
        AdHocAlert.withCriteria {
            'or'{
                'eq'("assignedTo.id", currentUser.id)
                'in'("assignedToGroup.id",groupIdList)
            }
            'eq'("workflowGroup.id",currentUser.workflowGroup?.id)
            'disposition' {
                'eq'('closed', false)
                'eq'('reviewCompleted', false)
                'ne'('displayName','Safety Topic')
            }
        } as List<AdHocAlert>
    }

    List<AdHocAlert> getAssignedAdhocAlertListByDueDate(User currentUser, String dueDateType) {
        List<Long> groupIdList = currentUser.groups.collect { it.id }
        Date dateWithoutTime = getDateWithoutTime(new Date())
        AdHocAlert.withCriteria {
            'or'{
                'eq'("assignedTo.id", currentUser.id)
                'in'("assignedToGroup.id",groupIdList)
            }
            'eq'("workflowGroup.id",currentUser.workflowGroup?.id)

            if (dueDateType=='PASTCOUNT') {
                'lt'("dueDate",dateWithoutTime)
            } else if(dueDateType=='CURRENTCOUNT') {
                'and'{
                    'gte'("dueDate",dateWithoutTime)
                    'lte'("dueDate",getNextDate(dateWithoutTime))
                }
            } else if(dueDateType=='FUTURECOUNT') {
                'gt'("dueDate",getNextDate(dateWithoutTime))
            }
            'disposition' {
                'eq'('closed', false)
                'eq'('reviewCompleted', false)
                'ne'('displayName','Safety Topic')
            }
        } as List<AdHocAlert>
    }
    private Date getDateWithoutTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getNextDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }
    @Transactional
    Map changeDisposition(String selectedRows, Disposition newDisposition, String justification, String validatedSignalName, String productJson,Long signalId) {
        User loggedInUser = userService.user
        String currentUserFullName = loggedInUser?.fullName
        Boolean attachedSignalData
        ValidatedSignal validatedSignal = ValidatedSignal.findByName(validatedSignalName)
        List<UndoableDisposition> undoableDispositionList =[]
        List selectedRowsList = JSON.parse(selectedRows)
        boolean bulkUpdate = selectedRowsList.size() > 1
        JSON.parse(selectedRows).each { Map<String, Long> selectedRow ->
            AdHocAlert adHocAlert = AdHocAlert.get(selectedRow["alert.id"])
            Disposition previousDisposition = adHocAlert.disposition

            Map dispDataMap = [objectId: adHocAlert.id, objectType: Constants.AlertConfigType.AD_HOC_ALERT, prevDispositionId: previousDisposition.id,
                               currDispositionId: newDisposition.id, prevDispPerformedBy: adHocAlert.dispPerformedBy, prevDueDate: adHocAlert.dueDate, prevActualDueDate:adHocAlert.initialDueDate]
            UndoableDisposition undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)
            undoableDispositionList.add(undoableDisposition)
            adHocAlert.disposition = newDisposition
            adHocAlert.customAuditProperties = ["justification": justification]
            adHocAlert.dispPerformedBy = currentUserFullName
            adHocAlert.isDispChanged = true
            calcDueDate(adHocAlert, adHocAlert.priority, adHocAlert.disposition, true,
                            cacheService.getDispositionConfigsByPriority(adHocAlert.priority.id))
            if(emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_ADHOC)) {
                    emailNotificationService.mailHandlerForDispChangeAdhoc(adHocAlert, previousDisposition, newDisposition)
            }
            if(!adHocAlert.validatedSignals.contains(validatedSignal) && !adHocAlert.validatedSignals.contains(validatedSignal)){
                createActivityForDispositionChange(adHocAlert, previousDisposition, newDisposition, justification, loggedInUser, validatedSignalName, validatedSignal)
            }
            List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions;
            boolean isDateValidated= false;
            if (validatedSignalName) {
                Disposition defaultSignalDisposition = loggedInUser?.getWorkflowGroup()?.defaultSignalDisposition
                isDateValidated= validatedDateDispositions.contains(defaultSignalDisposition.value);
                validatedSignal= validatedSignalService.attachAlertToSignal(validatedSignalName, adHocAlert.productSelection, adHocAlert, Constants.AlertConfigType.AD_HOC_ALERT, defaultSignalDisposition,null,signalId,isDateValidated)
                attachedSignalData = SignalUtil.joinSignalNames(adHocAlert.validatedSignals as List)

                    Integer dueIn
                    boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
                    String defaultValidatedDate=Holders.config.signal.defaultValidatedDate
                    boolean dueInStartEnabled= Holders.config.dueInStart.enabled
                    String dueInStartPoint = Holders.config.dueInStartPoint.field ?: Constants.WorkFlowLog.VALIDATION_DATE
                    if(enableSignalWorkflow){
                        dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(validatedSignal.id ,dueInStartPoint) : validatedSignalService.calculateDueIn(validatedSignal.id ,validatedSignal.workflowState)
                    } else {
                        dueIn = dueInStartEnabled ? validatedSignalService.calculateDueIn(validatedSignal.id ,dueInStartPoint) : validatedSignalService.calculateDueIn(validatedSignal.id ,defaultValidatedDate)
                    }
                    if(dueIn != null  && SystemConfig.first().displayDueIn) {
                        validatedSignalService.saveSignalStatusHistory([signalStatus: "Due Date", statusComment: "Due date has been updated.",
                                                 signalId    : validatedSignal.id, "createdDate": null], true)
                    }
                signalAuditLogService.createAuditLog([
                        entityName: "Signal: Ad-Hoc Review Observations",
                        moduleName: "Signal: Ad-Hoc Review Observations",
                        category: AuditTrail.Category.INSERT.toString(),
                        entityValue: "${validatedSignalName}: AdHoc associated",
                        username: userService.getUser().username,
                        fullname: userService.getUser().fullName
                ] as Map, [[propertyName: "AdHoc associated", oldValue: "", newValue: "${adHocAlert.name}"]] as List)

            }
            adHocAlert.save()
        }
        if(selectedRowsList.size()){
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }
        Map signal=validatedSignalService.fetchSignalDataFromSignal(validatedSignal,productJson,null);
        return [attachedSignalData:attachedSignalData,signal:signal]
    }

    @Transactional
    def revertDisposition(Long id, String justification) {
        log.info("Reverting Dispostion Started for Adhoc Alert")
        AdHocAlert adHocAlert = AdHocAlert.get(id)
        Boolean dispositionReverted = false
        User loggedInUser = userService.user

        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertConfigType.AD_HOC_ALERT)
            order('dateCreated', 'desc')
            maxResults(1)
        }

        if (adHocAlert && undoableDisposition?.isEnabled) {
            try {
                Disposition newDisposition = cacheService.getDispositionByValue(undoableDisposition.prevDispositionId)
                undoableDisposition.isUsed = false
                // saving state before undo for activity: 60067
                def prevUndoDisposition = adHocAlert.disposition
                def prevUndoDispPerformedBy = adHocAlert.dispPerformedBy
                def prevUndoDueDate = adHocAlert.dueDate

                adHocAlert.disposition = newDisposition
                adHocAlert.dueDate = undoableDisposition.prevDueDate
                adHocAlert.dispPerformedBy = undoableDisposition.prevDispPerformedBy
                adHocAlert.dueDate = undoableDisposition.prevDueDate
                adHocAlert.initialDueDate = undoableDisposition.prevActualDueDate
                adHocAlert.undoJustification = justification

                def activityMap = [
                        'Disposition': [
                                'previous': prevUndoDisposition ?: "",
                                'current': adHocAlert.disposition ?: ""
                        ],
                        'Performed By': [
                                'previous': prevUndoDispPerformedBy ?: "",
                                'current': adHocAlert.dispPerformedBy ?: ""
                        ],
                        'Due Date': [
                                'previous': prevUndoDueDate ? new SimpleDateFormat("yyyy-MM-dd").format(prevUndoDueDate) : "",
                                'current': adHocAlert.dueDate ? new SimpleDateFormat("yyyy-MM-dd").format(adHocAlert.dueDate) : ""
                        ]
                ]

                String activityChanges = activityMap.collect { k, v ->
                    def previous = v['previous'] ?: ""
                    def current = v['current'] ?: ""
                    if (previous != current) {
                        "$k changed from \'$previous\' to \'$current\'"
                    } else {
                        null
                    }
                }.findAll().join(', ')
                CRUDService.saveWithoutAuditLog(undoableDisposition)
                CRUDService.save(adHocAlert)

                UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: id, type: Constants.AlertConfigType.AD_HOC_ALERT])

                createActivityForUndoDisposition(adHocAlert, justification, loggedInUser, activityChanges)
                dispositionReverted=true
                log.info("Dispostion reverted successfully for adhoc alert id: " + id)
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("some error occoured while reverting disposition")
            }
        }
        return [attachedSignalData: null, dispositionReverted:dispositionReverted]
    }

    void createActivityForUndoDisposition(AdHocAlert adHocAlert,
                                          String justification, User loggedInUser, String activityChanges) {
        log.info("Creating Activity for reverting disposition for Adhoc Alert: " + adHocAlert.id)
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.UndoAction.value)
        String changeDetails = Constants.ChangeDetailsUndo.UNDO_DISPOSITION_CHANGE + " with " + activityChanges

        Map attrs = [product: getNameFieldFromJson(adHocAlert.productSelection),
                     event  : getNameFieldFromJson(adHocAlert.eventSelection)]

        activityService.create(adHocAlert, activityType, loggedInUser, changeDetails,
                justification, attrs)
    }

    void createActivityForDispositionChange(AdHocAlert adHocAlert, Disposition previousDisposition, Disposition targetDisposition,
                                            String justification, User loggedInUser, String validatedSignalName = null, ValidatedSignal validatedSignal) {
        String changeDetails
        ActivityType activityType
        if (previousDisposition.id == targetDisposition.id && !adHocAlert.validatedSignals.contains(validatedSignal)){
            activityType = ActivityType.findByValue(ActivityTypeValue.SignalAdded)
            changeDetails = "Case attached with Signal '$validatedSignalName'"

        } else if (!previousDisposition.isValidatedConfirmed()){
            activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
            if (validatedSignalName) {
                changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition' and attached with signal '$validatedSignalName'"
            } else {
                changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition'"
            }
        }

        Map attrs = [product: getNameFieldFromJson(adHocAlert.productSelection),
                     event  : getNameFieldFromJson(adHocAlert.eventSelection)]

        activityService.create(adHocAlert, activityType, loggedInUser, changeDetails,
                justification, attrs)
    }

    Map prepareOrderMap(Map params) {
        Map orderMap = [:]
        int columnNumber
        if (params."order[0][column]") {
            def numberString = params."order[0][column]"
            columnNumber = numberString as Integer
            orderMap.put(params."columns[${columnNumber}][data]", params."order[0][dir]")

        }
        return orderMap
    }

    List<User> userList(String search, String esc_char = "") {
        String searchPattern = search ? "%${search.toUpperCase()}%" : "%"
        List<User> listUser = User.createCriteria().list {
            sqlRestriction("UPPER(full_name) LIKE '${searchPattern}' ${esc_char}")
        }
        return listUser
    }

    List groupList(def search) {
        List<Group> listGroup = Group.createCriteria().list {
            ilike('name', "%" + search + '%')
        }
    }

    Map getAllowedAdHocAlerts(domainName, dispositionFilters, String callingScreen, Map params) {
        Integer totalColumns = 0
        List<String> filterWithUsersAndGroups = []
        params.each { k, v ->
            if (k.contains("columns")) {
                totalColumns++
            }
        }
        totalColumns = totalColumns / 6

        Map filterMap = alertService.prepareFilterMap(params, totalColumns)
        Map ordMap = prepareOrderMap(params)
        if (params.filterList  && params.filterList != "{}") {
            def jsonSlurper = new JsonSlurper()
            filterMap = jsonSlurper.parseText(params.filterList)
        }
        List dispositionList = []
        dispositionList = Disposition.list().collect { it.displayName }
        List dispositionListName = getDispositionsForName(dispositionList)
        List list = []
        User currentUser = userService.getUser()
        List<Disposition> openDispositions = getDispositionsForName(dispositionFilters)
        Group workflowGroup = currentUser.workflowGroup
        List<Long> groupIdList = currentUser.groups.collect { it.id }
        Date currentDate_a = new Date()
        Date currentDate = currentDate_a.clearTime()
        if(params.callingScreen == Constants.Commons.DASHBOARD){
            filterWithUsersAndGroups = (params["selectedAlertsFilterForDashboard"] == "null" || params["selectedAlertsFilterForDashboard"] == null) ? [] : params["selectedAlertsFilterForDashboard"]?.substring(1, params["selectedAlertsFilterForDashboard"].length() - 1).replaceAll("\"", "").split(",");
        }else {
            filterWithUsersAndGroups = (params["selectedAlertsFilter"] == "null" || params["selectedAlertsFilter"] == null) ? [] : params["selectedAlertsFilter"]?.substring(1, params["selectedAlertsFilter"].length() - 1).replaceAll("\"", "").split(",");
        }
        if (openDispositions) {
            String searchVal = params."search[value]"
            if (searchVal.contains("'")) {
                searchVal = searchVal?.replaceAll("'", "''")
            }
            boolean isEscapeCharacter = false
            if (searchVal.contains('_')) {
                searchVal = searchVal.replaceAll("\\_", "!_")
                isEscapeCharacter = true
            }
            if (callingScreen == Constants.Commons.DASHBOARD) {
                list = domainName.createCriteria().list(max: params.length, offset: params.start) {
                    'in'("disposition", openDispositions)
                    'or' {
                        eq("assignedTo.id", userService.getCurrentUserId())
                        if (groupIdList.size() > 0) {
                            or {
                                groupIdList.collate(1000).each {
                                    'in'('assignedToGroup.id', it)
                                }
                            }
                        }
                    }
                    'or' {
                        filterWithUsersAndGroups.each { itr ->
                            if(itr.contains("User_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedTo.id', extractedId as Long)
                            }
                            else if(itr.contains("UserGroup_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedToGroup.id', extractedId as Long)
                            }
                            else if(itr.contains("Mine_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('owner.id', extractedId as Long)
                            }
                            else if(itr.contains("AssignToMe_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedTo.id', extractedId as Long)
                                User loggedInUser = User.findById(extractedId as Long)
                                Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                                allGroupsContainsUser.each {
                                    eq('assignedToGroup.id', it.id as Long)
                                }
                            } else if (itr.contains("SharedWithMe_")) {
                                List<Long> configsShared = getUserService().getShareWithConfigurations()
                                if (configsShared) {
                                    'or' {
                                        configsShared.collate(1000).each {
                                            'in'('id', it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    eq("workflowGroup", workflowGroup)
                    if (searchVal != "undefined" && searchVal) {
                        List<User> listUser = userList(searchVal)
                        List<Group> listGroup = groupList(searchVal)
                        'or' {
                            if (!isEscapeCharacter) {
                                ilike('name', '%' + searchVal + '%')

                                'disposition' {
                                    ilike('displayName', "%" + searchVal + '%')
                                }

                                eq('dueDate', currentDate + (searchVal.isInteger() ? searchVal.toInteger() : 0))

                                ilike('initialDataSource', '%' + searchVal + '%')
                            } else {
                                iLikeWithEscape('name', '%' + searchVal + '%')

                                'disposition' {
                                    iLikeWithEscape('displayName', "%" + searchVal + '%')
                                }
                                eq('dueDate', currentDate + (searchVal.isInteger() ? searchVal.toInteger() : 0))

                                iLikeWithEscape('initialDataSource', '%' + searchVal + '%')
                            }

                            if (listUser) {
                                listUser.collate(999).each { itr ->
                                    'in'('assignedTo.id', itr*.id)
                                }
                            }
                            if (listGroup) {
                                listGroup.collate(999).each { itrg ->
                                    'in'('assignedToGroup.id', itrg*.id)
                                }
                            }

                            if ("YES".contains(searchVal?.toUpperCase())) {
                                'eq'('issuePreviouslyTracked', true)
                            } else if ("NO".contains(searchVal?.toUpperCase())) {
                                'eq'('issuePreviouslyTracked', false)
                            }else{
                                'eq'('issuePreviouslyTracked', null)
                            }
                        }
                    }
                    //for column wise
                    else if (filterMap.size() > 0) {
                        filterMap.each { k, v ->
                            if (k == "initDataSrc") {
                                ilike('initialDataSource', '%' + v + '%')
                            } else if (k == "dueIn") {
                                eq('dueDate', currentDate + (filterMap.get("dueIn").isInteger() ? filterMap.get("dueIn").toInteger() : 0))
                            } else if (k == "name") {
                                ilike('name', '%' + v + '%')
                            } else if (k == "disposition") {
                                'disposition' {
                                    iLikeWithEscape('displayName', "%${EscapedILikeExpression.escapeString(v)}%")
                                }
                            } else if (k == "issueTracked") {
                                if ("YES".contains(filterMap.get("issueTracked")?.toUpperCase())) {
                                    'eq'('issuePreviouslyTracked', true)
                                } else if ("NO".contains(filterMap.get("issueTracked")?.toUpperCase())) {
                                    'eq'('issuePreviouslyTracked', false)
                                } else {
                                    'eq'('issuePreviouslyTracked', null)
                                }
                            }else if (k == 'signalsAndTopics') {
                                createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                                createAlias("topics", "t", JoinType.LEFT_OUTER_JOIN)
                                'or' {
                                    ilike('vs.name', '%' + v + '%')
                                    ilike('t.name', '%' + v + '%')
                                }
                            } else if (k == "numOfIcsrs") {
                                eq('numberOfICSRs', filterMap.get("numOfIcsrs").isInteger() ? filterMap.get("numOfIcsrs").toInteger() : 0)
                            } else if (k == "assignedTo") {
                                createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                                createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                                or {
                                    iLikeWithEscape('at.fullName', "%${EscapedILikeExpression.escapeString(filterMap.get("assignedTo"))}%")
                                    iLikeWithEscape('atg.name', "%${EscapedILikeExpression.escapeString(filterMap.get("assignedTo"))}%")
                                }
                            } else if (k == "detectedDate") {
                                sqlRestriction("UPPER(detected_date) LIKE UPPER('%${filterMap.get("detectedDate")}%')")

                            } else if (k == "productSelection") {

                                String search = filterMap.get("productSelection")
                                Map searchCharacter = getEscCharacter(search)
                                if (searchCharacter.esc_char) {
                                    'or' {
                                        sqlRestriction("{alias}.id in (select id from alerts, json_table(PRODUCT_SELECTION,'\$.*[*]'\n" + "columns(names varchar path '\$.name')) t1 where UPPER(t1.names) like UPPER('%${searchCharacter.searchString}%') escape '${searchCharacter.esc_char}')")
                                        sqlRestriction("JSON_VALUE({alias}.PRODUCT_GROUP_SELECTION,'\$[*].name') like('%${searchCharacter.searchString}%') escape '${searchCharacter.esc_char}'")
                                    }
                                } else {
                                    'or' {
                                        sqlRestriction("{alias}.id in (select id from alerts, json_table(PRODUCT_SELECTION,'\$.*[*]'\n" + "columns(names varchar path '\$.name')) t1 where UPPER(t1.names) like UPPER('%${searchCharacter.searchString}%'))")
                                        sqlRestriction("JSON_VALUE({alias}.PRODUCT_GROUP_SELECTION,'\$[*].name') like('%${searchCharacter.searchString}%')")
                                    }
                                }

                            } else if (k == "eventSelection") {

                                String search = filterMap.get("eventSelection")
                                Map searchCharacter = getEscCharacter(search)
                                if (searchCharacter.esc_char) {
                                    'or' {
                                        sqlRestriction("{alias}.id in (select id from alerts, json_table(EVENT_SELECTION,'\$.*[*]'\n" + "columns(names varchar path '\$.name')) t1 where UPPER(t1.names) like UPPER('%${searchCharacter.searchString}%') escape '${searchCharacter.esc_char}')")
                                        sqlRestriction("JSON_VALUE({alias}.EVENT_GROUP_SELECTION,'\$[*].name') like('%${searchCharacter.searchString}%') escape '${searchCharacter.esc_char}'")
                                    }
                                } else {
                                    'or' {
                                        sqlRestriction("{alias}.id in (select id from alerts, json_table(EVENT_SELECTION,'\$.*[*]'\n" + "columns(names varchar path '\$.name')) t1 where UPPER(t1.names) like UPPER('%${searchCharacter.searchString}%'))")
                                        sqlRestriction("JSON_VALUE({alias}.EVENT_GROUP_SELECTION,'\$[*].name') like('%${searchCharacter.searchString}%')")
                                    }
                                }
                            }
                        }
                    }
                    if (ordMap.size() > 0) {
                        ordMap.each { k, v ->
                            if (k == "dueIn") {
                                order("dueDate", "${v}")
                            } else if (k == "detectedDate") {
                                order("detectedDate", "${v}")
                            } else if (k == "disposition") {
                                createAlias("disposition", "disp", JoinType.LEFT_OUTER_JOIN)
                                order("disp.displayName", "${v}")
                            } else if (k == "numOfIcsrs") {
                                order('numberOfICSRs', "${v}")
                            } else if (k == "issueTracked") {
                                order('issuePreviouslyTracked', "${v}")
                            } else if (k == "initDataSrc") {
                                order('initialDataSource', "${v}")
                            }
                        }
                    }

                    'disposition' {
                        'eq'('closed', false)
                        'eq'('reviewCompleted', false)

                    }
                    order("lastUpdated", "desc")
                }
            } else if (callingScreen == Constants.Commons.TRIGGERED_ALERTS) {
                list = domainName.createCriteria().list(max: params.length, offset: params.start) {
                    'in'("disposition", openDispositions)
                    and {
                        eq("workflowGroup", workflowGroup)
                    }
                    or {
                        filterWithUsersAndGroups.each { itr ->
                            if(itr.contains("User_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedTo.id', extractedId as Long)
                            }
                            else if(itr.contains("UserGroup_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedToGroup.id', extractedId as Long)
                            }
                            else if(itr.contains("Mine_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('owner.id', extractedId as Long)
                            }
                            else if(itr.contains("AssignToMe_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedTo.id', extractedId as Long)
                                User loggedInUser = User.findById(extractedId as Long)
                                Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                                allGroupsContainsUser.each {
                                    eq('assignedToGroup.id', it.id as Long)
                                }
                            } else if (itr.contains("SharedWithMe_")) {
                                List<Long> configsShared = getUserService().getShareWithConfigurations()
                                if (configsShared) {
                                    'or' {
                                        configsShared.collate(1000).each {
                                            'in'('id', it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (searchVal != "undefined" && searchVal) {
                        List<User> listUser = userList(searchVal)
                        List<Group> listGroup = groupList(searchVal)
                        'or' {
                            if (!isEscapeCharacter) {
                                ilike('name', '%' + searchVal + '%')

                                'disposition' {
                                    ilike('displayName', "%" + searchVal + '%')
                                }

                                eq('dueDate', currentDate + (searchVal.isInteger() ? searchVal.toInteger() : 0))
                                ilike('initialDataSource', '%' + searchVal + '%')
                            } else {
                                iLikeWithEscape('name', '%' + searchVal + '%')

                                'disposition' {
                                    iLikeWithEscape('displayName', "%" + searchVal + '%')
                                }
                                eq('dueDate', currentDate + (searchVal.isInteger() ? searchVal.toInteger() : 0))

                                iLikeWithEscape('initialDataSource', '%' + searchVal + '%')
                            }
                            if (listUser) {
                                listUser.collate(999).each { itr ->
                                    'in'('assignedTo.id', itr*.id)
                                }
                            }
                            if (listGroup) {
                                listGroup.collate(999).each { itrg ->
                                    'in'('assignedToGroup.id', itrg*.id)
                                }

                            }

                            if ("YES".contains(searchVal?.toUpperCase())) {
                                'eq'('issuePreviouslyTracked', true)
                            } else if ("NO".contains(searchVal?.toUpperCase())) {
                                'eq'('issuePreviouslyTracked', false)
                            }else{
                                'eq'('issuePreviouslyTracked', null)
                            }
                        }
                    }
                    //for column wise
                    else if (filterMap.size() > 0) {
                        filterMap.each { k, v ->
                            if (k == "initDataSrc") {
                                ilike('initialDataSource', '%' + v + '%')
                            } else if (k == "dueIn") {
                                eq('dueDate', currentDate + (filterMap.get("dueIn").isInteger() ? filterMap.get("dueIn").toInteger() : 0))
                            } else if (k == "name") {
                                ilike('name', '%' + v + '%')
                            } else if (k == "disposition") {
                                'disposition' {
                                    iLikeWithEscape('displayName', "%${EscapedILikeExpression.escapeString(v)}%")
                                }
                            }else if (k == "issueTracked") {
                                if ("YES".contains(filterMap.get("issueTracked")?.toUpperCase())) {
                                    'eq'('issuePreviouslyTracked', true)
                                } else if ("NO".contains(filterMap.get("issueTracked")?.toUpperCase())) {
                                    'eq'('issuePreviouslyTracked', false)
                                } else {
                                    'eq'('issuePreviouslyTracked', null)
                                }
                            } else if (k == 'signalsAndTopics') {
                                createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                                createAlias("topics", "t", JoinType.LEFT_OUTER_JOIN)
                                'or' {
                                    ilike('vs.name', '%' + v + '%')
                                    ilike('t.name', '%' + v + '%')
                                }
                            } else if (k == "numOfIcsrs") {
                                eq('numberOfICSRs', filterMap.get("numOfIcsrs").isInteger() ? filterMap.get("numOfIcsrs").toInteger() : 0)
                            } else if (k == "assignedTo") {
                                createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                                createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                                or {
                                    iLikeWithEscape('at.fullName', "%${EscapedILikeExpression.escapeString(filterMap.get("assignedTo"))}%")
                                    iLikeWithEscape('atg.name', "%${EscapedILikeExpression.escapeString(filterMap.get("assignedTo"))}%")
                                }

                            } else if (k == "detectedDate") {
                                sqlRestriction("UPPER(detected_date) LIKE UPPER('%${filterMap.get("detectedDate")}%')")
                            } else if (k == "productSelection") {
                                String search = filterMap.get("productSelection")
                                'or' {
                                    sqlRestriction("id in (select id from alerts, json_table(PRODUCT_SELECTION,'\$.*[*]'\n" + "columns(names varchar path '\$.name')) t1 where UPPER(t1.names) like UPPER('%${search}%'))")
                                    sqlRestriction("JSON_VALUE(PRODUCT_GROUP_SELECTION,'\$[*].name') like('%${search}%')")
                                }

                            } else if (k == "eventSelection") {

                                String search = filterMap.get("eventSelection")
                                'or' {
                                    sqlRestriction("id in (select id from alerts, json_table(EVENT_SELECTION,'\$.*[*]'\n" + "columns(names varchar path '\$.name')) t1 where UPPER(t1.names) like UPPER('%${search}%'))")

                                    sqlRestriction("JSON_VALUE(EVENT_GROUP_SELECTION,'\$[*].name') like('%${search}%')")

                                }
                            }


                        }
                    }
                    if (ordMap.size() > 0) {
                        ordMap.each { k, v ->
                            if (k == "dueIn") {
                                order("dueDate", "${v}")
                            } else if (k == "detectedDate") {
                                order("detectedDate", "${v}")
                            } else if (k == "disposition") {
                                createAlias("disposition", "disp", JoinType.LEFT_OUTER_JOIN)
                                order("disp.displayName", "${v}")
                            } else if (k == "numOfIcsrs") {
                                order('numberOfICSRs', "${v}")
                            } else if (k == "issueTracked") {
                                order('issuePreviouslyTracked', "${v}")
                            } else if (k == "initDataSrc") {
                                order('initialDataSource', "${v}")
                            }
                        }
                    }
                    order("lastUpdated", "desc")
                }
            } else {
                List<String> shareWithConfigs = getUserService().getUserAdhocConfigurations()
                list = domainName.createCriteria().list(max: params.length, offset: params.start) {
                    'in'("disposition", openDispositions)
                    'or' {
                        if (shareWithConfigs) {
                            'in'("disposition", openDispositions)
                            'or' {
                                shareWithConfigs.collate(1000).each {
                                    'in'('id', it)
                                }
                            }
                        }
                        eq('owner.id', userService.getCurrentUserId())
                        eq("assignedTo.id", userService.getCurrentUserId())
                        if (groupIdList.size() > 0) {
                            or {
                                groupIdList.collate(1000).each {
                                    'in'('assignedToGroup.id', it)
                                }
                            }
                        }
                    }
                    or {
                        filterWithUsersAndGroups.each { itr ->
                            if(itr.contains("User_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedTo.id', extractedId as Long)
                            }
                            else if(itr.contains("UserGroup_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedToGroup.id', extractedId as Long)
                            }
                            else if(itr.contains("Mine_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('owner.id', extractedId as Long)
                            }
                            else if(itr.contains("AssignToMe_")) {
                                String extractedId = itr.substring(itr.indexOf('_')+1)
                                eq('assignedTo.id', extractedId as Long)
                                User loggedInUser = User.findById(extractedId as Long)
                                Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                                allGroupsContainsUser.each {
                                    eq('assignedToGroup.id', it.id as Long)
                                }
                            } else if (itr.contains("SharedWithMe_")) {
                                List<Long> configsShared = getUserService().getShareWithConfigurations()
                                if (configsShared) {
                                    'or' {
                                        configsShared.collate(1000).each {
                                            'in'('id', it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    eq("workflowGroup", workflowGroup)

                    if (searchVal != "undefined" && searchVal) {
                        List<User> listUser = userList(searchVal)
                        List<Group> listGroup = groupList(searchVal)
                        if (searchVal.contains('"')) {
                            searchVal = searchVal.replace('"', '\"')
                        }
                        'or' {
                            if (!isEscapeCharacter) {
                                sqlRestriction("UPPER(name) LIKE UPPER('%${searchVal}%')")

                                'disposition' {
                                    ilike('displayName', "%" + searchVal + '%')
                                }
                                eq('dueDate', currentDate + (searchVal.isInteger() ? searchVal.toInteger() : 0))

                                ilike('initialDataSource', '%' + searchVal + '%')
                            } else {
                                String esc_char = "escape '!'"
                                sqlRestriction("UPPER(NAME) LIKE UPPER('%${searchVal}%') ${esc_char}")

                                'disposition' {
                                    iLikeWithEscape('displayName', "%" + searchVal + '%')
                                }
                                eq('dueDate', currentDate + (searchVal.isInteger() ? searchVal.toInteger() : 0))

                                iLikeWithEscape('initialDataSource', '%' + searchVal + '%')

                            }

                            if (listUser) {
                                listUser.collate(999).each { itr ->
                                    'in'('assignedTo.id', itr*.id)
                                }
                            }
                            if (listGroup) {
                                listGroup.collate(999).each { itrg ->
                                    'in'('assignedToGroup.id', itrg*.id)
                                }
                            }

                            if ("YES".contains(searchVal?.toUpperCase())) {
                                'eq'('issuePreviouslyTracked', true)
                            } else if ("NO".contains(searchVal?.toUpperCase())) {
                                'eq'('issuePreviouslyTracked', false)
                            }else{
                                'eq'('issuePreviouslyTracked', null)
                            }
                        }
                    }
                    //for column wise
                    else if (filterMap.size() > 0) {
                        and {
                            filterMap.each { k, v ->
                                if (k == "initDataSrc") {
                                    ilike('initialDataSource', '%' + v + '%')
                                } else if (k == "name") {
                                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(v)}%")
                                } else if (k == "disposition") {
                                    'disposition' {
                                        iLikeWithEscape('displayName', "%${EscapedILikeExpression.escapeString(v)}%")
                                    }
                                } else if (k == "dueIn") {
                                    eq('dueDate', currentDate + (filterMap.get("dueIn").isInteger() ? filterMap.get("dueIn").toInteger() : 0))
                                } else if (k == 'signalsAndTopics') {
                                    createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                                    createAlias("topics", "t", JoinType.LEFT_OUTER_JOIN)
                                    'or' {
                                        iLikeWithEscape('vs.name', "%${EscapedILikeExpression.escapeString(v)}%")
                                        iLikeWithEscape('t.name', "%${EscapedILikeExpression.escapeString(v)}%")
                                    }
                                } else if (k == "issueTracked") {

                                    if ("YES".contains(filterMap.get("issueTracked")?.toUpperCase())) {
                                        'eq'('issuePreviouslyTracked', true)
                                    } else if ("NO".contains(filterMap.get("issueTracked")?.toUpperCase())) {
                                        'eq'('issuePreviouslyTracked', false)
                                    } else {
                                        'eq'('issuePreviouslyTracked', null)
                                    }
                                } else if (k == "numOfIcsrs") {
                                    eq('numberOfICSRs', filterMap.get("numOfIcsrs").isInteger() ? filterMap.get("numOfIcsrs").toInteger() : 0)
                                } else if (k == "assignedTo") {
                                    createAlias("assignedTo", "at", JoinType.LEFT_OUTER_JOIN)
                                    createAlias("assignedToGroup", "atg", JoinType.LEFT_OUTER_JOIN)
                                    or {
                                        iLikeWithEscape('at.fullName', "%${EscapedILikeExpression.escapeString(filterMap.get("assignedTo"))}%")
                                        iLikeWithEscape('atg.name', "%${EscapedILikeExpression.escapeString(filterMap.get("assignedTo"))}%")
                                    }

                                } else if (k == "detectedDate") {
                                    String search = filterMap.get("detectedDate")
                                    Map searchCharacter = getEscCharacter(search)
                                    if (searchCharacter.esc_char) {
                                        sqlRestriction("UPPER(TO_CHAR({alias}.detected_date, 'DD-MON-YYYY')) LIKE UPPER('%${searchCharacter.searchString}%') escape '${searchCharacter.esc_char}'")
                                    } else {
                                        sqlRestriction("UPPER(TO_CHAR({alias}.detected_date, 'DD-MON-YYYY')) LIKE UPPER('%${searchCharacter.searchString}%')")
                                    }
                                } else if (k == "productSelection") {
                                    String search = filterMap.get("productSelection")
                                    Map searchCharacter = getEscCharacter(search)
                                    if (searchCharacter.esc_char) {
                                        'or' {
                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_each(PRODUCT_SELECTION::jsonb) AS kv(key, value) ON true " +
                                                    "JOIN LATERAL jsonb_array_elements(value) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%') ESCAPE '${searchCharacter.esc_char}')")

                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_array_elements(PRODUCT_GROUP_SELECTION::jsonb) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%') ESCAPE '${searchCharacter.esc_char}')")
                                        }
                                    } else {
                                        'or' {
                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_each(PRODUCT_SELECTION::jsonb) AS kv(key, value) ON true " +
                                                    "JOIN LATERAL jsonb_array_elements(value) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%'))")
                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_array_elements(PRODUCT_GROUP_SELECTION::jsonb) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%'))")
                                        }
                                    }
                                } else if (k == "eventSelection") {

                                    String search = filterMap.get("eventSelection")
                                    Map searchCharacter = getEscCharacter(search)
                                    if (searchCharacter.esc_char) {
                                        'or' {
                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_each(EVENT_SELECTION::jsonb) AS kv(key, value) ON true " +
                                                    "JOIN LATERAL jsonb_array_elements(value) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%') ESCAPE '${searchCharacter.esc_char}')")
                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_array_elements(EVENT_GROUP_SELECTION::jsonb) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%') ESCAPE '${searchCharacter.esc_char}')")
                                        }
                                    } else {
                                        'or' {
                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_each(EVENT_SELECTION::jsonb) AS kv(key, value) ON true " +
                                                    "JOIN LATERAL jsonb_array_elements(value) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%'))")
                                            sqlRestriction("{alias}.id IN (" +
                                                    "SELECT id FROM alerts " +
                                                    "JOIN LATERAL jsonb_array_elements(EVENT_GROUP_SELECTION::jsonb) AS elem ON true " +
                                                    "JOIN LATERAL jsonb_extract_path_text(elem, 'name') AS name_value ON true " +
                                                    "WHERE UPPER(name_value) LIKE UPPER('%${searchCharacter.searchString}%'))")
                                        }
                                    }
                                }
                            }
                        }
                    }
                   if (ordMap.size() > 0) {
                        ordMap.each { k, v ->
                            if (k == "dueIn") {
                                order("dueDate", "${v}")
                            } else if (k == "detectedDate") {
                                order("detectedDate", "${v}")
                            } else if (k == "disposition") {
                                createAlias("disposition","disp",JoinType.LEFT_OUTER_JOIN)
                                order("disp.displayName", "${v}")
                            } else if (k == "numOfIcsrs") {
                                order('numberOfICSRs', "${v}")
                            } else if (k == "issueTracked") {
                                order('issuePreviouslyTracked', "${v}")
                            } else if (k == "initDataSrc") {
                                order('initialDataSource', "${v}")
                            }
                        }
                    }
                    order("lastUpdated", "desc")
                }
            }
        }
        int totalRecords
        if (callingScreen == Constants.Commons.DASHBOARD){
          def dashBoardAlertList = domainName.createCriteria().list(max: params.length, offset: params.start) {
              'in'("disposition", openDispositions)
              'or' {
                  eq("assignedTo.id", userService.getCurrentUserId())
                  if (groupIdList.size() > 0) {
                      or {
                          groupIdList.collate(1000).each {
                              'in'('assignedToGroup.id', it)
                          }
                      }
                  }
              }
              eq("workflowGroup", workflowGroup)
              'disposition' {
                  'eq'('closed', false)
                  'eq'('reviewCompleted', false)

              }
          }
            totalRecords = dashBoardAlertList?.totalCount
        }else{
            List<String> shareWithConfigs = getUserService().getUserAdhocConfigurations()
            def adhocTotalRecords = domainName.createCriteria().list(max: params.length, offset: params.start) {
                'in'("disposition", dispositionListName)
                'or' {
                    if (shareWithConfigs) {
                        'in'("disposition", dispositionListName)
                        'or' {
                            shareWithConfigs.collate(1000).each {
                                'in'('id', it)
                            }
                        }
                    }
                    eq('owner.id', userService.getCurrentUserId())
                    eq("assignedTo.id", userService.getCurrentUserId())
                    if (groupIdList.size() > 0) {
                        or {
                            groupIdList.collate(1000).each {
                                'in'('assignedToGroup.id', it)
                            }
                        }
                    }
                }
                eq("workflowGroup", workflowGroup)
            }
            totalRecords = adhocTotalRecords.totalCount
        }
        int total = list.totalCount
        return [data: list, recordsFiltered: total, recordsTotal: totalRecords]
    }


    List<Disposition> getDispositionsForName(dispositionFilters) {
        List dispositionList = []
        if (dispositionFilters) {
            dispositionList = Disposition.findAllByDisplayNameInList(dispositionFilters)
        }
        dispositionList
    }

    List getCountryNames(){
        List countryNames = LmCountry.withTransaction {LmCountry.findAllByNameIsNotNull()}
        countryNames.sort ({it.name.toUpperCase()})
    }

    List getFormulations(){
        List formulations = LmFormulation.withTransaction {LmFormulation.findAllByFormulationIsNotNull()}
        formulations.sort ({it.formulation.toUpperCase()})
    }

    List getLmRetportTypes(){
        List lmReportTypes = LmReportType.withTransaction {LmReportType.findAllByTypeIsNotNull()}
        lmReportTypes.sort({it.type.toUpperCase()})
    }
    void dissociateAdhocAlertFromSignal(def alert, Disposition targetDisposition, String justification, ValidatedSignal signal) {
        Disposition previousDisposition = alert.disposition
        validatedSignalService.changeToInitialDisposition(alert, signal, targetDisposition)
        String changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition' and dissociated from signal '$signal.name'"

        Map attrs = [product: getNameFieldFromJson(alert.productSelection),
                     event  : getNameFieldFromJson(alert.eventSelection)]

        activityService.create(alert, ActivityType.findByValue(ActivityTypeValue.DispositionChange), userService.getUser(), changeDetails,
                justification, attrs)
    }

    Map getEscCharacter(String search) {
        String searchString=''
        String esc_char = ""
        if(search){
            searchString = search.toLowerCase()
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
        }
        return [searchString : searchString, esc_char : esc_char]

    }

    Map saveComment(String selectedAdhocAlertIds, String newComment) {
        Boolean success = true
        AdHocAlert adhocAlert = null
        User currentUser = userService.getUser()
        String activityDetails = ''
        Boolean isUpdated = false

        try {
            JSON.parse(selectedAdhocAlertIds).each { Long alertId ->
                adhocAlert = AdHocAlert.get(alertId)
                String oldComment = adhocAlert.notes ?: ''
                adhocAlert.notes = newComment ? newComment.replaceAll("(?i)'", "''")?.replaceAll('"', "\"") : ''
                adhocAlert.noteModifiedBy = currentUser.fullName
                adhocAlert.lastUpdatedNote = new Date()
                adhocAlert.save(flush: true)
                ActivityType activityType = null
                if (oldComment) {
                    isUpdated = true
                    if (newComment) {
                        activityType = ActivityType.findOrCreateByValue(ActivityTypeValue.CommentUpdated)
                        activityDetails = "Comment '" + oldComment + "' is updated with new comment '" + newComment + "'"
                    } else {
                        activityType = ActivityType.findOrCreateByValue(ActivityTypeValue.CommentRemoved)
                        activityDetails = "Comment '" + oldComment + "' is removed"
                    }
                } else {
                    activityType = ActivityType.findOrCreateByValue(ActivityTypeValue.CommentAdded)
                    activityDetails = "Comment '" + newComment + "' is added"
                }
                activityService.create(adhocAlert.id, activityType,
                        userService.getUser(), activityDetails, null)
            }
        } catch (Exception e) {
            success = false
            e.printStackTrace()
        }
        String timeZone = currentUser?.preference?.timeZone
        [success: success, comment: adhocAlert.notes ? adhocAlert.notes : "", dateUpdated: adhocAlert.lastUpdatedNote ? DateUtil.toDateStringWithTime(adhocAlert.lastUpdatedNote, timeZone) : '', createdBy: adhocAlert?.noteModifiedBy, isUpdated: isUpdated]
    }

    /**
     * Fetch dynamic dropdown values based on key.
     * @param key - The key for which dropdown values are required (e.g., 'formulations').
     * @return List of values.
     */
    List<String> prepareDynamicDropDownValues(String key) {
        // Fetch cached dropdown values.
        Map<String, List<String>> dropdownCache = cacheService.getAdhocAlertDynamicDropDownCache(Constants.AdhocAlert.DROPDOWN_VALUES_BY_KEYS)

        // Check if the key exists in the cache.
        List<String> cachedValues = dropdownCache?.get(key)

        // Return cached values if found, otherwise return an empty list.
        return cachedValues ?: []
    }

    boolean checkFilterDispositionChange(List<String> filters, String filtersString) {
        // Check for null parameters
        if (filters == null || filtersString == null) {
            // Consider filters to be different if either is null
            return true
        }

        // Parse the JSON string into a List
        List<String> filtersFromString = new JsonSlurper().parseText(filtersString) as List<String>

        // Compare the two lists by size and content
        return filters.size() != filtersFromString.size() || !filters.containsAll(filtersFromString)
    }

    boolean checkForEmptyBrackets(String input) {
        // Check if the input string is null or matches the empty bracket patterns
        if (input == null || input.trim().isEmpty() || input == "[]" || input == "[ ]" || input == "{}" || input == "{ }") {
            return true // Return null if conditions are met
        }
        return false // Return the original string if it does not match the conditions
    }

    /**
     * Converts a JSON array string to a List<String>.
     *
     * @param jsonString JSON array string in the format ["a", "b", "c"].
     * @return A List<String> containing the elements from the JSON array.
     */
    List<String> convertJsonToList(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return [] // Return an empty List if the input is null or empty
        }

        try {
            // Use JsonSlurper to parse the JSON array string into a List
            List<String> resultList = new JsonSlurper().parseText(jsonString) as List<String>
            return resultList
        } catch (Exception e) {
            log.error("Error parsing JSON string: ${e.message}")
            return [] // Return an empty List on error
        }
    }

    Set updateClosedStatus(Set<Map<String, Object>> setOfMaps, List<String> filterList) {
        if (Objects.isNull(filterList) || filterList.isEmpty()) {
            return setOfMaps
        }
        setOfMaps.each { map ->
            if (!filterList.contains(map["value"])) {
                map["closed"] = true
            }
        }
    }


}
