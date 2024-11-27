// TODO: POMI: This agency stuff doesn't do anything right now.
var productSelectionDataSheet = $('#showProductSelection').find('div').text();
$(function () {
    $spotfireType = $("#spotfireType");
    var fileType = $spotfireType.val();
    var drugType = $("#drugType");
    $("#drugType").on('change',function () {
        if ($("#selectedDatasource").val() && $("#selectedDatasource").val().includes("pva")&& !$("#selectedDatasource").val().includes("faers")){
            var drugOption = new Option("Drug", "DRUG", false, false);
            var vaccineOption = new Option("Vaccine", "VACCINCE", false, false);
            if (drugType.val() === "VACCINE") {
                $spotfireType.val(null).trigger("change");
                $spotfireType.empty();
                $spotfireType.append(vaccineOption).trigger('change.select2');
                $spotfireType.append(drugOption).trigger('change.select2');
                $spotfireType.find(vaccineOption).attr('selected', 'true').trigger('change');
                $spotfireType.find(drugOption).attr('disabled', 'disabled').trigger('change');
            } else {
                $spotfireType.val(null).trigger("change");
                $spotfireType.empty();
                if(fileType == 'VACCINCE'){
                    var vacc= new Option("Vaccine", "VACCINCE", true, true)
                    $spotfireType.append(vacc).trigger('change.select2');
                    $spotfireType.append(drugOption).trigger('change.select2');
                    $spotfireType.find(vacc).removeAttr('disabled');
                } else{
                    var drug= new Option("Drug", "DRUG", true, true)
                    $spotfireType.append(drug).trigger('change.select2');
                    $spotfireType.append(vaccineOption).trigger('change.select2');
                    $spotfireType.find(drug).removeAttr('disabled');
                }
            }
        }
    });
    drugType.trigger('change');
    $('#advancedOptions').hide();
    $('#agencyPartnerSelection').hide();
    $('#draft').parent().hide();

    $("#agencyPartnerTags").select2({
        placeholder: "Select Agency / Partner",
        allowClear: true,
        width: "100%",
        tags: ["Alvogen", "EMA", "FDA", "PMDA"]
    });
    $('#isPeriodic').on('click', function () {
        var $this = $(this);
        if ($this.is(':checked')) {
            $('#agencyPartnerSelection').show();
            $('#draft').parent().show();
        } else {
            $('#agencyPartnerSelection').hide();
            $('#draft').parent().hide();
        }
    });

    $(".repeat").on('click', function () {
        var repeatValue = $('.repeat-options').find('.selected-label').html();
        if (repeatValue == "None (run once)") {
            $("#repeatExecution").val(false);
        } else {
            $("#repeatExecution").val(true);
        }
    });

    $('#configurationForm input[type=submit]').on('click', function (evt) {
        evt.preventDefault();
        if(isDataSourceEnabled) {
            if (typeof prevDataSourceList !== 'undefined') {
                let dataSources = $('#selectedDatasource').val();
                if(!Array.isArray(dataSources)) {
                    dataSources = dataSources.split(",");
                }
                if ($.arrayIntersect(prevDataSourceList, dataSources).length == 0) {
                    $.Notification.notify('warning', 'top right', "Warning", "At least one data source selected in the previous version of the alert should be retained", {autoHideDelay: 5000});
                } else {
                    var action = $(this).data('action');
                    $('#configurationForm').attr('action', action);
                    $('#configurationForm').submit();
                }
            } else {
                var action = $(this).data('action');
                $('#configurationForm').attr('action', action);
                $('#configurationForm').submit();
            }
        } else {
            var action = $(this).data('action');
            $('#configurationForm').attr('action', action);
            $('#configurationForm').submit();
        }
    });


    var limitToCaseSeries = $("#limitToCaseSeries");
    if(limitToCaseSeries.length) {
        limitToCaseSeries.select2({
            minimumInputLength: 0,
            multiple: false,
            placeholder: $.i18n._('selectOne'),
            allowClear: true,
            width: "100%",
            ajax: {
                quietMillis: 250,
                dataType: "json",
                url: fetchCaseSeriesUrl,
                data: function (params) {
                    return {
                        term: params.term,       // search term
                        page: params.page || 1,  //page number
                        max: 30
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 1;
                    return {
                        results: data.list,
                        pagination: {
                            more: (params.page * 30) < data.totalCount
                        }
                    };
                }
            },
            escapeMarkup: function (m) {
                return m;
            }
        });

        if (caseSeriesData != undefined && caseSeriesData.id) {
            var option = new Option(caseSeriesData.text, caseSeriesData.id, true, true);
            limitToCaseSeries.append(option).trigger('change');
            whenCaseSeriesSelected(true);
        }

        limitToCaseSeries.on("change", function () {
            var selectedCaseSeries = limitToCaseSeries.val();
            if (selectedCaseSeries) {
                whenCaseSeriesSelected();
            } else {
                $('#excludeFollowUp').prop("disabled", false);
                $('#applyAlertStopList').prop("disabled", false);
                $('#adhocRun').prop("disabled", false);
                $('#excludeNonValidCases').prop("disabled", false);
                $('#includeLockedVersion').prop("disabled", false);
                $('#missedCases').prop("disabled", false);
                $('#evaluateDateAsNonSubmission').prop("disabled", false);
            }
        });
    }
    $("#assignedTo").on('change', function () {
        let selectedVal = $(this).val();
        let elem = $("#sharedWith")
        let selectedElem = elem.val().map((elem) => { return elem.replace('_readOnly', ''); });
        let isIncludes;
        if(selectedElem) {
            isIncludes = selectedElem.includes(selectedVal);
        }
        if(!isIncludes && selectedVal){
            let text = $(this).find("option[value="+selectedVal+"]").text();
            let option = new Option(text,selectedVal,true,true);
            elem.append(option).trigger("change.select2")
        }
    });
    $('#studyDrugCases').on('change', function() {
        if ($(this).is(':checked')) {
            $('#additionalOptions').removeClass('hidden');
        } else {
            $('#additionalOptions').addClass('hidden');
            $('#ignoreMedicationType').prop('checked', false)
            $('#considerPOI').prop('checked', false)
        }
    });

});


function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

$(document).on('change', "#dataMiningVariable", function(event){
    setProductMining()
});

$(document).on('click', '.addAllProducts', function (event) {
    selectAttributeValidation(event);
})

function selectAttributeValidation() {
    var foregroundSearchAttr = $("#foregroundSearchAttr").val();
    var foregroundSearch = $("#foregroundSearch").val();
    var productGroup = $("#productGroupSelect").val();
    $("#searchAttributeError").hide();
    if ($("#alertType").val() == 'Aggregate Case Alert' && JSON.parse(foregroundSearch) && (foregroundSearchAttr == "" || foregroundSearchAttr == null || foregroundSearchAttr == "null")) {
        $("#searchAttributeError").show();
        $("#foregroundSearch").val(false);
        $('#considerForegroundRun').prop('checked', false);
        e.preventDefault();
    }else{
        $("#searchAttributeError").hide();
    }
}


$(document).on('change', "#adhocRun", function(event){
    if($(this).is(':checked') == false || $("#dataMiningVariable").val() == "null"){
        $('#isProductMining').prop('checked', false).prop("disabled", true);;
    }
    if (!$(this).is(':checked') && typeof byDefaultPriority != 'undefined' && byDefaultPriority != null && byDefaultPriority != '') {
        if (!/^\d+$/.test($("#priority").val())) {
            //this is used to prevent default priority assignment in case of edit
            $("#priority").val(byDefaultPriority).trigger('change');
        }
    } else {
        $("#priority").val('null').trigger('change');
    }
});


function setProductMining(){
    sleep(1200).then(() => {
        setProductMiningAfterDelay();
    });
}
function setProductMiningAfterDelay() {
    if($("#dataMiningVariable").val() == "null" && ($("#productSelection").val() || ($("#productGroupSelection").val() && $("#productGroupSelection").val() !== "[]"))){
        $('#isProductMining').prop("checked", false).prop("disabled", true);
    } else if($("#dataMiningVariable").val() !== "null" && (!$("#productSelection").val() && (!$("#productGroupSelection").val() || $("#productGroupSelection").val() == "[]"))){
        $('#isProductMining').prop("checked", false).prop("disabled", true);
    } else if($("#dataMiningVariable").val() !== "null" && ($("#productSelection").val() || ($("#productGroupSelection").val() && $("#productGroupSelection").val() !== "[]"))){
        $('#isProductMining').prop("disabled", false);
    }
}
$(document).on('click', ".addAllProducts", function(event){
    sleep(1000).then(() => {
        var newProductSelecionDataSheet = $('#showProductSelection').find('div').text();
        if(productSelectionDataSheet !== newProductSelecionDataSheet){
            $("#dataSheet").html("")
            productSelectionDataSheet  = newProductSelecionDataSheet
        }
        setProductMining()
    })

});


$.arrayIntersect = function(a, b)
{
    return $.grep(a, function(i)
    {
        return $.inArray(i, b) > -1;
    });
};

function whenCaseSeriesSelected(isEdit) {
    if(typeof isEdit === "undefined") {
        $('#excludeNonValidCases').prop("checked", true).prop("disabled", false);
    } else {
        $('#excludeNonValidCases').prop("disabled", false);
    }
    $('#excludeFollowUp').prop("checked", false).prop("disabled", true);
    $('#applyAlertStopList').prop("checked", false).prop("disabled", true);
    $('#adhocRun').prop("disabled", false);
    $('#missedCases').prop("checked", false).prop("disabled", true);
    $('#evaluateDateAsNonSubmission').prop("disabled", true).val('LATEST_VERSION').trigger('change');
    $('#includeLockedVersion').prop("checked", true).prop("disabled", true);
}

function advancedOptions(e) {
    if (e == 1) {
        $('#showOptions').hide();
        $('#advancedOptions').slideDown();
    } else {
        $('#advancedOptions').hide();
        $('#showOptions').show();
    }
}

function hideShowContent(e) {
    var getContent = $(e).parent().parent().find('.rxmain-container-content');
    var display = true;
    if ($(getContent).hasClass('rxmain-container-hide')) {
        display = false;
    }

    var getIcon;
    if (display) {
        getIcon = $(e).parent().find('i');
        $(getIcon).removeClass('fa-caret-down').addClass('fa-caret-right');
        $(getContent).removeClass('rxmain-container-show').addClass('rxmain-container-hide');
        $("#addDocumentIcon").hide();
    } else {
        getIcon = $(e).parent().find('i');
        $(getIcon).removeClass('fa-caret-right').addClass('fa-caret-down');
        $(getContent).removeClass('rxmain-container-hide').addClass('rxmain-container-show');
        $("#addDocumentIcon").show();
    }
}

//The method called when form is submitted.
function onFormSubmit() {
    $("#saveRun").prop('disabled', true);
    $("#saveBtn").prop('disabled', true);
    //When isAdhocAlert is coming as true
    if(typeof isAdhocAlert !== 'undefined' && isAdhocAlert) {
        var optRadio = $("input:radio[name=optradio]:checked");
        if (!optRadio.hasClass("studyRadio")) {
            if (optRadio.hasClass("productRadio")) {
                if ($("#productSelection").val() == '') {
                    $("#productSelection").val('');
                } else {
                    var json = JSON.parse($("#productSelection").val());
                    json[5] = [];
                    $("#productSelection").val(JSON.stringify(json));
                }
            } else {
                productValues[1] = [];
                productValues[2] = [];
                productValues[3] = [];
                productValues[4] = [];
                $("#productSelection").val(JSON.stringify(productValues));
            }
        } else {
            $("#productSelection").val('');
        }
    }
    //If adhoc run value is check then scheduling is nullified and repeat execution value is set as false too.
    //Apart of that the priority is enabled/disabled too.
    if ($("#adhocRun").is(":checked")) {
        $('#scheduleDateJSON').val(JSON.stringify(null));
        //$("#priority").prop('disabled', false);
       // $("#priority").val($("#priority option:eq(1)").val());
        $("#repeatExecution").val(false);

    } else if(typeof isAdhocAlert == 'undefined') {//Need to set the scheduler values as passed value.
        var obj = $('#pvsAlertScheduler').scheduler('value');
        $('#scheduleDateJSON').val(JSON.stringify(obj));
        $("#toDate").attr("disabled", false);
    }
    try {
        setMultiselectValues();
    }
    catch (e) {
        //catch and just suppress error
        console.log(e);
    }
    $("select").attr('disabled',false);
    $("#adhocRun").prop("disabled", false);
    $("#includeLockedVersion").prop("disabled" , false);
    if ($("#adhocRun").is(":checked")) {
        return true;
    } else {
        try {
            return checkNumberFields();
        } catch (e) {
            return true;
        }
    }
}

function hideLockedCheckbox(){
    if($("#isIncludeLockedVersions").val() == "false") {
        $("#includeLockedVersion").prop("checked" , true);
    }
}

function disableLockedCheckbox(){
    var dataSource = $("#selectedDatasource").val();
    if(($("input[name=editable]") != undefined && $("input[name=editable]").val() == "true" )  || (dataSource == null) || (dataSource.indexOf("pva") == -1)) {
        $("#includeLockedVersion").prop("disabled" , true);
    } else {
        $("#includeLockedVersion").prop("disabled" , false);
    }
}

function changeCheckbox(isFirstChange) {
    var dataSource = $("#selectedDatasource").val();
    if((dataSource == null) || (dataSource.indexOf("pva") == -1) && !isFirstChange) {
        $("#includeLockedVersion").prop("checked" , false);
    } else if(!isFirstChange){
        $("#includeLockedVersion").prop("checked" , true);
    }
}
$(document).on('change', '#adhocRun', function () {
    if (!$("#adhocRun").is(':checked') && ($('#selectedDatasource').val() !== null && $('#selectedDatasource').val().includes("jader"))) {
        {
            $('#isTemplateAlert').prop('disabled', true)
        }
    }
});
$(document).on('change', '#selectedDatasource', function () {
    if ($("#adhocRun").is(':checked') || ($('#selectedDatasource').val() !== null && $('#selectedDatasource').val().includes("jader"))) {
        $('#isTemplateAlert').prop('disabled', true)
    } else {
        $('#isTemplateAlert').prop('disabled', false)
    }
 });

function changeLiteratureDataSource(isEditMode = false) {
    initializeDataSourceDisplay()

    $('input[name="selectedDatasource"]').on('change', function() {
        $('#literatureQuery').val('').trigger('change');
        var selectedValue = $('input[name="selectedDatasource"]:checked').val();
        console.log("Selected Data Source: " + selectedValue);
        toggleDataSourceDisplay(selectedValue)
        if (!isEditMode) {
            clearAlertDetailsAndProductSelection();
        }
    });

    function toggleDataSourceDisplay(selectedValue) {
        if (selectedValue === 'embase') {
            $('#isPubMedDataSource').addClass('hidden');
            $('.literatureQueryDropdown').removeClass('hidden');
        } else {
            $('#isPubMedDataSource').removeClass('hidden');
            $('.literatureQueryDropdown').addClass('hidden');
        }
    }

    // setting default values
    function clearAlertDetailsAndProductSelection() {
        $('#pvsAlertDetails input[type="text"]').val('');
        $('#pvsAlertDetails select[name="priority"]').val('null').trigger('change');
        $('#pvsAlertDetails select[name="assignedToValue"]').val('null').trigger('change');
        $('#sharedWith').val('User_' + userId).trigger('change');

        clear_search_results()
    }

    function initializeDataSourceDisplay() {
        var selectedValue = $('input[name="selectedDatasource"]:checked').val();
        toggleDataSourceDisplay(selectedValue);
    }
}

function fetchLiteratureQueryDetails() {
    $('#literatureQuery').on('change', function() {
        var queryId = this ? $(this).val() : null;
        $('#literatureQuery').val() ? $('#literatureQueryName').val($(this).select2('data')[0].name) : $('#literatureQueryName').val("");
        $('#literatureQueryId').val(queryId);
        $('.literatureSearchString').val('');
        if (queryId) {
            $.ajax({
                url: "/signal/literatureAlert/prepareSearchStringFromQuery?queryId=" + queryId,
            }).done(function (result) {
                if (result.status === true) {
                    $('.literatureSearchString').val(result.data);
                }
            }).fail(function () {
                $.Notification.notify('error', 'top right', "Error", "Error occurred while preparing search string.", {autoHideDelay: 5000});
            })
        }
    });
}

function copyToClipboardSearchString() {
    $("#copyButton").click(function() {
        // Select the textarea element
        var textToCopy = $(".literatureSearchString").val();

        // Use the Clipboard API to copy the text
        navigator.clipboard.writeText(textToCopy).then(function() {
            // Display a success message
            $.Notification.notify('success', 'top right', "Copied", "Text copied to clipboard.", {autoHideDelay: 5000});
        }).catch(function(err) {
            // Display an error message if copy fails
            $.Notification.notify('error', 'top right', "Error", "Failed to copy text to clipboard.", {autoHideDelay: 5000});
        });
    });
}

