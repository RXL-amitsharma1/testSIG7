<!DOCTYPE html>
<html>
<%@ page import="com.rxlogix.Constants" %>
<head>
    <meta name="layout" content="main"/>
    <title>PV Signal - Linked Signals</title>
    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/pvs/alertAdministration/linkedSignals.js"/>
</head>

<body>
<rx:container title="Linked Signals Information:- ${alertName}">
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="messageContainer"></div>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <g:if test="${alertType == "${Constants.AlertConfigType.SINGLE_CASE_ALERT}"}">
            <g:render template="/alertAdministration/includes/singleReview"/>
        </g:if>
        <g:if test="${alertType == "${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}"}">
            <g:render template="/alertAdministration/includes/aggregateReview"/>
        </g:if>
        <g:if test="${alertType == "${Constants.AlertConfigType.EVDAS_ALERT}"}">
            <g:render template="/alertAdministration/includes/evdasReview"/>
        </g:if>
        <g:if test="${alertType == "${Constants.AlertConfigType.LITERATURE_SEARCH_ALERT}"}">
            <g:render template="/alertAdministration/includes/literatureReview"/>
        </g:if>
        <g:render template="/validatedSignal/includes/validatedObservation/disassociateJustification"/>
    </sec:ifAnyGranted>
</rx:container>
</body>
</html>
