<%@ page import="grails.converters.JSON; com.rxlogix.Constants; com.rxlogix.enums.ReportFormat;grails.util.Holders" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.eventInfo.eventDetails.title"/></title>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>

    <asset:javascript src="purify/purify.min.js" />
    <asset:javascript src="app/pvs/alerts_review/event_detail.js"/>
    <asset:stylesheet src="text_diff.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:stylesheet src="jsTree/style.min.css"/>
    <asset:javascript src="vendorUi/jsTree/jstree.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>
    <asset:javascript src="highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/themes/grid-rx.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/pvs/priority/priority-change.js"/>
    <asset:javascript src="app/pvs/productEventHistory/productEventHistoryTable.js"/>
    <asset:javascript src="app/pvs/evdasHistory/evdasHistoryTable.js"/>
    <asset:javascript src="app/pvs/caseHistory/caseHistoryJustification.js"/>
    <asset:javascript src="app/pvs/eventDetail/prev_counts_and_scores.js"/>
    <asset:javascript src="app/pvs/eventDetail/history_from_other_source.js"/>


    <g:javascript>

        var getAttachmentUrl = '';
        var deleteAttachmentUrl = '';
        var getUploadUrl = '';
        var changePriorityUrl = '';
        var changeAssignedToUrl = '';
        var changeDispositionUrl = '';
        var updateJustificationUrl = '';
        var dataSourceLabel = "${Holders.config.signal.dataSource.safety.name}";
        <g:if test="${alertDetailMap.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
            getAttachmentUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchAttachment', params: [alertId: alertDetailMap.alertId, isArchived: isArchived])}";
            getUploadUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'upload', params: [isArchived: isArchived])}";
            changePriorityUrl = "${createLink(controller: "aggregateCaseAlert", action: 'changePriorityOfAlert')}";
            changeAssignedToUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'changeAssignedToGroup')}";
            changeDispositionUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'changeDisposition')}";
            deleteAttachmentUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'deleteAttachment', params: [alertId: alertDetailMap.alertId])}";
            updateJustificationUrl = "${createLink(controller: "productEventHistory", action: 'updateJustification')}";
    </g:if>
        <g:else>
            getAttachmentUrl = "${createLink(controller: 'evdasAlert', action: 'fetchAttachment', params: [alertId: alertDetailMap.alertId, isArchived: isArchived])}";
            getUploadUrl = "${createLink(controller: 'evdasAlert', action: 'upload', params: [isArchived: isArchived])}";
            changePriorityUrl = "${createLink(controller: "evdasAlert", action: 'changePriorityOfAlert')}";
            changeAssignedToUrl = "${createLink(controller: 'evdasAlert', action: 'changeAssignedToGroup')}";
            changeDispositionUrl = "${createLink(controller: 'evdasAlert', action: 'changeDisposition')}";
            deleteAttachmentUrl = "${createLink(controller: 'evdasAlert', action: 'deleteAttachment', params: [alertId: alertDetailMap.alertId])}";
            updateJustificationUrl = "${createLink(controller: "evdasHistory", action: 'updateJustification')}";
        </g:else>

        var isArchived = "${isArchived}";
        var hasReviewerAccess = ${hasReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};
        var option = new Option("${alertDetailMap.assignedTo?.fullName}", "${alertDetailMap.assignedTo.id}", true, true);
        var alertId = "${alertDetailMap.alertId}";
        var alertType = "${alertDetailMap.appType}";
        var productEventHistoryUrl = "${createLink(controller: "productEventHistory", action: 'listProductEventHistory')}";
        var evdasHistoryUrl = "${createLink(controller: "evdasHistory", action: 'listEvdasHistory')}";
        var detailsUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        var caseDetailAssessmentUrl = "${createLink(controller: 'caseInfo', action: 'fetchAssessmentDetailData')}";
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var forceJustification = ${forceJustification};
        var showChartsUrl = "${createLink(controller: "eventInfo", action: 'showCharts', params: [isArchived: isArchived])}";
        var showComparisionChartsUrl = "${createLink(controller: "statisticalComparison", action: 'showComparisionCharts')}";
        var prevScoresUrl = "${createLink(controller: 'eventInfo', action: 'listPreviousCounts', params: [isArchived: isArchived])}";
        var fetchDrillDownDataUrl = "${createLink(controller: "evdasAlert", action: "fetchCaseDrillDownData")}";
        var otherSourcesHistoryUrl = "${createLink(controller: "eventInfo", action: "listHistoryOfOtherSources", params: [isArchived: isArchived])}";
        var attachmentDownloadUrl = "${createLink(controller: "attachmentable", action: "download")}";
        var availableSignalNameList = JSON.parse('${availableSignals.collect{it.name} as JSON}');
        var isPriorityEnabled = ${isPriorityEnabled};
    </g:javascript>

    <style>
    .modal-lg {
        width: 1250px; /* New width for large modal */
    }

    .odd {
        background-color: #EFEFEF;
        padding: 0;
    }

    .treeview-content {
        margin-top: 3px !important;
    }

    .pv-fullcase-treeview {
        top: 75px !important;
    }

    #attachment-table.dataTable td,#attachment-table.dataTable th{
        word-break: break-all;
    }

    </style>
    <g:render template="/includes/widgets/actions/action_types"/>

    <g:javascript>
        function camel2title(camelCase) {
            // no side-effects
            return camelCase
                    // inject space before the upper case letters
                    .replace(/([A-Z])/g, function (match) {
                return " " + match;
            })
                    // replace first char with upper case
                    .replace(/^./, function (match) {
                return match.toUpperCase();
            });
        }

        $(document).ready(function () {
            $.ajax({
                url: showChartsUrl + '&alertId=' + alertId+'&alertType='+alertType,
                success: function (result) {
                    var dateRange = result.xAxisTitle;
                    if(dateRange.length == 0){
                        if(result.xAxisTitle_ev.length !== 0){
                            dateRange = result.xAxisTitle_ev;
                        }else{
                            dateRange = result.xAxisTitle_faers;
                        }
                    }
                    evdas_chart("#evdas-count-by-status", dateRange,
                            [{
                                name: ($.i18n._('eventDetail.newCountPva')) + "(" + dataSourceLabel + ")",
                                data: result.newCount_pva
                            },{
                                name: $.i18n._('eventDetail.cummCountPva') + "(" + dataSourceLabel + ")",
                                data: result.cummCount_pva
                            },{
                                name: $.i18n._('eventDetail.newFatalPva') + "(" + dataSourceLabel + ")",
                                data: result.newFatal_pva
                            },{
                                name: $.i18n._('eventDetail.cummFatalPva') + "(" + dataSourceLabel + ")",
                                data: result.cummFatal_pva
                            },{
                                name: $.i18n._('eventDetail.newEvpmEv'),
                                data: result.newEvpm_ev
                            },{
                                name: $.i18n._('eventDetail.totalEvpmEv'),
                                data: result.totalEvpm_ev
                            },{
                                name: $.i18n._('eventDetail.newFatalEv'),
                                data: result.newFatal_ev
                            },{
                                name: $.i18n._('eventDetail.totalFatalEv'),
                                data: result.totalFatal_ev
                            },{
                                name: $.i18n._('eventDetail.newPaedEv'),
                                data: result.newPaed_ev
                            },{
                                name: $.i18n._('eventDetail.totalPaedEv'),
                                data: result.totalPaed_ev
                            },{
                                name: $.i18n._('eventDetail.sdrPaedEv'),
                                data: result.sdrPaed_ev
                            },{
                                name: $.i18n._('eventDetail.newCountsFaers'),
                                data: result.newCounts_faers
                            },{
                                name: $.i18n._('eventDetail.cummCountsFaers'),
                                data: result.cummCounts_faers
                            }],
                            "Counts", 'Counts'
                    );

                    evdas_chart("#evdas-scores-by-status", dateRange,
                            [{
                                name: $.i18n._('eventDetail.prrPva') + "(" + dataSourceLabel + ")",
                                data: result.prr_pva
                            }, {
                                name: $.i18n._('eventDetail.rorPva') + "(" + dataSourceLabel + ")",
                                data: result.ror_pva
                            },{
                                name: $.i18n._('eventDetail.rorAllEv'),
                                data: result.rorAll_ev
                            },{
                                name: $.i18n._('eventDetail.eb05Faers'),
                                data: result.eb05_faers
                            },{
                                name: $.i18n._('eventDetail.eb95Faers'),
                                data: result.eb95_faers
                            }],
                            "Scores", 'Scores'
                    );

                    var statsCountTable = [];

                    var evEnabled = result.evEnabled
                    var countLength = 0;

                    for (var index = 0; index < countLength; index++) {
                        var obj = {
                            "xAxisTitle": dateRange[index],
                            "sponCount": result.sponCount[index] ? result.sponCount[index] : 0,
                            "fatalCount": result.fatalCount[index] ? result.fatalCount[index] : 0
                        };

                        if (evEnabled) {
                            obj.newEV = result.newEv[index];
                            obj.evSerious = result.seriousEv[index];
                            obj.evFatal = result.evFatalCount[index];
                        }
                        statsCountTable.push(obj);
                    }
                    var trend_data_content = signal.utils.render('statsCountTable', {trendData: statsCountTable, evEnabled:evEnabled});
                    $("#trend-div").html(trend_data_content);

                    var statsScoresTable = [];
                    for (var index = 0; index < countLength; index++) {
                        var obj = {
                            "xAxisTitle": dateRange[index],
                            "prrValue": result.prrValue[index] ? result.prrValue[index] : 0,
                            "rorValue": result.rorValue[index] ? result.rorValue[index] : 0 ,
                            "ebgmValue": result.ebgmValue[index] ? result.ebgmValue[index] : 0
                        };

                        if(evEnabled){
                            obj.rorEv = result.rorEv[index];
                        }
                        statsScoresTable.push(obj);
                    }
                    var score_data_content = signal.utils.render('statsScoresTable', {trendData: statsScoresTable, evEnabled:evEnabled});
                    $("#scores-div").html(score_data_content);

                }

            })
        });

        var evdas_chart = function (chartId, categories, series, yAxisTitle, chartTitle) {
            $(chartId).highcharts({
                chart: {
                    type: 'line'
                },
                xAxis: {
                    categories: categories,
                },
                yAxis: {
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }],
                    title: {
                        text: yAxisTitle
                    }
                },
                series: series,
                credits: {
                    enabled: false
                },
                title: {
                    text: chartTitle
                }
            });
        }

    </g:javascript>
</head>

<body>
<g:hiddenField name="keysJson" value="${dataKeys}"/>

<div class="info-widget">

    <div class="pv-fullcase-treeview">
        <div id="eventDetailTree" class="demo"></div>
    </div>


    <div class="treeview-content">
        <rx:containerCollapsible class="section m-b-10" title="Workflow Management">
            <div class="row">
                <div>
                    <span class="box-100">
                        <input class="alert-check-box" id="alertId" data-id="${alertDetailMap.alertId}" type="hidden"
                               value="${alertDetailMap.alertId}"/>
                        <span data-field="masterPrefTermAll" data-id="${alertDetailMap.pt}"></span>
                        <span data-field='assignedTo' data-info='row'
                              data-current-user-id="${alertDetailMap.assignedTo.id}"
                              data-current-user="${alertDetailMap.assignedTo?.fullName}"></span>
                    </span>
                </div>

                <div class="col-md-2 currentDisposition">
                    <label class="box-100">Current Disposition</label>
                    <h5 id = "eventDisposition">${alertDetailMap.disposition}</h5>
                </div>

                <div class="col-md-5">
                    <label>Disposition</label>
                    <span class="disposition">
                        <ul class="list-inline icon-list"
                            data-current-disposition="${alertDetailMap.disposition}">
                            <g:if test="${alertDetailMap.isValidationStateAchieved}">
                                <li>
                                    <a data-target="${!alertDetailMap.isValidationStateAchieved?(hasReviewerAccess?'#dispositionSignalPopover':''):
                                            (hasSignalCreationAccessAccess && hasReviewerAccess?'#dispositionSignalPopover':'')}" role="button"
                                       class="changeDisposition"
                                       data-validated-confirmed="${alertDetailMap.isValidationStateAchieved}"
                                       data-disposition-id="${alertDetailMap.dispositionId}" data-toggle="modal-popover"
                                       data-placement="bottom"
                                       title="${alertDetailMap.disposition}"><span>Add Signal</span>
                                    </a>
                                </li>
                            </g:if>
                            <g:else>
                                <g:each in="${currentDispositionOptions}">
                                    <g:if test="${it.validatedConfirmed}">
                                        <li>
                                            <a data-target="${!it.validatedConfirmed?(hasReviewerAccess?'#dispositionSignalPopover':''):
                                                    (hasSignalCreationAccessAccess && hasReviewerAccess?'#dispositionSignalPopover':'')}" role="button"
                                               class="changeDisposition"
                                               data-validated-confirmed="${it.validatedConfirmed}"
                                               data-auth-required="${it.isApprovalRequired}"
                                               data-disposition-id="${it.id}" data-toggle="modal-popover"
                                               data-placement="bottom" title="${it.displayName}">
                                                <i class="ico-circle"
                                                   style="background:${it.colorCode}">${it.abbreviation}</i>
                                            </a>
                                        </li>
                                    </g:if>
                                    <g:else>
                                        <g:if test="${forceJustification}">
                                            <li>
                                                <a data-target="${!it.validatedConfirmed?(hasReviewerAccess?'#dispositionJustificationPopover':''):
                                                        (hasSignalCreationAccessAccess && hasReviewerAccess?'#dispositionJustificationPopover':'')}" role="button"
                                                   class="changeDisposition"
                                                   data-validated-confirmed="${it.validatedConfirmed}"
                                                   data-auth-required="${it.isApprovalRequired}"
                                                   data-disposition-id="${it.id}"
                                                   data-toggle="modal-popover"
                                                   data-placement="bottom"
                                                   title="${it.displayName}">
                                                    <i class="ico-circle"
                                                       style="background:${it.colorCode}">${it.abbreviation}</i>
                                                </a>
                                            </li>
                                        </g:if>
                                        <g:else>
                                            <li>
                                                <a href="#" role="button" class="changeDisposition"
                                                   data-validated-confirmed="${it.validatedConfirmed}"
                                                   data-auth-required="${it.isApprovalRequired}"
                                                   data-disposition-id="${it.id}"
                                                   title="${it.displayName}">
                                                    <i class="ico-circle"
                                                       style="background:${it.colorCode}">${it.abbreviation}</i>
                                                </a>
                                            </li>
                                        </g:else>

                                    </g:else>
                                </g:each>
                            </g:else>
                        </ul>
                    </span>
                </div>

                <g:render template="/includes/widgets/assigned_to_Select" model="[isAction:true]"/>
            </div>
        </rx:containerCollapsible>
        <rx:containerCollapsible title="Event Detail">
            <g:if test="${data}">
                <rx:containerCollapsible title="Event Information">
                    <div class="scroll-body">
                        <table class="table no-border m-t-0 m-b-0">
                            <thead>
                            <tr>
                                <th><g:message code="app.label.event.product"/></th>
                                <th><g:message code="app.label.event.soc"/></th>
                            <g:if test="${data.eventInformation.first().soc == 'SMQ'}">
                                <th><g:message code="app.label.event.smq"/></th>
                            </g:if>
                            <g:else>
                                <th><g:message code="app.label.event.pt"/></th>
                            </g:else>

                                <th><g:message code="app.label.event.listed"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${data.eventInformation}" var="obj" status="column">
                                <g:if test="${column % 2 == 0}">
                                    <tr class="odd">
                                </g:if>
                                <g:else>
                                    <tr class="even">
                                </g:else>
                                <td>${obj.productName ?: '-'}</td>
                                <td>${obj.soc ? obj.soc.equals(Constants.Commons.UNDEFINED) ? 'N/A':obj.soc :'N/A' }</td>
                                <td>${obj.pt ?: '-'}</td>
                                <td>${obj.listed ?: '-'}</td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                </rx:containerCollapsible>
            </g:if>
            <g:else>
                <div class="m-b-10">
                    <span><g:message code="app.case.data.not.found"/></span>
                </div>
            </g:else>
        </rx:containerCollapsible>
        <rx:containerCollapsible class="section m-b-10" title="Attachments">
            <div class="col-sm-12">
                <a class="btn btn-primary ${buttonClass}" href="#attachmentCaseDetailModal" data-toggle="modal">Add File</a>

                <div class="attachments">
                    <table id="attachment-table" class="row-border hover  no-footer" width="100%">
                        <thead>
                        <tr>
                            <th><g:message code="caseDetails.link"/></th>
                            <th><g:message code="caseDetails.description"/></th>
                            <th><g:message code="caseDetails.timestamp"/></th>
                            <th><g:message code="caseDetails.modified.by"/></th>
                            <th class="sorting_disabled"><g:message code="caseDetails.action"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        </tbody>
                    </table>
                </div>
            </div>
        </rx:containerCollapsible>
        <rx:containerCollapsible class="section m-b-10" title="Comments">

            <div class="comment">
                <div id="alert-comment-container">
                    <g:textArea maxlength="4000" name="commentbox" id="commentbox"
                                placeholder="Please enter your comment here." class="form-control"/>
                </div>

                <span class="createdBy"></span>

                <button type="button" class="btn btn-primary add-comments pull-right m-t-15 ${buttonClass}">
                    <g:message code="default.button.add.label"/>
                </button>
                <br>
                <br>
                <span class="hide" id="executedConfigId">${alertDetailMap.execConfigId}</span>
                <span id="application"
                      class="hide">${alertDetailMap.appType}</span>
                <input type="hidden" id="commentId"/>
                <span class="hide" id="assignedTo">${alertDetailMap.assignedTo.id}</span>
                <span class="hide" id="productName">${alertDetailMap.productName}</span>
                <span class="hide" id="eventName">${alertDetailMap.pt}</span>
                <span class="hide" id="configId">${alertDetailMap.configId}</span>
                <span class="hide" id="ptCode">${alertDetailMap.ptCode}</span>
                <span class="hide" id="productId">${alertDetailMap.productId}</span>
                <span class="hide" id="alertId">${alertDetailMap.alertId}</span>
            </div>

        </rx:containerCollapsible>
        <rx:containerCollapsible class="section m-b-10" title="Actions">

            <div id="action-list-conainter" class="list">
                <table id="action-table" class="row-border hover dataTable no-footer" style="width: 100%">
                    <thead>
                    <tr>
                        <th><g:message code="app.action.id" default="Type"/></th>
                        <th><g:message code="app.action.list.type.label" default="Type"/></th>
                        <th><g:message code="app.label.action" default="Action"/></th>
                        <th><g:message code="app.action.list.action.details.label" default="Details"/></th>
                        <th><g:message code="app.label.due.date" default="Due Date"/></th>
                        <th><g:message code="app.label.assigned.to" default="Assigned To"/></th>
                        <th><g:message code="app.action.list.status.label" default="Status"/></th>
                        <th><g:message code="app.action.list.completion.label" default="Completion Date"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </rx:containerCollapsible>
        <rx:containerCollapsible class="section m-b-10" title="History">
            <g:if test="${alertDetailMap.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                <g:render template="quantitative_alert_history"/>
            </g:if>
            <g:else>
                <g:render template="evdas_alert_history"/>
            </g:else>
        </rx:containerCollapsible>
        <rx:containerCollapsible class="section m-b-10" title="${message(code: 'app.label.event.node.history.other.sources', default: 'Review History from Other Sources')}">
                <table id="reviewHistoryOtherSourceTable" class="row-border hover dataTable no-footer" style="width: 100%">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.event.data.source"/></th>
                        <th><g:message code="app.label.event.alert.name"/></th>
                        <th><g:message code="app.label.event.disposition"/></th>
                        <th><g:message code="app.label.event.justification"/></th>
                        <th><g:message code="app.label.event.performed.by"/></th>
                        <th><g:message code="app.label.event.date"/></th>
                    </tr>
                    </thead>
                </table>
        </rx:containerCollapsible>
    </div>

</div>

%{--Refractor this id and naming later--}%
<g:hiddenField name="productNameE" id="productNameE" value="${alertDetailMap.productName}"/>
<g:hiddenField name="assignedToE" id="assignedToE" value="${alertDetailMap.assignedTo.id}"/>
<g:hiddenField name="eventNameE" id="eventNameE" value="${alertDetailMap.pt}"/>
<g:hiddenField name="configIdE" id="configIdE" value="${alertDetailMap.configId}"/>
<g:hiddenField name="executedConfigIdE" id="executedConfigIdE" value="${alertDetailMap.execConfigId}"/>
<g:hiddenField name="ptCodeE" id="ptCodeE" value="${alertDetailMap.ptCode}"/>
<g:hiddenField name="productIdE" id="productIdE" value="${alertDetailMap.productId}"/>
<g:hiddenField name="appNameE" id="appNameE" value="${alertDetailMap.appType}"/>
<g:hiddenField id='showPrr' name='showPrr' value="${flagsMap.showPrr}"/>
<g:hiddenField id='showRor' name='showRor' value="${flagsMap.showRor}"/>
<g:hiddenField id='showEbgm' name='showEbgm' value="${flagsMap.showEbgm}"/>
<g:hiddenField id='showFaers' name='showFaers' value="${flagsMap.showFaers}"/>
<g:hiddenField id='showEvdas' name='showEvdas' value="${flagsMap.showEvdas}"/>
<g:hiddenField id='alertId' name='alertId' value="${alertDetailMap.alertId}"/>
<g:hiddenField id="isArchived" name="isArchived" value="${isArchived}"/>

<g:render template="/includes/modals/alert_comment_modal"/>
<g:render template="/includes/modals/action_edit_modal"/>
<g:render template="/includes/modals/attachments_modal_dialog_case_detail" model="[alertId: alertDetailMap.alertId]"/>
<g:render template="/includes/modals/actionCreateModal"
          model="[alertId: alertDetailMap.alertId, id: alertDetailMap.execConfigId, appType: alertDetailMap.appType, actionConfigList: actionConfigList, isArchived: isArchived]"/>

<g:render template="/includes/popover/dispositionJustificationSelect"/>
<g:render template="/includes/popover/dispositionSignalSelect"
          model="[availableSignals: availableSignals, forceJustification: forceJustification, caseDetail: true]"/>
<g:render template="/includes/popover/priorityJustificationSelect"
          model="[availableAlertPriorityJustifications: availableAlertPriorityJustifications]"/>
<g:render template="/includes/popover/prioritySelect"
          model="[availablePriorities: availablePriorities, forceJustification: forceJustification]"/>
<g:hiddenField class="execConfigId" name="execConfigId" value="${alertDetailMap.execConfigId}"/>


<script>
    $(function () {
        var dataKeys = JSON.parse($('#keysJson').val());
        var data = [];

        $.each(dataKeys, function (i, obj) {
                data.push({
                    "text": camel2title(obj.key),
                    "state": {"opened": true},
                    "icon": false,
                    "accordionDetails": {
                        "currentAccordionName": obj.title,
                        "parentAccordionName": "node1"
                    },
                    "li_attr": {
                        "id": obj.key.replace(/([A-Z])/g, '_$1').toLowerCase()
                    }
                });
        });

        window.node = [
            {
                "text": "Workflow Management",
                "icon": false,
                "state": {"opened": true},
                "accordionDetails": {
                    "currentAccordionName": getContainerText("WORKFLOW MANAGEMENT"),
                    "parentAccordionName": null
                },
                "li_attr": {
                    "id": "workflow_management"
                }
            },
            {
                "text": "Event Detail",
                "state": {"opened": true},
                "icon": false,
                "accordionDetails": {
                    "currentAccordionName": "node1",
                    "parentAccordionName": null
                },
                "children": data,
                "li_attr": {
                    "id": "event_detail"
                }
            },
            {
                "text": "Attachments",
                "icon": false,
                "state": {"opened": true},
                "accordionDetails": {
                    "currentAccordionName": getContainerText("ATTACHMENTS"),
                    "parentAccordionName": null
                },
                "li_attr": {
                    "id": "attachments"
                }
            },
            {
                "text": "Comments",
                "icon": false,
                "state": {"opened": true},
                "accordionDetails": {
                    "currentAccordionName": getContainerText("COMMENTS"),
                    "parentAccordionName": null
                },
                "li_attr": {
                    "id": "comments"
                }
            },
            {
                "text": "Actions",
                "icon": false,
                "state": {"opened": true},
                "accordionDetails": {
                    "currentAccordionName": getContainerText("ACTIONS"),
                    "parentAccordionName": null
                },
                "li_attr": {
                    "id": "actions"
                }
            },
            {
                "text": "History",
                "icon": false,
                "state": {"opened": true},
                "accordionDetails": {
                    "currentAccordionName": getContainerText("HISTORY"),
                    "parentAccordionName": null
                },
                "li_attr": {
                    "id": "history"
                }
            },
            {
                "text": "${message(code: 'app.label.event.node.history.other.sources', default: 'Review History from Other Sources')}",
                "icon": false,
                "state": {"opened": true},
                "accordionDetails": {
                    "currentAccordionName": getContainerText("${message(code: 'app.label.event.node.history.other.sources', default: 'Review History from Other Sources')}"),
                    "parentAccordionName": null
                },
                "li_attr": {
                    "id": "review_history_from_other_sources"
                }
            }
        ];
        function getContainerText(text) {
            return text.toLowerCase().replace(/ /g, "_") + "_container";
        }
    });
</script>
</body>
</html>
