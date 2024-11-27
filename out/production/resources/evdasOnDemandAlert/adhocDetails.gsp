<%@ page contentType="text/html;charset=UTF-8" import="com.rxlogix.enums.ReportFormat; com.rxlogix.config.PVSState;grails.util.Holders;" %>
<head>
    <meta name="layout" content="main"/>
    <title>EVDAS Adhoc Alert Details</title>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="vendorUi/popover/popover.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:javascript src="backbone/underscore.js"/>
    <asset:javascript src="backbone/backbone.js"/>
    <asset:javascript src="app/pvs/advancedFilter/advancedFilterQueryBuilder.js"/>
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
    <asset:stylesheet src="advancedFilter.css"/>

    <g:javascript>
        var userLocale = 'en';
        var apptype = "EVDAS Alert on Demand";
        var listDateRange = "${listDr}"
        var callingScreen = "${callingScreen}";
        var executedConfigId = "${executedConfigId}";
        var isViewUpdateAllowed = "${isViewUpdateAllowed}";
        var alertType = "${alertType}";
        var viewId = "${viewId}";
        var isArchived = "${false}";
        var emergingIssuesAbbrIme = {"abbr": "${Holders.config.importantEvents.ime.abbreviation}", "label": "${Holders.config.importantEvents.ime.label}"};
        var emergingIssuesAbbrDme = {"abbr": "${Holders.config.importantEvents.dme.abbreviation}", "label": "${Holders.config.importantEvents.dme.label}"};
        var emergingIssuesAbbrSM = {"abbr": "${Holders.config.importantEvents.specialMonitoring.abbreviation}", "label": "${Holders.config.importantEvents.specialMonitoring.label}"};
        var emergingIssuesAbbrEI = {"abbr": "${Holders.config.importantEvents.stopList.abbreviation}", "label": "${Holders.config.importantEvents.stopList.label}"};
        var listConfigUrl = "${createLink(controller: "evdasOnDemandAlert", action: 'listByExecutedConfig', params: [id: executedConfigId, callingScreen: callingScreen, cumulative: false, adhocRun: true])}";
        var evdasHistoryUrl = "${createLink(controller: "evdasHistory", action: 'listEvdasHistory')}";
        var statComparisonUrl = "${createLink(controller: 'statisticalComparison', action: 'showComparison', params: [configId: executedConfigId , appName: appName, callingScreen:callingScreen])}";
        var fetchStratifiedScoresUrl = "${createLink(controller: "evdasOnDemandAlert", action: "fetchStratifiedScores")}";
        var caseDrillDownUrl = "${createLink(controller: 'evdasOnDemandAlert', action:'fetchCaseDrillDownData', params: [count: count])}";
        var showTrendUrl = "${createLink(controller: "evdasOnDemandAlert", action: "showTrend")}";
        var showReportUrl = "${createLink(controller: "report", action: "index")}";
        var dateRangeListUrl = "${createLink(controller: "evdasAlert", action: "fetchDateRangeList")}";
        var fetchDrillDownDataUrl = "${createLink(controller: "evdasAlert", action: "fetchCaseDrillDownData")}";
        var saveViewUrl = "${createLink(controller: 'viewInstance', action: 'saveView')}";
        var updateViewUrl = "${createLink(controller: 'viewInstance', action: 'updateView')}";
        var deleteViewUrl = "${createLink(controller: 'viewInstance', action: 'deleteView')}";
        var evdasCaseDetailUrl = "${createLink(controller: "caseInfo", action: 'evdasCaseDetail')}";
        var discardTempChangesUrl = "${createLink(controller: 'viewInstance', action: 'discardTempChanges', params: ['viewInstance.id': viewId])}";
        var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
        var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
        var selectAutoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxAdvancedFilterSearch',params:[executedConfigId:executedConfigId])}";
        var fetchAdvFilterUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterNameAjax')}";
        var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
        var fetchUsersUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxUserSearch')}";
        var eventDetailsUrl = "${createLink(controller: "eventInfo", action: "eventDetail")}";
         var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var possibleValuesUrl = "${createLink(controller: 'evdasOnDemandAlert', action: 'fetchPossibleValues', params: [executedConfigId: executedConfigId])}";
        var allFieldsUrl = "${createLink(controller: 'evdasAlert', action: 'fetchAllFieldValues')}";
    </g:javascript>
    <asset:javascript src="app/pvs/alerts_review/evdas_alert_adhoc_details.js"/>
</head>

<body>
<div class="container-fluid whitestrip">
    <div class="row">
        <div class="col-md-12 p2">
            <span class="p18 ml-10"><a href="adhocReview">Evdas Adhoc Review <i class="fa fa-angle-right f14" aria-hidden="true"></i>
            </a> ${name}: ${dateRange}</span>
            <span><a href="${createLink(controller: 'evdasAlert', action: 'viewExecutedConfig', id: executedConfigId) ?: '#'}"
                     target="_blank" class="glyphicon glyphicon-info-sign theme-color"></a>
            </span>
        </div>
    </div>
</div>

<div class="pv-tab">
    <!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" class="active">
            <a href="#details" aria-controls="details" role="tab" data-toggle="tab">Alert Details</a>
        </li>
    </ul>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="details">
            <g:render template="adhocDetails_tab"
                      model="[id: executedConfigId, name: name, callingScreen: callingScreen, appName: appName, dateRange: dateRange, listDateRange: listDr,
                              freqNames: freqNames, viewId: viewId,evdasAdhocHelpMap:evdasAdhocHelpMap]"/>
        </div>
    </div>
</div>
<input type="hidden" name="alertType" value="${appType}"/>
<input type="hidden" id="cumulative" value="${cumulative}"/>
<input type="hidden" id="filterMap" value="${filterMap}"/>
<input type="hidden" id="columnIndex" value="${columnIndex}"/>
<input type="hidden" id="sortedColumn" value="${sortedColumn}"/>
<input type="hidden" id="advancedFilterView" value="${advancedFilterView}"/>

<g:render template="/includes/modals/stratified_scores_modal"/>
<g:render template="/advancedFilters/create_advanced_filters_modal" model="[fieldInfo: fieldList, isShareFilterViewAllowed: isShareFilterViewAllowed]"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'evdasOnDemandFields']"/>
<g:render template="/includes/modals/save_view_modal" model="[viewInstance       : viewInstance, isShareFilterViewAllowed: isShareFilterViewAllowed,
                                                              isViewUpdateAllowed: isViewUpdateAllowed]"/>

<g:hiddenField id='listDateRange' name='listDateRange' value="${listDr}"/>

</body>