$(function () {


    var columns = create_prevCount_table_columns();

    $('#prevCountAndScoresTable').DataTable({

        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },

        "ajax": {
            "url": prevScoresUrl + "&alertId=" + alertId + "&alertType=" + alertType,
            "dataSrc": "aaData"
        },

        "bLengthChange": true,
        "bProcessing": true,
        "columnDefs": [ { type: 'date', 'targets': [0] } ],
        "order": [[ 0, 'asc' ]],
        "oLanguage": {

            "sZeroRecords": $.i18n._('noDataAvailable'), "sEmptyTable": $.i18n._('noDataAvailable'),
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
        "scrollX": true,
        "iTotalDisplayRecords": "5",
        "aoColumns": columns,
        "responsive": true
    });
});


var create_prevCount_table_columns = function () {
    var aoColumns = [

        {
            "mData": "xAxisTitle",
            'className': 'col-min-100 text-center',
            "mRender": function (data, type, row) {
                var dateRange = row.xAxisTitle;
                if (!dateRange) {
                    if(typeof row.xAxisTitle_ev != UNDEFINED){
                        dateRange = row.xAxisTitle_ev;
                    }else{
                        dateRange = row.xAxisTitle_faers;
                    }
                }
                return dateRange + ' *';
            },
            "visible": true
        },
        {
            "mData": "newFatal_pva",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    row.newFatal_pva == -1 ? "-" : row.newFatal_pva,
                    row.cummFatal_pva == -1 ? "-" : row.cummFatal_pva)
            },
            className: 'text-center',
            "visible": true
        },
        {
            "mData": "newCount_pva",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    row.newCount_pva == -1 ? "-" : row.newCount_pva,
                    row.cummCount_pva == -1 ? "-" : row.cummCount_pva)
            },
            className: 'text-center',
            "visible": true
        },
        {
            "mData": "prr_pva",
            "mRender": function (data, type, row) {
                return (row.prr_pva == -1 ? "-" : row.prr_pva)
            },
            'className': 'col-min-30 text-center',
            "visible": true
        },
        {
            "mData": "ror_pva",
            "mRender": function (data, type, row) {
                return (row.ror_pva == -1 ? "-" : row.ror_pva)
            },
            'className': 'col-min-30 text-center',
            "visible": true
        },
        {
            "mData": "newEvpm_ev",
            'className': 'text-center',
            "mRender": function (data, type, row) {
                return '<div><div class="stacked-cell-center-top">' + (row.newEvpm_ev == -1 ? "-" : row.newEvpm_ev) + '</div>' +
                    '<div class="stacked-cell-center-top">' + (row.totalEvpm_ev == -1 ? "-" : row.totalEvpm_ev) + '</div></div>'
            },
            "visible": true
        },
        {
            "mData": "ime_ev",
            "mRender": function (data, type, row) {
                return (row.ime_ev == -1 ? "-" : row.ime_ev)
            },
            'className': 'col-min-30 text-center',
            "visible": true
        },
        {
            "mData": "newFatal_ev",
            'className': 'text-center',
            "mRender": function (data, type, row) {
                return '<div><div class="stacked-cell-center-top">' + (row.newFatal_ev == -1 ? "-" : row.newFatal_ev) + '</div>' +
                    '<div class="stacked-cell-center-top">' + (row.totalFatal_ev == -1 ? "-" : row.totalFatal_ev) + '</div></div>'
            },
            "visible": true
        },
        {
            "mData": "newPaed_ev",
            'className': '',
            "mRender": function (data, type, row) {
                return '<div><div class="stacked-cell-center-top">' + (row.newPaed_ev == -1 ? "-" : row.newPaed_ev) +
                    '</div><div class="stacked-cell-center-top">' + (row.totalPaed_ev == -1 ? "-" : row.totalPaed_ev) + '</div></div>'
            },
            "visible": true
        },
        {
            "mData": "sdrPaed_ev",
            'className': 'col-min-30 text-center',
            "mRender": function (data, type, row) {
                return '<div><div class="stacked-cell-center-top">' + (row.sdr_ev == -1 ? "-" : row.sdr_ev) +
                    '</div><div class="stacked-cell-center-top">' + (row.sdrPaed_ev == -1 ? "-" : row.sdrPaed_ev) + '</div></div>'
            },
            "visible": true
        },
        {
            "mData": "changes_ev",
            "mRender": function (data, type, row) {
                return (row.changes_ev == -1 ? "-" : row.changes_ev)
            },
            'className': 'col-min-30 text-center',
            "visible": true
        },
        {
            "mData": "rorAll_ev",
            "mRender": function (data, type, row) {
                return (row.rorAll_ev == -1 ? "-" : row.rorAll_ev)
            },
            'className': 'col-min-30 text-center',
            "visible": true
        },
        {
            "mData": "newCounts_faers",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    row.newCounts_faers == -1 ? "-" : row.newCounts_faers,
                    row.cummCounts_faers == -1 ? "-" : row.cummCounts_faers)
            },
            className: 'text-center',
            "visible": true
        },
        {
            "mData": "eb05_faers",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    '<span class="EB05">' + (row.eb05_faers == -1 ? "-" : row.eb05_faers) + '</span>',
                    '<span class="EB95">' + (row.eb95_faers == -1 ? "-" : row.eb95_faers) + '</span>'
                )
            },
            className: 'text-center',
            "visible": true
        }];

    return aoColumns
};
