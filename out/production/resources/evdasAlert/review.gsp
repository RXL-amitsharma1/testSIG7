<%@ page import="com.rxlogix.enums.ReportFormat" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.evdas.review" /></title>
    <g:javascript>

        var LINKS = {
            toPDF : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormat.PDF])}",
            toExcel : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormat.XLSX])}",
            toWord : "${createLink(controller: 'report', action: 'exportReportFromInbox', params: [outputFormat: ReportFormat.DOCX])}",
            toShare : "${createLink(controller: 'report', action: 'share')}",
            toEmail : "${createLink(controller: 'report', action: 'email')}"
        };
        var appType = "EVDAS Alert";
        var sessionRefreshUrl = "${createLink(controller: 'evdasAlert', action: 'changeFilterAttributes')}";
        var listConfigUrl = "${createLink(controller: 'evdasAlert', action: 'listConfig')}";
        var detailsUrl = "${createLink(controller: 'evdasAlert', action: 'details')}";
        var getSharedWith = "${createLink(controller: 'evdasAlert', action: 'getSharedWithUserAndGroups')}";

    </g:javascript>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/alerts_review/evdas_alert_review.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css" />

</head>
<body>
<rx:container title="${message(code: message(code:"app.evdas.review"))}" options="${true}" >
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>

    <table id="evdasAlerts" class="row-border hover evdas-alert-table" width="100%">
        <thead>
        <tr>
            <th><g:message code="app.label.alert.name"/></th>
            <th><g:message code="app.label.description"/></th>
            <th><g:message code="app.label.productSubstanceSelection"/></th>
            <th><g:message code="app.count.pec"/></th>
            <th><g:message code="app.new.pec"/></th>
            <th><g:message code="app.closed.pec"/></th>
            <th><g:message code="app.alert.priority"/></th>
            <th><g:message code="app.label.DateRange"/></th>
            <th><g:message code="app.alert.last.modified"/></th>
            <th><g:message code="app.alert.last.execution"/></th>
            <th><g:message code="app.label.action"/></th>
        </tr>
        </thead>
    </table>
</rx:container>
<div id="search-control" class="pull-right dropdown-toggle" style="display: flex; flex-direction: row;margin-right: 10px; visibility: hidden">
    <div id="dropdownUsers" class="col-xl-2 pull-right dropdown-toggle" style="width: 30rem;max-width: 30rem; display: flex; flex-direction: row;">
    <label for="alertsFilter" class="pull-right" style="white-space: nowrap; margin-right: 5px;margin-top: 2px">Select Users</label>
        <g:initializeUsersAndGroupsElement shareWithId="" isWorkflowEnabled="true" alertType="evdas"  />
    </div>
    <span id="custom-search-label" class="pull-right" style="margin-left: 25px;margin-right: 5px;margin-top: 2px">
        <label for="custom-search">Search</label>
    </span>
    <input id="custom-search" class="pull-right dropdown-toggle form-control" style="width: 200px; margin-left: 3px; height: 22px!important;">
</div>
<g:hiddenField id="filterVals" value="${session.getAttribute("evdas")}" name="filterVals" />

<g:form controller="${controller}" onsubmit="return submitForm()">
    <g:hiddenField name="executedConfigId" />
    <g:render template="/includes/modals/sharedWithModal"/>
</g:form>

</body>

