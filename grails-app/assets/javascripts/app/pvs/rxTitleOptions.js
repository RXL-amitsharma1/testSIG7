//Second parameter takes an array of indexes of columns that are not displayed by default
function loadTableOption(tableID, arrIndex, alertName) {
    var table = $(tableID).DataTable();
    var tableColumns = $('#tableColumns');
    var columns = $(tableID).DataTable().settings()[0].aoColumns;// fnSettings().aoColumns;
    var countVisible = 0;
    var trClass;
    var actionsButtons = 0;
    arrIndex = arrIndex || [];
    alertName = alertName || '';

    $.each(columns, function (i, v) {
        if ($.inArray(i, arrIndex) != -1) {
            table.column(i).visible(false)
        }
    });
    $.each(columns, function (k, v) {
        if (table.column(k).visible()) {
            countVisible++;
        }
    });
    $.each(columns, function (i, v) {
        if (countVisible == 1) {
            var checked = 'checked="checked" disabled="true"';
            trClass = 'rxmain-dropdown-settings-table-disabled';
        } else {
            var checked = 'checked="checked"';
            trClass = 'rxmain-dropdown-settings-table-enabled';
        }
        if (!table.column(i).visible()) {
            checked = '';
            trClass = 'rxmain-dropdown-settings-table-enabled';
        }
        var colWidth = $(tableID + ' th:eq(' + i + ')').width();
        if (v.sTitle != '<div class="th-label"></div>') {
            var columnName = v.sTitle;
            var columnEle = $.parseHTML(v.sTitle);
            if (columnEle && v.fixFiltered) {
                var htmlStr = '';

                for (var i = 0; i < columnEle.length; i++) {
                    if (columnEle[i].nodeType == 1 && columnEle[i].className.indexOf('col-filter') == -1) {
                        htmlStr += columnEle[i].outerHTML;
                    }
                }

                columnName = htmlStr;
            }
            if (columnName && columnName != '' && !$(columnEle).hasClass('select-all-alert-config')) {
                columnName = columnName.split('stacked-cell-center').join('stacked-cell-left');
                // if checkbox column is found hide it or do not add to list
                var addColumn = '<tr class="' + trClass + '">' +
                    '<td>' + columnName + '</td>' +
                    '<td style="text-align: center;"><input type="checkbox" ' + checked + ' class="chk-table-columns" data-columns="' + i + '" /></td>' +
                    '</tr>';
                $(addColumn).appendTo(tableColumns);
            } else {
                actionsButtons = 1;
            }
        }
    });
    $('.dropdown-menu input, .dropdown-menu label, .dropdown-menu button, .dropdown-menu select, .dropdown-menu .rxmain-container-dropdown').on('click', function (e) {
        e.stopPropagation();
    });
    $('.chk-table-columns').on('change', function (e) {
        e.preventDefault();
        var column = table.column($(this).attr('data-columns'));
        column.visible(!column.visible());
        var countNotVis = 0;
        $.each(columns, function (i, v) {
            if (!table.column(i).visible()) {
                countNotVis++;
            }
        });
        countNotVis = columns.length - countNotVis;
        if (countNotVis == (1 + actionsButtons)) {
            $('.chk-table-columns').each(function () {
                if (this.checked) {
                    $(this).attr("disabled", true);
                    var findRow = $(this).closest('tr');
                    $(findRow).removeClass('rxmain-dropdown-settings-table-enabled');
                    $(findRow).addClass('rxmain-dropdown-settings-table-disabled');
                }
            });
        } else {
            $('.chk-table-columns').each(function () {
                $(this).removeAttr("disabled");
                var findRow = $(this).closest('tr');
                $(findRow).removeClass('rxmain-dropdown-settings-table-disabled');
                $(findRow).addClass('rxmain-dropdown-settings-table-enabled');
            });
            $('.yadcf-filter-wrapper').hide();
        }
    });
    $('.btn-change-width').on('click', function (e) {
        var getWith = $('#col' + $(this).attr('data-columns')).val();
        var colId = $(this).attr('data-columns');
        $(tableID + ' th:eq(' + colId + ')').width(getWith);
    });
    $('#tableColumns tr').on('click', function (event) {
        if (event.target.type !== 'checkbox') {
            $(':checkbox', this).trigger('click');
        }
    });

}
