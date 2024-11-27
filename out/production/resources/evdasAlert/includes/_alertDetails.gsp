<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; com.rxlogix.util.DateUtil" %>
<div class="rxmain-container rxmain-container-top">
    <g:set var="userService" bean="userService"/>
    <g:hiddenField name="owner" id="owner" value="${configurationInstance?.owner?.id ?: userService.getUser()?.id}"/>

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.alert.details"/>
            </label>
        </div>

        <div class="rxmain-container-content">

            <div class="row">

                <div class="col-xs-4">
                    <div class="row">
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
                    %{--Share With--}%
                    <div class="row">
                        <div class="col-xs-12 form-group ${hasErrors(bean: configurationInstance, field: 'sharedGroups', 'has-error')}">
                            <g:initializeAssignToElement bean="${configurationInstance}" isClone="${clone}" currentUser="${currentUser}"/>
                        </div>
                    </div>
                </div>

                <div class="col-xs-4">
                    <div class="row">
                        <div class="form-group col-xs-12 ${hasErrors(bean: configurationInstance, field: 'priority', 'has-error')} priority-List">
                            <label><g:message code="app.label.priority" /><span class="required-indicator">*</span></label>
                            <g:select class="form-control select2" name="priority" value="${configurationInstance.adhocRun ? null : configurationInstance?.priority?.id}" optionKey="id" noSelection="['null':message(code:'select.one')]" optionValue="value" from="${priorityList}" />
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-12">
                            <g:initializeShareWithElement bean="${configurationInstance}" isClone="${clone}" currentUser="${currentUser}"/>
                        </div>
                    </div>
                </div>

                <div class="col-xs-4">
                    <label for="description"><g:message code="app.label.reportDescription"/></label>
                    <g:textArea name="description"
                                maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Configuration', field: 'description')}"
                                class="form-control"
                                style="height: 87px;">${configurationInstance?.description}</g:textArea>
                </div>


            </div>


        </div>

    </div>
</div>
