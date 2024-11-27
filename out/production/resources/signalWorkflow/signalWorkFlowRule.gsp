<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.config.WorkflowRule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.signal.workflow.rule"/></title>
    <g:javascript>
        var signalWorkflowRuleUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowRule')}";
        var signalWorkflowStateUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowState')}";
        var signalWorkflowRule = "${createLink(controller: 'signalWorkflow', action: 'signalWorkflowRule')}";
        var signalWorkflowListUrl = "${createLink(controller: 'signalWorkflow', action: 'signalWorkflowList')}";
        var createUrl = "${createLink(controller: 'signalWorkflow', action: 'createSignalWorkflowRule')}";
        var editUrl = "${createLink(controller: 'signalWorkflow', action: 'editSignalWorkflowState')}";
        var saveWorkflowRuleUrl = "${createLink(controller: 'signalWorkflow', action: 'saveWorkflowRule')}";
        var enableSignalWorkflowUrl = "${createLink(controller: 'signalWorkflow', action: 'enableSignalWorkflow')}";
        var enableWorkflow = ${enableWorkflow};
        var isEditingAllowed = ${isEditingAllowed};
    </g:javascript>
    <asset:javascript src="app/pvs/signalWorkflow/signal_workflow.js"/>
</head>

<body>
<rx:container title="Signal Workflow Rule">
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="row">
        <div class="col-lg-12">
            <table id="signalWorkflowRuleTable" class="row-border hover" width="100%">
                <thead>
                <tr>
                    <th><g:message code="app.label.signal.workflow.rule.name"/></th>
                    <th><g:message code="app.label.signal.workflow.rule.description"/></th>
                    <th><g:message code="app.label.signal.workflow.rule.fromState"/></th>
                    <th><g:message code="app.label.signal.workflow.rule.toState"/></th>
                    <th><g:message code="app.label.signal.workflow.rule.allowedGroups"/></th>
                    <th><g:message code="app.label.signal.workflow.rule.display"/></th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</rx:container>
</body>
</html>
