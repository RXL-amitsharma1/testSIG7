var dataSources = {"PVA": "pva", "Vigi Base": 'vigibase', "FAERS": 'faers', 'EUDRA': 'eudra',"JADER":"jader"};
var isFirstTimeCommonChange = true
var initializing = true

$(function () {
    var init = function () {
        $('.dispositions').select2();
        init_generic_name();
        manipulateSelection();
        showHideReportsSection();
        onSubmitBusinessConfigForm();
    };

    var manipulateSelection = function () {
        $("input:radio[name=optradio]").on('click', function () {
            if ($(this).hasClass("productRadio")) {
                showProductSelectionTextArea();
                $("#limitToSuspectProduct").prop("checked", true);
                $("#limitToSuspectProduct").prop("disabled", false);
                $("#limitTosuspectProduct").val(true);
            } else if ($(this).hasClass("studyRadio")) {
                showStudySelectionTextArea();
                $("#limitToSuspectProduct").prop("checked", false);
                $("#limitToSuspectProduct").prop("disabled", true);
                $("#limitTosuspectProduct").val(false);
                $('#studyDrugCases').prop("disabled",true);
                $('#studyDrugCases').prop("checked",false);
                $('#additionalOptions').addClass('hidden');
                $('#ignoreMedicationType').prop('checked',false);
                $('#considerPOI').prop('checked',false);
            } else if ($(this).hasClass("genericRadio")) {
                showGenericSelectionTextArea();
            } else if ($(this).hasClass("productGroupRadio")) {
                showProductGroupSelectionTextArea();
            }
            if(!($(this).hasClass("productRadio") && !$(this).hasClass("studyRadio") && !$(this).hasClass("genericRadio"))){
                resetPreviousSelection();
            }
        });

        var showProductSelectionTextArea = function () {
            $($("#showProductSelection").parent()).removeAttr("hidden");
            $($("#showStudySelection").parent()).attr("hidden", "hidden");
            $($("#showGenericSelection").parent()).attr("hidden", "hidden");
            $($("#showProductGroupSelection").parent()).attr("hidden", "hidden");
        };

        var showStudySelectionTextArea = function () {
            $($("#showStudySelection").parent()).removeAttr("hidden");
            $($("#showProductSelection").parent()).attr("hidden", "hidden");
            $($("#showGenericSelection").parent()).attr("hidden", "hidden");
            $($("#showProductGroupSelection").parent()).attr("hidden", "hidden");
        };

        var showGenericSelectionTextArea = function () {
            $($("#showGenericSelection").parent()).removeAttr("hidden");
            $($("#showProductSelection").parent()).attr("hidden", "hidden");
            $($("#showStudySelection").parent()).attr("hidden", "hidden");
            $($("#showProductGroupSelection").parent()).attr("hidden", "hidden");
        };

        var showProductGroupSelectionTextArea = function () {
            $($("#showGenericSelection").parent()).attr("hidden", "hidden");
            $($("#showProductSelection").parent()).attr("hidden", "hidden");
            $($("#showStudySelection").parent()).attr("hidden", "hidden");
            $($("#showProductGroupSelection").parent()).removeAttr("hidden");
        };

        var resetPreviousSelection = function () {
            resetProductSelection();
            resetStudySelection();
            resetGenericSelection();
            resetProductGroupSelection();
        };

         var resetProductSelection = function() {
             $("#productSelection").val('');
             $('#showProductSelection').html('');
         };

         var resetStudySelection = function() {
             $('#showStudySelection').html('');
             $("#studySelection").val('');
         };

         var resetGenericSelection = function() {
             $('#showGenericSelection').html('');
             $("#genericSelection").val('');
         };

         var resetProductGroupSelection = function() {
             $('#showProductGroupSelection').html('');
             $("#productGroups").val("");
             $("#productGroups").trigger("change");
         };
    };

    var init_generic_name = function () {
        $("#js-data-generics").select2({
            placeholder: "Search For Generic Names",
            minimumInputLength: 1,
            multiple: true,
            allowClear: true,
            ajax: {
                url: searchGenericsUrl,
                dataType: 'json',
                data: function (params) {
                    return {
                        term: params.term,
                        max: params.page || 10
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 10;
                    return {
                        results: data,
                        pagination: {
                            more: (params.page * 10) < data.length
                        }
                    };
                }
            }
        }).on('select2:select', function (e) {
            $('#js-data-generics').append(e.params.data.id);
        });

    };

    $("button.addGenericValues").on("click", function () {
        add_generic_name()
    });

    $("button.clearGenericValues").on("click", function () {
        $("#js-data-generics").val(null).trigger('change');
    });

    var add_generic_name = function () {
        var generic_names = [];
        var genericData = $('#js-data-generics').select2('data');
        $.each(genericData, function (index, element) {
            var selectedObj = {"genericName": element.text};
            generic_names.push(element.text);
            productValues[5].push(selectedObj)
        });

        if (generic_names.length > 0) {
            $("#productSelection").val(JSON.stringify(productValues));
            $("#showGenericSelection").html("");
            $("#showGenericSelection").append("<div style='padding: 5px'>" + generic_names + "</div>")
        }
    };

    $('#myDatePicker').datepicker({
        date: $("#myDatePicker").val() ? new Date($("#myDatePicker").val()) : null,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $("#productModal").on('show.bs.modal', function () {

        if (typeof $("#product_multiIngredient") != "undefined" && typeof $("#isMultiIngredient") != "undefined") {
            if ($("#isMultiIngredient").val() == 'true') {
                $("#product_multiIngredient").prop('checked', true);
            } else {
                $("#product_multiIngredient").prop('checked', false);
            }
        }
    });

    init();

});

function addAllowedUsersList(){
    if($('#productSelection').val()) {
        var selectedProducts = JSON.parse($('#productSelection').val());
        var selectedProductList = getProductNameList(selectedProducts);
        if (selectedProductList) {
            $.ajax({
                url: fetchAllowedUsersUrl,
                data: {'productList': selectedProductList},
            })
                .done(function (response) {
                    if ($('#selectedDatasource').val() != dataSources.FAERS) {
                        $('#assignedTo').html('');
                        $('#assignedTo').append('<option value="null">--Select One--</option>');
                        $.each(response, function () {
                            $('#assignedTo').append('<option value="' + this.id + '">' + this.fullName + '</option>');
                        });
                    }
                    if ($('#assignedTo').data('value')) {
                        $('#assignedTo').val($('#assignedTo').data('value'))
                    }
            })
        }
    }
}

function addDrugClassificationList(){
    if($('#selectedDatasource').val() && $('#selectedDatasource').val().length===1 && $('#selectedDatasource').val()[0] === dataSources.FAERS && $("#productSelection").val()){
        var selectedProducts = JSON.parse($('#productSelection').val());
        var selectedProductList =  getDictionaryNameList(selectedProducts);
        $.ajax({
            url: fetchDrugClassificationUrl,
            data: {'product': selectedProductList},
        })
            .done(function (response) {
                $('#drugClassification').html('');
                $('#drugClassification').append('<option value="">--Select One--</option>');
                $.each(response, function () {
                    $('#drugClassification').append('<option value="' + this.className + '">' + this.className + '</option>');
                });
                if($('#drugClassification').data('value')){
                    $('#drugClassification').val($('#drugClassification').data('value'))
                }
            })
    }
}

function getProductNameList(obj) {
    var productArray = [];
    var objArray = obj['3'];
    $.each(objArray, function (index, value) {
        productArray.push(value.name);
    });
    return productArray.join(',');
}

//Fetchs the names of dictionary selected values.
function getDictionaryNameList(obj) {
    var productArray = [];
    $.each(obj, function(i, val) {
        $.each(val, function (objIndex, objValue) {
            productArray.push(objValue.name);
        });
    })
    return productArray.join(',');
}

function showHideReportsSection() {
    function reportSecAttribStatusChange() {
        if ($('#selectedDatasource').val()) {
            if (!$('#selectedDatasource').val().includes(dataSources.PVA)) {
                if (!$('#selectedDatasource').val().includes(dataSources.PVA)) {
                    if(( typeof editAlert != "undefined" && editAlert === "create")){
                        $('#alertQuery').val('').trigger('change');
                        $('#alertQuery').prop("disabled", true);
                    }
                    $('#dateRangeType').val('CASE_RECEIPT_DATE').trigger('change');
                    $('#dateRangeType').prop("disabled", true);
                    $('#evaluateDateAsNonSubmission').val('LATEST_VERSION').trigger('change');
                    $('#evaluateDateAsNonSubmission').prop("disabled", true);
                    if (typeof initializing != "undefined" && !initializing) {
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                    }
                }
            }
            if ($('#selectedDatasource').val().includes(dataSources.EUDRA)) {
                $('#groupBySmq').prop("checked", false).prop("disabled", true);
                $('#selectedDatasheet').prop('disabled',false)
            }
            if (isDataSourceEnabled && $('#selectedDatasource').val().length > 1) {
                if( ($('#selectedDatasource').val() === "pva" &&  $('#selectedDatasource').val().length === 3) || ($('#selectedDatasource').val() === "eudra" &&  $('#selectedDatasource').val().length === 5)){
                    // this check is done for single case alert as here selected data source is coming as string instead of list
                    if (typeof editAlert != "undefined" && editAlert === "create") {
                        $("#adhocRun").prop("checked", true).prop("disabled", false);
                    }
                }
                else {
                    if(typeof editAlert != "undefined" && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.FAERS)){
                        $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                    }
                    $("#adhocRun").prop("checked", false).prop("disabled", true);
                }
                disableDataMiningVariableFields(true);
                $('#isProductMining').prop('disabled', true);
                if( ($('#selectedDatasource').val() === "pva" &&  $('#selectedDatasource').val().length === 3) || ($('#selectedDatasource').val() === "eudra" &&  $('#selectedDatasource').val().length === 5)){
                    // this check is done for single case alert or Evdas alert Configuration
                    if (typeof editAlert != "undefined" && editAlert === "create") {
                        enableDisablePriorityPopulate(true)
                        $('#myScheduler :input').prop('disabled', true);
                    }
                }
                else {
                    enableDisablePriorityPopulate(false)
                    $('#myScheduler :input').prop('disabled', false);
                }
            } else {
                if(typeof hasNormalAlertExecutionAccess != "undefined" && hasNormalAlertExecutionAccess) {
                    $("#adhocRun").prop('disabled', false);
                }
                if ($("#adhocRun").is(":checked")) {
                    enableDisablePriorityPopulate(true)
                }
            }
            var drugOption = new Option("Drug", "DRUG", false, false);
            var vaccineOption = new Option("Vaccine", "VACCINCE", false, false);
            if ($('#selectedDatasource').val().includes(dataSources.PVA)) {
                $('#reportSection').removeClass('hide');
                $('#reportSection').addClass('show');
            }
            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.FAERS) {
                if ($('#isSpotfireEnabled').val() == 'false') {
                    $('#reportSection').removeClass('show');
                    $('#reportSection').addClass('hide');
                } else if ($('#isSpotfireEnabled').val() === 'true') {
                    $('#reportSection').removeClass('hide');
                    $('#reportSection').addClass('show');
                }
                if(( typeof editAlert != "undefined" && editAlert === "create")){
                    $('#alertQuery').val('').trigger('change');
                    $('#alertQuery').prop("disabled", false);
                }
                $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                $('#templateQueryList').removeClass('show');
                $('#templateQueryList').addClass('hide');
                $('.report-section-buttons').removeClass('show');
                $('.report-section-buttons').addClass('hide');
                $('.drugClassificationContainer').show();
                $('input[type="submit"]').prop("disabled", false);
                $(".spotfire_type").val(null).trigger("change");
                $(".spotfire_type").empty();
                $(".spotfire_type").append(drugOption).trigger('change.select2');
                $(".spotfire_type").append(vaccineOption).trigger('change.select2');
                $("#spotfireType").find(vaccineOption).attr('disabled', 'disabled').trigger('change');
                if (!$("#adhocRun").is(":checked")) {
                    enableDisablePriorityPopulate(false)
                    $('#myScheduler :input').prop('disabled', false);
                }
            } else if ($('#selectedDatasource').val().length == 1 && ($('#selectedDatasource').val()[0] === dataSources.VIGIBASE)) {
                    $('#reportSection').removeClass('show');
                    $('#reportSection').addClass('hide');
                $('.drugClassificationContainer').hide();
                $('#alertQuery').val('').trigger('change');
                $('#alertQuery').prop("disabled", true);
                $('#includeLockedVersion').prop("checked", false).prop("disabled", true);
                $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                $('#templateQueryList').removeClass('show');
                $('#templateQueryList').addClass('hide');
                $('.report-section-buttons').removeClass('show');
                $('.report-section-buttons').addClass('hide');
                $('input[type="submit"]').prop("disabled", false);
                var drugOpt = new Option("Drug", "DRUG", true, true);
                var vaccineOpt = new Option("Vaccine", "VACCINCE", false, false);
                $(".spotfire_type").val(null).trigger("change");
                $(".spotfire_type").empty();
                $(".spotfire_type").append(drugOpt).trigger('change.select2');
                $(".spotfire_type").append(vaccineOpt).trigger('change.select2');
                $(".spotfire_type").val("DRUG").trigger('change');
                if (!$("#adhocRun").is(":checked")) {
                   if(!(typeof editAlert != "undefined" &&  editAlert === "copy")){
                       enableDisablePriorityPopulate(true)
                       $('#myScheduler :input').prop('disabled', true);
                   }
                }
            } else if ($('#selectedDatasource').val().length == 1 && ($('#selectedDatasource').val()[0] === dataSources.JADER)) {
                $('#reportSection').removeClass('show').addClass('hide');
                $('.drugClassificationContainer').hide();
                $('#alertQuery').val('').trigger('change');
                $('#alertQuery').prop("disabled", true);
                $('#missedCases').prop("checked", false).prop("disabled", true);
                $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                $('#templateQueryList').removeClass('show');
                $('#templateQueryList').addClass('hide');
                $('.report-section-buttons').removeClass('show');
                $('.report-section-buttons').addClass('hide');
                $('#isTemplateAlert').prop('disabled', true);
                $('#isMasterTemplateAlert').prop('disabled', true);
                if (!$("#adhocRun").is(":checked")) {
                    if(!(typeof editAlert != "undefined" &&  editAlert === "copy")){
                        var isDefaultPriotory = $("#priority").val() == 'null' ? false : true;
                        if(isDefaultPriotory && editAlert === "create"){
                            enableDisablePriorityPopulate(false)
                        }else {
                            enableDisablePriorityPopulate(true)
                        }
                        $('#myScheduler :input').prop('disabled', true);
                    }
                }
            } else if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.EUDRA) {
                disableDataMiningVariableFields(true)
                $.Notification.notify('warning', 'top right', "Warning", " Please configure EVDAS alerts from EVDAS configuration option available under Alert configuration section", {autoHideDelay: 5000});
                $('input[type="submit"]').prop("disabled", true);
            } else if ($('#selectedDatasource').val().length == 1 && ($('#selectedDatasource').val()[0] === dataSources.VAERS)) {
                if ($('#isSpotfireEnabled').val() == 'false') {
                    $('#reportSection').removeClass('show');
                    $('#reportSection').addClass('hide');
                } else if ($('#isSpotfireEnabled').val() === 'true') {
                    $('#reportSection').removeClass('hide');
                    $('#reportSection').addClass('show');
                }
                $('#alertQuery').val('').trigger('change');
                $('#alertQuery').prop("disabled", true);
                $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                $('#templateQueryList').removeClass('show');
                $('#templateQueryList').addClass('hide');
                $('.report-section-buttons').removeClass('show');
                $('.report-section-buttons').addClass('hide');
                $('input[type="submit"]').prop("disabled", false);
                var drugOpt = new Option("Drug", "DRUG", false, false);
                var vaccineOpt = new Option("Vaccine", "VACCINCE", true, true);
                $(".spotfire_type").val(null).trigger("change");
                $(".spotfire_type").empty();
                $(".spotfire_type").append(vaccineOpt).trigger('change.select2');
                $(".spotfire_type").append(drugOpt).trigger('change.select2');
                $("#spotfireType").find(drugOpt).attr('disabled', 'disabled').trigger('change');
                if (!$("#adhocRun").is(":checked")) {
                    enableDisablePriorityPopulate(true)
                    $('#myScheduler :input').prop('disabled', true);
                }
            } else {
                $('#templateQueryList').removeClass('hide');
                $('#templateQueryList').addClass('show');
                $('.report-section-buttons').removeClass('hide');
                $('.report-section-buttons').addClass('show');
                $('.drugClassificationContainer').hide();
                $(".spotfire_type").val(null).trigger("change");
                $(".spotfire_type").empty();
                $(".spotfire_type").append(drugOption).trigger('change.select2');
                $(".spotfire_type").append(vaccineOption).trigger('change.select2');
                $('input[type="submit"]').prop("disabled", false);
                if ($('#selectedDatasource').val().includes("faers")) {
                    $("#spotfireType").find(vaccineOption).attr('disabled', 'disabled').trigger('change');
                } else {
                    $("#drugType").trigger('change');
                }
                if ($('#selectedDatasource').val().includes(dataSources.PVA) && ($('#selectedDatasource').val().includes(dataSources.VAERS))) {
                    var drugOpt = new Option("Drug", "DRUG", false, false);
                    var vaccineOpt = new Option("Vaccine", "VACCINCE", true, true);
                    $(".spotfire_type").val(null).trigger("change");
                    $(".spotfire_type").empty();
                    $(".spotfire_type").append(vaccineOpt).trigger('change.select2');
                    $(".spotfire_type").append(drugOpt).trigger('change.select2');
                    $("#spotfireType").find(drugOpt).attr('disabled', 'disabled').trigger('change');
                    if (typeof initializing != "undefined" && !initializing) {
                        if( typeof editAlert != "undefined" &&( typeof editAlert === "copy" || editAlert === "create")){
                        $('#excludeNonValidCases').prop("checked", true).prop("disabled", false);
                        $('#excludeFollowUp').prop("checked", false).prop("disabled", true);
                        }
                        $('#missedCases').prop("checked", true).prop("disabled", false);
                    }
                }
                if ( $('#selectedDatasource').val().includes(dataSources.VAERS) && ($('#selectedDatasource').val().includes(dataSources.VIGIBASE))) {
                    var drugOpt = new Option("Drug", "DRUG", false, false);
                    var vaccineOpt = new Option("Vaccine", "VACCINCE", true, true);
                    $(".spotfire_type").val(null).trigger("change");
                    $(".spotfire_type").empty();
                    $(".spotfire_type").append(vaccineOpt).trigger('change.select2');
                    $(".spotfire_type").append(drugOpt).trigger('change.select2');
                    $("#spotfireType").find(drugOpt).attr('disabled', 'disabled').trigger('change');
                }
                if ($('#selectedDatasource').val().includes(dataSources.PVA) && ($('#selectedDatasource').val().includes(dataSources.FAERS))) {
                    $('#excludeNonValidCases').prop("checked", true).prop("disabled", false);
                    $('#missedCases').prop("checked", true).prop("disabled", false);
                }
                if ($('#selectedDatasource').val().includes(dataSources.PVA) && $('#selectedDatasource').val().includes(dataSources.VIGIBASE) && (!$('#selectedDatasource').val().includes(dataSources.VAERS) && !$('#selectedDatasource').val().includes(dataSources.FAERS))) {
                    var drugOpt = new Option("Drug", "DRUG", true, true);
                    var vaccineOpt = new Option("Vaccine", "VACCINCE", false, false);
                    $(".spotfire_type").val(null).trigger("change");
                    $(".spotfire_type").empty();
                    $(".spotfire_type").append(vaccineOpt).trigger('change.select2');
                    $(".spotfire_type").append(drugOpt).trigger('change.select2');
                    if (typeof initializing != "undefined" && !initializing) {
                        $('#excludeNonValidCases').prop("checked", true).prop("disabled", false);
                        $('#excludeFollowUp').prop("checked", true).prop("disabled", false);
                        $('#missedCases').prop("checked", true).prop("disabled", false);
                    }
                }
            }
            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.PVA) {
                if(( typeof editAlert != "undefined" && editAlert === "create" && !isValidationError)){
                    $('#excludeFollowUp').prop("checked", true).prop("disabled", false);
                    $('#missedCases').prop("checked", true).prop("disabled", false);
                    if(!(typeof hasNormalAlertExecutionAccess != "undefined" && !hasNormalAlertExecutionAccess)) {
                        $("#adhocRun").prop("checked", true).prop("disabled", false);
                    }
                    $('#excludeNonValidCases').prop("checked", true).prop("disabled", false);
                }
                var drugOpt = new Option("Drug", "DRUG", false, false);
                var vaccineOpt = new Option("Vaccine", "VACCINCE", true, true);
                $(".spotfire_type").val(null).trigger("change");
                $(".spotfire_type").empty();
                $(".spotfire_type").append(drugOpt).trigger('change.select2');
                $(".spotfire_type").append(vaccineOpt).trigger('change.select2');
                $(".spotfire_type").val("DRUG").trigger('change');
            }
            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.VAERS) {
                disableDataMiningVariableFields(true);
                $('#isProductMining').prop('disabled', true);
            }

        } else {
            $('#alertQuery').val('').trigger('change');
            $('#alertQuery').prop("disabled", true);
            $('#dateRangeType').val('CASE_RECEIPT_DATE').trigger('change');
            $('#dateRangeType').prop("disabled", true);
            $('#evaluateDateAsNonSubmission').val('LATEST_VERSION').trigger('change');
            $('#evaluateDateAsNonSubmission').prop("disabled", true);
        }
        if($('#selectedDatasource').val() && !$('#selectedDatasource').val().includes(dataSources.PVA)){
            $('#templateQueryList').removeClass('show').addClass('hide');
            $('.report-section-buttons').removeClass('show').addClass('hide');
        }

        if($('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.PVA)){
            if(( typeof editAlert != "undefined" && editAlert === "create")){
                $('#alertQuery').val('').trigger('change');
                $('#alertQuery').prop("disabled", false);
            }
            $('#reportSection').removeClass('hide').addClass('show');
            $('#templateQueryList').removeClass('hide').addClass('show');
            $('.report-section-buttons').removeClass('hide').addClass('show');
        }
        if($('#selectedDatasource').val() &&  $('#selectedDatasource').val().includes(dataSources.VIGIBASE)){
            if($('#selectedDatasource').val().includes(dataSources.PVA)){
                $('#reportSection').removeClass('hide').addClass('show');
                $('#templateQueryList').removeClass('hide').addClass('show');
                $('.report-section-buttons').removeClass('hide').addClass('show');
            }else if($('#selectedDatasource').val().includes(dataSources.VAERS) || $('#selectedDatasource').val().includes(dataSources.FAERS)){
                $('#reportSection').removeClass('hide').addClass('show');
                $('#templateQueryList').removeClass('show').addClass('hide');
                $('.report-section-buttons').removeClass('show').addClass('hide');
            }else{
                $('#reportSection').removeClass('show').addClass('hide');
                $('#templateQueryList').removeClass('show').addClass('hide');
                $('.report-section-buttons').removeClass('show').addClass('hide');
            }
        }
        initializing = false

        if (isFirstTimeCommonChange) {
            if(( typeof editAlert != "undefined" && editAlert === "create")){
                isFirstTimeCommonChange = false;
            }
        }
        else {
            $('#assignedTo').empty();
            bindShareWith($('#assignedTo'), sharedWithListUrl, sharedWithValuesUrl, "", false);
            $('#sharedWith').empty();
            bindShareWith2WithData($('#sharedWith'), sharedWithUrl, sharedWithData, true);
            if ($('#selectedDatasource').val()) {
                if ($('#selectedDatasource').val().includes(dataSources.PVA)) {
                    $('#excludeNonValidCases').prop("disabled", false);
                    $('#missedCases').prop("disabled", false);
                    $('#alertQuery').prop("disabled", false);
                    if($('#dateRangeType').val()===undefined){
                        $('#dateRangeType').val('CASE_RECEIPT_DATE').trigger('change');
                    }
                    $('#dateRangeType').prop("disabled", false);
                    if($('#evaluateDateAsNonSubmission').val()===undefined){
                        $('#evaluateDateAsNonSubmission').val('LATEST_VERSION').trigger('change');
                    }
                    $('#evaluateDateAsNonSubmission').prop("disabled", false);
                }
                if (!($('#selectedDatasource').val().includes(dataSources.EUDRA))) {
                    $('#groupBySmq').prop("disabled", false);
                }
            }
        }

        if((!$("#adhocRun").is(":checked") &&  typeof editAlert != "undefined" && editAlert === "edit")){
            enableDisablePriorityPopulate(false);
        }
    }

    $('#selectedDatasource').on("change", function () {
        reportSecAttribStatusChange();
    });



    if ($('#selectedDatasource').val()) {
        if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.FAERS) {
            $('#reportSection').removeClass('hide');
            $('#reportSection').addClass('show');
            $('.drugClassificationContainer').show();
        }
        if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.VAERS) {
            $('#dateRangeType').val('CASE_RECEIPT_DATE').trigger('change');
            $('#dateRangeType').prop("disabled", true);
            $('#evaluateDateAsNonSubmission').val('LATEST_VERSION').trigger('change');
            $('#evaluateDateAsNonSubmission').prop("disabled", true);
        }
    }
    if($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.VAERS) {
        $('#dateRangeType').val('CASE_RECEIPT_DATE').trigger('change');
        $('#dateRangeType').prop("disabled", true);
        $('#evaluateDateAsNonSubmission').val('LATEST_VERSION').trigger('change');
        $('#evaluateDateAsNonSubmission').prop("disabled", true);
    }

    var enableDisablePriorityPopulate = function (value) {
        $('#priority').prop('disabled', value);
        if (typeof editAlert !== "undefined" && (editAlert === "copy" || editAlert === "edit")) {
            return;  //this is done to prevent assignment of default priority in case of edit and copy
        }
        if (value == false && typeof byDefaultPriority != 'undefined' && byDefaultPriority != 'null') {
            $("#priority").val(byDefaultPriority).trigger('change');
        } else {
            $("#priority").val('null').trigger('change');
        }
    };

    if (typeof editAlert !== "undefined" && (editAlert === "copy" || editAlert === "edit")) {
        if (Array.isArray($('#selectedDatasource').val())) {
            attribStatusChangeOnGenerateAnalysisSelected();
        }
    }

    if (typeof editAlert !== "undefined" && editAlert === "copy") {
        if (typeof $('#selectedDatasource').val() === 'string') {
            $("#adhocRun").prop("disabled", false);
        }
    }


    function attribStatusChangeOnGenerateAnalysisSelected() {
        if ($('#selectedDatasource').val()) {
            if (isDataSourceEnabled && $('#selectedDatasource').val().length > 1) {
                $("#adhocRun").prop("checked", false).prop("disabled", true);
                if (!$('#selectedDatasource').val()?.includes(dataSources.PVA)) {
                    $('#includeLockedVersion').prop("checked", false).prop("disabled", true);
                }
                if ($('#selectedDatasource').val()?.includes(dataSources.PVA)) {
                    $('#includeLockedVersion').prop("disabled", false);
                }

                if ($('#selectedDatasource').val().length == 4) {
                    if ($('#selectedDatasource').val()?.includes(dataSources.PVA) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.FAERS) && $('#selectedDatasource').val()?.includes(dataSources.VIGIBASE))
                    {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#includeLockedVersion').prop("disabled", false);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.PVA) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.VAERS) && $('#selectedDatasource').val()?.includes(dataSources.VIGIBASE))
                    {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#includeLockedVersion').prop("disabled", false);
                        return;
                    }
                }
                if ($('#selectedDatasource').val().length == 3) {
                    if ($('#selectedDatasource').val()?.includes(dataSources.PVA) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.FAERS))
                    {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.PVA) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.VAERS))
                    {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("disabled", false);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.PVA) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.VIGIBASE))
                    {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("disabled", false);
                        $('#includeLockedVersion').prop("disabled", false);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.FAERS) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.VIGIBASE))
                    {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        $('#excludeNonValidCases').prop("disabled", true);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.VAERS) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.VIGIBASE))
                    {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        $('#excludeNonValidCases').prop("disabled", true);
                        $('#excludeFollowUp').prop("disabled", true);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.VAERS) && $('#selectedDatasource').val()?.includes(dataSources.PVA) && $('#selectedDatasource').val()?.includes(dataSources.VIGIBASE))
                    {
                        $('#includeLockedVersion').prop("disabled", false);
                        return;
                    }
                }
                if ($('#selectedDatasource').val().length == 2) {
                    if ($('#selectedDatasource').val()?.includes(dataSources.EUDRA) && $('#selectedDatasource').val()?.includes(dataSources.FAERS)) {
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.VIGIBASE) && $('#selectedDatasource').val()?.includes(dataSources.FAERS)) {
                        $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.VIGIBASE) && $('#selectedDatasource').val()?.includes(dataSources.VAERS)) {
                        $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        $('#excludeFollowUp').prop("disabled", true);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.VIGIBASE) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA)) {
                        $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        return;
                    }
                    if ($('#selectedDatasource').val()?.includes(dataSources.VAERS) && $('#selectedDatasource').val()?.includes(dataSources.EUDRA)) {
                        $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
                        $('#missedCases').prop("checked", false).prop("disabled", true);
                        $('#groupBySmq').prop("checked", false).prop("disabled", true);
                        $('#excludeFollowUp').prop("disabled", true);
                        return;
                    }

                }

            }
        }

    }

}

function onSubmitBusinessConfigForm() {
    $('#saveBusinessConfigButton').on('click', function () {
       $(this).attr('disabled', true);
       $('#businessConfigurationForm').submit();
    });
}
