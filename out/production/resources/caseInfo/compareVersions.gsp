<%@ page import="grails.util.Holders; grails.converters.JSON; com.rxlogix.enums.ReportFormat" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.caseInfo.compare.version"/></title>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <asset:javascript src="app/pvs/alerts_review/case_diff.js"/>
    <asset:javascript src="app/pvs/alerts_review/compare_versions.js"/>
    <asset:stylesheet src="text_diff.css"/>
    <asset:stylesheet src="jsTree/style.min.css"/>
    <asset:javascript src="vendorUi/jsTree/jstree.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_key_prevent.js"/>

    <asset:javascript src="highcharts.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-more.js"/>
    <asset:javascript src="vendorUi/highcharts/highcharts-3d.js"/>
    <asset:javascript src="vendorUi/highcharts/themes/grid-rx.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>

    <g:javascript>
        var type = null;
        var option = new Option("${assignedTo?.fullName}", "${assignedTo?.id}", true, true);
        var isCompareView = true;
        var isArchived = "${isArchived}";
        var uniqueKeyMap = JSON.parse("${uniqueMap}");
        var detailsUrl = "${createLink(controller: 'singleCaseAlert', action: 'details', params: [callingScreen: 'review'])}";
        var caseDetailAssessmentUrl = "${createLink(controller: 'caseInfo', action: 'fetchAssessmentDetailData')}";
        var treeNodesUrl = "${createLink(controller: "caseInfo", action: 'fetchTreeViewNodes', params: ['alertType' : com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT, 'alertId': alertId, 'isFaers': false,
                                                                                                        'isAdhocRun': false, 'compareScreen': true])}";
        var authUrl = "${createLink(controller: 'user', action: 'eAuthenticate')}";
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var followUpName = "${Holders.config.caseDetail.followUp.name}";
        var deviceInformation = "${deviceInformation}";
        var freeTextField = JSON.parse("${freeTextField}")
        var versionsCaseDetailUrl = "${createLink(controller: 'caseInfo', action: 'fetchVersionsCaseDetail', params: ['caseNumber'    : caseNumber, 'alertConfigId': configId, 'productFamily': productFamily, 'alertId': alertId, 'isAdhocRun': isAdhocRun,
                                                                                                                      'isDuplicate'   : type, 'isArchived': isArchived, 'exeConfigId': execConfigId, 'versionAvailable': versionAvailable,
                                                                                                                      'versionCompare': versionCompare, 'followUpAvailable': followUpAvailable, 'followUpCompare': followUpCompare, 'isFaers': isFaers])}";
        var isChildCase = ${isChildCase}
    </g:javascript>
    <style>
    .modal-lg {
        width: 1250px; /* New width for large modal */
    }

    .odd {
        background-color: #EFEFEF;
        padding: 0;
    }

    .wikEdDiffFragment {
        overflow: hidden
    }

    .wikEdDiffDelete{
        background: #fbE1eb!important;
    }
    </style>
</head>

<body>
<div class="menuButton m-r-10 back-button">
    <a id="back-button" class="btn btn-info"><i
            class="fa fa-caret-left m-r-5"></i>Back</a>
</div>

<div id="top-panel" class="container">
    <div class="row">
        <div id="detail-tabs" class="col-md-12 panel m-b-0">
            <div role="presentation" class="col-sm-6">
                <h4 aria-controls="details" style="float: left; margin-right: 20px;" role="tab"><g:message
                        code="app.label.case.number"/> : ${caseNumber}

                </h4>
            </div>

            <div class="col-sm-6">
                <div class="row">

                    <span class="fl-right m-r-25">
                        <span class="m-r-25">
                            <h4 aria-controls="details" class="fl-left" role="tab">${printData[0]}</h4>
                            <h4 aria-controls="details" class="compare-tilt" role="tab" style="font-size:23px!important;">~</h4>
                            <h4 aria-controls="details" class="fl-left" role="tab">${printData[1]}</h4>
                        </span>
                        <g:if test="${availableVersions?.size() > 1}">
                            <span class="menuButton m-r-10" tabindex="0">
                                <i class="mdi mdi-compare mr-10 font-24 lh-1  grid-menu-tooltip valign-m "
                                   id="version-list"
                                   data-title="Compare Versions"></i>

                                <div class="tooltip fade top in hide tooltip-versions m-t-40" id="version-list-tooltip">
                                    <div class="tooltip-inner row font-15">
                                        <div class="col-xs-6 col-md-6 col-lg-6 t-a-left"><div class="tooltip-heading">Available Versions</div>
                                            <g:each in="${availableVersions}" status="i" var="version">
                                                <div class="version-available m-t-5" data-id="${version.versionNum}"
                                                     data-followUp="${version.followUpNum}">
                                                    ${version.data} (${version.receiptDate})
                                                </div>
                                            </g:each>
                                        </div>

                                        <div class="col-xs-6 col-md-6 col-lg-6 t-a-left"><div class="tooltip-heading">Versions to Compare</div>
                                            <g:each in="${availableVersions}" status="i" var="version">

                                                <div class="version-compare m-t-5" data-id="${version.versionNum}"
                                                     data-followUp="${version.followUpNum}">
                                                    <span>${version.data} (${version.receiptDate})</span>
                                                    <g:if test="${!isChildCase && version.lastVersion}">
                                                        <span class="green-2">*</span>
                                                    </g:if>
                                                </div>
                                            </g:each>
                                        </div>
                                        <g:if test="${!isChildCase && isLastVersionPresent}">
                                            <div class="m-t-5 green-2 t-a-left">
                                                * Last reviewed version from the previous period
                                            </div>
                                        </g:if>
                                    </div>
                                </div>
                            </span>
                        </g:if>
                        <g:if test="${availableVersions?.size() > 1 && followUpNumber!='0'}">
                            <g:if test="${isLastVersionPresent}">
                                <input type="hidden" id="previous-version" value="${previousVersion}"/>
                                <input type="hidden" id="previous-followUp" value="${previousFollowUp}"/>
                            </g:if>
                            <span class="menuButton m-r-10">
                                <a class="btn btn-primary compare-versions"><g:message
                                        code="app.compare.versions"/></a>
                            </span>
                        </g:if>
                        <div class="text-right form-inline m-t-8 ico-menu" style="cursor: not-allowed">
                            <span class="btn-disabled menuButton ${buttonClass}">
                                <a href='${ciomsReportUrl}' target="_blank"
                                   class="btn btn-primary generate-cioms"><g:message
                                        code="caseDetails.generate.cioms"/></a>
                            </span>
                        </div>
                    </span>
                </div>
            </div>
        </div>
    </div>

</div>

<div class="info-widget">

    <div class="pv-fullcase-treeview">
        <div id="caseDetailTree" class="demo"></div>
    </div>


    <div class="treeview-content">
        <rx:containerCollapsible class="section m-b-10" title="Workflow and Categories Management">
            <div class="row">
                <div>
                    <span class="box-100">
                        <input data-field="productFamily" data-id="${productFamily}" type="hidden"
                               value="${productFamily}"/>
                        <input data-field="primaryEvent" data-id="${primaryEvent}" type="hidden"
                               value="${primaryEvent}"/>
                        <input class="alert-check-box" data-id="${alertId}" type="hidden"
                               value="${alertId}"/>
                        <input class="statsId" data-id="${statsId}" type="hidden"
                               value="${statsId}"/>
                        <span data-field="productName" data-id="${productName}"></span>
                        <span data-field='caseNumber' data-id="${caseNumber}"></span>
                        <span data-field='caseVersion' data-id="${caseVersion}"></span>
                        <span data-field='followUpNumber' data-id="${followUpNumber}"></span>
                        <span data-field="masterPrefTermAll" data-id="${pt}"></span>

                    </span>
                </div>
            <g:if test="${!isChildCase && !isAdhocRun && !isFaers}">
                <div class="col-md-3 currentDisposition">
                    <label class="box-100">Current Disposition</label>
                    <h5>${disposition}</h5>
                </div>
            </g:if>

                <div class="col-md-3" id="categories">

                    <label>Category</label>
                    <input type="hidden" id="compared-categories" value="${comparedCategories}"/>

                    <div id="categories-comparison">

                    </div>
                </div>

            <g:if test="${!isChildCase && !isAdhocRun && !isFaers}">
                <g:render template="/includes/widgets/assigned_to_Select"/>
            </g:if>
            </div>
        </rx:containerCollapsible>
        <div class="comparing-details">

        </div>

    </div>

</div>

<g:if test="${!isChildCase && caseDetail}">
    <g:hiddenField class="execConfigId" name="execConfigId" value="${caseDetail.execConfigId}"/>
    <g:hiddenField class="configId" name="configId" value="${caseDetail.configId}"/>
    <g:hiddenField class="productName" name="productName" value="${caseDetail.productName}"/>
</g:if>

<g:render template="/includes/modals/message_box"/>
<g:render template="/includes/modals/case_diff_modal"/>
<div id="compare-screens-spinner" class="hidden">
    <div class="grid-loading spinner-compare"><img src="/signal/assets/spinner.gif"
                                                                               width="30" align="middle"/></div>
</div>


<g:hiddenField class="caseNumber" name="caseNumber" value="${caseNumber}"/>
<g:hiddenField class="alertId" name="alertId" value="${alertId}"/>
<g:hiddenField id="version" name="versionNumber" value="${version}"/>
<g:hiddenField id="followUpNumber" name="followUpNumber" value="${followUpNumber}"/>
<g:hiddenField class="followUpNumber" name="followUpNumber" value="${followUpNumber}"/>
<g:hiddenField class="isFaers" name="isFaers" value="${isFaers}"/>
<g:hiddenField class="absentValue" name="absentValue" value="${absentValue}"/>
<g:hiddenField class="isAdhocRun" name="isAdhocRun" value="${isAdhocRun}"/>
<g:hiddenField id='isArchived' name='isArchived' value="${isArchived}"/>
<g:hiddenField id='isFaers' name='isFaers' value="${isFaers}"/>
<g:hiddenField id='fullCaseList' name='fullCaseList' value="${fullCaseList}"/>
<g:hiddenField id='detailsParameters' name='detailsParameters' value="${detailsParameters}"/>
<g:hiddenField id='execConfigId' name='execConfigId' value="${execConfigId}"/>
<g:hiddenField id='isSingleAlertScreen' name='isSingleAlertScreen' value="${isSingleAlertScreen}"/>
<g:hiddenField id='isCaseSeries' name='isCaseSeries' value="${isCaseSeries}"/>
<g:hiddenField id='isArgusDataSource' name='isArgusDataSource' value="${isArgusDataSource}"/>
<g:hiddenField id='oldFollowUp' name='oldFollowUp' value="${oldFollowUp}"/>
<g:hiddenField id='oldVersion' name='oldVersion' value="${oldVersion}"/>

</body>
</html>
