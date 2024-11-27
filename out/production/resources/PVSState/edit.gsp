<%@ page import="com.rxlogix.config.PVSState" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="PVSState.edit" default="Edit PVSState"/></title>
    <asset:javascript src="app/pvs/workflow/workflow_state.js"/>
</head>
<rx:container title="Edit Workflow State">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${PVSStateInstance}" var="theInstance"/>

    <div class="nav bord-bt">
        <span class="menuButton"><g:link class="list btn btn-primary" action="index">
            <g:message code="PVSState.list" default="Workflow State List"/></g:link></span>
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create">
                <g:message code="PVSState.new" default="New Workflow State"/></g:link></span>
        </sec:ifAnyGranted>
    </div>

    <div class="body">

        <g:form method="post">
            <g:render template="form1" model="[PVSStateInstance: PVSStateInstance, edit: true]"/>
        </g:form>
    </div>
</rx:container>
</html>
