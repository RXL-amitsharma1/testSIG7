$(function () {
    var startDate = $("#startDate_freq").val()
    var endDate = $("#endDate_freq").val()
    $('#start-date-picker').datepicker({
        allowPastDates: true,
        date: new Date(startDate),
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    $('#end-date-picker').datepicker({
        allowPastDates: true,
        date: new Date(endDate),
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
});

$(function () {

    $('form').on('submit', function(event) {
        var currentForm = this;
        event.preventDefault();

        bootbox.confirm({
            title: 'Update Substance Frequency ',
            message: confirmationMessage,
            buttons: {
                confirm: {
                    label: 'Update',
                    className: 'btn-primary'
                },
                cancel: {
                    label: 'Cancel',
                    className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    currentForm.submit();
                } else {
                    event.preventDefault();
                }
            }
        });
    });

});

