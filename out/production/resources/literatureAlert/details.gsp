<%@ page import="grails.converters.JSON;com.rxlogix.enums.ReportFormat; grails.plugin.springsecurity.SpringSecurityUtils; grails.util.Holders" %>
<head>
    <meta name="layout" content="main"/>
    <g:if test="${!isArchived}">
        <title><g:message code="app.new.literature.details"/></title>
    </g:if>
    <g:else>
        <title><g:message code="app.label.archived.alerts"/></title>
    </g:else>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="app/pvs/literatureSearch/literature_details.js"/>
    <asset:javascript src="app/pvs/literatureHistory/literature_History.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:javascript src="purify/purify.min.js" />

    <asset:stylesheet src="app/pvs/pvs_list.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>

    <g:javascript>
            var executedConfigId = "${executedConfigId}";
            var literatureConfigurationId=${literatureConfigurationId};
            var configId = "${configId}";
            var saveCategoryAccess = "${saveCategoryAccess}";
            var callingScreen = "${callingScreen}";
            var isArchived = "${isArchived}";
            var isCaseSeries = "${false}";
            var listConfigUrl =  "${createLink(controller: 'literatureAlert', action: 'literatureSearchAlertList')}";
            var updateAutoRouteDispositionUrl = "${createLink(controller: 'literatureAlert', action: 'updateAutoRouteDisposition')}";
            var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
            var assignToGroupUrl = "${createLink(controller: 'literatureAlert', action: 'changeAssignedToGroup')}";
            var dispositionIncomingOutgoingMap = JSON.parse("${dispositionIncomingOutgoingMap}");
            var dispositionData = JSON.parse('${dispositionData}')
            var availableSignalNameList = JSON.parse('${availableSignals.collect{it.name} as JSON}');
            var forceJustification = ${forceJustification};
            var changeDispositionUrl = "${createLink(controller: 'literatureAlert', action: 'changeDisposition', params: [callingScreen: callingScreen])}";
            var revertDispositionUrl = "${createLink(controller: 'literatureAlert', action: 'revertDisposition', params: [callingScreen: callingScreen])}"
            var changePriorityUrl = "${createLink(controller: "literatureAlert", action: 'changePriorityOfAlert')}";
            var fetchTagsUrl = "${createLink(controller: 'literatureAlert', action: 'alertTagDetails', params: [isArchived: isArchived])}";
            var saveTagUrl = "${createLink(controller: 'literatureAlert', action: 'saveAlertTags', params: [isArchived: isArchived])}";
            var tagDetailsUrl = "${createLink(controller: 'literatureAlert', action: 'details')}";
            var fillCommonTagUrl = "${createLink(controller: 'commonTag', action: 'getLitAlertCategories')}";
            var saveCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'saveAlertCategories')}";
            var fetchCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'commonTagDetails')}";
            var privateEnabled = "${grailsApplication.config.categories.feature.private.enabled ? true : false}"
            var nonConfiguredEnabled = "${grailsApplication.config.categories.feature.nonConfigured.enabled ? true : false}"
            var alertActivities = "${createLink(controller: 'literatureAlert', action: 'listLiteratureAlertActivities',params:['configId':configId])}";
            var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
            var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
            var discardTempChangesUrl = "${createLink(controller: 'viewInstance', action: 'discardTempChanges', params: ['viewInstance.id': viewId])}";
            var signalDetailUrl = "${createLink(controller: 'validatedSignal', action: 'details')}";
            var viewId = "${viewId}";
            var pubMedUrl = "${literatureArticleUrl}";
            var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
            var changeAlertLevelDispositionUrl = "${createLink(controller: 'literatureAlert',action: 'changeAlertLevelDisposition',params:['exConfigId':configId])}";
            var reviewCompletedDispostionList = JSON.parse('${reviewCompletedDispostionList}')
            var litCurrentArticleHistoryURL = "${createLink(controller: 'literatureHistory',action: 'listCaseHistory')}"
            var listArticleHistoryInOtherAlertsURL = "${createLink(controller: 'literatureHistory',action: 'listArticleHistoryInOtherAlerts')}"
            var updateJustificationUrl = "${createLink(controller: 'literatureHistory',action: 'updateJustification',)}"
            var archivedAlertUrl = "${createLink(controller: "literatureAlert", action: 'archivedAlert', params: [id: configId])}";
            var literatureDetailsUrl = "${createLink(controller: 'literatureAlert', action: 'details')}";
            var isPriorityEnabled = ${isPriorityEnabled};
            var hasReviewerAccess = ${hasLiteratureReviewerAccess};
            var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
            var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
            var buttonClass = "${buttonClass}";
            var bulkCategoryUrl = "${createLink(controller: 'commonTag', action: 'fetchCommonCategories')}";
            var bulkUpdateCategoryUrl = "${createLink(controller: 'commonTag', action: 'bulkUpdateCategory')}";
            var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
            var isCategoryRole = ${SpringSecurityUtils.ifAnyGranted("ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT")};
            var currUserName = "${currUserName}"

        var signalAccessUrl
        <g:if test="${Holders.config.validatedSignal.shareWith.enabled}">
            signalAccessUrl = "${createLink(controller: 'validatedSignal', action: 'isSignalAccessible')}"
        </g:if>
        var isCaseDetailView = "";
        var alertIdSet = new Set();
        var commonProductIdSet = new Set();
    </g:javascript>
    <style type="text/css">
    .table-responsive {
        border: 0 solid #FFFFFF !important;
        overflow-x: inherit !important;
        box-shadow: 0 0 0 #aaa !important;
    }
    </style>
    <g:render template="/includes/widgets/actions/action_types"/>

</head>
<body>
<div class="container-fluid whitestrip">
    <div class="row">
        <div class="col-md-12 p2">
            <g:if test="${isArchived}">
                <span class="pl8">
                    <a href="review"> <g:message code="app.new.literature.review"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i></a>
                    <g:message code="app.label.archived.alerts.label"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i></span> : ${name}: (${dateRange})
            </span>
            </g:if>
            <g:else>
                <span class="pl8">
                    <a href="review"> <g:message code="app.new.literature.review"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i></a></span> ${name}: (${dateRange})
                </span>
                <span tabindex="0" class="saveViewPanel grid-menu-tooltip"  accesskey="s" aria-expanded="true"  title="">
                       <a href="${createLink(controller: 'literatureAlert', action: 'view', id: literatureConfigurationId) ?: '#'}"
                       target="_blank"><i class="glyphicon glyphicon-info-sign themecolor"></i></a>
                    <span class="caret hidden"></span>
                </span>
            </g:else>
        </div>
    </div>
</div>

<g:render template="/includes/layout/flashErrorsDivs" bean="${alert}" var="theInstance"/>
<div class="accordion" id="accordion-pvs-analysis">
    <div class="pv-tab" id="detailsTab">
        <!-- Nav tabs -->
        <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
            <li role="presentation" class="active">
                <a href="#details" aria-controls="details" role="tab" data-toggle="tab" accesskey="1">Alert Details</a>
            </li>
            <li role="presentation">
                <a href="#activities"  id ="activity_tab" aria-controls="activities" role="tab" data-toggle="tab" accesskey="2"><g:message
                        code="app.label.alert.activities"/></a>
            </li>
        <g:if test="${isLatest}">
            <li role="presentation">
                <a href="#archivedAlerts" aria-controls="archivedAlerts" role="tab" data-toggle="tab" accesskey="3"><g:message
                        code="app.label.archived.alerts"/></a>
            </li>
        </g:if>
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="details">
                <g:render template="includes/details_tab" model="[viewId: viewId, alertDispositionList: alertDispositionList, name: name, dateRange: dateRange , detailsisArchived: isArchived, configId: configId,litHelpMap:litHelpMap]"/>
            </div>

            <div id="activities" class="tab-pane fade" role="tabpanel">
                <g:render template="includes/activities_tab"
                          model="[type: 'Literature Search Alert' ,name:name ]"/>
            </div>

            <div role="tabpanel" class="tab-pane fade" id="archivedAlerts">
                <g:render template="includes/archivedAlert"
                          model="[id: configId, name: name]"/>
            </div>
        </div>

        <!-- The signal modal goes here. -->
        <g:render template="/includes/modals/actionCreateModal"
                  model="[appType: 'Literature Search Alert', actionConfigList: actionConfigList,actionTypeList:actionTypeList, isArchived: isArchived]"/>
        <g:render template="/includes/modals/action_list_modal"/>
        <g:render template="/template/fieldConfiguration"
                  model="[fieldConfigurationBarId: 'literatureFields']"/>
        <g:render template="/includes/modals/literature_history_modal"/>
        <g:render template="/includes/modals/alert_tag_modal"/>
        <g:render template="/includes/modals/alert_revert_justification_modal"/>
        <g:render template="/includes/modals/common_tag_modal"/>
        <g:render template="/includes/modals/alert_comment_modal"/>
        <g:render template="/includes/modals/show_attachment_modal"/>
        <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
        <g:render template="/includes/popover/dispositionJustificationSelect"/>
        <g:render template="/includes/popover/dispositionSignalSelect"
                  model="[availableSignals: availableSignals, forceJustification: forceJustification]"/>
        <g:render template="/includes/popover/priorityJustificationSelect"
                  model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
        <g:render template="/includes/popover/prioritySelect"
                  model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>
        <g:hiddenField id='hasSignalManagementAccess' name="hasSignalManagementAccess" value="${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_SIGNAL_MANAGEMENT_CONFIGURATION, ROLE_SIGNAL_MANAGEMENT_REVIEWER, ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL")}"/>
        <g:hiddenField id='isArchived' name='isArchived' value="${isArchived}"/>
    </div>
</div>
</body>
