<%@ page import="com.rxlogix.config.Disposition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="disposition.list" default="Disposition List"/></title>
</head>
<rx:container title="Disposition List">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${dispositionInstanceTotal}" var="theInstance"/>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <div class="nav">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="disposition.new" default="New Disposition"/></g:link></span>
        </div>
    </sec:ifAnyGranted>

    <div class="body">
        <div class="list">
            <table class="dataTable notused">
                <thead>
                <tr>
                    <g:sortableColumn property="id" title="Display Name" titleKey="disposition.id"/>
                    <g:sortableColumn property="description" title="Description" titleKey="disposition.description"/>
                    <g:sortableColumn property="displayName" title="Value" titleKey="disposition.value"/>
                    <g:sortableColumn property="display" title="Display" titleKey="disposition.display"/>
                    <g:sortableColumn property="closed" title="Closed" titleKey="disposition.closed"/>
                    <g:sortableColumn property="validatedConfirmed" title="Validated Confirmed"
                                      titleKey="disposition.validatedConfirmed"/>
                    <g:sortableColumn property="reviewCompleted" title="Review Completed"
                                      titleKey="disposition.reviewCompleted"/>

                </tr>
                </thead>
                <tbody>
                <g:each in="${dispositionInstanceList}" status="i" var="dispositionInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td class="col-max-200"><g:link action="edit" id="${dispositionInstance.id}" style="word-wrap: break-word;">
                            ${dispositionInstance.displayName}</g:link></td>
                        <td class="col-min-200" style="white-space: pre-wrap !important" >${dispositionInstance.description}</td>
                        <td>${dispositionInstance.value}</td>
                        <td><g:formatBoolean boolean="${dispositionInstance.display}"
                                             true="Yes" false="No"/></td>
                        <td><g:formatBoolean boolean="${dispositionInstance.closed}"
                                             true="Yes" false="No"/></td>
                        <td><g:formatBoolean boolean="${dispositionInstance.validatedConfirmed}"
                                             true="Yes" false="No"/></td>
                        <td><g:formatBoolean boolean="${dispositionInstance.reviewCompleted}"
                                             true="Yes" false="No"/></td>

                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

        <div class="paginateButtons">
            <g:paginate total="${dispositionInstanceTotal}"/>
        </div>
    </div>
</rx:container>
</html>
