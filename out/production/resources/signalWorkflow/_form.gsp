<%@ page import="com.rxlogix.enums.GroupType; com.rxlogix.user.Group;" %>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="ruleName"><g:message code="app.label.signal.workflow.rule.name" default="Rule Name"/></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="ruleName" value=""
                     class="form-control rule-name" maxlength="255"/>
    </div>

    <div class="col-lg-3">
        <label><g:message code="app.label.signal.workflow.rule.fromState" default="From State"/></label>
        <g:select name="fromState" class="form-control from-state" from="${workflowStatesSignal}"
                  value="" />
    </div>


    <div class="col-lg-3">
        <label><g:message code="app.label.signal.workflow.rule.toState" default="To State"/></label>
        <g:select name="toState" class="form-control to-state" from="${workflowStatesSignal.findAll{ it != 'Safety Observation Validation'}}"
                  value="" />
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label><g:message code="app.label.signal.workflow.rule.description" default="Description"/></label>
        <g:textArea name="description" value=""
                    class="form-control description-rule"/>
    </div>

    <div class="col-lg-6">
        <div class="col-lg-12 form-pv">
            <div class="checkbox checkbox-primary checkbox-inline">
                <g:checkBox name="display" id="display-workflow-rule" value="true" class=""/>
                <label for="display-workflow-rule"><g:message code="app.label.signal.workflow.rule.display" default="Display"/></label>
            </div>
        </div>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="allowedGroups"><g:message code="app.label.signal.workflow.rule.allowedGroups"/></label>
        <g:select name="allowedGroups" id="signal-workflowRule-groups" from="${Group.findAllByGroupType(GroupType.USER_GROUP)}" multiple="true" optionKey="id"
                  optionValue="name" value="" class="form-control"/>
    </div>
</div>

<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
    <div class="row form-group">
        <div class="col-lg-5">
            <span class="button"><g:submitButton name="saveRule" class="saveRule btn btn-primary"
                                                 value="${message(code: 'app.label.save.signal.workflow.rule', 'default': 'Save')}"/></span>
            <span class="button"><g:actionSubmit class="cancel btn pv-btn-grey" action="signalWorkflowRule"
                                                 value="${message(code: 'app.label.cancel.signal.workflow.rule', 'default': 'Cancel')}"/></span>
        </div>
    </div>
</sec:ifAnyGranted>
