<%@ page import="com.rxlogix.enums.ReportFormat" %>

<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.new.literature.review"/></title>
    <g:javascript>
        var sessionRefreshUrl = "${createLink(controller: 'literatureAlert', action: 'changeFilterAttributes')}";
        var listConfigUrl = "${createLink(controller: 'literatureAlert', action: 'listByLiteratureConfiguration')}";
        var detailsUrl = "${createLink(controller: 'literatureAlert', action: 'details')}";
        var getSharedWith = "${createLink(controller: 'literatureAlert', action: 'getSharedWithUserAndGroups')}";
    </g:javascript>
    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/alerts_review/literature/literature_alert_review.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
</head>

<body>
<rx:container title="${message(code: message(code: "app.new.literature.review"))}" options="${true}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${literatureConfiguration}" var="theInstance"/>

    <div class="row">
        <div class="col-lg-12">
            <table id="simpleCaseAlerts" class="row-border hover simple-alert-table" width="100%">
                <thead>
                <tr>
                    <th>
                        <div class="th-label">
                            <g:message code="app.label.alert.name"/>
                        </div>
                    </th>
                    <th>Search String</th>
                    <th><g:message code="app.label.DateRange"/></th>
                    <th><g:message code="app.label.signal.data.source"/></th>
                    <th><g:message code="app.alert.last.modified"/></th>
                    <th><g:message code="app.alert.last.execution"/></th>
                    <th>
                        <g:if test="${true}">
                            <g:message code="app.label.action"/>
                        </g:if>
                    </th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</rx:container>
<div id="search-control" class="pull-right dropdown-toggle" style="display: flex; flex-direction: row;margin-right: 10px; visibility: hidden">
    <div id="dropdownUsers" class="col-xl-2 pull-right dropdown-toggle" style="width: 30rem;max-width: 30rem; display: flex; flex-direction: row;">
    <label for="alertsFilter" class="pull-right" style="white-space: nowrap; margin-right: 5px;margin-top: 2px">Select Users</label>
        <g:initializeUsersAndGroupsElement shareWithId="" isWorkflowEnabled="true" alertType="literatureFilter"  />
    </div>
    <span id="custom-search-label" class="pull-right" style="margin-left: 25px;margin-right: 5px;margin-top: 2px">
        <label for="custom-search">Search</label>
    </span>
    <input id="custom-search" class="pull-right dropdown-toggle form-control" style="width: 200px; margin-left: 3px; height: 22px!important;">
</div>
<g:hiddenField id="filterVals" value="${session.getAttribute("literatureFilter")}" name="filterVals" />

<g:form controller="${controller}" onsubmit="return submitForm()">
    <g:hiddenField name="executedConfigId" />
    <g:render template="/includes/modals/sharedWithModal"/>
</g:form>
</body>
