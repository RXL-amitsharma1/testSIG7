<%@ page import="com.rxlogix.config.Disposition; com.rxlogix.config.SignalStrategy; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.signal.AdHocAlertType; com.rxlogix.config.Priority; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.AlertAttributesService; com.rxlogix.SafetyLeadSecurityService;" %>
<g:set var="grailsApplication" bean="grailsApplication"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.ha.signal.status"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <div class="col-xs-4">

                    <div class="row">
                        <div class="col-xs-12 form-group  ${hasErrors(bean: alertInstance, field: 'haSignalStatus', 'has-error')}">
                            <label><g:message code="app.label.ha.signal.status"/></label>
                            <g:select name="haSignalStatus"
                                      from="${Disposition.findAllByDisplay(true).sort({ it.value })}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${alertInstance?.haSignalStatus?.id}"
                                      noSelection="${['': message(code: 'select.one')]}"
                                      class="form-control"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12 form-group">
                            <label><g:message code="app.label.ha.date.closed"/></label>

                            <div class="fuelux">
                                <div class="datepicker toolbarInline" id="haDateClosedDatePicker">
                                    <div class="input-group">
                                        <input placeholder="Select Date"
                                               class="form-control"
                                               name="haDateClosed" type="text"/>
                                        <g:render id="myHADateClosed"
                                                  template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                            <g:hiddenField name="myHADateClosed" value="${alertInstance?.haDateClosed ?: null}"/>
                        </div>
                    </div>

                </div>

                <div class="col-xs-8">
                    <label><g:message code="app.label.ha.signal.status.comments"/></label>
                    <g:textArea name="commentSignalStatus"
                                class="form-control"
                                style="height: 110px;">${alertInstance?.commentSignalStatus}</g:textArea>
                </div>

                %{--<div class="col-xs-4">--}%
                    %{--<label><g:message code="app.label.action.taken"/></label>--}%
                    %{--<g:select name="actionTaken" from="${grailsApplication.config.configurations.actionsTaken}"--}%
                              %{--value="${alertInstance?.actionTaken}"--}%
                              %{--class="form-control"--}%
                              %{--multiple="true"/>--}%
                %{--</div>--}%
            </div>

        </div>
    </div>
</div>