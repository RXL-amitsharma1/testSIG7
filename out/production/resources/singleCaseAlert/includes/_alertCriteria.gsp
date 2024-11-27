<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper;  grails.util.Holders" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            <g:render template="/includes/widgets/product_study_generic-selection" bean="${configurationInstance}"
                      model="[theInstance: configurationInstance, showHideGenericVal: 'hide']"/>

            <div class="col-md-3">
                 <label><g:message code="app.label.eventSelection"/></label>

                <label class="checkbox-inline no-bold add-margin-bottom" style="margin-bottom: 5px;">
                    <g:checkBox name="limitPrimaryPath" value="${configurationInstance?.limitPrimaryPath}"
                            checked="${configurationInstance?.limitPrimaryPath}"/>
                    <g:message code="app.label.eventSelection.limit.primary.path"/>
                </label>

            <div class="wrapper">
                <div id="showEventSelection" class="showDictionarySelection"></div>

                <div class="iconSearch">
                    <a id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0"
                       data-toggle="tooltip" title="Select event" accesskey="}"><i class="fa fa-search"></i></a>
                </div>

            </div>
                <g:textField name="eventSelection" value="${configurationInstance?.eventSelection}" hidden="hidden"/>
            </div>

            <div class="col-md-3">
            <label><g:message code="app.label.DateRangeType"/><span class="required-indicator">*</span></label>
                <g:select name="dateRangeType" id="dateRangeType"
                          from="${ViewHelper.getDateRangeTypeI18n()}"
                          optionValue="display" optionKey="name"
                          value="${configurationInstance?.dateRangeType}"
                          class="form-control select2"/>

                <div class="m-t-15">
                    <label>Evaluate Case Date On</label>
                     <div id="evaluateDateAsDiv">
                        <g:select name="evaluateDateAsNonSubmission"
                                  from="${ViewHelper.getEvaluateCaseDateI18n()}"
                                  optionValue="display" optionKey="name"
                                  value="${configurationInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs select2"/>

                    </div>
                    <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden" value="${configurationInstance?.evaluateDateAs}"/>

                    <div style="margin-top: 10px">
                        <div class="fuelux">
                            <div class="datepicker toolbarInline" id="asOfVersionDatePicker" hidden="hidden">
                                <div class="input-group">
                                    <g:hiddenField name="asOfVersionDateValue"  value="${configurationInstance?.asOfVersionDate ?: null}"/>
                                    <input placeholder="${message(code:"select.version")}"
                                           class="form-control" id="asOfVersionDateId"
                                           name="asOfVersionDate" type="text"/>

                                    <g:render id="asOfVersion"
                                              template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>

            <div class="hidden hidden-field" style="margin-top: 10px">
                <label>Include Cases Modified On/After</label>
                <div class="fuelux">
                    <div class="datepicker toolbarInline" id="onOrAfterDatePicker">
                        <div class="input-group">

                            <input placeholder="Select Date"
                                   class="form-control"
                                   name="onOrAfterDate" type="text"
                                   value="${configurationInstance?.onOrAfterDate ?: null}" />
                            <g:render id="onOrAfter"
                                      template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>

                </div>
            </div>
                <label><g:message code="app.label.limit.to.case.series"/></label>
                <g:select name="limitToCaseSeries"
                          from="${[]}"
                          class="form-control select2"/>
            </div>

            <div class="col-md-3">

                <label><g:message code="app.label.inclusionOptions"/><span class="required-indicator"/><span class="required-indicator"/></label>

                <div class="checkbox checkbox-primary">

                        <g:checkBox id="excludeFollowUp"
                                    onclick="checkValueForDateRangeCheckbox()"
                                    name="excludeFollowUp"
                                    value="${configurationInstance?.excludeFollowUp}"
                                    checked="${configurationInstance?.excludeFollowUp}"/>
                        <label for="excludeFollowUp">
                            <g:message code="reportCriteria.exclude.follow.up"/>
                        </label>
                </div>

                 <g:if test="${Holders.config.alertStopList}">
                <div class="checkbox checkbox-primary">
                        <g:checkBox id="applyAlertStopList"
                                    name="applyAlertStopList"
                                    value="${configurationInstance?.applyAlertStopList}"
                                    checked="${configurationInstance?.applyAlertStopList}" />
                        <label for="applyAlertStopList">
                            <g:message code="reportCriteria.exclude.non.valid.cases"/>
                        </label>
                </div>
                </g:if>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="adhocRun"
                                name="adhocRun"
                                value="${configurationInstance?.adhocRun}"
                                checked="${configurationInstance?.adhocRun}"
                                disabled="${(action.equals("edit") && configurationInstance?.adhocRun)}"/>

                    <label for="adhocRun">
                        <g:message code="app.label.configuration.on.demand.run"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeNonValidCases"
                                name="excludeNonValidCases"
                                value="${configurationInstance?.excludeNonValidCases}"
                                checked="${configurationInstance?.excludeNonValidCases}"/>
                    <label for="excludeNonValidCases">
                        <g:message code="app.label.configuration.exclude.invalid.cases"/>
                    </label>

                </div>

                <g:if test="${Holders.config.configurations.include.locked.versions.enabled}">
                    <div class="checkbox checkbox-primary includeLockedClass">
                        <g:checkBox id="includeLockedVersion"
                                    name="includeLockedVersion"
                                    value="${configurationInstance?.includeLockedVersion}"
                                    checked="${configurationInstance?.includeLockedVersion}"/>
                        <label for="includeLockedVersion">
                            <g:message code="reportCriteria.include.locked.versions.only"/>
                        </label>

                    </div>
                </g:if>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="missedCases"
                                name="missedCases"
                                value="${configurationInstance?.missedCases}"
                                checked="${configurationInstance?.missedCases}"/>
                    <label for="missedCases">
                        <g:message code="app.label.configuration.missed.cases"/>
                    </label>

                </div>
            </div>
        </div>

        <div class="row">

            <div class="col-md-3">
                <label>
                    <g:message code="app.label.chooseAQuery"/>
                </label>

                <div class="row queryContainer">
                    <g:if test="${editMode}">
                        <div>
                            <i class="fa fa-refresh fa-spin loading"></i>
                        </div>
                    </g:if>

                    <div class="doneLoading" style="padding-bottom: 5px;">
                        <g:select name="alertQuery" from="${[]}" class="form-control alertQuery"/>
                    </div>
                    <g:hiddenField name="alertQueryName" id="alertQueryName" value="${configurationInstance?.alertQueryName}"/>
                    <g:hiddenField name="alertQueryId" id="alertQueryId" value="${configurationInstance?.alertQueryId}"/>
                </div>
            </div>

            <div class="col-md-6">
                <g:render template="/singleCaseAlert/alertDateRange"
                          model="[configurationInstance: configurationInstance]"/>
            </div>
            <div class="col-md-3 ${hasErrors(bean: configurationInstance, field: 'dispositions', 'has-error')} hidden">
                <label><g:message code="app.signal.case.review.status"/></label>

                <div>
                    <g:select id="dispositions" name="dispositions"
                              from="${com.rxlogix.config.Disposition.findAll()}"
                              value="${configurationInstance?.dispositions?.id}"
                              optionKey="id"
                              optionValue="displayName"
                              multiple="true"
                              class="form-control dispositions select2"/>
                </div>
            </div>

        </div>

        <div class="row">
            <div class="alertQueryWrapper col-md-9">
                <div class="queryExpressionValues">
                    <g:if test="${configurationInstance?.alertQueryValueLists?.size() > 0}">
                        <g:each var="qvl" in="${configurationInstance.alertQueryValueLists}"  status="i">
                            <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                <g:if test="${qev?.hasProperty('reportField')}">
                                    <g:render template='/query/toAddContainerQEV' model="['type':'qev','qev': qev, 'i':i, 'j':j]"/>
                                </g:if>
                                <g:else>
                                    <g:render template='/query/customSQLValue' model="['type':'qev','qev': qev, 'i':i, 'j':j]"/>
                                </g:else>
                            </g:each>
                        </g:each>
                    </g:if>
                </div>
                <g:hiddenField class="validQueries" name="validQueries" value="${configurationInstance?.getQueriesIdsAsString()?:''}" />
            </div>
        </div>

       <g:render template="/includes/widgets/alertThresholds" model="['theInstance':configurationInstance]" />


    </div>
</div>
<input type="hidden" id="isIncludeLockedVersions" value="${Holders.config.configurations.include.locked.versions.enabled}"/>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList,isPVCM:isPVCM]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal" model="[sMQList: sMQList]"/>
    <g:render template="/includes/modals/product_selection_modal" />
    <g:render template="/includes/modals/study_selection_modal" />
</g:else>
<g:render template="/includes/modals/product_group_selection_modal" model="[productGroupList: productGroupList]"/>

