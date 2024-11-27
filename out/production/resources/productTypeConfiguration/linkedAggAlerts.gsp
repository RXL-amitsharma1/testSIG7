<!DOCTYPE html>
<html>
<%@ page import="com.rxlogix.Constants" %>
<head>
    <meta name="layout" content="main"/>
    <title>PV Signal - Product Rule Configuration</title>
    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/validated_signal/validatedObservation.js"/>
</head>

<body>
<rx:container title="Product Type :- ${productConfigName}">
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="messageContainer"></div>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <g:render template="/validatedSignal/includes/aggregateListForProductRule" model="[aggData: aggData, edit: true]"/>
    </sec:ifAnyGranted>
</rx:container>
</body>
</html>
