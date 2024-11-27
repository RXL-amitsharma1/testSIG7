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
        $("#productSelection").on('change', function () {
            if (typeof pageName === 'undefined' || pageName !== 'Reporting') {
                $("#previousStartDate").val('');
                $("#previousEndDate").val('');
                fetchSubstanceFrequencyProps();
            }
        });
    };

    $("#fromDate").on('change', function () {
        $("#toDate").get(0).selectedIndex = $("#fromDate :selected").index();
        $("#previousStartDate").val($("#fromDate :selected").prev().val());
        $("#previousEndDate").val($("#toDate :selected").prev().val());
        $("#toDate> option").each(function () {
            if (this.index < $("#fromDate :selected").index()) {
                $(this).attr('disabled', true);
            } else {
                $(this).attr('disabled', false);
            }
        });

    });

    function fetchSubstanceFrequencyProps() {
        var selectedProduct = $("#productSelection").val();
        if (typeof selectedProduct != "undefined" && selectedProduct != "") {
            var data =  {'productName': selectedProduct,'selectedDatasource':$('#selectedDatasource').val() };
            $.ajax({
                type: "POST",
                url: substanceFrequencyPropertiesUrl,
                data: data,
            })
                .done(function (result) {
                    if (result.probableStartDate && result.probableEndDate && result.startDate && result.endDate) {
                        var fromDateHtml = '<option value="null">--Select One--</option>';
                        result.probableStartDate.forEach(function (data) {
                            fromDateHtml += '<option value="' + data + '">' + data + '</option>'
                        });
                        $("#fromDate").html(fromDateHtml);
                        $("#fromDate").val(result.startDate);
                        var toDateHtml = '<option value="null">--Select One--</option>';
                        result.probableEndDate.forEach(function (data) {
                            toDateHtml += '<option value="' + data + '">' + data + '</option>'
                        });
                        $("#toDate").html(toDateHtml);
                        $("#toDate").val(result.endDate);
                        $("#frequency").val(result.frequency);
                        $('#search').prop('disabled', false);
                    }else{
                        $('#search').prop('disabled', true);
                        $("#fromDate").val('');
                        $("#toDate").val('');

                    }
                    $("#fromDate").trigger('change')
                });
        }
    }

    init()
});
