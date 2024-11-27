<%@ page import="com.rxlogix.util.ViewHelper; grails.util.Holders" %>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <!-- Header Section -->
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.dataMiningSearch"/>
            </label>
        </div>

        <!-- Content Section -->
        <div class="rxmain-container-content rxmain-container-show" aria-expanded="true">
            <div class="row">
                <div class="col-xs-8">
                    <!-- Configuration Name -->
                    <div class="row">
                        <div class="col-md-3">
                            <label>Configuration Name</label>
                            <span class="required-indicator">*</span>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-xs-6">
                            <div class="form-group">
                                <g:select class="form-control expressionField" id="dataMiningVariable" name="dataMiningVariable"
                                          value="PAM(S)"
                                          noSelection="['null': message(code: 'select.one')]"
                                          from="['PAM(S)', 'PAI(s)', 'Product Name(s)', 'Trade Name(s)', 'Substance (S+PT+HLT+HLGT)', 'Substance (S+HLT)']"/>
                            </div>
                        </div>
                        <div class="col-xs-6">
                            <i>*Latest data mining date range: 15-Sep-2024 to 21-Sep-2024</i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row gap-2">
                <!-- Product Selection -->
                <div class="col-md-4">
                    <label>
                        <g:message code="app.label.product.label"/>
                    </label>
                    <span class="required-indicator">*</span>

                    <!-- Search Product -->
                    <div class="wrapper" style="position: relative;">
                        <div id="showProductSelection" class="showDictionarySelection"></div>
                        <div class="iconSearch" style="position: absolute; bottom: 10px; right: 10px;">
                            <a id="searchProducts" data-toggle="modal" data-target="#productModal" tabindex="0" role="button" data-toggle="tooltip" title="Search Product">
                                <i class="fa fa-search"></i>
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Event Selection -->
                <div class="col-md-4">
                    <label>
                        <g:message code="app.label.eventSelection"/>
                        <span class="required-indicator"/>
                    </label>
                    <label class="checkbox-inline no-bold add-margin-bottom hidden" style="margin-bottom: 5px;">
                        <g:message code="app.label.eventSelection.limit.primary.path"/>
                    </label>

                    <!-- Search Event -->
                    <div class="wrapper" style="position: relative;">
                        <div id="showEventSelection" class="showDictionarySelection" style="width: 100%;"></div>
                        <div class="iconSearch" style="position: absolute; bottom: 10px; right: 10px;">
                            <a id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0" role="button" data-toggle="tooltip" title="Search Event">
                                <i class="fa fa-search"></i>
                            </a>
                        </div>
                    </div>
                    <g:textField name="eventSelection" value="" hidden="hidden"/>
                </div>

                <div class="col-md-3">
                    <div class="row">
                        <div class="col-xs-1"></div>
                        <div class="col-xs-11">
                            <label>
                                <g:message code="app.label.filter"/>
                                <span class="required-indicator"/>
                            </label>
                        </div>
                        <div class="col-xs-12">
                            <div class="row">
                                <div class="col-xs-1"></div>
                                <div class="col-xs-7">
                                    <select id="advanced-filter" class="form-control advanced-filter-dropdown"></select>
                                </div>
                                <div class="col-xs-4 text-left">
                                    <a href="#" class="edit-filter pv-ic" title="Edit Filter" tabindex="0" id="editAdvancedFilter">
                                        <i class="mdi mdi-pencil font-20"></i>
                                    </a>
                                    <a href="#" class="add-filter pv-ic" title="Add Filter" tabindex="0" id="addAdvancedFilter">
                                        <i class="mdi mdi-plus font-20"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-1">
                    <div class="row" style=" margin-top: 90px;">
                        <input type="button" class="btn btn-primary copyTemplateQueryLineItemButton" id="searchDataMiningResult" value="Search" autocomplete="off">
                    </div>
                </div>

            </div>
        </div>
    </div>
</div>
<g:javascript>
    var fetchAdvFilterUrl = "${createLink(controller: 'dataMiningResult', action: 'fetchAdvancedFilterNameAjax')}";

</g:javascript>
<asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
<asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
<asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>
<asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
<asset:stylesheet src="configuration.css"/>
<asset:stylesheet src="copyPasteModal.css"/>
<asset:stylesheet src="dictionaries.css"/>
<asset:stylesheet src="advancedFilter.css"/>



<g:render template="/advancedFilters/create_advanced_filters_modal" model="[fieldInfo: fieldList, isShareFilterViewAllowed: isShareFilterViewAllowed,isDataMiningScreen:true]"/>
<asset:javascript src="app/pvs/advancedFilter/advancedFilterQueryBuilder.js"/>
<asset:javascript src="backbone/underscore.js"/>
<asset:javascript src="backbone/backbone.js"/>

<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList, isPVCM:true]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal" />
    <g:render template="/includes/modals/product_selection_modal" />
    <g:render template="/includes/modals/study_selection_modal" />
</g:else>

<g:javascript>

    var isWhoDictionary = true;
    var selectAutoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxAdvancedFilterSearch', params: [executedConfigId: executedConfigId])}";
    var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
    var fetchUsersUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAjaxUserSearch')}";
    var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
    var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
    var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
    var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
    var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
    var queryList = "${createLink(controller: 'query',action: 'queryList')}";
    var fetchAdvancedFilterInfoUrl = "${createLink(controller: 'advancedFilter', action: 'fetchAdvancedFilterInfo')}";
    var fetchAdvFilterUrl = "${createLink(controller: 'dataMiningResult', action: 'fetchAdvancedFilterNameAjax')}";

</g:javascript>



