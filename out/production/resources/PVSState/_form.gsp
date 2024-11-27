<%@ page import="com.rxlogix.enums.GroupType; com.rxlogix.user.Group; com.rxlogix.config.PVSState" %>



<div class="fieldcontain ${hasErrors(bean: PVSStateInstance, field: 'description', 'error')} required">
	<label for="description">
		<g:message code="PVSState.description.label" default="Description" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="description" value="${fieldValue(bean: PVSStateInstance, field: 'description')}" />

</div>

<div class="fieldcontain ${hasErrors(bean: PVSStateInstance, field: 'display', 'error')} ">
	<label for="display">
		<g:message code="PVSState.display.label" default="Display" />
		
	</label>
	<g:checkBox name="display" value="${PVSStateInstance?.display}" />

</div>

<div class="fieldcontain ${hasErrors(bean: PVSStateInstance, field: 'displayName', 'error')} required">
	<label for="displayName">
		<g:message code="PVSState.displayName.label" default="Display Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="displayName" value="${fieldValue(bean: PVSStateInstance, field: 'displayName')}" />

</div>

<div class="fieldcontain ${hasErrors(bean: PVSStateInstance, field: 'finalState', 'error')} ">
	<label for="finalState">
		<g:message code="PVSState.finalState.label" default="Final State" />
		
	</label>
	<g:checkBox name="finalState" value="${PVSStateInstance?.finalState}" />

</div>

<div class="fieldcontain ${hasErrors(bean: PVSStateInstance, field: 'value', 'error')} required">
	<label for="value">
		<g:message code="PVSState.value.label" default="Value" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="value" value="${fieldValue(bean: PVSStateInstance, field: 'value')}" />

</div>

<div class="fieldcontain ${hasErrors(bean: PVSStateInstance, field: 'groups', 'error')} ">
	<label for="groups">
		Groups
		<g:message code="groups.label" default="Groups" />

	</label>
	<g:select name="groups" from="${Group.findAllByGroupType(GroupType.USER_GROUP)}" multiple="multiple" optionKey="id" size="5" value="${PVSStateInstance?.groups*.id}" class="many-to-many"/>

</div>

