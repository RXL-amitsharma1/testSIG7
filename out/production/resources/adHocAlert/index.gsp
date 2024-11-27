<%@ page contentType="text/html;charset=UTF-8" import="grails.converters.JSON; com.rxlogix.enums.ReportFormat; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.config.PVSState; grails.util.Holders" %>
<head>
    <meta name="layout" content="main"/>
    <title>Adhoc Alert</title>
    <g:javascript>
        var dataUrl = "${createLink(controller: 'singleCaseAlertRest', action: 'list')}";
        var stateChangeUrl = "${createLink(controller: 'singleCaseAlertRest', action: 'changeState')}";
        var dispositionChangeUrl = "${createLink(controller: 'singleCaseAlertRest', action: 'changeDisposition')}";
        var priorityUrl = "${createLink(controller: 'singleCaseAlertRest', action: 'changePriority')}";
        var userLocale = 'en';
        var alertId = "${alertId}";
        var viewId = "${viewId}";
        var hasReviewerAccess = ${hasAdhocReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
        var priorities = "${priorities}";
        var sessionRefreshUrl = "${createLink(controller: 'adHocAlert', action: 'changeFilterAttributes')}";
        var listConfigUrl = "${createLink(controller: 'adHocAlert', action: 'list', params: [callingScreen: params.callingScreen])}";
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}";
        var changePriorityUrl = "${createLink(controller: "adHocAlert", action: 'changePriorityOfAlert')}";
        var topicUrl = "${createLink(controller: "topic", action: 'addAlertToTopic', params: [id: executedConfigId])}";
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}";
        var fetchTopicsUrl = "${createLink(controller: 'topic', action: 'fetchTopicNames')}";
        var saveCommentUrl = "${createLink(controller: 'adHocAlert', action: 'saveComment')}";
        var fetchCommentUrl = "${createLink(controller: 'adHocAlert', action: 'fetchComment')}";
        var callingScreen = "${params.callingScreen}";
        var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
        var assignToGroupUrl = "${createLink(controller: 'adHocAlert', action: 'changeAssignedToGroup')}";
        var changeDispositionUrl = "${createLink(controller: 'adHocAlert', action: 'changeDisposition')}";
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var forceJustification = ${forceJustification};
        var allowedProductsAsSafetyLead = "${allowedProductsAsSafetyLead}";
        var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
        var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
        var signalDetailUrl = "${createLink(controller: 'validatedSignal', action: 'details')}";
        var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
        var availableSignalNameList = JSON.parse('${availableSignals.collect{it.name} as JSON}');
        var dispositionData = JSON.parse('${dispositionData}')
        var isProductSecurity = "${isProductSecurity}";
        var isPriorityEnabled = ${isPriorityEnabled};
        var currUserName = "${currUserName}";
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
        var revertDispositionUrl = "${createLink(controller: 'adHocAlert', action: 'revertDisposition', params: [callingScreen: callingScreen])}"
        var reviewCompletedDispostionList = JSON.parse('${reviewCompletedDispostionList}');
        var signalAccessUrl
        <g:if test="${Holders.config.validatedSignal.shareWith.enabled}">
            signalAccessUrl = "${createLink(controller: 'validatedSignal', action: 'isSignalAccessible')}"
        </g:if>
        var isArchived = "false";
    </g:javascript>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/ad_hoc_alert_list.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <asset:javascript src="purify/purify.min.js" />
    <asset:stylesheet src="adhoc-alert-list.css"/>
    <style>
     .pos-ab.attach{ top:-4px; right:-10px; }

     .pos-ab.comment{ bottom:-4px; right:-10px; }

     .font-13 { font-size: 13px !important; }
     input[type=checkbox] {
         margin: 8px 0 0!important;
     }

    </style>

</head>

<body>
<div class="container-fluid whitestrip">
    <div class="row">
        <div class="col-md-12 p2">
            <span class="p18 ml-10"><a href="javascript:void(0)" data-original-title="" title="">Adhoc Alert <i class="fa fa-angle-right f14" aria-hidden="true"></i></a>
            </span>
        </div>
     </div>
</div>
<div class="pv-tab">
    <!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" class="active">
            <a href="#details" aria-controls="details" role="tab" data-toggle="tab">Alert List</a>
        </li>
    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="details">
            <g:render template="includes/details_tab" bean="${alert}" model="[viewId:viewId,adhocHelpMap:adhocHelpMap]"/>
        </div>
    </div>

</div>
<div id="search-control" class="pull-right dropdown-toggle" style="display: flex; flex-direction: row;margin-right: 10px; visibility: hidden">
    <div id="dropdownUsers" class="col-xl-2 pull-right dropdown-toggle" style="width: 30rem;max-width: 30rem; display: flex; flex-direction: row;">
    <label for="alertsFilter" class="pull-right" style="white-space: nowrap; margin-right: 5px;margin-top: 2px">Select Users</label>
        <g:initializeUsersAndGroupsElement shareWithId="" isWorkflowEnabled="true" alertType="adhoc" callingScreen="${callingScreen}" isFromAdhoc="true" />
    </div>
    <span id="custom-search-label" class="pull-right" style="margin-left: 25px;margin-right: 5px;margin-top: 2px">
        <label for="custom-search">Search</label>
    </span>
    <input id="custom-search" class="pull-right dropdown-toggle form-control" style="width: 200px; margin-left: 3px; height: 22px!important;">
</div>
<g:hiddenField id="filterVals" value="${session.getAttribute("adhoc")}" name="filterVals" />
<g:hiddenField id="filterValsForDashboard" value="${session.getAttribute("adhocDashboard")}" name="filterValsForDashboard" />

<g:render template="/includes/modals/actionCreateModal" model="[alertId: alertId]"/>
<g:render template="/includes/modals/action_list_modal"/>
<g:render template="/includes/modals/addCommentNotesModal"/>
<g:render template="/includes/modals/message_box"/>
<g:render template="/includes/modals/alert_revert_justification_modal"/>
<g:render template="/includes/modals/add_alert_to_topic" modal="[strategyList: strategyList]"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'adhocFields']"/>
<g:render template="/includes/popover/dispositionJustificationSelect"/>
<g:render template="/includes/popover/dispositionSignalSelect" model="[availableSignals: availableSignals, forceJustification: forceJustification]"/>
<g:render template="/includes/popover/priorityJustificationSelect" model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
<g:render template="/includes/popover/prioritySelect" model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>
</body>
</html>