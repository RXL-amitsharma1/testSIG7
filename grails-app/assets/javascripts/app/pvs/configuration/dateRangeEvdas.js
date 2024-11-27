DATE_RANGE_ENUM = {
    CUSTOM: 'CUSTOM',
    CUMULATIVE: 'CUMULATIVE'
};
DATE_RANGE_TYPE = {
    SUBMISSION_DATE: 'submissionDate'
};
DATE_DISPLAY = "MM/DD/YYYY";
DATE_FMT_TZ = "YYYY-MM-DD";
$(function () {
    var asOf = null;

    var init = function () {
        $(".showHeaderFooterArea").on('click', showAdvancedOption);
        $(".showAdvancedOption").on('click', showAdvancedOption);
        $("#adhocRun").trigger('change');
        if(!($("#disableOldDictionaryInit").val()==="true")) {
            $(".clearProductValues").on('click', function () {
                $('.addProductValues').prop('disabled', false)
            });
            $(".addAllProducts").on('click', function () {
                var dateRangeEnum = $('#dateRangeEnum').val();
                if ((typeof pageName === 'undefined' || pageName !== 'Reporting') && dateRangeEnum !== 'CUMULATIVE') {
                    fetchSubstanceFrequencyProps();
                }
            })
        }
        $(".dateRangeEnumClass").trigger('change');
    };

    function renderDateWithTimeZone(date) {
        var parseDate = moment(date).format(DATE_FMT_TZ);
        return parseDate;
    }

    var updateInputField = function () {
        $('#asOfVersionDateValue').val(renderDateWithTimeZone(asOf));
    };

    $("#dateRangeType").select2({
        placeholder: "Select Date Range type"
    });

    $(document).on('change', '#dateRangeType', function () {
        checkDateRangeType();
    });

    updateInputField();
    checkDateRangeInEdit(document);
    checkDateRangeType();

    $(document).on('change', '.dateRangeEnumClass', function () {
        var elementId = (this.id);
        var index = elementId.replace(/[^0-9\.]+/g, "");
        var dateRangeEnum = $('#dateRangeEnum').val();
        if (dateRangeEnum === DATE_RANGE_ENUM.CUSTOM && !$('#adhocRun').is(":checked")) {
            $("#substanceFrequency").show();
        } else{
            $("#substanceFrequency").hide();
        }
        dateRangeChangedAction(document, parseInt(index))
    });

    $('.glyphicon-calendar').on('click', function () {

        var component = $(document).find(".fromDateChanged");
        _.each(component, function (dateRangeDiv, index) {

            if (index < component.length - 1) {
                dateRangeChangedAction(document, parseInt(index))
            }
        })
    });

    $("#adhocRun").on('change', function () {
        if ($(this).is(":checked")) {
            $("#substanceFrequency").hide();
            $("#toDate").attr("disabled", true);
            $("#priority").attr('disabled', true);
            $("#myScheduler :input").attr("disabled", true);
        } else {
            var dateRangeEnum = $('#dateRangeEnum').val();
            if (dateRangeEnum === DATE_RANGE_ENUM.CUSTOM){
                $("#substanceFrequency").show();
            } else{
                $("#substanceFrequency").hide();
            }
            $("#toDate").attr('disabled', true);
            $("#priority").attr("disabled", false);
            $("#myScheduler :input").attr("disabled", false);
        }
        var elementId = ('dateRangeSelector');
        var index = elementId.replace(/[^0-9\.]+/g, "");
        dateRangeChangedAction(document, parseInt(index))
    });

    $("#fromDate").on('change', function () {
        $("#toDate").get(0).selectedIndex = $("#fromDate :selected").index();
        $("#toDate> option").each(function () {
            if (this.index < $("#fromDate :selected").index()) {
                $(this).attr('disabled', true);
            } else {
                $(this).attr('disabled', false);
            }
        });
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

    function checkDateRangeInEdit(currentDocument) {

        var component = $(currentDocument).find(".dateRange");
        _.each(component, function (dateRangeDiv, index) {
            if (index < component.length - 1) {
                dateRangeChangedAction(currentDocument, parseInt(index));
            }
        })
    }

    function initializeDatePickersForEdit(currentDocument, index) {
        var from = null;
        var to = null;

        if ($('#dateRangeStartAbsolute').val()) {
            from = $('#dateRangeStartAbsolute').val();
        }
        if ($('#dateRangeStartAbsolute').val()) {
            to = $('#dateRangeStartAbsolute').val();
        }

        $('#datePickerFromDiv').datepicker({
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
            from = $('#datePickerFromDiv').datepicker('getDate');
            updateFields();
        });

        $('#datePickerToDiv').datepicker({
            allowPastDates: true,
            date: to,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        }).on('changed.fu.datepicker', function (evt, date) {
            to = date;
            updateFields();
        }).on('click', function () {
            to = $('#datePickerToDiv').datepicker('getDate');
            updateFields();
        });

        var updateFields = function () {
            $('#dateRangeStartAbsolute').val(renderDateWithTimeZone(from));
            $('#dateRangeEndAbsolute').val(renderDateWithTimeZone(to));
        };

        function renderDateWithTimeZone(date) {
            var parseDate = moment(date).tz(userTimeZone).format(DATE_FMT_TZ);
            return parseDate;
        }

        updateFields();
    }

    function dateRangeChangedAction(currentDocument, index) {

        var valueChanged = currentDocument.getElementById('dateRangeEnum').value;

        if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
            initializeDatePickersForEdit(currentDocument, index);
            $('#dateRangeSelector').show();
            fetchSubstanceFrequencyProps();
        } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
            $('#dateRangeSelector').hide();
        }
    };

    function showAdvancedOption() {
        var advancedOptionDiv = $('#' + $(this).attr('for'));
        advancedOptionDiv.toggle();

        if (advancedOptionDiv.is(':visible')) {
            $(this).text(LABELS.labelHideAdavncedOptions);
        } else {
            $(this).text(LABELS.labelShowAdavncedOptions);
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
    }

    function checkIncludeLockedVersions(lockedvalue) {
        if (typeof lockedvalue != "undefined" && lockedvalue != null) {
            if (lockedvalue.toLowerCase().indexOf('latest') != -1) {
                $('#includeLockedVersion').removeAttr("disabled");
            } else {
                $('#includeLockedVersion').prop("checked", true);
                $('#includeLockedVersion').attr("disabled", true);
            }
        }
    }

    function fetchSubstanceFrequencyProps() {
        var substanceName;
        var parsedJSON;
        var productSelectionJSON = getDictionaryObject("product").getValues()[ingredientLevel].length > 0 ? JSON.stringify(getDictionaryObject("product").getValues()) : $("#productSelection").val();
        var productGroupSelectionJSON = $("#productGroupSelection").val();
        if (productSelectionJSON) {
            parsedJSON = JSON.parse(productSelectionJSON);
            if (parsedJSON[1][0]) {
                 substanceName = parsedJSON[1][0].name;
            }
        } else if(productGroupSelectionJSON){
            parsedJSON = JSON.parse(productGroupSelectionJSON);
            if (parsedJSON[0]) {
                substanceName = parsedJSON[0].name.substring(0,parsedJSON[0].name.lastIndexOf('(')-1);
            }
        }
        if (substanceName) {
            var data = {
                'substanceName': substanceName,
                'isAdhocRun': $("#adhocRun").is(":checked")
            };
            $.ajax({
                type: "POST",
                url: substanceFrequencyPropertiesUrl,
                data: data,
            })
                .done(function (result) {
                    if (result.miningFrequency) {
                        $("#substanceFrequency").html(result.miningFrequency + " Frequency");
                        $("#frequency").val(result.frequencyName)
                    }
                    if (result.probableStartDate && result.probableEndDate) {
                        var fromDateHtml = '<option value="null">--Select One--</option>';
                        result.probableStartDate.forEach(function (data) {
                            fromDateHtml += '<option value="' + data + '">' + data + '</option>'
                        });
                        var startDateAbsoluteCustomFreq = $("#startDateAbsoluteCustomFreq").val();
                        $("#fromDate").html(fromDateHtml);
                        $("#fromDate").val(startDateAbsoluteCustomFreq);
                        var toDateHtml = '<option value="null">--Select One--</option>';
                        result.probableEndDate.forEach(function (data) {
                            toDateHtml += '<option value="' + data + '">' + data + '</option>'
                        });
                        var endDateAbsoluteCustomFreq = $("#endDateAbsoluteCustomFreq").val();
                        $("#toDate").html(toDateHtml);
                        $("#toDate").val(endDateAbsoluteCustomFreq)
                    }
                });
        }
    }

    init()
});
