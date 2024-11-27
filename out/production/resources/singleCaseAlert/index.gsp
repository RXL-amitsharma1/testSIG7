<%@ page import="com.rxlogix.enums.ReportFormat;" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.single.case.review" /></title>
    <g:javascript>
        var pvrIntegrate = "${grailsApplication.config.pvreports.url? true : false}";
        var integratedPvrUrl= "${grailsApplication.config.pvreports.url}";
        var spotfireIntegrate = "${grailsApplication.config.signal.spotfire.enabled}";
        var indexReportUrl = "${createLink(controller: 'reportResultRest', action:'index')}";
        var archivedReportUrl = "${createLink(controller: 'reportResultRest', action:'archived')}";
        var allReportUrl = "${createLink(controller: 'reportResultRest', action:'UnderReview')}";
        var listIndexUrl = "${createLink(controller: 'report', action:'index')}";
        var listArchivedUrl = "${createLink(controller: 'report', action:'archived')}";
        var listAllUrl = "${createLink(controller: 'report', action:'listAll')}";
        var showReportUrl = "${createLink(controller: 'report', action: 'criteria')}";
        var updateStatusUrl = "${createLink(controller: 'report', action: 'updateStatus')}";
        var deleteReport = "${createLink(controller: 'report', action: 'deleteReport', params: [relatedPage:related])}";

        var getSharedWith = "${createLink(controller: 'reportResultRest', action: 'getSharedWithUserAndGroups')}";
        var getEmailTo = "${createLink(controller: 'reportResultRest', action: 'getEmailToUsers')}";
        var generateCaseSeriesUrl = "${createLink(controller: 'singleCaseAlert', action: 'generateCaseSeries')}";

        var LINKS = {
            toPDF : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormat.PDF])}",
            toExcel : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormat.XLSX])}",
            toWord : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormat.DOCX])}",
            toShare : "${createLink(controller: 'report', action: 'share')}",
            toEmail : "${createLink(controller: 'report', action: 'email')}"
        };

        var listConfigUrl = "${createLink(controller: "singleCaseAlert", action: 'listConfig', params:[adhocRun:false])}";
        var detailsUrl = "${createLink(controller: 'singleCaseAlert', action: 'details')}";
        var template_list_url = "${createLink(controller: 'template', action: 'index')}";
        var sporfireValidationUrl = "${createLink(controller: 'dataAnalysis', action: 'checkSpotfireFile')}";
        var sessionRefreshUrl = "${createLink(controller: 'singleCaseAlert', action: 'changeFilterAttributes')}";
        var appType = "Single Case Alert";
    </g:javascript>
    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/alerts_review/simple_case_alert_review.js"/>
    <asset:javascript src="app/pvs/alerts_review/date_sorting.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
</head>
<body>
    <rx:container title="${message(code: message(code:"app.single.case.review"))}" options="${true}" >
        <g:render template="/includes/layout/flashErrorsDivs" />

        <table id="simpleCaseAlerts" class="row-border hover simple-alert-table" width="100%">
            <thead>
                <tr>
                    <th><g:message code="app.label.alert.name" /> </th>
                    <th><g:message code="app.label.product"/></th>
                    <th><g:message code="app.label.description" /></th>
                    <th><g:message code="app.label.signal.data.source"/></th>
                    <th><g:message code="app.label.case.count"/></th>
                    <th><g:message code="app.new.cases"/></th>
                    <th><g:message code="app.closed.cases"/></th>
                    <th><g:message code="app.alert.priority" /></th>
                    <th>Date Range</th>
                    <th><g:message code="app.alert.last.modified"/></th>
                    <th><g:message code="app.alert.last.execution"/></th>
                    <th>
                        <g:if test="${grailsApplication.config.pvreports.url}">
                            <g:message code="app.label.action"/>
                        </g:if>
                    </th>
                </tr>
            </thead>
        </table>
        <g:set var="userService" bean="userService" />
    </rx:container>
<div id="search-control" class="pull-right dropdown-toggle " style="display: flex; flex-direction: row;margin-right: 10px; visibility: hidden;">
    <div id="dropdownUsers" class="col-xl-2 pull-right dropdown-toggle" style="width: 30rem;max-width: 30rem; display: flex; flex-direction: row;">
    <label for="alertsFilter" class="pull-right" style="white-space: nowrap; margin-right: 5px;margin-top: 2px">Select Users</label>
        <g:initializeUsersAndGroupsElement shareWithId="" isWorkflowEnabled="true" alertType="icr"  />
    </div>
    <span id="custom-search-label" class="pull-right" style="margin-left: 25px;margin-right: 5px;margin-top: 2px">
        <label for="custom-search">Search</label>
    </span>
    <input id="custom-search" class="pull-right dropdown-toggle form-control" style="width: 200px; margin-left: 3px; height: 22px!important;">
</div>
<g:hiddenField id="filterVals" value="${session.getAttribute("icr")}" name="filterVals" />
    <g:render template="/includes/modals/case_series_modal" />
    <g:render template="/singleCaseAlert/includes/sporfireWarningModal"/>
    <g:form controller="${controller}" onsubmit="return submitForm()">
        <g:hiddenField name="executedConfigId" />
        <g:render template="/includes/modals/sharedWithModal"/>
    </g:form>
</body>


