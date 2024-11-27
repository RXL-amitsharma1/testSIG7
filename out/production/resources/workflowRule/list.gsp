<%@ page import="com.rxlogix.config.WorkflowRule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="workflowRule.list" default="WorkflowRule List"/></title>
</head>
<rx:container title="Workflow Rule List">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowRuleInstanceTotal}" var="theInstance"/>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <div class="nav">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="workflowRule.new" default="New Workflow Rule"/></g:link></span>
        </div>
    </sec:ifAnyGranted>
    <div class="body">

        <div class="list">
            <table class="dataTable notused">
                <thead>
                <tr>
                    <g:sortableColumn property="id" title="Name" titleKey="workflowRule.id"/>
                    <g:sortableColumn property="description" title="Description" titleKey="workflowRule.description"/>
                    <g:sortableColumn property="incomeState" title="Income State" titleKey="workflowRule.description"/>
                    <g:sortableColumn property="targetState" title="Target States" titleKey="workflowRule.targetState"/>
                    <g:sortableColumn property="dispositionAsString" title="Allowed Dispositions"
                                      titleKey="workflowRule.dispositionAsString"/>
                    <g:sortableColumn property="display" title="Display" titleKey="workflowRule.display"/>
                </tr>
                </thead>
                <tbody>
                <g:each in="${workflowRuleInstanceList}" status="i" var="workflowRuleInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td><g:link action="edit" id="${workflowRuleInstance.id}">
                            ${workflowRuleInstance.name}</g:link></td>
                        <td>${workflowRuleInstance.description}</td>
                        <td>${workflowRuleInstance.incomeState.displayName}</td>
                        <td>${workflowRuleInstance.targetState.displayName}</td>
                        <td>${workflowRuleInstance.dispositionsAsString}</td>
                        <td><g:formatBoolean boolean="${workflowRuleInstance.display}" true="Yes" false="No"/></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

        <div class="paginateButtons">
            <g:paginate total="${workflowRuleInstanceTotal}"/>
        </div>
    </div>
</rx:container>
</html>
