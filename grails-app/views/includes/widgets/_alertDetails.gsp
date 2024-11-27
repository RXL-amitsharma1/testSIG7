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
                        <label><g:message code="app.label.alert.name"/><span class="required-indicator">*</span>
                        </label>                          <a href="javascript:void(0)"
                                                             class="glyphicon glyphicon-info-sign themecolor"
                                                             data-toggle="modal"
                                                             id="infoLink"
                                                             data-target="#myModal4" style="display: none;"></a>
                        <g:if test="${actionName == 'copy'}">
                            <input type="text" name="name" id="nameTextbox" placeholder="${g.message(code: 'input.name.placeholder')}"
                                   class="form-control"
                                   maxlength="200"
                                   value=""/>
                        </g:if>
                        <g:else>
                            <input type="text" name="name" id="nameTextbox" placeholder="${g.message(code: 'input.name.placeholder')}" class="form-control"
                                   maxlength="200"
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
            <div class="modal fade" id="myModal4" role="dialog">
                <div class="modal-dialog" style="width:930px">

                    <!-- Modal content-->
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                            <h4 class="modal-title">Alert Name help</h4>
                        </div>

                    <div class="modal-body">
                        <table class="table table-bordered">
                            <tbody>
                            <p>Available Placeholder Tags for Alert Name:</p>
                            <ul>
                                <li><strong>[Product Name]:</strong> This mandatory tag must be included in the alert name. The system will replace it with the actual drug name for which data mining is performed. The maximum length for this tag is 80 characters.</li>

                                <li><strong>[Assigned To]:</strong> The system will substitute this tag with the name of the user or user group to whom the alert is assigned. The maximum length for this tag is 80 characters.</li>

                                <li><strong>[Product Hierarchy]:</strong> This tag will be replaced with the product hierarchy relevant to the data mining performed. The maximum length for this tag is 20 characters.</li>

                                <li><strong>[Event Hierarchy]:</strong> The system will replace this tag with the event hierarchy associated with the data mining process. The maximum length for this tag is 15 characters.</li>

                                <li><strong>[Product Type]:</strong> This tag will be substituted with the product type specified in the alert configuration. The maximum length for this tag is 20 characters.</li>

                                <li><strong>[Alert Group]:</strong> The system will replace this tag with the configured alert group name for the product assignment. The maximum length for this tag is 20 characters.</li>
                            </ul>

                            <p><strong>Example:</strong> The alert name format <strong>[Product Name]-[Assigned To]-[Product Hierarchy]-[Event Hierarchy]-[Product Type]</strong> will be replaced with:
                                <br>"Paracetamol-Safety Scientist-Ingredient-PT-Drug(S+C)".</p>
                            </tbody>
                        </table>
                    </div>


                    <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">Ok</button>
                        </div>
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
