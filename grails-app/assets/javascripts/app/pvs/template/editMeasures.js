var CUSTOM_PERIOD_COUNT = "CUSTOM_PERIOD_COUNT";
var DATE_FMT_TZ = "YYYY-MM-DDTHH:mm:ssZZ";
var DATE_DISPLAY = "MM/DD/YYYY";

var viewOnly = ($('#editable').val() === 'false');
var measureList = [];
var numColMeas;

var measureIndexList = [];

$(function () {
    var templateType = $('#templateType').val();

    if (templateType == 'DATA_TAB') {
        var templateId = $('#templateId').val();
        var measureSelected;

        // initialize all select2 for measures
        for (var index = 0; index < numColMeas; index++) {
            initMeasureSelect2(index);
        }

        // For edit and view page
        if ($("#JSONMeasures").val()) {
            var measureString = $("#JSONMeasures").val();
            if (measureString) {
                measureList = JSON.parse(measureString);
                // For each measure set
                $.each(measureList, function (colMesaIndex, measures) {
                    var measuresContainer = $(".measuresContainer")[colMesaIndex];
                    var maxIndex = 0;
                    $.each(measures, function(measIndex) {
                        addMeasureDiv(this.name, measuresContainer, measIndex);
                        // initialize select2 and date pickers
                        initDateRangeCountSelect2('colMeas'+colMesaIndex+'-meas'+measIndex);
                        initDatePickers('colMeas'+colMesaIndex+'-meas'+measIndex);

                        maxIndex = measIndex;
                    });
                    measureIndexList.push(maxIndex);
                });
            }
        }

        function getMeasureSequence(measure) {
            var measuresNodeList = document.getElementsByClassName("measuresContainer")[0].getElementsByClassName("measureName");
            var measures = Array.prototype.slice.call(measuresNodeList);
            return measures.indexOf(measure);
        }

        function showMeasureOptions(open) {
            if (open) {
                $(document.getElementsByClassName('columnRenameArea')[0]).hide();
                $(document.getElementsByClassName('measureOptions')[0]).slideDown();
            } else {
                $(document.getElementsByClassName('measureOptions')[0]).hide();
            }
        }

        $(document).on('click', '.removeMeasure', function () {
            var measure = this.parentElement;
            var measureName = measure.getElementsByClassName("measureName")[0];
            if (measureName.classList.contains("columnSelected")) {
                showMeasureOptions(false);
            }
            var containerWidth = $(measure.parentElement).width();
            containerWidth -= $(measure).width();
            $(measure.parentElement).width(containerWidth);
            measure.remove();
        });

        $(document).on('click', '.measureName', function () {
            showMeasureOptions(true);

            $('.measureOptions').hide();
            var container = getMeasureOptionsDiv(this);
            $(container).slideDown();

            $.each(document.getElementsByClassName('columnSelected'), function () {
                this.classList.remove('columnSelected');
            });
            this.classList.add("columnSelected");

            if ($(container).find('select')[0].value === CUSTOM_PERIOD_COUNT) {
                showDatePicker(true, container);
                setDatePicker(container);
            } else {
                showDatePicker(false, container);
            }

            if (viewOnly) {
                $("input").prop("disabled", true);
                $("button").prop("disabled", true);
                $("textarea").prop("disabled", true);
                $("select").prop("disabled", true);
                $.each($(".percentageOption").find(".no-bold"), function () {
                    this.classList.remove("add-cursor");
                });
            }
        });

        $(document).on('click', '.showCustomExpression_measure', function () {
            var container = this.closest('.measureOptions');
            var textArea = $(container).find('.customExpressionArea')[0];
            if (textArea.hasAttribute('hidden')){
                $(textArea).removeAttr('hidden');
            } else {
                $(textArea).attr('hidden', 'hidden');
            }
        });

        $(document).on('click', '.closeMeasureOptions', function () {
            showMeasureOptions(false);
            var measuresContainer = document.getElementsByClassName("measuresContainer")[0];
            measuresContainer.getElementsByClassName('columnSelected')[0].classList.remove('columnSelected');
            measureSelected = null;
        });

        $(document).on('click', 'input[type="submit"]', function () {
            var validIndex = [];
            $.each($('.columnMeasureSet'), function () {
                if ($(this).attr('sequence') != "") {
                    validIndex.push($(this).attr('sequence'));
                }
            });
            $('#validColMeasIndex').val(validIndex);
        });
    }

});

function initMeasureSelect2(index) {
    $("#selectMeasure"+index).select2({
    }).on("change", function (e) {
        if (e.added.text == 'Select Measure') {
            $(this).parent().addClass('has-error');
        } else {
            $(this).parent().removeClass('has-error');
            if (measureIndexList[index] > -1) {
                measureIndexList[index]++;
            } else {
                measureIndexList[index] = 0;
            }
            var measuresContainer = $(this.closest('.columnMeasureSet')).find(".measuresContainer");
            addMeasureDiv(e.added.text, measuresContainer, measureIndexList[index]);

            var optionId = 'colMeas' + index + '-meas' + measureIndexList[index];
            addMeasureOptionsDiv(optionId, e.added);

        }
    }).on("select2-open", function () {
        $('#selectMeasure'+index).val("Select Measure");
    });
}

function addMeasureDiv(measureName, container, measIndex) {
    var measureDiv = createMeasureDiv(measureName, measIndex);

    var containerWidth = $(container).width();
    $(container).append(measureDiv);
    containerWidth += $(measureDiv).width();
    $(container).width(containerWidth);

    return measureDiv;
}

function addMeasureOptionsDiv(optionId, measure) {
    var cloned = $('#colMeas-meas').clone();
    $(cloned).attr('id', optionId);

    $($(cloned).find('.measureType')[0]).attr('id', optionId + '-type')
        .attr('name', optionId + '-type')
        .val(measure.id);

    $($(cloned).find('.inputMeasureName')[0]).attr('id', optionId + '-name')
        .attr('name', optionId + '-name')
        .val(measure.text);

    $($(cloned).find('select')[0]).attr('id', optionId + '-dateRangeCount')
        .attr('name', optionId + '-dateRangeCount');

    // date pickers
    $($(cloned).find('#colMeas-meas-datePickerFrom')[0]).attr('id', optionId + '-datePickerFrom')
        .attr('name', optionId + '-datePickerFrom');
    $($(cloned).find('#colMeas-meas-datePickerTo')[0]).attr('id', optionId + '-datePickerTo')
        .attr('name', optionId + '-datePickerTo');
    $($(cloned).find('#colMeas-meas-customPeriodFrom')[0]).attr('id', optionId + '-customPeriodFrom')
        .attr('name', optionId + '-customPeriodFrom');
    $($(cloned).find('#colMeas-meas-customPeriodTo')[0]).attr('id', optionId + '-customPeriodTo')
        .attr('name', optionId + '-customPeriodTo');

    // radio group
    $($(cloned).find('input[name="colMeas-meas-percentageOption"]')).attr('id', optionId + '-percentageOption')
        .attr('name', optionId + '-percentageOption');

    $($(cloned).find('#colMeas-meas-showTotal')[0]).attr('id', optionId + '-showTotal')
        .attr('name', optionId + '-showTotal');

    $($(cloned).find('textarea[name="colMeas-meas-customExpression"]')[0]).attr('id', optionId + '-customExpression')
        .attr('name', optionId + '-customExpression');

    $('#measureOptionsArea').append(cloned);

    // initialize select2
    initDateRangeCountSelect2(optionId);

    // initialize date pickers
    initDatePickers(optionId);
}

function initDateRangeCountSelect2(optionId) {
    $('#' + optionId +'-dateRangeCount').select2({
    }).on("change", function (e) {
        if (e.added.text == 'Select Count') {
            $(this).addClass('has-error');
        } else {
            $(this).removeClass('has-error');
            var container = this.closest('.measureOptions');
            if (e.val == CUSTOM_PERIOD_COUNT) {
                showDatePicker(true, container);
                setDatePicker(container);
            } else {
                showDatePicker(false, container);
            }
        }
    });
}

function initDatePickers(optionId) {
    var hiddenInputFrom = $('#' + optionId + '-customPeriodFrom');
    var hiddenInputTo = $('#' + optionId + '-customPeriodTo');

    $('#' + optionId + '-datePickerFrom').datepicker({
        allowPastDates: true,
        formatDate: function(date) {
            return moment(date).tz(userTimeZone).format(DATE_DISPLAY);
        }
    }).on('changed.fu.datepicker', function (evt, date) {
        // when input is changed directly
        setCustomPeriod(renderDateWithTimeZone(date), null, hiddenInputFrom);
    }).on('click', function () {
        // when the calendar icon is clicked
        var date = $(this).datepicker('getDate');
        setCustomPeriod(renderDateWithTimeZone(date), null, hiddenInputFrom);
    });

    $('#' + optionId + '-datePickerTo').datepicker({
        allowPastDates: true,
        formatDate: function(date) {
            return moment(date).tz(userTimeZone).format(DATE_DISPLAY);
        }
    }).on('changed.fu.datepicker', function (evt, date) {
        // when input is changed directly
        setCustomPeriod(null, renderDateWithTimeZone(date), hiddenInputTo);
    }).on('click', function () {
        // when the calendar icon is clicked
        var date = $(this).datepicker('getDate');
        setCustomPeriod(null, renderDateWithTimeZone(date), hiddenInputTo);
    });
}

function setCustomPeriod(dateFrom, dateTo, hiddenInput) {
    if (dateFrom) {
        // Set the time to 00:00:00
        var dateFromInfo = dateFrom.split('T')[0];
        var timeZone = dateFrom.split('T')[1].substring(8);
        $(hiddenInput).val(dateFromInfo + 'T00:00:00' + timeZone);
    }
    if (dateTo) {
        // Set the time to 23:59:59
        var dateToInfo = dateTo.split('T')[0];
        var timeZone = dateTo.split('T')[1].substring(8);
        $(hiddenInput).val(dateToInfo + 'T23:59:59' + timeZone);
    }
}

function showDatePicker(isCustomPeriod, container) {
    if (isCustomPeriod) {
        $($(container).find('.customPeriodDatePickers')[0]).show();
    } else {
        $($(container).find('.customPeriodDatePickers')[0]).hide();
    }
}

function renderDateWithTimeZone(date) {
    return moment(date).tz(userTimeZone).format(DATE_FMT_TZ);
}

function setDatePicker(container) {
    var datePickerFrom = $(container).find('.datepicker')[0];
    var datePickerTo = $(container).find('.datepicker')[1];

    var dateFrom = $($(container).find('.customPeriodFrom')[0]).val();
    if (dateFrom) {
        dateFrom = renderDateWithTimeZone(dateFrom);
    } else {
        dateFrom = renderDateWithTimeZone(new Date());
    }

    var dateTo = $($(container).find('.customPeriodTo')[0]).val();
    if (dateTo) {
        dateTo = renderDateWithTimeZone(dateTo);
    } else {
        dateTo = renderDateWithTimeZone(new Date());
    }

    $(datePickerFrom).datepicker('setDate', dateFrom);
    $(datePickerTo).datepicker('setDate', dateTo);
}

function createMeasureDiv(measureName, measIndex) {
    var measureDiv = document.createElement("div");
    measureDiv.style.float = "left";
    measureDiv.classList.add('validMeasure');
    $(measureDiv).attr('sequence', measIndex);

    if (!viewOnly) {
        var closeIcon = document.createElement("i");
        closeIcon.classList.add("fa");
        closeIcon.classList.add("fa-times");
        closeIcon.classList.add("removeMeasure");
        closeIcon.classList.add("add-cursor");
        measureDiv.appendChild(closeIcon);
    }

    var measure = document.createElement("div");
    measure.classList.add("measureName");
    measure.classList.add("add-cursor");
    measure.innerHTML = measureName;
    measureDiv.appendChild(measure);

    return measureDiv;
}

function getMeasureSequence(index, measureDiv) {
    var measuresNodeList = $($(".measuresContainer")[index]).find(".measureName");
    var measures = Array.prototype.slice.call(measuresNodeList);
    return measures.indexOf(measureDiv);
}

function getMeasureOptionsDiv(measureDiv) {
    var colMeasDiv = $(measureDiv).closest('.columnMeasureSet');
    var colMeasIndex = $(colMeasDiv).attr('sequence');

    var measuresNodeList = $(colMeasDiv).find(".measureName");
    var measures = Array.prototype.slice.call(measuresNodeList);
    var measureIndex = measures.indexOf(measureDiv);

    return $('#colMeas'+ colMeasIndex + '-meas' + measureIndex)
}

function setValidMeasureIndexList() {
    $.each($('.columnMeasureSet'), function () {
        var colMeasIndex = $(this).attr('sequence');
        var validIndex = [];
        $.each($(this).find('.validMeasure'), function() {
            validIndex.push($(this).attr('sequence'));
        });
        console.log('valid measure index:', colMeasIndex, validIndex);

        $('#colMeas' + colMeasIndex + '-validMeasureIndex').val(validIndex);
    });
}