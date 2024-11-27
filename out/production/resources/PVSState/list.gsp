
<%@ page import="com.rxlogix.config.PVSState" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="PVSState.list" default="Workflow State List" /></title>
    </head>
    <rx:container title="Workflow State List">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${PVSStateInstanceTotal}" var="theInstance"/>
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <div class="nav">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create">
                <g:message code="PVSState.new" default="New Workflow State" /></g:link></span>
        </div>
        </sec:ifAnyGranted>
        <div class="body">

            <div class="list">
                <table class="dataTable notused">
                    <thead>
                        <tr>
                   	    <g:sortableColumn property="id" title="Value" titleKey="PVSState.id" />
                        <g:sortableColumn property="description" title="Description" titleKey="PVSState.description" />
                        <g:sortableColumn property="displayName" title="Display Name" titleKey="PVSState.displayName" />
                        <g:sortableColumn property="display" title="Display" titleKey="PVSState.display" />
                        <g:sortableColumn property="finalState" title="Final State" titleKey="PVSState.finalState" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${PVSStateInstanceList}" status="i" var="PVSStateInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="edit" id="${PVSStateInstance.id}">${fieldValue(bean: PVSStateInstance, field: "value")}</g:link></td>
                            <td>${fieldValue(bean: PVSStateInstance, field: "description")}</td>
                            <td>${fieldValue(bean: PVSStateInstance, field: "displayName")}</td>
                            <td><g:formatBoolean boolean="${PVSStateInstance.display}" true="Yes" false="No"/></td>
                            <td><g:formatBoolean boolean="${PVSStateInstance.finalState}" true="Yes" false="No" /></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${PVSStateInstanceTotal}" />
            </div>
        </div>
    </rx:container>
</html>
