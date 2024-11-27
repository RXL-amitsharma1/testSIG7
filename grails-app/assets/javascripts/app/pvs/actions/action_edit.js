$(function() {
    var init = function() {
        $('#action-editor-form #due-date-picker').datepicker({
            date: $('#dueDate').attr('data-date'),
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });

        $('#action-editor-form #completion-date-picker').datepicker({
            date: $('#completedDate').attr('data-date'),
            allowPastDates: true,
            restricted: [{from: TOMORROW , to: Infinity}],
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('inputParsingFailed.fu.datepicker',function (e) {
            $('#completedDate').val('');
        });

        $('#action-editor-form #actionStatus').on('change', function () {
            if ($(this).val() === 'Closed') {
                var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                $('#action-editor-form #completion-date-picker').find('#completedDate').val(completedDate);
            }
        });
    }

    init()
})