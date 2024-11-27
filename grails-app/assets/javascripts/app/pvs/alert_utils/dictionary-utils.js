var delimiter = ";"
var WILD_SEARCH_CHARACTER = '%';

function dictionaryBindSelect2WithUrl(selector, queryUrl, data, allowClear) {
    var select2Element = selector.select2({
        dropdownParent: selector.closest(".modal"),
        minimumInputLength: 0,
        multiple: false,
        allowClear: allowClear,
        placeholder: $.i18n._("selectOne"),
        width: "100%",
        ajax: {
            delay: 500,
            type:"POST",
            dataType: "json",
            url: queryUrl,
            data: function (params) {
                return {
                    term: params.term || "",
                    page: params.page || 1,
                    max: 30
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: data.items,
                    pagination: {
                        more: (params.page * 30) < data.total_count
                    }
                };
            }
        }
    });
    if (data) {
        var option = new Option(data.text, data.id, true, true);
        selector.append(option).trigger('change.select2');
    }
    return select2Element
}

function dictionaryBindMultipleSelect2WithUrl(selector, optionsDataUrl, allowClear,placeholder, selectedDatSource) {
    return selector.select2({
        dropdownParent: selector.closest(".modal"),
        minimumInputLength: 0,
        allowClear: allowClear,
        multiple: true,
        placeholder: placeholder,
        separator: MULTIPLE_AJAX_SEPARATOR,
        ajax: {
            url: optionsDataUrl,
            type:"POST",
            dataType: 'json',
            delay: 500,
            data: function (params) {
                return {
                    term: params.term,
                    page: params.page,
                    max: 30,
                    dataSource:selectedDatSource.val() !== undefined ? selectedDatSource.val() : ASSESSMENT_DATASOURCE_PVA,
                    isMultiIngredient: $("#product_multiIngredient_assessment").is(":checked"),
                    exactSearch: $("#product_exactSearchAssessment").is(':checked')
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: data.items,
                    pagination: {
                        more: (params.page * 30) < data.total_count
                    }
                };
            }
        }
    });
}

productValues = {"1": [], "2": [], "3": [], "4": [], "5": []};

$(function () {

    $(".iconSearch").on('click', function () {
        if ($('#productModal .modal-body .dicUlFormat').length > 5) {
            $('#productModal .modal-lg').addClass('modal-xl').removeClass('modal-lg');
        }
    });

    function selectAttributeValidation() {
        var foregroundSearchAttr = $("#foregroundSearchAttr").val();
        var foregroundSearchValue = '';
        if(typeof foregroundSearchAttr !=="undefined" && foregroundSearchAttr!==""){
            foregroundSearchValue = JSON.parse(foregroundSearchAttr)
        }
        var foregroundSearch = $("#foregroundSearch").val();
        sleep(100).then(function () {
        if(foregroundSearchValue){
                for(var i =0; i<foregroundSearchValue?.length; i++)
                {
                    if(foregroundSearchValue[i]['val']!==null && foregroundSearchValue[i]['val']!=="" ){
                        var option1 = new Option(foregroundSearchValue[i]['text'],foregroundSearchValue[i]['val'],true,true);
                        $('.prodDictFilterCol').find('select[name='+foregroundSearchValue[i]['key']+']').append(option1).trigger('change');
                        $('.prodDictSearchColCalc').find('select[name='+foregroundSearchValue[i]['key']+']').append(option1).trigger('change');
                    }
                }
        }});
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

    $('#productModal').on('shown.bs.modal', function (e) {
        $("#searchAttributeError").hide();
        selectAttributeValidation(e);
        if ($('#productModal .modal-body .dicUlFormat').length > 5) {
            $('#productModal .modal-lg').addClass('modal-xl').removeClass('modal-lg');
        }
        var dmvValue= $("#dataMiningVariable").val();
        if(dmvValue!=null && dmvValue!="null"  && typeof dmvValue !=="undefined"){
            greyoutCheckbox(true);
            $("#foregroundSearch").val(false);
            $('#considerForegroundRun').prop('checked', false);
        }
    });

    var radio = $("input:radio[name=optradio]:checked");
    if (radio.length === 0) {
        $("input:radio[name=optradio].productRadio").attr("checked", "checked");
        showSections("#showProductSelection");
    } else {
        if (radio.hasClass("genericRadio")) {
            var generics = JSON.parse($("#productSelection").val())[5];
            if (generics) {
                var container = $("#showGenericSelection");
                container.html("");
                var names = [];
                _.each(generics, function (it) {
                    names.push(it.genericName);
                });
                container.append("<div style='padding: 5px'>" + names.join(",") + "</div>");
                showSections("#showGenericSelection");
            }
        } else if (radio.hasClass("productRadio")) {
            showSections("#showProductSelection");
        } else if (radio.hasClass("studyRadio")) {
            showSections("#showStudySelection");
        }
    }

    function showSections(name) {
        $("#showProductSelection").parent().parent().find(".wrapper").attr("hidden", "hidden");
        $(name).parent().removeAttr("hidden");
    }

    function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
});