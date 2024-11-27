<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.user.Group" %>

<g:if test="${edit}">
    <g:hiddenField name="id" value="${PVSStateInstance?.id}"/>
    <g:hiddenField name="version" value="${PVSStateInstance?.version}"/>
</g:if>

<div class="row form-group">
    <div class="col-lg-3">
        <label for="value"><g:message code="PVSState.value" default="Value"/></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="value" value="${fieldValue(bean: PVSStateInstance, field: 'value')}" class="form-control"
                     disabled="${!SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}"/>
    </div>

    <div class="col-lg-3">
        <label for="displayName"><g:message code="PVSState.displayName" default="Display Name"/></label>
        <label><span class="required-indicator">*</span></label>
        <g:textField name="displayName" value="${fieldValue(bean: PVSStateInstance, field: 'displayName')}"
                     class="form-control"
                     disabled="${!SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}"/>
    </div>

</div>

<div class="row form-group">
    <div class="col-lg-3">
        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="display" id="displayPVSState" value="${PVSStateInstance?.display}" class=""
                        disabled="${!SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}"/>
            <label for="displayPVSState"><g:message code="PVSState.display" default="Display"/></label>
        </div>

        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="finalState" id="finalState" value="${PVSStateInstance?.finalState}" class=""
            disabled="${!SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}"/>
            <label for="finalState"><g:message code="PVSState.finalState" default="Final State"/></label>
        </div>
    </div>

    <div class="col-lg-3">
        <label for="displayName">Review Period(In Days)</label>
        <g:textField name="reviewPeriod" value="${fieldValue(bean: PVSStateInstance, field: 'reviewPeriod')}"
                     class="form-control"
                     disabled="${!SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}"/>

    </div>
</div>

<div class="row form-group">
    <div class="col-lg-6">
        <label for="description"><g:message code="PVSState.description" default="Description"/></label>
        <g:textField name="description" value="${fieldValue(bean: PVSStateInstance, field: 'description')}"
                     class="form-control"
                     disabled="${!SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")}"/>
    </div>
</div>


<div class="row form-group">
    <div class="col-lg-5">
        <g:if test="${edit}">
            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                <span class="menuButton"><g:actionSubmit class="save btn btn-primary" action="update"
                                                         value="${message(code: 'update', 'default': 'Update')}"/></span>
            </sec:ifAnyGranted>
        </g:if>
        <g:else>
            <span class="button"><g:submitButton name="create" class="save btn btn-primary"
                                                 value="${message(code: 'create', 'default': 'Create')}"/></span>
        </g:else>
    </div>
</div>
