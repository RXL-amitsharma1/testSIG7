<%@ page import="com.rxlogix.config.ActionType" %>

 <g:if test="${edit}">
	<g:hiddenField name="id" value="${actionTypeInstance?.id}" />
	<g:hiddenField name="version" value="${actionTypeInstance?.version}" />
</g:if>

<div class="row form-group">
	<div class="col-lg-3">
		<label for="value"><g:message code="app.label.name" default="Name" />:<span class="required-indicator">*</span></label>
		<g:textField name="value" value="${actionTypeInstance?.value}" class="form-control"/>
	</div>
	<div class="col-lg-3">
		<label for="displayName"><g:message code="label.displayName" default="Display Name" />:<span class="required-indicator">*</span></label>
		<g:textField name="displayName" value="${actionTypeInstance?.displayName}" class="form-control"/>
	</div>
</div>

<div class="row form-group">
	<div class="col-lg-6">
		<label for="description"><g:message code="label.description" default="Description" />:</label>
		<g:textField  maxlength="255" name="description" value="${actionTypeInstance?.description}"
					 class="form-control"/>
	</div>
</div>

<div class="row form-group">
	<div class="col-lg-1">
		<g:if test="${edit}">
			<span class="button"><g:actionSubmit class="save btn btn-primary actionTypeSaveButton" action="update" value="${message(code: 'default.button.update.label', 'default': 'Update')}" /></span>
		</g:if>
		<g:else>
			<span class="button"><g:submitButton name="create" class="save btn btn-primary actionTypeSaveButton" value="${message(code: 'default.button.create.label', 'default': 'Create')}" /></span>
		</g:else>
	</div>
</div>
