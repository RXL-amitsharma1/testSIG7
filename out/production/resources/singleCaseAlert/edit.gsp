<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<%@ page import="com.rxlogix.config.DateRangeValue" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.Edit.Alert.title" args="${[configurationInstance.name]}"/></title>

    <g:javascript>

        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var isValidationError= ${validationError?:false};
        var fetchAssignmentForProductsUrl = "${createLink(controller: 'productAssignment', action: 'fetchAssignmentForProducts')}";
        var isAutoAssignedTo = ${isAutoAssignedTo};
        var isAutoSharedWith = ${isAutoSharedWith};

        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}"
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

        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var queryList = "${createLink(controller: 'query',action: 'queryList')}";
        var templateList = "${createLink(controller: 'template',action: 'templateList')}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var cioms1Id = "${cioms1Id}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var templateIdNameListUrl = "${createLink(controller: 'template', action: 'templateIdNameList')}";
        var queryIdNameListUrl = "${createLink(controller: 'query', action: 'queryIdNameList')}";
        var reportFieldsForQueryUrl = "${createLink(controller: 'query', action: 'reportFieldsForQueryValue')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var fetchCaseSeriesUrl = "${createLink(controller: 'singleCaseAlert', action: 'fetchCaseSeries')}";
        var caseSeriesData = {id:"${selectedCaseSeriesId}",text:"${selectedCaseSeriesText}"};
        var validateValue = "${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var alertThresholdFlag = ${Holders.config.triggeredAlertThreshold};
        var byDefaultPriority       = '${byDefaultPriority}';
        var editAlert = "${action}";

       var LABELS = {
            labelShowAdvancedOptions : "${message(code:'add.header.title.and.footer')}",
            labelHideAdvancedOptions : "${message(code:'hide.header.title.and.footer')}"
        };

       var userIdList = "${userList.collect{it.id}.join(",")}".split(",");
       var isAlertScheduled = ${configurationInstance?.nextRunDate? true : false};

    var appType = "${appType}";

        $(document).ready(function () {
            $(".priority-List").find(".select2").select2();
        if(!alertThresholdFlag){
         $('#alertThreshold').addClass('hide');
        }
            $("#adhocRun").change(function () {
                $('#myScheduler :input').prop('disabled', $(this).is(':checked'));
                $('#priority').prop('disabled', $(this).is(':checked'));
                 $('#isTemplateAlert').prop('disabled', $(this).is(':checked'));
                 if($(this).is(':checked') &&  $('#isTemplateAlert').is(':checked')){
                    $('#isTemplateAlert').prop('checked', false);;
                }
            });
            $('.disable').click(function () {
                $(this).prop('disabled', true);
            });
        });
    </g:javascript>

    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_product_assignment.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_study_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="app/pvs/groups.js"/>
    <asset:javascript src="app/pvs/configuration/templateQueries.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/configuration/dateRange.js"/>
    <asset:javascript src="app/pvs/configuration/blankParameters.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_query_utils.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>

    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="configuration.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
    <asset:stylesheet src="dictionaries.css"/>
    </g:if>

</head>

<body>

<g:set var="userService" bean="userService"/>

    <div class="row" id="case-edit">
        <div class="col-sm-12">
            <div class="page-title-box">
                <div class="fixed-page-head">
                    <div class="page-head-lt">
                        <h4 title="${configurationInstance.name ?:""}">${message(code: 'app.Edit.Alert', args: [configurationInstance.name])}</h4>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12">
            <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

            <form id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

                <g:render template="form" model="[configurationInstance: configurationInstance, userList: userList, priorityList:priorityList, spotfireEnabled:spotfireEnabled]"/>
                <g:hiddenField name="editable" id="editable" value="true"/>
                <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="editPage"/>

                %{--BEGIN: Button Bar  ==============================================================================================================--}%
                <div class="pull-right rxmain-container-top">
                <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                    <g:actionSubmit accesskey="d" class="btn btn-primary repeat disable" data-action="${createLink(controller: 'singleCaseAlert',action: 'disable',params: [id:configurationInstance.id] )}" type="submit" value="${message(code:"default.button.unschedule.label")}"/>
                </g:if>
                <g:else>
                    <g:actionSubmit  accesskey="r" class="btn btn-primary repeat disable" data-action="${createLink(controller: 'singleCaseAlert',action: 'run',params: [id:configurationInstance.id] )}" type="submit" value="${message(code: 'default.button.update.run.label')}"/>
                </g:else>
                    <g:actionSubmit accesskey="u" class="btn btn-default btn-primary repeat disable" data-action="${createLink(controller: 'singleCaseAlert',action: 'update',params: [id:configurationInstance.id] )}" type="submit" value="${message(code: 'default.button.update.label')}"/>
                    <a tabindex="0" accesskey="c" class="btn btn-default pv-btn-grey"
                       href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
                </div>
                %{--END: Button Bar  ================================================================================================================--}%
                <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}" hidden="hidden"/>
                <input name="repeatFlag" id="repeatFlag" hidden="hidden"/>
                <input name="previousAction" id="previousAction" value="${action}" hidden="hidden"/>
            </form>

            <div>
                %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
                <g:render template='/templateQuery/templateQuery' model="[templateQueryInstance:null,
                                                                          i:'_clone',
                                                                          hidden:true,
                                                                          templateId: templateId, 'type': configurationInstance.type]"/>
                %{--</tbody>--}%

                <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"   model="[type:'qev']"/></div>
                <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"   model="[type:'qev']"/></div>
            </div>
        </div>
    </div>
</body>

