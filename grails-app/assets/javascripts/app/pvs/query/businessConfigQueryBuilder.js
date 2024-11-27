// AJAX Implementation
// These fields are used to store AJAX
var AJAXFieldMap = [];
var AJAXValueSampleList;
var AJAXOperatorStringList;
var AJAXOperatorNumList;
var AJAXOperatorDateList;
var AJAXOperatorValuelessList;
// ---------------------------END AJAX

// CONSTANTS
var RF_TYPE_STRING = 'string';
var RF_TYPE_DATE = 'date';
var RF_TYPE_NUMBER = 'number';
var RF_TYPE_PART_DATE = 'partialDate';

var EDITOR_TYPE_TEXT = 0
var EDITOR_TYPE_DATE = 1
var EDITOR_TYPE_SELECT = 2
var EDITOR_TYPE_NONE = 3

// DATE OPERATOR CONSTANTS - these are the names, not values of the QueryOperator enum.
var EQUALS_OPERATORS = ['EQUALS', 'NOT_EQUAL'];

// Extra values
// Re-assess Listedness
// Blank Parameters Implementation
var hasBlanks = false;
var keys = [];
// -----------END BLANK PARAMETERS

//Tabs
var activeTab='QUERY_BUILDER'; // current visible tab

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

//setBuilder
var $selectedQuery;
var selectedSetGroup;
var setBuilderAll;
var setBuilderSubgroup;
var $setJSON;

// Tabs
var $queryBuilder;
var $queryBuilderTab;
var $queryBuilderLink;
var $setBuilder;
var $setBuilderTab;
var $setBuilderLink;
var $customSQL;
var $customSQLTab;
var $customSQLLink;

// Backbone
// Definitions
var Expressions;
//setBuilder
var Sets;
var Expression;
var ExpressionList;
//setBuilder
var SetList;
// Variables
// This Backbone collection is used to store our Backbone expressions
var backboneExpressions;
// We render on initialize, so this variable is not called explicitly
var expressionList;
//setBuilder
// This Backbone collection is used to store our Backbone expressions
var backboneSets;
// We render on initialize, so this variable is not called explicitly
var copyAndPastesFields = {}

$(function () {
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
    //setBuilder
    $selectedQuery = $('#selectQuery');
    setBuilderAll = document.getElementById('setBuilderAll');

    $setJSON = $('#setJSON');
    // Tabs
    $queryBuilder = $('#queryBuilder');
    $queryBuilderTab = $('#queryBuilderTab');
    $queryBuilderLink = $('#queryBuilderLink');
    $setBuilder = $('#setBuilder');
    $setBuilderTab = $('#setBuilderTab');
    $setBuilderLink = $('#setBuilderLink');
    $customSQL = $('#customSQL');
    $customSQLTab = $('#customSQLTab');

    $customSQLLink = $('#customSQLLink');

    // Backbone is used to easily model the expressions we use ------------------------------------------------ BACKBONE

    Expressions = Backbone.Collection.extend({});

    //setBuilder
    Sets = Backbone.Collection.extend({});


    Expression = Backbone.Model.extend({
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

            //this isn't necessary, but doing it to test if we get the same result as we saved
            $queryJSON.val(printAll());
        }
    });

    // ---------------------------------------------------------------------------------------------------- END BACKBONE


    // Tab -------------------------------------------------------------------------------------------------------------

    var queryType = "QUERY_BUILDER";
    activeTab = queryType;
    $('#queryType').val(queryType);

    $('#errorMessageOperator').hide();

    // This Backbone collection is used to store our Backbone expressions
    backboneExpressions = new Expressions();

    //setBuilder
    // This Backbone collection is used to store our Backbone expressions
    backboneSets = new Expressions();

    expressionList = new ExpressionList();

    // On success, call these update methods to fill in UI values)
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
            case RF_TYPE_STRING: //string and text
                $.each(AJAXOperatorStringList, function (display, value) {
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

    //only execute this after operator values have been assigned
    function updateAJAXValues(container) {
        var field = $(getFieldFromExpression(container)).val();
        var selectValue = getValueSelectFromExpression(container);
        $(selectValue).empty();
        if (_.has(copyAndPastesFields, field)) {
            showHideValue(EDITOR_TYPE_TEXT, container)
        } else if (AJAXFieldMap[field] != RF_TYPE_DATE) {
            if (AJAXValueSampleList[field].length != 0) {
                $(selectValue).append($("<option></option>"));
                $.each(AJAXValueSampleList[field], function () {
                    $(selectValue).append($("<option></option>").attr("value", this).text(this));
                });
                showHideValue(EDITOR_TYPE_SELECT, container);
            } else {
                showHideValue(EDITOR_TYPE_TEXT, container);
            }
        } else {
            showHideValue(EDITOR_TYPE_DATE, container);
        }
    }

    // -------------------------------------------------------------------------------------------------------- END AJAX

    // Change events are captured by classname ---------------------------------------------------------------- onChange

    //setBuilder
    $(document).on('change', '.keywordExpression', function () {
        var expression = this.parentElement;
        var expressionGroup = expression.parentElement;

        //check it we have more than one expression
        var expressionsNum = getExpressionOrGroupCountFromGroup(expressionGroup);

        var changed = false;

        //No need to do anything if the change is between 2 expressions
        if (expressionsNum > 2) {
            //Compare to the first expression's keyword if it is not the first keyword
            //keywordIndex and firstExpressionIndex should have a value since expressionCount > 1
            var firstExpressionIndex = getFirstChildWithClassnameIndexFromElement('expression', expressionGroup);
            var keywordIndex = getKeywordIndexFromElement(expressionGroup.children[firstExpressionIndex]);


            if (this !== expressionGroup.children[firstExpressionIndex].children[keywordIndex]) {
                if ($(this).val() != $(expressionGroup.children[firstExpressionIndex].children[keywordIndex]).val()) {
                    changed = true;
                }
            }
            //Compare to the second if we are changing the first expression's keyword. We should not need to compare the case when we are comparing the last keyword, because the keyword select box isn't present.
            else {
                var secondExpressionIndex = getSecondChildWithClassnameIndexFromElement('expression', expressionGroup);
                //If we have one expression and the rest are groups, use the first group's index instead of the second expression
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

                //second half
                var secondHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroup, expressionIndex + 1, getLastExpressionOrGroupIndexFromGroup(expressionGroup));
                expressionGroup.insertBefore(secondHalfGroup, expressionGroup.children[expressionIndex].nextSibling);

                //first half
                var firstHalfGroup = encapsulateFromIndexToIndexInNewGroup(expressionGroup, 0, expressionIndex);
                expressionGroup.insertBefore(firstHalfGroup, expressionGroup.firstChild);

                //alter first half's keyword
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

    // On click methods ---------------------------------------------------------------------------------------- onClick

    $(document).on('click', '.removeExpression', function () {
        var expressionGroup = this.parentElement.parentElement;
        var affectIndex = this.parentElement.expressionIndex;
        backboneExpressions.remove(backboneExpressions.at(affectIndex));
        checkIndexes(affectIndex);
        this.parentElement.remove();

        if (expressionGroup.children.length > 0) {
            cleanUpKeywords(expressionGroup);
        }

        // Re-assess Listedness
        showHideReassessListedness(builderAll);

        printToJSON();
    });

    $(document).on('click', '.removeGroup', function (e) {
        e.preventDefault();
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

    //////////////////////////////////////////////
    // Method called when add rule icon clicked //
    //////////////////////////////////////////////
    // Add button to add a new expression
    $('#addExpression').on('click', function () {

        $('#errorMessageOperator').hide();
        var $addExpression = true;

        if ($addExpression) {
            var baseContainer = document.getElementById('toAddContainer');
            var field = $(getFieldFromExpression(baseContainer)).val();
            var op;
            var value;
            var isDate = false;
            var containerToAdd =  baseContainer.cloneNode(true);
            $(containerToAdd).removeAttr("id");
            disablePercentValue($(containerToAdd), true);
            $(getValueDateFromExpression(containerToAdd)).datepicker({
                allowPastDates: true
            });

            if (isDate) {
                $(getValueDateFromExpression(containerToAdd)).datepicker('setDate', value);
            }
            if (value == null || value == '') {
                $(getValueDateInputFromExpression(containerToAdd)).val('');
            }

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

            if (lastExpression > -1) {
                expressionGroup.insertBefore(createExpressionFromInput(containerToAdd, field, op, value, extraValues), expressionGroup.children[lastExpression].nextSibling);
            }
            else {
                if (expressionGroup.children.length > 0) {
                    expressionGroup.insertBefore(createExpressionFromInput(containerToAdd, field, op, value, extraValues), expressionGroup.children[0]);
                }
            }

            // This removes the empty value "" that gets appended when we destroy and then reinitialize this as select2.
            var selectArray = $('#selectSelect').val();
            //Type check is introduced.
            if (typeof selectArray != "undefined" && selectArray != null) {
                for(var i = selectArray.length; i--;) {
                    if(selectArray[i] === "") {
                        selectArray.splice(i, 1);
                    }
                }
            }

            cleanUpKeywords(expressionGroup);
            printToJSON();
        }
    });

});

// JSON building and parsing ---------------------------------------------------------------------------------- JSON

function printAll() {
    hasBlanks = false;
    keys = [];

    var container = document.getElementsByClassName('builderAll')[0];

    var result = '{"all":{"containerGroups":[' + printRecurJSON(container);

    //print copyAndPasteFields
    if(!$.isEmptyObject(copyAndPastesFields)) {
        result += ', "copyAndPasteFields":['
        _.each(copyAndPastesFields, function(it) {
            it.content = ""
            var jsonStr = JSON.stringify(it)
            result += jsonStr + ','
        })
        result = result.slice(0, -1)
        result += ']'
    }

    result += '}';

       try {
           JSON.parse(result);
       } catch (e) {
           console.log("Result json parsing failed");
       }

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
            var obj;
            obj = $(expressionGroup.children[i]);
            var threshold = obj.find('.expressionThreshold').val() ? obj.find('.expressionThreshold').val() : obj.find('.expressionThreshold-combo').val()
            tempResult += '{"index":"' + expressionGroup.children[i].expressionIndex
                + '","category":"' + obj.find('.expressionCategory').val()
                + '","attribute":"' + obj.find('.expressionAttribute').val()
                + '","operator":"' + obj.find('.expressionOp').val()
                + '","percent":"' + obj.find('.percentValue').attr('data-percent-value')
                + '","threshold":"' + threshold
                if(importantEventsValues.indexOf(obj.find('.expressionAttribute').val())!=-1){
                    tempResult += '","isProductSpecific":"' + obj.find('#isProductSpecific').is(':checked')
                }
                if(obj.find('.expressionCategory').val()=="SIGNAL_REVIEW_STATE"){
                    tempResult += '","splitSignalToPt":"' + obj.find('#splitSignalToPt').is(':checked')
                        + '","assClosedSignal":"' + obj.find('#assClosedSignal').is(':checked')
                        + '","assMultSignal":"' + obj.find('#assMultSignal').is(':checked')
                }
                if(obj.find('.expressionCategory').val()=="QUERY_CRITERIA"){
                    tempResult += '","queryName":"' + obj.find('.expressionAttribute  option[value='+obj.find('.expressionAttribute').val()+']').text()
                }
                tempResult += '"';
            tempResult += '}';
            if (firstExpressionIndex == -1) {
                firstExpressionIndex = i;
            }
            needComma = true;
        }
        else if (expressionGroup.children[i].classList.contains('group')) {
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

    buildCopyAndPasteFields(parsedJSON, containerGroups)

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
        disablePercentValue($(container), true);
        $(container).find('.expressionCategory').val(groupJSON.category);
        $(container).find('.expressionOp').val(groupJSON.operator);
        if(groupJSON.percent != undefined) {
            $(container).find('.percentValue').attr("data-percent-value", groupJSON.percent);
            if(groupJSON.percent != "")
                $(container).find('.percentValue').attr("title", groupJSON.percent+ "%");
        }
        if(groupJSON.category =='ALGORITHM' || groupJSON.category=='COUNTS'){
            $(container).find('.threshold-combo').removeClass('hide');
            $(container).find('.threshold').hide();
            editThresholdFields(groupJSON,$(container));
            if(groupJSON.attribute == PREVIOUS_CATEGORY || groupJSON.attribute == ALL_CATEGORY){
                groupJSON.threshold.split(",").forEach(function(obj){
                    if($(container).find(".expressionThreshold-combo option[value='"+obj+"']").length ==0) {
                        $(container).find('.expressionThreshold-combo').append(new Option(obj, obj, true, true));
                    }
                    if(groupJSON.threshold.split(",").length > 0)
                        $(container).find('.expressionThreshold-combo').val(groupJSON.threshold.split(",")).trigger('change');
                });
            }else{
                $(container).find('.expressionThreshold-combo').append(new Option(groupJSON.threshold, groupJSON.threshold, true, true));
                $(container).find('.expressionThreshold-combo').val(groupJSON.threshold).trigger('change');
            }
            if (!$(container).find('.expressionThreshold-combo option:selected').val()) {
                if (groupJSON.threshold == null) {
                    $(container).find('.expressionThreshold-combo').append(new Option('', null, true, true));
                    $(container).find('.expressionThreshold-combo').val(null).trigger('change');
                } else {
                    if(groupJSON.attribute == PREVIOUS_CATEGORY || groupJSON.attribute == ALL_CATEGORY){
                        groupJSON.threshold.split(",").forEach(function(obj){
                            if($(container).find(".expressionThreshold-combo option[value='"+obj+"']").length ==0) {
                                $(container).find('.expressionThreshold-combo').append(new Option(obj, obj, true, true));
                            }
                            if(groupJSON.threshold.split(",").length > 0)
                                $(container).find('.expressionThreshold-combo').val(groupJSON.threshold.split(",")).trigger('change');

                        });
                    }else{
                        $(container).find('.expressionThreshold-combo').append(new Option(groupJSON.threshold, groupJSON.threshold, true, true));
                        $(container).find('.expressionThreshold-combo').val(groupJSON.threshold).trigger('change');
                    }
                }
            }
        }else{
            $(container).find('.threshold-combo').addClass('hide');
            $(container).find('.threshold').show();
            $(container).find('.expressionThreshold').val(groupJSON.threshold);
        }
        var ddl2 = $(container).find('.expressionAttribute');
        generatedAttributeList(ddl2,groupJSON.category);
        ddl2.val(groupJSON.attribute);
        if(importantEventsValues.indexOf(groupJSON.attribute)!=-1){
            $(container).find('.isProductSpecific').removeClass('hidden');
            if(groupJSON.isProductSpecific === undefined || groupJSON.isProductSpecific == null || groupJSON.isProductSpecific == "false"){
                $(container).find('#isProductSpecific').prop('checked',false)
            }else{
                $(container).find('#isProductSpecific').prop('checked',true)
            }
        }
        if(groupJSON.category=="SIGNAL_REVIEW_STATE"){
            $(container).find('.checkBoxDiv').removeClass('hide');
            if(groupJSON.splitSignalToPt === undefined || groupJSON.splitSignalToPt == null || groupJSON.splitSignalToPt == "false"){
                $(container).find('#splitSignalToPt').prop('checked',false)
            }else{
                $(container).find('#splitSignalToPt').prop('checked',true)
            }
            if(groupJSON.assClosedSignal === undefined || groupJSON.assClosedSignal == null || groupJSON.assClosedSignal == "false"){
                $(container).find('#assClosedSignal').prop('checked',false)
            }else{
                $(container).find('#assClosedSignal').prop('checked',true)
            }
            if(groupJSON.assMultSignal === undefined || groupJSON.assMultSignal == null || groupJSON.assMultSignal == "false"){
                $(container).find('#assMultSignal').prop('checked',false)
            }else{
                $(container).find('#assMultSignal').prop('checked',true)
            }
        }
        expression.appendChild(container);
        if (keyword) {
            addKeyword(expression, keyword, 'keywordExpression');
        }
        if (editable) {
            addButtonRemove(expression, 'removeExpression');
        }
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
        $(container).find('.expressionThreshold-combo option').each(function() {
            $(this).siblings('[value="'+ this.value +'"]').remove();
        });
    }
}

// Below methods are unused but kept for posterity

function buildCopyAndPasteFields(jsonObj, containerGroups) {
    if (typeof(jsonObj.copyAndPasteFields) !== 'undefined') {
        _.each(jsonObj.copyAndPasteFields, function(it) {
            copyAndPastesFields[it.field] = {field: it.field, content: null, delimiter: it.delimiter}
        })
    }

    if (!_.isEmpty(containerGroups)) {
        _.each(containerGroups, function(group) {
            var expressions = group.expressions
            _.each(expressions, function (it) {
                if (_.find(copyAndPastesFields, function(item){return item.field === it.field})) {
                    copyAndPastesFields[it.field].content = it.value
                }
            })
        })
    }
}

//setBuilder
function printToJSON() {
        $queryJSON.val(printAll());
}

// Drag and Drop ------------------------------------------------------------------------------------- Drag and Drop

function handleDragStart(e) {
    this.style.opacity = '0.4';
    dragSourceEl = this;

    //set drag from group
    draggingFromGroup = this.parentElement;

    //we don't actually use any dataTransfer.
    //we drag the expressionIndex, the index of the model in our list of expressions, called expressions
//        e.dataTransfer.setData('application/x-moz-node', this.getElementsByClassName('toAddContainer')[0]);

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
        if (activeTab === 'QUERY_BUILDER') {
            swapElement = this.getElementsByClassName('toAddContainer')[0];
            this.insertBefore(dragSourceEl.getElementsByClassName('toAddContainer')[0], this.children[0]);
        } else if (activeTab === 'SET_BUILDER') {
            swapElement = this.getElementsByClassName('toAddContainerSet')[0];
            this.insertBefore(dragSourceEl.getElementsByClassName('toAddContainerSet')[0], this.children[0]);
        }
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
    if (activeTab === 'QUERY_BUILDER') {
        $(builderSubgroup).hide();
    } else if (activeTab === 'SET_BUILDER') {
        $(setBuilderSubgroup).hide();
    }

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
    if (activeTab === 'QUERY_BUILDER') {
        $(selectedGroup).removeClass('selectedGroup');
        selectedGroup = this;
        $(selectedGroup).addClass('selectedGroup');
    } else if (activeTab === 'SET_BUILDER') {
        $(selectedSetGroup).removeClass('selectedGroup');
        selectedSetGroup = this;
        $(selectedSetGroup).addClass('selectedGroup');
    }
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
    if (activeTab == 'QUERY_BUILDER') {
        option.text = 'and';
        keyword.add(option);
        option = document.createElement('option');
        option.text = 'or';
        keyword.add(option);
        keyword.value = keywordValue;
    } else if (activeTab === 'SET_BUILDER') {
        option.text = 'intersect';
        keyword.add(option);
        option = document.createElement('option');
        option.text = 'union';
        keyword.add(option);
        option = document.createElement('option');
        option.text = 'minus';
        keyword.add(option);
        keyword.value = keywordValue;
        keyword.classList.add('extra-width');
    }
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
                if (activeTab === 'QUERY_BUILDER') {
                    addKeyword(groupContainer.children[firstGroupIndex], 'and', 'keywordGroup');
                } else if (activeTab === 'SET_BUILDER') {
                    addKeyword(groupContainer.children[firstGroupIndex], 'intersect', 'keywordGroup');
                }
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
                if (activeTab === 'QUERY_BUILDER') {
                    addKeyword(expressionGroup.children[0], 'and', 'keywordExpression');
                } else if (activeTab === 'SET_BUILDER') {
                    addKeyword(expressionGroup.children[0], 'intersect', 'keywordExpression');
                }
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
                    if (activeTab === 'QUERY_BUILDER') {
                        addKeyword(expressionGroup.children[lastExpressionIndex], 'and', 'keywordExpression');
                    } else if (activeTab === 'SET_BUILDER') {
                        addKeyword(expressionGroup.children[lastExpressionIndex], 'intersect', 'keywordExpression');
                    }
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

function getCorrectSelectValueFromContainer(container) {
    var field = $(getFieldFromExpression(container)).val();
    var $op = $(getOperatorFromExpression(container))[0].value;

    if (_.has(copyAndPastesFields, field)) {
        return getValueTextFromExpression(container)
    } else if (AJAXFieldMap[field] == RF_TYPE_DATE) {
        return getValueDateFromExpression(container);
    } else if (AJAXFieldMap[field] == RF_TYPE_STRING) {
        if (_.contains(EQUALS_OPERATORS, $op)) {
            return getValueSelectFromExpression(container);
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
            $(getValueDateFromExpression(div).parentElement).show();
            break;
        case EDITOR_TYPE_SELECT:
            //Show select
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).show();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONE:
            //Hide All
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_TEXT:
        default:
            //Show text field
            $(getValueTextFromExpression(div).parentElement).show();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
    }
}

function getFieldFromExpression(container) {
}

function getOperatorFromExpression(container) {
}

function getValueTextFromExpression(container) {
}

function getValueSelectFromExpression(container) {
}

function getValueDateFromExpression(container) {
    return container.getElementsByClassName('expressionValueDate')[0];
}

function getValueDateInputFromExpression(container) {
    return container.getElementsByClassName('expressionValueDateInput')[0];
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
    $.each(builder.getElementsByClassName('group'), function() {
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

// Extra Values
// Re-assess Listedness
function showHideReassessListedness(builder) {
}

// ------------------------------------------------------------------------------------------- END GLOBAL HELPER METHODS

// Validation ----------------------------------------------------------------------------------------------- VALIDATION

$(document).on('change', '#name', function () {
    if ($(this).val().trim().length > 0) {
        $('#name').parent().removeClass('has-error');
    }
});

// Re-assess Listedness
function sendReassessListedness() {
    var $reassessListedness = $('#reassessListedness')
    if (activeTab !== 'QUERY_BUILDER' || !$reassessListedness.is(":visible")) {
        $reassessListedness.removeAttr('name');
    }
}

function finalizeForm() {
    sendReassessListedness();

    if (activeTab === 'QUERY_BUILDER') {
        $('#setJSON').removeAttr('name');
        removeEmptyGroups(builderAll);
    } else if (activeTab === 'SET_BUILDER') {
        $('#queryJSON').removeAttr('name');
        $('#hasBlanksQuery').val(false);
        removeEmptyGroups(setBuilderAll);
    }

    printToJSON();

    return true;
}

// ------------------------------------------------------------------------------------------------------ END VALIDATION

//Business Configuration Specific
function generatedAttributeList(ddl2,category) {
    ddl2.find('option').remove().end();
    createOption(ddl2, "Select Attribute","")
    var optionsArray = [];
    $.each(selectData, function (index, obj) {
        if (obj.type == category) {
            optionsArray.push(obj);
        }
    });
    optionsArray.sort(function(a, b) {
        return a.text.localeCompare(b.text);
    });
    $.each(optionsArray, function (index, obj) {
        createOption(ddl2, obj.text, obj.value);
    });
}

function createOption(ddl, text, value) {
    ddl.append('<option value="' + escapeAllHTML(value) + '">' + text + '</option>');
}


function percentClicked(me) {
    $("#percentValue").val($(me).attr('data-percent-value'));
    $('.savePercentValue').off().on('click', function (e) {
        $(me).attr('data-percent-value', $("#percentValue").val());
        if($("#percentValue").val() != "" && $("#percentValue").val() != undefined)
            $(me).attr('title', $("#percentValue").val() + "%");
        else
            $(me).attr('title', "");
        $('#percentModal').modal('hide');
    });
}

function numberValidate(){
    var e = document.getElementById('percentValue');
    if (!/^[0-9]+$/.test(e.value)) {
        e.value = e.value.substring(0,e.value.length-1);
    }
}