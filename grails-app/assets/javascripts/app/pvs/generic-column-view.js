var EVENT_DICTIONARY = "event";
var PRODUCT_DICTIONARY = "product";
var STUDY_DICTIONARY = "study";
var GENERIC_DICTIONARY = "generic";
var GENERIC_LEVEL = 5;
var eventValues = {"1":[], "2":[], "3":[], "4":[], "5":[], "6":[]};
var productValues = {"1":[], "2":[], "3":[], "4":[], "5":[]};
var studyValues = {"1":[], "2":[], "3":[]};
var selectedDictionaryValue = {};

function source(ID, cb, level, dictionaryType, isClick) {
    // show child events in next column
    if (ID) {
        // get "level" from li -> 1, 2, 3, 4, 5, 6
        var getSelectedUrl = getSelectedEventUrl + "?eventId=" + ID + "&dictionaryLevel=" + level;
        if (dictionaryType == PRODUCT_DICTIONARY) {
            getSelectedUrl = getSelectedProductUrl + "?productId=" + ID + "&dictionaryLevel=" + level;
        } else if (dictionaryType == STUDY_DICTIONARY) {
            getSelectedUrl = getSelectedStudyUrl + "?studyId=" + ID + "&dictionaryLevel=" + level;
        }

        $.ajax({
            type: "GET",
            url: getSelectedUrl,
            dataType: "json"
        })
            .done(function (result) {
                if (isClick) {
                    selectedDictionaryValue.id = result.id;
                    selectedDictionaryValue.name = result.name;
                    selectedDictionaryValue.level = level;
                    selectedDictionaryValue.genericName = result.genericName;
                }
                if (result.nextLevelItems.length) {
                    cb({items: result.nextLevelItems}, level);
            }
        });
    } else if (level) {
        // get "level" from ul -> SOC, HLGT, HLT, PT, LLT

    }
}

function sourceParents(IDs, level, cb, dictionaryType) {
    var getParentsUrl = getPreLevelEventParentsUrl + "?eventIds=" + IDs + "&dictionaryLevel=" + level;
    if (dictionaryType == PRODUCT_DICTIONARY) {
        getParentsUrl = getPreLevelProductParentsUrl + "?productIds=" + IDs + "&dictionaryLevel=" + level;
    } else if (dictionaryType == STUDY_DICTIONARY) {
        getParentsUrl = getPreLevelStudyParentsUrl + "?studyIds=" + IDs + "&dictionaryLevel=" + level;
    }
    if (IDs) {
        $.ajax({
            type: "GET",
            url: getParentsUrl,
            dataType: "json"
        })
            .done(function (result) {
                if (result.length > 0) {
                    cb({items: result}, level);
                }
        });
    }
}

function forSearch(searchTerm, level, cb, dictionaryType) {
    var searchUrl = searchEventsUrl + "?contains=" + searchTerm + "&dictionaryLevel=" + level;
    if (dictionaryType == PRODUCT_DICTIONARY) {
        searchUrl = searchProductsUrl + "?contains=" + searchTerm + "&dictionaryLevel=" + level;
    } else if (dictionaryType == STUDY_DICTIONARY) {
        searchUrl = searchStudiesUrl + "?contains=" + searchTerm + "&dictionaryLevel=" + level;
    }
    // show result in correct column
    $.ajax({
        type: "GET",
        url: searchUrl,
        dataType: "json"
    })
        .done(function (result) {
            cb({items: result}, level);
    });
}

function clearSearchInputs(exceptIndex, dictionaryType) {
    var inputClass = "searchEvents";
    if (dictionaryType == PRODUCT_DICTIONARY) {
        inputClass = "searchProducts"
    } else if (dictionaryType == STUDY_DICTIONARY) {
        inputClass = "searchStudies";
    }
     _.each(document.getElementsByClassName(inputClass), function (input, index) {
        if (index != exceptIndex) {
            input.value = "";
        }
    });
}

function clearAllText(dictionaryType) {
    var dictionaryValues = document.getElementsByClassName("eventDictionaryValue");

    if (dictionaryType == PRODUCT_DICTIONARY) {
        dictionaryValues = document.getElementsByClassName("productDictionaryValue");
    } else if (dictionaryType == STUDY_DICTIONARY) {
        dictionaryValues = document.getElementsByClassName("studyDictionaryValue");
    }
    _.each(dictionaryValues, function (div) {
        div.innerHTML = "";
    })
}

function clearShowValues(){

}

function resetDictionaryList(dictionaryType) {
    if (dictionaryType == EVENT_DICTIONARY) {
        eventValues = {"1":[], "2":[], "3":[], "4":[], "5":[], "6":[]};
    } else if (dictionaryType == PRODUCT_DICTIONARY || dictionaryType == GENERIC_DICTIONARY) {
        productValues = {"1":[], "2":[], "3":[], "4":[], "5":[]};
    } else if (dictionaryType == STUDY_DICTIONARY) {
        studyValues = {"1":[], "2":[], "3":[]};
    }
}

function generateIdList(data) {
    return _.map(data.items, function (item) {
        return item.id.toString();
    });
}

function generateColList(data) {
    return _.map(data, function (item) {
        return item.getAttribute("data-value");
    });
}

function collectNextCol(list) {
    var data = {};
    var dataList = [];

    _.each(list, function (li) {
        var obj = {id: li.getAttribute("data-value"), name: li.innerText, level: li.getAttribute("dictionaryLevel")};
        dataList.push(obj);
    });
    data.items = dataList;
    return data;
}

function generateNewDataForCol(data, col) {
    var childrenIdList = generateIdList(data);
    var nextColList = generateColList(col.querySelectorAll("li"));
    var existingChildren = _.intersection(childrenIdList, nextColList);
    var newData = collectNextCol(col.querySelectorAll("li"));
    if (!existingChildren) {
        newData = data; // use same data
        newData.selectedPath = col.querySelector(".highlighted").getAttribute("data-value");
    } else {
        newData.selectedPath = existingChildren[0];
    }
    newData.highlightedValue = col.querySelector(".highlighted").getAttribute("data-value");
    return newData;
}

function setProductEventDictionary() {
    var checkedDic = $('input[name=optradio]:checked')[0];
    if ($(checkedDic).hasClass("productRadio")) {
        $("#studySelection").val("");
    } else {
        $("#productSelection").val("");
    }
}

function addDictionary(dicValues, dicType) {
    if (selectedDictionaryValue && !isDuplicate(dicValues)) {
        setDictionaryValues(dicType);
        setDictionaryLevelText(dicType, selectedDictionaryValue.level, selectedDictionaryValue);
        selectedDictionaryValue = {};
    }
}

function isDuplicate(dictionaryValues) {
    var duplicate = false;
    var key = selectedDictionaryValue.level;
    _.each(dictionaryValues[key], function (v) {
        if (v.id == selectedDictionaryValue.id) {
            duplicate = true;
        }
    });
    return duplicate;
}

function setDictionaryValues(dictionaryType) {
    if (!_.isEmpty(selectedDictionaryValue)) {
        var selectedObj = {"name":selectedDictionaryValue.name, "id":selectedDictionaryValue.id, "genericName":selectedDictionaryValue.genericName};
        switch (dictionaryType) {
            case EVENT_DICTIONARY:
                eventValues[selectedDictionaryValue.level].push(selectedObj);
                break;
            case PRODUCT_DICTIONARY:
                selectedObj = {"name": selectedDictionaryValue.name, "id": selectedDictionaryValue.id}
                productValues[selectedDictionaryValue.level].push(selectedObj);
                break;
            case GENERIC_DICTIONARY:
                selectedObj = {"genericName": selectedDictionaryValue.genericName}
                productValues[selectedDictionaryValue.level].push(selectedObj);
                break;
            case STUDY_DICTIONARY:
                studyValues[selectedDictionaryValue.level].push(selectedObj);
                break;
        }
    }
}

function setDictionaryLevelText(dictionaryType, level, selectedObj) {
    if (level > 0) {
        var valueArea;
        var textDiv;
        switch (dictionaryType) {
            case EVENT_DICTIONARY:
                valueArea = document.getElementsByClassName("selectedEventDictionaryValue")[0];
                textDiv = valueArea.getElementsByClassName("level" + level)[0];
                break;
            case PRODUCT_DICTIONARY:
            case GENERIC_DICTIONARY:
                valueArea = document.getElementsByClassName("selectedProductDictionaryValue")[0];
                textDiv = valueArea.getElementsByClassName("level" + level)[0];
                break;
            case STUDY_DICTIONARY:
                valueArea = document.getElementsByClassName("selectedStudyDictionaryValue")[0];
                textDiv = valueArea.getElementsByClassName("level" + level)[0];
                break;
        }
        var oldValue = $(textDiv).text();
        if (oldValue) {
            oldValue = oldValue + ", ";
        }
        $(textDiv).text(oldValue + selectedObj.name + " (" + selectedObj.id + ")");
    }
}

$(function() {
    var eventLevels = {"1":"SOC", "2":"HLGT", "3":"HLT", "4":"PT", "5":"LLT", "6":"Synonyms"};
    var productLevels = {"1":"Ingredient", "2":"Family", "3":"Product Name", "4":"Trade Name", "5":"Generic Name"};
    var studyLevels = {"1":"Study Number", "2":"Project Number", "3":"Center"};

    var editable = ($('#editable').val() === 'true');

    $("input:radio[name=optradio]").on('click', function() {
        if ($(this).hasClass("productRadio")) {
            showProductSelectionTextArea();
        } else if($(this).hasClass("studyRadio")){
            showStudySelectionTextArea();
        }else if($(this).hasClass("genericRadio")) {
            showGenericSelectionTextArea();
        }
    });

    $("#js-data-generics").select2({
        placeholder: "Search for generic names",
        minimumInputLength: 1,
        multiple: true,
        allowClear: true,
        ajax: {
            url: searchGenericsUrl,
            dataType: 'json',
            data: function (term, page) { // page is the one-based page number tracked by Select2
                return {
                    q: term, //search term
                    page: page // page number
                };
            },
            results: function (data, page) {
                return {
                    results: data
                };
            },
            formatResult: function(option){
                return "<div>" + option.text + "</div>";
            }
        }

    });

    $(document).on('click', '.addEventValues', function () {
        addDictionary(eventValues, EVENT_DICTIONARY);
    });

    $(document).on('click', '.addProductValues', function () {
        addDictionary(productValues, PRODUCT_DICTIONARY);
    });

    $(document).on('click', '.addStudyValues', function () {
        addDictionary(studyValues, STUDY_DICTIONARY);
    });

    $(document).on('click', '.addAllEvents', function () {
        $("#eventSelection").val(checkEmptyValues(eventValues));
        showDictionaryValues(document.getElementById("showEventSelection"), eventValues, eventLevels);
    });

    $(document).on('click', '.addAllProducts', function () {
        clearDictionaryValues();
        $("#js-data-generics").val('');
        $("#productSelection").val(checkEmptyValues(productValues));
        if (typeof $("#product_multiIngredient") != "undefined" && $("#product_multiIngredient").is(':checked')) {
            $("#isMultiIngredient").val(true)
        } else {
            $("#isMultiIngredient").val(false)
        }
        showDictionaryValues(document.getElementById("showProductSelection"), productValues, productLevels);
    });

    $(document).on('click', '.addAllStudies', function () {
        $("#studySelection").val(checkEmptyValues(studyValues));
        showDictionaryValues(document.getElementById("showStudySelection"), studyValues, studyLevels);
    });

    $(document).on('click', '.addGenericValues', function () {
        resetDictionaryList(GENERIC_DICTIONARY);
        clearDictionaryValues();
        var generics = $("#js-data-generics").select2('val').toString();
        selectedDictionaryValue.level = GENERIC_LEVEL;
        selectedDictionaryValue.id = -1
        selectedDictionaryValue.genericName = generics;
        addDictionary(generics, GENERIC_DICTIONARY);
        $("#productSelection").val(checkEmptyValues(productValues));
        if (typeof $("#product_multiIngredient") != "undefined" && $("#product_multiIngredient").is(':checked')) {
            $("#isMultiIngredient").val(true)
        } else {
            $("#isMultiIngredient").val(false)
        }
        showDictionaryValues(document.getElementById("showGenericSelection"), productValues, 5);


    });

    $(document).on('click', '.clearGenericValues', function () {
        $("#js-data-generics").val('');
    });

    function checkEmptyValues(values) {
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
    }

    function clearDictionaryValues(){
        $('.showDictionarySelection').each( function (index, data) {
            if($(this).attr('id') != 'showEventSelection') {
                $(this).empty();
            }
        });
    }

    function showDictionaryValues(container, dictionaryValues, dictionaryLevels) {
        var values = "";
        container.innerHTML = "";

        for (var key in dictionaryValues) {
            if (dictionaryValues.hasOwnProperty(key)) {
                _.each(dictionaryValues[key], function (v) {
                    var name = v.name;
                    if(key == 5){
                        name = v.genericName;
                    }
                    values += name;
                })
            }
        }
        var div = document.createElement("div");
        if (editable) {
            div.style.padding = "5px";
        }
        div.innerHTML = values;
        container.appendChild(div);
    }

    // For edit/view page
    if ($("#eventSelection").val()) {
        eventValues = JSON.parse($("#eventSelection").val());
        if (editable) {
            setColumnViewText(EVENT_DICTIONARY, eventValues);
        }
        showDictionaryValues(document.getElementById("showEventSelection"), eventValues, eventLevels);
    }

    if ($("#productSelection").val()) { // either product or study
        productValues = JSON.parse($("#productSelection").val());
        if (editable) {
            document.getElementsByClassName("productRadio")[0].checked = true;
            showProductSelectionTextArea();
            setColumnViewText(PRODUCT_DICTIONARY, productValues);
        }
        showDictionaryValues(document.getElementById("showProductSelection"), productValues, productLevels);

    } else if ($("#studySelection").val()) {
        studyValues = JSON.parse($("#studySelection").val());
        if (editable) {
            document.getElementsByClassName("studyRadio")[0].checked = true;
            showStudySelectionTextArea();
            setColumnViewText(STUDY_DICTIONARY, studyValues);
        }
        showDictionaryValues(document.getElementById("showGenericSelection"), studyValues, studyLevels);
    } else if ($("#genericSelection").val()) {
        if (editable) {
            document.getElementsByClassName("genericRadio")[0].checked = true;
            showGenericSelectionTextArea();
        }
        showDictionaryValues(document.getElementById("showGenericSelection"), productValues, productLevels);

    }

    function setColumnViewText(dictionaryType, dictionaryValues) {
        for (var key in dictionaryValues) {
            if (dictionaryValues.hasOwnProperty(key)) {
                _.each(dictionaryValues[key], function (obj) {
                    setDictionaryLevelText(dictionaryType, key, obj);
                })
            }
        }
    }

    function showProductSelectionTextArea() {
        $(document.getElementById("showProductSelection").parentElement).removeAttr("hidden");
        $(document.getElementById("showStudySelection").parentElement).attr("hidden", "hidden");
        $(document.getElementById("showGenericSelection").parentElement).attr("hidden", "hidden");
    }

    function showStudySelectionTextArea() {
        $(document.getElementById("showStudySelection").parentElement).removeAttr("hidden");
        $(document.getElementById("showProductSelection").parentElement).attr("hidden", "hidden");
        $(document.getElementById("showGenericSelection").parentElement).attr("hidden", "hidden");
    }

    function showGenericSelectionTextArea() {
        $(document.getElementById("showGenericSelection").parentElement).removeAttr("hidden");
        $(document.getElementById("showProductSelection").parentElement).attr("hidden", "hidden");
        $(document.getElementById("showStudySelection").parentElement).attr("hidden", "hidden");
    }


    //Prevent Users from submitting form by hitting enter
    $(window).keydown(function(event){
        if(event.keyCode == 13) {
            event.preventDefault();
            return false;
        }
    });
});