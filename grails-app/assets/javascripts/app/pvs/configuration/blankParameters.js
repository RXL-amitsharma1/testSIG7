// These fields are used to store AJAX
var AJAXValueSampleList = [];
var AJAXOperatorStringList;
var AJAXOperatorNumList;
var AJAXOperatorDateList;
var AJAXOperatorValuelessList;
// We have 7 AJAX calls, so we update values after the 5th is complete
var AJAXFinished = 3;
var AJAXCount = 0;
// ---------------------------END AJAX

// CONSTANTS
var RF_TYPE_TEXT = 'text';
var RF_TYPE_STRING = 'string';
var RF_TYPE_DATE = 'date';
var RF_TYPE_NUMBER = 'number';
var RF_TYPE_PART_DATE = 'partialDate';
var RF_TYPE_AUTOCOMPLETE = 'autocomplete';
var RF_TYPE_NOCACHE_DROP_DOWN = 'nocachedropdown';

var EDITOR_TYPE_TEXT = 0;
var EDITOR_TYPE_DATE = 1;
var EDITOR_TYPE_SELECT = 2;
var EDITOR_TYPE_NONE = 3;
var EDITOR_TYPE_AUTOCOMPLETE = 4;
var EDITOR_TYPE_NONECACHE_SELECT = 5;


// DATE OPERATOR CONSTANTS - these are the names, not values of the QueryOperator enum.
var LAST_X_DATE_OPERATORS = ['LAST_X_DAYS', 'LAST_X_WEEKS', 'LAST_X_MONTHS', 'LAST_X_YEARS'];
var NEXT_X_DATE_OPERATORS = ['NEXT_X_DAYS', 'NEXT_X_WEEKS', 'NEXT_X_MONTHS', 'NEXT_X_YEARS'];
var RELATIVE_DATE_OPERATORS = ['YESTERDAY', 'LAST_WEEK', 'LAST_MONTH', 'LAST_YEAR', 'TOMORROW', 'NEXT_WEEK', 'NEXT_MONTH', 'NEXT_YEAR'];
var IS_EMPTY_OPERATORS = ['IS_EMPTY', 'IS_NOT_EMPTY'];
var EQUALS_OPERATORS = ['EQUALS', 'NOT_EQUAL'];
var CONTAINS_OPERATORS = ['CONTAINS', 'DOES_NOT_CONTAIN'];

$(function () {
    $.each(document.getElementsByClassName('doneLoading'), function () {
        $(this).hide();
    });

    $.each(document.getElementsByClassName('loading'), function () {
        $(this).show();
    });

    $('#selectDate').datepicker({
        allowPastDates: true
    });
    $('.errorMessageOperator').hide();
    getAJAXValues();

    if ($('#blankValuesJSON').val() != '') {
    }
});

function getFieldType(field) {
    var type = '';
    if (field.data('isnoncacheselectable')) {
        type = RF_TYPE_NOCACHE_DROP_DOWN;
    } else if (field.data('isautocomplete')) {
        type = RF_TYPE_AUTOCOMPLETE;
    } else {
        switch (field.data('datatype')) {
            case 'java.lang.String':
                if (field.data('istext')) {
                    type = RF_TYPE_TEXT;
                } else {
                    type = RF_TYPE_STRING;
                }
                break;
            case 'java.util.Date':
                type = RF_TYPE_DATE;
                break;
            case 'java.lang.Number':
                type = RF_TYPE_NUMBER;
                break;
            case 'com.rxlogix.config.PartialDate':
                type = RF_TYPE_PART_DATE;
                break;
        }
    }
    return type;
}

function getAJAXValues() {
    getDateOperatorsAJAX();
    getNumOperatorsAJAX();
    getStringOperatorsAJAX();
    getValuelessOperatorsAJAX();
}

function getDateOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: dateOperatorsUrl,
        dataType: 'json',
    })
        .done(function (result) {
            AJAXOperatorDateList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            } else {
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
            } else {
                AJAXCount++;
            }
        });
}

function getStringOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: stringOperatorsUrl,
        dataType: 'json',
    })
        .done(function (result) {
            AJAXOperatorStringList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            } else {
                AJAXCount++;
            }
        });
}

function getValuelessOperatorsAJAX() {
    $.ajax({
        type: "GET",
        url: valuelessOperatorsUrl,
        dataType: 'json',
    })
        .done(function (result) {
            AJAXOperatorValuelessList = result;
            if (AJAXCount == AJAXFinished) {
                updateInitialAJAXLists();
            } else {
                AJAXCount++;
            }
        });
}

// On success, call these update methods to fill in UI values
function updateAJAXFields(container) {
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));

    var op = getOperatorFromExpression(container);

    $(op).empty();

    switch (selectedFieldType) {
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
        case RF_TYPE_NOCACHE_DROP_DOWN: // NonCacheSelect uses same operators as string
        case RF_TYPE_AUTOCOMPLETE: // Autocomplete uses same operators as string
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

function updateAJAXOperators(container) {
    var field = $(getFieldFromExpression(container)).val();
    var operator = $(getOperatorFromExpression(container)).val();
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));


    if (_.contains(IS_EMPTY_OPERATORS, operator)) {
        showHideValue(3, container);
    } else if (selectedFieldType == RF_TYPE_DATE) {
        if (_.contains(LAST_X_DATE_OPERATORS, operator) || _.contains(NEXT_X_DATE_OPERATORS, operator)) {
            showHideValue(0, container);
        } else if (_.contains(RELATIVE_DATE_OPERATORS, operator)) {
            showHideValue(3, container);
        } else {
            showHideValue(1, container);
        }
    } else if (selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
        if ((_.contains(EQUALS_OPERATORS, operator))) {
            showHideValue(EDITOR_TYPE_NONECACHE_SELECT, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    } else if (selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
        if ((_.contains(EQUALS_OPERATORS, operator))) {
            showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    } else if (selectedFieldType == RF_TYPE_STRING) {
        if (operator != "" && (_.contains(EQUALS_OPERATORS, operator)) && AJAXValueSampleList && typeof AJAXValueSampleList[field] !== "undefined" && AJAXValueSampleList[field].length > 0) {
            showHideValue(2, container);
        } else {
            showHideValue(0, container);
        }
    } else if (selectedFieldType == RF_TYPE_PART_DATE) {
        if (_.contains(RELATIVE_DATE_OPERATORS, operator)) {
            showHideValue(3, container);
        } else {
            showHideValue(0, container);
        }
    } else {
        showHideValue(0, container);
    }
}

//only execute this after operator values have been assigned
function updateAJAXValues(container) {
    var field = $(getFieldFromExpression(container)).val();
    var selectedFieldType = getFieldType($(getFieldFromExpression(container)).find(":selected"));
    var selectValue = getValueSelectFromExpression(container);
    $(selectValue).empty();
    var operator = $(getOperatorFromExpression(container))[0]?.value;
    if (_.contains(CONTAINS_OPERATORS, operator)) {
        showHideValue(EDITOR_TYPE_TEXT, container);
    } else if (selectedFieldType != RF_TYPE_DATE) {
        if ((_.contains(EQUALS_OPERATORS, operator)) && selectedFieldType == RF_TYPE_AUTOCOMPLETE) {
            showHideValue(EDITOR_TYPE_AUTOCOMPLETE, container);
        } else if ((_.contains(EQUALS_OPERATORS, operator)) && selectedFieldType == RF_TYPE_NOCACHE_DROP_DOWN) {
            showHideValue(EDITOR_TYPE_NONECACHE_SELECT, container);
        } else if ((_.contains(EQUALS_OPERATORS, operator))) {
            loadValues(field, function (values) {
                if (values && values.length !== 0) {
                    $(selectValue).append($("<option></option>").attr("value", this).text(null));
                    $.each(values, function () {
                        $(selectValue).append($("<option></option>").attr("value", this).text(this));
                    });
                    showHideValue(EDITOR_TYPE_SELECT, container);
                } else {
                    showHideValue(EDITOR_TYPE_TEXT, container);
                }
            });
        } else {
            showHideValue(EDITOR_TYPE_TEXT, container);
        }
    } else {
        showHideValue(1, container);
    }

}

function loadValues(field, callback) {
    $.ajax({
        type: "GET",
        url: possibleValuesUrl,
        async: false,
        data: {
            field: field
        },
        dataType: 'json',
    })
        .done(function (result) {
            AJAXValueSampleList[field] = result;
            callback(result);
        })
        .fail(function (err) {
            console.log(err);
        });
}

//need to grab and set our next operators and values from field values. This should only run once on page load
function updateInitialAJAXLists() {
    $.each($('#templateQueriesContainer').find('.expression'), function () {
        var container = $(this).find('.toAddContainerQEV')[0];
        updateAJAXFields(container);
        updateAJAXValues(container);
        updateAJAXOperators(container);
    });

    $.each($('.alertQueryWrapper'), function () {
        var templateQueryNamePrefix = "";
        var $selectedField;
        var count0 = 0;
        $.each($(this).find('.queryExpressionValues .queryBlankContainer'), function () {
            var it = this;
            setFieldSelect($(getFieldFromExpression(it)), it.querySelector('input.qevReportField').value, null, count0, function (cnt, result1) {
                if ($(it).hasClass('toAddContainerQEV')) {
                    $(getFieldFromExpression(it)).attr('disabled', 'disabled');
                    $(getOperatorFromExpression(it)).val(it.querySelector('input.qevOperator').value).trigger('change');
                    $(getOperatorFromExpression(it)).attr('disabled', 'disabled');
                    $(getValueDateFromExpression(it)).datepicker({
                        allowPastDates: true
                    });
                    $(getFieldFromExpression(it)).attr('name', 'qev[' + cnt + '].field');
                    $(getOperatorFromExpression(it)).attr('name', 'qev[' + cnt + '].operator');
                    $(getKeyFromExpression(it)).attr('name', 'qev[' + cnt + '].key');
                    var showEvent = false, showPencil = false;
                    if (result1.dictionary == "EVENT" && result1.dataType !== "java.util.Date") {
                        showEvent = true;
                    }
                    if (result1.dataType !== "java.util.Date") {
                        showPencil = true;
                    }
                    $(getFieldFromExpression(it)).attr('data-validatable', result1.validatable);
                    showHidePencilEventIcon(it, showPencil, showEvent);
                    if ($(getValueSelectFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueSelectFromExpression(it));
                        $selectedField.val(splitStringListIntoArray(it.querySelector('input.qevValue').value)).trigger('change');
                    } else if ($(getValueSelectAutoFromExpression(it)).is(":visible")) {
                        var that = this;
                        $selectedField = $(getValueSelectAutoFromExpression(it));
                        const selectFieldId = $selectedField.attr('id');
                        $selectedField.removeAttr('id').select2({
                            minimumInputLength: 2,
                            multiple: true,
                            ajax: {
                                quietMillis: 300,
                                dataType: "json",
                                url: selectAutoUrl,
                                data: function (params) {
                                    var field = $(getFieldFromExpression(it)).val();
                                    return {
                                        field: field,
                                        term: params.term,
                                        max: params.page || 10,
                                        lang: userLocale
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
                        }).attr('id', selectFieldId);
                        var value = splitStringListIntoArray(it.querySelector('input.qevValue').value);
                        var arrayData = [];
                        if ($.isArray(value)) {
                            value = $.grep(value, function (item) {
                                return item != ""
                            });
                            _.each(value, function (item) {
                                arrayData.push({id: item, text: item});
                            });
                        }
                        for (var i = 0; i < arrayData.length; i++) {

                            var option = new Option(arrayData[i].text, arrayData[i].id, true, true);
                            $(getValueSelectAutoFromExpression(it)).append(option).trigger('change');
                        }

                        // // manually trigger the `select2:select` event
                        $(getValueSelectAutoFromExpression(it)).trigger({
                            type: 'select2:select',
                            params: {
                                data: arrayData
                            }
                        });
                    } else if ($(getValueTextFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueTextFromExpression(it));
                        $selectedField.val(it.querySelector('input.qevValue').value);
                    } else if ($(getValueDateInputFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueDateInputFromExpression(it));
                        $selectedField.val(it.querySelector('input.qevValue').value);
                    } else if ($(getValueSelectNonCacheFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueSelectNonCacheFromExpression(it));
                        const selectFieldId = $selectedField.attr('id')
                        $selectedField.removeAttr('id').select2({
                            minimumInputLength: 0,
                            separator: ";",
                            multiple: true,
                            ajax: {
                                quietMillis: 300,
                                dataType: "json",
                                url: selectNonCacheUrl,
                                data: function (params) {
                                    var field = $(getFieldFromExpression(it)).val();
                                    return {
                                        field: field,
                                        term: params.term,
                                        max: params.page || 10,
                                        lang: userLocale
                                    };
                                },
                                processResults: function (data, params) {
                                    params.page = params.page || 10;
                                    return {
                                        results: data.list,
                                        pagination: {
                                            more: (params.page * 10) < data.totalCount
                                        }
                                    };
                                }
                            }
                        }).attr('id',selectFieldId);
                        var convertedValue = splitStringListIntoArray(it.querySelector('input.qevValue').value);
                        var arrayDataNonCache = [];
                        if ($.isArray(convertedValue)) {
                            convertedValue = $.grep(convertedValue, function (item) {
                                return item != ""
                            });
                            _.each(convertedValue, function (item) {
                                arrayDataNonCache.push({id: item, text: item});
                            });
                        }
                        for (var i = 0; i < arrayDataNonCache.length; i++) {

                            var option = new Option(arrayDataNonCache[i].text, arrayDataNonCache[i].id, true, true);
                            $selectedField.append(option).trigger('change');
                        }

                        // // manually trigger the `select2:select` event
                        $selectedField.trigger({
                            type: 'select2:select',
                            params: {
                                data: arrayDataNonCache
                            }
                        });

                    } else {
                        console.log('Error: should have returned one of three value types!');
                    }

                    $selectedField.attr('name', 'qev[' + cnt + '].value');
                }
                if ($(this).hasClass('customSQLValueContainer')) {
                    $(getKeyFromExpression(it)).attr('name', 'qev[' + cnt + '].key');
                    $(getSQLValueFromExpression(it)).attr('name', 'qev[' + cnt + '].value');
                    $(getSQLKeyFromExpression(it)).text(it.querySelector('input.qevKey').value);
                    $(getSQLValueFromExpression(it)).val(it.querySelector('input.qevValue').value);
                }
            });
            modalOpener();
            count0++;
        });
    });

    $.each($('.templateQuery-div'), function () {
        var templateQueryNamePrefix = getTQNum(this);
        var $selectedField;
        var count0 = 0;
        $.each($(this).find('.queryExpressionValues .queryBlankContainer'), function () {
            var it = this;
            setFieldSelect($(getFieldFromExpression(it)), it.querySelector('input.qevReportField').value, null, count0, function (cnt, result1) {
                if ($(it).hasClass('toAddContainerQEV')) {
                    $(it).removeAttr('id');

                    $(getFieldFromExpression(it)).attr('disabled', 'disabled');
                    $(getOperatorFromExpression(it)).val(it.querySelector('input.qevOperator').value).trigger('change');
                    $(getOperatorFromExpression(it)).attr('disabled', 'disabled');
                    $(getValueDateFromExpression(it)).datepicker({
                        allowPastDates: true
                    });

                    $(getFieldFromExpression(it)).attr('name', templateQueryNamePrefix + '.qev[' + cnt + '].field');
                    $(getOperatorFromExpression(it)).attr('name', templateQueryNamePrefix + '.qev[' + cnt + '].operator');
                    $(getKeyFromExpression(it)).attr('name', templateQueryNamePrefix + '.qev[' + cnt + '].key');
                    var showEvent = false, showPencil = false;
                    if (result1.dictionary == "EVENT" && result1.dataType !== "java.util.Date") {
                        showEvent = true;
                    }
                    if (result1.dataType !== "java.util.Date") {
                        showPencil = true;
                    }
                    $(getFieldFromExpression(it)).attr('data-validatable', result1.validatable);
                    showHidePencilEventIcon(it, showPencil, showEvent);
                    if ($(getValueSelectFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueSelectFromExpression(it));
                        $selectedField.val(splitStringListIntoArray(it.querySelector('input.qevValue').value)).trigger('change');
                    } else if ($(getValueSelectAutoFromExpression(it)).is(":visible")) {
                        var that = it;
                        $selectedField = $(getValueSelectAutoFromExpression(it));
                        const selectFieldId = $selectedField.attr('id')
                        $selectedField.removeAttr('id').select2({
                            minimumInputLength: 2,
                            multiple: true,
                            ajax: {
                                quietMillis: 300,
                                dataType: "json",
                                url: selectAutoUrl,
                                data: function (params) {
                                    var field = $(getFieldFromExpression(that)).val();
                                    return {
                                        field: field,
                                        term: params.term,
                                        max: params.page || 10,
                                        lang: userLocale
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
                        }).attr('id', selectFieldId);
                        var value = splitStringIntoArray(it.querySelector('input.qevValue').value);
                        var arrayData = [];
                        if ($.isArray(value)) {
                            value = $.grep(value, function (item) {
                                return item != ""
                            });
                            _.each(value, function (item) {
                                arrayData.push({id: item, text: item});
                            });
                        }
                        for (var i = 0; i < arrayData.length; i++) {

                            var option = new Option(arrayData[i].text, arrayData[i].id, true, true);
                            $(getValueSelectAutoFromExpression(this)).append(option).trigger('change');
                        }

                        // // manually trigger the `select2:select` event
                        $(getValueSelectAutoFromExpression(it)).trigger({
                            type: 'select2:select',
                            params: {
                                data: arrayData
                            }
                        });
                    } else if ($(getValueSelectNonCacheFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueSelectNonCacheFromExpression(it));
                        const selectFieldId = $selectedField.attr('id')
                        $selectedField.removeAttr('id').select2({
                            minimumInputLength: 0,
                            separator: ";",
                            multiple: true,
                            ajax: {
                                quietMillis: 300,
                                dataType: "json",
                                url: selectNonCacheUrl,
                                data: function (params) {
                                    var field = $(getFieldFromExpression(it)).val();
                                    return {
                                        field: field,
                                        term: params.term,
                                        max: params.page || 10,
                                        lang: userLocale
                                    };
                                },
                                processResults: function (data, params) {
                                    params.page = params.page || 10;
                                    return {
                                        results: data.list,
                                        pagination: {
                                            more: (params.page * 10) < data.totalCount
                                        }
                                    };
                                }
                            }
                        }).attr('id', selectFieldId);
                        var convertedValue = splitStringListIntoArray(it.querySelector('input.qevValue').value);
                        var arrayDataNonCache = [];
                        if ($.isArray(convertedValue)) {
                            convertedValue = $.grep(convertedValue, function (item) {
                                return item != ""
                            });
                            _.each(convertedValue, function (item) {
                                arrayDataNonCache.push({id: item, text: item});
                            });
                        }
                        for (var i = 0; i < arrayDataNonCache.length; i++) {

                            var option = new Option(arrayDataNonCache[i].text, arrayDataNonCache[i].id, true, true);
                            $selectedField.append(option).trigger('change');
                        }

                        // // manually trigger the `select2:select` event
                        $selectedField.trigger({
                            type: 'select2:select',
                            params: {
                                data: arrayDataNonCache
                            }
                        });
                    } else if ($(getValueTextFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueTextFromExpression(it));
                        $selectedField.val(it.querySelector('input.qevValue').value);
                    } else if ($(getValueDateInputFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueDateInputFromExpression(it));
                        $selectedField.val(it.querySelector('input.qevValue').value);
                    } else {
                        console.log('Error: should have returned one of three value types!');
                    }

                    $selectedField.attr('name', templateQueryNamePrefix + '.qev[' + cnt + '].value');
                }
                // For custom SQL blanks
                if ($(this).hasClass('customSQLValueContainer')) {
                    $(getKeyFromExpression(this)).attr('name', templateQueryNamePrefix + '.qev[' + cnt + '].key');
                    $(getSQLValueFromExpression(this)).attr('name', templateQueryNamePrefix + '.qev[' + cnt + '].value');
                    $(getSQLKeyFromExpression(this)).text(this.querySelector('input.qevKey').value);
                    $(getSQLValueFromExpression(this)).val(this.querySelector('input.qevValue').value);
                }
            });
            count0++;
        });


        var count1 = 0;
        $.each(this.querySelector('div.queryExpressionValues').getElementsByClassName('customSQLValueContainer'), function () {
            $(getKeyFromExpression(this)).attr('name', templateQueryNamePrefix + '.qev[' + count1 + '].key');
            $(getSQLValueFromExpression(this)).attr('name', templateQueryNamePrefix + '.qev[' + count1 + '].value');
            $(getSQLKeyFromExpression(this)).text(this.querySelector('input.qevKey').value);
            $(getSQLValueFromExpression(this)).val(this.querySelector('input.qevValue').value);
            if (($(getFieldFromExpression(it)).val() == "masterCaseNum" || $(getFieldFromExpression(it)).val() == "masterCaseNumJ") && IS_EMPTY_OPERATORS.indexOf($(getOperatorFromExpression(it)).val()) == -1) {
                $(it).find(".copy-paste-pencil").show();
            } else {
                $(it).find(".copy-paste-pencil").hide();
            }
            count1++;
        });
        var count2 = 0;
        $.each(this.querySelector('div.templateSQLValues').getElementsByClassName('customSQLValueContainer'), function () {
            $(getKeyFromExpression(this)).attr('name', templateQueryNamePrefix + '.tv[' + count2 + '].key');
            $(getSQLValueFromExpression(this)).attr('name', templateQueryNamePrefix + '.tv[' + count2 + '].value');
            $(getSQLKeyFromExpression(this)).text(this.querySelector('input.qevKey').value);
            $(getSQLValueFromExpression(this)).val(this.querySelector('input.qevValue').value);
            if (($(getFieldFromExpression(it)).val() == "masterCaseNum" || $(getFieldFromExpression(it)).val() == "masterCaseNumJ") && IS_EMPTY_OPERATORS.indexOf($(getOperatorFromExpression(it)).val()) == -1) {
                $(it).find(".copy-paste-pencil").show();
            } else {
                $(it).find(".copy-paste-pencil").hide();
            }
            count2++;
        });
    });


    $.each($('.alertQueryWrapper1'), function () {
        var templateQueryNamePrefix = "";
        var $selectedField;
        var count0 = 0;
        $.each($(this).find('.queryExpressionValues1 .queryBlankContainer'), function () {
            var it = this;
            setFieldSelect($(getFieldFromExpression(it)), it.querySelector('input.fevReportField').value, null, count0, function (cnt, result1) {
                if ($(it).hasClass('toAddContainerQEV')) {
                    $(getFieldFromExpression(it)).attr('disabled', 'disabled');
                    $(getOperatorFromExpression(it)).val(it.querySelector('input.fevOperator').value).trigger('change');
                    $(getOperatorFromExpression(it)).attr('disabled', 'disabled');
                    $(getValueDateFromExpression(it)).datepicker({
                        allowPastDates: true
                    });
                    $(getFieldFromExpression(it)).attr('name', 'fev[' + cnt + '].field');
                    $(getOperatorFromExpression(it)).attr('name', 'fev[' + cnt + '].operator');
                    $(getKeyFromExpression(it)).attr('name', 'fev[' + cnt + '].key');
                    $(getValueSelectFromExpression(it)).attr('name', 'fev[' + cnt + '].value');
                    var showEvent = false, showPencil = false;
                    if (result1.dictionary == "EVENT" && result1.dataType !== "java.util.Date") {
                        showEvent = true;
                    }
                    if (result1.dataType !== "java.util.Date") {
                        showPencil = true;
                    }
                    $(getFieldFromExpression(it)).attr('data-validatable', result1.validatable);
                    showHidePencilEventIcon(it, showPencil, showEvent);
                    if ($(getValueSelectFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueSelectFromExpression(it));
                        $selectedField.val(splitStringListIntoArray(it.querySelector('input.fevValue').value)).trigger('change');
                    } else if ($(getValueSelectAutoFromExpression(it)).is(":visible")) {
                        var that = this;
                        $selectedField = $(getValueSelectAutoFromExpression(it));
                        const selectFieldId = $selectedField.attr('id')
                        $selectedField.removeAttr('id').select2({
                            minimumInputLength: 2,
                            multiple: true,
                            ajax: {
                                quietMillis: 300,
                                dataType: "json",
                                url: selectAutoUrl,
                                data: function (params) {
                                    var field = $(getFieldFromExpression(it)).val();
                                    return {
                                        field: field,
                                        term: params.term,
                                        max: params.page || 10,
                                        lang: userLocale
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
                        }).attr('id', selectFieldId);
                        var value = splitStringListIntoArray(it.querySelector('input.fevValue').value);
                        var arrayData = [];
                        if ($.isArray(value)) {
                            value = $.grep(value, function (item) {
                                return item != ""
                            });
                            _.each(value, function (item) {
                                arrayData.push({id: item, text: item});
                            });
                        }
                        for (var i = 0; i < arrayData.length; i++) {

                            var option = new Option(arrayData[i].text, arrayData[i].id, true, true);
                            $(getValueSelectAutoFromExpression(it)).append(option).trigger('change');
                        }

                        // // manually trigger the `select2:select` event
                        $(getValueSelectAutoFromExpression(it)).trigger({
                            type: 'select2:select',
                            params: {
                                data: arrayData
                            }
                        });
                    } else if ($(getValueTextFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueTextFromExpression(it));
                        $selectedField.val(it.querySelector('input.fevValue').value);
                    } else if ($(getValueDateInputFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueDateInputFromExpression(it));
                        $selectedField.val(it.querySelector('input.fevValue').value);
                    } else if ($(getValueSelectNonCacheFromExpression(it)).is(":visible")) {
                        $selectedField = $(getValueSelectNonCacheFromExpression(it));
                        const selectFieldId = $selectedField.attr('id')
                        $selectedField.removeAttr('id').select2({
                            minimumInputLength: 0,
                            separator: ";",
                            multiple: true,
                            ajax: {
                                quietMillis: 300,
                                dataType: "json",
                                url: selectNonCacheUrl,
                                data: function (params) {
                                    var field = $(getFieldFromExpression(it)).val();
                                    return {
                                        field: field,
                                        term: params.term,
                                        max: params.page || 10,
                                        lang: userLocale
                                    };
                                },
                                processResults: function (data, params) {
                                    params.page = params.page || 10;
                                    return {
                                        results: data.list,
                                        pagination: {
                                            more: (params.page * 10) < data.totalCount
                                        }
                                    };
                                }
                            }
                        }).attr('id', selectFieldId);
                        var convertedValue = splitStringListIntoArray(it.querySelector('input.fevValue').value);
                        var arrayDataNonCache = [];
                        if ($.isArray(convertedValue)) {
                            convertedValue = $.grep(convertedValue, function (item) {
                                return item != ""
                            });
                            _.each(convertedValue, function (item) {
                                arrayDataNonCache.push({id: item, text: item});
                            });
                        }
                        for (var i = 0; i < arrayDataNonCache.length; i++) {

                            var option = new Option(arrayDataNonCache[i].text, arrayDataNonCache[i].id, true, true);
                            $selectedField.append(option).trigger('change');
                        }

                        // // manually trigger the `select2:select` event
                        $selectedField.trigger({
                            type: 'select2:select',
                            params: {
                                data: arrayDataNonCache
                            }
                        });

                    } else {
                        console.log('Error: should have returned one of three value types!');
                    }

                    if(typeof $selectedField !=="undefined")
                    {
                        $selectedField.attr('name', 'fev[' + cnt + '].value');
                    }
                }
                if ($(this).hasClass('customSQLValueContainer')) {
                    $(getKeyFromExpression(it)).attr('name', 'fev[' + cnt + '].key');
                    $(getSQLValueFromExpression(it)).attr('name', 'fev[' + cnt + '].value');
                    $(getSQLKeyFromExpression(it)).text(it.querySelector('input.fevKey').value);
                    $(getSQLValueFromExpression(it)).val(it.querySelector('input.fevValue').value);
                }
            });
            $(".queryExpressionValues").find(".select2-selection__choice").each(function () {
                if ($(this).attr("title") == "") {
                    $(this).remove();
                }
            })
            $(".queryExpressionValues1").find(".select2-selection__choice").each(function () {
                if ($(this).attr("title") == "") {
                    $(this).remove();
                }
            })
            $(".queryExpressionValues1").find(".select2-selection__choice").attr("title","").remove();
            $(".queryExpressionValues").find(".select2-selection__choice").attr("title","").remove();
            count0++;
        });
    });

    $.each(document.getElementsByClassName('loading'), function () {
        $(this).hide();
    });
    $.each(document.getElementsByClassName('doneLoading'), function () {
        $(this).show();
    });
    AJAXCount = -1;

}

$(document).on('change', '.selectTemplate', function () {
    var templateContainer = getTemplateContainer(this);
    var templateValues = getTemplateValues(templateContainer);
    $(templateValues).empty();
    getCustomSQLValuesForTemplateAJAX($(this).val(), templateValues);
});

function selectTemplateOnChange(templateDropdownWidget, cioms1Id) {
    var templateContainer = getTemplateContainer(templateDropdownWidget);
    var templateValues = getTemplateValues(templateContainer);
    var templateWrapperRow = getTemplateWrapperRow(templateDropdownWidget);

    if ($(templateDropdownWidget).val() && ($(templateDropdownWidget).val() == cioms1Id)) {
        $(templateWrapperRow).find('.ciomsProtectedArea').removeAttr("hidden");
    } else {
        $(templateWrapperRow).find("input[id$=blindProtected]").prop("checked", false);
        $(templateWrapperRow).find("input[id$=privacyProtected]").prop("checked", false);
        $(templateWrapperRow).find('.ciomsProtectedArea').attr("hidden", "hidden");
    }
    $(templateValues).empty();
    getCustomSQLValuesForTemplateAJAX($(templateDropdownWidget).val(), templateValues);
    var queryLevel = $(templateContainer).find("select[id$=queryLevel]");
    var dateRange = $(templateContainer).find("select[id$=dateRangeEnum]");
    if ($(templateDropdownWidget).val() && $(templateDropdownWidget).val() != '') {
        $(templateWrapperRow).find('.templateViewButton').attr('href', templateViewUrl + '/' + $(templateDropdownWidget).val());
        $(templateWrapperRow).find('.templateViewButton').removeClass('hide');
        if ($(templateDropdownWidget).select2('data')[0].name) {
            $(templateWrapperRow).find("input[id$=templateName]").val($(templateDropdownWidget).select2('data')[0].name);
        }
        if (queryLevel.val().length == 0) {
            queryLevel.val('CASE').trigger('change');
        }
        if (dateRange.val().length == 0) {
            var globalDateRangeEnum = $('#alertDateRangeInformation\\.dateRangeEnum');
            if (globalDateRangeEnum.val() != 'CUMULATIVE') {
                dateRange.val('PR_DATE_RANGE').trigger('change');
            } else {
                dateRange.val('CUMULATIVE').trigger('change');
            }
        }

    } else {
        $(templateWrapperRow).find('.templateViewButton').addClass('hide');
        queryLevel.val('').trigger('change');
        dateRange.val('').trigger('change');

    }
}


// Use select2 change event for IE11 in templateQueries.js
function selectQueryOnChange(type,selectContainer) {
    var queryContainer = getQueryWrapperRow(selectContainer);
    var expressionValues = getExpressionValues(queryContainer);
    var queryWrapperRow = getQueryWrapperRow(selectContainer);
    $(expressionValues).empty();
    if (getAJAXCount() == -1) {
        if ($(selectContainer).val() != '') {
            $(queryWrapperRow).find('.queryViewButton').attr('href', queryViewUrl + '/' + $(selectContainer).val());
            $(queryWrapperRow).find('.queryViewButton').removeClass('hide');
            if ($(selectContainer).select2('data')[0].name) {
                $(queryWrapperRow).find("input[id$=queryName]").val($(selectContainer).select2('data')[0].name)
            }
        } else {
            $(queryWrapperRow).find('.queryViewButton').addClass('hide');
        }
        getBlankValuesForQueryAJAX(type,$(selectContainer).val(), expressionValues, (getTQNum(expressionValues) + "."));
        getCustomSQLValuesForQueryAJAX(type,$(selectContainer).val(), expressionValues, (getTQNum(expressionValues) + "."));
        getBlankValuesForQuerySetAJAX(type,$(selectContainer).val(), expressionValues, (getTQNum(expressionValues) + "."));
    } else {
        $(selectContainer).val('');
    }
}

function getBlankValuesForQuerySetAJAX(type, queryId, queryContainer, namePrefix) {
    $.ajax({
        url: blankValuesForQuerySetUrl + "?queryId=" + queryId,
        dataType: 'json',
    })
        .done(function (result) {
            if (result.length > 0) {
                var count = 0;
                var validQueries = [];
                var validForegroundQueries = [];
                $.each(result, function (index, query) {
                    // Add each query name
                    var queryName = query[0].queryName;
                    var container = createSingleQueryContainer(queryContainer, queryName);

                    if (type == "qev") {
                        validQueries.push(query[0].queryId);
                    }
                    if (type == "fev") {
                        validForegroundQueries.push(query[0].queryId);
                    }
                    if (query[0].type == "QUERY_BUILDER") {
                        count = appendNewQEVContainers(type, query, container, namePrefix, count);
                    } else if (query[0].type == "CUSTOM_SQL") {
                        count = appendNewCSQLContainers(type, query, container, type, namePrefix, count);
                    }
                });
                if (type == "qev") {
                    setValidQueries(queryId, queryContainer);
                }
                if (type == "fev") {
                    setForegroundValidQueries(queryId, queryContainer);
                }

                $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
            }
        })
        .fail(function (err) {
            console.log('Error retrieving set parameters' + JSON.stringify(err));
        });
}

function setValidQueries(queryIds, queryContainer) {
    $(queryContainer.parentElement).find('.validQueries').val(queryIds);
}

function setForegroundValidQueries(queryIds, queryContainer) {
    if(queryContainer!=null)
    {
        $(queryContainer.parentElement).find('.foregroundValidQueries').val(queryIds);
    }
}

function createSingleQueryContainer(wholeContainer, queryName) { // for query set
    var singleContainer = document.createElement('div');
    $(singleContainer).append('<div>' + queryName + ':</div>');
    wholeContainer.appendChild(singleContainer);
    return singleContainer;
}

$(document).on('change', '.expressionField', function () {
    var container = this.parentElement.parentElement;
    var expression = container.parentElement;

    var field = $(this)[0].value;
    updateAJAXFields(container);
    updateAJAXValues(container);
    $(getValueSelectNonCacheFromExpression(container)).val(null).trigger("change");
    //Clear select values
    $(getValueTextFromExpression(container)).val('');
    //Remove error outline if we have a value
    if (field != '') {
        $(this.parentElement).removeClass('has-error');
    }
});

$(document).on('change', '.expressionOp', function () {
    var container = this.parentElement.parentElement;
    var expression = container.parentElement;
    updateAJAXOperators(container);

    $(getValueSelectNonCacheFromExpression(container)).val(null).trigger("change");
    //Clear select values
    $(getValueTextFromExpression(container)).val('');
});

$(document).on('focusout', '.expressionValueDateInput', function (evt) {
    $(this).val(newSetDefaultDisplayDateFormat2($(this).val()));
    if ($(this).val() == 'Invalid date') {
        $(this).val('')
    }
})

$(document).on('change', '.expressionValueSelect', function () {
    $(this).parent().removeClass('has-error');
});

$(document).on('change', '.expressionValueText', function () {
    $(this).parent().removeClass('has-error');
});

$(document).on('change', '.expressionValueDate', function () {
    $(this).parent().removeClass('has-error');
});


function showHideValue(selectedType, div) {
    switch (selectedType) {
        case EDITOR_TYPE_DATE:
            //Show datepicker
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).show();
            break;
        case EDITOR_TYPE_SELECT:
            //Show select
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).show();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONECACHE_SELECT:
            // Show autocomplete
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueSelectNonCacheFromExpression(div).parentElement).show();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_NONE:
            //Hide All
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        case EDITOR_TYPE_AUTOCOMPLETE:
            // Show autocomplete
            $(getValueTextFromExpression(div).parentElement).hide();
            $(getValueSelectFromExpression(div).parentElement).hide();
            $(getValueSelectAutoFromExpression(div).parentElement).show();
            $(getValueSelectNonCacheFromExpression(div).parentElement).hide();
            $(getValueDateFromExpression(div).parentElement).hide();
            break;
        default:
            //Show text field
            $(getValueTextFromExpression(div)?.parentElement)?.show();
            $(getValueSelectFromExpression(div)?.parentElement)?.hide();
            $(getValueSelectAutoFromExpression(div)?.parentElement)?.hide();
            $(getValueSelectNonCacheFromExpression(div)?.parentElement)?.hide();
            $(getValueDateFromExpression(div)?.parentElement)?.hide();
            break;
    }
}

function getBlankValuesForQueryAJAX(type, queryId, queryContainer, namePrefix) {
    $.ajax({
        type: "GET",
        url: blankValuesForQueryUrl + "?queryId=" + queryId,
        dataType: 'json',
    })
        .done(function (result) {
            if (result.length > 0) {
                appendNewQEVContainers(type, result, queryContainer, namePrefix, 0);
            }

            if (type == "qev") {
                setValidQueries(queryId, queryContainer);
            }
            if (type == "fev") {
                setForegroundValidQueries(queryId, queryContainer);
            }

            $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
        })
        .fail(function () {
            console.log('Error retrieving parameters');
        });
}

function getCustomSQLValuesForTemplateAJAX(templateId, templateContainer) {
    $.ajax({
        type: "GET",
        url: customSQLValuesForTemplateUrl + "?templateId=" + templateId,
        dataType: 'json',
    })
        .done(function (result) {
            if (result.length > 0) {
                appendNewCSQLContainers(type, result, templateContainer, 'tv', namePrefix, 0);
            }
        })
        .fail(function () {
            console.log('Error retrieving custom SQL parameters');
        });
}

function getCustomSQLValuesForQueryAJAX(type, queryId, queryContainer, namePrefix) {
    $.ajax({
        type: "GET",
        url: customSQLValuesForQueryUrl + "?queryId=" + queryId,
        dataType: 'json',
    })
        .done(function (result) {
            if (result.length > 0) {
                appendNewCSQLContainers(type, result, queryContainer, type, namePrefix, 0);
            }
            if (type == "qev") {
                setValidQueries(queryId, queryContainer);
            }
            if (type == "fev" && queryContainer != null && queryContainer !== "null") {
                setForegroundValidQueries(queryId, queryContainer);
            }
            $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
        })
        .fail(function () {
            console.log('Error retrieving custom SQL parameters');
        });
}

function getFieldIsValidatableFromExpression(container) {
    return $(getFieldFromExpression(container)).find('option:selected').attr('data-validatable') == 'true';
}

function showHidePencilEventIcon(toAdd, showPencil, showEvent) {
    $(toAdd).find(".copy-paste-pencil").hide();
    $(toAdd).find(".searchEventDmv1").hide();

    if (JSON.parse(showPencil)) {
        $(toAdd).find(".copy-paste-pencil").show();
    } else {
        $(toAdd).find(".copy-paste-pencil").hide();
    }

    if (JSON.parse(showEvent)) {
        $(toAdd).find(".searchEventDmv1").show();
    } else {
        $(toAdd).find(".searchEventDmv1").hide();
    }
}

function appendNewQEVContainers(type, result, queryContainer, namePrefix, count) {
    _.each(result, function (it) {
        var toAdd = cloneAddContainer();
        if (queryContainer != null) {
            queryContainer.appendChild(toAdd);
        }

        if (type == "fev") {
            toAdd['fevId'] = it.id;
        } else {
            toAdd['qevId'] = it.id;
        }
        toAdd['key'] = it.key;
        $(getKeyFromExpression(toAdd)).attr('name', namePrefix + type + '[' + count + '].key');
        $(getKeyFromExpression(toAdd)).val(it.key);
        $(getFieldFromExpression(toAdd)).select2();
        $(getFieldFromExpression(toAdd)).attr('disabled', 'disabled');
        setFieldSelect($(getFieldFromExpression(toAdd)), it.field, queryContainer, count, function (cnt, result1) {
            var showEvent = false, showPencil = false;
            if (result1.dictionary == "EVENT" && result1.dataType !== "java.util.Date") {
                showEvent = true;
            }
            if (result1.dataType !== "java.util.Date") {
                showPencil = true;
            }
            $(getFieldFromExpression(toAdd)).attr('data-validatable', result1.validatable);
            showHidePencilEventIcon(toAdd, showPencil, showEvent);
            $(getOperatorFromExpression(toAdd)).select2();
            $(getOperatorFromExpression(toAdd)).val(it.operator).trigger('change');
            $(getOperatorFromExpression(toAdd)).attr('disabled', 'disabled');
            const selectField = $(getValueSelectFromExpression(toAdd))
            const selectFieldId = selectField.attr('id')
            selectField.removeAttr('id').select2().attr('id', selectFieldId);
            var $autocompleteSelect = $(getValueSelectAutoFromExpression(toAdd));
            const autocompleteSelectId = $autocompleteSelect.attr('id');
            $autocompleteSelect.removeAttr('id').select2({
                minimumInputLength: 3,
                multiple: true,
                ajax: {
                    quietMillis: 300,
                    dataType: "json",
                    url: selectAutoUrl,
                    data: function (params) {
                        var field = $(getFieldFromExpression(toAdd)).val();
                        return {
                            field: field,
                            term: params.term,
                            max: params.page || 10,
                            lang: userLocale
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
            }).attr('id', autocompleteSelectId);

            var $nonCacheSelect = $(getValueSelectNonCacheFromExpression(toAdd));
            const nonCacheSelectId = $nonCacheSelect.attr('id')
            $nonCacheSelect.removeAttr('id').select2({
                multiple: true,
                allowClear: false,
                ajax: {
                    quietMillis: 300,
                    dataType: "json",
                    url: selectNonCacheUrl,
                    data: function (params) {
                        var field = $(getFieldFromExpression(toAdd)).val();
                        return {
                            field: field,
                            term: params.term,
                            max:  10,
                            page: params.page || 1,
                            lang: userLocale
                        };
                    },
                    processResults: function (data, params) {
                        params.page = params.page || 1;
                        return {
                            results: data.list,
                            pagination: {
                                more: (params.page * 10) < data.totalCount
                            }
                        };
                    }
                }
            }).attr('id', nonCacheSelectId);

            $(getValueDateFromExpression(toAdd)).datepicker({
                allowPastDates: true
            });

            $(getFieldFromExpression(toAdd)).attr('name', namePrefix + type + '[' + cnt + '].field');
            $(getOperatorFromExpression(toAdd)).attr('name', namePrefix + type + '[' + cnt + '].operator');


            if ($(getValueSelectFromExpression(toAdd)).is(":visible")) {
                $(getValueSelectFromExpression(toAdd)).attr('name', namePrefix + type + '[' + cnt + '].value');
            } else if ($(getValueSelectAutoFromExpression(toAdd)).is(":visible")) {
                $(getValueSelectAutoFromExpression(toAdd)).attr('name', namePrefix + type + '[' + cnt + '].value');
            } else if ($(getValueSelectNonCacheFromExpression(toAdd)).is(":visible")) {
                $nonCacheSelect.attr('name', namePrefix + type + '[' + cnt + '].value');
                $(getValueTextFromExpression(toAdd)).attr('name', namePrefix + type + '[' + cnt + '].copyPasteValue');
            } else if ($(getValueTextFromExpression(toAdd)).is(":visible")) {
                $(getValueTextFromExpression(toAdd)).attr('name', namePrefix + type + '[' + cnt + '].value');
            } else if ($(getValueDateInputFromExpression(toAdd)).is(":visible")) {
                $(getValueDateInputFromExpression(toAdd)).attr('name', namePrefix + type + '[' + cnt + '].value');
            } else {
                console.log('Naming Error: should have returned one of three value types!');
            }
            modalOpener();
        });
        count++;
    });
    return count;
}

function appendNewCSQLContainers(type, result, valuesContainer, type, namePrefix, count) {
    // var count = 0;
    _.each(result, function (it) {
        var toAdd = cloneCustomSQLValueContainer();
        valuesContainer.appendChild(toAdd);
        toAdd['qevId'] = it.id;
        toAdd['key'] = it.key;
        $(getKeyFromExpression(toAdd)).attr('name', namePrefix + '.' + type + '[' + count + '].key').val(it.key);
        $(getSQLValueFromExpression(toAdd)).attr('name', namePrefix + '.' + type + '[' + count + '].value').text(it.key);
        count++;
    });
}

function getTQNum(queryContainer) {
    var $current = $(queryContainer).closest('.templateQuery-div');
    return $current.attr('id');
}

function cloneAddContainer() {
    var baseContainer = $('.toAddContainerQEV').last()[0];
    //$('#selectField').select2("destroy");
    //$('#selectOperator').select2("destroy");
    //$('#selectSelect').select2("destroy");
    var containerToAdd = baseContainer.cloneNode(true);
    containerToAdd.removeAttribute('id');
    return containerToAdd;
}

function cloneCustomSQLValueContainer() {
    var baseContainer = document.getElementById('customSQLValueContainer');
    var containerToAdd = baseContainer.cloneNode(true);
    containerToAdd.removeAttribute('id');
    return containerToAdd;
}

function getQueryWrapperRow(element) {
    return $(element).closest('.queryWrapperRow');
}

function getTemplateContainer(element) {
    return $(element).closest('.templateContainer');
}

function getTemplateValues(element) {
    return element[0].getElementsByClassName('templateSQLValues').item(0);
}

function getTemplateWrapperRow(element) {
    return $(element).closest('.templateWrapperRow');
}

function getExpressionValues(element) {
    return element[0].getElementsByClassName('queryExpressionValues').item(0);
}

function getExpressionValues1(element) {
    return element[0].getElementsByClassName('queryExpressionValues1').item(0);
}

function getFieldFromExpression(container) {
    return container.querySelector('select.expressionField');
}

function getOperatorFromExpression(container) {
    return container.querySelector('select.expressionOp');
}

function getValueTextFromExpression(container) {
    return container.getElementsByClassName('expressionValueText')[0];
}

function getValueSelectFromExpression(container) {
    var list = container.getElementsByClassName("expressionValueSelect");
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

function getValueSelectNonCacheFromExpression(container) {
    var list = $(container).find(".expressionValueSelectNonCache");
    if (list.length > 1) {
        return list[1];
    }
    return list[0];
}


function getValueDateFromExpression(container) {
    return container.getElementsByClassName('expressionValueDate')[0];
}

function getValueDateInputFromExpression(container) {
    return container.getElementsByClassName('expressionValueDateInput')[0];
}

function getKeyFromExpression(container) {
    return container.querySelector('input.qevKey');
}

function getValueMultiselectInputFromExpression(container) {
    return container.querySelector('input.qevValue');
}

function getSQLKeyFromExpression(container) {
    return container.querySelector('p.inputSQLKey');
}

function getSQLValueFromExpression(container) {
    return container.querySelector('input.inputSQLValue');
}

function getAJAXCount() {
    return AJAXCount;
}

//Multiselect Select2
//Returns a string if there is one value, or an array of strings if there are multiple values delimited by semicolons (';')
function splitStringListIntoArray(oldValue) {
    if (oldValue) {
        return oldValue.split(";");
    }
}

function splitStringIntoArray(oldValue) {
    if (oldValue) {
        return oldValue.split(",");
    }
}

function convertArrayToMultiselectString(value) {
    var result = value;
    if ($.isArray(value)) {
        var concatValues = '';
        for (var i = 0; i < value.length; i++) {
            if (value != '') {
                if (i != 0) {
                    //Multiselect Select2
                    concatValues += ';' + value[i];
                } else {
                    concatValues += value[i];
                }
            }
        }
        result = concatValues;
    }
    return result;
}

function setMultiselectValues() {
    var $valueSelect;
    var $valueSelectAuto;
    var $valueMultiselect;
    var $valueSelectNonCache;

    $.each($('.templateQuery-div, .alertQueryWrapper, .alertQueryWrapper1'), function () {
        $.each($(this).find('.toAddContainerQEV'), function () {
            var selectedFieldType = getFieldType($(getFieldFromExpression(this)).find(":selected"));
            $valueSelect = $(getValueSelectFromExpression(this));
            $valueSelectAuto = $(getValueSelectAutoFromExpression(this));
            $valueSelectNonCache = $(getValueSelectNonCacheFromExpression(this));

            switch (selectedFieldType) {
                case RF_TYPE_AUTOCOMPLETE: // Autocomplete uses same operators as string
                    $valueMultiselect = $(getValueMultiselectInputFromExpression(this));
                    $valueMultiselect.attr('name', $valueSelectAuto.attr('name'));
                    $valueSelectAuto.removeAttr('name');
                    $valueMultiselect.val(convertArrayToMultiselectString($valueSelectAuto.select2('val')));
                    break;
                case RF_TYPE_NOCACHE_DROP_DOWN: // Autocomplete uses same operators as string
                    $valueMultiselect = $(getValueMultiselectInputFromExpression(this));
                    $valueMultiselect.attr('name', $valueSelectNonCache.attr('name'));
                    $valueSelectNonCache.removeAttr('name');
                    $valueMultiselect.val(convertArrayToMultiselectString($valueSelectNonCache.select2('val')));
                    break;
                case RF_TYPE_STRING: // string and text
                    $valueMultiselect = $(getValueMultiselectInputFromExpression(this));
                    $valueMultiselect.attr('name', $valueSelect.attr('name'));
                    $valueSelect.removeAttr('name');
                    $valueMultiselect.val(convertArrayToMultiselectString($valueSelect.val()));
                    break;
                default:
                    break;
            }
        });
    });
}

function setFieldSelect($field, field, queryContainer, count, callback) {
    return $.ajax({
        type: "GET",
        url: reportFieldsForQueryUrl + "?name=" + field,
        dataType: 'json',
    })
        .done(function (result) {
            $field.append($('<option></option>')
                .attr("value", result.name)
                .attr("selected", true)
                .attr("data-dictionary", result.dictionary)
                .attr("data-level", result.level)
                .attr("data-validatable", result.validatable)
                .attr("data-isAutocomplete", result.isAutocomplete)
                .attr("data-isNonCacheSelectable", result.isNonCacheSelectable)
                .attr("data-dataType", result.dataType)
                .attr("data-description", result.description)
                .text(result.displayText));
            $field.trigger("change");
            callback(count, result);
            $(queryContainer).trigger("loadBlankAndCustomSqlFieldsComplete");
        });
}