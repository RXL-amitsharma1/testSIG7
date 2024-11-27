<%@ page import="com.rxlogix.config.ActionConfiguration" %>

<%@ page import="com.rxlogix.config.ActionType" %>

<g:if test="${edit}">
    <g:hiddenField name="id" value="${actionConfigurationInstance?.id}"/>
    <g:hiddenField name="version" value="${actionConfigurationInstance?.version}"/>
</g:if>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="value"><g:message code="action.configuration.name" default="Name"/>
            <span class="required-indicator">*</span></label>
        <g:textField name="value" value="${actionConfigurationInstance?.value}"  maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.ActionConfiguration', field: 'value')}"
                     class="form-control"/>
    </div>

    <div class="col-lg-3">
        <label for="displayName"><g:message code="action.configuration.display.name" default="Display Name"/>
            <span class="required-indicator">*</span></label>
        <g:textField name="displayName" value="${actionConfigurationInstance?.displayName}" maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.ActionConfiguration', field: 'displayName')}"
                     class="form-control"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="action.configuration.description" default="Description"/></label>
        <g:textField name="description" value="${actionConfigurationInstance?.description}" maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.ActionConfiguration', field: 'description')}"
                     class="form-control"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="isEmailEnabled"><g:message code="action.configuration.notify.by.email" default="Notify By Email"/>:</label>
        <input type="checkbox" name="isEmailEnabled" id="isEmailEnabled"
            ${actionConfigurationInstance.isEmailEnabled ? 'checked' : ''}/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-1">
        <g:if test="${edit}">
            <span class="button"><g:actionSubmit class="save btn btn-primary saveActionConfigButton" action="update"
                                                 value="${message(code: 'default.button.update.label', 'default': 'Update')}"/></span>
        </g:if>
        <g:else>
            <span class="button"><g:submitButton name="create" class="save btn btn-primary saveActionConfigButton"
                                                 value="${message(code: 'default.button.create.label', 'default': 'Create')}"/></span>
        </g:else>
    </div>
</div>
