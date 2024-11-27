<%@ page import="com.rxlogix.enums.GroupType; com.rxlogix.user.Group;" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.edit.signal.workflow.rule" default="Edit Signal Workflow Rule"/></title>
    <g:javascript>
        var signalWorkflowRuleUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowRule')}";
        var signalWorkflowStateUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowState')}";
        var signalWorkflowRule = "${createLink(controller: 'signalWorkflow', action: 'signalWorkflowRule')}";
        var signalWorkflowListUrl = "${createLink(controller: 'signalWorkflow', action: 'signalWorkflowList')}";
        var workflowStatesSignal = JSON.parse("${workflowStatesSignal.sort()}");
    </g:javascript>
    <asset:javascript src="app/pvs/signalWorkflow/signal_workflow.js"/>
</head>

<body>
    <rx:container title="Edit Signal Workflow Rule">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${signalWorkflowRuleInstance}" var="theInstance"/>
        <div class="nav bord-bt">
            <span class="menuButton">
                <g:link class="list btn btn-primary" action="signalWorkflowRule"><g:message code="app.label.signal.workflow.rule"
                                                                               default="Signal Workflow Rule"/></g:link>
            </span>
        </div>

        <div class="">
            <g:form action="saveWorkflowRule" method="post">
                <div class="row form-group">
                    <g:hiddenField name="id" value="${signalWorkflowRuleInstance?.id}"/>
                    <div class="col-lg-3">
                        <label for="ruleName"><g:message code="app.label.signal.workflow.rule.name" default="Rule Name"/></label>
                        <label><span class="required-indicator">*</span></label>
                        <g:textField name="ruleName" value="${signalWorkflowRuleInstance?.ruleName?.trim()?.replaceAll("\\s{2,}", " ")}"
                                     class="form-control rule-name" maxlength="255"/>
                    </div>

                    <div class="col-lg-3">
                        <label><g:message code="app.label.signal.workflow.rule.fromState" default="From State"/></label>
                        <g:select name="fromState" class="form-control from-state" from="${workflowStatesSignal.sort()}"
                                  value="${signalWorkflowRuleInstance?.fromState}" />
                    </div>


                    <div class="col-lg-3">
                        <label><g:message code="app.label.signal.workflow.rule.toState" default="To State"/></label>
                        <g:select name="toState" class="form-control to-state" from="${workflowStatesSignal?.sort().findAll{ it != signalWorkflowRuleInstance?.fromState }}"
                                  value="${signalWorkflowRuleInstance?.toState}" />
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-6">
                        <label><g:message code="app.label.signal.workflow.rule.description" default="Description"/></label>
                        <g:textArea name="description" value="${signalWorkflowRuleInstance?.description}"
                                    class="form-control description-rule"/>
                    </div>

                    <div class="col-lg-6">
                        <div class="col-lg-12 form-pv">
                            <div class="checkbox checkbox-primary checkbox-inline">
                                <g:checkBox name="display" id="display-workflow-rule" value="${signalWorkflowRuleInstance?.display}" class=""/>
                                <label><g:message code="app.label.signal.workflow.rule.display" default="Display"/></label>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-6">
                        <label for="allowedGroups"><g:message code="app.label.signal.workflow.rule.allowedGroups"/></label>
                        <g:select name="allowedGroups" id="signal-workflowRule-groups" from="${Group.findAllByGroupType(GroupType.USER_GROUP)}" multiple="true" optionKey="id"
                                  optionValue="name" value="${signalWorkflowRuleInstance?.allowedGroups?.id}" class="form-control"/>
                    </div>
                </div>

                <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                    <div class="row form-group">
                        <div class="col-lg-5">
                                <span class="button disable-check" data-action="updateWorkflowRule"><g:actionSubmit class="updateWorkflowRule btn btn-primary" action="updateWorkflowRule"
                                                                                                        value="${message(code: 'default.button.update.label', 'default': 'Update')}"/></span>
                                <span class="button disable-check" data-action="deleteWorkflowRule"><g:actionSubmit class="deleteWorkflowRule btn pv-btn-grey" action="deleteWorkflowRule"
                                                                                                        value="${message(code: 'default.button.delete.label', 'default': 'Delete')}"/></span>
                        </div>
                    </div>
                </sec:ifAnyGranted>
            </g:form>
        </div>
    </rx:container>
</body>
</html>
