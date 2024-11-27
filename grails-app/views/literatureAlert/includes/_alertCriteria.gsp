<%@ page import="grails.util.Holders; com.rxlogix.enums.DateRangeEnum; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            %{--Product Selection/Study/Generic Selection--}%
            <g:render template="/includes/widgets/product_study_generic-selection" bean="${configurationInstance}"
                      model="[theInstance: configurationInstance, showHideGenericVal: 'hide']"/>

            %{--Search String--}%
            <div class="col-md-4">
                <div class="${hasErrors(bean: configurationInstance, field: 'name', 'has-error')} row">
                    <div class="col-xs-12 form-group">
                        <label>Search String<span class="required-indicator">*</span></label>
                        <g:textArea name="searchString" class="form-control cell-break word-break literatureSearchString" style="padding-top: 5px;
                        box-sizing: border-box;"
                               maxlength="${gorm.maxLength(clazz: 'com.rxlogix.config.LiteratureConfiguration', field: 'searchString')}"
                               value="${configurationInstance?.searchString}"/>
                        <div class="iconSearch">
                            <i class="fa fa-copy" id="copyButton" title="Copy to Clipboard"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <g:render template="dateRange" model="[configurationInstance: configurationInstance, dateMap: dateMap]"/>
            </div>
        </div>
        <div class="row literatureQueryDropdown hidden">
            <div class="col-md-3">
                <label class="labelBold">
                    <g:message code="app.label.chooseAQuery"/>
                </label>

                <div class="row queryContainer">

                    <div class="doneLoading" style="padding-bottom: 5px;">
                        <g:select name="literatureQuery" id="literatureQuery" from="${[]}" class="form-control select2" noSelection="['' :'--Select One--']"/>
                    </div>
                    <g:hiddenField name="literatureQueryName" id="literatureQueryName" value="${configurationInstance?.literatureQueryName}"/>
                    <g:hiddenField name="literatureQueryId" id="literatureQueryId" value="${configurationInstance?.literatureQueryId}"/>
                </div>
            </div>
        </div>
    </div>
</div>

<g:javascript>
    $(function () {
        $("#dataSourcesProductDict").closest(".row").hide();
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

