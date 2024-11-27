<%@ page import="com.rxlogix.config.WorkflowRule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.create.signal.workflow.rule" default="Create Signal Workflow Rule"/></title>
    <g:javascript>
        var signalWorkflowRuleUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowRule')}";
        var signalWorkflowStateUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowState')}";
        var workflowStatesSignal = JSON.parse("${workflowStatesSignal}");
    </g:javascript>
    <asset:javascript src="app/pvs/signalWorkflow/signal_workflow.js"/>
</head>

<rx:container title="Create Signal Workflow Rule">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${signalWorkflowRuleInstance}" var="theInstance"/>
    <div class="nav bord-bt">
        <span class="menuButton">
            <g:link class="list btn btn-primary" action="signalWorkflowRule"><g:message code="app.label.signal.workflow.rule"
                                                                           default="Signal Workflow Rule"/></g:link>
        </span>
    </div>

    <div class="createRule">
        <g:form action="saveWorkflowRule" method="post">
            <g:render template="form" model="[workflowStatesSignal: workflowStatesSignal]"/>
        </g:form>
    </div>
</rx:container>
</html>
