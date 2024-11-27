<%@ page import="com.rxlogix.config.Priority" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="priority.list" default="Priority List"/></title>
</head>
<rx:container title="Priority List">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${priorityInstanceTotal}" var="theInstance"/>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <div class="nav">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="priority.new" default="New Priority"/></g:link></span>
        </div>
    </sec:ifAnyGranted>
    <div class="body">

        <div class="list">
            <table class="dataTable notused">
                <thead>
                <tr>
                    <g:sortableColumn class="col-sm-2" property="displayName" title="Display Name" titleKey="priority.id"/>
                    <g:sortableColumn class="col-sm-2" property="value" title="Value" titleKey="priority.value"/>
                    <g:sortableColumn class="col-sm-4" property="description" title="Description" titleKey="priority.description"/>
                    <g:sortableColumn class="col-sm-1" property="reviewPeriod" title="Review Period" titleKey="priority.reviewPeriod"/>
                    <g:sortableColumn class="col-sm-1" property="display" title="Display" titleKey="priority.display"/>
                    <g:sortableColumn class="col-sm-1" property="defaultPriority" title="Default" titleKey="priority.defaultPriority"/>
                    <g:sortableColumn class="col-sm-1" property="priorityOrder" title="Priority Order"
                                      titleKey="priority.priorityOrder"/>
                </tr>
                </thead>
                <tbody>
                <g:each in="${priorityInstanceList}" status="i" var="priorityInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td style="word-break: break-all"><g:link action="edit" id="${priorityInstance.id}">
                            ${priorityInstance.displayName}</g:link></td>
                        <td style="word-break: break-all">${priorityInstance.value}</td>
                        <td style="word-break: break-all;white-space: pre-wrap !important">${priorityInstance.description}</td>
                        <td>${priorityInstance.reviewPeriod}</td>
                        <td><g:formatBoolean boolean="${priorityInstance.display}" true="Yes" false="No"/></td>
                        <td><g:formatBoolean boolean="${priorityInstance.defaultPriority}" true="Yes" false="No"/></td>
                        <td>${priorityInstance.priorityOrder}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

        <div class="paginateButtons">
            <g:paginate total="${priorityInstanceTotal}"/>
        </div>
    </div>
</rx:container>
</html>
