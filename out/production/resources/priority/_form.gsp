<%@ page import="com.rxlogix.config.Priority" %>


<g:if test="${edit}">
    <g:hiddenField name="id" value="${priorityInstance?.id}" />
    <g:hiddenField name="version" value="${priorityInstance?.version}" />
</g:if>
<style>
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
}
input[type=number] {
    -moz-appearance: textfield;
}
.select2-search__field {
     width: 0 !important;
}
.countBox{
    font-weight: 800 !important;
}
</style>

<div class="col-lg-6">

<div class="row form-group">
    <div class="col-lg-6">
        <label for="value"><g:message code="app.label.value" default="Value" /></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="value" value="${priorityInstance.value}"
                     class="form-control"/>
    </div>
    <div class="col-lg-6">
        <label for="displayName"><g:message code="app.label.display.name" default="Display Name" /></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="displayName" value="${priorityInstance.displayName}"
                     class="form-control"/>
    </div>

</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="reviewPeriod"><g:message code="app.label.review.period" default="Default Review Period(Days)" /></label>
        <g:field type="number" min="1" name="reviewPeriod" value="${priorityInstance.reviewPeriod ?: 1}"
                     class="form-control" step="1"/>
    </div>
    <div class="col-lg-6">
        <label for="description"><g:message code="workflow.description" default="Description" /></label>
        <g:textField name="description" value="${priorityInstance.description}"
                     class="form-control"/>
    </div>
</div>

<div class="row form-group">
    <div class="col-lg-2">
        <label for="displayName"><g:message code="app.label.priority.order" /></label>
        <g:select name="priorityOrder" value="${priorityInstance.priorityOrder}"
                  from="${(1..20)}" class="form-control"/>
    </div>
    <div class="col-lg-4">
        <label for="iconClass"><g:message code="label.iconClass" default="Icon" /></label>
        <g:textField name="iconClass" value="${priorityInstance.iconClass}"
                     class="form-control"/>
    </div>
</div>


<div class="row form-group">
    <div class="col-lg-6">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="display" id="displayPriority" value="${priorityInstance?.display}" checked="${priorityInstance?.display}" />
            <label for="displayPriority"><g:message code="app.label.display.display" default="Display" />  </label>
        </div>
        <!-- defaultPriority -->
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox id="defaultPriority" name="defaultPriority" value="${priorityInstance?.defaultPriority}" checked="${priorityInstance?.defaultPriority}"/>
            <label for="defaultPriority"><g:message code="com.rxlogix.config.Priority.defaultPriority.label" default="Default Priority" />  </label>
        </div>
    </div>
</div>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">

        <div class="row form-group">
            <div class="col-lg-5">
                <g:if test="${edit}">
                    <span class="button"><g:actionSubmit class="save btn btn-primary" id="submitButton" action="update" value="${message(code: 'update', 'default': 'Update')}" /></span>
                </g:if>
                <g:else>
                    <span class="button"><g:submitButton name="create" class="save btn btn-primary" id="submitButton" value="${message(code: 'create', 'default': 'Create')}" /></span>
                </g:else>
            </div>
        </div>

    </sec:ifAnyGranted>
</div>
<div class="col-lg-6  hidden-item" id="dispostionContainer">
    <div class="row form-group m-b-0 " id="labelsDisposition">
        <div class="col-lg-1">
        </div>

        <div class="col-lg-6">
            <label for="displayName">Disposition</label>
            <label><span class="required-indicator">*</span></label>
        </div>

        <div class="col-lg-3">
            <label for="reviewPeriod">Review Period(Days)</label>
            <label><span class="required-indicator">*</span></label>
        </div>
    </div>
<g:each in="${dispositionConfigs}" status="i" var="config">
    <div class="row form-group dispositionRow" >
        <div class="col-lg-1">
        </div>
        <div class="col-lg-6">
            <g:select  name="displayNameDisposition" multiple="true" from="${config.displayName}" value="${config.displayName}" class="form-control select2 dispositionSelect">
            </g:select>
        </div>
        <div class="col-lg-3">
            <input type="number" name="reviewPeriodDisposition" value="${config[0].reviewPeriod}" class="form-control reviewPeriodDisposition" min="1" autocomplete = "off" value="{{reviewPeriod}}"/>
        </div>

        <div class="col-lg-1 removeDisposition">
            <div class="pull-right">
                <span class="glyphicon glyphicon-minus btn btn-inverse minusExpression" onclick="removeDispositionElement(this)"></span>
            </div>
        </div>
    </div>
</g:each>
</div>
<textarea name="dispositions" class="hidden-item"></textarea>




