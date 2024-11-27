<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ProductClassification; com.rxlogix.enums.DateRangeEnum; com.rxlogix.dto.SpotfireSettingsDTO; com.rxlogix.util.ViewHelper; com.rxlogix.config.TemplateQuery" %>

<g:if test="${spotfireEnabled}">
    <g:set var="spotfireSettings"
           value="${theInstance?.spotfireSettings ? SpotfireSettingsDTO.fromJson(theInstance?.spotfireSettings) : new SpotfireSettingsDTO()}"/>
</g:if>

<g:if test="${configurationInstance.selectedDatasource != Constants.DataSource.FAERS || configurationInstance.selectedDatasource != Constants.DataSource.VIGIBASE}">

    <div id="templateQueryList" data-counter="${theInstance?.templateQueries?.size()}">
        <g:if test="${theInstance?.templateQueries?.size() > 0}">
            <g:each var="templateQuery" in="${theInstance?.templateQueries}" status="i">
                <g:render template='/templateQuery/templateQuery'
                          model="['templateQueryInstance': templateQuery, 'i': i, 'hidden': false, clone: clone]"/>
            </g:each>
        </g:if>

    </div>
</g:if>


%{--Add Template Query button--}%
<div class="row" style="margin-left: 20px">
<sec:ifAnyGranted roles="ROLE_DATA_ANALYSIS, ROLE_ADMIN ">
    <g:if test="${spotfireEnabled}">
        <div class="checkbox checkbox-primary col-md-6">
            <input type="checkbox" value="true"
                   name="enableSpotfire" ${spotfireSettings.type ? 'checked="checked"' : ""} id="enableSpotfire"
                   autocomplete="off">
            <label for="enableSpotfire">
                <g:message code="app.label.spotfire.generateAnalysis"/>
            </label>
        </div>
    </g:if>
</sec:ifAnyGranted>
    <g:if test="${configurationInstance.selectedDatasource != Constants.DataSource.FAERS || configurationInstance.selectedDatasource != Constants.DataSource.VIGIBASE}">
        <div class="col-md-6 text-right pull-right report-section-buttons">
            <input type="button"
                   class="btn btn-primary copyTemplateQueryLineItemButton"
                   value="${message(code: "button.copy.section.label")}"/>
            <input type="button"
                   class="btn btn-primary addTemplateQueryLineItemButton"
                   value="${message(code: "button.add.section.label")}"/>
        </div>
    </g:if>
</div>


<g:if test="${spotfireEnabled}">
    <div class="row spotfire" hidden="hidden">
        <div class="col-md-12 ">
            <div class="row">
                <div class="col-md-4">
                    <div class="col-md-11">
                        <label><g:message code="app.label.spotfire.type"/></label>
                        <select name="spotfireType" id="spotfireType" class="form-control spotfire_control spotfire_type">
                            <option value="${ProductClassification.DRUG}" ${spotfireSettings?.type == ProductClassification.DRUG ? "selected" : ""}><g:message
                                    code="app.label.spotfire.type.drag"/></option>
                            <g:if test="${configurationInstance.selectedDatasource != Constants.DataSource.FAERS}">
                                <option value="${ProductClassification.VACCINCE}" ${spotfireSettings?.type == ProductClassification.VACCINCE ? "selected" : ""}><g:message
                                        code="app.label.spotfire.type.vacc"/></option>
                            </g:if>
                        </select>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="col-md-11">
                        <label><g:message code="app.label.DateRange"/></label>
                        <g:if test="${appType == com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                            <g:select name="spotfireDaterange" multiple="true"
                                      from="${ViewHelper.getNewDateRangeReportSection()}"
                                      optionValue="display"
                                      optionKey="name"
                                      value="${spotfireSettings?.rangeType?.collect { it as DateRangeEnum }}"
                                      class="form-control spotfire-date-range"/>
                        </g:if>
                        <g:else>
                            <g:select name="spotfireDaterange" multiple="true"
                                      from="${ViewHelper.getDateRangeReportSection()}"
                                      optionValue="display"
                                      optionKey="name"
                                      value="${spotfireSettings?.rangeType?.collect { it as DateRangeEnum }}"
                                      class="form-control spotfire_control"/>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>
    </div>
</g:if>
