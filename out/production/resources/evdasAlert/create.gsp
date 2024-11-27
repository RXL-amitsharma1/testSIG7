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

        var getSelectedProductUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedProduct')}";
        var getPreLevelProductParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelProductParents')}";
        var searchProductsUrl = "${createLink(controller: 'configurationRest', action: 'searchProducts')}";
        var editAlert = "${action}";

        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var queryList = "${createLink(controller: 'query',action: 'evdasQueryList')}";
        var dataSheetList = "${createLink(controller: 'dataSheet',action: 'dataSheets')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'evdasAlert', action: 'fetchSubstanceFrequencyProperties')}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var fetchFreqNameUrl = "${createLink(controller:"evdasAlert", action: "fetchFreqName")}";
        var validateValue = "${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var LABELS = {
            labelShowAdavncedOptions : "${message(code: 'reportCriteria.show.advanced.options')}",
            labelHideAdavncedOptions : "${message(code: 'reportCriteria.hide.advanced.options')}"
        };
         var dataSheets = "${dataSheetList}";
        var action = "${action}";
        var hasNormalAlertExecutionAccess = ${hasNormalAlertExecutionAccess};
        var ingredientLevel = ${PVDictionaryConfig.ingredientColumnIndex}
        var isAlertScheduled = ${configurationInstance?.nextRunDate? true : false};
        var byDefaultPriority       = '${byDefaultPriority}';
    </g:javascript>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>

    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>

    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/groups.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="app/pvs/configuration/dateRangeEvdas.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/datahseet.js"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="dictionaries.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:link rel="import" href="columnView/column-view.html"/>
    <script>
        $(document).ready(function () {
            $(".priority-List").find(".select2").select2();
            $("#dataSourcesProductDict").val("eudra")
            $("#dataSourcesProductDict").closest(".row").hide()
            disableDictionaryValues(true,false,true,true,true)
            if(action === "copy") {
                $(".addProductValues").click();
            }
            $('#myScheduler :input').prop('disabled', $("#adhocRun").is(':checked'));
            $queryName = $("#queryName");
            $query = $("#query");
            var data = ($queryName.val() && $("#evdasQueryId").val()) ? {id: $("#evdasQueryId").val(), text: $queryName.val()} : {};
            bindSelect2WithUrl($query, queryList, data);
            $query.on("change", function (e) {
                $query.val() ? $queryName.val($(this).select2('data')[0].name) : $queryName.val("");
            });
            if(typeof hasNormalAlertExecutionAccess != "undefined" && !hasNormalAlertExecutionAccess) {
                $("#adhocRun").prop('disabled', true);
            }
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

<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

<form id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

    <g:render template="form" model="[configurationInstance: configurationInstance, priorityList: priorityList,userList:userList,action: action]"/>

    <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>
    <g:hiddenField name="signalId" id="signalId" value="${signalId}"/>


    %{--BEGIN: Button Bar  ==============================================================================================================--}%
    <div style="margin-top:15px;">
        <div style="text-align: right">

            <g:actionSubmit class="btn primaryButton btn-primary" id="saveRun" title="Saves and schedules the alert for next run date."
                            type="submit" accesskey="r" data-action="${createLink(controller: 'evdasAlert',action: 'run')}"
                            value="${message(code: 'default.button.saveAndRun.label')}"/>
            <g:actionSubmit class="btn btn-primary" id="saveBtn" accesskey="s" title="Only saves the alert configuration without scheduling or running the alert."
                            type="submit" data-action="${createLink(controller: 'evdasAlert',action: 'save')}"
                            value="${message(code: 'default.button.save.label')}"/>
            <a class="btn btn-default pv-btn-grey" tabindex="0" accesskey="c"
               href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
        </div>
    </div>
    %{--END: Button Bar  ================================================================================================================--}%

</form>
</body>
