$(function () {


    var columns = createOtherHistoryTableColumns();

    $('#reviewHistoryOtherSourceTable').DataTable({

        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },

        "ajax": {
            "url": otherSourcesHistoryUrl + "&alertType=" + alertType + "&alertId=" + alertId,
            "dataSrc": "aaData"
        },
        drawCallback: function (settings) {
            tagEllipsis($('#reviewHistoryOtherSourceTable'));
            colEllipsis();
            webUiPopInit();
        },

        "bLengthChange": true,
        "bProcessing": true,
        "oLanguage": {

            "sZeroRecords": $.i18n._('noDataAvailable'), "sEmptyTable": "No data available in table",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu":"Show _MENU_",
            "sInfo":"of _TOTAL_ entries",
            "sInfoFiltered": "",
        },
        "aLengthMenu": [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        "pagination": true,
        "iTotalDisplayRecords": "5",
        "aoColumns": columns,
        "responsive": true
    });
});

var createOtherHistoryTableColumns = function () {
    var aoColumns = [

        {
            "mData": "dataSource",
            'className': 'col-min-30 dt-center',
            "visible": true
        },
        {
            "mData": "alertName",
            'className': 'col-min-30 dt-center',
            "visible": true
        },
        {
            "mData": "disposition",
            'className': 'col-min-30 dt-center',
            "visible": true
        },
        {
            "mData": "justification",
            "className": "dt-center justification-cell col-min-150 col-max-200 cell-break",
            "mRender": function (data, type, row) {
                var colElement = '<div class="col-container"><div class="col-height">';
                colElement += encodeToHTML(row.justification);
                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.justification)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                colElement += '</div></div>';
                return colElement;
            },
            "visible": true
        },
        {
            "mData": "performedBy",
            'className': 'col-min-30 dt-center',
            "visible": true
        },
        {
            "mData": "date",
            'className': 'col-min-30 dt-center',
            "visible": true
        }
    ];

    return aoColumns
};