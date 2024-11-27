<%@ page import="com.rxlogix.config.WorkflowRule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.signal.edit.workflow.state" default="Edit Signal Workflow State"/></title>
    <g:javascript>
        var signalWorkflowRuleUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowRule')}";
        var signalWorkflowStateUrl = "${createLink(controller: 'signalWorkflow', action: 'fetchSignalWorkflowState')}";
        var signalWorkflowRule = "${createLink(controller: 'signalWorkflow', action: 'signalWorkflowRule')}";
        var signalWorkflowListUrl = "${createLink(controller: 'signalWorkflow', action: 'signalWorkflowList')}";
    </g:javascript>
    <asset:javascript src="app/pvs/signalWorkflow/signal_workflow.js"/>
</head>

<body>
    <rx:container title="Edit Signal Workflow State">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${signalWorkflowStateInstance}" var="theInstance"/>
        <div class="nav bord-bt">
            <span class="menuButton">
                <g:link class="list btn btn-primary" action="signalWorkflowList"><g:message
                        code="app.label.signal.workflow.list"
                        default="Signal Workflow List"/></g:link>
            </span>
        </div>

        <div class="">
            <g:form action="saveWorkflowState" method="post">
                <div class="row form-group">
                    <g:hiddenField name="id" value="${signalWorkflowStateInstance?.id}"/>
                    <div class="col-lg-3">
                        <label><g:message code="app.label.signal.workflow.state.value" default="Value"/></label>
                        <label><span class="required-indicator">*</span></label>
                        <g:textField name="value" value="${signalWorkflowStateInstance?.value}"
                                     class="form-control"/>
                    </div>

                    <div class="col-lg-3">
                        <label><g:message code="app.label.signal.workflow.state.displayName"
                                          default="Display Name"/></label>
                        <label><span class="required-indicator">*</span></label>
                        <g:textField name="displayName" value="${signalWorkflowStateInstance?.displayName}"
                                     class="form-control"/>
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-6">
                        <label><g:message code="app.label.signal.workflow.state.allowedDispositions"
                                          default="Allowed Dispositions"/></label>
                        <g:select id="allowedDispositions" name="allowedDispositions"
                                  from="${com.rxlogix.config.Disposition.findAll()}"
                                  value="${signalWorkflowStateInstance?.allowedDispositions?.id}" optionKey="id"
                                  optionValue="displayName" multiple="true"
                                  class="form-control select2"/>
                    </div>
                </div>

                <div class="row form-group">
                    <div class="col-lg-1">
                        <div class="col-lg-12">
                            <div class="checkbox checkbox-primary checkbox-inline">
                                <g:checkBox name="defaultDisplay" id="display-workflow-state"
                                            value="${signalWorkflowStateInstance?.defaultDisplay}" class=""/>
                                <label><g:message code="app.label.signal.workflow.state.default" default="Default"/></label>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-3">
                        <div class="col-lg-12">
                            <div class="checkbox checkbox-primary checkbox-inline">
                                <g:checkBox name="dueInDisplay" id="dueIn-workflow-state"
                                            value="${signalWorkflowStateInstance?.dueInDisplay}" class=""/>
                                <label><g:message code="app.label.signal.workflow.state.calculateDueIn"
                                                  default="Calculate Due In"/></label>
                            </div>
                        </div>
                    </div>
                </div>

                <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                    <div class="row form-group">
                        <div class="col-lg-5">
                            <span class="button disable-check" data-action="updateWorkflowState"><g:actionSubmit
                                    class="updateWorkflowState btn btn-primary" action="updateWorkflowState"
                                    value="${message(code: 'app.label.save.signal.workflow.state', 'default': 'Save')}"/></span>
                            <span class="button"><g:actionSubmit class="cancelState btn pv-btn-grey" action="signalWorkflowList"
                        value="${message(code: 'app.label.cancel.signal.workflow.state', 'default': 'Cancel')}"/></span>
                        </div>
                    </div>
                </sec:ifAnyGranted>
            </g:form>
        </div>
    </rx:container>
</body>
</html>
