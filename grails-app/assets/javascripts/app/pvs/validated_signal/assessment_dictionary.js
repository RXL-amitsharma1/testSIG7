//= require app/pvs/alert_utils/common_key_prevent.js

var EVENT_DICTIONARY = "event";
var PRODUCT_DICTIONARY = "product";
var STUDY_DICTIONARY = "study";
var SMQBROAD = "Broad";
var eventDictionaryObjAssessment, productDictionaryObjAssessment, studyDictionaryObj;
var selectedDictionaryValue = [];
var isShowAllClicked = false;
var productGroupValues = [];
var productGroupDictionaryObjAssessment= [];
var eventGroupValues = [];
var eventGroupDictionaryObjAssessment = [];
var isCloseProductDictionaryAssessment = true;
var productGroupDataSources = [];

var DictionaryAssessment = function (dicType, levelNames, columnsSize, selectUrlValue, preLevelParentsUrlValue, searchUrlValue, spinnerPathValue) {
    this.isCloseDictionary = true;
    var dictionaryType;
    var selectUrl;
    var preLevelParentsUrl;
    var values = {};

    var dataSourcesJSON = [];
    var pvaValues = {};
    var faersValues = {};
    var vaersValues = {};
    var valuesVigibase = {};
    var eudraValues = {};
    var valuesDicGroup = [];
    var selectedDicGroups = [];
    var allowedDataSourcesProduct = ['pva'];
    this.currentSelectedLevel = 0;
    var searchUrl;
    var inputClass;
    var levels = {};
    var dicColumnsSize;
    var spinnerPath;

    var disableDictionaryButtons = function () {
        $(".addEventValuesAssessment").prop('disabled', true);
        $(".addProductValuesAssessment").prop('disabled', true);
        $(".addAllProductValuesAssessment").prop('disabled', true);
        $(".addAllEventsAssessment").prop('disabled', true);
        $(".addAllEventValuesAssessment").prop('disabled', true);
        $(".addAllProductsAssessment").prop('disabled', true);
        $(".clearProductValuesAssessment").prop('disabled', true);
        $(".clearEventValuesAssessment").prop('disabled', true);
        $('.createProductGroupAssessment').prop('disabled',true);
        $('.updateProductGroupAssessment').prop('disabled',true);
        $(".createEventGroupAssessment").prop('disabled', true);
        $(".updateEventGroupAssessment").prop('disabled', true);
        $('.modal-footer .loading').css("display", "inline");
    };

    var enableDictionaryButtons = function () {
        $(".addEventValuesAssessment").prop('disabled', false);
        $(".addProductValuesAssessment").prop('disabled', false);
        $(".addAllProductValuesAssessment").prop('disabled', false);
        $(".addAllEventsAssessment").prop('disabled', false);
        $(".addAllEventValuesAssessment").prop('disabled', false);
        $(".addAllProductsAssessment").prop('disabled', false);
        $(".clearProductValuesAssessment").prop('disabled', false);
        $(".clearEventValuesAssessment").prop('disabled', false);
        $('.modal-footer .loading').css("display", "none");
        enableDisableProductGroupButtonsAssessment(getDictionaryObjectAssessment(PRODUCT_DICTIONARY).getValues());
        if (dictionaryType == EVENT_DICTIONARY) {
            enableDisableEventGroupButtonsAssessment();
        }
    };

    var addLanguageToUrl = function (lang) {
        return lang ? ("&currentLang=" + lang) : ""
    };

    var showLoading = function (element) {
        if (element) {
            element.disabled = true;
            var st = element.style;
            var pathToImage = document.location.href.split();
            st.backgroundImage = "url('" + spinnerPath + "')";
            st.backgroundPosition = "right center";
            st.backgroundRepeat = "no-repeat";
        }
    };

    var hideLoading = function (element) {
        if (element) {
            element.disabled = false;
            var st = element.style;
            st.backgroundImage = "none";
        }
    };

    var setDictionaryValues = function (value, level, dataSource) {
        var selectedObj
        if (dataSource == ASSESSMENT_DATASOURCE_PVA && value.level == level) {
            selectedObj = {"name": value.name, "id": value.id, "isMultiIngredient": value.isMultiIngredient};
        } else if (dataSource == ASSESSMENT_DATASOURCE_PVA && value.level == ASSESSMENT_DRUG_REC_NUM_LEVEL) {
            selectedObj = {
                "name": value.name,
                "id": value.id,
                "isMultiIngredient": value.isMultiIngredient,
                "drugRecordNumber": value.drugRecordNumber
            };
        } else {
            selectedObj = {"name": value.name, "id": value.id};
        }
        values[value.level].push(selectedObj);
    };

    var setDictionaryValuesForDataSource = function (value,level,dataSource,ingredientLevel) {
        var selectedObj;
        if (dataSource == ASSESSMENT_DATASOURCE_PVA && value.level == ingredientLevel) {
            selectedObj = {"name": value.name, "id": value.id, "isMultiIngredient": value.isMultiIngredient};
        } else if (dataSource == ASSESSMENT_DATASOURCE_PVA && value.level == ASSESSMENT_DRUG_REC_NUM_LEVEL) {
            selectedObj = {
                "name": value.name,
                "id": value.id,
                "isMultiIngredient": value.isMultiIngredient,
                "drugRecordNumber": value.drugRecordNumber
            };
        } else {
            selectedObj = {"name": value.name, "id": value.id};
        }
        if (dataSource == ASSESSMENT_DATASOURCE_PVA) {
            pvaValues[level].push(selectedObj);
        } else if (dataSource == "faers") {
            faersValues[level].push(selectedObj);
        } else if (dataSource == "vaers") {
            vaersValues[level].push(selectedObj);
        } else if (dataSource == "eudra") {
            eudraValues[level].push(selectedObj);
        }else if (dataSource == "vigibase") {
            valuesVigibase[level].push(selectedObj);
        }
    };

    var isDuplicate = function (dictionaryValues, checkedDictionaryValue, level) {
        var key = dictionaryType == PRODUCT_DICTIONARY ? level : checkedDictionaryValue.level;
        for (var i = 0; i < dictionaryValues[key].length; i++) {
            if (dictionaryValues[key][i].id == checkedDictionaryValue.id)
                return true;
        }
        return false;
    };

    var isDuplicateEventValues = function (dictionaryValues, checkedDictionaryValue, level) {
        var key = dictionaryType == EVENT_DICTIONARY ? level : checkedDictionaryValue.level;
        if (dictionaryValues[key] != undefined && dictionaryValues[key].length > 0) {
            for (var i = 0; i < dictionaryValues[key].length; i++) {
                if (dictionaryValues[key][i].id == checkedDictionaryValue.id)
                    return true;
            }
        }
        return false;
    };

    var removeVal = function (dict, val) {
        return _.filter(dict, function (el) {
            if (el["id"] && val["id"]) {
                if (el["id"] === val["id"]) return false;
            } else {
                if (el["name"] === val["name"]) return false;
            }
            return true;
        });
    };

    var getEmptyValues = function (dicSize) {
        var values = {};
        for (var i = 1; i <= dicSize; i++) {
            values[i] = []
        }
        // Cleared value for drug rec num hierarchy
        values[ASSESSMENT_DRUG_REC_NUM_LEVEL] = []
        return values
    };

    var checkIfObjectExist = function (id) {
        for (var i = 0; i < valuesDicGroup.length; i++) {
            if (valuesDicGroup[i].id == id)
                return true
        }
        return false
    };


    this.setProductGroupText = function (editable) {
        var valueArea = $(".selectedProductDictionaryValueAssessment")[0];
        var textDiv1 = $(valueArea).find(".level-product-group-assessment");
        var selectedProductGrps = $('#productGroupSelectAssessment').select2('data');
        var multiIngForSelectedGrp;
        var dictionaryObj = getDictionaryObjectAssessment(dictionaryType)
        if (selectedProductGrps[0] && selectedProductGrps[0].hasOwnProperty("isMultiIngredient")) {
            multiIngForSelectedGrp = selectedProductGrps[0].isMultiIngredient;
            var allHaveSameMultiIng = true;
            _.each(selectedProductGrps, function (value) {
                if (value.hasOwnProperty('isMultiIngredient') && (value.isMultiIngredient != multiIngForSelectedGrp)) {
                    allHaveSameMultiIng = false
                }
            })
            if (dictionaryObj.getValuesDicGroup().length > 0) {
                _.each(dictionaryObj.getValuesDicGroup(), function (value) {
                    if (value.hasOwnProperty("isMultiIngredient") && (value.isMultiIngredient != multiIngForSelectedGrp)) {
                        allHaveSameMultiIng = false
                    }
                });
            }
            if (dictionaryObj.getValues()[ingredientLevel].length > 0) {
                _.each(dictionaryObj.getValues()[ingredientLevel], function (value) {
                    if (value.hasOwnProperty("isMultiIngredient") && (value.isMultiIngredient != multiIngForSelectedGrp)) {
                        allHaveSameMultiIng = false
                    }
                })
            }
            if(allHaveSameMultiIng == false){
                $('#dictWarningModal').modal("show");
                return;
            }
        }
        if (selectedProductGrps) {
            _.each(selectedProductGrps, function (value) {
                var found = checkIfObjectExist(value.id);
                if (!found) {
                    productGroupDataSources.push(value.text.substring(value.text.indexOf('Data Source:')).substring(12));
                    var $elem1 = $("<span class='productGroupValues'><span class='dictionaryItem'>" + value.name + "<span class='closebtn removeSingleDictionaryProductValueAssessment'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllProductsAssessment btn-show-products" data-id="' + value.id + '">' + '</i></span>');
                    var $closeButton = $elem1.find(".closebtn");
                    $closeButton.attr("data-type", dictionaryType);
                    $closeButton.attr("data-element", JSON.stringify({name: value.name, id: value.id, isMultiIngredient: value.isMultiIngredient, includeWHODrugs: value.includeWHODrugs}));
                    valuesDicGroup.push({name: value.name, id: value.id, isMultiIngredient: value.isMultiIngredient, includeWHODrugs: value.includeWHODrugs});
                    $(textDiv1).append($elem1);
                }
            })
        }
        if (editable) {
            if(isProductAssignment){
                $(textDiv1).html("")
            }
            _.each(getDictionaryObjectAssessment(dictionaryType).getValuesDicGroup(), function (value) {
                var $elem1 = $("<span class='productGroupValues'><span class='dictionaryItem'>" + value.name + "<span class='closebtn removeSingleDictionaryProductValueAssessment'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllProductsAssessment btn-show-products" data-id="' + value.id + '">' + '</i></span>');
                var $closeButton = $elem1.find(".closebtn");
                $closeButton.attr("data-type", dictionaryType);
                $closeButton.attr("data-element", JSON.stringify({name: value.name, id: value.id, isMultiIngredient: value.isMultiIngredient, includeWHODrugs: value.includeWHODrugs}));
                $(textDiv1).append($elem1);
            });
        }

    };

    this.setProductGroupUpdateText = function (value) {
        var valueArea = $(".selectedProductDictionaryValueAssessment")[0];
        var textDiv1 = $(valueArea).find(".level-product-group-assessment");
        productGroupDataSources.push(value.text.substring(value.text.indexOf('Data Source:')).substring(12));
        var $elem1 = $("<span class='productGroupValues'><span class='dictionaryItem'>" + value.name + "<span class='closebtn removeSingleDictionaryProductValueAssessment'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllProductsAssessment btn-show-products" data-id="' + value.groupId + '">' + '</i></span>');
        var $closeButton = $elem1.find(".closebtn");
        $closeButton.attr("data-type", dictionaryType);
        $closeButton.attr("data-element", JSON.stringify({name: value.name, id: value.groupId}));
        $(textDiv1).append($elem1);
    };

    this.setEventGroupUpdateText = function (value) {
        var valueArea = $(".selectedEventDictionaryValueAssessment")[0];
        var textDiv1 = $(valueArea).find(".level-event-group-assessment");
        var $elem1 = $("<span class='eventGroupValues'><span class='dictionaryItem'>" + value.name + "<span class='closebtn removeSingleDictionaryEventValueAssessment'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllEventsAssessment btn-show-events" data-id="' + value.groupId + '">' + '</i></span>');
        var $closeButton = $elem1.find(".closebtn");
        $closeButton.attr("data-type", dictionaryType);
        $closeButton.attr("data-element", JSON.stringify({name: value.name, id: value.groupId}));
        $(textDiv1).append($elem1);
    };

    this.setEventGroupText = function (editable) {
        var valueArea = $(".selectedEventDictionaryValueAssessment")[0];
        var textDiv1 = $(valueArea).find(".level-event-group-assessment");
        _.each(getDictionaryObjectAssessment(dictionaryType).getValuesDicGroup() ,  function (value) {
            var $elem1 = $("<span class='eventGroupValues'><span class='dictionaryItem'>" + value.name + "<span class='closebtn removeSingleDictionaryEventValueAssessment'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllEventsAssessment btn-show-events" data-id="' + value.id + '">' + '</i></span>');
            var $closeButton = $elem1.find(".closebtn");
            $closeButton.attr("data-type", dictionaryType);
            $closeButton.attr("data-element", JSON.stringify({name: value.name, id: value.id}));
            $(textDiv1).append($elem1);
        });
    };

    this.setDictionaryLevelText = function (level, selectedObj, dataSource,isNotNew) {
        if (level > 0) {
            var valueArea,$elem;
            switch (dictionaryType) {
                case EVENT_DICTIONARY:
                    valueArea = $(".selectedEventDictionaryValueAssessment")[0];
                    break;
                case PRODUCT_DICTIONARY:
                    valueArea = $(".selectedProductDictionaryValueAssessment")[0];
                    break;
            }
            var textDiv = $(valueArea).find(".level" + level)[0];
            var dataSourceMapValues = JSON.parse(dataSourcesColorMapValues);
            var title = ASSESSMENT_DATASOURCE_SAFETY_TITLE;
            if (dataSource == ASSESSMENT_DATASOURCE_FAERS)
                title = ASSESSMENT_DATASOURCE_FAERS_TITLE;
            else if (dataSource == ASSESSMENT_DATASOURCE_EVDAS)
                title = ASSESSMENT_DATASOURCE_EVDAS_TITLE;
            else if (dataSource == ASSESSMENT_DATASOURCE_VAERS)
                title = ASSESSMENT_DATASOURCE_VAERS_TITLE;
            else if (dataSource == ASSESSMENT_DATASOURCE_VIGIBASE)
                title = ASSESSMENT_DATASOURCE_VIGIBASE_TITLE;
            var label = (selectedObj.id == " " ? selectedObj.name : (selectedObj.name + " (" + selectedObj.id + ")"));
            if (level == ASSESSMENT_DRUG_REC_NUM_LEVEL) {
                label = selectedObj.id + " (" + selectedObj.name + ")"
            }
            if (dictionaryType == PRODUCT_DICTIONARY) {
                if(typeof isNotNew != 'undefined'){
                    $elem = $('<span class="dictionaryItem" title="'+ title +'">' + label + '<span class="closebtn removeSingleDictionaryValueAssessment">×</span></span>');
                } else {
                    $elem = $('<span class="dictionaryItem" data-source="'+dataSource+'" data-newproduct="new" title="' + title + '">' + label + '<span class="closebtn removeSingleDictionaryValueAssessment">×</span></span>');
                }
            }
            else {
                $elem = $("<span class='dictionaryItem'>" + label + "<span class='closebtn removeSingleDictionaryValueAssessment'>×</span></span>");
            }

            if (dictionaryType == PRODUCT_DICTIONARY) {
                $.each(dataSourceMapValues, function (key, value) {
                    if (key == dataSource && value) {
                        $elem.css('background-color', value);
                        $elem.css('color', '#fff');
                        $elem.find('.closebtn').css('color', '#fff');
                    }
                });
                var isDup = false;
                if (dataSource == 'pva') {
                    isDup = isDuplicate(pvaValues, selectedObj, level)
                } else if (dataSource == 'faers') {
                    isDup = isDuplicate(faersValues, selectedObj, level)
                } else if (dataSource == 'eudra') {
                    isDup = isDuplicate(eudraValues, selectedObj, level)
                } else if (dataSource == 'vaers') {
                    isDup = isDuplicate(vaersValues, selectedObj, level)
                } else if (dataSource == 'vigibase') {
                    isDup = isDuplicate(valuesVigibase, selectedObj, level)
                }
                if (!isDup)
                    setDictionaryValuesForDataSource(selectedObj, level, dataSource,ingredientLevel);
            } else {
                if(!isDuplicateEventValues(pvaValues, selectedObj,level))
                    setDictionaryValuesForDataSource(selectedObj, level, dataSource,ingredientLevel);
            }

            var $closeButton = $elem.find(".closebtn");
            $closeButton.attr("data-type", dictionaryType);
            $closeButton.attr("data-level", level);
            $closeButton.attr("data-element", JSON.stringify(selectedObj));
            $(textDiv).append($elem);
            $(textDiv).append(" ");
        }
    };


    this.source = function (ID, cb, level, isClick, isCtrlPressed, lang) {
        var isAjaxCall = true;
        var dataSource = getDataSource(dictionaryType);
        var multiIngredient = $("#productModalAssessment").find("#product_multiIngredient").is(":checked");
        //Don't need any prelevel hierarchy in case of dataSources other than pva
        if (dictionaryType === PRODUCT_DICTIONARY && dataSource != 'pva') {
            isAjaxCall = false;
        }

        if (ID && isAjaxCall) {
            disableDictionaryButtons();
            var that = this;
            $.ajax({
                type: "GET",
                url: selectUrl + ID + "&dataSource="+ dataSource + "&dictionaryLevel=" + level + addLanguageToUrl(lang) + "&isMultiIngredient=" + multiIngredient,
                dataType: "json",
            })
                .done(function (result) {
                    if (isClick) {
                        selectedDictionaryValue.id = result.id;
                        selectedDictionaryValue.name = result.name;
                        selectedDictionaryValue.level = level;
                        that.currentSelectedLevel = level;
                        // If PT was clicked, add primary SOC to data
                        if (dictionaryType === EVENT_DICTIONARY && level == 4) {
                            if (result.primarySOC) {
                                selectedDictionaryValue.primarySOC = result.primarySOC;
                                selectedDictionaryValue.primaryHLT = result.primaryHLT;
                                selectedDictionaryValue.primaryHLGT = result.primaryHLGT;
                            } else {
                                selectedDictionaryValue.primarySOC = null;
                                selectedDictionaryValue.primaryHLT = null;
                                selectedDictionaryValue.primaryHLGT = null;
                            }
                        }
                    }
                    if (result.nextLevelItems.length) {
                        cb({items: result.nextLevelItems, lang: result.lang}, level);
                    } else {
                        // PVR-3016: Synonyms returned on level 6 require a callback to fire to showParents.
                        cb({items: [], lang: result.lang}, level);
                    }
                    enableDictionaryButtons();
                });
        } else if (level) {
            // get "level" from ul -> SOC, HLGT, HLT, PT, LLT

        }
    };

    this.sourceParents = function (IDs, level, cb, lang) {
        var dataSource = getDataSource(dictionaryType);
        var urlValue = preLevelParentsUrl.split("?");
        var dictTypeValue = urlValue[1].split('=')[0];
        var multiIngredient = $("#productModalAssessment").find("#product_multiIngredient_assessment").is(":checked");
        var currentLang = lang ? lang : "";
        var data = {
            dataSource: dataSource,
            dictionaryLevel: level,
            currentLang: currentLang,
            isMultiIngredient: multiIngredient
        };
        data[dictTypeValue] = "" + IDs + "";
        if (IDs) {
            $.ajax({
                type: "GET",
                url: preLevelParentsUrl + IDs + "&dictionaryLevel=" + level + addLanguageToUrl(lang),
                dataType: "json",
            })
                .done(function (result) {
                    if (result.length > 0) {
                        cb({items: result, lang: lang}, level);
                    }
                });
        }
    };

    this.forSearch = function (searchTerm, level, ref_level, additionalCriteriaData, cb, selectedInput) {
        this.currentSelectedLevel = level;
        showLoading(selectedInput);
        var exact_search = $("#event_exactSearch").is(':checked') || $("#event_exactSearch1").prop('checked');
        var imp = false;
        var multi_ingredient;
        multi_ingredient = $("#product_multiIngredient_assessment").is(':checked');
        if (dictionaryType === PRODUCT_DICTIONARY) {
            exact_search = $("#product_exactSearchAssessment").is(':checked');
        }
        var data;
        if (delimiter != null && typeof delimiter == "string") {
            data = {
                contains: searchTerm,
                dictionaryLevel: level,
                delimiter: delimiter,
                ref_level: ref_level,
                exact_search: exact_search,
                multi_ingredient: multi_ingredient,
                imp: imp
            }
        } else {
            data = {
                contains: searchTerm,
                dictionaryLevel: level,
                ref_level: ref_level,
                exact_search: exact_search,
                multi_ingredient: multi_ingredient,
                imp: imp
            }
        }
        data['dataSource'] = dataSource;
        if (additionalCriteriaData) {
            var filters = {};
            $.each(additionalCriteriaData, function (i, item) {
                filters[item.key] = item.val;
            });
            data.filters = JSON.stringify(filters);
        }
        // show result in correct column
        $.ajax({
            type: "GET",
            url: searchUrl,
            data: data,
            dataType: "json",
        })
            .done(function (result) {
                if (result) {
                    addAllProductValues();
                }
                cb({items: result}, level);
                hideLoading(selectedInput);
            });
    };


    this.addAllOptions = function (objs,multiIngredient,ingredientLevel) {
        var that = this;
        if (dictionaryType == PRODUCT_DICTIONARY) {
            objs.each(function () {
                var name = $(this).text() ? $(this).text() : $(this).attr("text");
                var level = $(this).attr("dictionarylevel");
                var value = $(this).attr("data-value");
                var drugRecordNumber = $(this).attr("recordNumber");
                var dataSource = $(this).attr("data-source");
                var selectedObj = {"name": name, "id": value, "level": level,"isMultiIngredient": multiIngredient, "drugRecordNumber": drugRecordNumber};
                var objectToCompare
                if(getDataSource(dictionaryType) == 'pva'){
                    objectToCompare = pvaValues
                } else if(getDataSource(dictionaryType) == 'faers'){
                    objectToCompare = faersValues
                } else if (getDataSource(dictionaryType) == 'vaers') {
                    objectToCompare = vaersValues
                } else if (getDataSource(dictionaryType) == 'vigibase') {
                    objectToCompare = valuesVigibase
                } else {
                    objectToCompare = eudraValues
                }
                if (!isDuplicate(objectToCompare, selectedObj, selectedObj.level)){
                    setDictionaryValues(selectedObj,ingredientLevel, getDataSource(dictionaryType));
                    that.setDictionaryLevelText(selectedObj.level, selectedObj, getDataSource(dictionaryType));
                }
            });
        } else {
            objs.each(function () {
                var name = $(this).text();
                var level = $(this).attr("dictionarylevel");
                var value = $(this).attr("data-value");
                var selectedObj = {"name": name, "id": value, "level": level, "isMultiIngredient": multiIngredient};

                if (!isDuplicateEventValues(values, selectedObj , level)) {
                    setDictionaryValues(selectedObj,ingredientLevel,getDataSource(dictionaryType));
                    that.setDictionaryLevelText(selectedObj.level, selectedObj, getDataSource(dictionaryType));
                }
            });
        }
    };

    this.clearSearchInputs = function (exceptIndex) {
        _.each($(inputClass), function (input, index) {
            if (index != exceptIndex && !$(input).hasClass("additionalCriteria")) {
                input.value = "";
            }
        });
    };

    var clearAdditionalProductFilter = function () {
        _.each($('#productModalAssessment').find('.additionalCriteria'), function (input, index) {
            $(input).html('');
        });
    };

    this.clearAdditionalFilters = function () {
        var exactSearch = $('#event_exactSearch');
        if (dictionaryType == PRODUCT_DICTIONARY) {
            exactSearch = $('#product_exactSearchAssessment');
            $('#product_multiIngredient_assessment').prop('checked', false);
            $('#who_dr_checkbox_assessment').prop('checked', false);
            $("#productGroupSelectAssessment").val('');
            clearAdditionalProductFilter();
        } else if (dictionaryType == EVENT_DICTIONARY) {
            $("#eventSmqSelectAssessment").val('');
            $("#eventGroupSelectAssessment").val('');
        }
        exactSearch.prop('checked', false)
    };

    this.clearAllTextAssessment = function () {
        var dictionaryValues = $(".eventDictionaryValueAssessment");
        if (dictionaryType == PRODUCT_DICTIONARY) {
            dictionaryValues = $(".productDictionaryValueAssessment");
        }
        _.each(dictionaryValues, function (div) {
            div.innerHTML = "";
        });
    };

    this.clearProductGroupText = function () {
        var valueArea = $(".selectedProductDictionaryValueAssessment")[0];
        $(valueArea).find(".level-product-group-assessment").empty();
    };

    this.resetDictionaryList = function () {
        values = getEmptyValues(dicColumnsSize);
        pvaValues = getEmptyValues(dicColumnsSize);
        faersValues = getEmptyValues(dicColumnsSize);
        vaersValues = getEmptyValues(dicColumnsSize);
        eudraValues = getEmptyValues(dicColumnsSize);
        valuesVigibase = getEmptyValues(dicColumnsSize);
        valuesDicGroup = [];
        selectedDicGroups = [];
    };

    this.setColumnViewText = function (dictionaryValues, dataSource,isNotNew) {
        var that = this;
        for (var key in dictionaryValues) {
            if (dictionaryValues.hasOwnProperty(key)) {
                _.each(dictionaryValues[key], function (obj) {
                    that.setDictionaryLevelText(key, obj, dataSource,isNotNew);
                })
            }
        }
    };

    this.refresh = function () {
        var that = this;
        for (var key in values) {
            if (values.hasOwnProperty(key)) {
                _.each(values[key], function (obj) {
                    that.setDictionaryLevelText(key, obj);
                })
            }
        }
    };

    this.removeValue = function (level, val, datasource) {
        values[level] = removeVal(values[level], val);
        if (datasource == "FAERS") {
            faersValues[level] = removeVal(faersValues[level], val);
        } else if (datasource == "VAERS") {
            vaersValues[level] = removeVal(vaersValues[level], val);
        } else if (datasource == "EVDAS") {
            eudraValues[level] = removeVal(eudraValues[level], val);
        } else if (datasource == "VIGIBASE") {
            valuesVigibase[level] = removeVal(valuesVigibase[level], val);
        }  else
        pvaValues[level] = removeVal(pvaValues[level], val);
    };

    this.getValuesDicGroup = function () {
        return valuesDicGroup
    };

    this.setValuesDicGroup = function (newValues) {
        valuesDicGroup = newValues
    };

    this.addToValuesDicGroup = function (newValue) {
        valuesDicGroup.push(newValue)
    };

    this.resetValuesDicGroup = function () {
        valuesDicGroup = []
    };

    this.getSelectedDicGroups = function () {
        return selectedDicGroups
    };

    this.setSelectedDicGroups = function (newValues) {
        selectedDicGroups = newValues
    };

    this.addToSelectedDicGroups = function (newValue) {
        selectedDicGroups.push(newValue)
    };

    this.getAllowedDataSourcesProduct = function () {
        return allowedDataSourcesProduct
    };

    this.setAllowedDataSourcesProduct = function (newValues) {
        allowedDataSourcesProduct = newValues
    };

    this.getValues = function () {
        return values
    };

    this.setValues = function (newValues) {
        values = newValues
    };

    this.getValuesPva = function () {
        return pvaValues
    };

    this.setValuesPva = function (newValues) {
        pvaValues = newValues
    };

    this.getValuesFaers = function () {
        return faersValues
    };

    this.setValuesFaers = function (newValues) {
        faersValues = newValues
    };

    this.getValuesVaers = function () {
        return vaersValues
    };

    this.setValuesVaers = function (newValues) {
        vaersValues = newValues
    };

    this.getValuesEudra = function () {
        return eudraValues
    };

    this.getValuesVigibase = function () {
        return valuesVigibase
    };

    this.setValuesVigibase = function (newValues) {
        valuesVigibase = newValues
    };

    this.setValuesEudra = function (newValues) {
        eudraValues = newValues
    };

    this.getEventGroupValues = function () {
        return eventGroupValues
    };

    this.getProductGroupValues = function () {
        return productGroupValues
    };

    this.setProductGroupValues = function (newValues) {
        productGroupValues = newValues
    };

    this.setEventGroupValues = function (newValues) {
        eventGroupValues = newValues
    };

    this.getLevels = function () {
        return levels
    };

    this.setLevels = function (newLevels) {
        levels = newLevels
    };

    this.resetValues = function () {
        values = getEmptyValues(columnsSize);
    };

    this.isShowEnabled = function (id) {
        var isShow = false;
        $.each(selectedDicGroups, function (k, v) {
            if (v.result.id == id) {
                isShow = true;
                return true;
            }
        });
        return isShow
    };

    this.construct = function (dicType, levelNames, columnsSize, selectUrlValue, preLevelParentsUrlValue, searchUrlValue, spinnerPathValue) {
        searchUrl = searchUrlValue;
        dictionaryType = dicType;
        if (dicType === EVENT_DICTIONARY) {
            selectUrl = selectUrlValue + "?eventId=";
            preLevelParentsUrl = preLevelParentsUrlValue + "?eventIds=";
            inputClass = 'input.searchEvents';
        }
        if (dicType === PRODUCT_DICTIONARY) {
            selectUrl = selectUrlValue + "?productId=";
            preLevelParentsUrl = preLevelParentsUrlValue + "?productIds=";
            inputClass = 'input.searchProducts:not([id*="prod"])';
        }
        $.each(levelNames.split(','), function (index, value) {
            levels[index + 1] = value;
        });
        if (dicType === PRODUCT_DICTIONARY) {
            levels[ASSESSMENT_DRUG_REC_NUM_LEVEL] = ASSESSMENT_DRUG_RECORD_NUMBER
        }
        dicColumnsSize = columnsSize;
        values = getEmptyValues(columnsSize);

        for (var i = 1; i <= 3; i++) {
            dataSourcesJSON[i] = []
        }
        pvaValues = getEmptyValues(columnsSize);
        faersValues = getEmptyValues(columnsSize);
        vaersValues = getEmptyValues(columnsSize);
        eudraValues = getEmptyValues(columnsSize);
        valuesVigibase = getEmptyValues(columnsSize);
        spinnerPath = spinnerPathValue
    };

    return this.construct(dicType, levelNames, columnsSize, selectUrlValue, preLevelParentsUrlValue, searchUrlValue, spinnerPathValue);
};

function intializeDictionariesAssessment(options) {
    if (options.event) {
        eventDictionaryObjAssessment = new DictionaryAssessment(EVENT_DICTIONARY, options.event.levelNames, options.event.dicColumnCount, options.event.selectUrl, options.event.preLevelParentsUrl, options.event.searchUrl, options.spinnerPath);
    }
    if (options.product) {
        productDictionaryObjAssessment = new DictionaryAssessment(PRODUCT_DICTIONARY, options.product.levelNames, options.product.dicColumnCount, options.product.selectUrl, options.product.preLevelParentsUrl, options.product.searchUrl, options.spinnerPath);
    }
}

function getDictionaryObjectAssessment(dictionaryType) {
    if (!dictionaryType) {
        return
    }
    if (dictionaryType.toLowerCase() === EVENT_DICTIONARY) {
        return eventDictionaryObjAssessment
    }
    if (dictionaryType.toLowerCase() === PRODUCT_DICTIONARY) {
        return productDictionaryObjAssessment
    }
}

function source(ID, cb, level, dictionaryType, isClick, isCtrlPressed, lang) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    dictionary.source(ID, cb, level, isClick, isCtrlPressed, lang);
}

function sourceParents(IDs, level, cb, dictionaryType, lang) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    dictionary.sourceParents(IDs, level, cb, lang);
}

function forSearch(searchTerm, level, ref_level, additionalCriteriaData, cb, dictionaryType, selectedInput) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    dictionary.forSearch(searchTerm, level, ref_level, additionalCriteriaData, cb, selectedInput);
}

function addAllProductValues() {
    $(document).on('click', '.addAllProductValuesAssessment', function () {
        var dictionary = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var productOptions = $(this).parents("#productModalAssessment").find('[class*="dicLi"][dictionarylevel="' + dictionary.currentSelectedLevel + '"]');
        dictionary.addAllOptions(productOptions);
        var $whoDrCheckboxAssessment = $('#who_dr_checkbox_assessment');
        if ($whoDrCheckboxAssessment.is(':checked') && getDataSource(PRODUCT_DICTIONARY) === ASSESSMENT_DATASOURCE_PVA) {
            setTimeout(function () {
                fetchAndUpdateDrugRecordNumbersAssessment(dictionaryObj);
            });
        }
    });
}

function clearSearchInputsAssessment(exceptIndex, dictionaryType) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    dictionary.clearSearchInputs(exceptIndex);
}

function clearAdditionalFilters(dictionaryType) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    dictionary.clearAdditionalFilters();
}

function clearAllTextAssessment(dictionaryType) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    dictionary.clearAllTextAssessment();
}

function clearProductGroupTextAssessment(dictionaryType) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    if (dictionaryType == PRODUCT_DICTIONARY)
        dictionary.clearProductGroupText();
    if (dictionaryType == EVENT_DICTIONARY)
        clearEventGroupTextAssessment();
}

function resetDictionaryListAssessment(dictionaryType) {
    var dictionary = getDictionaryObjectAssessment(dictionaryType);
    dictionary.resetDictionaryList();
}

function resetMultiSearchModal() {
    var container = $("#copyAndPasteDicModal");
    container.find('.copyPasteContent').val("");
    container.find('.c_n_p_other_delimiter').val("");
    container.find(":radio[value=none]").prop("checked", true)
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
        var obj = {
            id: li.getAttribute("data-value"),
            name: li.innerText,
            level: li.getAttribute("dictionaryLevel")
        };
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

var clearAllTextFromDictionary = function () {
    var valueArea = $(".selectedProductDictionaryValueAssessment");
    valueArea.find('.level-product-group-assessment').html('');
    valueArea.find('.productDictionaryValueAssessment').html('');
};

function setProductEventDictionary() {
    // Check if we need to give either of two option or not.
    if ($('input[name=optradio]').length > 0) {
        var checkedDic = $('input[name=optradio]:checked')[0];
        if ($(checkedDic).hasClass("productRadio")) {
            $("#studySelection").val("");
        } else {
            $("#productSelection").val("").trigger("change");
        }
    }
}

function getReportFieldLevel() {
    return $('#selectField option:selected').attr('data-level');
}

function getReportFieldDicType() {
    return $('#selectField option:selected').attr('data-dictionary');
}

function showEventDicIcon(isEvent) {
    if (isEvent) {
        $('#searchEvents').show();
    } else {
        $('#searchEvents').hide();
    }
}

function showProductDicIcon(isProduct) {
    if (isProduct) {
        $('#searchProducts').show();
    } else {
        $('#searchProducts').hide();
    }
}

function showStudyDicIcon(isStudy) {
    if (isStudy) {
        $('#searchStudies').show();
    } else {
        $('#searchStudies').hide();
    }
}

function checkSuspectProduct() {
    checkProductDependedntCheckboxes("#suspectProduct");
}

function checkIncludeAllStudyDrugsCases() {
    checkProductDependedntCheckboxes("#includeAllStudyDrugsCases");
}

function checkHeaderProductSelection() {
    // ID example: templateQueries[0].headerProductSelection
    checkProductDependedntCheckboxes("input[id$='headerProductSelection']");
}

function checkProductDependedntCheckboxes(jQuerySelector) {
    if ((!isProductAssignment) && ($("#productSelectionAssessment").val() !== "" || $("#isTemplate").is(":checked"))) {
        $(jQuerySelector).removeAttr("disabled");
    } else if ((isProductAssignment) && (currentEditingRow.find("#productSelectionAssessment").val() !== "" || $("#isTemplate").is(":checked"))) {
        $(jQuerySelector).removeAttr("disabled");
    } else {
        $(jQuerySelector).prop("checked", false);
        $(jQuerySelector).attr("disabled", true);
    }
}

function getFieldLevelFromExpression(container) {
    return $(container.querySelector('select.expressionField')).find('option:selected').attr('data-level');
}

function showDictionaryValuesAssessment(container, dictionaryValues,dicType, dictionaryGroupValues, dictionaryLevels, editable) {
    var values = "";
    container.innerHTML = "";

    if (dictionaryGroupValues) {
        var dicGroupName = 'Product';
        if (dicType == EVENT_DICTIONARY) {
            dicGroupName = 'Event';
        }
        _.each(dictionaryGroupValues, function (v) {
            values += v.name.substring(0, v.name.lastIndexOf('(') - 1) + " (" + dicGroupName + " Group), "
        });
    }

    if (dictionaryValues) {
        for (var key in dictionaryValues) {
            if (dictionaryValues.hasOwnProperty(key)) {
                if (key == ASSESSMENT_DRUG_REC_NUM_LEVEL) {
                    _.each(dictionaryValues[key], function (v) {
                        values += v.id + " (" + v.name + ") (" + dictionaryLevels[key] + "), ";
                    })
                } else {
                    _.each(dictionaryValues[key], function (v) {
                        values += v.name + " (" + dictionaryLevels[key] + "), ";
                    })
                }
            }
        }
    }
    var div = document.createElement("div");
    if(!isProductAssignment) {
        if (editable) {
            div.style.padding = "5px";
        }
        div.innerHTML = values.substring(0, values.length - 2);
        container.appendChild(div);
    } else {
        if (editable) {
            div.style.padding = "1px";
        }
        div.innerHTML = values.substring(0, values.length - 2).split(",").join(",<br>");
        container.append(div);
    }
    checkSuspectProduct();
    checkIncludeAllStudyDrugsCases();
    checkHeaderProductSelection();
}


function getDataSource(dictionaryType) {
    var $selectedDataSource = $("#dataSourcesProductDictAssessment");
    if (dictionaryType == PRODUCT_DICTIONARY) {
        return $selectedDataSource.length > 0 ? $selectedDataSource.val() : 'pva';
    } else {
        return 'pva';
    }
}

function enableDisableProductGroupButtonsAssessment(values) {
    var ele = $(".productDictionaryValueAssessment").find(".dictionaryItem");
    var dicObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
    if (ele.length > 0 && dicObj.getValuesDicGroup().length) {
        $('.updateProductGroupAssessment').attr('disabled',false);
        $('.createProductGroupAssessment').attr('disabled',false);
    } else if(ele.length > 0 && !dicObj.getValuesDicGroup().length){
        $('.updateProductGroupAssessment').attr('disabled',true);
        $('.createProductGroupAssessment').attr('disabled',false);
    } else if (dicObj.getValuesDicGroup().length) {
        $('.updateProductGroupAssessment').attr('disabled', false);
        $('.createProductGroupAssessment').attr('disabled', false);
    } else {
        $('.updateProductGroupAssessment').attr('disabled',true);
        $('.createProductGroupAssessment').attr('disabled',true);
    }

}

function enableDisableEventGroupButtonsAssessment() {
    var ele = $(".eventDictionaryValueAssessment").find(".dictionaryItem");
    var dicObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
    if (ele.length > 0 && dicObj.getValuesDicGroup().length) {
        $('.updateEventGroupAssessment').attr('disabled', false);
        $('.createEventGroupAssessment').attr('disabled', false);
    } else if (ele.length > 0 && !dicObj.getValuesDicGroup().length) {
        $('.updateEventGroupAssessment').attr('disabled', true);
        $('.createEventGroupAssessment').attr('disabled', false);
    } else if (dicObj.getValuesDicGroup().length) {
        $('.updateEventGroupAssessment').attr('disabled', false);
        $('.createEventGroupAssessment').attr('disabled', false);
    } else {
        $('.updateEventGroupAssessment').attr('disabled', true);
        $('.createEventGroupAssessment').attr('disabled', true);
    }

}

function enableDisabledEventGroupButtonsAssessment(values) {
    var emptyList = true;
    for (var key in values) {
        if (values.hasOwnProperty(key)) {
            if (values[key].length > 0) {
                emptyList = false;
            }
        }
    }
    if (!emptyList) {
        $('.createEventGroupAssessment').attr('disabled', false);
        $('.updateEventGroupAssessment').attr('disabled', false);
    } else {
        $('.createEventGroupAssessment').attr('disabled', true);
    }
}

function showEventGroupValues(eventGroupList) {
    var valueArea = $(".selectedEventDictionaryValue")[0];
    var textDiv1 = $(valueArea).find(".level-event-group");
    if (eventGroupList && eventGroupList.length > 0) {
        var dictionaryObject = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var values = dictionaryObject.getValuesDicGroup();
        if (values == null) values = [];
        for (var i = 0; i < eventGroupList.length; i++) {
            var eventGroup = eventGroupList[i];
            if (!checkIfEventGroupObjectExist(values, eventGroup.id)) {
                var $elem1 = $("<span class='eventGroupValues'><span class='dictionaryItem'>" + eventGroup.name + "<span class='closebtn removeSingleDictionaryEventValue'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllEvents btn-show-events" data-id="' + eventGroup.id + '">' + '</i></span>');
                var $closeButton = $elem1.find(".closebtn");
                $closeButton.attr("data-type", dictionaryObject.dictionaryType);
                $closeButton.attr("data-element", JSON.stringify({name: eventGroup.name, id: eventGroup.id}));

                var selectedObj = {"name": eventGroup.name, "id": eventGroup.id};
                values.push(selectedObj);
                $(textDiv1).append($elem1);
            }
        }
        dictionaryObject.setValuesDicGroup(values);
    }
}

function clearEventGroupTextAssessment() {
    var valueArea = $(".selectedEventDictionaryValueAssessment")[0];
    $(valueArea).find(".level-event-group-assessment").empty();
}
function clearProductsGroupTextAssessment() {
    var valueArea = $(".selectedProductDictionaryValueAssessment")[0];
    $(valueArea).find(".level-product-group-assessment").empty();
}

$(function () {

    $('#eventModalAssessment').on('show.bs.modal', function () {
        clearSearchInputsAssessment(-1, EVENT_DICTIONARY);
        clearAdditionalFilters(EVENT_DICTIONARY);
        $('#eventGroupSelectAssessment').val(null).trigger('change');
        $('#eventSmqSelectAssessment').val(null).trigger('change');
        $("ul.eventDictionaryColWidth").find('li').each(function () {
            $(this).remove()
        });
    });

    $('#productModalAssessment').on('show.bs.modal', function () {
        clearSearchInputsAssessment(-1, PRODUCT_DICTIONARY);
        clearAdditionalFilters(PRODUCT_DICTIONARY);
        if (isProductAssignment) {
            clearAllTextFromDictionary()
            var dictionary = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
            dictionary.resetDictionaryList()
        }

        $('#productGroupSelectAssessment').val(null).trigger('change');
        $('#dataSourcesProductDict option[value="jader"]').remove();
        $("ul.productDictionaryColWidthCalc").find('li').each(function () {
            $(this).remove()
        });
    });

    var currentEventDicClickedIcon , isQuantitative = false;
    var editable = ($('#editable').val() === 'true');
    if (!($("#fromTemplate").val() === "true")) {
        checkSuspectProduct();
        checkIncludeAllStudyDrugsCases();
        checkHeaderProductSelection();
    }

    function addCurrentUserAsSelectAssessment($userShareSelect2) {
        $.get(userDetailUrl, function (data) {
            var newOption = new Option(data.text, data.id, true, true);
            $userShareSelect2.append(newOption).trigger('change');
        });
    }

    $('#productSearchIconLoading').remove();
    $('#productSearchIcon').show();
    $("input:radio[name=optradio]").prop('disabled', false);
    if (hasConfigurationEditorRole != "false") {
        dictionaryBindMultipleSelect2($(".eventSmqAssessment"), getSmqDropdownListUrl, true, $.i18n._("selectOne"), EVENT_DICTIONARY, $('.selectedDatasource')); //TODO need to make it configurable.
        dictionaryBindMultipleSelect2WithUrl($("#productGroupSelectAssessment"), productGroupListUrl, true, "Select Product Group"
            ,$('#dataSourcesProductDict'));
        bindShareWith2WithDataProdGrpAssessment($('#shareWithProductGroupAssessment'), sharedWithListProductGroupUrl);
        dictionaryBindMultipleSelect2($("#eventGroupSelectAssessment"), getEventGroupDropdownListUrl, true, "Select Event Group", EVENT_DICTIONARY);
        bindShareWith2WithDataProdGrpAssessment($('#eventGroupShareWithAssessment'), getEventGroupShareWithDropdownListUrl);
    }

    $(document).on("click", '.createProductGroupAssessment',function () {
        var $productModalAssessment = $(this).parents("#productModalAssessment");
        var $productGroupModalDictAssessment = $("#productGroupModalDictAssessment");
        var multiIngredient = $productModalAssessment.find("#product_multiIngredient_assessment").is(":checked");
        var isWhoDrug = $productModalAssessment.find("#who_dr_checkbox_assessment").is(":checked");
        var columnValues = getDictionaryObjectAssessment(PRODUCT_DICTIONARY).getValues();
        var ingLength = columnValues[ingredientLevel].length
        if(columnValues[ingredientLevel] && columnValues[ingredientLevel][ingLength-1] && columnValues[ingredientLevel][ingLength-1].hasOwnProperty("isMultiIngredient") && columnValues[ingredientLevel][ingLength-1].isMultiIngredient != multiIngredient){
            $('#dictWarningModal').modal("show");
            return;
        }
        $productGroupModalDictAssessment.find('.alert').hide();
        $productGroupModalDictAssessment.find("#deleteProductGroupAssessment").hide();
        $("#productGroupDictLabelAssessment").html("Create Product Group");
        $productGroupModalDictAssessment.modal('show');
        $('#shareWithProductGroupAssessment').val(null).trigger('change');
        $productGroupModalDictAssessment.find("#productGroupIsMultiIngredientAssessment").val(multiIngredient);
        $productGroupModalDictAssessment.find("#productGroupIsWhoDrugAssessment").val(isWhoDrug);
        $("#product_multiIngredient_assessment").prop("checked",multiIngredient);
        $("#who_dr_checkbox_assessment").prop("checked",isWhoDrug);
        addCurrentUserAsSelectAssessment($('#shareWithProductGroupAssessment'));
    });

    $(document).on("click", '.updateProductGroupAssessment',function () {
        var productGroupModal = $("#productGroupModalDictAssessment");
        productGroupModal.find('.alert').hide();
        $("#productGroupModalDictAssessment").find("#deleteProductGroupAssessment").show();
        var dicObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var $productModalAssessment = $(this).parents("#productModalAssessment");
        var $productMultiIngredientAssessment = $productModalAssessment.find("#product_multiIngredient_assessment");
        var $whoDrCheckboxAssessment = $productModalAssessment.find("#who_dr_checkbox_assessment");
        var multiIngredient = $productMultiIngredientAssessment.is(":checked");
        var isWhoDrug = $whoDrCheckboxAssessment.is(":checked");
        if (dicObj.getValuesPva()[ingredientLevel] && dicObj.getValuesPva()[ingredientLevel].length > 0) {
            var lengthPva = dicObj.getValuesPva()[ingredientLevel].length
            var groupMultiIngredient = dicObj.getValuesPva()[ingredientLevel][lengthPva - 1].isMultiIngredient;
            if (groupMultiIngredient != multiIngredient) {
                $('#dictWarningModal').modal("show");
                return;
            }
        }
        if (dicObj.getValuesDicGroup().length > 1) {
            showErrorMsgInProdDict('Update group is allowed for one product group, Please select one product group to update', $('#productModal .modal-body'));
            return;
        }
        var popUp = '<i data-toggle="popover" id="groupPopUpProdGrp" data-trigger="hover" data-html="true" data-container="body" data-original-title="Details" title="">';
        popUp += '<span class="fa fa-info-circle" style="font-size: 16px; cursor: pointer"></span></i>';
        $("#productGroupDictLabelAssessment").html("Update Product Group " + popUp);
        $("#productGroupDictLabelAssessment").find('#groupPopUpProdGrp').popover({
            sanitize: false,
            html: true
        });
        var currentSelectedDicGroup = dicObj.getSelectedDicGroups()[0];
        if (productGroupDictionaryObjAssessment[0] == undefined) {
            var ele = $("#productModalAssessment").find(".closebtn").attr('data-element');
            var id = JSON.parse(ele).id
            updateProdGroupAssessment(id ,  productGroupModal,multiIngredient, isWhoDrug);
            $("#product_multiIngredient_assessment").prop("checked",multiIngredient);
        } else {
            var groupDetails = "<span> Created By: " + currentSelectedDicGroup.result.createdBy + "</span><br>";
            groupDetails += "<span> Created Date: " + currentSelectedDicGroup.result.dateCreated + "</span><br>";
            groupDetails += "<span> Last Modified By: " + currentSelectedDicGroup.result.modifiedBy + "</span><br>";
            groupDetails += "<span> Last Modified Date: " + currentSelectedDicGroup.result.lastUpdated + "</span>";
            $("#productGroupDictLabelAssessment").find('#groupPopUpProdGrp').attr('data-content', groupDetails);
            productGroupModal.modal('show');
            productGroupModal.find("#productGroupIdAssessment").val(currentSelectedDicGroup.result.id);
            productGroupModal.find("#productGroupNameAssessment").val(currentSelectedDicGroup.result.groupName);
            productGroupModal.find("#descriptionProductGroupAssessment").val(currentSelectedDicGroup.result.description);
            $("#productGroupModalDictAssessment").find("#productGroupIsWhoDrugAssessment").val(isWhoDrug);
            $("#product_multiIngredient_assessment").prop("checked",multiIngredient);
            $("#who_dr_checkbox_assessment").prop("checked",isWhoDrug);
            $('#shareWithProductGroupAssessment').find('option').remove();
            $('#shareWithProductGroupAssessment').val(null).trigger('change');
            $.each(currentSelectedDicGroup.result.sharedWithUser, function (i, data) {
                var option = new Option(data.text, data.id, true, true);
                $('#shareWithProductGroupAssessment').append(option).trigger('change');
            });
            $.each(productGroupDictionaryObjAssessment[0].result.sharedWithGroup, function (i, data) {
                var option = new Option(data.text, data.id, true, true);
                $('#shareWithProductGroupAssessment').append(option).trigger('change');
            });
        }
    });

    var updateProdGroupAssessment = function (id, productGroupModal,multiIngredient, isWhoDrug) {
        $.ajax({
            type: "POST",
            url: fetchProductGroupsUrl,
            data: {id: id},
            dataType: "json",
            async: false,
        })
            .done(function (res) {
                var groupDetails = "<span> Created By: " + res.result.createdBy + "</span><br>";
                groupDetails += "<span> Created Date: " + res.result.dateCreated + "</span><br>";
                groupDetails += "<span> Last Modified By: " + res.result.modifiedBy + "</span><br>";
                groupDetails += "<span> Last Modified Date: " + res.result.lastUpdated + "</span>";
                $("#productGroupDictLabelAssessment").find('#groupPopUpProdGrp').attr('data-content', groupDetails);
                productGroupModal.modal('show');
                productGroupModal.find("#productGroupIdAssessment").val(res.result.id);
                productGroupModal.find("#productGroupNameAssessment").val(res.result.groupName);
                productGroupModal.find("#descriptionProductGroupAssessment").val(res.result.description);
                productGroupModal.find("#productGroupIsMultiIngredientAssessment").val(multiIngredient);
                productGroupModal.find("#productGroupIsWhoDrugAssessment").val(isWhoDrug);
                $('#shareWithProductGroupAssessment').find('option').remove();
                $('#shareWithProductGroupAssessment').val(null).trigger('change');
                $.each(res.result.sharedWithUser, function (i, data) {
                    var option = new Option(data.text, data.id, true, true);
                    $('#shareWithProductGroupAssessment').append(option).trigger('change');
                });
                $.each(res.result.sharedWithGroup, function (i, data) {
                    var option = new Option(data.text, data.id, true, true);
                    $('#shareWithProductGroupAssessment').append(option).trigger('change');
                });
            });
    };

    var updateEventGroupAssessment = function (id, eventGroupModal) {
        $.ajax({
            type: "POST",
            url: fetchEventGroupsUrl,
            data: {id: id},
            dataType: "json",
            async: false,
        })
            .done(function (res) {
                var groupDetails = "<span> Created By: " + res.result.createdBy + "</span><br>";
                groupDetails += "<span> Created Date: " + res.result.dateCreated + "</span><br>";
                groupDetails += "<span> Last Modified By: " + res.result.modifiedBy + "</span><br>";
                groupDetails += "<span> Last Modified Date: " + res.result.lastUpdated + "</span>";
                $("#eventGroupLabelAssessment").find('#groupPopUp').attr('data-content', groupDetails);
                eventGroupModal.modal('show');
                eventGroupModal.find("#eventGroupIdAssessment").val(res.result.id);
                eventGroupModal.find("#eventGroupNameAssessment").val(res.result.groupName);
                eventGroupModal.find("#eventGroupDescriptionAssessment").val(res.result.description);
                $('#eventGroupShareWithAssessment').find('option').remove();
                $('#eventGroupShareWithAssessment').val(null).trigger('change');
                $.each(res.result.shareWithUser, function (i, data) {
                    var option = new Option(data.name, data.id, true, true);
                    $('#eventGroupShareWithAssessment').append(option).trigger('change');
                });
                $.each(res.result.shareWithGroup, function (i, data) {
                    var option = new Option(data, "UserGroup_"+ data, true, true);
                    $('#eventGroupShareWithAssessment').append(option).trigger('change');
                });
            });
    };

    $(document).on("click", '.createEventGroupAssessment', function () {
        $("#eventGroupModalAssessment").find('.alert').hide();
        $("#saveEventGroupAssessment").html('Save');
        $("#eventGroupModalAssessment").find("#deleteEventGroupAssessment").hide();
        $("#eventGroupLabelAssessment").html("Create Event Group");
        $("#eventGroupModalAssessment").modal('show');
        addCurrentUserAsSelectAssessment($('#eventGroupShareWithAssessment'));
    });

    $(document).on("click", '.updateEventGroupAssessment', function () {
        var dictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var eventGroupModal = $("#eventGroupModalAssessment");
        eventGroupModal.find('.alert').hide();
        eventGroupModal.find("#deleteEventGroupAssessment").show();
        if (dictionaryObj.getValuesDicGroup().length > 1) {
            showErrorMsgInProdDict('Update group is allowed for one event group, Please select one event group to update', $('#eventModalAssessment .modal-body'));
            return;
        }
        $("#saveEventGroupAssessment").html('Update');
        var popUp = '<i data-toggle="popover" id="groupPopUp" data-trigger="hover" data-html="true" data-container="body" data-original-title="Details" title="">';
        popUp += '<span class="fa fa-info-circle" style="font-size: 16px; cursor: pointer"></span></i>';
        $("#eventGroupLabelAssessment").html("Update Event Group " + popUp);
        $("#eventGroupLabelAssessment").find('#groupPopUp').popover({
            sanitize: false,
            html: true
        });
        var currentSelectedDicGroup = dictionaryObj.getSelectedDicGroups()[0];

        if (currentSelectedDicGroup == undefined) {
            var ele = $("#eventModalAssessment").find(".closebtn").attr('data-element');
            var id = JSON.parse(ele).id;
            updateEventGroupAssessment(id, eventGroupModal)
        } else {
            var groupDetails = "<span> Created By: " + currentSelectedDicGroup.result.createdBy + "</span><br>";
            groupDetails += "<span> Created Date: " + currentSelectedDicGroup.result.dateCreated + "</span><br>";
            groupDetails += "<span> Last Modified By: " + currentSelectedDicGroup.result.modifiedBy + "</span><br>";
            groupDetails += "<span> Last Modified Date: " + currentSelectedDicGroup.result.lastUpdated + "</span>";
            $("#eventGroupLabelAssessment").find('#groupPopUp').attr('data-content', groupDetails);
            eventGroupModal.modal('show');
            eventGroupModal.find("#eventGroupIdAssessment").val(currentSelectedDicGroup.result.id);
            eventGroupModal.find("#eventGroupNameAssessment").val(currentSelectedDicGroup.result.groupName);
            eventGroupModal.find("#eventGroupDescriptionAssessment").val(currentSelectedDicGroup.result.description);
            $('#eventGroupShareWithAssessment').find('option').remove();
            $.each(currentSelectedDicGroup.result.sharedWithUser, function (i, data) {
                var option = new Option(data.text, data.id, true, true);
                $('#eventGroupShareWithAssessment').append(option).trigger('change');
            });
            $.each(currentSelectedDicGroup.result.sharedWithGroup, function (i, data) {
                var option = new Option(data.text, data.id, true, true);
                $('#eventGroupShareWithAssessment').append(option).trigger('change');
            });
        }
    });

    var isDuplicateProdValues = function (dictionaryValues, checkedDictionaryValue, level) {
        var productDictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var key = productDictionaryObj.dictionaryType == PRODUCT_DICTIONARY ? level : checkedDictionaryValue.level;
        if (dictionaryValues[key] != undefined && dictionaryValues[key].length > 0) {
            for (var i = 0; i < dictionaryValues[key].length; i++) {
                if (dictionaryValues[key][i].id == checkedDictionaryValue.id)
                    return true;
            }
        }
        return false;
    };

    var isDuplicateEventValues = function (dictionaryValues, checkedDictionaryValue, level) {
        var eventDictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var key = eventDictionaryObj.dictionaryType == EVENT_DICTIONARY ? level : checkedDictionaryValue.level;
        if (dictionaryValues[key] != undefined && dictionaryValues[key].length > 0) {
            for (var i = 0; i < dictionaryValues[key].length; i++) {
                if (dictionaryValues[key][i].id == checkedDictionaryValue.id)
                    return true;
            }
        }
        return false;
    };

    $(document).on('click', '.showAllProductsAssessment', function (evt) {
        console.log("showing-----");
        $.ajax({
            type: "POST",
            url: showAllProductsUrl,
            data: {id: $(this).attr('data-id')},
            dataType: "json",
        })
            .done(function (res) {
                isShowAllClicked = true;
                var obj = JSON.parse(res.result.data);
                var productDictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
                if (obj.pva != undefined && Object.keys(obj.pva).length > 0) {
                    var productGroupValuesPva = obj.pva;
                    var multiIngredient = productGroupValuesPva.isMultiIngredient
                    delete productGroupValuesPva.isMultiIngredient;
                    $.each(productGroupValuesPva, function (key, value) {
                        var nonEmptyValues;
                        if (value.length != 0) {
                            nonEmptyValues = value;
                            $.each(nonEmptyValues, function (k, v) {
                                var selectedObj = {"name": v.name, "id": v.id, "level": key};
                                if (!isDuplicateProdValues(productDictionaryObj.getValuesPva(), selectedObj)) {
                                    if (key == ingredientLevel) {
                                        if (multiIngredient == undefined) {
                                            productDictionaryObj.getValuesPva()[key].push({name: v.name, id: v.id, isMultiIngredient: false});
                                        } else {
                                            productDictionaryObj.getValuesPva()[key].push({name: v.name, id: v.id, isMultiIngredient: multiIngredient == "true"});
                                        }
                                    } else if (key == ASSESSMENT_DRUG_REC_NUM_LEVEL) {
                                        productDictionaryObj.getValuesPva()[key].push({name: v.name, id: v.id, drugRecordNumber: v.drugRecordNumber});
                                    } else {
                                        productDictionaryObj.getValuesPva()[key].push({name: v.name, id: v.id});
                                    }
                                }
                            });
                        }
                    });
                    if(multiIngredient == undefined)
                        productDictionaryObj.getValuesPva().isMultiIngredient = false;
                    else
                        productDictionaryObj.getValuesPva().isMultiIngredient = multiIngredient;
                    var dictionaryValues = $(".productDictionaryValueAssessment").find('span[title="Safety DB"]');
                    _.each(dictionaryValues, function (div) {
                        div.remove();
                    });
                    productDictionaryObj.setColumnViewText(productDictionaryObj.getValuesPva(), "pva",true);
                }
                if (obj.faers != undefined && Object.keys(obj.faers).length > 0) {
                    var productGroupValuesFaers = obj.faers;
                    $.each(productGroupValuesFaers, function (key, value) {
                        var nonEmptyValues;
                        if (value.length != 0) {
                            nonEmptyValues = value;
                            $.each(nonEmptyValues, function (k, v) {
                                var selectedObj = {"name": v.name, "id": v.id, "level": key};
                                if (!isDuplicateProdValues(productDictionaryObj.getValuesFaers(), selectedObj)) {
                                    productDictionaryObj.getValuesFaers()[key].push({name: v.name, id: v.id});
                                }

                            });
                        }
                    });
                    var dictionaryValues = $(".productDictionaryValueAssessment").find('span[title="FAERS"]');
                    _.each(dictionaryValues, function (div) {
                        div.remove();
                    });
                    productDictionaryObj.setColumnViewText(productDictionaryObj.getValuesFaers(), "faers",true);
                }
                if (obj.vaers != undefined && Object.keys(obj.vaers).length > 0) {
                    var productGroupValuesVaers = obj.vaers;
                    $.each(productGroupValuesVaers, function (key, value) {
                        var nonEmptyValues;
                        if (value.length != 0) {
                            nonEmptyValues = value;
                            $.each(nonEmptyValues, function (k, v) {
                                var selectedObj = {"name": v.name, "id": v.id, "level": key};
                                if (!isDuplicateProdValues(productDictionaryObj.getValuesVaers(), selectedObj)) {
                                    productDictionaryObj.getValuesVaers()[key].push({name: v.name, id: v.id});
                                }
                            });
                        }
                    });
                    var dictionaryValues = $(".productDictionaryValueAssessment").find('span[title="VAERS"]');
                    _.each(dictionaryValues, function (div) {
                        div.remove();
                    });
                    productDictionaryObj.setColumnViewText(productDictionaryObj.getValuesVaers(), "vaers", true);
                }
                if (obj.eudra != undefined && Object.keys(obj.eudra).length > 0) {
                    var productGroupValuesEudra = obj.eudra;
                    $.each(productGroupValuesEudra, function (key, value) {
                        var nonEmptyValues;
                        if (value.length != 0) {
                            nonEmptyValues = value;
                            $.each(nonEmptyValues, function (k, v) {
                                var selectedObj = {"name": v.name, "id": v.id, "level": key};
                                if (!isDuplicateProdValues(productDictionaryObj.getValuesEudra(), selectedObj)) {
                                    productDictionaryObj.getValuesEudra()[key].push({name: v.name, id: v.id});
                                }

                            });
                        }
                    });
                    var dictionaryValues = $(".productDictionaryValueAssessment").find('span[title="EVDAS"]');
                    _.each(dictionaryValues, function (div) {
                        div.remove();
                    });
                    productDictionaryObj.setColumnViewText(productDictionaryObj.getValuesEudra(), "eudra",true);
                }

                if (obj.vigibase != undefined && Object.keys(obj.vigibase).length > 0) {
                    var productGroupValuesVigibase = obj.vigibase;
                    $.each(productGroupValuesVigibase, function (key, value) {
                        var nonEmptyValues;
                        if (value.length != 0) {
                            nonEmptyValues = value;
                            $.each(nonEmptyValues, function (k, v) {
                                var selectedObj = {"name": v.name, "id": v.id, "level": key};
                                if (!isDuplicateProdValues(productDictionaryObj.getValuesVigibase(), selectedObj)) {
                                    productDictionaryObj.getValuesVigibase()[key].push({name: v.name, id: v.id});
                                }
                            });
                        }
                    });
                    var dictionaryValues = $(".productDictionaryValueAssessment").find('span[title="VigiBase"]');
                    _.each(dictionaryValues, function (div) {
                        div.remove();
                    });
                    productDictionaryObj.setColumnViewText(productDictionaryObj.getValuesVigibase(), "vigibase", true);
                }
                if (res.result.canEdit)
                    $('.updateProductGroupAssessment').attr('disabled', false);
                else
                    $('.updateProductGroupAssessment').attr('disabled', true);
                productDictionaryObj.addToSelectedDicGroups(res);
                console.log("productGroupDictionaryObjAssessment::::::: ",productGroupDictionaryObjAssessment)
            });
    });

    $(document).on('click', '.showAllEventsAssessment', function (evt) {
        $.ajax({
            type: "POST",
            url: showAllEventsUrl,
            data: {id: $(this).attr('data-id')},
            dataType: "json",
        })
            .done(function (res) {
                var obj = JSON.parse(res.result.data);
                var eventDictionaryObject = getDictionaryObjectAssessment(EVENT_DICTIONARY);
                if (obj.pva != undefined && Object.keys(obj.pva).length > 0) {
                    var eventPVAValues = eventDictionaryObject.getValuesPva();
                    var eventGroupValuesPva = obj.pva;
                    $.each(eventGroupValuesPva, function (key, value) {
                        var nonEmptyValues;
                        if (value.length != 0) {
                            nonEmptyValues = value;
                            $.each(nonEmptyValues, function (k, v) {
                                var selectedObj = {"name": v.name, "id": v.id, "level": key};
                                if (!isDuplicateEventValues(eventPVAValues, selectedObj, key)) {
                                    if (eventPVAValues[key] == null) {
                                        eventPVAValues[key] = [];
                                    }
                                    eventPVAValues[key].push({name: v.name, id: v.id});
                                }
                            });
                        }
                    });
                    var dictionaryValues = $(".eventDictionaryValueAssessment").find('span');
                    _.each(dictionaryValues, function (div) {
                        div.remove();
                    });
                    eventDictionaryObject.setColumnViewText(eventPVAValues, "pva" , true);
                }
                if (res.result.canEdit)
                    $('.updateEventGroupAssessment').attr('disabled', false);
                else
                    $('.updateEventGroupAssessment').attr('disabled', true);
                eventDictionaryObject.addToSelectedDicGroups(res);
            });
    });

    function bindShareWith2WithDataProdGrpAssessment(selector, sharedWithListUrl, sharedWithData) {
        selector.select2({
            minimumInputLength: 0,
            multiple: true,
            placeholder: 'Select Share With',
            width: "100%",
            separator: ";",
            ajax: {
                quietMillis: 250,
                dataType: "json",
                url: sharedWithListUrl,
                data: function (params) {
                    return {
                        term: params.term,
                        max: params.page || 10,
                        page: params.page,
                        lang: userLocale
                    };
                },
                processResults: function (data, params) {
                    params.page = params.page || 10;
                    return {
                        results: data.items,
                        pagination: {
                            more: (params.page * 10) < data.items.length
                        }
                    };
                }
            },
            escapeMarkup: function (m) {
                return m;
            }
        });
    }

    $(document).on('click', '.clearEventValuesAssessment', function () {
        clearDictionaryModal(EVENT_DICTIONARY);
        getDictionaryObjectAssessment(EVENT_DICTIONARY).isCloseDictionary = true
    });

    $(document).on('click', '.clearProductValuesAssessment', function () {
        $("input.searchProducts").each(function () {
            $(this).val("")
        });
        clearDictionaryModal(PRODUCT_DICTIONARY);
        getDictionaryObjectAssessment(PRODUCT_DICTIONARY).isCloseDictionary = true
    });


    $(document).on('click', '.addProductValuesAssessment', function () {
        var productOptions = $(this).parents("#productModalAssessment").find('.dicLi.selectedBackground');
        var multiIngredient = $(this).parents("#productModalAssessment").find("#product_multiIngredient_assessment").is(":checked");
        var dictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var columnValues = getDictionaryObjectAssessment(PRODUCT_DICTIONARY).getValuesPva();
        var enableDisableProductGroup = dictionaryObj.getValues()
        var dictionaryObjproduct = getDictionaryObject(PRODUCT_DICTIONARY);
        var selectedProductGroup = $('#productGroupSelectAssessment').select2('data');
        var selectedSameMultiIng = true;
        var ingLength = columnValues[ingredientLevel].length
        var hasMI;
        if(columnValues[ingredientLevel][ingLength - 1]) {
            if(columnValues[ingredientLevel][ingLength - 1].hasOwnProperty("isMultiIngredient")){
                hasMI = columnValues[ingredientLevel][ingLength - 1].isMultiIngredient;
            } else {
                hasMI = false
            }
        } else if (dictionaryObj.getValuesDicGroup().length != 0) {
            hasMI = dictionaryObj.getValuesDicGroup()[0].isMultiIngredient;
        } else {
            hasMI = multiIngredient;
        }
        if (selectedProductGroup.length > 0 && productOptions.length > 0){
            if(selectedProductGroup[0].hasOwnProperty("isMultiIngredient")){
                selectedSameMultiIng = (selectedProductGroup[0].isMultiIngredient == multiIngredient)
            }
            else{
                selectedSameMultiIng = (multiIngredient == false)
            }
        }
        if (getDataSource(PRODUCT_DICTIONARY) == "eudra" && productOptions.length  && !checkEmptyValuesForAddAllAssessment(getDictionaryObjectAssessment(PRODUCT_DICTIONARY).getValuesEudra())) {
            showErrorMsgInProdDict("Selection of multiple ingredients for EVDAS data source is not allowed", $('#productModal .modal-body'));
            return;
        }
        if((hasMI != multiIngredient || !selectedSameMultiIng) && dictionaryObjproduct.currentSelectedLevel == ingredientLevel){
            $('#dictWarningModal').modal("show");
        } else {
            var $who_dr_checkbox_assessment = $("#who_dr_checkbox_assessment");
            if (selectedProductGroup.length > 0 && selectedProductGroup[0].hasOwnProperty(ASSESSMENT_INCLUDE_WHO_KEY) && selectedProductGroup[0][ASSESSMENT_INCLUDE_WHO_KEY]) {
                $who_dr_checkbox_assessment.prop("checked", true).trigger("change");
            }
            dictionaryObj.addAllOptions(productOptions, multiIngredient, ingredientLevel);
            dictionaryObj.setProductGroupText();
            if ($who_dr_checkbox_assessment.is(":checked") && getDataSource(PRODUCT_DICTIONARY) === ASSESSMENT_DATASOURCE_PVA) {
                setTimeout(function () {
                    fetchAndUpdateDrugRecordNumbersAssessment(dictionaryObj);
                });
            }
            enableDisableProductGroupButtonsAssessment(enableDisableProductGroup);
        }
    });

    $(document).on('click', '.addAllProductValuesAssessment', function () {
        var dictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var multiIngredient = $(this).parents("#productModalAssessment").find("#product_multiIngredient_assessment").is(":checked");
        var columnValues = getDictionaryObjectAssessment(PRODUCT_DICTIONARY).getValuesPva();
        var ingLength = columnValues[ingredientLevel].length
        var enableDisableProductGroup = dictionaryObj.getValues()
        var hasMI
        var selectedProductGroup = $('#productGroupSelectAssessment').select2('data');
        var selectedSameMultiIng = true;
        if(columnValues[ingredientLevel][ingLength - 1]) {
            if(columnValues[ingredientLevel][ingLength - 1].hasOwnProperty("isMultiIngredient")){
                hasMI = columnValues[ingredientLevel][ingLength - 1].isMultiIngredient;
            } else {
                hasMI = false
            }
        } else if (dictionaryObj.getValuesDicGroup().length != 0) {
            hasMI = dictionaryObj.getValuesDicGroup()[0].isMultiIngredient;
        } else {
            hasMI = multiIngredient;
        }
        var productOptions = $(this).parents("#productModalAssessment").find('[class*="dicLi"][dictionarylevel="' + productDictionaryObj.currentSelectedLevel + '"]');
        if (selectedProductGroup.length > 0 && productOptions.length > 0 ){
            if(selectedProductGroup[0].hasOwnProperty("isMultiIngredient")){
                selectedSameMultiIng = (selectedProductGroup[0].isMultiIngredient == multiIngredient)
            }
            else{
                selectedSameMultiIng = (multiIngredient == false)
            }
        }
        var dictionaryObjproduct = getDictionaryObject(PRODUCT_DICTIONARY);
        if (getDataSource(PRODUCT_DICTIONARY) == "eudra" && (productOptions.length > 1 || !checkEmptyValuesForAddAllAssessment(getDictionaryObjectAssessment(PRODUCT_DICTIONARY).getValuesEudra()))) {
            showErrorMsgInProdDict("Selection of multiple ingredients for EVDAS data source is not allowed", $('#productModal .modal-body'));
        } else {
            if((hasMI != multiIngredient || !selectedSameMultiIng) && dictionaryObjproduct.currentSelectedLevel == ingredientLevel){
                $('#dictWarningModal').modal("show");
            } else {
                var $who_dr_checkbox_assessment = $('#who_dr_checkbox_assessment')
                if(selectedProductGroup.length > 0 && selectedProductGroup[0].hasOwnProperty("includeWHODrugs") && selectedProductGroup[0]['includeWHODrugs']){
                    $who_dr_checkbox_assessment.prop('checked',true).trigger('change');
                }
                dictionaryObj.addAllOptions(productOptions, multiIngredient, ingredientLevel);
                dictionaryObj.setProductGroupText();
                if($who_dr_checkbox_assessment.is(':checked') && getDataSource(PRODUCT_DICTIONARY) === ASSESSMENT_DATASOURCE_PVA){
                    setTimeout(function() {
                        fetchAndUpdateDrugRecordNumbersAssessment(dictionaryObj);
                    });
                }
                enableDisableProductGroupButtonsAssessment(enableDisableProductGroup);
            }
        }
    });

    $(document).on('click', '.addEventValuesAssessment', function () {
        var reportFieldLevel = getLevelForCurrentSelection(currentEventDicClickedIcon);
        var eventOptions = $(this).parents("#eventModalAssessment").find('.dicLi.selectedBackground');
        $('.errorMessage').hide();
        if (reportFieldLevel != undefined) {
            for (var i = 0; i < eventOptions.length; i++) {
                if ($(eventOptions[i]).attr("dictionarylevel") != reportFieldLevel) {
                    $('.errorMessage').show();
                    return;
                }
            }
        }
        addSmqValues(reportFieldLevel);
        addEventGroupValues();
        getDictionaryObjectAssessment(EVENT_DICTIONARY).addAllOptions(eventOptions);
        enableDisableEventGroupButtonsAssessment();
    });

    $(document).on('click', '.addAllEventsAssessment', function () {
        updateValuesWithCurrentSelectedEventDicData(currentEventDicClickedIcon);
    });

    $(document).on('click', '.addAllEventValuesAssessment', function () {
        var reportFieldLevel = getLevelForCurrentSelection(currentEventDicClickedIcon);
        var dictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var eventOptions = $(this).parents("#eventModalAssessment").find('[class*="dicLi"][dictionarylevel="' + eventDictionaryObj.currentSelectedLevel + '"]');
        $('.errorMessage').hide();
        if (reportFieldLevel != undefined) {
            for (var i = 0; i < eventOptions.length; i++) {
                if ($(eventOptions[i]).attr("dictionarylevel") != reportFieldLevel) {
                    $('.errorMessage').show();
                    return;
                }
            }
        }
        addSmqValues(reportFieldLevel);
        addEventGroupValues();
        dictionaryObj.addAllOptions(eventOptions);
        enableDisableEventGroupButtonsAssessment();
    });

    function addSmqValues(reportFieldLevel) {
        var smqList = $("#eventSmqSelectAssessment").select2("data");
        if (smqList && smqList.length > 0) {
            if ((reportFieldLevel != undefined) && _.find(smqList, function (it) {
                    return reportFieldLevel != (smq.type === SMQBROAD ? 7 : 8)
                })) {
                $('.errorMessage').show();
                return;
            }
            var dictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
            var values = dictionaryObj.getValues();
            var valuesPva = dictionaryObj.getValuesPva();
            if (values[7] == null) values[7] = [];
            if (valuesPva[7] == null) valuesPva[7] = [];
            if (values[8] == null) values[8] = [];
            if (valuesPva[8] == null) valuesPva[8] = [];
            for (var i = 0; i < smqList.length; i++) {
                var smq = smqList[i];
                var level = (smq.type == SMQBROAD ? 7 : 8);
                if (!_.find(valuesPva[level], function (it) {
                    return it.id == smq.code
                })) {
                    var selectedObj = {"name": smq.name, "id": smq.code};
                    valuesPva[level].push(selectedObj);
                    values[level].push(selectedObj);
                    dictionaryObj.setDictionaryLevelText(level, selectedObj)
                }
            }
            dictionaryObj.setValuesPva(valuesPva);
            dictionaryObj.setValues(values);
        }
    }

    function addEventGroupValues(eventGroupList) {
        if (!eventGroupList || eventGroupList == undefined) {
            eventGroupList = $("#eventGroupSelectAssessment").select2("data");
        }
        showEventGroupValues(eventGroupList);
    }

    function showEventGroupValues(eventGroupList) {
        var valueArea = $(".selectedEventDictionaryValueAssessment")[0];
        var textDiv1 = $(valueArea).find(".level-event-group-assessment");
        if (eventGroupList && eventGroupList.length > 0) {
            var dictionaryObject = getDictionaryObjectAssessment(EVENT_DICTIONARY);
            var values = dictionaryObject.getValuesDicGroup();
            if (values == null) values = [];
            for (var i = 0; i < eventGroupList.length; i++) {
                var eventGroup = eventGroupList[i];
                if (!checkIfEventGroupObjectExist(values, eventGroup.id)) {
                    var $elem1 = $("<span class='eventGroupValues'><span class='dictionaryItem'>" + eventGroup.name + "<span class='closebtn removeSingleDictionaryEventValueAssessment'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllEventsAssessment btn-show-events" data-id="' + eventGroup.id + '">' + '</i></span>');
                    var $closeButton = $elem1.find(".closebtn");
                    $closeButton.attr("data-type", dictionaryObject.dictionaryType);
                    $closeButton.attr("data-element", JSON.stringify({name: eventGroup.name, id: eventGroup.id}));

                    var selectedObj = {"name": eventGroup.name, "id": eventGroup.id};
                    values.push(selectedObj);
                    $(textDiv1).append($elem1);
                }
            }
            dictionaryObject.setValuesDicGroup(values);
        }
    }

    function addproductGroupValues(productGroupList) {
        if (!productGroupList || productGroupList == undefined) {
            productGroupList = $("#productGroupSelectAssessment").select2("data");
        }
        showproductGroupValues(productGroupList);
    }

    function showproductGroupValues(productGroupList) {
        var valueArea = $(".selectedProductDictionaryValueAssessment")[0];
        var textDiv1 = $(valueArea).find(".level-product-group-assessment");
        if (productGroupList && productGroupList.length > 0) {
            var dictionaryObject = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
            var values = dictionaryObject.getProductGroupValues();
            if (values == null) values = [];
            // clearProductsGroupText();
            for (var i = 0; i < productGroupList.length; i++) {
                var productGroup = productGroupList[i];
                if (!checkIfProductGroupObjectExist(values, productGroup.id)) {
                    productGroupDataSources.push(value.text.substring(value.text.indexOf('Data Source:')).substring(12));
                    var $elem1 = $("<span class='productGroupValues'><span class='dictionaryItem'>" + productGroup.text + "<span class='closebtn removeSingleDictionaryProductValueAssessment'>×</span></span>" + '<i class="fa fa-pencil-square-o showAllProductsAssessment btn-show-products" data-id="' + productGroup.id + '">' + '</i></span>');
                    var $closeButton = $elem1.find(".closebtn");
                    $closeButton.attr("data-type", dictionaryObject.dictionaryType);
                    $closeButton.attr("data-element", JSON.stringify({name: productGroup.text, id: productGroup.id}));

                    var selectedObj = {"name": productGroup.text, "id": productGroup.id};
                    values.push(selectedObj);
                    $(textDiv1).append($elem1);
                }
            }
            dictionaryObject.setProductGroupValues(values);
        }
    }

    $(document).on('click', '.removeSingleDictionaryEventValueAssessment', function (event) {
        var dictionary = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var $this = $(this);
        var toRemove = JSON.parse($this.attr("data-element"));
        $(this).closest('.eventGroupValues').remove();
        dictionary.setValuesDicGroup(removeValEventGrp(dictionary.getValuesDicGroup(), toRemove));
        dictionary.setSelectedDicGroups(removeSingleGrp(dictionary.getSelectedDicGroups(), toRemove));
        enableDisableEventGroupButtonsAssessment();
    });

    var removeValEventGrp = function (dict, val) {
        return _.filter(dict, function (el) {
            if (el["id"] && val["id"]) {
                if (el["id"] === val["id"]) return false;
            } else {
                if (el["name"] === val["name"]) return false;
            }
            return true;
        });
    };

    var checkIfEventGroupObjectExist = function (values, id) {
        for (var i = 0; i < values.length; i++) {
            if (values[i].id === id)
                return true
        }
        return false
    };

    var checkIfProductGroupObjectExist = function (values, id) {
        for (var i = 0; i < values.length; i++) {
            if (values[i].id === id)
                return true
        }
        return false
    };

    var showErrorMsgInProdDict = function(message, modal) {
        var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
        errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
        errorMsg += '<span aria-hidden="true">&times;</span> ';
        errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
        errorMsg += '</button> ' + message;
        errorMsg += '</div>';
        modal.prepend(errorMsg);
        setTimeout(function () {
            addHideClass($(".alert-danger"));
        }, 5000);
    };
    /**
     * 20000 ms error message is displayed
     * @param message text to be displayed
     * @param modal selector where text needs to be displayed
     */
    var showErrorMsgInProdDictLong = function(message, modal) {
        var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ';
        errorMsg += '<button type="button" class="close" data-dismiss="alert"> ';
        errorMsg += '<span aria-hidden="true">&times;</span> ';
        errorMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
        errorMsg += '</button> ' + message;
        errorMsg += '</div>';
        var $errorMessage = $(errorMsg);  // Converted to jQuery object
        modal.prepend($errorMessage);
        setTimeout(function () {
            addHideClass($errorMessage);
        }, 20000);
    };

    var addHideClass = function(row) {
        row.remove();
    };

    $(document).on('click', '.addAllProductsAssessment', function () {
        var dictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
        var $isMultiIngredientAssessment = $('#isMultiIngredientAssessment');
        var $who_dr_checkbox_assessment = $('#who_dr_checkbox_assessment');
        var pvaValues = dictionaryObj.getValuesPva();
        var selectedProductsSources = $.unique($.map($('#productModalAssessment [data-newproduct="new"]'), function (n, i) {
            return $(n).data('source')
        }));

        var selectedProductsSourcesFromGroup = $.unique($.map($('#productModalAssessment .productDictionaryValueAssessment .dictionaryItem'), function (n, i) {
            return $(n).prop('title')
        }));
        if (pvaValues[ingredientLevel].length > 0) {
            var ingredientArray = pvaValues[ingredientLevel];
            if (ingredientArray[0] && ingredientArray[0].hasOwnProperty('isMultiIngredient')) {
                var isMultiIngredient = ingredientArray[0].isMultiIngredient;
                $isMultiIngredientAssessment.val(isMultiIngredient);
            }
        } else if (dictionaryObj.getValuesDicGroup().length != 0 && dictionaryObj.getValuesDicGroup()[0].isMultiIngredient) {
            $isMultiIngredientAssessment.val(dictionaryObj.getValuesDicGroup()[0].isMultiIngredient);
        } else {
            $isMultiIngredientAssessment.val("false");
        }
        // Check who drug checkbox and trigger change according to
        if (pvaValues.hasOwnProperty(ASSESSMENT_DRUG_REC_NUM_LEVEL) && pvaValues[ASSESSMENT_DRUG_REC_NUM_LEVEL].length > 0) {
            var ingredientArray = pvaValues[ASSESSMENT_DRUG_REC_NUM_LEVEL];
            if (ingredientArray[0] && ingredientArray[0].hasOwnProperty(ASSESSMENT_DRUG_RECORD_KEY)) {
                $isMultiIngredientAssessment.val("true");
            }
        } else if (dictionaryObj.getValuesDicGroup().length != 0 && dictionaryObj.getValuesDicGroup()[0].hasOwnProperty(ASSESSMENT_INCLUDE_WHO_KEY) && dictionaryObj.getValuesDicGroup()[0].includeWHODrugs) {
            $isMultiIngredientAssessment.val("true");
        }
        var prodDicValues = {};
        var count = 0;
        if (checkEmptyValues(dictionaryObj.getValuesPva())) {
            count++;
            prodDicValues = dictionaryObj.getValuesPva();
        }

        if (checkEmptyValues(dictionaryObj.getValuesFaers())) {
            count++;
            prodDicValues = dictionaryObj.getValuesFaers();
        }

        if (checkEmptyValues(dictionaryObj.getValuesVaers())) {
            prodDicValues = dictionaryObj.getValuesVaers();
        }

        if (checkEmptyValues(dictionaryObj.getValuesVigibase())) {
            prodDicValues = dictionaryObj.getValuesVigibase();
        }

        if (checkEmptyValues(dictionaryObj.getValuesEudra())) {
            count++;
            prodDicValues = dictionaryObj.getValuesEudra();
        }
        if (!isWhoDictionary && pvaValues.hasOwnProperty(ASSESSMENT_DRUG_REC_NUM_LEVEL) && pvaValues[ASSESSMENT_DRUG_REC_NUM_LEVEL].length > 0) {
            $who_dr_checkbox_assessment.prop('checked', false).trigger('change');
            showErrorMsgInProdDictLong($.i18n._('whoDictionaryNotAvailable'), $('#productModalAssessment .modal-body'));
        } else if (!isProductAssignment && isEmerging && !dictionaryObj.getValuesDicGroup().length && selectedProductsSourcesFromGroup.length && selectedProductsSourcesFromGroup.length > 1) {
            showErrorMsgInProdDict($.i18n._('multiDataSourceProductGroup'), $('#productModalAssessment .modal-body'));
        } else if (!isProductAssignment && isEmerging && dictionaryObj.getValuesDicGroup().length && (!dictionaryObj.isCloseDictionary || selectedProductsSources.length)) {
            showErrorMsgInProdDict($.i18n._('unsavedProductGroup'), $('#productModalAssessment .modal-body'));
        } else {
            $('#isAssessmentDicitionary').val('false');
            if (dictionaryObj.getValuesDicGroup().length) {

                if (!isProductAssignment) {
                    $("#productSelectionAssessment").val(checkEmptyValues(dictionaryObj.getValues())).trigger("change");
                    $("#productGroupSelectionAssessment").val(JSON.stringify(dictionaryObj.getValuesDicGroup())).trigger("change");
                    showDictionaryValuesAssessment(document.getElementById("showProductSelectionAssessment"), dictionaryObj.getValues(), PRODUCT_DICTIONARY, dictionaryObj.getValuesDicGroup(), dictionaryObj.getLevels(), editable);
                } else {
                    if (prodDicValues.hasOwnProperty("isMultiIngredient")) {
                        delete prodDicValues.isMultiIngredient
                    }
                    currentEditingRow.find("#productSelectionAssessment").val(checkEmptyValues(dictionaryObj.getValues())).trigger("change");
                    currentEditingRow.find("#productGroupSelectionAssessment").val(JSON.stringify(dictionaryObj.getValuesDicGroup())).trigger("change");
                    currentEditingRow.find("#showProductSelectionAssessment").html("");
                    showDictionaryValuesAssessment(currentEditingRow.find("#showProductSelectionAssessment"), dictionaryObj.getValues(), PRODUCT_DICTIONARY, dictionaryObj.getValuesDicGroup(), dictionaryObj.getLevels(), editable);
                }
                checkSuspectProduct();
                checkIncludeAllStudyDrugsCases();
                checkHeaderProductSelection();
                $("#productModalAssessment").modal('hide');
            } else {
                if (!isProductAssignment) {
                    $("#productSelectionAssessment").val(checkEmptyValues(prodDicValues)).trigger("change");
                    $("#productGroupSelectionAssessment").val(JSON.stringify(dictionaryObj.getValuesDicGroup())).trigger("change");
                    showDictionaryValuesAssessment(document.getElementById("showProductSelectionAssessment"), prodDicValues, PRODUCT_DICTIONARY, dictionaryObj.getValuesDicGroup(), dictionaryObj.getLevels(), editable);
                } else {
                    currentEditingRow.find("#productSelectionAssessment").val(checkEmptyValues(prodDicValues)).trigger("change");
                    currentEditingRow.find("#productGroupSelectionAssessment").val(JSON.stringify(dictionaryObj.getValuesDicGroup())).trigger("change");
                    currentEditingRow.find("#showProductSelectionAssessment").html("");
                    showDictionaryValuesAssessment(currentEditingRow.find("#showProductSelectionAssessment"), prodDicValues, PRODUCT_DICTIONARY, dictionaryObj.getValuesDicGroup(), dictionaryObj.getLevels(), editable);
                }
                checkSuspectProduct();
                checkIncludeAllStudyDrugsCases();
                checkHeaderProductSelection();
                $("#productModalAssessment").modal('hide');
            }
        }
    });

    $("#productSelectionAssessment").on('change', function () {
        checkSuspectProduct();
        checkIncludeAllStudyDrugsCases();
        checkHeaderProductSelection();
    });


    $('#productModalAssessment').on('shown.bs.modal', function (e) {
        if(isProductAssignment){
            loadValuesToDictionary();
        }
        const assessmentDataSource = $('#clickedDatasource').val() ? $('#clickedDatasource').val() : ""
        if (getDataSource(PRODUCT_DICTIONARY) === ASSESSMENT_DATASOURCE_PVA || assessmentDataSource.includes(ASSESSMENT_DATASOURCE_PVA)) {
            var $whoDrCheckboxDivAssessment = $('#who_dr_checkbox_div_assessment');
            var $whoDrCheckboxAssessment = $('#who_dr_checkbox_assessment');
            $whoDrCheckboxDivAssessment.show();

            if ($whoDrCheckboxAssessment.is(':checked')) {
                $whoDrCheckboxAssessment.prop('checked', true).trigger('change');
            }
            var dictionary = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
            var dictionaryValues = dictionary.getValues();
            var dictionaryValuesDicGroup = dictionary.getValuesDicGroup();
            if ((dictionaryValues.hasOwnProperty(ASSESSMENT_DRUG_REC_NUM_LEVEL) && dictionaryValues[ASSESSMENT_DRUG_REC_NUM_LEVEL].length > 0) ||
                (dictionaryValuesDicGroup.length > 0 && dictionaryValuesDicGroup[0].includeWHODrugs)) {
                $whoDrCheckboxAssessment.prop('checked', true).trigger('change');
            }
        } else {
            $('#drugRecordSelection, #drugRecordSelectionAssessment, #who_dr_checkbox_div_assessment').hide();
        }
        var allowedSources = $(e.relatedTarget).data('allowed-sources-product');
        isQuantitative = $(e.relatedTarget).data('is-quantitative');
        if (allowedSources) {
            var dictionary = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
            dictionary.setAllowedDataSourcesProduct(allowedSources.split(','));
        }
     });



    $('#dataSourcesProductDictAssessment').on('change', function () {
        var $who_dr_checkbox_div = $('#who_dr_checkbox_div_assessment');
        var $product_multiIngredient_assessment = $("#product_multiIngredient_assessment")
        if (this.value === "faers") {
            disableDictionaryProductGroupValues(true, true, false, false);
            $(".repeat-container .dropdown-menu [data-value='hourly']").hide()
            $product_multiIngredient_assessment.hide();
            $product_multiIngredient_assessment.next("label").hide();
        } else if (this.value === "vaers") {
            disableDictionaryProductGroupValues(true, true, false, true);
            $(".repeat-container .dropdown-menu [data-value='hourly']").show()
            $product_multiIngredient_assessment.hide();
            $product_multiIngredient_assessment.next("label").hide();
        } else if (this.value === "eudra") {
            disableDictionaryProductGroupValues(true, true, true, false);
            $(".repeat-container .dropdown-menu [data-value='hourly']").show()
            $product_multiIngredient_assessment.hide();
            $product_multiIngredient_assessment.next("label").hide();
        } else if (this.value === "vigibase") {
            disableDictionaryProductGroupValues(true, true, false, false);
            $(".repeat-container .dropdown-menu [data-value='hourly']").show()
            $product_multiIngredient_assessment.hide();
            $product_multiIngredient_assessment.next("label").hide();
        } else {
            disableDictionaryProductGroupValues(false, false, false, false);
            $product_multiIngredient_assessment.show();
            $product_multiIngredient_assessment.next("label").show();
            $who_dr_checkbox_div.show();
            if ($('#who_dr_checkbox').is(':checked')) {
                $('#drugRecordSelection').show();
                $('#drugRecordSelectionAssessment').show();
            }
        }
    });

    function disableDictionaryProductGroupValues(family, trade, productName, ingredient) {
        $(".prodDictFilterColCalc label:contains('Family')").next("input").prop("disabled", family);
        $(".prodDictFilterColCalc label:contains('Trade Name')").next("input").prop("disabled", trade);
        $(".prodDictFilterColCalc label:contains('Product Name')").next("input").prop("disabled", productName);
        $(".prodDictFilterColCalc label:contains('Ingredient')").next("input").prop("disabled", ingredient);
    };

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

    function checkEmptyValuesForAddAllAssessment(values) {
        var result = '';
        var emptyList = true;
        for (var key in values) {
            if (values.hasOwnProperty(key)) {
                if (values[key].length > 0) {
                    emptyList = false;
                }
            }
        }
        return emptyList;
    }


    function loadValuesToDictionary() {
        // For edit/view page
        var $whoDrCheckboxAssessment = $('#who_dr_checkbox_assessment');
        var $productMultiIngredientAssessment = $("#product_multiIngredient_assessment");

        if ($("#eventSelectionAssessment").val()) {
            var eventDictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
            eventDictionaryObj.setValues(JSON.parse($("#eventSelectionAssessment").val()));
            eventDictionaryObj.setValuesPva(JSON.parse($("#eventSelectionAssessment").val()));
            if (editable) {
                eventDictionaryObj.setColumnViewText(eventDictionaryObj.getValues());
            }
        }

        if ($("#eventGroupSelectionAssessment").val()) {
            var eventDictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
            var eventGroupValue = JSON.parse($("#eventGroupSelectionAssessment").val());
            $.each(eventGroupValue, function (key, value) {
                eventDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id});
            });
            if (editable) {
                eventDictionaryObj.setEventGroupText(editable)
            }
        }

        if ($("#eventSelectionAssessment").val() || $("#eventGroupSelectionAssessment").val()) {
            showDictionaryValuesAssessment(document.getElementById("showEventSelectionAssessment"), eventDictionaryObj.getValues(), EVENT_DICTIONARY, eventDictionaryObj.getValuesDicGroup(), eventDictionaryObj.getLevels(), editable);
            enableDisableEventGroupButtonsAssessment();
        }

        if(!isProductAssignment) {
            if ($("#productGroupSelectionAssessment").val()) {
                var productDictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
                var productGroupValue = JSON.parse($("#productGroupSelectionAssessment").val());
                $.each(productGroupValue, function (key, value) {
                    if(value.isMultiIngredient != undefined){
                        productDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id, isMultiIngredient: value.isMultiIngredient, includeWHODrugs: value.includeWHODrugs});
                    } else{
                        productDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id, isMultiIngredient: false, includeWHODrugs: value.includeWHODrugs});
                    }
                });
                if (editable) {
                    productDictionaryObj.setProductGroupText(editable);
                }
            }
        } else {
            if (currentEditingRow!== undefined && currentEditingRow.find("#productGroupSelectionAssessment").val()) {
                var productDictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
                var productGroupValue = JSON.parse(currentEditingRow.find("#productGroupSelectionAssessment").val());
                if(isProductAssignment){
                    productDictionaryObj.resetValuesDicGroup()
                }

                if (productGroupValue[0]) {
                    if (productGroupValue[0].includeWHODrugs) {
                        $whoDrCheckboxAssessment.prop('checked', true).trigger('change');
                    } else if (productGroupValue[0].isMultiIngredient) {
                        $whoDrCheckboxAssessment.prop('checked', false);
                        $productMultiIngredientAssessment.prop('disabled', false);
                        $productMultiIngredientAssessment.prop('checked', true);
                    } else {
                        $whoDrCheckboxAssessment.prop('checked', false).trigger('change');
                    }
                }
                $.each(productGroupValue, function (key, value) {
                    if(value.isMultiIngredient != undefined){
                        productDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id, isMultiIngredient: value.isMultiIngredient, includeWHODrugs: value.includeWHODrugs});
                    } else{
                        productDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id, isMultiIngredient: false, includeWHODrugs: value.includeWHODrugs});
                    }                });
                if (editable) {
                    productDictionaryObj.setProductGroupText(editable);
                }
            }
        }

        if(!isProductAssignment) {
            if ($("#productSelectionAssessment").val()) {
                var productDictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
                var loadValues = JSON.parse($("#productSelectionAssessment").val());
                if(loadValues[ingredientLevel].length >0){
                    var isMultiIngredientAssessment = $("#isMultiIngredientAssessment").val();
                    $.each(loadValues[ingredientLevel],function (index,element) {
                        if (isMultiIngredientAssessment) {
                            element[ASSESSMENT_MULTI_INGREDIENT_KEY] = JSON.parse(isMultiIngredientAssessment);
                        } else {
                            element[ASSESSMENT_MULTI_INGREDIENT_KEY] = false;
                        }
                    })
                }
                var loadDataSource = 'pva';
                productDictionaryObj.setValues(loadValues);
                if(isEmerging) {
                    if ($('#productDictDataSource').length > 0) {
                        var dataSource = $('#productDictDataSource').val()?.split(';')[0]
                        if (dataSource == 'pva') {
                            productDictionaryObj.setValuesPva(loadValues);
                        } else if (dataSource == "faers") {
                            loadDataSource = 'faers';
                            productDictionaryObj.setValuesFaers(loadValues);
                        } else if (dataSource == "vaers") {
                            loadDataSource = 'vaers';
                            productDictionaryObj.setValuesVaers(loadValues);
                        }else if (dataSource == "vigibase") {
                            loadDataSource = 'vigibase';
                            productDictionaryObj.setValuesVigibase(loadValues);
                        } else if (dataSource == "eudra") {
                            loadDataSource = 'eudra';
                            productDictionaryObj.setValuesEudra(loadValues);
                        }
                    }
                }else {
                    productDictionaryObj.setValuesPva(loadValues);
                }

                if (editable) {
                    productDictionaryObj.setColumnViewText(loadValues, loadDataSource);
                }
            }

            if ($("#productSelectionAssessment").val() || $("#productGroupSelectionAssessment").val()) {
                showDictionaryValuesAssessment(document.getElementById("showProductSelectionAssessment"), productDictionaryObj.getValues(), PRODUCT_DICTIONARY, productDictionaryObj.getValuesDicGroup(), productDictionaryObj.getLevels(), editable);
                enableDisableProductGroupButtonsAssessment();
            }
        } else {
            if (currentEditingRow!== undefined && currentEditingRow!== undefined && currentEditingRow.find("#productSelectionAssessment").val()) {
                var productDictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
                var loadValues = JSON.parse(currentEditingRow.find("#productSelectionAssessment").val());
                if(loadValues[ingredientLevel].length >0){
                    $.each(loadValues[ingredientLevel],function (index,element) {
                        element['isMultiIngredient'] = currentEditingRow.find("#isMultiIngredientAssessment").val() == "true" ? true:false;
                    })
                }

                // Check if loadValues has ASSESSMENT_DRUG_REC_NUM_LEVEL and its length is 0, or if it doesn't have ASSESSMENT_DRUG_REC_NUM_LEVEL
                if (!(loadValues.hasOwnProperty(ASSESSMENT_DRUG_REC_NUM_LEVEL) && loadValues[ASSESSMENT_DRUG_REC_NUM_LEVEL].length === 0) && loadValues.hasOwnProperty(ASSESSMENT_DRUG_REC_NUM_LEVEL)) {
                    // Case when loadValues has ASSESSMENT_DRUG_REC_NUM_LEVEL and its length is not 0
                    $whoDrCheckboxAssessment.prop('checked', true).trigger('change');
                } else {
                    // Case when loadValues doesn't have ASSESSMENT_DRUG_REC_NUM_LEVEL or its length is 0
                    if (currentEditingRow.find("#isMultiIngredientAssessment").val() === "true") {
                        $whoDrCheckboxAssessment.prop('checked', false);
                        $productMultiIngredientAssessment.prop('disabled', false);
                        $productMultiIngredientAssessment.prop('checked', true);
                    } else {
                        $whoDrCheckboxAssessment.prop('checked', false).trigger('change');
                    }
                }
                var loadDataSource = ($('#clickedDatasource').val())?($('#clickedDatasource').val()):'pva';
                productDictionaryObj.setValues(loadValues);
                if (loadDataSource == 'pva') {
                    productDictionaryObj.setValuesPva(loadValues);
                }
                if (editable) {
                    productDictionaryObj.setColumnViewText(loadValues, loadDataSource);
                }
            }

            if (currentEditingRow!== undefined && (currentEditingRow.find("#productSelectionAssessment").val() || currentEditingRow.find("#productGroupSelectionAssessment").val())) {
                showDictionaryValuesAssessment(currentEditingRow.find("#showProductSelectionAssessment"), productDictionaryObj.getValues(), PRODUCT_DICTIONARY, productDictionaryObj.getValuesDicGroup(), productDictionaryObj.getLevels(), editable);
                enableDisableProductGroupButtonsAssessment();
            }
        }
    }

    loadValuesToDictionary();


    function updateValuesWithCurrentSelectedEventDicData(eventButton) {
        var eventDictionaryObjAssessment = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var currentEventIconContainer = $(eventButton).closest("[class*='toAddContainer']").first();
        if($('.dmvCopyAndPasteModal').hasClass('in')){
            currentEventIconContainer = $("#dataMiningVariableString").find('.toAddContainer');
        }
        var values = eventDictionaryObjAssessment.getValues();
        var valuesPva = eventDictionaryObjAssessment.getValuesPva();
        if (currentEventIconContainer.length > 0) {
            var reportFieldLevel = getFieldLevelFromExpression(currentEventIconContainer[0]);
            $(getValueTextFromExpression(currentEventIconContainer[0])).val(getNames(values[reportFieldLevel]));
        } else {
            if (eventDictionaryObjAssessment.getValuesDicGroup().length) {
                $("#eventSelectionAssessment").val(checkEmptyValues(values));
                $("#eventGroupSelectionAssessment").val(JSON.stringify(eventDictionaryObjAssessment.getValuesDicGroup())).trigger("change");
                showDictionaryValuesAssessment(document.getElementById("showEventSelectionAssessment"), eventDictionaryObjAssessment.getValues(), EVENT_DICTIONARY, eventDictionaryObjAssessment.getValuesDicGroup(), eventDictionaryObjAssessment.getLevels(), editable);
            } else {
                $("#eventSelectionAssessment").val(checkEmptyValues(valuesPva));
                $("#eventGroupSelectionAssessment").val(JSON.stringify(eventDictionaryObjAssessment.getValuesDicGroup())).trigger("change");
                showDictionaryValues(document.getElementById("showEventSelectionAssessment"), eventDictionaryObjAssessment.getValuesPva(), EVENT_DICTIONARY, eventDictionaryObjAssessment.getValuesDicGroup(),eventDictionaryObjAssessment.getLevels(), editable);
            }
        }
    }

    function updateEventModalDataAssessment(eventButton) {
        var eventDictionaryObj = getDictionaryObjectAssessment(EVENT_DICTIONARY);
        var currentEventIconContainer = $(eventButton).closest("[class*='toAddContainer']").first();
        if($('.dmvCopyAndPasteModal').hasClass('in')){
            currentEventIconContainer = $("#dataMiningVariableString").find('.toAddContainer');
        }
        var eventValues = eventDictionaryObj.getValues();
        if (currentEventIconContainer.length > 0) {
            var reportFieldLevel = getFieldLevelFromExpression(currentEventIconContainer[0]);
            var values = $(getValueTextFromExpression(currentEventIconContainer[0])).val();
            eventValues[reportFieldLevel] = getValuesArrayFromName(values);
            eventDictionaryObj.setValues(eventValues);
            eventDictionaryObj.setColumnViewText(eventDictionaryObj.getValues());
        } else {
            if ($("#eventSelectionAssessment").val()) {
                eventDictionaryObj.setValues(JSON.parse($("#eventSelectionAssessment").val()));
                eventDictionaryObj.setValuesPva(JSON.parse($("#eventSelectionAssessment").val()));
                eventDictionaryObj.setColumnViewText(eventDictionaryObj.getValues());
            }
            if ($("#eventGroupSelectionAssessment").val()) {
                var eventGroupValue = JSON.parse($("#eventGroupSelectionAssessment").val());
                $.each(eventGroupValue, function (key, value) {
                    eventDictionaryObj.addToValuesDicGroup({name: value.name, id: value.id});
                });
                if (editable) {
                    eventDictionaryObj.setEventGroupText(editable);
                }
            }
            enableDisableEventGroupButtonsAssessment();
        }
    }

    function getLevelForCurrentSelection(eventButton) {
        var currentEventIconContainer = $(eventButton).closest("[class*='toAddContainer']").first();
        if($('.dmvCopyAndPasteModal').hasClass('in')){
            currentEventIconContainer = $("#dataMiningVariableString").find('.toAddContainer');
        }
        if (currentEventIconContainer.length > 0) {
            return getFieldLevelFromExpression(currentEventIconContainer[0]);
        }
    }

    function getNames(values) {
        var result = '';
        $.each(values, function () {
            result += this.name + ';';
        });
        return result.substring(0, result.length - 1);
    }

    function getValuesArrayFromName(names) {
        var values = [];
        if (names) {
            $.each(names.split(";"), function () {
                values.push({name: this, id: " "});
            });
        }
        return values;
    }

        //Prevent Users from submitting form by hitting enter
        var preventEnter = false;
        $(window).keydown(function (event) {
            if (event.keyCode == 13 && preventEnter) {
                event.preventDefault();
            }
        });
        $('#productModalAssessment, #eventModalAssessment').on('shown.bs.modal', function (e) {
            preventEnter = true;
            var $el = $(e.relatedTarget);
            if ($el.data('hide-dictionary-group')) {
                $(this).find('.dictionary-group').hide();
            } else {
                $(this).find('.dictionary-group').show();
            }
        });
        $('#productModalAssessment, #eventModalAssessment').on('hidden.bs.modal', function (e) {
            preventEnter = false;
        });

        $(document).on('click', '.removeSingleDictionaryValueAssessment', function (event) {
            var $who_dr_checkbox_assessment = $('#who_dr_checkbox_assessment')
            var $this = $(this),
                dictionaryType = $this.attr("data-type"),
                level = $this.attr("data-level"),
                toRemove = JSON.parse($this.attr("data-element"));
            var productDatasource = $(this).closest(".dictionaryItem").attr('title');
            $(this).closest('.dictionaryItem').remove();
            var dictionary = getDictionaryObjectAssessment(dictionaryType);
            dictionary.removeValue(level, toRemove, productDatasource);
            if (level != ASSESSMENT_DRUG_REC_NUM_LEVEL && productDatasource === ASSESSMENT_DATASOURCE_SAFETY_TITLE && $who_dr_checkbox_assessment.is(':checked')) {
                $('#mainContent').trigger("columnView.reset." + PRODUCT_DICTIONARY);
                $(".dictionaryItem").remove();
                $(".productGroupValues").remove();
                dictionary.resetDictionaryList();
                $who_dr_checkbox_assessment.prop('checked', false).trigger('change');
            }
            enableDisableProductGroupButtonsAssessment(dictionary.getValues());
            var dictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
            var count = 0;
            if (checkEmptyValues(dictionaryObj.getValuesPva()))
                count++;

            if (checkEmptyValues(dictionaryObj.getValuesFaers()))
                count++;

            if (checkEmptyValues(dictionaryObj.getValuesVaers()))
                count++;

            if (checkEmptyValues(dictionaryObj.getValuesEudra()))
                count++;

            if (checkEmptyValues(dictionaryObj.getValuesVigibase()))
                count++;

            if (dictionaryObj.getValuesDicGroup().length && typeof $this.closest('.dictionaryItem').attr("data-newproduct") == 'undefined' && count) {
                dictionaryObj.isCloseDictionary = false;
            } else if (dictionaryObj.getValuesDicGroup().length && !count) {
                dictionaryObj.isCloseDictionary = true;
            }
            if (dictionaryType == EVENT_DICTIONARY) {
                enableDisableEventGroupButtonsAssessment();
            }
        });
        $('#product_multiIngredient_assessment').on("change",function(){
            var inputs = $('#productModalAssessment').find('input.searchProducts');
            var columns = $('#productModalAssessment').find('#carriage .dicUlFormat');
            for (var i = 0; i < columns.length; i++) {
                if (columns[i].firstChild && columns[i].firstChild.getAttribute("dictionarylevel") == ingredientLevel) {
                    columns[i].innerHTML = "";
                }
                if(inputs[i].getAttribute("level") == ingredientLevel){
                    inputs[i].value = "";
                }
            }
        });

    /**
     * who drug checkbox drug record display changes
     */
    $("#who_dr_checkbox_assessment").on("change",function() {
        var $drugRecordSelection = $("#drugRecordSelection");
        var $drugRecordSelectionAssessment = $("#drugRecordSelectionAssessment");
        var $productMultiIngredientAssessment = $("#product_multiIngredient_assessment");

        if ($(this).is(":checked")) {
            // Case when checkbox is checked
            showDrugRecordSelection();
            $productMultiIngredientAssessment.prop("checked", true).trigger("change");
            $productMultiIngredientAssessment.prop("disabled", true);
        } else {
            // Case when checkbox is not checked
            clearDrugRecordNumber();
            $drugRecordSelection.hide();
            $drugRecordSelectionAssessment.hide();
            $productMultiIngredientAssessment.prop("checked", false).trigger("change");
            $productMultiIngredientAssessment.prop("disabled", false);
        }

        function showDrugRecordSelection() {
            $drugRecordSelection.show();
            $drugRecordSelectionAssessment.show();
        }

        function clearDrugRecordNumber() {
            var dictionaryObj = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
            $("#mainContent").trigger("columnView.reset." + PRODUCT_DICTIONARY);
            $(".dictionaryItem").remove();
            $(".productGroupValues").remove();
            dictionaryObj.resetDictionaryList();
        }
    });
        $("#searchProductsAssessment").on("click",function () {
            $("#isAssessmentDicitionary").val("true");
        });
        $("#searchEvents").on("click",function () {
            $("#isAssessmentDicitionary").val("false");
        });


        $(document).on('click', '.removeSingleDictionaryProductValueAssessment', function (event) {
            var dictionary = getDictionaryObjectAssessment(PRODUCT_DICTIONARY);
            var $this = $(this);
            var toRemove = JSON.parse($this.attr("data-element"));
            $(this).closest('.productGroupValues').remove();
            dictionary.setValuesDicGroup(removeValProdGrp(dictionary.getValuesDicGroup(), toRemove));
            dictionary.setSelectedDicGroups(removeSingleGrp(dictionary.getSelectedDicGroups(), toRemove));
            enableDisableProductGroupButtonsAssessment(dictionary.getValues())
        });

        var removeValProdGrp = function (dict, val) {
            return _.filter(dict, function (el) {
                if (el["id"] && val["id"]) {
                    if (el["id"] === val["id"]) return false;
                } else {
                    if (el["name"] === val["name"]) return false;
                }
                return true;
            });
        };

    var removeSingleGrp = function (dict, val) {
        return _.filter(dict, function (el) {
            if (el["result"]["id"] && val["id"]) {
                if (el["result"]["id"] == val["id"]) return false;
            }
            return true;
        });
    };

        $("ul").on("keypress", "li", function (event) {
            if (event.keyCode === 92) {
                $(this).trigger('click');
            }
        });

        $(".prodDictFilterColCalc").on("keypress", "i.modal-link", function (event) {
            if (event.keyCode === 92) {
                $(this).trigger('click');
            }
        });

        $("#assessments").find(".iconSearch").on('click', function () {
            if ($('#productModalAssessment .modal-body .dicUlFormat').length > 5) {
                $('#productModalAssessment .modal-lg').addClass('modal-xl').removeClass('modal-lg');
            }
        });

        $('#productModalAssessment').on('shown.bs.modal', function (e) {
            if ($('#productModalAssessment .modal-body .dicUlFormat').length > 5) {
                $('#productModalAssessment .modal-lg').addClass('modal-xl').removeClass('modal-lg');
            }
        });

    if (!isProductAssignment && isEmerging) {
        populateDict();
    }
});

function clearDictionaryModal(dictionaryType) {
    productGroupDataSources = []
    resetDictionaryListAssessment(dictionaryType);
    clearAllTextAssessment(dictionaryType);
    clearSearchInputsAssessment(-1, dictionaryType);
    clearAdditionalFilters(dictionaryType);
    if (dictionaryType == EVENT_DICTIONARY) {
        $("ul.eventDictionaryColWidth").find('li').each(function () {
            $(this).remove()
        });
        clearEventGroupTextAssessment()
    } else {
        $("ul.productDictionaryColWidthCalc").find('li').each(function () {
            $(this).remove()
        });
        clearProductGroupTextAssessment(dictionaryType);
    }
    $('.errorMessage').hide();
    resetMultiSearchModal();
}

function  populateDict(){
    if ($('#productDictDataSource').length > 0) {
        var dataSource = $('#productDictDataSource').val()?.split(';')[0]
        if (dataSource == "faers") {
            $("#dataSourcesProductDict").val("faers").trigger('change');
            return
        } else if (dataSource == "eudra") {
            $("#dataSourcesProductDict").val("eudra").trigger('change');
            return
        }   else if (dataSource == "vaers") {
            $("#dataSourcesProductDict").val("vaers").trigger('change');
            return
        }
        else if (dataSource == "vigibase") {
            $("#dataSourcesProductDict").val("vigibase").trigger('change');
            return
        }
    }
    if ($("#dataSourcesProductDict").length > 0) {
        $("#dataSourcesProductDict").val("pva").trigger('change');
    }
}

/**
 * fetching Drug rec Number hierarchy from PVR
 * @param dictionaryObj
 */
function fetchAndUpdateDrugRecordNumbersAssessment(dictionaryObj){
    $.ajax({
        type: "POST",
        url: options.product.getDrugRecordNumberUrl,
        data: {
            dataSource: ASSESSMENT_DATASOURCE_PVA,
            productSelection: JSON.stringify(dictionaryObj.getValuesPva()),
            productGroupSelection: JSON.stringify(dictionaryObj.getValuesDicGroup())
        },
        dataType: "json",
        beforeSend: function () {
            toggleLoader(true);
        },
    })
        .done(function (res) {
            // Add values on UI and in dictionary for Drug rec num hierarchy
            var drugRecordData = res.items.map(item => {
                return {
                    'recordNumber': item.id,
                    'text': item.name,
                    'recordName': item.name,
                    'data-value': item.id,
                    'dictionarylevel': ASSESSMENT_DRUG_REC_NUM_LEVEL
                };
            });
            // Drug rec num index in hierarchy will be fixed (100)
            var drugRecordArray = $($.map(drugRecordData, function (el) {
                return $.makeArray(el);
            }));
            // Considering drug rec numbers as multi-ingredient
            dictionaryObj.addAllOptions(drugRecordArray, true);
        })
        .fail(function(res) {
            console.log("Error occurred in fetching Drug Record Number hierarhcy data.");
        })
        .always(function () {
            toggleLoader(false);
        });
}

function toggleLoader(show) {
    if (show) {
        $(".pv-loader-bg").show();
    } else {
        $(".pv-loader-bg").hide();
    }
}


