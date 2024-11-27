<%@ page import="com.rxlogix.config.ActionType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="action.type.list" default="ActionType List"/></title>
</head>
<rx:container title="List Action Types">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${actionTypeInstanceTotal}" var="theInstance"/>

    <div class="nav">
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="action.type.new" default="New ActionType"/></g:link></span>
        </sec:ifAnyGranted>
    </div>

    <div class="body">

        <div class="list">
            <table class="dataTable notused">
                <thead>
                <tr>
                    <g:sortableColumn property="value" title="Name" titleKey="action.type.name"/>
                    <g:sortableColumn property="displayName" title="Display Name" titleKey="action.type.display.name"/>
                    <g:sortableColumn property="description" title="Description" titleKey="action.type.description"/>
                </tr>
                </thead>
                <tbody>
                <g:each in="${actionTypeInstanceList}" status="i" var="actionTypeInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td><g:link action="show" id="${actionTypeInstance.id}">${fieldValue(bean: actionTypeInstance, field: "value")}</g:link></td>
                        <td>${fieldValue(bean: actionTypeInstance, field: "displayName")}</td>
                        <td style="white-space: pre-wrap !important">${fieldValue(bean: actionTypeInstance, field: "description")}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

        <div class="paginateButtons">
            <g:paginate total="${actionTypeInstanceTotal}"/>
        </div>
    </div>
</rx:container>
</html>
