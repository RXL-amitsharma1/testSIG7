<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            %{--Product Selection/Study Selection--}%
            <div class="col-md-4">
                <label class="radio-inline labelBold">
                    <input type="radio" name="optradio" class="productRadio" checked="checked">
                    <g:message code="app.label.productSelection"/>
                </label>

                <label class="radio-inline labelBold">
                    <input type="radio" name="optradio" class="studyRadio">
                    <g:message code="app.label.studySelection"/>
                </label>



                <div class="wrapper">
                    <div id="showProductSelection" class="showDictionarySelection"></div>

                    <div class="iconSearch">
                        <i class="fa fa-search" data-toggle="modal" data-target="#productModal"></i>
                    </div>
                </div>

                <div class="wrapper" hidden="hidden">
                    <div id="showStudySelection" class="showDictionarySelection"></div>

                    <div class="iconSearch">
                        <i class="fa fa-search" data-toggle="modal" data-target="#studyModal"></i>
                    </div>
                </div>

                <g:hiddenField name="productSelection" value="${configurationInstance?.productSelection}"/>
                <g:hiddenField name="isMultiIngredient" value="${configurationInstance?.isMultiIngredient}"/>
                <g:hiddenField name="studySelection" value="${configurationInstance?.studySelection}"/>


                <div class="row" hidden="hidden">
                    <div class="col-md-12">
                        <!-- TODO: For debugging use only; type="hidden" for this input field after we are done needing to see it -->
                        <input name="JSONExpressionValues" id="JSONExpressionValues" value=""/>
                        <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer"><g:message code="prettify.my.json.here"/></a>
                    </div>
                </div>

            </div>

            %{--Event Selection--}%
            <div class="col-md-3">
                 <label><g:message code="app.label.eventSelection"/></label>

                <div class="wrapper">
                    <div id="showEventSelection" class="showDictionarySelection"></div>

                    <div class="iconSearch">
                        <i class="fa fa-search" id="searchEvents" data-toggle="modal" data-target="#eventModal"></i>
                    </div>
                </div>
                <g:textField name="eventSelection" value="${configurationInstance?.eventSelection}" hidden="hidden"/>

            </div>

            %{--Date Range Type--}%
            <div class="col-md-3">
                <label><g:message code="app.label.DateRangeType"/></label>
                <g:select name="dateRangeType" id="dateRangeType"
                          from="${ViewHelper.getDateRangeTypeI18n()}"
                          optionValue="display" optionKey="name"
                          value="${configurationInstance?.dateRangeType}"
                          class="form-control"/>


                %{--Evaluate Case Date On--}%
                <div style="margin-top: 23px;">
                    <label>Evaluate Case Date On</label>
                    <div id="evaluateDateAsDiv">
                        <g:select name="evaluateDateAsNonSubmission"
                                  from="${ViewHelper.getEvaluateCaseDateI18n()}"
                                  optionValue="display" optionKey="name"
                                  value="${configurationInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs"/>

                    </div>
                    <div id="evaluateDateAsSubmissionDateDiv">

                        <g:select name="evaluateDateAsSubmissionDate"
                                  from="${ViewHelper.getEvaluateCaseDateSubmissionI18n()}"
                                  optionValue="display" optionKey="name"
                                  value="${configurationInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs"/>

                    </div>
                    <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden" value="${configurationInstance?.evaluateDateAs}"/>

                    %{--Date Picker--}%
                    <div style="margin-top: 10px">
                        <div class="fuelux">
                            <div class="datepicker toolbarInline" id="asOfVersionDatePicker">
                                <div class="input-group">
                                    <g:hiddenField name="asOfVersionDateValue"  value="${configurationInstance?.asOfVersionDate ?: null}"/>
                                    <input placeholder="${message(code:"select.version")}"
                                           class="form-control"
                                           name="asOfVersionDate" type="text"/>

                                    <g:render id="asOfVersion"
                                              template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>

                        </div>
                    </div>


                </div>

                %{--On Or After Date Picker--}%
                <div style="margin-top: 10px">
                    <label>Include Cases Modified On/After</label>
                    <div class="fuelux">
                        <div class="datepicker toolbarInline" id="editOnOrAfterDatePicker">
                            <div class="input-group">
                                <input class="form-control"
                                       name="editOnOrAfterDate" type="text"
                                       value="${configurationInstance?.onOrAfterDate ?: null}" />
                                <g:render id="editOnOrAfter"
                                          template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>

                    </div>
                </div>


            </div>

            %{--Inclusion Options--}%
            <div class="col-md-2">

                <label><g:message code="app.label.inclusionOptions"/></label>

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

                <div id="lockedVersionOnly" class="checkbox checkbox-primary">
                    <g:checkBox id="includeLockedVersion"
                                onclick="checkValueForDateRangeCheckbox()"
                                name="includeLockedVersion"
                                value="${configurationInstance?.includeLockedVersion}"
                                checked="${configurationInstance?.includeLockedVersion}"/>
                    <label for="lockedVersionOnly">
                        <g:message code="reportCriteria.include.locked.versions.only"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeNonValidCases"
                                onclick="checkValueForDateRangeCheckbox()"
                                name="excludeNonValidCases"
                                value="${configurationInstance?.excludeNonValidCases}"
                                checked="${configurationInstance?.excludeNonValidCases}"/>
                    <label for="excludeNonValidCases">
                        <g:message code="reportCriteria.exclude.non.valid.cases"/>
                    </label>
                </div>

            </div>

        </div>

        <g:if test="${actionName == 'edit'}">
            <div class="row">
                <div class="col-md-8">
                    <div id="fillInQuery" hidden="hidden">
                        <div id="showBlankExpressions" class="queryExpressionValues"></div>

                        <div hidden="hidden"><g:render template="/query/toAddContainer"/></div>
                    </div>
                </div>
            </div>
        </g:if>

    </div>

    <g:render template="/includes/widgets/alertThresholds" model="['theInstance':configurationInstance]" />

</div>

%{--todo: This is for edit only--}%

<div class="modal fade" id="eventModal" tabindex="-1" role="dialog" aria-labelledby="eventDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="eventDictionaryLabel"><g:message code="app.reportField.eventDictId"/></h4>
            </div>

            <div class="modal-body">
                <g:render template="eventDictionaryTemplate"/>
            </div>

            <div class="modal-footer">
                %{--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--}%
                <button type="button" class="btn btn-default clearEventValues"><g:message code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addEventValues"><g:message code="default.button.add.label"/></button>
                <button type="button" class="btn btn-default addAllEvents" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="productModal" tabindex="-1" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="productDictionaryLabel"><g:message code="app.reportField.productDictionary"/></h4>
            </div>

            <div class="modal-body">
                <g:render template="productDictionaryTemplate"/>
            </div>

            <div class="modal-footer">
                %{--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--}%
                <button type="button" class="btn btn-default clearProductValues"><g:message code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addProductValues"><g:message code="default.button.add.label"/></button>
                <button type="button" class="btn btn-default addAllProducts" data-dismiss="modal"><g:message code="default.button.close.label"/> </button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="studyModal" tabindex="-1" role="dialog" aria-labelledby="studyDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="studyDictionaryLabel"><g:message code="app.reportField.studyDictionary"/></h4>
            </div>

            <div class="modal-body">
                <g:render template="studyDictionaryTemplate"/>
            </div>

            <div class="modal-footer">
                %{--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--}%
                <button type="button" class="btn btn-default clearStudyValues"><g:message code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addStudyValues"><g:message code="default.button.add.label"/></button>
                <button type="button" class="btn btn-default addAllStudies" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>

