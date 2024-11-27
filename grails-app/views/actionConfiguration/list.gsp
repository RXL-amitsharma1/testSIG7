
<%@ page import="com.rxlogix.config.ActionConfiguration" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="action.configuration.list" default="ActionConfiguration List" /></title>
    </head>
    <rx:container title="Action Configurations">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${actionConfigurationInstanceTotal}" var="theInstance"/>
        <div class="nav">
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message code="action.configuration.new" default="New ActionConfiguration" /></g:link></span>
        </sec:ifAnyGranted>
        </div>
        <div class="body">
            <div class="list">
                <table class="dataTable notused">
                    <thead>
                        <tr>
                   	    <g:sortableColumn property="value" title="Name" titleKey="action.configuration.name" />
                   	    <g:sortableColumn property="displayName" title="Display Name" titleKey="action.configuration.display.name" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${actionConfigurationInstanceList}" status="i" var="actionConfigurationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td class="word-break"><g:link action="show" id="${actionConfigurationInstance.id}">
                                ${actionConfigurationInstance.value}</g:link></td>
                            <td class="word-break">${actionConfigurationInstance.displayName}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${actionConfigurationInstanceTotal}" />
            </div>
        </div>
    </rx:container>
</html>
