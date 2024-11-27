$(function () {

    var dataSources = {"PVA": "pva", "Vigi Base": 'vigibase', "FAERS": 'faers'}
    var productValues = {"1": [], "2": [], "3": [], "4": [], "5": []};
    var init = function () {
        $("#productModalMart").find("input.searchProductsMart").on('keyup', function (e) {
            if (e.keyCode == 13) {

                $.ajax({
                    cache: false,
                    url: '/signal/pvsProductDictionary/searchProducts',
                    dataType: 'json',
                    data: {
                        productId: null,
                        contains: $(this).val(),
                        dictionaryLevel: $(this).attr('level'),
                        selectedDatasource: $("#selectedDatasourceMart").val()
                    },
                })
                    .done(function (data) {
                        clear_all_records()
                        $(data).each(function (index) {
                            clear_all_inputs_except($(this).attr('level'))
                            var containerLevel = checkDictLevelMap($(this).attr('level') , $("#selectedDatasourceMart").val())
                            $("ul[dictionarylevelMart='" + containerLevel + "']").append(
                                "<li class='dicLi' data-value='" + data[index].id + "' dictionarylevelMart='" + data[index].level + "'>" +
                                data[index].name + "</li>")
                        })
                        init_get_components()
                    })
            }
        })

        $("button.addProductValuesMart").on('click', function (event) {
            if ($('#productModalMart').hasClass('in')) {
                add_selected_items()
            }
        })

        $("button.addAllProductsMart").on("click", function (event) {
            if ($('#productModalMart').hasClass('in')) {
                close_and_add_selected_items()
            }
        })

        $("button.clearProductValuesMart").on("click", function () {
            if ($('#productModalMart').hasClass('in')) {
                clear_all_records_and_inputs()
            }
        })

        $("button.addGenericValues").on("click", function () {
            add_generic_name()
        })

        $('button.clearGenericValues').on('click', function () {
            clear_generic_name()
        });

        load_products()
    }

    var clear_all_records = function () {
        $("ul.productDictionaryColWidthMart").find('li').each(function () {
            $(this).remove()
        })
    }

    var clear_all_records_and_inputs = function () {
        $("ul.productDictionaryColWidthMart").find('li').each(function () {
            $(this).remove()
        })

        $("input.searchProductsMart").each(function () {
            $(this).val("")
        })

        $("div.productDictionaryValueMart").text("")

        productValues = {"1": [], "2": [], "3": [], "4": [], "5": []}
    }

    var clear_all_parent_records = function (level) {
        $("ul.productDictionaryColWidthMart").find('li').each(function () {
            if ($(this).attr('dictionarylevelMart') < level) {
                $(this).remove()
            }
        })
    }

    var clear_all_child_records = function (level) {
        var levelVal = parseInt(level)
        while (levelVal <= 4) {
            levelVal = levelVal + 1
            $("ul.productDictionaryColWidthMart").find("li[dictionarylevelMart=" + levelVal + "]").remove()
        }
    }

    var clear_all_inputs_except = function (level) {
        $("input.searchProductsMart").each(function () {
            if ($(this).attr('level') != level) {
                $(this).val("")
            }
        })
    }

    var init_get_components = function () {
        $("ul.productDictionaryColWidthMart").find('li').on('click', function (event) {
            var productId = $(this).attr("data-value")
            var dicLevel = $(this).attr("dictionarylevelMart")
            deselect_items()
            deselect_items_except(dicLevel)
            $(this).addClass('selected selectedBackground')
            clear_all_child_records($(this).attr('dictionarylevelMart'))
            get_selected_product(productId, dicLevel)
            get_pre_level_parents(dicLevel, productId)
        })
    }

    var get_new_components = function () {
        $("ul.productDictionaryColWidthMart").find('li').on('click', function (event) {
            var productId = $(this).attr("data-value")
            var dicLevel = $(this).attr("dictionarylevelMart")
            $(this).addClass('selected selectedBackground')
            clear_all_child_records($(this).attr('dictionarylevelMart'))
            get_selected_product(productId, dicLevel)
        })
    }

    var get_selected_product = function (productId, dicLevel) {
        var selectedDataSource = $("#selectedDatasourceMart").val()
        $.ajax({
            cache: false,
            url: '/signal/pvsProductDictionary/getSelectedProduct',
            dataType: 'json',
            data: {
                dictionaryLevel: dicLevel,
                productId: productId,
                selectedDatasource: selectedDataSource
            },
        })
            .done(function (data) {
                var nextLevelItems = data.nextLevelItems
                $.each(nextLevelItems, function (index, element) {
                    if ($("ul[dictionarylevelMart='" + checkDictLevelMap(element.level , selectedDataSource) + "'] li[data-value='" + element.id + "'").length == 0) {
                        if (nextLevelItems.length == 1) {
                            $("ul[dictionarylevelMart='" + checkDictLevelMap(element.level , selectedDataSource) + "']").append("<li class='highlighted selected dicLi' data-value='" +
                                element.id + "' dictionarylevelMart='" + element.level + "'>" +
                                element.name + "</li>")
                        } else {
                            $("ul[dictionarylevelMart='" + checkDictLevelMap(element.level , selectedDataSource) + "']").append("<li class='dicLi' data-value='" + element.id + "' dictionarylevel='" +
                                element.level + "'>" + element.name + "</li>")
                        }
                    }
                });
                select_item()
                get_new_components()
            })
    }

    var get_pre_level_parents = function (level, productId) {
        clear_all_parent_records(level)
        var productIds = ""
        if (productId == undefined) {
            $.each($("ul li[dictionarylevelMart='" + level + "']"), function () {
                productIds += $(this).attr("data-value") + ","
            })
        } else {
            productIds = productId
        }

        if (typeof productIds == "undefined") {
            productIds = ''
        }
        selectedDataSource =  $("#selectedDatasourceMart").val();
        $.ajax({
            cache: false,
            url: '/signal/pvsProductDictionary/getPreLevelProductParents',
            dataType: 'json',
            data: {
                productIds: productIds,
                dictionaryLevel: level,
                selectedDatasource: selectedDataSource
            },
        })
            .done(function (data) {
                $.each(data, function (index, element) {
                    if ($("ul[dictionarylevelMart='" + checkDictLevelMap(element.level , selectedDataSource) + "'] li[data-value='" +
                            element.id + "'").length == 0) {
                        if (data.length == 1) {
                            $("ul[dictionarylevelMart='" + checkDictLevelMap(element.level , selectedDataSource) + "']").append(
                                "<li class='highlighted selected dicLi' data-value='" + element.id +
                                "' dictionarylevelMart='" + element.level + "'>" + element.name + "</li>")
                        } else {
                            $("ul[dictionarylevelMart='" + checkDictLevelMap(element.level , selectedDataSource) + "']").append(
                                "<li data-value='" + element.id + "' dictionarylevelMart='" + element.level + "'>" +
                                element.name + "</li>")
                        }
                    }

                    if (level > 1 && $(".selectedDatasource").val() != dataSources.FAERS) {
                        get_pre_level_parents(level - 1)
                    }
                });

                select_item()
            })
    }

    var add_generic_name = function () {
        var generic_names = []
        $.each($("#genericModalMart").find("li.select2-search-choice div"), function (index, element) {
            var selectedObj = {"genericName": $(this).text()}
            generic_names.push($(this).text())
            productValues[5].push(selectedObj)
        })

        $("#productSelectionMart").val(JSON.stringify(productValues))
        $("#showGenericSelectionMart").html("")
        $("#showGenericSelectionMart").append("<div style='padding: 5px'>" + generic_names + "</div>")
    }

    var clear_generic_name = function () {
        $("#js-data-genericsMart").val('');
    }

    var add_selected_items = function () {
        var isInvalidSelection = false;
        var productModal = $("#productModalMart")
        var id = productModal.find("ul li.selectedBackground").attr("data-value")
        var name = productModal.find("ul li.selectedBackground").text()
        var level = productModal.find("ul li.selectedBackground").attr("dictionarylevelMart")

        var oldValue = productModal.find("div.level" + level).text();
        if (oldValue) {
            oldValue = oldValue + ", ";
        }

        var selectedValues = oldValue.split(',')
        selectedValues = _.map(selectedValues, function (v) {
            return v.trim().toLowerCase()
        })

        if (!_.contains(selectedValues, name.trim().toLocaleLowerCase())) {
            if (level) {
                $.each(productValues, function (key, value) {
                    if (key != level) {
                        if (productValues[key].length > 0) {
                            isInvalidSelection = true
                        }
                    }
                });
                var selectedObj = {"name": name, "id": id};
                if (isInvalidSelection) {
                    showErrorMsgInDialog("Invalid Selection");
                } else {
                    $("div.selectedProductDictionaryValueMart").find("div.level" + level).text(oldValue + " " + name);
                    productValues[level].push(selectedObj)
                }
            }
        }
    }

    var close_and_add_selected_items = function () {
        var productList = []
        $.each(productValues, function (index, element) {
            $.each(element, function (indx, elem) {
                productList.push(elem.name)
            })
        })

        if (!$.isEmptyObject(productList)) {
            $("#productSelectionMart").val(JSON.stringify(productValues))
        } else {
            $("#productSelectionMart").val("")
        }

        $("#showProductSelectionMart").html("")
        $("#showProductSelectionMart").append("<div style='padding: 5px'>" + productList + "</div>")
    }

    var load_products = function () {
        var all_products = $("#productSelectionMart").val()

        if ($("#productSelectionMart").val() != "") {
            $("#showProductSelectionMart").html("")
            $("#showGenericSelection").html("")
            $.each(JSON.parse(all_products), function (index, element) {
                if (index == 5) {
                    var generic_name_to_show = []
                    if (element && element.length > 0) {
                        $.each(element, function (idx, elem) {
                            generic_name_to_show.push(elem.genericName)
                            productValues[index].push(elem)
                            $("#genericModal").find(".select2-search-field").html(
                                '<li class="select2-search-choice">    <div>' +
                                elem.genericName +
                                '</div>    <a href="#" class="select2-search-choice-close" tabindex="-1"></a></li>')
                        })
                        $("#showGenericSelection").append("<div style='padding: 5px'>" + generic_name_to_show + "</div>")
                        showGenericSelectionTextArea()
                    }
                } else if (index == 3 || index == 2 || index == 1 || index == 4) {
                    var prod_to_show = []
                    if (element && element.length > 0) {
                        $.each(element, function (idx, elem) {
                            prod_to_show.push(elem.name)
                            productValues[index].push(elem)
                        })
                        $("#showProductSelectionMart").append("<div style='padding: 2px'>" + prod_to_show + "</div>")
                        $("div.selectedProductDictionaryValueMart").find("div.level" + index).text(prod_to_show)
                        showProductSelectionTextArea()
                    }
                }
            })
        }
    }

    var deselect_items = function () {
        $("ul.productDictionaryColWidthMart").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted')
    }

    var deselect_items_except = function (level) {
        $("ul.productDictionaryColWidthMart").find('li.selected').each(function () {
            if ($(this).attr('dictionarylevelMart') == level) {
                $(this).removeClass("highlighted selected")
            }
        })
    }

    var select_item = function () {
        $("ul.productDictionaryColWidthMart").find('li').on('click', function (event) {
            $("ul.productDictionaryColWidthMart").find('li.selectedBackground').removeClass('selectedBackground').addClass('highlighted')
            $(this).addClass('selected selectedBackground')
        })
    }

    var showProductSelectionTextArea = function () {
        $("#showProductSelectionMart").closest('div.wrapper').removeAttr("hidden");
        $("#showStudySelection").closest('div.wrapper').attr('hidden', 'hidden')
        $("#showGenericSelection").closest('div.wrapper').attr("hidden", "hidden");
    }

    var showStudySelectionTextArea = function () {
        $(document.getElementById("showStudySelection").parentElement).removeAttr("hidden");
        $(document.getElementById("showProductSelectionMart").parentElement).attr("hidden", "hidden");
        $(document.getElementById("showGenericSelection").parentElement).attr("hidden", "hidden");
    }

    var showGenericSelectionTextArea = function () {
        $(document.getElementById("showGenericSelection").parentElement).removeAttr("hidden");
        $(document.getElementById("showProductSelectionMart").parentElement).attr("hidden", "hidden");
        $(document.getElementById("showStudySelection").parentElement).attr("hidden", "hidden");
    }

    init()

    $('#selectedDatasourceMart').on('change', function () {
        clear_all_records_and_inputs()
        close_and_add_selected_items()
        if (this.value === dataSources.FAERS) {
            disableDictionaryValues(false, false, true, false, true);
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide()
        } else {
            disableDictionaryValues(false, false, false, false, true);
            $(".repeat-container .dropdown-menu [data-value='hourly']").show()
        }
    })

    $(".prodDictFilterColCalcMart label:contains('Trade Name')").next("input").prop("disabled", true);
})