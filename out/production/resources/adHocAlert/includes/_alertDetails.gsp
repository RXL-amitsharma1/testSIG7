<%@ page import="com.rxlogix.config.SignalStrategy; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.signal.AdHocAlertType; com.rxlogix.config.Priority; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormat; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil; com.rxlogix.AlertAttributesService; com.rxlogix.SafetyLeadSecurityService;grails.util.Holders;" %>
<g:set var="grailsApplication" bean="grailsApplication" />
<g:set var="alertAttributesService" bean="alertAttributesService"/>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.alert.workflow"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                  <div class="col-xs-3 ${hasErrors(bean: alertInstance, field: 'priority', 'has-error')}">
                      <label><g:message code="app.label.priority"/><span class="required-indicator">*</span></label>

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

                <div class="col-xs-3">
                            <g:set var="psVal" value="${alertAttributesService?.getDefault("populationSpecific")}"/>
                            <label><g:message code="app.label.population.specific" /><span class="required-indicator"></span></label>
                            <g:select name="populationSpecific" from="${alertAttributesService?.get('populationSpecific')}"
                                      optionValue="value"
                                      value="${alertInstance?.getAttr('populationSpecific') ?: psVal}"
                                      noSelection="${psVal ? null : ['': message(code: 'select.one')]}"
                                      class="form-control"/>
                  </div>
            </div>

        </div>
    </div>
</div>

<div class="panel panel-default rxmain-container rxmain-container-top">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            <a data-toggle="collapse" data-parent="#accordion-pvs-form" href="#pvsAlertDetails" aria-expanded="true" class="">
                <g:message code="app.label.alert.details"/>
            </a>
        </h4>
    </div>
    <div id="pvsAlertDetails" class="panel-collapse rxmain-container-content rxmain-container-show collapse in pos-rel" aria-expanded="true">
        <div class="row">
            %{--Report Name--}%
            <div class="col-xs-4">
                <div class="${hasErrors(bean: alertInstance, field: 'name', 'has-error')} row">
                    <div class="col-xs-12 form-group">
                        <label><g:message code="app.label.alert.name"/><span class="required-indicator">*</span>
                        </label>
                        <input type="text" name="name" placeholder="${g.message(code: 'input.name.placeholder')}"
                               class="form-control"
                               value="${alertInstance?.name}"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-12 form-group ${hasErrors(bean: alertInstance, field: 'assignedTo', 'has-error')}">
                        <g:initializeAssignToElement bean="${alertInstance}"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-12 row">
                            <div class="col-xs-6 form-group ${hasErrors(bean: alertInstance, field: 'detectedDate', 'has-error')}">
                                <label>${Holders.config.alert.adhoc.custom.fields.detected.date}<span
                                    class="required-indicator">*</span></label>
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
                        <!-- Detected By -->
                        <div class="col-xs-6 form-group ${hasErrors(bean: alertInstance, field: 'detectedBy', 'has-error')}">
                            <label><g:message code="app.label.detected.by" /><span class="required-indicator">*</span></label>
                            <g:select id="detectedBy" name="detectedBy"
                                      from="${alertAttributesService?.get('detectedBy')}"
                                      optionValue="value"
                                      value="${alertInstance?.detectedBy}"
                                      noSelection="${['': message(code: 'select.one')]}"
                                      class="form-control"/>
                        </div>

                    </div>
                </div>
            <g:if test="${Holders.config.alert.adhoc.custom.fields.enabled == true}">
                <div class="row">
                    <div class="col-xs-12 row">
                        <!-- Aggregate Report Start Date -->
                        <div class="col-xs-6 form-group ${hasErrors(bean: alertInstance, field: 'aggReportStartDate', 'has-error')}">
                            <label><g:message code="app.label.aggregate.report.start.date"/></label>

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

                        <!-- Aggregate Report End Date -->
                        <div class="col-xs-6 form-group ${hasErrors(bean: alertInstance, field: 'aggReportEndDate', 'has-error')}">
                            <label><g:message code="app.label.aggregate.report.end.date"/></label>

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

                    </div>
                </div>
            </g:if>
            </div>
            %{--Priority--}%
            <div class="col-xs-4">


                %{--Evaluation Type--}%

            <div class="row">
                <div class="col-xs-12 form-group ${hasErrors(bean: alertInstance, field: 'initialDataSource', 'has-error')}">
                    <label><g:message code="app.label.initial.datasource"/><span class="required-indicator">*</span>
                    </label>
                    <g:select id="initialDataSource" name="initialDataSource"
                              from="${alertAttributesService?.get('initialDataSource')}"
                              optionValue="value"
                              value="${alertInstance?.initialDataSource}"
                              noSelection="${['': message(code: 'select.one')]}"
                              class="form-control"/>
                </div>

            </div>

                <div class="row">
                    <div class="col-xs-12 form-group">
                        <g:initializeShareWithElement bean="${alertInstance}"/>
                    </div>
                </div>
            <g:if test="${Holders.config.alert.adhoc.custom.fields.enabled == true}">
                <div class="row">
                    <div class="form-group col-xs-12">
                        <label><g:message code="app.label.evaluationType"/><span class="required-indicator"></span>
                        </label>
                        <g:select name="evaluationMethods" from="${alertAttributesService?.get('evaluationMethods')}"
                                  optionValue="value"
                                  value="${alertInstance?.getAttr('evaluationMethods')}"
                                  class="form-control" multiple="true"/>
                    </div>
                </div>
            <div class="row">
                    <div class="col-xs-6 form-group ${hasErrors(bean: alertInstance, field: 'lastDecisionDate', 'has-error')}">
                        <label><g:message code="app.label.last.decision.date"/></label>

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

                    <div class="col-xs-6 form-group">
                        <label><g:message code="app.label.action.taken"/></label>
                        <g:select name="actionTaken" from="${alertAttributesService.get('actionsTaken')}"
                                  value="${alertInstance?.actionTaken}"
                                  class="form-control"
                                  multiple="true"/>
                    </div>
                </div>
            </g:if>

                %{--Share With--}%

            </div>

            %{--Description--}%
            <div class="col-xs-4">
            <div id="check-public" class="pos-ab ">

                <g:checkBox name="issuePreviouslyTracked" value="${alertInstance?.issuePreviouslyTracked}"/>
                <label for="issuePreviouslyTracked"><g:message code="issue.previously.tracked" /></label>

            </div>

                <div class="row">
                    <div class="col-xs-12 form-group">
                        <label for="description"><g:message code="app.label.reportDescription"/><span class="required-indicator"/></label>
                        <g:textArea name="description"
                                    class="form-control ta-min-height"
                                    >${alertInstance?.description}</g:textArea>
                    </div>
                </div>



                <div class="row">
                    <div class="col-xs-12 form-group">
                        <label><g:message code="app.label.comments"/></label>
                        <g:textArea name="notes"
                                    class="form-control ta-min-height"
                                    >${alertInstance?.notes}</g:textArea>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
