<%@ page import="com.rxlogix.util.DateUtil;grails.util.Holders;com.rxlogix.signal.SignalOutcome" %>
<div class="row">
    <div class="col-md-3 form-group">
        <input type="hidden" value="${validatedSignal.dateCreated}" id="signalCreatedDate">
        <label class="">
            <g:message code="signal.configuration.products.label"/>
            <span class="required-indicator">*</span> <span class="fa fa-flag required-indicator"></span>
        </label>

        <div class="wrapper">
            <div id="showProductSelection" class="showDictionarySelection"></div>

            <div class="iconSearch">
                <a data-toggle="modal" data-target="#productModal" tabindex="0" data-toggle="tooltip"
                   title="Search product"  accesskey="p"><i class="fa fa-search"></i></a>
            </div>
        </div>
        <g:hiddenField name="productSelection" value="${validatedSignal?.products}"/>
        <g:hiddenField name="isValidatedSignal" value="${true}"/>
    </div>

    <div class="col-md-3 form-group">
        <label>
            <g:message code="app.label.eventSelection"/>
            <span class="required-indicator">*</span>
        </label>

        <div class="wrapper">
            <div id="showEventSelection" class="showDictionarySelection"></div>

            <div class="iconSearch">
                <a id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0" data-toggle="tooltip" title="Select event" accesskey="}"><i class="fa fa-search"></i></a>
            </div>

        </div>
        <g:textField name="eventSelection" value="${validatedSignal?.events}" hidden="hidden"/>
    </div>

    <div class="col-md-3">
        <div class="form-group">
            <label class=""><g:message code="app.label.signal.name"/><span class="required-indicator"> * <span class="fa fa-flag required-indicator"></span></span></label>
            <input class="form-control" value="${validatedSignal?.name}" name="name" id="signalName"/>
        </div>
        <div class="form-group pos-rel">
                <label class=""><g:message code="signal.configuration.linkedSignal.label"/></label>
                <a ${validatedSignal.linkedSignals ? '' : 'hidden="hidden"'} id="linkedSignalInfo" tabindex="0"
                                                                             class="dropdown-toggle"
                                                                             data-toggle="dropdown"
                                                                             role="button">
                    <i class="mdi mdi-arrow-down-drop-circle font-18 blue-1"></i>
                </a>

                <ul class="dropdown-menu dropdown-menu-left viewLinkedSignal" role="menu">
                </ul>
                <select name="linkedSignal" id="linkedSignal" class="form-control" multiple="multiple">
                    <g:each in="${linkedSignals}" var="signal">
                        <option value="${signal.id}" ${signal.id in validatedSignal.linkedSignals*.id ? 'selected="selected"' : ''}>${signal.name}</option>
                    </g:each>
                </select>
            </div>

    </div>

    <div class="col-md-3">
        <div class="row">
            <div class="form-group">
                <label for="initialDataSource"><g:message code="signal.details.signalSource.label"/><span
                        class="required-indicator">*</span> <span class="fa fa-flag required-indicator"></span></label>
                <g:select name="initialDataSource" id="initialDataSource" class="form-control"
                          from="${initialDataSource}"
                          multiple="true"
                          value=""/>
            </div>
            <g:hiddenField name="signalSource" id="signalSource" value="${validatedSignal?.initialDataSource}"/>

            <div class="form-group">
                <div class="fuelux">
                    <div class="datepicker" id="detected-date-picker">
                        <label><g:message code="app.label.detected.date"/> <span class="required-indicator">* <span class="fa fa-flag required-indicator"></span></span>
                        </label>
                        <div class="input-group">
                            <input placeholder="Detected Date" name="detectedDate" id="detectedDate"
                                   class="form-control input-sm detectedDate"
                                   type="text"
                                   data-date="" value="${validatedSignal?.detectedDate?(DateUtil.toDateStringWithoutTimezone(validatedSignal.detectedDate)):""}"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

    <div class="row">
        <div class="col-md-3">
             <div class="form-group">
                <label><g:message code="app.label.evaluationType"/> <span class="fa fa-flag required-indicator"></span>
                </label>
                <g:select name="evaluationMethod" from="${evaluationMethods}"
                          class="form-control" multiple="true"/>
                <g:hiddenField name="signalEvaluationMethod" id="signalEvaluationMethod"
                               value="${validatedSignal?.evaluationMethod}"/>
            </div>

            <div class="form-group">
                <label><g:message code="app.label.action.taken"/> <span class="fa fa-flag required-indicator"></span>
                </label><br>
                <g:select name="actionTaken"
                          from="${actionTakenList}"
                          class="form-control" multiple="true"/>
            </div>
            <g:hiddenField name="signalActionTaken" id="signalActionTaken" value="${validatedSignal?.actionTaken}"/>
        </div>

        <div class="col-md-3">
            <div class="form-group">
                <label for="signalTypeList"><g:message code="signal.configuration.topicCategory.label"/></label>
                <g:select name="signalTypeList" class="form-control" optionKey="name"
                          optionValue="name"
                          from="${signalTypeList}" multiple="true" value=""/>

                <g:hiddenField name="topicCategoryList" id="topicCategoryList"
                               value="${validatedSignal?.topicCategories*.name}"/>

            </div>

            <div class="form-group">
                <label><g:message code="signal.details.signalOutcome.label"/> <span
                        class="fa fa-flag required-indicator"></span></label>
                <input type="hidden" id="existingSignals" value="${validatedSignal.signalOutcomes*.name}">
                <g:select class="form-control" name="signalOutcome" id="signalOutcome" multiple="true" from=""/>
           </div>
        </div>

        <div class="col-md-3">
            <div class="form-group textarea-ext">
                <label for="reasonForEvaluation"><g:message code="signal.details.reasonForEvaluation.label"/> <span class="fa fa-flag required-indicator"></span></label>
                <g:textArea name="reasonForEvaluation"
                            class="form-control"
                            style="height: 90px;">${validatedSignal?.reasonForEvaluation}</g:textArea>
                <a class="btn-text-ext openTextArea" id="resonForEvModal" href="" tabindex="0" title="Open in extended form"><i
                        class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
            </div>
        </div>

        <div class="col-md-3">
            <div class="form-group textarea-ext" >
                <label for="genericComment"><g:message code="signal.details.comments.information"/></label>
                <g:textArea name="genericComment"
                            class="form-control" id="genericComment"
                            style="height: 90px;">${renderFormattedComment(comment:genericComment)}</g:textArea>
                <a class="btn-text-ext openTextArea" id="genCommentModal"  href="" tabindex="0" title="Open in extended form"><i
                        class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
            </div>
        </div>

    %{--<div class="row">
        <g:each var="entry" in="${Holders.config.signal.summary.dynamic.fields}">
            <g:if test="${entry.enabled == true}">
                <g:if test="${entry.fieldName == 'UD_Date1'}">
                    <div class="col-md-3">
                    <div class="form-group">
                        <div class="fuelux">
                            <div class="datepicker">
                                <label>${entry.label}
                                </label>
                                <div class="input-group">
                                    <input placeholder="${entry.label}" name="udDate1" id="udDate1"
                                           class="form-control input-sm detectedDate"
                                           type="text"
                                           data-date="" value="${validatedSignal?.udDate1?(DateUtil.toDateStringWithoutTimezone(validatedSignal.udDate1)):""}"/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    </div>
                </g:if>
                <g:if test="${entry.fieldName == 'UD_Date2'}">
                    <div class="col-md-3">
                    <div class="form-group">
                        <div class="fuelux">
                            <div class="datepicker">
                                <label>${entry.label}
                                </label>
                                <div class="input-group">
                                    <input placeholder="${entry.label}" name="udDate2" id="udDate2"
                                           class="form-control input-sm detectedDate"
                                           type="text"
                                           data-date="" value="${validatedSignal?.udDate2?(DateUtil.toDateStringWithoutTimezone(validatedSignal.udDate2)):""}"/>
                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    </div>
                </g:if>
            </g:if>
        </g:each>
    </div>--}%
    </div>

<g:set var="dd1" value="${false}"/>
<g:set var="dd2" value="${false}"/>
<g:set var="dt1" value="${false}"/>
<g:set var="dt2" value="${false}"/>
<g:set var="ta1" value="${false}"/>
<g:set var="ta2" value="${false}"/>
<g:set var="totalEnabled" value="${0}"/>
<g:set var="allEnabled" value="${new java.util.ArrayList<Map>()}"/>
<g:each var="entry" in="${(Holders.config.signal.summary.dynamic.fields).sort{it.sequence}}">
    <g:if test="${entry.enabled == true}">
        <g:set var="totalEnabled" value="${totalEnabled+1}"/>
        <g:set var="allEnabled" value="${allEnabled << entry}"/>
    </g:if>
</g:each>
<div class="row">
    <g:if test="${totalEnabled>=1}" >
        <div class="col-md-3">
            <g:render template="signalDynamicFields" model='[entry: allEnabled[0], validatedSignal: validatedSignal]'/>

        </div>
    </g:if>
    <g:if test="${totalEnabled>=2}" >
        <div class="col-md-3">
            <g:render template="signalDynamicFields" model='[entry: allEnabled[1], validatedSignal: validatedSignal]'/>
        </div>
    </g:if>
    <g:if test="${totalEnabled>=3}" >
        <div class="col-md-3">
            <g:render template="signalDynamicFields" model='[entry: allEnabled[2], validatedSignal: validatedSignal]'/>

        </div>
    </g:if>
    <g:if test="${totalEnabled>=4}" >
        <div class="col-md-3">
            <g:render template="signalDynamicFields" model='[entry: allEnabled[3], validatedSignal: validatedSignal]'/>

        </div>
    </g:if>
</div>
<div class="row">
    <g:if test="${totalEnabled>=5}" >
        <div class="col-md-3">
            <g:render template="signalDynamicFields" model='[entry: allEnabled[4], validatedSignal: validatedSignal]'/>

        </div>
    </g:if>
    <g:if test="${totalEnabled>=6}" >
        <div class="col-md-3">
            <g:render template="signalDynamicFields" model='[entry: allEnabled[5], validatedSignal: validatedSignal]'/>

        </div>
    </g:if>
    <div class="col-md-12 flag-message"><span class="fa fa-flag required-indicator"></span> Indicate fields which are part of PBRER signal summary appendix</div>

</div>

<g:select id="selectedDatasource" name="selectedDatasource"
          from="${dataSourceMap.entrySet()}"
          optionKey="key" optionValue="value"
          class="form-control selectedDatasourceSignal" style="display: none"/>
<g:hiddenField name="productGroupSelection" id="productGroupSelection" value="${validatedSignal?.productGroupSelection}"/>
<g:hiddenField name="eventGroupSelection" id="eventGroupSelection" value="${validatedSignal?.eventGroupSelection}"/>
<g:hiddenField name="isMultiIngredient" id="isMultiIngredient" value="${validatedSignal?.isMultiIngredient}"/>
<g:hiddenField name="isMultiIngredientAssessment" id ="isMultiIngredientAssessment" value="${validatedSignal?.isMultiIngredient}"/>
<g:hiddenField name="isAssessmentDicitionary" id="isAssessmentDicitionary" value="false"/>

<script>
    var dataSources = ["pva","eudra","faers","vaers","vigibase"];
    var dataSourceObject ={'pva':'Safety DB','eudra':'EVDAS','faers':'FAERS','vaers':'VAERS','vigibase':'VigiBase'};
    $(document).ready(function() {
        $("#selectedDatasource").select2({
            multiple: true,
        });
        $('#productModal').on('show.bs.modal', function(){
            var selectedDatasource=$('#selectDatasource').val()
            if(selectedDatasource){
            var element = $( this );
                element.find('.dictionaryItem').attr( "title", dataSourceObject[selectedDatasource]);
                element.find('#dataSourcesProductDict').val(selectedDatasource).change();
            }
        });
        $('#selectedDatasource').val(dataSources).trigger('change');
        $('#selectedDatasource').next(".select2-container").hide();
    });
</script>