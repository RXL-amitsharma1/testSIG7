<%@ page import="grails.util.Holders; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.evdas.title"/></title>
    <g:javascript>
        var isValidationError= ${validationError?:false};
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var getSelectedEventUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedEvent')}";
        var getPreLevelEventParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelEventParents')}";
        var searchEventsUrl = "${createLink(controller: 'configurationRest', action: 'searchEvents')}";
        var dataSheetList = "${createLink(controller: 'dataSheet',action: 'dataSheets')}";
        var getSelectedProductUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedProduct')}";
        var getPreLevelProductParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelProductParents')}";
        var searchProductsUrl = "${createLink(controller: 'configurationRest', action: 'searchProducts')}";
        var dataSheets = "${dataSheetList}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var queryList = "${createLink(controller: 'query',action: 'evdasQueryList')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'evdasAlert', action: 'fetchSubstanceFrequencyProperties')}";
        var fetchFreqNameUrl = "${createLink(controller:"evdasAlert", action: "fetchFreqName")}";
        var validateValue = "${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var editAlert = "${action}";
        var ingredientLevel = ${PVDictionaryConfig.ingredientColumnIndex};
        var LABELS = {
            labelShowAdavncedOptions : "${message(code: 'reportCriteria.show.advanced.options')}",
            labelHideAdavncedOptions : "${message(code: 'reportCriteria.hide.advanced.options')}"
        }
        var isAlertScheduled = ${configurationInstance?.nextRunDate? true : false};
        var byDefaultPriority       = '${byDefaultPriority}';
    </g:javascript>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>

    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/groups.js"/>
    <asset:javascript src="app/pvs/datahseet.js"/>
    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="app/pvs/configuration/dateRangeEvdas.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>

    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="dictionaries.css"/>
    <asset:link rel="import" href="columnView/column-view.html"/>
    <script>
        $(document).ready(function () {
            $(".priority-List").find(".select2").select2();
            $("#dataSourcesProductDict").closest(".row").hide();
            disableDictionaryValues(true,false,true,true,true);
            $(".addProductValues").click();


            $queryName = $("#queryName");
            $query = $("#query");
            var data = ($queryName.val() && $("#evdasQueryId").val()) ? {id: $("#evdasQueryId").val(), text: $queryName.val()} : {};
            bindSelect2WithUrl($query, queryList, data);
            $query.on("change", function (e) {
                $query.val() ? $queryName.val($(this).select2('data')[0].name) : $queryName.val("");
            });
            $("#allSheets").change(function(){
                if($(this).is(':checked')){
                    $(this).val('ALL_SHEET');
                }else{
                    $(this).val('CORE_SHEET');
                }
            });
            if($("#selectedDatasheet").is(":checked")){
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.show();
            }
        });
    </script>
</head>

<body>

<g:set var="userService" bean="userService"/>

<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

<form id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

    <g:render template="form" model="[configurationInstance: configurationInstance, priorityList: priorityList]"/>
    <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>
    <g:hiddenField name="configurationInstanceId" id="configurationInstanceId" value="${configurationInstance.id}"/>

    %{--BEGIN: Button Bar  ==============================================================================================================--}%
    <div class="m-t-15">
        <div class="text-right">
            <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                <g:actionSubmit accesskey="d" class="btn btn-primary repeat" data-action="${createLink(controller: 'evdasAlert',action: 'disable',params: [id:configurationInstance.id])}" type="submit"
                                value="${message(code: "default.button.unschedule.label")}"/>
            </g:if>
            <g:else>
                <g:actionSubmit title="Updates and schedules the alert for next run date." accesskey="r" class="btn btn-primary repeat" data-action="${createLink(controller: 'evdasAlert',action: 'run',params: [id:configurationInstance.id])}" type="submit"
                                value="${message(code: 'default.button.update.run.label')}"/>
            </g:else>
            <g:actionSubmit title="Only updates the alert configuration without scheduling or running the alert." accesskey="u" class="btn btn-default btn-primary repeat" data-action="${createLink(controller: 'evdasAlert',action: 'update',params: [id:configurationInstance.id])}" type="submit"
                            value="${message(code: 'default.button.update.label')}"/>
            <a class="btn btn-default pv-btn-grey" accesskey="c" tabindex="0"
               href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
        </div>
    </div>
    %{--END: Button Bar  ================================================================================================================--}%

</form>
</body>
