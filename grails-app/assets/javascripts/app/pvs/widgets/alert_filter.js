$(function() {
    var init = function() {
        $('#filters .panel-heading').on('click', function () {
            var icon = $(this).find(".show-filter")

            if (icon.attr('class').match('fa-caret-right')) {
                icon.removeClass('fa-caret-right')
                icon.addClass('fa-caret-down')
            } else {
                icon.addClass('fa-caret-right')
                icon.removeClass('fa-caret-down')
            }
        })

        init_datepickers()
    }

    var init_datepickers = function() {
        var detected_fromDate = $('#detected-date-from').datepicker('getFormattedDate')
        var detected_toDate = $('#detected-date-to').datepicker('getFormattedDate')
    }

    init()
})