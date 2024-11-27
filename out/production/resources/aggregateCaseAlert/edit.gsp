<%@ page import="grails.converters.JSON;grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;" %>
<%@ page import="com.rxlogix.config.DateRangeValue" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.Edit.Alert.title" args="${[configurationInstance.name ?: ""]}"/></title>

    <g:javascript>
        var isMultipleDatasource = ${isMultipleDatasource};
        var isValidationError= ${validationError?:false};
        var isPVCM = ${isPVCM};
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";
        var getSelectedEventUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedEvent')}";
        var getPreLevelEventParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelEventParents')}";
        var searchEventsUrl = "${createLink(controller: 'configurationRest', action: 'searchEvents')}";

        var fetchAssignmentForProductsUrl = "${createLink(controller: 'productAssignment', action: 'fetchAssignmentForProducts')}";
        var isAutoAssignedTo = ${isAutoAssignedTo};
        var isAutoSharedWith = ${isAutoSharedWith};

        var getSelectedProductUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedProduct')}";
        var getPreLevelProductParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelProductParents')}";
        var searchProductsUrl = "${createLink(controller: 'configurationRest', action: 'searchProducts')}";

        var getSelectedStudyUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedStudy')}";
        var getPreLevelStudyParentsUrl = "${createLink(controller: 'configurationRest', action: 'getPreLevelStudyParents')}";
        var searchStudiesUrl = "${createLink(controller: 'configurationRest', action: 'searchStudies')}";

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

        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";
        var templateList = "${createLink(controller: 'template',action: 'templateList')}";
        var cioms1Id = "${cioms1Id}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var fetchFreqNameUrl = "${createLink(controller:"aggregateCaseAlert", action: "fetchFreqName")}";
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'aggregateCaseAlert',action: 'getSubstanceFrequency')}";
        var queryList = "${createLink(controller: 'query',action: 'queryList')}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var fetchDrugClassificationUrl = "${createLink(controller: 'configurationRest', action: 'fetchDrugClassification')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var templateIdNameListUrl = "${createLink(controller: 'template', action: 'templateIdNameList')}";
        var queryIdNameListUrl = "${createLink(controller: 'query', action: 'queryIdNameList')}";
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
        var reportFieldsForQueryUrl = "${createLink(controller: 'query', action: 'reportFieldsForQueryValue')}";
        var dataSheetList = "${createLink(controller: 'dataSheet',action: 'dataSheets')}";
        var exeEvdasDateRange = "${exeEvdasDateRange}";
        var exeFaersDateRange = "${exeFaersDateRange}";
        var exeVaersDateRange = "${exeVaersDateRange}";
        var exeVigibaseDateRange = "${exeVigibaseDateRange}";
        var dataSheets = "${dataSheetList}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var validateValue = "${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var getDmvData = "${createLink(controller: 'query', action: 'getDmvData')}";
        var LABELS = {
            labelShowAdvancedOptions : "${message(code:'add.header.title.and.footer')}",
            labelHideAdvancedOptions : "${message(code:'hide.header.title.and.footer')}"
        };
        var prevDataSourceList = JSON.parse('${prevDataSourcesList}');
        var appType = "${appType}";
        var userIdList = "${userList.collect{it.id}.join(",")}".split(",");
        var editAlert = "${action}";
        var configurationMiningVariable ="${configurationInstance?.dataMiningVariable}";
        var enabledDataSourceList= '${enabledOptions}';
        var byDefaultPriority       = '${byDefaultPriority}';
        var productTypeOptions = JSON.parse("${productTypeMap as JSON}");
        var isAlertScheduled = ${configurationInstance?.nextRunDate? true : false};
        var templateUpdateWarning = '${message(code: 'app.template.alert.update.warning')}';
    </g:javascript>

    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_product_assignment.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/configuration/dateRange.js"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_study_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
    </g:else>
    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/groups.js"/>
    <asset:javascript src="app/pvs/configuration/templateQueries.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/configuration/blankParameters.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_query_utils.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/advanceOption.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:javascript src="app/pvs/datahseet.js"/>

    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="configuration.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>

    <script>
        $(document).ready(function(){
            $(".priority-List").find(".select2").select2();
            $('#myScheduler :input').prop('disabled', true);
            if(!$("#adhocRun").is(":checked")){
                $('#myScheduler :input').prop('disabled', false);
                disableDataMiningVariableFields(true);
                $('#isProductMining').prop('disabled',true);
            }
            $("#adhocRun").change(function () {
                $('#myScheduler :input').prop('disabled', $(this).is(':checked'));
                disableDataMiningVariableFields(!$(this).is(':checked'));
                $('#priority').prop('disabled', $(this).is(':checked'));
                $('#isTemplateAlert').prop('disabled', $(this).is(':checked'));
                if($(this).is(':checked') &&  $('#isTemplateAlert').is(':checked')){
                    $('#isTemplateAlert').prop('checked', false);
                }
                if(!$(this).is(':checked') && !$('#groupBySmq').is(':checked') ){
                    $('#selectedDatasheet').prop('disabled',false);
                }
            });
            if( $('#groupBySmq').is(':checked')){
                $('#selectedDatasheet').prop('checked',false);
                $('#selectedDatasheet').prop('disabled',true);
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.hide();
            }
            $('#groupBySmq').change(function () {
                if($(this).is(':checked')){
                    $('#selectedDatasheet').prop('checked',false)
                    var dataSheetOptions = $(".datasheet-options");
                    dataSheetOptions.hide();
                }
                if(!$(this).is(':checked')){
                    if($("#dataMiningVariable").is(':disabled')){
                        $('#selectedDatasheet').prop('disabled',false)
                    }else if($("#dataMiningVariable").val() === "null"){
                        $('#selectedDatasheet').prop('disabled',false)
                    }else{
                        $('#selectedDatasheet').prop('disabled',true)
                    }
                }else{
                    $('#selectedDatasheet').prop('disabled',true)
                }

            });
            if($("#adhocRun").is(":disabled")){
                $('#isProductMining').prop('disabled',true);
            }
            if(!$("#selectedDatasheet").is(":checked")){
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.hide();
            }else{
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.show();
            }
            if($("#dataMiningVariable").val() == "null"){
                if(!$("#groupBySmq").is(":checked")){
                    $('#selectedDatasheet').prop('disabled',false);
                }
            }
            if($("#dataMiningVariable").val() != "null"){
                $('#selectedDatasheet').prop('disabled',true);
                $('#selectedDatasheet').prop('checked',false);
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.hide();
            }
            $("#allSheets").change(function(){
                if($(this).is(':checked')){
                    $(this).val('ALL_SHEET');
                }else{
                    $(this).val('CORE_SHEET');
                }
            });
//            addAllowedUsersList()
            addDrugClassificationList();
            setProductMining();
            if ($('#isTemplateAlert').is(':checked')) {
                $('form').submit(function (event) {
                    var currentForm = this;
                    event.preventDefault();
                    bootbox.confirm({
                        title: 'Update Template Alert ',
                        message: templateUpdateWarning,
                        buttons: {
                            confirm: {
                                label: 'Update',
                                className: 'btn-primary'
                            },
                            cancel: {
                                label: 'Cancel',
                                className: 'btn-default'
                            }
                        },
                        callback: function (result) {
                            if (result) {
                                $('.disable').prop('disabled', true);
                                currentForm.submit();
                            } else {
                                $('.disable').prop('disabled', false);
                                event.preventDefault();
                            }
                        }
                    });
                });
            } else {
                $('.disable').click(function () {
                    $(this).prop('disabled', true);
                });
            }
        })
    </script>

</head>
<body>
<g:set var="userService" bean="userService"/>

<div class="row" id="case-edit">
    <div class="col-sm-12">
        <div class="page-title-box">
            <div class="fixed-page-head">
                <div class="page-head-lt">
                    <h4 title="${configurationInstance.name ?:""}">${message(code: 'app.Edit.Alert', args: [configurationInstance.name ?: ""])}</h4>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-sm-12">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

        <form id="configurationForm" name="configurationForm" method="post" autocomplete="off" onsubmit="return onFormSubmit()">

            <g:render template="form" model="[dataSourceMap: dataSourceMap, configurationInstance: configurationInstance, userList: userList, priorityList: priorityList, templateList: templateList, spotfireEnabled:spotfireEnabled,isPVCM: isPVCM]"/>
            <g:hiddenField name="editable" id="editable" value="true"/>
            <g:hiddenField name="editAggregate" id="editAggregate" value="false"/>
            <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="editPage"/>

            %{--BEGIN: Button Bar  ==============================================================================================================--}%
            <div class="text-right">
                <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                    <g:actionSubmit accesskey="d" class="btn btn-primary repeat disable" data-action="${createLink(controller: 'aggregateCaseAlert',action: 'disable',params: [id:configurationInstance.id] )}" type="submit" value="${message(code:"default.button.unschedule.label")}"/>
                </g:if>
                <g:else>
                    <g:actionSubmit accesskey="r" class="btn btn-primary repeat disable" data-action="${createLink(controller: 'aggregateCaseAlert',action: 'run',params: [id:configurationInstance.id] )}" type="submit" value="${message(code: 'default.button.update.run.label')}"/>
                </g:else>
                <g:actionSubmit accesskey="u"  class="btn btn-primary repeat disable" data-action="${createLink(controller: 'aggregateCaseAlert',action: 'update',params: [id:configurationInstance.id] )}" type="submit" value="${message(code: 'default.button.update.label')}"/>
                <a class="btn btn-default pv-btn-grey" tabindex="0" accesskey="c"
                   href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
            </div>
        <input type="hidden" name="fgData" id="fgData">
            %{--END: Button Bar  ================================================================================================================--}%
            <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}" hidden="hidden"/>
            <input name="previousAction" id="previousAction" value="${action}" hidden="hidden"/>
        </form>

        <div>
            %{--<!-- Render the templateQuery template (_templateQuery.gsp) hidden so we can clone it -->--}%
            <g:render template='/templateQuery/templateQuery' model="['templateQueryInstance':null,'i':'_clone','hidden':true]"/>
            %{--</tbody>--}%

            <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV"   model="[type:'qev']"/></div>
            <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue"   model="[type:'qev']"/></div>
        </div>
    </div>
</div>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
<script>
    var pvaUrls= {
        selectUrl :options.product.selectUrl,
        preLevelParentsUrl: options.product.preLevelParentsUrl,
        searchUrl:options.product.searchUrl
    };
    var otherUrls= {
        selectUrl :"${createLink(controller: 'pvsProductDictionary', action: 'getSelectedProduct')}",
        preLevelParentsUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getPreLevelProductParents')}",
        searchUrl:"${createLink(controller: 'pvsProductDictionary', action: 'searchProducts')}"
    };
    changeDataSource("${configurationInstance?.selectedDatasource?:'pva'}");
    $('#configurationForm').submit(function() {
        updateDmvVariable();
        return true;
    });
    $(".queryExpressionValues").find(".select2-selection__choice").each(function () {
        if($(this).attr("title")==""){
            $(this).remove();
        }
    })
    $(".queryExpressionValues1").find(".select2-selection__choice").each(function () {
        if($(this).attr("title")==""){
            $(this).remove();
        }
    })
    $(".queryExpressionValues1").find(".select2-selection__choice").attr("title","").remove();
    $(".queryExpressionValues").find(".select2-selection__choice").attr("title","").remove();
</script>
</g:if>
</body>

