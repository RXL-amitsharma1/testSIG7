var signal = signal || {}

window.actionButton = function (tablebody) {
    var selectedCell;
    var selector = $(tablebody).find('tbody');

    $(selector).on('mouseover', 'td', function () {
        var defaultValueEle = $(this).find('.default-value')
        defaultValueEle.addClass('dataTableHideCellContent')

        var foundDiv = $(this).find("div.btn-group");
        foundDiv.parent().addClass('no-padding');
        foundDiv.removeClass('dataTableHideCellContent');

    }).on('mouseleave', 'td', function () {
        var foundDiv = $(this).find("div.btn-group");
        if (!foundDiv.hasClass('open')) {
            foundDiv.addClass('dataTableHideCellContent');
            var defaultValueEle = $(this).find('.default-value')
            defaultValueEle.removeClass('dataTableHideCellContent')
        }

    }).on('click', 'td', function () {
        var foundDiv = $(this).find("div");
        if (!foundDiv.hasClass('open')) {
            selectedCell = $(this);
        } else {
            selectedCell = $(this);
        }
        $(this).on('focusout', function () {
        })
    })
}