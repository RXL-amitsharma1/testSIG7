package com.rxlogix.action

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.enums.ActionStatus
import com.rxlogix.enums.GroupType
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import sun.misc.Signal

import javax.sql.DataSource
import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static com.rxlogix.util.DateUtil.DEFAULT_DATE_FORMAT

@Slf4j
class ActionService implements LinkHelper {

    def userService
    def utilService
    def activityService
    def grailsApplication
    def alertAttributesService
    def sessionFactory
    def notificationHelper
    def messageSource
    def singleCaseAlertService
    def aggregateCaseAlertService
    def evdasAlertService
    SignalDataSourceService signalDataSourceService
    AlertService alertService
    EmailService emailService
    UserGroupService userGroupService
    def cacheService

    @Autowired
    DataSource dataSource

    def validatedSignalService

    def read(id) {
        def act = Action.findById(id)
        if (act) act.read = true
        act.save()
    }

    List<Map> listFirstXRowsByAssignedTo(User assignedTo, Boolean isAll, Integer count = 0) {
        List<Long> userGroupIdList = utilService.fetchUserListIdForGroup( assignedTo, GroupType.USER_GROUP ) + utilService.fetchUserListIdForGroup( assignedTo, GroupType.WORKFLOW_GROUP )
        List<Action> actionList = Action.createCriteria().list() {
            'or' {
                'eq'('assignedTo.id', assignedTo.id)
                if (userGroupIdList) {
                    'or' {
                        userGroupIdList.collate(1000).each {
                            'in'('assignedToGroup.id', it)
                        }
                    }
                }
            }
            if (!isAll) {
                not {
                    'in'("actionStatus", [ActionStatus.Closed.name(), ActionStatus.Deleted.name()])
                }
            }
            order("id", 'desc')
            if (count > 0) {
                maxResults(count)
                firstResult(0)
            }
        } as List<Action>
        List<Map> actionMap = []
        if (actionList) {
            actionMap = actionServiceDTO(actionList)
        }
        actionMap
    }

    List<Map> actionServiceDTO(List<Action> actionList) {
        String timezone = userService.getCurrentUserPreference()?.timeZone ?: Holders.config.server.timezone
        List<ActionType> actionTypeList = ActionType.list()
        List<ActionConfiguration> actionConfigurationList = ActionConfiguration.list().sort({it.value.toUpperCase()})
        List<Long> actionIdList = actionList.collect { it.id }
        List<Map> singleCaseAlertNameMap = alertService.getAlertNameMapForAction(SingleCaseAlert, actionIdList)
        List<Map> aggCaseAlertNameMap = alertService.getAlertNameMapForAction(AggregateCaseAlert, actionIdList)
        List<Map> litAlertNameMap = alertService.getAlertNameMapForAction(LiteratureAlert, actionIdList)
        List<Map> evdasAlertNameMap = alertService.getAlertNameMapForAction(EvdasAlert, actionIdList)
        List<Map> adhocAlertNameMap = alertService.getAlertNameMapForAction(AdHocAlert, actionIdList)
        List<Map> validatedSignalNameMap = alertService.getAlertNameMapForAction(ValidatedSignal, actionIdList)

        List<Map> actionMap = []
        ExecutorService executorService = Executors.newFixedThreadPool(50)
        List<Future> futureList = actionList.collect { Action action ->
            executorService.submit({ ->
                String alertName = ""
                if (action.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    alertName = singleCaseAlertNameMap.find { it.actionId == action.id }?.name
                } else if (action.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    alertName = aggCaseAlertNameMap.find { it.actionId == action.id }?.name
                } else if (action.alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                    alertName = litAlertNameMap.find { it.actionId == action.id }?.name
                } else if (action.alertType == Constants.AlertConfigType.EVDAS_ALERT) {
                    alertName = evdasAlertNameMap.find { it.actionId == action.id }?.name
                } else if (action.alertType == Constants.AlertConfigType.AD_HOC_ALERT) {
                    alertName = adhocAlertNameMap.find { it.actionId == action.id }?.name
                } else if (action.alertType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
                    alertName = validatedSignalNameMap.find { it.actionId == action.id }?.name
                }
                SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT)
                [id           : action.id,
                 config       : actionConfigurationList.find { it.id == action.configId }?.displayName,
                 type         : actionTypeList.find { it.id == action.typeId }?.displayName,
                 details      : action.details,
                 alertName    : alertName?:Constants.Commons.DASH_STRING,
                 dueDate      : action.dueDate ? sdf.format(action.dueDate): "",
                 actionStatus : action.actionStatus,
                 alertType    : action.alertType?:Constants.Commons.DASH_STRING,
                 comments     : action.comments,
                 completedDate: (action.completedDate) ? sdf.format(action.completedDate) : Constants.Commons.DASH_STRING                ]
            } as Callable)
        }
        futureList.each {
            actionMap.add(it.get())
        }
        executorService.shutdown()
        actionMap
    }

    Map getActionList(DataTableSearchRequest params, User assignedTo, String filterType) {
        User user = userService.getUser()
        Set<Group> groups = Group.findAllUserGroupByUser(user)
        List<Map> actionDTOList = []
        Integer filteredCount  = 0
        /**
         * Commented code due to creating new connection not taking from pool
         final Sql sqlObj = new Sql(signalDataSourceService.getReportConnection("dataSource"))
         */
        Sql sqlObj = null
        List<GroovyRowResult> actionList = []
        Integer countTemp
        try {
            sqlObj = new Sql(dataSource)
            actionList = sqlObj.rows(prepareActionListHQL(params, assignedTo, filterType), params.searchParam.start, params.pageSize())
            countTemp = sqlObj.rows(prepareActionListHQL(params, assignedTo, filterType)).size()
            String completedDate = ''
            String dueDate = ''
            actionList.each { GroovyRowResult action ->
                String actionStatus
                Action act = Action.get(action.get("ID"))
                def association = getAssociation(act)
                actionStatus = action.get("ACTIONSTATUS") == 'InProcess' ? 'InProgress' : action.get("ACTIONSTATUS")
                completedDate = DateUtil.getDateStringFromTimestampWithoutTimezone(action.get("COMPLETEDDATE"))
                dueDate = DateUtil.getDateStringFromTimestampWithoutTimezone(action.get("DUEDATE"))
                def hasAccess = false
                def isArchived = null
                if (action.get("ALERTTYPE") && action.get("ALERTID")) {
                    switch (action.get("ALERTTYPE")) {
                        case 'Single Case Alert':
                            def exConfig = SingleCaseAlert.get(action.get("ALERTID") as Long) ? SingleCaseAlert.get(action.get("ALERTID") as Long)?.executedAlertConfiguration : ArchivedSingleCaseAlert.get(action.get("ALERTID") as Long)?.executedAlertConfiguration
                            Configuration config = Configuration.get(exConfig?.configId)
                            if (association instanceof SingleCaseAlert || association instanceof ArchivedSingleCaseAlert)
                                isArchived = (association instanceof ArchivedSingleCaseAlert)
                            Set<User> users = config?.getShareWithUsers()
                            Set<Group> groupList = config?.getShareWithGroups()
                            hasAccess = exConfig?.autoShareWithUser?.contains(user) || exConfig?.autoShareWithGroup?.find { group -> groups?.contains(group) } || exConfig?.owner == user || users?.contains(user) || groupList?.find {
                                groups?.contains(it)
                            }
                            break
                        case 'Aggregate Case Alert':
                            def exConfig = AggregateCaseAlert.get(action?.get("ALERTID") as Long) ? AggregateCaseAlert.get(action?.get("ALERTID") as Long)?.executedAlertConfiguration : ArchivedAggregateCaseAlert.get(action?.get("ALERTID") as Long)?.executedAlertConfiguration
                            Configuration config = Configuration.get(exConfig?.configId)
                            if (association instanceof AggregateCaseAlert || association instanceof ArchivedAggregateCaseAlert)
                                isArchived = (association instanceof ArchivedAggregateCaseAlert)
                            Set<User> users = config?.getShareWithUsers()
                            Set<Group> groupList = config?.getShareWithGroups()
                            hasAccess = exConfig?.autoShareWithUser?.contains(user) || exConfig?.autoShareWithGroup?.find { group -> groups?.contains(group) } || exConfig?.owner == user || users?.contains(user) || groupList?.find {
                                groups?.contains(it)
                            }
                            break
                        case 'EVDAS Alert':
                            def exConfig = EvdasAlert.get(action?.get("ALERTID") as Long) ? EvdasAlert.get(action?.get("ALERTID") as Long)?.alertConfiguration : ArchivedEvdasAlert.get(action?.get("ALERTID") as Long)?.alertConfiguration
                            EvdasConfiguration config = EvdasConfiguration.findByName(exConfig?.name)
                            if (association instanceof EvdasAlert || association instanceof ArchivedEvdasAlert)
                                isArchived = (association instanceof ArchivedEvdasAlert)
                            Set<User> users = config?.getShareWithUsers()
                            Set<Group> groupList = config?.getShareWithGroups()
                            hasAccess = exConfig?.shareWithUser?.contains(user) || exConfig?.shareWithGroup?.find { group -> groups?.contains(group) } || exConfig?.owner == user || users?.contains(user) || groupList?.find {
                                groups?.contains(it)
                            }
                            break
                        case 'Ad-Hoc Alert':
                            def alert = AdHocAlert.get(action.get("ALERTID") as Long)
                            hasAccess = alert.shareWithUser?.contains(user) || alert.shareWithGroup?.find { group -> groups?.contains(group) } || alert.owner == user
                            break
                        case 'Literature Search Alert':
                            def exConfig = LiteratureAlert.get(action.get("ALERTID") as Long) ? LiteratureAlert.get(action.get("ALERTID") as Long)?.litSearchConfig : ArchivedLiteratureAlert.get(action.get("ALERTID") as Long)?.litSearchConfig
                            LiteratureConfiguration config = LiteratureConfiguration.findByName(exConfig?.name)
                            Set<User> users = config?.getShareWithUsers()
                            Set<Group> groupList = config?.getShareWithGroups()
                            hasAccess = exConfig?.shareWithUser?.contains(user) || exConfig?.shareWithGroup?.find { group -> groups?.contains(group) } || exConfig?.owner == user || users?.contains(user) || groupList?.find {
                                groups?.contains(it)
                            }
                            break
                        case 'Signal Management':
                            ValidatedSignal signal = ValidatedSignal.get(action?.get("ALERTID") as Long)
                            hasAccess = (Holders.config.validatedSignal.shareWith.enabled) ? (signal.shareWithUser?.contains(user) || signal.shareWithGroup?.find { group -> groups?.contains(group) }) : true
                            break
                    }
                }
                actionDTOList.add([id            : action.get("ID"),
                                   config        : action.get("CONFIG"),
                                   type          : action.get("TYPE"),
                                   details       : action.get("DETAILS"),
                                   alertName     : action.get("ALERTNAME") ?: Constants.Commons.DASH_STRING,
                                   dueDate       : dueDate,
                                   actionStatus  : actionStatus ? ActionStatus.valueOf(actionStatus).id : null,
                                   alertType     : action.get("ALERTTYPE") ?: Constants.Commons.DASH_STRING,
                                   comments      : action.get("COMMENTS"),
                                   completedDate : completedDate,
                                   entity        : action.get("ENTITY") ?: Constants.Commons.DASH_STRING,
                                   alertId       : action.get("ALERTID"),
                                   hasAccess     : hasAccess,
                                   followUpNumber: action.get("FOLLOWUPNUMBER"),
                                   caseVersion   : action.get("CASEVERSION")
                ] + (isArchived != null ? [isArchived: isArchived] : [:]))
            }
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            if (Objects.nonNull(sqlObj)) {
                sqlObj.close()
            }
        }
        Integer totalCount = Action.createCriteria().count() {
            actionListClosure.delegate = delegate
            actionListClosure(assignedTo, params, filterType, true)
        } as Integer

       if(params.searchParam.search.getValue() == "") {
             filteredCount = totalCount
        } else {
            filteredCount = countTemp
        }
        [aaData: actionDTOList, recordsTotal: totalCount, recordsFiltered: filteredCount]

    }

    String prepareActionListHQL(DataTableSearchRequest params, User assignedTo, String filterType) {
        StringBuilder searchAlertQuery = new StringBuilder()
        actionSearchQuery(searchAlertQuery, assignedTo, filterType, params)
        String orderByProperty = params?.orderBy()
        String orderDirection = params?.searchParam?.orderDir()
        if (orderByProperty?.equals('actionType')) {
            searchAlertQuery.append(" order by atype.DISPLAY_NAME ").append(orderDirection)
        } else if (orderByProperty?.equals('actionConfig')) {
            searchAlertQuery.append(" order by aconfg.DISPLAY_NAME ").append(orderDirection)
        } else if (orderByProperty?.equals('alertName')) {
            searchAlertQuery.append(" order by UPPER(alertname) ").append(orderDirection)
        } else if (orderByProperty?.equals('details')) {
            searchAlertQuery.append(" order by UPPER(details) ").append(orderDirection)
        } else if (orderByProperty?.equals('alertType')) {
            searchAlertQuery.append(""" order by CASE when alertType = \'${Constants.AlertConfigType.SINGLE_CASE_ALERT}\' then \'Individual Case\' 
                                when alertType = \'${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}\' then \'Aggregate\'
                                when alertType = \'${Constants.AlertConfigType.LITERATURE_SEARCH_ALERT}\' then \'Literature\'
                                when alertType = \'${Constants.AlertConfigType.EVDAS_ALERT}\' then \'Evdas\'
                                when alertType = \'${Constants.AlertConfigType.AD_HOC_ALERT}\' then \'Ad-Hoc\' 
                                else alertType
                                end """).append(orderDirection)
        } else {
            searchAlertQuery.append(" order by ").append(orderByProperty).append(" ").append(orderDirection)
        }

        searchAlertQuery.toString()
    }

    private void actionSearchQuery(StringBuilder searchAlertQuery, User assignedTo, String filterType, DataTableSearchRequest params) {
        String selectQuery = '''SELECT acth.id as id,
                                  aconfg.DISPLAY_NAME as config,
                                  atype.DISPLAY_NAME as type,
                                  acth.details as details,
                                  COALESCE(sca.name, asca.name, agg.name, aagg.name, lalert.name, alalert.name, ealert.name, aealert.name, adhocAlert.name, vsignal.name) as alertname,
                                  COALESCE(sca.case_number, asca.case_number, agg.pt, aagg.pt, lalert.article_title, alalert.article_title, ealert.pt, aealert.pt, adhocAlert.name, vsignal.name) as entity,
                                  COALESCE(sca.id, asca.id, agg.id, aagg.id, lalert.article_id, alalert.article_id, ealert.id, aealert.id, adhocAlert.id, vsignal.id) as ALERTID,
                                  COALESCE(sca.follow_up_number, asca.follow_up_number) as followUpNumber,
                                  COALESCE(sca.case_Version, asca.case_Version) as CASEVERSION,
                                  acth.DUE_DATE as dueDate,
                                  acth.ACTION_STATUS as actionStatus,
                                  acth.ALERT_TYPE as alertType,
                                  acth.COMMENTS as comments,
                                  CASE WHEN acth.ACTION_STATUS = 'Closed' THEN acth.COMPLETED_DATE ELSE acth.COMPLETED_DATE END as completedDate
                           FROM ACTIONS acth
                           LEFT JOIN ACTION_CONFIGURATIONS aconfg ON acth.config_id = aconfg.id
                           LEFT JOIN ACTION_TYPES atype ON acth.type_id = atype.id
                           LEFT JOIN SINGLE_ALERT_ACTIONS sacth ON acth.id = sacth.action_id  AND NOT EXISTS (
                            SELECT 2 FROM ARCHIVED_SCA_ACTIONS asacth WHERE asacth.action_id = acth.id
                           ) and sacth.IS_RETAINED = false
                           LEFT JOIN ARCHIVED_SCA_ACTIONS asacth ON acth.id = asacth.action_id and asacth.IS_RETAINED = false
                           LEFT JOIN AGG_ALERT_ACTIONS aacth ON acth.id = aacth.action_id
                           LEFT JOIN ARCHIVED_ACA_ACTIONS aaacth ON acth.id = aaacth.action_id and aaacth.IS_RETAINED = false
                           LEFT JOIN LIT_ALERT_ACTIONS lacth ON acth.id = lacth.action_id
                           LEFT JOIN ARCHIVED_LIT_ALERT_ACTIONS alacth ON acth.id = alacth.action_id and alacth.IS_RETAINED = false
                           LEFT JOIN EVDAS_ALERT_ACTIONS eacth ON acth.id = eacth.action_id
                           LEFT JOIN ARCHIVED_EVDAS_ALERT_ACTIONS aeacth ON acth.id = aeacth.action_id and aeacth.IS_RETAINED = false
                           LEFT JOIN ADHOC_ALERT_ACTIONS ahacth ON acth.id = ahacth.action_id
                           LEFT JOIN VALIDATED_SIGNAL_ACTIONS vsth ON acth.id = vsth.action_id
                           LEFT JOIN SINGLE_CASE_ALERT sca ON sacth.SINGLE_CASE_ALERT_ID = sca.id
                           LEFT JOIN ARCHIVED_SINGLE_CASE_ALERT asca ON asacth.ARCHIVED_SCA_ID = asca.id
                           LEFT JOIN AGG_ALERT agg ON aacth.AGG_ALERT_ID = agg.id
                           LEFT JOIN ARCHIVED_AGG_ALERT aagg ON aaacth.ARCHIVED_ACA_ID = aagg.id
                           LEFT JOIN LITERATURE_ALERT lalert ON lacth.LITERATURE_ALERT_ID = lalert.id
                           LEFT JOIN ARCHIVED_LITERATURE_ALERT alalert ON alacth.ARCHIVED_LIT_ALERT_ID = alalert.id
                           LEFT JOIN ALERTS adhocAlert ON ahacth.ALERT_ID = adhocAlert.id
                           LEFT JOIN VALIDATED_SIGNAL vsignal ON vsth.VALIDATED_SIGNAL_ACTIONS_ID = vsignal.id
                           LEFT JOIN EVDAS_ALERT ealert ON eacth.EVDAS_ALERT_ID = ealert.id
                           LEFT JOIN ARCHIVED_EVDAS_ALERT aealert ON aeacth.ARCHIVED_EVDAS_ALERT_ID = aealert.id'''
        searchAlertQuery.append(selectQuery)

        if (filterType != Constants.ActionItemFilterType.ALL) {
            searchAlertQuery.append(generateUserOrGroupSql(assignedTo, filterType))
        }

        if (filterType == Constants.ActionItemFilterType.MY_OPEN) {
            searchAlertQuery.append(" AND acth.ACTION_STATUS NOT IN ('Closed', 'Deleted') ")
        }

        String searchValue = params?.searchParam?.search?.getValue()
        if (searchValue == Constants.AlertType.QUALITATIVE) {
            searchValue = Constants.AlertConfigType.SINGLE_CASE_ALERT
        }

        if (searchValue == Constants.AlertType.QUANTITATIVE) {
            searchValue = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        }

        if (searchValue && StringUtils.isNotBlank(searchValue)) {
            (filterType != Constants.ActionItemFilterType.ALL) ? searchAlertQuery.append(" AND ") : searchAlertQuery.append(" WHERE ")

            if (searchValue.isNumber()) {
                searchAlertQuery.append("acth.id LIKE '%").append(searchValue).append("%' OR ")
            }

            String esc_char = ""
            if (searchValue.contains('_')) {
                searchValue = searchValue.replace("_", "!_")
                esc_char = "ESCAPE '!'"
            } else if (searchValue.contains('%')) {
                searchValue = searchValue.replace("%", "!%%")
                esc_char = "ESCAPE '!'"
            }

            searchAlertQuery.append("( UPPER(acth.details) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(acth.ALERT_TYPE) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(acth.ACTION_STATUS) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(acth.COMMENTS) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(aconfg.DISPLAY_NAME) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(atype.DISPLAY_NAME) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")

            // Adding search for alert names and PECs
            searchAlertQuery.append("UPPER(sca.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(asca.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(agg.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(aagg.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(lalert.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(alalert.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(ealert.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(aealert.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(adhocAlert.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(vsignal.name) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(sca.case_number) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(agg.pt) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(lalert.article_title) LIKE UPPER('%").append(searchValue).append("%') ${esc_char} OR ")
            searchAlertQuery.append("UPPER(ealert.pt) LIKE UPPER('%").append(searchValue).append("%') ${esc_char}) ")
        }
    }

    String generateUserOrGroupSql(User assignedTo, String filterType) {
        List<Long> groupIdList = utilService.fetchUserListIdForGroup(assignedTo, GroupType.USER_GROUP) + utilService.fetchUserListIdForGroup(assignedTo, GroupType.WORKFLOW_GROUP)
        String userOrGroupSql = " WHERE (acth.ASSIGNED_TO_ID = ${assignedTo.id} "
        if (groupIdList) {
            userOrGroupSql += "OR acth.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) "
        }
        if (filterType == Constants.ActionItemFilterType.MY_ALL) {
            userOrGroupSql += "OR acth.OWNER_ID = ${userService.currentUserId} "
        }
        userOrGroupSql += ") "
        userOrGroupSql
    }

    Closure actionListClosure = { User assignedTo, DataTableSearchRequest params, String filterType, Boolean isTotalCount ->

        List<Long> userGroupIdList = utilService.fetchUserListIdForGroup( assignedTo, GroupType.USER_GROUP ) + utilService.fetchUserListIdForGroup( assignedTo, GroupType.WORKFLOW_GROUP )
        if (filterType != Constants.ActionItemFilterType.ALL) {
            'or' {
                'eq'('assignedTo.id', assignedTo.id)
                if (userGroupIdList) {
                    'or' {
                        userGroupIdList.collate(1000).each {
                            'in'('assignedToGroup.id', it)
                        }
                    }
                }
                if (filterType == Constants.ActionItemFilterType.MY_ALL) {
                    eq('owner.id', userService.currentUserId)
                }
            }
        }
        if (filterType == Constants.ActionItemFilterType.MY_OPEN) {
            not {
                'in'("actionStatus", [ActionStatus.Closed.name(), ActionStatus.Deleted.name()])
            }
        }

        if (!isTotalCount && StringUtils.isNotBlank(params.searchParam.search.getValue())) {
            or {
                if (params.searchParam.search.getValue().isNumber()) {
                    eq("id", params.searchParam.search.getValue() as Long)
                }
                'ilike'("details", "%${params.searchParam.search.getValue()}%")
                'ilike'("alertType", "%${params.searchParam.search.getValue()}%")
                'ilike'("actionStatus", "%${params.searchParam.search.getValue()}%")
                'ilike'("comments", "%${params.searchParam.search.getValue()}%")
                'type' {
                    'ilike'("displayName", "%${params.searchParam.search.getValue()}%")
                }
                'config' {
                    'ilike'("displayName", "%${params.searchParam.search.getValue()}%")
                }
            }
        }
    }

    @Transactional
    def populate(Action actionInstance, owner, alertId, assignedTo, appType,boolean isArchived = false, boolean isEmbase = false) {

        def alert
       // EvdasAlert evdasAlert
        def domain = null
        if (appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            alert = isArchived ? ArchivedSingleCaseAlert.findById(alertId) : SingleCaseAlert.findById(alertId)
            actionInstance.alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
            actionInstance.execConfigId = alert.executedAlertConfiguration?.id
        } else if (appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            alert = isArchived ? ArchivedAggregateCaseAlert.findById(alertId) : AggregateCaseAlert.findById(alertId)
            actionInstance.alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
            actionInstance.execConfigId = alert.executedAlertConfiguration?.id
        } else if (appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
            alert = ValidatedSignal.findById(alertId)
            actionInstance.alertType = Constants.AlertConfigType.SIGNAL_MANAGEMENT
            actionInstance.execConfigId=alertId as Long
        } else if (appType == Constants.AlertConfigType.EVDAS_ALERT) {
            alert = isArchived ? ArchivedEvdasAlert.findById(alertId) : EvdasAlert.findById(alertId)
            actionInstance.alertType = Constants.AlertConfigType.EVDAS_ALERT
            actionInstance.execConfigId = alert.executedAlertConfiguration?.id
        } else if (appType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            if (isEmbase) {
                domain = isArchived ? ArchivedEmbaseLiteratureAlert : EmbaseLiteratureAlert
            } else {
                domain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
            }
            alert = domain.findById(alertId)
            actionInstance.alertType = Constants.AlertConfigType.LITERATURE_SEARCH_ALERT
            actionInstance.execConfigId = alert.exLitSearchConfig?.id
        } else if (appType == Constants.AlertConfigType.AD_HOC_ALERT) {
            alert = Alert.findById(alertId)
            actionInstance.alertType = Constants.AlertConfigType.AD_HOC_ALERT
            actionInstance.execConfigId = alert?.id
        } else if (appType != Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
            actionInstance.execConfigId = alert?.executedAlertConfiguration?.id
        }



        actionInstance.createdDate = new Date()
        actionInstance.owner = owner
        actionInstance = userService.assignGroupOrAssignTo(assignedTo, actionInstance)
        if (!actionInstance.assignedTo && !actionInstance.assignedToGroup)
            actionInstance.guestAttendeeEmail = assignedTo
        actionInstance.dueDate = actionInstance.dueDate ? (DateUtil.endOfDay(actionInstance.getDueDate())) : null
        actionInstance.completedDate = actionInstance.completedDate ? (DateUtil.endOfDay(actionInstance.getCompletedDate())) : null
        actionInstance.save(failOnError: true, flush: true)

        if (alert) {
            alert.addToActions(actionInstance)
            alert.save()
        }
        actionInstance
    }

    @Transactional
    def updateAction(Action action,
                     String activityDescription,
                     String alertId,
                     User user, String appType) {
        try {
            action.save(failOnError:true)
            if (activityDescription) {
                if (appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
                    if(!alertId){
                        ValidatedSignal.findAll().any{if(it.actions.toSet().contains(action)){
                            alertId = it.id as Long
                            return
                        }
                        }
                    }
                    activityService.createUpdateActivityForSignal(alertId, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                            user, activityDescription, null, action?.assignedToGroup, action?.assignedTo)
                }
                else if(appType == Constants.AlertConfigType.AD_HOC_ALERT)
                {
                    activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                            user, activityDescription, null)
                }
                else {
                    activityService.create(alertId, ActivityType.findByValue(ActivityTypeValue.ActionChange),
                            user, activityDescription, appType)
                }
            }
        } catch (Throwable th) {
            log.error(th.getMessage())
        }
        action
    }

    boolean assignToUpdated(Map assignedToMap, Action actionToUpdate) {
        !(assignedToMap.user.equals(actionToUpdate.assignedTo) && assignedToMap.group.equals(actionToUpdate.assignedToGroup))
    }

    def listActionsForAlert(alertId, appType,boolean isArchived = false, boolean isEmbase = false) {
        def user = userService.getUser()
        def timezone = user?.preference?.timeZone
        def actionList
        if (appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            actionList = isArchived ? ArchivedSingleCaseAlert.findById(alertId)?.actions : SingleCaseAlert.findById(alertId)?.actions
        } else if (appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            actionList = isArchived ? ArchivedAggregateCaseAlert.findById(alertId)?.actions : AggregateCaseAlert.findById(alertId)?.actions
        } else if (appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) {
            actionList = ValidatedSignal.findById(alertId)?.actions
        } else if (appType == Constants.AlertConfigType.TOPIC) {
            actionList = Topic.findById(alertId)?.actions
        } else if (appType == Constants.AlertConfigType.EVDAS_ALERT) {
            actionList =isArchived ? ArchivedEvdasAlert.findById(alertId)?.actions : EvdasAlert.findById(alertId)?.actions
        } else if (appType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            def domain
            if (isEmbase) {
                domain = isArchived ? ArchivedEmbaseLiteratureAlert : EmbaseLiteratureAlert
            } else {
                domain = isArchived ? ArchivedLiteratureAlert : LiteratureAlert
            }

            actionList = domain.findById(alertId)?.actions
        } else {
            actionList = Alert.get(alertId)?.actions
        }
        if (actionList) {
            actionList.collect {
                toDto(it, timezone)
            }
        } else {
            []
        }
    }

    List listActionsForAggAlerts(Long alertId, Boolean isArchived) {
        Set<Action> actionList = isArchived ? ArchivedAggregateCaseAlert.findById(alertId)?.actions : AggregateCaseAlert.findById(alertId)?.actions
        List resultList = actionList?.collect { Action action ->
            String tempDueDate = action.dueDate ? action.dueDate.format(DEFAULT_DATE_FORMAT) : ""
            [
                    actionId     : action.id,
                    config       : action.config?.displayName,
                    type         : action.config?.displayName != "Meeting" ? action.type?.displayName : Meeting.where {
                        actions {
                            id == action.id
                        }
                    }.list()*.meetingTitle,
                    details      : action.details,
                    assignedTo   : userService.getAssignedToName(action) ?: action.guestAttendeeEmail,
                    dueDate      : tempDueDate,
                    actionStatus : ActionStatus[action.actionStatus].id,
                    completedDate: action.completedDate ? action.completedDate.format(DEFAULT_DATE_FORMAT) : Constants.Commons.BLANK_STRING
            ]

        }
        return resultList
    }

    Map actionPropertiesJSON(List<Map> actionTypeList) {
        List actionConfigList = ActionConfiguration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("value", "value")
                property("id", "id")
            }
            ne('value', 'Meeting')
        } as List
        Map actionPropertiesJSONMap = [types    : actionTypeList,
                                       configs  : actionConfigList.sort({it.value.toUpperCase()}),
                                       allStatus: ActionStatus.allValues().collect {
                                           [name: it.toString(), value: it.id]
                                       }]
        actionPropertiesJSONMap
    }

    def toDto(Action action, timezone) {
        def today = DateTime.now()
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT)
        def tempDueDate = action.dueDate ? sdf.format(action.dueDate) : ""
        [
                id           : action.id,
                config       : action.config?.displayName,
                type         : action.config?.displayName != "Meeting" ? action.type?.displayName : Meeting.where {
                    actions {
                        id == action.id
                    }
                }.list()*.meetingTitle,
                details      : action.details,
                assignedTo   : userService.getAssignedToName(action) ?: action.guestAttendeeEmail,
                owner        : action.owner?.getFullName(),
                comments     : action.comments,
                dueDate      : tempDueDate,
                passDue      : today.isAfter(action.dueDate?.getTime()),
                actionStatus : action.actionStatus == 'InProcess' ? ActionStatus["InProgress"].id : ActionStatus[action.actionStatus].id,
                completedDate: action.completedDate ? sdf.format(action.completedDate) : Constants.Commons.BLANK_STRING
        ]
    }

    List<Map> batchPersistAction(Map actionMap) {
        List<Map> alertIdActionIdList = []
        Integer batchSize = Holders.config.signal.batch.size as Integer
        Action.withTransaction {
            Integer count = 0
            actionMap.each { def alert, List<Action> actionList ->
                actionList.each { action ->
                    count += 1
                    action.save(validate: false)
                    alertIdActionIdList.add([col1: action.id.toString(), col2: alert.toString()])
                    if (count == batchSize) {
                        Session session = sessionFactory.currentSession
                        session.flush()
                        session.clear()
                        count = 0
                    }
                }
            }
            Session session = sessionFactory.currentSession
            session.flush()
            session.clear()
            log.info("Action data is batch persisted.")
        }
        alertIdActionIdList
    }


    List<Map> getActionListMap(Long alertId) {
        List<Map> actionListMap = listActionsForAlert(alertId, Constants.AlertConfigType.SINGLE_CASE_ALERT).collect {
            [
                    id            : it.id as String,
                    type          : it.type,
                    action        : it.config,
                    details       : it.details,
                    dueDate       : it.dueDate,
                    assignedTo    : it.assignedTo,
                    status        : it.actionStatus,
                    completionDate: it.completedDate

            ]
        }
        if (actionListMap) {
            actionListMap
        } else {
            [[
                     id            : null,
                     type          : "",
                     action        : "",
                     details       : "",
                     dueDate       : "",
                     assignedTo    : "",
                     status        : "",
                     completionDate: ""

             ]]
        }
    }

    Map createActionDTO(Action act) {
        User user = userService.getUser()
        Set<Group> groups = Group.findAllUserGroupByUser(user)
        Set<Long> groupIds = groups*.id as Set //Intersection is not working,PVS-71212
        String userTimezone = userService?.getCurrentUserPreference()?.timeZone
        def map = act.toDTO(userTimezone)
        if (act.assignedTo) {
            List blindedUsersList = cacheService.getUserBlindingCache()
            map.assignedToObj.put('blinded', blindedUsersList?.contains(act.assignedTo.id)?true:false)
        } else if (act.assignedToGroup){
            List blindedGroupsList = cacheService.getUserGroupBlindingCache()
            map.assignedToObj.put('blinded', blindedGroupsList?.contains(act.assignedToGroup.id)?true:false)
        }
        def association = getAssociation(act)
        if (association) {
            if (association instanceof SingleCaseAlert || association instanceof ArchivedSingleCaseAlert) {
                Boolean isArchived = (association instanceof ArchivedSingleCaseAlert)
                def singleCaseAlert = association
                map.configName = singleCaseAlert.name
                map.configUrl = Constants.defaultUrls.NO_URL
                map.caseNumber = singleCaseAlert.caseNumber
                map.caseNumberUrl = Constants.defaultUrls.NO_URL
                def exConfig = ExecutedConfiguration.findById(singleCaseAlert.executedAlertConfiguration.id)
                def aggregateCaseAlert = AggregateCaseAlert.get(exConfig?.aggAlertId)
                Configuration config = Configuration.get(exConfig?.configId)
                Set<User> users = config?.getShareWithUsers()
                Set<Group> groupList = config?.getShareWithGroups()
                map.hasAccess = exConfig.autoShareWithUser?.contains(user) || exConfig?.autoShareWithGroup?.find { group -> groups?.contains(group) } || exConfig.owner == user || users?.contains(user) || groupList.find { group -> group?.id && groupIds.contains(group.id) }
                if(user.isSingleReviewer()) {
                    map.configUrl = createHref("singleCaseAlert", "details", [configId: singleCaseAlert.executedAlertConfiguration.id, callingScreen: "review", isArchived: isArchived,
                                                                              isCaseSeries: singleCaseAlert?.isCaseSeries, productName: aggregateCaseAlert?.productName, eventName: aggregateCaseAlert?.pt ])
                    map.caseNumberUrl = createHref("caseInfo", "caseDetail", [caseNumber         : singleCaseAlert.caseNumber,
                                                                              version            : singleCaseAlert.caseVersion,
                                                                              followUpNumber     : singleCaseAlert.followUpNumber,
                                                                              isArchived         : isArchived,
                                                                              alertId            : singleCaseAlert.id, isFaers: false,
                                                                              isSingleAlertScreen: true, isCaseSeries: false])
                }
            } else if (association instanceof AggregateCaseAlert || association instanceof ArchivedAggregateCaseAlert) {
                Boolean isArchived = (association instanceof ArchivedAggregateCaseAlert)
                def aggAlert = association
                map.configName = aggAlert.name
                map.configUrl = Constants.defaultUrls.NO_URL
                map.productName = aggAlert.productName
                map.soc = aggAlert.soc
                map.pt = aggAlert.executedAlertConfiguration.eventGroupSelection ? aggAlert.executedAlertConfiguration.getGroupNameFieldFromJson(aggAlert.executedAlertConfiguration.eventGroupSelection) : aggAlert.pt
                map.ptUrl = Constants.defaultUrls.NO_URL
                def exConfig = ExecutedConfiguration.findById(aggAlert.executedAlertConfiguration.id)
                Configuration config = Configuration.get(exConfig?.configId)
                Set<User> users = config?.getShareWithUsers()
                Set<Group> groupList = config?.getShareWithGroups()
                map.hasAccess = exConfig.autoShareWithUser?.contains(user) || exConfig?.autoShareWithGroup?.find { group -> groups?.contains(group) } || exConfig.owner == user || users?.contains(user) || groupList.find { group -> group?.id && groupIds.contains(group.id) }
                if(user.isAggregateReviewer()) {
                    map.configUrl = createHref("aggregateCaseAlert", "details", [configId: aggAlert.executedAlertConfiguration.id, callingScreen: "review", archived: isArchived])
                    map.ptUrl = createHref("eventInfo", "eventDetail", [alertId: aggAlert.id, type: act.alertType, isArchived: isArchived, isAlertScreen: true])
                }
            } else if (association instanceof EvdasAlert || association instanceof ArchivedEvdasAlert) {
                Boolean isArchived = (association instanceof ArchivedEvdasAlert)
                def evdasAlert = association
                map.configName = evdasAlert.name
                map.configUrl = Constants.defaultUrls.NO_URL
                map.productName = evdasAlert.productName
                map.soc = evdasAlert.soc
                map.pt = evdasAlert.pt
                map.ptUrl = Constants.defaultUrls.NO_URL
                def exConfig = EvdasConfiguration.findById(evdasAlert.alertConfiguration.id)
                EvdasConfiguration config = EvdasConfiguration.findByName(exConfig?.name)
                Set<User> users = config?.getShareWithUsers()
                Set<Group> groupList = config?.getShareWithGroups()
                map.hasAccess = exConfig.shareWithUser?.contains(user) || exConfig?.shareWithGroup?.find { group -> groups?.contains(group) } || exConfig.owner == user || users?.contains(user) || groupList.find { group -> group?.id && groupIds.contains(group.id) }
                if(user.isEvdasReviewer()) {
                    map.configUrl = createHref("evdasAlert", "details", [configId: evdasAlert.executedAlertConfiguration.id, callingScreen: "review", archived: isArchived])
                    map.ptUrl = createHref("eventInfo", "eventDetail", [alertId: evdasAlert.id, type: act.alertType, isArchived: isArchived, isAlertScreen: true])
                }
            } else if (association instanceof AdHocAlert) {
                AdHocAlert alert = (AdHocAlert) association
                map.configName = alert.name
                map.configUrl = Constants.defaultUrls.NO_URL
                map.hasAccess = alert.shareWithUser?.contains(user) || alert.shareWithGroup?.find{group -> groups?.contains(group)} || alert.owner==user
                if(user.isAdhocEvaluator()) {
                    map.configUrl = createHref("adHocAlert", "alertDetail", [id: alert.id])
                }
            } else if (association instanceof LiteratureAlert || association instanceof ArchivedLiteratureAlert) {
                Boolean isArchived = (association instanceof ArchivedLiteratureAlert)
                def literatureAlert = association
                map.configName = literatureAlert.name
                map.articleTitle = literatureAlert.articleTitle
                map.articleUrl = grailsApplication.config.app.literature.article.url + literatureAlert.articleId
                map.configUrl = Constants.defaultUrls.NO_URL
                def exConfig = LiteratureConfiguration.findById(literatureAlert.litSearchConfig.id)
                LiteratureConfiguration config = LiteratureConfiguration.findByName(exConfig?.name)
                Set<User> users = config?.getShareWithUsers()
                Set<Group> groupList = config?.getShareWithGroups()
                map.hasAccess = exConfig.shareWithUser?.contains(user) || exConfig?.shareWithGroup?.find { group -> groups?.contains(group) } || exConfig.owner == user || users?.contains(user) || groupList.find { group -> group?.id && groupIds.contains(group.id) }
                //Intersection is not working,PVS-71212
                if(user.isLiteratureReviewer()) {
                    map.configUrl = createHref("literatureAlert", "details", [configId: literatureAlert.exLitSearchConfig.id, callingScreen: "review", archived: isArchived])
                }
            } else if (association instanceof ValidatedSignal) {
                ValidatedSignal signal = (ValidatedSignal) association
                map.configName = signal.name
                map.configUrl = Constants.defaultUrls.NO_URL
                map.productName =  alertService.productSelectionSignal(signal)
                map.eventName = alertService.eventSelectionSignal(signal)
                map.hasAccess = (Holders.config.validatedSignal.shareWith.enabled) ? (signal.shareWithUser?.contains(user) || signal.shareWithGroup?.find { group -> groups?.contains(group) }) : true
                if(user.isSignalManagement()){
                    map.configUrl = createHref("validatedSignal", "details", [id: signal.id])
                }

            }
        }
        map
    }

    Map createAggregateActionDTO(Action act) {
        User user = userService.getUser()
        Set<Group> groups = Group.findAllUserGroupByUser(user)
        String userTimezone = userService?.getCurrentUserPreference()?.timeZone
        Map map = [
                "actionId"       : act.id,
                "type"           : ["id": act.type.id, "value": act.type.displayName],
                "config"         : ["id": act.config.id, "value": act.config.displayName],
                "assignedToValue": act.assignedTo ? ["id": act.assignedToValue(), "text": act.assignedTo.name] : act.assignedToGroup ? ["id": act.assignedToValue(), "text": act.assignedToGroup.name] : [id: act.guestAttendeeEmail, "text": act.guestAttendeeEmail],
                "dueDate"        : act.dueDate ? act.dueDate.format('dd-MMM-yyyy') : "",
                "completionDate" : act.completedDate ? act.completedDate.format('dd-MMM-yyyy') : "",
                "actionStatus"   : ActionStatus[act.actionStatus]?.id,
                "details"        : act.details,
                "comments"       : act.comments,
                "createdDate"    : act.createdDate ? DateUtil.toDateStringWithTimeInAmFormat(act.createdDate, userTimezone) : null,
        ]
        if (act.assignedTo) {
            List blindedUsersList = cacheService.getUserBlindingCache()
            map.assignedToValue.put('blinded', blindedUsersList?.contains(act.assignedTo.id) ? true : false)
        } else if (act.assignedToGroup) {
            List blindedGroupsList = cacheService.getUserGroupBlindingCache()
            map.assignedToValue.put('blinded', blindedGroupsList?.contains(act.assignedToGroup.id) ? true : false)
        }
        def association = getAssociation(act)
        if (association) {
            if (association instanceof AggregateCaseAlert || association instanceof ArchivedAggregateCaseAlert) {
                Boolean isArchived = (association instanceof ArchivedAggregateCaseAlert)
                def aggAlert = association
                map.configName = aggAlert.name
                map.productName = aggAlert.productName
                map.soc = aggAlert.soc
                map.pt = aggAlert.executedAlertConfiguration.eventGroupSelection ? aggAlert.executedAlertConfiguration.getGroupNameFieldFromJson(aggAlert.executedAlertConfiguration.eventGroupSelection) : aggAlert.pt
                String aggDataUrl = Holders.config.signal.angular.app.base.url + Holders.config.signal.angular.app.alert.details
                map.configUrl = createCustomHref(aggDataUrl, [configId: aggAlert.executedAlertConfiguration.id, callingScreen: "review", archived: isArchived])
            }
        }
        map
    }

    def getAssociation(Action act) {
        for (d in [ArchivedSingleCaseAlert, SingleCaseAlert, ArchivedAggregateCaseAlert, AggregateCaseAlert, ArchivedEvdasAlert,
                   EvdasAlert, ArchivedLiteratureAlert, LiteratureAlert, AdHocAlert, ValidatedSignal]) {
            List list = d.createCriteria().list {
                createAlias('actions', 'actions', JoinType.INNER_JOIN)
                eq('actions.id', act.id)
                order('id', 'asc')
            }
            if (list && list.size() > 0) return list[0]
        }
        null
    }

    Integer getActionDashboardCount( User user ) {
        List<Long> userGroupIdList = utilService.fetchUserListIdForGroup( user, GroupType.USER_GROUP ) + utilService.fetchUserListIdForGroup( user, GroupType.WORKFLOW_GROUP )
        Action.createCriteria().count {
            'or' {
                'eq'( 'assignedTo.id', user.id )
                if( userGroupIdList ) {
                    'or' {
                        userGroupIdList.collate( 700 ).each {
                            'in'( 'assignedToGroup.id', it )
                        }
                    }
                }
            }
            not {
                'in'( "actionStatus", [ ActionStatus.Closed.id, ActionStatus.Deleted.id ] )
            }
        }
    }

    Map getUserViewAccess(){
        User user = userService.getUser()
        Map rolesMap = [(Constants.AlertConfigType.SINGLE_CASE_ALERT): user.isSingleReviewer(),
                        (Constants.AlertConfigType.SIGNAL_MANAGEMENT): user.isSignalManagement(),
                        (Constants.AlertConfigType.LITERATURE_SEARCH_ALERT): user.isLiteratureReviewer(),
                        (Constants.AlertConfigType.EVDAS_ALERT): user.isEvdasReviewer(),
                        (Constants.AlertConfigType.AGGREGATE_CASE_ALERT): user.isAggregateReviewer(),
                        (Constants.AlertConfigType.AD_HOC_ALERT): user.isAdhocEvaluator(),
        ]
        return rolesMap
    }

    void addNotification(String appType, User user, String assignedTo, String dueDate, String completionDate, String actionDetail, String comments, String actionType, String action, String actionStatus, String productName, Group group = null) {
        try{
            String type = Constants.Action.ACTION_CREATION
            String message = Constants.Action.EMPTY
            if (appType == Constants.AlertConfigType.SINGLE_CASE_ALERT || appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT || appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT || appType == Constants.AlertConfigType.EVDAS_ALERT || appType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
                message = Constants.Action.ACTION_ITEM_CREATED_MESSAGE
                if (productName) {
                    message += " for ${productName}"
                }
            }
            else{
                message = Constants.Action.ACTION_ITEM_ASSIGNED_MESSAGE
            }
            List<User> notificationRecipients = group ? userGroupService.fetchUserListForGroup(group) : [user]
            log.debug("NotificationRecipients :" + notificationRecipients)

            NotificationLevel status = NotificationLevel.INFO
            //Added changes for PVS-68125
            notificationRecipients.each { User notificationRecipient ->
                String content = generateContent(actionType, action, assignedTo, dueDate, completionDate, actionStatus, actionDetail, comments)
                InboxLog inboxLog = new InboxLog(
                        notificationUserId: notificationRecipient.id,
                        level: status,
                        message: message,
                        messageArgs: "",
                        type: type,
                        subject: message,
                        content: content,
                        createdOn: new Date(),
                        inboxUserId: notificationRecipient.id,
                        isNotification: true)
                inboxLog.save(flush: true, failOnError: true)
                notificationHelper.pushNotification(inboxLog)
            }
        }
        catch(Exception e){
            log.error("Error creating Action: ${e.message}", e)
        }
    }

    private String generateContent(String actionType, String action, String assignedTo, String dueDate, String completionDate, String actionStatus, String actionDetail, String comments) {
        return """
        <strong>Action Type:</strong> ${actionType}<br>
        <strong>Action:</strong> ${action}<br>
        <strong>Assigned To:</strong> ${assignedTo}<br>
        <strong>Due Date:</strong> ${dueDate}<br>
        <strong>Completion Date:</strong> ${completionDate}<br>
        <strong>Comments:</strong> ${actionStatus}<br>
        <strong>Action Details:</strong> ${actionDetail}<br>
        <strong>Comments:</strong> ${comments}<br>
    """
    }

}
