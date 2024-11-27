<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil" %>
<div class="panel panel-default rxmain-container rxmain-container-top">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            <a data-toggle="collapse" data-parent="#accordion-pvs-form" href="#pvsAlertDetails" aria-expanded="true"
               class="">
                <g:message code="app.label.alert.details"/>
            </a>
        </h4>
    </div>

    <div id="pvsAlertDetails" class="panel-collapse rxmain-container-content rxmain-container-show collapse in"
         aria-expanded="true">
        <div class="row">
            %{--Report Name--}%
            <div class="col-xs-4">
                <div class="row">
                    <div class="col-xs-12 form-group">
                        <label><g:message code="app.label.alert.name"/><span class="required-indicator">*</span></label>
                        <g:if test="${actionName == 'copy'}">
                            <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}"
                                   maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.ExecutedLiteratureConfiguration', field: 'name')}"
                                   class="form-control" , id="name"
                                   value=""/>
                        </g:if>
                        <g:else>
                            <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}"
                                   maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.ExecutedLiteratureConfiguration', field: 'name')}"
                                   class="form-control" , id="name" value="${configurationInstance?.name}"/>
                        </g:else>
                    </div>
                </div>

                <div class="row">
                    <g:initializeShareWithElement bean="${configurationInstance}" isClone="${clone}" currentUser="${currentUser}"/>
                </div>
            </div>

            %{--Priority--}%
            <div class="col-xs-4">
                <div class="row">
                    <div class="form-group col-xs-12 ${hasErrors(bean: configurationInstance, field: 'priority', 'has-error')} priority-List">
                        <label><g:message code="app.label.priority"/><span class="required-indicator">*</span></label>
                        <g:select class="form-control select2" name="priority" value="${configurationInstance?.priority?.id}"
                                  optionKey="id" noSelection="['null': message(code: 'select.one')]" optionValue="value"
                                  from="${priorityList}"/>
                    </div>
                </div>
            </div>

            %{--Assigned To--}%
            <div class="col-xs-4">
                <div class="row">
                    <div class="col-xs-12  ${hasErrors(bean: configurationInstance, field: 'assignedTo', 'has-error')}">
                        <g:initializeAssignToElement bean="${configurationInstance}" isClone="${clone}" currentUser="${currentUser}"/>
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>

