<%@ page import="com.rxlogix.config.Disposition; com.rxlogix.config.SignalStrategy; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.signal.AdHocAlertType; com.rxlogix.config.Priority; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.AlertAttributesService; com.rxlogix.SafetyLeadSecurityService;" %>
<g:set var="grailsApplication" bean="grailsApplication"/>

                        <g:if test="${entry?.enabled == true && entry?.name == 'haSignalStatus'}">

                                <div class="col-xs-12 form-group  ${hasErrors(bean: alertInstance, field: 'haSignalStatus', 'has-error')}">
                                    <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                                    <g:select name="haSignalStatus"
                                              from="${Disposition.findAllByDisplay(true).sort({ it.value })}"
                                              optionKey="id"
                                              optionValue="value"
                                              value="${alertInstance?.haSignalStatus?.id}"
                                              noSelection="${['': message(code: 'select.one')]}"
                                              class="form-control"/>
                                </div>
                        </g:if>
                        <g:if test="${entry?.enabled == true && entry?.name == 'haDateClosed'}">

                            <div class="col-xs-12 form-group">
                                <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>

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

                        </g:if>
                    <g:if test="${entry?.enabled == true && entry?.name == 'commentSignalStatus'}">
                        <div class="form-group">
                            <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                            <g:textArea name="commentSignalStatus"
                                        class="form-control ta-min-height">${alertInstance?.commentSignalStatus}</g:textArea>
                        </div>
                    </g:if>