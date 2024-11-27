<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.new.single.case.alert" /></title>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_product_assignment.js"/>
    <asset:javascript src="app/pvs/groups.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_study_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/configuration/templateQueries.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/configuration/dateRange.js"/>
    <asset:javascript src="app/pvs/configuration/blankParameters.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_query_utils.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>

    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
%{--        var isCreatePage = ${!clone};--}%
        var isValidationError= ${validationError?:false};
        var isAutoAssignedTo = ${isAutoAssignedTo};
        var isAutoSharedWith = ${isAutoSharedWith};
        var fetchAssignmentForProductsUrl = "${createLink(controller: 'productAssignment', action: 'fetchAssignmentForProducts')}";

        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var allFieldsUrl = "${createLink(controller: 'query', action: 'getAllFields')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var queryList = "${createLink(controller: 'query',action: 'queryList')}";
        var templateList = "${createLink(controller: 'template',action: 'templateList')}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var cioms1Id = "${cioms1Id}";
        var editAlert = "${action}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var templateIdNameListUrl = "${createLink(controller: 'template', action: 'templateIdNameList')}";
        var queryIdNameListUrl = "${createLink(controller: 'query', action: 'queryIdNameList')}";
        var reportFieldsForQueryUrl = "${createLink(controller: 'query', action: 'reportFieldsForQueryValue')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var validateValue = "${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var getDmvData = "${createLink(controller: 'query', action: 'getDmvData')}";
        var fetchCaseSeriesUrl = "${createLink(controller: 'singleCaseAlert', action: 'fetchCaseSeries')}";
        var caseSeriesData = {id:"${selectedCaseSeriesId}",text:"${selectedCaseSeriesText}"};
        var productBasedSecurity = ${Holders.config.pvsignal.product.based.security};
        var alertThresholdFlag = ${Holders.config.triggeredAlertThreshold};
        var byDefaultPriority       = '${byDefaultPriority}';

       var LABELS = {
            labelShowAdvancedOptions : "${message(code:'add.header.title.and.footer')}",
            labelHideAdvancedOptions : "${message(code:'hide.header.title.and.footer')}"
        };

       var userIdList = "${userList.collect{it.id}.join(",")}".split(",");

       var appType = "${appType}";
       var hasNormalAlertExecutionAccess= ${hasNormalAlertExecutionAccess};
       var isAlertScheduled = ${configurationInstance?.nextRunDate? true : false};
        $(document).ready(function () {
             $(".priority-List").find(".select2").select2();
                    $('#adhocRun').click(function() {
                        if (!$(this).is(':checked') && byDefaultPriority != null && byDefaultPriority != '') {
                             $("#priority").val(byDefaultPriority).trigger('change');
                        }
                    });
        if(!alertThresholdFlag){
           $('#alertThreshold').addClass('hide');
        }
            $('#applyAlertStopList').prop("checked", true);
            $("#dataSourcesProductDict").closest(".row").hide()
            $('#myScheduler :input').prop('disabled', $("#adhocRun").is(':checked'));
            $("#adhocRun").change(function () {
                $('#myScheduler :input').prop('disabled', $(this).is(':checked'));
                $('#priority').prop('disabled', $(this).is(':checked'));
                 $('#isTemplateAlert').prop('disabled', $(this).is(':checked'));
                 if($(this).is(':checked') &&  $('#isTemplateAlert').is(':checked')){
                    $('#isTemplateAlert').prop('checked', false);;
                }
            });
            if (caseSeriesData != undefined && caseSeriesData.id) {
            $('#includeLockedVersion').prop("checked", true).prop("disabled", true);
            }
            if (productBasedSecurity == true) {
                addAllowedUsersList();
                $(".addAllProducts").on("click", function () {
                    addAllowedUsersList()
                });
            }
            if(typeof hasNormalAlertExecutionAccess != "undefined" && !hasNormalAlertExecutionAccess) {
                $("#adhocRun").prop('disabled', true);
            }
        });
    </g:javascript>
</head>


<body>
    <g:set var="userService" bean="userService"/>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

    <form class="singleCaseAlertForm" id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

    <g:render template="form"
              model="[configurationInstance: configurationInstance, priorityList: priorityList, userList: userList, action: action,
                      sMQList: sMQList, templateList: templateList, productGroupList: productGroupList, spotfireEnabled:spotfireEnabled, clone: clone, isPVCM: isPVCM]"/>

        <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>
    <g:hiddenField name="signalId" id="signalId" value="${signalId}"/>

        <div style="margin-top:15px;">
            <div style="text-align: right">
                <g:actionSubmit accesskey="r" tabindex="0" class="btn primaryButton btn-primary repeat" id="saveRun" data-action="${createLink(controller: 'singleCaseAlert',action: 'run')}" type="submit" value="${message(code: 'default.button.saveAndRun.label')}"/>
                <g:actionSubmit accesskey="s" tabindex="0"  class="btn btn-default btn-primary repeat" id="saveBtn" data-action="${createLink(controller: 'singleCaseAlert',action: 'save')}" type="submit" value="${message(code: 'default.button.save.label')}"/>
                <a aria-label="main navigation" accesskey="c" tabindex="0"  class="btn btn-default pv-btn-grey"
                   href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
            </div>
        </div>

        <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}" hidden="hidden"/>
        <input name="previousAction" id="previousAction" value="${action}" hidden="hidden"/>
    </form>

    <div>
        %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
        <g:render template='/templateQuery/templateQuery' model="['templateQueryInstance':null,
                                                                  'i':'_clone',
                                                                  'hidden':true]"/>

        <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"   model="[type:'qev']"/></div>
        <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"   model="[type:'qev']"/></div>
    </div>
</body>
