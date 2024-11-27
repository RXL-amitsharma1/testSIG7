<%@ page contentType="text/html;charset=UTF-8" import="grails.converters.JSON;com.rxlogix.Constants; com.rxlogix.signal.Justification; com.rxlogix.enums.ReportFormat; grails.plugin.springsecurity.SpringSecurityUtils; grails.util.Holders;com.rxlogix.Constants;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.title.quantitative.details"/></title>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/dataTables.fixedColumns.min.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/caseForm.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:javascript src="purify/purify.min.js" />

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


    <g:javascript>
        var abc = [];
        abc = "${dssDateRange}";
        var dssDateRange = "${dssDateRange}";
        var selectedDatasource="${selectedDatasource}";
        var callingScreen = "${callingScreen}";
        var apptype = "${appType}";
        var isJaderAvailable = "${isJaderAvailable}"
        var saveCategoryAccess = "${saveCategoryAccess}";
        var hasReviewerAccess = ${hasAggReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
        var buttonClass = "${buttonClass}";
        var isViewUpdateAllowed = "${isViewUpdateAllowed}";
        var groupBySmq = "${groupBySmq}";
        var dr = "${dr}";
        var isArchived = "${isArchived}";
        var isCaseSeries = "${false}";
        var prevColumnsEvdas = JSON.parse('${prevColumnsJson}');
        var prevColCount = "${prevColCount}";
        var listDateRange = "${listDr}";
        var frequencyName = "${freqNames}";
        var executedConfigId = "${executedConfigId}";
        var dashboardFilter = "${dashboardFilter}";
        var isFLagEnabled = "false";
        var viewId = "${viewId}";
        var alertType = "${alertType}"
        var listConfigUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'listByExecutedConfig', params: [id: executedConfigId, callingScreen: callingScreen, cumulative: cumulative, adhocRun: false, dashboardFilter: dashboardFilter, tagName: tagName, isArchived: isArchived, viewId: viewId, alertType: alertType])}";
        var pvrIntegrate = "${grailsApplication.config.pvreports.url ? true : false}";
        var privateEnabled = "${grailsApplication.config.categories.feature.private.enabled ? true : false}"
        var nonConfiguredEnabled = "${grailsApplication.config.categories.feature.nonConfigured.enabled ? true : false}"
        var productEventHistoryUrl = "${createLink(controller: "productEventHistory", action: 'listProductEventHistory')}";
        var updateJustificationUrl = "${createLink(controller: "productEventHistory", action: 'updateJustification')}";
        var template_list_url = "${createLink(controller: 'template', action: 'index')}";
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var evdasCaseDetailUrl = "${createLink(controller: "caseInfo", action: 'evdasCaseDetail')}";
        var fetchDrillDownDataUrl = "${createLink(controller: "evdasAlert", action: "fetchCaseDrillDownData")}";
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}";
        var changePriorityUrl = "${createLink(controller: "aggregateCaseAlert", action: 'changePriorityOfAlert')}";
        var generateCaseSeriesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'generateCaseSeries')}";
        var saveCasesUrl = "${createLink(controller: 'singleCaseAlert', action: 'saveCaseSeries')}";
        var topicUrl = "${createLink(controller: "topic", action: 'addAlertToTopic', params: [id: executedConfigId])}";
        var dssNetworkUrl = "${dssNetworkUrl}";
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}";
        var statComparisonUrl = "${createLink(controller: 'statisticalComparison', action: 'showComparison', params: [configId: executedConfigId, appName: "Aggregate Case Alert", callingScreen: callingScreen])}";
        var fetchTopicsUrl = "${createLink(controller: 'topic', action: 'fetchTopicNames')}";
        var showTrendUrl = "${createLink(controller: "aggregateCaseAlert", action: "showTrendAnalysis")}";
        var fetchStratifiedScoresUrl = "${createLink(controller: "aggregateCaseAlert", action: "fetchStratifiedScores", params: [isArchived: isArchived])}";
        var dateRangeListUrl = "${createLink(controller: "aggregateCaseAlert", action: "fetchDateRangeList")}";
        var saveViewUrl = "${createLink(controller: 'viewInstance', action: 'saveView')}";
        var updateViewUrl = "${createLink(controller: 'viewInstance', action: 'updateView')}";
        var deleteViewUrl = "${createLink(controller: 'viewInstance', action: 'deleteView')}";
        var fetchTagsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'alertTagDetails', params: [isArchived: isArchived])}";
        var fillCommonTagUrl = "${createLink(controller: 'commonTag', action: 'getQuanAlertCategories')}";
        var saveCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'saveAlertCategories')}";
        var fetchCommonTagsUrl = "${createLink(controller: 'commonTag', action: 'commonTagDetails')}";
        var saveTagUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'saveAlertTags', params: [isArchived: isArchived])}";
        var tagDetailsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        var detailsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
        var assignToGroupUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'changeAssignedToGroup')}";
        var discardTempChangesUrl = "${createLink(controller: 'viewInstance', action: 'discardTempChanges', params: ['viewInstance.id': viewId])}";
        var changeDispositionUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'changeDisposition', params: [callingScreen: callingScreen])}";
        var revertDispositionUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'revertDisposition', params: [callingScreen: callingScreen])}"
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var dispositionData = JSON.parse('${dispositionData}')
        var availableSignalNameList = JSON.parse('${availableSignals.collect { it.name } as JSON}');
        var forceJustification = ${forceJustification};
        var allowedProductsAsSafetyLead = "${allowedProductsAsSafetyLead}".split(",");
        var isProductSecurity = "${isProductSecurity}";
        var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['configId':configId,'viewInstance.id': viewId, 'selectedDatasource': selectedDatasource, callingScreen: callingScreen])}";
        var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
        var signalDetailUrl = "${createLink(controller: 'validatedSignal', action: 'details')}";
        var archivedAlertUrl = "${createLink(controller: "aggregateCaseAlert", action: 'archivedAlert', params: [id: executedConfigId])}";
        var aggregateDetailsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        var fetchCommentUrl = "${createLink(controller: 'commentTemplate', action: 'createCommentFromTemplate')}"
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var possibleValuesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchPossibleValues', params: [executedConfigId: executedConfigId])}";
        var allFieldsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchAllFieldValues', params: [executedConfigId: executedConfigId, callingScreen: callingScreen])}";
        var selectAutoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxAdvancedFilterSearch', params: [executedConfigId: executedConfigId])}";
        var fetchAdvFilterUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterNameAjax')}";
        var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
        var attachCaseListingUrl = "${createLink(controller: 'evdasAlert', action: 'attachCaseListingFile')}";
        var fetchUsersUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxUserSearch')}";
        var fetchAllTagsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'alertTagDetails')}";
        var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
        var changeAlertLevelDispositionUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'changeAlertLevelDisposition')}";
        var alertActivitiesUrl = "${createLink(controller: 'activity', action: 'listByExeConfig')}";
        var reviewCompletedDispostionList = JSON.parse('${reviewCompletedDispostionList}');
        var filterIndexList = JSON.parse('${filterIndex}');
        var filterIndexMap = JSON.parse('${filterIndexMap}');
        var eventDetailsUrl = "${createLink(controller: "eventInfo", action: "eventDetail")}";
        var signalAccessUrl
        var configId = ${configId};
        var isFaersEnabled = ${isFaersEnabled};
        var isVaersEnabled = ${isVaersEnabled};
        var isVigibaseEnabled = ${isVigibaseEnabled};
        var isJaderEnabled = ${isJaderEnabled};
        var isEvdasEnabled = ${isEvdasEnabled};
        var showFaersEbgm = ${showEbgmFaers};
        var showFaersPrr = ${showPrrFaers};
        var showFaersRor = ${showRorFaers};
        var showVaersEbgm = ${showEbgmVaers};
        var showVigibaseEbgm = ${showEbgmVigibase};
        var showJaderEbgm = ${showEbgmJader};
        var jaderColumnList = "${jaderColumnList as grails.converters.JSON}";
        var showVaersPrr = ${showPrrVaers};
        var showVaersRor = ${showRorVaers};
        var allFourEnabled = ${allFourEnabled};
        var allThreeEnabled = ${allThreeEnabled};
        var anyTwoEnabled = ${anyTwoEnabled};
        var anyOneEnabled = ${anyOneEnabled};
        var dssHistoryUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'getDssHistoryDetails', params: [isArchived: isArchived])}";
        var dssRationaleUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'getRationaleDetails', params: [isArchived: isArchived])}";
        var showVigibasePrr = ${showPrrVigibase};
        var showVigibaseRor = ${showRorVigibase};
        var showJaderPrr = ${showPrrJader};
        var showJaderRor = ${showRorJader};
        var dataAnalysisLabel="${message(code: 'app.label.dataAnalysis')}";
        var spotfireEnabled="${Holders.config.signal.spotfire.enabled}";
        var analysisStatusJson=JSON.parse('${analysisStatusJson}');
        var isTempViewSelected = ${isTempViewSelected};
        <g:if test="${Holders.config.validatedSignal.shareWith.enabled}">
            signalAccessUrl = "${createLink(controller: 'validatedSignal', action: 'isSignalAccessible')}"
        </g:if>
        var customAnalysisThirdPartyUrl = "${Holders.config.custom.thirdParty.analysis.url}";
        var customAnalysisLabel = "${Holders.config.custom.thirdParty.analysis.label}";
        var isCustomAnalysisEnabed = ${Holders.config.custom.thirdParty.analysis.enabled};
        var isPriorityEnabled = ${isPriorityEnabled};
        var emergingIssuesAbbrIme = {"abbr": "${Holders.config.importantEvents.ime.abbreviation}", "label": "${Holders.config.importantEvents.ime.label}"};
        var emergingIssuesAbbrDme = {"abbr": "${Holders.config.importantEvents.dme.abbreviation}", "label": "${Holders.config.importantEvents.dme.label}"};
        var emergingIssuesAbbrSM = {"abbr": "${Holders.config.importantEvents.specialMonitoring.abbreviation}", "label": "${Holders.config.importantEvents.specialMonitoring.label}"};
        var emergingIssuesAbbrEI = {"abbr": "${Holders.config.importantEvents.stopList.abbreviation}", "label": "${Holders.config.importantEvents.stopList.label}"};
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
        var isCategoryRole = ${SpringSecurityUtils.ifAnyGranted("ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT")};
        var isCaseDetailView = "";
        var alertIdSet = new Set();
        var commonProductIdSet = new Set();
        var versionNumber = ${version};
        var currUserName = "${currUserName}"

    </g:javascript>
    <asset:javascript src="app/pvs/productEventHistory/productEventHistoryTable.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryJustification.js"/>
    <asset:javascript src="app/pvs/alerts_review/agg_case_alert_details.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <g:render template="/includes/widgets/actions/action_types"/>
    <style>
    .yadcf-filter {
        height: 24px;
        padding-top: unset;
        padding-bottom: unset;
        text-align: left;
        color: inherit;
        background-color: white;
        border-radius: 4px;
        border: 1px solid #ccc;
    }

    .hyperLinkColor {
        color: #23527c;
    }

    #commentHistoryTable td {
        font-size: 12px !important;
    }
    </style>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

<div class="container-fluid whitestrip">
    <div class="row">
        <div class="col-md-12 p2">
            <g:if test="${callingScreen == Constants.Commons.REVIEW}">
                <g:if test="${isArchived}">
                    <span class="pl8"><a href="review"><g:message code="aggregate.review.label"/> <i
                            class="fa fa-angle-right f14" aria-hidden="true"></i>
                    </a><g:message code="app.label.archived.alerts.label"/> <i class="fa fa-angle-right f14"
                                                                               aria-hidden="true"></i>
                    </span> ${name}: ${dateRange}</span>
                </g:if>
                <g:else>
                    <span class="pl8"><a href="review"><g:message code="aggregate.review.label"/> <i
                            class="fa fa-angle-right f14" aria-hidden="true"></i>
                    </a></span> ${name}: ${dateRange}</span>
                </g:else>

                <span class="pos-rel drange" style="float: none;">
                    <span tabindex="0" class="saveViewPanel grid-menu-tooltip" accesskey="s" aria-expanded="true"
                          title="">
                        <a href="${createLink(controller: 'aggregateCaseAlert', action: 'viewExecutedConfig', id: executedConfigId) ?: '#'}"
                           target="_blank"><i class="glyphicon glyphicon-info-sign themecolor"></i></a>
                        <span class="caret hidden"></span>
                    </span>
                    <g:if test="${selectedDatasource != 'faers' && selectedDatasource != 'vaers' && selectedDatasource != 'eudra' && selectedDatasource != 'vigibase'}">
                        <g:if test="${evdasDateRange || faersDateRange || vigibaseDateRange || (vaersDateRange && isPVAEnabled)}">
                            <ul class="dropdown-menu save-list mw-252" id="fearsEvedas">

                                <li class="pdr5">
                                    <g:if test="${evdasDateRange}">
                                        <span class="otherDataSourcesName">${Constants.DataSource.EVDAS.toUpperCase()}:</span>
                                        <span class="otherDataSourcesDate">${evdasDateRange}</span>
                                    </g:if>
                                    <g:if test="${vaersDateRange}">
                                        <span class="otherDataSourcesName">${Constants.DataSource.VAERS.toUpperCase()}:</span>
                                        <span class="otherDataSourcesDate">${vaersDateRange}</span>
                                    </g:if>
                                    <g:if test="${faersDateRange}">
                                        <span class="otherDataSourcesName">${Constants.DataSource.FAERS.toUpperCase()}:</span>
                                        <span class="otherDataSourcesDate">${faersDateRange}</span>
                                    </g:if>
                                    <g:if test="${vigibaseDateRange}">
                                        <span class="otherDataSourcesName">${Constants.DataSource.VIGIBASE_CAMEL_CASE}:</span>
                                        <span class="otherDataSourcesDate">${vigibaseDateRange}</span>
                                    </g:if>
                                </li>

                            </ul>
                        </g:if>
                    </g:if>

                </span>
            </g:if>
            <g:else>
                <span class="panel-title"><g:message code="app.label.agg.alert.rule"/></span>
            </g:else>
        </div>
    </div>
</div>
%{--
<g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>
--}%

<div class="pv-tab">
    <g:if test="${callingScreen == "tags"}">

        <a href="#" class="btn pv-btn-grey pull-right tag-screen-back-btn" role="button"><i
                class="fa fa-long-arrow-left"></i>  Back</a>

    </g:if>
<!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" class="active">
            <a href="#details" aria-controls="details" role="tab" data-toggle="tab" accesskey="1">Alert Details</a>
        </li>
        <li role="presentation">
            <a href="#activities" id="activity_tab" aria-controls="activities" role="tab" data-toggle="tab"
               accesskey="2"><g:message
                    code="app.label.alert.activities"/></a>
        </li>
        <g:if test="${isLatest}">
            <li role="presentation">
                <a href="#archivedAlerts" aria-controls="archivedAlerts" role="tab" data-toggle="tab"
                   accesskey="3"><g:message
                        code="app.label.archived.alerts"/></a>
            </li>
        </g:if>
    </ul>


    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="details">
            <g:render template="includes/details_tab"
                      model="[id                  : executedConfigId, name: name, reportUrl: reportUrl,
                              analysisFileUrl     : analysisFileUrl, reportName: reportName,
                              callingScreen       : callingScreen, labelConfig: labelConfig, labelConfigKeyId:labelConfigKeyId,hyperlinkConfiguration:hyperlinkConfiguration,labelConfigCopy: labelConfigCopy, labelConfigJson: labelConfigJson, labelConfigNew: labelConfigNew, labelConfigJson: labelConfigJson, groupBySmq: groupBySmq, isPVAEnabled: isPVAEnabled,
                              listDateRange       : listDr, showPrr: showPrr, showRor: showRor, showPrrVaers: showPrrVaers, showPrrVigibase: showPrrVigibase,showPrrJader: showPrrJader,
                              prevColumns         : prevColumns, showEbgm: showEbgm, showEbgmVaers: showEbgmVaers, showEbgmVigibase: showEbgmVigibase,showEbgmJader: showEbgmJader,jaderColumnList:jaderColumnList,isJaderAvailable:isJaderAvailable,
                              isRor               : isRor, showPrrFaers: showPrrFaers, showRorFaers: showRorFaers, showEbgmFaers: showEbgmFaers,
                              alertDispositionList: alertDispositionList, showDss: showDss, showRorVaers: showRorVaers, showRorVigibase: showRorVigibase,showRorJader: showRorJader,
                              cumulative: cumulative, freqNames: freqNames, dateRange: dateRange,
                              dr: dr,  viewId: viewId, prevColCount: prevColCount, isArchived: isArchived,
                              evdasEndDate: evdasEndDate,faersEndDate:faersEndDate, selectedDatasource: selectedDatasource, labelCondition: labelCondition,
                              faersDateRange: faersDateRange, evdasDateRange: evdasDateRange, prevVaersDate: prevVaersDate, prevVigibaseDate: prevVigibaseDate, prevFaersDate: prevFaersDate, prevEvdasDate: prevEvdasDate, spotFireFiles:spotFireFiles,
                              isSaftyDb:isSaftyDb, showDssScores: showDssScores, isAutoProposed: isAutoProposed, isVaersEnabled: isVaersEnabled, isVigibaseEnabled: isVigibaseEnabled,isJaderEnabled:isJaderEnabled, subGroupsColumnList: subGroupsColumnList,
                              isEvdasEnabled: isEvdasEnabled, isFaersEnabled: isFaersEnabled, faersSubGroupsColumnList: faersSubGroupsColumnList,aggregateHelpMap:aggregateHelpMap,relativeSubGroupMap:relativeSubGroupMap,prrRorSubGroupMap:prrRorSubGroupMap]"/>
        </div>

        <div id="activities" class="tab-pane fade" role="tabpanel">
            <g:render template="/includes/widgets/activities_tab"
                      model="[alertId: executedConfigId, name: name, type: 'Aggregate Case Alert', callingScreen: callingScreen]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="archivedAlerts">
            <g:render template="/singleCaseAlert/includes/archivedAlerts"
                      model="[id: executedConfigId, name: name, type: 'Aggregate Case Alert']"/>
        </div>
    </div>
</div>
<input type="hidden" id="businessRules" value="${aggregateRules}"/>
<input type="hidden" id="cumulative" value="${cumulative}"/>
<input type="hidden" id="groupBySmq" value="${groupBySmq}"/>
<input type="hidden" id="dssUrl" value="${dssUrl}"/>
<input type="hidden" id="filterMap" value="${filterMap}"/>
<input type="hidden" id="columnIndex" value="${columnIndex}"/>
<input type="hidden" id="sortedColumn" value="${sortedColumn}"/>
<input type="hidden" id="advancedFilterView" value="${advancedFilterView}"/>
<input type="hidden" id="isFaers" value="${isFaers}"/>
<input type="hidden" id="isVaers" value="${isVaers}"/>
<input type="hidden" id="isVigibase" value="${isVigibase}"/>
<input type="hidden" id="isJader" value="${isJader}"/>
<g:hiddenField id='isArchived' name='isArchived' value="${isArchived}"/>
<input type="hidden" id="symptomsComanifestationColumnName"
       value="${Holders.config.spotfire.symptomsComanifestationColumnName}"/>
<input type="hidden" id="symptomsComanifestation" value="${Holders.config.spotfire.symptomsComanifestation}"/>

<g:render template="/includes/widgets/show_evdas_charts_modal"/>
<g:render template="/includes/modals/actionCreateModal"
          model="[id: executedConfigId, appType: 'Aggregate Case Alert', userList: userList, actionConfigList: actionConfigList, isArchived: isArchived]"/>
<g:render template="/includes/modals/action_list_modal"/>
<g:render template="/includes/modals/product_event_history_modal" model="[configId: configId]"/>
<g:render template="/includes/modals/alert_comment_modal" model="[isArchived: isArchived]"/>
<g:render template="/includes/modals/alert_revert_justification_modal"/>
<g:render template="/includes/modals/evdas_attachment_modal"/>
<g:render template="/includes/modals/message_box"/>
<g:hiddenField id='showDssScores' name='showDssScores' value="${showDssScores}"/>
<g:hiddenField id='showPrr' name='showPrr' value="${showPrr}"/>
<g:hiddenField id='showRor' name='showRor' value="${showRor}"/>
<g:hiddenField id='showEbgm' name='showEbgm' value="${showEbgm}"/>
<g:hiddenField id='showDss' name='showDss' value="${showDss}"/>
<g:hiddenField id='showStratification' name='showStratification' value="${showStratification}"/>
<g:hiddenField id='listDateRange' name='listDateRange' value="${listDr}"/>
<g:hiddenField id='isCaseSeriesAccess' name="isCaseSeriesAccess" value="${isCaseSeriesAccess}"/>
<g:hiddenField id='hasSignalManagementAccess' name="hasSignalManagementAccess"
               value="${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_SIGNAL_MANAGEMENT_CONFIGURATION, ROLE_SIGNAL_MANAGEMENT_REVIEWER, ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL")}"/>

<g:render template="/includes/modals/case_series_modal"/>
<g:render template="/includes/modals/stratified_scores_modal"/>
<g:render template="/includes/modals/downloadReportDatePickerModal"/>
<g:render template="/includes/modals/show_attachment_modal"/>
<g:render template="/includes/modals/save_view_modal"
          model="[viewInstance       : viewInstance, isShareFilterViewAllowed: isShareFilterViewAllowed,
                  isViewUpdateAllowed: isViewUpdateAllowed]"/>
<g:render template="/includes/modals/alert_tag_modal"/>
<g:render template="/includes/modals/common_tag_modal"/>
<g:render template="/template/fieldConfiguration"
          model="[fieldConfigurationBarId: 'quantitativeFields', appType: appType,isJaderAvailable:isJaderAvailable]"/>
<g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
<g:render template="/includes/popover/dispositionJustificationSelect"/>
<g:render template="/includes/popover/dispositionSignalSelect"
          model="[availableSignals: availableSignals, forceJustification: forceJustification]"/>
<g:render template="/includes/popover/priorityJustificationSelect"
          model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
<g:render template="/includes/popover/prioritySelect"
          model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>
<g:render template="/advancedFilters/create_advanced_filters_modal"
          model="[fieldInfo: fieldList, isShareFilterViewAllowed: isShareFilterViewAllowed,appType: isJader ? 'Aggregate Case Alert - JADER' : appType]"/>
<g:render template="/advancedFilters/includes/copyPasteFilterModal"/>
</body>
</html>
