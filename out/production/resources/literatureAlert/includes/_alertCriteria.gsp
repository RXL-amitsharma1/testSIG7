<%@ page import="grails.util.Holders; com.rxlogix.enums.DateRangeEnum; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            %{--Product Selection/Study/Generic Selection--}%
            <g:render template="/includes/widgets/product_study_generic-selection" bean="${configurationInstance}"
                      model="[theInstance: configurationInstance, showHideGenericVal: 'hide']"/>

            %{--Event Selection--}%
            <div class="col-md-3">
                <label><g:message code="app.label.eventSelection"/></label>

                <label class="checkbox-inline no-bold add-margin-bottom" style="margin-bottom: 5px;">
                    %{--<g:message code="app.label.eventSelection.limit.primary.path"/>--}%
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
            %{--Search String--}%
            <div class="col-md-5">
                <div class="${hasErrors(bean: configurationInstance, field: 'name', 'has-error')} row">
                    <div class="col-xs-12 form-group">
                        <label>Search String<span class="required-indicator">*</span></label>
                        <g:if test="${actionName == 'copy'}">
                            <input type="text" name="searchString"
                                   placeholder="${g.message(code: 'input.name.placeholder')}" class="form-control"
                                   maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.LiteratureConfiguration', field: 'name')}"
                                   value="${configurationInstance?.searchString}"/>
                        </g:if>
                        <g:else>
                            <input type="text" name="searchString"
                                   placeholder="${g.message(code: 'input.name.placeholder')}" class="form-control"
                                   maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.LiteratureConfiguration', field: 'name')}"
                                   value="${configurationInstance?.searchString}"/>
                        </g:else>
                    </div>

                    <div class="col-xs-12 form-group">
                        <g:render template="dateRange" model="[configurationInstance: configurationInstance, dateMap: dateMap]"/>
                    </div>
                </div>
            </div>
            <div class="col-md-1">
                <div class="row" style="padding-top: 20px;padding-left: 10px;">
                    <div class="col-xs-12">
                        <div class="checkbox checkbox-inline">
                            <g:checkBox name="pubmed" id="pubmed" checked="true" disabled="true"/>
                            <label class="col-xs-12">PubMed</label>
                        </div>
                        <div class="checkbox checkbox-inline">
                            <g:checkBox name="embase" id="embase" checked="false" disabled="true"/>
                            <label class="col-xs-12">Embase</label>
                        </div>
                    </div>
                </div>

                <div class="row hidden" style="padding-top: 22px;padding-left: 10px;">
                    <a aria-label="main navigation" accesskey="c" tabindex="0" class="btn primaryButton btn-primary repeat"
                       href="#">Advanced</a>
                </div>
            </div>
        </div>
    </div>
</div>

<g:javascript>
    $(document).ready(function () {
        $("#dataSourcesProductDict").closest(".row").hide()
        $("#dataSource").val("pubmed");

        $("#pubmed").click(function () {
            if ($("#pubmed").prop("checked")) {
                $("#embase").prop('checked', false)
                $("#dataSource").val("pubmed");
            } else {
                $("#pubmed").prop('checked', false)
            }
        });

        $("#embase").click(function () {
            if ($("#embase").prop("checked")) {
                $("#pubmed").prop('checked', false)
                $("#dataSource").val("embase");
            } else {
                $("#embase").prop('checked', false)
            }
        });
    });
</g:javascript>

<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal" model="[sMQList: sMQList]"/>
    <g:render template="/includes/modals/product_selection_modal" />
</g:else>

