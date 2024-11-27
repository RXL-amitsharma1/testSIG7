<!DOCTYPE html>
<html>
<%@ page import="com.rxlogix.Constants" %>
<head>
    <meta name="layout" content="main"/>
    <title>PV Signal - Validated Observations</title>
    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/validated_signal/validatedObservation.js"/>
</head>

<body>
<rx:container title="Validated Observations :- ${signalName}">
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="messageContainer"></div>
    <sec:ifAnyGranted roles="ROLE_ADMIN">

        <g:if test="${scaData}">
            <g:render template="/validatedSignal/includes/validatedObservation/singleObservation"/>
        </g:if>
        <g:if test="${aggData}">
            <g:render template="/validatedSignal/includes/validatedObservation/aggregateObservation"/>
        </g:if>
        <g:if test="${evdasData}">
            <g:render template="/validatedSignal/includes/validatedObservation/evdasObservation"/>
        </g:if>
        <g:if test="${literatureData}">
            <g:render template="/validatedSignal/includes/validatedObservation/literatureObservation"/>
        </g:if>
        <g:if test="${adhocData}">
            <g:render template="/validatedSignal/includes/validatedObservation/adhocObservation"/>
        </g:if>
        <g:render template="/validatedSignal/includes/validatedObservation/disassociateJustification"/>

    </sec:ifAnyGranted>
</rx:container>
<div id="disassociate-spinner" class="hidden">
    <div class="grid-loading" style="position: fixed;left: 50%;top: 50%;"><img src="/signal/assets/spinner.gif" width="30" align="middle" /></div>
</div>
</body>
</html>
