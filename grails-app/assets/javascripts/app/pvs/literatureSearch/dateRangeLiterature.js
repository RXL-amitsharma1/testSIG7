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

DATE_DISPLAY = "MM/DD/YYYY";
DATE_FMT_TZ = "YYYY-MM-DD";

$(function () {

    if (document.getElementById('dateRangeEnum').value != null &&
        _.contains(X_OPERATOR_ENUMS, document.getElementById('dateRangeEnum').value)) {
        $(document.getElementById('relativeDateRangeValue')).show();
    }

    function renderDateWithTimeZone(date) {
        var parseDate = moment(date).format(DATE_FMT_TZ);
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

    checkDateRangeInEdit(document);

    $(document).on('change', '.dateRangeEnumClass', function () {
        var elementId = (this.id);
        var index = elementId.replace(/[^0-9\.]+/g, "");
        dateRangeChangedAction(document, parseInt(index))
    });

    $('#myDatePicker').datepicker({
        date: $("#myDatePicker").val() ? new Date($("#myDatePicker").val()) : null,
        formatDate: function (date) {
            return setDefaultDisplayDateFormat(date);
        }
    });

    $('#datePickerFromDiv').datepicker({
        allowPastDates: true,
        date: $("#datePickerFromDiv").val() ? new Date($("#datePickerFromDiv").val()) : null,
        formatDate: function (date) {
            return setDefaultDisplayDateFormat(date);
        }
    });

    $('#datePickerToDiv').datepicker({
        allowPastDates: true,
        date: $("#datePickerToDiv").val() ? new Date($("#datePickerToDiv").val()) : null,
        formatDate: function (date) {
            return setDefaultDisplayDateFormat(date);
        }
    });

    $('.glyphicon-calendar').on('click', function () {

        var component = $(document).find(".fromDateChanged");
        _.each(component, function (dateRangeDiv, index) {

            if (index < component.length - 1) {
                dateRangeChangedAction(document, parseInt(index))
            }
        })
    });

    function checkDateRangeInEdit(currentDocument) {

        var component = $(currentDocument).find(".dateRangeEnumClass");
        _.each(component, function (dateRangeDiv, index) {
            if(index == 0){
                dateRangeChangedAction(currentDocument, parseInt(index));
            }
        })
    }

    function initializeDatePickersForEdit(currentDocument, index) {
        var from = null;
        var to = null;
        var dateRangeStartAbsolute = document.getElementById('dateRangeStartAbsolute');
        if ( dateRangeStartAbsolute && dateRangeStartAbsolute.value) {
            from = currentDocument.getElementById('dateRangeStartAbsolute').value;
            if ($('#dateRangeStartProductFreqFalse').is(":visible")) {
                $(currentDocument.getElementById('datePickerFromDiv')).datepicker('setDate', new Date(from.split('-')[0], from.split('-')[1] - 1, from.split('-')[2]));
            }
            to = currentDocument.getElementById('dateRangeEndAbsolute').value;
            if ($('#dateRangeEndProductFreqFalse').is(":visible")) {
                $(currentDocument.getElementById('datePickerToDiv')).datepicker('setDate', new Date(to.split('-')[0], to.split('-')[1] - 1, to.split('-')[2]));
            }
        }

        $(currentDocument.getElementById('datePickerFromDiv')).datepicker({
            allowPastDates: true,
            date: from,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        }).on('changed.fu.datepicker', function (evt, date) {
            from = date;
            // when input is changed directly
            updateFields();
        }).on('click', function (evt) {
            // when the calendar icon is clicked
            from = $(currentDocument.getElementById('datePickerFromDiv')).datepicker('getDate');
            updateFields();
        });

        $("#dateRangeStart").on('focusout', function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()))
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
            from=$(this).val()
            updateFields()
        });

        $(currentDocument.getElementById('datePickerToDiv')).datepicker({
            allowPastDates: true,
            date: to,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        }).on('changed.fu.datepicker', function (evt, date) {
            to = date;
            updateFields();
        }).on('click', function () {
            to = $(currentDocument.getElementById('datePickerToDiv')).datepicker('getDate');
            updateFields();
        });

        var updateFields = function () {
            $(currentDocument.getElementById('dateRangeStartAbsolute')).val(renderDateWithTimeZone(from));
            $(currentDocument.getElementById('dateRangeEndAbsolute')).val(renderDateWithTimeZone(to));
        };

        $("#dateRangeEnd").on('focusout', function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()))
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
            to=$(this).val()

            updateFields();
        });

        updateFields();
    }

    //Method that is invoked when date range is changed.
    function dateRangeChangedAction(currentDocument, index) {
        if($(currentDocument.getElementById('dateRangeEnumProductFreqTrue')).children().val() == "CUSTOM"  && $("#dateRangeEnumProductFreqTrue").is(':visible')){
            $(".fromDateChanged").show();
            $(".toDateChanged").show();
            $("#dateRangeStartProductFreqTrue").show();
            $("#dateRangeEndProductFreqTrue").show();
            $("#frequency").show();
        }
            $("#dateRangeStartProductFreqTrue").hide();
            $("#dateRangeEndProductFreqTrue").hide();
            var valueChanged = currentDocument.getElementById('dateRangeEnum').value;
            if(!valueChanged){
                valueChanged = $('#dateRangeEnum').val();
            }
            if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
                initializeDatePickersForEdit(currentDocument, index);
                $(currentDocument.getElementById('datePickerFromDiv')).show();
                $(currentDocument.getElementById('datePickerToDiv')).show();
                $(currentDocument.getElementById('relativeDateRangeValue')).hide();
                hideErrorOfXEnums(currentDocument)

            } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
                $(currentDocument.getElementById('datePickerFromDiv')).hide();
                $(currentDocument.getElementById('datePickerToDiv')).hide();
                hideErrorOfXEnums(currentDocument)

            } else {
                if (_.contains(X_OPERATOR_ENUMS, valueChanged)) {
                    $(currentDocument.getElementById('relativeDateRangeValue')).show();

                } else {
                    $(currentDocument.getElementById('relativeDateRangeValue')).hide();
                    hideErrorOfXEnums(currentDocument)
                }
                $(currentDocument.getElementById('datePickerFromDiv')).hide();
                $(currentDocument.getElementById('datePickerToDiv')).hide();
            }
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
});

function checkNumberFields() {
    var validNumber = true;
    $.each($('.relativeDateRangeValue'), function () {
        if (!Number(this.value)) {
            validNumber = false;
            $(this).parent().addClass('has-error');
            $(this).parent().find('.notValidNumberErrorMessage').show();
            $("#saveRun").prop('disabled', false);
        } else {
            $(this).parent().removeClass('has-error');
            $(this).parent().find('.notValidNumberErrorMessage').hide();
        }
    });

    return validNumber;
}

function hideErrorOfXEnums(currentDocument) {
    $(currentDocument.getElementById('relativeDateRangeValue')).hide();
    $(currentDocument.getElementById('relativeDateRangeValue')).val("1"); //By default value is also 1
    $(currentDocument.getElementById('relativeDateRangeValue')).siblings('.notValidNumberErrorMessage').hide();
    $(currentDocument.getElementById('relativeDateRangeValue')).parent().removeClass('has-error')
}
