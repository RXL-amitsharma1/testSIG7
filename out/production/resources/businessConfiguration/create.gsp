<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.business.configuration.title"/></title>
    <asset:stylesheet src="configuration.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <asset:javascript src="fuelux/fuelux.js"/>

    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="boostrap-switch.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/businessConfiguration/businessConfigProduct.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
        <asset:stylesheet src="copyPasteModal.css"/>
        <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
        <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    </g:else>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <style type="text/css">
    .row {
        padding-top: 5px;
    }
    </style>
    <g:javascript>
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var queryType = "QUERY_BUILDER";
        var editable = true;
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
        var disableInactiveTabs = false;
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
    </g:javascript>
</head>

<body>
<g:hiddenField name="fetchSelectBoxValuesUrl"
               value="${createLink(controller: 'businessConfiguration', action: 'fetchSelectBoxValues')}"/>
<g:hiddenField name="saveBusinessConfigurationUrl"
               value="${createLink(controller: 'businessConfiguration', action: 'saveBusinessConfiguration')}"/>
<g:hiddenField name="isGlobalRule" value="${isGlobalRule}"/>
<g:render template="/includes/layout/flashErrorsDivs" bean="${businessConfiguration}" var="theInstance"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label click">
                <g:message code="app.label.business.configuration.title"/>
            </label>
        </div>

        <div class="rxmain-container-content">
            <g:select id="selectedDatasource" name="selectedDatasource"
                      from="${datasourceMap.entrySet()}"
                      optionKey="key" optionValue="value" value="${businessConfiguration?.dataSource}"
                      class="form-control selectedDatasourceSignal" style="display: none"/>

        <g:if test="${!isGlobalRule}">
            <g:form name="businessConfigurationForm" controller="businessConfiguration" action="saveBusinessConfiguration" id="${businessConfiguration?.id}"
                    elementId="businessConfigurationForm">
                <g:render template="businessConfigurationForm" model="[businessConfiguration: businessConfiguration, datasourceMap: datasourceMap, isGlobalRule: isGlobalRule]"/>
            </g:form>
        </g:if>
        <g:else>
            <g:form name="businessConfigurationForm" controller="businessConfiguration" action="saveGlobalRule" id="${businessConfiguration?.id}"
                    elementId="businessConfigurationForm">
                <g:render template="businessConfigurationForm" model="[businessConfiguration: businessConfiguration, datasourceMap: datasourceMap, isGlobalRule: isGlobalRule]"/>
            </g:form>
        </g:else>

        </div>
    </div>
</div>
<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList, isBusinessConfiguration: true,multiIngredientValue: businessConfiguration?.isMultiIngredient,isPVCM: isPVCM]"/>
    <g:render template="/configuration/copyPasteModal" />
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal"/>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
<script>
    var pvaUrls = {
        selectUrl: options.product.selectUrl,
        preLevelParentsUrl: options.product.preLevelParentsUrl,
        searchUrl: options.product.searchUrl
    };
    var otherUrls = {
        selectUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getSelectedProduct')}",
        preLevelParentsUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getPreLevelProductParents')}",
        searchUrl: "${createLink(controller: 'pvsProductDictionary', action: 'searchProducts')}"
    };
    changeDataSource("${businessConfiguration?.dataSource?:'pva'}");

    $(document).ready(function() {
        $("#selectedDatasource").select2({
            multiple: true
        });
        $('#selectedDatasource').next(".select2-container").hide();
        $("#dataSource").on('change', function () {
            $('#selectedDatasource').val($(this).val());
            clear_search_results();
            if($(this).val() == 'eudra') {
                $("#dataSourcesProductDict").val("eudra");
                disableDictionaryValues(true, false, true, true, true)
            }
        });
        $('#productModal').on('show.bs.modal', function(){
            var selectedDatasource= ($("#datasource").find("select[name='dataSource']").val());
            var selectedDataSourceText = ($("#datasource .selectedDatasource").val())
            if(selectedDatasource){
                var element = $( this );
                element.find('#dataSourcesProductDict').val(selectedDatasource).change();
            }
            else if(selectedDataSourceText){
                $(this).find('#dataSourcesProductDict').val(selectedDataSourceText).change();
            }
        });
        $('#dataSourcesProductDict').prop('disabled',true)
        $("#selectedDatasource option[value='jader']").removeAttr("disabled");
    });
</script>
</g:if>
</body>
</html>
