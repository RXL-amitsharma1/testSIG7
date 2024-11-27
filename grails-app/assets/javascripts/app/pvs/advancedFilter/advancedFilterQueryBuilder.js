// AJAX Implementation
// These fields are used to store AJAX
var AJAXKeywordList;
var AJAXFieldMap = [];
var AJAXValueSampleList;
var AJAXOperatorStringList;
var AJAXOperatorNumList;
var AJAXOperatorDateList;
var AJAXOperatorValuelessList;
var AJAXOperatorBooleanList;
// We have 7 AJAX calls, so we update values after the 6th is complete
var AJAXFinished = 7;
var AJAXCount = 0;
// ---------------------------END AJAX

// CONSTANTS
var RF_TYPE_TEXT = 'text';
var RF_TYPE_STRING = 'string';
var RF_TYPE_DATE = 'date';
var RF_TYPE_NUMBER = 'number';
var RF_TYPE_PART_DATE = 'partialDate';
var RF_TYPE_AUTOCOMPLETE = 'autocomplete';
var RF_TYPE_BOOLEAN = 'boolean';
var CASE_SERIES = 'caseSeries';
var PT_FIELD = 'pt';
var CASE_NUMBER = 'caseNumber'

var EDITOR_TYPE_TEXT = 0;
var EDITOR_TYPE_DATE = 1;
var EDITOR_TYPE_SELECT = 2;
var EDITOR_TYPE_NONE = 3;
var EDITOR_TYPE_AUTOCOMPLETE = 4;


// DATE OPERATOR CONSTANTS - these are the names, not values of the QueryOperator enum.
var LAST_X_DATE_OPERATORS = ['LAST_X_DAYS', 'LAST_X_WEEKS', 'LAST_X_MONTHS', 'LAST_X_YEARS'];
var NEXT_X_DATE_OPERATORS = ['NEXT_X_DAYS', 'NEXT_X_WEEKS', 'NEXT_X_MONTHS', 'NEXT_X_YEARS'];
var RELATIVE_DATE_OPERATORS = ['YESTERDAY', 'LAST_WEEK', 'LAST_MONTH', 'LAST_YEAR'];
var IS_EMPTY_OPERATORS = ['IS_EMPTY', 'IS_NOT_EMPTY'];
var EQUALS_OPERATORS = ['EQUALS', 'NOT_EQUAL'];

// END CONSTANTS

// Blank Parameters Implementation
var hasBlanks = false;
var keys = [];
// -----------END BLANK PARAMETERS

// These fields reference the values we use when we add expressions
var $selectedField;
var $selectedOperator;
var $selectedValue;
var $selectDate;
var $selectSelect;
// This is the group we will add new expressions to
var selectedGroup;
var builderAll;
var builderSubgroup;
var $queryJSON;
// Drag and drop
var dragSourceEl;
var draggingFromGroup;

// Backbone
// Definitions
var Expressions;
var Expression;
var ExpressionList;
// Variables
// This Backbone collection is used to store our Backbone expressions
var backboneExpressions;
// We render on initialize, so this variable is not called explicitly
var expressionList;
var editable = true;

var signal = signal || {};
var productGroupAllowedFields = ["suspectProductList","conComitList","primSuspProdList","primSuspPaiList","paiAllList","patientHistDrugs","productName"]
var eventGroupAllowedFields = ["allPtList","medErrorPtList","indication","causeOfDeath","patientMedHist","pt"]
var allowedoperatorsForEgAndPg = ["CONTAINS", "DOES_NOT_CONTAIN"]

signal.advancedFilter = (function () {
    var initializeAdvancedFilters = function () {
        // Page startup -------------------------------------------------------------------------------------------- Startup

        // These fields reference the values we use when we add expressions
        $selectedField = $('#selectField');
        $selectedOperator = $('#selectOperator');
        $selectedValue = $('#selectValue');
        $selectDate = $('#selectDate');
        $selectSelect = $('#selectSelect');
        // This is the group we will add new expressions to
        builderAll = document.getElementById('builderAll');

        $queryJSON = $('#queryJSON');

        // Backbone is used to easily model the expressions we use ------------------------------------------------ BACKBONE

        Expressions = Backbone.Collection.extend({});

        Expression = Backbone.Model.extend({
            /*
             model does not explicitly state its attributes, so here it is with some sample values:
             field: "countryOfIncident",
             op: 'CONTAINS',
             value: 'United'
             */
        });

        ExpressionList = Backbone.View.extend({
            initialize: function () {
                //this should build from previously saved data if it exists. this view is only used for initial rendering.
                var JSONQuery = $queryJSON.val();

                //copyAndPastesFields = JSONQuery.
                if (JSONQuery == null || JSONQuery == '') {
                    selectedGroup = createGroup();
                    builderAll.appendChild(selectedGroup);
                }
                else {
                    buildQueryFromJSON(JSONQuery);
                    selectedGroup = builderAll.getElementsByClassName('group')[0];
                }

                addSubgroup(builderAll);
                builderSubgroup = getSubgroup(builderAll);

                $(selectedGroup).addClass('selectedGroup');

                $("#toAddContainer").on('query.builder.updateAJAXValues', function (evt) {
                    var container = evt.target;
                    updateAJAXValues(container)
                });

                //this isn't necessary, but doing it to test if we get the same result as we saved
                $queryJSON.val(printAll());
            }
        });

        getAJAXValues();

        $('#showDate').hide();
        $('#showSelect').hide();
        $('#showSelectAuto').hide();
        $('#errorMessageOperator').hide();



        // This Backbone collection is used to store our Backbone expressions
        backboneExpressions = new Expressions();
        expressionList = new ExpressionList();

        // Initialize various components
        $('#selectField').select2({
            dropdownParent: $("#createAdvancedFilterModal")
        });
        $('#selectField.expressionField').select2({dropdownParent: $("#createAdvancedFilterModal")});
        $('#selectField').val('-1').trigger('change');

        $('#selectSelect').select2({allowClear: false});
        $("#selectSelectAuto").select2({
            minimumInputLength: 2,
            multiple: true,
            allowClear: false,
            ajax: {
                quietMillis: 1000,
                delay: 1000,
                dataType: "json",
                url: function () {
                    return getSelectAutoUrl($(this))
                },
                data: function (params) {
                    var container = $(this).closest('.toAddContainer')[0];
                    var contaonerElement = $(this).closest('.toAddContainer');
                    var field = $(getFieldFromExpression(container)).select2('val');
                    var selectedOperator = contaonerElement.find("#selectOperator").val()
                    if(_.contains(allowedoperatorsForEgAndPg,selectedOperator)){
                        term = contaonerElement.find("#productGroupSelection").is(":checked") ? 'PG_' + params.term : ( contaonerElement.find("#eventGroupSelection").is(":checked")? 'EG_' + params.term : params.term)
                    } else {
                        term =params.term
                    }
                    var parameters = {};
                    if (field == 'alertTags') {
                        parameters['isCaseSeriesTag'] = true;
                    } else if (field == 'globalTags') {
                        parameters['isCaseSeriesTag'] = false;
                    } else if (field == 'assignedTo.id') {
                        parameters['isGroup'] = false;
                    } else if (field == 'assignedToGroup.id') {
                        parameters['isGroup'] = true;
                    } else {
                        parameters['field'] = field;
                    }
                    parameters['alertType'] = apptype;
                    parameters['term'] = term || "";
                    parameters['page'] = params.page || 1;
                    parameters['max'] = 30;
                    parameters['isFaers'] =$("#isFaers").val()??"false"

                    return parameters
                },
                processResults: function (data, params) {
                    params.page = params.page || 1;
                    return {
                        results: data.list
                    };
                }
            }
        });
        $("#selectSelectAuto").val('').trigger('change');
        $('#selectDate').datepicker({
            allowPastDates: true
        });
        $('#queryLevel').select2();

        $.each(document.getElementsByClassName('doneLoading'), function () {
            $(this).hide();
        });

        // --------------------------------------------------------------------------------------------------------- END TAB

        // ----------------------------------------------------------------------------------------------------- END STARTUP

        // AJAX Calls to get data ------------------------------------------------------------------------------------- AJAX


        // On success, call these update methods to fill in UI values)

        // -------------------------------------------------------------------------------------------------------- END AJAX


        // On click methods ---------------------------------------------------------------------------------------- onClick


        // Add button to add a new expression

        $('#createAdvancedFilterModal').off('hidden.bs.modal').on('hidden.bs.modal', function (e) {
            $('#createAdvancedFilterModal .modal-title').html("Add New Filter");
            //problems with reinitializing select field
            $('#queryJSON').val('');
            $('#builderAll').empty();
            if ($('#selectField').data('select2')) {
                $('#selectField').select2('destroy');
            }
            if ($('#selectSelect').data('select2')) {
                $('#selectSelect').select2("destroy");
            }
            if ($('#selectSelectAuto').data('select2')) {
                $('#selectSelectAuto').select2("destroy");
            }
            filterOpened = 0;
        });


        // ----------------------------------------------------------------------------------------------------- END ONCLICK
    };
    return {initializeAdvancedFilters: initializeAdvancedFilters}
})();

// Change events are captured by classname ---------------------------------------------------------------- onChange
$(document).on('change', '.expressionField', function () {
    var haveToShowDateInfo = false;
    $.each($(".expressionField"), function () {
        if($(this).val() === "dispLastChange"){
            haveToShowDateInfo = true;
        }
    })
    showInfoMessage("The Last Disposition Date is considered based on UTC/GMT time zone.", haveToShowDateInfo);

    var egElements =$(this).closest('.toAddContainer');
    var pgElements =$(this).closest('.toAddContainer');
    pgElements.find('#eventGroupSelection').prop('checked',false);
    egElements.find('#productGroupSelection').prop('checked',false);
    egElements.find('.eventGroupSelectionClass').hide();
    pgElements.find('.productGroupSelectionClass').hide();
    var container = $(this).closest('.toAddContainer')[0];
    var expression = $(container).closest('.expression')[0];

    var field = $(this)[0].value;
    updateAJAXFields(container);
    updateAJAXValues(container);

    //Update backbone model
    if (expression) {
        if (expression.expressionIndex != null) {
            backboneExpressions.at(expression.expressionIndex).set("field", field);
            var dateText = $(getValueDateInputFromExpression(container))[0].value
            backboneExpressions.at(expression.expressionIndex).set("op", $(getOperatorFromExpression(container))[0].value);
            if(dateText && AJAXFieldMap[field] === RF_TYPE_DATE){
                backboneExpressions.at(expression.expressionIndex).set("value", dateText);
            }
            else {
                backboneExpressions.at(expression.expressionIndex).set("value", "");
            }
        }
    }
    if((field == CASE_NUMBER || field == PT_FIELD) && IS_EMPTY_OPERATORS.indexOf($(getOperatorFromExpression(container))[0].value)==-1) {
        $(container).find(".copy-paste-pencil").show();
    } else{
        $(container).find(".copy-paste-pencil").hide();
    }

    //Clear select values
    $(getValueSelectFromExpression(container)).val('');
    $(getValueTextFromExpression(container)).val('');

    //Remove error outline if we have a value
    if (field != '') {
        $(this.parentElement).removeClass('has-error');
    }

    $queryJSON.val(printAll());
});

$(document).on('change', '.expressionOp', function () {
    var container = $(this).closest('.toAddContainer');
    var expression = container.closest('.expression')[0];
    var value = $(this)[0].value;
    updateAJAXOperators(container, value);
    var field = $(getFieldFromExpression($(this).closest('.toAddContainer')[0]))[0].value;
    var egElements = $(this).closest('.toAddContainer').find('.eventGroupSelectionClass');
    var pgElements = $(this).closest('.toAddContainer').find('.productGroupSelectionClass');

    if (productGroupAllowedFields.includes(field) && allowedoperatorsForEgAndPg.includes(value)) {
        egElements.hide();
        pgElements.show();
    } else if (eventGroupAllowedFields.includes(field) && allowedoperatorsForEgAndPg.includes(value)) {
        egElements.show();
        pgElements.hide();
    } else {
        pgElements.hide();
        egElements.hide();
    }
    if((field == CASE_NUMBER || field == PT_FIELD) && IS_EMPTY_OPERATORS.indexOf(value)==-1) {
        $(container).find(".copy-paste-pencil").show();
        $(container).find("#advance-filter-pencil").show();
    } else{
        $(container).find(".copy-paste-pencil").hide();
        $(container).find("#advance-filter-pencil").show();
    }

    //Update backbone model
    if (expression && (expression != undefined) && (expression.expressionIndex != null)) {
        backboneExpressions.at(expression.expressionIndex).set("op", value);

        var valueless = false;
        $.each(AJAXOperatorValuelessList, function (i, obj) {
            if (obj != null) {
                if (obj.value == value) {
                    valueless = true;
                    return false;
                }
            }
        });


        if (valueless) {
            backboneExpressions.at(expression.expressionIndex).set("value", value);
        } else {
            backboneExpressions.at(expression.expressionIndex).set("value", "");
        }
    }

    //Clear select values
    $(getValueSelectFromExpression(container)).val('').trigger('change');
    $(getValueSelectAutoFromExpression(container)).val('').trigger('change');
    $(getValueTextFromExpression(container)).val('');
    $(getValueDateInputFromExpression(container)).val('');

    $queryJSON.val(printAll());
});

$(document).on('change', '.expressionValueText', function () {
    var container = $(this).closest('.toAddContainer');
    var expression = container.closest('.expression')[0];

    //Update backbone model
    if (expression) {
        if (expression.expressionIndex != null) {
            backboneExpressions.at(expression.expressionIndex).set("value", $(getValueTextFromExpression(container))[0].value);
        }
    }

    //Remove error outline if we have a value
    if ($(this)[0].value != '') {
        $(this.parentElement).removeClass('has-error');
    }

    $queryJSON.val(printAll());
});

// Multiselect implementation
$(document).on('change', '.expressionValueSelectAuto', function () {
    var container = $(this).closest('.toAddContainer');
    var expression = $(container).closest('.expression')[0];

    var value = $(getValueSelectAutoFromExpression(container)).select2('val');
    if ($.isArray(value)) {
        value = $.grep(value, function (item) {
            if (item != "") {
                return item;
            }
        });
        if (value.length == 0) {
            value = "";
        }
    }
    // Update backbone model
    if (expression) {
        if ((expression != undefined) && (expression.expressionIndex != null)) {
            if ($.isArray(value)) {
                var concatValues = '';
                for (var i = 0; i < value.length; i++) {
                    if (i != 0) {
                        // Multiselect Select2
                        concatValues += ';' + value[i];
                    } else {
                        concatValues += value[i];
                    }
                }
                value = concatValues;
            }
            backboneExpressions.at(expression.expressionIndex).set("value", value);
            if(backboneExpressions.at(expression.expressionIndex).get('field') == CASE_SERIES  || value?.includes('EG_') || value?.includes('PG_')) {
                var text = $(getValueSelectAutoFromExpression(container)).select2('data')
                if ($.isArray(text)) {
                    var concatValues = '';
                    for (var i = 0; i < text.length; i++) {
                        if (i != 0) {
                            // Multiselect Select2
                            concatValues += ';' + text[i].text;
                        } else {
                            concatValues += text[i].text;
                        }
                    }
                    text = concatValues;
                }
                backboneExpressions.at(expression.expressionIndex).set("text", text);
            }
        }
    }

    $queryJSON.val(printAll());
});

$(document).on('click', '#advance-filter-pencil', function (evt) {
    openContainer = $(this).closest('.toAddContainer')
    $("#copyAndPasteModal").modal('show');
});

$(document).on('change', '.keywordExpression', function () {
    var expression = this.parentElement;
    var expressionGroup = expression.parentElement;

    // check it we have more than one expression
    var expressionsNum = getExpressionOrGroupCountFromGroup(expressionGroup);

    var changed = false;

    // No need to do anything if the change is between 2 expressions
    if (expressionsNum > 2) {
        // Compare to the first expression's keyword if it is not the first keyword
        // keywordIndex and firstExpressionIndex should have a value since expressionCount > 1
        var firstExpressionIndex = getFirstChildWithClassnameIndexFromElement('expression', expressionGroup);
        var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstExpressionIndex]);


        if (this !== expressionGroup.children[firstExpressionIndex].children[keywordIndex]) {
            if ($(this).val() != $(expressionGroup.children[firstExpressionIndex].children[keywordIndex]).val()) {
                changed = true;
            }
        }
        // Compare to the second if we are changing the first expression's keyword. We should not need to compare the case when we are comparing the last keyword, because the keyword select box isn't present.
        else {
            var secondExpressionIndex = getSecondChildWithClassnameIndexFromElement('expression', expressionGroup);
            // If we have one expression and the rest are groups, use the first group's index instead of the second expression
            if (secondExpressionIndex == -1) {
                secondExpressionIndex = getFirstChildWithClassnameIndexFromElement('group', expressionGroup);
            }
            var secondKeywordIndex = getKeywordIndexFromElement(expressionGroup.children[secondExpressionIndex]);
            if ($(this).val() != $(expressionGroup.children[secondExpressionIndex].children[secondKeywordIndex]).val()) {
                changed = true;
            }
        }

        if (changed) {
            var expressionIndex = getChildIndexFromParent(expression, expressionGroup);

            // second half
            var secondHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroup, expressionIndex + 1, getLastExpressionOrGroupIndexFromGroup(expressionGroup));
            expressionGroup.insertBefore(secondHalfGroup, expressionGroup.children[expressionIndex].nextSibling);

            // first half
            var firstHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroup, 0, expressionIndex);
            expressionGroup.insertBefore(firstHalfGroup, expressionGroup.firstChild);

            // alter first half's keyword
            var expressionKeyword = expression.children[getKeywordIndexFromElement(expression)];
            addKeyword(firstHalfGroup, expressionKeyword.value, 'keywordGroup');

            cleanUpKeywords(firstHalfGroup);
            cleanUpKeywords(secondHalfGroup);
            cleanUpKeywordGroups(expressionGroup);
        }
    }

    printToJSON();
});

$(document).on('change', '.keywordGroup', function () {
    var expressionGroup = this.parentElement;
    var expressionGroupContainer = expressionGroup.parentElement;

    //check it we have more than one expression or group
    var expressionsNum = getExpressionOrGroupCountFromGroup(expressionGroupContainer);

    var changed = false;

    //No need to do anything if the change is between 2 expressions
    if (expressionsNum > 2) {
        //Compare to the first group's keyword if it is not the first keyword
        //keywordIndex and firstGroupIndex should have a value since expressionCount > 1
        var firstGroupIndex = getFirstChildWithClassnameIndexFromElement('group', expressionGroupContainer);
        var keywordIndex = getKeywordIndexFromElement(expressionGroupContainer.children[firstGroupIndex]);

        if (this !== expressionGroupContainer.children[firstGroupIndex].children[keywordIndex]) {
            if ($(this).val() != $(expressionGroupContainer.children[firstGroupIndex].children[keywordIndex]).val()) {
                changed = true;
            }
        }
        //Compare to the second if we are changing the first group's keyword. We should not need to compare the case when we are comparing the last keyword, because the keyword select box isn't present.
        else {
            var secondGroupIndex = getSecondChildWithClassnameIndexFromElement('group', expressionGroupContainer);
            var secondKeywordIndex = getKeywordIndexFromElement(expressionGroupContainer.children[secondGroupIndex]);
            if ($(this).val() != $(expressionGroupContainer.children[secondGroupIndex].children[secondKeywordIndex]).val()) {
                changed = true;
            }
        }

        if (changed) {
            var groupIndex = getChildIndexFromParent(expressionGroup, expressionGroupContainer);

            //second half
            var secondHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroupContainer, groupIndex + 1, getLastExpressionOrGroupIndexFromGroup(expressionGroupContainer));
            expressionGroupContainer.insertBefore(secondHalfGroup, expressionGroupContainer.children[groupIndex].nextSibling);

            //first half
            var firstHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroupContainer, 0, groupIndex);
            expressionGroupContainer.insertBefore(firstHalfGroup, expressionGroupContainer.firstChild);

            //alter first half's keyword
            var expressionKeyword = expressionGroup.children[getKeywordIndexFromElement(expressionGroup)];
            addKeyword(firstHalfGroup, expressionKeyword.value, 'keywordGroup');

            cleanUpKeywordGroups(firstHalfGroup);
            cleanUpKeywordGroups(secondHalfGroup);
            cleanUpKeywordGroups(expressionGroupContainer);
        }
    }

    printToJSON();
});

// ---------------------------------------------------------------------------------------------------- END ONCHANGE


$(document).on('click', '#addExpression', function () {
    $('#errorMessageOperator').hide();
    var $addExpression = true;
    if ($selectedField.val() == 'Select Field') {
        $selectedField.parent().addClass('has-error');
        $addExpression = false;
    }

    if ($addExpression) {
        var baseContainer = document.getElementById('toAddContainer');
        var field = $(getFieldFromExpression(baseContainer)).val();
        var op = $(getOperatorFromExpression(baseContainer))[0].value;
        var value;
        var arrayData = []; // This is used for autocomplete fields
        var isDate = false;
        if ($(baseContainer.getElementsByClassName('expressionValueText')[0]).is(":visible")) {
            if (AJAXFieldMap[field] == RF_TYPE_DATE) {
                value = $(baseContainer.getElementsByClassName('expressionValueText')[0])[0].value;
                if (!_.isEmpty(value)) {
                    if (value <= 0 || isNaN(value)) {
                        $('#errorMessageOperator').show();
                        return false;
                    }
                }
            } else {
                value = $(baseContainer.getElementsByClassName('expressionValueText')[0])[0].value;
            }
        }
        else if ($(baseContainer.getElementsByClassName('expressionValueSelect')[0]).is(":visible")) {
            value = $(getValueSelectFromExpression(baseContainer)).val();
            if ($.isArray(value)) {
                value = $.grep(value, function (item) {
                    return item != ""
                });
            }
        } else if ($($(baseContainer).find('.expressionValueSelectAuto')[0]).is(":visible")) {
            var data = $(getValueSelectAutoFromExpression(baseContainer)).select2("data");
            value = $(getValueSelectAutoFromExpression(baseContainer)).select2("val");

            _.each(data, function (item) {
                arrayData.push({id: item.id, text: item.text});
            });

            if ($.isArray(value)) {
                value = $.grep(value, function (item) {
                    return item != ""
                });
            }
            if (!value) {
                value = ''
            }
        } else if ($(baseContainer.getElementsByClassName('expressionValueDateInput')[0]).is(":visible")) {
            value = $(baseContainer.getElementsByClassName('expressionValueDateInput')[0])[0].value;
            isDate = true;
        } else {
            value = $(baseContainer.getElementsByClassName('expressionOp')[0])[0].value;
        }

        //destroy select2 as it has issues when cloning, reinitialize after
        $('#selectField').select2("destroy");
        $('#selectSelect').select2("destroy");
        $('#selectSelectAuto').select2("destroy");
        var containerToAdd = baseContainer.cloneNode(true);
        $(containerToAdd).removeAttr("id");
        $(getValueDateFromExpression(containerToAdd)).datepicker({
            allowPastDates: true
        });

        if (isDate) {
            $(getValueDateFromExpression(containerToAdd)).datepicker('setDate', value);
        }
        if (value == null || value == '') {
            $(getValueDateInputFromExpression(containerToAdd)).val('');
        }
        addOnchangeMethodsToDatepickerElement(getValueDateFromExpression(containerToAdd));

        var extraValues = {};


        var expressionGroup;
        if (builderAll.getElementsByClassName('selectedGroup').length > 0) {
            expressionGroup = selectedGroup;
        } else {
            var groupList = builderAll.getElementsByClassName('group');
            if (groupList.length == 0) {
                expressionGroup = createGroup();
                builderAll.insertBefore(expressionGroup, builderAll.children[0]);
            }
            else {
                expressionGroup = groupList[0];
            }

            selectedGroup = expressionGroup;
            $(selectedGroup).addClass('selectedGroup');
        }

        //The expression is added to the last existing keyword, or at the beginning if no previous keywords exist. Expressions should always appear before groups.
        //check it we have more than one expression
        var lastExpression = getLastChildWithClassnameIndexFromElement('expression', expressionGroup);
        if(field == CASE_NUMBER || field == PT_FIELD && IS_EMPTY_OPERATORS.indexOf(op)==-1 ) {
            $(containerToAdd).find("#advance-filter-pencil").css('display' , 'block')
            $(containerToAdd).find(".copy-paste-pencil").css('display' , 'block')
        }

        if (lastExpression > -1) {
            expressionGroup.insertBefore(createExpressionFromInput(containerToAdd, field, op, value, extraValues), expressionGroup.children[lastExpression].nextSibling);
        }
        else {
            if (expressionGroup.children.length > 0) {
                expressionGroup.insertBefore(createExpressionFromInput(containerToAdd, field, op, value, extraValues), expressionGroup.children[0]);
            }
        }

        $(getFieldFromExpression(containerToAdd)).removeAttr('id').select2();
        $(getValueSelectFromExpression(containerToAdd)).removeAttr('id').select2();
        $('#selectField').select2({dropdownParent: $("#createAdvancedFilterModal")});
        $('#selectSelect').select2();
        $('#selectField.expressionField').select2({dropdownParent: $("#createAdvancedFilterModal")});
        $(getValueSelectAutoFromExpression(containerToAdd)).removeAttr('id').select2({
            minimumInputLength: 2,
            multiple: true,
            ajax: {
                quietMillis: 1000,
                delay: 1000,
                dataType: "json",
                url: function () {
                    return getSelectAutoUrl($(this))
                },
                data: function (params) {
                    var container = $(this).closest('.toAddContainer')[0];
                    var field = $(getFieldFromExpression(container)).select2('val');
                    var contaonerElement = $(this).closest('.toAddContainer');
                    var term
                    var selectedOperator = contaonerElement.find("#selectOperator").val()
                    if(_.contains(allowedoperatorsForEgAndPg,selectedOperator)){
                        term = contaonerElement.find("#productGroupSelection").is(":checked") ? 'PG_' + params.term : ( contaonerElement.find("#eventGroupSelection").is(":checked")? 'EG_' + params.term : params.term)
                    } else {
                        term =params.term
                    }
                    var parameters = {};
                    if (field == 'alertTags') {
                        parameters['isCaseSeriesTag'] = true;
                    } else if (field == 'globalTags') {
                        parameters['isCaseSeriesTag'] = false;
                    } else if (field == 'assignedTo.id') {
                        parameters['isGroup'] = false;
                    } else if (field == 'assignedToGroup.id') {
                        parameters['isGroup'] = true;
                    } else {
                        parameters['field'] = field;
                    }
                    parameters['alertType'] = apptype;
                    parameters['term'] = term || "";
                    parameters['page'] = params.page || 1;
                    parameters['max'] = 30;
                    parameters['isFaers'] =$("#isFaers").val()??"false"

                    return parameters
                },
                processResults: function (data, params) {
                    params.page = params.page || 1;
                    return {
                        results: data.list
                    };
                }
            }
        });

        for (var i = 0; i < arrayData.length; i++) {

            var option = new Option(arrayData[i].text, arrayData[i].id, true, true);
            $(getValueSelectAutoFromExpression(containerToAdd)).append(option).trigger('change');
        }

        // // manually trigger the `select2:select` event
        $(getValueSelectAutoFromExpression(containerToAdd)).trigger({
            type: 'select2:select',
            params: {
                data: arrayData
            }
        });


        $('#selectSelectAuto').select2({
            minimumInputLength: 2,
            multiple: true,
            ajax: {
                quietMillis: 1000,
                delay: 1000,
                dataType: "json",
                url: function () {
                    return getSelectAutoUrl($(this))
                },
                data: function (params) {
                    var container = $(this).closest('.toAddContainer')[0];
                    var field = $(getFieldFromExpression(container)).select2('val');
                    var contaonerElement = $(this).closest('.toAddContainer')
                    var term
                    var selectedOperator = contaonerElement.find("#selectOperator").val()
                    if(_.contains(allowedoperatorsForEgAndPg,selectedOperator)){
                        term = contaonerElement.find("#productGroupSelection").is(":checked") ? 'PG_' + params.term : ( contaonerElement.find("#eventGroupSelection").is(":checked")? 'EG_' + params.term : params.term)
                    } else {
                        term =params.term
                    }
                    var parameters = {};
                    if (field == 'alertTags') {
                        parameters['isCaseSeriesTag'] = true;
                    } else if (field == 'globalTags') {
                        parameters['isCaseSeriesTag'] = false;
                    } else if (field == 'assignedTo.id') {
                        parameters['isGroup'] = false;
                    } else if (field == 'assignedToGroup.id') {
                        parameters['isGroup'] = true;
                    } else {
                        parameters['field'] = field;
                    }
                    parameters['alertType'] = apptype;
                    parameters['term'] = term || "";
                    parameters['page'] = params.page || 1;
                    parameters['max'] = 30;
                    parameters['isFaers'] =$("#isFaers").val()??"false"

                    return parameters
                },
                processResults: function (data, params) {
                    params.page = params.page || 1;
                    return {
                        results: data.list
                    };
                }
            }
        });
        $('#selectSelectAuto').val("");

        // This removes the empty value "" that gets appended when we destroy and then reinitialize this as select2.
        var selectArray = $('#selectSelect').val();
        //Type check is introduced.
        if (typeof selectArray != "undefined" && selectArray != null) {
            for (var i = selectArray.length; i--;) {
                if (selectArray[i] === "") {
                    selectArray.splice(i, 1);
                }
            }
        }

        $('#selectSelect').val(selectArray);
        cleanUpKeywords(expressionGroup);
        printToJSON();

    }
});


$(document).on('click', '.removeExpression', function () {
    var expressionGroup = this.parentElement.parentElement;
    var affectIndex = this.parentElement.expressionIndex;
    backboneExpressions.remove(backboneExpressions.at(affectIndex));
    checkIndexes(affectIndex);
    this.parentElement.remove();

    if (expressionGroup.children.length > 0) {
        cleanUpKeywords(expressionGroup);
    }

    printToJSON();
});

$(document).on('click', '.removeGroup', function () {
    var groupContainer = this.parentElement;
    var topmostContainer = groupContainer.parentElement;
    var removedExpressions = groupContainer.getElementsByClassName('expression');
    var affectIndex;
    for (var i = 0; i < removedExpressions.length; i++) {
        affectIndex = removedExpressions[i].expressionIndex;
        backboneExpressions.remove(backboneExpressions.at(affectIndex));
        checkIndexes(affectIndex);
    }
    this.parentElement.remove();

    if (groupContainer.children.length > 0) {
        cleanUpKeywordGroups(groupContainer);
        cleanUpKeywords(groupContainer);
    }

    cleanUpKeywordGroups(topmostContainer);
    cleanUpKeywords(topmostContainer);
    printToJSON();
});

//Multiselect implementation
$(document).on('change', '.expressionValueSelect', function () {
    var container = $(this).closest('.toAddContainer');
    var expression = $(container).closest('.expression')[0];

    var value = $(getValueSelectFromExpression(container)).val();
    if ($.isArray(value)) {
        value = $.grep(value, function (item) {
            if (item != "") {
                return item;
            }
        });
        if (value.length == 0) {
            value = "";
        }
    }

    //Update backbone model
    if (expression) {
        if (expression.expressionIndex != null) {
            if ($.isArray(value)) {
                var concatValues = '';
                for (var i = 0; i < value.length; i++) {
                    if (i != 0) {
                        //Multiselect Select2
                        concatValues += ';' + value[i];
                    } else {
                        concatValues += value[i];
                    }
                }
                value = concatValues;
            }
            backboneExpressions.at(expression.expressionIndex).set("value", value);
        }
    }

    $queryJSON.val(printAll());
});

function showAdvancedFilterLoader(){
    $.each(document.getElementsByClassName('loading'), function () {
        $(this).show();
    });
    $.each(document.getElementsByClassName('doneLoading'), function () {
        $(this).hide();
    });
}

function hideAdvancedFilterLoader(){
    $.each(document.getElementsByClassName('loading'), function () {
        $(this).hide();
    });
    $.each(document.getElementsByClassName('doneLoading'), function () {
        $(this).show();
    });
}

function getPossibleValuesAJAX() {
    showAdvancedFilterLoader()
    $.ajax({
        type: "GET",
        url: possibleValuesUrl,
        dataType: 'json',
    })
        .done(function (result) {
            AJAXValueSampleList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
            }
            hideAdvancedFilterLoader();
        });
}

function getReportFieldsAJAX() {
    $.ajax({
        type: "GET",
        url: allFieldsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            $.each(result, function () {
                if (this.isAutocomplete) {
                    AJAXFieldMap[this.name] = RF_TYPE_AUTOCOMPLETE;
                } else {
                    switch (this.dataType) {
                        case 'java.lang.String':
                            if (this.isText) {
                                AJAXFieldMap[this.name] = RF_TYPE_TEXT;
                            }
                            else {
                                AJAXFieldMap[this.name] = RF_TYPE_STRING;
                            }
                            break;
                        case 'java.util.Date':
                            AJAXFieldMap[this.name] = RF_TYPE_DATE;
                            break;
                        case 'java.lang.Number':
                            AJAXFieldMap[this.name] = RF_TYPE_NUMBER;
                            break;
                        case 'java.lang.Boolean':
                            AJAXFieldMap[this.name] = RF_TYPE_BOOLEAN;
                            break;
                    }
                }
            });
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
            }
    });
}

function getDateOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: dateOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorDateList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
        }
    });
}

function getNumOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: numOperatorsUrl,
        dataType: 'json',
    })
        .done(function (result) {
            AJAXOperatorNumList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
            }
    });
}

function getStringOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: stringOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorStringList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
            }
    });
}

function getBooleanOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: booleanOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorBooleanList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
            }
    });
}

function getValuelessOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: valuelessOperatorsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXOperatorValuelessList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
            }
    });
}

function getKeywordsAJAX() {
    $.ajax({
        type: "GET",
        url: keywordsUrl,
        dataType: 'json'
    })
        .done(function (result) {
            AJAXKeywordList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            }
            else {
                AJAXCount++;
            }
    });
}

function getAJAXValues() {
    getPossibleValuesAJAX();
    getReportFieldsAJAX();
    getDateOperatorsAJAX();
    getNumOperatorsAJAX();
    getStringOperatorsAJAX();
    getBooleanOperatorsAJAX();
    getValuelessOperatorsAJAX();
    getKeywordsAJAX();
}


function updateAJAXFields(container) {
    var field = $(getFieldFromExpression(container)).val();
    var op = getOperatorFromExpression(container);
    $(op).empty();

    switch (AJAXFieldMap[field]) {
        case RF_TYPE_DATE:
            $.each(AJAXOperatorDateList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
        case RF_TYPE_NUMBER:
            $.each(AJAXOperatorNumList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
        case RF_TYPE_AUTOCOMPLETE: // Autocomplete uses same operators as string
            $("#selectSelectAuto").val('').trigger('change');
        case RF_TYPE_STRING: //string and text
            $.each(AJAXOperatorStringList, function (display, value) {
                if((field == CASE_SERIES && EQUALS_OPERATORS.indexOf(this.value)!=-1) || field != CASE_SERIES ) {
                    $(op).append($("<option></option>").attr("value", this.value).text(this.display));
                }
            });
            break;
        case RF_TYPE_BOOLEAN: //boolean
            $.each(AJAXOperatorBooleanList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
        case RF_TYPE_PART_DATE:
            $.each(AJAXOperatorDateList, function (display, value) {
                $(op).append($("<option></option>").attr("value", this.value).text(this.display));
            });
            break;
    }
}

function updateAJAXOperators(container, value) {
    var field = $(getFieldFromExpression(container)).val();
    var operator = $(getOperatorFromExpression(container))[0].value;
    var expression = container.parentElement;
    var parentAddContainerElement = container.closest('.toAddContainer');
    if (_.contains(IS_EMPTY_OPERATORS, operator)) {
        if ((expression != undefined) && (expression.expressionIndex != null)) {
            backboneExpressions.at(expression.expressionIndex).set("value", value);
        }
        showHideValue(EDITOR_TYPE_NONE, container);
        return;
    }

    if ((AJAXFieldMap[field] == RF_TYPE_DATE || AJAXFieldMap[field] == RF_TYPE_PART_DATE) && _.contains(RELATIVE_DATE_OPERATORS, operator)) {
        if ((expression != undefined) && (expression.expressionIndex != null)) {
            backboneExpressions.at(expression.expressionIndex).set("value", value);
        }
        showHideValue(EDITOR_TYPE_NONE, container);
        return;
    }


    if (AJAXFieldMap[field] == RF_TYPE_DATE && (_.contains(LAST_X_DATE_OPERATORS, operator))) {
        showHideValue(EDITOR_TYPE_TEXT, container);
        return;
    }

    if (AJAXFieldMap[field] == RF_TYPE_DATE) {
        showHideValue(EDITOR_TYPE_DATE, container);
    } else if (AJAXFieldMap[field] == RF_TYPE_STRING) {
        if ((_.contains(EQUALS_OPERATORS, operator)) && AJAXValueSampleList[field] && AJAXValueSampleList[field].length > 0) {
            showHideValue(EDITOR_TYPE_SELECT, container);
        } else if (_.contains(allowedoperatorsForEgAndPg, operator) && AJAXValueSampleList[field] && AJAXValueSampleList[field].length > 0) {
            if ($(parentAddContainerElement).find("#productGroupSelection").is(":checked") || $(parentAddContainerElement).find("#eventGroupSelection").is(":checked")) {
                showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
            } else {
                showHideValue(EDITOR_TYPE_TEXT, container);
            }
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    }
    else if (AJAXFieldMap[field] == RF_TYPE_BOOLEAN) {
        if ((_.contains(EQUALS_OPERATORS, operator)) && AJAXValueSampleList[field]  && AJAXValueSampleList[field].length > 0) {
            showHideValue(EDITOR_TYPE_SELECT, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    }else if (AJAXFieldMap[field] == RF_TYPE_AUTOCOMPLETE) {
        if ((_.contains(EQUALS_OPERATORS, operator))) {
            showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
        } else {
            if(_.contains(allowedoperatorsForEgAndPg,operator) && ($(parentAddContainerElement).find("#productGroupSelection").is(":checked") || $(parentAddContainerElement).find("#eventGroupSelection").is(":checked"))){
                showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
            }else{
                showHideValue(EDITOR_TYPE_TEXT, container);
            }
        }
    } else if (AJAXFieldMap[field] == RF_TYPE_PART_DATE) {
        showHideValue(EDITOR_TYPE_TEXT, container);
    } else {
        showHideValue(EDITOR_TYPE_TEXT, container);
    }
}

//only execute this after operator values have been assigned
function updateAJAXValues(container) {
    var field = $(getFieldFromExpression(container)).val();
    var selectValue = getValueSelectFromExpression(container);
    $(selectValue).empty();
    if (field != null) {
        if (field == '-1') {
            showHideValue(EDITOR_TYPE_TEXT, container);
        } else if (AJAXFieldMap[field] != RF_TYPE_DATE) {
            if (AJAXFieldMap[field] == RF_TYPE_AUTOCOMPLETE) {
                showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
            } else if(AJAXValueSampleList != null && AJAXValueSampleList[field] != null) {
                if (AJAXValueSampleList[field]) {
                    var text;
                    var value;
                    $.each(AJAXValueSampleList[field], function () {
                        if (this != '') {
                            value = this.id ? this.id : this;
                            text = this.text ? this.text : this;
                            $(selectValue).append($("<option></option>").attr("value", value).text(text));
                        }
                    });
                    showHideValue(EDITOR_TYPE_SELECT, container);
                }
            } else {
                    showHideValue(EDITOR_TYPE_TEXT, container);
                }
        } else {
            showHideValue(EDITOR_TYPE_DATE, container);
        }
    }
}

//need to grab and set our next operators and values from field values. This should only run once on page load
function updateInitialAJAXLists() {
    $.each(document.getElementsByClassName('builderAll')[0].getElementsByClassName('expression'), function () {
        var container = this.getElementsByClassName('toAddContainer')[0];
        updateAJAXFields(container);
        updateAJAXValues(container);
        $(getOperatorFromExpression(container)).val(backboneExpressions.at($(this)[0].expressionIndex).get('op'));
        updateAJAXOperators(container);
        var visible = getCorrectSelectValueFromContainer(container);
        var expressionValueAuto =   backboneExpressions.at($(this)[0].expressionIndex).get('value');
        var isValueEGOrPG = (expressionValueAuto?.includes('EG_') || expressionValueAuto?.includes('PG_')) ? true: false ;
        if (visible === getValueTextFromExpression(container) && !isValueEGOrPG) {
            $(getValueTextFromExpression(container)).val(backboneExpressions.at($(this)[0].expressionIndex).get('value'));

        } else if (visible === getValueSelectFromExpression(container) && !isValueEGOrPG) {
            // Multiselect Select2
            var oldValue = backboneExpressions.at($(this)[0].expressionIndex).get('value');
            var convertedValue = oldValue.split(";");
            if (convertedValue) {
                if (!convertedValue[0].trim()) {
                    convertedValue = null; // This removes the blank artifact if you split a blank value into [""]
                }
            }
            $(getValueSelectFromExpression(container)).val(convertedValue);
        } else if (visible === getValueSelectAutoFromExpression(container) || isValueEGOrPG) {
            // Multiselect Select2
            oldValue = backboneExpressions.at($(this)[0].expressionIndex).get('value');
            convertedValue = oldValue.split(";");
            if (convertedValue) {
                if (!convertedValue[0].trim()) {
                    convertedValue = null; // This removes the blank artifact if you split a blank value into [""]
                }
            }
            var field = $(getFieldFromExpression(container)).val();
            var arrayData = [];
            var element;
            if (field == ADVANCED_FILTER_FIELDS.ASSIGNED_TO_USER_ID || field == ADVANCED_FILTER_FIELDS.ASSIGNED_TO_GROUP_ID) {
                _.each(convertedValue, function (item) {

                    //filtering only selected Assigned To User/Group
                    element = $.grep(AJAXValueSampleList[field], function (e) {
                        return e.id == item
                    });
                    element ? arrayData.push({id: element[0].id, text: element[0].text}) : arrayData.push({id: item, text: item});
                });
            } else if(field == CASE_SERIES || oldValue?.includes('EG_') || oldValue?.includes('PG_') ){ // if case series or contains product/event group
                var oldText = backboneExpressions.at($(this)[0].expressionIndex).get('text')
                var newText = oldText.split(';')
                var index = 0;
                _.each(convertedValue, function (item) {
                       arrayData.push({id: item, text: newText[index]});
                       index += 1;
                });
            } else{
                _.each(convertedValue, function (item) {
                    arrayData.push({id: item, text: item});

                });
            }

            for (var i = 0; i < arrayData.length; i++) {

                var option = new Option(arrayData[i].text, arrayData[i].id, true, true);
                $(getValueSelectAutoFromExpression(container)).append(option).trigger('change');
            }
            // // manually trigger the `select2:select` event
            $(getValueSelectAutoFromExpression(container)).trigger({
                type: 'select2:select',
                params: {
                    data: arrayData
                }
            });

        }
        else {
            $(getValueDateInputFromExpression(container)).val(backboneExpressions.at($(this)[0].expressionIndex).get('value'));
        }
    });

    // $.each(document.getElementsByClassName('loading'), function () {
    //     $(this).hide();
    // });
    // $.each(document.getElementsByClassName('doneLoading'), function () {
    //     $(this).show();
    // });

    //setBuilder
    if (!editable) {
        $('#toAddContainer').parent().hide();
        $('#toAddContainerSet').parent().hide();
    }

    AJAXCount = 0;

}


// JSON building and parsing ---------------------------------------------------------------------------------- JSON

function printAll() {
    hasBlanks = false;
    keys = [];

    var container = document.getElementsByClassName('builderAll')[0];

    var result = '{"all":{"containerGroups":[' + printRecurJSON(container);

    if (hasBlanks) {
        result += ',"blankParameters":[';
        for (var i = 0; i < keys.length; i++) {
            if (i > 0) {
                result += ',';
            }
            result += '{' + keys[i] + '}';
        }
        result += ']';
    }
    result += '}';

    $('#hasBlanksQuery').val(hasBlanks);

    return result;
}

// Helper method, shouldn't be called directly
function printRecurJSON(expressionGroup) {
    var tempResult = " ";
    var keywordValueForGroup;
    var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(expressionGroup);

    var firstExpressionIndex = -1;
    var firstGroupIndex = -1;

    var needComma = false;

    for (var i = 0; i < expressionGroup.children.length; i++) {
        if (expressionGroup.children[i].classList.contains('expression')) {
            var tempExpression = backboneExpressions.at(expressionGroup.children[i].expressionIndex);
            var op = tempExpression.get('op');
            if (_.contains(IS_EMPTY_OPERATORS, op)) {
                tempExpression.set('value', op);
            } else {
                tempExpression.set('value', tempExpression.get('value'));
            }
            if(tempExpression.get('field') == CASE_SERIES || tempExpression.get('value')?.includes('EG_') || tempExpression.get('value')?.includes('PG_')) {
                tempResult += '{"index":"' + (expressionGroup?.children?.[i]?.expressionIndex ?? '')
                    + '","field":"' + (tempExpression?.get('field') ?? '')
                    + '","op":"' + (op ?? '')
                    + '","value":"' + ((tempExpression?.get('value') ?? '')
                        .replaceAll('\"','\\"')
                        .replaceAll('"','\"'))
                    + '","text":"' + (tempExpression?.get('text') ?? '')
                    + '"';
            } else {
                tempResult += '{"index":"' + (expressionGroup?.children?.[i]?.expressionIndex ?? '')
                    + '","field":"' + (tempExpression?.get('field') ?? '')
                    + '","op":"' + (op ?? '')
                    + '","value":"' + (tempExpression?.get('value')?.replaceAll('\"','\\"').replaceAll('"','\"').replaceAll('\r\n', '\\n') ?? '')
                    + '"';
            }

            if (tempExpression.get('field') == -1) {
                //check when field is not selected
                hasBlanks = true;
            }
            if (tempExpression?.get('value') == null || $.trim(tempExpression.get('value') ?? '') == '') {
                hasBlanks = true;
                keys.push('"key":' + (keys.length + 1)
                    + ',"field":"' + (tempExpression?.get('field') ?? '')
                    + '","op":"' + (op ?? '')
                    + '","value":"' + ((tempExpression?.get('value') ?? '')
                        .replaceAll('\"','\\"')
                        .replaceAll('"','\"'))
                    + '"');
                tempResult += ',"key":"' + keys.length + '"';
            }
            tempResult += '}';
            if (firstExpressionIndex == -1) {
                firstExpressionIndex = i;
            }
            needComma = true;
        }
        else if (expressionGroup.children[i].classList.contains('group')) {
            if (printRecurJSON(expressionGroup.children[i]) == '  ] } ') {
                //check no no criteria is added
                hasBlanks = true;
            }
            tempResult += '{"expressions":[' + printRecurJSON(expressionGroup.children[i]);
            if (firstGroupIndex == -1) {
                firstGroupIndex = i;
            }
            needComma = true;
        }
        if (needComma && i < lastExpressionOrGroupIndex) {
            tempResult += ", ";
            needComma = false;
        }
    }
    //One keyword per group, if we have a keyword
    if (firstExpressionIndex != -1) {
        var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstExpressionIndex]);
        if (keywordIndex != -1) {
            keywordValueForGroup = expressionGroup.children[firstExpressionIndex].children[keywordIndex].value;
        }
        if (keywordValueForGroup) {
            return tempResult + '],"keyword":"' + keywordValueForGroup + '"}';
        }
    }
    else if (firstExpressionIndex == -1 && firstGroupIndex != -1) {
        var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstGroupIndex]);
        if (keywordIndex != -1) {
            keywordValueForGroup = expressionGroup.children[firstGroupIndex].children[keywordIndex].value;
        }
        if (keywordValueForGroup) {
            return tempResult + '],"keyword":"' + keywordValueForGroup + '"}';
        }
    }
    return tempResult + ' ] } ';
}

function buildQueryFromJSON(JSONQuery) {
    var parsedJSON = JSON.parse(JSONQuery);

    //all is the upper container just for holding the json object.
    var all = parsedJSON["all"];

    //this is the list of all the groups to render in builderAll, the topmost level
    var containerGroups = all["containerGroups"];

    var containerKeyword = all["keyword"];

    containerGroups.forEach(function (group) {
        var addGroup = createGroup();

        var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(builderAll);

        if (lastExpressionOrGroupIndex != -1) {
            builderAll.insertBefore(addGroup, builderAll.children[lastExpressionOrGroupIndex].nextSibling);
        }
        else {
            builderAll.insertBefore(addGroup, builderAll.firstChild);
        }

        group.expressions.forEach(function (nextGroup) {
            parseGroup(addGroup, nextGroup, group.keyword);
        });

        if (containerKeyword) {
            addKeyword(addGroup, containerKeyword, 'keywordGroup');
        }

        cleanUpKeywords(addGroup);
        cleanUpKeywordGroups(addGroup);
    });

    cleanUpKeywordGroups(builderAll);
}

// Helper method, shouldn't be called directly
function parseGroup(groupElement, groupJSON, keyword) {
    if (groupJSON.expressions) {
        var addGroup = createGroup();

        var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(groupElement);
        if (lastExpressionOrGroupIndex != -1) {
            groupElement.insertBefore(addGroup, groupElement.children[lastExpressionOrGroupIndex].nextSibling);
        }
        else {
            groupElement.insertBefore(addGroup, groupElement.firstChild);
        }

        groupJSON.expressions.forEach(function (group) {
            parseGroup(addGroup, group, groupJSON.keyword);
        });

        if (keyword) {
            addKeyword(addGroup, keyword, 'keywordGroup');
        }

        cleanUpKeywords(addGroup);
        cleanUpKeywordGroups(addGroup);
    }
    else {
        var expression = document.createElement('div');
        expression.className = 'expression';
        var container = document.getElementById('toAddContainer').cloneNode(true);
        $(container).removeAttr("id");
        $(getValueDateFromExpression(container)).datepicker({
            allowPastDates: true
        });
        addOnchangeMethodsToDatepickerElement(getValueDateFromExpression(container));
        if(groupJSON.field == CASE_NUMBER || groupJSON.field == PT_FIELD && IS_EMPTY_OPERATORS.indexOf(groupJSON.op)==-1) {
            $(container).find("#advance-filter-pencil").css('display' , 'block')
            $(container).find(".copy-paste-pencil").css('display' , 'block')
        }
        expression.appendChild(container);

        var value;
        if (_.contains(IS_EMPTY_OPERATORS, groupJSON.op)) {
            value = groupJSON.op;
        } else {
            value = groupJSON.value;
        }
        var backboneExpression = new Expression({field: groupJSON.field, op: groupJSON.op, value: value});
        if(groupJSON.field == CASE_SERIES || value?.includes('EG_') || value?.includes('PG_')) {
            backboneExpression = new Expression({field: groupJSON.field, op: groupJSON.op, value: value, text: groupJSON.text})
        }
        backboneExpressions.add(backboneExpression);

        //text values
        getValueTextFromExpression(expression).value = value;

        //select2 values
        var $field = $(getFieldFromExpression(container));
        $field.val(groupJSON.field);
        if(allowedoperatorsForEgAndPg.includes(groupJSON.op) ){

            if (productGroupAllowedFields.includes(groupJSON.field)) {
                $(expression).find('.productGroupSelectionClass').show();
                $(expression).find('.eventGroupSelectionClass').hide();
            } else if (eventGroupAllowedFields.includes(groupJSON.field) ) {
                $(expression).find('.eventGroupSelectionClass').show();
                $(expression).find('.productGroupSelectionClass').hide();

            } else {
                $(expression).find('.productGroupSelectionClass').hide();
                $(expression).find('.eventGroupSelectionClass').hide();
            }
        }else{
            $(expression).find('.productGroupSelectionClass').hide();
            $(expression).find('.eventGroupSelectionClass').hide();
        }

        if(value?.includes('PG_')){

            $(expression).find('#productGroupSelection').prop('checked',true);


        }else if(value?.includes('EG_')){

            $(expression).find('#eventGroupSelection').prop('checked',true);

        }else{

            $(expression).find('#productGroupSelection').prop('checked',false);
            $(expression).find('#eventGroupSelection').prop('checked',false);
        }

        const fieldId = $field.attr('id')
        $field.removeAttr('id').select2().attr('id', fieldId);
        const selectFromExpr = $(getValueSelectFromExpression(container));
        const selectFromExprId = selectFromExpr.attr('id')
        selectFromExpr.removeAttr('id').select2().attr('id', selectFromExprId);
        const selectAuto = $(getValueSelectAutoFromExpression(container));
        const selectAutoId = selectAuto.attr('id');
        selectAuto.removeAttr('id').select2({
            minimumInputLength: 2,
            multiple: true,
            ajax: {
                quietMillis: 1000,
                delay: 1000,
                dataType: "json",
                url: function () {
                    return getSelectAutoUrl($(this))
                },
                data: function (params) {
                    var container = $(this).closest('.toAddContainer')[0];
                    var contaonerElement = $(this).closest('.toAddContainer');
                    var field = $(getFieldFromExpression(container)).select2('val');
                    var term
                    var selectedOperator = contaonerElement.find("#selectOperator").val()
                    if(_.contains(allowedoperatorsForEgAndPg,selectedOperator)){
                        term = contaonerElement.find("#productGroupSelection").is(":checked") ? 'PG_' + params.term : ( contaonerElement.find("#eventGroupSelection").is(":checked")? 'EG_' + params.term : params.term)
                    } else {
                        term =params.term
                    }
                    var parameters = {};
                    if (field == 'alertTags') {
                        parameters['isCaseSeriesTag'] = true;
                    } else if (field == 'globalTags') {
                        parameters['isCaseSeriesTag'] = false;
                    } else if (field == 'assignedTo.id') {
                        parameters['isGroup'] = false;
                    } else if (field == 'assignedToGroup.id') {
                        parameters['isGroup'] = true;
                    } else {
                        parameters['field'] = field;
                    }
                    parameters['alertType'] = apptype;
                    parameters['term'] = term || "";
                    parameters['page'] = params.page || 1;
                    parameters['max'] = 30;
                    parameters['isFaers'] =$("#isFaers").val()??"false"

                    return parameters
                },
                processResults: function (data, params) {
                    params.page = params.page || 1;
                    return {
                        results: data.list
                    };
                }
            }

        }).attr('id', selectAutoId);

        if (keyword) {
            addKeyword(expression, keyword, 'keywordExpression');
        }
        if (editable) {
            addButtonRemove(expression, 'removeExpression');
        }
        expression['expressionIndex'] = backboneExpressions.indexOf(backboneExpression);

        if (editable) {
            addDragListeners(expression);
            expression["draggable"] = true;
        }


        var lastExpressionIndex = getLastChildWithClassnameIndexFromElement('expression', groupElement);
        if (lastExpressionIndex != -1) {
            groupElement.insertBefore(expression, groupElement.children[lastExpressionIndex].nextSibling);
        }
        else {
            groupElement.insertBefore(expression, groupElement.firstChild);
        }
    }
}


//setBuilder
function printToJSON() {
    $queryJSON.val(printAll());
}

// Helper method, shouldn't be called directly
function parseSetGroup(groupElement, groupJSON, keyword) {
    if (groupJSON.expressions) {
        var addGroup = createGroup();

        var lastExpressionOrGroupIndex = getLastExpressionOrGroupIndexFromGroup(groupElement);
        if (lastExpressionOrGroupIndex != -1) {
            groupElement.insertBefore(addGroup, groupElement.children[lastExpressionOrGroupIndex].nextSibling);
        }
        else {
            groupElement.insertBefore(addGroup, groupElement.firstChild);
        }

        groupJSON.expressions.forEach(function (group) {
            parseSetGroup(addGroup, group, groupJSON.keyword);
        });

        if (keyword) {
            addKeyword(addGroup, keyword, 'keywordGroup');
        }

        cleanUpKeywords(addGroup);
        cleanUpKeywordGroups(addGroup);
    }
}

// -------------------------------------------------------------------------------------------------------- END JSON

// Drag and Drop ------------------------------------------------------------------------------------- Drag and Drop

function handleDragStart(e) {
    this.style.opacity = '0.4';
    dragSourceEl = this;

    //set drag from group
    draggingFromGroup = this.parentElement;

//        e.dataTransfer.effectAllowed = 'move';
    //we don't actually use any dataTransfer.
    //we drag the expressionIndex, the index of the model in our list of expressions, called expressions
//        e.dataTransfer.setData('application/x-moz-node', this.getElementsByClassName('toAddContainer')[0]);

    // Target (this) element is the source node.
    $(builderSubgroup).show();
}

function handleDragOver(e) {
    if (e.preventDefault) {
        e.preventDefault(); // Necessary. Allows us to drop.
    }

//        e.dataTransfer.dropEffect = 'move';  // See the section on the DataTransfer object.

    return false;
}

function handleDragEnter(e) {
    // this or e.target is the current hover target.
    this.classList.add('over');
    if (draggingFromGroup == null) {
        draggingFromGroup = this;
    }
    if (!draggingFromGroup.contains(this)) {
        var subgroups = draggingFromGroup.getElementsByClassName('subgroup');
        for (var i = 0; i < subgroups.length; i++) {
            $(subgroups[i]).hide();
        }
    }

    if ($(this).hasClass('group')) {
        $(getSubgroup(this)).show();
        draggingFromGroup = this;
    }
}

function handleDragLeave(e) {
    this.classList.remove('over');  // this or e.target is previous target element.
}

function handleDrop(e) {
    // this or e.target is current target element.

    if (e.stopPropagation) {
        e.stopPropagation(); // Stops some browsers from redirecting.
    }

    // Don't do anything if dropping the same expression we're dragging.
    if (dragSourceEl != this) {
        var swapElement;
        swapElement = this.getElementsByClassName('toAddContainer')[0];
        this.insertBefore(dragSourceEl.getElementsByClassName('toAddContainer')[0], this.children[0]);

        dragSourceEl.insertBefore(swapElement, dragSourceEl.children[0]);

        //Swap the expression indexes
        var tempIndex = dragSourceEl.expressionIndex;
        dragSourceEl.expressionIndex = this.expressionIndex;
        this.expressionIndex = tempIndex;
    }

    $(getSubgroup(this.parentElement)).hide();

    return false;
}

function handleDragEnd(e) {
    // this or e.target is the source node.
    this.style.opacity = '1';

    [].forEach.call($('.expression'), function (col) {
        col.classList.remove('over');
    });
    [].forEach.call($('.group'), function (col) {
        col.classList.remove('over');
    });
    [].forEach.call($('.subgroup'), function (col) {
        col.classList.remove('over');
        $(col).hide();
    });
    $(builderSubgroup).hide();

    $(getSubgroup(draggingFromGroup)).hide();

    printToJSON();
}

function handleDropIntoGroup(e) {
    // this or e.target is current target element.

    if (e.stopPropagation) {
        e.stopPropagation(); // Stops some browsers from redirecting.
    }

    var oldGroup = dragSourceEl.parentElement;

    // Don't do anything if dropping the expression into its own group
    if (oldGroup != this) {
        var addAfterIndex = getLastChildWithClassnameIndexFromElement('expression', this);
        var existingKeywordValue;
        if (addAfterIndex != -1) {
            this.insertBefore(dragSourceEl, this.children[addAfterIndex].nextSibling);
            var existingKeywordIndex = getKeywordIndexFromElement(this.children[addAfterIndex]);
            if (existingKeywordIndex != -1) {
                existingKeywordValue = this.children[addAfterIndex].children[existingKeywordIndex].value;
            }
        }
        else {
            this.insertBefore(dragSourceEl, this.firstChild);
        }

        //Need to preserve keyword integrity, so replace dropped element keyword with drop-into-group's keyword.
        if (existingKeywordValue) {
            var currentKeywordIndex = getKeywordIndexFromElement(dragSourceEl);
            if (currentKeywordIndex != -1) {
                dragSourceEl.children[currentKeywordIndex].value = existingKeywordValue;
            }
        }

        //clean up keywords in the old and the droppedInto groups
        cleanUpKeywords(oldGroup);
        cleanUpKeywordGroups(oldGroup);
        cleanUpKeywords(this);
    }

    $(getSubgroup(this)).hide();

    return false;
}

function handleDropIntoSubgroup(e) {
    if (e.stopPropagation) {
        e.stopPropagation(); // Stops some browsers from redirecting.
    }

    var oldGroup = dragSourceEl.parentElement;
    var expressionGroup = this.parentElement;

    var newGroup = createGroup();
    newGroup.insertBefore(dragSourceEl, newGroup.firstChild);
    var addAfterIndex = getLastExpressionOrGroupIndexFromGroup(expressionGroup);
    if (addAfterIndex != -1) {
        expressionGroup.insertBefore(newGroup, expressionGroup.children[addAfterIndex].nextSibling);
    }
    else {
        expressionGroup.insertBefore(newGroup, expressionGroup.firstChild);
    }

    cleanUpKeywords(oldGroup);
    cleanUpKeywordGroups(oldGroup);
    cleanUpKeywords(expressionGroup);
    cleanUpKeywordGroups(expressionGroup);
    //This takes care of the case when we drag an expression with a keyword.
    cleanUpKeywords(newGroup);

    $(this).hide();

    return false;
}

function handleClickGroup(e) {
    $(selectedGroup).removeClass('selectedGroup');
    selectedGroup = this;
    $(selectedGroup).addClass('selectedGroup');
}

// We allow only expressions to be draggable
function addDragListeners(element) {
    element.addEventListener('dragstart', handleDragStart, false);
    element.addEventListener('dragover', handleDragOver, false);
    element.addEventListener('dragenter', handleDragEnter, false);
    element.addEventListener('dragleave', handleDragLeave, false);
    element.addEventListener('drop', handleDrop, false);
    element.addEventListener('dragend', handleDragEnd, false);
}

function addGroupDragListeners(element) {
    element.addEventListener('dragover', handleDragOver, false);
    element.addEventListener('dragenter', handleDragEnter, true);
    element.addEventListener('dragleave', handleDragLeave, false);
    element.addEventListener('drop', handleDropIntoGroup, false);
    // true is needed here to capture this click event instead of bubble up
    element.addEventListener('click', handleClickGroup, true);
}

function addSubgroupDragListeners(element) {
    element.addEventListener('dragover', handleDragOver, false);
    element.addEventListener('dragenter', handleDragEnter, false);
    element.addEventListener('dragleave', handleDragLeave, false);
    element.addEventListener('drop', handleDropIntoSubgroup, true);
}

// ----------------------------------------------------------------------------------------------- END DRAG AND DROP

// Helper methods ----------------------------------------------------------------------------------- HELPER METHODS

// We need two onChange methods because fuelux datepicker has two fields that need to react to change events
function addOnchangeMethodsToDatepickerElement(datepicker) {
    $(datepicker).on('changed.fu.datepicker', function (evt, date) {
        var container = this.parentElement.parentElement;
        var expression  = $(container).closest('.expression')[0];
        if (expression.expressionIndex != null) {
            backboneExpressions.at(expression.expressionIndex).set("value", $(getValueDateInputFromExpression(container))[0].value);
        }
        $queryJSON.val(printAll());
    });
    $(datepicker).on('click', function () {
        var container = this.parentElement.parentElement.parentElement;
        var expression = container.parentElement;

        if (expression.expressionIndex != null) {
            backboneExpressions.at(expression.expressionIndex).set("value", $(getValueDateInputFromExpression(container))[0].value);
        }
        $queryJSON.val(printAll());
    });
    $(getValueDateInputFromExpression(datepicker.parentElement.parentElement)).on('change', function () {
        var container = datepicker.parentElement.parentElement;
        var expression  = $(container).closest('.expression')[0];
        if (expression.expressionIndex != null) {
            backboneExpressions.at(expression.expressionIndex).set("value", $(getValueDateInputFromExpression(container))[0].value);
        }
        $queryJSON.val(printAll());
    })
}

// Moves this group and all its siblings into a new group one level above it
function encapsulateFromIndexToIndexInNewGroup(expressionGroup, fromIndex, toIndex) {
    //Create a new group to move the elements into
    var newGroup = createGroup();

    if (expressionGroup.children.length < toIndex) {
        //alert('toIndex is too big!');
    }
    if (expressionGroup.children.length < fromIndex) {
        //alert('fromIndex is too small!');
    }

    //Traverse in reverse order
    for (var i = toIndex; i >= fromIndex; i--) {
        if ($(expressionGroup.children[i]).hasClass('expression') || $(expressionGroup.children[i]).hasClass('group')) {
            if (newGroup.firstChild) {
                newGroup.insertBefore(expressionGroup.children[i], newGroup.firstChild);
            }
            else {
                newGroup.appendChild(expressionGroup.children[i]);
            }
        }
    }

    return newGroup;
}

function createGroup() {
    var div = document.createElement('div');
    div.className = 'group';
    if (editable) {
        addButtonRemove(div, 'removeGroup');
    }
//        addButtonAddGroup(div);
    if (editable) {
        addGroupDragListeners(div);
    }
    addSubgroup(div);

    return div;
}

// Button added to expression to remove itself
function addButtonRemove(element, removeClassName) {
    var button = document.createElement('button');
    var text = document.createTextNode("x");
    button.appendChild(text);
    button.className = removeClassName + ' btn btn-default btn-xs';
    element.appendChild(button);
}

// Adds keyword to expression OR group, depending on classname.
function addKeyword(element, keywordValue, keywordClassName) {
    var keyword = document.createElement('select');
    var option = document.createElement('option');
    option.text = 'and';
    keyword.add(option);
    option = document.createElement('option');
    option.text = 'or';
    keyword.add(option);
    keyword.value = keywordValue;
    keyword.classList.add(keywordClassName);
    keyword.classList.add('form-control');
    element.appendChild(keyword);
    if (!editable) {
        keyword['disabled'] = 'disabled';
    }
}

function addSubgroup(element) {
    var subgroup = document.createElement('div');
    subgroup.className = 'subgroup';
    if (editable) {
        addSubgroupDragListeners(subgroup);
    }
    $(subgroup).hide();
    element.appendChild(subgroup);
}

// This is run after we do an operation that can mess up keywords at a multi-group level.
function cleanUpKeywordGroups(groupContainer) {
    //check it we have more than one group
    var groupsNum = 0;
    var lastGroupIndex = -1;
    for (var i = 0; i < groupContainer.children.length; i++) {
        if ($(groupContainer.children[i]).hasClass('group')) {
            groupsNum++;
            lastGroupIndex = i;
        }
    }

    //delete the last keyword for this group if we have one
    if (groupsNum > 0) {
        var keywordIndex = getKeywordIndexFromElement(groupContainer.children[lastGroupIndex]);

        if (keywordIndex > -1) {
            groupContainer.children[lastGroupIndex].children[keywordIndex].remove();
        }
    }

    //delete the last expression's keyword if it exists
    if (groupsNum > 0) {
        var keywordIndex = getKeywordIndexFromElement(groupContainer.children[lastGroupIndex]);

        //delete branch
        if (keywordIndex > -1) {
            groupContainer.children[lastGroupIndex].children[keywordIndex].remove();
        }
        //add branch - add a keyword to the second to last expression if we have 2 or more (because we added one)
        //if we only have 2 expressions, set keyword to AND
        //we only add if we don't have a keyword already

        //get the first group's keyword index
        var firstGroupIndex = -1;
        var groupCount = 0;
        while (firstGroupIndex == -1) {
            if ($(groupContainer.children[groupCount]).hasClass('group')) {
                firstGroupIndex = groupCount;
            }
            else {
                groupCount++;
            }
        }
        var firstChildKeywordIndex = getKeywordIndexFromElement(groupContainer.children[firstGroupIndex]);

        //Add a keyword to the first group if we don't already have one. This can occur after a group keyword change
        if (firstChildKeywordIndex == -1) {
            if (groupsNum == 2) {
                addKeyword(groupContainer.children[firstGroupIndex], 'and', 'keywordGroup');
            } else if (groupsNum > 2) {
                //add keyword to #1 with value from 2nd group
                var secondChildKeywordIndex = getKeywordIndexFromElement(groupContainer.children[firstGroupIndex].nextSibling);

                addKeyword(groupContainer.children[firstGroupIndex],
                    $(groupContainer.children[firstGroupIndex].nextSibling.children[secondChildKeywordIndex]).val(), 'keywordGroup');
            }
        }

        if (groupsNum > 2) {
            //Only add a keyword if we don't already have one
            var checkKeywordIndex = getKeywordIndexFromElement(groupContainer.children[lastGroupIndex - 1]);

            //We can assume that (lastGroupIndex - 1) will always return an expressionGroup, since expressions are always displayed before groups
            if (checkKeywordIndex == -1) {
                //preserve the keyword value so that we properly do not need to create a new group here
                addKeyword(groupContainer.children[lastGroupIndex - 1], $(groupContainer.children[firstGroupIndex].children[firstChildKeywordIndex]).val(), 'keywordGroup');
            }
        }
    }
    else {
        //Remove the last keyword if we only have one group
        var keywordIndex = getKeywordIndexFromElement(groupContainer);
        if (keywordIndex > -1 && lastGroupIndex != -1) {
            groupContainer.children[lastGroupIndex].children[keywordIndex].remove();
        }
    }
}

// This should be executed after we do an operation that can mess up keywords within a group.
function cleanUpKeywords(expressionGroup) {
    //check it we have more than one expression
    var lastExpressionIndex = getLastChildWithClassnameIndexFromElement('expression', expressionGroup);

    //Also need to check for any groups, since we can have keywords between the last expression and the first group
    var firstGroupIndex = getFirstChildWithClassnameIndexFromElement('group', expressionGroup);

    //delete the last expression's keyword if it exists
    if (lastExpressionIndex > -1) {
        //we can use getElementsByClassName here since expression is the last step of the heirarchy
        var keywordList = expressionGroup.children[lastExpressionIndex].getElementsByClassName('keywordExpression');

        //delete branch
        if (keywordList.length != 0 && firstGroupIndex == -1) {
            keywordList[0].remove();
        }
        //add branch - add a keyword the second to last expression if we have 2 or more (because we added one)
        //if we only have 2 expressions, set keyword to AND
        //we only add if we don't have a keyword already
        if (lastExpressionIndex == 1) {
            var firstKeywordList = expressionGroup.children[0].getElementsByClassName('keywordExpression');
            if (firstKeywordList.length == 0) {
                addKeyword(expressionGroup.children[0], 'and', 'keywordExpression');
            }
        }
        else if (lastExpressionIndex > 1) {
            var secondToLastKeywordList = expressionGroup.children[lastExpressionIndex - 1].getElementsByClassName('keywordExpression');

            if (secondToLastKeywordList.length == 0) {
                //preserve the keyword value so that we properly do not need to create a new group here
                addKeyword(expressionGroup.children[lastExpressionIndex - 1],
                    $(expressionGroup.children[0].getElementsByClassName('keywordExpression')[0]).val(), 'keywordExpression');
            }
        }

        //If we have a group after our expressions, make sure we have a keyword with the last expression
        if (firstGroupIndex != -1) {
            if (keywordList.length == 0) {
                //preserve the keyword value so that we properly do not need to create a new group here
                //only if we have a keyword here already
                var toAddTo = expressionGroup.children[0].getElementsByClassName('keywordExpression')[0];
                if (toAddTo) {
                    addKeyword(expressionGroup.children[lastExpressionIndex], $(expressionGroup.children[0].getElementsByClassName('keywordExpression')[0]).val(), 'keywordExpression');
                }
                else {
                    addKeyword(expressionGroup.children[lastExpressionIndex], 'and', 'keywordExpression');
                }
            }
        }
    }
}

function createExpressionFromInput(toAddContainer, field, op, value, extraValues) {
    var expressionValue = value;
    if ($.isArray(value)) {
        expressionValue = '';
        for (var i = 0; i < value.length; i++) {
            if (i != 0) {
                //Multiselect Select2
                expressionValue += ';' + value[i];
            } else {
                expressionValue += value[i];
            }
        }
    }

    var data = new Expression({field: field, op: op, value: expressionValue});
    backboneExpressions.add(data);

    var div = document.createElement('div');
    $(getFieldFromExpression(toAddContainer)).val(field);
    $(getOperatorFromExpression(toAddContainer)).val(op);

    var selectValue = getDestroyedSelectValueFromContainer(toAddContainer);
    $(selectValue).val(value).trigger('change');

    div.appendChild(toAddContainer);
    if (editable) {
        addButtonRemove(div, 'removeExpression');
    }
    div.className = 'expression';

    if (editable) {
        div["draggable"] = true;
        addDragListeners(div);
    }
    div['expressionIndex'] = backboneExpressions.indexOf(data);
    return div;
}

function getDestroyedSelectValueFromContainer(container) {
    //Select2 is currently destroyed
    var field = $(getFieldFromExpression(container)).val()[0].value;
    var $op = $(getOperatorFromExpression(container))[0].value;

    if (AJAXFieldMap[field] == RF_TYPE_DATE) {
        return getValueDateFromExpression(container);
    } else if (AJAXFieldMap[field] == RF_TYPE_STRING) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectFromExpression(container);
        }
    }
    else if (AJAXFieldMap[field] == RF_TYPE_BOOLEAN) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectFromExpression(container);
        }
    }else if (AJAXFieldMap[field] == RF_TYPE_AUTOCOMPLETE) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectAutoFromExpression(container);
        }
    }
    return getValueSelectFromExpression(container);
}

function getCorrectSelectValueFromContainer(container) {
    var field = $(getFieldFromExpression(container)).val();
    var $op = $(getOperatorFromExpression(container))[0].value;

    if (AJAXFieldMap[field] == RF_TYPE_DATE) {
        return getValueDateFromExpression(container);
    } else if (AJAXFieldMap[field] == RF_TYPE_STRING) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectFromExpression(container);
        }
    }
    else if (AJAXFieldMap[field] == RF_TYPE_BOOLEAN) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectFromExpression(container);
        }
    }else if (AJAXFieldMap[field] == RF_TYPE_AUTOCOMPLETE) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectAutoFromExpression(container);
        }
    }
    return getValueTextFromExpression(container);
}

function showHideValue(selectedType, div) {
    switch (selectedType) {
        case EDITOR_TYPE_DATE:
            //Show datepicker
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).show();
            break;
        case EDITOR_TYPE_SELECT:
            //Show select
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).show();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_AUTOCOMPLETE:
            // Show autocomplete
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).show();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONE:
            //Hide All
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_TEXT:
        default:
            //Show text field
            $(getValueTextFromExpression(div).parentElement).show();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
    }
}

function getFieldFromExpression(container) {
    return $(container).find('select.expressionField')[0];
}

function getOperatorFromExpression(container) {
    return $(container).find('.expressionOp')[0];
}

function getValueTextFromExpression(container) {
    return $(container).find('.expressionValueText')[0];
}

function getValueSelectFromExpression(container) {
    var list = $(container).find(".expressionValueSelect");
    if (list.length > 1) {
        return list[1];
    }
    return list[0];
}

function getValueSelectAutoFromExpression(container) {
    var list = $(container).find(".expressionValueSelectAuto");
    if (list.length > 1) {
        return list[1];
    }
    return list[0];
}

function getValueDateFromExpression(container) {
    return $(container).find('.expressionValueDate')[0];
}

function getValueDateInputFromExpression(container) {
    return $(container).find('.expressionValueDateInput')[0];
}

// Returns the direct child index from parent, -1 if it is not in the list of parent's children
function getChildIndexFromParent(child, parent) {
    return Array.prototype.indexOf.call(parent.children, child);
}

// Returns the first (and should be only) keyword index from the direct children of the element or -1 if it doesn't exist
function getKeywordIndexFromElement(element) {
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass('keywordExpression') || $(element.children[i]).hasClass('keywordGroup')) {
            return i;
        }
    }
    return -1;
}

// Returns the first child index with given classname from the children of the given element or -1 if it doesn't exist
function getFirstChildWithClassnameIndexFromElement(classname, element) {
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass(classname)) {
            return i;
        }
    }
    return -1;
}

// Returns the second child index with given classname from the children of the given element or -1 if it doesn't exist
function getSecondChildWithClassnameIndexFromElement(classname, element) {
    var first = true;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass(classname)) {
            if (first) {
                first = false;
            }
            else {
                return i;
            }
        }
    }
    return -1;
}

// Returns the last child index with given classname children of the given element or -1 if it doesn't exist
function getLastChildWithClassnameIndexFromElement(classname, element) {
    var lastIndex = -1;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass(classname)) {
            lastIndex = i;
        }
    }
    return lastIndex;
}

// Returns the last expression or group index from the children of the given element or -1 if it doesn't exist
function getLastExpressionOrGroupIndexFromGroup(element) {
    var lastIndex = -1;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass('expression') || $(element.children[i]).hasClass('group')) {
            lastIndex = i;
        }
    }
    return lastIndex;
}

// Returns the total number of expressions and groups from the children of the given element
function getExpressionOrGroupCountFromGroup(element) {
    var count = 0;
    for (var i = 0; i < element.children.length; i++) {
        if ($(element.children[i]).hasClass('expression') || $(element.children[i]).hasClass('group')) {
            count++;
        }
    }
    return count;
}

function getSubgroup(expressionGroup) {
    for (var i = 0; i < expressionGroup.children.length; i++) {
        if ($(expressionGroup.children[i]).hasClass('subgroup')) {
            return expressionGroup.children[i];
        }
    }
    return null;
}

// Called when deleting - need to update expressionIndex since we edited the Backbone Collection
function checkIndexes(affectIndex) {
    var allExpr = document.getElementsByClassName('expression');

    for (var i = 0; i < allExpr.length; i++) {
        if (affectIndex < allExpr[i].expressionIndex) {
            allExpr[i].expressionIndex--;
        }
    }
}

function removeEmptyGroups(builder) {
    $.each(builder.getElementsByClassName('group'), function () {
        // this will be window if we concurrently modify this list and the element does not exist anymore.
        if (this !== window) {
            if (this.getElementsByClassName('expression').length == 0) {
                var parent = this.parentElement;
                this.remove();
                cleanUpKeywordGroups(parent);
            }
        }
    });
}

// ------------------------------------------------------------------------------------------- END GLOBAL HELPER METHODS

// Validation ----------------------------------------------------------------------------------------------- VALIDATION

$(document).on('change', '#name', function () {
    if ($(this).val().trim().length > 0) {
        $('#name').parent().removeClass('has-error');
    }
});

$(document).on('submit', '#advancedFilterForm', function (event) {
    $("#btnSubmit").attr("disabled", "disabled");
    event.preventDefault();
    if (finalizeForm()) {
        var data = $(this).serialize();
        var url = $(this).attr('action');
        $.ajax({
            type: "POST",
            url: url,
            data: data,
            dataType: 'json',
        }).done(saveAdvancedFilterSuccessCallback);
    } else {
        showErrorMessage("Please provide information for all the mandatory fields which are marked with an asterisk (*)")
        $('.expressionField')[0].focus()
        $("#btnSubmit").removeAttr("disabled")
    }

});

$(document).on('click', '.filtersWithoutSaving', function (event) {
    event.preventDefault();
    if (finalizeForm()) {
        $(".advanced-filter-dropdown").val('').trigger('change');
        closeModal();
    } else {
        showErrorMessage("Please provide information for all the mandatory fields which are marked with an asterisk (*)")
    }
});

$(document).on('click', '.deleteAdvFilter', function (event) {
    event.preventDefault();
    bootbox.confirm({
        message: $.i18n._('deleteThis'),
        buttons: {
            confirm: {
                label: 'Yes',
                className: 'btn-primary'
            },
            cancel: {
                label: 'No',
                className: 'btn-default'
            }
        },
        callback: function (result) {
            if (result) {
                var request = new Object();
                request['id'] = $('#filterId').val();
                $.ajax({
                    type: "POST",
                    url: filterDeleteUrl,
                    data: request,
                    dataType: 'json',
                }).done(deleteAdvancedFilterSuccessCallback);
            }
        }
    });
});

var saveAdvancedFilterSuccessCallback = function (result) {
    if (result.status) {
        $("#btnSubmit").removeAttr("disabled")
        closeModal();
        var option = new Option(result.data.text, result.data.id, true, true);
        $(".advanced-filter-dropdown").append(option).trigger('change');
        if(result.message){
            showErrorNotification(result.message, 10000);
        }
    } else {
        $("#btnSubmit").removeAttr("disabled")
        showErrorMessage(result.message)
    }
};

var deleteAdvancedFilterSuccessCallback = function (result) {
    if (result.status) {
        closeModal();
        location.reload()
    } else {
        showErrorMessage(result.message)
    }
};

function showErrorMessage(message) {
    var $alert = $(".msgContainer").find('.alert');
    $alert.find('.message').text(message);
    $(".msgContainer").show();

    $alert.alert();
    $alert.fadeTo(5000, 500).slideUp(500, function () {
        $alert.slideUp(500);
    });

}
function showInfoMessage(message, isShow) {
    var $msgContainer = $(".date-info-advance-filter-modal");
    var $alert = $msgContainer.find('.alert');
    $alert.find('.message').text(message);
    if(isShow) {
        $msgContainer.show();
    } else {
        $msgContainer.hide();
    }

    $alert.alert();

}

function finalizeForm() {
    printToJSON();
    if ($('#hasBlanksQuery').val() === 'true' || $(".selectedGroup").length == 0) {
        return false
    } else {
        return true
    }
}

function getSelectAutoUrl(selectedField) {
    var container = selectedField.closest('.toAddContainer')[0];
    var field = $(getFieldFromExpression(container)).select2('val');
    if (field == 'alertTags' || field == 'globalTags') {
        return fetchTagsUrl
    } else if (field == 'assignedTo.id' || field == 'assignedToGroup.id') {
        return fetchUsersUrl
    }
    return selectAutoUrl

}

function closeModal() {
    $('#createAdvancedFilterModal').modal('hide');
}

$(document).on('click', '#eventGroupSelection,#productGroupSelection', function () {
    $(this).closest('.toAddContainer').find('.expressionOp').trigger('change');

});

$(function(){
    $('#selectField').on('change',function(){
        var selected_operator=$('#selectOperator').find(":selected").val();
        if($('#showValue').css("display")=='none' && ((selected_operator=='IS_EMPTY') || (selected_operator=='IS_NOT_EMPTY')))
        {$('#showValue').css("display","block");}
    });
});

// ------------------------------------------------------------------------------------------------------ END VALIDATION
