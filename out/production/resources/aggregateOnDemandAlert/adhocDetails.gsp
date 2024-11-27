<%@ page contentType="text/html;charset=UTF-8" import="grails.converters.JSON;com.rxlogix.Constants; com.rxlogix.signal.Justification; com.rxlogix.enums.ReportFormat; grails.plugin.springsecurity.SpringSecurityUtils; grails.util.Holders;com.rxlogix.config.PVSState" %>
<head>
    <meta name="layout" content="main"/>
    <title>Aggregate Adhoc Alert Details</title>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/dataTables.fixedColumns.min.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>

    <asset:javascript src="backbone/underscore.js"/>
    <asset:javascript src="backbone/backbone.js"/>
    <asset:javascript src="app/pvs/advancedFilter/advancedFilterQueryBuilder.js"/>
    <asset:stylesheet src="query.css"/>
    <asset:stylesheet src="app/pvs/businessConfiguration.css"/>

    <asset:stylesheet src="app/pvs/pvs_list.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="app/pvs/fixedColumns.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="advancedFilter.css"/>
    <asset:javascript src="app/pvs/caseForm.js"/>

    <g:javascript>
        var selectedDatasource="${selectedDatasource}";
        var callingScreen = "${callingScreen}"
        var groupBySmq = "${groupBySmq}"
        var dr = "${dr}"
        var apptype = "${appType}";
        var saveCategoryAccess = "${saveCategoryAccess}";
        var isViewUpdateAllowed = "${isViewUpdateAllowed}";
        var viewId = "${viewId}";
        var alertType = "${alertType}"
        var hasReviewerAccess = ${hasAggReviewerAccess};
        var buttonClass = "${buttonClass}";
        var isArchived = "${false}";
        var isCaseSeries = "${false}";
        var executedConfigId = "${executedConfigId}"
        var listConfigUrl = "${createLink(controller: 'aggregateOnDemandAlert', action: 'listByExecutedConfig',
            params: [id: executedConfigId, adhocRun:true])}"
        var pvrIntegrate = "${grailsApplication.config.pvreports.url ? true : false}";
        var privateEnabled = "${grailsApplication.config.categories.feature.private.enabled ? true : false}"
        var nonConfiguredEnabled = "${grailsApplication.config.categories.feature.nonConfigured.enabled ? true : false}"
        var productEventHistoryUrl = "${createLink(controller: "productEventHistory", action: 'listProductEventHistory')}"
        var template_list_url = "${createLink(controller: 'template', action: 'index')}"
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}"
        var getWorkflowUrl = "${createLink(controller: "workflow", action: 'getWorkflowState')}"
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}"
        var generateCaseSeriesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'generateCaseSeries')}";
        var topicUrl = "${createLink(controller: "topic", action: 'addAlertToTopic', params: [id: executedConfigId])}"
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}"
        var statComparisonUrl = "${createLink(controller: 'statisticalComparison', action: 'showComparison', params: [configId: executedConfigId, appName: "Aggregate Case Alert", callingScreen: callingScreen])}"
        var fetchTopicsUrl = "${createLink(controller: 'topic', action: 'fetchTopicNames')}"
        var showTrendUrl = "${createLink(controller: "aggregateCaseAlert", action: "showTrend")}"
        var evdasCaseDetailUrl = "${createLink(controller: "caseInfo", action: 'evdasCaseDetail')}";
        var fetchDrillDownDataUrl = "${createLink(controller: "evdasAlert", action: "fetchCaseDrillDownData")}";
        var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
        var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
        var fetchStratifiedScoresUrl = "${createLink(controller: "aggregateCaseAlert", action: "fetchStratifiedScores", params:[isArchived: isArchived])}";
        var dateRangeListUrl = "${createLink(controller: "aggregateCaseAlert", action: "fetchDateRangeList")}";
        var saveViewUrl = "${createLink(controller: 'viewInstance', action : 'saveView')}";
        var updateViewUrl = "${createLink(controller: 'viewInstance', action : 'updateView')}";
        var deleteViewUrl = "${createLink(controller: 'viewInstance', action : 'deleteView')}";
        var fetchTagsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'alertTagDetails', params:[isArchived: isArchived])}";
        var fillCommonTagUrl = "${createLink(controller: 'commonTag', action: 'getQuanAlertCategories')}";
        var saveCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'saveAlertCategories')}";
        var fetchCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'commonTagDetails')}";
        var saveTagUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'saveAlertTags', params:[isArchived: isArchived])}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var possibleValuesUrl = "${createLink(controller: 'aggregateOnDemandAlert', action: 'fetchPossibleValues', params: [executedConfigId: executedConfigId])}";
        var allFieldsUrl = "${createLink(controller: 'aggregateOnDemandAlert', action: 'fetchAllFieldValues',params: [executedConfigId: executedConfigId])}";
        var selectAutoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxAdvancedFilterSearch',params:[executedConfigId:executedConfigId])}";
        var fetchAdvFilterUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterNameAjax')}";
        var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
        var fetchUsersUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxUserSearch')}";
        var fetchAllTagsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'alertTagDetails')}";
        var customAnalysisThirdPartyUrl = "${Holders.config.custom.thirdParty.analysis.url}";
        var customAnalysisLabel = "${Holders.config.custom.thirdParty.analysis.label}";
        var isCustomAnalysisEnabed = ${Holders.config.custom.thirdParty.analysis.enabled};
        var forceJustification = ${forceJustification};
        var allowedProductsAsSafetyLead = "${allowedProductsAsSafetyLead}".split(",");
        var isProductSecurity = "${isProductSecurity}";
        var filterIndexList = JSON.parse('${filterIndex}');
        var filterIndexMap = JSON.parse('${filterIndexMap}');
        var eventDetailsUrl = "${createLink(controller: "eventInfo", action: "eventDetail")}";
        var discardTempChangesUrl = "${createLink(controller: 'viewInstance', action: 'discardTempChanges', params: ['viewInstance.id': viewId])}";
        var configId = ${configId};
        var isFaersEnabled = ${isFaersEnabled};
        var isVaersEnabled = ${isVaersEnabled};
        var isVigibaseEnabled = ${isVigibaseEnabled};
        var isEvdasEnabled = ${isEvdasEnabled};
        var isPvaEnabled = ${isPvaEnabled};
        var isDataMining = ${isDataMining};
        var isBatchAlert = ${isBatchAlert};
        var miningVariable = "${miningVariable}";
        var emergingIssuesAbbrIme = {"abbr": "${Holders.config.importantEvents.ime.abbreviation}", "label": "${Holders.config.importantEvents.ime.label}"};
        var emergingIssuesAbbrDme = {"abbr": "${Holders.config.importantEvents.dme.abbreviation}", "label": "${Holders.config.importantEvents.dme.label}"};
        var emergingIssuesAbbrSM = {"abbr": "${Holders.config.importantEvents.specialMonitoring.abbreviation}", "label": "${Holders.config.importantEvents.specialMonitoring.label}"};
        var emergingIssuesAbbrEI = {"abbr": "${Holders.config.importantEvents.stopList.abbreviation}", "label": "${Holders.config.importantEvents.stopList.label}"};
        var dataAnalysisLabel="${message(code: 'app.label.dataAnalysis')}";
        var spotfireEnabled="${Holders.config.signal.spotfire.enabled}";
        var analysisStatusJson=JSON.parse('${analysisStatusJson}');
        var parameter = {
            executedConfigId : "${Holders.config.custom.thirdParty.executedConfigId}",
            baseId           : "${Holders.config.custom.thirdParty.baseId}",
            meddraPtCode     : "${Holders.config.custom.thirdParty.meddraPtCode}",
            dateRangeTypeFlag: "${Holders.config.custom.thirdParty.dateRangeTypeFlag}",
            termScope        : "${Holders.config.custom.thirdParty.termScope}",
            flag             : "${Holders.config.custom.thirdParty.flag}",
            queryParam       : "${Holders.config.custom.thirdParty.queryParam}"
        }
         var bulkCategoryUrl = "${createLink(controller: 'commonTag', action: 'fetchCommonCategories')}";
         var bulkUpdateCategoryUrl = "${createLink(controller: 'commonTag', action: 'bulkUpdateCategory')}";
             var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
         var isCaseDetailView = "";
         var alertIdSet = new Set();
        var commonProductIdSet = new Set();
        var adhocColumnListNew = "${adhocColumnListNew as grails.converters.JSON}";
    </g:javascript>
    <asset:javascript src="app/pvs/productEventHistory/productEventHistoryTable.js"/>
    <asset:javascript src="app/pvs/alerts_review/agg_case_alert_adhoc_details.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <style>
    .yadcf-filter{
        height: 24px;
        padding-top: unset;
        padding-bottom: unset;
        text-align: left;
        max-width: 130px;
        color: inherit;
        background-color: white;
        border-radius: 4px;
        border: 1px solid #ccc;
    }
    </style>
</head>

<body>

<div class="container-fluid whitestrip">
    <div class="row">
        <div class="col-md-12 p2">
            <g:if test="${callingScreen == Constants.Commons.REVIEW}">
                <span class="pl8"><a href="adhocReview"> <g:message code="app.aggregated.case.review.adhoc"/> <i class="fa fa-angle-right f14" aria-hidden="true"></i>
                </a></span> ${name}: ${dateRange}</span>
                <span class="pos-rel drange" style="float: none;">
                    <span tabindex="0" class="saveViewPanel grid-menu-tooltip" accesskey="s" aria-expanded="true"
                          title="">
                        <a href="${createLink(controller: 'aggregateCaseAlert', action: 'viewExecutedConfig', id: executedConfigId) ?: '#'}"
                           target="_blank"><i class="glyphicon glyphicon-info-sign themecolor"></i></a>
                        <span class="caret hidden"></span>
                    </span>
                    <g:if test="${evdasDateRange || faersDateRange || vaersDateRange || vigibaseDateRange}">
                        <ul class="dropdown-menu save-list mw-252" id="fearsEvedas">

                            <li class="pdr5">
                                <g:if test="${evdasDateRange}">
                                    <span class="otherDataSourcesName">${Constants.DataSource.EVDAS.toUpperCase()}:</span>
                                    <span class="otherDataSourcesDate">${evdasDateRange}</span>
                                </g:if>
                                <g:if test="${faersDateRange}">
                                    <span class="otherDataSourcesName">${Constants.DataSource.FAERS.toUpperCase()}:</span>
                                    <span class="otherDataSourcesDate">${faersDateRange}</span>
                                </g:if>
                                <g:if test="${vaersDateRange}">
                                    <span class="otherDataSourcesName">${Constants.DataSource.VAERS.toUpperCase()}:</span>
                                    <span class="otherDataSourcesDate">${vaersDateRange}</span>
                                </g:if>
                                <g:if test="${vigibaseDateRange}">
                                    <span class="otherDataSourcesName">${Constants.DataSource.VIGIBASE_CAMEL_CASE}:</span>
                                    <span class="otherDataSourcesDate">${vigibaseDateRange}</span>
                                </g:if>
                            </li>

                        </ul>
                    </g:if>

                </span>
            </g:if>
            <g:else>
                <span class="panel-title">Aggregate Adhoc Alert</span>
            </g:else>
        </div>
    </div>
</div>

<g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

<div class="pv-tab">
    <g:if test="${callingScreen == "tags"}">

        <a href="#" class="btn pv-btn-grey pull-right tag-screen-back-btn" role="button"><i class="fa fa-long-arrow-left"></i>  Back</a>

    </g:if>
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
                      model="[id: executedConfigId, isJader: isJader, name: name, callingScreen: callingScreen, groupBySmq: groupBySmq, showPrr: showPrr, showRor: showRor, showEbgm: showEbgm, isVaersEnabled: isVaersEnabled, isVigibaseEnabled: isVigibaseEnabled, isFaersEnabled: isFaersEnabled,
                              showDss: showDss, dateRange: dateRange, reportUrl: reportUrl, analysisFileUrl: analysisFileUrl, viewId: viewId, evdasEndDate: evdasEndDate, freqNames: freqNames,
                              faersEndDate:faersEndDate, selectedDatasource: selectedDatasource, faersDateRange: faersDateRange, evdasDateRange: evdasDateRange, appType: appType,subGroupsColumnList:subGroupsColumnList,
                              miningVariable: miningVariable, isBatchAlert: isBatchAlert, aggAdhocDetailHelpMap: aggAdhocDetailHelpMap,adhocColumnListNew:adhocColumnListNew,relativeSubGroupMap:relativeSubGroupMap,prrRorSubGroupMap:prrRorSubGroupMap,subGroupColumnInfo:subGroupColumnInfo]"/>
        </div>

    </div>
</div>

<input type="hidden" id="cumulative" value="${cumulative}"/>
<input type="hidden" id="groupBySmq" value="${groupBySmq}"/>
<input type="hidden" id="dssUrl" value="${dssUrl}"/>
<input type="hidden" id="filterMap" value="${filterMap}"/>
<input type="hidden" id="columnIndex" value="${columnIndex}"/>
<input type="hidden" id="sortedColumn" value="${sortedColumn}"/>
<input type="hidden" id="isFaers" value="${isFaers}"/>
<input type="hidden" id="isVaers" value="${isVaers}"/>
<input type="hidden" id="isVigibase" value="${isVigibase}"/>
<input type="hidden" id="isJader" value="${isJader}"/>
<input type="hidden" id="advancedFilterView" value="${advancedFilterView}"/>
<g:hiddenField id='isCaseSeriesAccess' name="isCaseSeriesAccess" value="${isCaseSeriesAccess}"/>
<input type="hidden" id="symptomsComanifestationColumnName" value="${Holders.config.spotfire.symptomsComanifestationColumnName}"/>
<input type="hidden" id="symptomsComanifestation" value="${Holders.config.spotfire.symptomsComanifestation}"/>

<g:render template="/includes/modals/message_box" />
<g:hiddenField id='showPrr' name='showPrr' value="${showPrr}"/>
<g:hiddenField id='showRor' name='showRor' value="${showRor}"/>
<g:hiddenField id='showEbgm' name='showEbgm' value="${showEbgm}"/>
<g:hiddenField id='showDss' name='showDss' value="${showDss}"/>
<g:hiddenField id='listDateRange' name='listDateRange' value="${listDr}"/>
<g:hiddenField id='isCaseSeriesAccess' name="isCaseSeriesAccess" value="${isCaseSeriesAccess}"/>
<g:render template="/includes/modals/save_view_modal" model="[viewInstance       : viewInstance, isShareFilterViewAllowed: isShareFilterViewAllowed,
                                                              isViewUpdateAllowed: isViewUpdateAllowed]"/>
<g:render template="/includes/modals/alert_tag_modal"/>
<g:render template="/includes/modals/common_tag_modal"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'quantitativeOnDemandFields', appType: appType]"/>

<g:render template="/includes/modals/case_series_modal" />
<g:render template="/includes/modals/stratified_scores_modal"/>
<g:render template="/includes/modals/add_alert_to_topic" modal="[strategyList:strategyList]"/>
<g:render template="/includes/modals/downloadReportDatePickerModal"/>
<g:render template="/includes/modals/show_attachment_modal"/>
<g:render template="/advancedFilters/create_advanced_filters_modal" model="[fieldInfo: fieldList, appType: currentScreenType, isShareFilterViewAllowed: isShareFilterViewAllowed, miningVariable: miningVariable]"/>
<g:render template="/advancedFilters/includes/copyPasteFilterModal"/>
</body>
