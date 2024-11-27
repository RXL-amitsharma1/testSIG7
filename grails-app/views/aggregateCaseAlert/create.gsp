<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag;grails.converters.JSON;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.new.aggregate.case.alert"/></title>

    <g:javascript>
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";

        var isAutoAssignedTo = ${isAutoAssignedTo};
        var isPVCM = ${isPVCM};
        var isValidationError= ${validationError ?: false};
        var isAutoSharedWith = ${isAutoSharedWith};
        var isMultipleDatasource = ${isMultipleDatasource};
        var sharedWithUrl = "${createLink(controller: 'user', action: 'searchShareWithUserGroupList')}";
        var fetchAssignmentForProductsUrl = "${createLink(controller: 'productAssignment', action: 'fetchAssignmentForProducts')}";

        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";

        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";

        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var allFieldsUrl = "${createLink(controller: 'query', action: 'getAllFields')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";

        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";

        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'getSubstanceFrequency')}";
        var queryList = "${createLink(controller: 'query', action: 'queryList')}";
        var dataSheetList = "${createLink(controller: 'dataSheet', action: 'dataSheets')}";
        var fetchFreqNameUrl = "${createLink(controller: "aggregateCaseAlert", action: "fetchFreqName")}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var fetchDrugClassificationUrl = "${createLink(controller: 'configurationRest', action: 'fetchDrugClassification')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var templateList = "${createLink(controller: 'template', action: 'templateList')}";
        var cioms1Id = "${cioms1Id}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var templateIdNameListUrl = "${createLink(controller: 'template', action: 'templateIdNameList')}";
        var queryIdNameListUrl = "${createLink(controller: 'query', action: 'queryIdNameList')}";
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
        var reportFieldsForQueryUrl = "${createLink(controller: 'query', action: 'reportFieldsForQueryValue')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var validateValue = "${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var getDmvData = "${createLink(controller: 'query', action: 'getDmvData')}";
        var exeVaersDateRange = "${exeVaersDateRange}";
        var exeVigibaseDateRange = "${exeVigibaseDateRange}";
        var productBasedSecurity = ${Holders.config.pvsignal.product.based.security};
        var multipleDBEnabled = ${Holders.config.pvsignal.multiple.datasource.toBoolean()};

        var LABELS = {
            labelShowAdvancedOptions : "${message(code: 'add.header.title.and.footer')}",
            labelHideAdvancedOptions : "${message(code: 'hide.header.title.and.footer')}"
        };

        var appLabelProductGroup = "Quantitative Alert";
        var userIdList = "${userList.collect { it.id }.join(",")}".split(",");
        var configurationMiningVariable ="${configurationInstance?.dataMiningVariable}";
        var editAlert = "${action}";
        var hasNormalAlertExecutionAccess = ${hasNormalAlertExecutionAccess};
        var byDefaultPriority       = '${byDefaultPriority}';
        var enabledDataSourceList= '${enabledOptions}';
        var dataSheets = "${dataSheetList}";
        var productTypeOptions = JSON.parse("${productTypeMap as JSON}");
        var isAlertScheduled = ${configurationInstance?.nextRunDate ? true : false};
        var isWhoDictionary = true;

        $(function () {
            $(".priority-List").find(".select2").select2();
            var dataSourceObject ={'pva':'Safety DB','eudra':'EVDAS','faers':'FAERS','vaers':'VAERS','vigibase':'VigiBase','jader':'JADER'};
            $('#productModal').on('show.bs.modal', function(){
            var selectedDatasource=$('#selectedDatasource').val()
            if(selectedDatasource){
            var element = $( this );
                element.find('.dictionaryItem').attr( "title", dataSourceObject[selectedDatasource]);
                element.find('#dataSourcesProductDict').val(selectedDatasource).trigger('change');
            }
            });
         });
    </g:javascript>

    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_product_assignment.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/userGroupSelect.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_study_utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/alert_event_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <asset:javascript src="app/pvs/tags.js"/>
    <asset:javascript src="app/pvs/groups.js"/>
    <asset:javascript src="app/pvs/configuration/templateQueries.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js" asset-defer="defer"/>
    <asset:javascript src="app/pvs/configuration/dateRange.js"/>
    <asset:javascript src="app/pvs/configuration/blankParameters.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_query_utils.js"/>
    <asset:javascript src="app/pvs/datahseet.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <script>
        $(function () {
            function disableProductSelection() {
                $('#productSelectionDictionarySection').addClass('disabled');

                $('#productSelectionDictionarySection .iconSearch a').attr('disabled', true);

                $('#productSelectionDictionarySection .iconSearch a').click(function (e) {
                    e.preventDefault();
                });
                $("#showProductSelection").html("");
                $('input[name="productSelection"]').val('');
                $("#productGroupSelection").val("");
                if ($('#productHierarchySelectDropDown').val() != '') {
                    $("#showProductSelection").append("<div style='padding: 5px'> All Products (" + $('#productHierarchySelectDropDown').val() + ")</div>");
                }
            }

            function enableProductSelection() {
                $('#productSelectionDictionarySection').removeClass('disabled');
                $('#productSelectionDictionarySection .iconSearch a').off('click');
                $("#showProductSelection").html("");
                $('#productHierarchySelectDropDown').val(null).trigger('change');
            }

            function masterTemplateEnabled() {
                $('#isTemplateAlert').prop('checked', false).prop('disabled', true);
                $('#saveRun').prop('disabled', true);
                $('#alertGroupCheckBox').prop('disabled', false);
                $('#selectedProductHierarchy').show();
                $('#selectedDatasource').prop('disabled', true);
                $('#reportSection').removeClass('show').addClass('hide');
                $('#advacedOptionSection').removeClass('show').addClass('hide');
                $('#selectedDatasheet').prop('checked', false).prop('disabled', true);
                $(".datasheet-options").hide();
                $('#foregroundQuery').prop('checked', false).prop('disabled', true);
                $(".forgroundQuery").hide();
                $('#adhocRun').prop('checked', false).prop('disabled', true);
                $('#studyDrugCases').prop('checked', false).prop('disabled', true);
                $('#myScheduler :input').prop('disabled', false);
                $('#priority').prop('disabled', false);
                $('#infoLink').show();
                let bindData = {"id": "AUTO_ASSIGN", "name": "Auto Assign"}
                $('#assignedTo').empty()
                $('#assignedTo').prop('disabled', true);
                bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, bindData, false, true);
                $('#sharedWith').empty()
                let sharedData = [{"id": "AUTO_ASSIGN", "name": "Auto Assign"}]
                bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedData, true, false);
                disableProductSelection();
                if ($('#nameTextbox').val() == '' || $('#nameTextbox').val() == null) {
                    $('#nameTextbox').val('[Product Hierarchy]-[Product Name]-[Event Hierarchy]');
                }
            }

            function masterTemplateDisabled() {
                $('#alertGroupCheckBox').prop('checked', false).prop('disabled', true);
                $('#isTemplateAlert').prop('disabled', false);
                $('#saveRun').prop('disabled', false);
                $('#selectedProductHierarchy').hide();
                $('#selectedDatasource').prop('disabled', false);
                $('#reportSection').removeClass('hide').addClass('show');
                $('#advacedOptionSection').removeClass('hide').addClass('show');
                if (!$('#groupBySmq').is(':checked') && !$('#isMasterTemplateAlert').is(':checked')) {
                    $('#selectedDatasheet').prop('disabled', false);
                }
                $('#foregroundQuery').prop('disabled', false);
                $('#adhocRun').prop('disabled', false);
                enableProductSelection();
                $('#alertGroup').val(null).trigger('change');
                $('#alertGroupDiv').hide();
                $('#infoLink').hide();
                $('#assignedTo').empty();
                $('#assignedTo').prop('disabled', false);
                bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, "", false);
                $('#sharedWith').empty();
                bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedWithData, true);
                if ($('#nameTextbox').val() == '[Product Hierarchy]-[Product Name]-[Event Hierarchy]') {
                    $('#nameTextbox').val('');
                }
            }

            if ($('#isMasterTemplateAlert').is(':checked')) {
                masterTemplateEnabled()
            }

            function checkDatasourceAndToggleMasterTemplateCheckbox() {
                var selectedOptions = $('#selectedDatasource option:selected');
                var selectedDatasourceCount = selectedOptions.length;
                var hasPva = false;
                var productSelection = $("#productSelection").val();
                var productGroupSelection = $("#productGroupSelection").val();
                selectedOptions.each(function () {
                    if ($(this).val() === 'pva') {
                        hasPva = true;
                    }
                });
                if (selectedDatasourceCount > 1 || (selectedDatasourceCount === 1 && !hasPva) || (multipleDBEnabled && selectedDatasourceCount === 0)) {
                    $('#isMasterTemplateAlert').prop('disabled', true);
                    masterTemplateDisabled();

                } else if (productSelection == '' && (productGroupSelection == '' || productGroupSelection == '[]')) {
                    $('#isMasterTemplateAlert').prop('disabled', false);
                    if ($('#isMasterTemplateAlert').is(':checked')) {
                        masterTemplateEnabled();
                    }
                }
            }

            $('#selectedDatasource').change(function () {
                checkDatasourceAndToggleMasterTemplateCheckbox();
            });
            $('#myScheduler :input').prop('disabled', $("#adhocRun").is(':checked'));
            $("#adhocRun").on('change', function () {
                $('#myScheduler :input').prop('disabled', $(this).is(':checked'));
                disableDataMiningVariableFields(!$(this).is(':checked'))
                $('#priority').prop('disabled', $(this).is(':checked'));
                $('#isTemplateAlert').prop('disabled', $(this).is(':checked'));
                if ($(this).is(':checked')) {
                    $('#isMasterTemplateAlert').prop('disabled', $(this).is(':checked'));
                } else {
                    checkDatasourceAndToggleMasterTemplateCheckbox();

                }
                if ($(this).is(':checked') && $('#isTemplateAlert').is(':checked')) {
                    $('#isTemplateAlert').prop('checked', false);
                }
                if ($('#selectedDatasource').val() == dataSources.VAERS || $('#selectedDatasource').val() == dataSources.VIGIBASE || $('#selectedDatasource').val() == dataSources.EUDRA) {
                    disableDataMiningVariableFields(true);
                }
                if ($('#selectedDatasource').val() == dataSources.JADER) {
                    $('#isTemplateAlert').prop('checked', false);

                }
                if (!$(this).is(':checked') && !$('#groupBySmq').is(':checked') && !$('#isMasterTemplateAlert').is(':checked')) {
                    $('#isTemplateAlert').prop('disabled', false);
                }
                Reset();
            });
            $('#isMasterTemplateAlert').on('change', function() {
                if ($(this).is(':checked')) {
                    masterTemplateEnabled()
                } else {
                    masterTemplateDisabled()
                }
            });

            // Prevent removal of the Auto Assign option when MAster Template checkbox is checked
            $('#sharedWith').on('select2:unselecting', function(e) {
                if ($('#isMasterTemplateAlert').is(':checked') && e.params.args.data.id === 'AUTO_ASSIGN') {
                    e.preventDefault();
                }
            });
            $('#isTemplateAlert').on('change', function() {
                if ($(this).is(':checked')) {
                    $('#isMasterTemplateAlert').prop('checked', false).prop('disabled', true);
                } else {
                    $('#isMasterTemplateAlert').prop('disabled', false);
                }
            });
            if ($('#groupBySmq').is(':checked')) {
                $('#selectedDatasheet').prop('checked', false)
                $('#selectedDatasheet').prop('disabled', true)
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.hide();
            }
            $('#groupBySmq').on('change', function () {
                if($(this).is(':checked')){
                    $('#selectedDatasheet').prop('checked',false)
                    var dataSheetOptions = $(".datasheet-options");
                    dataSheetOptions.hide();
                }
                if (!$(this).is(':checked')) {
                    if ($("#dataMiningVariable").is(':disabled') && !$('#isMasterTemplateAlert').is(':checked')) {
                        $('#selectedDatasheet').prop('disabled', false)
                    } else if ($("#dataMiningVariable").val() === "null" && !$('#isMasterTemplateAlert').is(':checked')) {
                        $('#selectedDatasheet').prop('disabled', false)
                    } else {
                        $('#selectedDatasheet').prop('disabled', true)
                    }
                } else {
                    $('#selectedDatasheet').prop('disabled', true)
                }

            });
            $("#allSheets").on('change', function(){
                if($(this).is(':checked')){
                    $(this).val('ALL_SHEET')
                } else {
                    $(this).val('CORE_SHEET')
                }
            });
            if (productBasedSecurity == true) {
                $("#productSelection").on("change", function () {
                    addAllowedUsersList();
                });
            }
            if (!$("#adhocRun").is(":checked")) {
                $('#myScheduler :input').prop('disabled', false);
                disableDataMiningVariableFields(true);
                $('#isProductMining').prop('disabled', true);

            }
            if (!$("#selectedDatasheet").is(":checked")) {
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.hide();
            } else {
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.show();
            }
            if (typeof hasNormalAlertExecutionAccess != "undefined" && !hasNormalAlertExecutionAccess) {
                $("#adhocRun").prop('disabled', true);
            }
            if ($("#adhocRun").is(":disabled")) {
                $('#isProductMining').prop('disabled', true);
            }
            if ($("#dataMiningVariable").val() == "null") {
                $('#isProductMining').prop('disabled', true);
                if (!$("#groupBySmq").is(":checked") && !$('#isMasterTemplateAlert').is(':checked')) {
                    $('#selectedDatasheet').prop('disabled', false);
                }
            }
            if ($("#dataMiningVariable").val() != "null") {
                $('#selectedDatasheet').prop('disabled', true);
                $('#selectedDatasheet').prop('checked', false);
                var dataSheetOptions = $(".datasheet-options");
                dataSheetOptions.hide();
            }
            setProductMining()
            $("#productSelection").on("change", function () {
                addDrugClassificationList()
            });
            $("#productModal").on('hidden.bs.modal', function () {
                var productSelection = $("#productSelection").val();
                var productGroupSelection = $("#productGroupSelection").val();
                console.log(productGroupSelection);
                console.log(productGroupSelection != '[]');
                console.log(productSelection != '');
                console.log(productSelection);
                if(productSelection != '' || productGroupSelection != '[]'){
                    $('#isMasterTemplateAlert').prop('disabled', true);
                } else if (!$("#adhocRun").is(":checked")){
                    $('#isMasterTemplateAlert').prop('disabled', false);
                }
            });
        });
    </script>
</head>

<body>
<g:set var="userService" bean="userService"/>
<input type="hidden" id="selectedDatasheets" value="CORE_SHEET" autocomplete="off">
<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

<form class="aggCaseAlertForm" id="configurationForm" name="configurationForm" method="post" autocomplete="off"
      onsubmit="return onFormSubmit()">
    <g:render template="form"
              model="[dataSourceMap: dataSourceMap, configurationInstance: configurationInstance, priorityList: priorityList, appType: appType,
                      userList     : userList, action: action, sMQList: sMQList, templateList: templateList, productGroupList: productGroupList, spotfireEnabled: spotfireEnabled, isPVCM: isPVCM, listOfSelectedDataSource: listOfSelectedDataSource, productHierarchy: productHierarchy, eventHierarchy: eventHierarchy]"/>
    <g:hiddenField name="editAggregate" id="editAggregate" value="false"/>
    <g:hiddenField name="schedulerFrom" id="schedulerFrom" value="createPage"/>
    <div class="text-right m-t-15">
        <g:actionSubmit class="btn primaryButton btn-primary repeat" id="saveRun" tabindex="0" accesskey="r"
                        data-action="${createLink(controller: 'aggregateCaseAlert', action: 'run')}" type="submit"
                        value="${message(code: 'default.button.saveAndRun.label')}"/>
        <g:actionSubmit class="btn btn-default repeat btn-primary" id="saveBtn" tabindex="0" accesskey="s"
                        data-action="${createLink(controller: 'aggregateCaseAlert', action: 'save')}" type="submit"
                        value="${message(code: 'default.button.save.label')}"/>
        <a class="btn btn-default pv-btn-grey" accesskey="c" tabindex="0"
           href="${createLink(controller: 'configuration', action: 'index')}">${message(code: "default.button.cancel.label")}</a>
    </div>
    <input type="hidden" name="fgData" id="fgData">
    <input name="blankValuesJSON" id="blankValuesJSON" value="${configurationInstance?.blankValuesJSON}"
           hidden="hidden"/>
    <input name="previousAction" id="previousAction" value="${action}" hidden="hidden"/>
    <g:hiddenField name="signalId" id="signalId" value="${signalId}"/>
</form>

<div>
    <g:render template='/templateQuery/templateQuery'
              model="[templateQueryInstance: null,
                      i                    : '_clone',
                      hidden               : true,
                      templateId           : templateId,
                      clone                : clone]"/>
    <div class="expression" hidden="hidden"><g:render template="/query/toAddContainerQEV" model="[type: 'qev']"/></div>

    <div class="expression" hidden="hidden"><g:render template="/query/customSQLValue" model="[type: 'qev']"/></div>
</div>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
<script>
    var pvaUrls = {
        selectUrl: options.product.selectUrl,
        preLevelParentsUrl: options.product.preLevelParentsUrl,
        searchUrl: options.product.searchUrl
    };
    function Reset() {
        var dropDown = document.getElementById("dataMiningVariable");
        dropDown.selectedIndex = 0;
    }
    changeDataSource("${configurationInstance?.selectedDatasource?:'pva'}");
    $('#configurationForm').on('submit', function() {
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
    <style>
    #productSelectionDictionarySection.disabled {
        pointer-events: none;
        opacity: 0.5;
        background-color: #dbdbdb;
    }
    .select2-selection__choice[title="Auto Assign"] .select2-selection__choice__remove {
        display: none;
    }
    </style>
</g:if>
</body>
