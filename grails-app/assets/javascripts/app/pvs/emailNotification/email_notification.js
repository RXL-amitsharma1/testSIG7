$(function () {
    $('.pv-switch input').each((ind, item) => {
        $(item).prop('checked', $(item).attr('data-value') === 'true')
    })
});


