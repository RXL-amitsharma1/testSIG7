<%@ page import="grails.util.Holders; com.rxlogix.config.AlertType"%>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ExecutionStatus.title"/></title>
    <asset:javascript src="app/pvs/configuration/executionStatus.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="executionStatus.css"/>
    <g:javascript>
        var executionStatusUrl = "${createLink(controller: 'ConfigurationRest', action: 'executionStatus')}";
        var ShowConfigUrl = "${createLink(controller: 'Configuration', action: 'viewConfig')}";
        var listExecutionStatusUrl = "${createLink(controller: 'Configuration', action: 'executionStatus')}";
        var listAllResultsUrl = "${createLink(controller: 'Configuration', action: 'listAllResults')}";
        var evdasAlertDetailsUrl = "${createLink(controller: 'EvdasAlert', action:'view')}";
        var scaDetailsUrl = "${createLink(controller: 'SingleCaseAlert', action:'view')}";
        var agaDetailsUrl = "${createLink(controller: 'AggregateCaseAlert', action:'view')}";
        var resumeAlertUrl = "${createLink(controller: 'ConfigurationRest', action:'resumeAlertExecution')}";
        var executionErrorUrl = "${createLink(controller: 'Configuration', action:'signalExecutionError')}";
        var resumeReportUrl = "${createLink(controller: 'ConfigurationRest', action:'resumeReportExecution')}";
        var resumeSpotfireUrl = "${createLink(controller: 'ConfigurationRest', action:'resumeSpotfireExecution')}";
        var roleAccessMap = {
            ${AlertType.SINGLE_CASE_ALERT.key}:${roleAccessMap.get(AlertType.SINGLE_CASE_ALERT.key)},
            ${AlertType.AGGREGATE_CASE_ALERT.key}:${roleAccessMap.get(AlertType.AGGREGATE_CASE_ALERT.key)},
            ${AlertType.EVDAS_ALERT.key}:${roleAccessMap.get(AlertType.EVDAS_ALERT.key)},
            ${AlertType.LITERATURE_SEARCH_ALERT.key}:${roleAccessMap.get(AlertType.LITERATURE_SEARCH_ALERT.key)},
        }

    </g:javascript>
</head>

<body>
<rx:container title="${message(code: "app.ExecutionStatus.title")}" options="${true}" bean="${error}">

    <g:render template="/includes/layout/flashErrorsDivs"/>
    <table id="rxTableReportsExecutionStatus" class="row-border hover" width="100%">
        <thead>
        <tr>
            <th><g:message code="app.label.name"/></th>
            <th><g:message code="app.label.alert.type" default="Alert Type"/></th>
            <th><g:message code="app.label.frequency"/></th>
            <th><g:message code="app.label.runDate"/></th>
            <th><g:message code="app.label.runDuration" type="hidden"/></th>
            <th><g:message code="app.label.owner"/></th>
            <th><g:message code="app.label.alert.status"/></th>
            <th><g:message code="app.label.report.status"/></th>
            <g:if test="${Holders.config.signal.spotfire.enabled}">
                <th><g:message code="app.label.spotfire.status"/></th>
            </g:if>
            <g:else>
                <th id="spotFireHidden" style="display:none;"><g:message code="app.label.spotfire.status"/></th>
            </g:else>
        </tr>
        </thead>
    </table>
    <g:hiddenField name="relatedResults" id="relatedResults" value="${related}"/>
    <g:hiddenField name="isAdmin" id="isAdmin" value="${isAdmin}"/>
</rx:container>
<g:render template="/includes/widgets/executionStatusDropDown" />
</body>