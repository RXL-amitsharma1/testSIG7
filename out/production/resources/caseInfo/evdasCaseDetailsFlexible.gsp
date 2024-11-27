<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Case Detail</title>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>

    <asset:javascript src="app/pvs/alerts_review/case_diff.js"/>
    <asset:javascript src="app/pvs/alerts_review/evdas_case_details_flexible.js"/>
    <asset:stylesheet src="text_diff.css"/>
    <asset:stylesheet src="jsTree/style.min.css"/>
    <asset:javascript src="vendorUi/jsTree/jstree.js"/>

    <g:javascript>
        var treeNodesUrl = "${createLink(controller: "caseInfo", action: 'fetchTreeViewNodes', params: ['alertType': com.rxlogix.Constants.AlertConfigType.EVDAS_ALERT, 'wwid': wwid])}"
        var sectionNameList = JSON.parse('${sectionNameList}');
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var isFaers = false;
    </g:javascript>
    <style>
    .modal-lg {
        width: 1250px; /* New width for large modal */
    }

    .odd {
        background-color: #EFEFEF;
        padding: 0;
    }

    #evdasExportMenu .dropdown-menu {
        left: -160px;
    }
    </style>
    <g:render template="/includes/widgets/actions/action_types"/>
</head>

<body>
<div id="top-panel" class="container">
    <div class="row">
        <div id="detail-tabs" class="col-md-12 panel m-b-0">
            <div role="presentation" class="col-sm-5">
                <h4 aria-controls="details" role="tab"><g:message code="app.label.evdas.title"
                                                                  args="[caseNumber, followUpNumber]"/></h4>
            </div>

            <div class="col-sm-7">
                <div class="m-t-8 text-right form-inline">
                    <div class="form-group">
                        <g:select
                                name="versionNumber"
                                id="evdasVersionNumber"
                                class="form-control m-r-15 col-max-200"
                                from="${versionNumberList}"
                                value="${version}"
                                optionKey="id"
                                optionValue="desc"
                                noSelection='["": "${message(code: 'app.label.chooseFollowUp')}"]'/>
                    </div>
                    <span id="evdasExportMenu" class="m-r-15 pos-rel">
                        <span class="dropdown-toggle exportPanel" data-toggle="dropdown">
                            <img src="/signal/assets/excel.gif" class="" height="20" width="20"/>
                            <span class="caret"></span></button>

                        </span>
                        <ul class="dropdown-menu export-type-list" id="exportTypesTopic">
                            <strong class="font-12">Export</strong>
                            <li><g:link controller="caseInfo" action="exportCaseInfo" class="m-r-30"
                                        params="${[outputFormat: com.rxlogix.enums.ReportFormat.DOCX, caseNumber: caseNumber, version: version, evdasCase: evdasCase]}">
                                <img src="/signal/assets/word-icon.png" class="m-r-10" height="16"
                                     width="16"/><g:message code="save.as.word"/>
                            </g:link>
                            </li>
                            <li><g:link controller="caseInfo" action="exportCaseInfo" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.XLSX, caseNumber: caseNumber, version: version, evdasCase: evdasCase]}">
                                <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                                        code="save.as.excel"/>
                            </g:link></li>
                            <li><g:link controller="caseInfo" action="exportCaseInfo" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.PDF, caseNumber: caseNumber, version: version, evdasCase: evdasCase]}">
                                <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                                        code="save.as.pdf"/>
                            </g:link></li>
                        </ul>
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
        <rx:containerCollapsible title="Case Detail">
            <g:if test="${data}">
                <g:each in="${data}" var="k, v" status="i">
                    <g:if test="${v?.containsValues?.any { it }}">
                        <g:if test="${sectionRelatedInfo?."${k}".isFullText != true}">
                            <rx:containerCollapsible title="${k}">

                                <div class="scroll-body">
                                    <table class="table no-border m-t-0 m-b-0">
                                        <thead>
                                        <g:each in="${v[0]}" var="paramKey, paramValue">
                                            <g:if test="${!(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                                <th>${paramKey}</th>
                                            </g:if>
                                        </g:each>
                                        </thead>
                                        <tbody>
                                        <g:each in="${v}" var="ki" status="column">
                                            <g:if test="${column % 2 == 0}">
                                                <tr class="odd">
                                            </g:if>
                                            <g:else>
                                                <tr class="even">
                                            </g:else>
                                            <g:each in="${v[column]}" var="paramKey, paramValue">
                                                <g:if test="${v[column][Constants.CaseDetailFields.CONTAINS_VALUES] && !(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                                    <g:if test="${fieldRelatedInfo?."${k}"?."${paramKey}" == Constants.CaseDetailUniqueName.LINKED_CHILD_CASE}">
                                                        <td><g:link class="linked-child-case" action="caseDetail"
                                                                    target="_blank"
                                                                    data-casenumber="${paramValue}">${paramValue ?: '-'}</g:link></td>
                                                    </g:if>
                                                    <g:else>
                                                        <td>${paramValue ?: '-'}</td>
                                                    </g:else>
                                                </g:if>
                                            </g:each>

                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </div>

                            </rx:containerCollapsible>
                        </g:if>
                        <g:else>
                            <div class="rxmain-container">
                                <div class="rxmain-container-inner">
                                    <a>
                                        <div class="rxmain-container-header ico-menu">
                                            <div class="dropdown col-md-9 col-xs-9" data-toggle="collapse" data-target="#${k?.toLowerCase()?.replaceAll("\\s", "_")}_container">
                                                <label class="rxmain-container-header-label">${k}</label>
                                            </div>
                                            <div class="dropdown col-md-3 col-xs-3">
                                                <div class="container">
                                                    <div class="col-md-2">

                                                    </div>

                                                    <div class="col-md-3 zoom-tab to-center bd-right">
                                                        <label class="m-t-3">Zoom</label>
                                                    </div>

                                                    <div class="col-md-5 zoom-tab to-center bd-left">
                                                        <label class="fl-left font-16 zoom-in-element">-</label>
                                                        <label class="m-t-3 zoom-viewer">100</label>
                                                        <label>%</label>
                                                        <label class="fl-right font-16 zoom-out-element">+</label>
                                                    </div>

                                                    <div class="col-md-1 m-l-15 zoom-tab" style="padding:0!important;">
                                                        <label class="font-20 refresh-zoom"><i class="mdi mdi-refresh"></i></label>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </a>
                                    <div id="${k?.toLowerCase()?.replaceAll("\\s", "_")}_container"
                                         class="row rxmain-container-content panel-collapse collapse free-text-container">
                                        <g:each in="${v[0]}" var="paramKey, paramValue" status="column">
                                            <g:if test="${!(paramKey in [Constants.CaseDetailFields.CONTAINS_VALUES, Constants.CaseDetailFields.CHECK_SEQ_NUM, Constants.CaseDetailFields.CHECK_SEQ_NUM_TWO])}">
                                                <g:if test="${column % 2 == 0}">
                                                    <div class="row odd">
                                                </g:if>
                                                <g:else>
                                                    <div class="row even">
                                                </g:else>
                                                <div class="col-xs-2 break-word-hyphen">
                                                    <label>${paramKey}</label>
                                                </div>

                                                <div class="col-xs-10">
                                                    <pre style="background-color: inherit;font-size: 14px!important;border: 0;padding: inherit;font-family: 'Open Sans', sans-serif !important;word-break: normal;">${paramValue ?: '-'}</pre>
                                                </div>
                                                </div>
                                            </g:if>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </g:else>
                    </g:if>
                </g:each>
            </g:if>
            <g:else>
                <div class="m-b-10">
                    <span><g:message code="app.case.data.not.found"/></span>
                </div>
            </g:else>
        </rx:containerCollapsible>
    </div>

</div>

<g:hiddenField class="caseNumber" name="caseNumber" value="${caseNumber}"/>
<g:hiddenField class="alertId" name="alertId" value="${alertId}"/>
<g:hiddenField id="caseType" name="caseType" value="${caseType}"/>
<g:hiddenField id="wwid" name="wwid" value="${wwid}"/>
<g:hiddenField class="versionNumber" name="versionNumber" value="${version}"/>
</body>
</html>