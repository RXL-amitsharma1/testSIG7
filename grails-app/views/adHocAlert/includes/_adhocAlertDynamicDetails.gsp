<%@ page import="com.rxlogix.config.SignalStrategy; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.signal.AdHocAlertType; com.rxlogix.config.Priority; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.AlertAttributesService; com.rxlogix.SafetyLeadSecurityService;grails.util.Holders;" %>
<g:set var="grailsApplication" bean="grailsApplication" />
<g:set var="alertAttributesService" bean="alertAttributesService"/>

          %{--Report Name--}%
            <g:if test="${entry?.enabled == true && entry?.name == 'name'}">
                <div class="${hasErrors(bean: alertInstance, field: 'name', 'has-error')} row">
                    <div class="form-group">
                        <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span>
                        </label>
                        <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}"
                               class="form-control"
                               value="${alertInstance?.name}"/>
                    </div>
                </div>
            </g:if>
            <g:if test="${entry?.enabled == true && entry?.name == 'priority'}">
                <div class="${hasErrors(bean: alertInstance, field: 'priority', 'has-error')}">
                    <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>

                    <g:if test="${!editMode || (!alertInstance.productSelection || safetyLeadSecurityService.isUserSafetyLead(userService?.getUser(), alertInstance))}">
                        <g:select name="priority" from="${Priority.findAllByDisplay(true).sort({it.value.toUpperCase()})}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${alertInstance?.priority?.id}"
                                  noSelection="${['': message(code: 'select.one')]}"
                                  class="form-control"/>
                    </g:if>
                    <g:else>
                        <input type="text"
                               class="form-control"
                               disabled="disabled"
                               value="${alertInstance?.priority?.displayName}"/>
                        <g:hiddenField name="priority"  value="${alertInstance?.priority?.id}"/>
                    </g:else>
                </div>
            </g:if>
            <g:if test="${entry?.enabled == true && entry?.name == 'assignedToValue'}">
                    <div class="form-group ${hasErrors(bean: alertInstance, field: 'assignedTo', 'has-error')}">
                        <g:initializeAssignToElement bean="${alertInstance}"/>
                    </div>
            </g:if>

            <g:if test="${entry?.enabled == true && entry?.name == 'detectedDate'}">

                            <div class="form-group ${hasErrors(bean: alertInstance, field: 'detectedDate', 'has-error')}">
                                <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                                <div class="fuelux">
                                    <div class="datepicker toolbarInline" id="detectedDatePicker">
                                        <div class="input-group">
                                            <input placeholder="Select Date"
                                                   class="form-control" id="adhocDetectedDate"
                                                   name="detectedDate" type="text"/>
                                            <g:render id="myDetectedDate"
                                                      template="/includes/widgets/datePickerTemplate"/>
                                        </div>
                                    </div>
                                </div>
                                <g:hiddenField name="myDetectedDate" value="${alertInstance?.detectedDate ?: null}"/>
                            </div>

            </g:if>
                        <!-- Detected By -->
            <g:if test="${entry?.enabled == true && entry?.name == 'detectedBy'}">
                        <div class="form-group ${hasErrors(bean: alertInstance, field: 'detectedBy', 'has-error')}">
                            <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                            <g:select id="detectedBy" name="detectedBy"
                                      from="${alertAttributesService?.get('detectedBy')}"
                                      value="${alertInstance?.detectedBy}"
                                      noSelection="${['': message(code: 'select.one')]}"
                                      class="form-control"/>
                        </div>
            </g:if>

                <!-- Aggregate Report Start Date -->
                <g:if test="${entry?.enabled == true && entry?.name == 'aggReportStartDate'}">
                        <div class="form-group ${hasErrors(bean: alertInstance, field: 'aggReportStartDate', 'has-error')}">
                            <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>

                            <div class="fuelux">
                                <div class="datepicker toolbarInline" id="aggStartDatePicker">
                                    <div class="input-group">
                                        <input placeholder="Select Date"
                                               class="form-control"
                                               name="aggReportStartDate" type="text"/>
                                        <g:render id="myAggStartDate"
                                                  template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                            <g:hiddenField name="myAggStartDate" value="${alertInstance?.aggReportStartDate ?: null}"/>
                        </div>
                </g:if>
                <!-- Aggregate Report End Date -->
                <g:if test="${entry?.enabled == true && entry?.name == 'aggReportEndDate'}">
                        <div class="form-group ${hasErrors(bean: alertInstance, field: 'aggReportEndDate', 'has-error')}">
                            <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>

                            <div class="fuelux">
                                <div class="datepicker toolbarInline" id="aggEndDatePicker">
                                    <div class="input-group">
                                        <input placeholder="Select Date"
                                               class="form-control"
                                               name="aggReportEndDate" type="text"/>
                                        <g:render id="myAggEndDate"
                                                  template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                            <g:hiddenField name="myAggEndDate" value="${alertInstance?.aggReportEndDate ?: null}"/>
                        </div>
                </g:if>
            %{--Evaluation Type--}%
            <g:if test="${entry?.enabled == true && entry?.name == 'initialDataSource'}">
                <div class="form-group ${hasErrors(bean: alertInstance, field: 'initialDataSource', 'has-error')}">
                    <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span>
                    </label>
                    <g:select id="initialDataSource" name="initialDataSource"
                              from="${alertAttributesService?.get('initialDataSource')}"
                              value="${alertInstance?.initialDataSource}"
                              noSelection="${['': message(code: 'select.one')]}"
                              class="form-control"/>
                </div>

            </g:if>
            <g:if test="${entry?.enabled == true && entry?.name == 'sharedWith'}">
                    <div class="form-group">
                        <g:initializeShareWithElement bean="${alertInstance}"/>
                    </div>
            </g:if>
            <g:if test="${entry?.enabled == true && entry?.name == 'populationSpecific'}">
                <div class="form-group">
                    <g:set var="psVal" value="${alertAttributesService?.getDefault("populationSpecific")}"/>
                    <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                    <g:select name="populationSpecific" from="${alertAttributesService?.get('populationSpecific')}"
                              value="${alertInstance?.getAttr('populationSpecific') ?: psVal}"
                              noSelection="${psVal ? null : ['': message(code: 'select.one')]}"
                              class="form-control"/>
            </div>
            </g:if>
                <g:if test="${entry?.enabled == true && entry?.name == 'evaluationMethods'}">
                    <div class="form-group">
                        <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span>
                        </label>
                        <g:select name="evaluationMethods" from="${alertAttributesService?.get('evaluationMethods')}"
                                  value="${alertInstance?.getAttr('evaluationMethods')}"
                                  class="form-control" multiple="true"/>
                    </div>
                </g:if>
                <g:if test="${entry?.enabled == true && entry?.name == 'lastDecisionDate'}">
                    <div class="form-group ${hasErrors(bean: alertInstance, field: 'lastDecisionDate', 'has-error')}">
                        <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>

                        <div class="fuelux">
                            <div class="datepicker toolbarInline" id="lastDecisionDatePicker">
                                <div class="input-group">
                                    <input placeholder="Select Date"
                                           class="form-control"
                                           name="lastDecisionDate" type="text"/>
                                    <g:render id="lastDecisionDate"
                                              template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                        <g:hiddenField name="myLastDecisionDate" value="${alertInstance?.lastDecisionDate ?: null}"/>
                    </div>
                </g:if>
                <g:if test="${entry?.enabled == true && entry?.name == 'actionTaken'}">
                    <div class="form-group">
                        <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                        <g:select name="actionTaken" from="${alertAttributesService.get('actionsTaken')}"
                                  value="${alertInstance?.actionTaken}"
                                  class="form-control"
                                  multiple="true"/>
                    </div>
                </g:if>
                %{--Share With--}%

            %{--Description--}%
            <g:if test="${entry?.enabled == true && entry?.name == 'issuePreviouslyTracked'}">
            <div id="check-public" class="pos-ab ">

                <g:checkBox name="issuePreviouslyTracked" value="${alertInstance?.issuePreviouslyTracked}"/>
                <label for="issuePreviouslyTracked">${entry?.label}</label>

            </div>
            </g:if>
            <g:if test="${entry?.enabled == true && entry?.name == 'description'}">
                    <div class="form-group">
                        <label for="description">${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                        <g:textArea name="description"
                                    class="form-control ta-min-height"
                                    >${alertInstance?.description}</g:textArea>
                    </div>
            </g:if>

            <g:if test="${entry?.enabled == true && entry?.name == 'notes'}">
                    <div class="form-group">
                        <label>${entry?.label}<span class="required-indicator">${entry?.mandatory == true ? '*' : ''}</span></label>
                        <g:textArea name="notes"
                                    class="form-control ta-min-height"
                                    >${alertInstance?.notes}</g:textArea>
                    </div>
            </g:if>
