var sDate, eDate, vaersResponse = true, vigibaseResponse = true;
$(function () {
    $(".bgQueryIcon").hide();
    $(".fgQueryIcon").hide();
    $(document).on('change',"#productGroupSelection , #selectedDatasource",function () {
        $(document).find("select[name='alertDateRangeInformation.dateRangeEnum']").trigger('change');
    });

    $( "input[name='alertDateRangeInformation.relativeDateRangeValue']" ).on('change', function() {
        alertDateRangeChangedAction(document);
    });

    $('[data-toggle="tooltip"]').on('click', function () {
        $(this).tooltip('hide')
    });
    // Added for PVS-64882
    $("#dateRangeStart").on('focusout', function(){
        if($(this).val() !== "" && $('#dateRangeEnd').val() !== ""){
            alertDateRangeChangedAction(document);
        }
    });
    $("#dateRangeEnd").on('focusout', function(){
        if($(this).val() !== "" && $('#dateRangeStart').val() !== ""){
            alertDateRangeChangedAction(document);
        }
    });

    var updateVaersVigibaseData = function () {
        if (typeof vaersResponse != 'undefined' && vaersResponse) {
            if ($('#dateRangeStart').val() && $('#dateRangeEnd').val() && ((!sDate && !eDate) || (sDate != $('#dateRangeStart').val() || eDate != $('#dateRangeEnd').val()))) {
                if ($('#selectedDatasource').val()) {
                    var isPVA = $('#selectedDatasource').val().includes("pva");
                    var isEUDRA = $('#selectedDatasource').val().includes("eudra");
                    var isVAERS = $('#selectedDatasource').val().includes("vaers");
                }
                if (!(!isPVA && isVAERS && isEUDRA)) {
                    alertDateRangeChangedAction(document);
                }
            }
        }

        if (typeof vigibaseResponse != 'undefined' && vigibaseResponse) {
            if ($('#dateRangeStart').val() && $('#dateRangeEnd').val() && ((!sDate && !eDate) || (sDate != $('#dateRangeStart').val() || eDate != $('#dateRangeEnd').val()))) {
                if ($('#selectedDatasource').val()) {
                    var isPVA = $('#selectedDatasource').val().includes("pva");
                    var isEUDRA = $('#selectedDatasource').val().includes("eudra");
                    var isVAERS = $('#selectedDatasource').val().includes("vaers");
                    var isVIGIBASE = $('#selectedDatasource').val().includes("vigibase");
                }
                if (!(!isPVA && !isVAERS && isVIGIBASE && isEUDRA)) {
                    alertDateRangeChangedAction(document);
                }
            }
        }
    }
    updateVaersVigibaseData();

    var updateFgData = function () {
        sleep(2000).then(function () {
            var fgSelect2Data = [];
            var foregroundChilds = $(".queryExpressionValues1 .queryBlankContainer .expressionValueSelect1");
            if(foregroundChilds.find(".select2-selection__choice").length==0){
                foregroundChilds=$(".queryExpressionValues1 .queryBlankContainer .expressionValueSelect2");
            }
            foregroundChilds.each(function (index) {
                var child = $(this).find(".select2-selection__choice");
                var tempData = [];
                child.each(function () {
                        var data = $(this).attr("title");
                        tempData.push(data);
                    }
                )
                    fgSelect2Data.push(index+","+tempData);
            });
            $("#fgData").val(JSON.stringify(fgSelect2Data));
            $(".queryExpressionValues1").find(".select2-selection__choice").each(function () {
                if ($(this).attr("title") == "") {
                    $(this).remove();
                }
            })
        });
    }
    var init = function () {

        $("#foregroundQuery").on('change', function () {
            if ($(this).is(":checked")) {
                showHideForegroundQuery(true);
            } else {
                showHideForegroundQuery(false);
            }
        })
        var includesPva = $('#selectedDatasource').val().includes(dataSources.PVA);
        $("#selectedDatasource").on('change', function () {
            includesPva = $('#selectedDatasource').val();
            if (includesPva == null || includesPva == "null") {
                includesPva = false;
            } else {
                includesPva = $('#selectedDatasource').val().includes(dataSources.PVA);
            }
            if (JSON.parse(includesPva)) {
                showHideForegroundQueryCheckbox(true);
            } else {
                showHideForegroundQueryCheckbox(false);
                showHideForegroundQuery(false);
            }
        })
        $(document).find("select[name='alertDateRangeInformation.dateRangeEnum']").on("change", function (e) {
            alertDateRangeChangedAction(document);
        }).select2({allowClear: false}).trigger('change');

        $queryName = $("#alertQueryName");
        $alertQuery = $("#alertQuery");
        $foreGroundQueryName = $("#alertForegroundQueryName");
        $foreGroundAlertQuery = $("#alertForegroundQuery");

        var data = ($queryName.val() && $("#alertQueryId").val()) ? {
            id: $("#alertQueryId").val(),
            text: $queryName.val()
        } : {};
        var foreGroundData = ($foreGroundQueryName.val() && $("#alertForegroundQueryId").val()) ? {
            id: $("#alertForegroundQueryId").val(),
            text: $foreGroundQueryName.val()
        } : {};

        bindSelect2WithUrl($alertQuery, queryList, data);
        bindSelect2WithUrl($foreGroundAlertQuery , queryList, foreGroundData);

        $alertQuery.on("change", function (e) {
            $alertQuery.val() ? $queryName.val($(this).select2('data')[0].name) : $queryName.val("");
            selectGlobalQueryOnChange(this);
            $("#select2-selectSelect-results").remove();
            $("#select2-selectSelectNonCache-results").remove();
            $("#select2-selectValue-results").remove();
            $("#select2-selectSelectAuto-results").remove();
            $(".select2-container--open").css("top", "0px");
            $(".alertQueryWrapper").find(".expressionField,.expressionOp,.expressionValueSelect").select2();
        });
        $foreGroundAlertQuery.on("change", function (e) {
            $foreGroundAlertQuery.val() ? $foreGroundQueryName.val($(this).select2('data')[0].name) : $foreGroundQueryName.val("");
            selectForgroundGlobalQueryOnChange(this.value);
            $("#select2-selectSelect-results").remove();
            $("#select2-selectSelectNonCache-results").remove();
            $("#select2-selectValue-results").remove();
            $("#select2-selectSelectAuto-results").remove();
            $(".select2-container--open").css("top", "0px");
            $(".alertQueryWrapper1").find(".expressionField,.expressionOp,.expressionValueSelect").select2();
        });
        $(".open-foreground-query").on('click', function () {
            $("#myModal3").modal("show");
        })
        $(".open-background-query").on('click', function () {
            $("#myModal2").modal("show");
        })
        if ($foreGroundAlertQuery.val() != null) {
            showHideForegroundQuery(true);
            $("#foregroundQuery").prop("checked", true);
            updateFgData();

            $(".queryExpressionValues1 .queryBlankContainer .expressionValueSelect1").on('select2:select', function (e) {
                updateFgData();
            });
            $(".queryExpressionValues1 .queryBlankContainer .expressionValueSelect1").on('select2:unselect', function (e) {
                updateFgData();
            });
        }

        $(".queryExpressionValues").find(".select2-selection__choice").each(function () {
            if ($(this).attr("title") == "") {
                $(this).remove();
            }
        })
        $(".queryExpressionValues1").find(".select2-selection__choice").each(function () {
            if ($(this).attr("title") == "") {
                $(this).remove();
            }
        })

        $(".queryExpressionValues1").find(".select2-selection__choice").attr("title", "").remove();
        $(".queryExpressionValues").find(".select2-selection__choice").attr("title", "").remove();

    };
    init();


    var modalOpener = function () {
        $(document).on('click', '.copy-paste-pencil', function (evt) {
            $(this).siblings(".copyAndPasteModal").find(".copyPasteContent").val('');
            $(this).siblings(".copyAndPasteModal").find(".c_n_p_other_delimiter").val('');
            var radiobtn = $('.copyAndPasteModal').find('input:radio[value="none"]')[0];
            radiobtn.checked = true;
            $('.validate-copy-paste').removeAttr('disabled');
            $(this).siblings(".copyAndPasteModal").modal('show');
            fileChange($(this).siblings(".copyAndPasteModal"));
            fileSelect($(this).siblings(".copyAndPasteModal").find("#file_input"));
        })
    }

    var includesPva = $('#selectedDatasource').val().includes(dataSources.PVA);
    if (includesPva == null || includesPva == "null") {
        includesPva = false;
    } else {
        includesPva = $('#selectedDatasource').val().includes(dataSources.PVA);
    }
    if (JSON.parse(includesPva)) {
        showHideForegroundQueryCheckbox(true);
    } else {
        showHideForegroundQueryCheckbox(false);
    }
    modalOpener();
    if (typeof $alertQuery.val() !== "undefined") {
        var data = $("#alertQuery").val();
        if (data != null && data != "null" && data != "") {
            $(".bgQueryIcon").show();
            $(".viewBgQuery").attr("href", window.location.origin + "/signal/query/view/" + data);
            $(".viewBgQuery").show();
        } else {
            $(".viewBgQuery").hide();
            $(".bgQueryIcon").hide();
        }
    }
    if (typeof $foreGroundAlertQuery.val() !== "undefined") {
        var data = $("#alertForegroundQuery").val();
        if (data != null && data != "null" && data != "") {
            $(".fgQueryIcon").show();
            $(".viewFgQuery").attr("href", window.location.origin + "/signal/query/view/" + data);
            $(".viewFgQuery").show();
        } else {
            $(".viewFgQuery").hide();
            $(".fgQueryIcon").hide();
        }
    }
    $(".alertQueryWrapper").find(".expressionField,.expressionOp,.expressionValueSelect").select2();
    $(".alertQueryWrapper1").find(".expressionField,.expressionOp,.expressionValueSelect").select2();
    modalOpener();

});

function showHideForegroundQuery(show) {
    if (JSON.parse(show)) {
        $(".forgroundQuery").show();
    } else {
        $(".forgroundQuery").hide();
    }
}

function showHideForegroundQueryCheckbox(show) {
    if (JSON.parse(show) && !$('#isMasterTemplateAlert').is(':checked')) {
        $("#foregroundQuery").prop("disabled", false);
        $("#foregroundQuery").css("pointer-events", "");
        $("#foregroundQuery").css("cursor", "");
    } else {
        $("#foregroundQuery").prop("checked", false);
        $("#foregroundQuery").prop("disabled", true);
        $("#foregroundQuery").css({
            "pointer-events": "none",
            "cursor": "not-allowed"
        });
    }
}

function alertDateRangeChangedAction(currentDocument) {
    var valueChanged = currentDocument.getElementById("alertDateRangeInformation.dateRangeEnum").value;
    if (!valueChanged) {
        valueChanged = $('#dateRangeEnum').val();
    }
    var isPVA = false;
    var isEUDRA = false;
    var isFAERS = false;
    var isVAERS = false;
    var isVIGIBASE = false;
    var isJADER = false;

    if ($('#selectedDatasource').val()) {
        isPVA = $('#selectedDatasource').val().includes("pva");
        isEUDRA = $('#selectedDatasource').val().includes("eudra");
        isFAERS = $('#selectedDatasource').val().includes("faers");
        isVAERS = $('#selectedDatasource').val().includes("vaers");
        isVIGIBASE = $('#selectedDatasource').val().includes("vigibase");
        isJADER = $('#selectedDatasource').val().includes("jader");

    }
    if(isFAERS && !isEUDRA && !isPVA && !isVAERS && !isVIGIBASE &&($("#productGroupSelection").val()||$("#productSelection").val())){
        getFaersLatestDateQuarter();
    } else {
        $(document).find("#faersLatestQuarter").text("");
    }
    if(isJADER){
        getJaderLatestDateQuarter();
    }else{
        $(document).find("#jaderLatestQuarter").text("");
    }
    if(isVIGIBASE && !isEUDRA && !isPVA && !isVAERS && !isFAERS &&($("#productGroupSelection").val()||$("#productSelection").val())){
        getVigibaseLatestDateQuarter();
    } else {
        $(document).find("#vigibaseLatestQuarter").text("");
    }
    if (valueChanged !== DATE_RANGE_ENUM.CUMULATIVE) {
        if ($('#selectedDatasource').val() && $("#selectedDatasource").val() !== "[]" && $("#productGroupSelection").val()) {
            if (!isVIGIBASE) {
                $(document).find("#setVigibaseDateRange").text("");
                if ((isPVA && isVAERS)) {
                    getVaersDateRange(valueChanged);
                } else if ((isPVA && isVAERS && isEUDRA)) {
                    getVaersDateRange(valueChanged);
                    getEvdasAndFaersDateRange();
                } else if ((!isPVA && isVAERS && isEUDRA)) {
                    getEvdasAndFaersDateRange();
                    $(document).find("#setVaersDateRange").text("");
                    $(document).find("#setFaersDateRange").text("");
                } else {
                    $(document).find("#setVaersDateRange").text("");
                }
                if (isPVA && isEUDRA && isFAERS) {
                    getEvdasAndFaersDateRange();
                } else {
                    if (isPVA && isEUDRA && !isFAERS) {
                        getEvdasAndFaersDateRange();
                        $(document).find("#setFaersDateRange").text("");
                    } else {
                        if (isPVA && isFAERS && !isEUDRA) {
                            getEvdasAndFaersDateRange();
                            $(document).find("#setEvdasDateRange").text("");
                        } else {
                            if (isEUDRA && isFAERS && !isPVA) {
                                getEvdasAndFaersDateRange();
                                $(document).find("#setFaersDateRange").text("");
                            } else {
                                $(document).find("#setEvdasDateRange").text("");
                                $(document).find("#setFaersDateRange").text("");
                            }
                        }
                    }
                }
            } else {
                var selectedDataSources = $('#selectedDatasource').val();
                if (selectedDataSources.length == 1) {
                    $(document).find("#setEvdasDateRange").text("");
                    $(document).find("#setFaersDateRange").text("");
                    $(document).find("#setVaersDateRange").text("");
                    $(document).find("#setVigibaseDateRange").text("");
                } else {
                    getVigibaseEvdasAndFaersDateRange(valueChanged);
                }
            }
        }
    } else {
        $(document).find("#setEvdasDateRange").text("");
        $(document).find("#setFaersDateRange").text("");
        $(document).find("#setVaersDateRange").text("");
        $(document).find("#setVigibaseDateRange").text("");
    }
    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        initializeAlertDatePickersForEdit(currentDocument);
        $(currentDocument.getElementById('datePickerFromDiv')).show();
        $(currentDocument.getElementById('datePickerToDiv')).show();
        hideErrorOfXEnums(currentDocument)

    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        $(currentDocument.getElementById('datePickerFromDiv')).hide();
        $(currentDocument.getElementById('datePickerToDiv')).hide();
        hideErrorOfXEnums(currentDocument)

    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            $(currentDocument.getElementById('alertDateRangeInformation.relativeDateRangeValue')).show();

        } else {
            hideErrorOfXEnums(currentDocument)
        }
        $(currentDocument.getElementById('datePickerFromDiv')).hide();
        $(currentDocument.getElementById('datePickerToDiv')).hide();
    }
}

function getEvdasAndFaersDateRange(){
    var productGroupSelection=JSON.parse($("#productGroupSelection").val());
    var length=productGroupSelection.length;
    var productGroupId=""
    for(var i=0;i<length;i++){
        if(i!=length-1) {
            productGroupId += productGroupSelection[i].id + ","
        } else {
            productGroupId += productGroupSelection[i].id
        }
    }
    var data = {};
    data['productGroupId']=productGroupId;
    var selectedDatasource = $('#selectedDatasource').val().toString();
    data['selectedDatasource']= selectedDatasource;
    $.ajax({
        type: "POST",
        url: '/signal/aggregateCaseAlert/getEvdasAndFaersDateRange',
        data: data,
    })
        .done(function (data) {
            if(selectedDatasource.includes("eudra") && productGroupId===''){
                return false;
            }
            if (checkForDbDown(data)) {
                $(document).find("#setEvdasDateRange").text("EVDAS: Unable to load data due to an error");
                $(document).find("#setFaersDateRange").text("FAERS: Unable to load data due to an error");
                return false;
            }
            if(data.status==="failed"){
                $.Notification.notify('error', 'top right', "error", "Please upload evdas file first.", {autoHideDelay: 5000});
            } else {
                if(selectedDatasource.includes('eudra')) {
                    // Changed evdas date range with the latest period for the substance
                    if (typeof data.evdasStartDate === "undefined" || typeof data.evdasEndDate === "undefined") {
                        $(document).find("#setEvdasDateRange").text("EVDAS: Unable to load data due to an error");
                    }else if ((typeof data.evdasStartDate !== "undefined" && typeof data.evdasEndDate !== "undefined") && data.evdasStartDate != null  && data.evdasEndDate != null) {
                        $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + data.evdasStartDate + " to " + data.evdasEndDate);
                    }else if(typeof  exeEvdasDateRange !== "undefined" && exeEvdasDateRange !==""){
                        $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + setCharAt(exeEvdasDateRange,12,'to'));
                    }
                }
                if(selectedDatasource.includes('pva') && selectedDatasource.includes('faers')) {
                    $(document).find("#setFaersDateRange").show().addClass("m-t-7");
                    $(document).find("#setVaersDateRange").hide();
                    $(document).find("#setVigibaseDateRange").hide();
                    if (typeof data.faersDate === "undefined") {
                        $(document).find("#setFaersDateRange").text("FAERS: Unable to load data due to an error");
                    }else if(typeof  exeFaersDateRange !== "undefined" && exeFaersDateRange !==""){
                        $(document).find("#setFaersDateRange").text("Date Range (FAERS): " + setCharAt(exeFaersDateRange,12,'to'));
                    } else {
                        $(document).find("#setFaersDateRange").text("Date Range (FAERS): " + data.faersDate);
                    }
                }
            }
    });
}

function getFaersLatestDateQuarter() {
    var data = {};
    var selectedDatasource = $('#selectedDatasource').val().toString();
    data['selectedDatasource'] = selectedDatasource;
    $.ajax({
        type: "POST",
        url: '/signal/aggregateCaseAlert/getFaersLatestQuarter',
        data: data,
    })
        .done(function (data) {
            if (checkForDbDown(data)) {
                $(document).find("#faersLatestQuarter").text("FAERS: Unable to load data due to an error");
                return false;
            }
            if (data.status === "failed") {
                $.Notification.notify('error', 'top right', "error", "Data Not Available", {autoHideDelay: 5000});
            } else {
                if (selectedDatasource.includes('faers')) {
                    if (typeof exeFaersDateRange !== "undefined" && exeFaersDateRange !== "") {
                        $(document).find("#faersLatestQuarter").text("FAERS Data Loaded Till: " + setCharAt(exeFaersDateRange, 12, 'to'));
                    } else {
                        $(document).find("#faersLatestQuarter").text("FAERS Data Loaded Till: " + data.faersDate);
                    }
                }
            }
    });
}

function getVigibaseLatestDateQuarter() {
    var data = {};
    var selectedDatasource = $('#selectedDatasource').val().toString();
    data['selectedDatasource']= selectedDatasource;
    $.ajax({
        type: "POST",
        url: '/signal/aggregateCaseAlert/getVigibaseLatestQuarter',
        data: data,
    })
        .done(function (data) {
            if (checkForDbDown(data)) {
                $(document).find("#vigibaseLatestQuarter").text("VigiBase: Unable to load data due to an error");
                return false;
            }
            if(data.status==="failed"){
                $.Notification.notify('error', 'top right', "error", "Data Not Available", {autoHideDelay: 5000});
            } else {
                if(selectedDatasource.includes('vigibase')) {
                    if(typeof  exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !==""){
                        $(document).find("#vigibaseLatestQuarter").text("VigiBase Data Loaded Till: " + setCharAt(exeVigibaseDateRange,12,'to'));
                    } else {
                        $(document).find("#vigibaseLatestQuarter").text("VigiBase Data Loaded Till: " + data.vigibaseDate);
                    }
                }
            }
    });
}
function getJaderLatestDateQuarter() {
    var data = {};
    var selectedDatasource = $('#selectedDatasource').val().toString();
    data['selectedDatasource']= selectedDatasource;
    $.ajax({
        type: "POST",
        url: '/signal/aggregateCaseAlert/getJaderLatestQuarter',
        data: data,
    })
        .done(function (data) {
            if (checkForDbDown(data)) {
                $(document).find("#jaderLatestQuarter").text("JADER: Unable to load data due to an error");
                return false;
            }
            if(data.status==="failed"){
                $.Notification.notify('error', 'top right', "error", "Data Not Available", {autoHideDelay: 5000});
            } else {
                if(selectedDatasource.includes('jader')) {
                        $(document).find("#jaderLatestQuarter").text("JADER Data Loaded Till: " + data.jaderDate);
                }
            }
    });
}

function getVaersDateRange(valueChanged){
    var data = {};
    var selectedDatasource = $('#selectedDatasource').val().toString();
    data['startDate'] = $('#dateRangeStart').val();
    data['endDate'] = $('#dateRangeEnd').val();
    data['valueOfX'] = $('.relativeDateRangeValue').val();
    data['dateRange'] = valueChanged;
    $.ajax({
        type: "POST",
        url: '/signal/aggregateCaseAlert/getVaersDateRange',
        data: data,
        async: false,
    })
        .done(function (data) {
            if (checkForDbDown(data)) {
                $(document).find("#setVaersDateRange").text("VAERS: Unable to load data due to an error");
                return false;
            }

            $response = data;
            vaersResponse = $response.status;
            sDate = $('#dateRangeStart').val();
            eDate = $('#dateRangeEnd').val();
            if(selectedDatasource.includes('vaers') && $response.status) {
                if (!selectedDatasource.includes('vigibase')) {
                    $(document).find("#setFaersDateRange").hide();
                    $(document).find("#setVigibaseDateRange").hide();
                }
                $(document).find("#setVaersDateRange").show().addClass("m-t-7");
                if(typeof $response.data !== "undefined" && $response.data !==""){
                    $(document).find("#setVaersDateRange").text("Date Range (VAERS): " + $response.data);
                } else if(typeof  exeVaersDateRange !== "undefined" && exeVaersDateRange !==""){
                    $(document).find("#setVaersDateRange").text("Date Range (VAERS): " + setCharAt(exeVaersDateRange,12,'to'));
                }
            }
    });
}

//Added method for vigibase date range.
function getVigibaseEvdasAndFaersDateRange(valueChanged) {
    var productGroupSelection=JSON.parse($("#productGroupSelection").val());
    var length=productGroupSelection.length;
    var productGroupId=""
    for(var i=0;i<length;i++){
        if(i!=length-1) {
            productGroupId += productGroupSelection[i].id + ","
        } else {
            productGroupId += productGroupSelection[i].id
        }
    }
    var data = {};
    data['productGroupId']=productGroupId;
    var selectedDatasource = $('#selectedDatasource').val().toString();
    data['selectedDatasource']= selectedDatasource;
    data['dateRange'] = valueChanged;
    var selectedDataSources = $('#selectedDatasource').val();
    $.ajax({
        type: "POST",
        url: '/signal/aggregateCaseAlert/getVigibaseEvdasAndFaersDateRange',
        data: data,
    })
        .done(function (data) {
            if (checkForDbDown(data)) {
                $(document).find("#setFaersDateRange").text("FAERS: Unable to load data due to an error");
                $(document).find("#setEvdasDateRange").text("EVDAS: Unable to load data due to an error");
                $(document).find("#setVigibaseDateRange").text("VigiBase: Unable to load data due to an error");
                return false;
            }
            if(data.status==="failed"){
                $.Notification.notify('error', 'top right', "error", "Please upload evdas file first.", {autoHideDelay: 5000});
            } else {
                if(selectedDataSources.length == 2) {
                    if ((selectedDataSources.includes('pva') && selectedDataSources.includes('vigibase')) || selectedDataSources.includes('faers') && selectedDataSources.includes('vigibase')) {
                        $(document).find("#setFaersDateRange").hide();
                        $(document).find("#setFaersDateRange").text("");
                        $(document).find("#setVaersDateRange").hide();
                        $(document).find("#setVaersDateRange").text("");
                        $(document).find("#setVigibaseDateRange").show().addClass("m-t-7");
                        $(document).find("#setEvdasDateRange").hide();
                        $(document).find("#setEvdasDateRange").text("");
                        if (typeof data.vigibaseDate === "undefined") {
                            $(document).find("#setVigibaseDateRange").text("VigiBase: Unable to load data due to an error");
                        }
                        else if (typeof exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !== "") {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + setCharAt(exeVigibaseDateRange, 12, 'to'));
                        } else {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + data.vigibaseDate);
                        }
                    } else if (selectedDataSources.includes('eudra') && selectedDataSources.includes('vigibase')) {
                        $(document).find("#setFaersDateRange").hide();
                        $(document).find("#setFaersDateRange").text("");
                        $(document).find("#setVaersDateRange").hide();
                        $(document).find("#setVaersDateRange").text("");
                        $(document).find("#setVigibaseDateRange").hide();
                        $(document).find("#setVigibaseDateRange").text("");
                        $(document).find("#setEvdasDateRange").show().addClass("m-t-7");
                        if (typeof data.evdasStartDate === "undefined" || typeof data.evdasEndDate === "undefined") {
                            $(document).find("#setEvdasDateRange").text("EVDAS: Unable to load data due to an error");
                        }else if(typeof  exeEvdasDateRange !== "undefined" && exeEvdasDateRange !==""){
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + setCharAt(exeEvdasDateRange,12,'to'));
                        } else {
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + data.evdasStartDate + " to " + data.evdasEndDate);
                        }
                    } else if (selectedDataSources.includes('vaers') && selectedDataSources.includes('vigibase')) {
                        $(document).find("#setFaersDateRange").hide();
                        $(document).find("#setFaersDateRange").text("");
                        $(document).find("#setVaersDateRange").hide();
                        $(document).find("#setVaersDateRange").text("");
                        $(document).find("#setVigibaseDateRange").show().addClass("m-t-7");
                        $(document).find("#setEvdasDateRange").hide();
                        $(document).find("#setEvdasDateRange").text("");
                        if (typeof data.vigibaseDate === "undefined") {
                            $(document).find("#setVigibaseDateRange").text("VigiBase: Unable to load data due to an error");
                        }else if (typeof exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !== "") {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + setCharAt(exeVigibaseDateRange, 12, 'to'));
                        } else {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + data.vigibaseDate);
                        }
                    }
                } else if (selectedDataSources.length == 3) {
                    if (selectedDataSources.includes('pva') && selectedDataSources.includes('vigibase') && selectedDataSources.includes('faers')) {
                        $(document).find("#setFaersDateRange").show().addClass("m-t-7");
                        $(document).find("#setVaersDateRange").hide();
                        $(document).find("#setVaersDateRange").text("");
                        $(document).find("#setVigibaseDateRange").show().addClass("m-t-7");
                        $(document).find("#setEvdasDateRange").hide();
                        $(document).find("#setEvdasDateRange").text("");
                        if (typeof data.vigibaseDate === "undefined") {
                            $(document).find("#setVigibaseDateRange").text("VigiBase: Unable to load data due to an error");
                        }else if (typeof exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !== "") {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + setCharAt(exeVigibaseDateRange, 12, 'to'));
                        } else {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + data.vigibaseDate);
                        }

                        if (typeof data.faersDate === "undefined") {
                            $(document).find("#setFaersDateRange").text("FAERS: Unable to load data due to an error");
                        }else if(typeof  exeFaersDateRange !== "undefined" && exeFaersDateRange !==""){
                            $(document).find("#setFaersDateRange").text("Date Range (FAERS): " + setCharAt(exeFaersDateRange,12,'to'));
                        } else {
                            $(document).find("#setFaersDateRange").text("Date Range (FAERS): " + data.faersDate);
                        }
                    } else if (selectedDataSources.includes('pva') && selectedDataSources.includes('vigibase') && selectedDataSources.includes('vaers')) {
                        $(document).find("#setFaersDateRange").hide();
                        $(document).find("#setFaersDateRange").text("");
                        $(document).find("#setVaersDateRange").show().addClass("m-t-7");
                        $(document).find("#setVigibaseDateRange").show().addClass("m-t-7");
                        $(document).find("#setEvdasDateRange").hide();
                        $(document).find("#setEvdasDateRange").text("");
                        if (typeof data.vigibaseDate === "undefined") {
                            $(document).find("#setVigibaseDateRange").text("VigiBase: Unable to load data due to an error");
                        }else if (typeof exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !== "") {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + setCharAt(exeVigibaseDateRange, 12, 'to'));
                        } else {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + data.vigibaseDate);
                        }
                        getVaersDateRange(valueChanged);
                    } else if (selectedDataSources.includes('eudra') && selectedDataSources.includes('vigibase') && (selectedDataSources.includes('faers') || selectedDataSources.includes('pva') || selectedDataSources.includes('vaers'))) {
                        $(document).find("#setFaersDateRange").hide();
                        $(document).find("#setFaersDateRange").text("");
                        $(document).find("#setVaersDateRange").hide();
                        $(document).find("#setVaersDateRange").text("");
                        $(document).find("#setVigibaseDateRange").show().addClass("m-t-7");
                        $(document).find("#setEvdasDateRange").show().addClass("m-t-7");
                        if (typeof data.vigibaseDate === "undefined") {
                            $(document).find("#setVigibaseDateRange").text("VigiBase: Unable to load data due to an error");
                        }else if (typeof exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !== "") {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + setCharAt(exeVigibaseDateRange, 12, 'to'));
                        } else {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + data.vigibaseDate);
                        }
                        if (typeof data.evdasStartDate === "undefined" || typeof data.evdasEndDate === "undefined") {
                            $(document).find("#setEvdasDateRange").text("EVDAS: Unable to load data due to an error");
                        }else if(typeof  exeEvdasDateRange !== "undefined" && exeEvdasDateRange !==""){
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + setCharAt(exeEvdasDateRange,12,'to'));
                        } else {
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + data.evdasStartDate + " to " + data.evdasEndDate);
                        }
                    }
                } else if (selectedDataSources.length == 4) {
                    if (selectedDataSources.includes('pva') && selectedDataSources.includes('vigibase') && selectedDataSources.includes('faers') && selectedDataSources.includes('eudra')) {
                        $(document).find("#setFaersDateRange").show().addClass("m-t-7");
                        $(document).find("#setVaersDateRange").hide();
                        $(document).find("#setVaersDateRange").text("");
                        $(document).find("#setVigibaseDateRange").show().addClass("m-t-7");
                        $(document).find("#setEvdasDateRange").show().addClass("m-t-7");
                        if (typeof data.vigibaseDate === "undefined") {
                            $(document).find("#setVigibaseDateRange").text("VigiBase: Unable to load data due to an error");
                        } else  if (typeof exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !== "") {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + setCharAt(exeVigibaseDateRange, 12, 'to'));
                        } else {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + data.vigibaseDate);
                        }
                        if (typeof data.evdasStartDate === "undefined" || typeof data.evdasEndDate === "undefined") {
                            $(document).find("#setEvdasDateRange").text("EVDAS: Unable to load data due to an error");
                        }else if(typeof  exeEvdasDateRange !== "undefined" && exeEvdasDateRange !==""){
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + setCharAt(exeEvdasDateRange,12,'to'));
                        } else {
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + data.evdasStartDate + " to " + data.evdasEndDate);
                        }
                        if (typeof data.faersDate === "undefined") {
                            $(document).find("#setFaersDateRange").text("FAERS: Unable to load data due to an error");
                        }else if(typeof  exeFaersDateRange !== "undefined" && exeFaersDateRange !==""){
                            $(document).find("#setFaersDateRange").text("Date Range (FAERS): " + setCharAt(exeFaersDateRange,12,'to'));
                        } else {
                            $(document).find("#setFaersDateRange").text("Date Range (FAERS): " + data.faersDate);
                        }
                    } else if (selectedDataSources.includes('pva') && selectedDataSources.includes('vigibase') && selectedDataSources.includes('vaers') && selectedDataSources.includes('eudra')) {
                        $(document).find("#setFaersDateRange").hide();
                        $(document).find("#setFaersDateRange").text("");
                        $(document).find("#setVaersDateRange").show().addClass("m-t-7");
                        $(document).find("#setVigibaseDateRange").show().addClass("m-t-7");
                        $(document).find("#setEvdasDateRange").show().addClass("m-t-7");
                        if (typeof exeVigibaseDateRange !== "undefined" && exeVigibaseDateRange !== "") {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + setCharAt(exeVigibaseDateRange, 12, 'to'));
                        } else {
                            $(document).find("#setVigibaseDateRange").text("Date Range (VigiBase): " + data.vigibaseDate);
                        }
                        if (typeof data.evdasStartDate === "undefined" || typeof data.evdasEndDate === "undefined") {
                            $(document).find("#setEvdasDateRange").text("EVDAS: Unable to load data due to an error");
                        }else if(typeof  exeEvdasDateRange !== "undefined" && exeEvdasDateRange !==""){
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + setCharAt(exeEvdasDateRange,12,'to'));
                        } else {
                            $(document).find("#setEvdasDateRange").text("Date Range (EVDAS): " + data.evdasStartDate + " to " + data.evdasEndDate);
                        }
                        getVaersDateRange(valueChanged);
                    }
                }
            }
    });
}






function setCharAt(str,index,chr) {
    if(index > str.length-1) return str;
    return str.substring(0,index) + chr + str.substring(index+1);
}

function initializeAlertDatePickersForEdit(currentDocument) {
    var from = null;
    var to = null;
    var dateRangeStartAbsolute = document.getElementById('alertDateRangeInformation.dateRangeStartAbsolute');
    if (dateRangeStartAbsolute && dateRangeStartAbsolute.value) {
        from = currentDocument.getElementById('alertDateRangeInformation.dateRangeStartAbsolute').value;
        to = currentDocument.getElementById('alertDateRangeInformation.dateRangeEndAbsolute').value;
    }

    $(currentDocument).find('#alertDateRange #datePickerFromDiv').datepicker({
        allowPastDates: true,
        date: from,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $(currentDocument).find('#alertDateRange #datePickerToDiv').datepicker({
        allowPastDates: true,
        date: to,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $("#dateRangeStart").on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()))
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
        currentDocument.getElementById('alertDateRangeInformation.dateRangeStartAbsolute').value=$(this).val()
    });
    $("#dateRangeEnd").on('focusout', function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()))
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
        currentDocument.getElementById('alertDateRangeInformation.dateRangeEndAbsolute').value=$(this).val()
    });

}

function selectGlobalQueryOnChange(selectContainer) {
    var queryContainer = $(".alertQueryWrapper");
    var expressionValues = getExpressionValues(queryContainer);
    $(expressionValues).empty();
    if (getAJAXCount() == -1) {
        getBlankValuesForQueryAJAX("qev", $(selectContainer).val(), expressionValues, '');
        getCustomSQLValuesForQueryAJAX("qev", $(selectContainer).val(), expressionValues, '');
        getBlankValuesForQuerySetAJAX("qev", $(selectContainer).val(), expressionValues, '');
    } else {
        $(selectContainer).val('');
    }
    var data = $("#alertQuery").val();
    if (data != null && data != "null" && data != "") {
        $(".bgQueryIcon").show();
        $(".viewBgQuery").attr("href", window.location.origin + "/signal/query/view/" + data);
        $(".viewBgQuery").show();
    } else {
        $(".viewBgQuery").hide();
        $(".bgQueryIcon").hide();
    }
}

function selectForgroundGlobalQueryOnChange(selectContainer) {
    var queryContainer = $(".alertQueryWrapper1");
    var expressionValues = getExpressionValues1(queryContainer);
    $(expressionValues).empty();
    getBlankValuesForQueryAJAX("fev", selectContainer, expressionValues, '');
    getCustomSQLValuesForQueryAJAX("fev", selectContainer, expressionValues, '');
    getBlankValuesForQuerySetAJAX("fev", selectContainer, expressionValues, '');
    var data = $("#alertForegroundQuery").val();
    if (data != null && data != "null" && data != "") {
        $(".fgQueryIcon").show();
        $(".viewFgQuery").attr("href", window.location.origin + "/signal/query/view/" + data);
        $(".viewFgQuery").show();
    } else {
        $(".viewFgQuery").hide();
        $(".fgQueryIcon").hide();
    }
}

function hideErrorOfXEnums(currentDocument) {
    $(currentDocument.getElementById('alertDateRangeInformation.relativeDateRangeValue')).hide();
    $(currentDocument.getElementById('alertDateRangeInformation.relativeDateRangeValue')).val("1"); //By default value is also 1
    $(currentDocument.getElementById('alertDateRangeInformation.relativeDateRangeValue')).parent().find('.notValidNumberErrorMessage').hide();
    $(currentDocument.getElementById('alertDateRangeInformation.relativeDateRangeValue')).parent().removeClass('has-error')
}