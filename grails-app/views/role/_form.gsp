<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<div class="row">
    <div class="col-md-6">

        <div class="form-group">
            <label class="col-md-4 control-label" for="authority">
                <g:message code="role.authority.label"/><span class="required-indicator">*</span>
            </label>

            <div class="col-md-8">
                <g:textField name="authority" maxlength="${gorm.maxLength(clazz: 'com.rxlogix.user.Role', field: 'authority')}"
                             value="${roleInstance?.authority}"
                             placeholder="${message(code: 'group.name.label')}"
                             class="form-control"/>
            </div>
        </div>

        <div class="form-group">
            <label class="col-md-4 control-label" for="description"><g:message code="role.description.label"/></label>

            <div class="col-md-8">
                <g:textArea name="description" value="${roleInstance?.description}"
                            rows="5" cols="40" maxlength="${gorm.maxLength(clazz: 'com.rxlogix.user.Role', field: 'description')}"
                            placeholder="${message(code: 'role.description.label')}"
                            class="form-control"/>
                <small class="text-muted">Max: ${gorm.maxLength(clazz: 'com.rxlogix.user.Role', field: 'description')} characters</small>

            </div>
        </div>

    </div>

</div>