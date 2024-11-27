var product_values = {"1": []};

$(function () {
    var productModal = $("#productModal");
    var init = function () {
        get_all_products();

        show_products_edit_page();

        productModal.find(".clearProductValues").on("click", function (event) {
            clean_all_data()
        });

        productModal.find(".addProductValues").on("click", function (event) {
            add_data()
        });

        productModal.find(".addAllProducts").on("click", function (event) {
            show_products()
        })
    };

    $("input:radio[name=optradio]").on('click', function () {
        if ($(this).hasClass("productRadio")) {
            showProductSelectionTextArea();
            $("limitToSuspectProduct").prop("checked", true);
            $("limitToSuspectProduct").prop("disabled", false);
            $("limitTosuspectProduct").val(true);
        } else if ($(this).hasClass("studyRadio")) {
            showStudySelectionTextArea();
            $("limitToSuspectProduct").prop("checked", false);
            $("limitToSuspectProduct").prop("disabled", true);
            $("limitTosuspectProduct").val(false);
        } else if ($(this).hasClass("genericRadio")) {
            showGenericSelectionTextArea();
        }
    });

    var showProductSelectionTextArea = function () {
        $($("#showProductSelection").parent()).removeAttr("hidden");
        $($("#showStudySelection").parent()).attr("hidden", "hidden");
        $($("#showGenericSelection").parent()).attr("hidden", "hidden");
    };

    var showStudySelectionTextArea = function () {
        $($("#showStudySelection").parent()).removeAttr("hidden");
        $($("#showProductSelection").parent()).attr("hidden", "hidden");
        $($("#showGenericSelection").parent()).attr("hidden", "hidden");
    };

    function showGenericSelectionTextArea() {
        $($("#showGenericSelection").parent()).removeAttr("hidden");
        $($("#showProductSelection").parent()).attr("hidden", "hidden");
        $($("#showStudySelection").parent()).attr("hidden", "hidden");
    }

    if ($(".genericRadio").length > 0) {
        $("#js-data-generics").select2({
            placeholder: "Search for Generic Names",
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

    }

    var get_all_products = function () {
        productModal.find("input[level='1']").on('keyup', function (e) {
            if (e.keyCode == 13) {
                clean_search_inputs(1)
                $.ajax({
                    url: '/signal/configurationRest/searchProducts',
                    data: {
                        dictionaryLevel: $(this).attr('level'),
                        contains: $(this).val()
                    },
                })
                    .done(function (data) {
                        $(data).each(function (index) {
                            $("ul[dictionarylevel='Ingredient']").append("<li data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                            $("li[data-value='" + data[index].id + "']").on('click', function (event) {
                                clear_search_results(1)
                                $(this).addClass('selected selectedBackground')
                                fill_pre_level_family(data[index].id, data[index].level, false)
                            })
                        });
                    })
                    .fail(function () {})
            }
        })

        productModal.find("input[level='2']").on('keyup', function (e) {
            if (e.keyCode == 13) {
                clean_search_inputs(2)
                $.ajax({
                    url: '/signal/configurationRest/searchProducts',
                    data: {
                        dictionaryLevel: $(this).attr('level'),
                        contains: $(this).val()
                    },
                })
                    .done(function (data) {
                        $(data).each(function (index) {
                            $("ul[dictionarylevel='Family']").append("<li data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                            $("li[data-value='" + data[index].id + "']").on('click', function (event) {
                                clear_search_results(2)
                                $(this).addClass('selected selectedBackground')
                                fill_pre_level_ingredient(data[index].id, data[index].level, "parent")
                                fill_pre_level_product(data[index].id, data[index].level, false)
                            })
                        });
                    })
                    .fail(function () {})
            }
        });

        productModal.find("input[level='3']").on('keyup', function (e) {
            if (e.keyCode == 13) {
                clean_search_inputs(3)
                $.ajax({
                    url: '/signal/configurationRest/searchProducts',
                    data: {
                        dictionaryLevel: $(this).attr('level'),
                        contains: $(this).val()
                    }
                })
                    .done(function (data) {
                        $(data).each(function (index) {
                            $("ul[dictionarylevel='Product Name']").append("<li data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                            $("li[data-value='" + data[index].id + "']").on('click', function (event) {
                                clear_search_results(3)
                                $(this).addClass('selected selectedBackground')
                                fill_pre_level_trade_names(data[index].id, data[index].level)
                                fill_pre_level_family(data[index].id, data[index].level, true, "parent")
                            })
                        });
                    })
                    .fail(function () {})
            }
        });

        productModal.find("input[level='4']").on('keyup', function (e) {
            if (e.keyCode == 13) {
                clean_search_inputs(4)
                $.ajax({
                    url: '/signal/configurationRest/searchProducts',
                    data: {
                        dictionaryLevel: $(this).attr('level'),
                        contains: $(this).val()
                    },
                })
                    .done(function (data) {
                        $(data).each(function (index) {
                            $("ul[dictionarylevel='Trade Name']").append("<li data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                            $("li[data-value='" + data[index].id + "']").on('click', function (event) {
                                clear_search_results(4)
                                $(this).addClass('selected selectedBackground')
                                fill_pre_level_product(data[index].id, data[index].level, true, "parent")
                            })
                        })
                    })
                    .fail(function () {})
            }
        })
    };

    var fill_pre_level_trade_names = function (id, dicLevel, isChain) {

        $.ajax({
            url: '/signal/configurationRest/getGenericProduct',
            data: {
                dictionaryLevel: dicLevel,
                productId: id
            },
        })
            .done(function (data) {
                $(data).each(function (index) {
                    $("ul[dictionarylevel='Trade Name']").append("<li class='highlighted selected' data-value='" +
                        data[index].id + "' dictionarylevel='" + data[index].level + "'>" +
                        data[index].name + "</li>")
                });
        })

    }

    var fill_pre_level_family = function (id, dicLevel, isChain, parent) {
        $.ajax({
            url: '/signal/configurationRest/getGenericProduct',
            data: {
                dictionaryLevel: dicLevel,
                productId: id,
                parent: parent
            },
        })
            .done(function (data) {
                $(data).each(function (index) {
                    $("ul[dictionarylevel='Family']").append("<li data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                    $("li[data-value='" + data[index].id + "']").on('click', function (event) {
                        clear_search_results(2)
                        $(this).addClass('selected selectedBackground')
                        fill_pre_level_product(data[index].id, data[index].level, false)
                        //fill_pre_level_ingredient(data[index].id, data[index].level, "parent")
                    })
                    if (isChain) {
                        if (data.length == 1) {
                            $("li[data-value='" + data[index].id + "']").addClass('selected highlighted')
                        }
                        fill_pre_level_ingredient(data[index].id, data[index].level, "parent")
                    }
                });
        })
    }

    var fill_pre_level_ingredient = function (id, dicLevel, parent) {
        $.ajax({
            url: '/signal/configurationRest/getGenericProduct',
            data: {
                dictionaryLevel: dicLevel,
                productId: id,
                parent: parent
            },
        })
            .done(function (data) {
                $(data).each(function (index) {
                    if (data.length == 1) {
                        $("ul[dictionarylevel='Ingredient']").append("<li class='highlighted selected' data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                    } else {
                        $("ul[dictionarylevel='Ingredient']").append("<li data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                    }
                });
        })
    }

    var fill_pre_level_product = function (id, dicLevel, isChain, parent) {
        $.ajax({
            url: '/signal/configurationRest/getGenericProduct',
            data: {
                dictionaryLevel: dicLevel,
                productId: id,
                parent: parent
            },
        })
            .done(function (data) {
                $(data).each(function (index) {
                    $("ul[dictionarylevel='Product Name']").append("<li data-value='" + data[index].id + "' dictionarylevel='" + data[index].level + "'>" + data[index].name + "</li>")
                    $("li[data-value='" + data[index].id + "']").on('click', function (event) {
                        clear_search_results(3)
                        $(this).addClass('selected selectedBackground')
                        fill_pre_level_trade_names(data[index].id, data[index].level, false)
                        //fill_pre_level_family(data[index].id, data[index].level, true, "parent")
                    })
                    if (isChain) {
                        if (data.length == 1) {
                            $("li[data-value='" + data[index].id + "']").addClass('selected highlighted')
                        }
                        fill_pre_level_family(data[index].id, data[index].level, true, "parent")
                    }
                });
        })
    }

    var clean_search_inputs = function (exceptElemLevel) {
        clean_all_search_results()
        productModal.find('input').each(function () {
            if ($(this).attr("level") != exceptElemLevel) {
                $(this).val("")
            }
        })
    };

    var clean_all_search_results = function () {
        $("#carriage").find("ul li").remove()
    };

    var clean_all_data = function () {
        var product_values = {"1": []}
        clean_all_search_results()
        productModal.find(".level3").text("")
        productModal.find('input').each(function () {
            $(this).val("")
        })
    };

    var clear_search_results = function (exceptDicLevel) {
        $("#carriage").find("ul li").each(function () {
            if ($(this).attr("dictionarylevel") != exceptDicLevel) {
                $(this).remove()
            }
        })
    };

    var add_data = function () {
        var selectedObj = {
            "name": productModal.find("li.selected[dictionarylevel='3']").text(),
            "id": productModal.find("li.selected[dictionarylevel='3']").attr('data-value')
        };
        product_values[1].push(selectedObj)
        var oldValue = productModal.find(".level3").text();
        if (oldValue) {
            oldValue = oldValue + ", ";
        }
        productModal.find(".level3").text(oldValue + selectedObj.name + " (" + selectedObj.id + ")");
    };

    var show_products = function () {
        var productLevels = {"1": "Ingredient", "2": "Family", "3": "Product Name", "4": "Trade Name"};
        $("#showProductSelection").html("");
        $("#showProductSelection").append("<div style='padding: 5px'>" + productModal.find(".level3").text() + "</div>");
        //showDictionaryValues($("#showProductSelection"), productValues, productLevels);
        $("#productSelection").val(checkEmptyValues(product_values))
        if (typeof $("#product_multiIngredient") != "undefined" && $("#product_multiIngredient").is(':checked')) {
            $("#isMultiIngredient").val(true)
        } else {
            $("#isMultiIngredient").val(false)
        }
    };

    var show_products_edit_page = function () {
        var prodSel = $("#productSelection").val();
        if (prodSel) {
            var names = [];
            $.each(JSON.parse(prodSel), function (index, value) {
                //Check for product names
                if (index < 5 && value.length > 0 && index < 5) {
                    $.each(value, function (index, element) {
                        names.push(element.name)
                    });
                    $("#showProductSelection").append("<div style='padding: 5px'>" + names + "</div>")
                } else {
                    names = [];
                    $.each(value, function (index, element) {
                        names.push(element.name)
                    });
                    $("#showGenericSelection").append("<div style='padding: 5px'>" + names + "</div>")
                }
            })
        }
    };

    var checkEmptyValues = function (values) {
        var result = '';
        var emptyList = true;
        for (var key in values) {
            if (values.hasOwnProperty(key)) {
                if (values[key].length > 0) {
                    emptyList = false;
                }
            }
        }
        if (!emptyList) {
            result = JSON.stringify(values);
        }
        return result;
    };

    $('#myDatePicker').datepicker({
        date: $("#myDatePicker").val() ? new Date($("#myDatePicker").val()) : null,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    init()
});

$(window).keydown(function (event) {
    if (event.keyCode == 13) {
        event.preventDefault();
        return false;
    }
});