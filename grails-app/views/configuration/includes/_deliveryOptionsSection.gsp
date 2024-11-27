<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil" %>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-right fa-lg click" onclick="hideShowContent(this);"></i>
            <label class="rxmain-container-header-label click" onclick="hideShowContent(this);">
                <g:message code="app.label.alert.details"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">

                %{--Report Name--}%
                <div class="col-xs-4">
                    <div class="${hasErrors(bean: configurationInstance, field: 'name', 'has-error')}">
                        <label><g:message code="app.label.name"/><span class="required-indicator">*</span></label>
                        <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}" class="form-control"
                               maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Configuration', field: 'name')}"
                               value="${configurationInstance?.name}"/>
                    </div>

                    <div>&nbsp;</div>

                    %{--Tags--}%
                    <g:render template="/includes/widgets/tagsSelect2"
                              model="['domainInstance': configurationInstance]"/>

                </div>

                %{--Description--}%
                <div class="col-xs-4">
                 <label for="description"><g:message code="app.label.reportDescription"/></label>
                    <g:textArea name="description"
                                maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.Configuration', field: 'description')}"
                                class="form-control"
                                style="height: 110px;">${configurationInstance?.description}
                    </g:textArea>
                </div>

                %{--Public--}%
                <div class="col-xs-4">
                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <label class="no-bold">
                            <g:checkBox name="isPublic" value="${configurationInstance?.isPublic}"
                                        checked="${configurationInstance?.isPublic}"/>
                            <g:message code="app.label.public"/>
                        </label>
                    </sec:ifAnyGranted>


                </div>

                %{--<div class="col-xs-4">
                    <label for="attachments">Attachments</label>
                    <input multiple class="multi" type="file" name="attachments">

                </div>--}%
                </div>

            </div>

        </div>

        <div class="row">
            <div class="col-md-12 card-box">
                <div class="row">
                    <div class="col-xs-4">
                        %{--Send To Inbox Of--}%
                        <div class="row">
                            <div class="col-xs-12">
                                <label>
                                    <g:message code="deliveryOptions.send.to.inbox.of" />
                                </label>
                            </div>
                        </div>

                        %{--Share With--}%
                        <div class="row">
                            <div class="col-xs-12 ${hasErrors(bean: configurationInstance, field: 'deliveryOption.sharedWith', 'has-error')}">
                                <g:if test="${createMode}">
                                    <g:select id="shareWith"
                                              name="deliveryOption.sharedWith"
                                              from="${userService.getActiveUsers()}"
                                              optionKey="id"
                                              optionValue="fullNameAndUserName"
                                              value="${userService.getUser().id}"
                                              noSelection="${['': message(code:'select.one')]}"
                                              class="form-control" multiple="true"/>
                                </g:if>

                                <g:if test="${editMode}">
                                    <g:select id="shareWith"
                                              name="deliveryOption.sharedWith"
                                              from="${userService.getActiveUsers()}"
                                              optionKey="id"
                                              optionValue="fullNameAndUserName"
                                              value="${configurationInstance?.deliveryOption?.sharedWith?.id}"
                                              noSelection="${['': message(code:'select.one')]}"
                                              class="form-control" multiple="true"/>
                                </g:if>
                            </div>
                        </div>

                        %{--Email to--}%
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.emailTo"/></label>
                            </div>
                        </div>

                        %{--Email to--}%
                        <div class="row">
                            <div class="col-xs-12">
                                %{--<g:select id="emailUsers"
                                          name="deliveryOption.emailToUsers"
                                          from="${userService?.getAllEmails(configurationInstance) ?: null}"
                                          value="${configurationInstance?.deliveryOption?.emailToUsers}"
                                          noSelection="${['': message(code:'select.one')]}"
                                          class="form-control" multiple="true"/>--}%
                            </div>
                        </div>

                        %{--Attachments--}%
                        <div id="attachmentCheckboxes">
                            <div class="row">
                                <div class="col-xs-12">
                                    <label><g:message code="deliveryOptions.email.attachment.format" /></label>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-xs-12">
                                    <g:each in="${com.rxlogix.enums.ReportFormat.values()}">
                                        <label class="no-bold">
                                            <g:checkBox class="emailOption"
                                                        name="deliveryOption.attachmentFormats"
                                                        value="${it}"
                                                        checked="${configurationInstance?.deliveryOption?.attachmentFormats?.contains(ReportFormat?.valueOf(it.toString())) ? true : false}"/>
                                            ${message(code: it.i18nKey)}
                                        </label>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                    </div>

                    %{--Scheduler--}%
                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-3 fuelux" id="schedule">
                                %{--
                                    This has to be checked if this can be reused.
                                    Right now this can only be accessed from the configuration page because
                                    the page is not open to all the view pages.
                                --}%
                                %{--The Markup code--}%
                                <g:render template="/includes/schedulerTemplate"/>
                                <g:hiddenField name="isEnabled" id="isEnabled" value="${configurationInstance?.isEnabled}"/>
                                <g:hiddenField name="schedulerTime"
                                               value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(userService.getUser())}"/>
                                <g:hiddenField name="scheduleDateJSON"
                                               value="${configurationInstance?.scheduleDateJSON ?: null}"/>
                                <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone"
                                       value="${ViewHelper.getUserTimeZoneForConfig(configurationInstance,userService.getUser())}"/>
                                <input type="hidden" id="timezoneFromServer" name="timezone"
                                       value="${DateUtil.getTimezone(userService.getUser())}"/>
                            </div>
                        </div>

                        %{--For Edit only--}%
                        <div class="row">
                            <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                                <div class="col-xs-8  nextScheulerInfo ${hasErrors(bean: configurationInstance, field: 'nextRunDate', 'has-error')}">
                                    <label><g:message code="app.label.nextScheduledRunDate"/></label>

                                    <div>
                                        <g:render template="/includes/widgets/dateDisplayWithTimezone" model="[date:configurationInstance?.nextRunDate]"/>
                                    </div>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
