<g:set var="grailsApplication" bean="grailsApplication"/>
<style type="text/css">
input[type=radio] {
    margin: 0;
}
a:focus, input:focus, textarea:focus, select:focus, button:focus, i:focus, .btn:focus, li:focus, span:focus, .form-control:focus {
    outline: 0;
    box-shadow: none!important;
}

    .tag-container .btn-edit-tag, .tag-container .btn-edit-tag i {
        top: calc(54% - 24px) !important;
        left: 80px;
    }
    .tag-container .ico-dots, .tag-container .ico-dots i {
        top: calc(50% - 19px);
        left: 110px;
    }
.tag-container .btn-edit-tag, .tag-container .btn-edit-tag i{
    width:7%!important;
}
</style>
<g:hiddenField name="businessConfigurationId" value="${businessConfiguration?.id}"/>
<div id="row-code-container" class="hide">
    <g:render template="newDesignTemplate"/>
</div>
<g:render template="/includes/modals/common_tag_modal"/>
<div class="row" >
    <div class="col-md-2">
        <label for="ruleName">
            <g:message
                    code="app.label.signal.data.source"/>:${datasourceMap.containsKey(businessConfiguration.dataSource) ? datasourceMap[businessConfiguration.dataSource] : businessConfiguration.dataSource}
        </label>
    </div>

    <div class="col-md-1">
        <label for="ruleName">
            <g:message code="app.label.business.configuration.ruleName"/><span class="required-indicator">*</span>
        </label>
    </div>

    <div class="col-md-2">
        <input type="text" name="ruleName" id="ruleName" class="form-control" value="${ruleInformation?.ruleName}" maxlength="255"/>
    </div>

    %{-- Removed story/PVS-57996- part for drug classification --}%

    <div class="col-md-2">
        <div class="pull-right">
            <input type="checkbox" name="isBreakAfterRule" id="isBreakAfterRule"
                   value="true" ${ruleInformation?.isBreakAfterRule ? "checked" : ""}/>
            <label for="isBreakAfterRule">
                <g:message code="app.label.business.configuration.breakAfterRule"/>
            </label>
        </div>
    </div>

</div>
<br/>
<div class="row">
    <div class="col-md-10">
        <div class="form-group" id="case-alert-type-container">
            <label class="col-md-1 control-label" for="radios">Alert Type</label>

            <div class="col-md-4" id="radios">
                <g:if test="${ruleInformation?.isSingleCaseAlertType}">
                    <g:if test="${(businessConfiguration?.dataSource == com.rxlogix.Constants.DataSource.PVA)}">
                        <input type="radio" id="singleCaseAlertRadionButton" class="caseAlertType"
                               name="isSingleCaseAlert" checked
                               value="true"/>&nbsp; <g:message code="app.label.single.case.alert.rule"/> &nbsp;
                    </g:if>
                    <input type="radio" id="aggregateAlertRadionButton" class="caseAlertType"
                           name="isSingleCaseAlert"
                           value="false"/> &nbsp;${(businessConfiguration?.dataSource == com.rxlogix.Constants.DataSource.EUDRA) ? "${g.message(code: 'app.label.agg.evdas.rule')}" : "${g.message(code: 'app.label.agg.alert.rule')}"}
                </g:if>
                <g:else>
                    <g:if test="${(businessConfiguration?.dataSource == com.rxlogix.Constants.DataSource.PVA)}">
                        <input type="radio" id="singleCaseAlertRadionButton" class="caseAlertType"
                           name="isSingleCaseAlert"
                           value="true"/>&nbsp; <g:message code="app.label.single.case.alert.rule"/> &nbsp;
                    </g:if>
                    <input type="radio" id="aggregateAlertRadionButton" class="caseAlertType"
                           name="isSingleCaseAlert" checked
                           value="false"/> &nbsp;${(businessConfiguration?.dataSource == com.rxlogix.Constants.DataSource.EUDRA) ? "${g.message(code: 'app.label.agg.evdas.rule')}" : "${g.message(code: 'app.label.agg.alert.rule')}"}
                </g:else>
            </div>
        </div>
    </div>
    <div class="col-md-2">
        <div class="pull-right">
            <span class="glyphicon glyphicon-plus btn btn-primary m-b-10" id="addExpression"></span>
        </div>
    </div>
</div>

<div id="row-container" class="business-configuration-border">
    <legend style="font-size: 17px"><g:message code="app.label.rules"/>
        <span class="required-indicator">*</span>
    </legend>
    <div id="builderAll" class="builderAll"></div>
</div>
<br/>
<div class="business-configuration-border" style="margin: 10px 1px">
    <legend style="font-size: 17px">
        <g:message code="app.label.action"/>
        <span class="required-indicator">*</span>
    </legend>

    <div class="row">
        <div class="col-md-6">
            <div class="row">
                <div class="col-md-6 form-group" id="dispositionId">
                    <label for="disposition">
                        <g:message code="app.label.business.configuration.disposition"/>
                    </label>
                    <select id="dispositionAction" name="disposition" class="form-control select2">
                        <option value="">-Select-</option>
                        <g:each in="${dispositionList}" var="disposition">
                            <g:if test="${disposition?.id == ruleInformation?.disposition?.id}">
                                <option value="${disposition.id}"
                                        data-is-validation-confirmed="${disposition.isValidationConfirmed}" selected>
                                    ${disposition.name}</option>
                            </g:if>
                            <g:else>
                                <option value="${disposition.id}"
                                        data-is-validation-confirmed="${disposition.isValidationConfirmed}">
                                    ${disposition.name}</option>
                            </g:else>
                        </g:each>
                    </select>
                </div>

                <div class="col-md-6 form-group" id="bussJustificationId">
                    <label for="justificationText">
                        <g:message code="app.label.business.configuration.justification"/>
                    </label>
                    <g:select id="justificationAction" name="justification" from="${justificationList}" optionKey="id"
                              optionValue="name" value="${selectedJustification?.id}"
                              class="form-control" noSelection="['': '-Select-']"/>
                </div>
            </div>

            <div class="row">
                %{-- Removed story/PVS-57996- part for drug classification --}%

                <div class="col-md-6">
                    <label for="tags">
                        <g:message code="app.label.tag.column"/>
                    </label>
                    <div id = "tags-business" class="inputBox">
                    </div>
                </div>
            </div>
            <div class="row" id="attach-signal-container"
                 style="display:${ruleInformation?.disposition?.validatedConfirmed ? 'block' : 'none'}">
                <div class="col-md-6">
                    <label for="signal">Signal</label>
                    <g:select name="signal" class="form-control select2" from="${signalList}"
                              value="${ruleInformation?.signal}"
                              optionKey="name" noSelection="['': '-Select Signal-']" optionValue="name"/>
                </div>
            </div>


        </div>
        <div class="col-md-3">
            <label>Auto Text for routing</label>
            <textarea style="height: 100px" name="justificationText" id="justificationText" class="form-control"
                      placeholder="">${ruleInformation?.justificationText}</textarea>
        </div>
        <div class="col-md-3" id="rule-format-container">
            <label for="format">
                <g:message code="app.label.business.configuration.format"/>
            </label>
            <g:hiddenField id="formatInfo" name="formatInfo" value="${ruleInformation?.format}"/>
            <div class="row">
                <div class="col-md-5">
                    <input type="button" class="btn btn-primary formatModal" value="Edit Format">
                </div>

                <div class="col-md-7">
                    <div id="showFontColor" hidden="hidden">
                        <div class="row">
                            <div class="col-md-7">
                                <label>Font Color</label>
                            </div>

                            <div class="col-md-5">
                                <input type="color" id="inputFontColor" disabled="disabled"/>
                            </div>
                        </div>
                    </div>

                    <div id="showCellColor" hidden="hidden">
                        <div class="row">
                            <div class="col-md-7">
                                <label>Cell Color</label>
                            </div>

                            <div class="col-md-5">
                                <input type="color" id="inputCellColor" disabled="disabled"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<g:render template="/includes/modals/percent_value_modal"/>
<textarea name="allTags" class="hidden-item"></textarea>
<input type="hidden" name="JSONQuery" id="queryJSON" value="${ruleInformation?.ruleJSON}"/>
<input type="hidden" name = "rule-id" id="rule-id" value="${ruleInformation?.id}"/>
