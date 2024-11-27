//= require app/pvs/alert_utils/common_key_prevent.js
var productSelectionModal = {};
var pvaQueryList;
var faersQueryList;
var productValues = {"1": [], "2": [], "3": [], "4": [], "5": []};

$(function () {

    var dataSources = {"PVA": "pva", "FAERS": 'faers', "VIGIBASE": 'vigibase', 'EUDRA': 'eudra'};
    var init = function () {
        $("#productModal").find("input.searchProducts").on('keyup', function (e) {
            if (e.keyCode == 13) {
                $("#fetchingProducts").show();
                $.ajax({
                    cache: false,
                    url: '/signal/pvsProductDictionary/searchProducts',
                    dataType: 'json',
                    data: {
                        productId: null,
                        contains: $(this).val(),
                        dictionaryLevel: $(this).attr('level'),
                        selectedDatasource: $(".selectedDatasource").val()
                    },
                })
                    .done(function (data) {
                        clear_all_records();
                        $(data).each(function (index) {
                            clear_all_inputs_except($(this).attr('level'));
                            var containerLevel = checkDictLevelMap($(this).attr('level') , $(".selectedDatasource").val());
                            if(containerLevel === PRODUCT_GENERIC_NAME){
                                containerLevel = ALERT_STOP_LIST_PRODUCT_NAME;
                            }
                            $("ul.productDictionaryColWidth[dictionarylevel='" + containerLevel + "']").append(
                                "<li tabindex='0' role='button' class='dicLi' data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" +
                                data[index].name + "</li>")
                        });
                        $("#fetchingProducts").hide();
                        init_get_components();
                    })
                    .fail(function () {
                        $("#fetchingProducts").hide();
                })
            }
        });

        $("button.addProductValues").on('click', function (event) {
            if ($('#productModal').hasClass('in')) {
                add_selected_items()
            }
        });

        $("button.addAllProducts").on("click", function (event) {
            if ($('#productModal').hasClass('in')) {
                close_and_add_selected_items()
            }
        });

        $("button.clearProductValues").on("click", function () {
            if ($('#productModal').hasClass('in')) {
                clear_all_records_and_inputs()
            }
        });

        $("button.addGenericValues").on("click", function () {
            add_generic_name()
        });

        $('button.clearGenericValues').on('click', function () {
            clear_generic_name()
        });
        load_products()
    };

    var clear_all_records = function () {
        $("ul.productDictionaryColWidth").find('li').each(function () {
            $(this).remove()
        })
    };

    var clear_all_records_and_inputs = function () {

        $("ul.productDictionaryColWidth").find('li').each(function () {
            $(this).remove()
        });

        $("input.searchProducts").each(function () {
            $(this).val("")
        });

        $("div.productDictionaryValue").text("");

        productValues = {"1": [], "2": [], "3": [], "4": [], "5": []}

    };

    var clear_all_parent_records = function (level) {
        $("ul.productDictionaryColWidth").find('li').each(function () {
            if ($(this).attr('dictionarylevel') < level) {
                $(this).remove()
            }
        })
    };

    var clear_all_child_records = function (level) {
        var levelVal = parseInt(level);
        while (levelVal <= 4) {
            levelVal = levelVal + 1;
            $("ul.productDictionaryColWidth").find("li[dictionarylevel=" + levelVal + "]").remove()
        }
    };

    var clear_all_inputs_except = function (level) {
        $("input.searchProducts").each(function () {
            if ($(this).attr('level') != level) {
                $(this).val("")
            }
        })
    };

    var init_get_components = function () {
        $("ul.productDictionaryColWidth").find('li').on('click', function (event) {
            var productId = $(this).attr("data-value");
            var dicLevel = $(this).attr("dictionarylevel");
            deselect_items();
            deselect_items_except(dicLevel);
            $(this).addClass('selected selectedBackground');
            clear_all_child_records($(this).attr('dictionarylevel'));
            get_selected_product(productId, dicLevel);
            get_pre_level_parents(dicLevel, productId);
        })
    };

    var get_new_components = function (elemId) {
        $("ul.productDictionaryColWidth").find("li[data-value=" + elemId + "]").on('click', function (event) {
            var productId = $(this).attr("data-value");
            var dicLevel = $(this).attr("dictionarylevel");
            $(this).addClass('selected selectedBackground');
            clear_all_child_records($(this).attr('dictionarylevel'));
            get_selected_product(productId, dicLevel)
        })
    };

    var get_selected_product = function (productId, dicLevel) {
        $("#fetchingProducts").show();
        var selectedDataSource = $(".selectedDatasource").val();
        $.ajax({
            cache: false,
            url: '/signal/pvsProductDictionary/getSelectedProduct',
            data: {
                dictionaryLevel: dicLevel,
                productId: productId,
                selectedDatasource: selectedDataSource
            },
        })
            .done(function (data) {
                var nextLevelItems = data.nextLevelItems;
                var containerLevel = ""
                $.each(nextLevelItems, function (index, element) {
                    if ($("ul.productDictionaryColWidth[dictionarylevel='" + checkDictLevelMap(element.level , selectedDataSource) + "'] li[data-value='" + element.id + "'").length == 0) {
                        if(checkDictLevelMap(element.level , selectedDataSource) === PRODUCT_GENERIC_NAME){
                              containerLevel = ALERT_STOP_LIST_PRODUCT_NAME
                        } else{
                            containerLevel = checkDictLevelMap(element.level , selectedDataSource)
                        }
                        if (nextLevelItems.length == 1) {
                            $("ul.productDictionaryColWidth[dictionarylevel='" + containerLevel + "']").append("<li tabindex='0' role='button' class='highlighted selected dicLi' data-value='" +
                                element.id + "' dictionarylevel='" + element.level + "'>" +
                                element.name + "</li>")
                        } else {
                            $("ul.productDictionaryColWidth[dictionarylevel='" + containerLevel + "']").append("<li tabindex='0' role='button' class='dicLi' data-value='" + element.id + "' dictionarylevel='" +
                                element.level + "'>" + element.name + "</li>")
                        }
                        get_new_components(element.id);
                        $("#fetchingProducts").hide();
                    }
                });
                select_item();
            })
            .fail(function () {
                $("#fetchingProducts").hide();
        })
    };

    var get_pre_level_parents = function (level, productId) {
        clear_all_parent_records(level);
        var productIds = "";
        if (productId == undefined) {
            $.each($("ul li[dictionarylevel='" + level + "']"), function () {
                productIds += $(this).attr("data-value") + ","
            })
        } else {
            productIds = productId
        }

        if (typeof productIds == "undefined") {
            productIds = ''
        }
        var selectedDataSource = $(".selectedDatasource").val();
        $.ajax({
            cache: false,
            url: '/signal/pvsProductDictionary/getPreLevelProductParents',
            data: {
                productIds: productIds,
                dictionaryLevel: level,
                selectedDatasource: $(".selectedDatasource").val()
            },
        })
            .done(function (data) {
                $.each(data, function (index, element) {
                    var containerLevel = ""
                    if ($("ul.productDictionaryColWidth[dictionarylevel='" + checkDictLevelMap(element.level , selectedDataSource) + "'] li[data-value='" + element.id + "'").length == 0) {
                        if(checkDictLevelMap(element.level , selectedDataSource) === PRODUCT_GENERIC_NAME){
                            containerLevel = ALERT_STOP_LIST_PRODUCT_NAME
                        } else{
                            containerLevel = checkDictLevelMap(element.level , selectedDataSource)
                        }
                        if (data.length == 1 ) {
                            $("ul.productDictionaryColWidth[dictionarylevel='" + containerLevel + "']").append(
                                "<li tabindex='0' role='button' class='highlighted selected dicLi' data-value='" + element.id +
                                "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        } else {
                            $("ul.productDictionaryColWidth[dictionarylevel='" + containerLevel + "']").append(
                                "<li tabindex='0' role='button' class='dicLi' data-value='" + element.id + "' dictionarylevel='" + element.level + "'>" +
                                element.name + "</li>")
                        }
                    }

                    if (level > 1 && $(".selectedDatasource").val() != dataSources.FAERS) {
                        get_pre_level_parents(level - 1)
                    }
                });
                $("#fetchingProducts").hide();
                select_item()
            })
            .fail(function () {
                $("#fetchingProducts").hide();
        })
    };

    var add_generic_name = function () {
        var generic_names = [];
        productValues[5] = [];
        $.each($("#genericModal").find("li.select2-search-choice div"), function (index, element) {
            var selectedObj = {"genericName": $(this).text()};
            generic_names.push($(this).text());
            productValues[5].push(selectedObj)
        });
        if (generic_names.length > 0) {
            $("#productSelection").val(JSON.stringify(productValues));
            if (typeof $("#product_multiIngredient") != "undefined" && $("#product_multiIngredient").is(':checked')) {
                $("#isMultiIngredient").val(true)
            } else {
                $("#isMultiIngredient").val(false)
            }
            $("#showGenericSelection").html("");
            $("#showGenericSelection").append("<div style='padding: 5px'>" + generic_names + "</div>")
        }
    };

    var clear_generic_name = function () {
        $("#js-data-generics").val('');
    };

    var add_selected_items = function () {
        var productModal = $("#productModal");
        var id = productModal.find("ul li.selectedBackground").attr("data-value");
        var name = productModal.find("ul li.selectedBackground").text();
        var level = productModal.find("ul li.selectedBackground").attr("dictionarylevel");
        var oldValue = productModal.find("div.level" + level).text();
        if (oldValue) {
            oldValue = oldValue + ", ";
        }

        var selectedValues = oldValue.split(',');
        selectedValues = _.map(selectedValues, function (v) {
            return v.trim().toLowerCase()
        });

        if (!_.contains(selectedValues, name.trim().toLocaleLowerCase())) {
            $("div.selectedProductDictionaryValue").find("div.level" + level).text(oldValue + " " + name)
            if (level) {
                var selectedObj = {"name": name, "id": id};
                productValues[level].push(selectedObj)
            }
        }
    };

    var close_and_add_selected_items = function () {
        var productList = [];
        $.each(productValues, function (index, element) {
            if (index != 5) {
                $.each(element, function (indx, elem) {
                    productList.push(elem.name)
                });
            }
        });
        if (!$.isEmptyObject(productList)) {
            $("#productSelection").val(JSON.stringify(productValues))
        } else {
            $("#productSelection").val("")
        }
        if (typeof $("#product_multiIngredient") != "undefined" && $("#product_multiIngredient").is(':checked')) {
            $("#isMultiIngredient").val(true)
        } else {
            $("#isMultiIngredient").val(false)
        }

        $("#showProductSelection").html("");
        $("#showProductSelection").append("<div style='padding: 5px'>" + productList + "</div>");
    };

    var load_products = function () {
        var all_products = $("#productSelection").val();

        if ($("#productSelection").val() != "") {
            $("#showProductSelection").html("");
            $("#showGenericSelection").html("");
            //multi-ingredient-set check box value

            if (typeof $("#product_multiIngredient") != "undefined" && typeof $("#isMultiIngredient") != "undefined") {
                if($("#isMultiIngredient").val() == 'true') {
                    $("#product_multiIngredient").prop('checked', true);
                } else {
                    $("#product_multiIngredient").prop('checked', false);
                }
            }
            $.each(JSON.parse(all_products), function (index, element) {
                if (index == 5) {
                    var generic_name_to_show = [];
                    if (element && element.length > 0) {
                        $.each(element, function (idx, elem) {
                            generic_name_to_show.push(elem.genericName);
                            productValues[index].push(elem);
                            $("#genericModal").find(".select2-search-field").html(
                                '<li class="select2-search-choice">    <div>' +
                                elem.genericName +
                                '</div>    <a href="#" class="select2-search-choice-close" tabindex="-1"></a></li>')
                        });
                        $("#showGenericSelection").append("<div style='padding: 5px'>" + generic_name_to_show + "</div>");
                        showGenericSelectionTextArea()
                    }
                } else if (index == 3 || index == 2 || index == 1 || index == 4) {
                    var prod_to_show = [];
                    if (element && element.length > 0) {
                        $.each(element, function (idx, elem) {
                            prod_to_show.push(elem.name);
                            productValues[index].push(elem)
                        });
                        $("#showProductSelection").append("<div style='padding: 2px'>" + prod_to_show + "</div>");
                        $("div.selectedProductDictionaryValue").find("div.level" + index).text(prod_to_show);
                        showProductSelectionTextArea()
                    }
                }
            })
        }
    };

    var deselect_items = function () {
        $("ul.productDictionaryColWidth").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted')
    };

    var deselect_items_except = function (level) {
        $("ul.productDictionaryColWidth").find('li.selected').each(function () {
            if ($(this).attr('dictionarylevel') == level) {
                $(this).removeClass("highlighted selected")
            }
        })
    };

    var select_item = function () {
        $("ul.productDictionaryColWidth").find('li').on('click', function (event) {
            $("ul.productDictionaryColWidth").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted');
            $(this).addClass('selected selectedBackground')
        })
    };

    var showProductSelectionTextArea = function () {
        $("#showProductSelection").closest('div.wrapper').removeAttr("hidden");
        $("#showStudySelection").closest('div.wrapper').attr('hidden', 'hidden');
        $("#showGenericSelection").closest('div.wrapper').attr("hidden", "hidden");
    };

    var showStudySelectionTextArea = function () {
        $(document.getElementById("showStudySelection").parentElement).removeAttr("hidden");
        $(document.getElementById("showProductSelection").parentElement).attr("hidden", "hidden");
        $(document.getElementById("showGenericSelection").parentElement).attr("hidden", "hidden");
    };

    var showGenericSelectionTextArea = function () {
        $(document.getElementById("showGenericSelection").parentElement).removeAttr("hidden");
        $(document.getElementById("showProductSelection").parentElement).attr("hidden", "hidden");
        $(document.getElementById("showStudySelection").parentElement).attr("hidden", "hidden");
    };

    init();

    function toggleInterfaceChanges(value) {
        if (value === dataSources.FAERS) {
            $('#evaluateDateAsNonSubmission').attr("disabled", "disabled");
            $("#evaluateDateAsNonSubmission option[value='VERSION_ASOF']").remove();
            $('#asOfVersionDatePicker').hide();
            $('#excludeFollowUp').attr("disabled", "disabled");
            $('#excludeFollowUp').prop("checked", false);
            $('#includeLockedVersion').prop("checked", false);
            $("#drugType option[value='VACCINE']").remove();
            $("#dateRangeType option[value='CASE_LOCKED_DATE']").remove();
            $("#dateRangeType option[value='SAFTEY_RECEIPT_DATE']").remove();
            $("#dateRangeType option[value='CREATION_DATE']").remove();
            $("#templateQueryList").find(".selectQuery").html('').select2({
                data: faersQueryList
            });
            $("#templateQueryList").find(".selectQuery").val('').trigger('change');

        } else {
            $('#evaluateDateAsNonSubmission').removeAttr("disabled");
            $("#evaluateDateAsNonSubmission").append('<option value="VERSION_ASOF">Version As Of</option>');
            $('#excludeFollowUp').removeAttr("disabled");
            $('#includeLockedVersion').removeAttr("disabled");
            $("#drugType").append('<option value="VACCINE">Vaccine</option>');
            $("#dateRangeType").append('<option value="CASE_LOCKED_DATE">Case Locked Date</option>');
            $("#dateRangeType").append('<option value="SAFTEY_RECEIPT_DATE">Safety Receipt Date</option>');
            $("#dateRangeType").append('<option value="CREATION_DATE">Creation Date</option>');
            $("#templateQueryList").find(".selectQuery").html('').select2({
                data: pvaQueryList
            });
            $("#templateQueryList").find(".selectQuery").val('').trigger('change');
        }
        $('#editAggregate').val("true");
    }

    $('#selectedDatasource').on('change', function () {
        var isEditAggregate = $('#editAggregate').val();
        if ((isEditAggregate != 'undefined' && isEditAggregate == "true") || $('#fromDate').length) {
            clear_all_records_and_inputs();
            close_and_add_selected_items();
        }
        if ($('#fromDate').length) {
            $('#search').prop('disabled', true);
            $("#fromDate").val('');
            $("#fromDate").find('option').not(':first').remove();
            $("#toDate").val('');
            $("#toDate").find('option').not(':first').remove();

        }
        if (this.value === dataSources.PVA) {
            $(".productGroupRadio").attr('disabled', false);
        } else {
            $(".productGroupRadio").attr('disabled', true);
        }
        $('#template').attr('disabled', true);
        $('#reportName').attr('disabled', true);
        if (this.value === dataSources.FAERS) {
            disableDictionaryValues(false, false, true, false, true);
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide();
            $('.drugClassificationContainer').show();
        } else if (this.value === dataSources.EUDRA) {
            disableDictionaryValues(true, false, true, true, true);
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide();

        } else {
            disableDictionaryValues(false, false, false, false, false);
            $(".repeat-container .dropdown-menu [data-value='hourly']").show();
            $('.drugClassificationContainer').hide();
            $('#template').attr('disabled', false);
            $('#reportName').attr('disabled', false);
        }
        toggleInterfaceChanges(this.value)
        $('#studyDrugCases').prop('disabled',true);
        $('#studyDrugCases').prop("checked",false);
        $('#additionalOptions').addClass('hidden');
        $('#ignoreMedicationType').prop('checked',false);
        $('#considerPOI').prop('checked',false);
    });


    if($('#selectedDatasource').val() == dataSources.FAERS){
        $('#selectedDatasource').trigger('change');
    }

    productSelectionModal = {
        loadProducts: load_products
    }
});

