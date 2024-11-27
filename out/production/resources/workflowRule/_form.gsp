<%@ page import="com.rxlogix.enums.GroupType; com.rxlogix.user.Group;" %>
<asset:javascript src="app/pvs/workflow/workflow_rule.js"/>
<g:javascript>
            var isAdmin = ${isAdmin};
</g:javascript>

<g:if test="${edit}">
    <g:hiddenField name="id" value="${workflowRuleInstance?.id}"/>
    <g:hiddenField name="version" value="${workflowRuleInstance?.version}"/>
</g:if>

<div class="row form-group" id="dispositionId">
    <div class="col-lg-3">
        <label for="name"><g:message code="app.workflow.name.label" default="Name"/></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="name" value="${workflowRuleInstance?.name}"
                     class="form-control" maxlength="255"/>
    </div>

    <div class="col-lg-3">
        <label for="incomingDisposition.id"><g:message code="workflowRule.incomingDisposition" default="Incoming Disposition"/></label>
        <g:select name="incomingDisposition.id" class="form-control select2" from="${availableDispositions}" optionKey="id"
                  value="${workflowRuleInstance?.incomingDisposition?.id}" optionValue="displayName"/>
    </div>


    <div class="col-lg-3">
        <label for="targetDisposition.id"><g:message code="workflowRule.targetDisposition" default="Target Disposition"/></label>
        <g:select name="targetDisposition.id" class="form-control select2" from="${availableDispositions}"
                  optionKey="id" value="${workflowRuleInstance?.targetDisposition?.id}" optionValue="displayName"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="workflowRule.description" default="Description"/></label>
        <g:textArea name="description" value="${workflowRuleInstance?.description}"
                     class="form-control workflow-rule-description"/>
    </div>

    <div class="col-lg-6">
        <div class="col-lg-12 form-pv">
            <div class="checkbox checkbox-primary checkbox-inline">
                <g:checkBox name="display" id="displayPVSState" value="${workflowRuleInstance?.display}" class=""/>
                <label for="displayPVSState"><g:message code="label.display" default="Display"/></label>
            </div>
        </div>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="workflowGroups"><g:message code="workflow.rule.workflow.groups.label"/></label>
        <g:select name="workflowGroups" id="select-workflow-groups" from="${Group.findAllByGroupType(GroupType.WORKFLOW_GROUP)}" multiple="true" optionKey="id"
                  optionValue="name" value="${workflowRuleInstance?.workflowGroups*.id}" class="form-control"/>
    </div>
</div>


<div class="row form-group">
    <div class="col-lg-6">
        <label for="allowedUserGroups"><g:message code="workflow.rule.allowed.user.groups.label"/></label>
        <g:select name="allowedUserGroups" id="select-groups" from="${Group.findAllByGroupType(GroupType.USER_GROUP)}" multiple="true" optionKey="id"
                  optionValue="name" value="${workflowRuleInstance?.allowedUserGroups*.id}" class="form-control"/>
    </div>

    <div class="col-lg-2 form-pv">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="notify" id="notify" value="${workflowRuleInstance?.notify}"/>
            <label for="notify"><g:message code="notify.alert.assignee"/></label>
        </div>
    </div>
</div>

<sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
    <div class="row form-group">
        <div class="col-lg-5">
            <g:if test="${edit}">
                <span class="button disable-check" data-action="update"><g:actionSubmit class="save btn btn-primary" action="update"
                                                     value="${message(code: 'default.button.update.label', 'default': 'Update')}"/></span>
                <span class="button disable-check" data-action="delete"><g:actionSubmit class="delete btn pv-btn-grey" action="delete"
                                                     value="${message(code: 'default.button.delete.label', 'default': 'Delete')}"/></span>
            </g:if>
            <g:else>
                <span class="button"><g:submitButton name="create" class="save btn btn-primary"
                                                     value="${message(code: 'default.button.create.label', 'default': 'Create')}"/></span>
            </g:else>
        </div>
    </div>
</sec:ifAnyGranted>
