DATE_RANGE_ENUM = {
    CUSTOM: 'CUSTOM',
    CUMULATIVE: 'CUMULATIVE',
    RELATIVE: 'RELATIVE'
};
X_OPERATOR_ENUMS = {
    LAST_X_DAYS: 'LAST_X_DAYS',
    LAST_X_WEEKS: 'LAST_X_WEEKS',
    LAST_X_MONTHS: 'LAST_X_MONTHS',
    LAST_X_YEARS: 'LAST_X_YEARS'
};
DATE_RANGE_TYPE = {
    SUBMISSION_DATE: 'submissionDate',
    EVENT_RECEIPT_DATE: 'EVENT_RECEIPT_DATE'
};
DATE_DISPLAY = "MM/DD/YYYY";
DATE_FMT_TZ = "YYYY-MM-DD";

$(function () {
    initializeAsOfDate();

    $("#myStartDate").on('focusout', function(){

        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()))
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
        document.getElementById('myStartDate').value=moment($(this).val(),['DD/MM/YYYY','DD-MMM-YYYY']).utc($(this).val()).format(DEFAULT_DATE_DISPLAY_FORMAT);
    });

    function initializeAsOfDate() {
        var asOf = null;

        if (document.getElementById('asOfVersionDateValue') != null &&
            document.getElementById('asOfVersionDateValue').value != null) {
            asOf = (document.getElementById('asOfVersionDateValue').value);
        }
        $('#asOfVersionDatePicker').datepicker({
            allowPastDates: true,
            date:asOf
        }).on('changed.fu.datepicker', function (evt, date) {
            asOf = date;
            updateInputField();
        }).on('click', function () {
            asOf = $("#asOfVersionDatePicker").datepicker('getDate');
            updateInputField();
        });

        if ((typeof editAlert != "undefined" &&  editAlert === "edit")) {
            $("#asOfVersionDatePicker").datepicker('setDate', renderDateWithTimeZone(asOf));
        }
        var updateInputField = function () {
            if (asOf != 'Invalid Date' && asOf != "") {
                $('input[name="asOfVersionDate"]').val(renderDateWithTimeZone(asOf));
            }
        };
        updateInputField();
        $("#asOfVersionDateId").on('focusout', function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
            document.getElementById('asOfVersionDateValue').value=moment($(this).val(),['DD/MM/YYYY','DD-MMM-YYYY']).utc($(this).val()).format(DEFAULT_DATA_ANALYSIS_DATE_FORMAT)
        });
    }
    if (document.getElementById('dateRangeEnum').value != null &&
        _.contains(X_OPERATOR_ENUMS, document.getElementById('dateRangeEnum').value)) {
        $(document.getElementById('templateQueries[0].dateRangeInformationForTemplateQuery.relativeDateRangeValue')).hide();
    }

    var init = function () {
        $(".showHeaderFooterArea").on('click', showAdvancedOption);
        $(".showAdvancedOption").on('click', showAdvancedOption);
    };

    function renderDateWithTimeZone(date) {
        var parseDate = moment(date).format(DEFAULT_DATE_FORMAT);
        return parseDate;
    }

    $('#onOrAfterDatePicker').datepicker({
        allowPastDates: true,
        date: null,
        formatDate: function (date) {
            return moment(date).tz(userTimeZone).format(DATE_DISPLAY);
        }
    });

    $('#editOnOrAfterDatePicker').datepicker({
        allowPastDates: true,
        formatDate: function (date) {
            return moment(date).tz(userTimeZone).format(DATE_DISPLAY);
        }
    });

    if (document.getElementById("evaluateDateAs") != null && document.getElementById("evaluateDateAs").value === "VERSION_ASOF") {
        $('#asOfVersionDatePicker').show();
    } else {
        $('#asOfVersionDatePicker').hide();
    }

    $("#dateRangeType").select2({
        placeholder: "Select Date Range type"
    });

    $(".evaluateDateAs").select2({
        placeholder: "Select Case Date On"
    });

    $(document).on('change', '#evaluateDateAsNonSubmission', function () {
        checkForDatePickerValue();
    });

    $(document).on('change', '#evaluateDateAsSubmissionDate', function () {
        checkDateRangeType();
    });

    $(document).on('change', '#dateRangeType', function () {
        checkDateRangeType();
    });

    checkDateRangeType();

    $(document).on('change', '.dateRangeEnumClass', function () {
        var elementId = (this.id);
        var index = elementId.replace(/[^0-9\.]+/g, "");
        // dateRangeChangedAction(document, parseInt(index))
    });

    $('.glyphicon-calendar').on('click', function () {

        var component = $(document).find(".fromDateChanged");
        _.each(component, function (dateRangeDiv, index) {

            if (index < component.length - 1) {
                // dateRangeChangedAction(document, parseInt(index))
            }
        })
    });

    function checkForDatePickerValue() {

        var value = $("#evaluateDateAsNonSubmission").select2('val');
        checkDateRangeType();
        if (value) {
            if (value.toLowerCase().indexOf('as') != -1) {
                $('#asOfVersionDatePicker').show();
            } else {
                $('#asOfVersionDatePicker').hide();
            }

        } else {
            $('#asOfVersionDatePicker').hide();
        }
    }

    function showAdvancedOption() {
        var advancedOptionDiv = $('#' + $(this).attr('for'));
        advancedOptionDiv.toggle();

        if (advancedOptionDiv.is(':visible')) {
            $(this).text(LABELS.labelHideAdavncedOptions)
        } else {
            $(this).text(LABELS.labelShowAdavncedOptions)
        }
    }

    function checkDateRangeType() {
        if (document.getElementById('dateRangeType') != null &&
            document.getElementById('dateRangeType').value === DATE_RANGE_TYPE.SUBMISSION_DATE) {
            $("#evaluateDateAs").val($("#evaluateDateAsSubmissionDate").val());
            checkIncludeLockedVersions($("#evaluateDateAs").val());
            $("#evaluateDateAsDiv").hide();
            $("#evaluateDateAsSubmissionDateDiv").show();
            $('#asOfVersionDatePicker').hide();
        } else {
            $("#evaluateDateAs").val($("#evaluateDateAsNonSubmission").val());
            checkIncludeLockedVersions($("#evaluateDateAs").val());
            $("#evaluateDateAsDiv").show();
            $("#evaluateDateAsSubmissionDateDiv").hide();
        }
        if($("#dateRangeType").val() === DATE_RANGE_TYPE.EVENT_RECEIPT_DATE){
            $('#excludeFollowUp').prop("checked", false).prop("disabled", true);
        } else if($('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.VAERS) && !$('#selectedDatasource').val().includes(dataSources.PVA)){
            $('#excludeNonValidCases').prop("checked", false).prop("disabled", true);
            $('#includeLockedVersion').prop("checked", false).prop("disabled", true);
            $('#excludeFollowUp').prop("checked", false).prop("disabled", true);
        }
        else {
            $('#excludeFollowUp').prop("disabled", false);
        }
    }

    function checkIncludeLockedVersions(lockedvalue) {
        if (typeof lockedvalue != "undefined" && lockedvalue != null && ($("input[name=editable]") == undefined || $("input[name=editable]").val() != "true")) {
            if (lockedvalue.toLowerCase().indexOf('latest') != -1 && !$("#limitToCaseSeries").val()) {
                $('#includeLockedVersion').removeAttr("disabled");
            } else {
                $('#includeLockedVersion').prop("checked", true);
                $('#includeLockedVersion').attr("disabled", true);
                $('#lockedVersion').val(true)
            }
        }
    }

    $('#includeLockedVersion').on('change', function () {
        $('#lockedVersion').val(this.checked)
    });
    if($('#selectedDatasource').val() && $('#selectedDatasource').val().includes(dataSources.PVA)){
        $('#includeLockedVersion').prop("checked", $('#includeLockedVersion').prop("defaultChecked"))
    }
    $("#dateRangeEnumProductFreqTrue").hide();
    $("#dateRangeEndProductFreqTrue").hide();
    $("#dateRangeStartProductFreqTrue").hide();
    $("#frequency").hide();

    $("#dateRangeStartProductFreq").on('change', function () {
        $("#dateRangeEndProductFreq").get(0).selectedIndex = $("#dateRangeStartProductFreq :selected").index();
    });
    $("#dateRangeEndProductFreq").on('change', function () {
        $("#dateRangeStartProductFreq").get(0).selectedIndex = $("#dateRangeEndProductFreq :selected").index();
    });

    init();
    hideLockedCheckbox();
    disableLockedCheckbox();
});

function dateRangeChangedAction(currentDocument, index) {
    var datePickerFromDiv = $(currentDocument.getElementById('templateQueries[' + index + '].datePickerFromDiv'));
    var datePickerToDiv = $(currentDocument.getElementById('templateQueries[' + index + '].datePickerToDiv'));
    var relativeDateRangeValue = $(currentDocument.getElementById('templateQueries[' + index + '].dateRangeInformationForTemplateQuery.relativeDateRangeValue'));

    var valueChanged = currentDocument.getElementById('templateQueries[' + index + '].dateRangeInformationForTemplateQuery.dateRangeEnum').value;
    if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
        initializeDatePickersForEdit(currentDocument, index);
        datePickerFromDiv.show();
        datePickerToDiv.show();
        relativeDateRangeValue.hide();
    } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
        datePickerFromDiv.hide();
        datePickerToDiv.hide();
        relativeDateRangeValue.hide();
    } else {
        if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
            relativeDateRangeValue.show();
        } else {
            relativeDateRangeValue.hide();
        }
        datePickerFromDiv.hide();
        datePickerToDiv.hide();
    }
}

function initializeDatePickersForEdit(currentDocument, index) {
    var dateRangeStart = null;
    var dateRangeEnd = null;

    var dateRangeStartAbsolute = currentDocument.getElementById('templateQueries[' + index + '].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute');
    var dateRangeEndAbsolute = currentDocument.getElementById('templateQueries[' + index + '].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute');

    if (dateRangeStartAbsolute.value) {
        dateRangeStart = dateRangeStartAbsolute.value;
    }

    if (dateRangeEndAbsolute.value) {
        dateRangeEnd = dateRangeEndAbsolute.value;
    }

    $(currentDocument.getElementById('templateQueries[' + index + '].datePickerFromDiv')).datepicker({
        allowPastDates: true,
        date: dateRangeStart,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $(currentDocument.getElementById('templateQueries[' + index + '].datePickerToDiv')).datepicker({
        allowPastDates: true,
        date: dateRangeEnd,
        twoDigitYearProtection: true,
        momentConfig: {
            culture: userLocale,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
}

// Use select2 change event for IE11 in templateQueries.js
function dataRangeEnumClassOnChange (dateRangeContainer) {
    var elementId = (dateRangeContainer.id);
    var index = elementId.replace(/[^0-9\.]+/g, "");
    dateRangeChangedAction(document, parseInt(index))
}

function checkNumberFields() {
    var validNumber = true;
    $.each($('.relativeDateRangeValue'), function () {
        if (!Number(this.value)) {
            validNumber = false;
            $(this).parent().addClass('has-error');
            $(this).parent().find('.notValidNumberErrorMessage').show();
            $("#saveRun").prop('disabled', false);
            $("#saveBtn").prop('disabled', false);
        } else {
            $(this).parent().removeClass('has-error');
            $(this).parent().find('.notValidNumberErrorMessage').hide();
        }
    });

    return validNumber;
}
