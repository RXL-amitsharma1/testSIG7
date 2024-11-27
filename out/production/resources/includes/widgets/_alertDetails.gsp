<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils;com.rxlogix.user.Group; com.rxlogix.enums.GroupType; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.Constants;" %>
<div class="panel panel-default rxmain-container rxmain-container-top">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            <a data-toggle="collapse" data-parent="#accordion-pvs-form" href="#pvsAlertDetails" aria-expanded="true" class="">
                <g:message code="app.label.alert.details"/>
            </a>
        </h4>
    </div>
    <div id="pvsAlertDetails" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true">
        <div class="row">
            %{--Report Name--}%
            <div class="col-xs-4">
                <div class="${hasErrors(bean: configurationInstance, field: 'name', 'has-error')} row">
                    <div class="col-xs-12 form-group">
                        <label><g:message code="app.label.alert.name"/><span class="required-indicator">*</span></label>
                        <g:if test="${actionName=='copy'}">
                            <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}" class="form-control"
                                   maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Configuration', field: 'name')}"
                                   value=""/>
                        </g:if>
                        <g:else>
                            <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}" class="form-control"
                                   maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Configuration', field: 'name')}"
                                   value="${configurationInstance?.name}"/>
                        </g:else>
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-12 form-group ${hasErrors(bean: configurationInstance, field: 'assignedTo', 'has-error')}">
                        <g:initializeAssignToElement bean="${configurationInstance}" isClone = "${clone}" currentUser = "${currentUser}"/>
                    </div>
                </div>
            </div>
            %{--Public--}%
            <div class="col-xs-4">
                <div class="row">
                    <div class="form-group col-xs-12 ${hasErrors(bean: configurationInstance, field: 'priority', 'has-error')} priority-List">
                        <label><g:message code="app.label.priority" /><span class="required-indicator">*</span></label>
                        <g:select class="form-control select2" name="priority" value="${configurationInstance.adhocRun ? null : configurationInstance?.priority?.id}" optionKey="id" noSelection="['null':message(code:'select.one')]" optionValue="value" from="${priorityList}"
                        disabled="${configurationInstance?.adhocRun}"/>
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-12">
                        <g:initializeShareWithElement bean="${configurationInstance}" isClone = "${clone}" currentUser = "${currentUser}"/>
                    </div>
                </div>
            </div>
            %{--Description--}%
            <div class="col-xs-4">
                <div class="row">
                    <div class="col-xs-8">
                        <label for="description"><g:message code="app.label.reportDescription"/></label>
                    </div>
                    <div class="col-xs-4">
                        <g:if test="${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURE_TEMPLATE_ALERT")}">
                            <label class="checkbox-inline no-bold add-margin-bottom " style="margin-bottom: 5px;">
                                <g:checkBox name="isTemplateAlert" value="${configurationInstance?.isTemplateAlert}"
                                            checked="${configurationInstance?.isTemplateAlert}" disabled="${configurationInstance?.adhocRun}" />
                                <g:message code="app.label.templateAlert"/>
                            </label>
                        </g:if>
                    </div>
                </div>

            <div class="row">
                <div class="col-xs-12">
                    <g:textArea name="description"
                                maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Configuration', field: 'description')}"
                                class="form-control"
                                style="height: 110px;">${configurationInstance?.description}</g:textArea>
                </div>
            </div>
        </div>
        </div>
    </div>
</div>
