
var dataSources = {"PVA": "pva", "FAERS": 'faers', 'EUDRA': 'eudra', 'VAERS': 'vaers', 'VIGIBASE': 'vigibase','JADER':'jader'};
var faersDisabledColumnsIndexes = signal.utils.localStorageUtil.getProp("faersDisabledColumnsIndexes");
var initializing_first = true
var default_vaccine_pva = 'Vaccine-S';
var default_vaccine_vigibase = 'VACCINE_SUSPECT_VAERS';
var default_vaccine_vaers = 'VACCINE_SUSPECT_VIGIBASE';
$(function () {
    if (!$("#selectedDatasource").is("input")) {
        $("#selectedDatasource").select2({
            placeholder: "Select a datasource",
            allowClear: true
        });
    }

    var clear_all_records_and_inputs = function () {
        if ($('#fromDate').length) {
            $('#search').prop('disabled', true);
            $("#fromDate").val('');
            $("#toDate").val('');
        }

        if($('#productSelection').val() && ($('#productGroupSelection').val() === '[]' || !$('#productGroupSelection').val())) {
            $("div.productDictionaryValue").text("");
            $("#showProductSelection").html("");
            $("#productSelection").val("");
            $('.clearProductValues').trigger('click');
            $("#isMultiIngredient").val(false);

            $("input.searchProducts").each(function () {
                $(this).val("")
            });
        }

    };


    var close_and_add_selected_items = function () {
    };

    $(".spotfire-date-range").select2();
    $spotfireDaterange = $("#spotfireDaterange");

    function disableEnableDateRange(status, selectedValues) {
        var daterangeList = [$spotfireDaterange.find("option:eq(0)"), $spotfireDaterange.find("option:eq(1)"),
            $spotfireDaterange.find("option:eq(2)"), $spotfireDaterange.find("option:eq(3)"), $spotfireDaterange.find("option:eq(4)"), $spotfireDaterange.find("option:eq(5)"), $spotfireDaterange.find("option:eq(6)"), $spotfireDaterange.find("option:eq(7)")];
        if (status === 'safetyDR') {
            if(selectedValues === null || selectedValues.length == 0) {
                $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
            }
            $.each(daterangeList, function (index, daterange) {
                if (index > 1)
                    daterange.attr('disabled', 'disabled').trigger("change");
                else {
                    daterange.removeAttr('disabled').trigger("change");
                    if (selectedValues && selectedValues.includes(daterange.val())) {
                        daterange.prop('selected', 'selected').trigger("change");
                    }
                }
            });
        } else {
            if(status === 'faersDR') {
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[2].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (index <= 1)
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else if (index == 4 || index == 5 || index == 6 || index == 7) {
                        daterange.attr('disabled', 'disabled').trigger("change");
                    }
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'vaersDR'){
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[4].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (index <= 3)
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else if (index == 6 || index == 7) {
                        daterange.attr('disabled', 'disabled').trigger("change");
                    }
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'eudraDR'){
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[6].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (index <= 5)
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            }else if(status === 'vigibaseDR'){
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[6].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (index <= 5)
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'safetyAndFaersAndVigibaseDR'){
                var dateRangeArray = [4, 5];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'safetyAndVaersAndVigibaseDR'){
                var dateRangeArray = [2, 3];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            }else if(status === 'safetyAndVaersAndEudraDR'){
                var dateRangeArray = [2, 3];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            }else if(status === 'safetyAndFaersAndEudraDR'){
                var dateRangeArray = [4,5];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            }else if(status === 'safetyAndVigibaseAndEudraDR'){
                var dateRangeArray = [2,3,4,5];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'safetyAndFaersDR'){
                var dateRangeArray = [4, 5, 6, 7];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'safetyAndVigibaseDR'){
                var dateRangeArray = [2, 3, 4, 5];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'faersAndVigibaseDR'){
                var dateRangeArray = [0, 1, 4, 5];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[2].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'safetyAndVaersDR'){
                var dateRangeArray = [2, 3, 6, 7];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'vaersAndVigibaseDR') {
                var dateRangeArray = [0, 1, 2, 3];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[4].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'safetyAndEudraDR') {
                var dateRangeArray = [2, 3, 4, 5, 6, 7];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'vaersAndEudraDR') {
                var dateRangeArray = [0, 1, 2, 3, 6, 7,];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[4].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'faersAndEudraDR') {
                var dateRangeArray = [0, 1, 4, 5, 6, 7];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[2].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else if(status === 'vigibaseAndEudraDR') {
                var dateRangeArray = [0, 1, 2, 3, 4, 5];
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[6].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    if (dateRangeArray.includes(index))
                        daterange.attr('disabled', 'disabled').trigger("change");
                    else {
                        daterange.removeAttr('disabled').trigger("change");
                        if (selectedValues && selectedValues.includes(daterange.val())) {
                            daterange.prop('selected', 'selected').trigger("change");
                        }
                    }
                });
            } else {
                if(selectedValues === null || selectedValues.length == 0) {
                    $spotfireDaterange.val(daterangeList[0].val()).trigger("change");
                }
                $.each(daterangeList, function (index, daterange) {
                    daterange.removeAttr('disabled').trigger("change");
                    if (selectedValues && selectedValues.includes(daterange.val())) {
                        daterange.prop('selected', 'selected').trigger("change");
                    }
                });
            }
        }
    }
    var isFirstChange = true;
    $('#selectedDatasource').on('change', function () {
        var selectedDatasourceVal = $('#selectedDatasource').val();
        if(typeof selectedDatasourceVal === "string" ) {
            disableEnableDateRange("safetyDR", null);
        } else if (typeof $('#selectedDatasource').val() !== 'undefined' && $('#selectedDatasource').val() !== null && isDataSourceEnabled) {
            if ($('#selectedDatasource').val().length == 1
                && $('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1) {
                disableEnableDateRange("safetyDR", null);
            } else if ($('#selectedDatasource').val().length == 1
                && $('#selectedDatasource').val().filter(ds => ds == dataSources.FAERS).length == 1) {
                disableEnableDateRange("faersDR", null);
            } else if ($('#selectedDatasource').val().length == 1
                && $('#selectedDatasource').val().filter(ds => ds == dataSources.VAERS).length == 1) {
                disableEnableDateRange("vaersDR", null);
            }else if ($('#selectedDatasource').val().length == 1
                && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                disableEnableDateRange("eudraDR", null);
            }
            else if ($('#selectedDatasource').val().length == 1
                && $('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1) {
                disableEnableDateRange("vigibaseDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.FAERS).length == 1) {
                disableEnableDateRange("safetyAndFaersDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1) {
                disableEnableDateRange("safetyAndVigibaseDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.FAERS).length == 1) {
                disableEnableDateRange("faersAndVigibaseDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VAERS).length == 1) {
                disableEnableDateRange("safetyAndVaersDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                disableEnableDateRange("safetyAndEudraDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VAERS).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                disableEnableDateRange("vaersAndEudraDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.FAERS).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                disableEnableDateRange("faersAndEudraDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                disableEnableDateRange("vigibaseAndEudraDR", null);
            } else if ($('#selectedDatasource').val().length == 2 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VAERS).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1) {
                disableEnableDateRange("vaersAndVigibaseDR", null);
            } else if ($('#selectedDatasource').val().length == 3 || $('#selectedDatasource').val().length == 4) {
                    if ($('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.FAERS).length == 1) {
                        disableEnableDateRange("safetyAndFaersAndVigibaseDR", null);
                    } else if ($('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VAERS).length == 1) {
                        disableEnableDateRange("safetyAndVaersAndVigibaseDR", null);
                    } else if ($('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VAERS).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                        disableEnableDateRange("safetyAndVaersAndEudraDR", null);
                    }else if ($('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.FAERS).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                        disableEnableDateRange("safetyAndFaersAndEudraDR", null);
                    }else if ($('#selectedDatasource').val().filter(ds => ds == dataSources.PVA).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.VIGIBASE).length == 1 && $('#selectedDatasource').val().filter(ds => ds == dataSources.EUDRA).length == 1) {
                        disableEnableDateRange("safetyAndVigibaseAndEudraDR", null);
                    }
            }
        }
        if(typeof disableLockedCheckbox == 'function' ) {
            disableLockedCheckbox()
            changeCheckbox(isFirstChange)
        }
        setDrugTypeValues(selectedDatasourceVal,isFirstChange)
        $(".spotfire-date-range").select2().trigger('selection:update');
        if(isFirstChange){
            isFirstChange = false;
        } else {
            clear_all_records_and_inputs();
        }
        close_and_add_selected_items();
        if (isDataSourceEnabled && $('#selectedDatasource').val() && $('#selectedDatasource').val().length == 1) {
            if ($('#selectedDatasource').val()[0] !== dataSources.EUDRA) {
                changeDataSource($('#selectedDatasource').val()[0]);
                if ($('#selectedDatasource').val()[0] === dataSources.FAERS) {
                    $('#missedCases').prop("checked", false).prop("disabled", true);
                }
                if ($('#selectedDatasource').val()[0] === dataSources.VIGIBASE) {
                    $('#missedCases').prop("checked", false).prop("disabled", true);
                }
            }
            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] !== dataSources.FAERS && $('#selectedDatasource').val()[0] !== dataSources.VIGIBASE) {
                $('#missedCases').prop("disabled", false);
            }


            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.VAERS) {
                $('#includeLockedVersion').prop("checked", false).prop("disabled", true);
                $('#excludeFollowUp').prop("checked", false).prop("disabled", true);
                if ( typeof initializing_first != "undefined" && !initializing_first) {
                    $('#missedCases').prop("checked", false).prop("disabled", true);
                    $('#adhocRun').prop("checked", true);
                }
            }

            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.FAERS) {
                $('#missedCases').prop("checked", false).prop("disabled", true);
            }
            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.VIGIBASE) {
                if ( typeof initializing_first != "undefined" && !initializing_first) {
                    $('#missedCases').prop("checked", false).prop("disabled", true);
                    $('#adhocRun').prop("checked", true);
                    $('#excludeFollowUp').prop("checked", true).prop("disabled", false);
                }
            }
            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.JADER) {
                if ( typeof initializing_first != "undefined" && !initializing_first) {
                    $('#missedCases').prop("checked", false).prop("disabled", true);
                    $('#adhocRun').prop("checked", true);
                    $('#excludeFollowUp').prop("checked", true).prop("disabled", false);
                    $('#isTemplateAlert').prop('disabled', true);
                    $('#isMasterTemplateAlert').prop('disabled', true);

                }
            }
            if ($('#selectedDatasource').val().length == 1 && $('#selectedDatasource').val()[0] === dataSources.EUDRA) {
                $('#missedCases').prop("checked", false).prop("disabled", false);
                disableDataMiningVariableFields(true)
            }
        } else {
            changeDataSource($('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.VAERS) ? "vaers" : $('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.VIGIBASE) ? "vigibase" : $('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.JADER) ? "jader" : "pva")
        }

        if($('#selectedDatasource').val() && $('#selectedDatasource').val()[0] == dataSources.EUDRA) {
            if(typeof initializing_first != "undefined" && !initializing_first) {
                $('#excludeFollowUp').prop("checked", false).prop("disabled", true);
            }
        }

        if($('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.VAERS) && !$('#selectedDatasource').val().includes(dataSources.PVA)){
            $('#includeLockedVersion').prop("checked", false).prop("disabled", true);
            $('#excludeFollowUp').prop("checked", false).prop("disabled", true);
            $('#missedCases').prop("checked", false).prop("disabled", true);
            $('#reportSection').removeClass('hide').addClass('show');
            $('#templateQueryList').removeClass('show').addClass('hide');
            $('.report-section-buttons').removeClass('show').addClass('hide');
        } else {
            $('#reportSection').removeClass('hide').addClass('show');
        }
        if($('#selectedDatasource').val()?.length===1 && ($('#selectedDatasource').val()==dataSources.VIGIBASE)){
            $('#reportSection').removeClass('show').addClass('hide');
            $('.drugClassificationContainer').hide();
        }
        if($('#selectedDatasource').val()?.length===1 && ($('#selectedDatasource').val()==dataSources.JADER)){
            $('#reportSection').removeClass('show').addClass('hide');
            $('.drugClassificationContainer').hide();
            $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
            $('#includeLockedVersion').prop("checked", false).prop("disabled", true);
            $('#missedCases').prop("checked", false).prop("disabled", true);
            $('#isTemplateAlert').prop('disabled', true);
            $('#isMasterTemplateAlert').prop('disabled', true);
        }

        if($('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.PVA) && $('#selectedDatasource').val().includes(dataSources.VAERS)) {
            if (typeof initializing_first != "undefined" && !initializing_first) {
                $('#excludeFollowUp').prop("checked", true).prop("disabled", false);
            }
        }
        if(($('#selectedDatasource').val() == dataSources.JADER)){
            if (typeof initializing_first != "undefined" && !initializing_first) {
                $('#excludeFollowUp').prop("checked", false);
            }

        }


        if($('#selectedDatasource').val() && !$('#selectedDatasource').val().includes(dataSources.PVA)){
            $('#templateQueryList').removeClass('show').addClass('hide');
            $('.report-section-buttons').removeClass('show').addClass('hide');
        }

        if($('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.PVA)){
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

        initializing_first = false
        let productAssignment = $("#productSelection").val();
        let productGroupAssignment = $("#productGroupSelection").val() !== "[]" ? $("#productGroupSelection").val() : "";
        if((productAssignment || productGroupAssignment) && $("#selectedDatasource").val().includes('pva')){
            $('#studyDrugCases').prop('disabled',false);
            if($('#studyDrugCases')[0].checked){
                $('#additionalOptions').removeClass('hidden');
            }
        } else {
            $('#studyDrugCases').prop('disabled',true);
            $('#studyDrugCases').prop("checked",false);
            $('#additionalOptions').addClass('hidden');
            $('#ignoreMedicationType').prop('checked',false);
            $('#considerPOI').prop('checked',false);
        }
    });

    var selectedDatasource = $("#selectedDatasource").val();
    if (!$spotfireDaterange.val()) {
        $('#selectedDatasource').trigger('change');
    } else if (selectedDatasource) {
        var spotFireDateRangeValues = $spotfireDaterange.val();
        if (selectedDatasource.includes(dataSources.PVA) && !selectedDatasource.includes(dataSources.FAERS) && !selectedDatasource.includes(dataSources.VIGIBASE) && !selectedDatasource.includes(dataSources.VAERS)) {
            disableEnableDateRange("safetyDR", spotFireDateRangeValues);
        } else if (!selectedDatasource.includes(dataSources.PVA) && selectedDatasource.includes(dataSources.FAERS)) {
            disableEnableDateRange("faersDR", spotFireDateRangeValues);
        } else if (!selectedDatasource.includes(dataSources.PVA) && selectedDatasource.includes(dataSources.VIGIBASE)) {
            disableEnableDateRange("vigibaseDR", spotFireDateRangeValues);
        } else if (!selectedDatasource.includes(dataSources.PVA) && selectedDatasource.includes(dataSources.VAERS)) {
            disableEnableDateRange("vaersDR", spotFireDateRangeValues);
        } else if(selectedDatasource.includes(dataSources.PVA) && selectedDatasource.includes(dataSources.VAERS) && !selectedDatasource.includes(dataSources.VIGIBASE) && !selectedDatasource.includes(dataSources.FAERS)){
                disableEnableDateRange("safetyAndVaersDR", spotFireDateRangeValues);
        } else {
            disableEnableDateRange("safetyAndFaersDR", spotFireDateRangeValues);
        }
        setDrugTypeValues(selectedDatasource,isFirstChange)
        if(isFirstChange){
            isFirstChange = false;
        }
    }

    if(typeof isMultipleDatasource != 'undefined' && isMultipleDatasource){

        $('#selectedDatasource').select2()
            .on("select2:select", function (e) {
                var selected_element = $(e.currentTarget);
                var select_val = selected_element.val();
                $.each(this.options, function (i, item) {
                    if(!item.selected && select_val.includes(dataSources.JADER) && item.value !== dataSources.JADER){
                        $(item).prop("disabled", true);
                    }else if(!item.selected && item.value === dataSources.JADER){
                        $(item).prop("disabled", true);
                    } else if (!item.selected && select_val.includes(dataSources.FAERS) && item.value === dataSources.VAERS) {
                        $(item).prop("disabled", true);
                    } else if (!item.selected && select_val.includes(dataSources.VAERS) && item.value === dataSources.FAERS) {
                        $(item).prop("disabled", true);
                    }
                });
                $('#selectedDatasource').select2({
                    placeholder: "Select a datasource"
                });
            });

        if ((editAlert != "undefined") && (editAlert == "edit" || editAlert == "copy")) {
            $('#selectedDatasource').trigger('select2:select');
        }

        $('#selectedDatasource').select2().on("select2:unselect", function (e) {
                var value=   e.params.data.id;
                var enableDsWithRoles=enabledDataSourceList.toString()
                if (!$("#selectedDatasource").is("input")) {
                    $("#selectedDatasource").select2({
                        placeholder: "Select a datasource",
                        allowClear: true
                    });
                }
               var selectedValues = $('#selectedDatasource').val();
                $.each(this.options, function (i, item) {
                    if (!item.selected && value === dataSources.FAERS && item.value === dataSources.VAERS && (typeof enableDsWithRoles!=='undefined' && enableDsWithRoles.includes(item.value))) {
                        $(item).prop("disabled", false);
                    } else if (!item.selected && value === dataSources.VIGIBASE && item.value === dataSources.VAERS && (typeof enableDsWithRoles!=='undefined' && enableDsWithRoles.includes(item.value))) {
                        if ($('#selectedDatasource').val()?.filter(a => a == dataSources.FAERS).length == 0)
                        {
                            $(item).prop("disabled", false);
                        }
                    } else if (!item.selected && value === dataSources.VAERS && item.value === dataSources.FAERS && (typeof enableDsWithRoles!=='undefined' && enableDsWithRoles.includes(item.value))) {
                        $(item).prop("disabled", false);
                    } else if (!item.selected && value === dataSources.VAERS && item.value === dataSources.VIGIBASE && (typeof enableDsWithRoles!='undefined' && enableDsWithRoles.includes(item.value))) {
                        $(item).prop("disabled", false);
                    }
                    if (selectedValues && item.value != dataSources.JADER && typeof enableDsWithRoles !== 'undefined' && enableDsWithRoles.includes(item.value) && !item.selected && value === dataSources.VAERS && item.value === dataSources.FAERS) {
                        $(item).prop("disabled", false);
                    } else if (!selectedValues && item.value === dataSources.JADER && (typeof enableDsWithRoles !== 'undefined' && enableDsWithRoles.includes(item.value))) {
                        $(item).prop("disabled", false);
                    } else if (!selectedValues && (typeof enableDsWithRoles !== 'undefined' && enableDsWithRoles.includes(item.value))) {
                        $(item).prop("disabled", false);
                    }
                });
                $('#selectedDatasource').select2({
                    placeholder: "Select a datasource"
                });
            });
    }

    if(!faersDisabledColumnsIndexes){
        $.ajax({
            type: "GET",
            url: faersDisabledColumnsIndexesUrl,
        })
            .done(function (response) {
                signal.utils.localStorageUtil.setProp("faersDisabledColumnsIndexes", response.disabledIndexValues);
            });
    }
});
function changeDataSource(ds) {
    if (ds === dataSources.EUDRA) {
        setDictionaryUrls(otherUrls.selectUrl + "/" + ds, otherUrls.preLevelParentsUrl + "/" + ds, otherUrls.searchUrl + "/" + ds);
    } else {
        setDictionaryUrls(pvaUrls.selectUrl, pvaUrls.preLevelParentsUrl, pvaUrls.searchUrl);
    }
    if (ds === dataSources.FAERS) {
        disableDictionaryValues(false, false, true, false, true);
        // $("#productModal .modal-body").children().slice(0, 2).hide();
    } else if (ds === dataSources.EUDRA) {
        disableDictionaryValues(true, false, true, true, true);
        $("#productModal .modal-body").children().slice(0, 2).hide();
    } else if (ds === dataSources.VAERS) {
        disableDictionaryValues(false, true, true, false, true);
    } else if (ds === dataSources.VIGIBASE) {
        disableDictionaryValues(false, false, true, false, true);
    } else if (ds === dataSources.JADER) {
        disableDictionaryValues(false, false, true, false, true);
    } else {
        disableDictionaryValues(false, false, false, false, false);
        $("#productModal .modal-body").children().slice(0, 2).show();
    }
    toggleInterfaceChanges(ds);
}

function toggleInterfaceChanges(value) {
    $("#dataSheet").html("");
    if (value && value !== 'jader') {
        // Disable JADER if Other Data Source is coming by default selected
        $('#selectedDatasource option[value="jader"]').prop("disabled", true);
    } else {
        $('#selectedDatasource option[value="jader"]').prop("disabled", false);
    }
    $('#advacedOptionSection').removeClass('hide').addClass('show');
    if (value === dataSources.FAERS || value.includes(dataSources.FAERS)) {
        $('#asOfVersionDatePicker').hide();
        if (!(value.includes(dataSources.PVA) || value.includes(dataSources.EUDRA))) {
            $('#missedCases').prop("checked", false).prop("disabled", true);
        }
    }
    else if(value === dataSources.VAERS || value.includes(dataSources.VAERS)) {
     } else if ((value === dataSources.VIGIBASE || value.includes(dataSources.VIGIBASE))) {
        if((typeof editAlert !== 'undefined' && editAlert === "create")){
        }
    disableDataMiningVariableFields(true)
    } else if (value === dataSources.VIGIBASE){
        $('#reportSection').removeClass('show').addClass('hide');
    } else if (value === dataSources.JADER){
        $('#reportSection').removeClass('show').addClass('hide');
        $('#advacedOptionSection').removeClass('show').addClass('hide');
        $('#isTemplateAlert').prop('disabled', true);
        $('#isMasterTemplateAlert').prop('disabled', true);
    } else if (value === dataSources.EUDRA){
      disableDataMiningVariableFields(true)
    }
    if(value === dataSources.PVA || value === dataSources.FAERS) {
        disableDataMiningVariableFields(!$("#adhocRun").is(":checked"));
        miningVariableDropdown(value);
    }
    if(typeof hasNormalAlertExecutionAccess != "undefined"  && !hasNormalAlertExecutionAccess) {
            $("#adhocRun").prop('disabled', true);
    }
}
function miningVariableDropdown(value) {
    $.ajax({
        type: "GET",
        url: '/signal/aggregateCaseAlert/fetchMiningVariables' + "?selectedDatasource=" + value,
        dataType: "json",
    })
        .done(function (result) {
            var miningVariables = document.getElementById("dataMiningVariable");
            if(miningVariables.options){
                var len = miningVariables?.options.length - 1;
                for (var i = len; i > 0; i--) {
                    miningVariables.options.remove(i);
                }
            }
            for (var i = 0; i < result.length; i++) {
                var option = new Option(result[i].label, result[i].use_case+";"+result[i].isMeddra+";"+result[i].isOob+";"+result[i].isautocomplete+";"+result[i].dic_level+";"+result[i].dic_type+";"+result[i].validatable, false, false);
                if((editAlert==="edit" || editAlert==="copy" || isValidationError) && configurationMiningVariable && configurationMiningVariable === result[i].label) {
                    option = new Option(result[i].label, result[i].use_case+";"+result[i].isMeddra+";"+result[i].isOob+";"+result[i].isautocomplete+";"+result[i].dic_level+";"+result[i].dic_type+";"+result[i].validatable, true, true);
                }
                miningVariables.append(option);
           }
        })
        .fail(function () {
            console.log("Error while fetching the mining variables")
        });
}


function setDictionaryUrls(selectUrl, preLevelParentsUrl, searchUrl) {
    options.product.selectUrl = selectUrl;
    options.product.preLevelParentsUrl = preLevelParentsUrl;
    options.product.searchUrl = searchUrl;
    productDictionaryObj = new Dictionary(PRODUCT_DICTIONARY, options.product.levelNames, options.product.dicColumnCount, options.product.selectUrl, options.product.preLevelParentsUrl, options.product.searchUrl, options.spinnerPath);
}
 function setDrugTypeValues(selectedDatasourceVal,isFirstChange) {
    if(typeof editAlert !== 'undefined' && (editAlert === "create" || editAlert === "copy" || editAlert === "edit")) {
        var selectedDataSourceList = []
        if (selectedDatasourceVal instanceof Array){
            selectedDataSourceList=selectedDatasourceVal;
        }else if(typeof(selectedDatasourceVal)=='string'){
            selectedDataSourceList.push(selectedDatasourceVal);
        }
        $("#drugType").select2({
            multiple: true,
            placeholder: 'Select Values',
            allowClear: true
        });
        $('#drugType').on('select2:select', function (e) {
            var data = e.params?.data;
            var value = e.params?.data?.id;
            let obj = productTypeOptions.find(o => o.id == value);
            if(obj !== null && obj !== undefined) {
                obj.isManual = true;
            }
        });
        $('#drugType').on('select2:unselect', function (e) {
            var data = e.params?.data;
            var value = e.params?.data?.id;
            let obj = productTypeOptions.find(o => o.id == value);
            if(obj !== null && obj !== undefined){
                obj.isManual = false;
            }
        });
        $("#drugType").find('option').remove();
        selectedDataSourceList?.forEach((element) => {
            productTypeOptions.forEach((map) => {
                if (map.dataSource === element) {
                    if (map.default) {
                        $("#drugType").append(
                            '<option value="' + map.id + '" data-source="' + map.dataSource + '" title="Data Source: ' + map.displaySource + (element === 'pva' ? ('&#013Product Type: ' + map.productType) : '') + '&#013Role: ' + map.roleType + '" selected>' + encodeToHTML(map.name) + '</option>'
                        );
                    } else {
                        $("#drugType").append(
                            '<option value="' + map.id + '" data-source="' + map.dataSource + '" title="Data Source: ' + map.displaySource + (element === 'pva' ? ('&#013Product Type: ' + map.productType) : '') + '&#013Role: ' + map.roleType + '" >' + encodeToHTML(map.name) + '</option>'
                        );
                    }
                }
            })
        });
        if (selectedDataSourceList?.includes('vaers')) {
            $("#drugType").find('option').remove();
            selectedDataSourceList.forEach((element) => {
                productTypeOptions.forEach((map) => {
                    if (map.dataSource === element) {
                        if (( map?.isDefaultVaers === true || map.id === default_vaccine_vaers || map.id === default_vaccine_vigibase)) {
                            $("#drugType").append(
                                '<option value="' + map.id + '" data-source="' + map.dataSource + '" title="Data Source:' + map.displaySource + (element === 'pva' ? ('&#013Product Type:' + map.productType) : '') + '&#013Role:' + map.roleType + '" selected>' + encodeToHTML(map.name) + '</option>'
                            );
                        } else {
                            $("#drugType").append(
                                '<option value="' + map.id + '" data-source="' + map.dataSource + '" title="Data Source:' + map.displaySource + (element === 'pva' ? ('&#013Product Type:' + map.productType) : '') + '&#013Role:' + map.roleType + '" >' + encodeToHTML(map.name) + '</option>'
                            );
                        }
                    }
                })
            });
        }
        // if validation error, then data-value attr should have priority and on further selection auto-populate should work
        // if create then default should be populated and on further selection manually selection also be appended
        // if edit data-value attr should have priority and on further datasource selection default also be populated
        //
        if((isValidationError || editAlert==='edit' || editAlert==='copy') && isFirstChange){
            var manually_selected = $("#drugType").attr('data-value').split(',');
            if(manually_selected.length>0){
                manually_selected.forEach(function (it) {
                    let obj = productTypeOptions.find(o => o.id == it);
                    if(obj !== null && obj !== undefined){
                        obj.isManual = true;
                    }
                })
            }
            $('#drugType').val(manually_selected).trigger('change');
        }else{
            var total_list = []
            total_list.push.apply(total_list, $('#drugType').val());
            total_list.push.apply(total_list, productTypeOptions.filter(function (item) {
                if (item.isManual == true) {
                    return item.id;
                }
            }).map((obj) => obj.id));
            $('#drugType').val(total_list).trigger('change');
        }
    }
}
