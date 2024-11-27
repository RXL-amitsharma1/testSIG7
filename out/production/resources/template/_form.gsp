<%@ page import="com.rxlogix.user.User; com.rxlogix.config.ReportField; com.rxlogix.config.Category" %>

<g:set var="userService" bean="userService"/>
<g:hiddenField name="owner" id="owner" value="${reportTemplateInstance?.owner?.id ?: userService.getUser().id}"/>
<div class="row">
    <div class="col-xs-3">
        <div class="row">
            <div class="col-xs-12 ${hasErrors(bean: reportTemplateInstance, field: "name", "has-error")}">
                <label><g:message code="app.label.templateName" /><span class="required-indicator">*</span></label>
                <g:textField name="name"
                             placeholder="${g.message(code: 'input.name.placeholder')}"
                             maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.ReportTemplate', field: 'name')}"
                             value="${reportTemplateInstance?.name}"
                             class="form-control" />
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.description" /></label>
                <g:textField name="description"
                             maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.ReportTemplate', field: 'description')}"
                             value="${reportTemplateInstance?.description}"
                             class="form-control" />
            </div>
        </div>
    </div>
    <div class="col-xs-3">
        <div class="row select2-padding-bottom">
            <div class="col-xs-12">
                <label><g:message code="app.label.category" /></label>
                <g:select id="category"
                          name="category.id"
                          from="${Category.findAll()}"
                          value="${reportTemplateInstance?.category?.id}"
                          optionKey="id"
                          optionValue="name"
                          noSelection="['':message(code:'select.category')]"
                          class="form-control"/>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <g:render template="/includes/widgets/tagsSelect2" model="['domainInstance': reportTemplateInstance]" />
            </div>
        </div>
    </div>

    <sec:ifAnyGranted roles="ROLE_ADMIN">

        <div class="col-xs-3">
             <div class="row">
                 <label><g:message code="app.label.owner"/></label>
                 <input disabled type="text" name="owner" class="form-control"
                        value="${reportTemplateInstance?.owner?.fullName ?: userService.getUser().fullName}"/>
             </div>
        </div>
        </div>


        <div class="col-xs-3">
            <div class="row">
                <div class="col-xs-12">
                    <div class="checkbox">
                        <label>
                            <g:checkBox name="isPublic" value="${reportTemplateInstance?.isPublic}" checked="${reportTemplateInstance?.isPublic}"/><g:message code="app.label.public" />
                            <div class="col-xs-12">
                        </label>
                    </div>
                </div>
            </div>
        </div>
    </sec:ifAnyGranted>


</div>
<div class="row">
    <div class="col-xs-12 col-lg-12">
        <g:render template="includes/templateType" model="['reportTemplateInstance':reportTemplateInstance]" />
    </div>
</div>