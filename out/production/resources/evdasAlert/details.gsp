<%@ page contentType="text/html;charset=UTF-8" import="grails.converters.JSON; com.rxlogix.enums.ReportFormat; com.rxlogix.config.PVSState; grails.plugin.springsecurity.SpringSecurityUtils; grails.util.Holders;com.rxlogix.Constants" %>
<head>
    <meta name="layout" content="main"/>
    <g:if test="${!isArchived}">
        <title><g:message code="app.title.evdas.details"/></title>
    </g:if>
    <g:else>
        <title><g:message code="app.label.archived.alerts"/></title>
    </g:else>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="vendorUi/popover/popover.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:javascript src="backbone/underscore.js"/>
    <asset:javascript src="backbone/backbone.js"/>
    <asset:javascript src="app/pvs/advancedFilter/advancedFilterQueryBuilder.js"/>
    <asset:javascript src="purify/purify.min.js" />

    <asset:stylesheet src="query.css"/>
    <asset:stylesheet src="app/pvs/businessConfiguration.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="app/pvs/pvs_list.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="vendorUi/popover/popover.min.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="advancedFilter.css"/>

    <g:javascript>
        var userLocale = 'en';
        var apptype = "EVDAS Alert";
        var listDateRange = "${listDr}";
        var dateRangeRecent = "${dateRange}";
        var startDate = "${startDate}";
        var viewId = "${viewId}";
        var isArchived = "${isArchived}";
        var isViewUpdateAllowed = "${isViewUpdateAllowed}";
        var callingScreen = "${callingScreen}";
        var executedConfigId = "${executedConfigId}";
        var configId = "${configId}";
        var prevColumns = "${prevColumns}";
        var prevColCount = "${prevColCount}";
        var alertType = "${alertType}";
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}";
        var changePriorityUrl = "${createLink(controller: "evdasAlert", action: 'changePriorityOfAlert')}";
        var listConfigUrl = "${createLink(controller: "evdasAlert", action: 'listByExecutedConfig', params: [id: executedConfigId, callingScreen: callingScreen, cumulative: cumulative, adhocRun: false, isArchived: isArchived])}";
        var evdasHistoryUrl = "${createLink(controller: "evdasHistory", action: 'listEvdasHistory')}";
        var updateJustificationUrl = "${createLink(controller: "evdasHistory", action: 'updateJustification')}";
        var statComparisonUrl = "${createLink(controller: 'statisticalComparison', action: 'showComparison',
            params: [configId: executedConfigId, appName: "EVDAS Alert", callingScreen: callingScreen, isArchived: isArchived,
                     hasReviewerAccess: hasEvdasReviewerAccess])}";
        var fetchStratifiedScoresUrl = "${createLink(controller: "evdasAlert", action: "fetchStratifiedScores")}";
        var showTrendUrl = "${createLink(controller: "evdasAlert", action: "showTrendAnalysis")}";
        var showReportUrl = "${createLink(controller: "report", action: "index")}";
        var dateRangeListUrl = "${createLink(controller: "evdasAlert", action: "fetchDateRangeList")}";
        var saveViewUrl = "${createLink(controller: 'viewInstance', action: 'saveView')}";
        var updateViewUrl = "${createLink(controller: 'viewInstance', action: 'updateView')}";
        var deleteViewUrl = "${createLink(controller: 'viewInstance', action: 'deleteView')}";
        var evdasCaseDetailUrl = "${createLink(controller: "caseInfo", action: 'evdasCaseDetail')}";
        var fetchDrillDownDataUrl = "${createLink(controller: "evdasAlert", action: "fetchCaseDrillDownData")}";
        var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
        var assignToGroupUrl = "${createLink(controller: 'evdasAlert', action: 'changeAssignedToGroup')}";
        var discardTempChangesUrl = "${createLink(controller: 'viewInstance', action: 'discardTempChanges', params: ['viewInstance.id': viewId])}";
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var changeDispositionUrl = "${createLink(controller: 'evdasAlert', action: 'changeDisposition', params: [callingScreen: callingScreen])}";
        var revertDispositionUrl = "${createLink(controller: 'evdasAlert', action: 'revertDisposition', params: [callingScreen: callingScreen])}"
        var forceJustification = ${forceJustification};
        var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
        var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
        var signalDetailUrl = "${createLink(controller: 'validatedSignal', action: 'details')}";
        var archivedAlertUrl = "${createLink(controller: "evdasAlert", action: 'archivedAlert', params: [id: executedConfigId])}";
        var evdasDetailsUrl = "${createLink(controller: 'evdasAlert', action: 'details')}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var possibleValuesUrl = "${createLink(controller: 'evdasAlert', action: 'fetchPossibleValues', params: [executedConfigId: executedConfigId])}";
        var allFieldsUrl = "${createLink(controller: 'evdasAlert', action: 'fetchAllFieldValues')}";
        var selectAutoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxAdvancedFilterSearch',params:[executedConfigId:executedConfigId])}";
        var fetchAdvFilterUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterNameAjax')}";
        var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
        var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
        var fetchUsersUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxUserSearch')}";
        var changeAlertLevelDispositionUrl = "${createLink(controller: 'evdasAlert',action: 'changeAlertLevelDisposition')}";
        var attachCaseListingUrl = "${createLink(controller: 'evdasAlert',action: 'attachCaseListingFile')}";
        var alertActivitiesUrl = "${createLink(controller: 'activity',action: 'listByExeConfig')}";
        var reviewCompletedDispostionList = JSON.parse('${reviewCompletedDispostionList}');
        var eventDetailsUrl = "${createLink(controller: "eventInfo", action: "eventDetail")}";
        var availableSignalNameList = JSON.parse('${availableSignals.collect{it.name} as JSON}');
        var dispositionData = JSON.parse('${dispositionData}')
        var isPriorityEnabled = ${isPriorityEnabled};
        var signalAccessUrl
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
        var isCategoryRole = ${SpringSecurityUtils.ifAnyGranted("ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT")};
        var hasReviewerAccess = ${hasEvdasReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
        var buttonClass = "${buttonClass}";
        var isFLagEnabled = "false";
        var emergingIssuesAbbrIme = {"abbr": "${Holders.config.importantEvents.ime.abbreviation}", "label": "${Holders.config.importantEvents.ime.label}"};
        var emergingIssuesAbbrDme = {"abbr": "${Holders.config.importantEvents.dme.abbreviation}", "label": "${Holders.config.importantEvents.dme.label}"};
        var emergingIssuesAbbrSM = {"abbr": "${Holders.config.importantEvents.specialMonitoring.abbreviation}", "label": "${Holders.config.importantEvents.specialMonitoring.label}"};
        var emergingIssuesAbbrEI = {"abbr": "${Holders.config.importantEvents.stopList.abbreviation}", "label": "${Holders.config.importantEvents.stopList.label}"};
        var hasReportingAccess = ${hasReportingAccess};
        var isTempViewSelected = ${isTempViewSelected};
        var currUserName = "${currUserName}"
        <g:if test="${Holders.config.validatedSignal.shareWith.enabled}">
            signalAccessUrl = "${createLink(controller: 'validatedSignal', action: 'isSignalAccessible')}"
        </g:if>
    </g:javascript>
    <asset:javascript src="app/pvs/evdasHistory/evdasHistoryTable.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryJustification.js"/>
    <asset:javascript src="app/pvs/alerts_review/evdas_alert_details.js"/>
    <g:render template="/includes/widgets/actions/action_types"/>
</head>

<body>

<div class="container-fluid whitestrip">
    <div class="row">

        <div class="col-md-12 p2">
            <g:if test="${callingScreen == Constants.Commons.REVIEW}">
                <g:if test="${isArchived}">
                    <span class="p18 ml-10"><a href="review"><g:message code="app.evdas.review"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i>
                    </a><g:message code="app.label.archived.alerts.label"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i> ${name}: ${dr}</span>
                </g:if>
                <g:else>
                    <span class="p18 ml-10"><a href="review"><g:message code="app.evdas.review"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i>
                    </a> ${name}: ${dr}</span>
                </g:else>

                <span><a href="${createLink(controller: 'evdasAlert', action: 'viewExecutedConfig', id: executedConfigId) ?: '#'}"
                        target="_blank" class="glyphicon glyphicon-info-sign theme-color"></a></span>
            </g:if>
            <g:else>
                <span class="panel-title"><g:message code="app.label.evdas.alert"/></span>
            </g:else>
        </div>

    </div>
</div>

<div class="pv-tab">
    <!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" class="active">
            <a href="#details" aria-controls="details" role="tab" data-toggle="tab" accesskey="1"><g:message
                    code="app.label.alert.details"/></a>
        </li>
        <li role="presentation">
            <a href="#activities" id ="activity_tab" aria-controls="activities" role="tab" data-toggle="tab" accesskey="2"><g:message
                    code="app.label.alert.activities"/></a>
        </li>
        <g:if test="${isLatest}">
            <li role="presentation">
                <a href="#archivedAlerts" aria-controls="archivedAlerts" role="tab" data-toggle="tab" accesskey="3"><g:message
                        code="app.label.archived.alerts"/></a>
            </li>
        </g:if>
    </ul>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="details">
            <g:render template="includes/details_tab"
                      model="[id: executedConfigId, alertDispositionList: alertDispositionList, name: name, callingScreen: callingScreen,
                              appName: appName, dateRange: dr, listDateRange: listDr, freqNames: freqNames, viewId: viewId, isArchived: isArchived, evdasHelpMap:evdasHelpMap]"/>
        </div>

        <div id="activities" class="tab-pane fade" role="tabpanel">
            <g:render template="/includes/widgets/activities_tab"
                      model="[alertId: executedConfigId, name: name, type: 'EVDAS Alert', callingScreen: callingScreen]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="archivedAlerts">
            <g:render template="/singleCaseAlert/includes/archivedAlerts"
                      model="[id: executedConfigId, name: name, type: 'EVDAS Alert']"/>
        </div>
    </div>
</div>
<input type="hidden" id="businessRules" value="${eudraRules}"/>
<input type="hidden" id="cumulative" value="${cumulative}"/>
<input type="hidden" id="filterMap" value="${filterMap}"/>
<input type="hidden" id="columnIndex" value="${columnIndex}"/>
<input type="hidden" id="sortedColumn" value="${sortedColumn}"/>
<input type="hidden" id="advancedFilterView" value="${advancedFilterView}"/>
<g:hiddenField id='isArchived' name='isArchived' value="${isArchived}"/>
<g:hiddenField id='hasSignalManagementAccess' name="hasSignalManagementAccess" value="${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_SIGNAL_MANAGEMENT_CONFIGURATION, ROLE_SIGNAL_MANAGEMENT_REVIEWER, ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL")}"/>

<g:render template="/includes/widgets/show_evdas_charts_modal"/>
<g:render template="/includes/modals/alert_comment_modal"/>
<g:render template="/includes/modals/alert_revert_justification_modal"/>
<g:render template="/includes/modals/evdas_attachment_modal"/>
<g:render template="/includes/modals/evdas_history_modal"/>
<g:render template="/includes/modals/stratified_scores_modal"/>
<g:render template="/includes/modals/actionCreateModal"
          model="[id: executedConfigId, appType: 'EVDAS Alert', userList: userList, actionConfigList: actionConfigList, isArchived: isArchived]"/>
<g:render template="/includes/modals/action_list_modal"/>
<g:render template="/includes/modals/show_attachment_modal"/>
<g:hiddenField id='listDateRange' name='listDateRange' value="${listDr}"/>
<g:render template="/includes/modals/save_view_modal" model="[viewInstance: viewInstance, isShareFilterViewAllowed: isShareFilterViewAllowed,
                                                              isViewUpdateAllowed: isViewUpdateAllowed]"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'evdasFields']"/>
<g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
<g:render template="/includes/popover/dispositionJustificationSelect"/>
<g:render template="/includes/popover/dispositionSignalSelect" model="[availableSignals: availableSignals, forceJustification: forceJustification]"/>
<g:render template="/includes/popover/priorityJustificationSelect" model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
<g:render template="/includes/popover/prioritySelect" model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>
<g:render template="/advancedFilters/create_advanced_filters_modal" model="[fieldInfo: fieldList, isShareFilterViewAllowed: isShareFilterViewAllowed]"/>
<g:render template="/advancedFilters/includes/copyPasteFilterModal"/>

</body>