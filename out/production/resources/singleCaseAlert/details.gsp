<%@ page contentType="text/html;charset=UTF-8" import="grails.converters.JSON; com.rxlogix.enums.ReportFormat; grails.plugin.springsecurity.SpringSecurityUtils; grails.util.Holders;com.rxlogix.Constants" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.title.qualitative.details"/></title>
<g:javascript>
        var selectedFilter=false;
</g:javascript>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/themes/grid-rx.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>

    <asset:javascript src="app/pvs/alerts_review/single_case_alert_details.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryTable.js"/>
    <asset:javascript src="app/pvs/similarCases/similarCases.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryJustification.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:javascript src="purify/purify.min.js" />


    <asset:javascript src="backbone/underscore.js"/>
    <asset:javascript src="backbone/backbone.js"/>
    <asset:javascript src="app/pvs/advancedFilter/advancedFilterQueryBuilder.js"/>
    <asset:stylesheet src="query.css"/>
    <asset:stylesheet src="app/pvs/businessConfiguration.css"/>

    <asset:stylesheet src="app/pvs/pvs_list.css" />
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="app/pvs/caseForm.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="advancedFilter.css"/>

    <g:javascript>
        var userLocale = 'en';
        var apptype = "${appType}";
        var saveCategoryAccess = "${saveCategoryAccess}";
        var hasReviewerAccess = ${hasSingleReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
        var buttonClass = "${buttonClass}";
        var isViewUpdateAllowed = "${isViewUpdateAllowed}";
        var callingScreen = "${callingScreen}";
        var fixedColumnScaCount = ${fixedColumnScaCount};
        var indexListSca = JSON.parse("${indexListSca}");
        var executedConfigId = "${executedConfigId}";
        var dashboardFilter = "${dashboardFilter}";
        var viewId = "${viewId}";
        var alertType = "${alertType}";
        var isArchived = "${isArchived}";
        var isCaseSeries = "${isCaseSeries}";
        var isAdhocCaseSeries = "${isAdhocCaseSeries}";
        var isCaseSeriesAlert = "${isCaseSeriesAlert}";
        var isAggregateAdhoc="${isAggregateAdhoc}";
        var limitNoOfCases = "${Holders.config.caseSeries.limitNoOfCases}";
        var isFlagEnabled = "false";
        var isCaseSeriesGenerating = "${isCaseSeriesGenerating}";
        var justificationObj = JSON.parse("${justificationJSON}");
        var isVaers = $("#isVaers").val() == "true";
        var isVigibase = $("#isVigibase").val() == "true";
        var listConfigUrl = "${createLink(controller: "singleCaseAlert", action: 'listByExecutedConfig',
            params: [id: executedConfigId, cumulative: cumulative, adhocRun: false, dashboardFilter: dashboardFilter, tagName: tagName, isArchived: isArchived, isCaseSeries: isCaseSeries,isVaers: isVaers, isVigibase: isVigibase, isJader: isJader,viewId: viewId,alertType:alertType])}";
        var caseHistoryUrl = "${createLink(controller: "caseHistory", action: 'listCaseHistory')}";
        var updateJustificationUrl = "${createLink(controller: "caseHistory", action: 'updateJustification')}";
        var caseReviewPreviousUrl = "${createLink(controller: "singleCaseAlert", action: 'previousCaseState', params: [id: executedConfigId])}";
        var caseInfoUrl = "${createLink(controller: "singleCaseAlert", action: 'listCaseInfo', params:[cumulative: cumulative])}";
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}";
        var changePriorityUrl = "${createLink(controller: "singleCaseAlert", action: 'changePriorityOfAlert')}";
        var exportSignalSummary = "${createLink(controller: "singleCaseAlert", action: 'exportSignalSummaryReport', params: [id: executedConfigId])}";
        var topicUrl = "${createLink(controller: "topic", action: 'addAlertToTopic', params: [id: executedConfigId])}";
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}";
        var fetchTopicsUrl = "${createLink(controller: 'topic', action: 'fetchTopicNames')}";
        var caseHistorySuspectUrl = "${createLink(controller: "caseHistory", action: 'listSuspectProdCaseHistory')}";
        var singleCaseDetailsUrl = "${createLink(controller: 'singleCaseAlert', action: 'details')}";
        var saveViewUrl = "${createLink(controller: 'viewInstance', action : 'saveView')}";
        var updateViewUrl = "${createLink(controller: 'viewInstance', action : 'updateView')}";
        var deleteViewUrl = "${createLink(controller: 'viewInstance', action : 'deleteView')}";
        var fetchTagsUrl = "${createLink(controller: 'singleCaseAlert', action: 'searchTagsList')}";
        var fetchUsersUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxUserSearch')}";
        var saveTagUrl = "${createLink(controller: 'singleCaseAlert', action: 'saveAlertTags', params: [isArchived: isArchived])}";
        var fillCommonTagUrl = "${createLink(controller: 'commonTag', action: 'getQualAlertCategories')}";
        var saveCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'saveAlertCategories')}";
        var fetchCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'commonTagDetails')}";
        var privateEnabled = "${grailsApplication.config.categories.feature.private.enabled ? true : false}"
        var nonConfiguredEnabled = "${grailsApplication.config.categories.feature.nonConfigured.enabled ? true : false}"
        var tagDetailsUrl = "${createLink(controller: 'singleCaseAlert', action: 'details')}";
        var saveCasesUrl = "${createLink(controller: 'singleCaseAlert', action: 'saveCaseSeries', params: [executedConfigId: executedConfigId])}";
        var isCaseDetailView = "${isCaseDetailView}";
        var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
        var assignToGroupUrl = "${createLink(controller: 'singleCaseAlert', action: 'changeAssignedToGroup')}";
        var discardTempChangesUrl = "${createLink(controller: 'viewInstance', action: 'discardTempChanges', params: ['viewInstance.id': viewId])}";
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var dispositionData = JSON.parse('${dispositionData}')
        var forceJustification = ${forceJustification};
        var changeDispositionUrl = "${createLink(controller: 'singleCaseAlert', action: 'changeDisposition', params: [callingScreen: callingScreen])}";
        var allowedProductsAsSafetyLead = "${allowedProductsAsSafetyLead}".split(",");
        var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
        var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
        var signalDetailUrl = "${createLink(controller: 'validatedSignal', action: 'details')}";
        var archivedAlertUrl = "${createLink(controller: "singleCaseAlert", action: 'archivedAlert', params: [id: executedConfigId])}";
        var isProductSecurity = "${isProductSecurity}";
        var availableSignalNameList = JSON.parse('${availableSignals.collect{it.name} as JSON}');
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var possibleValuesUrl = "${createLink(controller: 'singleCaseAlert', action: 'fetchPossibleValues', params: [executedConfigId: executedConfigId])}";
        var allFieldsUrl = "${createLink(controller: 'singleCaseAlert', action: 'fetchAllFieldValues', params: [isFaers: isFaers, isVaers: isVaers, isVigibase: isVigibase,isJader: isJader])}";
        var selectAutoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxAdvancedFilterSearch',params:[executedConfigId:executedConfigId])}";
        var fetchAdvFilterUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterNameAjax')}";
        var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
        var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
        var changeAlertLevelDispositionUrl = "${createLink(controller: 'singleCaseAlert',action: 'changeAlertLevelDisposition')}";
        var updateAutoRouteDispositionUrl = "${createLink(controller: 'singleCaseAlert',action: 'updateAutoRouteDisposition')}";
        var isWarningMessageUrl = "${createLink(controller: 'singleCaseAlert', action: 'isWarningMessageInAutoRouteDisposition')}";
        var alertActivitiesUrl = "${createLink(controller: 'activity',action: 'listByExeConfig')}";
        var deleteTempViewUrl = "${createLink(controller: 'viewInstance', action: 'deleteTempView')}"
        var caseFormDowanloadUrl = "${createLink(controller: 'singleCaseAlert', action: 'downloadCaseForm')}"
        var reviewCompletedDispostionList = JSON.parse('${reviewCompletedDispostionList}');
        var isPriorityEnabled = ${isPriorityEnabled};
        var tempViewPresent = ${tempViewPresent};
        var detailedAdvancedFilterId  = ${detailedAdvancedFilterId};
        var detailedAdvanceFilterName = "${detailedAdvanceFilterName}";
        var detailedViewInstanceId = ${detailedViewInstanceId};
        var isTempViewSelected = ${isTempViewSelected};
        var clipboardInterval = ${clipboardInterval};
        var reportTemplateUrl = "${createLink(controller: "template", action: 'index', params: [configId: executedConfigId,type: type,typeFlag:typeFlag, aggExecutionId:aggExecutionId, version: version, productName: productName, eventName: eventName, alertId: alertId])}";
        var versionNumber = ${version};
        var signalAccessUrl
            var bulkCategoryUrl = "${createLink(controller: 'commonTag', action: 'fetchCommonCategories')}";
           var bulkUpdateCategoryUrl = "${createLink(controller: 'commonTag', action: 'bulkUpdateCategory')}";
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
        var isCategoryRole = ${SpringSecurityUtils.ifAnyGranted("ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT")};
        var alertIdSet = new Set();
        var commonProductIdSet = new Set();
        var revertDispositionUrl = "${createLink(controller: 'singleCaseAlert', action: 'revertDisposition', params: [callingScreen: callingScreen])}"
        var currUserName = "${currUserName}"
        var columnLabelMapForDetail = JSON.parse('${columnLabelMap as JSON}');
        var exportAlways = "${exportAlways}";
        var promptUser = "${promptUser}";
        var isFaersCheck = "${isFaers}";
        var isJaderCheck = "${isJader}";
        <g:if test="${Holders.config.validatedSignal.shareWith.enabled}">
            signalAccessUrl = "${createLink(controller: 'validatedSignal', action: 'isSignalAccessible')}"
        </g:if>
    </g:javascript>
    <g:if test="${!isCaseDetailView}">
        <g:javascript>
            $(document).ready(function () {
                var leftFixedColumns = isPriorityEnabled ? 5 : 4;
                if(callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS || callingScreen == CALLING_SCREEN.DASHBOARD) {
                    leftFixedColumns = leftFixedColumns + 1; //plus 1 for name
                }
                if($("#isFaers").val() == "true" || $("#isVaers").val() == "true" || $("#isVigibase").val() == "true"){
                    leftFixedColumns = 3;
                }
                window.sca_data_table.fixedColumns = new $.fn.dataTable.FixedColumns(window.sca_data_table, {
                    iLeftColumns: leftFixedColumns
                });
            });

        </g:javascript>
    </g:if>
    <g:render template="/includes/widgets/actions/action_types"/>
</head>

<body>
<!------------==============-------------white stripe code start--------------==========---------------->
<div class="container-fluid whitestrip">
    <div class="row">
        <div class="col-md-12 p2">
            <g:if test="${callingScreen == Constants.Commons.REVIEW && !isCaseSeries}">
                <g:if test="${isArchived}">
                    <span class="p18 ml-10"><a href="review"><g:message code="app.single.case.review"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i>
                    </a><g:message code="app.label.archived.alerts.label"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i> ${name}: ${dateRange}</span>
                </g:if>
                <g:else>
                    <span class="p18 ml-10"><a href="review">Individual Case Review <i class="fa fa-angle-right f14" aria-hidden="true"></i>
                    </a> ${name}: ${dateRange}</span>
                </g:else>
                <span><a
                        href="${createLink(controller: 'singleCaseAlert', action: 'viewExecutedConfig', id: executedConfigId) ?: '#'}"
                        target="_blank" class="glyphicon glyphicon-info-sign themecolor"></a></span>
            </g:if>
            <g:else>
                <span class="p18"><a>${isFaers ? "FAERS" : isVigibase ? "VigiBase" : isJader ? "JADER" : isVaers ? "VAERS" : "Safety DB"} - <g:message code="app.label.case.series"/></a> (${pECaseSeries})</span>
            </g:else>
        </div>
    </div>
</div>

<g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

    <div class="pv-tab" id="detailsTab">
        <g:if test="${callingScreen == "tags"}">

                <a href="#" class="btn pv-btn-grey pull-right tag-screen-back-btn" role="button"><i class="fa fa-long-arrow-left"> </i> Back </a>

        </g:if>
        <!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" class="active" id="details-tab">
            <a href="#details" aria-controls="details" role="tab" data-toggle="tab" accesskey="1"><g:message
                    code="app.label.alert.details"/></a>
        </li>
        <li role="presentation">
            <a href="#activities"  id ="activity_tab" aria-controls="activities" role="tab" data-toggle="tab" accesskey="2"><g:message
                    code="app.label.alert.activities"/></a>
        </li>
        <g:if test="${isLatest && !isCaseSeries}">
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
                      model="[id: executedConfigId, name: name, alertDispositionList: alertDispositionList, reportUrl: reportUrl, reportName: reportName, analysisFileUrl: analysisFileUrl, callingScreen: callingScreen, isVaers: isVaers, isVigibase: isVigibase, isJader: isJader, aggExecutionId:aggExecutionId,
                              cumulative: cumulative,hasSignalViewAccessAccess: hasSignalViewAccessAccess, dateRange: dateRange, viewId: viewId, isAdhocCaseSeries:isAdhocCaseSeries, isCaseSeries: isCaseSeries, isCaseVersion: isCaseVersion,isFaers: isFaers, customFieldsEnabled: customFieldsEnabled, isArchived: isArchived, soc: soc, caseSeriesId: caseSeriesId, eventName: eventName, productName:productName, columnLabelMap: columnLabelMap,singleHelpMap:singleHelpMap,alertId: alertId, version: version, type: type, typeFlag: typeFlag]"/>

        </div>

        <div id="activities" class="tab-pane fade" role="tabpanel">
            <g:render template="/includes/widgets/activities_tab"
                      model="[alertId: executedConfigId, name: name, type: 'Single Case Alert', callingScreen: callingScreen, dateRange: dateRange, isCaseSeries: isCaseSeries]"/>
        </div>
        <div role="tabpanel" class="tab-pane fade" id="archivedAlerts">
            <g:render template="includes/archivedAlerts"
                      model="[id: executedConfigId, name: name, type: 'Single Case Alert']"/>
        </div>
    </div>
</div>

<div id="copyCaseNumberModel" class="modal fade">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <label class="modal-title">Select Case Numbers</label>
            </div>

            <div class="modal-body row">
                <textarea id="caseNumbers" class="form-control input-large" rows='3'></textarea>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary">Save changes</button>
            </div>
        </div>
    </div>
</div>

<input type="hidden" id="safetyProductName" value="${safetyProductName}"/>
<input type="hidden" id="cumulative" value="${cumulative}"/>
<input type="hidden" id="filterMap" value="${filterMap}"/>
<input type="hidden" id="columnIndex" value="${columnIndex}"/>
<input type="hidden" id="sortedColumn" value="${sortedColumn}"/>
<input type="hidden" id="advancedFilterView" value="${advancedFilterView}"/>
<input type="hidden" id="isFaers" value="${isFaers}"/>
<input type="hidden" id="isVaers" value="${isVaers}"/>
<input type="hidden" id="isVigibase" value="${isVigibase}"/>
<input type="hidden" id="isJader" value="${isJader}"/>
<input type="hidden" id="isCaseSeries" value="${isCaseSeries}"/>
<input type="hidden" id="isAdhocCaseSeries" value="${isAdhocCaseSeries}"/>
<input type="hidden" id="fullCaseList"/>
<input type="hidden" id="showDob" value="${showDob}"/>
<input type="hidden" id="aggExecutionId" value="${aggExecutionId}"/>
<input type="hidden" id="alert-Id" value="${alertId}"/>
<input type="hidden" id="type-name" value="${type}"/>
<input type="hidden" id="type-flag-name" value="${typeFlag}"/>
<input type="hidden" id="versionNo" value="${version}"/>
<input type="hidden" id="productName-data" value="${productName}"/>
<input type="hidden" id="eventName-data" value="${eventName}"/>

<g:hiddenField id='isArchived' name='isArchived' value="${isArchived}"/>

<g:hiddenField id='customFieldsEnabled' name='customFieldsEnabled' value="${customFieldsEnabled}"/>
<g:hiddenField id='hasSignalManagementAccess' name="hasSignalManagementAccess" value="${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_SIGNAL_MANAGEMENT_CONFIGURATION, ROLE_SIGNAL_MANAGEMENT_REVIEWER, ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL")}"/>

<g:render template="/includes/modals/actionCreateModal"
          model="[id: executedConfigId, appType: 'Single Case Alert', actionConfigList: actionConfigList,actionTypeList:actionTypeList, isArchived: isArchived]"/>
<g:render template="/includes/modals/addCaseModal"
          model="[id: executedConfigId, justification: justification]"/>
<g:render template="/includes/modals/action_list_modal"/>
<g:render template="/includes/modals/case_history_modal" model="[executedConfigId: executedConfigId, isVaers: isVaers, isVigibase: isVigibase, isCaseVersion: isCaseVersion, isFaers:isFaers]"/>
<g:render template="/includes/modals/similar_cases_modal"/>
<g:render template="/includes/modals/alert_comment_modal"/>
<g:render template="/includes/modals/alert_revert_justification_modal"/>
<g:render template="/includes/modals/comment_history_modal" model="[isVaers: isVaers, isVigibase: isVigibase, isFaers: isFaers]"/>
<g:render template="/includes/modals/extendedComment"/>
<g:render template="/includes/modals/followUp_modal"/>
<g:render template="/includes/modals/message_box"/>
<g:render template="/includes/modals/downloadReportDatePickerModal"/>
<g:render template="/includes/modals/show_attachment_modal"/>
<g:render template="/includes/modals/case_form_name_modal"/>
<g:render template="/includes/modals/case_form_list_modal"/>
<g:render template="/includes/modals/save_view_modal" model="[viewInstance: viewInstance, isShareFilterViewAllowed: isShareFilterViewAllowed,
                                                              isViewUpdateAllowed: isViewUpdateAllowed]"/>
<g:render template="/includes/modals/single_case_alert_tag_modal"/>
<g:render template="/includes/modals/common_tag_modal"/>
<g:render template="/includes/modals/show_config_name_modal"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'qualitativeFields']"/>
<g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
<g:render template="/includes/popover/dispositionJustificationSelect" />
<g:render template="/includes/popover/dispositionSignalSelect" model="[availableSignals: availableSignals, forceJustification: forceJustification]"/>
<g:render template="/includes/popover/priorityJustificationSelect" model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
<g:render template="/includes/popover/prioritySelect" model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>
<div id="single-case-alert-spinner" class="hidden">
    <div class="grid-loading" style="position: fixed;left: 50%;top: 50%;"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>
</div>
<g:render template="/advancedFilters/create_advanced_filters_modal" model="[fieldInfo: fieldList, isShareFilterViewAllowed: isShareFilterViewAllowed]"/>
<g:render template="/advancedFilters/includes/copyPasteFilterModal"/>
</body>