//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js

DATE_DISPLAY = "MM/DD/YYYY";

$(function () {
    var matched_alerts_table
    $('#deleteAttachment a[href]').on('click', function () {
     $(this).addClass('disabled-link');
    });
    var init = function() {
        $('#sharedWith').select2();
        $('#groups').select2();
        $('#formulations').select2();
        $('#reportType').select2();
        $('#countryOfIncidence').select2();
        $('#evaluationMethods').select2();
        $('#actionTaken').select2();

        var detectedDate = $('#myDetectedDate').val();
        var tomorrow = new Date();
        tomorrow = new Date(tomorrow.setDate(tomorrow.getDate() + 1));
        $('#aggStartDatePicker').datepicker({
            allowPastDates: true,
            date: moment($('#myAggStartDate').val()),
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        });
        $('#aggEndDatePicker').datepicker({
            allowPastDates: true,
            date: moment($('#myAggEndDate').val()),
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        });
        $('#lastDecisionDatePicker').datepicker({
            allowPastDates: true,
            date: moment($('#myLastDecisionDate').val()),
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        });
        $('#haDateClosedDatePicker').datepicker({
            allowPastDates: true,
            date: moment($('#myHADateClosed').val()),
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        });

        $('#detectedDatePicker').datepicker({
            allowPastDates: true,
            restricted: [{
                from: TOMORROW,
                to: Infinity
            }],
            date: moment(detectedDate),
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            },
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });

        $('#editDetectedDatePicker').datepicker({
            allowPastDates: true,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        });

        init_matching_alert_bt();
        matched_alerts_table = init_matched_alerts_table()
    };

    var init_matching_alert_bt = function() {
        $('#matching-alert-btn').on('click',
            function(evt) {
                signal.utils.enable_load_button($('#matching-alert-btn'), true)();
                show_matched_alerts();
                signal.utils.enable_load_button($('#matching-alert-btn'), false)()
            }
        );

        $('#matched-alert-modal').on('shown.bs.modal', function (event) {
            var productName = get_current_product_name();
            var event = get_current_event();
            var topic = get_current_topic();
            var alertId = get_current_alert_id();

            var url = "/signal/adHocAlert/findMatchedAlerts?productName=" + productName +
                "&topic=" + topic + "&event=" + event + "&alertId=" + alertId;
            url = encodeURI(url);
            enable_matched_alerts_update_bt(false);
            reload_matched_alerts_table(url);

            var datatable = $('#matched-alerts-table').DataTable();
            var rows = datatable.rows();

            if (rows.length > 0)
                enable_matched_alerts_update_bt(true)
        });

        $('#matched-alert-modal #matched-alerts-update-bt').on('click', function(evt) {
            evt.preventDefault();
            var datatable = $('#matched-alerts-table').DataTable();
            var rows = datatable.rows();
            if (rows.length > 0)
                $('input#issuePreviouslyTracked').prop('checked', true);

            $('#matched-alert-modal').modal("hide")
        })
    };

    var enable_matched_alerts_update_bt = function(enable) {
        $('#matched-alert-modal #matched-alerts-update-bt').prop('disabled',!enable);
        if (enable)
            $('#matched-alert-modal #matched-alerts-update-bt').removeClass('disabled');
        else
            $('#matched-alert-modal #matched-alerts-update-bt').addClass('disabled');
    };

    var init_matched_alerts_table = function() {
        return signal.alerts_utils.init_matched_alerts_table(
            $('#matched-alerts-table'), "/signal/adHocAlert/findMatchedAlerts?productName=")
    };

    var show_matched_alerts = function() {
        $('#matched-alert-modal').modal()
    };

    var reload_matched_alerts_table = function(url) {
        var table = $('#matched-alerts-table').DataTable();
        table.ajax.url(url).load()
    };

    var get_current_product_name = function() {
        var prdName = $('#showProductSelection').find('div').text();
        if(prdName === "")
            prdName = $('#showGenericSelection').find('div').text();
        if (prdName) {
            prdName = prdName.replace('(Product Name)', '').trim();
        }
        return prdName
    };

    var get_current_event = function() {
        var event = $("#eventSelection").val();
        return event
    };

    var get_current_topic = function() {
        return $('#topic').val().trim()
    };

    var get_current_alert_id = function() {
        return $('#alertId').val()
    };

    init()
});
