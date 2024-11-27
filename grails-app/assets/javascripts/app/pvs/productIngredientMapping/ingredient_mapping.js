//= require app/pvs/alert_utils/common_key_prevent.js
var productSelectionModal = {};
var pvaQueryList;
var faersQueryList;
var productValuesIngredient = {"1": [], "2": [], "3": [], "4": [], "5": []};
var dicLevelMap = {};
var EVENT_DICTIONARY = "event";
var PRODUCT_DICTIONARY = "product";
var STUDY_DICTIONARY = "study";
$(function () {

    $(".iconSearch").on('click', function () {
        if ($('#productModalIngredient .modal-body .dicUlFormat').length > 5) {
            $('#productModalIngredient .modal-lg').addClass('modal-xl').removeClass('modal-lg');
        }
    });

    $('#productModalIngredient').on('shown.bs.modal', function (e) {
        if ($('#productModalIngredient .modal-body .dicUlFormat').length > 5) {
            $('#productModalIngredient .modal-lg').addClass('modal-xl').removeClass('modal-lg');
        }
    });

    getHierarchyMap();
    checkMultipleHierarchy();
    var dataSources = {"PVA": "pva", "Vigi Base": 'vigibase', "FAERS": 'faers', 'EUDRA': 'eudra'};
    var init = function () {
        $("#productModalIngredient").find("input.productValuesIngredient").on('keyup', function (e) {
            if (e.keyCode == 13) {
                $("#fetchingProducts").show();
                var url;
                var dataSource = $("#selectedDatasourceIngredient").val();
                var data = {};
                if (dataSource == "eudra") {
                    data = {
                        productId: null,
                        contains: $(this).val(),
                        dictionaryLevel: $(this).attr('level'),
                        selectedDatasource: dataSource
                    }
                    url = '/signal/pvsProductDictionary/searchProducts';
                } else {
                    data = {
                        contains: $(this).val(),
                        dictionaryLevel: $(this).attr('level'),
                        ref_level: null,
                        exact_search: false,
                        imp: false,
                        dataSource: dataSource,
                        filters: {}
                    }
                    url = '/signal/productDictionary/searchViews';
                }
                $.ajax({
                    cache: false,
                    url: url,
                    dataType: 'json',
                    data: data,
                })
                    .done(function (data) {
                        clear_all_records();
                        $(data).each(function (index) {
                            clear_all_inputs_except($(this).attr('level'));
                            var containerLevel = dicLevelMap[$(this).attr('level')];
                            $("ul.column-ingredient[dictionarylevel='" + containerLevel + "']").append(
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

        $("button.addProductValuesIngredient").on('click', function (event) {
            if ($('#productModalIngredient').hasClass('in')) {
                add_selected_items()
            }
        });

        $("button.addAllProductsIngredient").on("click", function (event) {
            if ($('#productModalIngredient').hasClass('in')) {
                close_and_add_selected_items()
            }
        });

        $("button.clearProductValuesIngredient").on("click", function () {
            if ($('#productModalIngredient').hasClass('in')) {
                clear_all_records_and_inputs()
            }
        });

        $("button.addGenericValuesIngredient").on("click", function () {
            add_generic_name()
        });

        $('button.clearGenericValuesIngredient').on('click', function () {
            clear_generic_name()
        });
        load_products()
    };

    var clear_all_records = function () {
        $("ul.column-ingredient").find('li').each(function () {
            $(this).remove()
        })
    };

    var clear_all_records_and_inputs = function () {

        $("ul.column-ingredient").find('li').each(function () {
            $(this).remove()
        });

        $("input.productValuesIngredient").each(function () {
            $(this).val("")
        });

        $("div.productDictionaryValue").text("");

        productValuesIngredient = {"1": [], "2": [], "3": [], "4": [], "5": []}

    };

    var clear_all_parent_records = function (level) {
        $("ul.column-ingredient").find('li').each(function () {
            if ($(this).attr('dictionarylevel') < level) {
                $(this).remove()
            }
        })
    };

    var clear_all_child_records = function (level) {
        var levelVal = parseInt(level);
        while (levelVal <= 4) {
            levelVal = levelVal + 1;
            $("ul.column-ingredient").find("li[dictionarylevel=" + levelVal + "]").remove()
        }
    };

    var clear_all_inputs_except = function (level) {
        $("input.productValuesIngredient").each(function () {
            if ($(this).attr('level') != level) {
                $(this).val("")
            }
        })
    };

    var init_get_components = function () {
        $("ul.column-ingredient").find('li').on('click', function (event) {
            event.stopPropagation()
            var productId = $(this).attr("data-value");
            var dicLevel = $(this).attr("dictionarylevel");
            deselect_items();
            deselect_items_except(dicLevel);
            $(this).addClass('selected selectedBackground');
            clear_all_child_records($(this).attr('dictionarylevel'));
            var dataSource = $("#selectedDatasourceIngredient").val();
            if (dataSource == "eudra") {
                return;
            }
            get_selected_product(productId, dicLevel);
            get_pre_level_parents(dicLevel, productId);
        })
    };

    var get_new_components = function (elemId) {
        $("ul.column-ingredient").find("li[data-value=" + elemId + "]").on('click', function (event) {
            var productId = $(this).attr("data-value");
            var dicLevel = $(this).attr("dictionarylevel");
            $(this).addClass('selected selectedBackground');
            clear_all_child_records($(this).attr('dictionarylevel'));
            get_selected_product(productId, dicLevel)
        })
    };

    var get_selected_product = function (productId, dicLevel) {
        $("#fetchingProducts").show();
        var dataSource = $("#selectedDatasourceIngredient").val();
        var data = {};
        var url;
        if (dataSource == "eudra") {
            return
        } else {
            data = {
                productId: productId,
                dataSource: dataSource,
                dictionaryLevel: dicLevel,
                currentLang: "en"
            }
            url = '/signal/productDictionary/getSelectedItem';
        }
        ;
        $.ajax({
            cache: false,
            url: url,
            data: data,
        })
            .done(function (data) {
                var nextLevelItems = data.nextLevelItems;
                $.each(nextLevelItems, function (index, element) {
                    if ($("ul.column-ingredient[dictionarylevel='" + dicLevelMap[element.level] + "'] li[data-value='" + element.id + "'").length == 0) {
                        if (nextLevelItems.length == 1) {
                            $("ul.column-ingredient[dictionarylevel='" + dicLevelMap[element.level] + "']").append("<li tabindex='0' role='button' class='highlighted selected dicLi' data-value='" +
                                element.id + "' dictionarylevel='" + element.level + "'>" +
                                element.name + "</li>")
                        } else {
                            $("ul.column-ingredient[dictionarylevel='" + dicLevelMap[element.level] + "']").append("<li tabindex='0' role='button' class='dicLi' data-value='" + element.id + "' dictionarylevel='" +
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
        var dataSource = $("#selectedDatasourceIngredient").val();
        var data = {};
        var url;
        if (dataSource == "eudra") {
            return
        } else {
            data = {
                dataSource: dataSource,
                dictionaryLevel: level,
                currentLang: "en",
                productIds: productIds
            }
            url = '/signal/productDictionary/getPreLevelProductParents';
        }
        $.ajax({
            cache: false,
            url: url,
            data: data,
        })
            .done(function (data) {
                $.each(data, function (index, element) {
                    if ($("ul.column-ingredient[dictionarylevel='" + dicLevelMap[element.level] + "'] li[data-value='" + element.id + "'").length == 0) {
                        if (data.length == 1) {
                            $("ul.column-ingredient[dictionarylevel='" + dicLevelMap[element.level] + "']").append(
                                "<li tabindex='0' role='button' class='highlighted selected dicLi' data-value='" + element.id +
                                "' dictionarylevel='" + element.level + "'>" + element.name + "</li>")
                        } else {
                            $("ul.column-ingredient[dictionarylevel='" + dicLevelMap[element.level] + "']").append(
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
        productValuesIngredient[5] = [];
        $.each($("#genericModal").find("li.select2-search-choice div"), function (index, element) {
            var selectedObj = {"genericName": $(this).text()};
            generic_names.push($(this).text());
            productValuesIngredient[5].push(selectedObj)
        });
        if (generic_names.length > 0) {
            $("#productSelection").val(JSON.stringify(productValuesIngredient));
            $("#showGenericSelection").html("");
            $("#showGenericSelection").append("<div style='padding: 5px'>" + generic_names + "</div>")
        }
    };

    var clear_generic_name = function () {
        $("#js-data-generics").val('');
    };

    var add_selected_items = function () {
        var productModal = $("#productModalIngredient");
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
            $("div.selectedProductDictionaryValueIngredient").find("div.level" + level).text(oldValue + " " + name)
            if (level) {
                var selectedObj = {"name": name, "id": id};
                productValuesIngredient[level].push(selectedObj)
            }
        }
    };

    var close_and_add_selected_items = function () {
        var productList = [];
        $.each(productValuesIngredient, function (index, element) {
            if (index != 5) {
                $.each(element, function (indx, elem) {
                    productList.push(elem.name)
                });
            }
        });
        if (!$.isEmptyObject(productList)) {
            $("#productSelectionIngredient").val(JSON.stringify(productValuesIngredient))
        } else {
            $("#productSelectionIngredient").val("")
        }

        $("#showProductSelectionIngredient").html("");
        $("#showProductSelectionIngredient").append("<div style='padding: 5px'>" + productList + "</div>");

    };

    var load_products = function () {
        var all_products = $("#productSelectionIngredient").val();
        if ($("#productSelectionIngredient").val() != "") {
            $("#showProductSelectionIngredient").html("");
            $.each(JSON.parse(all_products), function (index, element) {
                if (index == 5) {
                    var generic_name_to_show = [];
                    if (element && element.length > 0) {
                        $.each(element, function (idx, elem) {
                            generic_name_to_show.push(elem.genericName);
                            productValuesIngredient[index].push(elem);
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
                            productValuesIngredient[index].push(elem)
                        });
                        $("#showProductSelectionIngredient").append("<div style='padding: 2px'>" + prod_to_show + "</div>");
                        $("div.selectedProductDictionaryValueIngredient").find("div.level" + index).text(prod_to_show);
                        showProductSelectionTextArea()
                    }
                }
            })
        }
    };


    var deselect_items = function () {
        $("ul.column-ingredient").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted')
    };

    var deselect_items_except = function (level) {
        $("ul.column-ingredient").find('li.selected').each(function () {
            if ($(this).attr('dictionarylevel') == level) {
                $(this).removeClass("highlighted selected")
            }
        })
    };

    var select_item = function () {
        $("ul.column-ingredient").find('li').on('click', function (event) {
            $("ul.column-ingredient").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted');
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
            $('#includeLockedVersion').attr("disabled", "disabled");
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

    $('#selectedDatasourceIngredient').on('change', function () {
        clear_all_records_and_inputs();
        close_and_add_selected_items();
        $(".productDictionaryValueIngredient").html("")
        var isEditAggregate = $('#editAggregate').val();
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
            $(".ingredientCol label:contains('Family')").next("input").prop("disabled", true)
            $(".ingredientCol label:contains('Product Group')").next("input").prop("disabled", false)
            $(".ingredientCol label:contains('Product Name')").next("input").prop("disabled", false)
            $(".ingredientCol label:contains('Trade Name')").next("input").prop("disabled", true)
            $(".ingredientCol label:contains('Ingredients')").next("input").prop("disabled", false)
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide();
            $('.drugClassificationContainer').show();
        } else if (this.value === dataSources.EUDRA) {
            $(".ingredientCol label:contains('Family')").next("input").prop("disabled", true)
            $(".ingredientCol label:contains('Product Group')").next("input").prop("disabled", true)
            $(".ingredientCol label:contains('Product Name')").next("input").prop("disabled", true)
            $(".ingredientCol label:contains('Trade Name')").next("input").prop("disabled", true)
            $(".ingredientCol label:contains('Ingredients')").next("input").prop("disabled", false)
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide();

        } else {
            $(".ingredientCol label:contains('Family')").next("input").prop("disabled", false)
            $(".ingredientCol label:contains('Product Group')").next("input").prop("disabled", false)
            $(".ingredientCol label:contains('Product Name')").next("input").prop("disabled", false)
            $(".ingredientCol label:contains('Trade Name')").next("input").prop("disabled", false)
            $(".ingredientCol label:contains('Ingredients')").next("input").prop("disabled", false)
            $(".repeat-container .dropdown-menu [data-value='hourly']").show();
            $('.drugClassificationContainer').hide();
            $('#template').attr('disabled', false);
            $('#reportName').attr('disabled', false);
        }
        toggleInterfaceChanges(this.value)
    });

    if ($('#selectedDatasource').val() == dataSources.FAERS) {
        $('#selectedDatasource').trigger('change');
    }

    productSelectionModal = {
        loadProducts: load_products
    }
    disableHeirarchy()
});

function getHierarchyMap() {
    $.ajax({
        type: "GET",
        url: '/signal/pvsProductDictionary/fetchDictionaryList',
        dataType: "json",
    })
        .done(function (result) {
           dicLevelMap = result;
        });
}

function disableHeirarchy() {
    var dataSource = $('#selectedDatasourceIngredient').val();
    if (dataSource == "eudra") {
        $(".ingredientCol label:contains('Family')").next("input").prop("disabled", true);
        $(".ingredientCol label:contains('Product Group')").next("input").prop("disabled", true);
        $(".ingredientCol label:contains('Product Name')").next("input").prop("disabled", true);
        $(".ingredientCol label:contains('Trade Name')").next("input").prop("disabled", true);

        $(".repeat-container .dropdown-menu [data-value='hourly']").hide();
    }
}

function addColumnClass() {
    $("#columnView1Ingredient .productDictionaryColWidthCalc").addClass("column-ingredient")
}



