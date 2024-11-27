    <%@ page contentType="text/html;charset=UTF-8" import="grails.converters.JSON; com.rxlogix.enums.ReportFormat; com.rxlogix.config.PVSState;grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title>Individual Case Adhoc Alert Details</title>

    <asset:javascript src="vendorUi/highcharts/highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/themes/grid-rx.js"/>

    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="datatables/dataTables.colReorder.min.js"/>

    <asset:javascript src="app/pvs/alerts_review/single_case_alert_details_adhoc.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryTable.js"/>
    <asset:javascript src="app/pvs/similarCases/similarCases.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="backbone/underscore.js"/>
    <asset:javascript src="backbone/backbone.js"/>
    <asset:javascript src="app/pvs/advancedFilter/advancedFilterQueryBuilder.js"/>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="query.css"/>
    <asset:stylesheet src="app/pvs/businessConfiguration.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <asset:javascript src="app/pvs/caseHistory/caseHistoryJustification.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:stylesheet src="app/pvs/pvs_list.css" />
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:javascript src="app/pvs/caseForm.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="advancedFilter.css"/>

    <g:javascript>
        var userLocale = 'en';
        var apptype = "${appType}";
        var hasReviewerAccess = ${hasSingleReviewerAccess};
        var saveCategoryAccess = "${saveCategoryAccess}";
        var viewId = "${viewId}";
        var alertType = "${alertType}";
        var callingScreen = "${callingScreen}";
        var fixedColumnScaCount = ${fixedColumnScaCount};
        var indexListSca = JSON.parse("${indexListSca}");
        var executedConfigId = "${executedConfigId}";
        var isCaseDetailView = "${isCaseDetailView}";
        var isArchived = "${false}";
        var isCaseSeries = "${false}";
        var listConfigUrl = "${createLink(controller: "singleOnDemandAlert", action: 'listByExecutedConfig',
            params: [id: executedConfigId, callingScreen: callingScreen, cumulative: cumulative, adhocRun : true])}";
        var caseHistoryUrl = "${createLink(controller: "caseHistory", action: 'listCaseHistory')}";
        var caseReviewPreviousUrl = "${createLink(controller: "singleCaseAlert", action: 'previousCaseState', params: [id: executedConfigId])}";
        var caseInfoUrl = "${createLink(controller: "singleCaseAlert", action: 'listCaseInfo',params:[cumulative:false])}";
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}";
        var getWorkflowUrl = "${createLink(controller: "workflow", action: 'getWorkflowState')}";
        var exportSignalSummary = "${createLink(controller: "singleCaseAlert", action: 'exportSignalSummaryReport', params: [id: executedConfigId])}";
        var topicUrl = "${createLink(controller: "topic", action: 'addAlertToTopic', params: [id: executedConfigId])}";
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}";
        var fetchTopicsUrl = "${createLink(controller: 'topic', action: 'fetchTopicNames')}";
        var caseHistorySuspectUrl = "${createLink(controller: "caseHistory", action: 'listSuspectProdCaseHistory')}";
        var updateAutoRouteDispositionUrl = "${createLink(controller: 'singleCaseAlert',action: 'updateAutoRouteDisposition')}"
        var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
        var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
        var saveViewUrl = "${createLink(controller: 'viewInstance', action : 'saveView')}";
        var updateViewUrl = "${createLink(controller: 'viewInstance', action : 'updateView')}";
        var deleteViewUrl = "${createLink(controller: 'viewInstance', action : 'deleteView')}";
        var privateEnabled = "${grailsApplication.config.categories.feature.private.enabled ? true : false}";
        var nonConfiguredEnabled = "${grailsApplication.config.categories.feature.nonConfigured.enabled ? true : false}";
        var fillCommonTagUrl = "${createLink(controller: 'commonTag', action: 'getQualAlertCategories')}";
        var saveCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'saveAlertCategories')}";
        var fetchCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'commonTagDetails')}";
        var fetchAdvFilterUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterNameAjax')}";
        var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
        var discardTempChangesUrl = "${createLink(controller: 'viewInstance', action: 'discardTempChanges', params: ['viewInstance.id': viewId])}";
        var possibleValuesUrl = "${createLink(controller: 'singleOnDemandAlert', action: 'fetchPossibleValues', params: [executedConfigId: executedConfigId])}";
        var allFieldsUrl = "${createLink(controller: 'singleOnDemandAlert', action: 'fetchAllFieldValues')}";
        var selectAutoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxAdvancedFilterSearch',params:[executedConfigId:executedConfigId])}";
        var caseFormDowanloadUrl = "${createLink(controller: 'singleCaseAlert', action: 'downloadCaseForm')}"
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var deleteTempViewUrl = "${createLink(controller: 'viewInstance', action: 'deleteTempView')}"
        var tempViewPresent = ${tempViewPresent};
        var isTempViewSelected = ${isTempViewSelected};
        var clipboardInterval = ${clipboardInterval};
        var bulkCategoryUrl = "${createLink(controller: 'commonTag', action: 'fetchCommonCategories')}";
         var bulkUpdateCategoryUrl = "${createLink(controller: 'commonTag', action: 'bulkUpdateCategory')}";
         var reportTemplateUrl = "${createLink(controller: "template", action: 'index', params: [configId: executedConfigId])}";

        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
         var alertIdSet = new Set();
        var commonProductIdSet = new Set();
        var columnLabelMapForDetail = JSON.parse('${columnLabelMap as JSON}');
        var exportAlways = "${exportAlways}";
        var promptUser = "${promptUser}";
        var isVaers = "${isVaers}";

    </g:javascript>

    <g:if test="${!isCaseDetailView}">
        <g:javascript>
            $(document).ready(function () {
                window.sca_data_table.fixedColumns = new $.fn.dataTable.FixedColumns(window.sca_data_table, {
                    iLeftColumns: 2
                });
            });

        </g:javascript>

    </g:if>
</head>

<body>
<div class="container-fluid whitestrip">
    <div class="row">
        <div class="col-md-12 p2">
            <span class="p18 ml-10"><a href="adhocReview">Individual Case Adhoc Review <i class="fa fa-angle-right f14" aria-hidden="true"></i>
            </a> ${name}: ${dateRange}</span>
            <span><a href="${createLink(controller: 'singleCaseAlert', action: 'viewExecutedConfig', id: executedConfigId) ?: '#'}"
                     target="_blank" class="glyphicon glyphicon-info-sign theme-color"></a>
            </span>
        </div>
    </div>
</div>

<g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

<div class="pv-tab" id="detailsTab">
    <!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" class="active" >
            <a href="#details"  aria-controls="details" role="tab" data-toggle="tab">Alert Details</a>
        </li>
    </ul>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="details">
            <g:render template="adhoc_details_tab"
                      model="[id: executedConfigId, name: name, callingScreen: callingScreen, cumulative: cumulative, viewId: viewId, reportUrl: reportUrl, analysisFileUrl: analysisFileUrl, customFieldsEnabledAdhoc: customFieldsEnabledAdhoc, columnLabelMap: columnLabelMap, adhocIcrHelpMap:adhocIcrHelpMap]"/>
        </div>
    </div>
</div>

<div id="copyCaseNumberModel" class="modal fade">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">Select Case Numbers</h4>
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

<input type="hidden" id="cumulative" value="${cumulative}"/>
<input type="hidden" id="filterMap" value="${filterMap}"/>
<input type="hidden" id="columnIndex" value="${columnIndex}"/>
<input type="hidden" id="sortedColumn" value="${sortedColumn}"/>
<input type="hidden" id="advancedFilterView" value="${advancedFilterView}"/>
<input type="hidden" id="filterMap" value=""/>
<input type="hidden" id="columnIndex" value=""/>
<input type="hidden" id="sortedColumn" value=""/>
<input type="hidden" id="fullCaseList"/>
<g:hiddenField id='customFieldsEnabledAdhoc' name='customFieldsEnabledAdhoc' value="${customFieldsEnabledAdhoc}"/>
<input type="hidden" id="showDob" value="${showDob}"/>

<g:render template="/includes/modals/followUp_modal"/>
<g:render template="/includes/modals/message_box"/>
<g:render template="/includes/modals/add_alert_to_topic" modal="[strategyList: strategyList]"/>
<g:render template="/includes/modals/downloadReportDatePickerModal"/>
<g:render template="/includes/modals/case_form_name_modal"/>
<g:render template="/includes/modals/case_form_list_modal"/>
<g:render template="/includes/modals/save_view_modal" model="[viewInstance       : viewInstance, isShareFilterViewAllowed: isShareFilterViewAllowed,
                                                              isViewUpdateAllowed: isViewUpdateAllowed]"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'qualitativeOnDemandFields']"/>
<g:render template="/advancedFilters/create_advanced_filters_modal" model="[fieldInfo: fieldList, isShareFilterViewAllowed: isShareFilterViewAllowed]"/>
<g:render template="/includes/modals/common_tag_modal"/>
<g:render template="/advancedFilters/includes/copyPasteFilterModal"/>
</body>